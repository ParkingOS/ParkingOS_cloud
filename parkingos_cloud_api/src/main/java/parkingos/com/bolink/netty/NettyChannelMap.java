package parkingos.com.bolink.netty;

import io.netty.channel.Channel;
import parkingos.com.bolink.service.DoUpload;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;





public class NettyChannelMap {
	//存储和客户端的连接通道，ConcurrentHashMap线程安全
	private static Map<String, Channel> channelMap = new ConcurrentHashMap<String, Channel>();
	//存储客户端请求的时间，计算是否攻击，tokenAccessMap
	public static Map<String,List<Long>> tokenAccessMap = new ConcurrentHashMap<String, List<Long>>();

	public static TcpHandle tcpHandle;
	public static DoUpload doUpload;

	public static void add(String key, Channel socketChannel) {
		channelMap.put(key, socketChannel);
		//System.err.println(channelMap);
	}
	
	public static Channel get(String key) {
	    System.out.println(channelMap);
		return channelMap.get(key);
	}
	

	/*@SuppressWarnings("rawtypes")
	public static void remove(Channel socketChannel) {
		for (Map.Entry entry : channelMap.entrySet()) {
			if (entry.getValue() == socketChannel) {
				channelMap.remove(entry.getKey());
			}
		}
	}*/
	
	/*public static void remove(String key) {
		channelMap.remove(key);
	}*/
	
	
	public static String getParkByChannel(Channel channel){
		for(String key : channelMap.keySet()){
			if(channelMap.get(key).equals(channel))
				return key;
		}
		return null;
	}
	
	/*public static boolean isSaveChannel(String key){
		if(channelMap.containsKey(key))
			return true;
		return false;
	}*/
}
