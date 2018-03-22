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
import java.util.List;
import java.util.Map;


/**
 * 权限管理---角色管理
 * @author Gecko
 *
 */
public class AuthUserRoleAction extends Action {

	@Autowired
	private DataBaseService daService;
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		request.setAttribute("authid", request.getParameter("authid"));
		Long loginuin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		if(loginuin == null){
			response.sendRedirect("login.do");
			return null;
		}
		Map<String, Object> adminRoleMap = daService.getMap("select * from user_role_tb where type=? and oid =(select id from zld_orgtype_tb where name like ? limit ? ) limit ? ",
				new Object[]{0, "%BOSS%", 1, 1});//查找管理员角色
		if(action.equals("")){
			return mapping.findForward("orglist");
		}else if(action.equals("query")){
			String sql = "select * from user_role_tb  where adminid=? and oid=? order by id";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Map<String, Object>> list = daService.getAll(sql,
					new Object[]{loginuin, adminRoleMap.get("oid")});
			int count =0;
			if(list!=null)
				count = list.size();
			String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}
		return null;
	}

}
