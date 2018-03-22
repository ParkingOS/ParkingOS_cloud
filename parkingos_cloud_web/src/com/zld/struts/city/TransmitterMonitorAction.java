package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.RequestUtil;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


public class TransmitterMonitorAction extends Action {

	@Autowired
	private PgOnlyReadService onlyReadService;

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(cityid == null && groupid == null){
			return null;
		}
		if(cityid == null) cityid = -1L;
		if(groupid == null) groupid = -1L;
		if(action.equals("gettransmitter")){
			String result = getTransmitter(request);
			AjaxUtil.ajaxOutput(response, result);
		}if(action.equals("")){
			String gps = "";
			if(cityid != null && cityid > 0){
				Map cityMap = onlyReadService.getMap("select gps from org_city_merchants where id =? ",
						new Object[]{cityid});
				if(cityMap != null && cityMap.get("gps") != null){
					gps = (String)cityMap.get("gps");
				}
			}else if(groupid != null && groupid > 0){
				Map<String, Object> map = onlyReadService.getMap("select longitude,latitude from org_group_tb where id=? ",
						new Object[]{groupid});
				if(map != null && map.get("longitude") != null && map.get("latitude") != null){
					gps = map.get("longitude") + "," + map.get("latitude");
				}
			}
			request.setAttribute("gps", gps);
			return mapping.findForward("list");
		}
		return null;
	}

	private String getTransmitter(HttpServletRequest request) {
		Double lon = RequestUtil.getDouble(request, "lon", 0.0);
		Double lat = RequestUtil.getDouble(request, "lat", 0.0);
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		//String url ="http://api.map.baidu.com/geoconv/v1/?";
		//String result1 = null;
		if(lon>0&&lat>0){
			double lngp = 0.02346 * 2;
			double latp = 0.01792;
			String sql = "select * from sites_tb where  cityid=? and is_delete=? ";
			List<Map<String, Object>> result = onlyReadService.getAll(sql, new Object[] { cityid, 0 });
			String ret ="[";
			if(result!=null&&!result.isEmpty()){
				for(Map<String, Object> map : result){
					//url+="coords="+map.get("longitude")+","+map.get("latitude")+"&ak=gomvEhrIsmCOhYbLpVNuQSug";
					//result1 = new HttpProxy().doGet(url);
					//JSONObject jsonObject = JSONObject.fromObject(result1);
					//JSONArray array = jsonObject.getJSONArray("result");
					//for(Object object : array)
					//{
					//JSONObject jsonObject2 = (JSONObject)object;
					//Object xString = jsonObject2.get("x");
					//Object yString =jsonObject2.get("y");
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
			//return "[[116.417854,39.921988,\"地址：北京市东城区王府井大街88号乐天银泰百货八层\"],"+
			//		" [116.406605,39.921585,\"地址：北京市东城区东华门大街\"],"+
			//		" [116.412222,39.912345,\"地址：北京市东城区正义路甲5号\"]]";//StringUtils.createJson(result);
		}
		return "[]";
	}

}
