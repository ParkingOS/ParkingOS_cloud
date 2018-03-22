package com.zld.impl;

import com.zld.utils.CacheXMemcache;
import com.zld.utils.CachedTask;
import com.zld.utils.TimeTools;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * memcached工具，购买包月产品，支付订单，查询车牌号码
 * @author Administrator
 *
 */
@SuppressWarnings("unchecked")
@Repository
public class MemcacheUtils {


	private Logger logger = Logger.getLogger(MemcacheUtils.class);

	@SuppressWarnings("rawtypes")
	@Autowired
	private CacheXMemcache cacheXMemcache;

	public <T> T get(String key, Class<T> type) {
		try {
			return (T) cacheXMemcache.get(key, type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String get(String key) {
		try {
			return (String) cacheXMemcache.get(key, String.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean set(String key, Object value) {
		try {
			return cacheXMemcache.setCached(key, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean delete(String key) {
		try {
			return cacheXMemcache.delete(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 添加分布式锁
	 * add方法，成功返回true，当数据已经存在时，返回false，应用于分布式锁
	 * @param key
	 * @return
	 */
	public boolean addLock(String key){//存活2秒
		try {
			byte[] b = new byte[0];
			return cacheXMemcache.addCached(key, b, 300);
		} catch (Exception e) {
			logger.error(e);
		}
		return true;//当缓存抛出异常时，返回true，不使用分布式锁
	}
	/**
	 * 添加分布式锁
	 * @param key 锁标志
	 * @param exp 生命周期
	 * @return
	 */
	public boolean addLock(String key, int exp){
		try {
			byte[] b = new byte[0];
			return cacheXMemcache.addCached(key, b, exp);
		} catch (Exception e) {
			logger.error(e);
		}
		return true;//当缓存抛出异常时，返回true，不使用分布式锁
	}
	/**
	 * 删除分布式锁，虽然设置了锁的生命周期是2秒，但是仍建议在finally里调用
	 * @param key
	 * @return
	 */
	public boolean delLock(String key){
		try {
			if(key == null || "".equals(key)){
				return true;
			}
			return cacheXMemcache.delCached(key);
		} catch (Exception e) {
			logger.error(e);
		}
		return false;
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


	public   Map<Long ,Long> doMapLongLongCache(String key,
												Map<Long ,Long> map,String updateFlag) {
		final Map<Long ,Long> map2 = map;
		return (Map<Long ,Long>) cacheXMemcache.doCachedTask(new CachedTask<Map<Long ,Long>>(key, updateFlag) {
			public Map<Long ,Long> run() {
				return map2;
			}
		});
	}
	/*支付使用券缓存  ,key = usetickets_times**/
	public   Map<Long ,String> doMapLongStringCache(String key,
													Map<Long ,String> map,String updateFlag) {
		final Map<Long ,String> map2 = map;
		return (Map<Long ,String>) cacheXMemcache.doCachedTask(new CachedTask<Map<Long ,String>>(key, updateFlag) {
			public Map<Long ,String> run() {
				return map2;
			}
		});
	}

	/**
	 * @param 写入收费员token
	 * @return
	 */
	public  Map<String,String > doMapStringStringCache(String key,Map<String,String > value,String updateFlag) {
		final Map<String,String > value1 = value;
		return (Map<String,String >) cacheXMemcache.doCachedTask(new CachedTask<Map<String,String >>(key, updateFlag) {
			public Map<String,String > run() {
				return value1;
			}
		});
	}



	/**
	 * @param 写入下一个红包索引
	 * @return
	 */
	public  Integer doIntegerCache(String key,Integer value,String updateFlag) {
		final Integer value1 = value;
		return (Integer) cacheXMemcache.doCachedTask(new CachedTask<Integer>(key, updateFlag) {
			public Integer run() {
				return value1;
			}
		});
	}

	/*支付返现缓存，同一车场，同一车主每天只能一个返现3次  ,key = backmoney_times**/
	/*@SuppressWarnings("unchecked")
	public   Map<String ,String> doBackMoneyCache(String key,
			Map<String ,String> map,String updateFlag) {
		final Map map2 = map;
		return (Map<String ,String>) cacheXMemcache.doCachedTask(new CachedTask<Map<String ,String>>(key, updateFlag) {
			public Map<String ,String> run() {
				return map2;
			}
		});
	}*/

	/**
	 * @param 黑名单
	 * @return
	 */
	/*@SuppressWarnings("unchecked")
	public  List<Long> doBlackCache(String key,List<Long> value,String updateFlag) {
		final List<Long> value1 = value;
		return (List<Long>) cacheXMemcache.doCachedTask(new CachedTask<List<Long>>(key, updateFlag) {
			public List<Long> run() {
				return value1;
			}
		});
	}*/


	/**
	 * @param 黑名单
	 * @return
	 */
	public  List<Long> doListLongCache(String key,List<Long> value,String updateFlag) {
		final List<Long> value1 = value;
		return (List<Long>) cacheXMemcache.doCachedTask(new CachedTask<List<Long>>(key, updateFlag) {
			public List<Long> run() {
				return value1;
			}
		});
	}
	/**
	 * @param 收费员消息开关
	 * @return
	 */
	/*@SuppressWarnings("unchecked")
	public  String doCollectorMessageCache(String key,String value,String updateFlag) {
		final String value1 = value;
		return (String) cacheXMemcache.doCachedTask(new CachedTask<String>(key, updateFlag) {
			public String run() {
				return value1;
			}
		});
	}*/
	/**
	 * @param 停车场返现缓存 ，济南车场10%返现
	 * @return
	 */
	public  Map<Long, Integer> doMapLongIntegerCache(String key,Map<Long, Integer> map,String updateFlag) {
		final Map<Long, Integer> map2 = map;
		return (Map<Long, Integer>) cacheXMemcache.doCachedTask(new CachedTask<Map<Long, Integer>>(key, updateFlag) {
			public Map<Long, Integer> run() {
				return map2;
			}
		});
	}

	public  Map<String, Long> doMapStringLongCache(String key,Map<String, Long> map,String updateFlag) {
		final Map<String, Long> map2 = map;
		return (Map<String, Long>) cacheXMemcache.doCachedTask(new CachedTask<Map<String, Long>>(key, updateFlag) {
			public Map<String, Long> run() {
				return map2;
			}
		});
	}





	/**
	 * @param 读取下一个红包索引
	 * @return
	 */
	public Integer readGetHBonusCache(){
		Integer values = doIntegerCache("hbonus_index", null, null);
		logger.error(">>>>hbonus_index:"+values);
		return values;
	}

	/**
	 * @param uin：车主账户.一天只能用一张券
	 * @return
	 */
	public boolean readUseTicketCache(Long uin){
		Long todayLong = TimeTools.getToDayBeginTime();
		Map<Long ,String> map = doMapLongStringCache("usetickets_times", null, null);
		//logger.error(">>>>read>>> uin:"+uin+",map:"+ map);
		if(map!=null&&map.get(uin)!=null){
			String values = map.get(uin);
			logger.error(">>>停车券使用次数 ：cache value:"+values+",uin:"+uin+",today:"+todayLong);
			Long tLong = Long.valueOf(values.split("_")[0]);
			Integer times = Integer.valueOf(values.split("_")[1]);
			if(todayLong.intValue()==tLong.intValue()&&times>0)
				return false;
		}
		return true;
	}
	/**
	 * @param uin：车主账户
	 * @return
	 */
	public void updateUseTicketCache(Long uin){
		Map<Long ,String> map = doMapLongStringCache("usetickets_times", null, null);
		Long todayLong = TimeTools.getToDayBeginTime();
		//logger.error(">>>>update>>> uin:"+uin+",map:"+ map);
		if(map!=null){
			if(map.get(uin)!=null){
				String values = map.get(uin);

				Long tLong = Long.valueOf(values.split("_")[0]);
				Integer times = Integer.valueOf(values.split("_")[1]);
				if(tLong.intValue()==todayLong.intValue())
					map.put(uin, todayLong+"_"+(times+1));
				else {
					map.put(uin, todayLong+"_"+1);
				}
				logger.error(">>>更新停车券使用次数缓存  ：cache value:"+map.get(uin));
			}else {
				map.put(uin, todayLong+"_"+1);
				logger.error(">>>停车券缓存首次使用："+uin);
			}
			//logger.error(">>>停车券缓存存入 ："+map);
		}else {
			map = new HashMap<Long, String>();
			map.put(uin, todayLong+"_"+1);
			logger.error(">>>停车券缓存首次使用："+uin);
		}
		doMapLongStringCache("usetickets_times", map, "update");
	}

	/**
	 * 每天最高的补贴额度是3000
	 * @return
	 */
	public Double readAllowanceCache(){
		Long today = TimeTools.getToDayBeginTime();
		Double allmoney = 0d;
		Map<Long ,String> map = doMapLongStringCache("allowance_money", null, null);
		if( map != null && map.get(today) != null){
			allmoney = Double.valueOf(map.get(today) + "");
		}
		return allmoney;
	}

	/**
	 * 更新每日补贴上限缓存
	 * @param money
	 */
	public void updateAllowanceCache(Double money){
		Map<Long ,String> map = doMapLongStringCache("allowance_money", null, null);
		Long today = TimeTools.getToDayBeginTime();
		if(map != null){
			if(map.get(today) != null){
				Double allowance = Double.valueOf(map.get(today) + "");
				allowance += money;
				map.put(today, allowance + "");
				logger.error(">>>更新每日补贴上限缓存  ：cache value:"+allowance+",today:"+today+",money:"+money);
			}else {
				map.put(today, money+"");
				logger.error(">>>今日首次缓存："+money+",today:"+today);
			}
			//logger.error(">>>停车券缓存存入 ："+map);
		}else {
			map = new HashMap<Long, String>();
			map.put(today, money + "");
			logger.error("创建补贴上限缓存："+money+",today:"+today);
		}
		doMapLongStringCache("allowance_money", map, "update");
	}

	/**
	 * 按照每个车场订单量更新每日补贴缓存
	 * @param money
	 */
	public void updateAllowCacheByPark(Long comid,Double money){
		Map<Long ,String> map = doMapLongStringCache("allow_park_money", null, null);
		Long today = TimeTools.getToDayBeginTime();
		logger.error("updateAllowCacheByPark>>>comid:"+comid+",money:"+money+",today:"+today);
		if(map != null){
			if(map.get(comid) != null){
				String info = map.get(comid);
				Long time = Long.valueOf(info.split("_")[0]);//时间
				if(time.intValue() == today.intValue()){
					Double allow = Double.valueOf(info.split("_")[1]);//今日该车场补贴
					allow += money;
					map.put(comid, today + "_" + allow);
				}else{
					map.put(comid, today + "_" + money);
				}
				logger.error("按车场更新每日补贴缓存time:"+time+"comid:"+comid);
			}else{
				map.put(comid, today + "_" + money);
				logger.error("按车场补贴缓存首次使用"+"comid:"+comid);
			}
		}else{
			map = new HashMap<Long, String>();
			map.put(comid, today + "_" + money);
		}
		doMapLongStringCache("allow_park_money", map, "update");
	}

	/**
	 * 读取每个车场的补贴金额
	 * @return
	 */
	public Double readAllowCacheByPark(Long comid){
		Double allow = 0d;
		Long today = TimeTools.getToDayBeginTime();
		Map<Long ,String> map = doMapLongStringCache("allow_park_money", null, null);
		if( map != null && map.get(comid) != null){
			String info = map.get(comid);
			Long time = Long.valueOf(info.split("_")[0]);//时间
			if(time.intValue() == today.intValue()){
				allow = Double.valueOf(info.split("_")[1]);//今日该车场补贴
			}
		}
		return allow;
	}

	/**
	 * 读取每个车场的补贴金额
	 * @return
	 */
	public Double readAllowLimitCacheByPark(Long comid){
		Double limit = null;//初始值设为null，用于区分无缓存和补贴上限为0
		Map<Long ,String> map = doMapLongStringCache("allow_park_limit", null, null);
		if( map != null && !map.isEmpty()){
			if(map.get(comid) != null){
				limit = Double.valueOf(map.get(comid) + "");
			}else{
				limit = 0d;
			}
		}
		return limit;
	}

	/*支付返券缓存，同一车主每天只能返3次  ,key = backtickets_times**/
	/*@SuppressWarnings("unchecked")
	public   Map<Long ,String> doBackTicketCache(String key,
			Map<Long ,String> map,String updateFlag) {
		final Map map2 = map;
		return (Map<Long ,String>) cacheXMemcache.doCachedTask(new CachedTask<Map<Long ,String>>(key, updateFlag) {
			public Map<Long ,String> run() {
				return map2;
			}
		});
	}*/
	/**
	 * @param uin：车主账户
	 * @return
	 */
	public boolean readBackTicketCache(Long uin){
		Long todayLong = TimeTools.getToDayBeginTime();
		Map<Long ,String> map = doMapLongStringCache("backtickets_times", null, null);
		//logger.error(">>>>read>>> uin:"+uin+",map:"+ map);
		if(map!=null&&map.get(uin)!=null){
			String values = map.get(uin);
			logger.error(">>>停车券返券次数  ：cache value:"+values+",uin:"+uin+",today:"+todayLong);
			Long tLong = Long.valueOf(values.split("_")[0]);
			Integer times = Integer.valueOf(values.split("_")[1]);
			if(todayLong.intValue()==tLong.intValue()&&times>0)
				return false;
		}
		return true;
	}

	/**
	 * @param uin：车主账户
	 * @return
	 */
	public void updateBackTicketCache(Long uin){
		Long todayLong = TimeTools.getToDayBeginTime();
		Map<Long ,String> map = doMapLongStringCache("backtickets_times", null, null);
		//logger.error(">>>>update>>> uin:"+uin+",map:"+ map);
		if(map!=null){
			if(map.get(uin)!=null){
				String values = map.get(uin);

				Long tLong = Long.valueOf(values.split("_")[0]);
				Integer times = Integer.valueOf(values.split("_")[1]);
				if(tLong.intValue()==todayLong.intValue())
					map.put(uin, todayLong+"_"+(times+1));
				else {
					map.put(uin, todayLong+"_"+1);
				}
				logger.error(">>>更新停车券返券次数缓存  ："+map.get(uin));
			}else {
				map.put(uin, todayLong+"_"+1);
				logger.error(">>>停车券缓返券存首次使用："+uin);
			}
			//logger.error(">>>停车券返券次数缓存保存："+map);
		}else {
			map = new HashMap<Long, String>();
			map.put(uin, todayLong+"_"+1);
			logger.error(">>>停车券缓返券存首次使用："+uin);
		}
		doMapLongStringCache("backtickets_times", map, "update");
	}

	/**
	 * @param park_uin：车场_车主账户
	 * @return
	 */
	public boolean readBackMoneyCache(String park_uin){
		Long todayLong = TimeTools.getToDayBeginTime();
		Map<String,String> map = doMapStringStringCache("backmoney_times", null, null);
		//logger.error(">>>>read>>> park_uin:"+park_uin+",map:"+ map);
		if(map!=null&&map.get(park_uin)!=null){
			String values = map.get(park_uin);
			logger.error(">>>停车返现次数  ：cache value:"+values+",park_uin:"+park_uin+",today:"+todayLong);
			Long tLong = Long.valueOf(values.split("_")[0]);
			Integer times = Integer.valueOf(values.split("_")[1]);
			//if(todayLong.intValue()==tLong.intValue()&&times>2)
			if(todayLong.intValue()==tLong.intValue()&&times>0)//同一车场同一车主只返一次两元
				return false;
		}
		return true;
	}
	/**
	 * @param park_uin：车场_车主账户
	 * @return
	 */
	public void updateBackMoneyCache(String park_uin){
		Long todayLong = TimeTools.getToDayBeginTime();
		Map<String,String> map = doMapStringStringCache("backmoney_times", null, null);
		//logger.error(">>>>update>>> park_uin:"+park_uin+",map:"+ map);
		if(map!=null){
			if(map.get(park_uin)!=null){
				String values = map.get(park_uin);
				logger.error(">>>停车返现次数  ：cache value:"+values+",park_uin:"+park_uin+",today:"+todayLong);
				Long tLong = Long.valueOf(values.split("_")[0]);
				Integer times = Integer.valueOf(values.split("_")[1]);
				if(tLong.intValue()==todayLong.intValue())
					map.put(park_uin, todayLong+"_"+(times+1));
				else {
					map.put(park_uin, todayLong+"_"+1);
				}
				logger.error(">>>更新停车返现次数缓存  ：cache value:"+map.get(park_uin));
			}else {
				map.put(park_uin, todayLong+"_"+1);
				logger.error(">>>停车返现券存首次使用："+park_uin);
			}
			//logger.error(">>>停车返现券缓存保存："+map);
		}else {
			map = new HashMap<String, String>();
			map.put(park_uin, todayLong+"_"+1);
			logger.error(">>>停车返现券存首次使用："+park_uin);
		}
		doMapStringStringCache("backmoney_times", map, "update");
	}

	public String getWeixinToken(){
		String weixinToken = doStringCache("zld_weixin_token", null, null);
		if(weixinToken!=null){
			String [] time_token = weixinToken.split("_");
			Long time = Long.valueOf(time_token[0]);
			Long nTime = System.currentTimeMillis()/1000;
			logger.error("weixin token times :"+(nTime-time));
			if(nTime-time<120*60){
				return weixinToken.substring(weixinToken.indexOf("_")+1);
			}
		}
		return "notoken";
	}

	public String setWeixinToken(String token){
		return doStringCache("zld_weixin_token", (System.currentTimeMillis()/1000)+"_"+token, "update");
	}

	public String getWXPublicToken(){
		String weixinToken = doStringCache("zld_wxpublic_token", null, null);
		if(weixinToken!=null){
			String [] time_token = weixinToken.split("_");
			Long time = Long.valueOf(time_token[0]);
			Long nTime = System.currentTimeMillis()/1000;
			logger.error("wxpublic token times :"+(nTime-time));
			if(nTime-time<120*60){
				return weixinToken.substring(weixinToken.indexOf("_")+1);
			}
		}
		return "notoken";
	}

	public String setWXPublicToken(String token){
		return doStringCache("zld_wxpublic_token", (System.currentTimeMillis()/1000)+"_"+token, "update");
	}

	public String getJsapi_ticket(){
		String jsapi_ticket = doStringCache("zld_wxpublic_jsapi_ticket", null, null);
		if(jsapi_ticket!=null){
			String [] time_token = jsapi_ticket.split("_");
			Long time = Long.valueOf(time_token[0]);
			Long nTime = System.currentTimeMillis()/1000;
			logger.error("wxpublic jsapi_ticket times :"+(nTime-time));
			if(nTime-time<120*60){
				return jsapi_ticket.substring(jsapi_ticket.indexOf("_")+1);
			}
		}
		return "no_jsapi_ticket";
	}

	public String setJsapi_ticket(String ticket){
		return doStringCache("zld_wxpublic_jsapi_ticket", (System.currentTimeMillis()/1000)+"_"+ticket, "update");
	}

	public Long getUinUuid(String uuid){
		Map<String,Long> uinUuidMap = doMapStringLongCache("uuid_uin_map", null, null);
		if(uinUuidMap!=null){
			System.err.println(">>>>>>>>>>>>速通卡用户数："+uinUuidMap.size());
			return uinUuidMap.get(uuid);
		}else {
			return -1L;
		}
	}

	public void setUinUuid(Map<String,Long> uinUuidMap){
		doMapStringLongCache("uuid_uin_map", uinUuidMap, "update");
	}


	/**
	 * @param 读取节日红包开关
	 * @return
	 */
	public String readHBonusCache(){
		String values = doStringCache("hbonus_swith", null, null);
		logger.error(">>>>hbonus_swith:"+values);
		return values;
	}


	/**
	 * @param 写入节日红包开关
	 * @return
	 */
	/*@SuppressWarnings("unchecked")
	public  String doHBonusCache(String key,String value,String updateFlag) {
		final String value1 = value;
		return (String) cacheXMemcache.doCachedTask(new CachedTask<String>(key, updateFlag) {
			public String run() {
				return value1;
			}
		});
	}*/



}
