package com.zld.struts.request;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
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
import com.zld.pojo.Berth;
import com.zld.pojo.BerthSeg;
import com.zld.pojo.Card;
import com.zld.pojo.Order;
import com.zld.pojo.WorkRecord;
import com.zld.service.DataBaseService;
import com.zld.service.P4ReadService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.HttpProxy;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;

public class SyncFromP4ToP5Action extends Action {
	@Autowired
	private PgOnlyReadService readService;
	@Autowired
	private DataBaseService writeService;
	@Autowired
	private P4ReadService p4ReadService;
	/*long groupId = 28;//晋中集团编号28
	long roleId = 312L;//晋中收费员角色312
*/	int etc = 4;//pos机照牌车场	

	String condition = "tenantid%3D2";//晋中的条件
	//String condition = "companyid%3D17";//鹰潭
	//String condition = "tenantid%3D1016";//鹰潭
	
	//拷贝桂林集团数据到测试集团
	long groupId = 43;//拷贝鹰潭线上数据到测试集团
	long roleId = 460L;//

	long tenantid = 1016;//p4鹰潭的商户编号
	/*long groupId = 42;//鹰潭新数据集团
	long roleId = 450L;//鹰潭新数据集团,收费员
*/	
//	int etc = 4;//pos机照牌车场
	long source_group_id = 42;//p5之间拷贝数据时的数据源运营集团编号，桂林市鸿图房地产经纪有限公司
	
	Logger logger = Logger.getLogger(SyncFromP4ToP5Action.class);
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		if(action.equals("jinzhongcard")){
			//http://127.0.0.1/zld/syncdata.do?action=jinzhongcard&r=49
		
//			List<Map<String, Object>> data= p4ReadService.getAll("select * from " +
//					" (select my_table.*,rownum as my_rownum from " +
//					" (select  t.CardNo,p.PlateNumber  from ExtOtherAccount t,ExtOtherPlateNumber p " +
//					" where TelePhone is null  and t.id " +
//					" in(select AssignedOtherAccountId from  ExtOtherPlateNumber )" +
//					") my_table where rownum< ?) where  my_rownum>= ? ",new Object[]{100,1}) ;
			
//			List<Map<String, Object>> data= p4ReadService.getAll("select top 100 ta.*,rownum from(select t.CardNo,p.PlateNumber  from ExtOtherAccount t,ExtOtherPlateNumber p " +
//					" where TelePhone is null  and t.id " +
//					" in(select AssignedOtherAccountId from  ExtOtherPlateNumber) ) ta where rownum>100 ",new Object[]{}) ;
//			List<Map<String, Object>> data= //new ArrayList<Map<String,Object>>();
//					p4ReadService.getAll("select  t.id,t.CardNo,p.PlateNumber " +
//					" from ExtOtherAccount t left join ExtOtherPlateNumber p " +
//					" on p.AssignedOtherAccountId = t.id where TelePhone is null  and t.id " +
//					" in(select AssignedOtherAccountId from  ExtOtherPlateNumber) and t.id >9947 order by t.id  ",new Object[]{}) ;
////			Long count = p4ReadService.getCount("select count(*) from  ExtOtherPlateNumber where AssignedOtherAccountId=53 ", null); 
//			logger.error(count);
			BufferedReader reader =null;
			JSONArray array = null;
			String data = "";
			try {
				reader = new BufferedReader(new FileReader2("d:\\park.txt","utf-8"));
				String lineString ="";
				while ((lineString = reader.readLine()) != null) {
					data+=lineString;
					array = JSONArray.fromObject(data);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			String parstr = "";
			Map<String, String> cardCarNumber = new HashMap<String, String>();
			if(array!=null){
				for(int i=0;i<array.size();i++){
					JSONObject o = array.getJSONObject(i);
					if(i==0)
						parstr = "?";
					else {
						parstr +=",?";
					}
					String cardNo=o.getString("CardNo").toLowerCase();
					cardCarNumber.put(cardNo, o.getString("PlateNumber"));
					params.add(cardNo);
				}
			}
			List<Map<String, Object>> list = readService.getAllMap(
					"select id,nfc_uuid from com_nfc_tb where state=? and  nfc_uuid in("+parstr+")",
					params);
			System.err.println(list.size());
			String nfcsql = "update com_nfc_tb set state=? where id =? ";
			String carNumberSql = "insert into card_carnumber_tb (card_id,car_number,create_time) values(?,?,?)";
			List<Object[]> p1 = new ArrayList<Object[]>();
			List<Object[]> p2 = new ArrayList<Object[]>();
			Long ntime = System.currentTimeMillis()/1000;
			for(Map<String, Object> map : list){
				Object[] o1 = new Object[]{4,map.get("id")};
				String nfcUUID  =(String)map.get("nfc_uuid");
				Object[] o2 = new Object[]{map.get("id"),cardCarNumber.get(nfcUUID),ntime};
				p1.add(o1);
				p2.add(o2);
			}
//			for(Object[] objects : p1){
//				System.err.println(StringUtils.objArry2String(objects));
//			}
//			for(Object[] objects : p2){
//				System.err.println(StringUtils.objArry2String(objects));
//			}
			//System.err.println(data);
			System.err.println(p1.size());
			System.err.println(p2.size());
//			int i1=writeService.bathInsert(nfcsql, p1, new int[]{4,4});
//			int i2=writeService.bathInsert(carNumberSql, p2, new int[]{4,12,4});
//			System.err.println(i1+"---"+i2);
			AjaxUtil.ajaxOutput(response,StringUtils.createJson(list));
			return null;
		}else if(action.equals("park")){
			syncPark();
			//http://127.0.0.1/zld/syncdata.do?action=park
		}else if(action.equals("berthseg")){
			syncBerthSeg();
			//http://127.0.0.1/zld/syncdata.do?action=berthseg
		}else if(action.equals("sensor")){
			syncSensor();
			//http://127.0.0.1/zld/syncdata.do?action=sensor
		}else if(action.equals("berth")){
			syncBerth();
			//http://127.0.0.1/zld/syncdata.do?action=berth
		}else if(action.equals("site")){
			syncSite();
			//http://127.0.0.1/zld/syncdata.do?action=site
		}else if(action.equals("parkuser")){
			syncParkUser();
			//http://127.0.0.1/zld/syncdata.do?action=parkuser
		}else if(action.equals("workgroup")){
			syncWorkgroup();
			//http://127.0.0.1/zld/syncdata.do?action=workgroup
		}else if(action.equals("workgroupberthseg")){
			syncWorkGroupBerthSeg();
			//http://127.0.0.1/zld/syncdata.do?action=workgroupberthseg
		}else if(action.equals("workgroupparkuser")){
			syncWorkGroupPuser();
			//http://127.0.0.1/zld/syncdata.do?action=workgroupparkuser
		}else if(action.equals("card")){
			syncCard();
			//http://127.0.0.1/zld/syncdata.do?action=card
		}else if(action.equals("order")){
			syncOrder();
			//http://127.0.0.1/zld/syncdata.do?action=order
		}else if(action.equals("syncall")){
			syncPark();
			syncBerthSeg();
			syncSensor();
			syncBerth();
			syncSite();
			syncParkUser();
			syncWorkgroup();
			syncWorkGroupBerthSeg();
			syncWorkGroupPuser();
			syncCard();
			syncOrder();
		}else if(action.equals("syncorder")){//命令
			String url = "http://180.150.188.224:8080/zld/syncdata.do?action=";
			String ret = "接口的调用顺序不可改变，后面的数据导入依赖前面的导入，" +
					"如果一个接口重复调用会删除该接口之前已导入的数据，并且此接口后面的接口都要手动重新按顺序导入。<br><br><br>";
			
			ret += "同步车场数据接口:"+url + "park" + "<br><br>";
			ret += "同步泊位段数据接口:"+url + "berthseg" + "<br><br>";
			ret += "同步车检器数据接口:"+url + "sensor" + "<br><br>";
			ret += "同步泊位数据接口:"+url + "berth" + "<br><br>";
			ret += "同步基站数据接口:"+url + "site" + "<br><br>";
			ret += "同步收费员数据接口:"+url + "parkuser" + "<br><br>";
			ret += "同步工作组数据接口:"+url + "workgroup" + "<br><br>";
			ret += "同步工作组泊位段映射数据接口:"+url + "workgroupberthseg" + "<br><br>";
			ret += "同步工作组收费员映射数据接口:"+url + "workgroupparkuser" + "<br><br>";
			ret += "同步卡片数据接口:"+url + "card" + "<br><br>";
			ret += "同步逃单数据接口:"+url + "order" + "<br>";
			AjaxUtil.ajaxOutput(response, ret);
		}else if(action.equals("p3orderbyname")){
			syncP3OrderByName();
			//http://127.0.0.1/zld/syncdata.do?action=p3orderbyname
		}else if(action.equals("p4orderbyname")){
			syncP4OrderByName();
			//http://127.0.0.1/zld/syncdata.do?action=p4orderbyname
		}else if(action.equals("copypark")){
			copyPark();
			//http://127.0.0.1/zld/syncdata.do?action=copypark
		}else if(action.equals("copyberthseg")){
			copyBerthSeg();
			//http://127.0.0.1/zld/syncdata.do?action=copyberthseg
		}else if(action.equals("copyberth")){
			copyBerth();
			//http://127.0.0.1/zld/syncdata.do?action=copyberth
		}else if(action.equals("copyparkuser")){
			copyParkUser();
			//http://127.0.0.1/zld/syncdata.do?action=copyparkuser
		}else if(action.equals("backonline2offline")){
			backonline2offline();
			//http://127.0.0.1/zld/syncdata.do?action=backonline2offline
		}else if(action.equals("test")){
			String i = RequestUtil.processParams(request, "i");
			AjaxUtil.ajaxOutput(response, i);
		}else if(action.equals("repeatcard")){
			getRepeatCard();
		}else if(action.equals("syncOldNopaymentData")){
			syncOldNopaymentData();
			//http://127.0.0.1/zld/syncdata.do?action=syncOldNopaymentData
		}else if(action.equals("syncOldCardData")){
			syncOldCardData();
			//http://127.0.0.1/zld/syncdata.do?action=syncOldCardData
		}else if(action.equals("syncOldCardActData")){
			syncOldCardActData();
			//http://127.0.0.1/zld/syncdata.do?action=syncOldCardActData
		}else if(action.equals("syncCardAnly")){
			syncCardAnly();
			//http://127.0.0.1/zld/syncdata.do?action=syncCardAnly
		}else if(action.equals("consumeWrongCard")){
			consumeWrongCard();
			//http://127.0.0.1/zld/syncdata.do?action=consumeWrongCard
		}else if(action.equals("copyBerthOrder")){
			copyBerthOrder();
			//http://127.0.0.1/zld/syncdata.do?action=copyBerthOrder
		}
		return null;
	}
	
	private void getRepeatCard(){
		try {
			long groupId = 28;
			boolean stop = false;
			Long maxId = -1L;
			List<String> nfc_uuidList = new ArrayList<String>();
			while (!stop) {
				logger.error("maxId:"+maxId);
				Map<String, Object> map = readService.getMap("select id,nfc_uuid from " +
						" com_nfc_tb where group_id=? and type=? and id>? and is_delete=?" +
						" order by id limit ?", new Object[]{groupId, 2, maxId, 0, 1});
				if(map == null){
					break;
				}
				Long id = (Long)map.get("id");
				maxId = id;
				String nfc_uuid = (String)map.get("nfc_uuid");
				String inner_uuid = nfc_uuid.substring(0, 14);
				if(nfc_uuidList.contains(inner_uuid)){
					logger.error("已存在重复记录,inner_uuid:"+inner_uuid);
					continue;
				}
				Long count = readService.getLong("select count(id) from com_nfc_tb" +
						" where group_id=? and type=? and is_delete=? and nfc_uuid like ?",
						new Object[]{groupId, 2, 0, "%" + inner_uuid + "%"});
				if(count > 1){
					logger.error("卡片编号重复，nfc_uuid:"+nfc_uuid+",inner_uuid:"+inner_uuid+",count:"+count);
					nfc_uuidList.add(inner_uuid);
					FileWriter fw=new FileWriter("D:\\nfc.txt", true);
					fw.write(inner_uuid);
					fw.write("\r\n");
					fw.flush();
					fw.close();
				}
				Thread.sleep(200);
			}
			for(String card : nfc_uuidList){
				System.out.println(card);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void deldirty(){
		List<Map<String, Object>> list = readService.getAll("select * from order_tb where car_number in" +
				" ('赣L6A889','赣LJ3312','赣LD3336','赣A7P508','赣LW4653','赣LD3336','赣LD3336','赣LA2098'," +
				"'赣A7P508','粤DSA805','粤DSA805')", new Object[]{});
		
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		String id = "";
		for(Map<String, Object> map : list){
			String car_number = (String)map.get("car_number");
			Double total = Double.valueOf(map.get("total") + "");
			Long create_time = (Long)map.get("create_time");
			Long comid = (Long)map.get("comid");
			Long end_time = (Long)map.get("end_time");
			Long uid = (Long)map.get("uid");
			Long out_uid = (Long)map.get("out_uid");
			
			Long nopayId = -1L;
			if(car_number.equals("赣L6A889")
					&& total == 4){
				nopayId = 18290246L;
			}else if(car_number.equals("赣LJ3312")
					&& total == 3){
				nopayId = 18292864L;
			}else if(car_number.equals("赣LD3336")
					&& total == 11){
				nopayId = 18301855L;
			}else if(car_number.equals("赣A7P508")
					&& total == 3){
				nopayId = 18303865L;
			}else if(car_number.equals("赣LW4653")
					&& total == 3){
				nopayId = 18305008L;
			}else if(car_number.equals("赣LD3336")
					&& total == 2){
				nopayId = 18307067L;
			}else if(car_number.equals("赣LD3336")
					&& total == 4){
				nopayId = 18307600L;
			}else if(car_number.equals("赣LA2098")
					&& total == 2){
				nopayId = 18308424L;
			}else if(car_number.equals("赣A7P508")
					&& total == 4){
				nopayId = 18309086L;
			}else if(car_number.equals("粤DSA805")
					&& total == 2
					&& create_time == 1467159763L){
				nopayId = 18309882L;
			}else if(car_number.equals("粤DSA805")
					&& total == 2
					&& create_time == 1467166724L){
				nopayId = 18309934L;
			}
			
			long orderId = writeService.getkey("seq_order_tb");
			id += ","+orderId;
			//订单表
			Map<String, Object> orderSqlMap = new HashMap<String, Object>();
			orderSqlMap.put("sql", "insert into order_tb (id,create_time,comid,end_time,uid," +
					"car_number,berthnumber,berthsec_id,out_uid,total,uin,state,groupid," +
					"c_type,pay_type) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			orderSqlMap.put("values", new Object[]{orderId, create_time, comid, end_time, uid,
					car_number, -1, -1, out_uid, total, -1, 1, 42, 2, 1});
			bathSql.add(orderSqlMap);
			//逃单表
			Map<String, Object> pursueSqlMap = new HashMap<String, Object>();
			pursueSqlMap.put("sql", "update no_payment_tb set order_id=? where order_id=? ");
			pursueSqlMap.put("values", new Object[]{orderId, nopayId});
			bathSql.add(pursueSqlMap);
			//明细表
			Map<String, Object> accSqlMap = new HashMap<String, Object>();
			accSqlMap.put("sql", "update parkuser_cash_tb set orderid=? where orderid=? ");
			accSqlMap.put("values", new Object[]{orderId, nopayId});
			bathSql.add(accSqlMap);
		}
		System.out.println(id);
		boolean b = writeService.bathUpdate2(bathSql);
		System.out.println(b);
	}
	
	private void backonline2offline(){
		try {
			List<Map<String, Object>> list = readService.getAll("select * from order_tb where id in" +
					" (select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)" +
					" and (berthnumber =? or berthsec_id=?) ", new Object[]{4, 42, -1, -1});
			
			if(list != null && !list.isEmpty()){
				//------------------------------批量插入订单----------------------------//
				String addOrder = "insert into order_tb (create_time,comid,end_time,uid," +
						"car_number,berthnumber,berthsec_id,out_uid,total,uin,state,groupid," +
						"c_type) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
				List<Object[]> orderValues = new ArrayList<Object[]>();
				for(Map<String, Object> map : list){
					Long create_time = (Long)map.get("create_time");
					Long comid = (Long)map.get("comid");
					Long end_time = (Long)map.get("end_time");
					Long uid = (Long)map.get("uid");
					String car_number = (String)map.get("car_number");
					Long out_uid = (Long)map.get("out_uid");
					Long berthnumber = (Long)map.get("berthnumber");
					Long berthsec_id = (Long)map.get("berthsec_id");
					Double total = Double.valueOf(map.get("total") + "");
					//--------------订单表----------------//
					Object[] va = new Object[13];
					va[0] = create_time;
					va[1] = comid;
					va[2] = end_time;
					va[3] = uid;
					va[4] = car_number;
					va[5] = berthnumber;
					va[6] = berthsec_id;
					va[7] = out_uid;
					va[8] = total;
					va[9] = -1;
					va[10] = 2;
					va[11] = 42;
					va[12] = 2;
					orderValues.add(va);
				}
				int order = writeService.bathInsert(addOrder, orderValues, new int []{4,4,4,4,12,4,4,4,3,4,4,4,4});
				logger.error("批量插入结果>>>orderMap:"+order);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void copyParkUser(){
		int table_type = 5;
		clearData(table_type);
		boolean stop = false;
		try {
			Long curTime = System.currentTimeMillis()/1000;
			int pageSize = 500;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(table_type);
				List<Map<String, Object>> list = readService.getAll("select id,nickname from " +
						" user_info_tb where state=? and id>? and groupid=? order by id limit ? ", 
						new Object[]{0, maxId, source_group_id, pageSize});
				if(list == null || list.isEmpty()){
					logger.error("退出循环，数据已经复制完毕");
					return;
				}
				if(list.size() < pageSize){
					logger.error("最后一次循环>>>size:"+list.size());
					stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
				}
				List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
				Long lastId = maxId;//上一个的p4主键编号
				for(Map<String, Object> map : list){
					logger.error("map:"+map.toString());
					Long p4Id = (Long)map.get("id");
					String nickname = (String)map.get("nickname");
					if(p4Id == null
							|| p4Id <= 0){
						logger.error("返回信息错误>>>maxId:"+maxId);
						return;
					}
					//----------------------校验传过来的数据是否是正序排序------------------------//
					if(p4Id.intValue() <= lastId.intValue()){
						logger.error("停止循环，传过来的参数排序错误>>>p4Id:"+p4Id);
						return;
					}
					lastId = p4Id;
					//-------------------------注册收费员-----------------------------------//
					long userId=writeService.getkey("seq_user_info_tb");
					//映射表
					Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
					carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
							" values (?,?,?,?)");
					carMapSqlMap.put("values", new Object[]{table_type, p4Id, userId, groupId});
					bathSql.add(carMapSqlMap);
					//收费员
					String strid = userId + "zld";
					Map<String, Object> carSqlMap = new HashMap<String, Object>();
					carSqlMap.put("sql", "insert into user_info_tb (id,nickname,password,strid,reg_time," +
							"mobile,groupid,role_id,auth_flag,creator_id) values (?,?,?,?,?,?,?,?,?,?)");
					carSqlMap.put("values", new Object[]{userId, nickname, strid, strid, curTime, null,
							groupId, roleId, 2, -1L});
					bathSql.add(carSqlMap);
				}
				boolean b = writeService.bathUpdate2(bathSql);
				logger.error("结果>>>b:"+b+",maxId:"+maxId);
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void copyBerthSeg(){
		int table_type = 2;
		clearData(table_type);
		boolean stop = false;
		try {
			Long curTime = System.currentTimeMillis()/1000;
			int pageSize = 500;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(table_type);
				List<Map<String, Object>> list = readService.getAll("select id,berthsec_name,comid from " +
						" com_berthsecs_tb where is_active=? and id>? and comid in (select id from com_info_tb" +
						" where groupid=? and state<>?) order by id limit ? ", 
						new Object[]{0, maxId, source_group_id, 1, pageSize});
				if(list == null || list.isEmpty()){
					logger.error("退出循环，数据已经复制完毕");
					return;
				}
				if(list.size() < pageSize){
					logger.error("最后一次循环>>>size:"+list.size());
					stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
				}
				List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
				Long lastId = maxId;//上一个的p4主键编号
				for(Map<String, Object> map : list){
					logger.error("map:"+map.toString());
					Long p4Id = (Long)map.get("id");
					String berthsec_name = (String)map.get("berthsec_name");
					Long comid = (Long)map.get("comid");
					if(p4Id == null
							|| p4Id <= 0){
						logger.error("返回信息错误>>>maxId:"+maxId);
						return;
					}
					//----------------------校验传过来的数据是否是正序排序------------------------//
					if(p4Id.intValue() <= lastId.intValue()){
						logger.error("停止循环，传过来的参数排序错误>>>p4Id:"+p4Id);
						return;
					}
					lastId = p4Id;
					//-----------------------------车场映射-----------------------------//
					comid = getMappingId(comid, 1);
					if(comid <= 0){
						logger.error("停止循环，没有找到对应的车场");
						return;
					}
					//-------------------------注册泊位段数据------------------------------//
					long berthSegId=writeService.getkey("seq_com_berthsecs_tb");
					//映射表
					Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
					carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
							" values (?,?,?,?)");
					carMapSqlMap.put("values", new Object[]{table_type, p4Id, berthSegId, groupId});
					bathSql.add(carMapSqlMap);
					//泊位段
					Map<String, Object> carSqlMap = new HashMap<String, Object>();
					carSqlMap.put("sql", "insert into com_berthsecs_tb(id,berthsec_name,create_time,comid)" +
							" values(?,?,?,?)");
					carSqlMap.put("values", new Object[]{berthSegId, berthsec_name, curTime, comid});
					bathSql.add(carSqlMap);
				}
				boolean b = writeService.bathUpdate2(bathSql);
				logger.error("结果>>>b:"+b+",maxId:"+maxId);
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void copyBerth(){
		int table_type = 3;
		clearData(table_type);
		boolean stop = false;
		try {
			Long curTime = System.currentTimeMillis()/1000;
			int pageSize = 500;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(table_type);
				List<Map<String, Object>> list = readService.getAll("select id,cid,comid,berthsec_id from " +
						" com_park_tb where is_delete=? and id>? and comid in (select id from com_info_tb" +
						" where groupid=? and state<>?) order by id limit ? ", 
						new Object[]{0, maxId, source_group_id, 1, pageSize});
				if(list == null || list.isEmpty()){
					logger.error("退出循环，数据已经复制完毕");
					return;
				}
				if(list.size() < pageSize){
					logger.error("最后一次循环>>>size:"+list.size());
					stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
				}
				List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
				Long lastId = maxId;//上一个的p4主键编号
				for(Map<String, Object> map : list){
					logger.error("map:"+map.toString());
					Long p4Id = (Long)map.get("id");
					String cid = (String)map.get("cid");
					Long comid = (Long)map.get("comid");
					Long berthsec_id = (Long)map.get("berthsec_id");
					if(p4Id == null
							|| p4Id <= 0){
						logger.error("返回信息错误>>>maxId:"+maxId);
						return;
					}
					//----------------------校验传过来的数据是否是正序排序------------------------//
					if(p4Id.intValue() <= lastId.intValue()){
						logger.error("停止循环，传过来的参数排序错误>>>p4Id:"+p4Id);
						return;
					}
					lastId = p4Id;
					//-----------------------------映射-----------------------------//
					comid = getMappingId(comid, 1);
					if(comid <= 0){
						logger.error("没有找到对应的车场");
						continue;
					}
					berthsec_id = getMappingId(berthsec_id, 2);
					if(berthsec_id <= 0){
						logger.error("没有找到对应的泊位段,应该是垃圾泊位");
						continue;
					}
					//-------------------------校验泊位是否已注册---------------------------//
					Long berthCount = writeService.getLong("select count(id) from com_park_tb where " +
							" cid=? and is_delete=? and comid=? ", new Object[]{cid, 0, comid});
					if(berthCount > 0){
						logger.error("停止循环，泊位编号重复");
						return;
					}
					//-------------------------注册泊位数据------------------------------//
					long berthId=writeService.getkey("seq_com_park_tb");
					//映射表
					Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
					carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
							" values (?,?,?,?)");
					carMapSqlMap.put("values", new Object[]{table_type, p4Id, berthId, groupId});
					bathSql.add(carMapSqlMap);
					//泊位段
					Map<String, Object> carSqlMap = new HashMap<String, Object>();
					carSqlMap.put("sql", "insert into com_park_tb(id,comid,cid,berthsec_id,create_time)" +
							" values(?,?,?,?,?)");
					carSqlMap.put("values", new Object[]{berthId, comid, cid, berthsec_id, curTime});
					bathSql.add(carSqlMap);
				}
				boolean b = writeService.bathUpdate2(bathSql);
				logger.error("结果>>>b:"+b+",maxId:"+maxId);
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void copyPark(){
		int table_type = 1;
		clearData(table_type);
		boolean stop = false;
		try {
			Long curTime = System.currentTimeMillis()/1000;
			int pageSize = 500;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(table_type);
				List<Map<String, Object>> list = readService.getAll("select id,company_name from com_info_tb" +
						" where groupid=? and state<>? and id>? order by id limit ? ", 
						new Object[]{source_group_id, 1, maxId, pageSize});
				if(list == null || list.isEmpty()){
					logger.error("退出循环，数据已经复制完毕");
					return;
				}
				if(list.size() < pageSize){
					logger.error("最后一次循环>>>size:"+list.size());
					stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
				}
				List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
				Long lastId = maxId;//上一个的p4主键编号
				for(Map<String, Object> map : list){
					logger.error("map:"+map.toString());
					Long p4Id = (Long)map.get("id");
					String company_name = (String)map.get("company_name");
					if(p4Id == null
							|| p4Id <= 0){
						logger.error("返回信息错误>>>maxId:"+maxId);
						return;
					}
					//----------------------校验传过来的数据是否是正序排序------------------------//
					if(p4Id.intValue() <= lastId.intValue()){
						logger.error("停止循环，传过来的参数排序错误>>>p4Id:"+p4Id);
						return;
					}
					lastId = p4Id;
					//-------------------------注册车场-----------------------------------//
					Long comId = writeService.getkey("seq_com_info_tb");
					//车牌映射表
					Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
					carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
							" values (?,?,?,?)");
					carMapSqlMap.put("values", new Object[]{table_type, p4Id, comId, groupId});
					bathSql.add(carMapSqlMap);
					//车牌
					Map<String, Object> carSqlMap = new HashMap<String, Object>();
					carSqlMap.put("sql", "insert into com_info_tb(id,company_name,create_time," +
							"groupid,etc,state) values(?,?,?,?,?,?)");
					carSqlMap.put("values", new Object[]{comId, company_name, curTime, groupId, etc, 0});
					bathSql.add(carSqlMap);
				}
				boolean b = writeService.bathUpdate2(bathSql);
				logger.error("结果>>>b:"+b+",maxId:"+maxId);
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private Long getMaxId(int table){
		Long maxId = -1L;
		try {
			//---------------------查询已导入的数据最后的一条--------------------------//
			Map<String, Object> map = writeService.getMap("select * from  " +
					" tmp_p4top5_primarykey where table_type=? and p4_id=" +
					" (select max(p4_id) from tmp_p4top5_primarykey where table_type=? and groupid=? )",
					new Object[]{table, table, groupId});
			if(map != null){
				maxId = (Long)map.get("p4_id");
			}
			logger.error("table:"+table+",maxId:"+maxId);
		} catch (Exception e) {
			logger.error(e);
		}
		return maxId;
	}
	
	private Long getMappingId(Long p4Id, int table_type){
		Long p5Id = -1L;
		try {
			if(p4Id != null && p4Id > 0){
				Map<String, Object> map = writeService.getMap("select p5_id from " +
						" tmp_p4top5_primarykey where p4_id=? and table_type=? and groupid=? ", 
						new Object[]{p4Id, table_type, groupId});
				if(map != null && !map.isEmpty()){
					p5Id = (Long)map.get("p5_id");
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return p5Id;
	}
	
	private void clearData(int table_type){
		try {
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			
			switch (table_type) {
			case 0://删除卡片和映射
				Map<String, Object> sqlMap = new HashMap<String, Object>();
				sqlMap.put("sql", "delete from com_nfc_tb where id in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				sqlMap.put("values", new Object[]{table_type, groupId});
				bathSql.add(sqlMap);
				
				Map<String, Object> cardAcountSqlMap = new HashMap<String, Object>();
				cardAcountSqlMap.put("sql", "delete from card_account_tb where card_id in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				cardAcountSqlMap.put("values", new Object[]{table_type, groupId});//卡片明细
				bathSql.add(cardAcountSqlMap);
				
				Map<String, Object> userSqlMap = new HashMap<String, Object>();
				userSqlMap.put("sql", "delete from user_info_tb where id in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				userSqlMap.put("values", new Object[]{13, groupId});//用户表(车主)
				bathSql.add(userSqlMap);
				
				Map<String, Object> carSqlMap = new HashMap<String, Object>();
				carSqlMap.put("sql", "delete from car_info_tb where id in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				carSqlMap.put("values", new Object[]{6, groupId});//车牌表
				bathSql.add(carSqlMap);
				
				break;
			case 1://删除车场和映射
				Map<String, Object> sqlMap1 = new HashMap<String, Object>();
				sqlMap1.put("sql", "delete from com_info_tb where id in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				sqlMap1.put("values", new Object[]{table_type, groupId});
				bathSql.add(sqlMap1);
				break;
			case 2://删除泊位和映射
				Map<String, Object> sqlMap2 = new HashMap<String, Object>();
				sqlMap2.put("sql", "delete from com_berthsecs_tb where id in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				sqlMap2.put("values", new Object[]{table_type, groupId});
				bathSql.add(sqlMap2);
				break;
			case 3://删除泊位和映射
				Map<String, Object> sqlMap3 = new HashMap<String, Object>();
				sqlMap3.put("sql", "delete from com_park_tb where id in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				sqlMap3.put("values", new Object[]{table_type, groupId});
				bathSql.add(sqlMap3);
				break;
			case 4://删除订单和映射
				Map<String, Object> sqlMap4 = new HashMap<String, Object>();
				sqlMap4.put("sql", "delete from order_tb where id in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				sqlMap4.put("values", new Object[]{table_type, groupId});
				bathSql.add(sqlMap4);
				
				Map<String, Object> pursueMap = new HashMap<String, Object>();
				pursueMap.put("sql", "delete from no_payment_tb where order_id in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				pursueMap.put("values", new Object[]{table_type, groupId});
				bathSql.add(pursueMap);
				
				Map<String, Object> cashAccountMap = new HashMap<String, Object>();
				cashAccountMap.put("sql", "delete from parkuser_cash_tb where orderid in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				cashAccountMap.put("values", new Object[]{table_type, groupId});
				bathSql.add(cashAccountMap);
				break;
			case 5://删除收费员和映射
				Map<String, Object> sqlMap5 = new HashMap<String, Object>();
				sqlMap5.put("sql", "delete from user_info_tb where id in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				sqlMap5.put("values", new Object[]{table_type, groupId});
				bathSql.add(sqlMap5);
				Map<String, Object> sqlMap13 = new HashMap<String, Object>();
				sqlMap13.put("sql", "delete from parkuser_work_record_tb where state=? and uid in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				sqlMap13.put("values", new Object[]{0, table_type, groupId});
				bathSql.add(sqlMap13);
				break;
			case 7://删除工作组和映射
				Map<String, Object> sqlMap7 = new HashMap<String, Object>();
				sqlMap7.put("sql", "delete from work_group_tb where id in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				sqlMap7.put("values", new Object[]{table_type, groupId});
				bathSql.add(sqlMap7);
				Map<String, Object> sqlMap14 = new HashMap<String, Object>();
				sqlMap14.put("sql", "delete from parkuser_work_record_tb where state=? and uid in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				sqlMap14.put("values", new Object[]{0, 5, groupId});
				bathSql.add(sqlMap14);
				break;
			case 8://删除工作组和泊位段对应和映射
				Map<String, Object> sqlMap8 = new HashMap<String, Object>();
				sqlMap8.put("sql", "delete from work_berthsec_tb where id in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				sqlMap8.put("values", new Object[]{table_type, groupId});
				bathSql.add(sqlMap8);
				break;
			case 9://删除工作组和收费员对应和映射
				Map<String, Object> sqlMap9 = new HashMap<String, Object>();
				sqlMap9.put("sql", "delete from work_employee_tb where id in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				sqlMap9.put("values", new Object[]{table_type, groupId});
				bathSql.add(sqlMap9);
				break;
			case 11://删除基站和映射
				Map<String, Object> sqlMap11 = new HashMap<String, Object>();
				sqlMap11.put("sql", "delete from sites_tb where id in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				sqlMap11.put("values", new Object[]{table_type, groupId});
				bathSql.add(sqlMap11);
				break;
			case 12://删除车检器对应和映射
				Map<String, Object> sqlMap12 = new HashMap<String, Object>();
				sqlMap12.put("sql", "delete from dici_tb where id in " +
						"(select p5_id from tmp_p4top5_primarykey where table_type=? and groupid=?)");
				sqlMap12.put("values", new Object[]{table_type, groupId});
				bathSql.add(sqlMap12);
				break;
			default:
				break;
			}
			Map<String, Object> tmpSqlMap = new HashMap<String, Object>();
			tmpSqlMap.put("sql", "delete from tmp_p4top5_primarykey where table_type=? and groupid=?");
			tmpSqlMap.put("values", new Object[]{table_type, groupId});
			bathSql.add(tmpSqlMap);
			boolean b = writeService.bathUpdate2(bathSql);
			logger.error("清除已导入的表数据>>>b:"+b+",table_type:"+table_type+",groupId:" + groupId);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void syncP4OrderByName(){
		int table_type = 4;
		clearData(4);
		boolean stop = false;
		try {
			int pageSize = 1000;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(table_type);
				List<Map<String, Object>> list = p4ReadService.getAll("select top "+pageSize+" id,carintime as create_time," +
						"ParkId as comid,carouttime as end_time,InOperaId as uid,platenumber as car_number," +
						"berthnumber as berthnumber,BerthsecId as berthsec_id,OutOperaId as out_uid," +
						"Arrearage as total from AbpBusinessDetail where tenantid=? and status=? and isdeleted=?" +
						" and id>? order by id ", 
						new Object[]{tenantid, 3, 0, maxId});
				if(list == null || list.isEmpty()){
					logger.error("退出循环，数据已经复制完毕");
					return;
				}
				if(list.size() < pageSize){
					logger.error("最后一次循环>>>size:"+list.size());
					stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
				}
				List<Object> idList = new ArrayList<Object>();
				List<Object> parkIdList = new ArrayList<Object>();
				for(Map<String, Object> map : list){
					Long uid = (Long)map.get("uid");
					Long out_uid = (Long)map.get("out_uid");
					Integer comid = (Integer)map.get("comid");
					if(!idList.contains(uid)){
						idList.add(uid);
					}
					if(!idList.contains(out_uid)){
						idList.add(out_uid);
					}
					if(!parkIdList.contains(comid)){
						parkIdList.add(comid);
					}
				}
				if(idList != null && !idList.isEmpty()){
					String param = "";
					for(Object object : idList){
						if("".equals(param)){
							param = "?";
						}else{
							param += ",?";
						}
					}
					List<Map<String, Object>> parkuserList = p4ReadService.getAllMap("select id,truename as nickname " +
							" from AbpEmployees where id in ("+param+")", idList);
					if(parkuserList != null && !parkuserList.isEmpty()){
						for(Map<String, Object> map : list){
							Long uid = (Long)map.get("uid");
							Long out_uid = (Long)map.get("out_uid");
							for(Map<String, Object> map2 : parkuserList){
								Long id = (Long)map2.get("id");
								String nickname = (String)map2.get("nickname");
								if(id.intValue() == uid.intValue()){
									map.put("in_nickname", nickname);
									break;
								}
							}
							for(Map<String, Object> map2 : parkuserList){
								Long id = (Long)map2.get("id");
								String nickname = (String)map2.get("nickname");
								if(id.intValue() == out_uid.intValue()){
									map.put("out_nickname", nickname);
									break;
								}
							}
						}
					}
				}
				if(parkIdList != null && !parkIdList.isEmpty()){
					String param = "";
					for(Object object : parkIdList){
						if("".equals(param)){
							param = "?";
						}else{
							param += ",?";
						}
					}
					List<Map<String, Object>> parkList = p4ReadService.getAllMap("select id,parkname as company_name " +
							" from AbpParks where id in ("+param+")", parkIdList);
					
					if(parkList != null && !parkList.isEmpty()){
						for(Map<String, Object> map : list){
							Integer comid = (Integer)map.get("comid");
							for(Map<String, Object> map2 : parkList){
								Integer id = (Integer)map2.get("id");
								String company_name = (String)map2.get("company_name");
								if(comid.intValue() == id.intValue()){
									map.put("company_name", company_name);
									break;
								}
							}
						}
					}
				}
				List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
				//------------------------------批量插入订单----------------------------//
				String addOrder = "insert into order_tb (id,create_time,comid,end_time,uid," +
						"car_number,berthnumber,berthsec_id,out_uid,total,uin,state,groupid," +
						"c_type) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				List<Object[]> orderValues = new ArrayList<Object[]>();
				
				Long lastId = maxId;//上一个的p4主键编号
				for(Map<String, Object> map : list){
					logger.error(map);
					Long p4Id = (Long)map.get("id");
					Timestamp c = (Timestamp)map.get("create_time");
					Long create_time = c.getTime()/1000;
					Timestamp e = (Timestamp)map.get("end_time");
					Long end_time = e.getTime()/1000;
					String company_name = null;
					if(map.get("company_name") != null){
						company_name = (String)map.get("company_name");
					}
					String berthnumber = null;
					if(map.get("berthnumber") != null){
						berthnumber = (String)map.get("berthnumber");
					}
					String in_nickname = null;
					if(map.get("in_nickname") != null){
						in_nickname = (String)map.get("in_nickname");
					}
					String out_nickname = null;
					if(map.get("out_nickname") != null){
						out_nickname = (String)map.get("out_nickname");
					}
					String car_number = (String)map.get("car_number");
					Double total = Double.valueOf(map.get("total") + "");
					if(p4Id == null
							|| p4Id <= 0
							|| car_number == null
							|| "".equals(car_number)
							|| total == null
							|| total < 0
							|| (company_name == null && berthnumber == null)){
						logger.error("返回信息错误>>>maxId:"+maxId);
						return;
					}
					//----------------------校验传过来的数据是否是正序排序------------------------//
					if(p4Id.intValue() <= lastId.intValue()){
						logger.error("停止循环，传过来的参数排序错误>>>p4Id:"+p4Id);
						continue;
					}
					lastId = p4Id;
					//-----------------------------映射-----------------------------//
					Berth berth = null;
					if(berthnumber != null){
						Long count = readService.getLong("select count(id) from com_park_tb where cid=? and" +
								" is_delete=? and comid in (select id from com_info_tb where groupid=?)", 
								new Object[]{berthnumber, 0, groupId});
						logger.error("count:"+count);
						if(count == 1){
							 berth = readService.getPOJO("select * from com_park_tb where cid=? and" +
									" is_delete=? and comid in (select id from com_info_tb where groupid=?)",
									new Object[]{berthnumber, 0, groupId}, Berth.class);
						}else if(count > 1 && company_name != null){
							berth = readService.getPOJO("select p.* from com_park_tb p,com_info_tb c where p.comid=c.id" +
									" and p.cid=? and p.is_delete=? and c.company_name = ? and c.groupid=? ",
									new Object[]{berthnumber, 0, company_name, groupId}, Berth.class);
						}
					}
					if(berth == null && company_name == null){
						logger.error("没有找到匹配车场,过滤掉berthnumber:"+berthnumber+",company_name:"+company_name);
						continue;
					}
					Map<String, Object> parkMap = null;
					if(berth == null && company_name != null){
						parkMap = readService.getMap("select id from com_info_tb" +
								" where company_name = ? and groupid=? ", new Object[]{company_name, groupId});
					}
					if(berth == null && parkMap == null){
						logger.error("没有找到匹配车场,过滤掉berthnumber:"+berthnumber+",company_name:"+company_name);
						continue;
					}
					Long comid = -1L;
					Long berthSegId = -1L;
					Long berthId = -1L;
					if(berth != null){
						berthId = berth.getId();
						comid = berth.getComid();
						berthSegId = berth.getBerthsec_id();
					}else if(parkMap != null){
						comid = (Long)parkMap.get("id");
					}
					logger.error("comid:"+comid);
					if(comid < 0){
						logger.error("停止循环，没有匹配的车场");
						return;
					}
					Long inUid = -1L;
					Long outUid = -1L;
					if(in_nickname != null && !"".equals(in_nickname)){
						Map<String, Object> userMap = readService.getMap("select id from user_info_tb where" +
								" nickname=? and groupid=? ", new Object[]{in_nickname, groupId});
						if(userMap != null){
							inUid = (Long)userMap.get("id");
						}
					}
					if(out_nickname != null && !"".equals(out_nickname)){
						Map<String, Object> userMap = readService.getMap("select id from user_info_tb where" +
								" nickname=? and groupid=? ", new Object[]{out_nickname, groupId});
						if(userMap != null){
							outUid = (Long)userMap.get("id");
						}
					}
					logger.error("inUid:"+inUid+",outUid:"+outUid);
					//-------------------------注册泊位数据------------------------------//
					long orderId = writeService.getkey("seq_order_tb");
					//映射表
					Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
					carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
							" values (?,?,?,?)");
					carMapSqlMap.put("values", new Object[]{4, p4Id, orderId, groupId});
					bathSql.add(carMapSqlMap);
					//逃单表
					Map<String, Object> pursueSqlMap = new HashMap<String, Object>();
					pursueSqlMap.put("sql", "insert into no_payment_tb (create_time,end_time," +
							"order_id,car_number,comid,total) values(?,?,?,?,?,?)");
					pursueSqlMap.put("values", new Object[]{create_time, end_time, orderId,
							car_number, comid, total});
					bathSql.add(pursueSqlMap);
					//--------------订单表----------------//
					Object[] va = new Object[14];
					va[0] = orderId;
					va[1] = create_time;
					va[2] = comid;
					va[3] = end_time;
					va[4] = inUid;
					va[5] = car_number;
					va[6] = berthId;
					va[7] = berthSegId;
					va[8] = outUid;
					va[9] = total;
					va[10] = -1;
					va[11] = 2;
					va[12] = groupId;
					va[13] = 2;
					orderValues.add(va);
				}
				boolean b = writeService.bathUpdate2(bathSql);
				logger.error("结果>>>b:"+b+",maxId:"+maxId);
				int order = writeService.bathInsert(addOrder, orderValues, new int []{4,4,4,4,4,12,4,4,4,3,4,4,4,4});
				logger.error("批量插入结果>>>orderMap:"+order+",maxId:"+maxId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	private void syncP3OrderByName(){
		//clearData(4);
		boolean stop = false;
		try {
			int pageSize = 1000;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(4);
				String url = "http://219.159.88.140:8091/Api/Sales/values/GetP3EscapeList?id="+maxId+"&pagesize="+pageSize+"&groupid=3";
				String result = new HttpProxy().doGet(url);
				if(result != null && !"".equals(result)){
					result = result.replace("\\","");
					result = result.substring(1, result.length() - 1);
					JSONArray array = JSONArray.fromObject(result);
					//----------------------校验是否结束循环------------------------//
					if(array.size() == 0){
						logger.error("最后一次循环获取的数据结果为空>>>size:"+array.size());	
						return;
					}
					if(array.size() < pageSize){
						logger.error("最后一次循环>>>size:"+array.size());
						stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
					}
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					//------------------------------批量插入订单----------------------------//
					String addOrder = "insert into order_tb (id,create_time,comid,end_time,uid," +
							"car_number,berthnumber,berthsec_id,out_uid,total,uin,state,groupid," +
							"c_type) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					List<Object[]> orderValues = new ArrayList<Object[]>();
					
					Long lastId = maxId;//上一个的p4主键编号
					for(int i = 0; i< array.size(); i++){
						JSONObject object = array.getJSONObject(i);
						logger.error("object:"+object.toString());
						Long p4Id = object.getLong("id");
						Long create_time = TimeTools.getLongMilliSecondFromStrDate(object.getString("create_time"),"MM/dd/yyyy HH:mm:ss")/1000;
						Long end_time = TimeTools.getLongMilliSecondFromStrDate(object.getString("end_time"),"MM/dd/yyyy HH:mm:ss")/1000;
						if(object.getString("create_time").indexOf("PM") > -1 
								&& object.getString("create_time").indexOf(" 12:") == -1){
							create_time += 12 * 60 * 60;
						}
						if(object.getString("end_time").indexOf("PM") > -1
								&& object.getString("end_time").indexOf(" 12:") == -1){
							end_time += 12 * 60 * 60;				
						}
						String parkname = object.getString("parkname");
						String berthnumber = object.getString("berthnumber");
						String out_uid = object.getString("out_uid");
						String uid = object.getString("uid");
						String car_number = object.getString("car_number");
						Double total = object.getDouble("total");
						if(p4Id == null
								|| p4Id <= 0
								|| car_number == null
								|| "".equals(car_number)
								|| total == null
								|| total <= 0
								|| parkname == null
								|| "".equals(parkname)
								|| berthnumber == null
								|| "".equals(berthnumber)){
							logger.error("返回信息错误>>>maxId:"+maxId);
							return;
						}
						//----------------------校验传过来的数据是否是正序排序------------------------//
						if(p4Id.intValue() <= lastId.intValue()){
							logger.error("停止循环，传过来的参数排序错误>>>p4Id:"+p4Id);
							continue;
						}
						lastId = p4Id;
						//-----------------------------映射-----------------------------//
						Berth berth = null;
						Map<String, Object> parkMap = null;
						Long count = readService.getLong("select count(id) from com_park_tb where cid=? and" +
								" is_delete=? and comid in (select id from com_info_tb where groupid=?)", 
								new Object[]{berthnumber, 0, groupId});
						if(count == 1){
							 berth = readService.getPOJO("select * from com_park_tb where cid=? and" +
									" is_delete=? and comid in (select id from com_info_tb where groupid=?)",
									new Object[]{berthnumber, 0, groupId}, Berth.class);
						}else{
							parkMap = readService.getMap("select id from com_info_tb" +
									" where company_name = ? and groupid=? ", new Object[]{parkname, groupId});
						}
						if(berth == null && parkMap == null){
							logger.error("停止循环，既没有泊位匹配也没有车场匹配>>>berthnumber:"+berthnumber+",parkname:"+parkname);
							continue;
						}
						Long comid = -1L;
						Long berthSegId = -1L;
						Long berthId = -1L;
						if(berth != null){
							berthId = berth.getId();
							comid = berth.getComid();
							berthSegId = berth.getBerthsec_id();
						}else if(parkMap != null){
							comid = (Long)parkMap.get("id");
						}
						logger.error("comid:"+comid);
						if(comid < 0){
							logger.error("停止循环，没有匹配的车场");
							return;
						}
						Long inUid = -1L;
						Long outUid = -1L;
						if(uid != null && !"".equals(uid)){
							Map<String, Object> userMap = readService.getMap("select id from user_info_tb where" +
									" nickname=? and groupid=? ", new Object[]{uid, groupId});
							if(userMap != null){
								inUid = (Long)userMap.get("id");
								outUid = (Long)userMap.get("id");
							}
						}
						logger.error("inUid:"+inUid+",outUid:"+outUid);
						//-------------------------注册泊位数据------------------------------//
						long orderId = writeService.getkey("seq_order_tb");
						//映射表
						Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
						carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
								" values (?,?,?,?)");
						carMapSqlMap.put("values", new Object[]{4, p4Id, orderId, groupId});
						bathSql.add(carMapSqlMap);
						//逃单表
						Map<String, Object> pursueSqlMap = new HashMap<String, Object>();
						pursueSqlMap.put("sql", "insert into no_payment_tb (create_time,end_time," +
								"order_id,car_number,comid,total) values(?,?,?,?,?,?)");
						pursueSqlMap.put("values", new Object[]{create_time, end_time, orderId,
								car_number, comid, total});
						bathSql.add(pursueSqlMap);
						//--------------订单表----------------//
						Object[] va = new Object[14];
						va[0] = orderId;
						va[1] = create_time;
						va[2] = comid;
						va[3] = end_time;
						va[4] = inUid;
						va[5] = car_number;
						va[6] = berthId;
						va[7] = berthSegId;
						va[8] = outUid;
						va[9] = total;
						va[10] = -1;
						va[11] = 2;
						va[12] = groupId;
						va[13] = 2;
						orderValues.add(va);
					}
					boolean b = writeService.bathUpdate2(bathSql);
					logger.error("结果>>>b:"+b+",maxId:"+maxId);
					int order = writeService.bathInsert(addOrder, orderValues, new int []{4,4,4,4,4,12,4,4,4,3,4,4,4,4});
					logger.error("批量插入结果>>>orderMap:"+order+",maxId:"+maxId);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void syncOrder(){
		int table_type = 4;
		clearData(table_type);
		boolean stop = false;
		try {
			int pageSize = 500;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(table_type);
				String url = "http://www.bouwa.net:8090/Api/Sales/values/GetEscapeList?id="+maxId+"&pagesize="+pageSize+"&str="+condition;
				String result = new HttpProxy().doGet(url);
				if(result != null && !"".equals(result)){
					result = result.replace("\\","");
					result = result.substring(1, result.length() - 1);
					JSONArray array = JSONArray.fromObject(result);
					//----------------------校验是否结束循环------------------------//
					if(array.size() == 0){
						logger.error("最后一次循环获取的数据结果为空>>>size:"+array.size());	
						return;
					}
					if(array.size() < pageSize){
						logger.error("最后一次循环>>>size:"+array.size());
						stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
					}
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					//------------------------------批量插入订单----------------------------//
					String addOrder = "insert into order_tb (id,create_time,comid,end_time,uid," +
							"car_number,berthnumber,berthsec_id,out_uid,total,uin,state,groupid," +
							"c_type) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					List<Object[]> orderValues = new ArrayList<Object[]>();
					
					Long lastId = maxId;//上一个的p4主键编号
					for(int i = 0; i< array.size(); i++){
						JSONObject object = array.getJSONObject(i);
						logger.error("object:"+object.toString());
						Long p4Id = object.getLong("id");
						Long create_time = TimeTools.getLongMilliSecondFromStrDate(object.getString("create_time"),"yyyy/MM/dd HH:mm:ss")/1000;
						Long end_time = TimeTools.getLongMilliSecondFromStrDate(object.getString("end_time"),"yyyy/MM/dd HH:mm:ss")/1000;
						Long comid = object.getLong("comid");
						Long berthnumber = object.getLong("berthnumber");
						Long berthsec_id = object.getLong("berthsec_id");
						Long out_uid = object.getLong("out_uid");
						Long uid = object.getLong("uid");
						String car_number = object.getString("car_number");
						Double total = object.getDouble("total");
						
						if(p4Id == null
								|| p4Id <= 0
								|| comid == null
								|| comid <= 0
								|| car_number == null
								|| "".equals(car_number)
								|| total == null
								|| total < 0){
							logger.error("返回信息错误>>>maxId:"+maxId);
							return;
						}
						//----------------------校验传过来的数据是否是正序排序------------------------//
						if(p4Id.intValue() <= lastId.intValue()){
							logger.error("传过来的参数排序错误>>>p4Id:"+p4Id);
							continue;
						}
						lastId = p4Id;
						//-----------------------------映射-----------------------------//
						comid = getMappingId(comid, 1);
						if(comid <= 0){
							logger.error("跳过，没有找到对应的车场");
							continue;
						}
						berthnumber = getMappingId(berthnumber, 3);
						if(berthnumber <= 0){
							logger.error("没有找到对应的泊位");
							//continue;
						}
						berthsec_id = getMappingId(berthsec_id, 2);
						if(berthsec_id <= 0){
							logger.error("没有找到对应的泊位段");
							//return;
						}
						uid = getMappingId(uid, 5);
						if(uid <= 0){
							logger.error("没有找到对应的进场收费员");
							//return;
						}
						out_uid = getMappingId(out_uid, 5);
						if(out_uid <= 0){
							logger.error("没有找到对应的出场收费员");
							//return;
						}
						//-------------------------注册泊位数据------------------------------//
						long orderId = writeService.getkey("seq_order_tb");
						//映射表
						Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
						carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
								" values (?,?,?,?)");
						carMapSqlMap.put("values", new Object[]{table_type, p4Id, orderId, groupId});
						bathSql.add(carMapSqlMap);
						//逃单表
						Map<String, Object> pursueSqlMap = new HashMap<String, Object>();
						pursueSqlMap.put("sql", "insert into no_payment_tb (create_time,end_time," +
								"order_id,car_number,comid,total) values(?,?,?,?,?,?)");
						pursueSqlMap.put("values", new Object[]{create_time, end_time, orderId,
								car_number, comid, total});
						bathSql.add(pursueSqlMap);
						//--------------订单表----------------//
						Object[] va = new Object[14];
						va[0] = orderId;
						va[1] = create_time;
						va[2] = comid;
						va[3] = end_time;
						va[4] = uid;
						va[5] = car_number;
						va[6] = berthnumber;
						va[7] = berthsec_id;
						va[8] = out_uid;
						va[9] = total;
						va[10] = -1;
						va[11] = 2;
						va[12] = groupId;
						va[13] = 2;
						orderValues.add(va);
					}
					boolean b = writeService.bathUpdate2(bathSql);
					logger.error("结果>>>b:"+b+",maxId:"+maxId);
					int order = writeService.bathInsert(addOrder, orderValues, new int []{4,4,4,4,4,12,4,4,4,3,4,4,4,4});
					logger.error("批量插入结果>>>orderMap:"+order+",maxId:"+maxId);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void syncSensor(){
		int table_type = 12;
		clearData(table_type);
		boolean stop = false;
		try {
			int pageSize = 100;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(table_type);
				String url = "http://www.bouwa.net:8090/Api/Sales/values/GetSensorList?id="+maxId+"&pagesize="+pageSize+"&str="+condition;
				String result = new HttpProxy().doGet(url);
				if(result != null && !"".equals(result)){
					result = result.replace("\\","");
					result = result.substring(1, result.length() - 1);
					JSONArray array = JSONArray.fromObject(result);
					//----------------------校验是否结束循环------------------------//
					if(array.size() == 0){
						logger.error("最后一次循环获取的数据结果为空>>>size:"+array.size());	
						return;
					}
					if(array.size() < pageSize){
						logger.error("最后一次循环>>>size:"+array.size());
						stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
					}
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					Long lastId = maxId;//上一个的p4主键编号
					for(int i = 0; i< array.size(); i++){
						JSONObject object = array.getJSONObject(i);
						logger.error("object:"+object.toString());
						Long p4Id = object.getLong("id");
						String did = object.getString("did");
						Long comid = object.getLong("comid");
						Double magnetism = 0d;
						if(!"".equals(object.getString("magnetism"))){
							magnetism = object.getDouble("magnetism");
						}
						Double battery = 0d;
						if(!"".equals(object.getString("battery"))){
							battery = object.getDouble("battery");
						}
						if(p4Id == null
								|| p4Id <= 0
								|| comid == null
								|| comid <= 0
								|| did == null
								|| "".equals(did)){
							logger.error("返回信息错误>>>maxId:"+maxId);
							return;
						}
						//----------------------校验传过来的数据是否是正序排序------------------------//
						if(p4Id.intValue() <= lastId.intValue()){
							logger.error("传过来的参数排序错误>>>p4Id:"+p4Id);
							continue;
						}
						lastId = p4Id;
						//-----------------------------映射-----------------------------//
						comid = getMappingId(comid, 1);
						if(comid <= 0){
							logger.error("没有找到对应的车场");
							continue;
						}
						//-------------------------校验泊位是否已注册---------------------------//
						Long count = writeService.getLong("select count(id) from dici_tb where " +
								" did=? and comid=? ", new Object[]{did, comid});
						if(count > 0){
							logger.error("停止循环，车检器编号重复");
							return;
						}
						//-------------------------注册泊位数据------------------------------//
						long sensorId = writeService.getkey("seq_dici_tb");
						//映射表
						Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
						carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
								" values (?,?,?,?)");
						carMapSqlMap.put("values", new Object[]{table_type, p4Id, sensorId, groupId});
						bathSql.add(carMapSqlMap);
						//泊位段
						Map<String, Object> carSqlMap = new HashMap<String, Object>();
						carSqlMap.put("sql", "insert into dici_tb (id,did,magnetism,battery,comid) " +
								"values(?,?,?,?,?)");
						carSqlMap.put("values", new Object[]{sensorId, did, magnetism, battery, comid});
						bathSql.add(carSqlMap);
					}
					boolean b = writeService.bathUpdate2(bathSql);
					logger.error("结果>>>b:"+b+",maxId:"+maxId);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void syncSite(){
		int table_type = 11;
		clearData(table_type);
		boolean stop = false;
		try {
			int pageSize = 100;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(table_type);
				String url = "http://www.bouwa.net:8090/Api/Sales/values/GetTransmitterList?id="+maxId+"&pagesize="+pageSize+"&str="+condition;
				String result = new HttpProxy().doGet(url);
				if(result != null && !"".equals(result)){
					result = result.replace("\\","");
					result = result.substring(1, result.length() - 1);
					JSONArray array = JSONArray.fromObject(result);
					//----------------------校验是否结束循环------------------------//
					if(array.size() == 0){
						logger.error("最后一次循环获取的数据结果为空>>>size:"+array.size());	
						return;
					}
					if(array.size() < pageSize){
						logger.error("最后一次循环>>>size:"+array.size());
						stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
					}
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					Long lastId = maxId;//上一个的p4主键编号
					for(int i = 0; i< array.size(); i++){
						JSONObject object = array.getJSONObject(i);
						logger.error("object:"+object.toString());
						Long p4Id = object.getLong("id");
						Long create_time = TimeTools.getLongMilliSecondFromStrDate(object.getString("create_time"),"yyyy/MM/dd HH:mm:ss")/1000;
						Double voltage = 0d;
						if(!"".equals(object.getString("voltage"))){
							voltage = object.getDouble("voltage");
						}
						String uuid = object.getString("uuid");
						if(p4Id == null
								|| p4Id <= 0
								|| uuid == null
								|| "".equals(uuid)){
							logger.error("返回信息错误>>>maxId:"+maxId);
							return;
						}
						//----------------------校验传过来的数据是否是正序排序------------------------//
						if(p4Id.intValue() <= lastId.intValue()){
							logger.error("停止循环，传过来的参数排序错误>>>p4Id:"+p4Id);
							return;
						}
						lastId = p4Id;
						//-------------------------校验基站唯一编号---------------------------//
						Long count = writeService.getLong("select count(id) from sites_tb" +
								" where is_delete=? and uuid=? and groupid=? ", 
								new Object[]{0, uuid, groupId});
						if(count > 0){
							logger.error("停止循环，基站编号重复");
							return;
						}
						//-------------------------注册泊位段数据------------------------------//
						long siteId=writeService.getkey("seq_sites_tb");
						//映射表
						Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
						carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
								" values (?,?,?,?)");
						carMapSqlMap.put("values", new Object[]{table_type, p4Id, siteId, groupId});
						bathSql.add(carMapSqlMap);
						//泊位段
						Map<String, Object> carSqlMap = new HashMap<String, Object>();
						carSqlMap.put("sql", "insert into sites_tb(id,uuid,voltage,create_time," +
								"groupid) values(?,?,?,?,?)");
						carSqlMap.put("values", new Object[]{siteId, uuid, voltage, create_time, groupId});
						bathSql.add(carSqlMap);
					}
					boolean b = writeService.bathUpdate2(bathSql);
					logger.error("结果>>>b:"+b+",maxId:"+maxId);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void syncWorkGroupPuser(){
		int table_type = 9;
		clearData(table_type);
		boolean stop = false;
		try {
			int pageSize = 100;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(table_type);
				String url = "http://www.bouwa.net:8090/Api/Sales/values/GetWorkGroupEmployeeList?id="+maxId+"&pagesize="+pageSize+"&str="+condition;
				String result = new HttpProxy().doGet(url);
				if(result != null && !"".equals(result)){
					result = result.replace("\\","");
					result = result.substring(1, result.length() - 1);
					JSONArray array = JSONArray.fromObject(result);
					//----------------------校验是否结束循环------------------------//
					if(array.size() == 0){
						logger.error("最后一次循环获取的数据结果为空>>>size:"+array.size());	
						return;
					}
					if(array.size() < pageSize){
						logger.error("最后一次循环>>>size:"+array.size());
						stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
					}
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					Long lastId = maxId;//上一个的p4主键编号
					for(int i = 0; i< array.size(); i++){
						JSONObject object = array.getJSONObject(i);
						logger.error("object:"+object.toString());
						Long p4Id = object.getLong("id");
						Long work_group_id = object.getLong("work_group_id");
						Long employee_id = object.getLong("employee_id");
						if(p4Id == null
								|| p4Id <= 0
								|| work_group_id == null
								|| work_group_id <= 0
								|| employee_id == null
								|| employee_id <= 0){
							logger.error("返回信息错误>>>maxId:"+maxId);
							return;
						}
						//----------------------校验传过来的数据是否是正序排序------------------------//
						if(p4Id.intValue() <= lastId.intValue()){
							logger.error("停止循环，传过来的参数排序错误>>>p4Id:"+p4Id);
							return;
						}
						lastId = p4Id;
						//-----------------------------映射-----------------------------//
						work_group_id = getMappingId(work_group_id, 7);
						if(work_group_id <= 0){
							logger.error("停止循环，没有找到对应的工作组");
							return;
						}
						employee_id = getMappingId(employee_id, 5);
						if(employee_id <= 0){
							logger.error("停止循环，没有找到对应的收费员");
							return;
						}
						//-------------------------注册工作组泊位段数据------------------------------//
						long workPuserId=writeService.getkey("seq_work_employee_tb");
						//映射表
						Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
						carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
								" values (?,?,?,?)");
						carMapSqlMap.put("values", new Object[]{table_type, p4Id, workPuserId, groupId});
						bathSql.add(carMapSqlMap);
						//泊位段
						Map<String, Object> carSqlMap = new HashMap<String, Object>();
						carSqlMap.put("sql", "insert into work_employee_tb(id,work_group_id," +
								"employee_id,state) values(?,?,?,?)");
						carSqlMap.put("values", new Object[]{workPuserId, work_group_id, employee_id, 0});
						bathSql.add(carSqlMap);
					}
					boolean b = writeService.bathUpdate2(bathSql);
					logger.error("结果>>>b:"+b+",maxId:"+maxId);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void syncWorkGroupBerthSeg(){
		int table_type = 8;
		clearData(table_type);
		boolean stop = false;
		try {
			int pageSize = 100;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(table_type);
				String url = "http://www.bouwa.net:8090/Api/Sales/values/GetWorkGroupBerthsecList?id="+maxId+"&pagesize="+pageSize+"&str="+condition;
				String result = new HttpProxy().doGet(url);
				if(result != null && !"".equals(result)){
					result = result.replace("\\","");
					result = result.substring(1, result.length() - 1);
					JSONArray array = JSONArray.fromObject(result);
					//----------------------校验是否结束循环------------------------//
					if(array.size() == 0){
						logger.error("最后一次循环获取的数据结果为空>>>size:"+array.size());	
						return;
					}
					if(array.size() < pageSize){
						logger.error("最后一次循环>>>size:"+array.size());
						stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
					}
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					Long lastId = maxId;//上一个的p4主键编号
					for(int i = 0; i< array.size(); i++){
						JSONObject object = array.getJSONObject(i);
						logger.error("object:"+object.toString());
						Long p4Id = object.getLong("id");
						Long work_group_id = object.getLong("work_group_id");
						Long berthsec_id = object.getLong("berthsec_id");
						if(p4Id == null
								|| p4Id <= 0
								|| work_group_id == null
								|| work_group_id <= 0
								|| berthsec_id == null
								|| berthsec_id <= 0){
							logger.error("返回信息错误>>>maxId:"+maxId);
							return;
						}
						//----------------------校验传过来的数据是否是正序排序------------------------//
						if(p4Id.intValue() <= lastId.intValue()){
							logger.error("停止循环，传过来的参数排序错误>>>p4Id:"+p4Id);
							return;
						}
						lastId = p4Id;
						//-----------------------------映射-----------------------------//
						work_group_id = getMappingId(work_group_id, 7);
						if(work_group_id <= 0){
							logger.error("没有找到对应的工作组");
							continue;
						}
						berthsec_id = getMappingId(berthsec_id, 2);
						if(berthsec_id <= 0){
							logger.error("没有找到对应的泊位段");
							continue;
						}
						//-------------------------注册工作组泊位段数据------------------------------//
						long workBerthSegId=writeService.getkey("seq_work_berthsec_tb");
						//映射表
						Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
						carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
								" values (?,?,?,?)");
						carMapSqlMap.put("values", new Object[]{table_type, p4Id, workBerthSegId, groupId});
						bathSql.add(carMapSqlMap);
						//泊位段
						Map<String, Object> carSqlMap = new HashMap<String, Object>();
						carSqlMap.put("sql", "insert into work_berthsec_tb(id,work_group_id," +
								"berthsec_id,state) values(?,?,?,?)");
						carSqlMap.put("values", new Object[]{workBerthSegId, work_group_id, berthsec_id, 0});
						bathSql.add(carSqlMap);
					}
					boolean b = writeService.bathUpdate2(bathSql);
					logger.error("结果>>>b:"+b+",maxId:"+maxId);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void syncWorkgroup(){
		int table_type = 7;
		clearData(table_type);
		boolean stop = false;
		try {
			int pageSize = 100;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(table_type);
				String url = "http://www.bouwa.net:8090/Api/Sales/values/GetWorkGroupList?id="+maxId+"&pagesize="+pageSize+"&str="+condition;
				String result = new HttpProxy().doGet(url);
				if(result != null && !"".equals(result)){
					result = result.replace("\\","");
					result = result.substring(1, result.length() - 1);
					JSONArray array = JSONArray.fromObject(result);
					//----------------------校验是否结束循环------------------------//
					if(array.size() == 0){
						logger.error("最后一次循环获取的数据结果为空>>>size:"+array.size());	
						return;
					}
					if(array.size() < pageSize){
						logger.error("最后一次循环>>>size:"+array.size());
						stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
					}
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					Long lastId = maxId;//上一个的p4主键编号
					for(int i = 0; i< array.size(); i++){
						JSONObject object = array.getJSONObject(i);
						logger.error("object:"+object.toString());
						Long p4Id = object.getLong("id");
						String workgroup_name = object.getString("workgroup_name");
						Long create_time = TimeTools.getLongMilliSecondFromStrDate(object.getString("create_time"),"yyyy/MM/dd HH:mm:ss")/1000;
						if(p4Id == null
								|| p4Id <= 0){
							logger.error("返回信息错误>>>maxId:"+maxId);
							return;
						}
						//----------------------校验传过来的数据是否是正序排序------------------------//
						if(p4Id.intValue() <= lastId.intValue()){
							logger.error("停止循环，传过来的参数排序错误>>>p4Id:"+p4Id);
							return;
						}
						lastId = p4Id;
						//-------------------------注册工作组数据------------------------------//
						long workGroupId=writeService.getkey("seq_work_group_tb");
						//映射表
						Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
						carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
								" values (?,?,?,?)");
						carMapSqlMap.put("values", new Object[]{table_type, p4Id, workGroupId, groupId});
						bathSql.add(carMapSqlMap);
						//泊位段
						Map<String, Object> carSqlMap = new HashMap<String, Object>();
						carSqlMap.put("sql", "insert into work_group_tb(id,workgroup_name," +
								"create_time,is_active,company_id) values(?,?,?,?,?)");
						carSqlMap.put("values", new Object[]{workGroupId, workgroup_name,
								create_time, 0, groupId});
						bathSql.add(carSqlMap);
					}
					boolean b = writeService.bathUpdate2(bathSql);
					logger.error("结果>>>b:"+b+",maxId:"+maxId);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void syncBerth(){
		int table_type = 3;
		clearData(table_type);
		boolean stop = false;
		try {
			int pageSize = 500;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(table_type);
				String url = "http://www.bouwa.net:8090/Api/Sales/values/GetBerthsList?id="+maxId+"&pagesize="+pageSize+"&str="+condition;
				String result = new HttpProxy().doGet(url);
				if(result != null && !"".equals(result)){
					result = result.replace("\\","");
					result = result.substring(1, result.length() - 1);
					JSONArray array = JSONArray.fromObject(result);
					//----------------------校验是否结束循环------------------------//
					if(array.size() == 0){
						logger.error("最后一次循环获取的数据结果为空>>>size:"+array.size());	
						return;
					}
					if(array.size() < pageSize){
						logger.error("最后一次循环>>>size:"+array.size());
						stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
					}
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					Long lastId = maxId;//上一个的p4主键编号
					for(int i = 0; i< array.size(); i++){
						JSONObject object = array.getJSONObject(i);
						logger.error("object:"+object.toString());
						Long p4Id = object.getLong("id");
						String cid = object.getString("cid");
						Long create_time = TimeTools.getLongMilliSecondFromStrDate(object.getString("create_time"),"yyyy/MM/dd HH:mm:ss")/1000;
						Long comid = object.getLong("comid");
						Long berthsec_id = object.getLong("berthsec_id");
						Long dici_id = -1L;
						if(!"".equals(object.getString("dici_id"))){
							dici_id = object.getLong("dici_id");
						}
						if(p4Id == null
								|| p4Id <= 0
								|| comid == null
								|| comid <= 0
								|| berthsec_id == null
								|| berthsec_id <= 0){
							logger.error("返回信息错误>>>maxId:"+maxId);
							continue;
						}
						//----------------------校验传过来的数据是否是正序排序------------------------//
						if(p4Id.intValue() <= lastId.intValue()){
							logger.error("停止循环，传过来的参数排序错误>>>p4Id:"+p4Id);
							return;
						}
						lastId = p4Id;
						//-----------------------------映射-----------------------------//
						comid = getMappingId(comid, 1);
						if(comid <= 0){
							logger.error("没有找到对应的车场");
							continue;
						}
						berthsec_id = getMappingId(berthsec_id, 2);
						if(berthsec_id <= 0){
							logger.error("没有找到对应的泊位段,应该是垃圾泊位");
							continue;
						}
						dici_id = getMappingId(dici_id, 12);
						logger.error("绑定的车检器编号>>>dici_id:"+dici_id);
						//-------------------------校验泊位是否已注册---------------------------//
						Long berthCount = writeService.getLong("select count(id) from com_park_tb where " +
								" cid=? and is_delete=? and comid=? ", new Object[]{cid, 0, comid});
						if(berthCount > 0){
							logger.error("停止循环，泊位编号重复");
							return;
						}
						//-------------------------注册泊位数据------------------------------//
						long berthId=writeService.getkey("seq_com_park_tb");
						//映射表
						Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
						carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
								" values (?,?,?,?)");
						carMapSqlMap.put("values", new Object[]{table_type, p4Id, berthId, groupId});
						bathSql.add(carMapSqlMap);
						//泊位段
						Map<String, Object> carSqlMap = new HashMap<String, Object>();
						carSqlMap.put("sql", "insert into com_park_tb(id,comid,cid,berthsec_id,create_time,dici_id)" +
								" values(?,?,?,?,?,?)");
						carSqlMap.put("values", new Object[]{berthId, comid, cid, berthsec_id, create_time, dici_id});
						bathSql.add(carSqlMap);
					}
					boolean b = writeService.bathUpdate2(bathSql);
					logger.error("结果>>>b:"+b+",maxId:"+maxId);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void syncBerthSeg(){
		int table_type = 2;
		clearData(table_type);
		boolean stop = false;
		try {
			int pageSize = 100;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(table_type);
				String url = "http://www.bouwa.net:8090/Api/Sales/values/GetBerthsecsList?id="+maxId+"&pagesize="+pageSize+"&str="+condition;
				String result = new HttpProxy().doGet(url);
				if(result != null && !"".equals(result)){
					result = result.replace("\\","");
					result = result.substring(1, result.length() - 1);
					JSONArray array = JSONArray.fromObject(result);
					//----------------------校验是否结束循环------------------------//
					if(array.size() == 0){
						logger.error("最后一次循环获取的数据结果为空>>>size:"+array.size());	
						return;
					}
					if(array.size() < pageSize){
						logger.error("最后一次循环>>>size:"+array.size());
						stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
					}
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					Long lastId = maxId;//上一个的p4主键编号
					for(int i = 0; i< array.size(); i++){
						JSONObject object = array.getJSONObject(i);
						logger.error("object:"+object.toString());
						Long p4Id = object.getLong("id");
						String berthsec_name = object.getString("berthsec_name");
						Long create_time = TimeTools.getLongMilliSecondFromStrDate(object.getString("create_time"),"yyyy/MM/dd HH:mm:ss")/1000;
						Long comid = object.getLong("comid");
						if(p4Id == null
								|| p4Id <= 0
								|| comid == null
								|| comid <= 0){
							logger.error("返回信息错误>>>maxId:"+maxId);
							return;
						}
						//----------------------校验传过来的数据是否是正序排序------------------------//
						if(p4Id.intValue() <= lastId.intValue()){
							logger.error("停止循环，传过来的参数排序错误>>>p4Id:"+p4Id);
							return;
						}
						lastId = p4Id;
						//-----------------------------车场映射-----------------------------//
						comid = getMappingId(comid, 1);
						if(comid <= 0){
							logger.error("停止循环，没有找到对应的车场");
							return;
						}
						//-------------------------注册泊位段数据------------------------------//
						long berthSegId=writeService.getkey("seq_com_berthsecs_tb");
						//映射表
						Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
						carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
								" values (?,?,?,?)");
						carMapSqlMap.put("values", new Object[]{table_type, p4Id, berthSegId, groupId});
						bathSql.add(carMapSqlMap);
						//泊位段
						Map<String, Object> carSqlMap = new HashMap<String, Object>();
						carSqlMap.put("sql", "insert into com_berthsecs_tb(id,berthsec_name,create_time,comid)" +
								" values(?,?,?,?)");
						carSqlMap.put("values", new Object[]{berthSegId, berthsec_name, create_time, comid});
						bathSql.add(carSqlMap);
					}
					boolean b = writeService.bathUpdate2(bathSql);
					logger.error("结果>>>b:"+b+",maxId:"+maxId);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void syncParkUser(){
		int table_type = 5;
		clearData(table_type);
		boolean stop = false;
		try {
			int pageSize = 100;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(table_type);
				String url = "http://www.bouwa.net:8090/Api/Sales/values/GetEmployeeList?id="+maxId+"&pagesize="+pageSize+"&str="+condition;
				String result = new HttpProxy().doGet(url);
				if(result != null && !"".equals(result)){
					result = result.replace("\\","");
					result = result.substring(1, result.length() - 1);
					JSONArray array = JSONArray.fromObject(result);
					//----------------------校验是否结束循环------------------------//
					if(array.size() == 0){
						logger.error("最后一次循环获取的数据结果为空>>>size:"+array.size());	
						return;
					}
					if(array.size() < pageSize){
						logger.error("最后一次循环>>>size:"+array.size());
						stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
					}
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					Long lastId = maxId;//上一个的p4主键编号
					for(int i = 0; i< array.size(); i++){
						JSONObject object = array.getJSONObject(i);
						logger.error("object:"+object.toString());
						Long p4Id = object.getLong("id");
						String nickname = object.getString("nickname");
						Long reg_time = TimeTools.getLongMilliSecondFromStrDate(object.getString("reg_time"),"yyyy/MM/dd HH:mm:ss")/1000;
						String mobile = object.getString("mobile");
						Long createor_id = object.getLong("createor_id");
						Long comid = object.getLong("comid");
						if(p4Id == null
								|| p4Id <= 0){
							logger.error("返回信息错误>>>maxId:"+maxId);
							return;
						}
						//----------------------校验传过来的数据是否是正序排序------------------------//
						if(p4Id.intValue() <= lastId.intValue()){
							logger.error("停止循环，传过来的参数排序错误>>>p4Id:"+p4Id);
							return;
						}
						lastId = p4Id;
						//-------------------------注册车场-----------------------------------//
						//createor_id = getMappingId(createor_id, 5);
						comid = getMappingId(comid, 1);
						logger.error("p5_createor_id:"+createor_id+",comid:"+comid);
						//-------------------------检查手机号-----------------------------------//
						if(mobile != null && !"".equals(mobile)){
							if(StringUtils.checkMobile(mobile)){
								logger.error("手机号合法>>>mobile:"+mobile);
								Long ucount = writeService.getLong("select count(id) from user_info_tb" +
										" where mobile=? and auth_flag=? ", new Object[]{mobile, 2});
								if(ucount > 0){
									logger.error("该手机号已注册收费员>>>"+mobile);
									mobile = null;
								}
							}
						}
						//-------------------------注册收费员-----------------------------------//
						long userId=writeService.getkey("seq_user_info_tb");
						//映射表
						Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
						carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
								" values (?,?,?,?)");
						carMapSqlMap.put("values", new Object[]{table_type, p4Id, userId, groupId});
						bathSql.add(carMapSqlMap);
						//收费员
						String strid = userId + "zld";
						Map<String, Object> carSqlMap = new HashMap<String, Object>();
						carSqlMap.put("sql", "insert into user_info_tb (id,nickname,password,strid,reg_time," +
								"mobile,groupid,role_id,auth_flag,creator_id) values (?,?,?,?,?,?,?,?,?,?)");
						carSqlMap.put("values", new Object[]{userId, nickname, strid, strid, reg_time, mobile,
								groupId, roleId, 2, createor_id});
						bathSql.add(carSqlMap);
					}
					
					boolean b = writeService.bathUpdate2(bathSql);
					logger.error("结果>>>b:"+b+",maxId:"+maxId);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void syncPark(){
		int table_type = 1;
		clearData(table_type);
		boolean stop = false;
		try {
			int pageSize = 100;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(table_type);
				String url = "http://www.bouwa.net:8090/Api/Sales/values/GetParkList?id="+maxId+"&pagesize="+pageSize+"&str="+condition;
				String result = new HttpProxy().doGet(url);
				if(result != null && !"".equals(result)){
					result = result.replace("\\","");
					result = result.substring(1, result.length() - 1);
					JSONArray array = JSONArray.fromObject(result);
					//----------------------校验是否结束循环------------------------//
					if(array.size() == 0){
						logger.error("最后一次循环获取的数据结果为空>>>size:"+array.size());	
						return;
					}
					if(array.size() < pageSize){
						logger.error("最后一次循环>>>size:"+array.size());
						stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
					}
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					
					Long lastId = maxId;//上一个的p4主键编号
					for(int i = 0; i< array.size(); i++){
						JSONObject object = array.getJSONObject(i);
						logger.error("object:"+object.toString());
						Long p4Id = object.getLong("id");
						String company_name = object.getString("company_name");
						Long create_time = TimeTools.getLongMilliSecondFromStrDate(object.getString("create_time"),"yyyy/MM/dd HH:mm:ss")/1000;
						Integer parking_total = object.getInt("parking_total");
						if(p4Id == null
								|| p4Id <= 0){
							logger.error("返回信息错误>>>maxId:"+maxId);
							return;
						}
						//----------------------校验传过来的数据是否是正序排序------------------------//
						if(p4Id.intValue() <= lastId.intValue()){
							logger.error("停止循环，传过来的参数排序错误>>>p4Id:"+p4Id);
							return;
						}
						lastId = p4Id;
						//-------------------------注册车场-----------------------------------//
						Long comId = writeService.getkey("seq_com_info_tb");
						//车牌映射表
						Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
						carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
								" values (?,?,?,?)");
						carMapSqlMap.put("values", new Object[]{table_type, p4Id, comId, groupId});
						bathSql.add(carMapSqlMap);
						//车牌
						Map<String, Object> carSqlMap = new HashMap<String, Object>();
						carSqlMap.put("sql", "insert into com_info_tb(id,company_name,create_time,parking_total," +
								"groupid,etc,state) values(?,?,?,?,?,?,?)");
						carSqlMap.put("values", new Object[]{comId, company_name, create_time, 
								parking_total, groupId, etc, 0});
						bathSql.add(carSqlMap);
					}
					
					boolean b = writeService.bathUpdate2(bathSql);
					logger.error("结果>>>b:"+b+",maxId:"+maxId);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void syncCard(){
		int table_type = 0;
		clearData(table_type);
		boolean stop = false;
		try {
			Long curTime = System.currentTimeMillis()/1000;
			int pageSize = 500;
			while (!stop) {
				//---------------------查询已导入的数据最后的一条--------------------------//
				Long maxId = getMaxId(table_type);
				String url = "http://www.bouwa.net:8090/Api/Sales/values/GetCardList?id="+maxId+"&pagesize="+pageSize+"&str="+condition;
				String result = new HttpProxy().doGet(url);
				if(result != null && !"".equals(result)){
					result = result.replace("\\","");
					result = result.substring(1, result.length() - 1);
					JSONArray array = JSONArray.fromObject(result);
					//----------------------校验是否结束循环------------------------//
					if(array.size() == 0){
						logger.error("最后一次循环获取的数据结果为空>>>size:"+array.size());	
						return;
					}
					if(array.size() < pageSize){
						logger.error("最后一次循环>>>size:"+array.size());
						stop = true;//最后一次循环，返回的数据不足pageSize说明后面再没数据
					}
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					//------------------------------批量插入卡片----------------------------//
					String addCard = "insert into com_nfc_tb(id,nfc_uuid," +
							"create_time,state,uin,device,balance,card_number," +
							"group_id,reg_id,type,activate_id,activate_time) " +
							" values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
					List<Object[]> cardValues = new ArrayList<Object[]>();
					
					Long lastId = maxId;//上一个的p4主键编号
					for(int i = 0; i< array.size(); i++){
						JSONObject object = array.getJSONObject(i);
						logger.error("卡片信息>>>card:"+object.toString());
						Long p4Id = object.getLong("id");
						String nfc_uuid = object.getString("nfc_uuid");
						Long create_time = TimeTools.getLongMilliSecondFromStrDate(object.getString("create_time"),"yyyy/MM/dd HH:mm:ss")/1000;
						Long reg_id = -1L;
						if(!"".equals(object.getString("reg_id"))){
							reg_id = object.getLong("reg_id");
						}
						Long activate_time = TimeTools.getLongMilliSecondFromStrDate(object.getString("activate_time"),"yyyy/MM/dd HH:mm:ss")/1000;
						Long activate_id = -1L;
						if(!"".equals(object.getString("activate_id"))){
							activate_id = object.getLong("activate_id");
						}
						Integer state = object.getInt("state");
						String mobile = object.getString("mobile");
						String carnumber = object.getString("carnumber");
						String device = object.getString("device");
						String card_number = object.getString("card_number");
						Double balance = object.getDouble("balance");
						if(p4Id == null
								|| p4Id <= 0
								|| nfc_uuid == null
								|| "".equals(nfc_uuid)
								|| create_time == null
								|| activate_time == null
								|| state == null
								|| (state != 0 && state != 1)
								|| balance == null){
							logger.error("返回信息错误>>>maxId:"+maxId);
							continue;
						}
						//----------------------校验传过来的数据是否是正序排序------------------------//
						if(p4Id.intValue() <= lastId.intValue()){
							logger.error("停止循环，传过来的参数排序错误>>>p4Id:"+p4Id);
							continue;
						}
						lastId = p4Id;
						//------------------------校验卡片是否已存在 --------------------------//
						Long cardCount = writeService.getLong("select count(id) from com_nfc_tb where" +
								" nfc_uuid=? and type=? and is_delete=? ", new Object[]{nfc_uuid, 2, 0});
						if(cardCount > 0){
							logger.error("退出，卡片信息已存在>>>nfc_uuid:"+nfc_uuid);
							return;
						}
						//-------------------------查找收费员映射-----------------------------//
						reg_id = getMappingId(reg_id, 5);
						activate_id = getMappingId(activate_id, 5);
						logger.error("reg_id:"+reg_id+",activate_id:"+activate_id);
						//------------------------获取绑定的用户主键 --------------------------//
						Long userId = -1L;//用户编号 
						if(mobile != null && !"".equals(mobile) 
								&& carnumber != null && !"".equals(carnumber)
								&& StringUtils.checkMobile(mobile)
								&& StringUtils.checkPlate(carnumber)){
							Map<String, Object> userMap = writeService.getMap("select id from" +
									" user_info_tb where mobile=? and auth_flag=? ", 
									new Object[]{mobile, 4});
							if(userMap == null){
								logger.error("手机号未注册>>>mobile:"+mobile);
								userId = writeService.getkey("seq_user_info_tb");
								//用户映射表
								Map<String, Object> userMapSqlMap = new HashMap<String, Object>();
								userMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
										" values (?,?,?,?)");
								userMapSqlMap.put("values", new Object[]{13, -1, userId, groupId});
								bathSql.add(userMapSqlMap);
								//用户
								String strid = userId+"zld";
								Map<String, Object> userSqlMap = new HashMap<String, Object>();
								userSqlMap.put("sql", "insert into user_info_tb (id,nickname,password,strid," +
										"reg_time,mobile,auth_flag,comid) values (?,?,?,?,?,?,?,?)");
								userSqlMap.put("values", new Object[]{userId, "车主", strid, strid, 
										curTime, mobile, 4, 0});
								bathSql.add(userSqlMap);
							}else{
								logger.error("手机号已经注册>>>mobile:"+mobile);
								userId = (Long)userMap.get("id");
							}
							logger.error("userId:"+userId+",mobile:"+mobile);
							if(userId > 0){
								Long carCount = writeService.getLong("select count(c.id) from user_info_tb u,car_info_tb c" +
										" where u.id=c.uin and c.car_number=? and u.auth_flag=?",
										new Object[]{carnumber, 4});
								logger.error("carCount:"+carCount);
								if(carCount == 0){
									//删除车牌
									Map<String, Object> delCarSqlMap = new HashMap<String, Object>();
									delCarSqlMap.put("sql", "delete from car_info_tb where car_number=?");
									delCarSqlMap.put("values", new Object[]{carnumber});
									bathSql.add(delCarSqlMap);
									
									Long plateId = writeService.getkey("seq_car_info_tb");
									logger.error("车牌号未注册>>>carnumber:"+carnumber+",plateId:"+plateId);
									//车牌映射表
									Map<String, Object> carMapSqlMap = new HashMap<String, Object>();
									carMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
											" values (?,?,?,?)");
									carMapSqlMap.put("values", new Object[]{6, -1, plateId, groupId});
									bathSql.add(carMapSqlMap);
									//车牌
									Map<String, Object> carSqlMap = new HashMap<String, Object>();
									carSqlMap.put("sql", "insert into car_info_tb (id,uin,car_number,create_time) values (?,?,?,?)");
									carSqlMap.put("values", new Object[]{plateId, userId, carnumber, curTime});
									bathSql.add(carSqlMap);
								}
							}
						}
						//------------------------卡片状态 --------------------------//
						int p5State = 3;//开卡
						if(state == 1){//p4已激活
							p5State = 0;//已激活
						}
						if(userId > 0){
							p5State = 2;//已绑定用户
						}
						logger.error("p5State:"+p5State);
						//------------------------注册卡片 --------------------------//
						//卡片
						long cardId = writeService.getkey("seq_com_nfc_tb");
						//车牌映射表
						Map<String, Object> cardMapSqlMap = new HashMap<String, Object>();
						cardMapSqlMap.put("sql", "insert into tmp_p4top5_primarykey (table_type,p4_id,p5_id,groupid)" +
								" values (?,?,?,?)");
						cardMapSqlMap.put("values", new Object[]{table_type, p4Id, cardId, groupId});
						bathSql.add(cardMapSqlMap);
						//------------------------注册卡片 --------------------------//
						nfc_uuid = nfc_uuid.toLowerCase();
						Object[] va = new Object[13];
						va[0] = cardId;
						va[1] = nfc_uuid;
						va[2] = create_time;
						va[3] = p5State;
						va[4] = userId;
						va[5] = device;
						va[6] = balance;
						va[7] = card_number;
						va[8] = groupId;//晋中的运营集团编号
						va[9] = reg_id;
						va[10] = 2;//商家卡类型
						va[11] = activate_id;
						va[12] = activate_time;//商家卡类型
						cardValues.add(va);
					}
					boolean b = writeService.bathUpdate2(bathSql);
					logger.error("结果>>>b:"+b+",maxId:"+maxId);
					int card = writeService.bathInsert(addCard, cardValues, new int []{4,12,4,4,4,12,3,12,4,4,4,4,4});
					logger.error("批量插入结果>>>card:"+card+",maxId:"+maxId);
					
				}
			}
		} catch (Exception e) {
			logger.equals(e);
			e.printStackTrace();
		}
	}
	
	private void syncOldNopaymentData(){
		try {
			long startTime = 1448899200;//2015-12-01 00:00:00
			while (true) {
				List<Map<String, Object>> list = readService.getAll("select * from no_payment_tb where " +
						" end_time between ? and ? and groupid<=? order by id ", new Object[]{startTime, 
						startTime + 24 * 60 * 60, 0});
				if(list == null || list.isEmpty()){
					logger.error("循环结束");
					return;
				}
				for(Map<String, Object> map : list){
					Long noId = (Long)map.get("id");
					Long orderId = (Long)map.get("order_id");
					Integer state = (Integer)map.get("state");
					Map<String, Object> orderMap = readService.getMap("select * from order_tb where id=? ",
							new Object[]{orderId});
					if(orderMap != null){
						Long groupId = -1L;
						if(orderMap.get("groupid") != null){
							groupId = (Long)orderMap.get("groupid");
						}
						Long berthsec_id = -1L;
						if(orderMap.get("berthsec_id") != null){
							berthsec_id = (Long)orderMap.get("berthsec_id");
						}
						Long berthnumber = -1L;
						if(orderMap.get("berthnumber") != null){
							berthnumber = (Long)orderMap.get("berthnumber");
						}
						Double prepaid = Double.valueOf(orderMap.get("prepaid") + "");
						Long pursue_comid = -1L;
						Long pursue_berthseg_id = -1L;
						Long pursue_groupid = -1L;
						if(state == 1){
							pursue_groupid = groupId;
							Long pursue_uid = (Long)map.get("pursue_uid");
							Long pursue_time = (Long)map.get("pursue_time");
							WorkRecord workRecord = readService.getPOJO("select berthsec_id from " +
									" parkuser_work_record_tb where uid=? and start_time<=? and end_time>=? limit ?",
									new Object[]{pursue_uid, pursue_time, pursue_time, 1}, WorkRecord.class);
							if(workRecord != null){
								pursue_berthseg_id = workRecord.getBerthsec_id();
								BerthSeg berthSeg = readService.getPOJO("select comid from com_berthsecs_tb " +
										" where id=?", new Object[]{pursue_berthseg_id}, BerthSeg.class);
								if(berthSeg != null){
									pursue_comid = berthSeg.getComid();
								}
							}
						}
						int r = writeService.update("update no_payment_tb set berthseg_id=?,berth_id=?," +
								"groupid=?,prepay=?,pursue_comid=?,pursue_berthseg_id=?,pursue_groupid=? where id=? ", 
								new Object[]{berthsec_id, berthnumber, groupId, prepaid,pursue_comid, 
								pursue_berthseg_id, pursue_groupid, noId});
						logger.error("r:"+r);
					}
				}
				startTime += 24 * 60 * 60;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void syncOldCardData(){
		try {
			long startTime = 1472659200;//2016-10-01 00:00:00
			while (true) {
				List<Map<String, Object>> list = readService.getAll("select * from card_account_tb where " +
						" create_time between ? and ? and groupid<=? order by id ", new Object[]{startTime, 
						startTime + 24 * 60 * 60, 0});
				if(list == null || list.isEmpty()){
					logger.error("循环结束");
					//return;
				}
				for(Map<String, Object> map : list){
					logger.error(map);
					Long id = (Long)map.get("id");
					Long card_id = (Long)map.get("card_id");
					Integer type = (Integer)map.get("type");//（卡片的生命周期）0：充值 1：消费 2：开卡（卡片初始化，此时的卡片还不能使用） 3：激活卡片（此时卡片方可使用） 4：绑定用户 5：注销卡片
					Integer charge_type = (Integer)map.get("charge_type");//充值方式：0：现金充值 1：微信公众号充值 2：微信客户端充值 3：支付宝充值 4：预支付退款 5：订单退款
					Integer consume_type = (Integer)map.get("consume_type");//消费方式 0：支付停车费（非预付） 1：预付停车费 2：补缴停车费  3：追缴停车费
					Long orderid = (Long)map.get("orderid");
					Long uid = (Long)map.get("uid");//收费员账号
					Long create_time = (Long)map.get("create_time");
					Card card = readService.getPOJO("select group_id from com_nfc_tb " +
							" where id=? ", new Object[]{card_id}, Card.class);
					if(card == null || card.getGroup_id() <= 0){
						logger.error("卡片信息错误,card_id:"+card_id);
						continue;
					}
					Long groupid = card.getGroup_id();
					Long comid = -1L;
					Long berthseg_id = -1L;
					Long berth_id = -1L;
					if(charge_type == 4
							|| consume_type == 0
							|| consume_type == 1
							|| consume_type == 2){
						if(orderid <= 0){
							logger.error("订单信息错误,card_id:"+card_id);
							continue;
						}
						Order order = readService.getPOJO("select * from order_tb where id=? ",
								new Object[]{orderid}, Order.class);
						comid = order.getComid();
						berth_id = order.getBerthnumber();
						berthseg_id = order.getBerthsec_id();
					}else{//追缴停车费
						WorkRecord workRecord = readService.getPOJO("select berthsec_id from " +
								" parkuser_work_record_tb where uid=? and start_time<=? and end_time>=? limit ?",
								new Object[]{uid, create_time, create_time, 1}, WorkRecord.class);
						if(workRecord != null){
							berthseg_id = workRecord.getBerthsec_id();
							BerthSeg berthSeg = readService.getPOJO("select comid from com_berthsecs_tb " +
									" where id=?", new Object[]{berthseg_id}, BerthSeg.class);
							if(berthSeg != null){
								comid = berthSeg.getComid();
							}
						}
					}
					
					logger.error("groupid:"+groupid+",comid:"+comid+",berthseg_id:"+berthseg_id+",berth_id:"+berth_id);
					int r = writeService.update("update card_account_tb set berthseg_id=?," +
							"berth_id=?,groupid=?,comid=? where id=? ", new Object[]{berthseg_id, 
							berth_id, groupid, comid, id});
					logger.error("r:"+r);
				}
				startTime += 24 * 60 * 60;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void syncOldCardActData(){
		try {
			long startTime = 1475251200;//2016-10-01 00:00:00
			while (true) {
				List<Map<String, Object>> list = readService.getAll("select * from card_account_tb where " +
						" create_time between ? and ? and type=? and amount=? order by id ", 
						new Object[]{startTime, startTime + 24 * 60 * 60, 3, 0});
				if(list == null || list.isEmpty()){
					logger.error("循环结束");
					return;
				}
				for(Map<String, Object> map : list){
					logger.error(map);
					Long id = (Long)map.get("id");
					String remark = (String)map.get("remark");
					int s = remark.indexOf("额");
					int e = remark.indexOf("元");
					String money = remark.substring(s + 1, e);
					Double vDouble = Double.valueOf(money);
					int r = writeService.update("update card_account_tb set amount=? where id=? ", 
							new Object[]{vDouble, id});
					logger.error("r:"+r);
				}
				startTime += 24 * 60 * 60;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void syncCardAnly(){
		try {
			long groupid = 36;
			List<Map<String, Object>> list = readService.getAll("select * from card_anlysis_tb " +
					" where groupid=? order by create_time desc  ", new Object[]{groupid});
			for(int i = 0; i < list.size(); i++){
				Map<String, Object> map = list.get(i);
				Integer all_count = 0;//截止当前已激活卡片的数量
				Double all_balance = 0d;//截止当前已激活卡片的余额
				Double slot_charge = 0d;//一天内充值的金额
				Double slot_consume = 0d;//一天内消费的金额
				Integer slot_refund_count = 0;//一天内注销的卡片数量
				Double slot_refund_balance = 0d;//一天内注销的卡片退还的金额
				Integer slot_act_count = 0;//一天内的激活卡片数量
				Double slot_act_balance = 0d;//一天内激活的卡片初始化余额
				Integer slot_bind_count = 0;//一天内绑定的卡片数量
				if(map.get("all_count") != null){
					all_count = (Integer)map.get("all_count");
				}
				if(map.get("all_balance") != null){
					all_balance = Double.valueOf(map.get("all_balance") + "");
				}
				if(map.get("slot_charge") != null){
					slot_charge = Double.valueOf(map.get("slot_charge") + "");
				}
				if(map.get("slot_consume") != null){
					slot_consume = Double.valueOf(map.get("slot_consume") + "");
				}
				if(map.get("slot_refund_count") != null){
					slot_refund_count = (Integer)map.get("slot_refund_count");
				}
				if(map.get("slot_refund_balance") != null){
					slot_refund_balance = Double.valueOf(map.get("slot_refund_balance") + "");
				}
				if(map.get("slot_act_count") != null){
					slot_act_count = (Integer)map.get("slot_act_count");
				}
				if(map.get("slot_act_balance") != null){
					slot_act_balance = Double.valueOf(map.get("slot_act_balance") + "");
				}
				if(map.get("slot_bind_count") != null){
					slot_bind_count = (Integer)map.get("slot_bind_count");
				}
				
				double money = StringUtils.formatDouble(slot_charge + slot_act_balance - slot_consume - slot_refund_balance);
				int count = slot_act_count - slot_refund_count;
				
				double beforeMoney = StringUtils.formatDouble(all_balance - money);
				int beforeCount = all_count - count;
				
				Map<String, Object> beforeMap = list.get(i + 1);
				Long beforeId = (Long)beforeMap.get("id");
				beforeMap.put("all_count", beforeCount);
				beforeMap.put("all_balance", beforeMoney);
				int r = writeService.update("update card_anlysis_tb set all_count=?," +
						"all_balance=? where id=? ", new Object[]{beforeCount, beforeMoney, beforeId});
				logger.error("r:"+r);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void consumeWrongCard(){
		try {
			FileReader fr=new FileReader("D:\\nfc.txt");
	        BufferedReader br=new BufferedReader(fr);
	        String line = null;
	        while ((line = br.readLine()) != null) {
	        	Long count = readService.getLong("select count(id) from com_nfc_tb where nfc_uuid like ? and type=? " +
	        			" and is_delete=? ", new Object[]{"%"+line+"%", 2, 0});
	        	if(count == 0){
	        		System.out.println("卡片不存在:" + line);
	        		continue;
	        	}
	        	if(count == 1){
	        		System.out.println("卡片不存在重复:" + line);
	        		continue;
	        	}
				Long count2 = readService.getLong("select count(a.id) from com_nfc_tb c,card_account_tb a " +
						" where c.id=a.card_id and c.nfc_uuid like ? ", new Object[]{"%"+line+"%"});
				if(count2 == 0){
					System.out.println("无消费记录:" + line);
					continue;
				}
				List<Card> cards = readService.getPOJOList("select * from com_nfc_tb where nfc_uuid like ? and type=? " +
	        			" and is_delete=?", new Object[]{"%"+line+"%", 2, 0}, Card.class);
		        System.out.println(cards);
		        System.out.println();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//-----------------------------------分区表导数据------------------------------------------------//
	private void copyBerthOrder(){
		try {
			long startTime = 1469980800;//2016-07-01 00:00:00
			while (true && startTime < 1472659200) {
				int r = writeService.update("insert into order_tb_2016_08 select * from " +
						" only order_tb where create_time between ? and ? ", 
						new Object[]{startTime, startTime + 24 * 60 * 60 - 1});
				System.out.println(r);
				if(r > 0){
					r = writeService.update("delete from only order_tb where create_time between ? and ? ",
							new Object[]{startTime, startTime + 24 * 60 * 60 - 1});
					System.out.println(r);
				}
				/*int r = writeService.update("delete from only berth_order_tb where id in " +
						" (select id from berth_order_tb where in_time between ? and ? group by id having count(id)>?) ", 
						new Object[]{startTime, startTime + 24 * 60 * 60 - 1, 1});*/
				startTime += 24 * 60 * 60;
			}
			logger.error("结束");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
class FileReader2 extends InputStreamReader{
	public FileReader2(String FileName,String charSetName)throws  FileNotFoundException,UnsupportedEncodingException{
		super(new FileInputStream(FileName), charSetName);
	}
}
