package com.zld.struts.anlysis;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
import net.sf.json.JSONArray;
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

public class CityParkAnlysisAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Override
	public ActionForward execute(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response) throws Exception{
		String action = RequestUtil.processParams(request, "action");
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
			jSON();
			return mapping.findForward("column");
		}else if(action.equals("parkcolumn")){
			String sql = "select count(id) pcount,sum(parking_total) psum,city,parking_type from com_info_tb where city>? ";
			List<Map<String, Object>> list = null;
			List<Object> params = new ArrayList<Object>();
			params.add(0);
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
				params.addAll(parks);
				sql += "group by city,parking_type order by city ";
				list = pgOnlyReadService.getAllMap(sql, params);
			}
			list = setCity(list);
			String json = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("berthcolumn")){
			String sql = "select sum(parking_total) psum,city from com_info_tb where city>? ";
			List<Map<String, Object>> list = null;
			List<Object> params = new ArrayList<Object>();
			params.add(0);
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
				params.addAll(parks);
				sql += "group by city order by city ";
				list = pgOnlyReadService.getAllMap(sql, params);
			}
			setLocal(list);
			String json = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("list")){
			return mapping.findForward("list");
		}else if(action.equals("querylist")){
			String sql = "select count(id) pcount,sum(parking_total) psum,city,parking_type from com_info_tb where city>? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_info");
			List<Map<String, Object>> list = null;
			Integer count = 0;
			List<Object> params = new ArrayList<Object>();
			params.add(0);
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
				params.addAll(parks);

				if(sqlInfo!=null){
					sql +=" and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				sql += " group by city,parking_type order by city ";
				list = pgOnlyReadService.getAllMap(sql, params);
				list = setCity(list);

				if(list != null){
					count = list.size();
				}
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"city");
			AjaxUtil.ajaxOutput(response, json);
		}

		return null;
	}

	private void setLocal(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				Integer city = (Integer)map.get("city");
				String local = GetLocalCode.localDataMap.get(city);
				map.put("local", local);
			}
		}
	}

	private List<Map<String, Object>> setCity(List<Map<String, Object>> list){
		List<Map<String, Object>> rList = new ArrayList<Map<String,Object>>();
		List<Integer> cities = new ArrayList<Integer>();
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				Integer city = (Integer)map.get("city");
				Integer parking_type = (Integer)map.get("parking_type");
				Long psum = (Long)map.get("psum");
				if(cities.contains(city)){
					Long num = (Long)map.get("pcount");
					for(Map<String, Object> map2 : rList){
						Integer city2 = (Integer)map2.get("city");
						if(city.intValue() == city2.intValue()){
							if(parking_type == 2){
								Long road = Long.valueOf(map2.get("road")+"");
								map2.put("road", road + num);
							}else{
								Long indoor = Long.valueOf(map2.get("indoor")+"");
								map2.put("indoor", indoor + num);
							}
							Long p_sum = (Long)map2.get("psum");
							map2.put("psum", p_sum + psum);
							break;
						}
					}
				}else{
					Map<String, Object> newMap = new HashMap<String, Object>();
					String local = GetLocalCode.localDataMap.get(city);
					newMap.put("city", city);
					newMap.put("local", local);
					newMap.put("psum", psum);
					cities.add(city);
					if(parking_type == 2){
						newMap.put("road", map.get("pcount"));
						newMap.put("indoor", 0L);
						newMap.put("berthroad", map.get("psum"));
					}else{
						newMap.put("road", 0L);
						newMap.put("indoor", map.get("pcount"));
					}
					rList.add(newMap);
				}
			}
		}

		return rList;
	}

	private void jSON(){
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("category", "001车场");

		List<Map<String, Object>> list1 = new ArrayList<Map<String,Object>>();
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put("start", "3");
		map1.put("duration", "30");
		list1.add(map1);
		map.put("segments", list1);
		list.add(map);
		JSONArray json = JSONArray.fromObject(list);
		System.out.println(json.toString());
	}
}
