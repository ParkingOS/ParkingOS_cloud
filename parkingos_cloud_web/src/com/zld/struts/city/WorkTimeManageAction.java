package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class WorkTimeManageAction extends Action{
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;

	@SuppressWarnings("rawtypes")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			Long role_id = RequestUtil.getLong(request, "role_id", -1L);
			request.setAttribute("role_id", role_id);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Long role_id = RequestUtil.getLong(request, "role_id", -1L);
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(role_id);
			params.add(0);
			List list = null;
			String sql = "select * from work_time_tb where role_id=? and is_delete=? " ;
			String countSql = "select count(*) from work_time_tb where role_id=? and is_delete=? " ;
			Long count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by id desc ", params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list, pageNum, count, fieldsstr, "id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("create")){
			Long role_id = RequestUtil.getLong(request, "role_id", -1L);
			Integer b_hour = RequestUtil.getInteger(request, "b_hour", 0);//-- 上班(小时)
			Integer b_minute = RequestUtil.getInteger(request, "b_minute", 0);//-- 上班（分钟）
			Integer e_hour = RequestUtil.getInteger(request, "e_hour", 0);//-- 下班(小时)
			Integer e_minute = RequestUtil.getInteger(request, "e_minute", 0);//-- 下班(分钟)
			if(role_id > 0){
				int r = daService.update("insert into work_time_tb(b_hour,b_minute,e_hour,e_minute,role_id) " +
								"values(?,?,?,?,?)",
						new Object[]{b_hour, b_minute, e_hour, e_minute, role_id});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "-1");
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Integer b_hour = RequestUtil.getInteger(request, "b_hour", 0);//-- 上班(小时)
			Integer b_minute = RequestUtil.getInteger(request, "b_minute", 0);//-- 上班（分钟）
			Integer e_hour = RequestUtil.getInteger(request, "e_hour", 0);//-- 下班(小时)
			Integer e_minute = RequestUtil.getInteger(request, "e_minute", 0);//-- 下班(分钟)
			if(groupid > 0){
				int r = daService.update("update work_time_tb set b_hour=?,b_minute=?,e_hour=?,e_minute=? where id=? ",
						new Object[]{b_hour, b_minute, e_hour, e_minute, id});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "-1");
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			int r = daService.update("update work_time_tb set is_delete=? where id=? ",
					new Object[]{1, id});
			AjaxUtil.ajaxOutput(response, r + "");
		}
		return null;
	}
}
