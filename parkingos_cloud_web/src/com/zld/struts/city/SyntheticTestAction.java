package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.Constants;
import com.zld.utils.HttpProxy;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class SyntheticTestAction extends Action {

	@Autowired
	private PgOnlyReadService onlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long) request.getSession().getAttribute("loginuin");// 登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		String dataType = RequestUtil.getString(request, "datatype");
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		if(dataType.equals(""))
			dataType="all";
		request.setAttribute("datatype",dataType);
		// String target = null;
		if (action.equals("")) {
			Map cityMap = onlyReadService.getMap("select gps from org_city_merchants where id =? ", new Object[]{cityid});
			request.setAttribute("gps", cityMap.get("gps"));
			request.setAttribute("cityid", cityid);
			//return mapping.findForward("list");
			return mapping.findForward("baidumap");
		}else if (action.equals("getbusstation")) {
			String result = getBusSttion(request);
			AjaxUtil.ajaxOutput(response, result);
		}else if (action.equals("getbiketation")) {
			String result = getBikeSttion(request);
			AjaxUtil.ajaxOutput(response, result);
		}else if (action.equals("getparktation")) {
			String result = getParkStation(request);
			AjaxUtil.ajaxOutput(response, result);
		}else if (action.equals("getchargepolestation")) {
			String result = getChargePoles(request);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("gettransmitter")){
			String result = getTransmitter(request);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getinduce")){
			String result = getInduce(request);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getroute")){
			String result = getRoute(request,response);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getvideo")){
			String result = getVideo(request, cityid);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("playvideo")){
			Long videoid = RequestUtil.getLong(request, "videoid", -1L);
			if(videoid > 0){
				Map<String, Object> map = onlyReadService.getMap("select * from city_video_tb where id=? ",
						new Object[]{videoid});
				if(map != null){
					Long comid = (Long)map.get("comid");
					if(comid > 0){
						List<Map<String, Object>> list = onlyReadService.getAll("select video_name,channelid from city_video_tb where comid=? order by id desc ",
								new Object[]{comid});
						if(list.size() > 1){
							String ret = "{\"playlist\":[]}";
							String playlist = StringUtils.createJson(list);
							ret = ret.replace("[]", playlist);
							request.setAttribute("playlist", ret);
							return mapping.findForward("multivideo");

						}
					}
					request.setAttribute("channelID", map.get("channelid"));
					return mapping.findForward("singlevideo");
				}
			}
		}else if(action.equals("getpda")){
			String result = getPda(request);
			AjaxUtil.ajaxOutput(response, result);
		}
		return null;
	}
	//获取公交



	private String getBusSttion(HttpServletRequest request) {
		Double lon = RequestUtil.getDouble(request, "lon", 0.0);
		Double lat = RequestUtil.getDouble(request, "lat", 0.0);
		if (lon > 0 && lat > 0) {
			String sql = "select * from bus_station_tb where longitude between ? and ? and latitude between ? and ? ";
			double lngp = 0.02346 * 2;
			double latp = 0.01792;
			List<Map<String, Object>> result = onlyReadService.getAll(sql,
					new Object[] { lon - lngp, lon + lngp, lat - latp,
							lat + latp });
			String ret = "[";
			if (result != null && !result.isEmpty()) {
				for (Map<String, Object> map : result) {
					ret += "["
							+ ((Double.valueOf(map.get("longitude") + "") - 0.0053))
							+ ","
							+ ((Double.valueOf(map.get("latitude") + "") + 0.00175))
							+ ",\"" + map.get("station_name") + "\",\""
							+ map.get("station_memo") + "\","+map.get("station_id")+"],";

					// }
				}

				if (ret.endsWith(","))
					ret = ret.substring(0, ret.length() - 1);

				return ret + "]";
			}
		}
		return "[]";
	}
	//获取自行车
	private String getBikeSttion(HttpServletRequest request) {
		Double lon = RequestUtil.getDouble(request, "lon", 0.0);
		Double lat = RequestUtil.getDouble(request, "lat", 0.0);
		if (lon > 0 && lat > 0) {
			String sql = "select * from  city_bike_tb where longitude between ? and ? and latitude between ? and ? ";
			double lngp = 0.02346 * 2;
			double latp = 0.01792;
			List<Map<String, Object>> result = onlyReadService.getAll(sql,
					new Object[] { lon - lngp, lon + lngp, lat - latp,
							lat + latp });
			String ret = "[";
			if (result != null && !result.isEmpty()) {
				for (Map<String, Object> map : result) {
					Object surplusObject = map.get("surplus");
					Object plot_countObject = map.get("plot_count");
					if (surplusObject != null && surplusObject != null) {
						double surplus = Double.parseDouble(map.get("surplus")
								.toString());
						double plot_count = Double.parseDouble(map.get(
								"plot_count").toString());
						double calc = surplus / plot_count;
						if (calc < 0.1)

						{
							ret += "["+ ((Double.valueOf(map.get("longitude")
									+ "")) - 0.0055)+ ","+ ((Double.valueOf(map.get("latitude") + "") + 0.0019))
									+ ",\"" + map.get("name") + "\",\""
									+ map.get("address") + "\"," + 1 + ","
									+ map.get("surplus") + ","
									+ map.get("plot_count") + ",\"车位数：紧张\"],";
						} else {
							ret += "["
									+ ((Double.valueOf(map.get("longitude")
									+ "")) - 0.0055)
									+ ","
									+ ((Double
									.valueOf(map.get("latitude") + "") + 0.0019))
									+ ",\"" + map.get("name") + "\",\""
									+ map.get("address") + "\"," + 0 + ","
									+ map.get("surplus") + ","
									+ map.get("plot_count") + ",\"车位数：正常\"],";
						}
					}

					// }
				}

				if (ret.endsWith(","))
					ret = ret.substring(0, ret.length() - 1);

				return ret + "]";
			}
		}
		return "[]";
	}
	//获取停车场
	private String getParkStation(HttpServletRequest request) {
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
		String nowtime= df2.format(System.currentTimeMillis());
		/*Long b = TimeTools.getLongMilliSecondFrom_HHMMDD(nowtime)/1000;
		Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(nowtime+" 23:59:59");*/
		Double lon = RequestUtil.getDouble(request, "lon", 0.0);
		Double lat = RequestUtil.getDouble(request, "lat", 0.0);
		Long cityid = (Long) request.getSession().getAttribute("cityid");
		List<Object> params = new ArrayList<Object>();
		if (lon > 0 && lat > 0) {
			String sql = " select * from com_info_tb   ";
			double lngp = 0.02346 * 2;
			double latp = 0.01792;
			List<Object> parks = null;
			if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " where id in ("+preParams+") ";
				params.addAll(parks);
			}
			List<Map<String, Object>> result = onlyReadService.getAllMap(sql,params);
			String ret = "[";

			if (result != null && !result.isEmpty()) {
				for (Map<String, Object> map : result) {
					Map countMap = onlyReadService.getMap("select sum(amount) count,sum(total) total from remain_berth_tb where comid=?", new Object[]{map.get("id")});
					Double emptyObject = 1.0;
					Double totalObject =1.0;
					if(countMap!=null){
						emptyObject=StringUtils.formatDouble(countMap.get("count"));
						totalObject=StringUtils.formatDouble(countMap.get("total"));
					}
					String gps = ((Double.valueOf(map.get("longitude") + "")))+ ","
							+ ((Double.valueOf(map.get("latitude") + "")));
					if (emptyObject != null && totalObject != null) {
						double empty = emptyObject;
						double total = Double.parseDouble(map.get("parking_total").toString());
						if(totalObject>1)
							total = totalObject;
						double calc = empty / total;

						if (calc < 0.10) {
							ret += "[" + gps + ",\"地址:" + map.get("address")
									+ "\",\"停车场名称:" + map.get("company_name")
									+ "\"," + 1 + "," + emptyObject + ","
									+ totalObject + ",\"车位数：紧张\"],";
						} else {
							ret += "[" + gps + ",\"地址:" + map.get("address")
									+ "\",\"停车场名称:" + map.get("company_name")
									+ "\"," + 0 + "," + emptyObject + ","
									+ totalObject + ",\"车位数：正常\"],";
						}
					} else {
						ret += "[" + gps + ",\"地址:" + map.get("address")
								+ "\",\"停车场名称:" + map.get("company_name")
								+ "\"," + 2 + ",\"未知\","
								+ totalObject + ",\"车位数：正常\"],";
					}

				}

				if (ret.endsWith(","))
					ret = ret.substring(0, ret.length() - 1);
				return ret + "]";
			}

		}
		return "[]";
	}
	//获取充电桩
	private String getChargePoles(HttpServletRequest request) {
		Double lon = RequestUtil.getDouble(request, "lon", 0.0);
		Double lat = RequestUtil.getDouble(request, "lat", 0.0);
		String url = "";
		String result = null;
		if (lon > 0 && lat > 0) {
			String ret = "[";
			url = "http://121.40.130.8/zld/carinter.do?action=getchargeinfo&lng="
					+ lon + "&lat" + lat;
			result = new HttpProxy().doGet(url);
			JSONObject jsonObject = JSONObject.fromObject(result);
			JSONArray array = jsonObject.getJSONArray("data");
			if (array != null) {
				for (Object object : array) {
					JSONObject jsonObject2 = (JSONObject) object;
					Object xString = jsonObject2.get("lng");
					double x=(Double.parseDouble((xString).toString()))-0.012;
					Object yString = jsonObject2.get("lat");
					double y=(Double.parseDouble((yString).toString()))-0.0055;
					Object name = jsonObject2.get("name");
					Object addr = jsonObject2.get("addr");
					Object freeobject=jsonObject2.get("free");
					Object totalobject=jsonObject2.get("total");
					if(freeobject!=null&&totalobject!=null){
						double free=Double.parseDouble((freeobject).toString());
						double total=Double.parseDouble((totalobject).toString());
						double calc=free/total;
						if(calc<0.1)
						{
							ret += "[" + x + "," + y + ",\"" + name
									+ "\",\"" + addr + "\","+free+","+total+","+0+",\"桩位数：紧张\"],";
						}
						else
						{
							ret += "[" + x + "," + y + ",\"" + name
									+ "\",\"" + addr + "\","+free+","+total+","+1+",\"桩位数：正常\"],";
						}

					}

				}

				if (ret.endsWith(","))
					ret = ret.substring(0, ret.length() - 1);
				return ret + "]";

			}

		}
		return "[]";

	}
	//获取基站
	private String getTransmitter(HttpServletRequest request) {
		Double lon = RequestUtil.getDouble(request, "lon", 0.0);
		Double lat = RequestUtil.getDouble(request, "lat", 0.0);
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		if(lon>0&&lat>0){
			double lngp = 0.02346 * 2;
			double latp = 0.01792;
			String sql = "select * from sites_tb where cityid=? and is_delete=? ";
			List<Map<String, Object>> result = onlyReadService.getAll(sql, new Object[] { cityid, 0 });
			String ret ="[";
			if(result!=null&&!result.isEmpty()){
				for(Map<String, Object> map : result){

					if((map.get("longitude"))!=null&& (map.get("latitude"))!=null){
						String  state="";
						if(Integer.parseInt(map.get("state").toString())==0)
						{
							state="故障";
						}
						else {
							state="正常";
						}
						String gps = (Double.valueOf(map.get("longitude") + ""))+ ","
								+ (Double.valueOf(map.get("latitude") + ""));
						ret +="["+gps+",\"地址:"+map.get("address")+"\",\"基站名称:"+map.get("name")+"\","+map.get("voltage")+",\"状态:"+state+"\"],";
					}


				}

				if(ret.endsWith(","))
					ret = ret.substring(0,ret.length()-1);
				return ret+"]";
			}

		}
		return "[]";
	}
	//获取诱导
	private String getInduce(HttpServletRequest request) {
		Double lon = RequestUtil.getDouble(request, "lon", 0.0);
		Double lat = RequestUtil.getDouble(request, "lat", 0.0);
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		if(lon>0&&lat>0){
			String sql = "select * from induce_tb  where cityid=? and is_delete=?";
			List<Map<String, Object>> result = onlyReadService.getAll(sql, new Object[]{ cityid, 0 });
			String ret ="[";
			if(result!=null&&!result.isEmpty()){
				for(Map<String, Object> map : result){
					if((map.get("longitude"))!=null && (map.get("latitude"))!=null){
						String  type="";
						if(Integer.parseInt(map.get("type").toString())==1){
							type="二级诱导屏";
						} else if (Integer.parseInt(map.get("type").toString())==3) {
							type="三级诱导屏";
						}
						String data=getIduceData((Long)map.get("id"),request);//"[{\"total\":1,\"parklist\":[{\"id\":20427,\"parkname\":\"青年东路\",\"induce_id\":11,\"remain\":18,\"total\":58}],\"error\":null,\"success\":true}]";
						ret +="["+(Double.valueOf(map.get("longitude")+ ""))+ ","+ (Double.valueOf(map.get("latitude") + "")) +",\"地址:"+map.get("address")+"\",\"诱导名称:"+map.get("name")+"\",\"广告信息:"+map.get("ad")+"\",\""+map.get("type")+"\",\""+map.get("id")+"\","+data+"],";
					}
				}
				if(ret.endsWith(","))
					ret = ret.substring(0,ret.length()-1);
				return ret+"]";
			}

		}
		return "[]";
	}
	private String getIduceData(Long id,HttpServletRequest request) {
		String sql = "select * from induce_tb  where cityid=? and is_delete=? and id=?";
		Long Cityid = (Long)request.getSession().getAttribute("cityid");
		Map<String, Object> induceMap = onlyReadService.getMap(sql, new Object[]{Cityid,0,id});
		String did = (String)induceMap.get("did");
		String result = new HttpProxy().doGet("http://s.tingchebao.com/zld/induceinfo.do?action=parkinfo&did="+did);
		return result;
	}
	//获取公交线路
	private String getRoute(HttpServletRequest request,HttpServletResponse response) {
		String result = null;
		String stationid=RequestUtil.getString(request, "stationID");
		String	url = "http://121.40.130.8/zld/carinter.do?action=getstatbyid&routeID=-1&stationID="+stationid;
		result = new HttpProxy().doGet(url);
		System.out.println(result);
		return result;

	}
	//获取视频
	private String getVideo(HttpServletRequest request, Long cityid) {
		Double lon = RequestUtil.getDouble(request, "lon", 0.0);
		Double lat = RequestUtil.getDouble(request, "lat", 0.0);
		if(lon > 0 && lat > 0){
			List<Object> comids = new ArrayList<Object>();
			String sql = "select * from city_video_tb where cityid=? order by id desc ";
			List<Map<String, Object>> result = onlyReadService.getAll(sql, new Object[]{ cityid });
			String ret ="[";
			if(result != null && !result.isEmpty()){
				for(Map<String, Object> map : result){
					Long comid = (Long)map.get("comid");
					if(comid > 0){
						if(comids.contains(comid)){
							continue;
						}else{
							comids.add(comid);
						}
					}
					if(map.get("longitude") != null && map.get("latitude") != null){
						Double longitude = Double.valueOf(map.get("longitude")+ "");
						Double latitude = Double.valueOf(map.get("latitude")+ "");
						ret +="["+(longitude - 0.01210)+ "," + (latitude - 0.00440) + ",\""
								+map.get("state")+"\",\""+map.get("video_name")+"\",\""+map.get("id")+"\"],";
					}
				}
			}

			if(ret.endsWith(","))
				ret = ret.substring(0,ret.length()-1);
			return ret+"]";
		}
		return "[]";
	}
	//获取PDA数据
	private String getPda(HttpServletRequest request) {

		String sql = "SELECT ui.id, ui.nickname, ui.phone, ul.is_onseat, ul.ctime, ul.lat, ul.lon,ul.ctime"+
				" from user_info_tb as ui left join user_local_tb as ul on ui.id = ul.uid"+
				" where ui.cityid = ? and ui.auth_flag = "+Constants.AUTH_FLAG_COLLECTOR+
				" and (ul.ctime = (select max(ult.ctime) from user_local_tb as ult where ult.uid = ui.id) or ul.ctime is null)";
		Long Cityid = (Long)request.getSession().getAttribute("cityid");
		long time = System.currentTimeMillis()/1000 - 30*60;
		List<Map<String, Object>> result = onlyReadService.getAll(sql, new Object[]{Cityid});
		String str = "[";
		if(result != null){
			for(int i = 0 ; i < result.size() ; ++i){
				Map<String,Object> map = result.get(i);
				String update_time = "";
				if(map.get("ctime") != null){
					Date date = new Date(((Long)map.get("ctime"))*1000);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					update_time = sdf.format(date);
				}
				str += "{\"uid\":"+map.get("id")+", \"latitude\":"+map.get("lat")+
						",\"longtitude\":"+map.get("lon")+", \"is_onseat\":"+map.get("is_onseat")+
						",\"nickname\":\""+map.get("nickname")+"\""+
						",\"update_time\":\""+ update_time +"\"},";
				//str += "{\"uid\":1, \"latitude\":25.279087,\"longtitude\":110.29663, \"is_onseat\":1},";
			}
		}
		str += "]";
		return str;
	}




}
