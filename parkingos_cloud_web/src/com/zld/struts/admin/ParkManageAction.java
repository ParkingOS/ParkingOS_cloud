package com.zld.struts.admin;

import com.zld.AjaxUtil;
import com.zld.CustomDefind;
import com.zld.impl.CommonMethods;
import com.zld.impl.MemcacheUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.LogService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
/**
 * 总管理员   停车场注册修改删除等
 * @author Administrator
 *
 */
public class ParkManageAction extends Action {

	@Autowired
	private DataBaseService daService;
	@Autowired
	private LogService logService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Autowired
	private PgOnlyReadService onlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	Logger logger = Logger.getLogger(ParkManageAction.class);


	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {


		String action = RequestUtil.processParams(request, "action");
		Integer state = RequestUtil.getInteger(request, "state", 0);
		String userId = (String)request.getSession().getAttribute("userid");
		Long comId = (Long)request.getSession().getAttribute("comid");
		request.setAttribute("authid", request.getParameter("authid"));
		if(request.getParameter("state_start")!=null)
			state = RequestUtil.getInteger(request, "state_start", 0);
		if(state==-1)
			state=0;
		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("ugc")){
			if(state==0)//已审核UGC停车场
				return mapping.findForward("ugclist");
			else {//未审核UGC停车场
				return mapping.findForward("ugcverify");
			}
		}else if(action.equals("unionparks")){
			return mapping.findForward(action);
		}else if(action.equals("uploadparks")){
			request.setAttribute("unionId", CustomDefind.getValue("UNIONID"));
			request.setAttribute("serverId", CustomDefind.getValue("SERVERID"));
			request.setAttribute("unionKey", CustomDefind.getValue("UNIONKEY"));
			return mapping.findForward(action);
		}else if(action.equals("queryunionpark")){
			String sql = "select * from com_info_tb  where union_state>? ";
			String countSql = "select count(*) from com_info_tb where union_state>?  " ;
			Long count = daService.getLong(countSql,new Object[]{0});
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;//daService.getPage(sql, null, 1, 20);
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			if(count>0){
				list = daService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("uploadpark")){
			String sql = "select * from com_info_tb  where state =? and union_state=?" +
					" and isfixed=? and longitude>? and latitude>? and parking_total> ?";
			String countSql = "select count(*) from com_info_tb where  state =? and union_state=?" +
					" and isfixed= ? and longitude>? and latitude>? and parking_total> ?" ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;//daService.getPage(sql, null, 1, 20);
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(0);
			params.add(1);
			params.add(0.0);
			params.add(0.0);
			params.add(0);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_info");
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			Long count = daService.getCount(countSql,params);
			if(count>0){
				list = daService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("sendparktounion")){
			String ids = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "seleids"));
			String union_id = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "union_id"));
			String union_key = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "union_key"));
			String server_id = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "server_id"));
			String []sids = ids.split(",");
			int uploadCount = 0;
			int unUploadCount=0;
			if(sids.length>0){
				Object[] params = new Object[sids.length];
				String paramStr ="";
				for(int i=0;i<sids.length;i++){
					params[i]=Long.valueOf(sids[i]);
					if(i>0)
						paramStr+=",";
					paramStr+="?";
				}
				List<Map<String, Object>> list = onlyReadService.getAll(
						"select id,address,company_name,phone,longitude,latitude,parking_total,remarks " +
								"from com_info_tb where id in ("+paramStr+")",params);
				if(list!=null&&list.size()>0){
					for(Map<String, Object> map : list){
						//String url = "https://127.0.0.1/api-web/park/addpark";
						String url = CustomDefind.UNIONIP+"park/addpark";
						//String url = "https://s.bolink.club/unionapi/park/addpark";
						Map<String, Object> paramMap = new HashMap<String, Object>();
						paramMap.put("park_id", map.get("id"));
						paramMap.put("name", map.get("company_name"));
						paramMap.put("address", map.get("address"));
						paramMap.put("phone", map.get("phone"));
						paramMap.put("lng",  map.get("longitude")+"");
						paramMap.put("lat",  map.get("latitude")+"");
						paramMap.put("total_plot",  map.get("parking_total"));
						paramMap.put("empty_plot",  map.get("parking_total"));
						paramMap.put("price_desc", getPrice(Long.valueOf(map.get("id")+"")));
						paramMap.put("remark",map.get("remarks"));
						paramMap.put("union_id", union_id);
						paramMap.put("server_id", server_id);
						paramMap.put("rand", Math.random());
						String ret = "";
						try {
							logger.error(paramMap);
							String linkParams = StringUtils.createLinkString(paramMap)+"key="+union_key;
							System.out.println(linkParams);
							String sign =StringUtils.MD5(linkParams).toUpperCase();
							logger.error(sign);
							paramMap.put("sign", sign);
							//param = DesUtils.encrypt(param,"NQ0eSXs720170114");
							String param = StringUtils.createJson(paramMap);
							logger.error(param);
							ret = HttpsProxy.doPost(url, param, "utf-8", 20000, 20000);
							JSONObject object = new JSONObject(ret);
							logger.error("陈博文"+object);
							if(object!=null){
								Integer uploadState = object.getInt("state");
								if(uploadState==1){
									daService.update("update com_info_tb set upload_union_time=?,union_state=? " +
											"where id =?", new Object[]{System.currentTimeMillis()/1000,2,map.get("id")});
									uploadCount++;
								}else {
									logger.error(object.get("errmsg"));
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						logger.error(ret);
					}
					unUploadCount = list.size()-uploadCount;
				}
			}
			AjaxUtil.ajaxOutput(response, "上传"+sids.length+"个车场，成功"+uploadCount+"个，未成功"+unUploadCount+"个");
		}else if(action.equals("getUnionInfo")){
			Long park_id = RequestUtil.getLong(request, "park_id",-1L);
			Map unionInfo = daService.getMap("select oc.union_id, oc.ukey union_key, og.serverid server_id from org_city_merchants oc " +
					"left outer join org_group_tb og on oc.id = og.cityid " +
					"left outer join com_info_tb co on co.groupid = og.id " +
					"where co.id = ?", new Object[]{park_id});
			if(unionInfo==null || unionInfo.isEmpty() || unionInfo.get("union_id") == null){
				unionInfo = new HashMap();
				unionInfo.put("union_id", "");
				unionInfo.put("union_key", "");
				unionInfo.put("server_id", "");
			}
			String json = JsonUtil.createJsonforMap(unionInfo);
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("quickquery")){
			String sql = "select * from com_info_tb  where state=? and upload_uin= ? ";
			String countSql = "select count(*) from com_info_tb where state=? and upload_uin= ? " ;
			Long count = daService.getLong(countSql,new Object[]{state,-1});
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;//daService.getPage(sql, null, 1, 20);
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(state);
			params.add(-1);
			if(count>0){
				list = daService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("query")){
			Integer ptype = RequestUtil.getInteger(request, "ptype", 0);
			String sql = "select c.* from com_info_tb c where c.state=? ";
			String countSql = "select count(c.*) from com_info_tb c where c.state=?  ";
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_info", "c", new String[]{});
			Long no_marketer = RequestUtil.getLong(request, "no_marketer_start", -1L);
			List<Object> params = new ArrayList<Object>();
			params.add(state);
			if(ptype==0){
				sql +=" and upload_uin=? ";
				countSql +=" and upload_uin=? ";
				params.add(-1);
			}else {//查UGC车场
				sql +=" and upload_uin>? ";
				countSql +=" and upload_uin>? ";
				params.add(1);
			}
			if(no_marketer == 0){
				sql = "select c.* from com_info_tb c left join user_info_tb u on c.uid=u.id where c.state=? and (c.uid is null or u.state=?) ";
				countSql = "select count(c.*) from com_info_tb c left join user_info_tb u on c.uid=u.id where c.state=? and (c.uid is null or u.state=?) ";
				sqlInfo = RequestUtil.customSearch(request, "com_info", "c", new String[]{});
				params.add(1);
			}
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			//System.out.println(sqlInfo);
			Long count= daService.getCount(countSql, params);
			List list = null;//daService.getPage(sql, null, 1, 20);
			if(count>0){
				list = daService.getAll(sql+" order by c.id desc ", params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("ugcquery")){
			String sql = "select c.* from com_info_tb c where c.state=? and upload_uin>? ";
			String countSql = "select count(c.*) from com_info_tb c where c.state=? and upload_uin>? ";
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_info", "c", new String[]{});
			Long no_marketer = RequestUtil.getLong(request, "no_marketer_start", -1L);
			List<Object> params = new ArrayList<Object>();
			params.add(2);
			params.add(-1);
			if(no_marketer == 0){
				sql = "select c.* from com_info_tb c left join user_info_tb u on c.uid=u.id where c.state=? and (c.uid is null or u.state=?) ";
				countSql = "select count(c.*) from com_info_tb c left join user_info_tb u on c.uid=u.id where c.state=? and (c.uid is null or u.state=?) ";
				sqlInfo = RequestUtil.customSearch(request, "com_info", "c", new String[]{});
				params.add(1);
			}
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			//System.out.println(sqlInfo);
			Long count= daService.getCount(countSql, params);
			List list = null;//daService.getPage(sql, null, 1, 20);
			if(count>0){
				list = daService.getAll(sql+" order by c.id desc ", params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			String from = RequestUtil.processParams(request, "from");
			Double longitude =RequestUtil.getDouble(request, "longitude",0d);
			Double latitude =RequestUtil.getDouble(request, "latitude",0d);
			Long count = daService.getLong("select count(*) from com_info_tb where longitude=? and latitude=?",
					new Object[]{longitude,latitude});
			if(count>0){//经纬度重复了
				if(from.equals("client"))
					AjaxUtil.ajaxOutput(response, "-1");
				else
					AjaxUtil.ajaxOutput(response, "经纬度已存在！");
				return null;
			}
			String cmobile =RequestUtil.processParams(request, "cmobile");
			count = daService.getLong("select count(*) from user_info_tb where mobile=? and auth_flag=?",
					new Object[]{cmobile,1});
			if(count>0){//车场管理员手机号重复了
				if(from.equals("client"))
					AjaxUtil.ajaxOutput(response, "-2");
				else
					AjaxUtil.ajaxOutput(response, "手机号已存在！");
				return null;
			}
			Integer result = createAdmin(request);
			String log = "新建了停车场,"+result;
			if(result == 1){
				AjaxUtil.ajaxOutput(response, "1");
				logService.updateSysLog(comId, userId,log, 100);
			}else {
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("createAndUpload")){
			String from = RequestUtil.processParams(request, "from");
			Integer parkingTotal = RequestUtil.getInteger(request,"parking_total",0);
			if(parkingTotal==0){
				AjaxUtil.ajaxOutput(response, "请正确输入停车位信息！");
				return null;
			}
			Double longitude =RequestUtil.getDouble(request, "longitude",0d);
			Double latitude =RequestUtil.getDouble(request, "latitude",0d);
			Long count = daService.getLong("select count(*) from com_info_tb where longitude=? and latitude=?",
					new Object[]{longitude,latitude});
			if(count>0){//经纬度重复了
				if(from.equals("client"))
					AjaxUtil.ajaxOutput(response, "-1");
				else
					AjaxUtil.ajaxOutput(response, "经纬度已存在！");
				return null;
			}
			String cmobile =RequestUtil.processParams(request, "cmobile");
			count = daService.getLong("select count(*) from user_info_tb where mobile=? and auth_flag=?",
					new Object[]{cmobile,1});
			if(count>0){//车场管理员手机号重复了
				if(from.equals("client"))
					AjaxUtil.ajaxOutput(response, "-2");
				else
					AjaxUtil.ajaxOutput(response, "手机号已存在！");
				return null;
			}
			Integer result = createAdmin(request);
			String log = "新建了停车场,"+result;
			if(result == 1){

				//更新日志
				logService.updateSysLog(comId, userId,log, 100);

				//创建完车场直接上传到泊链
				String sql = "select * from com_info_tb where longitude=? and latitude=?";
				Map map= daService.getMap(sql,new Object[]{longitude,latitude});

				String union_id = CustomDefind.getValue("UNIONID");//AjaxUtil.decodeUTF8(RequestUtil.getString(request, "union_id"));
				String union_key = CustomDefind.getValue("UNIONKEY");//AjaxUtil.decodeUTF8(RequestUtil.getString(request, "union_key"));
				String server_id = CustomDefind.getValue("SERVERID");//AjaxUtil.decodeUTF8(RequestUtil.getString(request, "server_id"));
				int uploadCount = 0;
				int unUploadCount=0;
					if(map!=null){
						//String url = "https://127.0.0.1/api-web/park/addpark";
						String url = CustomDefind.UNIONIP+"park/addpark";
						//String url = "https://s.bolink.club/unionapi/park/addpark";
						Map<String, Object> paramMap = new HashMap<String, Object>();
						paramMap.put("park_id", map.get("id"));
						paramMap.put("name", map.get("company_name"));
						paramMap.put("address", map.get("address"));
						paramMap.put("phone", map.get("phone"));
						paramMap.put("lng",  map.get("longitude")+"");
						paramMap.put("lat",  map.get("latitude")+"");
						paramMap.put("total_plot",  map.get("parking_total"));
						paramMap.put("empty_plot",  map.get("parking_total"));
						paramMap.put("price_desc", getPrice(Long.valueOf(map.get("id")+"")));
						paramMap.put("remark",map.get("remarks"));
						paramMap.put("union_id", union_id);
						paramMap.put("server_id", server_id);
						paramMap.put("rand", Math.random());
						String ret = "";
						try {
							logger.error(paramMap);
							String linkParams = StringUtils.createLinkString(paramMap)+"key="+union_key;
							System.out.println(linkParams);
							String sign =StringUtils.MD5(linkParams).toUpperCase();
							logger.error(sign);
							paramMap.put("sign", sign);
							//param = DesUtils.encrypt(param,"NQ0eSXs720170114");
							String param = StringUtils.createJson(paramMap);
							logger.error(param);
							ret = HttpsProxy.doPost(url, param, "utf-8", 20000, 20000);
							JSONObject object = new JSONObject(ret);
							if(object!=null){
								Integer uploadState = object.getInt("state");
								if(uploadState==1){
									daService.update("update com_info_tb set upload_union_time=?,union_state=? " +
											"where id =?", new Object[]{System.currentTimeMillis()/1000,2,map.get("id")});
									uploadCount = 1;
								}else {
									unUploadCount = 1;
									logger.error(object.get("errmsg"));
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						logger.error(ret);
				}
				AjaxUtil.ajaxOutput(response, "新建并上传车场，成功"+uploadCount+"个，未成功"+unUploadCount+"个");

			}else {
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("modify")){	//后台修改
			String company =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "company_name"));
			String address =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
			String phone =RequestUtil.processParams(request, "phone");
			String mobile =RequestUtil.processParams(request, "mobile");
			String mcompany =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "mcompany"));
			String id =RequestUtil.processParams(request, "id");
			Integer stop_type = RequestUtil.getInteger(request, "stop_type", 0);
			Double minprice_unit = RequestUtil.getDouble(request, "minprice_unit", 0.00);
			Integer share_number = RequestUtil.getInteger(request, "share_number", 0);
			Integer parking_type = RequestUtil.getInteger(request, "parking_type", 0);
			Integer parking_total = RequestUtil.getInteger(request, "parking_total", 0);
			Integer city = RequestUtil.getInteger(request, "city", 0);
			Integer uid = RequestUtil.getInteger(request, "uid", 0);
			Integer biz_id = RequestUtil.getInteger(request, "biz_id", 0);
			Double longitude =RequestUtil.getDouble(request, "longitude",0.0);
			Double latitude =RequestUtil.getDouble(request, "latitude",0.0);
			state = RequestUtil.getInteger(request, "state", -1);
			Integer nfc = RequestUtil.getInteger(request, "nfc", 0);
			Integer etc = RequestUtil.getInteger(request, "etc", 0);
			Integer book = RequestUtil.getInteger(request, "book", 0);
			Integer navi = RequestUtil.getInteger(request, "navi", 0);
			Integer isfixed = RequestUtil.getInteger(request, "isfixed", 0);
			Integer monthlypay = RequestUtil.getInteger(request, "monthlypay", 0);
			Integer epay = RequestUtil.getInteger(request, "epay", 0);
			Integer isnight = RequestUtil.getInteger(request, "isnight", 0);//夜晚停车，0:支持，1不支持
			Long invalid_order = RequestUtil.getLong(request, "invalid_order", 0L);
			Long pid = RequestUtil.getLong(request, "pid", -1L);
			Integer isview = RequestUtil.getInteger(request, "isview", 0);
			Integer car_type = RequestUtil.getInteger(request, "car_type", 0);
			Integer passfree = RequestUtil.getInteger(request, "passfree", 0);
			Integer isautopay = RequestUtil.getInteger(request, "isautopay", 0);
			Integer full_set = RequestUtil.getInteger(request, "full_set", 0);
			Integer leave_set = RequestUtil.getInteger(request, "leave_set", 0);
			Integer activity = RequestUtil.getInteger(request, "activity", 0);//车场活动：0 没有活动 1申请活动 2:申请通过
			Double allowance = RequestUtil.getDouble(request, "allowance", 0d);
			String activity_content = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "activity_content"));//活动内容
			if(share_number>parking_total)
				share_number=parking_total;

			//检查经纬度
			Long count = daService.getLong("select count(*) from com_info_tb where longitude=? and latitude=? and id<>? ",
					new Object[]{longitude,latitude,Long.valueOf(id)});
			if(count > 0){//经纬度重复了
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
//			share_number = getShareNumber(Long.valueOf(id), share_number);
			//System.out.println(longitude+","+latitude);
			String log = "后台修改了停车场,编号："+id+"";
			if(state==-1)
				state=0;

			String fields = "invalid_order=?,company_name=?,address=?,phone=?,mobile=?,mcompany=?,parking_total=?," +
					"parking_type=?,type=?,minprice_unit=?,share_number=?,update_time=?,uid=?,biz_id=?,state=? ," +
					"etc=?,nfc=?,book=?,navi=?,monthlypay=?,isnight=?,isfixed=?,epay=?,city=?,longitude=?,latitude=?," +
					"fixed_pass_time=?,isview=?,car_type=?,passfree=?,activity=?,activity_content=?,isautopay=?,full_set=?,leave_set=?,allowance=?,pid=? ";
			Long fixed_pass_time = null;
			if(isfixed == 1){
				fixed_pass_time = System.currentTimeMillis()/1000;
			}
			Object [] values = new Object[]{invalid_order,company,address,phone,mobile,mcompany,parking_total,parking_type,stop_type,minprice_unit,share_number,
					System.currentTimeMillis()/1000,uid,biz_id,state,etc,nfc,book,navi,monthlypay,isnight,isfixed,epay,city,longitude,latitude,fixed_pass_time,
					isview,car_type,passfree,activity,activity_content,isautopay,full_set,leave_set,allowance,pid,Long.valueOf(id)};
			String sql = "update com_info_tb set "+fields+" where id=?";
			int result = daService.update(sql, values);
			if(result==1&&city>0){
				publicMethods.setCityCache(Long.valueOf(id),city);
			}
			if(result == 1){
				if(publicMethods.isEtcPark(Long.valueOf(id))){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{Long.valueOf(id),"com_info_tb",Long.valueOf(id),System.currentTimeMillis()/1000,1});
				}
			}
			AjaxUtil.ajaxOutput(response, result+"");
			logService.updateSysLog(Long.valueOf(id), userId,log+"("+sql+",params:"+StringUtils.objArry2String(values)+")", 101);
		}else if(action.equals("edit")){//客户端修改
			String company =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "company_name"));
			String address =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
			String phone =RequestUtil.processParams(request, "phone");
			//String mobile =RequestUtil.processParams(request, "mobile");
			String id =RequestUtil.processParams(request, "id");
			Integer stop_type = RequestUtil.getInteger(request, "stop_type", 0);
			Integer parking_type = RequestUtil.getInteger(request, "parking_type", 0);
			Integer parking_total = RequestUtil.getInteger(request, "parking_total", 0);
			Double longitude =RequestUtil.getDouble(request, "longitude",0.0);
			Double latitude =RequestUtil.getDouble(request, "latitude",0.0);
			String resume = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "resume"));
			logger.error(">>>>>>>>>>>>>>>>>>resume="+resume);
			String uin = RequestUtil.processParams(request, "uin");//客户端传来的帐号
			//share_number = getShareNumber(Long.valueOf(id), share_number);
			if(state==-1)
				state=0;
			String fields = "company_name=?,address=?,phone=?,parking_total=?," +
					"parking_type=?,stop_type=?,update_time=? ";
			Object [] values =new Object[]{company,address,phone,parking_total,parking_type,stop_type,
					System.currentTimeMillis()/1000,Long.valueOf(id)};
			if(!resume.equals("")){
				if(latitude!=0&&latitude!=0){
					fields = "company_name=?,address=?,phone=?,parking_total=?," +
							"parking_type=?,stop_type=?,update_time=? ,longitude=?,latitude=?,resume=?,remarks=? ";
					values =new Object[]{company,address,phone,parking_total,parking_type,stop_type,
							System.currentTimeMillis()/1000,longitude,latitude,resume,resume,Long.valueOf(id)};
				}else {
					fields = "company_name=?,address=?,phone=?,parking_total=?," +
							"parking_type=?,stop_type=?,update_time=? ,resume=? ";
					values =new Object[]{company,address,phone,parking_total,parking_type,stop_type,
							System.currentTimeMillis()/1000,resume,Long.valueOf(id)};
				}
			}else if(latitude!=0&&longitude!=0){
				fields = "company_name=?,address=?,phone=?,parking_total=?," +
						"parking_type=?,stop_type=?,update_time=? ,longitude=?,latitude=? ";
				values =new Object[]{company,address,phone,parking_total,parking_type,stop_type,
						System.currentTimeMillis()/1000,longitude,latitude,Long.valueOf(id)};
			}
			String sql = "update com_info_tb set "+fields+ "where id=? ";
			int result = daService.update(sql, values);
			AjaxUtil.ajaxOutput(response, result+"");
			String log = "客户端修改了停车场,编号："+id+"";
			logService.updateSysLog(Long.valueOf(id), uin,log+"("+sql+",params:"+StringUtils.objArry2String(values)+")", 101);
		}else if(action.equals("editcontactor")){
			String mobile =RequestUtil.processParams(request, "mobile");
			String strid =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "strid"));
			String pass =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "pass"));
			String nickname =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname"));
			if(pass.equals(""))
				pass = strid;
			Long comid =RequestUtil.getLong(request, "comid", -1L);
			String sql = "update user_info_tb set strid=?,password=?,mobile=?,nickname=? where comid=? and auth_flag=?";
			int result = daService.update(sql, new Object[]{strid,pass,mobile,nickname,comid,ZLDType.ZLD_PARKADMIN_ROLE});
			AjaxUtil.ajaxOutput(response, result+"");
			logService.updateSysLog(comId, userId,"修改了停车场管理员,编号："+strid, 201);
		}else if(action.equals("delete")){
			String id =RequestUtil.processParams(request, "selids");
			String sql = "update com_info_tb set state=?,update_time=? where id =?";
			Object [] values = new Object[]{1,System.currentTimeMillis()/1000,Long.valueOf(id)};
			int result = daService.update(sql, values);
//			if(result==1)
//				ParkingMap.deleteParkingMap(Long.valueOf(id));
			AjaxUtil.ajaxOutput(response, result+"");
			logService.updateSysLog(comId, userId,"删除了停车场，编号："+id, 102);
		}else if(action.equals("check")){
			String strid = RequestUtil.processParams(request, "value");
			String sql = "select count(*) from user_info_tb where strid =?";
			Long result = daService.getLong(sql, new Object[]{strid});
			if(result>0)
				AjaxUtil.ajaxOutput(response, "1");
			else {
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("localdata")){//地区信息
			AjaxUtil.ajaxOutput(response,GetLocalCode.getLocalData());
		}else if(action.equals("getlocalbycode")){
			Integer code = RequestUtil.getInteger(request, "code", 0);
			String local = GetLocalCode.localDataMap.get(code);
			if(local==null||local.equals("null")){
				AjaxUtil.ajaxOutput(response,"");
				return null;
			}
			if(code%100!=0)
				local =GetLocalCode.localDataMap.get((code/100)*100)+local;
			if(code%10000!=0)
				local =GetLocalCode.localDataMap.get((code/10000)*10000)+local;
			AjaxUtil.ajaxOutput(response,local);
		}else if(action.equals("getbizs")){
			List<Map> tradsList = daService.getAll("select * from bizcircle_tb where state =?",
					new Object[]{0});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(tradsList!=null&&tradsList.size()>0){
				for(Map map : tradsList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("name")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getparkings")){
			List<Map> tradsList = daService.getAll("select id,company_name from com_info_tb where state =?",
					new Object[]{0});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(tradsList!=null&&tradsList.size()>0){
				for(Map map : tradsList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("company_name")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("isview")){//显示在手机地图
			Long id =RequestUtil.getLong(request, "id",-1L);
			Long isview =RequestUtil.getLong(request, "isview",-1L);
			if(isview==0)
				isview=1L;
			else if(isview==1)
				isview=0L;
			int ret = 0;
			if(id!=-1&&isview!=-1){
				ret = daService.update("update com_info_tb set isview=?,update_time=? where id =?",
						new Object[]{isview,System.currentTimeMillis()/1000,id});
			}
			AjaxUtil.ajaxOutput(response, ret+"");
		}else if(action.equals("getver")){//查询审核数据
			String name = RequestUtil.getString(request, "type");
			Long id = RequestUtil.getLong(request, "id", -1L);
			//logger.error(id+","+name);
			String ret = "0/0";
			if(id!=-1){
				List<Map<String, Object>> list = daService.getAll("select "+name+ " from park_verify_tb where comid=?" , new Object[]{id});
				if(list!=null&&!list.isEmpty()){
					Integer pass = 0;
					for(Map<String, Object> map :list){
						Integer v = (Integer)map.get(name);
						if(v==1)
							pass = pass+1;
					}
					ret = pass+"/"+list.size();
				}
			}
			AjaxUtil.ajaxOutput(response, ret);
		}else if(action.equals("verifydetail")){//查看审核详情
			Long id = RequestUtil.getLong(request, "id", -1L);
			String type = RequestUtil.getString(request, "type");
			String sql = "select * from park_verify_tb where comid=?  ";
			String data = "[]";
			if(id!=-1&&!"".equals(type)){
				List<Map<String, Object>> list = daService.getAll("select v.uin,v."+type+ ",c.car_number,v.ctime from park_verify_tb v " +
						"left join car_info_Tb c on v.uin=c.uin where v.comid=? order by "+type+" desc" , new Object[]{id});
				if(list!=null&&!list.isEmpty()){
					data = "[";
					for(Map<String, Object> map :list){
						Long ctime = (Long)map.get("ctime");
						Integer v = (Integer)map.get(type);
						String vs = "通过";
						if(v==0)
							vs = "未通过";
						data+="[\""+map.get("car_number")+"\",\""+TimeTools.getTime_yyMMdd_HHmm(ctime*1000)+"\",\""+vs+"\"],";
					}
					if(data.endsWith(","))
						data = data.substring(0,data.length()-1);
					data = data+"]";
				}
			}
			request.setAttribute("type", type);
			request.setAttribute("data", data.replace("null", "未知"));
			return mapping.findForward("verifydetail");
		}else if(action.equals("initnobj")){//初始化非北京车场
			List<Map<String, Object>> mList = daService.getAll("select id from com_info_tb where city >?", new Object[]{110229});
			if(mList!=null&&!mList.isEmpty()){
				Map<Long, Integer> map = new HashMap<Long, Integer>();
				for(Map<String, Object> m : mList){
					map.put((Long)m.get("id"), 1);
				}
				memcacheUtils.doMapLongIntegerCache("comid_backmoney_cache", map, "update");
			}
			AjaxUtil.ajaxOutput(response, mList.size()+"");
			return null;
		}
		else if(action.equals("import")){
//			List<Map<String, Object>> list   = daService.getAll("select longitude,latitude from com_info_tb ", null);
			Set<String> set = null;//new HashSet<String>();
//			for(Map<String, Object> map :list){
//				set.add(map.get("longitude")+""+map.get("latitude"));
//			}
			Long ntime = System.currentTimeMillis()/1000;
			List<Object[]> values = new ArrayList<Object[]>();//ImportExcelUtil.importExcelFile(set);
			values.add(new Object[]{"毓贤街","毓贤街南侧",119.428060118785,32.3935220438341,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"七里香","文昌中路与国庆路交叉口",119.435645528907,32.3962392513232,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"银河电子城","文昌中路",119.442387299432,32.3968781006322,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"菜根香","国庆路",119.435811471349,32.3932786873232,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"五交化","国庆路与渡江路交叉口",119.436165337525,32.3906813694866,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"徐凝门街东（何园）","徐凝门街东侧",119.444360143582,32.3872557106368,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"秋雨东路","秋雨东路路南侧全线",119.410544071258,32.3909962083873,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"秋雨西路","秋雨西路路北侧全线",119.404136899797,32.3903941406342,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"兴城东路（西）","兴城东路（西）",119.411234020821,32.3813977220772,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"望月路（万鸿）","望月路（万鸿）（邗江路-百祥路南侧）",119.391311401272,32.3886407916227,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"望月路（美琪）","望月路（美琪）（邗江路-百祥路北侧）",119.385996157504,32.3877749600766,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"望月路西段","望月路（邗江中路——润扬路）",119.384698117787,32.3875496598764,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"兴城东路（东）","兴城东路（东）",119.411361348279,32.3814207120106,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"四望亭东侧","四望亭东侧",119.428033366341,32.3994080560416,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"四望亭路南（怡园）","四望亭路南侧(汶河北路至淮海路段）",119.425856206604,32.3994514519998,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"四望亭路南（盛宴）","四望亭路南侧（淮海路至来鹤桥段）",119.425953559577,32.3994515404289,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"四望亭路南（扬师院）","四望亭路南侧（来鹤桥至扬师院南门段）",119.42036960027,32.3994116619424,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"柳湖路南段","柳湖路向北双侧（四望亭路至扬师院东门桥段）",119.421019080302,32.399512919219,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"柳湖路北段","柳湖路东侧（扬师院东门桥——大虹桥西路段）",119.420931940081,32.4035303809354,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"绿扬村","冶春康乐园门口",119.428685784381,32.4031329276058,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"红园","红园门前",119.425502321245,32.4036513006389,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"工艺美术大楼门前","盐阜西路",119.425502651357,32.4023672874231,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"大梅家巷","淮海路与大梅家巷",119.425133015786,32.3978258496966,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"大明寺山下","大明寺脚下",119.40931040882,32.4212138936712,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"汶河北路","汶河北路东侧",119.428661216111,32.398498249061,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"天宁寺博物馆","天宁寺博物馆门前",119.432407288933,32.4023568298202,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"梅岭东路","梅岭东路全线",119.43520055061,32.4089373971226,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"梅岭西路","梅岭西路全线",119.4341142724156,32.4089267864255,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			values.add(new Object[]{"玉器厂门前","玉器厂门前沿河街道",119.437596634205,32.403046892805,2,321000,1000,ntime,ntime,0,0,"","",1008,7,321000});
			String sql = "insert into com_info_tb(company_name,address,longitude,latitude,parking_type,city,parking_total," +
					"create_time,update_time,state,type,mobile,remarks,chanid,groupid,cityid) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			System.out.println(sql);
			for(Object [] v: values){
				logger.error(StringUtils.objArry2String(v));
			}
			///logger.error(set.size());
//			for(Object [] va: values){
//				logger.error(StringUtils.objArry2String(va));
//			}
			int ret = 0;
			ret = daService.bathInsert(sql, values, new int[]{12,4,12,4,4,3,3,4,4,4,4,12,12,4,4,4});

			AjaxUtil.ajaxOutput(response, ret+"");
		}
		/*
		 * 西部客运枢纽停车场	白天小型车首小时4元，首小时后1元/小时；晚上首小时2元，首小时后1元/小时；	文昌西路扬州西部交通客运枢纽里面				政府指导价	室内	记时	一类区	142			119.357553,32.392485
		 * else if(action.equals("weixin")){
			List<Map<String, Object>> aliList = onlyReadService.getAll("select notify_no,create_time,money,uin,comid,wxp_orderid from alipay_log" +
					" where length(notify_no)< ? and create_time between ? and ? order by uin ", new Object[]{20,1437926400L,1443628800L});
			Map<String, Object> idMoneyMap = new HashMap<String, Object>();
			logger.error(aliList.size());
			for(Map<String, Object>map :aliList){
				String oid = (String)map.get("wxp_orderid");
				idMoneyMap.put(oid, map.get("money"));
			}
			logger.error(idMoneyMap.size());
			weixin(idMoneyMap);
		}
		else if(action.equals("wx")){
			//Long t1 = 1437926400L;
			//Long t2 = 1443542400L;
			//Long t2 = 1437951600L;
			Long t1 = RequestUtil.getLong(request, "start", -1L);
			Long t2 = RequestUtil.getLong(request, "end", -1L);
			if(t1==-1||t2==-1){
				t1 = 1437926400L;
				t2 = 1438358400L;
				//AjaxUtil.ajaxOutput(response, "输入不合法。。。。");
			}
			String fname = TimeTools.getTime_yyyyMMdd_HH(t1*1000)+"_"+TimeTools.getTime_yyyyMMdd_HH(t2*1000);
			//查出所有微信打折券消息记录
			System.out.println("开始查询所有订单。。。。。"+fname);
			List<Map<String, Object>> allList  = onlyReadService.getAll("select o.id,o.create_time,o.end_time,o.total,o.comid,o.uin," +
					"t.umoney,t.money,t.utime,t.resources,t.type from order_tb o left join ticket_tb t on t.orderid=o.id" +
					" where o.create_time between ? and ? order by o.id ", new Object[]{t1,t2});
			System.out.println("所有订单"+allList.size());
			//去重
			Set<Long> idSet = new HashSet<Long>();
			List<Map<String, Object>> orderList =new ArrayList<Map<String,Object>>();
			for(Map<String, Object>  map : allList){
				Long id = (Long)map.get("id");
				if(idSet.add(id)){
					orderList.add(map);
				}
			}
			System.out.println("去重后所有订单"+orderList.size());
			//查出所有微信支付日志
			List<Map<String, Object>> aliList = onlyReadService.getAll("select notify_no,create_time,money,uin,comid,wxp_orderid,orderid from alipay_log" +
					" where length(notify_no)< ? and create_time between ? and ? order by uin ", new Object[]{20,t1,t2});
			Map<Long, List<Map<String, Object>>> aliMap = new HashMap<Long, List<Map<String,Object>>>();
			System.out.println("微信支付日志"+aliList.size());
			for(Map<String, Object> m: aliList){
				Long uin = (Long)m.get("uin");
				if(uin==null||uin==-1)
					continue;
				if(aliMap.containsKey(uin)){
					List<Map<String, Object>> l = aliMap.get(uin);
					l.add(m);
				}else {
					List<Map<String, Object>> l = new ArrayList<Map<String,Object>>();
					l.add(m);
					aliMap.put(uin, l);
				}
			}
			//查所有OPENID
			List<Map<String, Object>> openList = onlyReadService.getAll("select id, wxp_openid from user_info_tb where wxp_openid is not null", null);
			System.out.println("已注册的公众号"+openList.size());
			Map<Long,String> openidMap = new HashMap<Long, String>();
			for(Map<String, Object> m: openList){
				openidMap.put((Long)m.get("id"), ""+m.get("wxp_openid"));
			}
			openList = onlyReadService.getAll("select uin,openid from wxp_user_tb ", null);
			System.out.println("未注册的公众号"+openList.size());
			for(Map<String, Object> m: openList){
				openidMap.put((Long)m.get("uin"), ""+m.get("openid"));
			}
			//补支付商户订单号
			for(Map<String, Object> map: orderList){
				Long uin = (Long)map.get("uin");
				map.put("openid", openidMap.get(uin));
				Long id = (Long)map.get("id");
				Integer res =(Integer)map.get("resources");//1购买券
				if(res==null)
					res = 0;
				Long obtime = (Long)map.get("create_time");
				Long oetime = (Long)map.get("end_time");
				if(oetime==null)
					oetime = obtime+60;
				Double umoney =  StringUtils.formatDouble(map.get("umoney"));//券使用金额
				if(umoney==null)
					umoney=0.0;
				if(res==1){//1购买券
					Integer zc = 8;
					if(obtime<1441036800){//9.1之前按7折，之后按8折
						zc = 7;
					}
					umoney = StringUtils.formatDouble(umoney*(10-zc)*0.1);
					map.put("umoney", umoney);
				}
				//map.put("userpay", StringUtils.formatDouble(total-umoney));
				map.put("tempmoney", umoney>3?3.0:umoney);
				map.put("create_time", TimeTools.getTime_yyyyMMdd_HHmmss(obtime*1000));
				map.put("end_time", TimeTools.getTime_yyyyMMdd_HHmmss(oetime*1000));
				List<Map<String, Object>> logList = aliMap.get(uin);
				if(logList!=null){
					for(Map<String, Object> m : logList){
						Long ltime = (Long)m.get("create_time");
						Double usrwxpay = StringUtils.formatDouble(m.get("money"));
						if(obtime==null||oetime==null||ltime==null)
							continue;
						if(oetime>1442646927){
							Long poid = (Long)m.get("orderid");
							if(id.equals(poid)){
								map.put("payid", m.get("notify_no"));
								map.put("wxp_orderid", m.get("wxp_orderid")+"_");
								map.put("userpay", usrwxpay);
								//logger.error(m.get("wxp_orderid"));
								break;
							}
						}else if(Math.abs(obtime-ltime)<500||Math.abs(oetime-ltime)<500){
							map.put("payid", m.get("notify_no"));
							map.put("wxp_orderid", m.get("wxp_orderid")+"_");
							map.put("userpay", usrwxpay);
							//logger.error(m.get("wxp_orderid"));
							break;
						}
					}
				}
			}
			System.out.println("开始写文件....共"+orderList.size()+"条");
			try {

				BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/data/"+fname+"_weixin.csv"),true));
				//BufferedWriter writer = new BufferedWriter(new FileWriter(new File("e:/weixin0727-0930.csv"),true));
				String line = "";
				int index = 0;
				for(Map<String, Object> map: orderList){
					//Iterator<String> keys = map.keySet().iterator();
					//while(keys.hasNext()){
					//	String key = keys.next();
					//	String openid =(String) map.get("openid");
					//	if(openid==null||"".equals(openid))
					//		continue;
						//line +=key +"="+map.get(keys.next())+",";
					//}
					String openid =(String) map.get("openid");
					String wxp_orderid =(String) map.get("wxp_orderid");
					//Integer m = (Integer)map.get("money");//折扣率
					if(openid==null||"".equals(openid))
						continue;
					if(wxp_orderid==null||"".equals(wxp_orderid))
						continue;
					if(map.size()<10)
						continue;
					line +=map.get("id")+","+map.get("create_time")+","+map.get("end_time")+","+map.get("total")+
							","+map.get("umoney")+","+map.get("tempmoney")+","+map.get("userpay")+","+map.get("openid")+","+map.get("payid")+
							","+map.get("wxp_orderid")+"\n";
//					if(m==3){
//						line3 +=map.toString();
//						line3 +="\n";
//					}
//					else {
//						line5 +=map.toString();
//						line5 +="\n";
//					}
					index++;
					if(index%500==0){
						writer.write(line);
						logger.error("已写到第"+index+"条;");
						line = "";
					}
				}
				writer.write(line);
				//writer.write(line5);
				//writer.write(line3);
				writer.flush();
				writer.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}else if(action.equals("parkout")){
			List all = daService.getAll("select id,company_name,city,create_time from com_info_tb where id " +
					"in(select distinct(comid) from order_tb where state=? and pay_type=?) order by create_time",new Object[]{1,2});
			Long time = 1433088000L;//6月1日
			List list1 = new ArrayList();
			List list2 = new ArrayList();
			List mall = daService.getAll("select count(id) count,sum(total) money,comid from order_tb " +
					"where state=? and pay_type=? group by comid",new Object[]{1,2});
			for(int i=0;i<all.size();i++){
				Map map = (Map)all.get(i);
				Long ctime = (Long)map.get("create_time");
				map.put("time", TimeTools.getTimeStr_yyyy_MM_dd(ctime*1000));
				Integer city = (Integer)map.get("city");
				String c =getCity(city);
				Long comid = (Long)map.get("id");
				if(c==null)
					c="北京市";
				map.put("city", c);
				if(ctime<time)
					list1.add(map);
				else {
					list2.add(map);
				}
				for(int k=0;k<mall.size();k++){
					Map map1 = (Map)mall.get(k);
					Long cid = (Long)map1.get("comid");
					if(comid.equals(cid)){
						map.put("total", map1.get("money"));
						map.put("count", map1.get("count"));
					}
				}
			}

			String r1 = "";
			for(int j=0;j<list1.size();j++){
				Map map = (Map)list1.get(j);
				Double price = 2d;
				Long count = (Long)map.get("count");
				Long ctime = (Long)map.get("create_time");
				Long days = (System.currentTimeMillis()/1000-ctime)/86400;
				if(days==0)
					days=1L;
				Double jy = Math.ceil(count/days);
				if(jy==0)
					jy=1.0;
				Double total = StringUtils.formatDouble(map.get("total"));
				if(count!=null&&count!=0&&total>0)
					price = StringUtils.formatDouble(total/count);
				r1 +=map.get("company_name")+","+map.get("city")+","+map.get("time")+","+count+","+total+","+price+","+jy+","+StringUtils.formatDouble(Math.ceil(price/jy))+"\n";
			}
			String r2 = "";
			for(int j=0;j<list2.size();j++){
				Map map = (Map)list2.get(j);
				Double price = 2d;
				Long count = (Long)map.get("count");
				Long ctime = (Long)map.get("create_time");
				Long days = (System.currentTimeMillis()/1000-ctime)/86400;
				if(days==0)
					days=1L;
				Double jy = Math.ceil(count/days);
				if(jy==0)
					jy=1.0;
				Double total = StringUtils.formatDouble(map.get("total"));
				if(count!=null&&count!=0&&total>0)
					price = StringUtils.formatDouble(total/count);
				r2 +=map.get("company_name")+","+map.get("city")+","+map.get("time")+","+count+","+total+","+price+","+jy+","+StringUtils.formatDouble(Math.ceil(price/jy))+"\n";
			}
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(new File("d:/parkbefore61.txt"),true));
				writer.write(r1);
				//writer.write(line5);
				//writer.write(line3);
				writer.flush();
				writer = new BufferedWriter(new FileWriter(new File("d:/parkafter61.txt"),true));
				writer.write(r2);
					//writer.write(line5);
					//writer.write(line3);
				writer.flush();
				writer.close();

			} catch (Exception e) {
				// TODO: handle exception
			}

		}*/
		return null;
	}
	/**
	 * 取首小时价格
	 * @param parkId
	 * @return
	 */
	private String getPrice(Long parkId){
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		//开始小时
		int bhour = calendar.get(Calendar.HOUR_OF_DAY);
		List<Map> priceList=daService.getAll("select * from price_tb where comid=? " +
				"and state=? and pay_type=? order by id desc", new Object[]{parkId,0,0});
		if(priceList==null||priceList.size()==0){//没有按时段策略
			//查按次策略
			priceList=daService.getAll("select * from price_tb where comid=? " +
					"and state=? and pay_type=? order by id desc", new Object[]{parkId,0,1});
			if(priceList==null||priceList.size()==0){//没有按次策略，返回提示
				return "0元/次";
			}else {//有按次策略，直接返回一次的收费
				Map timeMap =priceList.get(0);
				Integer unit = (Integer)timeMap.get("unit");
				if(unit!=null&&unit>0){
					if(unit>60){
						String t = "";
						if(unit%60==0)
							t = unit/60+"小时";
						else
							t = unit/60+"小时 "+unit%60+"分钟";
						return timeMap.get("price")+"元/"+t;
					}else {
						return timeMap.get("price")+"元/"+unit+"分钟";
					}
				}else {
					return timeMap.get("price")+"元/次";
				}
			}
			//发短信给管理员，通过设置好价格
		}else {//从按时段价格策略中分拣出日间和夜间收费策略
			if(priceList.size()>0){
				//logger.error(priceList);
				for(Map map : priceList){
					Integer btime = (Integer)map.get("b_time");
					Integer etime = (Integer)map.get("e_time");
					Double price = Double.valueOf(map.get("price")+"");
					Double fprice = Double.valueOf(map.get("fprice")+"");
					Integer ftime = (Integer)map.get("first_times");
					if(ftime!=null&&ftime>0){
						if(fprice>0)
							price = fprice;
					}
					if(btime<etime){//日间
						if(bhour>=btime&&bhour<etime){
							return price+"元/"+map.get("unit")+"分钟";
						}
					}else {
						if(bhour>=btime||bhour<etime){
							return price+"元/"+map.get("unit")+"分钟";
						}
					}
				}
			}
		}
		return "0.0元/小时";
	}

	private String getCity(Integer ciyt) {

		Map<Integer,String> cities = new HashMap<Integer, String>();
		cities.put(370200,  "青岛市");
		cities.put(210100,  "沈阳市");
		cities.put(210200,  "大连市");
		cities.put(440100,  "广州市");
		cities.put(110105,  "北京朝阳区");
		cities.put(410100,  "郑州市");
		cities.put(330700,  "浙江金华市");
		cities.put(610100,  "西安市");
		cities.put(120000,  "天津市");
		cities.put(210102,  "沈阳和平区");
		cities.put(110108,  "北京海淀区");
		cities.put(310000,  "上海市");
		cities.put(210000,  "辽宁省");
		cities.put(110112,  "北京通州区");
		cities.put(410200,  "开封市");
		cities.put(440300,  "深圳市");
		cities.put(500103,  "重庆渝中区");
		cities.put(330100,  "杭州市");
		cities.put(370100,  "济南市");
		cities.put(110000,  "北京市");
		cities.put(350200,  "厦门市");
		cities.put(210100,  "长春市");
		cities.put(130100,  "石家庄市");
		cities.put(330105,  "杭州拱墅区");
		return cities.get(ciyt);
	}

	//注册停车场管理员帐号
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Integer createAdmin(HttpServletRequest request){
		Long time = System.currentTimeMillis()/1000;
		String company =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "company_name"));
		//System.out.println(company);
		company = company.replace("\r", "").replace("\n", "");
		String address =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		address = address.replace("\r", "").replace("\n", "");
		String phone =RequestUtil.processParams(request, "phone");
		String mobile =RequestUtil.processParams(request, "mobile");
		String longitude =RequestUtil.processParams(request, "longitude");
		String latitude =RequestUtil.processParams(request, "latitude");
		String mcompany =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "mcompany"));
		Integer parking_type =RequestUtil.getInteger(request, "parking_type", 0);
		Integer parking_total =RequestUtil.getInteger(request, "parking_total", 0);
		Integer type = RequestUtil.getInteger(request, "type", 0);
		Integer city = RequestUtil.getInteger(request, "city", 0);
		Integer biz_id = RequestUtil.getInteger(request, "biz_id", 0);
		Integer uid = RequestUtil.getInteger(request, "uid", 0);
		Integer nfc = RequestUtil.getInteger(request, "nfc", 0);
		Integer etc = RequestUtil.getInteger(request, "etc", 0);
		Integer book = RequestUtil.getInteger(request, "book", 0);
		Integer navi = RequestUtil.getInteger(request, "navi", 0);
		Integer epay = RequestUtil.getInteger(request, "epay", 0);
		Integer monthlypay = RequestUtil.getInteger(request, "monthlypay", 0);
		Integer isnight = RequestUtil.getInteger(request, "isnight", 0);//夜晚停车，0:支持，1不支持
		Integer car_type = RequestUtil.getInteger(request, "car_type", 0);
		Double minprice_unit = RequestUtil.getDouble(request, "minprice_unit", 0.00);
		Long comId = daService.getLong("SELECT nextval('seq_com_info_tb'::REGCLASS) AS newid",null);

		List<Map> sqlsList = new ArrayList<Map>();
		Map comMap = new HashMap();
		//添加自动生成车场16位秘钥的逻辑
		String ukey = StringUtils.createRandomCharData(16);
		//String share_number =RequestUtil.processParams(request, "share_number");
//		String comsql = "insert into com_info_tb(id,company_name,address,mobile,phone,create_time," +
//				"mcompany,parking_type,parking_total,longitude,latitude,type,update_time,city,uid,biz_id,nfc,etc,book,navi,monthlypay,isnight,epay,car_type,minprice_unit,ukey)" +
//				" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//		Object[] comvalues = new Object[]{comId,company,address,mobile,phone,time,
//				mcompany,parking_type,parking_total,Double.valueOf(longitude),Double.valueOf(latitude),type,time,city,uid,biz_id,
//				nfc,etc,book,navi,monthlypay,isnight,epay,car_type,minprice_unit,ukey};
		String comsql = "insert into com_info_tb(id,company_name,address,mobile,phone,create_time," +
				"mcompany,parking_type,parking_total,longitude,latitude,type,update_time,city,uid,biz_id,nfc,etc,book,navi,monthlypay,isnight,epay,car_type,minprice_unit,ukey,state,isfixed,union_state)" +
				" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Object[] comvalues = new Object[]{comId,company,address,mobile,phone,time,
				mcompany,parking_type,parking_total,Double.valueOf(longitude),Double.valueOf(latitude),type,time,city,uid,biz_id,
				nfc,etc,book,navi,monthlypay,isnight,epay,car_type,minprice_unit,ukey,0,1,0};
		comMap.put("sql", comsql);
		comMap.put("values", comvalues);

		sqlsList.add(comMap);

		boolean r =  daService.bathUpdate(sqlsList);
		if(r){
			if(city>0)
				publicMethods.setCityCache(Long.valueOf(comId),city);

			if(etc == 2){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("comid", comId);
				map.put("cname", company);
				commonMethods.createDefDevice(request, map);
			}
			return 1;
		}
		else {
			return -1;
		}
	}

	/*private int getShareNumber(Long comid,int shareNumber){
		Long total = daService.getLong("select parking_total from com_info_tb where id =? ", new Object[]{comid});
		if(total!=null&&total<shareNumber)
			return total.intValue();
		return shareNumber;
	}*/

	public static void main(String[] args) {
		try {
			System.out.println(StringUtils.MD5(StringUtils.MD5("tcbtest")+"zldtingchebao201410092009"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private  void weixin(Map<String, Object> map){
		BufferedReader reader = null;
		String lineString=null;
		String result = "";
		BufferedWriter writer;
		try {
			reader = new BufferedReader(new FileReader(new File("e:/weixin0727-0930.csv")));
			writer = new BufferedWriter(new FileWriter(new File("e:/weixin0727_0930_final.csv")));
			int i=0;
			while ((lineString = reader.readLine()) != null) {
				String []info = lineString.split(",");
				String oid = info[9];
				oid = oid.substring(0,oid.length()-1);
				int index = lineString.indexOf("oRo");
				result += lineString.substring(0,index)+map.get(oid)+","+lineString.substring(index);
				result+="\n";
				if(i%100==0){
					writer.write(result);
					//writer.flush();
					result ="";
					logger.error(i);
				}
				i++;
			}
			reader.close();
			writer.write(result);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
	}
}
