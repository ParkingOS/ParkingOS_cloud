package com.zld.struts.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import pay.Constants;

import com.zld.AjaxUtil;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import com.zld.wxpublic.util.CommonUtil;

public class WeixinPublicChatAction extends Action {
	@Autowired
	private DataBaseService daService;
	
	@Autowired
	private PgOnlyReadService pService;
	
	@Autowired
	private PublicMethods publicMethods;
	
	private Logger logger = Logger.getLogger(WeixinPublicChatAction.class);

	/**
	 * weixin
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		if(action.equals("")){
			String code = RequestUtil.processParams(request, "code");
			String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+Constants.WXPUBLIC_APPID+"&secret="+Constants.WXPUBLIC_SECRET+"&code="+code+"&grant_type=authorization_code";
			String result = CommonUtil.httpsRequest(access_token_url, "GET", null);
			JSONObject map = JSONObject.fromObject(result);
			if(map == null || map.get("errcode") != null){
				logger.error(">>>>>>>>>>>>获取openid失败....,重新获取>>>>>>>>>>>");
				String redirect_url = "http%3a%2f%2f"+Constants.WXPUBLIC_REDIRECTURL+"%2fzld%2fwxpchat.do";
				String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
						+ Constants.WXPUBLIC_APPID
						+ "&redirect_uri="
						+ redirect_url
						+ "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
				response.sendRedirect(url);
				return null;
			}
			String openid = (String)map.get("openid");
//			String openid = "oRoektybTsv33_vSKKUwLAsJAquc";
			request.setAttribute("openid", openid);
			Map<String, Object> userMap = daService.getMap("select * from user_info_tb where auth_flag=? and wxp_openid=? and state=? ",
							new Object[] {4, openid, 0 });
			if(userMap == null){//未绑定用户帐户，进入用户绑定页面
				request.setAttribute("action", "wxpchat.do?action=bind");
				return mapping.findForward("adduser");
			}
			Long count = daService.getLong("select count(id) from car_info_tb where uin=? and state=? ",
					new Object[] { userMap.get("id"), 1 });
			if(count == 0){//进入绑定车牌页面
				request.setAttribute("mobile", userMap.get("mobile"));
				request.setAttribute("action", "wxpchat.do?action=getrosters");
				return mapping.findForward("addcarnumber");
			}
			
			String username = "";
			String password = "";
			if(userMap.get("hx_name") != null){
				username = (String)userMap.get("hx_name");
			}
			if(userMap.get("hx_pass") != null){
				password = (String)userMap.get("hx_pass");
			}
			request.setAttribute("username", username);
			request.setAttribute("password", password);
			return mapping.findForward("rosters");
			//http://127.0.0.1/zld/
		}else if(action.equals("bind")){
			String mobile = RequestUtil.processParams(request, "mobile").trim();
			String openid = RequestUtil.processParams(request, "openid");
			request.setAttribute("openid", openid);
			request.setAttribute("mobile", mobile);
			if(mobile.equals("") || openid.equals("")){
				return mapping.findForward("error");
			}
			Long bind_count = daService.getLong("select count(*) from user_info_tb where wxp_openid is not null and mobile=? ",
					new Object[] { mobile });
			int result = daService.update("update user_info_tb set wxp_openid=? where auth_flag=? and state=? and mobile=? ",
							new Object[] { openid, 4, 0, mobile });//微信公众号绑定车主账户
			if(result == 1){
				Map<String, Object> userMap = daService.getMap("select * from user_info_tb where mobile=? and auth_flag=? ",
								new Object[] { mobile, 4 });
				publicMethods.sharkbinduser(openid, (Long)userMap.get("id"), bind_count);
				
				Long count = daService.getLong("select count(c.id) from car_info_tb c,user_info_tb u where c.uin=u.id and u.auth_flag=? and c.state=? and u.mobile=? ",
								new Object[] {4, 1, mobile });
				if(count == 0){//进入绑定车牌账号
					request.setAttribute("action", "wxpchat.do?action=getrosters");
					return mapping.findForward("addcarnumber");
				} else {//跳转到车场列表页面
					String username = "";
					String password = "";
					if(userMap.get("hx_name") != null){
						username = (String)userMap.get("hx_name");
					}
					if(userMap.get("hx_pass") != null){
						password = (String)userMap.get("hx_pass");
					}
					request.setAttribute("username", username);
					request.setAttribute("password", password);
					return mapping.findForward("rosters");
				}
			}
			return mapping.findForward("error");
		}else if(action.equals("getrosters")){
			String openid = RequestUtil.processParams(request, "openid");
			request.setAttribute("openid", openid);
			if(openid.equals("")){
				return mapping.findForward("error");
			}
			Map<String, Object> userMap = daService.getMap("select * from user_info_tb where auth_flag=? and wxp_openid=? and state=? ",
					new Object[] {4, openid, 0 });
			
			String username = "";
			String password = "";
			if(userMap.get("hx_name") != null){
				username = (String)userMap.get("hx_name");
			}
			if(userMap.get("hx_pass") != null){
				password = (String)userMap.get("hx_pass");
			}
			request.setAttribute("username", username);
			request.setAttribute("password", password);
			return mapping.findForward("rosters");
			
		}else if(action.equals("tochat")){
			String username = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "username"));
			String contacts = RequestUtil.processParams(request, "contacts");//联系人
			if(username.equals("") || contacts.equals("")){
				return mapping.findForward("error");
			}
			Map<String, Object> userMap = pService.getMap("select wx_imgurl,hx_pass from user_info_tb where hx_name=? ", new Object[]{username});
			Map<String, Object> contactsMap = pService.getMap("select wx_imgurl,wx_name from user_info_tb where hx_name=? ", new Object[]{contacts});
			if(userMap == null || contactsMap == null){
				return mapping.findForward("error");
			}
			request.setAttribute("username", username);
			request.setAttribute("password", userMap.get("hx_pass"));
			request.setAttribute("contacts", contacts);
			request.setAttribute("cwxname", contactsMap.get("wx_name"));
			request.setAttribute("fromimg", contactsMap.get("wx_imgurl"));
			request.setAttribute("toimg", userMap.get("wx_imgurl"));
			return mapping.findForward("tochat");
			//http://192.168.199.239/zld/wxpchat.do?action=tochat&username=a&contacts=test
		}else if(action.equals("getimages")){
			String rosters = RequestUtil.processParams(request, "rosters");
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			if(!rosters.equals("")){
				String roster[] = rosters.split(",");
				if(rosters.length() > 0){
					List<Object> params = new ArrayList<Object>();
					String preParams  ="";
					for(int i=0; i<roster.length; i++){
						if(preParams.equals("")){
							preParams ="?";
						}else{
							preParams += ",?";
						}
						params.add(roster[i]);
					}
					list = pService.getAllMap("select hx_name,wx_name,wx_imgurl from user_info_tb where hx_name in (" + preParams + ")", params);
				}
			}
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(list));
		}
		return null;
	}
}
