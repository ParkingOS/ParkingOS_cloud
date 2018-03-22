package com.zld.struts.city;

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
import java.util.Map;

public class CityInfoAction extends Action {
	@Autowired
	private DataBaseService daService;
	@SuppressWarnings("unchecked")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		Long uin = (Long)request.getSession().getAttribute("loginuin");
		Integer authId = RequestUtil.getInteger(request, "authid",-1);
		request.setAttribute("authid", authId);
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			Map<String, Object> cityMap = daService.getPojo("select * from org_city_merchants where id=?",
					new Object[]{cityid});

			Map<String, Object> userMap = daService.getMap("select nickname,mobile,phone from user_info_tb where id=? ", new Object[]{uin});
			StringBuffer buffer = new StringBuffer("[");
			for (String  key : cityMap.keySet()) {
				buffer.append("{\"name\":\""+key+"\",\"value\":\""+cityMap.get(key)+"\"},");
			}
			for (String  key : userMap.keySet()) {
				buffer.append("{\"name\":\""+key+"\",\"value\":\""+userMap.get(key)+"\"},");
			}
			buffer.append("{\"name\":\"newpass\",\"value\":\"\"},");
			buffer.append("{\"name\":\"confirmpass\",\"value\":\"\"},{\"name\":\"is_inpark_incity\",\"value\":\"\"},");
			String result = buffer.toString();
			result = result.substring(0,result.length()-1)+"]";
			result =result.replace("null", "");
			request.setAttribute("info", result);
			request.setAttribute("uin", uin);
			return mapping.findForward("success");
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));

			String mobile = RequestUtil.processParams(request, "mobile");
			String phone = RequestUtil.processParams(request, "phone");
			String nickname = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname"));
			String newpass = RequestUtil.processParams(request, "newpass");
			String gps =  AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "gps"));
			String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
			String confirmpass = RequestUtil.processParams(request, "confirmpass");
			Integer is_group_pursue = RequestUtil.getInteger(request, "is_group_pursue", 0);
			Integer is_inpark_incity = RequestUtil.getInteger(request, "is_inpark_incity", 0);
			if(!newpass.equals("")){
				int ret = editPass(uin, newpass, confirmpass);
				if(ret != 1){
					AjaxUtil.ajaxOutput(response, ret + "");
					return null;
				}
			}
			int r = daService.update("update org_city_merchants set name=?,gps=?,address=?,is_group_pursue=?,is_inpark_incity=? where id=? ",
					new Object[]{name,gps,address, is_group_pursue,is_inpark_incity, id});
			r = daService.update("update user_info_tb set mobile=?,phone=?,nickname=? where id=? ",
					new Object[]{mobile, phone, nickname, uin});
			AjaxUtil.ajaxOutput(response, r + "");
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes" })
	private int editPass(Long uin, String newPass, String confirmPass){
		String sql = "update user_info_tb set password =? ,md5pass=? where id =?";
		String md5pass = newPass;
		try {
			md5pass = StringUtils.MD5(newPass);
			md5pass = StringUtils.MD5(md5pass+"zldtingchebao201410092009");
		} catch (Exception e) {
			e.printStackTrace();
		}
		int result = 0;
		if(newPass.length()<6){
			result = -2;
		}else if(newPass.equals(confirmPass)){
			Object [] values = new Object[]{newPass,md5pass,Long.valueOf(uin)};
			int r = daService.update(sql, values);
			result = r;
		}else {
			result = -3;
		}
		return result;
	}
}
