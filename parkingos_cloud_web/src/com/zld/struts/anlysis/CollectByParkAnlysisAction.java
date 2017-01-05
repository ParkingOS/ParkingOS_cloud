package com.zld.struts.anlysis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.AjaxUtil;
import com.zld.facade.StatsAccountFacade;
import com.zld.impl.CommonMethods;
import com.zld.pojo.StatsAccountClass;
import com.zld.pojo.StatsFacadeResp;
import com.zld.pojo.StatsReq;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;

public class CollectByParkAnlysisAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private StatsAccountFacade accountFacade;
	
	private Logger logger = Logger.getLogger(CollectByParkAnlysisAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//µÇÂ¼µÄÓÃ»§id
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		request.setAttribute("authid", request.getParameter("authid"));
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
			commonMethods.setIndexAuthId(request);
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Long today = TimeTools.getToDayBeginTime();
			request.setAttribute("btime", df2.format(today * 1000));
			request.setAttribute("etime",  df2.format(today * 1000 + 24 * 60 * 60 * 1000 -1));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime);
			Long e = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime);
			if(groupid == -1){
				groupid= RequestUtil.getLong(request, "groupid", -1L);
			}
			SqlInfo sqlInfo = RequestUtil.customSearch(request, "com_info");
			List<Object> params = new ArrayList<Object>();
			params.add(1);
			String sql = "select id,company_name from com_info_tb where state<>? ";
			String countSql = "select count(id) from com_info_tb where state<>? ";
			List<Map<String, Object>> list = null;
			Long count = 0L;
			if((e - b) < 7 * 24 * 60 * 60 && e > b){
				List<Object> parks = null;
				if(groupid > 0){
					parks = commonMethods.getParks(groupid);
				}else if(cityid > 0){
					parks = commonMethods.getparks(cityid);
				}
				if(parks != null && !parks.isEmpty()){
					String preParams = "";
					for(Object object : parks){
						if(preParams.equals(""))
							preParams ="?";
						else
							preParams += ",?";
					}
					sql += " and id in ("+preParams+") ";
					countSql += " and id in ("+preParams+") ";
					params.addAll(parks);
					
					if(sqlInfo != null) {
						countSql += " and "+ sqlInfo.getSql();
						sql += " and "+sqlInfo.getSql();
						params.addAll(sqlInfo.getParams());
					}
					
					count = pgOnlyReadService.getCount(countSql, params);
					if(count > 0){
						list = pgOnlyReadService.getAll(sql, params, pageNum, pageSize);
						setList(list, b, e);
					}
				}
				String json = JsonUtil.Map2Json(list, pageNum, count, fieldsstr, "id");
				AjaxUtil.ajaxOutput(response, json);
			}
		}
		return null;
	}
	
	private void setList(List<Map<String, Object>> userList, Long startTime, Long endTime){
		try {
			if(userList != null && !userList.isEmpty()){
				List<Object> idList = new ArrayList<Object>();
				for(Map<String, Object> map : userList){
					idList.add(map.get("id"));
				}
				StatsReq req = new StatsReq();
				req.setIdList(idList);
				req.setStartTime(startTime);
				req.setEndTime(endTime);
				StatsFacadeResp resp = accountFacade.statsParkAccount(req);
				if(resp.getResult() == 1){
					List<StatsAccountClass> classes = resp.getClasses();
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
						
						double escapeFee = accountClass.getEscapeFee();
						double sensorOrderFee = accountClass.getSensorOrderFee();
						
						double cashCustomFee = StringUtils.formatDouble(cashParkingFee + cashPrepayFee + cashAddFee - cashRefundFee);
						double epayCustomFee = StringUtils.formatDouble(ePayParkingFee + ePayPrepayFee + ePayAddFee - ePayRefundFee);
						double cardCustomFee = StringUtils.formatDouble(cardParkingFee + cardPrepayFee + cardAddFee - cardRefundFee);
						double cashTotalFee = StringUtils.formatDouble(cashPursueFee + cashCustomFee);
						double ePayTotalFee = StringUtils.formatDouble(ePayPursueFee + epayCustomFee);
						double cardTotalFee = StringUtils.formatDouble(cardPursueFee + cardCustomFee);
						double totalFee = StringUtils.formatDouble(cashTotalFee + ePayTotalFee + cardTotalFee);
						double allTotalFee = StringUtils.formatDouble(totalFee + escapeFee);
						double totalPursueFee = StringUtils.formatDouble(cashPursueFee + ePayPursueFee + cardPursueFee);
						
						
						for(Map<String, Object> infoMap : userList){
							Long userId = (Long)infoMap.get("id");
							if(id == userId.intValue()){
								infoMap.put("cashPursueFee", cashPursueFee);
								infoMap.put("cashCustomFee", cashCustomFee);
								infoMap.put("cashTotalFee", cashTotalFee);
								infoMap.put("ePayPursueFee", ePayPursueFee);
								infoMap.put("ePayCustomFee", epayCustomFee);
								infoMap.put("ePayTotalFee", ePayTotalFee);
								infoMap.put("cardPursueFee", cardPursueFee);
								infoMap.put("cardCustomFee", cardCustomFee);
								infoMap.put("cardTotalFee", cardTotalFee);
								infoMap.put("totalFee", totalFee);
								infoMap.put("escapeFee", escapeFee);
								infoMap.put("allTotalFee", allTotalFee);
								infoMap.put("totalPursueFee", totalPursueFee);
								break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
