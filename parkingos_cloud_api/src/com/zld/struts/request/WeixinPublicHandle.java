package com.zld.struts.request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pay.Constants;

import com.zld.impl.CommonMethods;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.LogService;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.utils.ZLDType;
import com.zld.weixinpay.utils.util.XMLUtil;
import com.zld.wxpublic.util.PayCommonUtil;

public class WeixinPublicHandle extends HttpServlet {
	private ServletContext servletContext;

	private DataBaseService daService;
	private PublicMethods publicMethods;
	private LogService logService;
	private CommonMethods commonMethods;

	private Logger logger = Logger.getLogger(WeixinPublicHandle.class);
	private static final long serialVersionUID = 4942068508811134127L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		InputStream inStream = request.getInputStream();
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);
		}
		outSteam.close();
		inStream.close();
		String result = new String(outSteam.toByteArray(), "utf-8");// 获取微信调用我们notify_url的返回信息
//		System.out.println(result);
		
		try {
			SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
			Map<Object, Object> map = XMLUtil.doXMLParse(result);
			for(Object keyValue : map.keySet()){
	            packageParams.put(keyValue, map.get(keyValue));
	        }
			
			if(map.get("return_code").toString().equalsIgnoreCase("SUCCESS")){
				String sign = PayCommonUtil.createSign("UTF-8", packageParams);//回调验证签名
				String signreturn = (String)map.get("sign");
				if (signreturn.equals(sign) && map.get("result_code").toString().equalsIgnoreCase("SUCCESS")) {
					logger.error("=========微信公众号付款成功========");
					//TODO 本地逻辑
					String openid = (String)map.get("openid");//账户
					String total_fee = (String)map.get("total_fee");
					Double wx_total = Double.valueOf(total_fee) * 0.01;//微信支付的金额
					String attach = (String)map.get("attach");//附加参数
					String out_trade_no = (String)map.get("out_trade_no");//订单编号
					String wxp_orderid = (String)map.get("transaction_id");
					
					Long ntime = System.currentTimeMillis()/1000;
					Long ticketId = -1L;//停车券ID
					Double ticketMoney = 0d;//停车券抵消金额
					Long disTicketId = -1L;//减免券编号
					Double disTicketMoney = 0d;//减免券抵扣金额
					Double money = 0d;//花费金额
					String mobile = "";//客户手机号
					Long uid = -1L;//收费员编号
					Integer type = 0;//消费类型（ 0:直付停车费 1：泊车费2,摇一摇付车费未绑定）
					Long uin =null;
					Long orderId = -1L;
					Integer bind_flag = 1;//已绑定账户
					Integer ticketmoney = 0;//购买停车券的面值
					Integer ticketnum = 0;//购买停车券的数量
					String starttime = "";//购买月卡的初始时间
					Integer months = 0;//购买的月卡月数
					Long prodid = -1L;//月卡编号
					Long end_time = -1L;//订单结算时间,2016-07-07添加
					JSONObject jsonObject = JSONObject.fromObject(attach);
					if(jsonObject.get("ticketId") != null){
						ticketId = Long.valueOf(jsonObject.get("ticketId")+"");//停车券
					}
					if(jsonObject.get("ticketMoney") != null){
						ticketMoney = Double.valueOf((String)jsonObject.get("ticketMoney"));
					}
					if(jsonObject.get("money") != null){
						money = Double.valueOf((String)jsonObject.get("money"));
					}
					if(jsonObject.get("type") != null){
						type = Integer.valueOf((String)jsonObject.get("type"));
					}
					
					if(jsonObject.get("uid") != null){
						uid = Long.valueOf((String)jsonObject.get("uid"));
					}
					if(jsonObject.get("mobile") != null){
						mobile = (String)jsonObject.get("mobile");
					}
					if(jsonObject.get("uin") != null){
						uin = Long.valueOf((String)jsonObject.get("uin"));
					}
					if(jsonObject.get("orderid") != null){
						orderId = Long.valueOf((String)jsonObject.get("orderid"));
					}
					if(type == 0){//直付的没有订单，从数据库预取一个
						orderId = daService.getkey("seq_order_tb");
					}
					if(jsonObject.get("ticketmoney") != null){
						ticketmoney = Integer.valueOf((String)jsonObject.get("ticketmoney"));
					}
					if(jsonObject.get("ticketnum") != null){
						ticketnum = Integer.valueOf((String)jsonObject.get("ticketnum"));
					}
					if(jsonObject.get("starttime") != null){
						starttime = (String)jsonObject.get("starttime");
					}
					if(jsonObject.get("months") != null){
						months = Integer.valueOf((String)jsonObject.get("months"));
					}
					if(jsonObject.get("prodid") != null){
						prodid = Long.valueOf((String)jsonObject.get("prodid"));
					}
					if(jsonObject.get("end_time") != null){
						end_time = Long.valueOf((String)jsonObject.get("end_time"));
					}
					if(jsonObject.get("disTicketId") != null){
						disTicketId = Long.valueOf((String)jsonObject.get("disTicketId"));
					}
					if(jsonObject.get("disTicketMoney") != null){
						disTicketMoney = Double.valueOf((String)jsonObject.get("disTicketMoney"));
					}
					
					Long count = daService.getLong("select count(*) from alipay_log where notify_no=? and create_time>?",
							new Object[] { out_trade_no ,(System.currentTimeMillis()/1000-(30*60*60))});
					if(count > 0){//已经处理过，不再处理
						logger.error("订单out_trade_no:"+out_trade_no + "已经处理过，返回！");
						response.getWriter().write(PayCommonUtil.setXML("SUCCESS", ""));//告诉微信服务器，我收到信息了，不要在调用回调action了
						return;
					}
					Map<String, Object> userMap = daService.getMap(
							"select * from user_info_tb where wxp_openid=? ",
							new Object[] { openid });
					boolean isBolinkUser = false;//是否是泊链用户
					if(userMap == null){//未绑定账户
						bind_flag = 0;
					}else{
						Integer unionState = (Integer)userMap.get("union_state");//有同步状态，已同步到泊链了
						if(unionState>0)
							isBolinkUser=true;
					}
						
					Integer ret = -1;
					if(bind_flag == 0){//未绑定账户，操作虚拟账户
						Map<String, Object> nobindMap= daService.getMap("select * from wxp_user_tb where openid=? ",
								new Object[] { openid });
						logger.error("未绑定账户，微信账户："+openid + ",attach:"+attach +"type:"+type);
						if(nobindMap != null){
							uin = (Long)nobindMap.get("uin");
							ret = daService.update("update wxp_user_tb set balance =balance+? where uin=?  ",
									new Object[] { wx_total, uin });
							logger.error("未绑定账户，往虚拟账户里充值："+wx_total + "元,openid:"+openid +"type:"+type);
							
						}
					}else{//已绑定账户，操作真实帐户
						logger.error("已绑定账户，微信账户："+openid + ",attach:"+attach +"type:"+type);
						if (uin == null) {
							uin = (Long)userMap.get("id");
						}
						if(uin != null){
							ret = daService.update("update user_info_tb set balance =balance+? where id=?  ",
									new Object[] { wx_total, uin });//先充值
						}
						
					}
					logger.error("uin:"+uin+",orderid:"+orderId+",type:"+type+",money:"+money+",wx_total:"+wx_total);
					if(uin != null){
						try {
							// 写入用户账户表--充值
							daService.update("insert into user_account_tb(uin,amount,type,create_time,remark,pay_type,orderid) values(?,?,?,?,?,?,?)",
											new Object[] {uin,wx_total,0,System.currentTimeMillis() / 1000 - 2,"微信公众号充值", 9, orderId });
							
							// 扣除微信手续费
							daService.update("insert into tingchebao_account_tb(amount,type,create_time,remark,utype,orderid) values(?,?,?,?,?,?)",
											new Object[] {wx_total * 0.006,1,System.currentTimeMillis() / 1000 - 2,"微信公众号充值手续费", 5, orderId });
							
							//写用户支付账号信息表
							logService.insertUserAccountMesg(2, uin, openid);
						} catch (Exception e) {
							// TODO: handle exception
						}
						
						if(ret == 1){
							logger.error("微信公众号充值成功，写入微信公众号充值日志 ....uin:"+uin+",orderid:"+orderId+",type:"+type);
							daService.update("insert into alipay_log (notify_no,create_time,uin,money,wxp_orderid,orderid) values(?,?,?,?,?,?)",
											new Object[] { out_trade_no,System.currentTimeMillis() / 1000,uin, wx_total, wxp_orderid, orderId });
							
							try {//处理推荐逻辑
								Long rcount = daService.getLong(
										"select count(*) from recommend_tb where (nid=? or openid=?) and type=?",
										new Object[] { uin, openid, 0 });
								if(rcount == 0){
									logger.error("微信公众号支付，该用户没有被推荐过:openid:"+openid+",uin:"+uin+",uid:"+uid);
								}else{
									logger.error("微信公众号支付，该用户已经被推荐过，不再写推荐记录,openid:"+openid+",uin:"+uin+",uid:"+uid);
								}
								if(wx_total >= 1){//直付来源和结算订单，支付金额大于1元
									if(rcount == 0 && (type == 0 || type==5 ) && uid != -1){
										logger.error("该用户没有被推荐过，并且支付金额大于等于1元,写推荐记录openid:"+openid+",uin:"+uin);
										Map usrMap =daService.getMap("select recommendquota from user_info_Tb where id =? ", new Object[]{uid});
										Double recommendquota = 5.00;
										if(usrMap!=null){
											recommendquota = StringUtils.formatDouble(Double.parseDouble(usrMap.get("recommendquota")+""));
											logger.error("该收费员的推荐奖额度是："+recommendquota);
										}
										int r = daService.update("insert into recommend_tb(pid,nid,type,state,create_time,openid,money) values(?,?,?,?,?,?,?) ",
												new Object[] { uid, uin, 0, 0, System.currentTimeMillis() / 1000 , openid, recommendquota});
									}
								}else{
									logger.error("该用户不符合推荐规则，不处理推荐openid:"+openid+",uin:"+uin);
								}
								if(bind_flag == 1 && (type == 0 || type==5 || type == 4)){
									logger.error("已绑定，立刻处理推荐成功逻辑openid:"+openid+",uin:"+uin);
									//publicMethods.handleWxRecommendCode(uin, 0L);//2016-09-07
								}else{
									logger.error("未绑定，openid:"+openid+",uin:"+uin);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							int ptype = 9;//微信公众号
							if(money > wx_total){
								ptype=10;//余额+微信公众号
							}
							if(type == 0){//直付停车费 
								Long comId = daService.getLong("select comid from user_info_tb where id=? ", new Object[]{uid});
								String carNumber = publicMethods.getCarNumber(uin);
								ret = publicMethods.epay(comId,money, uin, uid, ticketId, carNumber,ptype,bind_flag,orderId,wxp_orderid);
								logger.error(">>>>车主直接支付给收费员:" + ret);
								int _state = -1;// 默认支付不成功
								if (ret == 5) {
									_state = 2;
									ret = 1;
									// 发支付成功消息给收费员
									logService.insertParkUserMessage(comId,_state,uid,carNumber,orderId,money, "", 0,ntime,ntime+10, 0, null);
									// 发消息给车主
									logService.insertMessage(comId,_state, uin, carNumber,orderId,money,"", 0, ntime,ntime+10, 0);
								}else if(ret == -7){
									logService.insertUserMesg(0, uin, "微信公众号直付停车费"+wx_total+"元，微信公众号支付失败，充值金额已进入你的账户", "支付失败提醒");
								}
							}else if(type==1){//付泊车费
								logger.error(">>>>>weixin pay :type:泊车费，车主："+uin+"，订单ID："+orderId+",停车券："+ticketId);
								String carNumber = publicMethods.getCarNumber(uin);
								ret = publicMethods.payCarStopOrder(orderId, money, ticketId);
								logger.error(">>>>>weixin pay :type:泊车费,ret="+ret);
								if(ret==5){
									Integer payType = 2;
									String payctype = "微信";
									if(money>wx_total){
										payctype = "微信+余额";
										payType=3;
									}
									ret = daService.update("update carstop_order_tb set state=? ,pay_type=?, amount=? where id =? ", new Object[]{8,payType,money,orderId});
									logger.error(">>>>>weixin pay :type:泊车费,更新订单:"+ret+" total:"+money+",wx_total:"+wx_total+",paytype:"+payType);
									// 发支付成功消息给收费员
									logService.insertParkUserMessage(-1L,2,uid,carNumber,-1L,money,payctype, 0,ntime,ntime+10, 0, null);
									logger.error(">>>>>weixin pay :type:泊车费,已发消息给收费员");
								}
							}else if(type == 2){//充值并购买月卡
								logger.error("buy product by wxp>>>uin:"+uin+",starttime:"+starttime+",months:"+months+",prodid:"+prodid);
								if(prodid > 0 && !starttime.equals("") && months > 0){
									Map productMap = daService.getMap("select * from product_package_tb where id=? and state=? and remain_number>? ",
											new Object[] { prodid, 0, 0 });
									if (productMap != null) {
										String cname = (String) daService.getObject("select company_name from com_info_tb where id=?",
														new Object[] { productMap.get("comid") },String.class);
										// 写充值日志
										Double total= commonMethods.getProdSum(prodid, months);
										if (total > wx_total)
											ptype = 10;
										int r = daService.update("insert into money_record_tb(comid,create_time,amount,uin,type,pay_type,remark) values (?,?,?,?,?,?,?)",
														new Object[] {-1L,System.currentTimeMillis() / 1000,Double.valueOf(total),uin, ZLDType.MONEY_RECARGE,ptype,productMap.get("p_name")+ "充值 - " + cname });
										logger.error("buy product by wxp>>>uin:"+uin+",r:"+r);
										r = publicMethods.buyProducts(uin, productMap, months, starttime,"", ptype);
										logger.error("buy product by wxp>>>uin:"+uin+",prodid:"+prodid+",r:"+r);
										// 给用户（车主）发消息
										logService.insertMessage(-1L, 1, uin, "", 0L,Double.valueOf(total), total + "元充值并购买"+ productMap.get("p_name") + "成功",0, 0L, 0L, 2);
									}
								}
							}else if(type == 3){//仅充值
								logger.error("仅充值...");
								daService.update("insert into money_record_tb(comid,create_time,amount,uin,type,pay_type,remark) values (?,?,?,?,?,?,?)",
												new Object[] {-1L,System.currentTimeMillis() / 1000,wx_total, uin,ZLDType.MONEY_RECARGE, 9,"充值" });
								
								Integer isAuth = (Integer)userMap.get("is_auth");
								
								String remark = "点击详情查账户余额！";
								String remark_color = "#000000";
								String url = "http://"+Constants.WXPUBLIC_REDIRECTURL+"/zld/wxpaccount.do?action=balance&openid="+openid;
								Long bid=0L;
								/*if((wx_total==100) && isAuth==1){//充值100返大礼包
									//处理返红包
									logger.error("认证过的用户，并且充值100元uin:"+uin+",wx_total:"+wx_total);
									String sql = "insert into order_ticket_tb (id,uin,order_id,money,bnum,ctime,exptime,bwords,type) values(?,?,?,?,?,?,?,?,?)";
									Long ctime = System.currentTimeMillis()/1000;
									Long exptime = ctime + 24*60*60;
									bid =daService.getkey("seq_order_ticket_tb");
									Object []values = new Object[]{bid,uin,-1,100,25,ctime,exptime,"我在停车宝充值了100元，获得100元停车券礼包，分享给25个小伙伴，手快有，手慢无",4};
									int r  = daService.update(sql,values);
									logger.error("发大礼包，uin:"+uin+",r:"+r+",bid:"+bid);
									if(r!=1)
										bid=0L;
									logger.error("车主"+uin+"微信公众号充值了100元，返红包25/100。。："+r);
									if(bid > 0){
										remark = "恭喜您获得25个共100元停车券礼包，点击分享吧！";
										remark_color = "#FF0000";
										url = "http://"+Constants.WXPUBLIC_REDIRECTURL+"/zld/wxpublic.do?action=balancepayinfo&openid="+openid+"&money="+wx_total+"&bonusid="+bid+"&bonus_type=0&notice_type=3";
									}
								}*/
								try {
									logger.error(">>>>>>>>>>>停车宝微信公众号账户充值，充值成功给用户微信推消息,uin:"+uin+",openid:"+openid);
									
									Map<String, String> baseinfo = new HashMap<String, String>();
									List<Map<String, String>> orderinfo = new ArrayList<Map<String,String>>();
									String first = "恭喜您，充值成功！";
									
									baseinfo.put("url", url);
									baseinfo.put("openid", openid);
									baseinfo.put("top_color", "#000000");
									baseinfo.put("templeteid", Constants.WXPUBLIC_SUCCESS_NOTIFYMSG_ID);
									Map<String, String> keyword1 = new HashMap<String, String>();
									keyword1.put("keyword", "orderMoneySum");
									keyword1.put("value", money+"元");
									keyword1.put("color", "#000000");
									orderinfo.add(keyword1);
									Map<String, String> keyword2 = new HashMap<String, String>();
									keyword2.put("keyword", "orderProductName");
									keyword2.put("value", "停车宝账户充值");
									keyword2.put("color", "#000000");
									orderinfo.add(keyword2);
									Map<String, String> keyword3 = new HashMap<String, String>();
									keyword3.put("keyword", "Remark");
									keyword3.put("value", remark);
									keyword3.put("color", remark_color);
									orderinfo.add(keyword3);
									Map<String, String> keyword4 = new HashMap<String, String>();
									keyword4.put("keyword", "first");
									keyword4.put("value", first);
									keyword4.put("color", "#000000");
									orderinfo.add(keyword4);
									publicMethods.sendWXTempleteMsg(baseinfo, orderinfo);
								} catch (Exception e) {
									// TODO: handle exception
								}
								
								logger.error("充值成功，写入充值日志 ....");
								// 给用户（车主）发消息
								logService.insertMessage(-1L, 1, uin, "", 0L,wx_total, wx_total + "元充值成功", 0, 0L,0L, 2);
							}else if(type == 4){//NFC预支付
								logger.error("预支付...");
								Map<String, Object> orderMap = daService.getMap("select * from order_tb where id=? ", new Object[]{orderId});
								publicMethods.prepay(orderMap, money, uin, ticketId, ptype, bind_flag,wxp_orderid);
								logger.error("预支付成功 ....");
							}else if(type == 5){//充值并结算订单
								logger.error(">>>>>>>>>>>>>>>微信公众号结算订单>>>>>>>>>>>orderid:"+orderId);
								Map orderMap = daService.getMap("select * from order_tb where id=? ",
										new Object[] { orderId });
								if (orderMap != null) {
									String cname = (String) daService.getObject("select company_name from com_info_tb where id=?",
													new Object[] { orderMap.get("comid") },String.class);
									daService.update("insert into money_record_tb(comid,create_time,amount,uin,type,pay_type,remark) values (?,?,?,?,?,?,?)",
													new Object[] {-1L,System.currentTimeMillis() / 1000,wx_total, uin,
															ZLDType.MONEY_RECARGE,9, "停车费充值-" + cname });
									Integer r = publicMethods.payOrder(orderMap, money, uin, 2, ptype, ticketId, wxp_orderid, -1L, uid);
									logger.error("支付订单:" + r);
									int _state = -1;// 默认支付不成功
									String carNumber = publicMethods.getCarNumber(uin);
									if(orderMap.get("car_number") != null){
										carNumber = (String)orderMap.get("car_number");
									}
									if (r == 5) {
										logger.error(">>>>>>>>>>>>>>>微信公众号结算订单成功>>>>>>>>>>>>orderid:"+orderId+"comid:"+orderMap.get("comid") );
										_state = 2;
										Long comId = (Long)orderMap.get("comid");
										if(comId==20130){//宝和公司的测试订单，调用他们的接口发送订单支付状态
											String sr = commonMethods.sendOrderState2Baohe(Long.valueOf(orderId), 1, money,ptype);
											logger.error(">>>>>>>>>>>>>baohe sendresult:"+sr+",orderid:"+orderId+",state:1,total:"+money);
										}
										String msg = null;
										Integer orderState = (Integer)orderMap.get("state");
										String car_number = (String)orderMap.get("car_number");
										if(orderState == 2){
											logger.error("逃单追缴");
											msg = car_number + "电子补缴" + money + "元" + "，补缴时间：" + TimeTools.getTime_yyyyMMdd_HHmmss(ntime * 1000) + "，订单号：" + orderId;
										}
										// 发支付成功消息给收费员
										logService.insertParkUserMessage((Long) orderMap.get("comid"),_state,(Long) orderMap.get("uid"),carNumber,
												Long.valueOf(orderId),money, "", 0,
												(Long) orderMap.get("create_time"),System.currentTimeMillis()/1000, 0, msg);
										// 发消息给车主
									}
								}
							}else if(type == 6){//打赏收费员
								Map orderMap = daService.getMap("select * from order_tb where id=? ",
										new Object[] { orderId });
								String carNumber = publicMethods.getCarNumber(uin);
								if(orderMap.get("car_number") != null){
									String cnum = (String)orderMap.get("car_number");
									if(!carNumber.equals("")){
										carNumber = cnum;
									}
								}
								int r = publicMethods.doparkUserReward(uin, uid, orderId, ticketId, money, ptype,bind_flag);
								if(ret==1){
									Long btime = TimeTools.getToDayBeginTime();
									Long recount = daService.getLong("select count(id) from parkuser_reward_tb where uid =? and ctime >? ",
											new Object[]{uid,btime});
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
									logService.insertParkUserMessage(comid,2,uid,carNumber,uin,money, ""+recount, 0,ntime,ntime+10,5, null);
								}
							}else if(type == 7){
								logger.error("buyticket online>>>uin:"+uin+",wx_pay:"+wx_total+",money:"+money);
								int r = publicMethods.buyTickets(uin,ticketmoney,ticketnum,ptype);
								logger.error("buyticket online>>>uin:"+uin+"r:"+r);
							}else if(type==9){//泊链订单预支付
								logger.error("需要同步到泊链账户中......");
								String carNumber = publicMethods.getCarNumber(uin);
								if(!isBolinkUser)
									publicMethods.syncUserToBolink(uin);
							}
						} else {
							logService.insertMessage(-1L, 0, uin, "", 0L,wx_total, "写入用户余额失败", 0, 0L, 0L, 2);
						}
						logger.error("微信公众号支付成功....发消息给车主");
					}else {
						logger.error("充值错误，客户不存在，手机：" + mobile);
						// resHandler.sendToCFT("Fail");
						logService.insertMessage(-1L, 0, uin, "", 0L,wx_total, "车主信息不存在", 0, 0L, 0L, 2);
						// return;
					}
					
					//响应微信服务器
			        response.getWriter().write(PayCommonUtil.setXML("SUCCESS", ""));//告诉微信服务器，我收到信息了，不要在调用回调action了
					System.out.println("-------------"+PayCommonUtil.setXML("SUCCESS", ""));
					return;
			    }else{
			    	logger.error("微信公众号通知签名验证失败");
					logger.error("微信公众号支付错误....");
			    }
				logger.error("微信公众号支付错误....");
				response.getWriter().write(PayCommonUtil.setXML("FAIL", ""));//告诉微信服务器，我收到信息了，不要在调用回调action了
			}
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void init() throws ServletException {
		ApplicationContext ctx = WebApplicationContextUtils
				.getWebApplicationContext(getServletContext());
		daService= (DataBaseService) ctx.getBean("dataBaseService");
		publicMethods = (PublicMethods)ctx.getBean("publicMethods");
		logService = (LogService) ctx.getBean("logService");
		commonMethods = (CommonMethods) ctx.getBean("commonMethods");
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
	}
}
