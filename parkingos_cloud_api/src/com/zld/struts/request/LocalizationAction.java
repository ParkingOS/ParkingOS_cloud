package com.zld.struts.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.AjaxUtil;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
//Localization
public class LocalizationAction extends Action{
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	private Logger logger = Logger.getLogger(LocalizationAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String token =RequestUtil.processParams(request, "token");
		String action =RequestUtil.processParams(request, "action");
		Map<String,Object> infoMap = new HashMap<String, Object>();
		long comid =-1;
		Long uin = null;
		if(token.equals("")){
			infoMap.put("info", "no token");
		}else {
			Map comMap = daService.getPojo("select * from user_session_tb where token=?", new Object[]{token});
			if(comMap!=null&&comMap.get("comid")!=null){
				comid=Long.parseLong(comMap.get("comid")+"");
				uin =(Long) comMap.get("uin");
			}else {
				infoMap.put("info", "token is invalid");
			}
		}
		logger.error("action="+action+",uin="+uin);
		if("synchroTime".equals(action)){//同步时间
			AjaxUtil.ajaxOutput(response,System.currentTimeMillis()+"");
		}else if("synchroPrice".equals(action)){//同步价格和车场最小计价单位
			List priret = daService.getAll("select * from price_tb where comid=?", new Object []{comid});
			List comret = daService.getAll("select * from com_info_tb where id=?", new Object []{comid});
			String ret = "{\"price_tb\":"+StringUtils.createJson(priret)+",\"com_info_tb\":"+StringUtils.createJson(comret)+"}";
//			priret.addAll(comret);
//			String ret = StringUtils.createJson(priret);
			logger.error(ret);
			AjaxUtil.ajaxOutput(response,ret);
			return null;
		}else if("synchroVip".equals(action)){//同步月卡
//			String maxid = RequestUtil.getString(request, "maxid");
			List list = daService.getAll("select ci.car_number,c.e_time,c.uin from product_package_tb p,carower_product c ,user_info_tb u ,car_info_tb ci " +
		"where c.pid=p.id and p.comid=? and u.id=c.uin and ci.uin = c.uin and c.e_time>?", new Object[]{comid,System.currentTimeMillis()/1000});
			String ret = StringUtils.createJson(list);
			logger.error(ret);
			AjaxUtil.ajaxOutput(response,ret);
			return null;
		}else if("synchroNFC".equals(action)){//同步价格和车场最小计价单位
			String maxid = RequestUtil.getString(request, "maxid");
			List list = daService.getAll("select * from com_nfc_tb where id >? or update_time > create_time", new Object[]{maxid});
			String ret = StringUtils.createJson(list);
			logger.error(ret);
			AjaxUtil.ajaxOutput(response,ret);
			return null;
		}else if("firstDownloadOrder".equals(action)){//客户端登陆时同步订单  返回当天未结算订单和最大编号
			String sql = "select * from order_tb o where o.comid=? and o.state=? order by o.create_time asc ";//order by o.end_time desc
			List result = daService.getAll(sql, new Object[]{comid,0});
			Long maxid = daService.getLong("select max(id) from order_tb", null);
			String ret = "{\"orders\":"+StringUtils.createJson(result)+",\"maxid\":"+maxid+"}";
			AjaxUtil.ajaxOutput(response,ret);
			return null;
		}else if("mergeOrder".equals(action)){//合并订单
			//遍历需要合并的订单
			//查询订单（条件：create_time<end_time and car_number=? and state=0）
			//有的话 结算订单  update
			//没有的话下次合并继续提交尝试合并
//			String orders = RequestUtil.getString(request, "orders");//需要合并的订单
//			long uid = RequestUtil.getLong(request, "uid",-1L);
//			JSONObject jsonObject = JSONObject.fromObject(orders);
//			JSONArray ja = jsonObject.getJSONArray("data");
//			String result ="";
//			for (int i = 0; i < ja.size(); i++) {////遍历需要合并的订单
//				JSONObject jo =  ja.getJSONObject(i);
//				//查询订单（条件：comid=? create_time<? and car_number=? and state=0）
//				Long end = jo.getLong("end_time");
//				Map order = daService.getPojo("select * from order_tb where comid=? create_time<? and car_number=? and state=?", 
//						new Object []{comid,end,jo.getString("car_number"),0});
//				if(order!=null){//update user_info_tb set online_flag=? ,logon_time=? where id=?
//					//计算价格  //有的话 结算订单  update
//					Integer pid = jo.getInt("pid");
//					Long start = (Long)order.get("create_time");
//					String statol ;
//					if(pid>-1){
//						statol = publicMethods.getCustomPrice(start, end, pid);
//					}else {
//						statol = publicMethods.getPrice(start, end, comid, jo.getInt("car_type"));	
//					}
//					int ret = daService.update("update order_tb set end_time=?,uid=?,total=?,stotal=?,state=1 where id=?", 
//							new Object []{jo.getLong("end_time"),uid,jo.getDouble("total"),Double.parseDouble(statol),(Long)order.get("id")});
//					//成功后告诉客户端删除数据并且通知车主和收费员（多退少补）
//					
//					
//				}
//				
//				//没有的话下次合并继续提交尝试合并
//				 
//			}
//			
			
		}else if("synchroOrder".equals(action)){//同步订单
//			String ids = RequestUtil.getString(request, "ids");//需要查询是否结算的id
			Long id = Long.parseLong(RequestUtil.getString(request, "maxid"));
			Long cid = comid;
			String orders = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "orders"));//需要上传的订单.
			logger.error("sync order maxid:"+id+",order:"+orders);
			StringBuffer ret = new StringBuffer("");
			String querysql = "select * from order_tb o where o.comid=? and o.state=? and id>? order by o.create_time asc ";//order by o.end_time desc
			List result = daService.getAll(querysql, new Object[]{cid,0,id});
			long maxid = daService.getLong("select max(id) from order_tb", null);
			ret.append("{\"orders\":"+StringUtils.createJson(result));//需要同步的订单（其他收费员生成的）
			JSONObject jsonObject = JSONObject.fromObject(orders);//需要上传的订单（本地正常生成并结算的（除去估算订单））和同步服务器后本地结算的订单
			JSONArray ja = jsonObject.getJSONArray("data");
			String relation = ",\"relation\":[";
			String delOrderIds = ",\"delOrderIds\":\"";
			 for (int i = 0; i < ja.size(); i++) {//处理需要上传的订单返回id对应关系
				 JSONObject jo =  ja.getJSONObject(i);
				 boolean flag = false;//true代表需要update,false代表需要insert
				 try {
					 long j = Long.parseLong(jo.getLong("id")+"");
				 } catch (Exception e) {
					flag = true;
				 }
				 if(flag){//需要执行插入的记录
					 Long nextid = daService.getLong(
								"SELECT nextval('seq_order_tb'::REGCLASS) AS newid", null);
//					 state,end_time,auto_pay,pay_type,nfc_uuid" +
//				 		",c_type,uid,car_number,imei,pid,car_type,pre_state,in_passid,out_passid)"
					 StringBuffer insertsql = new StringBuffer("insert into order_tb(id,comid,uin,");//order by o.end_time desc
					 StringBuffer valuesql = new StringBuffer("?,?,?,?,?,?,?,?,");
					 ArrayList list = new ArrayList();
					 list.add(nextid);
					 list.add(jo.getLong("comid"));
					 list.add(jo.getLong("uin"));
					 Long createtime = null;
					 String carnumber = null;
					 if(!"null".equals(jo.getString("create_time"))){
						 valuesql.append("?,");
						 insertsql.append("create_time,");
						 createtime = jo.getLong("create_time");
						 list.add(createtime);
					 }
					 if(!"null".equals(jo.getString("car_number"))){
						 valuesql.append("?,");
						 insertsql.append("car_number,");
						 carnumber = jo.getString("car_number");
						 list.add(carnumber);
					 }
					 if(createtime!=null&&carnumber!=null){
						Map count =  daService.getMap("select id from order_tb where car_number=? and create_time = ? and state = ?", new Object[]{carnumber,createtime,0});
						if(count!=null&& count.size()>0){//如果线上服务器和本地上传的订单时间车牌相同则本地删除
							delOrderIds+=jo.getString("id")+",";
							continue;
						}
					 }
					 if(!"null".equals(jo.getString("total"))&&!"价格未知".equals(jo.getString("total"))){
						 valuesql.append("?,");
						 insertsql.append("total,");
						 list.add(jo.getDouble("total"));
					 }
					 insertsql.append("state,");
					 list.add(jo.getLong("state"));
					 if(!"null".equals(jo.getString("end_time"))&&!"0".equals(jo.getString("end_time"))){
						 valuesql.append("?,");
						 insertsql.append("end_time,");
						 list.add(jo.getLong("end_time"));
					 }
					 insertsql.append("auto_pay,");
					 list.add(jo.getLong("auto_pay"));
					 insertsql.append("pay_type,");
					 list.add(jo.getLong("pay_type"));
					 if(!"null".equals(jo.getString("nfc_uuid"))){
						 valuesql.append("?,");
						 insertsql.append("nfc_uuid,");
						 list.add(jo.getString("nfc_uuid"));
					 }
					 insertsql.append("c_type,");
					 list.add(jo.getLong("c_type"));
					 insertsql.append("uid,");
					 list.add(jo.getLong("uid"));
					 if(!"null".equals(jo.getString("imei"))){
						 insertsql.append("imei,");
						 valuesql.append("?,");
						 list.add(jo.getString("imei"));
					 }
					 insertsql.append("pid,");
					 insertsql.append("car_type,");
					 insertsql.append("pre_state,");
					 insertsql.append("in_passid,");
					 valuesql.append("?,?,?,?,?,?");
					 insertsql.append("out_passid,type) values ("+valuesql+")");
					
					 list.add(jo.getLong("pid"));
					 list.add(jo.getLong("car_type"));
					 list.add(jo.getLong("pre_state"));
					 list.add(jo.getString("in_passid").equals("null")?-1:jo.getLong("in_passid"));
					 list.add(jo.getString("out_passid").equals("null")?-1:jo.getLong("out_passid"));
					 list.add(0);//type   本地化订单
					 int insert = daService.update(insertsql.toString(), list.toArray());
					 logger.error("本地生成订单插入ret:"+insert+",orderid:"+nextid);
					 if(insert==1){
						 relation+="{\"local\":\""+jo.getString("id")+"\",\"line\":\""+nextid+"\"},";//id对应关系
						 if(jo.getLong("state")>0){
							 delOrderIds+=nextid+",";
							 if(jo.getLong("state")==1){
								 if(!"null".equals(jo.getString("total"))&&!"价格未知".equals(jo.getString("total"))&&jo.getInt("pay_type")!=8&&jo.getInt("c_type")!=5){
									 Long c = daService.getLong("select count(*) from parkuser_cash_tb where orderid = ?", new Object[]{nextid});
									 if(c!=null&&c<1){
										 int cashret = daService.update("insert into parkuser_cash_tb(uin,amount,orderid,create_time) values(?,?,?,?)", new Object[]{jo.getLong("uid"),jo.getDouble("total"),nextid,jo.getLong("end_time")});
										 logger.error("写现金收费记录ret："+cashret+",orderid:"+nextid+",amount:"+jo.getDouble("total"));
									 }else{
										 logger.error("已有现金记录：orderid:"+nextid+",amount:"+jo.getDouble("total"));
									 }
								 }else{
									 logger.error("价格格式错误或者月卡或者免费不写现金记录,orderid:"+nextid);
								 }
							 }
						 }
					 }
				 }else{//本地结算了更新服务器的操作				
					 Long lineid = jo.getLong("id");//update user_info_tb set online_flag=? ,logon_time=? where id=?
					 StringBuffer insertsql = new StringBuffer("update order_tb set");//order by o.end_time desc
					 ArrayList list = new ArrayList();
					 Long createtime = null;
					 String carnumber = null;
					 if(!"null".equals(jo.getString("create_time"))){
						 createtime = jo.getLong("create_time");
						 insertsql.append(" create_time=?,");
						 list.add(createtime);
					 }
					 if(!"null".equals(jo.getString("car_number"))){
						 insertsql.append(" car_number=?,");
						 list.add(jo.getString("car_number"));
						 carnumber = jo.getString("car_number");
					 }
					 if(createtime!=null&&carnumber!=null){
						logger.error("createtime:"+createtime+",car_number:"+carnumber);
						Long count =  daService.getLong("select count(*) from order_tb where car_number=? and create_time = ? and state = ?", new Object[]{carnumber,createtime,1});
						if(count!=null&& count>0){
							delOrderIds+=lineid+",";
							logger.error("线上该订单已结算,本地删除订单:"+lineid);
							continue;
						}
					 }
					 if(!"null".equals(jo.getString("total"))&&!"价格未知".equals(jo.getString("total"))){
						 insertsql.append(" total=?,");
						 list.add(jo.getDouble("total"));
					 }
					 if(!"null".equals(jo.getString("state"))){
						 insertsql.append(" state=?,");
						 list.add(jo.getLong("state"));
					 }
					 if(!"null".equals(jo.getString("end_time"))){
						 insertsql.append(" end_time=?,");
						 list.add(jo.getLong("end_time"));
					 }
					 if(!"null".equals(jo.getString("pay_type"))){
						 insertsql.append(" pay_type=?,");
						 list.add(jo.getLong("pay_type"));
					 }
					 if(!"null".equals(jo.getString("uid"))){
						 insertsql.append(" uid=?,");
						 list.add(jo.getLong("uid"));
					 }
					 insertsql.append(" out_passid=?");
					 list.add(jo.getString("out_passid").equals("null")?-1:jo.getLong("out_passid"));
					 String sql = insertsql+" where id = ?";
					 list.add(jo.getLong("id"));
					 int update = daService.update(sql, list.toArray());
					 logger.error("本地结算订单更新ret:"+update+",orderid:"+lineid);
					 if(update==1){
						 if(jo.getLong("state")>0){
							 delOrderIds+=jo.getLong("id")+",";
							 if(jo.getLong("state")==1){
								 if(!"null".equals(jo.getString("total"))&&!"价格未知".equals(jo.getString("total"))&&jo.getInt("pay_type")!=8&&jo.getInt("c_type")!=5){
									 Long c = daService.getLong("select count(*) from parkuser_cash_tb where orderid = ?", new Object[]{lineid});
									 if(c!=null&&c<1){
										 int r = daService.update("insert into parkuser_cash_tb(uin,amount,orderid,create_time) values(?,?,?,?)", new Object[]{jo.getLong("uid"),jo.getDouble("total"),jo.getLong("id"),jo.getLong("end_time")});
										 logger.error("写现金收费记录ret:"+r+",orderid:"+lineid+",amount:"+jo.getDouble("total")+",生成现金收费记录ret:"+r);
									 }else{
										 logger.error("已有现金记录：orderid:"+lineid+",amount:"+jo.getDouble("total"));
									 }
								 }else{
									 logger.error("价格格式错误或者月卡或者免费不写现金记录orderid:"+lineid);
								 }
							 }
						 }
					 }
				 }
//				 if(delOrderIds.length()>16)
//						ret.append(delOrderIds);
		     }
			 if(relation.length()>14)
				 ret.append(relation.substring(0, relation.length()-1)+']');
			ret.append(",\"maxid\":\""+(maxid+ja.size())+"\"");
//			if(StringUtils.isNotNull(ids)){//处理上次同步的订单是否已结算
//				String[] idArr = ids.split(",");
////				String delIds = "";
//				for (int i = 0; i < idArr.length; i++) {
//					Long state = daService.getLong("select o.state from order_tb o where o.comid=? and id=?  ",new Object[]{cid,Long.parseLong(idArr[i])});
//					if(state!=null&&state==1){
//						delOrderIds+=idArr[i]+",";
//					}
//				}
//			}
			if(delOrderIds.length()>16){
				ret.append(""+delOrderIds.substring(0, delOrderIds.length()-1)+"\"");
			}
			ret.append("}");
			logger.error("sync order return result："+ret.toString());
			AjaxUtil.ajaxOutput(response,ret.toString());
			return null;
//			测试:http://localhost:8080/zld/local.do?action=synchroOrder&token=?&maxid=2&ids=786835&orders={%22data%22:[{%22id%22:%22786491%22,%22
//			create_time%22:%221431431340%22,%22comid%22:%221197%22,%22uin%22:%2219614%22,%22total%22:%22null%22,%22state%22:%221%22,%22end_time
//			%22:%22null%22,%22auto_pay%22:%220%22,%22pay_type%22:%220%22,%22nfc_uuid
//			%22:%220468904A9A3D80%22,%22c_type%22:%220%22,%22uid%22:%2211802%22,%22car_number
//			%22:%22%E9%9D%92S12348%22,%22imei%22:%22359776056347380%22,%22pid%22:%22-1%22,%22car_type%22:%220%22,
//			%22pre_state%22:%220%22,%22in_passid%22:%22-1%22,%22out_passid%22:%22-1%22}]}
		}
		AjaxUtil.ajaxOutput(response,"");
		return null;
	}
//	{"orders":[],
//		"maxid":"790251","delOrderIds":"790220,790221,790222,790223,790224,790225,790226,790227,3c856c6b-3636-4448-905f-694e97277dcd," +
//				"b328342b-fd53-4e04-8058-9424f541ccbf,e0dc3805-3cf0-45d4-992e-7a74b3de13a6," +
//				"76f96608-1ab3-495b-af3a-16601d06e605,1d3552a2-4406-4b76-b5ba-20d23d3c43b9"}
	 
	 
	
}
