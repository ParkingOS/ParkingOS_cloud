package com.zld.facade.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zld.facade.PayPosOrderFacade;
import com.zld.facade.impl.GenPosOrderFacadeImpl.ExeCallable;
import com.zld.impl.CommonMethods;
import com.zld.impl.MemcacheUtils;
import com.zld.pojo.AutoPayPosOrderFacadeReq;
import com.zld.pojo.AutoPayPosOrderReq;
import com.zld.pojo.AutoPayPosOrderResp;
import com.zld.pojo.Berth;
import com.zld.pojo.Car;
import com.zld.pojo.GenPosOrderFacadeReq;
import com.zld.pojo.ManuPayPosOrderFacadeReq;
import com.zld.pojo.ManuPayPosOrderReq;
import com.zld.pojo.ManuPayPosOrderResp;
import com.zld.pojo.Order;
import com.zld.pojo.PayEscapePosOrderFacadeReq;
import com.zld.pojo.PayEscapePosOrderReq;
import com.zld.pojo.PayEscapePosOrderResp;
import com.zld.pojo.WorkRecord;
import com.zld.service.DataBaseService;
import com.zld.service.PayPosOrderService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.ExecutorsUtil;
import com.zld.utils.StringUtils;
@Component
public class PayPosOrderFacadeImpl implements PayPosOrderFacade {
	@Autowired
	private DataBaseService writeService;
	@Autowired
	private PgOnlyReadService readService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	@Resource(name = "payMonth")
	private PayPosOrderService payMonthService;
	@Autowired
	@Resource(name = "payEpay")
	private PayPosOrderService payEpayService;
	@Autowired
	@Resource(name = "payCash")
	private PayPosOrderService payCashService;
	@Autowired
	@Resource(name = "payCard")
	private PayPosOrderService payCardService;
	@Autowired
	private MemcacheUtils memcacheUtils;
	
	Logger logger = Logger.getLogger(PayPosOrderFacadeImpl.class);
	
	@Override
	public AutoPayPosOrderResp autoPayPosOrder(AutoPayPosOrderFacadeReq req) {
		AutoPayPosOrderResp resp = new AutoPayPosOrderResp();
		String lock = null;//分布式锁
		long workId = -1L;//工作记录编号
		boolean result = false;//订单记录生成结果
		try {
			logger.error(req.toString());
			//----------------------------参数--------------------------------//
			Long curTime = req.getCurTime();
			Long orderId = req.getOrderId(); 
			Double money = req.getMoney();//总金额
			String imei  =  req.getImei();//手机串号
			Integer version = req.getVersion();//版本号
			Long uid = req.getUid();
			Long groupId = req.getGroupId();//收费员所在运营集团
			Long endTime = req.getEndTime();
			//----------------------------校验参数--------------------------------//
			if(orderId <= 0 
					|| uid <= 0 
					|| money < 0
					|| groupId <= 0){//money可以为零
				resp.setResult(-1);
				resp.setErrmsg("参数错误");
				return resp;
			}
			//----------------------------分布式锁--------------------------------//
			lock = commonMethods.getLock(orderId);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				resp.setResult(-1);
				resp.setErrmsg("并发请求错误");
				return resp;
			}
			//------------------------多线程并行查询------------------------//
			ExecutorService pool = ExecutorsUtil.getExecutorService();
			ExeCallable callable0 = new ExeCallable(uid, orderId, 0);//上班记录
			ExeCallable callable1 = new ExeCallable(uid, orderId, 1);//订单信息
			ExeCallable callable2 = new ExeCallable(uid, orderId, 2);//订单绑定的车检器订单
			
			Future<Object> future0 = pool.submit(callable0);
			Future<Object> future1 = pool.submit(callable1);
			Future<Object> future2 = pool.submit(callable2);
			
			WorkRecord workRecord = (WorkRecord)future0.get();
			Order order = (Order)future1.get();
			Long brethOrderId = (Long)future2.get();
			//----------------------------校验上班记录--------------------------------//
			if(workRecord == null){
				resp.setResult(-1);
				resp.setErrmsg("未在当前泊位所属的泊位段签到");
				return resp;
			}
			workId = workRecord.getId();
			logger.error("workId:"+workId);
			//----------------------------获取订单信息--------------------------------//
			if(order == null){
				resp.setResult(-1);
				resp.setErrmsg("订单记录不存在");
				return resp;
			}
			logger.error(order.toString());
			if(order.getState() == 1){
				resp.setResult(-1);
				resp.setErrmsg("订单已结算");
				return resp;
			}
			if(order.getState() == 2){
				resp.setResult(-1);
				resp.setErrmsg("订单已置为逃单");
				return resp;
			}
			int payType = order.getPay_type();
			double prepay = order.getPrepaid();
			int cType = order.getC_type();
			long userId = order.getUin();
			String carNumber = order.getCar_number();
			long startTime = order.getCreate_time();
			//----------------------------用户信息--------------------------------//
			if(userId <= 0 && carNumber != null && !"".equals(carNumber)){
				Car car = readService.getPOJO("select * from car_info_tb " +
						" where car_number=? and state=?", new Object[]{carNumber, 1}, Car.class);
				if(car != null && car.getUin() > 0){
					userId = car.getUin();
				}
			}
			logger.error(userId);
			//------------------------根据车间亲订单获取订单结算信息----------------------//
			if(endTime < 0){//客户端低版本没有传endtime参数
				endTime = commonMethods.getOrderEndTime(brethOrderId, uid, curTime);
			}
			String duration = StringUtils.getTimeString(startTime, endTime);
			logger.error("endTime:" + endTime+ ",brethOrderId:"+brethOrderId);
			//-----------------------------逻辑处理-------------------------------//
			/*如果预支付不足，则不再以另一种支付方式（和预支付不同的支付方式）自动结算，
			比如，现金预支付不足，则不再以卡片余额或者停车宝账户余额结算*/
			AutoPayPosOrderReq autoPayReq = new AutoPayPosOrderReq();
			autoPayReq.setOrder(order);
			autoPayReq.setMoney(money);
			autoPayReq.setImei(imei);
			autoPayReq.setUid(uid);
			autoPayReq.setVersion(version);
			autoPayReq.setWorkId(workId);
			autoPayReq.setUserId(userId);
			autoPayReq.setEndTime(endTime);
			autoPayReq.setBerthOrderId(brethOrderId);
			autoPayReq.setGroupId(groupId);
			
			double refundMoney = 0;
			if(prepay > money){
				refundMoney = StringUtils.formatDouble(prepay - money);
			}
			logger.error("refundMoney:"+refundMoney);
			AutoPayPosOrderResp autoPayResp = null;
			resp.setDuration(duration);
			if(cType == 5){//结算月卡订单
				logger.error("月卡自动结算>>>orderid:"+orderId);
				autoPayResp = payMonthService.autoPayPosOrder(autoPayReq);
				logger.error(autoPayReq.toString());
				if(autoPayResp.getResult() == 1){
					result = true;//订单结算成功
					resp.setResult(1);//月卡结算标识
					resp.setErrmsg(autoPayResp.getErrmsg());
					return resp;
				}
			}
			if(payType == 4 && prepay >= money
					|| payType == 0 && money == 0 && prepay == 0){//现金预支付或者未预支付但是0元结算的订单
				logger.error("现金预支付足够或者未预支付但0元结算>>>orderid:"+orderId);
				autoPayResp = payCashService.autoPayPosOrder(autoPayReq);
				logger.error(autoPayReq.toString());
				if(autoPayResp.getResult() == 1){
					result = true;//订单结算成功
					resp.setResult(3);//现金结算标识
					resp.setErrmsg("预收金额："+prepay+
							"元，应收金额："+money+"元，应退款："+refundMoney+"元");
					return resp;
				}
			}
			if(payType == 9
					|| payType == 0 && prepay == 0){//结算刷卡订单
				logger.error("刷卡预支付或者未预支付>>>orderid:"+orderId);
				autoPayResp = payCardService.autoPayPosOrder(autoPayReq);
				logger.error(autoPayReq.toString());
				if(autoPayResp.getResult() == 1){
					result = true;//订单结算成功
					resp.setResult(4);//刷卡结算标识
					resp.setErrmsg(autoPayResp.getErrmsg());
					return resp;
				}
			}
			if(userId > 0 
					&& (payType == 2 && prepay >= money
					|| payType == 0 && prepay == 0)){//电子预支付或者未预支付过的订单
				//注意!!!这里存在一个问题，电子预支付不足的话，预支付的钱到不了车场账户
				logger.error("电子预支付足够或者未预支付过>>>orderid:"+orderId);
				autoPayResp = payEpayService.autoPayPosOrder(autoPayReq);
				logger.error(autoPayReq.toString());
				if(autoPayResp.getResult() == 1){
					result = true;//订单结算成功
					resp.setResult(2);//电子支付标识
					resp.setErrmsg(autoPayResp.getErrmsg());
					return resp;
				}
			}
			if(autoPayResp != null && autoPayResp.getResult() < 0){
				logger.error("操作失败>>>orderid:"+orderId+",result:"
			+autoPayResp.getResult()+",errmsg:"+autoPayResp.getErrmsg());
				logger.error(autoPayResp.toString());
				resp.setResult(-1);//应该自动结算，但是结算失败
				resp.setErrmsg(autoPayResp.getErrmsg());
				return resp;
			}
			if(autoPayResp == null || autoPayResp.getResult() == 0){
				logger.error("需手动结算>>>orderid:"+orderId);
				resp.setResult(0);//需要手动结算标识
				resp.setErrmsg("自动结算订单失败，需手动结算");
				return resp;
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {//删除锁
			boolean b = memcacheUtils.delLock(lock);
			logger.error("删除锁lock:"+lock+"b:"+b);
			if(result){//订单结算成功，更新出车数量
				boolean b1 = commonMethods.updateInOutCar(workId, 1);
				logger.error("更新出场车辆,b1:"+b1);
			}
		}
		resp.setResult(-1);//应该自动结算，但是结算失败
		resp.setErrmsg("系统错误");
		return resp;
	}

	@Override
	public ManuPayPosOrderResp manuPayPosOrder(ManuPayPosOrderFacadeReq req) {
		ManuPayPosOrderResp resp = new ManuPayPosOrderResp();
		String lock = null;
		long workId = -1L;//工作记录编号
		boolean result = false;//订单记录生成结果
		try {
			logger.error(req.toString());
			//----------------------------参数--------------------------------//
			Long curTime = req.getCurTime();
			Long orderId = req.getOrderId(); 
			Double money = req.getMoney();//总金额
			String imei  =  req.getImei();//手机串号
			Integer version = req.getVersion();//版本号
			Long uid = req.getUid();
			String nfc_uuid = req.getNfc_uuid();
			int payType = req.getPayType();//支付方式 0：现金支付 1：刷卡支付
			int bindcard = req.getBindcard();//0:客户端弹出绑定车主手机号的提示框 1：不弹出提示框直接刷卡预付
			Long groupId = req.getGroupId();
			Long endTime = req.getEndTime();
			//----------------------------校验参数--------------------------------//
			if(orderId <= 0 
					|| uid <= 0 
					|| money < 0
					|| groupId <= 0){//money可以为零
				resp.setResult(-1);
				resp.setErrmsg("参数错误");
				return resp;
			}
			//----------------------------分布式锁--------------------------------//
			lock = commonMethods.getLock(orderId);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				resp.setResult(-1);
				resp.setErrmsg("并发请求错误");
				return resp;
			}
			//------------------------多线程并行查询------------------------//
			ExecutorService pool = ExecutorsUtil.getExecutorService();
			ExeCallable callable0 = new ExeCallable(uid, orderId, 0);//上班记录
			ExeCallable callable1 = new ExeCallable(uid, orderId, 1);//订单信息
			ExeCallable callable2 = new ExeCallable(uid, orderId, 2);//订单绑定的车检器订单
			
			Future<Object> future0 = pool.submit(callable0);
			Future<Object> future1 = pool.submit(callable1);
			Future<Object> future2 = pool.submit(callable2);
			
			WorkRecord workRecord = (WorkRecord)future0.get();
			Order order = (Order)future1.get();
			Long brethOrderId = (Long)future2.get();
			//----------------------------校验上班记录--------------------------------//
			if(workRecord == null){
				resp.setResult(-1);
				resp.setErrmsg("未在当前泊位所属的泊位段签到");
				return resp;
			}
			workId = workRecord.getId();
			logger.error("workId:"+workId);
			//----------------------------获取订单信息--------------------------------//
			if(order == null){
				resp.setResult(-1);
				resp.setErrmsg("订单记录不存在");
				return resp;
			}
			logger.error(order.toString());
			double prepay = order.getPrepaid();
			long userId = order.getUin();
			String carNumber = order.getCar_number();
			long startTime = order.getCreate_time();
			int state = order.getState();
			int cType = order.getC_type();
			if(state == 1){
				resp.setResult(-1);
				resp.setErrmsg("订单已结算");
				return resp;
			}
			if(state == 2){
				resp.setResult(-1);
				resp.setErrmsg("订单已置为逃单");
				return resp;
			}
			if(prepay >= money || cType == 5){//预付金额大于支付金额，此时应该用autoPayPosOrder结算
				resp.setResult(-1);
				resp.setErrmsg("结算失败");
				return resp;
			}
			double pursueMoney = money;
			if(prepay > 0){
				pursueMoney = StringUtils.formatDouble(money - prepay);
			}
			logger.error("pursueMoney:"+pursueMoney);
			//----------------------------用户信息--------------------------------//
			if(userId <= 0 && carNumber != null && !"".equals(carNumber)){
				Car car = readService.getPOJO("select * from car_info_tb " +
						" where car_number=? and state=?", new Object[]{carNumber, 1}, Car.class);
				if(car != null && car.getUin() > 0){
					userId = car.getUin();
				}
			}
			logger.error(userId);
			//------------------------根据车间亲订单获取订单结算信息----------------------//
			if(endTime < 0){
				endTime = commonMethods.getOrderEndTime(brethOrderId, uid, curTime);
			}
			String duration = StringUtils.getTimeString(startTime, endTime);
			logger.error("endTime:" + endTime+ ",brethOrderId:"+brethOrderId);
			//------------------------------具体逻辑------------------------------//
			
			ManuPayPosOrderReq manuPayReq = new ManuPayPosOrderReq();
			manuPayReq.setOrder(order);
			manuPayReq.setMoney(money);
			manuPayReq.setImei(imei);
			manuPayReq.setUid(uid);
			manuPayReq.setVersion(version);
			manuPayReq.setWorkId(workId);
			manuPayReq.setUserId(userId);
			manuPayReq.setEndTime(endTime);
			manuPayReq.setBerthOrderId(brethOrderId);
			manuPayReq.setNfc_uuid(nfc_uuid);
			manuPayReq.setBindcard(bindcard);
			manuPayReq.setGroupId(groupId);
			logger.error(manuPayReq.toString());
			ManuPayPosOrderResp manuPayResp = null;
			if(payType == 0){
				logger.error("现金结算>>>orderid:"+orderId);
				manuPayResp = payCashService.manuPayPosOrder(manuPayReq);
				logger.error(manuPayResp.toString());
				if(manuPayResp.getResult() == 1){
					result = true;//订单结算成功
					resp.setErrmsg("预收金额："+prepay+"元，应收金额："+money+"元，应补收："+pursueMoney+"元");
					resp.setDuration(duration);
					resp.setResult(1);
					return resp;
				}
			}else if(payType == 1){
				logger.error("刷卡结算>>>orderid:"+orderId);
				manuPayResp = payCardService.manuPayPosOrder(manuPayReq);
				logger.error(manuPayResp.toString());
				if(manuPayResp.getResult() == 1){
					result = true;//订单结算成功
					resp.setErrmsg(manuPayResp.getErrmsg());
					resp.setDuration(duration);
					resp.setResult(1);
					return resp;
				}else if(manuPayResp.getResult() == -5){
					resp.setErrmsg(manuPayResp.getErrmsg());
					resp.setResult(-5);
					return resp;
				}else if(manuPayResp.getResult() == -6){
					logger.error("提示绑定用户>>>orderid:"+orderId);
					resp.setErrmsg(manuPayResp.getErrmsg());
					resp.setResult(-6);
					return resp;
				}
			}
			if(manuPayResp != null && manuPayResp.getResult() != 1){
				logger.error("结算失败>>>orderid:"+orderId);
				resp.setResult(-1);//应该自动结算，但是结算失败
				resp.setErrmsg(manuPayResp.getErrmsg());
				return resp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {//删除锁
			boolean b = memcacheUtils.delLock(lock);
			logger.error("删除锁lock:"+lock+",b:"+b);
			if(result){//订单结算成功，更新出车数量
				boolean b1 = commonMethods.updateInOutCar(workId, 1);
				logger.error("更新出场车辆,b1:"+b1);
			}
		}
		resp.setResult(-1);//应该自动结算，但是结算失败
		resp.setErrmsg("系统错误");
		return resp;
	}

	@Override
	public PayEscapePosOrderResp payEscapePosOrder(PayEscapePosOrderFacadeReq req) {
		PayEscapePosOrderResp resp = new PayEscapePosOrderResp();
		String lock = null;
		try {
			logger.error(req.toString());
			//----------------------------参数--------------------------------//
			Long curTime = req.getCurTime();
			Long orderId = req.getOrderId(); 
			Double money = req.getMoney();//总金额
			String imei  =  req.getImei();//手机串号
			Integer version = req.getVersion();//版本号
			Long uid = req.getUid();
			String nfc_uuid = req.getNfc_uuid();
			int payType = req.getPayType();//支付方式 0：现金支付 1：刷卡支付
			int bindcard = req.getBindcard();//0:客户端弹出绑定车主手机号的提示框 1：不弹出提示框直接刷卡预付
			Long groupId = req.getGroupId();//追缴收费员所在的运营集团编号
			Long parkId = req.getParkId();//追缴收费员所在的车场
			Long berthId = req.getBerthId();//追缴订单所在的泊位编号（2016-10-14日添加,为了记录在哪个泊位上追缴的订单）
			//----------------------------校验参数--------------------------------//
			if(orderId <= 0 
					|| uid <= 0 
					|| money < 0
					|| groupId <= 0
					|| parkId <= 0){//money可以为零
				resp.setResult(-1);
				resp.setErrmsg("参数错误");
				return resp;
			}
			//----------------------------分布式锁--------------------------------//
			lock = commonMethods.getLock(orderId);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				resp.setResult(-1);
				resp.setErrmsg("并发请求错误");
				return resp;
			}
			//------------------------多线程并行查询------------------------//
			ExecutorService pool = ExecutorsUtil.getExecutorService();
			ExeCallable callable0 = new ExeCallable(uid, orderId, 0);//上班记录
			ExeCallable callable1 = new ExeCallable(uid, orderId, 1);//订单信息
			ExeCallable callable2 = new ExeCallable(uid, orderId, 2);//订单绑定的车检器订单
			
			Future<Object> future0 = pool.submit(callable0);
			Future<Object> future1 = pool.submit(callable1);
			Future<Object> future2 = pool.submit(callable2);
			
			WorkRecord workRecord = (WorkRecord)future0.get();
			Order order = (Order)future1.get();
			Long brethOrderId = (Long)future2.get();
			//----------------------------校验上班记录--------------------------------//
			if(workRecord == null){
				resp.setResult(-1);
				resp.setErrmsg("未在当前泊位所属的泊位段签到");
				return resp;
			}
			long workId = workRecord.getId();
			long berthSegId = workRecord.getBerthsec_id();
			logger.error("workId:"+workId+",berthSegId:"+berthSegId);
			//----------------------------获取订单信息--------------------------------//
			if(order == null){
				resp.setResult(-1);
				resp.setErrmsg("订单记录不存在");
				return resp;
			}
			logger.error(order.toString());
			double prepay = order.getPrepaid();
			long userId = order.getUin();
			String carNumber = order.getCar_number();
			int state = order.getState();
			int cType = order.getC_type();
			if(state == 0){
				resp.setResult(-1);
				resp.setErrmsg("非逃单，请正常结算");
				return resp;
			}
			if(state == 1){
				resp.setResult(-1);
				resp.setErrmsg("订单已结算");
				return resp;
			}
			if(prepay >= money || cType == 5){//预付金额大于支付金额，此时应该用autoPayPosOrder结算
				resp.setResult(-1);
				resp.setErrmsg("结算失败");
				return resp;
			}
			double pursueMoney = money;
			if(prepay > 0){
				pursueMoney = StringUtils.formatDouble(money - prepay);
			}
			logger.error("pursueMoney:"+pursueMoney);
			//----------------------------用户信息--------------------------------//
			if(userId <= 0 && carNumber != null && !"".equals(carNumber)){
				Car car = readService.getPOJO("select * from car_info_tb " +
						" where car_number=? and state=?", 
						new Object[]{carNumber, 1}, Car.class);
				if(car != null && car.getUin() > 0){
					userId = car.getUin();
				}
			}
			logger.error(userId);
			//------------------------根据车间亲订单获取订单结算信息----------------------//
			logger.error("brethOrderId:"+brethOrderId);
			//------------------------------具体逻辑------------------------------//
			
			PayEscapePosOrderReq escapeReq = new PayEscapePosOrderReq();
			escapeReq.setOrder(order);
			escapeReq.setMoney(money);
			escapeReq.setImei(imei);
			escapeReq.setUid(uid);
			escapeReq.setVersion(version);
			escapeReq.setBerthSegId(berthSegId);
			escapeReq.setUserId(userId);
			escapeReq.setBerthOrderId(brethOrderId);
			escapeReq.setNfc_uuid(nfc_uuid);
			escapeReq.setBindcard(bindcard);
			escapeReq.setGroupId(groupId);
			escapeReq.setBerthId(berthId);
			escapeReq.setParkId(parkId);
			logger.error(escapeReq.toString());
			PayEscapePosOrderResp payEscapeResp = null;
			if(payType == 0){
				logger.error("现金结算>>>orderid:"+orderId);
				payEscapeResp = payCashService.payEscapePosOrder(escapeReq);
				logger.error(payEscapeResp.toString());
				if(payEscapeResp.getResult() == 1){
					resp.setErrmsg("预收金额："+prepay+"元，应收金额："+money+"元，应追缴："+pursueMoney+"元");
					resp.setResult(1);
					return resp;
				}
			}else if(payType == 1){
				logger.error("刷卡结算>>>orderid:"+orderId);
				payEscapeResp = payCardService.payEscapePosOrder(escapeReq);
				logger.error(payEscapeResp.toString());
				if(payEscapeResp.getResult() == 1){
					resp.setErrmsg(payEscapeResp.getErrmsg());
					resp.setResult(1);
					return resp;
				}else if(payEscapeResp.getResult() == -5){//卡片需要激活
					resp.setErrmsg(payEscapeResp.getErrmsg());
					resp.setResult(-5);
					return resp;
				}else if(payEscapeResp.getResult() == -6){
					logger.error("提示绑定用户>>>orderid:"+orderId);
					resp.setErrmsg(payEscapeResp.getErrmsg());
					resp.setResult(-6);
					return resp;
				}
			}
			if(payEscapeResp != null && payEscapeResp.getResult() != 1){
				logger.error("结算失败>>>orderid:"+orderId);
				resp.setResult(-1);//应该自动结算，但是结算失败
				resp.setErrmsg(payEscapeResp.getErrmsg());
				return resp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {//删除锁
			boolean b = memcacheUtils.delLock(lock);
			logger.error("删除锁lock:"+lock+",b:"+b);
		}
		resp.setResult(-1);//应该自动结算，但是结算失败
		resp.setErrmsg("系统错误");
		return resp;
	}
	
	class ExeCallable implements Callable<Object>{
		private Long uid = -1L;
		private Long orderId = -1L;
		private int type;
		ExeCallable(Long uid, Long orderId, int type){
			this.uid = uid;
			this.orderId = orderId;
			this.type = type;
		}
		@Override
		public Object call() throws Exception {
			Object result = null;
			try {
				switch (type) {
				case 0:
					result = commonMethods.getWorkRecord(uid);
					break;
				case 1:
					result = writeService.getPOJO("select * from order_tb where id=? ", 
							new Object[]{orderId}, Order.class);
					break;
				case 2:
					result = commonMethods.getBerthOrderId(orderId);//地磁订单编号
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}
	}

}
