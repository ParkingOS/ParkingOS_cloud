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
import java.util.List;
import java.util.Map;

public class PaymentAnlysisAction extends Action {
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
		Long comid=(Long)request.getSession().getAttribute("comid");
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}

		if(cityid == null) cityid = -1L;
		if(groupid == null) groupid = -1L;

		if(action.equals("")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			request.setAttribute("btime", df2.format(System.currentTimeMillis()));
			//request.setAttribute("etime",  df2.format(System.currentTimeMillis()));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			String nowtime= df2.format(System.currentTimeMillis());
			String sql = "	select c.company_name as company_name,c.comid as comid ,c.nopayment as nopayment ,d.payment as payment " +
					" from (select  sum( case when a.state=0 then a.total  else 0 end)   as nopayment,comid ,company_name" +
					" from no_payment_tb  as a left join com_info_tb as b on a.comid=b.id  where a.is_delete=? and a.end_time < ? group by comid ,company_name ) as c left join  (select  sum( case when a.state=1 then a.act_total  else 0 end)   as payment,comid ," +
					"company_name from no_payment_tb  as a left join com_info_tb as b on a.comid=b.id  where a.is_delete=? and a.pursue_time < ? group by comid ,company_name  ) as  d on c.comid=d.comid where   ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Map<String, Object>> list = null;
			int count = 0;
			String btime = RequestUtil.processParams(request, "btime");
			//String etime = RequestUtil.processParams(request, "etime");
			if(btime.equals(""))
				btime = nowtime;
			if(groupid==-1){
				groupid= RequestUtil.getLong(request, "groupid", -1L);
			}
			Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime+" 23:59:59");
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(e);
			params.add(0);
			params.add(e);
			List<Object> parks = null;
			if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}else if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}
			else if(comid>0)
			{
				params.add(comid);
			}
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " c.comid in ("+preParams+") ";
				params.addAll(parks);
				list = pgOnlyReadService.getAllMap(sql,params);
				if(list!=null&&!list.isEmpty()){
					for(Map<String, Object> map:list){
						double nopayment=StringUtils.formatDouble(map.get("nopayment"));
						double payment=StringUtils.formatDouble(map.get("payment"));
						if(nopayment==0){
							map.put("percent", 0);
						}else {
							map.put("percent",StringUtils.formatDouble((payment/(payment+nopayment))*100));
						}

					}
				}
				if(list != null && !list.isEmpty()){
					count = list.size();
				}
			}

			Double nopayment = 0.0;
			Double payment = 0.0;
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					nopayment += StringUtils.formatDouble(map.get("nopayment"));
					payment += StringUtils.formatDouble(map.get("payment"));
				}
			}
			String res = "总未缴金额："+nopayment+"，已追缴金额："+payment;

			//setList(list);
			String json = JsonUtil.anlysisMap2Json(list,1,count, fieldsstr,"comid",res);

			//json = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response, json);
		}
		else if(action.equals("echarts")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			String nowtime= df2.format(System.currentTimeMillis());
			String sql = "	select c.company_name as company_name,c.comid as comid ,c.nopayment as nopayment ,d.payment as payment " +
					" from (select  sum( case when a.state=0 then a.total  else 0 end)   as nopayment,comid ,company_name" +
					" from no_payment_tb  as a left join com_info_tb as b on a.comid=b.id  where a.is_delete=? and a.end_time < ? group by comid ,company_name ) as c left join  (select  sum( case when a.state=1 then a.act_total  else 0 end)   as payment,comid ," +
					"company_name from no_payment_tb  as a left join com_info_tb as b on a.comid=b.id  where a.is_delete=? and a.pursue_time < ? group by comid ,company_name  ) as  d on c.comid=d.comid where   ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Map<String, Object>> list = null;
			int count = 0;
			String btime = RequestUtil.processParams(request, "btime");
			String operate = RequestUtil.getString(request, "operate");
			//String etime = RequestUtil.processParams(request, "etime");
			if(btime.equals(""))
				btime = nowtime;
			Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime+" 23:59:59");
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(e);
			params.add(0);
			params.add(e);
			List<Object> parks = null;
			if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}else if(comid>0)
			{
				params.add(comid);
			}
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " c.comid in ("+preParams+") ";
				params.addAll(parks);
				list = pgOnlyReadService.getAllMap(sql,params);
				if(list!=null&&!list.isEmpty()){
					for(Map<String, Object> map:list){
						double nopayment=StringUtils.formatDouble(map.get("nopayment"));
						double payment=StringUtils.formatDouble(map.get("payment"));
						if(nopayment==0){
							map.put("percent", 0);
						}else {
							map.put("percent",StringUtils.formatDouble((payment/(payment+nopayment))*100));
						}

					}

				}
				if(list != null && !list.isEmpty()){
					count = list.size();
				}
			}




			String json = StringUtils.createJson(list);
			//AjaxUtil.ajaxOutput(response, json);
			if(operate.equals("")){
				request.setAttribute("btime", df2.format(System.currentTimeMillis()));

				//request.setAttribute("etime",  df2.format(System.currentTimeMillis()));
				request.setAttribute("json", json);
				return mapping.findForward("icon");
			}else {
				AjaxUtil.ajaxOutput(response, json);
			}
		}

		return null;
	}



}
