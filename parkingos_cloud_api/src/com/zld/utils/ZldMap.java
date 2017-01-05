package com.zld.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Map工具类
 * @author Administrator
 *
 */
public class ZldMap {
	
	//车主缓存
	public static Map<Long, Map<String, Object>> userMaps = null;
	//时间，每天更新一次
	public static Long time = TimeTools.getToDayBeginTime();
	
	public static Map<String, Object> getMap(String[] names,Object[] values){
		Map<String, Object> map =new HashMap<String, Object>();
		for(int i=0;i<names.length;i++){
			map.put(names[i], values[i]);
		}
		return map;
	}
	
	public static Map<String, Object> getAppendMap(Map<String,Object> map,String[] names,Object[] values){
		if(map==null)
			map = new HashMap<String, Object>();
		for(int i=0;i<names.length;i++){
			map.put(names[i], values[i]);
		}
		return map;
	}
	
	public static  Map<String, Object>  getUser(Long uin){
		Long ntime = TimeTools.getToDayBeginTime();
		if(ntime>time){
			userMaps.clear();
			time=ntime;
		}
		
		if(userMaps!=null&&!userMaps.isEmpty())
			return userMaps.get(uin);
		return null;
	}
	
	public  static void putUser(Long uin, Map<String, Object>  user) {
		if(userMaps==null){
			userMaps = new HashMap<Long, Map<String,Object>>();
		}
		if(user!=null)
			userMaps.put(uin, user);
	}
	
	public static void removeUser(Long uin){
		if(userMaps!=null&&userMaps.containsKey(uin))
			userMaps.remove(uin);
	}
}
