package com.zld.facade.impl;

import com.zld.facade.StatsAccountFacade;
import com.zld.pojo.*;
import com.zld.service.StatsAccountService;
import com.zld.service.StatsCardService;
import com.zld.service.StatsOrderService;
import com.zld.utils.ExecutorsUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
@Component
public class StatsAccountFacadeImpl implements StatsAccountFacade {
	@Autowired
	@Resource(name = "parkUserEpay")
	private StatsAccountService parkUserEpayService;
	@Autowired
	@Resource(name = "parkEpay")
	private StatsAccountService parkEpayService;
	@Autowired
	@Resource(name = "groupEpay")
	private StatsAccountService groupEpayService;
	@Autowired
	@Resource(name = "tenantEpay")
	private StatsAccountService tenantEpayService;
	@Autowired
	@Resource(name = "parkUserCash")
	private StatsAccountService parkUserCashService;
	@Autowired
	@Resource(name = "card")
	private StatsCardService cardService;
	@Autowired
	@Resource(name = "escapeOrder")
	private StatsOrderService escapeOrderService;
	@Autowired
	@Resource(name = "sensorOrder")
	private StatsOrderService sensorService;

	Logger logger = Logger.getLogger(StatsAccountFacadeImpl.class);

	@Override
	public StatsFacadeResp statsParkUserAccount(StatsReq req) {
		//logger.error(req.toString());
		StatsFacadeResp resp = new StatsFacadeResp();
		try {
			req.setType(0);//0：按收费员编号统计 1：按车场编号统计 2：按泊位段编号查询 3：按泊位查询 4:按运营集团查询
			resp = mergeFee(req);
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.setResult(-1);
		resp.setErrmsg("系统错误");
		return resp;
	}

	@Override
	public StatsFacadeResp statsParkAccount(StatsReq req) {
		//logger.error(req.toString());
		StatsFacadeResp resp = new StatsFacadeResp();
		try {
			req.setType(1);//0：按收费员编号统计 1：按车场编号统计 2：按泊位段编号查询 3：按泊位查询 4:按运营集团查询
			resp = mergeFee(req);
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.setResult(-1);
		resp.setErrmsg("系统错误");
		return resp;
	}

	@Override
	public StatsFacadeResp statsBerthSegAccount(StatsReq req) {
		//logger.error(req.toString());
		StatsFacadeResp resp = new StatsFacadeResp();
		try {
			req.setType(2);//0：按收费员编号统计 1：按车场编号统计 2：按泊位段编号查询 3：按泊位查询 4:按运营集团查询
			resp = mergeFee(req);
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.setResult(-1);
		resp.setErrmsg("系统错误");
		return resp;
	}

	@Override
	public StatsFacadeResp statsBerthAccount(StatsReq req) {
		//logger.error(req.toString());
		StatsFacadeResp resp = new StatsFacadeResp();
		try {
			req.setType(3);//0：按收费员编号统计 1：按车场编号统计 2：按泊位段编号查询 3：按泊位查询 4:按运营集团查询
			resp = mergeFee(req);
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.setResult(-1);
		resp.setErrmsg("系统错误");
		return resp;
	}

	@Override
	public StatsFacadeResp statsGroupAccount(StatsReq req) {
		//logger.error(req.toString());
		StatsFacadeResp resp = new StatsFacadeResp();
		try {
			req.setType(4);//0：按收费员编号统计 1：按车场编号统计 2：按泊位段编号查询 3：按泊位查询 4:按运营集团查询
			resp = mergeFee(req);
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.setResult(-1);
		resp.setErrmsg("系统错误");
		return resp;
	}

	/**
	 * 合并所有账目，封装给客户端
	 * @param req
	 * @return
	 */
	private StatsFacadeResp mergeFee(StatsReq req){
		StatsFacadeResp resp = new StatsFacadeResp();
		try {
			long startTime = req.getStartTime();
			long endTime = req.getEndTime();
			List<Object> idList = req.getIdList();
			if(startTime <= 0
					|| endTime <= 0
					|| idList == null
					|| idList.isEmpty()){
				resp.setResult(-1);
				resp.setErrmsg("参数错误");
				return resp;
			}
			ExecutorService pool = ExecutorsUtil.getExecutorService();
			ExeCallable callable0 = new ExeCallable(req, 0);
			ExeCallable callable1 = new ExeCallable(req, 1);
			ExeCallable callable2 = new ExeCallable(req, 2);
			ExeCallable callable3 = new ExeCallable(req, 3);
			ExeCallable callable4 = new ExeCallable(req, 4);
			ExeCallable callable5 = new ExeCallable(req, 5);
			ExeCallable callable6 = new ExeCallable(req, 6);
			ExeCallable callable7 = new ExeCallable(req, 7);

			Future<Object> future0 = pool.submit(callable0);
			Future<Object> future1 = pool.submit(callable1);
			Future<Object> future2 = pool.submit(callable2);
			Future<Object> future3 = pool.submit(callable3);
			Future<Object> future4 = pool.submit(callable4);
			Future<Object> future5 = pool.submit(callable5);
			Future<Object> future6 = pool.submit(callable6);
			Future<Object> future7 = pool.submit(callable7);


			StatsAccountResp parkUserEpayResp = (StatsAccountResp)future0.get();
			StatsAccountResp parkEpayResp = (StatsAccountResp)future1.get();
			StatsAccountResp groupEpayResp = (StatsAccountResp)future2.get();
			StatsAccountResp tenantEpayResp = (StatsAccountResp)future3.get();
			StatsAccountResp parkUserCashResp = (StatsAccountResp)future4.get();
			StatsCardResp cardResp = (StatsCardResp)future5.get();
			StatsOrderResp escapeOrderResp = (StatsOrderResp)future6.get();
			StatsOrderResp sensorOrderResp = (StatsOrderResp)future7.get();

			List<StatsAccountClass> classes = new ArrayList<StatsAccountClass>();
			//*****************************初始化********************************//
			for(Object object : idList){
				StatsAccountClass accountClass = new StatsAccountClass();
				accountClass.setId((Long)object);
				classes.add(accountClass);
			}
			//**************************收费员电子账户*****************************//
			if(parkUserEpayResp != null){
				List<StatsAccount> accounts = parkUserEpayResp.getAccounts();
				setAccountClass1(classes, accounts, 0);
			}
			//**************************车场电子账户*****************************//
			if(parkEpayResp != null){
				List<StatsAccount> accounts = parkEpayResp.getAccounts();
				setAccountClass1(classes, accounts, 0);
			}
			//**************************运营集团电子账户*****************************//
			if(groupEpayResp != null){
				List<StatsAccount> accounts = groupEpayResp.getAccounts();
				setAccountClass1(classes, accounts, 0);
			}
			//**************************城市商户电子账户*****************************//
			if(tenantEpayResp != null){
				List<StatsAccount> accounts = tenantEpayResp.getAccounts();
				setAccountClass1(classes, accounts, 0);
			}
			//**************************收费员现金账户*****************************//
			if(parkUserCashResp != null){
				List<StatsAccount> accounts = parkUserCashResp.getAccounts();
				setAccountClass1(classes, accounts, 1);
			}
			//**************************逃单*****************************//
			if(escapeOrderResp != null){
				List<StatsOrder> orders = escapeOrderResp.getOrders();
				setAccountClass2(classes, orders, 0);
			}
			//**************************车检器订单*****************************//
			if(sensorOrderResp != null){
				List<StatsOrder> orders = sensorOrderResp.getOrders();
				setAccountClass2(classes, orders, 1);
			}
			//**************************卡片账户*****************************//
			if(cardResp != null){
				List<StatsCard> cards = cardResp.getCards();
				setAccountClass3(classes, cards, 0);
			}
			resp.setClasses(classes);
			resp.setResult(1);
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.setResult(-1);
		resp.setErrmsg("系统错误");
		return resp;
	}

	/**
	 * 流水账分类
	 * @param classes
	 * @param accounts
	 * @param type 0：电子支付 1：现金支付 2：卡片支付
	 */
	private void setAccountClass1(List<StatsAccountClass> classes, List<StatsAccount> accounts, int type){
		try {
			if(accounts != null && !accounts.isEmpty()){
				for(StatsAccount account : accounts){
					long id = account.getId();
					for(StatsAccountClass accountClass : classes){
						long classId = accountClass.getId();
						if(id == classId){
							switch (type) {
								case 0://电子支付
									accountClass.setePayParkingFee(accountClass.getePayParkingFee() + account.getParkingFee());
									accountClass.setePayPrepayFee(accountClass.getePayPrepayFee() + account.getPrepayFee());
									accountClass.setePayRefundFee(accountClass.getePayRefundFee() + account.getRefundFee());
									accountClass.setePayAddFee(accountClass.getePayAddFee() + account.getAddFee());
									accountClass.setePayPursueFee(accountClass.getePayPursueFee() + account.getPursueFee());
									break;
								case 1://现金支付
									accountClass.setCashParkingFee(accountClass.getCashParkingFee()+ account.getParkingFee());
									accountClass.setCashPrepayFee(accountClass.getCashPrepayFee() + account.getPrepayFee());
									accountClass.setCashRefundFee(accountClass.getCashRefundFee() + account.getRefundFee());
									accountClass.setCashAddFee(accountClass.getCashAddFee() + account.getAddFee());
									accountClass.setCashPursueFee(accountClass.getCashPursueFee() + account.getPursueFee());
									break;
								default:
									break;
							}
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 订单分类
	 * @param classes
	 * @param orders
	 * @param type 0：逃单 1：车检器订单
	 */
	private void setAccountClass2(List<StatsAccountClass> classes, List<StatsOrder> orders, int type){
		try {
			if(orders != null && !orders.isEmpty()){
				for(StatsOrder order : orders){
					long id = order.getId();
					for(StatsAccountClass accountClass : classes){
						long classId = accountClass.getId();
						if(id == classId){
							switch (type) {
								case 0://逃单未追缴
									accountClass.setEscapeFee(order.getEscapeFee());
									break;
								case 1://车检器订单
									accountClass.setSensorOrderFee(order.getSensorFee());
									break;
								default:
									break;
							}
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 卡片分类
	 * @param classes
	 * @param cards
	 * @param type 0：商家自己发行的卡片
	 */
	private void setAccountClass3(List<StatsAccountClass> classes, List<StatsCard> cards, int type){
		try {
			if(cards != null && !cards.isEmpty()){
				for(StatsCard card : cards){
					long id = card.getId();
					for(StatsAccountClass accountClass : classes){
						long classId = accountClass.getId();
						if(id == classId){
							switch (type) {
								case 0://商家自己发行的卡片
									accountClass.setCardParkingFee(card.getParkingFee());
									accountClass.setCardPrepayFee(card.getPrepayFee());
									accountClass.setCardRefundFee(card.getRefundFee());
									accountClass.setCardAddFee(card.getAddFee());
									accountClass.setCardPursueFee(card.getPursueFee());

									accountClass.setCardActCount(card.getActCount());
									accountClass.setCardActFee(card.getActFee());
									accountClass.setCardBindCount(card.getBindCount());
									accountClass.setCardChargeCashFee(card.getChargeCashFee());
									accountClass.setCardRegCount(card.getRegCount());
									accountClass.setCardRegFee(card.getRegFee());
									accountClass.setCardReturnCount(card.getReturnCount());
									accountClass.setCardReturnFee(card.getReturnFee());
									break;

								default:
									break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class ExeCallable implements Callable<Object>{
		private StatsReq req;
		private int type;
		ExeCallable(StatsReq req, int type){
			this.req = req;
			this.type = type;
		}
		@Override
		public Object call() throws Exception {
			Object result = null;
			try {
				switch (type) {
					case 0:
						result = parkUserEpayService.statsAccount(req);
						break;
					case 1:
						result = parkEpayService.statsAccount(req);
						break;
					case 2:
						result = groupEpayService.statsAccount(req);
						break;
					case 3:
						result = tenantEpayService.statsAccount(req);
						break;
					case 4:
						result = parkUserCashService.statsAccount(req);
						break;
					case 5:
						result = cardService.statsCard(req);
						break;
					case 6:
						result = escapeOrderService.statsOrder(req);
						break;
					case 7:
						result = sensorService.statsOrder(req);
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
