package com.zldpark.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.zldpark.service.PgOnlyReadService;
import com.zldpark.utils.SqlInfo;


@Repository
public class CommonMethods {
	private Logger logger = Logger.getLogger(CommonMethods.class);
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	
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
}
