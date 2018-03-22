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
import java.util.Map;

public class CityInduceModuleAction extends Action {
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
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(cityid == null){
			return null;
		}
		if(cityid == null) cityid = -1L;
		if(action.equals("")){
			Long induce_id = RequestUtil.getLong(request, "induce_id", -1L);
			request.setAttribute("induce_id", induce_id);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select * from induce_module_tb where induce_id=? and is_delete=? ";
			String countSql = "select count(*) from induce_module_tb where induce_id=? and is_delete=? ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			Long induce_id = RequestUtil.getLong(request, "induce_id", -1L);
			List<Object> params = new ArrayList<Object>();
			params.add(induce_id);
			params.add(0);
			count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){//按照正序排序
				list = pgOnlyReadService.getAll(sql +" order by create_time ",params, pageNum, pageSize);
			}
			getBindParks(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("create")){
			int r = createModule(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			int r = editModule(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("delete")){
			int r = deleteModule(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("tounbindpark")){
			Long moduleid = RequestUtil.getLong(request, "moduleid", -1L);
			request.setAttribute("moduleid", moduleid);
			return mapping.findForward("unbindpark");
		}else if(action.equals("queryunpark")){
			String sql = "select id,company_name from com_info_tb where id not in (select comid from induce_park_tb where induce_id=? ) and state<>? ";
			String countSql = "select count(id) from com_info_tb where id not in (select comid from induce_park_tb where induce_id=? ) and state<>? ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			Long moduleid = RequestUtil.getLong(request, "moduleid", -1L);
			Map<String, Object> map = pgOnlyReadService.getMap("select induce_id from induce_module_tb where id=? ",
					new Object[]{moduleid});
			if(map != null){
				Long induce_id = (Long)map.get("induce_id");
				List<Object> params = new ArrayList<Object>();
				params.add(induce_id);
				params.add(1);
				List<Object> parks = null;
				if(cityid > 0){
					parks = commonMethods.getparks(cityid);
				}
				if(parks != null && !parks.isEmpty()){
					String preParams  ="";
					for(Object parkid : parks){
						if(preParams.equals(""))
							preParams ="?";
						else
							preParams += ",?";
					}
					sql += " and id in ("+preParams+") ";
					countSql += " and id in ("+preParams+") ";
					params.addAll(parks);

					count = pgOnlyReadService.getCount(countSql,params);
					if(count>0){
						list = pgOnlyReadService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
					}
				}

			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("bindpark")){
			int r = bindpark(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("tobindpark")){
			Long moduleid = RequestUtil.getLong(request, "moduleid", -1L);
			request.setAttribute("moduleid", moduleid);
			return mapping.findForward("bindpark");
		}else if(action.equals("querypark")){
			String sql = "select p.id,p.comid,c.company_name,p.sort from com_info_tb c,induce_park_tb p where c.id=p.comid and p.module_id=? and p.induce_id=? and c.state<>? ";
			String countSql = "select count(p.id) from com_info_tb c,induce_park_tb p where c.id=p.comid and p.module_id=? and p.induce_id=? and c.state<>? ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			Long moduleid = RequestUtil.getLong(request, "moduleid", -1L);
			Map<String, Object> map = pgOnlyReadService.getMap("select induce_id from induce_module_tb where id=? ",
					new Object[]{moduleid});
			if(map != null){
				Long induce_id = (Long)map.get("induce_id");
				List<Object> params = new ArrayList<Object>();
				params.add(moduleid);
				params.add(induce_id);
				params.add(1);
				List<Object> parks = null;
				if(cityid > 0){
					parks = commonMethods.getparks(cityid);
				}
				if(parks != null && !parks.isEmpty()){
					String preParams  ="";
					for(Object parkid : parks){
						if(preParams.equals(""))
							preParams ="?";
						else
							preParams += ",?";
					}
					sql += " and c.id in ("+preParams+") ";
					countSql += " and c.id in ("+preParams+") ";
					params.addAll(parks);

					count = pgOnlyReadService.getCount(countSql,params);
					if(count>0){
						list = pgOnlyReadService.getAll(sql +" order by p.sort nulls last ",params, pageNum, pageSize);
					}
				}

			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("unbindpark")){
			int r = unbindpark(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("editsort")){
			int r = setSort(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("editmodulesort")){
			int r = setModuleSort(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}
		return null;
	}

	private int setModuleSort(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		Integer sort = RequestUtil.getInteger(request, "sort", 0);
		int r = daService.update("update induce_module_tb set sort=? where id=? ",
				new Object[]{sort, id});
		return r;
	}

	private int setSort(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		Integer sort = RequestUtil.getInteger(request, "sort", 0);
		int r = daService.update("update induce_park_tb set sort=? where id=? ",
				new Object[]{sort, id});
		return r;
	}

	private void getBindParks(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> params = new ArrayList<Object>();
			String preParams = "";
			for(Map<String, Object> map : list){
				map.put("pcount", 0);
				params.add(map.get("id"));
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}

			List<Map<String, Object>> rList = pgOnlyReadService.getAllMap("select count(id) pcount,module_id from induce_park_tb where " +
					" module_id in ("+preParams+") group by module_id ", params);
			if(rList != null && !rList.isEmpty()){
				for(Map<String, Object> map : list){
					Long id = (Long)map.get("id");
					for(Map<String, Object> map2 : rList){
						Long module_id = (Long)map2.get("module_id");
						if(id.intValue() == module_id.intValue()){
							map.put("pcount", map2.get("pcount"));
							break;
						}
					}
				}
			}
		}
	}

	private int unbindpark(HttpServletRequest request){
		Long moduleid = RequestUtil.getLong(request, "moduleid", -1L);
		String ids = RequestUtil.processParams(request, "id");
		if(moduleid > 0 && !ids.equals("")){
			List<Object> params = new ArrayList<Object>();
			String[] idStr = ids.split(",");
			String preParams = "";
			for(int i = 0;i<idStr.length; i++){
				Long id = Long.valueOf(idStr[i]);
				params.add(id);
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			int r = daService.update("delete from induce_park_tb where id in ("+preParams+") ", params);
			if(r > 0){
				return 1;
			}
		}
		return 0;
	}

	private int bindpark(HttpServletRequest request){
		Long moduleid = RequestUtil.getLong(request, "moduleid", -1L);
		String ids = RequestUtil.processParams(request, "id");
		Map<String, Object> map = pgOnlyReadService.getMap("select induce_id from induce_module_tb where id=? ",
				new Object[]{moduleid});
		if(map != null && !ids.equals("")){
			Long induce_id = (Long)map.get("induce_id");
			if(induce_id > 0){
				List<Object[]> anlyList = new ArrayList<Object[]>();
				List<Object> params = new ArrayList<Object>();
				String[] idStr = ids.split(",");
				String preParams = "";
				for(int i = 0;i<idStr.length; i++){
					Long comid = Long.valueOf(idStr[i]);
					params.add(comid);
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";

					Object[] values2 = new Object[]{induce_id, comid, moduleid};
					anlyList.add(values2);
				}
				params.add(induce_id);
				Long count = pgOnlyReadService.getCount("select count(id) from induce_park_tb where comid in ("+preParams+") and induce_id=? ",params);
				if(count > 0){
					return -2;
				}
				int r = daService.bathInsert("insert into induce_park_tb(induce_id,comid,module_id) values(?,?,?) ",
						anlyList, new int[]{4,4,4});
				if(r > 0){
					return 1;
				}
			}
		}

		return -1;
	}

	private int deleteModule(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		Long count = pgOnlyReadService.getLong("select count(id) from induce_park_tb where module_id=? ",
				new Object[]{id});
		if(count > 0){
			return -2;
		}
		int r = daService.update("update induce_module_tb set is_delete=? where id=? ",
				new Object[]{1, id});
		return r;
	}

	private int editModule(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
		int r = daService.update("update induce_module_tb set name=? where id=? ",
				new Object[]{name, id});
		return r;
	}

	private int createModule(HttpServletRequest request){
		Long ntime = System.currentTimeMillis()/1000;
		Long induce_id = RequestUtil.getLong(request, "induce_id", -1L);
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
		int r = daService.update("insert into induce_module_tb(induce_id,name,create_time) values(?,?,?) ",
				new Object[]{induce_id, name, ntime});
		return r;
	}
}
