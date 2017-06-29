package com.zld.sdk.tcp;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.zld.sdk.doupload.DoUpload;
import com.zld.sdk.doupload.impl.DoUploadImpl;


public class NettyChannelMap {
	//存储和客户端的连接通道，ConcurrentHashMap线程安全
	private static Map<String, Channel> map = new ConcurrentHashMap<String, Channel>();
	private static Map<String, String> tokenMap = new HashMap<String, String>();
	//引入log日志
	private static Logger logger;
	public static DoUpload doUpload;
	
	public static void add(String key, Channel socketChannel) {
		map.put(key, socketChannel);
		System.err.println(map);
	}
	
	public static Channel get(String key) {
		return map.get(key);
	}
	
//	public static void addToken(String key,String token) {
//		tokenMap.put(key, token);
//		System.err.println(tokenMap);
//	}

//	public static String getToken(String key) {
//		return tokenMap.get(key);
//	}

	@SuppressWarnings("rawtypes")
	public static void remove(Channel socketChannel) {
		for (Map.Entry entry : map.entrySet()) {
			if (entry.getValue() == socketChannel) {
				map.remove(entry.getKey());
			}
		}
	}
	
	public static void remove(String key) {
		map.remove(key);
	}
	
	public static void removeToken(String key) {
		tokenMap.remove(key);
	}
	
	public static String getParkByChannel(Channel channel){
		for(String key : map.keySet()){
			if(map.get(key).equals(channel))
				return key;
		}
		return null;
	}
	
	public static boolean isSaveChannel(String key){
		if(map.containsKey(key))
			return true;
		return false;
	}
	
	/**
	 * 判断tcp通信是否联通
	 * @param comid
	 * @return
	 */
	public static boolean isLinked(String comid){
		boolean isLinked = false;
		Channel clientChannel = NettyChannelMap.get(comid);
		if(clientChannel != null && clientChannel.isActive() && clientChannel.isWritable()){
			isLinked = true;
		}else{
			isLinked = false;
			logger.error("tcp通道建立异常");
		}
		return isLinked;
	}
	
	public static void setUploadUtil(DoUpload uploadImpl){
		doUpload = uploadImpl;
	}
}
