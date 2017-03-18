package com.zld.struts.request;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
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
import com.zld.facade.GenPosOrderFacade;
import com.zld.facade.PayPosOrderFacade;
import com.zld.facade.StatsAccountFacade;
import com.zld.impl.CommonMethods;
import com.zld.impl.MemcacheUtils;
import com.zld.impl.MongoClientFactory;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.pojo.ActCardReq;
import com.zld.pojo.AutoPayPosOrderFacadeReq;
import com.zld.pojo.AutoPayPosOrderResp;
import com.zld.pojo.BaseResp;
import com.zld.pojo.BerthSeg;
import com.zld.pojo.BindCardReq;
import com.zld.pojo.CardChargeReq;
import com.zld.pojo.CardInfoReq;
import com.zld.pojo.CardInfoResp;
import com.zld.pojo.CollectorSetting;
import com.zld.pojo.GenPosOrderFacadeReq;
import com.zld.pojo.GenPosOrderFacadeResp;
import com.zld.pojo.Group;
import com.zld.pojo.ManuPayPosOrderFacadeReq;
import com.zld.pojo.ManuPayPosOrderResp;
import com.zld.pojo.Order;
import com.zld.pojo.ParseJson;
import com.zld.pojo.PayEscapePosOrderFacadeReq;
import com.zld.pojo.PayEscapePosOrderResp;
import com.zld.pojo.StatsAccountClass;
import com.zld.pojo.StatsFacadeResp;
import com.zld.pojo.StatsReq;
import com.zld.pojo.StatsWorkRecordInfo;
import com.zld.pojo.StatsWorkRecordResp;
import com.zld.pojo.UnbindCardReq;
import com.zld.pojo.WorkRecord;
import com.zld.service.CardService;
import com.zld.service.DataBaseService;
import com.zld.service.LogService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.ExecutorsUtil;
import com.zld.utils.OrderSortCompare;
import com.zld.utils.ParkingMap;
import com.zld.utils.RequestUtil;
import com.zld.utils.SendMessage;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.utils.ToAlipayQrTradePay;
/**
 * 停车场收费员请求处理，分享车位，打折处理等
 * @author Administrator
 *
 */
public class CollectorRequestAction extends Action{
	
	@Autowired
	private DataBaseService daService;
	@Autowired
	private LogService logService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private PgOnlyReadService pService;
	@Autowired 
	private CommonMethods commonMethods;
	@Autowired 
	private MongoDbUtils mongoDbUtils;
	@Autowired
	private PayPosOrderFacade payOrderFacade;
	@Autowired
	private GenPosOrderFacade genOrderFacade;
	@Autowired
	private CardService cardService;
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Autowired
	private StatsAccountFacade accountFacade;
	@Autowired
	private ToAlipayQrTradePay toAlipayQrTradePay;
	
	private Logger logger = Logger.getLogger(CollectorRequestAction.class);
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json");
		String token =RequestUtil.processParams(request, "token");
		String action =RequestUtil.processParams(request, "action");
		String out= RequestUtil.processParams(request, "out");
		Map<String,Object> infoMap = new HashMap<String, Object>();
		Long comId = null;//收费员所在车场
		Long uin = null;//收费员账号
		Long groupId = null;//收费员所在运营集团编号
		Long authFlag = 0L;
		if(token.equals("")){
			infoMap.put("info", "no token");
		}else {
			if(token.equals("notoken")){
				comId = RequestUtil.getLong(request, "comid",-1L);
				uin = RequestUtil.getLong(request, "uin",-1L);
			}else{
				Map<String, Object> comMap = daService.getPojo("select * from user_session_tb" +
						" where token=?", new Object[]{token});
				if(comMap != null && comMap.get("comid") != null){
					comId = (Long)comMap.get("comid");
					uin = (Long) comMap.get("uin");
					groupId = (Long)comMap.get("groupid");
				}else {
					infoMap.put("info", "token is invalid");
				}
			}
		}
		logger.error("token="+token+",comid="+comId+",action="+action+",uin="+uin);
		/*token为空或token无效时，返回错误		 */
		if(token.equals("") || comId == null || uin == null){
			if(out.equals("json"))
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
			else
				AjaxUtil.ajaxOutput(response, StringUtils.createXML(infoMap));
			return null;
		}
		String result ="";
		if(action.equals("myinfo")){
			result= myInfo(uin);
			//test:http://127.0.0.1/zld/collectorrequest.do?action=myinfo&token=0dc591f7ddda2d6fb73cd8c2b4e4a372
		}else if(action.equals("comparks")){
			//http://127.0.0.1/zld/collectorrequest.do?action=comparks&out=josn&token=5f0c0edb1cc891ac9c3fa248a28c14d5
			result =getComParks(comId);
			result = result.replace("null", "");
		}else if(action.equals("autoup")){//自动抬杆
			result=  autoUp(request,comId,uin);
			//http://127.0.0.1/zld/collectorrequest.do?action=autoup&price=&carnumber=&token=0dc591f7ddda2d6fb73cd8c2b4e4a372
		}else if(action.equals("toshare")){//分享车位
			Integer number = RequestUtil.getInteger(request, "s_number", -1);
			boolean isCanLalaRecord = ParkingMap.isCanRecordLaLa(uin);
			if(number!=-1){
				doShare(comId, uin,number,infoMap,isCanLalaRecord);
			}else {
				infoMap.put("info", "fail");
				infoMap.put("message", "分享数量不合法!");
			}
			if(out.equals("json")){
				result= StringUtils.createJson(infoMap);
			}else
				result=StringUtils.createXML(infoMap);
			//test:http://127.0.0.1/zld/collectorrequest.do?action=toshare&token=d450ea04d67bf0b428ea1204675d5b53&s_number=800

		}else if(action.equals("tosale")){//打折处理
			Long orderId = RequestUtil.getLong(request, "orderid", -1L);
			Integer houer = RequestUtil.getInteger(request, "hour", 0);
			if(orderId!=-1&&houer>0){
				doSale(comId, uin,houer, orderId,infoMap);
			}else {
				infoMap.put("info", "错误：没有订单编号或优惠小时!");
			}
			if(out.equals("json")){
				result= StringUtils.createJson(infoMap);
			}else
				result=StringUtils.createXML(infoMap);
			//test:http://127.0.0.1/zld/collectorrequest.do?action=tosale&token=d450ea04d67bf0b428ea1204675d5b53&orderid=1&hour=1
		}else if(action.equals("orderdetail")){//订单详情
			orderDetail(request,comId,uin,infoMap);
			if(out.equals("json")){
				result= StringUtils.createJson(infoMap);
			}else
				result= StringUtils.createXML(infoMap);
			result = result.replace("null", "");
			//http://127.0.0.1:8080/zld/collectorrequest.do?action=orderdetail&token=9e47c76a147e18ae9c60584de673ed9e&orderid=18245855&out=json
		}else if(action.equals("currorders")){//当前订单
			result = currOrders(request,uin,comId,out,infoMap);
			System.out.println(result);
			//test:http://127.0.0.1/zld/collectorrequest.do?action=currorders&token=4bad81d8d7993446265a155318182dee&page=1&size=10&out=json
		}else if(action.equals("orderhistory")){//历史订单
			result = orderHistory(request, comId, out, groupId);
			//http://127.0.0.1/zld/collectorrequest.do?action=orderhistory&day=last&uid=10828&ptype=0&token=ec5c8185dae6f48c03c43785fe17be22&uin=10824&page=1&size=10&out=json
		}else if(action.equals("ordercash")){//现金收费
			result = orderCash(request, out, uin, groupId);
			//test:http://127.0.0.1/zld/collectorrequest.do?action=ordercash&token=5286f078c6d2ecde9b30929f77771149&orderid=787824
		}else if(action.equals("ordercard")){//刷卡收费
			result = manuPayPosOrder(request, uin, 1, groupId);
		}else if(action.equals("freeorder")){//HD版，免费放行
			result = freeOrder(request,uin,comId);
			//http://192.168.199.239/zld/collectorrequest.do?action=freeorder&token=7d4860ef99bd70d5c91af535bb2c5065&orderid=1
		}else if(action.equals("cominfo")){//公司信息
			comInfo(request,comId,infoMap);
			//System.err.println(infoMap);
			//test:http://127.0.0.1/zld/collectorrequest.do?action=cominfo&token=761afb1ecc204a2d73223c2e96ae6b80&out=json
			if(out.equals("json")){
				result= StringUtils.createJson(infoMap);
			}else
				result=StringUtils.createXML(infoMap);
		}else if(action.equals("corder")){//一键查询
			result = corder(request,comId);
		}
		else if(action.equals("score")){//请求积分
			result= "[]";
		}else if(action.equals("getparkaccount")){
			Map parkAccountMap  = daService.getMap("select sum(amount) amount from park_account_tb where create_time>? and uid =? and type=? ", 
					new Object[]{TimeTools.getToDayBeginTime(),uin,0});
			Double total = 0d;
			if(parkAccountMap!=null){
				total =StringUtils.formatDouble(parkAccountMap.get("amount"));
			}
			result=""+total;
			//http://127.0.0.1/zld/collectorrequest.do?action=getparkaccount&token=4182e6ad895208c3d4829d447e0c61b7
		}else if(action.equals("getpadetail")){//账户明细
			result = getpaDetail(request,comId);
			//http://127.0.0.1/zld/collectorrequest.do?action=getpadetail&token=c5ea6e5fd0acdf97a262f7f86c31f3ae
		}else if(action.equals("withdraw")){//车场提现请求
			//http://192.168.199.240/zld/collectorrequest.do?action=withdraw&uid=10343&comid=858&money=20
			result = withdraw(request,uin,comId);
		}else if(action.equals("getpaccount")){//查询停车场账户总额
			Map comMap = daService.getMap("select money from com_info_tb where id=?", new Object[]{comId});
			Double total = 0d;
			if(comMap!=null){
				total = StringUtils.formatDouble(comMap.get("money"));
			}
			result= total+"";
			//http://127.0.0.1/zld/collectorrequest.do?action=getpaccount&token=17ad4f0a3cbdce40c56595f00d7666bc
		}else if(action.equals("getparkbank")){
			Map comaMap  = daService.getMap("select id,card_number,name,mobile,bank_name,area,bank_pint,user_id from com_account_tb where comid=? and type=? order by id desc ",new Object[]{comId,0});
			result=StringUtils.createJson(comaMap);
			result =  result.replace("null", "");
			//http://127.0.0.1/zld/collectorrequest.do?action=getparkbank&token=17ad4f0a3cbdce40c56595f00d7666bc
		}else if(action.equals("addparkbank")){
			result = addParkBank(request,uin,comId);
		}else if(action.equals("editpbank")){
			result = editpBank(request,uin,comId);
		}else if(action.equals("uploadll")){//upload lat and lon, 收费员上传经纬度
			result = uploadll(request,uin,comId,authFlag);
		}else if(action.equals("reguser")){//收费员推荐车主
			result = reguser(request,uin,comId);
		}else if(action.equals("regcolmsg")){//短信推荐车场
			result = regcolmsg(request,uin,comId,out);
		}else if(action.equals("recominfo")){//车场收费员推荐记录
			result = recominfo(request,uin);
		}else if(action.equals("getmesg")){
			result = getMesg(request,uin);
		}else if(action.equals("getincome")){//照牌收费员一段时间内现金收入金额和手机收入金额
			result = getIncome(request,uin);
			//http://127.0.0.1/zld/collectorrequest.do?action=getincome&token=15d1bb15b8dcb99aa7dbe0adc9797162&btime=2012-12-28
		}else if(action.equals("getnewincome")){//照牌收费员一段时间内现金收入金额和手机收入金额
			result = getNewIncome(request,uin);
			//http://127.0.0.1/zld/collectorrequest.do?action=getincome&token=15d1bb15b8dcb99aa7dbe0adc9797162&btime=2012-12-28
		}else if(action.equals("querycarpics")){//车场一个月的车牌缓存
			result = queryCarPics(request,uin,comId);
			//http://192.168.10.239/zld/collectorrequest.do?action=querycarpics&token=
		}else if(action.equals("incomanly")){//收入统计
			result = incomAnly(request,uin,comId);
			//http://192.168.199.240/zld/collectorrequest.do?action=incomanly&acctype=1&incom=2&datetype=2&page=1&token=6d5d6a1bd45b5dafd2294e99cf9c91c9
		}else if(action.equals("invalidorders")){
			Long invalid_order = RequestUtil.getLong(request, "invalid_order", 0L);
			int ret = daService.update("update com_info_tb set invalid_order=invalid_order+? where id=?", new Object[]{invalid_order, comId});
			result = result + "";
			//http://192.168.199.239/zld/collectorrequest.do?action=invalidorders&invalid_order=-1&token=198f697eb27de5515e91a70d1f64cec7
		}else if(action.equals("bindworksite")){//收费员绑定工作站
			result = bindWorkSite(request,uin);
			//collectorrequest.do?action=bindworksite&wid=&token=198f697eb27de5515e91a70d1f64cec7
		}else if(action.equals("gooffwork")){//收费员下班
			result = goOffWork(request,uin);
		}else if(action.equals("akeycheckaccount")){
			result = akeyCheckAccount(request,uin,comId);
		}else if(action.equals("getparkdetail")){//管理员查看整个车场的停车费明细
			result = getParkDetail(request,uin,comId,authFlag);
		}else if(action.equals("countprice")){
			Long btime = RequestUtil.getLong(request, "btime", -1L);
			Long etime = RequestUtil.getLong(request, "etime", -1L);
			Map<String,Object> info = new HashMap<String,Object>();
			String ret = publicMethods.getPrice(btime, etime, comId, 0);
			info.put("total", ret);
			result = StringUtils.createJson(info);
		}else if(action.equals("rewardscore")){
			infoMap =rewardscore(request,uin,infoMap);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
			result = StringUtils.createJson(infoMap);
			//http://127.0.0.1/zld/collectorrequest.do?action=rewardscore&token=5286f078c6d2ecde9b30929f77771149
		}else if(action.equals("rscorerank")){//积分排行榜
			result = rscoreRank(request,uin);
			//http://127.0.0.1/zld/collectorrequest.do?action=rscorerank&token=5286f078c6d2ecde9b30929f77771149
		}else if(action.equals("rewardrank")){
			result = rewardRank(request,uin);
			//http://127.0.0.1/zld/collectorrequest.do?action=rewardrank&token=5286f078c6d2ecde9b30929f77771149
		}else if(action.equals("bonusinfo")){
			result = bonusInfo(request,uin);
			//http://127.0.0.1/zld/collectorrequest.do?action=bonusinfo&token=67579fd93b96ad32ced2584b54d8454f
		}else if(action.equals("sendticket")){
			result = sendTicket(request,uin,comId);
			//http://127.0.0.1/zld/collectorrequest.do?action=sendticket&token=5286f078c6d2ecde9b30929f77771149&bmoney=3&score=1&uins=21616,21577,21554
		}else if(action.equals("sendbonus")){
			result = sendBonus(request,uin,comId);
			//http://127.0.0.1/zld/collectorrequest.do?action=sendbonus&token=5286f078c6d2ecde9b30929f77771149&bmoney=12&bnum=8&score=1
		}else if(action.equals("sendsuccess")){
			result = sendSuccess(request,uin);
		}else if(action.equals("rewardlist")){
			result = rewardList(request,uin);
			//http://127.0.0.1/zld/collectorrequest.do?action=rewardlist&token=116a87809926db5c477a9a1a58488ec1
		}else if(action.equals("parkinglist")){
			result = parkingList(request,uin);
			//http://127.0.0.1/zld/collectorrequest.do?action=parkinglist&token=116a87809926db5c477a9a1a58488ec1
		}else if(action.equals("sweepticket")){
			result = sweepTicket(request,uin,comId);
			//http://127.0.0.1/zld/collectorrequest.do?action=sweepticket&bmoney=3&score=1&token=5286f078c6d2ecde9b30929f77771149
		}else if(action.equals("deductscore")){//往微信里发送，用户点击领取
			result = deductScore(request,uin,comId,infoMap);
			//http://127.0.0.1/zld/collectorrequest.do?action=deductscore&score=1&ticketid=&token=
		}else if(action.equals("todayaccount")){//今日账户、积分、打赏查询
			result = todayAccount(request,uin,comId,infoMap);
			//http://127.0.0.1/zld/collectorrequest.do?action=todayaccount&token=5286f078c6d2ecde9b30929f77771149
		}else if(action.equals("remainscore")){
			Double todayscore = 0d;//剩余积分
			Map score = daService.getMap("select reward_score from user_info_tb where id=? ", new Object[] { uin });
			if(score != null && score.get("reward_score") != null){
				todayscore = Double.valueOf(score.get("reward_score") + "");
			}
			infoMap.put("score", todayscore);
			result =StringUtils.createJson(infoMap);
		}else if(action.equals("queryaccount")){//根据车牌查询在该车场的账户明细
			result = queryAccount(request,uin);
			//http://127.0.0.1/zld/collectorrequest.do?action=queryaccount&token=2dd4b1b320225dfd4fc44ad6b53fa734&carnumber=京QLL122
		}else if(action.equals("posincome")){//pos机生成订单
			result = posIncome(request, comId, groupId, uin);
			//http://127.0.0.1/zld/collectorrequest.do?action=posincome&token=2dd4b1b320225dfd4fc44ad6b53fa734&carnumber=京QLL122
		}else if(action.equals("getfreeparks")){//查询所有空闲车位
			result = getFreeParks(request,comId);
			//http://127.0.0.1/zld/collectorrequest.do?action=getfreeparks&token=5ebac7b26ce782ebffaadf76b1519a64
		}else if(action.equals("bondcarpark")){//绑定车位到车上
			result = bondCarPark(request,comId);
			//http://127.0.0.1/zld/collectorrequest.do?action=bondcarpark&token=a0b952263fbb0a264194a1443c71174d&orderid=11111&id=134
		}else if(action.equals("liftrodrecord")){//抬杆记录
			result = liftRod(request,uin,comId);
			//http://127.0.0.1/zld/collectorrequest.do?action=liftrodrecord&token=d481a6fb58e758c3f0ef9aa7c4bdff29&passid=13
		}else if(action.equals("liftrodreason")){//抬杆记录，更新原因
			result = liftRodReason(request);
			//http://127.0.0.1/zld/collectorrequest.do?action=liftrodreason&token=d481a6fb58e758c3f0ef9aa7c4bdff29&lrid=3&reason=1
		}else if(action.equals("liftroduppic")){//抬杆记录，上传图片
			result = liftRodPic(request);
			//http://127.0.0.1/zld/collectorrequest.do?action=liftroduppic&token=a0b952263fbb0a264194a1443c71174d&lrid=3
		}else if(action.equals("getberths")){//查询泊位信息，签到
			//http://127.0.0.1/zld/collectorrequest.do?action=getberths&token=522b6bc2abd903eacf6b9a4ae3359815&berthid=15&devicecode=357143047019192
			result =getBerths(request,uin,comId,token, groupId);
			result= result==null?"{}":result.replace("null", "");
			//logger.error("签到："+result);
		}else if(action.equals("workout")){//签退操作
			//http://127.0.0.1/zld/collectorrequest.do?action=workout&token=f0f8f63ebd720c34077ee6b302b15cff&berthid=-1&workid=44068&from=1
			result = workOut(request,uin);
		}else if(action.equals("uporderpic")){//上传车辆图片 
			//http://127.0.0.1/zld/collectorrequest.do?action=uporderpic&token=ca67649c7a6c023e08b0357658c08c3d&orderid=&type=
			result = uporderpic(request);
		}else if(action.equals("getecsorder")){//根据车牌查找本集团车场的逃单
			//http://127.0.0.1/zld/collectorrequest.do?action=getecsorder&token=149032374fcb20d719a08acb237cfc4d&comid=8690&car_number=无095455
			result = queryEscOrder(request, uin, comId, groupId);
		}else if(action.equals("payescorder")){//支付逃单，支付多外逃单处理
			//http://127.0.0.1/zld/collectorrequest.do?action=payescorder&token=53aa954da7de01e1e7439fb386c41234&orderlist=
			result = payEscOrder(request, uin, groupId, comId);
		}else if(action.equals("getorderpic")){//取订单图片orderid 订单编号 type:0入场照片，1出场照片 2追缴
			getOrderPic(request,response);
		  //http://127.0.0.1/zld/collectorrequest.do?action=getorderpic&token=a3a0dafbe61d9b491b6094b6f64a0693&orderid=842138&type=0
		}else if(action.equals("regpossequence")){//注册POS机串号
			result = regPosSequecce(request,comId,uin);
			 //http://127.0.0.1/zld/collectorrequest.do?action=regpossequence&token=a3a0dafbe61d9b491b6094b6f64a0693&device_code=
		}else if(action.equals("paydetail")){//查询本次签到内的收费
			result = getPayDetail(request);
			//http://127.0.0.1/zld/collectorrequest.do?action=paydetail&token=149032374fcb20d719a08acb237cfc4d&workid=768
		}else if(action.equals("paymonthorder")){//支付月卡
			result = payMonthOrder(request,uin);
			//http://127.0.0.1/zld/collectorrequest.do?action=paymonthorder&token=149032374fcb20d719a08acb237cfc4d&orderid=
		}else if(action.equals("payposorder")){//pos机调用结算，处理月卡及余额支付
			result = payPosOrder(request, uin, groupId);
			logger.error("payposorder result :"+result);
			/*http://127.0.0.1/zld/collectorrequest.do?action=payposorder&
			token=2d0f797087b3c43b27700a1ef0b14a10&orderid=842690&version=1341&total=0*/
		}else if(action.equals("actcard")){//激活卡片
			result = actCard(request, uin, groupId, comId);
		}else if(action.equals("bindcard")){//卡片绑定用户
			result = bindCard(request, uin, groupId, comId);
		}else if(action.equals("cardinfo")){//卡片详情
			result = cardInfo(request, uin, groupId);
			/*http://127.0.0.1/zld/collectorrequest.do?action=cardinfo&
			token=&version=&uuid=*/
		}else if(action.equals("chargecard")){//充值卡片
			result = chargeCard(request, uin, groupId, comId);
			/*http://127.0.0.1/zld/collectorrequest.do?action=chargecard&
			token=&version=&uuid=&money=*/
		}else if(action.equals("closecard")){//注销卡片
			result = closeCard(request, uin, groupId);
			/*http://127.0.0.1/zld/collectorrequest.do?action=closecard&
			token=&version=&uuid=*/
		}else if(action.equals("zfbpayqr")){//请求支付宝支付二维码
			//http://127.0.0.1/zld/collectorrequest.do?action=zfbpayqr&orderid=&total=
			result = zfbPayQr(request, uin);
		}else if(action.equals("getservertime")){
			result = getCurrentTime() + "";
		}
		
		AjaxUtil.ajaxOutput(response, result);
		return null;
	}
	
	private long getCurrentTime(){
		try {
			long currentTime = System.currentTimeMillis();
			return currentTime;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private String zfbPayQr(HttpServletRequest request, Long uid) {
		Long orderId = RequestUtil.getLong(request, "orderid", -1L);
		Double total = StringUtils.formatDouble(RequestUtil.getString(request, "total"));
		String ret = "{\"state\":\"0\",\"errmsg\":\"请稍候重试\"}";
		String qr = "";
		if(orderId>0&&total>0){
			Map orderMap = pService.getMap("select uin,car_number,comid,create_time from order_tb where id =? ", new Object[]{orderId});
			Long uin = -1L;
			String car_number ="";
			if(orderMap!=null){
				//查询公司信息
				Long ctime = (Long)orderMap.get("create_time");
				String time = TimeTools.getTime_MMdd_HHmm(ctime*1000)+"-"+TimeTools.getTime_MMdd_HHmm(System.currentTimeMillis());
				uin = (Long)orderMap.get("uin");
				car_number=(String)orderMap.get("car_number");
				qr = toAlipayQrTradePay.qrPay(orderId+"", total+"", "停车费-收费员("+uid+")车牌("+car_number+")时间("+time+")", uin+"_7_"+uid, uid);
				ret= "{\"state\":\"1\",\"errmsg\":\"\",\"qrcode\":\""+qr+"\"}";
			}else {
				ret= "{\"state\":\"0\",\"errmsg\":\"订单不存在\"}";
			}
		}
		logger.error("zfbPayQr-->"+ret);
		return ret;
	}

	private String closeCard(HttpServletRequest request, Long uid, Long groupId){
		try {
			String nfc_uuid = RequestUtil.processParams(request, "uuid");
			Integer version = RequestUtil.getInteger(request, "version", -1);
			logger.error("nfc_uuid:"+nfc_uuid+",version:"+version);
			UnbindCardReq req = new UnbindCardReq();
			req.setGroupId(groupId);
			req.setNfc_uuid(nfc_uuid);
			req.setUnBinder(uid);
			BaseResp resp = cardService.returnCard(req);
			if(resp.getResult() == 1){
				return "{\"result\":\"1\",\"errmsg\":\""+resp.getErrmsg()+"\"}";
			}else{
				return "{\"result\":\"-1\",\"errmsg\":\""+resp.getErrmsg()+"\"}";
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return "{\"result\":\"-1\",\"errmsg\":\"系统错误\"}";
	}
	
	private String chargeCard(HttpServletRequest request, Long uid, Long groupId, Long comid){
		try {
			String nfc_uuid = RequestUtil.processParams(request, "uuid");
			Integer version = RequestUtil.getInteger(request, "version", -1);
			Double money = RequestUtil.getDouble(request, "money", 0d);
			logger.error("nfc_uuid:"+nfc_uuid+",version:"+version+",money:"+money);
			CardChargeReq req = new CardChargeReq();
			req.setCashierId(uid);
			req.setChargeType(0);
			req.setGroupId(groupId);
			req.setMoney(money);
			req.setNfc_uuid(nfc_uuid);
			req.setParkId(comid);
			
			BaseResp resp = cardService.cardCharge(req);
			if(resp.getResult() == 1){
				return "{\"result\":\"1\",\"errmsg\":\""+resp.getErrmsg()+"\"}";
			}else{
				return "{\"result\":\"-1\",\"errmsg\":\""+resp.getErrmsg()+"\"}";
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return "{\"result\":\"-1\",\"errmsg\":\"系统错误\"}";
	}
	
	private String cardInfo(HttpServletRequest request, Long uid, Long groupId){
		try {
			String nfc_uuid = RequestUtil.processParams(request, "uuid");
			Integer version = RequestUtil.getInteger(request, "version", -1);
			logger.error("nfc_uuid:"+nfc_uuid+",version:"+version);
			CardInfoReq req = new CardInfoReq();
			req.setGroupId(groupId);
			req.setNfc_uuid(nfc_uuid);
			CardInfoResp resp = cardService.getCardInfo(req);
			JSONObject object = JSONObject.fromObject(resp);
			logger.error(object.toString());
			return object.toString();
		} catch (Exception e) {
			logger.error(e);
		}
		return "{\"result\":\"-1\",\"errmsg\":\"系统错误\"}";
	}
	
	private String actCard(HttpServletRequest request, Long uid, Long groupId, Long comid){
		try {
			String nfc_uuid = RequestUtil.processParams(request, "uuid");
			ActCardReq req = new ActCardReq();
			req.setUid(uid);
			req.setNfc_uuid(nfc_uuid);
			req.setGroupId(groupId);
			req.setParkId(comid);
			BaseResp resp = cardService.actCard(req);
			if(resp.getResult() == 1){
				return "{\"result\":\"1\",\"errmsg\":\""+resp.getErrmsg()+"\"}";
			}else{
				return "{\"result\":\"0\",\"errmsg\":\""+resp.getErrmsg()+"\"}";
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return "{\"result\":\"0\",\"errmsg\":\"系统错误\"}";
	}
	
	private String bindCard(HttpServletRequest request, Long uid, Long groupId, Long comid){
		try {
			String nfc_uuid = RequestUtil.processParams(request, "uuid");
			String mobile = RequestUtil.processParams(request, "mobile");
			String carNumber = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "carnumber")).toUpperCase();
			BindCardReq req = new BindCardReq();
			req.setBinder(uid);
			req.setNfc_uuid(nfc_uuid);
			req.setMobile(mobile);
			req.setCarNumber(carNumber);
			req.setGroupId(groupId);
			req.setParkId(comid);
			BaseResp resp = null;
			if(mobile == null || "".equals(mobile)){
				resp = cardService.bindPlateCard(req);
			}else{
				resp = cardService.bindUserCard(req);
			}
			if(resp.getResult() == 1){
				return "{\"result\":\"1\",\"errmsg\":\""+resp.getErrmsg()+"\"}";
			}else{
				return "{\"result\":\"0\",\"errmsg\":\""+resp.getErrmsg()+"\"}";
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return "{\"result\":\"0\",\"errmsg\":\"系统错误\"}";
	}
	
	/**
	 * 签退密码验证
	 * @param request
	 * @param uin
	 * @return
	 */
	private int signOutValid(HttpServletRequest request, Long uin){
		try {
			String collpwd = RequestUtil.processParams(request, "collpwd");
			Integer version = RequestUtil.getInteger(request, "version", -1);//版本号
			logger.error("validate>>>uid:"+uin+",collpwd:"+collpwd+",version:"+version);
			if(version > 1340){//客户端v1.3.40以后有次功能
				Map<String, Object> setMap = pService.getMap("select s.signout_valid," +
						"s.signout_password from collector_set_tb s,user_info_tb u " +
						"where s.role_id=u.role_id and u.id=? ", new Object[]{uin});
				logger.error("validate>>>uid:"+uin+",setMap:"+setMap);
				if(setMap != null){
					Integer signout_valid = (Integer)setMap.get("signout_valid");
					logger.error("signout_valid:"+signout_valid);
					if(signout_valid == 1 && setMap.get("signout_password") != null){
						String password = (String)setMap.get("signout_password");
						logger.error("validate>>>uid:"+uin+",signout_password:"+password);
						if(!password.equals("")){
							if(!collpwd.equals(password)){//校验密码
								return -1;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("validate>>>uin:"+uin, e);
		}
		return 1;
	}
	
	/**
	 * 查看收费汇总密码验证
	 * @param request
	 * @param uin
	 * @return
	 */
	private int validate(HttpServletRequest request, Long uin){
		try {
			String collpwd = RequestUtil.processParams(request, "collpwd");
			logger.error("validate>>>uid:"+uin+",collpwd:"+collpwd);
			Map<String, Object> setMap = pService.getMap("select hidedetail,s.password from collector_set_tb s,user_info_tb u " +
					" where s.role_id=u.role_id and u.id=? ", new Object[]{uin});
			logger.error("validate>>>uid:"+uin+",setMap:"+setMap);
			if(setMap != null){
				Integer hidedetail = (Integer)setMap.get("hidedetail");
				logger.error("hidedetail:"+hidedetail);
				if(hidedetail == 1 && setMap.get("password") != null){//隐藏收费汇总
					String password = (String)setMap.get("password");
					logger.error("validate>>>uid:"+uin+",password:"+password);
					if(!password.equals("")){
						if(!collpwd.equals(password)){//校验密码
							return -1;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("validate>>>uin:"+uin, e);
		}
		return 1;
	}
	
	private String autoPayPosOrder(HttpServletRequest request, Long uid, Long groupId){
		try {
			//----------------------------参数--------------------------------//
			Long orderId = RequestUtil.getLong(request, "orderid", -1L);//订单编号 
			Double money = RequestUtil.getDouble(request, "total", 0d);//总金额
			String imei  =  RequestUtil.getString(request, "imei");//手机串号
			Integer version = RequestUtil.getInteger(request, "version", -1);//版本号
			Long endtime = RequestUtil.getLong(request, "endtime", -1L);
			logger.error("orderid:"+orderId+",money:"+money+",imei:"+imei+
					",version:"+version+",endtime:"+endtime);
			//----------------------------废弃参数--------------------------------//
			//Integer isMonthUser = RequestUtil.getInteger(request, "ismonthuser",0);
			//是否是月卡用户,5代表月卡用户，从collectorrequest.do?action=getberths获取的该值
			//----------------------------校验参数--------------------------------//
			AutoPayPosOrderFacadeReq req = new AutoPayPosOrderFacadeReq();
			req.setOrderId(orderId);
			req.setMoney(money);
			req.setImei(imei);
			req.setUid(uid);
			req.setVersion(version);
			req.setGroupId(groupId);
			req.setEndTime(endtime);
			AutoPayPosOrderResp resp = payOrderFacade.autoPayPosOrder(req);
			logger.error(resp.toString());
			if(resp != null){
				JSONObject object = JSONObject.fromObject(resp);
				logger.error(object.toString());
				return object.toString();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return "{\"result\":\"0\",\"errmsg\":\"结算失败\"}";
	}
	
	/*pos机支付，处理余额及月卡结算*************/
	private String payPosOrder(HttpServletRequest request, Long uin, Long groupId) {
		Integer version = RequestUtil.getInteger(request, "version", -1);
		logger.error("version:"+version);
		if(version > 1340){
			return autoPayPosOrder(request, uin, groupId);
		}
		//先判断月卡用户
		Integer isMonthUser = RequestUtil.getInteger(request, "ismonthuser",0);//是否是月卡用户
		if(isMonthUser==5){
			return payMonthOrder(request, uin);
		}
		Long orderId = RequestUtil.getLong(request, "orderid", -1L);//订单编号 
		//Long brethOrderId = RequestUtil.getLong(request, "brethorderid", -1L);//地磁订单编号
		Double money = RequestUtil.getDouble(request, "total", 0d);//总金额
		if(money==0){//预收金额可以支付时，返回走现金支付流程
			return "{\"result\":\"3\",\"errmsg\":\"金额0元不需要支付\"}";
		}
		Integer workid = RequestUtil.getInteger(request, "workid",0);//工作班号
		String imei  =  RequestUtil.getString(request, "imei");//手机串号
		Map orderMap = daService.getMap("select * from order_tb where id =? ", 
				new Object[]{orderId});
		Long ntime = System.currentTimeMillis()/1000;
		Long brethOrderId = commonMethods.getBerthOrderId(orderId);//地磁订单编号
		Long end_time = commonMethods.getOrderEndTime(brethOrderId, uin, ntime);
		if(orderMap!=null&&!orderMap.isEmpty()){
			Double prePay = StringUtils.formatDouble(orderMap.get("prepaid"));//预收金额
			if(prePay>=money&&money>0){//预收金额可以支付时，返回走现金支付流程
				return "{\"result\":\"3\",\"errmsg\":\"预收金额可以支付\"}";
			}else if(prePay>0){
				money = money-prePay;//只收不足的部分金额，支付成功后，再把订单金额修改为实际金额
			}
			Long btime = (Long)orderMap.get("create_time");
			String duration = StringUtils.getTimeString(btime, end_time);
			Long user = (Long)orderMap.get("uin");
			Long comId = (Long)orderMap.get("comid");
			Integer is_auth =0;
			String carNumber = (String)orderMap.get("car_number");
			if(user==null||user<1){
				if(carNumber!=null&&!"".equals(carNumber)){
					Map carMap = daService.getMap("select uin,is_auth from car_info_tb where car_number=? and state=? ",
							new Object[]{carNumber,1});
					if(carMap!=null&&carMap.get("uin")!=null){
						user = (Long)carMap.get("uin");
						is_auth=(Integer)carMap.get("is_auth");
					}
				}
			}
			if(user>0){//找到了车主
				//查用户余额
				Double balance =0d;
				Map userMap = daService.getMap("select balance,wxp_openid,is_auth,credit_limit from user_info_tb where id=?",new Object[]{user});
				if(userMap!=null){
					balance=StringUtils.formatDouble(userMap.get("balance"));
					if(balance==0)
						return "{\"result\":\"-2\",\"errmsg\":\"车主余额不足\"}";
				}else {
					return "{\"result\":\"-3\",\"errmsg\":\"车主未注册\"}";
				}
				//查车主配置，是否设置了自动支付。没有配置时，默认25元以下自动支付 
				Integer autoCash=1;
				Map upMap = daService.getPojo("select auto_cash,limit_money from user_profile_tb where uin =?", new Object[]{user});
				Integer limitMoney =25;
				if(upMap!=null&&upMap.get("auto_cash")!=null){
					autoCash= (Integer)upMap.get("auto_cash");
					limitMoney = (Integer)upMap.get("limit_money");
				}
				if(autoCash!=null&&autoCash==1){//设置了自动支付
					boolean isupmoney=true;//是否可超过自动支付限额
					if(limitMoney!=null){
						if(limitMoney==-1||limitMoney>=money)//如果是不限或大于支付金额，可自动支付 
							isupmoney=false;
					}
					if(isupmoney){//超过自动支付限额
						return "{\"result\":\"-1\",\"errmsg\":\"超出自动支付限额："+limitMoney+"元\"}";
					}
					//车牌是否认证通过，信用额度是否用完，余额不足时，可用额度抵扣
					Double creditLimit=0.0;//车主信用额度充值金额，支付失败时，要反充值
					if((balance)<money){//余额不足时，查车牌是否认证通过
						creditLimit = money-(balance);
						if(is_auth==1){//已认证的车牌，查车主是否还有可用信用额度
							is_auth = (Integer)userMap.get("is_auth");
							Double climit = StringUtils.formatDouble(userMap.get("credit_limit"));
							if(is_auth==1&&climit>=creditLimit){
								int ret = daService.update("update user_info_tb set balance=balance+?,credit_limit=credit_limit-? where id =? ", 
										new Object[]{creditLimit,creditLimit,user});
								logger.error(">>>>>>>auto pay ,车主信用额度抵扣："+creditLimit+",ret："+ret);
								if(ret==1){//信用额度充值成功
									balance = money;
								}else {
									creditLimit=0.0;
								}
							}
						}
					}
					if((balance)>=money){//余额可以支付
						//int re = publicMethods.payOrder(orderMap, money, user, 2,0,-1L,null);//公共支付方法
						int re = publicMethods.payOrder(orderMap, money, user, 2,0,-1L,null, brethOrderId, uin);//公共支付方法
						if(re==5){//成功支付
							if(prePay>0){
								money = (money+prePay);
								int ret = daService.update("update order_tb set total=? where id =? ", new Object[]{money,orderId});
								logger.error("payposorder>>>有预收金额，实际支付金额："+(money-prePay)+",预收金额："+prePay+",订单金额："+money+",ret:"+ret);
							}
							//修改泊位状态
							Long berthnumber=(Long)orderMap.get("berthnumber");
							if(berthnumber!=null&&berthnumber>0){//根据泊位号更新泊位状态
								int ret =daService.update("update com_park_tb set state=?,order_id=? where id =? and order_id=?",  new Object[]{0,null,berthnumber,orderId});
								logger.error("payposorder 不是逃单更新泊位：ret :"+ret+",berthnumber："+berthnumber+",orderid:"+orderId);
							}
							//修改地磁订单
							if(brethOrderId>0){
								int r = daService.update("update berth_order_tb set out_uid=?,order_total=?  where id=? ", new Object[]{uin,money,brethOrderId});
								logger.error("payposorder 修改地磁订单：ret :"+r);
							}
							//工作记录
							if(workid>0){//有工作班号时，查一下此订单是不是在上班期间产生的，如果不是，要把此前预付金额加入工作班次表，签退时扣除这部分，并把订单编号写入工作班次表
								Long count = daService.getLong("select count(ID) from work_detail_tb where workid=? and orderid=? ",
										new Object[]{workid,orderId});
								if(count<1){//订单不是上班期间产生的
									int ret = 0;
									if(prePay>0){
										ret = daService.update("update parkuser_work_record_tb set history_money = history_money+? where id =? ",
												new Object[]{prePay,workid});
										logger.error("payposorder pos机自动结算，不是在本班次产生的订单，预收金额："+prePay+"写入班次表："+ret);
									}
									if(money>0){
										ret = daService.update("insert into work_detail_tb (uid,orderid,bid,workid,berthsec_id) values(?,?,?,?,?)", 
												new Object[]{uin,orderId,berthnumber,workid,orderMap.get("berthsec_id")});
										logger.error("payposorder pos机自动结算，不是在本班次产生的订单，订单编号："+orderId+"，班次号："+workid+",写入班次详情表："+ret);
									}
								}
							}
							logger.error("payposorder : success,发消息给车主及车场收费员....");
							logService.insertMessage(comId, 2, user,carNumber, orderId, (money-prePay), duration,0, btime, end_time, 0);
							//logService.insertParkUserMessage(comId, 2, uid, carNumber, orderId, money,duration,0, btime, etime,0);
							return "{\"result\":\"2\",\"errmsg\":\"预收金额："+prePay+"元，余额支付："+(money-prePay)+"元\",\"duration\":\""+duration+"\"}";
						}else {
							if(re!=5&&creditLimit>0){//支付失败时， 信用额度要反充值
								int ret = daService.update("update user_info_tb set balance=balance-?,credit_limit=credit_limit+? where id =? ", 
										new Object[]{creditLimit,creditLimit,user});
								logger.error("payposorder>>>>>>>auto pay ,车主信用额度抵扣支付失败， 信用额度反充值，金额："+creditLimit+",ret："+ret);
							}
							return "{\"result\":\"-4\",\"errmsg\":\"车主余额支付失败\"}";
						}
					}else {
						return "{\"result\":\"-2\",\"errmsg\":\"车主余额不足\"}";
					}
				}
			}else {
				return "{\"result\":\"0\",\"errmsg\":\"结算失败\"}";
			}
		}
		return "{\"result\":\"0\",\"errmsg\":\"结算失败，订单不存在 \"}";
	}
	//月卡支付
	private String payMonthOrder(HttpServletRequest request,Long uin) {
		Long ntime = System.currentTimeMillis()/1000;
		Long orderId = RequestUtil.getLong(request, "orderid", -1L);
		//Long brethOrderId = RequestUtil.getLong(request, "brethorderid", -1L);//地磁订单编号
		Long brethOrderId = commonMethods.getBerthOrderId(orderId);//地磁订单编号
		String imei  =  RequestUtil.getString(request, "imei");
		String result = "";
		Map<String, Object> orderMap = daService.getPojo("select * from order_tb where id=?", 
				new Object[]{ orderId });
		Integer state = (Integer)orderMap.get("state");
		if(state != null && state == 1){//已结算，返回
			return "{\"result\":\"-2\",\"errmsg\":\"订单已结算!\"}";
		}
		Long create_time = (Long)orderMap.get("create_time");
		Long end_time = commonMethods.getOrderEndTime(brethOrderId, uin, ntime);
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		//更新订单状态
		Map<String, Object> orderSqlMap = new HashMap<String, Object>();
		orderSqlMap.put("sql", "update order_tb set state=?,total=?,end_time=?,pay_type=?,imei=?,out_uid=? where id=?");
		orderSqlMap.put("values", new Object[]{1, 0.0, end_time, 3, imei, uin, orderId});
		bathSql.add(orderSqlMap);
		Long berthId = -1L;
		if(orderMap.get("berthnumber") != null){
			berthId = (Long)orderMap.get("berthnumber");
		}
		if(berthId != null && berthId > 0){
			//更新泊位状态
			Map<String, Object> berthSqlMap = new HashMap<String, Object>();
			berthSqlMap.put("sql", "update com_park_tb set state=?,order_id=? where id =? and order_id=?");
			berthSqlMap.put("values", new Object[]{0, null, berthId, orderId});
			bathSql.add(berthSqlMap);
		}
		if(brethOrderId != null && brethOrderId > 0){
			//更新车检器订单状态
			Map<String, Object> berthOrderSqlMap = new HashMap<String, Object>();
			berthOrderSqlMap.put("sql", "update berth_order_tb set out_uid=?,order_total=?  where id=? ");
			berthOrderSqlMap.put("values", new Object[]{uin, 0.0, brethOrderId});
			bathSql.add(berthOrderSqlMap);
		}
		boolean b = daService.bathUpdate2(bathSql);
		logger.error("payMonthOrder 更新地磁订单表：b :" + b + ",(update com_park_tb orderid):" + orderId + ",berthid:" + berthId + ",brethOrderid:" + brethOrderId);
		if(b){
			String duration = StringUtils.getTimeString(create_time, end_time);
			result ="{\"result\":\"1\",\"errmsg\":\"月卡结算成功\",\"duration\":\""+duration+"\"}";
		}else {
			result ="{\"result\":\"0\",\"errmsg\":\"结算失败，订单不存在 \"}";
		}
		logger.error("payMonthOrder result:"+result);
		return result;
	}
	private String getPayDetail(HttpServletRequest request) {
		Long workId = RequestUtil.getLong(request, "workid", -1L);
		Map workMap  = daService.getMap("select history_money,start_time from parkuser_work_record_tb where id=? ", new Object[]{workId});
		//上一岗位上预收金额
		Double historyPrePay = 0.0;
		if(workMap!=null&&!workMap.isEmpty()){
			historyPrePay = StringUtils.formatDouble(workMap.get("history_money"));
			logger.error("paydetail,垫付："+historyPrePay);
		}
			
		List<Map<String, Object>> orderList = daService.getAll("select total,state,prepaid,pay_type from order_tb" +
				" where id in(select orderid from work_detail_tb where workid=? )", new Object[]{workId});
		Double cash =0.0;
		Double card =0.0;
		Double etc =0.0;
		//Double prepay =0.0;
		logger.error("paydetail,收费详细："+orderList);
		if(orderList!=null&&!orderList.isEmpty()){
			for(Map<String, Object> map : orderList){
				Integer state  = (Integer)map.get("state");
				if(state!=null&&state==1){
					Integer payType =(Integer)map.get("pay_type");
					if(payType==1){
						cash +=StringUtils.formatDouble(map.get("total"));
					}else if(payType==2){
						etc +=StringUtils.formatDouble(map.get("total"))-StringUtils.formatDouble(map.get("prepaid"));
						cash +=StringUtils.formatDouble(map.get("prepaid"));
					}else {
						card +=StringUtils.formatDouble(map.get("total"));
					}
				}else {
					cash +=StringUtils.formatDouble(map.get("prepaid"));
				}
			}
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("cashpay",StringUtils.formatDouble(cash-historyPrePay));
		resultMap.put("onlinepay", StringUtils.formatDouble(etc));
		resultMap.put("cardpay", StringUtils.formatDouble(card));
		logger.error("paydetail,返回："+resultMap);
		return StringUtils.createJson(resultMap);
	}
	/**
	 * 注册POS机
	 * @param request
	 * @param comid
	 * @param uid
	 * @return
	 */
	private String regPosSequecce(HttpServletRequest request,Long comid,Long uid) {
		Long ntime = System.currentTimeMillis()/1000;
		String device_code = RequestUtil.getString(request, "device_code");
		Long count = daService.getLong("select count(Id) from mobile_tb where device_code=?  ", new Object[]{device_code});
		String result = "";
		if(count>0){//设备已登录
			result = "{\"result\":\"-1\",\"errmst\":\"设备已注册过\"}";
		}else {
			Long time = ntime;
			String sql = "insert into  mobile_tb (comid,uid,mode,create_time,device_code) values" +
					"(?,?,?,?,?)";
			Object [] values = new Object[]{comid,uid,"POS机",time,device_code};
			int ret = daService.update(sql, values);
			if(ret==1)
				result = "{\"result\":\"1\",\"errmst\":\"设备注册成功\"}";
			else
				result = "{\"result\":\"0\",\"errmst\":\"设备注册失败\"}";
		}
		return result;
	}

	private void getOrderPic(HttpServletRequest request,HttpServletResponse response) {
		Long orderId = RequestUtil.getLong(request, "orderid", -1L);
		Integer type = RequestUtil.getInteger(request, "type", 0);
		BasicDBObject conditions = new BasicDBObject();
		conditions.put("orderid", orderId);
		conditions.put("type", type);
		byte[] pic = mongoDbUtils.getPictures("carstop_pics", conditions);
		if(pic!=null){
			response.setDateHeader("Expires", System.currentTimeMillis()+4*60*60*1000);
			//response.setStatus(httpc);
			Calendar c = Calendar.getInstance();
			c.set(1970, 1, 1, 1, 1, 1);
			response.setHeader("Last-Modified", c.getTime().toString());
			response.setContentLength(pic.length);
			response.setContentType("image/jpeg");
			System.err.println(pic.length);
			try {
				OutputStream o = response.getOutputStream();
				o.write(pic);
				o.flush();
				o.close();
				response.flushBuffer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			try {
				response.sendRedirect("http://sysimages.tq.cn/images/webchat_101001/common/kefu.png");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String payEscapeOrder(HttpServletRequest request, Long uid, Long groupId, Long comid){
		try {
			String ids = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "orderlist"));
			Integer version = RequestUtil.getInteger(request, "version", -1);//版本号
			String nfc_uuid = RequestUtil.processParams(request, "uuid");//刷卡支付的卡片编号
			Integer bindcard = RequestUtil.getInteger(request, "bindcard", 0);//0:客户端弹出绑定车主手机号的提示框 1：不弹出提示框直接刷卡预付
			Integer payType = RequestUtil.getInteger(request, "paytype", 0);//支付方式 0：现金支付， 1：刷卡支付
			Long bid = RequestUtil.getLong(request, "bid", -1L);//泊位编号(2016-10-14添加，为了记录在哪个泊位上追缴的订单)
			//---------------------------废弃的参数----------------------------//
			Integer workid = RequestUtil.getInteger(request, "workid",0);//工作班号
			logger.error("ids:"+ids+",workid:"+workid+",version:"+version+
					",nfc_uuid:"+nfc_uuid+",bindcard:"+bindcard+",bid:"+bid);
			
			List<Map<String, Object>> list =ParseJson.jsonToList(ids);
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					Long orderId = Long.valueOf(map.get("orderid") + "");
					Double total = StringUtils.formatDouble(map.get("total"));
					if(orderId != null && orderId > 0){
						PayEscapePosOrderFacadeReq req = new PayEscapePosOrderFacadeReq();
						req.setOrderId(orderId);
						req.setBindcard(bindcard);
						req.setMoney(total);
						req.setNfc_uuid(nfc_uuid);
						req.setPayType(payType);
						req.setUid(uid);
						req.setVersion(version);
						req.setGroupId(groupId);
						req.setParkId(comid);
						req.setBerthId(bid);
						PayEscapePosOrderResp resp = payOrderFacade.payEscapePosOrder(req);
						if(resp.getResult() != 1){
							return "{\"result\":\""+resp.getResult()+"\",\"errmsg\":\""+resp.getErrmsg()+"\"}";
						}
					}
				}
				return "{\"result\":\"1\",\"errmsg\":\"结算成功\"}";
			}
			
		} catch (Exception e) {
			logger.error(e);
		}
		return "{\"result\":\"0\",\"errmsg\":\"系统错误\"}";
	}

	private String payEscOrder(HttpServletRequest request, Long uin, Long groupId, Long comid) {
		String result = "";
		String ids = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "orderlist"));
		Integer workid = RequestUtil.getInteger(request, "workid",0);//工作班号
		Integer version = RequestUtil.getInteger(request, "version", -1);//版本号
		logger.error("payOrder:"+ids+",version:"+version);
		if(version > 1340){//
			return payEscapeOrder(request, uin, groupId, comid);
		}
		// [{"car_number":"沪F55555","duartion":"2分钟","end":"1461206247","total":"1.00","orderid":"841808","prepay":"8.0","start":"1461206101","ischeck":true}]
		List<Map<String, Object>> array =ParseJson.jsonToList(ids);
		boolean re = true;
		String errmsg="";
		if(array!=null&&!array.isEmpty()){
			for(Map<String, Object> map : array){
				Long orderId =Long.valueOf(map.get("orderid")+"");
				Double total = StringUtils.formatDouble(map.get("total"));
				if(orderId!=null&&orderId>0){
					String ret = payOrder(orderId, total, uin, workid, "json", "", 0,1,-1L,-1L);
					logger.error("payOrder  payEscOrder  结算订单:"+map+",ret:"+ret);
					if(!"1".equals(ret)){
						re = false;
						if(errmsg.length()>0)
							errmsg +=";";
						Long start = Long.valueOf(map.get("start")+"");
						Long end = Long.valueOf(map.get("end")+"");
						errmsg+=TimeTools.getTime_yyyyMMdd_HHmm(start*1000)+"-"+TimeTools.getTime_yyyyMMdd_HHmm(end*1000)+"的订单结算失败";
					}
				}
			}
		}
		if(re){
			result= "{\"result\":\"1\",\"errmsg\":\"结算成功\"}";
		}else {
			result= "{\"result\":\"0\",\"errmsg\":\""+errmsg+"\"}";
		}
		return result;
	}
	
	
	private String queryEscOrder(HttpServletRequest request, Long uin, Long comId, Long groupId) {
		try {
			Long berthSgeId = RequestUtil.getLong(request, "berthid",-1L);
			String carNumber = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "car_number")).toUpperCase().trim();
			Integer version = RequestUtil.getInteger(request, "version", -1);//版本号
			
			logger.error("berthSgeId:"+berthSgeId+",carNumber:"+carNumber+",version:"+version+",groupid:"+groupId);
			String ret = "1";
			String errmsg = "";
			String orders = "[]";
			if(groupId != null && groupId > 0){
				List<Object> params = new ArrayList<Object>();
				boolean isPursue = commonMethods.pursueInCity(groupId);
				logger.error("isPursue:"+isPursue);
				List<Object> comList = null;
				if(isPursue){
					Group group = pService.getPOJO("select cityid from org_group_tb where id=? and cityid>? ",
							new Object[]{groupId, 0}, Group.class);
					comList = commonMethods.getparks(group.getCityid());
				} else {
					comList = commonMethods.getParks(groupId);
				}
				if(comList != null && !comList.isEmpty()){
					String preParams = "";
					for(Object parkid : comList){
						if(preParams.equals(""))
							preParams ="?";
						else
							preParams += ",?";
					}
					params.addAll(comList);
					params.add(0);
					params.add(carNumber);
					List<Map<String, Object>> escList = pService.getAllMap("select order_id as orderid,create_time as start," +
							"end_time as end,car_number,total,prepay,berthseg_id as berthsec_id from no_payment_tb where comid " +
							" in ("+preParams+") and state=? and car_number=? ", params);
					if(escList != null && !escList.isEmpty()){
						for(Map<String, Object>map : escList){
							Long in_time = (Long)map.get("start");
							Long out_time = (Long)map.get("end");
							map.put("prepay", StringUtils.formatDouble(map.get("prepay")));
							map.put("duartion", StringUtils.getTimeStringSenconds(in_time, out_time));
							map.put("in_time", TimeTools.getTime_yyyyMMdd_HHmmss(in_time));
							map.put("out_time", TimeTools.getTime_yyyyMMdd_HHmmss(out_time));
							map.put("total", map.get("total"));
						}
						getBerthSegInfo(escList);
						putPicUrls(escList);
						orders = StringUtils.createJson(escList);
						ret="0";
						errmsg="";
					}
					
					if(version >= 1390){//2016-12-14日添加
						boolean isInpark = commonMethods.isInparkInCity(groupId);
						logger.error("isInpark是否同一车牌可否在城市内重复入场:"+isInpark);
						if(!isInpark){//不可以入场，时，要查城市内所有车场
							Group group = pService.getPOJO("select cityid from org_group_tb where id=? and cityid>? ",
									new Object[]{groupId, 0}, Group.class);
							comList = commonMethods.getparks(group.getCityid());
						} else {
							comList = commonMethods.getParks(groupId);
						}
						preParams  = "";
						for(Object parkid : comList){
							if(preParams.equals(""))
								preParams ="?";
							else
								preParams += ",?";
						}
						params.clear();
						params.add(carNumber);
						params.add(0);
						params.add(0);
						params.addAll(comList);
						params.add(comId);
						params.add(1);
						Order order = daService.getPOJO("select comid,berthsec_id from order_tb where car_number=? and state=? and ishd=? " +
								" and comid in ("+preParams+") and comid <>? limit ? ", params, Order.class);
						if(order != null){
							errmsg = "该车辆在";
							Long parkId = order.getComid();
							Long berthSegId = order.getBerthsec_id();
							Map<String, Object> map = pService.getMap("select company_name from com_info_tb where id=? ",
									new Object[]{parkId});
							if(map != null && map.get("company_name") != null){
								errmsg += "停车场：" + map.get("company_name");
							}
							if(berthSegId > 0){
								BerthSeg berthSeg = pService.getPOJO("select berthsec_name from com_berthsecs_tb where id=? ",
										new Object[]{berthSegId}, BerthSeg.class);
								if(berthSeg != null){
									errmsg += "，泊位段："+berthSeg.getBerthsec_name();
								}
							}
							errmsg += "有未结算订单！";
							Long count = pService.getLong("select count(s.id) from collector_set_tb s,user_info_tb u where " +
									" s.role_id=u.role_id and s.is_duplicate_order=? and u.id=? ", new Object[]{0, uin});
							logger.error("count:"+count);
							if(count > 0||!isInpark){
								ret = "-3";//同一辆车在不同车场不能重复生成在场订单
							} else {
								ret = "-2";
							}
						}
					}
					Long count = daService.getLong("select count(id) from order_tb where car_number=? and state=? and ishd=? " +
							" and comid=? ", new Object[]{carNumber, 0, 0, comId});
					if(count > 0){
						ret = "-1";
						errmsg = "车辆已入场！";
					}
				}
				int ismonth = 0;
				if(commonMethods.isMonthUser(carNumber, comId))
					ismonth = 1;
				Long orderId = -1L;
				if(!ret.equals("-1")){
					orderId = daService.getkey("seq_order_tb");
				}
				String result = "{\"result\":\""+ret+"\",\"errmsg\":\""+errmsg+"\",\"orderid\":\""+orderId+"\",\"ismonthuser\":\""+ismonth+"\",\"orders\":"+orders+"}"; 
				logger.error("queryEscOrder result :"+result);
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "{\"result\":\"-1\",\"errmsg\":\"系统错误!\"}";
	}
	
	private void getBerthSegInfo(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				Long berthsec_id = (Long)map.get("berthsec_id");
				if(berthsec_id != null && berthsec_id > 0){
					BerthSeg berthSeg = pService.getPOJO("select berthsec_name from com_berthsecs_tb where id =?",
							new Object[]{berthsec_id}, BerthSeg.class);
					if(berthSeg != null && berthSeg.getBerthsec_name() != null){
						map.put("berthsec_name", berthSeg.getBerthsec_name());
					}
				}
			}
		}
	}
	
	private void putPicUrls(List<Map<String, Object>> list){
		try {
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					if(map.get("orderid") != null){
						Long orderId = (Long)map.get("orderid");
						BasicDBObject conditions = new BasicDBObject();
						conditions.put("orderid", orderId);
						conditions.put("type", 2);
						List<String> urls = mongoDbUtils.getPicUrls("car_pics", conditions);
						if(urls != null && !urls.isEmpty()){
							String urlStr = "";
							for(String url : urls){
								url = "carpicsup.do?action=getpicbyname&filename="+url;
								if("".equals(urlStr)){
									urlStr += url;
								}else{
									urlStr += "," + url;
								}
							}
							map.put("picurls", urlStr);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	private String uporderpic(HttpServletRequest request)throws Exception {
		Long id = RequestUtil.getLong(request, "orderid", -1L);
		Integer type = RequestUtil.getInteger(request, "type", 0);//0入场照片，1出场照片 2追缴
		logger.error(">>>>上传车辆泊车点照片....orderid:"+id+",type:"+type);
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("orderid", id);
		paramsMap.put("type", type);
		paramsMap.put("ctime", TimeTools.gettime1());
		String picurl  =publicMethods.uploadPic(request, paramsMap, "car_hd_pics");
		logger.error(">>>>上传车辆泊车点照片....图片名称："+picurl+",mongodb table:car_hd_pics");
		int ret =0;
		if(!"-1".equals(picurl)){
			ret=1;
		}
		return "{\"result\":\""+ret+"\"}";
	}
	private String workOut(HttpServletRequest request,Long uid) {
		try {
			Long ntime = System.currentTimeMillis()/1000;
			Long bid = RequestUtil.getLong(request, "berthid", -1L);
			Long workId = RequestUtil.getLong(request, "workid", -1L);
			Integer from = RequestUtil.getInteger(request, "from", 0);//来源 0：签退 1：查看收费汇总
			Integer version = RequestUtil.getInteger(request, "version", -1);//版本号
			logger.error("workout 退出泊位段:"+bid+",workid:"+workId+",from:"+from+",version:"+version);
			if(from == 0){
				int result = signOutValid(request, uid);
				if(result < 0){
					return  "{\"result\":\"-2\",\"errmsg\":\"密码错误！\"}";
				}
				List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
				//更新工作组中的指定的泊位段已签退
				Map<String, Object> workBerthSegSqlMap = new HashMap<String, Object>();
				workBerthSegSqlMap.put("sql", "update work_berthsec_tb set state=? where berthsec_id=? and is_delete =? ");
				workBerthSegSqlMap.put("values", new Object[]{0, bid, 0});
				bathSql.add(workBerthSegSqlMap);
				//签退操作
				
				boolean off = commonMethods.checkWorkTime(uid, ntime);
				int logoff_state = 0;
				if(!off){
					logoff_state = 1;
				}
				logger.error("logoff_state:"+logoff_state);
				Map<String, Object> workRecordSqlMap = new HashMap<String, Object>();
				workRecordSqlMap.put("sql", "update parkuser_work_record_tb set end_time=?,state=?,logoff_state=? where id =? ");
				workRecordSqlMap.put("values", new Object[]{ntime, 1, logoff_state, workId});
				bathSql.add(workRecordSqlMap);
				//标为离线
				Map<String, Object> onlineSqlMap = new HashMap<String, Object>();
				onlineSqlMap.put("sql", "update user_info_tb set online_flag=? where id=? ");
				onlineSqlMap.put("values", new Object[]{21, uid});
				bathSql.add(onlineSqlMap);
				boolean b = daService.bathUpdate2(bathSql);
				logger.error("workout 退出泊位段:"+bid+",workid:"+workId+",from:"+from+",b:"+b);
			}else if(from == 1){
				int result = validate(request, uid);
				if(result < 0){
					return  "{\"result\":\"-2\",\"errmsg\":\"密码错误！\"}";
				}
			}
			String result = "";
			if(version >= 1370){
				StatsWorkRecordResp resp = getIncome(uid, workId, ntime, from);
				result = JSONObject.fromObject(resp).toString();
			}else{//老版本
				Map<String, Object> infoMap = getIncomeOld(uid, workId, ntime);
				result = StringUtils.createJson(infoMap);
			}
			logger.error("work out result:"+result);
			return result;
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> getIncomeOld(Long uid, Long workId, Long curTime){
		Map<String, Object> infoMap = new HashMap<String, Object>();
		try {
			infoMap.put("result",1);
			//打印签退小票
			//上岗时，在场车场的预收金额
			Map<String, Object> workMap = daService.getMap("select history_money,start_time from" +
					" parkuser_work_record_tb where id=? ", new Object[]{workId});
			Long outcar = 0L;
			Long incar = 0L;
			Map<String, Object> inMap = daService.getMap("select count(id) ocount from order_tb" +
					" where id in (select orderid from work_detail_tb where workid=? ) and uid=? ", new Object[]{workId, uid});
			if(inMap != null && inMap.get("ocount") != null){
				incar = (Long)inMap.get("ocount");
			}
			Map<String, Object> outMap = daService.getMap("select count(id) ocount from order_tb" +
					" where id in (select orderid from work_detail_tb where workid=? ) and out_uid=? ", new Object[]{workId, uid});
			if(outMap != null && outMap.get("ocount") != null){
				outcar = (Long)outMap.get("ocount");
			}
			Long startTime = (Long)workMap.get("start_time");
			Long endTime = curTime;
			infoMap.put("onwork_time", TimeTools.getTime_yyMMdd_HHmm(startTime*1000));
			infoMap.put("outwork_time", TimeTools.getTime_yyMMdd_HHmm(endTime*1000));
			infoMap.put("incar",incar);//进车数量
			infoMap.put("outcar", outcar);//出车数量
			StatsReq req = new StatsReq();
			List<Object> idList = new ArrayList<Object>();
			idList.add(uid);
			req.setIdList(idList);
			req.setStartTime(startTime);
			req.setEndTime(endTime);
			StatsFacadeResp resp = accountFacade.statsParkUserAccount(req);
			if(resp.getResult() == 1){
				List<StatsAccountClass> classes = resp.getClasses();
				StatsAccountClass accountClass = classes.get(0);
				double cashParkingFee = accountClass.getCashParkingFee();
				double cashPrepayFee = accountClass.getCashPrepayFee();
				double cashRefundFee = accountClass.getCashRefundFee();
				double cashAddFee = accountClass.getCashAddFee();
				double cashPursueFee = accountClass.getCashPursueFee();
				
				double ePayParkingFee = accountClass.getePayParkingFee();
				double ePayPrepayFee = accountClass.getePayPrepayFee();
				double ePayRefundFee = accountClass.getePayRefundFee();
				double ePayAddFee = accountClass.getePayAddFee();
				double ePayPursueFee = accountClass.getePayPursueFee();
				
				double cardParkingFee = accountClass.getCardParkingFee();
				double cardPrepayFee = accountClass.getCardPrepayFee();
				double cardRefundFee = accountClass.getCardRefundFee();
				double cardAddFee = accountClass.getCardAddFee();
				double cardPursueFee = accountClass.getCardPursueFee();
				
				double escapeFee = accountClass.getEscapeFee();
				double sensorOrderFee = accountClass.getSensorOrderFee();
				
				//卡片统计
				double cardChargeCashFee = accountClass.getCardChargeCashFee();//卡片充值金额
				double cardReturnFee = accountClass.getCardReturnFee();//退卡退还金额
				double cardActFee = accountClass.getCardActFee();//卖卡金额
				long cardActCount = accountClass.getCardActCount();//激活卡片数量
				
				double cash = StringUtils.formatDouble(cashParkingFee + cashPursueFee + cashPrepayFee + cashAddFee);
				double epay = StringUtils.formatDouble(ePayParkingFee + ePayPursueFee + ePayPrepayFee + ePayAddFee);
				double card = StringUtils.formatDouble(cardParkingFee + cardPursueFee + cardPrepayFee + cardAddFee);
				double refund = StringUtils.formatDouble(cashRefundFee + ePayRefundFee + cardRefundFee);
				double pursue = StringUtils.formatDouble(cashPursueFee + ePayPursueFee + cardPursueFee);
				double prepay = StringUtils.formatDouble(cashPrepayFee + ePayPrepayFee + cardPrepayFee);
				double totalCash = StringUtils.formatDouble(cash - cashRefundFee);
				double receTotal = StringUtils.formatDouble(cash + epay + card - refund + escapeFee);
				infoMap.put("total_fee", cash);//现金总收费
				infoMap.put("epay", epay);//电子总收费
				infoMap.put("card_pay", card);//刷卡支付
				infoMap.put("history_prepay", refund);//垫付
				infoMap.put("upmoney", totalCash);//上缴
				infoMap.put("prepay_cash", cashPrepayFee);//现金预支付
				infoMap.put("prepay_epay", ePayPrepayFee);//电子预支付
				infoMap.put("prepay", prepay);//总预支付
				infoMap.put("pursue_cash", cashPursueFee);//现金追缴
				infoMap.put("pursue_epay", ePayPursueFee);//电子追缴
				infoMap.put("pursue", pursue);//总追缴
				infoMap.put("rece_fee", receTotal);//应收金额（实收+未缴）
				infoMap.put("act_card_count", cardActCount);
				infoMap.put("act_card_fee", cardActFee);
				infoMap.put("charge_card", cardChargeCashFee);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return infoMap;
	}
	
	private StatsWorkRecordResp getIncome(Long uid, Long workId, Long curTime, 
			Integer from){
		StatsWorkRecordResp statsWorkRecordResp = new StatsWorkRecordResp();
		try {
			int is_show_card = 0;//是否在收费汇总和打印小票处显示出来卡片的数据（有些运营集团没有卡片） 0：显示 1：不显示
			CollectorSetting setting = pService.getPOJO("select s.is_show_card from collector_set_tb s,user_info_tb u " +
					" where s.role_id=u.role_id and u.id=? order by s.id desc limit ? ",
					new Object[]{uid, 1}, CollectorSetting.class);
			if(setting != null){
				is_show_card = setting.getIs_show_card();
			}
			//打印签退小票
			WorkRecord workRecord = pService.getPOJO("select start_time from parkuser_work_record_tb " +
					" where id=? ", new Object[]{workId}, WorkRecord.class);
			if(workRecord != null){
				List<StatsWorkRecordInfo> infos = new ArrayList<StatsWorkRecordInfo>();
				statsWorkRecordResp.setResult(1);
				StatsWorkRecordInfo info1 = new StatsWorkRecordInfo();
				Long startTime = workRecord.getStart_time();//签到时间
				info1.setName("签到时间：");
				info1.setValue(TimeTools.getTime_yyMMdd_HHmm(startTime * 1000));
				infos.add(info1);
				String curName = "汇总时间：";
				if(from == 0){
					curName = "签退时间：";
				}
				StatsWorkRecordInfo info2 = new StatsWorkRecordInfo();
				info2.setName(curName);
				info2.setValue(TimeTools.getTime_yyMMdd_HHmm(curTime * 1000));
				infos.add(info2);
				//-------------------------------进出车数量-------------------------//
				int inCar = commonMethods.getInOutCar(workId, 0);
				int outCar = commonMethods.getInOutCar(workId, 1);
				StatsWorkRecordInfo info3 = new StatsWorkRecordInfo();
				info3.setName("进场车辆：");
				info3.setValue(inCar);
				infos.add(info3);
				StatsWorkRecordInfo info4 = new StatsWorkRecordInfo();
				info4.setName("出场车辆：");
				info4.setValue(outCar);
				infos.add(info4);
				//-------------------------------统计金额-------------------------//
				StatsReq req = new StatsReq();
				List<Object> idList = new ArrayList<Object>();
				idList.add(uid);
				req.setIdList(idList);
				req.setStartTime(startTime);
				req.setEndTime(curTime);
				StatsFacadeResp resp = accountFacade.statsParkUserAccount(req);
				if(resp.getResult() == 1){
					List<StatsAccountClass> classes = resp.getClasses();
					StatsAccountClass accountClass = classes.get(0);
					double cashParkingFee = accountClass.getCashParkingFee();
					double cashPrepayFee = accountClass.getCashPrepayFee();
					double cashRefundFee = accountClass.getCashRefundFee();
					double cashAddFee = accountClass.getCashAddFee();
					double cashPursueFee = accountClass.getCashPursueFee();
					
					double ePayParkingFee = accountClass.getePayParkingFee();
					double ePayPrepayFee = accountClass.getePayPrepayFee();
					double ePayRefundFee = accountClass.getePayRefundFee();
					double ePayAddFee = accountClass.getePayAddFee();
					double ePayPursueFee = accountClass.getePayPursueFee();
					
					double cardParkingFee = accountClass.getCardParkingFee();
					double cardPrepayFee = accountClass.getCardPrepayFee();
					double cardRefundFee = accountClass.getCardRefundFee();
					double cardAddFee = accountClass.getCardAddFee();
					double cardPursueFee = accountClass.getCardPursueFee();
					
					double escapeFee = accountClass.getEscapeFee();
					
					//卡片统计
					double cardChargeCashFee = accountClass.getCardChargeCashFee();//卡片充值金额
					double cardActFee = accountClass.getCardActFee();//卖卡金额
					long cardActCount = accountClass.getCardActCount();//激活卡片数量
					
					double cashCustomFee = StringUtils.formatDouble(cashParkingFee + cashPrepayFee + 
							cashAddFee - cashRefundFee);
					double epayCustomFee = StringUtils.formatDouble(ePayParkingFee + ePayPrepayFee + 
							ePayAddFee - ePayRefundFee);
					double cardCustomFee = StringUtils.formatDouble(cardParkingFee + cardPrepayFee + 
							cardAddFee - cardRefundFee);
					
					double cashTotalFee = StringUtils.formatDouble(cashPursueFee + cashCustomFee);
					double cashTotal = StringUtils.formatDouble(cashTotalFee + cardChargeCashFee + cardActFee);//需要上缴的现金金额
					
					List<StatsWorkRecordInfo> cashInfos = new ArrayList<StatsWorkRecordInfo>();
					StatsWorkRecordInfo cashInfo1 = new StatsWorkRecordInfo();
					cashInfo1.setName("普通订单：");
					cashInfo1.setValue(cashCustomFee + " 元");
					cashInfos.add(cashInfo1);
					StatsWorkRecordInfo cashInfo2 = new StatsWorkRecordInfo();
					cashInfo2.setName("追缴订单：");
					cashInfo2.setValue(cashPursueFee + " 元");
					cashInfos.add(cashInfo2);
					StatsWorkRecordInfo info5 = new StatsWorkRecordInfo();
					info5.setName("停车费-现金支付");
					info5.setValue(cashInfos);
					infos.add(info5);
					
					List<StatsWorkRecordInfo> epayInfos = new ArrayList<StatsWorkRecordInfo>();
					StatsWorkRecordInfo epayInfo1 = new StatsWorkRecordInfo();
					epayInfo1.setName("普通订单：");
					epayInfo1.setValue(epayCustomFee + " 元");
					epayInfos.add(epayInfo1);
					StatsWorkRecordInfo epayInfo2 = new StatsWorkRecordInfo();
					epayInfo2.setName("追缴订单：");
					epayInfo2.setValue(ePayPursueFee + " 元");
					epayInfos.add(epayInfo2);
					StatsWorkRecordInfo info6 = new StatsWorkRecordInfo();
					info6.setName("停车费-电子支付");
					info6.setValue(epayInfos);
					infos.add(info6);
					if(is_show_card == 0){
						List<StatsWorkRecordInfo> cardInfos = new ArrayList<StatsWorkRecordInfo>();
						StatsWorkRecordInfo cardInfo1 = new StatsWorkRecordInfo();
						cardInfo1.setName("普通订单：");
						cardInfo1.setValue(cardCustomFee + " 元");
						cardInfos.add(cardInfo1);
						StatsWorkRecordInfo cardInfo2 = new StatsWorkRecordInfo();
						cardInfo2.setName("追缴订单：");
						cardInfo2.setValue(cardPursueFee + " 元");
						cardInfos.add(cardInfo2);
						StatsWorkRecordInfo info7 = new StatsWorkRecordInfo();
						info7.setName("停车费-卡片支付");
						info7.setValue(cardInfos);
						infos.add(info7);
						
						List<StatsWorkRecordInfo> cards = new ArrayList<StatsWorkRecordInfo>();
						StatsWorkRecordInfo card1 = new StatsWorkRecordInfo();
						card1.setName("现金充值：");
						card1.setValue(cardChargeCashFee + " 元");
						cards.add(card1);
						StatsWorkRecordInfo card2 = new StatsWorkRecordInfo();
						card2.setName("售卡数量：");
						card2.setValue(cardActCount + " 张");
						cards.add(card2);
						StatsWorkRecordInfo card3 = new StatsWorkRecordInfo();
						card3.setName("售卡总面值：");
						card3.setValue(cardActFee + " 元");
						cards.add(card3);
						StatsWorkRecordInfo info8 = new StatsWorkRecordInfo();
						info8.setName("卡片");
						info8.setValue(cards);
						infos.add(info8);
					}
					StatsWorkRecordInfo info10 = new StatsWorkRecordInfo();
					info10.setName("逃单金额：");
					info10.setValue(escapeFee + " 元");
					info10.setFontColor("#FF0000");
					info10.setFontSize(16);
					infos.add(info10);
					StatsWorkRecordInfo info9 = new StatsWorkRecordInfo();
					info9.setName("上缴金额：");
					info9.setValue(cashTotal + " 元");
					info9.setFontColor("#FF0000");
					info9.setFontSize(16);
					infos.add(info9);
				}
				statsWorkRecordResp.setInfos(infos);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statsWorkRecordResp;
	}

	private String getBerths(HttpServletRequest request, Long uid, Long oldParkId, String token, long groupId) {
		//http://127.0.0.1/zld/collectorrequest.do?action=getberths&token=522b6bc2abd903eacf6b9a4ae3359815&berthid=15&devicecode=357143047019192
		try {
			Long ntime = System.currentTimeMillis()/1000;
			Long berthSegId = RequestUtil.getLong(request, "berthid", -1L);
			String device_code = RequestUtil.getString(request, "devicecode");
			//logger.error(berthSegId+","+device_code);
			if(berthSegId > 0){
				Map<String, Object> deviceMap = pService.getMap("select device_auth from mobile_tb where device_code=? ",
						new Object[]{device_code});
				//logger.error("deviceMap:"+deviceMap);
				if(deviceMap != null&&!deviceMap.isEmpty()){
					int device_auth = (Integer)deviceMap.get("device_auth");
					//logger.error("device_auth:"+device_auth);
					if(device_auth == 1){
						BerthSeg berthSeg = pService.getPOJO("select comid from com_berthsecs_tb where id =? ",
								new Object[]{berthSegId}, BerthSeg.class);
						long parkId = berthSeg.getComid();
						logger.error("berthsecs_id:"+berthSegId+",parkId:"+parkId+",old comid:"+oldParkId);
						updateSession(parkId, oldParkId, token);
						//查泊位上的订单，车牌
						List<Map<String, Object>> berths = daService.getAll("select id,cid as ber_name,order_id,state,dici_id from com_park_tb " +
								" where is_delete=? and berthsec_id=? order by cid ", new Object[]{0, berthSegId});
						//------------------------多线程并行查询------------------------//
						ExecutorService pool = ExecutorsUtil.getExecutorService();
						ExeCallable callable0 = new ExeCallable(berths, groupId, 0);//订单详情
						ExeCallable callable1 = new ExeCallable(berths, groupId, 1);//车检器状态
						ExeCallable callable2 = new ExeCallable(parkId, 2);//车场名称
						ExeCallable callable3 = new ExeCallable(parkId, 3);//车辆类型
						ExeCallable callable4 = new ExeCallable(berthSegId, uid, ntime, device_code, 4);//签到
						
						Future<Object> future0 = pool.submit(callable0);
						Future<Object> future1 = pool.submit(callable1);
						Future<Object> future2 = pool.submit(callable2);
						Future<Object> future3 = pool.submit(callable3);
						Future<Object> future4 = pool.submit(callable4);
						
						future0.get();
						future1.get();
						String parkName = (String)future2.get();
						String carType = (String)future3.get();
						Map<String, Object> signMap = (Map<String, Object>)future4.get();
						if(signMap != null){
							Long workId = (Long)signMap.get("workId");
							if(workId != null && workId > 0){
								String errmsg = (String)signMap.get("errmsg");
								logger.error("签到成功");
								return "{\"state\":\"1\",\"workid\":\"" + workId + "\",\"comid\":\"" + parkId + "\",\"cname\":\"" + parkName + "\",\"errmsg\":\""+errmsg+"\",\"data\":"+StringUtils.createJson(berths)+",\"car_type\":"+carType+"}";
							}
						}
						logger.error("签到失败");
						return "{\"state\":\"-1\",\"workid\":\"-1\",\"comid\":\"" + parkId + "\",\"cname\":\"" + parkName + "\",\"errmsg\":\"签到失败，请稍候重试！\",\"data\":[],\"car_type\":"+carType+"}";
					}
					logger.error("签到失败");
					return "{\"state\":\"0\",\"workid\":\"-2\",\"comid\":\"-1\",\"cname\":\"\",\"errmsg\":\"设备未审核，请联系管理员\",\"data\":[],\"car_type\":[]}";
				}
				logger.error("签到失败");
				return "{\"state\":\"0\",\"workid\":\"-1\",\"comid\":\"-1\",\"cname\":\"\",\"errmsg\":\"设备未注册，请联系管理员\",\"data\":[],\"car_type\":[]}";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "{}";
	}
	
	/**
	 * 签到
	 * @param berthSegId
	 * @param ntime
	 * @param uid
	 * @param device_code
	 * @return
	 */
	private Map<String, Object> signIn(long berthSegId, long ntime, long uid, String device_code){
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			WorkRecord workRecord = daService.getPOJO("select id,start_time from parkuser_work_record_tb where uid=? " +
					" and berthsec_id=? and state=? order by id desc limit ? ", 
					new Object[]{uid, berthSegId, 0, 1}, WorkRecord.class);
			if(workRecord == null){
				boolean logon = commonMethods.checkWorkTime(uid, ntime);
				int logon_state = 0;
				if(!logon){
					logon_state = 1;
				}
				logger.error("logon_state:"+logon_state);
				List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
				Map<String, Object> workRecordSqlMap = new HashMap<String, Object>();
				Long workId  = daService.getkey("seq_parkuser_work_record_tb");
				workRecordSqlMap.put("sql", "insert into parkuser_work_record_tb (id,berthsec_id,start_time,uid,state,device_code,logon_state) " +
						" values(?,?,?,?,?,?,?)");
				workRecordSqlMap.put("values", new Object[]{workId, berthSegId, ntime, uid, 0, device_code, logon_state});
				bathSql.add(workRecordSqlMap);
				
				Map<String, Object> workBerthSegSqlMap = new HashMap<String, Object>();
				workBerthSegSqlMap.put("sql", "update work_berthsec_tb set state=? where berthsec_id=? and is_delete=? ");
				workBerthSegSqlMap.put("values", new Object[]{1, berthSegId, 0});
				bathSql.add(workBerthSegSqlMap);
				
				Map<String, Object> onlineSqlMap = new HashMap<String, Object>();
				onlineSqlMap.put("sql", "update user_info_tb set online_flag=? where id=? ");
				onlineSqlMap.put("values", new Object[]{22, uid});
				bathSql.add(onlineSqlMap);
				
				boolean b = daService.bathUpdate2(bathSql);
				logger.error("b:"+b);
				if(b){
					map.put("workId", workId);
					map.put("errmsg", "");
					return map;
				}
				map.put("errmsg", "签到失败，请稍候重试！");
				return map;
			}else {
				Long startTime = workRecord.getStart_time();
				map.put("workId", workRecord.getId());
				map.put("errmsg", "您签到的时间：" + TimeTools.getTime_MMdd_HHmm(startTime * 1000));
				return map;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	/**
	 * 把新的车场编号更新进session中
	 * @param newParkId
	 * @param oldParkId
	 * @param token
	 */
	private void updateSession(long newParkId, long oldParkId, String token){
		//把comid更新到user_session_tb 和缓存中。。。
		try {
			if(newParkId != oldParkId){
				int r = daService.update("update user_session_tb set comid=? where token=? ",
						new Object[]{newParkId, token});
				logger.error("r:"+r);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取车场的车辆类型
	 * @param parkId
	 * @return
	 */
	private String getCarTypeInfo(long parkId){
		try {
			if(parkId > 0){
				long count = pService.getLong("select count(id) from com_info_tb where id=? and car_type=? ",
						new Object[]{parkId, 1});
				if(count > 0){
					List<Map<String, Object>> carTypeList = pService.getAll("select id,name from car_type_tb " +
							" where comid=? order by sort ", new Object[]{parkId});
					if(carTypeList != null && !carTypeList.isEmpty()){
						return StringUtils.createJson(carTypeList);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "[]";
	}
	
	/**
	 * 获取泊位上的订单信息
	 * @param list
	 * @param groupId
	 */
	private void getOrderInfo(List<Map<String, Object>> list, long groupId){
		try {
			if(list != null && !list.isEmpty()){
				List<Object> params = new ArrayList<Object>();
				String preParam = "";
				for(Map<String, Object> berth : list){
					berth.put("car_number", "");//客户端要求即使没有订单，也要返回car_numnber字段
					Integer state = (Integer)berth.get("state");
					Long orderId = (Long)berth.get("order_id");
					if(orderId != null && orderId > 0 && state == 1){
						params.add(orderId);
						if("".equals(preParam)){
							preParam = "?";
						}else{
							preParam += ",?";
						}
					}
				}
				if(!params.isEmpty()){
					params.add(0);
					List<Map<String, Object>> orders = daService.getAllMap("select id,car_number,prepaid,c_type,nfc_uuid from order_tb " +
							" where id in ("+preParam+") and state=? ", params);
					if(orders != null && !orders.isEmpty()){
						boolean showCardUser = false;
						String group_card_user = CustomDefind.GROUP_CARD_USER;
						if(group_card_user != null){
							String[] groupIds = group_card_user.split("\\|");
							for(int i = 0; i<groupIds.length; i++){
								long gId = Long.valueOf(groupIds[i]);
								if(gId == groupId){
									showCardUser = true;
									break;
								}
							}
						}
						logger.error("showCardUser:"+showCardUser);
						for(Map<String, Object> map : orders){
							Long id = (Long)map.get("id");
							for(Map<String, Object> map2 : list){
								Long orderId = (Long)map2.get("order_id");
								if(orderId != null){
									if(id.intValue() == orderId.intValue()){
										String car_number = (String)map.get("car_number");
										map2.put("orderid", id);
										map2.put("car_number", car_number);
										map2.put("prepay", map.get("prepaid"));
										map2.put("ismonthuser", map.get("c_type"));
										
										if(showCardUser){
											boolean is_card = commonMethods.cardUser(car_number, groupId);
											if(is_card){
												map2.put("is_card", 1);
											}else{
												map2.put("is_card", 0);
											}
										}
										break;
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取泊位上的车检器进车状态
	 * @param list
	 */
	private void getSensorInfo(List<Map<String, Object>> list){
		try {
			if(list != null && !list.isEmpty()){
				List<Object> params = new ArrayList<Object>();
				String preParams = "";
				for(Map<String, Object> map : list){
					if(map.get("dici_id") != null){
						params.add(map.get("dici_id"));
						if("".equals(preParams)){
							preParams = "?";
						}else{
							preParams += ",?";
						}
					}
				}
				if(params != null && !params.isEmpty()){
					params.add(0);
					List<Map<String, Object>> sensorList = pService.getAllMap("select id,state from dici_tb where " +
							" id in ("+preParams+") and is_delete=? ", params);
					if(sensorList != null && !sensorList.isEmpty()){
						for(Map<String, Object> map : sensorList){
							Long id = (Long)map.get("id");
							for(Map<String, Object> map2 : list){
								if(map2.get("dici_id") != null){
									Integer sensorId = (Integer)map2.get("dici_id");
									if(id.intValue() == sensorId.intValue()){
										map2.put("sensor_state", map.get("state"));
										break;
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("getSensorInfo>>>",e);
		}
	}

	private String liftRodReason(HttpServletRequest request) {
		Integer reason = RequestUtil.getInteger(request, "reason", -1);
		Long lrid = RequestUtil.getLong(request, "lrid", -1L);
		if(lrid==-1){
			return  "{result:-1,errmsg:\"订单编号为空！\"}";
		}
		String sql = "update lift_rod_tb set reason=? where id=?";
		int ret = daService.update(sql, new Object[]{reason,lrid});
		logger.error(">>>>>>>>>>lrid:"+lrid+",reason:"+reason+",update lift_rod_tb,ret:"+ret);
		return  "{result:\""+ret+"\",errmsg:\"操作成功！\"}";
	}

	private String liftRod(HttpServletRequest request, Long uin, Long comId) {
		Long ntime = System.currentTimeMillis()/1000;
		Long key = daService.getkey("seq_lift_rod_tb");
		Long pass_id = RequestUtil.getLong(request, "passid", -1L);//入口通道id
		Integer reason = RequestUtil.getInteger(request, "reason", -2);//原因未选 
		String sql = "insert into lift_rod_tb (id,comid,uin,ctime,pass_id,reason) values(?,?,?,?,?,?)";
		int ret = daService.update(sql, new Object[]{key,comId,uin,ntime,pass_id,reason});
		logger.error(">>>>>>>>>>"+comId+","+uin+",upload lift rod,insert into db ret:"+ret);
		if(ret==1)
			return  "{\"result\":\""+ret+"\",\"errmsg\":\"操作成功！\",lrid:\""+key+"\"}";
		else {
			return  "{\"result\":\""+ret+"\",\"errmsg\":\"操作失败！\"}";
		}
	}

	//上传抬杆记录
	private String liftRodPic(HttpServletRequest request) throws Exception{
		Long ntime = System.currentTimeMillis()/1000;
		Long lrid = RequestUtil.getLong(request, "lrid", -1L);
		logger.error("begin upload lift rod picture....lrid:"+lrid);
		Map<String, String> extMap = new HashMap<String, String>();
	    extMap.put(".jpg", "image/jpeg");
	    extMap.put(".jpeg", "image/jpeg");
	    extMap.put(".png", "image/png");
	    extMap.put(".gif", "image/gif");
	    extMap.put(".webp", "image/webp");
		if(lrid==-1){
			return  "{\"result\":\"-1\",\"errmsg\":\"订单编号为空！\"}";
		}
		request.setCharacterEncoding("UTF-8"); // 设置处理请求参数的编码格式
		DiskFileItemFactory  factory = new DiskFileItemFactory(); // 建立FileItemFactory对象
		factory.setSizeThreshold(16*4096*1024);
		ServletFileUpload upload = new ServletFileUpload(factory);
		// 分析请求，并得到上传文件的FileItem对象
		upload.setSizeMax(16*4096*1024);
		List<FileItem> items = null;
		try {
			items =upload.parseRequest(request);
		} catch (FileUploadException e) {
			e.printStackTrace();
			return "{\"result\":\"1\",\"errmsg\":\"车牌保存成功！\"}";
		}
		String filename = ""; // 上传文件保存到服务器的文件名
		InputStream is = null; // 当前上传文件的InputStream对象
		// 循环处理上传文件
		for (FileItem item : items){
			// 处理普通的表单域
			if (!item.isFormField()){
				// 从客户端发送过来的上传文件路径中截取文件名
				filename = item.getName().substring(item.getName().lastIndexOf("\\")+1);
				is = item.getInputStream(); // 得到上传文件的InputStream对象
				logger.error("filename:"+item.getName()+",stream:"+is);
			}else{
				continue;
			}
			String file_ext =filename.substring(filename.lastIndexOf(".")).toLowerCase();// 扩展名
			String picurl = lrid + "_"+ ntime + file_ext;
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
				
				DBCollection collection = mydb.getCollection("lift_rod_pics");
				//  DBCollection collection = mydb.getCollection("records_test");
				
				BasicDBObject document = new BasicDBObject();
				document.put("lrid", lrid);
				document.put("ctime", ntime);
				document.put("type", extMap.get(file_ext));
				document.put("content", content);
				document.put("filename", picurl);
				//开始事务
				//结束事务
				mydb.requestStart();
				collection.insert(document);
				//结束事务
				mydb.requestDone();
				in.close();        
				is.close();
				byteout.close();
				String sql = "update lift_rod_tb set img=? where id =?";
				int ret = daService.update(sql, new Object[]{picurl,lrid});
				logger.error(">>>>>>>>>>orderId:"+lrid+",filename:"+picurl+", update lift_rod_tb, ret:"+ret);
			} catch (Exception e) {
				e.printStackTrace();
				return "{\"result\":\"0\",\"errmsg\":\"图片上传失败！\"}";
			}finally{
				if(in!=null)
					in.close();
				if(byteout!=null)
					byteout.close();
				if(is!=null)
					is.close();
			}
		}
		return "{\"result\":\"1\",\"errmsg\":\"上传成功！\"}";
	}
	//绑定车位到车上
	private String bondCarPark(HttpServletRequest request, Long comId) {
		Long ntime = System.currentTimeMillis()/1000;
		Long id = RequestUtil.getLong(request, "id", -1L);
		Long oid = RequestUtil.getLong(request, "orderid", -1L);
		long count = daService.getLong("select count(*) from order_tb where id = ? and state=?",
				new Object[]{oid, 1});
		int ret = 0;
		if(count==0){
			count = daService.getLong("select count(id) from com_park_tb where order_id>? " +
					" and state =? and id=? ", new Object[]{0, 1, id});
			if(count == 0){
				ret = daService.update("update com_park_tb set order_id =?,state =?,enter_time=? where id =?",
						new Object[]{oid,1,ntime,id});
				logger.error("(update com_park_tb orderid):"+oid+",id:"+id+",result:"+ret);
			}
		}
		return "{\"result\":\""+ret+"\"}";
	}
	//查询所有空闲车位
	private String getFreeParks(HttpServletRequest request,Long comid) {
		//更新车位上已经结算的订单，改为未占用状态
		//commonMethods.updateParkInfo(comid);
		String result ="{\"result\":\"0\",\"errmsg\":\"未设置车位信息\"}";
		List list = daService.getAll("select id,cid as name from com_park_tb where comid =? and state = ? ", new Object[]{comid,0});
		if(list!=null&&!list.isEmpty()){
			
			result ="{\"result\":\"1\",\"errmsg\":\"\",\"info\":"+StringUtils.createJson(list)+"}";
		}
		return result;
	}

	private String queryAccount(HttpServletRequest request, Long uin) {
		Long ntime = System.currentTimeMillis()/1000;
		String carnumber = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "carnumber"));
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "size", 20);
		if(carnumber.equals("")){
			//AjaxUtil.ajaxOutput(response, "-1");
			return "-1";
		}
		Map<String, Object> carMap = pService.getMap(
				"select uin from car_info_tb where car_number=? ",
				new Object[] { carnumber });
		if(carMap == null || carMap.get("uin") == null){
			//AjaxUtil.ajaxOutput(response, "-1");
			return "-1";
		}
		List<Map<String, Object>> carList = pService
				.getAll("select car_number from car_info_tb where uin=? and state=? ",
						new Object[] { carMap.get("uin"), 1 });
		String cnum = "该车主有"+carList.size()+"个车牌:/n";
		for(int i = 0; i<carList.size(); i++){
			Map<String, Object> map = carList.get(i);
			if(i == 0){
				cnum += map.get("car_number");
			}else{
				cnum += "," + map.get("car_number");
			}
		}
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		List<Object> params = new ArrayList<Object>();
		String sql = "select a.*,o.car_number carnumber from parkuser_account_tb a,order_tb o where a.orderid=o.id and o.uid=? and o.uin=? and a.type=? and a.create_time between ? and ? order by a.create_time desc";
		String sqlcount = "select count(a.*) from parkuser_account_tb a,order_tb o where a.orderid=o.id and o.uid=? and o.uin=? and a.type=? and a.create_time between ? and ? ";
		params.add(uin);
		params.add(carMap.get("uin"));
		params.add(0);
		params.add(ntime - 30*24*60*60);
		params.add(ntime);
		Long count = pService.getCount(sqlcount, params);
		if(count > 0){
			list = pService.getAll(sql, params, pageNum, pageSize);
			setRemark(list);
		}
		String reslut =  "{\"count\":"+count+",\"carinfo\":\""+cnum+"\",\"info\":"+StringUtils.createJson(list)+"}";
		return reslut;
	}

	private String todayAccount(HttpServletRequest request, Long uin,
			Long comId, Map<String, Object> infoMap) {
		Long ntime = System.currentTimeMillis()/1000;
		Long b = TimeTools.getToDayBeginTime();
		Long e = ntime+60;
		Double parkmoney = 0d;
		Double parkusermoney = 0d;
//		Double cashmoney = 0d;
		Double rewardmoney = 0d;
		Double todayscore = 0d;//剩余积分
		Long todayin = 0L;//今日入场车辆
		Long todayout = 0L;//今日出场车辆
		//一键对账   1:车场管理员    2收费员
		Map park = daService.getMap( "select sum(amount) total from park_account_tb where create_time between ? and ? " +
				" and type <> ? and uid=? and comid=? ",new Object[]{b,e,1,uin,comId});
		if(park!=null&&park.get("total")!=null){
			parkmoney = Double.valueOf(park.get("total")+"");//车场账户收入（不计来源）
		}
		
		Map parkuser = daService.getMap( "select sum(amount) total from parkuser_account_tb where create_time between ? and ? " +
				" and type= ? and uin = ? ",new Object[]{b,e,0,uin});
		if(parkuser!=null&&parkuser.get("total")!=null){
			parkusermoney = Double.valueOf(parkuser.get("total")+"");//收费员账户收入（不计来源）
		}
		
		/*Map cash = daService.getMap( "select sum(amount) total from parkuser_cash_tb where create_time between ? and ? " +
				" and uin=? ",new Object[]{b,e,uin});
		if(cash!=null&&cash.get("total")!=null){
			cashmoney = Double.valueOf(cash.get("total")+"");//收费员现金收入
		}*/
		
		Map reward = daService.getMap("select sum(money) total from parkuser_reward_tb where ctime between ? and ? and uid=? ",
						new Object[] { b, e, uin });
		if(reward != null && reward.get("total") != null){
			rewardmoney = Double.valueOf(reward.get("total") + "");
		}
		
		Map score = pService.getMap("select reward_score from user_info_tb where id=? ", new Object[] { uin });
		if(score != null && score.get("reward_score") != null){
			todayscore = Double.valueOf(score.get("reward_score") + "");
		}
		
		todayin = pService.getLong("select count(1) from order_tb where comid=? " +
				"and create_time between ? and ?", new Object[]{comId,b,e});
		
		todayout = pService.getLong("select count(1) from order_tb where comid=? and state=? " +
				"and end_time between ? and ?", new Object[]{comId,1,b,e});
		
		infoMap.put("mobilemoney", StringUtils.formatDouble(parkmoney + parkusermoney));
		infoMap.put("rewardmoney", StringUtils.formatDouble(rewardmoney));
		infoMap.put("todayscore", StringUtils.formatDouble(todayscore));
		infoMap.put("todayin", todayin);
		infoMap.put("todayout", todayout);
		//AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
		return  StringUtils.createJson(infoMap);
	}

	private String deductScore(HttpServletRequest request, Long uin,
			Long comId, Map<String, Object> infoMap) {
		Long ntime = System.currentTimeMillis()/1000;
		Double score = RequestUtil.getDouble(request, "score", 0d);//消耗积分
		Long ticketid = RequestUtil.getLong(request, "ticketid", -1L);
		logger.error("ticketid:"+ticketid+",score:"+ticketid+",uin:"+uin);
		if(score == 0 || ticketid == -1){
			infoMap.put("result", -1);
		}
		Map<String, Object> userMap = daService.getMap(
				"select id,nickname,reward_score from user_info_tb where id=? ",
				new Object[] { uin });
		Map<String, Object> comMap = daService.getMap("select company_name from com_info_tb where id=? ", new Object[]{comId});
		Double reward_score = Double.valueOf(userMap.get("reward_score") + "");
		if(reward_score < score){
			infoMap.put("result", -3);
		//	AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));//积分不足
			logger.error("deductscore>>>打赏积分不足，收费员:"+uin+",score:"+score+",reward_score:"+reward_score);
			return  StringUtils.createJson(infoMap);
		}
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		
		Map<String, Object> scoreSqlMap = new HashMap<String, Object>();
		//积分明细
		Map<String, Object> scoreAccountSqlMap = new HashMap<String, Object>();
		
		scoreAccountSqlMap.put("sql", "insert into reward_account_tb (uin,score,type,create_time,remark,target,ticket_id) values(?,?,?,?,?,?,?)");
		scoreAccountSqlMap.put("values", new Object[]{uin,score,1,ntime,"停车券 用户微信点击领取",2,ticketid});
		bathSql.add(scoreAccountSqlMap);
		
		scoreSqlMap.put("sql", "update user_info_tb set reward_score=reward_score-? where id=? ");
		scoreSqlMap.put("values", new Object[]{score, uin});
		bathSql.add(scoreSqlMap);
		
		boolean b = daService.bathUpdate(bathSql);
		logger.error("uin:"+uin+",b:"+b);
		if(b){
			infoMap.put("result", 1);
		}
		//AjaxUtil.ajaxOutput(response,StringUtils.createJson(infoMap));
		return StringUtils.createJson(infoMap);
	}

	private String sweepTicket(HttpServletRequest request, Long uin, Long comId) {
		Long ntime = System.currentTimeMillis()/1000;
		Double score = RequestUtil.getDouble(request, "score", 0d);//消耗积分
		Integer bmoney = RequestUtil.getInteger(request, "bmoney", 0);//金额
		Map<String, Object>  infoMap = new HashMap<String, Object>();
		Long ticketId = daService.getkey("seq_ticket_tb");
		logger.error("sweepticket>>>收费员："+uin+",score:"+score+",bmoney:"+bmoney+",ticketId:"+ticketId);
		Map<String, Object> userMap = daService.getMap(
				"select id,nickname,reward_score from user_info_tb where id=? ",
				new Object[] { uin });
		Map<String, Object> comMap = daService.getMap("select company_name from com_info_tb where id=? ", new Object[]{comId});
		Double reward_score = Double.valueOf(userMap.get("reward_score") + "");
		if(reward_score < score){
			infoMap.put("result", -3);
			//AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));//积分不足
			logger.error("sendticket>>>打赏积分不足，收费员:"+uin+",score:"+score+",reward_score:"+reward_score);
			return StringUtils.createJson(infoMap);
		}
		Long ctime = ntime;
		String code = null;
		Long ticketids[] = new Long[]{ticketId};
		String []codes = StringUtils.getGRCode(ticketids);
		if(codes.length > 0){
			code = codes[0];
		}
		logger.error("sweepticket>>>收费员："+uin+",code:"+code);
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		//二维码
		Map<String, Object> codeSqlMap = new HashMap<String, Object>();
		
		Map<String, Object> ticketSqlMap = new HashMap<String, Object>();
		
		codeSqlMap.put("sql", "insert into qr_code_tb(comid,uid,ctime,type,state,code,isuse,ticketid,score) values(?,?,?,?,?,?,?,?,?)");
		codeSqlMap.put("values", new Object[] { comId, uin, ctime, 6, 0, code, 1, ticketId, score });
		bathSql.add(codeSqlMap);
		
		ticketSqlMap.put("sql", "insert into ticket_tb(id,create_time,limit_day,money,state,comid,type) values(?,?,?,?,?,?,?)");
		ticketSqlMap.put("values", new Object[] {ticketId, TimeTools.getToDayBeginTime(),TimeTools.getToDayBeginTime()+16*24*60*60-1, bmoney, 0, comId, 1});
		bathSql.add(ticketSqlMap);
		
		boolean b = daService.bathUpdate(bathSql);
		logger.error("sweepticket>>>收费员："+uin+",ticketId:"+ticketId+",code:"+code+",b:"+b);
		if(b){
			String url = "http://"+Constants.WXPUBLIC_S_DOMAIN+"/zld/qr/c/"+code;
			infoMap.put("result", 1);
			infoMap.put("code", url);
			infoMap.put("ticketid", ticketId);
			infoMap.put("cname", comMap.get("company_name"));
		}else{
			infoMap.put("result", -1);
		}
		return StringUtils.createJson(infoMap);
	}

	private String parkingList(HttpServletRequest request, Long uin) {
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "size", 20);
		Long btime = TimeTools.getToDayBeginTime() - 6* 24 * 60 *60;
		List<Object> params = new ArrayList<Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		String sql = "select uin,count(*) pcount from order_tb where state=? and uid=? and end_time>? and uin is not null group by uin order by pcount desc";
		String countsql = "select count(distinct uin) from order_tb where state=? and uid=? and end_time>? and uin is not null ";
		params.add(1);
		params.add(uin);
		params.add(btime);
		Long count = daService.getCount(countsql, params);
		if(count > 0){
			list = daService.getAll(sql, params, pageNum, pageSize);
			setCarNumber(list);
		}
		String result = "{\"count\":"+count+",\"info\":"+StringUtils.createJson(list)+"}";
		return result;
	}

	private String rewardList(HttpServletRequest request, Long uin) {
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "size", 20);
		List<Object> params = new ArrayList<Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		Long btime = TimeTools.getToDayBeginTime() - 6 * 24 * 60 * 60;
		params.add(uin);
		params.add(btime);
		String sql = "select uin,count(*) rcount,sum(money) rmoney from parkuser_reward_tb where uid=? and ctime>? group by uin order by rcount desc";
		String countsql = "select count(distinct uin) from parkuser_reward_tb where uid=? and ctime>? ";
		Long count = daService.getCount(countsql, params);
		if(count > 0){
			list = daService.getAll(sql, params, pageNum, pageSize);
			setCarNumber(list);
		}
		String result = "{\"count\":"+count+",\"info\":"+StringUtils.createJson(list)+"}";
		return result;
	}

	private String sendSuccess(HttpServletRequest request, Long uin) {
		Long ntime = System.currentTimeMillis()/1000;
		Long bonusId = RequestUtil.getLong(request, "bonusid", -1L);
		Double score = RequestUtil.getDouble(request, "score", 15d);
		Long ctime = ntime;
		logger.error("sendsuccess>>>红包发送成功回调:bonusid:"+bonusId+",uin:"+uin+",score:"+score);
		if(bonusId != -1){
			Long count = daService.getLong("select count(*) from reward_account_tb where orderticket_id=? ", new Object[]{bonusId});
			logger.error("sendsuccess>>>红包发送成功回调:bonusid:"+bonusId+",uin:"+uin+",count:"+count+",score:"+score);
			if(count == 0){
				Map<String, Object> userMap = daService.getMap(
						"select id,nickname,reward_score from user_info_tb where id=? ",
						new Object[] { uin });
				Double reward_score = Double.valueOf(userMap.get("reward_score") + "");
				logger.error("sendsuccess>>>红包发送成功回调:bonusid:"+bonusId+",uin:"+uin+",score:"+score+",剩余积分reward_score:"+reward_score+",此次消耗积分score:"+score);
				if(reward_score > score){
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					//积分账户
					Map<String, Object> scoreSqlMap = new HashMap<String, Object>();
					//积分明细
					Map<String, Object> scoreAccountSqlMap = new HashMap<String, Object>();
					
					scoreAccountSqlMap.put("sql", "insert into reward_account_tb (uin,score,type,create_time,remark,target,orderticket_id) values(?,?,?,?,?,?,?)");
					scoreAccountSqlMap.put("values", new Object[]{uin,score,1,ctime,"红包 ",1,bonusId});
					bathSql.add(scoreAccountSqlMap);
					
					scoreSqlMap.put("sql", "update user_info_tb set reward_score=reward_score-? where id=? ");
					scoreSqlMap.put("values", new Object[]{score, uin});
					bathSql.add(scoreSqlMap);
					boolean b = daService.bathUpdate(bathSql);
					logger.error("sendsuccess>>>红包发送成功回调:bonusid:"+bonusId+",uin:"+uin+",b:"+b);
					if(b){
						//AjaxUtil.ajaxOutput(response, "1");
						return "1";
					}
				}
			}
		}
		//AjaxUtil.ajaxOutput(response, "-1");
		return "-1";
	}

	private String sendBonus(HttpServletRequest request, Long uin, Long comId) {
		Long ntime = System.currentTimeMillis()/1000;
		Integer bmoney = RequestUtil.getInteger(request, "bmoney", 0);//金额
		Integer bnum = RequestUtil.getInteger(request, "bnum", 0);//个数
		Double score = RequestUtil.getDouble(request, "score", 0d);//消耗积分
		Map<String, Object> infoMap=new HashMap<String, Object>(); 
		logger.error("sendbonus>>>收费员："+uin+",bmoney:"+bmoney+",bnum:"+bnum+",score:"+score);
		Map<String, Object> comMap = daService.getMap("select company_name from com_info_tb where id=? ", new Object[]{comId});
		Map<String, Object> userMap = daService.getMap(
				"select id,nickname,reward_score from user_info_tb where id=? ",
				new Object[] { uin });
		Double reward_score = Double.valueOf(userMap.get("reward_score") + "");
		if(reward_score < score){
			infoMap.put("result", -3);
			//AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));//积分不足
			logger.error("sendticket>>>打赏积分不足，收费员:"+uin+",score:"+score+",reward_score:"+reward_score);
			return StringUtils.createJson(infoMap);
		}
		Long ctime = ntime;
		Long exptime = ctime + 24*60*60;
		Long bonusId = daService.getkey("seq_order_ticket_tb");
		int result = daService.update("insert into order_ticket_tb (id,uin,order_id,money,bnum,ctime,exptime,bwords,type) values(?,?,?,?,?,?,?,?,?)",
						new Object[] { bonusId, uin, -1, bmoney, bnum, ctime, exptime, "祝您一路发发发!", 2 });
		logger.error("sendbonus>>>:收费员"+uin+",result:"+result);
		if(result == 1){
			infoMap.put("result", 1);
			infoMap.put("bonusid", bonusId);
			infoMap.put("cname", comMap.get("company_name"));
		}else{
			infoMap.put("result", -1);
		}
		return  StringUtils.createJson(infoMap);
	}

	private String sendTicket(HttpServletRequest request, Long uin,Long comId) {
		Long ntime = System.currentTimeMillis()/1000;
		Integer bmoney = RequestUtil.getInteger(request, "bmoney", 0);//金额
		Double score = RequestUtil.getDouble(request, "score", 0d);//消耗积分
		String uins = RequestUtil.processParams(request, "uins");//车主账号
		logger.error("sendticket>>>收费员:"+uin+",bmoney:"+bmoney+",score:"+score+",uins:"+uins);
		String ids[] = uins.split(",");
		if(ids.length == 0 || uins.length() == 0){
			//AjaxUtil.ajaxOutput(response, "-2");//未选择车主
			return "-2";
		}
		Long ctime = ntime;
		Map<String, Object> userMap = daService.getMap(
				"select id,nickname,reward_score from user_info_tb where id=? ",
				new Object[] { uin });
		Map<String, Object> comMap = daService.getMap(
				"select company_name from com_info_tb where id=? ",
				new Object[] { comId });
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		//积分账户
		Map<String, Object> scoreSqlMap = new HashMap<String, Object>();
		Long exptime = ctime + 24*60*60;
		for(int i = 0; i<ids.length; i++){
			//写券
			Map<String, Object> ticketSqlMap = new HashMap<String, Object>();
			//积分明细
			Map<String, Object> scoreAccountSqlMap = new HashMap<String, Object>();
			
			Long cuin = Long.valueOf(ids[i]);
			String carNumber = publicMethods.getCarNumber(cuin);
			Long ticketId = daService.getkey("seq_ticket_tb");
			
			ticketSqlMap.put("sql", "insert into ticket_tb (id,create_time,limit_day,money,state,uin,type,comid) values(?,?,?,?,?,?,?,?)");
			ticketSqlMap.put("values", new Object[]{ticketId,TimeTools.getToDayBeginTime(),TimeTools.getToDayBeginTime()+16*24*60*60-1,bmoney,0,cuin,1,comId});
			bathSql.add(ticketSqlMap);
			
			scoreAccountSqlMap.put("sql", "insert into reward_account_tb (uin,score,type,create_time,remark,target,ticket_id) values(?,?,?,?,?,?,?)");
			scoreAccountSqlMap.put("values", new Object[]{uin,score,1,ctime,"停车券 "+carNumber,2,ticketId});
			bathSql.add(scoreAccountSqlMap);
		}
		Double allscore = StringUtils.formatDouble(score * ids.length);
		logger.error("sendticket>>>收费员:"+uin+",allscore:"+allscore);
		Double reward_score = Double.valueOf(userMap.get("reward_score") + "");
		if(reward_score < allscore){
			//AjaxUtil.ajaxOutput(response, "-3");//积分不足
			logger.error("sendticket>>>打赏积分不足，收费员:"+uin+",allscore:"+allscore+",reward_score:"+reward_score);
			return "-3";
		}
		if(allscore > 0 && bathSql.size() > 0){
			scoreSqlMap.put("sql", "update user_info_tb set reward_score=reward_score-? where id=? ");
			scoreSqlMap.put("values", new Object[]{allscore, uin});
			bathSql.add(scoreSqlMap);
		}
		boolean b = daService.bathUpdate(bathSql);
		logger.error("sendticket>>>收费员："+uin+",b:"+b);
		if(b){
			for(int i = 0;i<ids.length; i++){
				Long cuin = Long.valueOf(ids[i]);
				logService.insertUserMesg(5, cuin,"我是停车费收费员" + userMap.get("nickname") + "，给您赠送"
								+ bmoney + "元" + comMap.get("company_name")
								+ "专用券，邀您来我车场停车。", "停车券提醒");
			}
			sendWXMsg(ids, userMap, comMap, bmoney);
			//AjaxUtil.ajaxOutput(response, "1");
			return "1";
		}else{
			//AjaxUtil.ajaxOutput(response, "-1");
			return "-1";
		}
	}

	private String bonusInfo(HttpServletRequest request, Long uin) {
		String bonusinfo = CustomDefind.SENDTICKET;
		JSONArray jsonArray = JSONArray.fromObject(bonusinfo);
		for(int i=0; i<jsonArray.size(); i++){
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			int type = jsonObject.getInt("type");
			int bmoney = jsonObject.getInt("bmoney");
			double score = jsonObject.getDouble("score");
			if(type == 1 && bmoney == 5){
				Long btime = TimeTools.getToDayBeginTime();
				Long count = daService.getLong("select count(*) from reward_account_tb r,ticket_tb t where r.ticket_id=t.id and r.type=? and r.target=? and r.create_time>? and t.money=? and r.uin=? ",
								new Object[] { 1, 2, btime, 5, uin });
				score = score * (count + 1);
				logger.error("今日发的五元券个数count:"+count+",uid:"+uin+",today:"+btime+",下一个花费积分：score："+score);
				jsonObject.put("score", score);
				if(count >=10){
					jsonObject.put("limit", 1);
				}else{
					jsonObject.put("limit", 0);
				}
				break;
			}
		}
		logger.error("bonusinfo:"+jsonArray.toString()+",uin:"+uin);
		return jsonArray.toString();
	}

	private String rewardRank(HttpServletRequest request, Long uin) {
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "size", 20);
		List<Object> params = new ArrayList<Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		String monday = StringUtils.getMondayOfThisWeek();
		Long btime = TimeTools.getLongMilliSecondFrom_HHMMDD(monday)/1000;
		String sql = "select uid uin,sum(money) money from parkuser_reward_tb where ctime>=? group by uid order by money desc ";
		String countsql = "select count(distinct uid) from parkuser_reward_tb where ctime>=? ";
		params.add(btime);
		Long total = daService.getCount(countsql, params);
		if(total > 0){
			list = daService.getAll(sql, params, pageNum, pageSize);
			setinfo(list, pageNum, pageSize);
		}
		String result = "{\"count\":"+total+",\"info\":"+StringUtils.createJson(list)+"}";
		return result;
	}

	private String rscoreRank(HttpServletRequest request, Long uin) {
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "size", 20);
		Long btime = TimeTools.getToDayBeginTime();
		List<Object> params = new ArrayList<Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		String sql = "select uin,sum(score) score from reward_account_tb where create_time>=? and type=? group by uin order by score desc ";
		String countsql = "select count(distinct uin) from reward_account_tb where create_time>=? and type=? ";
		params.add(btime);
		params.add(0);
		Long total = daService.getCount(countsql, params);
		if(total > 0){
			list = daService.getAll(sql, params, pageNum, pageSize);
			setinfo(list, pageNum, pageSize);
		}
		String result = "{\"count\":"+total+",\"info\":"+StringUtils.createJson(list)+"}";
		return result;
	}

	private Map<String, Object> rewardscore(HttpServletRequest request,
			Long uin, Map<String, Object> infoMap) {
		Double remainscore = 0d;//剩余积分
		Long rank = 0L;//排行榜
		Double todayscore = 0d;//今日积分
		Long btime = TimeTools.getToDayBeginTime();
		Map<String, Object> scoreMap = daService
				.getMap("select reward_score from user_info_tb where id=? ",
						new Object[] { uin });
		if(scoreMap != null){
			remainscore = Double.valueOf(scoreMap.get("reward_score") + "");
		}
		Long scoreCount = daService.getLong("select count(*) from reward_account_tb where create_time> ? and type=? and uin=? ",
						new Object[] { btime, 0, uin });
		if(scoreCount > 0){
			List<Map<String, Object>> scoreList = daService
					.getAll("select uin,sum(score) score from reward_account_tb where create_time> ? and type=? group by uin order by score desc ",
							new Object[] { btime, 0 });
			for(Map<String, Object> map : scoreList){
				Long uid = (Long)map.get("uin");
				rank++;
				if(uid.intValue() == uin.intValue()){
					todayscore = Double.valueOf(map.get("score") + "");
					break;
				}
			}
		}
		infoMap.put("todayscore", todayscore);
		infoMap.put("rank", rank);
		infoMap.put("remainscore", remainscore);
		infoMap.put("ticketurl", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208427604&idx=1&sn=a3de34b678869c4bbe54547396fcb2a3#rd");
		infoMap.put("scoreurl", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208445618&idx=1&sn=b4d99d5233921ae53c847165c62dec2b#rd");
		return infoMap;
	}

	private String getParkDetail(HttpServletRequest request, Long uin,Long comId,Long authFlag) {
		Long ntime = System.currentTimeMillis()/1000;
		authFlag = daService.getLong("select auth_flag from user_info_tb where id =? ", new Object[]{uin});
		String ret="{";
		Long b = TimeTools.getToDayBeginTime();
		Long e = ntime;
//		long b=1436544000;,e=1435916665;
		Double mmoney = 0d;
		Double cashmoney = 0d;
		Double total = 0d;
		if(authFlag==1){
			ArrayList list1 = new ArrayList();
			ArrayList list2 = new ArrayList();
			list1.add(b);
			list1.add(e);
			list1.add(0);
			list1.add(0);
			list1.add(comId);
			list2.add(b);
			list2.add(e);
			list2.add(0);
			list2.add(4);
			list2.add("停车费%");
			list2.add(comId);
			List park = daService.getAllMap( "select b.nickname, sum(a.amount) total,a.uid from park_account_tb a,user_info_tb b where a.create_time between ? and ? " +
					" and a.type= ? and a.source=? and a.comid=? and a.uid=b.id group by a.uid,b.nickname ",list1);//车场账户停车费
			
			List parkuser = daService.getAllMap( "select b.nickname,sum(a.amount) total,a.uin uid from parkuser_account_tb a,user_info_tb b where a.create_time between ? and ? " +
					" and a.type= ? and a.target=? and a.remark like ? and a.uin=b.id and a.uin in (select id from user_info_tb where comid=?) group by a.uin,b.nickname",list2);//收费员账户停车费
			TreeSet<Long> set = new TreeSet<Long>();
			if(park!=null&&park.size()>0){
				if(parkuser!=null&&parkuser.size()>0)
					park.addAll(parkuser);
				for (int i = 0; i < park.size(); i++) {
//					System.out.println(park.size());
					Map obj1 = (Map)park.get(i);
					Long id1 = Long.valueOf(obj1.get("uid")+"");
					set.add(id1);
					Map cash = daService.getMap( "select sum(amount) total from parkuser_cash_tb where create_time between ? and ? " +
							" and uin=? ",new Object[]{b,e,id1});
					if(cash!=null&&cash.get("total")!=null){
						double cmoney = Double.valueOf(cash.get("total")+"");
						cashmoney+=cmoney;
						obj1.put("cash",StringUtils.formatDouble(cmoney ));//收费员现金收入
					}else{
						obj1.put("cash",0.00);//收费员现金收入
					}
					double ummoney = Double.valueOf(obj1.get("total")+"");
					mmoney+=ummoney;
					for (int j = i+1; j < park.size(); j++) {
//						System.out.println(park.size());
						Map obj2 = (Map)park.get(j);
						long id2 = Long.valueOf(obj2.get("uid")+"");
						if(id1==id2){
							double total1 =Double.valueOf(obj2.get("total")+"");
							mmoney+=total1;
							obj1.put("total", StringUtils.formatDouble(total1+ummoney));
							park.remove(j);
						}
					}
				}
			}else{
				park = parkuser;
				for (Object object : parkuser) {
					Map obj = (Map)object;
					Map cash = daService.getMap( "select sum(amount) total from parkuser_cash_tb where create_time between ? and ? " +
							" and uin=? ",new Object[]{b,e,Long.valueOf(obj.get("uid")+"")});
					set.add(Long.valueOf(obj.get("uid")+""));
					if(cash!=null&&cash.get("total")!=null){
						double cmoney = Double.valueOf(cash.get("total")+"");
						cashmoney+=cmoney;
					}
					if(obj.get("total")!=null)
						mmoney+=Double.valueOf(obj.get("total")+"");
				}
			}
			List user = daService.getAll( "select id, nickname from user_info_tb where comid=?",new Object[]{comId});
			for (Object object : user) {
				Map obj = (Map)object;
				if(set.add(Long.valueOf(obj.get("id")+""))){
					Map cash = daService.getMap( "select sum(amount) total from parkuser_cash_tb where create_time between ? and ? " +
							" and uin=? ",new Object[]{b,e,Long.valueOf(obj.get("id")+"")});
					if(cash!=null&&cash.get("total")!=null){
						double cmoney = Double.valueOf(cash.get("total")+"");
						cashmoney+=cmoney;
						Map tmap = new TreeMap();
						tmap.put("nickname",obj.get("nickname"));
						tmap.put("total",0.0);
						tmap.put("uid",obj.get("id"));
						tmap.put("cash",StringUtils.formatDouble(cmoney ));//收费员现金收入
						park.add(tmap);
					}
				}
			}
			total=cashmoney+mmoney;
			String detail = StringUtils.createJson(park);
			ret+="\"total\":\""+StringUtils.formatDouble(total)+"\",\"mmoeny\":\""+StringUtils.formatDouble(mmoney)+
			"\",\"cashmoney\":\""+StringUtils.formatDouble(cashmoney)+"\",\"detail\":"+detail+"}";
		}else{
			//你没有权限查看
			return "-1";
		}
		logger.error("getparkdetail>>>>："+ret);
		return ret;
	}

	private String akeyCheckAccount(HttpServletRequest request, Long uin,
			Long comId) {
		Long ntime = System.currentTimeMillis()/1000;
		String ret = "{";
		Long b = TimeTools.getToDayBeginTime();
		Long e = ntime+60;
		Double parkmoney = 0d;
		Double parkusermoney = 0d;
		Double cashmoney = 0d;
		Long ordertotal = 0L;
		Long epayordertotal = 0L;
		Double ordertotalmoney = 0d;
		Double epaytotalmoney = 0d;
		//一键对账   1:车场管理员    2收费员
		Map park = daService.getMap( "select sum(amount) total from park_account_tb where create_time between ? and ? " +
				" and type <> ? and uid=? and comid=? ",new Object[]{b,e,1,uin,comId});
		if(park!=null&&park.get("total")!=null)
			parkmoney = Double.valueOf(park.get("total")+"");//车场账户收入（不计来源）
		
		Map parkuser = daService.getMap( "select sum(amount) total from parkuser_account_tb where create_time between ? and ? " +
				" and type= ? and uin = ? ",new Object[]{b,e,0,uin});
		if(parkuser!=null&&parkuser.get("total")!=null)
			parkusermoney = Double.valueOf(parkuser.get("total")+"");//收费员账户收入（不计来源）
		
		Map cash = daService.getMap( "select sum(amount) total from parkuser_cash_tb where create_time between ? and ? " +
				" and uin=? ",new Object[]{b,e,uin});
		if(cash!=null&&cash.get("total")!=null)
			cashmoney = Double.valueOf(cash.get("total")+"");//收费员现金收入
		Map ordertotalMap = daService.getMap( "select count(*) scount,sum(total) total from order_tb where end_time between ? and ? " +
				" and uid=? and state=?",new Object[]{b,e,uin,1});//总的订单
		if(ordertotalMap!=null){
			if(ordertotalMap.get("total")!=null)
				ordertotalmoney = Double.valueOf(ordertotalMap.get("total")+"");
			if(ordertotalMap.get("scount")!=null)
				ordertotal = Long.valueOf(ordertotalMap.get("scount")+"");
		}
		Map epayordertotalMap = daService.getMap( "select count(*) scount,sum(total) total from order_tb where end_time between ? and ? " +
				" and uid=? and c_type=? and state=?",new Object[]{b,e,uin,4,1});//直付订单
		if(epayordertotalMap!=null){
			if(epayordertotalMap.get("total")!=null)
				epaytotalmoney = Double.valueOf(epayordertotalMap.get("total")+"");
			if(epayordertotalMap.get("scount")!=null)
				epayordertotal = Long.valueOf(epayordertotalMap.get("scount")+"");
		}
		ret+="\"totalmoney\":\""+StringUtils.formatDouble((parkmoney+parkusermoney+cashmoney))+"\",\"mobilemoney\":\""+StringUtils.formatDouble((parkmoney+parkusermoney))+
		"\",\"cashmoney\":\""+StringUtils.formatDouble(cashmoney)+"\",\"mycount\":\""+StringUtils.formatDouble(parkusermoney)+"\",\"parkaccout\":\""+StringUtils.formatDouble(parkmoney)+"\",\"timeordercount\":\""+
		(ordertotal-epayordertotal)+"\",\"timeordermoney\":\""+StringUtils.formatDouble((ordertotalmoney-epaytotalmoney))+
		"\",\"epayordercount\":\""+epayordertotal+"\",\"epaymoney\":\""+StringUtils.formatDouble(epaytotalmoney)+"\"}";
		logger.error("akeycheckaccount>>>："+ret);
		return ret;
	}

	private String goOffWork(HttpServletRequest request, Long uin) {
		Long  worksiteid =RequestUtil.getLong(request, "worksiteid",-1L);
		if(worksiteid <= 0){
			return "-1";
		}
		long endtime = System.currentTimeMillis() / 1000;
		int result = daService.update("update parkuser_work_record_tb set end_time=? where worksite_id = ? and uid = ? and end_time is null",
						new Object[] { endtime, worksiteid, uin });
		logger.error("gooffwork>>>>>下班result："+result+",uin:"+uin+",worksiteid:"+worksiteid);
		if(result > 0){
			return "1";
		}else{
			return "-1";
		}
	}

	private String bindWorkSite(HttpServletRequest request, Long uin) {
		Long wid = RequestUtil.getLong(request, "wid", -1L);
		logger.error(">>>>disbind,wid:"+wid);
		int ret =0;
		if(uin!=-1){
			if(wid==-1){//解绑
				ret = daService.update("delete from user_worksite_tb where uin = ? ", new Object[]{uin});
				logger.error(">>>>disbind  收费员解绑   worksite,user:"+uin+"ret:"+ret);
				ret = 1;
			}else {//绑定
				//绑定前先从其它工作站上下岗
				ret = daService.update("delete from user_worksite_tb where uin = ?  ", new Object[]{uin});
				logger.error(">>>>bind 收费员上岗，删除原来在的工作站:"+ret);
				//删除原绑定收费员
				Map oldMap = daService.getMap("select uin from user_worksite_tb where worksite_id=? ", new Object[]{wid});
				if(oldMap!=null){
					ret = daService.update("delete from user_worksite_tb where worksite_id = ?  ", new Object[]{wid});
					if(ret>0){
						Long uid =(Long)oldMap.get("uin");
						if(uid!=null&&uid>0)
							ret = daService.update("insert into order_message_tb(message_type,state,uin)" +
								" values(?,?,?)", new Object[]{4,0,uid});//发消息给收费员，通知其已不在岗
						logger.error(">>>>disbind 收费员上岗，原收费员下岗  worksite,delete old user:"+uid+",ret:"+ret);
					}
				}
				//绑定收费员
				ret = daService.update("insert into user_worksite_tb (worksite_id,uin) values(?,?)",new Object[]{wid,uin});
				logger.error(">>>>bind worksite,收费员上岗  bind new user:"+uin+", ret="+ret);
			}
		}
		return "{\"result\":\""+ret+"\"}";
	}

	private String incomAnly(HttpServletRequest request, Long uin,Long comId) {
		//0自己,1车场
		Integer acctype = RequestUtil.getInteger(request, "acctype", 0);
		//0停车费，1返现 ，2奖金,3 全部
		Integer income = RequestUtil.getInteger(request, "incom", 0);
		//0今天，1昨天，2本周，3本月
		Integer datetype = RequestUtil.getInteger(request, "datetype", 0);
		Integer page = RequestUtil.getInteger(request, "page", 1);
		page = page<1?1:page;
		List<Object> params = new ArrayList<Object>();
		
		String sql = "";
		String totalSql = "";
		if(acctype==0){//0自己,1车场
			sql +="select amount money,type mtype,create_time," +
					"remark note,target from parkuser_account_tb where uin=? ";
			totalSql = "select sum(amount) total from parkuser_account_tb where uin=?";
			params.add(uin);
		}else if(acctype==1){
			sql +=" select create_time ,remark r,amount money,type mtype  from park_account_tb where comid=? ";
			totalSql = "select sum(amount) total from park_account_tb where comid=?";
			params.add(comId);
		}
		if(income==0){//0停车费
			if(acctype==0){//0自己,1车场
				sql +=" and type=? and target=? ";
				totalSql +=" and type=? and target=? ";
				params.add(0);
				params.add(4);
			}else if(acctype==1){
				sql +=" and type= ? ";
				totalSql +=" and type= ? ";
				params.add(0);
			}
		}else if(income==1){//1返现 
			if(acctype==0){//0自己,1车场
				sql +=" and type=? and target=? and amount =? ";
				totalSql +=" and type=? and target=? and amount =? ";
				params.add(0);
				params.add(3);
				params.add(2d);
			}else if(acctype==1){
				sql +=" and type= ? ";
				totalSql +=" and type= ? ";
				params.add(2);
			}
		}else if(income==2){//2奖金
			if(acctype==0){//0自己,1车场
				sql +=" and type=? and target=? and amount >? ";
				totalSql +=" and type=? and target=? and amount >? ";
				params.add(0);
				params.add(3);
				params.add(2d);
			}else if(acctype==1){
				sql +=" and type= ? ";
				totalSql +=" and type= ? ";
				params.add(3);
			}
		}
		
		Long btime = TimeTools.getToDayBeginTime();
		Long etime = btime+24*60*60;
		if(datetype==1){
			etime = btime ;
			btime = btime-24*60*60;
		}else if(datetype==2){
			btime = TimeTools.getWeekStartSeconds();
		}else if(datetype==3){
			btime = TimeTools.getMonthStartSeconds();
		}
		sql +=" and create_time between ? and ? order by create_time desc";
		totalSql +=" and create_time between ? and ? ";
		params.add(btime);
		params.add(etime);
//		System.out.println(sql);
//		System.out.println(totalSql);
		System.err.println(">>>>>>incomanly:"+sql+":"+params);
		Map totalMap = pService.getMap(totalSql, params);
		List reslutList = pService.getAll(sql, params,page,20);	
		setAccountList(reslutList,acctype);
		String total = totalMap.get("total")+"";
		if(total.equals("null"))
			total = "0.0";
		String reslut =  "{\"total\":\""+total+"\",\"info\":"+StringUtils.createJson(reslutList)+"}";
		System.err.println(reslut);
		return reslut;
	}

	private String queryCarPics(HttpServletRequest request, Long uin,Long comId) {
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		String sql = "select distinct(car_number) from order_tb where comid=? and c_type=? and create_time between ? and ? ";
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
		String nowtime= df2.format(System.currentTimeMillis());
		Long endTime = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(nowtime+" 23:59:59");
		Long beginTime = endTime - 30*24*60*60;
		list = pService.getAll(sql, new Object[]{comId,2,beginTime,endTime});
		String result = StringUtils.createJson(list);
		return result;
	}

	private String getNewIncome(HttpServletRequest request, Long uin) {
		String lock = null;
		Long ntime = System.currentTimeMillis()/1000;
		Map<String, Object> map = new HashMap<String, Object>();
		String btime = RequestUtil.processParams(request, "btime");
		String etime = RequestUtil.processParams(request, "etime");
		Long logonTime = RequestUtil.getLong(request, "logontime", -1L);//20150618加上传入登录时间 
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
		Long worksiteid = RequestUtil.getLong(request, "worksite_id",-1L);
		Long comid = RequestUtil.getLong(request, "comid",-1L);
		String nowtime= df2.format(System.currentTimeMillis());
		Long b = ntime;
		Long e =b;
		if(logonTime!=-1){
			b = logonTime;
			//logger.error(b);
			b = (logonTime/60)*60;
		}else {
			if(btime.equals("")){
				btime = nowtime;
			}
			if(etime.equals("")){
				etime = nowtime;
			}
			b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
			e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
		}
		if(worksiteid!=-1){
			lock = "parkuser_work_record_tb" + uin;
			if(!memcacheUtils.addLock(lock, 5)){//10s调一次
				logger.error("getnewincome lock:"+lock+",5秒钟内不能重复调用...");
				return StringUtils.createJson(map);
			}
			Map ret = pService.getMap(
					"select * from parkuser_work_record_tb where end_time is null and uid=? and worksite_id = ?",
					new Object[] {uin,worksiteid});
			if(ret!=null){
				b = Long.valueOf(ret.get("start_time")+"");
				map.put("start_time", b);
				if(ret.get("end_time")==null){
					e=Long.MAX_VALUE;
				}else{
					e=Long.valueOf(ret.get("end_time")+"");
				}
			}
		}
		if(b > ntime - 10 * 24 * 60 * 60 && e > b){
			Map<String, Object> incomeMap = commonMethods.getIncome(b, e, uin);
			Double prepay_cash = Double.valueOf(incomeMap.get("prepay_cash") + "");//现金预支付
			Double add_cash = Double.valueOf(incomeMap.get("add_cash") + "");//现金补缴
			Double refund_cash = Double.valueOf(incomeMap.get("refund_cash") + "");//现金退款
			Double pursue_cash = Double.valueOf(incomeMap.get("pursue_cash") + "");//现金追缴
			Double pfee_cash = Double.valueOf(incomeMap.get("pfee_cash") + "");//现金停车费（非预付）
			Double prepay_epay = Double.valueOf(incomeMap.get("prepay_epay") + "");//电子预支付
			Double add_epay = Double.valueOf(incomeMap.get("add_epay") + "");//电子补缴
			Double refund_epay = Double.valueOf(incomeMap.get("refund_epay") + "");//电子退款
			Double pursue_epay = Double.valueOf(incomeMap.get("pursue_epay") + "");//电子追缴
			Double pfee_epay = Double.valueOf(incomeMap.get("pfee_epay") + "");//电子停车费（非预付）
			Double cash = StringUtils.formatDouble(pfee_cash + pursue_cash + prepay_cash + add_cash);
			Double epay = StringUtils.formatDouble(pfee_epay + pursue_epay + prepay_epay + add_epay);
			map.put("cashpay", cash);
			map.put("mobilepay", epay);
		}else{
			logger.error("b:"+b+",e:"+e+",ntime:"+ntime);
		}
		logger.error("getnewincome,uid:"+uin+",return:"+map);
		return StringUtils.createJson(map);
	}

	private String getIncome(HttpServletRequest request, Long uin) {
		Long ntime = System.currentTimeMillis()/1000;
		String btime = RequestUtil.processParams(request, "btime");
		String etime = RequestUtil.processParams(request, "etime");
		Long logonTime = RequestUtil.getLong(request, "logontime", -1L);//20150618加上传入登录时间 
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
		String nowtime= df2.format(System.currentTimeMillis());
		Long b = ntime;
		Long e =b;
		if(logonTime!=-1){
			b = logonTime;
			//logger.error(b);
			b = (logonTime/60)*60;
		}else {
			if(btime.equals("")){
				btime = nowtime;
			}
			if(etime.equals("")){
				etime = nowtime;
			}
			b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
			e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
		}
		//logger.error(b);
		String sql = "select sum(total) money,pay_type from order_tb where create_time>? and  " +
				"uid=? and c_type=? and state=? and end_time between ? and ? " +
				"group by pay_type order by pay_type desc ";
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		list = pService.getAll(sql, new Object[]{ntime-30*86400,uin,3,1,b,e});
		Map<String, Object> map = new HashMap<String, Object>();
		for(Map<String, Object> map2 : list){
			Integer pay_type = (Integer)map2.get("pay_type");
			if(pay_type == 2){//手机支付
				map.put("mobilepay", map2.get("money"));
			}else if(pay_type == 1){//现金支付
				map.put("cashpay", map2.get("money"));
			}
		}
		//logger.error(map);
		return StringUtils.createJson(map);
	}

	private String getMesg(HttpServletRequest request, Long uin) {
		Long maxid = RequestUtil.getLong(request, "maxid", -1L);
		Integer page = RequestUtil.getInteger(request, "page", 1);
		if(maxid>-1){
			Long count = daService.getLong("select count(ID) from parkuser_message_tb where uin=? and id>?", new Object[]{uin,maxid});
			return count+"";
		}else{
			List<Object> params = new ArrayList<Object>();
			params.add(uin);
			List<Map<String, Object>> list = daService.getAll("select * from parkuser_message_tb where uin=? order by id desc",
					params,page,10);
			return StringUtils.createJson(list);
		}
		//http://127.0.0.1/zld/carowner.do?action=getmesg&token=&page=-1&maxid=0
	}

	private String recominfo(HttpServletRequest request, Long uin) {
		Integer rtype = RequestUtil.getInteger(request, "type", 0);//0:车主，1:车场
		List<Map<String, Object>> list =null;
		if(rtype==0){
			list = daService.getAll("select c.nid,u.mobile uin,c.state,c.money from recommend_tb c left join user_info_tb u on c.nid=u.id where pid=? and c.type=? order by c.id desc ",new Object[]{uin,rtype});
		}else {
			list  =daService.getAll("select nid uin,state,money from recommend_tb where pid=? and type=? order by id desc",new Object[]{uin,rtype});
		}
		if(list!=null&&!list.isEmpty()){
			for(Map<String, Object> map :list){
				Integer state = (Integer)map.get("state");
				if(state==null||state!=1)
					continue;
				Double money = StringUtils.formatDouble(map.get("money"));
				if(rtype==0&&money==0)
					map.put("money", 5);
				else if(rtype==1&&money==0){
					map.put("money", 30);
				}
			}
		}
		return StringUtils.createJson(list);
		//http://127.0.0.1/zld/collectorrequest.do?action=recominfo&token=40ffacdad78acf0c43e0aabae9712602
	}

	private String regcolmsg(HttpServletRequest request, Long uin, Long comId,String out) {
		Long ntime = System.currentTimeMillis()/1000;
		System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>短信推荐车场+"+uin);
		Long tid = daService.getkey("seq_transfer_url_tb");
		//String url = "http://192.168.199.240/zld/turl?p="+tid;
		String url = "http://s.tingchebao.com/zld/turl?p="+tid;
		int result = daService.update("insert into transfer_url_tb(id,url,ctime,state) values (?,?,?,?)",
				new Object[]{tid,"regparker.do?action=toregpage&recomcode="+uin,
						ntime,0});
		
		if(result!=1)
			url="推荐失败!";
		if(out.equals("json")){
			return  "{\"url\":\""+url+"\"}";
		}else {
			return url;
		}
		//http://127.0.0.1/zld/collectorrequest.do?action=regcarmsg&token=6ed161cde6c7149de49d72719f2eb39b
	}

	private String reguser(HttpServletRequest request, Long uin, Long comId) {
		Long ntime = System.currentTimeMillis()/1000;
		String carNumber =AjaxUtil.decodeUTF8( RequestUtil.getString(request, "carnumber"));
		carNumber = carNumber.toUpperCase().trim();
		carNumber = carNumber.replace("I", "1").replace("O", "0");
		String mobile = RequestUtil.getString(request, "mobile");
		Long curTime = System.currentTimeMillis()/1000;
		if(!carNumber.equals("")){
			if(mobile.equals("")){//验证车牌号
				Long count = daService.getLong("select count(id) from car_info_tb where car_number=?", new Object[]{carNumber});
				if(count>0){
					return "-1";
				}
			}else {//注册车主，同时只验证手机号
				Long count = daService.getLong("select count(id) from user_info_tb where mobile=? and auth_flag=? ", new Object[]{mobile,4});
				if(count>0){
					return "-1";
				}
				//写用户数据
				List<Map<String, Object>> sqlList = new ArrayList<Map<String,Object>>();
				//用户信息
				Map<String, Object> userSqlMap = new HashMap<String, Object>();
				//下一个用户账号
				Long key = daService.getkey("seq_user_info_tb");
				userSqlMap.put("sql", "insert into user_info_tb (id,nickname,strid,mobile,reg_time,comid,auth_flag,recom_code,media) " +
						"values(?,?,?,?,?,?,?,?,?)");
				userSqlMap.put("values", new Object[]{key,"车主","zlduser"+key,mobile,ntime,0,4,uin,999});
				sqlList.add(userSqlMap);
				//车牌信息
				Map<String, Object> carSqlMap = new HashMap<String, Object>();
				carSqlMap.put("sql", "insert into car_info_tb(uin,car_number,create_time) values(?,?,?)");
				carSqlMap.put("values", new Object[]{key,carNumber,curTime});
				sqlList.add(carSqlMap);
				//推荐信息
				Map<String, Object> recomSqlMap = new HashMap<String, Object>();
				recomSqlMap.put("sql", "insert into recommend_tb (pid,nid,type,state,create_time) values(?,?,?,?,?)");
				recomSqlMap.put("values", new Object[]{uin,key,0,0,ntime});
				sqlList.add(recomSqlMap);
				
				boolean ret = daService.bathUpdate(sqlList);
				if(!ret){
					return "-2";
				}else {//发给车主30元停车券
					//推荐车主，收费员积1分
					//logService.updateScroe(5, uin, comId);
					int result=publicMethods.backNewUserTickets(ntime, key);// daService.bathInsert(tsql, values, new int[]{4,4,4,4,4});
					if(result==0){
						/*String bsql = "insert into bonus_record_tb (bid,ctime,mobile,state,amount) values(?,?,?,?,?) ";
						Object [] values = new Object[]{999,ntime,mobile,0,10};//登记为未领取红包，登录时写入停车券表（判断是否是黑名单后）
						logger.error(">>>>>>>>收费员推荐车主("+mobile+")，发30元停车券，写入红包记录表，登录时返还："+daService.update(bsql,values));*/
					
					}
					int	eb = daService.update("insert into user_profile_tb (uin,low_recharge,limit_money,auto_cash," +
							"create_time,update_time) values(?,?,?,?,?,?)", 
							new Object[]{key,10,25,1,ntime,ntime});
					
					logger.error("账户:"+uin+",手机："+mobile+",新注册用户(车场收费员推荐)，写入红包停车券"+result+"条,自动支付写入："+eb);
					String mesg ="五笔电子停车费，三笔来自停车宝。停车天天有优惠，8元钱停5次车，下载地址： http://t.cn/RZJ4UAv 【停车宝】";
					SendMessage.sendMultiMessage(mobile, mesg);
				}
			}
		}else {
			return  "0";
		}
		//http://127.0.0.1/zld/collectorrequest.do?action=reguser&token=6ed161cde6c7149de49d72719f2eb39b&mobile=15801482645&carnumber=123456
		return  "1";
	}

	private String uploadll(HttpServletRequest request, Long uin, Long comId,Long authFlag) {
		Long ntime = System.currentTimeMillis()/1000;
		authFlag = daService.getLong("select auth_flag from user_info_tb where id =? ", new Object[]{uin});
		Double lon = RequestUtil.getDouble(request, "lon", 0d);
		Double lat = RequestUtil.getDouble(request, "lat", 0d);
		if(lat==0||lon==0){
			return "0";
		}else if(comId==null||comId<1){
			return "0";
		}
		Map comMap = daService.getMap("select longitude,latitude from com_info_Tb where id =? ", new Object[]{comId});
		Integer isOnseat = 0;
		Double distance =1000.0;
		if(comMap!=null&&comMap.get("longitude")!=null){
			Double lon1 = Double.valueOf(comMap.get("longitude")+"");
			Double lat1 = Double.valueOf(comMap.get("latitude")+"");
			distance = StringUtils.distance(lon, lat, lon1, lat1);
			if(distance<500){//在车场500米范围内时，认为在位。
				isOnseat = 1;
			}
			logger.error(">>>>>parkuser distance,uin:"+uin+",dis:"+distance+",authflag:"+authFlag);
		}
		//更新收费员在位信息，23表示在位
		if(authFlag!=13){//泊车员上传不改状态
			daService.update("update user_info_tb set online_flag =? where id=? ", new Object[]{22+isOnseat,uin});
		}
		//写入位置上传日志 
		int result = daService.update("insert into user_local_tb (uid,lon,lat,distance,is_onseat,ctime) values(?,?,?,?,?,?)",
				new Object[]{uin,lon,lat,distance,isOnseat,ntime});
		Long count = daService.getLong("select count(id) from user_info_Tb where comid =? and online_flag=? ", new Object[]{comId,23});
		if(count>0){//有收费员在位,更新车场是否有收费员在位标志
			daService.update("update com_info_tb set is_hasparker=?, update_time=? where id = ? and is_hasparker=? ", new Object[]{1,ntime,comId,0});
		}else {
			daService.update("update com_info_tb set is_hasparker=?, update_time=? where id = ? and is_hasparker=?", new Object[]{0,ntime,comId,1});
		}
		//http://127.0.0.1/zld/collectorrequest.do?action=uploadll&token=aa9a48d2f41bb2722f29c8714cbc754c&lon=&lat=
		return ""+result;
	}

	private String editpBank(HttpServletRequest request, Long uin, Long comId) {

		Long id = RequestUtil.getLong(request, "id", -1L);
		String name =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
		String card_number =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "card_number"));
		String mobile =RequestUtil.processParams(request, "mobile");
		String bank_name =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "bank_name"));
		String area =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "area"));
		String bank_point =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "bank_pint"));
		String userId =RequestUtil.processParams(request, "user_id");
		Integer atype = RequestUtil.getInteger(request, "atype", 0);//0银行卡，1支付宝，2微信
		int result = 0;
		if(!card_number.equals("")&&!mobile.equals("")&&!bank_name.equals("")&&id!=-1){
			result = daService.update("update com_account_tb set name=?,card_number=?,mobile=?,bank_name=?," +
					"area=?,bank_pint=?,atype=?,user_id=? where id = ? and type=? ",
					new Object[]{name,card_number,mobile,bank_name,area,bank_point,atype,userId,id,0});
		}
		//http://127.0.0.1/zld/collectorrequest.do?action=editpbank&token=aa9a48d2f41bb2722f29c8714cbc754c
		//&name=&card_number=&mobile=&bank_name=&area=&bank_point=&atype=&note=user_id=&id=
		return  result+"";
	}

	private String addParkBank(HttpServletRequest request, Long uin, Long comId) {
		String name =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
//		Long uin =RequestUtil.getLong(request, "uin",-1L);
		String card_number =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "card_number"));
		String mobile =RequestUtil.processParams(request, "mobile");
		String userId =RequestUtil.processParams(request, "user_id");
		String bank_name =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "bank_name"));
		String area =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "area"));
		String bank_point =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "bank_pint"));
		int result = 0;
		if(!card_number.equals("")&&!mobile.equals("")&&!bank_name.equals("")){
			result = daService.update("insert into com_account_tb (comid,uin,name,card_number,mobile," +
					"bank_name,atype,area,bank_pint,type,state,user_id)" +
					" values(?,?,?,?,?,?,?,?,?,?,?,?)",
					new Object[]{comId,uin,name,card_number,mobile,bank_name,0,area,bank_point,0,0,userId});
		}
		//http://127.0.0.1/zld/collectorrequest.do?action=addparkbank&token=aa9a48d2f41bb2722f29c8714cbc754c
		//&name=&card_number=&mobile=&bank_name=&area=&bank_point=&atype=&note=
		logger.error(result);
		return  result+"";
	}

	private String withdraw(HttpServletRequest request, Long uin,Long comId) {
		Long ntime = System.currentTimeMillis()/1000;
		String result ="";
		Double money = RequestUtil.getDouble(request, "money", 0d);
		Long count = daService.getLong("select count(*) from park_account_tb where comid= ? and create_time>? and type=?  ", 
				new Object[]{comId,TimeTools.getToDayBeginTime(),1}) ;
		if(count>2){//每天只能三次
			result= "{\"result\":-2,\"times\":"+count+"}";
			return result;
		}
		
		List<Map> accList = daService.getAll("select id,type from com_account_tb where comid =? and type in(?,?) and state =? order by id desc",
				new Object[]{comId,0,2,0});
		Long accId = null;
		Integer type =0;
		if(accList!=null&&!accList.isEmpty()){
			accId = null;
			for(Map m: accList){
				type = (Integer)m.get("type");
				if(type!=null&&type==2){
					accId =  (Long)m.get("id");	
					break;
				}
			}
			if(accId==null)
				accId=(Long)accList.get(0).get("id");
		}else{
			//没有设置银行账户
			result=  "{\"result\":-1,\"times\":0}";
			return result;
		}
		//提现操作
		boolean isupdate =false;
		if(money>0){
			Map userMap = daService.getMap("select money from com_info_Tb where id=? ", new Object[]{comId});
			//用户余额
			Double balance =StringUtils.formatDouble(userMap.get("money"));
			if(money<=balance){//提现金额不大于余额
				//扣除帐号余额//写提现申请表
				List<Map<String, Object>> sqlList = new ArrayList<Map<String,Object>>();
				Map<String, Object> userSqlMap = new HashMap<String, Object>();
				userSqlMap.put("sql", "update com_info_Tb set money = money-? where id= ?");
				userSqlMap.put("values", new Object[]{money,comId});
				Map<String, Object> withdrawSqlMap = new HashMap<String, Object>();
				withdrawSqlMap.put("sql", "insert into withdrawer_tb  (comid,amount,create_time,acc_id,uin,wtype) values(?,?,?,?,?,?)");
				withdrawSqlMap.put("values", new Object[]{comId,money,ntime,accId,uin,type});
				Map<String, Object> moneySqlMap = new HashMap<String, Object>();
				moneySqlMap.put("sql", "insert into park_account_tb (comid,amount,create_time,type,remark,uid,source) values(?,?,?,?,?,?,?)");
				moneySqlMap.put("values", new Object[]{comId,money,ntime,1,"提现",uin,5});
				sqlList.add(userSqlMap);
				sqlList.add(withdrawSqlMap);
				sqlList.add(moneySqlMap);
				isupdate = daService.bathUpdate(sqlList);
			}
			if(isupdate)
				result="{\"result\":1,\"times\":"+count+"}";
			else {
				result="{\"result\":0,\"times\":"+count+"}";
			}
		}
		return result;
	}

	/**
	 * //账户明细
	 * @param request
	 * @param comId
	 * @return
	 */
	private String getpaDetail(HttpServletRequest request, Long comId) {

		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "size", 20);
		Long stype=RequestUtil.getLong(request, "stype", -1L);//0:收入，1提现
		String sql = "select create_time ,remark r,amount money,type mtype  from park_account_tb where comid=?";
		String countSql = "select count(id)  from park_account_tb where comid=?";
		List<Object> params = new ArrayList<Object>();
		params.add(comId);
		
		if(stype>-1){
			if(stype==1){//提现
				sql +=" and type=? ";
				countSql +=" and type=? ";
				params.add(stype);
			}else {//收入或停车宝返现 
				sql +=" and type in(?,?) ";
				countSql +=" and type in(?,?) ";
				params.add(stype);
				params.add(2L);
			}
		}
		Long count= daService.getCount(countSql, params);
		List pamList = null;//daService.getPage(sql, null, 1, 20);
		if(count>0){
			pamList = daService.getAll(sql+" order by id desc ", params, pageNum, pageSize);
		}
		
		if(pamList!=null&&!pamList.isEmpty()){
			for(int i=0;i<pamList.size();i++){
				Map map = (Map)pamList.get(i);
				Integer type = (Integer)map.get("mtype");
				String remark = (String)map.get("r");
				if(type==0){
					if(remark.indexOf("_")!=-1){
						map.put("note", remark.split("_")[0]);
						map.put("target", remark.split("_")[1]);
					}
				}else if(type==1){
					map.put("note", "提现");
					map.put("target", "银行卡");
				}else if(type==2){
					map.put("note", "返现");
					map.put("target", "停车宝");
					map.put("mtype", 1);
				}
				map.remove("r");
			}
		}
		String reslut =  "{\"count\":"+count+",\"info\":"+StringUtils.createJson(pamList)+"}";
		return reslut;
	}

	/**一键查询
	 * @param request
	 * @param uin
	 * @param comId
	 * @return
	 */
	private String corder(HttpServletRequest request, Long comId) {
		Long ntime = System.currentTimeMillis()/1000;
		/*
		 * 车场里目前停了多少车：（当前订单数量）
			今日已离场车场：（今日历史订单数量）
			今日已经收到金额：（今日历史订单总金额）
		 */
		Long btime = TimeTools.getToDayBeginTime();
		Long etime = ntime;
		Long ccount =0L;//当前订单数
		int ocount =0;//已结算订单数
		Long tcount = 0L;//今日当前订单数
		Double total =0d;
		List<Map<String, Object>> orderList = daService.getAll("select  total,state from order_tb where create_time>? and comid=? and end_time between ? and ? and state=? ",
				new Object[]{ntime-30*86400,comId,btime,etime,1});
		if(orderList!=null){
			ocount=orderList.size();
			for(Map<String,Object> map: orderList){
				total += Double.valueOf(map.get("total")+"");
			}
		}
		ccount = pService.getLong("select count(*) from order_tb where create_time>? and  comid=? and state=? ",
				new Object[]{ntime-30*86400,comId,0});
		tcount = pService.getLong("select count(1) from order_tb where create_time between ? and ? and comid=? and state=? "
				, new Object[]{btime,etime,comId,0});
		String result = "{\"ccount\":\""+ccount+"\",\"ocount\":\""+ocount+"\",\"tcount\":\""+tcount+"\",\"total\":\""+StringUtils.formatDouble(total)+"\"}";		
		//test:http://127.0.0.1/zld/collectorrequest.do?action=corder&token=b4e6727f914157c8745f6f2c023c8c96
		return result;
	}

	/**
	 * 车场信息
	 * @param request
	 * @param comId
	 * @param infoMap
	 */
	private void comInfo(HttpServletRequest request,Long comId,
			Map<String, Object> infoMap) {

		Map comMap = daService.getPojo("select * from com_info_tb where id=?", new Object[]{comId});
		List<Map<String, Object>> picMap = daService.getAll("select picurl from com_picturs_tb where comid=? order by id desc limit ? ",
				new Object[]{comId,1});
		String picUrls = "";
		if(picMap!=null&&!picMap.isEmpty()){
			for(Map<String, Object> map : picMap){
				picUrls +=map.get("picurl")+";";
			}
			if(picUrls.endsWith(";"))
				picUrls = picUrls.substring(0,picUrls.length()-1);
		}
		if(comMap!=null&&comMap.get("id")!=null){
			String mobile = (String)comMap.get("mobile");
			String phone = (String)comMap.get("phone");
			Integer city = (Integer)comMap.get("city");
			if(phone==null||phone.equals(""))
				phone = mobile;
			Map priceMap = getPriceMap(comId);
			String timeBetween = "";
			Double price = 0d;
			if(priceMap!=null){
				Integer payType = (Integer)priceMap.get("pay_type");
				if(payType==0){
					Integer start = (Integer)priceMap.get("b_time");
					Integer end = (Integer)priceMap.get("e_time");
					if(start<10&&end<10)
						timeBetween = "0"+start+":00-0"+end+":00";
					else if(start<10&&end>9){
						timeBetween = "0"+start+":00-"+end+":00";
					}else if(start>9){
						timeBetween = start+":00-"+end+":00";
					}
				}else {
					timeBetween = "00:00-24:00";
				}
				if(priceMap.get("price")!=null)
					price = Double.valueOf(priceMap.get("price")+"");
			}
			Integer parkType = (Integer)comMap.get("parking_type");
			parkType = parkType==null?0:parkType;
			String ptype = "地面";
			if(parkType==1)
				ptype="地下";
			else if(parkType==2){
				ptype="占道";
			}
			Integer stopType = (Integer)comMap.get("stop_type");
			String sType = "平面排列";
			if(stopType==1)
				sType="立体排列";
			infoMap.put("name", comMap.get("company_name"));
			infoMap.put("address", comMap.get("address"));
			infoMap.put("parkingtotal", comMap.get("parking_total"));
			infoMap.put("parktype",ptype);
			infoMap.put("phone", phone);
			infoMap.put("timebet", timeBetween);
			infoMap.put("price", price);
			infoMap.put("stoptype", sType);
			infoMap.put("service", "人工服务");
			infoMap.put("id", comId);
			infoMap.put("resume", comMap.get("resume")==null?"":comMap.get("resume"));
			infoMap.put("longitude", comMap.get("longitude"));
			infoMap.put("latitude", comMap.get("latitude"));
			infoMap.put("isfixed", comMap.get("isfixed"));
			infoMap.put("picurls",picUrls);
			List<Map<String, Object>> carTypeList = commonMethods.getCarType(comId);
			String carTypes = StringUtils.createJson(carTypeList);
			carTypes = carTypes.replace("value_no", "id").replace("value_name", "name");
			infoMap.put("car_type", comMap.get("car_type"));
			infoMap.put("allCarTypes", carTypes);
			infoMap.put("passfree", comMap.get("passfree"));
			infoMap.put("ishdmoney", comMap.get("ishdmoney"));
			infoMap.put("ishidehdbutton", comMap.get("ishidehdbutton"));
			infoMap.put("issuplocal", 0);
			infoMap.put("fullset",comMap.get("full_set"));//车位已满能否进场
			infoMap.put("leaveset",comMap.get("leave_set"));//车场识别识别抬杆设置  （有的月卡车场没人收费（不收费））
			infoMap.put("liftreason",getLiftReason(comId));
			List list = daService.getAll("select id as value_no,name as value_name from free_reasons_tb where comid=? order by sort , id desc ", new Object[]{comId});
			infoMap.put("freereasons",list);
			String swith=publicMethods.getCollectMesgSwith();
			if("1".equals(swith)){
				if(city!=null&&city==110000)//0605仅通知北京的收费员
					infoMap.put("mesgurl", "collectmesg.png");
				else {//通知济南以外的收费员
					infoMap.put("mesgurl", "collectmesg_jn.png");
				}
			}
		}else {
			infoMap.put("info", "token is invalid");
		}
	}

	private String getLiftReason(Long comid) {
		String reason = CustomDefind.getValue("LIFTRODREASON"+comid);
		String ret = "[";
		if(reason!=null){
			String res[] = reason.split("\\|");
			for(int i=0;i<res.length;i++){
				ret+="{value_no:"+i+",value_name:\""+res[i]+"\"},";
			}
		}
		if(ret.endsWith(","))
			ret = ret.substring(0,ret.length()-1);
		ret +="]";
		return ret;
	}
	
	/**
	 * 免费发行
	 * @param request
	 * @param uin
	 * @param comId
	 * @return
	 */
	private String freeOrder(HttpServletRequest request, Long uin, Long comId) {
		Long ntime = System.currentTimeMillis()/1000;
		Long orderId = RequestUtil.getLong(request, "orderid", -1L);
		Long out_passid = RequestUtil.getLong(request, "passid", -1L);//出口通道id
		Long isPolice = RequestUtil.getLong(request, "isPolice", -1L);//是否军警车
		Long freereasons = RequestUtil.getLong(request, "freereasons", -1L);//免费原因
		int result =0;
		if(orderId != -1){
			logger.error("收费员："+uin+"把订单："+orderId+"置为免费放行:"+freereasons);
			if(isPolice==1){
				result = daService.update("update order_tb set total=?,state=?,end_time=?,out_passid=?,uid=?,isclick=?,freereasons=? where id=? ",
						new Object[]{0,1,ntime,out_passid,uin,0,freereasons,orderId});
			}else{
				//计算应收价格
				Map map = daService.getPojo("select * from order_tb where id=? ", new Object[]{orderId});
				Integer pid = (Integer)map.get("pid");
				Integer car_type = (Integer)map.get("car_type");//0：通用，1：小车，2：大车
				Long start= (Long)map.get("create_time");
				Long end =  ntime;
				Double total  = 0d;
				if(map.get("end_time") != null){
					end = (Long)map.get("end_time");
				}
				Map ordermap = commonMethods.getOrderInfo(orderId, -1L, end);
				total = Double.valueOf(ordermap.get("aftertotal") + "");
				String sql = "update order_tb set total=?,state=?,end_time=?,pay_type=?,out_passid=?,uid=?,freereasons=? where id=? ";
				Object []values = new Object[]{total,1,ntime,8,out_passid,uin,freereasons,orderId};
				Integer isClick = (Integer)map.get("isclick");
				if(isClick==null||isClick!=1){
					sql ="update order_tb set total=?,state=?,end_time=?,pay_type=?,out_passid=?,uid=?,freereasons=?,isclick=? where id=? ";
					values = new Object[]{total,1,ntime,8,out_passid,uin,freereasons,0,orderId};
				}
				result = daService.update(sql,values);
			}
			
			int r = daService.update("update parkuser_cash_tb set amount=? where orderid=? and type=? ",
					new Object[] { 0, orderId, 0 });
			logger.error("freeorder>>>>现金收费明细置为0，orderid:"+orderId+",r:"+r);
		}
		return result+"";
	}
	
	private String manuPayPosOrder(HttpServletRequest request, Long uid, Integer payType, Long groupId){
		try {
			Long orderId = RequestUtil.getLong(request, "orderid", -1L);
			Double total = RequestUtil.getDouble(request, "total", 0d);
			String imei  =  RequestUtil.getString(request, "imei");
			Integer version = RequestUtil.getInteger(request, "version", 0);//版本号2.0以上返回json数据
			String nfc_uuid = RequestUtil.processParams(request, "uuid");//刷卡支付的卡片编号
			Integer bindcard = RequestUtil.getInteger(request, "bindcard", 0);//0:客户端弹出绑定车主手机号的提示框 1：不弹出提示框直接刷卡预付
			Long endtime = RequestUtil.getLong(request, "endtime", -1L);//订单结算时间，即生成结算金额的时间
			//-----------------------------废弃参数---------------------------------//
			Integer workid = RequestUtil.getInteger(request, "workid",0);//工作班号
			Long berthorderid = RequestUtil.getLong(request, "berthorderid", -1L);
			//-----------------------------具体逻辑 --------------------------------//
			ManuPayPosOrderFacadeReq req = new ManuPayPosOrderFacadeReq();
			req.setOrderId(orderId);
			req.setMoney(total);
			req.setImei(imei);
			req.setUid(uid);
			req.setVersion(version);
			req.setPayType(payType);
			req.setNfc_uuid(nfc_uuid);
			req.setBindcard(bindcard);
			req.setGroupId(groupId);
			req.setEndTime(endtime);
			ManuPayPosOrderResp resp = payOrderFacade.manuPayPosOrder(req);
			if(resp != null){
				JSONObject object = JSONObject.fromObject(resp);
				return object.toString();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "{\"result\":\"-1\",\"errmsg\":\"结算失败!\"}";
	}
	
	private String orderCash(HttpServletRequest request, String out, Long uin, Long groupId) {
		String result="";
		Long orderId = RequestUtil.getLong(request, "orderid", -1L);
		Double total = RequestUtil.getDouble(request, "total", 0d);
		String imei  =  RequestUtil.getString(request, "imei");
		Integer version = RequestUtil.getInteger(request, "version", 0);//版本号2.0以上返回json数据
		Integer workid = RequestUtil.getInteger(request, "workid",0);//工作班号
		Long berthorderid = RequestUtil.getLong(request, "berthorderid", -1L);
		Long endtime = RequestUtil.getLong(request, "endtime", -1L);
		
		if(version > 1340){
			return manuPayPosOrder(request, uin, 0, groupId);//现金支付
		}
		logger.error("ordercash  payOrder >>>>:orderid:"+orderId+",total:"+total+",uin:"+uin+",berthorder:"+berthorderid+",endtime:"+endtime);
		if(orderId!=-1){
			result = payOrder(orderId, total, uin, workid, out, imei, version,0,berthorderid,endtime);
		}
		logger.error(">>>>ordercash 收费员现金结算，返回:"+result);
		return result;
	}

	/**
	 * 现金结算订单
	 * @param orderId
	 * @param total
	 * @param uin
	 * @param workid
	 * @param out
	 * @param imei
	 * @param version
	 * @param isEsc 1结算逃单，0普通订单
	 * @return
	 */
	private String payOrder(Long orderId,Double total,Long uin,Integer workid,String out,
			String imei,Integer version,Integer isEsc,Long berthorderid,Long endtime){
		Long ntime = System.currentTimeMillis()/1000;
		logger.error("payOrder isEsc:"+isEsc);
		Map<String, Object> infoMap = new HashMap<String, Object>();
		String result = "";
		Map orderMap = daService.getPojo("select * from order_tb where id=?", new Object[]{orderId});
		Integer state = (Integer)orderMap.get("state");
		if(state!=null&&state==1){//已结算，返回
			return "{\"result\":\"-2\",\"errmsg\":\"订单已结算!\"}";
		}
		Double bakMoney=0.0;
		Double prepay = 0.0;
		if(endtime>0){
			ntime = endtime;
		}
		Long create_time = (Long)orderMap.get("create_time");
		berthorderid = commonMethods.getBerthOrderId(orderId);
		Long end_time = commonMethods.getOrderEndTime(berthorderid, uin, ntime);
		if(orderMap.get("prepaid")!=null) {
			prepay=StringUtils.formatDouble(orderMap.get("prepaid"));
			Double prePay = StringUtils.formatDouble(orderMap.get("total"));
			Integer payType =(Integer)orderMap.get("pay_type");
			Long userId = (Long)orderMap.get("uin");//先查一下是否是注册车主，pos机收费一般没有注册的车主
			if(userId!=null&&userId>0&&prePay>0&&payType!=null&&payType==2){//非注册车主不处理退款
				boolean _result = prepayRefund(orderMap,prePay);
				//预支付金额退款
				logger.error("payOrder 现金结算预付款退款:"+_result+",orderid:"+orderId);
			}
		}
		Long berthnumber =  (Long)orderMap.get("berthnumber");
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		//更新订单状态，收费成功
		Map<String, Object> orderSqlMap = new HashMap<String, Object>();
		Map<String, Object> cashsqlMap =new HashMap<String, Object>();
		bakMoney = StringUtils.formatDouble(prepay-total);
		if(isEsc==0){
			if(state==2){//置为未缴后不可当作普通订单结算
				return "{\"result\":\"-1\",\"errmsg\":\"已置为未缴不可正常结算!\"}";
			}
			orderSqlMap.put("sql", "update order_tb set state=?,total=?,end_time=?,pay_type=?,imei=?,out_uid=? where id=?");
			orderSqlMap.put("values", new Object[]{1,total,end_time,1,imei,uin,orderId});
			// target integer DEFAULT 0, -- （该字段不适用type=1）0：停车费（非预付），1：预付停车费，2：预付退款（预付超额），3：预付补缴（预付不足），4：追缴停车费
			Integer target = 3;//预付补缴（预付不足）
			Double money = bakMoney;
			Integer ctype=0;//收入
			if(bakMoney>0){
				target = 2;//预付退款（预付超额）
				ctype=1;//支出
			}else 
				money = StringUtils.formatDouble(total-prepay);
			cashsqlMap.put("sql", "insert into parkuser_cash_tb(uin,amount,type,orderid,create_time,target,ctype) values(?,?,?,?,?,?,?)");
			cashsqlMap.put("values",  new Object[]{uin,money,0,orderId,end_time,target,ctype});
			bathSql.add(cashsqlMap);
		}else {//是结算逃单 ，不修改结算时间
			orderSqlMap.put("sql", "update order_tb set state=?,total=?,pay_type=?,imei=?,out_uid=? where id=?");
			orderSqlMap.put("values", new Object[]{1,total,1,imei,uin,orderId});
			// target integer DEFAULT 0, -- （该字段不适用type=1）0：停车费（非预付），1：预付停车费，2：预付退款（预付超额），3：预付补缴（预付不足），4：追缴停车费
			Double money = bakMoney;
			if(bakMoney<0)
				money = StringUtils.formatDouble(total-prepay);
			cashsqlMap.put("sql", "insert into parkuser_cash_tb(uin,amount,type,orderid,create_time,target) values(?,?,?,?,?,?)");
			cashsqlMap.put("values",  new Object[]{uin,money,0,orderId,end_time,4});
			bathSql.add(cashsqlMap);
		}
		bathSql.add(orderSqlMap);
		
		//现金明细
		boolean b = daService.bathUpdate(bathSql);
		logger.error("payOrder >>>>ordreid:"+orderId+",b:"+b);
		if(b){
			infoMap.put("info", "现金收费成功!");
			if(isEsc==1){//逃单时，更新逃单
				int re = daService.update("update  no_payment_tb set state=?,pursue_uid=?,pursue_time=?,act_total=?  where order_id=? ", 
						new Object[]{1,uin,end_time,total,orderId});
				logger.error("payOrder 更新逃单：ret :"+re);
			}else {
				if(berthnumber!=null&&berthnumber>0){//根据泊位号更新泊位状态
					int re =daService.update("update com_park_tb set state=?,order_id=? where id =? and order_id=?",  new Object[]{0,null,berthnumber,orderId});
					logger.error("payOrder 不是逃单更新泊位：ret :"+re+",berthnumber："+berthnumber+",orderid:"+orderId);
				}
			}
			//更新订单消息中的状态 
			daService.update("update order_message_tb set state=? where orderid=?", new Object[]{2,orderId});
			int r = 0;
			if(berthorderid>0){
				r = daService.update("update berth_order_tb set out_uid=?,order_total=?  where id=? ", new Object[]{uin,total,berthorderid});
			}
			logger.error("payOrder 更新地磁订单表：ret :"+r);
			if(out.equals("json")){
				if(version>=2){//版本2.0以上，pos机结算时，如果有预收金额 ，要返回多退少补明细
					String mesg = "收费成功";
					if(bakMoney>0)
						mesg = "预收金额："+prepay+"元，应收金额："+total+"元，应退款："+bakMoney+"元";
					else if(bakMoney<0)
						mesg= "预收金额："+prepay+"元，应收金额："+total+"元，应补收："+StringUtils.formatDouble(total-prepay)+"元";
					
					String duration = StringUtils.getTimeString(create_time, end_time);
					result ="{\"result\":\"1\",\"errmsg\":\""+mesg+"\",\"duration\":\""+duration+"\"}";
				}else {
					result= "1";
				}
			}else {
				result = StringUtils.createXML(infoMap);
			}
			if(workid>0){//有工作班号时，查一下此订单是不是在上班期间产生的，如果不是，要把此前预付金额加入工作班次表，签退时扣除这部分，并把订单编号写入工作班次表
				Long count = daService.getLong("select count(ID) from work_detail_tb where workid=? and orderid=? ",
						new Object[]{workid,orderId});
				if(count<1){//订单不是上班期间产生的
					int ret = 0;
					if(prepay>0){
						ret = daService.update("update parkuser_work_record_tb set history_money = history_money+? where id =? ",
								new Object[]{prepay,workid});
						logger.error("payOrder pos机现金结算，不是在本班次产生的订单，预收金额："+prepay+"写入班次表："+ret);
					}
					if(total>0){
						ret = daService.update("insert into work_detail_tb (uid,orderid,bid,workid,berthsec_id) values(?,?,?,?,?)", 
								new Object[]{uin,orderId,berthnumber,workid,orderMap.get("berthsec_id")});
						logger.error("payOrder pos机现金结算，不是在本班次产生的订单，订单编号："+orderId+"，班次号："+workid+",写入班次详情表："+ret);
					}
				}
			}
			
		}else {
			infoMap.put("info", "现金收费失败!");
			if(out.equals("json")){
				if(version>=2){//版本2.0以上，pos机结算时，如果有预收金额 ，要返回多退少补明细
					result ="{\"result\":\"-1\",\"errmsg\":\"现金收费失败!\"}";
				}else {
					result= "-1";
				}
			}else {
				result = StringUtils.createXML(infoMap);
			}
		}
		logger.error("payOrder result:"+result);
		return result;
	}
	/**
	 * 查询历史订单
	 * @param request
	 * @param comId
	 * @param out
	 * @return
	 */
	private String orderHistory(HttpServletRequest request,Long comId,String out, long groupId) {
		Map<String, Object> infoMap = new HashMap<String, Object>();
		String result="";
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "size", 20);
		Long _uid = RequestUtil.getLong(request, "uid", -1L);
		String day = RequestUtil.processParams(request, "day");
		String ptype = RequestUtil.getString(request, "ptype");//支付方式
		List<Object> params = new ArrayList<Object>();
		params.add(0);
		params.add(comId);
//		Map com = daService.getMap( "select isshowepay from com_info_tb where id=? and isshowepay=?",new Object[]{comId,1});
//		if(com!=null&&com.get("isshowepay")!=null){
//			params.add(5);//直付订单不返回
//		}else{
//			params.add(4);//直付订单不返回
//		}
//		params.add(5);//修改目前月卡订单不显示
		String countSql = "select count(*) from order_tb where state>? and comid=? ";
		String sql = "select * from order_tb where state>?  and comid=?  ";//order by id desc ";
		String priceSql = "select sum(total) total,uid from order_tb where state>?  and comid=? ";
//		String countSql = "select count(*) from order_tb where state>? and comid=? and c_type<? ";
//		String sql = "select * from order_tb where state>?  and comid=? and c_type<?  ";//order by id desc ";
//		String priceSql = "select sum(total) total,uid from order_tb where state>?  and comid=? and c_type<? ";
		Long time = TimeTools.getToDayBeginTime();
		if(_uid!=-1){
			sql +=" and uid=? and end_time between ? and ?";
			countSql+=" and uid=? and end_time between ? and ?";
			priceSql +=" and uid=? and end_time between ? and ?";
			params.add(_uid);
			Long btime = time;
			if(day.equals("last")){
				params.add(btime-24*60*60);
				params.add(btime);
			}else {
				params.add(btime);
				params.add(btime+24*60*60);
			}
			if(ptype.equals("2")){//手机支付
				sql +=" and pay_type=? ";
				countSql+=" and pay_type=? ";
				priceSql +=" and pay_type=? ";
				params.add(2);
			}else if(ptype.equals("3")){//包月支付
				sql +=" and pay_type=? ";
				countSql+=" and pay_type=? ";
				priceSql +=" and pay_type=? ";
				params.add(3);
			}else if(ptype.equals("4")){//直付订单
				sql +=" and c_type=? ";
				countSql+=" and c_type=? ";
				priceSql +=" and c_type=? ";
				params.add(4);
			}
		}
		Long _total = pService.getCount(countSql,params);
		Object totalPrice = "0";
		Map pMap  = pService.getMap(priceSql+" group by uid ", params);
		if(pMap!=null&&pMap.get("total")!=null){
			totalPrice=pMap.get("total");
		}
		List<Map<String, Object>> list = pService.getAll(sql +" order by end_time desc ",// and create_time>?",
				params, pageNum, pageSize);
		logger.error("historyorder:"+_total+",totalprice:"+totalPrice);
		setPicParams(list);
		Integer ismonthuser = 0;//判断是否月卡用户
		if(list!=null&&list.size()>0){
			List<Map<String, Object>> infoMaps = new ArrayList<Map<String,Object>>();
			for(Map map : list){
				Map<String, Object> info = new HashMap<String, Object>();
				Long uid = (Long)map.get("uin");
				info.put("uin", map.get("uin"));
				String nfc_uuid = (String)map.get("nfc_uuid");
				String carNumber = "车牌号未知";
				if(map.get("car_number")!=null&&!"".equals((String)map.get("car_number"))){
					carNumber = map.get("car_number")+"";
				    if(StringUtils.isNumber(carNumber)){
				    	carNumber = "车牌号未知";
				    }
				}else {
					if(uid!=-1){
						carNumber = publicMethods.getCarNumber(uid);
					}
				}
				info.put("carnumber", carNumber);
				Long start= (Long)map.get("create_time");
				Long end= (Long)map.get("end_time");
				Double total =StringUtils.formatDouble(map.get("total"));// countPrice(start, end, comId);
				info.put("total", StringUtils.formatDouble(total));
				info.put("id", map.get("id"));
				info.put("state", map.get("state"));
				info.put("ptype", map.get("pay_type"));
				if(map.get("c_type")!=null&&Integer.valueOf(map.get("c_type")+"")==4){
					info.put("duration", "直接支付");
				}else {
					info.put("duration", "停车 "+StringUtils.getTimeString(start,end));
				}
				info.put("btime", TimeTools.getTime_yyyyMMdd_HHmm(start*1000));
				//判断是否是月卡用户
//				boolean b = publicMethods.isMonthUser(uid, comId);
				info.put("ctype", map.get("c_type"));
				if(Long.parseLong(map.get("c_type")+"")==5){
					ismonthuser = 1;//是月卡用户
				}else{
					ismonthuser = 0;//不是月卡用户
				}
				info.put("ismonthuser", ismonthuser);
				info.put("car_type", map.get("car_type"));
				//车牌照片参数设置（HD版需要）
				info.put("lefttop", map.get("lefttop"));
				info.put("rightbottom", map.get("rightbottom"));
				info.put("width", map.get("width"));
				info.put("height", map.get("height"));
				boolean is_card = commonMethods.cardUser(carNumber, groupId);
				if(is_card){
					info.put("is_card", 1);
				}else{
					info.put("is_card", 0);
				}
				infoMaps.add(info);
			}
			if(out.equals("json")){
				result = "{\"count\":"+_total+",\"price\":"+totalPrice+",\"info\":"+StringUtils.createJson(infoMaps)+"}";
			}else {
				result = StringUtils.createXML(infoMaps,_total);
			}
		}else {
			infoMap.put("info", "没有记录");
			result = StringUtils.createJson(infoMap);
		}
		return result;
	}
	//pos机生成订单
	private String posIncome(HttpServletRequest request, Long comId, Long groupId, Long uid) {
		try {
			Long curTime = System.currentTimeMillis()/1000;
			String imei  =  RequestUtil.getString(request, "imei");
			String carNumber=AjaxUtil.decodeUTF8(RequestUtil.getString(request, "carnumber"));
			Long bid = RequestUtil.getLong(request, "bid", -1L);//泊位编号
			Long orderId = RequestUtil.getLong(request, "orderid", -1L);//预取的订单号(后来添加的参数)
			//--------------------------------预支付的参数---------------------------------------------//
			String nfc_uuid = RequestUtil.processParams(request, "uuid");//预支付用到的参数：刷卡预支付卡片内置唯一编号
			Double prepay= RequestUtil.getDouble(request,  "prepay", 0d);//预付金额
			Integer bindcard = RequestUtil.getInteger(request, "bindcard", 0);//预支付用到的参数：0:客户端弹出绑定车主手机号的提示框 1：不弹出提示框直接刷卡预付
			Integer payType = RequestUtil.getInteger(request, "paytype", 0);//预支付用到的参数：0：现金预付 1：刷卡预付
			//--------------------------------弃用的参数---------------------------------------------//
			Long workId = RequestUtil.getLong(request, "workid", -1L);//签到表编号(弃用，从parkuser_work_record_tb查询)
			Long berthid= RequestUtil.getLong(request, "berthid", -1L);//所在泊位段编号(弃用,从泊位表里查询)
			Long berthOrderId = RequestUtil.getLong(request, "berthorderid", -1L);//已废弃，之前是从客户端传过来的数据，现在是从后台查询
			Integer ismonthuser = RequestUtil.getInteger(request, "ismonthuser", 0);
			Integer carType = RequestUtil.getInteger(request, "car_type", 0);
			logger.error("uid:"+uid+",workId:"+workId+",berthid:"+berthid+",bid:"+bid+
					",ismonthuser:"+ismonthuser+",berthOrderId:"+berthOrderId+",orderId:"+orderId
					+",prepay:"+prepay+",imei:"+imei+",carNumber:"+carNumber+",comid:"+comId
					+ ",bindcard:"+bindcard+",payType:"+payType);
			
			GenPosOrderFacadeReq req = new GenPosOrderFacadeReq();
			req.setBerthId(bid);
			req.setBindcard(bindcard);
			req.setCarNumber(carNumber);
			req.setGroupId(groupId);
			req.setImei(imei);
			req.setNfc_uuid(nfc_uuid);
			req.setOrderId(orderId);
			req.setParkId(comId);
			req.setPayType(payType);
			req.setPrepay(prepay);
			req.setUid(uid);
			req.setCarType(carType);
			GenPosOrderFacadeResp resp = genOrderFacade.genPosOrder(req);
			logger.error(resp.toString());
			if(resp != null){
				JSONObject object = JSONObject.fromObject(resp);
				logger.error(object.toString());
				Integer result = object.getInt("result");
				Integer ctype = object.getInt("ctype");
				if(result==1&&(ctype!=null&&ctype!=5)){//进场成功,不是月卡车辆时，传订单到泊链平台
					Long orderid = object.getLong("orderid");//订单号
					publicMethods.sendOrderToBolink(orderid, carNumber, comId);
				}
				return object.toString();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return "{\"result\":\"0\",\"errmsg\":\"进场错误，请重新操作\"}";
	}


	private String currOrders(HttpServletRequest request,Long uin,Long comId,String out,
			Map<String, Object> infoMap) {
		Long ntime = System.currentTimeMillis()/1000;
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "size", 20);
		List<Object> params = new ArrayList<Object>();
		params.add(0);
		params.add(comId);
		Long _total = pService.getLong("select count(*) from order_tb where state=? and comid=? ", 
				new Object[]{0,comId});
		//查停车订单
		List<Map<String,Object>> list = pService.getAll("select * from order_tb where state=? and comid=? order by id desc ",//and create_time>?",
				params, pageNum, pageSize);
		
		//查泊车订单
		List<Map<String,Object>> csList =null;// daService.getAll("select c.id,c.state,c.buid,c.euid,c.car_number,c.btime,c.start_time,t.next_price,t.max_price  " +
				//"from carstop_order_tb c left join car_stops_tb t on c.cid = t.id where (c.buid=? and c.state in(?,?)) or (c.euid=? and c.state in(?,?)) ",
				//new Object[]{uin,1,2,uin,5,6});
		
		
		//logger.error("currentorder:"+_total);
		List<Map<String, Object>> infoMaps = new ArrayList<Map<String,Object>>();
		Double ptotal = 0d;
		Long end=ntime;
		if(list!=null&&list.size()>0){
			for(Map map : list){
				Map<String, Object> info = new HashMap<String, Object>();
				Long uid = (Long)map.get("uin");
				String carNumber = "车牌号未知";
				if(map.get("car_number")!=null&&!"".equals((String)map.get("car_number"))){
					carNumber = (String)map.get("car_number");
				}else {
					if(uid!=-1){
						carNumber = publicMethods.getCarNumber(uid);
					}
				}
				info.put("carnumber", carNumber);
				Long start= (Long)map.get("create_time");
				
				Integer pid = (Integer)map.get("pid");
				Integer car_type = (Integer)map.get("car_type");//0：通用，1：小车，2：大车
				end = ntime;
				if(pid>-1){
					info.put("total",publicMethods.getCustomPrice(start, end, pid));
				}else {
					info.put("total",publicMethods.getPrice(start, end, comId, car_type));
//					int isspecialcar = 0;
//					Map map1 = daService.getMap("select typeid from car_number_type_tb where car_number = ? and comid=?", new Object[]{carNumber, comId});
//					if(map1!=null&&map1.size()>0){
//						isspecialcar = 1;
//					}
//					infoMap.put("total",publicMethods.getPriceHY(start, end, comId, car_type, isspecialcar));
				}
				info.put("id", map.get("id"));
				info.put("type", "order");
				info.put("state","-1");
				info.put("duration", "已停 "+StringUtils.getTimeString(start,end));
				info.put("btime", TimeTools.getTime_yyyyMMdd_HHmm(start*1000));
				infoMaps.add(info);
			}
		}
		
		if(csList!=null&&!csList.isEmpty()){
			for(Map<String, Object> map : csList){
				Map<String, Object> info = new HashMap<String, Object>();
				info.put("id", map.get("id"));
				Long start = (Long)map.get("btime");
				Integer state = (Integer)map.get("state");
				if(state>2){
					info.put("duration", "已停 "+StringUtils.getTimeString(start,end));
					info.put("btime", TimeTools.getTime_yyyyMMdd_HHmm(start*1000));
					Double nprice = Double.valueOf(map.get("next_price")+"");//时价
					Object tp = map.get("max_price");//最高价
					Double tprice =-1d;
					if(tp!=null)
						tprice = Double.valueOf(tp.toString());
					Long h = StringUtils.getHour(start, end);
					Double total = StringUtils.formatDouble(h*nprice);
					if(tprice!=-1&&total>tprice)
						total = tprice;
					info.put("total",total);
				}else {
					start = (Long)map.get("start_time");
					info.put("total","0.0");
					info.put("btime",TimeTools.getTime_yyyyMMdd_HHmm(start*1000));
					info.put("duration","正在接车");
				}
				info.put("carnumber", map.get("car_number"));
				info.put("state", map.get("state"));
				infoMaps.add(info);
			}
		}
		Collections.sort(infoMaps,new OrderSortCompare());
		
		String result = "";
		ptotal = StringUtils.formatDouble(ptotal);
		if(out.equals("json")){
			result = "{\"count\":"+_total+",\"price\":"+ptotal+",\"info\":"+StringUtils.createJson(infoMaps)+"}";
		}else {
			result = StringUtils.createXML(infoMaps,_total);
		}
		return result;
	}


	private void orderDetail(HttpServletRequest request,Long comId, Long uid,
			Map<String, Object> infoMap) {
		//http://127.0.0.1/zld/collectorrequest.do?action=orderdetail&token=27ffc2991d9385e6c18690fc0a3e9899&orderid=18245855&out=json&r=15
		Long ntime = System.currentTimeMillis()/1000;
		Long orderId = RequestUtil.getLong(request, "orderid", -1L);
		//Long brethorderid = RequestUtil.getLong(request, "brethorderid", -1L);
		logger.error("orderDetail>>>orderId:"+orderId);
		//http://127.0.0.1:8080/zld/collectorrequest.do?action=orderdetail&token=6f56758f82c1ccf17d4519918339dc2c&orderid=826699&out=json
		if(orderId!=-1){
			Map orderMap = daService.getPojo("select o.*,c.cid from order_tb o left join com_park_tb c on c.order_id=o.id" +
					" where o.id=?", new Object[]{orderId});
			if(orderMap!=null&&!orderMap.isEmpty()){
				Long start= (Long)orderMap.get("create_time");
				Long curtime = ntime;
				Long brethorderid = commonMethods.getBerthOrderId(orderId);
				Long end = commonMethods.getOrderEndTime(brethorderid, uid, curtime);
				Integer car_type = (Integer)orderMap.get("car_type");//0：通用，1：小车，2：大车
				if(orderMap.get("end_time")!=null)
					end = (Long)orderMap.get("end_time");
				Integer state = (Integer)orderMap.get("state");
				String _state="未结算";
				
				if(state==1){
					_state="已结算";
				}
				Long uin = (Long)orderMap.get("uin");
				Map userMap = daService.getMap("select mobile from user_info_Tb where id=?", new Object[]{uin});
				
				String mobile = "";
				if(userMap!=null&&userMap.get("mobile")!=null){
					mobile = userMap.get("mobile")+"";
				}
				if(orderMap!=null&&Integer.valueOf(orderMap.get("c_type")+"")==4){
					infoMap.put("showepay", "直接支付");
				}
				
				String carNumber =orderMap.get("car_number")+"";
				if(StringUtils.isNumber(carNumber)){
					carNumber = "车牌号未知";
				}
				if(carNumber.equals("null")||carNumber.equals("")){
					carNumber =publicMethods.getCarNumber(uin);
				}
				if("".equals(carNumber.trim())||"车牌号未知".equals(carNumber.trim()))
					carNumber ="null";
				if(orderMap.get("prepaid")!=null)
					infoMap.put("prepay", StringUtils.formatDouble(orderMap.get("prepaid")));
				Integer pid = (Integer)orderMap.get("pid");
				if(pid>-1){
					infoMap.put("total",publicMethods.getCustomPrice(start, end, pid));
				}else {
					int isspecialcar = 0;
					Map map = daService.getMap("select typeid from car_number_type_tb where car_number = ? and comid=?", new Object[]{carNumber, comId});
					if(map!=null&&map.size()>0){
						isspecialcar = 1;
					}
					logger.error(isspecialcar);
					infoMap.put("total",publicMethods.getPriceHY(start, end, comId, car_type, isspecialcar));
				}
				if(orderMap.get("c_type")!=null&&Integer.valueOf(orderMap.get("c_type")+"")==5){
					infoMap.put("total", StringUtils.formatDouble(0.0));
				}
				if(orderMap.get("state")!=null&&Integer.valueOf(orderMap.get("state")+"")==1){
					infoMap.put("total", StringUtils.formatDouble(orderMap.get("total")));
				}
				if(orderMap.get("c_type")!=null&&Integer.valueOf(orderMap.get("c_type")+"")==4){
					infoMap.put("total", StringUtils.formatDouble(orderMap.get("total")));
				}
				infoMap.put("orderid", orderId);
				infoMap.put("prepaymoney", orderMap.get("prepaid"));
				infoMap.put("begin", start);
				infoMap.put("end", end);
				infoMap.put("state",_state);
				infoMap.put("mobile", mobile);
				infoMap.put("car_type", orderMap.get("car_type"));
				infoMap.put("berthnumber", orderMap.get("berthnumber")==null?"":orderMap.get("berthnumber"));
				infoMap.put("park", orderMap.get("cid")==null?"":orderMap.get("cid"));
				infoMap.put("carnumber", carNumber);
				infoMap.put("duration", StringUtils.getTimeStringSenconds(start, end));
			}else {
				infoMap.put("info", "无此订单信息");
			}
		}else {
			infoMap.put("info", "无此订单信息");
		}
		logger.error(">>>>>>orderdetail ："+infoMap);
	}

	private void setRemark(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				if(map.get("remark") != null){
					String remark = (String)map.get("remark");
					remark = remark.split("_")[0];
					map.put("remark", remark);
				}
			}
		}
	}
	
	/**
	 * 查询车位信息
	 * @param comId
	 * @return
	 */
	private String getComParks(Long comId) {
		//更新车位上已经结算的订单，改为未占用状态
		//commonMethods.updateParkInfo(comId);
		List<Map<String, Object>> list = daService.getAll("select c.cid,c.state,o.id orderid,o.car_number,o.create_time btime,o.uin, o.end_time etime " +
				"from com_park_tb c left join order_tb o on c.order_id = o.id where c.comid=? order by c.id", new Object[]{comId});
		if(list!=null&&!list.isEmpty()){
			return StringUtils.createJson(list);
		}
		return "[]";
	}

	
	
	private void setCarNumber(List<Map<String, Object>> list){
		List<Object> uins = new ArrayList<Object>();
		for(Map<String, Object> map : list){
			uins.add(map.get("uin"));
		}
		if(!uins.isEmpty()){
			String preParams  ="";
			for(Object uin : uins){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
			resultList = daService.getAllMap("select u.id,car_number from user_info_tb u left join car_info_tb c on u.id=c.uin where u.id in ("
									+ preParams + ")", uins);
			List<Object> binduins = new ArrayList<Object>();
			List<Object> nobinduins = new ArrayList<Object>();
			for(Map<String, Object> map : resultList){
				Long uin = (Long)map.get("id");
				if(!binduins.contains(uin)){
					for(Map<String, Object> map2 : list){
						Long id = (Long)map2.get("uin");
						if(uin.intValue() == id.intValue()){
							if(map.get("car_number") != null){
								map2.put("carnumber", map.get("car_number"));
							}
						}
					}
					binduins.add(uin);
				}
			}
		}
	}
	
	private void sendWXMsg(String[] ids, Map userMap,Map comMap,Integer money){
		Long exptime = TimeTools.getToDayBeginTime()+16*24*60*60;
		String exp = TimeTools.getTimeStr_yyyy_MM_dd(exptime * 1000);
		List<Object> uins = new ArrayList<Object>();
		List<Map<String, Object>> openids = new ArrayList<Map<String, Object>>();
		
		for(int i=0;i<ids.length; i++){
			Long uin = Long.valueOf(ids[i]);
			uins.add(uin);
		}
		if(!uins.isEmpty()){
			String preParams  ="";
			for(Object uin : uins){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
			List<Object> binduins = new ArrayList<Object>();
			List<Object> nobinduins = new ArrayList<Object>();//虚拟账户
			resultList = daService.getAllMap(
					"select id,wxp_openid from user_info_tb where id in (" + preParams + ") ", uins);
			for(Map<String, Object> map : resultList){
				Map<String, Object> map2 = new HashMap<String, Object>();
				Long uin = (Long)map.get("id");
				if(map.get("wxp_openid") != null){
					map2.put("openid", map.get("wxp_openid"));
					map2.put("bindflag", 1);
					openids.add(map2);
				}
				binduins.add(uin);
			}
			for(Object object: uins){
				if(!binduins.contains(object)){
					nobinduins.add(object);
				}
			}
			logger.error("sendWXMsg>>>虚拟账户："+nobinduins.toString());
			if(!nobinduins.isEmpty()){
				preParams  ="";
				for(Object uin : nobinduins){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				resultList = daService.getAllMap(
						"select openid from wxp_user_tb where uin in (" + preParams + ") ", nobinduins);
				for(Map<String, Object> map : resultList){
					Map<String, Object> map2 = new HashMap<String, Object>();
					if(map.get("openid") != null){
						map2.put("openid", map.get("openid"));
						map2.put("bindflag", 0);
						openids.add(map2);
					}
				}
			}
			logger.error("sendWXMsg>>>:发消息的openid:"+openids.toString());
			if(openids.size() > 0){
				for(Map<String, Object> map : openids){
					try {
						String openid = (String)map.get("openid");
						Integer bindflag = (Integer)map.get("bindflag");
						
						String url = "http://"+Constants.WXPUBLIC_REDIRECTURL+"/zld/wxpaccount.do?action=toticketpage&openid="+openid;
						if(bindflag == 0){
							url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXPUBLIC_APPID+"&redirect_uri=http%3a%2f%2f"+Constants.WXPUBLIC_REDIRECTURL+"%2fzld%2fwxpaccount.do&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
						}
						Map<String, String> baseinfo = new HashMap<String, String>();
						List<Map<String, String>> orderinfo = new ArrayList<Map<String,String>>();
						String first = "恭喜获得收费员"+userMap.get("nickname")+"("+userMap.get("id")+")赠送的"+comMap.get("company_name")+"专用券";
						String remark = "点击查看详情！";
						String remark_color = "#000000";
						baseinfo.put("url", url);
						baseinfo.put("openid", openid);
						baseinfo.put("top_color", "#000000");
						baseinfo.put("templeteid", Constants.WXPUBLIC_TICKET_ID);
						Map<String, String> keyword1 = new HashMap<String, String>();
						keyword1.put("keyword", "coupon");
						keyword1.put("value", money+"元");
						keyword1.put("color", "#000000");
						orderinfo.add(keyword1);
						Map<String, String> keyword2 = new HashMap<String, String>();
						keyword2.put("keyword", "expDate");
						keyword2.put("value", exp);
						keyword2.put("color", "#000000");
						orderinfo.add(keyword2);
						Map<String, String> keyword3 = new HashMap<String, String>();
						keyword3.put("keyword", "remark");
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
						e.printStackTrace();
					}
					
				}
			}
		}
	}
	
	private void setinfo(List<Map<String, Object>> list,Integer pageNum,Integer pageSize){
		List<Object> uids = new ArrayList<Object>();
		Integer sort = (pageNum - 1)*pageSize;//排行
		for(Map<String, Object> map : list){
			Long uin = (Long)map.get("uin");
			uids.add(uin);
			
			sort++;
			map.put("sort", sort);
		}
		if(!uids.isEmpty()){
			String preParams  ="";
			for(Object uid : uids){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
			resultList = daService.getAllMap(
							"select u.id,nickname,company_name from user_info_tb u,com_info_tb c where u.comid=c.id and u.id in ("
									+ preParams + ") ", uids);
			for(Map<String, Object> map : resultList){
				Long id = (Long)map.get("id");
				String nickname = null;
				String cname = null;
				if(map.get("nickname") != null && ((String)map.get("nickname")).length() > 0){
					nickname = ((String)map.get("nickname")).substring(0, 1);
					for(int i=1;i<((String)map.get("nickname")).length();i++){
						nickname += "*";
					}
				}
				if(map.get("company_name") != null && ((String)map.get("company_name")).length() > 0){
					cname = ((String)map.get("company_name")).substring(0, 1);
					cname += "****停车场";
				}
				for(Map<String, Object> map2: list){
					Long uid = (Long)map2.get("uin");
					if(id.intValue() == uid.intValue()){
						map2.put("nickname", nickname);
						map2.put("cname", cname);
					}
				}
			}
		}
	}
	private String autoUp(HttpServletRequest request,Long comId,Long uid) {
		Long ntime = System.currentTimeMillis()/1000;
		String carNumber = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "carnumber"));
		String cardno = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "cardno"));
		//处理流程：根据车牌查订单：
		/*
		 * 1:有订单，查是否有预支付 ：
		 * 		（）有预付，查金额是否充足，
		 * 			（）充足：返回：{state:1,orderid,btime,etime,carnumber,duration,total}
		 * 			（）不足：返回   {state:2,prefee,total,collect}
		 * 		（）无预付，查是否是会员
		 * 			（）会员 ：余额是否充足
		 * 				（）充足，是否自动结算  
		 * 					（）是：返回：{state:1,orderid,btime,etime,carnumber,duration,total}
		 * 					（）否：收现金返回：{state:0,orderid,btime,etime,carnumber,duration,total}
		 * 				（）不充足 ： 收现金 返回：{state:0,orderid,btime,etime,carnumber,duration,total}
		 * 			（）非会员 :收现金：返回：{state:0,orderid,btime,etime,carnumber,duration,total}
		 * 2：无订单，生成订单，
		 * 		（）会员 ：余额是否充足
		 * 				（）充足，是否自动结算  
		 * 					（）是：返回：{state:1,orderid,btime,etime,carnumber,duration,total}
		 * 					（）否：收现金返回：{state:0,orderid,btime,etime,carnumber,duration,total}
		 * 				（）不充足 ： 收现金 返回：{state:0,orderid,btime,etime,carnumber,duration,total}
		 * 		（）非会员 :收现金：返回：{state:0,orderid,btime,etime,carnumber,duration,total}
		 *
		 */
		Double price = RequestUtil.getDouble(request, "price", 0d);
		//System.out.println(carNumber);
		String result = "{}";
		//生成订单并结算
		if(comId==null||uid==null||uid==-1||comId==-1){
			result="{\"state\":\"-3\",\"errmsg\":\"没有停车场或收费员信息，请重新登录!\"}";
			return result;
		}
		Long uin = -1L;
		if(!carNumber.equals("")){
			Map carMap = daService.getMap("select uin from car_info_tb where car_number=?", new Object[]{carNumber});
			if(carMap!=null)
				uin = (Long)carMap.get("uin");
		}
		boolean isvip = true;//会员
		if(uin==null||uin==-1) {
			result="{\"state\":\"-1\",\"errmsg\":\"车主未注册!\",\"orderid\":\"\"}";
			isvip=false;
			uin = -1L;
		}
		//查订单:
		Map<String,Object> orderMap =null;
		Long orderId = null;
		boolean isOrder= false;
		if("".equals(carNumber)&&!"".equals(cardno)){//极速第三方卡结算
			String uuid = comId+"_"+cardno;
			Long ncount  = daService.getLong("select count(*) from com_nfc_tb where nfc_uuid=? and state=?", 
					new Object[]{uuid,0});
			if(ncount==0){
				logger.error("极速通刷卡...卡号："+uuid+",未注册....");
				result="{\"state\":\"-10\",\"errmsg\":\"卡号没有注册!\",\"orderid\":\"-1\"}";
			}
			orderMap = pService.getMap("select * from order_tb where comid=? and nfc_uuid=? and state=? ", new Object[]{comId,uuid,0});
			if(orderMap==null||orderMap.isEmpty()){
				if(price<0){
					result="{\"state\":\"-2\",\"errmsg\":\"价格不对:"+price+"!\",\"orderid\":\"-1\"}";
				}else {
					//生成订单
					
					orderId = daService.getkey("seq_order_tb");
					int ret = daService.update("insert into order_tb (id,create_time,end_time,comid,uin,state,pay_type,c_type,uid,nfc_uuid,type,total) values(?,?,?,?,?,?,?,?,?,?,?,?)", 
							new Object[]{orderId,ntime,ntime+60,comId,uin,1,1,3,uid,uuid,2,0.0});
					if(ret!=1){//订单写入出错
						result="{\"state\":\"-4\",\"errmsg\":\"生成订单失败!\",\"orderid\":\""+orderId+"\"}";
					}
					if(ncount>0)
						return "{\"state\":\"-11\",\"errmsg\":\"车主未预支付!\",\"orderid\":\"\"}";
				}
			}else {
				orderId = (Long)orderMap.get("id");
				Double prePay = StringUtils.formatDouble(orderMap.get("total"));
				uin = (Long)orderMap.get("uin");
				Long ouid = (Long)orderMap.get("uid");
				logger.error("orderid:"+orderId+",prepay:"+prePay+",uin:"+uin+",total:"+price);
				if(uid!=null&&ouid!=null&&!ouid.equals(uid)){
					daService.update("update order_tb set uid=? where id =? ", new Object[]{uid,orderId});
				}
				if(prePay>0){//有预支付 
					Integer ret = publicMethods.doPrePayOrder(orderMap, price);
					if(ret==1){//支付成功
						if(prePay>=price){//余额充足
							orderMap = daService.getMap("select * from order_tb where id=? ", new Object[]{orderId});
							result=getThirdCardOrderInfo(orderMap);//"{\"state\":\"1\"}";//{state:1,orderid,btime,etime,carnumber,duration,total}
						}else {
							result="{\"state\":\"2\",\"prefee\":\""+prePay+"\",\"total\":\""+price+"\",\"collect\":\""+StringUtils.formatDouble((price-prePay))+"\"}";
						}
					}
				}else {
					if(!isvip){
						daService.update("update order_tb set state=? ,total=?,end_time=?,pay_type=? where id = ? ", new Object[]{1,price,ntime,1,orderId});
						if(ncount>0)
							return "{\"state\":\"-11\",\"errmsg\":\"车主未预支付!\",\"orderid\":\"\"}";
					}
				}
			}
			
		}else {//极速通照牌结算
			orderMap = pService.getMap("select * from order_tb where comid=? and car_number=? and state=? ", new Object[]{comId,carNumber,0});
			if(orderMap!=null){//有订单
				orderId = (Long)orderMap.get("id");
				Double prePay = StringUtils.formatDouble(orderMap.get("total"));
				logger.error("极速通>>>>orderid:"+orderId+",uin:"+uin+",prePay:"+prePay+",price:"+price+",isvip:"+isvip);
				uin = (Long)orderMap.get("uin");
				Long ouid = (Long)orderMap.get("uid");
				if(uid!=null&&ouid!=null&&!ouid.equals(uid)){
					daService.update("update order_tb set uid=? where id =? ", new Object[]{uid,orderId});
				}
				if(prePay>0){//有预支付 
					Integer ret = publicMethods.doPrePayOrder(orderMap, price);
					if(ret==1){//支付成功
						if(prePay>=price){//余额充足
							orderMap = daService.getMap("select * from order_tb where id=? ", new Object[]{orderId});
							result=getOrderInfo(orderMap);//"{\"state\":\"1\"}";//{state:1,orderid,btime,etime,carnumber,duration,total}
						}else {
							result="{\"state\":\"2\",\"prefee\":\""+prePay+"\",\"total\":\""+price+"\",\"collect\":\""+StringUtils.formatDouble((price-prePay))+"\"}";
						}
					}
					return result;
				}else {//无预支付
					isOrder= true;
				}
				if(!isvip){
					daService.update("update order_tb set state=? ,total=?,end_time=?,pay_type=? where id = ? ", new Object[]{1,price,ntime,1,orderId});
					return result;
				}
			}else{//无订单
				//查车主
				if(price<0){
					result="{\"state\":\"-2\",\"errmsg\":\"价格不对:"+price+"!\",\"orderid\":\""+orderId+"\"}";
				}else {
					//生成订单
					orderId = daService.getkey("seq_order_tb");
					String sql = "insert into order_tb (id,create_time,comid,uin,state,c_type,uid,car_number,type) values(?,?,?,?,?,?,?,?,?)";
					Object [] values =new Object[]{orderId,ntime,comId,uin,0,3,uid,carNumber,1};
					if(uin==-1){//非会员
						sql ="insert into order_tb (id,create_time,comid,uin,state,total,end_time,pay_type,c_type,uid,car_number,type) values(?,?,?,?,?,?,?,?,?,?,?,?)";
						values =new Object[]{orderId,ntime,comId,uin,1,price,ntime+60,1,3,uid,carNumber,1};
					}
					int ret = daService.update(sql, values)	;
					if(ret==1){//订单已写入
						isOrder = true;
					}else {
						result="{\"state\":\"-4\",\"errmsg\":\"生成订单失败!\",\"orderid\":\""+orderId+"\"}";
						return result;
					}
				}
			}
			if(isOrder&&uin!=-1){//有订单要支付 --->>>>
				//查可用停车券
				Map tempMap = publicMethods.useTickets(uin, price, comId,uid,0);
				Long ticketId = null;
				if(tempMap!=null){
					ticketId = (Long)tempMap.get("id");
				}
				//查当前订单
				tempMap = daService.getMap("select * from order_tb where id =? ", new Object[]{orderId});
				
				//自动支付设置
				int isautopay = isAutoPay(uin,price);
				if(isautopay==-1){//车主未设置自动支付
					result="{\"state\":\"-8\",\"errmsg\":\"车主未设置自动支付!\",\"orderid\":\""+orderId+"\",\"carnumber\":\""+carNumber+"\",\"total\":\""+price+"\"}";
					daService.update("update order_tb set state=? ,total=?,pay_type=?,end_time=?  where id = ? ", new Object[]{1,price,1,ntime,orderId});
					return result;
				}else if(isautopay==-2){//订单金额超出自动支付限额
					result="{\"state\":\"-9\",\"errmsg\":\"订单金额超出自动支付限额!\",\"orderid\":\""+orderId+"\",\"carnumber\":\""+carNumber+"\",\"total\":\""+price+"\"}";
					daService.update("update order_tb set state=? ,total=?,pay_type=?,end_time=?  where id = ? ", new Object[]{1,price,1,ntime,orderId});
					return result;
				}
				//结算订单
				int re = publicMethods.payOrder(tempMap, price, uin, 2,0,ticketId,null, -1L, uid);
				logger.info(">>>>>>>>>>>>车主账户支付 ："+re+",orderid:"+orderId);
				if(re==5){//结算成功
					tempMap = daService.getMap("select * from order_tb where id =? ", new Object[]{orderId});
					result=getOrderInfo(tempMap);//"{\"state\":\"1\",\"errmsg\":\"订单支付成功!\"}";//{state:1,orderid,btime,etime,carnumber,duration,total}
				}else{
					switch (re) {
					case -8://已支付，不能重复支付
						result="{\"state\":\"-5\",\"errmsg\":\"已支付，不能重复支付!\",\"orderid\":\""+orderId+"\"}";
						break;
					case -7://支付失败
						result="{\"state\":\"-6\",\"errmsg\":\"支付失败!\",\"orderid\":\""+orderId+"\"}";						
						break;
					case -12://余额不足
						result="{\"state\":\"-7\",\"errmsg\":\"余额不足!\",\"orderid\":\""+orderId+"\",\"carnumber\":\""+carNumber+"\",\"total\":\""+price+"\"}";
						daService.update("update order_tb set state=? ,total=?,pay_type=?,end_time=?  where id = ? ", new Object[]{1,price,1,ntime,orderId});
						break;
					default:
						result="{\"state\":\"-6\",\"errmsg\":\"支付失败!\",\"orderid\":\""+orderId+"\"}";						
						break;
					}
				}
			}
		}
		//预支付不足：result="{\"result\":\"2\",\"prefee\":\""+prefee+"\",\"total\":\""+money+"\",\"collect\":\""+(money-prefee)+"\"}";
		//结算成功：{"total":"79.4","duration":"5天 18小时24分钟","carnumber":"京AFY123","etime":"10:38","state":"2","btime":"16:14","orderid":"786636"} 
		if(result.equals("{}"))
			result="{\"state\":\"-6\",\"errmsg\":\"支付失败!\",\"orderid\":\""+orderId+"\"}";
		logger.error(">>>>>>极速通:"+result);
		return result;
	}
	/**是否自动支付***/
	private int isAutoPay(Long uin, Double price) {
		//查车主配置，是否设置了自动支付。没有配置时，默认25元以下自动支付 
		Integer autoCash=1;
		Map upMap = daService.getPojo("select auto_cash,limit_money from user_profile_tb where uin =?", new Object[]{uin});
		Integer limitMoney =25;
		if(upMap!=null&&upMap.get("auto_cash")!=null){//车主有自动支付设置
			autoCash= (Integer)upMap.get("auto_cash");
			limitMoney = (Integer)upMap.get("limit_money");
			if(autoCash!=null&&autoCash==1){//设置了自动支付
				if(limitMoney==-1)//不限上金额
					return 1;
				else if(price>limitMoney){//订单金额超出了自动支付限额
					return -2;
				}
			}else//设置了不自动支付
				return -1;
		}
		//车主没有自动支付设置，返回可支付
		return 1;
	}
	private String getOrderInfo(Map orderMap){
		Long btime = (Long)orderMap.get("create_time");
		Long etime = (Long)orderMap.get("end_time");
		String dur = StringUtils.getTimeString(btime,etime);
		String bt = TimeTools.getTime_yyyyMMdd_HHmm(btime*1000).substring(11);
		String et = TimeTools.getTime_yyyyMMdd_HHmm(etime*1000).substring(11);
		String ret = "{\"state\":\"1\",\"orderid\":\""+orderMap.get("id")+"\",\"btime\":\""+bt+"\",\"etime\":\""+et+"\"," +
				"\"carnumber\":\""+orderMap.get("car_number")+"\",\"duration\":\""+dur+"\",\"total\":\""+orderMap.get("total")+"\"}";
		return ret;
	}
	
	private String getThirdCardOrderInfo(Map orderMap){
		Long btime = (Long)orderMap.get("create_time");
		Long etime = (Long)orderMap.get("end_time");
		String dur = StringUtils.getTimeString(btime,etime);
		String bt = TimeTools.getTime_yyyyMMdd_HHmm(btime*1000).substring(11);
		String et = TimeTools.getTime_yyyyMMdd_HHmm(etime*1000).substring(11);
		String uuid = (String)orderMap.get("nfc_uuid");
		if(uuid!=null&&uuid.indexOf("_")!=-1)
			uuid = uuid.split("_")[1];
		else {
			uuid = "";
		}
		String ret = "{\"state\":\"1\",\"orderid\":\""+orderMap.get("id")+"\",\"btime\":\""+bt+"\",\"etime\":\""+et+"\"," +
				"\"carnumber\":\""+uuid+"\",\"duration\":\""+dur+"\",\"total\":\""+orderMap.get("total")+"\"}";
		return ret;
	}

	
	/**
	 * 分享车位
	 * @param comId 停车场编号
	 * @param uin 客户编号 
	 * @param number 分享数
	 * @param infoMap 返回结果
	 */
	private void doShare(Long comId,Long uin,Integer number,Map<String,Object> infoMap,boolean isCanLalaRecord){
		Long ntime = System.currentTimeMillis()/1000;
		//更新公司表中停车场的分享数量，
		if(comId!=null&&uin!=null){
			int result = daService.update("update com_info_tb set share_number =?,update_time=? where id=?",
					new Object[]{number,ntime,comId});
			//计算返回可用数量
			if(result==1){
//				if(isCanLalaRecord)
//					doCollectorSort(number,uin,comId);
				//查询当前未结算的订单数，（真来电已占车位数）
				Long count = pService.getLong("select count(*) from order_tb where comid=? and state=? ",//and create_time>?",
						new Object[]{comId,0});//,TimeTools.getToDayBeginTime()});
				infoMap.put("info", "success");
				infoMap.put("busy", count+"");
				logService.updateShareLog(comId, uin, number);
			}else {
				infoMap.put("info", "fail");
				infoMap.put("message", "分享车位失败，请稍候再试!");
			}
		}else {
			infoMap.put("info", "fail");
			infoMap.put("message", "公司或员工不合法!");
		}
	}
	
	/*private  void doCollectorSort(Integer number,Long uin,Long comId){
		
		Long time = ntime;
		boolean isLala  = false;
		try {
			isLala = publicMethods.isCanLaLa(number, uin, time);
		} catch (Exception e) {
			logger.error("memcacahe error:"+e.getMessage());
			isLala=ParkingMap.isCanRecordLaLa(uin);
		}
		if(isLala){
			logService.updateScroe(1, uin,comId);
		}
	}*/
	
	/**
	 * 打折处理
	 * @param comId 停车场编号
	 * @param uin 客户编号 
	 * @param hour  优惠小时 
	 * @param orderId 订单编号
	 * @param infoMap 返回结果
	 */
	private void doSale(Long comId,Long uin,Integer hour,Long orderId,Map<String,Object> infoMap){
		//更新订单表的金额，停车场的总额及余额，车主的余额,
		Map orderMap = daService.getPojo("select * from order_tb where id=?", new Object[]{orderId});
		if(orderMap!=null){
			Long cid  = (Long)orderMap.get("comid");
			Long uid = (Long)orderMap.get("uin");
			if(cid.intValue()==comId.intValue()){//验证订单是否正确 
				Double total = getPrice(hour, comId);
				//更新订单金额
				List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
				Map<String, Object> orderSqlMap = new HashMap<String, Object>();
				orderSqlMap.put("sql", "update order_tb set total = total-? where id =?");
				orderSqlMap.put("values", new Object[]{total,orderId});
				bathSql.add(orderSqlMap);
				//更新停车场总额及余额
				Map<String, Object> comSqlMap = new HashMap<String, Object>();
				comSqlMap.put("sql", "update com_info_tb set " +
						"total_money=total_money-? ,money=money-? where id=?");
				comSqlMap.put("values", new Object[]{total,total,comId});
				bathSql.add(comSqlMap);
				//更新车主余额
				Map<String, Object> userSqlMap = new HashMap<String, Object>();
				userSqlMap.put("sql", "update user_info_tb set balance = balance+? where id =?");
				userSqlMap.put("values", new Object[]{total,uid});
				bathSql.add(userSqlMap);
				boolean result = daService.bathUpdate(bathSql);
				if(result){//更新余额成功时，返回消息，并写系统日志
					infoMap.put("info", "success");
					infoMap.put("message", "优惠成功!");
					//写系统日志
					doLog(comId, uin, TimeTools.gettime()+",优惠了订单，编号："+orderId+",优惠金额："+total,2);
					//写系统消息，车主可以通过刷新消息取到
					doMessage(uid, TimeTools.gettime()+",您的订单(编号："+orderId+")优惠了"+total+"元,已经更新了你的余额，请查收。");
				}else {
					infoMap.put("info", "fail");
					infoMap.put("message", "优惠失败，请稍候重试!");
				}
			}
		}else{
			infoMap.put("info", "fail");
			infoMap.put("message", "参数有误!");
		}
			
		//添加消费流水
		//写打折日志 
	}

	private Double getPrice (Integer hour,Long comId){
		//计算优惠金额
		Map priceMap = daService.getPojo("select * from price_tb where comid=?" +
				" and state=? order by id desc",new Object[]{comId,1});
		Double price = 0d;
		if(priceMap!=null){
			Integer payType = (Integer)priceMap.get("pay_type");
			price = Double.valueOf(priceMap.get("price")+"");
			switch (payType) {
			case 0://分段
				Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
				calendar.setTimeInMillis(System.currentTimeMillis());
				//开始小时
				int nhour = calendar.get(Calendar.HOUR_OF_DAY);
				Integer bTime = (Integer)priceMap.get("b_time");
				Integer eTime = (Integer)priceMap.get("e_time");
				//当前时间在分段区间内
				if(nhour>bTime&&nhour<eTime)
					price = price*hour;
				break;
			case 2://按时间单位
				Integer unit = (Integer)priceMap.get("unit");
				price = hour*60/unit*price;
				break;		
			default:
				break;
			}
		}
		return price;
		
	}
	/**
	 * //写系统消息，（收费员定时取消息）
	 * @param comId
	 * @param mesgType  0:收费员消息   ,1:车主消息
	 * @param uin
	 * @param body
	 * @param orderId
	 * @param total
	 */
	private void doMessage(Long uin,String body){
		Long ntime = System.currentTimeMillis()/1000;
		daService.update("insert into message_tb (type,uin,create_time,content,state) values (?,?,?,?,?)", 
				new Object[]{1,uin,ntime,body,0});
	}
	/*
	 * 写系统日志 
	 */
	private void doLog(Long comid,Long uin,String log,Integer type){
		logService.updateOrderLog(comid, uin, log, type);
	}
	/**
	 * 计算订单金额
	 * @param start
	 * @param end
	 * @param comId
	 * @return 订单金额_是否优惠
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map getPriceMap(Long comId){
		Map priceMap1=null;
		List<Map> priceList=daService.getAll("select * from price_tb where comid=? " +
				"and state=? order by id desc", new Object[]{comId,0});
		if(priceList==null||priceList.size()==0){
			//发短信给管理员，通过设置好价格
		}else {
			priceMap1=priceList.get(0);
			boolean pm1 = false;//找到map1,必须是结束时间大于开始时间
			Integer payType = (Integer)priceMap1.get("pay_type");
			if(payType==0&&priceList.size()>1){
				for(Map map : priceList){
					if(pm1)
						break;
					payType = (Integer)map.get("pay_type");
					Integer btime = (Integer)map.get("b_time");
					Integer etime = (Integer)map.get("e_time");
					if(payType==0&&etime>btime){
						if(!pm1){
							priceMap1 = map;
							pm1=true;
						}
					}
				}
			}
		}
		return priceMap1;	
	}
	
	private List<Map<String, Object >> setScroeList(List<Map> list){
		List<Map<String, Object >> templiList = new ArrayList<Map<String, Object >>();
		List<Object> uins = new ArrayList<Object>();
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				Map map = (Map)list.get(i);
				if(map.get("uin")!=null){
					Long uin = (Long)map.get("uin");
//					if(!uins.contains(uin))
						uins.add(uin);
				}
			}
		}
		if(!uins.isEmpty()){
			String preParams  ="";
			for(Object uin : uins){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			uins.add(0);
			List<Map<String, Object>> resultList = daService.getAllMap("select u.id,u.mobile ,u.nickname as uname,c.company_name cname ," +
					"c.uid from user_info_tb u,com_info_tb c" +
					" where u.comid=c.id and  u.id in ("+preParams+")  and c.state=?", uins);
			
			//Map<String ,Object> markerMap = new HashMap<String ,Object>();
			if(resultList!=null&&!resultList.isEmpty()){
				
				for(int i=0;i<list.size();i++){
					Map map1 = (Map)list.get(i);
					for(Map<String,Object> map: resultList){
						Long uin = (Long)map.get("id");
						if(map1.get("uin").equals(uin)){
							templiList.add(map1);
							map1.put("nickname", "-");
							String cname = (String)map.get("cname");
							if(cname.length() > 1){
								String hidecname = "***";
								/*for(int j=0;j<cname.length()-2;j++){
									hidecname += "*";
								}*/
								hidecname =cname.substring(0, 1) +hidecname + cname.substring(cname.length()-1, cname.length());
								cname = hidecname;
							}
							map1.put("cname", cname);
							map1.put("score", StringUtils.formatDouble(map1.get("score")));
							break;
						}
					}
				}
			}
		}
		return templiList;
	}
	
	private String myInfo(Long uin){
		Map userMap = daService.getMap("select id, nickname,auth_flag,mobile from user_info_tb where id=?",new Object[]{uin});
		String info="";
		if(userMap!=null){
			Long count = daService.getLong("select Count(id) from collector_account_pic_tb where uin=? and state=? ", new Object[]{uin,0});
			Long role = (Long)userMap.get("auth_flag");
			String _role = "收费员";
			if(role==1)
				_role = "管理员";
			return "{\"name\":\""+userMap.get("nickname")+"\",\"uin\":\""+userMap.get("id")+
					"\",\"role\":\""+_role+"\",\"mobile\":\""+userMap.get("mobile")+"\",\"pic\":\""+count+"\"}";
		}
		return "{}";
	}
	
	private void setSort(List list){
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				Map map = (Map)list.get(i);
				map.put("sort", i+1);
			}
		}
	}
	
	private void setAccountList (List<Map<String, Object>> list,Integer ptype){
		if(list!=null&&!list.isEmpty()){
			if(ptype==0){
				for(Map<String, Object> map :list){
					Integer target = (Integer)map.get("target");
					if(target!=null){
						switch (target) {
						case 0:
							map.put("target", "银行卡");
							break;
						case 1:
							map.put("target", "支付宝");					
							break;
						case 2:
							map.put("target", "微信");
							break;
						case 3:
							map.put("target", "停车宝");
							break;
						case 4:
							String note = (String)map.get("note");
							String [] notes  = note.split("_");
							map.put("note",notes[0]);
							if(notes.length==2)
								map.put("target", notes[1]);
							else
								map.put("target","");
							break;
						default:
							break;
						}
					}
				}
			}else if(ptype==1){
				if(list!=null&&!list.isEmpty()){
					for(int i=0;i<list.size();i++){
						Map map = (Map)list.get(i);
						Integer type = (Integer)map.get("mtype");
						String remark = (String)map.get("r");
						if(type==0){
							if(remark.indexOf("_")!=-1){
								map.put("note", remark.split("_")[0]);
								map.put("target", remark.split("_")[1]);
							}
						}else if(type==1){
							map.put("note", "提现");
							map.put("target", "银行卡");
						}else if(type==2){
							map.put("note", "返现");
							map.put("target", "停车宝");
						}
						map.remove("r");
					}
				}
			}
			
		}
	}
	
	/*
	 * 设置车牌照片参数
	 */
	private void setPicParams(List list){
		List<Object> orderids = new ArrayList<Object>();
		if(list != null && !list.isEmpty()){
			for(int i=0;i<list.size();i++){
				Map map = (Map)list.get(i);
				orderids.add(map.get("id"));
			}
		}
		if(!orderids.isEmpty()){
			String preParams  ="";
			for(Object orderid : orderids){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
			resultList = daService.getAllMap("select * from car_picturs_tb where orderid in ("+preParams+") order by pictype", orderids);
			if(!resultList.isEmpty()){
				for(int i=0;i<list.size();i++){
					Map map1 = (Map)list.get(i);
					Long id=(Long)map1.get("id");
					for(Map<String,Object> map: resultList){
						Long orderid = (Long)map.get("orderid");
						if(id.intValue()==orderid.intValue()){
							Integer pictype = (Integer)map.get("pictype");
							if(pictype == 0){
								map1.put("lefttop", map.get("lefttop"));
								map1.put("rightbottom", map.get("rightbottom"));
								map1.put("width", map.get("width"));
								map1.put("height", map.get("height"));
								break;
							}
						}
					}
				}
			}
		}
	}
	/**
	 * 
	 * @param orderMap   订单
	 * @param prepaymoney   预支付金额
	 * @return
	 */
	private boolean prepayRefund(Map orderMap , Double prepaymoney){
		Long orderId = (Long)orderMap.get("id");
		Map<String, Object> ticketMap = daService.getMap(
				"select * from ticket_tb where orderid=? order by utime limit ?",
				new Object[] { orderId,1});
		DecimalFormat dFormat = new DecimalFormat("#.00");
		Double back = 0.0;
		List<Map<String, Object>> backSqlList = new ArrayList<Map<String,Object>>();
		if(ticketMap != null){
			logger.error(">>>>>>>>>>>>使用过券，ticketid:"+ticketMap.get("id")+",orderid="+orderId);
			Integer money = (Integer)ticketMap.get("money");
			Double umoney = Double.valueOf(ticketMap.get("umoney")+"");
			umoney = Double.valueOf(dFormat.format(umoney));
			back = Double.valueOf(dFormat.format(prepaymoney - umoney));
			logger.error(">>>>>>>>>>>预支付金额prefee："+prepaymoney+",使用券的金额umoney："+umoney+",应退款金额："+back+",orderid:"+orderId);
			Map<String, Object> tcbAccountsqlMap = new HashMap<String, Object>();
			tcbAccountsqlMap.put("sql", "insert into tingchebao_account_tb(amount,type,create_time,remark,utype,orderid) values(?,?,?,?,?,?)");
			tcbAccountsqlMap.put("values", new Object[]{umoney,0,System.currentTimeMillis() / 1000 ,"停车券返款金额", 6, orderId });
			backSqlList.add(tcbAccountsqlMap);
		}else{
			logger.error(">>>>>>>>>>>>没有使用过券>>>>>>>>>>>>>orderid:"+orderId);
			back = Double.valueOf(dFormat.format(prepaymoney));
		}
		Long uin = (Long)orderMap.get("uin");
		if(back > 0){
			Map count = daService.getPojo("select * from user_info_tb where id=? ", new Object[]{uin});
			Map<String, Object> usersqlMap = new HashMap<String, Object>();
			if(count != null){//真实帐户
				usersqlMap.put("sql", "update user_info_tb set balance=balance+? where id=? ");
				usersqlMap.put("values", new Object[]{back,uin});
				backSqlList.add(usersqlMap);
			}else{//虚拟账户
				usersqlMap.put("sql", "update wxp_user_tb set balance=balance+? where uin=? ");
				usersqlMap.put("values", new Object[]{back,uin});
				backSqlList.add(usersqlMap);
			}
			Map<String, Object> userAccountsqlMap = new HashMap<String, Object>();
			userAccountsqlMap.put("sql", "insert into user_account_tb(uin,amount,type,create_time,remark,pay_type,orderid) values(?,?,?,?,?,?,?)");
			userAccountsqlMap.put("values", new Object[]{uin,back,0,System.currentTimeMillis() / 1000 - 2,"现金结算预支付预支付返款", 12, orderId });
			backSqlList.add(userAccountsqlMap);
			boolean b = daService.bathUpdate(backSqlList);
			logger.error(">>>>>>>>>>预支付返款结果："+b+",orderid:"+orderId);
			try {
				String openid = "";
				if(count!=null)
					openid = count.get("wxp_openid")+"";
				if(!StringUtils.isNotNull(openid)){
					Map wx = daService.getPojo("select * from wxp_user_tb where uin=? ", new Object[]{uin});
					openid = wx.get("openid")+"";
				}
				if(!openid.equals("")){
					logger.error(">>>>>>>>>>>预支付后现金结算订单退回预支付款   微信推消息,uin:"+uin+",openid:"+openid);
					String first = "因现金结算，预支付退款";
					Map<String, String> baseinfo = new HashMap<String, String>();
					List<Map<String, String>> orderinfo = new ArrayList<Map<String,String>>();
					String url = "http://"+Constants.WXPUBLIC_REDIRECTURL+"/zld/wxpaccount.do?action=balance&openid="+openid;
					baseinfo.put("url", url);
					baseinfo.put("openid", openid);
					baseinfo.put("top_color", "#000000");
					baseinfo.put("templeteid", Constants.WXPUBLIC_BACK_NOTIFYMSG_ID);
					Map<String, String> keyword1 = new HashMap<String, String>();
					keyword1.put("keyword", "orderProductPrice");
					keyword1.put("value",back+"元");
					keyword1.put("color", "#000000");
					orderinfo.add(keyword1);
					Map<String, String> keyword2 = new HashMap<String, String>();
					keyword2.put("keyword", "orderProductName");
					keyword2.put("value", "预支付退款");
					keyword2.put("color", "#000000");
					orderinfo.add(keyword2);
					Map<String, String> keyword3 = new HashMap<String, String>();
					keyword3.put("keyword", "orderName");
					keyword3.put("value", orderId+"");
					keyword3.put("color", "#000000");
					orderinfo.add(keyword3);
					Map<String, String> keyword4 = new HashMap<String, String>();
					keyword4.put("keyword", "Remark");
					keyword4.put("value", "点击详情查账户余额！");
					keyword4.put("color", "#000000");
					orderinfo.add(keyword4);
					Map<String, String> keyword5 = new HashMap<String, String>();
					keyword5.put("keyword", "first");
					keyword5.put("value", first);
					keyword5.put("color", "#000000");
					orderinfo.add(keyword5);
					publicMethods.sendWXTempleteMsg(baseinfo, orderinfo);
				}
			} catch (Exception e) {
				logger.error("退回成功，消息发送失败");
				e.printStackTrace();
				return true;
			}
			logger.error("退回成功 ....");	
			
			return true;
		}else{
			logger.error(">>>>>>>>>>>>>>>退还金额back小于0，orderid："+orderId);
			return false;
		}
	}
	
	class ExeCallable implements Callable<Object>{
		private List<Map<String, Object>> list;
		private Long groupId = -1L;
		private int type;
		ExeCallable(List<Map<String, Object>> list, Long groupId, int type){
			this.list = list;
			this.groupId = groupId;
			this.type = type;
		}
		private Long parkId = -1L;
		ExeCallable(Long parkId, int type){
			this.parkId = parkId;
			this.type = type;
		}
		private Long berthSegId = -1L;
		private Long uid = -1L;
		private Long ntime = -1L;
		private String device_code;
		ExeCallable(Long berthSegId, Long uid, 
				Long ntime, String device_code, int type){
			this.berthSegId = berthSegId;
			this.uid = uid;
			this.ntime = ntime;
			this.device_code = device_code;
			this.type = type;
		}
		
		@Override
		public Object call() throws Exception {
			Object result = null;
			try {
				switch (type) {
				case 0://获取订单信息
					getOrderInfo(list, groupId);
					break;
				case 1:
					getSensorInfo(list);
					break;
				case 2:
					Map<String, Object> parkMap = pService.getMap("select company_name " +
							" from com_info_tb where id=? ", new Object[]{parkId});
					if(parkMap != null){
						result = parkMap.get("company_name");
					}
					break;
				case 3:
					result = getCarTypeInfo(parkId);
					break;
				case 4:
					result = signIn(berthSegId, ntime, uid, device_code);
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}
	}
}
