package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.pojo.QueryCount;
import com.zld.pojo.QueryList;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class CityBigOrderManageAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;

	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
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
		ExecutorService pool = ExecutorsUtil.getExecutorService();//获取线程池
		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String orderfield = RequestUtil.processParams(request, "orderfield");
			String orderby = RequestUtil.processParams(request, "orderby");
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"order_tb","o",new String[]{"cid","groupid"});
			SqlInfo sqlInfo1 = getSuperSqlInfo(request);
			if(groupid == -1){
				groupid= RequestUtil.getLong(request, "groupid_start", -1L);
			}
			List<Map<String, Object>> list = null;
			Long count = 0L;
			Long ntime = System.currentTimeMillis()/1000;
			ArrayList<Object> params = new ArrayList<Object>();
			String sql = "select o.*,p.cid from order_tb o left join com_park_tb p on o.berthnumber=p.id" +
					" where o.state=? and o.ishd=? and o.create_time between ? and ? ";
			String countSql = "select count(o.id) from order_tb o left join com_park_tb p on o.berthnumber=p.id" +
					" where o.state=? and o.ishd=? and o.create_time between ? and ? ";
			params.add(0);
			params.add(0);
			params.add(ntime - 10*24*60*60);
			params.add(ntime - 2*24*60*60);
			List<Object> parks = null;
			if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}else if(cityid > 0){
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
				sql += " and o.comid in ("+preParams+") ";
				countSql += " and o.comid in ("+preParams+") ";
				params.addAll(parks);

				if(sqlInfo!=null){
					countSql+=" and "+ sqlInfo.getSql();
					sql +=" and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				if(sqlInfo1 != null){
					countSql+=" and "+ sqlInfo1.getSql();
					sql +=" and "+sqlInfo1.getSql();
					params.addAll(sqlInfo1.getParams());
				}
				if(orderfield.equals("")){
					orderfield = "create_time ";
				}
				if(orderby.equals("")){
					orderby = " asc ";
				}
				if(orderfield.equals("cid")){
					orderfield = "p." + orderfield;
				}else{
					orderfield = "o." + orderfield;
				}
				sql += " order by " + orderfield + " " + orderby + " nulls last ";
				QueryCount queryCount = new QueryCount(pgOnlyReadService, countSql, params);
				QueryList queryList = new QueryList(pgOnlyReadService, sql, params, pageNum, pageSize);
				Future<Long> future0 = pool.submit(queryCount);
				Future<List> future1 = pool.submit(queryList);
				count = future0.get();
				list = future1.get();
				getCollector(list);
				setList(list);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}

		return null;
	}

	private void setList(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			Long ntime = System.currentTimeMillis()/1000;
			for(Map<String, Object> map : list){
				Long create_time = (Long)map.get("create_time");
				map.put("duration", StringUtils.getTimeString(create_time, ntime));
			}
		}
	}

	private void getCollector(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> idList = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> order : list){
				if(!idList.contains(order.get("uid"))){
					idList.add(order.get("uid"));
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
			}

			List<Map<String, Object>> rlist = pgOnlyReadService.getAllMap("select id,nickname from user_info_tb where id in ("
					+ preParams + ") ", idList);
			if(rlist != null && !rlist.isEmpty()){
				for(Map<String, Object> map : list){
					Long uid = (Long)map.get("uid");
					for(Map<String, Object> map2 : rlist){
						Long id = (Long)map2.get("id");
						if(uid.intValue() == id.intValue()){
							map.put("collector", map2.get("nickname"));
						}
					}
				}
			}
		}
	}

	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		String cid = RequestUtil.processParams(request, "cid");
		SqlInfo sqlInfo1 = null;
		if(!cid.equals("")){
			sqlInfo1 = new SqlInfo(" p.cid like ? ",new Object[]{"%" + cid + "%"});
		}
		return sqlInfo1;
	}
}
