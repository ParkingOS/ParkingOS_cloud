package com.zld.pcloud_sensor.task;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class HeartBeatTask implements Runnable {
	
	Logger logger = Logger.getLogger(HeartBeatTask.class);
	
	private final ChannelHandlerContext ctx;
	
	public HeartBeatTask(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}
	@Override
	public void run() {
		logger.error("向天泊推送心跳消息");
		byte[] req = ("\n{'CID':-1,'V':0,'Err':1001}\r").getBytes();
		ByteBuf buf = Unpooled.buffer(req.length);
		buf.writeBytes(req);
		ctx.writeAndFlush(buf);
	}

}
