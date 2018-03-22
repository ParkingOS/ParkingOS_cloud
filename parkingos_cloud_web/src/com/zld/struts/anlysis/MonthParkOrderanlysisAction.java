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
import java.util.*;

/**
 * 车场订单统计
 *
 * @author Administrator
 */
public class MonthParkOrderanlysisAction extends Action {

    @Autowired
    private DataBaseService daService;
    @Autowired
    private PgOnlyReadService pgOnlyReadService;
    @Autowired
    private CommonMethods commonMethods;

    private Logger logger = Logger.getLogger(MonthParkOrderanlysisAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String action = RequestUtil.processParams(request, "action");
        Long comid = (Long) request.getSession().getAttribute("comid");
        Integer role = RequestUtil.getInteger(request, "role", -1);
        Long uin = (Long) request.getSession().getAttribute("loginuin");//登录的用户id
        request.setAttribute("authid", request.getParameter("authid"));
        Integer isHd = (Integer) request.getSession().getAttribute("ishdorder");
        Long groupid = (Long) request.getSession().getAttribute("groupid");
        Long cityid = (Long) request.getSession().getAttribute("cityid");
        if (ZLDType.ZLD_ACCOUNTANT_ROLE == role || ZLDType.ZLD_CARDOPERATOR == role)
            request.setAttribute("role", role);
        if (uin == null) {
            response.sendRedirect("login.do");
            return null;
        }

        if (comid == 0) {
            comid = RequestUtil.getLong(request, "comid", 0L);
        }
        request.setAttribute("groupid", groupid);
        request.setAttribute("cityid", cityid);
        if (comid == 0) {
            comid = getComid(comid, cityid, groupid);
        }
        if (action.equals("")) {
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM");
            request.setAttribute("btime", df2.format(System.currentTimeMillis()));
            request.setAttribute("etime", df2.format(System.currentTimeMillis()));
            request.setAttribute("comid", comid);
            return mapping.findForward("list");
        } else if (action.equals("query")) {


            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM");
            String nowtime = df2.format(System.currentTimeMillis());


            String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
            String btime = RequestUtil.processParams(request, "btime");
            String etime = RequestUtil.processParams(request, "etime");
            if (!"".equals(etime)) {
                etime = TimeTools.getTime_yyMM(TimeTools.getDateFromStr2(etime));
            }
            logger.info("======>>>btime:"+btime);
            logger.info("======>>>etime:"+etime);
//            System.out.println(btime);
//            System.out.println(etime);
            if (btime.equals(""))
                btime = nowtime;
            if (etime.equals("")) {
                Long nextMonth = TimeTools.getNextMonthStartMillis();
                etime = df2.format(nextMonth);
            }


            Date d1 = new SimpleDateFormat("yyyy-MM").parse(btime);//定义起始日期
            Date d2 = new SimpleDateFormat("yyyy-MM").parse(etime);//定义结束日期
            Calendar dd = Calendar.getInstance();//定义日期实例
            dd.setTime(d1);//设置日期起始时间

            List<Map<String, Object>> backList = new ArrayList<Map<String, Object>>();
            int totalCount = 0;//总订单数
            double totalMoney = 0.0;//订单金额
            double cashMoney = 0.0;//现金支付金额
            double elecMoney = 0.0;//电子支付金额
            double actFreeMoney = 0.0;//免费金额+减免支付

            int i= 1;
            while (dd.getTime().before(d2)&&i<=12) {//判断是否到结束日期
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                String str = sdf.format(dd.getTime());
                Long b = dd.getTime().getTime() / 1000;
                dd.add(Calendar.MONTH, 1);//进行当前日期月份加1
                Long e = dd.getTime().getTime() / 1000;

                SqlInfo sqlInfo = null;
                List<Object> params = null;
                sqlInfo = new SqlInfo(" end_time between ? and ? ",
                        new Object[]{b, e});

                String sql = "select count(*) scount,sum(amount_receivable) amount_receivable, " +
                        "sum(total) total , sum(cash_pay) cash_pay,sum(cash_prepay) cash_prepay, sum(electronic_pay) electronic_pay,sum(electronic_prepay) electronic_prepay, " +
                        "sum(reduce_amount) reduce_pay from order_tb  ";
                String free_sql = "select count(*) scount,sum(amount_receivable-electronic_prepay-cash_prepay-reduce_amount) free_pay from order_tb";

                sql += " where " + sqlInfo.getSql() + " and comid=?  and state= ? and out_uid> ? and ishd=? ";
                free_sql += " where " + sqlInfo.getSql() + " and comid=?  and state= ? and out_uid> ? and ishd=? ";

                params = sqlInfo.getParams();
                params.add(comid);
                params.add(1);
                params.add(-1);
                params.add(0);

                //总订单集合
//                System.out.println("======sql:" + sql);
//                System.out.println("======b:" + b);
//                System.out.println("======e:" + e);
                Map totalMap = pgOnlyReadService.getMap(sql, params);
                //月卡订单集合
//                List<Map<String, Object>> monthList = pgOnlyReadService.getAllMap(sql + " and pay_type=3 ", params);
                //免费订单集合
                Map freeMap = pgOnlyReadService.getMap(free_sql + " and pay_type=8 ", params);


                if (totalMap != null && totalMap.size() > 0) {

                    if (Integer.parseInt(totalMap.get("scount") + "") > 0) {
                        totalMap.put("sdate", str);

                        totalCount += Integer.parseInt(totalMap.get("scount") + "");

                        totalMoney += Double.parseDouble(totalMap.get("amount_receivable") + "");

                        //格式化应收
                        totalMap.put("amount_receivable", String.format("%.2f", StringUtils.formatDouble(Double.parseDouble(totalMap.get("amount_receivable") + ""))));

                        //现金支付
                        cashMoney += StringUtils.formatDouble(totalMap.get("cash_pay")) + StringUtils.formatDouble(totalMap.get("cash_prepay"));
                        totalMap.put("cash_pay", String.format("%.2f", StringUtils.formatDouble(totalMap.get("cash_pay")) + StringUtils.formatDouble(totalMap.get("cash_prepay"))));
                        //电子支付
                        elecMoney += StringUtils.formatDouble(totalMap.get("electronic_pay")) + StringUtils.formatDouble(totalMap.get("electronic_prepay"));
                        totalMap.put("electronic_pay", String.format("%.2f", StringUtils.formatDouble(totalMap.get("electronic_pay")) + StringUtils.formatDouble(totalMap.get("electronic_prepay"))));
                        //每一行的合计 = 现金支付+电子支付
                        totalMap.put("act_total", String.format("%.2f", StringUtils.formatDouble(Double.parseDouble(totalMap.get("cash_pay") + "") + Double.parseDouble(totalMap.get("electronic_pay") + ""))));

                        //免费支付
                        totalMap.put("free_pay", String.format("%.2f", 0.00));
                        //遍历免费集合
                        if (freeMap != null && freeMap.size() > 0) {
                            double freePay = StringUtils.formatDouble(Double.parseDouble((freeMap.get("free_pay") == null ? "0.00" : freeMap.get("free_pay") + "")));
                            double reduceAmount = StringUtils.formatDouble(Double.parseDouble((totalMap.get("reduce_pay") == null ? "0.00" : totalMap.get("reduce_pay") + "")));
                            double actFreePay = freePay + reduceAmount;
                            totalMap.put("free_pay", String.format("%.2f", StringUtils.formatDouble(actFreePay)));
                            actFreeMoney += actFreePay;
                        }
                        backList.add(totalMap);
                    }else{
                        totalMap.put("sdate", str);
                        totalMap.put("amount_receivable",0.00);
                        totalMap.put("free_pay",0.00);
                        totalMap.put("cash_pay",0.00);
                        totalMap.put("electronic_pay",0.00);
                        backList.add(totalMap);
                    }
                }
                i++;
            }
            if (backList.size() > 0) {
                Map sumMap = new HashMap();
                sumMap.put("sdate", "合计");
                sumMap.put("scount", totalCount);
                sumMap.put("amount_receivable", String.format("%.2f", StringUtils.formatDouble(totalMoney)));
                sumMap.put("cash_pay", String.format("%.2f", StringUtils.formatDouble(cashMoney)));
                sumMap.put("electronic_pay", String.format("%.2f", StringUtils.formatDouble(elecMoney)));
                sumMap.put("act_total", String.format("%.2f", StringUtils.formatDouble((cashMoney + elecMoney))));
                sumMap.put("free_pay", String.format("%.2f", StringUtils.formatDouble(actFreeMoney)));
                backList.add(sumMap);
            }
            String json = JsonUtil.anlysisMap2Json(backList, 1, backList.size(), fieldsstr, "id", "");
            System.out.println(json);
            AjaxUtil.ajaxOutput(response, json);
        }
        return null;
    }

//			/*原来统计分析中查询的收费员是uid，改为查询出场收费员out_uid的信息 by lqb 2017-05-27*/
//			/*
//			 *
//			 */
//
//			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
//			String nowtime = df2.format(System.currentTimeMillis());
//			String type = RequestUtil.processParams(request, "type");
//			String sql = "select count(*) scount,sum(amount_receivable) amount_receivable, " +
//					"sum(total) total , sum(cash_pay) cash_pay,sum(cash_prepay) cash_prepay, sum(electronic_pay) electronic_pay,sum(electronic_prepay) electronic_prepay, " +
//					"sum(reduce_amount) reduce_pay, out_uid,comid from order_tb  ";
//			String free_sql = "select count(*) scount,sum(amount_receivable-electronic_prepay-cash_prepay-reduce_amount) free_pay,out_uid,comid from order_tb";
//			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
//			String btime = RequestUtil.processParams(request, "btime");
//			String etime = RequestUtil.processParams(request, "etime");
//			if (btime.equals(""))
//				btime = nowtime + " 00:00:00";
//			if (etime.equals(""))
//				etime = nowtime;
//			SqlInfo sqlInfo = null;
//			List<Object> params = null;
//			Long b = TimeTools.getToDayBeginTime();
//			Long e = System.currentTimeMillis() / 1000;
//			String dstr = btime + "-" + etime;
//			if (!btime.equals("") && !etime.equals("")) {
//				b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime);
//				e = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime + " 23:59:59");
//				sqlInfo = new SqlInfo(" end_time between ? and ? ",
//						new Object[]{b, e});
//			}
//			sql += " where " + sqlInfo.getSql() + " and comid=?  and state= ? and out_uid> ? and ishd=? ";
//			free_sql += " where " + sqlInfo.getSql() + " and comid=?  and state= ? and out_uid> ? and ishd=? ";
//			List<Object> subParams = new ArrayList<Object>();
//			params = sqlInfo.getParams();
//			for (Object object : params) {
//				subParams.add(object);
//			}
//			params.add(comid);
//			params.add(1);
//			params.add(-1);
//			params.add(0);
//			//总订单集合
//			List<Map<String, Object>> totalList = pgOnlyReadService.getAllMap(sql + " group by out_uid,comid order by scount desc ", params);
//			//月卡订单集合
//			List<Map<String, Object>> monthList = pgOnlyReadService.getAllMap(sql + " and pay_type=3 group by out_uid,comid order by scount desc ", params);
//			//免费订单集合
//			List<Map<String, Object>> freeList = pgOnlyReadService.getAllMap(free_sql + " and pay_type=8 group by out_uid,comid order by scount desc ", params);
//			int totalCount = 0;//总订单数
//			int monthCount = 0;
//			double cashpay = 0.0;//现金结算
//			double cashprepay = 0.0;//现金预付
//			double totalMoney = 0.0;//订单金额
//			double cashMoney = 0.0;//现金支付金额
//			double elecMoney = 0.0;//电子支付金额
//			double freeMoney = 0.0;//免费金额
//			double reduce_amount = 0.0;//减免支付
//			List<Map<String, Object>> backList = new ArrayList<Map<String, Object>>();
//			if (totalList != null && totalList.size() > 0) {
//				Map<Long, String> nameMap = new HashMap<>();
//				for (Map<String, Object> totalOrder : totalList) {
//					Long _comid = (Long) totalOrder.get("comid");
//					String names = nameMap.get(_comid);
//					if (names == null) {
//						Map<String, Object> namesMap = daService.getMap("select c.company_name,g.name from com_info_tb c left join" +
//								" org_group_tb g on c.groupid = g.id where c.id =?", new Object[]{_comid});
//						logger.error(namesMap);
//						if (namesMap != null && !namesMap.isEmpty()) {
//							nameMap.put(_comid, namesMap.get("company_name") + "bolink" + namesMap.get("name"));
//							totalOrder.put("comid", namesMap.get("company_name"));
//							totalOrder.put("groupid", namesMap.get("name"));
//						} else {
//							nameMap.put(_comid, "bolink");
//						}
//					} else {
//						totalOrder.put("comid", names.split("bolink")[0]);
//						totalOrder.put("groupid", names.split("bolink")[1]);
//					}
//					totalCount += Integer.parseInt(totalOrder.get("scount") + "");
//					totalMoney += Double.parseDouble(totalOrder.get("amount_receivable") + "");
//					//设定默认值   名字这个 全部按照user_id来处理
//					String sql_worker = "select nickname from user_info_tb where id = ?";
////					String sql_worker = "select nickname from user_info_tb where user_id = ? and comid = ? and state =0";
//					Object[] val_worker = new Object[]{Long.parseLong(totalOrder.get("out_uid") + "")};
////					Object []val_worker = new Object[]{totalOrder.get("out_uid")+"",comid};
//					Map worker = daService.getMap(sql_worker, val_worker);
//					if (worker != null && worker.containsKey("nickname")) {
//						//出场收费员Id
//						totalOrder.put("id", totalOrder.get("out_uid"));
//						//收费员名称
//						totalOrder.put("name", worker.get("nickname"));
//					}
//					//时间段
//					totalOrder.put("sdate", dstr);
//					//月卡订单数
//					totalOrder.put("monthcount", 0);
//					//遍历月卡集合
//					if (monthList != null && monthList.size() > 0) {
//						for (Map<String, Object> monthOrder : monthList) {
//							if (totalOrder.get("out_uid").equals(monthOrder.get("out_uid"))) {
//								monthCount += Integer.parseInt(monthOrder.get("scount") + "");
//								totalOrder.put("monthcount", monthOrder.get("scount"));
//							}
//						}
//					}
//					//现金结算
//					cashpay += StringUtils.formatDouble(totalOrder.get("cash_pay"));
//					totalOrder.put("cash_pay", String.format("%.2f", StringUtils.formatDouble(totalOrder.get("cash_pay"))));
//					//现金预付
//					cashprepay += StringUtils.formatDouble(totalOrder.get("cash_prepay"));
//					totalOrder.put("cash_prepay", String.format("%.2f", StringUtils.formatDouble(totalOrder.get("cash_prepay"))));
//
////					cashMoney +=StringUtils.formatDouble(totalOrder.get("cash_pay"))+StringUtils.formatDouble(totalOrder.get("cash_prepay"));
////					totalOrder.put("cash_pay",String.format("%.2f",StringUtils.formatDouble(totalOrder.get("cash_pay"))+StringUtils.formatDouble(totalOrder.get("cash_prepay"))));
//					//电子支付
//
//					elecMoney += StringUtils.formatDouble(totalOrder.get("electronic_pay")) + StringUtils.formatDouble(totalOrder.get("electronic_prepay"));
//					totalOrder.put("electronic_pay", String.format("%.2f", StringUtils.formatDouble(totalOrder.get("electronic_pay")) + StringUtils.formatDouble(totalOrder.get("electronic_prepay"))));
//					//免费支付
//					totalOrder.put("free_pay", 0.0);
//					//遍历免费集合
//					if (freeList != null && freeList.size() > 0) {
//						for (Map<String, Object> freeOrder : freeList) {
//							if (totalOrder.get("out_uid").equals(freeOrder.get("out_uid"))) {
//								freeMoney += Double.parseDouble((freeOrder.get("free_pay") == null ? "0" : freeOrder.get("free_pay") + ""));
//								totalOrder.put("free_pay", StringUtils.formatDouble(Double.parseDouble((freeOrder.get("free_pay") == null ? "0" : freeOrder.get("free_pay") + ""))));
//							}
//						}
//					}
//					reduce_amount += Double.parseDouble((totalOrder.get("reduce_pay") == null ? "0" : totalOrder.get("reduce_pay") + ""));
//					backList.add(totalOrder);
//				}
//			}
//
//
////			String money = "总订单数："+totalCount+",月卡订单数:"+monthCount+",订单金额:"+StringUtils.formatDouble(totalMoney)+"元," +
////					"现金支付:"+StringUtils.formatDouble(cashMoney)+"元,电子支付 :"+StringUtils.formatDouble(elecMoney)+"元," +
////					"免费金额:"+StringUtils.formatDouble(freeMoney)+"元,减免劵支付:"+StringUtils.formatDouble(reduce_amount)+"元";
//			String money = "总订单数：" + totalCount + ",月卡订单数:" + monthCount + ",订单金额:" + StringUtils.formatDouble(totalMoney) + "元," +
//					"现金结算:" + StringUtils.formatDouble(cashpay) + "现金预付:" + StringUtils.formatDouble(cashprepay) + "元,电子支付 :" + StringUtils.formatDouble(elecMoney) + "元," +
//					"免费金额:" + StringUtils.formatDouble(freeMoney) + "元,减免劵支付:" + StringUtils.formatDouble(reduce_amount) + "元";
//			String json = JsonUtil.anlysisMap2Json(backList, 1, backList.size(), fieldsstr, "id", money);
//			System.out.println(json);
//			AjaxUtil.ajaxOutput(response, json);
//			return null;
//        }
//        return null;
//    }

    private String setName(List list, String dstr) {
        List<Object> uins = new ArrayList<Object>();
        String total_count = "";
        Double total = 0d;
        Long count = 0l;
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                Map map = (Map) list.get(i);
                uins.add(map.get("out_uid"));
                Double t = Double.valueOf(map.get("total") + "");
                Long c = (Long) map.get("scount");
                map.put("sdate", dstr);
                map.put("total", StringUtils.formatDouble(t));
                total += t;
                count += c;
            }
        }
        if (!uins.isEmpty()) {
            String preParams = "";
            for (Object uin : uins) {
                if (preParams.equals(""))
                    preParams = "?";
                else
                    preParams += ",?";
            }
            List<Map<String, Object>> resultList = daService.getAllMap("select id,nickname  " +
                    "from user_info_tb " +
                    " where id in (" + preParams + ") ", uins);
            if (resultList != null && !resultList.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    Map map1 = (Map) list.get(i);
                    for (Map<String, Object> map : resultList) {
                        Long uin = (Long) map.get("id");
                        if (map1.get("out_uid").equals(uin)) {
                            map1.put("name", map.get("nickname"));
                            break;
                        }
                    }
                }
            }
        }
        return total + "_" + count;
    }

    private void setList(List<Map<String, Object>> lists, String dstr) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        List<Long> comidList = new ArrayList<Long>();
        for (Map<String, Object> map : lists) {
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

    private void requestUtil(HttpServletRequest request) {
        request.setAttribute("uid", RequestUtil.processParams(request, "uid"));
        request.setAttribute("btime", RequestUtil.processParams(request, "btime"));
        request.setAttribute("etime", RequestUtil.processParams(request, "etime"));
        request.setAttribute("otype", RequestUtil.processParams(request, "otype"));
        System.out.println(RequestUtil.processParams(request, "otype"));
        Integer paytype = RequestUtil.getInteger(request, "pay_type", 0);
        request.setAttribute("pay_type", paytype);
        if (paytype == 8) {
            request.setAttribute("total", RequestUtil.getDouble(request, "free", 0d));
        } else {
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

    private Double getPayMoney2(Long uid, Long comid, SqlInfo sqlInfo, List<Object> params) {
        String sql = "select sum(total) money from order_tb where comid=? and pay_type=? and uid=? and " + sqlInfo.getSql();
//		params.add(0,uid);
//		params.add(0,1);
//		params.add(0,comid);
        Object[] values = new Object[]{comid, 1, uid, params.get(0), params.get(1)};
        Map map = pgOnlyReadService.getMap(sql, values);
        if (map != null && map.get("money") != null)
            return Double.valueOf(map.get("money") + "");
        return 0d;
    }

    //现金支付
    private Double getPayMoney(Long uid, List<Object> params) {
        String sql = "select sum(amount) money from parkuser_cash_tb where uin=? and type=? and create_time between ? and ? ";
        Object[] values = new Object[]{uid, 0, params.get(0), params.get(1)};
        Map map = daService.getMap(sql, values);
        if (map != null && map.get("money") != null)
            return Double.valueOf(map.get("money") + "");
        return 0d;
    }

    private Long getComid(Long comid, Long cityid, Long groupid) {
        List<Object> parks = null;
        if (groupid != null && groupid > 0) {
            parks = commonMethods.getParks(groupid);
            if (parks != null && !parks.isEmpty()) {
                comid = (Long) parks.get(0);
            } else {
                comid = -999L;
            }
        } else if (cityid != null && cityid > 0) {
            parks = commonMethods.getparks(cityid);
            if (parks != null && !parks.isEmpty()) {
                comid = (Long) parks.get(0);
            } else {
                comid = -999L;
            }
        }

        return comid;
    }
}