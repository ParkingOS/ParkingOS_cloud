package com.zld.struts.request;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.AjaxUtil;
import com.zld.sdk.tcp.NettyChannelMap;
import com.zld.service.DataBaseService;
import com.zld.utils.Check;
import com.zld.utils.StringUtils;

public class SendMessageByTCPAction extends Action {
	
	private Logger logger = Logger.getLogger(SendMessageByTCPAction.class);
	@Autowired
	DataBaseService dataBaseService ;
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = request.getParameter("action");
		if(action == null){
			action = "";
		}else if(action.equals("sendliftrodmsg")){
			//发送抬杆消息数据到收费系统
			String comid = request.getParameter("comid");
			//获取下传数据的通道信息
			Channel channel = NettyChannelMap.get(getChannel(comid));
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
			doBackMessage(json.toString(), channel);
			StringUtils.ajaxOutput(response,"1");
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
			params.put(name,AjaxUtil.decodeUTF8(valueStr));
		}
		logger.error(">>>>>>>>>>>>>>泊链tcp转发消息，数据："+params);
		String message = StringUtils.createJson(params);
		String pass = (String)params.get("comid");
		String localId = (String)params.get("local_id");
		if(!Check.isEmpty(localId)){
			pass += "_"+localId;
		}
		Channel channel = NettyChannelMap.get(pass);
		boolean isSend = doBackMessage(message, channel);
		doBackBusiness(params,isSend);
		logger.error(">>>>>>>>>>>>>>泊链tcp转发消息，结果："+isSend);
		StringUtils.ajaxOutput(response,"{\"state\":"+isSend+"}");
		
		/*if("".equals(action)){
			String park_id = request.getParameter("park_id");
			String data = request.getParameter("data");
			String serviceName = request.getParameter("service_name");
			Channel clientChannel = NettyChannelMap.get(park_id.toString());
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("service_name", serviceName);
			jsonObj.put("data", data);
			String sendMessage = jsonObj.toString();
			logger.error("同步信息到车场" + sendMessage + ",channel:" + clientChannel);
			if (clientChannel != null && clientChannel.isActive()
					&& clientChannel.isWritable()) {
				byte[] req = ("\n" + sendMessage + "\r").getBytes("UTF-8");
				ByteBuf buf = Unpooled.buffer(req.length);
				buf.writeBytes(req);
				clientChannel.writeAndFlush(buf);
				logger.error("已经同步消息到停车场SDK...");
				String message = "Send Successed";
				try {
					AjaxUtil.ajaxOutput(response, message);
				} catch (IOException e) {
					logger.error("发送返回值信息出现异常，检查后台程序");
					e.printStackTrace();
				}
			} else {
				logger.error("与停车场的连接异常，客户端SDK已掉线...");
			}
		}
		if("sendData".equals(action)){
			String park_id = request.getParameter("park_id");
			String data = request.getParameter("data");
			Channel clientChannel = NettyChannelMap.get(park_id.toString());
			logger.error("同步信息到车场" + data + ",channel:" + clientChannel);
			if (clientChannel != null && clientChannel.isActive()
					&& clientChannel.isWritable()) {
				byte[] req = ("\n" + data + "\r").getBytes("UTF-8");
				ByteBuf buf = Unpooled.buffer(req.length);
				buf.writeBytes(req);
				clientChannel.writeAndFlush(buf);
				logger.error("已经同步消息到停车场SDK...");
				String message = "{\"state\":1,\"message\":\"Send Successed\"}";
				try {
					AjaxUtil.ajaxOutput(response, message);
				} catch (IOException e) {
					logger.error("发送返回值信息出现异常，检查后台程序");
					e.printStackTrace();
				}
			} else {
				logger.error("与停车场的连接异常，客户端SDK已掉线...");
				String message = "{\"state\":0,\"message\":\"Send fail\"}";
				try {
					AjaxUtil.ajaxOutput(response, message);
				} catch (IOException e) {
					logger.error("发送返回值信息出现异常，检查后台程序");
					e.printStackTrace();
				}
			}
		}*/
		return null;
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
							int re = dataBaseService.update("update order_tb set islocked=? where id =? ", new Object[]{state,orderId});
							logger.error("锁车业务，操作失败，结果："+re);
						}
					}
				}
			}
		}
	}

	/**
	 * 消息返回
	 * 
	 * @param mesg
	 * @param data
	 */
	private boolean doBackMessage(String mesg, Channel channel) {
		if (channel != null && channel.isActive()
				&& channel.isWritable()) {
			try {
				logger.error("发消息到SDK，channel:"+channel+",mesg:" + mesg);
				byte[] req= ("\n" + mesg + "\r").getBytes("utf-8");
				ByteBuf buf = Unpooled.buffer(req.length);
				buf.writeBytes(req);
				ChannelFuture future = channel.writeAndFlush(buf);
				return true;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else{
			logger.error("客户端已断开连接...");
		}
		return false;
	}
	
	/**
	 * 获取下发数据的TCP通道
	 * @param comid
	 * @return
	 */
	private String getChannel(String comid){
		String channelPass = "";
		Map parkMap = dataBaseService.getMap("select * from park_token_tb where park_id=?", new Object[]{comid});
		if(parkMap != null && !parkMap.isEmpty()){
			String localId = String.valueOf(parkMap.get("local_id"));
			if(!Check.isEmpty(localId)){
				channelPass += comid+"_"+localId;
			}
		}else{
			channelPass = comid;
		}
		return channelPass;
	}
}
