package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.CustomDefind;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.StringUtils;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 停车场后台管理员登录后，管理员工，员工分为收费员和财务
 * @author Administrator
 *
 */
public class MemberManageAction extends Action{

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	private Logger logger = Logger.getLogger(MemberManageAction.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		Long loginuin = (Long)request.getSession().getAttribute("loginuin");
		request.setAttribute("authid", request.getParameter("authid"));
		Integer isAdmin =(Integer)request.getSession().getAttribute("isadmin");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		Long  cityid=(Long)request.getSession().getAttribute("cityid");
		//System.out.println("isadmin:"+isAdmin);
		logger.error("cityid:"+cityid+",groupid:"+groupid);
		Integer supperadmin = (Integer)request.getSession().getAttribute("supperadmin");
		if(loginuin==null){
			response.sendRedirect("login.do");
			return null;
		}
		if(comid==0){//来自车场云后台
			comid = RequestUtil.getLong(request, "comid", 0L);
		}
		if(action.equals("")){
			request.setAttribute("comid", comid);
			return mapping.findForward("list");
		}else if(action.equals("adminlist")){
			Map comMap = daService.getMap("select order_per from com_info_tb where id=? ", new Object[]{comid});
			request.setAttribute("ordepercent", comMap.get("order_per"));
			request.setAttribute("comid", comid);
			return mapping.findForward("adminlist");
		}else if(action.equals("quickquery")){
			String sql = "select * from user_info_tb where comid=? and auth_flag>? and state=? and auth_flag<>? and auth_flag<>?";
			String countSql = "select count(*) from user_info_tb  where comid=? and auth_flag>? and state=? and auth_flag<>? and auth_flag<>?";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;//daService.getPage(sql, null, 1, 20);
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(comid);
			params.add(0);
			params.add(0);
			params.add(14);
			params.add(15);

			if(supperadmin!=1&&isAdmin!=null&&isAdmin==0){//不是车场管理员登录，隐藏车场管理员
				sql +=" and role_id <>? ";
				countSql +=" and role_id <>? ";
				params.add(30);
			}

			Long count = daService.getCount(countSql,params);
			if(count>0){
				list = daService.getAll(sql+ " order by id desc",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("query")){
			String sql = "select * from user_info_tb where comid=? and auth_flag>? and state=? ";
			String countSql = "select count(*) from user_info_tb where  comid=? and auth_flag>? and state=? " ;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo base = new SqlInfo("1=1", new Object[]{comid,0,0});
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"user_info");
			List<Object> params = null;
			if(sqlInfo!=null){
				sqlInfo = SqlInfo.joinSqlInfo(base,sqlInfo, 2);
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params= sqlInfo.getParams();
			}else {
				params= base.getParams();
			}
			if(isAdmin!=null&&isAdmin==0){//不是车场管理员登录，隐藏车场管理员
				sql +=" and role_id <>? ";
				countSql +=" and role_id <>? ";
				params.add(30);
			}
			//System.out.println(sqlInfo);
			Long count= daService.getCount(countSql, params);
			List list = null;//daService.getPage(sql, null, 1, 20);
			if(count>0){
				list = daService.getAll(sql+ " order by id desc", params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			int result = createMember(request,groupid,cityid);
			if(result==1){
				AjaxUtil.ajaxOutput(response, "1");
			}else {
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("edit")){
			String nickname =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname"));
			String strid =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "strid"));
			String phone =RequestUtil.processParams(request, "phone");
			String mobile =RequestUtil.processParams(request, "mobile");
			if(mobile.equals("")){
				mobile = null;
			}
			Integer role_id = RequestUtil.getInteger(request, "role_id", -1);
			Integer isview = RequestUtil.getInteger(request, "isview", -1);
			//修改时间
			Long updateTimeLong = System.currentTimeMillis()/1000;

			Long id =RequestUtil.getLong(request, "id", -1L);
			//Long count = daService.getLong("select count(*) from user_info_tb where mobile=? and role_id=? and id<>? ", new Object[]{mobile,role_id,id});
//			if(count > 0){
//				AjaxUtil.ajaxOutput(response, "-1");
//				return null;
//			}
			String sql = "update user_info_tb set nickname=?,strid=?,phone=?,mobile=?,role_id=?,isview=?,update_time=?  where id=? ";
			int result = daService.update(sql, new Object[]{nickname,strid,phone,mobile,role_id,isview,updateTimeLong,Long.valueOf(id)});
			if(result == 1){
				//判断是否支持ETCPARK车场
				String isSupportETCPark = CustomDefind.ISSUPPORTETCPARK;
				if(isSupportETCPark.equals("1")){
					if(publicMethods.isEtcPark(comid)){
						int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"user_info_tb",Long.valueOf(id),System.currentTimeMillis()/1000,1});
						logger.error("parkadmin or admin:"+nickname+" add comid:"+comid+" parkuser ,add sync ret:"+r);
					}else{
						logger.error("parkadmin or admin:"+nickname+" add comid:"+comid+" parkuser ");
					}
				}else{
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"user_info_tb",Long.valueOf(id),System.currentTimeMillis()/1000,1});
					logger.error("parkadmin or admin:"+nickname+" add comid:"+comid+" parkuser ,add sync ret:"+r);
				}

//				Long total = daService.getLong("select count(*) from user_info_tb where auth_flag=? and comid=? ", new Object[]{1,comid});
//				if(total > 1){//一个车场有多个管理员，提醒一下
//					result = 2;
//				}
				mongoDbUtils.saveLogs(request, 0, 3, "修改了员工："+id+",nickname:"+nickname);
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("adminedit")){
			String nickname =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname"));
			String strid =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "strid"));
			String phone =RequestUtil.processParams(request, "phone");
			String mobile =RequestUtil.processParams(request, "mobile");
			double firstorderquota =Double.parseDouble(RequestUtil.processParams(request, "firstorderquota")+"");
			double rewardquota =Double.parseDouble(RequestUtil.processParams(request, "rewardquota")+"");
			double recommendquota =Double.parseDouble(RequestUtil.processParams(request, "recommendquota")+"");
			double ticketquota =Double.parseDouble(RequestUtil.processParams(request, "ticketquota")+"");
			Integer order_hid = RequestUtil.getInteger(request, "order_hid",0);
			Integer auth_flag = RequestUtil.getInteger(request, "auth_flag", -1);
			Integer isview = RequestUtil.getInteger(request, "isview", -1);
			Long id =RequestUtil.getLong(request, "id", -1L);
			Long role_id =-1L;
			if(auth_flag==1){
				role_id=30L;
			}
//			Long count = daService.getLong("select count(*) from user_info_tb where mobile=? and auth_flag=? and id<>? ", new Object[]{mobile,auth_flag,id});
//			if(count > 0){
//				AjaxUtil.ajaxOutput(response, "-1");
//				return null;
//			}
			String sql = "update user_info_tb set nickname=?,strid=?,phone=?,mobile=?,auth_flag=?,isview=?,firstorderquota=?,rewardquota=?,recommendquota=?,ticketquota=?,order_hid=?,role_id=? where id=? ";
			int result = daService.update(sql, new Object[]{nickname,strid,phone,mobile,auth_flag,isview,firstorderquota,rewardquota,recommendquota,ticketquota,order_hid,role_id,Long.valueOf(id)});
			if(result == 1){
				if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"user_info_tb",Long.valueOf(id),System.currentTimeMillis()/1000,1});
					logger.error("parkadmin or admin:"+loginuin+" edit comid:"+comid+" parkuser ,add sync ret:"+r);
				}else{
					logger.error("parkadmin or admin:"+loginuin+" edit comid:"+comid+" parkuser");
				}
				Long total = daService.getLong("select count(*) from user_info_tb where auth_flag=? and comid=? ", new Object[]{1,comid});
				if(total > 1){//一个车场有多个管理员，提醒一下
					result = 2;
				}
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("editpass")){
			String nickname = request.getSession().getAttribute("loginuin")+"";
			String uin =RequestUtil.processParams(request, "id");
			String sql = "update user_info_tb set password =? ,md5pass=? where id =?";
			String newPass = RequestUtil.processParams(request, "newpass");
			String confirmPass = RequestUtil.processParams(request, "confirmpass");
			String md5pass = newPass;
			try {
				md5pass = StringUtils.MD5(newPass);
				md5pass = StringUtils.MD5(md5pass+"zldtingchebao201410092009");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(newPass.length()<6){
				AjaxUtil.ajaxOutput(response, "密码长度小于6位，请重新输入！");
			}else if(newPass.equals(confirmPass)){
				Object [] values = new Object[]{newPass,md5pass,Long.valueOf(uin)};
				int result = daService.update(sql, values);
				if(result==1){
					if(publicMethods.isEtcPark(comid)){
						int ret = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"user_info_tb",Long.valueOf(uin),System.currentTimeMillis()/1000,1});
						logger.error("parkadmin or admin:"+loginuin+" edit parkuserpass:"+uin+",password:"+newPass+",ret:"+result+",add sync ret："+ret);
					}else{
						logger.error("parkadmin or admin:"+loginuin+" edit parkuserpass:"+uin+",password:"+newPass+",ret:"+result);
					}
					mongoDbUtils.saveLogs(request, 0, 3, "重置了员工："+uin+"的密码");
				}
				AjaxUtil.ajaxOutput(response, result+"");
			}else {
				AjaxUtil.ajaxOutput(response, "两次密码输入不一致，请重新输入！");
			}
			return null;
		}else if(action.equals("delete")){
			String id =RequestUtil.processParams(request, "selids");
			Map userMap = daService.getMap("select * from  user_info_tb  where id =?", new Object[]{Long.valueOf(id)});
			String sql = "update user_info_tb set state =?  where id =?";
			Object [] values = new Object[]{1,Long.valueOf(id)};
			int result = daService.update(sql, values);
			if(result==1){
				//判断是否支持ETCPARK
				String isSupportEtcPark = CustomDefind.ISSUPPORTETCPARK;
				if(isSupportEtcPark.equals("1")){
					if(publicMethods.isEtcPark(comid)){
						int ret = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"user_info_tb",Long.valueOf(id),System.currentTimeMillis()/1000,2});
						logger.error("parkadmin or admin:"+loginuin+" delete parkuserpass:"+id+",password:,ret:"+result+",add sync ret："+ret);
					}else{
						logger.error("parkadmin or admin:"+loginuin+" delete parkuserpass:"+id+",password:,ret:"+result);
					}
				}else{
					int ret = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"user_info_tb",Long.valueOf(id),System.currentTimeMillis()/1000,2});
					logger.error("parkadmin or admin:"+loginuin+" delete parkuserpass:"+id+",password:,ret:"+result+",add sync ret："+ret);
				}
				mongoDbUtils.saveLogs(request, 0, 4, "删除了员工："+userMap);
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("check")){
			String strid = RequestUtil.processParams(request, "value");
			String sql = "select count(*) from user_info_tb where strid =?";
			Long result = daService.getLong(sql, new Object[]{strid});
			if(result>0)
				AjaxUtil.ajaxOutput(response, "1");
			else {
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("isview")){//是否可以收费
			Long id =RequestUtil.getLong(request, "id",-1L);
			Integer isview =RequestUtil.getInteger(request, "isview",-1);
			int ret = 0;
			if(id!=-1&&isview!=-1){
				ret = daService.update("update user_info_tb set isview=? where id =?", new Object[]{isview,id});
				if(ret==1){
					if(publicMethods.isEtcPark(comid)){
						int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"user_info_tb",id,System.currentTimeMillis()/1000,1});
						logger.error("parkadmin or admin:"+loginuin+" edit parkuser isview:"+id+",add sync ret："+r);
					}else{
						logger.error("parkadmin or admin:"+loginuin+" edit parkuser isview:"+id);
					}
				}
			}
			AjaxUtil.ajaxOutput(response, ret+"");
		}else if(action.equals("getrole")){
			List list =null;
			Map orgMap = daService.getMap("select r.id,r.role_name from user_role_tb r,zld_orgtype_tb o where r.oid=o.id and o.name like ? and r.adminid=? and r.type=? ",
					new Object[]{"%车场%", 0, 0});
			if(orgMap != null){
				Long com_id = (Long)request.getSession().getAttribute("comid");
				if(com_id > 0){//车场管理员登录，显示该管理员创建的角色
					list=daService.getAll("select id as value_no,role_name as value_name from user_role_tb where adminid=? or id=?",
							new Object[]{loginuin,orgMap.get("id")});
				}else if(comid > 0){
					list = daService.getAll("select id as value_no,role_name as value_name from user_role_tb where adminid" +
							" in(select id from user_info_tb where comid=? and role_id=?) ", new Object[]{comid,orgMap.get("id")});
					if(list == null){
						list = new ArrayList();
					}
					Map adminMap = new HashMap<String, Object>();
					adminMap.put("value_no", orgMap.get("id"));
					adminMap.put("value_name", orgMap.get("role_name"));
					list.add(0,adminMap);
				}
			}
			String result = "[]";
			if(list!=null&&!list.isEmpty()){
				result = StringUtils.createJson(list);
			}
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("setorderpercent")){
			Long comId =  RequestUtil.getLong(request, "comid", -1L);
			Integer percent = RequestUtil.getInteger(request, "order_per", 0);
			int ret =0;
			if(comid>0&&percent>0){
				ret = daService.update("update com_info_tb set order_per=? where id =?", new Object[]{percent,comId});
			}
			AjaxUtil.ajaxOutput(response, ret+"");
		}
		return null;
	}


	//注册停车场收费员帐号
	@SuppressWarnings({ "rawtypes" })
	private int createMember(HttpServletRequest request,Long groupId,Long cityId){
		String strid = "";
		String nickname =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname"));
		String phone =RequestUtil.processParams(request, "phone");
		String mobile =RequestUtil.processParams(request, "mobile");
		Long auth_flag =RequestUtil.getLong(request, "auth_flag", -1L);
		String loginuin = request.getSession().getAttribute("loginuin")+"";
		Long role_id =RequestUtil.getLong(request, "role_id", -1L);
		Integer isview = RequestUtil.getInteger(request, "isview", 1);
		Long sex = RequestUtil.getLong(request,"sex",-1L);
		if(nickname.equals("")) nickname=null;
		if(phone.equals("")) phone=null;
		if(mobile.equals("")) mobile=null;
		Map adminMap = (Map) request.getSession().getAttribute("userinfo");
		Long time = System.currentTimeMillis()/1000;
		//用户表
		Long nextid = daService.getLong(
				"SELECT nextval('seq_user_info_tb'::REGCLASS) AS newid", null);
		//定义登录账号为主键id
		strid = String.valueOf(nextid);
		//定义user_id
		String userId = strid;
		if(!commonMethods.checkStrid(strid))
			return 0;
		Long comId = (Long)request.getSession().getAttribute("comid");
		if(comId == null || comId==0)
			comId = RequestUtil.getLong(request, "comid", 0L);

		if(groupId==null||groupId<0){
			Map<String,Object> comMap = daService.getMap("select groupid from com_info_tb where id =? ",new Object[]{comId});
			if(comMap!=null)
				groupId = (Long)comMap.get("groupid");
		}
		logger.error("groupid:"+groupId);
		if(auth_flag==1){//总后台设置的管理员，默认为后台车场管理员
			role_id=30L;
		}else if(auth_flag==-1)
			auth_flag=2L;
		if(role_id == 30){
			auth_flag = 1L;
		}
		String sql="insert into user_info_tb (id,nickname,password,strid," +
				"address,reg_time,mobile,phone,auth_flag,comid,role_id,isview,user_id,cityid,groupid,sex) " +
				"values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Object [] values= new Object[]{nextid,nickname,strid,strid,
				adminMap.get("address"),time,mobile,phone,auth_flag,comId,role_id,isview,userId,cityId,groupId,sex};
		int r = daService.update(sql, values);
		if(r==1){
			//判断是否支持验证ETCPark
			String isSupportEtcPark = CustomDefind.ISSUPPORTETCPARK;
			if(isSupportEtcPark.equals("1")){
				if(publicMethods.isEtcPark(comId)){
					int ret = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comId,"user_info_tb",nextid,System.currentTimeMillis()/1000,0});
					logger.error("parkadmin or admin:"+loginuin+" add uid:"+nextid+" parkuser ,add sync ret:"+ret);
				}else{
					logger.error("parkadmin or admin:"+loginuin+" add uid:"+nextid+" parkuser ");
				}
			}else{
				int ret = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comId,"user_info_tb",nextid,System.currentTimeMillis()/1000,0});
				logger.error("parkadmin or admin:"+loginuin+" add uid:"+nextid+" parkuser ,add sync ret:"+ret);
			}
			mongoDbUtils.saveLogs( request,0, 2, "添加了车场成员，手机号："+mobile);
		}
		return r;
	}
	/*//注册停车场收费员帐号
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean createMember(HttpServletRequest request){
		String strid =RequestUtil.processParams(request, "strid");
		String nickname =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname"));
		String phone =RequestUtil.processParams(request, "phone");
		String mobile =RequestUtil.processParams(request, "mobile");
		String role =RequestUtil.processParams(request, "other_flag");
		if(role.equals("")||role.equals("-1"))
			role="2";//默认为收费员
		if(nickname.equals("")) nickname=null;
		if(phone.equals("")) phone=null;
		if(mobile.equals("")) mobile=null;
		Map adminMap = (Map) request.getSession().getAttribute("amdininfo");
		Long time = System.currentTimeMillis()/1000;
		if(!checkStrid(strid))
			return false;
		Long departId = Long.valueOf(adminMap.get("department_id")+"");
		Long uin =  daService.getLong("SELECT nextval('seq_uin'::REGCLASS) AS newid", null);
		Long comId = (Long)request.getSession().getAttribute("company_id");
		List<Map> sqlsList = new ArrayList<Map>();
		//用户表
		Map userMap = new HashMap();
		userMap.put("sql", "insert into user_info_tb (uin,nickname,password,strid,company,address,reg_time,department_id,mobile,phone,other_flag,company_id) " +
				"values (?,?,?,?,?,?,?,?,?,?,?,?)");
		userMap.put("values", new Object[]{uin,nickname,"666666",strid,adminMap.get("company"),adminMap.get("address"),time,departId,mobile,phone,Long.valueOf(role),comId});
		//online表
		Map onlineMap = new HashMap();

		onlineMap.put("sql", "insert into user_online_tb (uin,strid,nickname,main_flag,stradmstrid)" +
				"values(?,?,?,?,?)");
		onlineMap.put("values", new Object[]{uin,strid,nickname,new Integer(11536276),adminMap.get("strid")});
		//fuwu表
		Map fuwuMap = new HashMap();
		fuwuMap.put("sql", "insert into fuwu (uin,strid,xzmp,huihua,mail,sib,opendate,openyear,if_tryout,opentype,admopenusers,fftype) " +
				"values(?,?,?,?,?,?,?,?,?,?,?,?)");
		fuwuMap.put("values", new Object[]{uin,strid,1,1,1,1,new Timestamp(time*1000),0,100,0,0,0});

		sqlsList.add(userMap);
		sqlsList.add(onlineMap);
		sqlsList.add(fuwuMap);
		boolean r = daService.bathUpdate(sqlsList);
		return r;
	}*/
	public static void main(String[] args) {
		String a  = "{com_led_tb:{\"id\":\"16\",\"ledip\":\"192.68.11.2\",\"ledport\":\"8888\",\"leduid\":\"1\",\"movemode\":\"0\",\"movespeed\":\"1\",\"dwelltime\":\"1\",\"ledcolor\":\"0\",\"showcolor\":\"0\",\"typeface\":\"1\",\"typesize\":\"0\",\"matercont\":\"4\",\"passid\":\"3\",\"operate\":\"1\"},com_led_tb:{\"id\":\"17\",\"ledip\":\"192.158.1.1\",\"ledport\":\"8888\",\"leduid\":\"2\",\"movemode\":\"0\",\"movespeed\":\"0\",\"dwelltime\":\"0\",\"ledcolor\":\"0\",\"showcolor\":\"0\",\"typeface\":\"1\",\"typesize\":\"0\",\"matercont\":\"3\",\"passid\":\"27\",\"operate\":\"1\"},user_info_tb:{\"id\":\"21892\",\"nickname\":\"车主\",\"password\":\"null\",\"strid\":\"carower_21892\",\"sex\":\"null\",\"email\":\"null\",\"phone\":\"null\",\"mobile\":\"11111111111\",\"address\":\"null\",\"resume\":\"null\",\"reg_time\":\"1442311127\",\"logon_time\":\"null\",\"logoff_time\":\"null\",\"online_flag\":\"null\",\"comid\":\"null\",\"auth_flag\":\"4\",\"balance\":\"0.00\",\"state\":\"0\",\"recom_code\":\"null\",\"md5pass\":\"null\",\"cid\":\"null\",\"department_id\":\"null\",\"media\":\"10\",\"isview\":\"1\",\"collector_pics\":\"0\",\"collector_auditor\":\"null\",\"imei\":\"null\",\"client_type\":\"0\",\"version\":\"null\",\"wxp_openid\":\"null\",\"wx_name\":\"null\",\"wx_imgurl\":\"null\",\"shop_id\":\"null\",\"firstorderquota\":\"8.00\",\"rewardquota\":\"2.00\",\"recommendquota\":\"5.00\",\"reward_score\":\"0\",\"is_auth\":\"0\",\"credit_limit\":\"null\",\"ticketquota\":\"-1.00\",\"operate\":\"0\"},carower_product:{\"id\":\"350\",\"pid\":\"54\",\"uin\":\"21892\",\"create_time\":\"\"442466288\",\"b_time\":\"1442419200\",\"e_time\":\"1445011200\",\"total\":\"457.00\",\"remark\":\"\",\"name\":\"\",\"address\":\"\",\"operate\":\"0\"}}";
//		JSONArray jo = JSONArray.fromObject(a);
		JSONObject jo = JSONObject.fromObject(a);
		jo.getJSONArray("com_led_tb");
		boolean b = jo.containsKey("com_led_tb");
		System.out.println();
	}
}