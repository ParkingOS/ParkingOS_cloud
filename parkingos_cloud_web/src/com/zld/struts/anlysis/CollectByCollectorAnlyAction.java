package com.zld.struts.anlysis;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.zld.utils.ExportExcelUtil;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;

public class CollectByCollectorAnlyAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private StatsAccountFacade accountFacade;
	
	private Logger logger = Logger.getLogger(CollectByCollectorAnlyAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
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
			return mapping.findForward("list");
		}if(action.equals("query")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			Long b = TimeTools.getToDayBeginTime();
			Long e = b + 24 * 60 * 60;
			if(groupid == -1){//城市商户要求可按照运营集团筛查
				groupid = RequestUtil.getLong(request, "groupid", -1L);
			}
			SqlInfo sqlInfo = RequestUtil.customSearch(request, "user_info");
			List<Object> params = new ArrayList<Object>();
			String sql = "select id,nickname,resume from user_info_tb where ";
			String countSql = "select count(id) from user_info_tb where ";
			List<Object> collectors = null;
			if(cityid > 0){
				collectors = commonMethods.getcollctors(cityid);
			}else if(groupid > 0){
				collectors = commonMethods.getCollctors(groupid);
			}
			if(collectors != null && !collectors.isEmpty()){
				String preParams = "";
				for(Object object : collectors){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " id in ("+preParams+") ";
				countSql += " id in ("+preParams+") ";
				params.addAll(collectors);
			}
			if(sqlInfo != null) {
				countSql += " and "+ sqlInfo.getSql();
				sql += " and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			List<Map<String, Object>> list = null;
			Long count = pgOnlyReadService.getCount(countSql, params);
			if(count > 0){
				list = pgOnlyReadService.getAll(sql, params, pageNum, pageSize);
				setList(list, b, e);
			}
			String json = JsonUtil.Map2Json(list, pageNum, count, fieldsstr, "id");
			AjaxUtil.ajaxOutput(response, json);
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
				StatsFacadeResp resp = accountFacade.statsParkUserAccount(req);
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
						
						//卡片统计
						double cardChargeCashFee = accountClass.getCardChargeCashFee();//卡片充值金额
						double cardReturnFee = accountClass.getCardReturnFee();//退卡退还金额
						double cardActFee = accountClass.getCardActFee();//卖卡金额
						
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
								infoMap.put("cardChargeCashFee", cardChargeCashFee);
								infoMap.put("cardReturnFee", cardReturnFee);
								infoMap.put("cardActFee", cardActFee);
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
