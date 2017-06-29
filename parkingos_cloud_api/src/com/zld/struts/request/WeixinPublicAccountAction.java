package com.zld.struts.request;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import oracle.jdbc.driver.UpdatableResultSet;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import pay.Constants;
import pay.PayConfigDefind;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.util.Hash;
import com.zld.AjaxUtil;
import com.zld.CustomDefind;
import com.zld.impl.CommonMethods;
import com.zld.impl.MemcacheUtils;
import com.zld.impl.MongoClientFactory;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.HttpProxy;
import com.zld.utils.HttpsProxy;
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
	
	@Autowired
	private MemcacheUtils memcacheUtils;
	
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
			String forward = RequestUtil.processParams(request, "forward");
			String code = RequestUtil.processParams(request, "code");
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>进入微信我的账户>>>>>>>>>>>>>>>,code:"+code+" forward:"+forward);
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
			String wxname = "";
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
				wxname = (String)wxuserinfo.get("nickname");
				String wximg = (String)wxuserinfo.get("headimgurl");
				request.setAttribute("wximg", wximg);
				request.setAttribute("wxname", wxname);
			}
			//其他参数
			
//			String openid = "oRoektybTsv33_vSKKUwLAsJAquc";
//			openid = "oRoekt7uy9abm5hrUBCWYHHDF5sY";
			
			request.setAttribute("openid", openid);
			Map<String, Object> userMap = daService
					.getMap("select * from user_info_tb where auth_flag=? and wxp_openid=? and state=? ",
							new Object[] {4, openid, 0 });
			long uin = -1L;
			if(userMap == null){//未绑定用户帐户，进入用户绑定页面
				//request.setAttribute("action", "wxpaccount.do?action=bind");
				//return mapping.findForward("adduser");
				
				//自动用openid注册一个没有手机号的账户
				Long time = System.currentTimeMillis()/1000;
				uin= daService.getLong("SELECT nextval('seq_user_info_tb'::REGCLASS) AS newid", null);
				String strid = uin+"";
				int update = daService.update("insert into user_info_tb(id,nickname,password,strid," +
				"reg_time,auth_flag,comid,media,cityid,wxp_openid) values(?,?,?,?,?,?,?,?,?,?)", new Object[]{uin,"车主:"+wxname,strid,strid,
						time,4,0,11,-1L,openid});
				if(update==1){
					int	eb = daService.update("insert into user_profile_tb (uin,low_recharge,limit_money,auto_cash," +
							"create_time,update_time) values(?,?,?,?,?,?)", 
							new Object[]{uin,10,25,1,time,time});
					logger.error(">>>>新用户注册，默认写入支付配置...+"+eb);
				}else{
					logger.error("account>>>注册异常,重新注册...");
					return mapping.findForward("account");
				}
				userMap = daService.getMap("select * from user_info_tb where auth_flag=? and wxp_openid=? and state=? ",
						new Object[] {4, openid, 0 });
				logger.error("account>>>自动注册新用户");
			}
			Long count = daService.getLong(
					"select count(id) from car_info_tb where uin=? and state=? ",
					new Object[] { userMap.get("id"), 1 });
			if(count == 0){//进入绑定车牌页面
				request.setAttribute("openid", openid);
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
			if("topresentorderlist".equals(forward)){
				request.setAttribute("domain", Constants.WXPUBLIC_REDIRECTURL);
				return mapping.findForward("presentorderlist");
			}else if("toparkprod".equals(forward)){
				request.setAttribute("domain", Constants.WXPUBLIC_REDIRECTURL);
				return mapping.findForward("parkprod");
			}else{
				return mapping.findForward("account");
			}
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
			String wxname = RequestUtil.processParams(request, "wxname");
			String wximg = RequestUtil.processParams(request, "wximg");
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
			request.setAttribute("wximg", wximg);
			request.setAttribute("wxname", wxname);
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
			request.setAttribute("openid", userMap.get("openid"));
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
				//request.setAttribute("mobile", userMap.get("mobile"));
				return mapping.findForward("tocarnumber");
			}
			return mapping.findForward("error");
		}else if(action.equals("toupload")){
			String carnumber = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "carnumber"));
			logger.error("carnumber:"+carnumber);
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
			/*try {
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
			}*/
			request.setAttribute("carnumber", carnumber);
			request.setAttribute("action", "wxpaccount.do?action=tocarnumber");
			request.setAttribute("domain", Constants.WXPUBLIC_REDIRECTURL);
			request.setAttribute("openid", openid);
			return mapping.findForward("addcarnumber");
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
			//String serverid = RequestUtil.processParams(request, "serverid");
			Long carid = RequestUtil.getLong(request, "carid", -1L);
			String opneid = RequestUtil.processParams(request, "openid");
			//Long curTime = System.currentTimeMillis()/1000;
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
			
			//添加车牌
			int addCarnum = commonMethods.addCarnum(uin, carnumber);
			
			/*//1.根据车牌查询所有
			List<Map> all = daService.getAll("select uin,is_auth,state from car_info_tb where car_number = ?", new Object[]{carnumber});
			for (Map map : all) {
				logger.error(map);
				Long cuin = (Long)map.get("uin");
				Integer state = (Integer) map.get("state");
				Integer isauth = (Integer) map.get("is_auth");
				if(cuin.intValue()==uin&&serverid.equals("")&&state!=0){
					logger.error("车牌已被自己注册");
					AjaxUtil.ajaxOutput(response, "-3");
					return null;
				}
				if(isauth>0){//已认证
					logger.error("车牌已被其他车主认证");
					AjaxUtil.ajaxOutput(response, "-2");
					return null;
				}
				//根据cuin查月卡,如果有未过期的月卡,并且有手机号,则不可绑定
				List<Map> productlist = daService.getAll("select e_time from carower_product where uin = ?", new Object[]{cuin});
				for (Map prod : productlist) {
					Long endTime = (Long) prod.get("e_time");
					if(endTime==null){
						logger.error("缺失月卡过期时间");
						AjaxUtil.ajaxOutput(response, "-1");
						return null;
					}
					Long cTime = TimeTools.getToDayBeginTime();
					logger.error(cTime);
					logger.error("月卡结束日期:"+TimeTools.getTimeStr_yyyy_MM_dd(endTime*1000)+" 当前日期:"+TimeTools.getTimeStr_yyyy_MM_dd(cTime*1000));
					if(cTime<endTime){
						//并且有手机号 才不能绑定
						logger.error("月卡未过期");
						Map uMap = daService.getMap("select mobile from user_info_tb where id = ?", new Object[]{cuin});
						Object obj = uMap.get("mobile");
						if(obj!=null&&StringUtils.isNotNull((String)obj)){
							logger.error("有手机号,不能绑定");
							AjaxUtil.ajaxOutput(response, "-2");
							return null;
						}
						Object obj2 = uMap.get("wxp_openid");
						int result = 0;
						if(obj2!=null&&StringUtils.isNotNull((String)obj2)){
							//无微信,把月卡转给现在的用户,删除此用户
							//删除之前用户
							//删除之前用户
							result = daService.update("delete from user_info_tb where id = ?", new Object[]{cuin});
							logger.error("删除老用户:"+result);
							//更新车牌和月卡的用户
							result = daService.update("update car_info_tb set uin = ? where car_number = ?", new Object[]{uin,carnumber});
							logger.error("更新该车牌用户:"+result);
							Map map2 = daService.getMap("select id from carower_product where uin = ?", new Object[]{cuin});
							result = daService.update("update carower_product set uin = ? where id = ?", new Object[]{uin,map2.get("id")});
							logger.error("更新车牌用户月卡:"+result);
							AjaxUtil.ajaxOutput(response, "1");
							return null;
						}
					}
				}
			}
			
			logger.error(">>>>>>>车牌可以绑定");
			int update = -1;
			if(carid == -1){
				logger.error("添加新车牌carnumber:"+carnumber+",uin:"+uin+",carid:"+carid);
				Map carMap = daService.getMap("select uin from car_info_tb where car_number=? ", new Object[]{carnumber});
				Long count = daService.getLong("select count(id) from car_info_tb where uin=? and state=? ", new Object[]{uin, 1});
				if(count > 3){
					logger.error("车牌已经超过 三个，uin:"+uin+",carnumber:"+carnumber+",count:"+count);
					AjaxUtil.ajaxOutput(response, "-4");
					return null;
				}
				//先删除所有该车牌数据
				Long carcount = daService.getLong("select count(id) from car_info_tb where car_number = ?", new Object[]{carnumber});
				int delCarnum = 1;
				if(carcount>0){
					String delSql = "delete from car_info_tb where car_number= ?";
					delCarnum = daService.update(delSql, new Object[]{carnumber});
				}
				if(delCarnum>0){
					//新加一条
					carid = daService.getkey("seq_ticket_tb");
					String addSql = "insert into car_info_tb(id,car_number,uin,create_time) values(?,?,?,?)";
					update = daService.update(addSql, new Object[]{carid,carnumber,uin,curTime});
					if(update>0){
						logger.error("绑定车牌成功");
						logger.error("注册新车牌，uin:"+uin+",carnumber:"+carnumber+",carid:"+carid);
					}else{
						logger.error("绑定车牌失败");
					}
				}else{
					logger.error("删除旧车牌失败");
				}
				
				
			}else{
				//此通道已关闭
				logger.error("修改车牌carnumber:"+carnumber+",uin:"+uin+",carid:"+carid);
				//1.删除该车牌id!=carid且auth<1的所有条目
				Long delcount = daService.getLong("select count(id) from car_info_tb where car_number = ? and is_auth < ?", new Object[]{carnumber,1});
				int update = 1;
				if(delcount>0){//如果有,删除
					String delSql = "delete from car_info_tb where car_number= ? and is_auth < ?";
					update = daService.update(delSql, new Object[]{carnumber,1});
				}
				if(update>0){
					//2.更新车牌
					int r = daService.update("update car_info_tb set car_number=? where id=? ",
							new Object[] { carnumber, carid });
					if(r>0){
						logger.error("修改车牌，uin:"+uin+",carnumber:"+carnumber+",carid:"+carid);
					}else{
						logger.error("修改失败");
					}
				}else{
					logger.error("删除无效车牌失败");
				}
			}*/
			//上一版添加逻辑
/*			if(carid == -1){
				logger.error("添加新车牌carnumber:"+carnumber+",uin:"+uin+",carid:"+carid);
				Map carMap = daService.getMap("select uin from car_info_tb where car_number=? ", new Object[]{carnumber});
				logger.error(carMap);
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
			}*/
			/*if(carid != -1 && !serverid.equals("")){
				logger.error("upload picture>>>uin:"+uin+",carid:"+carid+",carnumber:"+carnumber+",serverid:"+serverid);
				String meidaId[] = serverid.split(",");
				try {
					Long count = daService.getLong("select count(id) from car_info_tb where car_number=? and is_auth> ?", new Object[]{carnumber, 0});
					logger.error("uin:"+uin+",carid:"+carid+",carnumber:"+carnumber+"count:"+count);
					//该车辆未成功认证
					if(count == 0){
						String accessToken = publicMethods.getWXPAccessToken();
						for(int i=0; i< meidaId.length; i++){
							uploadCheck2Mongodb(accessToken, meidaId[i], uin, carnumber, i);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}*/
			AjaxUtil.ajaxOutput(response, addCarnum+"");
			return null;
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
			//String mobile = RequestUtil.processParams(request, "mobile");
			String openid = RequestUtil.processParams(request, "openid");
			if(openid.equals("")){
				return mapping.findForward("error");
			}
			request.setAttribute("domain", Constants.WXPUBLIC_REDIRECTURL);
			request.setAttribute("openid", openid);
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
			Double prepay = Double.valueOf(orderMap.get("prepaid") + "");
			request.setAttribute("state",state);
			request.setAttribute("btime",TimeTools.getTime_yyyyMMdd_HHmm((Long)orderMap.get("create_time")*1000));
			request.setAttribute("prepay", prepay);
			if(state == 2 && prepay == 0){
				request.setAttribute("escapeUrl", "http://"+Constants.WXPUBLIC_REDIRECTURL+"/zld/wxpfast.do?action=beginprepay&orderid="+orderid+"&openid="+userMap.get("wxp_openid")+"&paytype=1");
			}
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
				/*//Integer c_type = (Integer)orderMap.get("c_type");
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
				request.setAttribute("intype", intype);*/
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
			Integer present = RequestUtil.getInteger(request, "present", -1);
			//String mobile = RequestUtil.processParams(request, "mobile");
			String openid = RequestUtil.processParams(request, "openid");
			if(openid.equals("")){
				return mapping.findForward("error");
			}
			List<Map<String, Object>> infoMapList = new ArrayList<Map<String,Object>>();
			Map<String, Object> userMap = pService
					.getMap("select * from user_info_tb where wxp_openid=? and auth_flag=? ",
							new Object[] { openid, 4 });
			if(userMap != null){
				/*if(pageNum == 1){
					String statequery = "";
					Object[] param = null;
					if(present==1){
						logger.error(">>>>>>>查询用户id为:"+userMap.get("id")+"的在场订单");
						statequery = "state = ?";
						param = new Object[]{ userMap.get("id"), 0 };
					}else{
						logger.error(">>>>>>>查询用户id为:"+userMap.get("id")+"的所有订单");
						statequery = "state in (?,?)";
						param = new Object[] { userMap.get("id"), 0, 2 };
					}
					String sql = "select * from order_tb where uin=? and "+statequery+" order by state,create_time desc";
					logger.error("sql: "+sql);
					List<Map<String, Object>> orderList = pService.getAll(sql,param);
					if(orderList != null){
						for(Map<String, Object> map : orderList){
							Map<String, Object> info = new HashMap<String, Object>();
							Long comid = (Long)map.get("comid");
							Integer state = (Integer)map.get("state");
							Map<String, Object> comMap = pService.getMap("select company_name from com_info_tb where id=? ",
											new Object[] { comid });
							if(comMap != null){
								info.put("parkname", comMap.get("company_name"));
							}
							Long ctime = (Long)map.get("create_time");
							Integer pid = (Integer)map.get("pid");
							//Integer car_type = (Integer)map.get("car_type");//0：通用，1：小车，2：大车
							if(state == 0){
								if(pid>-1){
									info.put("total", Double.valueOf(publicMethods.getCustomPrice(ctime, System.currentTimeMillis()/1000, pid)));
								}else {
									info.put("total", Double.valueOf(publicMethods.getPrice(ctime, System.currentTimeMillis()/1000, (Long)map.get("comid"), car_type)));
								}
							}else if(state == 2){
								info.put("total", map.get("total"));
							}
							info.put("date", TimeTools.getTime_yyyyMMdd_HHmmss(ctime*1000));
							info.put("orderid", map.get("id"));
							info.put("state", state);
							info.put("comid", comid);
							info.put("islocked", map.get("islocked"));
							info.put("lockKey", map.get("lock_key"));
							info.put("openid", userMap.get("wxp_openid"));
							info.put("carnumber", map.get("car_number"));
							logger.error("订单编号:"+map.get("id")+" 支付状态(0未支付|1支付|2逃单):"+state);
							infoMapList.add(info);
						}
					}
				}*/
				if(present!=1){//不是1,查询的是所有订单,以下逻辑为查询历史订单(已结算)
					Map<String, String> params = new HashMap<String, String>();
					params.put("page", pageNum+"");
					params.put("size", pageSize+"");
					params.put("action", "historyroder");
					params.put("openid", openid);
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
			}
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMapList));
		}else if(action.equals("presentorderlist")){
			//查询所有在场订单
			//mobile=>uin=>车牌=>每个车牌在每个车场的最新未结算订单
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "size", 20);
			//String mobile = RequestUtil.processParams(request, "mobile");
			String openid = RequestUtil.processParams(request, "openid");
			if(openid.equals("")){
				return mapping.findForward("error");
			}
			List<Map<String, Object>> infoMapList = new ArrayList<Map<String,Object>>();
			Map<String, Object> userMap = pService
					.getMap("select * from user_info_tb where wxp_openid=? and auth_flag=? ",
							new Object[] { openid, 4 });
			if(userMap==null){
				return mapping.findForward("error");
			}
			Long uin = (Long) userMap.get("id");
			List<String> allCarnumbers = commonMethods.getAllCarnumbers(uin);
			for (String carnum : allCarnumbers) {
				String sql = "select * from order_tb where car_number = ? and state = ? order by create_time desc";
				List<Map> all = daService.getAll(sql, new Object[]{carnum,0});
				for (Map map : all) {
					Map<String, Object> info = new HashMap<String, Object>();
					Long comid = (Long)map.get("comid");
					Integer state = (Integer)map.get("state");
					Map<String, Object> comMap = pService.getMap("select company_name from com_info_tb where id=? ",
									new Object[] { comid });
					if(comMap != null){
						info.put("parkname", comMap.get("company_name"));
					}
					Long ctime = (Long)map.get("create_time");
					//Integer pid = (Integer)map.get("pid");
					//String car_type = (String)map.get("car_type");//0：通用，1：小车，2：大车
					/*if(state == 0){
						if(pid>-1){
							info.put("total", Double.valueOf(publicMethods.getCustomPrice(ctime, System.currentTimeMillis()/1000, pid)));
						}else {
							info.put("total", Double.valueOf(publicMethods.getPrice(ctime, System.currentTimeMillis()/1000, (Long)map.get("comid"), car_type)));
						}
					}else if(state == 2){
						info.put("total", map.get("total"));
					}*/
					
					//去泊链查这笔订单的价格?
					String orderId = (String) map.get("order_id_local");
					Long id = (Long) map.get("id");
					//去泊链查询订单价格,泊链根据本平台配置转发.去支付平台查询价格,支付平台去泊链查询价格,并在本平台生成bolink订单
					//Map<String, Object> bolinkOrder = publicMethods.catBolinkOrder(null, orderId, carnum, comid+"", 2, uin);
					Map<String, Object> presentOrder = publicMethods.catPresentOrderPrice(Long.valueOf(CustomDefind.UNIONID), comid+"", carnum, orderId);
					
					/*Integer state = (Integer)orderMap.get("state");//0没有在场订单 1有在场订单 2已预付过
					if(state==null)
						state = 0;
					if(state>0){
						Integer duration = (Integer)orderMap.get("duration");
						duration = duration==null?1:duration;
						Double prepay = StringUtils.formatDouble(orderMap.get("prepay"));
						Long startTime=Long.valueOf(orderMap.get("start_time")+"");
						request.setAttribute("starttime",TimeTools.getTime_MMdd_HHmm(startTime*1000));
						request.setAttribute("parktime",StringUtils.getTimeString(duration));
						request.setAttribute("beforetotal",prepay);
						request.setAttribute("aftertotal", orderMap.get("money"));
						request.setAttribute("distotal", 0);
						request.setAttribute("prestate", prepay>0?1:0);
						request.setAttribute("pretotal", prepay);
						request.setAttribute("descp", "");
						orderId= orderMap.get("order_id")+"";
						request.setAttribute("isbolink", 1);
						request.setAttribute("park_id", parkId);
					}*/
					
					if(presentOrder!=null){
						logger.error("presentorderlist>>>presentOrder:"+presentOrder);
						int status = (Integer) presentOrder.get("state");
						if(status==1){
							Double money =StringUtils.formatDouble(presentOrder.get("money"));
							info.put("status", status);
							info.put("total", money);
							info.put("prestate", 0);
						}else if(status==2){
							Double money =StringUtils.formatDouble(presentOrder.get("money"));
							Double prepay =StringUtils.formatDouble(presentOrder.get("prepay"));
							info.put("status", status);
							info.put("total", money);
							info.put("prestate", 1);
							info.put("pretotal", prepay);
						}else if(status==3){
							info.put("status", status);
							info.put("total", "未知");
						}else if(status==0){
							info.put("status", status);
							info.put("total", "未知");
						}
					}
					info.put("isthirdpay", PayConfigDefind.getValue("IS_TO_THIRD_WXPAY"));
					info.put("date", TimeTools.getTime_yyyyMMdd_HHmmss(ctime*1000));
					info.put("orderid", map.get("order_id_local"));
					info.put("id",id);
					info.put("state", state);
					info.put("comid", comid);
					info.put("islocked", map.get("islocked"));
					info.put("lockKey", map.get("lock_key"));
					info.put("openid", userMap.get("wxp_openid"));
					info.put("carnumber", carnum);
					logger.error("presentorderlist>>>"+info);
					logger.error("订单编号:"+map.get("id")+" 支付状态(0未支付|1支付|2逃单):"+state);
					infoMapList.add(info);
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
			//月卡续费逻辑
			String openid = RequestUtil.processParams(request, "openid");
			String cardid = RequestUtil.getString(request, "cardid");
			Long cid = RequestUtil.getLong(request, "cid", -1l);
			Long comid = RequestUtil.getLong(request, "comid", -1l);
			String prodid = RequestUtil.getString(request, "prodid");
			Integer type = RequestUtil.getInteger(request, "type", 0);//0:购买 1：续费
			logger.error("tobuyprod openid:"+openid);
			if(openid.equals("")){
				return mapping.findForward("error");
			}
			Map<String, Object> userMap = commonMethods.getUserByOpenid(openid);
			logger.error("tobuyprod userMap:"+userMap);
			if(userMap == null){
				return mapping.findForward("error");
			}
			if(!prodid.equals("")&&!prodid.equals("-1")){
				Map<String, Object> pMap = daService.getMap("select p.limitday,p.p_name,c.company_name from product_package_tb p,com_info_tb c where p.comid=c.id and p.card_id=? ", 
						new Object[]{prodid});
				if(pMap == null){
					return mapping.findForward("error");
				}
				/*Object limit = pMap.get("limitday");
				if(limit!=null){
					Long exptime = Long.valueOf(limit + "");
					String expstr = TimeTools.getTimeStr_yyyy_MM_dd(exptime*1000);
					String[] expstrs = expstr.split("-");
					request.setAttribute("exptime", expstr);
					request.setAttribute("maxyear", Integer.valueOf(expstrs[0]));
					request.setAttribute("maxmonth", Integer.valueOf(expstrs[1])-1);
					request.setAttribute("maxday", Integer.valueOf(expstrs[2]));
				}*/
				request.setAttribute("exptime", -1);
				//request.setAttribute("pname", pMap.get("p_name"));
				request.setAttribute("cname", pMap.get("company_name"));
			}else{
				Map<String, Object> pMap = daService.getMap("select t.company_name as name from carower_product c,com_info_tb t where c.com_id=t.id and c.id = ?", 
						new Object[]{cid});
				request.setAttribute("cname", pMap.get("name"));
				request.setAttribute("exptime", -1);
				request.setAttribute("pname", -1);
				request.setAttribute("maxyear", 2020);
				request.setAttribute("maxmonth", 12);
				request.setAttribute("maxday", 31);
			}
			String btime = TimeTools.getDate_YY_MM_DD();
			Long uin = (Long)userMap.get("id");
			String title = "购买包月";
			if(type == 1){
				Map<String, Object> map = daService.getMap("select e_time from carower_product where id = ?", 
						new Object[]{cid});
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
			request.setAttribute("cardid", cardid);
			request.setAttribute("comid", comid);
			request.setAttribute("mobile", userMap.get("mobile"));
			request.setAttribute("prodid", prodid);
			request.setAttribute("openid", openid);
			request.setAttribute("title", title);
			request.setAttribute("minyear", Integer.valueOf(minstrs[0]));
			request.setAttribute("minmonth", Integer.valueOf(minstrs[1])-1);
			request.setAttribute("minday", Integer.valueOf(minstrs[2]));
			
			return mapping.findForward("buyprod");
		}else if(action.equals("getprodprice")){
			Long prodid = RequestUtil.getLong(request, "prodid", -1L);
			String starttime = RequestUtil.processParams(request, "starttime");
			Integer months = RequestUtil.getInteger(request, "months", 1);
			/*if(prodid == -1 || starttime.equals("")){
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
			}*/
			//未过期,查询价格
			Map<String, Object> map = new HashMap<String, Object>();
			//Double total= commonMethods.getProdSum(prodid, months);
			map.put("total", 100);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(map));
		}else if(action.equals("getlocalprice")){//通过sdk查询月卡续费金额
			//Long prodid = RequestUtil.getLong(request, "prodid", -1L);
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			String starttime = RequestUtil.processParams(request, "starttime");
			String cardid = RequestUtil.processParams(request, "cardid");
			Integer months = RequestUtil.getInteger(request, "months", 1);
			//通过sdk查询月卡记录
			String m = months+"";
			String trade_no = TimeTools.getTimeYYYYMMDDHHMMSS()+comid+m+cardid;
			logger.error("getlocalprice trade_no:"+trade_no);
			//组织参数
			Map<String,String> params = new HashMap<String,String>();
			params.put("service_name", "query_prodprice");
			params.put("card_id", cardid);
			params.put("months",months+"");
			params.put("start_time", starttime);
			params.put("trade_no", trade_no);
			params.put("comid", comid+"");
			//优先查询sdk通道 
			/*Map<String, Object> LoginMap= daService.getMap("select server_ip,local_id from park_token_tb where park_id = ? order by id desc limit 1", 
					new Object[]{comid+""});*/
			List<Map<String, Object>> allLogin = daService.getAll("select server_ip,local_id from park_token_tb where park_id = ?", new Object[]{comid+""});
			int size = allLogin.size();
			logger.error("getlocalprice>>>车场"+comid+"登录终端数量:"+size);
			if(size==0){
				logger.error("getlocalprice 车场未有一个终端登录");
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("total", 0.0);
				map.put("trade_no", trade_no);
				map.put("comid", comid);
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(map));
				return null;
			}
			int nextInt =  new Random().nextInt(size);
			logger.error("getlocalprice 随机选取终端:"+nextInt);
			//对于统一车场多个终端,随机选取一个
			Map<String, Object> LoginMap = allLogin.get(nextInt);
			logger.error("getlocalprice loginMap:"+LoginMap);
			logger.error("getlocalprice query order price SDK登录信息:"+LoginMap);
			String localId = "";
			boolean isSend = false;
			String money = "0";
			if(LoginMap!=null&&!LoginMap.isEmpty()){
				localId = (String) LoginMap.get("local_id");
				params.put("local_id", localId);
				try {
					//本机IP
					String localIp = Inet4Address.getLocalHost().getHostAddress().toString();
					//SDK登录IP
					String serverIp = (String)LoginMap.get("server_ip");
					if(serverIp!=null){
							//HttpProxy httpProxy = new HttpProxy();
							//String result = httpProxy.doPost("http://"+serverIp+"/zld/sendmesg", params);
							String message = StringUtils.createLinkString(params);
							logger.error("getlocalprice message:"+message);
							String url = "http://"+serverIp+"/zld/sendmsgtopark.do?"+message;
							logger.error(url);
							String doGet = new HttpProxy().doGet(url);
							if(doGet!=null){
								org.json.JSONObject jo = new org.json.JSONObject(doGet);
								isSend = (Boolean) jo.get("state");
								logger.error("getlocalprice 查询订单金额："+isSend);
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}else {
				logger.error("getlocalprice 没有查到SDK登录信息....");
			}
			if(isSend){
				try {
					int i = 1;
					long start = System.currentTimeMillis()/1000;
					for(long s=start;s<=start+2;s=System.currentTimeMillis()/1000){
						logger.error("getlocalprice 从缓存查询价格第"+i+"次");
						money = memcacheUtils.getCache(trade_no);
						if(money!=null){
							break;
						}
						try {
							Thread.currentThread().sleep(250);
						} catch (InterruptedException e) {
							logger.error(e.getMessage());
						}
						i++;
					}
					logger.error("getlocalprice 调用sdk查询价格,开始查询缓存中价格："+money);
				} catch (Exception e) {
					logger.error("getlocalprice error!!"+e.getMessage());
				}
			}
			if(money==null){
				logger.error("getlocalprice 获取月卡价格失败");
				money="0.0";
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("total", money);
			map.put("trade_no", trade_no);
			map.put("comid", comid);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(map));
			return null;
		}else if(action.equals("checkparkstatus")){
			//***********************去bolink查询车场是否在bolink登录*******************//
			Long comid = RequestUtil.getLong(request, "comid", -1l);
			Double money = RequestUtil.getDouble(request, "money", -1D);
			String checkUrl = "https://s.bolink.club/unionapi/checkparkstatus";
			//String checkUrl = "https://127.0.0.1:8443/api-web/checkparkstatus";
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("park_id", comid+"");
			paramMap.put("money", money);
			paramMap.put("union_id",CustomDefind.UNIONID);
			logger.error("topayprod>>>url:"+checkUrl+">>>params:"+paramMap);
			String doPost = HttpsProxy.doPost(checkUrl, StringUtils.createJson(paramMap), "UTF-8", 20000, 20000);
			JSONObject ret = JSONObject.fromObject(doPost);
			logger.error("topayprod>>>ret:"+ret);
			Map<String, Object> retMap = new HashMap<String, Object>();
			if(ret!=null){
				int state = ret.getInt("state");
				String errmsg = "";
				if(state!=1){
					errmsg = ret.getString("errmsg");
				}
				retMap.put("state", 1);
				retMap.put("errmsg", errmsg);
				//retMap.put("local_id", ret.getString("local_id"));
				logger.error("topayprod>>>车场"+comid+"登录bolink状态:(1登录/0离线)"+state);
				AjaxUtil.ajaxOutput(response, JSONObject.fromObject(retMap).toString());
				return null;
			}else{
				logger.error("topayprod>>>到泊链网络异常");
				retMap.put("state", 0);
				retMap.put("errmsg", "网络异常");
				AjaxUtil.ajaxOutput(response, JSONObject.fromObject(retMap).toString());
				return null;
			}
		}else if(action.equals("topayprod")){
			Long prodid = RequestUtil.getLong(request, "prodid", -1L);
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			String starttime = RequestUtil.processParams(request, "starttime");
			String cardid = RequestUtil.processParams(request, "cardid");
			Integer months = RequestUtil.getInteger(request, "months", 1);
			String openid = RequestUtil.processParams(request, "openid");
			String trade_no = RequestUtil.processParams(request, "trade_no");
			Double thirdprice = RequestUtil.getDouble(request, "thirdprice", -1d);
			
			if(starttime.equals("") || openid.equals("")){//prodid == -1 || 
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
			boolean isThirdPay = PayConfigDefind.getValue("IS_TO_THIRD_WXPAY").equals("1");
			if(isThirdPay){
				logger.error("topayprod>>>第三方查询月卡价格");
				//调bolink接口查询价格
				wx_pay = thirdprice;
			}else{
				logger.error("topayprod>>>本地查询月卡价格");
				if(total > balance){
					balance_pay = balance;//余额全部用于支付
					wx_pay = StringUtils.formatDouble(total - balance_pay);
				}else{
					balance_pay = total;
				}
			}
			
			//****************************获取车场名称************************//
			/*Map parkMap = daService.getMap("select company_name from com_info_tb where id = ?", new Object[]{comid});
			String parkName  = "";
			if(parkMap!=null){
				parkName = (String) parkMap.get("company_name");
			}
			logger.error("parkname:"+parkName);*/
			//***********************************************************//
			String unionid = CustomDefind.UNIONID;
			logger.error("topayprod 月卡续费金额:"+wx_pay);
			//wx_pay=0.01d;
			if(wx_pay > 0){
				try {
					Map<String, Object> attachMap = new HashMap<String, Object>();
					if(isThirdPay){
						attachMap.put("type", 11);//第三方充值并购买包月
					}else{
						attachMap.put("type", 2);//本地充值并购买包月
						attachMap.put("prodid", prodid);
					}
					attachMap.put("params", starttime+"__"+months+"__"+trade_no+"__"+wx_pay+"__"+comid+"__"+unionid+"__"+cardid);
					String backurl = "http://"+PayConfigDefind.getValue("WXPUBLIC_S_DOMAIN")+"/zld/wxpfast.do?action=thirdsuccess1";
					//附加数据
					String attach = StringUtils.createJson(attachMap);
					logger.error("topayprod>>>attch:"+attach+" 长度:"+attach.length());
					Map<String, String> paramsMap = new HashMap<String, String>();
					paramsMap.put("attch", attach);
					paramsMap.put("unionid", unionid);
					paramsMap.put("fee", wx_pay+"");
					String params = StringUtils.createLinkString(paramsMap);
					//签名
					String sign =  StringUtils.MD5(params+"key="+CustomDefind.UNIONKEY).toUpperCase();
					
					if(isThirdPay){
						//第三方支付,跳转至泊链
						//写库,月卡支付流水
						Map map = daService.getMap("select car_number,member_id from carower_product where card_id = ?", new Object[]{cardid});
						String carnums = "";
						String memberid = "";
						if(map!=null){
							carnums = (String) map.get("car_number");
							memberid = (String) map.get("member_id");
						}
						int update = daService.update("insert into card_renew_tb(trade_no,card_id,amount_receivable,pay_type,car_number,buy_month,comid,create_time,user_id,collector) values(?,?,?,?,?,?,?,?,?,?)", 
								new Object[]{trade_no,cardid,wx_pay,"微信公众号",carnums,months,comid,System.currentTimeMillis()/1000,memberid,"微信公众号"});
						logger.error("topayprod写月卡交易流水:"+update);
						String url = "https://s.bolink.club/unionapi/prepay?fee="+wx_pay+"&unionid="+unionid+"&sign="+sign+"&backurl="+backurl+"&attch="+attach;
						//String url = "http://jarvisqh.vicp.io/api-web/prepay/?fee="+wx_pay+"&unionid="+unionid+"&sign="+sign+"&park_name="+AjaxUtil.encodeUTF8(parkName)+"&backurl="+backurl+"&attch="+attach;
						logger.error("topayprod>>>月卡url:"+url);
						response.sendRedirect(url);
						return null;
					}else{
						//本平台支付
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
					}
				} catch (Exception e) {
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
		}else if(action.equals("lockcar")){
			int ret = -2;
			logger.error(">>>>>>>进入锁定车辆");
			Integer lockstatus = RequestUtil.getInteger(request, "lockstatus", -1);
			Long id = RequestUtil.getLong(request, "orderid", -1L);
			logger.error(">>>>>>>锁定请求(1锁定/0解锁):"+lockstatus+" 云平台订单编号:"+id);
			if(id==-1||lockstatus==-1){
				logger.error(">>>>>>>lockstatus或orderid为空");
				return mapping.findForward("error");
			}
			Integer lockKey = -1;
			//0.当前状态,如果是2,则提示锁定中,;如果是4,则提示解锁中
			String ip = "";
			String localId = null;
			String parkid = "";
			String orderid = "";
			Integer islocked = -1;
			//1.查询订单信息
			Map<String, Object> orderMap = pService.getMap("select * from order_tb where id = ?", new Object[]{id});
			if(orderMap!=null){
				orderid = (String) orderMap.get("order_id_local");
				if(orderMap.get("islocked")!=null){
					islocked = (Integer) orderMap.get("islocked");
				}
				Object object = orderMap.get("lock_key");
				if(object!=null){
					lockKey = Integer.valueOf((String)object);
				}
				Long comid = (Long) orderMap.get("comid");
				parkid = String.valueOf(comid);
			}else{
				//订单不存在
				logger.error(">>>>>>>订单"+id+"不存在");
				return mapping.findForward("error");
			}
			
			//2.查询车场在线状态
			//Map<String, Object> parkTokenMap = pService.getMap("select server_ip,local_id from park_token_tb where park_id = ?", new Object[]{parkid});
			List<Map<String, Object>> allLogin = daService.getAll("select server_ip,local_id from park_token_tb where park_id = ?", new Object[]{parkid});
			int size = allLogin.size();
			if(size==0){
				logger.error("getlocalprice 车场未有一个终端登录");
				ret = 9;
				String json = "{\"state\":"+ret+",\"lockKey\": "+lockKey+"}";
				logger.error(json);
				AjaxUtil.ajaxOutput(response, json);
				return null;
			}
			int nextInt = new Random().nextInt(size);
			logger.error("随机选取终端:"+nextInt);
			//对于统一车场多个终端,随机选取一个
			Map<String, Object> parkTokenMap = allLogin.get(nextInt);
			if(parkTokenMap!=null){
				if(parkTokenMap.get("server_ip")!=null){
					ip = (String) parkTokenMap.get("server_ip");
					localId = (String) parkTokenMap.get("local_id");
					logger.error("转发下行接口地址:"+ip);
					//ip="localhost";
					//ip="106.75.108.6";
				}else{
					//车场不在线
					ret= 9;
				}
				logger.error("server_ip:"+ip);
			}else{
				ret = 9;
				logger.error("server_ip:"+ip);
			}
			
			if(ret==9){
				//ret=10;
				logger.error(">>>>>>>车场"+parkid+"不在线");
			}
			
			//3.锁车或解锁
			if(ret!=9){
				if(lockstatus==1){
					//锁车
					if(islocked==0||islocked==3){
						//可以锁定
						logger.error(">>>>>>>可以锁定,锁定中...");
						lockKey = (int) (Math.random()*9000+1000);
						int lockcar = lockcar(id, lockstatus,lockKey);
						if(lockcar>0){
							//修改成功
							//发送请求
							//ret=1;
							ret = sendmsgtopark(ip, orderid, lockstatus, parkid, id, lockKey,localId);
						}else{
							//修改失败
							logger.error("修改订单锁定状态失败");
							ret = 3;
						}
					}else if(islocked==2){
						//重新锁定
						logger.error(">>>>>>>可以锁定,重新锁定中...");
						int lockcar = lockcar(id, lockstatus,lockKey);
						if(lockcar>0){
							//修改成功
							//发送请求
							//ret=1;
							ret = sendmsgtopark(ip, orderid, lockstatus, parkid, id, lockKey,localId);
						}else{
							//修改失败
							logger.error("修改订单锁定状态失败");
							ret = 3;
						}
					}else{
						//返回已锁定
						ret = 6;
					}
				}else if(lockstatus==0){
					//解锁
					if(islocked==1||islocked==4||islocked==5){
						//可以解锁
						logger.error(">>>>>>>可以解锁,解锁中...");
						int lockcar = lockcar(id, lockstatus,null);
						if(lockcar>0){
							//修改成功
							//发送请求
							//ret=0;
							ret = sendmsgtopark(ip, orderid, lockstatus, parkid, id, null,localId);
						}else{
							//修改失败
							logger.error("修改订单锁定状态失败");
							ret = 5;
						}
					}else{
						//返回未锁定
						ret = 7;
					}
				}
			}
			
			//ret: -2系统异常 -1通知处理失败  0解锁成功  1锁定成功  3锁定失败 5解锁失败 6已锁定 7未锁定 9车场离线
			String json = "{\"state\":"+ret+",\"lockKey\": "+lockKey+"}";
			logger.error(json);
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}
		return null;
	}
	
	/**
	 * 向车场发送通知
	 * @return
	 */
	@SuppressWarnings("static-access")
	private int sendmsgtopark(String ip,String orderid,Integer lockstatus,String parkid,Long id,Integer lockKey,String localId){
		int ret = -1;
		//5.通知收费端
		String url = "http://"+ip+":8080/zld/sendmsgtopark.do?service_name=lock_car&order_id="+orderid+"&is_locked="+lockstatus+"&comid="+parkid+"&id="+id+"&local_id="+localId;
		if(lockKey!=null){
			url += "&lock_key="+lockKey;
		}
		logger.error(">>>>>>>发送通知:url:"+url);
		String doGet = new HttpProxy().doGet(url);
		logger.error(doGet);
		if(doGet!=null){
			Boolean state = false;
			try {
				org.json.JSONObject jo = new org.json.JSONObject(doGet);
				state = (Boolean) jo.get("state");
			} catch (JSONException e) {
				logger.error(e.getMessage());
			}
			if(state){
				//通知处理成功
				int i = 1;
				long start = System.currentTimeMillis()/1000;
				for(long s=start;s<=start+2;s=System.currentTimeMillis()/1000){
					Map<String, Object> orderMap = pService.getMap("select * from order_tb where id = ?", new Object[]{id});
					if(orderMap!=null){
						//locksatatus=1,如果islocked为1,则锁定成功;如果islocked值为2,3,则锁定失败"
						//locksatatus=0,如果islocked为0,则解锁成功;如果islocked值为4,5,则解锁失败
						logger.error(">>>>>>>查询解锁状态第"+i+"次");
						Integer islock = (Integer) orderMap.get("islocked");
						if(lockstatus==1){
							if(islock==1){
								//锁定成功
								logger.error("锁定成功!锁车密码:"+lockKey);
								ret = 1;
								break;
							}else if(islock==2||islock==3){
								//锁定失败
								logger.error("锁定失败");
								ret = 3;
							}
						}else if(lockstatus==0){
							if(islock==0){
								//解锁成功
								logger.error("解锁成功!");
								ret = 0;
								break;
							}else if(islock==4||islock==5){
								//解锁失败
								logger.error("解锁失败");
								ret = 5;
							}
						}
					}
					try {
						Thread.currentThread().sleep(300);
					} catch (InterruptedException e) {
						logger.error(e.getMessage());
					}
					i++;
				}
			}else{
				//通知处理失败
				logger.error(">>>>>>>通知处理失败");
			}
		}
		return ret;
	}
	
	/**
	 * 修改车辆锁定状态
	 * @return
	 */
	private int lockcar(Long id,Integer lockstatus,Integer key){
		int ret = -1;
		String sql = "";
		if(lockstatus==1){
			//锁车
			sql = "update order_tb set islocked = ?, lock_key = ? where id=?";
			ret = daService.update(sql, new Object[]{2,key,id});
		}else if(lockstatus==0){
			//解锁
			sql = "update order_tb set islocked = ? where id=?";
			ret = daService.update(sql, new Object[]{4,id});
		}
		return ret;
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
    
	/**
	 * 消息返回
	 * 
	 * @param mesg
	 * @param data
	 */
	private boolean doBackMessage(String mesg, Channel channel) {
		if (channel != null && channel.isActive()
				&& channel.isWritable()) {
			try {
				logger.error("发消息到SDK，channel:"+channel+",mesg:" + mesg);
				byte[] req= ("\n" + mesg + "\r").getBytes("utf-8");
				ByteBuf buf = Unpooled.buffer(req.length);
				buf.writeBytes(req);
				ChannelFuture future = channel.writeAndFlush(buf);
				return true;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else{
			logger.error("客户端已断开连接...");
		}
		return false;
	}
}
