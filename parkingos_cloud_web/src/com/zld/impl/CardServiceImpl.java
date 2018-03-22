package com.zld.impl;

import com.zld.pojo.*;
import com.zld.service.CardService;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CardServiceImpl implements CardService {
	@Autowired
	private DataBaseService writeService;
	@Autowired
	private PgOnlyReadService readService;
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Autowired
	private CommonMethods commonMethods;

	Logger logger = Logger.getLogger(CardServiceImpl.class);
	@Override
	public BaseResp cardCharge(CardChargeReq req) {
		BaseResp resp = new BaseResp();
		String lock = null;
		try {
			logger.error(req.toString());
			long curTime = req.getCurTime();
			long cashierId = req.getCashierId();
			long cardId = req.getCardId();
			double money = req.getMoney();
			int chargeType = req.getChargeType();
			long orderId = req.getOrderId();
			long groupId = req.getGroupId();
			String subOrderId = req.getSubOrderId();
			if(cardId <= 0
					|| money <= 0
					|| chargeType < 0
					|| cashierId <= 0
					|| groupId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("参数错误");
				return resp;//参数错误
			}
			//----------------------------分布式锁--------------------------------//
			lock = commonMethods.getLock(cardId);
			if(!memcacheUtils.addLock(lock, 60)){//为了防止充值并发
				logger.error("lock:"+lock);
				resp.setResult(-8);
				resp.setErrmsg("同一张卡片一分钟之内只能充值一次");
				return resp;
			}
			//---------------------------卡片信息------------------------------------//
			Card card = readService.getPOJO("select * from com_nfc_tb where " +
							" id=? and is_delete=? and type=? limit ? ", new Object[]{cardId, 0, 2, 1},
					Card.class);
			if(card == null){
				resp.setResult(-3);
				resp.setErrmsg("该卡片未开卡");
				return resp;
			}
			logger.error(card.toString());
			if(card.getGroup_id() <= 0){
				resp.setResult(-4);
				resp.setErrmsg("卡片信息错误");
				return resp;
			}
			if(card.getGroup_id().intValue() != groupId){
				resp.setResult(-5);
				resp.setErrmsg("卡片不属于当前运营集团，不能充值");
				return resp;
			}
			if(card.getState() == 1){//注销状态
				resp.setResult(-6);
				resp.setErrmsg("卡片已注销，需重新开卡");
				return resp;
			}
			if(card.getState() == 3){//开卡状态
				resp.setResult(-7);
				resp.setErrmsg("卡片未激活");
				return resp;
			}
			long userId = card.getUin();
			//---------------------------支付方式------------------------------------//
			String remark = null;
			switch (chargeType) {
				case 0:
					remark = "现金充值" + money + "元";
					break;
				case 1:
					remark = "微信公众号充值" + money + "元";
					break;
				case 2:
					remark = "微信客户端充值" + money + "元";
					break;
				case 3:
					remark = "支付宝客户端充值" + money + "元";
					break;
				case 4:
					remark = "预支付退款" + money + "元";
					break;
				case 5:
					remark = "订单退款" + money + "元";
					break;
				default:
					break;
			}
			//---------------------------具体逻辑------------------------------------//
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//更新卡片余额
			Map<String, Object> cardSqlMap = new HashMap<String, Object>();
			//卡片流水
			Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();
			//收费员现金流水
			Map<String, Object> cashAccountSqlMap = new HashMap<String, Object>();
			//第三方支付子账号充值流水
			Map<String, Object> subAccountSqlMap = new HashMap<String, Object>();

			cardSqlMap.put("sql", "update com_nfc_tb set balance=balance+? where id=?");
			cardSqlMap.put("values", new Object[]{money, cardId});
			bathSql.add(cardSqlMap);
			Long card_account_id = writeService.getkey("seq_card_account_tb");
			cardAccountSqlMap.put("sql", "insert into card_account_tb(id,uin,card_id,type,charge_type," +
					"amount,create_time,remark,orderid,uid,groupid) values(?,?,?,?,?,?,?,?,?,?,?)");
			cardAccountSqlMap.put("values", new Object[]{card_account_id, userId, cardId, 0, chargeType,
					money, curTime, remark, orderId, cashierId, groupId});
			bathSql.add(cardAccountSqlMap);
			switch (chargeType) {
				case 0://现金充值
					cashAccountSqlMap.put("sql", "insert into parkuser_cash_tb(uin,amount,type,create_time," +
							"target,ctype,card_account_id,groupid) values(?,?,?,?,?,?,?,?)");
					cashAccountSqlMap.put("values", new Object[]{cashierId, money, 2, curTime, 5, 0,
							card_account_id, groupId});
					bathSql.add(cashAccountSqlMap);
					break;
				case 1://微信公众号充值
				case 2://微信客户端充值
				case 3://支付宝客户端充值
					int subType = 0;//在线充值方式 0：微信公众号 1：微信客户端 2：支付宝客户端
					if(chargeType == 2){
						subType = 1;
					}else if(chargeType == 3){
						subType = 2;
					}
					subAccountSqlMap.put("sql", "insert into sub_account_tb(groupid,amount,sub_orderid," +
							"create_time,card_account_id,uin,type) values(?,?,?,?,?,?,?,?)");
					subAccountSqlMap.put("values", new Object[]{groupId, money, subOrderId, curTime,
							card_account_id, userId, subType});
					bathSql.add(subAccountSqlMap);
					break;
				case 4://预支付退款
					//逻辑后面补充........
					break;
				case 5://订单退款
					//逻辑后面补充........
					break;
				default:
					break;
			}
			boolean r = writeService.bathUpdate2(bathSql);
			logger.error("r:"+r);
			if(r){
				resp.setResult(1);
				resp.setErrmsg("充值成功");
				return resp;
			}
			resp.setResult(-6);
			resp.setErrmsg("充值失败");
		} catch (Exception e) {
			resp.setResult(-1);
			resp.setErrmsg("系统错误");
		}
		return resp;
	}

	@Override
	public BaseResp returnCard(ReturnCardReq req) {
		BaseResp resp = new BaseResp();
		String lock = null;
		try {
			logger.error(req.toString());
			Long cardId = req.getCardId();
			Long unbinder = req.getUnBinder();
			Long curTime = req.getCurTime();
			Long groupId = req.getGroupId();
			if(cardId <= 0
					|| unbinder <= 0
					|| groupId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("参数错误");
				return resp;
			}
			//-------------------------分布式锁---------------------------//
			lock = commonMethods.getLock(cardId);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				resp.setResult(-3);
				resp.setErrmsg("并发请求错误");
				return resp;
			}
			//-------------------------卡片信息---------------------------//
			Card card = writeService.getPOJO("select * from com_nfc_tb where " +
							" id=? and is_delete=? and type=? limit ? ", new Object[]{cardId, 0, 2, 1},
					Card.class);
			//主库查询防并发
			if(card == null){
				resp.setResult(-4);
				resp.setErrmsg("该卡片未开卡");
				return resp;
			}
			logger.error(card);
			if(card.getGroup_id() <= 0){
				resp.setResult(-5);
				resp.setErrmsg("卡片信息错误");
				return resp;
			}
			if(card.getGroup_id().intValue() != groupId){
				resp.setResult(-6);
				resp.setErrmsg("卡片不属于当前运营集团，不能注销");
				return resp;
			}
			if(card.getState() == 1){//注销状态
				resp.setResult(-7);
				resp.setErrmsg("卡片已注销");
				return resp;
			}
			Double balance = card.getBalance();
			int state = card.getState();
			//-------------------------判断是否有未处理订单---------------------------//
			List<Order> orderList = readService.getPOJOList("select * from order_tb" +
							" where nfc_uuid=? and state<>? ",
					new Object[]{card.getNfc_uuid(), 1}, Order.class);//nfc_uuid有索引
			if(orderList != null && !orderList.isEmpty()){
				String ids = null;
				for(Order order : orderList){
					if(ids == null){
						ids += order.getId();
					}else{
						ids += "," + order.getId();
					}
				}
				logger.error("orderids:"+ids);
				resp.setResult(-8);
				resp.setErrmsg("卡片存在未处理订单，需先处理，订单编号："+ids);
				return resp;
			}
			//-------------------------具体逻辑---------------------------//
			String remark = "注销卡片，退还余额"+balance+"元";
			if(state == 3){//开卡（此时的卡片还不能用，要激活后才可使用）
				remark = "注销未激活卡片";
				balance = 0d;//卡片尚未卖出，没有退款操作，实际操作金额为0
			}
			logger.error("balance:"+balance);
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//更新卡片余额
			Map<String, Object> cardSqlMap = new HashMap<String, Object>();
			//写卡片流水
			Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();
			//写收费员现金流水
			Map<String, Object> cashSqlMap = new HashMap<String, Object>();
			//卡片和车牌关联表
			Map<String, Object> cardCarSqlMap = new HashMap<String, Object>();

			cardSqlMap.put("sql", "update com_nfc_tb set state=?,cancel_id=?," +
					"cancel_time=?,balance=?,uin=? where id=?");
			cardSqlMap.put("values", new Object[]{1, unbinder, curTime, 0d, -1, cardId});
			bathSql.add(cardSqlMap);
			long accountId = writeService.getkey("seq_card_account_tb");
			cardAccountSqlMap.put("sql", "insert into card_account_tb(id,card_id,type," +
					"amount,create_time,remark,uid,uin,groupid) values(?,?,?,?,?,?,?,?,?)");
			cardAccountSqlMap.put("values", new Object[]{accountId, cardId, 5, balance, curTime,
					remark, unbinder, card.getUin(), groupId});
			bathSql.add(cardAccountSqlMap);
			if(state != 3){//3：开卡（此时的卡片还不能用，要激活后才可使用），此时的卡片尚未卖出，无需写退款流水
				cashSqlMap.put("sql", "insert into parkuser_cash_tb(uin,amount,type,create_time," +
						"target,ctype,card_account_id,groupid) values(?,?,?,?,?,?,?,?)");
				cashSqlMap.put("values", new Object[]{unbinder, balance, 2, curTime,
						6, 1, accountId, groupId});
				bathSql.add(cashSqlMap);
			}
			if(state == 4){
				cardCarSqlMap.put("sql", "update card_carnumber_tb set is_delete=? where card_id=? " +
						" and is_delete=? ");
				cardCarSqlMap.put("values", new Object[]{1, cardId, 0});
				bathSql.add(cardCarSqlMap);
			}
			boolean b = writeService.bathUpdate2(bathSql);
			logger.error("b:"+b);
			if(b){
				resp.setResult(1);
				resp.setErrmsg("注销成功");
				return resp;
			}
			resp.setResult(-9);
			resp.setErrmsg("注销失败");
		} catch (Exception e) {
			logger.error(e);
			resp.setResult(-1);
			resp.setErrmsg("系统错误");
		} finally {
			boolean b = memcacheUtils.delLock(lock);
			logger.error("删除锁lock:"+lock+"b:"+b);
		}
		return resp;
	}

	@Override
	public BaseResp bindUserCard(BindCardReq req) {
		BaseResp resp = new BaseResp();
		String lock = null;
		try {
			logger.error(req.toString());
			Long cardId = req.getCardId();
			Long binder = req.getBinder();
			String mobile = req.getMobile();
			String carNumber = req.getCarNumber();
			Long curTime = req.getCurTime();
			long groupId = req.getGroupId();
			//没有泊位属性
			if(cardId <= 0
					|| binder <= 0
					//手机号和车牌号不能都为空
					|| (carNumber == null || "".equals(carNumber))
					|| (mobile == null || "".equals(mobile))
					|| groupId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("参数错误");
				return resp;
			}
			//----------------------------校验手机号和车牌号------------------------//
			boolean m = StringUtils.checkMobile(mobile);
			if(!m){
				resp.setResult(-13);
				resp.setErrmsg("请输入正确的手机号");
				return resp;
			}
			List<String> plateList = new ArrayList<String>();
			if(carNumber != null){
				//多车牌以英文逗号隔开
				String[] cars = carNumber.split(",");
				for(int i = 0; i< cars.length; i++){
					String plate = cars[i];
					plateList.add(plate);
					if(!StringUtils.checkPlate(plate)){
						resp.setResult(-14);
						resp.setErrmsg("请输入正确的车牌号，多个车牌以英文逗号隔开");
						return resp;
					}
				}
			}
			//----------------------------分布式锁--------------------------------//
			lock = commonMethods.getLock(cardId);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				resp.setResult(-12);
				resp.setErrmsg("并发请求错误");
				return resp;
			}
			//--------------------校验卡片---------------------//
			Card card = writeService.getPOJO("select * from com_nfc_tb where " +
							" id=? and is_delete=? and type=? ",
					new Object[]{cardId, 0, 2}, Card.class);
			//主库查询防并发
			if(card == null){
				resp.setResult(-3);
				resp.setErrmsg("卡片不存在");
				return resp;
			}
			logger.error(card.toString());
			int state = card.getState();
			switch (state) {
				case 0://0：已激活未绑定
				case 2://2：已绑定用户
				case 4://4：已绑定车牌号
					break;
				case 1://注销状态
					resp.setResult(-6);
					resp.setErrmsg("该卡片已被注销，需重新开卡");
					return resp;
				case 3://3：开卡
					resp.setResult(-8);
					resp.setErrmsg("卡片没有激活");
					return resp;
				default:
					resp.setResult(-9);
					resp.setErrmsg("卡片信息错误");
					return resp;
			}
			//--------------------具体逻辑---------------------//
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//用户
			Map<String, Object> userSqlMap = new HashMap<String, Object>();
			//卡片和车牌关联表
			Map<String, Object> cardCarSqlMap = new HashMap<String, Object>();
			//绑定卡片
			Map<String, Object> cardSqlMap = new HashMap<String, Object>();
			//卡片流水绑定用户
			Map<String, Object> bindAccountSqlMap = new HashMap<String, Object>();
			//卡片流水
			Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();

			Long userId = -1L;
			Map<String, Object> userMap = readService.getMap("select id from user_info_tb" +
					" where mobile=? and auth_flag=? ", new Object[]{mobile, 4});
			if(userMap == null){
				userId = writeService.getkey("seq_user_info_tb");
				String strid = userId+"zld";
				userSqlMap.put("sql", "insert into user_info_tb (id,nickname,password,strid," +
						"reg_time,mobile,auth_flag,comid) values (?,?,?,?,?,?,?,?)");
				userSqlMap.put("values", new Object[]{userId, "车主", strid, strid, curTime, mobile, 4, 0});
				bathSql.add(userSqlMap);
			}else{
				userId = (Long)userMap.get("id");
			}
			logger.error("userId:"+userId+",mobile:"+mobile);
			if(plateList != null && !plateList.isEmpty()){
				//-------------------------------检查车牌是否被别人绑定，都没有绑定就插入一条--------------------------//
				for(String car : plateList){
					//删除车牌号(书鱼决定的，不通知用户强行删除用户和车牌的绑定关系)
					Map<String, Object> delCarSqlMap = new HashMap<String, Object>();
					delCarSqlMap.put("sql", "update car_info_tb set state=? where car_number=? and " +
							" state=? and uin>? and uin<>? ");
					delCarSqlMap.put("values", new Object[]{0, car, 1, 0, userId});
					bathSql.add(delCarSqlMap);

					Long count = readService.getLong("select count(id) from car_info_tb where car_number=? and " +
							" state=? and uin=? ", new Object[]{car, 1, userId});
					if(count == 0){
						//添加车牌号
						Map<String, Object> carSqlMap = new HashMap<String, Object>();
						carSqlMap.put("sql", "insert into car_info_tb (uin,car_number,create_time) values (?,?,?)");
						carSqlMap.put("values", new Object[]{userId, car, curTime});
						bathSql.add(carSqlMap);
					}
				}
				//----------------------------------删除以前的车牌-----------------------------------//
				List<Car> carList = readService.getPOJOList("select id,car_number from car_info_tb where uin=? and " +
						" state=? ", new Object[]{userId, 1}, Car.class);
				if(carList != null && !carList.isEmpty()){
					for(Car car : carList){
						String plate = car.getCar_number();
						Long id = car.getId();
						if(!plateList.contains(plate)){//其他的车牌删除掉
							//删除车牌号
							Map<String, Object> carSqlMap = new HashMap<String, Object>();
							carSqlMap.put("sql", "update car_info_tb set state=? where id=? ");
							carSqlMap.put("values", new Object[]{0, id});
							bathSql.add(carSqlMap);
						}
					}
				}
			}
			cardSqlMap.put("sql", "update com_nfc_tb set state=?,uin=?,uid=?,update_time=? where id=?");
			cardSqlMap.put("values", new Object[]{2, userId, binder, curTime, card.getId()});
			bathSql.add(cardSqlMap);

			cardAccountSqlMap.put("sql", "insert into card_account_tb(card_id,type,create_time,remark,uid," +
					"uin,comid,berthseg_id,groupid) values(?,?,?,?,?,?,?,?,?)");
			cardAccountSqlMap.put("values", new Object[]{card.getId(), 4, curTime, "绑定用户，手机号:" + mobile,
					binder, userId, -1L, -1L, groupId});
			bathSql.add(cardAccountSqlMap);

			bindAccountSqlMap.put("sql", "update card_account_tb set uin=? where card_id=? and uin<=? ");//state=0激活
			bindAccountSqlMap.put("values", new Object[]{userId, card.getId(), 0});
			bathSql.add(bindAccountSqlMap);

			if(state == 4){//绑定的车牌号
				//和书鱼决定，原来的车牌绑定数据会删掉,不自动导入车牌表
				cardCarSqlMap.put("sql", "update card_carnumber_tb set is_delete=? where card_id=? " +
						" and is_delete=? ");
				cardCarSqlMap.put("values", new Object[]{1, cardId, 0});
				bathSql.add(cardCarSqlMap);
			}
			boolean b = writeService.bathUpdate2(bathSql);
			logger.error("b:"+b);
			if(b){
				resp.setResult(1);
				resp.setErrmsg("绑定成功");
				return resp;
			}
			resp.setResult(-11);
			resp.setErrmsg("绑定失败");
		} catch (Exception e) {
			logger.error(e);
		} finally {
			boolean b = memcacheUtils.delLock(lock);
			logger.error("删除锁lock:"+lock+"b:"+b);
		}
		resp.setResult(-1);
		resp.setErrmsg("系统错误");
		return resp;
	}

	@Override
	public BaseResp unBindCard(UnbindCardReq req) {
		BaseResp resp = new BaseResp();
		String lock = null;
		try {
			logger.error(req.toString());
			Long cardId = req.getCardId();
			Long unbinder = req.getUnBinder();
			Long curTime = req.getCurTime();
			Long groupId = req.getGroupId();
			if(cardId <= 0
					|| unbinder <= 0
					|| groupId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("参数错误");
				return resp;
			}
			//-------------------------分布式锁---------------------------//
			lock = commonMethods.getLock(cardId);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				resp.setResult(-3);
				resp.setErrmsg("并发请求错误");
				return resp;
			}
			//-------------------------卡片信息---------------------------//
			Card card = writeService.getPOJO("select * from com_nfc_tb where " +
							" id=? and is_delete=? and type=? limit ? ", new Object[]{cardId, 0, 2, 1},
					Card.class);
			//主库查询防并发
			if(card == null){
				resp.setResult(-4);
				resp.setErrmsg("该卡片未开卡");
				return resp;
			}
			logger.error(card);
			if(card.getGroup_id() <= 0){
				resp.setResult(-5);
				resp.setErrmsg("卡片信息错误");
				return resp;
			}
			if(card.getGroup_id().intValue() != groupId){
				resp.setResult(-6);
				resp.setErrmsg("卡片不属于当前运营集团，不能解绑");
				return resp;
			}
			int state = card.getState();
			long userId = card.getUin();
			switch (state) {
				case 0://0:激活
				case 3://0:开卡
					resp.setResult(-10);
					resp.setErrmsg("卡片未绑定用户或者车牌");
					return resp;
				case 1://注销状态
					resp.setResult(-7);
					resp.setErrmsg("卡片已注销");
					return resp;
				default:
					break;
			}
			//-------------------------查询绑定信息------------------------//
			String remark = null;
			if(state == 2 && userId > 0){
				logger.error("卡片绑定的用户，解除绑定，userId:" + userId);
				Map<String, Object> userMap = readService.getMap("select mobile from user_info_tb " +
						" where id=? ", new Object[]{userId});
				if(userMap != null){
					remark = "解绑用户，手机号：" + userMap.get("mobile");
				}
			}else if(state == 4){
				List<CardCarNumber> ccList = readService.getPOJOList("select car_number from card_carnumber_tb" +
						" where card_id=? and is_delete=? ", new Object[]{cardId, 0}, CardCarNumber.class);
				String carNumber = "";
				if(ccList != null && !ccList.isEmpty()){
					for(CardCarNumber c : ccList){
						if("".equals(carNumber)){
							carNumber = c.getCar_number();
						}else{
							carNumber += ("，" + c.getCar_number());
						}
					}
				}
				remark = "解绑车牌号，车牌号：" + carNumber;
			}
			logger.error("remark:" + remark);
			//-------------------------具体逻辑---------------------------//
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//更新卡片余额
			Map<String, Object> cardSqlMap = new HashMap<String, Object>();
			//写卡片流水
			Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();
			//卡片和车牌关联表
			Map<String, Object> cardCarSqlMap = new HashMap<String, Object>();
			//更新之前流水
			Map<String, Object> bindAccountSqlMap = new HashMap<String, Object>();

			cardSqlMap.put("sql", "update com_nfc_tb set state=?,uin=? where id=?");
			cardSqlMap.put("values", new Object[]{0, -1, cardId});
			bathSql.add(cardSqlMap);
			long accountId = writeService.getkey("seq_card_account_tb");
			cardAccountSqlMap.put("sql", "insert into card_account_tb(id,card_id,type,create_time," +
					"remark,uid,uin,groupid) values(?,?,?,?,?,?,?,?)");
			cardAccountSqlMap.put("values", new Object[]{accountId, cardId, 6, curTime, remark,
					unbinder, -1, groupId});
			bathSql.add(cardAccountSqlMap);
			if(state == 2){//绑定了用户
				bindAccountSqlMap.put("sql", "update card_account_tb set uin=? where card_id=? and uin>? ");//state=0激活
				bindAccountSqlMap.put("values", new Object[]{-1, card.getId(), 0});
				bathSql.add(bindAccountSqlMap);
			}else if(state == 4){//绑定车牌号
				cardCarSqlMap.put("sql", "update card_carnumber_tb set is_delete=? where card_id=? " +
						" and is_delete=? ");
				cardCarSqlMap.put("values", new Object[]{1, cardId, 0});
				bathSql.add(cardCarSqlMap);
			}
			boolean b = writeService.bathUpdate2(bathSql);
			logger.error("b:"+b);
			if(b){
				resp.setResult(1);
				resp.setErrmsg("解绑成功");
				return resp;
			}
			resp.setResult(-9);
			resp.setErrmsg("解绑失败");
		} catch (Exception e) {
			logger.error(e);
			resp.setResult(-1);
			resp.setErrmsg("系统错误");
		} finally {
			boolean b = memcacheUtils.delLock(lock);
			logger.error("删除锁lock:"+lock+"b:"+b);
		}
		return resp;
	}

	@Override
	public BaseResp bindPlateCard(BindCardReq req) {
		BaseResp resp = new BaseResp();
		String lock = null;
		try {
			logger.error(req.toString());
			Long cardId = req.getCardId();
			Long binder = req.getBinder();
			String mobile = req.getMobile();
			String carNumber = req.getCarNumber();
			Long curTime = req.getCurTime();
			long groupId = req.getGroupId();
			//没有泊位属性
			if(cardId <= 0
					|| binder <= 0
					//手机号和车牌号不能都为空
					|| (carNumber == null || "".equals(carNumber))
					|| (mobile != null && !"".equals(mobile))
					|| groupId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("参数错误");
				return resp;
			}
			//----------------------------校验车牌号------------------------//
			List<String> plateList = new ArrayList<String>();
			//多车牌以英文逗号隔开
			String[] cars = carNumber.split(",");
			for(int i = 0; i< cars.length; i++){
				String plate = cars[i];
				plateList.add(plate);
				if(!StringUtils.checkPlate(plate)){
					resp.setResult(-14);
					resp.setErrmsg("请输入正确的车牌号，多个车牌以英文逗号隔开");
					return resp;
				}
			}
			//----------------------------分布式锁--------------------------------//
			lock = commonMethods.getLock(cardId);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				resp.setResult(-12);
				resp.setErrmsg("并发请求错误");
				return resp;
			}
			//--------------------校验卡片---------------------//
			Card card = writeService.getPOJO("select * from com_nfc_tb where " +
							" id=? and is_delete=? and type=? ",
					new Object[]{cardId, 0, 2}, Card.class);
			//主库查询防并发
			if(card == null){
				resp.setResult(-3);
				resp.setErrmsg("卡片不存在");
				return resp;
			}
			logger.error(card.toString());
			int state = card.getState();
			switch (state) {
				case 0://0：已激活未绑定
				case 2://2：已绑定用户
				case 4://4：已绑定车牌号
					break;
				case 1://注销状态
					resp.setResult(-6);
					resp.setErrmsg("该卡片已被注销，需重新开卡");
					return resp;
				case 3://3：开卡
					resp.setResult(-8);
					resp.setErrmsg("卡片没有激活");
					return resp;
				default:
					resp.setResult(-9);
					resp.setErrmsg("卡片信息错误");
					return resp;
			}
			//--------------------具体逻辑---------------------//
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//绑定卡片
			Map<String, Object> cardSqlMap = new HashMap<String, Object>();
			//卡片流水绑定用户
			Map<String, Object> bindAccountSqlMap = new HashMap<String, Object>();
			//卡片流水
			Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();

			cardSqlMap.put("sql", "update com_nfc_tb set state=?,uin=?,uid=?,update_time=? where id=?");
			cardSqlMap.put("values", new Object[]{4, -1L, binder, curTime, card.getId()});
			bathSql.add(cardSqlMap);

			cardAccountSqlMap.put("sql", "insert into card_account_tb(card_id,type,create_time,remark,uid," +
					"uin,comid,berthseg_id,groupid) values(?,?,?,?,?,?,?,?,?)");
			cardAccountSqlMap.put("values", new Object[]{card.getId(), 4, curTime, "绑定车牌号，车牌号:" + carNumber,
					binder, -1L, -1L, -1L, groupId});
			bathSql.add(cardAccountSqlMap);

			bindAccountSqlMap.put("sql", "update card_account_tb set uin=? where card_id=? and uin>? ");//state=0激活
			bindAccountSqlMap.put("values", new Object[]{-1, card.getId(), 0});
			bathSql.add(bindAccountSqlMap);

			for(String plate : plateList){
				Long count = readService.getLong("select count(id) from card_carnumber_tb where car_number=?" +
						" and card_id=? and is_delete=? ", new Object[]{plate, cardId, 0});
				logger.error("plate:"+plate+"count:"+count);
				if(count == 0){
					//卡片和车牌关联表
					Map<String, Object> cardCarSqlMap = new HashMap<String, Object>();
					cardCarSqlMap.put("sql", "insert into card_carnumber_tb(car_number,card_id,create_time) values(?,?,?)");
					cardCarSqlMap.put("values", new Object[]{plate, cardId, curTime});
					bathSql.add(cardCarSqlMap);
				}
			}
			boolean b = writeService.bathUpdate2(bathSql);
			logger.error("b:"+b);
			if(b){
				resp.setResult(1);
				resp.setErrmsg("绑定成功");
				return resp;
			}
			resp.setResult(-11);
			resp.setErrmsg("绑定失败");
		} catch (Exception e) {
			logger.error(e);
		} finally {
			boolean b = memcacheUtils.delLock(lock);
			logger.error("删除锁lock:"+lock+"b:"+b);
		}
		resp.setResult(-1);
		resp.setErrmsg("系统错误");
		return resp;
	}
}
