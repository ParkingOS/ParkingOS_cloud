package com.zld.struts.anlysis;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoDbUtils;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShouldFactAnlysisAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private DataBaseService daService;
	@Autowired
	private MongoDbUtils mongoDbUtils;
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
			request.setAttribute("etime",  df2.format(System.currentTimeMillis()));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			String nowtime= df2.format(System.currentTimeMillis());
			String sqlorder = "select sum( case when a.state=1 then a.total  else 0 end)  as allfact ," +
					"sum( case when a.state=1 then a.total  else 0 end)  as fact," +
					"sum( case when a.state=1 then a.total  else 0 end)  as shouldfact ," +"b.company_name," +
					"a.comid  from    order_tb as a left join  com_info_tb as b on a.comid=b.id where a.create_time between ? and ? and ";
			String sqlnopayment = "select  sum( case when state=1 then act_total  else 0 end)  as payment," +
					"sum( case when state=0 then total  else 0 end)   as nopayment," +
					"comid from   no_payment_tb where create_time between ? and ? and  ";
			//String countSql ="select count(*)  from   no_payment_tb  as a  left join  com_info_tb as b on a.comid=b.id  where a.create_time between ? and ?";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Map<String, Object>> orderlist = null;
			List<Map<String, Object>> nopaylist = null;
			int count = 0;
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			if(btime.equals(""))
				btime = nowtime;
			if(etime.equals(""))
				etime = nowtime;
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
			Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
			List<Object> orderparams = new ArrayList<Object>();
			List<Object> nopayparams = new ArrayList<Object>();
			orderparams.add(b);
			orderparams.add(e);
			nopayparams.add(b);
			nopayparams.add(e);
			List<Object> parks = null;
			if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(groupid > 0){
				parks = commonMethods.getParks(groupid);
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
				sqlorder += "a.comid in ("+preParams+") group by a.comid,b.company_name  ";
				sqlnopayment+= "comid in ("+preParams+") group by comid ";
				orderparams.addAll(parks);
				nopayparams.addAll(parks);
				orderlist = pgOnlyReadService.getAllMap(sqlorder,orderparams);
				nopaylist= pgOnlyReadService.getAllMap(sqlnopayment,nopayparams);
				//把nopaylist查到的数据追加到orderlist
				if(orderlist!=null&&!orderlist.isEmpty()){
					if(nopaylist!=null&&!nopaylist.isEmpty()){
						for(Map<String, Object> map: orderlist){
							//如果没有匹配到对应的停车场就让追缴和为追缴值为0
							map.put("payment",0);
							map.put("nopayment", 0);
							Long order_comid=(Long)map.get("comid");
							if(order_comid!=null&&order_comid>0){
								for(Map<String, Object> nMap: nopaylist){
									Long nocomid = (Long)nMap.get("comid");
									if(nocomid!=null&&nocomid>0){
										if(order_comid.equals(nocomid)){
											map.put("payment", nMap.get("payment"));
											map.put("nopayment", nMap.get("nopayment"));
											map.put("allfact",StringUtils.formatDouble(map.get("allfact"))+
													StringUtils.formatDouble(nMap.get("payment")));
											map.put("shouldfact",StringUtils.formatDouble(map.get("fact"))+
													StringUtils.formatDouble(nMap.get("nopayment")));
											map.put("factpercent",StringUtils.formatDouble(map.get("fact"))/(StringUtils.formatDouble(map.get("fact"))+
													StringUtils.formatDouble(nMap.get("nopayment"))));
											map.put("nopaypercent",StringUtils.formatDouble(map.get("nopayment"))/(StringUtils.formatDouble(map.get("fact"))+
													StringUtils.formatDouble(nMap.get("nopayment"))));
											map.put("paynopaypercent",StringUtils.formatDouble(nMap.get("payment"))/
													StringUtils.formatDouble(nMap.get("nopayment")));

											break;
										}
									}
								}
							}
						}
					}
					//如果追缴表没有查到时间段内的数据置已追缴和未缴为0
					else {
						for(Map<String, Object> map: orderlist){
							map.put("payment",0);
							map.put("nopayment",0);
							map.put("factpercent",100);
							map.put("nopaypercent",0);
							map.put("paynopaypercent",0);
						}
					}
				}
				if(orderlist != null && !orderlist.isEmpty()){
					count = orderlist.size();
				}
			}
			//setList(list);
			String json = JsonUtil.Map2Json(orderlist,pageNum,count, fieldsstr,"id");

			//json = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response, json);
		}

		return null;
	}



}
