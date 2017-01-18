package com.zld.pcloud_sensor;

import org.apache.log4j.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import com.zld.pcloud_sensor.handler.impl.SensorClientHandler;
import com.zld.pcloud_sensor.listener.ConnectionListener;
import com.zld.pcloud_sensor.util.SpringContextUtil;

public class SensorClient {
	
	Logger logger = Logger.getLogger(SensorClient.class);
	
	public SensorClient(){
		/**
		 * 加载application-context.xml，之后方可使用SpringContextUtil获取实例
		 */
		SpringContextUtil.initContext();
	}
	
	EventLoopGroup group = new NioEventLoopGroup();
	
	public void connect(String addr, int port) throws Exception{
		try {
			Bootstrap bootstrap = new Bootstrap();
			final SensorClientHandler clientHandler = SpringContextUtil.getBean("sensorClientHandler",
					SensorClientHandler.class);
			clientHandler.setClient(this);
			bootstrap.group(group)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline()
						//10分钟超时
						.addLast(new IdleStateHandler(600, 600, 0))
						//天泊的返回消息没有加分隔符，所以在此去掉解码器，否则收不到消息
						//.addLast(new LineBasedFrameDecoder(1024))
						.addLast(new StringDecoder())
						.addLast(clientHandler);
					}
				});
			ChannelFuture future = bootstrap
					.connect(addr, port)
					.addListener(new ConnectionListener(this))
					.sync();
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			logger.error("客户端异常", e);
		}
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		new SensorClient().connect(Constants.TB_ADDR, Constants.TB_PORT);
	}
}
