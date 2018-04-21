package com.zldpark.schedule;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.zldpark.utils.TimeTools;

public class TaskPeriod1h implements Runnable {
	private DataBaseService dataBaseService;
	private PgOnlyReadService pgOnlyReadService;
	private MemcacheUtils memcacheUtils;
	private CommonMethods commonMethods;
	
	public TaskPeriod1h(DataBaseService dataBaseService, PgOnlyReadService pgOnlyReadService,
			MemcacheUtils memcacheUtils, CommonMethods commonMethods){
		this.dataBaseService = dataBaseService;
		this.pgOnlyReadService = pgOnlyReadService;
		this.memcacheUtils = memcacheUtils;
		this.commonMethods = commonMethods;
	}

	private static Logger log = Logger.getLogger(TaskPeriod1h.class);
	
	@Override
	public void run() {
		log.error("********************开始1小时一次的定时任务***********************");
		try {
			Long curTime = System.currentTimeMillis()/1000;
			ExecutorService pool = ExecutorsUtil.getExecutorService();
			ExeTask callable0 = new ExeTask(curTime, 0);
			ExeTask callable1 = new ExeTask(curTime, 1);
			ExeTask callable2 = new ExeTask(curTime, 2);
			Future<Object> future0 = pool.submit(callable0);
			Future<Object> future1 = pool.submit(callable1);
			Future<Object> future2 = pool.submit(callable2);
			future0.get();
			future1.get();
			future2.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.error("********************结束1小时一次的定时任务***********************");
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
					anlyPark(curTime);
					break;
				case 1:
					anlyRoadPark(curTime);
					break;
				case 2:
					anlyOnline(curTime);
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
	//每小时统计一次车场泊位空闲情况
	private void anlyPark(Long ntime){//统计封闭车场余位信息
		Long begintime = TimeTools.getToDayBeginTime();
		log.error("======开始统计空闲泊位=======");
		try {
			//查询一天之内有订单的车场 
			List<Map<String, Object>> comList = pgOnlyReadService.getAll("select id,parking_total,share_number,invalid_order from com_info_tb where id " +
					" in (select distinct(comid) as cid from order_tb where create_time>?) and parking_type<>? ", new Object[]{begintime-24*60*60, 2});
			if(comList!=null&&!comList.isEmpty()){
				log.error("========需要统计的车场数:"+comList.size());
				List<Object> idList = new ArrayList<Object>();
				//每一个车场分析两天前及14天前的无效订单，计算出空闲泊位 
				String inserSql = "insert into park_anlysis_tb (create_time,comid,share_count,used_count,month_used_count,time_used_count)" +
						" values(?,?,?,?,?,?)";
				String remainSql = "insert into remain_berth_tb(comid,amount,total,update_time) values(?,?,?,?) ";
				List<Object[]> anlyList = new ArrayList<Object[]>();
				List<Object[]> remainList = new ArrayList<Object[]>();
				for(Map<String, Object> cMap: comList){
					Long comId =(Long) cMap.get("id");//车场编号
					idList.add(comId);
					Integer parking_total = 0;
					Integer share_number = 0;
					Long invalid_order = 0L;//未结算的垃圾订单数
					if(cMap.get("parking_total") != null){
						parking_total = (Integer)cMap.get("parking_total");
					}
					if(cMap.get("share_number") != null){
						share_number = (Integer)cMap.get("share_number");
					}
					if(cMap.get("invalid_order") != null){
						invalid_order = (Long)cMap.get("invalid_order");
					}
					if(share_number == 0){
						share_number = parking_total;
					}
					Long useCount = 0L;
					if(comId != null && comId > 0){
						long time2 = ntime-2*24*60*60;
						long time16 = ntime-16*24*60*60;
						Long month_used_count = 0L;
						Long time_used_count = 0L;
						String sql = "select count(ID) ucount,c_type from order_tb where comid=? and create_time>? and state=? group by c_type ";
						List<Map<String, Object>> allList = pgOnlyReadService.getAll(sql, new Object[]{comId,time2,0});
						if(allList != null && !allList.isEmpty()){
							for(Map<String, Object> map : allList){
								Integer c_type = (Integer)map.get("c_type");
								Long ucount = (Long)map.get("ucount");
								if(c_type == 5 || c_type == 7 || c_type == 8){//月卡泊位占用数
									month_used_count = ucount;
								}else{//时租泊位占用数
									time_used_count += ucount;
								}
							}
						}
						
						Long invmonth_used_count = 0L;
						Long invtime_used_count = 0L;
						String sql1 = "select count(ID) ucount,c_type from order_tb where comid=? and create_time>? and create_time<? and state=? group by c_type ";
						List<Map<String, Object>> invList = pgOnlyReadService.getAll(sql1, new Object[]{comId,time16,time2,0});
						if(invList != null && !invList.isEmpty()){
							for(Map<String, Object> map : invList){
								Integer c_type = (Integer)map.get("c_type");
								Long ucount = (Long)map.get("ucount");
								if(c_type == 5 || c_type == 7 || c_type == 8){//月卡泊位占用数
									invmonth_used_count = ucount;
								}else{//时租泊位占用数
									invtime_used_count += ucount;
								}
							}
						}
						//******************计算两天总共产生的垃圾订单数*****************************//
						int inv_month = (int) (invmonth_used_count*2/14);
						int inv_time = (int) (invtime_used_count*2/14);
						
						if(month_used_count >= inv_month){
							month_used_count -= inv_month;
						}else{
							month_used_count = 0L;
						}
						
						if(time_used_count >= inv_time){
							time_used_count -= inv_time;
						}else{
							time_used_count = 0L;
						}
						//********************减去偏移量*********************************//
						double rate = 0;
						if(month_used_count + time_used_count > 0){
							rate = (double)month_used_count/(month_used_count + time_used_count);//偏移量比率
						}
						Long month_offset = Math.round(invalid_order * rate);//月卡泊位占用偏移量
						month_used_count = month_used_count - month_offset;//月卡车去掉偏移量后的占用泊位数
						time_used_count = time_used_count - (invalid_order - month_offset);//去掉偏移量后的时租车占用泊位数
						if(month_used_count < 0){
							month_used_count = 0L;
						}
						if(time_used_count < 0){
							time_used_count = 0L;
						}
						useCount = month_used_count + time_used_count;
						if(useCount > share_number){
							share_number = useCount.intValue();
						}
						Object[] values = new Object[]{ntime,comId,share_number,useCount.intValue(),month_used_count.intValue(),time_used_count.intValue()};
						anlyList.add(values);
						Long remain = share_number - useCount;
						Object[] values2 = new Object[]{comId,remain.intValue(),share_number,ntime};
						remainList.add(values2);
					}
				}
				log.error("======需要写入数据库的中记录数："+anlyList.size());
				if(!anlyList.isEmpty()){
					int ret = dataBaseService.bathInsert(inserSql, anlyList, new int[]{4,4,4,4,4,4});
					log.error("=======写入完成，条数:"+ret);
				}
				if(!remainList.isEmpty()){
					String preParams  ="";
					for(Object id : idList){
						if(preParams.equals(""))
							preParams ="?";
						else
							preParams += ",?";
					}
					int ret = dataBaseService.updateParamList("delete from remain_berth_tb where comid in ("+preParams+")", idList);
					ret = dataBaseService.bathInsert(remainSql, remainList, new int[]{4,4,4,4});
					log.error("写入余位表===ret："+ret);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			log.error("anlyPark error",e);
		}
	}
	
	private void anlyRoadPark(Long ntime){//道路停车场余位信息
		log.error("======开始统计道路停车场空闲泊位=======");
		try {
			Long begintime = TimeTools.getToDayBeginTime();
			//查询一天之内有订单的车场 
			List<Map<String, Object>> comList = pgOnlyReadService.getAll("select id from com_info_tb where id " +
					" in (select distinct(comid) as cid from order_tb where create_time>?) and parking_type=? ", new Object[]{begintime-24*60*60, 2});
			List<Object> paramList = new ArrayList<Object>();
			List<Object> paramList2 = new ArrayList<Object>();
			List<Object> idList = new ArrayList<Object>();
			if(comList != null && !comList.isEmpty()){
				String preParams  ="";
				for(Map<String, Object> map : comList){
					Long comId = (Long)map.get("id");//车场编号
					idList.add(comId);
					
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				updateRoadBerth(idList, ntime);//一个小时统计一次道路停车的泊位数,要在校验前面
				
				paramList.addAll(idList);
				paramList.add(0);
				paramList.add(0);
				paramList.add(0);
				List<Map<String, Object>> list = pgOnlyReadService.getAllMap("select count(distinct p.id) amount,p.comid,p.berthsec_id from com_park_tb p left join dici_tb d on p.dici_id=d.id where " +
						" p.comid in ("+preParams+") and (p.order_id is null or p.order_id<?) and (d.state=? or d.state is null) and p.is_delete=? group by p.comid,p.berthsec_id ", paramList);
				paramList2.addAll(idList);
				paramList2.add(0);
				List<Map<String, Object>> berthlist = pgOnlyReadService.getAllMap("select count(id) total,comid,berthsec_id from com_park_tb where " +
						"comid in ("+preParams+") and is_delete=? group by comid,berthsec_id ", paramList2);
				log.error("size:"+list.size());
				if(list != null && !list.isEmpty()){
					String remainSql = "insert into remain_berth_tb(comid,amount,berthseg_id,total,update_time) values(?,?,?,?,?) ";
					List<Object[]> anlyList = new ArrayList<Object[]>();
					for(Map<String, Object> map : list){
						Long comid = (Long)map.get("comid");
						Long amount = (Long)map.get("amount");
						Long berthsec_id = (Long)map.get("berthsec_id");
						Long total = 0L;
						for(Map<String, Object> map2 : berthlist){
							Long cid = (Long)map2.get("comid");
							Long bid = (Long)map2.get("berthsec_id");
							if(cid.intValue() == comid.intValue() && bid.intValue() == berthsec_id.intValue()){
								total = (Long)map2.get("total");
								break;
							}
						}
						map.put("total", total);
						Object[] values2 = new Object[]{comid,amount.intValue(),berthsec_id,total.intValue(),ntime};
						anlyList.add(values2);
					}
					
					if(!anlyList.isEmpty()){
						int ret = dataBaseService.updateParamList("delete from remain_berth_tb where comid in ("+preParams+") ", idList);
						ret = dataBaseService.bathInsert(remainSql, anlyList, new int[]{4,4,4,4,4});
						log.error("插入数据，ret："+ret);
					}
				}
			}
		} catch (Exception e) {
			log.error("anlyRoadPark error",e);
		}
	}
	
	private void updateRoadBerth(List<Object> idList, Long ntime){
		try {
			if(idList != null && !idList.isEmpty()){
				String preParams  ="";
				for(Object o : idList){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				List<Object> params = new ArrayList<Object>();
				params.addAll(idList);
				params.add(0);
				List<Map<String, Object>> list = pgOnlyReadService.getAllMap("select sum(total) psum,sum(amount) rsum,comid from remain_berth_tb " +
						" where comid in ("+preParams+") and state=? group by comid ", params);
				String inserSql = "insert into park_anlysis_tb (create_time,comid,share_count,used_count,month_used_count,time_used_count)" +
						" values(?,?,?,?,?,?)";
				List<Object[]> anlyList = new ArrayList<Object[]>();
				if(list != null && !list.isEmpty()){
					for(Map<String, Object> map : list){
						Long comId = (Long)map.get("comid");
						Long psum = Long.valueOf(map.get("psum") + "");//总泊位数
						Long rsum = Long.valueOf(map.get("rsum") + "");//剩余车位数
						Long usum = 0L;//占用的泊位数
						Long msum = 0L;//月卡车占用数
						Long tsum = 0L;//时租车占用数
						if(psum > rsum){
							usum = psum - rsum;
						}
						List<Map<String, Object>> orderList = pgOnlyReadService.getAll("select count(o.id) ccount,o.c_type from order_tb o,com_park_tb p " +
								"where o.id=p.order_id and p.comid=? and p.is_delete=? and p.order_id>? group by o.c_type ", new Object[]{comId, 0, 0});
						if(orderList != null && !orderList.isEmpty()){
							for(Map<String, Object> map2 : orderList){
								Integer c_type = (Integer)map2.get("c_type");
								Long ccount = (Long)map2.get("ccount");
								if(c_type == 5 || c_type == 7 || c_type == 8){
									msum += ccount;
								}
							}
						}
						if(msum > usum){
							msum = usum;
						}
						tsum = usum - msum;
						Object[] values = new Object[]{ntime,comId,psum.intValue(),usum.intValue(),msum.intValue(),tsum.intValue()};
						anlyList.add(values);
					}
					log.error("updateRoadBerth，size:"+anlyList.size());
					if(!anlyList.isEmpty()){
						int ret = dataBaseService.bathInsert(inserSql, anlyList, new int[]{4,4,4,4,4,4});
						log.error("updateRoadBerth，条数:"+ret);
					}
				}
			}
		} catch (Exception e) {
			log.error("updateRoadBerth",e);
		}
	}
	
	private void anlyOnline(Long ntime){
		try {
			String sql1 = "select count(id) ocount,comid,auth_flag from user_info_tb where auth_flag in (?,?,?) and state<>? and online_flag=? and comid>? group by comid,auth_flag ";
			String sql2 = "select count(id) ocount,groupid,auth_flag from user_info_tb where auth_flag in (?,?,?) and state<>? and groupid>? and " +
					" id in (select uid from parkuser_work_record_tb where state=?) group by groupid,auth_flag ";
			String sql3 = "select count(id) ocount,cityid,auth_flag from user_info_tb where auth_flag in (?,?,?) and state<>? and online_flag=? and cityid>? group by cityid,auth_flag ";
			List<Map<String, Object>> list1 = pgOnlyReadService.getAll(sql1, new Object[]{1,2,16,1,22,0});
			List<Map<String, Object>> list2 = pgOnlyReadService.getAll(sql2, new Object[]{1,2,16,1,0,0});
			List<Map<String, Object>> list3 = pgOnlyReadService.getAll(sql3, new Object[]{1,2,16,1,22,0});
			String sql = "insert into online_anlysis_tb(comid,groupid,cityid,collector_online,inspector_online,create_time) values(?,?,?,?,?,?) ";
			if(list1 != null && !list1.isEmpty()){
				List<Map<String, Object>> comList = new ArrayList<Map<String,Object>>();
				List<Object> comidList = new ArrayList<Object>();
				List<Object[]> anlyList = new ArrayList<Object[]>();
				for(Map<String, Object> map : list1){
					Long comid = (Long)map.get("comid");
					Long auth_flag = (Long)map.get("auth_flag");
					Long ocount = (Long)map.get("ocount");
					if(comidList.contains(comid)){
						for(Map<String, Object> map2 : comList){
							Long id = (Long)map2.get("comid");
							if(comid.intValue() == id.intValue()){
								Long collector_online = (Long)map2.get("collector_online");
								Long inspector_online = (Long)map2.get("inspector_online");
								if(auth_flag == 1 || auth_flag == 2){//收费员
									map2.put("collector_online", collector_online + ocount);
								}else if(auth_flag == 16){//巡查员
									map2.put("inspector_online", inspector_online + ocount);
								}
							}
						}
					}else{
						Map<String, Object> map2 = new HashMap<String, Object>();
						map2.put("comid", comid);
						map2.put("collector_online", 0L);
						map2.put("inspector_online", 0L);
						if(auth_flag == 1 || auth_flag == 2){//收费员
							map2.put("collector_online", ocount);
						}else if(auth_flag == 16){//巡查员
							map2.put("inspector_online", ocount);
						}
						comList.add(map2);
						comidList.add(comid);
					}
					
				}
				
				for(Map<String, Object> map : comList){
					Long comid = (Long)map.get("comid");
					Long collector_online = (Long)map.get("collector_online");
					Long inspector_online = (Long)map.get("inspector_online");
					Object[] values2 = new Object[]{comid,-1L,-1L,collector_online,inspector_online,ntime};
					anlyList.add(values2);
				}
				
				if(!anlyList.isEmpty()){
					int ret = dataBaseService.bathInsert(sql, anlyList, new int[]{4,4,4,4,4,4});
					log.error("车场数据，ret："+ret);
				}
			}
			
			if(list2 != null && !list2.isEmpty()){
				List<Map<String, Object>> groupList = new ArrayList<Map<String,Object>>();
				List<Object> groupidList = new ArrayList<Object>();
				List<Object[]> anlyList = new ArrayList<Object[]>();
				for(Map<String, Object> map : list2){
					Long groupid = (Long)map.get("groupid");
					Long auth_flag = (Long)map.get("auth_flag");
					Long ocount = (Long)map.get("ocount");
					if(groupidList.contains(groupid)){
						for(Map<String, Object> map2 : groupList){
							Long id = (Long)map2.get("groupid");
							if(groupid.intValue() == id.intValue()){
								Long collector_online = (Long)map2.get("collector_online");
								Long inspector_online = (Long)map2.get("inspector_online");
								if(auth_flag == 1 || auth_flag == 2){//收费员
									map2.put("collector_online", collector_online + ocount);
								}else if(auth_flag == 16){//巡查员
									map2.put("inspector_online", inspector_online + ocount);
								}
							}
						}
					}else{
						Map<String, Object> map2 = new HashMap<String, Object>();
						map2.put("groupid", groupid);
						map2.put("collector_online", 0L);
						map2.put("inspector_online", 0L);
						if(auth_flag == 1 || auth_flag == 2){//收费员
							map2.put("collector_online", ocount);
						}else if(auth_flag == 16){//巡查员
							map2.put("inspector_online", ocount);
						}
						groupList.add(map2);
						groupidList.add(groupid);
					}
					
				}
				
				for(Map<String, Object> map : groupList){
					Long groupid = (Long)map.get("groupid");
					Long collector_online = (Long)map.get("collector_online");
					Long inspector_online = (Long)map.get("inspector_online");
					Object[] values2 = new Object[]{-1L,groupid,-1L,collector_online,inspector_online,ntime};
					anlyList.add(values2);
				}
				
				if(!anlyList.isEmpty()){
					int ret = dataBaseService.bathInsert(sql, anlyList, new int[]{4,4,4,4,4,4});
					log.error("车场数据，ret："+ret);
				}
			}
			
			if(list3 != null && !list3.isEmpty()){
				List<Map<String, Object>> cityList = new ArrayList<Map<String,Object>>();
				List<Object> cityidList = new ArrayList<Object>();
				List<Object[]> anlyList = new ArrayList<Object[]>();
				for(Map<String, Object> map : list3){
					Long cityid = (Long)map.get("cityid");
					Long auth_flag = (Long)map.get("auth_flag");
					Long ocount = (Long)map.get("ocount");
					if(cityidList.contains(cityid)){
						for(Map<String, Object> map2 : cityList){
							Long id = (Long)map2.get("cityid");
							if(cityid.intValue() == id.intValue()){
								Long collector_online = (Long)map2.get("collector_online");
								Long inspector_online = (Long)map2.get("inspector_online");
								if(auth_flag == 1 || auth_flag == 2){//收费员
									map2.put("collector_online", collector_online + ocount);
								}else if(auth_flag == 16){//巡查员
									map2.put("inspector_online", inspector_online + ocount);
								}
							}
						}
					}else{
						Map<String, Object> map2 = new HashMap<String, Object>();
						map2.put("cityid", cityid);
						map2.put("collector_online", 0L);
						map2.put("inspector_online", 0L);
						if(auth_flag == 1 || auth_flag == 2){//收费员
							map2.put("collector_online", ocount);
						}else if(auth_flag == 16){//巡查员
							map2.put("inspector_online", ocount);
						}
						cityList.add(map2);
						cityidList.add(cityid);
					}
					
				}
				
				for(Map<String, Object> map : cityList){
					Long cityid = (Long)map.get("cityid");
					Long collector_online = (Long)map.get("collector_online");
					Long inspector_online = (Long)map.get("inspector_online");
					Object[] values2 = new Object[]{-1L,-1L,cityid,collector_online,inspector_online,ntime};
					anlyList.add(values2);
				}
				
				if(!anlyList.isEmpty()){
					int ret = dataBaseService.bathInsert(sql, anlyList, new int[]{4,4,4,4,4,4});
					log.error("车场数据，ret："+ret);
				}
			}
		} catch (Exception e) {
			log.error("anlyOnline", e);
		}
	}
}
