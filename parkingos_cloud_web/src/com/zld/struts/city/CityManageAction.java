package com.zld.struts.city;

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

public class CityManageAction extends Action {
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
			String sql = "select * from org_city_merchants where state=? " ;
			String countSql = "select count(*) from org_city_merchants where state=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			Long count = daService.getCount(countSql,params);
			if(count>0){
				list = daService.getAll(sql +" order by ctime desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("create")){
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "name"));
			String union_id = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "union_id"));
			String ukey = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "ukey"));
			int r = daService.update("insert into org_city_merchants(name, ctime, union_id, ukey) values(?,?,?,?) ",
					new Object[]{name, System.currentTimeMillis()/1000, union_id, ukey});
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "name"));
			String union_id = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "union_id"));
			String ukey = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "ukey"));
			if(id > 0){
				int r = daService.update("update org_city_merchants set name=?, union_id=?, ukey=? where id=? ",
						new Object[]{name, union_id, ukey, id});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "0");
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			if(id > 0){
				int r = daService.update("update org_city_merchants set state=? where id=? ",
						new Object[]{1, id});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "0");
		}else if(action.equals("set")){
			Long cityid = RequestUtil.getLong(request, "id", -1L);
			request.setAttribute("treeurl", "getdata.do?action=citysetting&cityid="+cityid);
			request.setAttribute("title", "城市设置");
			return mapping.findForward("tree");
		}
		return null;
	}
}
