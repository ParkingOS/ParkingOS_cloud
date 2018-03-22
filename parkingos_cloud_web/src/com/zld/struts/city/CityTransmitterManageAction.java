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

public class CityTransmitterManageAction extends Action {

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;

	Logger logger = Logger.getLogger(CityTransmitterManageAction.class);
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
			request.setAttribute("site_state_start", RequestUtil.processParams(request, "site_state_start"));
			request.setAttribute("cityid", cityid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select * from sites_tb where is_delete=? and " ;
			String countSql = "select count(*) from sites_tb where is_delete=? and " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"sites_tb");
			SqlInfo sqlInfo1 = getSuperSqlInfo(request);
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
				sql += " (comid in ("+preParams+") ";
				countSql += " (comid in ("+preParams+") ";
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
				if(groupid > 0){
					countSql+=" or groupid = ?";
					sql +=" or groupid = ?";
					params.add(groupid);
				}
				countSql += ")";
				sql += ")";
				count = pgOnlyReadService.getCount(countSql,params);
				if(count>0){
					list = pgOnlyReadService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
				}
			}
			setList(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);

		}else if(action.equals("edit")){
			int r = editTransmitter(request);
			AjaxUtil.ajaxOutput(response, "" + r);
		}else if(action.equals("create")){
			int r = createTransmitter(request, cityid);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("delete")){
			int r = deleteTransmitter(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("detail")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			request.setAttribute("id", id);
			return mapping.findForward("detail");
		}else if(action.equals("querydetail")){
			Long id = RequestUtil.getLong(request, "siteid", -1L);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"device_fault_tb");
			String sql = "select * from device_fault_tb where site_id=? ";
			String countSql = "select count(id) from device_fault_tb where site_id=? ";
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
	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		Long ntime = System.currentTimeMillis()/1000;
		Integer state = RequestUtil.getInteger(request, "site_state_start", -1);
		SqlInfo sqlInfo1 = null;
		if(state == 1){
			sqlInfo1 = new SqlInfo(" ? - heartbeat < ?",new Object[]{ntime, 30 * 60});
		}else if(state == 0){
			sqlInfo1 = new SqlInfo(" ((heartbeat is null) or (? - heartbeat >= ?)) ",new Object[]{ ntime, 30 * 60});
		}
		return sqlInfo1;
	}

	private void setList(List<Map<String, Object>> list){
		Long ntime = System.currentTimeMillis()/1000;
		if(list != null && !list.isEmpty()){
			List<Object> idList = new ArrayList<Object>();
			String preParams = "";
			for(Map<String, Object> map : list){
				int state = 0;
				if(map.get("heartbeat") != null){
					Long heartbeat = (Long)map.get("heartbeat");
					if(ntime - heartbeat < 30 * 60){
						state = 1;
					}
				}
				map.put("site_state", state);
				map.put("fcount", 0);
				idList.add(map.get("id"));
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			List<Map<String, Object>> list2 = pgOnlyReadService.getAllMap(
					"select count(id) fcount,site_id from device_fault_tb " +
							"where site_id in ("+preParams+") group by site_id ", idList);
			if(list2 != null && !list2.isEmpty()){
				for(Map<String, Object> map : list){
					Long id = (Long)map.get("id");
					for(Map<String, Object> map2 : list2){
						Long siteId = (Long)map2.get("site_id");
						if(id.intValue() == siteId.intValue()){
							map.put("fcount", map2.get("fcount"));
							break;
						}
					}
				}
			}
		}
	}

	private int createTransmitter(HttpServletRequest request, Long cityid){
		String uuid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "uuid"));
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		Double longitude = RequestUtil.getDouble(request, "longitude", 0d);
		Double latitude = RequestUtil.getDouble(request, "latitude", 0d);
		Double voltage = RequestUtil.getDouble(request, "voltage", 0d);
		Integer state = RequestUtil.getInteger(request, "state", 1);//0：故障 1：正常
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		if(uuid.equals("")) uuid = null;
		if(uuid != null && !uuid.equals("")){
			Long count = pgOnlyReadService.getLong("select count(id) from sites_tb where is_delete=? and uuid=? and cityid=? ",
					new Object[]{0, uuid,cityid});
			if(count > 0){
				return -2;
			}
		}
		int r = daService.update("insert into sites_tb(uuid,voltage,longitude,latitude,create_time,address,cityid,name,state,comid) values(?,?,?,?,?,?,?,?,?,?)",
				new Object[]{uuid, voltage, longitude, latitude, System.currentTimeMillis()/1000,address, cityid, name, state, comid});
		return r;
	}

	private int editTransmitter(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		String uuid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "uuid"));
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		Double longitude = RequestUtil.getDouble(request, "longitude", 0d);
		Double latitude = RequestUtil.getDouble(request, "latitude", 0d);
		Double voltage = RequestUtil.getDouble(request, "voltage", 0d);
		Integer state = RequestUtil.getInteger(request, "state", 1);//0：故障 1：正常
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		if(uuid.equals("")) uuid = null;
		/*if(uuid != null && !uuid.equals("")){
			Long count = pgOnlyReadService.getLong("select count(id) from sites_tb where is_delete=? and uuid=? and id<>?  ", 
					new Object[]{0, uuid, id});
			if(count > 0){
				return -2;
			}
		}*/
		int r = daService.update("update sites_tb set uuid=?,address=?,name=?,longitude=?,latitude=?,voltage=?,state=?,comid=?,update_time=? where id=? ",
				new Object[]{uuid, address, name, longitude, latitude,voltage, state, comid, System.currentTimeMillis()/1000, id});
		return r;
	}

	private int deleteTransmitter(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		int r = daService.update("update sites_tb set is_delete=? where id=? ", new Object[]{1, id});
		return r;
	}

}
	
