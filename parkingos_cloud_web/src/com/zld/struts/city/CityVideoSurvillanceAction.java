package com.zld.struts.city;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.util.Hash;
import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.StringUtils;

import freemarker.template.utility.StringUtil;

public class CityVideoSurvillanceAction extends Action {
	
	@Autowired
	private PgOnlyReadService onlyReadService;
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//鐧诲綍鐨勭敤鎴穒d
		request.setAttribute("authid", request.getParameter("authid"));
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		//String target = null;
		if(action.equals("getvideo")){
			String result = getVideo(request);
			AjaxUtil.ajaxOutput(response, result);
		}if(action.equals("")){
			request.setAttribute("cityid", cityid);
			return mapping.findForward("list");
		}
		return null;
	}

	private String getVideo(HttpServletRequest request) {
		Double lon = RequestUtil.getDouble(request, "lon", 0.0);
		Double lat = RequestUtil.getDouble(request, "lat", 0.0);
		Long cityid = (Long)request.getSession().getAttribute("cityid");
       	//String url ="http://api.map.baidu.com/geoconv/v1/?";
       	//String result1 = null;
		if(lon>0&&lat>0){
			//double lngp = 0.02346 * 2;
			//double latp = 0.01792;
			String sql = "select * from city_video_tb where cityid=? order by id";
			List<Map<String, Object>> result = onlyReadService.getAll(sql, new Object[] {cityid });
			/*String sql = "select * from city_video_tb where longitude between ? and ? and latitude between ? and ? and cityid=?";
			List<Map<String, Object>> result = onlyReadService.getAll(sql, new Object[] { lon - lngp, lon + lngp, lat - latp,lat + latp,cityid });*/
			String ret ="";
			if(result!=null&&!result.isEmpty()){
				List<Map<String, Object>> vList = new ArrayList<Map<String,Object>>();
				for(Map<String, Object> map : result){
					Map<String, Object> v = new HashMap<String, Object>();
					//{ "name": "瑗块儴鏋㈢航1", "channelID": "1000002$1$0$0" }
					v.put("name",map.get("video_name"));
					v.put("channelID", map.get("channelid"));
					vList.add(v);
				}
				ret +="{\"playlist\":"+StringUtils.createJson(vList)+"}";
				return ret;
			}
			//return "[[116.417854,39.921988,\"鍦板潃锛氬寳浜競涓滃煄鍖虹帇搴滀簳澶ц88鍙蜂箰澶╅摱娉扮櫨璐у叓灞俓"],"+
			//		" [116.406605,39.921585,\"鍦板潃锛氬寳浜競涓滃煄鍖轰笢鍗庨棬澶ц\"],"+
			//		" [116.412222,39.912345,\"鍦板潃锛氬寳浜競涓滃煄鍖烘涔夎矾鐢�鍙穃"]]";//StringUtils.createJson(result);
		}
		return "[]";
	}

}
