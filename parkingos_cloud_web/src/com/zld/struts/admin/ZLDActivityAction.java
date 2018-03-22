package com.zld.struts.admin;

import com.zld.impl.MemcacheUtils;
import com.zld.service.DataBaseService;
import com.zld.utils.RequestUtil;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ZLDActivityAction extends Action{

	@Autowired
	private DataBaseService daService;
	@Autowired
	private MemcacheUtils memcacheUtils;

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");

		if(action.equals("")){
			request.setAttribute("hbonus", "0");
			String key = memcacheUtils.readHBonusCache();
			if(key!=null&&key.equals("1"))
				request.setAttribute("hbonus", "1");
			return mapping.findForward("list");
		}else if(action.equals("query")){

		}else if(action.equals("detail")){
			Long id = RequestUtil.getLong(request, "id", -1L);

			return mapping.findForward("detail");
			//http://127.0.0.1/zld/activity.do?action=detail&id=&mobile=
		}
		return null;
	}


}
