package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CitySensorManageAction extends Action {

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;

	Logger logger = Logger.getLogger(CitySensorManageAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//鐧诲綍鐨勭敤鎴穒d
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
			commonMethods.setIndexAuthId(request);
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Long today = TimeTools.getToDayBeginTime();
			request.setAttribute("btime", df2.format(today * 1000));
			request.setAttribute("etime",  df2.format(today * 1000 + 24 * 60 * 60 * 1000 -1));
			request.setAttribute("cityid", cityid);
			request.setAttribute("site_state_start", RequestUtil.processParams(request, "site_state_start"));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime);
			Long e = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"dici_tb","",new String[]{"cid", "fcount"});
			SqlInfo sqlInfo1 = getSuperSqlInfo1(request);
			SqlInfo sqlInfo2 = getSuperSqlInfo2(request);

			String sql = "select * from dici_tb where is_delete=? " ;
			String countSql = "select count(id) from dici_tb where is_delete=? ";
			String faultSql = "select count(id) from dici_tb where is_delete=? ";
			String filterSql = "select id from dici_tb where ";//约束查询历史掉线次数的数据量
			ArrayList<Object> params = new ArrayList<Object>();
			params.add(0);
			List<Map<String, Object>> list = null;
			Long count = 0L;
			Long falutCount = 0L;
			double rate = 0;
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
				countSql += " and comid in ("+preParams+")";
				filterSql += " comid in ("+preParams+")";
				faultSql += " and comid in ("+preParams+")";

				params.addAll(parks);

				if(sqlInfo!=null){
					countSql += " and "+ sqlInfo.getSql();
					sql += " and "+sqlInfo.getSql();
					faultSql += " and "+ sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				if(sqlInfo1 != null){
					countSql += " and "+ sqlInfo1.getSql();
					sql += " and "+sqlInfo1.getSql();
					faultSql += " and "+ sqlInfo1.getSql();
					params.addAll(sqlInfo1.getParams());
				}
				if(sqlInfo2 != null){
					countSql += " and "+ sqlInfo2.getSql();
					sql +=" and "+sqlInfo2.getSql();
					faultSql += " and "+ sqlInfo2.getSql();
					params.addAll(sqlInfo2.getParams());
				}
				SqlInfo sqlInfo3 = getSuperSqlInfo3(request, filterSql, parks, b, e);
				if(sqlInfo3 != null){
					countSql += " and "+ sqlInfo3.getSql();
					sql +=" and "+sqlInfo3.getSql();
					faultSql += " and "+ sqlInfo3.getSql();
					params.addAll(sqlInfo3.getParams());
				}
				SqlInfo sqlInfo4 = getFaultSqlInfo(filterSql, parks, b, e);
				ArrayList<Object> params2 = (ArrayList<Object>) params.clone();
				if(sqlInfo4 != null){
					faultSql += " and "+ sqlInfo4.getSql();
					params2.addAll(sqlInfo4.getParams());
				}
				count = pgOnlyReadService.getCount(countSql,params);
				falutCount = pgOnlyReadService.getCount(faultSql, params2);
				if(count>0){
					list = pgOnlyReadService.getAll(sql +" order by operate_time desc ",params, pageNum, pageSize);
					setList(list, b, e);
				}

				if(count > 0){
					rate = StringUtils.formatDouble(((double)falutCount/count) * 100);
				}

			}
			String json = JsonUtil.anlysisMap2Json(list, pageNum, count, fieldsstr, "id", "车检器历史掉线率：" + rate + "%");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("detail")){
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Long id = RequestUtil.getLong(request, "id", -1L);
			request.setAttribute("btime", btime);
			request.setAttribute("etime", etime);
			request.setAttribute("id", id);
			return mapping.findForward("detail");
		}else if(action.equals("querydetail")){
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
			Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
			Long id = RequestUtil.getLong(request, "sensorid", -1L);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"device_fault_tb");
			String sql = "select * from device_fault_tb where sensor_id=? and " +
					" create_time between ? and ? ";
			String countSql = "select count(id) from device_fault_tb where sensor_id=? " +
					" and create_time between ? and ? ";
			List<Map<String, Object>> list = null;
			List<Object> params = new ArrayList<Object>();
			params.add(id);
			params.add(b);
			params.add(e);
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			sql += " order by create_time desc";
			Long count = pgOnlyReadService.getCount(countSql, params);
			if(count > 0){
				list = pgOnlyReadService.getAll(sql, params, pageNum, pageSize);
				setDetailList(list);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}
		return null;
	}

	private SqlInfo getSuperSqlInfo1(HttpServletRequest request){
		String cid = RequestUtil.processParams(request, "cid");
		SqlInfo sqlInfo1 = null;
		if(!cid.equals("")){
			sqlInfo1 = new SqlInfo(" id in (select dici_id from com_park_tb where is_delete=? " +
					" and cid like ? )",new Object[]{0, "%" + cid + "%"});
		}
		return sqlInfo1;
	}

	private SqlInfo getSuperSqlInfo2(HttpServletRequest request){
		Integer site_state = RequestUtil.getInteger(request, "site_state_start", -1);
		SqlInfo sqlInfo1 = null;
		Long ntime = System.currentTimeMillis()/1000;
		if(site_state == 0){//故障设备
			sqlInfo1 = new SqlInfo(" ?-beart_time>=? ", new Object[]{ntime, 30 * 60});
		}else if(site_state == 1){//正常设备
			sqlInfo1 = new SqlInfo(" ?-beart_time<? ", new Object[]{ntime, 30 * 60});
		}
		return sqlInfo1;
	}

	private SqlInfo getSuperSqlInfo3(HttpServletRequest request, String filterSql,
									 List<Object> params, Long bTime, Long eTime){
		Integer fcount = RequestUtil.getInteger(request, "fcount", -1);
		if(fcount == 0){
			return getFaultSqlInfo(filterSql, params, bTime, eTime);
		}
		return null;
	}

	private SqlInfo getFaultSqlInfo(String filterSql, List<Object> params, Long bTime, Long eTime){
		List<Object> params2 = new ArrayList<Object>();
		params2.addAll(params);
		params2.add(bTime);
		params2.add(eTime);
		params2.add(0);
		//此处sensorSql为了防止查询数据太多而做的约束
		SqlInfo sqlInfo1 = new SqlInfo(" id in (select sensor_id from device_fault_tb where sensor_id " +
				" in ("+filterSql+") and create_time between ? and ? group by sensor_id " +
				" having count(id)>?) ", params2);
		return sqlInfo1;
	}

	private void setDetailList(List<Map<String, Object>> list){
		try {
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					Long create_time = System.currentTimeMillis()/1000;
					Long end_time = System.currentTimeMillis()/1000;
					if(map.get("create_time") != null){
						create_time = (Long)map.get("create_time");
					}
					if(map.get("end_time") != null){
						end_time = (Long)map.get("end_time");
					}
					String duration = StringUtils.getTimeString(create_time, end_time);
					map.put("duration", duration);
				}
			}
		} catch (Exception e) {
			logger.equals(e);
		}
	}

	private void setList(List<Map<String, Object>> list, Long bTime, Long eTime){
		Long ntime = System.currentTimeMillis()/1000;
		if(list != null && !list.isEmpty()){
			List<Object> params1 = new ArrayList<Object>();
			List<Object> params2 = new ArrayList<Object>();
			String preParams = "";
			for(Map<String, Object> map : list){
				int state = 0;
				if(map.get("beart_time") != null){
					Long heartbeat = (Long)map.get("beart_time");
					if(ntime - heartbeat < 30 * 60){
						state = 1;
					}
				}
				map.put("site_state", state);
				map.put("fcount", 0);
				params1.add(map.get("id"));
				params2.add(map.get("id"));
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			params1.add(0);
			List<Map<String, Object>> list1 = pgOnlyReadService.getAllMap("select dici_id,cid from com_park_tb " +
					" where dici_id in ("+preParams+") and is_delete=? ", params1);
			if(list1 != null && !list1.isEmpty()){
				for(Map<String, Object> map : list){
					Long dici_id = (Long)map.get("id");
					for(Map<String, Object> map2 : list1){
						Integer id = (Integer)map2.get("dici_id");
						if(dici_id.intValue() == id.intValue()){
							map.put("cid", map2.get("cid"));
							break;
						}
					}
				}
			}
			params2.add(bTime);
			params2.add(eTime);
			List<Map<String, Object>> list2 = pgOnlyReadService.getAllMap(
					"select count(id) fcount,sensor_id from device_fault_tb " +
							"where sensor_id in ("+preParams+") and create_time " +
							" between ? and ? group by sensor_id ", params2);
			if(list2 != null && !list2.isEmpty()){
				for(Map<String, Object> map : list){
					Long id = (Long)map.get("id");
					for(Map<String, Object> map2 : list2){
						Long sensorId = (Long)map2.get("sensor_id");
						if(id.intValue() == sensorId.intValue()){
							map.put("fcount", map2.get("fcount"));
							break;
						}
					}
				}
			}
		}
	}
}
	
