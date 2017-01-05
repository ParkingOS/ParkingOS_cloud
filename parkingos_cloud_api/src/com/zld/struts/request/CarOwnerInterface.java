package com.zld.struts.request;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import pay.Constants;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.zld.AjaxUtil;
import com.zld.CustomDefind;
import com.zld.easemob.main.HXHandle;
import com.zld.impl.CommonMethods;
import com.zld.impl.MemcacheUtils;
import com.zld.impl.MongoClientFactory;
import com.zld.impl.PublicMethods;
import com.zld.pojo.ParseJson;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.Check;
import com.zld.utils.HttpProxy;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.utils.ZldDesUtils;
import com.zld.utils.ZldMap;
import com.zld.utils.ZldXMLUtils;
/**
 * 车主2.0接口
 * @author Administrator
 * 20150415
 */
public class CarOwnerInterface extends Action {
	
	@Autowired
	private PgOnlyReadService onlyService;
	@Autowired
	private DataBaseService service;
	@Autowired
	private PublicMethods publicMethods; 
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Autowired
	private CommonMethods commonMethods;
	private Logger logger = Logger.getLogger(CarOwnerInterface.class);

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		//System.err.println(request.getParameterMap());
		logger.error("action:"+action);
		if(action.equals("parkinfo")){
			String ret = getParkInfo(request);
			logger.error(ret);
			AjaxUtil.ajaxOutput(response, ret);
			//读取停车场详情   http://127.0.0.1/zld/carinter.do?action=parkinfo&comid=8689
		}else if(action.equals("getcomment")){//读取评价
			String result = getComments(request);
			//读取停车场评论   http://127.0.0.1/zld/carinter.do?action=getcomment&comid=1197&page=1&mobile=15801482643
			AjaxUtil.ajaxOutput(response,result);
		}else if(action.equals("uppark")){//上传车场
			String ret = upPark(request);
			AjaxUtil.ajaxOutput(response, ret);
			//上传车场   http://192.168.199.240/zld/carinter.do?action=uppark&mobile=15801482513&parkname=aaa&desc=bbb&lng=116.317514&lat=40.043024&type=0
		}else if(action.equals("preverifypark")){//请求审核车场
			String 	ret = doPreVerifyPark(request);
			AjaxUtil.ajaxOutput(response, ret);
			//准备审核车场   http://192.168.199.240/zld/carinter.do?action=preverifypark&mobile=15801482643&lat=40.042474&lng=116.306970
		}else if(action.equals("verifypark")){//请求审核车场
			String 	ret = doVerifyPark(request);
			AjaxUtil.ajaxOutput(response, ret);
			//审核车场   http://192.168.199.240/zld/carinter.do?action=verifypark&mobile=15801482643&id=&isname=&islocal=&ispay=&isresume=
		}else if(action.equals("usetickets")){//选择停车券，排序：可用，金额（从大到小），有效期（快到期的在前）
			String result = useTickets(request);
			AjaxUtil.ajaxOutput(response, result);
			//使用停车券   http://192.168.199.240/zld/carinter.do?action=usetickets&mobile=15801482643&total=5&orderid=&uid=10700&preid=38878&utype=0
		}else if(action.equals("gettickets")){//查所有停车券
			String result = getallTickets(request);
			AjaxUtil.ajaxOutput(response, result);
			//使用停车券   http://192.168.199.240/zld/carinter.do?action=gettickets&mobile=15801482643&page=10&type=0
		}else if(action.equals("wxaccount")){//取微信停车券
			String result = getWxAccount(request);
			AjaxUtil.ajaxOutput(response, result);
			//查微信折扣代金券 http://127.0.0.1/zld/carinter.do?action=wxaccount&mobile=15801482643&uid=10700&total=15.99
		}else if(action.equals("orderdetail")){//微信预支付订单详情
			doOrderDetail(request);
			request.setAttribute("title", CustomDefind.getValue("TITLE"));
			request.setAttribute("desc", CustomDefind.getValue("DESCRIPTION"));
			return mapping.findForward("orderdetail");
			//查微信折扣代金券 http://192.168.199.239/zld/carinter.do?action=orderdetail&orderid=786119&prepay=1000
		}else if(action.equals("getwxbonus")){//领取微信红包
			Long id = RequestUtil.getLong(request, "id", -1L);
			int ret = doPreGetWeixinBonus(request,id);
			if(ret==1){
				String location ="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXPUBLIC_APPID+"&redirect_uri=http%3A%2F%2F"+Constants.WXPUBLIC_REDIRECTURL+"%2Fzld%2Fcarowner.do%3Faction%3Dgetobonus%26id%3D"+id+"%26operate%3Dcaibonus&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
				response.sendRedirect(location);
				return null;
			}else {
				request.setAttribute("tpic", "ticket_wx");
				request.setAttribute("mname", "折");
				request.setAttribute("isover", 0);
				return mapping.findForward("caibouns");
			}
//			return mapping.findForward(ret);
		}else if(action.equals("verifyrule")){//审核规则
			return mapping.findForward("verifyrule");
			//AjaxUtil.ajaxOutput(response, "images/verifyrule.png");
			//审核规则  http://192.168.199.240/zld/carinter.do?action=verifyrule
		}else if(action.equals("upfine")){//上传车场好处
			return mapping.findForward("upfine");
			//AjaxUtil.ajaxOutput(response, "images/upfine.png");
			//上传车场好处  http://192.168.199.240/zld/carinter.do?action=upfine
		}else if(action.equals("editmobile")){//红包修改手机号码
			String ret = editMobile(request);
			AjaxUtil.ajaxOutput(response, ret);
		}else if(action.equals("puserdetail")){
			String ret = puserDetail(request);
			AjaxUtil.ajaxOutput(response, ret);
			//收费员详情  http://192.168.199.240/zld/carinter.do?action=puserdetail&uid=21654
		}else if(action.equals("pusrcomments")){
			String ret = pcommDetail(request);
			ret = ret.replace("null", "");
			AjaxUtil.ajaxOutput(response, ret);
			//收费员评价详情  http://192.168.199.240/zld/carinter.do?action=pusrcomments&uid=10700
		}else if(action.equals("getcarnumbs")){//车主查询所有车牌号
			//http://192.168.199.240/zld/carinter.do?action=getcarnumbs&mobile=13641309140
			String cns = getCarNumbers(request);
			AjaxUtil.ajaxOutput(response, cns);
		}else if(action.equals("upuserpic")){//上传车主行驶证
			String result = uploadCarPics2Mongodb(request);
			logger.error(result);
			AjaxUtil.ajaxOutput(response, result);
			//http://192.168.199.240/zld/carinter.do?action=upuserpic&mobile=13641309140&carnumber=
		}else if(action.equals("getrewardquota")){
			Long uid = RequestUtil.getLong(request, "pid",-1L);
			Double rewardquota = 2.0;
			Map user = onlyService.getMap("select rewardquota from user_info_tb where id = ?", new Object[]{uid});
			if(user!=null&&user.get("rewardquota")!=null)
				rewardquota = StringUtils.formatDouble(user.get("rewardquota"));
			logger.error("收费员uid:"+uid+"的打赏用券额度："+rewardquota);
			AjaxUtil.ajaxOutput(response, rewardquota+"");
			//收费员打赏用券额度 http://localhost/zld/carinter.do?action=getrewardquota&pid=21732
		}else if(action.equals("getchargewords")){
			AjaxUtil.ajaxOutput(response, "充100，送充值礼包，认证用户专享");
			//充值提示： http://localhost/zld/carinter.do?action=getchargewords
		}else if(action.equals("prebuyticket")){//准备购买停车券
			String result = buyTicket(request);
			AjaxUtil.ajaxOutput(response, result);
			//购买停车券： http://192.168.199.240/zld/carinter.do?action=prebuyticket&mbile=18101333937
		}else if(action.equals("addorder")){//车主扫车位二维码，生成订单
			String ret =addOrder(request);
			AjaxUtil.ajaxOutput(response, ret);
		}else if(action.equals("getuiontickets")){//查看可合体的停车券
			String result = getUionTickets(request);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("reqticketuion")){//发起停车券合体
			String result = reqTikcetUion(request);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("ticketuioninfo")){//合体的信息，用于在响应合体的车主客户端显示过程
			AjaxUtil.ajaxOutput(response, ticketUionInfo(request));
		}else if(action.equals("preresticketuion")){//检查合体请求是否还有效
			AjaxUtil.ajaxOutput(response, preResTicketUion(request));
		}
		else if(action.equals("resticketuion")){//响应停车券合体
			String result = resTikcetUion(request);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("viewticketuion")){//查看停车券合体结果
			String result = viewTikcetUion(request);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getwxpcartic")){//返回微信公众号文章地址
			String ret = getWxpArtic(request);
			AjaxUtil.ajaxOutput(response, ret);
		}else if(action.equals("quickpay")){//快捷支付
			String ret = quickPay(request);
			AjaxUtil.ajaxOutput(response, ret);
			//http://localhost/zld/carinter.do?action=quickpay&mobile=13641309140
		}else if(action.equals("gethxheads")){//取环信好友头像
			AjaxUtil.ajaxOutput(response, getHxHeads(request));
			//http://localhost/zld/carinter.do?action=gethxheads&mobile=18811157723
		}else if(action.equals("preticketuion")){
			AjaxUtil.ajaxOutput(response, preTicketUion(request));
		}else if(action.equals("getparks")){//查询去停过车的车场
			AjaxUtil.ajaxOutput(response, getParks(request));
			//http://localhost/zld/carinter.do?action=getparks&mobile=13677226466
		}else if(action.equals("parkcars")){//查询这个车场停车的车主
			AjaxUtil.ajaxOutput(response, parkcars(request));
			//http://localhost/zld/carinter.do?action=parkcars&id=3251&mobile=15210932334
		}else if(action.equals("gethxname")){//根据账户查环信账户
			AjaxUtil.ajaxOutput(response, getHxName(request));
			//http://localhost/zld/carinter.do?action=gethxname&id=21776
		}else if(action.equals("preaddfriend")){//准备加好友，保存好友类型
			AjaxUtil.ajaxOutput(response, preAddFriend(request));
			//http://localhost/zld/carinter.do?action=preaddfriend&fhxname=hx21776&mobile=15210932334&type=&resume=
		}else if(action.equals("addfriend")){//加环信好友
			AjaxUtil.ajaxOutput(response, addFriend(request));
			//http://localhost/zld/carinter.do?action=addfriend&id=hx21800&mobile=15801482643
		}else if(action.equals("getfriendhead")){//取新的环信的好友微信头像及车牌号
			AjaxUtil.ajaxOutput(response, getNewFriendhead(request));
			//http://localhost/zld/carinter.do?action=getfriendhead&ids=hx21776,hx21770,hx21783
		}
		/****扬州项目app接口***/
//		else if(action.equals("Query_NearbyStatInfo")){//查询附近公交站点
//			//http://121.40.130.8/zld/carinter.do?action=Query_NearbyStatInfo&Longitude=119.394150&Latitude=32.387352&Range=2000
//			AjaxUtil.ajaxOutput(response, getNearbyStatInfo(request));
//		}
		else if(action.equals("getbusinfo")){//查询附近公交站点
			//http://service.yzjttcgs.com/zld/carinter.do?action=getbusinfo&Longitude=119.394150&Latitude=32.387352&Range=2000
			AjaxUtil.ajaxOutput(response, getNearbyStatInfo(request));
		}
//		else if(action.equals("RouteStatData")){//根据线路站点ID信息
//			AjaxUtil.ajaxOutput(response, getRouteStatData(request));
//			//http://121.40.130.8/zld/carinter.do?action=RouteStatData&RouteID=2023&TimeStamp=2016-03-15+13%3A301%3A11
//		}
		else if(action.equals("stationinfo")){//根据线路站点ID信息
			AjaxUtil.ajaxOutput(response, getRouteStatData(request));
			//http://127.0.0.1/zld/carinter.do?action=stationinfo&RouteID=1021  &timestamp=2016-03-15+13%3A301%3A11
		}else if(action.equals("getstatByName")){//根据线路站名称信息
			AjaxUtil.ajaxOutput(response, getStatByName(request));
			//http://121.40.130.8/zld/carinter.do?action=getstatByName&stationName=%25E6%259C%2588%25E4%25BA%25AE
		}else if(action.equals("getstatbyname")){//根据线路站名称信息
			AjaxUtil.ajaxOutput(response, getStatByName(request));
			//http://121.40.130.8/zld/carinter.do?action=getstatByName&stationName=%25E6%259C%2588%25E4%25BA%25AE
		}else if(action.equals("getstatbyid")){//实时信息查询_按站点ID 
			AjaxUtil.ajaxOutput(response, getStatById(request));
			//http://service.yzjttcgs.com/zld/carinter.do?action=getstatbyid&routeID=1021&stationID=    114423
		}else if(action.equals("getchargeinfo")){
			//http://127.0.0.1/zld/carinter.do?action=getchargeinfo&lng=119.394150&lat=32.387352
			AjaxUtil.ajaxOutput(response, getChargeInfo(request));
		}else if(action.equals("chargdetail")){
			//http://127.0.0.1/zld/carinter.do?action=chargdetail&id=3210030010
			AjaxUtil.ajaxOutput(response, chargeDetail(request));
		}else if(action.equals("getbikeall")){
			//http://127.0.0.1/zld/carinter.do?action=getbikeall&lng=119.387583&lat=32.396773
			AjaxUtil.ajaxOutput(response, "{\"bikelist\":"+getBikeAll(request)+"}");
		}else if(action.equals("bikedetail")){
			//http://service.yzjttcgs.com/zld/carinter.do?action=bikedetail&id=118
			AjaxUtil.ajaxOutput(response, bikeDetail(request));
		}else if(action.equals("bikehistory")){//租车记录
			//http://127.0.0.1/zld/carinter.do?action=bikehistory&mobile=18101333937
			AjaxUtil.ajaxOutput(response, bikeHistory(request));
		}else if(action.equals("prereservecar")){//准备预约车位
			//http://127.0.0.1/zld/carinter.do?action=prereservecar&comid=8689&mobile=18101333937
			AjaxUtil.ajaxOutput(response, preReserveCar(request));
		}else if(action.equals("reservecar")){//预约车位
			//http://service.yzjttcgs.com/zld/carinter.do?action=reservecar&later=1&comid=8689&mobile=18101333937&car_number=
			AjaxUtil.ajaxOutput(response, reserveCar(request));
		}else if(action.equals("canclereservecar")){//取消预约车位
			//http://service.yzjttcgs.com/zld/carinter.do?action=canclereservecar&orderid=112&mobile=18101333937
			AjaxUtil.ajaxOutput(response, cancleReserveCar(request));
		}else if(action.equals("findcar")){//反向寻车
			//http://service.yzjttcgs.com/zld/carinter.do?action=findcar&currplot=118&carplot=178&comid=8689&mobile=18560603731
			AjaxUtil.ajaxOutput(response, findCar(request));
		}else if(action.equals("getcarlocal")){//查询车辆所在位置
			//http://service.yzjttcgs.com/zld/carinter.do?action=getcarlocal&mobile=18560603731
			AjaxUtil.ajaxOutput(response, getCarLocal(request));
		}else if (action.equals("getalltaxi")){
			AjaxUtil.ajaxOutput(response, getAllTaxi(request));
		}else if(action.equals("getrescarorders")){//查询车场预约订单
			AjaxUtil.ajaxOutput(response, getReserveCarOrders(request));
		}else if(action.equals("rescarorderdetail")){
			//http://127.0.0.1/zld/carinter.do?action=rescarorderdetail&id=130
			AjaxUtil.ajaxOutput(response,resCarOrderDetail(request));
		}else if(action.equals("calltaxi")){
			AjaxUtil.ajaxOutput(response, callTaxi(request));
		}
		/*else if(action.equals("getalisign")){//取支付宝签名
			AjaxUtil.ajaxOutput(response, getAliSign(request));
		}*/
		else if(action.equals("carorderhistory")){
			AjaxUtil.ajaxOutput(response, historyCarOrder(request));
		}else if(action.equals("openbtlock")){
			//http://127.0.0.1/zld/carinter.do?action=openbtlock&plot=177
			AjaxUtil.ajaxOutput(response, openBLLock(request));
		}
		/****扬州项目app接口***/
		return null;
	}
	private String openBLLock(HttpServletRequest request) {
		String plotNo = RequestUtil.getString(request, "plot");
		/*KL959907328: A178车位
		KL940703361：A177车位*/
		String url = "";
		if(plotNo.equals("177")){
			url="http://www.no1parkinglock.com:8088/task/testAdd.action?numstatus=2&num=KL940703361";
		}else if(plotNo.equals("178")){
			url="http://www.no1parkinglock.com:8088/task/testAdd.action?numstatus=2&num=KL959907328";
		}
		return url;
	}
//	private String getAliSign(HttpServletRequest request) {
//		//http://service.yzjttcgs.com/zld/carinter.do?action=getalisign&content=
//		//http://127.0.0.1/zld/carinter.do?action=getalisign&partner=2088411488582814&seller_id=caiwu@zhenlaidian.com&out_trade_no=041616213922193&subject=测试的商品&body=该测试商品的详细描述&total_fee=0.01&notify_url=http://service.yzjttcgs.com/zld/rechage&service=mobile.securitypay.pay&payment_type=1&_input_charset=utf-8&it_b_pay=30m&return_url=m.alipay.com
//		Map map = request.getParameterMap();
//		Map<String, String> parMap = new HashMap<String, String>();
//		for(Object key :map.keySet()){
//			parMap.put(key.toString(),"\""+request.getParameter(key.toString())+"\"");
//		}
//		parMap.remove("action");
//		String sign = AlipayUtil.sign(parMap);
//		logger.error("ali pay origin content:"+parMap+",sign:"+sign);
//		return sign;
//	}
	private String callTaxi(HttpServletRequest request) {
		Double mylng =RequestUtil.getDouble(request, "mylng", 0.0);
		Double mylat =RequestUtil.getDouble(request, "mylat", 0.0);
		String mystation  = RequestUtil.getString(request, "mystation");
		
		Double desclng =RequestUtil.getDouble(request, "desclng", 0.0);
		Double desclat =RequestUtil.getDouble(request, "desclat", 0.0);
		String descstation =RequestUtil.getString(request, "descstation");
		String mobile = RequestUtil.getString(request, "mobile");
		
		String url = "http://192.168.6.189:8080/OrderSrv/example/callCarMyself?" +
				"action.mLongitude="+mylng+"&action.mLatitude="+mylat+"&action.addr="+mystation+"" +
				"&action.mobilenumber="+mobile+"&action.findRadius=1000&action.dest="+descstation+"" +
				"&action.destlng="+desclng+"&action.destlat="+desclat+"&action.callfee=0" +
				"&action.ddtj=3&action.tip=0&action.carpool=0&action.veltype=4";
		String result = new HttpProxy().doGet(url);
		
		return null;
	}
	private String resCarOrderDetail(HttpServletRequest request) {
		Long id= RequestUtil.getLong(request, "id", -1L);
		logger.error("reservecar order detail id:"+id);
		String result ="{}";
		if(id!=-1){
			Map<String, Object> map = service.getMap("select o.create_time,o.state,o.id,o.arrive_time," +
				"o.limit_time,o.car_number,o.plot_no,c.company_name,c.id parkid,c.parking_type from order_reserve_tb o left join" +
				" com_info_tb c on o.comid=c.id where o.id=? ", new Object[]{id});
			if(map!=null&&!map.isEmpty()){
				Map<String, Object> info = new HashMap<String, Object>();
				Long ctime = (Long)map.get("create_time");
				info.put("date", TimeTools.getTime_yyyyMMdd_HHmm(ctime*1000));
				Long atime = (Long)map.get("arrive_time");
				info.put("arriver_time", TimeTools.getTime_yyyyMMdd_HHmm(atime*1000).substring(11));
				Long limittime = (Long)map.get("limit_time");
				info.put("limit_time", TimeTools.getTime_yyyyMMdd_HHmm(limittime*1000).substring(11));
				Integer state = (Integer)map.get("state");
				String s = "未入场";//0:欠费 1:已补缴 2:未入场 3:已取消
				if(state==0){
					s="欠费 ";
				}else if(state==1){
					s="已补缴";
				}else if(state==3){
					s="已取消";
				}
				info.put("state", s);
				info.put("plotno",  map.get("plot_no"));
				info.put("parkname", map.get("company_name"));
				info.put("orderid", map.get("id"));
				//查图片
				Map<String,Object> picMap = onlyService.getMap("select picurl from com_picturs_tb where comid=? order by id desc limit ?",
						new Object[]{map.get("parkid"),1});
				String picUrls = "";
				if(picMap!=null&&!picMap.isEmpty()){
					picUrls=(String)picMap.get("picurl");//"http://121.40.130.8/zld/parkpics/"+
				}else{
					Integer parkType = (Integer)map.get("parking_type");
					if(parkType==4){
						picUrls="8674_1460623804316.png";
					}else {
						picUrls="8694_1460616855017.png";
					}
				}
				info.put("imgurl", picUrls);
				result = StringUtils.createJson(info);
				logger.error("reservecar order detail id:"+id+",result:"+result);
			}
		}
		return result;
	}
	private String getReserveCarOrders(HttpServletRequest request) {
		//http://121.40.130.8/zld/carinter.do?action=getrescarorders&mobile=18101333937
		//http://127.0.0.1/zld/carinter.do?action=getrescarorders&mobile=18101333937
		String mobile = RequestUtil.getString(request, "mobile");
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "size", 20);
		Long uin = getUinByMobile(mobile);
		List<Map<String, Object>> infoMaps = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> list = onlyService.getPage("select o.create_time,o.state,o.id,o.arrive_time," +
				"o.limit_time,o.car_number,o.plot_no,c.company_name from order_reserve_tb o left join" +
				" com_info_tb c on o.comid=c.id where o.uin =? and o.type=? order by o.id desc",
				new Object[]{uin,0}, pageNum, pageSize);
		if(list!=null&&list.size()>0){
			for(Map map : list){
				Map<String, Object> info = new HashMap<String, Object>();
				Long ctime = (Long)map.get("create_time");
				info.put("date", TimeTools.getTime_yyyyMMdd_HHmm(ctime*1000));
				Long atime = (Long)map.get("arrive_time");
				info.put("arriver_time", TimeTools.getTime_yyyyMMdd_HHmm(atime*1000).substring(11));
				Long limittime = (Long)map.get("limit_time");
				info.put("limit_time", TimeTools.getTime_yyyyMMdd_HHmm(limittime*1000).substring(11));
				Integer state = (Integer)map.get("state");
				String s = "未入场";//0:欠费 1:已补缴 2:未入场 3:已取消
				if(state==0){
					s="欠费 ";
				}else if(state==1){
					s="已补缴";
				}else if(state==3){
					s="已取消";
				}
				info.put("state", s);
				info.put("plotno",  map.get("plot_no"));
				info.put("parkname", map.get("company_name"));
				info.put("orderid", map.get("id"));
				infoMaps.add(info);
			}
		}
		if(!infoMaps.isEmpty()){
			return "{\"booklist\":"+StringUtils.createJson(infoMaps)+"}";
		}
		return "[]";
	}
	private String getAllTaxi(HttpServletRequest request) {
		//http://service.yzjttcgs.com/zld/carinter.do?action=getalltaxi&mobile=18101333937&lng=119.387583&lat=32.396773&range=200
		String mobile =RequestUtil.getString(request, "mobile");
		Double lng =RequestUtil.getDouble(request, "lng", 0.0);
		Double lat =RequestUtil.getDouble(request, "lat", 0.0);
//		lng = lng-0.0073;
//		lat = lat-0.00575 ;
		Integer range = RequestUtil.getInteger(request, "range", 600);
		String result = new HttpProxy().doGet(CustomDefind.getValue("WEBSERVICECLIENTURL")+"?type=taxi&action=getalltaxi" +
		//String result = new HttpProxy().doGet("http://172.16.230.2:8080/wsclient/tsclient?type=taxi&action=getalltaxi" +
								"&mobile="+mobile+"&lng="+lng+"" +
				"&lat="+lat+"&range="+range);
//		System.out.println(result);
		String ret = "{\"total\":4,\"footer\":[],\"success\":true,\"msg\":\"操作成功！\",\"rows\":[{\"sex\":\"男\",\"empcode\":\"4110246812062617\",\"tel\":\"15052525285\",\"carno\":\"苏KV9048\",\"mtype\":\"捷达\",\"starlevel\":0,\"veltype\":1,\"id\":1,\"simno\":\"15396774156\",\"distance\":0.422,\"ownername\":\"大众出租公司\",\"name\":\"柴长雨\",\"longitude\":119.38902,\"latitude\":32.396557},{\"sex\":\"男\",\"empcode\":\"3210026702272437\",\"tel\":\"13813199080\",\"carno\":\"苏KBV147\",\"mtype\":\"桑塔纳2型\",\"starlevel\":0,\"veltype\":1,\"id\":2,\"simno\":\"18051446414\",\"distance\":0.427,\"ownername\":\"个体出租\",\"name\":\"王俊\",\"longitude\":119.388862,\"latitude\":32.396363},{\"sex\":\"女\",\"empcode\":\"3210026807011540\",\"tel\":\"13815835067\",\"carno\":\"苏KAW815\",\"mtype\":\"桑塔纳2型\",\"starlevel\":0,\"veltype\":1,\"id\":3,\"simno\":\"18051447747\",\"distance\":0.514,\"ownername\":\"个体出租\",\"name\":\"陈恒香\",\"longitude\":119.387823,\"latitude\":32.396238},{\"sex\":\"女\",\"empcode\":\"3206117408312648\",\"tel\":\"13004315738\",\"carno\":\"苏KBV867\",\"mtype\":\"桑塔纳3000\",\"starlevel\":0,\"veltype\":1,\"id\":4,\"simno\":\"18051446454\",\"distance\":0.69,\"ownername\":\"个体出租\",\"name\":\"吴海云\",\"longitude\":119.385927,\"latitude\":32.39639}]}";
		if(result!=null)
			return result;
		return ret;//"{\"total\":2,\"success\":true,\"msg\":\"操作成功！\",\"rows\":[{\"id\":1,\"longitude\":118.888888,\"latitude\":32.222222,\"carno\":\"苏K66666\",\"simno\":\"13073222222\",\"tel\":\"13075555555\",\"name\":\"张三\",\"sex\":\"男\",\"starlevel\":\"5星\",\"ownername\":\"鸿运\",\"empcode\":\"A2334\",\"veltype\":\"东风\",\"mtype\":\"ADEEE\",\"ordercount\":50,\"cancelcount\":20,\"praiserate\":27.1},{\"id\":2,\"longitude\":118.8444888,\"latitude\":32.222222,\"carno\":\"苏A12345\",\"simno\":\"13075555555\",\"tel\":\"13075555555\",\"name\":\"李四\",\"sex\":\"男\",\"starlevel\":\"4星\",\"ownername\":\"鸿运\",\"empcode\":\"AAASA\",\"veltype\":\"捷达\",\"mtype\":\"126585\",\"ordercount\":50,\"cancelcount\":20,\"praiserate\":27.1}]}";
	}
	private String getCarLocal(HttpServletRequest request) {
		//http://127.0.0.1/zld/carinter.do?action=getcarlocal&mobile=18101333937
		String mobile = RequestUtil.getString(request, "mobile");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String carNumber = "";
		Map carMap = onlyService.getMap("select car_number from car_info_tb where uin=" +
				" (select id from user_info_tb where mobile=? and auth_flag=? )", new Object[]{mobile,4});
		if(carMap!=null&&!carMap.isEmpty()){
			carNumber = (String)carMap.get("car_number");
		}
		if(carNumber==null||carNumber.trim().length()!=7){
			resultMap.put("state", 0);
			resultMap.put("errmsg", "车牌不合法");
		}else{
			//carNumber="苏KA7406";
			logger.error("get cal car_number:"+carNumber);
			Map<String, Object> carLocalInfo =getCarPlot(carNumber);
			if(!carLocalInfo.isEmpty()){
				String plot =  (String)carLocalInfo.get("plot");
				resultMap.put("comid",carLocalInfo.get("comid"));
				resultMap.put("parkname", carLocalInfo.get("parkname"));
				resultMap.put("plot", carLocalInfo.get("plot"));
				if(plot!=null&&!"".equals(plot.trim()))
					resultMap.put("state", 1);
				else {
					resultMap.put("state", 1);
				}
				resultMap.put("errmsg", "");
			}else {
				resultMap.put("comid","");
				resultMap.put("parkname", "");
				resultMap.put("plot", "");
				resultMap.put("state", -1);
				resultMap.put("errmsg", "");
			}
		}
		logger.error("get cal local:"+resultMap);
		return StringUtils.createJson(resultMap);
	}
	private String bikeHistory(HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String mobile = RequestUtil.getString(request, "mobile");
		//查询是否注册过身份证或市民卡
		Map<String, Object> userMap = getUserByMobile(mobile);
		String certNo =(String)userMap.get("certno");//身份证卡号
		String actNo = "9616900760075187";//(String)userMap.get("actno");//市民卡号
		if(certNo==null&&actNo==null){
			resultMap.put("state", -1);
			resultMap.put("errmsg", "请输入市民卡号或身份证号查询");
			resultMap.put("data", "[]");
		}else {
			String url ="http://yxiudongyeahnet.vicp.cc/wsclient/tsclient?type=bike&action=history&pageno=0&pagesize=100&mobile="+mobile;
			if(actNo!=null)
				url +="&actno="+actNo;
			else 
				url +="&certno="+certNo;
			Long etime = System.currentTimeMillis();
			Long btime = etime-15*24*3600*1000;//默认查15天的
			String bstr = TimeTools.getTime_yyyyMMdd_HHmmss(btime);
			String estr = TimeTools.getTime_yyyyMMdd_HHmmss(etime);
			bstr = bstr.replaceAll("-", "").replaceAll(":", "").replaceAll(" ","").trim();
			estr = estr.replaceAll("-", "").replaceAll(":", "").replaceAll(" ","").trim();
			url +="&btime="+bstr+"&etime="+estr;
			String result = new HttpProxy().doGet(url);
			Map<String, Object> retMap = null;
			if(result!=null){
				retMap = ZldXMLUtils.parserStrXml(result);
			}
			if(retMap!=null&&!retMap.isEmpty()){
				resultMap.put("state", 1);
				resultMap.put("count", retMap.get("reccount"));
				String data="[]";
				String retInfo =(String) retMap.get("retinfo");
				if(retInfo!=null&&retInfo.indexOf("|")!=-1){
					if(retInfo.startsWith("|"))
						retInfo = retInfo.substring(1);
					String [] info  = retInfo.split("\\|");
					List<Map<String, Object>> dataList = new ArrayList<Map<String,Object>>();
					for(int i=0;i<info.length;i+=5){
						Map<String, Object> dataMap = new HashMap<String, Object>();
						//dataMap.put("id", info[i]);
						dataMap.put("start", info[1]);
						dataMap.put("borrlocation", info[2]);
						dataMap.put("end", info[3]);
						dataMap.put("droplocation", info[4]);
						dataList.add(dataMap);
					}
					resultMap.put("data", StringUtils.createJson(dataList));
				}
			}else {
				resultMap.put("state", 0);
				resultMap.put("errmsg", "未查询到记录");
				resultMap.put("data", "[]");
			}
		}
		
		return StringUtils.createJson(resultMap);
	}
	private String cancleReserveCar(HttpServletRequest request) {
		//http://127.0.0.1/zld/carinter.do?action=canclereservecar&orderid=101&mobile=18101333937

		Long id  = RequestUtil.getLong(request, "orderid", -1L);
		String mobile = RequestUtil.getString(request, "mobile");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> user = getUserByMobile(mobile);
		Map<String, Object> orderMap = service.getMap("select * from order_reserve_tb where id= ? and uin =? and state=?  ",
				new Object[]{id,user.get("id"),2});
		if(orderMap!=null){
			String car_number = (String)orderMap.get("car_number");
			Long endTime = (Long)orderMap.get("limit_time");
			Long ntime = System.currentTimeMillis()/1000;
//			if(ntime>endTime){//已过预约最晚进场时间
//				resultMap.put("satae", -2);
//				resultMap.put("errmsg", "已超过取消时间");
//			}else {
				//调用移动接口
				String url = CustomDefind.getValue("WEBSERVICECLIENTURL") +
						"?type=mobilews&action=canclereserve&car_number="+AjaxUtil.encodeUTF8(AjaxUtil.encodeUTF8(car_number));
				String outXml = new HttpProxy().doGet(url);
				String code =null;
				try {
					JSONObject object = new JSONObject(outXml);
					code = object.getString("Code");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(code!=null&&code.equals("1000")){
					int ret = service.update("update order_reserve_tb set state =? where id =? ", new Object[]{3,orderMap.get("id")});
					if(ret==1){
						resultMap.put("satae", 1);
						resultMap.put("errmsg", "已取消车位预约");
					}else {
						resultMap.put("satae", -1);
						resultMap.put("errmsg", "已取消车位预约");
					}
				}
//			}
		}else {
			resultMap.put("satae", 0);
			resultMap.put("errmsg", "订单不存在");
		}
		return StringUtils.createJson(resultMap);
	}
	//反向寻车
	private String findCar(HttpServletRequest request) {
		//http://127.0.0.1/zld/carinter.do?action=findcar&currplot=A15&carplot=A1&comid=8551&mobile=18101333937
		
		Map<String, Object> resultMap = new HashMap<String, Object>();	
		//车主当前所在车位
		String currPlot = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "currplot"));
		String carPlot = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "carplot"));
		Long comId = RequestUtil.getLong(request, "comid",-1L);
		String mobile = RequestUtil.getString(request, "mobile");
		Map carMap = onlyService.getMap("select car_number from car_info_tb where uin =" +
				"(select id from user_info_tb where mobile=? and auth_flag=?)", new Object[]{mobile,4});
		String carNumber = null;
		logger.error("find car,currplot:"+currPlot+",carplot:"+carPlot+",comid:"+comId);
		if(carMap!=null)
			carNumber = (String)carMap.get("car_number");
		if(carNumber==null||carNumber.trim().equals("")){
			resultMap.put("state", -1);
			resultMap.put("errmsg", "您未注册车牌");
			resultMap.put("imgurl", "");
			return StringUtils.createJson(resultMap);
		}
		///Map<String, Object> carPlotResult = getCarPlot(carNumber);
			//String ParkingId = (String)carPlotResult.get("comid");
		if(carPlot==null){
			resultMap.put("state", -2);
			resultMap.put("errmsg", "没有查到停车信息");
			resultMap.put("imgurl", "");
		}else {
			String imgurl= getFindCarImg(comId, currPlot, carPlot);
			if(imgurl!=null&&!"".equals(imgurl)){
				resultMap.put("state", 1);
				resultMap.put("errmsg", "");
				resultMap.put("imgurl", imgurl);
			}else {
				resultMap.put("state", -2);
				resultMap.put("errmsg", "没有查到停车信息");
				resultMap.put("imgurl", "");
			}
		}
		logger.error("findcar image:"+resultMap);
		return StringUtils.createJson(resultMap);
	}
	//调用海信接口
	private String getFindCarImg(Long comid, String currPlot, String carPlot) {
		String url = new HttpProxy().doGet(CustomDefind.getValue("WEBSERVICECLIENTURL") +"?type=findcar&parkid="+comid+"&sid="+currPlot+"&eid="+carPlot);
		return url;
	}
	//移动接口调用车主停车所在位置
	private Map<String, Object> getCarPlot(String carNumber) {
		String reslut = new HttpProxy().doGet(CustomDefind.getValue("WEBSERVICECLIENTURL") +"" +
				"?type=mobilews&action=findcar&car_number="+AjaxUtil.encodeUTF8(AjaxUtil.encodeUTF8(carNumber)));
		Map<String, Object> retMap = new HashMap<String, Object>();
		try {
			JSONObject object = new JSONObject(reslut);
			String prarId = object.getString("ParkingId");
			if(prarId!=null){
				Map comMap = onlyService.getMap("select company_name,id from com_info_tb where park_uuid=? ", new Object[]{prarId+"0000000000000000000000000000000"});
				if(comMap!=null){
					retMap.put("parkname", comMap.get("company_name"));
					retMap.put("comid",comMap.get("id"));
				}
			}
			retMap.put("plot",object.getString("CarportNo") );
		} catch (JSONException e) {
			logger.error("返回寻车,用户车牌："+carNumber+"，接口返回："+reslut+"，解析错误："+e.getMessage());
			e.printStackTrace();
		}
		logger.error("get cal loacl interface return:"+retMap);
		return retMap;
	}
	//开始预约车位 
	private String reserveCar(HttpServletRequest request) {
		//http://127.0.0.1/zld/carinter.do?action=reservecar&later=1&comid=8689&mobile=18101333937
		String mobile = RequestUtil.getString(request, "mobile");
		String comid = RequestUtil.getString(request, "comid");
		String carNumber =AjaxUtil.decodeUTF8(AjaxUtil.decodeUTF8(RequestUtil.getString(request, "car_number")));
		//几小时后到达
		Integer later = RequestUtil.getInteger(request, "later", 1);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Double p1 = 6.0;//1小时价格
		Double p2 = 8.0;//2小时价格
		Double balance = 7.0;//余额
		//String carNumber = "";
		Map<String, Object> userMap= getUserByMobile(mobile);
		if(userMap!=null){
			balance = StringUtils.formatDouble(userMap.get("balance"));
//			Map<String, Object> carMap = onlyService.getMap("select car_number from car_info_tb where uin = ? ", new Object[]{userMap.get("id")});
//			if(carMap!=null)
//				carNumber = (String) carMap.get("car_number");
		}
		logger.error("预约停车：预计"+later+"小时后到达,用户余额："+ balance+",车牌："+carNumber );
		/*if(carNumber==null||"".equals(carNumber.trim())){
			resultMap.put("state", -1);
			resultMap.put("plot", "");
			resultMap.put("errmsg", "请先注册车牌");
			return StringUtils.createJson(resultMap);
		}else if(carNumber.length()!=7){
			resultMap.put("state", -2);
			resultMap.put("plot", "");
			resultMap.put("errmsg", "车牌不合法:"+carNumber);
			return StringUtils.createJson(resultMap);
		}*/
		//计算预约车费，与余额对比
		boolean isCanDate=false;
		if(balance>0){//根据预约时间判断余额是否充足
			if(later==1)
				isCanDate = (balance>=p1);
			else if(later==2){
				isCanDate = (balance>=p2);
			}
		}
		if(carNumber.length()!=7){
			resultMap.put("state", 0);
			resultMap.put("errmsg", "请选择车牌");
		}else if(isCanDate){//可以预约
			
			//调用扬州移动的预约接口
			Long ntime =System.currentTimeMillis()/1000;
			Map comMap = service.getMap("select park_uuid from com_info_tb where id =? ", new Object[]{Long.valueOf(comid)});
			String plot = null;
			if(comMap!=null&&comMap.get("park_uuid")!=null){
				String comNo = (String)comMap.get("park_uuid");
				plot = getPlot(carNumber,comNo.substring(0,5),TimeTools.getTime_yyyyMMdd_HHmmss((ntime+later*3600)*1000),TimeTools.getTime_yyyyMMdd_HHmmss((ntime+(later+1)*3600)*1000));
			}
			if(plot!=null&&!"".equals(plot)){//预约成功
				
				Long id = service.getkey("seq_order_reserve_tb");
				int ret = service.update("insert into order_reserve_tb(id,comid,uin,create_time,arrive_time,limit_time,state,car_number,type,plot_no)" +
						"values(?,?,?,?,?,?,?,?,?,?)", new Object[]{id,Long.valueOf(comid),userMap.get("id"),ntime,ntime+later*3600,ntime+(later+1)*3600,2,carNumber,0,plot});
				if(ret==1){
					resultMap.put("state", 1);
					resultMap.put("plot", plot);
					resultMap.put("orderid", id);
					resultMap.put("errmsg", "预约成功！");
				}else {
					resultMap.put("state", 0);
					resultMap.put("errmsg", "车位已满，请稍候再试！");
				}
			}else {//预约失败
				resultMap.put("state", 0);
				resultMap.put("errmsg", "车位已满，请稍候再试！");
			}
		}else {//余额不足
			resultMap.put("state", -3);
			resultMap.put("errmsg", "余额不足！");
		}
		return StringUtils.createJson(resultMap);
	}
	private String getPlot(String car_number,String comid,String btime,String etime) {
		String url = CustomDefind.getValue("WEBSERVICECLIENTURL") +
				"?type=mobilews&action=reservecar&parkid="+comid+"&car_number="+AjaxUtil.encodeUTF8(AjaxUtil.encodeUTF8(car_number))+"&btime="+AjaxUtil.encodeUTF8(btime)+"&etime="+AjaxUtil.encodeUTF8(etime);
		logger.error("resvercar wsurl :"+url);
		String outXml = new HttpProxy().doGet(url);
		logger.error("resvercar ws result :"+outXml);
		String plot = "";
		try {
			JSONObject object = new JSONObject(outXml);
			String code = object.getString("Code");
			if(code!=null&&code.equals("1000")){
				plot = object.getString("BerthNo");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return plot;
	}
	//准备预约车位
	private String preReserveCar(HttpServletRequest request) {
		//http://127.0.0.1/zld/carinter.do?action=prereservecar&comid=8689&mobile=18101333937

		Long parkId = RequestUtil.getLong(request, "comid", -1L);
		String mobile = RequestUtil.getString(request, "mobile");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		//验证车牌
		String carNumbers ="[";
		List<Map<String, Object>> carList = onlyService.getAll("select car_number from car_info_tb where uin =(" +
				"select id  from user_info_tb where mobile=? ) ", new Object[]{mobile});
		if(carList!=null&&!carList.isEmpty()){
			for(Map<String, Object> map : carList){
				carNumbers += "\""+(String)map.get("car_number")+"\",";
			}
		}
		if(carNumbers.endsWith(","))
			carNumbers = carNumbers.substring(0,carNumbers.length()-1);
		carNumbers +="]";
		resultMap.put("reserve_max_hour", 2);
		Map<String, Object> comMap = onlyService.getMap("select book from com_info_tb where id =? ", new Object[]{parkId});
		if(comMap==null||comMap.get("book").toString().equals("0")){
			resultMap.put("state", 0);
			resultMap.put("total", 0);
			resultMap.put("free", 0);
			resultMap.put("errmsg", "");
			resultMap.put("orderid", "");
			resultMap.put("car_numbers", carNumbers);
			return StringUtils.createJson(resultMap);
		}
		resultMap.put("price", "首小时5元，两小时内6元");
		
		if(carNumbers.equals("[]")){
			resultMap.put("state", -1);
			resultMap.put("total", 0);
			resultMap.put("free", 0);
			resultMap.put("orderid", "");
			resultMap.put("car_numbers", carNumbers);
			resultMap.put("errmsg", "请先注册车牌");
			return StringUtils.createJson(resultMap);
		}
		//查询可预约车位
		Map oMap= onlyService.getMap("select id,limit_time from order_reserve_tb where state=? and  uin =(select id from " +
				"user_info_tb where mobile=? and auth_flag=? ) ",new Object[]{2,mobile,4});
		if(oMap!=null&&!oMap.isEmpty()){//已有预约订单，未结算
			resultMap.put("state", -2);
			resultMap.put("total", 0);
			resultMap.put("free", 0);
			resultMap.put("orderid", oMap.get("id"));
			Long limitTime = (Long)oMap.get("limit_time");
			if(limitTime!=null&&limitTime>0){
				resultMap.put("limit_time",TimeTools.getTime_yyyyMMdd_HHmm(limitTime*1000) );
			}
			resultMap.put("car_numbers", carNumbers);
			resultMap.put("errmsg", "已有预约订单");
			return StringUtils.createJson(resultMap);
		}
		Long count = onlyService.getLong("select count(id) from order_reserve_tb where comid=? and state=? ", new Object[]{parkId,2});
		if(count>1){//车位已满
			resultMap.put("state", -3);
			resultMap.put("total", 0);
			resultMap.put("free", 0);
			resultMap.put("orderid", "");
			resultMap.put("car_numbers", carNumbers);
			resultMap.put("errmsg", "预约车位已满");
			return StringUtils.createJson(resultMap);
		}
		resultMap.put("state", 1);
		resultMap.put("total", 2);
		resultMap.put("free", 2-count);
		resultMap.put("errmsg", "");
		resultMap.put("car_numbers", carNumbers);
		resultMap.put("orderid", "");
		return StringUtils.createJson(resultMap);
	}
	//查询充电桩详情
	private String chargeDetail(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "id", -1L);
		List<Map<String, Object>> chargeList = getChargeSite();
		Map<String, Object> retMap = new HashMap<String, Object>();
		String result = "{}";
		if(!chargeList.isEmpty()){
			for(Map<String, Object> map :chargeList){
				if(!retMap.isEmpty())
					break;
				//计算距离
				Long idLong= Long.valueOf(map.get("staCode")+"");
				if(idLong==null||id.intValue()!=idLong.intValue()){
					continue;
				}
				//转换字段名称
				retMap.put("addr", map.get("staAddress"));
				retMap.put("id", idLong);
				retMap.put("lng", map.get("lng"));
				retMap.put("lat", map.get("lat"));
				retMap.put("name", map.get("staName"));
				Integer ac = Integer.valueOf(map.get("acNum")+"");
				Integer acable = Integer.valueOf(map.get("acableNum")+"");
				Integer dc = Integer.valueOf(map.get("dcNum")+"");
				Integer dcable = Integer.valueOf(map.get("dcableNum")+"");
				retMap.put("total", ac+dc);
				retMap.put("free", acable+dcable);
				retMap.put("price", map.get("price")+"元/千瓦时");
				retMap.put("desc", ac+"个交流充电桩，"+acable+"个可用；"+dc+"个直流充电桩，"+dcable+"个可用");
			}
		}
		//http://127.0.0.1/zld/carinter.do?action=chargdetail&id=3210030009

//		String c1 = "{\"id\":\"1\",\"name\":\"新城西区商务中心\",\"lng\":\"119.390696\",\"lat\":\"32.395396\"," +
//				"\"total\":\"12\",\"addr\":\"文昌西路525号\",\"desc\":\"10个交流充电桩，2个交流充电桩\",\"price\":\"1.47元/KW(服务费)\",\"free\":\"12\"}" ;
//		String c2= "{\"id\":\"2\",\"name\":\"客运站地下停车场\",\"lng\":\"119.356177\",\"lat\":\"32.392036\"," +
//				"\"total\":\"6\",\"addr\":\"邗江区扬州火车站旁边\",\"desc\":\"6个直流充电桩\",\"price\":\"1.47元/KW(服务费)\",\"free\":\"6\"}" ;
//		String result ="{}";
//		if(id==1)
//			result =c1;
//		else if(id==2){
//			result=c2;
//		}
		result = StringUtils.createJson(retMap);
		return result;
	}
	//查询附近充电桩站点，目前是虚数据
	private String getChargeInfo(HttpServletRequest request) {
		//{\"pageCount\":\"1\",\"itemCount\":\"3\",\"staList\":[{\"staCode\":\"3210030010\",\"staName\":\"扬州交通特来电充电站\",\"staType\":\"公共站\",\"staOpstate\":\"运营中\",\"province\":\"江苏省\",\"city\":\"扬州市\",\"region\":\"邗江区\",\"staAddress\":\"江苏省扬州市邗江区扬州市文昌西路525号\",\"lng\":\"119.390196\",\"lat\":\"32.395320\",\"price\":\"1.63\",\"acNum\":\"2\",\"dcNum\":\"10\",\"acableNum\":\"2\",\"dcableNum\":\"5\"},{\"staCode\":\"3210030011\",\"staName\":\"扬州西部客运枢纽地下停车场充电站\",\"staType\":\"公共站\",\"staOpstate\":\"运营中\",\"province\":\"江苏省\",\"city\":\"扬州市\",\"region\":\"邗江区\",\"staAddress\":\"江苏省扬州市邗江区扬州市火车站西侧\",\"lng\":\"119.356392\",\"lat\":\"32.392111\",\"price\":\"1.63\",\"acNum\":\"0\",\"dcNum\":\"5\",\"acableNum\":\"0\",\"dcableNum\":\"0\"},{\"staCode\":\"3210030009\",\"staName\":\"扬州蜀岗西峰客栈充电站\",\"staType\":\"公共站\",\"staOpstate\":\"运营中\",\"province\":\"江苏省\",\"city\":\"扬州市\",\"region\":\"邗江区\",\"staAddress\":\"江苏省扬州市邗江区北路蜀冈玫瑰园对面\",\"lng\":\"119.413693\",\"lat\":\"32.420896\",\"price\":\"1.63\",\"acNum\":\"1\",\"dcNum\":\"2\",\"acableNum\":\"0\",\"dcableNum\":\"0\"}]}
		Double lng =RequestUtil.getDouble(request, "lng", 0.0);
		Double lat =RequestUtil.getDouble(request, "lat", 0.0);
		List<Map<String, Object>> chargeList = getChargeSite();
		String reslut = "{}";
		if(!chargeList.isEmpty()){
			List<Map<String, Object>> retList = new ArrayList<Map<String,Object>>();
			for(Map<String, Object> map :chargeList){
				//计算距离
				Map<String, Object> retMap = new HashMap<String, Object>();
				Double lng1 =Double.valueOf(map.get("lng")+"");
				Double lat1 =Double.valueOf(map.get("lat")+"");
				double distance = StringUtils.distanceByLnglat(lng,lat,lng1,lat1);
				Integer d=  StringUtils.formatDouble(distance*1000).intValue();
				retMap.put("distance", d);
				//转换字段名称
				retMap.put("addr", map.get("staAddress"));
				retMap.put("id", map.get("staCode"));
				retMap.put("lng", map.get("lng"));
				retMap.put("lat", map.get("lat"));
				retMap.put("name", map.get("staName"));
				Integer ac = Integer.valueOf(map.get("acNum")+"");
				Integer acable = Integer.valueOf(map.get("acableNum")+"");
				Integer dc = Integer.valueOf(map.get("dcNum")+"");
				Integer dcable = Integer.valueOf(map.get("dcableNum")+"");
				retMap.put("total", ac+dc);
				retMap.put("price", map.get("price")+"元/千瓦时");
				retMap.put("free", acable+dcable);
				retMap.put("desc", ac+"个交流充电桩，"+acable+"个可用；"+dc+"个直流充电桩，"+dcable+"个可用");
				retList.add(retMap);
			}
			reslut = "{\"suggid\":\"\",\"data\":"+StringUtils.createJson(retList)+"}";
		}
		//http://127.0.0.1/zld/carinter.do?action=getchargeinfo&lng=119.394150&lat=32.387352

//		String reslut = "{\"suggid\":\"\",\"data\":[{\"id\":\"1\",\"name\":\"新城西区商务中心\",\"lng\":\"119.390696\",\"lat\":\"32.395396\",\"total\":\"12\",\"addr\":\"文昌西路525号\",\"desc\":\"10个交流充电桩，2个交流充电桩\",\"distance\":\""+d1+"\"}," +
//				"{\"id\":\"2\",\"name\":\"客运站地下停车场\",\"lng\":\"119.356177\",\"lat\":\"32.392036\",\"total\":\"6\",\"addr\":\"邗江区扬州火车站旁边\",\"desc\":\"6个直流充电桩\",\"distance\":\""+d2+"\"}]}" ;
		//logger.error("充电桩返回："+reslut);
		return reslut;
	}
	//自行车站点详情
	private String bikeDetail(HttpServletRequest request) {
		Long id =RequestUtil.getLong(request, "id",-1L);
		//http://127.0.0.1/zld/carinter.do?action=bikedetail&id=120
		//http://service.yzjttcgs.com/zld/carinter.do?action=bikedetail&id=120
		//String url ="http://172.16.220.32:8080/wsclient/tsclient?type=bike&action=detail&id="+id ;
		//String result = new HttpProxy().doGet(url);
		//{ret=0, zdaddr=翠岗路路南, wdinfo=32.396822, totalcws=32, zdname=职大北门, jdinfo=119.382417, leftcws=29}
		Map<String, Object> retMap = new HashMap<String, Object>();
		/*if(result!=null){
			Map<String, Object> resultMap = ZldXMLUtils.parserStrXml(result);
			if(resultMap!=null){
				retMap.put("name", resultMap.get("zdname"));
				retMap.put("address", resultMap.get("zdaddr"));
				retMap.put("lng", resultMap.get("jdinfo"));
				retMap.put("lat", resultMap.get("wdinfo"));
				retMap.put("price", "首小时免费，后1元/小时，最高20元每天");
				retMap.put("id", id);
				retMap.put("total_count", resultMap.get("totalcws"));
				retMap.put("left_count", resultMap.get("leftcws"));
			}
		}*/
		if(retMap.isEmpty()){
			Map bikeMap = onlyService.getMap("select * from city_bike_tb where id =? ", new Object[]{id});
			if(bikeMap!=null&&!bikeMap.isEmpty()){
				retMap.put("name", bikeMap.get("name"));
				retMap.put("address", bikeMap.get("address"));
				retMap.put("lng", bikeMap.get("longitude"));
				retMap.put("lat", bikeMap.get("latitude"));
				retMap.put("price", "首小时免费，后1元/小时，最高20元每天");
				retMap.put("id", id);
				retMap.put("total_count", bikeMap.get("plot_count"));
				Integer free = (Integer)bikeMap.get("surplus");
//				free = free-new Random().nextInt(2);
//				free=free<=0?5:free;
				retMap.put("left_count",free);
			}
		}
		return StringUtils.createJson(retMap);
	}
	//查询附近两公里范围内的自行车租用站点
	private String getBikeAll(HttpServletRequest request) {
		//http://127.0.0.1/zld/carinter.do?action=getbikeall&lng=119.387583&lat=32.396773
		updateBikeInfo();
		//double lon1 = 0.023482756;//两公里内
		//double lat1 = 0.017978752;
		double lon1 = 0.015482756/2;//一公里内
		double lat1 = 0.014978752/2;
		Double lng =RequestUtil.getDouble(request, "lng", 0.0);
		Double lat =RequestUtil.getDouble(request, "lat", 0.0);
//		lng = lng-0.0053;
//		lat = lat-0.00575 ;
		String sql = "select id,name,address,longitude lng,latitude lat,plot_count,surplus" +
				" from city_bike_tb where longitude between ? and ? " +
				"and latitude between ? and ?  ";
		List<Object> params = new ArrayList<Object>();
		params.add(lng-lon1);
		params.add(lng+lon1);
		params.add(lat-lat1);
		params.add(lat+lat1);
		List<Map<String, Object>> result = service.getAll(sql, params,0,0);
		logger.error("get bike all :lng:"+lng+",lat:"+lat);
		if(result!=null){
			for(Map<String, Object> map:result){
				double lon2 = Double.valueOf(map.get("lng")+"");
				double lat2 = Double.valueOf(map.get("lat")+"");
				double distance = StringUtils.distanceByLnglat(lng,lat,lon2,lat2);
				map.put("distance", StringUtils.formatDouble(distance*1000).intValue());
			}
			return StringUtils.createJson(result);
		}
		else
			return "[]";
	}
	//查询公交站点详情
	private String getStatById(HttpServletRequest request) {
		Long routeId = RequestUtil.getLong(request, "routeID", -1L);
		Long stationId = RequestUtil.getLong(request, "stationID", -1L);
		HttpProxy httpProxy = new HttpProxy();
		String url =CustomDefind.getValue("BUSURL") +"Query_ByStationID/?routeID="+routeId+"&stationID="+stationId;
		String result =httpProxy.doGet(url);
		logger.error("url:"+url+",result："+result);
		if(result==null)
			result="[]";
		else {
			result="{\"data\":"+result+"}";
		}
		return result;
	}
	//模糊查询公交站点
	private String getStatByName(HttpServletRequest request){
		String StationName =RequestUtil.getString(request, "stationName");
		
		HttpProxy httpProxy = new HttpProxy();
		String url =CustomDefind.getValue("BUSURL") +"Query_ByStaNameNE/?StationName="+StationName;
		String result =httpProxy.doGet(url);
		logger.error("url:"+url+",result："+result);
		if(result==null)
			result="[]";
		else {
			result="{\"data\":"+result+"}";
		}
		return result;
		
	}
	//根据线路查询站点信息
	private String getRouteStatData(HttpServletRequest request) {
		Long routeID = RequestUtil.getLong(request, "RouteID", -1L);
		Long time = RequestUtil.getLong(request, "time", -1L);
		///carinter.do?action=stationinfo&routeid=2023&time=2016-03-15+13%3A301%3A11
		if(time==-1)
			time = System.currentTimeMillis()/1000;
		String timeStamp = TimeTools.getTime_yyyyMMdd_HHmmss(time*1000);
		
		HttpProxy httpProxy = new HttpProxy();
		String url =CustomDefind.getValue("BUSURL") +"Require_RouteStatData/?RouteID="+routeID+"&TimeStamp="+AjaxUtil.encodeUTF8(timeStamp);
		String result =httpProxy.doGet(url);
		//logger.error("url:"+url+",result："+result);
		if(result==null)
			result="[]";
		else {
			/**
			 * {"RouteID":1021,"RunBusNum":0,"RStaRealTInfoList":[{"StationID":"102002","RStanum":1,"ExpArriveBusStaNum":1,"StopBusStaNum":0,"BusType":0},{"StationID":"101414","RStanum":5,"ExpArriveBusStaNum":1,"StopBusStaNum":0,"BusType":0},{"StationID":"105184","RStanum":12,"ExpArriveBusStaNum":1,"StopBusStaNum":0,"BusType":0},{"StationID":"100241","RStanum":14,"ExpArriveBusStaNum":1,"StopBusStaNum":0,"BusType":0},{"StationID":"104544","RStanum":19,"ExpArriveBusStaNum":0,"StopBusStaNum":1,"BusType":0}],"IsEnd":null}
			* {"RouteID":1021,"RunBusNum":0,"RStaRealTInfoList":[{"StationID":"100262","RStanum":3,"ExpArriveBusStaNum":0,"StopBusStaNum":1,"BusType":0},{"StationID":"100243","RStanum":11,"ExpArriveBusStaNum":1,"StopBusStaNum":0,"BusType":0},{"StationID":"114373","RStanum":16,"ExpArriveBusStaNum":1,"StopBusStaNum":0,"BusType":0},{"StationID":"101982","RStanum":18,"ExpArriveBusStaNum":1,"StopBusStaNum":0,"BusType":0},{"StationID":"105972","RStanum":22,"ExpArriveBusStaNum":1,"StopBusStaNum":0,"BusType":0}],"IsEnd":null}
			 */
			List<Map<String, Object>> info = ParseJson.jsonToList1(result);
			try {
				for(Map<String, Object> map : info){
					List<Map<String, Object>> segmentList=(List<Map<String, Object>>)map.get("SegmentList");
					for(Map<String, Object> map1: segmentList){
						String url1  = "http://218.91.52.117:8999/BusService/Query_ByRouteID?RouteID="+routeID+"&Segmentid="+map1.get("SegmentID");
						String result1 =httpProxy.doGet(url1);
						List<Map<String, Object>> StationList =(List<Map<String, Object>>)map1.get("StationList");
						Map<String, Object> SegmentMap = ParseJson.jsonToMap1(result1);
						for(Map<String, Object> map3 : StationList){
							String sid = (String)map3.get("StationID");
							List<Map<String, Object>> RStaRealTInfoList = (List<Map<String, Object>>)SegmentMap.get("RStaRealTInfoList");
							map3.put("expArriveBusStaNum","0");
							map3.put("stopBusStaNum","0");
							for(Map<String, Object> map4:RStaRealTInfoList ){
								String sid1 = (String)map4.get("StationID");
								if(sid.equals(sid1)){
									map3.put("expArriveBusStaNum", map4.get("ExpArriveBusStaNum"));
									map3.put("stopBusStaNum", map4.get("StopBusStaNum"));
									break;
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			String json = ParseJson.createJson(info);
			result="{\"data\":"+json+"}";
			logger.error("result："+result);
		}
		return result;
	}
	//查询附近指定范围内的公交车站点
	private String getNearbyStatInfo(HttpServletRequest request) {
		Double lng = RequestUtil.getDouble(request, "Longitude", 0d);
		Double lat = RequestUtil.getDouble(request, "Latitude", 0d);
		Integer range =700;// RequestUtil.getInteger(request, "Rang", 1800);
//		lng = lng-0.0053;
//		lat = lat-0.00575 ;
		///carinter.do?action=getbusinfo&lng=119.394150&lat=32.387352&rang=200
		if(range<1000)
			range=1000;
		HttpProxy httpProxy = new HttpProxy();
		String url =CustomDefind.getValue("BUSURL") +"Query_NearbyStatInfo/?Longitude="+lng+"&Latitude="+lat+"&Range="+range;
		String result = httpProxy.doGet(url);
		if(result==null)
			result="[]";
		else {
			//result="{\"buslist\":"+result+"}";
		}
		logger.error("公交数据成功返回：lng:"+lng+",lat:"+lat);
		//Long count = onlyService.getCount("select count(id) from bus_station_tb ", null);
		//if(count==0){
			JSONArray array=null;
			List<Map<String, Object>> busList = new ArrayList<Map<String,Object>>();
			try {
				array = new JSONArray(result);
				if(array!=null&&array.length()>0){
					for(int i=0;i<array.length();i++){
						Map<String, Object> busMap = new HashMap<String, Object>();
						JSONObject object = array.getJSONObject(i);
						for (Iterator<String> iter = object.keys(); iter.hasNext();) { 
					       String key = iter.next();
					       Object value = object.get(key);
					       if(value==null||value.toString().toLowerCase().trim().equals("null"))
					    	   value="";
					       if(key.equals("StationPostion")){
					    	   JSONObject subObject = new JSONObject(value.toString());
					    	   busMap.put("Longitude", subObject.get("Longitude"));
					    	   busMap.put("Latitude", subObject.get("Latitude"));
					       }else if(key.equals("Distance")){
					    	   busMap.put(key, StringUtils.formatDouble(value));
					       }else {
					    		   busMap.put(key, value);
//					    	   if(key.equals("Distance"))
//					    		   busMap.put(key, StringUtils.formatDouble(value));
//					    	   else {
//					    	   }
					       }
						 }
						busList.add(busMap);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			result = "{\"buslist\":"+StringUtils.createJson(busList)+"}";
//			if(!busList.isEmpty()){
//				logger.error("开始写库....");
//				String sql = "insert into bus_station_tb (station_id,station_name,longitude,latitude," +
//						"station_memo,create_time,update_time) values(?,?,?,?,?,?,?)";
//				List<Object[]> values = new ArrayList<Object[]>();
//				Long ntime = System.currentTimeMillis()/1000;
//				for(Map<String, Object> map : busList){
//					Object []params= new Object[]{map.get("StationID"),map.get("StationName"),map.get("Longitude"),
//							map.get("Latitude"),map.get("StationMemo"),ntime,ntime};
//					values.add(params);
//				}
//				int ret = service.bathInsert(sql, values, new int[]{4,12,3,3,12,4,4});
//				logger.error("写入公交数据："+ret+"条");
//			}
		//}
			//logger.error("成功返回："+result);
		return result;
	}


	//准备加好友，保存好友类型
	private String preAddFriend(HttpServletRequest request) {
		String mobile = RequestUtil.getString(request, "mobile");
		String fname = RequestUtil.getString(request, "fhxname");
		Long buin = getUinByMobile(mobile);
		Long euin =-1L;
		if(!fname.equals("")&&Check.isLong(fname.substring(2)))
			euin = Long.valueOf(fname.substring(2));
		//"user_preaddfriend_tb_buin_euin_key"
		String result = "{\"result\":\"0\",\"errmsg\":\"账号异常\"}";
		Integer type = RequestUtil.getInteger(request, "type", 1);
		String resume = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "resume"));
		System.err.println("<<<<<<<<<<<resume:"+resume);
		Long ntime = System.currentTimeMillis()/1000;
		int ret = 0;
		if(buin>1&&euin>1){
			try {
				ret=service.update("insert into user_preaddfriend_tb(buin,euin,ctime,utime,atype,resume)" +
						" values(?,?,?,?,?,?)", new Object[]{buin,euin,ntime,ntime,type,resume});
				logger.error(mobile+",preaddhxfriend,euin:"+euin+",ret:"+ret);
				result = "{\"result\":\"1\",\"errmsg\":\"保存成功\"}";
			} catch (Exception e) {
				if(e.getMessage().indexOf("user_preaddfriend_tb_buin_euin_key")!=-1){//重复添加了，更新一下就行了
					ret = service.update("update user_preaddfriend_tb set atype=?,resume=?,utime=? where " +
							"buin=? and euin=? ", new Object[]{type,resume,ntime,buin,euin});
					logger.error(mobile+",preaddhxfriend error ,update data,euin:"+euin+",ret:"+ret);
					result = "{\"result\":\"1\",\"errmsg\":\"已经存在,更新成功\"}";
				}
			}
			
		}
		logger.error(mobile+",preaddhxfriend,result:"+result);
		return result;
	}
	//取新的环信的好友微信头像及车牌号
	private String getNewFriendhead(HttpServletRequest request) {
		String _ids = RequestUtil.getString(request, "ids");
		String result = "[]";
		if(!"".equals(_ids)){
			List<Object> params = new ArrayList<Object>();
			String perParams = "";
			if(_ids.indexOf(",")!=-1){
				String []ids = _ids.split(",");
				if(ids.length>0){
					for(String id : ids){
						if(Check.isLong(id.substring(2))){
							perParams +=",?";
							params.add(Long.valueOf(id.substring(2)));
						}
					}
				}
			}else {
				String id =_ids.substring(2);
				if(Check.isLong(id)){
					perParams = ",?";
					params.add(Long.valueOf(id));
				}
			}
			if(!params.isEmpty()){
				List<Map<String, Object>> userList = onlyService.getAllMap("select u.id,u.hx_name,wx_imgurl,car_number from user_info_tb u left join car_info_tb c on c.uin = u.id " +
						" where u.id in("+perParams.substring(1)+")", params);
				List<Map<String, Object>> ulList = new ArrayList<Map<String,Object>>();
				List<Long> uList = new ArrayList<Long>();
				if(userList!=null&&!userList.isEmpty()){
					for(Map<String, Object> map: userList){
						Long uin =(Long)map.get("id");
						if(uList.contains(uin))
							continue;
						else {
							uList.add(uin);
						}
						String carNumber = (String)map.get("car_number");
						if(carNumber==null||carNumber.equals(""))
							carNumber = "车牌号未知";
						else if(carNumber.length()==7)
							carNumber = carNumber.substring(0,4)+"***"+carNumber.substring(6);
						map.put("car_number", carNumber);
						map.remove("id");
						ulList.add(map);
					}
					result= StringUtils.createJson(ulList);
				}
			}
		}
		logger.error("result:"+result);
		return result;
	}
	//根据账户查环信账户
	private String getHxName(HttpServletRequest request) {
		Long euin = RequestUtil.getLong(request, "id", -1L);
		String ret  ="{\"result\":\"-1\",\"errmsg\":\"车主不存在！\",\"hxname\":\"\"}";
		if(euin>0){
			boolean isFriendAddHx =false;
			//查询被加车主是否已注册环信账户
			Map userMap = onlyService.getMap("select id,hx_name from user_info_tb where id =? ", new Object[]{euin});
			if(userMap!=null&&!userMap.isEmpty()){
				String hxName = (String)userMap.get("hx_name");
				if(hxName==null||hxName.equals("")){
					String hxPass = publicMethods.getHXpass(euin);
					isFriendAddHx= HXHandle.reg("hx"+euin,hxPass);
					if(isFriendAddHx){
						int r = service.update("update user_info_tb set hx_name=?,hx_pass=? where id = ?", new Object[]{"hx"+euin,hxPass,euin});
						isFriendAddHx = r==1?true:false;
						if(r==1)
							ret  ="{\"result\":\"1\",\"errmsg\":\"\",\"hxname\":\""+hxName+"\"}";
					}
				}else {
					ret  ="{\"result\":\"1\",\"errmsg\":\"\",\"hxname\":\""+hxName+"\"}";
				}
			}
		}
		return ret;
	}
	//加环信好友
	private String addFriend(HttpServletRequest request) {
		String ret  ="{\"result\":\"0\",\"errmsg\":\"添加失败\"}";
		//被加的车主
		String fid = RequestUtil.getString(request, "id");
		String mobile = RequestUtil.getString(request, "mobile");
		Long buin =-1L;
		//车主
		if(!"".equals(fid)&&!mobile.equals("")){
			boolean isFriendAddHx =false;
			Long euin = -1L;
			if(Check.isLong(fid.substring(2)))
				euin = Long.valueOf(fid.substring(2));
			else {
				ret  ="{\"result\":\"-1\",\"errmsg\":\"车主不存在！\"}";
				return ret;
			}
			//查询被加车主是否已注册环信账户
			Map userMap = onlyService.getMap("select id,hx_name from user_info_tb where id =? ", new Object[]{euin});
			if(userMap!=null&&!userMap.isEmpty()){
				String hxName = (String)userMap.get("hx_name");
				if(hxName==null||hxName.equals("")){
					String hxPass = publicMethods.getHXpass(euin);
					isFriendAddHx= HXHandle.reg("hx"+euin,hxPass);
					if(isFriendAddHx){
						int r = service.update("update user_info_tb set hx_name=?,hx_pass=? where id = ?", new Object[]{"hx"+euin,hxPass,euin});
						isFriendAddHx = r==1?true:false;
					}
				}else {
					isFriendAddHx = true;
				}
			}else {
				ret  ="{\"result\":\"-1\",\"errmsg\":\"车主不存在！\"}";
			}
			boolean isOwnAddHx=false;
			if(isFriendAddHx){
				userMap = onlyService.getMap("select id,hx_name from user_info_tb where mobile=? and auth_flag=?  ", new Object[]{mobile,4});
				if(userMap!=null&&!userMap.isEmpty()){
					String hxName = (String)userMap.get("hx_name");
					buin = (Long)userMap.get("id");
					if(hxName==null||hxName.equals("")){
						String hxPass = publicMethods.getHXpass(buin);
						isOwnAddHx= HXHandle.reg("hx"+euin,publicMethods.getHXpass(buin));
						if(isOwnAddHx){
							int r = service.update("update user_info_tb set hx_name=?,hx_pass=? where id = ?", new Object[]{"hx"+buin,hxPass,buin});
							isOwnAddHx = r==1?true:false;
						}
					}else {
						isOwnAddHx=true;
					}
				}else {
					ret  ="{\"result\":\"-2\",\"errmsg\":\"您的账户异常！\"}";
				}
			}
			if(isOwnAddHx&&isFriendAddHx){//双方都已加入环信账户，登记不zld好友表
				Long count = onlyService.getLong("select count(ID) from user_friend_tb where buin=? and euin=? ", new Object[]{buin,euin});
				if(count<1){
					Map preAddMap = onlyService.getMap("select atype from user_preaddfriend_tb where buin=? and euin=? ", new Object[]{euin,buin});
					Integer atype=1;
					logger.error(mobile+",addfriend,preAddMap:"+preAddMap);
					if(preAddMap!=null&&!preAddMap.isEmpty()){
						atype = (Integer)preAddMap.get("atype");
						int r = service.update("delete from user_preaddfriend_tb where buin=? and euin=?", new Object[]{euin,buin});
						logger.error(mobile+",addfriend,preAddMap deleted:"+r);
					}
					int r1 = service.update("insert into user_friend_tb (buin,euin,ctime,atype,is_add_hx)" +
							"values(?,?,?,?,?)", new Object[]{buin,euin,System.currentTimeMillis()/1000,atype,1});
					logger.error(mobile+",加车主环信好友："+euin+",ret:"+r1);
					if(r1==1){
						int r2=service.update("insert into user_friend_tb (buin,euin,ctime,atype,is_add_hx)" +
								"values(?,?,?,?,?)", new Object[]{euin,buin,System.currentTimeMillis()/1000,atype,1});
						logger.error(euin+",车主环信好友："+mobile+",ret:"+r2);
						if(r2==1){
							ret  ="{\"result\":\"1\",\"errmsg\":\"添加成功！\"}";
							//给双方发消息
							logger.error(mobile+",buin:"+buin+",euin:"+euin);
							logger.error("buin-euin sendmessage:"+HXHandle.sendMsg("hx"+buin, "hx"+euin, "对方通过了你的好友请求，你们已经是好友了。"));
							logger.error("euin-buin sendmessage:"+HXHandle.sendMsg("hx"+euin, "hx"+buin, "你通过了对方的好友请求，你们已经是好友了。" ));
						}
					}
				}else {
					ret  ="{\"result\":\"1\",\"errmsg\":\"他们已经是好友了\"}";
				}
			}
		}else {
			ret  ="{\"result\":\"-3\",\"errmsg\":\"添加失败，请稍候重试！\"}";
		}
		logger.error(mobile+",buin:"+buin+",euin:"+fid+",ret:"+ret);
		return ret;
	}

	//查询这个车场停车的车主
	private String parkcars(HttpServletRequest request) {
		Long comId = RequestUtil.getLong(request, "id", -1L);
		String mobile = RequestUtil.getString(request, "mobile");
		Long uin = getUinByMobile(mobile);
		logger.error(uin+",mobile:"+mobile+",comId:"+comId);
		if(comId>0){
			List<Map<String, Object>> userList = onlyService.getAll("select u.id,wx_imgurl,car_number from user_info_tb u left join car_info_tb c on c.uin = u.id " +
					" where u.id in(select uin from order_tb where comid =? and uin not in(?,?) order by id desc) ", new Object[]{comId,uin,-1L});
			if(userList!=null&&!userList.isEmpty()){
				List<Map<String, Object>> friendList = onlyService.getAll("select euin from user_friend_tb where buin=?  ", new Object[]{uin});
				List<Map<String, Object>> preFriendList= onlyService.getAll("select buin,euin from user_preaddfriend_tb where buin=? or euin=?  ", new Object[]{uin,uin});
				logger.error("preFriendList:"+preFriendList);
				List<Long> friends = new ArrayList<Long>();
				if(friendList!=null&&!friendList.isEmpty()){
					for(Map<String, Object> fmap: friendList){
						friends.add((Long)fmap.get("euin"));
					}
				}
				for(Map<String, Object> map: userList){
					String carNumber = (String)map.get("car_number");
					if(carNumber==null||carNumber.equals(""))
						carNumber = "车牌号未知";
					else if(carNumber.length()==7)
						carNumber = carNumber.substring(0,4)+"***"+carNumber.substring(6);
					map.put("car_number", carNumber);
//					String wxImgUrl = (String)map.get("wx_imgurl");
//					if(wxImgUrl==null||"".equals(wxImgUrl)){
//						wxImgUrl="images/bunusimg/logo.png";
//						map.put("wx_imgurl", wxImgUrl);
//					}
					map.put("isfriend", "0");
					Long cid = (Long)map.get("id");
					if(friends.contains(cid)){
						map.put("isfriend", "1");
					}else if(preFriendList!=null&&!preFriendList.isEmpty()){
						for(Map<String, Object> fMap : preFriendList){
							Long buin = (Long)fMap.get("buin");
							Long euin = (Long)fMap.get("euin");
							if(buin.equals(uin)&&euin.equals(cid)){
								map.put("isfriend", "2");
								break;
							}else if(buin.equals(cid)&&euin.equals(uin)){
								map.put("isfriend", "3");
								break;
							}
						}
					}
					//map.remove("id");
				}
				String ret = StringUtils.createJson(userList);
				ret = ret.replace("null", "");
				logger.error(ret);
				return ret;
			}
		}
		return "[]";
	}

	//查询去停过车的车场
	private String getParks(HttpServletRequest request) {
		String mobile = RequestUtil.getString(request, "mobile");
		Long uin = getUinByMobile(mobile);
		if(uin>0){
			List parkList = onlyService.getAll("select id,company_name as name from com_info_tb where id in(" +
					"select comid  from order_tb where uin = ? order by id desc) limit ? ", new Object[]{uin,10});
			if(parkList!=null&&!parkList.isEmpty())
				return StringUtils.createJson(parkList);
		}
		return "[]";
	}

	private String preResTicketUion(HttpServletRequest request) {
		//http://localhost/zld/carinter.do?action=preresticketuion&mobile=18811157723&id=426
		String mobile = RequestUtil.getString(request, "mobile");
		Long id = RequestUtil.getLong(request, "id", -1L);
		logger.error(mobile+",id:"+id);
		Map<String, Object> retMap = ZldMap.getMap(new String[]{"result","errmsg"} , new Object[]{1,"可以合体"});
		Map uionMap = service.getMap("select * from ticket_uion_tb where id =?  ", new Object[]{id});
		
		if(uionMap!=null){
			Integer s = (Integer)uionMap.get("state");
			if(s==0){
				
				String _touin = RequestUtil.getString(request, "touin");
				String _uin = RequestUtil.getString(request, "uin");
				Long touin=-1L;
				if(_touin.indexOf("hx")!=-1)
					touin= Long.valueOf(_touin.substring(2));
				Long uin =-1L;
				if(_uin.indexOf("hx")!=-1)
					uin= Long.valueOf(_uin.substring(2));
				Long count  = service.getLong("select count(id) from ticket_uion_tb where ((req_uin =? and res_uin=?) or (res_uin =? and req_uin=?) ) and req_time > ?", 
						new Object[]{uin,touin,uin,touin,TimeTools.getToDayBeginTime()});
				logger.error(uin+".....resuin:"+uin+",requin:"+touin+",今日合体次数："+count);
				if(count>0){
					retMap.put("result", "-2");
					retMap.put("errmsg", "今天你们已经合体过，不能再合体了，要注意身体哟！");
					logger.error("preResTicketUion>>>>>>>>info:"+retMap);
					return StringUtils.createJson(retMap);
				}
				
				Map<String, Object> reqMap= service.getMap("select id,state,limit_day,money,resources,limit_day,pmoney from ticket_tb" +
						" where id = ? ", new Object[]{uionMap.get("req_tid")});
				Integer state = (Integer)reqMap.get("state");
				if(state!=null&&state==1){
					retMap.put("result", "-1");
					retMap.put("errmsg", "停车券已使用");
				}else {
					Long limitDay = (Long)reqMap.get("limit_day");
					if(limitDay<(System.currentTimeMillis()/1000)){
						retMap.put("result", "-1");
						retMap.put("errmsg", "停车券已过期");
					}
				}
			}
		}else {
			retMap = ZldMap.getMap(new String[]{"result","errmsg"} , new Object[]{-1,"合体请求已不存在"});
		}
		logger.error("preResTicketUion>>>>>>>>info:"+retMap);
		return StringUtils.createJson(retMap);
	}

	/**
	 * 合体前合体信息....
	 * @param request
	 * @return
	 */
	private String ticketUionInfo(HttpServletRequest request) {
		//http://192.168.199.240/zld/carinter.do?action=ticketuioninfo&mobile=13641309140&tid=46401&id=461
		String mobile = RequestUtil.getString(request, "mobile");
		Long tid = RequestUtil.getLong(request, "tid", -1L);
		Long id = RequestUtil.getLong(request, "id", -1L);
		logger.error(mobile+",id:"+id+",tid:"+tid);
		Long uin = getUinByMobile(mobile);
		//计算请求合体的合体值
		Map reqTicketMap = null;
		Map uionMap = service.getMap("select * from ticket_uion_tb where id =? and state=? ", new Object[]{id,0});
		Long pretid =-1L;
		Long reqNumber=0L;
		Long ntime = TimeTools.getToDayBeginTime();
		int ret =1;
		String mesg ="";
		Integer reqmoney= 0;//请求合体的合体值
		String friend="{}";
		String own ="{}";
		if(uionMap!=null&&!uionMap.isEmpty()){
			pretid =(Long)uionMap.get("req_tid");
			reqTicketMap= service.getMap("select id,money,resources,limit_day,pmoney from ticket_tb where id = ? and state=? ", new Object[]{pretid,0});
			if(reqTicketMap!=null&&!reqTicketMap.isEmpty()){
				reqmoney=(Integer)reqTicketMap.get("money");
				Integer res =  (Integer)reqTicketMap.get("resources");
				Long limitDay = (Long)reqTicketMap.get("limit_day");
				reqNumber = (reqmoney*2+(limitDay-ntime)/(24*60*60))*(res+1);
				friend ="{\"uiontotal\":\""+reqNumber+"\"}";
			}else {
				ret=-4;
				mesg = "合体请求失败,停车券已使用";
			}
		}else {
			ret=-3;
			mesg = "合体请求失败，合体已取消";
		}
		
		//计算响应合体的合体值
		Long resNumber=0L;//响应合体的合体值
		Integer resmoney =0;
		Map resTicketMap = null;
		if(reqNumber>0){
			resTicketMap = service.getMap("select id,money,resources,limit_day,pmoney from ticket_tb where id = ? and state=? ", new Object[]{tid,0});
			if(resTicketMap!=null&&!resTicketMap.isEmpty()){
				resmoney = (Integer)resTicketMap.get("money");
				if((resmoney+reqmoney)%2==0){
					if(reqmoney%2==0){
						mesg = "合体失败,双方同为偶数";
						ret =-2;
					}else{
						mesg = "合体失败,双方同为奇数";
						ret=-1;
						
					}
					logger.error(mobile+">>>>>合体失败："+mesg);
				}else {
					Integer res =  (Integer)resTicketMap.get("resources");
					Long limitDay = (Long)resTicketMap.get("limit_day");
					resNumber = (resmoney*2+(limitDay-ntime)/(24*60*60))*(res+1);
					own="{\"ticketvalue\":\""+resmoney*2+"\",\"expvalue\":\""+(limitDay-ntime)/(24*60*60)+"\",\"buyvalue\":\""+(res+1)+"\",\"uiontotal\":\""+resNumber+"\"}";
				}
			}else {
				ret =-5;
				mesg = "响应合体的停车券已过期";
			}
		}
		Map<String, Object> resultMap = ZldMap.getMap(new String[]{"result","errmsg","own","friend","winrate"}, 
				new Object[]{ret,mesg,own,friend,resNumber+"/"+(resNumber+reqNumber)});
		String result = StringUtils.createJson(resultMap);
		logger.error(uin+",响应合体，合体信息："+result);
		return result;
	}

	/**
	 * 取环信好友头像
	 * @param request
	 * @return 微信名，微信头像地址，加入好友原因，车牌号
	 */
	private String getHxHeads(HttpServletRequest request) {
		String mobile = RequestUtil.getString(request, "mobile");
		String hxName = RequestUtil.getString(request, "hxname");
		String ret  ="{}";
		Long uin = getUinByMobile(mobile);
		if("".equals(hxName)){//只有mobil参数时，取所有好友信息
			//好友微信名，头像地址
			List friends = onlyService.getAll("select id,wx_name,wx_imgurl from user_info_tb where  id in " +
					"(select euin from user_friend_tb where buin=? and is_add_hx=?)", new Object[]{uin,1});
			//好友来源
			List fList = onlyService.getAll("select euin,atype from user_friend_tb where buin=?   and is_add_hx=? ",
					new Object[]{uin,1});
			//好友车牌
			List carList = onlyService.getAll("select uin,car_number from car_info_tb where uin in " +
					"(select euin from user_friend_tb where buin=? and is_add_hx=?) and state=? order by id desc", new Object[]{uin,1,1});
			if(friends!=null&&!friends.isEmpty()){
				for(int i=0;i<friends.size();i++){
					Map map = (Map)friends.get(i);
					Long id = (Long)map.get("id");
					//设置来源
					for(int j=0;i<fList.size();j++){
						Map map2 = (Map)fList.get(j);
						Long fuin = (Long)map2.get("euin");
						if(id.equals(fuin)){
							Integer atype = (Integer)map2.get("atype");
							if(atype!=null){
								if(atype==0)
									map.put("source","打灰机");
								else if(atype==1)
									map.put("source","同车场车友");
								else {
									map.put("source","未知");
								}
							}
							break;
						}
					}
					//设置车牌
					for(int k=0;k<carList.size();k++){
						Map map2 = (Map)carList.get(k);
						Long fuin = (Long)map2.get("uin");
						if(id.equals(fuin)){
							String carNumber = (String)map2.get("car_number");
							if(carNumber!=null&&carNumber.length()==7)
								carNumber=carNumber.substring(0,4)+"***"+carNumber.substring(6);
							map.put("carnumber",carNumber);
							break;
						}
					}
					map.put("id", "hx"+map.get("id"));
				}
			}
			ret = StringUtils.createJson(friends);
		}else {//取单个好友信息
			Long fuin = Long.valueOf(hxName.substring(2));
			Map userMap = onlyService.getMap("select id,wx_name,wx_imgurl from user_info_tb where id =? ", new Object[]{uin});
			Map fMap = onlyService.getMap("select euin,atype from user_friend_tb where buin=? and euin=?  and is_add_hx=? ",
					new Object[]{uin,fuin,1});
			Integer atype = (Integer)fMap.get("atype");
			if(atype!=null&&atype==0)
				userMap.put("source","打灰机");
			else {
				userMap.put("source","未知");
			}
			userMap.put("id", "hx"+userMap.get("id"));
			userMap.put("carnumber", publicMethods.getCarNumber(fuin));
			ret = StringUtils.createJson(userMap);
		}
		ret = ret.replace("null", "");
		//logger.error(">>>>friends:"+ret);
		return ret;
	}
	private String quickPay(HttpServletRequest request) {
		String mobile = RequestUtil.getString(request, "mobile");
		Long uin = getUinByMobile(mobile);
		List<Object> params = new ArrayList<Object>();
		params.add(uin);
		params.add(1);
		params.add(-1);
		Long time = System.currentTimeMillis()/1000-30*24*60*60;
		params.add(time);
		List<Map<String, Object>> list = onlyService.getAllMap("select uid as id,max(end_time)paytime from order_tb where uin = ?  and state = ? and  uid<>? and end_time >? group by uid order by paytime desc",
				params);
		if(list!=null&&list.size()>0){
			for(Map map:list){
				long uid = Long.parseLong(map.get("id")+"");
				params.clear();
				params.add(uid);
				params.add(0);
				Map<String, Object> usermap = onlyService.getMap("select u.nickname as name ,u.online_flag  online,c.company_name parkname from user_info_tb u,com_info_tb c where u.id=? and u.state=? and u.comid=c.id ",params);
				map.putAll(usermap);
			}
		}
		String ret = StringUtils.createJson(list);
		logger.error(">>>uin:"+uin+"快捷支付的结果:"+ret);
		return ret ;
	}
	/**
	 * 返回微信公众号文章地址
	 * @param request
	 * @return
	 */
	private String getWxpArtic(HttpServletRequest request) {
		//uoinrule,useticket,credit,backbalance
		//http://localhost/zld/carinter.do?action=getwxpcartic&mobile=13641309140&artictype=backbalance
		String atype = RequestUtil.getString(request, "artictype");
		if(atype.equals("uoinrule"))//停车券合并规则说明
			return "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209338628&idx=1&sn=d40db1b84727c85eb6e557113ec44cb1#rd";
		else if(atype.equals("useticket"))//停车宝停车券使用说明
			return "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&amp;mid=208427587&amp;idx=1&amp;sn=6cec3794e585e4d31b5079f919b01614#rd";
		else if(atype.equals("credit"))//停车宝信用额度说明
			return "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&amp;mid=208427120&amp;idx=1&amp;sn=6cb6719bf1520ef5a72097fe5c7fe56a#rd";
		else if(atype.equals("backbalance"))//关于车主退还余额的说明
			return "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&amp;mid=209376960&amp;idx=1&amp;sn=369c4bea18d70d656c4f3e30b86cc843#rd";
		return "";
	}
	/**
	 * 查看合体详情
	 * @param request
	 * @return
	 */
	private String viewTikcetUion(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "id", -1L);
		String mobile = RequestUtil.getString(request, "mobile");
		Long uin = getUinByMobile(mobile);
		logger.error(mobile+",id:"+id);
		Map uionMap = service.getMap("select * from ticket_uion_tb where id =? ", new Object[]{id});
		Map<String, Object> retMap = ZldMap.getMap(new String[]{"result","errmsg"} , new Object[]{-1,""});
		if(uionMap!=null&&!uionMap.isEmpty()){
			Long winUin = (Long)uionMap.get("win_uin");
			Long reqUin = (Long)uionMap.get("req_uin");
			Long resUin = (Long)uionMap.get("res_uin");
			
			//请求合体的车主停车券
			Map reqMap = service.getMap("select t.resources,t.money,t.state,t.limit_day,wx_imgurl from ticket_tb t left join user_info_tb u on u.id =t.uin where t.id=?", new Object[]{uionMap.get("req_tid")});
			//响应合体的车主停车券
			Map resMap = service.getMap("select resources,money,wx_imgurl from ticket_tb t left join user_info_tb u on u.id =t.uin where t.id=?", new Object[]{uionMap.get("res_tid")});
			boolean isOwnWin=false;//是否是自己赢了
			boolean isReqOwn=false;//请求车主是否是自己
			if(winUin!=null){
				Integer reqMoney = (Integer)uionMap.get("req_money");
				Integer resMoney = (Integer)uionMap.get("res_money");
				if(reqUin.equals(uin))
					isReqOwn=true;
				if(winUin.equals(uin))//请求合体的车主是自己
					isOwnWin=true;
				String friendCar ="";
				if(isReqOwn){//请求合体的车主是否是自己
					friendCar = publicMethods.getCarNumber(resUin);
					if(friendCar.length()==7){
						friendCar = friendCar.substring(0,4)+"***"+friendCar.substring(6);
					}
					if(winUin==-1){
						retMap = ZldMap.getMap(new String[]{"ownticket"},new Object[]{"{\"name\":\"我的停车券\",\"money\":\""+reqMoney+"\",\"isbuy\":\""+reqMap.get("resources")+"\"}"});
						retMap = ZldMap.getAppendMap(retMap,new String[]{"friendticket"}, new Object[]{"{\"name\":\"来自车主"+friendCar+"的停车券\",\"money\":\""+resMoney+"\",\"isbuy\":\""+resMap.get("resources")+"\"}"});
					}else {
						if(isOwnWin){
							retMap = ZldMap.getMap(new String[]{"ownticket"}, new Object[]{"{\"name\":\"我的停车券\",\"money\":\""+(reqMoney)+"\",\"isbuy\":\""+reqMap.get("resources")+"\"}"});
							retMap = ZldMap.getAppendMap(retMap,new String[]{"friendticket"}, new Object[]{"{\"name\":\"来自车主"+friendCar+"的停车券\",\"money\":\""+resMoney+"\",\"isbuy\":\""+resMap.get("resources")+"\"}"});
						}else {
							retMap = ZldMap.getMap(new String[]{"ownticket"}, new Object[]{"{\"name\":\"我的停车券\",\"money\":\""+reqMoney+"\",\"isbuy\":\""+reqMap.get("resources")+"\"}"});
							retMap = ZldMap.getAppendMap(retMap,new String[]{"friendticket"}, new Object[]{"{\"name\":\"来自车主"+friendCar+"的停车券\",\"money\":\""+(resMoney)+"\",\"isbuy\":\""+resMap.get("resources")+"\"}"});
						}
					}
				}else {
					friendCar = publicMethods.getCarNumber(reqUin);
					if(friendCar.length()==7){
						friendCar = friendCar.substring(0,4)+"***"+friendCar.substring(6);
					}
					if(winUin==-1){
						retMap = ZldMap.getMap(new String[]{"ownticket"},new Object[]{"{\"name\":\"我的停车券\",\"money\":\""+resMoney+"\",\"isbuy\":\""+resMap.get("resources")+"\"}"});
						retMap = ZldMap.getAppendMap(retMap,new String[]{"friendticket"}, new Object[]{"{\"name\":\"来自车主"+friendCar+"的停车券\",\"money\":\""+reqMoney+"\",\"isbuy\":\""+reqMap.get("resources")+"\"}"});
					}else {
						if(isOwnWin){
							retMap = ZldMap.getMap(new String[]{"ownticket"},new Object[]{"{\"name\":\"我的停车券\",\"money\":\""+(resMoney)+"\",\"isbuy\":\""+resMap.get("resources")+"\"}"});
							retMap = ZldMap.getAppendMap(retMap,new String[]{"friendticket"}, new Object[]{"{\"name\":\"来自车主"+friendCar+"的停车券\",\"money\":\""+reqMoney+"\",\"isbuy\":\""+reqMap.get("resources")+"\"}"});
						}else {
							retMap = ZldMap.getMap(new String[]{"ownticket"},new Object[]{"{\"name\":\"我的停车券\",\"money\":\""+resMoney+"\",\"isbuy\":\""+resMap.get("resources")+"\"}"});
							retMap = ZldMap.getAppendMap(retMap,new String[]{"friendticket"}, new Object[]{"{\"name\":\"来自车主"+friendCar+"的停车券\",\"money\":\""+(reqMoney)+"\",\"isbuy\":\""+reqMap.get("resources")+"\"}"});
							
						}
					}
				}
				if(winUin==-1){//合体失败
					retMap= ZldMap.getAppendMap(retMap,new String[]{"result","errmsg"},new Object[]{0,"合体失败"});
					retMap= ZldMap.getAppendMap(retMap,new String[]{"ownret","friendret"},new Object[]{
							"{\"imgurl\":\""+(isReqOwn?reqMap.get("wx_imgurl"):resMap.get("wx_imgurl"))+"\",\"toptip\":\"失去"+(isReqOwn?reqMap.get("money"):resMap.get("money"))+"元停车券\",\"righttip\":\"失败\"}",
							"{\"imgurl\":\""+(isReqOwn?resMap.get("wx_imgurl"):reqMap.get("wx_imgurl"))+"\",\"toptip\":\"失去"+(isReqOwn?resMap.get("money"):reqMap.get("money"))+"元停车券\",\"righttip\":\"失败\"}"});
				}else{//合体成功
					retMap= ZldMap.getAppendMap(retMap,new String[]{"result","errmsg"},new Object[]{1,"合体成功"});
					String ownTip1 = "";
					String ownTip2 = "";
					String ownRightTip = "";
					String friendTip1 = "";
					String friendTip2 = "";
					String friendRightTip = "";
					reqMoney = (Integer)reqMap.get("money");
					resMoney = (Integer)resMap.get("money");
					if(isOwnWin){
						ownTip1="获得"+(isReqOwn?reqMoney:resMoney)+"元停车券";
						ownTip2="获得两张第二关游戏资格卡";
						ownRightTip = "获胜";
						friendTip1="失去"+(isReqOwn?resMoney:reqMoney)+"元停车券";
						friendTip2="获得一张第二关游戏资格卡";
						friendRightTip = "失败";
					}else{
						friendTip1="获得"+(isReqOwn?resMoney:reqMoney)+"元停车券";
						friendTip2="获得两张第二关游戏资格卡";
						friendRightTip = "获胜";
						ownTip1="失去"+(isReqOwn?reqMoney:resMoney)+"元停车券";
						ownTip2="获得一张第二关游戏资格卡";
						ownRightTip = "失败";
					}
					retMap= ZldMap.getAppendMap(retMap,new String[]{"ownret","friendret"},new Object[]{
							"{\"win\":\""+(isOwnWin?1:0)+"\",\"imgurl\":\""+(isReqOwn?reqMap.get("wx_imgurl"):resMap.get("wx_imgurl"))+"\",\"toptip\":\""+ownTip1+"\",\"buttip\":\""+ownTip2+"\",\"righttip\":\""+ownRightTip+"\"}",
							"{\"win\":\""+(isOwnWin?0:1)+"\",\"imgurl\":\""+(isReqOwn?resMap.get("wx_imgurl"):reqMap.get("wx_imgurl"))+"\",\"toptip\":\""+friendTip1+"\",\"buttip\":\""+friendTip2+"\",\"righttip\":\""+friendRightTip+"\"}"});
				}
			}else {
				Integer state = (Integer)reqMap.get("state");
				if(state!=null&&state==1){
					retMap.put("errmsg", "停车券已使用");
				}else {
					Long limitDay = (Long)reqMap.get("limit_day");
					if(limitDay<(System.currentTimeMillis()/1000))
						retMap.put("errmsg", "停车券已过期");
				}
				Integer reqMoney = (Integer)reqMap.get("money");
				retMap = ZldMap.getAppendMap(retMap, new String[]{"ownticket"}, new Object[]{"{\"money\":\""+reqMoney+"\",\"isbuy\":\""+reqMap.get("resources")+"\",\"name\":\"我的停车券\"}"});
			}
		}
		//http://192.168.199.240/zld/carinter.do?action=viewticketuion&mobile=15801482643&id=125
		//http://192.168.199.240/zld/carinter.do?action=viewticketuion&mobile=18811157723&id=8
		logger.error(retMap);
		return StringUtils.createJson(retMap);
	}

	/**
	 * 查可合体的停车券
	 * @param request
	 * @return
	 */
	private String getUionTickets(HttpServletRequest request) {
		 //http://192.168.199.240/zld/carinter.do?action=getuiontickets&mobile=15801482643&page=1
		Integer page = RequestUtil.getInteger(request, "page", 1);
		String mobile = RequestUtil.getString(request, "mobile");
		List<Object> params = new ArrayList<Object>();
		Long uin = getUinByMobile(mobile);
		String sql = "select id,money,resources,type,state,pmoney,limit_day as limitday from ticket_tb  where uin = ?" +
				" and type=? and limit_day>? and state=? and money<? and id not in" +
				"(select req_tid from ticket_uion_tb where req_uin=? and state=? ) order by money ,limit_day";//order by limit_day";
		Long ntime =TimeTools.getToDayBeginTime();
		params.add(uin);
		params.add(0);
		params.add(ntime);
		params.add(0);
		params.add(12);
		params.add(uin);
		params.add(0);
		List<Map<String, Object>> ticketMap = onlyService.getAll(sql, params, page, 50);
		//处理没有返回到账户中的停车券
		//logger.error(">>>>");
		String result = "[]";
		if(ticketMap!=null&&!ticketMap.isEmpty()){
			for(Map<String, Object> tMap : ticketMap){
				Long limitDay = (Long)tMap.get("limitday");
				if(ntime >limitDay)
					tMap.put("exp", 0);
				else {
					tMap.put("exp", 1);
				}
				Integer res = (Integer)tMap.get("resources");
				tMap.put("isbuy",res);
				tMap.put("iscanuse",1);
				tMap.remove("resources");
				Integer money = (Integer)tMap.get("money");
				Integer topMoney = CustomDefind.getUseMoney(money.doubleValue(), 1);
				tMap.put("desc", "满"+topMoney+"元可以抵扣全额");
				if(res==1){
					tMap.put("desc", "满"+(money+1)+"元可以抵扣全额,<br/>过期后退还 <font color='#32a669'>"+StringUtils.formatDouble(tMap.get("pmoney"))+"</font> 元至您的账户");
				}
			}
			result= StringUtils.createJson(ticketMap).replace("null", "");
		}
		return result;
	}
	/**
	 * 响应合体，计算合体结果
	 * @param request
	 * @return
	 */
	private String resTikcetUion(HttpServletRequest request) {
		String mobile = RequestUtil.getString(request, "mobile");
		Long tid = RequestUtil.getLong(request, "tid", -1L);
		Long id = RequestUtil.getLong(request, "id", -1L);
		logger.error(mobile+",id:"+id+",tid:"+tid);
		Long uin = getUinByMobile(mobile);
		/**
		 * 合体规则：
			只有一个人出了单数，另一个人出了双数金额的券，才能合体成功。
			合体失败后，两人的停车券都会失去。
			合体成功后，金额大的人，得到两张停车券之和，最高12元，并得到两张第二关游戏资格卡。
			金额小的人，停车券失去，但得到一张第二关游戏资格卡。
			游戏资格卡不会过期。
			固定的两个人，每天只能合体一次，要注意身体健康。
			购买的停车券和非购买的停车券合体后，购买的停车券
			合体成功后，停车券最后的有效期按更短的算。
			合体根据合体值来判断。

			1，金额。           几元停车券得到金额*2的合体值。
			2，有效期           每增加1天有效期，增加1分合体值。
			3，是否购买        上面的合体值直接乘以5.
				合体值就是赢得合体的概率。
				比如，你的1元停车券，还有2天到期，非购买，则你 得停车券合体值为4
				你的2元券，还有3天到期，购买，你的停车券合体值为35.
				则你们的输赢概率比为35:4
				合体成功后，停车券金额为两个停车券金额之和，上限12，有效期按赢的算，谁赢了，有效期变成谁的，合体的退款金额不变。
		 */
		String mesg = "合体请求失败";
		int ret = 0;//合体结果 -4至0 失败，1成功,请求合体车主赢，2成功，响应车主赢
		Long reqNumber=0L;
		Long ntime = System.currentTimeMillis()/1000;
		Integer reqmoney =0;
		Map reqTicketMap = null;
		Map uionMap = service.getMap("select * from ticket_uion_tb where id =? and state=? ", new Object[]{id,0});
		Long pretid =-1L;
		Long preUin = -1L;
		Long reqtime = null;
		if(uionMap!=null&&!uionMap.isEmpty()){
			pretid =(Long)uionMap.get("req_tid");
			preUin = (Long)uionMap.get("req_uin");
			reqtime =(Long)uionMap.get("req_time");
			reqTicketMap= service.getMap("select id,money,resources,limit_day,pmoney from ticket_tb where id = ? and state=? ", new Object[]{pretid,0});
			if(reqTicketMap!=null&&!reqTicketMap.isEmpty()){
				reqmoney=(Integer)reqTicketMap.get("money");
				Integer res =  (Integer)reqTicketMap.get("resources");
				Long limitDay = (Long)reqTicketMap.get("limit_day");
				if(limitDay<ntime){
					ret=-4;
					mesg = "合体请求失败，对方停车券已过期";
				}else
					reqNumber = (reqmoney*2+(limitDay-ntime)/(24*60*60))*(res+1);
			}else {
				ret=-2;
				mesg = "合体请求失败,停车券已使用";
			}
		}else {
			ret=-3;
			mesg = "合体请求失败，合体已取消";
		}
		
		Long resNumber=0L;
		Integer resmoney =0;
		Long winUin=-1L;
		
		Map resTicketMap = null;
		Long winTicketId=-1L;
		Long loseTicketId=-1L;//失败车主的停车券编号
		Long limitAddtime = 0L;//赢券的有效期累加值,当前时间减去请求或响应合并时的时间 
		boolean isBuy = false;
		if(reqNumber>0){
			resTicketMap = service.getMap("select id,money,resources,limit_day,pmoney from ticket_tb where id = ? and state=? ", new Object[]{tid,0});
			if(resTicketMap!=null&&!resTicketMap.isEmpty()){
				resmoney = (Integer)resTicketMap.get("money");
				if((resmoney+reqmoney)%2==0){
					int r = service.update("update ticket_tb set state=? where id in(?,?)", new Object[]{1,pretid,tid});
					if(reqmoney%2==0)
						mesg = "合体失败,双方同为偶数";
					else
						mesg = "合体失败,双方同为奇数";
					ret=-1;
					logger.error(mobile+">>>>>合体失败,更新两人停车券："+r);
				}else {
					Integer res =  (Integer)resTicketMap.get("resources");
					Long limitDay = (Long)resTicketMap.get("limit_day");
					resNumber = (resmoney*2+(limitDay-ntime)/(24*60*60))*(res+1);
					Integer rand = new Random().nextInt(reqNumber.intValue()+resNumber.intValue());
					logger.error(preUin+",请求合体的合体值 ："+reqNumber+"，"+uin+",响应合体的合体值 ："+resNumber+",rand:"+rand);
					if(rand+1<=reqNumber){
						winUin=preUin;
						ret=1;
						winTicketId=pretid;
						loseTicketId=tid;
						res =  (Integer)reqTicketMap.get("resources");
						mesg = "合体成功，请求合体车主赢";
						limitAddtime  = ntime-reqtime;
						addSecondGameCard(preUin,2);
						addSecondGameCard(uin,1);
					}else {
						winUin=uin;
						ret=2;
						winTicketId=tid;
						loseTicketId=pretid;
						mesg = "合体成功，响应合体车主赢";
						addSecondGameCard(preUin,1);
						addSecondGameCard(uin,2);
					}
					if(res==1)
						isBuy =true;
					logger.error("合体：win:"+winUin);
					
				}
			}
		}
		
		if(ret>0){
			Integer total =reqmoney+resmoney;
			if(total>12)
				total=12;
			Double utotal = StringUtils.formatDouble(reqTicketMap.get("pmoney"))+StringUtils.formatDouble(resTicketMap.get("pmoney"));
			if(utotal>12)
				utotal=12.0;
			if(!isBuy)
				utotal=0.0;
			int r = service.update("update ticket_tb set money =?,pmoney=?,limit_day=limit_day + ? where id=? ",
					new Object[]{total,utotal,limitAddtime,winTicketId});
			logger.error("reqticket:"+reqTicketMap);
			logger.error("resticket:"+resTicketMap);
			logger.error("nowtime:"+ntime+",limitAddtime:"+limitAddtime);
			logger.error("win:"+winTicketId+",合并后券金额："+total+",退款金额 ："+utotal);
			logger.error(mobile+",合体成功，赢方："+ret+"(1:请求合体车主赢,2:响应车主赢),更新停车券："+r);
			
			r=service.update("update ticket_uion_tb set res_tid=?, res_uin=?,res_time=?, win_uin=?,state=?,res_money=? where id=? ",
					new Object[]{tid,uin,System.currentTimeMillis()/1000,winUin,1,resmoney,id});
			r = service.update("update ticket_tb set state=? where id =? ", new Object[]{1,loseTicketId});
		}else {
			int r=service.update("update ticket_uion_tb set res_tid=?, res_uin=?,res_time=?,win_uin=?,state=?,res_money=? where id=?",
					new Object[]{tid,uin,System.currentTimeMillis()/1000,winUin,1,resmoney,id});
		}
		if(ret==1){
			Map userMap=onlyService.getMap("select mobile from user_info_tb where id =? ", new Object[]{preUin});
			mobile= userMap==null?"":userMap.get("mobile")+"";
		}else if(ret!=2){
			mobile="";
		}
		Map<String, Object> resultMap = ZldMap.getMap(new String[]{"result","errmsg","id","winner"}, new Object[]{ret,mesg,id,mobile});
		return StringUtils.createJson(resultMap);
		//http://192.168.199.240/zld/carinter.do?action=resticketuion&mobile=15801482643&tid=44878&id=8
	}
	/**
	 * 加第二关入场券
	 * @param uin
	 * @param number
	 */
	private void addSecondGameCard(Long uin,Integer number){
		int ret =0;
		for(int i=0;i<number;i++){
			ret +=service.update("insert into flygame_score_tb (uin,fgid,remark,ptype,money,ctime) values(?,?,?,?,?,?)",
						new Object[]{uin,-1,"第二关入口",6,0,System.currentTimeMillis()/1000});
		}
		logger.error(uin+"，加了"+number+"张第二关入场券.ret:"+ret);
	}
	
	/**
	 * 查询是否可以合体，相同两个车主，一天只能合体一次
	 * @param request
	 * @return
	 */
	private String preTicketUion(HttpServletRequest request) {
		//http://192.168.199.240/zld/carinter.do?action=preticketuion&uin=hx21766&touin=hx21691
		String _touin = RequestUtil.getString(request, "touin");
		String _uin = RequestUtil.getString(request, "uin");
		String result = "{\"result\":\"0\",\"errmsg\":\"今天你们已经合体过，不能再合体了，要注意身体哟！\"}";
		Long touin =-1L;
		if(_touin.indexOf("hx")!=-1)
			touin= Long.valueOf(_touin.substring(2));
		Long uin =-1L;
		if(_uin.indexOf("hx")!=-1)
			uin= Long.valueOf(_uin.substring(2));
		if(uin>0&&touin>0){
			Long ttime = TimeTools.getToDayBeginTime();
			Long count  = service.getLong("select count(id) from ticket_uion_tb where ((req_uin =? and res_uin=?) or (res_uin =? and req_uin=?) )  and req_time > ?", 
					new Object[]{uin,touin,uin,touin,ttime});
			logger.error(uin+".....resuin:"+uin+",requin:"+touin+",今日合体次数："+count);
			if(count<1)
				result = "{\"result\":\"1\",\"errmsg\":\"可以合体\"}";
		}
		logger.error("preticketuion,uin:"+uin+",touin:"+touin+", result:"+result);
		return result;
	}
	
	/**
	 * 车主请求合体
	 * @param request
	 * @return
	 */
	private String reqTikcetUion(HttpServletRequest request) {
		
		String mobile = RequestUtil.getString(request, "mobile");
		//响应合体人
		String _touin = RequestUtil.getString(request, "touin");
		Long touin =-1L;
		if(_touin.indexOf("hx")!=-1)
			touin= Long.valueOf(_touin.substring(2));
		Long uin = getUinByMobile(mobile);
		Long tid = RequestUtil.getLong(request, "tid", -1L);
		int ret = 0;
		Long key =-1L;
		logger.error("ticket uion:resuin:"+uin+",requin:"+touin);
		logger.error(mobile+",tid:"+tid);
		String mesg = "合体请求失败";
		if(uin>0&&tid>0&&touin>0){
			Map ticketMap = onlyService.getMap("select id,money from ticket_tb where id =? ", new Object[]{tid});
			if(ticketMap!=null){
				Long ttime = TimeTools.getToDayBeginTime();
				Long count  = service.getLong("select count(id) from ticket_uion_tb where ((req_uin =? and res_uin=?) or (res_uin =? and req_uin=?) ) and req_time > ?", 
						new Object[]{uin,touin,uin,touin,ttime});
				logger.error(uin+".....resuin:"+uin+",requin:"+touin+",今日合体次数："+count);
				if(count<1){
					key = service.getkey("seq_ticket_uion_tb");
					ret = service.update("insert into ticket_uion_tb(id,req_uin,req_tid,req_time,req_money) values(?,?,?,?,?)", 
							new Object[]{key,uin,tid,System.currentTimeMillis()/1000,ticketMap.get("money")});
				}else {
					ret =-1;
					mesg = "今天你们已经合体过，不能再合体了，要注意身体哟！";
				}
			}
		}
		if(ret==1)
			mesg = "合体请求成功";
		Map userMap = onlyService.getMap("select wx_name,wx_imgurl,car_number from " +
				"user_info_tb u left join car_info_tb c on c.uin=u.id where u.id=?", new Object[]{uin});
		Map<String, Object> resultMap = ZldMap.getMap(new String[]{"result","errmsg","wxname","wximgurl","carnumber","id"}, 
				new Object[]{ret,mesg,userMap.get("wx_name"),userMap.get("wx_imgurl"),userMap.get("car_number"),key});
		//http://192.168.199.240/zld/carinter.do?action=reqticketuion&mobile=15210932334&tid=44807&touin=
		String result = StringUtils.createJson(resultMap);
		logger.error("reqticketuion,uin:"+uin+",touin:"+touin+", result:"+result);
		return result;
	}

	/**
	 * 车主扫描车位二维码生成订单
	 * @param request
	 * @return
	 */
	private String addOrder(HttpServletRequest request) {
		//根据二维码查车位信息
		String mobile =RequestUtil.getString(request, "mobile");
		Long uin = getUinByMobile(mobile);
		String carNumber = publicMethods.getCarNumber(uin);
		Long cid = RequestUtil.getLong(request, "cid", -1L);
		String result = "{\"result\":\"0\",\"errmsg\":\"生成订单失败\"}";
		Map parkMap = onlyService.getMap("select * from com_park_tb where qid=?", new Object[]{cid});
		if(parkMap!=null&&!parkMap.isEmpty()){//查到了车位
			Long count = service.getLong("select count(id) from com_park_tb where state=? " +
					" and order_id>? and id=? ", new Object[]{1, 0, parkMap.get("id")});
			logger.error("count:"+count);
			if(count == 0){
				Long key = service.getkey("seq_order_tb");
				Map uidMap = service.getMap("select id from user_info_tb where comid=? ", new Object[]{parkMap.get("comid")});
				int ret = service.update("insert into order_tb (id,create_time,comid,uin,c_type,car_number,state,uid) values(?,?,?,?,?,?,?,?)", 
						new Object[]{key,System.currentTimeMillis()/1000,parkMap.get("comid"),uin,6,carNumber,0,uidMap.get("id")});
				if(ret==1){
					ret = service.update("update com_park_tb set state=? ,order_id=? where id=?", new Object[]{1,key,parkMap.get("id")});
					logger.error("(update com_park_tb orderid):"+key+",id:"+parkMap.get("id"));
					if(ret==1){
						result = "{\"result\":\"1\",\"errmsg\":\"进场成功\"}";
					}
				}
			}
		}
		logger.error(mobile+",扫二维码生成订单cid:"+cid+",result :"+result);
		return result;
	}
	
	/**
	 * 车主请求购买停车券
	 * @param request
	 * @return
	 */
	private String buyTicket(HttpServletRequest request) {
		String mobile  = RequestUtil.getString(request, "mobile");
		Map userMap = onlyService.getPojo("select is_auth from user_info_Tb where mobile=? and auth_flag=? ", new Object[]{mobile,4});
		Integer isAuth = 0;
		if(userMap!=null){
			isAuth=(Integer) userMap.get("is_auth");
		}
		Double authdiscount = StringUtils.formatDouble(CustomDefind.getValue("AUTHDISCOUNT"))*10;
		Double noauthdiscount = StringUtils.formatDouble(CustomDefind.getValue("NOAUTHDISCOUNT"))*10;
		return "{\"isauth\":\""+isAuth+"\",\"auth\":\""+authdiscount+"\",\"notauth\":\""+noauthdiscount+"\"}";
	}
	/**
	 * 查询所有车牌
	 * @param request
	 * @return
	 */
	private String getCarNumbers(HttpServletRequest request) {
		String mobile  = RequestUtil.getString(request, "mobile");
		Long uin = getUinByMobile(mobile);
		List<Map<String, Object>> carList = onlyService.getAll("select car_number,is_auth " +
				"from car_info_tb where uin=? and state =? ", new Object[]{uin,1});
		Integer state=0;
		if(carList!=null&&!carList.isEmpty()){
			List<Integer> sList = new ArrayList<Integer>();
			for(Map<String, Object> car : carList){
				sList.add((Integer)car.get("is_auth"));
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
			for(Map<String, Object> car : carList){
				Integer isAuth = (Integer)car.get("is_auth");
				if(isAuth.equals(state))
					car.put("is_default", "1");
				else {
					car.put("is_default", "0");
				}
			}
			return StringUtils.createJson(carList);
		}
		return "[]";
	}

	/**
	 * 上传车主行驶证
	 * @param request
	 * @return
	 * @throws Exception
	 */
	private String uploadCarPics2Mongodb (HttpServletRequest request) throws Exception{
		//logger.error("begin upload user picture....");
		String mobile =RequestUtil.getString(request, "mobile");
		String carid =AjaxUtil.decodeUTF8(RequestUtil.getString(request, "carnumber"));
		String oldcarid =AjaxUtil.decodeUTF8(RequestUtil.getString(request, "old_carnumber"));
		logger.error("begin upload user picture....mobile:"+mobile+",carnumber:"+carid);
		Map<String, String> extMap = new HashMap<String, String>();
	    extMap.put(".jpg", "image/jpeg");
	    extMap.put(".jpeg", "image/jpeg");
	    extMap.put(".png", "image/png");
	    extMap.put(".gif", "image/gif");
	    extMap.put(".webp", "image/webp");
		if(mobile.equals("")){
			return  "{\"result\":\"-3\",\"errmsg\":\"手机号为空！\"}";
		}
		if(carid.equals("")){
			return  "{\"result\":\"-4\",\"errmsg\":\"车牌号为空！\"}";
		}
		
		logger.error("mobile:"+mobile+",carnumber:"+carid+",oldcarnumber"+oldcarid);
		Map userMap = getUserByMobile(mobile);
		Long uin = getUinByMobile(mobile);
		if(uin==null||uin==-1){
			return  "{\"result\":\"-5\",\"errmsg\":\"手机号未注册！\"}";
		}
		carid = carid.toUpperCase();
		oldcarid = oldcarid.toUpperCase();
		List<Map<String, Object>> carList  = service.getAll("select car_number from car_info_Tb where uin=? ", new Object[]{uin}); 
		boolean isHasCarId = false;
		boolean isUpdate = false;
		int cnum=0;
		if(carList!=null&&!carList.isEmpty()){
			cnum=carList.size();
			for(Map<String, Object> cMap: carList){
				String carNumber = (String)cMap.get("car_number");
				if(carid.equals(carNumber)){//车牌已存在
					isHasCarId = true;
				}
				if(carNumber.equals(oldcarid))
					isUpdate=true;
			}
		}
		if(!isUpdate){
			if(!oldcarid.equals(""))
				return "{\"result\":\"-5\",\"errmsg\":\"原车牌不存在，不能更新！\"}";
			else if(isHasCarId&&oldcarid.equals(""))
				return "{\"result\":\"-6\",\"errmsg\":\"车牌不能重复注册！\"}";
		}
		if(!isUpdate&&!oldcarid.equals(""))
			return "{\"result\":\"-5\",\"errmsg\":\"原车牌不存在，不能更新！\"}";
		
		
		if(isUpdate&&!oldcarid.equals("")&&!carid.equals(oldcarid)){//需要更新原车牌
			Long ccount = service.getLong("select count(id) from car_info_Tb where car_number=? ", new Object[]{carid});
			if(ccount>0){
				return "{\"result\":\"-6\",\"errmsg\":\"车牌已注册过，请重新输入！\"}";
			}
			logger.error("carowner:"+mobile+",update car_number,old:"+oldcarid+",new:"+carid+",ret:"+
					service.update("update car_info_tb set car_number=? where uin=? and car_number=? ", new Object[]{carid,uin,oldcarid}));
		}else {
			if(!isHasCarId){
				if(cnum<3){
					Long ccount = service.getLong("select count(id) from car_info_Tb where car_number=? ", new Object[]{carid});
					if(ccount>0){
						return "{\"result\":\"-6\",\"errmsg\":\"车牌已注册过，请重新输入！\"}";
					}
					int ret = service.update("insert into car_info_Tb (uin,car_number,state,create_time) values(?,?,?,?) ",
							new Object[]{uin,carid,1,System.currentTimeMillis()/1000});
					if(ret==1){
						isHasCarId = true;
						Long cityId=(Long)userMap.get("cityid");
						String uuid = (String)userMap.get("uuid");
						if(uuid==null&&cityId!=null&&cityId==321000){
							publicMethods.sendMessageToThird(uin, null, mobile, carid, userMap.get("strid")+"", 0);
						}
					}
					logger.error("carowner:"+mobile+",add car_number:"+carid+",ret:"+ret+",curr carnumber count:"+(cnum+1));
				}else {
					return "{\"result\":\"-2\",\"errmsg\":\"车牌已超过三个！\"}";
				}
			}
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
		int index =0;
		for (FileItem item : items){
			// 处理普通的表单域
			if (!item.isFormField()){
				// 从客户端发送过来的上传文件路径中截取文件名
				filename = item.getName().substring(item.getName().lastIndexOf("\\")+1);
				is = item.getInputStream(); // 得到上传文件的InputStream对象
				logger.error("index:"+index+",filename:"+item.getName()+",stream:"+is);
				System.err.println("index:"+index+",1========="+is.available());
				System.err.println("index:"+index+",2========="+item.getContentType());
			}else
				continue;
			String file_ext =filename.substring(filename.lastIndexOf(".")).toLowerCase();// 扩展名
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
				//  DBCollection collection = mydb.getCollection("records_test");
				
				BasicDBObject document = new BasicDBObject();
				document.put("uin", uin);
				document.put("carid", carid);
				document.put("ctime",  System.currentTimeMillis()/1000);
				document.put("type", extMap.get(file_ext));
				document.put("content", content);
				document.put("filename", picurl);
				//开始事务
				System.out.println("index:"+index+",开始写入dB");
				//结束事务
				mydb.requestStart();
				System.out.println(collection.insert(document));
				//结束事务
				mydb.requestDone();
				System.out.println("index:"+index+",写入了dB");
				in.close();        
				is.close();
				byteout.close();
				
				String sql = "update car_info_tb set pic_url1=?,is_auth=?,create_time=?  where uin=? and car_number=?";
				if(index==1){
					sql = "update car_info_tb set pic_url2=?,is_auth=?,create_time=?  where uin=? and car_number=?";
				}
				int ret = service.update(sql, new Object[]{picurl,2,System.currentTimeMillis()/1000,uin,carid});
				logger.error("第"+index+"张图片：ret:"+ret);
				index++;
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
	
	/**
	 * 车场评论
	 * @param request
	 * @return
	 */
	private String pcommDetail(HttpServletRequest request) {
		Long uid = RequestUtil.getLong(request, "uid", -1L);
		Integer page = RequestUtil.getInteger(request, "page", 1);
		Integer size = RequestUtil.getInteger(request, "size", 20);
		String ret = "[]";
		if(uid!=-1){
			List<Map<String, Object>> cList = onlyService.getPage("select comments as info,ctime,uin from parkuser_comment_tb  " +
					" where uid=? order by ctime desc ", new Object[]{uid},page,size);
			if(cList!=null&&!cList.isEmpty()){
				for(Map<String, Object> map : cList){
					Long uin = (Long)map.get("uin");
					if(uin!=null){
						String carNumber = publicMethods.getCarNumber(uin);
						if(!carNumber.equals("车牌号未知"))
							map.put("user", carNumber);
						else {
							map.put("user", "");
						}
					}
				}
				ret = StringUtils.createJson(cList);
			}
		}
		return ret;
	}
	/***收费员详情,返回：服务次数，最近一周服务次数，收到打赏数量及金额，收到的评价数**/
	private String puserDetail(HttpServletRequest request) {
		Long uid = RequestUtil.getLong(request, "uid", -1L);
		String ret = "{}";
		if(uid!=-1){
			//所有服务次数
			Long scount= onlyService.getLong("select count(ID) from order_tb where uid=? and state=? ", new Object[]{uid,1});
			//一周服务次数
			Long wcount= onlyService.getLong("select count(ID) from order_tb where uid=? and state=? and end_time >? ",
					new Object[]{uid,1,TimeTools.getToDayBeginTime()-7*24*60*60});
			Map<String,Object> rMap = onlyService.getMap("select count(ID) rcount,sum(money) money,uid from parkuser_reward_tb" +
					" where uid =?  group by uid ", new Object[]{uid});
			//评论数
			Long ccount = onlyService.getLong("select count(ID) from parkuser_comment_tb where uid=? ", new Object[]{uid});
			Map userMap = onlyService.getMap("select mobile from user_info_tb where id =?", new Object[]{uid});
			if(rMap==null){
				rMap = new HashMap<String,Object>();
				rMap.put("pcount", 0);
				rMap.put("money", 0);
			}else {
				rMap.remove("uid");
			}
			rMap.put("scount", scount);
			rMap.put("wcount", wcount);
			rMap.put("ccount", ccount);
			if(userMap!=null)
				rMap.put("mobile", userMap.get("mobile"));
			else {
				rMap.put("mobile", "");
			}
			ret = StringUtils.createJson(rMap);
		}
		ret = ret.replace("null", "");
		return ret;
	}
	//红包修改手机号码
	private String editMobile(HttpServletRequest request) {
		String omobile = RequestUtil.getString(request, "omobile");
		String nmobile =RequestUtil.getString(request, "nmobile");
		Map nuserMap = service.getMap("select id,wxp_openid from user_info_tb where mobile=? and auth_flag=? ", new Object[]{nmobile,4});
		int ret = 0;
		Long uin = -1L;
		if(nuserMap!=null){
			String openid = (String)nuserMap.get("wxp_openid");
			uin = (Long)nuserMap.get("id");
			if(openid!=null&&openid.length()>2)
				return "-1";
		}else {//不存在账户，生成账户
			uin = publicMethods.regUser(nmobile, 1000L,-1L,false);//注册并发元停车券
		}
		Map userMap = service.getMap("select id,wxp_openid,wx_name,wx_imgurl from user_info_tb where mobile=? and auth_flag=? ", new Object[]{omobile,4});
		if(uin!=null&&uin!=-1&&userMap!=null&&userMap.get("wxp_openid")!=null){
			ret = service.update("update user_info_tb set wxp_openid=?,wx_name=?,wx_imgurl=? where id=?  ",  
					new Object[]{userMap.get("wxp_openid"),userMap.get("wx_name"),userMap.get("wx_imgurl"),uin});
			if(ret==1){
				ZldMap.removeUser(uin);
				ret = service.update("update user_info_tb set wxp_openid=?,wx_name=?,wx_imgurl=? where id=? ", new Object[]{null,null,null,userMap.get("id")});
				logger.error(">>>>>抢红包修改手机号， 原手机 ："+omobile+"，新手机 ："+nmobile);
			}
		}
		return  ret+"";
	}

	private int doPreGetWeixinBonus(HttpServletRequest request,Long id) {
		//是否存在红包，是否已领完
		String words = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "words"));
		Map bMap = service.getMap("select etime,bnum,bwords,money,exptime,uin from order_ticket_tb where id =? ", new Object[]{id});
		Long count= service.getLong("select count(id) from order_ticket_detail_tb where otid =? and uin is not null", new Object[]{id});
		int ret = 1;
		Integer bnum=0;
		Integer money = 0;
		if(bMap!=null){
			String bwords = (String)bMap.get("bwords");
			if(!words.equals("")){//更新祝福语
				service.update("update order_ticket_tb set bwords=? where id=? ", new Object[]{id});
				bwords = words;
			}
			if(bwords!=null&&bwords.length()>14)
				bwords = bwords.substring(0,11)+"...";
			Long exptime = (Long)bMap.get("exptime");
			bnum = (Integer)bMap.get("bnum");
			money = (Integer)bMap.get("money");
			Long ntime = System.currentTimeMillis()/1000;
			Map userMap = service.getMap("select wx_imgurl from user_info_tb where id=? ", new Object[]{bMap.get("uin")});
			if(userMap!=null&&userMap.get("wx_imgurl")!=null)
				request.setAttribute("carowenurl",userMap.get("wx_imgurl"));
			request.setAttribute("bwords",bwords);
			request.setAttribute("bnum",bnum);
			request.setAttribute("totalmoney",money);
			if(ntime>exptime&&count==0){//已过期
				request.setAttribute("tipwords", "折扣券已过期");
				ret =-1;
			}else {
				if(count==0){//没有折过红包，开始折红包
					if(bnum>0&&money>0){
						String insertSql = " insert into order_ticket_detail_tb (otid,amount,btype) values(?,?,?)";
						List<Object[]> values = new ArrayList<Object[]>();
						for(int i=0;i<bnum;i++){
							Object[] objects = new Object[]{id,money,1};
							values.add(objects);
						}
						int _ret = service.bathInsert(insertSql, values, new int[]{4,4,4});
						logger.error("新抢微作红包，写入红包....."+_ret);
					}
				}else if(count.intValue()==bnum){//已领完
					getBonusList(request,id);
					request.setAttribute("tipwords", "折扣券已领完");
					ret = -2;
				}
			}
		}else {//不存在
			request.setAttribute("tipwords", "折扣券已过期");
			ret =-3;
		}
		
		return ret;
	}
	/**查红包结果*/
	private void getBonusList(HttpServletRequest request,Long bid){
		//查出所有领取的红包
		List<Map<String, Object>> blList = service.getAll("select o.id,amount,ttime,wx_name,wx_imgurl,wxp_openid,o.ticketid,u.mobile from order_ticket_detail_tb o left join user_info_tb u on o.uin=u.id where otid=? and ttime is not null ", new Object[]{bid});
		String data = "[]";
		if(blList!=null&&!blList.isEmpty()){
			data = "[";
			for(Map<String, Object> map : blList){
				Long time = (Long)map.get("ttime");
				String wxname = (String)map.get("wx_name");
				String wxurl = (String)map.get("wx_imgurl");
				if(wxname==null){
					wxname = map.get("mobile")+"";
					if(wxname.length()>10){
						wxname = wxname.substring(0,3)+"****"+wxname.substring(7);
					}
				}
				if(wxurl==null){
					wxurl ="images/bunusimg/logo.png";
				}
				data +="{\"amount\":\""+StringUtils.formatDouble(map.get("amount"))+"\"," +
						"\"ttime\":\""+TimeTools.getTime_yyMMdd_HHmm(time*1000).substring(3)+"\"," +
						"\"wxname\":\""+wxname+"\",\"wxurl\":\""+wxurl+"\"},";
			}
			data = data.substring(0,data.length()-1);
			data +="]";
			request.setAttribute("haveget", blList.size());
		}
		request.setAttribute("havegetpic", "wxhaveget");
		request.setAttribute("data", data);
	}
	/**
	 * 查询所有停车券
	 * @param request
	 * @return
	 */
	private String getallTickets(HttpServletRequest request) {
		 //http://192.168.199.240/zld/carinter.do?action=gettickets&mobile=15801482643&type=0&page=1
		Integer page = RequestUtil.getInteger(request, "page", 1);
		Integer type = RequestUtil.getInteger(request, "type", 0);//0当前未使用且未过期的券 1当已使用或已过期的券
		String mobile = RequestUtil.getString(request, "mobile");
		String from = RequestUtil.processParams(request, "from");//区分微信公众号和客户端
		List<Object> params = new ArrayList<Object>();
		Long uin = getUinByMobile(mobile);
		String sql = "select * from ticket_tb where uin=? ";
		params.add(uin);
		if(!from.equals("wxpublic")){
			sql += " and type<? ";
			params.add(2);
		}
		
		Long btime =TimeTools.getToDayBeginTime();
		if(type==0){//当前未使用且未过期的券
			sql+=" and state=? and limit_day>=? order by limit_day ";
			params.add(0);
			params.add(btime);
		}else if(type==1){//当已使用或已过期的券
			sql+=" and ( state=? or limit_day<? ) order by id desc";
			params.add(1);
			params.add(btime);
		}
		List<Map<String, Object>> list = onlyService.getAll(sql, params, page, 20);
		String result = "[]";
		if(list != null){
			list = commonMethods.getTicketInfo(list, 2, -1L, 2);
			result= StringUtils.createJson(list).replace("null", "");
		}
		return result;
	}
	
	private String doVerifyPark(HttpServletRequest request) {
		String mobile = RequestUtil.getString(request, "mobile");
		Long uin = getUinByMobile(mobile);
		Long id = RequestUtil.getLong(request, "id", -1L);
		
		Integer isname = RequestUtil.getInteger(request, "isname", -1);
		Integer islocal = RequestUtil.getInteger(request, "islocal", -1);
		Integer ispay = RequestUtil.getInteger(request, "ispay", -1);
		Integer isresume = RequestUtil.getInteger(request, "isresume", -1);
		int ret = 0;//0审核失败 1审核成功 -1重复审核 
		if(id!=-1){
			Long count = service.getLong("select count(id) from park_verify_tb where uin =? and comid =?", new Object[]{uin,id});
			if(count>0)
				return "-1";
			ret = service.update("insert into park_verify_tb (isname,islocal,ispay,isresume,comid,uin,ctime) values(?,?,?,?,?,?,?)", 
					new Object[]{isname,islocal,ispay,isresume,id,uin,System.currentTimeMillis()/1000});
		}
		return ""+ret;
	}
	
	private String doPreVerifyPark(HttpServletRequest request) {
		String mobile = RequestUtil.getString(request, "mobile");
		String ids = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "ids"));
		logger.error(">>>>VerifyPark,ids:"+ids);
		Long uin = getUinByMobile(mobile);
		Double lng = RequestUtil.getDouble(request, "lng", 0d);
		Double lat = RequestUtil.getDouble(request, "lat", 0d);
		double d1 = 0.382346;//20公里以内
		double d2 = 0.291792;//20公里以内
		//每天只能审核三个未审核的车场
		Long count = onlyService.getLong("select count(ID) from park_verify_tb where uin =? and ctime >? ", new Object[]{uin,TimeTools.getToDayBeginTime()});
		if(count>2)
			return "{\"id\":\"-1\"}" ;
		String sql = "select id, company_name as name,resume as desc,type," +
				"longitude as lng ,latitude as lat from com_info_tb where longitude between ? and ? and latitude between ? and ? and  state =? " +
				"and upload_uin is not null and upload_uin !=? and id not in" +
				"(select comid from park_verify_tb where uin =? )  ";
		//Object [] values = new Object[]{lng-d1,lng+d1,lat-d2,lat+d2,2,uin,uin,1};
		List<Object> params = new ArrayList<Object>();
		params.add(lng-d1);
		params.add(lng+d1);
		params.add(lat-d2);
		params.add(lat+d2);
		params.add(2);
		params.add(uin);
		params.add(uin);
		if(!ids.equals("")){
//			if(ids.endsWith(","))
//				ids = ids.substring(0,ids.length()-1);
			if(ids.startsWith(","))
				ids = ids.substring(1);
			if(ids.indexOf(",")!=-1){
				String []_ids = ids.split(",");
				String preParams = "";
				
				for(int i=0;i<_ids.length;i++){
					if(_ids[i]==null||!Check.isNumber(_ids[i]))
						continue;
					if(i==0)
						preParams ="?";
					else {
						preParams +=",?";
					}
					params.add(Long.valueOf(_ids[i]));
				}
				sql +=" and id not in("+preParams+") ";
			}else {
				if(Check.isNumber(ids)){
					sql +=" and id not in(?) ";
					params.add(Long.valueOf(ids));
				}
			}
		}
		sql +=" order by id desc limit ?";
		params.add(1);
		Map uploadedCom = service.getMap(sql,params );
		logger.error("sql:"+sql+",/rparams"+params+" ,/rret:"+uploadedCom);
		if(uploadedCom!=null)
			return StringUtils.createJson(uploadedCom);
		
		return "{\"id\":\"-2\"}";
	}
	
	/*
	 * 订单详情
	 */
	private void doOrderDetail(HttpServletRequest request) {
		Long orderId = RequestUtil.getLong(request, "orderid", -1L);
		Double prePay = RequestUtil.getDouble(request, "prepay",0d);
		Double back = RequestUtil.getDouble(request, "back",0d);
		Integer first_flag = RequestUtil.getInteger(request, "first_flag", 0);//0:非首笔支付， 1：首笔支付
		logger.error("doOrderDetail>>>>orderid:"+orderId+",prePay:"+prePay+",back:"+back+",first_flag:"+first_flag);
		if(orderId!=-1){
			Map orderMap = service.getMap("select * from order_tb where id =? ", new Object[]{orderId});
			request.setAttribute("prepay",prePay);
			Double total = StringUtils.formatDouble(orderMap.get("total"));
			request.setAttribute("total",total);
			if(prePay<total){
				request.setAttribute("addmoney",total-prePay);
				request.setAttribute("back_dp", "补交金额");
			}else if(prePay>total){
				request.setAttribute("back_dp", "退还金额");
				request.setAttribute("addmoney",back);
			}
			request.setAttribute("orderid",orderMap.get("id"));
			request.setAttribute("state",orderMap.get("state"));
			request.setAttribute("btime",TimeTools.getTime_yyyyMMdd_HHmm((Long)orderMap.get("create_time")*1000));
			request.setAttribute("etime",TimeTools.getTime_yyyyMMdd_HHmm((Long)orderMap.get("end_time")*1000));
			Long comid = (Long)orderMap.get("comid");
			if(comid!=null){
				Map comMap = onlyService.getMap("select company_name from com_info_tb where id=?", new Object[]{comid});
				if(comMap!=null)
					request.setAttribute("comname", comMap.get("company_name"));
			}
			Map bonusMap = onlyService.getMap("select * from order_ticket_tb where order_id=? limit ? ", new Object[]{orderId,1});
			if(bonusMap!=null&&!bonusMap.isEmpty()){
				logger.error(">>>有微信红包...");
				request.setAttribute("bonusid", bonusMap.get("id"));
				request.setAttribute("bonus_money", bonusMap.get("money"));
				request.setAttribute("bonus_bnum", bonusMap.get("bnum"));
				request.setAttribute("bonus_type", bonusMap.get("type"));
				request.setAttribute("first_flag", first_flag);
			}else{
				request.setAttribute("bonusid", -1);
			}
			//微信公众号JSSDK授权验证
			Map<String, String> result = new HashMap<String, String>();
			try {
				result = publicMethods.getJssdkApiSign(request);
			}catch (Exception e) {
				e.printStackTrace();
				
			}
			System.out.println(result);
			//jssdk权限验证参数
			request.setAttribute("appid", Constants.WXPUBLIC_APPID);
			request.setAttribute("nonceStr", result.get("nonceStr"));
			request.setAttribute("timestamp", result.get("timestamp"));
			request.setAttribute("signature", result.get("signature"));
		}
	}

	private String getWxAccount(HttpServletRequest request) {
		//返回余额及可用停车券
		String mobile = RequestUtil.getString(request, "mobile");
		Long uin = RequestUtil.getLong(request, "wxp_uin", -1L);
		Double total = RequestUtil.getDouble(request, "total", 0d);
		Long uid = RequestUtil.getLong(request, "uid", -1L);//直付时，传收费员编号 
		logger.error("choose ditotal ticket of weixin>>>mobile:"+mobile+",uin:"+uin+",total:"+total+",uid:"+uid);
		Map<String, Object> userMap = null;
		if(!mobile.equals("")){
			userMap  = getUserByMobile(mobile);
			uin  = (Long)userMap.get("id");
		}else{
			userMap = getWxUserByUin(uin);
		}
		Object balance = userMap.get("balance");
		Map<String, Object> ticketMap = null;
		
		boolean parkuserblack = publicMethods.isBlackParkUser(uid, true);
		boolean userblack = publicMethods.isBlackUser(uin);
		boolean isCanuseCache = memcacheUtils.readUseTicketCache(uin);
		
		logger.error("uin:"+uin+",parkuserblack:"+parkuserblack+",parkuserblack:"+parkuserblack+",userblack:"+userblack+",isCanuseCache:"+isCanuseCache);
		if(uid > 0 && isCanuseCache && !parkuserblack && !userblack){
			Long count = onlyService.getLong("select count(*) from user_account_tb where uin=? and type=? ",
					new Object[] { uin, 1 });//判断是否是首笔支付
			if(count == 0){
				logger.error("is first pay>>>uin:"+uin+",count:"+count);
				ticketMap = commonMethods.chooseDistotalTicket(uin, uid, total);
			}
		}
		
		//处理没有返回到账户中的停车券
		if(!mobile.equals("")){
			commonMethods.checkBonus(mobile, uin);
		}
		String ret = "{\"balance\":\""+balance+"\",\"tickets\":[]}";
		String tickets = "[";
		if(ticketMap!=null)
			tickets +=StringUtils.createJson(ticketMap);//"{\"id\":\""+ticketMap.get("id")+"\",\"money\":\""+ticketMap.get("money")+"\"}";
		tickets +="]";
		ret = ret.replace("[]", tickets);
		return ret;
		//查代余额及代金券 http://127.0.0.1/zld/carowner.do?action=&mobile=15801270154&total=5
	}
	private String useTickets(HttpServletRequest request) {
		//http://192.168.199.240/zld/carinter.do?action=usetickets&mobile=15801482643&total=5&orderid=&uid=10700&preid=38878&utype=0
		String mobile = RequestUtil.getString(request, "mobile");
		Long preId = RequestUtil.getLong(request, "preid", -1L);
		Double total = RequestUtil.getDouble(request, "total", 0d);
		Long orderId = RequestUtil.getLong(request, "orderid", -1L);//普通订单，传订单编号
		Long uid = RequestUtil.getLong(request, "uid", -1L);//直付时，传收费员编号 
		Integer utype = RequestUtil.getInteger(request, "utype", 0);//0更早版本，不返比订单金额大的券，1老版本，自动选券时，返回普通券的最高抵扣金额，2新版本，根据券类型返回最高抵扣金额
		Integer ptype = RequestUtil.getInteger(request, "ptype", -1);//0账户充值；1包月产品；2停车费结算；3直付;4打赏 5购买停车券
		Integer source = RequestUtil.getInteger(request, "source", 0);//0客户端 1：微信公众号
		
		Long parkId = null;
		Long uin = getUinByMobile(mobile);
		logger.error("userTickets>>uin:"+uin+",orderid:"+orderId+",uid:"+uid+"preid:"+preId+",total:"+total+",utype:"+utype+",ptype:"+ptype+",uid:"+uid);
		if(orderId != -1){
			Map<String, Object> orderMap = onlyService.getMap("select comid,uid from order_tb where id=?", new Object[]{orderId});
			if(orderMap!=null){
				parkId = (Long)orderMap.get("comid");
				uid = (Long)orderMap.get("uid");
			}
		}else if(uid != -1){
			Map<String, Object> userMap2 = onlyService.getMap("select comid from user_info_tb where id = ? ", new Object[]{uid});
			if(userMap2!=null)
				parkId = (Long)userMap2.get("comid");
		}
		boolean isAuth = publicMethods.isAuthUser(uin);
		logger.error("userTickets>>uin:"+uin+",orderid:"+orderId+",parkId:"+parkId+",uid:"+uid+",isAuth:"+isAuth);
		List<Map<String, Object>> userTikcets = new ArrayList<Map<String,Object>>();
		if(uid != -1 && parkId != null && total > 0){
			List<Map<String, Object>> list = commonMethods.chooseTicket(uin, total, utype, uid, isAuth, ptype, parkId, orderId, source);
			if(list != null){
				userTikcets = list;
			}
		}
		String result = StringUtils.createJson(userTikcets);
		logger.error("userTickets>>uin:"+uin+",orderid:"+orderId+",size:"+userTikcets.size());
		return result.replace("null", "");
	}

	private String upPark(HttpServletRequest request) {
		//每天只能上传三个车场
		String mobile = RequestUtil.getString(request, "mobile");
		Long uin = getUinByMobile(mobile);
		String parkname  = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "parkname"));
		String desc  =  AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "desc"));
		String address  =  AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "addr"));
		//System.out.println(parkname);
		Double	 lng = RequestUtil.getDouble(request, "lng", 0D);
		Double	 lat = RequestUtil.getDouble(request, "lat", 0D);
		Integer type = RequestUtil.getInteger(request, "type", 1);
		Long ntime = System.currentTimeMillis()/1000;
		int ret = 0;
		Long count = onlyService.getLong("select count(ID) from com_info_Tb where upload_uin=? and create_time >? ", 
				new Object[]{uin,TimeTools.getToDayBeginTime()});
		if(count>2)
			return "-1";
		if(lng!=0&&lat!=0){
			try {
				ret = service.update("insert into com_info_tb (company_name,resume,address,longitude,latitude,state," +
						"create_time,upload_uin,type)values(?,?,?,?,?,?,?,?,?)", new Object[]{parkname,desc,address,lng,lat,2,ntime,uin,type});
			} catch (Exception e) {
				ret=-2;
			}
		}
		return ret+"";
	}

	private String getComments(HttpServletRequest request) {
		Integer page = RequestUtil.getInteger(request, "page", 1);
		Long comId= RequestUtil.getLong(request, "comid", -1L);
		String mobile = RequestUtil.getString(request, "mobile");
		Long userId = getUinByMobile(mobile);
		List<Map<String, Object>> comList =onlyService.getPage("select * from com_comment_tb where comid=? order by id desc",
				new Object[]{comId},page,20);
		List<Map<String, Object>> resultMap = new ArrayList<Map<String,Object>>();
		boolean ishave=false;
		if(comList!=null&&comList.size()>0){
			for(Map<String, Object> map : comList){
				Map<String, Object> iMap = new HashMap<String, Object>();
				Long createTime = (Long)map.get("create_time");
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(createTime*1000);
				String times = TimeTools.getTime_MMdd_HHmm(createTime*1000);
				Long uin = (Long)map.get("uin");
				iMap.put("parkId",comId);// 评价的车场ID
				iMap.put("date", times.substring(0,5));// 评价日期：7-24
				iMap.put("week", "星期"+StringUtils.getWeek(calendar.get(Calendar.DAY_OF_WEEK)));//评价日期是星期几：星期四
				iMap.put("time", times.substring(6));// 评价的车场ID
				iMap.put("info",  map.get("comment"));//评价内容：巴拉巴拉一大串废话。。。
				iMap.put("ctime",  createTime);
				iMap.put("user", publicMethods.getCarNumber(uin));// 评价者：车主（车牌号：京A***A111）
				if(uin.equals(userId)){
					ishave = true;
					resultMap.add(0,iMap);
				}else {
					resultMap.add(iMap);
				}
			}
			if(!ishave&&page==1&&userId!=-1){
				Map tMap = service.getMap("select * from com_comment_tb where comid=? and uin=? ", new Object[]{comId,userId});
				if(tMap!=null&&!tMap.isEmpty()){
					Map<String, Object> iMap = new HashMap<String, Object>();
					Long createTime = (Long)tMap.get("create_time");
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(createTime*1000);
					String times = TimeTools.getTime_MMdd_HHmm(createTime*1000);
					Long uin = (Long)tMap.get("uin");
					if(uin.equals(userId))
						ishave = true;
					iMap.put("parkId",comId);// 评价的车场ID
					iMap.put("date", times.substring(0,5));// 评价日期：7-24
					iMap.put("week", "星期"+StringUtils.getWeek(calendar.get(Calendar.DAY_OF_WEEK)));//评价日期是星期几：星期四
					iMap.put("time", times.substring(6));// 评价的车场ID
					iMap.put("info",  tMap.get("comment"));//评价内容：巴拉巴拉一大串废话。。。
					iMap.put("ctime",  createTime);
					iMap.put("user", publicMethods.getCarNumber(uin));// 评价者：车主（车牌号：京A***A111）
					resultMap.add(0,iMap);
				}
			}
			return StringUtils.createJson(resultMap);
		}
		return "[]";
	}

	private String getParkInfo(HttpServletRequest request) {
		Long pid = RequestUtil.getLong(request, "comid", -1L);
		logger.error("comid:"+pid);
		String result = "{}";
		/*
		 * 
		 * id;// 停车场ID
			 name;// 停车场名称
			 lng;// 纬度
			 lat;// 经度
			 free;// 空闲车位数（等同于freespace）
			 price;// 当前价格（元/每小时，负数表示免费，正数表示有价格，0或“”表示没有价格信息）
			 total;// 总车位
			 addr;// 停车场地址
			 phone;// 停车场电话
			 monthlypay;// 是否支持月卡
			 epay;// 是否支持手机支付
			 desc;// 车场描述
 			[photo_url];// 车场照片的url地址集合
	["3","停车宝测试车场（请勿购买产品）","116.317565","40.043024","50","","50","北京市北京市海淀区上地信息路26号","","1","",["parkpics/3_1421131961.jpeg"]]
		 */
		//车场名，电话，空闲/车位总数，图片，地址，价格，描述
		if(pid!=null&&pid>0){
			Map<String,Object> comMap = onlyService.getMap("select id,longitude lng,latitude lat,epay,share_number,company_name as name,mobile phone," +
					"parking_total as total,address addr,remarks as desc ,type ,parking_type,empty " +
					"from com_info_tb where id =?", new Object[]{pid});
			
			//查图片
			Map<String,Object> picMap = onlyService.getMap("select picurl from com_picturs_tb where comid=? order by id desc limit ?",
					new Object[]{pid,1});
			String picUrls = "";
			if(picMap!=null&&!picMap.isEmpty()){
				picUrls=(String)picMap.get("picurl");//"http://121.40.130.8/zld/parkpics/"+
			}
			/**扬州项目*
			else {
				Integer parkType = (Integer)comMap.get("parking_type");
				if(parkType==4){
					picUrls="8674_1460623804316.png";
				}else {
					picUrls="8694_1460616855017.png";
				}
			}
			//查空闲车位数
			Integer total= (Integer) comMap.get("empty");
			
			/**扬州项目***/
			/**停车宝项目***/
			Map<String, Object> map = onlyService.getMap("select sum(amount) free from remain_berth_tb where comid=? and state=? ", 
					new Object[]{pid, 0});
			Long free = 0L;
			if(map != null && map.get("free") != null){
				free = Long.valueOf(map.get("free") + "");
			}
			logger.error("get free lots>>>comid:"+pid+",free:"+free);
			/**停车宝项目***/
			//查价格
			Integer type =(Integer)comMap.get("type");
			String price ="";
			if(type==0)
				price = getPrice(pid);
			comMap.put("free", free);
			comMap.put("price", price);
			comMap.put("photo_url", "[\""+picUrls+"\"]");
			comMap.remove("share_number");
//			result="[\""+comMap.get("id")+"\",\""+comMap.get("company_name")+"\"" +
//					",\""+comMap.get("longitude")+"\",\""+comMap.get("latitude")+"\"" +
//					",\""+total+"\",\""+price+"\"" +
//					",\""+comMap.get("parking_total")+"\",\""+comMap.get("address")+"\"" +
//					",\""+comMap.get("mobile")+"\",\""+comMap.get("epay")+"\"" +
//					",\""+comMap.get("remarks")+"\",[\""+picUrls+"\"]]";
			result = StringUtils.createJson(comMap);
		}
		return result.replace("null", "");
	}
	/**
	 * 查停车价格
	 * @param parkId
	 * @return
	 */
	private String getPrice(Long parkId){
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		//开始小时
		int bhour = calendar.get(Calendar.HOUR_OF_DAY);
		List<Map<String,Object>> priceList=onlyService.getAll("select * from price_tb where comid=? " +
				"and state=? and pay_type=? order by id desc", new Object[]{parkId,0,0});
		if(priceList==null||priceList.size()==0){//没有按时段策略
			//查按次策略
			priceList=onlyService.getAll("select * from price_tb where comid=? " +
					"and state=? and pay_type=? order by id desc", new Object[]{parkId,0,1});
			if(priceList==null||priceList.size()==0){//没有按次策略，返回提示
				return "0元/次";
			}else {//有按次策略，直接返回一次的收费
				Map<String,Object> timeMap =priceList.get(0);
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
				for(Map<String,Object> map : priceList){
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
	private Long getUinByMobile(String mobile){
		if(!"".equals(mobile)){
			Map userMap = onlyService.getPojo("select id from user_info_Tb where mobile=? and auth_flag=? ", new Object[]{mobile,4});
			if(userMap!=null&&!userMap.isEmpty()){
				return (Long) userMap.get("id");
			}
		}
		return -1L;
	}
	private String getParkNameByComid(Long comid){
		Map<String, Object> comMap = onlyService.getMap("select company_name from com_info_tb where id =? ",new Object[]{comid});
		if(comMap!=null)
			return (String)comMap.get("company_name");
		return "";
				
	}
	private Map<String, Object> getUserByMobile(String mobile){
		if(!"".equals(mobile)){
			Map userMap = onlyService.getPojo("select * from user_info_Tb where mobile=? and auth_flag=? ", new Object[]{mobile,4});
			if(userMap!=null){
				return userMap;
			}
		}
		return null;
	}
	private Map<String, Object> getWxUserByUin(Long uin){
		if(uin != -1){
			Map<String, Object> userMap = onlyService.getMap(
					"select * from wxp_user_tb where uin=? ",
					new Object[] { uin });
			if(userMap != null){
				return userMap;
			}
		}
		return null;
	}
	
	//查询充电桩信息
	private  List<Map<String, Object>> getChargeSite(){
		List<Map<String, Object>> resList = new ArrayList<Map<String,Object>>();
		//String url ="http://api.wyqcd.cn:8004/api/Sta/PostSta";
		String url =CustomDefind.getValue("RECHARGEURL");//"http://open.teld.cn/api/Sta/PostSta";
		//String url ="http://127.0.0.1/zld/carinter.do?action=RouteStatData";
		String signKey = "15JBfEs6QnRPiLMlN3SXZrLq9UvXXdY7";
		String token =getChargeAccToken();
		//String token ="VbuEV8c0pUXOlxPDS6bTnRPXe_4opVooEQyf1NR61FUM_kUWhBfljvjqChGTpc3ZoZPEMvNgDfq9U5QTLE3JPkOfRBVbGIHSvF-ff2VbuhPqp8vMi_V2nxkbPQP-3EpwstgJTemH5vrT-I9o1PN1sDlc3ngjmrZOQm1kzACoh_Kync1aiGnLsJWKDYjQvMzKZVXtA7a6m-Wa3tSMxF7-bG-rM-zliBsOQ2Q5FMi6DwDTER2u";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("province","江苏");
		paramsMap.put("city","扬州");
		paramsMap.put("region", "");
		paramsMap.put("type","" );
		paramsMap.put("opState", "");
		paramsMap.put("pageNo", "1");
		paramsMap.put("pageSize", "100");
		String linkedParam=StringUtils.createLinkedJson(paramsMap);
		//System.out.println(linkedParam);
		String sign =null;
		try {
			String signedStr = "requestMsg="+linkedParam+signKey;
			//System.out.println(signedStr);
			sign= StringUtils.MD5(new String(signedStr.getBytes("utf-8"))).toLowerCase();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("sign:"+sign);
		String queryDes="";
		try {
			queryDes = new String(ZldDesUtils.encrypt(linkedParam));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("queryDes:"+queryDes);
		String queryParam = "{\"requestMsg\":\""+queryDes+"\",\"sign\":\""+sign+"\"}";
		//System.out.println("queryparams:"+queryParam);
		String result = new HttpProxy().doTeldPost(url, token, queryParam);
	//	logger.error("取充电桩数据:"+result);
		try {
			JSONObject jsonObject = new JSONObject(result);
			String resultValue  = jsonObject.getString("resultValue");
			String res = ZldDesUtils.decrypt(resultValue);
			jsonObject = new JSONObject(res);
			JSONArray array= jsonObject.getJSONArray("staList");
			for(int i=0;i<array.length();i++){
				jsonObject = array.getJSONObject(i);
				Map<String, Object> retMap = new HashMap<String, Object>();
				for (Iterator<String> iter = jsonObject.keys(); iter.hasNext();) { 
				     String key = (String)iter.next();
				     retMap.put(key, jsonObject.get(key));
				}
				resList.add(retMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resList;
	}
		//取充电桩接口TOKEN，7200秒失效 
	private  String getChargeAccToken(){
		String  cachetoken = memcacheUtils.doStringCache("recharge_acctoken", null, null);
		String token = null;
		Long ntime = System.currentTimeMillis()/1000;
		if(cachetoken!=null&&cachetoken.length()>11){
			String time = cachetoken.substring(0,10);
			if(Check.isLong(time)){
				Long lastTime  = Long.valueOf(time);
				if(lastTime+7200>ntime){
					token =cachetoken.substring(10);
				}
			}
			logger.error("充电桩缓存token:"+token);
		}
		if(token==null||token.trim().equals("")){
			String url ="http://open.teld.cn/OAuth/Token";
			Map<String, String> paramsMap = new HashMap<String, String>();
			paramsMap.put("grant_type", "client_credentials");
			paramsMap.put("client_id", "teldhh20cdbpb2umuocw");
			paramsMap.put("client_secret", "Y0iFg61V12");
			String re = new HttpProxy().doPost(url,paramsMap);
			//{"access_token":"7YheTLsEhZpUx_GNvvcjxMeYaKOuumoYnYZSbzBzIDqh6vSkYwMs7TejVby-Jt9Isb8waxehy6L7Qe4AsxQSiY8BTV2qlg1cPwCsy-dSx0YqX1ZyLMhQqlj4voCOwxF5ACM3EXngJNu5HmcJXQc1qfPEIuMllf2z99W5yooil03969rP615jQHjKJnKOPasoZ_oEohQBP3RzXPt0Vhu8OBp20yeFpJdDx8JrmAjZwuO_oLbv","token_type":"bearer","expires_in":7199}
			try {
				logger.error("充电桩取token:"+re);
				JSONObject jsonObject = new JSONObject(re);
				token = jsonObject.getString("access_token");
				if(token!=null)
					memcacheUtils.doStringCache("recharge_acctoken", ntime+token, "update");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return token;
	}
	
	//加载所有自行车数据，数据与原来数据条数相同时，更新，不同时，删除并插入
	private void updateBikeInfo(){
		String url ="http://172.16.220.32:8080/wsclient/tsclient?type=bike&action=queryall";
		//String url ="http://127.0.0.1/wsclient/tsclient?type=bike&action=queryall";
		String result = new HttpProxy().doGet(url);
		//{ret=0, zdaddr=翠岗路路南, wdinfo=32.396822, totalcws=32, zdname=职大北门, jdinfo=119.382417, leftcws=29}
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(result!=null){
			Map<String, Object> resultMap = ZldXMLUtils.parserStrXml(result);
			//<RET>0</RET><INFO></INFO><RECCOUNT>412</RECCOUNT><TOTALREC>412</TOTALREC><RETINFO>|
			if(resultMap!=null){
				String ret = (String)resultMap.get("ret");
				if(ret!=null&&ret.equals("0")){
					String total = (String)resultMap.get("totalrec");
					if(total!=null&&Check.isNumber(total)){
						Integer t = Integer.valueOf(total);
						if(t>0){//update
							String sql = "update city_bike_tb set plot_count=?,surplus=? where id=? ";
							String data = (String)resultMap.get("retinfo");
							if(data!=null){
								if(data.startsWith("|"))
									data = data.substring(1);
								String d[] = data.split("\\|");
								List<Object[]> values = new ArrayList<Object[]>();
								for(int i=0;i<d.length;i+=7){
									Integer count = 0;
									if(Check.isNumber(d[i+5]))
											count = Integer.valueOf(d[i+5]);
									Integer surplus = 0;
									if(Check.isNumber(d[i+6]))
										surplus = Integer.valueOf(d[i+6]);
									values.add( new Object[]{count,surplus,Long.valueOf(d[i])});
								}
								int todb = service.bathInsert(sql, values, new int[]{4,4,4});
								logger.error("bikeall update "+todb);
							}
						}
						/*else{//重新写入记录
							String sql = "insert into city_bike_tb (id,name,address,longitude,latitude,plot_count,surplus)" +
									" values(?,?,?,?,?,?,?);";
							String data = (String)resultMap.get("retinfo");
							if(data!=null){
								if(data.startsWith("|"))
									data = data.substring(1);
								String d[] = data.split("\\|");
								if(d.length>100){
									int s = service.update("delete from city_bike_tb where id>?",new Object[]{0});
									if(s>0){
										List<Object[]> values = new ArrayList<Object[]>();
										for(int i=0;i<d.length;i+=7){
											String lng = d[i+3];
											String lat = d[i+4];
											Double lg = 0.0;
											double la = 0.0;
											if(Check.isDouble(lng))
												lg = ZldUploadUtils.formatDouble(lng, 6);
											if(Check.isDouble(lat)){
												la=ZldUploadUtils.formatDouble(lat, 6);
											}
											Integer count = 0;
											if(Check.isNumber(d[i+5]))
												count = Integer.valueOf(d[i+5]);
											Integer surplus = 0;
											if(Check.isNumber(d[i+6]))
												surplus = Integer.valueOf(d[i+6]);
											values.add( new Object[]{Long.valueOf(d[i]),d[i+1],d[i+2],lg,la,count,surplus});
										}
										int todb = service.bathInsert(sql, values, new int[]{4,12,12,3,3,4,4});
										logger.error("bikeall insert "+todb);
									}
								}
							}
						}*/
					}
				}
			}
		}
	}
	private String historyCarOrder(HttpServletRequest request){
		//http://service.yzjttcgs.com/zld/carinter.do?action=carorderhistory&page=1&size=10&mobile=18101333937
		//日期，停车场名称，总价
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "size", 200);
		List<Object> params = new ArrayList<Object>();
		String mobile = RequestUtil.getString(request, "mobile");
		Long uin = getUinByMobile(mobile);
		params.add(1);
		params.add(uin);
		params.add(1);
		//Long time = TimeTools.getToDayBeginTime();
		List<Map<String, Object>> infoMaps = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> list = onlyService.getAll("select t.id, t.create_time,t.total,c.company_name from order_tb t,com_info_tb c " +
				"where t.comid=c.id and  t.state=? and t.uin=? and t.pay_type>? order by t.end_time desc",// and create_time>?",
				params, pageNum, pageSize);
		params.clear();
		params.add(uin);
		params.add(8);
	
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
		//System.err.println(infoMaps);
		String result = "{\"parklist\":"+StringUtils.createJson(infoMaps)+"}";
		logger.error("car order history:"+result);
		return result;
	}

}
