package com.zld.facade.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zld.facade.GenPosOrderFacade;
import com.zld.impl.CommonMethods;
import com.zld.impl.MemcacheUtils;
import com.zld.pojo.Berth;
import com.zld.pojo.Car;
import com.zld.pojo.GenPosOrderFacadeReq;
import com.zld.pojo.GenPosOrderFacadeResp;
import com.zld.pojo.GenPosOrderReq;
import com.zld.pojo.GenPosOrderResp;
import com.zld.pojo.WorkRecord;
import com.zld.service.DataBaseService;
import com.zld.service.GenPosOrderService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.ExecutorsUtil;
import com.zld.utils.TimeTools;
@Component
public class GenPosOrderFacadeImpl implements GenPosOrderFacade {
	@Autowired
	private DataBaseService writeService;
	@Autowired
	private PgOnlyReadService readService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	@Resource(name = "genCash")
	private GenPosOrderService genOrderCashService;
	@Autowired
	@Resource(name = "genCard")
	private GenPosOrderService genOrderCardService;
	@Autowired
	private MemcacheUtils memcacheUtils;
	
	Logger logger = Logger.getLogger(GenPosOrderFacadeImpl.class);
	@Override
	public GenPosOrderFacadeResp genPosOrder(GenPosOrderFacadeReq req) {
		GenPosOrderFacadeResp resp = new GenPosOrderFacadeResp();
		String lock1 = null;
		String lock2 = null;
		long workId = -1L;//工作记录编号
		boolean result = false;//订单记录生成结果
		try {
			logger.error(req.toString());
			Long orderId = req.getOrderId();//订单号
			String carNumber = req.getCarNumber();//车牌号
			Long berthId = req.getBerthId();//泊位
			String imei = req.getImei();
			Long uid = req.getUid();//收费员编号
			Integer version = req.getVersion();//版本号
			Long parkId = req.getParkId();//车场编号
			Long groupId = req.getGroupId();//运营集团编号
			Long curTime = req.getCurTime();//当前时间
			Integer carType =req.getCarType();//车辆类型
			//-------------------------预付参数-----------------------//
			Integer payType = req.getPayType();//预付类型 0:现金预付 1：刷卡预付
			String nfc_uuid = req.getNfc_uuid();//刷卡预支付的卡片编号
			Integer bindcard = req.getBindcard();//预支付用到的参数：0:客户端弹出绑定车主手机号的提示框 1：不弹出提示框直接刷卡预付
			Double prepay = req.getPrepay();//预支付金额
			//-------------------------校验参数-----------------------//
			if(carNumber == null
					|| "".equals(carNumber)
					|| berthId <= 0
					|| uid <= 0
					|| parkId <= 0
					|| groupId <= 0){
				resp.setResult(0);
				resp.setErrmsg("参数错误");
			}
			//-------------------------订单参数-----------------------//
			if(orderId <= 0){
				orderId = writeService.getkey("seq_order_tb");
			}
			logger.error("orderid:"+orderId);
			//----------------------------分布式锁--------------------------------//
			lock1 = commonMethods.getLock(carNumber);
			if(!memcacheUtils.addLock(lock1)){
				logger.error("lock1:"+lock1);
				resp.setResult(0);
				resp.setErrmsg("并发请求错误");
				return resp;
			}
			lock2 = commonMethods.getLock(berthId);
			if(!memcacheUtils.addLock(lock2)){
				logger.error("lock2:"+lock2);
				resp.setResult(0);
				resp.setErrmsg("并发请求错误");
				return resp;
			}
			//------------------------多线程并行查询------------------------//
			ExecutorService pool = ExecutorsUtil.getExecutorService();
			ExeCallable callable0 = new ExeCallable(req, 0);//上班记录
			ExeCallable callable1 = new ExeCallable(req, 1);//泊位信息
			ExeCallable callable2 = new ExeCallable(req, 2);//需用主库查询，防并发
			ExeCallable callable3 = new ExeCallable(req, 3);//需用主库查询，防并发
			ExeCallable callable4 = new ExeCallable(req, 4);//用户信息
			ExeCallable callable5 = new ExeCallable(req, 5);//车辆类型;
			ExeCallable callable6 = new ExeCallable(req, 6);//月卡会员
			ExeCallable callable7 = new ExeCallable(req, 7);//可绑定的车检器订单
			
			Future<Object> future0 = pool.submit(callable0);
			Future<Object> future1 = pool.submit(callable1);
			Future<Object> future2 = pool.submit(callable2);
			Future<Object> future3 = pool.submit(callable3);
			Future<Object> future4 = pool.submit(callable4);
			Future<Object> future5 = pool.submit(callable5);
			Future<Object> future6 = pool.submit(callable6);
			Future<Object> future7 = pool.submit(callable7);
			
			WorkRecord workRecord = (WorkRecord)future0.get();
			Berth berth = (Berth)future1.get();
			Long count = (Long)future2.get();
			Long bcount = (Long)future3.get();
			Car car = (Car)future4.get();
			carType = (Integer)future5.get();
			Boolean monthUser = (Boolean)future6.get();
			Long berthOrderId = (Long)future7.get();
			//------------------------校验上班记录---------------------//
			if(workRecord == null){
				resp.setResult(0);
				resp.setErrmsg("未在当前泊位段签到");
				return resp;
			}
			workId = workRecord.getId();
			logger.error("workId:"+workId);
			//----------------------------泊位信息校验--------------------//
			if(berth == null){
				resp.setResult(0);
				resp.setErrmsg("泊位信息错误");
				return resp;
			}
			//-------------------------校验进场----------------------//
			logger.error("count:"+count);
			if(count > 0){
				resp.setResult(0);
				resp.setErrmsg("该车牌已有入场订单");
				return resp;
			}
			logger.error("bcount:"+bcount);
			if(bcount > 0){
				resp.setResult(0);
				resp.setErrmsg("该泊位已有入场订单");
				return resp;
			}
			//----------------------------获取入场用户信息-----------------------//
			Long userId = -1L;//车辆所属车主账号
			if(car != null){
				userId = car.getUin();
			}
			//----------------------------获取车辆类型--------------------------//
			logger.error("carType:"+carType+",userId:"+userId);
			//---------------------------确定进场方式---------------------------//
			int cType = 2;//2:照牌进场
			if(monthUser){//月卡会员
				cType =5;
			}
			logger.error("cType:"+cType);
			//--------------------------获取可绑定的车检器订单信息-------------------//
			Long startTime = commonMethods.getOrderStartTime(berthOrderId, uid, curTime);
			logger.error("berthOrderId:"+berthOrderId+",orderid:"+orderId+",startTime:"+startTime);
			//---------------------------具体逻辑---------------------------//
			resp.setOrderid(orderId);
			resp.setBtime(TimeTools.getTime_yyyyMMdd_HHmmss(startTime * 1000));
			
			GenPosOrderReq genPosOrderReq = new GenPosOrderReq();
			genPosOrderReq.setBerth(berth);
			genPosOrderReq.setBerthOrderId(berthOrderId);
			genPosOrderReq.setBindcard(bindcard);
			genPosOrderReq.setCarNumber(carNumber);
			genPosOrderReq.setCarType(carType);
			genPosOrderReq.setcType(cType);
			genPosOrderReq.setGroupId(groupId);
			genPosOrderReq.setImei(imei);
			genPosOrderReq.setNfc_uuid(nfc_uuid);
			genPosOrderReq.setOrderId(orderId);
			genPosOrderReq.setParkId(parkId);
			genPosOrderReq.setPrepay(prepay);
			genPosOrderReq.setStartTime(startTime);
			genPosOrderReq.setUid(uid);
			genPosOrderReq.setUserId(userId);
			genPosOrderReq.setVersion(version);
			genPosOrderReq.setWorkId(workId);
			
			
			GenPosOrderResp genPosOrderResp = null;
			if(payType == 0){
				logger.error("现金预支付");
				genPosOrderResp = genOrderCashService.genPosOrder(genPosOrderReq);
				logger.error(genPosOrderResp.toString());
				if(genPosOrderResp.getResult() == 1){
					result = true;//订单生成成功
					resp.setResult(1);
					resp.setErrmsg(genPosOrderResp.getErrmsg());
					return resp;
				}
			}else if(payType == 1){
				logger.error("刷卡预支付");
				genPosOrderResp = genOrderCardService.genPosOrder(genPosOrderReq);
				logger.error(genPosOrderResp.toString());
				if(genPosOrderResp.getResult() == 1){
					result = true;//订单生成成功
					resp.setResult(1);
					resp.setErrmsg(genPosOrderResp.getErrmsg());
					return resp;
				}else if(genPosOrderResp.getResult() == -5){
					resp.setResult(-5);//未激活卡片,提示去激活
					resp.setErrmsg(genPosOrderResp.getErrmsg());
					return resp;
				}else if(genPosOrderResp.getResult() == -6){
					resp.setResult(-6);//提示绑定卡片
					resp.setErrmsg(genPosOrderResp.getErrmsg());
					return resp;
				}
			}
			if(genPosOrderResp != null && genPosOrderResp.getResult() != 1){
				logger.error("订单生成失败>>>orderid:"+orderId);
				resp.setResult(0);//应该自动结算，但是结算失败
				resp.setErrmsg(genPosOrderResp.getErrmsg());
				return resp;
			}
			logger.error("逻辑错误>>>orderid:"+orderId);
		} catch (Exception e) {
			logger.error(e);
		} finally {//删除锁
			boolean b1 = memcacheUtils.delLock(lock1);
			boolean b2 = memcacheUtils.delLock(lock2);
			logger.error("删除锁lock1:"+lock1+",b1:"+b1+",lock2:"+lock2+",b2:"+b2);
			if(result){//订单生成成功，更新进车数量
				boolean b3 = commonMethods.updateInOutCar(workId, 0);
				logger.error("更新进场车辆,b3:"+b3);
			}
		}
		resp.setResult(0);
		resp.setErrmsg("系统错误");
		return resp;
	}
	
	class ExeCallable implements Callable<Object>{
		private GenPosOrderFacadeReq req;
		private int type;
		ExeCallable(GenPosOrderFacadeReq req, int type){
			this.req = req;
			this.type = type;
		}
		@Override
		public Object call() throws Exception {
			Object result = null;
			try {
				switch (type) {
				case 0:
					result = commonMethods.getWorkRecord(req.getUid());
					break;
				case 1:
					result = commonMethods.berth(req.getBerthId());
					break;
				case 2:
					result = writeService.getLong("select count(ID) from order_tb where " +
							"state =? and car_number=? and comid=? ", 
							new Object[]{0, req.getCarNumber(), req.getParkId()});//需用主库查询，防并发
					break;
				case 3:
					result = writeService.getLong("select count(p.id) from com_park_tb p,order_tb o" +
							" where p.order_id=o.id and p.state=? and o.state=? and p.id=?", 
							new Object[]{1, 0, req.getBerthId()});
					break;
				case 4:
					result = readService.getPOJO("select * from car_info_tb where " +
							" car_number=? and state=? ", new Object[]{req.getCarNumber(), 1}, Car.class);
					break;
				case 5:
					if(req.getCarType() <= 0){
						result = commonMethods.getCarType(req.getCarNumber(), req.getParkId());
					}else{
						result = req.getCarType();//pos机可能传该参数
					}
					break;
				case 6:
					result = commonMethods.isMonthUser(req.getCarNumber(), req.getParkId());;
					break;
				case 7:
					result = commonMethods.getPreBerthOrderId(req.getBerthId());
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
