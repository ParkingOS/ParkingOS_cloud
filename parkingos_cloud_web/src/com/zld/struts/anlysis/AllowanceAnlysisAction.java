package com.zld.struts.anlysis;

import com.zld.AjaxUtil;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllowanceAnlysisAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	private Logger logger = Logger.getLogger(AllowanceAnlysisAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		if(comid==null){
			response.sendRedirect("login.do");
			return null;
		}else if(action.equals("")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			request.setAttribute("btime", df2.format(System.currentTimeMillis()));
			request.setAttribute("etime",  df2.format(System.currentTimeMillis()));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			String nowtime= df2.format(System.currentTimeMillis());
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Integer city_b = RequestUtil.getInteger(request, "city_b", 0);
			Integer city_e = RequestUtil.getInteger(request, "city_e", 659004);
			Long b = 0L;
			if(!btime.equals("")){
				b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
			}
			if(etime.equals("")){
				etime = nowtime;
			}
			Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
			//车场拉拉奖
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add("%停车宝排行榜周奖%");
			params.add(b);
			params.add(e);
			params.add(city_b);
			params.add(city_e);
			Map<String, Object> map = pgOnlyReadService
					.getMap("select sum(amount) plala from park_account_tb where type=? and remark like ? and create_time between ? and ? and comid in (select id from com_info_tb where city between ? and ?) ",
							params);
			Double plala = StringUtils.formatDouble(map.get("plala"));
			//收费员拉拉奖
			params.clear();
			params.add(0);
			params.add("%停车宝排行榜周奖%");
			params.add(b);
			params.add(e);
			params.add(city_b);
			params.add(city_e);
			Map<String, Object> map1 = pgOnlyReadService
					.getMap("select sum(p.amount) ulala from parkuser_account_tb p,user_info_tb u where p.uin=u.id and p.type=? and p.remark like ? and p.create_time between ? and ? and u.comid in (select id from com_info_tb where city between ? and ?) ",
							params);
			Double ulala = StringUtils.formatDouble(map1.get("ulala"));
			//拉拉奖额度
			Double lala = plala + ulala;
			//手机支付停车费
			params.clear();
			params.add(0);
			params.add(1);
			params.add(1);
			//params.add(2);
			params.add(b);
			params.add(e);
			params.add(city_b);
			params.add(city_e);
			Map<String, Object> map2 = pgOnlyReadService
					.getMap("select sum(total) mtotal from order_tb where  total>=? and state=? and pay_type=? and end_time between ? and ? and comid in (select id from com_info_tb where city between ? and ?) ",
							params);
			Double mtotal = StringUtils.formatDouble(map2.get("mtotal"));
			//直付停车费
			params.clear();
			params.add(0);
			params.add(1);
			params.add(b);
			params.add(e);
			params.add(city_b);
			params.add(city_e);
			Map<String, Object> map3 = pgOnlyReadService
					.getMap("select sum(a.amount) ztotal from user_account_tb a,user_info_tb u where a.uid=u.id and a.uid>? and a.target=? and a.create_time between ? and ? and u.comid in (select id from com_info_tb where city between ? and ?) ",
							params);
			//直付停车费
			Double ztotal = StringUtils.formatDouble(map3.get("ztotal"));
			//实收停车费
			Double total = mtotal + ztotal;
			//代金券
			params.clear();
			params.add(0);
			params.add(1);
			params.add(b);
			params.add(e);
			params.add(city_b);
			params.add(city_e);
			List<Map<String, Object>> ticketList = new ArrayList<Map<String,Object>>();
			ticketList = pgOnlyReadService.getAllMap("select * from ticket_tb where orderid>? and state=? and utime between ? and ? and comid in (select id from com_info_tb where city between ? and ?) ", params);

			Double normalttotal = 0d;//普通券补贴额
			Double specialttotal = 0d;//专用券补贴额
			Double buyttotal = 0d;//购买券补贴额
			Double wx3ttotal = 0d;//三折券补贴额
			Double wx5ttotal = 0d;//5折券补贴额
			if(ticketList != null && !ticketList.isEmpty()){
				for(Map<String, Object> map4 : ticketList){
					Integer type = (Integer)map4.get("type");
					Integer resources = (Integer)map4.get("resources");
					Integer money = (Integer)map4.get("money");
					Double umoney = 0d;
					Double pmoney = 0d;
					if(map4.get("umoney") != null){
						umoney = Double.valueOf(map4.get("umoney") + "");
					}
					if(map4.get("pmoney") != null){
						pmoney = Double.valueOf(map4.get("pmoney") + "");
					}
					if(type == 0){
						if(resources == 0){//普通券
							normalttotal += umoney;
						}else if(resources == 1){//购买券
							if(umoney > pmoney){//实际抵扣金额-自己购买支付的金额
								buyttotal += (umoney - pmoney);
							}
						}
					}else if(type == 1){
						specialttotal += umoney;
					}else if(type == 2){
						if(money == 3){
							wx3ttotal += umoney;
						}else if(money == 5){
							wx5ttotal += umoney;
						}
					}
				}
			}
			Double rewardttotal = 0d;//打赏补贴额
			params.clear();
			params.add(1);
			params.add(b);
			params.add(e);
			params.add(city_b);
			params.add(city_e);
			List<Map<String, Object>> rewordticketList = new ArrayList<Map<String,Object>>();
			rewordticketList = pgOnlyReadService
					.getAllMap("select t.* from ticket_tb t,parkuser_reward_tb p where t.id = p.ticket_id and t.state=? and p.ctime between ? and ? and p.comid in (select id from com_info_tb where city between ? and ?) ",
							params);
			if(rewordticketList != null && !rewordticketList.isEmpty()){
				for(Map<String, Object> map4 : rewordticketList){
					Integer type = (Integer)map4.get("type");
					Integer resources = (Integer)map4.get("resources");
					Integer money = (Integer)map4.get("money");
					Double umoney = 0d;
					Double pmoney = 0d;
					if(map4.get("umoney") != null){
						umoney = Double.valueOf(map4.get("umoney") + "");
					}
					if(map4.get("pmoney") != null){
						pmoney = Double.valueOf(map4.get("pmoney") + "");
					}
					if(resources == 0){
						rewardttotal += umoney;
					}else if(resources == 1){
						if(umoney > pmoney){
							rewardttotal += (umoney - pmoney);
						}
					}
				}
			}
			Double  ttotal = wx3ttotal + wx5ttotal + normalttotal + specialttotal + buyttotal;
			//车场补贴
			params.clear();
			params.add(2);
			params.add(b);
			params.add(e);
			params.add(city_b);
			params.add(city_e);
			Map<String, Object> map5 = pgOnlyReadService
					.getMap("select sum(amount) pb from park_account_tb where type=? and create_time between ? and ? and comid in (select id from com_info_tb where city between ? and ?) ",
							params);
			Double pb = StringUtils.formatDouble(map5.get("pb"));
			//收费员补贴
			params.clear();
			params.add(0);
			params.add(3);
			params.add(b);
			params.add(e);
			params.add(city_b);
			params.add(city_e);
			Map<String, Object> map6 = pgOnlyReadService
					.getMap("select sum(p.amount) ub from parkuser_account_tb p,user_info_tb u where p.uin=u.id and p.type=? and p.target=? and p.create_time between ? and ? and u.comid in (select id from com_info_tb where city between ? and ?) ",
							params);
			Double ub = StringUtils.formatDouble(map6.get("ub"));
			//车场补贴额
			Double btotal = pb + ub;
			//全部金额
			Double alltotal = lala + total + btotal;
			//拉拉奖的百分比
			String lala_percent = String.format("%.2f", StringUtils.formatDouble(StringUtils.formatDouble(lala)/StringUtils.formatDouble(alltotal))*100)+"%";
			//实收停车费百分比
			String parking_percent = String.format("%.2f", StringUtils.formatDouble(StringUtils.formatDouble(total-ttotal)/StringUtils.formatDouble(alltotal))*100)+"%";
			//停车券补贴额百分比
			String ticket_percent = String.format("%.2f", StringUtils.formatDouble(StringUtils.formatDouble(ttotal)/StringUtils.formatDouble(alltotal))*100)+"%";
			//车场补贴额百分比
			String park_percent = String.format("%.2f", StringUtils.formatDouble(StringUtils.formatDouble(btotal)/StringUtils.formatDouble(alltotal))*100)+"%";

			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			Map<String, Object> newMap = new HashMap<String, Object>();
			newMap.put("lala", StringUtils.formatDouble(lala));
			newMap.put("parking", StringUtils.formatDouble(total - ttotal));
			newMap.put("ticket", StringUtils.formatDouble(ttotal));
			newMap.put("allowance", StringUtils.formatDouble(btotal));
			newMap.put("lala_percent", lala_percent);
			newMap.put("parking_percent", parking_percent);
			newMap.put("ticket_percent", ticket_percent);
			newMap.put("allowance_percent", park_percent);
			newMap.put("normalttotal", StringUtils.formatDouble(normalttotal));
			newMap.put("specialttotal", StringUtils.formatDouble(specialttotal));
			newMap.put("buyttotal", StringUtils.formatDouble(buyttotal));
			newMap.put("wx3ttotal", StringUtils.formatDouble(wx3ttotal));
			newMap.put("wx5ttotal", StringUtils.formatDouble(wx5ttotal));

			newMap.put("rewardttotal", StringUtils.formatDouble(rewardttotal));
			newMap.put("id", 0);
			list.add(newMap);
			int count = list!=null?list.size():0;
			String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("queryformonth")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			String nowtime= df2.format(System.currentTimeMillis());
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Integer city_b = RequestUtil.getInteger(request, "city_b", 0);
			Integer city_e = RequestUtil.getInteger(request, "city_e", 659004);
			logger.error(btime+"=="+etime);
			Long b = 0L;
			if(!btime.equals("")){
				b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
			}
			if(etime.equals("")){
				etime = nowtime;
			}
			Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
			//车场拉拉奖
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add("%停车宝排行榜周奖%");
			params.add(b);
			params.add(e);
//			params.add(city_b);
//			params.add(city_e);
			logger.error("停车宝排行榜周奖");
			Map<String, Object> map = pgOnlyReadService
					.getMap("select sum(amount) plala from park_account_tb where type=? and remark like ? and create_time between ? and ? ",//and comid in (select id from com_info_tb where city between ? and ?) ",
							params);
			Double plala = StringUtils.formatDouble(map.get("plala"));
			//收费员拉拉奖
			params.clear();
			params.add(0);
			params.add("%停车宝排行榜周奖%");
			params.add(b);
			params.add(e);
//			params.add(city_b);
//			params.add(city_e);
			logger.error("收费员拉拉奖");
			Map<String, Object> map1 = pgOnlyReadService
					.getMap("select sum(p.amount) ulala from parkuser_account_tb p,user_info_tb u where p.uin=u.id and p.type=? and p.remark like ? and p.create_time between ? and ? ",//and u.comid in (select id from com_info_tb where city between ? and ?) ",
							params);
			Double ulala = StringUtils.formatDouble(map1.get("ulala"));
			//拉拉奖额度
			Double lala = plala + ulala;
			//手机支付停车费
			params.clear();
			params.add(4);
			params.add(1);
			params.add(1);
			params.add(2);
			params.add(b);
			params.add(e);
//			params.add(city_b);
//			params.add(city_e);
			logger.error("手机支付停车费");
			Map<String, Object> map2 = pgOnlyReadService
					.getMap("select sum(total) mtotal from order_tb where c_type!=? and total>=? and state=? and pay_type=? and end_time between ? and ? ",//and comid in (select id from com_info_tb where city between ? and ?) ",
							params);
			Double mtotal = StringUtils.formatDouble(map2.get("mtotal"));
			//直付停车费
			params.clear();
			params.add(0);
			params.add(1);
			params.add(b);
			params.add(e);
//			params.add(city_b);
//			params.add(city_e);
			logger.error("直付停车费");
			Map<String, Object> map3 = pgOnlyReadService
					.getMap("select sum(a.amount) ztotal from user_account_tb a,user_info_tb u where a.uid=u.id and a.uid>? and a.target=? and a.create_time between ? and ? ",//and u.comid in (select id from com_info_tb where city between ? and ?) ",
							params);
			//直付停车费
			Double ztotal = StringUtils.formatDouble(map3.get("ztotal"));
			//实收停车费
			Double total = mtotal + ztotal;
			//代金券
			params.clear();
			params.add(0);
			params.add(1);
			params.add(b);
			params.add(e);
//			params.add(city_b);
//			params.add(city_e);
			logger.error("代金券");
			List<Map<String, Object>> ticketList = new ArrayList<Map<String,Object>>();
			ticketList = pgOnlyReadService.getAllMap("select * from ticket_tb where orderid>? and state=? and utime between ? and ? ",//and comid in (select id from com_info_tb where city between ? and ?) ",
					params);

			Double normalttotal = 0d;//普通券补贴额
			Double specialttotal = 0d;//专用券补贴额
			Double buyttotal = 0d;//购买券补贴额
			Double wx3ttotal = 0d;//三折券补贴额
			Double wx5ttotal = 0d;//5折券补贴额
			if(ticketList != null && !ticketList.isEmpty()){
				for(Map<String, Object> map4 : ticketList){
					Integer type = (Integer)map4.get("type");
					Integer resources = (Integer)map4.get("resources");
					Integer money = (Integer)map4.get("money");
					Double umoney = 0d;
					Double pmoney = 0d;
					if(map4.get("umoney") != null){
						umoney = Double.valueOf(map4.get("umoney") + "");
					}
					if(map4.get("pmoney") != null){
						pmoney = Double.valueOf(map4.get("pmoney") + "");
					}
					if(type == 0){
						if(resources == 0){//普通券
							normalttotal += umoney;
						}else if(resources == 1){//购买券
							if(umoney > pmoney){//实际抵扣金额-自己购买支付的金额
								buyttotal += (umoney - pmoney);
							}
						}
					}else if(type == 1){
						specialttotal += umoney;
					}else if(type == 2){
						if(money == 3){
							wx3ttotal += umoney;
						}else if(money == 5){
							wx5ttotal += umoney;
						}
					}
				}
			}
			Double rewardttotal = 0d;//打赏补贴额
			params.clear();
			params.add(1);
			params.add(b);
			params.add(e);
//			params.add(city_b);
//			params.add(city_e);
			logger.error("打赏补贴额");
			List<Map<String, Object>> rewordticketList = new ArrayList<Map<String,Object>>();
			rewordticketList = pgOnlyReadService
					.getAllMap("select t.* from ticket_tb t,parkuser_reward_tb p where t.id = p.ticket_id and t.state=? and p.ctime between ? and ? ",//and p.comid in (select id from com_info_tb where city between ? and ?) ",
							params);
			if(rewordticketList != null && !rewordticketList.isEmpty()){
				for(Map<String, Object> map4 : rewordticketList){
					Integer type = (Integer)map4.get("type");
					Integer resources = (Integer)map4.get("resources");
					Integer money = (Integer)map4.get("money");
					Double umoney = 0d;
					Double pmoney = 0d;
					if(map4.get("umoney") != null){
						umoney = Double.valueOf(map4.get("umoney") + "");
					}
					if(map4.get("pmoney") != null){
						pmoney = Double.valueOf(map4.get("pmoney") + "");
					}
					if(resources == 0){
						rewardttotal += umoney;
					}else if(resources == 1){
						if(umoney > pmoney){
							rewardttotal += (umoney - pmoney);
						}
					}
				}
			}
			Double  ttotal = wx3ttotal + wx5ttotal + normalttotal + specialttotal + buyttotal;
			//车场补贴
			params.clear();
			params.add(2);
			params.add(b);
			params.add(e);
//			params.add(city_b);
//			params.add(city_e);
			logger.error("车场补贴");
			Map<String, Object> map5 = pgOnlyReadService
					.getMap("select sum(amount) pb from park_account_tb where type=? and create_time between ? and ? ",//and comid in (select id from com_info_tb where city between ? and ?) ",
							params);
			Double pb = StringUtils.formatDouble(map5.get("pb"));
			//收费员补贴
			params.clear();
			params.add(0);
			params.add(3);
			params.add(b);
			params.add(e);
//			params.add(city_b);
//			params.add(city_e);
			logger.error("收费员补贴");
			Map<String, Object> map6 = pgOnlyReadService
					.getMap("select sum(p.amount) ub from parkuser_account_tb p,user_info_tb u where p.uin=u.id and p.type=? and p.target=? and p.create_time between ? and ? ",//and u.comid in (select id from com_info_tb where city between ? and ?) ",
							params);
			Double ub = StringUtils.formatDouble(map6.get("ub"));
			//车场补贴额
			Double btotal = pb + ub;
			//全部金额
			Double alltotal = lala + total + btotal;
			//拉拉奖的百分比
			String lala_percent = String.format("%.2f", StringUtils.formatDouble(StringUtils.formatDouble(lala)/StringUtils.formatDouble(alltotal))*100)+"%";
			//实收停车费百分比
			String parking_percent = String.format("%.2f", StringUtils.formatDouble(StringUtils.formatDouble(total-ttotal)/StringUtils.formatDouble(alltotal))*100)+"%";
			//停车券补贴额百分比
			String ticket_percent = String.format("%.2f", StringUtils.formatDouble(StringUtils.formatDouble(ttotal)/StringUtils.formatDouble(alltotal))*100)+"%";
			//车场补贴额百分比
			String park_percent = String.format("%.2f", StringUtils.formatDouble(StringUtils.formatDouble(btotal)/StringUtils.formatDouble(alltotal))*100)+"%";

			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			Map<String, Object> newMap = new HashMap<String, Object>();
//			newMap.put("lala", StringUtils.formatDouble(lala));
//			newMap.put("parking", StringUtils.formatDouble(total - ttotal));
			newMap.put("ticket", StringUtils.formatDouble(ttotal));
			newMap.put("allowance", StringUtils.formatDouble(btotal));
//			newMap.put("lala_percent", lala_percent);
//			newMap.put("parking_percent", parking_percent);
//			newMap.put("ticket_percent", ticket_percent);
//			newMap.put("allowance_percent", park_percent);
//			newMap.put("normalttotal", StringUtils.formatDouble(normalttotal));
//			newMap.put("specialttotal", StringUtils.formatDouble(specialttotal));
//			newMap.put("buyttotal", StringUtils.formatDouble(buyttotal));
//			newMap.put("wx3ttotal", StringUtils.formatDouble(wx3ttotal));
//			newMap.put("wx5ttotal", StringUtils.formatDouble(wx5ttotal));
//
//			newMap.put("rewardttotal", StringUtils.formatDouble(rewardttotal));
//			newMap.put("id", 0);

			/*
			 * 每半月订单总量
			 * */
			Long orderCount = pgOnlyReadService.getLong("select count(id) from order_tb where state=? and pay_type=? and  " +
					"create_time between ? and ? ", new Object[]{1,2,b,e});
			newMap.put("ordercount", orderCount);
			newMap.put("rate", StringUtils.formatDouble(StringUtils.formatDouble(ttotal)+StringUtils.formatDouble(btotal))/orderCount+" 元/单");
			newMap.put("date", btime+"至"+etime);
			list.add(newMap);
			int count = list!=null?list.size():0;
			//String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
			String json =StringUtils.createJson(newMap);
			AjaxUtil.ajaxOutput(response, json);
		}
		return null;
	}

	private double getWxAllowance(List<Map<String, Object>> orderList){
		double allowance = 0d;
		DecimalFormat dFormat = new DecimalFormat("#.00");
		if(orderList!= null && !orderList.isEmpty()){
			for(Map<String, Object> map : orderList){
				if(map.get("total") != null){
					Double total = Double.valueOf(map.get("total") + "");
					if(total >24){
						allowance += 12d;
					}else{
						allowance += Double.valueOf(dFormat.format(total*0.5));
					}
				}
			}
		}
		allowance = Double.valueOf(dFormat.format(allowance));
		return allowance;
	}
}
