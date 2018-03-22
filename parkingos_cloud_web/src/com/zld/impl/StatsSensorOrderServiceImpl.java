package com.zld.impl;

import com.zld.pojo.*;
import com.zld.service.PgOnlyReadService;
import com.zld.service.StatsOrderService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service("sensorOrder")
public class StatsSensorOrderServiceImpl implements StatsOrderService {
	@Autowired
	private PgOnlyReadService readService;

	Logger logger = Logger.getLogger(StatsSensorOrderServiceImpl.class);
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
				column = "out_uid";//按收费员编号统计
			}else if(type == 1){
				column = "comid";//按车场编号统计
			}else if(type == 2){
				column = "berthseg_id";//按泊位段编号统计
			}else if(type == 3){
				column = "dici_id";//按泊位编号统计
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
			params.add(1);//已结算
			params.add(startTime);
			params.add(endTime);
			params.addAll(idList);
			String sql = "select sum(total) summoney,"+column+" from berth_order_tb where " +
					" is_delete=? and state=? and out_time between ? and ? and "+column+
					" in ("+preParams+") group by "+column;
			List<Map<String, Object>> list = readService.getAllMap(sql, params);
			if(list != null && !list.isEmpty()){
				List<StatsOrder> orders = new ArrayList<StatsOrder>();
				for(Map<String, Object> map : list){
					Long id = (Long)map.get(column);
					Double summoney = Double.valueOf(map.get("summoney") + "");

					StatsOrder order = new StatsOrder();
					order.setId(id);
					order.setSensorFee(summoney);
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
	@Override
	public AccountResp order(AccountReq req) {
		// TODO Auto-generated method stub
		return null;
	}

}
