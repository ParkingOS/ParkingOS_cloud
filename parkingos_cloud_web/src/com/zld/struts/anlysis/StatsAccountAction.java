package com.zld.struts.anlysis;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.pojo.AccountReq;
import com.zld.pojo.AccountResp;
import com.zld.service.PgOnlyReadService;
import com.zld.service.StatsAccountService;
import com.zld.service.StatsCardService;
import com.zld.service.StatsOrderService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.TimeTools;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatsAccountAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	@Resource(name = "parkUserCash")
	private StatsAccountService parkUserCash;
	@Autowired
	@Resource(name = "parkUserEpay")
	private StatsAccountService parkUserEpayService;
	@Autowired
	@Resource(name = "parkEpay")
	private StatsAccountService parkEpayService;
	@Autowired
	@Resource(name = "groupEpay")
	private StatsAccountService groupEpayService;
	@Autowired
	@Resource(name = "tenantEpay")
	private StatsAccountService tenantEpayService;
	@Autowired
	@Resource(name = "card")
	private StatsCardService cardService;
	@Autowired
	@Resource(name = "escapeOrder")
	private StatsOrderService orderService;

	private Logger logger = Logger.getLogger(StatsAccountAction.class);

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
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
			Long statsid = RequestUtil.getLong(request, "statsid", -1L);
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Integer seltype = RequestUtil.getInteger(request, "seltype", -1);
			Integer from = RequestUtil.getInteger(request, "from", -1);
			request.setAttribute("statsid", statsid);
			request.setAttribute("btime", btime);
			request.setAttribute("etime", etime);
			request.setAttribute("seltype", seltype);
			request.setAttribute("from", from);
			if(from == 0){
				return mapping.findForward("parkUserCash");
			}else if(from == 1){
				request.setAttribute("treeurl", "getdata.do?action=epaysetting&statsid="
						+statsid+"&btime="+btime+"&etime="+etime+"&seltype="+seltype);
				request.setAttribute("title", "电子账目明细");
				return mapping.findForward("tree");
			}else if(from == 2){
				return mapping.findForward("parkUserEpay");
			}else if(from == 3){
				return mapping.findForward("parkEpay");
			}else if(from == 4){
				return mapping.findForward("groupEpay");
			}else if(from == 5){
				return mapping.findForward("tenantEpay");
			}else if(from == 6){
				return mapping.findForward("cardPay");
			}else if(from == 7){
				return mapping.findForward("escape");
			}else if(from == 8
					|| from == 9
					|| from == 10){
				return mapping.findForward("actCard");
			}
		}else if(action.equals("query")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Long statsid = RequestUtil.getLong(request, "statsid", -1L);
			Integer seltype = RequestUtil.getInteger(request, "seltype", -1);
			Integer from = RequestUtil.getInteger(request, "from", -1);
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			String nowtime= df2.format(System.currentTimeMillis());
			if(btime.equals(""))
				btime = nowtime + " 00:00:00";
			if(etime.equals(""))
				etime = nowtime + " 23:59:59";
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime);
			Long e = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime);

			AccountReq req = new AccountReq();
			req.setId(statsid);
			req.setStartTime(b);
			req.setEndTime(e);
			req.setPageNum(pageNum);
			req.setPageSize(pageSize);
			req.setType(seltype);
			List<Map<String, Object>> list = null;
			Long count = 0L;
			AccountResp resp = null;
			if(from == 0){//现金明细
				SqlInfo sqlInfo = RequestUtil.customSearch(request, "parkuser_cash_tb");
				req.setSqlInfo(sqlInfo);
				resp = parkUserCash.account(req);
			}else if(from == 2){//收费员电子明细
				SqlInfo sqlInfo = RequestUtil.customSearch(request, "parkuser_account_tb");
				req.setSqlInfo(sqlInfo);
				resp = parkUserEpayService.account(req);
			}else if(from == 3){//车场电子明细
				SqlInfo sqlInfo = RequestUtil.customSearch(request, "park_account_tb");
				req.setSqlInfo(sqlInfo);
				resp = parkEpayService.account(req);
			}else if(from == 4){//运营集团电子明细
				SqlInfo sqlInfo = RequestUtil.customSearch(request, "group_account_tb");
				req.setSqlInfo(sqlInfo);
				resp = groupEpayService.account(req);
			}else if(from == 5){//商户电子明细
				SqlInfo sqlInfo = RequestUtil.customSearch(request, "city_account_tb");
				req.setSqlInfo(sqlInfo);
				resp = tenantEpayService.account(req);
			}else if(from == 6){//卡片明细
				String baseSql = " (charge_type in (?) or consume_type in (?,?,?,?)) ";
				List<Object> baseParams = new ArrayList<Object>();
				baseParams.add(4);
				baseParams.add(0);
				baseParams.add(1);
				baseParams.add(2);
				baseParams.add(3);
				SqlInfo sqlInfo = RequestUtil.customSearch(request, "card_account_tb");
				if(sqlInfo != null){
					baseSql = baseSql + " and " + sqlInfo.getSql();
					baseParams.addAll(sqlInfo.getParams());
				}
				SqlInfo sqlInfo2 = new SqlInfo(baseSql, baseParams);
				req.setSqlInfo(sqlInfo2);
				resp = cardService.account(req);
			}else if(from == 7){//逃单
				SqlInfo sqlInfo = RequestUtil.customSearch(request, "no_payment_tb");
				req.setSqlInfo(sqlInfo);
				resp = orderService.order(req);
			}else if(from == 8){
				String baseSql = " type=? ";
				List<Object> baseParams = new ArrayList<Object>();
				baseParams.add(3);//激活卡片
				SqlInfo sqlInfo = RequestUtil.customSearch(request, "card_account_tb");
				if(sqlInfo != null){
					baseSql = baseSql + " and " + sqlInfo.getSql();
					baseParams.addAll(sqlInfo.getParams());
				}
				SqlInfo sqlInfo2 = new SqlInfo(baseSql, baseParams);
				req.setSqlInfo(sqlInfo2);
				resp = cardService.account(req);
			}else if(from == 9){
				String baseSql = " charge_type=? and type=? ";
				List<Object> baseParams = new ArrayList<Object>();
				baseParams.add(0);//现金充值
				baseParams.add(0);//充值
				SqlInfo sqlInfo = RequestUtil.customSearch(request, "card_account_tb");
				if(sqlInfo != null){
					baseSql = baseSql + " and " + sqlInfo.getSql();
					baseParams.addAll(sqlInfo.getParams());
				}
				SqlInfo sqlInfo2 = new SqlInfo(baseSql, baseParams);
				req.setSqlInfo(sqlInfo2);
				resp = cardService.account(req);
			}else if(from == 10){
				String baseSql = " type=? ";
				List<Object> baseParams = new ArrayList<Object>();
				baseParams.add(5);//注销卡片
				SqlInfo sqlInfo = RequestUtil.customSearch(request, "card_account_tb");
				if(sqlInfo != null){
					baseSql = baseSql + " and " + sqlInfo.getSql();
					baseParams.addAll(sqlInfo.getParams());
				}
				SqlInfo sqlInfo2 = new SqlInfo(baseSql, baseParams);
				req.setSqlInfo(sqlInfo2);
				resp = cardService.account(req);
			}
			if(resp != null && resp.getResult() == 1){
				list = resp.getList();
				count = resp.getCount();
				if(from == 8
						|| from == 9
						|| from == 10){
					setNfc(list);
				}
			}
			String json = JsonUtil.Map2Json(list, pageNum, count, fieldsstr, "id");
			AjaxUtil.ajaxOutput(response, json);
		}
		return null;
	}

	private void setNfc(List<Map<String, Object>> list){
		try {
			if(list != null && !list.isEmpty()){
				List<Object> idList = new ArrayList<Object>();
				String preParam = "";
				for(Map<String, Object> map : list){
					idList.add(map.get("card_id"));
					if(preParam.equals("")){
						preParam = "?";
					}else{
						preParam += ",?";
					}
				}
				List<Map<String, Object>> list2 = pgOnlyReadService.getAllMap("select id,nfc_uuid " +
						" from com_nfc_tb where id in ("+preParam+")", idList);
				if(list2 != null && !list2.isEmpty()){
					for(Map<String, Object> map : list2){
						Long id = (Long)map.get("id");
						for(Map<String, Object> map2 : list){
							Long cardId = (Long)map2.get("card_id");
							if(id.intValue() == cardId.intValue()){
								map2.put("nfc_uuid", map.get("nfc_uuid"));
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
