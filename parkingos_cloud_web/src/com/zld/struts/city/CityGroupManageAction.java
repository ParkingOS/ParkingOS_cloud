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
import java.util.Map;

public class CityGroupManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@SuppressWarnings("rawtypes")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		Long chanid = (Long)request.getSession().getAttribute("chanid");
		Integer supperadmin = (Integer)request.getSession().getAttribute("supperadmin");//是否是超级管理员
		request.setAttribute("authid", request.getParameter("authid"));
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(supperadmin == 0 && cityid == null && chanid == null){
			return null;
		}
		if(cityid == null) cityid = -1L;
		if(chanid == null) chanid = -1L;

		if(supperadmin == 1){
			cityid = RequestUtil.getLong(request, "cityid", -1L);
			chanid = RequestUtil.getLong(request, "chanid", -1L);
		}
		if(action.equals("")){
			request.setAttribute("supperadmin", supperadmin);
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
			if(cityid > 0){
				sql += " and cityid=? ";
				countsql += " and cityid=? ";
				params.add(cityid);
			}else if(chanid > 0){
				sql += " and chanid=? ";
				countsql += " and chanid=? ";
				params.add(chanid);
			}
			sql += " order by id desc";
			Long count = daService.getCount(countsql, params);
			if(count > 0){
				list = daService.getAll(sql, params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			int r = createGroup(request, cityid, chanid);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			int r = editGroup(request, cityid);
			AjaxUtil.ajaxOutput(response, "" + r);
			return null;
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			int	r = daService.update("update org_group_tb set state=? where id=?",
					new Object[]{1,id});
			AjaxUtil.ajaxOutput(response, "" + r);
		}else if(action.equals("set")){
			Long groupid = RequestUtil.getLong(request, "id", -1L);
			request.setAttribute("treeurl", "getdata.do?action=groupsetting&groupid="+groupid);
			request.setAttribute("title", "运营集团设置");
			return mapping.findForward("tree");
		}

		return null;
	}

	private int editGroup(HttpServletRequest request, Long cityid){
		Long id = RequestUtil.getLong(request, "id", -1L);
		Integer state = RequestUtil.getInteger(request, "state", 0);
		String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "name"));
		Integer type = RequestUtil.getInteger(request, "type", 0);
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		Double longitude = RequestUtil.getDouble(request, "longitude", 0d);
		Double latitude = RequestUtil.getDouble(request, "latitude", 0d);
		String serverid = RequestUtil.getString(request, "serverid");
		if(address.equals("")) address = null;
		if(longitude == 0) longitude = null;
		if(latitude == 0) latitude = null;
		String sql = "update org_group_tb set name=?,state=?,type=?,address=?,longitude=?,latitude=?,cityid=?, serverid=? where id=? ";
		int	r = daService.update(sql, new Object[]{name, state, type, address, longitude, latitude, cityid, serverid, id});
		return r;
	}

	private int createGroup(HttpServletRequest request, Long cityid, Long chanid){
		String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "name"));
		Integer type = RequestUtil.getInteger(request, "type", 0);
		Integer state = RequestUtil.getInteger(request, "state", 0);
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		Double longitude = RequestUtil.getDouble(request, "longitude", 0d);
		Double latitude = RequestUtil.getDouble(request, "latitude", 0d);
		String serverid = RequestUtil.getString(request, "serverid");
		if(address.equals("")) address = null;
		if(longitude == 0) longitude = null;
		if(latitude == 0) latitude = null;
		int r  = daService.update("insert into org_group_tb (name,state,create_time,cityid,type,chanid,address,longitude,latitude,serverid) " +
						" values(?,?,?,?,?,?,?,?,?,?) ",
				new Object[]{name, state, System.currentTimeMillis()/1000, cityid, type, chanid, address, longitude, latitude, serverid});
		return r;
	}
}
