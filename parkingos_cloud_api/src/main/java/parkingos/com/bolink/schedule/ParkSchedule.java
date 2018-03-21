package parkingos.com.bolink.schedule;

import com.alibaba.fastjson.JSONObject;
import com.zld.common_dao.dao.CommonDao;
import com.zld.common_dao.enums.FieldOperator;
import com.zld.common_dao.qo.PageOrderConfig;
import com.zld.common_dao.qo.SearchBean;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;
import parkingos.com.bolink.beans.*;
import parkingos.com.bolink.netty.NettyChannelMap;
import parkingos.com.bolink.utlis.Check;
import parkingos.com.bolink.utlis.CommonUtils;
import parkingos.com.bolink.utlis.StringUtils;
import parkingos.com.bolink.utlis.TimeTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;


public class ParkSchedule extends TimerTask {

	private static Logger logger = Logger.getLogger(ParkSchedule.class);

	//private CommonDao commonDao;
	private CommonDao commonDao;

	private CommonUtils commonUtils;

	public ParkSchedule(CommonDao commonDao,CommonUtils commonUtils) {
		this.commonDao = commonDao;
		this.commonUtils = commonUtils;
	}

	@Override
	public void run() {
		logger.error("数据库操作对象:" + commonDao);
		logger.error("开始下行数据定时任务....");
		sendMessageToSDK();
	}

	private void sendMessageToSDK() {
		SyncInfoPoolTb syncInfoPoolTb = new SyncInfoPoolTb();
		syncInfoPoolTb.setState(0);
		//下发数据需要按时间顺序发送，不能倒序
		PageOrderConfig orderConfig = new PageOrderConfig();
		orderConfig.setOrderInfo("id","desc");
		List<SearchBean> searchBeans = new ArrayList<>();
		SearchBean searchBeans1 = new SearchBean();
		searchBeans1.setOperator(FieldOperator.GREATER_THAN);
		searchBeans1.setStartValue(TimeTools.getToDayBeginTime()-86400);
		searchBeans1.setFieldName("create_time");
		searchBeans.add(searchBeans1);
		List<SyncInfoPoolTb> dataNeedSyncList =  commonDao.selectListByConditions(syncInfoPoolTb,searchBeans,orderConfig);//.selectAllBySelective(syncInfoPoolTb);//null;// daService.getAllMap("select * from sync_info_pool_tb where state=? ", list);
		logger.info(">>>>需要同步的数据>>>>>>>"+dataNeedSyncList);
		if (dataNeedSyncList != null && dataNeedSyncList.size() > 0) {
			for (SyncInfoPoolTb infoPoolTb : dataNeedSyncList) {
				if (infoPoolTb != null ) {
					String tableName = String.valueOf(infoPoolTb.getTableName());
					Long tableId = Long.valueOf(String.valueOf(infoPoolTb.getTableId()));
					Long comid = Long.valueOf(String.valueOf(infoPoolTb.getComid()));
					Integer operate = Integer.valueOf(String.valueOf(infoPoolTb.getOperate()))+1;
					if (tableName.equals("carower_product")) {
						String result = sendcardMember( tableId, comid, operate);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送月卡会员信息结果" + result);
					} else if (tableName.equals("price_tb")) {
						//logger.info("未处理价格同步");
						String result = sendPriceInfo( tableId, comid, operate);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送价格信息结果" + result);
					} else if (tableName.equals("product_package_tb")) {
						String result = sendProductPackageInfo(tableId, comid, operate);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送月卡套餐信息结果" + result);
					} else if (tableName.equals("user_info_tb")) {
						String result = sendUserInfo(tableId, comid, operate);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送收费员信息结果" + result);
					} else if (tableName.equals("order_tb")) {
						logger.info("未处理订单同步");
//						String result = sendOrderInfo(tableId, comid, operate);
//						logger.error(">>>>>>>>>>>>>>>>>>>>>发送结算订单信息结果" + result);
					}else if (tableName.equals("car_type_tb")) {
						String result = sendCarTypeInfo( tableId, comid, operate);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送车型信息结果" + result);
					} else if (tableName.equals("zld_black_tb")) {
						String result = sendBlackUser(tableId, comid, operate);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送黑名单信息结果" + result);
					} else if (tableName.equals("card_renew_tb")) {
						String result = sendCardReNewInfo(tableId, comid, operate);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送月卡续费信息结果" + result);
					}  else if(tableName.equals("com_pass_tb")){
						String result = sendComPassInfo(tableId, comid, operate);
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
	private String sendBlackUser(Long tableId, Long comid, Integer operate) {
		ZldBlackTb black = new ZldBlackTb();
		black.setId(tableId);
		black = (ZldBlackTb)commonDao.selectObjectByConditions(black);//selectObjectBySelective(black);
		String result = "0";
		//获取下传数据的通道信息
		Channel channel = NettyChannelMap.get(commonUtils.getChannel(String.valueOf(comid)));
		if (channel == null || !channel.isActive() || !channel.isWritable()) {//通道不可用，客户端掉线
			logger.error(comid + "，客户端掉线.....");
			return null;
		}
		//定义下传数据的json对象
		JSONObject jsonSend = new JSONObject();
		//操作类型

		//查询出对应的需要下传的数据
		if (black != null ) {
			jsonSend.put("black_uuid", black.getBlackUuid());
			jsonSend.put("car_number", black.getCarNumber());
			jsonSend.put("operator", black.getOperator());
			jsonSend.put("create_time", black.getCtime());
			jsonSend.put("resume", black.getRemark());
			jsonSend.put("operate_type", operate);
		} else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "blackuser_sync");
		jsonMesg.put("data", jsonSend);
		logger.error(jsonMesg.toString());
//		jsonSend.put("service_name", "blackuser_sync");
//		logger.error(jsonSend.toString());
		boolean isSend = commonUtils.doBackMessage(jsonMesg.toString(), channel);
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
	private String sendCardReNewInfo(Long tableId, Long comid, Integer operate) {
		CardRenewTb  renewTb = new CardRenewTb();
		renewTb.setId(tableId.intValue());
		renewTb = (CardRenewTb)commonDao.selectObjectByConditions(renewTb);//.selectObjectBySelective(renewTb);
		String result = "0";
		//获取下传数据的通道信息
		Channel channel = NettyChannelMap.get(commonUtils.getChannel(String.valueOf(comid)));
		if (channel == null || !channel.isActive() || !channel.isWritable()) {//通道不可用，客户端掉线
			logger.error(comid + "，客户端掉线.....");
			return null;
		}
		//定义下传数据的json对象
		JSONObject jsonSend = new JSONObject();
		//操作类型

		//查询出对应的需要下传的数据
		if (renewTb != null ) {
			jsonSend.put("trade_no",renewTb.getTradeNo() );
			jsonSend.put("card_id", renewTb.getCardId());
			jsonSend.put("pay_time", renewTb.getPayTime());
			jsonSend.put("amount_receivable",renewTb.getAmountReceivable());
			jsonSend.put("pay_type",renewTb.getPayType());
			jsonSend.put("collector",renewTb.getCollector());
			jsonSend.put("buy_month",renewTb.getBuyMonth());
			jsonSend.put("car_number",renewTb.getCarNumber());
			jsonSend.put("user_id",renewTb.getUserId());
			jsonSend.put("resume",renewTb.getResume());
			jsonSend.put("start_time",renewTb.getStartTime());
			//jsonSend.put("operate_type", operate);
			jsonSend.put("amount_pay",renewTb.getAmountPay());
		} else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "month_pay_sync");
		jsonMesg.put("data", jsonSend);
		logger.error(jsonMesg.toString());
//		jsonSend.put("service_name", "month_pay_sync");
//		logger.error(jsonSend.toString());
		boolean isSend = commonUtils.doBackMessage(jsonMesg.toString(), channel);
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
	private String sendCarTypeInfo(Long tableId, Long comid, Integer operate) {
		CarTypeTb carTypeTb = new CarTypeTb();
		carTypeTb.setId(tableId);
		carTypeTb = (CarTypeTb)commonDao.selectObjectByConditions(carTypeTb);//selectObjectBySelective(carTypeTb);
		String result = "0";
		//获取下传数据的通道信息
		logger.info(carTypeTb);
		Channel channel = NettyChannelMap.get(commonUtils.getChannel(String.valueOf(comid)));
		if (channel == null || !channel.isActive() || !channel.isWritable()) {//通道不可用，客户端掉线
			logger.error(comid + "，客户端掉线.....");
			return null;
		}
		//定义下传数据的json对象
		JSONObject jsonSend = new JSONObject();
		//操作类型

		//查询出对应的需要下传的数据
		if (carTypeTb != null ) {
			jsonSend.put("car_type_id",carTypeTb.getCartypeId());
			jsonSend.put("name", carTypeTb.getName());
			jsonSend.put("create_time",carTypeTb.getCreateTime());
			jsonSend.put("sort", carTypeTb.getSort());
			jsonSend.put("update_time", carTypeTb.getUpdateTime());
//			jsonSend.put("operate_type", operate);
			jsonSend.put("operate_type", operate);
		} else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "car_type_sync");
		jsonMesg.put("data", jsonSend);
		logger.error(jsonMesg.toString());
//		jsonSend.put("service_name", "car_type_sync");
//		logger.error(jsonSend.toString());
		boolean isSend = commonUtils.doBackMessage(jsonMesg.toString(), channel);
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
								 Integer operate) {
		String result = "0";
		//获取下传数据的通道信息
		Channel channel = NettyChannelMap.get(commonUtils.getChannel(String.valueOf(comid)));
		if (channel == null || !channel.isActive() || !channel.isWritable()) {//通道不可用，客户端掉线
			logger.error(comid + "，客户端掉线.....");
			return null;
		}
		//定义封装下传信息的json对象
//		JSONObject jsonObj = new JSONObject();
		//定义下传数据的json对象
		JSONObject jsonSend = new JSONObject();
		//操作类型

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
		logger.error(jsonMesg.toString());
		boolean isSend = commonUtils.doBackMessage(jsonMesg.toString(), channel);
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
								Integer operate) {
		String result = "0";
		//获取下传数据的通道信息
		Channel channel = NettyChannelMap.get(commonUtils.getChannel(String.valueOf(comid)));
		if (channel == null || !channel.isActive() || !channel.isWritable()) {//通道不可用，客户端掉线
			logger.error(comid + "，客户端掉线.....");
			return null;
		}
		//定义封装下传信息的json对象
//		JSONObject jsonObj = new JSONObject();
		//定义下传数据的json对象
		JSONObject jsonSend = new JSONObject();
		//操作类型

		//查询出对应的需要下传的数据
		UserInfoTb userInfoTb=new UserInfoTb();
		userInfoTb.setId(tableId);
		userInfoTb = (UserInfoTb)commonDao.selectObjectByConditions(userInfoTb);//.selectObjectBySelective(userInfoTb);
		//Map mapNeed = null;//daService.getMap("select * from "+tableName+" where id=? ", new Object[]{tableId});
		if (userInfoTb != null ) {
			if (operate == 2) {
				jsonSend.put("update_time", userInfoTb.getUpdateTime());
			}
			//收费员性别
			//jsonSend.put("sex", userInfoTb.getSex());
			if(userInfoTb.getSex()==0||userInfoTb.getSex()==1){
				jsonSend.put("sex",userInfoTb.getSex());
			}
			jsonSend.put("user_id", userInfoTb.getUserId());
			jsonSend.put("name", userInfoTb.getNickname());
			jsonSend.put("create_time",userInfoTb.getRegTime());
			jsonSend.put("operate_type", operate);
		} else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "collector_sync");
		jsonMesg.put("data", jsonSend);
		logger.error(jsonMesg.toString());
//		jsonSend.put("service_name", "collector_sync");
//		logger.error(jsonSend.toString());
		boolean isSend = commonUtils.doBackMessage(jsonMesg.toString(), channel);
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
	private String sendProductPackageInfo(Long tableId,
										  Long comid, Integer operate) {
		String result = "0";
		//获取下传数据的通道信息
		Channel channel = NettyChannelMap.get(commonUtils.getChannel(String.valueOf(comid)));
		if (channel == null || !channel.isActive() || !channel.isWritable()) {//通道不可用，客户端掉线
			logger.error(comid + "，客户端掉线.....");
			return null;
		}
		//定义封装下传信息的json对象
		JSONObject jsonObj = new JSONObject();
		//定义下传数据的json对象
		JSONObject jsonSend = new JSONObject();
		//操作类型

		//查询出对应的需要下传的数据
		ProductPackageTb packageTb = new ProductPackageTb();
		packageTb.setId(tableId);
		packageTb = (ProductPackageTb)commonDao.selectObjectByConditions(packageTb);//.selectObjectBySelective(packageTb);
//		Map mapNeed = null;// daService.getMap("select * from "+tableName+" where id=? ", new Object[]{tableId});
		if (packageTb != null ) {
			//修改时间
			jsonSend.put("update_time", packageTb.getUpdateTime());
			String carTypdId = packageTb.getCarTypeId();
			if(Check.isLong(carTypdId)){
				carTypdId = getCarTypd(Long.valueOf(carTypdId));
			}
			jsonSend.put("car_type",carTypdId);
			jsonSend.put("package_id", packageTb.getCardId());
			jsonSend.put("name", packageTb.getpName());
			jsonSend.put("create_time", packageTb.getCreateTime());
			jsonSend.put("describe", packageTb.getDescribe());
			jsonSend.put("price", packageTb.getPrice());
			jsonSend.put("update_time",packageTb.getUpdateTime());
			jsonSend.put("period",packageTb.getPeriod());
			jsonSend.put("operate_type", operate);
		} else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "month_card_sync");
		jsonMesg.put("data", jsonSend);
		logger.error(jsonMesg.toString());
//		jsonSend.put("service_name","month_card_sync");
//		logger.error(jsonSend.toString());
		boolean isSend = commonUtils.doBackMessage(jsonMesg.toString(), channel);
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
								 Integer operate) {
		String result = "0";
		//获取下传数据的通道信息
		Channel channel = NettyChannelMap.get(commonUtils.getChannel(String.valueOf(comid)));
		if (channel == null || !channel.isActive() || !channel.isWritable()) {//通道不可用，客户端掉线
			logger.error(comid + "，客户端掉线.....");
			return null;
		}
		//定义转换查询数据库结果转为json的对象
		JSONObject jsonObj = new JSONObject();
		//定义下传数据的json对象
		JSONObject jsonSend = new JSONObject();
		//操作类型

		//查询出对应的需要下传的数据
		PriceTb priceTb = new PriceTb();
		priceTb.setId(tableId);
		priceTb = (PriceTb) commonDao.selectObjectByConditions(priceTb);

		if (priceTb != null ) {
			//修改时间
			jsonSend.put("update_time", priceTb.getUpdateTime());
			//Long carTypdId =Long.valueOf(priceTb.getCarType());
			jsonSend.put("price_id",priceTb.getPriceId());
			jsonSend.put("car_type",priceTb.getCarTypeZh());
			//jsonSend.put("car_type_zd",priceTb.getCarTypeZh());
			jsonSend.put("create_time", priceTb.getCreateTime());
			jsonSend.put("describe", priceTb.getDescribe());
			//jsonSend.put("price", priceTb.getPrice());
			jsonSend.put("update_time",priceTb.getUpdateTime());
			jsonSend.put("operate_type", operate);
			//jsonSend.put("state",priceTb.getState());
		} else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}
		//查询出对应的需要下传的数据
		/*Map mapNeed = null;// daService.getMap("select * from "+tableName+" where id=? ", new Object[]{tableId});
		if (mapNeed != null && !mapNeed.isEmpty()) {
			//价格记录id
			String priceId = String.valueOf(mapNeed.get("price_id"));
			//创建时间
			Long createTime = Long.valueOf(String.valueOf(mapNeed.get("create_time")));
			//修改时间
			Long updateTime = -1L;
			if (operate == 2) {
				updateTime = Long.valueOf(String.valueOf(mapNeed.get("update_time")));
			}
			if (updateTime > 0) {
				jsonSend.put("update_time", updateTime);
			}
			//车辆类型
			String carType = String.valueOf(mapNeed.get("car_type_zh"));
			//价格描述
			String describe = String.valueOf(mapNeed.get("describe"));
			String mapNeedStr = StringUtils.createJson(mapNeed);
			jsonObj = JSONObject.fromObject(mapNeedStr);
			//价格记录id
			String priceId = "";
			if(checkLegal(jsonObj, "price_id")){
				priceId = jsonObj.getString("price_id");
			}
			//创建时间
			Long createTime = -1L;
			if(checkLegal(jsonObj, "create_time")){
				createTime = jsonObj.getLong("create_time");
			}
			//修改时间
			Long updateTime = -1L;
			if(checkLegal(jsonObj, "update_time")){
				updateTime = jsonObj.getLong("update_time");
			}
			if(updateTime >0){
				jsonSend.put("update_time", updateTime);
			}
			//车辆类型
			String carType = "";
			if(checkLegal(jsonObj, "car_type_zh")){
				carType = jsonObj.getString("car_type_zh");
			}
			//价格描述
			String describe = "";
			if(checkLegal(jsonObj, "describe")){
				describe = jsonObj.getString("describe");
			}
			jsonSend.put("price_id", priceId);
			jsonSend.put("car_type", carType);
			jsonSend.put("create_time", createTime);
			jsonSend.put("operate_type", operate);
			jsonSend.put("describe", describe);
		} else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}
		*/
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "price_sync");
		jsonMesg.put("data", jsonSend);
		logger.error(jsonMesg.toString());
//		jsonSend.put("service_name", "price_sync");
//		logger.error(jsonSend.toString());
		boolean isSend = commonUtils.doBackMessage(jsonMesg.toString(), channel);
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
								  Integer operate) {
		String result = "0";
		//获取下传数据的通道信息
		logger.info(commonUtils);
		Channel channel = NettyChannelMap.get(commonUtils.getChannel(String.valueOf(comid)));
		if (channel == null || !channel.isActive() || !channel.isWritable()) {//通道不可用，客户端掉线
			logger.error(comid + "，客户端掉线.....");
			return null;
		}
		//定义转换查询数据库结果转为json的对象
		//JSONObject jsonObj = new JSONObject();
		//定义下传数据的json对象
		JSONObject jsonSend = new JSONObject();
		//操作类型

		//查询出对应的需要下传的数据
		CarowerProduct product = new CarowerProduct();
		product.setComId(comid);
		product.setId(tableId);
		Map mapNeed =  null;// daService.getMap("select * from "+tableName+" where id=? and com_id=?", new Object[]{tableId,comid});
		product = (CarowerProduct)commonDao.selectObjectByConditions(product);//.selectObjectBySelective(product);
		if (product != null ) {
			logger.error(">>>>>>>查询需要同步的月卡会员信息：" + product.toString());
			//根据文档说明下传具体的数据
			Long pid = product.getPid();
			jsonSend.put("pid", getProudetId(pid));

			jsonSend.put("card_id", product.getCardId());
			jsonSend.put("update_time", product.getUpdateTime());
			jsonSend.put("create_time", product.getCreateTime());
			jsonSend.put("begin_time", product.getbTime());
			jsonSend.put("end_time", product.geteTime());
			jsonSend.put("name", product.getName());
			jsonSend.put("car_number", product.getCarNumber());
			String carTypeId=getCarTypd(product.getCarTypeId());
			jsonSend.put("car_type_id", carTypeId);
			jsonSend.put("limit_day_type", product.getLimitDayType());
			jsonSend.put("price", StringUtils.formatDouble(product.getActTotal()));
			jsonSend.put("tel", product.getMobile());
			jsonSend.put("remark",product.getRemark());
			jsonSend.put("p_lot", product.getpLot()==null?"":product.getpLot());
			jsonSend.put("amount_receivable", StringUtils.formatDouble(product.getTotal()));
			jsonSend.put("operate_type", operate);
			logger.error(">>>>>>>>>>>>>>>>>>>>>>>>>>传输的数据内容为：" + jsonSend.toString());
		} else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "month_member_sync");
		jsonMesg.put("data", jsonSend);
//		jsonSend.put("service_name", "month_member_sync");
		boolean isSend = commonUtils.doBackMessage(jsonMesg.toString(), channel);
		logger.error(">>>>>>>>>>>>>>>>>>>>同步月卡会员数据到收费系统：" + jsonSend.toString());
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
								   Integer operate){
		String result = "0";
		//获取下传数据的通道信息
		logger.info(commonUtils);
		Channel channel = NettyChannelMap.get(commonUtils.getChannel(String.valueOf(comid)));
		if (channel == null || !channel.isActive() || !channel.isWritable()) {//通道不可用，客户端掉线
			logger.error(comid + "，客户端掉线.....");
			return null;
		}

		//定义下传数据的json对象
		JSONObject jsonSend = new JSONObject();
		//操作类型

		//查询出对应的需要下传的数据
		ComPassTb comPassTb = new ComPassTb();
		comPassTb.setId(tableId);
		comPassTb.setComid(comid);
		comPassTb = (ComPassTb)commonDao.selectObjectByConditions(comPassTb);
		if(comPassTb!=null){
			jsonSend.put("passname",comPassTb.getPassname());
			jsonSend.put("passtype",comPassTb.getPasstype());
			jsonSend.put("operate_type",operate);
			jsonSend.put("channel_id",comPassTb.getChannelId());

		}else {
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}

//		jsonSend.put("service_name", "gate_sync");
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "gate_sync");
		jsonMesg.put("data", jsonSend);
		boolean isSend = commonUtils.doBackMessage(jsonMesg.toString(), channel);
		logger.error(">>>>>>>>>>>>>>>>>>>>同步月卡会员数据到收费系统：" + jsonSend.toString());
		logger.error(">>>>>>>>>>>>>>云端发送数据到停车收费系统结果：" + isSend);
		if (isSend) {
			result = "1";
		}
		return result;
	}



	/**
	 * 校验数据是否合法
	 *
	 * @param json 数据
	 * @param key tcp通道
	 * @return 是否合法
	 */
	private boolean checkLegal(JSONObject json, String key) {
		boolean isLegal = false;
		if (json.containsKey(key) && !"".equals(json.getString(key)) && !"null".equals(json.getString(key))) {
			isLegal = true;
		}
		return isLegal;
	}

	private String getCarTypd(Long id){
		if(Check.isEmpty(id+""))
			return id+"";
		CarTypeTb typeTb = new CarTypeTb();
		typeTb.setId(id);
		typeTb = (CarTypeTb)commonDao.selectObjectByConditions(typeTb);
		if(typeTb!=null&&typeTb.getCartypeId()!=null)
			return typeTb.getCartypeId();
		return id+"";
	}

	private String getProudetId(Long id){
		if(Check.isEmpty(id+""))
			return id+"";
		ProductPackageTb packageTb = new ProductPackageTb();
		packageTb.setId(id);
		packageTb = (ProductPackageTb)commonDao.selectObjectByConditions(packageTb);
		if(packageTb!=null&&packageTb.getCardId()!=null)
			return packageTb.getCardId();
		return id+"";
	}



}
