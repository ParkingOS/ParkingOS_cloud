package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MemcacheUtils;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.TimeTools;
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

public class CityServerManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private MemcacheUtils memcacheUtils;

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
			String sql = "select * from local_info_tb where " ;
			String countSql = "select count(*) from local_info_tb where " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
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
				sql += " comid in ("+preParams+") ";
				countSql += " comid in ("+preParams+") ";
				params.addAll(parks);
				count = pgOnlyReadService.getCount(countSql,params);
				if(count>0){
					list = pgOnlyReadService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
				}
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("query")){
			String sql = "select * from local_info_tb where " ;
			String countSql = "select count(*) from local_info_tb where " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_berthsecs_tb");
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
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
				sql += " comid in ("+preParams+") ";
				countSql += " comid in ("+preParams+") ";
				params.addAll(parks);

				if(sqlInfo!=null){
					countSql+=" and "+ sqlInfo.getSql();
					sql +=" and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				count = pgOnlyReadService.getCount(countSql,params);
				if(count>0){
					list = pgOnlyReadService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
				}
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("create")){
			int r = creatServer(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			int r = editServer(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}
		return null;
	}


	private int editServer(HttpServletRequest request){
		Integer id = RequestUtil.getInteger(request, "id", -1);
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		if(comid == -1){
			return -1;
		}
		Integer isupdate = RequestUtil.getInteger(request, "is_update", -1);
		String limit_time =RequestUtil.processParams(request, "limit_time");
		String secret =RequestUtil.processParams(request, "secret");
		String remark =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));
		Long time = TimeTools.getLongMilliSecondFrom_HHMMDD(limit_time)/1000;
		int r = daService.update("update local_info_tb set is_update=?,limit_time=?,secret=?,remark=?,comid=? where id = ? ",
				new Object[]{isupdate,time,secret,remark,comid,id});
		return r;
	}

	@SuppressWarnings("rawtypes")
	private int creatServer(HttpServletRequest request){
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		if(comid == -1){
			return -1;
		}
		Integer isupdate = RequestUtil.getInteger(request, "is_update", -1);
		Long limit_time =TimeTools.getBeginTime(System.currentTimeMillis()) + 10*365*24*60*60;//RequestUtil.processParams(request, "limit_time");
		String secret =RequestUtil.processParams(request, "secret");
		String remark =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));
		int r = daService.update("insert into local_info_tb(comid,is_update,limit_time,secret,remark) values(?,?,?,?,?)",
				new Object[]{comid,isupdate,limit_time,secret,remark});
		if(r==1){
			List<Long> tcache = memcacheUtils.doListLongCache("etclocal_park_cache", null, null);
			if(tcache!=null&&!tcache.contains(comid)){
				tcache.add(comid);
			}else {
				tcache = new ArrayList<Long>();
				List all = daService.getAll("select comid from local_info_tb", null);
				for (Object object : all) {
					Map map = (Map)object;
					Long obj = Long.valueOf(map.get("comid")+"");
					tcache.add(obj);
				}
			}
			memcacheUtils.doListLongCache("etclocal_park_cache", tcache, "update");
		}
		return r;
	}
}
