package parkingos.com.bolink.utils;


import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;


public class RequestUtil {
	static Logger logger = Logger.getLogger(RequestUtil.class);
	
	//*******************从request的parameter中获取数据*************************//
	public static String processParams(HttpServletRequest request,String param){
		if(request.getParameter(param)!=null)
			return request.getParameter(param);
		return "";
	}
	
	public static Integer getInteger(HttpServletRequest request ,String param,Integer defaultvalue){
		String value = processParams(request, param);
		if(value.equals(""))
			return defaultvalue;
		else {
			try {
				Integer integer = Integer.valueOf(value);
				return integer;
			} catch (Exception e) {
				return defaultvalue;
			}
		}
	}
	
	public static Long getLong(HttpServletRequest request ,String param,Long defaultvalue){
		String value = processParams(request, param);
		if(value.equals(""))
			return defaultvalue;
		else {
			try {
				Long lvalue = Long.valueOf(value);
				return lvalue;
			} catch (Exception e) {
				return defaultvalue;
			}
		}
	}

	
	public static String getString(HttpServletRequest request ,String param){
		return  processParams(request, param);
	}
	
	public static Double getDouble(HttpServletRequest request ,String param,Double defaultvalue){
		String value = processParams(request, param);
		if(value.equals(""))
			return defaultvalue;
		else {
			try {
				Double dvalue = Double.valueOf(value);
				return dvalue;
			} catch (Exception e) {
				return defaultvalue;
			}
		}
	}
	
	//*******************从request的attr中获取数据*************************//
	
	public static Object processAttr(HttpServletRequest request,String param){
		if(request.getAttribute(param)!=null)
			return request.getAttribute(param);
		return null;
	}
	
	public static Long getAttrLong(HttpServletRequest request,String param,Long defaultvalue){
		Object processAttr = processAttr(request, param);
		if(processAttr==null){
			return defaultvalue;
		}else{
			try {
				Long value = Long.valueOf(processAttr+"");
				return value;
			} catch (NumberFormatException e) {
				return defaultvalue;
			}
		}
	}
	
	public static Double getAttrDouble(HttpServletRequest request,String param,Double defaultvalue){
		Object processAttr = processAttr(request, param);
		if(processAttr==null){
			return defaultvalue;
		}else{
			try {
				Double value = Double.valueOf(processAttr+"");
				return value;
			} catch (NumberFormatException e) {
				return defaultvalue;
			}
		}
	}
	
	public static Integer getAttrInteger(HttpServletRequest request,String param,Integer defaultvalue){
		Object processAttr = processAttr(request, param);
		if(processAttr==null){
			return defaultvalue;
		}else{
			try {
				Integer value = Integer.valueOf(processAttr+"");
				return value;
			} catch (NumberFormatException e) {
				return defaultvalue;
			}
		}
	}
	
	public static String getAttrString(HttpServletRequest request,String param){
		Object processAttr = processAttr(request, param);
		if(processAttr==null){
			return "";
		}else{
			try {
				String value = processAttr+"";
				return value;
			} catch (NumberFormatException e) {
				return "";
			}
		}
	}
	
	public static boolean exists(String s,String... strs){
		for(int i=0;i<strs.length;i++){
			if(strs[i].equals(s)){
				return true;
			}
		}
		return false;
	}
	

}
