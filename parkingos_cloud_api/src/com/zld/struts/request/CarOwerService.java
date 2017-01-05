package com.zld.struts.request;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.AjaxUtil;
import com.zld.impl.PublicMethods;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.Check;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;

public class CarOwerService extends Action{
	
	@Autowired
	private PgOnlyReadService daService;
	@Autowired
	private PublicMethods publicMethods;
	private Logger logger = Logger.getLogger(CarOwerService.class);

	
	
	/**
	 *车主登录从服务器取数据
	 *http://www.tingchebao.com/zld/carservice.do?action=getparking&begintime=1401451200
	 *http://s.zhenlaidian.com/zld/carservice.do?action=getparkshare&ids=1,2
	 *http://192.168.199.240/zld/carservice.do?action=getparkshare&ids=3,10
	 *http://192.168.199.240/zld/carservice.do?action=getparking&begintime=1404514000
	 */
	
	@SuppressWarnings({ "rawtypes"})
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		logger.error("action="+action);
		
		if(action.equals("")){//收费员推荐车主，通过红包领取来注册
			String pid = RequestUtil.getString(request, "pid");
			response.sendRedirect("carowner.do?action=gethbonus&id=999&uid="+pid);
			return null;
		}
		if(action.equals("getmesg")){//取个推消息
			//http://192.168.199.240/zld/carservice.do?action=getmesg&mesgid=168
			Long mesgId = RequestUtil.getLong(request, "mesgid", -1L);
			String message = "";
			if(mesgId!=-1){
				Map mesgMap = daService.getMap("select content from message_tb where id=? ", new Object[]{mesgId});
				if(mesgMap!=null&&mesgMap.get("content")!=null)
					message = (String)mesgMap.get("content");
			}
			AjaxUtil.ajaxOutput(response, message);
			return null;
		}else if(action.equals("getparking")){//取停车场数据，登录后加载，传入时间，初始登录没有时间
			Long time = RequestUtil.getLong(request, "begintime",0L);
			if(time==1422254651){
				AjaxUtil.ajaxOutput(response, "{}");
				return null;
			}
			String sql ="select id,company_name" +
					",type,parking_total total" +
					",state" +
					",update_time" +
					",epay" +
					",monthlypay" +
					",isview" +
					",longitude,latitude from com_info_Tb ";
			List<Object> values = new ArrayList<Object>();
			if(time==0){
				sql +=" where state=? and type=? ";
				values.add(0);
				values.add(0);
			}else{
				sql+="  where type=? and update_time>? ";
				values.add(0);
				values.add(time);
			}
			List<Map<String,Object>> comsMapList = daService.getAllMap(sql,values);
			//处理isview=0(不在地图上显示),如果isview=0,修改state=2(未审核)
			if(comsMapList!=null&&!comsMapList.isEmpty()){
				for(Map<String,Object> map : comsMapList){
					Integer isview = (Integer)map.get("isview");
					String cname = (String)map.get("company_name");
					if(cname!=null){
							cname = cname.replace("\r", "").replace("\n", "").replace("\"", "").replace("“", "");
						map.put("company_name", cname);
					}
					if(isview==0)
						map.put("state", 2);
				}
			}
			
			String result = StringUtils.createJson(comsMapList);// StringUtils.createXML(comsMapList);
			result=result.replace("null", "-");
			AjaxUtil.ajaxOutput(response, result);
			//http://192.168.199.240/zld/carservice.do?action=getparking&begintime=1429632000

		}else if(action.equals("getepaypark")){//查询可支付（有收费员在位的停车场）
			List list = daService.getAll("select id from com_info_Tb where "+//is_hasparker=? " +
					"  state=? and epay=? and isfixed=?  ", new Object[]{0,1,1});
			String ret = "[]";
			if(list!=null&&!list.isEmpty()){
				ret = "[";
				for(int i=0;i<list.size();i++){
					Map map = (Map)list.get(i);
					if(i!=0)
						ret+=",";
					ret +=map.get("id")+"";
				}
				ret +="]";
			}
			AjaxUtil.ajaxOutput(response, ret);
			//http://192.168.199.240/zld/carservice.do?action=getepaypark
			return null;
		}else if(action.equals("getsharebyll")){//for iphone苹果端可以传入左下角和右上角的经纬度。
			logger.error("getsharebyll:begin....");
			List<Map<String, Object>> maps = new ArrayList<Map<String,Object>>();
			Double lon = RequestUtil.getDouble(request, "lblon", 0d);
			Double lat = RequestUtil.getDouble(request, "lblat", 0d);
			Double lon1 = RequestUtil.getDouble(request, "rtlon", 0d);
			Double lat1 = RequestUtil.getDouble(request, "rtlat", 0d);
			List<Map<String, Object>> comMaps = null;//daService.getPage(sql, null, 1, 20);
			if(lon!=0&&lat!=0){//传入了经纬度时，查附近500内的停车场
				String sql = "select id,share_number,parking_total from com_info_tb where type=? and  longitude between ? and ? " +
						"and latitude between ? and ? and state=? ";
				List<Object> params = new ArrayList<Object>();
				params.add(0);
				params.add(lon);
				params.add(lon1);
				params.add(lat);
				params.add(lat1);
				params.add(0);
				comMaps = daService.getAll(sql, params, 0, 0);
			}
			Map<Long,Integer> comidNumber = new HashMap<Long, Integer>();
			//String params = "";
			List<Object> valusList = new ArrayList<Object>();
			if(comMaps!=null&&!comMaps.isEmpty()){
				for(Map map : comMaps){
					try {
						comidNumber.put((Long)map.get("id"), (Integer)map.get("share_number"));
//						if(params.equals(""))
//							params ="?";
//						else {
//							params +=",?";
//						}
//						valusList.add(map.get("id"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				valusList.add(0,0);
				List<Map<String, Object>> comOrderMaps = daService.getAllMap("select count(*) count,comid  from order_tb where state=?  group by comid",valusList);
				if(comOrderMaps!=null){
					for(Map map : comOrderMaps){
						Long comId = (Long) map.get("comid");
						Integer shared = Integer.valueOf(map.get("count")+"");
						if(comidNumber.containsKey(comId)){
							Integer count = comidNumber.get(comId);
							if(count>=shared)
								comidNumber.put(comId, count-shared);
							else
								comidNumber.put(comId, 0);
						}
					}
				}
				if(comidNumber.size()>0){
					for(Long key :comidNumber.keySet()){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("id", key);
						map.put("number", comidNumber.get(key));
						maps.add(map);
					}
				}
			}
			String result = StringUtils.createJson(maps);
			logger.error("getsharebyll:over....");
			AjaxUtil.ajaxOutput(response, result);
			//http://192.168.199.240/zld/carservice.do?action=getsharebyll&lblon=116.318512&lblat=40.042214&rtlon=116.328512&rtlat=40.043214
		}else if(action.equals("getprices")){//
			Double lon = RequestUtil.getDouble(request, "lblon", 0d);
			Double lat = RequestUtil.getDouble(request, "lblat", 0d);
			Double lon1 = RequestUtil.getDouble(request, "rtlon", 0d);
			Double lat1 = RequestUtil.getDouble(request, "rtlat", 0d);
			Integer dur = RequestUtil.getInteger(request, "time", 0);
			Integer car_type = RequestUtil.getInteger(request, "car_type", 0);//0：通用，1：小车，2：大车
			if(lon1==0||lat1==0){
				lon1=lon+0.008036;
				lon =lon-0.008036;
				lat1 = lat +0.005032;
				lat = lat - 0.005032;
			}
//			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
//			//开始小时
//			int bhour = calendar.get(Calendar.HOUR_OF_DAY);
			String result = "[{}]";
			List<Map<String, Object>> comList = daService.getAll(" select id from com_info_tb" +
					" where type=? and  longitude between ? and ? and latitude between ? and ? and state=? ", 
					new Object[]{0,lon,lon1,lat,lat1,0});
			if(comList!=null){
				result="[";
				for(int i=0;i<comList.size();i++){
					Map map = comList.get(i);
					Long comid = (Long) map.get("id");
					/*Map priceMap = publicMethods.getPriceMap(comid);
					String _price = "0";
					if(priceMap!=null){
						int pay_type = (Integer)priceMap.get("pay_type");
						Double price = Double.valueOf(priceMap.get("price")+"");
						_price = price+"";
						if(pay_type==0){
							Integer unit = (Integer)priceMap.get("unit");
							_price = StringUtils.formatDouble((Double.valueOf(60)/Double.valueOf(unit))*price)+"";
						}
					}*/
					Long btime = System.currentTimeMillis()/1000;
					String _price = publicMethods.getPrice(btime, btime+(dur*60), comid,car_type);
					if(i!=0)
						result +=",";
					result += "{\"id\":\""+comid+"\",\"price\":\""+_price+"\"}";
				}
				result +="]";
			}
			AjaxUtil.ajaxOutput(response, result);
			return null;
			//http://192.168.199.240/zld/carservice.do?action=getprices&lblon=116.318512&lblat=40.042214&rtlon=&rtlat=&time=120
			
		}
		/*else if(action.equals("getprices")){//
			Double lon = RequestUtil.getDouble(request, "lblon", 0d);
			Double lat = RequestUtil.getDouble(request, "lblat", 0d);
			Double lon1 = RequestUtil.getDouble(request, "rtlon", 0d);
			Double lat1 = RequestUtil.getDouble(request, "rtlat", 0d);
			if(lon1==0||lat1==0){
				lon1=lon+0.008036;
				lon =lon-0.008036;
				lat1 = lat +0.005032;
				lat = lat - 0.005032;
			}
			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
			//开始小时
			int bhour = calendar.get(Calendar.HOUR_OF_DAY);
			String result = "[{}]";
			List<Map> comList = daService.getAll(" select id from com_info_tb" +
					" where  longitude between ? and ? and latitude between ? and ? and state=? ", 
					new Object[]{lon,lon1,lat,lat1,0});
			if(comList!=null){
				result="[";
				for(int i=0;i<comList.size();i++){
					Map map = comList.get(i);
					Long comid = (Long) map.get("id");
					Map priceMap = publicMethods.getPriceMap(comid);
					String _price = "0";
					if(priceMap!=null){
						int pay_type = (Integer)priceMap.get("pay_type");
						Double price = Double.valueOf(priceMap.get("price")+"");
						_price = price+"";
						if(pay_type==0){
							Integer unit = (Integer)priceMap.get("unit");
							_price = StringUtils.formatDouble((Double.valueOf(60)/Double.valueOf(unit))*price)+"";
						}
					}
					if(i!=0)
						result +=",";
					result += "{\"id\":\""+comid+"\",\"price\":\""+_price+"\"}";
				}
				result +="]";
			}
			AjaxUtil.ajaxOutput(response, result);
			return null;
			//http://192.168.199.240/zld/carservice.do?action=getprices&lblon=116.318512&lblat=40.042214&rtlon=&rtlat=
			
		}*/
		else if(action.equals("getshares")){//查询停车场空闲车位数,----新接口for android ,只能传入地图中心的经纬度
			//http://192.168.199.209/zld/carservice.do?action=getshares&lon=116.318512&lat=40.042214
			List<Map<String, Object>> maps = new ArrayList<Map<String,Object>>();
			Double lon = RequestUtil.getDouble(request, "lon", 0d);
			Double lat = RequestUtil.getDouble(request, "lat", 0d);
			//500米经纬度偏移量
//			double lon1 = 0.009536;
//			double lat1 = 0.007232; 
			List<Map<String, Object>> comMaps = null;//daService.getPage(sql, null, 1, 20);
			if(lon!=0&&lat!=0){//传入了经纬度时，查附近500内的停车场
				double lon1 = 0.008036;
				double lat1 = 0.005032; 
				String sql = "select id,share_number,parking_total from com_info_tb where  longitude between ? and ? and latitude between ? and ? and state=?";
				List<Object> params = new ArrayList<Object>();
				params.add(lon-lon1);
				params.add(lon+lon1);
				params.add(lat-lat1);
				params.add(lat+lat1);
				params.add(0);
				comMaps = daService.getAll(sql, params, 0, 0);
			}else {//查2分钟内更新的停车场
				Long time = System.currentTimeMillis()/1000-2*60;
				comMaps= daService.getAll("select id,share_number,parking_total from com_info_tb where type=? and update_time>?",new Object[]{0,time});
			}
			
			Map<Long,Integer> comidNumber = new HashMap<Long, Integer>();
			//String params = "";
			List<Object> valusList = new ArrayList<Object>();
			if(comMaps!=null&&!comMaps.isEmpty()){
				for(Map map : comMaps){
					try {
						comidNumber.put((Long)map.get("id"), (Integer)map.get("share_number"));
//						if(params.equals(""))
//							params ="?";
//						else {
//							params +=",?";
//						}
//						valusList.add(map.get("id"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				valusList.add(0,0);
				List<Map<String, Object>> comOrderMaps = daService.getAllMap("select count(*) count,comid  from order_tb where state=? group by comid",valusList);
				if(comOrderMaps!=null){
					for(Map map : comOrderMaps){
						Long comId = (Long) map.get("comid");
						Integer shared = Integer.valueOf(map.get("count")+"");
						if(comidNumber.containsKey(comId)){
							Integer count = comidNumber.get(comId);
							if(count>=shared)
								comidNumber.put(comId, count-shared);
							else {
								comidNumber.put(comId,0);
							}
						}
					}
				}
				if(comidNumber.size()>0){
					for(Long key :comidNumber.keySet()){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("id", key);
						map.put("number", comidNumber.get(key));
						maps.add(map);
					}
				}
			}
			String result = StringUtils.createJson(maps);
			AjaxUtil.ajaxOutput(response, result);
			//http://192.168.199.240/zld/carservice.do?action=getshares&lon=116.318512&lat=40.042214
			//http://192.168.199.240/zld/carservice.do?action=getshares&lon=0&lat=0
		}
		else if(action.equals("getparkshare")){
			logger.error("action="+action+" begin....");
			List<Map<String, Object>> maps = new ArrayList<Map<String,Object>>();
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			int hour = c.get(Calendar.HOUR_OF_DAY);
			if(hour>=8&&hour<=10){
				AjaxUtil.ajaxOutput(response, "[]");
				return null;
			}
//			maps=getShares();
			
			String ids = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "ids"));
			String[] allId = ids.split(",");
			String params = "";
			List<Object> valusList = new ArrayList<Object>();
			for(String id: allId){
				if(!id.equals("")&&Check.isNumber(id)){
					if(params.equals(""))
						params = "?";
					else {
						params+=",?";
					}
					valusList.add(Long.valueOf(id));
				}
			}
			if(!ids.equals("")){
				Map<Long,Integer> comidNumber = new HashMap<Long, Integer>();
				List<Map<String, Object>> comMaps = daService.getAllMap("select id,share_number,parking_total from com_info_tb where id in("+params+")",valusList);
				if(comMaps!=null){
					for(Map map : comMaps){
						try {
							comidNumber.put((Long)map.get("id"), (Integer)map.get("share_number"));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				valusList.add(0,0);
				List<Map<String, Object>> comOrderMaps = daService.getAll("select count(*) count,comid  from order_tb where state=?   group by comid",new Object[]{0});
				if(comOrderMaps!=null){
					for(Map map : comOrderMaps){
						Long comId = (Long) map.get("comid");
						Integer shared = Integer.valueOf(map.get("count")+"");
						if(comidNumber.containsKey(comId)){
							Integer count = comidNumber.get(comId);
							if(count>shared)
								comidNumber.put(comId, count-shared);
							else {
								comidNumber.put(comId,0);
							}
						}
					}
				}
				if(comidNumber.size()>0){
					for(Long key :comidNumber.keySet()){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("id", key);
						map.put("number", comidNumber.get(key));
						maps.add(map);
					}
				}
			}
			//logger.error(maps);
			logger.error("action="+action+" over....");
			String result = StringUtils.createJson(maps);
			//http://192.168.199.240/zld/carservice.do?action=getparkshare&ids=3,10
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getprice")){
			Long parkId = RequestUtil.getLong(request, "parkid", -1L);
			Map priceMap = publicMethods.getPriceMap(parkId);
			String _price = "0";
			if(priceMap!=null){
				int pay_type = (Integer)priceMap.get("pay_type");
				Double price = Double.valueOf(priceMap.get("price")+"");
				_price = price+"元/次";
				Integer unit = (Integer)priceMap.get("unit");
				if(pay_type==0){
					_price =price+"元/"+unit+"分钟";
				}else {
					if(unit!=null&&unit>0){
						if(unit>60){
							String t = "";
							if(unit%60==0)
								t = unit/60+"小时";
							else
								t = unit/60+"小时 "+unit%60+"分钟";
							_price =priceMap.get("price")+"元/"+t;
						}else {
							_price = priceMap.get("price")+"元/"+unit+"分钟";
						}
					}else {
						_price = priceMap.get("price")+"元/次";
					}
				}
			}
			if(parkId!=-1){
				AjaxUtil.ajaxOutput(response, _price);
			} 
		//	http://192.168.199.209/zld/carservice.do?action=getprice&parkid=3
		}else if(action.equals("scanibeacon")){//找Ibeacon位置
			/*String sql = "select * from area_ibeacon_tb where lng between ? and ? and lat between ? and ? ";
			Double lng = RequestUtil.getDouble(request, "lng", 0d);
			Double lat = RequestUtil.getDouble(request, "lat", 0d);
			//偏移200米
			double d1 = 0.002346;
			double d2 = 0.001792;
			
			Object [] params = new Object[]{lng-d1,lng+d1,lat-d2,lat+d2};//查询车主为中心，前后左右200米以内的Ibeacon
			List<Map<String, Object>> ibeanList = daService.getAll(sql, params);
			logger.error(sql+":"+StringUtils.objArry2String(params));
			if(ibeanList!=null&&!ibeanList.isEmpty()){//存在200米以内的Ibeacon
				Double d = 0d;
				for(Map<String, Object> map : ibeanList){
					if(map.get("lng")!=null&&map.get("lat")!=null){
						//计算车主到Ibeacon的最短距离
						double des = StringUtils.distance(lng, lat, 
								Double.valueOf(map.get("lng")+""), Double.valueOf(map.get("lat")+""));
						if(d==0||des<d)
							d = des;
					}
				}
				AjaxUtil.ajaxOutput(response,d.intValue()+"" );
			}else {//不存在200米以内的Ibeacon
				AjaxUtil.ajaxOutput(response,"201" );
			}*/
			AjaxUtil.ajaxOutput(response, "205");
//			http://192.168.199.240/zld/carservice.do?action=scanibeacon&lng=116.305970&lat=40.041474
		}
		return null;
	}



	private List<Map<String, Object>> getShares() {
		//查出所有两分钟中更新的停车场
		List<Map<String, Object>> comMaps = daService.getAll("select id,share_number from " +
				"com_info_tb where update_time>?",new Object[]{System.currentTimeMillis()/1000-2*60});
		Map<Long, Integer> comidNumber = new HashMap<Long, Integer>();
		List<Object> comIdList=new ArrayList<Object>();
		List<Map<String, Object>> maps = new ArrayList<Map<String,Object>>();
		if(comMaps!=null&&!comMaps.isEmpty()){
			String preComs = "";
			for(Map map : comMaps){
				try {
					comidNumber.put((Long)map.get("id"), (Integer)map.get("share_number"));
					if(preComs.equals("")){
						preComs="?";
					}else {
						preComs+=",?";
					}
					comIdList.add(map.get("id"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			comIdList.add(0,0);
			List<Map<String, Object>> comOrderMaps = daService.getAllMap("select count(*) count,comid  from order_tb where state=? and comid in("+preComs+") group by comid",comIdList);
			if(comOrderMaps!=null){
				for(Map map : comOrderMaps){
					Long comId = (Long) map.get("comid");
					Integer shared = Integer.valueOf(map.get("count")+"");
					if(comidNumber.containsKey(comId)){
						Integer count = comidNumber.get(comId);
						comidNumber.put(comId, count-shared);
					}
				}
			}
			
			if(comidNumber.size()>0){
				for(Long key :comidNumber.keySet()){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("id", key);
					map.put("number", comidNumber.get(key));
					//map.put("total", comidTotal.get(key));
					maps.add(map);
				}
			}
		}
		return maps;
	}
	
}