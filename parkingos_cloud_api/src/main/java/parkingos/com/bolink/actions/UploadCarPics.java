package parkingos.com.bolink.actions;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import parkingos.com.bolink.beans.CarpicTb;
import parkingos.com.bolink.beans.ComInfoTb;
import parkingos.com.bolink.beans.OrgCityMerchants;
import parkingos.com.bolink.beans.ParkTokenTb;
import parkingos.com.bolink.dao.MongoDBFactory;
import parkingos.com.bolink.utlis.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;

//import com.sun.org.apache.xml.internal.security.utils.Base64;


/**
 *  图片上传
 * @author laoyao
 *
 */
@Controller
public class UploadCarPics {

	Logger logger = Logger.getLogger(UploadCarPics.class);
	@Autowired
	CommonDao commonDao;


	@RequestMapping(value = "/carpicsup.do")
	public void carPics(HttpServletRequest request, HttpServletResponse response) throws Exception{
		String action = RequestUtil.processParams(request, "action");
		if (action.equals("receivepic")) {
			/*
				 * 通过流的方式读取request请求对象中的数据
				 */
			logger.info(">>>>>>>>>>>>>>调用上传图片的方法...........start");
			byte[] bytes = new byte[1024 * 1024];
			InputStream is = request.getInputStream();

			int nRead = 1;
			int nTotalRead = 0;
			while (nRead > 0) {
				nRead = is.read(bytes, nTotalRead, bytes.length - nTotalRead);
				if (nRead > 0)
					nTotalRead = nTotalRead + nRead;
			}
			String str = new String(bytes, 0, nTotalRead, "utf-8");
			JSONObject jsonObj = null;
			try {
				jsonObj = JSONObject.parseObject(str);
			} catch (Exception e) {
				StringUtils.ajaxOutput(response, "json数据格式不正确");
			}
			//logger.info(jsonObj.toJSONString());
			String token = "";
			if (jsonObj.containsKey("token")) {
				token = jsonObj.getString("token");
			}
			String serviceName = "";
			if (jsonObj.containsKey("serviceName")) {
				serviceName = jsonObj.getString("service_name");
			}
			String data = "";
			if (jsonObj.containsKey("data")) {
				data = jsonObj.getString("data");
			}
			// 根据token查询出对应的车场id
			String comidNew = "";
			ParkTokenTb tokenTb = new ParkTokenTb();
			tokenTb.setToken(token);
			tokenTb = (ParkTokenTb) commonDao.selectObjectByConditions(tokenTb);//.selectListByConditions(token);//.selectObjectBySelective(tokenTb);

			if (tokenTb != null) {
				comidNew = tokenTb.getParkId();
			} else {
				logger.error(">>>>>>>>>>>>>>>>>token错误，未找到对应的车场信息：" + token);
				return;
			}

			logger.error(">>>>>>>>>>>>>>>>收到图片数据内容长度........" + data.length());
			String result = handleSDKuploadPic(data, comidNew); //doUpload.uploadCarpic(comidNew, data);
			logger.error(">>>>>>>>>>>>>>>>>>>>上传图片执行结果：uploadCarpic:" + result);
			StringUtils.ajaxOutput(response, result);
		}else if(action.equals("getpic")){
			String typeStr = RequestUtil.getString(request, "typeNew");
			String orderidlocal = RequestUtil.getString(request, "orderid");
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			getpictureNew(orderidlocal, comid,typeStr,request,response);
		}else if(action.equals("getnewpic")){
			String orgData  = readBodyFormRequsetStream(request);
			logger.error(orgData);
			JSONObject jsonObject = JSONObject.parseObject(orgData);
			String typeStr = jsonObject.getString("type");
			String orderidlocal = jsonObject.getString("orderid");
			String comid = jsonObject.getString("comid");
			String sign = jsonObject.getString("sign");
			Long comId = -1L;
			if(Check.isLong(comid)){
				comId = Long.parseLong(comid);
				ComInfoTb infoTb = new ComInfoTb();
				infoTb.setId(comId);
				infoTb = (ComInfoTb)commonDao.selectObjectByConditions(infoTb);
				if(infoTb!=null&&infoTb.getCityid()!=null){
					OrgCityMerchants merchants = new OrgCityMerchants();
					merchants.setId(infoTb.getCityid());
					merchants = (OrgCityMerchants)commonDao.selectObjectByConditions(merchants);
					logger.error("cityinfo:"+merchants);
					if(merchants!=null){
						String signString = "comid="+comid+"&orderid="+orderidlocal+"&type="+typeStr+"key="+merchants.getUkey();
						String _sign = StringUtils.MD5(signString, "utf-8");
						logger.error("presign:"+sign+",sign:"+_sign+",result:"+sign.equals(_sign));
					}
					getpictureNew(orderidlocal, comId,typeStr,request,response);
				}
//				Map cityMap = .getMap("select ukey from org_city_merchants where id = " +
//						"(select cityid from com_info_tb where id =? )", new Object[]{comId});
			}
		}else if(action.equals("getnewcloudpic")){
			String orgData  = readBodyFormRequsetStream(request);
			logger.error(orgData);
			JSONObject dataJson = JSONObject.parseObject(orgData, Feature.OrderedField);
			JSONObject jsonObject = JSONObject.parseObject(dataJson.getString("data"));
			String typeStr = jsonObject.getString("type");
			String orderidlocal = jsonObject.getString("orderid");
			String comid = jsonObject.getString("comid");
			String sign = jsonObject.getString("sign");
			Long comId = -1L;
			if(Check.isLong(comid)){
				comId = Long.parseLong(comid);
				ComInfoTb infoTb = new ComInfoTb();
				infoTb.setId(comId);
				infoTb = (ComInfoTb)commonDao.selectObjectByConditions(infoTb);
				if(infoTb!=null&&infoTb.getCityid()!=null){
					OrgCityMerchants merchants = new OrgCityMerchants();
					merchants.setId(infoTb.getCityid());
					merchants = (OrgCityMerchants)commonDao.selectObjectByConditions(merchants);
					logger.error("cityinfo:"+merchants);
					if(merchants!=null){
						String signString = dataJson.getString("data")+"key="+merchants.getUkey();
						String _sign = StringUtils.MD5(signString, "utf-8");
						logger.error("presign:"+sign+",sign:"+_sign+",result:"+sign.equals(_sign));
					}
					getpictureNew(orderidlocal, comId,typeStr,request,response);
				}
			}
		}
	}

	//取request流数据
	private  String readBodyFormRequsetStream(HttpServletRequest request) {
		try {
			// request.setCharacterEncoding("UTF-8");
			int size = request.getContentLength();
			///  System.out.println(size);
			if (size > 0) {
				InputStream is = request.getInputStream();
				int readLen = 0;
				int readLengthThisTime = 0;
				byte[] message = new byte[size];
				while (readLen != size) {
					readLengthThisTime = is.read(message, readLen, size- readLen);
					if (readLengthThisTime == -1) {// Should not happen.
						break;
					}
					readLen += readLengthThisTime;
				}
				return new String(message,"utf-8");
			}

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return "";
	}
	/**
	 * 处理SDK图片上传
	 *
	 * @param data
	 * @param parkId
	 * @return
	 */
	private String handleSDKuploadPic(String data, String parkId) throws Exception {
		JSONObject jsonObj = JSONObject.parseObject(data);
		String pictureSource = "";
		String returnRet = "";
		int ret = 0;
		if (jsonObj.containsKey("picture_source")
				&& jsonObj.getString("picture_source") != null) {
			pictureSource = jsonObj.getString("picture_source");
		} else {
			return "{\"state\":0,\"errmsg\":\"图片上传失败，缺少必填字段picture_source！\"}";
		}
		String dbName = "";
		String yearMonth = TimeTools.getTimeYYYYMMDDHHMMSS().substring(0, 6);
		String orderId = "";
		String liftRodId = "";
		String parkOrderType = "";
		String content = "";
		Long createTime = System.currentTimeMillis() / 1000;
		String resume = "";
		String picType = "";
		String carNumber = "";
		String eventId = "";
		if (pictureSource.equals("order")) {
			if (jsonObj.containsKey("order_id")) {
				orderId = jsonObj.getString("order_id");
				if(Check.isEmpty(orderId)){
					return "{\"state\":0,\"errmsg\":\"图片上传失败，缺少订单编号！\"}";
				}
			} else {
				return "{\"state\":0,\"errmsg\":\"图片上传失败，缺少订单编号！\"}";
			}
			dbName = "car_pic_" + yearMonth;
		} else if (pictureSource.equals("liftrod")) {
			if (jsonObj.containsKey("liftrod_id")) {
				liftRodId = jsonObj.getString("liftrod_id");
				if(Check.isEmpty(liftRodId)){
					return "{\"state\":0,\"errmsg\":\"图片上传失败，缺少抬杆记录编号！\"}";
				}
			} else {
				return "{\"state\":0,\"errmsg\":\"图片上传失败，缺少抬杆记录编号！\"}";
			}
			dbName = "liftrod_pic_" + yearMonth;
		}else if(pictureSource.equals("confirm")){
			if(jsonObj.containsKey("event_id")){
				eventId = jsonObj.getString("event_id");
			}else{
				return "{\"state\":0,\"errmsg\":\"图片上传失败，缺少事件编号！\"}";
			}
			dbName = "confirm_pic_" + yearMonth;
		}
		if (jsonObj.containsKey("content")) {
			content = jsonObj.getString("content");
		} else {
			return "{\"state\":0,\"errmsg\":\"图片上传失败，缺少图片数据！\"}";
		}

		if (jsonObj.containsKey("create_time")
				&& Check.isLong(jsonObj.getString("create_time"))) {
			createTime = jsonObj.getLong("create_time");
		}
		if (jsonObj.containsKey("car_number"))
			carNumber = jsonObj.getString("car_number");

		if (jsonObj.containsKey("resume"))
			resume = jsonObj.getString("resume");
		if (jsonObj.containsKey("park_order_type"))
			parkOrderType = jsonObj.getString("park_order_type");
		if(jsonObj.containsKey("pic_type"))
			picType = jsonObj.getString("pic_type");
		byte[] picture = Base64Utils.decode(content.getBytes());// Base64.decode(content);
		DB mydb = MongoDBFactory.getInstance().getMongoDBBuilder("zld");
		mydb.requestStart();
		DBCollection collection = mydb.getCollection(dbName);
		// DBCollection collection = mydb.getCollection("records_test");
		BasicDBObject document = new BasicDBObject();
		document.put("parkid", parkId);
		document.put("ctime", createTime);
		document.put("resume", resume);
		document.put("content", picture);
		document.put("type","image/"+picType);

		if(pictureSource.equals("order")){
			document.put("orderid", orderId);
			document.put("gate", parkOrderType);
		}else if(pictureSource.equals("liftrod")){
			document.put("liftrodid", liftRodId);
		}else if(pictureSource.equals("confirm")){
			document.put("event_id", eventId);
		}

		// 开始事务
		mydb.requestStart();
		collection.insert(document);
		// 结束事务
		mydb.requestDone();
		//把表名写入订单表，区分订单或抬杆
		if(pictureSource.equals("order")){
			//将mongodb中存取图片所对应的表名写入到数据库中
			//先查询carpic_tb表中是否已经存在该记录，存在则更新，不存在则新添加一条
			CarpicTb carpicTb = new CarpicTb();
			carpicTb.setComid(parkId);
			carpicTb.setOrderId(orderId);
			int count = commonDao.selectCountByConditions(carpicTb);//.selectCountBySelective(carpicTb);
			int r = 0;
			if(count>0){
				CarpicTb fields = new CarpicTb();
				fields.setCarpicTableName(dbName);
				r = commonDao.updateByConditions(fields,carpicTb);//.updateBySelective(fields,carpicTb);
			}else{
				carpicTb.setCarpicTableName(dbName);
				r = commonDao.insert(carpicTb);
			}
			logger.info("result:"+r);

		}else if(pictureSource.equals("liftrod")){
			//将mongodb中存取图片所对应的表名写入到数据库中
			//先查询carpic_tb表中是否已经存在该记录，存在则更新，不存在则新添加一条
			CarpicTb carpicTb = new CarpicTb();
			carpicTb.setComid(parkId);
			carpicTb.setLiftrodId(liftRodId);
			int count = commonDao.selectCountByConditions(carpicTb);//.selectCountBySelective(carpicTb);
			int r = 0;
			if(count>0){
				CarpicTb fields = new CarpicTb();
				fields.setCarpicTableName(dbName);
				r = commonDao.updateByConditions(fields,carpicTb);//.updateBySelective(fields,carpicTb);
			}else{
				carpicTb.setLiftpicTableName(dbName);
				r = commonDao.insert(carpicTb);
			}
			logger.info("result:"+r);

		}else if(pictureSource.equals("confirm")){
			//将mongodb中存取图片所对应的表名写入到数据库中
			//先查询carpic_tb表中是否已经存在该记录，存在则更新，不存在则新添加一条
			CarpicTb carpicConditions = new CarpicTb();
			carpicConditions.setEventId(eventId);
			carpicConditions.setComid(parkId);
			CarpicTb carpicTb = (CarpicTb) commonDao.selectObjectByConditions(carpicConditions);
			if(carpicTb != null ){
				CarpicTb updateCarpic = new CarpicTb();
				updateCarpic.setConfirmpicTableName(dbName);
				int updateCarpicConfirm = commonDao.updateByConditions(updateCarpic,carpicConditions);
				logger.error(">>>>>>>>>>>>>>>>>>更新图片资源地址结果......."+updateCarpicConfirm+">>>>>>>>>>事件记录编号:"+eventId);
			}else{
				CarpicTb insertCarpic = new CarpicTb();
				insertCarpic.setEventId(eventId);
				insertCarpic.setComid(parkId);
				insertCarpic.setConfirmpicTableName(dbName);
				int addCarpicConfirm = commonDao.insert(insertCarpic);
				logger.error(">>>>>>>>>>>>>>>>添加图片资源地址的结果........."+addCarpicConfirm+">>>>>>>>>>事件记录编号:"+eventId);
			}
		}
		return "{\"state\":1,\"errmsg\":\"上传成功！\"}";
	}
	private void getpictureNew(String orderidlocal, Long comid, String typeStr,
							   HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.error("getpictureNew from mongodb....");
		logger.error("getpictureNew from mongodb file:orderid="+orderidlocal+"type="+typeStr);
		if(orderidlocal!=null && typeStr !=null){
			long currentnum = RequestUtil.getLong(request, "currentnum",-1L);
			DB db = MongoDBFactory.getInstance().getMongoDBBuilder("zld");//
			//根据订单编号查询出mongodb中存入的对应个表名
			CarpicTb carpicConditions = new CarpicTb();
			carpicConditions.setOrderId(orderidlocal);
			carpicConditions.setComid(String.valueOf(comid));
			CarpicTb carpicTb = (CarpicTb)commonDao.selectObjectByConditions(carpicConditions);
			String collectionName = "";
			if(carpicTb !=null ){
				collectionName = carpicTb.getCarpicTableName();
			}
			if(collectionName==null||"".equals(collectionName)||"null".equals(collectionName)){
				logger.error(">>>>>>>>>>>>>查询图片错误........");
				response.sendRedirect("http://test.bolink.club/zld/images/nocar.png");
				return;
			}
			logger.error("table:"+collectionName);
			DBCollection collection = db.getCollection(collectionName);
			if(collection != null){
				BasicDBObject document = new BasicDBObject();
				document.put("parkid", String.valueOf(comid));
				document.put("orderid", orderidlocal);
				document.put("gate", typeStr);
				if(currentnum>=0){
					document.put("currentnum", currentnum);
				}
				DBObject obj  = collection.findOne(document);
				if(obj == null){
					AjaxUtil.ajaxOutput(response, "");
					logger.error("取图片错误.....");
					return;
				}
				byte[] content = (byte[])obj.get("content");
				logger.error("取图片成功.....大小:"+content.length);
				db.requestDone();
				response.setDateHeader("Expires", System.currentTimeMillis()+12*60*60*1000);
				response.setContentLength(content.length);
				response.setContentType("image/jpeg");
				OutputStream o = response.getOutputStream();
				o.write(content);
				o.flush();
				o.close();
				System.out.println("mongdb over.....");
			}else{
				response.sendRedirect("http://test.bolink.club/zld/images/nocar.png");
			}
		}else {
			response.sendRedirect("http://test.bolink.club/zld/images/nocar.png");
		}
	}

}
