package com.zld.struts.city;

import com.mongodb.BasicDBObject;
import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoDbUtils;
import com.zld.pojo.QueryCount;
import com.zld.pojo.QueryList;
import com.zld.pojo.QuerySum;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class CityOrderManageAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private DataBaseService dataBaseService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;

	private Logger logger = Logger.getLogger(CityOrderManageAction.class);

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
			request.setAttribute("from", RequestUtil.processParams(request, "from"));
			List<Map<String, Object>> authList = (List<Map<String, Object>>)request.getSession().getAttribute("authlist");
			if(authList != null){
				for(Map<String, Object> map : authList){
					if(map.get("url") != null){
						String url = (String)map.get("url");
						if(url.contains("cityindex.do")){
							request.setAttribute("index_authid", map.get("auth_id"));
						}
					}
				}
			}
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
			SqlInfo sqlInfo = RequestUtil.customSearch(request, "order_tb", "o", new String[]{"cid","groupid"});
			SqlInfo sqlInfo1 = getSuperSqlInfo(request);
			Double total = 0d;
			Double cash_total = 0d;
			Double elec_total = 0d;
			List list = null;
			Long count = 0L;
			if(groupid==-1){
				groupid = RequestUtil.getLong(request, "groupid_start", -1L);
			}
			ArrayList<Object> params = new ArrayList<Object>();
			String sql = "select o.*,(o.end_time-o.create_time) as duration,p.cid from order_tb o left join com_park_tb p " +
					" on o.berthnumber=p.id where o.state=? and o.ishd=? and o.end_time between ? and ? ";
			String countSql = "select count(o.id) from order_tb o left join com_park_tb p on o.berthnumber=p.id" +
					" where o.state=? and o.ishd=? and o.end_time between ? and ? ";
			String sumSql = "select sum(o.total) total from order_tb o left join com_park_tb p on o.berthnumber=p.id" +
					" where o.state=? and o.ishd=? and o.end_time between ? and ? ";
			//现金支付-总金额
			String cashSql = "select sum(o.cash_prepay) pretotal,sum(o.cash_pay) total from order_tb o left join com_park_tb p on o.berthnumber=p.id" +
					" where o.state=? and o.ishd=? and o.end_time between ? and ? ";
			//电子(手机)支付-总金额
			String elecSql = "select sum(o.electronic_prepay) pretotal, sum(o.electronic_pay) total from order_tb o left join com_park_tb p on o.berthnumber=p.id" +
					" where o.state=? and o.ishd=? and o.end_time between ? and ?";
			params.add(1);
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
				sumSql +=" and o.comid in ("+preParams+") ";
				cashSql +=" and o.comid in ("+preParams+") ";
				elecSql +=" and o.comid in ("+preParams+") ";
				params.addAll(parks);

				if(sqlInfo!=null){
					countSql+=" and "+ sqlInfo.getSql();
					sql +=" and "+sqlInfo.getSql();
					sumSql +=" and "+sqlInfo.getSql();
					cashSql +=" and "+sqlInfo.getSql();
					elecSql +=" and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				if(sqlInfo1 != null){
					countSql+=" and "+ sqlInfo1.getSql();
					sql +=" and "+sqlInfo1.getSql();
					sumSql +=" and "+sqlInfo1.getSql();
					cashSql +=" and o.comid in ("+preParams+") ";
					elecSql +=" and o.comid in ("+preParams+") ";
					params.addAll(sqlInfo1.getParams());
				}
				if(orderfield.equals("")){
					orderfield = "end_time";
				}
				if(orderby.equals("")){
					orderby = " desc ";
				}
				if(orderfield.equals("cid")){
					orderfield = "p." + orderfield;
				}else{
					orderfield = "o." + orderfield;
				}
				logger.error(sql);
				logger.error(cashSql);
				logger.error(params);
				sql += " order by " + orderfield + " " + orderby + " nulls last ";
				QuerySum querySum = new QuerySum(pgOnlyReadService, sumSql, params);
				QueryCount queryCount = new QueryCount(pgOnlyReadService, countSql, params);
				QueryList queryList = new QueryList(pgOnlyReadService, sql, params, pageNum, pageSize);
				QuerySum queryCash = new QuerySum(pgOnlyReadService, cashSql, params);
				QuerySum queryElec = new QuerySum(pgOnlyReadService, elecSql, params);
				Future<Long> future0 = pool.submit(queryCount);
				Future<List> future1 = pool.submit(queryList);
				Future<Map> future2 = pool.submit(querySum);
				Future<Map> futureCash = pool.submit(queryCash);
				Future<Map> futureElec = pool.submit(queryElec);
				count = future0.get();
				list = future1.get();
				Map<String, Object> map = future2.get();
				if(map != null && map.get("total") != null){
					total = Double.valueOf(map.get("total") + "");
				}
				Map<String, Object> cashMap = futureCash.get();
				if(cashMap != null ){
					Double cashtotal = 0.0;
					Double precashtotal = 0.0;
					if(cashMap.get("total")!=null){
						cashtotal = StringUtils.formatDouble(cashMap.get("total"));
					}
					if(cashMap.get("pretotal")!=null){
						precashtotal =  StringUtils.formatDouble(cashMap.get("pretotal"));
					}
					cash_total = cashtotal+precashtotal;
//					cash_total = StringUtils.formatDouble(cashMap.get("total")) + StringUtils.formatDouble(cashMap.get("pretotal"));
				}
				Map<String, Object> elecMap = futureElec.get();
				if(elecMap != null ){
					Double eletotal = 0.0;
					Double preeletotal = 0.0;
					if(elecMap.get("total")!=null){
						eletotal = StringUtils.formatDouble(elecMap.get("total"));
					}
					if(elecMap.get("pretotal")!=null){
						preeletotal =  StringUtils.formatDouble(elecMap.get("pretotal"));
					}
					elec_total = eletotal+preeletotal;
//					elec_total = StringUtils.formatDouble(elecMap.get("total")) + StringUtils.formatDouble(elecMap.get("pretotal"));
				}
				getCollector(list);
				queryShopTicket(list);
			}

			String json = JsonUtil.anlysisMap2Json(list,pageNum,count, fieldsstr,"id", "订单总金额："+String.format("%.2f",total)+"元,现金支付："
					+String.format("%.2f",cash_total)+"元,手机支付："+String.format("%.2f",elec_total)+"元");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("getcollname")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Map<String, Object> map = pgOnlyReadService.getMap("select nickname from user_info_tb where id=? ",
					new Object[]{id});
			String nickname = "";
			if(map != null && map.get("nickname") != null){
				nickname = (String)map.get("nickname");
			}
			AjaxUtil.ajaxOutput(response, nickname);
		}else if(action.equals("getpassname")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Map<String, Object> map = pgOnlyReadService.getMap("select passname from com_pass_tb where id=? ",
					new Object[]{id});
			String passname = "";
			if(map != null && map.get("passname") != null){
				passname = (String)map.get("passname");
			}
			AjaxUtil.ajaxOutput(response, passname);
		}else if(action.equals("getfreereason")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Map<String, Object> map = pgOnlyReadService.getMap("select name from free_reasons_tb where id=? ",
					new Object[]{id});
			String name = "";
			if(map != null && map.get("name") != null){
				name = (String)map.get("name");
			}
			AjaxUtil.ajaxOutput(response, name);
		}else if(action.equals("exportExcel")){

			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			String orderfield = RequestUtil.processParams(request, "orderfield");
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"order_tb");
			SqlInfo sqlInfo1 = getSuperSqlInfo(request);
			List<Map<String, Object>> list = null;
			List<Object> params = new ArrayList<Object>();
			params.add(1);
			params.add(0);
			String sql = "select *,(end_time-create_time) as duration from order_tb where state=? and ishd=? ";

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
				sql += " and comid in ("+preParams+") ";
				params.addAll(parks);

				if(sqlInfo!=null){
					sql +=" and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				if(sqlInfo1 != null){
					sql +=" and "+sqlInfo1.getSql();
					params.addAll(sqlInfo1.getParams());
				}
				if(orderfield.equals("")){
					orderfield = " end_time ";
				}
				sql += " order by " + orderfield + " nulls last ";
				list = pgOnlyReadService.getAllMap(sql,params);
				getCollector(list);
				getPassName(list);
				getParkname(list);
				getBerthName(list);
				getBerthSegName(list);
			}

			List<List<String>> bodyList = new ArrayList<List<String>>();
			String [] heards = null;
			if(list != null && !list.isEmpty()){
				mongoDbUtils.saveLogs(request,0, 5, "导出订单数量："+list.size()+"条");
				String [] f = new String[]{"id","company_name","berthsec_name","cid","car_number","c_type","create_time","end_time","duration","pay_type","total","prepaid","uid","collector","state","isclick","in_passname","out_passname"};
				heards = new String[]{"订单编号","车场名称","所属泊位段","泊位编号","车牌号","进场方式","进场时间","出场时间","停车时长","支付方式","金额","预付金额","收款人账号","收款人名称","订单状态","结算方式","进场通道","出场通道"};
				for(Map<String, Object> map : list){
					List<String> values = new ArrayList<String>();
					for(String field : f){
						Object v = map.get(field);
						if(v == null)
							v = "";
						if("c_type".equals(field)){
							try{
								switch(Integer.valueOf(v + "")){//0:NFC,1:IBeacon,2:照牌   3通道照牌 4直付 5月卡用户
									case 0:values.add("NFC刷卡");break;
									case 1:values.add("Ibeacon");break;
									case 2:values.add("手机扫牌");break;
									case 3:values.add("通道扫牌");break;
									case 4:values.add("直付");break;
									case 5:values.add("全天月卡");break;
									case 6:values.add("车位二维码");break;
									case 7:values.add("月卡第二辆车");break;
									case 8:values.add("分段月卡");break;
									default:values.add("");
								}
							}catch (Exception e){
								values.add((String) v);
							};

						}else if("duration".equals(field)){
							Long start = (Long)map.get("create_time");
							Long end = (Long)map.get("end_time");
							if(start != null && end != null){
								values.add(StringUtils.getTimeString(start, end));
							}else{
								values.add("");
							}
						}else if("pay_type".equals(field)){
							switch(Integer.valueOf(v + "")){
								case 0:values.add("账户支付");break;
								case 1:values.add("现金支付");break;
								case 2:values.add("手机支付");break;
								case 3:values.add("包月");break;
								case 4:values.add("中央预支付现金");break;
								case 5:values.add("中央预支付银联卡");break;
								case 6:values.add("中央预支付商家卡");break;
								case 8:values.add("免费");break;
								default:values.add("");
							}
						}else if("state".equals(field)){
							switch(Integer.valueOf(v + "")){
								case 0:values.add("未支付");break;
								case 1:values.add("已支付");break;
								case 2:values.add("逃单");break;
								default:values.add("");
							}
						}else if("isclick".equals(field)){
							switch(Integer.valueOf(v + "")){
								case 0:values.add("系统结算");break;
								case 1:values.add("手动结算");break;
								default:values.add("");
							}
						}else if("in_passname".equals(field) || "out_passname".equals(field)){
							values.add(v + "");
						}else if("create_time".equals(field) || "end_time".equals(field)){
							if(!"".equals(v.toString())){
								values.add(TimeTools.getTime_yyyyMMdd_HHmmss(Long.valueOf((v+""))*1000));
							}else{
								values.add("");
							}
						}else if("prepaid".equals(field)){
							if(map.get("prepaid") != null){
								Double prepaid = Double.valueOf(map.get("prepaid") + "");
								if(prepaid > 0){
									values.add(prepaid + "");
								}else{
									values.add("");
								}
							}else{
								values.add("");
							}
						}else{
							values.add(v + "");
						}
					}
					bodyList.add(values);
				}
			}

			String fname = "订单数据" + com.zld.utils.TimeTools.getDate_YY_MM_DD();
			fname = StringUtils.encodingFileName(fname);
			java.io.OutputStream os;
			try {
				response.reset();
				response.setHeader("Content-disposition", "attachment; filename="
						+ fname + ".xls");
				response.setContentType("application/x-download");
				os = response.getOutputStream();
				ExportExcelUtil importExcel = new ExportExcelUtil("订单数据",
						heards, bodyList);
				importExcel.createExcelFile(os);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else if(action.equals("getcollectors")){
			List<Map<String, Object>> collList=null;
			if(cityid>0&&cityid!=null){
				collList= getcollectors(cityid);
			}else if(groupid>0&&groupid!=null) {
				collList= getgroupcollectors(groupid);
			}

			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(collList != null && !collList.isEmpty()){
				for(Map map : collList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("nickname")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}

		return null;
	}

	private List<Map<String, Object>> getcollectors(Long cityid){
		try {
			if(cityid != null && cityid > 0){
				List<Object> idList = commonMethods.getcollctors(cityid);
				if(idList != null && !idList.isEmpty()){
					String preParams  ="";
					for(Object o : idList){
						if(preParams.equals(""))
							preParams ="?";
						else
							preParams += ",?";
					}

					List<Map<String, Object>> collList = pgOnlyReadService.getAllMap("select id,nickname " +
							" from user_info_tb where id in ("+preParams+")", idList);
					return collList;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	private List<Map<String, Object>> getgroupcollectors(Long groupid){
		try {
			if(groupid != null && groupid > 0){
				List<Object> idList = commonMethods.getCollctors(groupid);
				if(idList != null && !idList.isEmpty()){
					String preParams  ="";
					for(Object o : idList){
						if(preParams.equals(""))
							preParams ="?";
						else
							preParams += ",?";
					}

					List<Map<String, Object>> collList = pgOnlyReadService.getAllMap("select id,nickname " +
							" from user_info_tb where id in ("+preParams+")", idList);
					return collList;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		String cid = RequestUtil.processParams(request, "cid");
		SqlInfo sqlInfo1 = null;
		if(!cid.equals("")){
			sqlInfo1 = new SqlInfo(" p.cid like ? ",new Object[]{"%" + cid + "%"});
		}
		return sqlInfo1;
	}

	private List<String> getOrderPic(Long orderId) {
		BasicDBObject conditions = new BasicDBObject();
		conditions.put("orderid", orderId);
		List<String> fileNames = mongoDbUtils.getOrderPicUrls("carstop_pics", conditions);
		return fileNames;
	}

	private void getParkname(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> idList = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> order : list){
				if(!idList.contains(order.get("comid"))){
					idList.add(order.get("comid"));
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
			}

			List<Map<String, Object>> rlist = pgOnlyReadService.getAllMap("select id,company_name from com_info_tb where id in ("
					+ preParams + ") ", idList);
			if(rlist != null && !rlist.isEmpty()){
				for(Map<String, Object> map : list){
					Long comid = (Long)map.get("comid");
					for(Map<String, Object> map2 : rlist){
						Long id = (Long)map2.get("id");
						if(comid.intValue() == id.intValue()){
							map.put("company_name", map2.get("company_name"));
						}
					}
				}
			}
		}
	}

	private void getBerthSegName(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> idList = new ArrayList<Object>();
			String preParams = "";
			for(Map<String, Object> order : list){
				if(!idList.contains(order.get("berthsec_id"))){
					idList.add(order.get("berthsec_id"));
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
			}

			List<Map<String, Object>> rlist = pgOnlyReadService.getAllMap("select id,berthsec_name from com_berthsecs_tb where id in ("
					+ preParams + ") ", idList);
			if(rlist != null && !rlist.isEmpty()){
				for(Map<String, Object> map : list){
					Long berthsec_id = (Long)map.get("berthsec_id");
					if(berthsec_id != null && berthsec_id > 0){
						for(Map<String, Object> map2 : rlist){
							Long id = (Long)map2.get("id");
							if(berthsec_id.intValue() == id.intValue()){
								map.put("berthsec_name", map2.get("berthsec_name"));
							}
						}
					}
				}
			}
		}
	}

	private void getBerthName(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> idList = new ArrayList<Object>();
			String preParams = "";
			for(Map<String, Object> order : list){
				if(!idList.contains(order.get("berthnumber"))){
					idList.add(order.get("berthnumber"));
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
			}

			List<Map<String, Object>> rlist = pgOnlyReadService.getAllMap("select id,cid from com_park_tb where id in ("
					+ preParams + ") ", idList);
			if(rlist != null && !rlist.isEmpty()){
				for(Map<String, Object> map : list){
					Long berthnumber = (Long)map.get("berthnumber");
					if(berthnumber != null && berthnumber > 0){
						for(Map<String, Object> map2 : rlist){
							Long id = (Long)map2.get("id");
							if(berthnumber.intValue() == id.intValue()){
								map.put("cid", map2.get("cid"));
							}
						}
					}
				}
			}
		}
	}

	private void getCollector(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> idList = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> order : list){
				if(!idList.contains(order.get("uid"))){
					idList.add(order.get("out_uid"));
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
			}

			List<Map<String, Object>> rlist = pgOnlyReadService.getAllMap("select id,nickname from user_info_tb where id in ("
					+ preParams + ") ", idList);
			if(rlist != null && !rlist.isEmpty()){
				for(Map<String, Object> map : list){
					Long uid = (Long)map.get("out_uid");
					for(Map<String, Object> map2 : rlist){
						Long id = (Long)map2.get("id");
						if(uid.intValue() == id.intValue()){
							map.put("collector", map2.get("nickname"));
						}
					}
				}
			}
		}
	}

	private void getPassName(List<Map<String, Object>> list) {
		if(list != null && !list.isEmpty()) {
			List<Object> idList = new ArrayList<Object>();
			String preParams = "";
			for (Map<String, Object> order : list) {
				if (!idList.contains(order.get("in_passid"))) {
					if (order.get("in_passid") != null && !"".equals(order.get("in_passid"))) {
						try {
							idList.add(Long.parseLong((String) order.get("in_passid")));
							if (preParams.equals(""))
								preParams = "?";
							else
								preParams += ",?";
						} catch (Exception e) {
							continue;
						}
						;
					}
					//idList.add(order.get("in_passid"));
//					if(preParams.equals(""))
//						preParams ="?";
//					else
//						preParams += ",?";
				}

				if (!idList.contains(order.get("out_passid"))) {
					//idList.add(order.get("out_passid"));
					if (order.get("out_passid") != null && !"".equals(order.get("out_passid"))) {
						try {
							idList.add(Long.parseLong((String) order.get("out_passid")));
							if (preParams.equals(""))
								preParams = "?";
							else
								preParams += ",?";
						} catch (Exception e) {
							continue;
						}
						;
					}
//					if(preParams.equals(""))
//						preParams ="?";
//					else
//						preParams += ",?";
				}
			}
			if (!"".equals(preParams)) {
				List<Map<String, Object>> rList = pgOnlyReadService.getAllMap("select passname,id from com_pass_tb where id in ("
						+ preParams + ")", idList);
				if (rList != null && !rList.isEmpty()) {
					for (Map<String, Object> map : list) {
						Long in_passid = null;
						Long out_passid = null;
	//					Long in_passid = (Long)map.get("in_passid");
	//					Long out_passid = (Long)map.get("out_passid");
						try {
							in_passid = (Long) map.get("in_passid");
							out_passid = (Long) map.get("out_passid");
						} catch (Exception e) {
							continue;
						}
						;
						if (in_passid != null) {
							for (Map<String, Object> map2 : rList) {
								Long id = (Long) map2.get("id");
								if (in_passid.intValue() == id.intValue()) {
									map.put("in_passname", map2.get("passname"));
									break;
								}
							}
						}

						if (out_passid != null) {
							for (Map<String, Object> map2 : rList) {
								Long id = (Long) map2.get("id");
								if (out_passid.intValue() == id.intValue()) {
									map.put("out_passname", map2.get("passname"));
									break;
								}
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void queryShopTicket(List<Map<String, Object>> orderList){
		List<Object> oidList = new ArrayList<Object>();
		if(orderList != null && !orderList.isEmpty()){
			String preParams  ="";
			for(Map<String, Object> order : orderList){
				oidList.add(order.get("id"));
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}

			List<Map<String, Object>> list = pgOnlyReadService.getAllMap("select orderid,sum(umoney) shopmon from ticket_tb where orderid in ("
					+ preParams + ") group by orderid ", oidList);
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : orderList){
					Long id = (Long)map.get("id");
					Double total = 0d;
					if(map.get("total") != null){
						total = Double.valueOf(map.get("total") + "");
					}
					for(Map<String, Object> map2 : list){
						Long orderid = (Long)map2.get("orderid");
						Double shopmon = 0d;
						if(map2.get("shopmon") != null){
							shopmon = Double.valueOf(map2.get("shopmon") + "");
						}
						if(id.intValue() == orderid.intValue()){
							map.put("total", StringUtils.formatDouble(total + shopmon));
							break;
						}
					}
				}
			}
		}
	}
}
