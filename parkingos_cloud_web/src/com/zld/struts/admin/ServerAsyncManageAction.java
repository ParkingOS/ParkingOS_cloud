package com.zld.struts.admin;

import com.zld.AjaxUtil;
import com.zld.service.PgOnlyReadService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerAsyncManageAction extends Action{
	@Autowired
	private PgOnlyReadService readService;

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		if (uin == null) {
			response.sendRedirect("logging.do");
			return null;
		}
		if ("".equals(action)) {
			return mapping.findForward("list");
		}else if (action.equals("query")) {
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"sync_info_pool_tb");
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			List<Object> params = new ArrayList<Object>();
			String sql = "select * from sync_info_pool_tb ";
			String countSql = "select count(*) from sync_info_pool_tb";
			if(sqlInfo!=null){
				countSql+=" where "+ sqlInfo.getSql();
				sql +=" where "+sqlInfo.getSql();
				params = sqlInfo.getParams();
			}
			sql += " order by create_time desc ";
			Long count = readService.getCount(countSql, params);
			if(count > 0){
				list = readService.getAll(sql, params, pageNum, pageSize);
				setList(list);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}
		return null;
	}

	private void setList(List<Map<String, Object>> list){
		try {
			Long curTime = System.currentTimeMillis()/1000;
			if(list != null && !list.isEmpty()){
				List<Object> paramlList = new ArrayList<Object>();
				String preParam = "";
				for(Map<String, Object> map : list){
					map.put("syncstate", 0);
					paramlList.add(map.get("comid"));
					if(preParam.equals("")){
						preParam = "?";
					}else{
						preParam += ",?";
					}
				}
				List<Map<String, Object>> parkList = readService.getAllMap("select id,company_name from com_info_tb " +
						" where id in ("+preParam+")", paramlList);
				if(parkList != null && !parkList.isEmpty()){
					for(Map<String, Object> map : list){
						Long comid = (Long)map.get("comid");
						for(Map<String, Object> map2 : parkList){
							Long id = (Long)map2.get("id");
							if(id.intValue() == comid.intValue()){
								map.put("company_name", map2.get("company_name"));
								break;
							}
						}
					}
				}
				List<Map<String, Object>> localList = readService.getAllMap("select comid,create_time from local_info_tb " +
						" where comid in ("+preParam+")", paramlList);
				if(localList != null && !localList.isEmpty()){
					for(Map<String, Object> map : list){
						Long comid = (Long)map.get("comid");
						for(Map<String, Object> map2 : localList){
							Long id = (Long)map2.get("comid");
							if(id.intValue() == comid.intValue()){
								if(map2.get("create_time") != null){
									Long create_time = (Long)map2.get("create_time");
									if(curTime - create_time < 5 * 60){
										map.put("syncstate", 1);
									}
								}
								break;
							}
						}
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
