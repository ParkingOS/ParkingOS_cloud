package com.zld.struts.request;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import pay.Constants;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.zld.AjaxUtil;
import com.zld.CustomDefind;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoClientFactory;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.HttpProxy;
import com.zld.utils.RequestUtil;
import com.zld.utils.SendMessage;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.wxpublic.util.CommonUtil;
import com.zld.wxpublic.util.PayCommonUtil;

public class WeixinPublicAccountAction extends Action {
	@Autowired
	private DataBaseService daService;
	
	@Autowired
	private PgOnlyReadService pService;
	
	@Autowired
	private PublicMethods publicMethods;
	
	@Autowired
	private CommonMethods commonMethods;
	
	private Logger logger = Logger.getLogger(WeixinPublicAccountAction.class);

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
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>进入微信我的账户>>>>>>>>>>>>>>>,code:"+code);
			String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+Constants.WXPUBLIC_APPID+"&secret="+Constants.WXPUBLIC_SECRET+"&code="+code+"&grant_type=authorization_code";
			String result = CommonUtil.httpsRequest(access_token_url, "GET", null);
			JSONObject map = null;
			JSONObject wxuserinfo = null;
			if(result!=null){
				map = JSONObject.fromObject(result);
			}
			if(map ==null || map.get("errcode") != null){
				System.out.println("获取openid失败....");
				String redirect_url = "http%3a%2f%2f"+Constants.WXPUBLIC_REDIRECTURL+"%2fzld%2fwxpaccount.do";
				String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
						+ Constants.WXPUBLIC_APPID
						+ "&redirect_uri="
						+ redirect_url
						+ "&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
				response.sendRedirect(url);
				return null;
			}
			String openid = (String)map.get("openid");
			String access_token = (String)map.get("access_token");
			String scope = (String)map.get("scope");
			if(scope.equals("snsapi_userinfo")){
				String userinfo_url = "https://api.weixin.qq.com/sns/userinfo?access_token="+access_token+"&openid="+openid+"&lang=zh_CN";
				result = CommonUtil.httpsRequest(userinfo_url, "GET", null);
				if(result != null){
					wxuserinfo = JSONObject.fromObject(result);
				}
				if(wxuserinfo ==null || wxuserinfo.get("errcode") != null){
					System.out.println("获取openid失败....");
					String redirect_url = "http%3a%2f%2f"+Constants.WXPUBLIC_REDIRECTURL+"%2fzld%2fwxpaccount.do";
					String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
							+ Constants.WXPUBLIC_APPID
							+ "&redirect_uri="
							+ redirect_url
							+ "&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
					response.sendRedirect(url);
					return null;
				}
				String wxname = (String)wxuserinfo.get("nickname");
				String wximg = (String)wxuserinfo.get("headimgurl");
				request.setAttribute("wximg", wximg);
				request.setAttribute("wxname", wxname);
			}
			//其他参数
			
//			String openid = "oRoektybTsv33_vSKKUwLAsJAquc";
			
			request.setAttribute("openid", openid);
			Map<String, Object> userMap = daService
					.getMap("select * from user_info_tb where auth_flag=? and wxp_openid=? and state=? ",
							new Object[] {4, openid, 0 });
			if(userMap == null){//未绑定用户帐户，进入用户绑定页面
				request.setAttribute("action", "wxpaccount.do?action=bind");
				return mapping.findForward("adduser");
			}
			Long count = daService.getLong(
					"select count(id) from car_info_tb where uin=? and state=? ",
					new Object[] { userMap.get("id"), 1 });
			if(count == 0){//进入绑定车牌页面
				request.setAttribute("mobile", userMap.get("mobile"));
				request.setAttribute("action", "wxpaccount.do?action=toaccountpage");
				return mapping.findForward("addcarnumber");
			}
			
			Long time = System.currentTimeMillis()/1000;
			Long ticket_count = daService.getLong("select count(id) from ticket_tb where uin = ? and limit_day > ? and state=? ",
					new Object[]{userMap.get("id"),time,0});
			String credit = "0/0";
			Integer is_auth = (Integer)userMap.get("is_auth");
			if(is_auth == 1){
				credit = userMap.get("credit_limit")+"/30";
			}
			request.setAttribute("ticket_count", ticket_count);//停车券数
			request.setAttribute("balance", userMap.get("balance"));//余额
			request.setAttribute("mobile", userMap.get("mobile"));
			request.setAttribute("credit", credit);
			return mapping.findForward("account");
		}else if(action.equals("bind")){
			String mobile = RequestUtil.processParams(request, "mobile").trim();
			String openid = RequestUtil.processParams(request, "openid");
			request.setAttribute("openid", openid);
			request.setAttribute("mobile", mobile);
			if(mobile.equals("") || openid.equals("")){
				return mapping.findForward("error");
			}
			Long bind_count = daService.getLong(
					"select count(*) from user_info_tb where wxp_openid is not null and mobile=? ",
					new Object[] { mobile });
			int result = daService
					.update("update user_info_tb set wxp_openid=? where auth_flag=? and state=? and mobile=? ",
							new Object[] { openid, 4, 0, mobile });//微信公众号绑定车主账户
			if(result == 1){
				Map<String, Object> userMap = daService
						.getMap("select * from user_info_tb where mobile=? and auth_flag=? ",
								new Object[] { mobile, 4 });
				publicMethods.sharkbinduser(openid, (Long)userMap.get("id"),bind_count);
				
				Long count = daService
						.getLong("select count(c.id) from car_info_tb c,user_info_tb u where c.uin=u.id and u.auth_flag=? and c.state=? and u.mobile=? ",
								new Object[] {4, 1, mobile });
				if(count == 0){//进入绑定车牌账号
					request.setAttribute("action", "wxpaccount.do?action=toaccountpage");
					return mapping.findForward("addcarnumber");
				} else {//跳转到账户页面
					Long time = System.currentTimeMillis()/1000;
					Long ticket_count = daService.getLong("select count(id) from ticket_tb where uin = ? and limit_day > ? and state=? ",
							new Object[]{userMap.get("id"),time,0});
					String credit = "0/0";
					Integer is_auth = (Integer)userMap.get("is_auth");
					if(is_auth == 1){
						credit = userMap.get("credit_limit")+"/30";
					}
					request.setAttribute("credit", credit);
					request.setAttribute("ticket_count", ticket_count);//停车券数
					request.setAttribute("balance", userMap.get("balance"));//余额
					return mapping.findForward("account");
				}
			}
			return mapping.findForward("error");
		}else if(action.equals("toaccountpage")){
			String openid = RequestUtil.processParams(request, "openid");
			if(openid.equals("")){
				return mapping.findForward("error");
			}
			Map<String, Object> userMap = daService
					.getMap("select * from user_info_tb where auth_flag=? and wxp_openid=? and state=? ",
							new Object[] {4, openid, 0 });
			if(userMap == null){
				return mapping.findForward("error");
			}
			Long time = System.currentTimeMillis()/1000;
			Long ticket_count = daService.getLong("select count(id) from ticket_tb where uin = ? and limit_day > ? and state=? ",
					new Object[]{userMap.get("id"),time,0});
			
			String credit = "0/0";
			Integer is_auth = (Integer)userMap.get("is_auth");
			if(is_auth == 1){
				credit = userMap.get("credit_limit")+"/30";
			}
			request.setAttribute("credit", credit);
			request.setAttribute("ticket_count", ticket_count);//停车券数
			request.setAttribute("balance", userMap.get("balance"));//余额
			request.setAttribute("openid", openid);
			request.setAttribute("mobile", userMap.get("mobile"));
			return mapping.findForward("account");
		}else if(action.equals("toticketpage")){
			String openid = RequestUtil.processParams(request, "openid");
			Integer type = RequestUtil.getInteger(request, "type", 0);//0:可用停车券，1历史停车券
			
			if(openid.equals("")){
				return mapping.findForward("error");
			}
			Map<String, Object> userMap = daService
					.getMap("select * from user_info_tb where auth_flag=? and wxp_openid=? and state=? ",
							new Object[] {4, openid, 0 });
			if(userMap == null){
				return mapping.findForward("error");
			}
			String mobile = (String)userMap.get("mobile");
			request.setAttribute("mobile", mobile);
			request.setAttribute("type", type);
			request.setAttribute("openid", openid);
			request.setAttribute("domain", Constants.WXPUBLIC_REDIRECTURL);
			return mapping.findForward("ticket");
		}else if(action.equals("toaccountdetail")){
			String openid = RequestUtil.processParams(request, "openid");
			Long orderid = RequestUtil.getLong(request, "orderid", -1L);
//			openid = "oRoektybTsv33_vSKKUwLAsJAquc";
			Map<String, Object> userMap = daService
					.getMap("select * from user_info_tb where auth_flag=? and wxp_openid=? and state=? ",
							new Object[] {4, openid, 0 });
			if(userMap == null){
				return mapping.findForward("error");
			}
			request.setAttribute("mobile", userMap.get("mobile"));
			request.setAttribute("orderid", orderid);
			return mapping.findForward("accountdetail");
		}else if(action.equals("regbonus")){
			Long media =RequestUtil.getLong(request, "media",11L);
			Long uid =RequestUtil.getLong(request, "uid",-1L);//推荐人，当id=999时（收费员推荐车主）
			request.setAttribute("media", media);
			request.setAttribute("uid", uid);
			
			String code = RequestUtil.processParams(request, "code");
			logger.error(">>>>>>>>>>>>>>>>>>>>>>>>>进入微信摇一摇红包>>>>>>>>>>>>>>>,code:"+code);
			String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+Constants.WXPUBLIC_APPID+"&secret="+Constants.WXPUBLIC_SECRET+"&code="+code+"&grant_type=authorization_code";
			String result = CommonUtil.httpsRequest(access_token_url, "GET", null);
			JSONObject map = JSONObject.fromObject(result);
			if(map.get("errcode") != null){
				return mapping.findForward("error");
			}
			String openid = (String)map.get("openid");
			logger.error("openid:"+openid);
//			String openid = "o0ciquFyGzc2DSxh8lxzwMIW6fwg";
			request.setAttribute("openid", openid);
			
			Map<String, Object> userMap = daService.getMap(
					"select * from user_info_tb where wxp_openid=? ",
					new Object[] { openid });
			if(userMap == null){
				return mapping.findForward("regbonus");
			}else{
				Long count = daService.getLong(
						"select count(id) from car_info_tb where uin=? and state=? ",
						new Object[] { userMap.get("id"), 1 });
				request.setAttribute("type", 0);//0老用户
				request.setAttribute("mobile", userMap.get("mobile"));
				if(count == 0){
					return mapping.findForward("addcnum_regbonus");
				}else{
					//微信公众号JSSDK授权验证
					Map<String, String> ret = new HashMap<String, String>();
					ret = publicMethods.getJssdkApiSign(request);
					//jssdk权限验证参数
					request.setAttribute("appid", Constants.WXPUBLIC_APPID);
					request.setAttribute("nonceStr", ret.get("nonceStr"));
					request.setAttribute("timestamp", ret.get("timestamp"));
					request.setAttribute("signature", ret.get("signature"));
					return mapping.findForward("nobonus");
				}
			}
		}else if(action.equals("getcode")){
			String mobile = RequestUtil.processParams(request, "mobile");
			Long media =RequestUtil.getLong(request, "media",11L);
			Long uid =RequestUtil.getLong(request, "uid",-1L);//推荐人，当id=999时（收费员推荐车主）
			if(mobile.equals("")){
				return mapping.findForward("error");
			}
			Long uin = -1L;
			Integer type = 0;//type=0：非新注册用户 type=1:新注册用户
			String sql = "select * from user_info_tb where mobile=? and auth_flag=? ";
			Map<String, Object> user = daService.getPojo(sql, new Object[]{mobile,4});
			if(user == null){
				Double money = 10d;//新注册返10元券
				uin = publicMethods.regUser(mobile, media,uid,true);
				if(uin > 0){
					/*int r = daService.update("insert into bonus_record_tb (bid,ctime,mobile,state,amount) values(?,?,?,?,?)",
							new Object[] { media, System.currentTimeMillis()/1000, mobile, 0, money });*/
				}
				type = 1;
				logger.error("摇一摇红包，新注册用户，mobile："+mobile);
			}else{
				uin = (Long)user.get("id");
			}
			
			//发送短信验证码
			if(!publicMethods.isCanSendShortMesg(mobile)){
				AjaxUtil.ajaxOutput(response, "-7");
				return null;
			}
			Integer code = new SendMessage().sendMessageToCarOwer(mobile);
			if(code!=null){
				logger.error("code:"+code+",mobile:"+mobile+" ,uin="+uin);
				//保存验证码
				//删除已经保存但没有验证过的验证码（已无效的验证码）
				try {
					daService.update("delete from verification_code_tb where uin =?",new Object[]{uin});
				} catch (Exception e) {
					e.printStackTrace();
				}
				int r =daService.update("insert into verification_code_tb (verification_code,uin,create_time,state)" +
						" values (?,?,?,?)", new Object[]{code,uin,System.currentTimeMillis()/1000,0});
				logger.error("code:"+code+",mobile:"+mobile+" ,uin="+uin+"，保存验证码："+r);
				Map verificationMap = daService.getPojo("select verification_code from verification_code_tb" +
						" where uin=? and state=? ", new Object[]{uin,0});
				logger.error(verificationMap);
				if(r==1)
					AjaxUtil.ajaxOutput(response, type + "");
				else{
					AjaxUtil.ajaxOutput(response, "-7");
					return null;
				}
			}else {
				AjaxUtil.ajaxOutput(response, "-3");
			}
		}else if(action.equals("regbonusbind")){
			String openid = RequestUtil.processParams(request, "openid");
			String mobile = RequestUtil.processParams(request, "mobile");
			Integer type = RequestUtil.getInteger(request, "type", 0);//type=1表示新注册
			request.setAttribute("type", type);
			request.setAttribute("openid", openid);
			request.setAttribute("mobile", mobile);
			if(mobile.equals("") || openid.equals("")){
				return mapping.findForward("error");
			}
			Long bind_count = daService.getLong(
					"select count(*) from user_info_tb where wxp_openid is not null and mobile=? ",
					new Object[] { mobile });
			int result = daService
					.update("update user_info_tb set wxp_openid=? where auth_flag=? and state=? and mobile=? ",
							new Object[] { openid, 4, 0, mobile });//微信公众号绑定车主账户
			if(result == 1){
				Map<String, Object> userMap = daService
						.getMap("select * from user_info_tb where mobile=? and auth_flag=? ",
								new Object[] { mobile, 4 });
				publicMethods.sharkbinduser(openid, (Long)userMap.get("id"),bind_count);
				
				Long count = daService.getLong(
						"select count(id) from car_info_tb where uin=? and state=? ",
						new Object[] { userMap.get("id"), 1 });
				if(count == 0){//进入绑定车牌账号
					return mapping.findForward("addcnum_regbonus");
				}else{
					//微信公众号JSSDK授权验证
					Map<String, String> ret = new HashMap<String, String>();
					ret = publicMethods.getJssdkApiSign(request);
					//jssdk权限验证参数
					request.setAttribute("appid", Constants.WXPUBLIC_APPID);
					request.setAttribute("nonceStr", ret.get("nonceStr"));
					request.setAttribute("timestamp", ret.get("timestamp"));
					request.setAttribute("signature", ret.get("signature"));
					return mapping.findForward("nobonus");
				}
			}else{
				return mapping.findForward("error");
			}
		}else if(action.equals("tobonuspage")){
			Integer type = RequestUtil.getInteger(request, "type", 0);
			String mobile = RequestUtil.processParams(request, "mobile");
			
//			type = 1;
//			mobile="18201517240";
			
			if(mobile.equals("")){
				return mapping.findForward("error");
			}
			//微信公众号JSSDK授权验证
			Map<String, String> ret = new HashMap<String, String>();
			ret = publicMethods.getJssdkApiSign(request);
			//jssdk权限验证参数
			request.setAttribute("appid", Constants.WXPUBLIC_APPID);
			request.setAttribute("nonceStr", ret.get("nonceStr"));
			request.setAttribute("timestamp", ret.get("timestamp"));
			request.setAttribute("signature", ret.get("signature"));
			if(type == 1){
				request.setAttribute("mobile", mobile);
				Map<String, Object> bonusMap = daService
						.getMap("select * from bonus_record_tb where mobile=? order by ctime limit 1 ",
								new Object[] {mobile});
				request.setAttribute("amount", bonusMap.get("amount"));//已经领取过
				return mapping.findForward("bonus");
			}else{
				return mapping.findForward("nobonus");
			}
			
		}else if(action.equals("balance")){
			String openid = RequestUtil.processParams(request, "openid");
			
//			openid = "oRoektybTsv33_vSKKUwLAsJAquc";
			logger.error(">>>>>>>>>>>>>>>>微信公众号进入我的余额:openid"+openid);
			if(openid.equals("")){
				return mapping.findForward("error");
			}
			Map<String, Object> userMap = daService.getMap(
					"select balance from user_info_tb where wxp_openid=? ",
					new Object[] { openid });
			if(userMap == null){
				return mapping.findForward("error");
			}
			request.setAttribute("balance", userMap.get("balance"));
			request.setAttribute("openid", openid);
			return mapping.findForward("balance");
		}else if(action.equals("charge")){
			String openid = RequestUtil.processParams(request, "openid");
			Double money = RequestUtil.getDouble(request, "fee", 0d);
			
			if(openid.equals("") || money <= 0){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Map<String, Object> userMap = daService.getMap(
					"select * from user_info_tb where wxp_openid=? ",
					new Object[] { openid });
			if(userMap == null){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			String addressip = request.getRemoteAddr();
			Map<String, Object> attachMap = new HashMap<String, Object>();
			attachMap.put("money", money);//充值金额
			attachMap.put("mobile", userMap.get("mobile"));//车主手机号
			attachMap.put("type", 3);//充值
			attachMap.put("bind_flag", 1);//0:未绑定 1：已绑定账户
			//附加数据
			String attach = StringUtils.createJson(attachMap);
			Map<String, Object> infoMap = new HashMap<String, Object>();
			try {
				//设置支付参数
				SortedMap<Object, Object> signParams = new TreeMap<Object, Object>();
				//获取JSAPI网页支付参数
				signParams = PayCommonUtil.getPayParams(addressip, money, "停车宝账户充值", attach, openid);
				
				infoMap.put("appid", signParams.get("appId"));
				infoMap.put("nonceStr", signParams.get("nonceStr"));
				infoMap.put("packagevalue", signParams.get("package"));
				infoMap.put("timestamp", signParams.get("timeStamp"));
				infoMap.put("paySign", signParams.get("paySign"));
				infoMap.put("signType", signParams.get("signType"));
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			infoMap.put("openid", openid);
			//其他参数
			infoMap.put("money", money);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
		}else if(action.equals("tocarnumber")){
			String openid = RequestUtil.processParams(request, "openid");
			Map<String, Object> userMap = daService
					.getMap("select id,mobile from user_info_tb where wxp_openid=? limit ? ",
							new Object[] { openid, 1 });
			if(userMap != null){
				Long count = daService.getLong("select count(*) from car_info_tb where uin=? and state=? ",
								new Object[] { userMap.get("id"), 1 });
				logger.error("uin:"+userMap.get("id")+",count:"+count);
				request.setAttribute("count", count);
				
				request.setAttribute("openid", openid);
				request.setAttribute("mobile", userMap.get("mobile"));
				return mapping.findForward("tocarnumber");
			}
			return mapping.findForward("error");
		}else if(action.equals("toupload")){
			String carnumber = RequestUtil.processParams(request, "carnumber");
			String openid = RequestUtil.processParams(request, "openid");
			if(openid.equals("")){
				return mapping.findForward("error");
			}
			if(!carnumber.equals("")){
				Map<String, Object> carMap = daService.getMap("select id from car_info_tb where car_number=? ",
						new Object[] { carnumber });
				if(carMap != null){
					request.setAttribute("carid", carMap.get("id"));
				}
			}
			try {
				//微信公众号JSSDK授权验证
				Map<String, String> ret = new HashMap<String, String>();
				ret = publicMethods.getJssdkApiSign(request);
				//jssdk权限验证参数
				request.setAttribute("appid", Constants.WXPUBLIC_APPID);
				request.setAttribute("nonceStr", ret.get("nonceStr"));
				request.setAttribute("timestamp", ret.get("timestamp"));
				request.setAttribute("signature", ret.get("signature"));
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			request.setAttribute("carnumber", carnumber);
			request.setAttribute("domain", Constants.WXPUBLIC_REDIRECTURL);
			request.setAttribute("openid", openid);
			return mapping.findForward("toupload");
		}else if(action.equals("previewpic")){//预览认证图片
			String serverid = RequestUtil.processParams(request, "serverid");//微信服务器图片serverid
			if(!serverid.equals("")){
				try {
					String accessToken = publicMethods.getWXPAccessToken();
					previewpic(accessToken, serverid, response);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}else if(action.equals("upload")){
			String carnumber = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "carnumber"));
			String serverid = RequestUtil.processParams(request, "serverid");
			Long carid = RequestUtil.getLong(request, "carid", -1L);
			String opneid = RequestUtil.processParams(request, "openid");
			Long curTime = System.currentTimeMillis()/1000;
			logger.error("upload>>>carnumber:"+carnumber+",carid:"+carid);
			if(carnumber.equals("") || opneid.equals("")){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Map<String, Object> userMap = daService.getMap(
					"select id from user_info_tb where wxp_openid=? limit ? ",
					new Object[] { opneid, 1 });
			if(userMap == null){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Long uin = (Long)userMap.get("id");
			if(carid == -1){
				logger.error("添加新车牌carnumber:"+carnumber+",uin:"+uin+",carid:"+carid);
				Map carMap = daService.getMap("select uin from car_info_tb where car_number=? ", new Object[]{carnumber});
				if(carMap != null){//该车牌已注册
					Long cuin = (Long)carMap.get("uin");
					logger.error("该车牌已被注册，uin:"+uin+",注册该车牌的车主是cuin："+cuin+",carnumber:"+carnumber);
					if(cuin.intValue() != uin.intValue()){//车牌被别人注册了
						AjaxUtil.ajaxOutput(response, "-2");
					}else{//车牌被自己注册了
						AjaxUtil.ajaxOutput(response, "-3");
					}
					return null;
				}else{
					Long count = daService.getLong("select count(id) from car_info_tb where uin=? and state=? ", new Object[]{uin, 1});
					if(count < 3){
						carid = daService.getkey("seq_ticket_tb");
						int r = daService.update("insert into car_info_tb(id,car_number,uin,create_time) values(?,?,?,?)", new Object[]{carid, carnumber, uin, curTime});
						logger.error("注册新车牌，uin:"+uin+",carnumber:"+carnumber+",carid:"+carid+",r:"+r);
					}else{
						logger.error("车牌已经超过三个，uin:"+uin+",carnumber:"+carnumber+",count:"+count);
						AjaxUtil.ajaxOutput(response, "-4");
						return null;
					}
				}
			}else{
				logger.error("修改车牌carnumber:"+carnumber+",uin:"+uin+",carid:"+carid);
				Map<String, Object> carMap = daService.getMap("select uin from car_info_tb where car_number=? and id != ? ",
								new Object[] { carnumber, carid });
				if(carMap != null){
					Long cuin = (Long)carMap.get("uin");
					if(cuin.intValue() != uin.intValue()){//车牌被别人注册了
						AjaxUtil.ajaxOutput(response, "-2");
					}else{//车牌被自己注册了
						AjaxUtil.ajaxOutput(response, "-3");
					}
					return null;
				}else{
					int r = daService.update("update car_info_tb set car_number=? where id=? ",
							new Object[] { carnumber, carid });
					logger.error("修改车牌，uin:"+uin+",carnumber:"+carnumber+",carid:"+carid+",r:"+r);
				}
			}
			if(carid != -1 && !serverid.equals("")){
				logger.error("upload picture>>>uin:"+uin+",carid:"+carid+",carnumber:"+carnumber+",serverid:"+serverid);
				String meidaId[] = serverid.split(",");
				try {
					Long count = daService.getLong("select count(id) from car_info_tb where car_number=? and is_auth> ?", new Object[]{carnumber, 0});
					logger.error("uin:"+uin+",carid:"+carid+",carnumber:"+carnumber+"count:"+count);
					if(count == 0){
						String accessToken = publicMethods.getWXPAccessToken();
						for(int i=0; i< meidaId.length; i++){
							uploadCheck2Mongodb(accessToken, meidaId[i], uin, carnumber, i);
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
			AjaxUtil.ajaxOutput(response, "1");
		}else if(action.equals("tobuyticket")){
			//******************购买停车券去掉了 ***************//
			if(true){
				return mapping.findForward("error");
			}
			//******************************************//
			String openid = RequestUtil.processParams(request, "openid");
			if(openid.equals("")){
				return mapping.findForward("error");
			}
			Map<String, Object> userMap = daService
					.getMap("select is_auth from user_info_tb where wxp_openid=? limit ? ",
							new Object[] { openid, 1 });
			if(userMap != null){
				Integer isauth = (Integer)userMap.get("is_auth");
				logger.error("tobuyticket>>>openid:"+openid+",isauth:"+isauth);
				Double authdiscount = StringUtils.formatDouble(CustomDefind.getValue("AUTHDISCOUNT"))*10;
				Double noauthdiscount = StringUtils.formatDouble(CustomDefind.getValue("NOAUTHDISCOUNT"))*10;
				request.setAttribute("isauth", isauth);
				request.setAttribute("openid", openid);
				request.setAttribute("authdisc", authdiscount);
				request.setAttribute("noauthdisc", noauthdiscount);
				return mapping.findForward("tobuyticket");
			}else{
				return mapping.findForward("error");
			}
		}else if(action.equals("ticketprice")){
			String openid = RequestUtil.processParams(request, "openid");
			Integer ticketmoney = RequestUtil.getInteger(request, "ticketmoney", 0);
			Integer num = RequestUtil.getInteger(request, "num", 0);
			logger.error("ticketprice>>>openid:"+openid+",ticketmoney:"+ticketmoney+",num:"+num);
			if(openid.equals("") || ticketmoney == 0 || num == 0){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Map<String, Object> userMap = daService
					.getMap("select is_auth from user_info_tb where wxp_openid=? limit ? ",
							new Object[] { openid, 1 });
			if(userMap != null){
				Integer isauth = (Integer)userMap.get("is_auth");
				Double discount = Double.valueOf(CustomDefind.getValue("NOAUTHDISCOUNT"));
				Double moneybefore = StringUtils.formatDouble(ticketmoney*num);
				if(isauth == 1){
					discount = Double.valueOf(CustomDefind.getValue("AUTHDISCOUNT"));
				}
				Double moneyafter = StringUtils.formatDouble(ticketmoney*num*discount);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("moneybefore", moneybefore);
				map.put("moneyafter", moneyafter);
				logger.error("ticketprice>>>openid:"+openid+",isauth:"+isauth+",moneybefore:"+moneybefore+",moneyafter:"+moneyafter);
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(map));
			}else{
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
		}else if(action.equals("buyticket")){
			String openid = RequestUtil.processParams(request, "openid");
			Integer ticketmoney = RequestUtil.getInteger(request, "ticketmoney", 0);
			Integer ticketnum = RequestUtil.getInteger(request, "ticketnum", 0);
			logger.error("buyticket>>>openid:"+openid+",ticketmoney:"+ticketmoney+",ticketnum:"+ticketnum);
			if(openid.equals("") || ticketmoney == 0 || ticketnum == 0){
				return mapping.findForward("error");
			}
			Map<String, Object> userMap = daService
					.getMap("select * from user_info_tb where wxp_openid=? limit ? ",
							new Object[] { openid, 1 });
			if(userMap == null){
				return mapping.findForward("error");
			}else{
				Integer isauth = (Integer)userMap.get("is_auth");
				Double discount = Double.valueOf(CustomDefind.getValue("NOAUTHDISCOUNT"));
				if(isauth == 1){
					discount = Double.valueOf(CustomDefind.getValue("AUTHDISCOUNT"));
				}
				Double total = StringUtils.formatDouble(ticketmoney*ticketnum*discount);
				Double wx_pay = 0d;
				Double balance_pay = 0d;
				logger.error("buyticket>>>openid:"+openid+",isauth:"+isauth+",total:"+total+",uin:"+userMap.get("id"));
				Double balance = Double.valueOf(userMap.get("balance") + "");//用户余额
				if(total > balance){
					balance_pay = balance;//余额全部用于支付
					wx_pay = StringUtils.formatDouble(total - balance_pay);
				}else{
					balance_pay = total;
				}
				if(wx_pay > 0){
					try {
						Map<String, Object> attachMap = new HashMap<String, Object>();
						attachMap.put("money", total);//打赏金额
						attachMap.put("type", 7);//买停车券
						attachMap.put("ticketmoney", ticketmoney);
						attachMap.put("ticketnum", ticketnum);
						//附加数据
						String attach = StringUtils.createJson(attachMap);
						//设置支付参数
						SortedMap<Object, Object> signParams = new TreeMap<Object, Object>();
						//获取JSAPI网页支付参数
						signParams = PayCommonUtil.getPayParams(request.getRemoteAddr(), wx_pay, "购买停车券", attach, openid);
						request.setAttribute("appid", signParams.get("appId"));
						request.setAttribute("nonceStr", signParams.get("nonceStr"));
						request.setAttribute("package", signParams.get("package"));
						request.setAttribute("packagevalue", signParams.get("package"));
						request.setAttribute("timestamp", signParams.get("timeStamp"));
						request.setAttribute("paySign", signParams.get("paySign"));
						request.setAttribute("signType", signParams.get("signType"));
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
				request.setAttribute("total", total);
				request.setAttribute("balance_pay", balance_pay);
				request.setAttribute("wx_pay", wx_pay);
				request.setAttribute("ticketmoney", ticketmoney);
				request.setAttribute("ticketnum", ticketnum);
				request.setAttribute("openid", openid);
				request.setAttribute("mobile", userMap.get("mobile"));
				return mapping.findForward("buyticket");
			}
		}else if(action.equals("toorderlist")){
			String mobile = RequestUtil.processParams(request, "mobile");
			if(mobile.equals("")){
				return mapping.findForward("error");
			}
			request.setAttribute("domain", Constants.WXPUBLIC_REDIRECTURL);
			request.setAttribute("mobile", mobile);
			return mapping.findForward("orderlist");
		}else if(action.equals("orderdetail")){
			String mobile = RequestUtil.processParams(request, "mobile");
			Long orderid = RequestUtil.getLong(request, "orderid", -1L);
			if(mobile.equals("") || orderid == -1){
				return mapping.findForward("error");
			}
			
			Map<String, Object> userMap = pService
					.getMap("select id,wxp_openid from user_info_tb where mobile=? and auth_flag=? ",
							new Object[] { mobile, 4 });
			if(userMap == null || userMap.get("wxp_openid") == null){
				return mapping.findForward("error");
			}
			Long etime = System.currentTimeMillis()/1000;
			request.setAttribute("orderid", orderid);
			request.setAttribute("domain", Constants.WXPUBLIC_REDIRECTURL);
			request.setAttribute("bonusid", -1);
			request.setAttribute("openid", userMap.get("wxp_openid"));
			Map<String, Object> orderMap = pService.getMap(
					"select * from order_tb where id=? ", new Object[] {orderid});
			if(orderMap == null){
				return mapping.findForward("error");
			}
			Integer state = (Integer)orderMap.get("state");
			request.setAttribute("state",state);
			request.setAttribute("btime",TimeTools.getTime_yyyyMMdd_HHmm((Long)orderMap.get("create_time")*1000));
			
			if(orderMap.get("end_time") != null){
				etime = (Long)orderMap.get("end_time");
				request.setAttribute("etime",TimeTools.getTime_yyyyMMdd_HHmm(etime*1000));
			}
			request.setAttribute("parktime", StringUtils.getTimeString((Long)orderMap.get("create_time"), etime));
			if(state == 0){
				Integer pid = (Integer)orderMap.get("pid");
				Integer car_type = (Integer)orderMap.get("car_type");//0：通用，1：小车，2：大车
				if(pid>-1){
					request.setAttribute("total", Double.valueOf(publicMethods.getCustomPrice((Long)orderMap.get("create_time"), System.currentTimeMillis()/1000, pid)));
				}else {
					request.setAttribute("total", Double.valueOf(publicMethods.getPrice((Long)orderMap.get("create_time"), System.currentTimeMillis()/1000, (Long)orderMap.get("comid"), car_type)));
				}
			}else{
				request.setAttribute("total", orderMap.get("total"));
			}
			if(orderMap.get("pay_type") != null){
				Integer pay_type = (Integer)orderMap.get("pay_type");
				String paytype = "";
				if(pay_type == 0 || pay_type == 2){
					paytype = "电子支付";
				}else if(pay_type == 1 || pay_type == 4){
					paytype = "现金支付";
				}else if(pay_type == 3){
					paytype = "包月";
				}else if(pay_type == 5){
					paytype = "银联卡";
				}else if(pay_type == 6){
					paytype = "商家卡";
				}else if(pay_type == 8){
					paytype = "免费";
				}
				request.setAttribute("paytype", paytype);
			}
			if(orderMap.get("c_type") != null){
				Integer c_type = (Integer)orderMap.get("c_type");
				String intype = "";
				if(c_type == 0){
					intype = "NFC刷卡";
				}else if(c_type == 1){
					intype = "IBeacon";
				}else if(c_type == 2 || c_type == 3 || c_type == 5){
					intype = "扫描车牌";
				}else if(c_type == 4){
					intype = "非计时";
				}else if(c_type == 6){
					intype = "扫车位二维码";
				}
				request.setAttribute("intype", intype);
			}
			if(orderMap.get("car_number") != null){
				String carnumber = (String)orderMap.get("car_number");
				if(carnumber.length() != carnumber.getBytes().length){
					request.setAttribute("carnumber", carnumber);
				}
			}
			if(orderMap.get("comid") != null){
				Long comid = (Long)orderMap.get("comid");
				request.setAttribute("comid", comid);
				Map comMap = pService.getMap("select company_name from com_info_tb where id=?", new Object[]{comid});
				if(comMap!=null){
					request.setAttribute("cname", comMap.get("company_name"));
				}
				
				//查图片
				Map<String,Object> picMap = pService.getMap("select picurl from com_picturs_tb where comid=? order by id desc limit ?",
						new Object[]{comid,1});
				String picUrls = "";
				if(picMap!=null&&!picMap.isEmpty()){
					picUrls="http://"+Constants.WXPUBLIC_S_DOMAIN+"/zld/"+(String)picMap.get("picurl");
				}
				request.setAttribute("picUrls", picUrls);
			}
			if(orderMap.get("uid") != null){
				Long uid = (Long)orderMap.get("uid");
				request.setAttribute("uid", uid);
				Map<String, Object> uidMap = pService.getMap(
						"select nickname from user_info_tb where id=? ",
						new Object[] { uid });
				if(uidMap != null){
					request.setAttribute("nickname", uidMap.get("nickname"));
				}
				
				Integer reward_flag = 0;
				Long count = pService.getLong(
						"select count(id) from parkuser_reward_tb where uin=? and order_id=? ",
						new Object[] { userMap.get("id"), orderid });
				if(count > 0){
					reward_flag = 1;
				}else{
					count = pService.getLong("select count(*) from parkuser_reward_tb where uin=? and ctime>=? and uid=? ",
									new Object[] { userMap.get("id"), TimeTools.getToDayBeginTime(), uid });
					if(count > 0){
						reward_flag = 2;
					}
				}
				request.setAttribute("reward_flag", reward_flag);
				if(reward_flag == 0){
					request.setAttribute("url", "http://"+Constants.WXPUBLIC_REDIRECTURL+"/zld/wxpfast.do?action=toreward&uid="+uid+"&orderid="+orderid+"&openid="+userMap.get("wxp_openid"));
				}
				logger.error("uid:"+uid+",orderid:"+orderid+",reward_flag:"+reward_flag);
			}
			Map bonusMap = pService.getMap("select * from order_ticket_tb where order_id=? limit ? ", new Object[]{orderid,1});
			if(bonusMap!=null&&!bonusMap.isEmpty()){
				logger.error(">>>有微信红包...");
				request.setAttribute("bonusid", bonusMap.get("id"));
				request.setAttribute("bonus_money", bonusMap.get("money"));
				request.setAttribute("bonus_bnum", bonusMap.get("bnum"));
				request.setAttribute("bonus_type", bonusMap.get("type"));
				
				request.setAttribute("title", CustomDefind.getValue("TITLE"));
				request.setAttribute("desc", CustomDefind.getValue("DESCRIPTION"));
				
				try {
					//微信公众号JSSDK授权验证
					Map<String, String> ret = new HashMap<String, String>();
					ret = publicMethods.getJssdkApiSign(request);
					//jssdk权限验证参数
					request.setAttribute("appid", Constants.WXPUBLIC_APPID);
					request.setAttribute("nonceStr", ret.get("nonceStr"));
					request.setAttribute("timestamp", ret.get("timestamp"));
					request.setAttribute("signature", ret.get("signature"));
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
			return mapping.findForward("horderdetail");
		}else if(action.equals("orderlist")){
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "size", 20);
			String mobile = RequestUtil.processParams(request, "mobile");
			if(mobile.equals("")){
				return mapping.findForward("error");
			}
			List<Map<String, Object>> infoMapList = new ArrayList<Map<String,Object>>();
			Map<String, Object> userMap = pService
					.getMap("select * from user_info_tb where mobile=? and auth_flag=? ",
							new Object[] { mobile, 4 });
			if(userMap != null){
				if(pageNum == 1){
					List<Map<String, Object>> orderList = pService.getAll(
							"select * from order_tb where uin=? and state=? order by create_time desc",
							new Object[] { userMap.get("id"), 0 });
					if(orderList != null){
						for(Map<String, Object> map : orderList){
							Map<String, Object> info = new HashMap<String, Object>();
							Long comid = (Long)map.get("comid");
							Map<String, Object> comMap = pService
									.getMap("select company_name from com_info_tb where id=? ",
											new Object[] { comid });
							if(comMap != null){
								info.put("parkname", comMap.get("company_name"));
							}
							Long ctime = (Long)map.get("create_time");
							Integer pid = (Integer)map.get("pid");
							Integer car_type = (Integer)map.get("car_type");//0：通用，1：小车，2：大车
							if(pid>-1){
								info.put("total", Double.valueOf(publicMethods.getCustomPrice(ctime, System.currentTimeMillis()/1000, pid)));
							}else {
								info.put("total", Double.valueOf(publicMethods.getPrice(ctime, System.currentTimeMillis()/1000, (Long)map.get("comid"), car_type)));
							}
							info.put("date", TimeTools.getTimeStr_yyyy_MM_dd(ctime*1000));
							info.put("orderid", map.get("id"));
							info.put("state", 0);
							infoMapList.add(info);
						}
					}
				}
				
				Map<String, String> params = new HashMap<String, String>();
				params.put("page", pageNum+"");
				params.put("size", pageSize+"");
				params.put("action", "historyroder");
				params.put("mobile", mobile);
				String result = new HttpProxy().doPost("http://"+Constants.WXPUBLIC_REDIRECTURL+"/zld/carowner.do", params);
				if(result != null && !result.equals("")){
					JSONArray jsonArray = JSONArray.fromObject(result);
					for(int i=0;i<jsonArray.size();i++){
						JSONObject jsonObject = (JSONObject)jsonArray.get(i);
						Map<String, Object> info = new HashMap<String, Object>();
						info.put("total", jsonObject.get("total"));
						info.put("date", jsonObject.get("date"));
						info.put("parkname", jsonObject.get("parkname"));
						info.put("orderid", jsonObject.get("orderid"));
						info.put("state", 1);
						infoMapList.add(info);
					}
				}
			}
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMapList));
		}else if(action.equals("parkprod")){
			String openid = RequestUtil.processParams(request, "openid");
			if(openid.equals("")){
				return mapping.findForward("error");
			}
			Map<String, Object> userMap = commonMethods.getUserByOpenid(openid);
			if(userMap == null){
				return mapping.findForward("error");
			}
			request.setAttribute("domain", Constants.WXPUBLIC_REDIRECTURL);
			request.setAttribute("mobile", userMap.get("mobile"));
			request.setAttribute("openid", openid);
			return mapping.findForward("parkprod");
		}else if(action.equals("toprodlist")){
			String openid = RequestUtil.processParams(request, "openid");
			if(openid.equals("")){
				return mapping.findForward("error");
			}
			Map<String, Object> userMap = commonMethods.getUserByOpenid(openid);
			if(userMap == null){
				return mapping.findForward("error");
			}
			request.setAttribute("openid", openid);
			request.setAttribute("mobile", userMap.get("mobile"));
			return mapping.findForward("prodlist");
		}else if(action.equals("tobuyprod")){
			String openid = RequestUtil.processParams(request, "openid");
			Long prodid = RequestUtil.getLong(request, "prodid", -1L);
			Integer type = RequestUtil.getInteger(request, "type", 0);//0:购买 1：续费
			if(openid.equals("") || prodid == -1){
				return mapping.findForward("error");
			}
			Map<String, Object> userMap = commonMethods.getUserByOpenid(openid);
			if(userMap == null){
				return mapping.findForward("error");
			}
			Map<String, Object> pMap = daService.getMap("select p.limitday,p.p_name,c.company_name from product_package_tb p,com_info_tb c where p.comid=c.id and p.id=? ", 
					new Object[]{prodid});
			if(pMap == null){
				return mapping.findForward("error");
			}
			Long exptime = Long.valueOf(pMap.get("limitday") + "");
			String expstr = TimeTools.getTimeStr_yyyy_MM_dd(exptime*1000);
			String[] expstrs = expstr.split("-");
			String btime = TimeTools.getDate_YY_MM_DD();
			Long uin = (Long)userMap.get("id");
			String title = "购买包月";
			if(type == 1){
				Map<String, Object> map = daService.getMap("select e_time from carower_product where uin=? and pid=? order by e_time desc limit ? ", 
						new Object[]{uin, prodid, 1});
				if(map != null){
					Long etime = (Long)map.get("e_time");
					if(etime > TimeTools.getToDayBeginTime()){
						btime = TimeTools.getTimeStr_yyyy_MM_dd(etime*1000);
					}
				}
				title = "包月续费";
			}
			String[] minstrs = btime.split("-");
			request.setAttribute("btime", btime);
			request.setAttribute("mobile", userMap.get("mobile"));
			request.setAttribute("prodid", prodid);
			request.setAttribute("openid", openid);
			request.setAttribute("title", title);
			request.setAttribute("exptime", expstr);
			request.setAttribute("minyear", Integer.valueOf(minstrs[0]));
			request.setAttribute("minmonth", Integer.valueOf(minstrs[1])-1);
			request.setAttribute("minday", Integer.valueOf(minstrs[2]));
			request.setAttribute("maxyear", Integer.valueOf(expstrs[0]));
			request.setAttribute("maxmonth", Integer.valueOf(expstrs[1])-1);
			request.setAttribute("maxday", Integer.valueOf(expstrs[2]));
			request.setAttribute("exptime", expstr);
			request.setAttribute("pname", pMap.get("p_name"));
			request.setAttribute("cname", pMap.get("company_name"));
			
			return mapping.findForward("buyprod");
		}else if(action.equals("getprodprice")){
			Long prodid = RequestUtil.getLong(request, "prodid", -1L);
			String starttime = RequestUtil.processParams(request, "starttime");
			Integer months = RequestUtil.getInteger(request, "months", 1);
			if(prodid == -1 || starttime.equals("")){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Long tondaybegin = TimeTools.getToDayBeginTime();
			Long b =TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(starttime+" 00:00:00");
			if(b < tondaybegin){
				AjaxUtil.ajaxOutput(response, "-2");
				return null;
			}
			Integer r = commonMethods.checkProdExp(prodid, b, months);
			if(r == 1){
				AjaxUtil.ajaxOutput(response, "-3");
				return null;
			}
			Map<String, Object> map = new HashMap<String, Object>();
			Double total= commonMethods.getProdSum(prodid, months);
			map.put("total", total);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(map));
		}else if(action.equals("topayprod")){
			Long prodid = RequestUtil.getLong(request, "prodid", -1L);
			String starttime = RequestUtil.processParams(request, "starttime");
			Integer months = RequestUtil.getInteger(request, "months", 1);
			String openid = RequestUtil.processParams(request, "openid");
			if(prodid == -1 || starttime.equals("") || openid.equals("")){
				return mapping.findForward("error");
			}
			Long tondaybegin = TimeTools.getToDayBeginTime();
			Long b =TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(starttime+" 00:00:00");
			if(b < tondaybegin){
				return mapping.findForward("error");
			}
			Integer r = commonMethods.checkProdExp(prodid, b, months);
			if(r == 1){
				return mapping.findForward("error");
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(b*1000);
			calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+months);
			Long endtime = calendar.getTimeInMillis();
			Map<String, Object> userMap = commonMethods.getUserByOpenid(openid);
			if(userMap == null){
				return mapping.findForward("error");
			}
			Double total= commonMethods.getProdSum(prodid, months);
			Double wx_pay = 0d;
			Double balance_pay = 0d;
			logger.error("topayprod>>>openid:"+openid+",total:"+total+",uin:"+userMap.get("id")+",starttime:"+starttime+",months:"+months);
			Double balance = Double.valueOf(userMap.get("balance") + "");//用户余额
			if(total > balance){
				balance_pay = balance;//余额全部用于支付
				wx_pay = StringUtils.formatDouble(total - balance_pay);
			}else{
				balance_pay = total;
			}
			if(wx_pay > 0){
				try {
					Map<String, Object> attachMap = new HashMap<String, Object>();
					attachMap.put("type", 2);//充值并购买包月
					attachMap.put("starttime", starttime);
					attachMap.put("months", months);
					attachMap.put("prodid", prodid);
					//附加数据
					String attach = StringUtils.createJson(attachMap);
					//设置支付参数
					SortedMap<Object, Object> signParams = new TreeMap<Object, Object>();
					//获取JSAPI网页支付参数
					signParams = PayCommonUtil.getPayParams(request.getRemoteAddr(), wx_pay, "购买包月", attach, openid);
					request.setAttribute("appid", signParams.get("appId"));
					request.setAttribute("nonceStr", signParams.get("nonceStr"));
					request.setAttribute("package", signParams.get("package"));
					request.setAttribute("packagevalue", signParams.get("package"));
					request.setAttribute("timestamp", signParams.get("timeStamp"));
					request.setAttribute("paySign", signParams.get("paySign"));
					request.setAttribute("signType", signParams.get("signType"));
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
			logger.error("topayprod>>>openid:"+openid+",total:"+total+",uin:"+userMap.get("id")+",wx_pay:"+wx_pay+",balance_pay:"+balance_pay);
			request.setAttribute("total", total);
			request.setAttribute("balance_pay", balance_pay);
			request.setAttribute("wx_pay", wx_pay);
			request.setAttribute("starttime", starttime);
			request.setAttribute("endtime", TimeTools.getTimeStr_yyyy_MM_dd(endtime));
			request.setAttribute("months", months);
			request.setAttribute("openid", openid);
			request.setAttribute("mobile", userMap.get("mobile"));
			request.setAttribute("prodid", prodid);
			return mapping.findForward("payprod");
		}
		return null;
	}
	
	/*
	 * 上传认证图片
	 */
	private String uploadCheck2Mongodb (String accessToken, String mediaId,Long uin,String carnumber,int index) throws Exception{
		String requestUrl = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token=ACCESS_TOKEN&media_id=MEDIA_ID";  
        requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken).replace("MEDIA_ID", mediaId);  
		logger.error("upload to mongodb>>>uin:"+uin+",mediaId:"+mediaId+",index:"+index);
		Map<String, String> extMap = new HashMap<String, String>();
	    extMap.put("image/jpeg", ".jpg");
	    extMap.put("image/png", ".png");
	    extMap.put("image/gif", ".gif");
	    extMap.put("video/mpeg4", ".mp4");
		URL url = new URL(requestUrl);  
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
        conn.setDoInput(true);  
        conn.setRequestMethod("GET");
        String ctype = conn.getHeaderField("Content-Type");
		InputStream is = conn.getInputStream(); // 当前上传文件的InputStream对象
		String file_ext =extMap.get(ctype);// 扩展名
		logger.error("upload carnumber pics>>>uin:"+uin+",carnumber:"+carnumber+",file_ext:"+file_ext+",index:"+index);
		if(file_ext != null && !file_ext.equals("") && is != null){
			String picurl = uin + "_"+index+"_"+ System.currentTimeMillis()/1000 + file_ext;
			BufferedInputStream in = null;
			ByteArrayOutputStream byteout =null;
		    try {
		    	in = new BufferedInputStream(is);   
		    	byteout = new ByteArrayOutputStream(1024);        	       
			      
		 	    byte[] temp = new byte[1024];        
		 	    int bytesize = 0;        
		 	    while ((bytesize = in.read(temp)) != -1) {        
		 	          byteout.write(temp, 0, bytesize);        
		 	    }        
		 	      
		 	    byte[] content = byteout.toByteArray(); 
		 	    DB mydb = MongoClientFactory.getInstance().getMongoDBBuilder("zld");
			    mydb.requestStart();
				  
			    DBCollection collection = mydb.getCollection("user_dirvier_pics");
				  
				BasicDBObject document = new BasicDBObject();
				document.put("uin", uin);
				document.put("carid", carnumber);
				document.put("ctime",  System.currentTimeMillis()/1000);
				document.put("type", extMap.get(file_ext));
				document.put("content", content);
				document.put("filename", picurl);
				  //开始事务
				mydb.requestStart();
				collection.insert(document);
				  //结束事务
				mydb.requestDone();
				in.close();        
			    is.close();
			    byteout.close();
			    
			    String sql = "update car_info_tb set pic_url1=?,is_auth=?,create_time=?  where uin=? and car_number=?";
				if(index==1){
					sql = "update car_info_tb set pic_url2=?,is_auth=?,create_time=?  where uin=? and car_number=?";
				}
				int ret = daService.update(sql, new Object[]{picurl,2,System.currentTimeMillis()/1000,uin,carnumber});
				logger.error("上传第"+index+"张图片：ret:"+ret+",uin:"+uin+"carnumber:"+carnumber+",mediaid:"+mediaId);
			} catch (Exception e) {
				e.printStackTrace();
				return "-1";
			}finally{
				if(in!=null)
					in.close();
				if(byteout!=null)
					byteout.close();
				if(is!=null)
					is.close();
			}
		}
		
		return "1";
	}
	
	/** 
     * 获取媒体文件 
     * @param accessToken 接口访问凭证 
     * @param media_id 媒体文件id 
     * @param savePath 文件在服务器上的存储路径 
     * */  
    public void previewpic(String accessToken, String mediaId, HttpServletResponse response) {
        // 拼接请求地址  
        String requestUrl = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token=ACCESS_TOKEN&media_id=MEDIA_ID";  
        requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken).replace("MEDIA_ID", mediaId);  
        System.out.println(requestUrl);  
        try {  
            URL url = new URL(requestUrl);  
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
            conn.setDoInput(true);  
            conn.setRequestMethod("GET");  
            
            BufferedInputStream in = null;  
    		ByteArrayOutputStream byteout =null;
            in = new BufferedInputStream(conn.getInputStream());   
	    	byteout = new ByteArrayOutputStream(1024);        	       
		      
	 	    byte[] temp = new byte[1024];        
	 	    int bytesize = 0;        
	 	    while ((bytesize = in.read(temp)) != -1) {        
	 	          byteout.write(temp, 0, bytesize);        
	 	    }
	 	    byte[] content = byteout.toByteArray(); 
            response.setDateHeader("Expires", System.currentTimeMillis()+4*60*60*1000);
			Calendar c = Calendar.getInstance();
			c.set(1970, 1, 1, 1, 1, 1);
			response.setHeader("Last-Modified", c.getTime().toString());
			response.setContentLength(content.length);
			response.setContentType("image/jpeg");
		    OutputStream o = response.getOutputStream();
		    o.write(content);
		    o.flush();
		    o.close();
		    response.flushBuffer();
        } catch (Exception e) {  
            logger.error("preview picture fail>>>serverid:"+mediaId);
        }  
    } 
}
