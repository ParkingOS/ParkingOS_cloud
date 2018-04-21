package com.zldpark.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
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
 * 10分钟执行一次
 * @author whx
 *
 */
public class TaskPeriod10m implements Runnable {
	
	private DataBaseService dataBaseService;
	private PgOnlyReadService pgOnlyReadService;
	private MemcacheUtils memcacheUtils;
	private CommonMethods commonMethods;
	
	public TaskPeriod10m(DataBaseService dataBaseService, PgOnlyReadService pgOnlyReadService,
			MemcacheUtils memcacheUtils, CommonMethods commonMethods){
		this.dataBaseService = dataBaseService;
		this.pgOnlyReadService = pgOnlyReadService;
		this.memcacheUtils = memcacheUtils;
		this.commonMethods = commonMethods;
	}

	private static Logger log = Logger.getLogger(TaskPeriod10m.class);
	
	@Override
	public void run() {
		log.error("********************开始10分钟一次的定时任务***********************");
		try {
			Long curTime = System.currentTimeMillis()/1000;
			ExecutorService pool = ExecutorsUtil.getExecutorService();
			ExeTask callable0 = new ExeTask(curTime, 0);
			ExeTask callable1 = new ExeTask(curTime, 1);
			Future<Object> future0 = pool.submit(callable0);
			Future<Object> future1 = pool.submit(callable1);
			future0.get();
			future1.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.error("********************结束10分钟一次的定时任务***********************");
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
					peakAlertAnly(curTime);
					break;
				case 1:
					updateOnlineStat(curTime);
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
	
	private void peakAlertAnly(Long ntime){
		try {
			log.error("======开始统计车场利用率=======");
			String sql = "insert into peakalert_anlysis_tb(create_time,comid,present,berths) values(?,?,?,?) ";
			List<Map<String, Object>> list = pgOnlyReadService.getAll("select c.id,p.rcount,p.parking_total,c.hotarea_id,c.company_name,c.cityid " +
					"from (select sum(amount) rcount,sum(total) parking_total,comid from remain_berth_tb where state=? group by comid) as p," +
					"com_info_tb c where p.comid=c.id order by p.rcount desc  ", new Object[]{0});
			if(list != null && !list.isEmpty()){
				List<Object[]> values = new ArrayList<Object[]>();
				for(Map<String, Object> map : list){
					Object[] va = new Object[4];
					va[0] = ntime;
					va[1] = (Long)map.get("id");
					Long rcount = Long.valueOf(map.get("rcount") + "");
					Long parking_total = Long.valueOf(map.get("parking_total") + "");
					Long ocount = 0L;
					if(parking_total > rcount){
						ocount = parking_total - rcount;
					}
					va[2] = ocount;
					va[3] = parking_total;
					values.add(va);
					map.put("ocount", ocount);
				}
				
				int r = dataBaseService.bathInsert(sql, values, new int []{4,4,4,4});
				log.error("r:"+r);
				try {
					parkHotAlert(list, ntime);
				} catch (Exception e) {
					// TODO: handle exception
					log.error("parkHotAlert", e);
				}
			}
		} catch (Exception e) {
			log.error("peakAlertAnly", e);
		}
	}
	
	private void updateOnlineStat(Long ntime){
		try {
			//心跳集合中
			log.error(">>>>开始查询所有收费员和巡查员的在线情况....");
			List<Map<String,Object>> uinMap = pgOnlyReadService.getAll("select id from user_info_tb where auth_flag in(?,?,?)", 
					new Object[]{1,2,16});
			List<Long> uinList = new ArrayList<Long>();
			if(uinMap != null && !uinMap.isEmpty()){
				for(Map<String,Object> uMap : uinMap){
					uinList.add((Long)uMap.get("id"));
				}
			}
			Map<Long , Long> userMapCache = memcacheUtils.readParkerTokentimCache(uinList);
			List<Object> onlineList = new ArrayList<Object>();
			List<Object> offlineList = new ArrayList<Object>();
			//过滤掉心跳时间超过10分钟的收费员
			if(userMapCache != null && !userMapCache.isEmpty()){
				for(Long key : userMapCache.keySet()){
					if(userMapCache.get(key) > ntime - 10*60){
						onlineList.add(key);
					}else{
						offlineList.add(key);
					}
				}
			}
			log.error("当前在线人数："+onlineList.size()+",离线人数："+offlineList.size());
			if(onlineList != null && !onlineList.isEmpty()){
				String preParms = "";
				for(Object online : onlineList){
					if(preParms.equals(""))
						preParms = "?";
					else
						preParms +=",?";
				}
				List<Object> params1 = new ArrayList<Object>();
				params1.add(22);
				params1.addAll(onlineList);
				int r = dataBaseService.update("update user_info_tb set online_flag=? where id in ("+preParms+") ", params1);
				log.error("更新在线数，r:"+r);
			}
			if(offlineList != null && !offlineList.isEmpty()){
				String preParms = "";
				for(Object offline : offlineList){
					if(preParms.equals(""))
						preParms = "?";
					else
						preParms +=",?";
				}
				List<Object> params2 = new ArrayList<Object>();
				params2.add(21);
				params2.addAll(offlineList);
				int r = dataBaseService.update("update user_info_tb set online_flag=? where id in ("+preParms+") ", params2);
				log.error("更新离线数，r:"+r);
			}
			
		} catch (Exception e) {
			log.error("updateOnlineStat", e);
		}
	}
	
	private void parkHotAlert(List<Map<String, Object>> list, Long ntime){
		List<Map<String, Object>> hotarea = new ArrayList<Map<String,Object>>();
		List<Object> hotList = new ArrayList<Object>();
		double rateline = 0.85d;
		List<Map<String, Object>> hottingList = pgOnlyReadService.getAll("select count(id) hcount,comid from city_peakalert_tb where comid>? and state=? group by comid ", 
				new Object[]{0, 0});//查询所有未结束的停车场高峰预警
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				Long comid = (Long)map.get("id");
				Long cityid = (Long)map.get("cityid");
				Long hotarea_id = (Long)map.get("hotarea_id");
				Long ocount = Long.valueOf(map.get("ocount") + "");
				Long parking_total = Long.valueOf(map.get("parking_total") + "");
				if(parking_total == 0){
					continue;
				}
				double rate = (double)ocount/parking_total;
				//*********************************车场高峰预警管理********************************//
				Long count = 0L;
				if(hottingList != null && !hottingList.isEmpty()){
					for(Map<String, Object> map2 : hottingList){
						Long cid = (Long)map2.get("comid");
						Long hcount = (Long)map2.get("hcount");
						if(cid.intValue() == comid.intValue()){
							count = hcount;
							break;
						}
					}
				}
				if(rate >= rateline){//高峰预警
					if(count > 0){//之前有车场预警
						int r = dataBaseService.update("update city_peakalert_tb set handle_time=? where comid=? and state=? ", 
								new Object[]{ ntime, comid, 0});
					}else{//新增车场预警
						int r = dataBaseService.update("insert into city_peakalert_tb(title,content,state,create_time,cityid,comid) values(?,?,?,?,?,?) ", 
								new Object[]{"停车场高峰预警",map.get("company_name")+"泊位利用率大于85%",0, ntime, cityid, comid});
					}
				}else{//不是高峰预警
					if(count > 0){//之前有高峰预警
						int r = dataBaseService.update("update city_peakalert_tb set state=?,handle_time=? where comid=? and state=? ", 
								new Object[]{ 1, ntime, comid, 0});
					}
				}
				//*********************************热点区域********************************//
				if(hotarea_id > 0){
					if(hotList.contains(hotarea_id)){
						for(Map<String, Object> map2 : hotarea){
							Long hid = (Long)map2.get("hotarea_id");
							Long acount = (Long)map2.get("acount");
							Integer pcount = (Integer)map2.get("pcount");
							if(hid.intValue() == hotarea_id.intValue()){
								map2.put("acount", acount + ocount);
								map2.put("pcount", pcount + parking_total);
								break;
							}
						}
					}else{
						hotList.add(hotarea_id);
						Map<String, Object> infoMap = new HashMap<String, Object>();
						infoMap.put("hotarea_id", hotarea_id);
						infoMap.put("acount", ocount);
						infoMap.put("pcount", parking_total);
						hotarea.add(infoMap);
					}
				}
			}
		}
		try {
			areaHotAlert(hotarea, ntime);
		} catch (Exception e) {
			// TODO: handle exception
			log.error("areaHotAlert", e);
		}
	}
	
	private void areaHotAlert(List<Map<String, Object>> list , Long ntime){
		if(list != null && !list.isEmpty()){
			List<Object> params = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> map : list){
				params.add(map.get("hotarea_id"));
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			List<Map<String, Object>> areaList = pgOnlyReadService.getAllMap("select id,name,cityid from city_hotarea_tb where id in ("+preParams+") ", params);
			if(areaList != null && !areaList.isEmpty()){
				double rateline = 0.85d;
				List<Map<String, Object>> hottingList = pgOnlyReadService.getAll("select count(id) hcount,hotarea_id from city_peakalert_tb where hotarea_id>? and state=? group by hotarea_id ", 
						new Object[]{0, 0});//查询所有未结束的热点区域高峰预警
				for(Map<String, Object> map : list){
					Long hid = (Long)map.get("hotarea_id");
					Long acount = (Long)map.get("acount");
					Integer pcount = Integer.valueOf(map.get("pcount") + "");
					if(pcount == 0){
						continue;
					}
					String areaname = "";
					Long cityid = -1L;
					for(Map<String, Object> map2 : areaList){
						Long hotarea_id = (Long)map2.get("id");
						if(hotarea_id.intValue() == hid.intValue()){
							if(map2.get("name") != null){
								areaname = (String)map2.get("name");
							}
							cityid = (Long)map2.get("cityid");
							break;
						}
					}
					Long count = 0L;
					if(hottingList != null && !hottingList.isEmpty()){
						for(Map<String, Object> map2 : hottingList){
							Long hotarea_id = (Long)map2.get("hotarea_id");
							Long hcount = (Long)map2.get("hcount");
							if(hid.intValue() == hotarea_id.intValue()){
								count = hcount;
								break;
							}
						}
					}
					double rate = (double)acount/pcount;
					if(rate >= rateline){
						if(count > 0){
							int r = dataBaseService.update("update city_peakalert_tb set handle_time=? where hotarea_id=? and state=? ", 
									new Object[]{ ntime, hid, 0});
						}else{
							int r = dataBaseService.update("insert into city_peakalert_tb(title,content,state,create_time,cityid,hotarea_id) values(?,?,?,?,?,?) ", 
									new Object[]{"热点区域高峰预警",areaname+"泊位利用率大于85%",0, ntime, cityid, hid});
						}
					}else{
						if(count > 0){
							int r = dataBaseService.update("update city_peakalert_tb set state=?,handle_time=? where hotarea_id=? and state=? ", 
									new Object[]{ 1, ntime, hid, 0});
						}
					}
				}
			}
		}
	}
}
