package com.zld.struts.group;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.zld.AjaxUtil;
import com.zld.impl.MongoClientFactory;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 停车场后台管理员登录后，查看订单，不能修改和删除
 * @author Administrator
 *
 */
public class GroupOrderManageAction extends Action{

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private PublicMethods PublicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	private Logger logger = Logger.getLogger(GroupOrderManageAction.class);

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		Integer role = RequestUtil.getInteger(request, "role",-1);
		Integer authId = RequestUtil.getInteger(request, "authid",-1);
		request.setAttribute("authid", authId);
		request.setAttribute("role", role);
		Integer otype = RequestUtil.getInteger(request, "otype", -1);
		Integer isHd = (Integer)request.getSession().getAttribute("ishdorder");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		if(comid == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(comid==0)
			comid = RequestUtil.getLong(request, "comid", 0L);
		if(groupid != null && groupid > 0){
			request.setAttribute("groupid", groupid);
			if(comid == null || (comid <= 0&&comid!=-2)){
				Map map = pgOnlyReadService.getMap("select id,company_name from com_info_tb where groupid=? order by id limit ? ",
						new Object[]{groupid, 1});
				if(map != null){
					comid = (Long)map.get("id");
				}else{
					comid = -999L;
				}
			}
		}

		int total = 0;
		int month = 0;
		int parktotal = 0;
		int blank = 0;
		String allSql = "select count(*)total from order_tb where comid = ? and state=?";
		Object [] allparm =  new Object[]{comid,0};
		String monthSql = "select count(*)total from order_tb where comid = ? and state=? and c_type=? ";
		Object [] monthparm =  new Object[]{comid,0,5};
		if(isHd==1){//限制
			allSql +=" and ishd<>? ";
			allparm = new Object[]{comid,0,1};
			monthSql +=" and ishd<>? ";
			monthparm =new Object[]{comid,0,5,1};
		}
		Map allmap = pgOnlyReadService.getMap(allSql,allparm);
		Map monthmap = pgOnlyReadService.getMap(monthSql,monthparm);
		Map cominfo = pgOnlyReadService.getMap("select share_number,parking_total from com_info_tb where id = ? ", new Object[]{comid});
		if(allmap!=null&&allmap.get("total")!=null)
			total = Integer.valueOf(allmap.get("total")+"");
		if(monthmap!=null&&monthmap.get("total")!=null)
			month = Integer.valueOf(monthmap.get("total")+"");
		if(cominfo!=null){
			Integer parking_total = 0;
			if(cominfo.get("parking_total") != null){
				parking_total=(Integer)cominfo.get("parking_total");//车场车位数
			}
			Integer shareNumber = 0;
			if(cominfo.get("share_number") != null){
				shareNumber=(Integer)cominfo.get("share_number");//车场车位分享数
			}
			if(shareNumber > 0){
				parktotal = shareNumber;
			}else{
				parktotal = parking_total;
			}
		}
		blank = parktotal-total;
		if(blank<=0)
			blank=0;
		request.setAttribute("parkinfo", "" );//AjaxUtil.decodeUTF8("车位统计:场内停车"+total+"辆,其中月卡车"+month+"辆,临停车"+(total-month)+"辆,空车位"+blank+"辆"));
		if(action.equals("")){
			request.setAttribute("comid", comid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			List arrayList = query(request,groupid,isHd,otype);
			List list = (List<Map<String, Object>>) arrayList.get(0);
			Integer pageNum = (Integer) arrayList.get(1);
			long count = Long.valueOf(arrayList.get(2)+"");
			String fieldsstr = arrayList.get(3)+"";
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("exportExcel")){
			Map uin = (Map)request.getSession().getAttribute("userinfo");
			if(uin!=null&&uin.get("auth_flag")!=null){
				if(Integer.valueOf(uin.get("auth_flag")+"")==ZLDType.ZLD_ACCOUNTANT_ROLE||Integer.valueOf(uin.get("auth_flag")+"")==ZLDType.ZLD_CARDOPERATOR){
					String ret = "没有权限导出订单数据";
					logger.error(">>>>"+ret);
					AjaxUtil.ajaxOutput(response,ret);
					return null;
				}
			}
			List arrayList = query(request,groupid,isHd,otype);
			List<Map<String, Object>> list = (List<Map<String, Object>>) arrayList.get(0);
			List<List<String>> bodyList = new ArrayList<List<String>>();
			String [] heards = null;
			if(list!=null&&list.size()>0){
				mongoDbUtils.saveLogs( request,0, 5, "导出订单数量："+list.size()+"条");
				//setComName(list);
				String [] f = new String[]{"id","c_type","car_number","create_time","end_time","duration","pay_type","total","uid","state","isclick","in_passid","out_passid"};
				heards = new String[]{"编号","进场方式","车牌号","进场时间","出场时间","时长","支付方式","金额","收款人","状态","结算方式","进场通道","出场通道"};
				Map<Long, String> uinNameMap = new HashMap<Long, String>();
				Map<Integer, String> passNameMap = new HashMap<Integer, String>();
				for(Map<String, Object> map : list){
					List<String> values = new ArrayList<String>();
					for(String field : f){
						Object v =map.get(field);
						if(v==null)
							v="";
						if("uid".equals(field)){
							Long uid = -1L;
							if(Check.isLong(v+""))
								uid = Long.valueOf(v+"");
							if(uinNameMap.containsKey(uid))
								values.add(uinNameMap.get(uid));
							else{
								String name = getUinName(Long.valueOf(map.get(field)+""));
								values.add(name);
								uinNameMap.put(uid, name);
							}
						}else if("c_type".equals(field)){
							switch(Integer.valueOf(v+"")){//0:NFC,1:IBeacon,2:照牌   3通道照牌 4直付 5月卡用户
								case 0:values.add("NFC刷卡");break;
								case 1:values.add("Ibeacon");break;
								case 2:values.add("手机扫牌");break;
								case 3:values.add("通道扫牌");break;
								case 4:values.add("直付");break;
								case 5:values.add("月卡");break;
								default:values.add("");
							}
						}else if("duration".equals(field)){
							Long start = (Long)map.get("create_time");
							Long end = (Long)map.get("end_time");
							if(start!=null&&end!=null){
								values.add(StringUtils.getTimeString(start, end));
							}else{
								values.add("");
							}
						}else if("pay_type".equals(field)){
							switch(Integer.valueOf(v+"")){//0:NFC,1:IBeacon,2:照牌   3通道照牌 4直付 5月卡用户
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
							switch(Integer.valueOf(v+"")){//0:NFC,1:IBeacon,2:照牌   3通道照牌 4直付 5月卡用户
								case 0:values.add("未支付");break;
								case 1:values.add("已支付");break;
								case 2:values.add("逃单");break;
								default:values.add("");
							}
						}else if("isclick".equals(field)){
							switch(Integer.valueOf(map.get(field)+"")){//0:NFC,1:IBeacon,2:照牌   3通道照牌 4直付 5月卡用户
								case 0:values.add("系统结算");break;
								case 1:values.add("手动结算");break;
								default:values.add("");
							}
						}else if("in_passid".equals(field)||"out_passid".equals(field)){
							if(!"".equals(v.toString())&&Check.isNumber(v.toString())){
								Integer passId = Integer.valueOf(v.toString());
								if(passNameMap.containsKey(passId))
									values.add(passNameMap.get(passId));
								else {
									String passName = getPassName(comid, passId);
									values.add(passName);
									passNameMap.put(passId, passName);
								}
							}else{
								values.add("");
							}
						}else{
							if("create_time".equals(field)||"end_time".equals(field)){
								if(!"".equals(v.toString())){
									values.add(TimeTools.getTime_yyyyMMdd_HHmmss(Long.valueOf((v+""))*1000));
								}else{
									values.add("null");
								}
							}else{
								values.add(v+"");
							}
						}
					}
					bodyList.add(values);
				}
			}
			String fname = "订单数据" + TimeTools.getDate_YY_MM_DD();
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
//			String json = "";
//			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("completezeroorder")){
			String ids =RequestUtil.processParams(request, "ids");
			int ret = 0;
			if(StringUtils.isNotNull(ids)){
				String[] idsarr = ids.split(",");
				long etime = System.currentTimeMillis()/1000;
				for (int i = 0; i < idsarr.length; i++) {
					long id = Long.valueOf(idsarr[i]);
					if(PublicMethods.isEtcPark(comid)){
						etime+=1;
						//非月卡
						ret=daService.update("update order_tb set total=?,pay_type=?,end_time=?,state=?,need_sync=? where id=? and state=? and c_type<>?", new Object[]{0.0,1,etime,1,4,id,0,5});
						if(ret>0){
							mongoDbUtils.saveLogs( request,0, 6, "带本地服务器的后台0元结算非月卡订单："+id );
							logger.error("带本地服务器的后台0元结算非月卡订单："+id +",结算方式pay_type：1");
						}else {
							//月卡
							ret=daService.update("update order_tb set total=?,pay_type=?,end_time=?,state=?,need_sync=? where id=? and state=? and c_type=?", new Object[]{0.0,3,etime,1,4,id,0,5});
							if(ret>0){
								mongoDbUtils.saveLogs( request,0, 6, "带本地服务器的后台0元结算月卡订单："+id);
								logger.error("带本地服务器的后台后台0元结算月卡订单："+id +",结算方式pay_type：3");
							}
						}
					}else{
						//非月卡
						ret=daService.update("update order_tb set total=?,pay_type=?,end_time=?,state=? where id=? and state=? and c_type<>?", new Object[]{0.0,1,etime,1,id,0,5});
						if(ret>0){
							mongoDbUtils.saveLogs( request,0, 6, "后台0元结算非月卡订单："+id );
							logger.error("后台0元结算非月卡订单："+id +",结算方式pay_type：1");
						}else {
							//月卡
							ret=daService.update("update order_tb set total=?,pay_type=?,end_time=?,state=? where id=? and state=? and c_type=?", new Object[]{0.0,3,etime,1,id,0,5});
							if(ret>0){
								mongoDbUtils.saveLogs( request,0, 6, "后台0元结算月卡订单："+id);
								logger.error("后台0元结算月卡订单："+id +",结算方式pay_type：3");
							}
						}
					}

				}
			}
			AjaxUtil.ajaxOutput(response, ret+"");
		}else if(action.equals("edit")){
			String nickname =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname"));
			String strid =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "strid"));
			String phone =RequestUtil.processParams(request, "phone");
			String mobile =RequestUtil.processParams(request, "mobile");
			String id =RequestUtil.processParams(request, "id");
			String sql = "update order_tb set nickname=?,strid=?,phone=?,mobile=? where uin=?";
			Object [] values = new Object[]{nickname,strid,phone,mobile,Long.valueOf(id)};
			int result = daService.update(sql, values);
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("delete")){
			String id =RequestUtil.processParams(request, "selids");
			String sql = "delete from user_info where id =?";
			Object [] values = new Object[]{Long.valueOf(id)};
			int result = daService.update(sql, values);
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("carpics")){
			Long orderid = RequestUtil.getLong(request, "orderid", -1L);
			DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");
			DBCollection collection = db.getCollection("car_inout_pics");
			BasicDBObject document = new BasicDBObject();
			document.put("orderid", orderid);
			document.put("gate", 0);
//			DBCursor objsin = collection.find(document);
//
//			int insize = objsin.size();
//			objsin.close();
			Long insize  = collection.count(document);
			document.put("gate", 1);
//			DBCursor objsout = collection.find(document);
//			int outsize = objsout.size();
//			objsout.close();
			Long outsize =collection.count(document);

			if(insize==0&&outsize==0){//查不到时查另外一张表
				collection = db.getCollection("car_hd_pics");
				outsize =collection.count(document);
				document.put("gate", 0);
				insize  = collection.count(document);
				logger.error("mongodb>>>>>>>>>>>car_inout_pics表中没有，从car_hd_pics表中查询"+insize+","+outsize);
			}

			String inhtml = "<img src='carpicsup.do?action=downloadpic&comid=0&type=0&orderid="+orderid+"' id='p1' width='600px' height='600px'></img>";
			String outhtml = "<img src='carpicsup.do?action=downloadpic&comid=0&type=1&orderid="+orderid+"' id='p1' width='600px' height='600px'></img>";
			if(insize>1){
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i <insize ; i++) {
					sb.append("<img src='carpicsup.do?action=downloadpic&comid=0&type=0&currentnum="+i+"&orderid="+orderid+"' id='p1' width='600px' height='600px'></img>").append("<br/><br/>");
				}
				inhtml = sb.toString();
			}
			if(outsize>1){
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i <outsize ; i++) {
					sb.append("<img src='carpicsup.do?action=downloadpic&comid=0&type=1&currentnum="+i+"&orderid="+orderid+"' id='p1' width='600px' height='600px'></img>").append("<br/><br/>");
				}
				outhtml = sb.toString();
			}
			request.setAttribute("inhtml", inhtml);
			request.setAttribute("outhtml", outhtml);
//			request.setAttribute("orderid", orderid);
			return mapping.findForward("carpics");
		}else if(action.equals("getalluser")){
			List<Map> tradsList = daService.getAll("select id,nickname from user_info_tb where (comid=? or groupid=?) and state=? and auth_flag in(?,?)",
					new Object[]{comid, groupid, 0, 1, 2});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"全部\"}";
			if(tradsList!=null&&tradsList.size()>0){
				for(Map map : tradsList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("nickname")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getfreereasons")){
			List<Map> tradsList = daService.getAll("select id,name from free_reasons_tb where comid=? ",
					new Object[]{comid});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\" \"}";
			if(tradsList!=null&&tradsList.size()>0){
				for(Map map : tradsList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("name")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getcollectors")){
			List<Map<String, Object>> collList = pgOnlyReadService.getAll("select id,nickname from user_info_tb where comid in" +
					" (select * from com_info_tb where groupid =?)", new Object[]{groupid});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(collList != null && !collList.isEmpty()){
				for(Map map : collList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("nickname")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
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
		}else if(action.equals("getcid")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Map<String, Object> map = null;
			if(id>-1){
				map = pgOnlyReadService.getMap("select cid from com_park_tb where id=? ",
						new Object[]{id});
			}
			String name = "";
			if(map != null && map.get("cid") != null){
				name = (String)map.get("cid");
			}
			AjaxUtil.ajaxOutput(response, name);
		}

		return null;
	}

	private String getUinName(Long uin) {
		Map list = pgOnlyReadService.getPojo("select * from user_info_tb where id =?  ",new Object[]{uin});
		String uinName = "";
		if(list!=null&&list.get("nickname")!=null){
			uinName = list.get("nickname")+"";
		}
		return uinName;
	}

	private String getPassName(Long comId,Integer passId) {
		String sql = "select passname from com_pass_tb where comid=? and id = ?";
		Map m = pgOnlyReadService.getPojo(sql, new Object[]{comId,passId});
		if(m!=null){
			return m.get("passname")+"";
		}
		return "";
	}

	private List query(HttpServletRequest request,long groupid,Integer isHd,Integer otype){
		ArrayList arrayList = new ArrayList();
		String orderfield = RequestUtil.processParams(request, "orderfield");
		String orderby = RequestUtil.processParams(request, "orderby");
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		String sql = "select *,uid as uidname from order_tb where groupid=?  ";
		if(comid>0){
			sql = "select *,uid as uidname from order_tb where comid=?  ";
			groupid = comid;
		}
		if(orderfield.equals("")){
			orderfield = " end_time ";
		}else if(orderfield.equals("duration")){
			sql = "select *,(end_time-create_time) as duration from order_tb where groupid=?  ";
		}
		if(orderby.equals("")){
			orderby = " desc nulls last ";
		}else {
			orderby +=" nulls last";
		}

		String countSql = "select count(*) from order_tb where  groupid=? " ;
		if(comid>0){
			countSql = "select count(*) from order_tb where  comid=? ";
		}
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
		String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
		SqlInfo base = new SqlInfo("1=1", new Object[]{groupid});

		if(isHd==1){
			if(otype>-1){
				countSql +=" and ishd=? and state=? and isclick=? ";
				sql      +=" and ishd=? and state=? and isclick=? ";
				base = new SqlInfo("1=1", new Object[]{groupid,0,1,otype});
			}else {
				countSql +=" and ishd=? ";
				sql      +=" and ishd=? ";
				base = new SqlInfo("1=1", new Object[]{groupid,0});
			}
		}else {
			if(otype>-1){
				countSql +=" and state=? and isclick=? ";
				sql      +=" and state=? and isclick=? ";
				base = new SqlInfo("1=1", new Object[]{groupid,1,otype});
			}
		}

		SqlInfo sqlInfo = RequestUtil.customSearch(request,"order_tb");
		List<Object> params =new ArrayList<Object>();

		if(sqlInfo!=null){
			sqlInfo = SqlInfo.joinSqlInfo(base,sqlInfo, 2);
			String infoSql = sqlInfo.getSql();
			if(infoSql!=null&&infoSql.length()>0){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
			}
			params = sqlInfo.getParams();
		}else {
			params= base.getParams();
		}
		sql += " order by " + orderfield + " " + orderby+" ,id desc";
		//System.out.println(sqlInfo);
		Long count= pgOnlyReadService.getCount(countSql, params);
		List<Map<String, Object>> list = null;//pgOnlyReadService.getPage(sql, null, 1, 20);
		if(count>0){
			list = pgOnlyReadService.getAll(sql, params, pageNum, pageSize);
			List<Object> orderidList = new ArrayList<Object>();
			for(Map<String, Object> map : list){
				orderidList.add(map.get("id"));
				Integer isClick =(Integer)map.get("isclick");
				Integer state = (Integer)map.get("state");
				if(state==null||state==0){
					if(isClick==0)
						map.put("isclick", -1);
				}
			}
			List<Map<String, Object>> shopTicketList = queryShopTicket(orderidList);
			if(shopTicketList != null && !shopTicketList.isEmpty()){
				for(Map<String, Object> map : list){
					Long id = (Long)map.get("id");

					Double total = 0d;
					if(map.get("total") != null){
						total = Double.valueOf(map.get("total") + "");
					}
					for(Map<String, Object> map2 : shopTicketList){
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
		arrayList.add(list);
		arrayList.add(pageNum);
		arrayList.add(count);
		arrayList.add(fieldsstr);
		return arrayList;
	}

	private List<Map<String, Object>> queryShopTicket(List<Object> orderidList){
		if(orderidList != null && !orderidList.isEmpty()){
			String preParams  ="";
			for(Object orderid : orderidList){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}

			List<Map<String, Object>> list = pgOnlyReadService.getAllMap("select orderid,sum(umoney) shopmon from ticket_tb where orderid in ("
					+ preParams + ") group by orderid ", orderidList);
			return list;

		}
		return null;
	}
}