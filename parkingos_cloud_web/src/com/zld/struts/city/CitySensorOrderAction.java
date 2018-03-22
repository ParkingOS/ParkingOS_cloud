package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.pojo.QueryCount;
import com.zld.pojo.QueryList;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class CitySensorOrderAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;

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
		ExecutorService pool = ExecutorsUtil.getExecutorService();//获取线程池
		if(action.equals("")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Long today = TimeTools.getToDayBeginTime();
			request.setAttribute("btime", df2.format(today*1000));
			request.setAttribute("etime",  df2.format(System.currentTimeMillis()));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String orderfield = RequestUtil.processParams(request, "orderfield");
			String orderby = RequestUtil.processParams(request, "orderby");
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime);
			Long e = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"berth_order_tb","",
					new String[]{"berthsec_id", "cid", "groupid", "car_number"});
			SqlInfo sqlInfo1 = getSuperSqlInfo(request);
			SqlInfo sqlInfo2 = getSuperSqlInfo1(request);
			SqlInfo sqlInfo3 = getSuperSqlInfo2(request);
			if(groupid == -1){
				groupid= RequestUtil.getLong(request, "groupid", -1L);
			}
			String sql = "select * from berth_order_tb where in_time between ? and ? ";
			String countSql = "select count(id) from berth_order_tb where in_time between ? and ? ";
			List list = null;
			Long count = 0L;
			ArrayList<Object> params = new ArrayList<Object>();
			params.add(b);
			params.add(e);
			List<Object> parks = null;
			if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}else if(cityid > 0){
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
				sql += " and comid in ("+preParams+") ";
				countSql += " and comid in ("+preParams+") ";
				params.addAll(parks);

				if(sqlInfo!=null){
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
				if(sqlInfo3 != null){
					countSql+=" and "+ sqlInfo3.getSql();
					sql +=" and "+sqlInfo3.getSql();
					params.addAll(sqlInfo3.getParams());
				}
				if(orderfield.equals("")){
					orderfield = "id ";
				}
				if(orderby.equals("")){
					orderby = " desc ";
				}
				if(orderfield.equals("cid")){
					orderfield = "dici_id";
				}
				sql += " order by " + orderfield + " " + orderby + " nulls last ";
				QueryCount queryCount = new QueryCount(pgOnlyReadService, countSql, params);
				QueryList queryList = new QueryList(pgOnlyReadService, sql, params, pageNum, pageSize);
				Future<Long> future0 = pool.submit(queryCount);
				Future<List> future1 = pool.submit(queryList);
				count = future0.get();
				list = future1.get();
			}
			getBerth(list);
			getPosOrderInfo(list);
			getDuration(list);
			getCollector(list);
			String json = JsonUtil.Map2Json(list, pageNum, count, fieldsstr, "id");
			AjaxUtil.ajaxOutput(response, json);
		}

		return null;
	}

	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		Long berthsec_id = RequestUtil.getLong(request, "berthsec_id_start", -1L);
		SqlInfo sqlInfo1 = null;
		if(berthsec_id > 0){
			sqlInfo1 = new SqlInfo(" dici_id in (select id from com_park_tb where " +
					" berthsec_id=?) ",new Object[]{berthsec_id});
		}
		return sqlInfo1;
	}

	private SqlInfo getSuperSqlInfo1(HttpServletRequest request){
		String cid = RequestUtil.processParams(request, "cid");
		SqlInfo sqlInfo1 = null;
		if(!cid.equals("")){
			sqlInfo1 = new SqlInfo(" dici_id in (select id from com_park_tb where " +
					" cid like ? ) ",new Object[]{"%" + cid + "%"});
		}
		return sqlInfo1;
	}

	private SqlInfo getSuperSqlInfo2(HttpServletRequest request){
		String car_number = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number"));
		SqlInfo sqlInfo1 = null;
		if(!car_number.equals("")){
			sqlInfo1 = new SqlInfo(" orderid in (select id from order_tb where " +
					" car_number like ?) ",new Object[]{"%" + car_number + "%"});
		}
		return sqlInfo1;
	}

	private void getPosOrderInfo(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> idList = new ArrayList<Object>();
			String preParams = "";
			for(Map<String, Object> order : list){
				idList.add(order.get("orderid"));
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}

			List<Map<String, Object>> rList = pgOnlyReadService.getAllMap("select id,create_time," +
					"end_time,car_number from order_tb where id in (" + preParams + ")", idList);
			if(rList != null && !rList.isEmpty()){
				for(Map<String, Object> map : list){
					Long orderId = (Long)map.get("orderid");
					for(Map<String, Object> map2 : rList){
						Long id = (Long)map2.get("id");
						if(id.intValue() == orderId.intValue()){
							map.put("create_time", map2.get("create_time"));
							map.put("end_time", map2.get("end_time"));
							map.put("car_number", map2.get("car_number"));
							break;
						}
					}
				}
			}
		}
	}

	private void getDuration(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> order : list){
				if(order.get("in_time") != null && order.get("out_time") != null){
					Long in_time = (Long)order.get("in_time");
					Long out_time = (Long)order.get("out_time");
					order.put("dura", StringUtils.getTimeString(in_time, out_time));
				}
			}
		}
	}

	private void getCollector(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> idList = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> order : list){
				if(!idList.contains(order.get("in_uid"))){
					idList.add(order.get("in_uid"));
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}

				if(!idList.contains(order.get("out_uid"))){
					idList.add(order.get("out_uid"));
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
			}

			List<Map<String, Object>> rList = pgOnlyReadService.getAllMap("select id,nickname " +
					" from user_info_tb where id in (" + preParams + ")", idList);
			if(rList != null && !rList.isEmpty()){
				for(Map<String, Object> map : list){
					Long in_uid = (Long)map.get("in_uid");
					Long out_uid = (Long)map.get("out_uid");
					for(Map<String, Object> map2 : rList){
						Long id = (Long)map2.get("id");
						if(in_uid.intValue() == id.intValue()){
							map.put("in_collector", map2.get("nickname"));
							break;
						}
					}
					for(Map<String, Object> map2 : rList){
						Long id = (Long)map2.get("id");
						if(out_uid.intValue() == id.intValue()){
							map.put("out_collector", map2.get("nickname"));
							break;
						}
					}
				}
			}
		}
	}

	private void getBerth(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> idList = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> order : list){
				if(!idList.contains(order.get("dici_id"))){
					idList.add(order.get("dici_id"));
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
			}

			List<Map<String, Object>> rList = pgOnlyReadService.getAllMap("select id,cid,berthsec_id " +
					" from com_park_tb where id in (" + preParams + ")", idList);
			if(rList != null && !rList.isEmpty()){
				for(Map<String, Object> map : list){
					Long dici_id = (Long)map.get("dici_id");
					for(Map<String, Object> map2 : rList){
						Long id = (Long)map2.get("id");
						if(dici_id.intValue() == id.intValue()){
							map.put("cid", map2.get("cid"));
							map.put("berthsec_id", map2.get("berthsec_id"));
							break;
						}
					}
				}
			}
		}
	}
}
