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
import com.zld.pojo.Card;
import com.zld.pojo.DefaultCardReq;
import com.zld.pojo.DefaultCardResp;
import com.zld.pojo.ManuPayPosOrderReq;
import com.zld.pojo.ManuPayPosOrderResp;
import com.zld.pojo.Order;
import com.zld.pojo.PayEscapePosOrderReq;
import com.zld.pojo.PayEscapePosOrderResp;
import com.zld.service.CardService;
import com.zld.service.DataBaseService;
import com.zld.service.PayPosOrderService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.Check;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;

/**
 * 刷商家卡结算订单
 * @author Administrator
 *
 */
@Service("payCard")
public class PayPosOrderCardServiceImpl implements PayPosOrderService {
	@Autowired
	private CardService cardService;
	@Autowired
	private DataBaseService writeService;
	@Autowired
	private PgOnlyReadService readService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private PublicMethods publicMethods;
	
	Logger logger = Logger.getLogger(PayPosOrderCardServiceImpl.class);
	
	/**
	 * 进场刷卡，出场自动结算订单
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
			Long brethOrderId = req.getBerthOrderId();
			Long endTime = req.getEndTime();
			long userId = req.getUserId();
			Long groupId = req.getGroupId();
			//----------------------------校验参数--------------------------------//
			if(order == null 
					|| uid <= 0 
					|| workId <= 0 
					|| money < 0
					|| endTime == null
					|| curTime == null
					|| groupId <= 0){//money可以为零
				resp.setResult(-2);
				resp.setErrmsg("参数错误");
				return resp;//参数错误
			}
			//----------------------------获取订单信息--------------------------------//
			logger.error("order:"+order.toString());
			if(order.getState() != 0){
				resp.setResult(-3);
				resp.setErrmsg("订单已处理");
				return resp;
			}
			long orderId = order.getId();
			double prepay = order.getPrepaid();
			String carNumber = order.getCar_number();
			String nfc_uuid = order.getNfc_uuid();
			int pay_type = order.getPay_type();
			long parkId = order.getComid();
			long berthId = order.getBerthnumber();
			long berthSegId = order.getBerthsec_id();
			if(parkId <= 0){
				resp.setResult(-4);
				resp.setErrmsg("订单信息错误");
				return resp;
			}
			//----------------------------获取卡片信息--------------------------------//
			Card card = null;
			if(pay_type == 9 && nfc_uuid != null && !"".equals(nfc_uuid)){
				logger.error("刷卡预支付过>>>orderid:"+orderId);
				card = readService.getPOJO("select * from com_nfc_tb where nfc_uuid=? " +
						" and is_delete=? and type=? and state<>? and group_id=? limit ? ", 
						new Object[]{nfc_uuid, 0, 2, 1, groupId, 1}, Card.class);
				//state 0：激活 1：注销  2：绑定用户 3：开卡（此时的卡片还不能用，要激活后才可使用）
				//这里的nfc_uuid用等于，因为order_tb里存储的是就是完整的nfc_uuid
				if(card == null){
					logger.error("没有找到卡片信息>>>orderid:"+orderId);
					resp.setResult(-7);
					resp.setErrmsg("订单信息错误");
					return resp;
				}
			}else if(pay_type == 0 && prepay == 0){
				logger.error("没有预支付过，尝试获取一个默认的卡片>>>orderid:"+orderId);
				DefaultCardReq defaultCardReq = new DefaultCardReq();
				defaultCardReq.setParkId(parkId);
				defaultCardReq.setUserId(userId);
				defaultCardReq.setCarNumber(carNumber);
				DefaultCardResp defaultCardResp = cardService.getDefaultCard(defaultCardReq);
				if(defaultCardResp.getResult() == 1 
						&& defaultCardResp.getCard() != null){
					card = defaultCardResp.getCard();
					pay_type = 9;//置为刷卡支付方式
					logger.error("pay_type:"+pay_type+"card:"+card.toString());
				}
			}
			//----------------------------具体逻辑--------------------------------//
			logger.error("orderid:"+orderId+"pay_type:"+pay_type+",prepay:"+prepay
					+",money:"+money);
			if(card != null){//刷卡进场的并且金额足够的
				double balance = card.getBalance();
				logger.error("orderid:"+orderId+"card:"+card.toString()+",balance:"+balance);
				if(pay_type == 9 && prepay + balance >= money){//刷卡预支付或者未预支付
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					//更新订单状态
					Map<String, Object> orderSqlMap = new HashMap<String, Object>();
					orderSqlMap.put("sql", "update order_tb set state=?,total=?,end_time=?," +
							"pay_type=?,imei=?,out_uid=?,nfc_uuid=? where id=?");
					orderSqlMap.put("values", new Object[]{1, money, endTime, pay_type, imei,
							uid, card.getNfc_uuid(), orderId});
					bathSql.add(orderSqlMap);
					if(prepay < money){
						int consume_type = 0;//-- 消费方式 0：支付停车费（非预付） 1：预付停车费 2：补缴停车费
						String remark = "停车费 " + carNumber;
						if(prepay > 0){//有预付
							consume_type = 2;
							remark = "补缴停车费 " + carNumber;
						}
						Double pursueMoney = StringUtils.formatDouble(money - prepay);
						Map<String, Object> cardSqlMap = new HashMap<String, Object>();
						cardSqlMap.put("sql", "update com_nfc_tb set balance=balance-? where id=? ");
						cardSqlMap.put("values", new Object[]{pursueMoney, card.getId()});
						bathSql.add(cardSqlMap);
						Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();
						cardAccountSqlMap.put("sql", "insert into card_account_tb(uin,card_id,type,consume_type," +
								"amount,create_time,remark,orderid,uid,comid,berthseg_id,berth_id,groupid)" +
								" values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
						cardAccountSqlMap.put("values", new Object[]{userId, card.getId(), 1, consume_type, 
								pursueMoney, curTime, remark, orderId, uid, parkId, berthSegId, berthId, groupId});
						bathSql.add(cardAccountSqlMap);
					}
					if(prepay > money){
						double refundMoney = StringUtils.formatDouble(prepay - money);
						//更新卡片余额
						Map<String, Object> cardSqlMap = new HashMap<String, Object>();
						//卡片流水
						Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();
						cardSqlMap.put("sql", "update com_nfc_tb set balance=balance+? where id=?");
						cardSqlMap.put("values", new Object[]{refundMoney, card.getId()});
						bathSql.add(cardSqlMap);
						Long card_account_id = writeService.getkey("seq_card_account_tb");
						cardAccountSqlMap.put("sql", "insert into card_account_tb(id,uin,card_id,type,charge_type," +
								"amount,create_time,remark,orderid,uid,comid,berthseg_id,berth_id,groupid) " +
								"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
						cardAccountSqlMap.put("values", new Object[]{card_account_id, userId, card.getId(), 0, 4, refundMoney,
								curTime, "预支付退款 " + carNumber, orderId, uid, parkId, berthSegId, berthId, groupId});
						bathSql.add(cardAccountSqlMap);
					}
					if(order.getBerthnumber() > 0){
						//更新泊位状态
						Map<String, Object> berthSqlMap = new HashMap<String, Object>();
						berthSqlMap.put("sql", "update com_park_tb set state=?,order_id=?,end_time=? " +
								" where id =? and order_id=?");
						berthSqlMap.put("values", new Object[]{0, null, endTime, 
								order.getBerthnumber(), orderId});
						bathSql.add(berthSqlMap);
					}
					if(brethOrderId > 0){
						//更新车检器订单状态
						Map<String, Object> berthOrderSqlMap = new HashMap<String, Object>();
						berthOrderSqlMap.put("sql", "update berth_order_tb set out_uid=?," +
								"order_total=? where id=? ");
						berthOrderSqlMap.put("values", new Object[]{uid, money, brethOrderId});
						bathSql.add(berthOrderSqlMap);
					}
					boolean result = writeService.bathUpdate2(bathSql);
					logger.error("result:"+result);
					if(result){
						resp.setResult(1);
						resp.setErrmsg("刷卡结算成功");
						//发卡片消费短信
						//手机号
						Map userMap = readService.getMap("select mobile from user_info_tb where id =? ", new Object[]{userId});
						logger.error("发卡片消费短信，user:"+userMap);
						if(userMap!=null&&!userMap.isEmpty()){
							String mobile =(String) userMap.get("mobile");
							logger.error("发卡片消费短信，mobile:"+mobile);
							if(money>0&&mobile!=null&&Check.checkMobile(mobile)){
								logger.error("开始发卡片消费短信，mobile:"+mobile);
								String timeStr = TimeTools.gettime();
								Map comMap = readService.getMap("select company_name from com_info_tb where id =? ", new Object[]{order.getComid()});	
								String comName ="";
								if(comMap!=null)
									comName = (String)comMap.get("company_name");
								String dur = StringUtils.getTimeString(order.getCreate_time(), endTime);
								publicMethods.sendCardMessage(mobile, "您于"+timeStr+"在"+comName+"停车场" +
										"停车"+dur+"，自动扣费"+money+"元，收费员编号"+uid+".");
							}
						}
						return resp;
					}
				}
			}
			resp.setResult(0);
			resp.setErrmsg("刷卡结算订单失败");
			return resp;
		} catch (Exception e) {
			logger.error(e);
			resp.setResult(-1);
			resp.setErrmsg("系统错误");
		}
		return resp;
	}

	@Override
	public ManuPayPosOrderResp manuPayPosOrder(ManuPayPosOrderReq req) {
		ManuPayPosOrderResp resp = new ManuPayPosOrderResp();
		try {
			logger.error(req.toString());
			//----------------------------参数--------------------------------//
			Long curTime = req.getCurTime();
			Order order = req.getOrder();
			Double money = req.getMoney();//总金额
			String imei = req.getImei();//手机串号
			Long workId = req.getWorkId();//当前上班记录
			Long uid = req.getUid();//收费员编号
			Integer version = req.getVersion();//版本号
			Long brethOrderId = req.getBerthOrderId();
			Long endTime = req.getEndTime();
			String nfc_uuid = req.getNfc_uuid();
			long userId = req.getUserId();
			int bindcard = req.getBindcard();
			Long groupId = req.getGroupId();
			//----------------------------校验参数--------------------------------//
			if(order == null 
					|| uid <= 0 
					|| workId <= 0 
					|| money < 0
					|| endTime == null
					|| curTime == null
					|| groupId <= 0){//money可以为零
				resp.setResult(-2);
				resp.setErrmsg("参数错误");
				return resp;//参数错误
			}
			//----------------------------获取订单信息--------------------------------//
			logger.error("order:"+order.toString());
			long orderId = order.getId();
			double prepay = order.getPrepaid();
			int state = order.getState();
			long berthId = order.getBerthnumber();
			int cType = order.getC_type();
			String carNumber = order.getCar_number();
			long parkId = order.getComid();
			long berthSegId = order.getBerthsec_id();
			if(state == 1){
				resp.setResult(-3);
				resp.setErrmsg("订单已结算");
				return resp;
			}
			if(state == 2){
				resp.setResult(-4);
				resp.setErrmsg("已置为未缴不可正常结算!");
				return resp;
			}
			if(parkId <= 0){
				resp.setResult(-7);
				resp.setErrmsg("订单信息错误!");
				return resp;
			}
			if(prepay >= money 
					|| cType == 5){//这些情况应该用autoPayPosOrder结算
				resp.setResult(-8);
				resp.setErrmsg("结算失败!");
				return resp;
			}
			int consume_type = 0;
			double pursueMoney = money;
			String remark = "停车费 " + carNumber;
			if(prepay > 0){
				consume_type = 2;
				pursueMoney = StringUtils.formatDouble(money - prepay);
				remark = "补缴停车费 " + carNumber;
			}
			logger.error("consume_type:"+consume_type+",pursueMoney:"+pursueMoney);
			//----------------------------获取卡片信息--------------------------------//
			Card card = null;
			if(nfc_uuid == null || "".equals(nfc_uuid)){
				logger.error("尝试自动获取一个余额最大的默认卡片>>>orderid:"+orderId);
				if(parkId > 0){
					DefaultCardReq defaultCardReq = new DefaultCardReq();
					defaultCardReq.setParkId(parkId);
					defaultCardReq.setUserId(userId);
					defaultCardReq.setCarNumber(carNumber);
					DefaultCardResp defaultCardResp = cardService.getDefaultCard(defaultCardReq);
					if(defaultCardResp.getResult() == 1 
							&& defaultCardResp.getCard() != null){
						card = defaultCardResp.getCard();
						logger.error(card.toString());
					}
				}
			} else {
				logger.error("手动刷卡>>>orderid:"+orderId);
				card = commonMethods.card(nfc_uuid, groupId);
			}
			if(card == null){
				resp.setResult(-9);
				resp.setErrmsg("该卡片未开卡，或不属于当前运营集团");
				return resp;
			}
			logger.error(card.toString());
			if(card.getState() == 1){//注销状态
				resp.setResult(-12);
				resp.setErrmsg("卡片已注销，需重新开卡");
				return resp;
			}
			if(card.getState() == 3){//开卡状态
				resp.setResult(-5);
				resp.setErrmsg("卡片未激活");
				return resp;
			}
			if(bindcard == 0 && card.getState() == 0){
				resp.setResult(-6);
				resp.setErrmsg("卡片未绑定用户");
				return resp;
			}
			if(card.getBalance() < pursueMoney){//余额不足直接返回，书鱼决定的
				resp.setResult(-13);
				resp.setErrmsg("余额不足，卡片余额:"+card.getBalance()+"元");
				return resp;
			}
			//----------------------------具体逻辑--------------------------------//
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			Map<String, Object> cardSqlMap = new HashMap<String, Object>();
			cardSqlMap.put("sql", "update com_nfc_tb set balance=balance-? where id=? ");
			cardSqlMap.put("values", new Object[]{pursueMoney, card.getId()});
			bathSql.add(cardSqlMap);
			Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();
			cardAccountSqlMap.put("sql", "insert into card_account_tb(uin,card_id,type,consume_type," +
					"amount,create_time,remark,orderid,uid,comid,berthseg_id,berth_id,groupid) " +
					" values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
			cardAccountSqlMap.put("values", new Object[]{userId, card.getId(), 1, consume_type, pursueMoney,
					curTime, remark, orderId, uid, parkId, berthSegId, berthId, groupId});
			bathSql.add(cardAccountSqlMap);
			//更新订单状态
			Map<String, Object> orderSqlMap = new HashMap<String, Object>();
			orderSqlMap.put("sql", "update order_tb set state=?,total=?,end_time=?,pay_type=?,imei=?," +
					"out_uid=?,nfc_uuid=? where id=?");
			orderSqlMap.put("values", new Object[]{1, money, endTime, 9, imei, uid, nfc_uuid, orderId});
			bathSql.add(orderSqlMap);
			if(berthId > 0){
				//更新泊位状态
				Map<String, Object> berthSqlMap = new HashMap<String, Object>();
				berthSqlMap.put("sql", "update com_park_tb set state=?,order_id=?,end_time=?" +
						" where id =? and order_id=?");
				berthSqlMap.put("values", new Object[]{0, null, endTime, berthId, orderId});
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
			logger.error("b"+b);
			if(b){
				resp.setResult(1);
				resp.setErrmsg("刷卡结算成功");
				return resp;
			}
			resp.setResult(0);
			resp.setErrmsg("刷卡结算失败");
		} catch (Exception e) {
			// TODO: handle exception
			resp.setResult(-1);
			resp.setErrmsg("系统错误");
		}
		return resp;
	}

	@Override
	public PayEscapePosOrderResp payEscapePosOrder(PayEscapePosOrderReq req) {
		PayEscapePosOrderResp resp = new PayEscapePosOrderResp();
		try {
			logger.error(req.toString());
			Long curTime = req.getCurTime();
			Order order = req.getOrder();
			long uid = req.getUid();//收费员编号
			String imei = req.getImei();
			double money = req.getMoney();//结算金额
			int version = req.getVersion();
			long berthSegId = req.getBerthSegId();
			Long brethOrderId = req.getBerthOrderId();
			String nfc_uuid = req.getNfc_uuid();
			long userId = req.getUserId();
			int bindcard = req.getBindcard();
			Long groupId = req.getGroupId();//追缴收费员所在的运营集团
			Long berthId = req.getBerthId();//追缴订单的泊位,可能为-1（2016-10-14添加）
			long parkId = req.getParkId();//追缴收费员所在的停车场
			if(order == null 
					|| uid <= 0 
					|| money < 0 
					|| curTime == null
					|| groupId <= 0
					|| parkId <= 0){//money可以为0
				resp.setResult(-2);
				resp.setErrmsg("参数错误");
				return resp;
			}
			//-----------------------------获取订单信息-----------------------------//
			logger.error("order:"+order.toString());
			long orderId = order.getId();
			double prepay = order.getPrepaid();
			int state = order.getState();
			int cType = order.getC_type();
			String carNumber = order.getCar_number();
			if(state == 0){
				resp.setResult(-3);
				resp.setErrmsg("非逃单，请正常结算");
				return resp;
			}
			if(state == 1){
				resp.setResult(-4);
				resp.setErrmsg("订单已结算");
				return resp;
			}
			if(prepay >= money
					|| cType == 5){//预付金额大于支付金额，此时应该用autoPayPosOrder结算
				resp.setResult(-8);
				resp.setErrmsg("结算失败!");
				return resp;
			}
			int consume_type = 3;//追缴停车费
			double pursueMoney = money;
			if(prepay > 0){
				pursueMoney = StringUtils.formatDouble(money - prepay);
			}
			logger.error("consume_type:"+consume_type+",pursueMoney:"+pursueMoney);
			//----------------------------获取卡片信息--------------------------------//
			Card card = null;
			if(nfc_uuid == null || "".equals(nfc_uuid)){
				logger.error("尝试自动获取一个余额最大的默认卡片>>>orderid:"+orderId);
				if(parkId > 0){
					DefaultCardReq defaultCardReq = new DefaultCardReq();
					defaultCardReq.setParkId(parkId);
					defaultCardReq.setUserId(userId);
					defaultCardReq.setCarNumber(carNumber);
					DefaultCardResp defaultCardResp = cardService.getDefaultCard(defaultCardReq);
					if(defaultCardResp.getResult() == 1 
							&& defaultCardResp.getCard() != null){
						card = defaultCardResp.getCard();
						logger.error(card.toString());
					}
				}
			}else{
				logger.error("手动刷卡>>>orderid:"+orderId);
				card = commonMethods.card(nfc_uuid, groupId);
				//这里用主库查询，追缴的时候可能一次性追缴多个逃单，用备库可能产生脏读
			}
			if(card == null){
				resp.setResult(-9);
				resp.setErrmsg("该卡片未开卡，或不属于当前运营集团");
				return resp;
			}
			logger.error(card.toString());
			if(card.getState() == 1){//注销状态
				resp.setResult(-12);
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
			if(card.getBalance() < pursueMoney){//余额不足直接返回，书鱼决定的
				resp.setResult(-13);
				resp.setErrmsg("余额不足，卡片余额:"+card.getBalance()+"元");
				return resp;
			}
			//----------------------------具体逻辑--------------------------------//
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			Map<String, Object> cardSqlMap = new HashMap<String, Object>();
			cardSqlMap.put("sql", "update com_nfc_tb set balance=balance-? where id=? ");
			cardSqlMap.put("values", new Object[]{pursueMoney, card.getId()});
			bathSql.add(cardSqlMap);
			Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();
			cardAccountSqlMap.put("sql", "insert into card_account_tb(uin,card_id,type,consume_type," +
					"amount,create_time,remark,orderid,uid,comid,berthseg_id,berth_id,groupid) " +
					"values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
			cardAccountSqlMap.put("values", new Object[]{userId, card.getId(), 1, consume_type, pursueMoney,
					curTime, "追缴停车费 " + carNumber, orderId, uid, parkId, berthSegId, berthId, groupId});
			bathSql.add(cardAccountSqlMap);
			//更新订单状态
			Map<String, Object> orderSqlMap = new HashMap<String, Object>();
			orderSqlMap.put("sql", "update order_tb set state=?,total=?,pay_type=?,imei=?,out_uid=?," +
					"nfc_uuid=? where id=?");
			orderSqlMap.put("values", new Object[]{1, money, 9, imei, uid, nfc_uuid, orderId});
			bathSql.add(orderSqlMap);
			//更新追缴表数据
			Map<String, Object> escapeSqlMap = new HashMap<String, Object>();
			escapeSqlMap.put("sql", "update no_payment_tb set state=?,pursue_uid=?,pursue_time=?,act_total=?," +
					"pursue_comid=?,pursue_berthseg_id=?,pursue_berth_id=?,pursue_groupid=? where order_id=? ");
			escapeSqlMap.put("values", new Object[]{1, uid, curTime, money, parkId, berthSegId, berthId,
					groupId, orderId});
			bathSql.add(escapeSqlMap);
			
			if(brethOrderId > 0){
				//更新车检器订单状态
				Map<String, Object> berthOrderSqlMap = new HashMap<String, Object>();
				berthOrderSqlMap.put("sql", "update berth_order_tb set out_uid=?,order_total=? where id=? ");
				berthOrderSqlMap.put("values", new Object[]{uid, money, brethOrderId});
				bathSql.add(berthOrderSqlMap);
			}
			boolean b = writeService.bathUpdate2(bathSql);
			logger.error("b"+b);
			if(b){
				resp.setResult(1);
				resp.setErrmsg("刷卡追缴成功");
				return resp;
			}
			resp.setResult(0);
			resp.setErrmsg("刷卡追缴失败");
		} catch (Exception e) {
			logger.error(e);
			resp.setResult(-1);
			resp.setErrmsg("系统错误");
		}
		return resp;
	}

}
