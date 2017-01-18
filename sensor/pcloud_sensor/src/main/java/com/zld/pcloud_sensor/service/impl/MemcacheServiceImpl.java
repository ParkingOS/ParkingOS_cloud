package com.zld.pcloud_sensor.service.impl;

import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zld.pcloud_sensor.Constants;
import com.zld.pcloud_sensor.service.MemcacheService;

@Service
public class MemcacheServiceImpl implements MemcacheService {
	@Autowired
	private MemcachedClient memcachedClient;
	Logger logger = Logger.getLogger(MemcacheServiceImpl.class);
	//非spring配置
	/*public MemcacheServiceImpl(){
		try {
			MemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses("127.0.0.1:11211"));
			//使用二进制文件  
		    builder.setCommandFactory(new BinaryCommandFactory());  
		    //使用一致性哈希算法（Consistent Hash Strategy）  
		    builder.setSessionLocator(new KetamaMemcachedSessionLocator());  
		    //使用序列化传输编码  
		    builder.setTranscoder(new SerializingTranscoder());  
		    //进行数据压缩，大于1KB时进行压缩  
		    builder.getTranscoder().setCompressionThreshold(1024); 
			在高负载环境下，nio的单连接也会遇到瓶颈，此时你可以通过设置连接池来让更多的连接分担memcached的请求负载，从而提高系统的吞吐量。
			连接池通常不建议设置太大，我推荐在0-30之间为好，太大则浪费系统资源，太小无法达到分担负载的目的。
			builder.setConnectionPoolSize(30);
			五、为什么会抛出java.util.TimeoutException?
			这是由于xmemcached的通讯层是基于非阻塞IO的，那么在请求发送给memcached之后，需要等待应答的到来，这个等待时间默认是1秒，如果 超过1秒就抛出java.util.TimeoutExpcetion给用户。如果你频繁抛出此异常，可以尝试将全局的等待时间设置长一些，如我在压测中设置为5秒：
			MemcachedClient  memcachedClient=……
			memcachedClient.setOpTimeout(5000L);
			请注意，setOpTimeout设置的是全局的等待时间，如果你仅仅是希望将get或者set等操作的超时延长一点，那么可以通过这些方法的重载方法来使用：
			<T> T get(java.lang.String key, long timeout)
			boolean set(java.lang.String key, int exp, java.lang.Object value, long timeout)
			builder.setOpTimeout(5000);
			builder.setConnectTimeout(2000);
			this.memcachedClient = builder.build();
			//如果你对心跳检测不在意，也可以关闭心跳检测，减小系统开销
			memcachedClient.setEnableHeartBeat(false);
			//这个关闭，仅仅是关闭了心跳的功能，客户端仍然会去统计连接是否空闲，禁止统计可以通过：
			builder.getConfiguration().setStatisticsServer(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	@Override
	public boolean lock(String key) {
		try {
			key = Constants.SNESOR_SIGN + key;
			byte[] b = new byte[0];
			return memcachedClient.add(key, 0, b);
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
		return true;//当缓存抛出异常时，返回true，不使用分布式锁
	}

	@Override
	public boolean lock(String key, int exp) {
		try {
			key = Constants.SNESOR_SIGN + key;
			byte[] b = new byte[0];
			return memcachedClient.add(key, exp, b);
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
		return true;//当缓存抛出异常时，返回true，不使用分布式锁
	}

	@Override
	public boolean delete(String key) {
		try {
			if(key == null || "".equals(key)){
				return true;
			}
			key = Constants.SNESOR_SIGN + key;
			return memcachedClient.delete(key);
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean set(String key, Object value, int exp) {
		try {
			key = Constants.SNESOR_SIGN + key;
			return memcachedClient.set(key, exp, value);
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public <T> T get(String key, Class<T> type) {
		try {
			key = Constants.SNESOR_SIGN + key;
			if(memcachedClient.get(key) != null){
				return (T)memcachedClient.get(key);
			}
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean set(String key, Object value) {
		try {
			key = Constants.SNESOR_SIGN + key;
			return memcachedClient.set(key, 0, value);
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 获取锁名称
	 * @param key
	 * @return
	 */
	@Override
	public String getLock(Object key){
		String lock = null;
		try {
			String className = Thread.currentThread().getStackTrace()[2].getClassName();//获取当前方法的上一级调用者的类名
			String methodName = Thread.currentThread() .getStackTrace()[2].getMethodName();//获取当前方法的上一级调用者的方法名
			lock = className + "-" + methodName + "-" + key;
			return lock;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lock;
	}

	@Override
	public String get(String key) {
		return get(key, String.class);
	}

}
