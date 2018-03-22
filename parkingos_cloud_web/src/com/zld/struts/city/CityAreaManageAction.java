package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
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

public class CityAreaManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;

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
		if(cityid == null && groupid == null){
			return null;
		}
		if(cityid == null) cityid = -1L;
		if(groupid == null) groupid = -1L;

		if(cityid > 0){
			groupid = RequestUtil.getLong(request, "groupid", -1L);
		}
		if(action.equals("")){
			request.setAttribute("cityid", cityid);
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String sql = "select * from org_area_tb where state=? " ;
			String countSql = "select count(*) from org_area_tb where state=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			List<Object> groups = new ArrayList<Object>();
			if(cityid > 0){//城市商户登录
				groups = commonMethods.getGroups(cityid);
			}else if(groupid > 0){//运营集团登录
				groups.add(groupid);
			}
			if(groups != null && !groups.isEmpty()){
				String preParams  ="";
				for(Object grouid : groups){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " and groupid in ("+preParams+") ";
				countSql += " and groupid in ("+preParams+") ";
				params.addAll(groups);

				count = pgOnlyReadService.getCount(countSql,params);
				if(count>0){
					list = pgOnlyReadService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
				}
			}

			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("create")){
			int r = createArea(request, groupid);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			int r = editArea(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("delete")){
			int r = deleteArea(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}
		return null;
	}

	private int deleteArea(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		int r = daService.update("update org_area_tb set state=? where id=? ",
				new Object[]{1, id});
		return r;
	}

	private int createArea(HttpServletRequest request, Long groupid){
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
		Integer state = RequestUtil.getInteger(request, "state", 0);
		if(groupid == -1){
			return -1;
		}
		int r = daService.update("insert into org_area_tb(name,state,groupid,create_time) values(?,?,?,?)",
				new Object[]{name, state, groupid, System.currentTimeMillis()/1000});
		return r;
	}

	private int editArea(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
		Integer state = RequestUtil.getInteger(request, "state", 0);
		int r = daService.update("update org_area_tb set name=?,state=? where id=? ",
				new Object[]{name, state, id});
		return r;
	}
}
