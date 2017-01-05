package com.zld.struts.request;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import pay.Constants;

import com.zld.AjaxUtil;
import com.zld.impl.MemcacheUtils;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.LogService;
import com.zld.utils.HttpProxy;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.wxpublic.util.CommonUtil;
import com.zld.wxpublic.util.PayCommonUtil;

public class ParkingAttendantAction extends Action{
	
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	@Autowired
	private LogService logService;
	@Autowired
	private MemcacheUtils memcacheUtils;
	private Logger logger = Logger.getLogger(ParkingAttendantAction.class);
	
	
	/**
	 *泊车
	 */
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		
		String action = RequestUtil.getString(request, "action");
		logger.error(">>>ParkingAttendantAction action:"+action);
		if(action.equals("getstate")){//获取泊车员状态
			AjaxUtil.ajaxOutput(response, getstate(request));
			//http://192.168.199.240/zld/attendant.do?action=getstate&uin=1000004
		}else if(action.equals("editstate")){//修改泊车员状态
			//http://192.168.199.240/zld/attendant.do?action=editstate&uin=1000004&state=24
			AjaxUtil.ajaxOutput(response, editstate(request));
		}
		////////////=================泊车员接口======================/////////////////////
		//interface 3
		else if(action.equals("getstopcar")){//泊车员接车接单  
			//状态:0车主泊车请求 1泊车员已响应泊车 2正在泊车  3泊车完成 4车主取车请求 5 泊车员已响应取车 6泊车员正在取车 7等待支付 8支付成功
			//订单ID
			AjaxUtil.ajaxOutput(response,getstopcar(request));
			//http://192.168.199.240/zld/attendant.do?action=getcar&uid=&id=
		}
		//interface 4
		else if(action.equals("havegetcar")){//泊车员已成功接车
			AjaxUtil.ajaxOutput(response, havegetcar(request));
		}
		//interface 5
		else if(action.equals("uporderpic")){//泊车员上传泊车订单完成后车辆照片
			AjaxUtil.ajaxOutput(response, uporderpic(request));
		}
		//interface 6
		else if(action.equals("complstop")){//泊车员完成泊车
			//车辆停车点的经纬度
			AjaxUtil.ajaxOutput(response,complstop(request));
		}
		//interface 7
		else if(action.equals("getbackcar")){//泊车员抢车主还车请求
			//状态:0车主泊车请求 1泊车员已响应泊车 2正在泊车  3泊车完成 4车主取车请求 5 泊车员已响应取车 6泊车员正在取车 7等待支付 8支付成功
			AjaxUtil.ajaxOutput(response, getbackcar(request));
			//http://192.168.199.240/zld/attendant.do?action=getbackcar&uid=&id=&distance=
		}
		//interface 8
		else if(action.equals("havebackcar")){//泊车员已从泊车点取到车主要还的车
			Long id = RequestUtil.getLong(request, "id", -1L);
			int ret = 0;
			if(id!=-1){
				ret = daService.update("update carstop_order_tb set state = ? where id=? ", new Object[]{6,id});
			}
			AjaxUtil.ajaxOutput(response, "{\"result\":\""+ret+"\"}" );
		}
		//interface 9
		else if(action.equals("complorder")){//泊车员还车，结算订单
			AjaxUtil.ajaxOutput(response, comlorder(request));
		}
		//interface 10
		else if(action.equals("payorder")){
			AjaxUtil.ajaxOutput(response, payorder(request));
		}else if(action.equals("getdata")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String result = getDatas(id);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("handleqr")){//扫描收费员二维码
			Long uid = RequestUtil.getLong(request, "uid", -1L);
			String res = "";
			if(uid!=-1){
				res = handleqr(request,uid);
				if(res.indexOf("currentorder")!=-1){
					String []params = res.split("_");
					Long uin = Long.valueOf(params[1]);
					Long id = Long .valueOf(params[2]);
					return mapping.findForward(currentOrder(request, uin, id));
				}else {
					return mapping.findForward(res);
				}
			}
			//AjaxUtil.ajaxOutput(response, "");
		}
		
		/////////============泊车员接口======================/////////////////////		
		
		/////////============车主请求=======================////////////////////
		else if(action.equals("wantstop")){//车主要泊车
			String res = wantstop(request);
			if(res.indexOf("currentorder")!=-1){
				String []params = res.split("_");
				Long uin = Long.valueOf(params[1]);
				Long id = Long .valueOf(params[2]);
				return mapping.findForward(currentOrder(request, uin, id));
			}else {
				return mapping.findForward(res);
			}
			
			//http://192.168.199.240/zld/attendant.do?action=wantstop
		}else if(action.equals("getstopsbyll")){//根据地区查泊车点
			String ret =getStopByLL(request);
			logger.error(">>>ret:"+ret);
			AjaxUtil.ajaxOutput(response, ret);
		}else if(action.equals("stopdetail")){//泊车点详情
			stopdetail(request,null,null,null,null,null);
			return mapping.findForward("stopdetail");
		}else if(action.equals("cancelorder")){
			AjaxUtil.ajaxOutput(response, cancelOrder(request));
		}else if(action.equals("stopcar")){//车主请求泊车
			AjaxUtil.ajaxOutput(response, stopcar(request));
		}else if(action.equals("backcar")){//车主请求还车
			AjaxUtil.ajaxOutput(response, backcar(request));
		}else if(action.equals("getcarstate")){//车主查询订单是否已接单
			AjaxUtil.ajaxOutput(response, getCarState(request));
		}else if(action.equals("pay")){//车主支付页面
			pay(request);
			return mapping.findForward("payorder");
		}else if(action.equals("stopdret")){//>泊车中已接单
			logger.error(">>>泊车中已接单....");
			return mapping.findForward("stopresult");
		}else if(action.equals("currorder")){//车主查询订单详情
			String forword = currentOrder(request,null,null);
			return mapping.findForward(forword);
			//http://192.168.199.240/zld/attendant.do?action=currorder&uin=21533
		}else if(action.equals("topay")){//
			String ret = topay(request);
			return mapping.findForward(ret);
		}else if(action.equals("weixinpay")){
			String result = weixinpay(request);
			return mapping.findForward(result);
		}else if(action.equals("orderlist")){
			//http://192.168.199.240/zld/attendant.do?action=orderlist&openid=ohx0juLoqWRtOnNGhUbAMCVi3vw4
			return mapping.findForward(orderList(request));
		}else if(action.equals("getpic")){//取泊车点照片
			String fname = RequestUtil.getString(request, "id");
			String dbName = RequestUtil.getString(request, "db");
			if(dbName.equals(""))
				dbName = "park_pics";
			mongoDbUtils.getPicByFileName(fname, dbName, response);
		}
		/////////============车主请求=======================////////////////////	
		return null;
	}
	
    private  String getStopByLL(HttpServletRequest request) {
    	Double lng = RequestUtil.getDouble(request, "lng", 0d);
    	Double lat = RequestUtil.getDouble(request, "lat", 0d);
    	Long uin = RequestUtil.getLong(request, "uin", -1L);
    	Map<String, Integer> csMap = new HashMap<String, Integer>();
    	csMap.put("北京市", 110000);
    	csMap.put("济南市", 370100);
    	csMap.put("青岛市", 370200);
    	Integer citycode=0;
    	if(lng>70&&lat>3){
    		StringBuffer sb = new StringBuffer("http://api.map.baidu.com/geocoder/v2/?ak=InjEBUoTWHZWkCKNHSUOZFP8&output=json&pois=1&location=");
    		sb.append(lat).append(",").append(lng);//拼接请求参数
    		String local = new HttpProxy().doGet(sb.toString());
    		logger.error(sb+",city:"+local);
    		String city ="";
    		try {
				city = new JSONObject(local).getJSONObject("result").getJSONObject("addressComponent").getString("city");
				logger.error("city:"+city);
				if(!city.equals("")){
					Iterator<String> keys = csMap.keySet().iterator();
					while (keys.hasNext()) {
						String key = keys.next();
						if(city.indexOf(key)!=-1){
							citycode = csMap.get(key);
						}
					}
				}
			} catch (JSONException e) {
				logger.error(e.getMessage());
			}
    	}
    	logger.error("citycode:"+citycode);
    	if(citycode!=0){
    		List<Map<String, Object>> carStopsList =null;
    		carStopsList = daService.getAll("select id,name from car_stops_tb where state=? and city=?  order by id  ", new Object[]{0,citycode});
    		if(carStopsList!=null&&carStopsList.size()>0)
    			return  StringUtils.createJson(carStopsList);
    		else
    			return "-1";
    	}else {
			return "-2";
		}
	}
	public static void main(String[] args) {
		StringBuffer sb = new StringBuffer("http://api.map.baidu.com/geocoder/v2/?ak=InjEBUoTWHZWkCKNHSUOZFP8&output=json&pois=1&location=");
		sb.append("39.927203").append(",").append("116.447442");//拼接请求参数
		String local = new HttpProxy().doGet(sb.toString());
		System.out.println(local);
		String city ="";
		try {
			city = new JSONObject(local).getJSONObject("result").getJSONObject("addressComponent").getString("city");
			System.out.println(city);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private String handleqr(HttpServletRequest request, Long uid) {
		String openid =  RequestUtil.getString(request, "openid");
		if("".equals(openid))
			openid=getOpenid(request);
		if(openid==null||"null".equals(openid)){
			return "error";
		}
		request.setAttribute("openid",openid);
		logger.error("openid:"+openid);
		Long uin = getUinByOpenid(openid);
		logger.error("uin:"+uin);
		if(uin==null||uin==-1){//没有绑定公众号，到微信公共号登录页面...
			request.setAttribute("topage", "wantstop");
			request.setAttribute("action", "wxpublic.do?action=bind");
			logger.error(">>>到微信公共号登录页面....");
			return "login";
		}else {//是否同意泊车协议，是否要再次提醒
			Integer isRemind = RequestUtil.getInteger(request, "isremind", -1);
			if(isRemind!=-1){
				int ret = daService.update("update zld_protocol_tb set is_remind=? ,agree=? where uin=? ", new Object[]{isRemind,1,uin});
				if(ret<1){
					daService.update("insert into zld_protocol_tb(uin,ctime,is_remind,agree) values(?,?,?,?) ", 
							new Object[]{uin,System.currentTimeMillis()/1000,isRemind,1});
				}
			}else {
				Map proMap = daService.getMap("select * from zld_protocol_tb where uin=? ", new Object[]{uin});
				if(proMap!=null){
					isRemind = (Integer)proMap.get("is_remind");
					Integer agree = (Integer)proMap.get("agree");
					if(agree==0||isRemind==0){
						request.setAttribute("uid", uid);
						request.setAttribute("openid", openid);
						request.setAttribute("action", "handleqr");
						return "protocol";
					}
				}else {
					request.setAttribute("uid", uid);
					request.setAttribute("openid", openid);
					request.setAttribute("action", "handleqr");
					return "protocol";
				}
			}
		}
		//车主车牌
		String carNumber = publicMethods.getCarNumber(uin);
		if(carNumber.equals("车牌号未知")){//没有完成车牌号绑定 ，到微信公共号完成车牌号页面...
			Map userMap = daService.getMap("select mobile from user_info_Tb where id =? ", new Object[]{uin});
			if(userMap!=null){
				request.setAttribute("mobile", userMap.get("mobile"));
				request.setAttribute("topage", "wantstop");
				request.setAttribute("openid", openid);
				logger.error(">>>到微信公共号完成车牌号页面....");
				return "addcar";
			}else 
				return "error";
		}
		Long id = null;
		List<String> uids = new ArrayList<String>();
		uids.add(uid+"_1");
		//查找是否已存在订单
		Map coMap = daService.getMap("select id,cid,state,lng,lat from carstop_order_tb where uin =? and state <? order by id desc limit ?" , new Object[]{uin,8,1});
		if(coMap!=null){//有订单
			logger.error(">>>>>>carstop:已存在订单...."+coMap);
			Integer state = (Integer)coMap.get("state");
			id = (Long) coMap.get("id");
			if(state==3||state==4){//发接车请求//状态是已经有泊车员接单，重发消息
				int r = sendAttendantMessage(carNumber,uids,id,9);
				if(r>0){//已发消息给泊车员,还车请求
					int ret = daService.update("update carstop_order_tb set state=?,euid=?,end_time=? where id=? ", new Object[]{5,uid,System.currentTimeMillis()/1000,id});
					if(ret==1){
						daService.update("update user_info_Tb set online_flag=? where id=?",new Object[]{24,uid});
					}
				}
				logger.error(">>>>>>>接车消息发给了"+r+"个泊车员："+uids);
			}else if(state==0||state==1){
				int r = sendAttendantMessage(carNumber,uids,id,8);
				if(r>0){//已发消息给泊车员,还车请求
					int ret = daService.update("update carstop_order_tb set state=?,buid=? where id=? ", new Object[]{1,uid,id});
					if(ret==1){
						daService.update("update user_info_Tb set online_flag=? where id=?",new Object[]{25,uid});
					}
				}
				logger.error(">>>>>>>取车消息发给了"+r+"个泊车员："+uids);
			}else if(state<8){//订单已接车，但没有完成，到当前订单页面
				logger.error(">>>>>>carstop:订单已接车，到当前订单页面"+coMap);
			}
		}else {//生成并分配到泊车员
			id = daService.getkey("seq_carstop_order_tb");
			Long cid = null;
			Map localMap = daService.getMap("select * from user_local_tb where uid = ? and ctime =" +
					"(select max(ctime) from user_local_tb where uid=?)", new Object[]{uid,uid});
			Double lat = 0d;
			Double lng = 0d;
			if(localMap!=null){
				lat =Double.valueOf(localMap.get("lat")+"");
				lng = Double.valueOf(localMap.get("lon")+"");
			}
			List<Map<String,Object>> allStops = daService.getAll("select id,longitude,latitude from car_stops_tb where state=?", new Object[]{0});
			double d = 0;
			if(allStops!=null && !allStops.isEmpty()){
				for(Map<String,Object> map : allStops){
					Double lat1 = Double.valueOf(map.get("latitude")+"");
					Double lng1 = Double.valueOf(map.get("longitude")+"");
					double d1 = StringUtils.distance(lng, lat, lng1, lat1);
					if(d==0||d>d1){
						d = d1;
						cid = (Long)map.get("id");
					}
				}
			}
			String sql = "insert into carstop_order_tb(id,cid,uin,car_number,start_time,state,buid,lng,lat) values (?,?,?,?,?,?,?,?,?)";
			Object[] values = new Object[]{id,cid,uin,carNumber,System.currentTimeMillis()/1000,1,uid,lng,lat};
			int ret = daService.update(sql, values);
			if(ret ==1){//新建订单成功,通知泊车员接车...
				int r = sendAttendantMessage(carNumber,uids,id,8);
				/*if(r>0){//已发消息给泊车员,泊车请求
					 ret = daService.update("update carstop_order_tb set state=?,end_time=? where id=? ", new Object[]{1,System.currentTimeMillis()/1000,id});
				}*/
				daService.update("update user_info_Tb set online_flag=? where id=?",new Object[]{24,uid});
				logger.error(">>>>>>>取车消息发给了"+r+"个泊车员："+uids);
			}
		}  
		return "currentorder_"+uin+"_"+id;
	}

	private String orderList(HttpServletRequest request) {
		String mobile = RequestUtil.getString(request, "mobile");
		request.setAttribute("mobile", mobile);
		return "orderlist";
	}
	private String wantstop(HttpServletRequest request) throws Exception {
		//泊车点列表
    	String openid =  RequestUtil.getString(request, "openid");
		if("".equals(openid))
				openid=getOpenid(request);
		if(openid==null||"null".equals(openid)){
			return "error";
		}
		request.setAttribute("openid",openid);
		logger.error("openid:"+openid);
		Long uin = getUinByOpenid(openid);
		logger.error("uin:"+uin);
		if(uin==null||uin==-1){//没有绑定公众号，到微信公共号登录页面...
			request.setAttribute("topage", "wantstop");
			request.setAttribute("action", "wxpublic.do?action=bind");
			logger.error(">>>到微信公共号登录页面....");
			return "login";
		}else {//是否同意泊车协议，是否要再次提醒
			Integer isRemind = RequestUtil.getInteger(request, "isremind", -1);
			if(isRemind!=-1){
				int ret = daService.update("update zld_protocol_tb set is_remind=? ,agree=? where uin=? ", new Object[]{isRemind,1,uin});
				if(ret<1){
					daService.update("insert into zld_protocol_tb(uin,ctime,is_remind,agree) values(?,?,?,?) ", 
							new Object[]{uin,System.currentTimeMillis()/1000,isRemind,1});
				}
			}else {
				Map proMap = daService.getMap("select * from zld_protocol_tb where uin=? ", new Object[]{uin});
				if(proMap!=null){
					isRemind = (Integer)proMap.get("is_remind");
					Integer agree = (Integer)proMap.get("agree");
					if(agree==0||isRemind==0){
						request.setAttribute("openid", openid);
						request.setAttribute("action", "wantstop");
						return "protocol";
					}
				}else {
					request.setAttribute("openid", openid);
					request.setAttribute("action", "wantstop");
					return "protocol";
				}
			}
		}
		//车主车牌
		String carNumber = publicMethods.getCarNumber(uin);
		if(carNumber.equals("车牌号未知")){//没有完成车牌号绑定 ，到微信公共号完成车牌号页面...
			Map userMap = daService.getMap("select mobile from user_info_Tb where id =? ", new Object[]{uin});
			if(userMap!=null){
				request.setAttribute("mobile", userMap.get("mobile"));
				request.setAttribute("topage", "wantstop");
				request.setAttribute("openid", openid);
				logger.error(">>>到微信公共号完成车牌号页面....");
				return "addcar";
			}else 
				return "error";
		}
		//查找是否已存在订单
		Map coMap = daService.getMap("select id,cid,state,lng,lat from carstop_order_tb where uin =? and state<>9 order by id desc limit ?" , new Object[]{uin,1});
		if(coMap!=null){//有订单
			logger.error(">>>>>>carstop:已存在订单...."+coMap);
			Integer state = (Integer)coMap.get("state");
			if(state==0){//已有泊车订单，继续发泊车请求
				Double lng = Double.valueOf(coMap.get("lng")+"");
				Double lat = Double.valueOf(coMap.get("lat")+"");
				Long cid = (Long) coMap.get("cid");
				Long id = (Long) coMap.get("id");
				stopdetail(request, uin, cid, id,lng, lat);
				logger.error(">>>>>>carstop:已有泊车订单，继续发泊车请求...."+coMap);
				return "stopdetail";
			}else	if(state<8){//订单已接车，但没有完成，到当前订单页面
				Long id = (Long) coMap.get("id");
				logger.error(">>>>>>carstop:订单已接车，到当前订单页面"+coMap);
				return "currentorder_"+uin+"_"+id;//mapping.findForward(currentOrder(request,uin,id));
			}
		}
		
//		Map<String, String> ret = new HashMap<String, String>();
//		ret = getJssdkApiSign(request);
//		//jssdk权限验证参数
//			//request.setAttribute("appid","wx08c66cac888faa2a");//Constants.WXPUBLIC_APPID);
//		request.setAttribute("appid",Constants.WXPUBLIC_APPID);
//		request.setAttribute("nonceStr", ret.get("nonceStr"));
//		request.setAttribute("timestamp", ret.get("timestamp"));
//		request.setAttribute("signature", ret.get("signature"));

		request.setAttribute("uin", uin);
		return "carstops";
	}
	private String cancelOrder(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "id", -1L);
		int ret = 0;
		if(id!=-1){
			Map map =daService.getMap("select buid from carstop_order_tb where id =?", new Object[]{id});
			if(map!=null&&map.get("buid")!=null){
				ret =daService.update("update user_info_tb set online_flag=? where id = ?", new Object[]{23,(Long)map.get("buid")});
				logger.error(">>>>更新泊车员状态 ："+ret);
				ret = daService.update("update carstop_order_tb set state =? where id =? and state =?", new Object[]{9,id,1});
				logger.error(">>>>取消泊车员已响应泊车的订单 ："+ret);
			}else{
//				ret = daService.update("delete from carstop_order_tb where id =? and state =?", new Object[]{id,1});
				ret = daService.update("update carstop_order_tb set state =? where id =? and state =?", new Object[]{9,id,0});
				logger.error(">>>>后台管理员取消车主发出泊车请求的订单 ："+ret);
			}
		}
		return ret+"";
	}
/**
 * 微信支付
 * @param request
 * @return
 * @throws Exception
 */
	private String weixinpay(HttpServletRequest request) throws Exception {
		Long uid = RequestUtil.getLong(request, "uid", -1L);//收费员ID
		Long id = RequestUtil.getLong(request, "id", -1L);//收费员ID
		Double money = RequestUtil.getDouble(request, "total", 0d);//需要支付的停车费金额
		Long uin = RequestUtil.getLong(request, "uin",1L);
		Long ticketId = RequestUtil.getLong(request, "ticketid", -1L);//停车券
		String openid = "";
		Double ticket_money = RequestUtil.getDouble(request, "ticket_money", 0d);//停车券金额
		Map<String, Object> userMap = daService.getMap("select balance,wxp_openid from user_info_tb where id=? ",new Object[] {uin });
		if(userMap == null){
			return "error";
		}else {
			openid = (String)userMap.get("wxp_openid");
		}
		String addressip = request.getRemoteAddr();
		if(uid == -1){
			return "error";
		}
		Long time = System.currentTimeMillis()/1000;
		Long ticket_count = daService.getLong("select count(id) from ticket_tb where uin = ? and limit_day > ? and state=? ",
				new Object[]{uin,time,0});
		String ticket_description = "未选券";
		if(ticket_count>0&&ticket_money==0){
			ticket_description = "今日已超过使用次数";
		}
		if(ticket_money > 0){
			ticket_description = "已选择"+ticket_money.intValue()+"元券";
		}
		Double balance_pay = 0d;//余额支付的金额
		Double wx_pay = 0d;//微信支付的金额
		
		Double balance = Double.valueOf(userMap.get("balance") + "");//用户余额
		
		balance_pay = balance;//全部用余额支付
		DecimalFormat dFormat = new DecimalFormat("#.00");
		wx_pay = Double.valueOf(dFormat.format(money - ticket_money - balance));
		Map<String, Object> attachMap = new HashMap<String, Object>();
		attachMap.put("uid", uid);//收费员ID
		attachMap.put("money", money);//直付金额
		attachMap.put("mobile", userMap.get("mobile"));//车主手机号
		attachMap.put("type", 1);//直付停车费
		attachMap.put("ticketId", ticketId);//停车券
		attachMap.put("orderid", id);//停车券
		attachMap.put("uin", uin);//停车券
		//附加数据
		String attach = StringUtils.createJson(attachMap);
		//设置支付参数
		SortedMap<Object, Object> signParams = new TreeMap<Object, Object>();
		//获取JSAPI网页支付参数
		signParams = PayCommonUtil.getPayParams(addressip, wx_pay, "停车费支付", attach, openid);
		request.setAttribute("appid", signParams.get("appId"));
		request.setAttribute("nonceStr", signParams.get("nonceStr"));
		request.setAttribute("package", signParams.get("package"));
		request.setAttribute("packagevalue", signParams.get("package"));
		request.setAttribute("timestamp", signParams.get("timeStamp"));
		request.setAttribute("paySign", signParams.get("paySign"));
		request.setAttribute("signType", signParams.get("signType"));
		//其他参数
		request.setAttribute("wx_pay", wx_pay);//微信支付金额
		request.setAttribute("balance_pay", balance_pay);//余额支付金额
		request.setAttribute("ticket_description", ticket_description);//停车券支付金额
		request.setAttribute("money", money);//总支付金额
		request.setAttribute("ticketid", ticketId);//停车券ID
		request.setAttribute("ticket_count", ticket_count);//可用停车券个数
		request.setAttribute("openid", openid);
		request.setAttribute("uid", uid);//收费员id
		request.setAttribute("orderid", id);//收费员id
		return "topaypage";
	}
	

	private String topay(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "id", -1L);
		Double total = RequestUtil.getDouble(request, "total", 0d);
		Integer ptype = RequestUtil.getInteger(request, "ptype", 0);
		Long ticketId = RequestUtil.getLong(request, "ticketid", -1L);
		int ret = 0;
		String payType = "现金";
		boolean isPay = false;
		if(id!=-1){
			Map oMap = daService.getMap("select state from carstop_order_tb where id =?", new Object[]{id});
			if(oMap!=null){
				Integer state = (Integer)oMap.get("state");
				if(state==8){//已支付
					isPay=true;
					ret=1;
				}else {//去结算 
					if(ptype==0){//现金支付
						ret = daService.update("update carstop_order_tb set state=? ,pay_type=?, amount=? where id =? ", new Object[]{8,0,total,id});
					}else if(ptype==1){//余额支付 
						ret = publicMethods.payCarStopOrder(id,total,ticketId);
						if(ret==5){
							ret = daService.update("update carstop_order_tb set state=? ,pay_type=?, amount=? where id =? ", new Object[]{8,1,total,id});
						}
						payType = "余额";
					}
				}
			}
			
		}
		
		if(ret==1){
			Map<String, Object> cotMap = daService.getMap("select id,uin,btime,etime,amount,euid,car_number,amount from carstop_order_tb where id =? ", new Object[]{id});
			Long btime = (Long)cotMap.get("btime");
			Long etime = (Long)cotMap.get("etime");
			request.setAttribute("id", cotMap.get("id"));
			request.setAttribute("btime", TimeTools.getTime_yyyyMMdd_HHmmss(btime*1000));
			request.setAttribute("etime", TimeTools.getTime_yyyyMMdd_HHmmss(etime*1000));
			request.setAttribute("amount", cotMap.get("amount"));
			Long uid = (Long)cotMap.get("euid");
			String carNumber = (String)cotMap.get("car_number");
			//[您在XX泊车点支付成功,10.0元,泊车费,点击查看订单详情]
			if(!isPay){
				String message[] = new String[]{"泊车费支付成功",""+cotMap.get("amount"),"泊车费("+payType+")","点击查看订单详情"};
				String openid = "";
				Long uin =(Long)cotMap.get("uin");
				Map userMap = daService.getMap("select wxp_openid from user_info_Tb where id = ? ", new Object[]{uin});
				if(userMap!=null)
					openid = (String)userMap.get("wxp_openid");
				if(openid!=null&&openid.length()>10){
					logger.error(">>>支付成功，通过微信发消息给车主...");
					try {
						String url = "http://s.tingchebao.com/zld/attendant.do?action=currorder&uin="+uin+"&id="+id;
						Map<String, String> baseinfo = new HashMap<String, String>();
						List<Map<String, String>> orderinfo = new ArrayList<Map<String,String>>();
						String first = "泊车费支付成功";
						String remark = "点击查看订单详情";
						String remark_color = "#000000";
						baseinfo.put("url", url);
						baseinfo.put("openid", openid);
						baseinfo.put("top_color", "#000000");
						baseinfo.put("templeteid", Constants.WXPUBLIC_SUCCESS_NOTIFYMSG_ID);
						Map<String, String> keyword1 = new HashMap<String, String>();
						keyword1.put("keyword", "orderMoneySum");
						keyword1.put("value", cotMap.get("amount")+"元");
						keyword1.put("color", "#000000");
						orderinfo.add(keyword1);
						Map<String, String> keyword2 = new HashMap<String, String>();
						keyword2.put("keyword", "orderProductName");
						keyword2.put("value", "泊车费("+payType+")");
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
						e.printStackTrace();
					}
				}
				//订单红包
				Map bMap  = daService.getMap("select id,btime from order_ticket_tb where uin=? and ctime > ? order by id desc limit ?",
						new Object[]{uin,System.currentTimeMillis()/1000-5*60,1});
				if(bMap!=null){
					Long b_time = (Long)bMap.get("btime");
					if(b_time!=null&&b_time>10000){//已经分享过，不再分享
						bMap=null;
					}
				}
				Long bonusid = -1L;
				if(bMap!=null&&bMap.get("id")!=null){//有未分享的订单红包
					bonusid=(Long) bMap.get("id");
				}
				
				request.setAttribute("bonusid", bonusid);
				
				//微信公众号JSSDK授权验证
				Map<String, String> result = new HashMap<String, String>();
				try {
					result = publicMethods.getJssdkApiSign(request);
				}catch (Exception e) {
					e.printStackTrace();
				}
				//jssdk权限验证参数
				request.setAttribute("appid", Constants.WXPUBLIC_APPID);
				request.setAttribute("nonceStr", result.get("nonceStr"));
				request.setAttribute("timestamp", result.get("timestamp"));
				request.setAttribute("signature", result.get("signature"));
				//发消息给收费员
				logService.insertParkUserMessage(-1L,2,uid,carNumber,-1L,total, payType, 0,btime,etime, 0);
			}else {
				request.setAttribute("ispay", "您已支付过！");
			}
		}
		return "payresult";
	}

	private String currentOrder(HttpServletRequest request,Long uin,Long id) {
		if(uin==null){
			uin =RequestUtil.getLong(request, "uin", -1l);
			if(uin==-1)
				uin = getUinByOpenid(getOpenid(request));
		}
		
		if(id==null)
			id = RequestUtil.getLong(request, "id", -1L);
		logger.error("current order uin:"+uin+",orderid:"+id);
		Map cotMap = null;
		if(id!=-1){
			cotMap=	daService.getMap("select * from carstop_order_tb where id= ?", new Object[]{id});
		}else {
			cotMap=	daService.getMap("select * from carstop_order_tb where uin =? order by id desc ", new Object[]{uin});
		}
		String result = "{\"state\":\"-1\"}";
		Integer state =0;
		if(cotMap!=null){
			//状态，取车员头像，服务次数，驾龄，电话
			state = (Integer)cotMap.get("state");
			//状态:0车主泊车请求 1泊车员已响应泊车 2正在泊车  3泊车完成 4车主取车请求 5 泊车员已响应取车 6泊车员正在取车 7订单完成
			Map amMap =null;
			Long times = 0L;
			Double total = 0d;
			Long uid = null;
			Long btime  =(Long) cotMap.get("btime");
			
			if(state>0&&state<5){//查接车泊车员
				uid = (Long)cotMap.get("buid");
			}else if(state>1&&state>4){//查取车泊车员
				uid = (Long)cotMap.get("euid");
			}
			if(state==8){//订单已完成，查泊车点名称，订单结束时间
				Long cid = (Long)cotMap.get("cid");
				Long etime = (Long)cotMap.get("etime");
				Map<String, Object> ctMap = daService.getMap("select id,name ,address from car_stops_tb where id=?", new Object[]{cid});
				request.setAttribute("total", cotMap.get("amount"));
				request.setAttribute("btime", TimeTools.getTime_yyyyMMdd_HHmmss(btime*1000));
				request.setAttribute("etime", TimeTools.getTime_yyyyMMdd_HHmmss(etime*1000));
				request.setAttribute("name", ctMap.get("name"));
				request.setAttribute("address", ctMap.get("address"));
				request.setAttribute("id", ctMap.get("id"));
				request.setAttribute("dur",StringUtils.getTimeString(btime, etime));
				//订单红包
				Map bMap  = daService.getMap("select id,btime from order_ticket_tb where uin=? and order_id=? and ctime > ? order by id desc limit ?",
						new Object[]{uin,997L,TimeTools.getToDayBeginTime(),1});
				if(bMap!=null){
					Long b_time = (Long)bMap.get("btime");
					if(b_time!=null&&b_time>10000){//已经分享过，不再分享
						bMap=null;
					}
				}
				if(bMap!=null&&bMap.get("id")!=null){//有未分享的订单红包
					request.setAttribute("bonusid", bMap.get("id"));
				}
				result = "{\"state\":\"8\"}";
			}else {
				if(state>2&&state!=9){
					Long etime = (Long)cotMap.get("etime");
					if(etime==null)
						etime = System.currentTimeMillis()/1000;
					Long cid = (Long)cotMap.get("cid");
					total = getPrice(cid, btime, etime, uin,(Long)cotMap.get("start_time"));
				}
				if(uin!=null){
					amMap= daService.getMap("select u.nickname,u.mobile,p.pic_url,p.driver_year from user_info_tb u " +
							"left join user_pic_tb p on u.id=p.uin where u.id = ?", new Object[]{uid});
					times = daService.getLong("select count(id) from carstop_order_tb where buid=? or euid=? ", new Object[]{uid,uid});
				}
				if(amMap!=null){
					String stime = "";
					if(btime!=null)
						stime = TimeTools.getTime_MMdd_HHmm(btime*1000);
					result= "{\"state\":\""+state+"\",\"upic\":\""+amMap.get("pic_url")+"\"," +
							"\"times\":\""+times+"\",\"dyears\":\""+amMap.get("driver_year")+"\"," +
							"\"mobile\":\""+amMap.get("mobile")+"\",\"total\":\""+total+"\"," +
							"\"btime\":\""+stime+"\",\"opic\":\""+cotMap.get("pic")+"\",\"oilpic\":\""+cotMap.get("oilpic")+"\"," +
							"\"uname\":\""+amMap.get("nickname")+"\",\"id\":\""+cotMap.get("id")+"\"}";
					
				}
			}
		}
		logger.error(">>>>>>>>>result:"+result);
		request.setAttribute("uin", uin);
		request.setAttribute("data", result);
		return "currorder";
	}

	private void pay(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "id", -1L);
		Long uin = RequestUtil.getLong(request, "uin", -1L);
		Map cotMap = daService.getMap("select cid,start_time,btime,etime,euid from carstop_order_tb where id = ? ", new Object[]{id});
		if(cotMap!=null){
			Long btime = (Long)cotMap.get("btime");
			Long etime = (Long)cotMap.get("etime");
			Long cid = (Long)cotMap.get("cid");
			Double total = getPrice(cid, btime, etime, uin,(Long)cotMap.get("start_time"));
			request.setAttribute("btime", TimeTools.getTime_MMdd_HHmm(btime*1000));
			request.setAttribute("etime", TimeTools.getTime_MMdd_HHmm(etime*1000));
			request.setAttribute("total", total);
			request.setAttribute("uid", cotMap.get("euid"));
			request.setAttribute("id", id);
			request.setAttribute("uin", uin);
			Map userMap = daService.getMap("select balance from user_info_tb where id=?", new Object[]{uin});
			request.setAttribute("balance", userMap.get("balance"));
			Map ticketMap =null;
			if(memcacheUtils.readUseTicketCache(uin))
				ticketMap=publicMethods.useTickets(uin, total,null,Long.parseLong(cotMap.get("euid")+""),0);
			if(ticketMap!=null){
				request.setAttribute("ticket", ticketMap.get("money"));
				request.setAttribute("ticketid", ticketMap.get("id"));
			}else {
				request.setAttribute("ticket",0);
			}
		}
	}

	private String getCarState(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "id", -1L);
		if(id==-1)
			return "0";
		logger.error(">>>>>车主查泊车:"+id);
		//消息类型，0接车，1取车
		Map orderMap  = daService.getMap("select state from carstop_order_tb where id =?", new Object[]{id});
		Integer state =-1;
		if(orderMap!=null)
			state = (Integer)orderMap.get("state");
		//状态:0车主泊车请求 1泊车员已响应泊车 2正在泊车  3泊车完成 4车主取车请求 5 泊车员已响应取车 6泊车员正在取车 7等待支付 8支付成功
		logger.error(">>>>>车主查泊车:"+state);
		return state+"";
	}

	private String backcar(HttpServletRequest request) {
		//订单编号
		Long id = RequestUtil.getLong(request, "id", -1L);
		Map cotMap = daService.getMap("select c.car_number,c.cid,c.state,c.euid  from carstop_order_tb c where c.id =? ",  new Object[]{id});
		String carNumber = (String)cotMap.get("car_number");
		if(cotMap!=null){
			Integer state = (Integer)cotMap.get("state");
			if(state==4){//状态在泊车完成时，才能取车
				Long uid = (Long)cotMap.get("euid");
				if(uid!=null){
					
					List<String> uids =new ArrayList<String>();
					uids.add(uid+"_1");
					int r = sendAttendantMessage(carNumber,uids,id,6 );
					return "1";
				}
			}
		}
		//取泊车点编号，车主车牌号
		Long cid = (Long)cotMap.get("cid");
		
		//取泊车点的经纬度
		cotMap = daService.getMap("select longitude,latitude from car_stops_tb where id =? ", new Object[]{cid});
		Double lng = Double.valueOf(cotMap.get("longitude")+"");
		Double lat = Double.valueOf(cotMap.get("latitude")+"");
		
		//发消息级泊车员
		List<String> uids = (List<String>)getUids(lng, lat);
		String result = "0";
		if(uids!=null&&!uids.isEmpty()){
			int r = sendAttendantMessage(carNumber,uids,id,6 );
			if(r>0){//已发消息给泊车员,还车请求
				int ret = daService.update("update carstop_order_tb set state=?,end_time=? where id=? ", new Object[]{4,System.currentTimeMillis()/1000,id});
			}
			logger.error(">>>>>>>取车消息发给了"+r+"个泊车员："+uids);
			result= "1";	
		}
		return result;
	}  

	private String stopcar(HttpServletRequest request) {
		//车主经纬度
		Double lng = RequestUtil.getDouble(request, "lng", 0d);
		Double lat = RequestUtil.getDouble(request, "lat", 0d);
		//车主账户
		Long uin = RequestUtil.getLong(request, "uin", -1L);
		//距离
		//泊车点编号 
		Long id = RequestUtil.getLong(request, "id", -1L);
		//查车牌号
		String carNumber = publicMethods.getCarNumber(uin);
		
		//查找是否已存在订单
		Map coMap = daService.getMap("select id,state from carstop_order_tb where uin =? and state<? order by id desc limit ?" , new Object[]{uin,8,1});
		if(coMap!=null){//有订单
			logger.error(">>>>>carstop error ,stopcar ,已存在订单;"+coMap);
			//return coMap.get("id")+"";
			Long oid = (Long)coMap.get("id");
			List<String> uids = (List<String>)getUids(lng, lat);
			if(uids!=null&&!uids.isEmpty()){
				int r = sendAttendantMessage(carNumber,uids,oid,5 );
				logger.error(">>>>>>>泊车消息发给了"+r+"个泊车员："+uids); 
			}
			return oid+"";
		}
		//写订单表，发消息给泊车员
		Long nextId = RequestUtil.getLong(request, "orderid", -1L);
		int ret = 0;
		String result = "0";
		if(nextId==-1){
			nextId = daService.getkey("seq_carstop_order_tb");
			String sql = "insert into carstop_order_tb(id,cid,uin,car_number,start_time,state,lng,lat) values (?,?,?,?,?,?,?,?)";
			Object[] values = new Object[]{nextId,id,uin,carNumber,System.currentTimeMillis()/1000,0,lng,lat};
			ret = daService.update(sql, values);
		}else {//已生成过订单，不再生成 ，直接发消息给泊车员
			ret =1;
		}
		if(ret==1){//订单写入成功
			//发消息级泊车员
			Map carstopMap = daService.getMap("select longitude,latitude from car_stops_tb where id =?", new Object[]{id});
			//取泊车点的经纬度
			if(carstopMap!=null){
				lng = Double.valueOf(carstopMap.get("longitude")+"");
				lat = Double.valueOf(carstopMap.get("latitude")+"");
			}
			List<String> uids = (List<String>)getUids(lng, lat);
			if(uids!=null&&!uids.isEmpty()){
				int r = sendAttendantMessage(carNumber,uids,nextId,5 );
				logger.error(">>>>>>>泊车消息发给了"+r+"个泊车员："+uids);
				result= nextId+"";
				
			}else {
				result= "1";
			}
		}
		return result;
	}

	private void stopdetail(HttpServletRequest request,Long uin,Long cid,Long orderid,Double lng,Double lat) {
		if(uin==null){
			lng = RequestUtil.getDouble(request, "lng", 0d);
			lat = RequestUtil.getDouble(request, "lat", 0d);
			uin = RequestUtil.getLong(request, "uin", -1L);
			cid = RequestUtil.getLong(request, "id", -1L);
		}
		Map carStopMap = daService.getMap("select * from car_stops_tb where id=? ", new Object[]{cid});
		Double lng1 = Double.valueOf(carStopMap.get("longitude")+"");
		Double lat1 = Double.valueOf(carStopMap.get("latitude")+"");
		Double dis = StringUtils.distance(lng, lat, lng1, lat1);
		String pic =(String) carStopMap.get("pic");
		if(pic!=null){
			pic = pic.trim();
		}else {
			pic = "";
		}
		
		List<String> uids = (List<String>)getUids(lng1, lat1);
		if(uids!=null&&!uids.isEmpty())//有泊车员在线
			request.setAttribute("online", "1");
		else {
			request.setAttribute("online", "0");
		}
		
		Map cspriceMap = daService.getMap("select * from carstops_price_tb where cid=? order by ctime limit ?", new Object[]{cid,1});
		Integer ptype = 0;//泊车点价格类型    0临停 1常停
		Integer npunit = 60;
		if(cspriceMap!=null){
			ptype = (Integer) cspriceMap.get("type");
			npunit = (Integer)cspriceMap.get("next_unit");
		}
		Map coMap = daService.getMap("select max(start_time) ctime from carstop_order_tb where uin =?", new Object[]{uin});
		String isfirst = "0";//是否是首次停车
		String ismfirst = "0";//是否是本周首次停车
		String priceInfo = "";//价格说明，在客户手机中显示 
		if(ptype==0){//临停 
			Integer funit = (Integer)cspriceMap.get("first_unit");
			//Integer nfunit = (Integer)cspriceMap.get("next_unit");
			Integer favunit = (Integer)cspriceMap.get("fav_unit");
			if(npunit!=null&&npunit>60)
				priceInfo = cspriceMap.get("first_price")+"元首"+funit/60+"小时 ，" +
					"之后"+cspriceMap.get("next_price")+"元每"+npunit/60+"小时 ";
			else if(npunit==60){
				priceInfo = cspriceMap.get("first_price")+"元首小时 ，" +
						"之后"+cspriceMap.get("next_price")+"元每小时 ";
			}else{
				priceInfo = cspriceMap.get("first_price")+"元首小时 ，" +
						"之后"+cspriceMap.get("next_price")+"元每"+npunit+"分钟 ";
			}
			Long ctime = (Long)coMap.get("ctime");
			if(ctime==null){
				isfirst="1";
				request.setAttribute("favprice", cspriceMap.get("fav_price"));
				request.setAttribute("favunit",favunit/60);
			}
		}else if(ptype==1){//常停
			//Integer nfunit = (Integer)cspriceMap.get("next_unit"); 
			if(npunit!=null&&npunit>60)
				priceInfo = cspriceMap.get("next_price")+"元每"+npunit/60+"小时，" +
					cspriceMap.get("top_price")+"元每天";
			else if(npunit==60){
				priceInfo = cspriceMap.get("next_price")+"元每小时，" +
						cspriceMap.get("top_price")+"元每天";
			}else{
				priceInfo = cspriceMap.get("next_price")+"元每"+npunit+"分钟，" +
						cspriceMap.get("top_price")+"元每天";
			}
			if(coMap!=null){
				Long ctime = (Long)coMap.get("ctime");
				Long ntime = TimeTools.getWeekStartSeconds();
				if(ctime==null||ntime>ctime){
					ismfirst="1";
					request.setAttribute("favprice", cspriceMap.get("fav_price"));
				}
			}
		}
		request.setAttribute("priceInfo", priceInfo);
		request.setAttribute("isfirst", isfirst);
		request.setAttribute("ismfirst", ismfirst);
		request.setAttribute("distance", StringUtils.formatDouble(dis/1000).intValue());
		request.setAttribute("name", carStopMap.get("name"));
		request.setAttribute("pic", pic);
		request.setAttribute("id",cid);
		request.setAttribute("uin", uin);
		request.setAttribute("lng", lng);
		request.setAttribute("lat", lat);
		request.setAttribute("orderid", orderid);
		logger.error(">>>>>车主：lng:"+lng+",lat:"+lat+",lng1:"+lng1+",lat1:"+lat1+",dis:"+dis+",泊车员在线："+uids);
	}

	private String payorder(HttpServletRequest request) {
		//钱去哪里？//用券，返券，返现？
		//停车员的comid为0时是停车宝专职人员，其它为对应的停车场兼职人员，泊车费：专职写入停车宝账户，兼职写入对应的停车场账户
		Long uid = RequestUtil.getLong(request, "uid", -1L);
		Long id = RequestUtil.getLong(request, "id", -1L);
		logger.error("payorder:id:"+id+",uid:"+uid);
		int result = 0;
		if(id!=-1){//订单改为等待支付状态 
			//状态:0车主泊车请求 1泊车员已响应泊车 2正在泊车  3泊车完成 4车主取车请求 5 泊车员已响应取车 6泊车员正在取车 7等待支付 8支付成功
			result = daService.update("update carstop_order_tb set state=?,etime=? where id=? ",
					new Object[]{7,System.currentTimeMillis()/1000,id});
		}
		if(result==1){
			//泊车员置空闲状态    
			if(uid!=-1)
				daService.update("update user_info_tb set online_flag=? where id=? ", new Object[]{23,uid});
			else 
				daService.update("update user_info_tb set online_flag=? where id=(select euin from carstop_order_tb where id=?)", new Object[]{23,id});
		}
		return "{\"result\":\""+result+"\"}";
	}

	private String comlorder(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "id", -1L);
		Map cotMap = daService.getMap("select uin,cid,btime,etime,start_time from carstop_order_tb where id = ? ", new Object[]{id});
		String ret = "{}";
		if(cotMap!=null){
			Long btime = (Long)cotMap.get("btime");
			Long etime = System.currentTimeMillis()/1000;// (Long)cotMap.get("etime");
			Double total = getPrice((Long)cotMap.get("cid"), btime, etime, (Long)cotMap.get("uin"),(Long)cotMap.get("start_time"));
			String dur = StringUtils.getTimeString(btime, etime);
			ret="{\"btime\":\""+TimeTools.getTime_MMdd_HHmm(btime*1000)+"\"," +
					"\"etime\":\""+TimeTools.getTime_MMdd_HHmm(etime*1000)+"\"," +
					"\"dur\":\""+dur+"\",\"total\":\""+total+"\"}";
		}
		return ret;
	}

	private String getbackcar(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "id", -1L);
		Long uid = RequestUtil.getLong(request, "uid", -1L);
		int result = 0;
		if(id!=-1&&uid!=-1){
			result = daService.update("update carstop_order_tb set euid=? ,state=? where id=? and state=? ", new Object[]{uid,5,id,4});
		}
		logger.error(">>>id:"+id+",uid:"+uid);
		String ret ="{\"result\":\""+result+"\"}" ;
		if(result==1){//成功抢单
			//泊车员置还车中状态 
			if(uid!=-1)
			daService.update("update user_info_tb set online_flag=? where id=? ", new Object[]{25,uid});
		}
		logger.error(ret);
		sendCancelMessage(uid,id,6);
		return ret;
	}

	private String complstop(HttpServletRequest request) {
		Double lng = RequestUtil.getDouble(request, "lng", 0d);//车辆经纬度
		Double lat = RequestUtil.getDouble(request, "lat", 0d);
		String keyNo = RequestUtil.getString(request, "keyno");//车辆钥匙编号
		Long id = RequestUtil.getLong(request, "id", -1L);
		Long uid = RequestUtil.getLong(request, "uid", -1L);
		logger.error("id:"+id+",uid:"+uid);
		int ret = daService.update("update carstop_order_tb set lng=?,lat=?,keyno=?,btime=?  where id=? ", 
				new Object[]{lng,lat,keyNo,System.currentTimeMillis()/1000,id});
		return "{\"result\":\""+ret+"\"}" ;
	}

	private String uporderpic(HttpServletRequest request)throws Exception {
		Long id = RequestUtil.getLong(request, "id", -1L);
		Long uid =RequestUtil.getLong(request, "uid", -1L);
		Integer type = RequestUtil.getInteger(request, "type", 0);//0车辆照片，1油表照片
		logger.error(">>>>上传车辆泊车点照片....orderid:"+id+",uid:"+uid);
		String address = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "addr"));
		String picurl  =publicMethods.uploadPicToMongodb(request, uid, "carstop_pics");//uploadOrderPic2Mongodb(request, uid);
		logger.error(">>>>上传车辆泊车点照片....图片名称："+picurl);
		int ret =0;
		if(!"-1".equals(picurl)){
			if(type==0){
				ret = daService.update("update carstop_order_tb set pic=?,car_local=?,state=? where id =? ",
						new Object[]{picurl,address,3,id});
				if(ret==1&&uid!=-1){
					int r = daService.update("update user_info_tb set online_flag=? where id=? ", new Object[]{23,uid});
					logger.error(">>>更改泊车员状态 ："+r);
				}
			}else if(type==1){
				ret = daService.update("update carstop_order_tb set oilpic=? where id =? ",
						new Object[]{picurl,id});
			}
		}
		return "{\"result\":\""+ret+"\"}";
	}

	private String havegetcar(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "id", -1L);
		Long uid = RequestUtil.getLong(request, "uid", -1L);
		String carNumber = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "carnumber"));//车辆钥匙编号
		System.err.println(">>>>>car_umber:"+carNumber);
		Long count = daService.getLong("select count(id) from carstop_order_tb where id =? and state <> ?", new Object[]{id,9});
		int ret = 0;
		if(count==0){
			//daService.update("update user_info_tb set online_flag=? where id=(select buid from ", values)
			ret = -1;
		}else {
			if(carNumber!=null&&!"".equals(carNumber))
				ret = daService.update("update carstop_order_tb set state=?,car_number=? where id=? ", new Object[]{2,carNumber,id});
			else {
				ret = daService.update("update carstop_order_tb set state=? where id=? ", new Object[]{2,id});
			}
		}
		return "{\"result\":\""+ret+"\"}";
	}

	private String getstopcar(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "id", -1L);
		Long uid = RequestUtil.getLong(request, "uid", -1L);
		
//		Integer distance =RequestUtil.getInteger(request, "distance", 500);
		int result = 0;
		if(id!=-1&&uid!=-1){
			result = daService.update("update carstop_order_tb set buid=? ,state=? where id=? and state=? ", new Object[]{uid,1,id,0});
		}
		if(result==1&&uid!=-1){
			daService.update("update user_info_tb set online_flag=? where id=? ", new Object[]{24,uid});
			//处理消息，给其它已收到消息的泊车员发取消订单消息
			sendCancelMessage(uid,id,5);
		}
		String ret = "{\"result\":\""+result+"\"}";
		logger.error(">>>>>修改泊接车:ret:"+ret);
		return ret;
	}

	private void sendCancelMessage(Long uid, Long id,Integer state) {
		//所有泊车员的消息
		List<Map<String, Object>> mesgList = daService.getAll("select id,already_read,uin from order_message_tb" +
				" where orderid=? and state= ? ", new Object[]{id,state});
		if(mesgList!=null&&!mesgList.isEmpty()){
			List<String> isReadUid = new ArrayList<String>();
			for(Map<String, Object> map : mesgList){
				System.out.println(map+",uid:"+uid+"");
				Integer isRead = (Integer)map.get("already_read");
				Long uin = (Long)map.get("uin");
				if(isRead!=null&&isRead==1&&uin.intValue()!=uid.intValue()){//消息已发出去了,再发一条取消订单的消息
					isReadUid.add(uin+"_0");
				}
			}
			if(!isReadUid.isEmpty()){
				int ret = sendAttendantMessage("", isReadUid, id, 7);
				if(ret>0){
					logger.error(">>>泊车订单取消:"+isReadUid+",ret:"+ret);
					ret  = daService.update("update order_message_tb set already_read=? where orderid=? and state <? ", new Object[]{1,id,6});
				}
				logger.error(">>>泊车订单取消:更新消息状态,ret:"+ret);
			}
		}
	}

	private String editstate(HttpServletRequest request) {
		Long uin = RequestUtil.getLong(request, "uin", -1L);
		Long state = RequestUtil.getLong(request, "state", 0L);
		int ret = 0;
		logger.error(">>>>>修改泊车员状态:uin:"+uin+",state:"+state);
		if(uin!=-1&&state!=0){
			ret = daService.update("update user_info_tb set online_flag =? where id =? ", new Object[]{state,uin});
		}
		return ret+"";
	}

	private String getstate(HttpServletRequest request) {
		Long uin = RequestUtil.getLong(request, "uin", -1L);
		Long state = 0L;
		if(uin!=-1){
			Map userMap = daService.getMap("select online_flag from user_info_tb where id =? ", new Object[]{uin});
			if(userMap!=null)
				state=(Long)userMap.get("online_flag");
		}
		return state+"";
	}

	//查在位泊车员
	private List<String> getUids(Double lng1,Double lat1){
		List<Map<String, Object>> userLocalList  = daService.getAll("select uid,lat,lon,ctime from user_local_tb where ctime>? and uid in" +
				"(select id from user_info_tb where auth_flag=? and state=? and online_flag=? ) order by ctime desc",
				new Object[]{System.currentTimeMillis()/1000-10*60,13,0,23} );
		logger.error(">>>>>>泊车员:"+userLocalList);
		List<String> onlineUids = new ArrayList<String>();
		List<Long> uidsList = new ArrayList<Long>();
		if(userLocalList!=null&&!userLocalList.isEmpty()){
			for(Map<String, Object> map : userLocalList){//找到最近的停车员
				Double lng2 = Double.valueOf(map.get("lon")+"");
				Double lat2 = Double.valueOf(map.get("lat")+"");
				Double dist = StringUtils.distance(lng2, lat2, lng1, lat1);
				Long uid = (Long)map.get("uid");
				//System.out.println(dist);
				if(dist<4000){
					if(!uidsList.contains(uid)){
						uidsList.add(uid);
						onlineUids.add(uid+"_"+dist.intValue());
					}
				}
			}
		}
		logger.error(">>>>>>在岗泊车员:"+userLocalList);
		return onlineUids;
	}
	/**
	 * 
	 * @param carNumber 车牌号
	 * @param uidsList 收费员列表 
	 * @param id 订单编号 
	 * @param state 5车主停车 6车主还车 7车主取消 8自动接车 9自动还车
	 * @return 发送给收费员消息个数
	 */
	private int sendAttendantMessage(String carNumber,List<String> uidsList,Long id,Integer state){
		String sql ="insert into order_message_tb(car_number,uin,duartion,message_type,orderid,state) values(?,?,?,?,?,?)";
		List<Object[]> list = new ArrayList<Object[]>();
		if(uidsList!=null&&!uidsList.isEmpty()){
			for(String uid : uidsList){
				Long _uid = Long.valueOf(uid.split("_")[0]);
				String dist = uid.split("_")[1];
				Object[] va = new Object[]{carNumber,_uid,dist,3,id,state};
				list.add(va);
				String ret = "{\"mtype\":3,\"info\":{\"orderid\":\""+id+"\""+
						",\"carnumber\":\""+carNumber+"\",\"duration\":\""+dist+"\"," +
						"\"state\":\""+state+"\"}}";
				Map<Long, String> messCacheMap = memcacheUtils.doMapLongStringCache("parkuser_messages", null, null);
				if(messCacheMap==null)
					messCacheMap = new HashMap<Long, String>();
				messCacheMap.put(_uid, ret);
				logger.error(ret);
				memcacheUtils.doMapLongStringCache("parkuser_messages", messCacheMap, "update");
				logger.error(">>>>消息："+messCacheMap);
			}
		}
		return daService.bathInsert(sql, list, new int[]{12,4,12,4,4,4});
	}
	/**
	 * 上传订单照片
	 * @param request
	 * @param uin
	 * @return
	 * @throws Exception
	 */
/*	private String uploadOrderPic2Mongodb (HttpServletRequest request,Long uin) throws Exception{
		logger.error(">>>>>begin upload order picture....");
		Map<String, String> extMap = new HashMap<String, String>();
	    extMap.put(".jpg", "image/jpeg");
	    extMap.put(".jpeg", "image/jpeg");
	    extMap.put(".png", "image/png");
	    extMap.put(".gif", "image/gif");
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
			return "-1";
		}
		String filename = ""; // 上传文件保存到服务器的文件名
		InputStream is = null; // 当前上传文件的InputStream对象
		// 循环处理上传文件
		for (FileItem item : items){
			// 处理普通的表单域
			if (item.isFormField()){
				if(item.getFieldName().equals("comid")){
					if(!item.getString().equals(""))
						comId = item.getString("UTF-8");
				}
				
			}else if (item.getName() != null && !item.getName().equals("")){// 处理上传文件
				// 从客户端发送过来的上传文件路径中截取文件名
				logger.error(item.getName());
				filename = item.getName().substring(item.getName().lastIndexOf("\\")+1);
				is = item.getInputStream(); // 得到上传文件的InputStream对象
				
			}
		}
		String file_ext =filename.substring(filename.lastIndexOf(".")).toLowerCase();// 扩展名
		String picurl = uin+"_"+System.currentTimeMillis()+file_ext;
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
			  
		    DBCollection collection = mydb.getCollection("carstop_pics");
		  //  DBCollection collection = mydb.getCollection("records_test");
			  
			BasicDBObject document = new BasicDBObject();
			document.put("uin",  uin);
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
		    logger.error(">>>>泊车点上传图片完成 .....");
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
	    
		return picurl;
	}*/
	
	private String getOpenid(HttpServletRequest request){
		String code = RequestUtil.processParams(request, "code");
		String appid = Constants.WXPUBLIC_APPID;
		String secret=Constants.WXPUBLIC_SECRET;
		String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+appid+"&secret="+secret+"&code="+code+"&grant_type=authorization_code";
		String result = CommonUtil.httpsRequest(access_token_url, "GET", null);
		logger.error(">>>>>>>>code:"+code+",access_token_url:"+access_token_url+",result :"+result);
		String openid =null;
		if(result!=null){
			JSONObject map;
			try {
				map = new JSONObject(result);
				openid=map.getString("openid");//(String)map.get("openid");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return openid;
	}
	
	private Long getUinByOpenid(String openid){
		Map<String, Object> userMap = daService
				.getMap("select id from user_info_tb where state=? and auth_flag=? and wxp_openid=? ",
						new Object[] { 0, 4, openid });
		if(userMap!=null)
			return (Long)userMap.get("id");
		else 
			return null;
	}
	
	private String getMobileByOpenid(String openid){
		Map<String, Object> userMap = daService
				.getMap("select mobile from user_info_tb where state=? and auth_flag=? and wxp_openid=? ",
						new Object[] { 0, 4, openid });
		if(userMap!=null)
			return (String)userMap.get("mobile");
		else 
			return null;
	}
	
	private String	getDatas(Long id){
		//响应收费员请求 订单详情：接单时间，订单状态，车主位置，车牌号码，车主手机，订单编号
		Map cotMap= daService.getMap("select c.id,c.state,c.uin,c.car_number,u.mobile,c.lat,c.lng,c.keyno,c.car_local from carstop_order_tb c " +
				"left join user_info_tb u on c.uin=u.id where c.id =? ", new Object[]{id});
		Integer state = 0;
		String ret="{}";
		if(cotMap!=null)
			state = (Integer)cotMap.get("state");
		
		if(state==1){//已抢单
			ret= "{\"time\":\""+TimeTools.gettime1()+"\",\"state\":\"等待接车\"," +
					"\"lng\":\""+cotMap.get("lng")+"\",\"lat\":\""+cotMap.get("lat")+"\"," +
					"\"carnumber\":\""+cotMap.get("car_number")+"\",\"mobile\":\""+cotMap.get("mobile")+"\",\"id\":\""+id+"\"}";
			//泊车员置还车中状态 
		}else if(state==2){//已接车,正在泊车
			ret= "{\"time\":\""+TimeTools.gettime1()+"\",\"state\":\"接车途中\",\"keyno\":\""+cotMap.get("keyno")+"\"," +
					"\"carnumber\":\""+cotMap.get("car_number")+"\",\"mobile\":\""+cotMap.get("mobile")+"\",\"id\":\""+id+"\"}";
			
		}else if(state==5){//泊车员已响应取车
			ret ="{\"time\":\""+TimeTools.gettime1()+"\",\"state\":\"等待还车\",\"id\":\""+id+"\"," +
					"\"carnumber\":\""+cotMap.get("car_number")+"\",\"mobile\":\""+cotMap.get("mobile")+"\"," +
					"\"keyno\":\""+cotMap.get("keyno")+"\",\"carlocal\":\""+cotMap.get("car_local")+"\"}";
		}else if(state==6){//6泊车员正在取车
			ret ="{\"time\":\""+TimeTools.gettime1()+"\",\"state\":\"还车途中\",\"id\":\""+id+"\"," +
					"\"carnumber\":\""+cotMap.get("car_number")+"\",\"mobile\":\""+cotMap.get("mobile")+"\"}";
		
		}
		ret = ret.replace("null", "");
		logger.error(">>>状态："+state+",data:"+ret);
	//	http://192.168.199.240/zld/attendant.do?action=getdata&id=178
		return ret;
	}
	/**
	 * 计算价格
	 * @param cid
	 * @param start
	 * @param end
	 * @return
	 */
	private Double getPrice (Long cid,Long start,Long end,Long uin,Long create_time){
		Map cspriceMap = daService.getMap("select * from carstops_price_tb where cid=? order by ctime limit ?", new Object[]{cid,1});
		Integer ptype = 0;//泊车点价格类型    0临停 1常停
		Double price = 0d;
		Long dur = (end-start)/60;//时长，分钟
		if(dur==0)
			dur=1L;
		logger.error("时长:"+dur);
		if(cspriceMap!=null){
			ptype = (Integer) cspriceMap.get("type");
			//看看是不是符合优惠
			Map coMap = daService.getMap("select max(start_time) ctime from carstop_order_tb where uin =? and start_time !=?", new Object[]{uin,create_time});
			boolean isfirst = false;//是否是首次停车
			boolean ismfirst = false;//是否是本周首次停车
			if(ptype==0){//临停 
				Long ctime = (Long)coMap.get("ctime");
				if(ctime==null)
					isfirst=true;
			}else if(ptype==1){//常停
				if(coMap!=null){
					Long ctime = (Long)coMap.get("ctime");
					Long ntime = TimeTools.getWeekStartSeconds();
					if(ctime==null||ntime>ctime)
						ismfirst=true;
				}
			}
			
			if(ptype==0){//临停    XX元首三小时，超出后X元一小时  首次停车X元首X小时，超出后X元一小时
				Double first_price = Double.valueOf(cspriceMap.get("first_price")+"");
				Double next_price = Double.valueOf(cspriceMap.get("next_price")+"");
				Double fav_price = Double.valueOf(cspriceMap.get("fav_price")+"");
				Integer first_unit =(Integer)cspriceMap.get("first_unit");
				Integer next_unit =(Integer)cspriceMap.get("next_unit");
				Integer fav_unit =(Integer)cspriceMap.get("fav_unit");
				if(isfirst){//是第一次停车，按优惠价格走
					first_price = fav_price;
					first_unit = fav_unit;
				}
				if(dur>first_unit){
					dur  = dur-first_unit;
					if(dur%next_unit!=0)
						price =first_price+(dur/next_unit)*next_price +next_price;
					else {
						price =first_price+(dur/next_unit)*next_price;
					}
				}else {
					price=first_price;
				}
			}else if(ptype==1){//X元每小时，XX元包天  每周首单X元钱
				Double next_price = Double.valueOf(cspriceMap.get("next_price")+"");
				Double top_price = Double.valueOf(cspriceMap.get("top_price")+"");//最高价
				Double fav_price = Double.valueOf(cspriceMap.get("fav_price")+"");//优惠价
				Integer next_unit =(Integer)cspriceMap.get("next_unit");
			
				if(dur%next_unit!=0)
					price =(dur/next_unit)*next_price +next_price;
				else {
					price =(dur/next_unit)*next_price;
				}
				
				if(ismfirst){
					if(fav_price>0&&fav_price<price)//如果优惠价大于当前价格，取当前价
						price=fav_price;
				}
				if(price>top_price&&top_price>0)//如果价格大于最高价，取最高价
					price=top_price;
			}
		}
		return price;
	}
	
}