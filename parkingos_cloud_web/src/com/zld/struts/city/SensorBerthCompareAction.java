package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
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

public class SensorBerthCompareAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;

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
		}else if(action.equals("query")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_park_tb","p",new String[]{"state","did"});
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String orderfield = RequestUtil.processParams(request, "orderfield");
			String orderby = RequestUtil.processParams(request, "orderby");
			SqlInfo sqlInfo1 = getSuperSqlInfo(request);
			SqlInfo sqlInfo2 = getSuperSqlInfo1(request);
			SqlInfo sqlInfo3 = getSuperSqlInfo2(request);
			String sql = "select d.battery,d.beart_time,d.id,p.cid,d.did,p.comid, " +
					"p.berthsec_id,d.beart_time,d.state,p.id as berthid from dici_tb d, com_park_tb p " +
					" where d.id=p.dici_id and p.is_delete=? and d.is_delete=? ";
			String countsql = "select count(d.id) from dici_tb d,com_park_tb p where " +
					" d.id=p.dici_id and p.is_delete=? and d.is_delete=? ";
			List<Map<String, Object>> list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(0);
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
				sql += " and p.comid in ("+preParams+") ";
				countsql += " and p.comid in ("+preParams+") ";
				params.addAll(parks);

				if(sqlInfo!=null){
					sql +=" and "+sqlInfo.getSql();
					countsql += " and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				if(sqlInfo1 != null){
					countsql+=" and "+ sqlInfo1.getSql();
					sql +=" and "+sqlInfo1.getSql();
					params.addAll(sqlInfo1.getParams());
				}
				if(sqlInfo2 != null){
					countsql+=" and "+ sqlInfo2.getSql();
					sql +=" and "+sqlInfo2.getSql();
					params.addAll(sqlInfo2.getParams());
				}
				if(sqlInfo3 != null){
					countsql+=" and "+ sqlInfo3.getSql();
					sql +=" and "+sqlInfo3.getSql();
					params.addAll(sqlInfo3.getParams());
				}
				if(orderfield.equals("")){
					orderfield = " p.cid ";
				}
				if(orderby.equals("")){
					orderby = " asc ";
				}
				sql += " order by " + orderfield + " " + orderby + " nulls last ";
				count = pgOnlyReadService.getCount(countsql,params);
				if(count > 0){
					list = pgOnlyReadService.getAll(sql, params, pageNum, pageSize);
				}
			}
			setOrderInfo(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("sensorgraph")){
			List<Map<String, Object>> list = getpark(cityid, groupid);
			request.setAttribute("parkinfo", StringUtils.createJson(list));
			return mapping.findForward("sensor");
		}else if(action.equals("querysensor")){
			Long berthseg_id = RequestUtil.getLong(request, "berthseg_id", -1L);
			SqlInfo sqlInfo1 = getSuperSqlInfo1(request);
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			List<Object> params = new ArrayList<Object>();
			if(berthseg_id > 0){
				String sql = "select d.battery,d.beart_time,d.id,p.cid,d.did,p.comid,p.berthsec_id,d.beart_time,d.state,p.id as berthid from dici_tb d,com_park_tb p where " +
						" d.id=p.dici_id and p.is_delete=? and p.berthsec_id=? d.is_delete=? ";
				params.add(0);
				params.add(0);
				params.add(berthseg_id);
				if(sqlInfo1 != null){
					sql +=" and "+sqlInfo1.getSql();
					params.addAll(sqlInfo1.getParams());
				}
				list = pgOnlyReadService.getAllMap(sql, params);
				setOrderInfo(list);
			}
			String json = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response, json);
		}
		return null;
	}

	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		Integer state = RequestUtil.getInteger(request, "state_start", -1);
		SqlInfo sqlInfo1 = null;
		if(state > -1){
			sqlInfo1 = new SqlInfo(" d.state =?  ",new Object[]{state});
		}
		return sqlInfo1;
	}
	private SqlInfo getSuperSqlInfo1(HttpServletRequest request){
		Long ntime = System.currentTimeMillis()/1000;
		Integer state = RequestUtil.getInteger(request, "sensor_state_start", -1);
		SqlInfo sqlInfo1 = null;
		if(state == 0){
			sqlInfo1 = new SqlInfo(" ? - d.beart_time<?  ",new Object[]{ntime, 30 *60});
		}else if(state == 1){
			sqlInfo1 = new SqlInfo(" ? - d.beart_time>=?  ",new Object[]{ntime, 30 *60});
		}
		return sqlInfo1;
	}

	private SqlInfo getSuperSqlInfo2(HttpServletRequest request){
		String did = RequestUtil.processParams(request, "did");
		SqlInfo sqlInfo1 = null;
		if(!"".equals(did)){
			sqlInfo1 = new SqlInfo(" d.did like ? ",new Object[]{"%" + did + "%"});
		}
		return sqlInfo1;
	}

	private void setOrderInfo(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			Long ntime = System.currentTimeMillis()/1000;
			List<Object> idList = new ArrayList<Object>();
			String preParams = "";
			for(Map<String, Object> map : list){
				idList.add(map.get("berthid"));
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";

				int state = 0;
				if(map.get("beart_time") != null){
					Long beart_time = (Long)map.get("beart_time");
					map.put("heartbeat_time", TimeTools.getTime_yyyyMMdd_HHmmss(beart_time*1000));
					if(ntime - beart_time > 30 *60){//心跳超过三十分钟没有就标为离线
						state = 1;
					}
				}
				map.put("sensor_state", state);
			}
			List<Object> params1 = new ArrayList<Object>();
			params1.addAll(idList);
			params1.add(0);
			List<Map<String, Object>> berthList = pgOnlyReadService.getAllMap("select o.in_time,p.id from berth_order_tb o,com_park_tb p" +
					" where o.dici_id=p.id and p.id in ("+preParams+") and o.state=? order by o.in_time desc ", params1);

			if(berthList != null && !berthList.isEmpty()){
				for(Map<String, Object> map : list){
					Long berthid = (Long)map.get("berthid");
					Integer state = (Integer)map.get("state");
					for(Map<String, Object> map2 : berthList){
						Long id = (Long)map2.get("id");
						if(id.intValue() == berthid.intValue()){
							if(state == 1){
								Long in_time = (Long)map2.get("in_time");
								map.put("sensor_in_time", TimeTools.getTime_yyyyMMdd_HHmmss(in_time*1000));
								break;
							}
						}
					}
				}
			}

			List<Map<String, Object>> orderList = pgOnlyReadService.getAllMap("select o.create_time,o.car_number,p.id from order_tb o,com_park_tb p" +
					" where o.id=p.order_id and p.id in ("+preParams+") and o.state=? ", params1);
			if(orderList != null && !orderList.isEmpty()){
				for(Map<String, Object> map : list){
					Long berthid = (Long)map.get("berthid");
					for(Map<String, Object> map2 : orderList){
						Long id = (Long)map2.get("id");
						if(id.intValue() == berthid.intValue()){
							Long in_time = (Long)map2.get("create_time");
							map.put("order_in_time", TimeTools.getTime_yyyyMMdd_HHmmss(in_time*1000));
							map.put("car_number", map2.get("car_number"));
							break;
						}
					}
				}
			}
		}
	}

	private List<Map<String, Object>> getpark(Long cityid, Long groupid){
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
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

			List<Map<String, Object>> parkList = pgOnlyReadService.getAllMap("select id,company_name from com_info_tb" +
					" where id in ("+preParams+") ", parks);
			list = parkList;
		}
		return list;
	}
}
