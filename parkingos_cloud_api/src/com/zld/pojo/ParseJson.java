package com.zld.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class ParseJson {

	
	public static List<Map<String, Object>> jsonToList(String json){
		JSONArray array=null;
		List<Map<String, Object>> busList = new ArrayList<Map<String,Object>>();
		try {
			array = new JSONArray(json);
			for(int i=0;i<array.length();i++){
				Map<String, Object>	 retMap = jsonObj2Map(array.getJSONObject(i));
				busList.add(retMap);
			}
			return busList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Map<String,Object> jsonToMap(String json){
		try {
			JSONObject jobj = new JSONObject(json);
			Map<String, Object>	 retMap = jsonObj2Map(jobj);
			return retMap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Map<String,Object> jsonObj2Map(JSONObject jobj){
		Map<String,Object> map = new HashMap<String, Object>();
		for (Iterator<String> iter = jobj.keys(); iter.hasNext();) { 
		       String key = (String)iter.next();
		        try {
		        	Object value = jobj.get(key);
		        	key = key.substring(0,1).toLowerCase()+key.substring(1);
					if (value instanceof JSONObject) {
						Map<String,Object> map2 = jsonObj2Map((JSONObject)value);
						map.put(key, map2);
					}else if(value instanceof JSONArray){
						JSONArray value2 = (JSONArray)value;
						List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
						for(int i=0;i<value2.length();i++){
							Map<String,Object> map3 = jsonObj2Map(value2.getJSONObject(i));
							list.add(map3);
						}
						map.put(key, list);
					}else {
						map.put(key, value);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			 }
		return map;
	}
	
	public static List<Map<String, Object>> jsonToList1(String json){
		JSONArray array=null;
		List<Map<String, Object>> busList = new ArrayList<Map<String,Object>>();
		try {
			array = new JSONArray(json);
			for(int i=0;i<array.length();i++){
				Map<String, Object>	 retMap = jsonObj2Map1(array.getJSONObject(i));
				busList.add(retMap);
			}
			return busList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Map<String,Object> jsonToMap1(String json){
		try {
			JSONObject jobj = new JSONObject(json);
			Map<String, Object>	 retMap = jsonObj2Map1(jobj);
			return retMap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Map<String,Object> jsonObj2Map1(JSONObject jobj){
		Map<String,Object> map = new HashMap<String, Object>();
		for (Iterator<String> iter = jobj.keys(); iter.hasNext();) { 
		       String key = (String)iter.next();
		        try {
		        	Object value = jobj.get(key);
		        	//key = key.substring(0,1).toLowerCase()+key.substring(1);
					if (value instanceof JSONObject) {
						Map<String,Object> map2 = jsonObj2Map1((JSONObject)value);
						map.put(key, map2);
					}else if(value instanceof JSONArray){
						JSONArray value2 = (JSONArray)value;
						List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
						for(int i=0;i<value2.length();i++){
							Map<String,Object> map3 = jsonObj2Map1(value2.getJSONObject(i));
							list.add(map3);
						}
						map.put(key, list);
					}else {
						map.put(key, value);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			 }
		return map;
	}
	
	public static void main(String[] args) {
		String listStr = "[{\"IsNewData\":\"0\",\"RouteID\":110022,\"RouteName\":\"1002路内环\",\"RouteType\":\"2\",\"SegmentList\":[{\"SegmentID\":100210,\"SegmentName\":\"西湖西苑\",\"FirstTime\":\"2016-01-16 06:30:00\",\"LastTime\":\"2016-01-16 18:30:00\",\"RoutePrice\":\"0\",\"NormalTimeSpan\":0,\"PeakTimeSpan\":0,\"StationList\":[{\"StationID\":\"107552\",\"StationName\":\"西湖西苑\",\"StationNO\":\"107552\",\"StationPostion\":{\"Longitude\":119.38729,\"Latitude\":32.40852},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"111101\",\"StationName\":\"梅苑双语学校\",\"StationNO\":\"111101\",\"StationPostion\":{\"Longitude\":119.3854,\"Latitude\":32.41248},\"Stationmemo\":\"东边站台\"},{\"StationID\":\"116592\",\"StationName\":\"唐悦国际社区（蜀秀花园西）\",\"StationNO\":\"116592\",\"StationPostion\":{\"Longitude\":119.37957,\"Latitude\":32.41899},\"Stationmemo\":\"\"},{\"StationID\":\"116582\",\"StationName\":\"汇锦花苑西\",\"StationNO\":\"116582\",\"StationPostion\":{\"Longitude\":119.38044,\"Latitude\":32.4225},\"Stationmemo\":\"\"},{\"StationID\":\"116572\",\"StationName\":\"维扬中学\",\"StationNO\":\"116572\",\"StationPostion\":{\"Longitude\":119.38095,\"Latitude\":32.42489},\"Stationmemo\":\"\"},{\"StationID\":\"104292\",\"StationName\":\"景文印刷厂\",\"StationNO\":\"104292\",\"StationPostion\":{\"Longitude\":119.38587,\"Latitude\":32.42519},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"102142\",\"StationName\":\"龚庄（扬州棋院）\",\"StationNO\":\"102142\",\"StationPostion\":{\"Longitude\":119.39063,\"Latitude\":32.42449},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"116563\",\"StationName\":\"山水锦城东门\",\"StationNO\":\"116563\",\"StationPostion\":{\"Longitude\":119.39228,\"Latitude\":32.4235},\"Stationmemo\":\"\"},{\"StationID\":\"116552\",\"StationName\":\"西湖印象华府\",\"StationNO\":\"116552\",\"StationPostion\":{\"Longitude\":119.39298,\"Latitude\":32.42217},\"Stationmemo\":\"\"},{\"StationID\":\"107572\",\"StationName\":\"西湖镇政府\",\"StationNO\":\"107572\",\"StationPostion\":{\"Longitude\":119.39717,\"Latitude\":32.42488},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"107862\",\"StationName\":\"新东方中学\",\"StationNO\":\"107862\",\"StationPostion\":{\"Longitude\":119.40234,\"Latitude\":32.42593},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"107563\",\"StationName\":\"西湖镇\",\"StationNO\":\"107563\",\"StationPostion\":{\"Longitude\":119.4051,\"Latitude\":32.42496},\"Stationmemo\":\"西边站台\"},{\"StationID\":\"103593\",\"StationName\":\"鉴真路\",\"StationNO\":\"103593\",\"StationPostion\":{\"Longitude\":119.40678,\"Latitude\":32.42186},\"Stationmemo\":\"西边站台\"},{\"StationID\":\"106333\",\"StationName\":\"蜀冈西峰\",\"StationNO\":\"106333\",\"StationPostion\":{\"Longitude\":119.41055,\"Latitude\":32.41498},\"Stationmemo\":\"西边站台\"},{\"StationID\":\"115284\",\"StationName\":\"扬州天下南\",\"StationNO\":\"115284\",\"StationPostion\":{\"Longitude\":119.41067,\"Latitude\":32.41152},\"Stationmemo\":\"\"},{\"StationID\":\"115274\",\"StationName\":\"念香苑北门\",\"StationNO\":\"115274\",\"StationPostion\":{\"Longitude\":119.40255,\"Latitude\":32.40852},\"Stationmemo\":\"\"},{\"StationID\":\"116524\",\"StationName\":\"念香西苑\",\"StationNO\":\"116524\",\"StationPostion\":{\"Longitude\":119.39905,\"Latitude\":32.40699},\"Stationmemo\":\"\"},{\"StationID\":\"115264\",\"StationName\":\"柳馨花园南门\",\"StationNO\":\"115264\",\"StationPostion\":{\"Longitude\":119.39688,\"Latitude\":32.40609},\"Stationmemo\":\"\"},{\"StationID\":\"104891\",\"StationName\":\"山河园（柳馨花园）\",\"StationNO\":\"104891\",\"StationPostion\":{\"Longitude\":119.39473,\"Latitude\":32.40603},\"Stationmemo\":\"东边站台\"},{\"StationID\":\"107544\",\"StationName\":\"西湖东苑\",\"StationNO\":\"107544\",\"StationPostion\":{\"Longitude\":119.39167,\"Latitude\":32.40926},\"Stationmemo\":\"北边站台\"},{\"StationID\":\"107554\",\"StationName\":\"西湖西苑\",\"StationNO\":\"107554\",\"StationPostion\":{\"Longitude\":119.38687,\"Latitude\":32.40858},\"Stationmemo\":\"北边站台\"}],\"FirtLastShiftInfo\":\"首末班：06:30--18:30\",\"FirtLastShiftInfo2\":null,\"Memos\":null}],\"TimeStamp\":\"2016-03-12 00:12:39\",\"RouteMemo\":null}]";
		String objStr = "{\"IsNewData\":\"0\",\"RouteID\":110022,\"RouteName\":\"1002路内环\",\"RouteType\":\"2\",\"SegmentList\":[{\"SegmentID\":100210,\"SegmentName\":\"西湖西苑\",\"FirstTime\":\"2016-01-16 06:30:00\",\"LastTime\":\"2016-01-16 18:30:00\",\"RoutePrice\":\"0\",\"NormalTimeSpan\":0,\"PeakTimeSpan\":0,\"StationList\":[{\"StationID\":\"107552\",\"StationName\":\"西湖西苑\",\"StationNO\":\"107552\",\"StationPostion\":{\"Longitude\":119.38729,\"Latitude\":32.40852},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"111101\",\"StationName\":\"梅苑双语学校\",\"StationNO\":\"111101\",\"StationPostion\":{\"Longitude\":119.3854,\"Latitude\":32.41248},\"Stationmemo\":\"东边站台\"},{\"StationID\":\"116592\",\"StationName\":\"唐悦国际社区（蜀秀花园西）\",\"StationNO\":\"116592\",\"StationPostion\":{\"Longitude\":119.37957,\"Latitude\":32.41899},\"Stationmemo\":\"\"},{\"StationID\":\"116582\",\"StationName\":\"汇锦花苑西\",\"StationNO\":\"116582\",\"StationPostion\":{\"Longitude\":119.38044,\"Latitude\":32.4225},\"Stationmemo\":\"\"},{\"StationID\":\"116572\",\"StationName\":\"维扬中学\",\"StationNO\":\"116572\",\"StationPostion\":{\"Longitude\":119.38095,\"Latitude\":32.42489},\"Stationmemo\":\"\"},{\"StationID\":\"104292\",\"StationName\":\"景文印刷厂\",\"StationNO\":\"104292\",\"StationPostion\":{\"Longitude\":119.38587,\"Latitude\":32.42519},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"102142\",\"StationName\":\"龚庄（扬州棋院）\",\"StationNO\":\"102142\",\"StationPostion\":{\"Longitude\":119.39063,\"Latitude\":32.42449},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"116563\",\"StationName\":\"山水锦城东门\",\"StationNO\":\"116563\",\"StationPostion\":{\"Longitude\":119.39228,\"Latitude\":32.4235},\"Stationmemo\":\"\"},{\"StationID\":\"116552\",\"StationName\":\"西湖印象华府\",\"StationNO\":\"116552\",\"StationPostion\":{\"Longitude\":119.39298,\"Latitude\":32.42217},\"Stationmemo\":\"\"},{\"StationID\":\"107572\",\"StationName\":\"西湖镇政府\",\"StationNO\":\"107572\",\"StationPostion\":{\"Longitude\":119.39717,\"Latitude\":32.42488},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"107862\",\"StationName\":\"新东方中学\",\"StationNO\":\"107862\",\"StationPostion\":{\"Longitude\":119.40234,\"Latitude\":32.42593},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"107563\",\"StationName\":\"西湖镇\",\"StationNO\":\"107563\",\"StationPostion\":{\"Longitude\":119.4051,\"Latitude\":32.42496},\"Stationmemo\":\"西边站台\"},{\"StationID\":\"103593\",\"StationName\":\"鉴真路\",\"StationNO\":\"103593\",\"StationPostion\":{\"Longitude\":119.40678,\"Latitude\":32.42186},\"Stationmemo\":\"西边站台\"},{\"StationID\":\"106333\",\"StationName\":\"蜀冈西峰\",\"StationNO\":\"106333\",\"StationPostion\":{\"Longitude\":119.41055,\"Latitude\":32.41498},\"Stationmemo\":\"西边站台\"},{\"StationID\":\"115284\",\"StationName\":\"扬州天下南\",\"StationNO\":\"115284\",\"StationPostion\":{\"Longitude\":119.41067,\"Latitude\":32.41152},\"Stationmemo\":\"\"},{\"StationID\":\"115274\",\"StationName\":\"念香苑北门\",\"StationNO\":\"115274\",\"StationPostion\":{\"Longitude\":119.40255,\"Latitude\":32.40852},\"Stationmemo\":\"\"},{\"StationID\":\"116524\",\"StationName\":\"念香西苑\",\"StationNO\":\"116524\",\"StationPostion\":{\"Longitude\":119.39905,\"Latitude\":32.40699},\"Stationmemo\":\"\"},{\"StationID\":\"115264\",\"StationName\":\"柳馨花园南门\",\"StationNO\":\"115264\",\"StationPostion\":{\"Longitude\":119.39688,\"Latitude\":32.40609},\"Stationmemo\":\"\"},{\"StationID\":\"104891\",\"StationName\":\"山河园（柳馨花园）\",\"StationNO\":\"104891\",\"StationPostion\":{\"Longitude\":119.39473,\"Latitude\":32.40603},\"Stationmemo\":\"东边站台\"},{\"StationID\":\"107544\",\"StationName\":\"西湖东苑\",\"StationNO\":\"107544\",\"StationPostion\":{\"Longitude\":119.39167,\"Latitude\":32.40926},\"Stationmemo\":\"北边站台\"},{\"StationID\":\"107554\",\"StationName\":\"西湖西苑\",\"StationNO\":\"107554\",\"StationPostion\":{\"Longitude\":119.38687,\"Latitude\":32.40858},\"Stationmemo\":\"北边站台\"}],\"FirtLastShiftInfo\":\"首末班：06:30--18:30\",\"FirtLastShiftInfo2\":null,\"Memos\":null}],\"TimeStamp\":\"2016-03-12 00:12:39\",\"RouteMemo\":null}";
		List<Map<String, Object>> object3 =jsonToList(listStr);
		Map<String, Object> obMap = jsonToMap(objStr);
		System.err.println(obMap);
	}
	
	public  static String createJson( List<Map<String, Object>> data) {
		String json = "[";
		int i=0;
		int j=0;
		if(data!=null&&data.size()>0){
			for(Map<String, Object > map : data){
				if(i!=0)
					json +=",";
				json+="{";
				for(String key : map.keySet()){
					if(j!=0)
						json +=",";
					Object v = map.get(key);
					if(v!=null){
						if(v instanceof List){
							v=createJson((List<Map<String, Object>>)v);
							json +="\""+key+"\":"+v+"";
						}else if(v instanceof Map){
							v = createJson((Map<String, Object>)v);
							json +="\""+key+"\":"+v+"";
						}else {
							v = v.toString().trim();
							json +="\""+key+"\":\""+v+"\"";
						}
					}
					j++;
				}
				json+="}";
				i++;
				j=0;
			}
			
		}
		json +="]";
		return json;
	}
	public  static String createJson( Map<String, Object> data) {
		String json = "{";
		int j=0;
		for(String key : data.keySet()){
			if(j!=0)
				json +=",";
			Object v = data.get(key);
			if(v!=null){
				if(v instanceof List){
					v=createJson((List<Map<String, Object>>)v);
					json +="\""+key+"\":"+v+"";
				}else if(v instanceof Map){
					v = createJson((Map<String, Object>)v);
					json +="\""+key+"\":"+v+"";
				}else {
					v = v.toString().trim();
					json +="\""+key+"\":\""+v+"\"";
				}
			}
			j++;
		}
		json +="}";
		return json;
	}
}
