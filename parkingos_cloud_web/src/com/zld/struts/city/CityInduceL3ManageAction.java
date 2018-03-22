package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
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

public class CityInduceL3ManageAction extends Action {
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
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select i.*,p.comid from induce_tb i left join induce_park_tb p on i.id=p.induce_id where i.state=? and cityid=? and i.type=? and i.is_delete=? " ;
			String countSql = "select count(i.*) from induce_tb i left join induce_park_tb p on i.id=p.induce_id where i.state=? and cityid=? and i.type=? and i.is_delete=?" ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo sqlInfo1 = getSuperSqlInfo(request);
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"induce_tb","i",new String[]{"comid"});
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(cityid);
			params.add(2);
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
			String r = createInduce(request, cityid, uin);
			AjaxUtil.ajaxOutput(response, r);
		}else if(action.equals("edit")){
			String r = editInduce(request, uin);
			AjaxUtil.ajaxOutput(response, r);
		}else if(action.equals("delete")){
			int r = deleteInduce(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}

		return null;
	}

	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		Integer comid = RequestUtil.getInteger(request, "comid_start", -1);
		SqlInfo sqlInfo1 = null;
		if(comid > -1){
			sqlInfo1 = new SqlInfo(" p.comid=? ",new Object[]{comid});
		}
		return sqlInfo1;
	}

	private int deleteInduce(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		int r = daService.update("update induce_tb set is_delete=? where id=? ",
				new Object[]{1, id});
		return r;
	}

	private String editInduce(HttpServletRequest request, Long updator_id){
		Long id = RequestUtil.getLong(request, "id", -1L);
		Long comid = RequestUtil.getLong(request, "comid", -1L);
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
			Long count = pgOnlyReadService.getLong("select count(id) from induce_tb where is_delete=? and did=? and id<>? ",
					new Object[]{0, did, id});
			if(count > 0){
				return "-2";
			}
		}
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		Map<String, Object> induceSqlMap = new HashMap<String, Object>();
		Map<String, Object> induceParkSqlMap = new HashMap<String, Object>();
		induceSqlMap.put("sql", "update induce_tb set name=?,type=?,longitude=?,latitude=?,address=?,update_time=?,updator_id=?,did=? where id=? ");
		induceSqlMap.put("values", new Object[]{name, type, longitude, latitude, address, ntime, updator_id, did, id});
		bathSql.add(induceSqlMap);
		if(comid > 0){
			Long count = pgOnlyReadService.getLong("select count(id) from induce_park_tb where induce_id=? ",
					new Object[]{id});
			if(count > 0){
				induceParkSqlMap.put("sql", "update induce_park_tb set comid=? where induce_id=? ");
				induceParkSqlMap.put("values", new Object[]{comid, id});
				bathSql.add(induceParkSqlMap);
			}else{
				induceParkSqlMap.put("sql", "insert into induce_park_tb(induce_id,comid) values(?,?)");
				induceParkSqlMap.put("values", new Object[]{id, comid});
				bathSql.add(induceParkSqlMap);
			}
		}else{
			induceParkSqlMap.put("sql", "delete from induce_park_tb where induce_id=? ");
			induceParkSqlMap.put("values", new Object[]{id});
			bathSql.add(induceParkSqlMap);
		}
		boolean b = daService.bathUpdate2(bathSql);
		if(b){
			return "1";
		}
		return "0";
	}

	private String createInduce(HttpServletRequest request, Long cityid, Long creator_id){
		Long comid = RequestUtil.getLong(request, "comid", -1L);
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
				return "-2";
			}
		}
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		Map<String, Object> induceSqlMap = new HashMap<String, Object>();
		Map<String, Object> induceParkSqlMap = new HashMap<String, Object>();
		Long induce_id = daService.getkey("seq_induce_tb");
		induceSqlMap.put("sql", "insert into induce_tb(id,name,type,longitude,latitude,address,cityid,state,create_time,creator_id,did) values(?,?,?,?,?,?,?,?,?,?,?)");
		induceSqlMap.put("values", new Object[]{induce_id, name, type, longitude, latitude, address, cityid, 0, ntime, creator_id, did});
		bathSql.add(induceSqlMap);
		if(comid > 0){
			induceParkSqlMap.put("sql", "insert into induce_park_tb(induce_id,comid) values(?,?)");
			induceParkSqlMap.put("values", new Object[]{induce_id, comid});
			bathSql.add(induceParkSqlMap);
		}
		boolean b = daService.bathUpdate(bathSql);
		if(b){
			return "1";
		}
		return "0";
	}

	private void setList(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> creators = new ArrayList<Object>();
			List<Object> updators = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> map : list){
				creators.add(map.get("creator_id"));
				updators.add(map.get("updator_id"));
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
		}
	}
}
