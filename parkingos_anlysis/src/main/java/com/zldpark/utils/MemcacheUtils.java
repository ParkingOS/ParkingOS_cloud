package com.zldpark.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;



/**
 * memcached工具，购买包月产品，支付订单，查询车牌号码 
 * @author Administrator
 *
 */

@Repository
public class MemcacheUtils {

	@Autowired
	private CacheXMemcache cacheXMemcache;
	

	/**
	 * @param 收费员心跳
	 * @return
	 */
	public Map<Long, Long> readParkerTokentimCache(List<Long> uinList){
		Map<Long, Long> values = new HashMap<Long, Long>();
		for(Long uin : uinList){
			Long v = doParkerTokentimCache("parker_token"+uin, null, null);
			if(v!=null)
				values.put(uin, v);
		}
		return values;
	}
	
	/**
	 * @param 写入收费员心跳
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Long doParkerTokentimCache(String key,Long value,String updateFlag) {
		final Long value1 = value;
		return (Long) cacheXMemcache.doCachedTask(new CachedTask<Long>(key, updateFlag) {
			public Long run() {
				return value1;
			}
		});
	}

	public  String doStringCache(String key,
			String map,String updateFlag) {
		final String map2 = map;
		return (String) cacheXMemcache.doCachedTask(new CachedTask<String>(key, updateFlag) {
			public String run() {
				return map2;
			}
		});
	}
	public String setWXPublicToken(String token){
		return doStringCache("zld_wxpublic_token", (System.currentTimeMillis()/1000)+"_"+token, "update");
	}
	public String getWXPublicToken(){
		String weixinToken = doStringCache("zld_wxpublic_token", null, null);
		if(weixinToken!=null){
			String [] time_token = weixinToken.split("_");
			System.err.println(">>>>>>>>>>>>>>>>>>weixinToken:"+weixinToken);
			Long time = Long.valueOf(time_token[0]);
			Long nTime = System.currentTimeMillis()/1000;
			if(nTime-time<120*60){
				return weixinToken.substring(weixinToken.indexOf("_")+1);
			}
		}
		return "notoken";
	}
	
	public  Map<Long ,String> doMapLongStringCache(String key,
			Map<Long ,String> map,String updateFlag) {
		final Map<Long ,String> map2 = map;
		return (Map<Long ,String>) cacheXMemcache.doCachedTask(new CachedTask<Map<Long ,String>>(key, updateFlag) {
			public Map<Long ,String> run() {
				return map2;
			}
		});
	}
}
