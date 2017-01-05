package com.zld.struts.request;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.AjaxUtil;
import com.zld.impl.MemcacheUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.LogService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.GetLocalCode;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.utils.ZLDType;
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
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Integer state = RequestUtil.getInteger(request, "state", 0);
		String userId = (String)request.getSession().getAttribute("userid");
		Long comId = (Long)request.getSession().getAttribute("comid");
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
			Integer isview = RequestUtil.getInteger(request, "isview", 0);
			Integer car_type = RequestUtil.getInteger(request, "car_type", 0);
			Integer passfree = RequestUtil.getInteger(request, "passfree", 0);
			Integer isautopay = RequestUtil.getInteger(request, "isautopay", 0);
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
					"etc=?,nfc=?,book=?,navi=?,monthlypay=?,isnight=?,isfixed=?,epay=?,city=?,longitude=?,latitude=?,fixed_pass_time=?,isview=?,car_type=?,passfree=?,activity=?,activity_content=?,isautopay=?,allowance=? ";
			Long fixed_pass_time = null;
			if(isfixed == 1){
				fixed_pass_time = System.currentTimeMillis()/1000;
			}
			Object [] values = new Object[]{invalid_order,company,address,phone,mobile,mcompany,parking_total,parking_type,stop_type,minprice_unit,share_number,
					System.currentTimeMillis()/1000,uid,biz_id,state,etc,nfc,book,navi,monthlypay,isnight,isfixed,epay,city,longitude,latitude,fixed_pass_time,isview,car_type,passfree,activity,activity_content,isautopay,allowance,Long.valueOf(id)};
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
			System.err.println(">>>>>>>>>>>>>>>>>>resume="+resume);
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
			//System.err.println(id+","+name);
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
		/*else if(action.equals("import")){
			List<Map<String, Object>> list   = daService.getAll("select longitude,latitude from com_info_tb ", null);
			Set<String> set = new HashSet<String>();
			for(Map<String, Object> map :list){
				set.add(map.get("longitude")+""+map.get("latitude"));
			}
			List<Object[]> values = ImportExcelUtil.importExcelFile(set);
			///System.err.println(set.size());
			for(Object [] va: values){
				System.err.println(StringUtils.objArry2String(va));
			}
			String sql = "insert into com_info_tb(company_name,address,remarks,create_time,update_time,longitude,latitude,type,state,city) values(?,?,?,?,?,?,?,?,?,?)";
			int ret = 0;
			ret = daService.bathInsert(sql, values, new int[]{12,12,12,4,4,3,3,4,4,4});
			
			AjaxUtil.ajaxOutput(response, ret+"");
		}
		else if(action.equals("weixin")){
			List<Map<String, Object>> aliList = onlyReadService.getAll("select notify_no,create_time,money,uin,comid,wxp_orderid from alipay_log" +
					" where length(notify_no)< ? and create_time between ? and ? order by uin ", new Object[]{20,1437926400L,1443628800L});
			Map<String, Object> idMoneyMap = new HashMap<String, Object>();
			System.err.println(aliList.size());
			for(Map<String, Object>map :aliList){
				String oid = (String)map.get("wxp_orderid");
				idMoneyMap.put(oid, map.get("money"));
			}
			System.err.println(idMoneyMap.size());
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
								//System.err.println(m.get("wxp_orderid"));
								break;
							}
						}else if(Math.abs(obtime-ltime)<500||Math.abs(oetime-ltime)<500){
							map.put("payid", m.get("notify_no"));
							map.put("wxp_orderid", m.get("wxp_orderid")+"_");
							map.put("userpay", usrwxpay);
							//System.err.println(m.get("wxp_orderid"));
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
						System.err.println("已写到第"+index+"条;");
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
		//String share_number =RequestUtil.processParams(request, "share_number");
		String comsql = "insert into com_info_tb(id,company_name,address,mobile,phone,create_time," +
				"mcompany,parking_type,parking_total,longitude,latitude,type,update_time,city,uid,biz_id,nfc,etc,book,navi,monthlypay,isnight,epay,car_type,minprice_unit)" +
				" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Object[] comvalues = new Object[]{comId,company,address,mobile,phone,time,
				mcompany,parking_type,parking_total,Double.valueOf(longitude),Double.valueOf(latitude),type,time,city,uid,biz_id,
				nfc,etc,book,navi,monthlypay,isnight,epay,car_type,minprice_unit};
		comMap.put("sql", comsql);
		comMap.put("values", comvalues);
		
		sqlsList.add(comMap);
		
		boolean r =  daService.bathUpdate(sqlsList);
		if(r){
			if(city>0)
				publicMethods.setCityCache(Long.valueOf(comId),city);
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
					System.err.println(i);
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
