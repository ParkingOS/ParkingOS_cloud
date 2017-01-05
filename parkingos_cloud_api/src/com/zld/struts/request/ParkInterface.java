package com.zld.struts.request;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
/**
 * 车主2.0接口
 * @author Administrator
 * 20150415
 */
public class ParkInterface extends Action {
	
	@Autowired
	private PgOnlyReadService onlyService;
	@Autowired
	private DataBaseService service;
	
	private Logger logger = Logger.getLogger(ParkInterface.class);

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		if(action.equals("parks")){
			String ret = getParks(request);
			logger.error(ret);
			AjaxUtil.ajaxOutput(response, ret);
			//	http://118.192.85.142:8080/zld/parkinter.do?action=parks&lng=116.322747&lat=39.989056
			
		}else if(action.equals("parkdetail")){//读取评价
			String result = getDetail(request);
			//读取停车场详情   http://127.0.0.1/zld/parkinter.do?action=parkdetail&comid=3
			AjaxUtil.ajaxOutput(response,result);
		}else if(action.equals("getcomment")){
			//读取停车场评论   http://127.0.0.1/zld/parkinter.do?action=getcomment&comid=1197&page=2
			AjaxUtil.ajaxOutput(response, getComments(request));
		}else if(action.equals("dici")){//地磁
			//http://127.0.0.1/zld/parkinter.do?action=dici&dicino=1&state=1
			Long dicino = RequestUtil.getLong(request, "dicino",-1L);
			Integer state = RequestUtil.getInteger(request, "state",-1);
			String dici = RequestUtil.getString(request, "dici");
			System.err.println("dici test:>>>>>"+dici);
			if(!dici.equals("")){//数据格式：*205,$134,02,01,0 
				
				String []info = dici.split(",");
				if(info.length>4){
					for(String in: info){
						System.err.println("dici test:>>>>>"+"info:"+in);
					}
					String serId = info[1].substring(1);//主机ID
					String did = info[3];//传感器ID
					String s = info[4].substring(0,1);//车位状态 0无车 1有车
					Map diciMap = service.getMap("select id from dici_tb where serid=? and did=? and is_delete=? ", 
							new Object[]{serId, did, 0});
					dicino=(Long)diciMap.get("id");
					state=Integer.valueOf(s);
				}else {
					AjaxUtil.ajaxOutput(response, dici);
				}
			}
			System.err.println("dici test:>>>>>"+"no:"+dicino+",state:"+state);
//			Map map = request.getParameterMap();
//			System.err.println(map);
			int ret =0;
			ret = service.update("update dici_tb set state=? where id =? ", new Object[]{state,dicino});
			System.err.println("dici test:>>>>>"+"no:"+dicino+",state:"+state+",ret:"+ret);
			if(ret==1){
				Map parkMap = onlyService.getMap("select * from com_park_tb where dici_id=?", new Object[]{dicino});
				if(state==1){//有车入场
					Map uidMap = service.getMap("select id from user_info_tb where comid=? ", new Object[]{parkMap.get("comid")});
					Long key = service.getkey("seq_order_tb");
					ret = service.update("insert into order_tb (id,create_time,comid,uin,c_type,car_number,state,uid) values(?,?,?,?,?,?,?,?)", 
							new Object[]{key,System.currentTimeMillis()/1000,parkMap.get("comid"),-1L,6,"",0,uidMap.get("id")});
					ret = service.update("update com_park_tb set order_id=? ,state=? where dici_id=?", new Object[]{key,1,dicino});
					logger.error("更新泊位上的订单："+ret+",dici_id："+dicino+",orderid:"+key);
				}else if(state==0){//车场出场
					ret = service.update("update order_tb set end_time=? where id=? ", 
							new Object[]{System.currentTimeMillis()/1000,parkMap.get("order_id")});
				}
			}
			AjaxUtil.ajaxOutput(response, ret+"");
		}else if(action.equals("getdici")){
			//http://127.0.0.1/zld/parkinter.do?action=getdici&comid=10
			Long comId = RequestUtil.getLong(request, "comid", -1L);
			List<Map<String, Object>> list = service.getAll("select c.cid,c.dici_id did,d.state " +
					"from com_park_tb c left join dici_tb d on c.dici_id = d.id where c.comid=? order by c.id", new Object[]{comId});
			System.err.println(list);
			if(list!=null&&!list.isEmpty()){
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(list));
			}else {
				AjaxUtil.ajaxOutput(response, "{}");
			}
			return null;
		}else if(action.equals("uploadcamerastate")){
			//上传摄像头状态   http://127.0.0.1/zld/parkinter.do?action=uploadcamerastate&cameraid=3&state=0
			String result = uploadcamerastate(request);
			AjaxUtil.ajaxOutput(response,result);
		}else if(action.equals("uploadbrakestate")){
			//上传摄像头状态   http://127.0.0.1/zld/parkinter.do?action=uploadcamerastate&cameraid=3&state=0
			String result = uploadbrakestate(request);
			AjaxUtil.ajaxOutput(response,result);
		}else if(action.equals("uploadledstate")){
			//上传摄像头状态   http://127.0.0.1/zld/parkinter.do?action=uploadcamerastate&cameraid=3&state=0
			String result = uploadledstate(request);
			AjaxUtil.ajaxOutput(response,result);
		} 
		return null;
	}
	private String uploadledstate(HttpServletRequest request) {
		Long ledid = RequestUtil.getLong(request, "ledid", -1L);
		Long state= RequestUtil.getLong(request, "state", -1L);//
		int res = 0;
		if(state!=-1&&ledid!=-1){
			res = service.update("update  com_led_tb set state=?,upload_time=? where id=? ", new Object[]{state,System.currentTimeMillis()/1000,ledid});
		}
		logger.error("uploadledstate ledid"+ledid+",state:"+state+",res:"+res);
		return res+"";
	}
	private String uploadbrakestate(HttpServletRequest request) {
		Long passid = RequestUtil.getLong(request, "passid", -1L);
//		Long carmera_id = RequestUtil.getLong(request, "cameraid", -1L);
		Long state= RequestUtil.getLong(request, "state", -2L)+1;//状态是0(故障)和1（正常）
		int res = 0;
		if(state!=-1&&passid!=-1){
			res = service.update("update  com_brake_tb set state=?,upload_time=? where passid=? ", new Object[]{state,System.currentTimeMillis()/1000,passid});
			if(res==0){
				res = service.update("insert into com_brake_tb(passid,state,upload_time) values (?,?,?) ", new Object[]{passid,state,System.currentTimeMillis()/1000});
			}
		}
		logger.error("uploadbrakestate passid"+passid+",state:"+state+",res:"+res);
		return res+"";
	}
	private String uploadcamerastate(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "cameraid",-1L);
		Long state= RequestUtil.getLong(request, "state", -1L);
		int res = 0;
		if(state!=-1&&id!=-1){
			res = service.update("update com_camera_tb set state = ? ,upload_time = ? where id=?", new Object[]{state,System.currentTimeMillis()/1000,id});
		}
		logger.error("uploadcamerastate id:"+id+",state:"+state+",res:"+res);
		return res+"";
	}
	private String getComments(HttpServletRequest request) {
		Integer page = RequestUtil.getInteger(request, "page", 1);
		Long comId= RequestUtil.getLong(request, "comid", -1L);
		List<Map<String, Object>> comList =onlyService.getPage("select * from com_comment_tb where comid=? order by id desc",
				new Object[]{comId},page,20);
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
				iMap.put("week", "星期"+StringUtils.getWeek(calendar.get(Calendar.DAY_OF_WEEK)));//评价日期是星期几：星期四
				iMap.put("time", times.substring(6));// 评价的车场ID
				iMap.put("info",  map.get("comment"));//评价内容：巴拉巴拉一大串废话。。。
				iMap.put("user", getCarNumber(uid));// 评价者：车主（车牌号：京A***A111）
				resultMap.add(iMap);
			}
			return StringUtils.createJson(resultMap);
		}
		return "[]";
	}
	private String getDetail(HttpServletRequest request) {
		Long pid = RequestUtil.getLong(request, "comid", -1L);
		logger.error("comid:"+pid);
		String result = "{}";
		if(pid!=null&&pid>0){
			Map<String,Object> comMap = onlyService.getMap("select id,longitude lng,latitude lat,epay,company_name as name,mobile phone," +
					"parking_total as total,address addr,remarks as desc " +
					"from com_info_tb where id =?", new Object[]{pid});
			
			//查图片
			Map<String,Object> picMap = onlyService.getMap("select picurl from com_picturs_tb where comid=? order by id desc limit ?",
					new Object[]{pid,1});
			String picUrls = "";
			if(picMap!=null&&!picMap.isEmpty()){
				picUrls=(String)picMap.get("picurl");
			}
			//查空闲车位数
			Integer total = (Integer)comMap.get("total");
			Long orderCount = onlyService.getLong("select count(id) from order_tb where comid=? and state=? ", new Object[]{pid,0}) ;
			total = total-orderCount.intValue();
			if(total<0)
				total =0;
			//查价格
			String price = getPrice(pid);
			
			comMap.put("free", total);
			comMap.put("price", price);
			comMap.put("photo_url", "[\""+picUrls+"\"]");
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

	private String getParks(HttpServletRequest request) {
		Double lon = RequestUtil.getDouble(request, "lng", 0d);
		Double lat = RequestUtil.getDouble(request, "lat", 0d);
		double lon1 = 0.023482756;
		double lat1 = 0.017978752;
		String sql = "select id,company_name as name,longitude lng,latitude lat,parking_total total,share_number," +
				"address addr,phone,monthlypay,epay,type,isfixed from com_info_tb where longitude between ? and ? " +
				"and latitude between ? and ? and state=? and isview=? ";//and isfixed=? ";
		List<Object> params = new ArrayList<Object>();
		params.add(lon-lon1);
		params.add(lon+lon1);
		params.add(lat-lat1);
		params.add(lat+lat1);
		params.add(0);
		params.add(1);
		List<Map<String, Object>> list = null;//daService.getPage(sql, null, 1, 20);
		list = onlyService.getAll(sql, params, 0, 0);
		Map<Long, Integer> shareNumMap = new HashMap<Long, Integer>();
		String preIds = "";
		List<Long> pids = new ArrayList<Long>();
		if(list!=null&&list.size()>0){
			for(Map<String, Object> map :list){
				String cname = (String)map.get("name");
				if(cname!=null){
					cname = cname.replace("\r", "").replace("\n", "").replace("\"", "").replace("“", "");
					map.put("name", cname);
				}
				Integer type = (Integer)map.get("type");
				pids.add((Long)map.get("id"));
				preIds +="?,";
				shareNumMap.put((Long)map.get("id"), (Integer)map.get("share_number"));
				//查询价格
				//Integer type = (Integer)map.get("type");
				if(type==0)//收费，查价格
					map.put("price", getPrice((Long)map.get("id")));
				else {//免费，返回-1
					map.put("price", "-1");
				}
			}
		}
		//查询空闲车位
		if(!shareNumMap.isEmpty()){
			params.clear();
			if(preIds.endsWith(","))
				preIds = preIds.substring(0,preIds.length()-1);
			params.add(0);
			params.addAll(pids);
			List<Map<String, Object>> busyMaps = onlyService.getAllMap("select count(ID) count,comid from order_tb " +
					"where state=? and comid in("+preIds+") group by comid", params);
			if(busyMaps!=null){
				for(Map<String, Object> bmap : busyMaps){
					Long comId = (Long)bmap.get("comid");
					Long count = (Long)bmap.get("count");
					Long scount = (shareNumMap.get(comId)-count);
					shareNumMap.put(comId,scount.intValue());
				}
			}
			for(Long comidLong : shareNumMap.keySet()){
				for(Map<String, Object> map :list){
					Long cid = (Long)map.get("id");
					if(comidLong.intValue()==cid.intValue()){
						map.put("free", shareNumMap.get(comidLong));
					}
				}
			}
		}
		String result = StringUtils.createJson(list);
		return result;
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
		List<Map<String,Object>> priceList=onlyService.getAll("select * from price_tb where comid=? " +
				"and state=? and pay_type=? order by id desc", new Object[]{parkId,0,0});
		if(priceList==null||priceList.size()==0){//没有按时段策略
			//查按次策略
			priceList=onlyService.getAll("select * from price_tb where comid=? " +
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
			}
			//发短信给管理员，通过设置好价格
		}else {//从按时段价格策略中分拣出日间和夜间收费策略
			if(priceList.size()>0){
				//logger.error(priceList);
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
	/**
	 * 查车牌号
	 * @param uin
	 * @return
	 */
	public String getCarNumber(Long uin){
		String carNumber="车牌号未知";//车主车牌号
		Map carNuberMap = onlyService.getPojo("select car_number from car_info_tb where uin=? and state=?  ", 
				new Object[]{uin,1});
		if(carNuberMap!=null&&carNuberMap.get("car_number")!=null&&!carNuberMap.get("car_number").toString().equals(""))
			carNumber = (String)carNuberMap.get("car_number");
		return carNumber;
	}	
}
