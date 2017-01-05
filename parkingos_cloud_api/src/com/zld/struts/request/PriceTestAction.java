package com.zld.struts.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;

public class PriceTestAction extends Action {
	@Autowired
	private DataBaseService writeService;
	@Autowired
	private PgOnlyReadService readService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private PublicMethods publicMethods;
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception{
		String action = RequestUtil.processParams(request, "action");
		if(action.equals("getcartypes")){
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> retList = commonMethods.getCarType(comid);
			HashMap<String, Object> hashMap = new HashMap<String, Object>();
			hashMap.put("value_name","È«²¿");
			hashMap.put("value_no", -1);
			resultList.add(hashMap);
			resultList.addAll(retList);
			String result = StringUtils.getJson(resultList);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("oldprice")){
			return mapping.findForward("oldlist");
		}else if(action.equals("getoldfee")){
			Map<String, Object> orderMap = new HashMap<String, Object>();
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Long comid = RequestUtil.getLong(request, "comid",-1L);
			Long userid = RequestUtil.getLong(request, "userid", -1L);
			Integer vehicle_type = RequestUtil.getInteger(request, "vehicle_type", 0);
			
			Long start = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime);
			Long end = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime);
			
			orderMap.put("uin", userid);
			orderMap.put("create_time", start);
			orderMap.put("c_type", 3);
			orderMap.put("state", 0);
			orderMap.put("pid", -1);
			orderMap.put("car_type", vehicle_type);
			String result = publicMethods.getOrderPrice(comid, orderMap, end);
			JSONObject object = JSONObject.fromObject(result);
			AjaxUtil.ajaxOutput(response, object.get("collect") + "");
		}
		return null;
	}
}
