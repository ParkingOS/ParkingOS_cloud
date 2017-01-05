package com.zld.utils;

import java.util.HashMap;
import java.util.Map;

public class ResultMap {
	
	Map<String, Object> map= new HashMap<String, Object>();
	
	public ResultMap(Map map){
		if(map!=null)
			this.map=map;	
	}

	public String getString(String key){
		String result = (String)map.get(key);
		if(result!=null&&!"null".equals(result)){
			result = result.replace("'", "").replace("\"", "");
			return result;
		}
		return "";
	}
	
	public Double getDouble(String key){
		return StringUtils.formatDouble(map.get(key));
	}
	
	public Long getLong(String key){
		Long result = (Long)map.get(key);
		if(result!=null)
			return result;
		return 0L;
	}
	
	public Integer getInteger(String key){
		Integer result = (Integer)map.get(key);
		if(result!=null)
			return result;
		return 0;
	}

}
