package com.zld.struts.request;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.zld.AjaxUtil;
import com.zld.impl.MongoClientFactory;
import com.zld.impl.MongoDbUtils;
import com.zld.sdk.doupload.impl.DoUploadImpl;
import com.zld.service.DataBaseService;
import com.zld.utils.Check;
import com.zld.utils.RequestUtil;
import com.zld.utils.TimeTools;

public class CarPicsUploadAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	@Autowired
	private DoUploadImpl doUpload;

	private Logger logger = Logger.getLogger(CarPicsUploadAction.class);

	/*
	 * 上传照牌图片
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		if (action.equals("uploadpic")) {
			Long orderid = RequestUtil.getLong(request, "orderid", -1L);
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			// type区分是入口还是出口，入口为0，出口为1
			Long type = RequestUtil.getLong(request, "type", -1L);
			if (comid == -1 || orderid == -1 || type == -1) {
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			String lefttop = RequestUtil.processParams(request, "lefttop");
			String rightbottom = RequestUtil.processParams(request,
					"rightbottom");
			String width = RequestUtil.processParams(request, "width");
			String height = RequestUtil.processParams(request, "height");
			Long preorderid = RequestUtil.getLong(request, "preorderid", -1L);// 未结算的订单id
			String result = uploadCarPics2Mongodb(request, comid, orderid,
					lefttop, rightbottom, width, height, type);
			// 把该照片作为未结算订单的出口照片
			if (preorderid != -1) {
				result = uploadCarPics2Mongodb(request, comid, preorderid,
						lefttop, rightbottom, width, height, 1L);
			}
			AjaxUtil.ajaxOutput(response, result);
			// http://127.0.0.1/zld/carpicsup.do?action=uploadpic&comid=0&orderid=238705&type=0
		} else if (action.equals("downloadpic")) {
			Long orderid = RequestUtil.getLong(request, "orderid", -1L);
			// type区分是入口还是出口，入口为0，出口为1
			Long type = RequestUtil.getLong(request, "type", -1L);
			if (orderid == -1 || type == -1) {
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			downloadCarPics(orderid, type, request, response);
			// http://118.192.88.90:8080/zld/carpicsup.do?action=downloadpic&comid=0&orderid=238747&type=0
		} else if (action.equals("downloadlogpic")) {
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			if (comid == -1) {
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Map map = daService.getMap(
					"select * from com_info_tb where id = ? ",
					new Object[] { comid });
			if (map != null && map.get("chanid") != null) {
				Long chanid = (Long) map.get("chanid");
				Map LogoMap = daService.getMap(
						"select * from logo_tb where type = ? and orgid = ?",
						new Object[] { 0, chanid });
				if (LogoMap != null && LogoMap.get("url_sec") != null) {
					String logourl = LogoMap.get("url_sec") + "";
					String fname = "停车宝";
					if (LogoMap.get("name") != null) {
						fname = LogoMap.get("name") + "";
					}
					downloadLOGOPics(logourl, fname, request, response);
				}

			}
			// http://118.192.88.90:8080/zld/carpicsup.do?action=downloadpic&comid=0&orderid=238747&type=0
		} else if (action.equals("getpicbyname")) {
			String filename = RequestUtil.processParams(request, "filename");
			if ("".equals(filename)) {
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			mongoDbUtils.getPicByFileName(filename, "car_inout_pics", response);
		} else if (action.equals("receivepic")) {
			/*
			 * 通过流的方式读取request请求对象中的数据
			 */
			logger.error(">>>>>>>>>>>>>>调用上传图片的方法...........start");
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
			try{
				jsonObj = JSONObject.fromObject(str);
			}catch(Exception e){
				AjaxUtil.ajaxOutput(response, "json数据格式不正确");
			}
			String token = "";
			if(jsonObj.containsKey("token")){
				token = jsonObj.getString("token");
			}
			String serviceName = "";
			if(jsonObj.containsKey("serviceName")){
				serviceName = jsonObj.getString("service_name");
			}
			String data = "";
			if(jsonObj.containsKey("data")){
				data = jsonObj.getString("data");
			}
			// 根据token查询出对应的车场id
			String comidNew = "";
			Map<String, Object> userInfoMap = daService.getMap(
					"select * from park_token_tb where token=? ",
					new Object[] { token });
			if (userInfoMap != null && !userInfoMap.isEmpty()) {
				comidNew = (String) userInfoMap.get("park_id");
			}else{
				logger.error(">>>>>>>>>>>>>>>>>token错误，未找到对应的车场信息："+token);
				return null;
			}
			logger.error(">>>>>>>>>>>>>>>>收到图片数据内容长度........" + data.length());
			String result =handleSDKuploadPic(data, comidNew); //doUpload.uploadCarpic(comidNew, data);
			logger.error(">>>>>>>>>>>>>>>>>>>>上传图片执行结果：uploadCarpic:" + result);

		}
		return null;
	}

	private String uploadCarPics2Mongodb(HttpServletRequest request,
			Long comid, Long orderid, String lefttop, String rightbottom,
			String width, String height, Long type) throws Exception {
		long currentnum = RequestUtil.getLong(request, "currentnum", -1L);
		// logger.error("begin upload picture....");
		Map<String, String> extMap = new HashMap<String, String>();
		extMap.put(".jpg", "image/jpeg");
		extMap.put(".jpeg", "image/jpeg");
		extMap.put(".png", "image/png");
		extMap.put(".gif", "image/gif");
		request.setCharacterEncoding("UTF-8"); // 设置处理请求参数的编码格式
		DiskFileItemFactory factory = new DiskFileItemFactory(); // 建立FileItemFactory对象
		factory.setSizeThreshold(16 * 4096 * 1024);
		ServletFileUpload upload = new ServletFileUpload(factory);
		// 分析请求，并得到上传文件的FileItem对象
		upload.setSizeMax(16 * 4096 * 1024);
		List<FileItem> items = null;
		try {
			items = upload.parseRequest(request);
		} catch (FileUploadException e) {
			e.printStackTrace();
			return "-1";
		}
		String filename = ""; // 上传文件保存到服务器的文件名
		InputStream is = null; // 当前上传文件的InputStream对象
		// 循环处理上传文件
		String comId = "";
		String orderId = "";
		for (FileItem item : items) {
			// 处理普通的表单域
			if (item.isFormField()) {
				if (item.getFieldName().equals("comid")) {
					if (!item.getString().equals(""))
						comId = item.getString("UTF-8");
				} else if (item.getFieldName().equals("orderid")) {
					if (!item.getString().equals("")) {
						orderId = item.getString("UTF-8");
					}
				}

			} else if (item.getName() != null && !item.getName().equals("")) {// 处理上传文件
				// 从客户端发送过来的上传文件路径中截取文件名
				// logger.error(item.getName());
				filename = item.getName().substring(
						item.getName().lastIndexOf("\\") + 1);
				is = item.getInputStream(); // 得到上传文件的InputStream对象

			}
		}
		if (comid == null && (comId.equals("") || !Check.isLong(comId)))
			return "-1";
		if (orderid == null && (orderId.equals("") || !Check.isLong(orderId))) {
			return "-1";
		}
		String file_ext = filename.substring(filename.lastIndexOf("."))
				.toLowerCase();// 扩展名
		String picurl = comid + "_" + orderid + "_"
				+ System.currentTimeMillis() / 1000 + file_ext;
		BufferedInputStream in = null;
		ByteArrayOutputStream byteout = null;
		try {
			in = new BufferedInputStream(is);
			byteout = new ByteArrayOutputStream(1024);

			byte[] temp = new byte[1024];
			int bytesize = 0;
			while ((bytesize = in.read(temp)) != -1) {
				byteout.write(temp, 0, bytesize);
			}

			byte[] content = byteout.toByteArray();
			DB mydb = MongoClientFactory.getInstance().getMongoDBBuilder("zld");
			mydb.requestStart();
			DBCollection collection = mydb.getCollection("car_inout_pics");
			// DBCollection collection = mydb.getCollection("records_test");
			BasicDBObject document = new BasicDBObject();
			document.put("comid", comid);
			document.put("orderid", orderid);
			document.put("gate", type);
			document.put("ctime", System.currentTimeMillis() / 1000);
			document.put("type", extMap.get(file_ext));
			document.put("content", content);
			document.put("filename", picurl);
			document.put("currentnum", currentnum);
			// 开始事务
			mydb.requestStart();
			collection.insert(document);
			// 结束事务
			mydb.requestDone();
			in.close();
			is.close();
			byteout.close();
			List<Object> params = new ArrayList<Object>();
			params.add(orderid);
			params.add(type);
			String sql = "select count(*) from car_picturs_tb where orderid=? and pictype=?";
			Long count = 0L;
			count = daService.getCount(sql, params);
			if (count > 0) {
				sql = "update car_picturs_tb set create_time=?,lefttop=?,rightbottom=?,width=?,height=? where orderid=? and pictype=?";
				daService.update(sql, new Object[] {
						System.currentTimeMillis() / 1000, lefttop,
						rightbottom, width, height, orderid, type });
			} else {
				sql = "insert into car_picturs_tb(orderid,pictype,create_time,lefttop,rightbottom,width,height) values(?,?,?,?,?,?,?)";
				daService.update(
						sql,
						new Object[] { orderid, type,
								System.currentTimeMillis() / 1000, lefttop,
								rightbottom, width, height });
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "-1";
		} finally {
			if (in != null)
				in.close();
			if (byteout != null)
				byteout.close();
			if (is != null)
				is.close();
		}

		return "1";
	}

	private void downloadCarPics(Long orderid, Long type,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		logger.error("download from mongodb....");
		System.err.println("downloadCarPics from mongodb file:orderid="
				+ orderid + "type=" + type);
		if (orderid != null && type != null) {
			DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
			DBCollection collection = db.getCollection("car_inout_pics");
			BasicDBObject document = new BasicDBObject();
			document.put("orderid", orderid);
			document.put("gate", type);
			// 按生成时间查最近的数据
			DBObject obj = collection.findOne(document);
			if (obj == null) {
				collection = db.getCollection("car_hd_pics");
				obj = collection.findOne(document);
				logger.error("mongodb>>>>>>>>>>>car_inout_pics表中没有，从car_hd_pics表中查询"
						+ obj);
			}
			if (obj == null) {
				AjaxUtil.ajaxOutput(response, "");
				logger.error("取图片错误.....");
				return;
			}
			byte[] content = (byte[]) obj.get("content");
			logger.error("取图片成功.....大小:" + content.length);
			db.requestDone();
			response.setDateHeader("Expires", System.currentTimeMillis() + 12
					* 60 * 60 * 1000);
			response.setContentLength(content.length);
			response.setContentType("image/jpeg");
			OutputStream o = response.getOutputStream();
			o.write(content);
			o.flush();
			o.close();
			System.out.println("mongdb over.....");
		} else {
			response.sendRedirect("http://sysimages.tq.cn/images/webchat_101001/common/kefu.png");
		}
	}

	private void downloadLOGOPics(String url, String fname,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		logger.error("downloadLOGOPics from mongodb....");
		System.err.println("downloadlogoPics from mongodb file:url=" + url);
		if (url != null) {
			DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
			DBCollection collection = db.getCollection("logo_pics");
			BasicDBObject document = new BasicDBObject();
			document.put("filename", url);
			// 按生成时间查最近的数据
			DBObject obj = collection.findOne(document);
			if (obj == null) {
				AjaxUtil.ajaxOutput(response, "");
				logger.error("取图片错误.....");
				return;
			}
			byte[] content = (byte[]) obj.get("content");
			logger.error("取图片成功.....大小:" + content.length);
			db.requestDone();
			response.setDateHeader("Expires", System.currentTimeMillis() + 12
					* 60 * 60 * 1000);
			response.setContentLength(content.length);
			response.setContentType("image/jpeg");
			response.setHeader("Content-disposition", "attachment; filename="
					+ URLEncoder.encode(fname, "UTF-8") + ".jpg");

			System.out.println(fname + "," + URLEncoder.encode(fname, "UTF-8"));
			OutputStream o = response.getOutputStream();
			o.write(content);
			o.flush();
			o.close();
			System.out.println("mongdb over.....");
		} else {
			// response.sendRedirect("http://sysimages.tq.cn/images/webchat_101001/common/kefu.png");
		}
	}

	/**
	 * 处理SDK图片上传
	 * 
	 * @param data
	 * @param parkId
	 * @return
	 */
	private String handleSDKuploadPic(String data, String parkId) throws Exception {
		JSONObject jsonObj = JSONObject.fromObject(data);
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
		if (pictureSource.equals("order")) {
			if (jsonObj.containsKey("order_id")) {
				orderId = jsonObj.getString("order_id");
			} else {
				return "{\"state\":0,\"errmsg\":\"图片上传失败，缺少订单编号！\"}";
			}
			dbName = "car_pic_" + yearMonth;
		} else if (pictureSource.equals("liftrod")) {
			if (jsonObj.containsKey("liftrod_id")) {
				liftRodId = jsonObj.getString("liftrod_id");
			} else {
				return "{\"state\":0,\"errmsg\":\"图片上传失败，缺少抬杆记录编号！\"}";
			}
			dbName = "liftrod_pic_" + yearMonth;
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
		byte[] picture = Base64.decode(content);
		DB mydb = MongoClientFactory.getInstance().getMongoDBBuilder("zld");
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
		}else {
			document.put("liftrodid", liftRodId);
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
			Map mapOrderPic = daService.getMap("select * from carpic_tb where order_id=? and comid=?", new Object[]{orderId,parkId});
			if(mapOrderPic != null && !mapOrderPic.isEmpty()){
				int updateCarpicOrder = daService.update("update carpic_tb set carpic_table_name=? where comid=? and order_id=?", new Object[]{dbName,parkId,orderId});
				logger.error(">>>>>>>>>>>>>>>>>>更新图片资源地址结果......."+updateCarpicOrder+">>>>>>>>>>订单号:"+orderId);
			}else{
				Long id = daService.getLong(
						"SELECT nextval('seq_carpic_tb'::REGCLASS) AS newid",null);
				int addCarpicOrder = daService.update("insert into carpic_tb (id,order_id,comid,carpic_table_name) values(?,?,?,?)", new Object[]{id,orderId,parkId,dbName});
				logger.error(">>>>>>>>>>>>>>>>添加图片资源地址的结果........."+addCarpicOrder+">>>>>>>>>>订单号:"+orderId);
			}
//			int updateOrderTable = daService.update("update order_tb set carpic_table_name=? where comid=? and order_id_local=?",new Object[]{dbName,Long.valueOf(parkId),orderId});
//			logger.error(">>>>>>>>>>存入数据库中的结果"+updateOrderTable);
		}else if(pictureSource.equals("liftrod")){
			//将mongodb中存取图片所对应的表名写入到数据库中 
			//先查询carpic_tb表中是否已经存在该记录，存在则更新，不存在则新添加一条
			Map mapLiftrodPic = daService.getMap("select * from carpic_tb where liftrod_id=? and comid=?", new Object[]{liftRodId,parkId});
			if(mapLiftrodPic != null && !mapLiftrodPic.isEmpty()){
				int updateCarpicLiftrod = daService.update("update carpic_tb set liftpic_table_name=? where comid=? and liftrod_id=?", new Object[]{dbName,parkId,liftRodId});
				logger.error(">>>>>>>>>>>>>>>>>>更新图片资源地址结果......."+updateCarpicLiftrod+">>>>>>>>>>抬杆记录编号:"+liftRodId);
			}else{
				Long id = daService.getLong(
						"SELECT nextval('seq_carpic_tb'::REGCLASS) AS newid",null);
				int addCarpicLiftrod = daService.update("insert into carpic_tb (id,liftrod_id,comid,liftpic_table_name) values(?,?,?,?)", new Object[]{id,liftRodId,parkId,dbName});
				logger.error(">>>>>>>>>>>>>>>>添加图片资源地址的结果........."+addCarpicLiftrod+">>>>>>>>>>抬杆记录编号:"+liftRodId);
			}
//			int updateLiftrodTable = daService.update("update lift_rod_tb set liftpic_table_name=? where comid=? and liftrod_id=?",new Object[]{dbName,Long.valueOf(parkId),liftRodId});
//			logger.error(">>>>>>>>>>存入数据库中的结果"+updateLiftrodTable);
		}
		return "{\"state\":1,\"errmsg\":\"上传成功！\"}";
	}
}
