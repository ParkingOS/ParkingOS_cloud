package com.zld.impl;


import com.mongodb.*;
import com.zld.utils.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作MongoDb
 * @author Laoyao
 * @date 20131025
 */
@Service
public class MongoDbUtils {

	private Logger logger = Logger.getLogger(MongoDbUtils.class);

	public List<String> getParkPicUrls(Long uin,String dbName){
		List<String> result =new ArrayList<String>();
		DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
		DBCollection mdb = db.getCollection("parkuser_pics");
		DBCursor dbCursor = mdb.find(new BasicDBObject("uin", uin), new BasicDBObject("filename", true));
		while(dbCursor.hasNext()){
			DBObject dbObject = dbCursor.next();
			result.add(dbObject.get("filename")+"");
		}
		return result;
	}

	public List<String> getOrderPicUrls(String dbName,BasicDBObject condition){
		List<String> result =new ArrayList<String>();
		DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
		DBCollection mdb = db.getCollection(dbName);
		DBCursor dbCursor = mdb.find(condition);
		while(dbCursor.hasNext()){
			DBObject dbObject = dbCursor.next();
			result.add(dbObject.get("filename")+"");
		}
		return result;
	}

	public byte[] getParkPic(String id,String dbName){
		DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
		DBCollection collection = db.getCollection(dbName);
		BasicDBObject document = new BasicDBObject();
		document.put("filename", id);
		//document.put("uin", uin);
		DBObject obj = collection.findOne(document);
		if(obj == null){
			db = MongoDBFactory.getInstance().getMongoDBBuilder("zld");//
			collection = db.getCollection(dbName);
			document = new BasicDBObject();
			document.put("filename", id);
			//document.put("uin", uin);
			obj = collection.findOne(document);
		}
		if(obj == null){
			return null;
		}
		db.requestDone();
		return (byte[])obj.get("content");
	}

	/**
	 * add 20160106 by yao
	 * @param itype 0操作日志
	 * @param otype 0登录，1退出，2添加，3修改，4删除 5导出 6结算
	 * @param content
	 * @return
	 */
	public String saveLogs(HttpServletRequest request, Integer itype,Integer otype,String content){
		try {
			String ip = StringUtils.getIpAddr(request);
			Long uin = (Long)request.getSession().getAttribute("loginuin");
			Long comid = (Long)request.getSession().getAttribute("comid");
			Long cityid = (Long)request.getSession().getAttribute("cityid");
			Long groupid = (Long)request.getSession().getAttribute("groupid");

			DB mydb = MongoClientFactory.getInstance().getMongoDBBuilder("zld");
			mydb.requestStart();

			DBCollection collection = mydb.getCollection("zld_logs");
			//  DBCollection collection = mydb.getCollection("records_test");
			BasicDBObject object = new BasicDBObject();
			object.put("comId", comid);
			object.put("cityid", cityid);
			object.put("groupid", groupid);
			object.put("uin", uin);
			object.put("itype",  itype);
			object.put("otype",  otype);
			object.put("uri", request.getServletPath());
			object.put("content", content);
			object.put("time", System.currentTimeMillis()/1000);
			object.put("ip", ip);
			//开始事务
			mydb.requestStart();
			WriteResult result = collection.insert(object);
			//结束事务
			mydb.requestDone();
			return result.toString();
		} catch (Exception e) {
			logger.error("saveLogs", e);
		}
		return "";
	}
	/**
	 * 查询记录数
	 * @param dbName
	 * @param conditions
	 * @return
	 */
	public Long queryMongoDbCount(String dbName,BasicDBObject conditions){
		DB mydb = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
		DBCollection mdb = mydb.getCollection(dbName);
		Long count =0L;
		mydb.requestStart();
		if(conditions!=null&&!conditions.isEmpty()){
			count=mdb.count(conditions);
		}else {
			count=mdb.count();
		}
		mydb.requestDone();
		return count;
	}
	/**
	 * 分页查询
	 * @param dbName
	 * @param conditions
	 * @param sort
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public List<Map<String, Object>> queryMongoDbResult(String dbName,BasicDBObject conditions,BasicDBObject sort,int pageNum,int pageSize){
		DB mydb = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
		DBCollection mdb = mydb.getCollection(dbName);
		DBCursor dbCursor =null;
		mydb.requestStart();
		if(pageSize==0){//不分页
			if(conditions!=null&&!conditions.isEmpty()){
				dbCursor = mdb.find(conditions).sort(sort);  ;
			}else {
				dbCursor = mdb.find().sort(sort);  ;
			}
		}else {
			if(conditions!=null&&!conditions.isEmpty()){
				dbCursor = mdb.find(conditions).skip((pageNum - 1) * 10).sort(new BasicDBObject()).limit(pageSize).sort(sort);  ;
			}else {
				dbCursor = mdb.find().skip((pageNum - 1) * 10).sort(new BasicDBObject()).limit(pageSize).sort(sort);  ;
			}
		}
		mydb.requestDone();
		List<Map<String, Object>> retMaps = new ArrayList<Map<String, Object>>();
		while(dbCursor.hasNext()){
			Map<String, Object> map = parseRet(dbCursor.next());
			retMaps.add(map);
		}
		return retMaps;
	}
	/**
	 * 封装成Map
	 * @param dbObject
	 * @return
	 */
	private Map<String, Object> parseRet(DBObject dbObject){
		Map<String, Object> retMap= new HashMap<String, Object>();
		for(String key : dbObject.keySet()){
			retMap.put(key, dbObject.get(key));
		}
		return retMap;
	}

	/**
	 * 分页示例
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public List<Map<String, Object>> pageList(int page,int pageSize){
		DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
		DBCollection mdb = db.getCollection("parkuser_pics");

		DBCursor limit = mdb.find().skip((page - 1) * 10).sort(new BasicDBObject()).limit(pageSize);
		List<Map<String, Object>> retMaps = new ArrayList<Map<String, Object>>();
		while (limit.hasNext()) {
			Map<String, Object> map = parseRet(limit.next());
			retMaps.add(map);
		}
		return retMaps;
	}



	//查询示例
	public void query() {
		//查询所有
		//queryAll();
		/*DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
		DBCollection users = db.getCollection("parkuser_pics");
		//查询id = 4de73f7acd812d61b4626a77
		print("find id = 4de73f7acd812d61b4626a77: " + users.find(new BasicDBObject("_id", new ObjectId("4de73f7acd812d61b4626a77"))).toArray());

		//查询age = 24
		print("find age = 24: " + users.find(new BasicDBObject("age", 24)).toArray());

		//查询age >= 24
		print("find age >= 24: " + users.find(new BasicDBObject("age", new BasicDBObject("$gte", 24))).toArray());
		print("find age <= 24: " + users.find(new BasicDBObject("age", new BasicDBObject("$lte", 24))).toArray());

		print("查询age!=25：" + users.find(new BasicDBObject("age", new BasicDBObject("$ne", 25))).toArray());
		print("查询age in 25/26/27：" + users.find(new BasicDBObject("age", new BasicDBObject(QueryOperators.IN, new int[] { 25, 26, 27 }))).toArray());
		print("查询age not in 25/26/27：" + users.find(new BasicDBObject("age", new BasicDBObject(QueryOperators.NIN, new int[] { 25, 26, 27 }))).toArray());
		print("查询age exists 排序：" + users.find(new BasicDBObject("age", new BasicDBObject(QueryOperators.EXISTS, true))).toArray());

		print("只查询age属性：" + users.find(null, new BasicDBObject("age", true)).toArray());
		print("只查属性：" + users.find(null, new BasicDBObject("age", true), 0, 2).toArray());
		print("只查属性：" + users.find(null, new BasicDBObject("age", true), 0, 2, Bytes.QUERYOPTION_NOTIMEOUT).toArray());

		//只查询一条数据，多条去第一条
		print("findOne: " + users.findOne());
		print("findOne: " + users.findOne(new BasicDBObject("age", 26)));
		print("findOne: " + users.findOne(new BasicDBObject("age", 26), new BasicDBObject("name", true)));

		//查询修改、删除
		print("findAndRemove 查询age=25的数据，并且删除: " + users.findAndRemove(new BasicDBObject("age", 25)));

		//查询age=26的数据，并且修改name的值为Abc
		print("findAndModify: " + users.findAndModify(new BasicDBObject("age", 26), new BasicDBObject("name", "Abc")));
		print("findAndModify: " + users.findAndModify(
				new BasicDBObject("age", 28), //查询age=28的数据
				new BasicDBObject("name", true), //查询name属性
				new BasicDBObject("age", true), //按照age排序
				false, //是否删除，true表示删除
				new BasicDBObject("name", "Abc"), //修改的值，将name修改成Abc
				true,
				true));

		DBCursor cur = users.find();
		while (cur.hasNext()) {
			print(cur.next());
		}*/
	}
	//打印
	private void print (Object value){
		System.out.println(value.toString());
	}
}
