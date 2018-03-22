package com.zld.struts.admin;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
import com.zld.service.LogService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.StringUtils;
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
 * 市场专员管理，在总管理员后台
 * @author Administrator
 *
 */
public class MarketerManageAction extends Action{

	@Autowired
	private DataBaseService daService;
	@Autowired
	private LogService logService;

	private Logger logger = Logger.getLogger(MarketerManageAction.class);

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		request.setAttribute("authid", request.getParameter("authid"));
		Long loginuin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		if(loginuin == null){
			response.sendRedirect("login.do");
			return null;
		}
		Map<String, Object> adminRoleMap = daService.getMap("select * from user_role_tb where type=? and oid =(select id from zld_orgtype_tb where name like ? limit ? ) limit ? ",
				new Object[]{0, "%BOSS%", 1, 1});//查找管理员角色
		if(adminRoleMap == null){
			return null;
		}
		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("query")){
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"user_info", "u", new String[]{});
			String sql = "select u.* from user_info_tb u,user_role_tb r where u.role_id=r.id and u.state=? and r.state=? and r.oid=? ";
			String countSql = "select count(u.*) from user_info_tb u,user_role_tb r where u.role_id=r.id and u.state=? and r.state=? and r.oid=? ";
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(0);
			params.add(adminRoleMap.get("oid"));
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			List<Map<String, Object>> list = null;
			Long count = daService.getCount(countSql, params);
			if(count > 0){
				list = daService.getAll(sql, params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			int result = createMember(request);
			if(result==1)
				AjaxUtil.ajaxOutput(response, "1");
			else {
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("edit")){
			String nickname =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname"));
			String strid =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "strid"));
			String phone =RequestUtil.processParams(request, "phone");
			String mobile =RequestUtil.processParams(request, "mobile");
			Long id =RequestUtil.getLong(request, "id",-1L);
			Long role_id = RequestUtil.getLong(request, "role_id", -1L);
			if(role_id < 0){
				AjaxUtil.ajaxOutput(response, "0");
				return null;
			}
			String sql = "update user_info_tb set nickname=?,strid=?,phone=?,mobile=?,role_id=? where id=?";
			Object [] values = new Object[]{nickname,strid,phone,mobile,role_id,id};
			int result = daService.update(sql, values);
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "selids", -1L);
			String sql = "update user_info_tb set state=? where id =?";
			Object [] values = new Object[]{1, id};
			int result = daService.update(sql, values);
			if(result==1)
				logService.updateSysLog(comid,request.getSession().getAttribute("loginuin")+"","禁用了停车场人员,编号："+id, 204);
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("cominfo")){
			Integer uin = RequestUtil.getInteger(request, "id", -1);
			if(uin!=-1){
				Map userInfo = daService.getPojo("select comid from user_info_Tb where id=?", new Object[]{uin});
				if(userInfo!=null&&userInfo.get("comid")!=null){
					Map cominfoMap =  daService.getPojo("select * from com_info_tb  where id=? ", new Object[]{userInfo.get("comid")});
					request.setAttribute("cominfo", cominfoMap);
				}
			}
			return mapping.findForward("cominfo");
			//http://127.0.0.1/zld/marketer.do?action=editpass&id=10133&r=1404548466661
		}else if(action.equals("editpass")){
			int result = 0;
			Long id = RequestUtil.getLong(request, "id", -1L);
			String confirmpass = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "confirmpass"));
			String newpass =  AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "newpass"));
			String md5pass = newpass;
			try {
				md5pass = StringUtils.MD5(newpass);
				md5pass = StringUtils.MD5(md5pass+"zldtingchebao201410092009");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(id!=-1&&!newpass.equals("")&&newpass.equals(confirmpass)){
				result = daService.update("update user_info_tb set password=?,md5pass=?  where id =?",
						new Object[]{newpass,md5pass,id});
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}
		else if(action.equals("check")){
			String strid = RequestUtil.processParams(request, "value");
			boolean b = checkStrid(strid);
			if(!b)
				AjaxUtil.ajaxOutput(response, "1");
			else {
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("getzldrole")){
			List<Map<String, Object>> list = daService.getAll("select id as value_no,role_name as value_name" +
					" from user_role_tb where adminid =? and oid=? " , new Object[]{loginuin, adminRoleMap.get("oid")});
			Map adminMap = new HashMap<String, Object>();
			adminMap.put("value_no", -1);
			adminMap.put("value_name", "请选择");
			list.add(0,adminMap);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(list));
		}
		return null;
	}
	//注册停车场收费员帐号
	@SuppressWarnings({ "rawtypes" })
	private int createMember(HttpServletRequest request){
		String strid =RequestUtil.processParams(request, "strid");
		String nickname =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname"));
		String phone =RequestUtil.processParams(request, "phone");
		String mobile =RequestUtil.processParams(request, "mobile");
		Long role_id =RequestUtil.getLong(request, "role_id", -1L);
		if(nickname.equals("")) nickname=null;
		if(phone.equals("")) phone=null;
		if(mobile.equals("")) mobile=null;
		if(role_id < 0){
			return 0;
		}
		Map adminMap = (Map) request.getSession().getAttribute("userinfo");
		Long time = System.currentTimeMillis()/1000;
		if(!checkStrid(strid))
			return 0;
		Long comId = (Long)request.getSession().getAttribute("comid");
		//用户表
		String sql="insert into user_info_tb (nickname,password,strid," +
				"address,reg_time,mobile,phone,role_id,comid) " +
				"values (?,?,?,?,?,?,?,?,?)";
		Object [] values= new Object[]{nickname,strid,strid,
				adminMap.get("address"),time,mobile,phone,role_id,comId};
		int r = daService.update(sql, values);
		return r;
	}

	private boolean checkStrid(String strid){
		String sql = "select count(*) from user_info_tb where strid =? ";
		Long result = daService.getLong(sql, new Object[]{strid});
		if(result > 0){
			return false;
		}
		return true;
	}
}