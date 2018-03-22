package com.zld.struts.auth;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
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

public class RoleManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@SuppressWarnings("rawtypes")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("query")){
			String sql = "select * from user_role_tb where state=? and ((oid=? and type=?) or (oid=? and adminid=?))" ;
			String countSql = "select count(*) from user_role_tb where state=? and ((oid=? and type=?) or (oid=? and adminid=?)) " ;
			Long oid = RequestUtil.getLong(request, "oid", 0L);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(oid);
			params.add(0);
			params.add(oid);
			params.add(uin);
			Long count = daService.getCount(countSql,params);
			if(count>0){
				list = daService.getAll(sql +" order by id desc  ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("orgrole")){
			Long oid = RequestUtil.getLong(request, "oid", -1L);
			request.setAttribute("oid", oid);
			return mapping.findForward("list");
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String role_name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "role_name"));
			String resume = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "resume"));
			if(id > -1){
				Long count = daService.getLong("select count(*) from user_role_tb where id=? and type=? ",
						new Object[]{id, 0});
				if(count > 0){
					AjaxUtil.ajaxOutput(response, "-2");
				}else{
					int r = daService.update("update user_role_tb set role_name=?,resume=?, update_time=? where id=? ",
							new Object[]{role_name, resume, System.currentTimeMillis()/1000, id});
					AjaxUtil.ajaxOutput(response, r + "");
				}
				return null;
			}
			AjaxUtil.ajaxOutput(response, "0");
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			if(id > -1){
				Long count = daService.getLong("select count(*) from user_role_tb where id=? and type=? ",
						new Object[]{id, 0});
				if(count > 0){
					AjaxUtil.ajaxOutput(response, "-2");
					return null;
				}
				count = daService.getLong("select count(id) from user_info_tb where role_id=? and state<>? ",
						new Object[]{id, 1});
				if(count > 0){
					AjaxUtil.ajaxOutput(response, "-3");
					return null;
				}
				int r = daService.update("update user_role_tb set state=? where id=? ",
						new Object[]{1, id});
				AjaxUtil.ajaxOutput(response, "" + r);
				return null;
			}
			AjaxUtil.ajaxOutput(response, "0");
		}
		return null;
	}
}
