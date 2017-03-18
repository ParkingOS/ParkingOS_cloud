package com.zld.struts.request;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

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
import com.zld.CustomDefind;
import com.zld.impl.CommonMethods;
import com.zld.impl.MemcacheUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.LogService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.Check;
import com.zld.utils.OrderSortCompare;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.utils.ZldMap;
import com.zld.wxpublic.util.CommonUtil;
/**
 * 车主请求处理
 * @author Administrator
 *
 */
public class CarownerRequestAction extends Action{
	
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Autowired
	private LogService logService;
	@Autowired
	private CommonMethods commonMethods;
	
	private Logger logger = Logger.getLogger(CarownerRequestAction.class);
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Long ntime = System.currentTimeMillis()/1000;
		String mobile =RequestUtil.processParams(request, "mobile");
		String action =RequestUtil.processParams(request, "action");
		logger.info("-----------------------------action:"+action+",mobile="+mobile);
		Long comId= RequestUtil.getLong(request, "comid", -1L);
		Map<String,Object> infoMap = new HashMap<String, Object>();
		Long uin = null;
		Map userMap =null;
		Integer client_type=0;
		if(!"".equals(mobile)){
			userMap = daService.getPojo("select * from user_info_Tb where mobile=? and auth_flag=? ", new Object[]{mobile,4});
			if(userMap!=null){
				uin =(Long) userMap.get("id");
				if(userMap.get("client_type")!=null)
					client_type = (Integer)userMap.get("client_type");
//				Integer unionState = (Integer)userMap.get("union_state");
//				if(unionState==0){//没有上传到泊链
//					List carList= pService.getAll("select car_number from car_info_tb where uin = ? and state=? ", new Object[]{uin,1});
//					if(carList!=null&&!carList.isEmpty()){//有车牌需要同步到泊链平台
//						publicMethods.syncUser2Bolink(carList,StringUtils.formatDouble(userMap.get("balance")));
//					}
//				}
			}else {
				infoMap.put("info", "mobile is invalid");
			}
		}else if(comId==-1&&action.indexOf("bonus")==-1){
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
			return null;
		}
		logger.error("-----------------------------action:"+action+",uin="+uin+",mobile="+mobile+",client_type:"+client_type);
		if(action.equals("orderdetail")){//orderdetail:订单详情（历史订单）==
			infoMap = orderDetail(request,uin);
			String info = StringUtils.createJson(infoMap);
			info = info.replace("null", "");
			AjaxUtil.ajaxOutput(response, info );
			return null;
			//订单详情（历史订单）http://127.0.0.1/zld/carowner.do?action=orderdetail&orderid=786121&mobile=15801482643
		}else if(action.equals("historyroder")){//historyroder:历史订单，==
			List<Map<String, Object>> infoMapList = historyOrder(request,uin);
			//Long _total = daService.getLong("select count(*) from order_tb where state=? ", new Object[]{1});
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMapList));
			return null;
			//历史订单，http://127.0.0.1/zld/carowner.do?action=historyroder&page=1&size=10&mobile=18101333937
		}else if(action.equals("detail")){//detail:个人信息，==
			infoMap = detail(mobile,userMap);
			//个人信息，http://127.0.0.1/zld/carowner.do?action=detail&mobile=1
		}else if(action.equals("bonusinfo")){
			List<Map<String, Object>> list = daService.getAll("select id,exptime,type,btime  from order_ticket_tb where uin = ? and money> ? order by id desc limit ?", new Object[]{uin,0,15});
			if(list!=null){
				setBonusType(list);
			}
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(list));
			//个人信息，http://127.0.0.1/zld/carowner.do?action=bonusinfo&mobile=13641309140
			return null;
		}else if(action.equals("parkdetail")){//parkdetail:停车场信息，==
			infoMap = parkDetail(request,comId,uin);
			//停车场信息，http://127.0.0.1/zld/carowner.do?action=parkdetail&comid=3
		}else if(action.equals("parkproduct")){//parkproduct:停车场的包月产品==
			List<Map<String, Object>> infoLists = parkProduct(comId,uin);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoLists));
			//AjaxUtil.ajaxOutput(response, StringUtils.createXML(infoLists));
			return null;
			//停车场包月产品 http://127.0.0.1/zld/carowner.do?action=parkproduct&comid=3&mobile=15801482643
		}else if(action.equals("buyproduct")){//buyproduct:购买包月，==
			int result  = buyProduct(request,uin);
			AjaxUtil.ajaxOutput(response, result+"");
			return null;
			//购买包月，http://127.0.0.1/zld/carowner.do?action=buyproduct&productid=1&mobile=15801482643
		}else if(action.equals("currorder")){//currorder：当前订单，只一条或没有,==
			infoMap = currOrder(request,uin);
			//当前订单，只一条或没有,http://127.0.0.1/zld/carowner.do?action=currorder&mobile=15801482643
		}else if(action.equals("currentorder")){//currorder：当前未结算订单，只一条或没有,==
			String from = RequestUtil.getString(request, "from");
			infoMap = currentOrder(request,uin);
			String result = StringUtils.createJson(infoMap);
			if(from.equals("qr")){
				result="{\"type\":\"2\",\"info\":"+result+"}";
				logger.error(">>扫二维吗查订单："+result);
			}else if(from.equals("qrpark")){
				result="{\"type\":\"5\",\"info\":"+result+"}";
				logger.error(">>扫二维吗结算订单："+result);
			}
			AjaxUtil.ajaxOutput(response,result);
			return null;
			//当前订单，只一条或没有,http://127.0.0.1/zld/cparowner.do?action=currentorder&mobile=15801482643
		}else if(action.equals("buyticket")){
//			String result = buyTicket(request,uin);
//			AjaxUtil.ajaxOutput(response, result);
			AjaxUtil.ajaxOutput(response, "0");
			return null;
			//购买停车券： http://192.168.199.240/zld/carowner.do?action=buyticket&mbile=18101333937&value=10&number=2
		}else if(action.equals("products")){//products:我已买的包月产品==
			List<Map<String, Object >> infoList = products(request,uin);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoList));
			return null;
			//我的包月产品  http://127.0.0.1/zld/carowner.do?action=products&mobile=15375242041 ==
		}
		else if(action.equals("getprofile")){//查个人设置
			infoMap = getProfile(uin);
			//查个人设置 http://127.0.0.1/zld/carowner.do?action=getprofile&mobile=15801270154
		}else if(action.equals("payorder")){
			//version=2，返回带红包的json数据 
			String ret = payOrder(request,comId,uin);
			AjaxUtil.ajaxOutput(response, ret);
			return null;
			//余额支付　 http://127.0.0.1/zld/carowner.do?action=payorder&mobile=15375242041&ptype=1支付包月产品(&productid=number=&start=),
				//2支付订单(&orderid=&total=)
		}else if(action.equals("prepay")){
			String ret = prepay(request,comId,uin);
			AjaxUtil.ajaxOutput(response, ret);
			return null;
			//http://wang151068941.oicp.net/zld/carowner.do?action=prepay&mobile=18201517240&comid=3251&orderid=829931&money=0.01&ticketid=
		}else if(action.equals("paying")){//正在支付,给收费员发消息，提示车主正在手机支付
			//收费员帐号
			Long orderId = RequestUtil.getLong(request, "orderid", -1L);
			Map orderMap = daService.getMap("select * from order_tb where id=?", new Object[]{orderId});
			Long btime = (Long)orderMap.get("create_time");
			Long etime  = (Long)orderMap.get("end_time");
			String duration = StringUtils.getTimeString(btime, etime);
			String carNumber =  publicMethods.getCarNumber(uin);
			Long uid = (Long)orderMap.get("uid");
			//给收费员消息，提示正在手机支付....
			logService.insertParkUserMessage(comId, 1, uid, carNumber, orderId,  Double.valueOf(orderMap.get("total")+""),
					duration, 0, btime, etime, 0, null);
			return null;
			//支付中 http://127.0.0.1/zld/carowner.do?action=paying&mobile=15801482643&orderid=1066
		}else if(action.equals("accountdetail")){//帐户明细
			List<Map<String,Object>> allList =accountDetail(request,uin);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(allList));
			return null;
			///帐户明细 http://127.0.0.1/zld/carowner.do?action=accountdetail&mobile=15375242041&type=2//2全部，0：充值，1：消费
		}
		else if(action.equals("profile")){//个人设置
			//table=user_profile_tb;
			//个人设置 http://127.0.0.1/zld/carowner.do?action=profile&mobile=15801482643&
			if(uin==null){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			int result = setProfile(request,uin);
			AjaxUtil.ajaxOutput(response, result+"");
			return null;
		}else if(action.equals("setprof")){//客户端1.0.20以上版本的个人设置接口
			if(uin==null){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			int result = setPro(request, uin);
			AjaxUtil.ajaxOutput(response, result+"");
			//http://127.0.0.1/zld/carowner.do?mobile=15801270154&action=setprof&low_recharge=10&limit_money=0
			return null;
		}else if(action.equals("getprof")){//客户端1.0.20以上版本的个人设置接口
			//Map<String, Object> infoMap = new HashMap<String, Object>();
			infoMap = getProf(request,uin);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
			return null;
			//http://127.0.0.1/zld/carowner.do?mobile=15801482643&action=getprof
		}else if(action.equals("editcarnumber")){//编辑车牌号
			int result = editCarNumber(request,uin);
			AjaxUtil.ajaxOutput(response, result+"");
			return null;
			//编辑车牌号 http://127.0.0.1/zld/carowner.do?mobile=15801270154&action=editcarnumber&carnumber=%25E4%25BA%25ACg23667
		
		}else if(action.equals("eidtphone")){//修改电话
			int result = editPhone(request, uin,mobile);
			AjaxUtil.ajaxOutput(response, result+"");
			return null;
			//修改电话 http://127.0.0.1/zld/carowner.do?action=eidtphone&mobile=13332223333&newmobile=15822224452
		}else if(action.equals("logout")){//退出
			int result = 0;
			if(uin!=null)
				result = daService.update("delete from user_session_tb where uin = ?",	new Object[]{uin});
			AjaxUtil.ajaxOutput(response, result+"");
			return null;
			//退出 http://127.0.0.1/zld/carowner.do?action=logout&mobile=13332223333
		}else if(action.equals("praise")){//评价停车场
			int result = praise(request,comId,uin);
			AjaxUtil.ajaxOutput(response,result+"");
			return null;
			// 评价停车场(贬、赞)http://127.0.0.1/zld/carowner.do?action=praise&comid=3&praise=0&mobile=
		}else if(action.equals("comment")){//写入评价
			int result = comment(request,comId,uin);
			AjaxUtil.ajaxOutput(response, result+"");
			return null;
			//评论停车场 http://127.0.0.1/zld/carowner.do?action=comment&comid=3&mobile=15375242041&comment=iunfsakehrej3245324
		}else if(action.equals("getcomment")){//读取评价
			List<Map<String, Object>> resultMap = getComment(request,comId);
			//读取停车场评论 put  http://127.0.0.1/zld/carowner.do?action=getcomment&comid=3&mobile=15375242041
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(resultMap));
			return null;
		}else if(action.equals("gettickets")){
			String tickets = getTickets(request,uin,mobile);
			AjaxUtil.ajaxOutput(response, tickets);
			return null;
			//查代金券 http://127.0.0.1/zld/carowner.do?action=gettickets&mobile=13641309140
		}else if(action.equals("usetickets")){
			List<Map<String, Object>> ticketMap = daService.getAll("select id,create_time beginday ,limit_day limitday,state,money  from ticket_tb where uin = ? and state=? and limit_day>?",
					new Object[]{uin,0,ntime});
			if(ticketMap==null||ticketMap.isEmpty())
				AjaxUtil.ajaxOutput(response, "[]");
			else {
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(ticketMap));
			}
			return null;
			//查代金券 http://127.0.0.1/zld/carowner.do?action=usetickets&mobile=15375242041 
		}else if(action.equals("getaccount")){
			//返回余额及可用停车券
			//查代余额及代金券 http://127.0.0.1/zld/carowner.do?action=getaccount&mobile=18101333937&total=2.8&ptype=1&uid=21694&utype=1
			Object balance = userMap.get("balance");
			String ret = getAccount(request,balance,uin,mobile);
			//处理没有返回到账户中的停车券
			commonMethods.checkBonus(mobile, uin);
			AjaxUtil.ajaxOutput(response, ret);
			//查代余额及代金券 http://127.0.0.1/zld/carowner.do?action=getaccount&mobile=18101333937&total=3&ptype=4&uid=21694
			return null;
		}else if(action.equals("isvip")){//是否是速通卡用户
			Long count = daService.getLong("select count(*) from con_nfc_tb where uin =? ", new Object[]{uin});
			AjaxUtil.ajaxOutput(response, count+"");
			return null;
		}else if(action.equals("epay")){
			String ret = epay(request,uin);
			AjaxUtil.ajaxOutput(response, ret);
			//查代金券 http://127.0.0.1/zld/carowner.do?action=epay&mobile=15375242041&uid=&total=&ticketid=
			return null;
		}else if(action.equals("getparkusers")){//车主点击“付车费”
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			List userliList = daService.getAll("select id,nickname as name,online_flag online from user_info_tb where " +
					"comid=? and  state=? and isview=? and auth_flag in(?,?) " +
					"order by online desc  nulls last, name desc  nulls last", 
					new Object[]{comid,0,1,1,2});
			//System.out.println(userliList);
			String result = StringUtils.createJson(userliList);
			AjaxUtil.ajaxOutput(response,result.replace("null", "") );
			return null;
			//查代金券 http://127.0.0.1/zld/carowner.do?action=getparkusers&comid=1197&mobile=15375242041
		}else if(action.equals("getpkuser")){
			String result = getPKUser(request,comId,uin);
			AjaxUtil.ajaxOutput(response,result.replace("null", "") );
			return null;
			//查代金券 http://127.0.0.1/zld/carowner.do?action=getpkuser&uid=10700&mobile=15375242041
		}else if(action.equals("regcarmsg")){//短信推荐车场
			String ret =regCarMsg(request,uin);
			AjaxUtil.ajaxOutput(response, ret);
			//http://127.0.0.1/zld/carowner.do?action=regcarmsg&mobile=15375242041
			return null;
		}else if(action.equals("hbonus")){//查询是否有节日红包
			String ret = hBonus(request,uin);
			AjaxUtil.ajaxOutput(response, ret);
			return null;
			//http://127.0.0.1/zld/carowner.do?action=hbonus&mobile=15801482643
		}else if(action.equals("obparms")){//分享订单红包时，请求参数
			String ret = oBparms(request);
			AjaxUtil.ajaxOutput(response, ret);
			return null;
			//http://127.0.0.1/zld/carowner.do?action=obparms&mobile=15801482463&bid=1
		}else if(action.equals("hbparms")){//分享节日红包时，请求参数
			String title = CustomDefind.getValue("TITLE");
			String description = CustomDefind.getValue("DESCRIPTION");
			AjaxUtil.ajaxOutput(response, "{\"imgurl\":\"images/bonus/weixilogo_300.png\",\"title\":\""+title+"\"," +
					"\"description\":\""+description+"\",\"url\":\"carowner.do?action=gethbonus\"}");
			return null;
			//http://127.0.0.1/zld/carowner.do?action=hbparms&mobile=15375242041
		}else if(action.equals("pusercomment")){//车主对收费员的评论
			String ret = commpuser(request);
			AjaxUtil.ajaxOutput(response, ret);
			return null;
			//车主对收费员的评论 http://192.168.199.240/zld/carowner.do?action=pusercomment&mobile=18101333937&orderid=&comment=
		}else if(action.equals("puserreward")){//车主对收费员的打赏
			String ret = reward(request);
			AjaxUtil.ajaxOutput(response, ret);
			return null;
			//车主对收费员的评论  http://192.168.199.240/zld/carowner.do?action=puserreward&mobile=18101333937&orderid=786820&ticketid=38875&uid=11802&money=2
		}else if(action.equals("sweepticket")){
			String result= sweepTicket(request,uin,mobile);
			AjaxUtil.ajaxOutput(response,result);
			return null;
			//http://192.168.199.239/zld/carowner.do?action=sweepticket&codeid=100253&mobile=18201517240;
		}else if(action.equals("recominfo")){//取推荐记录
			List list = daService.getAll("select nid uin,state from recommend_tb where pid=? and type=? ",new Object[]{uin,1});
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(list));
			return null;
			//http://127.0.0.1/zld/carowner.do?action=recominfo&mobile=15801482643
		}else if(action.equals("getmesg")){
			String ret = getMesg(request,uin);
			AjaxUtil.ajaxOutput(response, ret);
			//http://127.0.0.1/zld/carowner.do?action=getmesg&mobile=15801482643&page=1
			return null;
		}else if(action.equals("getrecomurl")){
			AjaxUtil.ajaxOutput(response, "http://t.cn/RZuFpVJ");
			return null;
			//http://127.0.0.1/zld/carowner.do?action=getrecomurl&mobile=15801482643
		}else if(action.equals("gethbonus")){//领取节日红包、广告红包
			//http://127.0.0.1/zld/carowner.do?action=gethbonus&id=9
			Long id =RequestUtil.getLong(request, "id",3L);
			if(id>12&&id!=999){//不存在的红包
				AjaxUtil.ajaxOutput(response, "对不起，你要领的礼包不存在 ！！！！");
				return null;
			}
			Long uid =RequestUtil.getLong(request, "uid",-1L);//推荐人，当id=999时（收费员推荐车主）
			logger.error("广告红包，到领取页面....收费员uid:"+uid+",红包id:"+id);
			if(mobile.equals("")){
				request.setAttribute("type", "-2");//领取页面，在分享链接中打开 
				request.setAttribute("action",action);//领取页面，在分享链接中打开 
				request.setAttribute("id",id);//领取页面，在分享链接中打开 
				request.setAttribute("uid",uid);//领取页面，在分享链接中打开 
				return mapping.findForward("success");
			}
			if(!Check.checkMobile(mobile)){
				logger.error("广告红包，手机不合法...."+mobile);
				request.setAttribute("type", "-4");//手机号不合法
				request.setAttribute("action",action);//领取页面，在分享链接中打开 
				request.setAttribute("id",id);//领取页面，在分享链接中打开 
				request.setAttribute("mobile",mobile);//手机号
				return mapping.findForward("success");
			}
			//一个红包只能领取一次
			String qsql = "select amount from bonus_record_tb where bid= ? and mobile=? ";
			Object []values =new Object[]{id,mobile};
			if(id==3){//节日红包，一天可以领一次
				qsql += " and ctime>?";
				values =new Object[]{id,mobile,TimeTools.getToDayBeginTime()};
			}
			Map buMap  = daService.getMap(qsql,values);
			int t =0;
			if(buMap!=null)
				t = (Integer)buMap.get("amount");
			if(t>0){//已经领取过
				request.setAttribute("type", "-3");//已经领取过
				request.setAttribute("amount", t);//已经领取过
				request.setAttribute("uphone", mobile);
				request.setAttribute("action",action);//领取页面，在分享链接中打开 
				request.setAttribute("id",id);//领取页面，在分享链接中打开 
				logger.error("广告红包，已领取过....mobile="+mobile+",money:"+t);
				return mapping.findForward("bonusm");
			}
			String bsql = "insert into bonus_record_tb (bid,ctime,mobile,state,amount) values(?,?,?,?,?) ";
			String tsql = "insert into ticket_tb (create_time,limit_day,money,state,uin) values(?,?,?,?,?) ";
			if(uin!=null&&uin>0){//老用户
				if(id==12){//合作红包,老用户只要是第一次领取，也给30元
					int ret = backTickets(uin, 3);
					request.setAttribute("amount", 30);
					request.setAttribute("uphone", mobile);
					request.setAttribute("type", 1);
					logger.error(">>>>合作红包,老用户写入停车券："+ret);
					if(ret>0){
						daService.update(bsql, new Object[]{id,ntime,mobile,1,30});
					}
				}else {
					logger.error("广告红包，老用户("+uin+")....mobile:"+mobile);
					Integer [] amounts = new Integer[]{1,3,3,3,3,1,1,3,3,3,3,3,1,3,3,3,3,1,3,1,3,3,1,1,3,3,3,3,3,1,3,3,2,3,3,1,3,3,3,3,3,1,3,3,3,3,3,1,3,3,2,3,1,3,2,3,3,1,3,2,3,3,3,1,3,3,2,3,1,3,2,3,3,1,3,3,3,1,2,3,3,3,3,1,3,3,3,2,1,3,2,3,1,3,3,3,1,3,3,1};
					Integer index = memcacheUtils.readGetHBonusCache();
					Integer amount = amounts[0];
					if(index!=null){
						amount= amounts[index];
						index++;
						if(index==100)
							index=0;
					}else {
						index=0;
					}
					memcacheUtils.doIntegerCache("hbonus_index", index, "update");
					List<Map<String, Object>> sqls = new ArrayList<Map<String,Object>>();
					Map<String, Object>	bMap = new HashMap<String, Object>();
					bMap.put("sql", bsql);
					bMap.put("values",new Object[]{id,ntime,mobile,1,amount});
					sqls.add(bMap);
					
					Map<String, Object>	tMap = new HashMap<String, Object>();
					tMap.put("sql", tsql);
					tMap.put("values",new Object[]{ntime,ntime+6*24*60*60,amount,0,uin});
					sqls.add(tMap);
					boolean ret = daService.bathUpdate(sqls);
					if(ret){
						logService.insertUserMesg(1, uin, "恭喜您获得一张"+amount+"元停车券!", "礼包提醒");
					}
					logger.error("getobonus:"+ret);	
					request.setAttribute("amount", amount);
					request.setAttribute("uphone", mobile);
					logger.error("广告红包，老用户返"+amount+"...."+mobile);
				}
				
			}else if(!mobile.equals("")){//新用户
				Long uinLong = publicMethods.regUser(mobile, id,uid,false);
				Integer money = 10;
				if(id==12)
					money=30;
//				if(id==8||id==7)
//					money=100;
				Object [] _values = new Object[]{id,ntime,mobile,0,money};
//				if(regUser(mobile, id,uid)>0){
//					_values = new Object[]{id,ntime,mobile,1,money};
//				}
				int ret = daService.update(bsql,_values );
				logger.error("广告红包，新用户返"+money+"....mobile:"+mobile+",用户账户："+uinLong);
				//int ret = daService.update(bsql, new Object[]{id,ntime,mobile,0,30});
				logger.error("getobonus:"+ret);	
				request.setAttribute("amount", money);
				request.setAttribute("uphone", mobile);
				request.setAttribute("type", 1);
			}
			request.setAttribute("action",action);//领取页面，在分享链接中打开 
			request.setAttribute("id",id);//领取页面，在分享链接中打开 
			return mapping.findForward("bonusm");
		}else if(action.equals("getobonus")){//领取订单红包
			//http://127.0.0.1/zld/carowner.do?action=getobonus&id=1
			Long bid = RequestUtil.getLong(request, "id",-1L);
			String operate = RequestUtil.getString(request, "operate");
			if(operate.equals("")){
				//String ret = getOrderBonus(bid, request);
				//String location ="https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx08c66cac888faa2a&redirect_uri=http%3A%2F%2Fwww.tingchebao.com%2Fzld%2Fcarowner.do%3Faction%3Dgetobonus%26id%3D"+bid+"%26operate%3Dcaibonus&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
				int ret = doPreGetBonus(request, bid);
				if(ret==1){//有红包，也没有过期
					String location ="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXPUBLIC_APPID+"&redirect_uri=http%3A%2F%2F"+Constants.WXPUBLIC_REDIRECTURL+"%2Fzld%2Fcarowner.do%3Faction%3Dgetobonus%26id%3D"+bid+"%26operate%3Dcaibonus&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
					//用户授权
					response.sendRedirect(location);
					return null;
				}else {
					request.setAttribute("isover", 0);
					return mapping.findForward("caibouns");
				}
//				return mapping.findForward("bounsret");
				//request.setAttribute("bid", bid);
				//return mapping.findForward(ret);
			}else if(operate.equals("caibonus")){//拆红包
				String auth_range = RequestUtil.getString(request, "authrange");
				String []wxkeys = getOpenid(request);
				if(wxkeys == null){
					return mapping.findForward("error");
				}
				Map user  = getMobileByOpenid(wxkeys[0]);
				String wximgurl =null;
				if(user!=null)
					wximgurl =(String)user.get("wx_imgurl");
				
				//非授权微信
				if(auth_range.equals("")){
					//没有微信头像及微信名称，需要用户授权
					if(user==null||user.get("wx_name")==null||wximgurl==null || wximgurl.length()<1 ){
						//用户授权
						String authurl =   "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXPUBLIC_APPID+"&redirect_uri=http%3A%2F%2F"+Constants.WXPUBLIC_REDIRECTURL+"%2Fzld%2Fcarowner.do%3Faction%3Dgetobonus%26id%3D"+bid+"%26operate%3Dcaibonus" +
								"%26authrange%3Dtingchebao&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
						//String authurl =   "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXPUBLIC_APPID+"&redirect_uri=http%3A%2F%2Fwww.tingchebao.com%2Fzld%2Fcarowner.do%3Faction%3Dgetobonus%26id%3D"+bid+"%26operate%3Dcaibonus&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
						response.sendRedirect(authurl);
						return null;
					}
				}
				if(user!=null&&(wximgurl==null || wximgurl.length()<1)){//更新微信头像
					setWxUser(wxkeys[0], wxkeys[1], mobile, (Long)user.get("id"),bid);
				}
				//查是否已领过
				if(user!=null&&user.get("id")!=null){
					Map utmMap = daService.getMap("select amount from order_ticket_detail_tb where uin =? and otid=? ", new Object[]{user.get("id"),bid});
					if(utmMap!=null&&utmMap.get("amount")!=null){//已领过
						request.setAttribute("money", utmMap.get("amount"));
						request.setAttribute("mobile", user.get("mobile"));
						String ret = caiBonusList(request, (Long)user.get("id"),bid);
						return mapping.findForward(ret);
					}
				}
				
				String target ="caibouns";
				if(user!=null){//已注册用户，直接领红包
					mobile = (String)user.get("mobile");
					target = caiBonusRet(request, bid, (Long)user.get("id"), mobile, wxkeys[0], wxkeys[1]);
				}else {//到页面输入手机号
					caiBonusList(request,null,bid);
					request.setAttribute("bid",bid);
					request.setAttribute("openid", wxkeys[0]);
					request.setAttribute("acctoken", wxkeys[1]);
				}
				logger.error(">>>>>>>>>>>>>>>>>>>>carhonbai target:"+target);
				return mapping.findForward(target);
			}else if(operate.equals("caibonusret")){//取红包结果，及全部拆红包结果
				String openid = RequestUtil.getString(request, "openid");
				String accToken = RequestUtil.getString(request, "acctoken");
				if(uin!=null&&uin!=-1){
					int isnewuser=setWxUser(openid, accToken, mobile, uin,bid);
					if(isnewuser==-1){
						request.setAttribute("message", "您输入的手机号已绑定，请直接进入微信号领取！");
						return mapping.findForward("error");
					}else if(isnewuser==-2){
						request.setAttribute("message", "当前微信账户已领取过停车券，新红包才能再次领取！");
						return mapping.findForward("error");
					}
				}
				if(uin!=null&&uin!=-1){
					Map utmMap = daService.getMap("select amount from order_ticket_detail_tb where uin =? and otid=? ", new Object[]{uin,bid});
					if(utmMap!=null&&utmMap.get("amount")!=null){//已领过
						request.setAttribute("money", utmMap.get("amount"));
						request.setAttribute("mobile", mobile);
						String ret = caiBonusList(request, uin,bid);
						return mapping.findForward(ret);
					}
				}
				String target = caiBonusRet(request,bid,uin,mobile,openid,accToken);
				return mapping.findForward(target);
			}
		}
		String reslut = StringUtils.createJson(infoMap);
		//logger.info(reslut);
		AjaxUtil.ajaxOutput(response,reslut);
		//AjaxUtil.ajaxOutput(response, StringUtils.createXML(infoMap));
		return null;
	}
	
	private String getMesg(HttpServletRequest request, Long uin) {
		Long maxid = RequestUtil.getLong(request, "maxid", -1L);
		Integer page = RequestUtil.getInteger(request, "page", 1);
		if(maxid>-1){
			Long count = daService.getLong("select count(ID) from user_message_tb where uin=? and id>?", new Object[]{uin,maxid});
			return count+"";
		}else{
			List<Object> params = new ArrayList<Object>();
			params.add(uin);
			List<Map<String, Object>> list = daService.getAll("select id,type,ctime,content,title from user_message_tb where uin=? order by id desc",
					params,page,10);
			return StringUtils.createJson(list);
		}
	}

	private String sweepTicket(HttpServletRequest request, Long uin,String mobile) {
		Long ntime = System.currentTimeMillis()/1000;
		Map<String,Object> infoMap = new HashMap<String, Object>();
		Long codeid = RequestUtil.getLong(request, "codeid", -1L);
		infoMap.put("type", 3);
		String info = null;
		if(codeid != -1){
			Map<String, Object> codeMap = daService
					.getMap("select * from qr_code_tb where id=? and type=? and state=? and ticketid is not null and comid is not null and uid is not null ",
							new Object[] { codeid, 6, 0 });
			String carnumber = publicMethods.getCarNumber(uin);
			if(codeMap != null){
				Long ticketid = (Long)codeMap.get("ticketid");
				Map<String, Object> ticketMap = daService.getMap("select * from ticket_tb where id=? and limit_day>? and type=? and uin is null ",
								new Object[] { ticketid, System.currentTimeMillis() / 1000, 1 });
				logger.error("车主端扫码领专用券>>>mobile:"+mobile+",codeid:"+codeid+",uin:"+uin+",ticketid:"+ticketid);
				if(ticketMap != null){
					Double score = Double.valueOf(codeMap.get("score") + "");
					Long uid = (Long)codeMap.get("uid");
					Long comid = (Long)codeMap.get("comid");
					Map<String, Object> uidMap = daService.getMap("select nickname,reward_score from user_info_tb where id=? ", new Object[]{uid});
					Map<String, Object> comMap = daService.getMap("select company_name from com_info_tb where id=? ", new Object[]{comid});
					
					Double reward_score = Double.valueOf(uidMap.get("reward_score") +"");
					logger.error("车主端扫码领专用券>>>mobile:"+mobile+",codeid:"+codeid+",uin:"+uin+",ticketid:"+ticketid+",score:"+reward_score);
					if(reward_score >= score){//type=0必须积分满足
						List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
						//二维码
						Map<String, Object> codeSqlMap = new HashMap<String, Object>();
						
						Map<String, Object> ticketSqlMap = new HashMap<String, Object>();
						
						Map<String, Object> scoreSqlMap = new HashMap<String, Object>();
						//积分明细
						Map<String, Object> scoreAccountSqlMap = new HashMap<String, Object>();
						
						codeSqlMap.put("sql", "update qr_code_tb set state=? where id=? ");
						codeSqlMap.put("values", new Object[] { 1, codeid });
						bathSql.add(codeSqlMap);
						
						ticketSqlMap.put("sql", "update ticket_tb set uin=? where id=? ");
						ticketSqlMap.put("values", new Object[] { uin, ticketid});
						bathSql.add(ticketSqlMap);
						
						scoreAccountSqlMap.put("sql", "insert into reward_account_tb (uin,score,type,create_time,remark,target,ticket_id) values(?,?,?,?,?,?,?)");
						scoreAccountSqlMap.put("values", new Object[]{uid,score,1,ntime,"停车券 扫码",2,ticketid});
						bathSql.add(scoreAccountSqlMap);
						
						scoreSqlMap.put("sql", "update user_info_tb set reward_score=reward_score-? where id=? ");
						scoreSqlMap.put("values", new Object[]{score, uid});
						bathSql.add(scoreSqlMap);
						boolean b = daService.bathUpdate(bathSql);
						logger.error("车主端扫码领专用券>>>mobile:"+mobile+",codeid:"+codeid+",uin:"+uin+",uid:"+uid+",comid:"+comid+",b:"+b);
						if(b){
							info="{\"id\":\""+ticketid+"\",\"money\":\""+ticketMap.get("money")+"\",\"cname\":\""+comMap.get("company_name")+"\",\"type\":\""+ticketMap.get("type")+"\",\"fee\":{\"id\":\""+uid+"\",\"name\":\""+uidMap.get("nickname")+"\"}}";
							Map<String, Object> rMap = new HashMap<String, Object>();
							rMap.put("uin", uid);
							rMap.put("score", score);
							rMap.put("tmoney", ticketMap.get("money"));
							rMap.put("carnumber", carnumber);
							logService.insertParkUserMesg(7, rMap);
						}else{
							logger.error("车主端扫码领专用券>>>mobile:"+mobile+",codeid:"+codeid+",uin:"+uin+",ticketid:"+ticketid+"b:"+b);
							info="{\"id\":\"-1\"}";
						}
					}else{
						logger.error("车主端扫码领专用券>>>收费员积分不足mobile:"+mobile+",codeid:"+codeid+",uin:"+uin+",ticketid:"+ticketid+",收费员剩余score:"+reward_score+",此次消耗积分：score:"+score);
						info="{\"id\":\"-1\"}";
					}
				}else{
					logger.error("车主端扫码领专用券>>>mobile:"+mobile+",codeid:"+codeid+",uin:"+uin+",二维码过期");
					info="{\"id\":\"-1\"}";//该二维码已失效
				}
			}else{
				logger.error("车主端扫码领专用券>>>mobile:"+mobile+",codeid:"+codeid+",uin:"+uin+",二维码已被领取");
				info="{\"id\":\"-2\"}";//该二维码已失效
			}
		}else{
			info="{\"id\":\"-1\"}";
		}
		String result="{\"type\":\"3\",\"info\":"+info+"}";
		return  result;
	}

	private String oBparms(HttpServletRequest request) {
		Long bid = RequestUtil.getLong(request, "bid", -1L);//抢的红包编号
		Map<String, Object> map = daService.getMap("select * from order_ticket_tb where id = ?", new Object[]{bid});
		if(map!=null){
			Integer type = (Integer)map.get("type");
			String imgurl = "images/bonus/order_bonu.png";
			String title = CustomDefind.getValue("TITLE");
			if(type==3){
				title="通过认证1246红包";
				imgurl = "images/bonus/auth_ticket.png";
			}else if(type==4){
				title="停车宝充值停车券大礼包";
				imgurl = "images/bonus/recharge.png";
			}else if(type==5){
				title="打飞机停车券大礼包";
				imgurl = "images/flygame/share_b.png";
			}
			return "{\"imgurl\":\""+imgurl+"\",\"title\":\""+title+"\"," +
					"\"description\":\""+map.get("bwords")+"\",\"url\":\"carowner.do?action=getobonus\",\"total\":\""+map.get("money")+"\",\"bnum\":\""+map.get("bnum")+"\"}";
		}
		return null;
	}

	private String hBonus(HttpServletRequest request, Long uin) {
		String key = memcacheUtils.readHBonusCache();
		String version = RequestUtil.getString(request, "version");
		if(!version.equals(""))
			logger.error(">>>>写入版本号:"+version+","+daService.update("update user_info_tb set version=? where id =? ", new Object[]{version,uin}));
		if(key!=null&&key.equals("1")){//节日开关已开
			return "{\"imgurl\":\"hbonou_ltip.jpg\",\"sharable\":\"0\"}";
		}
		return "{}";
	}

	private String regCarMsg(HttpServletRequest request, Long uin) {
		Long ntime = System.currentTimeMillis()/1000;
		Long tid = daService.getkey("seq_transfer_url_tb");
		String url = "http://www.tingchebao.com/zld/turl?p="+tid;
		//String url = "http://192.168.10.240/zld/turl?p="+tid;
		int result = daService.update("insert into transfer_url_tb(id,url,ctime,state) values (?,?,?,?)",
				new Object[]{tid,"regparker.do?action=toregpage&recomcode="+uin,
						ntime,0});
		if(result==1)
			return url;
		else {
			return "推荐失败!";
		}
	}

	private String getPKUser(HttpServletRequest request,Long comId,Long uin) {
		Long ntime = System.currentTimeMillis()/1000;
		Map<String,Object> infoMap = new HashMap<String, Object>();
		Long uid = RequestUtil.getLong(request, "uid", -1L);
		Map orderMap = daService.getMap("select * from order_tb o where o.uin=? and o.state=? and o.comid = " +
				"(select u.comid from user_info_Tb u where u.id =? ) ", new Object[]{uin,0,uid});
		String result ="{}";
		if(orderMap==null||orderMap.isEmpty()){
			 orderMap = daService.getMap("select * from order_tb o where o.uin=? and o.state=? and pay_type=?" +
			 		" and o.end_time >? and o.comid = " +
					"(select u.comid from user_info_Tb u where u.id =? ) order by end_time desc ", 
					new Object[]{uin,1,1,ntime-15*60,uid});
		}
		if(orderMap!=null&&!orderMap.isEmpty()){//处理已存在的订单，如果有订单且是未结算或已现金结算时，不返回收费信息
			Integer state = (Integer)orderMap.get("state");
			String cname =""; 
			String address = "";
			Map comMap = daService.getMap("select company_name,address from com_info_tb where id=?",new Object[]{orderMap.get("comid")});
			if(comMap!=null){
				cname = (String)comMap.get("company_name");
				address = (String)comMap.get("address");
			}
			if(state==0){//未结算，绑定订单
				Long btime = (Long)orderMap.get("create_time");
				Long end = ntime;
				Integer pid = (Integer)orderMap.get("pid");
				Integer car_type = (Integer)orderMap.get("car_type");//0：通用，1：小车，2：大车
				if(pid>-1){
					infoMap.put("total",publicMethods.getCustomPrice(btime, end, pid));
				}else {
					infoMap.put("total",publicMethods.getPrice(btime, end, comId, car_type));	
				}
				infoMap.put("btime", btime);
				infoMap.put("etime",end);
				infoMap.put("parkname", cname);
				infoMap.put("address",address);
				infoMap.put("orderid", orderMap.get("id"));
				infoMap.put("state",orderMap.get("state"));
				infoMap.put("parkid", orderMap.get("comid"));
				result= StringUtils.createJson(infoMap);
			}else if(state==1){//已结算
				Map<String, Object> infomMap = new HashMap<String, Object>();
				comId = (Long)orderMap.get("comid");
				Long btime = (Long)orderMap.get("create_time");
				Long etime = (Long)orderMap.get("end_time");
				infomMap.put("parkname", cname);
				infomMap.put("address",address);
				infomMap.put("btime", btime);
				infomMap.put("etime", etime);
				infomMap.put("total", StringUtils.formatDouble(orderMap.get("total")));
				infomMap.put("state",orderMap.get("pay_type"));// -- 0:未结算，1：待支付，2：支付完成
				infomMap.put("orderid",orderMap.get("id"));
				result = StringUtils.createJson(infomMap);
			}
		}
		if(result.equals("{}")){//没有订单，返回收费信息
			Map uMap = daService.getMap("select u.id,u.nickname as name,c.company_name as parkname from " +
					"user_info_Tb u, com_info_tb  c where u.comid=c.id and u.id=?", new Object[]{uid});
			result = StringUtils.createJson(uMap);
		}
		logger.info(">>>>"+result);
		return null;
	}

	private String epay(HttpServletRequest request, Long uin) {
		Long ntime = System.currentTimeMillis()/1000;
		String version = RequestUtil.processParams(request, "version");
		Long uid = RequestUtil.getLong(request, "uid", -1L);
		Double money  = RequestUtil.getDouble(request, "total", 0d);
		Long ticketId = RequestUtil.getLong(request, "ticketid", -1L);
		Integer bind_flag = RequestUtil.getInteger(request, "bind_flag", 1);//0:未绑定账户，1：已绑定账户
		//String version = RequestUtil.processParams(request, "version");
		// comId, total, uin, uid, ticketId, comName, ptype
		Long _comId = daService.getLong("select comid from user_info_tb where id=? ", new Object[]{uid});
		String carNumber = publicMethods.getCarNumber(uin);
		Long orderId = daService.getkey("seq_order_tb");
		int	result = publicMethods.epay(_comId, money, uin, uid, ticketId,carNumber, 0, bind_flag,orderId,null);
		if(result==5){
			result = 1;
			//发支付成功消息给收费员
			logService.insertParkUserMessage(_comId, 2,uid,carNumber, -1L,money,"", 0, ntime, ntime+10,0, null);
		}
		logger.error(">>>>>>>>>>>车主直接支付订单返回："+result);
		if(version.equals("2")){
			if(result==1){//支付成功，查询有没有直付红包
				Long count = null;
				Map bMap  =daService.getMap("select id from order_ticket_tb where uin=? and  order_id=? and ctime>? ",
						new Object[]{uin,orderId,ntime-5*60});//五分钟前的红包
				if(bMap!=null){
					Long btime = (Long)bMap.get("btime");
					if(btime!=null&&btime>10000){//已经分享过，不再分享
						bMap=null;
					}
				}
				if(bMap!=null&&bMap.get("id")!=null)
					count = (Long)bMap.get("id");
				logger.error(">>>>>>>>>>json>直付订单，支付订单返回："+"{\"result\":\"1\",\"tips\":\""+count+"\",\"errmsg\":\""+orderId+"\"}");
				if(count!=null&&count>0){
					return "{\"result\":\"1\",\"tips\":\""+count+"\",\"errmsg\":\""+orderId+"\"}";
				}else {
//					if(client_type==0)//android
//						AjaxUtil.ajaxOutput(response, "{\"result\":\"1\",\"tips\":\"on\"}");
//					else {//ios
					return "{\"result\":\"1\",\"tips\":\"\",\"errmsg\":\""+orderId+"\"}";
//					}
				}
				
//				Long count = null;
//				Map bMap  =daService.getMap("select id from bouns_tb where uin=? and  order_id=?",new Object[]{uin,998});
//				if(bMap!=null&&bMap.get("id")!=null)
//					count = (Long)bMap.get("id");
//				if(count!=null&&count>0){
//					AjaxUtil.ajaxOutput(response, "{\"result\":\"1\",\"tips\":\""+count+"\"}");
//				}else {
//					AjaxUtil.ajaxOutput(response, "{\"result\":\"1\",\"tips\":\"\"}");
//				}
//				logger.error(">>>>>>>>>>json>直付订单，支付订单返回："+"{\"result\":\"1\",\"tips\":\""+count+"\"}");
			}else {
				logger.error(">>>>>>>>>json>>直付订单，支付订单返回："+"{\"result\":\""+result+"\",\"tips\":\"\"}");
				return  "{\"result\":\""+result+"\",\"tips\":\"\",\"errmsg\":\"\"}";
			}
		}else {
			return result+"";
		}
	}

	private String getAccount(HttpServletRequest request, Object balance,Long uin,String mobile) {
		Double total = RequestUtil.getDouble(request, "total", 0d);
		Long orderId = RequestUtil.getLong(request, "orderid", -1L);//普通订单，传订单编号
		Long uid = RequestUtil.getLong(request, "uid", -1L);//直付时，传收费员编号 
		Integer ptype = RequestUtil.getInteger(request, "ptype", -1);//0账户充值；1包月产品；2停车费结算；3直付;4打赏 5购买停车券
		Integer utype = RequestUtil.getInteger(request, "utype", 0);//0普通选券（默认）1可用大于支付金额的停车券
		Integer source = RequestUtil.getInteger(request, "source", 0);//0客户端 1：微信公众号
		
		Long parkId = null;
		Map<String, Object> ticketMap = null;
		if(orderId != -1){
			Map<String, Object> orderMap = daService.getMap("select comid,uid from order_tb where id=?", new Object[]{orderId});
			if(orderMap!=null){
				parkId = (Long)orderMap.get("comid");
				uid = (Long)orderMap.get("uid");
			}
		}else if(uid != -1){
			Map<String, Object> uidMap = daService.getMap("select comid from user_info_tb where id = ? ", new Object[]{uid});
			if(uidMap!=null){
				parkId = (Long)uidMap.get("comid");
			}
		}
		logger.error("chooseTicket>>>uin"+uin+",total:"+total+",orderId:"+orderId+",uid:"+uid+",parkId:"+parkId+",ptype:"+ptype);
		if(uid != -1 && parkId != null && total > 0){
			boolean parkuserblack = publicMethods.isBlackParkUser(uid, true);
			boolean parkblack = publicMethods.isBlackParkUser(parkId, false);
			boolean userblack = publicMethods.isBlackUser(uin);
			boolean isAuth = publicMethods.isAuthUser(uin);
			
			logger.error("chooseTicket:uin"+uin+",orderId:"+orderId+",parkuserblack:"+parkuserblack+",parkblack:"+parkblack+",userblack:"+userblack+",ptype:"+ptype+",isAuth:"+isAuth);
			if(parkId != null && !parkuserblack && !parkblack && !userblack){
				List<Map<String, Object>> list = commonMethods.chooseTicket(uin, total, utype, uid, isAuth, ptype, parkId, orderId, source);
				if(list != null && !list.isEmpty()){
					Map<String, Object> map = list.get(0);
					if(map.get("iscanuse") != null && (Integer)map.get("iscanuse") == 1){
						ticketMap = map;
					}
				}
			}
		}
		String ret = "{\"balance\":\""+balance+"\",\"tickets\":[]}";
		String tickets = "[";
		if(ticketMap!=null)
			tickets +=StringUtils.createJson(ticketMap);//"{\"id\":\""+ticketMap.get("id")+"\",\"money\":\""+ticketMap.get("money")+"\"}";
		tickets +="]";
		ret = ret.replace("[]", tickets);
		return ret;
	}

	private String getTickets(HttpServletRequest request, Long uin,String mobile) {
		Long ntime = System.currentTimeMillis()/1000;
		List<Map<String, Object>> ticketMap = daService.getAll("select t.create_time beginday ,limit_day limitday,resources," +
				"t.state,t.money,company_name cname,utime ,pmoney,t.type " +
				"from ticket_tb t left join com_info_tb c on t.comid=c.id where uin = ? and t.type<? order by limit_day", new Object[]{uin,2});
		
		//Integer ptype  = RequestUtil.getInteger(request, "ptype", -1);
		//处理没有返回到账户中的停车券
		boolean isback  = commonMethods.checkBonus(mobile, uin);
		//logger.error(">>>>");
		if(ticketMap!=null&&!ticketMap.isEmpty()){
			for(Map<String, Object> tMap : ticketMap){
				Long limitDay = (Long)tMap.get("limitday");
				Integer money = (Integer)tMap.get("money");
				Integer res = (Integer)tMap.get("resources");
				tMap.put("isbuy", res);
				tMap.remove("resources");
				if(ntime >limitDay)
					tMap.put("exp", 0);
				else {
					tMap.put("exp", 1);
				}
				Integer state = (Integer)tMap.get("state");
				Integer limit =Integer.valueOf(CustomDefind.getValue("TICKET_LIMIT"));
				Integer ttype = (Integer)tMap.get("type");
				Integer topMoney = CustomDefind.getUseMoney(money.doubleValue(), 1);
				tMap.put("desc", "满"+topMoney+"元可以抵扣全额");
				if(ttype==1||res==1){
					tMap.put("desc", "满"+(money+limit)+"元可以抵扣全额");
				}
				if(res==1&&state==0){
					tMap.put("desc", "满"+(money+1)+"元可以抵扣全额,过期后退还"+StringUtils.formatDouble(tMap.get("pmoney"))+"元至您的账户");
				}
			}
//			System.err.println(StringUtils.createJson(ticketMap).replace("null", ""));
			return StringUtils.createJson(ticketMap).replace("null", "");
		}
		return "[]";
	}

	private List<Map<String, Object>> getComment(HttpServletRequest request,
			Long comId) {
		List<Map<String, Object>> comList =daService.getAll("select * from com_comment_tb where comid=? order by id desc",
				new Object[]{comId});
		List<Map<String, Object>> resultMap = new ArrayList<Map<String,Object>>();
		if(comList!=null&&comList.size()>0){
			for(Map<String, Object> map : comList){
				Map<String, Object> iMap = new HashMap<String, Object>();
				Long createTime = (Long)map.get("create_time");
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(createTime*1000);
				String times = TimeTools.getTime_MMdd_HHmm(createTime*1000);
				Long uid = (Long)map.get("uin");
				iMap.put("parkId",comId);// 评价的车场ID
				iMap.put("date", times.substring(0,5));// 评价日期：7-24
				iMap.put("week", "星期"+getWeek(calendar.get(Calendar.DAY_OF_WEEK)));//评价日期是星期几：星期四
				iMap.put("time", times.substring(6));// 评价的车场ID
				iMap.put("info",  map.get("comment"));//评价内容：巴拉巴拉一大串废话。。。
				iMap.put("user", getCarNumber(uid));// 评价者：车主（车牌号：京A***A111）
				resultMap.add(iMap);
			}
		}
		return resultMap;
	}

	private int comment(HttpServletRequest request, Long comId, Long uin) {
		Long ntime = System.currentTimeMillis()/1000;
		String comment = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "comment"));
		if(comId==null||comId==-1){
			Long orderid =RequestUtil.getLong(request, "orderid", -1L);
			if(orderid>0){
				Map useMap = daService.getMap("select comid from order_tb where id =? ", new Object[]{orderid});
				if(useMap!=null&&useMap.get("comid")!=null)
					comId = (Long)useMap.get("comid");
			}
		}
		int result = 0;
		if(comId!=null&&comId!=-1&&uin!=-1&&!comment.equals("")){
			//20150612添加---车主只能对车场评论一次
			Long count = daService.getLong("select count(ID) from com_comment_tb where uin=? and comid=?", new Object[]{uin,comId});
			if(count<1)
				result = daService.update("insert into com_comment_tb (comid,uin,comment,create_time)" +
					" values(?,?,?,?)", new Object[]{comId,uin,comment,ntime});
			else {
				result = daService.update("update com_comment_tb set comment=? where uin=? and comid=? ", new Object[]{comment,uin,comId});
			}
		}
		if(result>1)
			result=1;
		return result;
	}

	private int praise(HttpServletRequest request, Long comId, Long uin) {
		Long ntime = System.currentTimeMillis()/1000;
		Integer praise = RequestUtil.getInteger(request, "praise", -1);
		int result = 0;
		if(comId!=-1&&uin!=null&&uin!=-1){
			try {
				//在此车场消费一笔以上才可以评价 20150205
				Long ocount = daService.getLong("select count(id) from order_tb where comid=? and uin=? and state=?  ",
						new Object[]{comId,uin,1});
				if(ocount>0)
					result = daService.update("insert into com_praise_tb (comid,uin,praise,create_time)"
								+ "values (?,?,?,?)", new Object[] { comId,uin, praise ,ntime});
			} catch (Exception e) {
				e.printStackTrace();
			}
			/*if(result==1&&praise==0){//差评扣分
				List<Map> list = daService.getAll("select id from user_info_tb where comid=? and state=? ",
						new Object[]{comId,0});
				if(list!=null&&!list.isEmpty()){
					for(Map map : list){
						logService.updateScroe(3,(Long) map.get("id"),comId);
					}
				}
			}*/
		}
		return result;
	}

	private int editPhone(HttpServletRequest request, Long uin,String mobile) {
		String _mobile = RequestUtil.processParams(request, "newmobile");
		int result = 0;
		if(uin!=null&&!_mobile.equals(""))
			try {
				result = daService.update("update user_info_Tb set mobile = ? where mobile =?",
						new Object[]{_mobile,mobile});
			} catch (Exception e) {
				if(e.getMessage().indexOf("user_info_tb_mobile_key")!=-1){
					result= 2;
				}else {
					result =3;
				}
				logger.error("=eidtphone="+e.getMessage());
			}
		return result;
	}
	
	private int editCarNumber(HttpServletRequest request, Long uin) {
		String carNumber=AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "carnumber"));
		carNumber = carNumber.toUpperCase().trim();
		carNumber = carNumber.replace("I", "1").replace("O", "0");
		logger.error(">>>>>editcarnumber:"+carNumber+",uin:"+uin);
		int result = 0;
		if(uin!=null&&!carNumber.equals("")) {
			try {
				Map oldCarMap = daService.getMap("select car_number from car_info_tb where uin = ? ", new Object[]{uin});
				result = daService.update(
						"update car_info_tb set car_number=? where uin=?",
						new Object[] { carNumber, uin });
				if(result==1&&oldCarMap!=null){
					publicMethods.syncUserCarNumber(uin, carNumber, (String)oldCarMap.get("car_number"));
				}
			} catch (Exception e) {
				if(e.getMessage().indexOf("car_info_tb_car_number_key")!=-1){
					result= 2;
				}else {
					result =3;
				}
				logger.error("=editcarnumber="+e.getMessage());
			}
		}
		return result;
	}

	private Map<String, Object> getProf(HttpServletRequest request, Long uin) {
		Map<String,Object> infoMap = new HashMap<String, Object>();
		Map profileMap = daService.getPojo("select low_recharge,limit_money,auto_cash from user_profile_tb where uin=?",new Object[]{uin});
		if(profileMap!=null){
			Integer autoCash = (Integer)profileMap.get("auto_cash");
			//@"10", @"25", @"50", @"100", @"0"
			Integer lre = (Integer)profileMap.get("low_recharge");
			if(lre!=10&&lre!=25&&lre!=50&&lre!=100)
				lre = 0;
			infoMap.put("low_recharge", lre);
			if(autoCash!=null&&autoCash==1){
				infoMap.put("limit_money", profileMap.get("limit_money"));
			}else {
				infoMap.put("limit_money", 0);
			}
			//infoMap.put("auto_cash", profileMap.get("auto_cash"));
		}else {
			infoMap.put("low_recharge", 0);
			infoMap.put("limit_money", 25);
		}
		logger.error(">>>>>>>>>>>>>>>getprofile:"+infoMap);
		return infoMap;
	}

	private int setPro(HttpServletRequest request, Long uin) {
		Long ntime = System.currentTimeMillis()/1000;
		Integer low_recharge=RequestUtil.getInteger(request, "low_recharge", 0);
		Integer limit_money=RequestUtil.getInteger(request, "limit_money", 0);
		Long count = daService.getLong("select count(*) from user_profile_tb where uin=?", new Object[]{uin});
		logger.error(">>>>>>>>>>>>>>>lowre:"+low_recharge+",limitmoney:"+limit_money);
		Long time = ntime;
		Integer auto_cash = 0;
		if(limit_money==-1||limit_money>0){
			auto_cash=1;
		}
		int result = 0;
		if(count>0){//update
			result = daService.update("update user_profile_tb set low_recharge=?," +
					" auto_cash=?,limit_money=?,update_time=? where uin=?",
					new Object[]{low_recharge,auto_cash,limit_money,time,uin});
			logger.error(">>>>>>>>>>>>>>>update profile uin:"+uin+",ret:"+result);
			/*if(result==1){//通知泊链平台，修改用户限额
				//查询车场是否同步过到泊链平台，查出上次同步的限额
				Map userMap = daService.getMap("select balance ,bolink_limit from user_info_tb u " +
						"left join user_profile_tb p on p.uin=u.id where  u.id =?   and u.union_state>? ", new Object[]{uin,0});
				logger.error("update profile,user set :"+userMap+",当前修改：limitmoney :"+limit_money);
				if(userMap!=null){
					Double bolinkLimit = StringUtils.formatDouble(userMap.get("bolink_limit"));
					Double balance =StringUtils.formatDouble(userMap.get("balance"));
					boolean isSend = false;
					Double money = Double.valueOf(limit_money);
					
					if(auto_cash==0){
						if(bolinkLimit>0){//泊链的限额大于0，但用户现在设置了不自动支付，需要通知泊链修改限额
							isSend=true;
							money=0.0;
						}
					}else {
						if(limit_money>0){
							if(limit_money<bolinkLimit){//用户修改限额小于了泊链的限额，需要通知泊链修改限额,但不能高于余额
								isSend=true;
								if(limit_money>balance){
									money = balance;
								}
							}else if(bolinkLimit!=limit_money.doubleValue()){//用户修改限额大于了泊链的限额，需要通知泊链修改限额,但不能高于余额
								if(limit_money>balance){
									if(balance!=bolinkLimit){
										isSend=true;
										money = balance;
									}
								}else {
									isSend=true;
								}
							}
						}else {//用户不限额了
							if(bolinkLimit!=balance){
								isSend=true;
								money = balance;
							}
						}
					}
					if(isSend){
						publicMethods.syncUserLimit(uin, money);
					}
				}
			}*/
		}else {
			result = daService.update("insert into user_profile_tb (low_recharge,auto_cash," +
					"create_time,limit_money,update_time,uin) values (?,?,?,?,?,?)", 
					new Object[]{low_recharge,auto_cash,time,limit_money,time,uin});
		}
		logger.error(">>>>>>>>>>>>>>>sertprofile:limit_money:"+limit_money+",auto_cash:"+auto_cash+",result:"+result);
		return result;
	}

	private int setProfile(HttpServletRequest request, Long uin) {
		Long ntime = System.currentTimeMillis()/1000;
		Integer low_recharge=RequestUtil.getInteger(request, "low_recharge", 0);
		Integer voice_warn=RequestUtil.getInteger(request, "voice_warn", 0);
		Integer auto_cash=RequestUtil.getInteger(request, "auto_cash", 0);
		Integer enter_warn=RequestUtil.getInteger(request, "enter_warn", 0);
		Long count = daService.getLong("select count(*) from user_profile_tb where uin=?", new Object[]{uin});
		Long time = ntime;
		int result = 0;
		if(count>0){//update
			result = daService.update("update user_profile_tb set low_recharge=?," +
					" voice_warn=?,auto_cash=?,enter_warn=?,update_time=? where uin=?",
					new Object[]{low_recharge,voice_warn,auto_cash,enter_warn,time,uin});
		}else {
			result = daService.update("insert into user_profile_tb (low_recharge,voice_warn,auto_cash,enter_warn," +
					"create_time,update_time,uin) values (?,?,?,?,?,?,?)", 
					new Object[]{low_recharge,voice_warn,auto_cash,enter_warn,time,time,uin});
		}
		return result;
	}

	private List<Map<String, Object>> accountDetail(HttpServletRequest request,Long uin) {
		Integer type = RequestUtil.getInteger(request, "type", 2);//2全部，0：充值，1：消费
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);//取第几页
		Long orderid = RequestUtil.getLong(request, "orderid", -1L);//属于哪个订单的账户明细，默认和订单无关
		String countSql = "select count(*) from user_account_tb where uin=?";
		String sql ="select create_time,amount,type,remark,pay_type from user_account_tb where uin=?"; 
		List<Object> params = new ArrayList<Object>();
		params.add(uin);
		if(type!=2){
			countSql +=" and type= ?";
			sql +=" and type=?";
			params.add(type);
		}
		if(orderid > 0){
			countSql +=" and orderid= ?";
			sql +=" and orderid=?";
			params.add(orderid);
		}
		Long count = daService.getCount(countSql, params);
		List<Map<String,Object>> allList =null;
		if(count>0){
			allList = daService.getAll(sql + " order by create_time desc ", params, pageNum, 15);
		}
		if(allList!=null){
			for(Map<String,Object> map : allList){
				Integer payType =(Integer)map.get("pay_type");
				// -- 0余额，1支付宝，2微信，3网银，4余额+支付宝,5余额+微信,6余额+网银 ,7停车宝充值 
				// 8活动奖励,9微信公众号，10微信公众号+余额，11微信打折券,12预支付返款 13停车券退款
				String payName = "余额";
				if(payType!=null){
					switch (payType) {
						case 1:
							payName = "支付宝";				
							break;
						case 2:
							payName = "微信";
							break;
						case 3:
							payName = "网银";
							break;
						case 4:
							payName = "余额+支付宝";
							break;
						case 5:
							payName = "余额+微信";
							break;
						case 6:
							payName = "余额+网银";
							break;
						case 7:
							payName = "停车券";
							break;
						case 8:
							payName = "活动奖励";
							break;
						case 9:
							payName = "微信公众号";
							break;
						case 10:
							payName = "微信公众号+余额";
							break;
						case 11:
							payName = "微信打折券";
							break;
						case 12:
							payName = "预支付返款";
							break;
						case 13:
							payName = "停车券退款";
							break;
						default:
							payName = "余额";
					}
				}
				map.put("pay_name", payName);
			}
		}
		return allList;
	}
	
	private String prepay(HttpServletRequest request, Long comid, Long uin){
		Map<String, Object> infoMap = new HashMap<String, Object>();
		Long orderId = RequestUtil.getLong(request, "orderid", -1L);
		Long ticketId  =RequestUtil.getLong(request, "ticketid", -1L);
		Double money = RequestUtil.getDouble(request, "money", 0d);
		String wxp_orderid = RequestUtil.processParams(request, "wxp_orderid");
		logger.error("prepay>>>orderid:"+orderId+",uin:"+uin+",comid:"+comid+",money:"+money+",ticketid:"+ticketId);
		int result = 0;
		if(orderId > 0){
			Map<String, Object> orderMap = daService.getMap("select * from order_tb where id=? and state=? ", 
					new Object[]{orderId, 0});
			if(orderMap != null){
				Double prefee = 0d;
				if(orderMap.get("total") != null){
					prefee = Double.valueOf(orderMap.get("total") + "");
				}
				logger.error("prepay>>>orderid:"+orderId+",uin:"+uin+",prefee:"+prefee+",ticketid:"+ticketId);
				if(prefee == 0){
					int r = publicMethods.prepay(orderMap, money, uin, ticketId, 0, 1, wxp_orderid);
					result = r;
				}
			}
		}
		if(comid == 20130){
			String re = commonMethods.sendPrepay2Baohe(orderId, result, money, 0);
			logger.error("toprepay>>>orderid:"+orderId+",uin:"+uin+",re:"+re);
		}
		infoMap.put("result", result);
		infoMap.put("money", money);
		infoMap.put("orderid", orderId);
		infoMap.put("paytype", 0);
		return StringUtils.createJson(infoMap);
	}

	private String payOrder(HttpServletRequest request,Long comId,Long uin) {
		Long ntime = System.currentTimeMillis()/1000;
		String type = RequestUtil.processParams(request, "ptype");
		//version=2，返回带红包的json数据 
		String version = RequestUtil.processParams(request, "version");
		if(type.equals("1")){//购买包月产品
			int result  = buyProduct(request,uin);
			return result+"";
		}else if(type.equals("2")){//支付订单
			Long orderId = RequestUtil.getLong(request, "orderid", -1L);
			Long ticketId  =RequestUtil.getLong(request, "ticketid", -1L);
			int result  =0;
			Double total = RequestUtil.getDouble(request, "total", 0d);
			logger.error("payorder>>>orderid:"+orderId+",type:"+type+",total:"+total+",version:"+version);
			Map orderMap =null;
			if(orderId!=-1){
			
				orderMap = daService.getMap("select * from order_tb where id=?", new Object[]{orderId});
				if(total==0)
					total = StringUtils.formatDouble(orderMap.get("total"));
				int re = publicMethods.payOrder(orderMap,total, uin, 2,0,ticketId,null,-1L, -1L);
				int state =1;//默认支付不成功
				logger.info(">>>>>>>>>>>>车主账户支付 ："+re+",orderid:"+orderId+",uin:"+uin);
				comId = (Long)orderMap.get("comid");
				Long btime = (Long)orderMap.get("create_time");
				Long etime  = (Long)orderMap.get("end_time");
				if(etime==null)
					etime=ntime;
				String duration = StringUtils.getTimeString(btime, etime);
				String carNumber = (String)orderMap.get("car_number");
				if(carNumber==null||"".equals(carNumber)||"车牌号未知".equals(carNumber))
						carNumber = publicMethods.getCarNumber(uin);
				if(re==5){
					state  = 2;
					result = 1;
					if(comId==20130){//宝和公司的测试订单，调用他们的接口发送订单支付状态
						String sr = commonMethods.sendOrderState2Baohe(orderId, result, total, 0);
						logger.error(">>>>>>>>>>>>>baohe sendresult:"+sr+",orderid:"+orderId+",state:"+result+",total:"+total);
					}
					//发支付成功消息给收费员
					logService.insertParkUserMessage(comId, state, (Long)orderMap.get("uid"),carNumber, orderId, total,duration, 0, btime, etime, 0, null);
					
				}else 
					result=re;
				//发消息给车主-----余额支付只返回结果，不再发消息===20140826
				//logService.doMessage(comId, state, uin,carNumber, orderId, Double.valueOf(orderMap.get("total")+""),duration, 0, btime, etime,0);
			}
			//{result:1,errmsg:"",tips:"bonusid"}
			if(version.equals("2")){
				if(result==1){//支付成功，查询有没有红包
					Long count = null;
					Map bMap  =daService.getMap("select id from order_ticket_tb where order_id=?",new Object[]{orderId});
					//Map bMap  =daService.getMap("select id from bouns_tb where order_id=?",new Object[]{orderId});
					if(bMap!=null&&bMap.get("id")!=null)
						count = (Long)bMap.get("id");
					logger.error(">>>>>>>>>>json>支付订单返回："+"{\"result\":\"1\",\"tips\":\""+count+"\",\"errmsg\":\""+orderId+"\"}");
					if(count!=null&&count>0){
						return  "{\"result\":\"1\",\"tips\":\""+count+"\",\"errmsg\":\""+orderId+"\"}";
					}else {
//						if(client_type==0)//android
//							AjaxUtil.ajaxOutput(response, "{\"result\":\"1\",\"tips\":\"on\"}");
//						else {//ios
						return "{\"result\":\"1\",\"tips\":\"\",\"errmsg\":\""+orderId+"\"}";
//						}
					}
				}else {
					logger.error(">>>>>>>>>json>>支付订单返回："+"{\"result\":\""+result+"\",\"tips\":\"\",\"errmsg\":\""+orderId+"\"}");
					return "{\"result\":\""+result+"\",\"tips\":\"\",\"errmsg\":\"\"}";
				}
			}else{
				logger.error(">>>>>>>>>>>支付订单返回："+result);
				return result+"";
			}
		}
		return "0";
	}

	private void setBonusType(List<Map<String, Object>> list) {
		Long ntime = System.currentTimeMillis()/1000;
		for(Map<String, Object> map : list){
			Long btime = (Long)map.get("btime");
			Long exptime = (Long)map.get("exptime");
			Integer type = (Integer)map.get("type");
			if(btime==null){//未领取
				if(ntime>exptime){
					map.put("state", 0);//已过期
				}else {
					map.put("state", 1);//未过期
				}
			}else {
				map.put("state", 2);//已领取
			}
			if(type==0){
				map.put("title", "订单停车券礼包");
			}else if(type==1){
				map.put("title", "微信打折券礼包");
			}else if(type==2){
				map.put("title", "车场专用券礼包");
			}else if(type==3){
				map.put("title", "认证通过大礼包");
			}else if(type==4){
				map.put("title", "充值停车券礼包");
			}else if(type==5){
				map.put("title", "游戏停车券礼包");
			}else {
				map.put("title", "订单停车券礼包");
			}
			map.remove("type");
			map.remove("btime");
		}
	}

	/**
	 * 购买停车券
	 * @param request
	 * @return
	 */
	private String buyTicket(HttpServletRequest request,Long uin) {
		Integer value = RequestUtil.getInteger(request, "value", 0);
		Integer number = RequestUtil.getInteger(request, "number", 0);
		int result = publicMethods.buyTickets(uin,value,number,0);
		return "{\"result\":\""+result+"\",\"errmesg\":\"\"}";
	}
	//车主对收费员的打赏
	private String reward(HttpServletRequest request) {
		Long ntime = System.currentTimeMillis()/1000;
		//http://192.168.199.240/zld/carowner.do?action=puserreward&mobile=18101333937&orderid=&ticketid=&uid=&money=
		String ret = "{\"result\":\"0\",\"errmsg\":\"打赏失败\"}";
		Long ticketId = RequestUtil.getLong(request, "ticketid", -1L);
		Long orderId = RequestUtil.getLong(request, "orderid", -1L);
		Double money = RequestUtil.getDouble(request, "total", 0.0);
		Long uid = RequestUtil.getLong(request, "uid", -1L);
		String mobile = RequestUtil.getString(request, "mobile");
		Long uin = getUinByMobile(mobile);
		int result = 0;
		Long count = daService.getLong("select count(id) from parkuser_reward_tb where uin=? and order_id=? ", new Object[]{uin,orderId});
		if(count>0){
			ret = "{\"result\":\"-1\",\"errmsg\":\"已打赏过\"}";
		}else {
			Long btime = TimeTools.getToDayBeginTime();
			if(uin!=-1&&uid!=-1&&orderId!=-1&&money>0){
				result = publicMethods.doparkUserReward(uin, uid, orderId, ticketId, money,0,1);
			}
			if(result==1){
				String carNumber = publicMethods.getCarNumber(uin);
				Long recount = daService.getLong("select count(id) from parkuser_reward_tb where uid =? and ctime >? ",
						new Object[]{uid,TimeTools.getToDayBeginTime()});
				Map<String, Object> tscoreMap = daService.getMap("select sum(score) tscore from reward_account_tb where type=? and create_time>? and uin=? ",
						new Object[] { 0, btime, uid });
				Long comid = -1L;
				if(tscoreMap != null && tscoreMap.get("tscore") != null){
					Double tscore = Double.valueOf(tscoreMap.get("tscore") + "");
					if(tscore >= 5000){
						comid = -2L;
						logger.error("今日打赏已达上限uid:"+uid+",tscore:"+tscore+",uin:"+uin);
					}
				}
				logger.error("rewardmessage>>uid:"+uid+",uin:"+uin+",recount:"+recount+",carnumber:"+carNumber);
				logService.insertParkUserMessage(comid,2,uid,carNumber,uin,money, ""+recount, 0,ntime,ntime+10,5, null);
				ret = "{\"result\":\"1\",\"errmsg\":\"打赏成功\"}";
			}else if(result==-1){
				ret = "{\"result\":\"-2\",\"errmsg\":\"余额不足\"}";
			}
		}
		return ret;
	}
	/*车主对收费员的评论**/
	private String commpuser(HttpServletRequest request) {
		Long ntime = System.currentTimeMillis()/1000;
		String comment = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "comment"));
		Long orderid =RequestUtil.getLong(request, "orderid", -1L);
		String mobile = RequestUtil.getString(request, "mobile");
		Long uin = getUinByMobile(mobile);
		String result = "{\"result\":\"0\",\"errmsg\":\"评论失败\"}";
		if(uin!=null&&uin!=-1){
			Long count = daService.getLong("select count(ID) from parkuser_comment_tb where uin=? and order_id=? ", new Object[]{uin,orderid});
			if(count<1){
				Long uid = -1L;//收费员编号
				if(orderid>0){
					Map useMap = daService.getMap("select uid from order_tb where id =? ", new Object[]{orderid});
					if(useMap!=null&&useMap.get("uid")!=null)
						uid = (Long)useMap.get("uid");
					if(uid!=null&&uid!=-1&&uin!=-1&&!comment.equals("")){
						int ret = daService.update("insert into parkuser_comment_tb (uid,uin,comments,ctime,order_id)" +
								" values(?,?,?,?,?)", new Object[]{uid,uin,comment,ntime,orderid});
						if(ret==1){
							result = "{\"result\":\"1\",\"errmsg\":\"评论完成\"}";
						}
					}
				}else {
					result = "{\"result\":\"-2\",\"errmsg\":\"收费员信息不存在\"}";
				}
			}else {
				result = "{\"result\":\"-1\",\"errmsg\":\"您已评论过\"}";
			}
		}
		return  result;
	}
	/**是否存在红包，是否已领完*/
	private int doPreGetBonus(HttpServletRequest request,Long id) {
		Long ntime = System.currentTimeMillis()/1000;
		//是否存在红包，是否已领完
		String words = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "words"));
		Map bMap = daService.getMap("select etime,bnum,exptime,bwords,money,type,uin from order_ticket_tb where id =? ", new Object[]{id});
		Long count= daService.getLong("select count(id) from order_ticket_detail_tb where otid =?", new Object[]{id});
		//Long havecount= daService.getLong("select count(id) from order_ticket_detail_tb where otid =? and uin is not null", new Object[]{id});
		int ret = 1;
		Integer bnum=0;
		Integer money = 0;
		Integer type = 0;//0普通停车券 1微信打券
		String tname = "停车券";
		String tpic = "ticket";
		String fontColor="e38165";
		String mname="元";
		if(bMap!=null){
			String bwords = (String)bMap.get("bwords");
			if(!words.equals("")){//更新祝福语
				daService.update("update order_ticket_tb set bwords=? where id=? ", new Object[]{words,id});
				bwords = words;
			}
			if(bwords!=null&&bwords.length()>14)
				bwords = bwords.substring(0,12)+"...";
			Long exptime = (Long)bMap.get("exptime");
			bnum = (Integer)bMap.get("bnum");
			money = (Integer)bMap.get("money");
			type = (Integer)bMap.get("type");
			if(type==1){
				tname="折扣券";
				tpic="ticket_wx";
				mname="折";
			}else if(type==5){
				tname="灰机券";
				fontColor="8bd3a3";
				request.setAttribute("fly", "_fly");
			}
			Map userMap = daService.getMap("select wx_imgurl from user_info_tb where id=? ", new Object[]{bMap.get("uin")});
			if(userMap!=null&&userMap.get("wx_imgurl")!=null)
				request.setAttribute("carowenurl",userMap.get("wx_imgurl"));
			request.setAttribute("bwords",bwords);
			request.setAttribute("bnum",bnum);
			if(type==0||type==5)
				request.setAttribute("btotal","(共"+ bMap.get("money")+"元)");
			if(ntime>exptime+15*24*60*60){//已过期
				request.setAttribute("tpic", tpic);
				request.setAttribute("mname", mname);
				request.setAttribute("tipwords", tname+"已过期");
				ret =-1;
			}else {
				if(count==0){//没有折过红包，开始折红包
					if(bnum>0&&money>0){
						if(type==3){//认证红包
							String insertSql = " insert into order_ticket_detail_tb (otid,amount) values(?,?)";
							List<Object[]> values = new ArrayList<Object[]>();
							Integer[] _bonus=new Integer[]{1,2,1,1,2,1,1,2,1,1,2,1,1,2,1,1,2,1,2,4,6};
							for(Integer d : _bonus){
								Object[] objects = new Object[]{id,d};
								values.add(objects);
							}
							int _ret = daService.bathInsert(insertSql, values, new int[]{4,4});
							logger.error("新抢认证红包，写入红包....."+_ret);
						}else {
							String insertSql = " insert into order_ticket_detail_tb (otid,amount) values(?,?)";
							List<Object[]> values = new ArrayList<Object[]>();
							List<Integer> _bonus=StringUtils.getBonusIngteger(money, bnum,3);
							if(type==4)
								_bonus=StringUtils.getBonusIngteger(money, bnum,12);
							else if(type==2&&money==18)
								_bonus=StringUtils.getBonusIngteger(money, bnum,4);
							for(Integer d : _bonus){
								Object[] objects = new Object[]{id,d};
								values.add(objects);
							}
							int _ret = daService.bathInsert(insertSql, values, new int[]{4,4});
							logger.error("新抢红包，写入红包....."+_ret);
						}
					}
				}
			}
		}else {//不存在
			request.setAttribute("tpic", tpic);
			request.setAttribute("mname", mname);
			request.setAttribute("tipwords", tname+"已过期");
			ret =-3;
		}
		
		/*if(havecount.intValue()==bnum){
			request.setAttribute("tipwords", tname+"抢完了");
			caiBonusList(request, null, id);
			ret =-4;
		}*/
		request.setAttribute("btype", type);
		request.setAttribute("tname", tname);
		request.setAttribute("fontColor", fontColor);
		return ret;
	}
	
	private String caiBonusRet(HttpServletRequest request,Long bid,Long uin,String mobile,String openid,String acc_token) {
		Long ntime = System.currentTimeMillis()/1000;
		Map bMap = daService.getMap("select * from order_ticket_tb where id = ? ", new Object[]{bid});
		int bums =0;
		Long havecount= daService.getLong("select count(id) from order_ticket_detail_tb where otid =? and uin is not null", new Object[]{bid});
		Integer type = 0;//0普通停车券 1微信打券
		Long comid = -1L;
		String tname = "停车券";
		String tpic = "ticket";
		String mname="元";
		Integer btype =0;
		String fontColor="e38165";
		Long otuin = null;//红包所属车主
		if(bMap!=null){
			Map userMap = daService.getMap("select wx_imgurl,comid from user_info_tb where id=? ", new Object[]{bMap.get("uin")});
			bums = (Integer)bMap.get("bnum");
			type = (Integer)bMap.get("type");
			otuin = (Long)bMap.get("uin");
			btype = type;
			if(type==1){
				type=2;
				tname="折扣券";
				tpic="ticket_wx";
				mname="折";
			}else if(type == 2){//专用券
				type = 1;
				tname = "专用券";
				tpic = "ticket_limit";
				if(userMap.get("comid") != null && ((Long)userMap.get("comid")) > 0){
					comid = (Long)userMap.get("comid");
					Map<String, Object> comMap = daService.getMap(
							"select company_name from com_info_tb where id=? ",
							new Object[] { comid });
					if(comMap == null){
						request.setAttribute("mname", mname);
						request.setAttribute("tipwords", tname+"已过期");
						request.setAttribute("isover", 0);
						return "caibouns";
					}else{
						request.setAttribute("cname", comMap.get("company_name"));
					}
				}else{
					request.setAttribute("mname", mname);
					request.setAttribute("tipwords", tname+"已过期");
					request.setAttribute("isover", 0);
					return "caibouns";
				}
				logger.error("抢停车场专用券,uin"+uin+",收费员编号："+bMap.get("uin"));
			}else if(type==3){//认证红包,写入普通红包类型
				type=0;
				tname="认证礼包";
			}else if(type==4){
				type=0;
				tname="充值礼包";
			}else if(type==5){
				type=0;
				tname="灰机券";
				fontColor="8bd3a3";
				request.setAttribute("fly", "_fly");
			}
			String bwords = (String)bMap.get("bwords");
			if(bwords!=null&&bwords.length()>14)
				bwords = bwords.substring(0,12)+"...";
			request.setAttribute("bwords",bwords);
			
			if(userMap!=null&&userMap.get("wx_imgurl")!=null)
				request.setAttribute("carowenurl",userMap.get("wx_imgurl"));
			request.setAttribute("bnum",bMap.get("bnum"));
			if(type==0||type==5)
				request.setAttribute("btotal","(共"+ bMap.get("money")+"元)");
		}else {
			//没有红包了
			request.setAttribute("mname", mname);
			request.setAttribute("tipwords", tname+"已过期");
			request.setAttribute("isover", 0);
			return "caibouns";
		}
		request.setAttribute("mname", mname);
		request.setAttribute("tpic", tpic);
		request.setAttribute("tname", tname);
		request.setAttribute("btype", btype);
		request.setAttribute("fontColor", fontColor);
		if(havecount==bums){
			request.setAttribute("tipwords", tname+"已抢完");
			request.setAttribute("isover", 0);
			request.setAttribute("tpic", tpic+"_l");
			getBonusList(request, bid, uin);
			return "caibouns";
		}
		
		Integer utype = 0;//老用户
		if(uin==null){//新用户先注册
			String bsql = "insert into bonus_record_tb (bid,ctime,mobile,state,amount) values(?,?,?,?,?) ";
			logger.error(">>>>>>>>抢停车券，新用户抢订单红包,先注册...");
			uin = publicMethods.regUser(mobile, 1000L,-1L,false);//注册并发元停车券
			Object [] values = new Object[]{bid,ntime,mobile,0,10};//登记为未领取红包，登录时写入停车券表（判断是否是黑名单后）
			logger.error(">>>>>>>>抢停车券，新用户领三张10元券："+daService.update(bsql,values));	
			utype=1;//新用户
			setWxUser(openid, acc_token, mobile, uin,bid);
		}
		
		if(uin==null||uin==-1){
			//没有红包了
			request.setAttribute("tipwords", tname+"已抢完");
			request.setAttribute("isover", 0);
			return "caibouns";
		}
		boolean isbig=false;//是否取大红包
		//取停车券红包，总红包个数小于10个的都是新用户，新用户从大领到小。老用户随机
		if(utype==0){//老用户
			Long counts = daService.getLong("select count(id) from order_ticket_detail_tb where uin=? ", new Object[]{uin});
			if(counts<10)
				isbig=true;
		}else {
			isbig=true;
		}
		Map otdtMap = null;
		List<Map> otdList =null;
		if(btype==3){//认证红包顺序领取
			otdList=daService.getAll("select id,amount from order_ticket_detail_tb where  uin is null and otid=? order by id", new Object[]{bid});
		}else if(btype==4){//充值礼包
			otdList=daService.getAll("select id,amount,uin from order_ticket_detail_tb where otid=? order by id", new Object[]{bid});
		}else {
			otdList=daService.getAll("select id,amount from order_ticket_detail_tb where  uin is null and otid=? order by amount desc", new Object[]{bid});
		}
		Long otdid = null;
		if(otdList!=null&&!otdList.isEmpty()){
			if(btype==4){//充值礼包，红包所属车主要领取第二大红包
				Integer notget=0;//未领取红包数
				List<Map> timeList = new ArrayList<Map>();
				for(Map oMap : otdList){
					if(oMap.get("uin")==null){
						timeList.add(oMap);
						notget ++;
					}
				}
				Map map = otdList.get(otdList.size()-2);//第二大红包
				Long ouin = (Long)map.get("uin");
				if(ouin==null){//红包所属车主还没有抢红包
					if(otuin.equals(uin)){//红包所属车主来抢红包
						otdtMap =map;
					}else {//去掉第二大的红包
						if(notget>1)//还有两个以上红包没有领取时
							otdList.remove(map);
					}
				}
				if(otdtMap==null&&timeList.size()>0){
					if(isbig){//新用户从大领到小，认证红包顺序领取
						otdtMap = timeList.get(timeList.size()-1);
					}else {//老用户随机
						int rand = new Random().nextInt(timeList.size());
						otdtMap = timeList.get(rand);
					}
				}
				
			}else if(isbig||btype==3){//新用户从大领到小，认证红包顺序领取
				otdtMap = otdList.get(0);
			}else {//老用户随机
				int rand = new Random().nextInt(otdList.size());
				otdtMap = otdList.get(rand);
			}
		}
		Integer money = 0;//红包金额
		if(otdtMap!=null){
			otdid = (Long)otdtMap.get("id");
			money = (Integer)otdtMap.get("amount");
		}else {
			//没有红包了
			request.setAttribute("tipwords", tname+"已抢完");
			request.setAttribute("isover", 0);
			return "caibouns";
		}
		request.setAttribute("mobile", mobile);
		request.setAttribute("money", StringUtils.formatDouble(money));
				
		
		//抢券，返回1成功，-1已被抢走
		int ret = daService.update("update order_ticket_detail_tb set uin = ?,ttime=? ,type=?  where id = ? and uin is null", 
				new Object[]{uin,ntime,utype,otdid});
		logger.error(">>>>>>>>抢停车券，车主："+mobile+",uin:"+uin+",金额："+money+",结果 "+ret);
		
		if(ret == 1){//红包领取成功,写入停车券表
			Long ttime = TimeTools.getToDayBeginTime();
			Long etime = ttime+16*24*60*60-1;
			if(btype==5)//打灰机券两天有效期
				etime = ttime + 3*24*60*60-1;
			else if(btype!=2){//其它除专用券外都是三天有效期
				etime = ttime + 4*24*60*60-1;
			}
			Long ticketId = daService.getkey("seq_ticket_tb");
			String tsql = "insert into ticket_tb (id,create_time,limit_day,money,state,uin,type,comid) values(?,?,?,?,?,?,?,?) ";
			int _ret = daService.update(tsql, new Object[]{ticketId,ttime,etime,money,0,uin,type,comid});
			
			if(_ret==1){//停车券编号写入红包详情表
				daService.update("update order_ticket_detail_tb set ticketid=? where id =?", new Object[]{ticketId,otdid});
			}
			logger.error(">>>>抢停车券，车主："+mobile+",ticketid:"+ticketId+",uin:"+uin+",金额："+money+",写券给用户："+_ret);
			
			//更新红包表的领取时间或领取结束时间 
			Long btime = (Long)bMap.get("btime");
			int result = 0;
			if(btime==null){
				result = daService.update("update order_ticket_tb set btime=? where id=?  ", new Object[]{ntime,bid});
				logger.error(">>>开始领取红包...."+result);
			}else {
				Long count = daService.getLong("select count(id) from order_ticket_detail_tb where otid=? and uin is not null", new Object[]{bid});
				if(count==bums){
					result = daService.update("update order_ticket_tb set etime = ? where id =? ", new Object[]{ntime,bid});
					logger.error(">>>已领完红包...."+result);
				}
			}
		}
		
		//查出所有领取的红包
		getBonusList(request, bid, uin);
		return "bounsret";
	}
	public int setWxUser(String openid,String acc_token,String mobile,Long uin,Long otid){
		//写入微信名及头像地址
		Map userMap = daService.getMap("select wxp_openid,wx_name,wx_imgurl from user_info_Tb where id =? ", new Object[]{uin});
		String userOpenId = (String)userMap.get("wxp_openid");
		if(userOpenId!=null&&userOpenId.length()>2){
			if(!userOpenId.equals(openid)){
				return -1;
			}
		}
		if(userMap!=null){
			String wxname = (String)userMap.get("wx_name");
			if(wxname!=null)
				wxname=wxname.replace("'", "").replace("\"", "");
			
			String wxurl =(String)userMap.get("wx_imgurl");
			if(wxname==null||wxurl==null ||wxurl.length()<2 ){
				String url = "https://api.weixin.qq.com/sns/userinfo?access_token="+acc_token+"&openid="+openid+"&lang=zh_CN";
				String result = CommonUtil.httpsRequest(url, "GET", null);
				JSONObject retmap =null;
				if(result!=null){
					retmap = JSONObject.fromObject(result);
				}
				logger.error(">>>>>>>>return wxuserinfo map :"+retmap);
				if(retmap != null && retmap.get("nickname") != null){
					wxname = retmap.getString("nickname");
					if(wxname!=null)
						wxname = wxname.replace("'", "").replace("\"", "");
					wxurl = retmap.getString("headimgurl");
					if(wxname!=null){
						//保存到数据库
						int rets = daService.update("update user_info_tb set wx_imgurl=? ,wx_name=? where id = ? ", new Object[]{wxurl,wxname,uin});
						if(rets==1)
							ZldMap.removeUser(uin);
						logger.error(">>>uin save wxname("+wxname+") and wxurl("+wxurl+"):"+rets);
					}
				}
			}
		}
		//保存openid
		if(openid!=null&&!"".equals(openid)){
			logger.error(">>保存openid:mobile="+mobile+",ret="+daService.update("update user_info_Tb set wxp_openid=? where id =? ", new Object[]{openid,uin}));
			publicMethods.sharkbinduser(openid, uin, 0L);
		}
		
		return 1;
	}
	
	/**
	 * 拆红包列表 
	 * @param request
	 * @param uid
	 * @param bid
	 * @return
	 */
	private String caiBonusList(HttpServletRequest request,Long uin,Long bid) {
		Map bMap = daService.getMap("select bwords,bnum,uin,money,type from order_ticket_tb where id = ? ", new Object[]{bid});
		Integer type = 0;//0普通停车券 1微信打券
		String tpic = "haveget";
		String fontColor="e38165";
		if(uin==null)
			tpic ="ticket";
		String mname="元";
		String tname="停车券";
				
		if(bMap!=null){
			type = (Integer)bMap.get("type");
			if(type==1){
				type=2;
				tpic="haveget_wx";
				if(uin==null)
					tpic ="ticket_wx";
				mname="折";
				tname="折扣券";
			}else if(type == 2){
				type = 1;
				tpic="haveget_limit";
				if(uin==null)
					tpic ="ticket_limit";
				tname="专用券";
			}else if(type==5){
				request.setAttribute("fly", "_fly");
				fontColor="8bd3a3";
				tname="灰机券";
			}
			String bwords = (String)bMap.get("bwords");
			if(bwords!=null&&bwords.length()>14)
				bwords = bwords.substring(0,12)+"...";
			request.setAttribute("bwords",bwords);
			request.setAttribute("bnum",bMap.get("bnum"));
			Map userMap = daService.getMap("select wx_imgurl from user_info_tb where id=? ", new Object[]{bMap.get("uin")});
			if(userMap!=null&&userMap.get("wx_imgurl")!=null)
				request.setAttribute("carowenurl",userMap.get("wx_imgurl"));
			if(type==0||type==5)
				request.setAttribute("btotal","(共"+ bMap.get("money")+"元)");
		}
		request.setAttribute("tname", tname);
		request.setAttribute("mname", mname);
		request.setAttribute("tpic", tpic);
		request.setAttribute("btype", type);
		request.setAttribute("fontColor", fontColor);
		getBonusList(request, bid, uin);
		return "bounslist";
	}

	/**查红包结果*/
	private void getBonusList(HttpServletRequest request,Long bid,Long uin){
		//查出所有领取的红包
		List<Map<String, Object>> blList = daService.getAll("select o.id,amount,ttime,wx_name,wx_imgurl,wxp_openid,o.uin,o.ticketid,u.mobile from order_ticket_detail_tb o left join user_info_tb u on o.uin=u.id where otid=? and ttime is not null ", new Object[]{bid});
		String data = "[]";
		if(blList!=null&&!blList.isEmpty()){
			data = "[";
			for(Map<String, Object> map : blList){
				Long time = (Long)map.get("ttime");
				String wxname = (String)map.get("wx_name");
				if(wxname!=null)
					wxname=wxname.replace("'", "").replace("\"", "");
				String wxurl = (String)map.get("wx_imgurl");
				if(wxname==null){
					wxname = map.get("mobile")+"";
					if(wxname.length()>10){
						wxname = wxname.substring(0,3)+"****"+wxname.substring(7);
					}else {
						wxname="车主...";
					}
				}
				if(wxurl==null||wxurl.length()<2){
					wxurl ="images/bunusimg/defaulthead.png";
				}
				Long ouin = (Long)map.get("uin");
				if(uin!=null&&ouin.equals(uin)){//查车
					request.setAttribute("tid", map.get("ticketid"));
					request.setAttribute("uin",uin);
				}
				data +="{\"amount\":\""+StringUtils.formatDouble(map.get("amount"))+"\"," +
						"\"ttime\":\""+TimeTools.getTime_yyMMdd_HHmm(time*1000).substring(3)+"\"," +
						"\"wxname\":\""+wxname+"\",\"wxurl\":\""+wxurl+"\"},";
			}
			data = data.substring(0,data.length()-1);
			data +="]";
			request.setAttribute("haveget", blList.size());
		}else {
			request.setAttribute("haveget", 0);
		}
		request.setAttribute("havegetpic", "haveget");
		request.setAttribute("data", data);
	}
	
	private Map<String, Object> getProfile(Long uin){
		Map<String, Object> infoMap = new HashMap<String, Object>();
		Map profileMap = daService.getPojo("select * from user_profile_tb where uin=?",new Object[]{uin});
		if(profileMap!=null){
			infoMap.put("low_recharge", profileMap.get("low_recharge"));
			infoMap.put("voice_warn", profileMap.get("voice_warn"));
			infoMap.put("auto_cash", profileMap.get("auto_cash"));
			infoMap.put("enter_warn",  profileMap.get("enter_warn"));
		}else {
//			infoMap.put("info", "-1");
//			infoMap.put("message", "没有设置");
		}
		return infoMap;
	}
	/**
	 * 我的包月产品
	 * @param request
	 * @param uin
	 * @return
	 * http://127.0.0.1/zld/carowner.do?action=products&mobile=15375242041 ==
	 */ 
	private List<Map<String, Object >> products(HttpServletRequest request,Long uin){
		Long ntime = System.currentTimeMillis()/1000;
		List<Map<String, Object >> infoList = new ArrayList<Map<String,Object>>();
		Long curtime = ntime;
		List<Map> pList = daService.getAll("select p.id prodid,p.p_name,p.b_time pb ,p.e_time pe " +
				//",p.remain_number" +
				",p.price,p.bmin,p.emin,c.id cid,c.b_time cb,c.e_time ce,c.create_time ,t.company_name " +
				"from carower_product c ,product_package_tb p ,com_info_tb t where p.comid=t.id and c.pid=p.id and uin=?", 
				new Object[]{uin});
		if(pList!=null&&pList.size()>0){
			for(Map map : pList){
				Map<String,Object> infoMap = new HashMap<String, Object>();
				Long cbtime = (Long)map.get("cb");
				Long cetime = (Long)map.get("ce");
				Integer pbtime = (Integer)map.get("pb");
				Integer petime = (Integer)map.get("pe");
				Integer bmin = (Integer)map.get("bmin");
				Integer emin = (Integer)map.get("emin");
				String limtime = pbtime+":";
				if(bmin<10)
					limtime +="0";
				limtime +=bmin+" - "+petime+":";
				if(emin<10)
					limtime+="0";
				limtime +=emin;
				Long limitDay = cetime-curtime;
				if(limitDay<0)
					limitDay =0L;
				infoMap.put("name", map.get("company_name"));
				infoMap.put("parkname", map.get("p_name"));
				infoMap.put("price", map.get("price"));
				infoMap.put("limitdate",TimeTools.getTimeStr_yyyy_MM_dd(cbtime*1000)
						+" 至  "+TimeTools.getTimeStr_yyyy_MM_dd(cetime*1000));//有效期限
				infoMap.put("limittime",limtime);//时效时段
				//infoMap.put("number", map.get("remain_number"));//剩余数量
				infoMap.put("resume", map.get("resume"));//描述
				infoMap.put("limitday", limitDay/(24*60*60));//剩余天数
				infoMap.put("prodid", map.get("prodid"));//包月产品编号
				int state = 0;//月卡状态 0：未开始 1:使用中 2已过期
				if(cbtime <= curtime){
					if(cetime > curtime){
						state = 1;//正在使用中
					}else{
						state = 2;//已过期
					}
				}
				infoMap.put("state", state);
				infoList.add(infoMap);
			}
		}
		return infoList;
	}
	
	/**
	 * 当前订单，正在停车的订单，没有或只有一条
	 * @param request
	 * @param uin
	 * @return
	 * http://127.0.0.1/zld/carowner.do?action=orderdetail&orderid=786119&mobile=15801482643
	 */
	private Map<String, Object> orderDetail(HttpServletRequest request,Long uin){
		Map<String, Object> infoMap = new HashMap<String, Object>();
		Long orderId=RequestUtil.getLong(request, "orderid", -1L);
		boolean isBolinkOrder = false;
		//处理泊链的订单详情
		Map orderMap = daService.getPojo("select o.create_time,o.end_time,o.id,o.comid," +
				"c.company_name,c.address,o.total,o.state,o.uid,o.c_type " +
				"from order_tb o,com_info_tb c where o.comid=c.id and  o.id=? and uin=? ",
				new Object[]{orderId,uin});
		if(orderMap==null||orderMap.isEmpty()){//本平台没有此订单，查询泊链平台订单
			String carNumber = publicMethods.getCarNumber(uin);
			orderMap = daService.getPojo("select start_time create_time,end_time,id," +
					"park_name company_name,union_name address,money total,state " +
					"from bolink_order_tb where  id=?  ",
					new Object[]{orderId});
			if(orderMap!=null&&!orderMap.isEmpty()){
				isBolinkOrder = true;
				orderMap.put("c_type", 3);
				orderMap.put("comid", -1);
			}
		}
		if(orderMap!=null){
			Long btime = (Long)orderMap.get("create_time");
			Long end = (Long)orderMap.get("end_time");
			//Long comId = (Long)orderMap.get("comid");
			infoMap.put("total", StringUtils.formatDouble(orderMap.get("total")));
			//infoMap.put("duration", "停车"+(end-btime)/(60*60)+"时"+((end-btime)/60)%60+"分");
			infoMap.put("btime",btime);// TimeTools.getTime_yyyyMMdd_HHmm(btime*1000).substring(10));
			infoMap.put("etime",end);// TimeTools.getTime_yyyyMMdd_HHmm(end*1000).substring(10));
			infoMap.put("ctype", orderMap.get("c_type"));
			infoMap.put("parkname", orderMap.get("company_name"));
			infoMap.put("address", orderMap.get("address"));
			infoMap.put("orderid", orderMap.get("id"));
			infoMap.put("state", orderMap.get("state"));
			infoMap.put("parkid",  orderMap.get("comid"));
			if(!isBolinkOrder){
				Long uid= (Long)orderMap.get("uid");
				if(uid!=null){
					Map userMap = daService.getMap("select mobile,nickname from user_info_Tb where id =? ",new Object[]{uid});
					if(userMap!=null){
						infoMap.put("payee","{\"id\":\""+uid+"\",\"name\":\""+userMap.get("nickname")+"\",\"mobile\":\""+userMap.get("mobile")+"\"}");
					}
				}
				Map bMap= daService.getMap("select id from order_ticket_tb where order_id=? and type=? and etime is null", new Object[]{orderMap.get("id"),0});
				if(bMap!=null&&bMap.get("id")!=null){
					infoMap.put("bonusid", bMap.get("id"));
				}
				infoMap.put("reward","0");
				infoMap.put("comment","0");
				Long count = daService.getLong("select count(id) from parkuser_reward_tb where uin=? and order_id=? ", new Object[]{uin,orderId});
				if(count>0){
					infoMap.put("reward","1");
				}else{
					count = daService.getLong("select count(*) from parkuser_reward_tb where uin=? and ctime>=? and uid=? ",
							new Object[] { uin, TimeTools.getToDayBeginTime(), uid });
					if(count > 0){
						infoMap.put("reward","1");
					}
				}
				count = daService.getLong("select count(ID) from parkuser_comment_tb where uin=? and order_id=? ", new Object[]{uin,orderId});
				if(count>0){
					infoMap.put("comment","1");
				}
			}else{
				infoMap.put("reward","1");
				infoMap.put("comment","1");
				//infoMap.put("bonusid",0);
				infoMap.put("uid","10000");
				infoMap.put("payee","{\"id\":\"10000\",\"name\":\"第三方收费员\",\"mobile\":\"\"}");
			}
			logger.error(infoMap);
			//infoMap.put("begintime", TimeTools.getTime_yyyyMMdd_HHmm(btime*1000));
			//infoMap.put("endtime", TimeTools.getTime_yyyyMMdd_HHmm(end*1000));
		}else {
//			infoMap.put("info", "-1");
//			infoMap.put("message", "没有订单");
		}
		return infoMap;
	}
	
	/**
	 * 当前订单，正在停车的订单，没有或只有一条
	 * @param request
	 * @param uin
	 * @return
	 */
	private Map<String, Object> currOrder(HttpServletRequest request,Long uin){
		Long ntime = System.currentTimeMillis()/1000;
		Map<String, Object> infoMap = new HashMap<String, Object>();
		Map orderMap = daService.getPojo("select o.create_time,o.id,o.comid,o.car_type,c.company_name,c.address,o.state,o.pid from order_tb o,com_info_tb c where o.comid=c.id and o.uin=? and o.state=?",
				new Object[]{uin,0});
		if(orderMap!=null){
			Long btime = (Long)orderMap.get("create_time");
			Long end = ntime;
			Long comId = (Long)orderMap.get("comid");
			Integer pid = (Integer)orderMap.get("pid");
			Integer car_type = (Integer)orderMap.get("car_type");//0：通用，1：小车，2：大车
			if(pid>-1){
				infoMap.put("total",publicMethods.getCustomPrice(btime, end, pid));
			}else {
				infoMap.put("total",publicMethods.getPrice(btime, end, comId, car_type));	
			}
			
			//infoMap.put("duration", "已停"+(end-btime)/(60*60)+"时"+((end-btime)/60)%60+"分");
			infoMap.put("btime", btime);//TimeTools.getTime_yyyyMMdd_HHmm(btime*1000).substring(10));
			infoMap.put("etime",end);// TimeTools.getTime_yyyyMMdd_HHmm(end*1000).substring(10));
			infoMap.put("parkname", orderMap.get("company_name"));
			infoMap.put("address", orderMap.get("address"));
			infoMap.put("orderid", orderMap.get("id"));
			infoMap.put("state",orderMap.get("state"));
			infoMap.put("parkid", comId);
			//infoMap.put("begintime", TimeTools.getTimeStr_yyyy_MM_dd(btime*1000));
		}
		return infoMap;
	}
	
	/**
	 * 当前订单，新接口，优先返回三分钟内已结算(state=1)的未用手机支付过(pay_type!=2)的订单，没有返回未结算(state=0)的一条订单
	 * @param request
	 * @param uin
	 * @return
	 */
	private Map<String, Object> currentOrder(HttpServletRequest request,Long uin){
		Long ntime = System.currentTimeMillis()/1000;
		Long comid = RequestUtil.getLong(request, "comid", -1L);//20141213新增查询当前订单时传入车场编号，用于扫描支付订单时排除其它车场的订单
		Long orderId = RequestUtil.getLong(request, "orderid", -1L);//从扫码接口跳转到这里时，有订单编号 
		Map<String, Object> infoMap = new HashMap<String, Object>();
		//查询
		Map orderMap =null;
		String sql = null;
		Object[] values =null;
		boolean isBolinkOrder=false;
		if(orderId!=-1){
			orderMap = daService.getMap("select create_time,id,comid,state,total,pid,car_type " +
				"from order_tb  where id=?", new Object[]{orderId});
		}else {
			sql = "select create_time,end_time,id,comid,state,total,pid,car_type " +
					"from order_tb where uin=? and state=? and pay_type=? and end_time >? "; //order by o.end_time desc 
//			sql = "select o.create_time,o.end_time,o.id,o.comid,c.company_name,c.address,o.state,o.total,o.pid,o.car_type " +
//					"from order_tb o,com_info_tb c where o.comid=c.id and o.uin=? and o.state=? and pay_type=? " +
//					"and end_time >? "; //order by o.end_time desc 
			values = new Object[]{uin,1,1,ntime-15*60};
			if(comid!=-1){
				sql +=" and comid= ?";
				values = new Object[]{uin,1,1,ntime-15*60,comid};
			}
			orderMap= daService.getMap(sql+" order by end_time desc ",values);
			if(orderMap==null){
				orderMap = daService.getMap("select * " +
						" from bolink_order_tb where uin =? and state=?  order by  id desc ",new Object[]{uin,0});
				if(orderMap!=null&&!orderMap.isEmpty())//是泊链订单
					isBolinkOrder = true;
			}
		}
		if(!isBolinkOrder){
			boolean isover = true;
			if(orderMap==null||orderMap.isEmpty()){
				sql = "select o.create_time,o.id,o.comid,o.state,o.pid,o.car_type " +
						"from order_tb o  where o.uin=? and o.state=?  ";//order by o.end_time desc
				values = new Object[]{uin,0};
				if(comid!=-1){
					sql +=" and o.comid= ?";
					values = new Object[]{uin,0,comid};
				}
				orderMap = daService.getMap(sql+ " order by o.end_time desc",values);
				isover=false;
			}
			if(orderMap!=null){
				Integer pid = (Integer)orderMap.get("pid");
				Long btime = (Long)orderMap.get("create_time");
				
				Long end = ntime;
				if(orderMap.get("end_time")!=null)
					end = (Long)orderMap.get("end_time");
				Long comId = (Long)orderMap.get("comid");
				
				if(comId!=null&&comId>0){//查车场名称及地址
					Map cMap = daService.getMap("select company_name,address from com_info_tb where id=? ", new Object[]{comId});
					if(cMap!=null){
						infoMap.put("parkname", cMap.get("company_name"));
						infoMap.put("address", cMap.get("address"));
					}
				}
				
				Integer car_type = (Integer)orderMap.get("car_type");//0：通用，1：小车，2：大车
				Double total =StringUtils.formatDouble(orderMap.get("total"));
				Integer state = (Integer)orderMap.get("state");
				if(isover&&state!=null&&state==1)//已结算，返回订单中的结算价格
					infoMap.put("total",total);
				else {//未结算，计算订单价格
					if(pid>-1){
						infoMap.put("total",publicMethods.getCustomPrice(btime, end, pid));
					}else {
						infoMap.put("total",publicMethods.getPrice(btime, end, comId, car_type));	
					}
					//infoMap.put("total", publicMethods.getPrice(btime, end, comId));
				}
				infoMap.put("btime", btime);
				infoMap.put("etime",end);
				
				infoMap.put("orderid", orderMap.get("id"));
				infoMap.put("state",orderMap.get("state"));
				infoMap.put("parkid", comId);
				//infoMap.put("begintime", TimeTools.getTimeStr_yyyy_MM_dd(btime*1000));
			}else {
//			infoMap.put("info", "-1");
//			infoMap.put("message", "没有订单");
			}
		}else {
			//调用泊链查询价格
			Long updateTime = (Long)orderMap.get("update_time");
			double money =0.0;
			if(updateTime!=null&&ntime-updateTime<60){
				money = StringUtils.formatDouble(orderMap.get("money"));
			}
			if(money==0){//没有15分钟内的缓存价格，重新查询
				Map<String,Object> oMap =  publicMethods.catBolinkOrder((Long)orderMap.get("id"), 
						(String)orderMap.get("order_id"), (String)orderMap.get("plate_number"),null,0);
				if(oMap!=null)
					money =StringUtils.formatDouble(oMap.get("money"));
			}
			infoMap.put("parkname", orderMap.get("union_name")+"-"+orderMap.get("park_name"));
			infoMap.put("address",orderMap.get("park_name"));
			infoMap.put("btime", orderMap.get("start_time"));
			infoMap.put("etime",ntime);
			infoMap.put("total",money);
			infoMap.put("orderid", orderMap.get("id"));
			infoMap.put("state",orderMap.get("state"));
			infoMap.put("parkid",  orderMap.get("id"));
		}
		System.err.println(">>>>>>>>>>>>>>>current order:>"+infoMap);
		return infoMap;
	}
	
	//购买包月，http://127.0.0.1/zld/carowner.do?action=buyproduct&productid=2&mobile=15375242041&number=&start=&etc_id=
	private int buyProduct(HttpServletRequest request,Long uin){
		Long pid = RequestUtil.getLong(request, "productid", -1L);
		Integer number = RequestUtil.getInteger(request, "number", 0);
		String start = RequestUtil.processParams(request, "start");
		String etcId = RequestUtil.processParams(request, "etc_id");
		int result = 0;
		if(pid!=-1&&number>0&&!start.equals("")){
			Map productMap = daService.getMap("select * from product_package_tb where id=?", new Object[]{Long.valueOf(pid)});
			result=publicMethods.buyProducts(uin, productMap, number, start,etcId,0);
		}
		return result;
	}
	//http://127.0.0.1/zld/carowner.do?action=parkproduct&comid=3&mobile=15801482643
	private List<Map<String, Object>> parkProduct(Long comId,Long uin){
		List<Map<String, Object>> infoMList = new ArrayList<Map<String,Object>>();
		List<Map> productList = daService.getAll("select * from product_package_tb where comid=? and state =?", new Object[]{comId,0});
		List<Map> myProductId = daService.getAll("select pid from carower_product where uin=?  ", new Object[]{uin});
		if(productList!=null&&productList.size()>0){
			for(Map map :productList){
				Map<String, Object> infoMap = new HashMap<String, Object>();
				Long pid = (Long)map.get("id");
				String isBuy = "0";
				if(myProductId!=null&&myProductId.size()>0)
				for(Map map2: myProductId){
					Long _pid = (Long)map2.get("pid");
					if(pid.intValue()==_pid.intValue()){
						isBuy="1";
						break;
					}
				}
				String bmin = map.get("bmin")+"";
				String emin = map.get("emin")+"";
				if(bmin.equals("null")||bmin.equals("0"))
					bmin="00";
				if(emin.equals("null")||emin.equals("0"))
					emin = "00";
				infoMap.put("id", map.get("id"));
				infoMap.put("name", map.get("p_name"));
				infoMap.put("price",map.get("price"));
				infoMap.put("limittime", map.get("b_time")+":"+bmin+"-"+map.get("e_time")+":"+emin);
				infoMap.put("number",map.get("remain_number"));
				infoMap.put("isbuy", isBuy);
				infoMap.put("resume", map.get("resume"));
				infoMList.add(infoMap);
			}
		}
		return infoMList;
	}
	//  http://127.0.0.1/zld/carowner.do?action=parkdetail&comid=3
	private Map<String, Object> parkDetail(HttpServletRequest request,Long comId,Long uin){

		Map<String, Object> infoMap = new HashMap<String, Object>();
		//车位数/占用车位数，车位总数，停车费信息，地址，类型，电话，分类 
		/*  currentPrice;// 当前价格，不用返回了 。
		 *  praiseNum;// "赞一个"次数
			disparageNum;// "贬一个"次数
			hasPraise;// 当前用户是否赞过该停车场：1-->赞过，0-->没赞过
			//description;// 车场描述
			//nfc;// 是否支持nfc：1--支持，0-->不支持，下同
			//etc;// 是否支持etc
			//book;// 是否支持预定
			//navi;// 是否支持室内导航
			//monthlyPay;// 是否支持月卡
			//photoUrl;//停车场照片url地址集合，多张图片
		 */
		Map comMap = daService.getPojo("select * from com_info_tb where id=?", new Object[]{comId});
		//查图片
		List<Map<String, Object>> picMap = daService.getAll("select picurl from com_picturs_tb where comid=? order by id desc limit ?",
				new Object[]{comId,1});
		String picUrls = "[";
		if(picMap!=null&&!picMap.isEmpty()){
			for(Map<String, Object> map : picMap){
				if(picUrls.equals("["))
					picUrls += "\""+map.get("picurl")+"\"";
				else {
					picUrls += ",\""+map.get("picurl")+"\"";
				}
			}
		}
		picUrls += "]";
		//查评价
		int praiseNum=0;
		int disparageNum=0;
		int hasPraise=-1;
		if(uin!=null&&uin>0){
			List<Map<String, Object>> praiseMap = daService.getAll("select * from com_praise_tb where comid=?",new Object[]{comId});
			logger.info(praiseMap);
			logger.info(uin);
			if(praiseMap!=null&&!praiseMap.isEmpty()){
				for(Map<String, Object> map :praiseMap){
					Long uid = (Long) map.get("uin");
					Integer praise = (Integer)map.get("praise");
					if(praise==0)
						disparageNum++;
					else
						praiseNum++;
					if(uin!=null&&uin.intValue()==uid.intValue()){
						hasPraise=praise;
					}
				}
			}
		}
		if(comMap!=null){
			Long used = pService.getLong("select count(*) from order_tb where comid=? and state=?", new Object[]{comId,0}) ;
			Integer shareNumbre =  (Integer)comMap.get("share_number");
			Long updateTime = (Long)comMap.get("update_time");
			
			infoMap.put("total", comMap.get("parking_total"));
			//infoMap.put("share", shareNumbre-used);
			infoMap.put("address", comMap.get("address"));
			//infoMap.put("parkname", comMap.get("company_name"));
			infoMap.put("mobile", comMap.get("mobile"));
			infoMap.put("updatetime", TimeTools.getTime_yyyyMMdd_HHmm(updateTime*1000 ));
			infoMap.put("parking_type", comMap.get("parking_type"));//类型：0在面，1地下，2占道
			infoMap.put("stop_type", comMap.get("stop_type"));//分类：0平面，1立体
			//infoMap.put("used", used);//已占用数
			infoMap.put("photoUrl", picUrls);//已占用数
			infoMap.put("nfc", comMap.get("nfc"));//是否支持nfc：1--支持，0-->不支持，下同
			infoMap.put("etc", comMap.get("etc"));//是否支持etc
			infoMap.put("book", comMap.get("book"));//是否支持室内导航
			infoMap.put("navi", comMap.get("navi"));//车场描述
			infoMap.put("epay", comMap.get("epay"));//车场描述
			infoMap.put("monthlyPay", comMap.get("monthlypay"));//是否支持月卡
			infoMap.put("praiseNum", praiseNum);//"赞一个"次数
			infoMap.put("disparageNum", disparageNum);//"贬一个"次数
			infoMap.put("hasPraise", hasPraise);//当前用户是否赞过该停车场：1-->赞过，0-->没赞过
			infoMap.put("description", comMap.get("resume")==null?"":comMap.get("resume"));//车场描述
			infoMap.put("freeSpace", shareNumbre-used);//// 空闲车位数
			infoMap.put("currentPrice",getPrice(comId));// 当前首小时价格
		}
		return infoMap;
	}
	private Map<String, Object> detail(String mobile,Map userMap){
		Long ntime = System.currentTimeMillis()/1000;
		Map<String, Object> infoMap = new HashMap<String, Object>();
		//余额，车牌，电话
		if(userMap!=null){
			Long uin  = (Long)userMap.get("id");
			List<Map<String, Object>> carList = daService.getAll("select car_number,is_auth from car_info_tb where uin = ? order by is_auth", new Object[]{uin});
			Integer state  = 0;
			String carNumber = "";
			if(carList!=null&&!carList.isEmpty()){
				List<Integer> sList = new ArrayList<Integer>();
				for(Map<String, Object> car : carList){
					sList.add((Integer)car.get("is_auth"));
					carNumber= (String)car.get("car_number");
				}
				if(sList.size()==1)
					state = sList.get(0);
				else {
					if(sList.contains(-1))
						state=-1;
					else if (sList.contains(2)){
						state=2;
					}else if(sList.contains(0)){
						state=0;
					}else if(sList.contains(1))
						state=1;
				}
			}
			Double balance = Double.valueOf(userMap.get("balance")+"");
			infoMap.put("balance", balance);
			infoMap.put("mobile", mobile);
			infoMap.put("carNumber",carNumber);
			//添加两个字段,信用额度和审核状态
			Integer uIsAuth = (Integer)userMap.get("is_auth");//车主是否已认证
			if(uIsAuth!=null&&uIsAuth==1){
				infoMap.put("limit_balan", userMap.get("credit_limit"));//可用信用额度
				infoMap.put("limit", 30);//信用额度
				infoMap.put("limit_warn",10);//信用额度告警
			}else {
				infoMap.put("limit_balan", 0);//可用信用额度
				infoMap.put("limit", 0);//信用额度
				infoMap.put("limit_warn",0);//信用额度
			}
			infoMap.put("state", state);//车牌审核状态 0:未认证，1:已认证，2:认证中
			//红包查询
			Long count  =daService.getLong("select count(ID) from order_ticket_tb where uin=? and money>? and  exptime>? and btime is null ",
					new Object[]{userMap.get("id"),0,ntime});
			if(count==0)
				infoMap.put("bonusid", "");//大于0，表示有红包未开抢
			else
				infoMap.put("bonusid", count);//大于0，表示有红包未开抢
			//			Long bid = null;
//			if(bMap!=null&&bMap.get("id")!=null)
//				bid = (Long)bMap.get("id");
//			if(bid!=null&&bid>0){
//				infoMap.put("bonusid", bid);
//			}
		}
		return infoMap;
	}
	
	private String getCarNumber(Long uin){
		Map carInfoMap = daService.getPojo("select car_number from car_info_tb where uin=? ",
				new Object[]{uin});
		if(carInfoMap!=null&&carInfoMap.get("car_number")!=null)
			return (String)carInfoMap.get("car_number");
		return "";
	}
	
	private List<Map<String, Object>> historyOrder(HttpServletRequest request,Long uin){
		//日期，停车场名称，总价
		
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "size", 20);
		List<Object> params = new ArrayList<Object>();
		params.add(1);
		params.add(uin);
		params.add(1);
		//Long time = TimeTools.getToDayBeginTime();
		List<Map<String, Object>> infoMaps = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> list = daService.getAll("select t.id, t.create_time,t.total,c.company_name from order_tb t,com_info_tb c " +
				"where t.comid=c.id and  t.state=? and t.uin=? and t.pay_type>? order by t.end_time desc",// and create_time>?",
				params, pageNum, pageSize);
		params.clear();
		//String car_number = publicMethods.getCarNumber(uin);
		params.add(uin);
		params.add(1);
		//泊链停车订单
		List<Map<String, Object>> carStopList = daService.getAll("select id,money as total,start_time as create_time,park_name as company_name from" +
				" bolink_order_tb where uin=? and state =?  ",params,pageNum,pageSize);
		if(list!=null){
			if(carStopList!=null)
				list.addAll(carStopList);
		}else {
			list = carStopList;
		}
		if(carStopList!=null){//排序，按create_time 降序
			Collections.sort(list, new OrderSortCompare());
		}
		if(list!=null&&list.size()>0){
			for(Map map : list){
				Map<String, Object> info = new HashMap<String, Object>();
				Long ctime = (Long)map.get("create_time");
				info.put("date", TimeTools.getTimeStr_yyyy_MM_dd(ctime*1000));
				info.put("total", StringUtils.formatDouble(map.get("total")));
				info.put("parkname", map.get("company_name"));
				info.put("orderid", map.get("id"));
				infoMaps.add(info);
			}
		}else {
			Map<String, Object> info = new HashMap<String, Object>();
//			info.put("info", "没有记录");
//			infoMaps.add(info);
		}
		System.err.println(infoMaps);
		return infoMaps;
	}

	/**
	 * 取首小时价格
	 * @param parkId
	 * @return
	 */
	private String getPrice(Long parkId){
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		//开始小时
		int bhour = calendar.get(Calendar.HOUR_OF_DAY);
		List<Map> priceList=daService.getAll("select * from price_tb where comid=? " +
				"and state=? and pay_type=? order by id desc", new Object[]{parkId,0,0});
		if(priceList==null||priceList.size()==0){//没有按时段策略
			//查按次策略
			priceList=daService.getAll("select * from price_tb where comid=? " +
					"and state=? and pay_type=? order by id desc", new Object[]{parkId,0,1});
			if(priceList==null||priceList.size()==0){//没有按次策略，返回提示
				return "0元/次";
			}else {//有按次策略，直接返回一次的收费
				Map timeMap =priceList.get(0);
				Integer unit = (Integer)timeMap.get("unit");
				if(unit!=null&&unit>0){
					if(unit>60){
						String t = "";
						if(unit%60==0)
							t = unit/60+"小时";
						else
							t = unit/60+"小时 "+unit%60+"分钟";
						return timeMap.get("price")+"元/"+t;
					}else {
						return timeMap.get("price")+"元/"+unit+"分钟";
					}
				}else {
					return timeMap.get("price")+"元/次";
				}
				//return timeMap.get("price")+"元/次";
			}
			//发短信给管理员，通过设置好价格
		}else {//从按时段价格策略中分拣出日间和夜间收费策略
			if(priceList.size()>0){
				logger.info(priceList);
				for(Map map : priceList){
					Integer btime = (Integer)map.get("b_time");
					Integer etime = (Integer)map.get("e_time");
					Double price = Double.valueOf(map.get("price")+"");
					Double fprice = Double.valueOf(map.get("fprice")+"");
					Integer ftime = (Integer)map.get("first_times");
					if(ftime!=null&&ftime>0){
						if(fprice>0)
							price = fprice;
					}
					if(btime<etime){//日间 
						if(bhour>=btime&&bhour<etime){
							return price+"元/"+map.get("unit")+"分钟";
						}
					}else {
						if(bhour>=btime||bhour<etime){
							return price+"元/"+map.get("unit")+"分钟";
						}
					}
				}
			}
		}
		return "0.0元/小时";
	}
	
	private String getWeek(int week){
		switch (week) {
		case 2:
			return "一";
		case 3:
			return "二";
		case 4:
			return "三";
		case 5:
			return "四";
		case 6:
			return "五";
		case 7:
			return "六";
		case 1:
			return "日";
		}
		return "";
	}
	

	/**
	 * 调用微信接口，取用户的openid
	 * @param request
	 * @return [opedid,access_token]
	 */
	private String[] getOpenid(HttpServletRequest request){
		String code = RequestUtil.processParams(request, "code");
		logger.error(">>>>>>>>code:"+code+",comfig appid:");
		if(code==null||"".equals(code))
			return null;
		String appid = Constants.WXPUBLIC_APPID;
		String secret=Constants.WXPUBLIC_SECRET;
		String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+appid+"&secret="+secret+"&code="+code+"&grant_type=authorization_code";
		logger.error(">>>>>>>>access_token_url:"+access_token_url);
		String result = CommonUtil.httpsRequest(access_token_url, "GET", null);
		JSONObject map =null;
		if(result!=null){
			map = JSONObject.fromObject(result);
		}
		if(map == null || map.get("errcode") != null){
			return null;
		}
		String openid = (String)map.get("openid");
		String accToken = (String)map.get("access_token");
		logger.error(">>>>>>>>return map :"+map);
		return new String[]{openid,accToken};
	}
	/**
	 * 根据openid 查用户手机
	 * @param openid
	 * @return
	 */
	private Map getMobileByOpenid(String openid){
		Map<String, Object> userMap= null;
		if(openid!=null&&!openid.equals("")){
			userMap = daService.getMap("select id, mobile,wx_name,wx_imgurl from user_info_tb where state=? and auth_flag=? and wxp_openid=? ",
							new Object[] { 0, 4, openid });
		}
		return userMap;
	}
	
	public Long getUinByMobile(String mobile){
		if(!"".equals(mobile)){
			Map userMap = daService.getPojo("select id from user_info_Tb where mobile=? and auth_flag=? ", new Object[]{mobile,4});
			if(userMap!=null){
				return (Long) userMap.get("id");
			}
		}
		return -1L;
	}
	
	
	public int backTickets(Long uin,Integer num){
		//2015-03-10，开卡及领红包时不再先写入停车券，登录时判断黑名单后添加停车券
		String tsql = "insert into ticket_tb (create_time,limit_day,money,state,uin) values(?,?,?,?,?) ";
		List<Object[]> values = new ArrayList<Object[]>();
		Long ntime =TimeTools.getToDayBeginTime();
		for(int i=0;i<num;i++){
			Object[] v1 = new Object[]{ntime,ntime+6*24*60*60-1,1,0,uin};
			Object[] v2 = new Object[]{ntime,ntime+6*24*60*60-1,2,0,uin};
			Object[] v3 = new Object[]{ntime,ntime+6*24*60*60-1,3,0,uin};
			Object[] v4 = new Object[]{ntime,ntime+6*24*60*60-1,4,0,uin};
			values.add(v1);values.add(v2);values.add(v3);values.add(v4);
		}
		int result= daService.bathInsert(tsql, values, new int[]{4,4,4,4,4});
		if(result>0){
			logService.insertUserMesg(5, uin, "恭喜您获得30元停车券!", "停车券提醒");
		}
		return result;
	}
}


