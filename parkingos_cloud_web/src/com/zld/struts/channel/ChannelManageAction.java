package com.zld.struts.channel;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class ChannelManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@SuppressWarnings("rawtypes")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}

		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String sql = "select * from org_channel_tb where state=? ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = daService.getAll(sql+" order by create_time desc ",new Object[]{0});
			int count =0;
			if(list!=null)
				count = list.size();
			String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "name"));
			int ret = 0;
			ret = daService.update("insert into org_channel_tb (name,state,create_time) values(?,?,?) ",
					new Object[]{name,0,System.currentTimeMillis()/1000});
			AjaxUtil.ajaxOutput(response, ret+"");
			return null;
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Integer state = RequestUtil.getInteger(request, "state", 0);
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "name"));
			int	result = daService.update("update org_channel_tb set name =?,state=? where id=?",
					new Object[]{name,state,id});
			AjaxUtil.ajaxOutput(response, ""+result);
			return null;
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			int	result = daService.update("update org_channel_tb set state=? where id=?",
					new Object[]{1,id});
			AjaxUtil.ajaxOutput(response, ""+result);
		}else if(action.equals("set")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			request.setAttribute("chanid", id);
			return mapping.findForward("setting");
		}
		return null;
	}
}
