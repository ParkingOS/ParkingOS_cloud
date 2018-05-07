package parkingos.com.bolink.actions;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zld.common_dao.dao.CommonDao;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import parkingos.com.bolink.beans.ComPassTb;
import parkingos.com.bolink.beans.OrderTb;
import parkingos.com.bolink.beans.ParkTokenTb;
import parkingos.com.bolink.netty.NettyChannelMap;
import parkingos.com.bolink.utlis.AjaxUtil;
import parkingos.com.bolink.utlis.Check;
import parkingos.com.bolink.utlis.CommonUtils;
import parkingos.com.bolink.utlis.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Controller
public class SendMessageByTCPAction{

	Logger logger = Logger.getLogger(SendMessageByTCPAction.class);
	@Autowired
	CommonDao commonDao;
	@Autowired
	CommonUtils commonUtils;

	@RequestMapping(value = "/sendmsgtopark.do")
	public void sendmsgtopark(HttpServletRequest request, HttpServletResponse response) throws Exception{
		String action = request.getParameter("action");
		if(action == null){
			action = "";
		}else if(action.equals("sendliftrodmsg")){
			//发送抬杆消息数据到收费系统
			String comid = request.getParameter("comid");
			//通道号
			String channelId = StringUtils.decodeUTF8(request.getParameter("channelId"));
			logger.info("抬杆通知："+channelId);
			String channel=null;
			if(Check.isNumber(channelId)){
				ComPassTb comPassTb = new ComPassTb();
				comPassTb.setId(Long.parseLong(channelId));
				comPassTb.setState(0);
				comPassTb =(ComPassTb) commonDao.selectObjectByConditions(comPassTb);
				if(comPassTb!=null){
					channel = comPassTb.getChannelId();
				}else{
					logger.info("中控抬杆通知发送消息===>>>>chnnel不存在");
					return;
				}
			}
			logger.info("抬杆通知："+channel);
			//获取下传数据的通道信息
			//Channel channel = NettyChannelMap.gets(commonUtils.getChannel(comid));
			ParkTokenTb tokenTb = commonUtils.getChannelInfo(comid,channel);//commonUtils.getChannel(Long.valueOf(comid));
			//封装抬杆数据
			JSONObject jsonObj = new JSONObject();
			JSONObject json = new JSONObject();
			//通道名称
			String channelName = request.getParameter("channelName");

			//道闸指令
			Integer operate = Integer.valueOf(request.getParameter("operate"));
			logger.info("中控抬杆通知发送消息===>>>>");
			if(tokenTb!=null){
				json.put("channel_name", URLDecoder.decode(channelName,"UTF-8"));
				json.put("channel_id", URLDecoder.decode(channel,"UTF-8"));
				json.put("operate", operate);
				json.put("service_name", "operate_liftrod");
				boolean isSend = commonUtils.doSendMessage(json.toString(),tokenTb);//commonUtils.doBackMessage(json.toString(), channel);
				logger.info("中控抬杆通知发送消息===>>>>:"+isSend);
				StringUtils.ajaxOutput(response,"1");
				return ;
			}else{
				logger.info("中控抬杆通知发送消息===>>>>通道为空发送失败！" );
				return ;
			}
		}
		Map<String,Object> params = new HashMap<String,Object>();
		Map<String,String[]> requestParams = request.getParameterMap();
		for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			if("is_locked".equals(name)){
				params.put(name,Integer.valueOf(valueStr));
			}else{
				params.put(name,StringUtils.decodeUTF8(valueStr));
			}
		}
		logger.error(">>>>>>>>>>>>>>泊链tcp转发消息，数据："+params);
		String message = JSON.toJSONString(params).toString();
		logger.error(">>>>>>>>>>>>>>json-message:"+message);
		String pass = (String)params.get("comid");
//		String localId = (String)params.get("local_id");
//		if(!Check.isEmpty(localId)){
//			pass += "_"+localId;
//		}
		//Channel channel = NettyChannelMap.gets(pass);
		ParkTokenTb tokenTb = commonUtils.getChannel(Long.valueOf(pass));
		boolean isSend = commonUtils.doSendMessage(message.toString(),tokenTb);//commonUtils.doBackMessage(message, channel);
		doBackBusiness(params,isSend);
		logger.error(">>>>>>>>>>>>>>泊链tcp转发消息，结果："+isSend);
		StringUtils.ajaxOutput(response,"{\"state\":"+isSend+"}");
	}

	@RequestMapping(value = "/sendmesgtopark")
	public void sendMesgToPark(HttpServletRequest request, HttpServletResponse response) throws Exception{
		String data = readBodyFormRequsetStream(request);
		logger.error(data);
		JSONObject jsonObject = JSONObject.parseObject(data);
		String channelId = jsonObject.getString("channelid");
		logger.error(channelId);
		Channel channel = NettyChannelMap.get(channelId);
		JSONObject sendData = jsonObject.getJSONObject("data");
		boolean isSend = commonUtils.doBackMessage(sendData.toString(), channel);
		logger.error(isSend);
		AjaxUtil.ajaxOutput(response,JSONObject.parseObject("{\"result\":"+isSend+"}"));
	}
	//取request流数据
	private  String readBodyFormRequsetStream(HttpServletRequest request) {
		try {
			int size = request.getContentLength();
			if (size > 0) {
				InputStream is = request.getInputStream();
				int readLen = 0;
				int readLengthThisTime = 0;
				byte[] message = new byte[size];
				while (readLen != size) {
					readLengthThisTime = is.read(message, readLen, size- readLen);
					if (readLengthThisTime == -1) {// Should not happen.
						break;
					}
					readLen += readLengthThisTime;
				}
				return new String(message,"utf-8");
			}

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return "";
	}
	/**
	 * 发送锁车消息，实现锁车业务
	 * @param params
	 * @param isSend
	 */
	private void doBackBusiness(Map<String,Object> params, boolean isSend) {
		String serviceName = (String)params.get("service_name");
		if(serviceName!=null&&serviceName.equals("lock_car")){//锁车业务
			if(params.containsKey("id")&&Check.isLong(params.get("id")+"")){
				Long orderId = Long.valueOf(params.get("id")+"");
				if(params.containsKey("is_locked")){
					Integer state = -1;
					String isLocked = (String) params.get("is_locked");
					if(isLocked!=null){
						if(isLocked.equals("1")){//锁车
							if(!isSend)
								state=3;
						}else if(isLocked.equals("0")){//解锁
							if(!isSend)
								state=5;
						}
						if(state>0){
							OrderTb orderTb = new OrderTb() ;
//							orderTb.setIsclick(state);
							orderTb.setIslocked(state);
							orderTb.setId(orderId);
							int re = commonDao.updateByPrimaryKey(orderTb);
							//int re = dataBaseService.update("update order_tb set islocked=? where id =? ", new Object[]{state,orderId});
							logger.error("锁车业务，操作失败，结果："+re);
						}
					}
				}
			}
		}
	}
}
