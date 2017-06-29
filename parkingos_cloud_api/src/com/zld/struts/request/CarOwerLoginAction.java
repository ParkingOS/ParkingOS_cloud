package com.zld.struts.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zld.AjaxUtil;
import com.zld.easemob.apidemo.EasemobIMUsers;
import com.zld.easemob.main.HXHandle;
import com.zld.impl.CommonMethods;
import com.zld.impl.MemcacheUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SendMessage;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;

public class CarOwerLoginAction extends Action{
	
	@Autowired
	private DataBaseService daService;
	
	private Logger logger = Logger.getLogger(CarOwerLoginAction.class);
	
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private CommonMethods methods;
	
	
	/**
	 *车主登录，注册，验证码处理
	 * 返回码：
	 * 4:注册成功，但推荐码不存在
	 * 3:车牌保存成功
	 * 2:输入车牌号
	 * 1:登录成功，
	 * 0:输入验证码，
	 * -1：验证码无效，
	 * -2：注册失败，
	 * -3：给手机发送验证码失败，
	 * -4：角色错误
	 * -5:手机号码错误
	 * -6：系统验证码不存在
	 * -7:保存验证码错误
	 * -8:车牌保存失败
	 * -9:车牌已存在 
	 * test : http://s.tingchebao.com/zld/carlogin.do?action=login&mobile=15920157107
	 *http://127.0.0.1/zld/carlogin.do?action=login&mobile=15920157107
	 *http://192.168.0.188/zld//carlogin.do?action=validcode&code=1234&mobile=13641309140&hx=1
	 *http://192.168.0.188/zld//carlogin.do?action=addcar&carnumber=苏GHR009&mobile=18101333937
	 *http://s.zhenlaidian.com/zld//carlogin.do?action=validcode&code=6271&mobile=15801682643
	 *新加黑名单处理：2015-03-10 by LaoYao
	 *新注册用户或登录用户上传本机的IMEI号，新注册时，查询IMEI号存在的数量，如果是两个以上，新注册的用户直接放入黑名单，不返券
	 *登录用户上传本机的IMEI号，也查询IMEI号存在的数量，如果是两个以上，该用户直接放入黑名单
	 *同时修改所有红包入口注册及推荐入口，不再直接注册新车主，车主登录时再根据是否是黑名单的用户，决定是否返券
	 *进入黑名单的车主停车时，不再返券，且不给所在车场返现。
	 */
	
	@SuppressWarnings({ "rawtypes"})
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		String mobile =RequestUtil.processParams(request, "mobile");
		String openid =RequestUtil.processParams(request, "openid");
		//logger.error("action:"+action+",mobile:"+mobile);
		/*if(mobile==null||"".equals(mobile)){
			AjaxUtil.ajaxOutput(response, "-5");
			return null;
		}*/
		if(action.equals("dologin")){//车主登录，帐号不存在时，注册上，返回验证码,主动获取验证码（短信获取验证码）
			String sql = "select * from user_info_tb where mobile=? and auth_flag=? ";
			Map user = daService.getPojo(sql, new Object[]{mobile,4});
			String imei  =  RequestUtil.getString(request, "imei").trim();//手机串号
			Map<String,Object> infoMap = new HashMap();//返回值
			if(!publicMethods.isCanSendShortMesg(mobile)){
				infoMap.put("mesg", "-2");
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
				return null;
			}
			Long uin = null;
			if(user==null){
				//注册车主 
				uin= daService.getLong("SELECT nextval('seq_user_info_tb'::REGCLASS) AS newid", null);
				int r = createCarOwerInfo(request, uin);
				if(r==1){
					boolean isBlack = doValidateBlackByImei(imei,uin,mobile);//是否是黑名单车主
					user = daService.getPojo("select * from user_info_tb where id = ?", new Object[]{uin});
					//新用户注册，返3元停车券,有效期15天
					Long time = TimeTools.getToDayBeginTime();
					Long etime = time + 6*24*60*60-1;
					Long ntime = System.currentTimeMillis()/1000;
					if(!isBlack){//不是黑名单车主才返券
						if(!methods.checkBonus(mobile,uin)){//没有红包时，加十元券
							try {
								daService.update("insert into ticket_tb (uin,create_time,limit_day,money,state) values(?,?,?,?,?)",
										new Object[]{uin,time,etime,1,0});
								daService.update("insert into ticket_tb (uin,create_time,limit_day,money,state) values(?,?,?,?,?)",
										new Object[]{uin,time,etime,2,0});
								daService.update("insert into ticket_tb (uin,create_time,limit_day,money,state) values(?,?,?,?,?)",
										new Object[]{uin,time,etime,3,0});
								daService.update("insert into ticket_tb (uin,create_time,limit_day,money,state) values(?,?,?,?,?)",
										new Object[]{uin,time,etime,4,0});
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}else {
						logger.error(">>>>手机串号："+imei+",已存在两个用户以上，手机号："+mobile+",是黑名单用户了....不返券");
					}
					int	eb = daService.update("insert into user_profile_tb (uin,low_recharge,limit_money,auto_cash," +
							"create_time,update_time) values(?,?,?,?,?,?)", 
							new Object[]{uin,10,25,1,ntime,ntime});
					
					//System.out.println(">>>>>>>>>发3元停车券结果："+e+"，有效期至："+TimeTools.getTime_yyyyMMdd_HHmmss(time*1000)+",设置默认支付:"+eb);
				}else {
					infoMap.put("mesg", "-2");
					AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
					return null;
				}
			}else {
				uin = (Long)user.get("id");
			}
			Long role = (Long)user.get("auth_flag");
			if(user!=null&&role==4){
				//发送短信验证码
				Integer code = new Random(System.currentTimeMillis()).nextInt(1000000);
				if(code<99)
					code=code*10000;
				if(code<999)
					code =code*1000;
				if(code<9999)
					code = code*100;
				if(code<99999)
					code = code*10;
				if(code!=null){
					logger.error("code:"+code+",mobile:"+mobile);
					//保存验证码
					//删除已经保存但没有验证过的验证码（已无效的验证码）
					daService.update("delete from verification_code_tb where uin =?",new Object[]{uin});
					int r =daService.update("insert into verification_code_tb (verification_code,uin,create_time,state)" +
							" values (?,?,?,?)", new Object[]{code,uin,System.currentTimeMillis()/1000,0});
					if(r==1){
						infoMap.put("mesg", "0");
						infoMap.put("code", code);
						infoMap.put("tomobile", getToMobile(mobile));
						//移动 联通  106901336275
						//电信是1069004270441
						AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
					}else{
						infoMap.put("mesg", "-7");
						AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
					}
				}else {
					infoMap.put("mesg", "-3");
					AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
				}
			}else {
				infoMap.put("mesg", "-4");
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
			}
		}else if(action.equals("login")){//车主登录，帐号不存在时，注册上，返回验证码
			String ip = StringUtils.getIpAddr(request);
			//logger.error("car user login ip:"+ip);
			boolean isTruck = publicMethods.isTruck(ip);
			if(isTruck){
				AjaxUtil.ajaxOutput(response, "-1");
				logger.error("login action,mobile:"+mobile+",ip="+ip+",正在攻击");
				return null;
			}
				
			String sql = "select * from user_info_tb where mobile=? and auth_flag=? ";
			Map user = daService.getPojo(sql, new Object[]{mobile,4});
//			if(!memcacheUtils.addLock("login_action_"+ip, 1)){//1秒重复请求不处理
//				AjaxUtil.ajaxOutput(response, "-1");
//				logger.error("login action,mobile="+mobile+",小于秒请求，不处理");
//				return null;
//			}
			
			if(!memcacheUtils.addLock("login_action_"+mobile, 1)){//1秒重复请求不处理
				AjaxUtil.ajaxOutput(response, "-1");
				logger.error("login action,mobile="+mobile+",小于秒请求，不处理");
				return null;
			}
			Long uin = null;
			if(user==null){
				//注册车主 
				uin= daService.getLong("SELECT nextval('seq_user_info_tb'::REGCLASS) AS newid", null);
				int r = createCarOwerInfo(request, uin);
				if(r!=1){//注册失败
					AjaxUtil.ajaxOutput(response, "-2");
					return null;
				}
				user = daService.getPojo("select * from user_info_tb where id = ?", new Object[]{uin});
				/*if(r==1){
					//新用户注册，返10元停车券,有效期15天
					Long time = TimeTools.getToDayBeginTime();
					time = time + 16*24*60*60-1;
					Long ntime = System.currentTimeMillis()/1000;
					if(!checkBonus(mobile,uin))//没有红包时，加一张10元券
						daService.update("insert into ticket_tb (uin,create_time,limit_day,money,state) values(?,?,?,?,?)",
							new Object[]{uin,System.currentTimeMillis()/1000,time,10,0});
					int	eb = daService.update("insert into user_profile_tb (uin,low_recharge,limit_money,auto_cash," +
							"create_time,update_time) values(?,?,?,?,?,?)", 
							new Object[]{uin,10,25,1,ntime,ntime});
					int ret =daService.update("insert into user_message_tb(type,ctime,uin,content,title) values(?,?,?,?,?)",
							new Object[]{1,System.currentTimeMillis()/1000,uin,"恭喜您获得一张10元停车券!", "红包提醒"} );
					System.out.println(">>>>>>>>>发10元停车券结果："+ret+"，有效期至："+TimeTools.getTime_yyyyMMdd_HHmmss(time*1000)+",设置默认支付:"+eb);
				}else {
					AjaxUtil.ajaxOutput(response, "-2");
					return null;
				}*/
			}else {
				uin = (Long)user.get("id");
			}
			Long role = (Long)user.get("auth_flag");
			if(user!=null&&role==4){
				//发送短信验证码
				if(!publicMethods.isCanSendShortMesg(mobile)){
					AjaxUtil.ajaxOutput(response, "-7");
					return null;
				}
				Map userCode = daService.getMap("select verification_code from " +
						"verification_code_tb where create_time >? and  uin =? ", new Object[]{TimeTools.getToDayBeginTime(),uin});
				Integer code = 0;
				if(userCode!=null){
					code=(Integer)userCode.get("verification_code");
				}
				if(code==null||code==0){
					code = new SendMessage().sendMessageToCarOwer(mobile);
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
//						Map verificationMap = daService.getPojo("select verification_code from verification_code_tb" +
//								" where uin=? and state=? ", new Object[]{uin,0});
//						logger.error(verificationMap);
						if(r==1)
							AjaxUtil.ajaxOutput(response, "0");
						else{
							AjaxUtil.ajaxOutput(response, "-7");
							return null;
						}
					}
				}else {
					AjaxUtil.ajaxOutput(response, "-3");
				}
			}else {
				AjaxUtil.ajaxOutput(response, "-4");
			}
		}else if(action.equals("validcode")){//验证发回的验证码
			String vcode =RequestUtil.processParams(request, "code");
			//hx=1,登录后返回环信账户及密码，json格式
			Integer hx = RequestUtil.getInteger(request, "hx", 0);
			String sql = "select * from user_info_tb where mobile=? and auth_flag=?";
			Map user = daService.getPojo(sql, new Object[]{mobile,4});
			String result = "-1";
			if(hx==1){
				result="{\"result\":\"-1\",\"errmsg\":\"登录失败\",\"hxname\":\"\",\"hxpass\":\"\"}";
			}
			if(user==null){
				result="-5";
				if(hx==1)
					result="{\"result\":\"-5\",\"errmsg\":\"手机号未注册\",\"hxname\":\"\",\"hxpass\":\"\"}";
				AjaxUtil.ajaxOutput(response, result);
				return null;
			}
			Long uin = Long.valueOf(user.get("id")+"");
			logger.error("hx："+hx+",code:"+vcode+",mobile:"+mobile+" ,uin="+uin);
			Map verificationMap = daService.getPojo("select verification_code from verification_code_tb" +
					" where uin=? and state=? ", new Object[]{uin,0});
			logger.error(verificationMap);
			if(verificationMap==null){
				result="-6";
				if(hx==1)
					result="{\"result\":\"-6\",\"errmsg\":\"验证码错误\",\"hxname\":\"\",\"hxpass\":\"\"}";
				AjaxUtil.ajaxOutput(response, result);
				return null;
			}
			String code = verificationMap.get("verification_code").toString();
			logger.error(code+":"+code.equals(vcode));
			if(code.equals(vcode)){//验证码匹配成功
				String imei  =  RequestUtil.getString(request, "imei").trim();
				//删除验证码表
				daService.update("delete from verification_code_tb where uin =?",new Object[]{uin});
				//更新车主状态 ，在线，保存登录时间
				String updateSql = "update user_info_tb set online_flag=? ,logon_time=? where id=?";
				Object[] values = new Object[]{22,System.currentTimeMillis()/1000,uin};
				daService.update(updateSql, values);
				//是否是黑名单用户
				boolean isBack=doValidateBlackByImei(imei,uin,mobile);
				/*if(!isBack){//不在黑名单内，处理红包登录表，如果有，写入该用户的停车券表中
					if(!methods.checkBonus(mobile,uin)){//没有停车券，默认送一张10元
						Long count = daService.getLong("select count(id) from ticket_tb where uin =? ", new Object[]{uin});
						if(count==0)
							try {
								Long time = TimeTools.getToDayBeginTime();
								Long etime = time + 6*24*60*60-1;
								daService.update("insert into ticket_tb (uin,create_time,limit_day,money,state) values(?,?,?,?,?)",
										new Object[]{uin,time,etime,1,0});
								daService.update("insert into ticket_tb (uin,create_time,limit_day,money,state) values(?,?,?,?,?)",
										new Object[]{uin,time,etime,2,0});
								daService.update("insert into ticket_tb (uin,create_time,limit_day,money,state) values(?,?,?,?,?)",
										new Object[]{uin,time,etime,3,0});
								daService.update("insert into ticket_tb (uin,create_time,limit_day,money,state) values(?,?,?,?,?)",
										new Object[]{uin,time,etime,4,0});
							} catch (Exception e) {
								e.printStackTrace();
							}
					}
				}*/
				
				
				//查询是否已填写过车牌
				String  carNumber =null;
				Map carInfo = daService.getPojo("select car_number from car_info_Tb where uin=? and state=?", new Object[]{uin,1});
				if(carInfo!=null&&carInfo.get("car_number")!=null)
					carNumber = (String)carInfo.get("car_number");
				logger.error(carNumber);
				if(carNumber!=null){
					//处理推荐返现
					/*if(!isBack){//不是黑名单时，处理推荐奖励
						Long recomCode = (Long)user.get("recom_code");
						//handleRecommendCode(uin, recomCode,mobile);
						//用户通过注册月卡会员注册车主，给车场返现
						//handleVipRegister(uin);//2016-09-07
					}*/
					result="1";
				}else{
					//result="2";
					result="1";//暂时不强制输入车牌
				}
			}
			logger.error(mobile+",login,result:"+result);
			AjaxUtil.ajaxOutput(response, result.replace("null", ""));
			return null;
			
		}else if(action.equals("addcar")){//添加车牌号
			 //http://192.168.0.188/zld//carlogin.do?action=addcar&carnumber=苏GHR009&mobile=18101333937
			//publicMethods.sendMessageToThird(21816L, 2000, null, null, null, 1);
			String cn = RequestUtil.processParams(request, "carnumber");//
			String carNumber  = AjaxUtil.decodeUTF8(cn).toUpperCase().trim();
			carNumber = carNumber.replace("I", "1").replace("O", "0");
			Long curTime = System.currentTimeMillis()/1000;
			//Long recomCode = RequestUtil.getLong(request, "recom_code", 0L);
			Map userMap = daService.getMap("select id,strid,recom_code,logon_time,cityid from user_info_Tb where wxp_openid=? and auth_flag=?", new Object[]{openid,4});
			//Object oid = daService.getObject("select id from user_info_Tb where mobile=? and auth_flag=? ", new Object[]{mobile,4}, Long.class);
			Long uin= null;
			Long cityId = null;
			if(userMap!=null&&userMap.get("id")!=null){
				uin = (Long)userMap.get("id");
				cityId=(Long)userMap.get("cityid");
			}
			if(uin==null){
				AjaxUtil.ajaxOutput(response, "-5");
			}else {
				int result = 0;
				
				result = methods.addCarnum(uin, carNumber);
				/*//根据车牌号查用户
				Map carmap = daService.getMap("select uin from car_info_tb where car_number = ?", new Object[]{carNumber});
				if(carmap!=null){
					Long cuin = (Long) carmap.get("uin");
					Map cusermap = daService.getMap("select wxp_openid from user_info_tb where id = ?", new Object[]{cuin});
					if(cusermap!=null){
						String wxpopenid = (String) cusermap.get("wxp_openid");
						if(wxpopenid==null){
							//删除之前用户
							result = daService.update("delete from user_info_tb where id = ?", new Object[]{cuin});
							logger.error("删除老用户:"+result);
							//更新车牌和月卡的用户
							result = daService.update("update car_info_tb set uin = ? where car_number = ?", new Object[]{uin,carNumber});
							logger.error("更新该车牌用户:"+result);
							Map map = daService.getMap("select id from carower_product where uin = ?", new Object[]{cuin});
							result = daService.update("update carower_product set uin = ? where id = ?", new Object[]{uin,map.get("id")});
							logger.error("更新车牌用户月卡:"+result);
						}else{
							//真实用户,查看月卡
							Map prod = daService.getMap("select e_time from carower_product where uin = ?", new Object[]{cuin});
							if(prod!=null){
								Long endTime = (Long) prod.get("e_time");
								Long cTime = TimeTools.getToDayBeginTime();
								logger.error(cTime);
								logger.error("月卡结束日期:"+TimeTools.getTimeStr_yyyy_MM_dd(endTime*1000)+" 当前日期:"+TimeTools.getTimeStr_yyyy_MM_dd(cTime*1000));
								if(cTime<endTime){
									//月卡未过期,不能绑定
									//不能绑定该车牌
									AjaxUtil.ajaxOutput(response, "-9");
									return null;
								}
							}
							//可以绑定
							result = daService.update("update car_info_tb set uin = ? where car_number = ?", new Object[]{uin,carNumber});
							logger.error("过期月卡被绑定:"+result);
							if(result>0){
								result=1;
							}
						}
					}
				}else{
					//新建车牌
					try {
						result=daService.update("insert into car_info_Tb (uin,car_number,create_time)" +
							"values(?,?,?)", new Object[]{uin,carNumber,curTime});
					} catch (Exception e) {
						e.printStackTrace();
						if(e.getMessage().indexOf("car_info_tb_car_number_key")!=-1){
							AjaxUtil.ajaxOutput(response, "-9");
							return null;
						}
					}
				}*/
				
				if(result==1){//车牌写入成功，处理推荐
					//methods.checkBonus(mobile,uin);
					//Long recomCode = (Long)userMap.get("recom_code");
					//最近登录时间，如果为空，为新登录用户，查一下停车券过期情况，如果全部过期，加一张三元停车券
					/*Long logon_time = (Long) userMap.get("logon_time");
					if(logon_time==null){//未登录过
						Long ntime = System.currentTimeMillis()/1000;
						Long countLong = daService.getLong("select count(id) from ticket_tb where uin=? and limit_day>? ", 
								new Object[]{uin,ntime+24*60*60});
						if(countLong==0){//加一张停车券
							int ret = daService.update("insert into ticket_tb (uin,create_time,limit_day,money,state) values(?,?,?,?,?)",
									new Object[]{uin,ntime,ntime+15*24*60*60,3,0});
							logger.error(">>>>>>first login,ticket is invalid,add a 10 yuan ticket :"+ret);
						}
					}*/
					//logger.error(recomCode);
					//if(recomCode!=null&&recomCode>0){
						//推荐人 
						//logger.error(recomCode);
						//List<Long> blackUserList = memcacheUtils.doListLongCache("zld_black_users", null, null);
						//处理推荐
//						if(blackUserList==null||!blackUserList.contains(uin))//不在黑名单中可以处理推荐返现
//							handleRecommendCode(uin, recomCode,mobile);
					//}
					AjaxUtil.ajaxOutput(response, result+"");
				}else 
					AjaxUtil.ajaxOutput(response, "-8");
			}
		}else if(action.equals("addcid")){//客户端登录后，上传CID，用于消息推送给　"个推"
			//http://192.168.199.240/zld/carlogin.do?action=addcid&cid=123456789&mobile=15801482643&hx=1
			String cid = RequestUtil.getString(request, "cid");
			Integer hx = RequestUtil.getInteger(request, "hx",0);
			System.out.println(">>>>>>>>>>>>cid:"+cid+",mobile:"+mobile);
			//环信账户及密码
			String sql = "select * from user_info_tb where mobile=? and auth_flag=?";
			Map user = daService.getPojo(sql, new Object[]{mobile,4});
			String hxName  = (String)user.get("hx_name");
			String hxPass  = (String)user.get("hx_pass");
			boolean ishas = false;//getHxNamePass(user,hxPass);
			if(ishas){
				hxName  = (String)user.get("hx_name");
				hxPass  = (String)user.get("hx_pass");
			}
			Integer ctype = 0;//客户端类型  0:android,1:ios
			if(cid.length()>32)
				ctype=1;
			int result = 0;
			if(!cid.equals(""))
				result =daService.update("update user_info_Tb set cid = ?,client_type=?  where mobile=? and auth_flag=?",
						new Object[]{cid,ctype,mobile,4});
			else {
				result =daService.update("update user_info_Tb set cid = ?  where mobile=? and auth_flag=?",
						new Object[]{cid,mobile,4});
			}
			String ret=result+"";
			if(hx==1)//
				ret ="{\"result\":\""+result+"\",\"hxname\":\""+hxName+"\",\"hxpass\":\""+hxPass+"\",\"wximgurl\":\""+(user.get("wx_imgurl")==null?"":user.get("wx_imgurl"))+"\"}";
			logger.error(ret);
			AjaxUtil.ajaxOutput(response, ret);
		}
		return null;
	}
	
	/**
	 * 处理环信IM,注册车主账户，更新好机友到环信系统，发消息给好机友
	 * @param user
	 * @return
	 * @throws Exception
	 */
	private boolean getHxNamePass(Map user,String pass) throws Exception {
		Long uin = (Long)user.get("id");
		if(pass==null||"".equals(pass))
			pass = publicMethods.getHXpass(uin);
		if(HXHandle.reg("hx"+uin,pass)){
			user.put("hx_name", "hx"+uin);
			user.put("hx_pass", pass);
			int ret = daService.update("update user_info_tb set hx_name=? ,hx_pass=? where id =? ", new Object[]{"hx"+uin,pass,uin});
			logger.error(user.get("mobile")+">>>>>登录，注册环信结果："+ret);
			if(ret==1){
				//注册好机友到环信系统
				List friends = daService.getAll("select euin from user_friend_tb where buin=? and is_add_hx=? ", new Object[]{uin,0});
				List regFriends = daService.getAll("select id from user_info_tb where  id in" +
						"(select euin from user_friend_tb where buin=? and is_add_hx=?) and (hx_name is  null or hx_pass is null)", new Object[]{uin,0});
				logger.error(user.get("mobile")+">>>>>>好友需要更新到环信系统，number:"+(friends==null?"0":friends.size()));
				logger.error(user.get("mobile")+">>>>>>好友注册到环信系统，number:"+(regFriends==null?"0":regFriends.size()));
				List<Long> regUinList = new ArrayList<Long>();
				if(regFriends!=null&&!regFriends.isEmpty()){
					for(int i=0;i<regFriends.size();i++){
						Map uMap = (Map)regFriends.get(i);
						regUinList.add((Long)uMap.get("id"));
					}
				}
				//http://192.168.199.240/zld//carlogin.do?action=validcode&code=1234&mobile=13641309140&hx=1
				if(friends!=null&&!friends.isEmpty()){
					JsonNodeFactory factory = new JsonNodeFactory(false);
					//批量注册的机友
					ArrayNode arrayNode = new ArrayNode(factory);
					//加好友列表
					List<Long> addFriends = new ArrayList<Long>();
					String sql = "update user_info_tb set hx_name=? ,hx_pass=? where id =? ";
					List<Object[]> params = new ArrayList<Object[]>();
					for(int i=0;i<friends.size();i++){
						Map uMap = (Map)friends.get(i);
						Long _uin = (Long)uMap.get("euin");
						if(regUinList.contains(_uin)){//去掉已经注册的机友
							ObjectNode jsonNode = factory.objectNode();
							jsonNode.put("username", "hx"+_uin);
							jsonNode.put("password",  publicMethods.getHXpass(_uin));
							arrayNode.add(jsonNode);
							params.add(new Object[]{"hx"+_uin, publicMethods.getHXpass(_uin),_uin});
						}
						addFriends.add(_uin);
					}
					//批量注册机友
					ObjectNode objectNode = null;
					if(arrayNode.size()>0)
						objectNode=EasemobIMUsers.createNewIMUserBatch(arrayNode);
					if(null!=objectNode){
						String statusCode = JsonUtil.getJsonValue(objectNode.toString(), "statusCode");
						if(statusCode.equals("200")){
							logger.error(user.get("mobile")+"，批量注册好友成功，"+objectNode.toString());
							if(!params.isEmpty()){
								int r = daService.bathInsert(sql, params, new int[]{12,12,4});
								logger.error(user.get("mobile")+"，批量注册好友成功,更新到数据库：ret:"+r);
							}
						}else {
							logger.error(user.get("mobile")+"，批量注册好友失败，"+objectNode.toString());
						}
					}
					if(!addFriends.isEmpty()){
					  	//循环加入好友
						for(Long touin : addFriends){
							objectNode = EasemobIMUsers.addFriendSingle("hx"+uin, "hx"+touin);
							String statusCode = JsonUtil.getJsonValue(objectNode.toString(), "statusCode");
							if(statusCode.equals("200")){
								logger.error(user.get("mobile")+"，加好友成功，好友："+"hx"+touin+","+objectNode.toString());
								int r = daService.update("update user_friend_tb set is_add_hx=? where  buin=? or euin=?  ", new Object[]{1,uin,uin});
								logger.error(user.get("mobile")+">>>>>>更新zld好友表，ret:"+r);
							}else {
								logger.error(user.get("mobile")+"，加好友失败，好友："+"hx"+touin+","+objectNode.toString());
							}
						}
					}
				}
			}
			return true;
		}
		return false;
	}
	/**
	 * 验证手机串号，判断是否是黑名单用户
	 * 查询手机串号是否被注册过，如果有两个以上用户使用过该串号，返回 true;
	 * @param uin
	 * @param imei
	 */
	private boolean doValidateBlackByImei( String imei,Long uin,String mobile) {
		// TODO Auto-generated method stub
		logger.error(">>>>>mobile:"+mobile+",imei:"+imei+",uin:"+uin);
		if("000000000000000".equals(imei))//刷机后的手机串号，不放在黑名单
			return false;
		boolean isBlack = publicMethods.isBlackUser(uin);
		if(isBlack)
			return isBlack;
		if(imei==null||"".equals(imei)||imei.indexOf("null")!=-1)
			return false;
		String sql = "select mobile from user_info_Tb where imei=? and auth_flag=? and id <>?  ";
		Object [] values = new Object[]{imei,4,uin};
		List uList = daService.getAll(sql, values);
		if(uList!=null&&uList.size()>1){
			if(!publicMethods.isAuthUser(uin)){
				//写入黑名单表
				Long ntime = System.currentTimeMillis()/1000;
				try {
					List<Long> whiteUsers = memcacheUtils.doListLongCache("zld_white_users", null, null);
					if(whiteUsers==null||!whiteUsers.contains(uin)){
						int ret = daService.update("insert into zld_black_tb(ctime,utime,uin,state,remark) values(?,?,?,?,?)",
								new Object[]{ntime,ntime,uin,0,"当前手机："+mobile+",手机串号重复："+imei+",已存在手机："+uList});
						logger.error(">>>加入黑名单,uin:"+uin+",imei:"+imei+"，结果 ："+ret);
						if(ret==1){
							//放入黑名单缓存
							List<Long> blackUsers = memcacheUtils.doListLongCache("zld_black_users", null, null);
							if(blackUsers==null){
								blackUsers = new ArrayList<Long>();
								//blackUsers.clear();
								blackUsers.add(uin);
								memcacheUtils.doListLongCache("zld_black_users", blackUsers, "update");
							}else {
								if(!blackUsers.contains(uin)){
									//blackUsers.clear();
									blackUsers.add(uin);
									System.err.println(blackUsers);
									memcacheUtils.doListLongCache("zld_black_users", blackUsers, "update");
								}
							}
							return true;
						}
					}else {
						logger.error(">>>>>>>zld_white_tb>,uin:"+uin+",imei:"+imei+"，在白名单中 ："+whiteUsers);
					}
				} catch (Exception e) {
					logger.error(">>>加入黑名单错误,uin:"+uin+",imei:"+imei+"，已经存在！");
					e.printStackTrace();
				}
			}else{
				logger.error(">>>两个以上用户使用过该串号,但因车主已认证不加入黑名单,uin:"+uin+",imei:"+imei);
			}
		}else {//更新车主账户手机串号
			int ret = daService.update("update user_info_Tb set imei=? where id =? ", new Object[]{imei,uin});
			logger.error(">>>不在黑名单，uin:"+uin+",imei:"+imei+"，更新车主手机串号结果："+ret);
		}
		return false;
	}
	//注册车主信息
	@SuppressWarnings({ "rawtypes" })
	private int createCarOwerInfo(HttpServletRequest request,Long uin){
		Integer media = RequestUtil.getInteger(request, "media", 0);
		String mobile =RequestUtil.processParams(request, "mobile");
		if(mobile.equals("")) mobile=null;
		Map adminMap = daService.getPojo("select * from user_info_tb where comid = ?", new Object[]{0});
		Long time = System.currentTimeMillis()/1000;
		
		String strid = uin+"";
		//用户表
		String sql= "insert into user_info_tb (id,nickname,password,strid," +
				"address,reg_time,mobile,auth_flag,comid,media,cityid) " +
				"values (?,?,?,?,?,?,?,?,?,?,?)";
		Object[] values= new Object[]{uin,"车主",strid,strid,
				adminMap.get("address"),time,mobile,4,0,media,-1L};
		
		int r = daService.update(sql,values);
		if(r==1){
			int	eb = daService.update("insert into user_profile_tb (uin,low_recharge,limit_money,auto_cash," +
					"create_time,update_time) values(?,?,?,?,?,?)", 
					new Object[]{uin,10,25,1,time,time});
			logger.error(">>>>新用户注册，默认写入支付配置...+"+eb);
			
		}
		return r;
	}	
	private String  getToMobile(String mobile){
		//移动 联通  106901336275
		//电信是1069004270441
		//中国电信：133,153,177,180,181,189
		if(mobile.startsWith("133")||mobile.startsWith("153")
				||mobile.startsWith("177")||mobile.startsWith("180")
				||mobile.startsWith("181")||mobile.startsWith("189")
				||mobile.startsWith("170"))
			return "1069004270441";
		return "106901336275";
	}
	
	
	//用户通过注册月卡会员注册的车主，在车主第一次登录的时候给对应的车场返5元
	private void handleVipRegister(Long uin){
		Map<String, Object> map = daService.getMap("select * from recommend_tb where nid=? and type=? and state=? ", new Object[]{uin,0,0});
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		if(map != null){
			Long comid = (Long)map.get("comid");
			Long count = daService.getLong("select count(*) from com_info_tb where id=? and state !=?", new Object[]{comid,1});
			if(count > 0){
				Map<String, Object> parkaccountMap = new HashMap<String, Object>();
				parkaccountMap.put("sql", "insert into park_account_tb(comid,amount,type,create_time,remark,source) values(?,?,?,?,?,?) ");
				parkaccountMap.put("values", new Object[]{comid,5,0,System.currentTimeMillis()/1000,"通过注册月卡会员注册车主_"+uin,3});
				bathSql.add(parkaccountMap);
				
				Map<String, Object> cominfoMap = new HashMap<String, Object>();
				cominfoMap.put("sql", "update com_info_tb set money=money+?,total_money=total_money+? where id=?");
				cominfoMap.put("values", new Object[]{5,5,comid});
				bathSql.add(cominfoMap);
				
				Map<String, Object> recomSqlMap = new HashMap<String, Object>();
				recomSqlMap.put("sql", "update recommend_tb set state=? where nid=? and type=? and state=?");
				recomSqlMap.put("values", new Object[]{1,uin,0,0});
				bathSql.add(recomSqlMap);
				
				daService.bathUpdate(bathSql);
			}
		}
	}
	
/*	private void handleRecommendCode(Long uin,Long recomCode,String mobile){
		//推荐人
		//logger.error(recomCode);
		Map usrMap = daService.getMap("select auth_flag from user_info_tb where id=?", new Object[]{recomCode});
		//推荐人角色
		Long auth_flag = null;
		if(usrMap!=null)
			auth_flag = (Long) usrMap.get("auth_flag");
		if(auth_flag!=null&&(auth_flag==1||auth_flag==2)){//是收费员推荐的车主，目前没有车主推荐车主的记录
			Long count  = daService.getLong("select count(ID) from recommend_tb where nid=? and pid=? and state=? and type=?", new Object[]{uin,recomCode,0,0});
			//推荐类型.0：车主，1:车场
			logger.error("is recom:"+count);
			if(count!=null&&count>0){//被推荐过，推荐人的奖励没有支付//奖励车场收费员账号5元
				
				List<Map<String , Object>> sqlMaps = new ArrayList<Map<String,Object>>();
				Map<String, Object> usersqlMap = new HashMap<String, Object>();
				//收费员账户加5元
				usersqlMap.put("sql", "update user_info_tb set balance=balance+? where id=? ");
				usersqlMap.put("values", new Object[]{5,recomCode});
				sqlMaps.add(usersqlMap);
			
				//写入收费员账户明细
				Map<String, Object> parkuserAccountMap = new HashMap<String, Object>();
				parkuserAccountMap.put("sql", "insert into parkuser_account_tb(uin,amount,type,create_time,remark,target) " +
						"values(?,?,?,?,?,?)");
				parkuserAccountMap.put("values", new Object[]{recomCode,5,0,System.currentTimeMillis()/1000,"推荐奖励",3});
				sqlMaps.add(parkuserAccountMap);
				
				//更新推荐记录
				Map<String, Object> recomsqlMap = new HashMap<String, Object>();
				recomsqlMap.put("sql", "update recommend_tb set state=? where nid=? and pid=?");
				recomsqlMap.put("values", new Object[]{1,uin,recomCode});
				sqlMaps.add(recomsqlMap);
				
				
				
				logger.error(count);
				boolean ret = daService.bathUpdate(sqlMaps);
				if(ret){//写入收费员消息表
					String mobile_end = mobile.substring(7);
					int result =daService.update("insert into parkuser_message_tb(type,ctime,uin,title,content) values(?,?,?,?,?)",
							new Object[]{0,System.currentTimeMillis()/1000, recomCode, "推荐提醒", "你推荐的车主（手机尾号"+mobile_end+"）注册成功，你获得５元奖励。"} );
					logger.error(">>>>>>>>>奖励车场收费员推荐车主5元消息:"+result);
				}
				logger.error(">>>>>>>>>奖励车场收费员推荐车主5元："+ret);
			}
		}else {
			logger.error(recomCode);
		}
	}*/
/*	public static void main(String[] args) {
		Integer code = new Random(System.currentTimeMillis()).nextInt(1000000);
		if(code<99)
			code=code*10000;
		if(code<999)
			code =code*1000;
		if(code<9999)
			code = code*100;
		if(code<99999)
			code = code*10;
		System.out.println(code);
	}*/
}