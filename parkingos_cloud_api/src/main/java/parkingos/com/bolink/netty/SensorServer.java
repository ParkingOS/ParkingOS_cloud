package parkingos.com.bolink.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;

public class SensorServer {
	Logger logger = Logger.getLogger(SensorServer.class);
	
	public SensorServer(){
		logger.info("begin init tcp.....");
		/*
		 * 加载application-context.xml，之后方可使用SpringContextUtil获取实例
		 */
		//SpringContextUtil.initContext();
	}
	
	public void bind(int port) throws Exception{
		logger.error("listening port:" + port);
		 /*
         * NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器，
         * Netty提供了许多不同的EventLoopGroup的实现用来处理不同传输协议。
         * 在这个例子中我们实现了一个服务端的应用，
         * 因此会有2个NioEventLoopGroup会被使用。
         * 第一个经常被叫做‘boss’，用来接收进来的连接。
         * 第二个经常被叫做‘worker’，用来处理已经被接收的连接，
         * 一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上。
         * 如何知道多少个线程已经被使用，如何映射到已经创建的Channels上都需要依赖于EventLoopGroup的实现，
         * 并且可以通过构造函数来配置他们的关系。
         */
		EventLoopGroup parentGroup = new NioEventLoopGroup();
		EventLoopGroup childGroup = new NioEventLoopGroup();
		try {
			/*
			 * ServerBootstrap 是一个启动NIO服务的辅助启动类
			 * 你可以在这个服务中直接使用Channel
			 */
			ServerBootstrap bootstrap = new ServerBootstrap();
			/*
			 * 这一步是必须的，如果没有设置group将会报java.lang.IllegalStateException: group not set异常
			 */
			bootstrap.group(parentGroup, childGroup)
			/*
			 * ServerSocketChannel以NIO的selector为基础进行实现的，用来接收新的连接
			 * 这里告诉Channel如何获取新的连接.
			 */
				.channel(NioServerSocketChannel.class)
			/*你可以设置这里指定的通道实现的配置参数。
			 * 我们正在写一个TCP/IP的服务端，
			 *  因此我们被允许设置socket的参数选项比如tcpNoDelay和keepAlive。
			 *  请参考ChannelOption和详细的ChannelConfig实现的接口文档以此可以对ChannelOptions的有一个大概的认识。
			 */
				.option(ChannelOption.SO_BACKLOG, 1024)
			/*
			 * 这里的事件处理类经常会被用来处理一个最近的已经接收的Channel。
			 * ChannelInitializer是一个特殊的处理类，
			 * 他的目的是帮助使用者配置一个新的Channel。
			 * 也许你想通过增加一些处理类比如NettyServerHandler来配置一个新的Channel
			 * 或者其对应的ChannelPipeline来实现你的网络程序。
			 * 当你的程序变的复杂时，可能你会增加更多的处理类到pipline上，
			 * 然后提取这些匿名类到最顶层的类上。
			 */
				.childHandler(new ChildChannelHandler());
			//绑定端口，同步等待成功
			ChannelFuture f = bootstrap.bind(port).sync();
			//等待服务端监听端口关闭
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
			//这里的handler是线程安全的，所以添加了Sharable注解，共用一个实例
			Charset charset = Charset.forName("UTF-8");
			SensorServerHandler sensorHandler = new SensorServerHandler();
			channel.pipeline()
				.addLast(new LineBasedFrameDecoder(1024))
				.addLast(new StringDecoder(charset))
				.addLast(sensorHandler);
		}
	}
}
