package com.zld.struts.request;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoClientFactory;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.AESEncryptor;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;

public class SyncInterfaceNew extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgService;
	@Autowired
	private CommonMethods methods;
	@Autowired
	private PublicMethods publicMethods;
	//1.同步车场设置信息
	private Logger logger = Logger.getLogger(SyncInterfaceNew.class);

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		String token = RequestUtil.getString(request, "token");
		//验证token
//		if(!action.equals(""))
//			return null;
		if(!action.equals("getToken")&&!action.equals("syncOrder")&&!action.equals("updateLocal")){
			if(token.equals("")){
				AjaxUtil.ajaxOutput(response,"no token");
				return null;
			}else {
				long comId = -1;
				Map comMap = daService.getPojo("select * from user_session_tb where token=?", new Object[]{token});
				if(comMap!=null&&comMap.get("comid")!=null){
					comId=(Long)comMap.get("comid");
					if(!publicMethods.isEtcPark(comId)){
						AjaxUtil.ajaxOutput(response,"token is invalid is not etcpark");
						return null;
					}
				}else {
					AjaxUtil.ajaxOutput(response,"token is invalid");
					return null;
				}
				//logger.error("token="+token+",comid="+comId+",action="+action);
			}
			
		}
		if(action.equals("syncinfopool")){//本地同步线上更改的  如价格，月卡，包月套餐等等
			syncinfopool(request,response);
		}else if("getlimitday".equals(action)){//获取授权日期
			getlimitday(request,response);
		}else if("syncswitchorder".equals(action)){//同步线上生成的订单到本地
			syncswitchorder(request,response);
		}else if("synclinecomplete".equals(action)){//同步线上结算的订单到本地
			synclinecomplete(request,response);
		}else if(action.equals("uploadOrder2Line")){//本地上传订单到线上
			/**
			 * 1,解析本地上传(生成订单1，免费2，结算订单3，4修改订单)的订单信息
			 */
			 String orders = AjaxUtil.decodeUTF8(AjaxUtil.decodeUTF8(RequestUtil.getString(request, "order")));//需要上传的订单.
			 Long type = RequestUtil.getLong(request, "type",-1L);
			 logger.error("uploadOrder2Line order:"+orders+",type:"+type+"action:"+action);
			 String ret = null;
			 if(!StringUtils.isNotNull(orders)){
				 AjaxUtil.ajaxOutput(response, "0");
				 return null;
			 }
			 if(type==1){//添加
				ret =  addOrder(orders,null);
				AjaxUtil.ajaxOutput(response, ret);
			 }else if(type==2){//免费
				ret =  freeOrder(orders);
				AjaxUtil.ajaxOutput(response, ret); 
			 }else if(type==3){//完成
				ret =  completeOrder(orders,null);
				AjaxUtil.ajaxOutput(response, ret);
			 }else if(type==4){//更新
				ret =  updateOrder(orders);
				AjaxUtil.ajaxOutput(response, ret);
			 }
			 if(type==-1){
				ret = dealOrder(orders);
				AjaxUtil.ajaxOutput(response, ret);
			 }
			 logger.error("uploadOrder2Line return order result:"+ret);
		}else if(action.equals("syncprepay")){//本地来同步线上预支付的订单
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			ArrayList list = new ArrayList();
			Long time = System.currentTimeMillis()/1000-30*86400;
			list.add(time);
			list.add(comid);
			List comsList = pgService.getAll("select * from com_info_tb where pid = ?",new Object[]{comid});
			String sql = "";
			int j=0;
			for (int i = 1; i < comsList.size()+1; i++) {
				long comidoth = Long.parseLong(((Map)comsList.get(i-1)).get("id")+"");
				list.add(comidoth);
				sql += " or comid = ? ";
			}
			list.add(0);
			list.add(1);
			List orders = pgService.getAll("select * from order_tb where create_time>? and  (comid = ? "+sql+") and state = ? and  need_sync=? ", list,1,5);
			String result = StringUtils.createJson(orders);
			logger.error("syncprepay comid"+comid+" return order result:"+result);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("completesyncprepay")){//本地通知获取到了预支付的订单  更改order_tb中的need_sync状态
			Long orderid = RequestUtil.getLong(request, "orderid", -1L);
			logger.error("completesyncprepay orderid"+orderid);
//				if(orderid>-1)
			int result = daService.update("update order_tb set need_sync=? where id = ?", new Object[]{2,orderid});
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("syncOrder")){//同步已电子支付订单（因线上结算了，本地未结算）
			Long orderid = RequestUtil.getLong(request, "orderid",-1L);//需要上传的订单.
			Long time = System.currentTimeMillis()/1000-30*86400;
			List values = new ArrayList();
			values.add(time);
			values.add(1);
			values.add(orderid);
			Map map = daService.getMap("select * from order_tb where create_time>? and  state=? and id = ?", values);
			String result = StringUtils.createJson(map);
			logger.error("syncOrder result"+result);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("uploadWork2Line")){//本地上传上下班记录
			String result = uploadWork(request);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("uploadliftrodpic")){//本地上传订单出入图片
			uploadliftrodpic(request);
		}else if(action.equals("updateversion")){//本地通知线上更新本地服务器版本
			Long comid = RequestUtil.getLong(request, "comid",-1L);
			Integer version = RequestUtil.getInteger(request, "version",-1);
			int ret= 0;
			if(comid>-1&&version>-1){
				Long count = daService.getLong("select count(*) from local_info_tb where comid = ? ", new Object[]{comid});
				long time = System.currentTimeMillis()/1000;
				if(count!=null&&count>0){
					ret = daService.update("update local_info_tb set version= ?,create_time=? where comid = ?", new Object[]{version,time,comid});
				}else{
					ret = daService.update("insert into local_info_tb(comid,version,create_time) values(?,?,?)", new Object[]{comid,version,time});
				}
			}
			logger.error("comid:"+comid+" update local version :"+version);
			AjaxUtil.ajaxOutput(response, ret+"");
		}else if(action.equals("updateLocal")){//获取更新文件集合和更新bat的压缩包
			updateLocal(request,response);
		}else if("getToken".equals(action)){//获取token
			String m = RequestUtil.getString(request, "mes");
			if(m!=null&&m.length()>0){
				String mes = AESEncryptor.decrypt("0123456789ABCDEFtingcaidfjalsjffdaslfkdafjdaljdf",m);
//					Long appid = RequestUtil.getLong(request, "appid", -1L);//appid 就是comid
//					String secret = RequestUtil.getString(request, "secret");//默认是tingchebaozld+comid
				String secret = mes.split(":")[0];
				Long appid = Long.parseLong(mes.split(":")[1]);
				logger.error("getToken m:"+m+",secert:"+secret+",appid:"+appid);
				if(publicMethods.isEtcPark(appid)){
					if(secret!=null&&!secret.equals("")){
						Map map = pgService.getMap("select * from local_info_tb where comid = ?", new Object[]{appid});
						if(map!=null&&map.get("secret")!=null){
							String se = map.get("secret")+"";
							if(secret.equals(se)){
								token = StringUtils.MD5(appid+secret+System.currentTimeMillis());
								doSaveSession(appid, token);
								AjaxUtil.ajaxOutput(response, token);
							}
						}
					}
				}
			}
		}else if("uploadTicket2Line".equals(action)){//上传减免券
			String ticket = AjaxUtil.decodeUTF8(AjaxUtil.decodeUTF8(RequestUtil.getString(request, "ticket")));//需要上传的减免券.
			logger.error("uploadTicket2Line ticket:"+ticket);
//				String ret = addTicket(ticket);
			JSONArray ja = JSONArray.fromObject(ticket);
//				ArrayList<String> list = new ArrayList<String>();
			JSONArray jsonAry = new JSONArray();
			 for (int i = 0; i < ja.size(); i++) {
				 String ret = "";
				 JSONObject jo =  ja.getJSONObject(i);
				 ret = addTicket(jo);
				 jsonAry.add(ret);
			 }
			logger.error("uploadTicket2Line return: "+jsonAry.toString());
			AjaxUtil.ajaxOutput(response, jsonAry.toString());
			
		}else if("syncTicket".equals(action)){//同步减免券
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			ArrayList list = new ArrayList();
			list.add(comid);
			List comsList = pgService.getAll("select * from com_info_tb where pid = ?",new Object[]{comid});
			String sql = "";
			int j=0;
			for (int i = 1; i < comsList.size()+1; i++) {
				long comidoth = Long.parseLong(((Map)comsList.get(i-1)).get("id")+"");
				list.add(comidoth);
				sql += " or comid = ? ";
			}
			list.add(0);
			List ticket = pgService.getAll("select * from ticket_tb where (comid = ? "+sql+") and need_sync=? ", list,1,5);
			String result = StringUtils.createJson(ticket);
			logger.error("syncTicket comid"+comid+" return order result:"+result);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("completesyncticket")){//本地通知获取到了减免券  更改ticket中的need_sync状态
			Long ticketid = RequestUtil.getLong(request, "ticketid", -1L);
			logger.error("completesyncticket orderid"+ticketid);
			int result = 0;
			if(ticketid>-1){
				result = daService.update("update ticket_tb set need_sync=? where id = ?", new Object[]{1,ticketid});
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}else if("uploadliftrod".equals(action)){//上传抬杆记录
			String res = uploadliftrod(request);
			AjaxUtil.ajaxOutput(response, res);
		}else if("uploadled".equals(action)){
			String res = uploadled(request);
			AjaxUtil.ajaxOutput(response, res);
		}else if("uploadbrake".equals(action)){
			String res = uploadbrake(request);
			AjaxUtil.ajaxOutput(response, res);
		}else if("uploadcamera".equals(action)){
			String res = uploadcamera(request);
			AjaxUtil.ajaxOutput(response, res);
		}else if("uploadworksite".equals(action)){
			String res = uploadworksite(request);
			AjaxUtil.ajaxOutput(response, res);
		}else if ("getsubstation".equals(action)) {
			String res = getsubstation(request);
			AjaxUtil.ajaxOutput(response, res);
		}else if ("uploadnumbertype".equals(action)) {
			String carnumbertype = AjaxUtil.decodeUTF8(AjaxUtil.decodeUTF8(RequestUtil.getString(request, "carnumbertype")));//需要上传的订单.
			logger.error("uploadnumbertype:"+carnumbertype);
			JSONArray ja = JSONArray.fromObject(carnumbertype);
			JSONArray jsonAry = new JSONArray();
			for (int i = 0; i < ja.size(); i++) {
				JSONObject jo = ja.getJSONObject(i);
				String ret = "";
				if (jo.get("line_id") == null || "".equals(jo.get("line_id") + "") || "null".equals(jo.get("line_id") + "")) {
					ret = sqlAndValue(jo,"car_number_type_tb");
				} else {
					daService.update("update car_number_type_tb set typeid = ?,update_time=? where id = ? and update_time<?", new Object[]{jo.getLong("typeid"),jo.getLong("update_time"),jo.getLong("line_id"),jo.getLong("update_time")});
					JSONObject json = new JSONObject();
					json.put("state", 1);
					json.put("line_id", jo.getString("line_id"));
					json.put("localId", jo.getString("id"));
					json.put("res", "1");
					ret=json.toString();
				}
				jsonAry.add(ret);
			}
			AjaxUtil.ajaxOutput(response, jsonAry.toString());
		}
		return null;
	}


	private String getsubstation(HttpServletRequest request) {
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		logger.error("getsubstation comid :"+comid);
		Long count = 0L;
		count = pgService.getLong("select count(*) from com_info_tb where pid = ?", new Object[]{comid});
		return count+"";
	}
	private void updateLocal(HttpServletRequest request,
		HttpServletResponse response) throws IOException {
		Long comid = RequestUtil.getLong(request, "comid",-1L);//需要上传的订单.
		Integer version = RequestUtil.getInteger(request, "version",-1);//需要上传的订单.
		if(comid==-1){
			return;
		}
		logger.error("updateLocal comid:"+comid+",version:"+version);
//		int maxversion = Integer.parseInt(CustomDefind.LOCALMAXVERSION);
//		logger.error("updateLcoal maxversion:"+maxversion);
		Map map = daService.getMap("select * from local_info_tb where comid = ? ", new Object[]{comid});
		if(map==null){
			return ;
		}
		if(Integer.parseInt(map.get("is_update")+"")==0){//||version>=maxversion){
			return ;
		}
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        OutputStream fos = null;
        InputStream fis = null;
        try{
	        //如果是从服务器上取就用这个获得系统的绝对路径方法。 
	    	String filepath = request.getSession().getServletContext().getRealPath("/update.rar");//zld/update.rar
	        File uploadFile = new File(filepath);
	        if(!uploadFile.exists())
	        	return ;
	        fis = new FileInputStream(uploadFile);
	        bis = new BufferedInputStream(fis);
	        fos = response.getOutputStream();
	        bos = new BufferedOutputStream(fos);
	        int bytesRead = 0;
	        byte[] buffer = new byte[8192];
	        while ((bytesRead = bis.read(buffer, 0, 8192)) != -1) {
	            bos.write(buffer, 0, bytesRead);
	        }
	        daService.update("update local_info_tb set is_update=? where comid = ? ", new Object[]{0,comid});
        }catch (Exception e) {
    	   return ;
        }finally{
			if(bos!=null)
				bos.flush();
			if(fis!=null)
				fis.close();
			if(bis!=null)
				bis.close();
			if(fos!=null)
				fos.close();
			if(fos!=null)
				fos.close();
		}
	}
	private String uploadled(HttpServletRequest request) {
		//定义返回的json对象
		JSONObject json = new JSONObject();
		Long ledid = RequestUtil.getLong(request, "id",-1L);
		Long state = RequestUtil.getLong(request, "state",-1L);
		Long upload_time = RequestUtil.getLong(request, "upload_time",-1L);
		int res = 0;
		if(state!=-1&&ledid!=-1&&upload_time!=-1){
			res = daService.update("update  com_led_tb set state=?,upload_time=? where id=? ", new Object[]{state,upload_time,ledid});
		}
//		return res+"_"+ledid+"_"+upload_time;
		json.put("state", res);
		json.put("ledid", ledid);
		json.put("upload_time", upload_time);
		return json.toString();
	}
	private String uploadbrake(HttpServletRequest request) {
		//定义返回的json对象
		JSONObject json = new JSONObject();
		Long passid = RequestUtil.getLong(request, "passid",-1L);
		Long state = RequestUtil.getLong(request, "state",-1L);
		Long upload_time = RequestUtil.getLong(request, "upload_time",-1L);
		int res = 0;
		if(state!=-1&&passid!=-1&&upload_time!=-1){
			res = daService.update("update com_brake_tb set state=?,upload_time=? where passid=? ", new Object[]{state,upload_time,passid});
			if(res==0){
				res = daService.update("insert into com_brake_tb(passid,state,upload_time) values (?,?,?) ", new Object[]{passid,state,upload_time});
			}
		}
//		return res+"_"+passid+"_"+upload_time;
		json.put("state", res);
		json.put("passid", passid);
		json.put("upload_time", upload_time);
		return json.toString();
	}
	private String uploadcamera(HttpServletRequest request) {
		//定义返回的json对象
		JSONObject json = new JSONObject();
		Long id = RequestUtil.getLong(request, "id",-1L);
		Long state = RequestUtil.getLong(request, "state",-1L);
		Long upload_time = RequestUtil.getLong(request, "upload_time",-1L);
		int res = 0;
		if(state!=-1&&id!=-1&&upload_time!=-1){
			res = daService.update("update com_camera_tb set state = ? ,upload_time = ? where id=?", new Object[]{state,upload_time,id});
		}
//		return res+"_"+id+"_"+upload_time;
		json.put("state", res);
		json.put("localId", id);
		json.put("upload_time", upload_time);
		return json.toString();
	}
	private String uploadworksite(HttpServletRequest request) {
    	//定义返回的json对象
		JSONObject json = new JSONObject();
		Long id = RequestUtil.getLong(request, "worksite_id", -1L);
		String equipmentmodel =RequestUtil.processParams(request, "equipmentmodel");
		String memoryspace =RequestUtil.processParams(request, "memoryspace");
		String internalspace =RequestUtil.processParams(request, "internalspace");
		Long upload_time = RequestUtil.getLong(request, "upload_time", -1L);
		int r = daService.update("update com_worksite_tb set host_name=?,host_memory=?,host_internal=?,upload_time=? where id = ? ", new Object[]{equipmentmodel,memoryspace,internalspace,upload_time,id});
		logger.error("upload info worksite_id:"+id+",equipmentmodel:"+equipmentmodel+",memoryspace:"+memoryspace+",internalspace:"+internalspace+",r:"+r);
//		return r+"_"+id+"_"+upload_time;
		json.put("state", r);
		json.put("worksite_id", id);
		json.put("upload_time", upload_time);
		return json.toString();
	}
	private String uploadliftrod(HttpServletRequest request) {
		 String liftrod = AjaxUtil.decodeUTF8(AjaxUtil.decodeUTF8(RequestUtil.getString(request, "liftrod")));//需要上传的订单.
		 logger.error("uploadliftrod>>"+liftrod);
		 JSONArray ja = JSONArray.fromObject(liftrod);
		 JSONObject jo = new JSONObject();
		 String res = "";
		 //定义返回值的JSONArray对象
		 JSONArray jsonAry = new JSONArray();
		 for (int i = 0; i < ja.size(); i++) {
			jo = ja.getJSONObject(i);
			JSONObject json = new JSONObject();
			if(jo.get("line_id")!=null&&jo.getLong("line_id")!=-1){
				int r = daService.update("update lift_rod_tb set img = ? where id = ?", new Object[]{jo.getString("img"),jo.getLong("line_id")});
				json.put("state", r);
				json.put("line_id", jo.getString("line_id"));
				json.put("localId", jo.getString("id"));
//				res += r+"_"+jo.getString("line_id")+"_"+jo.getString("id")+",";
				res=json.toString();
			}else{
				res = sqlAndValue(jo,"lift_rod_tb");
			}
			jsonAry.add(json);
		 }
		logger.error("uploadliftrod result>>"+res);
		return jsonAry.toString();
	}
	private void synclinecomplete(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Long comid = RequestUtil.getLong(request, "comid",-1L);
		Long maxtime = RequestUtil.getLong(request, "maxtime",-1L);
		logger.error("action:synclinecomplete,maxtime:"+maxtime);
		Long time = System.currentTimeMillis()/1000 - 30*86400;
		ArrayList arrayList = new ArrayList();
		arrayList.add(time);
		arrayList.add(comid);
		List comsList = pgService.getAll("select * from com_info_tb where pid = ?",new Object[]{comid});
		String sql = "";
		int j=0;
		for (int i = 1; i < comsList.size()+1; i++) {
			long comidoth = Long.parseLong(((Map)comsList.get(i-1)).get("id")+"");
			arrayList.add(comidoth);
			sql += " or comid = ? ";
		}
		arrayList.add(maxtime);
		arrayList.add(1);
		arrayList.add(4);
		List list = pgService.getAll("select * from order_tb where create_time>? and (comid = ? "+sql+") and end_time > ? and state=? and need_sync=? order by end_time",arrayList ,1,20);
		String result = StringUtils.createJson(list);
		logger.error("synclinecomplete return result:"+result);
		AjaxUtil.ajaxOutput(response, result);
	}
	private void syncswitchorder(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Long comid = RequestUtil.getLong(request, "comid",-1L);
		Long maxid = RequestUtil.getLong(request, "maxid",-1L);
		Long time = System.currentTimeMillis()/1000 - 30*86400;
		logger.error("action:syncswitchorder,maxid:"+maxid);
		daService.update("update switch_line_tb set end_time = ? where comid = ? and end_time is null ", new Object[]{System.currentTimeMillis() / 1000, comid});
		ArrayList arrayList = new ArrayList();
		arrayList.add(time);
		arrayList.add(comid);
		List comsList = pgService.getAll("select * from com_info_tb where pid = ?",new Object[]{comid});
		String sql = "";
		int j=0;
		for (int i = 1; i < comsList.size()+1; i++) {
			long comidoth = Long.parseLong(((Map)comsList.get(i-1)).get("id")+"");
			arrayList.add(comidoth);
			sql += " or comid = ? ";
		}
		arrayList.add(maxid);
		arrayList.add(0);
		arrayList.add(3);
		String sqlString = "select * from order_tb where create_time>? and (comid = ? "+sql+") and id > ? and state=? and need_sync=? order by id";
		logger.error("syncswitchorder>>>>>>>begin");
		List list = pgService.getAll(sqlString,arrayList ,1,20);
		String string = "";
		if(arrayList != null && !arrayList.isEmpty()){
			for(Object object : arrayList){
				string += "," + object;
			}
		}
		logger.error("syncswitchorder>>>>>>>sqlString:"+sqlString+",arrayList:"+string);
		String result = StringUtils.createJson(list);
		logger.error("comid:"+comid+"syncswitchorder return result:"+result);
		AjaxUtil.ajaxOutput(response, result);
		
	}
	/**
	 * 获取车场本地服务器授权到期时间
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void getlimitday(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Long comid = RequestUtil.getLong(request, "comid",-1L);
		Map map= daService.getMap("select * from local_info_tb where comid=?", new Object[]{comid});
		long limit_time = -1;
		if(map!=null&&map.get("limit_time")!=null){
			limit_time =Long.parseLong(map.get("limit_time")+"");
		}
		logger.error("comid "+comid+"getlimitday limite_time:"+limit_time);
		AjaxUtil.ajaxOutput(response, limit_time+"");
		
	}
	/**
	 * 获取线上的信息更改
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void syncinfopool(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Long maxid = RequestUtil.getLong(request, "maxid", 0L);
		Long comid = RequestUtil.getLong(request, "comid",-1L);
		String harddisk = RequestUtil.getString(request, "harddisk");
		String cpu = RequestUtil.getString(request, "cpu").split("\\.")[0]+"%";
		String memory = RequestUtil.getString(request, "memory")+"%";
		Integer version = RequestUtil.getInteger(request, "version",10);
		//logger.error("action:syncinfopool,maxid:"+maxid+",version:"+version);
		Long count = daService.getLong("select count(*) from local_info_tb where comid = ? ", new Object[]{comid});
		long time = System.currentTimeMillis()/1000;
		if(count!=null&&count>0){
			daService.update("update local_info_tb set version=?,cpu = ? ,memory=?, harddisk=?,create_time=? where comid = ?", new Object[]{version,cpu,memory,harddisk,time,comid});
		}else{
			daService.update("insert into local_info_tb(comid,version,cpu,memory,harddisk,create_time,limit_time) values(?,?,?,?,?,?,?)", new Object[]{comid,version,cpu,memory,harddisk,time,System.currentTimeMillis()/1000+60*60*24*30});
		}
		StringBuffer result = new StringBuffer("{");
		String r = null;
		if(comid > 0){
			List<Object> arrylist = new ArrayList<Object>();
			List<Object> arrayList2 = new ArrayList<Object>();
			arrayList2.add(1);
			arrylist.add(comid);
			arrayList2.add(comid);
			List comsList = pgService.getAll("select * from com_info_tb where pid = ?",new Object[]{comid});
			String sql = "";
			int j=0;
			for (int i = 1; i < comsList.size()+1; i++) {
				long comidoth = Long.parseLong(((Map)comsList.get(i-1)).get("id")+"");
				arrylist.add(comidoth);
				arrayList2.add(comidoth);
				sql += " or comid = ? ";
			}
			arrylist.add(maxid);
			arrayList2.add(maxid);
			arrayList2.add(0);
			List<Map> list = daService.getAll("select * from sync_info_pool_tb where (comid = ? "+sql+") and id > ? order by id ",arrylist ,1,20);
			int re = daService.update("update sync_info_pool_tb set state=? where (comid = ? "+sql+") and id <= ? and state=? ", arrayList2);
			logger.error("sync-line>>>id:"+maxid+",comid:"+comid+",re:"+re);
			Long id = maxid;
			for (Map li:list){
				long c = Long.parseLong(li.get("id")+"");
				if(c>id){
					id = c;
				}
				Object tablename = li.get("table_name");
				Integer tableid = Integer.valueOf(li.get("table_id")+"");
				Object operate = li.get("operate");
				if(tablename!=null&&tableid>=0){
					List retList = daService.getAll("select * from "+tablename+" where id = ?", new Object[]{tableid});
					String ret = StringUtils.createJson(retList);
					if(ret.length()>3){
						ret = ret.substring(1, ret.length()-2)+",\"operate\":\""+operate+"\""+",\"op_id\":\""+c+"\"}";
						result.append(tablename+":").append(ret+",");
					}else{
						if(Long.parseLong(operate+"")==2){
							ret = "{\"id\":\""+tableid+"\",\"operate\":\""+operate+"\""+",\"op_id\":\""+c+"\"}";
							result.append(tablename+":").append(ret+",");
						}
					}
				}
			}
			//logger.error(list.size()+" "+result.length()+" "+result.toString());
			if(result.length()>2){
				r = result.substring(0, result.length()-1)+",\"maxid\":\""+id+"\"";
			}
			if(result.length()==1){
				r = "{\"maxid\":\""+id+"\"";
			}
			r+="}";
			//logger.error("syncinfopool return result:"+r);
			AjaxUtil.ajaxOutput(response, r);
		}
		
	}
	/**
	 * 上传抬杆记录图片
	 * @param request
	 * @throws IOException
	 */
	private void uploadliftrodpic(HttpServletRequest request) throws IOException {
//		Long ntime = System.currentTimeMillis()/1000;
		Long lrid = RequestUtil.getLong(request, "lrid", -1L);
		Long ntime = RequestUtil.getLong(request, "ctime", -1L);
		logger.error("begin upload lift rod picture....lrid:"+lrid+",ctime:"+ntime);
		Map<String, String> extMap = new HashMap<String, String>();
	    extMap.put(".jpg", "image/jpeg");
	    extMap.put(".jpeg", "image/jpeg");
	    extMap.put(".png", "image/png");
	    extMap.put(".gif", "image/gif");
	    extMap.put(".webp", "image/webp");
		if(lrid==-1){
			return;
		}
		request.setCharacterEncoding("UTF-8"); // 设置处理请求参数的编码格式
		DiskFileItemFactory  factory = new DiskFileItemFactory(); // 建立FileItemFactory对象
		factory.setSizeThreshold(16*4096*1024);
		ServletFileUpload upload = new ServletFileUpload(factory);
		// 分析请求，并得到上传文件的FileItem对象
		upload.setSizeMax(16*4096*1024);
		List<FileItem> items = null;
		try {
			items =upload.parseRequest(request);
		} catch (FileUploadException e) {
			e.printStackTrace();
			return;
		}
		String filename = ""; // 上传文件保存到服务器的文件名
		InputStream is = null; // 当前上传文件的InputStream对象
		// 循环处理上传文件
		for (FileItem item : items){
			// 处理普通的表单域
			if (!item.isFormField()){
				// 从客户端发送过来的上传文件路径中截取文件名
				filename = item.getName().substring(item.getName().lastIndexOf("\\")+1);
				is = item.getInputStream(); // 得到上传文件的InputStream对象
				logger.error("filename:"+item.getName()+",stream:"+is);
			}else{
				continue;
			}
			String file_ext =filename.substring(filename.lastIndexOf(".")).toLowerCase();// 扩展名
			String picurl = lrid + "_"+ System.currentTimeMillis()/1000 + file_ext;
			BufferedInputStream in = null;  
			ByteArrayOutputStream byteout =null;
			try {
				in = new BufferedInputStream(is);   
				byteout = new ByteArrayOutputStream(1024);        	       
				
				byte[] temp = new byte[1024];        
				int bytesize = 0;        
				while ((bytesize = in.read(temp)) != -1) {        
					byteout.write(temp, 0, bytesize);        
				}        
				
				byte[] content = byteout.toByteArray(); 
				DB mydb = MongoClientFactory.getInstance().getMongoDBBuilder("zld");
				mydb.requestStart();
				
				DBCollection collection = mydb.getCollection("lift_rod_pics");
				//  DBCollection collection = mydb.getCollection("records_test");
				
				BasicDBObject document = new BasicDBObject();
				document.put("lrid", lrid);
				document.put("ctime", ntime);
				document.put("type", extMap.get(file_ext));
				document.put("content", content);
				document.put("filename", picurl);
				//开始事务
				//结束事务
				mydb.requestStart();
				collection.insert(document);
				//结束事务
				mydb.requestDone();
				in.close();        
				is.close();
				byteout.close();
				String sql = "update lift_rod_tb set img=? where id =?";
				int ret = daService.update(sql, new Object[]{picurl,lrid});
				logger.error(">>>>>>>>>>orderId:"+lrid+",filename:"+picurl+", update lift_rod_tb, ret:"+ret);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}finally{
				if(in!=null)
					in.close();
				if(byteout!=null)
					byteout.close();
				if(is!=null)
					is.close();
			}
		}
	
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
		//定义返回值的json对象
		JSONObject json = new JSONObject();
		Long count = daService.getLong("select count(*) from ticket_tb where orderid = ? and create_time=? and state = ? ", new Object[]{jo.getLong("lineorderid"),jo.getLong("create_time"),1});
		if(count!=null&&count>0){//已经存在则告诉上传成功（）
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
			json.put("state", 1);
			json.put("localId",jo.getLong("id"));
//			return "1_"+jo.getLong("id");
			return json.toString();
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
		json.put("state", 1);
		json.put("localId",jo.getLong("id"));
//		return "1_"+jo.getLong("id");
		return json.toString();
	}
	private String doSaveSession(Long comid,String token){
		Map<String, Object> map = daService.getMap("select token from user_session_tb where comid=? and uin =?", new Object[]{comid,-1});//uin=-1代表是车场同步token
		String oldtoken =null;
		if(map!=null){
			oldtoken = (String)map.get("token");
			daService.update("update user_session_tb set token=? ,create_time=? where comid=? and uin =?", 
					new Object[]{token,System.currentTimeMillis()/1000,comid,-1});
		}else {
			//保存本次登录的token
			daService.update("insert into user_session_tb (comid,token,create_time,uin) " +
					"values (?,?,?,?)", 
					new Object[]{comid,token,System.currentTimeMillis()/1000,-1});
		}
		
		return oldtoken;
	}

	/**
	 * 上传上班记录
	 * @param request
	 * @return
	 */
	private String uploadWork(HttpServletRequest request) {
		//需要上传的上班记录.
		String work = RequestUtil.getString(request, "work");
		JSONObject jo = JSONObject.fromObject(work);
		//定义返回值json对象
		JSONObject json = new JSONObject();
		logger.error("uploadwork:"+work);
		int ret = 0;
		Long id = daService.getLong(
				"SELECT nextval('seq_parkuser_work_record_tb'::REGCLASS) AS newid", null);
		try {
			logger.error(jo.getString("line_id"));
			if(jo.getString("line_id")!=null&&!jo.getString("line_id").equals("null")){
				logger.error("update");
				id = jo.getLong("line_id");
				if(id!=null&&id>0){
					String sql  = "update parkuser_work_record_tb set end_time = ?  where id = ?";
					ret = daService.update(sql, new Object[]{jo.getLong("end_time"),jo.getLong("line_id")});
					logger.error("ret:"+ret+","+jo.getLong("end_time"));
				}
				json.put("line_id", id);
			}else{
				Long wid = jo.getLong("worksite_id");
				if(wid!=null&&wid>0){
					int r1 =daService.update("update parkuser_work_record_tb set end_time=? where worksite_id = ? and end_time is null", new Object[]{jo.getLong("start_time"),wid});
					logger.error("按工作站更新,worksite_id="+wid+",r="+r1);
				}
				Long uid = jo.getLong("uid");
				if(uid!=null&&uid>0){
					int r2 =daService.update("update parkuser_work_record_tb set end_time=? where  uid=? and end_time is null", new Object[]{jo.getLong("start_time"),uid});
					logger.error("按收费员更新,uid="+uid+",r="+r2);
				}
				if(jo.getString("end_time")!=null&&!jo.getString("end_time").equals("null")){
					String sql  = "insert into parkuser_work_record_tb(id,start_time,end_time,worksite_id,uid) values (?,?,?,?,?)";
					ret = daService.update(sql, new Object[]{id,jo.getLong("start_time"),jo.getLong("end_time"),jo.getLong("worksite_id"),jo.getLong("uid")});
				}else{
					String sql  = "insert into parkuser_work_record_tb(id,start_time,worksite_id,uid) values (?,?,?,?)";
					ret = daService.update(sql, new Object[]{id,jo.getLong("start_time"),jo.getLong("worksite_id"),jo.getLong("uid")});
				}
				json.put("cloudId", id);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		json.put("state", "1");
		json.put("localId", jo.getLong("id"));
//		return 1+"_"+jo.getLong("id")+"_"+id;
//		return null;
		return json.toString();
	}
	/**
	 * 处理上传订单
	 * @param orders
	 * @return
	 */
	private String dealOrder(String orders) {
		JSONArray ja = JSONArray.fromObject(orders);
//		ArrayList<String> list = new ArrayList<String>();
		String ret = "";
//		 for (int i = 0; i < ja.size(); i++) {
//			 JSONObject jo =  ja.getJSONObject(i);
//			 if(jo.getInt("state")==0){
//				 ret+=addOrder(null, jo)+",";
//			 }else if(jo.getInt("state")==1){
//				 if(jo.get("line_id")==null||"".equals(jo.get("line_id")+"")||"null".equals(jo.get("line_id")+"")){
//					 ret+=addOrder(null, jo)+",";
//				 }else{
//					 ret+=completeOrder(null, jo)+",";
//				 }
//			 }
//		 }
		JSONArray jsonAry = new JSONArray();
		String result ="";
		 for (int i = 0; i < ja.size(); i++) {
			 JSONObject jo =  ja.getJSONObject(i);
			 if(jo.getInt("state")==0){
				 result=addOrder(null, jo);
			 }else if(jo.getInt("state")==1){
				 if(jo.get("line_id")==null||"".equals(jo.get("line_id")+"")||"null".equals(jo.get("line_id")+"")){
					 result=addOrder(null, jo);
				 }else{
					 result=completeOrder(null, jo);
				 }
			 }
			 jsonAry.add(result);
		 }
		return jsonAry.toString();
	}
	/**
	 * 上传生成订单
	 * @param orders
	 * @param jo
	 * @return
	 */
	private String addOrder(String orders,JSONObject jo) {
		if(jo==null)
			jo = JSONObject.fromObject(orders);//需要上传的订单（本地正常生成并结算的（除去估算订单））和同步服务器后本地结算的订单
		 Long nextid = daService.getLong(
					"SELECT nextval('seq_order_tb'::REGCLASS) AS newid", null);
//		 state,end_time,auto_pay,pay_type,nfc_uuid" +
//	 		",c_type,uid,car_number,imei,pid,car_type,pre_state,in_passid,out_passid)"
		 StringBuffer insertsql = new StringBuffer("insert into order_tb(id,comid,uin,");//order by o.end_time desc
		 StringBuffer valuesql = new StringBuffer("?,?,?,?,?,?,?,?,");
		 ArrayList list = new ArrayList();
		 list.add(nextid);
		 Long comid= jo.getLong("comid");
		 list.add(comid);
		 Long uin = jo.getLong("uin");
//		 list.add(jo.getLong("uin"));
		 Long createtime = null;
		 String carnumber = null;
		 System.out.println(" add one orders:"+orders);
		 if(!"null".equals(jo.getString("create_time"))){
			 valuesql.append("?,");
			 insertsql.append("create_time,");
			 createtime = jo.getLong("create_time");
			 list.add(createtime);
		 }
		 if(!"null".equals(jo.getString("car_number"))){
			 valuesql.append("?,");
			 insertsql.append("car_number,");
			 carnumber = jo.getString("car_number");
			 list.add(carnumber);
		 }
		 if(createtime!=null&&carnumber!=null){
			Map count =  daService.getMap("select id,uin from order_tb where car_number=? and create_time = ? and state = ?", new Object[]{carnumber,createtime,0});
			if(count!=null&& count.size()>0){//如果线上服务器和本地上传的订单的时间，车牌相同则本地删除
				JSONObject json = new JSONObject();
				json.put("state", "1");
				json.put("uin", count.get("uin"));
				json.put("cloudId", count.get("id"));
				json.put("localId", jo.getLong("id"));
//				return 1+"_"+count.get("uin")+"_"+count.get("id")+"_"+jo.getLong("id");
//				return null;
				return json.toString();
			}
			Map u = daService.getMap("select uin from car_info_tb where car_number=?", new Object[]{carnumber});
			if(u!=null&&u.get("uin")!=null){
				uin = Long.valueOf(u.get("uin")+"");
			}
		 }
		 list.add(2,uin);
		 if(!"null".equals(jo.getString("total"))&&!"价格未知".equals(jo.getString("total"))){
			 valuesql.append("?,");
			 insertsql.append("total,");
			 list.add(jo.getDouble("total"));
		 }
		 insertsql.append("state,");
		 list.add(jo.getLong("state"));
		 if(!"null".equals(jo.getString("end_time"))&&!"0".equals(jo.getString("end_time"))){
			 valuesql.append("?,");
			 insertsql.append("end_time,");
			 list.add(jo.getLong("end_time"));
		 }
		 insertsql.append("auto_pay,");
		 list.add(jo.getLong("auto_pay"));
		 insertsql.append("pay_type,");
		 list.add(jo.getLong("pay_type"));
		 if(!"null".equals(jo.getString("nfc_uuid"))){
			 valuesql.append("?,");
			 insertsql.append("nfc_uuid,");
			 list.add(jo.getString("nfc_uuid"));
		 }
		 insertsql.append("c_type,");
		 list.add(jo.getLong("c_type"));
		 insertsql.append("uid,");
		 list.add(jo.getLong("uid"));
		 if(!"null".equals(jo.getString("imei"))){
			 insertsql.append("imei,");
			 valuesql.append("?,");
			 list.add(jo.getString("imei"));
		 }
		 insertsql.append("pid,");
		 insertsql.append("car_type,");
		 insertsql.append("pre_state,");
		 insertsql.append("in_passid,");
		 valuesql.append("?,?,?,?,?,?,?,?,?,?");
		 insertsql.append("out_passid,type,need_sync,freereasons,ishd,isclick) values ("+valuesql+")");
		
		 list.add(jo.getLong("pid"));
		 list.add(jo.getLong("car_type"));
		 list.add(jo.getLong("pre_state"));
		 list.add(jo.getString("in_passid").equals("null")?-1:jo.getLong("in_passid"));
		 list.add(jo.getString("out_passid").equals("null")?-1:jo.getLong("out_passid"));
		 
		 list.add(0);//type   服务器本地化订单    
		 list.add(2);//need_sync   2  本地化订单
		 if(jo.get("freereasons")!=null&&!"null".equals(jo.getString("freereasons"))&&!"".equals(jo.getString("freereasons"))){
			 list.add(jo.getInt("freereasons"));
		 }else{
			 list.add(-1);
		 }
		 Integer isHd = methods.isHd(comid);
		 list.add(isHd);
		 if(jo.get("isclick")!=null&&!"null".equals(jo.getString("isclick"))&&!"".equals(jo.getString("isclick"))){
			 list.add(jo.getInt("isclick"));
		 }else{
			 list.add(0);
		 }
		 int insert = daService.update(insertsql.toString(), list.toArray());
		 System.out.println(insertsql.toString()+":"+list.toArray().toString());
		 logger.error("本地生成订单插入ret:"+insert+",orderid:"+nextid);
		 JSONObject json = new JSONObject();
			json.put("state", "1");
			json.put("uin",uin);
			json.put("cloudId", nextid);
			json.put("localId", jo.getLong("id"));
//			return insert+"_"+uin+"_"+nextid+"_"+jo.getLong("id");
//			return null;
			return json.toString();
	}
	/**
	 * 线下修改订单上传
	 * @param order
	 * @return
	 */
	private String updateOrder(String order){
		 JSONObject	jo = JSONObject.fromObject(order);
		 Long lineid = -1L;//jo.getLong("id");//update user_info_tb set online_flag=? ,logon_time=? where id=?
		 StringBuffer insertsql = new StringBuffer("update order_tb set");//order by o.end_time desc
		 ArrayList list = new ArrayList();
		 Long createtime = null;
		 String carnumber = null;
		 if(!"null".equals(jo.getString("create_time"))){
			 createtime = jo.getLong("create_time");
			 insertsql.append(" create_time=?,");
			 list.add(createtime);
		 }
		 if(jo.getString("line_id")!=null&&!"null".equals(jo.getString("line_id"))){
			 lineid = jo.getLong("line_id");
		 }
		 if(!"null".equals(jo.getString("car_number"))){
			 insertsql.append(" car_number=?,");
			 list.add(jo.getString("car_number"));
			 carnumber = jo.getString("car_number");
		 }
//		 if(createtime!=null&&carnumber!=null){
//			logger.error("createtime:"+createtime+",car_number:"+carnumber);
//			Long id =  daService.getLong("select max(id) from order_tb where car_number=? and create_time = ? and state = ?", new Object[]{carnumber,createtime,0});
//			if(id!=null&& id>0){
//				lineid=id;
//			}
//		 }
		 long uin = -1;
		 Map u = daService.getMap("select uin from car_info_tb where car_number=?", new Object[]{carnumber});
			if(u!=null&&u.get("uin")!=null){
				 uin = Long.valueOf(u.get("uin")+"");
				 insertsql.append(" uin=?,");
				 list.add(uin);
				 
			}
		 if(!"null".equals(jo.getString("total"))){
			 insertsql.append(" total=?,");
			 list.add(jo.getDouble("total"));
		 }
		 if(!"null".equals(jo.getString("state"))){
			 insertsql.append(" state=?,");
			 list.add(jo.getLong("state"));
		 }
		 if(!"null".equals(jo.getString("end_time"))){
			 insertsql.append(" end_time=?,");
			 list.add(jo.getLong("end_time"));
		 }
		 if(!"null".equals(jo.getString("pay_type"))){
			 insertsql.append(" pay_type=?,");
			 System.out.println(jo.getInt("pay_type"));
			 list.add(jo.getInt("pay_type"));
		 }
		 if(!"null".equals(jo.getString("uid"))){
			 insertsql.append(" uid=?,");
			 list.add(jo.getLong("uid"));
		 }
		 insertsql.append(" out_passid=?,type=?,freereasons=?,isclick=? ");
		 list.add(jo.getLong("out_passid"));
		 list.add(0);
		 if(jo.get("freereasons")!=null&&!"null".equals(jo.getString("freereasons"))&&!"".equals(jo.getString("freereasons"))){
			 list.add(jo.getInt("freereasons"));
		 }else{
			 list.add(-1);
		 }
		 if(jo.get("isclick")!=null&&!"null".equals(jo.getString("isclick"))&&!"".equals(jo.getString("isclick"))){
			 list.add(jo.getInt("isclick"));
		 }else{
			 list.add(0);
		 }
		 String sql = insertsql+" where id = ?";
		 list.add(lineid);
		 int update = daService.update(sql, list.toArray());
		 System.out.println(sql);
		 logger.error("本地结算订单更新ret:"+update+",orderid:"+lineid);
		 if(update==1){
			 if(jo.getLong("state")>0){
				 if(jo.getLong("state")==1){
					 if(!"null".equals(jo.getString("total"))&&jo.getInt("c_type")!=5&&jo.getInt("pay_type")==1){
						 Long c = daService.getLong("select count(*) from parkuser_cash_tb where orderid = ?", new Object[]{lineid});
						 if(c!=null&&c<1){
							 int r = daService.update("insert into parkuser_cash_tb(uin,amount,orderid,create_time) values(?,?,?,?)", new Object[]{jo.getLong("uid"),jo.getDouble("total"),jo.getLong("line_id"),jo.getLong("end_time")});
							 logger.error("写现金收费记录ret:"+r+",orderid:"+lineid+",amount:"+jo.getDouble("total")+",生成现金收费记录ret:"+r);
						 }else{
							 daService.update("update parkuser_cash_tb set amount=? where orderid =? ", new Object[]{StringUtils.formatDouble(jo.getDouble("total")),jo.getLong("line_id")});
							 logger.error("修改现金记录：orderid:"+lineid+",amount:"+jo.getDouble("total"));
						 }
					 }else{
						 logger.error("月卡或者免费或者电子支付不写现金记录orderid:"+lineid);
					 }
				 }
			 }
		 }
		 JSONObject json = new JSONObject();
			json.put("state", "1");
			json.put("uin",uin);
			json.put("cloudId", lineid);
			json.put("localId", jo.getLong("id"));
//			return update+"_"+uin+"_"+lineid+"_"+jo.getLong("id");
//			return null;
			return json.toString();
		 
	}
	/**
	 *  结算 （已上传生成 的订单）
	 * @param order
	 * @param jo
	 * @return
	 */
	private String completeOrder(String order,JSONObject jo){
		 if(jo==null)
			 jo = JSONObject.fromObject(order);
		 boolean backflag = false;
//		 if(jo.getInt("sync_state")==3&&jo.getLong("uin")>0){
//			//处理退款
//			 backflag = backmoney(jo);
//		 }
		 Integer sync_state = jo.getInt("sync_state");
		 Integer pay_type = jo.getInt("pay_type");
		 int lstate = 0;//线上订单状态   为0时才能退款   还有如果线下state==0假如安卓hd切到线上的话会结算订单  但是线下没有结算   当车再次进场0元结算掉会同步上来update订单  因此这时候需要伪装下返回值
		 Long orderId = jo.getLong("line_id");
		 boolean centerflag = false;
		 if(sync_state==3&&!backflag){//处理退款
			 Map map = daService.getMap("select * from order_tb where id =? and state=? and total>? and pay_type=? and need_sync>? and uin>?", new Object[]{orderId,0,0,2,0,0});
			 if(map!=null){
				backflag = backmoney(jo);
			 }else{
				 if(pay_type==2){//防止大连需要退款的时候会把订单改成电子支付
					 pay_type = 1; 
					 backflag = true;
				 }
			 }
		 }
		
		 if(sync_state==3&&!backflag){
			 return 1+"_"+jo.getLong("id");
		 }
		 //解决线上生成的同时本地服务器此时开着同步下了订单  但是线上结算状态不同步下来   该车下次进来的时候出现0元结算后同步上来 更改订单状态
		 if(jo.get("total")!=null&&!"null".equals(jo.getString("total"))&&jo.getDouble("total")==0){
			 Map map = daService.getMap("select * from order_tb where id =? and state=? and total>? and need_sync=? ", new Object[]{orderId,1,0,3});
			 if(map!=null){
				 Long etime = Long.parseLong(map.get("end_time")+"");
				 if(etime+60<jo.getLong("end_time")){//当线下数据同步上去的时候金额为0  并且线上该订单线上已结算有金额（并且该订单是线上生成的）   并且时间相差一分钟则直接返回成功  不更新数据
					 return 1+"_"+jo.getLong("id");
				 }
			 }
		 }
			 Long lineid = -1L;//jo.getLong("id");//update user_info_tb set online_flag=? ,logon_time=? where id=?
			 StringBuffer insertsql = new StringBuffer("update order_tb set");//order by o.end_time desc
			 ArrayList list = new ArrayList();
			 Long createtime = null;
			 String carnumber = null;
			 if(!"null".equals(jo.getString("create_time"))){
				 createtime = jo.getLong("create_time");
				 insertsql.append(" create_time=?,");
				 list.add(createtime);
			 }
			 if(jo.getString("line_id")!=null&&!"null".equals(jo.getString("line_id"))){
				 lineid = jo.getLong("line_id");
			 }
			 if(!"null".equals(jo.getString("car_number"))){
				 insertsql.append(" car_number=?,");
				 list.add(jo.getString("car_number"));
				 carnumber = jo.getString("car_number");
			 }
//			 if(createtime!=null&&carnumber!=null){
//				logger.error("createtime:"+createtime+",car_number:"+carnumber);
//				Long id =  daService.getLong("select max(id) from order_tb where car_number=? and create_time = ? and state = ?", new Object[]{carnumber,createtime,0});
//				if(id!=null&& id>0){
//					lineid=id;
//				}
//			 }
			 if(!"null".equals(jo.getString("total"))){
				 insertsql.append(" total=?,");
				 list.add(jo.getDouble("total"));
			 }
			 if(!"null".equals(jo.getString("state"))){
				 Long state = jo.getLong("state");
				 insertsql.append(" state=?,");
//				 if(state==3){
//					 state=1L;
//				 }
				 list.add(state);
			 }
			 if(!"null".equals(jo.getString("end_time"))){
				 insertsql.append(" end_time=?,");
				 list.add(jo.getLong("end_time"));
			 }
			 Map map2 = daService.getMap("select * from order_tb where id =? ", new Object[]{orderId});
			 if(map2 != null && jo.getInt("pay_type")!=8&&(Integer.parseInt(map2.get("pay_type")+"")==4||Integer.parseInt(map2.get("pay_type")+"")==5||Integer.parseInt(map2.get("pay_type")+"")==6)){
					centerflag = true;
			 }
			 if(!centerflag){
				 if(!"null".equals(jo.getString("pay_type"))){
					 insertsql.append(" pay_type=?,");
//					 System.out.println(jo.getInt("pay_type"));
					 list.add(pay_type);
				 }
			 }
			 if(!"null".equals(jo.getString("uid"))){
				 insertsql.append(" uid=?,");
				 list.add(jo.getLong("uid"));
			 }
			 if(jo.get("freereasons")!=null&&!"null".equals(jo.getString("freereasons"))&&!"".equals(jo.getString("freereasons"))){
				 insertsql.append(" freereasons=?,");
				 list.add(jo.getInt("freereasons"));
			 }
			 if(jo.get("isclick")!=null&&!"null".equals(jo.getString("isclick"))&&!"".equals(jo.getString("isclick"))){
				 insertsql.append(" isclick=?,");
				 list.add(jo.getInt("isclick"));
			 }
			 if(jo.get("car_type")!=null&&!"null".equals(jo.getString("car_type"))&&!"".equals(jo.getString("car_type"))){
				 insertsql.append(" car_type=?,");
				 list.add(jo.getInt("car_type"));
			 }
			 if(jo.get("c_type")!=null&&!"null".equals(jo.getString("c_type"))&&!"".equals(jo.getString("c_type"))){
				 insertsql.append(" c_type=?,");
				 list.add(jo.getInt("c_type"));
			 }
			 insertsql.append(" out_passid=?");
			 list.add(jo.getLong("out_passid"));
			 String sql = insertsql+" where id = ? ";
			 list.add(lineid);
//			 list.add(0);
			 int update = daService.update(sql, list.toArray());
			 System.out.println(sql);
			 logger.error("本地结算订单更新ret:"+update+",orderid:"+lineid);
			 if(update==1){
				 if(jo.getLong("state")>0){
					 if(jo.getLong("state")==1){
						 if(!"null".equals(jo.getString("total"))&&jo.getInt("pay_type")!=8&&jo.getInt("c_type")!=5&&jo.getInt("pay_type")!=2){
							 Long c = daService.getLong("select count(*) from parkuser_cash_tb where orderid = ? and type = ?", new Object[]{lineid,0});
							 if(c!=null&&c<1){
								 int r = daService.update("insert into parkuser_cash_tb(uin,amount,orderid,create_time) values(?,?,?,?)", new Object[]{jo.getLong("uid"),jo.getDouble("total"),jo.getLong("line_id"),jo.getLong("end_time")});
								 logger.error("写现金收费记录ret:"+r+",orderid:"+lineid+",amount:"+jo.getDouble("total")+",生成现金收费记录ret:"+r);
							 }else{
								 //修改
								 daService.update("update parkuser_cash_tb set amount=? where orderid =? ", new Object[]{StringUtils.formatDouble(jo.getDouble("total")),jo.getLong("line_id")});
								 logger.error("修改现金记录：orderid:"+lineid+",amount:"+jo.getDouble("total"));
							 }
						 }else{
							 logger.error("电子支付或者月卡或者免费不写现金记录orderid:"+lineid);
						 }
						 if((jo.getInt("pay_type")==8)){
							 daService.update("update parkuser_cash_tb set amount=? where orderid =? ", new Object[]{0,jo.getLong("line_id")});
							 logger.error("修改现金记录：orderid:"+lineid+",amount:为0");
						 }
					 }
				 }
			 }
			 if(lstate==1){
				 update=1;
				 if(jo.getInt("pay_type")==8){
					 int r1 = daService.update("update order_tb set pay_type=? where id=? ",
								new Object[] { 8, lineid});
					 logger.error("orderid:"+lineid+" set pay_type=8 result:"+r1);
					 int r2 = daService.update("update parkuser_cash_tb set amount=? where orderid=? and type=? ",
								new Object[] { 0, lineid, 0 });
					 logger.error("orderid:"+lineid+" set parkuser_cash_tb=0 result:"+r1);
					 update=2;
				 }
			 }
			 JSONObject json = new JSONObject();
				json.put("state", "1");
				json.put("localId", jo.getLong("id"));
//				return update+"_"+jo.getLong("id");
//				return null;
				return json.toString();
		 
	}

	/**
	 * 免费订单
	 * @param orders
	 * @return
	 */
	private String freeOrder(String orders) {
		 JSONObject jo = JSONObject.fromObject(orders);
		 Long lineid = jo.getLong("line_id");//update user_info_tb set online_flag=? ,logon_time=? where id=?
		 StringBuffer insertsql = new StringBuffer("update order_tb set");//order by o.end_time desc
		 ArrayList list = new ArrayList();
		 if(!"null".equals(jo.getString("total"))){
			 insertsql.append(" total=?,");
			 list.add(jo.getDouble("total"));
		 }
		 if(!"null".equals(jo.getString("end_time"))){
			 insertsql.append(" end_time=?,");
			 list.add(jo.getLong("end_time"));
		 }
		 if(!"null".equals(jo.getString("state"))){
			 insertsql.append(" state=?,");
			 list.add(jo.getLong("state"));
		 }
		 if(!"null".equals(jo.getString("pay_type"))){
			 insertsql.append(" pay_type=?,");
			 System.out.println(jo.getInt("pay_type"));
			 list.add(jo.getInt("pay_type"));
		 }
		 if(jo.get("freereasons")!=null&&!"null".equals(jo.getString("freereasons"))&&!"".equals(jo.getString("freereasons"))){
			 insertsql.append(" freereasons=?,");
			 list.add(jo.getInt("freereasons"));
		 }
		 insertsql.append(" out_passid=?");
		 list.add(jo.getString("out_passid").equals("null")?-1:jo.getLong("out_passid"));
		 String sql = insertsql+" where id = ?";
		 list.add(lineid);
		 int update = daService.update(sql, list.toArray());
		 System.out.println(sql);
		 logger.error("本地更新免费订单ret:"+update+",orderid:"+lineid);
		 if(update==1){
			 if(jo.getLong("state")==1){
				 if(jo.getInt("pay_type")==8){
					 Long c = daService.getLong("select count(*) from parkuser_cash_tb where orderid = ?", new Object[]{lineid});
					 if(c!=null&&c>0){
						 int r = daService.update("delete from parkuser_cash_tb where orderid = ?", new Object[]{lineid});
						 logger.error("删除现金收费记录ret:"+r+",orderid:"+lineid+",amount:"+jo.getDouble("total")+",删除现金收费记录ret:"+r);
					 }
				 }
			 }
		 }
		 JSONObject json = new JSONObject();
			json.put("state", "1");
			json.put("localId", jo.getLong("id"));
//			return update+"_"+jo.getLong("id");
//			return null;
			return json.toString();
	}
	/**
	 * 预支付多了  退款
	 * @param jo
	 * @return
	 */
	private boolean backmoney(JSONObject jo){
		boolean b = false;
		 Long orderId = jo.getLong("line_id");
		 Map map = daService.getMap("select * from order_tb where id =?", new Object[]{orderId});
		 Object t = map.get("total");
		 if(map!=null&&t!=null&&Double.parseDouble(t+"")>0&&Integer.parseInt(map.get("pay_type")+"")!=4&&Integer.parseInt(map.get("pay_type")+"")!=5&&Integer.parseInt(map.get("pay_type")+"")!=6){//多余金额退回车主微信钱包
				Double prefee = Double.parseDouble(t+"");
				Double total = 0d;
				Double back = 0d;
				Double tcbback = 0d;
				Long uin = Long.parseLong(map.get("uin")+"");
				Long uid = Long.parseLong(map.get("uid")+"");
				Long comid = Long.parseLong(map.get("comid")+"");
				if(prefee>total){//多余金额退回车主微信钱包
					logger.error("预支付金额大于停车费金额,预支付金额："+prefee+",停车费金额："+total+",orderid:"+orderId+",uin:"+uin);
					List<Map<String, Object>> backSqlList = new ArrayList<Map<String,Object>>();
					DecimalFormat dFormat = new DecimalFormat("#.00");
					//如果用过三折券，就一直用三折券
					Map<String, Object> ticketMap = daService.getMap(
							"select * from ticket_tb where orderid=? order by utime limit ?",
							new Object[] { orderId,1});
					if(ticketMap != null){
						Long ticketId = (Long)ticketMap.get("id");
						logger.error("使用过券，ticketid:"+ticketId+",orderid="+orderId+",uin:"+uin);
						Double umoney = Double.valueOf(ticketMap.get("umoney")+"");
						umoney = Double.valueOf(dFormat.format(umoney));
						Double preupay = Double.valueOf(dFormat.format(prefee - umoney));
						logger.error("预支付金额prefee："+prefee+",使用券的金额umoney："+umoney+",车主实际支付的金额："+preupay+",orderid:"+orderId);
						Double tmoney = 0d;
						Integer type = (Integer)ticketMap.get("type");
						if(type == 0 || type == 1){//代金券
							tmoney = publicMethods.getTicketMoney(ticketId, 2, uid, total, 2, comid, orderId);
							logger.error("orderid:"+orderId+",uin:"+uin+",tmoney:"+tmoney);
						}else if(type == 2){
							tmoney = publicMethods.getDisTicketMoney(uin, uid, total);
							logger.error("orderid:"+orderId+",uin:"+uin);
						}
						Double upay = Double.valueOf(dFormat.format(total - tmoney));
						logger.error("实际停车费total:"+total+",实际停车费应该打折的金额tmoney:"+tmoney+",实际停车费车主实际应该支付的金额upay："+upay+",orderid:"+orderId);
						if(preupay > upay){
							back = Double.valueOf(dFormat.format(preupay - upay));
							logger.error("preupay:"+preupay+",upay:"+upay+",orderid:"+orderId+",uin:"+uin);
						}
						if(umoney > tmoney){
							tcbback = Double.valueOf(dFormat.format(umoney - tmoney));
						}
						int r = daService.update("update ticket_tb set bmoney = ? where id=? ", new Object[]{tmoney, ticketMap.get("id")});
					}else{
						logger.error("没有使用过券orderid:"+orderId+",uin:"+uin);
						back = Double.valueOf(dFormat.format(prefee - total));
					}
					logger.error("预支付退还金额:"+back+",停车券返款金额：tcbback:"+tcbback);
					if(back > 0){
						Long count = daService.getLong("select count(*) from user_info_tb where id=? ", new Object[]{uin});
						Map<String, Object> usersqlMap = new HashMap<String, Object>();
						if(count > 0){//真实帐户
							usersqlMap.put("sql", "update user_info_tb set balance=balance+? where id=? ");
							usersqlMap.put("values", new Object[]{back,uin});
							backSqlList.add(usersqlMap);
						}else{//虚拟账户
							usersqlMap.put("sql", "update wxp_user_tb set balance=balance+? where uin=? ");
							usersqlMap.put("values", new Object[]{back,uin});
							backSqlList.add(usersqlMap);
						}
						Map<String, Object> userAccountsqlMap = new HashMap<String, Object>();
						userAccountsqlMap.put("sql", "insert into user_account_tb(uin,amount,type,create_time,remark,pay_type,orderid) values(?,?,?,?,?,?,?)");
						userAccountsqlMap.put("values", new Object[]{uin,back,0,System.currentTimeMillis() / 1000 - 2,"预支付返款", 12, orderId });
						backSqlList.add(userAccountsqlMap);
						if(tcbback > 0){
							Map<String, Object> tcbbacksqlMap = new HashMap<String, Object>();
							tcbbacksqlMap.put("sql", "insert into tingchebao_account_tb(amount,type,create_time,remark,utype,orderid) values(?,?,?,?,?,?)");
							tcbbacksqlMap.put("values", new Object[]{tcbback,0,System.currentTimeMillis()/1000,"停车券返款金额",6,orderId});
							backSqlList.add(tcbbacksqlMap);
						}
						
						b = daService.bathUpdate(backSqlList);
						logger.error("预支付返款结果："+b+",orderid:"+orderId+",uin:"+uin);
					}else{
						logger.error("退还金额back小于0，orderid："+orderId+",uin:"+uin);
					}
				}
			}
		 return b;
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
//				if(e.getMessage().endsWith("存在")){
//					daService.update("delete from "+tablename +" where id = ?", new Object[]{comJo.getLong("id")});
//					r = daService.update(sql, values);
//				}
			}
		}
		 JSONObject json = new JSONObject();
			json.put("state", "1");
			json.put("cloudId", nextid);
			json.put("localId", comJo.getLong("id"));
//			return r+"_"+nextid+"_"+comJo.getLong("id");
//			return null;
			return json.toString();
	}


	/**
	 * 根据表获取所有的字段名和字段类型
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
}
