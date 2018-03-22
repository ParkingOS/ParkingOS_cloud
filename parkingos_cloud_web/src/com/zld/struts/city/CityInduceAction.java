package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
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

public class CityInduceAction extends Action {

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;

	Logger logger = Logger.getLogger(CityInduceAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//鐧诲綍鐨勭敤鎴穒d
		request.setAttribute("authid", request.getParameter("authid"));
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		if(uin == null || cityid== null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			commonMethods.setIndexAuthId(request);
			request.setAttribute("induce_state_start", RequestUtil.processParams(request, "induce_state_start"));
			request.setAttribute("cityid", cityid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select * from induce_tb where cityid=? and is_delete=? " ;
			String countSql = "select count(*) from induce_tb where cityid=? and is_delete=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"induce_tb");
			SqlInfo sqlInfo1 = getSuperSqlInfo(request);
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(cityid);
			params.add(0);
			if(sqlInfo != null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			if(sqlInfo1 != null){
				countSql += " and "+ sqlInfo1.getSql();
				sql +=" and "+sqlInfo1.getSql();
				params.addAll(sqlInfo1.getParams());
			}
			count = daService.getCount(countSql,params);
			if(count>0){
				list = daService.getAll(sql +" order by update_time desc ",params, pageNum, pageSize);
			}
			commonMethods.getState(list);
			setList(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("edit")){
			int r = editInduce(request);
			AjaxUtil.ajaxOutput(response, "" + r);
		}else if(action.equals("detail")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			request.setAttribute("id", id);
			return mapping.findForward("detail");
		}else if(action.equals("querydetail")){
			Long id = RequestUtil.getLong(request, "induceid", -1L);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"device_fault_tb");
			String sql = "select * from device_fault_tb where induce_id=? ";
			String countSql = "select count(id) from device_fault_tb where induce_id=? ";
			List<Map<String, Object>> list = null;
			List<Object> params = new ArrayList<Object>();
			params.add(id);
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

	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		Integer site_state = RequestUtil.getInteger(request, "induce_state_start", -1);
		SqlInfo sqlInfo1 = null;
		Long ntime = System.currentTimeMillis()/1000;
		if(site_state == 1){//故障设备
			sqlInfo1 = new SqlInfo(" ?-heartbeat_time>=? ", new Object[]{ntime, 30 * 60});
		}else if(site_state == 0){//正常设备
			sqlInfo1 = new SqlInfo(" ?-heartbeat_time<? ", new Object[]{ntime, 30 * 60});
		}
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
	private void setList(List<Map<String, Object>> list){
		try {
			if(list != null && !list.isEmpty()){
				List<Object> idList = new ArrayList<Object>();
				String preParams = "";
				for(Map<String, Object> map : list){
					map.put("fcount", 0);
					idList.add(map.get("id"));
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				List<Map<String, Object>> list2 = pgOnlyReadService.getAllMap(
						"select count(id) fcount,induce_id from device_fault_tb " +
								"where induce_id in ("+preParams+") group by induce_id ", idList);
				if(list2 != null && !list2.isEmpty()){
					for(Map<String, Object> map : list){
						Long id = (Long)map.get("id");
						for(Map<String, Object> map2 : list2){
							Long sensorId = (Long)map2.get("induce_id");
							if(id.intValue() == sensorId.intValue()){
								map.put("fcount", map2.get("fcount"));
								break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	private int editInduce(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		String did = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "did"));
		Double longitude = RequestUtil.getDouble(request, "longitude", 0d);
		Double latitude = RequestUtil.getDouble(request, "latitude", 0d);
		if(did.equals("")) did = null;
		if(did != null && !did.equals("")){
			Long count = pgOnlyReadService.getLong("select count(id) from induce_tb where is_delete=? and did=? and id<>? ",
					new Object[]{0, did, id});
			if(count > 0){
				return -2;
			}
		}
		int r = daService.update("update induce_tb set address=?,name=?,longitude=?,latitude=?,did=? where id=? ",
				new Object[]{address,name,longitude,latitude,did,id});
		return r;
	}

}
	
