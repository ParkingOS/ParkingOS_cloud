package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;


/**
 * 车场云操作日志
 * @author Administrator
 *
 */
public class PrakLogsAction extends Action{
	@Autowired
	private PgOnlyReadService onlyReadService;
	Logger logger = Logger.getLogger(PrakLogsAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		Integer authId = RequestUtil.getInteger(request, "authid",-1);
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", authId);
		if(uin==null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			request.setAttribute("comid", request.getParameter("comid"));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select * from park_log_tb where park_id=? ";
			String countSql = "select count(*) from park_log_tb  where park_id=? ";
			Long count = onlyReadService.getLong(countSql,new Object[]{comid});
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;//daService.getPage(sql, null, 1, 20);
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(comid);
			if(count>0){
				list = onlyReadService.getAll(sql+ " order by id desc",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}
		return null;
	}

}
