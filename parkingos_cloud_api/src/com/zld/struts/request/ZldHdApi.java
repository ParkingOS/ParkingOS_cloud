package com.zld.struts.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;
import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MemcacheUtils;
import com.zld.impl.MongoClientFactory;
import com.zld.impl.PublicMethods;
import com.zld.pojo.ParseJson;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.Check;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.utils.ZldUploadOperate;
import com.zld.utils.ZldUploadUtils;


/**
 * 设备信息  上行接口
 * @author laoyao
 *
 */
@Path("hdinfo")
public class ZldHdApi {
	
	Logger logger = Logger.getLogger(ZldHdApi.class);
	
	/**
	 * 设备信息
	 * http://127.0.0.1/zld/api/hdinfo/uploadhd
	 */
	@POST
	@Path("/uploadhd")//设备
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void uploadhd(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		if(paramMap.get("park_uuid")!=null){
			String comid =zldUploadOperate.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			paramMap.put("comid", comid);
			paramMap.remove("park_uuid");
		}
		//0标签读写设备,1一卡通读写设备,3车辆检测器,4地感,5手持终端,6车牌识别设备,7安防监控设备,8:通信基站，9诱导屏,10道闸
		//先写入设备表
		Map<String, Object> returnMap = zldUploadOperate.handleData(context,paramMap,params,"com_hd_tb",-1);
		if(returnMap.get("status").equals("1")){
			System.out.println("设备已写入....");
			
			String type = paramMap.get("type");
			if(Check.isNumber(type)){
				Integer t = Integer.valueOf(type);
				switch (t) {
				case 3:
					returnMap.clear();
					paramMap.remove("type");
					returnMap = zldUploadOperate.handleData(context,paramMap,params,"dici_tb",-1);
					break;
				case 6:
					returnMap.clear();
					paramMap.remove("type");
					paramMap.remove("plot_id");
					paramMap.put("upload_time", paramMap.get("operate_time"));
					paramMap.remove("operate_time");
					paramMap.put("passid", "-1");
					returnMap = zldUploadOperate.handleData(context,paramMap,params,"com_camera_tb",-1);
					break;
			   case 8://基站
					returnMap.clear();
					paramMap.remove("type");
					paramMap.remove("plot_id");
					paramMap.put("upload_time", paramMap.get("operate_time"));
					paramMap.remove("operate_time");
					paramMap.put("passid", "-1");
					returnMap = zldUploadOperate.handleData(context,paramMap,params,"sites_tb",-1);
					break;
				case 9:
					returnMap.clear();
					paramMap.remove("type");
					paramMap.remove("plot_id");
					paramMap.put("upload_time", paramMap.get("operate_time"));
					paramMap.remove("operate_time");
					paramMap.put("passid", "-1");
					returnMap = zldUploadOperate.handleData(context,paramMap,params,"com_led_tb",-1);
					break;
				case 10:
					returnMap.clear();
					paramMap.remove("type");
					paramMap.remove("plot_id");
					paramMap.put("upload_time", paramMap.get("operate_time"));
					paramMap.remove("operate_time");
					paramMap.put("passid", "-1");
					returnMap = zldUploadOperate.handleData(context,paramMap,params,"com_brake_tb",-1);
					break;
				default:
					break;
				}
			}
		}
		logger.error(returnMap);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	/**
	 * 1.获取车检器设备心跳/电压、电容列表:
	 * http://127.0.0.1/zld/api/hdinfo/InsertSensor
	 */
	@POST
	@Path("/InsertSensor")//获取车检器设备心跳/电压、电容列表
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void insertSensor(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		logger.error("paramMap:"+paramMap);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		DataBaseService daService = (DataBaseService) ctx.getBean("dataBaseService");
		PgOnlyReadService pService = (PgOnlyReadService) ctx.getBean("pgOnlyReadService");
		CommonMethods commonMethods = (CommonMethods)ctx.getBean("commonMethods");
		Long curTime = System.currentTimeMillis()/1000;
		String sensornumber = paramMap.get("sensornumber");
		double magnetism = 0;
		double battery = 0;
		long siteId = -1L;
		if(paramMap.get("magnetism") != null){
			magnetism = Double.valueOf(paramMap.get("magnetism"));
		}
		if(paramMap.get("battery") != null){
			battery = Double.valueOf(paramMap.get("battery"));
		}
		if(paramMap.get("site_uuid") != null){
			String siteUUID = paramMap.get("site_uuid");
			Map<String, Object> map = pService.getMap("select id from sites_tb where uuid=? and is_delete=? limit ? ",
					new Object[]{siteUUID, 0, 1});
			if(map != null){
				siteId = (Long)map.get("id");
			}
		}
		logger.error("siteId:"+siteId);
		Long count = daService.getLong("select count(id) from dici_tb where did=? and is_delete=? ", 
				new Object[]{sensornumber, 0});
		logger.error("count:"+count);
		if(count == 0){
			int ret = daService.update("insert into dici_tb (did,magnetism,battery,site_id," +
					"operate_time,beart_time) values(?,?,?,?,?,?)", 
					new Object[]{sensornumber, magnetism, battery, siteId, curTime, curTime});
			logger.error("ret:"+ret);
		}else{
			if(battery > 0){//讯朗的车间器每天只传几次电压，其他时候电压值都传0
				int ret = daService.update("update dici_tb set beart_time=?,magnetism=?," +
						"battery=?,site_id=? where did=? ", 
						new Object[]{curTime, magnetism, battery, siteId, sensornumber});
				logger.error("ret:"+ret);
			}else{
				int ret = daService.update("update dici_tb set beart_time=?,magnetism=?," +
						"site_id=? where did=? ", 
						new Object[]{curTime, magnetism, siteId, sensornumber});
				logger.error("ret:"+ret);
			}
		}
		AjaxUtil.ajaxOutput(response, StringUtils.createXML1(paramMap));
		
		commonMethods.deviceRecover(0, sensornumber, curTime);
		Map<String, Object> diciMap = pService.getMap("select comid from dici_tb where did=? and is_delete=? ", 
				new Object[]{sensornumber, 0});
		Integer comid = -1;
		if(diciMap != null && diciMap.get("comid") != null){
			comid = (Integer)diciMap.get("comid");
		}
		paramMap.put("source", "InsertSensor");
		writeToMongodb("zld_hdbeart_logs", paramMap, comid.longValue());
	}
	/**
	 * 获取基站心跳/电压列表
	 * http://127.0.0.1/zld/api/hdinfo/InsertTransmitter
	 */
	@POST
	@Path("/InsertTransmitter")//获取基站心跳/电压列表
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void insertTransmitter(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		//TransmitterNumber
		//VoltageCaution 电压
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		DataBaseService daService = (DataBaseService) ctx.getBean("dataBaseService");
		CommonMethods commonMethods = (CommonMethods)ctx.getBean("commonMethods");
		String sql = "update sites_tb set heartbeat=? where uuid=? ";
		Object []values = new Object[]{System.currentTimeMillis()/1000,paramMap.get("transmitternumber")};
		Double vol = StringUtils.formatDouble(paramMap.get("voltagecaution"));
		if(vol>0){
			sql ="update sites_tb set voltage=?,heartbeat=?,update_time=? where uuid=? ";
			values = new Object[]{Double.valueOf(paramMap.get("voltagecaution")),
					System.currentTimeMillis()/1000,System.currentTimeMillis()/1000,paramMap.get("transmitternumber")};
		}
		int ret = daService.update(sql, values);
		logger.error("InsertTransmitter ret :"+ret+",paramsMap :"+paramMap);
		commonMethods.deviceRecover(1, paramMap.get("transmitternumber"), System.currentTimeMillis()/1000);
		Integer comid=-1;
		if(ret>0){
			Map diciMap = daService.getMap("select comid from dici_tb where did = ? and is_delete=? ",
					new Object[]{paramMap.get("transmitternumber"), 0});
			if(diciMap!=null&&diciMap.get("comid")!=null){
				comid = (Integer)diciMap.get("comid");
			}
		}else{
			Long ntime = System.currentTimeMillis()/1000;
			ret = daService.update("insert into sites_tb (uuid,state,voltage,heartbeat,create_time) values(?,?,?,?,?)", 
					new Object[]{paramMap.get("transmitternumber"),1,Double.valueOf(paramMap.get("voltagecaution")),ntime,ntime});
			logger.error("InsertTransmitter insert ret :"+ret);
		}
		AjaxUtil.ajaxOutput(response, StringUtils.createXML1(paramMap));
		paramMap.put("battery", paramMap.get("voltagecaution"));
		paramMap.remove("voltagecaution");
		paramMap.put("source", "InsertTransmitter");
		writeToMongodb("zld_hdbeart_logs", paramMap,comid.longValue());
		//logger.error("InsertTransmitter write to mongodb result :"+result+",paramsMap :"+paramMap);
		
	}
	
	/**
	 * 通讯数据记录
	 * http://127.0.0.1/zld/api/hdinfo/InsertSensorLog
	 */
	@POST
	@Path("/InsertSensorLog")//通讯数据记录
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void insertSensorLog(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		writeToMongodb("zld_sensordata_logs", paramMap,-1L);
		AjaxUtil.ajaxOutput(response, StringUtils.createXML1(paramMap));
		//Content 通讯内容
		//Exception  是否发生异常
	}
	/**
	 *车检器进场
	 * http://127.0.0.1/zld/api/hdinfo/InsertCarAdmission
	 */
	@POST
	@Path("/InsertCarAdmission")//车检器进场 InsertCarAdmission
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void insertCarAdmission(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		DataBaseService daService = (DataBaseService) ctx.getBean("dataBaseService");
		MemcacheUtils  memcacheUtils=(MemcacheUtils)ctx.getBean("memcacheUtils");
		//CarInTime 进场时间
		//Indicate 配对标示
		//SensorNumber 车检器编号
		//需要写地磁表，在磁订单表，发消息给收费员
		paramMap.put("type", "车检器进场");
		//判断是否已发过消息
		Map<String, String> cMap = new HashMap<String, String>();
		cMap.put("sensornumber", paramMap.get("sensornumber"));
		cMap.put("carintime", AjaxUtil.decodeUTF8(paramMap.get("carintime")));
		Long pcount = isHave("zld_sensor_logs", cMap);
		if(pcount!=null&&pcount>0){
			paramMap.put("error", "数据已处理过！");
			logger.equals("InsertCarAdmission >>>数据已处理过...."+paramMap);
			AjaxUtil.ajaxOutput(response, StringUtils.createXML1(paramMap));
			return;
		}
		//判断是否已发过消息结束
		writeToMongodb("zld_sensor_logs",paramMap,-1L);
		String carIntime = AjaxUtil.decodeUTF8(paramMap.get("carintime"));
		Long intime = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(carIntime);
		String sql = "update dici_tb set state = ? where did =? ";
		int ret =daService.update(sql, new Object[]{1,paramMap.get("sensornumber")});
		logger.error("InsertCarAdmission>>>>>>>车辆进场>>>>>>>>>>InsertCarAdmission "+paramMap+", update dici state:"+ret);
		if(ret<1){//地磁不存在 ，直接返回
			paramMap.put("error", "地磁不存在");
			AjaxUtil.ajaxOutput(response, StringUtils.createXML1(paramMap));
			return;
		}
		Map diciMap = daService.getMap("select id,comid from dici_tb where did = ? and is_delete=? ",
				new Object[]{paramMap.get("sensornumber"), 0});
		Long comId = -1L;
		Long dici_id = -1L;
		if(diciMap!=null&&diciMap.get("comid")!=null){
			dici_id = (Long)diciMap.get("id");
		}
		logger.error("did:"+paramMap.get("sensornumber")+",dici_id:"+dici_id);
		if(dici_id > 0){
			//根据地磁编号查找是否已注册到车位
			Map<String, Object> comParkMap = daService.getMap("select id,berthsec_id,comid,state,order_id from com_park_tb " +
					"where is_delete=? and  dici_id =? ", new Object[]{0,dici_id});
			logger.error("did:"+paramMap.get("sensornumber")+",comParkMap:"+comParkMap+",dici_id:"+dici_id);
			Long bid =-1L;
			if(comParkMap != null && !comParkMap.isEmpty()){//地磁已绑定到车位
				Long pid = (Long)comParkMap.get("id");
				comId = (Long)comParkMap.get("comid");
				Integer state = (Integer)comParkMap.get("state");
				Long uin = -1L;
				sql ="select uid from parkuser_work_record_tb where state=? and start_time>? and  berthsec_id =? ";
				Map userMap = daService.getMap(sql, new Object[]{0,0,comParkMap.get("berthsec_id")});
				logger.error("InsertCarAdmission find user:"+userMap);
				if(userMap!=null&&userMap.get("uid")!=null){//查到了收费员
					uin = (Long)userMap.get("uid");
				}
				//是否已生成过订单
				Long count = daService.getLong("select count(ID) from berth_order_tb where berth_id=? and indicate=? and in_time=? ",
						new Object[]{paramMap.get("sensornumber"),paramMap.get("indicate"),intime});
				logger.error("sensor come in>>>uin:"+uin+",paramMap:"+paramMap+",count:"+count);
				if(count==0){
					Integer bind_flag = 0;//默认不可以绑定POS机订单
					if(state == 0){//当泊位状态是空闲的时候才可以绑定POS机订单
						bind_flag = 1;
					}
					bid = daService.getkey("seq_berth_order_tb");
					sql="insert into berth_order_tb (id,in_time,state,berth_id,indicate,comid,dici_id,in_uid,bind_flag) values(?,?,?,?,?,?,?,?,?)";
					ret = daService.update(sql, new Object[]{bid,intime,0,paramMap.get("sensornumber"),
							paramMap.get("indicate"),comId,comParkMap.get("id"),uin,bind_flag});
					logger.error("InsertCarAdmission insert berth_order_tb state:"+ret);
				}
				logger.error("bid:"+bid+",ret:"+ret);
				//查询地磁对应的泊位对应的泊位段对应的上岗收费员
//				"(select berthsec_id from com_park_tb where is_delete=? and dici_id ="+
//				"(select id from dici_tb where did= ?))";
				//logger.error("InsertCarAdmission find user:"+userMap);
				//查询地磁对应的泊位编号
				
//			if(comParkMap!=null&&comParkMap.get("id")!=null){
//				pid = comParkMap.get("id")+"";
//			}
				if(userMap!=null&&userMap.get("uid")!=null){//查到了收费员
					putMesgToCache("10",uin,pid+"","1",bid,memcacheUtils);
					paramMap.put("uid",uin+"");
				}else {//消息给泊位段
//			sql ="select berthsec_id from com_park_tb where is_delete=? and dici_id ="+
//					"(select id from dici_tb where did= ?)";
//			Map berthMap = daService.getMap(sql, new Object[]{0,paramMap.get("sensornumber")});
					logger.error("InsertCarAdmission no user write to berthsecid:"+comParkMap.get("berthsec_id"));
					//if(berthMap!=null&&berthMap.get("berthsec_id")!=null){//找到了泊位段
					Long berthSegId = (Long)comParkMap.get("berthsec_id");
					if(berthSegId != null && berthSegId > 0){
						putMesgToCache("10",berthSegId,pid+"","1",bid,memcacheUtils);
						paramMap.put("berthsecid",berthSegId+"");
					}
					//}
				}
			}else {
				logger.error("InsertCarAdmission...地磁没有绑定车位..."+paramMap);
			}
		}
		
		paramMap.put("type", "in");
		writeToMongodb("zld_hdinout_logs", paramMap,comId);
		//logger.error("InsertCarAdmission write to mongodb result :"+result+",paramsMap :"+paramMap);
		AjaxUtil.ajaxOutput(response, StringUtils.createXML1(paramMap));
	}
	
	/**
	 * 车检器出场
	 * http://127.0.0.1/zld/api/hdinfo/InsertCarEntrance
	 */
	@POST
	@Path("/InsertCarEntrance")//车检器出场
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void insertCarEntrance(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		DataBaseService daService = (DataBaseService) ctx.getBean("dataBaseService");
		MemcacheUtils  memcacheUtils=(MemcacheUtils)ctx.getBean("memcacheUtils");
		CommonMethods  commonMethods=(CommonMethods)ctx.getBean("commonMethods");
		PublicMethods  publicMethods=(PublicMethods)ctx.getBean("publicMethods");
		//CarOutTime 出场时间
		//Indicate  配对标示
		//SensorNumber 车检器编号
		paramMap.put("type", "车检器出场");
		//判断是否已发过消息
		Map<String, String> cMap = new HashMap<String, String>();
		cMap.put("sensornumber", paramMap.get("sensornumber"));
		cMap.put("carouttime", AjaxUtil.decodeUTF8(paramMap.get("carouttime")));
		Long pcount = isHave("zld_sensor_logs", cMap);
		if(pcount!=null&&pcount>0){
			paramMap.put("error", "数据已处理过！");
			logger.equals("InsertCarEntrance >>>数据已处理过...."+paramMap);
			AjaxUtil.ajaxOutput(response, StringUtils.createXML1(paramMap));
			return;
		}
		//判断是否已发过消息结束
		writeToMongodb("zld_sensor_logs",paramMap,-1L);
		String carIntime = AjaxUtil.decodeUTF8(paramMap.get("carouttime"));
		Long outtime = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(carIntime);
		String sql = "update dici_tb set state = ?  where did =? ";
		int ret =daService.update(sql, new Object[]{0,paramMap.get("sensornumber")});
		logger.error(">>>>>>>车辆出场>>>>>>>>>>InsertCarEntrance "+paramMap+",update dici state:"+ret);
		if(ret<1){//地磁不存在 ，直接返回
			paramMap.put("error", "地磁不存在");
			AjaxUtil.ajaxOutput(response, StringUtils.createXML1(paramMap));
			return;
		}
		//更新地磁订单
		//?计算价格
		Double total = 0.0;
		Long intime = null;
		Integer comId = -1;
		Map diciMap = daService.getMap("select id,comid from dici_tb where did = ? and is_delete=? ",
				new Object[]{paramMap.get("sensornumber"), 0});
		if(diciMap!=null&&diciMap.get("comid")!=null){
			comId = (Integer)diciMap.get("comid");
		}
		Map botMap = daService.getMap("select id,in_time,orderid from berth_order_tb where berth_id=? and indicate=? and state=? order by in_time desc limit ? ", 
				new Object[]{paramMap.get("sensornumber"),paramMap.get("indicate"),0, 1});
		logger.error("sensor come out>>>comId:"+comId+",botMap:"+botMap);
		Long bid = -1L;
		Long orderid = -1L;
		if(botMap!=null&&botMap.get("in_time")!=null){
			intime=(Long)botMap.get("in_time");
			bid = (Long)botMap.get("id");
			orderid = (Long)botMap.get("orderid");
			if(intime>0){
				if(comId>0){
					try {
						Map<String, Object> orderMap = daService.getMap("select * from order_tb where id=? ", new Object[]{orderid});
						if(orderMap != null){
							Long uin = (Long)orderMap.get("uin");
							String carNumber =orderMap.get("car_number")+"";
							Integer pid = (Integer)orderMap.get("pid");
							Integer car_type = (Integer)orderMap.get("car_type");
							if(StringUtils.isNumber(carNumber)){
								carNumber = "车牌号未知";
							}
							if(carNumber.equals("null")||carNumber.equals("")){
								carNumber =publicMethods.getCarNumber(uin);
							}
							if("".equals(carNumber.trim())||"车牌号未知".equals(carNumber.trim()))
								carNumber ="null";
							if(pid>-1){
								total = Double.valueOf(publicMethods.getCustomPrice(intime, outtime, pid));
							}else {
								int isspecialcar = 0;
								Map map = daService.getMap("select typeid from car_number_type_tb where car_number = ? and comid=?", 
										new Object[]{carNumber, comId.longValue()});
								if(map!=null&&map.size()>0){
									isspecialcar = 1;
								}
								total = Double.valueOf(publicMethods.getPriceHY(intime, outtime, comId.longValue(), car_type, isspecialcar));
							}
						}else{
							Integer car_type = 0;
							List<Map<String, Object>> allCarTypes = commonMethods.getCarType(comId.longValue());
							if(allCarTypes != null && !allCarTypes.isEmpty()){
								car_type = Integer.valueOf(allCarTypes.get(0).get("value_no")+"");
							}
							total = Double.valueOf(publicMethods.getPriceHY(intime, outtime, comId.longValue(), car_type, 0));
						}
					} catch (Exception e) {
						logger.error(e.getMessage());
					}
				}
			}
			ret = daService.update("update berth_order_tb set out_time =?,state=?,total=? where id=? ", 
					new Object[]{outtime,1,total,bid});
			logger.error("InsertCarEntrance update berth_order_tb state:"+ret);
		}
		logger.error("bid:"+bid+",ret:"+ret);
		//根据地磁编号查找是否已注册到车位
		Map<String, Object> comParkMap = daService.getMap("select id,berthsec_id,comid from com_park_tb " +
				"where is_delete=? and  dici_id =(select id from dici_tb where did=? and is_delete=? )  ", 
				new Object[]{0,paramMap.get("sensornumber"), 0});
		Long uin = -1L;
		if(comParkMap!=null&&!comParkMap.isEmpty()){
			//查询地磁对应的泊位对应的泊位段对应的上岗收费员
			sql ="select uid from parkuser_work_record_tb where state=? and start_time>? and  berthsec_id =? ";
//					"(select berthsec_id from com_park_tb where is_delete=? and dici_id ="+
//					"(select id from dici_tb where did= ?))";
			Map userMap = daService.getMap(sql, new Object[]{0,0,comParkMap.get("berthsec_id")});
			logger.error("InsertCarEntrance find user:"+userMap);
			String pid =comParkMap.get("id")+"";	
//			if(comParkMap!=null&&comParkMap.get("id")!=null){
//				pid = comParkMap.get("id")+"";
//			}
			logger.error("userMap:"+userMap);
			if(userMap!=null&&userMap.get("uid")!=null){//查到了收费员
				uin = (Long)userMap.get("uid");
				if(ret==1&&uin!=null&&uin>0){
					ret = daService.update("update berth_order_tb set out_uid =? where berth_id=? and indicate=? ", 
							new Object[]{uin,paramMap.get("sensornumber"),paramMap.get("indicate")});
					logger.error("InsertCarEntrance >>>>更新出场收费员到地磁订单表："+ret);
					putMesgToCache("10",uin,pid,"0",bid,memcacheUtils);
					paramMap.put("uid",uin+"");
				}
			}else {//消息给泊位段
//				sql ="select berthsec_id from com_park_tb where is_delete=?  and dici_id ="+
//						"(select id from dici_tb where did= ?)";
//				Map berthMap = daService.getMap(sql, new Object[]{0,paramMap.get("sensornumber")});
				logger.error("InsertCarEntrance no user write to berthsecid:"+comParkMap.get("berthsec_id"));
				//if(berthMap!=null&&berthMap.get("berthsec_id")!=null){//找到了泊位段
				Long berthId = (Long)comParkMap.get("berthsec_id");
				if(berthId!=null&&berthId>0){
					putMesgToCache("10",berthId,pid,"0",bid,memcacheUtils);
					paramMap.put("berthsecid",berthId+"");
				}
				//没有收费员在岗就把绑定的POS机未结算订单置为逃单
				escape(bid, total, daService, commonMethods);
			}
		}else {
			logger.error("InsertCarEntrance...地磁没有绑定车位..."+paramMap);
		}
		paramMap.put("type", "out");
		writeToMongodb("zld_hdinout_logs", paramMap,comId.longValue());
		//logger.error("InsertCarEntrance write to mongodb result :"+result+",paramsMap :"+paramMap);
		AjaxUtil.ajaxOutput(response, StringUtils.createXML1(paramMap));
	}
	/**
	 * 车检器复位
	 * http://127.0.0.1/zld/api/hdinfo/SensorReset
	 */
	@POST
	@Path("/SensorReset")//车检器复位
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void sensorReset(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		DataBaseService daService = (DataBaseService) ctx.getBean("dataBaseService");
		//TransmitterNumber  基站编号
		//SensorNumber 车检器编号
		int ret = daService.update("update dici_tb set beart_time=? where did=? ", new Object[]{System.currentTimeMillis()/1000,paramMap.get("Sensornumber")});
		logger.error("SensorReset update dici ret:"+ret);
		ret =daService.update("update sites_tb set heartbeat=?,state=? where uuid=? ", new Object[]{
				System.currentTimeMillis()/1000,1,paramMap.get("transmitternumber")});
		logger.error("SensorReset update sites_tb ret:"+ret);
		Integer comid=-1;
		if(ret==1){
			Map diciMap = daService.getMap("select comid from dici_tb where did = ? and is_delete=? ",
					new Object[]{paramMap.get("transmitternumber"), 0});
			if(diciMap!=null&&diciMap.get("comid")!=null){
				comid = (Integer)diciMap.get("comid");
			}
		}
		writeToMongodb("zld_hdreset_logs", paramMap,comid.longValue());
		//logger.error("SensorReset write to mongodb result :"+result+",paramsMap :"+paramMap);
		AjaxUtil.ajaxOutput(response, StringUtils.createXML1(paramMap));
	}

	/**
	 * 写消息到缓存
	 * @param mesgtype 消息类型 10车位消息，由地磁发送
	 * @param key 收消息人编号，有收费员在岗时，给收费员，无人在岗时，给泊位段，收费员上岗后收到
	 * @param mesgKey 消息key
	 * @param putmesg 消息内容
	 * @param memcacheUtils 
	 */
	private void putMesgToCache(String mesgtype,Long key,String mesgKey,String putmesg,Long bid,MemcacheUtils memcacheUtils){
		Map<Long, String>  messCacheMap = memcacheUtils.doMapLongStringCache("parkuser_messages", null, null);
		String mesg = null;
		if(messCacheMap!=null){
			mesg=messCacheMap.get(key);
			//messCacheMap.remove(-1L);
		}else {
			messCacheMap = new HashMap<Long, String>();
		}
		//System.err.println("curr cache:"+messCacheMap);
		boolean iscached = false;
		if(mesg!=null){
			Map<String, Object> messageMap = ParseJson.jsonToMap(mesg);
			if(messageMap!=null&&!messageMap.isEmpty()){
				String mtype = (String)messageMap.get("mtype");
				if(mtype!=null&&mtype.equals(mesgtype)){//泊位消息
					List<Map<String, Object>> messageList =(List<Map<String, Object>>) messageMap.get("mesgs");
					if(messageList!=null&&!messageList.isEmpty()){
						for(Map<String, Object> msgMap: messageList){
							String parkid = (String)msgMap.get("id");//泊位编号
							if(parkid!=null&&parkid.equals(mesgKey)){//已经存在这个泊位的消息，删除
								messageList.remove(msgMap);
								break;
							}
						}
						Map<String, Object> parkMesgMap = new HashMap<String, Object>();
						parkMesgMap.put("id", mesgKey);
						parkMesgMap.put("state", putmesg);
						parkMesgMap.put("berthorderid", bid);
						//加入当前泊位状态消息
						messageList.add(parkMesgMap);
						String jsonMesg = "{\"mtype\":\""+mesgtype+"\",\"mesgs\": "+StringUtils.createJson(messageList)+"}";
						messCacheMap.put(key, jsonMesg);
						System.err.println("savr cache:"+key+"="+jsonMesg);
						memcacheUtils.doMapLongStringCache("parkuser_messages", messCacheMap, "update");
						iscached = true;
					}
				}
			}
		}
		if(!iscached){
			List<Map<String, Object>> messageList = new ArrayList<Map<String,Object>>();
			Map<String, Object> parkMesgMap = new HashMap<String, Object>();
			parkMesgMap.put("id", mesgKey);
			parkMesgMap.put("state", putmesg);
			parkMesgMap.put("berthorderid", bid);
			//加入当前泊位状态消息
			messageList.add(parkMesgMap);
			String jsonMesg ="{\"mtype\":\""+mesgtype+"\",\"mesgs\": "+StringUtils.createJson(messageList)+"}";
			messCacheMap.put(key, jsonMesg);
			System.err.println("savr cache:"+key+"="+jsonMesg);
			memcacheUtils.doMapLongStringCache("parkuser_messages", messCacheMap, "update");
		}
	}
	
	private void writeToMongodb(String dbName,Map<String, String> paramMap,Long comId){
		WriteResult result =null;
		try {
			DB mydb = MongoClientFactory.getInstance().getMongoDBBuilder("zld");
			mydb.requestStart();
			DBCollection collection = mydb.getCollection(dbName);
			BasicDBObject object = new BasicDBObject();
			for(String key : paramMap.keySet()){
				object.put(key, paramMap.get(key));
			}
			object.put("comid", comId);
			object.put("ctime", System.currentTimeMillis()/1000);
			mydb.requestStart();
			result = collection.insert(object);
			  //结束事务
			mydb.requestDone();
		} catch (Exception e) {
			logger.error("sensor write to monbodb error...."+result);
			e.printStackTrace();
		}
	}
	
	private Long isHave(String dbName,Map<String, String> paramMap){
		Long count = 0L;
		try {
			DB mydb = MongoClientFactory.getInstance().getMongoDBBuilder("zld");
			mydb.requestStart();
			DBCollection collection = mydb.getCollection(dbName);
			BasicDBObject object = new BasicDBObject();
			for(String key : paramMap.keySet()){
				object.put(key, paramMap.get(key));
			}
			mydb.requestStart();
			count =  collection.count(object);
			  //结束事务
			mydb.requestDone();
		} catch (Exception e) {
			logger.error("sensor query  monbodb count error...."+paramMap);
			e.printStackTrace();
			return count;
		}
		return count;
	}
	
	private void escape(Long bid, Double total,DataBaseService daService, CommonMethods commonMethods){
		logger.error("bid:"+bid+",total:"+total);
		try {
			if(bid != null && bid > 0){
				Map<String, Object> orderMap = daService.getMap("select o.id,o.uid from berth_order_tb b,order_tb o " +
						"where b.orderid=o.id and b.id=? and b.orderid>? and o.state=? ", new Object[]{bid, 0, 0});
				if(orderMap != null){
					Long orderid = (Long)orderMap.get("id");
					Long uid = (Long)orderMap.get("uid");
					boolean b = commonMethods.escape(orderid, uid, total, -1L);
					logger.error("handleEscape>>>orderid:"+orderid+",bid:"+bid+",total:"+total+",b:"+b);
				}
			}
		} catch (Exception e) {
			logger.error("handleEscape>>>bid:"+bid+",total:"+total,e);
		}
	}
}
