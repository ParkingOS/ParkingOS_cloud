package com.zld.impl;

import com.zld.CustomDefind;
import com.zld.pojo.WorkRecord;
import com.zld.pojo.WorkTime;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
@Repository
public class CommonMethods {


	private Logger logger = Logger.getLogger(CommonMethods.class);
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
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
				Map<String, Object> userMap = pgOnlyReadService.getMap("select role_id from user_info_tb where id=? and role_id>? ",
						new Object[]{uin, 0});
				if(userMap != null){
					Long role_id = (Long)userMap.get("role_id");
					long offsetTime = time - TimeTools.getToDayBeginTime();
					logger.error("offsetTime:"+offsetTime);
					List<WorkTime> workTimes = pgOnlyReadService.getPOJOList("select * from work_time_tb " +
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
	 * 主页控制台跳转到其他页面，返回时需要主页的授权编号
	 * @return
	 */
	public void setIndexAuthId(HttpServletRequest request){
		try {
			request.setAttribute("from", RequestUtil.processParams(request, "from"));
			List<Map<String, Object>> authList = (List<Map<String, Object>>)request.getSession().getAttribute("authlist");
			if(authList != null){
				for(Map<String, Object> map : authList){
					if(map.get("url") != null){
						String url = (String)map.get("url");
						if(url.contains("cityindex.do")){
							request.setAttribute("index_authid", map.get("auth_id"));
						}
						if(url.contains("citysensor.do")){
							request.setAttribute("index_authid", map.get("auth_id"));
						}
						if(url.contains("citytransmitter.do")){
							request.setAttribute("index_authid", map.get("auth_id"));
						}
						if(url.contains("cityinduce.do")){
							request.setAttribute("index_authid", map.get("auth_id"));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			WorkRecord workRecord = pgOnlyReadService.getPOJO("select * from parkuser_work_record_tb where " +
							" uid=? and state=? order by id desc limit ? ",
					new Object[]{parkUserId, 0,  1}, WorkRecord.class);
			return workRecord;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 查询所有收费员的收入汇总
	 * @param idList
	 * @param beginTime
	 * @param endTime
	 * @param type
	 * @return
	 */
	public List<Map<String, Object>> getIncomeByTimeAnly(List<Object> idList, Long beginTime, Long endTime, int type){
		try {
			if(idList != null && !idList.isEmpty()){
				String preParam = "";
				for(Object object : idList){
					if(preParam.equals("")){
						preParam = "?";
					}else{
						preParam += ",?";
					}
				}
				List<Object> params = new ArrayList<Object>();
				params.add(beginTime);
				params.add(endTime);
				params.addAll(idList);
				params.add(type);
				String sql = "select create_time,sum(prepay_cash) as prepay_cash," +
						"sum(add_cash) as add_cash,sum(refund_cash) as refund_cash,sum(pursue_cash) as pursue_cash,sum(pfee_cash) as pfee_cash," +
						"sum(prepay_epay) as prepay_epay,sum(add_epay) as add_epay,sum(refund_epay) as refund_epay,sum(pursue_epay) as pursue_epay," +
						"sum(pfee_epay) as pfee_epay,sum(escape) as escape,sum(prepay_escape) as prepay_escape,sum(sensor_fee) as sensor_fee," +
						"sum(prepay_card) as prepay_card,sum(add_card) as add_card,sum(refund_card) as refund_card,sum(pursue_card) as pursue_card," +
						"sum(pfee_card) as pfee_card,sum(charge_card_cash) charge_card_cash,sum(return_card_count) return_card_count," +
						"sum(return_card_fee) return_card_fee,sum(act_card_count) act_card_count,sum(act_card_fee) act_card_fee," +
						"sum(reg_card_count) reg_card_count,sum(reg_card_fee) reg_card_fee,sum(bind_card_count) bind_card_count from " +
						"parkuser_income_anlysis_tb where create_time between ? and ? and uin in ("+preParam+") and type=? group by create_time order by create_time ";
				List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql, params);
				return list;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 查询所有收费员的收入汇总
	 * @param idList
	 * @param beginTime
	 * @param endTime
	 * @param type
	 * @return
	 */
	public Map<String, Object> sumIncomeAnly(List<Object> idList, Long beginTime, Long endTime, int type){
		try {
			if(idList != null && !idList.isEmpty()){
				String preParam = "";
				for(Object object : idList){
					if(preParam.equals("")){
						preParam = "?";
					}else{
						preParam += ",?";
					}
				}
				List<Object> params = new ArrayList<Object>();
				params.add(beginTime);
				params.add(endTime);
				params.addAll(idList);
				params.add(type);
				String sql = "select sum(prepay_cash) as prepay_cash," +
						"sum(add_cash) as add_cash,sum(refund_cash) as refund_cash,sum(pursue_cash) as pursue_cash,sum(pfee_cash) as pfee_cash," +
						"sum(prepay_epay) as prepay_epay,sum(add_epay) as add_epay,sum(refund_epay) as refund_epay,sum(pursue_epay) as pursue_epay," +
						"sum(pfee_epay) as pfee_epay,sum(escape) as escape,sum(prepay_escape) as prepay_escape,sum(sensor_fee) as sensor_fee," +
						"sum(prepay_card) as prepay_card,sum(add_card) as add_card,sum(refund_card) as refund_card,sum(pursue_card) as pursue_card," +
						"sum(pfee_card) as pfee_card,sum(charge_card_cash) charge_card_cash,sum(return_card_count) return_card_count," +
						"sum(return_card_fee) return_card_fee,sum(act_card_count) act_card_count,sum(act_card_fee) act_card_fee," +
						"sum(reg_card_count) reg_card_count,sum(reg_card_fee) reg_card_fee,sum(bind_card_count) bind_card_count from " +
						"parkuser_income_anlysis_tb where create_time between ? and ? and uin in ("+preParam+") and type=? ";
				Map<String, Object> infoMap = pgOnlyReadService.getMap(sql, params);
				return infoMap;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 查询每个收费员的收费情况
	 * @param idList
	 * @param beginTime
	 * @param endTime
	 * @param type
	 * @return
	 */
	public List<Map<String, Object>> getIncomeAnly(List<Object> idList, Long beginTime, Long endTime, int type){
		try {
			if(idList != null && !idList.isEmpty()){
				List<Object> paramsList = new ArrayList<Object>();
				paramsList.add(beginTime);
				paramsList.add(endTime);
				paramsList.addAll(idList);
				paramsList.add(type);
				String param = "";
				for(Object object : idList){
					if(param.equals("")){
						param = "?";
					}else{
						param += ",?";
					}
				}
				List<Map<String, Object>> list = pgOnlyReadService.getAllMap("select uin as id,sum(prepay_cash) as prepay_cash," +
						"sum(add_cash) as add_cash,sum(refund_cash) as refund_cash,sum(pursue_cash) as pursue_cash,sum(pfee_cash) as pfee_cash," +
						"sum(prepay_epay) as prepay_epay,sum(add_epay) as add_epay,sum(refund_epay) as refund_epay,sum(pursue_epay) as pursue_epay," +
						"sum(pfee_epay) as pfee_epay,sum(escape) as escape,sum(prepay_escape) as prepay_escape,sum(sensor_fee) as sensor_fee," +
						"sum(prepay_card) as prepay_card,sum(add_card) as add_card,sum(refund_card) as refund_card,sum(pursue_card) as pursue_card," +
						"sum(pfee_card) as pfee_card,sum(charge_card_cash) charge_card_cash,sum(return_card_count) return_card_count," +
						"sum(return_card_fee) return_card_fee,sum(act_card_count) act_card_count,sum(act_card_fee) act_card_fee," +
						"sum(reg_card_count) reg_card_count,sum(reg_card_fee) reg_card_fee,sum(bind_card_count) bind_card_count from " +
						"parkuser_income_anlysis_tb where create_time between ? and ? and uin in ("+param+") and type=? group by uin", paramsList);
				return list;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Map<String, Object>> getIncome(Long startTime, Long endTime, List<Object> comidList, List<Object> uinList, Map<String, Object> otherMap){
		try {
			List<Object> params = new ArrayList<Object>();
			String preParams = "";
			String sql = "";
			String groupSql = "";
			if(comidList != null && !comidList.isEmpty()){
				for(Object o : comidList){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql = " o.comid in ("+preParams+")";
				groupSql = ",o.comid ";
				params.addAll(comidList);
			}else if(uinList != null && !uinList.isEmpty()){
				for(Object o : uinList){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql = " a.uin in ("+preParams+")";
				groupSql = ",a.uin";
				params.addAll(uinList);
			}
			List<Object> params1 = new ArrayList<Object>();
			params1.addAll(params);
			params1.add(0);//停车费（非预付
			params1.add(1);//预付停车费
			params1.add(2);//预付退款（预付超额）
			params1.add(3);//预付补缴（预付不足）
			params1.add(4);//追缴停车费
			SqlInfo sqlInfo1 = new SqlInfo(sql + " and a.target in (?,?,?,?,?)", params1);
			List<Map<String, Object>> list1 = anlysisMoney(1, startTime, endTime, new String[]{"a.target" + groupSql}, sqlInfo1, null);
			params1.clear();
			params1.addAll(params);
			params1.add(4);//车主付停车费（非预付）或者打赏收费员
			params1.add(5);//追缴停车费
			params1.add(6);//车主预付停车费
			params1.add(7);//预付退款（预付超额）
			params1.add(8);//预付补缴（预付金额不足）
			SqlInfo sqlInfo2 = new SqlInfo(sql + " and a.target in (?,?,?,?,?)", params1);
			List<Map<String, Object>> list2 = anlysisMoney(2, startTime, endTime, new String[]{"a.target" + groupSql}, sqlInfo2, null);
			params1.clear();
			params1.addAll(params);
			params1.add(0);//停车费（非预付）
			params1.add(7);//追缴停车费
			params1.add(8);//车主预付停车费
			params1.add(9);//预付退款（预付超额）
			params1.add(10);//预付补缴（预付金额不足）
			String sql2 = sql;
			String groupSql2 = groupSql;
			if(sql.contains("a.uin")){
				sql2 = sql.replace("a.uin", "a.uid");
			}
			if(groupSql.contains("a.uin")){
				groupSql2 = groupSql.replace("a.uin", "a.uid");
			}
			SqlInfo sqlInfo3 = new SqlInfo(sql2 + " and a.source in (?,?,?,?,?)", params1);
			List<Map<String, Object>> list3 = anlysisMoney(3, startTime, endTime, new String[]{"a.source" + groupSql2}, sqlInfo3, null);
			params1.clear();
			params1.addAll(params);
			params1.add(0);//停车费（非预付）
			params1.add(2);//追缴停车费
			params1.add(3);//预付停车费
			params1.add(4);//预付退款（预付）
			params1.add(5);//预付补缴（预付金额不足）
			SqlInfo sqlInfo4 = new SqlInfo(sql2 + " and a.source in (?,?,?,?,?)", params1);
			List<Map<String, Object>> list4 = anlysisMoney(4, startTime, endTime, new String[]{"a.source" + groupSql2}, sqlInfo4, null);
			params1.clear();
			params1.addAll(params);
			params1.add(0);//停车费（非预付）
			params1.add(2);//追缴停车费
			params1.add(3);//预付停车费
			params1.add(4);//预付退款（预付）
			params1.add(5);//预付补缴（预付金额不足）
			SqlInfo sqlInfo5 = new SqlInfo(sql2 + " and a.source in (?,?,?,?,?)", params1);
			List<Map<String, Object>> list5 = anlysisMoney(5, startTime, endTime, new String[]{"a.source" + groupSql2}, sqlInfo5, null);
			params1.clear();
			params1.addAll(params);
			String sql3 = sql;
			String groupSql3 = groupSql;
			if(sql.contains("a.uin")){
				sql3 = sql.replace("a.uin", "uid");
			}
			if(sql.contains("o.comid")){
				sql3 = sql.replace("o.comid", "comid");
			}
			if(groupSql.contains("a.uin")){
				groupSql3 = groupSql.replace("a.uin", "uid");
			}
			if(groupSql.contains("o.comid")){
				groupSql3 = groupSql.replace("o.comid", "comid");
			}
			if(groupSql3.contains(",")){
				groupSql3 = groupSql3.substring(1);
			}
			SqlInfo sqlInfo6 = new SqlInfo(sql3, params1);
			List<Map<String, Object>> list6 = anlysisMoney(6, startTime, endTime, new String[]{groupSql3}, sqlInfo6, null);
			String sql4 = sql;
			String groupSql4 = groupSql;
			if(sql.contains("a.uin")){
				sql4 = sql.replace("a.uin", "out_uid");
			}
			if(sql.contains("o.comid")){
				sql4 = sql.replace("o.comid", "comid");
			}
			if(groupSql.contains("a.uin")){
				groupSql4 = groupSql.replace("a.uin", "out_uid");
			}
			if(groupSql.contains("o.comid")){
				groupSql4 = groupSql.replace("o.comid", "comid");
			}
			if(groupSql4.contains(",")){
				groupSql4 = groupSql4.substring(1);
			}
			SqlInfo sqlInfo7 = new SqlInfo(sql4, params1);
			List<Map<String, Object>> list7 = anlysisMoney(7, startTime, endTime, new String[]{groupSql4}, sqlInfo7, null);
			String sql5 = sql;
			String groupSql5 = groupSql;
			if(sql.contains("a.uin")){
				sql5 = sql.replace("a.uin", "o.uid");
			}
			if(groupSql.contains("a.uin")){
				groupSql5 = groupSql.replace("a.uin", "o.uid");
			}
			if(groupSql5.contains(",")){
				groupSql5 = groupSql5.substring(1);
			}
			SqlInfo sqlInfo8 = new SqlInfo(sql5, params1);
			List<Map<String, Object>> list8 = anlysisMoney(8, startTime, endTime, new String[]{groupSql5}, sqlInfo8, null);
			List<Map<String, Object>> list9 = anlysisMoney(9, startTime, endTime, new String[]{groupSql5}, sqlInfo8, null);
			List<Map<String, Object>> list10 = anlysisMoney(10, startTime, endTime, new String[]{groupSql5}, sqlInfo8, null);
			List<Map<String, Object>> list11 = anlysisMoney(11, startTime, endTime, new String[]{groupSql5}, sqlInfo8, null);
			List<Map<String, Object>> list12 = anlysisMoney(12, startTime, endTime, new String[]{groupSql5}, sqlInfo8, null);
			List<Map<String, Object>> list14 = anlysisMoney(14, startTime, endTime, new String[]{groupSql5}, sqlInfo8, null);
			params1.clear();
			params1.addAll(params);
			params1.add(4);//charge_type -- 充值方式：4：预支付退款
			params1.add(0);//consume_type --消费方式 0：支付停车费（非预付）
			params1.add(1);//consume_type --消费方式 1：预付停车费
			params1.add(2);//consume_type --消费方式 2：补缴停车费
			params1.add(3);//consume_type --消费方式3：追缴停车费
			SqlInfo sqlInfo9 = new SqlInfo(sql2 + " and (a.charge_type in (?) or a.consume_type in (?,?,?,?))", params1);
			List<Map<String, Object>> list13 = anlysisMoney(13, startTime, endTime,
					new String[]{"a.charge_type,a.consume_type" + groupSql2}, sqlInfo9, null);

			List<Map<String, Object>> infoList = new ArrayList<Map<String,Object>>();
			List<Object> idList = new ArrayList<Object>();
			mergeIncome(1, list1, infoList, idList);
			mergeIncome(2, list2, infoList, idList);
			mergeIncome(3, list3, infoList, idList);
			mergeIncome(4, list4, infoList, idList);
			mergeIncome(5, list5, infoList, idList);
			mergeIncome(6, list6, infoList, idList);
			mergeIncome(7, list7, infoList, idList);
			mergeIncome(8, list8, infoList, idList);
			mergeIncome(9, list9, infoList, idList);
			mergeIncome(10, list10, infoList, idList);
			mergeIncome(11, list11, infoList, idList);
			mergeIncome(12, list12, infoList, idList);
			mergeIncome(13, list13, infoList, idList);
			mergeIncome(14, list14, infoList, idList);
			return infoList;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	private List<Map<String, Object>> mergeIncome(int type, List<Map<String, Object>> list, List<Map<String, Object>> infoList, List<Object> idList){
		try {
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					Long id = null;
					Integer target = null;
					Double summoney = 0d;
					if(map.get("summoney") != null){
						summoney = Double.valueOf(map.get("summoney") + "");
					}
					if(map.get("comid") != null){
						id = (Long)map.get("comid");
					}else if(map.get("uin") != null){
						id = (Long)map.get("uin");
					}else if(map.get("uid") != null){
						id = (Long)map.get("uid");
					}else if(map.get("out_uid") != null){
						id = (Long)map.get("out_uid");
					}
					if(map.get("target") != null){
						target = (Integer)map.get("target");
					}else if(map.get("source") != null){
						target = (Integer)map.get("source");
					}
					if(idList.contains(id)){
						for(Map<String, Object> infoMap : infoList){
							Long infoId = (Long)infoMap.get("id");
							if(id.intValue() == infoId.intValue()){
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
								Double sensor_fee = Double.valueOf(infoMap.get("sensor_fee") + "");//车检器停车费
								Double prepay_card = Double.valueOf(infoMap.get("prepay_card") + "");//刷卡预支付
								Double add_card = Double.valueOf(infoMap.get("add_card") + "");//刷卡补缴
								Double refund_card = Double.valueOf(infoMap.get("refund_card") + "");//刷卡退款
								Double pursue_card = Double.valueOf(infoMap.get("pursue_card") + "");//刷卡追缴
								Double pfee_card = Double.valueOf(infoMap.get("pfee_card") + "");//刷卡停车费（非预付）
								if(type == 1){
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
								}else if(type == 2){
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
								}else if(type == 3){
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
								}else if(type == 4){
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
								}else if(type == 5){
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
								}else if(type == 6){
									escape += summoney;
								}else if(type == 7){
									sensor_fee += summoney;
								}else if(type == 8
										|| type == 9
										|| type == 10
										|| type == 11
										|| type == 12
										|| type == 14){
									prepay_escape += summoney;
								}else if(type == 13){
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
								}
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
								infoMap.put("sensor_fee", sensor_fee);
								infoMap.put("prepay_card", prepay_card);
								infoMap.put("add_card", add_card);
								infoMap.put("refund_card", refund_card);
								infoMap.put("pursue_card", pursue_card);
								infoMap.put("pfee_card", pfee_card);
							}
						}
					}else{
						idList.add(id);
						Double prepay_cash = 0d;
						Double add_cash = 0d;
						Double refund_cash = 0d;
						Double pursue_cash = 0d;
						Double pfee_cash = 0d;
						Double prepay_epay = 0d;
						Double add_epay = 0d;
						Double refund_epay = 0d;
						Double pfee_epay = 0d;
						Double pursue_epay = 0d;
						Double escape = 0d;
						Double prepay_escape = 0d;
						Double sensor_fee = 0d;
						Double prepay_card = 0d;
						Double add_card = 0d;
						Double refund_card = 0d;
						Double pursue_card = 0d;
						Double pfee_card = 0d;
						if(type == 1){
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
						}else if(type == 2){
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
						}else if(type == 3){
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
						}else if(type == 4){
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
						}else if(type == 5){
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
						}else if(type == 6){
							escape += summoney;
						}else if(type == 7){
							sensor_fee += summoney;
						}else if(type == 8
								|| type == 9
								|| type == 10
								|| type == 11
								|| type == 12
								|| type ==14){
							prepay_escape += summoney;
						}else if(type == 13){
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
						}
						Map<String, Object> infoMap = new HashMap<String, Object>();
						infoMap.put("prepay_cash", prepay_cash);
						infoMap.put("add_cash", add_cash);
						infoMap.put("refund_cash", refund_cash);
						infoMap.put("pursue_cash", pursue_cash);
						infoMap.put("pfee_cash", pfee_cash);
						infoMap.put("prepay_epay", prepay_epay);
						infoMap.put("add_epay", add_epay);
						infoMap.put("refund_epay", refund_epay);
						infoMap.put("pfee_epay", pfee_epay);
						infoMap.put("pursue_epay", pursue_epay);
						infoMap.put("escape", escape);
						infoMap.put("prepay_escape", prepay_escape);
						infoMap.put("sensor_fee", sensor_fee);
						infoMap.put("prepay_card", prepay_card);
						infoMap.put("add_card", add_card);
						infoMap.put("refund_card", refund_card);
						infoMap.put("pursue_card", pursue_card);
						infoMap.put("pfee_card", pfee_card);
						infoMap.put("id", id);
						infoList.add(infoMap);
					}
				}
			}
			return infoList;
		} catch (Exception e) {
			// TODO: handle exception
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
				result = pgOnlyReadService.getAllMap(sql, params);
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

	public Map<String, Object> getBerthCount(Long groupid, Long cityid){
		String sql = "select sum(share_count) asum,sum(used_count) usum from park_anlysis_tb where ";
		List<Object> params = new ArrayList<Object>();
		List<Object> parks = null;
		if(cityid > 0){
			parks = getparks(cityid);
		}else if(groupid > 0){
			parks = getParks(groupid);
		}
		if(parks != null && !parks.isEmpty()){
			String preParams  ="";
			for(Object parkid : parks){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			params.addAll(parks);
			sql += " comid in ("+preParams+") and create_time=(select max(create_time) from park_anlysis_tb)";
			Map<String, Object> map = pgOnlyReadService.getMap(sql, params);
			return map;
		}
		return null;
	}

	/**
	 * 获取诱导屏心跳状态
	 * @param list
	 */
	public void getState(List<Map<String, Object>> list){
		Long ntime = System.currentTimeMillis()/1000;
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				int state = 0;
				if(map.get("heartbeat_time") == null){
					state = 1;
				}else{
					Long heartbeat_time = (Long)map.get("heartbeat_time");
					if(ntime - heartbeat_time > 30 * 60){
						state = 1;
					}
				}
				map.put("induce_state", state);
			}
		}
	}

	/**
	 * 根据账号获取车牌号
	 * @param uin
	 * @return
	 */
	public String getcar(Long uin){
		String cars = "无";
		if(uin!=-1){
			List<Map<String, Object>> carList = daService.getAll("select car_number " +
					" from car_info_tb where uin =? ", new Object[]{uin});
			if(carList!=null&&!carList.isEmpty()){
				cars = "";
				for(Map<String, Object> map :carList){
					cars += map.get("car_number")+",";
				}
				if(cars.endsWith(","))
					cars =cars.substring(0,cars.length()-1);
			}
		}
		return cars;
	}

	/**
	 * 检查该车位是否被占用
	 * @param comid
	 * @param plot
	 * @param btime
	 * @param etime
	 * @param cid
	 * @return
	 */
	public String checkplot(Long comid, String plot, Long btime, Long etime, Long cid){
		logger.error("check plots>>>comid:"+comid+",plot:"+plot+",btime:"+btime+",etime:"+etime);
		String r = null;
		if(plot != null && !plot.equals("")){
			Long count = daService.getLong("select count(id) from com_park_tb where cid=? and comid=? ",
					new Object[]{plot, comid});
			logger.error("check plots>>>count:"+count);
			if(count == 0){
				r = "车位"+plot+"不存在";
			}
			count = pgOnlyReadService.getLong("select count(cp.id) from carower_product cp,product_package_tb p where cp.pid=p.id and ((cp.b_time<=? and cp.e_time>?) or (cp.b_time<? and cp.b_time>=?)) and cp.p_lot=? and p.comid=? and cp.id!=? and cp.is_delete=?",
					new Object[]{btime, btime, etime, btime, plot, comid, cid,0});
			logger.error("check plots>>>count:"+count);
			if(count > 0){
				r = "当前时间段内，车位"+plot+"已被占用，推荐车位编号：";
			}
			if(r != null){
				List<Map<String, Object>> list = pgOnlyReadService.getAll("select cid from com_park_tb where comid=? ",
						new Object[]{comid});
				List<Map<String, Object>> list2 = pgOnlyReadService.getAll("select cp.p_lot from carower_product cp,product_package_tb p where cp.pid=p.id and ((cp.b_time<=? and cp.e_time>?) or (cp.b_time<? and cp.b_time>=?)) and p.comid=? and cp.id!=? and cp.is_delete=? and cp.p_lot is not null ",
						new Object[]{btime, btime, etime, btime, comid, cid,0});
				List<String> allplots = new ArrayList<String>();
				List<String> usedplots = new ArrayList<String>();
				for(Map<String, Object> map : list){
					if(map.get("cid") != null){
						allplots.add((String)map.get("cid"));
					}
				}
				for(Map<String, Object> map : list2){
					if(map.get("p_lot") != null){
						usedplots.add((String)map.get("p_lot"));
					}
				}
				for(int i = 0;i<allplots.size(); i++){
					String p_lot = allplots.get(i);
					if(i > 5){
						break;
					}
					if(!usedplots.contains(p_lot)){
						if(i == 0){
							r += p_lot;
						}else{
							r += ","+p_lot;
						}
					}
				}
			}
		}
		logger.error("check plots>>>r:"+r);
		return r;
	}
	/**
	 * 获取运营集团辖下的车场或者运营集团辖下的区域地下的车场
	 * @param groupid
	 * @return
	 */
	public List<Object> getParks(Long groupid){
		List<Object> parks = new ArrayList<Object>();
		try {
			List<Object> params = new ArrayList<Object>();
			String sql = "select id from com_info_tb where state<>? and groupid=? " ;
			params.add(1);
			params.add(groupid);
			List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql, params);
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					parks.add(map.get("id"));
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
		List<Map<String, Object>> list = pgOnlyReadService.getAll("select id from org_group_tb" +
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
			List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql, params);
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					areas.add(map.get("id"));
				}
			}
		}
		return areas;
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
				List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql, params);
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
	 * 获取运营集团的收费员
	 */
	public List<Object> getCollctors(Long groupid){
		List<Object> collectors = new ArrayList<Object>();
		try {
			List<Object> params = new ArrayList<Object>();
			params.add(1);
			params.add(1);
			params.add(2);
			params.add(groupid);
			String sql = "select id from user_info_tb where state<>? and (auth_flag=? or auth_flag=?)" +
					" and groupid=? " ;
			List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql, params);

			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					collectors.add(map.get("id"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return collectors;
	}

	public List<Object> getcollctors(Long cityid){
		List<Object> collectors = new ArrayList<Object>();
		try {
			List<Object> params = new ArrayList<Object>();
			String sql = "select id from user_info_tb where state<>? and (auth_flag=? or auth_flag=?)" +
					" and (cityid=? " ;
			params.add(1);
			params.add(1);
			params.add(2);
			params.add(cityid);
			List<Object> groups = getGroups(cityid);//查询该城市所辖的运营集团
			if(groups != null && !groups.isEmpty()){
				String preParams  ="";
				for(Object grouid : groups){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " or groupid in ("+preParams+") ";
				params.addAll(groups);
			}
			sql += ")";
			List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql, params);
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					collectors.add(map.get("id"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return collectors;
	}

	/**
	 * 获取车场的泊位段编号
	 * @param parks
	 * @return
	 */
	public List<Object> getBerthSeg(List<Object> parks){
		List<Object> berthseg = new ArrayList<Object>();
		List<Object> params = new ArrayList<Object>();
		String sql = "select id from com_berthsecs_tb where is_active=? " ;
		params.add(0);
		if(parks != null && !parks.isEmpty()){
			String preParams  ="";
			for(Object park : parks){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			sql += " and comid in ("+preParams+") ";
			params.addAll(parks);

			List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql,params);
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					berthseg.add(map.get("id"));
				}
			}
		}
		return berthseg;
	}

	//更新车位信息，更新已结算订单的占用车位
	public void updateParkInfo(Long comId) {
		int r =daService.update("update com_park_tb set state =?,order_id=? where order_id in " +
						"(select id from order_tb where state in(?,?) and id in(select order_id from com_park_tb where comid=?)) ",
				new Object[]{0,null,1,2,comId});
		logger.error(comId+"，更新了"+r+"条车位信息");
	}


	//查询礼包
	public boolean checkBonus(String mobile,Long uin){
		List bList = pgOnlyReadService.getAll("select * from bonus_record_tb where mobile=? and state=? ",new Object[]{mobile,0});
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
			list=	pgOnlyReadService.getAll("select * from ticket_tb where uin = ? " +
							"and state=? and limit_day>=? and type<? and money<?  order by limit_day",
					new Object[]{uin,0,TimeTools.getToDayBeginTime(),2,ticketquota+1});

		}else {
			list  = pgOnlyReadService.getAll("select * from ticket_tb where uin = ? " +
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

	/**
	 * 检查帐号是否已注册
	 * @param strid
	 * @return
	 */
	public boolean checkStrid(String strid){
		String sql = "select count(*) from user_info_tb where strid =?";
		Long result = daService.getLong(sql, new Object[]{strid});
		if(result>0){
			return false;
		}
		return true;
	}

	public boolean checkStrid(String strid, Long uin){
		String sql = "select count(*) from user_info_tb where strid =? and id<>? ";
		Long result = daService.getLong(sql, new Object[]{strid, uin});
		if(result>0){
			return false;
		}
		return true;
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
			if(pMap!=null){
				if(pMap.get("price")!=null){
					price = Double.valueOf(pMap.get("price")+"");
				}
			}
			total = months*price;
		}
		return total;
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
	 * 扫减免券，获取减免前后的停车费金额
	 * @param orderMap
	 * @param shopTicketMap
	 * @param delaytime 预支付延时时间
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getPrice(Long orderId, Long end_time){
		Map<String, Object> orderMap = daService.getMap("select * from order_tb where id=? ",
				new Object[]{orderId});

		Map<String, Object> map = new HashMap<String, Object>();
		Long comid = (Long)orderMap.get("comid");
		Double beforetotal = 0d;
		Double aftertotal = 0d;
		Integer car_type = (Integer)orderMap.get("car_type");//0：通用，1：小车，2：大车
		Integer pid = (Integer)orderMap.get("pid");
		Long create_time = (Long)orderMap.get("create_time");
		Integer distime = 0;//抵扣的时长

		beforetotal = getPrice(car_type, pid, comid, create_time, end_time);

		Map<String, Object> shopTicketMap = daService.getMap("select * from ticket_tb where orderid=? and (type=? or type=?) ",
				new Object[]{orderId, 3, 4});
		if(shopTicketMap != null){
			Integer type = (Integer)shopTicketMap.get("type");
			if(type == 3){
				Integer time = (Integer)shopTicketMap.get("money");
				if(end_time > create_time + time *60 *60){
					aftertotal = getPrice(car_type, pid, comid, create_time, end_time - time * 60 *60);
					distime =time *60 *60;
				}else if(end_time > create_time){
					distime = (end_time.intValue() - create_time.intValue());
				}
			}else if(type == 4){
				if(end_time > create_time){
					distime = (end_time.intValue() - create_time.intValue());
				}
			}
		}else{
			aftertotal = beforetotal;
		}

		Double distotal = beforetotal - aftertotal >0 ? (beforetotal - aftertotal) : 0d;

		if(shopTicketMap != null && beforetotal > aftertotal){
			int r = daService.update("update ticket_tb set umoney=?,bmoney=? where id=? ",
					new Object[]{StringUtils.formatDouble(distotal), Double.valueOf(distime)/(60*60), shopTicketMap.get("id")});
		}
		map.put("beforetotal", beforetotal);
		map.put("aftertotal", aftertotal);
		return map;
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
	public Double getPrice(Integer car_type, Integer pid, Long comid, Long create_time, Long end_time){
		Double total = 0d;
		if(pid>-1){
			total = Double.valueOf(publicMethods.getCustomPrice(create_time, end_time, pid));
		}else {
			total = Double.valueOf(publicMethods.getPrice(create_time, end_time, comid, car_type));
		}
		return total;
	}

	/**
	 * 获取订单的信息
	 * @param orderId
	 * @param shopTicketId 减免券ID
	 * @param uin 用户ID
	 * @param delaytime 预支付的延迟时间
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getOrderInfo(Long orderId, Long shopTicketId, Long end_time){
		Double pretotal = 0d;//已经预支付的金额
		Map<String, Object> orderMap = daService.getMap("select * from order_tb where id=? ",
				new Object[]{orderId});
		if(orderMap == null){
			return null;
		}
		if(orderMap.get("total") != null){
			pretotal = Double.valueOf(orderMap.get("total") + "");//预支付的金额
		}
		Long create_time = (Long)orderMap.get("create_time");
		Map<String, Object> map = getComOrderInfo(orderId, shopTicketId, create_time, end_time);

		map.put("createtime", create_time);
		map.put("starttime", TimeTools.getTime_yyyyMMdd_HHmm(create_time * 1000));
		map.put("parktime", StringUtils.getTimeString(create_time, System.currentTimeMillis()/1000));
		map.put("pretotal", pretotal);
		map.put("shopticketid", shopTicketId);
		map.put("uid", orderMap.get("uid"));
		map.put("comid", orderMap.get("comid"));
		map.put("carnumber", orderMap.get("car_number"));
		return map;
	}

	/**
	 * 获取已结算订单的信息
	 * @param orderid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getOrderInfoPayed(Long orderid, Long shopTicketId){
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> orderMap = daService.getMap("select * from order_tb where id=? and state=? ",
				new Object[]{orderid, 1});
		if(orderMap == null){
			return null;
		}
		Long create_time = (Long)orderMap.get("create_time");
		Long end_time = (Long)orderMap.get("end_time");
		Double total = Double.valueOf(orderMap.get("total") + "");
		map = getComOrderInfo(orderid, shopTicketId, create_time, end_time);

		map.put("createtime", create_time);
		map.put("starttime", TimeTools.getTime_yyyyMMdd_HHmm(create_time * 1000));
		map.put("parktime", StringUtils.getTimeString(create_time, end_time));
		map.put("total", total);
		map.put("uid", orderMap.get("uid"));
		map.put("comid", orderMap.get("comid"));
		map.put("carnumber", orderMap.get("car_number"));
		map.put("shopticketid", shopTicketId);
		map.put("paytype", orderMap.get("pay_type"));
		return map;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getComOrderInfo(Long orderid, Long shopTicketId, Long create_time, Long end_time){
		Map<String, Object> map = new HashMap<String, Object>();
		Double beforetotal = 0d;//减免之前的停车费金额
		Double aftertotal = 0d;//减免之后的停车费金额

		Integer tickettype = 3;//减免券类型，默认减时券
		Integer tickettime = 0;//减时券的时长
		Integer ticketstate = 0;//减免券的状态，0：不可用 1:可用
		Map<String, Object> shopTicketMap = daService.getMap("select * from ticket_tb where orderid=? and (type=? or type=?) ",
				new Object[]{orderid, 3, 4});
		if(shopTicketMap == null){
			if(shopTicketId != null && shopTicketId > 0){
				shopTicketMap = daService.getMap("select * from ticket_tb where id=? and (orderid=? or orderid=?) and state=? and (type=? or type=?) and limit_day>? ",
						new Object[]{shopTicketId, -1, orderid, 0, 3, 4, end_time});
			}
		}else{
			shopTicketId = (Long)shopTicketMap.get("id");
		}
		if(shopTicketMap != null){
			int r = daService.update("update ticket_tb set orderid=? where id=? ", new Object[]{orderid, shopTicketId});
			tickettype = (Integer)shopTicketMap.get("type");
			tickettime = (Integer)shopTicketMap.get("money");
			ticketstate = 1;//该减免券可用
		}
		Map<String, Object> map2 = getPrice(orderid, end_time);
		beforetotal = Double.valueOf(map2.get("beforetotal") + "");
		aftertotal = Double.valueOf(map2.get("aftertotal") + "");
		map.put("beforetotal", beforetotal);
		map.put("aftertotal", aftertotal);
		map.put("ticketstate", ticketstate);
		map.put("tickettype", tickettype);
		map.put("tickettime", tickettime);
		return map;
	}

	/**
	 * 根据用户ID获取是临时账户还是正式账户
	 * @param uin
	 * @return
	 */
	public Integer getBindflag(Long uin){
		Long count = daService.getLong("select count(1) from user_info_tb where id=? ", new Object[]{uin});
		return count.intValue();
	}

	public Integer addCarnumber(Long uin, String carnumber){
		Long cutTime = System.currentTimeMillis()/1000;
		Integer bindflag = getBindflag(uin);
		if(bindflag == 1){
			Long count = daService.getLong("select count(*) from car_info_tb where uin!=? and car_number=? and state=? ",
					new Object[] { uin, carnumber, 1 });
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
						new Object[]{uin, carnumber, cutTime});
				if(r > 0){
					return 1;
				}
			}
		}else if(bindflag == 0){
			int r = daService.update("update wxp_user_tb set car_number=? where uin=? ",
					new Object[]{carnumber, uin});
			if(r > 0){
				return 1;
			}
		}
		return -4;
	}

	/**
	 * 创建工作站，因为需要创建车场的时候创建默认工作站，所以抽象成一个方法了
	 * @param request
	 * @param comid
	 * @param cname
	 * @return
	 */
	public Long createWorksite(HttpServletRequest request, Map<String, Object> map){
		try {
			Long operater = (Long)request.getSession().getAttribute("loginuin");
			Long comid = (Long)map.get("comid");
			Long nextid = daService.getLong(
					"SELECT nextval('seq_com_worksite_tb'::REGCLASS) AS newid", null);
			String sql = "insert into com_worksite_tb(id,comid,worksite_name,description,net_type) values(?,?,?,?,?)";
			int r = daService.update(sql, new Object[]{nextid,comid,map.get("worksite_name"),map.get("description"),map.get("net_type")});
			logger.error("parkadmin or admin:"+operater+" add comid:"+map.get("comid")+" worksite");
			if(r == 1){
				/*
				 * 工作站向下同步的接口暂不开放  2017-07-11
				 * if(publicMethods.isEtcPark(comid)){
					int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_worksite_tb",nextid,System.currentTimeMillis()/1000,0});
					logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" worksite ,add sync ret:"+re);
				}*/
				mongoDbUtils.saveLogs( request,0, 2, "添加了工作站："+map.get("worksite_name"));
				return nextid;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return 0L;
	}

	/**
	 * 创建通道，因为需要创建车场的时候创建默认通道，所以抽象成一个方法了
	 * @param request
	 * @param map
	 * @return
	 */
	public Long createPass(HttpServletRequest request, Map<String, Object> map){
		try {
			Long operater = (Long)request.getSession().getAttribute("loginuin");
			Long comid = (Long)map.get("comid");
			Long nextid = daService.getLong(
					"SELECT nextval('seq_com_pass_tb'::REGCLASS) AS newid", null);
			String sql = "insert into com_pass_tb(id,worksite_id,comid,passname,passtype,description,month_set,month2_set,channel_id) values(?,?,?,?,?,?,?,?,?)";
			int r = daService.update(sql, new Object[]{nextid,map.get("worksite_id"),comid,map.get("passname"),map.get("passtype"),map.get("description"),map.get("month_set"),map.get("month2_set"),nextid+""});
			logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" pass ");
			if(r == 1){
				/*
				 * 添加的通道信息向下同步的接口暂不开放*/
				  if(publicMethods.isEtcPark(comid)){
					int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_pass_tb",nextid,System.currentTimeMillis()/1000,0});
					logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" pass ,add sync ret:"+re);
				}
				mongoDbUtils.saveLogs( request,0, 2, "添加了通道:"+map.get("passname"));
				return nextid;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return 0L;
	}

	/**
	 * 创建摄像头，因为需要创建车场的时候创建默认摄像头，所以抽象成一个方法了
	 * @param request
	 * @param map
	 * @return
	 */
	public Integer createCamera(HttpServletRequest request, Map<String, Object> map){
		try {
			Long nickname = (Long)request.getSession().getAttribute("loginuin");
			Long comid = (Long)map.get("comid");
			//添加
			Long nextid = daService.getLong(
					"SELECT nextval('seq_com_camera_tb'::REGCLASS) AS newid", null);
			String sql = "insert into com_camera_tb(id,passid,camera_name,ip,port,cusername,manufacturer,comid) values(?,?,?,?,?,?,?,?)";
			int re = daService.update(sql, new Object[]{nextid,map.get("passid"),map.get("camera_name"),map.get("ip"),map.get("port"),map.get("cusername"),map.get("manufacturer"),comid});
			if(re == 1){
				/*
				 * 添加的摄像头信息同步下传的接口暂不支持
				 * if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_camera_tb",nextid,System.currentTimeMillis()/1000,0});
					logger.error("parkadmin or admin:"+nickname+" add comid:"+comid+" camera ,add sync ret:"+r);
				}else{
					logger.error("parkadmin or admin:"+nickname+" add comid:"+comid+" camera ");
				}*/
				mongoDbUtils.saveLogs( request,0, 2, "添加了摄像头:"+map.get("camera_name"));
				return 1;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return 0;
	}

	public Integer createLED(HttpServletRequest request, Map<String, Object> map){
		try {
			Long nickname = (Long)request.getSession().getAttribute("loginuin");
			Long comid = (Long)map.get("comid");
			//添加
			Long nextid = daService.getLong(
					"SELECT nextval('seq_com_led_tb'::REGCLASS) AS newid", null);
			String sql = "insert into com_led_tb(id,passid,ledip,ledport,leduid,movemode,movespeed,dwelltime,ledcolor,showcolor,typeface,typesize,matercont,width,height,type,rsport,comid) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			int re = daService.update(sql, new Object[]{nextid,map.get("passid"),map.get("ledip"),map.get("ledport"),map.get("leduid"),map.get("movemode"),map.get("movespeed"),map.get("dwelltime"),map.get("ledcolor"),map.get("showcolor"),map.get("typeface"),map.get("typesize"),map.get("matercont"),map.get("width"),map.get("height"),map.get("type"),map.get("rsport"),comid});

			if(re == 1){
				/*
				 * 添加的LED信息同步下传的接口暂不开放
				 * if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_led_tb",nextid,System.currentTimeMillis()/1000,0});
					logger.error("parkadmin or admin:"+nickname+" add comid:"+comid+" led ,add sync ret:"+r);
				}else{
					logger.error("parkadmin or admin:"+nickname+" add comid:"+comid+" led");
				}*/
				mongoDbUtils.saveLogs(request, 0, 2, "添加了（comid:"+comid+"）的LED："+map.get("ledip")+":"+map.get("ledport"));
				return 1;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return 0;
	}

	/**
	 * 创建新车场时，设置默认的工作站等信息
	 * @param request
	 * @param map
	 */
	public void createDefDevice(HttpServletRequest request, Map<String, Object> map){
		try {
			Long comid = (Long)map.get("comid");
			String cname = (String)map.get("cname");
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("comid", comid);
			params.put("worksite_name", cname);
			params.put("description", cname);
			params.put("net_type", 0);
			Long worksiteid = createWorksite(request, params);
			logger.info("create default worksite successed>>>comid:"+comid+",worksiteid:"+worksiteid);
			if(worksiteid != null && worksiteid > 0){
				params.clear();
				params.put("comid", comid);
				params.put("worksite_id", worksiteid);
				params.put("passname", cname + "入口");
				params.put("passtype", 0);
				params.put("month_set", -1);
				params.put("month2_set", -1);
				Long inpassId = createPass(request, params);
				logger.info("create default inpass successed>>>comid:"+comid+",worksiteid:"+worksiteid+",inpassId:"+inpassId);

				params.clear();
				params.put("comid", comid);
				params.put("worksite_id", worksiteid);
				params.put("passname", cname + "出口");
				params.put("passtype", 1);
				params.put("month_set", -1);
				params.put("month2_set", -1);
				Long outpassId = createPass(request, params);
				logger.info("create default outpass successed>>>comid:"+comid+",worksiteid:"+worksiteid+",outpassId:"+outpassId);

				if(inpassId != null && inpassId > 0){
					params.clear();
					params.put("comid", comid);
					params.put("camera_name", cname + "入口");
					params.put("ip", "192.168.1.201");
					params.put("port", "554");
					params.put("manufacturer", "停车宝");
					params.put("passid", inpassId);
					Integer result = createCamera(request, params);
					logger.info("create default incamera successed>>>comid:"+comid+",inpassId:"+inpassId+",result:"+result);

					params.clear();
					params.put("comid", comid);
					params.put("ledip", "192.168.1.203");
					params.put("ledport", "8888");
					params.put("leduid", "41");
					params.put("movemode", 9);
					params.put("movespeed", 1);
					params.put("dwelltime", 1);
					params.put("ledcolor", 1);
					params.put("showcolor", 0);
					params.put("typeface", 1);
					params.put("typesize", 1);
					params.put("matercont", "停车宝");
					params.put("passid", inpassId);
					params.put("width", 64);
					params.put("height", 32);
					params.put("rsport", 2);
					result = createLED(request, params);
					logger.info("create default inled successed>>>comid:"+comid+",inpassId:"+inpassId+",result:"+result);
				}

				if(outpassId != null && outpassId > 0){
					params.clear();
					params.put("comid", comid);
					params.put("camera_name", cname + "出口");
					params.put("ip", "192.168.1.202");
					params.put("port", "554");
					params.put("manufacturer", "停车宝");
					params.put("passid", outpassId);
					Integer result = createCamera(request, params);
					logger.info("create default incamera successed>>>comid:"+comid+",inpassId:"+inpassId+",result:"+result);

					params.clear();
					params.put("comid", comid);
					params.put("ledip", "192.168.1.204");
					params.put("ledport", "8888");
					params.put("leduid", "41");
					params.put("movemode", 9);
					params.put("movespeed", 1);
					params.put("dwelltime", 1);
					params.put("ledcolor", 1);
					params.put("showcolor", 0);
					params.put("typeface", 1);
					params.put("typesize", 1);
					params.put("matercont", "停车宝");
					params.put("passid", outpassId);
					params.put("width", 64);
					params.put("height", 32);
					params.put("rsport", 2);
					result = createLED(request, params);
					logger.info("create default inled successed>>>comid:"+comid+",inpassId:"+inpassId+",result:"+result);
				}
			}

		} catch (Exception e) {
			logger.error(e);
		}
	}

	/**
	 * @param uin          车主账户
	 * @param total        订单金额
	 * @return             可用停车券列表
	 */
	public List<Map<String,Object>> getUseTickets(Long uin,Double total){
		Long time = System.currentTimeMillis()/1000;
		List<Map<String,Object>> ticketList=pgOnlyReadService.getAll("select id,limit_day as limitday,money,resources," +
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

	/**
	 *
	 * @param comid
	 * @param btime
	 * @param etime
	 * @return
	 * @time 2017年 下午4:48:20
	 * @author QuanHao
	 */
	public String getParkTotalStatistic(Long comid, Long btime, Long etime){
		double cash = 0.0;
		double wallet = 0.0;
		double total = 0.0;
		Integer month = 0;
		String sql = "select total,cash_pay,(electronic_prepay+electronic_pay) electronic_pay,reduce_amount reduce_pay,pay_type from order_tb where comid = ? and end_time between ? and ?" +
				"and state= ? and out_uid> ? and ishd=?";
		List<Map<String, Object>> parkList = pgOnlyReadService.getAll(sql, new Object[]{comid,btime,etime,1,0,0});
		for (Map<String, Object> map : parkList) {
			Integer payType = (Integer) map.get("pay_type");
			double totalmoney = StringUtils.formatDouble(map.get("total"));
			total += totalmoney;

			double cash_pay = StringUtils.formatDouble(map.get("cash_pay"));
			double electronic_pay = StringUtils.formatDouble(map.get("electronic_pay"));
			double reduce_pay = StringUtils.formatDouble(map.get("reduce_pay"));
			//详细金额信息都不记录，统计total(畅盈长沙)
			if((Check.isEmpty(cash_pay+"") || cash_pay == 0)
					&& (Check.isEmpty(electronic_pay+"") || electronic_pay == 0)
					&&  (Check.isEmpty(reduce_pay+"") || reduce_pay == 0)){
				if(payType == 1){
					cash += totalmoney;
				}else if(payType == 2){
					wallet += totalmoney;
				}
			}else{
				cash += cash_pay;
				wallet += electronic_pay;
			}
			if(payType==3){
				month += 1;
			}
		}
		return cash+"_"+month+"_"+wallet+"_"+total;
	}

	/**
	 * 获取某个收费员的减免券，中央预支付、现金金额
	 * @param uid
	 * @param comid
	 * @param btime
	 * @param etime
	 * @param ishd
	 * @return
	 */
	public String getTicketAndCenterPay(Long uid, Long btime, Long etime,Integer ishd,Long comid) {
		double ticket = 0.0;
		double Center = 0.0;
		double cash = 0.0;
		Double pmoney = 0d;
//		String sql1 = "select sum(b.amount)money from order_tb a,parkuser_cash_tb b where  a.end_time between" +
//				" ? and ? and a.state=? and a.uid=? and a.id=b.orderid and b.type=?";
		String sql1 = "select sum(c.amount)money from (select distinct(orderid,amount),orderid,amount from order_tb a," +
				"parkuser_cash_tb b where a.comid=? and  a.end_time between ? and ? and a.state=? and a.out_uid=?  " +
				"and a.id=b.orderid and b.type=?  ";

		Object [] v1 = new Object[]{comid,btime,etime,1,uid,0};
		if(ishd!=null&&ishd==1){
			sql1 += " and ishd=? ";
			v1 = new Object[]{comid,btime,etime,1,uid,0,0};
		}
		sql1+=" order by orderid)c";
		Map cashmap = pgOnlyReadService.getMap(sql1,v1);
		if(cashmap!=null&&cashmap.get("money")!=null){
			cash = Double.valueOf(cashmap.get("money")+"");
		}

		String sql2 = "select sum(b.amount)money from order_tb a,parkuser_cash_tb b where a.comid=? and  a.end_time between" +
				" ? and ? and a.state=? and a.out_uid=? and a.id=b.orderid and b.type=? ";
		Object [] v2 = new Object[]{comid,btime,etime,1,uid,1};
		if(ishd!=null&&ishd==1){
			sql2 += " and ishd=? ";
			v2 = new Object[]{comid,btime,etime,1,uid,1,0};
		}

		Map centermap = pgOnlyReadService.getMap(sql2,v2);
		if(centermap!=null&&centermap.get("money")!=null){
			Center = Double.valueOf(centermap.get("money")+"");
		}

		String sql3 = "select sum(b.umoney)money from order_tb a,ticket_tb b where a.comid=? and  a.end_time between" +
				" ? and ? and a.state=? and a.out_uid=? and a.id=b.orderid and (b.type=3 or b.type=4)";
		Object [] v3 =new Object[]{comid,btime,etime,1,uid};
		if(ishd!=null&&ishd==1){
			sql3 += " and ishd=? ";
			v3 = new Object[]{comid,btime,etime,1,uid,0};
		}

		Map tickethmap = pgOnlyReadService.getMap(sql3,v3);
		if(tickethmap!=null&&tickethmap.get("money")!=null){
			ticket = Double.valueOf(tickethmap.get("money")+"");
		}

		Map park = pgOnlyReadService.getMap( "select sum(a.amount) total from order_tb o,park_account_tb a where o.id=a.orderid and o.end_time between ? and ? " +
				" and a.type= ? and a.source=? and a.uid=? ",new Object[]{btime,etime,0,0,uid});
		if(park!=null&&park.get("total")!=null)
			pmoney += Double.valueOf(park.get("total")+"");

		Map parkuser = pgOnlyReadService.getMap( "select sum(a.amount) total from order_tb o,parkuser_account_tb a where o.id=a.orderid and o.comid=? and o.end_time between ? and ? " +
				" and a.type= ? and a.uin = ? and a.target =? and a.remark like ? ",new Object[]{comid,btime,etime,0,uid,4,"停车费%"});//target=4包括停车费和打赏
		if(parkuser!=null&&parkuser.get("total")!=null)
			pmoney += Double.valueOf(parkuser.get("total")+"");

		return ticket+"_"+Center+"_"+cash+"_"+pmoney;
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
		List<Map<String, Object>> list = pgOnlyReadService.getAll("select * from ticket_tb where uin = ? and state=? and limit_day>=? and type<? and money<=? and resources>=?  order by money ",
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
			Map<String, Object> comMap = pgOnlyReadService.getMap(
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
					map.put("desc", "满"+map.get("full")+"元可以抵扣全额,过期后退还"+backmoney+"元至您的账户");
				}else{
					map.put("desc", "满"+map.get("full")+"元可以抵扣全额");
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
		Map<String, Object> map = daService.getMap("select car_type from com_info_tb where id=? ", new Object[]{comid});
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();

		if(map != null){
			Integer car_type = (Integer)map.get("car_type");
			if(car_type != 0){
				List<Map<String, Object>> list = pgOnlyReadService.getAll("select id as value_no,name as value_name from car_type_tb where comid=? order by sort , id desc ", new Object[]{comid});
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
	//----------------选券逻辑end--------------------//
	/**
	 * 获取监控器
	 * @param comid
	 * @return
	 */

	public List<Map<String, Object>> getMonitors(String comid,String groupid){
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			List<Object> params = new ArrayList<Object>();
			String sql = "";
			if(!Check.isEmpty(comid) && !"0".equals(comid)&& !"-1".equals(comid)){
				sql = "select id as value_no,name as value_name from monitor_info_tb where comid=? and state=?";
				params.add(comid);
				params.add(1);
			}else{
				List<Map<String, Object>> parks = getParkList(Long.parseLong(groupid));
				sql = "select id as value_no,name as value_name from monitor_info_tb where comid in (";
				for(int i=0; i<parks.size();i++){
					sql += " ?";
					if(i!=parks.size()-1){
						sql += ",";
					}
					params.add(parks.get(i).get("value_no").toString());
				}
				sql += " ) and state=?";
				params.add(1);
			}
			list = pgOnlyReadService.getAllMap(sql, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	public List<Map<String, Object>> getParkList(Long groupid){
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			List<Object> params = new ArrayList<Object>();
			String sql = "select id as value_no, company_name as value_name from com_info_tb where state<>? and groupid=? " ;
			params.add(1);
			params.add(groupid);
			list = pgOnlyReadService.getAllMap(sql, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	public List<Map<String, Object>> getChannels(String comid,String groupid){
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			List<Object> params = new ArrayList<Object>();
			String sql = "";
			if(!Check.isEmpty(comid) && !"0".equals(comid)&& !"-1".equals(comid)){
				//sql = "SELECT DISTINCT (cp. ID) AS value_no, cp.passname AS value_name,mi.id FROM com_pass_tb cp,monitor_info_tb mi WHERE cp.id = mi.channel_id and cp.comid=?";
				sql = "SELECT DISTINCT (cp. ID) AS value_no, cp.passname AS value_name FROM com_pass_tb cp WHERE cp.comid=?";
				params.add(Long.valueOf(comid));
			}else{
				List<Map<String, Object>> parks = getParkList(Long.parseLong(groupid));
				//sql = "SELECT DISTINCT (cp. ID) AS value_no, cp.passname AS value_name,mi.id FROM com_pass_tb cp,monitor_info_tb mi WHERE cp.id = mi.channel_id and cp.comid in (";
				sql = "SELECT DISTINCT (cp. ID) AS value_no, cp.passname AS value_name FROM com_pass_tb cp WHERE cp.comid in (";
				for(int i=0; i<parks.size();i++){
					sql += " ?";
					if(i!=parks.size()-1){
						sql += ",";
					}
					params.add(Long.parseLong(parks.get(i).get("value_no").toString()));
				}
				sql += " )";
			}
			list = pgOnlyReadService.getAllMap(sql, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
