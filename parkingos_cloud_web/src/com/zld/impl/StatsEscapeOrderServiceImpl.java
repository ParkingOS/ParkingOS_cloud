package com.zld.impl;

import com.zld.pojo.*;
import com.zld.service.PgOnlyReadService;
import com.zld.service.StatsOrderService;
import com.zld.utils.ExecutorsUtil;
import com.zld.utils.SqlInfo;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
@Service("escapeOrder")
public class StatsEscapeOrderServiceImpl implements StatsOrderService {
	@Autowired
	private PgOnlyReadService readService;

	Logger logger = Logger.getLogger(StatsEscapeOrderServiceImpl.class);

	@Override
	public StatsOrderResp statsOrder(StatsReq req) {
		//logger.error(req.toString());
		StatsOrderResp resp = new StatsOrderResp();
		try {
			long startTime = req.getStartTime();
			long endTime = req.getEndTime();
			List<Object> idList = req.getIdList();
			int type = req.getType();//0：按收费员编号统计 1：按车场编号统计 2：按泊位段编号查询 3：按泊位查询
			if(startTime <= 0
					|| endTime <= 0
					|| idList == null
					|| idList.isEmpty()){
				resp.setResult(-1);
				resp.setErrmsg("参数错误");
				return resp;
			}
			String column = null;
			if(type == 0){
				column = "uid";//按收费员编号统计
			}else if(type == 1){
				column = "comid";//按车场编号统计
			}else if(type == 2){
				column = "berthseg_id";//按泊位段编号统计
			}else if(type == 3){
				column = "berth_id";//按泊位编号统计
			}else if(type == 4){
				column = "groupid";
			}
			if(column == null){
				resp.setResult(-1);
				resp.setErrmsg("参数错误");
				return resp;
			}
			String preParams = "";
			for(int i = 0; i<idList.size(); i++){
				if(i == 0){
					preParams ="?";
				}else{
					preParams += ",?";
				}
			}
			List<Object> params = new ArrayList<Object>();
			params.add(0);//状态正常
			params.add(0);//未追缴状态
			params.add(startTime);
			params.add(endTime);
			params.addAll(idList);
			String sql = "select sum(total-prepay) summoney,"+column+" from no_payment_tb where" +
					" is_delete=? and state=? and end_time between ? and ? and "+column+
					" in ("+preParams+") group by "+column;
			List<Map<String, Object>> list = readService.getAllMap(sql, params);
			if(list != null && !list.isEmpty()){
				List<StatsOrder> orders = new ArrayList<StatsOrder>();
				for(Map<String, Object> map : list){
					Long id = (Long)map.get(column);
					Double summoney = Double.valueOf(map.get("summoney") + "");

					StatsOrder order = new StatsOrder();
					order.setId(id);
					order.setEscapeFee(summoney);
					orders.add(order);
				}
				resp.setOrders(orders);
				return resp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.setResult(-1);
		resp.setErrmsg("系统错误");
		return resp;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public AccountResp order(AccountReq req) {
		//logger.error(req.toString());
		AccountResp resp = new AccountResp();
		try {
			ExecutorService pool = ExecutorsUtil.getExecutorService();//获取线程池
			long startTime = req.getStartTime();
			long endTime = req.getEndTime();
			long id = req.getId();
			int pageNum = req.getPageNum();
			int pageSize = req.getPageSize();
			int type = req.getType();//0：按收费员编号统计 1：按车场编号统计 2：按泊位段编号查询 3：按泊位查询
			SqlInfo sqlInfo = req.getSqlInfo();
			if(startTime <= 0
					|| endTime <= 0
					|| id <= 0){
				resp.setResult(-1);
				resp.setErrmsg("参数错误");
				return resp;
			}
			String column = null;
			if(type == 0){
				column = "uid";//按收费员编号统计
			}else if(type == 1){
				column = "comid";//按车场编号统计
			}else if(type == 2){
				column = "berthseg_id";//按泊位段编号统计
			}else if(type == 3){
				column = "berth_id";//按泊位编号统计
			}else if(type == 4){
				column = "groupid";
			}
			if(column == null){
				resp.setResult(-1);
				resp.setErrmsg("参数错误");
				return resp;
			}
			ArrayList<Object> params = new ArrayList<Object>();
			params.add(0);//状态正常
			params.add(0);//未追缴状态
			params.add(startTime);
			params.add(endTime);
			params.add(id);
			String sql = "select * from no_payment_tb where is_delete=? and state=? and end_time" +
					" between ? and ? and " +column+ "=? ";
			String countSql = "select count(id) from no_payment_tb where is_delete=? and state=? and end_time" +
					" between ? and ? and " +column+ "=? ";
			if(sqlInfo != null) {
				countSql += " and "+ sqlInfo.getSql();
				sql += " and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			sql += " order by end_time desc ";
			QueryCount queryCount = new QueryCount(readService, countSql, params);
			QueryList queryList = new QueryList(readService, sql, params, pageNum, pageSize);
			Future<Long> future0 = pool.submit(queryCount);
			Future<List> future1 = pool.submit(queryList);
			Long count = future0.get();
			List list = future1.get();
			resp.setCount(count);
			resp.setList(list);
			resp.setResult(1);
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.setResult(-1);
		resp.setErrmsg("系统错误");
		return resp;
	}

}
