package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityWorksiteManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;

	@SuppressWarnings({ "rawtypes", "unused" })
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

		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String sql = "select * from com_worksite_tb where state=? " ;
			String countSql = "select count(*) from com_worksite_tb where state=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			List<Object> parks = null;
			if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " and comid in ("+preParams+") ";
				countSql += " and comid in ("+preParams+") ";
				params.addAll(parks);

				count = daService.getCount(countSql,params);
				if(count>0){
					list = daService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
				}
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("query")){
			String sql = "select * from com_worksite_tb where state=? " ;
			String countSql = "select count(*) from com_worksite_tb where state=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_worksite_tb");
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			List<Object> parks = null;
			if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " and comid in ("+preParams+") ";
				countSql += " and comid in ("+preParams+") ";
				params.addAll(parks);

				if(sqlInfo!=null){
					countSql+=" and "+ sqlInfo.getSql();
					sql +=" and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				count = daService.getCount(countSql,params);
				if(count>0){
					list = daService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
				}
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("create")){
			int r = createWorksite(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			int r = editWorksite(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("delete")){
			int r = deleteWorksite(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}

		return null;
	}

	private int deleteWorksite(HttpServletRequest request){
		Long id =RequestUtil.getLong(request, "selids", -1L);
		Map<String, Object> worksiteMap = pgOnlyReadService.getMap("select comid from com_worksite_tb where id=? ",
				new Object[]{id});
		Long comid = -1L;
		if(worksiteMap.get("comid") != null){
			comid = (Long)worksiteMap.get("comid");
		}
		int r = daService.update("update com_worksite_tb set state=? where id=? ",
				new Object[]{1, id});
		if(r == 1){
			if(publicMethods.isEtcPark(comid)){
				int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)",
						new Object[]{comid,"com_worksite_tb",Long.valueOf(id),System.currentTimeMillis()/1000,2});
			}
			mongoDbUtils.saveLogs( request,0, 4, "删除了工作站,编号："+id);
		}
		return r;
	}

	private int editWorksite(HttpServletRequest request){
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		if(comid == -1){
			return -1;
		}
		Long id =RequestUtil.getLong(request, "id", -1L);
		String worksite_name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "worksite_name"));
		String description = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "description"));
		Integer net_type = RequestUtil.getInteger(request, "net_type", 0);//默认是网络状况是0：流量
		String sql = "update com_worksite_tb set worksite_name=?,description=?,net_type=?,comid=? where id=?";
		int r = daService.update(sql, new Object[]{worksite_name,description,net_type,comid,id});
		if(r == 1){
			if(publicMethods.isEtcPark(comid)){
				int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_worksite_tb",id,System.currentTimeMillis()/1000,1});
			}
			mongoDbUtils.saveLogs( request,0, 3, "修改了工作站（编号："+id+"）："+worksite_name);
		}
		return r;
	}

	private int createWorksite(HttpServletRequest request){
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		if(comid == -1){
			return -1;
		}
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
		Long r = commonMethods.createWorksite(request, map);
		if(r > 0)
			return 1;
		else
			return 0;
	}
}
