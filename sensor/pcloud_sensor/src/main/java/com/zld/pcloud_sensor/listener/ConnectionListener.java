package com.zld.pcloud_sensor.listener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.zld.pcloud_sensor.Constants;
import com.zld.pcloud_sensor.SensorClient;

public class ConnectionListener implements ChannelFutureListener {
	
	Logger logger = Logger.getLogger(ConnectionListener.class);
	
	private SensorClient client;
	
	public ConnectionListener (SensorClient client) {
		this.client = client;
	}
	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		if(!future.isSuccess()){
			logger.error("客户端连接失败，30秒后准备重连...");
			EventLoopGroup group = future.channel().eventLoop();
			group.schedule(new Runnable() {
				
				@Override
				public void run() {
					try {
						logger.error("客户端开始重连...");
						client.connect(Constants.TB_ADDR, Constants.TB_PORT);
					} catch (Exception e) {
						logger.error("重连异常", e);
					}
				}
			}, 30, TimeUnit.SECONDS);
		}else{
			logger.error("客户端连接成功...");
		}
	}

}
