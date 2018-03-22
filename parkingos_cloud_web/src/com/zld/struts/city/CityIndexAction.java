package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.facade.StatsAccountFacade;
import com.zld.impl.CommonMethods;
import com.zld.pojo.StatsAccountClass;
import com.zld.pojo.StatsFacadeResp;
import com.zld.pojo.StatsReq;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.ExecutorsUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class CityIndexAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private StatsAccountFacade accountFacade;

	Logger logger = Logger.getLogger(CityIndexAction.class);
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
			setResponse(request, cityid);
			return mapping.findForward("list");
		}else if(action.equals("alert")){
			Long ntime = System.currentTimeMillis()/1000;
			Integer draw = RequestUtil.getInteger(request, "draw", 0);
			List<Map<String, Object>> list = pgOnlyReadService.getAll("select title,create_time,state from city_peakalert_tb where " +
					"cityid=? and create_time>? order by create_time desc ", new Object[]{cityid, ntime - 7*24*60});
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					if(map.get("create_time") != null){
						Long create_time = (Long)map.get("create_time");
						map.put("ctime", TimeTools.getTime_yyyyMMdd_HHmmss(create_time*1000));
					}
				}
			}
			String ret = "{\"recordsTotal\":\""+list.size()+"\",\"draw\":\""+draw+"\",\"recordsFiltered\":\""+list.size()+"\",\"data\":[]}";

			String jsonData =StringUtils.createJson(list);
			ret = ret.replace("[]", jsonData);
			AjaxUtil.ajaxOutput(response, ret);
		}else if(action.equals("online")){
			String json = anlyOnline(request, cityid);
			AjaxUtil.ajaxOutput(response, json);
		}
		return null;
	}

	private void setResponse(HttpServletRequest request, Long cityid){
		try {
			ExecutorService pool = ExecutorsUtil.getExecutorService();
			ExeCallable callable0 = new ExeCallable(request, cityid, 0);
			ExeCallable callable1 = new ExeCallable(request, cityid, 1);
			ExeCallable callable2 = new ExeCallable(request, cityid, 2);
			ExeCallable callable3 = new ExeCallable(request, cityid, 3);
			ExeCallable callable4 = new ExeCallable(request, cityid, 4);
			ExeCallable callable5 = new ExeCallable(request, cityid, 5);
			ExeCallable callable6 = new ExeCallable(request, cityid, 6);
			ExeCallable callable7 = new ExeCallable(request, cityid, 7);
			ExeCallable callable8 = new ExeCallable(request, cityid, 8);

			Future<Object> future0 = pool.submit(callable0);
			Future<Object> future1 = pool.submit(callable1);
			Future<Object> future2 = pool.submit(callable2);
			Future<Object> future3 = pool.submit(callable3);
			Future<Object> future4 = pool.submit(callable4);
			Future<Object> future5 = pool.submit(callable5);
			Future<Object> future6 = pool.submit(callable6);
			Future<Object> future7 = pool.submit(callable7);
			Future<Object> future8 = pool.submit(callable8);

			future0.get();
			future1.get();
			future2.get();
			future3.get();
			future4.get();
			future5.get();
			future6.get();
			future7.get();
			future8.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getAlert(HttpServletRequest request, Long cityid){
		try {
			String countSql = "select count(*) from com_alert_tb where cityid=? and state=? " ;
			Long count = pgOnlyReadService.getLong(countSql, new Object[]{cityid, 0});
			request.setAttribute("alertcount", count);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void getIncome(HttpServletRequest request, Long cityid){
		try {
			Long curTime = System.currentTimeMillis()/1000;
			Long startTime = TimeTools.getToDayBeginTime();
			List<Object> idList = null;
			if(cityid > 0){
				idList = commonMethods.getcollctors(cityid);
			}
			Map<String, Object> map = setMoney(idList, startTime, curTime);
			request.setAttribute("income", map.get("income"));
			request.setAttribute("income_struct", StringUtils.createJson(map));
		} catch (Exception e) {
			logger.error("getMoney", e);
		}
	}

	private Map<String,Object> setMoney(List<Object> idList, long startTime, long endTime){
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			double cashTotalFee = 0;
			double epayTotalFee = 0;
			double cardTotalFee = 0;
			double totalFee = 0;
			if(idList != null && !idList.isEmpty()){
				StatsReq req = new StatsReq();
				req.setIdList(idList);
				req.setStartTime(startTime);
				req.setEndTime(endTime);
				StatsFacadeResp resp = accountFacade.statsParkUserAccount(req);
				if(resp.getResult() == 1){
					List<StatsAccountClass> classes = resp.getClasses();
					if(classes != null && !classes.isEmpty()){
						for(StatsAccountClass accountClass : classes){
							long id = accountClass.getId();
							double cashParkingFee = accountClass.getCashParkingFee();
							double cashPrepayFee = accountClass.getCashPrepayFee();
							double cashRefundFee = accountClass.getCashRefundFee();
							double cashAddFee = accountClass.getCashAddFee();
							double cashPursueFee = accountClass.getCashPursueFee();

							double ePayParkingFee = accountClass.getePayParkingFee();
							double ePayPrepayFee = accountClass.getePayPrepayFee();
							double ePayRefundFee = accountClass.getePayRefundFee();
							double ePayAddFee = accountClass.getePayAddFee();
							double ePayPursueFee = accountClass.getePayPursueFee();

							double cardParkingFee = accountClass.getCardParkingFee();
							double cardPrepayFee = accountClass.getCardPrepayFee();
							double cardRefundFee = accountClass.getCardRefundFee();
							double cardAddFee = accountClass.getCardAddFee();
							double cardPursueFee = accountClass.getCardPursueFee();

							cashTotalFee += StringUtils.formatDouble(cashParkingFee + cashPrepayFee +
									cashAddFee + cashPursueFee - cashRefundFee);
							epayTotalFee += StringUtils.formatDouble(ePayParkingFee + ePayPrepayFee +
									ePayAddFee + ePayPursueFee - ePayRefundFee);
							cardTotalFee += StringUtils.formatDouble(cardParkingFee + cardPrepayFee +
									cardAddFee + cardPursueFee - cardRefundFee);
						}
					}
				}
			}
			totalFee += StringUtils.formatDouble(cashTotalFee + epayTotalFee + cardTotalFee);
			map.put("cash", cashTotalFee);
			map.put("epay", epayTotalFee);
			map.put("card", cardTotalFee);
			map.put("income", totalFee);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void getRedirAuth(HttpServletRequest request, Long cityid){
		List<Map<String, Object>> authList = (List<Map<String, Object>>)request.getSession().getAttribute("authlist");
		if(authList != null){
			for(Map<String, Object> map : authList){
				if(map.get("url") != null){
					String url = (String)map.get("url");
					if(url.contains("citymember.do")){
						request.setAttribute("con_authid", map.get("auth_id"));
					}
					if(url.contains("citypeakalert.do")){
						request.setAttribute("alert_authid", map.get("auth_id"));
					}
					if(url.contains("feebypark.do")){
						request.setAttribute("order_authid", map.get("auth_id"));
					}
					if(url.contains("citysensor.do")){
						request.setAttribute("sensor_authid", map.get("auth_id"));
					}
					if(url.contains("citytransmitter.do")){
						request.setAttribute("site_authid", map.get("auth_id"));
					}
					if(url.contains("cityinduce.do")){
						request.setAttribute("induce_authid", map.get("auth_id"));
					}
				}
			}
		}
	}

	private void getVIPInfo(HttpServletRequest request, Long cityid){
		Long todaybeigintime = TimeTools.getToDayBeginTime();
		Long todayNewVip = pgOnlyReadService.getLong("select count(id) from user_info_tb where auth_flag=? and cityid=? and reg_time>? ",
				new Object[]{4, cityid, todaybeigintime});
		Long allVip = pgOnlyReadService.getLong("select count(id) from user_info_tb where auth_flag=? and cityid=? ",
				new Object[]{4, cityid});
		request.setAttribute("newvip", todayNewVip);
		request.setAttribute("allvip", allVip);
	}

	private void getDeviceInfo(HttpServletRequest request, Long cityid){
		Long ntime = System.currentTimeMillis()/1000;
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
			List<Object> params0 = new ArrayList<Object>();
			params0.addAll(parks);
			params0.add(0);
			//************************车检器总数量和故障数***************************//
			Long allSensor = pgOnlyReadService.getCount("select count(id) from dici_tb where comid in " +
					" ("+preParams+") and is_delete=? ", params0);//获取所有的车检器数量
			List<Object> params1 = new ArrayList<Object>();
			params1.addAll(parks);
			params1.add(ntime);
			params1.add(30 * 60);
			params1.add(0);
			Long failSensor = pgOnlyReadService.getCount("select count(id) from dici_tb where comid in " +
					" ("+preParams+") and (?-beart_time>? or beart_time is null) and is_delete=? ", params1);//获取你故障的车检器数量
			//************************摄像头总数量和故障数***************************//
			Long allCamera = pgOnlyReadService.getCount("select count(id) from com_camera_tb where comid in " +
					" ("+preParams+")", parks);//获取所有的车检器数量
			List<Object> params2 = new ArrayList<Object>();
			params2.addAll(parks);
			params2.add(0);
			Long failCamera = pgOnlyReadService.getCount("select count(id) from com_camera_tb where comid in " +
					" ("+preParams+") and state=? ", params2);//获取所有的车检器数量
			//************************基站总数量和故障数***************************//
			List<Object> params3 = new ArrayList<Object>();
			params3.addAll(parks);
			params3.add(0);
			Long allSite = pgOnlyReadService.getCount("select count(id) from sites_tb where comid in " +
					" ("+preParams+") and is_delete=? ", params3);//获取所有的车检器数量
			List<Object> params4 = new ArrayList<Object>();
			params4.addAll(parks);
			params4.add(0);
			params4.add(ntime);
			params4.add(30 * 60);
			Long failSite = pgOnlyReadService.getCount("select count(id) from sites_tb where comid in " +
					" ("+preParams+") and is_delete=? and (?-heartbeat>? or heartbeat is null) ", params4);//获取你故障的车检器数量
			//************************诱导屏总数量和故障数***************************//
			List<Object> params5 = new ArrayList<Object>();
			params5.add(cityid);
			params5.add(0);
			Long allInduce = pgOnlyReadService.getCount("select count(id) from induce_tb where cityid=? " +
					" and is_delete=? ", params5);//获取所有的车检器数量
			List<Object> params6 = new ArrayList<Object>();
			params6.add(cityid);
			params6.add(0);
			params6.add(ntime);
			params6.add(30 * 60);
			Long failInduce = pgOnlyReadService.getCount("select count(id) from induce_tb where cityid=? " +
					" and is_delete=? and (?-heartbeat_time>? or heartbeat_time is null) ", params6);//获取你故障的车检器数量

			if(allSensor == 0) allSensor = 1L;
			if(allCamera == 0) allCamera = 1L;
			if(allSite == 0) allSite = 1L;
			if(allInduce == 0) allInduce = 1L;
			double sd = (1 - (double)failSensor/allSensor) * 100;
			DecimalFormat formater = new DecimalFormat("#0.##");
			request.setAttribute("sensor_rate", formater.format((1 - (double)failSensor/allSensor) * 100));
			request.setAttribute("camera_rate", formater.format((1 - (double)failCamera/allCamera) * 100));
			request.setAttribute("site_rate", formater.format((1 - (double)failSite/allSite) * 100));
			request.setAttribute("induce_rate", formater.format((1 - (double)failInduce/allInduce) * 100));
			request.setAttribute("failSensor", failSensor);
			request.setAttribute("failCamera", failCamera);
			request.setAttribute("failSite", failSite);
			request.setAttribute("failInduce", failInduce);
			request.setAttribute("fail_device", failCamera + failInduce + failSensor + failSite);
		}
	}

	private void getUtilization(HttpServletRequest request, Long cityid){
		try {
			Long asum = 0L;
			Long usum = 0L;
			Map<String, Object> map = commonMethods.getBerthCount(-1L, cityid);
			if(map != null){
				if(map.get("asum") != null){
					asum = (Long)map.get("asum");
				}
				if(map.get("usum") != null){
					usum = (Long)map.get("usum");
				}
			}
			if(usum > asum){
				usum = asum;
			}
			if(asum == 0) asum = 1L;
			request.setAttribute("berth_rate", Math.round(StringUtils.formatDouble(usum*100/asum)));
		} catch (Exception e) {
			logger.error("getUtilization", e);
		}
	}

	private String anlyOnline(HttpServletRequest request, Long cityid){
		try {
			Long ntime = System.currentTimeMillis()/1000;
			if(cityid != null && cityid > 0){
				String sql = "select sum(collector_online) as collector_online,sum(inspector_online) as inspector_online,create_time" +
						" from online_anlysis_tb where (cityid=? ";
				List<Object> params = new ArrayList<Object>();
				params.add(cityid);
				List<Object> groups = new ArrayList<Object>();
				groups = commonMethods.getGroups(cityid);
				if(groups != null && !groups.isEmpty()){
					String preParms = "";
					for(Object o : groups){
						if(preParms.equals(""))
							preParms = "?";
						else
							preParms +=",?";
					}

					sql += " or groupid in ("+preParms+") ";
					params.addAll(groups);
				}
				List<Object> parks = new ArrayList<Object>();
				parks = commonMethods.getparks(cityid);
				if(parks != null && !parks.isEmpty()){
					String preParms = "";
					for(Object o : parks){
						if(preParms.equals(""))
							preParms = "?";
						else
							preParms +=",?";
					}

					sql += " or comid in ("+preParms+") ";
					params.addAll(parks);
				}
				params.add(ntime - 24*60*60);
				sql += ") and create_time>? group by create_time order by create_time ";
				List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql, params);
				if(list != null && !list.isEmpty()){
					for(Map<String, Object> map : list){
						Long create_time = (Long)map.get("create_time");
						map.put("time",TimeTools.getTime_MMdd_HHmm(create_time*1000));
					}
				}

				String json = StringUtils.createJson(list);
				return json;
			}
		} catch (Exception e) {
			logger.error("anlyOnline", e);
		}
		return "[]";
	}
	//************************POS机订单占比***************************//
	private void  posPercent(HttpServletRequest request, Long cityid){
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
		String nowtime= df2.format(System.currentTimeMillis());
		Long b =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(nowtime+" 00:00:00");
		Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(nowtime+" 23:59:59");
		String  posSql="select count(id) from  order_tb where end_time between ? and ?";
		String  SitesSql="select count(id) from  berth_order_tb where  out_uid > ? and  out_time between ? and ?   ";
		List<Object> paramsPos = new ArrayList<Object>();
		List<Object> paramsSites = new ArrayList<Object>();
		Long countPos = 0L;
		Long countSites = 0L;
		paramsPos.add(b);
		paramsPos.add(e);
		paramsSites.add(0);
		paramsSites.add(b);
		paramsSites.add(e);

		List<Object> parks = new ArrayList<Object>();
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
			posSql += " and comid in ("+preParams+") ";
			SitesSql += " and comid in ("+preParams+") ";
			paramsPos.addAll(parks);
			paramsSites.addAll(parks);

		}
		countPos= pgOnlyReadService.getCount(posSql, paramsPos);
		countSites=pgOnlyReadService.getCount(SitesSql, paramsSites);
		double percent=StringUtils.formatDouble(((double)countPos/countSites) * 100);
		request.setAttribute("percent", percent);
	}
	//************************泊位周转率***************************//
	private void  parkTurn(HttpServletRequest request, Long cityid){
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
		String nowtime= df2.format(System.currentTimeMillis());
		Long b =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(nowtime+" 00:00:00");
		Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(nowtime+" 23:59:59");
		String  posSql="select count(id) from  order_tb where end_time between ? and ?  ";
		String  SitesSql="select count(id) from  com_park_tb where is_delete=? and  ";
		List<Object> paramsPos = new ArrayList<Object>();
		List<Object> paramsSites = new ArrayList<Object>();
		Long countPos = 0L;
		Long countSites = 0L;
		paramsPos.add(b);
		paramsPos.add(e);
		paramsSites.add(0);
		List<Object> parks = new ArrayList<Object>();
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
			posSql += " and comid in ("+preParams+") ";
			SitesSql += " comid in ("+preParams+") ";
			paramsPos.addAll(parks);
			paramsSites.addAll(parks);

		}
		countPos = pgOnlyReadService.getCount(posSql, paramsPos);
		countSites = pgOnlyReadService.getCount(SitesSql, paramsSites);
		double parkturn = StringUtils.formatDouble((double)countPos/countSites);
		request.setAttribute("parkturn", parkturn);
	}
	//************************追缴率***************************//
	private void  recoveryrate(HttpServletRequest request, Long cityid){
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
		String nowtime= df2.format(System.currentTimeMillis());
		Long b =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(nowtime+" 00:00:00");
		Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(nowtime+" 23:59:59");
		String  posSql="select count(id) from  no_payment_tb where end_time between ? and ?  ";
		String  SitesSql="select count(id) from  no_payment_tb where end_time  between ? and ? and state=? and  ";
		List<Object> paramsPos = new ArrayList<Object>();
		List<Object> paramsSites = new ArrayList<Object>();
		Long countPos = 0L;
		Long countSites = 0L;
		paramsPos.add(b);
		paramsPos.add(e);
		paramsSites.add(b);
		paramsSites.add(e);
		paramsSites.add(1);
		List<Object> parks = new ArrayList<Object>();
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
			posSql += " and comid in ("+preParams+") ";
			SitesSql += " comid in ("+preParams+") ";
			paramsPos.addAll(parks);
			paramsSites.addAll(parks);

		}
		countPos= pgOnlyReadService.getCount(posSql, paramsPos);
		countSites=pgOnlyReadService.getCount(SitesSql, paramsSites);
		double eacaperate=StringUtils.formatDouble(((double)countSites/countPos) * 100);
		request.setAttribute("eacaperate", eacaperate);
	}

	class ExeCallable implements Callable<Object>{
		private HttpServletRequest request;
		private Long cityid = -1L;
		private int type;
		ExeCallable(HttpServletRequest request, Long cityid, int type){
			this.request = request;
			this.cityid = cityid;
			this.type = type;
		}
		@Override
		public Object call() throws Exception {
			Object result = null;
			try {
				switch (type) {
					case 0:
						getRedirAuth(request, cityid);
						break;
					case 1:
						getVIPInfo(request, cityid);//会员信息
						break;
					case 2:
						getDeviceInfo(request, cityid);//设备正常率
						break;
					case 3:
						getUtilization(request,cityid);//利用率
						break;
					case 4:
						getIncome(request, cityid);//今日收入
						break;
					case 5:
						getAlert(request, cityid);//告警事件
						break;
					case 6:
						posPercent(request,cityid);//pos机订单占比
						break;
					case 7:
						parkTurn(request, cityid);//泊位周转率
						break;
					case 8:
						recoveryrate(request, cityid);//追缴率
						break;
					default:
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}

	}

}
