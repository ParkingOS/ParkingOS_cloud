package com.zld.struts.controller;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test extends Action {
	@Autowired
	private DataBaseService dataBaseService;
	
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("content")){
			request.setAttribute("name", "whx");
			return mapping.findForward("content");
		}else if(action.equals("getorgtype")){
			//https://segmentfault.com/a/1190000004478726
			Integer start = RequestUtil.getInteger(request, "start", 1);
			Integer length = RequestUtil.getInteger(request, "length", 20);
			String name = RequestUtil.processParams(request, "name");
			Integer draw = RequestUtil.getInteger(request, "draw", 0);
			Integer pageNum = start/length + 1;
			start++;
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			List<Object> params = new ArrayList<Object>();
			params.add(4);
			Long count = dataBaseService.getCount("select count(id) from user_info_tb where auth_flag<? ", params);
			list = dataBaseService.getAll("select * from user_info_tb where auth_flag<? ", params, pageNum, length);
			
			String ret = "{\"recordsTotal\":\""+count+"\",\"draw\":\""+draw+"\",\"recordsFiltered\":\""+count+"\",\"data\":[]}";
			
			String tickets =StringUtils.createJson(list);
			ret = ret.replace("[]", tickets);
			AjaxUtil.ajaxOutput(response, ret);
		}else if(action.equals("ajaxtable")){
			return mapping.findForward("ajaxtable");
		}else if(action.equals("scrollertable")){
			return mapping.findForward("scroller");
		}else if(action.equals("alert")){
			Integer draw = RequestUtil.getInteger(request, "draw", 0);
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			List<Object> params = new ArrayList<Object>();
			params.add(4);
			params.add(50);
			list = dataBaseService.getAllMap("select * from user_info_tb where auth_flag<? limit ? ", params);
			
			String ret = "{\"recordsTotal\":\""+list.size()+"\",\"draw\":\""+draw+"\",\"recordsFiltered\":\""+list.size()+"\",\"data\":[]}";
			
			String tickets =StringUtils.createJson(list);
			ret = ret.replace("[]", tickets);
			AjaxUtil.ajaxOutput(response, ret);
		}
		return null;
	}
}
