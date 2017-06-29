package com.zld.sdk.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;


import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

public class DownloadUtil {
	
	Logger logger = Logger.getLogger(DownloadUtil.class);
	
	
	/**
	 * 获取预支付订单到本地
	 * 参数信息：车场编号：comid，订单信息：order
	 * @param data
	 */
	public void downloadPrepaidOrder(JSONObject data){
		if(data!=null){
			String comId = data.getString("comid");
			Channel clientChannel = NettyChannelMap.get(comId);
			if(clientChannel != null){
				if(clientChannel.isActive()){
					if(clientChannel.isWritable()){
						logger.error("返回登录消息"+data);
						byte[] req = ("\n" + data + "\r").getBytes();
						ByteBuf buf = Unpooled.buffer(req.length);
						buf.writeBytes(req);
						clientChannel.writeAndFlush(buf);
					}else{
						logger.error("tcp通道不可正常写入-----do not write");
					}
				}else{
					logger.error("tcp通道未正常激活------do not active");
				}
			}else{
				logger.error("tcp通道建立异常");
			}
		}
	}
	
	/**
	 * 车场会员信息转发到本地
	 * @param data
	 */
	public void downloadUserInfo(JSONObject data){
		
	}
	
	/**
	 * 包月套餐信息转发到本地
	 * @param data
	 */
	public void downloadProductPackage(JSONObject data){
		
	}
	
	/**
	 * 车场价格改变或新加信息转发到本地
	 * @param data
	 */
	public void downloadPrice(JSONObject data){
		
	}
	
	/**
	 * 车型改变信息转发到本地
	 * @param data
	 */
	public void downloadCarType(JSONObject data){
		
	}
	
	/**
	 * 服务器初始化信息转发到本地
	 * @param data
	 */
	public void downloadServerInit(JSONObject data){
		
	}
	
}
