package parkingos.com.bolink.actions;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zld.common_dao.dao.CommonDao;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import parkingos.com.bolink.beans.OrderTb;
import parkingos.com.bolink.netty.NettyChannelMap;
import parkingos.com.bolink.utlis.Check;
import parkingos.com.bolink.utlis.CommonUtils;
import parkingos.com.bolink.utlis.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Controller
public class SendMessageByTCPAction{

	Logger logger = Logger.getLogger(UploadCarPics.class);
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
			//获取下传数据的通道信息
			Channel channel = NettyChannelMap.get(commonUtils.getChannel(comid));
			//封装抬杆数据
			JSONObject jsonObj = new JSONObject();
			JSONObject json = new JSONObject();
			//通道名称
			String channelName = request.getParameter("channelName");
			//通道号
			String channelId = request.getParameter("channelId");
			//道闸指令
			Integer operate = Integer.valueOf(request.getParameter("operate"));
			json.put("channel_name", URLDecoder.decode(channelName,"UTF-8"));
			json.put("channel_id", URLDecoder.decode(channelId,"UTF-8"));
			json.put("operate", operate);
			json.put("service_name", "operate_liftrod");
			commonUtils.doBackMessage(json.toString(), channel);
			StringUtils.ajaxOutput(response,"1");
			return ;
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
		String localId = (String)params.get("local_id");
		if(!Check.isEmpty(localId)){
			pass += "_"+localId;
		}
		Channel channel = NettyChannelMap.get(pass);
		boolean isSend = commonUtils.doBackMessage(message, channel);
		doBackBusiness(params,isSend);
		logger.error(">>>>>>>>>>>>>>泊链tcp转发消息，结果："+isSend);
		StringUtils.ajaxOutput(response,"{\"state\":"+isSend+"}");
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
							orderTb.setIsclick(state);
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
