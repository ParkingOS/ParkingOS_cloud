package com.zld.struts.group;

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
import java.util.Map;
//废弃
public class GroupManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@SuppressWarnings("rawtypes")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Integer supperadmin = (Integer)request.getSession().getAttribute("supperadmin");//是否是超级管理员
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}

		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String sql = "select * from org_group_tb where state=? ";
			String countsql = "select count(id) from org_group_tb where state=? ";
			List<Map<String, Object>> list = null;
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			Long count = daService.getCount(countsql, params);
			if(count > 0){
				list = daService.getAll(sql, params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "name"));
			Long cityid = RequestUtil.getLong(request, "cityid", -1L);
			Long chanid = RequestUtil.getLong(request, "chanid", -1L);
			Integer type = RequestUtil.getInteger(request, "type", 0);
			Integer state = RequestUtil.getInteger(request, "state", 0);
			int ret = 0;
			ret = daService.update("insert into org_group_tb (name,state,create_time,chanid,cityid,type) values(?,?,?,?,?,?) ",
					new Object[]{name, state, System.currentTimeMillis()/1000, chanid, cityid, type});
			AjaxUtil.ajaxOutput(response, ret+"");
			return null;
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Integer state = RequestUtil.getInteger(request, "state", 0);
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "name"));
			Integer type = RequestUtil.getInteger(request, "type", 0);
			Long cityid = RequestUtil.getLong(request, "cityid", -1L);
			Long chanid = RequestUtil.getLong(request, "chanid", -1L);
			int	r = daService.update("update org_group_tb set name =?,state=?,type=?,cityid=?,chanid=? where id=?",
					new Object[]{name,state,type,cityid, chanid, id});
			AjaxUtil.ajaxOutput(response, "" + r);
			return null;
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			int	r = daService.update("update org_group_tb set state=? where id=?",
					new Object[]{1, id});
			AjaxUtil.ajaxOutput(response, "" + r);
		}else if(action.equals("set")){
			Long groupid = RequestUtil.getLong(request, "id", -1L);
			request.setAttribute("treeurl", "getdata.do?action=groupsetting&groupid="+groupid);
			request.setAttribute("title", "运营集团设置");
			return mapping.findForward("tree");
		}
		return null;
	}
}
