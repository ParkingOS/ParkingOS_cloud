package com.zld.pcloud_sensor.util;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyChannelMap {
	//存储和客户端的连接通道，ConcurrentHashMap线程安全
	private static Map<String, Channel> map = new ConcurrentHashMap<String, Channel>();

	public static void add(String key, Channel socketChannel) {
		map.put(key, socketChannel);
	}

	public static Channel get(String key) {
		return map.get(key);
	}

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
}
