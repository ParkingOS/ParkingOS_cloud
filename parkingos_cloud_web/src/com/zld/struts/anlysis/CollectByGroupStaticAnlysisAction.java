package com.zld.struts.anlysis;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
import org.apache.log4j.Logger;
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

public class CollectByGroupStaticAnlysisAction extends Action{
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;

	private Logger logger = Logger.getLogger(CollectByParkStaticAnlysisAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		request.setAttribute("authid", request.getParameter("authid"));
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(cityid == null){
			return null;
		}
		if(cityid == null) cityid = -1L;
		if(action.equals("")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Long today = TimeTools.getToDayBeginTime();
			request.setAttribute("btime", df2.format(today * 1000 - 24 * 60 * 60 * 1000));
			request.setAttribute("etime",  df2.format(today * 1000 -1));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime) + 24 * 60 *60;
			Long e = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime) + 24 * 60 *60;
			SqlInfo sqlInfo = RequestUtil.customSearch(request, "org_group_tb");
			List<Object> params = new ArrayList<Object>();
			params.add(cityid);
			params.add(0);
			String sql = "select id,name from org_group_tb where cityid=? and state=? ";
			String countSql = "select count(id) from org_group_tb where cityid=? and state=? ";
			if(sqlInfo != null) {
				countSql += " and "+ sqlInfo.getSql();
				sql += " and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			List<Map<String, Object>> list = null;
			String res = "";
			Long count = pgOnlyReadService.getCount(countSql, params);
			if(count > 0){
				List<Map<String, Object>> titleList = pgOnlyReadService.getAllMap(sql, params);
				res = setTitle(titleList, b, e);
				list = pgOnlyReadService.getAll(sql, params, pageNum, pageSize);
				setList(list, b, e);
			}
			String json = JsonUtil.anlysisMap3Json(list, pageNum, count, fieldsstr,"id",res);
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("export")){
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime) + 24 * 60 *60;
			Long e = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime) + 24 * 60 *60;
			SqlInfo sqlInfo = RequestUtil.customSearch(request, "org_group_tb");
			List<Object> params = new ArrayList<Object>();
			params.add(cityid);
			params.add(0);
			String sql = "select id,name from org_group_tb where cityid=? and state=? ";
			if(sqlInfo != null) {
				sql += " and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql, params);
			if(list != null && !list.isEmpty()){
				setList(list, b, e);
				export(response, list, btime, etime);
			}
		}

		return null;
	}
	private void export(HttpServletResponse response, List<Map<String, Object>> list, String btime, String etime){
		try {
			if(list != null && !list.isEmpty()){
				String heards[] = new String[]{"运营集团编号","运营集团名称","普通订单","追缴订单","合计","普通订单","追缴订单","合计",
						"普通订单","追缴订单","合计","实收停车费","未缴停车费","应收停车费"};
				List<List<String>> bodyList = new ArrayList<List<String>>();
				for(Map<String, Object> map : list){
					List<String> valueList = new ArrayList<String>();
					valueList.add(map.get("id") + "");
					valueList.add(map.get("name") + "");
					valueList.add(map.get("cashCustomFee") + "");
					valueList.add(map.get("cashPursueFee") + "");
					valueList.add(map.get("cashTotalFee") + "");

					valueList.add(map.get("ePayCustomFee") + "");
					valueList.add(map.get("ePayPursueFee") + "");
					valueList.add(map.get("ePayTotalFee") + "");

					valueList.add(map.get("cardCustomFee") + "");
					valueList.add(map.get("cardPursueFee") + "");
					valueList.add(map.get("cardTotalFee") + "");

					valueList.add(map.get("totalFee") + "");
					valueList.add(map.get("escapeFee") + "");
					valueList.add(map.get("allTotalFee") + "");
					bodyList.add(valueList);
				}
				String fname = "运营集团收费报表" + btime + "至" + etime;
				java.io.OutputStream os = response.getOutputStream();
				response.reset();
				response.setHeader("Content-disposition", "attachment; filename="
						+ StringUtils.encodingFileName(fname) + ".xls");
				ExportExcelUtil importExcel = new ExportExcelUtil("运营集团收费报表",
						heards, bodyList);
				List<Map<String,String>> mulitHeadList = new ArrayList<Map<String,String>>();
				Map<String, String> mhead0 = new HashMap<String, String>();
				mhead0.put("length", "0");
				mhead0.put("content", "");
				mulitHeadList.add(mhead0);
				Map<String, String> mhead1 = new HashMap<String, String>();
				mhead1.put("length", "0");
				mhead1.put("content", "");
				mulitHeadList.add(mhead1);
				Map<String, String> mhead2 = new HashMap<String, String>();
				mhead2.put("length", "2");
				mhead2.put("content", "停车费-现金支付");
				mulitHeadList.add(mhead2);
				Map<String, String> mhead3 = new HashMap<String, String>();
				mhead3.put("length", "2");
				mhead3.put("content", "停车费-电子支付");
				mulitHeadList.add(mhead3);
				Map<String, String> mhead4 = new HashMap<String, String>();
				mhead4.put("length", "2");
				mhead4.put("content", "停车费-刷卡支付");
				mulitHeadList.add(mhead4);
				Map<String, String> mhead5 = new HashMap<String, String>();
				mhead5.put("length", "2");
				mhead5.put("content", "停车费-合计");
				mulitHeadList.add(mhead5);
				importExcel.mulitHeadList = mulitHeadList;
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

	private void setList(List<Map<String, Object>> list, Long startTime, Long endTime){
		try {
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> infoMap : list){
					infoMap.put("cashPursueFee", 0);
					infoMap.put("cashCustomFee", 0);
					infoMap.put("cashTotalFee", 0);
					infoMap.put("ePayPursueFee", 0);
					infoMap.put("ePayCustomFee", 0);
					infoMap.put("ePayTotalFee", 0);
					infoMap.put("cardPursueFee", 0);
					infoMap.put("cardCustomFee", 0);
					infoMap.put("cardTotalFee", 0);
					infoMap.put("totalFee", 0);
					infoMap.put("escapeFee", 0);
					infoMap.put("allTotalFee", 0);
					infoMap.put("cardChargeCashFee", 0);
					infoMap.put("cardReturnFee", 0);
					infoMap.put("cardActFee", 0);
					infoMap.put("totalPursueFee", 0);
				}

				List<Object> idList = new ArrayList<Object>();
				String preParams = "";
				for(Map<String, Object> map : list){
					idList.add(map.get("id"));
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				List<Map<String, Object>> rList = commonMethods.getIncomeAnly(idList, startTime, endTime, 3);
				if(rList != null && !rList.isEmpty()){
					for(Map<String, Object> infoMap : rList){
						Long uin = (Long)infoMap.get("id");
						Double prepay_cash = Double.valueOf(infoMap.get("prepay_cash") + "");//现金预支付
						Double add_cash = Double.valueOf(infoMap.get("add_cash") + "");//现金补缴
						Double refund_cash = Double.valueOf(infoMap.get("refund_cash") + "");//现金退款
						Double pursue_cash = Double.valueOf(infoMap.get("pursue_cash") + "");//现金追缴
						Double pfee_cash = Double.valueOf(infoMap.get("pfee_cash") + "");//现金停车费（非预付）

						Double prepay_epay = Double.valueOf(infoMap.get("prepay_epay") + "");//电子预支付
						Double add_epay = Double.valueOf(infoMap.get("add_epay") + "");//电子补缴
						Double refund_epay = Double.valueOf(infoMap.get("refund_epay") + "");//电子退款
						Double pursue_epay = Double.valueOf(infoMap.get("pursue_epay") + "");//电子追缴
						Double pfee_epay = Double.valueOf(infoMap.get("pfee_epay") + "");//电子停车费（非预付）
						Double escape = Double.valueOf(infoMap.get("escape") + "");//逃单未追缴的停车费

						Double prepay_card = Double.valueOf(infoMap.get("prepay_card") + "");//刷卡预支付
						Double add_card = Double.valueOf(infoMap.get("add_card") + "");//刷卡补缴
						Double refund_card = Double.valueOf(infoMap.get("refund_card") + "");//刷卡退款
						Double pursue_card = Double.valueOf(infoMap.get("pursue_card") + "");//刷卡追缴
						Double pfee_card = Double.valueOf(infoMap.get("pfee_card") + "");//刷卡停车费（非预付）

						Double charge_card_cash = Double.valueOf(infoMap.get("charge_card_cash") + "");//卡片现金充值金额
						Double return_card_fee = Double.valueOf(infoMap.get("return_card_fee") + "");//注销卡片退还的余额
						Double act_card_fee = Double.valueOf(infoMap.get("act_card_fee") + "");//激活卡片总面值（卖卡总面值）

						double cashCustomFee = StringUtils.formatDouble(pfee_cash + prepay_cash + add_cash - refund_cash);
						double epayCustomFee = StringUtils.formatDouble(pfee_epay + prepay_epay + add_epay - refund_epay);
						double cardCustomFee = StringUtils.formatDouble(pfee_card + prepay_card + add_card - refund_card);
						double cashTotalFee = StringUtils.formatDouble(pursue_cash + cashCustomFee);
						double ePayTotalFee = StringUtils.formatDouble(pursue_epay + epayCustomFee);
						double cardTotalFee = StringUtils.formatDouble(pursue_card + cardCustomFee);
						double totalFee = StringUtils.formatDouble(cashTotalFee + ePayTotalFee + cardTotalFee);
						double allTotalFee = StringUtils.formatDouble(totalFee + escape);
						double totalPursueFee = StringUtils.formatDouble(pursue_cash + pursue_epay + pursue_card);

						for(Map<String, Object> map : list){
							Long id = (Long)map.get("id");
							if(id.intValue() == uin.intValue()){
								map.put("cashPursueFee", pursue_cash);
								map.put("cashCustomFee", cashCustomFee);
								map.put("cashTotalFee", cashTotalFee);
								map.put("ePayPursueFee", pursue_epay);
								map.put("ePayCustomFee", epayCustomFee);
								map.put("ePayTotalFee", ePayTotalFee);
								map.put("cardPursueFee", pursue_card);
								map.put("cardCustomFee", cardCustomFee);
								map.put("cardTotalFee", cardTotalFee);
								map.put("totalFee", totalFee);
								map.put("escapeFee", escape);
								map.put("allTotalFee", allTotalFee);
								map.put("cardChargeCashFee", charge_card_cash);
								map.put("cardReturnFee", return_card_fee);
								map.put("cardActFee", act_card_fee);
								map.put("totalPursueFee", totalPursueFee);
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

	private String setTitle(List<Map<String, Object>> list, Long startTime, Long endTime){
		double cashTotalFee = 0;
		double ePayTotalFee = 0;
		double cardTotalFee = 0;
		double chargeCardFee = 0;//卡片充值
		double returnCardFee = 0;//退卡金额
		double actCardFee = 0;//激活面值
		double escapeFee = 0;//逃单未追缴的停车费
		double allFee = 0;//应收
		try {
			if(list != null && !list.isEmpty()){
				List<Object> collectors = new ArrayList<Object>();
				for(Map<String, Object> map : list){
					collectors.add(map.get("id"));
				}
				Map<String, Object> infoMap = commonMethods.sumIncomeAnly(collectors, startTime, endTime, 3);
				if(infoMap != null && !infoMap.isEmpty()){
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
					Double cardPrepayFee = 0d;//刷卡预支付
					Double cardAddFee = 0d;//刷卡补缴
					Double cardRefundFee = 0d;//刷卡退款
					Double cardPursueFee = 0d;//刷卡追缴
					Double cardParkingFee = 0d;//刷卡停车费（非预付）
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
					if(infoMap.get("escape") != null){
						escapeFee = Double.valueOf(infoMap.get("escape") + "");
					}

					cashTotalFee = StringUtils.formatDouble(cashParkingFee + cashPrepayFee +
							cashAddFee + cashPursueFee - cashRefundFee);
					ePayTotalFee = StringUtils.formatDouble(ePayParkingFee + ePayPrepayFee +
							ePayAddFee + ePayPursueFee - ePayRefundFee);
					cardTotalFee = StringUtils.formatDouble(cardParkingFee + cardPrepayFee +
							cardAddFee + cardPursueFee - cardRefundFee);
					allFee = StringUtils.formatDouble(cashTotalFee + ePayTotalFee +
							cardTotalFee + escapeFee);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String res = "停车费-->现金支付："+cashTotalFee+"元，电子支付："+ePayTotalFee+"元";
		/*String res = "停车费-->现金支付："+cashTotalFee+"元，电子支付："+ePayTotalFee+
				"元，卡片支付："+cardTotalFee+"元"+"，未缴："+escapeFee+"，元"+"，应收："+allFee+"元";*/
		return res;
	}
}
