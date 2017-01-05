package com.zld.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;

public class JdbcUtils {
	@SuppressWarnings("unchecked")
	public static List createObject(Class<?> c,List<Map<String, String>> resultList) throws Exception{
		
		List list= new ArrayList();
		for(Map<String, String> m : resultList){
			Iterator<String> keys = m.keySet().iterator();
			Object t =  c.newInstance();
			JXPathContext jxpcontext = JXPathContext.newContext(t);
			while(keys.hasNext()){
				String key = keys.next().toLowerCase();
				if(key.equals("my_rownum"))//ÅÅ³ý·ÖÒ³×Ö¶Î
					continue;
				try {
					jxpcontext.setValue(key, m.get(key.toUpperCase()));
				} catch (Exception e) {
					System.err.println("error:method="+key);
					//e.printStackTrace();
				}
			}
			list.add(t);
		}
		return list;
	}
}
