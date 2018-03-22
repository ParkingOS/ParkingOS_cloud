package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.StringUtils;
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
import java.util.Map;
/**
 * 停车场后台管理员登录后，提现管理
 * @author Administrator
 *
 */
public class WithDrawerManageAction extends Action{

	@Autowired
	private DataBaseService daService;

	private Logger logger = Logger.getLogger(WithDrawerManageAction.class);

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
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
			Map comMap = daService.getMap("select money from com_info_tb where id =?", new Object[]{comid});
			Double money = 0d;
			if(comMap != null){
				money = StringUtils.formatDouble(comMap.get("money"));
			}
			request.setAttribute("money", money);
			request.setAttribute("comid", comid);
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String sql = "select * from withdrawer_tb where comid=?  order by id desc";
			String countSql = "select count(*) from withdrawer_tb  where comid=? ";
			Long count = daService.getLong(countSql,new Object[]{comid});
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;//daService.getPage(sql, null, 1, 20);
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(comid);
			if(count>0){
				list = daService.getAll(sql, params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("query")){
			String sql = "select * from withdrawer_tb where comid=? ";
			String countSql = "select count(*) from withdrawer_tb where  comid=?  " ;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"withdraw");
			List<Object> params = new ArrayList<Object>();
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params = sqlInfo.getParams();
			}
			params.add(0,comid);
			//System.out.println(sqlInfo);
			Long count= daService.getCount(countSql, params);
			List list = null;//daService.getPage(sql, null, 1, 20);
			if(count>0){
				list = daService.getAll(sql+" order by id desc", params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}
		return null;
	}


}