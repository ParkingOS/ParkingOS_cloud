package com.zld.struts.anlysis;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityParkAccountAnlysisAction extends Action {
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
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			request.setAttribute("btime", df2.format(System.currentTimeMillis()));
			request.setAttribute("etime",  df2.format(System.currentTimeMillis()));
			Map<String, Object> map = getTotal(request, cityid, groupid);
			request.setAttribute("count", map.get("count"));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			String nowtime= df2.format(System.currentTimeMillis());
			String sql = "select count(1) scount,sum(total) total,comid from order_tb where ishd=? and" +
					" total>=? and state=? and pay_type=? and c_type !=? and end_time between ? and ? and  ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Map<String, Object>> list = null;
			if(groupid==-1){
				groupid= RequestUtil.getLong(request, "groupid", -1L);
			}
			int count = 0;
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			if(btime.equals(""))
				btime = nowtime;
			if(etime.equals(""))
				etime = nowtime;
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
			Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(0);
			params.add(1);
			params.add(2);
			params.add(4);
			params.add(b);
			params.add(e);
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
				sql += " comid in ("+preParams+") ";
				params.addAll(parks);
				sql += " group by comid order by scount ";
				list = pgOnlyReadService.getAllMap(sql,params);
				if(list != null && !list.isEmpty()){
					count = list.size();
				}
			}
			setList(list);
			Double total = 0.0;
			Integer scount = 0;
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					total += StringUtils.formatDouble(map.get("total"));
					scount += Integer.valueOf(map.get("scount")+"");
				}
			}
			String res = "结算金额："+StringUtils.formatDouble(total)+"，交易数量："+scount;

			String json = JsonUtil.anlysisMap2Json(list,1,count, fieldsstr,"comid",res);
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("gettotal")){
			Map<String, Object> infoMap =getTotal(request, cityid, groupid);
			String json = StringUtils.createJson(infoMap);
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("tompaydetail")){
			request.setAttribute("parkid", RequestUtil.processParams(request, "parkid"));
			request.setAttribute("btime", RequestUtil.processParams(request, "btime"));
			request.setAttribute("etime", RequestUtil.processParams(request, "etime"));
			return mapping.findForward("mpaydetail");
		}else if(action.equals("mpaydetail")){
			Long parkid = RequestUtil.getLong(request, "parkid", -1L);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			String btime = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "btime"));
			String etime = RequestUtil.processParams(request, "etime");
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
			Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			list = pgOnlyReadService.getAll("select * from order_tb where ishd=? and total>=? " +
							" and state=? and pay_type=? and c_type !=? and end_time between ? and ? and comid=? order by end_time desc ",
					new Object[]{0, 0, 1, 2, 4, b, e , parkid});
			int count = list!=null?list.size():0;
			String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}

		return null;
	}

	@SuppressWarnings("rawtypes")
	private void setName(List list){
		List<Object> comids = new ArrayList<Object>();
		if(list != null && !list.isEmpty()){
			for(int i=0;i<list.size();i++){
				Map map = (Map)list.get(i);
				comids.add(map.get("comid"));
			}
		}
		if(!comids.isEmpty()){
			String preParams  ="";
			for(Object comid : comids){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
			resultList = pgOnlyReadService.getAllMap("select c.id,c.company_name,u.nickname " +
					"from com_info_tb  c,user_info_tb u " +
					" where c.uid=u.id and  c.id in ("+preParams+") ", comids);
			if(!resultList.isEmpty()){
				for(int i=0;i<list.size();i++){
					Map map1 = (Map)list.get(i);
					Long comid=(Long)map1.get("comid");
					for(Map<String,Object> map: resultList){
						Long uin = (Long)map.get("id");
						if(comid.intValue()==uin.intValue()){
							map1.put("cname", map.get("company_name"));
							map1.put("uname", map.get("nickname"));
							break;
						}
					}
				}
			}
		}
	}

	private Map<String, Object> getTotal(HttpServletRequest request, Long cityid, Long groupid){
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
		String nowtime= df2.format(System.currentTimeMillis());
		String btime = RequestUtil.processParams(request, "btime");
		String etime = RequestUtil.processParams(request, "etime");
		if(btime.equals(""))
			btime = nowtime;
		if(etime.equals(""))
			etime = nowtime;
		Long b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
		Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
		List<Object> params = new ArrayList<Object>();
		Long count = 0L;
		String sql = "select count(*) from order_tb where total>=? and state=? and pay_type=? and c_type !=? and end_time between ? and ? and  ";
		params.add(0);
		params.add(1);
		params.add(2);
		params.add(4);
		params.add(b);
		params.add(e);
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
			params.addAll(parks);

			count = pgOnlyReadService.getCount(sql, params);
		}
		Map<String, Object> infoMap = new HashMap<String, Object>();
		infoMap.put("count", count);
		return infoMap;
	}

	private void setList(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> idsList = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> map : list){
				idsList.add(map.get("comid"));
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}

			List<Map<String, Object>> list2 = pgOnlyReadService.getAllMap("select id,company_name,groupid from com_info_tb where id in ("+preParams+") ", idsList);
			if(list2 != null && !list2.isEmpty()){
				for(Map<String, Object> map : list){
					Long comid = (Long)map.get("comid");
					for(Map<String, Object> map2 : list2){
						Long id = (Long)map2.get("id");
						if(comid.intValue() == id.intValue()){
							map.put("company_name", map2.get("company_name"));
							break;
						}
					}
				}
			}
		}
	}
}
