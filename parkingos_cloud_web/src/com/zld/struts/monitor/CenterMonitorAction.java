package com.zld.struts.monitor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.zld.AjaxUtil;
import com.zld.impl.*;
import com.zld.service.DataBaseService;
import com.zld.struts.dwr.DWRScriptSessionListener;
import com.zld.struts.dwr.Push;
import com.zld.utils.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.directwebremoting.ScriptSession;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;



public class CenterMonitorAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private MemcacheUtils memcacheUtils;

	private Logger logger = Logger.getLogger(CenterMonitorAction.class);
	private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
	int[] berthTimeData = new int[24];
	long[] berthPercentData = new long[24];
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		if(uin==null){
			uin = Long.parseLong(request.getParameter("loginuin"));
			request.getSession().setAttribute("loginuin",uin);
		}
		request.setAttribute("authid", request.getParameter("authid"));
		//Long cityid = (Long)request.getSession().getAttribute("cityid");
		String groupid = request.getSession().getAttribute("groupid") == null ? "" : request.getSession().getAttribute("groupid")+"";
		if("".equals(groupid)&&request.getParameter("groupid")!=null&&!"undefined".equals(request.getParameter("groupid"))){
			groupid = request.getParameter("groupid");
			request.getSession().setAttribute("groupid",groupid);
		}
		String comid = request.getSession().getAttribute("comid") == null ? "" : request.getSession().getAttribute("comid")+"";
		if("".equals(comid)&&request.getParameter("comid")!=null){
			comid = request.getParameter("comid");
			request.getSession().setAttribute("comid",comid);
		}
		//String comid = "21798";
		logger.error("中央监控>>>>>action:"+action+">>>>");
		//是否根据推送消息弹出视频
		boolean popVideo = true;
		if(action.equals("callinform")){//对讲通知消息
			/*//获取时间
			long callTime = Long.parseLong(RequestUtil.processParams(request, "t"));
			//获取呼叫信息内容
			String data = RequestUtil.processParams(request, "data");
			logger.error("中央监控>>>>>data:"+data+">>>>");
			if(!Check.isEmpty(data)){
				JSONObject json = JSONObject.fromObject(data);
				//呼叫类型3-外呼 4-呼入
				String callType = json.getString("callstyle");
				if("4".equals(callType)){
					//分机号
					String telePhone = json.getString("callerid");
					logger.error("中央监控>>>>>呼入分机号:"+telePhone+">>>>");
					Map<String, Object> monMap = daService.getMap("select p.group_phone, m.play_src from phone_info_tb p, monitor_info_tb m where m.id=p.monitor_id and p.tele_phone= ? and m.state= ? and p.state= ?", new Object[]{Long.parseLong(telePhone),1,1});
					if(monMap==null || monMap.isEmpty()){
						AjaxUtil.ajaxOutput(response, "-1");
						return null;
					}
					//主机号
					String mainPhone = json.getString("calledid");
					logger.error("中央监控>>>>>呼入主机机号:"+mainPhone+">>>>");
					//判断主机号性质 1-集团 0-车场
					int mainPhoneType = 0;
					if(Long.parseLong(mainPhone) == Long.parseLong(monMap.get("group_phone").toString())){
						mainPhoneType = 1;
					}
					logger.error("中央监控>>>>>呼入主机机号类型(1-集团 0-车场)："+mainPhoneType+">>>>");

					*//*String sql = "update phone_info_tb set call_time=?,is_call=?,main_phone_type=? where tele_phone=? ";
					int result = daService.update(sql, new Object[]{callTime, 1, mainPhoneType, Long.parseLong(telePhone)});*//*

					//推送弹视频的消息
					Map<String, Object> message = new HashMap<String,Object>();
					message.put("main_phone_type", mainPhoneType);
					message.put("play_src", monMap.get("play_src"));
		    		Push.popCenteVideo(gson.toJson(message));
				}
			}*/
			//电话Id
			String channel_uniqueid = RequestUtil.processParams(request, "channel_uniqueid");
			//主叫号
			String callerid_num = RequestUtil.processParams(request, "callerid_num");
			logger.error("中央监控>>>>>呼入主叫号:"+callerid_num+">>>>");
			//被叫号
			String exten = RequestUtil.processParams(request, "exten");
			logger.error("中央监控>>>>>呼入被叫号:"+exten+">>>>");
			Map<String, Object> monMap = daService.getMap("select p.group_phone,p.park_phone, m.play_src,m.id from phone_info_tb p, monitor_info_tb m where m.id=p.monitor_id and p.tele_phone= ? and m.state= ? and p.state= ?", new Object[]{Long.parseLong(callerid_num),1,1});
			if(monMap==null || monMap.isEmpty()){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			//判断主机号性质 1-集团 0-车场
			int mainPhoneType = -1;
			if(Long.parseLong(exten) == Long.parseLong(monMap.get("group_phone").toString())){
				mainPhoneType = 1;
			}
			if(Long.parseLong(exten) == Long.parseLong(monMap.get("park_phone").toString())){
				mainPhoneType = 0;
			}
			logger.error("中央监控>>>>>呼入主机机号类型(1-集团 0-车场)："+mainPhoneType+">>>>");
			if(mainPhoneType == -1){
				logger.error("中央监控>>>>>推送消息的主叫号>>>"+callerid_num+"和被叫号"+exten+"在云平台没有对应关系");
			}else{
				//推送弹视频的消息
				Map<String, Object> message = new HashMap<String,Object>();
				message.put("play_src", monMap.get("play_src"));
				message.put("main_phone_type", mainPhoneType);
				message.put("id", monMap.get("id"));
				logger.error("zhangqiang 中央监控>>>>>发起推送消息"+gson.toJson(message));
				Collection<ScriptSession> sessions = DWRScriptSessionListener.getScriptSessions();
				if(sessions != null && sessions.size() >0){//有dwr监听事件再推送消息
					Push.popCenteVideo(gson.toJson(message),sessions);
				}
			}
			return null;

		}
		if(uin == null){
			if(action.equals("alert")){//无账号登陆时，页面datatables 报错 invalid json response
				String ret = "{\"recordsTotal\":\"0\",\"draw\":\"0\",\"recordsFiltered\":\"0\",\"data\":[]}";
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				String jsonData =StringUtils.createJson(list);
				ret = ret.replace("[]", jsonData);
				AjaxUtil.ajaxOutput(response, ret);
				return null;
			}
			response.sendRedirect("login.do");
			return null;
		}
		//车场
		if(Check.isEmpty(comid) || "0".equals(comid)){
			comid = "";
		}
		//获取下属车场
		List<Map<String, Object>> parkList = null;
		if(Check.isEmpty(groupid) || "0".equals(groupid)){
			groupid = RequestUtil.getLong(request, "groupid_start", -1L) == -1 ? "" : RequestUtil.getLong(request, "groupid_start", -1L)+"";
		}
		if(!Check.isEmpty(groupid) && !"-1".equals(groupid)){
			parkList = commonMethods.getParkList(Long.parseLong(groupid));
			request.setAttribute("parks", parkList);
			//登录角色标志--集团
			request.setAttribute("loginSign", "group");
		}else{
			//登录角色标志--车场
			request.setAttribute("loginSign", "park");
		}
		if(action.equals("")){//主页面
			String sql = "";
			List<Object> params = new ArrayList<Object>();
			if(Check.isEmpty(comid)){//集团账号登录
				sql = "select m.* from monitor_info_tb m where m.groupid=? and m.state=? order by m.show_order asc ";
				params.add(groupid);
				params.add(1);
			}else{//车场账号登录
				sql = "select m.* from monitor_info_tb m where m.comid=? and m.state=? order by m.show_order asc ";
				params.add(comid);
				params.add(1);
			}
			List<Map<String, Object>> list = daService.getAllMap(sql, params);
			Map<String, Object> videoMap = new HashMap<String,Object>();
			if(!list.isEmpty()){
				for(int i=0; i<list.size();i++){
					videoMap.put("video"+(i+1), (String)list.get(i).get("play_src"));
					videoMap.put("monitor"+(i+1), list.get(i).get("id"));
				}
			}
			request.setAttribute("videos", videoMap);

			return mapping.findForward("list");
		}else if(action.equals("alert")){
			Long ntime = System.currentTimeMillis()/1000;
			Integer draw = RequestUtil.getInteger(request, "draw", 0);
			String sql = "";
			List<Object> params = new ArrayList<Object>();
			if(Check.isEmpty(comid)){//集团账号登录
				sql = "select co.* from confirm_order_tb co, carpic_tb cp " +
					"where co.comid=cp.comid and co.event_id=cp.event_id and co.groupid=? and co.state=? order by upload_time asc";
				params.add(groupid);
			}else{//车场账号登录
				sql = "select co.* from confirm_order_tb co, carpic_tb cp " +
					"where co.comid=cp.comid and co.event_id=cp.event_id and co.comid=? and co.state=? order by upload_time asc";
				params.add(comid);
			}
			params.add(0);
			List<Map<String, Object>> confirmOrderList = daService.getAll(sql, params,0,0);
			/*if(confirmOrderList != null && !confirmOrderList.isEmpty()){
				for(Map<String, Object> map : confirmOrderList){
					if(map.get("create_time") != null){
						Long create_time = (Long)map.get("create_time");
						map.put("ctime", TimeTools.getTime_yyyyMMdd_HHmmss(create_time*1000));
					}
				}
			}*/
			String ret = "{\"recordsTotal\":\""+confirmOrderList.size()+"\",\"draw\":\""+draw+"\",\"recordsFiltered\":\""+confirmOrderList.size()+"\",\"data\":[]}";

			String jsonData =StringUtils.createJson(confirmOrderList);
			ret = ret.replace("[]", jsonData);
			AjaxUtil.ajaxOutput(response, ret);
		}else if(action.equals("monitorManager")){//监控器管理子页面
			Integer iframe = RequestUtil.getInteger(request, "iframe", 0);
			request.setAttribute("iframe", iframe);
			return mapping.findForward("monitorList");
		}else if(action.equals("quickquery")){//快速查询监控器列表
			//获取车场监控器列表
			String orderfield = RequestUtil.processParams(request, "orderfield");
			String orderby = RequestUtil.processParams(request, "orderby");
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			List<Object> params = new ArrayList<Object>();
			if("".equals(orderfield)){
				orderfield = " id";
			}
			if("".equals(orderby)){
				orderby = " desc nulls last";
			}else {
				orderby += " nulls last";
			}
			String sql = "";
			String countsql = "";
			if(Check.isEmpty(comid)){//集团账号登录
				sql = "select m.* from monitor_info_tb m where  groupid=? and m.state=? order by m." + orderfield + " " + orderby+" , m.id desc ";
				countsql = "select count(*) from monitor_info_tb where groupid=? and state=?";
				params.add(groupid);
				params.add(1);
			}else{//车场账号登录
				sql = "select m.* from monitor_info_tb m where m.comid= ? and m.state=? order by m." + orderfield + " " + orderby+" , m.id desc ";
				countsql = "select count(*) from monitor_info_tb where comid= ? and state=?";
				params.add(comid);
				params.add(1);
			}
			Long count = daService.getCount(countsql, params);
			if(count > 0){
				list = daService.getAll(sql, params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("createMonitor")){
			String channel_id = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "channel_id"));
			String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
			String play_src = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "play_src"));
			String parkid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "comid"));
			if(Check.isEmpty(parkid) || "-1".equals(parkid)){
				parkid = comid;
			}
			//获取集团编号
			String qryGroupId = "-1";
			if(Check.isEmpty(groupid)){
				Map parkMap = daService.getMap("select * from com_info_tb where id=? ", new Object[]{Long.parseLong(parkid)});
				if(parkMap.get("groupid")!=null && Long.parseLong(parkMap.get("groupid")+"")!=-1L){
					qryGroupId = parkMap.get("groupid")+"";
				}
			}else{
				qryGroupId = groupid;
			}
			Integer is_show = RequestUtil.getInteger(request, "is_show", 1);
			Integer show_order =RequestUtil.getInteger(request, "show_order",1);
			int r = daService.update("insert into monitor_info_tb(name,channel_id,play_src,is_show,comid,groupid,show_order,state) values(?,?,?,?,?,?,?,?)",
							new Object[] {name, Long.parseLong(channel_id==""? "-1":channel_id), play_src, is_show, String.valueOf(parkid),qryGroupId,show_order,1});
			if(r==1)
				mongoDbUtils.saveLogs( request,0, 2, "添加了监控器，播放地址地址："+play_src+",通道id:"+channel_id+",车场编号:"+comid+",集团编号:"+groupid);
			AjaxUtil.ajaxOutput(response, r+"");
			return null;
		}else if(action.equals("qryMonitor")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List ret = query(request,comid,groupid);
			List list = (List) ret.get(0);
			Long count =  (Long) ret.get(1);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("qryParks")){
			List<Map<String, Object>> parks = new ArrayList<Map<String,Object>>();
			if(!Check.isEmpty(groupid) && !"-1".equals(groupid)){
				parks = commonMethods.getParkList(Long.parseLong(groupid));
			}
			String json = StringUtils.createJson(parks);
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("qryChannels")){
			String parkid = URLDecoder.decode(RequestUtil.getString(request, "comid"),"UTF-8");
			if(Check.isEmpty(parkid)){
				parkid = comid;
			}
			List<Map<String, Object>> channels = commonMethods.getChannels(parkid,groupid);
			String json = StringUtils.createJson(channels);
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("qryChannelByMonitId")){
			Long monitor_id = RequestUtil.getLong(request, "monitor_id",-1L);
			Map channleMap = daService.getMap("select mi.*,cp.passname,cp.channel_id as channelid  from monitor_info_tb mi,com_pass_tb cp where cp.id = mi.channel_id and mi.id=? ", new Object[]{monitor_id});
			String json = StringUtils.createJson(channleMap);
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("editMonitor")){
			String channel_id =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "channel_id"));
			String name =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
			Integer is_show =RequestUtil.getInteger(request, "is_show",1);
			Integer show_order =RequestUtil.getInteger(request, "show_order",1);
			String play_src =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "play_src"));
			Long id =RequestUtil.getLong(request, "id", -1L);
			Long count = daService.getLong("select count(*) from monitor_info_tb where id=? ", new Object[]{id});
			if(count == 0){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			String sql = "update monitor_info_tb set name=?,channel_id=?,is_show=?,play_src=?, show_order=? where id=? ";
			int result = daService.update(sql, new Object[]{name,Long.parseLong(channel_id==""? "-1":channel_id),is_show,play_src,show_order,id});
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("delMonitor")){
			Long monitor_id = RequestUtil.getLong(request, "selids", -1L);
			if(monitor_id == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Map shopMap = daService.getMap("select * from monitor_info_tb where id=? ", new Object[]{monitor_id});
			int r = daService.update("update monitor_info_tb set state=? where id=? ",
					new Object[] {0, monitor_id });
			if(r==1)
				mongoDbUtils.saveLogs( request,0, 4, "删除了监控："+shopMap);
			AjaxUtil.ajaxOutput(response, r+"");
		}else if(action.equals("phoneManager")){//对讲管理页面
			return mapping.findForward("phoneList");
		}else if(action.equals("quickqueryPhone")){//快速查询对讲列表
			//获取车场对讲列表
			String orderfield = RequestUtil.processParams(request, "orderfield");
			String orderby = RequestUtil.processParams(request, "orderby");
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			List<Object> params = new ArrayList<Object>();
			if("".equals(orderfield)){
				orderfield = " id";
			}
			if("".equals(orderby)){
				orderby = " desc nulls last";
			}else {
				orderby += " nulls last";
			}
			String sql = "";
			String countsql = "";
			if(Check.isEmpty(comid)){//集团账号登录
				sql = "select m.* from phone_info_tb m where groupid=? and m.state=? order by m." + orderfield + " " + orderby+" , m.id desc ";
				countsql = "select count(*) from phone_info_tb where groupid=? and state=?";
				params.add(groupid);
				params.add(1);
			}else{//车场账号登录
				sql = "select m.* from phone_info_tb m where m.comid= ? and m.state=? order by m." + orderfield + " " + orderby+" , m.id desc ";
				countsql = "select count(*) from phone_info_tb where comid= ? and state=?";
				params.add(comid);
				params.add(1);
			}
			Long count = daService.getCount(countsql, params);
			if(count > 0){
				list = daService.getAll(sql, params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("createPhone")){
			String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
			Long tele_phone = RequestUtil.getLong(request, "tele_phone", 0L);
			String parkid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "comid"));
			if(Check.isEmpty(parkid) || "-1".equals(parkid)){
				parkid = comid;
			}
			//获取集团编号
			String qryGroupId = "-1";
			if(Check.isEmpty(groupid)){
				Map parkMap = daService.getMap("select * from com_info_tb where id=? ", new Object[]{Long.parseLong(parkid)});
				if(parkMap.get("groupid")!=null && Long.parseLong(parkMap.get("groupid")+"")!=-1L){
					qryGroupId = parkMap.get("groupid")+"";
				}
			}else{
				qryGroupId = groupid;
			}
			Long park_phone = RequestUtil.getLong(request, "park_phone",0L);
			Long group_phone = RequestUtil.getLong(request, "group_phone",0L);
			Integer monitor_id = RequestUtil.getInteger(request, "monitor_id", 0);
			int r = daService.update("insert into phone_info_tb(name,tele_phone,comid,park_phone,group_phone,monitor_id,groupid,state) values(?,?,?,?,?,?,?,?)",
							new Object[] {name,tele_phone,parkid,park_phone,group_phone,monitor_id,qryGroupId,1});
			if(r==1)
				mongoDbUtils.saveLogs( request,0, 2, "添加了对讲，名称："+name+",分机号:"+tele_phone+",车场编号:"+parkid+",车场主机号:"+park_phone
											+",集团主机号:"+group_phone+",监控编号:"+monitor_id);
			AjaxUtil.ajaxOutput(response, r+"");
			return null;
		}else if(action.equals("qryPhone")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List ret = queryPhone(request,comid,groupid);
			List list = (List) ret.get(0);
			Long count =  (Long) ret.get(1);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("editPhone")){
			String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
			Long tele_phone = RequestUtil.getLong(request, "tele_phone", 0L);
			String parkid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "comid"));
			if(Check.isEmpty(parkid) || "-1".equals(parkid)){
				parkid = comid;
			}
			Long park_phone = RequestUtil.getLong(request, "park_phone",0L);
			Long group_phone = RequestUtil.getLong(request, "group_phone",0L);
			Integer monitor_id = RequestUtil.getInteger(request, "monitor_id", 0);
			Long id =RequestUtil.getLong(request, "id", -1L);
			Long count = daService.getLong("select count(*) from phone_info_tb where id=? ", new Object[]{id});
			if(count == 0){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			String sql = "update phone_info_tb set name=?,tele_phone=?,comid=?,park_phone=?, group_phone=?,monitor_id=? where id=? ";
			int result = daService.update(sql, new Object[]{name,tele_phone,parkid,park_phone,group_phone,monitor_id,id});
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("delPhone")){
			Long phone_id = RequestUtil.getLong(request, "selids", -1L);
			if(phone_id == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Map shopMap = daService.getMap("select * from phone_info_tb where id=? ", new Object[]{phone_id});
			int r = daService.update("update phone_info_tb set state=? where id=? ",
					new Object[] {0, phone_id });
			if(r==1)
				mongoDbUtils.saveLogs( request,0, 4, "删除了监控："+shopMap);
			AjaxUtil.ajaxOutput(response, r+"");
		}else if(action.equals("qryMonitors")){
			List<Map<String, Object>> monitors = commonMethods.getMonitors(comid, groupid);
			String json = StringUtils.createJson(monitors);
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("getConfirmOrders")){//获取需要人工确认的订单事件
			String sql = "";
			List<Object> params = new ArrayList<Object>();
			if(Check.isEmpty(comid)){//集团账号登录
				sql = "select * from confirm_order_tb where groupid=? and state=?";
				params.add(groupid);
			}else{//车场账号登录
				sql = "select * from confirm_order_tb where comid=? and state=?";
				params.add(comid);
			}
			params.add(0);
			List<Map<String, Object>> confirmOrderList = daService.getAll(sql, params,0,0);
			response.setHeader("Content-type", "text/html;charset=UTF-8");
	        PrintWriter out = response.getWriter();
	        out.println(JSONArray.fromObject(confirmOrderList));
	        out.flush();
	        out.close();
		}else if(action.equals("getConfirmOrder")){//获取事件图片
			System.out.println("进入获取单个图片的时间>>>>>>>>>>>"+new Date().getTime());
			String event_id = URLDecoder.decode(RequestUtil.getString(request, "event_id"),"UTF-8");
			String car_number =  URLDecoder.decode(RequestUtil.getString(request, "car_number"),"UTF-8");
			String parkid =  URLDecoder.decode(RequestUtil.getString(request, "comid"),"UTF-8");
			if(Check.isEmpty(comid) || Long.parseLong(comid)==0){
				comid = parkid;
			}
			this.getConfirmPic(event_id, Long.parseLong(comid),car_number,request,response);
			System.out.println("出来获取单个图片的时间>>>>>>>>>>>"+new Date().getTime());
		}else if(action.equals("matchConfirmOrder")){//模糊匹配订单，并找到对应进场订单
			System.out.println("进入获取多个图片的时间>>>>>>>>>>>"+new Date().getTime());
			String event_id = URLDecoder.decode(RequestUtil.getString(request, "event_id"),"UTF-8");
			String car_number = URLDecoder.decode(RequestUtil.getString(request, "car_number"),"UTF-8");
			String parkid =  URLDecoder.decode(RequestUtil.getString(request, "comid"),"UTF-8");
			if(Check.isEmpty(comid) || Long.parseLong(comid)==0){
				comid = parkid;
			}
			this.matchConfirmPic(event_id, Long.parseLong(comid),car_number,request,response);
			System.out.println("出来获取多个图片的时间>>>>>>>>>>>"+new Date().getTime());
		}else if(action.equals("querySelectOrder")){//查询选中的匹配订单
			String orderId = RequestUtil.getString(request, "order_id");
			String carNumber = URLDecoder.decode(RequestUtil.getString(request, "car_number"),"UTF-8");
			String parkid =  URLDecoder.decode(RequestUtil.getString(request, "comid"),"UTF-8");
			if(Check.isEmpty(comid) || Long.parseLong(comid)==0){
				comid = parkid;
			}
			Map retMap  = daService.getMap("select * from order_tb " +
					"where comid=? and car_number =?  and state =?   ",
					new Object[]{Long.parseLong(comid),carNumber,0});
			//Map<String, Object> retMap = publicMethods.catBolinkOrder(null,orderId,carNumber, comid,0,-1L);
			response.setHeader("Content-type", "text/html;charset=UTF-8");
	        PrintWriter out = response.getWriter();
	        out.println(JSONObject.fromObject(retMap));
	        out.flush();
	        out.close();
		}else if(action.equals("balanceOrderInfo")){//推送匹配订单通知到车场
			String orderId = RequestUtil.getString(request, "order_id");
			String carNumber = RequestUtil.getString(request, "car_number");
			String channel_id = RequestUtil.getString(request, "channel_id");
			String event_id = RequestUtil.getString(request, "event_id");
			String parkid =  URLDecoder.decode(RequestUtil.getString(request, "comid"),"UTF-8");
			if(Check.isEmpty(comid) || Long.parseLong(comid)==0){
				comid = parkid;
			}
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("comid", comid+"");
			params.put("inform_time", TimeTools.getLongMilliSeconds());
			params.put("channel_id",URLEncoder.encode(channel_id,"UTF-8"));
			params.put("order_id", orderId);
			params.put("event_id", URLEncoder.encode(event_id,"UTF-8"));
			params.put("car_number", URLEncoder.encode(carNumber,"UTF-8"));
			String message =  StringUtils.createLinkString(params);
			logger.error("balanceOrderInfo message:"+message);
			String url = "http://127.0.0.1/zld/centermonitor.do?action=balanceOrderInfo&"+message;
			//测试代码
			//String url = "http://test.bolink.club/zld/centermonitor.do?action=balanceOrderInfo&"+message;
			logger.error("balanceOrderInfo url:"+url);
			String result = new HttpProxy().doGet(url);
			logger.error("balanceOrderInfo result:"+result);
			Map<String, String> retMap = new HashMap<String, String>();
			if("1".equals(result)){
				try {
					Thread.sleep(500);//等0.5s
					//查询未处理该eventId的手动匹配订单
					Map picMap1 = daService.getMap("select * from confirm_order_tb where event_id=? and comid=? and state =?",new Object[]{event_id,String.valueOf(comid),0});
					if(picMap1 == null){
						retMap.put("succsess", "1");
						retMap.put("message", "处理成功");
						retMap.put("img", "balSuc.png");
					}else {
						Thread.sleep(500);//等0.5s
						Map picMap2 = daService.getMap("select * from confirm_order_tb where event_id=? and comid=? and state =?",new Object[]{event_id,String.valueOf(comid),0});
						if(picMap2 == null){
							retMap.put("succsess", "1");
							retMap.put("message", "处理成功");
							retMap.put("img", "balSuc.png");
						}else{
							Thread.sleep(1000);//等1s
							Map picMap3 = daService.getMap("select * from confirm_order_tb where event_id=? and comid=? and state =?",new Object[]{event_id,String.valueOf(comid),0});
							if(picMap3 == null){
								retMap.put("succsess", "1");
								retMap.put("message", "处理成功");
								retMap.put("img", "balSuc.png");
							}else{
								retMap.put("succsess", "0");
								retMap.put("message", "处理失败");
								retMap.put("img", "balFail.png");
							}
						}
					}
					//查询事件是否存在
				} catch (Exception e) {
					// TODO: handle exception
				}
			}else{
				retMap.put("succsess", "0");
				retMap.put("message", "处理失败");
				retMap.put("img", "balFail.png");
			}
			response.setHeader("Content-type", "text/html;charset=UTF-8");
	        PrintWriter out = response.getWriter();
	        out.println(JSONObject.fromObject(retMap));
	        out.flush();
	        out.close();
		}else if(action.equals("liftRod")){//抬杆通知
			Map<String, String> retMap = new HashMap<String, String>();
			String channel_id =  URLDecoder.decode(RequestUtil.getString(request, "channel_id"));
			String channel_name =  URLDecoder.decode(RequestUtil.getString(request, "channel_name"));
			String parkid =  URLDecoder.decode(RequestUtil.getString(request, "comid"),"UTF-8");
			if(Check.isEmpty(comid) || Long.parseLong(comid)==0){
				comid = parkid;
			}
			if(channel_id == null || channel_id.isEmpty()){
				retMap.put("succsess", "0");
				retMap.put("message", "处理失败，通道号为空");
			}else{
				//先初始化抬杆状态 -1
				int update = daService.update("update liftrod_info_tb set state=? where channel_id=? and operate=? and comid=? ",
						new Object[]{-1L,channel_id,0,parkid});
				logger.error("liftRod 初始化抬杆状态:"+update);
				HttpProxy httpProxy = new HttpProxy();
				String url = "http://127.0.0.1/zld/sendmsgtopark.do?action=sendliftrodmsg";
				//测试代码
				//String url = "http://test.bolink.club/zld/sendmsgtopark.do?action=sendliftrodmsg";
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("comid", comid);
				params.put("channelName", URLEncoder.encode(channel_name,"UTF-8"));
				params.put("channelId", URLEncoder.encode(channel_id,"UTF-8"));
				params.put("operate", 0);
				String result = httpProxy.doPostTwo(url, params);
				System.out.println(result);
				if("1".equals(result)){
					//发送消息后查询数据库是否完成操作
					Long state = -1L;
					for(int i=0;i<30;i++){
						Thread.sleep(100);
						Map map = daService.getMap("select state from liftrod_info_tb where channel_id=? and operate=? and comid=?",
								new Object[]{channel_id,0,String.valueOf(comid)});
						if(map != null && !map.isEmpty()){
							state = Long.valueOf(String.valueOf(map.get("state")));
						}
						if(state == 1){
							break;
						}else{
							continue;
						}
					}
					if(state == 1){
						retMap.put("succsess", "1");
						retMap.put("message", "处理成功");
					}else{
						retMap.put("succsess", "0");
						retMap.put("message", "处理失败");
					}
				}else{
					retMap.put("succsess", "0");
					retMap.put("message", "处理失败");
				}
			}
			response.setHeader("Content-type", "text/html;charset=UTF-8");
	        PrintWriter out = response.getWriter();
	        out.println(JSONObject.fromObject(retMap));
	        out.flush();
	        out.close();
		}else if(action.equals("berthpercent")){
			String json = berthPercent(request, comid, null, groupid);
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("electrade")){
			List<Object> params = new ArrayList<Object>();
			String elecSql = "";
			String countsql = "";
			Long qrytime = TimeTools.getLongMilliSecondFrom_HHMMDD(TimeTools.getDate_YY_MM_DD())/1000;//当天零时的毫秒数 
			params.add(qrytime);
			if(!Check.isEmpty(comid) && !"0".equals(comid)){
				elecSql = "select sum(electronic_pay+electronic_prepay) total from order_tb o where o.state=1 and create_time>? and o.comid=?";
		    	countsql = "SELECT COUNT (DISTINCT(o.order_id_local)) FROM order_tb o WHERE o. STATE = 1 and (o.electronic_pay>0 or o.electronic_prepay>0) and create_time>? AND o.comid =? ";
		    	params.add(Long.valueOf(comid));
			}else{
				List<Map<String, Object>> parks = commonMethods.getParkList(Long.parseLong(groupid));
				elecSql = "select sum(electronic_pay+electronic_prepay) total from order_tb o where o.state=1 and create_time>? and o.comid in (";
		    	countsql = "SELECT COUNT (DISTINCT(o.order_id_local)) FROM order_tb o WHERE o. STATE = 1 and (o.electronic_pay>0 or o.electronic_prepay>0) and create_time>? AND o.comid in (";
				for(int i=0; i<parks.size();i++){
					elecSql += " ?";
					countsql += " ?";
					if(i!=parks.size()-1){
						elecSql += ",";
						countsql += ",";
					}
					params.add(Long.parseLong(parks.get(i).get("value_no").toString()));
				}
				elecSql += " )";
				countsql += " )";
			}
	    	Long elecCount = daService.getCount(countsql, params);
	    	Map elecMap = daService.getMap(elecSql, params);
	    	//非空判断，防止页面电子交易出现NaN
	    	if(elecMap.get("total")==null){
	    		elecMap.put("total",0);
	    	}
	    	elecMap.put("count", elecCount);
	    	logger.error("初次加载------车场:"+comid+">>>>>>>电子交易集合："+elecMap);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(elecMap));
		}else if(action.equals("updateConfirmStatus")){
			Long id = RequestUtil.getLong(request, "id", 0L);
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			Map<String, Object> confimPicSqlMap = new HashMap<String, Object>();
			confimPicSqlMap.put("sql", "update confirm_order_tb set state=? where id=?");
			confimPicSqlMap.put("values", new Object[]{1,id});
			bathSql.add(confimPicSqlMap);
			boolean b = daService.bathUpdate(bathSql);
			logger.error("非真实正常处理匹配事件结果------>"+b);
			Map<String,Object> resultMap = new HashMap<String,Object>();
			resultMap.put("success", b);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(resultMap));
		}
		return null;
	}

	/**
	 * 泊位利用率
	 * @param request
	 * @param city_id,
	 * @return
	 */
	private String berthPercent(HttpServletRequest request, String com_id, String city_id, String group_id){
		try {
			long comid = -1;
			long cityid = -1;
			long groupid = -1;
			if(!Check.isEmpty(com_id)){
				comid = Long.parseLong(com_id);
			}
			if(!Check.isEmpty(city_id)){
				cityid = Long.parseLong(city_id);
			}
			if(!Check.isEmpty(group_id)){
				groupid = Long.parseLong(group_id);
			}
			Long ntime = System.currentTimeMillis()/1000;
			String sql = "select sum(share_count) asum,sum(used_count) usum, create_time from park_anlysis_tb where ";
			List<Object> params = new ArrayList<Object>();
			List<Object> parks = null;
			if(comid > 0){
				parks = new ArrayList<Object>();
				parks.add(comid);
			}else if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				params.addAll(parks);
				params.add(ntime - 24*60*60);
				sql += " comid in ("+preParams+") and create_time>? group by create_time order by create_time asc";
				//sql += " comid in ("+preParams+") group by create_time";
				List<Map<String, Object>> list = daService.getAllMap(sql, params);
				List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
				if(list != null && !list.isEmpty()){
					for(Map<String, Object> map : list){
						Long create_time = (Long)map.get("create_time");
						map.put("time",TimeTools.getDateFromString(TimeTools.getTime_yyyyMMdd_HHmm(create_time*1000),"yyyy-MM-dd HH:mm").getHours());
						Long asum = 0L;
						Long usum = 0L;
						if(map.get("asum") != null){
							asum = (Long)map.get("asum");
						}
						if(map.get("usum") != null){
							usum = (Long)map.get("usum");
						}
						Long rate = (usum*100/asum) > 100 ? 100 : (usum*100/asum);
						map.put("percent",Math.round(StringUtils.formatDouble(rate)));
					}
				}
				//判断是否需要刷新泊位图
				boolean refreshBerth = checkRefreshBerth(list,berthTimeData,berthPercentData);
				for(Map<String, Object> map : list){
					map.put("refreshBerth",refreshBerth);
				}
				String json = StringUtils.createJson(list);
				return json;
			}
		} catch (Exception e) {
			logger.error("berthPercent", e);
		}
		return "[]";
	}

	/**
	 * 判断是否需要刷新泊位图
	 * @param list
	 * @param berthTimeData
	 * @param berthPercentData
	 * @return
	 */
	private boolean checkRefreshBerth(List<Map<String, Object>> list,int[] berthTimeData,long[] berthPercentData){
		boolean flag = false;
		if(berthPercentData[0] == 0L){//除了初始化，百分比不可能为0
			flag = true;
		}
		for(int i=0;i<list.size();i++){
			Map<String, Object> map = list.get(i);
			if(flag){
				berthTimeData[i] = (int)map.get("time");
				berthPercentData[i] = (long)map.get("percent");
			}else{
				if((int)map.get("time") != berthTimeData[i] || (long)map.get("percent") != berthPercentData[i]){
					flag = true;
					berthTimeData[i] = (int)map.get("time");
					berthPercentData[i] = (long)map.get("percent");
				}
			}

		}
		return flag;
	}
	/**
	 * 获取要匹配的车辆图片
	 * @param comid
	 * @param carNumber
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private void getConfirmPic(String eventId, Long comid,String carNumber,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.error("getConfirmPic from mongodb>>>>>>>>>eventId="+eventId+">>>>>>>comid="+comid);
		if(eventId!=null){
			DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
			//查询出mongodb中存入的对应个表名
			Map picMap = daService.getMap("select * from carpic_tb where event_id=? and comid=?", new Object[]{eventId,String.valueOf(comid)});
			String collectionName = "";
			if(picMap !=null && !picMap.isEmpty()){
				collectionName = (String) picMap.get("confirmpic_table_name");
			}
			if(collectionName==null||"".equals(collectionName)||"null".equals(collectionName)){
				logger.error(">>>>>>>>>>>>>查询图片错误........");
				response.sendRedirect("http://test.bolink.club/zld/images/nocar.png");
				return;
			}
			logger.error("table:"+collectionName);
			DBCollection collection = db.getCollection(collectionName);
			if(collection != null){
				BasicDBObject document = new BasicDBObject();
				document.put("parkid", String.valueOf(comid));
				document.put("event_id", eventId);
				DBObject obj  = collection.findOne(document);
				if(obj == null){
					AjaxUtil.ajaxOutput(response, "");
					logger.error("取图片错误.....");
					return ;
				}
				byte[] content = (byte[])obj.get("content");
				logger.error("取图片成功.....大小:"+content.length);
				//通道号
				String event_id = (String)obj.get("event_id");
				try {
					String foldPath = request.getServletContext().getRealPath("/images/monitor/");
					File folder = new File(foldPath);
					if(!folder.exists() || !folder.isDirectory()){
						folder.mkdirs();
					}
					 InputStream in = new ByteArrayInputStream(content);
					 String filePath = request.getServletContext().getRealPath("/images/monitor/"+comid+"_"+eventId+"_"+carNumber.substring(1)+".jpg");
		             File file=new File(filePath);//可以是任何图片格式.jpg,.png等
		             FileOutputStream fos=new FileOutputStream(file);
		             byte[] b = new byte[1024*8];
		             int nRead = 0;
		             while ((nRead = in.read(b)) != -1) {
		                  fos.write(b, 0, nRead);
		              }
		              fos.flush();
		              fos.close();
		              in.close();
		              response.setHeader("Content-type", "text/html;charset=UTF-8");
		              PrintWriter out = response.getWriter();
		              JSONObject retObj = new JSONObject();
		              retObj.put("picName", comid+"_"+event_id+"_"+carNumber.substring(1)+".jpg");
		              retObj.put("event_id", event_id);
		              retObj.put("car_nmber", carNumber);
		              /*out.println(retObj);  
		              out.flush();  
		              out.close(); */
		              AjaxUtil.ajaxOutput(response, retObj.toString());
		              return ;
				} catch (Exception e) {
					// TODO: handle exception
					logger.info(e.toString());
				}
			    System.out.println("mongdb over.....");
			}else{
				return ;
			}
		}else {
			return;
		}
		return;
	}


	/**
	 * 模糊匹配进场车辆图片
	 * @param comid
	 * @param carNumber
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private void matchConfirmPic(String eventId, Long comid,String carNumber,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.error("matchConfirmPic from mongodb>>>>>>>>>channelId="+eventId+">>>>>>>comid="+comid+">>>>>>>>>carnumber"+carNumber);
		//获取需要人工确认的订单事件
		//List<Map<String, Object>> mactchOrderList = daService.getAll("select * from order_tb where comid =?  and car_number like ?" +
		//		" and state=? order by id desc", new Object[]{comid,"%"+carNumber.substring(1),0});
		List<Map<String, Object>> mactchOrderList = queryBlurOrdersByCarnumber(comid,carNumber);
		JSONArray jsonArray = new JSONArray();
		if(mactchOrderList == null || mactchOrderList.isEmpty()){
			/*PrintWriter out = response.getWriter();  
	        out.println(jsonArray);  
	        out.flush();  
	        out.close(); */
			AjaxUtil.ajaxOutput(response, jsonArray.toString());
			return;
		}
		for(int i=0; i<mactchOrderList.size();i++){
			System.out.println("进入获取多个图片开始进行第"+(i+1)+"次循环的时间>>>>>>>>>>>"+new Date().getTime());
			Map<String, Object> orderMap = mactchOrderList.get(i);
			if(orderMap.get("order_id_local")==null){
				continue;
			}
			String orderid = (String)orderMap.get("order_id_local");
			String car_number = (String)orderMap.get("car_number");
			DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
			//根据订单编号查询出mongodb中存入的对应个表名
			Map map = daService.getMap("select * from carpic_tb where order_id=? and comid=?", new Object[]{orderid,String.valueOf(comid)});
			String collectionName = "";
			if(map !=null && !map.isEmpty()){
				collectionName = (String) map.get("carpic_table_name");
			}
			if(collectionName==null||"".equals(collectionName)||"null".equals(collectionName)){
				logger.error(">>>>>>>>>>>>>根据车牌"+carNumber+"匹配到orderid"+orderid+"查询图片错误........");
				continue;
			}
			logger.error(">>>>>>>>>>>>>根据车牌"+carNumber+"匹配到orderid"+orderid+">>>>>table:"+collectionName);
			DBCollection collection = db.getCollection(collectionName);
			if(collection != null){
				BasicDBObject document = new BasicDBObject();
				document.put("parkid", String.valueOf(comid));
				document.put("orderid", orderid);
				document.put("gate", "in");
				DBObject obj  = collection.findOne(document);
				if(obj == null){
					//AjaxUtil.ajaxOutput(response, "");
					logger.error(">>>>>>>>>>>>>根据车牌"+carNumber+"匹配到orderid"+orderid+"取图片错误.....");
					continue;
				}
				byte[] content = (byte[])obj.get("content");
				System.out.println("进入获取多个图片开始第"+(i+1)+"次循环得到图片的时间>>>>>>>>>>>"+new Date().getTime());
				logger.error(">>>>>>>>>>>>>根据车牌"+carNumber+"匹配到orderid"+orderid+"取图片成功.....大小:"+content.length);
				db.requestDone();
				try {
					String foldPath = request.getServletContext().getRealPath("/images/monitor/");
					File folder = new File(foldPath);
					if(!folder.exists() || !folder.isDirectory()){
						folder.mkdirs();
					}
					 InputStream in = new ByteArrayInputStream(content);
					 String filePath = request.getServletContext().getRealPath("/images/monitor/"+comid+"_"+orderid+".jpg");
		             File file=new File(filePath);//可以是任何图片格式.jpg,.png等
		             FileOutputStream fos=new FileOutputStream(file);
		             byte[] b = new byte[1024*8];
		             int nRead = 0;
		             while ((nRead = in.read(b)) != -1) {
		                  fos.write(b, 0, nRead);
		              }
		              fos.flush();
		              fos.close();
		              in.close();
		              System.out.println("进入获取多个图片开始第"+(i+1)+"次循环生成图片的时间>>>>>>>>>>>"+new Date().getTime());
		              JSONObject jsonObject = new JSONObject();
		              jsonObject.put("orderId", orderid);
		              jsonObject.put("carNumber", car_number);
		              jsonObject.put("picName", comid+"_"+orderid+".jpg");
		              jsonArray.add(jsonObject);
				} catch (Exception e) {
					// TODO: handle exception
					logger.info(e.toString());
				}
			    System.out.println("mongdb over.....");
			}
		}

		response.setHeader("Content-type", "text/html;charset=UTF-8");
        /*PrintWriter out = response.getWriter();  
        out.println(jsonArray);  
        out.flush();  
        out.close(); */
		AjaxUtil.ajaxOutput(response, jsonArray.toString());
		return;
	}

	/**
     * 二进制转字符串
     * @param b
     * @return
     */
    public static String byte2hex(byte[] b) // 二进制转字符串
    {
        StringBuffer sb = new StringBuffer();
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1) {
                sb.append("0" + stmp);
            } else {
                sb.append(stmp);
            }

        }
        return sb.toString();
    }
    /**
     * 根据车牌号模糊查询在场订单
     * @param comid
     * @param carNumber
     * @return
     */
    private  List<Map<String, Object>> queryBlurOrdersByCarnumber(Long comid,String carNumber){
    	List<Object> params = new ArrayList<Object>();
    	params.add(comid);
    	List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
    	//匹配除省份，无牌车首字符可能不是中文(c >= 0x4E00 &&  c <= 0x9FA5)
    	char firstChar = carNumber.charAt(0);
    	if(firstChar>0x9FA5 || firstChar<0x4E00){
    		carNumber="无"+carNumber;
    	}
    	//1 模糊匹配除去一位汉字
    	params.add("%"+carNumber.substring(1));
    	params.add(0);
    	list = daService.getAllMap("select * from order_tb where comid =? and car_number like ?"
    			+" and state=? order by create_time desc", params);
    	if(list == null || list.size() == 0){
    		// 2 模糊匹配除去两位
    		params = params.subList(0, 1);
			params.add(1,"%"+carNumber.substring(1,carNumber.length()-1)+"%");
			params.add(2,"%"+carNumber.substring(2)+"%");
			params.add(0);
        	list = daService.getAllMap("select * from order_tb where comid =? and (car_number like ? or car_number like ? )"
        			+" and state=? order by create_time desc", params);
        	if(list == null || list.size() == 0){
        		//3  模糊匹配除去三位
            	params = params.subList(0, 1);
            	params.add(1,"%"+carNumber.substring(1,carNumber.length()-2)+"%");
        		params.add(2,"%"+carNumber.substring(2,carNumber.length()-1)+"%");
        		params.add(3,"%"+carNumber.substring(3,carNumber.length())+"%");
        		params.add(0);
        		list = daService.getAllMap("select * from order_tb where comid =? and"
            			+" (car_number like ? or car_number like ? or car_number like ? )"
            			+" and state=? order by create_time desc", params);
        		if(list == null || list.size() == 0){
            		//4 模糊匹配除去四位
                	params = params.subList(0, 1);
                	params.add(1,"%"+carNumber.substring(1,carNumber.length()-3)+"%");
            		params.add(2,"%"+carNumber.substring(2,carNumber.length()-2)+"%");
            		params.add(3,"%"+carNumber.substring(3,carNumber.length()-1)+"%");
            		params.add(4,"%"+carNumber.substring(4,carNumber.length())+"%");
            		params.add(0);
            		list = daService.getAllMap("select * from order_tb where comid =? and"
                			+" (car_number like ? or car_number like ? or car_number like ? or car_number like ?)"
                			+" and state=? order by create_time desc", params);
            	}
        	}
    	}
    	return list;
    }

    private List query(HttpServletRequest request,String comid, String groupid){
		String orderfield = RequestUtil.processParams(request, "orderfield");
		String orderby = RequestUtil.processParams(request, "orderby");
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
		List<Object> ret = new ArrayList<Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		String sql = "";
		String countsql = "";
		List<Object> params = new ArrayList<Object>();
		if(Check.isEmpty(comid)){//集团账号登录
			sql = "select m.* from monitor_info_tb m where m.groupid=? and m.state=?";
			countsql = "select count(*) from monitor_info_tb m where groupid=? and m.state=?";
			params.add(groupid);
			params.add(1);
		}else{//车场账号登录
			sql = "select m.* from monitor_info_tb m where m.comid= ? and m.state=?";
			countsql = "select count(*) from monitor_info_tb m where m.comid= ? and m.state=?";
			params.add(comid);
			params.add(1);
		}
		SqlInfo base = new SqlInfo("1=1", new Object[]{});
		SqlInfo sqlInfo = RequestUtil.customSearch(request,"monitor_info_tb","m",null);
		if(sqlInfo != null ){
			sqlInfo = SqlInfo.joinSqlInfo(base,sqlInfo, 2);
			sql += " and " + sqlInfo.getSql();
			countsql += " and " + sqlInfo.getSql();
			params.addAll(sqlInfo.getParams());
		}else{
			params.addAll(base.getParams());
		}
		if("".equals(orderfield)){
			orderfield = " id";
		}
		if("".equals(orderby)){
			orderby = " desc nulls last";
		}else {
			orderby += " nulls last";
		}
		sql += " order by m." + orderfield + " " + orderby+", m.id desc ";
		Long count = daService.getCount(countsql, params);
		list = daService.getAll(sql, params, pageNum, pageSize);
		ret.add(list);
		ret.add(count);
		return ret;
	}
    private List queryPhone(HttpServletRequest request,String comid, String groupid){
		String orderfield = RequestUtil.processParams(request, "orderfield");
		String orderby = RequestUtil.processParams(request, "orderby");
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
		List<Object> ret = new ArrayList<Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		List<Object> params = new ArrayList<Object>();
		String sql = "";
		String countsql = "";
		if(Check.isEmpty(comid)){//集团账号登录
			sql = "select m.* from phone_info_tb m where m.groupid=? and m.state=?";
			countsql = "select count(*) from phone_info_tb m where groupid=? and m.state=?";
			params.add(groupid);
			params.add(1);
		}else{//车场账号登录
			sql = "select m.* from phone_info_tb m where m.comid= ? and m.state=?";
			countsql = "select count(*) from phone_info_tb m where m.comid= ? and m.state=?";
			params.add(comid);
			params.add(1);
		}
		SqlInfo base = new SqlInfo("1=1", new Object[]{comid,groupid});
		SqlInfo sqlInfo = RequestUtil.customSearch(request,"phone_info_tb","m",null);
		if(sqlInfo != null ){
			sqlInfo = SqlInfo.joinSqlInfo(base,sqlInfo, 2);
			sql += " and " + sqlInfo.getSql();
			countsql += " and " + sqlInfo.getSql();
			params.addAll(sqlInfo.getParams());
		}else{
			params.addAll(base.getParams());
		}
		if("".equals(orderfield)){
			orderfield = " id";
		}
		if("".equals(orderby)){
			orderby = " desc nulls last";
		}else {
			orderby += " nulls last";
		}
		sql += " order by m." + orderfield + " " + orderby+", m.id desc ";
		Long count = daService.getCount(countsql, params);
		list = daService.getAll(sql, params, pageNum, pageSize);
		ret.add(list);
		ret.add(count);
		return ret;
	}
}
