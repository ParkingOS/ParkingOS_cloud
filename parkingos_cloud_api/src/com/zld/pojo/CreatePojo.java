package com.zld.pojo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * JSON 转为POJO类对象或集合
 * @author Laoyao
 */
public class CreatePojo {
	//如果对象中有另一个对象集合，要把另一个对象的路径配置一下。这个集合的名称符合下面规范，
	//public List<Semgent> semgmentList,Semgent就是集合中的对象，加上path后能生成对象，字段要公有
	//path javaBean所在包名，结尾要加.,如"com.zld.pojo.";
	public static String POJOPATH="com.zld.pojo.";
	
	public static void main(String[] args) {
		String listStr = "[{\"IsNewData\":\"0\",\"RouteID\":110022,\"RouteName\":\"1002路内环\",\"RouteType\":\"2\",\"SegmentList\":[{\"SegmentID\":100210,\"SegmentName\":\"西湖西苑\",\"FirstTime\":\"2016-01-16 06:30:00\",\"LastTime\":\"2016-01-16 18:30:00\",\"RoutePrice\":\"0\",\"NormalTimeSpan\":0,\"PeakTimeSpan\":0,\"StationList\":[{\"StationID\":\"107552\",\"StationName\":\"西湖西苑\",\"StationNO\":\"107552\",\"StationPostion\":{\"Longitude\":119.38729,\"Latitude\":32.40852},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"111101\",\"StationName\":\"梅苑双语学校\",\"StationNO\":\"111101\",\"StationPostion\":{\"Longitude\":119.3854,\"Latitude\":32.41248},\"Stationmemo\":\"东边站台\"},{\"StationID\":\"116592\",\"StationName\":\"唐悦国际社区（蜀秀花园西）\",\"StationNO\":\"116592\",\"StationPostion\":{\"Longitude\":119.37957,\"Latitude\":32.41899},\"Stationmemo\":\"\"},{\"StationID\":\"116582\",\"StationName\":\"汇锦花苑西\",\"StationNO\":\"116582\",\"StationPostion\":{\"Longitude\":119.38044,\"Latitude\":32.4225},\"Stationmemo\":\"\"},{\"StationID\":\"116572\",\"StationName\":\"维扬中学\",\"StationNO\":\"116572\",\"StationPostion\":{\"Longitude\":119.38095,\"Latitude\":32.42489},\"Stationmemo\":\"\"},{\"StationID\":\"104292\",\"StationName\":\"景文印刷厂\",\"StationNO\":\"104292\",\"StationPostion\":{\"Longitude\":119.38587,\"Latitude\":32.42519},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"102142\",\"StationName\":\"龚庄（扬州棋院）\",\"StationNO\":\"102142\",\"StationPostion\":{\"Longitude\":119.39063,\"Latitude\":32.42449},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"116563\",\"StationName\":\"山水锦城东门\",\"StationNO\":\"116563\",\"StationPostion\":{\"Longitude\":119.39228,\"Latitude\":32.4235},\"Stationmemo\":\"\"},{\"StationID\":\"116552\",\"StationName\":\"西湖印象华府\",\"StationNO\":\"116552\",\"StationPostion\":{\"Longitude\":119.39298,\"Latitude\":32.42217},\"Stationmemo\":\"\"},{\"StationID\":\"107572\",\"StationName\":\"西湖镇政府\",\"StationNO\":\"107572\",\"StationPostion\":{\"Longitude\":119.39717,\"Latitude\":32.42488},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"107862\",\"StationName\":\"新东方中学\",\"StationNO\":\"107862\",\"StationPostion\":{\"Longitude\":119.40234,\"Latitude\":32.42593},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"107563\",\"StationName\":\"西湖镇\",\"StationNO\":\"107563\",\"StationPostion\":{\"Longitude\":119.4051,\"Latitude\":32.42496},\"Stationmemo\":\"西边站台\"},{\"StationID\":\"103593\",\"StationName\":\"鉴真路\",\"StationNO\":\"103593\",\"StationPostion\":{\"Longitude\":119.40678,\"Latitude\":32.42186},\"Stationmemo\":\"西边站台\"},{\"StationID\":\"106333\",\"StationName\":\"蜀冈西峰\",\"StationNO\":\"106333\",\"StationPostion\":{\"Longitude\":119.41055,\"Latitude\":32.41498},\"Stationmemo\":\"西边站台\"},{\"StationID\":\"115284\",\"StationName\":\"扬州天下南\",\"StationNO\":\"115284\",\"StationPostion\":{\"Longitude\":119.41067,\"Latitude\":32.41152},\"Stationmemo\":\"\"},{\"StationID\":\"115274\",\"StationName\":\"念香苑北门\",\"StationNO\":\"115274\",\"StationPostion\":{\"Longitude\":119.40255,\"Latitude\":32.40852},\"Stationmemo\":\"\"},{\"StationID\":\"116524\",\"StationName\":\"念香西苑\",\"StationNO\":\"116524\",\"StationPostion\":{\"Longitude\":119.39905,\"Latitude\":32.40699},\"Stationmemo\":\"\"},{\"StationID\":\"115264\",\"StationName\":\"柳馨花园南门\",\"StationNO\":\"115264\",\"StationPostion\":{\"Longitude\":119.39688,\"Latitude\":32.40609},\"Stationmemo\":\"\"},{\"StationID\":\"104891\",\"StationName\":\"山河园（柳馨花园）\",\"StationNO\":\"104891\",\"StationPostion\":{\"Longitude\":119.39473,\"Latitude\":32.40603},\"Stationmemo\":\"东边站台\"},{\"StationID\":\"107544\",\"StationName\":\"西湖东苑\",\"StationNO\":\"107544\",\"StationPostion\":{\"Longitude\":119.39167,\"Latitude\":32.40926},\"Stationmemo\":\"北边站台\"},{\"StationID\":\"107554\",\"StationName\":\"西湖西苑\",\"StationNO\":\"107554\",\"StationPostion\":{\"Longitude\":119.38687,\"Latitude\":32.40858},\"Stationmemo\":\"北边站台\"}],\"FirtLastShiftInfo\":\"首末班：06:30--18:30\",\"FirtLastShiftInfo2\":null,\"Memos\":null}],\"TimeStamp\":\"2016-03-12 00:12:39\",\"RouteMemo\":null}]";
		String objStr = "{\"IsNewData\":\"0\",\"RouteID\":110022,\"RouteName\":\"1002路内环\",\"RouteType\":\"2\",\"SegmentList\":[{\"SegmentID\":100210,\"SegmentName\":\"西湖西苑\",\"FirstTime\":\"2016-01-16 06:30:00\",\"LastTime\":\"2016-01-16 18:30:00\",\"RoutePrice\":\"0\",\"NormalTimeSpan\":0,\"PeakTimeSpan\":0,\"StationList\":[{\"StationID\":\"107552\",\"StationName\":\"西湖西苑\",\"StationNO\":\"107552\",\"StationPostion\":{\"Longitude\":119.38729,\"Latitude\":32.40852},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"111101\",\"StationName\":\"梅苑双语学校\",\"StationNO\":\"111101\",\"StationPostion\":{\"Longitude\":119.3854,\"Latitude\":32.41248},\"Stationmemo\":\"东边站台\"},{\"StationID\":\"116592\",\"StationName\":\"唐悦国际社区（蜀秀花园西）\",\"StationNO\":\"116592\",\"StationPostion\":{\"Longitude\":119.37957,\"Latitude\":32.41899},\"Stationmemo\":\"\"},{\"StationID\":\"116582\",\"StationName\":\"汇锦花苑西\",\"StationNO\":\"116582\",\"StationPostion\":{\"Longitude\":119.38044,\"Latitude\":32.4225},\"Stationmemo\":\"\"},{\"StationID\":\"116572\",\"StationName\":\"维扬中学\",\"StationNO\":\"116572\",\"StationPostion\":{\"Longitude\":119.38095,\"Latitude\":32.42489},\"Stationmemo\":\"\"},{\"StationID\":\"104292\",\"StationName\":\"景文印刷厂\",\"StationNO\":\"104292\",\"StationPostion\":{\"Longitude\":119.38587,\"Latitude\":32.42519},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"102142\",\"StationName\":\"龚庄（扬州棋院）\",\"StationNO\":\"102142\",\"StationPostion\":{\"Longitude\":119.39063,\"Latitude\":32.42449},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"116563\",\"StationName\":\"山水锦城东门\",\"StationNO\":\"116563\",\"StationPostion\":{\"Longitude\":119.39228,\"Latitude\":32.4235},\"Stationmemo\":\"\"},{\"StationID\":\"116552\",\"StationName\":\"西湖印象华府\",\"StationNO\":\"116552\",\"StationPostion\":{\"Longitude\":119.39298,\"Latitude\":32.42217},\"Stationmemo\":\"\"},{\"StationID\":\"107572\",\"StationName\":\"西湖镇政府\",\"StationNO\":\"107572\",\"StationPostion\":{\"Longitude\":119.39717,\"Latitude\":32.42488},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"107862\",\"StationName\":\"新东方中学\",\"StationNO\":\"107862\",\"StationPostion\":{\"Longitude\":119.40234,\"Latitude\":32.42593},\"Stationmemo\":\"南边站台\"},{\"StationID\":\"107563\",\"StationName\":\"西湖镇\",\"StationNO\":\"107563\",\"StationPostion\":{\"Longitude\":119.4051,\"Latitude\":32.42496},\"Stationmemo\":\"西边站台\"},{\"StationID\":\"103593\",\"StationName\":\"鉴真路\",\"StationNO\":\"103593\",\"StationPostion\":{\"Longitude\":119.40678,\"Latitude\":32.42186},\"Stationmemo\":\"西边站台\"},{\"StationID\":\"106333\",\"StationName\":\"蜀冈西峰\",\"StationNO\":\"106333\",\"StationPostion\":{\"Longitude\":119.41055,\"Latitude\":32.41498},\"Stationmemo\":\"西边站台\"},{\"StationID\":\"115284\",\"StationName\":\"扬州天下南\",\"StationNO\":\"115284\",\"StationPostion\":{\"Longitude\":119.41067,\"Latitude\":32.41152},\"Stationmemo\":\"\"},{\"StationID\":\"115274\",\"StationName\":\"念香苑北门\",\"StationNO\":\"115274\",\"StationPostion\":{\"Longitude\":119.40255,\"Latitude\":32.40852},\"Stationmemo\":\"\"},{\"StationID\":\"116524\",\"StationName\":\"念香西苑\",\"StationNO\":\"116524\",\"StationPostion\":{\"Longitude\":119.39905,\"Latitude\":32.40699},\"Stationmemo\":\"\"},{\"StationID\":\"115264\",\"StationName\":\"柳馨花园南门\",\"StationNO\":\"115264\",\"StationPostion\":{\"Longitude\":119.39688,\"Latitude\":32.40609},\"Stationmemo\":\"\"},{\"StationID\":\"104891\",\"StationName\":\"山河园（柳馨花园）\",\"StationNO\":\"104891\",\"StationPostion\":{\"Longitude\":119.39473,\"Latitude\":32.40603},\"Stationmemo\":\"东边站台\"},{\"StationID\":\"107544\",\"StationName\":\"西湖东苑\",\"StationNO\":\"107544\",\"StationPostion\":{\"Longitude\":119.39167,\"Latitude\":32.40926},\"Stationmemo\":\"北边站台\"},{\"StationID\":\"107554\",\"StationName\":\"西湖西苑\",\"StationNO\":\"107554\",\"StationPostion\":{\"Longitude\":119.38687,\"Latitude\":32.40858},\"Stationmemo\":\"北边站台\"}],\"FirtLastShiftInfo\":\"首末班：06:30--18:30\",\"FirtLastShiftInfo2\":null,\"Memos\":null}],\"TimeStamp\":\"2016-03-12 00:12:39\",\"RouteMemo\":null}";
		Object object1 = getObjectFromJson(RouteStation.class, objStr);
		List<Object> list = getListObjFromJson(RouteStation.class, listStr);
		Object object2 = CreatePojo.getObjFromJson(RouteStation.class, objStr);
		Object object3 = CreatePojo.getObjFromJson(RouteStation.class, listStr);
	}
	
	/**
	 * @param c 对象类型
	 * @param json 
	 * @return 明确返回是单个对象 c
	 */
	public static Object getObjectFromJson(Class<?> c,String json){
		try {
			JSONObject jobj = new JSONObject(json);
			Map<String, Object>	 retMap = jsonObj2Map(jobj);
			Object object = createObject(c, retMap);
			return object;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * @param c 对象类型
	 * @param json 
	 * @return 明确返回对象集合 List<c>
	 */
	public static List<Object> getListObjFromJson(Class<?> c,String json){
		JSONArray array=null;
		List<Map<String, Object>> busList = new ArrayList<Map<String,Object>>();
		try {
			array = new JSONArray(json);
			for(int i=0;i<array.length();i++){
				Map<String, Object>	 retMap = jsonObj2Map(array.getJSONObject(i));
				busList.add(retMap);
			}
			List<Object> list = createObjectList(c, busList);
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param c 对象类型
	 * @param json 
	 * @return 不明确返回集合还是对象，c 或 List<c> ,要对返回的对象进行判断
	 */
	public static Object getObjFromJson(Class<?> c,String json){
		if(json.startsWith("[")){
			JSONArray array=null;
			List<Map<String, Object>> busList = new ArrayList<Map<String,Object>>();
			try {
				array = new JSONArray(json);
				for(int i=0;i<array.length();i++){
					Map<String, Object>	 retMap = jsonObj2Map(array.getJSONObject(i));
					busList.add(retMap);
				}
				List<Object> list =createObjectList(c, busList);
				return list;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}else if(json.startsWith("{")){
			try {
				JSONObject jobj = new JSONObject(json);
				Map<String, Object>	 retMap = jsonObj2Map(jobj);
				return createObject(c, retMap);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	private static Map<String,Object> jsonObj2Map(JSONObject jobj){
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
	
	/**
	 * 返回单个对象
	 * @param c
	 * @param objMap
	 * @return
	 * @throws Exception
	 */
	private static Object createObject(Class<?> c,Map<String, Object> objMap)
			throws Exception {
		Iterator<String> keys = objMap.keySet().iterator();
		Object t = c.newInstance();
		JXPathContext jxpcontext = JXPathContext.newContext(t);
		while (keys.hasNext()) {
			String key = keys.next();
			try {
				Object value = objMap.get(key);
				if(value==null||value.toString().trim().equals("null"))
					continue;
				if(value instanceof Map ){
					Field field = c.getField(key);
					Class<?> class1 = field.getType();
					Object o = createObject(class1, (Map<String, Object>)value);
					jxpcontext.setValue(key,o);
				}else if(value instanceof List){
					Field field = c.getField(key);
					String name = field.getName();
					name = name.substring(0,1).toUpperCase()+name.substring(1,name.length()-4);
					Object o = createObjectList(Class.forName(POJOPATH+name), (List<Map<String, Object>>)value);
					jxpcontext.setValue(key,o);
				}else {
					jxpcontext.setValue(key, objMap.get(key));
				}
			} catch (Exception e) {
				System.err.println(c.getName()+",error:method=" + key);
			}
		}
		return t;
	}
	
	/**
	 * 返回对象集合
	 * @param c
	 * @param objMapList
	 * @return
	 * @throws Exception
	 */
	private static List<Object> createObjectList(Class<?> c,List<Map<String, Object>> objMapList)
			throws Exception {
		List<Object> list = new ArrayList<Object>();
		for(Map<String, Object> objMap:objMapList){
			list.add(createObject(c, objMap));
		}
		return list;
	}
}
