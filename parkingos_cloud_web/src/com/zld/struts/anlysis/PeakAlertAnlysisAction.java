package com.zld.struts.anlysis;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.RequestUtil;
import com.zld.utils.TimeTools;
import net.sf.json.JSONArray;
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

public class PeakAlertAnlysisAction extends Action {
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
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			Long today = TimeTools.getToDayBeginTime();
			request.setAttribute("btime", df2.format(today*1000));
			return mapping.findForward("parkgantt");
		}else if(action.equals("peakgantt")){
			String btime = RequestUtil.processParams(request, "btime");
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
			String sql = "select p.*,c.company_name from peakalert_anlysis_tb p,com_info_tb c where p.comid=c.id and p.create_time between ? and ? ";
			List<Map<String, Object>> list = null;
			List<Object> params = new ArrayList<Object>();
			params.add(b);
			params.add(b + 24*60*60);
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
				sql += " and p.comid in ("+preParams+") ";
				params.addAll(parks);
				sql += " order by p.create_time ";//必须升序排序
				list = pgOnlyReadService.getAllMap(sql, params);
			}
			list = setList(list);
			JSONArray json = JSONArray.fromObject(list);
			AjaxUtil.ajaxOutput(response, json.toString());
		}

		return null;
	}

	private List<Map<String, Object>> setList(List<Map<String, Object>> list){
		double rateline = 0.85d;//高峰预警分界线
		List<Map<String, Object>> rList = new ArrayList<Map<String,Object>>();
		List<Object> comidList = new ArrayList<Object>();
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				Long comid = (Long)map.get("comid");
				Long ctime = (Long)map.get("create_time");
				Integer present = (Integer)map.get("present");
				Integer berths = (Integer)map.get("berths");
				Long begintime = TimeTools.getBeginTime(ctime*1000);
				if(berths == 0){//泊位总数不能为零
					continue;
				}
				Long duration = (ctime - begintime)/60;
				double rate = (double)present/berths;
				if(comidList.contains(comid)){
					for(Map<String, Object> map2 : rList){
						Long com_id = (Long)map2.get("comid");
						if(comid.intValue() == com_id.intValue()){
							List<Map<String, Object>> segments = (List<Map<String, Object>>)map2.get("segments");
							if(!segments.isEmpty()){
								Map<String, Object> lastMap = segments.get(segments.size() - 1);//获取时间最近的一次高峰预警
								Integer flag = (Integer)lastMap.get("flag");
								if(flag == 0){//最近的一次预警还未结束
									if(rate < rateline){//高峰预警结束
										lastMap.put("flag", 1);
									}
									lastMap.put("end", duration);//重置预警结束时间
								}else{//最近的一次高峰预警已结束
									if(rate >= rateline){
										Map<String, Object> segMap = new HashMap<String, Object>();
										segMap.put("start", duration);//预警开始时间
										segMap.put("end", duration);//预警结束时间,初始化数据为预警开始时间
										segMap.put("flag", 0);//flag==0表示预警还未结束
										segments.add(segMap);
									}
								}
							}else{
								if(rate >= rateline){//大于85%表示高峰预警
									Map<String, Object> segMap = new HashMap<String, Object>();
									segMap.put("start", duration);//预警开始时间
									segMap.put("end", duration);//预警结束时间,初始化数据为开始时间
									segMap.put("flag", 0);//flag==0表示预警还未结束
									segments.add(segMap);
								}
							}
							break;
						}
					}
				}else{
					comidList.add(comid);
					Map<String, Object> infoMap = new HashMap<String, Object>();
					infoMap.put("comid", comid);
					infoMap.put("category", map.get("company_name"));
					List<Map<String, Object>> segList = new ArrayList<Map<String,Object>>();
					if(rate >= rateline){//大于85%表示高峰预警
						Map<String, Object> segMap = new HashMap<String, Object>();
						segMap.put("start", duration);//预警开始时间
						segMap.put("end", duration);//预警结束时间,初始化数据为开始时间
						segMap.put("flag", 0);//flag==0表示预警还未结束
						segList.add(segMap);
					}
					infoMap.put("segments", segList);
					rList.add(infoMap);
				}
			}
		}
		return rList;
	}
}
