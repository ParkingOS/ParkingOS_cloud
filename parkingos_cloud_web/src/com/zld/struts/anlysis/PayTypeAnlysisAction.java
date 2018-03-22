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

public class PayTypeAnlysisAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;

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
			commonMethods.setIndexAuthId(request);
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			request.setAttribute("btime", df2.format(System.currentTimeMillis()));
			request.setAttribute("etime",  df2.format(System.currentTimeMillis()));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			String nowtime= df2.format(System.currentTimeMillis());
			String sqlorder = " select d.comid, company_name, account,cash,bymobile,bymonth,prepaid,unionpay,storcard," +
					"free,allfact from" +
					"(select b.company_name as company_name ," +
					"sum( case when pay_type=0 and a.state=1  then a.total  else 0 end)  as account ," +
					"sum( case when pay_type=1 and a.state=1  then a.total  else 0 end)  as cash ," +
					"sum( case when pay_type=2 and a.state=1  then a.total  else 0 end)  as bymobile ," +
					"sum( case when pay_type=3 and a.state=1  then a.total  else 0 end)  as  bymonth," +
					"sum( case when pay_type=4 and a.state=1  then a.total  else 0 end)  as  prepaid," +
					"sum( case when pay_type=5 and a.state=1  then a.total  else 0 end)  as unionpay ," +
					"sum( case when pay_type=6 and a.state=1  then a.total  else 0 end)  as storcard ," +
					"sum( case when pay_type=8 and a.state=1 then a.total  else 0 end)  as free ," +
					"sum( case when a.state=1  then a.total  else 0 end)  as allfact ," +
					"a.comid  from order_tb as a left join  com_info_tb as b on a.comid=b.id where a.ishd=? and a.create_time between ? and ?  group  by a.comid ,b.company_name) as d where ";
			String sqlnopayment = "select  sum( case when state=1 then act_total  else 0 end)  as payment," +
					"sum( case when state=0 then total  else 0 end)   as nopayment," +
					"comid from   no_payment_tb where is_delete=? and create_time between ? and ? and  ";
			//String countSql ="select count(*)  from   no_payment_tb  as a  left join  com_info_tb as b on a.comid=b.id  where a.create_time between ? and ?";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Map<String, Object>> orderlist = null;
			List<Map<String, Object>> nopaylist = null;
			int count = 0;
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			if(groupid==-1){
				groupid= RequestUtil.getLong(request, "groupid", -1L);
			}
			if(btime.equals(""))
				btime = nowtime;
			if(etime.equals(""))
				etime = nowtime;
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
			Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
			List<Object> orderparams = new ArrayList<Object>();
			List<Object> nopayparams = new ArrayList<Object>();
			orderparams.add(0);
			orderparams.add(b);
			orderparams.add(e);
			nopayparams.add(0);
			nopayparams.add(b);
			nopayparams.add(e);
			List<Object> parks = null;
			if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}else if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(comid > 0){
				parks.add(comid);
			}
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sqlorder += "d.comid in ("+preParams+")";
				sqlnopayment+= "comid in ("+preParams+") group by comid ";
				orderparams.addAll(parks);
				nopayparams.addAll(parks);
				orderlist = pgOnlyReadService.getAllMap(sqlorder,orderparams);
				nopaylist= pgOnlyReadService.getAllMap(sqlnopayment,nopayparams);
				//把nopaylist查到的数据追加到orderlist
				if(orderlist!=null&&!orderlist.isEmpty()){
					if(nopaylist!=null&&!nopaylist.isEmpty()){
						for(Map<String, Object> map: orderlist){
							map.put("payment",0);
							Long order_comid=(Long)map.get("comid");
							if(order_comid!=null&&order_comid>0){
								for(Map<String, Object> nMap: nopaylist){
									Long nocomid = (Long)nMap.get("comid");
									if(nocomid!=null&&nocomid>0){
										if(order_comid.equals(nocomid)){
											map.put("payment", nMap.get("payment"));
											map.put("allfact",StringUtils.formatDouble(map.get("allfact"))+
													StringUtils.formatDouble(nMap.get("payment")));
											break;
										}
									}
								}
							}
						}
					}
					//如果追缴表没有查到时间段内的数据置已追缴为0
					else {
						for(Map<String, Object> map: orderlist){
							map.put("payment",0);
						}
					}
				}
				if(orderlist != null && !orderlist.isEmpty()){
					count = orderlist.size();
				}
			}
			Double allfact = 0.0;
			Double cash = 0.0;
			if(orderlist != null && !orderlist.isEmpty()){
				for(Map<String, Object> map : orderlist){
					allfact += StringUtils.formatDouble(map.get("allfact"));
					cash += StringUtils.formatDouble(map.get("cash"));
				}
			}
			String res = "总收入："+allfact+"，现金收费："+cash;

			//setList(list);
			String json = JsonUtil.anlysisMap2Json(orderlist,pageNum,count, fieldsstr,"id",res);

			//json = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response, json);
		}

		return null;
	}



}
