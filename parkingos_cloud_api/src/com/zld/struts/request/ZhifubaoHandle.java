package com.zld.struts.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pay.AlipayUtil;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.LogService;
import com.zld.utils.Check;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.utils.ZLDType;

public class ZhifubaoHandle extends HttpServlet{
	private ServletContext servletContext;

	private DataBaseService daService;
	private PublicMethods publicMethods;
	private LogService logService;
	private CommonMethods commonMethods;
	
	private Logger logger = Logger.getLogger(ZhifubaoHandle.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 4942068508811134127L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		servletContext = getServletContext();
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		daService = (DataBaseService)ctx.getBean("dataBaseService");
		publicMethods =(PublicMethods)ctx.getBean("publicMethods");
		logService=(LogService)ctx.getBean("logService");
		commonMethods = (CommonMethods) ctx.getBean("commonMethods");
		
		Map map = req.getParameterMap();
		/**
		 * 支付宝回调说明 ，这里接收两类回调
		 * 
		 * 普通支付：type=1-6
		 * body=18101333937_0&buyer_email=15801482643&buyer_id=2088112611027102&discount=0.00
		 * &gmt_create=2016-11-28 17:02:42&is_total_fee_adjust=Y&notify_id=f5bd4caea603a135b2b53d0ede66885gru
		 * &notify_time=2016-11-28 17:02:42&notify_type=trade_status_sync&out_trade_no=112817023414043
		 * &payment_type=1&price=0.10&quantity=1&seller_email=caiwu@zhenlaidian.com&seller_id=2088411488582814
		 * &subject=账户充值&total_fee=0.10&trade_no=2016112821001004100200777631&trade_status=WAIT_BUYER_PAY
		 * &use_coupon=N
		 * 
		 * 扫码回调：type=7
		 *  {body=-1_7_401723, open_id=20880058437367510024045431418146, subject=车主赣BBBBBB支付停车费, 
		 *  sign_type=RSA, buyer_logon_id=185****2450, auth_app_id=2016080801718601, 
		 *  notify_type=trade_status_sync, out_trade_no=24093932, version=1.0, point_amount=0.00, 
		 *  fund_bill_list=[{"amount":"0.01","fundChannel":"ALIPAYACCOUNT"}], total_amount=0.01, 
		 *  buyer_id=2088912086349460, trade_no=2016113021001004460276581016, 
		 *  notify_time=2016-11-30 13:07:30, charset=utf-8, invoice_amount=0.01, 
		 *  trade_status=TRADE_SUCCESS, gmt_payment=2016-11-30 13:01:18, 
		 *  sign=b+L7vZa+wMUJ+3DsM4qZAxXu2gNpaoR6aNJkCw69MyDm1NPQBAlAWd+Ep+jVgGvr7VppUco3O8N5i+ef/hpWDJysrTpz0tfk91LcG4+hl8KL566PbaBG/LxhB4JL8m8TMWf4QBRGLrWVHJghaT9HO2087LBRO9dOWRr1D1Fe/lI=, 
		 *  gmt_create=2016-11-30 13:00:58, buyer_pay_amount=0.01, receipt_amount=0.01, app_id=2016080801718601, 
		 *  seller_id=2088411488582814, notify_id=38fed5f14dd9ebaf883df1f422ff53ejju, seller_email=caiwu@zhenlaidian.com}
		 *  
		 *  主要区别是购买人：buyer_email -- buyer_logon_id，金额 ：total_fee --- total_amount
		 *  
		 *   扫码预付 uin_8_bolinkorderid
		 */
		Map<String, String> parMap = new HashMap<String, String>();
		String body = req.getParameter("body");//传递信息的参数
		String notify  =req.getParameter("notify_id");
		String userPayAccount = req.getParameter("buyer_id");
		logger.error(">>>>>alipay buyer_id:"+userPayAccount);
		/*
		 * 协议说明：
		 * body：
			用户手机号_type; 当前已用0,1,2,3,4
			type充值类型：
			0：帐号充值，body的组成为两部分，用户手机号_0
			如:15801582643_0
			1：充值并购买停车场包月产品,body的组成为五个部分，
			用户手机号_1_购买的产品编号_购买数量_起始日期
			如:15801482643_1_1022_3_20140815
			2:充值并支付订单,body三部分
			用户手机号_2_订单编号_优惠券编号 
			如：15801482643_2_1011_1123
			3:充值并直接支付给收费员,body五部分 
			用户手机号_3_收费员账号_支付金额_优惠券编号
			如：15801482643_3_10700_15.0_1123
			4:充值并打赏给收费员,body六部分 
			用户手机号_4_收费员账号_打赏金额_订单编号_优惠券编号
			如：15801482643_4_10700_15.0_2590099_1123
			5:充值并打购买停车券,body四部分 
			用户手机号_5_停车券面额_购买数量
			如：15801482643_5_10_2
			6:充值并支付订单,body五部分 
			用户手机号_6_订单号_金额_停车券编号
			如：15801482643_6_333333_3.0_3344
			7:扫码充值并支付订单,body四部分 
			用户账户_7_收费员账户，订单编号在out_trade_no回传参数中
			如：21192_7_10999
		 */
		Long ntime = System.currentTimeMillis()/1000;
		String mobile="";//用户手机号，用来标志用户
		String type = "";//充值类型：0，帐号充值，1：充值并购买停车场包月产品,2:充值并支付订单
		String pid = "";//购买的产品编号，根据上一参数的开始位标识，0不购买，1购买包月产品，2....
		String number = "";//购买数量
		String start = "";//起始日期
		String orderId="-1";//订单编号
		Long ticketId = null;
		Long uid=-1L;//收费员账号
		Double money=0d;//直接支付金额
		Integer bind_flag = 1;//0:未绑定账户，1：已绑定账户
		Integer ticketNumber =0;//购买数量
		Integer ticketPrice =0;//停车券面额
		String tradeId = "";//req.getParameter("");
		logger.error("body:"+body);
		Long uin =null;
		if(body.indexOf("_")!=-1){
			String [] info = body.split("_");
			if(info.length>1){
				mobile = info[0];
				type = info[1];
				if(type!=null&&type.equals("1")&&info.length==5){
					pid = info[2];
					number = info[3];
					start = info[4];
				}else if(type.equals("2")){
					orderId=info[2];
					if(info.length==4&&Check.isLong(info[3]))
						ticketId = Long.valueOf(info[3]);
				}else if(type.equals("3")){
					try {
						orderId = daService.getkey("seq_order_tb") + "";//直付没有订单，预取一个
						uid = Long.valueOf(info[2]);
						money = StringUtils.formatDouble(info[3]);
						ticketId = Long.valueOf(info[4]);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}else if(type.equals("4")){
					try {//15801482643_4_10700_15.0_2590099_1123
						uid = Long.valueOf(info[2]);
						money = StringUtils.formatDouble(info[3]);
						orderId = info[4];
						ticketId = Long.valueOf(info[5]);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}else if(type.equals("5")){
					try {//15801482643_5_10_2 用户手机号_5_停车券面额_购买数量
						ticketPrice = Integer.valueOf(info[2]);
						ticketNumber = Integer.valueOf(info[3]);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}else if(type.equals("6")){
					try {//15801482643_6_333333_3.0_99999 //15801482643_type_订单号_金额_停车券编号
						orderId = info[2];
						money = StringUtils.formatDouble(info[3]);
						ticketId = Long.valueOf(info[4]);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}else if(type.equals("7")){
					orderId = req.getParameter("out_trade_no");
					uin = Long.valueOf(mobile);
					uid = Long.valueOf(info[2]);
				}else if(type.equals("8")){
					tradeId = req.getParameter("out_trade_no");
					uin = Long.valueOf(mobile);
					orderId =info[2];
				}
			}
		}
		logger.error("参数 ：,mobile="+mobile+",type="+type+",pid="+pid+",number="+number+",start="+start+",orderid="+orderId);
		
		if(!type.equals("7")){//只有扫码支付时，可能是不是注册车主
			Map userMap = daService.getMap("select id from user_info_tb where mobile=? and auth_flag=? ", new Object[]{mobile,4});
			if(userMap!=null&&!userMap.isEmpty()){
				uin =(Long)userMap.get("id");
			}
		}
		logger.error("客户编号 ,uin="+uin);
		for(Object key :map.keySet()){
			parMap.put(key.toString(), req.getParameter(key.toString()));
		}
		String sign = req.getParameter("sign");
		//String buyer_email=req.getParameter("buyer_email");
		//String buyer_id=req.getParameter("buyer_id");
		
		boolean isalipay = false;
		logger.error("zhifubao callback:"+parMap);
		if(type.equals("7")||type.equals("8"))
			isalipay=AlipayUtil.getQrSignVeryfy(parMap,sign);
		else {
			isalipay=AlipayUtil.getSignVeryfy(parMap,sign);
		}
		logger.error("zhifubao callback,veryfy:"+isalipay);
		//System.out.println("user mobile:"+mobile);
		if(isalipay){//认证成功
			logger.error("ali认证成功！");
			String state  = req.getParameter("trade_status");
			String total = req.getParameter("total_fee");
			if(total==null)
				total = req.getParameter("total_amount");
			Integer payType = 1;//- 0余额，1支付宝，2微信，3网银，4余额+支付宝,5余额+微信,6余额+网银
			if(state.equals("TRADE_FINISHED")|| state.equals("TRADE_SUCCESS")){
				logger.error("状态　："+state+",ali付款完成 ！");
				
				//验证是否已充值:
				Long count = daService.getLong("select count(*) from alipay_log where notify_no=? and create_time>? ", 
						new Object[]{notify,(System.currentTimeMillis()/1000-(12*60*60))});
				if(count>0){//已充值过，不再处理
					logger.error("已处理过充值,返回 ！");
					AjaxUtil.ajaxOutput(resp, "success");
					return ;
				}
				logger.error("未处理过充值....继续....notify_id="+notify);
				boolean isbind=true;//是否为注册车主，false为临时账户
				if(type.equals("7")&&uin==-1){//是扫码支付,客户不存在 
					if(Check.isLong(orderId)){//订单号检查
						Map orderMap = daService.getMap("select car_number from order_tb where id =? ",new Object[]{ Long.valueOf(orderId)});
						if(orderMap!=null&&!orderMap.isEmpty()){
							String carNumber=(String)orderMap.get("car_number");
							if(Check.checkPhone(userPayAccount,"m")){//购买者是用手机号注册的，我们注册一下用户，并绑定好车牌
								logger.error("支付宝扫码支付，用户未注册过，用户支付宝使用的手机注册，账户："+userPayAccount);
								if(userPayAccount.startsWith("0"))//过滤掉前面的0
									userPayAccount = userPayAccount.substring(1);
								//检查用户是否注册过
								if(userPayAccount.length()==11){//手机号11位
									Map userMap = daService.getMap("select id from user_info_tb where mobile=? and auth_flag=? ", new Object[]{userPayAccount,4});
									if(userMap!=null&&!userMap.isEmpty()){//用户已存在 ，使用当前的账号
										uin =(Long)userMap.get("id");
										logger.error("支付宝扫码支付，用户已注册过，账户："+uin+",但可能车牌不是本车主的车牌，支付记录此车主下");
									}else{//不存在注册一个新的账户
										uin = daService.getkey("seq_user_info_tb");
										String sql= "insert into user_info_tb (id,nickname,password,strid," +
												"reg_time,mobile,auth_flag,comid,media,recom_code) " +
												"values (?,?,?,?,?,?,?,?,?,?)";
										Object[] values= new Object[]{uin,"车主","zlduser"+uin,"zlduser"+uin,ntime,userPayAccount,4,0,0,uid};
										int r = daService.update(sql,values);
										logger.error("支付宝扫码支付，用户未注册过，用支付宝注册手机注册了一个用户，手机："+userPayAccount+",uin="+uin+",r="+r);
										if(r==1){//车主注册成功，绑定车牌和写用户配置
											//查询车牌是否已绑定
											Map carMap = daService.getMap("select uin from car_info_tb where car_number=? ",  new Object[]{carNumber});
											if(carMap==null||carMap.isEmpty()){
												r = daService.update("insert into car_info_tb(uin,car_number,create_time,remark) values(?,?,?,?)",
														new Object[]{uin,carNumber,ntime,"支付宝扫码支付时注册"});
												logger.error("支付宝扫码支付，用户未注册过，绑定车牌："+carNumber+",uin="+uin+",r="+r);
											}else {
												logger.error("支付宝扫码支付，车牌已注册过："+carNumber+",不再绑定 ，原绑定车主为：uin="+carMap.get("uin"));
											}
											r= daService.update("insert into user_profile_tb (uin,low_recharge,limit_money,auto_cash," +
													"create_time,update_time) values(?,?,?,?,?,?)", 
													new Object[]{uin,10,25,1,ntime,ntime});
											logger.error("支付宝扫码支付，用户未注册过，用支付宝注册手机注册了一个用户,写用户通用配置：r="+r);
										}
									}
								}
							}else {//购买者不是用手机号注册的，把车牌写入虚拟账户，用户app注册时，更新数据到用户账户下
								logger.error("支付宝扫码支付，用户未注册过，用户支付宝没有使用的手机注册，账户："+userPayAccount+",只能根据车牌写一个临时账户");
								isbind=false;//临时账户
								//检查是否已有临时账户
								Map wxpUserMap= daService.getMap("select uin,car_number from wxp_user_tb where car_number=? ", new Object[]{carNumber});
								if(wxpUserMap!=null&&wxpUserMap.get("uin")!=null){
									uin = (Long)wxpUserMap.get("uin");
									logger.error("支付宝扫码支付,已有临时账户："+wxpUserMap);
								}
								if(uin==-1){
									uin = daService.getkey("seq_user_info_tb");
									int r = daService.update("insert into wxp_user_tb(openid,create_time,uin,car_number) values(?,?,?,?) ",
											new Object[] { "zhifubao", System.currentTimeMillis() / 1000, uin,orderMap.get("car_number")});
									logger.error("没有临时账户，创建一个uin:"+uin+",car_number:"+orderMap.get("car_number")+",r:"+r);
								}
							}
						}else {
							logger.error("支付宝扫码支付错误：订单编号错误，查不到订单+"+orderId);
						}
					}else {
						logger.error("支付宝扫码支付错误：订单编号错误，格式错误，不是数字："+orderId);
					}
				}
				if(uin!=null&&uin>0){//客户存在　
					int result =0;
					if(type.equals("8")||!isbind){//支付宝预付或不是注册用户;充值到临时账户
						result=result=daService.update("update wxp_user_tb set balance =balance+? where uin=?  ", 
								new Object[]{Double.valueOf(total),uin});
					}else {
						result=daService.update("update user_info_tb set balance =balance+? where id=?  ", 
								new Object[]{Double.valueOf(total),uin});
					}
					try {
						List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
						Map<String, Object> userSqlMap = new HashMap<String, Object>();
						//Map<String, Object> tcbFeeSqlMap = new HashMap<String, Object>();
						Map<String, Object> tcbSqlMap = new HashMap<String, Object>();
						userSqlMap.put("sql", "insert into user_account_tb(uin,amount,type,create_time,remark,pay_type,orderid) values(?,?,?,?,?,?,?)");
						userSqlMap.put("values", new Object[]{uin,Double.valueOf(total),0,System.currentTimeMillis()/1000-2,"支付宝充值",1,Long.valueOf(orderId)});
						bathSql.add(userSqlMap);
						tcbSqlMap.put("sql", "insert into tingchebao_account_tb(amount,type,create_time,remark,utype,orderid,online_orderid,uin) values(?,?,?,?,?,?,?,?)");
						tcbSqlMap.put("values", new Object[]{Double.valueOf(total),0,System.currentTimeMillis() / 1000 - 2,"支付宝充值", 7, Long.valueOf(orderId), notify, uin });
						bathSql.add(tcbSqlMap);
//						tcbFeeSqlMap.put("sql", "insert into tingchebao_account_tb(amount,type,create_time,remark,utype,orderid,online_orderid,uin) values(?,?,?,?,?,?,?,?)");
//						tcbFeeSqlMap.put("values", new Object[]{Double.valueOf(total)*0.025,1,System.currentTimeMillis() / 1000 - 2,"支付宝充值手续费", 1, Long.valueOf(orderId), notify, uin });
//						bathSql.add(tcbFeeSqlMap);
						
						boolean b = daService.bathUpdate(bathSql);
						logger.error("orderid:"+orderId+",uin:"+uin+",b:"+b);
						//写用户支付账号信息表
						logService.insertUserAccountMesg(0, uin, userPayAccount);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					
					logger.error("充值,写入客户帐户:money="+total+",uin="+uin+",result="+result);
					if(result==1){
						//写阿里充值日志 
						logger.error("充值成功，写入阿里充值日志 ....");
						daService.update("insert into alipay_log (notify_no,create_time,uin,money,orderid,wxp_orderid) values(?,?,?,?,?,?)",
								new Object[]{notify,System.currentTimeMillis()/1000,uin,Double.valueOf(total),Long.valueOf(orderId),req.getParameter("trade_no")});
						logger.error("充值结果："+result+"，手机号："+mobile+" ，金额 ："+total);
						//判断下一步操作，0不操作，1购买包月产品 2支付订单
						if(type.equals("0")){//仅充值
							logger.error("仅充值...");
							daService.update( "insert into money_record_tb(comid,create_time,amount,uin,type,pay_type,remark) values (?,?,?,?,?,?,?)", 
									new Object[]{-1L,System.currentTimeMillis()/1000,Double.valueOf(total),uin,ZLDType.MONEY_RECARGE,payType,"充值"});
							logger.error("充值成功，写入充值日志 ....");
							logService.insertMessage(-1L, 1, uin,"", 0L,Double.valueOf(total),"", 0,0L,0L,2);
							//仅充值时，认证过车主应该先补全信用额度30元，补全后多出的金额才可以充到余额中。
							Map userMap = daService.getMap("select is_auth,credit_limit from user_info_Tb where id=? ", new Object[]{uin});
							Integer isAuth = 0;
							if(userMap!=null){
								isAuth=(Integer)userMap.get("is_auth");
								if(isAuth!=null&&isAuth==1){
									Double limit = StringUtils.formatDouble(userMap.get("credit_limit"));
									if(limit!=null){
										if(limit<30){
											int r = daService.update("update user_info_tb set balance = balance-? ,credit_limit=? where id =? ", new Object[]{StringUtils.formatDouble(30-limit),30.0,uin});
											logger.error("认证过车主应该先补全信用额度30元,补全金额："+(30-limit)+",ret:"+r);
										}
									}
								}
							}
							Double tot= Double.valueOf(total)*100;
							//publicMethods.sendMessageToThird(uin,tot.intValue(), null, null, null, 1);
							logger.error("充值成功，给车主发消息....");
							Long bid=0L;
							/*if(Double.valueOf(total)==100&&isAuth==1){//充值100返大礼包
								//处理返红包
								String sql = "insert into order_ticket_tb (id,uin,order_id,money,bnum,ctime,exptime,bwords,type) values(?,?,?,?,?,?,?,?,?)";
								Long ctime = System.currentTimeMillis()/1000;
								Long exptime = ctime + 24*60*60;
								bid =daService.getkey("seq_order_ticket_tb");
								Object []values = new Object[]{bid,uin,-1,100,25,ctime,exptime,"我在停车宝充值了100元，获得100元停车券礼包，分享给25个小伙伴，手快有，手慢无",4};
								int ret  = daService.update(sql,values);
								if(ret!=1)
									bid=0L;
								logger.error("车主"+uin+"停车宝充值了100元，返红包25/100。。："+ret);
								logService.insertUserMesg(1, uin, "恭喜您获得充值大礼包", "红包提醒");
							}*/
							// 给用户（车主）发消息
							publicMethods.syncUserToBolink(uin);
							logService.insertMessage(-1L, 1, uin, "", bid,Double.valueOf(total), total + "元充值成功", 0, 0L,0L, 2);
							//publicMethods.sendMessageToThird(uin, Integer.valueOf(total), null, null, null, 1);
						}else if(type.equals("1")){//充值并购买包月产品
							logger.error("充值并购买包月产品...");
							Map productMap = daService.getMap("select * from product_package_tb where id=? and state=? and remain_number>? ", 
									new Object[]{Long.valueOf(pid),0,0});
							if(productMap!=null){
								String cname = (String)daService.getObject("select company_name from com_info_tb where id=?",new Object[]{productMap.get("comid")}, String.class);
								//写充值日志
								Double price = Double.valueOf(productMap.get("price")+"");
								price = price*Integer.valueOf(number);
								if(price>Double.valueOf(total))
									payType=4;
								daService.update( "insert into money_record_tb(comid,create_time,amount,uin,type,pay_type,remark) values (?,?,?,?,?,?,?)", 
										new Object[]{-1L,System.currentTimeMillis()/1000,Double.valueOf(total),uin,ZLDType.MONEY_RECARGE,payType,productMap.get("p_name")+"充值 - "+cname});
								logger.error("充值成功，写入充值日志 ....");
								result=publicMethods.buyProducts(uin,productMap, Integer.valueOf(number), start,"",payType);
								logger.error(productMap.get("p_name")+"充值 - "+cname+":"+total+",result:"+result);
								//给车主发消息
								logService.insertMessage(-1L, 1, uin,"", 0L,Double.valueOf(total),"", 0,0L,0L,2);
							}
						}else if(type.equals("2")){//充值并支付订单...
							logger.error("充值并支付订单...");
							//写充值日志
							Map orderMap = daService.getMap("select * from order_tb where id=? ", new Object[]{Long.valueOf(orderId)});
							logger.error("total:"+total+"ordermap:"+orderMap);
							String cname = (String)daService.getObject("select company_name from com_info_tb where id=?",new Object[]{orderMap.get("comid")}, String.class);
							if(orderMap!=null){
								Long comId = (Long)orderMap.get("comid");
								Double ordermoney = 0d;
								if(orderMap.get("total") != null){
									ordermoney = StringUtils.formatDouble(orderMap.get("total"));
								}
								if(ordermoney==0.0)
									ordermoney =  StringUtils.formatDouble(total);
								//Double price = Double.valueOf(orderMap.get("total")+"");
								if(Double.valueOf(total)<ordermoney){
									payType=4;
								}
								daService.update( "insert into money_record_tb(comid,create_time,amount,uin,type,pay_type,remark) values (?,?,?,?,?,?,?)", 
										new Object[]{-1L,System.currentTimeMillis()/1000,Double.valueOf(total),uin,ZLDType.MONEY_RECARGE,payType,"停车费充值-"+cname});
								logger.error("充值成功，写入充值日志 ....");
								
								result=publicMethods.payOrder(orderMap,ordermoney,uin,2,payType,ticketId,null,-1L,uid);
								logger.error("支付订单:"+result+",orderid:"+orderId+",uin:"+uin+",comid:"+orderMap.get("comid"));
								int _state =-1;//默认支付不成功
								String carNumber = (String)orderMap.get("car_number");
								if(carNumber==null||"".equals(carNumber)||"车牌号未知".equals(carNumber))
										carNumber = publicMethods.getCarNumber(uin);
								Long endTime =(Long)orderMap.get("end_time"); 
								if(endTime==null)
									endTime=System.currentTimeMillis()/1000;
								if(result==5){
									_state  = 2;
									result = 1;
									//发支付成功消息给收费员
									logService.insertParkUserMessage((Long)orderMap.get("comid"), _state, (Long)orderMap.get("uid"),carNumber, Long.valueOf(orderId),ordermoney,"", 0, (Long)orderMap.get("create_time"), endTime,0, null);
									//发消息给车主
								}else if(result==-7){
									logService.insertUserMesg(0, uin, "由于网络原因，"+cname+"，停车费"+total+"元，支付宝支付失败", "支付失败提醒");
								}
								if(comId==20130){//宝和公司的测试订单，调用他们的接口发送订单支付状态
									String sr = commonMethods.sendOrderState2Baohe(Long.valueOf(orderId), result, ordermoney, payType);
									logger.error(">>>>>>>>>>>>>baohe sendresult:"+sr+",orderid:"+orderId+",state:"+result+",total:"+ordermoney+",paytype:"+payType);
								}
								logService.insertMessage((Long)orderMap.get("comid"), _state, uin,carNumber, Long.valueOf(orderId),ordermoney,"", 0,  (Long)orderMap.get("create_time"), endTime,0);
							}
						}else if(type.equals("3")) {//充值并直接支付给收费员...
							Long comId = daService.getLong("select comid from user_info_tb where id=? ", new Object[]{uid});
							String carNumber = publicMethods.getCarNumber(uin);
							result = publicMethods.epay(comId,money, uin, uid, ticketId, carNumber,4, bind_flag,Long.valueOf(orderId),null);
							logger.error(">>>>车主直接支付给收费员:" + result);
							int _state = -1;// 默认支付不成功
							if (result == 5) {
								_state = 2;
								result = 1;
								// 发支付成功消息给收费员
								logService.insertParkUserMessage(comId,_state,uid,carNumber,Long.valueOf(orderId),money, "", 0,ntime,ntime+10, 0, null);
								// 发消息给车主
							}else if(result==-7){
								logService.insertUserMesg(0, uin, "直付停车费"+total+"元，支付宝支付失败，充值金额已进入你的账户", "支付失败提醒");
							}
							//logService.doMessage(comId, _state, uin, carNumber,-1L,money, "支付成功",0, ntime,ntime+10, 2);
							logService.insertMessage(comId,_state, uin, carNumber,Long.valueOf(orderId),money,"", 0, ntime,ntime+10, 0);
						}else if(type.equals("4")){//充值并打赏
							int ptype = 1;//支付宝
							if(money>Double.valueOf(total)){
								ptype=4;//余额+支付宝
							}
							String carNumber = publicMethods.getCarNumber(uin);
							int ret = publicMethods.doparkUserReward(uin, uid, Long.valueOf(orderId), ticketId, money, ptype,1);
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
								logService.insertMessage(-1L, 1, uin,carNumber,  0L,StringUtils.formatDouble(total),"", 0,ntime,ntime+10,2);
							}else if(ret==-2){
								logService.insertMessage(-1L, 0, uin,carNumber,  0L,StringUtils.formatDouble(total),"已打赏过", 0,ntime,ntime+10,2);
							}else
								logService.insertMessage(-1L, 0, uin,carNumber,  0L,StringUtils.formatDouble(total),"", 0,ntime,ntime+10,2);
						}else if(type.equals("5")){//充值并购买停车券
							boolean isAuth = publicMethods.isAuthUser(uin);
							Integer discount = 9;
							if(isAuth)
								discount=7;
							Double ttotal =  StringUtils.formatDouble(ticketNumber*ticketPrice*discount*0.1);
							int pttype=1;
							if(ttotal>StringUtils.formatDouble(total))
								pttype=4;
							int ret = publicMethods.buyTickets(uin, ticketPrice, ticketNumber, pttype);
							logger.error(mobile+"微信购买停车券：val:"+ticketPrice+",num:"+ticketNumber+",ret:" + ret);
							// 给用户（车主）发消息
							logService.insertMessage(-1L, 1, uin, "", 0L,Double.valueOf(total), total + "元充值并购买停车券成功",0, 0L, 0L, 2);
						}else if(type.equals("6")){//预付停车费
							logger.error("zhifubao prepay>>>orderid:"+orderId+",uin:"+uin+",wx_total:"+total+",money:"+money+",tickid:"+ticketId);
							Map orderMap = daService.getMap("select * from order_tb where id=? ",
									new Object[] { Long.valueOf(orderId) });
							Long comId = (Long)orderMap.get("comid");
							String carNumber = (String)orderMap.get("car_number");
							if(carNumber==null||"".equals(carNumber)||"车牌号未知".equals(carNumber))
									carNumber = publicMethods.getCarNumber(uin);
							int ret = publicMethods.prepay(orderMap, money, uin, ticketId, payType, bind_flag, null);
							logger.error("zhifubao prepay>>>orderid:"+orderId+",uin:"+uin+",ret:"+ret+",comId:"+comId);
							int _state = -1;// 默认支付不成功
							if (ret == 1) {
								_state = 2;
							}else if(ret == -7){
								logService.insertUserMesg(0, uin, "由于网络原因，预付停车费"+total+"元，微信支付失败", "支付失败提醒");
							}
							if(comId==20130){//宝和公司的测试订单，调用他们的接口发送订单支付状态
								String sr = commonMethods.sendPrepay2Baohe(Long.valueOf(orderId), ret,money, payType);
								logger.error(">>>>>>>>>>>>>baohe sendresult:"+sr+",orderid:"+orderId+",state:"+ret+",money:"+money+",paytype:"+payType);
							}
							logService.insertMessage((Long) orderMap.get("comid"),_state, uin, carNumber,Long.valueOf(orderId),
									money,"", 0, (Long) orderMap.get("create_time"), System.currentTimeMillis()/1000, 0);
						}else if(type.equals("7")){//支付宝扫码支付
							logger.error("支付宝扫码支付订单...");
							//写充值日志
							Map orderMap = daService.getMap("select * from order_tb where id=? ", new Object[]{Long.valueOf(orderId)});
							logger.error("total:"+total+"ordermap:"+orderMap);
							String cname = (String)daService.getObject("select company_name from com_info_tb where id=?",new Object[]{orderMap.get("comid")}, String.class);
							if(orderMap!=null){
								//Double ordermoney = 0d;
								daService.update( "insert into money_record_tb(comid,create_time,amount,uin,type,pay_type,remark) values (?,?,?,?,?,?,?)", 
										new Object[]{-1L,System.currentTimeMillis()/1000,Double.valueOf(total),uin,ZLDType.MONEY_RECARGE,payType,"停车费充值-"+cname});
								logger.error("支付宝扫充值成功，写入充值日志 ....");
								
								result=publicMethods.payOrder(orderMap,Double.valueOf(total),uin,2,payType,ticketId,null,-1L,uid);
								logger.error("支付订单:"+result+",orderid:"+orderId+",uin:"+uin+",comid:"+orderMap.get("comid"));
								int _state =-1;//默认支付不成功
								String carNumber = (String)orderMap.get("car_number");
								Long endTime =(Long)orderMap.get("end_time"); 
								if(endTime==null)
									endTime=System.currentTimeMillis()/1000;
								if(result==5){
									_state  = 2;
									result = 1;
								}
								//发支付成功消息给收费员
								logService.insertParkUserMessage((Long)orderMap.get("comid"), _state, uid,carNumber, Long.valueOf(orderId),Double.valueOf(total),"", 0, (Long)orderMap.get("create_time"), endTime,0, null);
								//更新车主账户到订单
								daService.update("update order_tb set uin=? where id = ? ", new Object[]{uin,Long.valueOf(orderId)});
							}
						}else if(type.equals("8")){
							logger.error("prepay bolink order ,update prepay :"+daService.update("update bolink_order_tb set prepay=?,prepay_time=? where id =? ", new Object[]{money,ntime,Long.valueOf(orderId)}));
							publicMethods.prepayToBolink(uin,money,Long.valueOf(orderId));
						}
					}else {
						logService.insertMessage(-1L, 0, uin,"", 0L,Double.valueOf(total),"服务器写入失败", 0,0L,0L,2);
					}
				}else{
					logService.insertMessage(-1L, 0, uin,"", 0L,Double.valueOf(total),"充值错误，客户不存在，手机："+mobile, 0,0L,0L,2);
					logger.error("充值错误，客户不存在，手机："+mobile);
				}
			}else {
				logger.error("状态WAIT_PAY，不操作返回... trade_status:"+state);
			}
			AjaxUtil.ajaxOutput(resp, "success");
			logger.error("操作成功，返回给阿里success...");
		}else {//认证失败
			logService.insertMessage(-1L, 0, uin,"", 0L,0d,"支付宝返回验证失败", 0,0L,0L,2);
			AjaxUtil.ajaxOutput(resp, "fail");
			logger.error("操作失败，返回给阿里fail...");
		}
	}

}
