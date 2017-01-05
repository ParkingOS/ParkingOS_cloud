package com.zld.struts.request;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.AjaxUtil;
import com.zld.CustomDefind;
import com.zld.service.DataBaseService;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
/**
 * 渠道接口登录 
 * @author Administrator
 *
 */
public class ChannelLoginAction extends Action{
	
	@Autowired
	private DataBaseService daService;
	private Logger logger = Logger.getLogger(ChannelLoginAction.class);
	//http://127.0.0.1/zld/collectorlogin.do?username=1000005&action=forpass
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Map<String,Object> infoMap = new HashMap<String, Object>();
		logger.error("action:"+action);
		
		//http://127.0.0.1/zld/collectorlogin.do?username=21856&password=123456
		//http://127.0.0.1/zld/collectorlogin.do?username=12703&password=123456&out=json
		String username =RequestUtil.processParams(request, "username");
		String pass =RequestUtil.processParams(request, "password");
		String version = RequestUtil.getString(request, "version");
		logger.error("user:"+username+",pass:"+pass);
		String sql = "select * from user_info_tb where id=? and md5pass=?";// and auth_flag=?";
		if(pass.length()<32){
			//md5密码 ，生成规则：原密码md5后，加上'zldtingchebao201410092009'再次md5
			pass =StringUtils.MD5(pass);
			pass = StringUtils.MD5(pass +"zldtingchebao201410092009");
		}
		if(!StringUtils.isNumber(username)){
			infoMap.put("status", "0");
			AjaxUtil.ajaxOutput(response, StringUtils.createXML(infoMap));
			return null;
		}
		Map user = daService.getPojo(sql, new Object[]{Long.valueOf(username),pass});//,ZLDType.ZLD_COLLECTOR_ROLE});
		//logger.error(user);
		if(user==null){
			infoMap.put("info", "用户名或密码错误");
		}else {
			Long uin = (Long)user.get("id");
			String token = StringUtils.MD5(username+pass+System.currentTimeMillis());
			infoMap.put("status", "1");
			infoMap.put("token", token);
			infoMap.put("chanid", user.get("chanid"));
			infoMap.put("city_merchants_id", 321000);
			infoMap.put("signkey",CustomDefind.getValue("RSA"+user.get("chanid")));
			infoMap.put("company_id", 1009);
			doSaveSession(uin,token);
		}
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
		return null;
	}
	/**
	 * 保存token到数据库中
	 * @param uin
	 * @param token
	 */
	private void doSaveSession(Long uin,String token){
		Map<String, Object> map = daService.getMap("select token from user_session_tb where uin=? ", new Object[]{uin});
		if(map!=null){
			daService.update("update user_session_tb set token=? ,create_time=? where uin=? ", 
					new Object[]{token,System.currentTimeMillis()/1000,uin});
		}else {
			//保存本次登录的token
			daService.update("insert into user_session_tb (uin,token,create_time) " +
					"values (?,?,?)", 
					new Object[]{uin,token,System.currentTimeMillis()/1000});
		}
	}
	
	public static void main(String[] args) {
		String pass = "guomai001";
		try {
			pass =StringUtils.MD5(pass);
			pass = StringUtils.MD5(pass +"zldtingchebao201410092009");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println(pass);
	}
}