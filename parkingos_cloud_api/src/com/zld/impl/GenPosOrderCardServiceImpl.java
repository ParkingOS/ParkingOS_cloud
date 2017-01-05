package com.zld.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zld.pojo.Berth;
import com.zld.pojo.Card;
import com.zld.pojo.DefaultCardReq;
import com.zld.pojo.DefaultCardResp;
import com.zld.pojo.GenPosOrderReq;
import com.zld.pojo.GenPosOrderResp;
import com.zld.service.CardService;
import com.zld.service.DataBaseService;
import com.zld.service.GenPosOrderService;
import com.zld.service.PgOnlyReadService;

@Service("genCard")
public class GenPosOrderCardServiceImpl implements GenPosOrderService {
	@Autowired
	private DataBaseService writeService;
	@Autowired
	private PgOnlyReadService readService;
	@Autowired
	private CardService cardService;
	@Autowired
	private CommonMethods commonMethods;
	
	Logger logger = Logger.getLogger(GenPosOrderCardServiceImpl.class);
	@Override
	public GenPosOrderResp genPosOrder(GenPosOrderReq req) {
		GenPosOrderResp resp = new GenPosOrderResp();
		try {
			logger.error(req.toString());
			Long orderId = req.getOrderId();//订单号
			String carNumber = req.getCarNumber();//车牌号
			Berth berth = req.getBerth();//泊位
			String imei = req.getImei();
			Long uid = req.getUid();//收费员编号
			Long userId = req.getUserId();//车主编号
			Long workId = req.getWorkId();//上班编号
			Long berthOrderId = req.getBerthOrderId();//需要绑定的车检器订单编号
			Long startTime = req.getStartTime();
			Integer cType = req.getcType();//订单生成方式 2：录入车牌 5：月卡会员
			Integer carType = req.getCarType();//车辆类型
			Integer version = req.getVersion();//版本号
			Long parkId = req.getParkId();//车场编号
			Long groupId = req.getGroupId();//运营集团编号
			Long curTime = req.getCurTime();//当前时间
			//-------------------------预付参数-----------------------//
			String nfc_uuid = req.getNfc_uuid();
			Integer bindcard = req.getBindcard();
			Double prepay = req.getPrepay();//预支付金额
			//-------------------------校验参数----------------------//
			if(orderId <= 0 
					|| carNumber == null
					|| "".equals(carNumber)
					|| berth == null
					|| workId <= 0
					|| startTime <= 0
					|| uid <= 0
					|| parkId <= 0
					|| carType < 0
					|| groupId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("参数错误");
				return resp;
			}
			//-------------------------卡片信息----------------------//
			Card card = null;
			if(nfc_uuid == null || "".equals(nfc_uuid)){//没有刷卡,取默认的卡片
				if(parkId > 0){
					DefaultCardReq defaultCardReq = new DefaultCardReq();
					defaultCardReq.setParkId(parkId);
					defaultCardReq.setUserId(userId);
					defaultCardReq.setCarNumber(carNumber);
					DefaultCardResp defaultCardResp = cardService.getDefaultCard(defaultCardReq);
					if(defaultCardResp.getResult() == 1 
							&& defaultCardResp.getCard() != null){
						card = defaultCardResp.getCard();
					}
				}
			}else{//刷卡
				card = commonMethods.card(nfc_uuid, groupId);
			}
			if(card == null){
				resp.setResult(-7);
				resp.setErrmsg("该卡片未开卡，或不属于当前运营集团");
				return resp;
			}
			logger.error(card.toString());
			if(card.getGroup_id() <= 0){//卡片没有所属运营集团编号
				resp.setResult(-8);
				resp.setErrmsg("卡片信息错误");
				return resp;
			}
			if(card.getState() == 1){//注销状态
				resp.setResult(-10);
				resp.setErrmsg("卡片已注销，需重新开卡");
				return resp;
			}
			if(card.getState() == 3){//开卡状态
				resp.setResult(-5);
				resp.setErrmsg("卡片未激活");
				return resp;
			}
			if(bindcard == 0 && card.getState() == 0){//激活状态
				resp.setResult(-6);
				resp.setErrmsg("卡片未绑定用户");
				return resp;
			}
			
			if(card.getBalance() < prepay){//余额不足直接返回，书鱼决定的
				resp.setResult(-11);
				resp.setErrmsg("余额不足，卡片余额:"+card.getBalance()+"元");
				return resp;
			}
			
			//-------------------------预付类型----------------------//
			int pay_type = 9;//9：刷卡
			//-------------------------具体逻辑----------------------//
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//生成订单
			Map<String, Object> orderSqlMap = new HashMap<String, Object>();
			//班次工作记录表
			Map<String, Object> workDetailSqlMap = new HashMap<String, Object>();
			//卡片
			Map<String, Object> cardSqlMap = new HashMap<String, Object>();
			//卡片消费流水
			Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();
			//更新泊位订单表的绑定状态
			Map<String, Object> berthOrderSqlMap = new HashMap<String, Object>();
			//更新泊位表状态
			Map<String, Object> berthSqlMap = new HashMap<String, Object>();
			
			orderSqlMap.put("sql", "insert into order_tb (id,comid,groupid,berthsec_id,uin,state," +
					"create_time,nfc_uuid,c_type,uid,imei,car_number,berthnumber,prepaid," +
					"prepaid_pay_time,pay_type,car_type) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			orderSqlMap.put("values", new Object[]{orderId, parkId, groupId, berth.getBerthsec_id(),
					userId, 0, curTime, card.getNfc_uuid(), cType, uid, imei, carNumber, berth.getId(), prepay, 
					curTime, pay_type, carType});//关于收费的字段，包括明细，都要取当前时间内，否则在一个班次内有可能对不起帐来
			//注意订单里存储的不是参数nfc_uuid而是卡片里面的nfc_uuid，这俩不一样
			bathSql.add(orderSqlMap);
			
			workDetailSqlMap.put("sql", "insert into work_detail_tb (uid,orderid,bid,workid,berthsec_id) " +
					"values(?,?,?,?,?)");
			workDetailSqlMap.put("values", new Object[]{uid, orderId, berth.getId(), workId,
					berth.getBerthsec_id()});
			bathSql.add(workDetailSqlMap);
			
			if(prepay > 0){//商家卡刷卡
				cardSqlMap.put("sql", "update com_nfc_tb set balance=balance-? where id=? ");
				cardSqlMap.put("values", new Object[]{prepay, card.getId()});
				bathSql.add(cardSqlMap);
				cardAccountSqlMap.put("sql", "insert into card_account_tb(uin,card_id,type,consume_type," +
						"amount,create_time,remark,orderid,uid,comid,berthseg_id,berth_id,groupid) " +
						"values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
				cardAccountSqlMap.put("values", new Object[]{userId, card.getId(), 1, 1, prepay, curTime,
						"预付停车费 " + carNumber, orderId, uid, parkId, berth.getBerthsec_id(), berth.getId(), groupId});
				bathSql.add(cardAccountSqlMap);
			}
			if(berthOrderId > 0){//绑定车检器订单
				berthOrderSqlMap.put("sql", "update berth_order_tb set orderid=?,in_uid=? where id=? ");
				berthOrderSqlMap.put("values", new Object[]{orderId, uid, berthOrderId});
				bathSql.add(berthOrderSqlMap);
			}
			berthSqlMap.put("sql", "update com_park_tb set order_id=?,state=?,enter_time=? where id=? ");
			berthSqlMap.put("values", new Object[]{orderId, 1, curTime, berth.getId()});
			bathSql.add(berthSqlMap);
			boolean b = writeService.bathUpdate2(bathSql);
			if(b){
				resp.setResult(1);
				resp.setErrmsg("进场成功，正在打印进场凭条...");
				return resp;
			}
			resp.setResult(-12);
			resp.setErrmsg("进场失败");
		} catch (Exception e) {
			logger.error(e);
			resp.setResult(-1);
			resp.setErrmsg("系统错误");
		}
		return resp;
	}

}
