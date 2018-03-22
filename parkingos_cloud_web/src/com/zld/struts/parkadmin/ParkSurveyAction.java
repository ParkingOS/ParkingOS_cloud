package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
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
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * 停车场后台概况
 * @author Administrator
 *
 */
public class ParkSurveyAction extends Action{

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;

	private Logger logger = Logger.getLogger(ParkSurveyAction.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		request.setAttribute("authid", request.getParameter("authid"));
		Integer isHd = (Integer)request.getSession().getAttribute("ishdorder");
		if(comid==null){
			response.sendRedirect("login.do");
			return null;
		}
		if(comid==0)
			comid = RequestUtil.getLong(request, "comid", 0L);
		if(action.equals("")){
			//取车场交易及车位数据
			//开始时间
			Long btime= TimeTools.getToDayBeginTime()-9*24*60*60;
			Long etime = System.currentTimeMillis()/1000;
			String sql1 = "select b.amount,a.end_time from order_tb a,parkuser_cash_tb b where a.end_time between" +
					" ? and ? and a.state=? and a.comid=? and a.id=b.orderid and b.type=? and a.uid> ? ";
			List<Object> params = new ArrayList<Object>();
			params.add(btime);
			params.add(etime);
			params.add(1);
			params.add(comid);
			params.add(0);
			params.add(0);
			if(isHd != null && isHd == 1){
				sql1 += " and ishd=? ";
				params.add(isHd);
			}
			List<Map<String, Object>> cashList = pgOnlyReadService.getAllMap(sql1, params);
			String sql2 = "select a.amount,o.end_time from order_tb o,park_account_tb a where o.id=a.orderid and o.end_time between ? and ? " +
					" and a.type= ? and a.source=? and o.comid=? and o.uid>? ";
			params.clear();
			params.add(btime);
			params.add(etime);
			params.add(0);
			params.add(0);
			params.add(comid);
			params.add(0);
			if(isHd != null && isHd == 1){
				sql2 += " and ishd=? ";
				params.add(isHd);
			}
			List<Map<String, Object>> eparkList = pgOnlyReadService.getAllMap(sql2, params);

			String sql3 = "select a.amount,o.end_time from order_tb o,parkuser_account_tb a where o.id=a.orderid and o.end_time between ? and ? " +
					" and a.type=? and o.comid=? and a.target=? and a.remark like ? and o.uid>? ";

			params.clear();
			params.add(btime);
			params.add(etime);
			params.add(0);
			params.add(comid);
			params.add(4);
			params.add("停车费%");
			params.add(0);
			if(isHd != null && isHd == 1){
				sql3 += " and ishd=? ";
				params.add(isHd);
			}
			List<Map<String, Object>> eparkerList = pgOnlyReadService.getAllMap(sql3, params);
			Long ttime = TimeTools.getToDayBeginTime();

			logger.error("十天现金订单数："+cashList.size()+",电子支付订单数:"+eparkList.size()+eparkerList.size());
			Map<Long, List<Double>> oMap = new HashMap<Long, List<Double>>();
			List<Long> dList = new ArrayList<Long>();
			setList(eparkList, 0, dList, oMap);
			setList(eparkerList, 0, dList, oMap);
			setList(cashList, 1, dList, oMap);
			//	System.err.println(comid+">>>>"+oMap);
			String times = "[";
			String total="[";
			String epay ="[";
			String cash ="[";
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/M/d");
			Collections.sort(dList);
			String totday = "";
			for(Long ti : dList){
				times +="'"+dateFormat.format(new Date(ti*1000)).substring(5)+"',";
				List<Double> s = oMap.get(ti);
				total +=StringUtils.formatDouble(s.get(0))+",";
				epay +=StringUtils.formatDouble(s.get(1))+",";
				cash +=StringUtils.formatDouble(s.get(2))+",";
				if(ti.equals(ttime)){//今日收费
					totday +="今日共收费:"+StringUtils.formatDouble(s.get(0))+"，现金收费:"+
							StringUtils.formatDouble(s.get(2))+"，电子支付:"+StringUtils.formatDouble(s.get(1));
				}
			}
			if(times.endsWith(","))
				times = times.substring(0,times.length()-1);
			times +="]";
			if(total.endsWith(","))
				total = total.substring(0,total.length()-1);
			total +="]";
			if(epay.endsWith(","))
				epay = epay.substring(0,epay.length()-1);
			epay +="]";
			if(cash.endsWith(","))
				cash = cash.substring(0,cash.length()-1);
			cash +="]";

			if(times.equals("[]")){
				times="['"+dateFormat.format(new Date(ttime*1000)).substring(5)+"']";
				total="[0]";
				epay="[0]";
				cash="[0]";
			}

			//	String times = "['12/7','12/8','12/9','12/10','12/11','12/12','12/13','12/14','12/15']";
			//String total="[5500.00, 4805.20, 5349.20, 5343.00, 5221.90, 5530, 5140,5322,5455]";
			//	String epay ="[3000, 3455, 3555, 3222, 3444, 3330, 3310,4000,3220]";
			//	String cash ="[1500, 2320, 2010, 1540, 1900, 2300, 2100,2000,3000]";
			//取当天每小时车位占用数
			List<Map<String,Object>> parkList =pgOnlyReadService.getAll("select * from park_anlysis_tb " +
					"where comid=? and create_time >=? order by create_time", new Object[]{comid,ttime});
			String ptimes = "[";
			String park ="[";
			String month_used = "[";
			String time_used = "[";
			if(parkList!=null&&!parkList.isEmpty()){
				for(Map<String,Object> pmMap : parkList){
					Long ctime = (Long)pmMap.get("create_time");
					ptimes +="'"+TimeTools.getTime_yyyyMMdd_HHmm(ctime*1000).substring(11)+"',";
					park +=pmMap.get("used_count")+",";
					month_used += pmMap.get("month_used_count") + ",";
					time_used += pmMap.get("time_used_count") + ",";
				}
			}
			if(ptimes.endsWith(","))
				ptimes = ptimes.substring(0,ptimes.length()-1);
			ptimes +="]";
			if(park.endsWith(","))
				park = park.substring(0,park.length()-1);
			park +="]";
			if(month_used.endsWith(",")){
				month_used = month_used.substring(0, month_used.length() - 1);
			}
			month_used += "]";
			if(time_used.endsWith(",")){
				time_used = time_used.substring(0, time_used.length() - 1);
			}
			time_used += "]";
			//String park ="[500, 460, 470, 280, 350, 330, 380,450,545]";
			if(ptimes.equals("[]")){
				ptimes="['01:00']";
				park="[0]";
				month_used="[0]";
				time_used="[0]";
			}
			String moneyData="{title:['总收费','电子收费','现金收费'],xname:'时间',xtime:"+times+","+
					"yname:'金额(元)',data:[{name:'总收费',data:"+total+"},{name:'电子收费',data:"+epay+"},{name:'现金收费',data:"+cash+"}]}";

			String parkData="{title:['总占用车位数','月卡车占用车位数','时租车占用车位数'],xname:'时间',xtime:"+ptimes+","+
					"yname:'占用车位数',data:[{name:'总占用车位数',data:"+park+"},{name:'月卡车占用车位数',data:"+month_used+"},{name:'时租车占用车位数',data:"+time_used+"}]}";

			Map<String, Object> comMap =
					pgOnlyReadService.getMap("select empty from com_info_tb where id =?", new Object[]{comid});

			request.setAttribute("comid", comid);
			request.setAttribute("moneyData", moneyData);
			request.setAttribute("parkData", parkData);
			request.setAttribute("today", totday.equals("")?"今日无收费":totday);
			request.setAttribute("parktotal", "车位数:"+comMap.get("empty"));
			System.out.println(moneyData);
			System.out.println(parkData);
			return mapping.findForward("survey");
		}else if(action.equals("query")){
			String sql = "select cp.passname,pw.uid,start_time,cp.id,cp.worksite_id,cp.passtype from com_pass_tb cp " +
					"left join parkuser_work_record_tb pw on pw.worksite_id = cp.worksite_id "+
					"where end_time is null and pw.worksite_id in(select id from com_worksite_tb where comid = ?) " +
					"order by pw.start_time desc  ";

			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 50);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Object> params = new ArrayList<Object>();
			params.add(comid);
			Integer count = 0;
			//System.out.println(sqlInfo);
			List<Map<String, Object>> list = null;//daService.getPage(sql, null, 1, 20);
			list = pgOnlyReadService.getAll(sql, params, pageNum, pageSize);
			List<Map<String, Object>> oList=new ArrayList<Map<String,Object>>();
			if(list!=null&&!list.isEmpty()){
				Map<Long,String> nameMap = new HashMap<Long, String>();
				for(Map<String, Object> map : list){
					Long uid = (Long)map.get("uid");
					Long passid = (Long)map.get("id");
					Long workSiteId = (Long)map.get("worksite_id");
					String passType = (String)map.get("passtype");
					if(!nameMap.containsKey(uid)){
						Map<String, Object> userMap = pgOnlyReadService.getMap("select nickname from user_info_tb where " +
								"id=?	",new Object[]{uid});
						map.put("nickname", "");
						if(userMap!=null){
							map.put("nickname",userMap.get("nickname"));
							nameMap.put(uid, userMap.get("nickname")+"");
						}
						//nameMap.put(uid, userMap.get("nickname")+"");
					}else {
						map.put("nickname",nameMap.get(uid));
					}
					//设备状态
					map.put("server",-1);//主机状态
					map.put("carm",-1);//摄像头状态
					map.put("brake",-1);//道闸状态
					map.put("led",-1);//LED状态
					Map serverMap = pgOnlyReadService.getMap("select upload_time from com_worksite_tb where id =?",new Object[]{workSiteId});
					if(serverMap!=null&&!serverMap.isEmpty()){
						Long utime = (Long)serverMap.get("upload_time");
						if(utime!=null){
							Long ntime = System.currentTimeMillis()/1000;
							if(ntime-utime<300){//更新时间小于30秒是工作状态,主机是正常状态时，才查其它设备的状态
								map.put("server",1);
								//摄像头状态
								Map cameraMap = pgOnlyReadService.getMap("select state from com_camera_tb where passid =?",new Object[]{passid});
								if(cameraMap!=null&&!cameraMap.isEmpty()){
									map.put("carm",cameraMap.get("state"));
								}
								//道闸状态
								Map brakeMap = pgOnlyReadService.getMap("select state from com_brake_tb where passid =?",new Object[]{passid});
								if(brakeMap!=null&&!brakeMap.isEmpty()){
									map.put("brake",brakeMap.get("state"));
								}
								//LED状态
								Map ledMap = pgOnlyReadService.getMap("select state,upload_time from com_led_tb where passid =?",new Object[]{passid});
								if(ledMap!=null&&!ledMap.isEmpty()){
									Long uptime = (Long)ledMap.get("upload_time");
									Integer state = (Integer)ledMap.get("state");
									if(uptime!=null&&ntime-uptime<30&&state!=null&&state==1)
										map.put("led",ledMap.get("state"));
								}
							}
						}
					}

					oList.add(map);
				}
				count = list.size();
			}else {
				/*Map<String, Object> emptyData = new HashMap<String, Object>();
				emptyData.put("uid", "暂无数据...");
				oList.add(emptyData);
				count=1;*/
			}
			String json = JsonUtil.Map2Json(oList,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}
		return null;
	}

	private void setList(List<Map<String, Object>> list, Integer type, List<Long> dList, Map<Long, List<Double>> oMap){
		Long btime= TimeTools.getToDayBeginTime()-9*24*60*60;
		for(Map<String, Object> cMap: list){
			Long endTime = (Long)cMap.get("end_time");
			Double total = StringUtils.formatDouble(cMap.get("amount"));
			Long t = btime+((endTime-btime)/(24*60*60))*24*60*60;
			if(oMap.containsKey(t)){
				List<Double> ds = oMap.get(t);
				Double tot = ds.get(0);
				tot +=total;
				ds.remove(0);
				ds.add(0,tot);
				if(type==0){//电子支付
					Double ep = ds.get(1);
					ep += total;
					ds.remove(1);
					ds.add(1,ep);
				}else if(type == 1){
					Double cp = ds.get(2);
					cp += total;
					ds.remove(2);
					ds.add(cp);
				}
			}else {
				List<Double> ds = new ArrayList<Double>();
				ds.add(total);
				if(type == 0){//电子结算
					ds.add(total);
					ds.add(0.0);
				}else if(type == 1){//现金支付
					ds.add(0.0);
					ds.add(total);
				}
				oMap.put(t, ds);
				dList.add(t);
			}
		}
	}
}