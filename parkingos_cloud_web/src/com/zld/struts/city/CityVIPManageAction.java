package com.zld.struts.city;

import java.util.ArrayList;
import java.util.Calendar;
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

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.TimeTools;

public class CityVIPManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods; 
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	
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
		
		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select v.*,u.mobile,u.nickname,u.address from vip_tb v,user_info_tb u where v.uin=u.id " ;
			String countSql = "select count(v.id) from vip_tb v,user_info_tb u where v.uin=u.id " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"vip_tb","v",new String[]{"mobile","car_number","nickname","address"});
			SqlInfo sqlInfo1 = RequestUtil.customSearch(request,"user_info","u",new String[]{"car_number","uin","comid","bcount","acttotal","atotal","create_time","e_time","id"});
			SqlInfo sqlInfo2 = getSuperSqlInfo(request);
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
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
				sql += " and v.comid in ("+preParams+") ";
				countSql += " and v.comid in ("+preParams+") ";
				params.addAll(parks);
				
				if(sqlInfo != null){
					countSql+=" and "+ sqlInfo.getSql();
					sql +=" and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				if(sqlInfo1 != null){
					countSql+=" and "+ sqlInfo1.getSql();
					sql +=" and "+sqlInfo1.getSql();
					params.addAll(sqlInfo1.getParams());
				}
				if(sqlInfo2 != null){
					countSql+=" and "+ sqlInfo2.getSql();
					sql +=" and "+sqlInfo2.getSql();
					params.addAll(sqlInfo2.getParams());
				}
				count = daService.getCount(countSql,params);
				if(count>0){
					list = daService.getAll(sql +" order by v.create_time desc ",params, pageNum, pageSize);
				}
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("create")){
			String r = buyProduct(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			int r = editUser(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("renew")){
			String r = buyProduct(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("detail")){
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			String mobile = RequestUtil.processParams(request, "mobile");
			request.setAttribute("comid", comid);
			request.setAttribute("mobile", mobile);
			return mapping.findForward("detail");
		}else if(action.equals("vipdetail")){
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			String mobile = RequestUtil.processParams(request, "mobile");
			if(comid == -1 || mobile.equals("")){
				return null;
			}
			String sql = "select c.id,p.id p_name,c.uin,c.name,c.address,c.create_time,c.b_time ,c.e_time ,c.remark,c.total,u.mobile,c.p_lot,c.act_total from " +
					"product_package_tb p,carower_product c ,user_info_tb u " +
					"where c.pid=p.id and u.id=c.uin and p.comid=? and u.mobile=? and u.auth_flag=? order by c.create_time desc ";
			String countSql = "select count(c.id) from product_package_tb p,carower_product c ,user_info_tb u " +
					"where c.pid=p.id and u.id=c.uin and p.comid=? and u.mobile=? and u.auth_flag=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Map<String, Object>> list = null;
			List<Object> params = new ArrayList<Object>();
			params.add(comid);
			params.add(mobile);
			params.add(4);
			Long count = pgOnlyReadService.getCount(countSql, params);
			if(count > 0){
				list = pgOnlyReadService.getAll(sql, params, pageNum, pageSize);
			}
			setList(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}
		
		return null;
	}
	
	private void setList(List<Map<String,Object>> list){
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				Long b_time = (Long)map.get("b_time");
				Long e_time = (Long)map.get("e_time");
				Integer months = Math.round((e_time - b_time)/(30*24*60*60));
				map.put("months", months);
			}
		}
	}
	
	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		String car_nubmer = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "car_number"));
		SqlInfo sqlInfo1 = null;
		if(!car_nubmer.equals("")){
			sqlInfo1 = new SqlInfo(" v.uin in (select uin from car_info_tb where car_number like ?)  ",new Object[]{"%"+car_nubmer+"%"});
		}
		return sqlInfo1;
	}
	
	private int editUser(HttpServletRequest request){
		Long uin = RequestUtil.getLong(request, "uin", -1L);
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname"));
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		int r = daService.update("update user_info_tb set nickname=?,address=? where id=? ", 
				new Object[]{name, address, uin});
		return r;
	}
	
	//注册包月会员 
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String buyProduct(HttpServletRequest request){
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		//包月产品
		Long pid =RequestUtil.getLong(request, "p_name",-1L);
		//车主手机
		String mobile =RequestUtil.processParams(request, "mobile").trim();
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname").trim());
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address").trim());
		//起始时间
		String b_time =RequestUtil.processParams(request, "b_time");
		//购买月数
		Integer months = RequestUtil.getInteger(request, "months", 1);
		//备注
		String remark = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));
		//停车位编号
		String p_lot = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "p_lot")).trim();
		//实收金额
		String acttotal = RequestUtil.processParams(request, "act_total");
		
		Long ntime = System.currentTimeMillis()/1000;
		Long btime = TimeTools.getLongMilliSecondFrom_HHMMDD(b_time)/1000;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(btime*1000);
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+months);
		Long etime = calendar.getTimeInMillis()/1000;
		
		if(comid == -1 || pid == -1){
			return "-1";
		}
		
		//金额
		Double total= commonMethods.getProdSum(pid, months);
		
		Map<String, Object> pMap = daService.getMap("select limitday,price from product_package_tb where id=? ", 
				new Object[]{pid});
		if(pMap != null && pMap.get("limitday") != null){
			Long limitDay = (Long)pMap.get("limitday");
			if(limitDay<etime){//超出有效期
				return "-2";
			}
		}
		
		Double act_total = total;
		if(!acttotal.equals("")){
			act_total = Double.valueOf(acttotal);
		}
		
		Map<String, Object> userMap = daService.getMap("select id from user_info_Tb where mobile=? and auth_flag=? ", 
				new Object[]{mobile,4});
		
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		Map<String, Object> userSqlMap = new HashMap<String, Object>();
		Map<String, Object> recomSqlMap = new HashMap<String, Object>();
	    Map<String, Object> carowerPackMap = new HashMap<String, Object>();
	    if(name.equals("")){
	    	name = "车主";
	    }
	    Long uin =-1L;
		if(userMap==null){//车主未注册
			uin = daService.getkey("seq_user_info_tb");
			userSqlMap.put("sql", "insert into user_info_tb (id,strid,nickname,mobile,auth_flag,reg_time,media,address) values(?,?,?,?,?,?,?,?)");
			userSqlMap.put("values", new Object[]{uin,"carower_"+uin,name,mobile,4,ntime,10,address});
			bathSql.add(userSqlMap);
			
			//写入记录表，用户通过注册月卡会员注册车主
			recomSqlMap.put("sql", "insert into recommend_tb(nid,type,state,create_time,comid) values(?,?,?,?,?)");
			recomSqlMap.put("values", new Object[]{uin,0,0,System.currentTimeMillis(),comid});
			bathSql.add(recomSqlMap);
		}else {
			userSqlMap.put("sql", "update user_info_tb set nickname=?,address=? where mobile=? and auth_flag=? ");
			userSqlMap.put("values", new Object[]{name,address,mobile,4});
			bathSql.add(userSqlMap);
			uin = (Long)userMap.get("id");
		}
		if(uin == null || uin == -1)
			return "-1";
		
		String result = commonMethods.checkplot(comid, p_lot, btime, etime, -1L);
		if(result != null){
			return result;
		}
		
		Long nextid = daService.getkey("seq_carower_product");
		carowerPackMap.put("sql", "insert into carower_product (id,uin,pid,create_time,b_time,e_time,total,remark,name,address,p_lot,act_total) values(?,?,?,?,?,?,?,?,?,?,?,?)");
		carowerPackMap.put("values", new Object[]{nextid,uin,pid,ntime,btime,etime,total,remark,name,address,p_lot,act_total});
		bathSql.add(carowerPackMap);
		if(daService.bathUpdate(bathSql)){
			String operater = request.getSession().getAttribute("loginuin")+"";
			if(publicMethods.isEtcPark(comid)){
//					if(f){
					int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"user_info_tb",uin,System.currentTimeMillis()/1000,0});
					if(uin>-1){
						List list = daService.getAll("select id from car_info_tb where uin = ?", new Object[]{uin});
						for (Object obj : list) {
							Map map = (Map)obj;
							Long carid = Long.parseLong(map.get("id")+"");
							if(carid!=null&&carid>0){
								daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_info_tb",carid,System.currentTimeMillis()/1000,0});
							}
						}
					}
//					}
				int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"carower_product",nextid,System.currentTimeMillis()/1000,0});
			}
			mongoDbUtils.saveLogs( request,0, 2, "车主"+mobile+"购买了套餐（编号："+pid+"）,金额："+act_total);
			return "1";
		}else {
			return "-1";
		}
	}
}
