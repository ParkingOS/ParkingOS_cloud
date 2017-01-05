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
 * 电子结算订单
 * @author whx
 *
 */
@Service("payEpay")
public class PayPosOrderEpayServiceImpl implements PayPosOrderService {
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private PgOnlyReadService readService;
	@Autowired
	private DataBaseService writeService;
	
	Logger logger = Logger.getLogger(PayPosOrderEpayServiceImpl.class);
	
	/**
	 * 结算电子预支付金额足够的订单，或者电子支付没有预支付过的订单
	 */
	@Override
	public AutoPayPosOrderResp autoPayPosOrder(AutoPayPosOrderReq req) {
		AutoPayPosOrderResp resp = new AutoPayPosOrderResp();
		try {
			logger.error("req:"+req.toString());
			//----------------------------参数--------------------------------//
			Long curTime = req.getCurTime();
			Order order = req.getOrder();
			Double money = req.getMoney();//总金额
			String imei = req.getImei();//手机串号
			Long workId = req.getWorkId();//当前上班记录
			Long uid = req.getUid();//收费员编号
			Integer version = req.getVersion();//版本号
			Long userId = req.getUserId();//用户编号
			Long brethOrderId = req.getBerthOrderId();
			Long endTime = req.getEndTime();
			//----------------------------校验参数--------------------------------//
			if(order == null 
					|| uid <= 0 
					|| workId <= 0 
					|| money < 0
					|| curTime == null){//money可以为零
				resp.setResult(-1);
				resp.setErrmsg("参数错误");
				return resp;//参数错误
			}
			//----------------------------获取订单信息--------------------------------//
			Map<String, Object> orderMap = new org.apache.commons.beanutils.BeanMap(order);
			logger.error("orderMap:"+orderMap);
			Integer state = (Integer)orderMap.get("state");
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
			Double prepay = Double.valueOf(orderMap.get("prepaid") + "");
			Integer pay_type = (Integer)orderMap.get("pay_type");
			long orderId = order.getId();
			//-----------------------------逻辑处理----------------------------------//
			if(userId > 0){
				boolean result = false;
				if(prepay >= money && pay_type == 2){
					logger.error("电子与预支付足够>>>orderid:"+order.getId());
					int r = publicMethods.doPrePayOrder(orderMap, money);
					logger.error("r:"+r);
					if(r == 1){
						result = true;
					}
				}else if(prepay == 0 && pay_type == 0){//未有预支付过的订单电子支付
					logger.error("尝试电子支付>>>orderid:"+order.getId());
					//查车主配置，是否设置了自动支付。没有配置时，默认25元以下自动支付 
					Integer autoCash = 1;//默认自动支付
					Integer limitMoney = 25;//默认是最高自动支付25元
					Map<String, Object> upMap = readService.getPojo("select auto_cash,limit_money " +
							" from user_profile_tb where uin =?", new Object[]{userId});
					if(upMap !=null && upMap.get("auto_cash") != null){
						autoCash = (Integer)upMap.get("auto_cash");
						limitMoney = (Integer)upMap.get("limit_money");//-1表示没有上限
					}
					logger.error("autoCash:"+autoCash+",limitMoney:"+limitMoney);
					if(autoCash == 1 
							&& (limitMoney == -1 || limitMoney >= money)){
						int r = publicMethods.payOrder(orderMap, money, userId, 2, 0,
								-1L, null, -1L, uid);
						logger.error("r:"+r);
						if(r == 5){
							result = true;
						}
					}
				}
				logger.error("result:"+result);
				if(result){
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
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
					logger.error("b:"+b+",orderid:"+orderId);
					resp.setResult(1);
					resp.setErrmsg("电子支付结算订单成功");
					return resp;
				}
			}
			resp.setResult(0);
			resp.setErrmsg("订单结算失败");
		} catch (Exception e) {
			logger.error(e);
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
