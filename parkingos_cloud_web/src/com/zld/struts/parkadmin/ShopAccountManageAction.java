package com.zld.struts.parkadmin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;

public class ShopAccountManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;

	private Logger logger = Logger.getLogger(ShopManageAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		request.setAttribute("authid", request.getParameter("authid"));
		if(comid==null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			request.setAttribute("comid", comid);
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			List<Object> sqlparams = new ArrayList<Object>();
			String sql = "select su.* from (SELECT s.*, u.nickname operate_name FROM shop_account_tb s, user_info_tb u" +
					" WHERE park_id =? AND ( ( s. OPERATOR = u. ID AND s. OPERATOR IS NOT NULL AND s. OPERATOR != - 1 ) OR ( s.strid = u.strid AND s.strid IS NOT NULL AND s.strid != '' ) ) " +
					"union select s.*,'系统平台' from shop_account_tb s where s.strid is null and s.OPERATOR is null and s.park_id = ? ) su ORDER BY su.operate_time desc";
			sqlparams.add(comid);
			sqlparams.add(comid);
			List<Object> countsqlparams = new ArrayList<Object>();
			String countsql = "select count(*) from shop_account_tb where park_id=? ";
			countsqlparams.add(comid);
			Long count = daService.getCount(countsql, countsqlparams);
			if(count > 0){
				list = daService.getAll(sql, sqlparams, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("query")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String shop_name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "shop_name"));
			String operator_name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "operator_name"));
			Integer operate_type = RequestUtil.getInteger(request, "operate_type_start", 0);
			String operate_time = RequestUtil.getString(request, "operate_time");
			String operate_time_start = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "operate_time_start"));
			String operate_time_end = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "operate_time_end"));
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			List<Object> params = new ArrayList<Object>();
			String sql = "select su.* from (SELECT s.*, u.nickname operate_name FROM shop_account_tb s, user_info_tb u" +
					" WHERE park_id =? AND ( ( s. OPERATOR = u. ID AND s. OPERATOR IS NOT NULL AND s. OPERATOR != - 1 ) OR ( s.strid = u.strid AND s.strid IS NOT NULL AND s.strid != '' ) ) " +
					"union select s.*,'系统平台' from shop_account_tb s where s.strid is null and s.OPERATOR is null and s.park_id = ? ) su where 1=1";
			String countsql = "select count(su.*) from (SELECT s.*, u.nickname operate_name FROM shop_account_tb s, user_info_tb u" +
					" WHERE park_id =? AND ( ( s. OPERATOR = u. ID AND s. OPERATOR IS NOT NULL AND s. OPERATOR != - 1 ) OR ( s.strid = u.strid AND s.strid IS NOT NULL AND s.strid != '' ) ) " +
					"union select s.*,'系统平台' from shop_account_tb s where s.strid is null and s.OPERATOR is null and s.park_id = ? ) su where 1=1";
			params.add(comid);
			params.add(comid);
			if(shop_name!=null && !"".equals(shop_name)){
				sql+=" and su.shop_name like ?";
				countsql+=" and su.shop_name like ?";
				params.add("%"+shop_name+"%");
			}
			if(operator_name!=null && !"".equals(operator_name)){
				sql+=" and su.operate_name like ?";
				countsql+=" and su.operate_name like ?";
				params.add("%"+operator_name+"%");
			}

			if(operate_type != 0){
				sql+=" and su.operate_type = ?";
				countsql+=" and su.operate_type = ?";
				params.add(operate_type);
			}
			if("1".equals(operate_time) && !"".equals(operate_time_start)){
				sql+=" and su.operate_time >= ?";
				countsql+=" and su.operate_time >= ?";
				params.add(TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(operate_time_start));
			}
			if("2".equals(operate_time) && !"".equals(operate_time_start)){
				sql+=" and ? >= su.operate_time";
				countsql+=" and ? >= su.operate_time";
				params.add(TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(operate_time_start));
			}
			if("3".equals(operate_time) && !"".equals(operate_time_start)){
				sql+=" and su.operate_time = ?";
				countsql+=" and su.operate_time = ?";
				params.add(TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(operate_time_start));
			}
			if("between".equals(operate_time)){
				sql+=" and su.operate_time between ? and ?";
				countsql+=" and su.operate_time between ? and ?";
				params.add(TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(operate_time_start));
				params.add(TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(operate_time_end));
			}
			sql+=" order by su.operate_time desc";
			Long count = daService.getCount(countsql, params);
			if(count > 0){
				list = daService.getAll(sql, params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}
		return null;
	}
}
