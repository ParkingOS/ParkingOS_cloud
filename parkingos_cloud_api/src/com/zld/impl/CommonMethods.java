package com.zld.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;
import com.zld.CustomDefind;
import com.zld.pojo.Berth;
import com.zld.pojo.Car;
import com.zld.pojo.Card;
import com.zld.pojo.CardCarNumber;
import com.zld.pojo.Group;
import com.zld.pojo.Induce;
import com.zld.pojo.Order;
import com.zld.pojo.Sensor;
import com.zld.pojo.Site;
import com.zld.pojo.Tenant;
import com.zld.pojo.WorkRecord;
import com.zld.pojo.WorkTime;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.HttpProxy;
import com.zld.utils.SqlInfo;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
@Repository
public class CommonMethods {
	
	private Logger logger = Logger.getLogger(CommonMethods.class);
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private PgOnlyReadService pService;
	
	private static Map<String, Map<String, Object>> slowReqMap = 
			new ConcurrentHashMap<String, Map<String, Object>>();
	/**
	 * 检查签入签退是否在正常上班时间内
	 * @param role_id 角色
	 * @param time	签入签退时间
	 * @param type	0：签入 1：签退
	 * @return
	 */
	public boolean checkWorkTime(Long uin, long time){
		try {
			if(uin != null && uin > 0 && time > 0){
				Map<String, Object> userMap = pService.getMap("select role_id from user_info_tb where id=? and role_id>? ",
						new Object[]{uin, 0});
				if(userMap != null){
					Long role_id = (Long)userMap.get("role_id");
					long offsetTime = time - TimeTools.getToDayBeginTime();
					logger.error("offsetTime:"+offsetTime);
					List<WorkTime> workTimes = pService.getPOJOList("select * from work_time_tb " +
							" where role_id=? and is_delete=? ", new Object[]{role_id, 0}, WorkTime.class);
					if(workTimes != null && !workTimes.isEmpty()){
						for(WorkTime workTime : workTimes){
							int b_hour = workTime.getB_hour();
							int b_minute = workTime.getB_minute();
							int e_hour = workTime.getE_hour();
							int e_minute = workTime.getE_minute();
							int start = b_hour * 60 * 60 + b_minute * 60;
							int end = e_hour * 60 * 60 + e_minute * 60;
							if(offsetTime > start && offsetTime < end){//上班期间签入签出都算异常
								return false;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("检查上班状态异常", e);
		}
		return true;
	}
	
	/**
	 * 检查集团所属的城市商户是否设置了跨集团追缴
	 * @param groupId
	 * @return
	 */
	public boolean pursueInCity(Long groupId){
		try {
			if(groupId != null && groupId > 0){
				Group group = pService.getPOJO("select cityid from org_group_tb where id=? and cityid>? ",
						new Object[]{groupId, 0}, Group.class);
				if(group != null){
					Tenant tenant = pService.getPOJO("select is_group_pursue from org_city_merchants " +
							" where state=? and id=? ", new Object[]{0, group.getCityid()}, Tenant.class);
					if(tenant != null && tenant.getIs_group_pursue() == 1){
						logger.error("该集团所属的城市商户设置了可以跨集团追缴,groupid:"+groupId);
						return true;
					}
				}
			}
		} catch (Exception e) {
			logger.error("错误", e);
		}
		return false;
	}
	
	/**
	 * 检查集团所属的城市商户是否设置了同一车牌可否在城市内重复入场
	 * @param groupId
	 * @return
	 */
	public boolean isInparkInCity(Long groupId){
		try {
			if(groupId != null && groupId > 0){
				Group group = pService.getPOJO("select cityid from org_group_tb where id=? and cityid>? ",
						new Object[]{groupId, 0}, Group.class);
				if(group != null){
					Tenant tenant = pService.getPOJO("select is_inpark_incity from org_city_merchants " +
							" where state=? and id=? ", new Object[]{0, group.getCityid()}, Tenant.class);
					if(tenant != null && tenant.getIs_inpark_incity() == 0){
						logger.error("该集团所属的城市商户设置了可以同一车牌可否在城市内重复入场,groupid:"+groupId);
						return false;
					}
				}
			}
		} catch (Exception e) {
			logger.error("错误", e);
		}
		return true;
	}
	
	/**
	 * 根据车牌号判别是否是卡片用户
	 * @param carNumber
	 * @param groupId 运营集团编号
	 * @return
	 */
	public boolean cardUser(String carNumber, long groupId){
		try {
			if(carNumber == null || "".equals(carNumber) || groupId <= 0){
				return false;
			}
			Car car = pService.getPOJO("select uin from car_info_tb where car_number=? " +
					" and state=? and uin>? ", new Object[]{carNumber, 1, 0}, Car.class);
			if(car != null){
				long userId = car.getUin();
				Long count = pService.getLong("select count(id) from com_nfc_tb where group_id=? " +
						" and state=? and uin=? and is_delete=? and type=? ",
						new Object[]{groupId, 2, userId, 0, 2});
				if(count > 0){
					return true;
				}
			}
			List<CardCarNumber> cardCarNumbers = pService.getPOJOList("select card_id from " +
					" card_carnumber_tb where is_delete=? and car_number=? ", 
					new Object[]{0, carNumber}, CardCarNumber.class);
			if(cardCarNumbers != null && !cardCarNumbers.isEmpty()){
				List<Object> params = new ArrayList<Object>();
				String preParam = "";
				for(CardCarNumber c : cardCarNumbers){
					params.add(c.getCard_id());
					if("".equals(preParam)){
						preParam = "?";
					}else{
						preParam = ",?";
					}
				}
				params.add(groupId);
				params.add(4);
				params.add(0);
				params.add(2);
				Long count = pService.getCount("select count(id) from com_nfc_tb where id in " +
						" ("+preParam+") and group_id=? and state=? and is_delete=? and type=? ", params);
				if(count > 0){
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 监控慢的接口
	 */
	public void requestInitialized(HttpServletRequest request){
		try {
			request.setAttribute("reqInitTime", System.currentTimeMillis()/1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 监控慢的接口
	 */
	public void requestDestroyed(HttpServletRequest request){
		try {
			String url = request.getServletPath();
			String action = request.getParameter("action");
			url = (action != null) ? (url + "?action=" +action ) : url;
			if(url.contains("collectorrequest.do")){//只检测收费员版
				String localAddr = request.getLocalAddr();
				if(request.getAttribute("reqInitTime") == null){
					return;
				}
				long reqInitTime = (Long)request.getAttribute("reqInitTime");
				long reqDestTime = System.currentTimeMillis()/1000;
				long lifeTime = reqDestTime - reqInitTime;
				if(lifeTime > 5&&!"uporderpic".equals(action)){//接口花费5秒,过滤掉uporderpic
					if(slowReqMap.get(url) == null){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("lastTime", reqDestTime);
						map.put("counter", 0);
						slowReqMap.put(url, map);
					}else{
						Map<String, Object> map = slowReqMap.get(url);
						int counter = (Integer)map.get("counter");
						long lastTime = (Long)map.get("lastTime");
						counter ++;
						map.put("counter", counter);
						if(counter >= 10){
							slowReqMap.remove(url);
							if(reqDestTime - lastTime <= 30 * 60){
								alertSlowInterface(url, reqInitTime, reqDestTime, localAddr);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void alertSlowInterface(String url, long startTime, long endTime, String localAddr){
		try {
			if(memcacheUtils.addLock(url, 1 * 60 * 60)){//一个接口1小时只发一次
				publicMethods.sendCardMessage("18201517240", "接口：" + url + "过慢");
				publicMethods.sendCardMessage("18101333937", "接口：" + url + "过慢");
				publicMethods.sendCardMessage("17701081721", "接口：" + url + "过慢");
				int r = daService.update("insert into slow_alert_tb(url,start_time,end_time," +
						"local_host) values(?,?,?,?)", new Object[]{url, startTime, endTime, localAddr});
				logger.error("alertSlowInterface,r:"+r);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取一个工作记录内的进出车数量
	 * @return
	 */
	public int getInOutCar(long workId, int type){
		try {
			if(workId > 0){
				String key = null;
				if(type == 0){
					key = "pos_in_car_number_" + workId; 
				}else if(type == 1){
					key = "pos_out_car_number_" + workId; 
				}
				if(key != null){
					String number = memcacheUtils.getCache(key);
					if(number != null){
						return Integer.valueOf(number);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * 计算一个工作时间内的进出场车辆
	 * @param workId 工作记录编号
	 * @param type 0: 进车 1：出车
	 */
	public boolean updateInOutCar(long workId, int type){
		try {
			if(workId > 0){
				String key = null;
				if(type == 0){
					key = "pos_in_car_number_" + workId; 
				}else if(type == 1){
					key = "pos_out_car_number_" + workId; 
				}
				if(key != null){
					int num = 0;
					String number = memcacheUtils.getCache(key);
					if(number != null){
						num = Integer.valueOf(number);
					}
					num ++;//累加
					return memcacheUtils.setCache(key, num + "", 24 * 60 * 60);//缓存一天，一天后过期
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 获取泊位
	 * @param berthId 泊位编号
	 * @return
	 */
	public Berth berth(Long berthId){
		try {
			Berth berth = pService.getPOJO("select * from com_park_tb where id=?" +
					" and is_delete=? ", new Object[]{berthId, 0}, Berth.class);
			return berth;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 根据卡片内置的卡号，获取卡片信息
	 * @param nfc_uuid
	 * @param groupId
	 * @return
	 */
	public Card card(String nfc_uuid, Long groupId){
		try {
			Card card = daService.getPOJO("select * from com_nfc_tb where nfc_uuid like ?" +
					" and is_delete=? and type=? and state<>? and group_id=? limit ? ", 
					new Object[]{nfc_uuid + "%", 0, 2, 1, groupId, 1}, Card.class);
			//卡片查主库吧，防止脏读
			return card;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 写这个方法的原因是：客户端调用卡片的接口时，由于逻辑的问题，有时候是传的完整的卡号，
	 * 有的时候是传的卡片内置的不完整的卡号，生成的锁也不一样，会产生并发的问题，所以，
	 * 在此如果传的是完整的卡号就截取卡片的内置编号生成锁，现在的卡号有两种，一种是8位的，
	 * 一种是14位的
	 * @param nfc_uuid
	 * @return
	 */
	public String getNFCLock(String nfc_uuid){
		String lock = null;
		try {
			if(nfc_uuid != null && !"".equals(nfc_uuid)){
				if(nfc_uuid.length() == 20){
					nfc_uuid = nfc_uuid.substring(0, 14);
					if(nfc_uuid.contains("000000")){
						nfc_uuid = nfc_uuid.substring(0, 8);
					}
				}
				lock = getLock(nfc_uuid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lock;
	}
	
	/**
	 * 获取锁名称
	 * @param key
	 * @return
	 */
	public String getLock(Object key){
		String lock = null;
		try {
			String className = Thread.currentThread().getStackTrace()[2].getClassName();//获取当前方法的上一级调用者的类名
			String methodName = Thread.currentThread() .getStackTrace()[2].getMethodName();//获取当前方法的上一级调用者的方法名
			lock = className + "-" + methodName + "-" + key;
			return lock;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lock;
	}
	
	/**
	 * 获取收费员当前签到的工作记录
	 * @param parkUserId
	 * @return
	 */
	public WorkRecord getWorkRecord(Long parkUserId){
		try {
			WorkRecord workRecord = pService.getPOJO("select * from parkuser_work_record_tb where " +
					" uid=? and state=? order by id desc limit ? ", 
					new Object[]{parkUserId, 0,  1}, WorkRecord.class);
			return workRecord;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 根据车牌号查询是不是该停车场的月卡会员
	 * @param carNumber
	 * @param comId
	 * @return
	 */
	public boolean isMonthUser(String carNumber, Long comId){
		Integer monthcount = 0;
		Long uin = null;
		Map carMap = pService.getMap("select uin from car_info_Tb where car_number=? and state=? ", new Object[]{carNumber,1});
		if(carMap!=null&&carMap.get("uin")!=null)
			uin = (Long)carMap.get("uin");
		if(uin!=null&&uin>0){
			List rlist= publicMethods.isMonthUser(uin, comId);
			if(rlist!=null&&rlist.size()==2){
				monthcount = Integer.parseInt(rlist.get(1)+"");
				if(monthcount>0)
					return true;
			}
		}
		return false;
	}
	
	/**
	 * 车检器、基站、诱导屏恢复故障
	 * @param type 0:车检器 1:基站 2:诱导屏
	 * @param uuid 设备唯一编号
	 * @param heartBeat 心跳时间
	 */
	public void deviceRecover(int type, String uuid, Long heartBeat){
		try {
			logger.error("type:"+type+",uuid:"+uuid+",heartBeat:"+heartBeat);
			switch (type) {
			case 0://车检器
				Sensor sensor = pService.getPOJO("select id from dici_tb where did=? and " +
						" is_delete=? limit ? ",new Object[]{uuid, 0, 1}, Sensor.class);
				if(sensor != null){
					int r = daService.update("update device_fault_tb set end_time=? " +
							" where sensor_id=? and end_time is null", 
							new Object[]{heartBeat, sensor.getId()});
					logger.error("snesorId:"+sensor.getId()+",r:"+r);
				}
				break;
			case 1://基站
				Site site = pService.getPOJO("select id from sites_tb " +
						" where uuid=? and is_delete=? limit ? ", new Object[]{uuid, 0, 1},
						Site.class);
				if(site != null){
					int r = daService.update("update device_fault_tb set end_time=? " +
							" where site_id=? and end_time is null", 
							new Object[]{heartBeat, site.getId()});
					logger.error("siteId:"+site.getId()+",r:"+r);
				}
				break;
			case 2://诱导屏
				Induce induce = pService.getPOJO("select id from induce_tb where" +
						" did=? and is_delete=? limit ?", new Object[]{uuid, 0, 1}, Induce.class);
				if(induce != null){
					int r = daService.update("update device_fault_tb set end_time=? " +
							" where induce_id=? and end_time is null", 
							new Object[]{heartBeat, induce.getId()});
					logger.error("induceId:"+induce.getId()+",r:"+r);
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	/**
	 * 根据车牌号和车场来确定车辆类型
	 * @param carNumber
	 * @param comId
	 * @return
	 */
	public Integer getCarType(String carNumber, Long comId){
		Integer car_type = 0;
		try {
			Map<String, Object> carNumbertType = daService.getMap("select typeid from car_number_type_tb " +
					" where car_number=? and comid=?", new Object[]{carNumber,comId});
			if(carNumbertType != null && carNumbertType.get("typeid") != null){
				car_type = Integer.parseInt(carNumbertType.get("typeid")+"");
			}
			if(car_type == 0){//取默认车型
				List<Map<String, Object>> allCarTypes = getCarType(comId);
				if(!allCarTypes.isEmpty()){
					car_type = Integer.valueOf(allCarTypes.get(0).get("value_no")+"");
				}
			}
		} catch (Exception e) {
			logger.error("getCarType", e);
		}
		return car_type;
	}
	
	/**
	 * 根据泊位获取当前在岗的收费员
	 * @param berthId	泊位编号
	 * @return
	 */
	public Long getWorkingCollector(Long berthId){
		Long uin = -1L;
		try {
			if(berthId != null && berthId > 0){
				Map<String, Object> berthMap = pService.getMap("select berthsec_id from com_park_tb " +
						" where id=? and is_delete=? limit ?", new Object[]{berthId, 0, 1});
				logger.error("berthId:"+berthId+",berthMap:"+berthMap);
				if(berthMap != null && berthMap.get("berthsec_id") != null){
					Long berthsec_id = (Long)berthMap.get("berthsec_id");
					if(berthsec_id > 0){
						Long count = pService.getLong("select count(id) from work_berthsec_tb " +
								" where berthsec_id=? and state=? and is_delete=? ", new Object[]{berthsec_id, 1, 0});
						logger.error("berthId:"+berthId+",count:"+count);
						if(count > 0){
							Map<String, Object> workRecord = pService.getMap("select uid from parkuser_work_record_tb " +
									" where berthsec_id=? and state=? limit ? ", new Object[]{berthsec_id, 0, 1});
							logger.error("berthId:"+berthId+",workRecord:"+workRecord);
							if(workRecord != null && workRecord.get("uid") != null){
								uin = (Long)workRecord.get("uid");
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("getWorkingCollector", e);
		}
		logger.error("berthId:"+berthId+",uin:"+uin);
		return uin;
	}
	
	/**
	 * 获取收费员的收入情况
	 * @param startTime
	 * @param endTime
	 * @param uin
	 * @return
	 */
	public Map<String, Object> getIncome(Long startTime, Long endTime, Long uin){
		try {
			List<Object> params = new ArrayList<Object>();
			params.add(uin);
			params.add(0);//停车费（非预付
			params.add(1);//预付停车费
			params.add(2);//预付退款（预付超额）
			params.add(3);//预付补缴（预付不足）
			params.add(4);//追缴停车费
			SqlInfo sqlInfo1 = new SqlInfo(" a.uin =? and a.target in (?,?,?,?,?)", params);
			List<Map<String, Object>> list1 = anlysisMoney(1, startTime, endTime, new String[]{"a.target"}, sqlInfo1, null);
			params.clear();
			params.add(uin);
			params.add(4);//车主付停车费（非预付）或者打赏收费员
			params.add(5);//追缴停车费
			params.add(6);//车主预付停车费
			params.add(7);//预付退款（预付超额）
			params.add(8);//预付补缴（预付金额不足）
			SqlInfo sqlInfo2 = new SqlInfo(" a.uin =? and a.target in (?,?,?,?,?)", params);
			List<Map<String, Object>> list2 = anlysisMoney(2, startTime, endTime, new String[]{"a.target"}, sqlInfo2, null);
			params.clear();
			params.add(uin);
			params.add(0);//停车费（非预付）
			params.add(7);//追缴停车费
			params.add(8);//车主预付停车费
			params.add(9);//预付退款（预付超额）
			params.add(10);//预付补缴（预付金额不足）
			SqlInfo sqlInfo3 = new SqlInfo(" a.uid =? and a.source in (?,?,?,?,?)", params);
			List<Map<String, Object>> list3 = anlysisMoney(3, startTime, endTime, new String[]{"a.source"}, sqlInfo3, null);
			params.clear();
			params.add(uin);
			params.add(0);//停车费（非预付）
			params.add(2);//追缴停车费
			params.add(3);//预付停车费
			params.add(4);//预付退款（预付）
			params.add(5);//预付补缴（预付金额不足）
			SqlInfo sqlInfo4 = new SqlInfo(" a.uid =? and a.source in (?,?,?,?,?)", params);
			List<Map<String, Object>> list4 = anlysisMoney(4, startTime, endTime, new String[]{"a.source"}, sqlInfo4, null);
			params.clear();
			params.add(uin);
			params.add(0);//停车费（非预付）
			params.add(2);//追缴停车费
			params.add(3);//预付停车费
			params.add(4);//预付退款（预付）
			params.add(5);//预付补缴（预付金额不足）
			SqlInfo sqlInfo5 = new SqlInfo(" a.uid =? and a.source in (?,?,?,?,?)", params);
			List<Map<String, Object>> list5 = anlysisMoney(5, startTime, endTime, new String[]{"a.source"}, sqlInfo5, null);
			params.clear();
			params.add(uin);
			SqlInfo sqlInfo6 = new SqlInfo(" uid =? ", params);
			List<Map<String, Object>> list6 = anlysisMoney(6, startTime, endTime, new String[]{}, sqlInfo6, null);
			params.clear();
			params.add(uin);
			SqlInfo sqlInfo8 = new SqlInfo(" o.uid =? ", params);
			List<Map<String, Object>> list8 = anlysisMoney(8, startTime, endTime, new String[]{}, sqlInfo8, null);
			params.clear();
			params.add(uin);
			SqlInfo sqlInfo9 = new SqlInfo(" o.uid =? ", params);
			List<Map<String, Object>> list9 = anlysisMoney(9, startTime, endTime, new String[]{}, sqlInfo9, null);
			params.clear();
			params.add(uin);
			SqlInfo sqlInfo10 = new SqlInfo(" o.uid =? ", params);
			List<Map<String, Object>> list10 = anlysisMoney(10, startTime, endTime, new String[]{}, sqlInfo10, null);
			params.clear();
			params.add(uin);
			SqlInfo sqlInfo11 = new SqlInfo(" o.uid =? ", params);
			List<Map<String, Object>> list11 = anlysisMoney(11, startTime, endTime, new String[]{}, sqlInfo11, null);
			params.clear();
			params.add(uin);
			SqlInfo sqlInfo12 = new SqlInfo(" o.uid =? ", params);
			List<Map<String, Object>> list12 = anlysisMoney(12, startTime, endTime, new String[]{}, sqlInfo12, null);
			params.clear();
			params.add(uin);
			SqlInfo sqlInfo14 = new SqlInfo(" o.uid =? ", params);
			List<Map<String, Object>> list14 = anlysisMoney(14, startTime, endTime, new String[]{}, sqlInfo14, null);
			params.clear();
			params.add(uin);
			params.add(4);//charge_type -- 充值方式：4：预支付退款
			params.add(0);//consume_type --消费方式 0：支付停车费（非预付）
			params.add(1);//consume_type --消费方式 1：预付停车费
			params.add(2);//consume_type --消费方式 2：补缴停车费
			params.add(3);//consume_type --消费方式3：追缴停车费
			SqlInfo sqlInfo13 = new SqlInfo(" a.uid =? and (a.charge_type in (?) or a.consume_type in (?,?,?,?)) ", params);
			List<Map<String, Object>> list13 = anlysisMoney(13, startTime, endTime, 
					new String[]{"a.charge_type,a.consume_type"}, sqlInfo13, null);
			Map<String, Object> infoMap = new HashMap<String, Object>();
			infoMap.put("prepay_cash", 0d);//现金预付
			infoMap.put("add_cash", 0d);//现金补缴
			infoMap.put("refund_cash", 0d);//现金退款
			infoMap.put("pursue_cash", 0d);//现金追缴
			infoMap.put("pfee_cash", 0d);//现金停车费（非预付）
			infoMap.put("prepay_epay", 0d);//电子预支付
			infoMap.put("add_epay", 0d);//电子补缴
			infoMap.put("refund_epay", 0d);//电子退款
			infoMap.put("pfee_epay", 0d);//电子停车费（非预付）
			infoMap.put("pursue_epay", 0d);//电子追缴
			infoMap.put("escape", 0d);//逃单未追缴的停车费
			infoMap.put("prepay_escape", 0d);//逃单未追缴的订单已预缴的金额
			infoMap.put("prepay_card", 0d);//刷卡预付
			infoMap.put("add_card", 0d);//刷卡补缴
			infoMap.put("refund_card", 0d);//刷卡退款
			infoMap.put("pursue_card", 0d);//刷卡追缴
			infoMap.put("pfee_card", 0d);//刷卡停车费（非预付）
			mergeIncome(1, list1, infoMap);
			mergeIncome(2, list2, infoMap);
			mergeIncome(3, list3, infoMap);
			mergeIncome(4, list4, infoMap);
			mergeIncome(5, list5, infoMap);
			mergeIncome(6, list6, infoMap);
			mergeIncome(8, list8, infoMap);
			mergeIncome(9, list9, infoMap);
			mergeIncome(10, list10, infoMap);
			mergeIncome(11, list11, infoMap);
			mergeIncome(12, list12, infoMap);
			mergeIncome(13, list13, infoMap);
			mergeIncome(14, list14, infoMap);
			logger.error("startTime:"+startTime+",endTime:"+endTime+",uin:"+uin+",map:"+infoMap);
			return infoMap;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	private Map<String, Object> mergeIncome(int type, List<Map<String, Object>> list, Map<String, Object> infoMap){
		try {
			Double prepay_cash = Double.valueOf(infoMap.get("prepay_cash") + "");//现金预支付
			Double add_cash = Double.valueOf(infoMap.get("add_cash") + "");//现金补缴
			Double refund_cash = Double.valueOf(infoMap.get("refund_cash") + "");//现金退款
			Double pursue_cash = Double.valueOf(infoMap.get("pursue_cash") + "");//现金追缴
			Double pfee_cash = Double.valueOf(infoMap.get("pfee_cash") + "");//现金停车费（非预付）
			Double prepay_epay = Double.valueOf(infoMap.get("prepay_epay") + "");//电子预支付
			Double add_epay = Double.valueOf(infoMap.get("add_epay") + "");//电子补缴
			Double refund_epay = Double.valueOf(infoMap.get("refund_epay") + "");//电子退款
			Double pursue_epay = Double.valueOf(infoMap.get("pursue_epay") + "");//电子追缴
			Double pfee_epay = Double.valueOf(infoMap.get("pfee_epay") + "");//电子停车费（非预付）
			Double escape = Double.valueOf(infoMap.get("escape") + "");//逃单未追缴的停车费
			Double prepay_escape = Double.valueOf(infoMap.get("prepay_escape") + "");//逃单未追缴的订单已预缴的金额
			Double prepay_card = Double.valueOf(infoMap.get("prepay_card") + "");//刷卡预支付
			Double add_card = Double.valueOf(infoMap.get("add_card") + "");//刷卡补缴
			Double refund_card = Double.valueOf(infoMap.get("refund_card") + "");//刷卡退款
			Double pursue_card = Double.valueOf(infoMap.get("pursue_card") + "");//刷卡追缴
			Double pfee_card = Double.valueOf(infoMap.get("pfee_card") + "");//刷卡停车费（非预付）
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					Integer target = null;
					if(map.get("target") != null){
						target = (Integer)map.get("target");
					}else if(map.get("source") != null){
						target = (Integer)map.get("source");
					}
					Double summoney = 0d;
					if(map.get("summoney") != null){
						summoney = Double.valueOf(map.get("summoney") + "");
					}
					switch (type) {
					case 1:
						if(target == 0){//现金停车费（非预付）
							pfee_cash += summoney;
						}else if(target == 1){//预付停车费
							prepay_cash += summoney;
						}else if(target == 2){//预付退款（预付超额）
							refund_cash += summoney;
						}else if(target == 3){//预付补缴（预付不足）
							add_cash += summoney; 
						}else if(target == 4){//追缴停车费
							pursue_cash += summoney;
						}
						break;
					case 2:
						if(target == 4){//车主付停车费（非预付）或者打赏收费员
							pfee_epay += summoney;
						}else if(target == 5){//追缴停车费
							pursue_epay += summoney;
						}else if(target == 6){//预付停车费
							prepay_epay += summoney;
						}else if(target == 7){//预付退款（预付超额）
							refund_epay += summoney;
						}else if(target == 8){//预付补缴（预付不足）
							add_epay += summoney; 
						}
						break;
					case 3:
						if(target == 0){//停车费（非预付）
							pfee_epay += summoney;
						}else if(target == 7){//追缴停车费
							pursue_epay += summoney;
						}else if(target == 8){//预付停车费
							prepay_epay += summoney;
						}else if(target == 9){//预付退款（预付超额）
							refund_epay += summoney;
						}else if(target == 10){//预付补缴（预付不足）
							add_epay += summoney; 
						}
						break;
					case 4:
						if(target == 0){//停车费（非预付）
							pfee_epay += summoney;
						}else if(target == 2){//追缴停车费
							pursue_epay += summoney;
						}else if(target == 3){//预付停车费
							prepay_epay += summoney;
						}else if(target == 4){//预付退款（预付超额）
							refund_epay += summoney;
						}else if(target == 5){//预付补缴（预付不足）
							add_epay += summoney; 
						}
						break;
					case 5:
						if(target == 0){//停车费（非预付）
							pfee_epay += summoney;
						}else if(target == 2){//追缴停车费
							pursue_epay += summoney;
						}else if(target == 3){//预付停车费
							prepay_epay += summoney;
						}else if(target == 4){//预付退款（预付超额）
							refund_epay += summoney;
						}else if(target == 5){//预付补缴（预付不足）
							add_epay += summoney; 
						}
						break;
					case 6:
						escape += summoney;
						break;
					case 8:
					case 9:
					case 10:
					case 11:
					case 12:
					case 14:
						prepay_escape += summoney;
						break;
					case 13:
						Integer charge_type = (Integer)map.get("charge_type");
						Integer consume_type = (Integer)map.get("consume_type");
						if(charge_type == 4){//4：预支付退款
							refund_card += summoney;
						}else if(consume_type == 0){//0：支付停车费（非预付）
							pfee_card += summoney;
						}else if(consume_type == 1){//1：预付停车费
							prepay_card += summoney;
						}else if(consume_type == 2){//2：补缴停车费
							add_card += summoney;
						}else if(consume_type == 3){//3：追缴停车费
							pursue_card += summoney;
						}
						break;
					default:
						break;
					}
				}
			}
			if(prepay_cash < 0) prepay_cash = 0d;
			if(add_cash < 0) add_cash = 0d;
			if(refund_cash < 0) refund_cash = 0d;
			if(pursue_cash < 0) pursue_cash = 0d;
			if(pfee_cash < 0) pfee_cash = 0d;
			if(prepay_epay < 0) prepay_epay = 0d;
			if(add_epay < 0) add_epay = 0d;
			if(refund_epay < 0) refund_epay = 0d;
			if(pursue_epay < 0) pursue_epay = 0d;
			if(pfee_epay < 0) pfee_epay = 0d;
			if(escape < 0) escape = 0d;
			if(prepay_escape < 0) prepay_escape = 0d;
			if(prepay_card < 0) prepay_card = 0d;
			if(add_card < 0) add_card = 0d;
			if(refund_card < 0) refund_card = 0d;
			if(pursue_card < 0) pursue_card = 0d;
			if(pfee_card < 0) pfee_card = 0d;
			infoMap.put("prepay_cash", prepay_cash);
			infoMap.put("add_cash", add_cash);
			infoMap.put("refund_cash", refund_cash);
			infoMap.put("pursue_cash", pursue_cash);
			infoMap.put("pfee_cash", pfee_cash);
			infoMap.put("prepay_epay", prepay_epay);
			infoMap.put("add_epay", add_epay);
			infoMap.put("refund_epay", refund_epay);
			infoMap.put("pursue_epay", pursue_epay);
			infoMap.put("pfee_epay", pfee_epay);
			infoMap.put("escape", escape);
			infoMap.put("prepay_escape", prepay_escape);
			infoMap.put("prepay_card", prepay_card);
			infoMap.put("add_card", add_card);
			infoMap.put("refund_card", refund_card);
			infoMap.put("pursue_card", pursue_card);
			infoMap.put("pfee_card", pfee_card);
			return infoMap;
		} catch (Exception e) {
			logger.error("mergeIncome", e);
		}
		return null;
	}
	
	/**
	 * 统一接口,统计停车费
	 * @param type	1：现金，2：收费员账户电子收费，3：车场账户电子收费，4：运营集团账户电子收费，5：商户账户电子收费，6：查未追缴订单金额，7：查车检器订单金额
	 * @param startTime	开始时间
	 * @param endTime	结束时间
	 * @param groupby	分组查询的字段
	 * @param sqlInfo	其他的限制条件
	 * @param otherMap	非基础限制条件都写在这里
	 * @return
	 */
	public List<Map<String, Object>> anlysisMoney(int type, Long startTime, Long endTime, 
			String[] groupby, SqlInfo sqlInfo, Map<String, Object> otherMap){
		List<Map<String, Object>> result= null;
		try {
			List<Object> params = new ArrayList<Object>();
			String sql = null;
			String condSql = "";
			params.add(startTime);
			params.add(endTime);
			String ogroupSql = groupSql(groupby);//查询分组字段
			String groupSql = "";
			if(!"".equals(ogroupSql)){
				groupSql = " group by " + ogroupSql.substring(1);
			}
			if(sqlInfo!=null){//其他限制条件
				condSql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			switch (type) {
			case 1://查现金
				sql = "select sum(a.amount) summoney "+ogroupSql+" from parkuser_cash_tb a,order_tb o " +
						" where a.orderid=o.id and a.create_time between ? and ? "+condSql + groupSql;
				break;
			case 2://查收费员账户电子收费
				sql = "select sum(a.amount) summoney "+ogroupSql+" from parkuser_account_tb a,order_tb o " +
						" where a.orderid=o.id and a.create_time between ? and ? " + condSql +
						" and a.remark like ? "+ groupSql;
				params.add("停车费%");
				break;
			case 3://查车场账户电子收费
				sql = "select sum(a.amount) summoney "+ogroupSql+" from park_account_tb a,order_tb o " +
						" where a.orderid=o.id and a.create_time between ? and ? "+condSql + groupSql;
				break;
			case 4://查运营集团账户电子收费
				sql = "select sum(a.amount) summoney "+ogroupSql+" from group_account_tb a,order_tb o " +
						" where a.orderid=o.id and a.create_time between ? and ? "+condSql + groupSql;
				break;
			case 5://查商户账户电子收费
				sql = "select sum(a.amount) summoney "+ogroupSql+" from city_account_tb a,order_tb o " +
						" where a.orderid=o.id and a.create_time between ? and ? "+condSql + groupSql;
				break;
			case 6://查未追缴金额
				sql = "select sum(total) summoney "+ogroupSql+" from no_payment_tb where end_time " +
						" between ? and ? "+condSql+" and state=? "+groupSql;
				params.add(0);
				break;
			case 7://查车检器订单金额
				sql = "select sum(total) summoney "+ogroupSql+" from berth_order_tb where out_time" +
						" between ? and ? "+condSql + " " + groupSql;
				break;
			case 8://查逃单但未追缴的订单现金预付的金额
				sql = "select sum(a.amount) summoney "+ogroupSql+" from no_payment_tb o,parkuser_cash_tb a" +
						" where o.order_id=a.orderid and o.end_time between ? and ? " + condSql + 
						" and o.state=? and a.target=? " + groupSql;
				params.add(0);//未追缴
				params.add(1);//预付停车费
				break;
			case 9://查逃单但未追缴的订单电子预付的金额
				sql = "select sum(a.amount) summoney "+ogroupSql+" from no_payment_tb o,parkuser_account_tb a" +
						" where o.order_id=a.orderid and o.end_time between ? and ? " + condSql + 
						" and o.state=? and a.target=? " + groupSql;
				params.add(0);//未追缴
				params.add(6);//预付停车费
				break;
			case 10://查逃单但未追缴的订单电子预付的金额
				sql = "select sum(a.amount) summoney "+ogroupSql+" from no_payment_tb o,park_account_tb a" +
						" where o.order_id=a.orderid and o.end_time between ? and ? " + condSql +
						" and o.state=? and a.source=? " + groupSql;
				params.add(0);//未追缴
				params.add(8);//预付停车费
				break;
			case 11://查逃单但未追缴的订单电子预付的金额
				sql = "select sum(a.amount) summoney "+ogroupSql+" from no_payment_tb o,group_account_tb a" +
						" where o.order_id=a.orderid and o.end_time between ? and ? " + condSql +
						" and o.state=? and a.source=? " + groupSql;
				params.add(0);//未追缴
				params.add(3);//预付停车费
				break;
			case 12://查逃单但未追缴的订单电子预付的金额
				sql = "select sum(a.amount) summoney "+ogroupSql+" from no_payment_tb o,city_account_tb a" +
						" where o.order_id=a.orderid and o.end_time between ? and ? " + condSql +
						" and o.state=? and a.source=? " + groupSql;
				params.add(0);//未追缴
				params.add(3);//预付停车费
				break;
			case 13://查询刷卡金额
				sql = "select sum(a.amount) summoney "+ogroupSql+" from card_account_tb a,order_tb o " +
						" where a.orderid=o.id and a.create_time between ? and ? "+condSql + groupSql;
				break;
			case 14://查逃单但未追缴的刷卡预付的金额
				sql = "select sum(a.amount) summoney "+ogroupSql+" from no_payment_tb o,card_account_tb a" +
						" where o.order_id=a.orderid and o.end_time between ? and ? " + condSql + 
						" and o.state=? and a.consume_type=? " + groupSql;
				params.add(0);//未追缴
				params.add(1);//预付停车费
				break;
			default:
				break;
			}
			if(sql != null){
				result = pService.getAllMap(sql, params);
			}
		} catch (Exception e) {
			logger.error("anlysisMoney", e);
		}
		return result;
	}
	/**
	 * 拼接分组字段sql
	 * @param groupMap
	 * @return
	 */
	private String groupSql(String[] groupby){
		String groupSql = "";//分组字段
		try {
			if(groupby != null && groupby.length > 0){
				for(int i = 0; i < groupby.length; i++){
					groupSql += "," + groupby[i];
				}
			}
		} catch (Exception e) {
			logger.error("groupSql", e);
		}
		return groupSql;
	}
	
	/**
	 * 置为逃单
	 * @param orderid	POS机订单编号
	 * @param uid	置为逃单的收费员编号
	 * @param money	总金额
	 * @param brethorderid	车间器订单编号
	 * @return
	 */
	public boolean escape(Long orderid, Long uid, Double money, Long endtime){
		logger.error("orderid:"+orderid+",uid:"+uid+",money:"+money+",endtime:"+endtime);
		long comId = -1;
		long workId = -1L;//工作记录编号
		boolean result = false;//订单记录生成结果
		String lock = null;
		try {
			//----------------------------分布式锁--------------------------------//
			lock = getLock(orderid);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				return false;
			}
			Long curTime = System.currentTimeMillis()/1000;
			Order order = daService.getPOJO("select * from order_tb where id=? and state=? ",
					new Object[]{orderid, 0}, Order.class);
			if(order != null){
				logger.error("order:" + order);
				if(endtime == null || endtime <= 0){
					endtime = curTime;
				}
				if(order.getPrepaid() >= money){
					logger.error("预付大于等于停车金额,不能置为逃单");
					return false;
				}
				if(order.getState() == 2){
					logger.error("已经置为逃单");
					return false;
				}
				//------------------------绑定的车检器订单时间----------------------------//
				Long brethorderid = getBerthOrderId(orderid);
				Long end_time = getSensorTime(brethorderid, 1, uid, endtime);
				logger.error("brethorderid:"+brethorderid+",end_time:"+end_time);
				//------------------------获取订单参数----------------------------//
				comId = order.getComid();
				Long berthId = order.getBerthnumber();
				Long berthSegId = order.getBerthsec_id();
				Long groupId = order.getGroupid();
				Double prepay = order.getPrepaid();
				Long create_time = order.getCreate_time();
				String car_number = order.getCar_number();
				Long uin = order.getUin();
				//------------------------获取工作记录----------------------------//
				if(uid != null && uid > 0){//有的订单是自动置为逃单的，可能没有出场收费员
					WorkRecord workRecord = getWorkRecord(uid);
					if(workRecord != null){
						workId = workRecord.getId();
					}
				}
				logger.error("workId:"+workId);
				//------------------------具体逻辑----------------------------//
				List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
				//更新订单状态，收费成功
				Map<String, Object> escapeSqlMap = new HashMap<String, Object>();
				Map<String, Object> berthSqlMap = new HashMap<String, Object>();
				Map<String, Object> orderSqlMap = new HashMap<String, Object>();
				escapeSqlMap.put("sql", "insert into no_payment_tb (create_time,end_time,order_id,total,car_number," +
						"uin,comid,uid,berthseg_id,berth_id,groupid,prepay) values(?,?,?,?,?,?,?,?,?,?,?,?)");
				escapeSqlMap.put("values", new Object[]{create_time, end_time, orderid, money, car_number, uin,
						comId, uid, berthSegId, berthId, groupId, prepay});
				bathSql.add(escapeSqlMap);
				if(berthId != null && berthId >0){
					berthSqlMap.put("sql", "update com_park_tb set state=?,order_id=? where id =? and order_id=? ");
					berthSqlMap.put("values", new Object[]{0, null, berthId, orderid});
					bathSql.add(berthSqlMap);
				}
				orderSqlMap.put("sql", "update order_tb set state=?,total=?,end_time=?,out_uid=? where id =?");
				orderSqlMap.put("values", new Object[]{2, money, end_time, uid, orderid});
				bathSql.add(orderSqlMap);
				result = daService.bathUpdate2(bathSql);
				logger.error("orderid:"+orderid+",uid:"+uid+",money:"+money+"(update com_park_tb orderid) bathsql result:"+result);
				return result;
			}
		} catch (Exception e) {
			logger.error("escapeorderid:"+orderid, e);
		} finally {
			boolean b = memcacheUtils.delLock(lock);
			logger.error("b:"+b);
			if(result && workId > 0){//订单结算成功，更新出车数量
				logger.error("workId:"+workId);
				boolean b1 = updateInOutCar(workId, 1);
				logger.error("更新出场车辆,b1:"+b1);
			}
			updateRemainBerth(comId, 1);
		}
		return false;
	}
	
	/**
	 * 获取订单结束时间
	 * @param orderId 订单编号
	 * @param type 0：进场 1：出场
	 * @param uid 收费员编号
	 * @param curtime 当前系统时间
	 * @return
	 */
	public Long getOrderEndTime(Long brethOrderId, Long uid, Long curtime){
		Long sensortime = curtime;
		try {
			sensortime = getSensorTime(brethOrderId, 1, uid, curtime);
		} catch (Exception e) {
			logger.error("getOrderTime", e);
		}
		return sensortime;
	}
	
	/**
	 * 获取订单开始时间
	 * @param orderId 订单编号
	 * @param type 0：进场 1：出场
	 * @param uid 收费员编号
	 * @param curtime 当前系统时间
	 * @return
	 */
	public Long getOrderStartTime(Long brethOrderId, Long uid, Long curtime){
		Long sensortime = curtime;
		try {
			sensortime = getSensorTime(brethOrderId, 0, uid, curtime);
		} catch (Exception e) {
			logger.error("getOrderTime", e);
		}
		return sensortime;
	}
	
	/**
	 * 获取车检器进出场作为POS机订单出入场时间
	 * @param berthOrderId 车检器订单编号
	 * @param type 0：进场 1：出场
	 * @param uid 收费员编号
	 * @param curtime 当前系统时间
	 * @return
	 */
	private Long getSensorTime(Long berthOrderId, Integer type, Long uid, Long curtime){
		logger.error("getSensorTime>>>berthOrderId:"+berthOrderId+",type:"+type+",uid:"+uid+",curtime:"+curtime);
		Long sensortime = curtime;
		try {
			if(uid != null && uid > 0){
				Map<String, Object> setMap = pService.getMap("select is_sensortime from collector_set_tb s,user_info_tb u " +
						" where s.role_id=u.role_id and u.id=? ", new Object[]{uid});
				if(setMap != null){
					Integer is_sensortime = (Integer)setMap.get("is_sensortime");
					logger.error("getSensorTime>>>berthOrderId:"+berthOrderId+",is_sensortime:"+is_sensortime);
					if(is_sensortime == 1){
						return sensortime;
					}
				}
				if(berthOrderId != null && berthOrderId > 0){
					Map<String, Object> map = pService.getMap("select * from berth_order_tb " +
							" where id=? ", new Object[]{berthOrderId});
					logger.error("getSensorTime>>>berthOrderId:"+berthOrderId+",map:"+map);
					if(map != null){
						if(type == 0){
							if(map.get("in_time") != null){
								Long in_time = (Long)map.get("in_time");
								if(curtime > in_time && (curtime - in_time < 10 * 60)){
									sensortime = in_time;
								}else{
									logger.error("berthOrderId:"+berthOrderId+",uid:"+uid+",curtime:"+curtime+",in_time:"+in_time);
								}
							}
						}else if(type == 1){
							if(map.get("out_time") != null){
								Long out_time = (Long)map.get("out_time");
								if(curtime > out_time && (curtime - out_time < 10 * 60)){
									sensortime = out_time;
								}else{
									logger.error("berthOrderId:"+berthOrderId+",uid:"+uid+",curtime:"+curtime+",out_time:"+out_time);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("getSensorTime>>>berthOrderId:"+berthOrderId,e);
		}
		return sensortime;
	}
	
	/**
	 * 根据POS机订单获取车检器订单编号，如果查到有绑定车检器订单就取绑定的车检器订单编号
	 * @param orderId	POS机订单编号
	 * @param berthOrderId	车检器订单编号
	 * @return
	 */
	public Long getBerthOrderId(Long orderId){
		Long berthOrderId = -1L;
		try {
			//logger.error("getBerthOrderId>>>orderId:"+orderId);
			if(orderId != null && orderId > 0){
				Map<String, Object> berthOrderMap = pService.getMap("select id from berth_order_tb " +
						" where orderid=? order by in_time desc limit ? ", new Object[]{orderId, 1});
				if(berthOrderMap != null){
					berthOrderId = (Long)berthOrderMap.get("id");
				}
			}
			logger.error("getBerthOrderId>>>orderId:"+orderId+",berthOrderId:"+berthOrderId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return berthOrderId;
	}
	
	/**
	 * 取该泊位最新的一条有效的车检器订单
	 * @param berthId
	 * @return
	 */
	public Long getPreBerthOrderId(Long berthId){
		Long berthOrderId = -1L;
		try {
			if(berthId != null && berthId > 0){
				Long count = pService.getLong("select count(d.id) from com_park_tb p,dici_tb d where p.dici_id=d.id " +
						" and p.id=? and d.state=? and d.is_delete=? ", new Object[]{berthId, 1, 0});
				logger.error("getPreBerthOrderId>>>berthId:"+berthId+",count:"+count);
				if(count > 0){
					Map<String, Object> map = pService.getMap("select id,state,orderid,bind_flag from " +
							" berth_order_tb where id=(select max(id) as maxid from berth_order_tb where dici_id=?) ", 
							new Object[]{berthId});
					logger.error("getPreBerthOrderId>>>berthId:"+berthId+",map:"+map);
					if(map != null){
						Integer state = (Integer)map.get("state");
						Integer bind_flag = (Integer)map.get("bind_flag");
						if(state == 0 && bind_flag == 1){
							Long orderid = (Long)map.get("orderid");
							if(orderid < 0){
								berthOrderId = (Long)map.get("id");
							}
						}
					}
				}
			}
			logger.error("getPreBerthOrderId>>>berthId:"+berthId+",berthOrderId:"+berthOrderId);
		} catch (Exception e) {
			logger.error("getPreBerthOrderId", e);
		}
		return berthOrderId;
	}
	
	public void writeToMongodb(String dbName,Map<String, String> paramMap){
		WriteResult result =null;
		try {
			DB mydb = MongoClientFactory.getInstance().getMongoDBBuilder("zld");
			mydb.requestStart();
			DBCollection collection = mydb.getCollection(dbName);
			BasicDBObject object = new BasicDBObject();
			for(String key : paramMap.keySet()){
				object.put(key, paramMap.get(key));
			}
			object.put("ctime", System.currentTimeMillis()/1000);
			mydb.requestStart();
			result = collection.insert(object);
			  //结束事务
			mydb.requestDone();
		} catch (Exception e) {
			logger.error("write to monbodb error...."+result);
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取泊位剩余数
	 * @param comid
	 * @param type 0：入场 1：出场
	 * @return
	 */
	public Long updateRemainBerth(Long comid, Integer type){
		Long remain = 0L;
		try {
			logger.error("remain berth>>>comid:"+comid+",type:"+type);
			Long ntime = System.currentTimeMillis()/1000;
			Map<String, Object> comMap = pService.getMap("select parking_total,share_number,invalid_order from com_info_tb where id=? and parking_type<>? and etc=? ", 
					new Object[]{comid, 2, 2});//parking_type -- 车位类型，0地面，1地下，2占道 3室外 4室内 5室内外
			if(comMap == null){
				return null;
			}
			Integer parking_total = 0;
			Integer share_number = 0;
			Long invalid_order = 0L;//未结算的垃圾订单数
			if(comMap.get("parking_total") != null){
				parking_total = (Integer)comMap.get("parking_total");
			}
			if(comMap.get("share_number") != null){
				share_number = (Integer)comMap.get("share_number");
			}
			if(comMap.get("invalid_order") != null){
				invalid_order = (Long)comMap.get("invalid_order");
			}
			if(share_number == 0){
				share_number = parking_total;
			}
			logger.error("park info>>>comid:"+comid+",parking_total:"+parking_total+",share_number:"+share_number+",invalid_order:"+invalid_order);
			Map<String, Object> remainMap = daService.getMap("select amount from remain_berth_tb where comid=? and state=? and berthseg_id<? limit ?", 
					new Object[]{comid, 0, 0, 1});
			
			if(remainMap != null){
				remain = (Long)remainMap.get("amount");
				if(type == 0){//入场
					remain--;
				}else if(type == 1){
					remain++;
				}
			}else{//封闭车场,初始化余位数
				remain = getValidUseCount(comid, share_number, invalid_order);
			}
			if(remain < 0){
				remain = 0L;
			}
			logger.error("remain berth >>>comid:"+comid+",remain:"+remain);
			if(remainMap == null){
				int ret = daService.update("insert into remain_berth_tb(comid,amount,update_time) values(?,?,?) ", 
						new Object[]{comid, remain, ntime});
				logger.error("update remain berth >>>comid:"+comid+",remain:"+remain+",ret:"+ret);
			}else{
				int ret = daService.update("update remain_berth_tb set amount=?,update_time=? where comid=? and berthseg_id<? ", 
						new Object[]{remain, ntime, comid, 0});
				logger.error("update remain berth >>>comid:"+comid+",remain:"+remain+",ret:"+ret);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return remain;
	}
	
	public Long getValidUseCount(Long comid, Integer share_number, Long invalid_order){
		Long ntime = System.currentTimeMillis()/1000;
		long time2 = ntime - 2*24*60*60;
		long time16 = ntime - 16*24*60*60;
		Long month_used_count = 0L;
		Long time_used_count = 0L;
		String sql = "select count(ID) ucount,c_type from order_tb where comid=? and create_time>? and state=? group by c_type ";
		List<Map<String, Object>> allList = pService.getAll(sql, new Object[]{comid,time2,0});
		if(allList != null && !allList.isEmpty()){
			for(Map<String, Object> map : allList){
				Integer c_type = (Integer)map.get("c_type");
				Long ucount = (Long)map.get("ucount");
				if(c_type == 5 || c_type == 7 || c_type == 8){//月卡车位占用数
					month_used_count += ucount;
				}else{//时租车位占用数
					time_used_count += ucount;
				}
			}
		}
		
		Long invmonth_used_count = 0L;
		Long invtime_used_count = 0L;
		String sql1 = "select count(ID) ucount,c_type from order_tb where comid=? and create_time>? and create_time<? and state=? group by c_type ";
		List<Map<String, Object>> invList = pService.getAll(sql1, new Object[]{comid,time16,time2,0});
		if(invList != null && !invList.isEmpty()){
			for(Map<String, Object> map : invList){
				Integer c_type = (Integer)map.get("c_type");
				Long ucount = (Long)map.get("ucount");
				if(c_type == 5 || c_type == 7 || c_type == 8){//月卡车位占用数
					invmonth_used_count += ucount;
				}else{//时租车位占用数
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
		Long month_offset = Math.round(invalid_order * rate);//月卡车位占用偏移量
		month_used_count = month_used_count - month_offset;//月卡车去掉偏移量后的占用车位数
		time_used_count = time_used_count - (invalid_order - month_offset);//去掉偏移量后的时租车占用车位数
		if(month_used_count < 0){
			month_used_count = 0L;
		}
		if(time_used_count < 0){
			time_used_count = 0L;
		}
		Long useCount = month_used_count + time_used_count;
		if(useCount > share_number){
			share_number = useCount.intValue();
		}
		Long remain = share_number - useCount;
		logger.error("getValidUseCount>>>comid:"+comid+",useCount:"+useCount+",share_number:"+share_number);
		return remain;
	}
	
	/**
	 * @param orderid 订单编号
	 * @param state 支付状态1成功其它不成功
	 * @param money 支付金额
	 * @return 1成功，-1支付不成功 0订单编号不存在
	 */
	public String sendOrderState2Baohe(Long orderid,int state,Double money,Integer paytype){
		HttpProxy hProxy = new HttpProxy();
		//http://www.bouwa.org/api/services/p4/Business/GetOnlinePayStatus?
		String url = "http://www.bouwa.org/api/services/p4/Business/GetOnlinePayStatus?orderid="+orderid+"&status="+state+"&money="+money+"&paytype="+paytype;
		String result ="0";
		try {
			String ret = hProxy.doGet(url);
			if (ret != null) {
				JSONObject object = new JSONObject(ret);
				//{"Success":true,"Result":"1","Error":null,"UnAuthorizedRequest":false,"rows":null,"total":0}
				//Result:1成功，-1支付不成功 0订单编号不存在
				result = object.getString("Result");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
	}
	
	public String sendPrepay2Baohe(Long orderid,int state,Double money,Integer paytype){
		HttpProxy hProxy = new HttpProxy();
		//http://www.bouwa.org/api/services/p4/Business/GetOnlinePayStatus?
		String url = "http://www.bouwa.org/api/services/p4/Business/GetOnLinePrepaidPayStatus?orderid="+orderid+"&status="+state+"&money="+money+"&paytype="+paytype;
		String result ="0";
		try {
			String ret = hProxy.doGet(url);
			System.out.println(ret);
			if (ret != null) {
				JSONObject object = new JSONObject(ret);
				//{"Success":true,"Result":"1","Error":null,"UnAuthorizedRequest":false,"rows":null,"total":0}
				//Result:1成功，-1支付不成功 0订单编号不存在
				result = object.getString("Result");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
	}
	
	public Integer isHd(Long comId) {
		Map comMap = daService.getMap("select order_per from com_info_tb where id =? ", new Object[]{comId});
		Integer per = (Integer)comMap.get("order_per");
		if(per!=null&&per>0){
			Integer i = new Random().nextInt(100);
			if(i<per)
				return 1;
		}
		return 0;
	}
	
	
	//更新车位信息，更新已结算订单的占用车位
//	public void updateParkInfo(Long comId) {
//		int r =daService.update("update com_park_tb set state =?,order_id=? where order_id in " +
//				"(select id from order_tb where state in(?,?) and id in(select order_id from com_park_tb where comid=?)) ",
//				new Object[]{0,null,1,2,comId});
//		logger.error(comId+"，更新了"+r+"条车位信息");
//	}
	
	
	//查询礼包
	public boolean checkBonus(String mobile,Long uin){
		List bList = daService.getAll("select * from bonus_record_tb where mobile=? and state=? ",new Object[]{mobile,0});
		String tsql = "insert into ticket_tb (create_time,limit_day,money,state,uin,type) values(?,?,?,?,?,?) ";
		List<Object[]> values = new ArrayList<Object[]>();
		if(bList!=null&&bList.size()>0){
			Long bid = null;
			for(int i=0;i<bList.size();i++){
				Map map = (Map)bList.get(i);
				Long _bid = (Long)map.get("bid");
				if(_bid!=null&&_bid>0)
					bid = _bid;
				Integer money = (Integer)map.get("amount");
				
				Integer type = (Integer)map.get("type");
				Long ctime = TimeTools.getToDayBeginTime();//(Long)map.get("ctime");
				Long etime = ctime+6*24*60*60-1;
				
				if(type==1){//微信打折券
					values.add(new Object[]{ctime,etime,money,0,uin,2});
				}else {//普通停车券
					if(money==30||money==100){//3张10元券
						if(money==30){
							values.add(new Object[]{ctime,etime,4,0,uin,0});
							values.add(new Object[]{ctime,etime,4,0,uin,0});
							values.add(new Object[]{ctime,etime,1,0,uin,0});
							values.add(new Object[]{ctime,etime,1,0,uin,0});
							values.add(new Object[]{ctime,etime,3,0,uin,0});
							values.add(new Object[]{ctime,etime,3,0,uin,0});
							values.add(new Object[]{ctime,etime,2,0,uin,0});
							values.add(new Object[]{ctime,etime,2,0,uin,0});
							values.add(new Object[]{ctime,etime,4,0,uin,0});
							values.add(new Object[]{ctime,etime,1,0,uin,0});
							values.add(new Object[]{ctime,etime,3,0,uin,0});
							values.add(new Object[]{ctime,etime,2,0,uin,0});
						}else {
							int end = 10;
							for(int j=0;j<end;j++){
								values.add(new Object[]{ctime,etime,10,0,uin,0});
							}
						}
					}else if(money==10){//1张10元券
						values.add(new Object[]{ctime,etime,4,0,uin,0});
						values.add(new Object[]{ctime,etime,1,0,uin,0});
						values.add(new Object[]{ctime,etime,3,0,uin,0});
						values.add(new Object[]{ctime,etime,2,0,uin,0});
					}else {
						Object[] v1 = new Object[]{ctime,etime,money,0,uin,0};
						values.add(v1);
					}
				}
			}
			if(values.size()>0){
				int ret= daService.bathInsert(tsql, values, new int[]{4,4,4,4,4,4});
				logger.error("账户:"+uin+",手机："+mobile+",用户登录 ，写入红包停车券"+ret+"条");
				logger.error(">>>>用户已领完券，更新红包记录："+daService.update("update bonus_record_tb set state=? where mobile=?", new Object[]{1,mobile}));
				if(ret>0){
					//更新车主注册媒体来源 0：车主注册，1-997是订制红包（1今日头条红包（北京），2传单红包,3节日红包.4.今日头条（外地）），998直付红包,999是收费员推荐，1000以上是车主分享订单红包
					if(bid!=null&&bid>0){
						Integer media = 0;
						if(bid>999){//1000以上的编号是车主分享订单红包，其它为订制红包，先写入用户表
							media=1000;
						}else {
							media = bid.intValue();
						}
						if(media>0){//更新媒体来源
							daService.update("update user_info_tb set media=? where id=? ", new Object[]{media,uin});
						}
					}
					return true;
				}
			}
		}else {
			logger.error("账户:"+uin+",手机："+mobile+",没有红包....");
		}
		return false;
	}
	/**
	 * 取可用停车券，未认证车主最多使用3元券。
	 * 	 * 9元的停车费： 也可以使用18元的停车券，但只能抵扣8元。  
		这个8最好是动态的服务器获取，因为有可能压缩补贴，比如优惠券只能抵扣（停车费-2），8就变为7了。
	 * @param uin
	 * @param fee
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getTickets(Long uin,Double fee,Long comId,Long uid){
		//查出所有可用的券
		//Long ntime = System.currentTimeMillis()/1000;
		Integer limit = CustomDefind.getUseMoney(fee,0);
		Double splimit = StringUtils.formatDouble(CustomDefind.getValue("TICKET_LIMIT"));
		boolean blackuser = isBlackUser(uin);
		boolean blackparkuser =false;
		if(comId!=null)
			blackparkuser=publicMethods.isBlackParkUser(comId, false);
		boolean isauth = publicMethods.isAuthUser(uin);
		if(!isauth){
			if(blackuser||blackparkuser){
				if(blackuser){
					logger.error("车主在黑名单内uin:"+uin+",fee:"+fee+",comid:"+comId);
				}
				if(blackparkuser){
					logger.error("车场在黑名单内uin:"+uin+",fee:"+fee+",comid:"+comId);
				}
				return null;
			}
		}else{
			logger.error("车主uin:"+uin+"是认证车主，用券不判断是否是黑名单，车场是否黑名单。");
		}
		List<Map<String, Object>> list = null;
		double ticketquota=-1;
		if(uid!=-1){
			Map usrMap =daService.getMap("select ticketquota from user_info_Tb where id =? and ticketquota<>?", new Object[]{uid,-1});
			if(usrMap!=null){
				ticketquota = Double.parseDouble(usrMap.get("ticketquota")+"");
			}
		}
		logger.error("该收费员:"+uid+"的用券额度是："+ticketquota+"，(-1代表没限制)");
		if(!isauth){//未认证车主最多使用2元券。
			double noAuth = 1;//未认证车主最高试用noAuth(2)元券,以后改动这个值就ok
			if(ticketquota>=0&&ticketquota<=noAuth){
//				ticketquota = ticketquota+1;
			}else{
				ticketquota=noAuth;
			}
			list=	daService.getAll("select * from ticket_tb where uin = ? " +
					"and state=? and limit_day>=? and type<? and money<?  order by limit_day",
					new Object[]{uin,0,TimeTools.getToDayBeginTime(),2,ticketquota+1});

		}else {
			list  = daService.getAll("select * from ticket_tb where uin = ? " +
					"and state=? and limit_day>=? and type<=?  order by limit_day",
					new Object[]{uin,0,TimeTools.getToDayBeginTime(),2});
		}
		logger.error("uin:"+uin+",fee:"+fee+",comid:"+comId+",today:"+TimeTools.getToDayBeginTime());
		if(list!=null&&!list.isEmpty()){
			List<String> _over3day_moneys = new ArrayList<String>();
			int i=0;
			for(Map<String, Object> map : list){
				Integer money = (Integer)map.get("money");
				//Long limit_day = (Long)map.get("limit_day");
				Long tcomid = (Long)map.get("comid");
				Integer type = (Integer)map.get("type");
//				logger.error("ticket>>>uin:"+uin+",comId:"+comId+",tcomid:"+tcomid+",type:"+type+",ticketid:"+map.get("id"));
				if(comId!=null&&comId!=-1&&tcomid!=null&&type == 1){
					if(comId.intValue()!=tcomid.intValue()){
						logger.error(">>>>get ticket:不是这个车场的停车券，不能用....comId:"+comId+",tcomid:"+tcomid+",uin:"+uin);
						i++;
						continue;
					}
				}
				Integer res = (Integer)map.get("resources");
				if(limit==0&&res==0&&type==0){//支付金额小于3元，不先普通券
					i++;					
					continue;
				}
				if(type==1||res==1){
					limit=Double.valueOf((fee-splimit)).intValue();
				}else {
					limit= CustomDefind.getUseMoney(fee,0);
				}
				map.put("isbuy", res);
				if(money==limit){//券值+1元 等于 支付金额时直接返回
					return map;
				}
				//判断 是否 有 不是该车场的专用券
				
				map.remove("comid");
//				map.remove("limit_day");
				_over3day_moneys.add(i+"_"+Math.abs(limit-money));
				i++;
			}
			if(_over3day_moneys.size()>0){//停车券与停车费的绝对值分析 ，取绝对值最小的
				int sk = 0;//保存index
				double sv=0;//保存最小值
				int index = 0;
				for(String s : _over3day_moneys){
					int k = Integer.valueOf(s.split("_")[0]);
					double v = Double.valueOf(s.split("_")[1]);
					if(index==0){
						sk=k;
						sv = v;
					}else {
						if(sv>v){
							sk=k;
							sv = v;
						}
					}
					index++;
				}
				logger.error("uin:"+uin+",comid:"+comId+",sk:"+sk);
				return list.get(sk);
			}
		}else{
			logger.error("未选到券uin:"+uin+",comid:"+comId+",fee:"+fee);
		}
		return null;
	}
	
	/**是否在黑名单*/
	public boolean isBlackUser(Long uin){
		List<Long> blackUserList = memcacheUtils.doListLongCache("zld_black_users", null, null);
		boolean isBlack = true;
		if(blackUserList==null||!blackUserList.contains(uin))//不在黑名单中可以处理推荐返现
			isBlack=false;
		return isBlack;
	}
	
	/**
	 * 根据openid获取用户信息
	 * @param openid
	 * @return
	 */
	public Map<String, Object> getUserByOpenid(String openid){
		Map<String, Object> userMap = daService.getMap("select * from user_info_tb where wxp_openid=? limit ? ",
				new Object[] { openid, 1 });
		return userMap;
	}
	
	/**
	 * 根据openid获取用户的信息
	 * @param openid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getUserinfoByOpenid(String openid){
		Map<String, Object> map = new HashMap<String, Object>();
		Integer bindflag = 0;
		Long uin = -1L;
		String mobile = "";
		Double balance = 0d;
		Map<String, Object> userMap = daService.getMap("select * from user_info_tb where wxp_openid=? limit ? ",
				new Object[] { openid, 1 });
		if(userMap != null){
			bindflag = 1;
			uin = (Long)userMap.get("id");
			mobile = (String)userMap.get("mobile");
			balance = Double.valueOf(userMap.get("balance") + "");
		}else{
			userMap = daService.getMap("select * from wxp_user_tb where openid=? limit ? ", new Object[]{openid, 1});
			if(userMap == null){
				uin = daService.getLong("SELECT nextval('seq_user_info_tb'::REGCLASS) AS newid",null);
				int r = daService.update("insert into wxp_user_tb(openid,create_time,uin) values(?,?,?) ",
								new Object[] { openid, System.currentTimeMillis() / 1000, uin});
				logger.error("没有临时账户，创建一个uin:"+uin+",openid:"+openid+",r:"+r);
			}else{
				uin = (Long)userMap.get("uin");
				balance = Double.valueOf(userMap.get("balance") + "");
			}
		}
		map.put("bindflag", bindflag);
		map.put("uin", uin);
		map.put("mobile", mobile);
		map.put("balance", balance);
		return map;
	}
	
	/**
	 * 获取车主的一个车牌号
	 * @param uin
	 * @param bindflag
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getCarnumber(Long uin, Integer bindflag){
		String carnumber = "";
		if(bindflag == 0){//临时账户
			Map<String, Object> carMap = daService.getMap("select car_number from wxp_user_tb where uin= ? limit ? ", 
					new Object[]{uin, 1});
			if(carMap != null && carMap.get("car_number") != null){
				carnumber = (String)carMap.get("car_number");
			}
		}else if(bindflag == 1){
			Map<String, Object> carMap = daService.getMap("select car_number from car_info_tb where uin=? and state=? limit ?",
					new Object[] { uin, 1, 1 });
			if(carMap != null && carMap.get("car_number") != null){
				carnumber = (String)carMap.get("car_number");
			}
		}
		return carnumber;
	}
	
	/**
	 * 扫减免券，获取减免前后的停车费金额
	 * @param orderMap
	 * @param shopTicketMap
	 * @param delaytime 预支付延时时间
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getPrice(Long orderId, Long shopTicketId, Long end_time){
		try {
			Map<String, Object> orderMap = daService.getMap("select * from order_tb where id=? ", 
					new Object[]{orderId});
			Map<String, Object> shopTicketMap = daService.getMap("select * from ticket_tb where id=? ", 
					new Object[]{shopTicketId});
			Map<String, Object> map = new HashMap<String, Object>();
			Integer state = (Integer)orderMap.get("state");
			Long comid = (Long)orderMap.get("comid");
			Double beforetotal = 0d;//使用减免券之前的停车费金额
			Double aftertotal = 0d;//使用减免券之后的停车费金额
			Double distotal = 0d;//减免券抵扣的金额
			Integer distime = 0;//抵扣的时长
			Integer car_type = (Integer)orderMap.get("car_type");//0：通用，1：小车，2：大车
			Integer pid = (Integer)orderMap.get("pid");
			Long create_time = (Long)orderMap.get("create_time");
			if(state == 0){//未结算订单
				beforetotal = getPrice(car_type, pid, comid, create_time, end_time,orderId);
			}else if(orderMap.get("total") != null){//已结算或者逃单
				beforetotal = Double.valueOf(orderMap.get("total") + "");
			}
			if(shopTicketMap != null){
				Integer type = (Integer)shopTicketMap.get("type");
				if(type == 3){//减时券
					Integer time = (Integer)shopTicketMap.get("money");
					if(end_time > create_time + time *60 *60){
						aftertotal = getPrice(car_type, pid, comid, create_time, end_time - time * 60 *60,orderId);
						distime = time * 60 * 60;
					}else if(end_time > create_time){
						distime = (end_time.intValue() - create_time.intValue());
					}
				}else if(type == 4){//免费券
					if(end_time > create_time){
						distime = (end_time.intValue() - create_time.intValue());
					}
				}
			}else{
				aftertotal = beforetotal;
			}
			
			if(beforetotal > aftertotal){
				distotal = StringUtils.formatDouble(beforetotal - aftertotal);
			} else {//考虑到已结算或者逃单的情况，期间有可能价格变动，导致减免后的价格反而比减免前的还大
				aftertotal = beforetotal;
			}
			List<Map<String , Object>> sqlMaps = new ArrayList<Map<String,Object>>();
			if(shopTicketMap != null){
				Integer need_sync = -1;//默认-1
				if(publicMethods.isEtcPark(comid)){//双机服务器车场线上扫描减免券做标记  0需要同步下去
					need_sync = 0;
				}
				Map<String, Object> ticketsqlMap = new HashMap<String, Object>();
				ticketsqlMap.put("sql", "update ticket_tb set umoney=?,bmoney=?,need_sync=? where id=? ");
				ticketsqlMap.put("values", new Object[]{distotal, Double.valueOf(distime)/(60*60), need_sync, shopTicketId});
				sqlMaps.add(ticketsqlMap);
			}
			boolean b = daService.bathUpdate2(sqlMaps);
			logger.error("orderid:"+orderId+",b:"+b);
			map.put("beforetotal", beforetotal);
			map.put("aftertotal", aftertotal);
			map.put("distotal", distotal);
			return map;
		} catch (Exception e) {
			logger.error("getPrice", e);
		}
		return null;
	}
	
	/**
	 * 根据订单信息获取车费金额
	 * @param car_type
	 * @param pid
	 * @param comid
	 * @param create_time
	 * @param end_time
	 * @return
	 */
	public Double getPrice(Integer car_type, Integer pid, Long comid, Long create_time, Long end_time, Long orderid){
		Double total = 0d;
		if(pid>-1){
			total = Double.valueOf(publicMethods.getCustomPrice(create_time, end_time, pid));
		}else {
			Map orderMap = daService.getMap("select * from order_tb where id = ?", new Object[]{orderid});
			//模拟已结算
			orderMap.put("end_time", end_time);
			orderMap.put("state", 1);
			String result = "";
			try {
				result = publicMethods.getOrderPrice(comid, orderMap, end_time);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//getPrice(create_time, end_time, comid, car_type));
			net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(result);
			if(jsonObject.get("collect")!=null){
				total = StringUtils.formatDouble(jsonObject.get("collect"));
			}
		}
		return total;
	}
	
	/**
	 * 获取订单信息
	 * @param orderId
	 * @param shopTicketId
	 * @param end_time
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getOrderInfo(Long orderId, Long shopTicketId, Long end_time){
		try {
			Map<String, Object> orderMap = daService.getMap("select * from order_tb where id=? ", 
					new Object[]{orderId});
			if(orderMap == null){
				return null;
			}
			Integer state = (Integer)orderMap.get("state");
			if(state == 1 || state == 2){//如果订单已结算或者逃单则使用订单结束时间
				if(orderMap.get("end_time") != null){
					end_time = (Long)orderMap.get("end_time");
				}
			}
			Long create_time = (Long)orderMap.get("create_time");
			Integer tickettype = 3;//减免券类型，默认减时券
			Integer tickettime = 0;//减时券的时长
			Integer ticketstate = 0;//减免券的状态，0：不可用 1:可用
			Map<String, Object> shopTicketMap = daService.getMap("select * from ticket_tb where orderid=? " +
					" and (type=? or type=?) ", new Object[]{orderId, 3, 4});
			if(shopTicketMap == null){//如果没有绑定减免券就自动的检查有没有可用的减免券
				if(shopTicketId != null && shopTicketId > 0){
					shopTicketMap = daService.getMap("select * from ticket_tb where id=? and (orderid=? or orderid=?) " +
							" and state=? and (type=? or type=?) and limit_day>? ", 
							new Object[]{shopTicketId, -1, orderId, 0, 3, 4, end_time});
				}
			}else{
				shopTicketId = (Long)shopTicketMap.get("id");
			}
			if(shopTicketMap != null){
				int r = daService.update("update ticket_tb set orderid=? where id=? ", 
						new Object[]{orderId, shopTicketId});
				tickettype = (Integer)shopTicketMap.get("type");
				tickettime = (Integer)shopTicketMap.get("money");//减免券的面额(XX小时)
				ticketstate = 1;//该减免券可用
				logger.error("orderId:"+orderId+",r:"+r);
			}
			Map<String, Object> map2 = getPrice(orderId, shopTicketId, end_time);
			map2.putAll(orderMap);
			map2.put("ticketstate", ticketstate);
			map2.put("tickettype", tickettype);
			map2.put("tickettime", tickettime);
			map2.put("shopticketid", shopTicketId);
			map2.put("starttime", TimeTools.getTime_yyyyMMdd_HHmm(create_time * 1000));
			map2.put("parktime", StringUtils.getTimeString(create_time, end_time));
			map2.put("pretotal", orderMap.get("prepaid"));
			map2.put("end_time", end_time);//记录当前时间
			return map2;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	/**
	 * 根据用户ID获取是临时账户还是正式账户
	 * @param uin
	 * @return
	 */
	public Integer getBindflag(Long uin){
		Long count = daService.getLong("select count(1) from user_info_tb where id=? and auth_flag=? ", new Object[]{uin,4});
		return count.intValue();
	}
	
	public Integer addCarnumber(Long uin, String carnumber){
		Long curTime = System.currentTimeMillis()/1000;
		Integer bindflag = getBindflag(uin);
		if(bindflag == 1){
			Long count = daService.getLong("select count(*) from car_info_tb where uin!=? and car_number=? ",
					new Object[] { uin, carnumber });
			if(count > 0){//该车牌号已被别人注册
				return -1;
			}
			count = daService.getLong("select count(*) from car_info_tb where uin=? and car_number=? ",
					new Object[] { uin, carnumber});
			if(count > 0){//该车主已经注册过该车牌号
				return -2;
			}else{
				count = daService.getLong("select count(*) from car_info_tb where uin=? ",
						new Object[] { uin });
				if(count >= 3){//该车主注册的车牌号的个数
					return -3;
				}
				int r=daService.update("insert into car_info_Tb (uin,car_number,create_time) values(?,?,?)", 
						new Object[]{uin, carnumber, curTime});
				if(r > 0){
					publicMethods.syncUserCarNumber(uin, carnumber, "");
					return 1;
				}
			}
		}else if(bindflag == 0){
			Long count = daService.getLong("select count(*) from wxp_user_tb where  car_number=? ",
					new Object[] { carnumber});
			if(count > 0){//该车牌号已被别人注册
				return -1;
			}
			int r = daService.update("update wxp_user_tb set car_number=? where uin=? ", 
					new Object[]{carnumber, uin});
			if(r > 0){
				return 1;
			}
		}
		return -4;
	}
	
	public Integer checkProdExp(Long prodid, Long starttime, Integer months){
		Integer result = 0;//购买的月数没有超过产品有效期
		if(prodid != null && prodid> 0){
			Map pMap = daService.getMap("select limitday,price from product_package_tb where id=? ", 
					new Object[]{prodid});
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(starttime*1000);
			calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+months);
			Long etime = calendar.getTimeInMillis()/1000;
					
			Long limitDay = null;//pMap.get("limitday");
			if(pMap!=null&&pMap.get("limitday")!=null){
				limitDay = (Long)pMap.get("limitday");
			}
			if(limitDay!=null){
				if(limitDay<etime){//超出有效期
					result = 1;
				}
			}
		}
		return result;
	}
	
	/**
	 * @param uin          车主账户
	 * @param total        订单金额
	 * @return             可用停车券列表
	 */
	public List<Map<String,Object>> getUseTickets(Long uin,Double total){
		Long time = System.currentTimeMillis()/1000;
		List<Map<String,Object>> ticketList=daService.getAll("select id,limit_day as limitday,money,resources," +
				"comid,type from ticket_tb where uin = ?" +
				" and limit_day >= ? and state=? and type<?  order by type desc,money,limit_day ",
				new Object[]{uin,time,0,2});
		
		Integer limit = CustomDefind.getUseMoney(total, 0);//普通券抵扣金额
		Integer sysLimit = Integer.valueOf(CustomDefind.getValue("TICKET_LIMIT"));//专用券，购买券金额与订单的差额
		if(ticketList!=null&&!ticketList.isEmpty()){
			for(Map<String,Object> map:ticketList){
				Integer money = (Integer)map.get("money");
				Integer res = (Integer)map.get("resources");
				Integer topMoney = CustomDefind.getUseMoney(money.doubleValue(), 1);
				Integer type=(Integer)map.get("type");
				if(res==1||type==1){//车场专用券或购买券
					topMoney = money+sysLimit;
					limit = total.intValue()-sysLimit;
				}else {
					limit = CustomDefind.getUseMoney(total, 0);
					
				}
				if(topMoney<total){//最高限额小于支付金额
					limit=money;
				}
				map.put("limit", limit);
			}
		}
		//logger.error(ticketList);
		return ticketList;
	}
	
	//----------------打折券选券逻辑begin--------------------//
	/**
	 * 选择打折券
	 * @param uin
	 * @param uid
	 * @param total
	 * @return
	 */
	public Map<String, Object> chooseDistotalTicket(Long uin, Long uid, Double total){
		double firstorderquota = 8.0;//默认额度
		double ditotal = 0d;//打折额度
		double disquota = StringUtils.formatDouble(firstorderquota * ditotal);//打五折后的抵扣金额
		
		logger.error("选折扣券uin:"+uin+",uid:"+uid+",disquota:"+disquota+",firstorderquota:"+firstorderquota+",total:"+total);
		Map<String, Object> userMap2 = daService.getMap("select comid,firstorderquota from user_info_tb where id = ? ", new Object[]{uid});
		if(userMap2!=null){
			firstorderquota = Double.valueOf(userMap2.get("firstorderquota") + "");
			disquota = StringUtils.formatDouble(firstorderquota * ditotal);
		}
		logger.error("选折扣券uin:"+uin+",uid:"+uid+",firstorderquota:"+firstorderquota+",disquota:"+disquota);
		Map<String, Object> ticketMap = new HashMap<String, Object>();
		ticketMap.put("id", -100);
		Double ticket_money = Double.valueOf(StringUtils.formatDouble(total*ditotal));
		if(ticket_money > disquota){
			ticket_money =disquota;
		}
		ticketMap.put("money", ticket_money);
		logger.error("uin:"+uin+",total:"+total+",ticketMap:"+ticketMap);
		return ticketMap;
	}
	
	/**
	 * 计算包月产品费用
	 * @param prodId 包月产品编号
	 * @param months 购买月数
	 * @return
	 */
	public Double getProdSum(Long prodId, Integer months){
		Double total = 0d;
		if(prodId != null && prodId > 0 && months != null && months > 0){
			Double price = 0d;
			Map<String, Object> pMap = daService.getMap("select limitday,price from product_package_tb where id=? ", 
					new Object[]{prodId});
			if(pMap!=null&&pMap.get("limitday")!=null){
				price = Double.valueOf(pMap.get("price")+"");
			}
			total = months*price;
		}
		return total;
	}
	//----------------打折券选券逻辑end--------------------//
	
	//----------------代金券选券逻辑begin--------------------//
	/**
	 * 
	 * @param uin
	 * @param total
	 * @param utype 0普通选券（默认）1可用大于最大抵扣金额的停车券
	 * @param uid
	 * @param isAuth
	 * @param ptype 0账户充值；1包月产品；2停车费结算；3直付;4打赏 5购买停车券
	 * @param parkId
	 * @param source 0:来自客户端选券 1：来自公众号选券
	 * @return
	 */
	public List<Map<String, Object>> chooseTicket(Long uin, Double total, Integer utype, Long uid, boolean isAuth, Integer ptype, Long parkId, Long orderId, Integer source){
		List<Map<String, Object>> list = null;
		if(ptype == 4){//打赏选券
			list = chooseRewardTicket(uin, total, isAuth, uid, utype, ptype, parkId, orderId, source);
		}else if(ptype == -1 || ptype == 2 || ptype == 3){
			list = chooseParkingTicket(uin, total, utype, uid, isAuth, ptype, parkId, orderId, source);
		}
		return list;
	}
	
	/**
	 * 停车消费选券
	 * @param uin
	 * @param total 停车费金额
	 * @param utype 0普通选券（默认）1可用大于最大抵扣金额的停车券
	 * @param uid
	 * @param isAuth
	 * @param source 0:来自客户端选券 1：来自公众号选券
	 * @return
	 */
	public List<Map<String, Object>> chooseParkingTicket(Long uin, Double total, Integer utype, Long uid, boolean isAuth, Integer ptype, Long parkId, Long orderId, Integer source){
		List<Map<String, Object>> list = null;
		boolean isCanUserTicket = memcacheUtils.readUseTicketCache(uin);
		logger.error("choose parking pay ticket>>>uin:"+uin+",total:"+total+",utype:"+utype+",uid:"+uid+",isAuth:"+isAuth+",isCanUserTicket:"+isCanUserTicket);
		if(isCanUserTicket){
			Double moneylimit = 9999d;//选券无限制
			Map<String, Object> uidMap =daService.getMap("select ticketquota from user_info_Tb where id =? and ticketquota<>?", new Object[]{uid,-1});
			if(uidMap != null){
				moneylimit = Double.parseDouble(uidMap.get("ticketquota")+"");
			}
			logger.error("uin:"+uin+",uid:"+uid+",moneylimit:"+moneylimit+",isAuth:"+isAuth);
			Integer tickettype = 2;//选券类型
			if(!isAuth){
				if(source == 0){
					moneylimit = 0d;
				}else if(source == 1){
					moneylimit = 0d;
				}
			}
			logger.error("uin:"+uin+",uid:"+uid+",moneylimit:"+moneylimit+",isAuth:"+isAuth+",tickettype:"+tickettype);
			list = getLimitTickets(moneylimit, tickettype, uin, utype, ptype, uid, total, parkId, orderId);
		}
		return list;
	}
	
	/**
	 * 选打赏券
	 * @param uin
	 * @param total
	 * @param isAuth
	 * @param uid
	 * @param utype 0普通选券（默认）1可用大于最大抵扣金额的停车券
	 * @param ptype 0账户充值；1包月产品；2停车费结算；3直付;4打赏 5购买停车券
	 * @param source 0:来自客户端选券 1：来自公众号选券
	 * @return
	 */
	public List<Map<String, Object>> chooseRewardTicket(Long uin, Double total, boolean isAuth, Long uid, Integer utype, Integer ptype, Long parkId, Long orderId, Integer source){
		List<Map<String, Object>> list = null;
		Map<Long, Long> tcacheMap =memcacheUtils.doMapLongLongCache("reward_userticket_cache", null, null);
		boolean isCanUserTicket=true;
		if(tcacheMap!=null){
			Long time = tcacheMap.get(uin);
			if(time!=null&&time.equals(TimeTools.getToDayBeginTime())){
				isCanUserTicket=false;
			}
			logger.error("today reward cache:"+tcacheMap.size()+",uin:"+uin+",uid:"+uid+",time:"+time+",todaybegintime:"+TimeTools.getToDayBeginTime());
		}
		logger.error("choose reward ticket:uin:"+uin+",uid:"+uid+",isCanUserTicket:"+isCanUserTicket+",isAuth:"+isAuth+",total:"+total);
		
		if(isCanUserTicket){
			Double moneylimit = 9999d;//选券无限制
			Integer tickettype = 1;//选券类型
			if(!isAuth){
				if(source == 0){
					moneylimit = 0d;
				}else if(source == 1){
					moneylimit = 0d;
				}
			}
			list = getLimitTickets(moneylimit, tickettype, uin, utype, ptype, uid, total, parkId, orderId);
		}
		return list;
	}
	
	/**
	 * 按照停车券类型限制和停车券金额上限取停车券列表
	 * @param moneylimit 停车券金额上限
	 * @param tickettype 停车券类型限制
	 * @param uin
	 * @param utype 0普通选券（默认）1可用大于最大抵扣金额的停车券
	 * @param ptype 0账户充值；1包月产品；2停车费结算；3直付;4打赏 5购买停车券
	 * @param uid
	 * @param total 金额
	 * @return
	 */
	private List<Map<String, Object>> getLimitTickets(Double moneylimit, Integer tickettype, Long uin, Integer utype, Integer ptype, Long uid, Double total, Long parkId, Long orderId){
		Integer resource = 1;//只能用购买券
		if(readAllowCache(parkId)){
			logger.error("already uplimit of allowance everyday>>>uin:"+uin+",orderid:"+orderId);
			resource = 1;
		}
		List<Map<String, Object>> list = daService.getAll("select * from ticket_tb where uin = ? and state=? and limit_day>=? and type<? and money<=? and resources>=?  order by money ",
				new Object[] { uin, 0, TimeTools.getToDayBeginTime(), tickettype, moneylimit, resource });
		list = chooseTicketByLevel(list, ptype, uid, total, utype, parkId, orderId);
		return list;
	}
	
	private boolean readAllowCache(Long comid){
		Double limit = memcacheUtils.readAllowLimitCacheByPark(comid);
		logger.error("comid:"+comid+",limit:"+limit);
		if(limit != null){//有缓存
			Double allowmoney = memcacheUtils.readAllowCacheByPark(comid);
			logger.error("comid:"+comid+",allowmoney:"+allowmoney);
			Map<String, Object> comMap = daService.getMap(
					"select allowance from com_info_tb where id=? ",
					new Object[] { comid });
			if(comMap != null && comMap.get("allowance") != null){
				Double allowance = Double.valueOf(comMap.get("allowance") + "");
				logger.error("comid:"+comid+",allowance:"+allowance);
				if(allowance > 0){
					if(allowmoney >= allowance){
						return true;
					}
				}
			}
			if(allowmoney >= limit){//查看是否超过每日补贴上限
				return true;
			}
		}else{//没有按车场出单分配的补贴,这时候按照总单量来限制
			Double allallowmoney = memcacheUtils.readAllowanceCache();
			if(CustomDefind.getValue("ALLOWANCE") != null){
				Double uplimit = Double.valueOf(CustomDefind.getValue("ALLOWANCE") + "");
				Double toDaylimit = getAllowance(TimeTools.getToDayBeginTime(), uplimit);
//				if(toDaylimit<1000||toDaylimit>uplimit)
//					toDaylimit=1000d;
				logger.error("今日补贴总额 ：allallowmoney:"+allallowmoney+",uplimit:"+uplimit+",toDaylimit:"+toDaylimit);
				if(allallowmoney >= toDaylimit){//今日补贴总额已经超过了上限
					return true;
				}
			}
		}
		return false;
	}
	
	//2015-11-05 开始，每天减100,到0停止
	private Double getAllowance(Long time,Double limit) {
		Long baseTime = 1446652800L;//2015-11-05
		Long abs = time-baseTime;
		Long t  = abs/(24*60*60);
		logger.error(">>>>>（2015-11-03开始）补贴递减100的倍数："+t);
		if(t>0){
			Double retDouble= limit-t*100;
			if(retDouble<0d)
				retDouble=0d;
			return retDouble;
		}
		return limit;
	}
	
	/**
	 * @param ptype 0账户充值；1包月产品；2停车费结算；3直付;4打赏 5购买停车券
	 * @param uid   收费员编号
	 * @param total 消费金额
	 * @param type  0：根据金额计算券抵扣金额 1：根据券金额计算满多少消费金额可全额抵扣
	 * @param utype 0普通选券（默认）1可用大于最大抵扣金额的停车券
	 * @return
	 */
	private Map<String, Object> getDistotalLimit(Integer ptype,Long uid, Double total, Integer type, Integer utype, Long orderId){
//		logger.error("getDistotalLimit>>>ptype:"+ptype+",uid:"+uid+",total:"+total+",utype:"+utype);
		Map<String, Object> map = new HashMap<String, Object>();
		Double climit = 0d;
		Double blimit = 0d;
		Double slimit = 0d;
		if(ptype == 4){//打赏选券
			Double rewardquota = 3.0;//抵扣上限
			Map<String, Object> userMap = daService.getMap("select rewardquota from user_info_tb where id = ?", new Object[]{uid});
			if(userMap != null && userMap.get("rewardquota") != null){
				rewardquota =StringUtils.formatDouble(userMap.get("rewardquota"));
			}
			if(type == 0){
				if(orderId != null && orderId > 0){
					Map<String, Object> orderMap = daService.getMap("select total from order_tb where id=? ", new Object[]{orderId});
					if(orderMap != null && orderMap.get("total") != null){
						Double fee = Double.valueOf(orderMap.get("total") + "");//停车费金额
						
						//普通券  X：支付车费金额满 (fee) Y：可用券抵扣金额 (climit) 算法：X=6Y-2 上限是rewardquota
						climit = Math.floor((fee+2)*(1.0/6));//向上取整
						if(climit < 0){
							climit = 0d;
						}
						if(climit > total){
							climit = total;
						}
						if(climit > rewardquota){
							climit = rewardquota;
						}
						//购买券   X：支付车费金额满 (fee) Y：可用券抵扣金额 (blimit) 算法：X=Y上限是rewardquota
						blimit = Math.floor(fee);//向上取整
						if(blimit < 0){
							blimit = 0d;
						}
						if(blimit > total){
							blimit = total;
						}
						if(blimit > rewardquota){
							blimit = rewardquota;
						}
						//专用券   X：支付车费金额满 (fee) Y：可用券抵扣金额 (slimit) 算法：X=6Y-2 上限是rewardquota
						slimit = Math.floor((fee+2)*(1.0/6));//向上取整
						if(slimit < 0){
							slimit = 0d;
						}
						if(slimit > total){
							slimit = total;
						}
						if(slimit > rewardquota){
							slimit = rewardquota;
						}
					}
				}
				logger.error("getDistotalLimit>>>uid:"+uid+",climit:"+climit+",blimit:"+blimit+",slimit:"+slimit+",total:"+total+",ptype:"+ptype+",utype:"+utype+",type:"+type);
				
			}else if(type == 1){
				if(total > rewardquota){
					total = rewardquota;
				}
				//普通券  X：支付车费金额满 (climit) Y：可用券抵扣金额 (total) 算法：X=6Y-2 上限是rewardquota
				climit = Math.ceil(total*6 - 2);
				//购买券  X：支付车费金额满 (blimit) Y：可用券抵扣金额 (total) 算法：X=Y 上限是rewardquota
				blimit = Math.ceil(total);
				//专用券  X：支付车费金额满 (slimit) Y：可用券抵扣金额 (total) 算法：X=6Y-2 上限是rewardquota
				slimit = Math.ceil(total*6 - 2);
				
				map.put("distotal", total);//实际最高抵扣金额
//					logger.error("getDistotalLimit>>>uid:"+uid+",climit:"+climit+",blimit:"+blimit+",slimit:"+slimit+",rewardquota:"+rewardquota+",total:"+total+",ptype:"+ptype+",utype:"+utype+",type:"+type+",distotal:"+total);
			}
			
		}else if(ptype == -1 || ptype == 2 || ptype == 3){
			Double uplimit = 9999d;//抵扣上限
			if(type == 0){
				//普通券  X：车费金额满 (total) Y：可用券抵扣金额 (climit) 算法：X=6Y - 2 上限是uplimit
				climit = Math.floor((total + 2)*(1.0/6));//向上取整
				if(climit < 0){
					climit = 0d;
				}
				if(climit > uplimit){
					climit = uplimit;
				}
				//购买券  X：车费金额满 (total) Y：可用券抵扣金额 (climit) 算法：X=Y 上限是uplimit
				blimit = Math.floor(total);//向上取整
				if(blimit < 0){
					blimit = 0d;
				}
				if(blimit > uplimit){
					blimit = uplimit;
				}
				//专用券  X：车费金额满 (total) Y：可用券抵扣金额 (climit) 算法：X=3Y+1 上限是uplimit
				slimit = Math.floor((total - 1)*(1.0/3));//向上取整
				if(slimit < 0){
					slimit = 0d;
				}
				if(slimit > uplimit){
					slimit = uplimit;
				}
				logger.error("getDistotalLimit>>>uid:"+uid+",climit:"+climit+",blimit:"+blimit+",slimit:"+slimit+",uplimit:"+uplimit+",total:"+total+",ptype:"+ptype+",utype:"+utype+",type:"+type);
			}else if(type == 1){
				if(total > uplimit){
					total = uplimit;
				}
				//普通券  X：支付金额满 (climit) Y：可用券抵扣金额 (total) 算法：X=Y+1+Y/1 上限是uplimit
				climit = Math.ceil(total*6 - 2);
				//购买券  X：支付金额满 (blimit) Y：可用券抵扣金额 (total) 算法：X=Y 上限是uplimit
				blimit = Math.ceil(total);
				//专用券  X：支付金额满 (slimit) Y：可用券抵扣金额 (total) 算法：X=Y+1 上限是uplimit
				slimit = Math.ceil(total*3 + 1);
				map.put("distotal", total);//实际最高抵扣金额
//				logger.error("getDistotalLimit>>>uid:"+uid+",climit:"+climit+",blimit:"+blimit+",slimit:"+slimit+",uplimit:"+uplimit+",total:"+total+",ptype:"+ptype+",utype:"+utype+",type:"+type+",distotal:"+total);
			}
			
		}
		map.put("climit", climit);
		map.put("blimit", blimit);
		map.put("slimit", slimit);
//		logger.error("uid:"+uid+",map:"+map);
		setDistotalByUtype(map, utype, type);
		return map;
	}
	
	/**
	 * 主要处理老客户端utype=1的情况下，取几种抵扣算法中抵扣最小的一个作为抵扣，老客户端是选择不同的券用同一个limit，这样可以防止用户手动选券时抵扣错误
	 * @param map
	 * @param utype 0普通选券（默认）1可用大于最大抵扣金额的停车券
	 * @param type 0：根据金额计算券抵扣金额 1：根据券金额计算满多少消费金额可全额抵扣
	 * @return
	 */
	private Map<String, Object> setDistotalByUtype(Map<String, Object> map, Integer utype, Integer type){
//			logger.error("setDistotalByUtype>>>map:"+map+",utype:"+utype+",type:"+type);
		if(map != null && utype == 1 && type == 0){
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			for(String key : map.keySet()){
				Map<String, Object> dMap = new HashMap<String, Object>();
				dMap.put("dlimit", map.get(key));
				list.add(dMap);
			}
			//按照从小到大排序
			Collections.sort(list, new ListSort6());
			Double dlimit = Double.valueOf(list.get(0).get("dlimit") + "");
//			logger.error("setDistotalByUtype>>>list:"+list+",utype:"+utype+",type:"+type);
			for(String key : map.keySet()){
				map.put(key, dlimit);
			}
//			logger.error("setDistotalByUtype>>>map:"+map+",utype:"+utype+",type:"+type);
		}
		return map;
	}
	
	public List<Map<String, Object>> getTicketInfo(List<Map<String, Object>> list, Integer ptype,Long uid, Integer utype){
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				Integer type=(Integer)map.get("type");
				Integer money = (Integer)map.get("money");
				Integer resources = (Integer)map.get("resources");
				Long limitDay = (Long)map.get("limit_day");
				Double backmoney = StringUtils.formatDouble(map.get("pmoney"));
				Long btime =TimeTools.getToDayBeginTime();
				//==========获取满多少元可全额抵扣begin=============//
				Map<String, Object> fullMap = getDistotalLimit(2, uid, Double.valueOf(money + ""), 1, utype, -1L);
				Double climit = Double.valueOf(fullMap.get("climit") + "");
				Double blimit = Double.valueOf(fullMap.get("blimit") + "");
				Double slimit = Double.valueOf(fullMap.get("slimit") + "");
				Double distotal = Double.valueOf(fullMap.get("distotal") + "");
				map.put("distotal", distotal);
				if(type == 1){
					map.put("full", slimit);
				}
				if(type == 0 && resources == 0){
					map.put("full", climit);
				}
				if(type == 0 && resources == 1){
					map.put("full", blimit);
				}
				//==========获取满多少元可全额抵扣end=============//
				if(btime >limitDay)
					map.put("exp", 0);
				else {
					map.put("exp", 1);
				}
				map.put("isbuy",resources);
				if(resources == 1){//购买的券
//					map.put("desc", "满"+map.get("full")+"元可以抵扣全额,过期后退还"+backmoney+"元至您的账户");
					map.put("desc", " ");
				}else{
//					map.put("desc", "满"+map.get("full")+"元可以抵扣全额");
					map.put("desc", " ");
				}
				map.put("cname", "");
				if(type == 1 && map.get("comid") != null){
					map.put("cname", getParkNameByComid((Long)map.get("comid")));
				}
				map.put("limitday", limitDay);
			}
		}
		return list;
	}
	
	/**
	 * 
	 * @param comid
	 * @return
	 */
	public String getParkNameByComid(Long comid){
		Map<String, Object> comMap = daService.getMap("select company_name from com_info_tb where id =? ",new Object[]{comid});
		if(comMap!=null){
			return (String)comMap.get("company_name");
		}
		return "";		
	}
	
	/**
	 * @param list 券列表
	 * @param ptype 0账户充值；1包月产品；2停车费结算；3直付;4打赏 5购买停车券
	 * @param uid 
	 * @param total 消费金额
	 * @param utype  0普通选券（默认）1可用大于最大抵扣金额的停车券
	 * @return
	 */
	public List<Map<String, Object>> chooseTicketByLevel(List<Map<String, Object>> list, Integer ptype,Long uid, Double total, Integer utype, Long parkId, Long orderId){
		list = null;//2016-09-07马姐提出关掉所有补贴，孙总同意
		
		//抵扣算法
		Map<String, Object> distotalMap = getDistotalLimit(ptype, uid, total, 0, utype, orderId);
		Double climit = Double.valueOf(distotalMap.get("climit") + "");
		Double blimit = Double.valueOf(distotalMap.get("blimit") + "");
		Double slimit = Double.valueOf(distotalMap.get("slimit") + "");
		logger.error("the up limit of distotal>>>uid:"+uid+",map:"+distotalMap+",ptype:"+ptype+",total:"+total);
		if(list != null && !list.isEmpty()){
			for(int i=0; i<list.size();i++){
				Map<String, Object> map = list.get(i);
				Integer iscanuse = 1;//0:不可用 1：可用
				Double limit = 0d;//该停车券可抵扣金额
				Integer type=(Integer)map.get("type");
				Integer money = (Integer)map.get("money");
				Integer resources = (Integer)map.get("resources");
				if(type == 1){//专用停车券
					if(map.get("comid") != null){
						Long comid = (Long)map.get("comid");
						if(comid.intValue() != parkId.intValue()){//不是该车场专用券不可用
							iscanuse = 0;
						}
					}else{
						iscanuse = 0;
					}
					
					if(slimit >= money){
						limit = Double.valueOf(money + "");
					}else{
						limit = slimit;
						if(utype == 0){//不选择大于最大抵扣金额的券
							iscanuse = 0;
						}
					}
					map.put("limit", limit);//抵扣金额
					map.put("level", 3);//专用券优先级最高
				}
				if(type == 0 && resources == 0){//非购买停车券
					if(climit >= money){
						limit = Double.valueOf(money + "");
					}else{
						limit = climit;
						if(utype == 0){//不选择大于最大抵扣金额的券
							iscanuse = 0;
						}
					}
					map.put("limit", limit);//抵扣金额
					map.put("level", 2);//普通非购买券优先权其次
				}
				if(type == 0 && resources == 1){//购买停车券
					if(blimit >= money){
						limit = Double.valueOf(money + "");
					}else{
						iscanuse = 0;//小辉说购买券不可选 
					}
					map.put("limit", limit);//抵扣金额
					map.put("level", 1);//购买券优先级最低
				}
				if(limit == 0){//抵扣0不可用
					iscanuse = 0;
				}
				map.put("offset",  Math.abs(limit-money));//差值绝对值
				map.put("iscanuse", iscanuse);//是否可用大于最大抵扣
			}
			Collections.sort(list, new ListSort());//按照iscanuse由大到小排序
			Collections.sort(list, new ListSort1());//相同的iscanuse按照抵扣金额limit由大到小排序
			Collections.sort(list, new ListSort2());//相同的iscanuse、limit按照offset由小到大排序
			Collections.sort(list, new ListSort3());//相同的iscanuse、limit和offset按照money由小到大排序
			Collections.sort(list, new ListSort4());//相同iscanuse、limit、offset和money按照level由大到小排序
			Collections.sort(list, new ListSort5());//相同iscanuse、limit、offset、money和level相同按照limit_day由小到大排序
			
			getTicketInfo(list, ptype, uid, utype);//计算停车券满多少元可达最大抵扣额
			
		}
		return list;
	}
	
	class ListSort implements Comparator<Map<String, Object>>{
		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			// TODO Auto-generated method stub
			Map map = getParams(o1, o2);
			Integer c1 = (Integer)map.get("c1");
			Integer c2 = (Integer)map.get("c2");
			
			return c2.compareTo(c1);
		}
		
	}
	
	class ListSort1 implements Comparator<Map<String, Object>>{
		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			// TODO Auto-generated method stub
			Map map = getParams(o1, o2);
			Integer c1 = (Integer)map.get("c1");
			Integer c2 = (Integer)map.get("c2");
			
			BigDecimal b1 = (BigDecimal)map.get("b1");
			BigDecimal b2 = (BigDecimal)map.get("b2");
			if(c2.compareTo(c1) == 0){
				return b2.compareTo(b1);
			}else{
				return 0;
			}
		}
		
	}
	
	class ListSort2 implements Comparator<Map<String, Object>>{
		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			// TODO Auto-generated method stub
			Map map = getParams(o1, o2);
			Integer c1 = (Integer)map.get("c1");
			Integer c2 = (Integer)map.get("c2");
			
			BigDecimal b1 = (BigDecimal)map.get("b1");
			BigDecimal b2 = (BigDecimal)map.get("b2");
			
			BigDecimal l1 = (BigDecimal)map.get("l1");
			BigDecimal l2 = (BigDecimal)map.get("l2");
			if(c2.compareTo(c1) == 0 && b2.compareTo(b1) == 0){
				return l1.compareTo(l2);
			}else{
				return 0;
			}
		}
		
	}
	
	class ListSort3 implements Comparator<Map<String, Object>>{

		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			// TODO Auto-generated method stub
			Map map = getParams(o1, o2);
			Integer c1 = (Integer)map.get("c1");
			Integer c2 = (Integer)map.get("c2");
			
			BigDecimal b1 = (BigDecimal)map.get("b1");
			BigDecimal b2 = (BigDecimal)map.get("b2");
			
			BigDecimal l1 = (BigDecimal)map.get("l1");
			BigDecimal l2 = (BigDecimal)map.get("l2");
			
			Integer m1 = (Integer)map.get("m1");
			Integer m2 = (Integer)map.get("m2");
			
			if(c2.compareTo(c1) == 0 && b2.compareTo(b1) == 0 && l2.compareTo(l1) == 0){
				return m1.compareTo(m2);
			}else{
				return 0;
			}
		}
		
	}
	
	class ListSort4 implements Comparator<Map<String, Object>>{

		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			// TODO Auto-generated method stub
			Map map = getParams(o1, o2);
			Integer c1 = (Integer)map.get("c1");
			Integer c2 = (Integer)map.get("c2");
			
			BigDecimal b1 = (BigDecimal)map.get("b1");
			BigDecimal b2 = (BigDecimal)map.get("b2");
			
			BigDecimal l1 = (BigDecimal)map.get("l1");
			BigDecimal l2 = (BigDecimal)map.get("l2");
			
			Integer m1 = (Integer)map.get("m1");
			Integer m2 = (Integer)map.get("m2");
			
			Integer e1 = (Integer)map.get("e1");
			Integer e2 = (Integer)map.get("e2");
			
			if(c2.compareTo(c1) == 0 && b2.compareTo(b1) == 0 && l2.compareTo(l1) == 0 && m2.compareTo(m1) == 0){
				return e2.compareTo(e1);
			}else{
				return 0;
			}
		}
		
	}
	
	class ListSort5 implements Comparator<Map<String, Object>>{

		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			// TODO Auto-generated method stub
			Map map = getParams(o1, o2);
			Integer c1 = (Integer)map.get("c1");
			Integer c2 = (Integer)map.get("c2");
			
			BigDecimal b1 = (BigDecimal)map.get("b1");
			BigDecimal b2 = (BigDecimal)map.get("b2");
			
			BigDecimal l1 = (BigDecimal)map.get("l1");
			BigDecimal l2 = (BigDecimal)map.get("l2");
			
			Integer m1 = (Integer)map.get("m1");
			Integer m2 = (Integer)map.get("m2");
			
			Integer e1 = (Integer)map.get("e1");
			Integer e2 = (Integer)map.get("e2");
			
			Long d1 = (Long)map.get("d1");
			Long d2 = (Long)map.get("d2");
			
			if(c2.compareTo(c1) == 0 && b2.compareTo(b1) == 0 && l2.compareTo(l1) == 0 && m2.compareTo(m1) == 0 && e2.compareTo(e1) == 0){
				return d1.compareTo(d2);
			}else{
				return 0;
			}
		}
		
	}
	
	class ListSort6 implements Comparator<Map<String, Object>>{
		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			// TODO Auto-generated method stub
			BigDecimal b1 = new BigDecimal(0);
			BigDecimal b2 = new BigDecimal(0);
			if(o1.get("dlimit") != null){
				if(o1.get("dlimit") instanceof Double){
					Double ctotal = (Double)o1.get("dlimit");
					b1 = b1.valueOf(ctotal);
				}else{
					b1 = (BigDecimal)o1.get("dlimit");
				}
			}
			if(o2.get("dlimit") != null){
				if(o2.get("dlimit") instanceof Double){
					Double ctotal = (Double)o2.get("dlimit");
					b2 = b2.valueOf(ctotal);
				}else{
					b2 = (BigDecimal)o2.get("dlimit");
				}
			}
			return b1.compareTo(b2);
		}
		
	}
	
	public List<Map<String, Object>> getCarType(Long comid){
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		try {
			Map<String, Object> map = daService.getMap("select car_type from com_info_tb where id=? ", new Object[]{comid});
			if(map != null){
				Integer car_type = (Integer)map.get("car_type");
				if(car_type != 0){
					List<Map<String, Object>> list = daService.getAll("select id as value_no,name as value_name from car_type_tb where comid=? order by sort , id desc ", new Object[]{comid});
					if(!list.isEmpty()){
						result.addAll(list);
					}else {
						Map<String, Object> bigMap = new HashMap<String, Object>();
						bigMap.put("value_name","小车");
						bigMap.put("value_no", 1);
						Map<String, Object> smallMap = new HashMap<String, Object>();
						smallMap.put("value_name","大车");
						smallMap.put("value_no", 2);
						result.add(bigMap);
						result.add(smallMap);
					}
				}else {
					Map<String, Object> firtstMap = new HashMap<String, Object>();
					firtstMap.put("value_name","通用");
					firtstMap.put("value_no", 0);
					result.add(firtstMap);
				}
			}
			
		} catch (Exception e) {
			logger.error("getCarType", e);
		}
		return result;
	}
	private Map<String, Object> getParams(Map<String, Object> o1, Map<String, Object> o2){
		Map<String, Object> map = new HashMap<String, Object>();
		Integer c1 = (Integer)o1.get("iscanuse");
		if(c1 == null) c1 = 0;
		Integer c2 = (Integer)o2.get("iscanuse");
		if(c2 == null) c2 = 0;
		map.put("c1", c1);
		map.put("c2", c2);
		
		BigDecimal b1 = new BigDecimal(0);
		BigDecimal b2 = new BigDecimal(0);
		if(o1.get("limit") != null){
			if(o1.get("limit") instanceof Double){
				Double ctotal = (Double)o1.get("limit");
				b1 = b1.valueOf(ctotal);
			}else{
				b1 = (BigDecimal)o1.get("limit");
			}
		}
		if(o2.get("limit") != null){
			if(o2.get("limit") instanceof Double){
				Double ctotal = (Double)o2.get("limit");
				b2 = b2.valueOf(ctotal);
			}else{
				b2 = (BigDecimal)o2.get("limit");
			}
		}
		map.put("b1", b1);
		map.put("b2", b2);

		BigDecimal l1 = new BigDecimal(0);
		BigDecimal l2 = new BigDecimal(0);
		if(o1.get("offset") != null){
			if(o1.get("offset") instanceof Double){
				Double ctotal = (Double)o1.get("offset");
				l1 = l1.valueOf(ctotal);
			}else{
				l1 = (BigDecimal)o1.get("offset");
			}
		}
		if(o2.get("offset") != null){
			if(o2.get("offset") instanceof Double){
				Double ctotal = (Double)o2.get("offset");
				l2 = l2.valueOf(ctotal);
			}else{
				l2 = (BigDecimal)o2.get("offset");
			}
		}
		map.put("l1", l1);
		map.put("l2", l2);
		
		Integer m1 = (Integer)o1.get("money");
		if(m1 == null) m1 = 0;
		Integer m2 = (Integer)o2.get("money");
		if(m2 == null) m2 = 0;
		map.put("m1", m1);
		map.put("m2", m2);
		
		Integer e1 = (Integer)o1.get("level");
		if(e1 == null) e1 = 0;
		Integer e2 = (Integer)o2.get("level");
		if(e2 == null) e2 = 0;
		map.put("e1", e1);
		map.put("e2", e2);
		
		Long d1 = (Long)o1.get("limit_day");
		if(d1 == null) d1 = 0L;
		Long d2 = (Long)o2.get("limit_day");
		if(d2 == null) d2 = 0L;
		map.put("d1", d1);
		map.put("d2", d2);
		
		return map;
	}
	/**
	 * 获取城市底下的车场
	 * @param cityid
	 * @return
	 */
	public List<Object> getparks(Long cityid){
		List<Object> parks = new ArrayList<Object>();
		try {
			List<Object> params = new ArrayList<Object>();
			String sql = "select id from com_info_tb where state<>? " ;
			params.add(1);
			List<Object> groups = getGroups(cityid);//查询该城市所辖的运营集团
			if(groups != null && !groups.isEmpty()){
				String preParams  ="";
				for(Object grouid : groups){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " and groupid in ("+preParams+") ";
				params.addAll(groups);
				List<Map<String, Object>> list = pService.getAllMap(sql, params);
				if(list != null && !list.isEmpty()){
					for(Map<String, Object> map : list){
						parks.add(map.get("id"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parks;
	}
	
	/**
	 * 获取城市底下的运营集团编号
	 * @param cityid
	 * @return
	 */
	public List<Object> getGroups(Long cityid){//查询城市所辖的运营集团
		List<Object> groups = new ArrayList<Object>();
		List<Map<String, Object>> list = pService.getAll("select id from org_group_tb" +
				" where cityid=? and state=? ", 
				new Object[]{cityid, 0});
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				groups.add(map.get("id"));
			}
		}
		return groups;
	}
	
	/**
	 * 获取运营集团辖下的车场或者运营集团辖下的区域地下的车场
	 * @param groupid
	 * @return
	 */
	public List<Object> getParks(Long groupid){
		List<Object> parks = new ArrayList<Object>();
		List<Object> params = new ArrayList<Object>();
		String sql = "select id from com_info_tb where state<>? and (groupid=? " ;
		params.add(1);
		params.add(groupid);
		String preParams  ="";
		List<Object> groups = new ArrayList<Object>();
		groups.add(groupid);
		List<Object> areas = getAreas(groups);//查询城市直辖的区域和城市所辖的运营集团所辖的区域
		if(areas != null && !areas.isEmpty()){
			preParams = "";
			for(Object area : areas){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			sql += " or areaid in ("+preParams+") ";
			params.addAll(areas);
		}
		sql += " )";
		List<Map<String, Object>> list = pService.getAllMap(sql,params);
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				parks.add(map.get("id"));
			}
		}
		return parks;
	}
	/**
	 * 获取运营集团底下的区域
	 * @param groups
	 * @return
	 */
	public List<Object> getAreas(List<Object> groups){//查询城市直属区域和城市所辖集团属下的区域
		List<Object> areas = new ArrayList<Object>();
		List<Object> params = new ArrayList<Object>();
		String sql = "select id from org_area_tb where state=? ";
		params.add(0);
		if(groups != null && !groups.isEmpty()){
			String preParams  ="";
			for(Object grouid : groups){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			sql += " and groupid in ("+preParams+")";
			params.addAll(groups);
			List<Map<String, Object>> list = pService.getAllMap(sql, params);
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					areas.add(map.get("id"));
				}
			}
		}
		return areas;
	}
	//----------------选券逻辑end--------------------//
}
