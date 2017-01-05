package com.zld.struts.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
import com.zld.utils.HttpProxy;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.utils.ZldUploadUtils;


/**
 * 第三方数据接口
 * @author Administrator
 * 20160322
 */
@Path("third")
public class ZldThirdApi {
	

	
	@POST
	@Path("/bus")//查询公交
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void bus(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		String action = paramMap.get("action");
		String result= getBusResult(action,paramMap);
		AjaxUtil.ajaxOutput(response, result);
	}
	
	@POST
	@Path("/bike")//充电桩
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void bike(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		String action = paramMap.get("action");
		String result= getBikeResult(action,paramMap,context);
		AjaxUtil.ajaxOutput(response, result);
	}

	
	@POST
	@Path("/charge")//充电桩
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void charge(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		String action = paramMap.get("action");
		//String result= getBikeResult(action,paramMap,context);
		//AjaxUtil.ajaxOutput(response, result);
	}

	
	
	private String getBikeResult(String action, Map<String, String> paramMap,ServletContext context) {
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		DataBaseService daService = (DataBaseService) ctx.getBean("dataBaseService");
		if(action.equals("queryall")){
			double lon1 = 0.023482756;
			double lat1 = 0.017978752;
			Double lng =ZldUploadUtils.formatDouble(paramMap.get("lng"), 6);
			Double lat =ZldUploadUtils.formatDouble(paramMap.get("lat"), 6);
			String sql = "select id,name,address,longitude lng,latitude lat,plot_count,surplus" +
					" from city_charging_pile where longitude between ? and ? " +
					"and latitude between ? and ?  ";
			List<Object> params = new ArrayList<Object>();
			params.add(lng-lon1);
			params.add(lng+lon1);
			params.add(lat-lat1);
			params.add(lat+lat1);
			List result = daService.getAll(sql, params,0,0);
			if(result!=null)
				return StringUtils.createJson(result);
			else
				return "[]";
		}else if(action.equals("detail")){
			String id = paramMap.get("id");
			String result = new HttpProxy().doGet("http://127.0.0.1/webserviceclient/tsclient?type=bike&action=detail&id="+id);
			System.out.println(result);
		}
		return "{}";
	}

	private String getBusResult(String action, Map<String, String> paramMap) {
		String serverUrl ="http://218.91.52.117:8999/BusService/";
		if(action.equals("getnearbystatinfo")){//查询附近所有公交站点
			//http://127.0.0.1/zld/api/bus/carinter.do?action=Query_NearbyStatInfo&Longitude=119.394150&Latitude=32.387352&Range=200
			Double lng = Double.valueOf(paramMap.get("lng"));//RequestUtil.getDouble(request, "Longitude", 0d);
			Double lat = Double.valueOf(paramMap.get("lat"));// RequestUtil.getDouble(request, "Latitude", 0d);
			Integer range = Integer.valueOf(paramMap.get("range"));//RequestUtil.getInteger(request, "Range", 1000);
			HttpProxy httpProxy = new HttpProxy();
			String url =serverUrl+"Query_NearbyStatInfo/?Longitude="+lng+"&Latitude="+lat+"&Range="+range;
			String result = httpProxy.doGet(url);
			if(result==null)
				result="[]";
			else {
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
						       }else {
						    		   busMap.put(key, value);
//						    	   if(key.equals("Distance"))
//						    		   busMap.put(key, StringUtils.formatDouble(value));
//						    	   else {
//						    	   }
						       }
							 }
							busList.add(busMap);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				result = "{\"buslist\":"+StringUtils.createJson(busList)+"}";
			}
				
//				if(!busList.isEmpty()){
//					logger.error("开始写库....");
//					String sql = "insert into bus_station_tb (station_id,station_name,longitude,latitude," +
//							"station_memo,create_time,update_time) values(?,?,?,?,?,?,?)";
//					List<Object[]> values = new ArrayList<Object[]>();
//					Long ntime = System.currentTimeMillis()/1000;
//					for(Map<String, Object> map : busList){
//						Object []params= new Object[]{map.get("StationID"),map.get("StationName"),map.get("Longitude"),
//								map.get("Latitude"),map.get("StationMemo"),ntime,ntime};
//						values.add(params);
//					}
//					int ret = service.bathInsert(sql, values, new int[]{4,12,3,3,12,4,4});
//					logger.error("写入公交数据："+ret+"条");
//				}
			//}
			return result;
		}else if(action.equals("getstatbyid")){//实时信息查询_按站点ID 
			//http://121.40.130.8/zld/carinter.do?action=getstatbyid&routeID=2055&stationID=103593

			Long routeId = Long.valueOf(paramMap.get("routeid"));//RequestUtil.getLong(request, "routeID", -1L);
			Long stationId = Long.valueOf(paramMap.get("stationid"));//RequestUtil.getLong(request, "stationID", -1L);
			HttpProxy httpProxy = new HttpProxy();
			String url =serverUrl+"Query_ByStationID/?routeID="+routeId+"&stationID="+stationId;
			String result =httpProxy.doGet(url);
			if(result==null)
				result="[]";
			else {
				result="{\"data\":"+result+"}";
			}
			return result;
		}else if(action.equals("getstatbyname")){//根据线路站名称信息
			String StationName =paramMap.get("stationname");//RequestUtil.getString(request, "stationName");
			HttpProxy httpProxy = new HttpProxy();
			String url =serverUrl+"Query_ByStaNameNE/?StationName="+StationName;
			String result =httpProxy.doGet(url);
			if(result==null)
				result="[]";
			else {
				result="{\"data\":"+result+"}";
			}
			return result;
		}else if(action.equals("routestatdata")){//根据线路站点ID信息
			Long routeID =Long.valueOf(paramMap.get("routeid"));// RequestUtil.getLong(request, "RouteID", -1L);
			Long time =Long.valueOf(paramMap.get("time"));// RequestUtil.getLong(request, "time", -1L);
			if(time==-1)
				time = System.currentTimeMillis()/1000;
			String timeStamp = TimeTools.getTime_yyyyMMdd_HHmmss(time*1000);
			HttpProxy httpProxy = new HttpProxy();
			String url =serverUrl+"Require_RouteStatData/?RouteID="+routeID+"&TimeStamp="+AjaxUtil.encodeUTF8(timeStamp);
			String result =httpProxy.doGet(url);
			if(result==null)
				result="[]";
			else {
				result="{\"data\":"+result+"}";
			}
			return result;
		}
		return null;
	}
	
	
	
}