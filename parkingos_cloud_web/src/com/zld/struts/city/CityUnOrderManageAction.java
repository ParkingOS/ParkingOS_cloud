package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.pojo.QueryCount;
import com.zld.pojo.QueryList;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class CityUnOrderManageAction extends Action {
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

	private Logger logger = Logger.getLogger(CityUnOrderManageAction.class);

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
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"order_tb","o",
					new String[]{"parking_type","duration","cid","groupid","uid"});
			SqlInfo sqlInfo1 = getSuperSqlInfo(request);
			SqlInfo sqlInfo2 = getSuperSqlInfo1(request);
			SqlInfo sqlInfo3 = getSqlInfo3(request);
			if(groupid==-1){
				groupid = RequestUtil.getLong(request, "groupid_start", -1L);
			}
			List list = null;
			Long count = 0L;
			Long ntime = System.currentTimeMillis()/1000;
			ArrayList<Object> params = new ArrayList<Object>();
			String sql = "select o.*,c.parking_type,p.cid from order_tb o left join com_info_tb c on o.comid=c.id left join " +
					" com_park_tb p on o.berthnumber=p.id where o.state=? and o.ishd=? and o.create_time between ? and ? ";
			String countSql = "select count(o.id) from order_tb o left join com_info_tb c on o.comid=c.id left join " +
					" com_park_tb p on o.berthnumber=p.id where o.state=? and o.ishd=? and o.create_time between ? and ? ";
			params.add(0);
			params.add(0);
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
				sql += " and o.comid in ("+preParams+") ";
				countSql += " and o.comid in ("+preParams+") ";
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
					orderfield = "create_time";
				}

				if(orderby.equals("")){
					orderby = " desc ";
				}
				if(orderfield.equals("duration")){
					orderfield = "create_time";
					if(orderby.equals("asc")){
						orderby = "desc";
					}else if(orderby.equals("desc")){
						orderby = "asc";
					}
				}
				if(orderfield.equals("cid")){
					orderfield = "p." + orderfield;
				}else{
					orderfield = "o." + orderfield;
				}
				sql += " order by " + orderfield + " " + orderby + " nulls last ";
				System.out.println(sql);
				System.err.println(params);
				QueryCount queryCount = new QueryCount(pgOnlyReadService, countSql, params);
				QueryList queryList = new QueryList(pgOnlyReadService, sql, params, pageNum, pageSize);
				Future<Long> future0 = pool.submit(queryCount);
				Future<List> future1 = pool.submit(queryList);
				count = future0.get();
				list = future1.get();
				setList(list);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("completezeroorder")){
			int ret = completezeroorder(request);
			AjaxUtil.ajaxOutput(response, ret+"");
		}

		return null;
	}


	private int completezeroorder(HttpServletRequest request){
		int ret = 0;
		try {
			String ids =RequestUtil.processParams(request, "ids");
			if(StringUtils.isNotNull(ids)){
				String[] idsarr = ids.split(",");
				long etime = System.currentTimeMillis()/1000;
				for (int i = 0; i < idsarr.length; i++) {
					long id = Long.valueOf(idsarr[i]);
					Map<String, Object> map = pgOnlyReadService.getMap("select comid,c_type,berthnumber from order_tb where id=? ",
							new Object[]{id});
					if(map == null || map.get("comid") == null){
						continue;
					}
					Long comid = (Long)map.get("comid");
					Integer c_type = (Integer)map.get("c_type");
					Long berthId = null;
					if(map.get("berthnumber") != null){
						berthId = (Long)map.get("berthnumber");
					}
					Integer need_sync = 0;//预支付订单需要同步到线下  0:不需要
					Integer pay_type = 1;//1:现金支付
					String log = "后台0元结算非月卡订单：";
					if(c_type == 5){
						pay_type = 3;//3:包月
						log = "后台0元结算月卡订单：";
					}
					if(publicMethods.isEtcPark(comid)){
						need_sync = 4;//4:线上结算的都需要同步下去
						log = "带本地服务器的" + log;
					}
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					//更新订单
					Map<String, Object> orderSqlMap = new HashMap<String, Object>();
					//更新泊位
					Map<String, Object> berthSqlMap = new HashMap<String, Object>();
					orderSqlMap.put("sql", "update order_tb set isclick=?,total=?,pay_type=?,end_time=?,state=?,need_sync=? " +
							" where id=? and state=? ");
					orderSqlMap.put("values", new Object[]{1, 0, pay_type, etime, 1, need_sync, id, 0});
					bathSql.add(orderSqlMap);
					if(berthId != null && berthId > 0){
						berthSqlMap.put("sql", "update com_park_tb set state=?,order_id=? where id =? and order_id=?");
						berthSqlMap.put("values", new Object[]{0, null, berthId, id});
						bathSql.add(berthSqlMap);
					}
					boolean b = daService.bathUpdate2(bathSql);
					if(b){
						ret = 1;
						mongoDbUtils.saveLogs( request,0, 6, log + id);
						logger.error(log + id + ",结算方式pay_type:" + pay_type);
					}
				}
			}
		} catch (Exception e) {
			logger.error("completezeroorder", e);
		}
		return ret;
	}

	private void setList(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			Long ntime = System.currentTimeMillis()/1000;
			for(Map<String, Object> map : list){
				Long create_time = (Long)map.get("create_time");
				map.put("duration", StringUtils.getTimeString(create_time, ntime));
			}
		}
	}

	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		Integer parking_type = RequestUtil.getInteger(request, "parking_type_start", -1);
		SqlInfo sqlInfo1 = null;
		if(parking_type > -1){
			sqlInfo1 = new SqlInfo(" c.parking_type=? ",new Object[]{parking_type});
		}
		return sqlInfo1;
	}

	private SqlInfo getSuperSqlInfo1(HttpServletRequest request){
		String cid = RequestUtil.processParams(request, "cid");
		SqlInfo sqlInfo1 = null;
		if(!cid.equals("")){
			sqlInfo1 = new SqlInfo(" p.cid like ? ",new Object[]{"%" + cid + "%"});
		}
		return sqlInfo1;
	}
	private SqlInfo getSqlInfo3(HttpServletRequest request){
		String uid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "uid"));
		SqlInfo sqlInfo1 = null;
		if(!uid.equals("")){
			sqlInfo1 = new SqlInfo(" o.uid in(select id from user_info_tb where nickname like ?)  ",new Object[]{"%" + uid + "%"});
		}
		return sqlInfo1;
	}
}
