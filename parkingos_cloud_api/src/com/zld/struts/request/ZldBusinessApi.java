package com.zld.struts.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
import com.zld.utils.Check;
import com.zld.utils.StringUtils;
import com.zld.utils.ZldUploadOperate;
import com.zld.utils.ZldUploadUtils;


/**
 * 运营数据 上行接口
 * @author laoyao
 *
 */
@Path("business")
public class ZldBusinessApi {
	
	
	Logger logger = Logger.getLogger(ZldBaseInfoApi.class);
	
	
	/**
	 * 结算订单
	 * http://127.0.0.1/zld/api/business/payorder
	 */
	@POST
	@Path("/payorder")//结算订单
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void payOrder(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		logger.error("payorder:origin params:"+params);
		logger.error("payorder:anlysis params:"+paramMap);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		Map<String, Object> returnMap =new HashMap<String, Object>();
		if(paramMap.get("park_uuid")!=null){
			String comid =zldUploadOperate.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			if(comid==null||comid.equals("-1")){
				returnMap.put("status", "2");
				returnMap.put("resultCode", "100");
				returnMap.put("message", "车场信息不合法，找不到所属车场，ParkingNo不合法。");
				returnMap.put("data", "{}");
				logger.error("payorder:error,车场信息不合法，找不到所属车场，返回："+returnMap+"，原始请求:"+params);
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
				return ;
			}else {
				paramMap.put("comid", comid);
				paramMap.remove("park_uuid");
			}
		}else {
			returnMap.put("status", "2");
			returnMap.put("resultCode", "100");
			returnMap.put("message", "车场信息不合法，找不到所属车场，原始请求:"+params);
			returnMap.put("data", "{}");
			logger.error("payorder:error,车场信息不合法，找不到所属车场，返回："+returnMap+"，原始请求:"+params);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
			return ;
		}
		Long uin = -1L;
		if(paramMap.get("car_number")!=null){
			paramMap.put("car_number", paramMap.get("car_number").toUpperCase());
			uin = zldUploadOperate.getUinByCarNumber(paramMap.get("car_number"), context);
		}
		paramMap.put("uin", uin+"");
		paramMap.put("c_type", "2");
		logger.error(">>>>>payorder,params:"+paramMap);
		String isEscape = paramMap.get("isescape");//是否逃欠费
		DataBaseService daService = (DataBaseService) ctx.getBean("dataBaseService");
		Long neworderId = daService.getkey("seq_order_tb");
		if(isEscape!=null&&isEscape.equals("true")){//逃单订单，直接写入数据库，不扣费
			logger.error("payorder >>>写逃单记录...");
			String escape = paramMap.get("escape");//逃欠费金额. 单位：分
			Double t = 0.0;
			if(escape!=null&&Check.isNumber(escape)){
				t = StringUtils.formatDouble(escape)/100;//转为元，保留两位小数
			}
			paramMap.put("total", t+"");
			paramMap.put("id", neworderId+"");
			//写订单表
			paramMap.put("state", "0");
			returnMap= zldUploadOperate.handleData(context,paramMap,params,"order_tb",0);
			if(returnMap.get("status").equals("1")){
				//写逃单表
				String sql = "insert into no_payment_tb(create_time,order_id,end_time,car_number,comid,uin,total) values(?,?,?,?,?,?,?)";
				Object[] values = new Object[]{Long.valueOf(paramMap.get("create_time")),neworderId,Long.valueOf(paramMap.get("end_time")),
						paramMap.get("car_number"),Long.valueOf(paramMap.get("comid")),uin,t};
				int ret = daService.update(sql, values);
				logger.error(">>>>>escape order>>>>第三方结算订单，上传逃单，写入逃单表，车主："+paramMap.get("car_number")+",金额："+t+"，结果："+ret);
			}
		}else {//结算订单
			logger.error("payorder >>>结算订单...");
			String allTotal = paramMap.get("all_total");
			String total = paramMap.get("total");
			if(allTotal!=null&&Check.isNumber(allTotal)&&total!=null&&Check.isNumber(total)){
				Integer t= Integer.valueOf(total);
				Integer at = Integer.valueOf(allTotal);
				String carNumber = (String)paramMap.get("car_number");
				if(at!=0&&at>t){//有逃单金额
					logger.error("payorder >>>有逃单金额:"+(at-t)+"分，开始处理逃单...");
					Integer et = 0;
					et = at-t;//计算逃单金额
					//结算欠费订单
					List<Map<String, Object>> list = daService.getAll("select * from no_payment_tb where state=?" +
							" and car_number= ?", new Object[]{0,paramMap.get("car_number")});
					//保存需要写入车场账户的逃单金额
					Map<Long,String> comIdMoneyMap = new HashMap<Long, String>();
					if(list!=null&&!list.isEmpty()){
						Double tf = Double.valueOf(at-t)/100;
						for(Map<String, Object> map : list){
							Double itemTotal = StringUtils.formatDouble(map.get("total"));
							tf = tf -itemTotal;
							Long id = (Long)map.get("id");
							Long orderId = (Long)map.get("order_id");
							if(tf>=0){
								int r1 = daService.update("update order_tb set state = ? ,act_total=?,pay_type=? where id=? ",
										new Object[]{1,itemTotal,Integer.valueOf(paramMap.get("pay_type")),orderId});
								if(r1==1){
									int r2=daService.update("update no_payment_tb set state=?,pursue_time=? where id=? ", 
											new Object[]{1,System.currentTimeMillis()/1000,id});
									logger.error(">>>>逃单对应的入场订单处理完成，编号:"+orderId);
									if(r2==1){
										logger.error(">>>>逃单订单处理完成，编号:"+id);
										//需要写入车场账户的逃单金额
										comIdMoneyMap.put((Long)map.get("comid"), orderId+"_"+itemTotal);
									}else {
										logger.error(">>>>逃单订单处理失败，编号:"+id+",ret："+r2);
									}
								}else {
									logger.error(">>>>逃单对应的入场订单处理失败，编号:"+orderId+",ret:"+r1);
								}
							}else{
								logger.error(">>>>逃单金额不够更新欠费订单，还欠费:"+tf+",未能结算的逃单编号："+orderId);
							}
						}
					}
					if(paramMap.get("pay_type").equals("7")){
						payEscOrder(comIdMoneyMap,carNumber,daService);
					}
				}
				//正常结算订单
				paramMap.put("id", neworderId+"");
				paramMap.put("total",""+ Double.valueOf(total)/100);
				paramMap.put("state", "1");
				paramMap.put("c_type", "2");
				returnMap = zldUploadOperate.handleData(context,paramMap,params,"order_tb",0);
				logger.error("payorder >>>结算订单返回："+returnMap);
				if(returnMap.get("status").equals("1")&&paramMap.get("pay_type").equals("7")){
					logger.error("payorder >>>已写处理完订单，开始写账户明细...");
					payNorOrder(Double.valueOf(t)/100,neworderId,carNumber,paramMap.get("comid"),daService);
				}
			}
		}
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	


	/**
	 * 上传黑名单
	 * http://127.0.0.1/zld/api/business/addblack
	 */
	@POST
	@Path("/addblack")//黑名单
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void addblack(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		Map<String, Object> returnMap = zldUploadOperate.handleData(context,paramMap,params,"zld_black_tb",-1);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	@POST
	@Path("/uploadparkstatus")//停车场实时状态上报
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void uploadParkStatus(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		
		if(paramMap.get("park_uuid")!=null){
			String comid =zldUploadOperate.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			paramMap.put("comid", comid);
			paramMap.remove("park_uuid");
		}
		Map<String, Object> returnMap =  zldUploadOperate.handleData(context,paramMap,params,"com_parkstatus_tb",0);
		if(returnMap.get("status").equals("1")){
			try {
				DataBaseService daService = (DataBaseService) ctx.getBean("dataBaseService");
				int ret =daService.update("update com_info_tb set empty=?,share_number=? where id=? ", 
						new Object[]{Integer.valueOf(paramMap.get("empty")),Integer.valueOf(paramMap.get("total")),
						Long.valueOf(paramMap.get("comid"))}); 
				logger.error("uploadparkstatus,更新车场状态："+ret);
			} catch (BeansException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		logger.error("uploadparkstatus result:"+returnMap);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	
	/**
	 * 收费员上下岗
	 * http://127.0.0.1/zld/api/business/parkusercheck
	 */
	@POST
	@Path("/parkusercheck")//停车场每日收费汇报接口
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void parkusercheck(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		if(paramMap.get("park_uuid")!=null){
			String comid =zldUploadOperate.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			paramMap.put("comid", comid);
			paramMap.remove("park_uuid");
		}
		if(paramMap.get("user_uuid")!=null){
			String uid =zldUploadOperate.getUserIdByUUID(paramMap.get("user_uuid"),context);
			paramMap.put("uid", uid);
			paramMap.remove("user_uuid");
		}
		if(paramMap.get("berthsec_uuid")!=null){
			String berthsecId =zldUploadOperate.getBerthsecIdIdByUUID(paramMap.get("berthsec_uuid"),context);
			paramMap.put("berthsec_id", berthsecId);
			paramMap.remove("berthsec_uuid");
		}
		Map<String, Object> returnMap = zldUploadOperate.handleData(context,paramMap,params,"com_parkuser_check",0);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	
	/**
	 * 上传工作组
	 * http://127.0.0.1/zld/api/business/uploadworkgroups
	 */
	@POST
	@Path("/uploadworkgroups")//停车场每日收费汇报接口
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void uploadWorkGroups(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		if(paramMap.get("company_uuid")!=null){
			String companyId =zldUploadOperate.getCompanyIddByUUID(paramMap.get("company_uuid"),context);
			paramMap.put("company_id", companyId);
			paramMap.remove("company_uuid");
		}
		Map<String, Object> returnMap = zldUploadOperate.handleData(context,paramMap,params,"work_group_tb",-1);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	/**
	 * 上传交易订单
	 * http://127.0.0.1/zld/api/business/addorder
	 */
	@POST
	@Path("/addorder")//订单上传
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void addorder(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		if(paramMap.get("park_uuid")!=null){
			String comid =zldUploadOperate.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			paramMap.put("comid", comid);
			paramMap.remove("park_uuid");
		}
		if(paramMap.get("in_employeeid")!=null){
			String uid =zldUploadOperate.getUserIdByUUID(paramMap.get("in_employeeid"),context);
			paramMap.put("uid", uid);
			paramMap.remove("in_employeeid");
		}
		if(paramMap.get("out_employeeid")!=null){
			String outUid =zldUploadOperate.getComIdByParkUUID(paramMap.get("out_employeeid"),context);
			paramMap.put("out_uid", outUid);
			paramMap.remove("out_employeeid");
		}
		Map<String, Object> returnMap = zldUploadOperate.handleData(context,paramMap,params,"order_tb",0);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	/**
	 * 编辑订单
	 * http://127.0.0.1/zld/api/business/updateorder
	 */
	@POST
	@Path("/updateorder")//编辑上传
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void updateorder(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		if(paramMap.get("park_uuid")!=null){
			String comid =zldUploadOperate.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			paramMap.put("comid", comid);
			paramMap.remove("park_uuid");
		}
		Map<String, Object> returnMap = zldUploadOperate.handleData(context,paramMap,params,"order_tb",1);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	

	
	/**
	 * 停车场每日收费汇报接口
	 * http://127.0.0.1/zld/api/business/uploadparkdaypay
	 */
	@POST
	@Path("/uploadparkdaypay")//停车场每日收费汇报接口
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void uploadparkdaypay(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		if(paramMap.get("park_uuid")!=null){
			String comid =zldUploadOperate.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			paramMap.put("comid", comid);
			paramMap.remove("park_uuid");
		}
		Map<String, Object> returnMap = zldUploadOperate.handleData(context,paramMap,params,"park_daypay_tb",0);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	/**
	 * 3.3.11停车场每日停车量汇报接口
	 * http://127.0.0.1/zld/api/business/uploadparkdaypay
	 */
	@POST
	@Path("/uploadparkdayuse")//停车场每日停车量汇报接口
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void uploadparkdayuse(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		if(paramMap.get("park_uuid")!=null){
			String comid =zldUploadOperate.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			paramMap.put("comid", comid);
			paramMap.remove("park_uuid");
		}
		Map<String, Object> returnMap = zldUploadOperate.handleData(context,paramMap,params,"park_dayuse_tb",0);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	
	//正常结算订单
	private void payNorOrder(Double t,Long orderId, String carNumber,String comId,DataBaseService service) {
		writeToAccount(Long.valueOf(comId), orderId, t, carNumber, service);
	}

	//结算逃单
	private void payEscOrder(Map<Long,String> map, String carNumber,DataBaseService service) {
		if(map!=null&&!map.isEmpty()){
			for(Long comId : map.keySet()){
				if(comId>0){
					String orderIdMoney [] = map.get(comId).split("_");
					Double money = StringUtils.formatDouble(orderIdMoney[1]);
					Long orderId = Long.valueOf(orderIdMoney[0]);
					writeToAccount(comId, orderId, money, carNumber, service);
				}
			}
		}
	}
	//写入账户
	private void writeToAccount(Long comId,Long orderId,Double money,String carNumber,DataBaseService service){
		Long ntime = System.currentTimeMillis()/1000;
		Map<String, Object> userMap = service.getMap("select uin from car_info_tb where car_number=? ",
				new Object[]{carNumber});
		Long uin = userMap!=null?(Long)userMap.get("uin"):-1L;
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		Map moneySetMap = service.getMap("select giveto from money_set_tb where comid=? ", new Object[]{comId});
		Integer giveTo =2;
		if(moneySetMap!=null&&moneySetMap.get("giveto")!=null){
			//'0:公司账户，1：个人账户 ，2：运营集团账户';//默认写到集团账户
			giveTo = (Integer)moneySetMap.get("giveto");
		}
		if(giveTo==0){//写到车场账户
			Map<String, Object> parkAccountsqlMap =new HashMap<String, Object>();
			parkAccountsqlMap.put("sql", "insert into park_account_tb(comid,amount,type,create_time,remark,uid,source,orderid) values(?,?,?,?,?,?,?,?)");
			parkAccountsqlMap.put("values",  new Object[]{comId,money,0,ntime,"停车费",-1L,0,orderId});
			bathSql.add(parkAccountsqlMap);
			
			Map<String, Object> comSqlMap =new HashMap<String, Object>();
			comSqlMap.put("sql", "update com_info_tb  set total_money =total_money+?,money=money+? where id=?");
			comSqlMap.put("values", new Object[]{money,money,comId});
			bathSql.add(comSqlMap);
		}else {//写到集团账户
			Map groupMap = service.getMap("select groupid from com_info_tb where id =? ", new Object[]{comId});
			Long groupId =-1L;
			if(groupMap!=null&&groupMap.get("groupid")!=null)
				groupId = (Long)groupMap.get("groupid");
			if(groupId!=null&&groupId>0){
				Map<String, Object> groupAccountsqlMap =new HashMap<String, Object>();
				groupAccountsqlMap.put("sql", "insert into group_account_tb(comid,amount,type,create_time,remark,uid,source,orderid,groupid) values(?,?,?,?,?,?,?,?,?)");
				groupAccountsqlMap.put("values",  new Object[]{comId,money,0,ntime,"停车费",-1L,0,orderId,groupId});
				bathSql.add(groupAccountsqlMap);
				
				Map<String, Object> groupSqlMap =new HashMap<String, Object>();
				groupSqlMap.put("sql", "update org_group_tb  set balance =balance+? where id=?");
				groupSqlMap.put("values", new Object[]{money,groupId});
				bathSql.add(groupSqlMap);
			}else {//没有集团编号还是写到车场账户
				Map<String, Object> parkAccountsqlMap =new HashMap<String, Object>();
				parkAccountsqlMap.put("sql", "insert into park_account_tb(comid,amount,type,create_time,remark,uid,source,orderid) values(?,?,?,?,?,?,?,?)");
				parkAccountsqlMap.put("values",  new Object[]{comId,money,0,ntime,"停车费",-1L,0,orderId});
				bathSql.add(parkAccountsqlMap);
				
				Map<String, Object> comSqlMap =new HashMap<String, Object>();
				comSqlMap.put("sql", "update com_info_tb  set total_money =total_money+?,money=money+? where id=?");
				comSqlMap.put("values", new Object[]{money,money,comId});
				bathSql.add(comSqlMap);
			}
		}
		//写车主账户
		Map<String, Object> trueUsersqlMap =new HashMap<String, Object>();
		trueUsersqlMap.put("sql", "update user_info_tb set balance=balance-? where id=?");
		trueUsersqlMap.put("values", new Object[]{money, uin });
		bathSql.add(trueUsersqlMap);
		//写车主明细
		Map<String, Object> userAccountsqlMap =new HashMap<String, Object>();
		userAccountsqlMap.put("sql", "insert into user_account_tb(uin,amount,type,create_time,remark,pay_type,orderid) values(?,?,?,?,?,?,?)");
		userAccountsqlMap.put("values", new Object[]{uin,money,1,ntime,"停车费",0,orderId});
		bathSql.add(userAccountsqlMap);
		boolean result= service.bathUpdate(bathSql);
		logger.error("payorder,写入账户："+result);
	}
	
}
