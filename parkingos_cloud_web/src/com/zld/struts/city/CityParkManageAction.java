package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
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

public class CityParkManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private PublicMethods publicMethods;

	@SuppressWarnings({ "rawtypes", "unused" })
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

		if(cityid > 0){//城市商户登录
			groupid = RequestUtil.getLong(request, "groupid", -1L);
		}
		if(action.equals("")){
			request.setAttribute("cityid", cityid);
			request.setAttribute("groupid", groupid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select * from com_info_tb where state<>? " ;
			String countSql = "select count(*) from com_info_tb where state<>? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_info","",new String[]{});
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(1);
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			List<Object> parks = null;
			if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " and id in ("+preParams+") ";
				countSql += " and id in ("+preParams+") ";
				params.addAll(parks);

				count = pgOnlyReadService.getCount(countSql,params);
				if(count>0){
					list = pgOnlyReadService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
				}
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			int r = createPark(request, cityid, groupid);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			int r = editPark(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "selids", -1L);
			int r = daService.update("update com_info_tb set state=? where id=? ",
					new Object[]{1, id});
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("set")){
			Long comid = RequestUtil.getLong(request, "id", -1L);
			request.setAttribute("parkid", comid);
			Map<String, Object> parkMap = pgOnlyReadService.getPojo("select * from com_info_tb where id=?",
					new Object[]{comid});
			Integer parking_type = 0;
			String info="";
			if(parkMap!=null){
				info ="名称："+parkMap.get("company_name")+"，地址："+parkMap.get("address")+"<br/>创建时间："
						+TimeTools.getTime_yyyyMMdd_HHmm((Long)parkMap.get("create_time")*1000)+"，车位总数："+parkMap.get("parking_total")
						+"，分享车位："+parkMap.get("share_number")+"，经纬度：("+parkMap.get("longitude")+","+parkMap.get("latitude")+")";
				parking_type = (Integer)parkMap.get("parking_type");
			}
			request.setAttribute("parking_type", parking_type);
			request.setAttribute("parkinfo", info);
			return mapping.findForward("set");
		}else if(action.equals("uploadpic")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String picurl  =publicMethods.uploadPicToMongodb(request, id, "park_pics");
			int ret =0;
			if(!"-1".equals(picurl)){
				ret = daService.update("insert into com_picturs_tb(comid,picurl,create_time) values(?,?,?) ",
						new Object[]{id,picurl,System.currentTimeMillis()/1000});
			}
			AjaxUtil.ajaxOutput(response, ret+"");
		}
		return null;
	}

	private int editPark(HttpServletRequest request){
		Long comid = RequestUtil.getLong(request, "id", -1L);
		Long time = System.currentTimeMillis()/1000;
		String company =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "company_name"));
		company = company.replace("\r", "").replace("\n", "");
		String address =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		String phone =RequestUtil.processParams(request, "phone");
		String mobile =RequestUtil.processParams(request, "mobile");
		String mcompany =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "mcompany"));
		Integer parking_total =RequestUtil.getInteger(request, "parking_total", 0);
		Integer state =RequestUtil.getInteger(request, "state", 0);
		Integer etc = RequestUtil.getInteger(request, "etc", 0);
		Integer parking_type =RequestUtil.getInteger(request, "parking_type", 0);
		Integer city = RequestUtil.getInteger(request, "city", 0);
		Double longitude =RequestUtil.getDouble(request, "longitude",0d);
		Double latitude =RequestUtil.getDouble(request, "latitude",0d);
		if(longitude == 0 || latitude == 0){
			return -2;
		}
		Long count = pgOnlyReadService.getLong("select count(*) from com_info_tb where longitude=? and latitude=? and  id<>? ",
				new Object[]{longitude,latitude,comid});
		if(count > 0){
			return -3;
		}
		String sql = "update com_info_tb set company_name=?,address=?,phone=?,mcompany=?,parking_total=?,update_time=?,state=?,etc=?,parking_type=?,city=?,mobile=?,longitude=?,latitude=? where id=? ";
		int r = daService.update(sql, new Object[]{company, address, phone, mcompany, parking_total, time, state, etc, parking_type, city, mobile, longitude, latitude, comid});
		return r;
	}

	//注册停车场
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Integer createPark(HttpServletRequest request, Long cityid, Long groupid){
		Long areaid = RequestUtil.getLong(request, "areaid", -1L);
		Long time = System.currentTimeMillis()/1000;
		String company =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "company_name"));
		company = company.replace("\r", "").replace("\n", "");
		String address =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		address = address.replace("\r", "").replace("\n", "");
		String phone =RequestUtil.processParams(request, "phone");
		String mobile =RequestUtil.processParams(request, "mobile");
		String mcompany =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "mcompany"));
		Integer parking_total =RequestUtil.getInteger(request, "parking_total", 0);
		Integer parking_type =RequestUtil.getInteger(request, "parking_type", 0);
		Integer etc = RequestUtil.getInteger(request, "etc", 0);
		Integer state =RequestUtil.getInteger(request, "state", 0);
		Integer city = RequestUtil.getInteger(request, "city", 0);
		Double longitude =RequestUtil.getDouble(request, "longitude",0d);
		Double latitude =RequestUtil.getDouble(request, "latitude",0d);
		if(longitude == 0 || latitude == 0){
			return -2;
		}
		Long count = pgOnlyReadService.getLong("select count(*) from com_info_tb where longitude=? and latitude=?",
				new Object[]{longitude,latitude});
		if(count > 0){
			return -3;
		}
		if(groupid == -1){
			return -1;
		}
		Long comId = daService.getLong("SELECT nextval('seq_com_info_tb'::REGCLASS) AS newid",null);
		//添加自动生成车场16位秘钥的逻辑
		String ukey = StringUtils.createRandomCharData(16);
		String comsql = "insert into com_info_tb(id,company_name,address,phone,create_time,mcompany,parking_total,update_time,groupid,state,areaid,etc,cityid,parking_type,city,mobile,longitude,latitude,ukey)" +
				" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		int r =  daService.update(comsql, new Object[]{comId, company, address, phone, time, mcompany, parking_total, time, groupid, state, areaid, etc, cityid, parking_type, city, mobile,longitude,latitude,ukey});
		if(r == 1){
			if(etc == 2){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("comid", comId);
				map.put("cname", company);
				commonMethods.createDefDevice(request, map);
			}
		}
		return r;
	}
}
