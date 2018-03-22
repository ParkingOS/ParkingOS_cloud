package com.zld.struts.anlysis;

import com.zld.AjaxUtil;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.StringUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParkAccountAnlysisAction extends Action {
	@Autowired
	private PgOnlyReadService daService;
	@Override
	public ActionForward execute(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response) throws Exception{
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		Integer role = RequestUtil.getInteger(request, "role",-1);
		request.setAttribute("role", role);
		request.setAttribute("authid", request.getParameter("authid"));
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		if(comid==null){
			response.sendRedirect("login.do");
			return null;
		}
		if(comid==0)
			comid = RequestUtil.getLong(request, "comid", 0L);
		if(groupid != null && groupid > 0){
			request.setAttribute("groupid", groupid);
			if(comid == null || comid <= 0){
				Map map = daService.getMap("select id,company_name from com_info_tb where groupid=? order by id limit ? ", 
						new Object[]{groupid, 1});
				if(map != null){
					comid = (Long)map.get("id");
				}else{
					comid = -999L;
				}
			}
		}
		if(action.equals("")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			request.setAttribute("etime", df2.format(System.currentTimeMillis()));
			request.setAttribute("btime", df2.format(System.currentTimeMillis()));
			String isparkcloud=RequestUtil.getString(request, "isparkcloud");
			request.setAttribute("isparkcloud",isparkcloud);
			request.setAttribute("comid", comid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String isparkcloud=RequestUtil.getString(request, "isparkcloud");
			String sql = "select * from park_account_tb ";
			String countSql = "select count(ID) as count,sum(amount) as total from park_account_tb " ;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"park_account");
			List<Object> params = new ArrayList<Object>();
			if(sqlInfo!=null){
				countSql+=" where  "+ sqlInfo.getSql();
				sql +=" where "+sqlInfo.getSql();
				params= sqlInfo.getParams();
			}
			if(isparkcloud.equals("1")){
				if(countSql.indexOf("where")!=-1){
					countSql +=" and comid=? ";
					sql +=" and comid=? ";
				}else {
					countSql +=" where comid=? ";
					sql +=" where comid=? ";
				}
				params.add(comid);
			}
			Long count= 0l;//daService.getLong(countSql, params);
			Double total = 0.0;
			Map cMap = daService.getMap(countSql, params);
			if(cMap!=null){
				count=(Long)cMap.get("count");
				total = StringUtils.formatDouble(cMap.get("total"));
			}
			List list = null;//daService.getPage(sql, null, 1, 20);
			if(count>0){
				list = daService.getAll(sql+" order by id desc ", params, pageNum, pageSize);
			}
			String json = JsonUtil.anlysisMap2Json(list,pageNum,count, fieldsstr,"id",""+total);
			AjaxUtil.ajaxOutput(response, json);
		}
		return null;
	}
}
