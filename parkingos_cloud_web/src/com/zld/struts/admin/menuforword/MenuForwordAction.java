package com.zld.struts.admin.menuforword;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * 电子支付菜单
 * @author Administrator
 *
 */
public class MenuForwordAction extends Action{



	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String uri = request.getRequestURI();
		request.setAttribute("authid", request.getParameter("authid"));
		System.err.println(">>>>>>>>>>>>>>>>>>>>"+uri);
		if(uri.indexOf("devicemenu")!=-1){
			request.setAttribute("menuname","设备管理");
		}else if(uri.indexOf("syssetmenu")!=-1){
			request.setAttribute("menuname","综合设置");
		}else if(uri.indexOf("authmenu")!=-1){
			request.setAttribute("menuname","权限管理");
		}else if(uri.indexOf("anlysismenu")!=-1){
			request.setAttribute("menuname","统计分析");
		}else if(uri.indexOf("parkepaymenu")!=-1){
			request.setAttribute("menuname","电子支付");
		}else if(uri.indexOf("parkordermenu")!=-1){
			request.setAttribute("menuname","订单管理");
		}else if(uri.indexOf("parkmanagemenu")!=-1){
			request.setAttribute("menuname","系统管理");
		}else if(uri.indexOf("membermanage")!=-1){
			request.setAttribute("menuname","员工权限");
		}else if(uri.indexOf("carplate")!=-1){
			request.setAttribute("menuname","设备管理");
		}else if(uri.indexOf("parkanlysis")!=-1){
			request.setAttribute("menuname","统计分析");
		}else if(uri.indexOf("logomanage")!=-1){
			request.setAttribute("menuname","系统管理");
		}else if(uri.indexOf("sysmanage") != -1){
			request.setAttribute("menuname","系统管理");
		}else if(uri.indexOf("vipmanage") != -1){
			request.setAttribute("menuname","会员管理");
		}else if(uri.indexOf("citycommand") != -1){
			request.setAttribute("menuname","指挥中心");
		}else if(uri.indexOf("inducemenu") != -1){
			request.setAttribute("menuname","诱导管理");
		}else if(uri.indexOf("paymenu") != -1){
			request.setAttribute("menuname","支付管理");
		}else if(uri.indexOf("cityanlysis") != -1){
			request.setAttribute("menuname","决策分析");
		}
		return mapping.findForward("menu");
	}


}