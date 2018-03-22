package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityVideoSurvillanceAction extends Action {

	@Autowired
	private PgOnlyReadService onlyReadService;

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//閻ц缍嶉惃鍕暏閹寸d
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
					//{ "name": "鐟楀潡鍎撮弸銏㈣埅1", "channelID": "1000002$1$0$0" }
					v.put("name",map.get("video_name"));
					v.put("channelID", map.get("channelid"));
					vList.add(v);
				}
				ret +="{\"playlist\":"+StringUtils.createJson(vList)+"}";
				return ret;
			}
			//return "[[116.417854,39.921988,\"閸︽澘娼冮敍姘娴滎剙绔舵稉婊冪厔閸栬櫣甯囨惔婊�俺婢堆嗩敎88閸欒渹绠版径鈺呮懕濞夋壆娅ㄧ拹褍鍙撶仦淇�],"+
			//		" [116.406605,39.921585,\"閸︽澘娼冮敍姘娴滎剙绔舵稉婊冪厔閸栬桨绗㈤崡搴ㄦ，婢堆嗩敎\"],"+
			//		" [116.412222,39.912345,\"閸︽澘娼冮敍姘娴滎剙绔舵稉婊冪厔閸栫儤顒滄稊澶庣熅閻拷閸欑﹥"]]";//StringUtils.createJson(result);
		}
		return "[]";
	}

}
