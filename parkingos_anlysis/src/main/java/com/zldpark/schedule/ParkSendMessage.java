package com.zldpark.schedule;

import com.alibaba.fastjson.JSONObject;
import com.zldpark.service.DataBaseService;
import com.zldpark.service.PgOnlyReadService;
import com.zldpark.utils.Check;
import com.zldpark.utils.HttpProxy;
import com.zldpark.utils.StringUtils;
import com.zldpark.utils.TimeTools;


import org.apache.log4j.Logger;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;


public class ParkSendMessage extends TimerTask {

	private static Logger logger = Logger.getLogger(ParkSendMessage.class);
	private DataBaseService dataBaseService;
	private PgOnlyReadService pgOnlyReadService;

	public ParkSendMessage(DataBaseService dataBaseService,PgOnlyReadService pgOnlyReadService ){
		this.dataBaseService = dataBaseService;
		this.pgOnlyReadService = pgOnlyReadService;
	}

	@Override
	public void run() {
		logger.error("数据库操作对象:" + dataBaseService);
		logger.error("开始下行数据定时任务....");
		sendMessageToSDK();
	}

	private void sendMessageToSDK() {
		Long stime = System.currentTimeMillis()/1000 - 86400;
		List<Map<String, Object>> needList = pgOnlyReadService.getAll("select * from sync_info_pool_tb where" +
				" state =? and create_time > ? order by id desc ", new Object[]{0,stime});
		logger.info(">>>>需要同步的数据>>>>>>>"+needList);
		if (needList != null && needList.size() > 0) {
			logger.error(">>>>需要同步的记录数>>>>>>>"+needList.size());
			for (Map<String, Object> infoPoolTb : needList) {
				if (infoPoolTb != null ) {
					Long comid = (Long)infoPoolTb.get("comid");
					Map<String, Object> parkTokenTb =null;
					if(comid!=null){
						parkTokenTb=getChannel(comid);
					}
					if(parkTokenTb==null){//没有在线的sdk,继续
						logger.error("sdk不在线，"+infoPoolTb);
						continue;
					}
					String tableName = (String)infoPoolTb.get("table_name");
					Long tableId = (Long)infoPoolTb.get("table_id");
					Integer operate = (Integer)infoPoolTb.get("operate")+1;
					if (tableName.equals("carower_product")) {
						String result = sendcardMember( tableId, comid, operate,parkTokenTb);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送月卡会员信息结果" + result);
					} else if (tableName.equals("price_tb")) {
						//logger.info("未处理价格同步");
						String result = sendPriceInfo( tableId, comid, operate,parkTokenTb);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送价格信息结果" + result);
					} else if (tableName.equals("product_package_tb")) {
						String result = sendProductPackageInfo(tableId, comid, operate,parkTokenTb);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送月卡套餐信息结果" + result);
					} else if (tableName.equals("user_info_tb")) {
						String result = sendUserInfo(tableId, comid, operate,parkTokenTb);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送收费员信息结果" + result);
					} else if (tableName.equals("order_tb")) {
						logger.info("未处理订单同步");
//						String result = sendOrderInfo(tableId, comid, operate);
//						logger.error(">>>>>>>>>>>>>>>>>>>>>发送结算订单信息结果" + result);
					}else if (tableName.equals("car_type_tb")) {
						String result = sendCarTypeInfo( tableId, comid, operate,parkTokenTb);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送车型信息结果" + result);
					} else if (tableName.equals("zld_black_tb")) {
						String result = sendBlackUser(tableId, comid, operate,parkTokenTb);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送黑名单信息结果" + result);
					} else if (tableName.equals("card_renew_tb")) {
						String result = sendCardReNewInfo(tableId, comid, operate,parkTokenTb);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送月卡续费信息结果" + result);
					}  else if(tableName.equals("com_pass_tb")){
						String result = sendComPassInfo(tableId, comid, operate,parkTokenTb);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送通道信息结果" + result);
					} else {
						logger.error(">>还没有处理当前同步业务:" + tableName);
					}
				}
			}
		}
	}

	/**
	 * 下发黑名单
	 * @param tableId 表数据编号
	 * @param comid 车场编号
	 * @param operate 操作
	 * @return 操作结果
	 */
	private String sendBlackUser(Long tableId, Long comid, Integer operate,Map<String, Object> parkTokenTb) {
		Map<String,Object> black = pgOnlyReadService.getMap("select * from zld_black_tb where id = ? ", new Object[]{tableId});
		//定义下传数据的json对象
		JSONObject jsonSend = new JSONObject();
		//操作类型
		String result = "0";
		//查询出对应的需要下传的数据
		if (black != null ) {
			jsonSend.put("black_uuid", black.get("black_uuid"));//black.getBlackUuid());
			jsonSend.put("car_number", black.get("car_number"));//black.getCarNumber());
			jsonSend.put("operator", black.get("operator"));//black.getOperator());
			jsonSend.put("create_time", black.get("ctime"));//black.getCtime());
			jsonSend.put("resume", black.get("remark"));//black.getRemark());
			jsonSend.put("operate_type", operate);
		} else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "blackuser_sync");
		jsonMesg.put("data", jsonSend);
		logger.error(jsonMesg);
		boolean isSend = doSendMessage(jsonMesg,parkTokenTb);
		logger.error(">>>>>>>>>>>>>>云端发送数据到停车收费系统结果：" + isSend);
		if (isSend) {
			result = "1";
		}
		return result;
	}
	/**
	 * 下发月卡充值记录
	 * @param tableId 表数据编号
	 * @param comid 车场编号
	 * @param operate 操作
	 * @return 操作结果
	 */
	private String sendCardReNewInfo(Long tableId, Long comid, Integer operate,Map<String, Object> parkTokenTb) {
		Map<String,Object> renewTb = pgOnlyReadService.getMap("select * from card_renew_tb where id = ? ", new Object[]{tableId});
		
		//定义下传数据的json对象
		JSONObject jsonSend = new JSONObject();
		//操作类型
		String result = "0";
		//查询出对应的需要下传的数据
		if (renewTb != null ) {
			jsonSend.put("trade_no",renewTb.get("trade_no"));//renewTb.getTradeNo() );
			jsonSend.put("card_id", renewTb.get("card_id"));//renewTb.getCardId());
			jsonSend.put("pay_time", renewTb.get("pay_time"));//renewTb.getPayTime());
			jsonSend.put("amount_receivable",renewTb.get("amount_receivable"));//renewTb.getAmountReceivable());
			jsonSend.put("pay_type",renewTb.get("pay_type"));//renewTb.getPayType());
			jsonSend.put("collector",renewTb.get("collector"));//renewTb.getCollector());
			jsonSend.put("buy_month",renewTb.get("buy_month"));//renewTb.getBuyMonth());
			jsonSend.put("car_number",renewTb.get("car_number"));//renewTb.getCarNumber());
			jsonSend.put("user_id",renewTb.get("user_id"));//renewTb.getUserId());
			jsonSend.put("resume",renewTb.get("resume"));//renewTb.getResume());
			jsonSend.put("start_time",renewTb.get("start_time"));//renewTb.getStartTime());
			//jsonSend.put("operate_type", operate);
			jsonSend.put("amount_pay",renewTb.get("amount_pay"));//renewTb.getAmountPay());
		} else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "month_pay_sync");
		jsonMesg.put("data", jsonSend);
		logger.error(jsonMesg);
		boolean isSend = doSendMessage(jsonMesg,parkTokenTb);
		logger.error(">>>>>>>>>>>>>>云端发送数据到停车收费系统结果：" + isSend);
		if (isSend) {
			result = "1";
		}
		return result;
	}

	/**
	 * 下发车型数据
	 * @param tableId 表数据编号
	 * @param comid 车场编号
	 * @param operate 操作
	 * @return 操作结果
	 */
	private String sendCarTypeInfo(Long tableId, Long comid, Integer operate,Map<String, Object> parkTokenTb) {
		Map<String,Object> carTypeTb = pgOnlyReadService.getMap("select * from car_type_tb where id = ? ", new Object[]{tableId});
		
		//定义下传数据的json对象
		JSONObject jsonSend = new JSONObject();
		//操作类型
		String result = "0";
		//查询出对应的需要下传的数据
		if (carTypeTb != null ) {
			jsonSend.put("car_type_id",carTypeTb.get("cartype_id"));//carTypeTb.getCartypeId());
			jsonSend.put("name", carTypeTb.get("name"));//carTypeTb.getName());
			jsonSend.put("create_time",carTypeTb.get("create_time"));//carTypeTb.getCreateTime());
			jsonSend.put("sort", carTypeTb.get("sort"));//carTypeTb.getSort());
			jsonSend.put("update_time", carTypeTb.get("update_time"));//carTypeTb.getUpdateTime());
//			jsonSend.put("operate_type", operate);
			jsonSend.put("operate_type", operate);
		} else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "car_type_sync");
		jsonMesg.put("data", jsonSend);
		logger.error(jsonMesg);
		boolean isSend = doSendMessage(jsonMesg,parkTokenTb);
		logger.error(">>>>>>>>>>>>>>云端发送数据到停车收费系统结果：" + isSend);
		if (isSend) {
			result = "1";
		}
		return result;
	}

	/**
	 * 将云后台零元结算的订单消息发送到收费系统

	 * @param tableId 表主键
	 * @param comid 车场编号
	 * @param operate 操作
	 * @return 发送结果
	 */
	private String sendOrderInfo(Long tableId, Long comid,
								 Integer operate,Map<String, Object> parkTokenTb) {
		String result = "0";
		JSONObject jsonSend = new JSONObject();
		//查询出对应的需要下传的数据
		Map mapNeed = null;/// daService.getMap("select * from "+tableName+" where id=? ", new Object[]{tableId});
		if (mapNeed != null && !mapNeed.isEmpty()) {
			//收费系统对应的订单编号
			String orderIdLocalString = String.valueOf(mapNeed.get("order_id_local"));
			//出场收费员编号
			String outUid = String.valueOf(mapNeed.get("out_uid"));
			//出场时间
			Long entTimeLong = Long.valueOf(String.valueOf(mapNeed.get("end_time")));
			//结算方式
			Integer payType = Integer.valueOf(String.valueOf(mapNeed.get("pay_type")));
			jsonSend.put("out_uid", outUid);
			jsonSend.put("end_time", entTimeLong);
			jsonSend.put("order_id", orderIdLocalString);
			jsonSend.put("pay_type", payType);
		} else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "completezero_order");
		jsonMesg.put("data", jsonSend);
		logger.error(jsonMesg);
		boolean isSend = doSendMessage(jsonMesg,parkTokenTb);
		logger.error(">>>>>>>>>>>>>>云端发送数据到停车收费系统结果：" + isSend);
		if (isSend) {
			result = "1";
		}
		return result;
	}


	/**
	 * 将云后台修改的收费员信息发送到收费系统

	  * @param tableId 表主键
	 * @param comid 车场编号
	 * @param operate 操作
	 * @return 操作结果
	 */
	private String sendUserInfo(Long tableId, Long comid,
								Integer operate,Map<String, Object> parkTokenTb) {
		String result = "0";
		JSONObject jsonSend = new JSONObject();
		//操作类型
		Map<String,Object> userInfoTb = pgOnlyReadService.getMap("select * from user_info_tb where id = ? ",
				new Object[]{tableId});
		
		if (userInfoTb != null ) {
			if (operate == 2) {
				jsonSend.put("update_time", userInfoTb.get("update_time"));//userInfoTb.getUpdateTime());
			}
			Long sex = userInfoTb.get("sex")==null?1L:(Long)userInfoTb.get("sex");
			if(sex==0||sex==1){
				jsonSend.put("sex",sex);
			}
			jsonSend.put("user_id",userInfoTb.get("user_id"));// userInfoTb.getUserId());
			jsonSend.put("name", userInfoTb.get("nickname"));//userInfoTb.getNickname());
			jsonSend.put("create_time",userInfoTb.get("reg_time"));//userInfoTb.getRegTime());
			jsonSend.put("operate_type", operate);
		} else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "collector_sync");
		jsonMesg.put("data", jsonSend);
		logger.error(jsonMesg);
		boolean isSend = doSendMessage(jsonMesg,parkTokenTb);
		logger.error(">>>>>>>>>>>>>>云端发送数据到停车收费系统结果：" + isSend);
		if (isSend) {
			result = "1";
		}
		return result;
	}

	/**
	 * 将云后台修改的月卡套餐信息发送到车场收费系统

	 * @param tableId 表主键
	 * @param comid 车场编号
	 * @param operate 操作
	 * @return 操作结果
	 */
	private String sendProductPackageInfo(Long tableId,Long comid, Integer operate,Map<String, Object> parkTokenTb) {
		String result = "0";
		//定义下传数据的json对象
		JSONObject jsonSend = new JSONObject();
		//操作类型
		Map<String,Object> packageTb = pgOnlyReadService.getMap("select * from product_package_tb where id = ? ",
				new Object[]{tableId});
		if (packageTb != null ) {
			//修改时间
			jsonSend.put("update_time", packageTb.get("update_time"));//packageTb.getUpdateTime());
			String carTypdId =(String)packageTb.get("car_type_id");// packageTb.getCarTypeId();
			if(Check.isLong(carTypdId)){
				carTypdId = getCarTypd(Long.valueOf(carTypdId));
			}
			jsonSend.put("car_type",carTypdId);
			jsonSend.put("package_id", packageTb.get("card_id"));//packageTb.getCardId());
			jsonSend.put("name", packageTb.get("p_name"));//packageTb.getpName());
			jsonSend.put("create_time", packageTb.get("create_time"));//packageTb.getCreateTime());
			jsonSend.put("describe", packageTb.get("describe"));//packageTb.getDescribe());
			jsonSend.put("price", packageTb.get("price"));//packageTb.getPrice());
			jsonSend.put("update_time",packageTb.get("update_time"));//packageTb.getUpdateTime());
			jsonSend.put("period",packageTb.get("period"));//packageTb.getPeriod());
			jsonSend.put("operate_type", operate);
		} else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "month_card_sync");
		jsonMesg.put("data", jsonSend);
		logger.error(jsonMesg);
		boolean isSend = doSendMessage(jsonMesg,parkTokenTb);
		logger.error(">>>>>>>>>>>>>>云端发送数据到停车收费系统结果：" + isSend);
		if (isSend) {
			result = "1";
		}
		return result;
	}

	/**
	 * 将云后台修改的价格息发送到车场收费系统

	 * @param tableId 表主键
	 * @param comid 车场编号
	 * @param operate 操作
	 * @return 操作结果
	 */
	private String sendPriceInfo(Long tableId, Long comid,
								 Integer operate,Map<String, Object> parkTokenTb) {
		String result = "0";
		JSONObject jsonSend = new JSONObject();
		//操作类型
		Map<String,Object> priceTb = pgOnlyReadService.getMap("select * from price_tb where id = ? ",
				new Object[]{tableId});

		if (priceTb != null ) {
			//修改时间
			jsonSend.put("update_time", priceTb.get("update_time"));//.getUpdateTime());
			jsonSend.put("price_id",priceTb.get("price_id"));//priceTb.getPriceId());
			jsonSend.put("car_type",priceTb.get("car_type_zh"));//priceTb.getCarTypeZh());
			jsonSend.put("create_time", priceTb.get("create_time"));//priceTb.getCreateTime());
			jsonSend.put("describe", priceTb.get("describe"));//priceTb.getDescribe());
			jsonSend.put("operate_type", operate);
		} else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}

		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "price_sync");
		jsonMesg.put("data", jsonSend);
		logger.error(jsonMesg);
		boolean isSend = doSendMessage(jsonMesg,parkTokenTb);
		logger.error(">>>>>>>>>>>>>>云端发送数据到停车收费系统结果：" + isSend);
		if (isSend) {
			result = "1";
		}
		return result;
	}

	/**
	 * 将云后台修改的月卡会员的信息发送到车场收费系统

	 * @param tableId 表主键
	 * @param comid 车场编号
	 * @param operate 操作
	 * @return 操作结果
	 */
	private String sendcardMember(Long tableId, Long comid,
								  Integer operate,Map<String, Object> parkTokenTb) {
		String result = "0";
		JSONObject jsonSend = new JSONObject();
		//操作类型
		Map<String,Object> product = pgOnlyReadService.getMap("select * from carower_product where id = ? ",
				new Object[]{tableId});
		
		if (product != null ) {
			logger.error(">>>>>>>查询需要同步的月卡会员信息：" + product);
			//根据文档说明下传具体的数据
			Long pid = (Long)product.get("pid");
			jsonSend.put("pid", getProudetId(pid));

			jsonSend.put("card_id", product.get("card_id"));//.getCardId());
			jsonSend.put("update_time",  product.get("update_time"));//product.getUpdateTime());
			jsonSend.put("create_time",  product.get("create_time"));//product.getCreateTime());
			jsonSend.put("begin_time",  product.get("b_time"));//product.getbTime());
			jsonSend.put("end_time",  product.get("e_time"));//product.geteTime());
			jsonSend.put("name",  product.get("name"));//product.getName());
			jsonSend.put("car_number",  product.get("car_number"));//product.getCarNumber());
			String carTypeId=getCarTypd((Long)product.get("car_type_id"));
			jsonSend.put("car_type_id", carTypeId);
			jsonSend.put("limit_day_type",  product.get(""));//product.getLimitDayType());
			jsonSend.put("price", StringUtils.formatDouble(product.get("act_total")));//.getActTotal()));
			jsonSend.put("tel",  product.get("mobile"));//product.getMobile());
			jsonSend.put("remark", product.get("remark"));// ,product.getRemark());
			jsonSend.put("p_lot",  product.get("p_lot")==null?"":product.get("p_lot"));//product.getpLot()==null?"":product.getpLot());
			jsonSend.put("amount_receivable", StringUtils.formatDouble(product.get("total")));//.getTotal()));
			jsonSend.put("operate_type", operate);
			logger.error(">>>>>>>>>>>>>>>>>>>>>>>>>>传输的数据内容为：" + jsonSend);
		} else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "month_member_sync");
		jsonMesg.put("data", jsonSend);
//		jsonSend.put("service_name", "month_member_sync");
		boolean isSend = doSendMessage(jsonMesg,parkTokenTb);
		//logger.error(">>>>>>>>>>>>>>>>>>>>同步月卡会员数据到收费系统：" + jsonSend);
		logger.error(">>>>>>>>>>>>>>云端发送数据到停车收费系统结果：" + isSend);
		if (isSend) {
			result = "1";
		}
		return result;
	}

	/**
	 * 将云后台修改的通道的信息发送到车场系统

	 * @param tableId 表主键
	 * @param comid 车场编号
	 * @param operate 操作
	 * @return 操作结果
	 */
	private String sendComPassInfo(Long tableId, Long comid,
								   Integer operate,Map<String, Object> parkTokenTb){
		String result = "0";
		Map<String,Object> comPassTb = pgOnlyReadService.getMap("select * from com_pass_tb where id = ? ",
				new Object[]{tableId});
		
		JSONObject jsonSend = new JSONObject();
		if(comPassTb!=null){
			jsonSend.put("passname",comPassTb.get("passname"));//.getPassname());
			jsonSend.put("passtype",comPassTb.get("passtype"));//comPassTb.getPasstype());
			jsonSend.put("operate_type",operate);
			jsonSend.put("channel_id",comPassTb.get("channel_id"));//comPassTb.getparkTokenTb());

		}else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}

		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "gate_sync");
		jsonMesg.put("data", jsonSend);
		boolean isSend = doSendMessage(jsonMesg,parkTokenTb);
		logger.error(">>>>>>>>>>>>>>>>>>>>同步月卡会员数据到收费系统：" + jsonSend);
		logger.error(">>>>>>>>>>>>>>云端发送数据到停车收费系统结果：" + isSend);
		if (isSend) {
			result = "1";
		}
		return result;
	}




	private String getCarTypd(Long id){
		if(Check.isEmpty(id+""))
			return id+"";
		Map<String , Object> typeTb = pgOnlyReadService.getMap("select cartype_id from car_type_tb where id =? ",new Object[]{id});
		if(typeTb!=null&&typeTb.get("cartype_id")!=null)
			return (String)typeTb.get("cartype_id");
		return id+"";
	}

	private String getProudetId(Long id){
		if(Check.isEmpty(id+""))
			return id+"";
		Map<String , Object> packageTb = pgOnlyReadService.getMap("select car_type_id from product_package_tb where id =? ",new Object[]{id});
		if(packageTb!=null&&packageTb.get("car_type_id")!=null)
			return (String)packageTb.get("car_type_id");
		return id+"";
	}


	/**
     * 获取下发数据的TCP通道
     * @param comid
     * @return
     */
    public Map<String, Object> getChannel(Long comid) {
    	Map<String, Object> tokenTb =  pgOnlyReadService.getMap("select * from park_token_tb where park_id=? order by id desc ", new Object[]{comid+""});
        if (tokenTb == null||tokenTb.isEmpty() ) {
        	return null;
        } 
        logger.error("sdk login token:" + tokenTb);
        return tokenTb;
    }
    
    private boolean doSendMessage(JSONObject message,Map<String, Object> parkTokenTb){
    	logger.error(parkTokenTb);
    	//{id=30223, park_id=21836, token=6f5967a3de4b46389443e647a2a3d4f2, login_time=1521001258, beat_time=null,
    	//server_ip=10.24.217.9, source_ip=/59.173.116.96:6529, local_id=00e18c926756_1200_01}
    	String ip = (String)parkTokenTb.get("server_ip");
    	String localId = (String)parkTokenTb.get("local_id");
    	//logger.error(ip+","+localId);
    	if(Check.isEmpty(localId))
    		localId = parkTokenTb.get("park_id")+"";
    	else{
    		localId = parkTokenTb.get("park_id")+"_"+localId;
    	}
    	logger.error(localId);
    	JSONObject jsonObject = JSONObject.parseObject("{}");
    	jsonObject.put("channelid", localId);
    	jsonObject.put("data", message);
    	String url = "http://"+ip+":8080/zld/sendmesgtopark";
    	logger.error(url);
    	logger.error(jsonObject);
    	try {
			String ret = new HttpProxy().doBodyPost(url, jsonObject.toString());
			logger.error(ret);
			if(ret!=null){
				JSONObject result = JSONObject.parseObject(ret);
				if(result.containsKey("result"))
					return result.getBooleanValue("result");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return false;
    }

}
