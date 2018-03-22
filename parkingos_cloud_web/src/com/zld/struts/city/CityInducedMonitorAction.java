package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.HttpProxy;
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


public class CityInducedMonitorAction extends Action {

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
		//String target = null;
		if(action.equals("getinduce")){
			String result = getInduce(request);
			AjaxUtil.ajaxOutput(response, result);
		} else if(action.equals("")){
			Map cityMap = onlyReadService.getMap("select gps from org_city_merchants where id =? ", new Object[]{cityid});
			request.setAttribute("gps", cityMap.get("gps"));
			request.setAttribute("cityid", cityid);
			return mapping.findForward("list");
		}
		return null;
	}

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

}
