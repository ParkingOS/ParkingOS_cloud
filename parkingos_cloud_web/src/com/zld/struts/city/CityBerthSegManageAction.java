package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MemcacheUtils;
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

public class CityBerthSegManageAction extends Action {
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
			String sql = "select * from com_berthsecs_tb where is_active=? " ;
			String countSql = "select count(*) from com_berthsecs_tb where is_active=? " ;
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
					list = daService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
				}
			}
			setList(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("query")){
			String sql = "select * from com_berthsecs_tb where is_active=? " ;
			String countSql = "select count(*) from com_berthsecs_tb where is_active=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_berthsecs_tb");
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
					list = daService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
				}
			}
			setList(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("create")){
			int r = createBerthSeg(request);
			AjaxUtil.ajaxOutput(response, "" + r);
		}else if(action.equals("edit")){
			int r = editBerthSeg(request);
			AjaxUtil.ajaxOutput(response, "" + r);
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Long count = pgOnlyReadService.getLong("select count(id) from com_park_tb where berthsec_id=? and is_delete=? ",
					new Object[]{id, 0});
			if(count > 0){
				AjaxUtil.ajaxOutput(response, "-2");
				return null;
			}
			int r = daService.update("update com_berthsecs_tb set is_active=? where id=? ",
					new Object[]{1, id});
			AjaxUtil.ajaxOutput(response, "" + r);
		}else if(action.equals("getcityparks")){
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			String sql = "select id,company_name from com_info_tb ";
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
				sql += " where id in ("+preParams+") ";
				params.addAll(parks);
				list = pgOnlyReadService.getAllMap(sql, params);
			}
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(list != null && !list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("company_name")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getberthseg")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Map<String, Object> berthsegMap = pgOnlyReadService.getMap("select berthsec_name from com_berthsecs_tb where id=? ",
					new Object[]{id});
			AjaxUtil.ajaxOutput(response, berthsegMap.get("berthsec_name") +"" );
		}else if(action.equals("tobindberth")){
			Long berthsegid = RequestUtil.getLong(request, "berthsegid", -1L);
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			request.setAttribute("comid", comid);
			request.setAttribute("berthsegid", berthsegid);
			return mapping.findForward("bindberth");
		}else if(action.equals("queryberth")){
			Long berthsegid = RequestUtil.getLong(request, "berthsegid", -1L);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_park_tb","c",new String[]{"did"});
			String sql = "select c.*,d.did from com_park_tb c left join dici_tb d on c.dici_id=d.id where c.berthsec_id=? and c.is_delete=? ";
			String countSql = "select count(c.id) from com_park_tb c left join dici_tb d on c.dici_id=d.id where c.berthsec_id=? and c.is_delete=? ";
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(berthsegid);
			params.add(0);
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			if(berthsegid > 0){
				count = daService.getCount(countSql,params);
				if(count>0){
					list = daService.getAll(sql +" order by c.id desc ",params, pageNum, pageSize);
				}
			}

			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("unbindberth")){
			String ids = RequestUtil.processParams(request, "id");
			if(!ids.equals("")){
				List<Object> params = new ArrayList<Object>();
				params.add(-1);
				String[] idstr = ids.split(",");
				String preParams = "";
				for(int i = 0; i < idstr.length; i++){
					params.add(Long.valueOf(idstr[i]));
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				int r = daService.update("update com_park_tb set berthsec_id=? where id in ("+preParams+") ", params);
				if(r > 0){
					AjaxUtil.ajaxOutput(response, "1");
					return null;
				}
			}
			AjaxUtil.ajaxOutput(response, "0");
		}else if(action.equals("tounbindberth")){
			Long berthsegid = RequestUtil.getLong(request, "berthsegid", -1L);
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			request.setAttribute("comid", comid);
			request.setAttribute("berthsegid", berthsegid);
			return mapping.findForward("unbindberth");
		}else if(action.equals("queryunberth")){
			Long berthsegid = RequestUtil.getLong(request, "berthsegid", -1L);
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_park_tb","c",new String[]{"did"});
			String sql = "select c.*,d.did from com_park_tb c left join dici_tb d on c.dici_id=d.id where c.berthsec_id<>? and c.is_delete=? and c.comid=? ";
			String countSql = "select count(c.id) from com_park_tb c left join dici_tb d on c.dici_id=d.id where c.berthsec_id<>? and c.is_delete=? and c.comid=? ";
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(berthsegid);
			params.add(0);
			params.add(comid);
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			if(berthsegid > 0){
				count = daService.getCount(countSql,params);
				if(count>0){
					list = daService.getAll(sql +" order by c.id desc ",params, pageNum, pageSize);
				}
			}

			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("bindberth")){
			Long berthsegid = RequestUtil.getLong(request, "berthsegid", -1L);
			String ids = RequestUtil.processParams(request, "id");
			if(berthsegid > 0 && !ids.isEmpty()){
				List<Object> params = new ArrayList<Object>();
				params.add(berthsegid);
				String[] idstr = ids.split(",");
				String preParams = "";
				for(int i = 0; i < idstr.length; i++){
					params.add(Long.valueOf(idstr[i]));
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				int r = daService.update("update com_park_tb set berthsec_id=? where id in ("+preParams+") ", params);
				if(r > 0){
					AjaxUtil.ajaxOutput(response, "1");
					return null;
				}
			}
			AjaxUtil.ajaxOutput(response, "0");
		}else if(action.equals("queryworker")){
			String result = "-1_无收费员上班";
			long id = RequestUtil.getLong(request,"id",-1L);
			Map map = daService.getMap("select w.uid,u.nickname from parkuser_work_record_tb w,user_info_tb u where berthsec_id =? and " +
					"end_time is null and w.uid = u.id and w.state=?",new Object[]{id,0});
			if(map!=null&&map.get("uid")!=null){
				result = map.get("uid")+"_"+map.get("nickname")+"";
			}
			AjaxUtil.ajaxOutput(response,result);
		}else if(action.equals("workout")){
			String result = "0";
			long id = RequestUtil.getLong(request,"id",-1L);
			long uid = RequestUtil.getLong(request,"uid",-1L);
			if(id <= 0 || uid <= 0){
				AjaxUtil.ajaxOutput(response,"-1");
				return null;
			}
			Long ntime = System.currentTimeMillis()/1000;
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//更新工作组中的指定的泊位段已签退
			Map<String, Object> workBerthSegSqlMap = new HashMap<String, Object>();
			workBerthSegSqlMap.put("sql", "update work_berthsec_tb set state=? where berthsec_id=? and is_delete =? ");
			workBerthSegSqlMap.put("values", new Object[]{0, id, 0});
			bathSql.add(workBerthSegSqlMap);
			boolean off = commonMethods.checkWorkTime(uid, ntime);
			int logoff_state = 0;
			if(!off){
				logoff_state = 1;
			}
			//签退操作
			Map<String, Object> workRecordSqlMap = new HashMap<String, Object>();
			workRecordSqlMap.put("sql", "update parkuser_work_record_tb set end_time=?,state=?,logoff_state=? where berthsec_id=? and state=?");
			workRecordSqlMap.put("values", new Object[]{ntime, 1, logoff_state, id, 0});
			bathSql.add(workRecordSqlMap);
			//标为离线
			Map<String, Object> onlineSqlMap = new HashMap<String, Object>();
			onlineSqlMap.put("sql", "update user_info_tb set online_flag=? where id=? ");
			onlineSqlMap.put("values", new Object[]{21, uid});
			bathSql.add(onlineSqlMap);
			//更新token
			Map<String, Object> map = daService.getMap("select token from user_session_tb where uin=? ", new Object[]{uid});
			String oldtoken = (String)map.get("token");
			String token = "zldtokenvoid"+System.currentTimeMillis();
			Map<String,String >  parkTokenCacheMap =memcacheUtils.doMapStringStringCache("parkuser_token", null, null);
			if(parkTokenCacheMap!=null){
				parkTokenCacheMap.put(token, uin + "_" + -1 + "_" + groupid);
				if(oldtoken!=null){
					Map<String, Object> sessionSqlMap = new HashMap<String, Object>();
					sessionSqlMap.put("sql", "update user_session_tb set token=? ,create_time=? where uin=? ");
					sessionSqlMap.put("values", new Object[]{token, ntime, uid});
					bathSql.add(sessionSqlMap);
				}
			}
			boolean b = daService.bathUpdate2(bathSql);
			if(b){
				result = "1";
				parkTokenCacheMap.remove(oldtoken);
				memcacheUtils.doMapStringStringCache("parkuser_token", parkTokenCacheMap, "update");
			}
			AjaxUtil.ajaxOutput(response,result);
		}
		return null;
	}

	private int editBerthSeg(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		String uuid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "uuid"));
		String berthsec_name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "berthsec_name"));
		String park_uuid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "park_uuid"));
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		Double longitude = RequestUtil.getDouble(request, "longitude", 0d);
		Double latitude = RequestUtil.getDouble(request, "latitude", 0d);
		Integer is_active = RequestUtil.getInteger(request, "is_active", 0);

		int r = daService.update("update com_berthsecs_tb set uuid=?,berthsec_name=?,park_uuid=?,address=?,longitude=?,latitude=?,is_active=? where id=? ",
				new Object[]{uuid, berthsec_name, park_uuid, address, longitude, latitude, is_active, id});
		return r;
	}

	private int createBerthSeg(HttpServletRequest request){
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		if(comid == -1){
			return -1;
		}
		String uuid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "uuid"));
		String berthsec_name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "berthsec_name"));
		String park_uuid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "park_uuid"));
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		Double longitude = RequestUtil.getDouble(request, "longitude", 0d);
		Double latitude = RequestUtil.getDouble(request, "latitude", 0d);
		Integer is_active = RequestUtil.getInteger(request, "is_active", 0);

		int r = daService.update("insert into com_berthsecs_tb(uuid,berthsec_name,park_uuid,create_time,address,longitude,latitude,is_active,comid) values(?,?,?,?,?,?,?,?,?)",
				new Object[]{uuid, berthsec_name, park_uuid, System.currentTimeMillis()/1000, address, longitude, latitude, is_active, comid});
		return r;
	}

	private void setList(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> segids = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> map : list){
				segids.add(map.get("id"));
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			segids.add(0);
			List<Map<String, Object>> list2 = pgOnlyReadService.getAllMap("select count(id) pcount,berthsec_id from com_park_tb where berthsec_id in ("+preParams+") and is_delete=? group by berthsec_id ", segids);
			if(list2 != null && !list2.isEmpty()){
				for(Map<String, Object> map : list){
					Long id = (Long)map.get("id");
					map.put("berthnum", 0);
					for(Map<String, Object> map2 : list2){
						Long berthsec_id = (Long)map2.get("berthsec_id");
						if(id.intValue() == berthsec_id.intValue()){
							map.put("berthnum", map2.get("pcount"));
							break;
						}
					}
				}
			}
		}
	}

}
