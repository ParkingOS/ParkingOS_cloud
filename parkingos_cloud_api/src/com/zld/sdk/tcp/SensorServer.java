package com.zld.sdk.tcp;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public class SensorServer {
	Logger logger = Logger.getLogger(SensorServer.class);
	
	public SensorServer(){
	}
	
	public void bind(int port) throws Exception{
		logger.error("listening port:" + port);
		EventLoopGroup parentGroup = new NioEventLoopGroup();
		EventLoopGroup childGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(parentGroup, childGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 1024)
				.childHandler(new ChildChannelHandler());
			ChannelFuture f = bootstrap.bind(port).sync();
			f.channel().closeFuture().sync();
			logger.info(">>>>>>>>>>>TCP SERVER STARTED...port:" + port);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
		}
	}
	
	public class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
		
		@Override
		protected void initChannel(SocketChannel channel) throws Exception {
			SensorServerHandler sensorHandler = new SensorServerHandler();
			channel.pipeline()
				.addLast(new LineBasedFrameDecoder(1024))
//				.addLast(new StringDecoder())
//				.addLast(new StringEncoder())
				.addLast(sensorHandler);
		}

	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ExecutorService pool = Executors.newFixedThreadPool(2);
			pool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						new SensorServer().bind(Constants.SERVERPORT);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
