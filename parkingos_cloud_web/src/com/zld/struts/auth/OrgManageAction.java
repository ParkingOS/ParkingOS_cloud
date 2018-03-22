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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrgManageAction extends Action {
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
			Long pid = RequestUtil.getLong(request, "pid", -1L);
			request.setAttribute("pid", pid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select * from zld_orgtype_tb where pid=? and state=? " ;
			String countSql = "select count(*) from zld_orgtype_tb where pid=? and state=? " ;
			Long pid = RequestUtil.getLong(request, "pid", 0L);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(pid);
			params.add(0);
			Long count = daService.getCount(countSql,params);
			if(count>0){
				list = daService.getAll(sql +" order by sort ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("orgtree")){
			request.setAttribute("treeurl", "getdata.do?action=orgtree");
			request.setAttribute("title", "组织类型");
			return mapping.findForward("tree");
		}else if(action.equals("orgmanage")){
			request.setAttribute("treeurl", "getdata.do?action=orgmanage");
			request.setAttribute("title", "组织类型");
			return mapping.findForward("tree");
		}else if(action.equals("orgrole")){
			request.setAttribute("treeurl", "getdata.do?action=orgrole");
			request.setAttribute("title", "组织类型");
			return mapping.findForward("tree");
		}else if(action.equals("create")){
			Long pid = RequestUtil.getLong(request, "pid", -1L);
			String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
			Integer state = RequestUtil.getInteger(request, "state", 0);
			if(pid > -1){
				Long orgid = daService.getkey("seq_zld_orgtype_tb");
				List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
				//组织类型
				Map<String, Object> orgtypeSqlMap = new HashMap<String, Object>();
				//组织管理员
				Map<String, Object> roleSqlMap = new HashMap<String, Object>();
				orgtypeSqlMap.put("sql", "insert into zld_orgtype_tb(id, name,state,pid,create_time,creator_id) values(?,?,?,?,?,?) ");
				orgtypeSqlMap.put("values", new Object[]{orgid, name, state, pid, System.currentTimeMillis()/1000, uin});
				bathSql.add(orgtypeSqlMap);
				roleSqlMap.put("sql", "insert into user_role_tb(role_name,state,oid,create_time,adminid,type) values(?,?,?,?,?,?) ");
				roleSqlMap.put("values", new Object[]{name + "管理员", state, orgid, System.currentTimeMillis()/1000, uin, 0});
				bathSql.add(roleSqlMap);
				boolean b = daService.bathUpdate(bathSql);
				if(b){
					AjaxUtil.ajaxOutput(response, "1");
				}else{
					AjaxUtil.ajaxOutput(response, "0");
				}
				return null;
			}
			AjaxUtil.ajaxOutput(response, "0");
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
			Integer state = RequestUtil.getInteger(request, "state", 0);
			if(id > 0){
				List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
				//组织类型
				Map<String, Object> orgtypeSqlMap = new HashMap<String, Object>();
				//组织管理员
				Map<String, Object> roleSqlMap = new HashMap<String, Object>();
				orgtypeSqlMap.put("sql", "update zld_orgtype_tb set name=?,state=?,update_time=? where id=? ");
				orgtypeSqlMap.put("values", new Object[]{name, state, System.currentTimeMillis()/1000, id});
				bathSql.add(orgtypeSqlMap);
				roleSqlMap.put("sql", "update user_role_tb set role_name=? where oid=? and type=? ");
				roleSqlMap.put("values", new Object[]{name + "管理员", id, 0});
				bathSql.add(roleSqlMap);
				boolean b = daService.bathUpdate(bathSql);
				if(b){
					AjaxUtil.ajaxOutput(response, "1");
				}else{
					AjaxUtil.ajaxOutput(response, "0");
				}
				return null;
			}
			AjaxUtil.ajaxOutput(response, "0");
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			if(id > 0){
				int r = daService.update("update zld_orgtype_tb set state=? where id=? ",
						new Object[]{1, id});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "0");
		}
		return null;
	}
}
