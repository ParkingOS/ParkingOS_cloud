package com.zld.struts.parkadmin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.AjaxUtil;
import com.zld.dao.PgOnlyReadDao;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.Check;
import com.zld.utils.ExportExcelUtil;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.utils.ZLDType;
/**
 * 停车场后台会员，包月卡用户
 * @author Administrator
 *
 */
public class VipManageAction extends Action{
	
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	@Autowired
	private PgOnlyReadService pService;
	
	private Logger logger = Logger.getLogger(VipManageAction.class);

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		Integer role = RequestUtil.getInteger(request, "role",-1);
		String operater = request.getSession().getAttribute("loginuin")+"";
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		request.setAttribute("role", role);
		request.setAttribute("authid", request.getParameter("authid"));
		if(comid==null){
			response.sendRedirect("login.do");
			return null;
		}
		String uid = request.getSession().getAttribute("userid")+"";
		if(comid==0)
			comid = RequestUtil.getLong(request, "comid", 0L);
		if(action.equals("")){
			if(groupid != null && groupid > 0){//集团管理员登录
				request.setAttribute("groupid", groupid);
				if(comid == null || comid == 0){
					Map map = daService.getMap("select id,company_name from com_info_tb where groupid=? order by id limit ? ", 
							new Object[]{groupid, 1});
					if(map != null){
						comid = (Long)map.get("id");
					}else{
						comid = -999L;
					}
				}
			}
			request.setAttribute("comid", comid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			List arrayList = query(request,comid);
			List list = (List<Map<String, Object>>) arrayList.get(0);
			Integer pageNum = (Integer) arrayList.get(1);
			long count = Long.valueOf(arrayList.get(2)+"");
			String fieldsstr = arrayList.get(3)+"";
			setList(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("exportExcel")){
			Map uin = (Map)request.getSession().getAttribute("userinfo");
			if(uin!=null&&uin.get("auth_flag")!=null){
				if(Integer.valueOf(uin.get("auth_flag")+"")==ZLDType.ZLD_ACCOUNTANT_ROLE||Integer.valueOf(uin.get("auth_flag")+"")==ZLDType.ZLD_CARDOPERATOR){
					String ret = "没有权限导出会员数据";
					logger.error(">>>>"+ret);
					AjaxUtil.ajaxOutput(response,ret);
					return null;
				}
			}
			List arrayList = query(request,comid);
			List<Map<String, Object>> list = (List<Map<String, Object>>) arrayList.get(0);
			List<List<String>> bodyList = new ArrayList<List<String>>();
			String [] heards = null;
			if(list!=null&&list.size()>0){
				mongoDbUtils.saveLogs( request,0, 5, "导出会员数量："+list.size());
				//setComName(list);
				String [] f = new String[]{"id","p_name","mobile","uin","name","address","car_number","create_time","b_time","e_time","total","remark"};
				heards = new String[]{"编号","包月产品名称","车主手机","车主账户","名字","地址","车牌号码","购买时间","开始时间","结束时间","金额","备注"};
				for(Map<String, Object> map : list){
					List<String> values = new ArrayList<String>();
					for(String field : f){
						if("car_number".equals(field)){
							values.add(commonMethods.getcar(Long.valueOf(values.get(3)+"")));
						}else if("p_name".equals(field)){
							Map cpMap = daService.getMap("select p_name from product_package_tb where id =? ",new Object[]{Long.valueOf(map.get("p_name")+"")});
							values.add(cpMap.get("p_name")+"");
						}else{
							if("create_time".equals(field)||"b_time".equals(field)||"e_time".equals(field)){
								values.add(TimeTools.getTime_yyyyMMdd_HHmmss(Long.valueOf((map.get(field)+""))*1000));
							}else{
								values.add(map.get(field)+"");
							}
						}
					}
					bodyList.add(values);
				}
			}
			String fname = "会员数据" + com.zld.utils.TimeTools.getDate_YY_MM_DD();
			fname = StringUtils.encodingFileName(fname);
			java.io.OutputStream os;
			try {
				response.reset();
				response.setHeader("Content-disposition", "attachment; filename="
						+ fname + ".xls");
				response.setContentType("application/x-download");
				os = response.getOutputStream();
				ExportExcelUtil importExcel = new ExportExcelUtil("会员数据",
						heards, bodyList);
				importExcel.createExcelFile(os);
			} catch (IOException e) {
				e.printStackTrace();
			}
//			String json = "";
//			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("addcar")){
			String carNumber = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "carnumber"));
			String []cars = new String[]{carNumber};
			if(carNumber.indexOf(",")!=-1){
				cars = carNumber.split(",");//做个限制，要不然出现一个账户下多个车牌
			}
			Long curTime = System.currentTimeMillis()/1000;
			/*if(cars.length>3){
				AjaxUtil.ajaxOutput(response, "每个账户最多绑定三个车牌");
				return null;
			}*/
			Long uin = RequestUtil.getLong(request, "uin", -1L);
			int ret = 0;
			if(carNumber!=null&&uin!=-1){
				//修改原进场车场是包月的,未结算的，改为普通进场
				//删除原车牌
//				try {
//					//优化下如果车牌号未变的话不做任何操作
//					List<String> list = daService.getAll("select car_number from car_info_tb where uin = ?",new Object[]{uin} );
//					HashSet<String> set = new HashSet<String>();
//					for (String str : list) {
//						set.add(str);
//					}
//					for (int i = 0; i < cars.length; i++) {
//						boolean b = set.add(cars[i]);
//						if(b==true){
//							int result = daService.update("update order_tb set c_type=? where uin=? and state=? and c_type=? ", new Object[]{3,uin,0,5});
//							logger.error("管理员uid："+uid+"为会员uin:"+uin+"添加车牌号："+carNumber+"，修改原进场车场是包月的,未结算的，改为普通进场记录条数："+result);
//							break;
//						}
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
				String sql = "insert into car_info_tb(uin,car_number,create_time) values(?,?,?)";
				Set<String> set = new HashSet<String>();
				List<Object[]> values = new ArrayList<Object[]>();
				for(String car :cars){	
					car = car.toUpperCase();
					if(StringUtils.checkPlate(car)){
						set.add(car);
						Map<String, Object> map = daService.getMap("select uin from car_info_tb where car_number=? and uin<>?", new Object[]{car,uin});
						if(map!=null&&map.size()>0){
//							AjaxUtil.ajaxOutput(response, car+" 已在存在！");
//							return null;
							//如果账户没有登录记录，没有在其它车场有月卡信息，可以删除用户
							for (Map.Entry<String, Object> entry : map.entrySet()) {
//								System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
								int dret = deleteUser(comid,Long.parseLong(entry.getValue()+""),car);
								
								Map<String, Object> userMap = daService.getMap("select mobile from user_info_tb where id=? ", new Object[]{entry.getValue()});
								String mobile = "";
								if(userMap != null && userMap.get("mobile") != null){
									mobile = (String)userMap.get("mobile");
									mobile = mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4, mobile.length());
								}
								
								if(dret==-1){
									AjaxUtil.ajaxOutput(response, car+" 已被手机号"+mobile+"绑定,并且已购买了本车场包月产品");
									return null;
								}else if(dret==-2){
									AjaxUtil.ajaxOutput(response, car+" 已被手机号"+mobile+"绑定，请使用该手机号添加月卡");
									return null;
								}else if(dret==-3){
									AjaxUtil.ajaxOutput(response, car+" 车主删除失败");
									return null;
								}else if(dret==1){
									mongoDbUtils.saveLogs( request,0, 4, "删除无效车牌："+car);
								}
							}
						}
						Object []value = new Object[]{uin, car, curTime};
						values.add(value);
					}else{
						AjaxUtil.ajaxOutput(response,"车牌号错误");
						return null;
					}
				}
				if(cars.length!=set.size()){
					AjaxUtil.ajaxOutput(response,"车牌号重复");
					return null;
				}
				if(values.size()==0){
					AjaxUtil.ajaxOutput(response, "车牌号错误");
					return null;
				}
				List ids = daService.getAll("select id,car_number from car_info_tb where uin = ?", new Object[]{uin});
				StringBuffer sBuffer = new StringBuffer();
				for (Object obj : ids) {
					Map map = (Map)obj;
					Long id = Long.valueOf(map.get("id")+"");
					String car = map.get("car_number")+"";
					if(!set.contains(car)){
						Map map1 = daService.getMap("select * from order_tb where car_number = ? and state=? and comid = ?",new Object[]{car,0,comid});
						if(map1!=null){
							AjaxUtil.ajaxOutput(response,"车牌"+car+"车辆在场，请出场或0元结算后再修改车牌");
							return null;
						}
					}
				}
				for (Object obj : ids) {
					Map map = (Map)obj;
					Long id = Long.valueOf(map.get("id")+"");
					sBuffer.append(map.get("car_number"));
					int su = daService.update("delete from car_info_tb where id =? ", new Object[]{id});
					if(su>0){
						if(publicMethods.isEtcPark(comid)){
							int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_info_tb",id,System.currentTimeMillis()/1000,2});
							logger.error("parkadmin or admin:"+operater+" delete comid:"+comid+" car ,add sync ret:"+r);
						}
					}
				}
				if(!values.isEmpty()){
					try{
						ret=daService.bathInsert(sql, values, new int[]{4,12,4});
						logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" car   ret:"+ret);
						if(ret>0){
							List list = daService.getAll("select id from car_info_tb where uin = ?", new Object[]{uin});
							for (Object obj : list) {
								Map map = (Map)obj;
								Object id = map.get("id");
								if(publicMethods.isEtcPark(comid)){
									int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_info_tb",Long.valueOf(id+""),System.currentTimeMillis()/1000,0});
									logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" car ,add sync ret:"+r);
								}
							}
							mongoDbUtils.saveLogs( request,0, 3, "给车主（"+uin+"）修改了车牌："+sBuffer.toString()+"-->"+carNumber);
						}
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			AjaxUtil.ajaxOutput(response, ret+"");
			return null;
		}else if(action.equals("create")){
			String result = buyProduct(request,comid);
			if(result.equals("1")){//短信通知车主
				//车主手机
				String mobile =RequestUtil.processParams(request, "mobile").trim();
				//车牌号码
				String car_number =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number")).toUpperCase();
				Map<String, Object> map = daService.getMap("select company_name from com_info_tb where id=? ", new Object[]{comid});
				String company_name = (String)map.get("company_name");
				String msg = "【重大喜讯】亲爱的"+company_name+"会员：为方便您的停靠，本车场重金引入停车宝，您的车牌号码【"+car_number+"】已录入系统，出入自动抬杆放行。手机下载停车宝APP，随时可更换车牌号，立改立生效，单双号限行也不怕。更可通过APP查看包月详情，办理续费，续费几个月您说了算。另送您会员专享停车券，用本手机号登录停车宝APP即可领取，全市225家车场通用哦！登录www.tingchebao.com火速下载，退订回N【停车宝】";
				//SendMessage.sendMultiMessage(mobile, msg);
			}
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("edit")){
			
			Long id = RequestUtil.getLong(request, "id", -1L);
			//车牌号码
			String car_number =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number")).toUpperCase();
			if(id == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			//Long count = daService.getLong("select count(*) from car_info_tb where car_number=? ", new Object[]{car_number});
			String result = editProduct(request, comid);
			//if(result.equals("1") && count == 0){
				//车主手机
//				String mobile =RequestUtil.processParams(request, "mobile").trim();
//				Map<String, Object> map = daService.getMap("select company_name from com_info_tb where id=? ", new Object[]{comid});
//				String company_name = (String)map.get("company_name");
//				String msg = "【重大喜讯】亲爱的"+company_name+"会员：为方便您的停靠，本车场重金引入停车宝，您的车牌号码【"+car_number+"】已录入系统，出入自动抬杆放行。手机下载停车宝APP，随时可更换车牌号，立改立生效，单双号限行也不怕。更可通过APP查看包月详情，办理续费，续费几个月您说了算。另送您会员专享停车券，用本手机号登录停车宝APP即可领取，全市225家车场通用哦！登录www.tingchebao.com火速下载，退订回N【停车宝】";
//				SendMessage.sendMultiMessage(mobile, msg);
		//	}
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("delete")){
			String id =RequestUtil.processParams(request, "selids");
			Map cpMap = daService.getMap("select uin from carower_product where id =? ",new Object[]{Long.valueOf(id)});
			
			Long uin =-1L;
			if(cpMap!=null)
				uin=(Long)cpMap.get("uin");
			
			int result = daService.update("delete from carower_product where id =?", new Object[]{Long.valueOf(id)});
			if(result==1){
				if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"carower_product",Long.valueOf(id),System.currentTimeMillis()/1000,2});
					logger.error("parkadmin or admin:"+operater+" delete comid:"+comid+" vipuser  ,add sync ret:"+r);
				}else{
					logger.error("parkadmin or admin:"+operater+" delete comid:"+comid+" vipuser");
				}
//				result = deleteUser(comid, uin);
				mongoDbUtils.saveLogs( request,0, 4, "删除了车主（"+uin+"）的套餐");
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("checkmobile")){
			String mobile = RequestUtil.processParams(request, "mobile").trim();
			if(!Check.checkMobile(mobile)){
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("getcar")){
			Long uin = RequestUtil.getLong(request, "uin", -1L);
			String cars = commonMethods.getcar(uin);
			AjaxUtil.ajaxOutput(response, cars);
		}else if(action.equals("renew")){
			String result = buyProduct(request,comid);
			AjaxUtil.ajaxOutput(response, result);
		}
		return null;
	}

	//如果账户没有登录记录，没用绑定微信，没有在其它车场有月卡信息，可以删除用户
	private int deleteUser(long comid,Long uin,String carnumber) {
		//查是否在其它车场购买了包月产品
		List<Map<String,Long>> list = daService.getAll("select p.comid from carower_product c,product_package_tb p where c.uin=? and c.e_time >? and p.id = c.pid ",
				new Object[]{uin,System.currentTimeMillis()/1000});
		if(list!=null&&list.size()>0){
			for(Map<String,Long> map:list){
				if(map.get("comid").longValue()==comid)
					return -1;
			}
			return -2;
		}
		Map userMap = daService.getMap("select balance,logon_time,wxp_openid from user_info_tb where id =?", new Object[]{uin});
		if(userMap!=null){
			Double balance = StringUtils.formatDouble(userMap.get("balance"));
			Long logoTime = (Long)userMap.get("logon_time");
			String wxp_openid = userMap.get("wxp_openid")+"";
			if(logoTime!=null||balance>0||(wxp_openid!=null&&!wxp_openid.equals("")&&!wxp_openid.equals("null"))){//不可以删除用户
				return -2;
			}
			//开始删除
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			
			//删除用户信息
			Map<String, Object> userSqlMap = new HashMap<String, Object>();
			List<Map> ids = daService.getAll("select id from car_info_tb where uin = ? and car_number=?", new Object[]{uin,carnumber});
			for (Map map : ids) {
				//删除车牌信息
				long id = Long.parseLong(map.get("id")+"");
				Map<String, Object> carSqlMap = new HashMap<String, Object>();
				carSqlMap.put("sql", "delete from car_info_tb where id = ?");
				carSqlMap.put("values", new Object[]{id});
				bathSql.add(carSqlMap);
			}
//			userSqlMap.put("sql", "delete from user_info_tb where id = ?");
//			userSqlMap.put("values", new Object[]{uin});
//			bathSql.add(userSqlMap);
			boolean ret = daService.bathUpdate(bathSql);
			if(!ret)
				return -3;
			else{
				if (publicMethods.isEtcPark(comid)) {
					for (Map map : ids) {
						long id = Long.parseLong(map.get("id")+"");
						int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_info_tb",id,System.currentTimeMillis()/1000,2});
						logger.error("parkadmin or admin delete carnumber uin:"+uin+" ,add sync ret:"+re);
					}
				}
			}
		}
		
		return 1;
	}


	//注册包月会员 
	private String buyProduct(HttpServletRequest request, Long comid){
		//包月产品
		Long pid =RequestUtil.getLong(request, "p_name",-1L);
		//车主手机
		String mobile =RequestUtil.processParams(request, "mobile").trim();
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name").trim());
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address").trim());
		//车牌号码
		//String car_number =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number")).toUpperCase();
		//起始时间
		String b_time =RequestUtil.processParams(request, "b_time");
		//购买月数
		Integer months = RequestUtil.getInteger(request, "months", 1);
		
		Integer flag = RequestUtil.getInteger(request, "flag", -1);
		//备注
		String remark = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));
		
		//停车位编号
		String p_lot = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "p_lot")).trim();
		//实收金额
		String acttotal = RequestUtil.processParams(request, "act_total");
		
		Long ntime = System.currentTimeMillis()/1000;
		Long btime = TimeTools.getLongMilliSecondFrom_HHMMDD(b_time)/1000;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(btime*1000);
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+months);
		Long etime = calendar.getTimeInMillis()/1000;
		
		//金额
		Double total= commonMethods.getProdSum(pid, months);//RequestUtil.getDouble(request, "total", 0d);
		
		Map pMap = daService.getMap("select limitday,price from product_package_tb where id=? ", new Object[]{pid});
		Long limitDay = null;//pMap.get("limitday");
		if(pMap!=null&&pMap.get("limitday")!=null){
			limitDay = (Long)pMap.get("limitday");
		}
		if(limitDay!=null){
			if(limitDay<etime){//超出有效期
				return "-2";
			}
		}
		
		Double act_total = total;
		if(!acttotal.equals("")){
			act_total = Double.valueOf(acttotal);
		}
		
		Map userMap = daService.getMap("select id from user_info_Tb where mobile=? and auth_flag=? ", new Object[]{mobile,4});
		
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		Map<String, Object> userSqlMap = new HashMap<String, Object>();
		Map<String, Object> recomSqlMap = new HashMap<String, Object>();
		Map<String, Object> carInfoMap = new HashMap<String, Object>();
	    Map<String, Object> carowerPackMap = new HashMap<String, Object>();
	    Long uin =-1L;
	    boolean f = true;
		if(userMap==null){//车主未注册
			uin = daService.getkey("seq_user_info_tb");
			userSqlMap.put("sql", "insert into user_info_tb (id,strid,nickname,mobile,auth_flag,reg_time,media) values(?,?,?,?,?,?,?)");
			userSqlMap.put("values", new Object[]{uin,"carower_"+uin,"车主",mobile,4,ntime,10});
			bathSql.add(userSqlMap);
			
			//写入记录表，用户通过注册月卡会员注册车主
			recomSqlMap.put("sql", "insert into recommend_tb(nid,type,state,create_time,comid) values(?,?,?,?,?)");
			recomSqlMap.put("values", new Object[]{uin,0,0,System.currentTimeMillis(),comid});
			bathSql.add(recomSqlMap);
		}else {
			f=false;
			uin = (Long)userMap.get("id");
		}
		if(uin==null||uin==-1)
			return "-1";
		
		String result = commonMethods.checkplot(comid, p_lot, btime, etime, -1L);
		if(result != null){
			return result;
		}
		
		Long nextid = daService.getkey("seq_carower_product");
		carowerPackMap.put("sql", "insert into carower_product (id,uin,pid,create_time,b_time,e_time,total,remark,name,address,p_lot,act_total) values(?,?,?,?,?,?,?,?,?,?,?,?)");
		carowerPackMap.put("values", new Object[]{nextid,uin,pid,ntime,btime,etime,total,remark,name,address,p_lot,act_total});
		bathSql.add(carowerPackMap);
		if(daService.bathUpdate(bathSql)){
			String operater = request.getSession().getAttribute("loginuin")+"";
			if(publicMethods.isEtcPark(comid)){
//				if(f){
					int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"user_info_tb",uin,System.currentTimeMillis()/1000,0});
					logger.error("parkadmin or admin:"+operater+" add  comid:"+comid+" user ,add sync ret:"+re);
					if(uin>-1){
						List list = daService.getAll("select id from car_info_tb where uin = ?", new Object[]{uin});
						for (Object obj : list) {
							Map map = (Map)obj;
							Long carid = Long.parseLong(map.get("id")+"");
							if(carid!=null&&carid>0){
								daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_info_tb",carid,System.currentTimeMillis()/1000,0});
							}
						}
					}
//				}
				int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"carower_product",nextid,System.currentTimeMillis()/1000,0});
				logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" vipuser ,add sync ret:"+r);
			}
			mongoDbUtils.saveLogs( request,0, 2, "车主"+mobile+"购买了套餐（编号："+pid+"）,金额："+act_total);
			return "1";
		}else {
			return "-1";
		}
	}

	//编辑包月会员 
	@SuppressWarnings({ "rawtypes" })
	private String editProduct(HttpServletRequest request, Long comid){
		Long id = RequestUtil.getLong(request, "id", -1L);
		//包月产品
		Long pid =RequestUtil.getLong(request, "p_name",-1L);
		//车主手机
		String mobile =RequestUtil.processParams(request, "mobile").trim();
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name").trim());
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address").trim());
		//车牌号码
	//	String car_number =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number")).toUpperCase();
		//起始时间
		String b_time =RequestUtil.processParams(request, "b_time");
		//购买月数
		Integer months = RequestUtil.getInteger(request, "months", 1);
				
		Integer flag = RequestUtil.getInteger(request, "flag", -1);
		//备注
		String remark = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));
		
		//停车位编号
		String p_lot = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "p_lot"));
		//实收金额
		String acttotal = RequestUtil.processParams(request, "act_total");
		//金额
		Double total = commonMethods.getProdSum(pid, months);//RequestUtil.getDouble(request, "total", 0d);
				
		Map pMap = daService.getMap("select limitday,price from product_package_tb where id=? ", new Object[]{pid});
				
		Long ntime = System.currentTimeMillis()/1000;
		Long btime = TimeTools.getLongMilliSecondFrom_HHMMDD(b_time)/1000;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(btime*1000);
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+months);
		Long etime = calendar.getTimeInMillis()/1000;
				
		Long limitDay = null;//pMap.get("limitday");
		if(pMap!=null&&pMap.get("limitday")!=null){
			limitDay = (Long)pMap.get("limitday");
		}
		if(limitDay!=null){
			if(limitDay<etime){//超出有效期
				return "-2";
			}
		}

		Double act_total = total;
		if(!acttotal.equals("")){
			act_total = Double.valueOf(acttotal);
		}
		
		Map userMap = daService.getMap("select id from user_info_Tb where mobile=? and auth_flag=? ", new Object[]{mobile,4});
				
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		Map<String, Object> userSqlMap = new HashMap<String, Object>();
		Map<String, Object> recomSqlMap = new HashMap<String, Object>();
		//Map<String, Object> carInfoMap = new HashMap<String, Object>();
		Map<String, Object> carowerPackMap = new HashMap<String, Object>();
		Long uin =-1L;
		if(userMap==null){//车主未注册
			uin = daService.getkey("seq_user_info_tb");
			userSqlMap.put("sql", "insert into user_info_tb (id,strid,nickname,mobile,auth_flag,reg_time,media) values(?,?,?,?,?,?,?)");
			userSqlMap.put("values", new Object[]{uin,"carower_"+uin,"车主",mobile,4,ntime,10});
			bathSql.add(userSqlMap);
					
			//写入记录表，用户通过注册月卡会员注册车主
			recomSqlMap.put("sql", "insert into recommend_tb(nid,type,state,create_time,comid) values(?,?,?,?,?)");
			recomSqlMap.put("values", new Object[]{uin,0,0,System.currentTimeMillis(),comid});
			bathSql.add(recomSqlMap);
		}else {
			uin = (Long)userMap.get("id");
		}
		if(uin==null||uin==-1)
			return "-1";
				
		String result = commonMethods.checkplot(comid, p_lot, btime, etime, id);
		if(result != null){
			return result;
		}
		
		carowerPackMap.put("sql", "update carower_product set uin=?,pid=?,create_time=?,b_time=?,e_time=?,total=?,remark=?,name=?,address=?,p_lot=?,act_total=? where id=? ");
		carowerPackMap.put("values", new Object[]{uin,pid,ntime,btime,etime,total,remark,name,address,p_lot,act_total,id});
		bathSql.add(carowerPackMap);
		if(daService.bathUpdate(bathSql)){
			String operater = request.getSession().getAttribute("loginuin")+"";
			if(bathSql.size()==1){
				if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"carower_product",id,System.currentTimeMillis()/1000,1});
					logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" vipuser ,add sync ret:"+r);
				}
			}else{
				if(publicMethods.isEtcPark(comid)){
					int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"user_info_tb",uin,System.currentTimeMillis()/1000,0});
					logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" user ,add sync ret:"+re);
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"carower_product",id,System.currentTimeMillis()/1000,1});
					logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" vipuser ,add sync ret:"+r);
				}
			}
			mongoDbUtils.saveLogs( request,0,3, "修改了车主"+mobile+"的套餐（编号："+pid+"）");
			return "1";
		}else {
			return "-1";
		}
	}

	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		String mobile = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "mobile"));
		String p_name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "p_name"));
		String car_nubmer = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "car_number"));
		SqlInfo sqlInfo1 = null;
		SqlInfo sqlInfo2 = null;
		SqlInfo sqlInfo3 = null;
		if(!mobile.equals("")){
			sqlInfo1 = new SqlInfo(" u.mobile like ? ",new Object[]{"%"+mobile+"%"});
		}
		if(!p_name.equals("")){
			sqlInfo3 = new SqlInfo(" p.p_name like ?  ",new Object[]{"%"+p_name+"%"});
		}
		if(!car_nubmer.equals("")){
			sqlInfo2 = new SqlInfo(" c.uin in (select uin from car_info_tb where car_number like ?)  ",new Object[]{"%"+car_nubmer+"%"});
		}
		if(sqlInfo1!=null){
			if(sqlInfo2!=null)
				sqlInfo1 = SqlInfo.joinSqlInfo(sqlInfo1, sqlInfo2, 2);
			if(sqlInfo3!=null)
				sqlInfo1 = SqlInfo.joinSqlInfo(sqlInfo1, sqlInfo3, 2);
			return sqlInfo1;
		}else if(sqlInfo2!=null){
			if(sqlInfo3!=null)
				sqlInfo2 = SqlInfo.joinSqlInfo(sqlInfo2, sqlInfo3, 2);
			return sqlInfo2;
		}
		return sqlInfo3;
	}
	
	private void setList(List<Map<String,Object>> list){
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				Long b_time = (Long)map.get("b_time");
				Long e_time = (Long)map.get("e_time");
				Integer months = Math.round((e_time - b_time)/(30*24*60*60));
				map.put("months", months);
			}
		}
	}
	private List query(HttpServletRequest request,long comid){
		ArrayList arrayList = new ArrayList();
		List comsList = pService.getAll("select * from com_info_tb where pid = ?",new Object[]{comid});
		List<Object> params = new ArrayList<Object>();
		String sqlparm = "";
		for (int i = 1; i < comsList.size()+1; i++) {
			sqlparm += " or p.comid = ? ";
		}
		String sql = "select p.id p_name,c.uin,c.id,c.name,c.address,c.create_time,c.b_time ,c.e_time ,c.remark,c.total,u.mobile,c.p_lot,c.act_total from " +
		"product_package_tb p,carower_product c ,user_info_tb u where c.pid=p.id and u.id=c.uin and (p.comid=? "+sqlparm+")";
		String countSql = "select count(c.id) from product_package_tb p,carower_product c ,user_info_tb u " +
				"where c.pid=p.id and  u.id=c.uin and (p.comid=? "+sqlparm +")";
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
		String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
		SqlInfo sqlInfo = RequestUtil.customSearch(request, "c_product", "c", new String[]{"mobile", "car_number", "p_name"});
		SqlInfo ssqlInfo = getSuperSqlInfo(request);
		if(sqlInfo!=null){
			if(ssqlInfo!=null)
				sqlInfo = SqlInfo.joinSqlInfo(sqlInfo,ssqlInfo, 2);
			countSql+=" and "+ sqlInfo.getSql();
			sql +=" and "+sqlInfo.getSql();
			params= sqlInfo.getParams();
		}else if(ssqlInfo!=null){
			countSql+=" and "+ ssqlInfo.getSql();
			sql +=" and "+ssqlInfo.getSql();
			params= ssqlInfo.getParams();
		}
		params.add(0,comid);
		for (int i = 1; i < comsList.size()+1; i++) {
			long comidoth = Long.parseLong(((Map)comsList.get(i-1)).get("id")+"");
			params.add(i,comidoth);
//			sqlparm += " or p.comid = ? ";
		}
		Integer p_name = RequestUtil.getInteger(request, "p_name_start",-1);
		String relation = "";
		if(p_name > 0){
			relation = " and p.id = ?";
			params.add(params.size(),Long.valueOf(p_name));
		}
		//System.out.println(sqlInfo);
		Long count=daService.getCount(countSql+relation, params);
		List list = null;//daService.getPage(sql, null, 1, 20);
		if(count>0){
			String orderby="id";
			String sort="desc";
			String orderfield = RequestUtil.processParams(request, "orderfield");
			String reqorderby = RequestUtil.processParams(request, "orderby");
			if(StringUtils.isNotNull(orderfield))
				orderby=orderfield;
			if(StringUtils.isNotNull(orderfield))
				sort=reqorderby;
			list = daService.getAll(sql+ relation+" order by "+orderby+" "+sort, params, pageNum, pageSize);
		}
		arrayList.add(list);
		arrayList.add(pageNum);
		arrayList.add(count);
		arrayList.add(fieldsstr);
		return arrayList;
	}
}