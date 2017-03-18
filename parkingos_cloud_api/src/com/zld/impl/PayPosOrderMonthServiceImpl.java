package com.zld.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zld.pojo.AutoPayPosOrderReq;
import com.zld.pojo.AutoPayPosOrderResp;
import com.zld.pojo.ManuPayPosOrderReq;
import com.zld.pojo.ManuPayPosOrderResp;
import com.zld.pojo.Order;
import com.zld.pojo.PayEscapePosOrderReq;
import com.zld.pojo.PayEscapePosOrderResp;
import com.zld.service.DataBaseService;
import com.zld.service.PayPosOrderService;
import com.zld.service.PgOnlyReadService;
/**
 * 结算月卡订单
 * @author whx
 *
 */
@Service("payMonth")
public class PayPosOrderMonthServiceImpl implements PayPosOrderService {
	@Autowired
	private DataBaseService writeService;
	
	Logger logger = Logger.getLogger(PayPosOrderMonthServiceImpl.class);
	
	/**
	 * 自动结算月卡订单
	 */
	@Override
	public AutoPayPosOrderResp autoPayPosOrder(AutoPayPosOrderReq req) {
		AutoPayPosOrderResp resp = new AutoPayPosOrderResp();
		try {
			logger.error(req.toString());
			Long curTime = req.getCurTime();
			Order order = req.getOrder();
			Long uid = req.getUid();
			String imei = req.getImei();
			Long brethOrderId = req.getBerthOrderId();
			Long endTime = req.getEndTime();
			Integer payType = req.getPayType();
			if(order == null 
					|| uid <= 0 
					|| endTime == null 
					|| curTime == null){
				resp.setResult(-1);
				resp.setErrmsg("参数错误");
				return resp;
			}
			//-----------------------------获取订单信息-----------------------------//
			logger.error(order.toString());
			long orderId = order.getId();
			int cType = order.getC_type();//5月卡用户
			double money = 0;//结算金额
			int state = order.getState();
			if(state == 1){
				resp.setResult(-2);
				resp.setErrmsg("订单已结算");
				return resp;
			}
			if(state == 2){
				resp.setResult(-3);
				resp.setErrmsg("订单已置为逃单");
				return resp;
			}
			//-------------------------------具体逻辑-----------------------------//
			logger.error("orderid:"+orderId+",cType:"+cType);
			if(cType == 5 || payType==10){//月卡或已在泊链支付的订单
				List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
				//更新订单状态
				Map<String, Object> orderSqlMap = new HashMap<String, Object>();
				orderSqlMap.put("sql", "update order_tb set state=?,total=?,end_time=?" +
						",pay_type=?,imei=?,out_uid=? where id=?");
				orderSqlMap.put("values", new Object[]{1, money, endTime, payType, imei, uid, orderId});
				bathSql.add(orderSqlMap);
				if(order.getBerthnumber() > 0){
					//更新泊位状态
					Map<String, Object> berthSqlMap = new HashMap<String, Object>();
					berthSqlMap.put("sql", "update com_park_tb set state=?,order_id=?," +
							"end_time=? where id =? and order_id=?");
					berthSqlMap.put("values", new Object[]{0, null, endTime, order.getBerthnumber(), orderId});
					bathSql.add(berthSqlMap);
				}
				if(brethOrderId > 0){
					//更新车检器订单状态
					Map<String, Object> berthOrderSqlMap = new HashMap<String, Object>();
					berthOrderSqlMap.put("sql", "update berth_order_tb set out_uid=?,order_total=? where id=? ");
					berthOrderSqlMap.put("values", new Object[]{uid, money, brethOrderId});
					bathSql.add(berthOrderSqlMap);
				}
				boolean b = writeService.bathUpdate2(bathSql);
				logger.error("payMonthOrder b :" + b + ",orderid:" + orderId + ",brethOrderid:" + brethOrderId);
				if(b){
					resp.setResult(1);
					resp.setErrmsg("月卡订单结算成功");
					return resp;
				}
			}
			resp.setResult(0);
			resp.setErrmsg("月卡订单结算失败");
		} catch (Exception e) {
			resp.setResult(-4);
			resp.setErrmsg("系统错误");
		}
		return resp;
	}

	@Override
	public ManuPayPosOrderResp manuPayPosOrder(ManuPayPosOrderReq req) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PayEscapePosOrderResp payEscapePosOrder(PayEscapePosOrderReq req) {
		// TODO Auto-generated method stub
		return null;
	}

}
