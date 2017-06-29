package com.zld.struts.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pay.wxnew.Constants;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.LogService;
import com.zld.utils.Check;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.utils.ZLDType;
import com.zld.utils.ZldXMLUtils;
import com.zld.weixinpay.utils.util.MD5Util;

import pay.wxnew.RequestHandler;;

public class WeixinNewHandle extends HttpServlet {
	private ServletContext servletContext;

	private DataBaseService daService;
	private PublicMethods publicMethods;
	private LogService logService;
	private CommonMethods commonMethods;

	private Logger logger = Logger.getLogger(WeixinNewHandle.class);
	/**
	 * AppID：wx73454d7f61f862a5 AppSecret：b3e563822a872e5a37eb692a856ed4ba
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

		logger.error("weixin callback begin....");
		/**
		 * 如果微信收到的应答不是success,认为通知失败，会通过一定的策略（如30分钟共8次）定期重新发起通知，尽可能提高通知的成功率
		 * 但不保证通知最终能成功
		 * 由于存在重新发送后台通知的情况，因此同样的通知对应业务数据的状态，判断该通知是否已经处理过。如果没有处理过再进行处理
		 * ，如果处理过直接返回success. 在对业务数据进行状态检查和处理之前，要采用数据锁进行并发控制，以避免函数重入造成的数据混乱。
		 * 目前补单机制的间隔时间为8s,10s,10s,30s,60s,120s,360s,1000s
		 */
		// 商户号
		//String partner = Constants.WXPAY_PARTNERID;// "1900000109";
		String params=null;
		boolean isContnuie=false;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader((ServletInputStream)req.getInputStream()));  
			String line = null;  
			StringBuilder sb = new StringBuilder();  
			while((line = br.readLine())!=null){  
               sb.append(line);  
	        }  
			params = sb.toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		String return_msg = "OK";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		logger.error(params);
		if(params!=null&&!"".equals(params)){
			paramMap = ZldXMLUtils.parserStrXml(params);
		}
		/**
		 * <xml><appid><![CDATA[wx485c58b62cbb4dd0]]></appid>
		 * <attach><![CDATA[18101333937_0_yangzhou]]></attach>
		 * <bank_type><![CDATA[CFT]]></bank_type>
		 * <cash_fee><![CDATA[1]]></cash_fee>
		 * <fee_type><![CDATA[CNY]]></fee_type>
		 * <is_subscribe><![CDATA[N]]></is_subscribe>
		 * <mch_id><![CDATA[1332937401]]></mch_id>
		 * <nonce_str><![CDATA[a724b9124acc7b5058ed75a31a9c2919]]></nonce_str>
		 * <openid><![CDATA[oETT-w27Rb51THrC40CA7dQ1I4H8]]></openid>
		 * <out_trade_no><![CDATA[22bcf8e0aeae1b6e4cdb0677cf700f1d]]></out_trade_no>
		 * <result_code><![CDATA[SUCCESS]]></result_code>
		 * <return_code><![CDATA[SUCCESS]]></return_code>
		 * <sign><![CDATA[0484D7B17D044E07F6CEEE481B7A564E]]></sign>
		 * <time_end><![CDATA[20160430010640]]></time_end>
		 * <total_fee>1</total_fee>
		 * <trade_type><![CDATA[APP]]></trade_type>
		 * <transaction_id><![CDATA[4006662001201604305357218668]]></transaction_id></xml>
		 * 
		 * {is_subscribe=N, appid=wx485c58b62cbb4dd0, 
		 * fee_type=CNY, nonce_str=a724b9124acc7b5058ed75a31a9c2919, 
		 * out_trade_no=22bcf8e0aeae1b6e4cdb0677cf700f1d,
		 *  transaction_id=4006662001201604305357218668, 
		 *  trade_type=APP, result_code=SUCCESS, 
		 *  sign=0484D7B17D044E07F6CEEE481B7A564E,
		 *  mch_id=1332937401, total_fee=1, 
		 *  attach=18101333937_0_yangzhou, 
		 *  time_end=20160430010640, 
		 *  openid=oETT-w27Rb51THrC40CA7dQ1I4H8, bank_type=CFT,
		 *  return_code=SUCCESS, cash_fee=1}
		 */
		String returnCode = (String)paramMap.get("return_code");
		String resultCode = (String)paramMap.get("result_code");
		//logger.error("return_code:"+returnCode+",result_code:"+resultCode+",paramMap:"+paramMap);
		if(resultCode!=null&&returnCode!=null&&resultCode.equals("SUCCESS")&&returnCode.equals("SUCCESS")){
			isContnuie=true;
		}else {
			logger.error("没有可用信息...request:"+params);
			return_msg="没有可用信息:"+paramMap.get("return_msg");
			AjaxUtil.ajaxOutput(resp, "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA["+return_msg+"]]></return_msg></xml>");//resHandler.sendToCFT("Success");
			return;
		}
		// 判断签名
		String sign =(String) paramMap.get("sign");
		paramMap.remove("sign");
		String vsign  = createSign(paramMap);
		String userPayAccount =(String) paramMap.get("openid");
		
		logger.error("weixin callback osign:"+sign+",nsign:"+vsign);
		if (sign.equals(vsign)) {
			// 通知id
			//String notify_id = resHandler.getParameter("notify_id");

			// 验证是否已充值:
			servletContext = getServletContext();
			ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
			daService = (DataBaseService) ctx.getBean("dataBaseService");
			publicMethods = (PublicMethods) ctx.getBean("publicMethods");
			logService = (LogService) ctx.getBean("logService");
			commonMethods = (CommonMethods) ctx.getBean("commonMethods");

			String total_fee = (String)paramMap.get("total_fee");
			Double total = Double.valueOf(total_fee) * 0.01;
			String attach = (String)paramMap.get("attach");
			/*
			 * 协议说明： attach： 用户手机号_type; type充值类型： 0：帐号充值，body的组成为两部分，用户手机号_0
			 * 如:15801582643_0 1：充值并购买停车场包月产品,body的组成为五个部分，
			 * 用户手机号_1_购买的产品编号_购买数量_起始日期 如:15801482643_1_1022_3_20140815
			 * 2:充值并支付订单,body三部分 用户手机号_2_订单编号_优惠券编号 如：15801482643_2_1011_1123
			 * 3:充值并直接支付给收费员,body五部分 用户手机号_3_收费员账号_支付金额_优惠券编号 如：15801482643_3_10700_15.0_1123
			 * 4:充值并打赏给收费员,body六部分 
				用户手机号_4_收费员账号_打赏金额_订单编号_优惠券编号
				如：15801482643_4_10700_15.0_2590099_1123
				5:充值并打购买停车券,body四部分 
				用户手机号_5_停车券面额_购买数量
				如：15801482643_5_10_2
			 */
			// System.err.println(attach);
			logger.error("attach:" + attach);
			Integer payType = 2;// - 0余额，1支付宝，2微信，3网银，4余额+支付宝,5余额+微信,6余额+网银
			// 处理数据库逻辑
			if (attach == null || "".equals(attach)) {
				logger.error("attach空，没有可用信息");
				return_msg="attach空，没有可用信息";
				AjaxUtil.ajaxOutput(resp, "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");//resHandler.sendToCFT("Success");
				return;
			}
			Long ntime = System.currentTimeMillis()/1000;
			String mobile = "";// 用户手机号，用来标志用户
			String type = "";// 充值类型：0，帐号充值，1：充值并购买停车场包月产品,2:充值并支付订单
			String pid = "";// 购买的产品编号，根据上一参数的开始位标识，0不购买，1购买包月产品，2....
			String number = "";// 购买数量
			String start = "";// 起始日期
			String orderId = "-1";// 订单编号
			Long uid=-1L;//收费员账号
			Double money=0d;//直接支付金额
			Integer ticketNumber =0;//购买数量
			Integer ticketPrice =0;//停车券面额
			Long ticketId = null;
			Integer bind_flag = 1;//0:未绑定账户，1：已绑定账户
			if (attach.indexOf("_") != -1) {
				String[] info = attach.split("_");
				if (info.length > 1) {
					mobile = info[0];
					type = info[1];
					if (type != null && type.equals("1") && info.length == 5) {
						pid = info[2];
						number = info[3];
						start = info[4];
					} else if (type.equals("2")) {
						orderId = info[2];
						if (info.length == 4 && Check.isLong(info[3]))
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
						try {//15801482643_5_10_2 用户手机号_5_停车券面额_购买数量
							orderId = info[2];
							money = StringUtils.formatDouble(info[3]);
							ticketId = Long.valueOf(info[4]);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
				}
			}
			logger.error(">>>>>>body=["+attach+"],参数 ：,mobile=" + mobile + ",type=" + type + ",pid="
					+ pid + ",number=" + number + ",start=" + start+ ",orderid=" + orderId+",uid="+uid+",total="+money+",ticketid="+ticketId);
			Long uin = (Long) daService.getObject("select id from user_info_Tb where mobile=? and auth_flag=? ",
							new Object[] { mobile, 4 }, Long.class);
			logger.error("客户编号 ,uin=" + uin);
			// 判断签名及结果
			// if (queryRes.isTenpaySign() && "0".equals(retcode)
			// && "0".equals(trade_state) && "1".equals(trade_mode)) {
			// logger.error("微信验证通过");
			// 取结果参数做业务处理
			/*
			 * System.out.println("out_trade_no:"+
			 * queryRes.getParameter("out_trade_no") + " transaction_id:"+
			 * queryRes.getParameter("transaction_id"));
			 * System.out.println("trade_state:"+
			 * queryRes.getParameter("trade_state") + " total_fee:"+
			 * queryRes.getParameter("total_fee")); //
			 * 如果有使用折扣券，discount有值，total_fee+discount=原请求的total_fee
			 * System.out.println("discount:"+ queryRes.getParameter("discount")
			 * + " time_end:" + queryRes.getParameter("time_end"));
			 */// ------------------------------
				// 处理业务开始
				// ------------------------------
				// 防重发。每次回调时的out_trade_no相同
			String out_trade_no = (String)paramMap.get("out_trade_no");//resHandler.getParameter("out_trade_no");
			logger.error("out_trade_no:" + out_trade_no);
			Long count = daService.getLong("select count(*) from alipay_log where notify_no=? and create_time>?",
					new Object[] { out_trade_no ,(System.currentTimeMillis()/1000-(12*60*60))});
			if (count > 0) {// 已充值过，不再处理
				logger.error("已处理过充值,返回 ！");
				AjaxUtil.ajaxOutput(resp, "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
				return;
			}
			logger.error("未处理过充值....继续....out_trade_no=" + out_trade_no);
			// 注意交易单不要重复处理
			// 注意判断返回金额

			if (uin != null) {// 客户存在　
				int result = daService.update("update user_info_tb set balance =balance+? where id=?  ",
								new Object[] { Double.valueOf(total), uin });

				try {
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					Map<String, Object> userSqlMap = new HashMap<String, Object>();
					//Map<String, Object> tcbFeeSqlMap = new HashMap<String, Object>();
					Map<String, Object> tcbSqlMap = new HashMap<String, Object>();
					userSqlMap.put("sql", "insert into user_account_tb(uin,amount,type,create_time,remark,pay_type,orderid) values(?,?,?,?,?,?,?)");
					userSqlMap.put("values", new Object[]{uin,Double.valueOf(total),0,System.currentTimeMillis() / 1000 - 2,"微信充值", 2, Long.valueOf(orderId) });
					bathSql.add(userSqlMap);
					tcbSqlMap.put("sql", "insert into tingchebao_account_tb(amount,type,create_time,remark,utype,orderid,online_orderid,uin) values(?,?,?,?,?,?,?,?)");
					tcbSqlMap.put("values", new Object[]{total,0,System.currentTimeMillis() / 1000 - 2,"微信充值", 6, Long.valueOf(orderId), out_trade_no, uin });
					bathSql.add(tcbSqlMap);
//					tcbFeeSqlMap.put("sql", "insert into tingchebao_account_tb(amount,type,create_time,remark,utype,orderid,online_orderid,uin) values(?,?,?,?,?,?,?,?)");
//					tcbFeeSqlMap.put("values", new Object[]{Double.valueOf(total) * 0.006,1,System.currentTimeMillis() / 1000 - 2,"微信充值手续费", 2, Long.valueOf(orderId), out_trade_no, uin });
//					bathSql.add(tcbFeeSqlMap);
					
					boolean b = daService.bathUpdate(bathSql);
					logger.error("orderid:"+orderId+",uin:"+uin+",b:"+b);
					//写用户支付账号信息表
					logService.insertUserAccountMesg(1, uin, userPayAccount);
					
				} catch (Exception e) {
					e.printStackTrace();
				}

				logger.error("充值,写入客户帐户:money=" + total + ",uin=" + uin+ ",result=" + result);
				if (result == 1) {
					// 写阿里充值日志
					logger.error("充值成功，写入微信充值日志 ....");
					daService.update("insert into alipay_log (notify_no,create_time,uin,money,orderid) values(?,?,?,?,?)",
									new Object[] { out_trade_no,System.currentTimeMillis() / 1000,uin, Double.valueOf(total),Long.valueOf(orderId) });
					logger.error("充值结果：" + result + "，手机号：" + mobile + " ，金额 ："+ total);
					// 判断下一步操作，0不操作，1购买包月产品 2支付订单
					if (type.equals("0")) {// 仅充值
						logger.error("仅充值...");
						daService.update("insert into money_record_tb(comid,create_time,amount,uin,type,pay_type,remark) values (?,?,?,?,?,?,?)",
										new Object[] {-1L,System.currentTimeMillis() / 1000,Double.valueOf(total), uin,ZLDType.MONEY_RECARGE, payType,"充值" });
						logger.error("充值成功，写入充值日志 ....");
						//仅充值时，认证过车主应该先补全信用额度30元，补全后多出的金额才可以充到余额中。
						Map userMap = daService.getMap("select is_auth,credit_limit from user_info_Tb where id=? ", new Object[]{uin});
						Integer isAuth =0;
						if(userMap!=null){
							isAuth= (Integer)userMap.get("is_auth");
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
						Long bid=0L;
						/*if(total==100&&isAuth==1){//充值100返大礼包
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
						logService.insertMessage(-1L, 1, uin, "", bid,Double.valueOf(total), total + "元充值成功", 0, 0L,0L, 2);
						//publicMethods.sendMessageToThird(uin, Integer.valueOf(total_fee), null, null, null, 1);
					} else if (type.equals("1")) {// 充值并购买包月产品
						logger.error("充值并购买包月产品...");
						Map productMap = daService.getMap("select * from product_package_tb where id=? and state=? and remain_number>? ",
										new Object[] { Long.valueOf(pid), 0, 0 });
						if (productMap != null) {
							String cname = (String) daService.getObject("select company_name from com_info_tb where id=?",
											new Object[] { productMap.get("comid") },String.class);
							// 写充值日志
							Double price = Double.valueOf(productMap.get("price") + "");
							price = price * Integer.valueOf(number);
							if (price > Double.valueOf(total))
								payType = 5;
							daService.update("insert into money_record_tb(comid,create_time,amount,uin,type,pay_type,remark) values (?,?,?,?,?,?,?)",
											new Object[] {-1L,System.currentTimeMillis() / 1000,Double.valueOf(total),uin,
													ZLDType.MONEY_RECARGE,payType,productMap.get("p_name")+ "充值 - " + cname });
							logger.error("充值成功，写入充值日志 ....");
							result = publicMethods.buyProducts(uin, productMap,
									Integer.valueOf(number), start,"", payType);
							logger.error(productMap.get("p_name") + "充值 - "
									+ cname + ":" + total + ",result:" + result);
							// 给用户（车主）发消息
							logService.insertMessage(-1L, 1, uin, "", 0L,Double.valueOf(total), total + "元充值并购买"+ productMap.get("p_name") + "成功",0, 0L, 0L, 2);

						}
					} else if (type.equals("2")) {// 充值并支付订单...
						logger.error("充值并支付订单...");
						// 写充值日志
						Map orderMap = daService.getMap("select * from order_tb where id=? ",
								new Object[] { Long.valueOf(orderId) });
						logger.error("total:"+total+"ordermap:"+orderMap);
						if (orderMap != null) {
							String cname = (String) daService.getObject("select company_name from com_info_tb where id=?",
											new Object[] { orderMap.get("comid") },String.class);
							Long comId = (Long)orderMap.get("comid");
							Double ordermoney =  0d;
							if(orderMap.get("total") != null){
								ordermoney = StringUtils.formatDouble(orderMap.get("total"));
							}
							if(ordermoney==0.0)
								ordermoney =  StringUtils.formatDouble(total);
							//Double price = Double.valueOf(orderMap.get("total")+ "");
							if (Double.valueOf(total) < ordermoney) {
								payType = 5;
							}
							daService.update("insert into money_record_tb(comid,create_time,amount,uin,type,pay_type,remark) values (?,?,?,?,?,?,?)",
											new Object[] {-1L,System.currentTimeMillis() / 1000,Double.valueOf(total), uin,
													ZLDType.MONEY_RECARGE,payType, "停车费充值-" + cname });
							logger.error("充值成功，写入充值日志 ....");
							
							result = publicMethods.payOrder(orderMap,ordermoney,
									uin, 2, payType, ticketId, null,-1L,uid);
							logger.error("支付订单:" + result+",orderid:"+orderId+",uin:"+uin+",comid:"+orderMap.get("comid"));
							int _state = -1;// 默认支付不成功
							String carNumber = (String)orderMap.get("car_number");
							if(carNumber==null||"".equals(carNumber)||"车牌号未知".equals(carNumber))
									carNumber = publicMethods.getCarNumber(uin);
							Long endTime =(Long)orderMap.get("end_time"); 
							if(endTime==null)
								endTime=System.currentTimeMillis()/1000;
							if (result == 5) {
								_state = 2;
								result = 1; 
								// 发支付成功消息给收费员
								logService.insertParkUserMessage((Long) orderMap.get("comid"),_state,(Long) orderMap.get("uid"),carNumber,
										Long.valueOf(orderId),ordermoney, "", 0,
										(Long) orderMap.get("create_time"),endTime, 0, null);
								// 发消息给车主
							}else if(result==-7){
								logService.insertUserMesg(0, uin, "由于网络原因，"+cname+"，停车费"+total+"元，微信支付失败", "支付失败提醒");
							}
							if(comId==20130){//宝和公司的测试订单，调用他们的接口发送订单支付状态
								String sr = commonMethods.sendOrderState2Baohe(Long.valueOf(orderId), result,ordermoney, payType);
								logger.error(">>>>>>>>>>>>>baohe sendresult:"+sr+",orderid:"+orderId+",state:"+result+",total:"+ordermoney+",paytype:"+payType);
							}
							logService.insertMessage((Long) orderMap.get("comid"),_state, uin, carNumber,Long.valueOf(orderId),
									ordermoney,"", 0, (Long) orderMap.get("create_time"),
									endTime, 0);
						}
					}else if(type.equals("3")) {//充值并直接支付给收费员...
						Long comId = daService.getLong("select comid from user_info_tb where id=? ", new Object[]{uid});
						String carNumber = publicMethods.getCarNumber(uin);
						result = publicMethods.epay(comId,money, uin, uid, ticketId, carNumber,5, bind_flag,Long.valueOf(orderId),null);
						logger.error(">>>>车主直接支付给收费员:" + result);
						int _state = -1;// 默认支付不成功
						if (result == 5) {
							_state = 2;
							result = 1;
							// 发支付成功消息给收费员
							logService.insertParkUserMessage(comId,_state,uid,carNumber,Long.valueOf(orderId),money, "", 0,ntime,ntime+10, 0, null);
							// 发消息给车主
						}
						//logService.doMessage(comId, _state, uin, carNumber,-1L,money, "支付成功",0, ntime,ntime+10, 2);
						logService.insertMessage(comId,_state, uin, carNumber,Long.valueOf(orderId),money,"", 0, ntime,ntime+10, 0);
					}else if(type.equals("4")){//充值并打赏
						int ptype = 2;//微信
						if(money>Double.valueOf(total)){
							ptype=5;//余额+微信
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
							logService.insertMessage(-1L, 1, uin,carNumber, 0L,StringUtils.formatDouble(total),"支付成功!", 0,ntime,ntime+10,2);
						}else if(ret==-2){
							logService.insertMessage(-1L, 0, uin,carNumber, 0L,StringUtils.formatDouble(total),"已打赏过", 0,ntime,ntime+10,2);
						}else {
							logService.insertMessage(-1L, 0, uin,carNumber, 0L,StringUtils.formatDouble(total),"支付失败", 0,ntime,ntime+10,2);
						}
					}else if(type.equals("5")){//充值并购买停车券
						boolean isAuth = publicMethods.isAuthUser(uin);
						Integer discount = 9;
						if(isAuth)
							discount=7;
						Double ttotal =  StringUtils.formatDouble(ticketNumber*ticketPrice*discount*0.1);
						int pttype=2;
						if(ttotal>StringUtils.formatDouble(total))
							pttype=5;
						int ret = publicMethods.buyTickets(uin, ticketPrice, ticketNumber, pttype);
						logger.error(mobile+"微信购买停车券：val:"+ticketPrice+",num:"+ticketNumber+",ret:" + ret);
						// 给用户（车主）发消息
						logService.insertMessage(-1L, 1, uin, "", 0L,Double.valueOf(total), total + "元充值并购买停车券成功",0, 0L, 0L, 2);
					}else if(type.equals("6")){
						logger.error("weixin prepay>>>orderid:"+orderId+",uin:"+uin+",wx_total:"+total+",money:"+money+",tickid:"+ticketId);
						Map orderMap = daService.getMap("select * from order_tb where id=? ",
								new Object[] { Long.valueOf(orderId) });
						Long comId = (Long)orderMap.get("comid");
						String carNumber = (String)orderMap.get("car_number");
						if(carNumber==null||"".equals(carNumber)||"车牌号未知".equals(carNumber))
								carNumber = publicMethods.getCarNumber(uin);
						int ret = publicMethods.prepay(orderMap, money, uin, ticketId, payType, bind_flag, null);
						logger.error("weixin prepay>>>orderid:"+orderId+",uin:"+uin+",ret:"+ret+",comId:"+comId);
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
					}else if(result==-7){
						logService.insertUserMesg(0, uin, "直付停车费"+total+"元，微信支付失败，充值金额已进入你的账户", "支付失败提醒");
					}
				} else {
					logService.insertMessage(-1L, 0, uin, "", 0L,Double.valueOf(total), "写入用户余额失败", 0, 0L, 0L, 2);
				}
			} else {
				logger.error("充值错误，客户不存在，手机：" + mobile);
				// resHandler.sendToCFT("Fail");
				logService.insertMessage(-1L, 0, uin, "", 0L,Double.valueOf(total), "车主信息不存在", 0, 0L, 0L, 2);
				// return;
			}
			// ------------------------------
			// 处理业务完毕
			// ------------------------------
			logger.error("微信支付成功....发消息给车主");
			AjaxUtil.ajaxOutput(resp, "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");//resHandler.sendToCFT("Success");
			return;
			/*
			 * } else { // 错误时，返回结果未签名，记录retcode、retmsg看失败详情。
			 * System.out.println("查询验证签名失败或业务错误");
			 * System.out.println("retcode:"+ queryRes.getParameter("retcode") +
			 * " retmsg:" + queryRes.getParameter("retmsg"));
			 * logService.doMessage(-1L, 0, uin,"",
			 * 0L,Double.valueOf(total),"微信验证不通过", 0,0L,0L,2);
			 * logger.error("微信支付错误...."); }
			 */

			/*
			 * } else {
			 * 
			 * System.out.println("后台调用通信失败"); //logService.doMessage(-1L, 0,
			 * uin,"", 0L,Double.valueOf(total),"微信验证不通过", 0,0L,0L,2);
			 * System.out.println(httpClient.getResponseCode());
			 * System.out.println(httpClient.getErrInfo()); //
			 * 有可能因为网络原因，请求已经处理，但未收到应答。 logger.error("微信支付错误...."); }
			 */
		} else {
			logger.error("weixin callback params:"+paramMap+",osign:"+sign+",nsign:"+vsign);
			return_msg="通知签名验证失败";
			System.out.println("通知签名验证失败");
			logger.error("微信支付错误...."+return_msg);
			AjaxUtil.ajaxOutput(resp, "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA["+return_msg+"]]></return_msg></xml>");//resHandler.sendToCFT("Success");
		}
		
	}
	/**
	 * 创建md5摘要,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
	 */
	public String createSign(Map<String, Object> packageParams) {
		StringBuffer sb = new StringBuffer();
		List<String> keys = new ArrayList<String>(packageParams.keySet());
		Collections.sort(keys);
		for(String key :keys) {
			String k = key;
			String v = (String)packageParams.get(key);
			if (null != v && !"".equals(v) && !"sign".equals(k)
					&& !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + Constants.WXPAY_PARTNERKEY);
		System.out.println("md5 sb:" + sb);
		String sign = MD5Util.MD5Encode(sb.toString(), "UTF-8")
				.toUpperCase();

		return sign;

	}
	/*public static void main(String[] args) {
		String string ="<xml><appid><![CDATA[wx485c58b62cbb4dd0]]></appid><attach><![CDATA[18101333937_0_yangzhou]]></attach><bank_type><![CDATA[CFT]]></bank_type><cash_fee><![CDATA[1]]></cash_fee><fee_type><![CDATA[CNY]]></fee_type><is_subscribe><![CDATA[N]]></is_subscribe><mch_id><![CDATA[1332937401]]></mch_id><nonce_str><![CDATA[a724b9124acc7b5058ed75a31a9c2919]]></nonce_str><openid><![CDATA[oETT-w27Rb51THrC40CA7dQ1I4H8]]></openid><out_trade_no><![CDATA[22bcf8e0aeae1b6e4cdb0677cf700f1d]]></out_trade_no><result_code><![CDATA[SUCCESS]]></result_code><return_code><![CDATA[SUCCESS]]></return_code><sign><![CDATA[0484D7B17D044E07F6CEEE481B7A564E]]></sign><time_end><![CDATA[20160430010640]]></time_end><total_fee>1</total_fee><trade_type><![CDATA[APP]]></trade_type><transaction_id><![CDATA[4006662001201604305357218668]]></transaction_id></xml>";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap = ZldXMLUtils.parserStrXml(string);
		String sign =(String) paramMap.get("sign");
		paramMap.remove("sign");
		String vsign  = createSign(paramMap);
		String userPayAccount =(String) paramMap.get("openid");
		System.err.println(sign+",sign:"+vsign+","+userPayAccount);
	}*/
}
