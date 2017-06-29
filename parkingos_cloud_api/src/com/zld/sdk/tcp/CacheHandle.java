package com.zld.sdk.tcp;

import java.util.ArrayList;
import java.util.List;

/**
 * 防止因网络不同造成重复请求，采用缓存处理的类
 * @author liuqb
 * @date  2017-4-10
 */
public class CacheHandle {
	//定义一个存储sign值的List变量
	private static List<String> signs=new ArrayList<String>();;
	
    /**
     * 判断缓存中是否存在当前sign值
     * @param item
     * @return boolean
     * 如果存在返回true，否则为false
     */
    public static synchronized boolean containsItem(String item) {
		boolean isContain = signs.contains(item);
		if(signs.size()>1200){//超过1200个元素时，删除500个
			int k = signs.size()-700;
			for(int i =0;i<k;i++)
				signs.remove(0);
		}
		if(isContain){
			if(!signs.contains(item))
				signs.add(item);
			return true;
		}else {
			signs.add(item);
		}
    	return false;
    }
    
}
