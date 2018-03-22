package com.zld.struts.anlysis;

import com.zld.AjaxUtil;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
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

public class CardAnlysisAction extends Action {
	@Autowired
	private PgOnlyReadService readService;
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(groupid == null){
			return null;
		}
		if(groupid == null)
			groupid = -1L;
		if(action.equals("")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Long today = TimeTools.getToDayBeginTime();
			request.setAttribute("btime", df2.format(today * 1000 - 24 * 60 * 60 * 1000));
			request.setAttribute("etime",  df2.format(today * 1000 -1));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime) + 24 * 60 *60;
			Long e = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime) + 24 * 60 *60;
			Map<String, Object> slotMap = readService.getMap("select sum(slot_charge) slot_charge," +
					" sum(slot_consume) slot_consume,sum(slot_refund_count) slot_refund_count," +
					"sum(slot_refund_balance) slot_refund_balance,sum(slot_act_count) slot_act_count," +
					"sum(slot_act_balance) slot_act_balance from card_anlysis_tb where create_time " +
					" between ? and ? and groupid=? ", new Object[]{b, e, groupid});
			Map<String, Object> firstMap = readService.getMap("select all_count,all_balance from card_anlysis_tb" +
							" where create_time=? and groupid=? limit ?",
					new Object[]{b - 24 * 60 *60, groupid, 1});
			Map<String, Object> lastMap = readService.getMap("select all_count,all_balance from card_anlysis_tb" +
							" where create_time=? and groupid=? limit ?",
					new Object[]{e - 24 * 60 *60 + 1, groupid, 1});
			if(slotMap != null){
				slotMap.put("groupid", groupid);
				if(firstMap != null){
					slotMap.put("b_all_count", firstMap.get("all_count"));
					slotMap.put("b_all_balance", firstMap.get("all_balance"));
				}
				if(lastMap != null){
					slotMap.put("e_all_count", lastMap.get("all_count"));
					slotMap.put("e_all_balance", lastMap.get("all_balance"));
				}
			}
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			list.add(slotMap);
			String json = JsonUtil.Map2Json(list, 1, 1, fieldsstr, "groupid");
			AjaxUtil.ajaxOutput(response, json);
		}
		return null;
	}
}
