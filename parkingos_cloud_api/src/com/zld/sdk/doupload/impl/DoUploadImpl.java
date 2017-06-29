package com.zld.sdk.doupload.impl;


import java.net.Inet4Address;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import sun.java2d.opengl.OGLContext;

import com.mongodb.util.Hash;
import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MemcacheUtils;
import com.zld.impl.PublicMethods;
import com.zld.sdk.doupload.DoUpload;
import com.zld.service.DataBaseService;
import com.zld.utils.Check;
import com.zld.utils.StringUtils;
/**
 * 对应sdk接口声明的实现方法类，其中包括订单上传和同步，
 * 月卡信息同步到本地，减免券的上传和同步，
 * @author liuqb
 * @date  2017-3-31
 */
@SuppressWarnings("unchecked")
@Repository
@Service
public class DoUploadImpl implements DoUpload {

	@Autowired
	private DataBaseService daService;
	@Autowired
	private CommonMethods methods;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MemcacheUtils memcacheUtils;
	//上传车场本地数据到云端，包括车辆类型绑定上传，订单上传，收费员上下班工作记录上传，等
	private Logger logger = Logger.getLogger(DoUploadImpl.class);
	
	@Override
	public String tokenCheck(String token) {
		logger.error("tokenCheck ---------Start");
		//定义返回值
		JSONObject json = new JSONObject();
		String state = "0";
		Long comid = -1L;
		String ukey = ""; 
		Map<String, Object> userInfoMap = daService.getMap(
				"select * from park_token_tb where token=? ",
				new Object[] { token });
		if (userInfoMap != null && !userInfoMap.isEmpty()) {
			comid =Long.valueOf(userInfoMap.get("park_id")+"");
			Map<String, Object> comInfoMap = daService.getMap(
					"select * from com_info_tb where id=?",
					new Object[] { comid });
			if (comInfoMap != null && !comInfoMap.isEmpty()) {
				ukey = String.valueOf(comInfoMap.get("ukey"));
				state = "1";
			}else{
				logger.error("车场编号异常，未成功在云平台注册");
			}
		}else{
			logger.error("车场初始化失败，token值异常");
		}
		json.put("state", state);
		json.put("comid", comid);
		json.put("ukey", ukey);
		logger.error("tokenCheck ---------close");
		return json.toString();
	}
	
	@Override
	public String checkSign(String preSign,String ukey,String data) {
		logger.error("checkSign -------start");
		String logStr = "checkSign start";
		//定义返回值
		String result = "0";
		String strKey = data+ "key="+ ukey;
		String sign = null;
		try {
			sign = StringUtils.MD5(strKey,"utf-8").toUpperCase();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("md5加密出现异常,请联系后台管理员！！！");
			result = "error:md5加密出现异常,请联系后台管理员！！！";
		}
		System.err.println(strKey + "," + sign + ":" + preSign
				+ ",ret:" + sign.equals(preSign));
		if (sign.equals(preSign)) {
			result="1";
		}else {
			logger.error(logStr + "error:上传数据格式校验错误");
			result = "error:识别sign标记异常";
		}
		return result;
	}
	
	@Override
	public String doUploadCarType(String preSign, String token, JSONObject arg0) {
		String logStr = "tcp doUploadCarType to cloud";
		// 定义返回值对象
		JSONArray jsonAry = new JSONArray();
		Map<String, Object> userInfoMap = daService.getMap(
				"select * from user_session_tb where token=? ",
				new Object[] { token });
		if (userInfoMap != null && !userInfoMap.isEmpty()) {
			Long comid = Long.valueOf((String) userInfoMap.get("comid"));
			Map<String, Object> comInfoMap = daService.getMap(
					"select * from com_info_tb where comid=?",
					new Object[] { comid });
			if (comInfoMap != null && !comInfoMap.isEmpty()) {
				String strKey = arg0.toString() + "key="
						+ comInfoMap.get("ukey");
				String sign = null;
				try {
					sign = StringUtils.MD5(strKey).toUpperCase();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("md5加密出现异常,请联系后台管理员！！！");
				}
				System.err.println(strKey + "," + sign + ":" + preSign
						+ ",ret:" + sign.equals(preSign));
				if (sign.equals(preSign)) {
					logger.error("上传车辆类型方法调用--------start");
					String carType = arg0.getString("carTypeInfo");
					logger.error("doUploadCarType:" + carType);
					JSONArray carTypes = JSONArray.fromObject(carType);
					String ret = "";
					for (int i = 0; i < carTypes.size(); i++) {
						JSONObject jo = carTypes.getJSONObject(i);
						if (jo.get("line_id") == null
								|| "".equals(jo.get("line_id") + "")
								|| "null".equals(jo.get("line_id") + "")) {
							ret = sqlAndValue(jo, "car_number_type_tb");
						} else {
							daService.update(
									"update car_number_type_tb set typeid = ?,update_time=? "
											+ "where id = ? and update_time<?",
									new Object[] { jo.getLong("typeid"),
											jo.getLong("update_time"),
											jo.getLong("line_id"),
											jo.getLong("update_time") });
							JSONObject jsonRet = new JSONObject();
							jsonRet.put("resultVal", "1");
							jsonRet.put("cloudId", jo.getString("line_id"));
							jsonRet.put("localId", jo.getString("id"));
							jsonRet.put("tableName", "car_number_type_tb");
							ret = jsonRet.toString();
						}
						jsonAry.add(ret);
					}
				} else {
					logger.error(logStr + "error:上传数据格式校验错误");
					return "error:sign错误";
				}
			} else {
				logger.error(logStr + "error:comid异常，未找到对应的车场信息");
				return "error:车场信息异常";
			}
		} else {
			logger.error(logStr + "error:token值异常，未找到对应的车场id");
			return "error:token值异常";
		}
		return jsonAry.toString();
	}

	
	
	@Override
	public String uploadTicket(String comid, String data) {
		String logStr = "tcp uploadTicket to cloud";
		// 定义返回值对象
		JSONObject jsonResult = new JSONObject();
		int update=0;
		JSONObject jsonObj = JSONObject.fromObject(data);
		String ticketIdRet="";
		//判断是否存在必传字段
		if(jsonObj.containsKey("ticket_id") && jsonObj.containsKey("create_time") 
				&& jsonObj.containsKey("limit_day") && jsonObj.containsKey("car_number") 
				&& jsonObj.containsKey("order_id")){
			//开始进行逻辑操作
			Long parkId = Long.valueOf(comid);
			String ticketId = jsonObj.getString("ticket_id");
			ticketIdRet = ticketId;
			Long createTime = jsonObj.getLong("create_time");
			Long limitDay = jsonObj.getLong("limit_day");
			String carNumber = jsonObj.getString("car_number");
			Long orderId = jsonObj.getLong("order_id");
			Map<String,Object> ticketRecord = daService.getMap("select * from ticket_tb " +
					"where comid=? and ticket_id=?", new Object[]{parkId,ticketId});
			//判断该减免券是否已经存在
			if(ticketRecord!=null && !ticketRecord.isEmpty()){
				//已经存在减免券记录做更新操作
				StringBuffer updatesql = new StringBuffer();
				List valuesql = new ArrayList();
				updatesql.append("update ticket_tb set create_time=?,limit_day=?,car_number=?,orderid=?");
				valuesql.add(createTime);
				valuesql.add(limitDay);
				valuesql.add(carNumber);
				valuesql.add(orderId);
				if(jsonObj.containsKey("operate_user")){
					String operateUser = jsonObj.getString("operate_user");
					updatesql.append(",operate_user=?");
					valuesql.add(operateUser);
				}
				if(jsonObj.containsKey("money")){
					Double money = jsonObj.getDouble("money");
					updatesql.append(",money=?");
					valuesql.add(money);
				}
				if(jsonObj.containsKey("state")){
					String state = jsonObj.getString("state");
					updatesql.append(",state_zh=?");
					valuesql.add(state);
				}
				if(jsonObj.containsKey("use_time")){
					Long utime = jsonObj.getLong("use_time");
					updatesql.append(",utime=?");
					valuesql.add(utime);
				}
				if(jsonObj.containsKey("type")){
					String type = jsonObj.getString("type");
					updatesql.append(",type_zh=?");
					valuesql.add(type);
				}
				if(jsonObj.containsKey("pay_money")){
					Double umoney = jsonObj.getDouble("pay_money");
					updatesql.append(",umoney=?");
					valuesql.add(umoney);
				}
				if(jsonObj.containsKey("remark")){
					String remark = jsonObj.getString("remark");
					updatesql.append(",remark=?");
					valuesql.add(remark);
				}
				updatesql.append(" where comid=? and ticket_id=?");
				valuesql.add(parkId);
				valuesql.add(ticketId);
				update = daService.update(updatesql.toString(), valuesql);
			}else{
				//进行添加操作
				Long id = daService.getLong(
								"SELECT nextval('seq_ticket_tb'::REGCLASS) AS newid",
								null);
				StringBuffer updatesql = new StringBuffer();
				StringBuffer valuesql = new StringBuffer();
				List listsql = new ArrayList();
				updatesql.append("insert into ticket_tb (id,comid,ticket_id,create_time,limit_day,orderid,car_number");
				valuesql.append("?,?,?,?,?,?,?");
				listsql.add(id);
				listsql.add(parkId);
				listsql.add(ticketId);
				listsql.add(createTime);
				listsql.add(limitDay);
				listsql.add(orderId);
				listsql.add(carNumber);
				if(jsonObj.containsKey("operate_user")){
					String operateUser = jsonObj.getString("operate_user");
					updatesql.append(",operate_user");
					valuesql.append(",?");
					listsql.add(operateUser);
				}
				if(jsonObj.containsKey("money")){
					Double money = jsonObj.getDouble("money");
					updatesql.append(",money");
					valuesql.append(",?");
					listsql.add(money);
				}
				if(jsonObj.containsKey("state")){
					String state = jsonObj.getString("state");
					updatesql.append(",state_zh");
					valuesql.append(",?");
					listsql.add(state);
				}
				if(jsonObj.containsKey("use_time")){
					Long utime = jsonObj.getLong("use_time");
					updatesql.append(",utime");
					valuesql.append(",?");
					listsql.add(utime);
				}
				if(jsonObj.containsKey("type")){
					String type = jsonObj.getString("type");
					updatesql.append(",type_zh");
					valuesql.append(",?");
					listsql.add(type);
				}
				if(jsonObj.containsKey("pay_money")){
					Double umoney = jsonObj.getDouble("pay_money");
					updatesql.append(",umoney");
					valuesql.append(",?");
					listsql.add(umoney);
				}
				if(jsonObj.containsKey("remark")){
					String remark = jsonObj.getString("remark");
					updatesql.append(",remark");
					valuesql.append(",?");
					listsql.add(remark);
				}
				updatesql.append(" )values("+valuesql+")");
				update = daService.update(updatesql.toString(), listsql);
			}
//			JSONObject jsonFirst = new JSONObject();
//			jsonFirst.put("state", update);
//			jsonFirst.put("ticket_id", ticketId);
//			jsonFirst.put("service_name", "upload_ticket");
//			jsonResult.put("result", jsonFirst.toString());
			jsonResult.put("state", update);
			jsonResult.put("ticket_id", ticketId);
			jsonResult.put("service_name", "upload_ticket");
			jsonResult.put("errmsg", "");
		}else{
//			logger.error(logStr + "error:data异常，未找到必须字段（ticket_id,order_id,car_number,limit_day）值。");
//			JSONObject jsonError = new JSONObject();
//			jsonError.put("result", "{\"state\":0,\"service_name\":\"upload_ticket\",\"ticket_id\":\""+ticketIdRet+"\",\"error\":\"上传数据中没找到必须的字段（ticket_id,order_id,car_number,limit_day）值!\"}");
//			return jsonError.toString();
			logger.error(logStr + "error:data异常，未找到必须字段（ticket_id,order_id,car_number,limit_day）值。");
			String jsonError = "{\"state\":0,\"service_name\":\"upload_ticket\",\"ticket_id\":\""+ticketIdRet+"\",\"errmsg\":\"上传数据中没找到必须的字段（ticket_id,order_id,car_number,limit_day）值!\"}";
			return jsonError;
		}
		return jsonResult.toString();
	}

	@Override
	public String doUploadBrakeState(String preSign, String token,
			JSONObject arg0) {
		String logStr = "tcp doUploadBrakeState to cloud";
		// 定义返回值对象
		JSONObject jsonResult = new JSONObject();
		Map<String, Object> userInfoMap = daService.getMap(
				"select * from user_session_tb where token=? ",
				new Object[] { token });
		if (userInfoMap != null && !userInfoMap.isEmpty()) {
			Long comid = Long.valueOf((String) userInfoMap.get("comid"));
			Map<String, Object> comInfoMap = daService.getMap(
					"select * from com_info_tb where comid=?",
					new Object[] { comid });
			if (comInfoMap != null && !comInfoMap.isEmpty()) {
				String strKey = arg0.toString() + "key="
						+ comInfoMap.get("ukey");
				String sign = null;
				try {
					sign = StringUtils.MD5(strKey).toUpperCase();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("md5加密出现异常,请联系后台管理员！！！");
				}
				System.err.println(strKey + "," + sign + ":" + preSign
						+ ",ret:" + sign.equals(preSign));
				if (sign.equals(preSign)) {
					logger.error("上传岗亭工作状态方法开始调用执行------start");
					// 获取对应的车场编号
					Long passid = arg0.getLong("passid");
					Long state = arg0.getLong("state");
					Long uploadTime = arg0.getLong("upload_time");
					int res = 0;
					if (state != -1 && passid != -1 && uploadTime != -1) {
						res = daService
								.update("update com_brake_tb set state=?,upload_time=? where passid=? ",
										new Object[] { state, uploadTime,
												passid });
						if (res == 0) {
							res = daService
									.update("insert into com_brake_tb(passid,state,upload_time) values (?,?,?) ",
											new Object[] { passid, state,
													uploadTime });
						}
					}
					jsonResult.put("resultVal", res);
					jsonResult.put("passid", passid);
					jsonResult.put("uploadTime", uploadTime);
					jsonResult.put("tableName", "com_brake_tb");
				} else {
					logger.error(logStr + "error:上传数据格式校验错误");
					return "error:sign错误";
				}
			} else {
				logger.error(logStr + "error:comid值异常，未找到对应的车场信息。");
				return "error:车场信息错误";
			}
		} else {
			logger.error(logStr + "error:token值异常，未找到对应的车场信息。");
			return "error:token值错误";
		}
		return jsonResult.toString();
	}

	@Override
	public String uploadLiftrod(String comid, String data) {
		String logStr = "tcp uploadLiftrod to cloud";
		// 定义返回值对象
		JSONObject jsonResult = new JSONObject();
		int update=0;
		JSONObject jsonObj = JSONObject.fromObject(data);
		//判断是否存在必传字段
		if(jsonObj.containsKey("liftrod_id")){
			//开始进行逻辑操作
			
				Long parkId = Long.valueOf(comid);
				String liftrodId = jsonObj.getString("liftrod_id");
				Map<String,Object> liftRod = daService.getMap("select * from lift_rod_tb " +
						"where comid=? and liftrod_id=?", new Object[]{parkId,liftrodId});
				//判断该抬杆记录是否已经存在
				if(liftRod != null && !liftRod.isEmpty()){
					//已经存在减免券记录做更新操作
					StringBuffer updatesql = new StringBuffer();
					List valuesql = new ArrayList();
					updatesql.append("update lift_rod_tb set liftrod_id=?");
					valuesql.add(liftrodId);
					if(jsonObj.containsKey("create_time")){
						Long ctime = jsonObj.getLong("create_time");
						updatesql.append(",ctime=?");
						valuesql.add(ctime);
					}
					if(jsonObj.containsKey("update_time")){
						Long updateTime = jsonObj.getLong("update_time");
						updatesql.append(",update_time=?");
						valuesql.add(updateTime);
					}
					if(jsonObj.containsKey("reason")){
						int reason = jsonObj.getInt("reason");
						updatesql.append(",reason=?");
						valuesql.add(reason);
					}
					if(jsonObj.containsKey("channel_id")){
						String outChannelId= jsonObj.getString("channel_id");
						updatesql.append(",pass_id=?");
						valuesql.add(outChannelId);
					}
					if(jsonObj.containsKey("car_number")){
						String carNumber = jsonObj.getString("car_number");
						updatesql.append(",car_number=?");
						valuesql.add(carNumber);
					}
					if(jsonObj.containsKey("order_id")){
						String orderId = jsonObj.getString("order_id");
						updatesql.append(",order_id=?");
						valuesql.add(orderId);
					}
					if(jsonObj.containsKey("resume")){
						String resume = jsonObj.getString("resume");
						updatesql.append(",resume=?");
						valuesql.add(resume);
					}
					if(jsonObj.containsKey("user_id")){
						Long uid  = getUserId(jsonObj.getString("user_id"), parkId);
						updatesql.append(",uin=?");
						valuesql.add(uid);
					}
					updatesql.append(" where comid=? and liftrod_id=?");
					valuesql.add(parkId);
					valuesql.add(liftrodId);
					update = daService.update(updatesql.toString(), valuesql);
				}else{
					//进行添加操作
					Long id = daService.getLong(
									"SELECT nextval('seq_lift_rod_tb'::REGCLASS) AS newid",
									null);
					StringBuffer updatesql = new StringBuffer();
					StringBuffer valuesql = new StringBuffer();
					List listsql = new ArrayList();
					updatesql.append("insert into lift_rod_tb (id,comid,liftrod_id,uin");
					valuesql.append("?,?,?,?");
					listsql.add(id);
					listsql.add(parkId);
					listsql.add(liftrodId);
					if(jsonObj.containsKey("user_id")){
						Long uid  = getUserId(jsonObj.getString("user_id"), parkId);
						listsql.add(uid);
					}else {
						listsql.add(-1L);
					}
					if(jsonObj.containsKey("create_time")){
						Long ctime = jsonObj.getLong("create_time");
						updatesql.append(",ctime");
						valuesql.append(",?");
						listsql.add(ctime);
					}
					if(jsonObj.containsKey("update_time")){
						Long updateTime = jsonObj.getLong("update_time");
						updatesql.append(",update_time");
						valuesql.append(",?");
						listsql.add(updateTime);
					}
					if(jsonObj.containsKey("reason")){
						int reason = jsonObj.getInt("reason");
						updatesql.append(",reason");
						valuesql.append(",?");
						listsql.add(reason);
					}
					if(jsonObj.containsKey("channel_id")){
						String outChannelId = jsonObj.getString("channel_id");
						updatesql.append(",pass_id");
						valuesql.append(",?");
						listsql.add(outChannelId);
					}
					if(jsonObj.containsKey("name")){
						String name = jsonObj.getString("name");
						updatesql.append(",name");
						valuesql.append(",?");
						listsql.add(name);
					}
					if(jsonObj.containsKey("car_number")){
						String carNumber = jsonObj.getString("car_number");
						updatesql.append(",car_number");
						valuesql.append(",?");
						listsql.add(carNumber);
					}
					if(jsonObj.containsKey("order_id")){
						String orderId = jsonObj.getString("order_id");
						updatesql.append(",order_id");
						valuesql.append(",?");
						listsql.add(orderId);
					}
					if(jsonObj.containsKey("resume")){
						String resume = jsonObj.getString("resume");
						updatesql.append(",resume");
						valuesql.append(",?");
						listsql.add(resume);
					}
					updatesql.append(" )values("+valuesql+")");
					update = daService.update(updatesql.toString(), listsql);
				}
//				JSONObject jsonFirst = new JSONObject();
//				jsonFirst.put("state", update);
//				jsonFirst.put("liftrod_id", liftrodId);
//				jsonFirst.put("service_name", "upload_liftrod");
//				jsonFirst.put("errmsg", "");
//				jsonResult.put("result", jsonFirst.toString());
				jsonResult.put("state", update);
				jsonResult.put("liftrod_id", liftrodId);
				jsonResult.put("service_name", "upload_liftrod");
				jsonResult.put("errmsg", "");
				
		}else{
//			logger.error(logStr + "error:data异常，未找到必须字段（liftrod_id）值。");
//			JSONObject jsonError = new JSONObject();
//			jsonError.put("result", "{\"state\":0,\"service_name\":\"upload_liftrod\",\"errmsg\":\"上传数据中没找到必须的字段（liftrod_id）值!\"}");
//			return jsonError.toString();
			logger.error(logStr + "error:data异常，未找到必须字段（liftrod_id）值。");
			String jsonError = "{\"state\":0,\"service_name\":\"upload_liftrod\",\"errmsg\":\"上传数据中没找到必须的字段（liftrod_id）值!\"}";
			return jsonError;
		}
		return jsonResult.toString();
	}

	@Override
	public String doUploadSeverState(String preSign, String token,
			JSONObject arg0) {
		String logStr = "tcp doUploadSeverState to cloud";
		// 定义返回值对象
		JSONObject jsonResult = new JSONObject();
		Map<String, Object> userInfoMap = daService.getMap(
				"select * from user_session_tb where token=? ",
				new Object[] { token });
		if (userInfoMap != null && !userInfoMap.isEmpty()) {
			Long comid = Long.valueOf((String) userInfoMap.get("comid"));
			Map<String, Object> comInfoMap = daService.getMap(
					"select * from com_info_tb where comid=?",
					new Object[] { comid });
			if (comInfoMap != null && !comInfoMap.isEmpty()) {
				String strKey = arg0.toString() + "key="
						+ comInfoMap.get("ukey");
				String sign = null;
				try {
					sign = StringUtils.MD5(strKey).toUpperCase();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("md5加密出现异常,请联系后台管理员！！！");
				}
				System.err.println(strKey + "," + sign + ":" + preSign
						+ ",ret:" + sign.equals(preSign));
				if (sign.equals(preSign)) {
					logger.error("停车场工作站状态上传接口 -----------start");
					Long id = arg0.getLong("worksite_id");
					if (id == null) {
						id = -1L;
					}
					String equipmentmodel = arg0.getString("equipmentmodel");
					String memoryspace = arg0.getString("memoryspace");
					String internalspace = arg0.getString("internalspace");
					Long upload_time = arg0.getLong("upload_time");
					if (upload_time == null) {
						upload_time = -1L;
					}
					int r = daService
							.update("update com_worksite_tb set host_name=?,host_memory=?,host_internal=?,upload_time=? where id = ? ",
									new Object[] { equipmentmodel, memoryspace,
											internalspace, upload_time, id });
					jsonResult.put("resultVal", r);
					jsonResult.put("worksite_id", id);
					jsonResult.put("upload_time", upload_time);
					jsonResult.put("tableName", "com_worksite_tb");
					logger.error("upload info worksite_id:" + id
							+ ",equipmentmodel:" + equipmentmodel
							+ ",memoryspace:" + memoryspace + ",internalspace:"
							+ internalspace + ",r:" + r);
				} else {
					logger.error(logStr + "error:上传数据格式校验错误");
					return "error:sign错误";
				}
			} else {
				logger.error(logStr + "error:comid值异常，未找到对应的车场信息。");
				return "error:车场信息错误";
			}
		} else {
			logger.error(logStr + "error:token值异常，未找到对应的车场信息。");
			return "error:token值错误";
		}
		return jsonResult.toString();
	}

	@Override
	public String uploadWorkRecord(String comid,String data) {
		String logStr = "tcp uploadWorkRecord to cloud";
		// 定义返回值对象
		JSONObject jsonResult = new JSONObject();
		int update=0;
		JSONObject jsonObj = JSONObject.fromObject(data);
		String uuidRet="";
		//判断是否存在必传字段
		if(jsonObj.containsKey("uuid") && jsonObj.containsKey("user_id") && jsonObj.containsKey("state")){
			//开始进行逻辑操作
			Long parkId = Long.valueOf(comid);
			String uuid = jsonObj.getString("uuid");
			String user_id = jsonObj.getString("user_id");
			uuidRet = uuid;
			Long userId = -1L;
			if(user_id!=null){
				Map<String, Object> userMap = daService.getMap("select id from user_info_tb " +
						"where user_id=? or strid=? and comid=? ", new Object[]{user_id,user_id,parkId});
				if(userMap!=null&&!userMap.isEmpty()){
					userId = (Long)userMap.get("id");
				}
			}
			Integer state = jsonObj.getInt("state");
			Map<String,Object> userWorkRecord = daService.getMap("select * from parkuser_work_record_tb " +
					"where park_id=? and uuid=?", new Object[]{parkId,uuid});
			//判断员工是否已经上班
			if(userWorkRecord!=null && !userWorkRecord.isEmpty()){
				//已经存在一条上班记录做更新操作
				StringBuffer updatesql = new StringBuffer();
				List valuesql = new ArrayList();
				updatesql.append("update parkuser_work_record_tb set uuid=?,uid=?,state=?");
				valuesql.add(uuid);
				valuesql.add(userId);
				valuesql.add(state);
				if(jsonObj.containsKey("end_time")){
					Long endTime = jsonObj.getLong("end_time");
					updatesql.append(",end_time=?");
					valuesql.add(endTime);
				}
				if(jsonObj.containsKey("start_time")){
					Long startTime = jsonObj.getLong("start_time");
					updatesql.append(",start_time=?");
					valuesql.add(startTime);
				}
				if(jsonObj.containsKey("worksite_id")){
					Long workSiteId = jsonObj.getLong("worksite_id");
					updatesql.append(",worksite_id=?");
					valuesql.add(workSiteId);
				}
				updatesql.append(" where park_id=? and uuid=?");
				valuesql.add(parkId);
				valuesql.add(uuid);
				update = daService.update(updatesql.toString(), valuesql);
			}else{
				List<Map<String,Object>> workRecords = daService.getAll("select * from parkuser_work_record_tb " +
						"where park_id=? and uid=? and end_time is null", new Object[]{parkId,userId});
				if(workRecords!=null && !workRecords.isEmpty()){
					//前面未下班的记录，下班时间置成传该次过来的上班时间
					for (int i = 0; i < workRecords.size(); i++) {
						StringBuffer updatesql = new StringBuffer();
						Map<String,Object> workRecordMap = workRecords.get(i);
						List valuesql = new ArrayList();
						updatesql.append("update parkuser_work_record_tb set uid=?,state=?");
						valuesql.add(userId);
						valuesql.add(1);//下班
						if(jsonObj.containsKey("start_time")){
							Long startTime = jsonObj.getLong("start_time");
							updatesql.append(",end_time=?");
							valuesql.add(startTime);
						}
						updatesql.append(" where id=? ");
						valuesql.add(workRecordMap.get("id"));
						update = daService.update(updatesql.toString(), valuesql);
					}
				}
				//进行添加操作
				Long id = daService.getLong(
								"SELECT nextval('seq_parkuser_work_record_tb'::REGCLASS) AS newid",
								null);
				StringBuffer updatesql = new StringBuffer();
				StringBuffer valuesql = new StringBuffer();
				List listsql = new ArrayList();
				updatesql.append("insert into parkuser_work_record_tb (id,uuid,uid,state,park_id");
				valuesql.append("?,?,?,?,?");
				listsql.add(id);
				listsql.add(uuid);
				listsql.add(userId);
				listsql.add(state);
				listsql.add(parkId);
				if(jsonObj.containsKey("end_time")){
					Long endTime = jsonObj.getLong("end_time");
					updatesql.append(",end_time");
					valuesql.append(",?");
					listsql.add(endTime);
				}
				if(jsonObj.containsKey("start_time")){
					Long startTime = jsonObj.getLong("start_time");
					updatesql.append(",start_time");
					valuesql.append(",?");
					listsql.add(startTime);
				}
				if(jsonObj.containsKey("worksite_id")){
					Long workSiteId = jsonObj.getLong("worksite_id");
					updatesql.append(",worksite_id");
					valuesql.append(",?");
					listsql.add(workSiteId);
				}
				updatesql.append(" )values("+valuesql+")");
				update = daService.update(updatesql.toString(), listsql);
			}	
//			JSONObject jsonFirst = new JSONObject();
//			jsonFirst.put("state", update);
//			jsonFirst.put("uuid", uuid);
//			jsonFirst.put("service_name", "work_record");
//			jsonResult.put("result", jsonFirst.toString());
			jsonResult.put("state", update);
			jsonResult.put("uuid", uuid);
			jsonResult.put("service_name", "work_record");
			jsonResult.put("errmsg", "");
		}else{
//			JSONObject jsonError = new JSONObject();
//			logger.error(logStr + "error:data异常，未找到必须字段（uuid,uid,state）值。");
//			jsonError.put("result", "{\"state\":0,\"service_name\":\"work_record\",\"uuid\":\""+uuidRet+"\",\"error\":\"上传数据中没找到必须的字段（uuid,uid,state）值!\"}");
			logger.error(logStr + "error:data异常，未找到必须字段（uuid,user_id,state）值。");
			String jsonError = "{\"state\":0,\"service_name\":\"work_record\",\"uuid\":\""+uuidRet+"\",\"errmsg\":\"上传数据中没找到必须的字段（uuid,user_id,state）值!\"}";
			return jsonError;
		}
		return jsonResult.toString();
	}
	
	@Override
	public String uploadLog(String comid, String data) {
		String logStr = "tcp uploadLog to cloud";
		// 定义返回值对象
		JSONObject jsonResult = new JSONObject();
		int update=0;
		JSONObject jsonObj = JSONObject.fromObject(data);
		String logIdRet = "";
		//判断是否存在必传字段
		if(jsonObj.containsKey("log_id") && jsonObj.containsKey("operate_time") && 
				jsonObj.containsKey("content")){
			//开始进行逻辑操作
			Long parkId = Long.valueOf(comid);
			String logId = jsonObj.getString("log_id");
			logIdRet = logId;
			String content = jsonObj.getString("content");
			Long operateTime = jsonObj.getLong("operate_time");
			Map<String,Object> logRecord = daService.getMap("select * from park_log_tb " +
					"where park_id=? and log_id=?", new Object[]{parkId,logId});
			//判断日志是否已经上传
			if(logRecord != null && !logRecord.isEmpty()){
				//已经存在该条日志记录做更新操作
				StringBuffer updatesql = new StringBuffer();
				List valuesql = new ArrayList();
				updatesql.append("update park_log_tb set content=?,operate_time=?");
				valuesql.add(content);
				valuesql.add(operateTime);
				
				if(jsonObj.containsKey("type")){
					String type = jsonObj.getString("type");
					updatesql.append(",type=?");
					valuesql.add(type);
				}
				if(jsonObj.containsKey("user_id")){
					String user_id = jsonObj.getString("user_id");
					updatesql.append(",operate_user=?");
					valuesql.add(user_id);
				}
				if(jsonObj.containsKey("remark")){
					String remark = jsonObj.getString("remark");
					updatesql.append(",remark=?");
					valuesql.add(remark);
				}
				updatesql.append(" where park_id=? and log_id=?");
				valuesql.add(parkId);
				valuesql.add(logId);
				update = daService.update(updatesql.toString(), valuesql);
			}else{
				//进行添加操作
				Long id = daService.getLong(
								"SELECT nextval('seq_park_log_tb'::REGCLASS) AS newid",
								null);
				StringBuffer updatesql = new StringBuffer();
				StringBuffer valuesql = new StringBuffer();
				List listsql = new ArrayList();
				updatesql.append("insert into park_log_tb (id,log_id,content,operate_time,park_id");
				valuesql.append("?,?,?,?,?");
				listsql.add(id);
				listsql.add(logId);
				listsql.add(content);
				listsql.add(operateTime);
				listsql.add(parkId);
				if(jsonObj.containsKey("type")){
					String type = jsonObj.getString("type");
					updatesql.append(",type");
					valuesql.append(",?");
					listsql.add(type);
				}
				if(jsonObj.containsKey("user_id")){
					String operateUser = jsonObj.getString("user_id");
					updatesql.append(",operate_user");
					valuesql.append(",?");
					listsql.add(operateUser);
				}
				if(jsonObj.containsKey("remark")){
					String remark = jsonObj.getString("remark");
					updatesql.append(",remark");
					valuesql.append(",?");
					listsql.add(remark);
				}
				updatesql.append(" )values("+valuesql+")");
				update = daService.update(updatesql.toString(), listsql);
			}
//			JSONObject jsonFirst = new JSONObject();
//			jsonFirst.put("state", update);
//			jsonFirst.put("log_id", logId);
//			jsonFirst.put("service_name", "park_log");
//			jsonResult.put("result", jsonFirst.toString());
			jsonResult.put("state", update);
			jsonResult.put("log_id", logId);
			jsonResult.put("service_name", "park_log");
			jsonResult.put("errmsg", "");
		}else{
//			logger.error(logStr + "error:data异常，未找到必须字段（log_id,operate_time,content）值。");
//			JSONObject jsonError = new JSONObject();
//			jsonError.put("result", "{\"state\":0,\"service_name\":\"park_log\",\"log_id\":\""+logIdRet+"\",\"error\":\"上传数据中没找到必须的字段（log_id,operate_time,content）值!\"}");
//			return jsonError.toString();
			logger.error(logStr + "error:data异常，未找到必须字段（log_id,operate_time,content）值。");
			String jsonError =  "{\"state\":0,\"service_name\":\"park_log\",\"log_id\":\""+logIdRet+"\",\"errmsg\":\"上传数据中没找到必须的字段（log_id,operate_time,content）值!\"}";
			return jsonError;
		}
		return jsonResult.toString();
	}
	@Override
	public String uploadMonthMember(String comid, String data) {
		String logStr = "tcp uploadMonthMember to cloud";
		// 定义返回值对象
		JSONObject jsonResult = new JSONObject();
		int update=0;
		JSONObject jsonObj = JSONObject.fromObject(data);
		String memberIdRet = "";
		if(jsonObj.containsKey("member_id")){
			memberIdRet = jsonObj.getString("member_id");
		}
		int operateTypeRet=0;
		if(jsonObj.containsKey("operate_type")){
			operateTypeRet = jsonObj.getInt("operate_type");
		}
		//判断是否存在必传字段
		if(jsonObj.containsKey("card_id") && jsonObj.containsKey("car_number") 
				 && jsonObj.containsKey("operate_type")){
			//开始进行逻辑操作
			int operateType = jsonObj.getInt("operate_type");
			/*针对月卡会员的上传因为涉及到数据表之间的关联所以需要先判断该月卡是否存在
			 * 当月卡不存在的时候则提示收费系统先办理月卡，
			 * 然后根据正确的月卡编号添加月卡会员
			**/
			Long pid = -1L;
			if(jsonObj.containsKey("pid")){
				pid = jsonObj.getLong("pid");
			}
			String memberId = "";
			if(jsonObj.containsKey("member_id")){
				memberId=jsonObj.getString("member_id");
			}
			String cardId = "";
			if(jsonObj.containsKey("card_id")){
				cardId=jsonObj.getString("card_id");
			}else {
				logger.error("cardID:"+cardId+",comid:"+comid);
				JSONObject jsonError = new JSONObject();
				jsonError.put("result", "{\"state\":0,\"error\":\"没有月卡编号(card_id)\",\"service_name\":\"upload_month_member\"}");
				return jsonError.toString();
			}
			/*当operate_type=1时，上传相同的card_id时查询card_id是否已存在，是则更新，否则添加*/
			if(operateType == 1){
				//当月卡会员上传相同的card_id时做更新操作，查询card_id是否已在数据库中存在
				Long count = daService.getLong("select count(*) from carower_product where card_id=? and com_id=?", new Object[]{cardId,Long.valueOf(comid)});
				if(count >= 1){
					operateType = 2;
				}
			}
			Long uin = -1l;
			//判断数据的操作类型
			if(operateType == 2){//修改会员信息
				//查询对应的会员id
				//已经存在则对会员记录做更新操作
				StringBuffer updatesql = new StringBuffer();
				//更新carower_product表中的记录
				List valuesql = new ArrayList();
				updatesql.append("update carower_product set pid=?");
				valuesql.add(pid);
				if(jsonObj.containsKey("update_time")){
					Long updateTime = jsonObj.getLong("update_time");
					updatesql.append(",update_time=?");
					valuesql.add(updateTime);
				}
				//如果上传的开始时间小于等于月卡结束时间,则不更新开始时间,否则更新开始时间
				Map map = daService.getMap("select e_time from carower_product where card_id=? and com_id = ?", new Object[]{cardId,Long.valueOf(comid)});
				Long etime = null;
				if(map!=null){
					etime = (Long) map.get("e_time");
				}
				if(jsonObj.containsKey("begin_time")){
					Long begin_time = jsonObj.getLong("begin_time");
					if(begin_time>etime){
						//开始时间大于结束时间,过期续费,需更新开始时间;否则为月卡期限内,则只更新月卡结束时间
						updatesql.append(",b_time=?");
						valuesql.add(begin_time);
					}
				}
				if(jsonObj.containsKey("end_time")){
					Long end_time = jsonObj.getLong("end_time");
					updatesql.append(",e_time=?");
					valuesql.add(end_time);
				}
				if(jsonObj.containsKey("name")){
					String name = jsonObj.getString("name");
					updatesql.append(",name=?");
					valuesql.add(name);
				}
				if(jsonObj.containsKey("price")){
					updatesql.append(",total=?");
					valuesql.add(StringUtils.formatDouble(jsonObj.get("price")));
				}
				if(jsonObj.containsKey("member_id")){
					updatesql.append(",member_id=?");
					valuesql.add(jsonObj.getString("member_id"));
				}
				if(jsonObj.containsKey("car_number")){
					updatesql.append(",car_number=?");
					valuesql.add(jsonObj.getString("car_number"));
				}
				updatesql.append(" where card_id=? and com_id = ?");
				valuesql.add(cardId);
				valuesql.add(Long.valueOf(comid));
				update = daService.update(updatesql.toString(), valuesql);
				logger.error("edit month user :"+update);
			}else if(operateType ==1 ){
				//进行添加操作
				//添加成功后添加到其他表中对应的字段信息
				//添加carower_product表中的记录
				//添加user_info_tb表
				StringBuffer updatesqlTwo = new StringBuffer();
				StringBuffer valuesqlTwo = new StringBuffer();
				List<Object> listsqlTwo = new ArrayList<Object>();
				updatesqlTwo.append("insert into carower_product (uin,create_time,pid,b_time,e_time," +
						"total,name,car_number,member_id,card_id,com_id");
				valuesqlTwo.append("?,?,?,?,?,?,?,?,?,?,?");
				/*
				 * 根据车牌号car_number查询对应的uin
				 * */
				Long idInit = -1L;
				String carNumber = jsonObj.getString("car_number");
				String [] carNumberStr = carNumber.split("\\|");
				if(carNumberStr != null && carNumberStr.length>0){
					for(String str:carNumberStr){
						Map uinMap = daService.getMap("select * from car_info_tb where car_number=?", new Object[]{str});
						if(uinMap != null && !uinMap.isEmpty()){
							Long idNew = (Long) uinMap.get("id");
							if(idNew >= idInit){
								uin = (Long) uinMap.get("uin");
								idInit = idNew;
							}
						}
					}
				}
				listsqlTwo.add(uin);
				if(jsonObj.containsKey("create_time")){
					Long createTime = jsonObj.getLong("create_time");
					listsqlTwo.add(createTime);
				}else{
					Long createTime = System.currentTimeMillis()/1000;
					listsqlTwo.add(createTime);
				}
				listsqlTwo.add(pid);
				if(jsonObj.containsKey("begin_time")){
					Long btime = jsonObj.getLong("begin_time");
					listsqlTwo.add(btime);
				}else{
					listsqlTwo.add(System.currentTimeMillis()/1000);
				}
				if(jsonObj.containsKey("end_time")){
					Long etime = jsonObj.getLong("end_time");
					listsqlTwo.add(etime);
				}else{
					listsqlTwo.add(System.currentTimeMillis()/1000+86400);
				}
				if(jsonObj.containsKey("price")){
					listsqlTwo.add(StringUtils.formatDouble(jsonObj.get("price")));
				}else{
					listsqlTwo.add(0.0);
				}
				if(jsonObj.containsKey("name")){
					listsqlTwo.add(jsonObj.getString("name"));
				}else{
					listsqlTwo.add("");
				}
				if(jsonObj.containsKey("car_number")){
					listsqlTwo.add(jsonObj.getString("car_number"));
				}else{
					listsqlTwo.add("");
				}
				if(jsonObj.containsKey("member_id")){
					listsqlTwo.add(jsonObj.getString("member_id"));
				}else{
					listsqlTwo.add("");
				}
				if(jsonObj.containsKey("card_id")){
					listsqlTwo.add(jsonObj.getString("card_id"));
				}else{
					listsqlTwo.add("");
				}
				listsqlTwo.add(Long.valueOf(comid));
				updatesqlTwo.append(" )values("+valuesqlTwo+")");
				update = daService.update(updatesqlTwo.toString(), listsqlTwo);
				logger.error("add month user :"+update);
			}else if(operateType == 3 ){
				//进行删除操作
				logger.error("删除月卡....cardid:"+cardId+",comid:"+comid);
				if(!"".equals(cardId)&&comid!=null){
					String sqlTwo = "delete from carower_product where card_id=? and com_id=?";
					update = daService.update(sqlTwo, new Object[]{cardId,Long.valueOf(comid)});
					logger.error("删除月卡....result:"+update);
				}else {
					logger.error("cardID:"+cardId+",comid:"+comid);
					String jsonError = "{\"state\":0,\"error\":\"删除失败，没有月卡编号(card_id)或车场信息\",\"service_name\":\"upload_month_member\"}";
					return jsonError;
				}
			}else{
				logger.error(logStr +"error:operate_type不存在："+operateType);
				String jsonError = "{\"state\":0,\"errmsg\":\"operate_type不存在\",\"service_name\":\"upload_month_member\"}";
				return jsonError;
			}
			jsonResult.put("state", update);
			jsonResult.put("member_id", memberId);
			jsonResult.put("service_name", "upload_month_member");
			jsonResult.put("operate_type", operateType);
			jsonResult.put("errmsg", "");
		}else{
			logger.error(logStr + "error:data异常，未找到必须字段（member_id,car_number,pid,operate_type）值。");
			String jsonError = "{\"state\":0,\"service_name\":\"upload_month_member\",\"member_id\":\""+memberIdRet+"\",\"operate_type\":"+operateTypeRet+",\"errmsg\":\"上传数据中没找到必须的字段（member_id,car_number,pid,operate_type）值!\"}";
			return jsonError;
		}
		return jsonResult.toString();
	}

	
	
	
	/**
	 * 免费订单
	 * @param orders
	 * @return
	 */
	private String freeOrder(String orders) {
		 JSONObject jo = JSONObject.fromObject(orders);
		 //获取云端订单编号
		 Long cloudOrderId = jo.getLong("cloudOrderId");
		 StringBuffer insertsql = new StringBuffer("update order_tb set");
		 ArrayList list = new ArrayList();
		 //获取订单代付总额
		 if(jo.containsKey("total")){
			 if(!"null".equals(jo.getDouble("total"))){
				 insertsql.append(" total=?,");
				 list.add(jo.getDouble("total"));
			 }
		 }
		 //获取停车结束时间
		 if(jo.containsKey("end_time")){
			 if(!"null".equals(jo.getLong("end_time"))){
				 insertsql.append(" end_time=?,");
				 list.add(jo.getLong("end_time"));
			 }
		 }
		 //获取当前订单的支付状态
		 if(jo.containsKey("state")){
			 if(!"null".equals(jo.getLong("state"))){
				 insertsql.append(" state=?,");
				 list.add(jo.getLong("state"));
			 }
		 }
		 //获取订单的支付方式
		 if(jo.containsKey("pay_type")){
			 if(!"null".equals(jo.getString("pay_type"))){
				 insertsql.append(" pay_type=?,");
				 System.out.println(jo.getInt("pay_type"));
				 list.add(jo.getInt("pay_type"));
			 }
		 }
		 //获取订单的免费原因
		 if(jo.containsKey("freereasons")){
			 if(!"null".equals(jo.getString("freereasons"))&&!"".equals(jo.getString("freereasons"))){
				 insertsql.append(" freereasons=?,");
				 list.add(jo.getInt("freereasons"));
			 }
		 }
		 insertsql.append(" out_passid=?");
		 list.add(jo.getString("out_passid").equals("null")?-1:jo.getLong("out_passid"));
		 String sql = insertsql+" where id = ?";
		 list.add(cloudOrderId);
		 int update = daService.update(sql, list.toArray());
		 System.out.println(sql);
		 logger.error("本地更新免费订单ret:"+update+",orderid:"+cloudOrderId);
		 if(update==1){
			 if(jo.containsKey("state")){
				 if(jo.getLong("state")==1){
					 if(jo.containsKey("pay_type")){
						 if(jo.getInt("pay_type")==8){
							 Long c = daService.getLong("select count(*) from parkuser_cash_tb where orderid = ?", new Object[]{cloudOrderId});
							 if(c!=null&&c>0){
								 int r = daService.update("delete from parkuser_cash_tb where orderid = ?", new Object[]{cloudOrderId});
								 logger.error("删除现金收费记录ret:"+r+",orderCloudid:"+cloudOrderId+",amount:"+jo.getDouble("total")+",删除现金收费记录ret:"+r);
							 }
						 }
					 }
				 }
			 }
		 }
		 JSONObject jsonObj = new JSONObject();
		 jsonObj.put("resultVal", update);
		 if(jo.containsKey("localOrderId")){
			 jsonObj.put("localOrderId", jo.getLong("localOrderId"));
		 }
		return jsonObj.toString();
	}
	
	/**
	 * 结算 （已上传生成 的订单）
	 * 
	 * @param order
	 * @param jo
	 * @return
	 */
	private String completeOrder(String order, JSONObject jo) {
		if (jo == null)
			jo = JSONObject.fromObject(order);
		boolean backflag = false;
		// 获取订单是否已上传，0表示未上传，1表示上传成功
		Integer sync_state = null;
		// 获取订单的支付类型
		Integer pay_type = null;
		if (jo.containsKey("sync_state")) {
			sync_state = jo.getInt("sync_state");
		}
		// 获取订单的支付类型
		if (jo.containsKey("pay_type")) {
			String payType = jo.getString("pay_type");
			if("cash".equals(payType)){
				pay_type=1;
			}else if("wallet".equals(payType)){
				pay_type=2;
			}else if("month_user".equals(payType)){
				pay_type=3;
			}
		}
		// 线上订单状态 为0时才能退款 ，如果线下state==0假如安卓hd切到线上的话会结算订单
		// 但是线下没有结算 当车再次进场0元结算掉会同步上来update订单 因此这时候需要伪装下返回值
		int lstate = 0;
		// 获取云平台订单id
		Long orderId = jo.getLong("cloudOrderId");
		boolean centerflag = false;
		if (sync_state != null) {
			if (sync_state == 3 && !backflag) {// 处理退款
				Map map = daService
						.getMap("select * from order_tb where id =? and state=? and total>? and pay_type=? and need_sync>? and uin>?",
								new Object[] { orderId, 0, 0, 2, 0, 0 });
				if (map != null) {
					backflag = backmoney(jo);
				} else {
					if (pay_type == 2) {// 防止大量需要退款的时候会把订单改成电子支付
						pay_type = 1;
						backflag = true;
					}
				}
			}
		}
		if (sync_state != null) {
			if (sync_state == 3 && !backflag) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("resultVal", "1");
				jsonObj.put("localOrderId", jo.getLong("localOrderId"));
				return jsonObj.toString();
			}
		}
		// 解决线上生成订单的同时本地服务器此时开着同步下了订单 但是线上结算状态不同步下来
		// 该车下次进来的时候出现0元结算后同步上来 更改订单状态
		if (jo.get("total") != null && !"null".equals(jo.getString("total"))
				&& jo.getDouble("total") == 0) {
			Map map = daService
					.getMap("select * from order_tb where id =? and state=? and total>? and need_sync=? ",
							new Object[] { orderId, 1, 0, 3 });
			if (map != null) {
				Long etime = Long.parseLong(map.get("end_time") + "");
				if (etime + 60 < jo.getLong("end_time")) {
					// 当线下数据同步上去的时候金额为0 并且线上该订单线上已结算有金额（并且该订单是线上生成的）
					// 并且时间相差一分钟则直接返回成功 不更新数据
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("resultVal", "1");
					jsonObj.put("orderLocalId", jo.getLong("id"));
					return jsonObj.toString();
				}
			}
		}
		Long cloudOrderId = -1L;
		StringBuffer insertsql = new StringBuffer("update order_tb set");
		ArrayList list = new ArrayList();
		Long createtime = null;
		String carnumber = null;
		if (jo.get("create_time") != null) {
			if (!"null".equals(jo.getString("create_time"))) {
				createtime = jo.getLong("create_time");
				insertsql.append(" create_time=?,");
				list.add(createtime);
			}
		}
		if (jo.get("cloudOrderId") != null) {
			if (jo.getString("cloudOrderId") != null
					&& !"null".equals(jo.getString("cloudOrderId"))) {
				cloudOrderId = jo.getLong("cloudOrderId");
			}
		}
		if (jo.get("car_number") != null) {
			if (!"null".equals(jo.getString("car_number"))) {
				insertsql.append(" car_number=?,");
				list.add(jo.getString("car_number"));
				carnumber = jo.getString("car_number");
			}
		}
		if (jo.get("total") != null) {
			if (!"null".equals(jo.getString("total"))) {
				insertsql.append(" total=?,");
				list.add(jo.getDouble("total"));
			}
		}
		if (jo.get("state") != null) {
			if (!"null".equals(jo.getString("state"))) {
				Long state = jo.getLong("state");
				insertsql.append(" state=?,");
				list.add(state);
			}
		}
		if (jo.get("end_time") != null) {
			if (!"null".equals(jo.getString("end_time"))) {
				insertsql.append(" end_time=?,");
				list.add(jo.getLong("end_time"));
			}
		}
		Map map2 = daService.getMap("select * from order_tb where id =? ",
				new Object[] { orderId });
		if (map2 != null
				&& jo.get("pay_type") != null && jo.getInt("pay_type") != 8
				&& (Integer.parseInt(map2.get("pay_type") + "") == 4
						|| Integer.parseInt(map2.get("pay_type") + "") == 5 || Integer
						.parseInt(map2.get("pay_type") + "") == 6)) {
			centerflag = true;
		}
		if (!centerflag) {
			if(jo.containsKey("pay_type")){
				if (!"null".equals(jo.getString("pay_type"))) {
					insertsql.append(" pay_type=?,");
					list.add(pay_type);
				}
			}
		}
		if (jo.get("uid") != null) {
			if (!"null".equals(jo.getString("uid"))) {
				insertsql.append(" uid=?,");
				list.add(jo.getLong("uid"));
			}
		}
		if (jo.get("freereasons") != null
				&& !"null".equals(jo.getString("freereasons"))
				&& !"".equals(jo.getString("freereasons"))) {
			insertsql.append(" freereasons=?,");
			list.add(jo.getInt("freereasons"));
		}
		if (jo.get("isclick") != null
				&& !"null".equals(jo.getString("isclick"))
				&& !"".equals(jo.getString("isclick"))) {
			insertsql.append(" isclick=?,");
			list.add(jo.getInt("isclick"));
		}
		if (jo.get("car_type") != null
				&& !"null".equals(jo.getString("car_type"))
				&& !"".equals(jo.getString("car_type"))) {
			insertsql.append(" car_type=?,");
			list.add(jo.getInt("car_type"));
		}
		if (jo.get("c_type") != null && !"null".equals(jo.getString("c_type"))
				&& !"".equals(jo.getString("c_type"))) {
			insertsql.append(" c_type=?,");
			list.add(jo.getInt("c_type"));
		}
		if (jo.containsKey("out_passid")) {
			insertsql.append(" out_passid=?");
			list.add(jo.getLong("out_passid"));
		}
		String sql = insertsql + " where id = ? ";
		list.add(cloudOrderId);
		int update = daService.update(sql, list.toArray());
		System.out.println(sql);
		logger.error("本地结算订单更新ret:" + update + ",orderid:" + cloudOrderId);
		if (update == 1) {
			if (jo.get("state") != null && jo.getLong("state") > 0) {
				if (jo.getLong("state") == 1) {
						if(jo.containsKey("total") && jo.containsKey("pay_type") 
								&& jo.containsKey("c_type")){
						if (!"null".equals(jo.getString("total"))
								&& jo.getInt("pay_type") != 8
								&& jo.getInt("c_type") != 5
								&& jo.getInt("pay_type") != 2) {
							Long c = daService
									.getLong(
											"select count(*) from parkuser_cash_tb where orderid = ? and type = ?",
											new Object[] { cloudOrderId, 0 });
							if (c != null && c < 1) {
								int r = daService
										.update("insert into parkuser_cash_tb(uin,amount,orderid,create_time) values(?,?,?,?)",
												new Object[] { jo.getLong("uid"),
														jo.getDouble("total"),
														jo.getLong("cloudOrderId"),
														jo.getLong("end_time") });
								logger.error("写现金收费记录ret:" + r + ",orderid:"
										+ cloudOrderId + ",amount:"
										+ jo.getDouble("total") + ",生成现金收费记录ret:"
										+ r);
							} else {
								// 修改
								daService
										.update("update parkuser_cash_tb set amount=? where orderid =? ",
												new Object[] {
														StringUtils.formatDouble(jo
																.getDouble("total")),
														jo.getLong("cloudOrderId") });
								logger.error("修改现金记录：orderid:" + cloudOrderId
										+ ",amount:" + jo.getDouble("total"));
							}
						} else {
							logger.error("电子支付或者月卡或者免费不写现金记录orderid:"
									+ cloudOrderId);
						}
						if ((jo.getInt("pay_type") == 8)) {
							daService
									.update("update parkuser_cash_tb set amount=? where orderid =? ",
											new Object[] { 0, jo.getLong("line_id") });
							logger.error("修改现金记录：orderid:" + cloudOrderId
									+ ",amount:为0");
						}
					}
				}
			}
		}
		if (lstate == 1) {
			update = 1;
			if (jo.getInt("pay_type") == 8) {
				int r1 = daService.update(
						"update order_tb set pay_type=? where id=? ",
						new Object[] { 8, cloudOrderId });
				logger.error("orderid:" + cloudOrderId
						+ " set pay_type=8 result:" + r1);
				int r2 = daService
						.update("update parkuser_cash_tb set amount=? where orderid=? and type=? ",
								new Object[] { 0, cloudOrderId, 0 });
				logger.error("orderid:" + cloudOrderId
						+ " set parkuser_cash_tb=0 result:" + r1);
				update = 2;
			}
		}
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("resultVal", update);
		jsonObj.put("localOrderId", jo.getLong("localOrderId"));
		return jsonObj.toString();

	}
	
	/**
	 * 车主预支付款多余的情况下，进行退款
	 * @param jo
	 * @return
	 */
	private boolean backmoney(JSONObject jo) {
		boolean b = false;
		// 获取云平台订单id
		Long orderId = jo.getLong("line_id");
		Map map = daService.getMap("select * from order_tb where id =?",
				new Object[] { orderId });
		Object t = map.get("total");
		if (map != null && t != null && Double.parseDouble(t + "") > 0
				&& Integer.parseInt(map.get("pay_type") + "") != 4
				&& Integer.parseInt(map.get("pay_type") + "") != 5
				&& Integer.parseInt(map.get("pay_type") + "") != 6) {// 多余金额退回车主微信钱包
			Double prefee = Double.parseDouble(t + "");
			Double total = 0d;
			Double back = 0d;
			Double tcbback = 0d;
			Long uin = Long.parseLong(map.get("uin") + "");
			Long uid = Long.parseLong(map.get("uid") + "");
			Long comid = Long.parseLong(map.get("comid") + "");
			// 多余金额退回车主微信钱包
			if (prefee > total) {
				logger.error("预支付金额大于停车费金额,预支付金额：" + prefee + ",停车费金额：" + total
						+ ",orderid:" + orderId + ",uin:" + uin);
				List<Map<String, Object>> backSqlList = new ArrayList<Map<String, Object>>();
				// 创建格式数据的类，保留两位小数
				DecimalFormat dFormat = new DecimalFormat("#.00");
				// 如果用过三折券，就一直用三折券
				Map<String, Object> ticketMap = daService
						.getMap("select * from ticket_tb where orderid=? order by utime limit ?",
								new Object[] { orderId, 1 });
				if (ticketMap != null) {
					Long ticketId = (Long) ticketMap.get("id");
					logger.error("使用过券，ticketid:" + ticketId + ",orderid="
							+ orderId + ",uin:" + uin);
					Double umoney = Double
							.valueOf(ticketMap.get("umoney") + "");
					umoney = Double.valueOf(dFormat.format(umoney));
					Double preupay = Double.valueOf(dFormat.format(prefee
							- umoney));
					logger.error("预支付金额prefee：" + prefee + ",使用券的金额umoney："
							+ umoney + ",车主实际支付的金额：" + preupay + ",orderid:"
							+ orderId);
					Double tmoney = 0d;
					Integer type = (Integer) ticketMap.get("type");
					if (type == 0 || type == 1) {// 代金券
						tmoney = publicMethods.getTicketMoney(ticketId, 2, uid,
								total, 2, comid, orderId);
						logger.error("orderid:" + orderId + ",uin:" + uin
								+ ",tmoney:" + tmoney);
					} else if (type == 2) {
						tmoney = publicMethods.getDisTicketMoney(uin, uid,
								total);
						logger.error("orderid:" + orderId + ",uin:" + uin);
					}
					Double upay = Double
							.valueOf(dFormat.format(total - tmoney));
					logger.error("实际停车费total:" + total + ",实际停车费应该打折的金额tmoney:"
							+ tmoney + ",实际停车费车主实际应该支付的金额upay：" + upay
							+ ",orderid:" + orderId);
					if (preupay > upay) {
						back = Double.valueOf(dFormat.format(preupay - upay));
						logger.error("preupay:" + preupay + ",upay:" + upay
								+ ",orderid:" + orderId + ",uin:" + uin);
					}
					if (umoney > tmoney) {
						tcbback = Double.valueOf(dFormat
								.format(umoney - tmoney));
					}
					int r = daService.update(
							"update ticket_tb set bmoney = ? where id=? ",
							new Object[] { tmoney, ticketMap.get("id") });
				} else {
					logger.error("没有使用过券orderid:" + orderId + ",uin:" + uin);
					back = Double.valueOf(dFormat.format(prefee - total));
				}
				logger.error("预支付退还金额:" + back + ",停车券返款金额：tcbback:" + tcbback);
				if (back > 0) {
					Long count = daService.getLong(
							"select count(*) from user_info_tb where id=? ",
							new Object[] { uin });
					Map<String, Object> usersqlMap = new HashMap<String, Object>();
					if (count > 0) {// 真实帐户
						usersqlMap
								.put("sql",
										"update user_info_tb set balance=balance+? where id=? ");
						usersqlMap.put("values", new Object[] { back, uin });
						backSqlList.add(usersqlMap);
					} else {// 虚拟账户
						usersqlMap
								.put("sql",
										"update wxp_user_tb set balance=balance+? where uin=? ");
						usersqlMap.put("values", new Object[] { back, uin });
						backSqlList.add(usersqlMap);
					}
					Map<String, Object> userAccountsqlMap = new HashMap<String, Object>();
					userAccountsqlMap
							.put("sql",
									"insert into user_account_tb(uin,amount,type,create_time,remark,pay_type,orderid) values(?,?,?,?,?,?,?)");
					userAccountsqlMap.put("values", new Object[] { uin, back,
							0, System.currentTimeMillis() / 1000 - 2, "预支付返款",
							12, orderId });
					backSqlList.add(userAccountsqlMap);
					if (tcbback > 0) {
						Map<String, Object> tcbbacksqlMap = new HashMap<String, Object>();
						tcbbacksqlMap
								.put("sql",
										"insert into tingchebao_account_tb(amount,type,create_time,remark,utype,orderid) values(?,?,?,?,?,?)");
						tcbbacksqlMap.put("values", new Object[] { tcbback, 0,
								System.currentTimeMillis() / 1000, "停车券返款金额",
								6, orderId });
						backSqlList.add(tcbbacksqlMap);
					}

					b = daService.bathUpdate(backSqlList);
					logger.error("预支付返款结果：" + b + ",orderid:" + orderId
							+ ",uin:" + uin);
				} else {
					logger.error("退还金额back小于0，orderid：" + orderId + ",uin:"
							+ uin);
				}
			}
		}
		return b;
	}

	
	/**
	 * 线下修改订单上传
	 * @param order
	 * @return
	 */
	private String updateOrder(String order) {
		JSONObject jo = JSONObject.fromObject(order);
		Long cloudOrderId = -1L;
		StringBuffer insertsql = new StringBuffer("update order_tb set");
		ArrayList list = new ArrayList();
		Long createtime = null;
		String carnumber = null;
		if(jo.containsKey("create_time")){
			if (!"null".equals(jo.getString("create_time"))) {
				createtime = jo.getLong("create_time");
				insertsql.append(" create_time=?,");
				list.add(createtime);
			}
		}
		if(jo.containsKey("cloudOrderId")){
			if (jo.getString("cloudOrderId") != null
					&& !"null".equals(jo.getString("cloudOrderId"))) {
				cloudOrderId = jo.getLong("cloudOrderId");
			}
		}
		if(jo.containsKey("car_number")){
			if (!"null".equals(jo.getString("car_number"))) {
				insertsql.append(" car_number=?,");
				list.add(jo.getString("car_number"));
				carnumber = jo.getString("car_number");
			}
		}
		long uin = -1;
		Map u = daService.getMap(
				"select uin from car_info_tb where car_number=?",
				new Object[] { carnumber });
		if (u != null && u.get("uin") != null) {
			uin = Long.valueOf(u.get("uin") + "");
			insertsql.append(" uin=?,");
			list.add(uin);
		}
		if(jo.containsKey("total")){
			if (!"null".equals(jo.getString("total"))) {
				insertsql.append(" total=?,");
				list.add(jo.getDouble("total"));
			}
		}
		if(jo.containsKey("state")){
			if (!"null".equals(jo.getString("state"))) {
				insertsql.append(" state=?,");
				list.add(jo.getLong("state"));
			}
		}
		if(jo.containsKey("end_time")){
			if (!"null".equals(jo.getString("end_time"))) {
				insertsql.append(" end_time=?,");
				list.add(jo.getLong("end_time"));
			}
		}
		if(jo.containsKey("pay_type")){
			if (!"null".equals(jo.getString("pay_type"))) {
				insertsql.append(" pay_type=?,");
				System.out.println(jo.getInt("pay_type"));
				list.add(jo.getInt("pay_type"));
			}
		}
		if(jo.containsKey("uid")){
			if (!"null".equals(jo.getString("uid"))) {
				insertsql.append(" uid=?,");
				list.add(jo.getLong("uid"));
			}
		}
		if(jo.containsKey("out_passid")){
			insertsql.append(" out_passid=?,");
			list.add(jo.getLong("out_passid"));
		}
		if(jo.containsKey("type")){
			insertsql.append(" type=?,");
			list.add(0);
		}
		if(jo.containsKey("freereasons")){
			insertsql.append(" freereasons=?,");
			if (!"null".equals(jo.getString("freereasons"))
					&& !"".equals(jo.getString("freereasons"))) {
				list.add(jo.getInt("freereasons"));
			} else {
				list.add("");
			}
		}
		if(jo.containsKey("isclick")){
			insertsql.append(" isclick=?,");
			if (!"null".equals(jo.getString("isclick"))
					&& !"".equals(jo.getString("isclick"))) {
				list.add(jo.getInt("isclick"));
			} else {
				list.add(0);
			}
		}
		//开源接口对sql语句进行处理
		if(",".equals(insertsql.substring(insertsql.length()-1,insertsql.length()))){
			String insertsqlStr = insertsql.substring(0,insertsql.length()-1);
			insertsql = new StringBuffer(insertsqlStr);
		}
		String sql = insertsql + " where id = ?";
		list.add(cloudOrderId);
		int update = daService.update(sql, list.toArray());
		System.out.println(sql);
		logger.error("本地结算订单更新ret:" + update + ",orderid:" + cloudOrderId);
		if (update == 1) {
			if (jo.get("state") != null && jo.getLong("state") > 0) {
				if (jo.getLong("state") == 1) {
					if(jo.containsKey("total") && jo.containsKey("c_type") && jo.containsKey("pay_type")){
						if (!"null".equals(jo.getString("total"))
								&& jo.getInt("c_type") != 5
								&& jo.getInt("pay_type") == 1) {
							Long c = daService
									.getLong(
											"select count(*) from parkuser_cash_tb where orderid = ?",
											new Object[] { cloudOrderId });
							if (c != null && c < 1) {
								if(jo.containsKey("uid") && jo.containsKey("total")
										&& jo.containsKey("cloudOrderId") && jo.containsKey("end_time")){
									int r = daService
											.update("insert into parkuser_cash_tb(uin,amount,orderid,create_time) values(?,?,?,?)",
													new Object[] { jo.getLong("uid"),
															jo.getDouble("total"),
															jo.getLong("cloudOrderId"),
															jo.getLong("end_time") });
									logger.error("写现金收费记录ret:" + r + ",orderid:"
											+ cloudOrderId + ",amount:"
											+ jo.getDouble("total") + ",生成现金收费记录ret:"
											+ r);
								}
							} else {
								if(jo.containsKey("total") && jo.containsKey("cloudOrderId")){
									daService
									.update("update parkuser_cash_tb set amount=? where orderid =? ",
											new Object[] {
													StringUtils.formatDouble(jo
															.getDouble("total")),
													jo.getLong("cloudOrderId") });
									logger.error("修改现金记录：orderid:" + cloudOrderId
									+ ",amount:" + jo.getDouble("total"));
								}
							}
						} else {
							logger.error("月卡或者免费或者电子支付不写现金记录orderid:" + cloudOrderId);
						}
					}
				}
			}
		}
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("resultVal", update);
		jsonObj.put("uin", uin);
		jsonObj.put("cloudOrderId", cloudOrderId);
		if(jo.containsKey("localOrderId")){
			jsonObj.put("localOrderId", jo.getLong("localOrderId"));
		}
		return jsonObj.toString();
	}
	
	/**
	 * 处理上传订单
	 * @param orders
	 * @return
	 */
	private String doUploadOrders(String orders) {
		//获取上传的订单，并转为json数组
		JSONArray ja = JSONArray.fromObject(orders);
		String ret = "";
		//定义返回的JSONArray
		JSONArray jsonResult = new JSONArray();
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			if(jo.containsKey("state")){
				if (jo.getInt("state") == 0) {
//					ret = addOrder(null, jo);
				} else if (jo.getInt("state") == 1) {
					if (jo.get("cloudOrderId") == null
							|| "".equals(jo.get("cloudOrderId") + "")
							|| "null".equals(jo.get("cloudOrderId") + "")) {
//						ret = addOrder(null, jo);
					} else {
						ret = completeOrder(null, jo);
					}
				}
			}else{
				ret = "state--支付状态值为空";
			}
			jsonResult.add(ret);
		}
		return jsonResult.toString();
	}
	
	/** 拼接sql 和values
	 * @param comJo  数据
	 * @param tablename
	 */
	public  String sqlAndValue(JSONObject comJo,String tablename) {
		String ret = "";
		List list = new ArrayList();
		Map<String,String>	columnsList = getColumns(tablename);
		StringBuffer insertsql = new StringBuffer("insert into "+tablename +" (");
		StringBuffer valuesql = new StringBuffer(" values(");
		ArrayList values = new ArrayList();
		Long nextid = 0L;
		for (Map.Entry<String, String> entry : columnsList.entrySet()) {
			try{
				if(comJo.getString(entry.getKey())!=null&&!"null".equals(comJo.getString(entry.getKey()))){
					insertsql.append(entry.getKey()+",");
					valuesql.append("?,");
					if(entry.getKey().equals("id")){
						nextid = daService.getLong(
								"SELECT nextval('seq_"+tablename+"'::REGCLASS) AS newid", null);
						values.add(nextid);
					}else{
						if(entry.getValue().startsWith("bigint")){
							values.add(comJo.getLong(entry.getKey()));
						}else if(entry.getValue().startsWith("numeric")){
							values.add(comJo.getDouble(entry.getKey()));
						}else if(entry.getValue().startsWith("integer")){
							values.add(comJo.getInt(entry.getKey()));
						}else if(entry.getValue().startsWith("charact")){
							values.add(comJo.getString(entry.getKey()));
						}
					}
				}
			}catch (Exception e) {
				e.getMessage();
			}
		}
		String sql = "";
		int r = 0;
		if(insertsql.toString().endsWith(",")&&valuesql.toString().endsWith(",")){
			sql = insertsql.substring(0,insertsql.length()-1)+") "+valuesql.substring(0,valuesql.length()-1)+")";
			try{
				r = daService.update(sql, values);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("resultVal", r);
		jsonObj.put("cloudId", nextid);
		jsonObj.put("localId",comJo.getLong("id"));
		jsonObj.put("tableName", tablename);
		return jsonObj.toString();
	}
	
	/**
	 * 根据数据库中对应的表名获取所有的字段名和字段类型
	 * @param tablename
	 * @return
	 */
	public  Map getColumns(String tablename){
		HashMap<String, String> hashMap = new HashMap<String,String>();
		List list = daService.getAll("select column_name,data_type from information_schema.columns where table_schema='public' and table_name= ? ", new Object[]{tablename});
		for (Object object : list) {
			Map map = (Map)object;
			hashMap.put(map.get("column_name")+"",map.get("data_type")+"");
		}
		return hashMap;
	}
	
	/**
	 * 添加本地上传的ticket
	 * @param jo
	 * @return
	 */
	private String addTicket(JSONObject jo) {
		if(jo==null){
			return "";
		}
		Long count = daService.getLong("select count(*) from ticket_tb where orderid = ? and create_time=? and state = ? ", new Object[]{jo.getLong("lineorderid"),jo.getLong("create_time"),1});
		if(count!=null&&count>0){
			String sql = "update ticket_tb set ";
			ArrayList list = new ArrayList();
			if(jo.get("umoney")!=null&&!"null".equals(jo.getString("umoney"))){
				sql += "umoney = ? ,";
				list.add(jo.getDouble("umoney"));
			}
			if(jo.get("bmoney")!=null&&!"null".equals(jo.getString("bmoney"))){
				sql += "bmoney = ? ,";
				list.add(jo.getDouble("bmoney"));
			}
			if(jo.get("state")!=null&&!"null".equals(jo.getString("state"))){
				sql += "state = ? ,";
				list.add(jo.getInt("state"));
			}
			if(sql.length()>21&&sql.endsWith(",")){
				sql = sql.substring(0, sql.length()-1)+" where id =?";
				list.add(jo.getLong("lineid"));
			}
			daService.update(sql, list);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("resultVal", "1");
			jsonObj.put("localId", jo.getLong("id"));
			jsonObj.put("tableName", "ticket_tb");
			return jsonObj.toString();
		}
		Long nextid = daService.getLong(
					"SELECT nextval('seq_ticket_tb'::REGCLASS) AS newid", null);
		StringBuffer insertsql = new StringBuffer();
		StringBuffer valuesql = new StringBuffer();
		ArrayList list = new ArrayList();
		insertsql.append("insert into ticket_tb(id,");
		valuesql.append(" values (?,");
		list.add(nextid);
		if(jo.get("create_time")!=null&&!"null".equals(jo.getString("create_time"))){
			insertsql.append("create_time,");
			valuesql.append("?,");
			list.add(jo.getLong("create_time"));
		}
		if(jo.get("limit_day")!=null&&!"null".equals(jo.getString("limit_day"))){
			insertsql.append("limit_day,");
			valuesql.append("?,");
			list.add(jo.getLong("limit_day"));
		}
		if(jo.get("money")!=null&&!"null".equals(jo.getString("money"))){
			insertsql.append("money,");
			valuesql.append("?,");
			list.add(jo.getLong("money"));
		}
		if(jo.get("state")!=null&&!"null".equals(jo.getString("state"))){
			insertsql.append("state,");
			valuesql.append("?,");
			list.add(jo.getLong("state"));
		}
		if(jo.get("uin")!=null&&!"null".equals(jo.getString("uin"))){
			insertsql.append("uin,");
			valuesql.append("?,");
			list.add(jo.getLong("uin"));
		}
		if(jo.get("comid")!=null&&!"null".equals(jo.getString("comid"))){
			insertsql.append("comid,");
			valuesql.append("?,");
			list.add(jo.getLong("comid"));
		}
		if(jo.get("utime")!=null&&!"null".equals(jo.getString("utime"))){
			insertsql.append("utime,");
			valuesql.append("?,");
			list.add(jo.getLong("utime"));
		}
		if(jo.get("umoney")!=null&&!"null".equals(jo.getString("umoney"))){
			insertsql.append("umoney,");
			valuesql.append("?,");
			list.add(jo.getDouble("umoney"));
		}
		if(jo.get("type")!=null&&!"null".equals(jo.getString("type"))){
			insertsql.append("type,");
			valuesql.append("?,");
			list.add(jo.getLong("type"));
		}
		if(jo.get("lineorderid")!=null&&!"null".equals(jo.getString("lineorderid"))){
			insertsql.append("orderid,");
			valuesql.append("?,");
			list.add(jo.getLong("lineorderid"));
		}
		if(jo.get("bmoney")!=null&&!"null".equals(jo.getString("bmoney"))){
			insertsql.append("bmoney,");
			valuesql.append("?,");
			list.add(jo.getDouble("bmoney"));
		}
		String sql = insertsql.substring(0, insertsql.length()-1)+")"+valuesql.substring(0, valuesql.length()-1)+")";
		int r = daService.update(sql, list);
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("resultVal", r);
		jsonObj.put("localId", jo.getLong("id"));
		jsonObj.put("tableName", "ticket_tb");
		return jsonObj.toString();
	}
	
	/**
	 * 测试方法使用
	 * @param args
	 */
	public static void main(String[] args) {
		JSONObject json = new JSONObject();
		JSONArray jsonAry = new JSONArray();
		json.put("trip", "济南");
		json.put("time", "2017-04-01");
		String jsonStr = json.toString();
		jsonAry.add(jsonStr);
		System.out.println(jsonAry.toString());
	}

	@Override
	public String doLogin(JSONObject data, String preSign,String sourceIP) {
		String logStr = "tcp park login ";
		logger.info(data);
		String token = "";
		String parkId = data.getString("park_id");
		String localId = "";
		if(data.containsKey("local_id"))//多终端电脑登录
			localId = data.getString("local_id");
		Long comid = Long.valueOf(parkId);
		//查询出车场具体信息
		Map<String, Object> comParkMap = daService.getMap("select * from com_info_tb where id=? ",
				new Object[]{comid});
		if(comParkMap!=null && !comParkMap.isEmpty()){
			String strKey = data.toString()+"key="+comParkMap.get("ukey");
			String sign=null;
			try {
				sign = StringUtils.MD5(strKey,"utf-8").toUpperCase();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("md5加密出现异常,请联系后台程序员。");
			}
			logger.error(strKey+","+sign+":"+preSign+",ret:"+sign.equals(preSign));
			if(sign.equals(preSign)){
				token = UUID.randomUUID().toString().replace("-", "");
				String countsql = "select count(*) from park_token_tb where park_id=? ";
				Object[] values = new Object[]{parkId};
				if(localId!=null&&!"".equals(localId)){
					countsql = "select count(*) from park_token_tb where park_id=? and local_id=? ";
					values = new Object[]{parkId,localId};
				}
				Long countUserSession = daService.getLong(countsql,values);
				int r = 0;
				String serverIP = "";//车场登录服务器IP,集群时，要记录下服务器IP,后台或接口推下行数据时，要到相应的服务器上推送
				try {
					serverIP = Inet4Address.getLocalHost().getHostAddress().toString();
				} catch (Exception e) {
					logger.error("取IP错误...");
				}
				if(countUserSession==0){
					//自动生成主键id
					Long id = daService.getLong(
							"SELECT nextval('seq_park_token_tb'::REGCLASS) AS newid", null);
					String sql  = "insert into park_token_tb(id,token,login_time,park_id,server_ip,source_ip,local_id)" +
							" values (?,?,?,?,?,?,?)";
					r = daService.update(sql, new Object[]{id,token,System.currentTimeMillis()/1000,parkId,serverIP,sourceIP,localId});
					logger.info("tcp login park:"+comid+",token"+token+",login result:"+r);
				}else {
					String sql = "update park_token_tb set token=?,login_time=?,server_ip=?,source_ip=? where park_id=? and local_id=?  ";
					Object[] params = new Object[]{token,System.currentTimeMillis()/1000,serverIP,sourceIP,parkId,localId};
					if(Check.isEmpty(localId)){
						sql = "update park_token_tb set token=?,login_time=?,server_ip=?,source_ip=? where park_id=? ";
						params = new Object[]{token,System.currentTimeMillis()/1000,serverIP,sourceIP,parkId};
					}
					r = daService.update(sql, params);
					logger.info("tcp login , relogon,park:"+comid+",token"+token+",result:"+r);
				}
				if(r==1){
					logger.error(logStr+"error:登录成功");
					return token;
				}else {
					logger.error(logStr+"error:登录失败");
					return "error:登录失败";
				}
			}else {
				logger.error(logStr+"error:车场签名错误");
				return "error:签名错误";
			}
		}else {
			logger.error(logStr+"error:车场不存在");
			return "error:车场不存在";
		}
	}

	@Override
	public String enterPark(String comid, String data) {
		//定义返回对象
		JSONObject jsonObj = new JSONObject();
		//定义返回值
		String result="";
		Long parkId = -1L;
		//判断必须上传的参数是否存在
		JSONObject json = JSONObject.fromObject(data);
		String orderId = "";
		if(json.containsKey("order_id")){
			orderId = json.getString("order_id");
		}
		if(json.containsKey("car_number") && json.containsKey("in_time") 
				&& json.containsKey("uid") && json.containsKey("order_id")){
			parkId = Long.valueOf(comid);
			result = addOrder(parkId,data);
		}else{
			logger.error("errmsg:data异常，未找到必须字段（car_number,in_time,uid,order_id）值。");
			result = "{\"state\":0,\"service_name\":\"in_park\",\"order_id\":\""+orderId+"\",\"errmsg\":\"上传数据中没找到必需的字段（car_number,in_time,uid,order_id）值!\"}";
		}
//		jsonObj.put("result", result);
//		return jsonObj.toString();
		return result;
	}
	
	/**
	 * 进入车场时通过Tcp通道传递数据
	 * @param parkId:车场编号
	 * @param data：车辆入场信息
	 * @return
	 */
	private String addOrder(Long parkId,String data) {
		//对传入数据进行utf-8解码操作
		data = StringUtils.decodeUTF8(data);
		JSONObject jo = JSONObject.fromObject(data);
		Long nextid = daService.getLong(
				"SELECT nextval('seq_order_tb'::REGCLASS) AS newid", null);
		StringBuffer insertsql = new StringBuffer("insert into order_tb(id,comid,uin");
		StringBuffer valuesql = new StringBuffer("?,?,?");
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(nextid);
		list.add(parkId);
		Long uin = -1L;
		//对data中的其他字段信息做逻辑处理
		String carNumber="";
		if(jo.containsKey("car_number")){
			 if(!"null".equals(jo.getString("car_number"))){
				 insertsql.append(",car_number");
				 valuesql.append(",?");
				 carNumber = jo.getString("car_number");
				 list.add(carNumber);
			 }
		}
		
		Long createTime = -1L;
		System.out.println(" add one order:"+data);
		if(jo.containsKey("in_time")){
			if(!"null".equals(jo.getLong("in_time"))){
				insertsql.append(",create_time");
				valuesql.append(",?");
				createTime = jo.getLong("in_time");
				list.add(createTime);
			}
		}
		
		if(createTime!=null && carNumber!=null){
			Map count =  daService.getMap("select id,uin from order_tb where car_number=?  and comid = ? and state = ?", new Object[]{carNumber,parkId,0});
			if(count!=null && count.size()>0){
				//如果线上服务器和本地上传的订单的时间，车牌相同则本地删除
				int r  = daService.update("delete from order_tb where car_number=?  and comid = ? and state = ?", new Object[]{carNumber,parkId,0});
				logger.error("车辆已入场，重新入场，删除原订单:"+r);
			}
			//根据车牌号获取车场会员信息
			Map u = daService.getMap("select uin from car_info_tb where car_number=?", new Object[]{carNumber});
			if(u==null||u.isEmpty()){
				u = daService.getMap("select uin from wxp_user_tb where car_number=?", new Object[]{carNumber});
			}
			if(u!=null && u.get("uin")!=null){
				uin = Long.valueOf(u.get("uin")+"");
			}
		}
		list.add(2,uin);
		//车辆类型值处理
		if(jo.containsKey("car_type")){
			 if(!"null".equals(jo.getString("car_type"))){
				 insertsql.append(",car_type");
				 valuesql.append(",?");
				 list.add(jo.getString("car_type"));
			 }
		}
		//车辆入场类型处理
		if(jo.containsKey("c_type")){
			 if(!"null".equals(jo.getString("c_type"))){
				 Object object = jo.get("c_type");
				 if(object instanceof String){
					 insertsql.append(",c_type");
					 valuesql.append(",?");
					 list.add(jo.getString("c_type"));
				 }
			 }
		}
		if(jo.containsKey("uid")){
			String uid = jo.getString("uid");
			Long userId = getUserId(uid, parkId);
			insertsql.append(",uid");
			valuesql.append(",?");
			list.add(userId);
		 }
		if(jo.containsKey("order_id")){
			if(!"null".equals(jo.getString("order_id"))){
				 insertsql.append(",order_id_local");
				 valuesql.append(",?");
				 list.add(jo.getString("order_id"));
			 }
		}
		if(jo.containsKey("in_channel_id")){
			if(!"null".equals(jo.getString("in_channel_id"))){
				 insertsql.append(",in_passid");
				 valuesql.append(",?");
				 String cid = getChannelId(jo.getString("in_channel_id"), parkId);
				 if(cid!=null)
					 list.add(cid);
				 else {
					 list.add(jo.getString("in_channel_id"));
				 } 
			 }
		}
	    insertsql.append(",total");
	    valuesql.append(",?");
	    list.add(0.0);
	    //入场订单状态为未支付
		 insertsql.append(",state");
		 valuesql.append(",?");
		 list.add(0);
		
		 if(jo.containsKey("out_channel_id")){
			 if(!"".equals(jo.getString("out_channel_id"))){
				 insertsql.append(",out_passid");
				 valuesql.append(",?");
				 list.add(jo.getString("out_channel_id"));
			 }
		 }
		 if(jo.containsKey("type")){
			 if(!"".equals(jo.getString("type"))){
				 insertsql.append(",type");
				 valuesql.append(",?");
				 list.add(0);//type   服务器本地化订单  
			 }
		 }
		 
		 insertsql.append(") values ("+valuesql+")");
		 int insert = daService.update(insertsql.toString(), list.toArray());
		 logger.error(">>>>>>>>>>>>>>>>>车辆入场订单入库sql语句："+insertsql.toString()+":"+list);
		 logger.error("本地生成订单插入ret:"+insert+",orderid:"+nextid);
		 JSONObject jsonObj = new JSONObject();
		 jsonObj.put("state", insert);
		 if(jo.containsKey("order_id")){
			 jsonObj.put("order_id",jo.getString("order_id"));
		 }
		 jsonObj.put("service_name", "in_park");
		 jsonObj.put("errmsg", "");
		 try {
			if(jo.containsKey("empty_plot")){
				logger.error("更新余位："+ daService.update("update com_info_tb set empty=? where id = ? ", new Object[]{jo.getInt("empty_plot"),Long.valueOf(parkId)}));
			 }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObj.toString();
	}

	@Override
	public String exitPark(String comid, String data) {
		//定义返回对象
		JSONObject jsonObj = new JSONObject();
		//定义返回值
		String result="";
		Long parkId = -1L;
		String orderIdRet="";
		//判断必须上传的参数是否存在
		JSONObject json = JSONObject.fromObject(data);
		if(json.containsKey("car_number") && json.containsKey("in_time") 
				&& json.containsKey("uid") && json.containsKey("order_id")
				&& json.containsKey("out_time") && json.containsKey("total")){
			parkId = Long.valueOf(comid);
			//判断车辆出场时对应的订单信息是否已经存在，存在则更新，不存在则添加新的订单
			JSONObject dataObj = JSONObject.fromObject(data);
			String carNumber = "";
			if(dataObj.containsKey("car_number")){
				carNumber = AjaxUtil.decodeUTF8(dataObj.getString("car_number"));
			}
			Long createTime = -1L;
			if(dataObj.containsKey("in_time")){
				createTime = dataObj.getLong("in_time");
			}
			String orderId = dataObj.getString("order_id");
			orderIdRet =orderId;
			if(orderId != null && carNumber != null){
				Map count =  daService.getMap("select id,uin from order_tb where car_number=? and order_id_local = ? and comid = ? and state=? ", new Object[]{carNumber,orderId,parkId,1});
				if(count!=null && count.size()>0){
					//完整订单上传时存在原来的订单号则在写入之前进行删除操作
					logger.error(">>>>>>>>>>>>>complete order error,订单已存在 ，删除订单号："+orderId+"车场编号park_id:"+parkId);
				}
				//完整订单上传时重新写入一张出场订单,并把订单编号对应的原来的订单删除
				logger.error(">>>>>>>>>>>>update complete order add a order start");
				result = addOrderExit(parkId,data);
				logger.error(">>>>>>>>>>>>>>添加完整订单结果： add order result:"+result);
			}
		}else{
			logger.error("errmsg:data异常，未找到必须字段（car_number,in_time,out_time,total,uid,order_id）值。");
			result = "{\"state\":0,\"service_name\":\"upload_order\",\"order_id\":\""+orderIdRet+"\",\"errmsg\":\"上传数据中没找到必需的字段（car_number,in_time,out_time,total,uid,order_id）值!\"}";
		}
		
//		jsonObj.put("result", result);
//		return jsonObj.toString();
		return result;
	}
	
	@Override
	public String outPark(String comid, String data) {
		//定义返回对象
		JSONObject jsonObj = new JSONObject();
		//定义返回值
		String result="";
		Long parkId = -1L;
		String orderIdRet ="";
		//判断必须上传的参数是否存在
		JSONObject json = JSONObject.fromObject(data);
		if(json.containsKey("car_number") && json.containsKey("in_time") 
				&& json.containsKey("uid") && json.containsKey("order_id")
				&& json.containsKey("out_time") && json.containsKey("total")){
			parkId = Long.valueOf(comid);
			//判断车辆出场时对应的订单信息是否已经存在，存在则更新，不存在则添加新的订单
			JSONObject dataObj = JSONObject.fromObject(data);
			String carNumber = "";
			if(dataObj.containsKey("car_number")){
				carNumber = AjaxUtil.decodeUTF8(dataObj.getString("car_number"));
			}
			Long createTime = -1L;
			if(dataObj.containsKey("in_time")){
				createTime = dataObj.getLong("in_time");
			}
			String orderId = dataObj.getString("order_id");
			orderIdRet = orderId;
			if(orderId != null && carNumber != null){
				Map count =  daService.getMap("select * from order_tb where order_id_local = ? and comid = ? and state=? ", new Object[]{orderId,parkId,0});
				if(count!=null && count.size()>0){
					//如果已经存在则对库表里的订单信息进行更新操作
					logger.error("update complete order error,存在未结算订单，更新订单 ");
					result = updateOrder(parkId,data);
					logger.error(">>>>>>>>>>>>update order result :更新订单结果："+result);
				}
				else{
					//重新写入一张出场订单之前，先判断订单是否重复
					Map countNew =  daService.getMap("select * from order_tb where order_id_local = ? and comid = ? and create_time=? ", new Object[]{orderId,parkId,createTime});
					if(countNew != null && countNew.size()>0){
						logger.error(">>>>>>>>>>>>>>>>>>>>>>>>发现相同时间入场订单，执行更新订单操作"+parkId+"订单信息："+data);
						result = updateOrder(parkId,data);
						logger.error(">>>>>>>>>>>>>>>>>>>update order result:更新订单结果："+result);
					}else{
						logger.error(">>>>>>>>>>>>>>>>>>>>>>>>未找到入场订单，重新写入一条完整出场订单"+parkId+"订单信息："+data);
						result = addOrderOutPark(parkId,data);
						logger.error(">>>>>>>>>>>>>>>>>>>add order result:未找到入场订单，写入完整订单结果："+result);
					}
				}
			}
		}else{
			logger.error("error:data异常，未找到必须字段（car_number,in_time,out_time,total,uid,order_id）值。");
			result = "{\"state\":0,\"service_name\":\"out_park\",\"order_id\":\""+orderIdRet+"\",\"errmsg\":\"上传数据中没找到必需的字段（car_number,in_time,out_time,total,uid,order_id）值!\"}";
		}
		
//		jsonObj.put("result", result);
//		return jsonObj.toString();
		return result;
	}
	
	/**
	 * 调用车辆出场接口新添加订单方法
	 * @param parkId
	 * @param data
	 * @return
	 */
	private String addOrderOutPark(Long parkId, String data) {
		//对传入数据进行utf-8解码操作
		data = StringUtils.decodeUTF8(data);
		JSONObject jo = JSONObject.fromObject(data);
		Long nextid = daService.getLong(
				"SELECT nextval('seq_order_tb'::REGCLASS) AS newid", null);
		StringBuffer insertsql = new StringBuffer("insert into order_tb(id,comid,uin");
		StringBuffer valuesql = new StringBuffer("?,?,?");
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(nextid);
		list.add(parkId);
		Long uin = -1L;
		String carNumber="";
		if(jo.containsKey("car_number")){
			 if(!"null".equals(jo.getString("car_number"))){
				 insertsql.append(",car_number");
				 valuesql.append(",?");
				 carNumber = jo.getString("car_number");
				 list.add(carNumber);
			 }
		}
		
		Long createTime = -1L;
		System.out.println(" add one order:"+data);
		if(jo.containsKey("in_time")){
			if(!"null".equals(jo.getLong("in_time"))){
				insertsql.append(",create_time");
				valuesql.append(",?");
				createTime = jo.getLong("in_time");
				list.add(createTime);
			}
		}
		
		if(createTime!=null && carNumber!=null){
			//根据车牌号获取车场会员信息
			Map u = daService.getMap("select uin from car_info_tb where car_number=?", new Object[]{carNumber});
			if(u==null||u.isEmpty()){
				u = daService.getMap("select uin from wxp_user_tb where car_number=?", new Object[]{carNumber});
			}
			if(u!=null && u.get("uin")!=null){
				uin = Long.valueOf(u.get("uin")+"");
			}
		}
		list.add(2,uin);
		//车辆类型值处理
		if(jo.containsKey("car_type")){
			 if(!"null".equals(jo.getString("car_type"))){
				 insertsql.append(",car_type");
				 valuesql.append(",?");
				 list.add(jo.getString("car_type"));
			 }
		}
		//车辆入场类型处理
		if(jo.containsKey("c_type")){
			 if(!"null".equals(jo.getString("c_type"))){
				 Object object = jo.get("c_type");
				 if(object instanceof String){
					 insertsql.append(",c_type");
					 valuesql.append(",?");
					 list.add(jo.getString("c_type"));
				 }
			 }
		}
		if(jo.containsKey("uid")){
			String uid = jo.getString("uid");
			Long userId = getUserId(uid, parkId);
			insertsql.append(",uid");
			valuesql.append(",?");
			list.add(userId);
		 }
		if(jo.containsKey("out_uid")){
			String outUid = jo.getString("out_uid");
			Long userId = getUserId(outUid, parkId);
			insertsql.append(",out_uid");
			valuesql.append(",?");
			list.add(userId);
		 }
		if(jo.containsKey("order_id")){
			if(!"null".equals(jo.getString("order_id"))){
				 insertsql.append(",order_id_local");
				 valuesql.append(",?");
				 list.add(jo.getString("order_id"));
			 }
		}
		if(jo.containsKey("in_channel_id")){
			if(!"null".equals(jo.getString("in_channel_id"))){
				 insertsql.append(",in_passid");
				 valuesql.append(",?");
				 String cid = getChannelId(jo.getString("in_channel_id"), parkId);
				 if(cid!=null)
					 list.add(cid);
				 else {
					 list.add(jo.getString("in_channel_id"));
				 } 
			 }
		}
		//最后实收金额
		double total = 0.0;
		if(jo.containsKey("total")){
			if(!"".equals(jo.getString("total")) && !(jo.getString("total")).equals("null")){
				total = StringUtils.formatDouble(jo.getString("total"));
			}
		}
		insertsql.append(",total");
		valuesql.append(",?");
		list.add(total);
		
		if(jo.containsKey("out_time")){
			 String endTime = jo.getString("out_time");
			 if(!"null".equals(jo.getString("out_time"))&&!"0".equals(jo.getString("out_time"))){
				 insertsql.append(",end_time");
				 valuesql.append(",?");
				 list.add(jo.getLong("out_time"));
			 }
		 }
		
		 if(jo.containsKey("duration")){
			 if(!"null".equals(jo.getString("duration"))){
				 insertsql.append(",duration");
				 valuesql.append(",?");
				 list.add(jo.getLong("duration"));
			 }
		 }
		 
		 if(jo.containsKey("auto_pay")){
			 if(!"null".equals(jo.getString("auto_pay"))){
				 insertsql.append("auto_pay");
				 valuesql.append(",?");
				 list.add(jo.getLong("auto_pay"));
			 }
		 }
		 
		 if(jo.containsKey("pay_type")){
			 if(!"null".equals(jo.getString("pay_type"))){
				 insertsql.append(",pay_type");
				 valuesql.append(",?");
				 //根据接口协议添加支付类型,非现金支付都记录为手机扫码
				 if("cash".equals(jo.getString("pay_type"))){
					 list.add(1);
				 }else if("monthuser".equals(jo.getString("pay_type"))){
					 list.add(3);
				 }else if("wallet".equals(jo.getString("pay_type"))||"sweepcode".equals(jo.getString("pay_type"))){
					 list.add(2);
				}else {
					 list.add(8);
				}
			 }
		 }
		 if(jo.containsKey("nfc_uuid")){
			 if(!"null".equals(jo.getString("nfc_uuid"))){
				 insertsql.append(",nfc_uuid");
				 valuesql.append(",?");
				 list.add(jo.getString("nfc_uuid"));
			 }
		 }
		 if(jo.containsKey("imei")){
			 if(!"null".equals(jo.getString("imei"))){
				 insertsql.append(",imei");
				 valuesql.append(",?");
				 list.add(jo.getString("imei"));
			 }
		 }
		 if(jo.containsKey("pid")){
			 if(!"null".equals(jo.getString("pid"))){
				 insertsql.append(",pid");
				 valuesql.append(",?");
				 list.add(jo.getString("pid"));
			 }
		 }
		 if(jo.containsKey("pre_state")){
			 if(!"null".equals(jo.getString("pre_state"))){
				 insertsql.append(",pre_state");
				 valuesql.append(",?");
				 list.add(jo.getString("pre_state"));
			 }
		 }
		 if(jo.containsKey("out_channel_id")){
			 if(!"null".equals(jo.getString("out_channel_id"))){
				 insertsql.append(",out_passid");
				 valuesql.append(",?");
				 list.add(jo.getString("out_channel_id"));
			 }
		 }
		 if(jo.containsKey("type")){
			 if(!"null".equals(jo.getString("type"))){
				 insertsql.append(",type");
				 valuesql.append(",?");
				 list.add(0);//type   服务器本地化订单  
			 }
		 }
		 if(jo.containsKey("need_sync")){
			 if(!"null".equals(jo.getString("need_sync"))){
				 insertsql.append(",need_sync");
				 valuesql.append(",?");
				 list.add(2);//need_sync   2  本地化订单
			 }
		 }
		 if(jo.containsKey("freereasons")){
			 insertsql.append(",freereasons");
			 valuesql.append(",?");
			 if("null".equals(jo.getString("freereasons"))&&!"".equals(jo.getString("freereasons"))){
				 list.add(jo.getString("freereasons"));
			 }else{
				 list.add("");
			 }
		 }
		 //应收金额
		 double amountReceivable = 0.0;
		 if(jo.containsKey("amount_receivable")){
			 if(!"null".equals(jo.getString("amount_receivable")) && !"".equals(jo.getString("amount_receivable"))){
				 amountReceivable = StringUtils.formatDouble(jo.getString("amount_receivable"));
			 }
		 }
		 insertsql.append(",amount_receivable");
		 valuesql.append(",?");
		 list.add(amountReceivable);
		 
		 //减免金额
		 double reduceAmount = 0.0;
		 if(jo.containsKey("reduce_amount")){
			 if(!"null".equals(jo.getString("reduce_amount")) && !"".equals(jo.getString("reduce_amount"))){
				 reduceAmount = StringUtils.formatDouble(jo.getString("reduce_amount"));
			 }
		 }
		 insertsql.append(",reduce_amount");
		 valuesql.append(",?");
		 list.add(reduceAmount);
		 
		 //电子预付金额
		 double electronicPrepay = 0.0;
		 if(jo.containsKey("electronic_prepay")){
			 if(!"null".equals(jo.getString("electronic_prepay")) && !"".equals(jo.getString("electronic_prepay"))){
				 electronicPrepay = StringUtils.formatDouble(jo.getString("electronic_prepay"));
			 }
		 }
		 insertsql.append(",electronic_prepay");
		 valuesql.append(",?");
		 list.add(electronicPrepay);
		 
		 //电子支付金额
		 double electronicPay = 0.0;
		 if(jo.containsKey("electronic_pay")){
			 if(!"null".equals(jo.getString("electronic_pay")) && !"".equals(jo.getString("electronic_pay"))){
				 electronicPay = StringUtils.formatDouble(jo.getString("electronic_pay"));
			 }
		 }
		 insertsql.append(",electronic_pay");
		 valuesql.append(",?");
		 list.add(electronicPay);
		 
		 //现金预付金额
		 double cashPrepay = 0.0;
		 if(jo.containsKey("cash_prepay")){
			 if(!"null".equals(jo.getString("cash_prepay")) && !"".equals(jo.getString("cash_prepay"))){
				 cashPrepay = StringUtils.formatDouble(jo.getString("cash_prepay"));
			 }
		 }
		 insertsql.append(",cash_prepay");
		 valuesql.append(",?");
		 list.add(cashPrepay);
		 
		 //现金支付金额
		 double cashPay = 0.0;
		 if(jo.containsKey("cash_pay")){
			 if(!"null".equals(jo.getString("cash_pay")) && !"".equals(jo.getString("cash_pay"))){
				 cashPay = StringUtils.formatDouble(jo.getString("cash_pay"));
			 }
		 }
		 insertsql.append(",cash_pay");
		 valuesql.append(",?");
		 list.add(cashPay);
		 
		 if(jo.containsKey("islocked")){
			 if(!"null".equals(jo.getString("islocked"))){
				 insertsql.append(",islocked");
				 valuesql.append(",?");
				 list.add(jo.getInt("islocked"));
			 }
		 }
		 if(jo.containsKey("lock_key")){
			 if(!"null".equals(jo.getString("lock_key"))){
				 insertsql.append(",lock_key");
				 valuesql.append(",?");
				 list.add(jo.getInt("lock_key"));
			 }
		 }
		 if(jo.containsKey("remark")){
			 if(!"null".equals(jo.getString("remark"))){
				 insertsql.append(",remark");
				 valuesql.append(",?");
				 list.add(jo.getString("remark"));
			 }
		 }
		 insertsql.append(",state");
		 valuesql.append(",?");
		 list.add(1);
		 insertsql.append(") values ("+valuesql+")");
		 int insert = daService.update(insertsql.toString(), list.toArray());
		 System.out.println(insertsql.toString()+":"+list);
		 logger.error(">>>>>>>>>>>>>>>>>>生成一条出场订单："+insertsql.toString()+":"+list);
		 logger.error("生成一条出场订单插入ret:"+insert+",orderid:"+nextid);
		 JSONObject jsonObj = new JSONObject();
		 jsonObj.put("state", insert);
		 if(jo.containsKey("order_id")){
			 jsonObj.put("order_id",jo.getString("order_id"));
		 }
		 jsonObj.put("service_name", "out_park");
		 try {
			if(jo.containsKey("empty_plot")){
				logger.error("更新余位："+ daService.update("update com_info_tb set empty=? where id = ? ", new Object[]{jo.getInt("empty_plot"),Long.valueOf(parkId)}));
			 }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObj.toString();
	}

	/**
	 * 车辆出场添加订单
	 * @param parkId
	 * @param data
	 * @return
	 */
	private String addOrderExit(Long parkId, String data) {
		//对传入数据进行utf-8解码操作
		data = StringUtils.decodeUTF8(data);
		JSONObject jo = JSONObject.fromObject(data);
		Long nextid = daService.getLong(
				"SELECT nextval('seq_order_tb'::REGCLASS) AS newid", null);
		StringBuffer insertsql = new StringBuffer("insert into order_tb(id,comid,uin");
		StringBuffer valuesql = new StringBuffer("?,?,?");
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(nextid);
		list.add(parkId);
		Long uin = -1L;
		String carNumber="";
		if(jo.containsKey("car_number")){
			 if(!"null".equals(jo.getString("car_number"))){
				 insertsql.append(",car_number");
				 valuesql.append(",?");
				 carNumber = jo.getString("car_number");
				 list.add(carNumber);
			 }
		}
		
		Long createTime = -1L;
		System.out.println(" add one order:"+data);
		if(jo.containsKey("in_time")){
			if(!"null".equals(jo.getLong("in_time"))){
				insertsql.append(",create_time");
				valuesql.append(",?");
				createTime = jo.getLong("in_time");
				list.add(createTime);
			}
		}
		
		if(createTime!=null && carNumber!=null){
//			Map count =  daService.getMap("select id,uin from order_tb where car_number=?  and comid = ? and state = ?", new Object[]{carNumber,parkId,0});
//			if(count!=null && count.size()>0){
//				//如果线上服务器和本地上传的订单的时间，车牌相同则本地删除
//				int r  = daService.update("delete from order_tb where car_number=?  and comid = ? and state = ?", new Object[]{carNumber,parkId,0});
//				logger.error("车辆已入场，重新入场，删除原订单:"+r);
//			}
			//根据车牌号获取车场会员信息
			Map u = daService.getMap("select uin from car_info_tb where car_number=?", new Object[]{carNumber});
			if(u==null||u.isEmpty()){
				u = daService.getMap("select uin from wxp_user_tb where car_number=?", new Object[]{carNumber});
			}
			if(u!=null && u.get("uin")!=null){
				uin = Long.valueOf(u.get("uin")+"");
			}
		}
		list.add(2,uin);
		//车辆类型值处理
		if(jo.containsKey("car_type")){
			 if(!"null".equals(jo.getString("car_type"))){
				 insertsql.append(",car_type");
				 valuesql.append(",?");
				 list.add(jo.getString("car_type"));
			 }
		}
		//车辆入场类型处理
		if(jo.containsKey("c_type")){
			 if(!"null".equals(jo.getString("c_type"))){
				 Object object = jo.get("c_type");
				 if(object instanceof String){
					 insertsql.append(",c_type");
					 valuesql.append(",?");
					 list.add(jo.getString("c_type"));
				 }
			 }
		}
		if(jo.containsKey("uid")){
			String uid = jo.getString("uid");
			Long userId = getUserId(uid, parkId);
			insertsql.append(",uid");
			valuesql.append(",?");
			list.add(userId);
		 }
		if(jo.containsKey("out_uid")){
			String outUid = jo.getString("out_uid");
			Long userId = getUserId(outUid, parkId);
			insertsql.append(",out_uid");
			valuesql.append(",?");
			list.add(userId);
		 }
		if(jo.containsKey("order_id")){
			if(!"null".equals(jo.getString("order_id"))){
				 insertsql.append(",order_id_local");
				 valuesql.append(",?");
				 list.add(jo.getString("order_id"));
			 }
		}
		if(jo.containsKey("in_channel_id")){
			if(!"null".equals(jo.getString("in_channel_id"))){
				 insertsql.append(",in_passid");
				 valuesql.append(",?");
				 String cid = getChannelId(jo.getString("in_channel_id"), parkId);
				 if(cid!=null)
					 list.add(cid);
				 else {
					 list.add(jo.getString("in_channel_id"));
				 } 
			 }
		}
		
		//最后实收金额
		double total = 0.0;
		if(jo.containsKey("total")){
			if(!"".equals(jo.getString("total")) && !(jo.getString("total")).equals("null")){
				total = StringUtils.formatDouble(jo.getString("total"));
			}
		}
		insertsql.append(",total");
		valuesql.append(",?");
		list.add(total);
		
		 if(jo.containsKey("out_time")){
			 String endTime = jo.getString("out_time");
			 if(!"null".equals(jo.getString("out_time"))&&!"0".equals(jo.getString("out_time"))){
				 insertsql.append(",end_time");
				 valuesql.append(",?");
				 list.add(jo.getLong("out_time"));
			 }
		 }
		 if(jo.containsKey("duration")){
			 if(!"null".equals(jo.getString("duration"))){
				 insertsql.append(",duration");
				 valuesql.append(",?");
				 list.add(jo.getLong("duration"));
			 }
		 }
		 if(jo.containsKey("auto_pay")){
			 if(!"null".equals(jo.getString("auto_pay"))){
				 insertsql.append("auto_pay");
				 valuesql.append(",?");
				 list.add(jo.getLong("auto_pay"));
			 }
		 }
		 if(jo.containsKey("pay_type")){
			 if(!"null".equals(jo.getString("pay_type"))){
				 insertsql.append(",pay_type");
				 valuesql.append(",?");
				 //根据接口协议添加支付类型,非现金支付都记录为手机扫码
				 if("cash".equals(jo.getString("pay_type"))){
					 list.add(1);
				 }else if("monthuser".equals(jo.getString("pay_type"))){
					 list.add(3);
				 }else if("wallet".equals(jo.getString("pay_type"))||"sweepcode".equals(jo.getString("pay_type"))){
					 list.add(2);
				}else {
					 list.add(8);
				}
			 }
		 }
		 if(jo.containsKey("nfc_uuid")){
			 if(!"null".equals(jo.getString("nfc_uuid"))){
				 insertsql.append(",nfc_uuid");
				 valuesql.append(",?");
				 list.add(jo.getString("nfc_uuid"));
			 }
		 }
		 if(jo.containsKey("imei")){
			 if(!"null".equals(jo.getString("imei"))){
				 insertsql.append(",imei");
				 valuesql.append(",?");
				 list.add(jo.getString("imei"));
			 }
		 }
		 if(jo.containsKey("pid")){
			 if(!"null".equals(jo.getString("pid"))){
				 insertsql.append(",pid");
				 valuesql.append(",?");
				 list.add(jo.getString("pid"));
			 }
		 }
		 if(jo.containsKey("pre_state")){
			 if(!"null".equals(jo.getString("pre_state"))){
				 insertsql.append(",pre_state");
				 valuesql.append(",?");
				 list.add(jo.getString("pre_state"));
			 }
		 }
		 if(jo.containsKey("out_channel_id")){
			 if(!"null".equals(jo.getString("out_channel_id"))){
				 insertsql.append(",out_passid");
				 valuesql.append(",?");
				 list.add(jo.getString("out_channel_id"));
			 }
		 }
		 if(jo.containsKey("type")){
			 if(!"null".equals(jo.getString("type"))){
				 insertsql.append(",type");
				 valuesql.append(",?");
				 list.add(0);//type   服务器本地化订单  
			 }
		 }
		 if(jo.containsKey("need_sync")){
			 if(!"null".equals(jo.getString("need_sync"))){
				 insertsql.append(",need_sync");
				 valuesql.append(",?");
				 list.add(2);//need_sync   2  本地化订单
			 }
		 }
		 if(jo.containsKey("freereasons")){
			 insertsql.append(",freereasons");
			 valuesql.append(",?");
			 if("null".equals(jo.getString("freereasons"))&&!"".equals(jo.getString("freereasons"))){
				 list.add(jo.getString("freereasons"));
			 }else{
				 list.add("");
			 }
		 }
		 
		 //应收金额
		 double amountReceivable = 0.0;
		 if(jo.containsKey("amount_receivable")){
			 if(!"null".equals(jo.getString("amount_receivable")) && !"".equals(jo.getString("amount_receivable"))){
				 amountReceivable = StringUtils.formatDouble(jo.getString("amount_receivable"));
			 }
		 }
		 insertsql.append(",amount_receivable");
		 valuesql.append(",?");
		 list.add(amountReceivable);
		 
		 //减免金额
		 double reduceAmount = 0.0;
		 if(jo.containsKey("reduce_amount")){
			 if(!"null".equals(jo.getString("reduce_amount")) && !"".equals(jo.getString("reduce_amount"))){
				 reduceAmount = StringUtils.formatDouble(jo.getString("reduce_amount"));
			 }
		 }
		 insertsql.append(",reduce_amount");
		 valuesql.append(",?");
		 list.add(reduceAmount);
		 
		 //电子预付金额
		 double electronicPrepay = 0.0;
		 if(jo.containsKey("electronic_prepay")){
			 if(!"null".equals(jo.getString("electronic_prepay")) && !"".equals(jo.getString("electronic_prepay"))){
				 electronicPrepay = StringUtils.formatDouble(jo.getString("electronic_prepay"));
			 }
		 }
		 insertsql.append(",electronic_prepay");
		 valuesql.append(",?");
		 list.add(electronicPrepay);
		 
		 //电子支付金额
		 double electronicPay = 0.0;
		 if(jo.containsKey("electronic_pay")){
			 if(!"null".equals(jo.getString("electronic_pay")) && !"".equals(jo.getString("electronic_pay"))){
				 electronicPay = StringUtils.formatDouble(jo.getString("electronic_pay"));
			 }
		 }
		 insertsql.append(",electronic_pay");
		 valuesql.append(",?");
		 list.add(electronicPay);
		 
		 //现金预付金额
		 double cashPrepay = 0.0;
		 if(jo.containsKey("cash_prepay")){
			 if(!"null".equals(jo.getString("cash_prepay")) && !"".equals(jo.getString("cash_prepay"))){
				 cashPrepay = StringUtils.formatDouble(jo.getString("cash_prepay"));
			 }
		 }
		 insertsql.append(",cash_prepay");
		 valuesql.append(",?");
		 list.add(cashPrepay);
		 
		 //现金支付金额
		 double cashPay = 0.0;
		 if(jo.containsKey("cash_pay")){
			 if(!"null".equals(jo.getString("cash_pay")) && !"".equals(jo.getString("cash_pay"))){
				 cashPay = StringUtils.formatDouble(jo.getString("cash_pay"));
			 }
		 }
		 insertsql.append(",cash_pay");
		 valuesql.append(",?");
		 list.add(cashPay);
		 
		 if(jo.containsKey("islocked")){
			 if(!"null".equals(jo.getString("islocked"))){
				 insertsql.append(",islocked");
				 valuesql.append(",?");
				 list.add(jo.getInt("islocked"));
			 }
		 }
		 if(jo.containsKey("lock_key")){
			 if(!"null".equals(jo.getString("lock_key"))){
				 insertsql.append(",lock_key");
				 valuesql.append(",?");
				 list.add(jo.getInt("lock_key"));
			 }
		 }
		 if(jo.containsKey("remark")){
			 if(!"null".equals(jo.getString("remark"))){
				 insertsql.append(",remark");
				 valuesql.append(",?");
				 list.add(jo.getString("remark"));
			 }
		 }
		 insertsql.append(",state");
		 valuesql.append(",?");
		 list.add(1);
		 insertsql.append(") values ("+valuesql+")");
		 /**
		  * 在完整订单添加之前查询是否已经存在该订单编号对应的订单，
		  * 存在则删除，不存在则添加
		  */
		 String orderIdNew = jo.getString("order_id");
		 //查询该订单是否存在
		 //根据车牌号获取车场会员信息
		 Map isOrder = daService.getMap("select * from order_tb where order_id_local=? and comid=?", new Object[]{orderIdNew,parkId});
		 if(isOrder!=null && !isOrder.isEmpty()){
			int delret = daService.update("delete  from order_tb where comid=? and order_id_local=? and create_time=? ", new Object[]{parkId,orderIdNew,createTime});
			logger.error(">>>>>>>>>>>>>>>>>上传完整订单，删除之前订单,comid:"+parkId+"order_id:"+orderIdNew+"执行结果："+delret);
		 }
		 int insert = daService.update(insertsql.toString(), list.toArray());
		 System.out.println(insertsql.toString()+":"+list);
		 logger.error("本地生成订单插入ret:"+insert+",orderid:"+nextid);
		 JSONObject jsonObj = new JSONObject();
		 jsonObj.put("state", insert);
		 if(jo.containsKey("order_id")){
			 jsonObj.put("order_id",jo.getString("order_id"));
		 }
		 jsonObj.put("service_name", "upload_order");
		 try {
			if(jo.containsKey("empty_plot")){
				logger.error("更新余位："+ daService.update("update com_info_tb set empty=? where id = ? ", new Object[]{jo.getInt("empty_plot"),Long.valueOf(parkId)}));
			 }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObj.toString();
	}

	/**
	 * 车辆出场时修改订单
	 * @param order
	 * @return
	 */
	private String updateOrder(Long parkId, String data) {
		JSONObject jo = JSONObject.fromObject(data);
		StringBuffer insertsql = new StringBuffer("update order_tb set");
		ArrayList<Object> list = new ArrayList<Object>();
		Long createtime = null;
		String carnumber = null;
		if(jo.containsKey("car_number")){
			if (!"".equals(jo.getString("car_number"))) {
				insertsql.append(" car_number=?,");
				list.add(jo.getString("car_number"));
				carnumber = AjaxUtil.decodeUTF8(jo.getString("car_number"));
			}
		}
		if(jo.containsKey("in_time")){
			if (!"".equals(jo.getString("in_time"))) {
				createtime = jo.getLong("in_time");
				insertsql.append(" create_time=?,");
				list.add(createtime);
			}
		}
		if(jo.containsKey("out_time")){
			if (!"".equals(jo.getString("out_time"))) {
				insertsql.append(" end_time=?,");
				list.add(jo.getLong("out_time"));
			}
		}
		if(jo.containsKey("uid")){
			String uid = jo.getString("uid");
			Long userId = getUserId(uid, parkId);
			insertsql.append(" uid=?,");
			list.add(userId);
		}
		if(jo.containsKey("out_uid")){
			String uid = jo.getString("out_uid");
			Long userId = getUserId(uid, parkId);
			insertsql.append(" out_uid=?,");
			list.add(userId);
		}
		if(jo.containsKey("duration")){
			if (!"".equals(jo.getString("duration"))) {
				insertsql.append(" duration=?,");
				list.add(jo.getLong("duration"));
			}
		}
		if(jo.containsKey("car_type")){
			if (!"".equals(jo.getString("car_type"))) {
				insertsql.append(" car_type = ?,");
				list.add(jo.getString("car_type"));
			}
		}
		if(jo.containsKey("c_type")){
			if (!"".equals(jo.getString("c_type"))) {
				insertsql.append(" c_type=?,");
				list.add(jo.getString("c_type"));
			}
		}
		if(jo.containsKey("pay_type")){
			if(!"".equals(jo.getString("pay_type"))){
				 insertsql.append("pay_type=?,");
				 String pay_type = jo.getString("pay_type");
				 //根据接口协议添加支付类型,非现金支付都记录为手机扫码
				 if("cash".equals(pay_type)){
					 list.add(1);
				 }else if("monthuser".equals(pay_type)){
					 list.add(3);
				 }else if("wallet".equals(pay_type)||"sweepcode".equals(pay_type)){
					 list.add(2);
				}else {
					 list.add(8);
				}
			 }
		}
		double reduceAmount = 0.0;//减免金额
		if(jo.containsKey("reduce_amount")){
			if (!"".equals(jo.getString("reduce_amount"))) {
				reduceAmount=jo.getDouble("reduce_amount");
			}
		}
		insertsql.append(" reduce_amount=?,");
		list.add(reduceAmount);
		
		double electronic_prepay = 0.0;//电子预付
		if(jo.containsKey("electronic_prepay")){
			if (!"".equals(jo.getString("electronic_prepay"))) {
				electronic_prepay=jo.getDouble("electronic_prepay");
			}
		}
		insertsql.append(" electronic_prepay=?,");
		list.add(electronic_prepay);
		double electronic_pay = 0.0;//电子结算
		if(jo.containsKey("electronic_pay")){
			if (!"".equals(jo.getString("electronic_pay"))) {
				electronic_pay=jo.getDouble("electronic_pay");
			}
		}
		insertsql.append(" electronic_pay=?,");
		list.add(electronic_pay);
		double cash_prepay = 0.0;//现金预付
		if(jo.containsKey("cash_prepay")){
			if (!"".equals(jo.getString("cash_prepay"))) {
				cash_prepay=jo.getDouble("cash_prepay");
			}
		}
		insertsql.append(" cash_prepay=?,");
		list.add(cash_prepay);
		double cash_pay = 0.0;//现金结算
		if(jo.containsKey("cash_pay")){
			if (!"".equals(jo.getString("cash_pay"))) {
				cash_pay=jo.getDouble("cash_pay");
			}
		}
		insertsql.append(" cash_pay=?,");
		list.add(cash_pay);
		double amount_receivable = 0.0;//应收金额
		if(jo.containsKey("amount_receivable")){
			if (!"".equals(jo.getString("amount_receivable"))) {
				amount_receivable=jo.getDouble("amount_receivable");
			}
		}
		double total =0.0;
		if(jo.containsKey("total")){
			if (!"".equals(jo.getString("total"))) {
				total =jo.getDouble("total");
			}
		}
		insertsql.append(" total=?,");
		list.add(total);
		
		insertsql.append(" amount_receivable=?,");
		list.add(amount_receivable);
		
		if(jo.containsKey("freereasons")){
			if (!"".equals(jo.getString("freereasons"))
					&& !"".equals(jo.getString("freereasons"))) {
				insertsql.append(" freereasons=?,");
				list.add(jo.getString("freereasons"));
			}
		}
		//如果该车主是车场会员则将会员编号更新到表中
		long uin = -1;
		Map u = daService.getMap(
				"select uin from car_info_tb where car_number=?",
				new Object[] { carnumber });
		if (u != null && u.get("uin") != null) {
			uin = Long.valueOf(u.get("uin") + "");
			insertsql.append(" uin=?,");
			list.add(uin);
		}
		
		if(jo.containsKey("out_channel_id")){
			if(!"".equals(jo.getString("out_channel_id"))){
				insertsql.append(" out_passid=?,");
				 String cid = getChannelId(jo.getString("out_channel_id"), parkId);
				 if(cid!=null)
					 list.add(cid);
				 else {
					 list.add(jo.getString("out_channel_id"));
				 }
			}
		}
		if(jo.containsKey("in_channel_id")){
			if(!"".equals(jo.getString("in_channel_id"))){
				insertsql.append(" in_passid=?,");
				 String cid = getChannelId(jo.getString("in_channel_id"), parkId);
				 if(cid!=null)
					 list.add(cid);
				 else {
					 list.add(jo.getString("in_channel_id"));
				 }
			}
		}
		
		//开源接口对sql语句进行处理
		if(",".equals(insertsql.substring(insertsql.length()-1,insertsql.length()))){
			String insertsqlStr = insertsql.substring(0,insertsql.length()-1);
			insertsql = new StringBuffer(insertsqlStr);
		}
		insertsql.append(",state=? ");
		list.add(1);
		String sql = insertsql + " where comid = ? and order_id_local =? and create_time=? ";
		list.add(parkId);
		list.add(jo.getString("order_id"));
		list.add(createtime);
		logger.error("sql:"+sql+",params:"+list);
		int update = daService.update(sql, list.toArray());
		//System.out.println(sql);
		logger.error("result:"+update);
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("state", update);
		jsonObj.put("order_id", jo.getString("order_id"));
		jsonObj.put("service_name", "out_park");
		jsonObj.put("errmsg", "");
		 try {
			if(jo.containsKey("empty_plot")){
					logger.error("更新余位："+ daService.update("update com_info_tb set empty=? where id = ? ", new Object[]{jo.getInt("empty_plot"),Long.valueOf(parkId)}));
				 }
			} catch (Exception e) {
				e.printStackTrace();
			}
		return jsonObj.toString();
	}

	@Override
	public String uploadMonthCard(String comid, String data) {
		String logStr = "tcp uploadMonthCard to cloud";
		// 定义返回值对象
		JSONObject jsonResult = new JSONObject();
		int update=0;
		JSONObject jsonObj = JSONObject.fromObject(data);
		String packageIdRet = "";
		if(jsonObj.containsKey("package_id")){
			packageIdRet = jsonObj.getString("package_id");
		}
		int operateTypeRet = 0;
		if(jsonObj.containsKey("operate_type")){
			operateTypeRet = jsonObj.getInt("operate_type");
		}
		//判断是否存在必传字段
		if(jsonObj.containsKey("package_id") && jsonObj.containsKey("price") 
				&& jsonObj.containsKey("operate_type")){
			//开始进行逻辑操作
			Long parkId = Long.valueOf(comid);
			String cardId = jsonObj.getString("package_id");
			
			Double price = jsonObj.getDouble("price");
			int operateType = jsonObj.getInt("operate_type");
			operateTypeRet = operateType;
			if(operateType == 1){
				Long count = daService.getLong("select count(*) from product_package_tb where card_id=? and comid=?", new Object[]{cardId,parkId});
				if(count>=1){
					operateType = 2;
				}
			}
			if(operateType == 2){
				//已经存在月卡记录做更新操作
				StringBuffer updatesql = new StringBuffer();
				List valuesql = new ArrayList();
				updatesql.append("update product_package_tb set price=?,operate_type=?,is_sync=?");
				valuesql.add(price);
				valuesql.add(2);
				valuesql.add(1);
				if(jsonObj.containsKey("create_time")){
					Long create_time = jsonObj.getLong("create_time");
					updatesql.append(",create_time=?");
					valuesql.add(create_time);
				}
				if(jsonObj.containsKey("update_time")){
					Long update_time = jsonObj.getLong("update_time");
					updatesql.append(",update_time=?");
					valuesql.add(update_time);
				}
				if(jsonObj.containsKey("name")){
					String name = jsonObj.getString("name");
					updatesql.append(",p_name=?");
					valuesql.add(name);
				}
				if(jsonObj.containsKey("describe")){
					String describe = jsonObj.getString("describe");
					updatesql.append(",describe=?");
					valuesql.add(describe);
				}
				updatesql.append(" where comid=? and card_id=?");
				valuesql.add(parkId);
				valuesql.add(cardId);
				update = daService.update(updatesql.toString(), valuesql);
			}else if(operateType == 1){
				//进行添加操作
				Long id = daService.getLong(
								"SELECT nextval('seq_product_package_tb'::REGCLASS) AS newid",
								null);
				StringBuffer updatesql = new StringBuffer();
				StringBuffer valuesql = new StringBuffer();
				List listsql = new ArrayList();
				updatesql.append("insert into product_package_tb (id,comid,card_id,price,operate_type,is_sync");
				valuesql.append("?,?,?,?,?,?");
				listsql.add(id);
				listsql.add(parkId);
				listsql.add(cardId);
				listsql.add(price);
				listsql.add(1);
				listsql.add(1);
				if(jsonObj.containsKey("create_time")){
					Long create_time = jsonObj.getLong("create_time");
					updatesql.append(",create_time");
					valuesql.append(",?");
					listsql.add(create_time);
				}
				if(jsonObj.containsKey("update_time")){
					Long update_time = jsonObj.getLong("update_time");
					updatesql.append(",update_time");
					valuesql.append(",?");
					listsql.add(update_time);
				}
				if(jsonObj.containsKey("describe")){
					String describe = jsonObj.getString("describe");
					updatesql.append(",describe");
					valuesql.append(",?");
					listsql.add(describe);
				}
				if(jsonObj.containsKey("name")){
					String name = jsonObj.getString("name");
					updatesql.append(",p_name");
					valuesql.append(",?");
					listsql.add(name);
				}
				updatesql.append(" )values("+valuesql+")");
				update = daService.update(updatesql.toString(), listsql);
			}else if(operateType==3){
				//对product_package_tb表进行删除操作
				String sql = "delete from product_package_tb where comid=? and card_id=?";
				update = daService.update(sql, new Object[]{parkId,cardId});
			}
			jsonResult.put("state", update);
			jsonResult.put("package_id", cardId);
			jsonResult.put("service_name", "upload_month_card");
			jsonResult.put("operate_type", operateType);
			jsonResult.put("errmsg", "");
		}else{
			logger.error(logStr + "error:data异常，未找到必须字段（package_id,price,operate_type）值。");
			String jsonError = "{\"state\":0,\"service_name\":\"upload_month_card\",\"operate_type\":"+operateTypeRet+",\"package_id\":\""+packageIdRet+"\",\"error\":\"上传数据中没找到必须的字段（package_id,price,operate_type）值!\"}";
			return jsonError;
		}
		return jsonResult.toString();
	}



	@Override
	public String uploadPrice(String parkId, String data) {
		String logStr = "tcp uploadPrice to cloud";
		// 定义返回值对象
		JSONObject jsonResult = new JSONObject();
		int update=0;
		JSONObject jsonObj = JSONObject.fromObject(data);
		int operateTypeRet = 0;
		if(jsonObj.containsKey("operate_type")){
			operateTypeRet = jsonObj.getInt("operate_type");
		}
		String priceIdRet= "";
		if(jsonObj.containsKey("price_id")){
			priceIdRet = jsonObj.getString("price_id");
		}
		//判断是否存在必传字段
		if(jsonObj.containsKey("price_id") && jsonObj.containsKey("car_type") 
				&& jsonObj.containsKey("operate_type")){
			//开始进行逻辑操作
			String priceId = jsonObj.getString("price_id");
			String carType = jsonObj.getString("car_type");
			Long comId = Long.valueOf(parkId);
			int operateType = jsonObj.getInt("operate_type");
			if(operateType == 2){
				//已经存在价格记录做更新操作
				StringBuffer updatesql = new StringBuffer();
				List valuesql = new ArrayList();
				updatesql.append("update price_tb set car_type_zh=?,operate_type,is_sync");
				valuesql.add(carType);
				valuesql.add(2);
				valuesql.add(1);
				if(jsonObj.containsKey("create_time")){
					Long create_time = jsonObj.getLong("create_time");
					updatesql.append(",create_time=?");
					valuesql.add(create_time);
				}
				if(jsonObj.containsKey("update_time")){
					Long update_time = jsonObj.getLong("update_time");
					updatesql.append(",update_time=?");
					valuesql.add(update_time);
				}
				if(jsonObj.containsKey("describe")){
					String describe = jsonObj.getString("describe");
					updatesql.append(",describe=?");
					valuesql.add(describe);
				}
				if(Check.isNumber(carType)){
					updatesql.append(",car_type=?");
					valuesql.add(Integer.valueOf(carType));
				}
				updatesql.append(" where comid=? and price_id=?");
				valuesql.add(comId);
				valuesql.add(priceId);
				update = daService.update(updatesql.toString(), valuesql);
			}else if(operateType == 1){
				//进行添加操作
				Long id = daService.getLong(
								"SELECT nextval('seq_price_tb'::REGCLASS) AS newid",
								null);
				StringBuffer updatesql = new StringBuffer();
				StringBuffer valuesql = new StringBuffer();
				List listsql = new ArrayList();
				updatesql.append("insert into price_tb (id,comid,price_id,car_type_zh,operate_type,is_sync");
				valuesql.append("?,?,?,?,?,?");
				listsql.add(id);
				listsql.add(comId);
				listsql.add(priceId);
				listsql.add(carType);
				listsql.add(1);
				listsql.add(1);
				if(jsonObj.containsKey("create_time")){
					Long create_time = jsonObj.getLong("create_time");
					updatesql.append(",create_time");
					valuesql.append(",?");
					listsql.add(create_time);
				}
				if(jsonObj.containsKey("update_time")){
					Long update_time = jsonObj.getLong("update_time");
					updatesql.append(",update_time");
					valuesql.append(",?");
					listsql.add(update_time);
				}
				if(jsonObj.containsKey("describe")){
					String describe = jsonObj.getString("describe");
					updatesql.append(",describe");
					valuesql.append(",?");
					listsql.add(describe);
				}
				if(Check.isNumber(carType)){
					updatesql.append(",car_type");
					valuesql.append(",?");
					listsql.add(Integer.valueOf(carType));
				}
				updatesql.append(" )values("+valuesql+")");
				update = daService.update(updatesql.toString(), listsql);
			}else if(operateType == 3){
				//对price_tb表进行删除操作
				String sql = "delete from price_tb where comid=? and price_id=?";
				update = daService.update(sql, new Object[]{comId,priceId});
			}
			jsonResult.put("state", update);
			jsonResult.put("price_id", priceId);
			jsonResult.put("service_name", "upload_price");
			jsonResult.put("operate_type", operateType);
			jsonResult.put("errmsg", "");
			jsonResult.put("park_id", comId);
		}else{
			logger.error(logStr + "error:data异常，未找到必须字段（price_id,car_type,operate_type）值。");
			String jsonError = "{\"state\":0,\"service_name\":\"upload_price\",\"operate_type\":"+operateTypeRet+",\"price_id\":\""+priceIdRet+"\",\"error\":\"上传数据中没找到必须的字段（package_id,price,operate_type）值!\"}";
			return jsonError;
		}
		return jsonResult.toString();
	}

	@Override
	public String uploadCollector(String parkId, String data) {
		JSONObject jsonObject = JSONObject.fromObject(data);
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("service_name", "upload_collector");
		int ret = 0;
		if(jsonObject.containsKey("operate_type")){
			int operate = jsonObject.getInt("operate_type");
			retMap.put("operate_type", operate);
			if(jsonObject.containsKey("user_id")){
				String userId = jsonObject.getString("user_id");
				retMap.put("user_id", userId);
				String name = "";
				if(jsonObject.containsKey("name"))
					name = jsonObject.getString("name");
				Long createTime  = System.currentTimeMillis()/1000;
				if(jsonObject.containsKey("create_time"))
					createTime = jsonObject.getLong("create_time");
				Long updateTime  = System.currentTimeMillis()/1000;
				if(jsonObject.containsKey("update_time"))
					updateTime = jsonObject.getLong("update_time");
				Integer sex = -1;
				if(jsonObject.containsKey("sex"))
					sex = jsonObject.getInt("sex");
				if(operate==1){
					ret = daService.update("insert into user_info_tb " +
							"(nickname,user_id,sex,reg_time,update_time,comid,auth_flag,state,role_id)" +
							" values(?,?,?,?,?,?,?,?,?)", new Object[]{name,userId,sex,createTime,updateTime,Long.valueOf(parkId),1,0,30});
					logger.error(">>>>>>>>>>>>>>>上传车场收费员信息执行添加操作结果："+ret+"收费员编号："+userId);
					
					
				}else if(operate==2){
					String sql = "update user_info_tb set  ";
					List<Object> values = new ArrayList<Object>();
					if(!"".equals(name)){
						sql +="nickname=? ";
						values.add(name);
					}
					if(createTime!=null){
						if(!values.isEmpty())
							sql +=",";
						sql +=" reg_time=? ";
						values.add(createTime);
					}
					if(updateTime!=null){
						if(!values.isEmpty())
							sql +=",";
						sql +=" update_time=? ";
						values.add(updateTime);
					}	
					if(sex!=null){
						if(!values.isEmpty())
							sql +=",";
						sql +=" sex=? ";
						values.add(sex);
					}
					if(!values.isEmpty()){
						sql +=" where user_id=? ";
						values.add(userId);
						ret = daService.update(sql,values.toArray());
						logger.error(">>>>>>>>>>>更新车场收费员信息执行结果："+ret+"收费员编号："+userId);
					}
				}else if(operate==3){
					ret = daService.update("update user_info_tb set state =? where user_id=? ", new Object[]{1,userId});
					logger.error(">>>>>>>>>>>禁用车场收费员信息执行结果："+ret+"收费员编号："+userId);
				}
			}
		}
		retMap.put("state", ret);
		return StringUtils.createJson(retMap);
	}

	@Override
	public String priceSyncAfter(Long comid, String priceId,Integer state,Integer operate) {
		Map<String,Object> productPackageMap = daService.getMap("select * from price_tb where id=? ", new Object[]{Long.valueOf(priceId)});
		JSONObject jsonObj = new JSONObject();
		if(productPackageMap !=null && !productPackageMap.isEmpty()){
			Long id = (Long)productPackageMap.get("id");
			String sql = "update sync_info_pool_tb set state=? where table_name=? " +
					"and table_id=? and operate=? ";
			int ret = daService.update(sql, new Object[]{state,"price_tb",id,operate-1});
			jsonObj.put("state", ret);
			jsonObj.put("table_name", "price_tb");
		}
		return jsonObj.toString();
	}



	@Override
	public String packageSyncAfter(Long comid, String productPackageId, Integer state,Integer operate) {
		Map<String,Object> productPackageMap = daService.getMap("select * from product_package_tb where id=? ", new Object[]{Long.valueOf(productPackageId)});
		JSONObject jsonObj = new JSONObject();
		if(productPackageMap !=null && !productPackageMap.isEmpty()){
			Long id = (Long)productPackageMap.get("id");
			String sql = "update sync_info_pool_tb set state=? where table_name=? " +
					"and table_id=? and operate=? ";
			int ret = daService.update(sql, new Object[]{state,"product_package_tb",id,operate-1});
			jsonObj.put("state", ret);
			jsonObj.put("table_name", "product_package_tb");
		}
		return jsonObj.toString();
	}



	@Override
	public String userInfoSyncAfter(Long comid, String cardId,Integer state,Integer operate) {
		Map<String,Object> carowerProductMap = daService.getMap("select id from carower_product where card_id=? ", new Object[]{cardId});
		JSONObject jsonObj = new JSONObject();
		if(carowerProductMap!=null&&!carowerProductMap.isEmpty()){
			Long id = (Long)carowerProductMap.get("id");
			String sql = "update sync_info_pool_tb set state=? where table_name=? " +
					"and table_id=? and operate=? ";
			int ret = daService.update(sql, new Object[]{state,"carower_product",id,operate-1});
			jsonObj.put("state", ret);
			jsonObj.put("table_name", "carower_product");
		}
		return jsonObj.toString();
	}
	
	@Override
	public void logout(String parkId ){
		String pid = parkId;
		String localId  = null;
		if(parkId.indexOf("_")!=-1){
			pid = parkId.split("_")[0];
			localId = parkId.substring(pid.length()+1);
		}
		String sql = "delete from park_token_tb where park_id= ? ";
		Object[] values = new Object[]{parkId};
		if(localId!=null){
			sql = sql +" and local_id=? ";
			values = new Object[]{pid,localId};
		}
		int r =daService.update(sql,values);
		logger.error("退出登录,"+parkId+",ret:"+r);
	}

	@Override
	public void lockCar(String jsonData) {
		JSONObject jsonObject = JSONObject.fromObject(jsonData);
		String orderId = null;
		Integer opState = null;
		if(jsonObject.containsKey("order_id")){
			orderId = jsonObject.getString("order_id");
		}
		Integer state = null;
		if(jsonObject.containsKey("state")){
			state = jsonObject.getInt("state");
		}
		Integer isLocked = null;
		if(jsonObject.containsKey("is_locked")){
			isLocked = jsonObject.getInt("is_locked");//0解锁，1加锁
		}
		if(isLocked!=null){
			if(isLocked==0){
				if(state!=null&&state==1)
					opState = 0;//解锁成功
				else {
					opState = 5;//解锁失败
				}
			}else {
				if(isLocked==1){
					if(state!=null&&state==1)
						opState = 1;//加锁成功
					else {
						opState = 3;//加锁失败
					}
				}
			}
		}
		if(opState!=null&&orderId!=null){
			String sql = "update order_tb set islocked=? where order_id_local=? ";
			int r = daService.update(sql, new Object[]{opState,orderId});
			logger.error("用户锁车,orderID:"+orderId+"state:"+opState+",ret:"+r);
		}else {
			logger.error("锁车业务数据错误 ："+jsonData);
		}
	}

	@Override
	public String checkTokenSign(String token, String preSign,String data) {
		String result="";
		Map<String, Object> parkInfoMap = daService.getMap("select * from park_token_tb where token=? ",
				new Object[] { token });
		if (parkInfoMap != null && !parkInfoMap.isEmpty()) {
			String parkId =(String)parkInfoMap.get("park_id");
			if(parkId!=null&&Check.isLong(parkId)){
				result  = parkId;
				Map<String, Object> comInfoMap = daService.getMap("select * from com_info_tb where id=?",
						new Object[] { Long.valueOf(parkId)});
				if (comInfoMap != null && !comInfoMap.isEmpty()) {
					String ukey = String.valueOf(comInfoMap.get("ukey"));
					String strKey = data+ "key="+ ukey;
					try {
						String sign = StringUtils.MD5(strKey,"utf-8").toUpperCase();
						logger.error(strKey + "," + sign + ":" + preSign
								+ ",ret:" + sign.equals(preSign));
//						if (!sign.equals(preSign)) {
//							result = "error:签名错误";
//						}
					} catch (Exception e) {
						e.printStackTrace();
						result = "error:md5加密出现异常,请联系后台管理员！！！";
					}
				}else{
					result = "error:车场编号异常，未成功在云平台注册";
				}
			}else {
				result = "error:车场没有登录";
			}
		}else{
			result = "error:车场初始化失败,token不存在";
		}
		logger.error("token,sign验证结果："+result);
		return result;
	}

	public Long getUserId (String userId,Long comId){
		Long uid = -1L;
		if(uid!=null){
			Map<String, Object> userMap = daService.getMap("select id from user_info_tb " +
					"where strid=? and comid=? ", new Object[]{userId,comId});
			if(userMap!=null&&!userMap.isEmpty()){
				uid = (Long)userMap.get("id");
			}else {
				if(Check.isLong(userId))
					uid = Long.valueOf(userId);
			}
		}
		return uid;
	}
	
	public String getChannelId (String channelId,Long comId){
		String cid = null;
		if(channelId!=null){
			Map<String, Object> userMap = daService.getMap("select channel_id from com_pass_tb " +
					"where passname=? and comid=? ", new Object[]{channelId,comId});
			if(userMap!=null&&!userMap.isEmpty()){
				cid = (String)userMap.get("channel_id");
			}
		}
		return cid;
	}

	@Override
	public String uploadCarpic(String parkId, String data) {
		JSONObject jsonObj = JSONObject.fromObject(data);
		Long createTime = -1L;
		String pictureSource = "";
		int ret = 0;
		if(jsonObj.containsKey("picture_source") && jsonObj.getString("picture_source") != null){
			pictureSource = jsonObj.getString("picture_source");
		}else{
			return "{\"state\":0,\"errmsg\":\"图片上传失败，缺少必填字段picture_source！\"}";
		}
		if(jsonObj.containsKey("create_time") && jsonObj.getString("create_time")!= null){
			createTime = jsonObj.getLong("create_time");
		}else{
			createTime = System.currentTimeMillis()/1000;
		}
		//定义返回数据json对象
		JSONObject json = new JSONObject();
		//判断当前上传图片的操作类型
		if(pictureSource.equals("order")){
			//定义订单编号orderId
			String orderId = "";
			if(jsonObj.containsKey("order_id") && jsonObj.getString("order_id") != null){
				orderId = jsonObj.getString("order_id");
			}else{
				return "{\"state\":0,\"errmsg\":\"图片上传失败，上传车场订单图片时，缺少必填字段order_id！\"}";
			}
			//定义订单图片类型parkOrderType,in入场订单图片，out出场图片类型
			String parkOrderType = "";
			if(jsonObj.containsKey("park_order_type") && jsonObj.getString("park_order_type") != null){
				parkOrderType = jsonObj.getString("park_order_type");
			}else{
				return "{\"state\":0,\"errmsg\":\"图片上传失败，上传车场订单图片时，缺少必填字段park_order_type！\"}";
			}
			//根据orderId判断是否存在该图片如果存在则更新数据库，不存在则添加
			Long count = daService.getLong("select count(*) from carpic_tb where order_id=? and comid=? and park_order_type=?", new Object[]{orderId,parkId,parkOrderType});
			if(count>=1){
				//该条图片记录已经存在做更新操作
				String sql = "update carpic_tb set update_time=?";
				List<Object> values = new ArrayList<Object>();
				Long updateTime = -1L;
				if(jsonObj.containsKey("update_time")){
					updateTime = jsonObj.getLong("updateTime");
				}else{
					updateTime = System.currentTimeMillis()/1000;
				}
				values.add(updateTime);
				if(jsonObj.containsKey("content")){
					String  content = jsonObj.getString("content");
					sql +=",content=? ";
					values.add(content);
				}
				if(jsonObj.containsKey("car_number")){
					String  carNumber = jsonObj.getString("car_number");
					sql +=",car_number=? ";
					values.add(carNumber);
				}
				if(jsonObj.containsKey("pic_type")){
					String  picType = jsonObj.getString("pic_type");
					sql +=",pic_type=? ";
					values.add(picType);
				}
				if(jsonObj.containsKey("resume")){
					String  resume = jsonObj.getString("resume");
					sql +=",resume=? ";
					values.add(resume);
				}
				if(!values.isEmpty()){
					sql +=" where order_id=? and comid=? and park_order_type=?";
					values.add(orderId);
					values.add(parkId);
					values.add(parkOrderType);
					ret = daService.update(sql,values.toArray());
				}
			}else{
				//新的订单图片记录做添加操作
				//进行添加操作
				Long id = daService.getLong(
								"SELECT nextval('seq_carpic_tb'::REGCLASS) AS newid",
								null);
				StringBuffer updatesql = new StringBuffer();
				StringBuffer valuesql = new StringBuffer();
				List listsql = new ArrayList();
				updatesql.append("insert into carpic_tb (id,picture_source,comid,order_id,create_time,park_order_type");
				valuesql.append("?,?,?,?,?,?");
				listsql.add(id);
				listsql.add(pictureSource);
				listsql.add(parkId);
				listsql.add(orderId);
				listsql.add(createTime);
				listsql.add(parkOrderType);
				if(jsonObj.containsKey("content")){
					String content = jsonObj.getString("content");
					updatesql.append(",content");
					valuesql.append(",?");
					listsql.add(content);
				}
				if(jsonObj.containsKey("car_number")){
					String carNumber = jsonObj.getString("car_number");
					updatesql.append(",car_number");
					valuesql.append(",?");
					listsql.add(carNumber);
				}
				if(jsonObj.containsKey("pic_type")){
					String picType = jsonObj.getString("pic_type");
					updatesql.append(",pic_type");
					valuesql.append(",?");
					listsql.add(picType);
				}
				if(jsonObj.containsKey("resume")){
					String resume = jsonObj.getString("resume");
					updatesql.append(",resume");
					valuesql.append(",?");
					listsql.add(resume);
				}
				updatesql.append(" )values("+valuesql+")");
				ret = daService.update(updatesql.toString(), listsql);
			}
			json.put("state", ret);
			json.put("order_id", orderId);
			json.put("park_order_type", parkOrderType);
			json.put("service_name", "upload_carpic");
			json.put("errmsg", " ");
		}else if(pictureSource.equals("liftrod")){
			//定义抬杆记录编号liftrodId
			String liftrodId = "";
			if(jsonObj.containsKey("liftrod_id") && jsonObj.getString("liftrod_id") != null){
				liftrodId = jsonObj.getString("liftrod_id");
			}else{
				return "{\"state\":0,\"errmsg\":\"图片上传失败，上传车场抬杆图片时，缺少必填字段liftrod_id！\"}";
			}
			//根据liftrodId判断是否存在该图片如果存在则更新数据库，不存在则添加
			Long count = daService.getLong("select count(*) from carpic_tb where liftrod_id=? and comid=?", new Object[]{liftrodId,parkId});
			if(count>=1){
				//该条图片记录已经存在做更新操作
				String sql = "update carpic_tb set update_time=?";
				List<Object> values = new ArrayList<Object>();
				Long updateTime = -1L;
				if(jsonObj.containsKey("update_time")){
					updateTime = jsonObj.getLong("updateTime");
				}else{
					updateTime = System.currentTimeMillis()/1000;
				}
				values.add(updateTime);
				if(jsonObj.containsKey("content")){
					String  content = jsonObj.getString("content");
					sql +=",content=? ";
					values.add(content);
				}
				if(jsonObj.containsKey("car_number")){
					String  carNumber = jsonObj.getString("car_number");
					sql +=",car_number=? ";
					values.add(carNumber);
				}
				if(jsonObj.containsKey("pic_type")){
					String  picType = jsonObj.getString("pic_type");
					sql +=",pic_type=? ";
					values.add(picType);
				}
				if(jsonObj.containsKey("resume")){
					String  resume = jsonObj.getString("resume");
					sql +=",resume=? ";
					values.add(resume);
				}
				if(!values.isEmpty()){
					sql +=" where liftrod_id=? and comid=?";
					values.add(liftrodId);
					values.add(parkId);
					ret = daService.update(sql,values.toArray());
				}
			}else{
				//新的图片记录做添加操作
				//进行添加操作
				Long id = daService.getLong(
								"SELECT nextval('seq_carpic_tb'::REGCLASS) AS newid",
								null);
				StringBuffer updatesql = new StringBuffer();
				StringBuffer valuesql = new StringBuffer();
				List listsql = new ArrayList();
				updatesql.append("insert into carpic_tb (id,comid,picture_source,liftrod_id,create_time");
				valuesql.append("?,?,?,?,?");
				listsql.add(id);
				listsql.add(parkId);
				listsql.add("liftrod");
				listsql.add(liftrodId);
				listsql.add(createTime);
				if(jsonObj.containsKey("content")){
					String content = jsonObj.getString("content");
					updatesql.append(",content");
					valuesql.append(",?");
					listsql.add(content);
				}
				if(jsonObj.containsKey("car_number")){
					String carNumber = jsonObj.getString("car_number");
					updatesql.append(",car_number");
					valuesql.append(",?");
					listsql.add(carNumber);
				}
				if(jsonObj.containsKey("pic_type")){
					String picType = jsonObj.getString("pic_type");
					updatesql.append(",pic_type");
					valuesql.append(",?");
					listsql.add(picType);
				}
				if(jsonObj.containsKey("resume")){
					String resume = jsonObj.getString("resume");
					updatesql.append(",resume");
					valuesql.append(",?");
					listsql.add(resume);
				}
				updatesql.append(" )values("+valuesql+")");
				ret = daService.update(updatesql.toString(), listsql);
			}
			json.put("state", ret);
			json.put("liftrod_id", liftrodId);
			json.put("service_name", "upload_carpic");
			json.put("errmsg", " ");
		}
		return json.toString();
	}

	@Override
	public String monthPayRecord(String parkId, String data) {
		JSONObject jsonObj = JSONObject.fromObject(data);
		Long createTime = -1L;
		String tradeNo = "";
		int ret = 0;
		if(jsonObj.containsKey("trade_no") && jsonObj.getString("trade_no") != null){
			tradeNo = jsonObj.getString("trade_no");
		}
		if(jsonObj.containsKey("create_time") && jsonObj.getString("create_time")!= null){
			createTime = jsonObj.getLong("create_time");
		}else{
			createTime = System.currentTimeMillis()/1000;
		}
		//根据orderId判断是否存在月卡续费记录如果存在则更新数据库，不存在则添加
		Long count = daService.getLong("select count(*) from card_renew_tb where trade_no=? and comid=?", new Object[]{tradeNo,parkId});
		if(count>=1){
			//该条月卡缴费记录已经存在做更新操作
			String sql = "update card_renew_tb set update_time=? ";
			List<Object> values = new ArrayList<Object>();
			Long updateTime = -1L;
			if(jsonObj.containsKey("update_time")){
				updateTime = jsonObj.getLong("updateTime");
			}else{
				updateTime = System.currentTimeMillis()/1000;
			}
			values.add(updateTime);
			if(jsonObj.containsKey("card_id")){
				String  cardId = jsonObj.getString("card_id");
				sql +=",card_id=? ";
				values.add(cardId);
			}
			if(jsonObj.containsKey("pay_time")){
				Long  payTime = jsonObj.getLong("pay_time");
				sql +=",pay_time=? ";
				values.add(payTime);
			}
			if(jsonObj.containsKey("amount_receivable")){
				String  amountReceivable = jsonObj.getString("amount_receivable");
				sql +=",amount_receivable=? ";
				values.add(amountReceivable);
			}
			if(jsonObj.containsKey("amount_pay")){
				String  amountPay = jsonObj.getString("amount_pay");
				sql +=",amount_pay=? ";
				values.add(amountPay);
			}
			if(jsonObj.containsKey("pay_type")){
				String  payType = jsonObj.getString("pay_type");
				sql +=",pay_type=? ";
				values.add(payType);
			}
			if(jsonObj.containsKey("collector")){
				String  collector = jsonObj.getString("collector");
				sql +=",collector=? ";
				values.add(collector);
			}
			if(jsonObj.containsKey("buy_month")){
				int  buyMonth = jsonObj.getInt("buy_month");
				sql +=",buy_month=? ";
				values.add(buyMonth);
			}
			if(jsonObj.containsKey("car_number")){
				String  carNumber = jsonObj.getString("car_number");
				sql +=",car_number=? ";
				values.add(carNumber);
			}
			if(jsonObj.containsKey("user_id")){
				String  userId = jsonObj.getString("user_id");
				sql +=",user_id=? ";
				values.add(userId);
			}
			if(jsonObj.containsKey("resume")){
				String  resume = jsonObj.getString("resume");
				sql +=",resume=? ";
				values.add(resume);
			}
			if(!values.isEmpty()){
				sql +=" where trade_no=? and comid=?";
				values.add(tradeNo);
				values.add(parkId);
				ret = daService.update(sql,values.toArray());
			}
		}else{
			//新的月卡续费记录做添加操作
			Long id = daService.getLong(
							"SELECT nextval('seq_card_renew_tb'::REGCLASS) AS newid",
							null);
			StringBuffer updatesql = new StringBuffer();
			StringBuffer valuesql = new StringBuffer();
			List listsql = new ArrayList();
			updatesql.append("insert into card_renew_tb (id,comid,trade_no,create_time");
			valuesql.append("?,?,?,?");
			listsql.add(id);
			listsql.add(parkId);
			listsql.add(tradeNo);
			listsql.add(createTime);
			if(jsonObj.containsKey("card_id")){
				String cardId = jsonObj.getString("card_id");
				updatesql.append(",card_id");
				valuesql.append(",?");
				listsql.add(cardId);
			}
			if(jsonObj.containsKey("pay_time")){
				Long payTime = jsonObj.getLong("pay_time");
				updatesql.append(",pay_time");
				valuesql.append(",?");
				listsql.add(payTime);
			}
			if(jsonObj.containsKey("amount_receivable")){
				String amountReceivable = jsonObj.getString("amount_receivable");
				updatesql.append(",amount_receivable");
				valuesql.append(",?");
				listsql.add(amountReceivable);
			}
			if(jsonObj.containsKey("amount_pay")){
				String amountPay = jsonObj.getString("amount_pay");
				updatesql.append(",amount_pay");
				valuesql.append(",?");
				listsql.add(amountPay);
			}
			if(jsonObj.containsKey("pay_type")){
				String payType = jsonObj.getString("pay_type");
				updatesql.append(",pay_type");
				valuesql.append(",?");
				listsql.add(payType);
			}
			if(jsonObj.containsKey("collector")){
				String collector = jsonObj.getString("collector");
				updatesql.append(",collector");
				valuesql.append(",?");
				listsql.add(collector);
			}
			if(jsonObj.containsKey("buy_month")){
				int buyMonth = jsonObj.getInt("buy_month");
				updatesql.append(",buy_month");
				valuesql.append(",?");
				listsql.add(buyMonth);
			}
			if(jsonObj.containsKey("car_number")){
				String carNumber = jsonObj.getString("car_number");
				updatesql.append(",car_number");
				valuesql.append(",?");
				listsql.add(carNumber);
			}if(jsonObj.containsKey("user_id")){
				String userId = jsonObj.getString("user_id");
				updatesql.append(",user_id");
				valuesql.append(",?");
				listsql.add(userId);
			}
			if(jsonObj.containsKey("resume")){
				String resume = jsonObj.getString("resume");
				updatesql.append(",resume");
				valuesql.append(",?");
				listsql.add(resume);
			}
			updatesql.append(" )values("+valuesql+")");
			ret = daService.update(updatesql.toString(), listsql);
		}
		JSONObject json = new JSONObject();
		json.put("state", ret);
		json.put("trade_no", tradeNo);
		json.put("service_name", "month_pay_record");
		json.put("errmsg", "");
		return json.toString();
	}

	@Override
	public String queryProdprice(String parkId, String data) {
		JSONObject jsonObj = JSONObject.fromObject(data);
		String tradeno = jsonObj.getString("trade_no");
		String price = jsonObj.getString("price");
		boolean f = memcacheUtils.setCache(tradeno, price, 10*60);
		String result = "";
		if(f){
			result = "{\"state\":1,\"errmsg\":\" \"}";
		}else{
			result = "{\"state\":0,\"errmsg\":\"价格存入缓存出错\"}";
		}
		return result;
	}

	@Override
	public void operateLiftrod(Long comid,String channelId,Integer state,Integer operate) {
		Map mapLiftrod = daService.getMap("select * from liftrod_info_tb where channel_id=? and operate=? and comid=?", 
				new Object[]{channelId,operate,String.valueOf(comid)});
		if(mapLiftrod != null && !mapLiftrod.isEmpty()){
			int update = daService.update("update liftrod_info_tb set state=? where channel_id=? and operate=? and comid=? ",
					new Object[]{channelId,operate,String.valueOf(comid)});
			logger.error(">>>>>>>>>>>修改通知抬杆信息入库结果："+update);
		}else{
			Long id = daService.getkey("seq_liftrod_info_tb");
			int update = daService.update("insert into liftrod_info_tb(id,channel_id,state,operate,comid)values(?,?,?,?,?)", 
					new Object[]{id,channelId,state,operate,String.valueOf(comid)});
			logger.error(">>>>>>>>>>>添加通知抬杆信息入库结果："+update);
		}
	}
	
}
