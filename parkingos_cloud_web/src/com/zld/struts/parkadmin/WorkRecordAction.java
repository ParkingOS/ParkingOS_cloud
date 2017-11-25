package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * 上下班记录
 * @author Administrator
 *
 */
public class WorkRecordAction extends Action{

	@Autowired
	private DataBaseService daService;

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		Integer authId = RequestUtil.getInteger(request, "authid",-1);
		request.setAttribute("authid", authId);
		if(comid==-1){
			comid = (Long)request.getSession().getAttribute("comid");
		}
		if(comid==null||comid==-1){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			request.setAttribute("comid", request.getParameter("comid"));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select * from parkuser_work_record_tb where park_id=?  ";
			String countSql = "select count(*) from parkuser_work_record_tb where park_id=?  ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo base = new SqlInfo("1=1", new Object[]{comid});
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"parkuser_work_record_tb");
			List<Object> params = null;
			if(sqlInfo!=null){
				sqlInfo = SqlInfo.joinSqlInfo(base,sqlInfo, 2);
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params = sqlInfo.getParams();
			}else {
				params= base.getParams();
			}
			//System.out.println(sqlInfo);
			Long count =daService.getCount(countSql, params);
			List list = daService.getAll(sql+" order by id desc",params,pageNum,pageSize);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}
		return null;
	}

}
