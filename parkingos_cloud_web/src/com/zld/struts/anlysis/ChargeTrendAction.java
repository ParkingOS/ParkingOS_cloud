package com.zld.struts.anlysis;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
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

public class ChargeTrendAction extends Action {
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
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Long today = TimeTools.getToDayBeginTime();
			request.setAttribute("btime", df2.format(today * 1000 - 7 * 24 * 60 * 60 *1000));
			request.setAttribute("etime",  df2.format(today * 1000 -1));
			return mapping.findForward("trend");
		}else if(action.equals("querytrend")){
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime) + 24 * 60 *60;
			Long e = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime) + 24 * 60 *60;
			if(groupid == -1){
				groupid = RequestUtil.getLong(request, "groupid", -1L);
			}
			List<Map<String, Object>> list = null;
			List<Object> collectors = null;
			if(cityid > 0){
				collectors = commonMethods.getcollctors(cityid);
			}else if(groupid > 0){
				collectors = commonMethods.getCollctors(groupid);
			}
			if(collectors != null && !collectors.isEmpty()){
				list = setList(collectors, b, e);
			}
			String json = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("list")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Long today = TimeTools.getToDayBeginTime();
			request.setAttribute("btime", df2.format(today * 1000 - 7 * 24 * 60 * 60 *1000));
			request.setAttribute("etime",  df2.format(today * 1000 -1));
			return mapping.findForward("list");
		}else if(action.equals("querylist")){
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime) + 24 * 60 *60;
			Long e = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime) + 24 * 60 *60;
			List<Map<String, Object>> list = null;
			List<Object> collectors = null;
			if(cityid > 0){
				collectors = commonMethods.getcollctors(cityid);
			}else if(groupid > 0){
				collectors = commonMethods.getCollctors(groupid);
			}
			if(collectors != null && !collectors.isEmpty()){
				list = setList(collectors, b, e);
			}
			int count = 0;
			if(list != null){
				count = list.size();
			}
			String json = JsonUtil.Map2Json(list, pageNum, count, fieldsstr, "create_time");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("export")){
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime) + 24 * 60 *60;
			Long e = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime) + 24 * 60 *60;
			List<Map<String, Object>> list = null;
			List<Object> collectors = null;
			if(cityid > 0){
				collectors = commonMethods.getcollctors(cityid);
			}else if(groupid > 0){
				collectors = commonMethods.getCollctors(groupid);
			}
			if(collectors != null && !collectors.isEmpty()){
				list = setList(collectors, b, e);
				export(response, list, btime, etime);
			}
		}
		return null;
	}

	private void export(HttpServletResponse response, List<Map<String, Object>> list, String btime, String etime){
		try {
			if(list != null && !list.isEmpty()){
				String heards[] = new String[]{"日期","现金收费","电子收费","刷卡收费","总收费"};
				List<List<String>> bodyList = new ArrayList<List<String>>();
				for(Map<String, Object> map : list){
					List<String> valueList = new ArrayList<String>();
					valueList.add(map.get("time") + "");
					valueList.add(map.get("cashTotalFee") + "");
					valueList.add(map.get("ePayTotalFee") + "");
					valueList.add(map.get("cardTotalFee") + "");
					valueList.add(map.get("totalFee") + "");
					bodyList.add(valueList);
				}
				String fname = "停车费报表" + btime + "至" + etime;
				java.io.OutputStream os = response.getOutputStream();
				response.reset();
				response.setHeader("Content-disposition", "attachment; filename="
						+ StringUtils.encodingFileName(fname) + ".xls");
				ExportExcelUtil importExcel = new ExportExcelUtil("停车费报表",
						heards, bodyList);
				Map<String, String> headInfoMap=new HashMap<String, String>();
				headInfoMap.put("length", heards.length - 1 + "");
				headInfoMap.put("content", fname);
				importExcel.headInfo = headInfoMap;
				importExcel.createExcelFile(os);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<Map<String, Object>> setList(List<Object> collectors, Long startTime, Long endTime){
		try {
			if(collectors != null && !collectors.isEmpty()){
				List<Map<String, Object>> rList = new ArrayList<Map<String,Object>>();
				List<Map<String, Object>> list = commonMethods.getIncomeByTimeAnly(collectors, startTime, endTime, 0);
				if(list != null && !list.isEmpty()){
					for(Map<String, Object> infoMap : list){
						Long create_time = (Long)infoMap.get("create_time");
						Double cashPrepayFee = 0d;//现金预支付
						Double cashAddFee = 0d;//现金补缴
						Double cashRefundFee = 0d;//现金退款
						Double cashPursueFee = 0d;//现金追缴
						Double cashParkingFee = 0d;//现金停车费（非预付）
						Double ePayPrepayFee = 0d;//电子预支付
						Double ePayAddFee = 0d;//电子补缴
						Double ePayRefundFee = 0d;//电子退款
						Double ePayPursueFee = 0d;//电子追缴
						Double ePayParkingFee = 0d;//电子停车费（非预付）
						Double escapeFee = 0d;//逃单未追缴的停车费
						Double cardPrepayFee = 0d;//刷卡预支付
						Double cardAddFee = 0d;//刷卡补缴
						Double cardRefundFee = 0d;//刷卡退款
						Double cardPursueFee = 0d;//刷卡追缴
						Double cardParkingFee = 0d;//刷卡停车费（非预付）
						double cashTotalFee = 0;
						double ePayTotalFee = 0;
						double cardTotalFee = 0;
						double chargeCardFee = 0;//卡片充值
						double returnCardFee = 0;//退卡金额
						double actCardFee = 0;//激活面值
						if(infoMap.get("prepay_cash") != null){
							cashPrepayFee = Double.valueOf(infoMap.get("prepay_cash") + "");
						}
						if(infoMap.get("add_cash") != null){
							cashAddFee = Double.valueOf(infoMap.get("add_cash") + "");
						}
						if(infoMap.get("refund_cash") != null){
							cashRefundFee = Double.valueOf(infoMap.get("refund_cash") + "");
						}
						if(infoMap.get("pursue_cash") != null){
							cashPursueFee = Double.valueOf(infoMap.get("pursue_cash") + "");
						}
						if(infoMap.get("pfee_cash") != null){
							cashParkingFee = Double.valueOf(infoMap.get("pfee_cash") + "");
						}
						if(infoMap.get("prepay_epay") != null){
							ePayPrepayFee = Double.valueOf(infoMap.get("prepay_epay") + "");
						}
						if(infoMap.get("add_epay") != null){
							ePayAddFee = Double.valueOf(infoMap.get("add_epay") + "");
						}
						if(infoMap.get("refund_epay") != null){
							ePayRefundFee = Double.valueOf(infoMap.get("refund_epay") + "");
						}
						if(infoMap.get("pursue_epay") != null){
							ePayPursueFee = Double.valueOf(infoMap.get("pursue_epay") + "");
						}
						if(infoMap.get("pfee_epay") != null){
							ePayParkingFee = Double.valueOf(infoMap.get("pfee_epay") + "");
						}
						if(infoMap.get("prepay_card") != null){
							cardPrepayFee = Double.valueOf(infoMap.get("prepay_card") + "");
						}
						if(infoMap.get("add_card") != null){
							cardAddFee = Double.valueOf(infoMap.get("add_card") + "");
						}
						if(infoMap.get("refund_card") != null){
							cardRefundFee = Double.valueOf(infoMap.get("refund_card") + "");
						}
						if(infoMap.get("pursue_card") != null){
							cardPursueFee = Double.valueOf(infoMap.get("pursue_card") + "");
						}
						if(infoMap.get("pfee_card") != null){
							cardParkingFee = Double.valueOf(infoMap.get("pfee_card") + "");
						}
						if(infoMap.get("charge_card_cash") != null){
							chargeCardFee = Double.valueOf(infoMap.get("charge_card_cash") + "");
						}
						if(infoMap.get("return_card_fee") != null){
							returnCardFee = Double.valueOf(infoMap.get("return_card_fee") + "");
						}
						if(infoMap.get("act_card_fee") != null){
							actCardFee = Double.valueOf(infoMap.get("act_card_fee") + "");
						}

						cashTotalFee = StringUtils.formatDouble(cashParkingFee + cashPrepayFee +
								cashAddFee + cashPursueFee - cashRefundFee);
						ePayTotalFee = StringUtils.formatDouble(ePayParkingFee + ePayPrepayFee +
								ePayAddFee + ePayPursueFee - ePayRefundFee);
						cardTotalFee = StringUtils.formatDouble(cardParkingFee + cardPrepayFee +
								cardAddFee + cardPursueFee - cardRefundFee);

						Map<String, Object> map = new HashMap<String, Object>();
						map.put("create_time", create_time);
						map.put("time", TimeTools.getTimeStr_yyyy_MM_dd(create_time * 1000 - 24*60*60));
						map.put("cashTotalFee", cashTotalFee);
						map.put("ePayTotalFee", ePayTotalFee);
						map.put("cardTotalFee", cardTotalFee);
						map.put("totalFee", StringUtils.formatDouble(cashTotalFee + ePayTotalFee + cardTotalFee));
						rList.add(map);
					}
				}
				return rList;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
