package com.zld.impl;


import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * 操作MongoDb
 * @author Laoyao
 * @date 20131025
 */
@Service
public class MongoDbUtils {
	
	private Logger logger = Logger.getLogger(MongoDbUtils.class);
	
	public List<String> getPicUrls(String dbName, BasicDBObject conditions){
		List<String> result =new ArrayList<String>();
		try {
			DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
			DBCollection mdb = db.getCollection(dbName);
			DBCursor dbCursor = mdb.find(conditions);
			if(dbCursor!=null&&dbCursor.size()==0&&dbName.equals("car_hd_pics")){
				 mdb = db.getCollection("car_pics");
				 dbCursor = mdb.find(conditions);
				 logger.error("mongodb>>>>>>>>>>>car_hd_pics表中没有，从car_pics表中查询:"+dbCursor);
			}
			while(dbCursor.hasNext()){
				DBObject dbObject = dbCursor.next();
				result.add(dbObject.get("filename") + "");
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return result;
	}

	public void getPicByFileName(String filename, String dbName, HttpServletResponse response){
		try {
			DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
			DBCollection collection = db.getCollection(dbName);
			BasicDBObject document = new BasicDBObject();
			document.put("filename", filename);
			DBObject obj = collection.findOne(document);
			if(obj == null&&dbName.equals("car_hd_pics")){
				//db = MongoDBFactory.getInstance().getMongoDBBuilder("zld");//
				collection = db.getCollection("car_pics");
//				document = new BasicDBObject();
//				document.put("filename", filename);
				obj = collection.findOne(document);
				logger.error("mongodb>>>>>>>>>>>car_hd_pics表中没有，从car_pics表中查询"+obj);
			}
			if(obj == null){
				return;
			}
			db.requestDone();
			byte [] content = (byte[])obj.get("content");
			response.setDateHeader("Expires", System.currentTimeMillis()+4*60*60*1000);
			//response.setStatus(httpc);
			Calendar c = Calendar.getInstance();
			c.set(1970, 1, 1, 1, 1, 1);
			response.setHeader("Last-Modified", c.getTime().toString());
			response.setContentLength(content.length);
			response.setContentType("image/jpeg");
			//System.err.println(content.length);
			OutputStream o = response.getOutputStream();
			o.write(content);
			o.flush();
			o.close();
			response.flushBuffer();
		} catch (Exception e) {
			logger.error(e);
		}
	}
	/**
	 * 取图片
	 * @param dbName
	 * @param conditions
	 * @return
	 */
	public byte[] getPictures(String dbName,BasicDBObject conditions){
		DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
		DBCollection collection = db.getCollection(dbName);
		DBObject obj = collection.findOne(conditions);
		if(obj == null){
			return null;
		}
		db.requestDone();
		return (byte[])obj.get("content");
	}
	
}
