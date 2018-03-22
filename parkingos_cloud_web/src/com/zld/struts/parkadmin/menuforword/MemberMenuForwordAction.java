package com.zld.struts.parkadmin.menuforword;

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
public class MemberMenuForwordAction extends Action{



	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setAttribute("authid", request.getParameter("authid"));
		return mapping.findForward("memberauthmenu");
	}


}