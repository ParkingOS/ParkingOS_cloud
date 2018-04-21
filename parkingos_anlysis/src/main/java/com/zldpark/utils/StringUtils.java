package com.zldpark.utils;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class StringUtils {

	
	public static boolean isNotNull(String value){
		if(value==null||value.equals(""))
			return false;
		return true;
	}
	
	public static boolean isNumber(String value){
		if(value==null||value.equals(""))
			return false;
		try {
			Long a = Long.valueOf(value);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public static boolean isDouble(String value){
		if(value==null||value.equals(""))
			return false;
		try {
			Double a = new Double(value);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public static String _2null(String value){
		if("".equals(value))
			return null;
		return value;
	}
	
	public static Double getDoubleValue(String value){
		Double double1 = null;
		try {
			double1 = Double.valueOf(value);
		} catch (Exception e) {
			double1 =0.0d;
		}
		return double1;
	}
	
	public static double mul(double d1,double d2){ 
        BigDecimal bd1 = new BigDecimal(Double.toString(d1)); 
        BigDecimal bd2 = new BigDecimal(Double.toString(d2)); 
        return bd1.multiply(bd2).doubleValue(); 
	} 
	
	public static Double formatDouble(Object value){
		if(Check.isDouble(value+"")){
			DecimalFormat df=new DecimalFormat("#.00"); 
			String dv = df.format(Double.valueOf(value+""));
			if(Check.isDouble(dv))
				return Double.valueOf(dv);
		}
		return 0.0d;
	}
	
	public static Long getLongMilliSecondFromStrDate(String strDate, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);

		long millSeconds = new GregorianCalendar().getTimeInMillis();
		try {
			millSeconds = sdf.parse(strDate).getTime();
		} catch (Exception e) {
			// logger.error("---------get seconds error:"+e.getMessage());
		}
		return new Long(millSeconds);
	}
	
	public static String getPre (String value){
		for(int i= 0;i<value.length();i++){
			char a = value.charAt(i);
			if(!String.valueOf(a).equals("0"))
				return value.substring(0,i);
		}
		return "";
	}
	
	public static Long getHour(Long start,Long end){
		if(end!=null&&start!=null){
			Long hours = (end-start)/3600;
			if((end-start)%60!=0)
				hours+=1;
			return hours;
		}
		return 0L;
	}
	
/*	*//**
	 * 生成 xml文件流
	 *//*
	public static String createXML(Map<String, String > info) {// 获取最大访客数XML
		StringBuffer xml = new StringBuffer();
		xml.append("<?xml version=\"1.0\" encoding=\"gb2312\"?>");
		xml.append("<content>");
		for(String key : info.keySet()){
			xml.append("<"+key+">"+info.get(key)+"</"+key+">");
		}
		xml.append("</content>");
		return xml.toString();
	}
	*/
	/**
	 * 生成 xml文件流
	 */
	public static String createXML(Map<String, Object > info) {// 获取最大访客数XML
		StringBuffer xml = new StringBuffer();
		xml.append("<?xml version=\"1.0\" encoding=\"gb2312\"?>");
		xml.append("<content>");
		for(String key : info.keySet()){
			xml.append("<"+key+">"+info.get(key)+"</"+key+">");
		}
		xml.append("</content>");
		return xml.toString();
	}
	/**
	 * 生成 xml文件流
	 */
	public static String createXML(List<Map<String, Object >> info) {// 获取最大访客数XML
		StringBuffer xml = new StringBuffer();
		xml.append("<?xml version=\"1.0\" encoding=\"gb2312\"?>");
		xml.append("<content>");
		if(info!=null&&info.size()>0){
			for(Map<String, Object > map : info){
				xml.append("<info>");
				for(String key : map.keySet()){
					xml.append("<"+key+">"+map.get(key)+"</"+key+">");
				}
				xml.append("</info>");
			}
		}else {
			xml.append("<info>");
			xml.append("没有数据");
			xml.append("</info>");
		}
		xml.append("</content>");
		return xml.toString();
	}
	/**
	 * 生成 xml文件流
	 */
	public static String createXML(List<Map<String, Object >> info,Long size) {// 获取最大访客数XML
		StringBuffer xml = new StringBuffer();
		xml.append("<?xml version=\"1.0\" encoding=\"gb2312\"?>");
		xml.append("<content count=\""+size+"\">");
		if(info!=null&&info.size()>0){
			for(Map<String, Object > map : info){
				xml.append("<info>");
				for(String key : map.keySet()){
					xml.append("<"+key+">"+map.get(key)+"</"+key+">");
				}
				xml.append("</info>");
			}
		}else {
			xml.append("<info>");
			xml.append("没有数据");
			xml.append("</info>");
		}
		xml.append("</content>");
		return xml.toString();
	}
	
	public static String createJson(List<Map<String, Object >> info){
		String json = "[";
		int i=0;
		int j=0;
		if(info!=null&&info.size()>0){
			for(Map<String, Object > map : info){
				if(i!=0)
					json +=",";
				json+="{";
				for(String key : map.keySet()){
					if(j!=0)
						json +=",";
					json +="\""+key+"\":\""+map.get(key)+"\"";
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
	public static String createJson(Map<String, Object > info){
		String json = "";
		int j=0;
		if(info!=null&&info.size()>0){
			json+="{";
			for(String key : info.keySet()){
				//System.out.println(key);
				if(j!=0)
					json +=",";
				if(info.get(key)!=null&&info.get(key).toString().startsWith("["))
					json +="\""+key+"\":"+info.get(key);
				else {
					json +="\""+key+"\":\""+info.get(key)+"\"";
				}	
				j++;
			}
			json+="}";
		}else {
			json="{}";
		}
		return json;
	}
	/**
	 * 计算停车费
	 * @param start
	 * @param end
	 * @param price
	 * @return
	 */
	public static String getAccount(Long start,Long end,Double price){
		if(start!=null&&end!=null){
			Long duration = getHour(start,end);
			return Math.round(Double.valueOf(price+"")*Double.valueOf(duration))+".00";
		}
		return "";
	}

	
	public static String getTimeString(Long start,Long end){
		Long hour = (end-start)/3600;
		Long minute = ((end-start)%3600)/60;
		String result = "";
		int day = 0;
		if(hour==0)
			result =minute+"分钟";
		else 
			result =hour+"小时"+minute+"分钟";
		if(hour>24){
			day = hour.intValue()/24;
			hour = hour%24;
			result = day+"天 "+hour+"小时"+minute+"分钟";
		}
		return result;
	}
	public static String objArry2String(Object[] values){
		StringBuffer rBuffer = new StringBuffer();
		if(values!=null&&values.length>0){
			for(Object o : values){
				rBuffer.append(o+",");
			}
		}
		return rBuffer.toString();
	}
//	public static void main(String[] args) {
//		String s = null;
//		try {
//			s = MD5("laoyao11111140888993");
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		System.out.println(s);
//	}
	public static String getMondayOfThisWeek() {
		  Calendar c = Calendar.getInstance();
		  int day_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
		  if (day_of_week == 0)
		   day_of_week = 7;
		  c.add(Calendar.DATE, -day_of_week + 1);
		  SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
		  return df2.format(c.getTime()); 
	}
	 public static String getFistdayOfMonth() {
		 Date nowTime=new Date(System.currentTimeMillis());//取系统时间
		 try{       
			 SimpleDateFormat sformat=new SimpleDateFormat("yyyy-MM-01");
			 return sformat.format(nowTime);  
		 }catch(Exception   ex){    
			 ex.printStackTrace();       
		 }
		 return null;
	}
	 
	 public static String getLastFistdayOfMonth() {
		 Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		 c.add(Calendar.MONTH, -1);
		 Date nowTime=new Date(c.getTimeInMillis());//取系统时间
		 try{       
			 SimpleDateFormat sformat=new SimpleDateFormat("yyyy-MM-01");
			 return sformat.format(nowTime);  
		 }catch(Exception   ex){    
			 ex.printStackTrace();       
		 }
		 return null;
	}
	 
	 
	 public static double distanceByLnglat(double _Longitude1, double _Latidute1,
				double _Longitude2, double _Latidute2) {
	    	//0.09446
			double radLat1 = _Latidute1 * Math.PI / 180;
			double radLat2 = _Latidute2 * Math.PI / 180;
			double a = radLat1 - radLat2;
			double b = _Longitude1 * Math.PI / 180 - _Longitude2 * Math.PI / 180;
			double s = 2 * Math.atan(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
					+ Math.cos(radLat1) * Math.cos(radLat2)
					* Math.pow(Math.sin(b / 2), 2)));
			s = s * 6378137.0;// 取WGS84标准参考椭球中的地球长半径(单位:m)
			s = Math.round(s * 10000) / 10000;
			s = (s / 1000) * 0.621371192;
			//int result = (int) Math.ceil(s);
			// System.out.println(result);
			return s;
	}
	 
	 
	 public static void main(String[] args) {
			//System.out.println(distanceByLnglat(116.30697,40.042474,116.316416,40.042474));
//			double d1 = 0.008036;
//			double d2 = 0.005032; 
//			//System.out.println(distanceByLnglat(116.316416,40.042474,116.325862,40.042474));
//			//0.007232
//			//System.out.println(distanceByLnglat(116.316416,40.049716,116.316416,40.042484));
//			double lon = 116.316416;
//			double lat = 40.049716;
//			
//			System.out.println(distanceByLnglat(lon,lat,lon,lat+d2));
//			
//			System.out.println(distanceByLnglat(lon,lat,lon+d1,lat));
//			lon = lon+1;
//			lat = lat+10;
//			
//			System.out.println(distanceByLnglat(lon,lat,lon,lat+d2));
//			                          
//			System.out.println(distanceByLnglat(lon,lat,lon+d1,lat));
		 System.out.println(formatDouble("0.06399"));
		}
	/**
	 * 生成MD5
	 */
	public static String MD5(String s) throws Exception {
		MessageDigest messagedigest = MessageDigest.getInstance("MD5");
		messagedigest.reset();
		byte abyte0[] = messagedigest.digest(s.getBytes());
		return byteToString(abyte0);
	}
	private static String byteToString(byte abyte0[]) {
		int i = abyte0.length;
		char ac[] = new char[i * 2];
		int j = 0;
		for (int k = 0; k < i; k++) {
			byte byte0 = abyte0[k];
			ac[j++] = hexDigits[byte0 >>> 4 & 0xf];
			ac[j++] = hexDigits[byte0 & 0xf];
		}

		return new String(ac);
	}
	private static final char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'a', 'b', 'c', 'd', 'e', 'f' };
	
	
	public static String encodingFileName(String fileName) {
		String returnFileName = "";
		try {
			returnFileName = new String(fileName.getBytes("gb2312"), "ISO8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return returnFileName;
	}
	
	/*
	 * 消息模板公用方法 
	 */
	public static void sendWXTempleteMsg(Map<String, String> baseinfo,List<Map<String, String>> orderinfo,String accesstoken){
		//推送模板消息
		JSONObject msgObject = new JSONObject();
		JSONObject dataObject = new JSONObject();
		for(Map<String, String> map : orderinfo){
			JSONObject keynote = new JSONObject();
			keynote.put("value", map.get("value"));
			keynote.put("color", map.get("color"));
			dataObject.put(map.get("keyword"), keynote);
		}
		msgObject.put("touser", baseinfo.get("openid"));
		msgObject.put("template_id", baseinfo.get("templeteid"));//
		msgObject.put("url", baseinfo.get("url"));
	    msgObject.put("topcolor", baseinfo.get("top_color"));
		msgObject.put("data", dataObject);
		String msg = msgObject.toString();
		System.err.println(">>>>>>>退款发送微信消息："+msg);
		sendMessage(msg, accesstoken);
	}
	public static void sendMessage(String msg, String accesstoken){
		String sendUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accesstoken;
		String result = CommonUtil.httpsRequest(sendUrl, "POST", msg);
		System.err.println(">>>>>>>退款发送微信消息结果："+result);
	}
	
	
	public static String getJsonValue(String rescontent, String key) {
		JSONObject jsonObject;
		String v = null;
		try {
			jsonObject = JSONObject.parseObject(rescontent);
			v = jsonObject.getString(key);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return v;
	}
	public static Map<String,Object> jsonToMap(String json){
		try {
			JSONObject jobj = JSONObject.parseObject(json);
			Map<String, Object>	 retMap = jsonObj2Map(jobj);
			return retMap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static Map<String,Object> jsonObj2Map(JSONObject jobj){
		Map<String,Object> map = new HashMap<String, Object>();
		for (String key : jobj.keySet()) {
//			String key = (String)iter.next();
			try {
				Object value = jobj.get(key);
				key = key.substring(0,1).toLowerCase()+key.substring(1);
				if (value instanceof JSONObject) {
					Map<String,Object> map2 = jsonObj2Map((JSONObject)value);
					map.put(key, map2);
				}else if(value instanceof JSONArray){
					JSONArray value2 = (JSONArray)value;
					List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
					for(int i=0;i<value2.size();i++){
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
}
