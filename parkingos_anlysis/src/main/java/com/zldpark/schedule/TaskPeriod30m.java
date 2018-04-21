package com.zldpark.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.zldpark.impl.CommonMethods;
import com.zldpark.service.DataBaseService;
import com.zldpark.service.PgOnlyReadService;
import com.zldpark.utils.ExecutorsUtil;
import com.zldpark.utils.MemcacheUtils;

/**
 * 30分一次的定时任务
 * @author whx
 *
 */
public class TaskPeriod30m implements Runnable {
	
	private DataBaseService dataBaseService;
	private PgOnlyReadService pgOnlyReadService;
	private MemcacheUtils memcacheUtils;
	private CommonMethods commonMethods;
	
	public TaskPeriod30m(DataBaseService dataBaseService, PgOnlyReadService pgOnlyReadService,
			MemcacheUtils memcacheUtils, CommonMethods commonMethods){
		this.dataBaseService = dataBaseService;
		this.pgOnlyReadService = pgOnlyReadService;
		this.memcacheUtils = memcacheUtils;
		this.commonMethods = commonMethods;
	}

	private static Logger log = Logger.getLogger(TaskPeriod30m.class);
	
	@Override
	public void run() {
		log.error("********************开始30分钟一次的定时任务***********************");
		try {
			Long curTime = System.currentTimeMillis()/1000;
			ExecutorService pool = ExecutorsUtil.getExecutorService();
			ExeTask callable0 = new ExeTask(curTime, 0);
			ExeTask callable1 = new ExeTask(curTime, 1);
			ExeTask callable2 = new ExeTask(curTime, 2);
			ExeTask callable3 = new ExeTask(curTime, 3);
			Future<Object> future0 = pool.submit(callable0);
			Future<Object> future1 = pool.submit(callable1);
			Future<Object> future2 = pool.submit(callable2);
			Future<Object> future3 = pool.submit(callable3);
			future0.get();
			future1.get();
			future2.get();
			future3.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.error("********************结束30分钟一次的定时任务***********************");
	}
	
	class ExeTask implements Callable<Object>{
		private long curTime;
		private int queue;
		
		public ExeTask(long curTime, int queue) {
			this.curTime = curTime;
			this.queue = queue;
		}
		@Override
		public Object call() throws Exception {
			try {
				switch (queue) {
				case 0:
					sensorOnlineAnly(curTime);
					break;
				case 1:
					faultInduce(curTime);
					break;
				case 2:
					faultSensor(curTime);
					break;
				case 3:
					faultSite(curTime);
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
	private void sensorOnlineAnly(Long ntime){
		try {
			log.error("统计车检器在线率");
			List<Map<String, Object>> onlineList = pgOnlyReadService.getAll("select count(id) ocount,comid from " +
					" dici_tb where beart_time>? and comid>? and is_delete=? group by comid ", 
					new Object[]{ntime - 30 * 60, 0, 0});
			List<Map<String, Object>> allList = pgOnlyReadService.getAll("select count(id) acount,comid from " +
					" dici_tb where comid>? and is_delete=? group by comid ", 
					new Object[]{0, 0});
			if(allList != null && !allList.isEmpty()){
				for(Map<String, Object> map : allList){
					Long comid = Long.valueOf(map.get("comid") + "");
					map.put("ocount", 0);
					if(onlineList != null && !onlineList.isEmpty()){
						for(Map<String, Object> map2 : onlineList){
							Long id = Long.valueOf(map2.get("comid") + "");
							if(comid.intValue() == id.intValue()){
								map.put("ocount", map2.get("ocount"));
								break;
							}
						}
					}
				}
				String sql = "insert into sensor_online_anlysis_tb(comid,create_time,online,total) " +
						" values(?,?,?,?)";
				List<Object[]> values = new ArrayList<Object[]>();
				for(Map<String, Object> map : allList){
					Object[] va = new Object[4];
					va[0] = map.get("comid");
					va[1] = ntime;
					va[2] = map.get("ocount");
					va[3] = map.get("acount");
					values.add(va);
				}
				int r = dataBaseService.bathInsert(sql, values, new int []{4,4,4,4});
				log.error("r:"+r);
			}
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	/**
	 * 统计车检器掉线情况
	 * @param ntime
	 */
	private void faultSensor(Long ntime){//车检器掉线统计
		try {
			log.error("开始统计车检器掉线情况");
			List<Map<String, Object>> list = pgOnlyReadService.getAll("select id,beart_time from " +
					" dici_tb where (beart_time<=? and beart_time is not null) and id not in " +
					" (select sensor_id from device_fault_tb where end_time is null and sensor_id>? ) and is_delete=? ", 
					new Object[]{ntime - 30 * 60, 0, 0});
			if(list != null && !list.isEmpty()){
				String sql = "insert into device_fault_tb(sensor_id,create_time) values(?,?)";
				List<Object[]> values = new ArrayList<Object[]>();
				for(Map<String, Object> map : list){
					Object[] va = new Object[2];
					va[0] = map.get("id");
					va[1] = map.get("beart_time");
					values.add(va);
				}
				int r = dataBaseService.bathInsert(sql, values, new int []{4,4});
				log.error("r:"+r);
			}
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	/**
	 * 统计基站掉线情况
	 * @param ntime
	 */
	private void faultSite(Long ntime){//基站掉线统计
		try {
			log.error("开始统计基站掉线情况");
			List<Map<String, Object>> list = pgOnlyReadService.getAll("select id,heartbeat from " +
					" sites_tb where (heartbeat<=? and heartbeat is not null) and is_delete=? and id not in " +
					" (select site_id from device_fault_tb where end_time is null and site_id>? ) ", 
					new Object[]{ntime - 30 * 60, 0, 0});
			if(list != null && !list.isEmpty()){
				String sql = "insert into device_fault_tb(site_id,create_time) values(?,?)";
				List<Object[]> values = new ArrayList<Object[]>();
				for(Map<String, Object> map : list){
					Object[] va = new Object[2];
					va[0] = map.get("id");
					va[1] = map.get("heartbeat");
					values.add(va);
				}
				int r = dataBaseService.bathInsert(sql, values, new int []{4,4});
				log.error("r:"+r);
			}
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	/**
	 * 统计诱导屏掉线情况
	 * @param ntime
	 */
	private void faultInduce(Long ntime){//诱导屏掉线统计
		try {
			log.error("开始统计诱导屏掉线情况");
			List<Map<String, Object>> list = pgOnlyReadService.getAll("select id,heartbeat_time from " +
					" induce_tb where (heartbeat_time<=? and heartbeat_time is not null) and is_delete=?" +
					" and id not in (select induce_id from device_fault_tb where end_time is null and induce_id>? ) ", 
					new Object[]{ntime - 30 * 60, 0, 0});
			if(list != null && !list.isEmpty()){
				String sql = "insert into device_fault_tb(induce_id,create_time) values(?,?)";
				List<Object[]> values = new ArrayList<Object[]>();
				for(Map<String, Object> map : list){
					Object[] va = new Object[2];
					va[0] = map.get("id");
					va[1] = map.get("heartbeat_time");
					values.add(va);
				}
				int r = dataBaseService.bathInsert(sql, values, new int []{4,4});
				log.error("r:"+r);
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

}
