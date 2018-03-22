package com.zld.struts.anlysis;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
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

/**
 * 车场订单统计
 * @author Administrator
 *
 */
public class ParkOrderanlysisAction extends Action {

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;

	private Logger logger = Logger.getLogger(ParkOrderanlysisAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		Integer role = RequestUtil.getInteger(request, "role",-1);
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Integer isHd = (Integer)request.getSession().getAttribute("ishdorder");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		if(ZLDType.ZLD_ACCOUNTANT_ROLE==role||ZLDType.ZLD_CARDOPERATOR==role)
			request.setAttribute("role", role);
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}

		if(comid == 0){
			comid = RequestUtil.getLong(request, "comid", 0L);
		}
		request.setAttribute("groupid", groupid);
		request.setAttribute("cityid", cityid);
		if(comid == 0){
			comid = getComid(comid, cityid, groupid);
		}
		if(action.equals("")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			request.setAttribute("btime", df2.format(System.currentTimeMillis()));
			request.setAttribute("etime",  df2.format(System.currentTimeMillis()));
			request.setAttribute("comid", comid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			/*原来统计分析中查询的收费员是uid，改为查询出场收费员out_uid的信息 by lqb 2017-05-27*/
			/*
			 *
			 */

			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			String nowtime= df2.format(System.currentTimeMillis());
			String type = RequestUtil.processParams(request, "type");
			String sql = "select count(*) scount,sum(amount_receivable) amount_receivable, " +
					"sum(total) total , sum(cash_pay) cash_pay,sum(cash_prepay) cash_prepay, sum(electronic_pay) electronic_pay,sum(electronic_prepay) electronic_prepay, " +
					"sum(reduce_amount) reduce_pay, out_uid,comid from order_tb  ";
			String free_sql = "select count(*) scount,sum(amount_receivable-electronic_prepay-cash_prepay-reduce_amount) free_pay,out_uid,comid from order_tb";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			if(btime.equals(""))
				btime = nowtime + " 00:00:00";
			if(etime.equals(""))
				etime = nowtime;
			SqlInfo sqlInfo =null;
			List<Object> params = null;
			Long b = TimeTools.getToDayBeginTime();
			Long e = System.currentTimeMillis()/1000;
			String dstr = btime+"-"+etime;
			if(type.equals("today")){
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
				dstr = "今天";
			}else if(type.equals("toweek")){
				b = TimeTools.getWeekStartSeconds();
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
				dstr = "本周";
			}else if(type.equals("lastweek")){
				e = TimeTools.getWeekStartSeconds();
				b= e-7*24*60*60;
				e = e-1;
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
				dstr = "上周";
			}else if(type.equals("tomonth")){
				b=TimeTools.getMonthStartSeconds();
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
				dstr="本月";
			}else if(!btime.equals("")&&!etime.equals("")){
				b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime);
				e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
			}
			sql +=" where "+sqlInfo.getSql()+" and comid=?  and state= ? and out_uid> ? and ishd=? ";
			free_sql +=" where "+sqlInfo.getSql()+" and comid=?  and state= ? and out_uid> ? and ishd=? ";
			List<Object> subParams =new ArrayList<Object>();
			params= sqlInfo.getParams();
			for(Object object :params){
				subParams.add(object);
			}
			params.add(comid);
			params.add(1);
			params.add(-1);
			params.add(0);
			//总订单集合
			List<Map<String, Object>> totalList = pgOnlyReadService.getAllMap(sql +" group by out_uid,comid order by scount desc ",params);
			//月卡订单集合
			List<Map<String, Object>> monthList = pgOnlyReadService.getAllMap(sql +" and pay_type=3 group by out_uid,comid order by scount desc ",params);
			//免费订单集合
			List<Map<String, Object>> freeList = pgOnlyReadService.getAllMap(free_sql +" and pay_type=8 group by out_uid,comid order by scount desc ",params);
			int totalCount = 0;//总订单数
			int monthCount = 0;
			double cashpay = 0.0;//现金结算
			double cashprepay = 0.0;//现金预付
			double totalMoney = 0.0;//订单金额
			double cashMoney = 0.0;//现金支付金额
			double elecMoney = 0.0;//电子支付金额
			double freeMoney = 0.0;//免费金额
			double reduce_amount = 0.0;//减免支付
			List<Map<String, Object>> backList = new ArrayList<Map<String, Object>>();
			if(totalList != null && totalList.size() > 0){
				Map<Long ,String> nameMap =new HashMap<>();
				for(Map<String, Object> totalOrder : totalList){
					Long _comid = (Long)totalOrder.get("comid");
					String names = nameMap.get(_comid);
					if(names==null){
						Map<String,Object> namesMap = daService.getMap("select c.company_name,g.name from com_info_tb c left join" +
								" org_group_tb g on c.groupid = g.id where c.id =?",new Object[]{_comid});
						logger.error(namesMap);
						if(namesMap!=null&&!namesMap.isEmpty()){
							nameMap.put(_comid,namesMap.get("company_name")+"bolink"+namesMap.get("name"));
							totalOrder.put("comid",namesMap.get("company_name"));
							totalOrder.put("groupid",namesMap.get("name"));
						}else{
							nameMap.put(_comid,"bolink");
						}
					}else{
						totalOrder.put("comid",names.split("bolink")[0]);
						totalOrder.put("groupid",names.split("bolink")[1]);
					}
					totalCount += Integer.parseInt(totalOrder.get("scount")+"");
					totalMoney += Double.parseDouble(totalOrder.get("amount_receivable")+"");
					//设定默认值   名字这个 全部按照user_id来处理
					String sql_worker = "select nickname from user_info_tb where id = ?";
//					String sql_worker = "select nickname from user_info_tb where user_id = ? and comid = ? and state =0";
					Object []val_worker = new Object[]{Long.parseLong(totalOrder.get("out_uid")+"")};
//					Object []val_worker = new Object[]{totalOrder.get("out_uid")+"",comid};
					Map worker = daService.getMap(sql_worker ,val_worker);
					if(worker!=null && worker.containsKey("nickname")){
						//出场收费员Id
						totalOrder.put("id",totalOrder.get("out_uid"));
						//收费员名称
						totalOrder.put("name",worker.get("nickname"));
					}
					//时间段
					totalOrder.put("sdate",dstr);
					//月卡订单数
					totalOrder.put("monthcount",0);
					//遍历月卡集合
					if(monthList != null && monthList.size() > 0){
						for(Map<String, Object> monthOrder : monthList){
							if(totalOrder.get("out_uid").equals(monthOrder.get("out_uid"))){
								monthCount += Integer.parseInt(monthOrder.get("scount")+"");
								totalOrder.put("monthcount", monthOrder.get("scount"));
							}
						}
					}
					//现金结算
					cashpay += StringUtils.formatDouble(totalOrder.get("cash_pay"));
					totalOrder.put("cash_pay",String.format("%.2f",StringUtils.formatDouble(totalOrder.get("cash_pay"))));
					//现金预付
					cashprepay += StringUtils.formatDouble(totalOrder.get("cash_prepay"));
					totalOrder.put("cash_prepay",String.format("%.2f",StringUtils.formatDouble(totalOrder.get("cash_prepay"))));

//					cashMoney +=StringUtils.formatDouble(totalOrder.get("cash_pay"))+StringUtils.formatDouble(totalOrder.get("cash_prepay"));
//					totalOrder.put("cash_pay",String.format("%.2f",StringUtils.formatDouble(totalOrder.get("cash_pay"))+StringUtils.formatDouble(totalOrder.get("cash_prepay"))));
					//电子支付

					elecMoney += StringUtils.formatDouble(totalOrder.get("electronic_pay"))+StringUtils.formatDouble(totalOrder.get("electronic_prepay"));
					totalOrder.put("electronic_pay", String.format("%.2f",StringUtils.formatDouble(totalOrder.get("electronic_pay"))+StringUtils.formatDouble(totalOrder.get("electronic_prepay"))));
					//免费支付
					totalOrder.put("free_pay",0.0);
					//遍历免费集合
					if(freeList != null && freeList.size() > 0){
						for(Map<String, Object> freeOrder : freeList){
							if(totalOrder.get("out_uid").equals(freeOrder.get("out_uid"))){
								freeMoney += Double.parseDouble((freeOrder.get("free_pay")== null ? "0" : freeOrder.get("free_pay")+""));
								totalOrder.put("free_pay", StringUtils.formatDouble(Double.parseDouble((freeOrder.get("free_pay")== null ? "0" : freeOrder.get("free_pay")+""))));
							}
						}
					}
					reduce_amount += Double.parseDouble((totalOrder.get("reduce_pay")== null ? "0" : totalOrder.get("reduce_pay")+""));
					backList.add(totalOrder);
				}
			}


//			String money = "总订单数："+totalCount+",月卡订单数:"+monthCount+",订单金额:"+StringUtils.formatDouble(totalMoney)+"元," +
//					"现金支付:"+StringUtils.formatDouble(cashMoney)+"元,电子支付 :"+StringUtils.formatDouble(elecMoney)+"元," +
//					"免费金额:"+StringUtils.formatDouble(freeMoney)+"元,减免劵支付:"+StringUtils.formatDouble(reduce_amount)+"元";
			String money = "总订单数："+totalCount+",月卡订单数:"+monthCount+",订单金额:"+StringUtils.formatDouble(totalMoney)+"元," +
					"现金结算:"+StringUtils.formatDouble(cashpay)+"现金预付:"+StringUtils.formatDouble(cashprepay)+"元,电子支付 :"+StringUtils.formatDouble(elecMoney)+"元," +
					"免费金额:"+StringUtils.formatDouble(freeMoney)+"元,减免劵支付:"+StringUtils.formatDouble(reduce_amount)+"元";
			String json = JsonUtil.anlysisMap2Json(backList,1,backList.size(), fieldsstr,"id",money);
			System.out.println(json);
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("detail")){
			requestUtil(request);
			return mapping.findForward("detail");
		}else if(action.equals("work")){
			requestUtil(request);
			return mapping.findForward("work");
		}else if(action.equals("workdetail")){
			String bt = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "btime"));
			String et = RequestUtil.processParams(request, "etime");
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			String pay_type = RequestUtil.processParams(request, "pay_type");
			String type = RequestUtil.processParams(request, "otype");
			Long btime = TimeTools.getToDayBeginTime();
			Long etime = System.currentTimeMillis()/1000;
			List list = null;//daService.getPage(sql, null, 1, 20);
			List freeorder = null;
			if(type.equals("today")){
			}else if(type.equals("toweek")){
				btime = TimeTools.getWeekStartSeconds();
			}else if(type.equals("lastweek")){
				etime = TimeTools.getWeekStartSeconds();
				btime= etime-7*24*60*60;
				etime = etime-1;
			}else if(type.equals("tomonth")){
				btime=TimeTools.getMonthStartSeconds();
			}else if(type.equals("custom")){
				if(bt.length()==10){
					bt = bt + " 00:00:00";
				}
				btime = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(bt);
				etime =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(et+" 23:59:59");
			}else if(!bt.equals("")&&!et.equals("")){
				btime = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(bt);
				etime =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(et+" 23:59:59");
			}
			long uid = RequestUtil.getLong(request, "uid", -1L);
			String sql = "select a.id,a.start_time,a.end_time,a.uid,b.worksite_name worksite_id " +
					" from " +
					" parkuser_work_record_tb a left join " +
					" com_worksite_tb b  on b.id=a.worksite_id  " +
					"where a.start_time is not null and (a.end_time  between ? and ? or a.start_time between ? and ? or (a.start_time between ? and ? and " +
					"(a.end_time>? or a.end_time is null))) and a.uid = ? ";// order by a.end_time desc";//查询上班信息
			List<Object> params = new ArrayList();
			params.add(btime);
			params.add(etime);
			params.add(btime);
			params.add(etime);
			params.add(btime);
			params.add(etime);
			params.add(etime);
			params.add(uid);
			sql +=" order by a.end_time desc";
			logger.error(sql);
			logger.error(params);
			list = daService.getAllMap(sql,params);

			logger.error(list);
			double amountmoney = 0.0;//总金额
//			double cash_money = 0.0;//现金支付金额
			double cashpay = 0.0;//现金结算金额
			double cashprepay = 0.0;//现金预付金额
			double elec_money = 0.0;//电子支付金额
			double reduce_money = 0.0;//减免支付金额
			double free_money = 0.0;//减免支付金额
			int count =0;
			int monthcount =0;
			for (int i = 0; i < list.size(); i++) { //循环组织每个班的统计
				List<Object> p = new ArrayList();
				Map work = (Map)list.get(i);
				long start_time = (Long)work.get("start_time");
				long end_time = Long.MAX_VALUE;
				try {
					end_time = (Long)work.get("end_time");
				} catch (Exception e) {
				}
				p.add(start_time);
				p.add(end_time);
				p.add(1);
				p.add(uid);
				p.add(0);
				p.add(comid);
				List list2 = new ArrayList();//总的订单
				List list3 = new ArrayList();//免费
				List list4 = new ArrayList();//现金

				//总的订单数和总的金额
				String sql2 = "select count(*) ordertotal,sum(amount_receivable) amount_receivable, " +
						"sum(total) total , sum(cash_pay) cash_pay,sum(cash_prepay) cash_prepay, sum(electronic_pay) electronic_pay,sum(electronic_prepay) electronic_prepay, " +
						"sum(reduce_amount) reduce_pay from order_tb where end_time between ? and ? " +
						" and state= ? and out_uid = ? and ishd=? and comid=?";
				list2 = daService.getAllMap(sql2 ,p);
				//月卡订单数
				String sql5 = "select count(*) ordertotal from order_tb where end_time between ? and ? " +
						" and state= ? and out_uid = ? and pay_type =? and ishd=? and comid=?";
				Object []v5 = new Object[]{start_time,end_time,1,uid,3,0,comid};
				Map list5 = daService.getMap(sql5 ,v5);
				work.put("monthcount", list5.get("ordertotal"));
				monthcount+=Integer.parseInt(list5.get("ordertotal")+"");
				count+=Integer.parseInt((((Map)list2.get(0)).get("ordertotal"))+"");
				if(list2!=null&&list2.size()==1){
					Map<String,Object> oMap = (Map)list2.get(0);
					int ordertotal = 0;
					double totalMOney = 0 ;
					try{
						//amount_receivable = Double.parseDouble((((Map)list2.get(0)).get("amount_receivable"))+"");
						ordertotal = Integer.parseInt((oMap.get("ordertotal"))+"");
						totalMOney = Double.parseDouble((oMap.get("amount_receivable"))+"");

					}catch (Exception e) {
						totalMOney=0.0;
					}
					//work.put("amount_receivable",StringUtils.formatDouble(amount_receivable));
					work.put("ordertotal",ordertotal);
					work.put("total",StringUtils.formatDouble(totalMOney));
					amountmoney+=totalMOney;
					//现金支付
//					cash_money +=StringUtils.formatDouble(oMap.get("cash_pay"))+StringUtils.formatDouble(oMap.get("cash_prepay"));
//					work.put("cash_pay",StringUtils.formatDouble(oMap.get("cash_pay"))+StringUtils.formatDouble(oMap.get("cash_prepay")));
					//现金结算
					cashpay += StringUtils.formatDouble(oMap.get("cash_pay"));
					work.put("cash_pay",String.format("%.2f",StringUtils.formatDouble(oMap.get("cash_pay"))));
					//现金预付
					cashprepay += StringUtils.formatDouble(oMap.get("cash_prepay"));
					work.put("cash_prepay",String.format("%.2f",StringUtils.formatDouble(oMap.get("cash_prepay"))));


					//电子支付
					elec_money += StringUtils.formatDouble(oMap.get("electronic_pay"))+StringUtils.formatDouble(oMap.get("electronic_prepay"));
					work.put("electronic_pay", StringUtils.formatDouble(oMap.get("electronic_pay"))+StringUtils.formatDouble(oMap.get("electronic_prepay")));
					//减免劵支付
					reduce_money +=StringUtils.formatDouble(oMap.get("reduce_pay"));
					work.put("reduce_pay", StringUtils.formatDouble(oMap.get("reduce_pay")));
				}
				//免费订单集合
				String sql6 = "select sum(amount_receivable-electronic_prepay-cash_prepay-reduce_amount) free_pay from order_tb where end_time between ? and ? " +
						" and state= ? and out_uid = ? and pay_type =? and ishd=? and comid=?";
				Object []v6 = new Object[]{start_time,end_time,1,uid,8, 0, comid};
				Map list6 = daService.getMap(sql6 ,v6);
				//免费支付
				free_money += Double.parseDouble((list6.get("free_pay")== null ? "0" : list6.get("free_pay")+""));
				work.put("free_pay", StringUtils.formatDouble(Double.parseDouble(list6.get("free_pay")== null ? "0" : (list6.get("free_pay")+""))));
			}
//			String title = "总订单数："+count+"，月卡订单数："+monthcount+"，总结算金额："+String.format("%.2f",amountmoney)+"元，其中现金支付："+String.format("%.2f",cash_money)
//					+"元，电子支付 ："+StringUtils.formatDouble(elec_money)+"元，" +
//					"免费金额："+String.format("%.2f",free_money)+"元,减免劵支付："+String.format("%.2f",reduce_money)+"元";
			String title = "总订单数："+count+"，月卡订单数："+monthcount+"，总结算金额："+String.format("%.2f",amountmoney)+"元，其中现金结算："+String.format("%.2f",cashpay)
					+"元，现金预付 : "+String.format("%.2f",cashprepay)+"元, 电子支付 ："+StringUtils.formatDouble(elec_money)+"元，" +
					"免费金额："+String.format("%.2f",free_money)+"元,减免劵支付："+String.format("%.2f",reduce_money)+"元";
			String ret = JsonUtil.anlysisMap2Json(list,1,list.size(), fieldsstr,"id",title);
			logger.error(ret);
			AjaxUtil.ajaxOutput(response, ret);
			return null;
		}else if(action.equals("orderdetail")){
			String sql = "select *,(amount_receivable-electronic_prepay-cash_prepay-reduce_amount) free_pay from order_tb  ";
			//统计总订单数，金额，电子和现金支付
			String sql2 = "select count(*) ordertotal,sum(amount_receivable) amount_receivable, " +
					"sum(total) total , sum(cash_prepay) cash_prepay,sum(cash_pay) cash_pay, sum(electronic_pay) electronic_pay," +
					"sum(electronic_prepay) electronic_prepay,sum(reduce_amount) reduce_pay from order_tb";
			Long uid = RequestUtil.getLong(request, "uid", -2L);
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			String btime = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "btime"));
			String etime = RequestUtil.processParams(request, "etime");
			String type = RequestUtil.processParams(request, "otype");
			SqlInfo sqlInfo =null;
			List<Object> params = null;
			Long b = TimeTools.getToDayBeginTime();
			Long e = System.currentTimeMillis()/1000;
			if(type.equals("today")){
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
			}else if(type.equals("toweek")){
				b = TimeTools.getWeekStartSeconds();
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
			}else if(type.equals("lastweek")){
				e = TimeTools.getWeekStartSeconds();
				b= e-7*24*60*60;
				e = e-1;
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
			}else if(type.equals("tomonth")){
				b=TimeTools.getMonthStartSeconds();
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
			}else if(type.equals("workcustom")){
				b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime);
				e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime);
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
			}else if(!btime.equals("")&&!etime.equals("")){
				if(btime.length()==10){
					btime = btime + " 00:00:00";
				}
				b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime);
				e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
			}

			sql +=" where "+sqlInfo.getSql()+" and out_uid=?  and state= ? and comid=? and ishd=?  ";
			sql2 +=" where "+sqlInfo.getSql()+" and out_uid=?  and state= ? and comid=? and ishd=?  ";
			params= sqlInfo.getParams();
			params.add(uid);
			params.add(1);
			params.add(comid);
			params.add(0);


			double amountmoney = 0.0;//总金额
//			double cash_money = 0.0;//现金支付金额
			double cashpay = 0.0;//现金结算金额
			double cashprepay = 0.0;//现金预付金额
			double elec_money = 0.0;//电子支付金额
			int count =0;

			if(uid!=-2){
				List<Map<String, Object>> orders = daService.getAllMap(sql+"order by end_time desc",params);
				for(Map<String, Object> order : orders){
					Map<String, Object> work = new HashMap<String, Object>();
					//编号
					work.put("id", order.get("id"));
					//停车日期
					work.put("create_time", order.get("create_time"));
					//结算日期
					work.put("end_time", order.get("end_time"));
					//订单金额
					work.put("amount_receivable", order.get("amount_receivable"));
					//现金支付  order.get("amount_receivable")
//					work.put("cashMoney", StringUtils.formatDouble(order.get("cash_pay"))+StringUtils.formatDouble(order.get("cash_prepay")));
					//现金结算
					work.put("cash_pay", StringUtils.formatDouble(order.get("cash_pay")));
					//现金预付
					work.put("cash_prepay", StringUtils.formatDouble(order.get("cash_prepay")));
					//电子支付
					work.put("elecMoney", StringUtils.formatDouble(order.get("electronic_prepay"))+StringUtils.formatDouble(order.get("electronic_pay")));
							//+ Double.parseDouble((order.get("electronic_prepay")== null ? "0" : order.get("electronic_prepay")+"")));
					//月卡
					//work.put("monthCard", order.get(""));
					//免费支付
					work.put("freeMoney",0.0);
					if(order.get("pay_type")!=null && Integer.parseInt(order.get("pay_type")+"")==8){
						work.put("freeMoney", StringUtils.formatDouble(Double.parseDouble(order.get("free_pay")== null ? "0" : (order.get("free_pay")+""))));
					}
					//减免支付
					work.put("reduceMoney", order.get("reduce_amount"));
					//停车时长
					work.put("duration", order.get("duration"));
					//支付方式
					work.put("pay_type", order.get("pay_type"));
					//NFC卡号
					work.put("nfc_uuid", order.get(""));
					//车牌号
					work.put("car_number", order.get("car_number"));
					//查看车辆图片
					work.put("order_id_local", order.get("order_id_local"));
					list.add(work);
				}
				List<Map<String, Object>> orderList = daService.getAllMap(sql2,params);
				Double reduce_pay = 0.0;
				if(orderList!=null && orderList.size()>0){
					Map<String, Object> map = orderList.get(0);
					amountmoney = StringUtils.formatDouble((map.get("amount_receivable")));
//					cash_money = StringUtils.formatDouble((map.get("cash_pay")))+StringUtils.formatDouble((map.get("cash_prepay")));
					cashpay = StringUtils.formatDouble((map.get("cash_pay")));
					cashprepay = StringUtils.formatDouble((map.get("cash_prepay")));
					elec_money = StringUtils.formatDouble((map.get("electronic_pay")))+StringUtils.formatDouble((map.get("electronic_prepay")));
					count+=Integer.parseInt((map.get("ordertotal"))+"");
					reduce_pay = StringUtils.formatDouble((map.get("reduce_pay")));
				}
//				String title = StringUtils.formatDouble(amountmoney)+"元，其中现金支付："+String.format("%.2f",cash_money)+"元，" +
//						"电子支付 ："+String.format("%.2f",elec_money)+"元，减免券支付："+String.format("%.2f",reduce_pay)+"元，共"+count+"条";
				String title = StringUtils.formatDouble(amountmoney)+"元，其中现金结算："+String.format("%.2f",cashpay)+"元，" +
						"现金预付 : "+String.format("%.2f",cashprepay)+"元, 电子支付 ："+String.format("%.2f",elec_money)+"元，减免券支付："+String.format("%.2f",reduce_pay)+"元，共"+count+"条";
				String json = JsonUtil.anlysisMap2Json(list,1,list.size(), fieldsstr,"id",title);
				//String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
				AjaxUtil.ajaxOutput(response, json);
				return null;
			}else {
				AjaxUtil.ajaxOutput(response, "{\"page\":1,\"total\":0,\"rows\":[]}");
			}
		}
		return null;
	}

	private String setName(List list,String dstr){
		List<Object> uins = new ArrayList<Object>();
		String total_count="";
		Double total = 0d;
		Long count = 0l;
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				Map map = (Map)list.get(i);
				uins.add(map.get("out_uid"));
				Double t = Double.valueOf(map.get("total")+"");
				Long c = (Long)map.get("scount");
				map.put("sdate", dstr);
				map.put("total", StringUtils.formatDouble(t));
				total+=t;
				count+=c;
			}
		}
		if(!uins.isEmpty()){
			String preParams  ="";
			for(Object uin : uins){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			List<Map<String, Object>> resultList = daService.getAllMap("select id,nickname  " +
					"from user_info_tb " +
					" where id in ("+preParams+") ", uins);
			if(resultList!=null&&!resultList.isEmpty()){
				for(int i=0;i<list.size();i++){
					Map map1 = (Map)list.get(i);
					for(Map<String,Object> map: resultList){
						Long uin = (Long)map.get("id");
						if(map1.get("out_uid").equals(uin)){
							map1.put("name", map.get("nickname"));
							break;
						}
					}
				}
			}
		}
		return total+"_"+count;
	}

	private void setList(List<Map<String, Object>> lists,String dstr){
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		List<Long> comidList = new ArrayList<Long>();
		for(Map<String, Object> map :lists){
			//Long comId = (Long)map.get("comid");
			//Integer state = (Integer)map.get("state");
			map.put("sdate", dstr);
//			if(state==1){
//				comidList.add(comId);
//				result.add(map);
//			}else {
//				if(comidList.contains(comId)){
//					for(Map<String, Object> dMap : result){
//						Long cid = (Long)dMap.get("comid");
//						if(cid.intValue()==comId.intValue()){
//							dMap.put("corder",map.get("scount"));
//							break;
//						}
//					}
//				}else {
//					map.put("corder", map.get("scount"));
//					map.put("scount", null);
//					result.add(map);
//				}
//			}
		}
	}
	private void requestUtil(HttpServletRequest request){
		request.setAttribute("uid", RequestUtil.processParams(request, "uid"));
		request.setAttribute("btime", RequestUtil.processParams(request, "btime"));
		request.setAttribute("etime", RequestUtil.processParams(request, "etime"));
		request.setAttribute("otype", RequestUtil.processParams(request, "otype"));
		System.out.println(RequestUtil.processParams(request, "otype"));
		Integer paytype = RequestUtil.getInteger(request, "pay_type", 0);
		request.setAttribute("pay_type",paytype);
		if(paytype==8){
			request.setAttribute("total", RequestUtil.getDouble(request, "free", 0d));
		}else{
			request.setAttribute("total", RequestUtil.getDouble(request, "total", 0d));
		}
		request.setAttribute("free", RequestUtil.getDouble(request, "free", 0d));
		request.setAttribute("pmoney", RequestUtil.getDouble(request, "pmoney", 0d));
		request.setAttribute("ppremoney", RequestUtil.getDouble(request, "ppremoney", 0d));
		request.setAttribute("pmobile", RequestUtil.getDouble(request, "pmobile", 0d));
		request.setAttribute("count", RequestUtil.getInteger(request, "count", 0));
		request.setAttribute("comid", RequestUtil.getInteger(request, "comid", 0));
		request.setAttribute("pay_type", RequestUtil.getInteger(request, "pay_type", 0));
	}

	private Double getPayMoney2 (Long uid,Long comid,SqlInfo sqlInfo,List<Object> params){
		String sql ="select sum(total) money from order_tb where comid=? and pay_type=? and uid=? and "+sqlInfo.getSql();
//		params.add(0,uid);
//		params.add(0,1);
//		params.add(0,comid);
		Object[] values = new Object[]{comid,1,uid,params.get(0),params.get(1)};
		Map map = pgOnlyReadService.getMap(sql, values);
		if(map!=null&&map.get("money")!=null)
			return Double.valueOf(map.get("money")+"");
		return 0d;
	}
	//现金支付
	private Double getPayMoney (Long uid,List<Object> params){
		String sql ="select sum(amount) money from parkuser_cash_tb where uin=? and type=? and create_time between ? and ? ";
		Object[] values = new Object[]{uid,0,params.get(0),params.get(1)};
		Map map = daService.getMap(sql, values);
		if(map!=null&&map.get("money")!=null)
			return Double.valueOf(map.get("money")+"");
		return 0d;
	}

	private Long getComid(Long comid, Long cityid, Long groupid){
		List<Object> parks = null;
		if(groupid != null && groupid > 0){
			parks = commonMethods.getParks(groupid);
			if(parks != null && !parks.isEmpty()){
				comid = (Long)parks.get(0);
			}else{
				comid = -999L;
			}
		}else if(cityid != null && cityid > 0){
			parks = commonMethods.getparks(cityid);
			if(parks != null && !parks.isEmpty()){
				comid = (Long)parks.get(0);
			}else{
				comid = -999L;
			}
		}

		return comid;
	}
}