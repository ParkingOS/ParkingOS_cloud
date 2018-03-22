package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorksiteManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	@Autowired
	private CommonMethods commonMethods;

	private Logger logger = Logger.getLogger(WorksiteManageAction.class);

	/*
	 * 工作站设置
	 */
	@Override
	public ActionForward execute(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response) throws Exception{
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		Long operater = (Long)request.getSession().getAttribute("loginuin");
		request.setAttribute("authid", request.getParameter("authid"));
		if(operater == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(comid == 0){
			comid = RequestUtil.getLong(request, "comid", -1L);
		}
		if(action.equals("")){
			request.setAttribute("comid", comid);
			return mapping.findForward("list");
		}else if(action.equals("worksitequery")){
			String sql = "select * from com_worksite_tb where comid=?";
			String countsql = "select count(1) from com_worksite_tb where comid=?";
			Long count = daService.getLong(countsql, new Object[]{comid});
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(comid);
			if(count>0){
				list = daService.getAll(sql+ " order by id desc",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			logger.error("insert into com_worksite_tb....");
			String worksite_name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "worksite_name"));
			String description = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "description"));
			Integer net_type = RequestUtil.getInteger(request, "net_type", 0);//默认是网络状况是0：流量
			if(worksite_name.equals("")) worksite_name = null;
			if(description.equals("")) description = null;
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("comid", comid);
			map.put("worksite_name", worksite_name);
			map.put("description", description);
			map.put("net_type", net_type);
			Long result = commonMethods.createWorksite(request, map);
			if(result > 0){
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("edit")){
			logger.error("update com_worksite_tb....");
			Long id =RequestUtil.getLong(request, "id", -1L);
			if(id == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			String worksite_name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "worksite_name"));
			String description = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "description"));
			Integer net_type = RequestUtil.getInteger(request, "net_type", 0);//默认是网络状况是0：流量
			String sql = "update com_worksite_tb set worksite_name=?,description=?,net_type=? where id=?";
			int r = daService.update(sql, new Object[]{worksite_name,description,net_type,id});
			logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" worksite");
			if(r == 1){
				if(publicMethods.isEtcPark(comid)){
					int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_worksite_tb",id,System.currentTimeMillis()/1000,1});
					logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" worksite ,add sync ret:"+re);
				}
				mongoDbUtils.saveLogs( request,0, 3, "修改了工作站（编号："+id+"）："+worksite_name);
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("delete")){
			logger.error("delete from com_worksite_tb....");
			String id =RequestUtil.processParams(request, "selids");
			String sql = "delete from com_worksite_tb where id=?";
			Map wMap = daService.getMap("select * from com_worksite_tb where id =?", new Object[]{Long.valueOf(id)});
			int r = daService.update(sql, new Object[]{Long.valueOf(id)});
			logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" worksite ");
			if(r == 1){
				if(publicMethods.isEtcPark(comid)){
					int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_worksite_tb",Long.valueOf(id),System.currentTimeMillis()/1000,2});
					logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" worksite ,add sync ret:"+re);
				}
				mongoDbUtils.saveLogs( request,0, 4, "删除了工作站（编号："+id+"）："+wMap);
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "0");
			}
		}
		return null;
	}
}
