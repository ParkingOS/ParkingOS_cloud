package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityInduceL2ManageAction extends Action {
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
		Integer type = RequestUtil.getInteger(request, "type", 0);

		if(action.equals("")){
			request.setAttribute("type", type);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select i.*,a.ad,a.begin_time,a.end_time,a.isactive,a.publish_time from induce_tb i left join induce_ad_tb a on i.id=a.induce_id where i.state=? and i.cityid=? and i.type=? and i.is_delete=? " ;
			String countSql = "select count(i.*) from induce_tb i left join induce_ad_tb a on i.id=a.induce_id where i.state=? and i.cityid=? and i.type=? and i.is_delete=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"induce_tb","i",new String[]{"comid"});
			SqlInfo sqlInfo1 = getSuperSqlInfo(request);
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(cityid);
			params.add(type);
			params.add(0);
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
			count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
			}
			setList(list);
			commonMethods.getState(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("create")){
			int r = createInduce(request, cityid, uin);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			int r = editInduce(request, uin);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("delete")){
			int r = deleteInduce(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("parkdetail")){
			Long induce_id = RequestUtil.getLong(request, "induce_id", -1L);
			request.setAttribute("induce_id", induce_id);
			return mapping.findForward("parklist");
		}else if(action.equals("parkquery")){
			String sql = "select * from induce_park_tb where induce_id=? ";
			String countSql = "select count(*) from induce_park_tb where induce_id=? ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			Long induce_id = RequestUtil.getLong(request, "induce_id", -1L);
			List<Object> params = new ArrayList<Object>();
			params.add(induce_id);
			count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by sort nulls last ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("parkcreate")){
			Long induce_id = RequestUtil.getLong(request, "induce_id", -1L);
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			if(induce_id > 0 && comid > 0){
				Long count = pgOnlyReadService.getLong("select count(id) from induce_park_tb where induce_id=? and comid=? ",
						new Object[]{induce_id, comid});
				if(count > 0){
					AjaxUtil.ajaxOutput(response, "已经添加该车场");
					return null;
				}
				int r = daService.update("insert into induce_park_tb(induce_id,comid) values(?,?)",
						new Object[]{induce_id, comid});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "0");
		}else if(action.equals("parkdelete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			int r = daService.update("delete from induce_park_tb where id=? ", new Object[]{id});
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("hisdetail")){
			Long induce_id = RequestUtil.getLong(request, "induce_id", -1L);
			request.setAttribute("induce_id", induce_id);
			return mapping.findForward("hislist");
		}else if(action.equals("hisquery")){
			String sql = "select * from induce_ad_history_tb where induce_id=? ";
			String countSql = "select count(*) from induce_ad_history_tb where induce_id=? ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			Long induce_id = RequestUtil.getLong(request, "induce_id", -1L);
			List<Object> params = new ArrayList<Object>();
			params.add(induce_id);
			count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
			}
			setList2(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("editad")){
			int r = editAd(request, uin);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("publish")){
			int r = publish(request, uin);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("bathpublish")){
			int r = bathPublish(request, uin);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("moduledetail")){
			Long induce_id = RequestUtil.getLong(request, "induce_id", -1L);
			request.setAttribute("induce_id", induce_id);
			return mapping.findForward("modulelist");
		}

		return null;
	}

	private int editAd(HttpServletRequest request, Long updator_id){
		Long induce_id = RequestUtil.getLong(request, "induce_id", -1L);
		String ad = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "ad"));
		String begin_time = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "begin_time"));
		String end_time = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "end_time"));
		Long btime = null;
		Long etime = null;
		if(!begin_time.equals("")){
			btime = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(begin_time);
		}
		if(!end_time.equals("")){
			etime = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(end_time);
		}
		if(ad.equals("")){
			ad = null;
		}
		Long ntime = System.currentTimeMillis()/1000;
		if(induce_id < 0){
			return -1;
		}
		Map<String, Object> map = pgOnlyReadService.getMap("select * from induce_ad_tb where induce_id=? ",
				new Object[]{induce_id});
		int r = 0;
		if(map != null){
			Integer isactive = (Integer)map.get("isactive");
			if(isactive == 1){//已发布
				String his_ad = null;
				if(map.get("ad") != null){
					his_ad = (String)map.get("ad");
				}
				if(his_ad == null){
					if(ad != null){
						isactive = 0;
					}
				}else{
					if(ad == null || !his_ad.equals(ad)){
						isactive = 0;
					}else{
						Long begin = null;
						Long end = null;
						if(map.get("begin_time") != null){
							begin = (Long)map.get("begin_time");
						}
						if(map.get("end_time") != null){
							end = (Long)map.get("end_time");
						}
						boolean b = false;
						boolean e = false;
						if(btime == null){
							if(begin == null){
								b = true;
							}
						}else{
							if(begin != null && btime.intValue() == begin.intValue()){
								b = true;
							}
						}
						if(b){
							if(etime == null){
								if(end == null){
									e = true;
								}
							}else{
								if(end != null && etime.intValue() == end.intValue()){
									e = true;
								}
							}
						}
						if(!e){
							isactive = 0;
						}
					}
				}
			}
			r = daService.update("update induce_ad_tb set ad=?,begin_time=?,end_time=?,update_time=?,isactive=?,updator_id=? where induce_id=? ",
					new Object[]{ad, btime, etime, ntime, isactive, updator_id, induce_id});
		}else{
			r = daService.update("insert into induce_ad_tb(induce_id,ad,begin_time,end_time,create_time,updator_id) values(?,?,?,?,?,?)",
					new Object[]{induce_id, ad, btime, etime, ntime, updator_id});
		}

		return r;
	}

	private int bathPublish(HttpServletRequest request, Long publishor){
		String ids = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "ids"));
		if(ids.equals("")){
			return -1;
		}
		Long ntime = System.currentTimeMillis()/1000;
		String[] idList = ids.split(",");
		List<Object> params1 = new ArrayList<Object>();
		List<Object> params2 = new ArrayList<Object>();
		params1.add(0);
		params2.add(1);
		params2.add(ntime);
		params2.add(0);
		String preParams  ="";
		for(int i = 0; i< idList.length; i++){
			Long induce_id = Long.valueOf(idList[i]);
			params1.add(induce_id);
			params2.add(induce_id);
			if(preParams.equals(""))
				preParams ="?";
			else
				preParams += ",?";
		}

		List<Map<String, Object>> nopubList = pgOnlyReadService.getAllMap("select * from induce_ad_tb where isactive=? and induce_id in ("+preParams+") ", params1);
		if(nopubList == null || nopubList.isEmpty()){
			return -2;
		}

		int r = daService.update("update induce_ad_tb set isactive=?,publish_time=? where isactive=? and induce_id in ("+preParams+") ", params2);
		if(r > 0){
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			for(Map<String, Object> map : nopubList){
				Map<String, Object> hismap = new HashMap<String, Object>();
				hismap.put("sql", "insert into induce_ad_history_tb(induce_id,create_time,begin_time,end_time,ad,creator_id) values(?,?,?,?,?,?) ");
				hismap.put("values", new Object[]{map.get("induce_id"), ntime, map.get("begin_time"), map.get("end_time"), map.get("ad"), publishor});
				bathSql.add(hismap);
			}
			boolean b = daService.bathUpdate(bathSql);
		}
		if(r > 0){
			return 1;
		}
		return 0;
	}

	private int publish(HttpServletRequest request, Long publishor){
		Long induce_id = RequestUtil.getLong(request, "induce_id", -1L);
		if(induce_id < 0){
			return -1;
		}
		Long ntime = System.currentTimeMillis()/1000;
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		Map<String, Object> induceSqlMap = new HashMap<String, Object>();
		Map<String, Object> hisSqlMap = new HashMap<String, Object>();
		induceSqlMap.put("sql", "update induce_ad_tb set isactive=?,publish_time=? where induce_id=? ");
		induceSqlMap.put("values", new Object[]{1, ntime, induce_id});
		bathSql.add(induceSqlMap);
		Map<String, Object> induceMap = pgOnlyReadService.getMap("select * from induce_ad_tb where induce_id=? ",
				new Object[]{induce_id});
		if(induceMap == null){
			return -1;
		}
		hisSqlMap.put("sql", "insert into induce_ad_history_tb(induce_id,create_time,begin_time,end_time,ad,creator_id) values(?,?,?,?,?,?) ");
		hisSqlMap.put("values", new Object[]{induce_id, ntime, induceMap.get("begin_time"), induceMap.get("end_time"), induceMap.get("ad"), publishor});
		bathSql.add(hisSqlMap);
		boolean b = daService.bathUpdate(bathSql);
		if(b){
			return 1;
		}
		return 0;
	}

	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		Integer comid = RequestUtil.getInteger(request, "comid_start", -1);
		SqlInfo sqlInfo1 = null;
		if(comid > -1){
			sqlInfo1 = new SqlInfo(" i.id in (select induce_id from induce_park_tb where comid=?) ",new Object[]{comid});
		}
		return sqlInfo1;
	}

	private int deleteInduce(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		int r = daService.update("update induce_tb set is_delete=? where id=? ",
				new Object[]{1, id});
		return r;
	}

	private int editInduce(HttpServletRequest request, Long updator_id){
		Long id = RequestUtil.getLong(request, "id", -1L);
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		Double longitude = RequestUtil.getDouble(request, "longitude", 0d);
		Double latitude = RequestUtil.getDouble(request, "latitude", 0d);
		String did = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "did"));
		Long ntime = System.currentTimeMillis()/1000;
		if(longitude == 0d) longitude = null;
		if(latitude == 0d) latitude = null;
		if(did.equals("")) did = null;
		if(did != null && !did.equals("")){
			Long count = pgOnlyReadService.getLong("select count(id) from induce_tb where is_delete=? and did=? and id<>? ",
					new Object[]{0, did, id});
			if(count > 0){
				return -2;
			}
		}
		int r = daService.update("update induce_tb set name=?,longitude=?,latitude=?,address=?,update_time=?,updator_id=?,did=? where id=? ",
				new Object[]{name, longitude, latitude, address, ntime, updator_id, did, id});
		return r;
	}

	private int createInduce(HttpServletRequest request, Long cityid, Long creator_id){
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
		Integer type = RequestUtil.getInteger(request, "type", 2);
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		Double longitude = RequestUtil.getDouble(request, "longitude", 0d);
		Double latitude = RequestUtil.getDouble(request, "latitude", 0d);
		String did = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "did"));
		Long ntime = System.currentTimeMillis()/1000;
		if(longitude == 0d) longitude = null;
		if(latitude == 0d) latitude = null;
		if(did.equals("")) did = null;
		if(did != null && !did.equals("")){
			Long count = pgOnlyReadService.getLong("select count(id) from induce_tb where is_delete=? and did=? ",
					new Object[]{0, did});
			if(count > 0){
				return -2;
			}
		}
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		Map<String, Object> induceSqlMap = new HashMap<String, Object>();
		Map<String, Object> adSqlMap = new HashMap<String, Object>();
		Long induce_id = daService.getkey("seq_induce_tb");
		induceSqlMap.put("sql", "insert into induce_tb(id,name,type,longitude,latitude,address,cityid,state,create_time,creator_id,did) values(?,?,?,?,?,?,?,?,?,?,?)");
		induceSqlMap.put("values", new Object[]{induce_id, name, type, longitude, latitude, address, cityid, 0, ntime, creator_id, did});
		bathSql.add(induceSqlMap);
		adSqlMap.put("sql", "insert into induce_ad_tb(induce_id,ad,begin_time,end_time,create_time) values(?,?,?,?,?)");
		adSqlMap.put("values", new Object[]{induce_id, null, null, null, ntime});
		bathSql.add(adSqlMap);
		boolean b = daService.bathUpdate(bathSql);
		if(b){
			return 1;
		}
		return 0;
	}

	private void setList2(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> creatorids = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> map : list){
				creatorids.add(map.get("creator_id"));
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}

			List<Map<String, Object>> list2 = pgOnlyReadService.getAllMap("select id,nickname from user_info_tb where id in ("+preParams+")", creatorids);
			if(list2 != null && !list2.isEmpty()){
				for(Map<String, Object> map : list){
					Long creator_id = (Long)map.get("creator_id");
					for(Map<String, Object> map2 : list2){
						Long id = (Long)map2.get("id");
						if(creator_id.intValue() == id.intValue()){
							map.put("creator_name", map2.get("nickname"));
							break;
						}
					}
				}
			}
		}
	}

	private void setList(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> creators = new ArrayList<Object>();
			List<Object> updators = new ArrayList<Object>();
			List<Object> induceids = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> map : list){
				creators.add(map.get("creator_id"));
				updators.add(map.get("updator_id"));
				induceids.add(map.get("id"));
				map.put("hcount", 0);
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}

			List<Map<String, Object>> list2 = pgOnlyReadService.getAllMap("select id,nickname from user_info_tb where id in ("+preParams+")", creators);
			if(list2 != null && !list2.isEmpty()){
				for(Map<String, Object> map : list){
					Long creator_id = (Long)map.get("creator_id");
					for(Map<String, Object> map2 : list2){
						Long id = (Long)map2.get("id");
						if(creator_id.intValue() == id.intValue()){
							map.put("creator_name", map2.get("nickname"));
							break;
						}
					}
				}
			}

			list2 = pgOnlyReadService.getAllMap("select id,nickname from user_info_tb where id in ("+preParams+")", updators);
			if(list2 != null && !list2.isEmpty()){
				for(Map<String, Object> map : list){
					Long creator_id = (Long)map.get("updator_id");
					for(Map<String, Object> map2 : list2){
						Long id = (Long)map2.get("id");
						if(creator_id.intValue() == id.intValue()){
							map.put("update_name", map2.get("nickname"));
							break;
						}
					}
				}
			}


			list2 = pgOnlyReadService.getAllMap("select induce_id,count(id) hcount from induce_ad_history_tb where induce_id in ("+preParams+") group by induce_id ", induceids);
			if(list2 != null && !list2.isEmpty()){
				for(Map<String, Object> map : list){
					Long id = (Long)map.get("id");
					for(Map<String, Object> map2 : list2){
						Long induce_id = (Long)map2.get("induce_id");
						if(induce_id.intValue() == id.intValue()){
							map.put("hcount", map2.get("hcount"));
							break;
						}
					}
				}
			}
		}
	}
}
