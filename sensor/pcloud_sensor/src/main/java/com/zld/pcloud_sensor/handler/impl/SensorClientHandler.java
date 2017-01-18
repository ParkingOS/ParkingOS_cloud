package com.zld.pcloud_sensor.handler.impl;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zld.pcloud_sensor.Constants;
import com.zld.pcloud_sensor.SensorClient;
import com.zld.pcloud_sensor.task.HeartBeatTask;
import com.zld.pcloud_sensor.util.NettyChannelMap;

@Service
@Sharable
public class SensorClientHandler extends ChannelHandlerAdapter {
	
	Logger logger = Logger.getLogger(SensorClientHandler.class);
	
	private int UNCONNECT_NUM = 0;//超时次数
	
	private SensorClient client;//车检器客户端
	
	private volatile ScheduledFuture<?> heartBeatTask;//定时任务
	
	public void setClient(SensorClient client) {
		this.client = client;
	}
	
	@Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.error("客户端通道已激活");
		NettyChannelMap.add(Constants.TB_CLIENT, ctx.channel());
		//每10秒向天泊推送消息,否则大约半分钟天泊服务器就会主动关闭连接
		heartBeatTask = ctx.channel().eventLoop()
				.scheduleAtFixedRate(new HeartBeatTask(ctx), 0, 10, TimeUnit.SECONDS);
    }
	
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		logger.error("客户端接收到天泊的回执消息:" + msg);
		UNCONNECT_NUM = 0;//收到回复消息就清空
    }
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		if(heartBeatTask != null){
			logger.error("取消发送心跳任务");
			heartBeatTask.cancel(true);
		}
    }
	
	@Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.error("客户端连接中断，30秒后准备重连...");
        ctx.close();
        EventLoopGroup group = ctx.channel().eventLoop();
        group.schedule(new Runnable() {
			
			@Override
			public void run() {
				try {
					logger.error("客户端开始重连...");
					client.connect(Constants.TB_ADDR, Constants.TB_PORT);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 30, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }
	
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			if(UNCONNECT_NUM > 10){
				logger.error("超时超过10次，重新发起连接");
				ctx.close();
				return;
			}
			//网络连接中，处理Idle事件是很常见的，一般情况下，客户端与服务端在指定时间内没有任何读写请求，就会认为连接是idle(空闲)的
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
			case WRITER_IDLE:
				logger.error("写超时");
				UNCONNECT_NUM ++;
				break;
			case READER_IDLE:
				logger.error("读超时");
				UNCONNECT_NUM ++;
				break;
			default:
				break;
			}
        }
    }
}
