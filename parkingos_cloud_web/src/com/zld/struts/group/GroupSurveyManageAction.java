package com.zld.struts.group;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.DataBaseService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupSurveyManageAction extends Action {
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
		String action = RequestUtil.processParams(request, "action");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Integer isHd = (Integer)request.getSession().getAttribute("ishdorder");
		if(uin==null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			Map<String, Object> map = pgOnlyReadService.getMap("select id,company_name from com_info_tb where groupid=? and state!=? order by id limit ?",
					new Object[]{groupid, 1, 1});
			if(map != null){
				request.setAttribute("comid", map.get("id"));
				request.setAttribute("company_name", map.get("company_name"));
			}
			return mapping.findForward("list");
		}else if(action.equals("psurvey")){
			String sql = "select id,company_name,parking_total from com_info_tb where groupid=? and state!=? " ;
			String countSql = "select count(id) from com_info_tb where groupid=? and state!=? " ;

			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Map<String, Object>> list = null;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(groupid);
			params.add(1);
			Long count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by id ",params, pageNum, pageSize);
				setListNew(list, isHd);
				setLots(list);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("dsurvey")){
			String sql = "select id from com_info_tb where groupid=? " ;
			String countSql = "select count(id) from com_info_tb where groupid=? " ;

			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(groupid);
			Long count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setList(List<Map<String, Object>> list, Integer ishd){
		Long b = TimeTools.getToDayBeginTime();
		Long e = System.currentTimeMillis()/1000;
		if(list != null && !list.isEmpty()){
			for(Map map : list){
				Long id = (Long)map.get("id");
				Double etotal = 0d;
				Double ctotal = 0d;
				List<Map<String, Object>> list2 = pgOnlyReadService.getAll("select distinct(uid) from order_tb where comid=? and state=? and uid>? and end_time between ? and ? ",
						new Object[]{id, 1, 0, b, e});
				if(list2 != null && !list2.isEmpty()){
					for(Map map2 : list2){
						Long uid = (Long)map2.get("uid");
						String result = commonMethods.getTicketAndCenterPay(uid, b, e, ishd,id);
						Double cash = Double.valueOf(result.split("_")[2]);
						Double epay = Double.valueOf(result.split("_")[3]);
						etotal += epay;
						ctotal += cash;
					}
				}
				map.put("etotal", StringUtils.formatDouble(etotal));
				map.put("ctotal", StringUtils.formatDouble(ctotal));
				map.put("atotal", StringUtils.formatDouble(etotal + ctotal));
			}
		}
	}

	private void setListNew(List<Map<String, Object>> list, Integer ishd){
		Long b = TimeTools.getToDayBeginTime();
		Long e = System.currentTimeMillis()/1000;
		if(list != null && !list.isEmpty()){
			double cash = 0.0;
			Integer month = 0;
			double wallet = 0.0;
			double total = 0.0;
			for(Map map : list){
				Long id = (Long)map.get("id");
				String result = commonMethods.getParkTotalStatistic(id, b, e);
				cash = Double.valueOf(result.split("_")[0]);
				month = Integer.valueOf(result.split("_")[1]);
				wallet = Double.valueOf(result.split("_")[2]);
				total = Double.valueOf(result.split("_")[3]);
				map.put("etotal", StringUtils.formatDouble(wallet)+"");
				map.put("ctotal", StringUtils.formatDouble(cash)+"");
				map.put("mtotal", month);
				map.put("atotal", StringUtils.formatDouble(total)+"");
			}
		}

	}

	@SuppressWarnings({ "rawtypes", "unused" })
	private void setLots(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			for(Map map : list){
				Long comid = (Long)map.get("id");
				Integer parking_total = (Integer)map.get("parking_total");
				long time2 = TimeTools.getBeginTime(System.currentTimeMillis()-2*24*60*60*1000);
				long time16 = TimeTools.getBeginTime(System.currentTimeMillis()-16*24*60*60*1000);
				Long month_used_count = 0L;
				Long time_used_count = 0L;
				String sql = "select count(ID) ucount,pay_type from order_tb where comid=? and create_time>? and state=? group by pay_type ";
				List<Map<String, Object>> allList = pgOnlyReadService.getAll(sql, new Object[]{comid,time2,0});
				if(allList != null && !allList.isEmpty()){
					for(Map<String, Object> map2 : allList){
						Integer pay_type = (Integer)map2.get("pay_type");
						Long ucount = (Long)map2.get("ucount");
						if(pay_type == 3){//月卡车位占用数
							month_used_count = ucount;
						}else{//时租车位占用数
							time_used_count += ucount;
						}
					}
				}

				Long invmonth_used_count = 0L;
				Long invtime_used_count = 0L;
				String sql1 = "select count(ID) ucount,pay_type from order_tb where comid=? and create_time>? and create_time<? and state=? group by pay_type ";
				List<Map<String, Object>> invList = pgOnlyReadService.getAll(sql1, new Object[]{comid,time16,time2,0});
				if(invList != null && !invList.isEmpty()){
					for(Map<String, Object> map2 : invList){
						Integer pay_type = (Integer)map2.get("pay_type");
						Long ucount = (Long)map2.get("ucount");
						if(pay_type == 3){//月卡车位占用数
							invmonth_used_count = ucount;
						}else{//时租车位占用数
							invtime_used_count += ucount;
						}
					}
				}

				int inv_month = (int) (invmonth_used_count*2/14);
				int inv_time = (int) (invtime_used_count*2/14);

				if(month_used_count >= inv_month){
					month_used_count -= inv_month;
				}else{
					month_used_count = 1L;
				}

				if(time_used_count >= inv_time){
					time_used_count -= inv_time;
				}else{
					time_used_count = 1L;
				}

				double ratio = 0d;

				if(parking_total < time_used_count + month_used_count){
					parking_total = time_used_count.intValue() + month_used_count.intValue();
				}
				Integer rlots = parking_total - time_used_count.intValue() - month_used_count.intValue();
				if(parking_total > 0){
					map.put("rlots", rlots);
					map.put("ratio", StringUtils.formatDouble(((double)(time_used_count + month_used_count)/parking_total)*100) + "%");
				}
			}
		}
	}
}
