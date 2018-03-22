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

public class FunctionManageAction extends Action {
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
		if(action.equals("")){
			Long pid = RequestUtil.getLong(request, "pid", 0L);
			Long oid = RequestUtil.getLong(request, "oid", 0L);
			request.setAttribute("pid", pid);
			request.setAttribute("oid", oid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select * from auth_tb where pid=? and oid=? and state=? " ;
			String countSql = "select count(*) from auth_tb where pid=? and oid=? and state=? " ;
			Long pid = RequestUtil.getLong(request, "pid", 0L);
			Long oid = RequestUtil.getLong(request, "oid", 0L);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(pid);
			params.add(oid);
			params.add(0);
			Long count = daService.getCount(countSql,params);
			if(count>0){
				list = daService.getAll(sql +" order by sort ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("functree")){
			Long oid = RequestUtil.getLong(request, "oid", -1L);
			request.setAttribute("treeurl", "getdata.do?action=functree&oid="+oid);
			request.setAttribute("title", "功能列表");
			return mapping.findForward("tree");
		}else if(action.equals("create")){
			Long oid = RequestUtil.getLong(request, "oid", -1L);
			Long pid = RequestUtil.getLong(request, "pid", -1L);
			String nname = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nname"));
			String url = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "url"));
			Integer state = RequestUtil.getInteger(request, "state", 0);
			String sub_auth = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "sub_auth"));
			String actions = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "actions"));
			if(oid > -1 && pid > -1){
				int r = daService.update("insert into auth_tb(nname,state,pid,url,oid,sub_auth,actions) values(?,?,?,?,?,?,?)",
						new Object[]{nname, state, pid, url, oid, sub_auth, actions});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "0");
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String nname = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nname"));
			String url = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "url"));
			String sub_auth = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "sub_auth"));
			String actions = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "actions"));
			Integer state = RequestUtil.getInteger(request, "state", 0);
			if(id > 0){
				int r = daService.update("update auth_tb set nname=?,state=?,url=?,sub_auth=?,actions=? where id=? ",
						new Object[]{nname, state, url, sub_auth, actions, id});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "0");
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			if(id > 0){
				int r = daService.update("update auth_tb set state=?,delete_time=?,deletor_id=? where id=? ",
						new Object[]{1, System.currentTimeMillis()/1000, uin, id});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "0");
		}
		return null;
	}
}
