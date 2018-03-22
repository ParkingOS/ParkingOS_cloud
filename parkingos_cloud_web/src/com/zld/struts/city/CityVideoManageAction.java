package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class CityVideoManageAction extends Action {

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;


	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//
		request.setAttribute("authid", request.getParameter("authid"));
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		if(uin == null || cityid== null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			request.setAttribute("cityid", cityid);
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String sql = "select * from city_video_tb where cityid=? and state<>? " ;
			String countSql = "select count(*) from city_video_tb where cityid=? and state<>? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(cityid);
			params.add(2);
			count = daService.getCount(countSql,params);
			if(count>0){
				list = daService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");

			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("query")){
			String sql = "select * from city_video_tb where cityid=? and state<>? " ;
			String countSql = "select count(*) from city_video_tb where cityid=? and state<>? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"city_video_tb");
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(cityid);
			params.add(2);
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);

		}
		else if(action.equals("create")){
			int r = createVideo(request, cityid);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			int r = editVideo(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("delete")){
			int r = deleteVideo(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}
		return null;
	}

	private int createVideo(HttpServletRequest request, Long cityid){
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "video_name"));
		String ip = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "ip"));
		String port = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "port"));
		String deviceid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "devideid"));
		String channelid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "channelid"));
		String cusername = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "cusername"));
		String cpassword = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "cpassword"));
		String latitude = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "latitude"));
		String longitude = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "longitude"));
		String manufacture = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "manufacture"));
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		Integer state = RequestUtil.getInteger(request, "state", 1);//0：故障 1：正常 2：删除
		Integer type = RequestUtil.getInteger(request, "type", 1);//监控类型 0：路侧监控 1:封闭停车场监控
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		Long ntime  =System.currentTimeMillis()/1000;
		int r = daService.update("insert into city_video_tb(video_name, ip, port, cusername, cpassword, manufacture, state, type, cityid, latitude, longitude, create_time, deviceid, channelid, address, comid) " +
						"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
				new Object[]{name,ip,port,cusername,cpassword,manufacture,state,type, cityid,latitude,longitude,ntime,deviceid, channelid, address,comid});
		return r;
	}

	private int editVideo(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "video_name"));
		String ip = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "ip"));
		String port = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "port"));
		String deviceid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "devideid"));
		String channelid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "channelid"));
		String cusername = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "cusername"));
		String cpassword = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "cpassword"));
		String latitude = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "latitude"));
		String longitude = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "longitude"));
		String manufacture = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "manufacture"));
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		Integer state = RequestUtil.getInteger(request, "state", 1);//0：故障 1：正常 2：删除
		Integer type = RequestUtil.getInteger(request, "type", 1);//监控类型 0：路侧监控 1:封闭停车场监控
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		Long ntime  =System.currentTimeMillis()/1000;
		int r = daService.update("update city_video_tb set video_name=?, ip=?, port=?,deviceid=?,channelid=?, cusername=?, cpassword=?, manufacture=?, state=?, latitude=?, longitude=?, update_time=?, comid=?, address=?,type=? where id=? ",
				new Object[]{name,ip,port,deviceid,channelid,cusername,cpassword,manufacture,state,latitude,longitude,ntime,comid,address,type,id});
		return r;
	}

	private int deleteVideo(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		int r = daService.update("update city_video_tb set state=? where id=? ",
				new Object[]{2,id});
		return r;
	}

}
	
