package parkingos.com.bolink.netty;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import parkingos.com.bolink.utlis.StringUtils;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
public class TcpHandle {
	Logger logger = Logger.getLogger(TcpHandle.class);

	/**
	 * 写入订单
	 * @param jsonData
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Object> addOrder(JSONObject jsonData, Long unionId, String localId) throws Exception{
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("state",0);
		retMap.put("order_id",jsonData.getString("order_id"));
		retMap.put("errmsg", "上传失败");
		if(jsonData!=null){
			jsonData.put("union_id", unionId);
			jsonData.put("local_id", localId);
			handleHttpsRet(jsonData.toString(), retMap,"inpark");
		}
		return retMap;
	}
	
	private void handleHttpsRet(String jsonData,Map<String, Object> retMap,String action){
		String httpRet= "";

//		if(!action.equals("dobeat"))
//			logger.info("tcp 调用unionapi接口返回数据:"+retMap);
	}
	/**
	 * 结算订单
	 * @param jsonData
	 * @return
	 */
	public Map<String, Object> completeOrder(JSONObject jsonData,Long unionId,String localId){
		Map<String, Object> retMap = new HashMap<String, Object>();
	    String pay_type  = jsonData.containsKey("pay_type")?jsonData.getString("pay_type"):"";
		retMap.put("state",0);
		retMap.put("order_id",jsonData.getString("order_id"));
		retMap.put("errmsg", "结算失败");
		retMap.put("pay_type", pay_type);
		if(jsonData!=null){
			jsonData.put("union_id", unionId);
			jsonData.put("local_id", localId);
			handleHttpsRet(jsonData.toString(), retMap,"outpark");
		}
		return retMap;
	}
	/**
	 * 登录处理
	 * @param jsonObject
	 * @param preSign
	 * @return
	 */
	public String login(JSONObject jsonObject,String preSign,String clientIp,String key) throws Exception{
		String logStr = "tcp park login ";
		logger.info(jsonObject);
		
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("state",0);
		if(jsonObject!=null){
			handleHttpsRet(jsonObject.toString(), retMap,"prelogin");
		}
		//return retMap;
//		String localId = null;
//		if(jsonObject.containsKey("local_id"))
//			localId = jsonObject.getString("local_id");
//		String token = "";
		if(retMap.containsKey("error")){
			return retMap.get("error")+"";
		}
		String strKey = jsonObject.toString()+"key="+retMap.get("ukey");
		String sign = StringUtils.MD5(strKey).toUpperCase();
		logger.info(strKey+","+sign+":"+preSign+",ret:"+sign.equals(preSign));
		if(sign.equals(preSign)){//登录
			retMap.clear();
			jsonObject.put("client_ip", clientIp);
			jsonObject.put("channel_id", key);
			jsonObject.put("server_ip", Inet4Address.getLocalHost().getHostAddress().toString());
			handleHttpsRet(jsonObject.toString(), retMap,"dologin");
		}else {
			logger.error(logStr+"error:车场签名错误");
			return "error:签名错误";
		}
		if(retMap.containsKey("error")){
			return retMap.get("error")+"";
		}else {
			return retMap.get("token")+"";
		}
		
		/*
		String unionId = jsonObject.getString("union_id");
		String parkId = jsonObject.getString("park_id");
		if(Check.isEmpty(parkId)){
			logger.error(logStr+"error:park_id为空");
			return "error:park_id为空";
		}
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("union_id", Long.valueOf(unionId));
		paramMap.put("park_id", parkId);
		//paramMap.put("state", 1);
		Map<String, Object> parkMap = service.getMap(paramMap, "union_park_tb");
		if(parkMap==null||paramMap.isEmpty()){
			logger.error(logStr+"error:车场不存在");
			return "error:车场不存在";
		}else {
			Integer parkState = (Integer)parkMap.get("state");
			if(parkState==null||parkState!=1){
				logger.error(logStr+"error:车场未审核");
				return "error:车场未审核";
			}
		}
		paramMap.clear();
		paramMap.put("id", Long.valueOf(unionId));
		paramMap.put("state", 1);
		Map<String, Object> unionMap = service.getMap(paramMap,"union_platfrom_tb");
		if(unionMap!=null&&!unionMap.isEmpty()){
			String strKey = jsonObject.toString()+"key="+unionMap.get("ukey");
			String sign = StringUtils.MD5(strKey).toUpperCase();
			System.err.println(strKey+","+sign+":"+preSign+",ret:"+sign.equals(preSign));
			if(sign.equals(preSign)){
				token = UUID.randomUUID().toString().replace("-", "");
				paramMap.clear();
				paramMap.put("union_id", Long.valueOf(unionId));
				paramMap.put("park_id", parkId);
				if(!Check.isEmpty(localId))
					paramMap.put("local_id", localId);
				List<Object> paramsList = new ArrayList<Object>();
				paramsList.add(Long.valueOf(unionId));
				paramsList.add(parkId);
				String countSql = "select count(*) from park_token_tb where union_id=? and park_id=? ";
				if(!Check.isEmpty(localId)){
					countSql+=" and local_id=? ";
					paramsList.add(localId);
				}else {
					countSql +=" and local_id is null ";
				}
				//int count = service.getCount(paramMap, "park_token_tb");
				int count = service.getCount(countSql, paramsList);
				int r = 0;
				if(count==0){
					paramMap.put("union_id", Long.valueOf(unionId));
					paramMap.put("park_id", parkId);
					paramMap.put("login_time", System.currentTimeMillis()/1000);
					paramMap.put("token", token);
					paramMap.put("server_ip", Inet4Address.getLocalHost().getHostAddress().toString());
					paramMap.put("source_ip",clientIp);
					if(!Check.isEmpty(localId))
						paramMap.put("local_id",localId);
					paramMap.put("channel_id",key);
					r = service.insertData(paramMap, "park_token_tb");
					logger.info("tcp login park:"+parkId+",uinon:"+unionId+",token"+token+",login result:"+r);
				}else {
					Map<String, Object> fieldMap = new HashMap<String, Object>();
					fieldMap.put("token", token);
					fieldMap.put("login_time", System.currentTimeMillis()/1000);
					fieldMap.put("server_ip", Inet4Address.getLocalHost().getHostAddress().toString());
					fieldMap.put("channel_id", key);
					paramMap.clear();
					if(localId!=null&&!"".equals(localId)){
						paramMap.put("local_id", localId);
					}
					//paramMap.put("local_id",localId);
					paramMap.put("union_id", Long.valueOf(unionId));
					paramMap.put("park_id", parkId);
					Object[] values =  new Object[]{token,System.currentTimeMillis()/1000
							,Inet4Address.getLocalHost().getHostAddress().toString(),key,Long.valueOf(unionId)
							,parkId};
					String sql = "update park_token_tb set token =? ,login_time=?,server_ip=?,channel_id=?" +
							" where union_id=? and park_id =? ";
					if(!Check.isEmpty(localId)){
						sql += " and local_id=? ";
						values = new Object[]{token,System.currentTimeMillis()/1000
									,Inet4Address.getLocalHost().getHostAddress().toString(),key,Long.valueOf(unionId)
									,parkId,localId};
					}else {
						sql += " and local_id is null  ";
					}
					r = service.updateData(sql, values);
					//r = service.updateData(fieldMap,"park_token_tb",paramMap);
					logger.info("tcp login , relogon,park:"+parkId+",uinon:"+unionId+",token"+token+",result:"+r);
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
			logger.error(logStr+"error:账户或密码不正确");
			return "error:账户或密码不正确";
		}*/
	}
	
	public void logout(String key,String clientIp ){
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("state",0);
		JSONObject data = JSONObject.parseObject("");
		data.put("client_ip", clientIp);
		if(data!=null){
			handleHttpsRet(data.toString(), retMap,"logout");
		}
//		//String keys[] = key.split("_");
//		//200100_21768_hui_11
//			int r =service.updateData("delete from park_token_tb where channel_id= ?",
//					new Object[]{key});
//			if(r==1){
//				NettyChannelMap.remove(key);
//			}
		logger.error("退出登录,"+key+",ret:"+retMap);
	}
	public void doBeatUp(String key,String clientIp ){
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("state",0);
		JSONObject data = JSONObject.parseObject("{\"channel_id\":\""+key+"\",\"client_ip\":\""+clientIp+"\"}");
		if(data!=null){
			handleHttpsRet(data.toString(), retMap,"dobeat");
		}
		//logger.info(retMap);
	}
	
	/**
	 * 攻击过滤 
	 * @param token
	 * @return
	 */
	private boolean isHit(String token){
		logger.info("recive data，token:"+token);
		Map<String,List<Long>> tokenAccessMap =NettyChannelMap.tokenAccessMap;
		Long ntime = System.currentTimeMillis()/1000;
		if(tokenAccessMap.containsKey(token)){
			List<Long> requestTimes = tokenAccessMap.get(token);
			//logger.info("tcp recive data，request cache:"+requestTimes);
			if(requestTimes==null){
				requestTimes = new ArrayList<Long>();
				tokenAccessMap.put(token, requestTimes);
			}
			while (requestTimes.size()>100) {
				requestTimes.remove(0);
			}
			//logger.info("tcp recive data，request cache:"+requestTimes);
			tokenAccessMap.put(token, requestTimes);
			if(requestTimes.size()==100){
				Long preRequest = requestTimes.get(0);
				if(preRequest!=null&&ntime-preRequest<11){//10秒内100请求限制
					logger.error("tcp recive data，10秒内超过100次，返回");
					return true;
				}
			}
			requestTimes.add(ntime);
		}else {
			List<Long> requestTimes = new ArrayList<Long>();
			requestTimes.add(ntime);
			tokenAccessMap.put(token, requestTimes);
			logger.info("tcp recive data，request cache:"+requestTimes);
		}
		logger.info("tcp recive data，没有超过10秒100次限制");
		return false;
	}
	
	/**
	 * 验证token、签名
	 * @param sign
	 * @param token
	 * @param jsonData
	 * @return -1：token无效   -2：签名无效    >0 ：正常返回厂商平台账户
	 */
	public Map<String, Object> checkSign(String sign,String token,String jsonData){
		//过滤攻击：
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if(isHit(token)){
			resultMap.put("error", "request_over_times");
			return resultMap;
		}
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("state",0);
		JSONObject data = JSONObject.parseObject("{\"token\":\""+token+"\"}");
		if(data!=null){
			handleHttpsRet(data.toString(), retMap,"checksign");
		}
		if(retMap.containsKey("error")){
			resultMap.put("error",retMap.get("error"));
		}else {
			String signString = jsonData+"key="+retMap.get("ukey");
			logger.info("signstr:"+signString);
			String _sign = StringUtils.MD5(signString).toUpperCase();
			logger.info("presign:"+sign+",sign:"+_sign);
			if(_sign.equals(sign)){
				resultMap.put("local_id", retMap.get("local_id"));
				resultMap.put("union_id",retMap.get("union_id"));
				resultMap.put("park_id", retMap.get("park_id"));
			}else {
				resultMap.put("error", "签名无效");
			}
		}
		/*Map<String,Object> tokenMap = service.getMap("select union_id,local_id,park_id from park_token_tb where token=?", new Object[]{token});
		if(tokenMap==null||tokenMap.isEmpty()){
			resultMap.put("error", "token_error");
		}else {
			Map<String, Object> unionInfoMap = service.getMap("select ukey,id from union_platfrom_tb where id = ?" 
					,new Object[]{tokenMap.get("union_id")});
			logger.info(unionInfoMap);
			if(unionInfoMap!=null&&!unionInfoMap.isEmpty()){
				String signString = jsonData+"key="+unionInfoMap.get("ukey");
				logger.info("signstr:"+signString);
				String _sign = StringUtils.MD5(signString).toUpperCase();
				logger.info("presign:"+sign+",sign:"+_sign);
				if(_sign.equals(sign)){
					resultMap.put("local_id", tokenMap.get("local_id"));
					resultMap.put("union_id",unionInfoMap.get("id"));
					resultMap.put("park_id", tokenMap.get("park_id"));
				}else {
					resultMap.put("error", "签名无效");
				}
			}else {
				resultMap.put("error", "厂商平台信息查不到");
			}
		}*/
		return resultMap;
	}
	/**
	 * 泊链查询订单价格后，车场SDK返回数据处理
	 * @param data
	 * @param unionId
	 */
	public void updateOrderPrice(JSONObject data, Long unionId) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("state",0);
		if(data!=null){
			handleHttpsRet(data.toString(), retMap,"queryprice");
		}
	}
	
	
	/**
	 * 泊链推送预付订单后，车场SDK返回结果处理
	 * @param data
	 * @param unionId
	 */
	public void doPrepayOrderOver(JSONObject data, Long unionId,String parkId) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("state",0);
		if(data!=null){
			handleHttpsRet(data.toString(), retMap,"prepayorder");
		}
	}
	
	/**
	 * 泊链推送月卡续费后，车场SDK返回结果处理
	 * @param data
	 * @author zhangq 2017-5-24 月卡续费
	 */
	public void doMonthCardPayOver(JSONObject data, Long to_unionId) {
		data.put("to_unionid", to_unionId);
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("state",0);
		handleHttpsRet(data.toString(), retMap,"monthcardpay");
	}

	public void doNolicenceInPark(JSONObject data, Long unionId, String parkId) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("state",0);
		data.put("union_id", unionId);
		data.put("park_id", parkId);
		handleHttpsRet(data.toString(), retMap,"nolicenceinpark");
	}
}
