package parkingos.com.bolink.utlis.weixinpay.memcachUtils;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * memcached工具
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

	/**
	 * 添加分布式锁
	 * add方法，成功返回true，当数据已经存在时，返回false，应用于分布式锁
	 * @param key 锁标志
	 * @return
	 */
	public boolean addLock(String key){//存活5分钟
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
	 * 删除分布式锁，因为设置了锁的生命周期是5分钟，强烈建议在finally里调用
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
	
	/**
	 * set命令不但可以简单添加，如果set的key已经存在，该命令可以更新该key所对应的原来的数据，也就是实现更新的作用。
	 * @param key
	 * @param value
	 * @param exp
	 * @return
	 */
	public boolean setCache(String key, Object value, int exp){
		try {
			return cacheXMemcache.setCached(key, value, exp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public Object getCache(String key){
		try {
			return cacheXMemcache.getCached(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String doStringCache(String key,
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
	
	public   Map<Long ,String> doMapLongStringCache(String key,
			Map<Long ,String> map,String updateFlag) {
		final Map<Long ,String> map2 = map;
		return (Map<Long ,String>) cacheXMemcache.doCachedTask(new CachedTask<Map<Long ,String>>(key, updateFlag) {
			public Map<Long ,String> run() {
				return map2;
			}
		});
	}
	
	public  Map<String,String > doMapStringStringCache(String key,Map<String,String > value,String updateFlag) {
		final Map<String,String > value1 = value;
		return (Map<String,String >) cacheXMemcache.doCachedTask(new CachedTask<Map<String,String >>(key, updateFlag) {
			public Map<String,String > run() {
				return value1;
			}
		});
	}
	
	public  Integer doIntegerCache(String key,Integer value,String updateFlag) {
		final Integer value1 = value;
		return (Integer) cacheXMemcache.doCachedTask(new CachedTask<Integer>(key, updateFlag) {
			public Integer run() {
				return value1;
			}
		});
	}
	
	public  List<Long> doListLongCache(String key,List<Long> value,String updateFlag) {
		final List<Long> value1 = value;
		return (List<Long>) cacheXMemcache.doCachedTask(new CachedTask<List<Long>>(key, updateFlag) {
			public List<Long> run() {
				return value1;
			}
		});
	}
	/**
	 
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
	
	public  Map<Long, List<Map<String, Object>>> doMapLongListCache(String key,Map<Long, List<Map<String, Object>>> map,String updateFlag) {
		final Map<Long, List<Map<String, Object>>> map2 = map;
		return (Map<Long, List<Map<String, Object>>>) cacheXMemcache.doCachedTask(new CachedTask<Map<Long, List<Map<String, Object>>>>(key, updateFlag) {
			public Map<Long, List<Map<String, Object>>> run() {
				return map2;
			}
		});
	}
	
	/**
	 * 判断是否是同一次请求
	 * @param key 缓存key
	 * @param request 请求
	 * @param unionId 联盟平台账户
	 * @param rand 随机数
	 * @return 是否是同一次
	 */
	public boolean isDuplicateQuery(String key,String request,Long unionId,String rand){
		/**rand判断，同一个平台账户同一个请求，rand值不可重复*/
		Map<String, String> unionIdRequestRandMap =	doMapStringStringCache(key, null, null);
		if(unionIdRequestRandMap!=null){
			//取出上一次请求的rand
			String preRand = unionIdRequestRandMap.get(unionId+"_"+request);
			logger.info(key+",unionid="+unionId+",prerand="+preRand+",rand="+rand);
			if(preRand!=null){
				if(preRand.equals(rand)){//判断为重复提交，不查询结果，返回错误
					logger.info(key+",unionid="+unionId+",rand值相同，为非法提交，返回错误");
					return true;
				}
			}
			//放入新的rand
			unionIdRequestRandMap.put(unionId+"_"+request, rand);
		}else {//第一次查询，初始化缓存
			unionIdRequestRandMap=new HashMap<String, String>();
			unionIdRequestRandMap.put(unionId+"_"+request, rand);
		}
		doMapStringStringCache(key, unionIdRequestRandMap, "update");
		/**rand判断，同一个平台账户同一个请求，rand值不可重复*/
		return false;
	}
	
}
	
