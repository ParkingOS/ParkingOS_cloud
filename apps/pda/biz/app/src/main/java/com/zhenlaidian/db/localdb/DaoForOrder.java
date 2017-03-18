package com.zhenlaidian.db.localdb;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

public class DaoForOrder {
	// 查询语句：select * from 表名 where 条件子句 group by 分组字句 having ... order by 排序子句
	// 如：
	// select * from person
	// select * from person order by id desc
	// select name from person group by name having count(*)>1
	// 分页SQL与mysql类似，下面SQL语句获取5条记录，跳过前面3条记录
	// select * from Account limit 5 offset 3 或者 select * from Account limit 3,5
	// 插入语句：insert into 表名(字段列表) values(值列表)。如： insert into person(name, age)
	// values(‘传智’,3)
	// 更新语句：update 表名 set 字段名=值 where 条件子句。如：update person set name=‘传智‘ where
	// id=10
	// 删除语句：delete from 表名 where 条件子句。如：delete from person where id=10
	private static LocalDBHelper dbhelper;
	private static String DATABASE_TABLE = "order_tb";

	public DaoForOrder(Context context) {
		dbhelper = new LocalDBHelper(context);
	}
	
	/**
	 * 添加订单记录；
	 * 
	 * @param order
	 */
	public void addOrder(Order_tb order) {
		 SQLiteDatabase db = dbhelper.getWritableDatabase();
		try {
			if (db != null) {
				ContentValues cv = new ContentValues();
				cv.put("create_time", order.create_time);
				cv.put("comid", order.comid);
				cv.put("uin", order.uin);
				cv.put("total", order.total);
				cv.put("state", order.state);
				cv.put("end_time", order.end_time);
				cv.put("auto_pay", order.auto_pay);
				cv.put("pay_type", order.pay_type);
				cv.put("nfc_uuid", order.nfc_uuid);
				cv.put("c_type", order.c_type);
				cv.put("uid", order.uid);
				cv.put("car_number", order.car_number);
				cv.put("imei", order.imei);
				cv.put("pid", order.pid);
				cv.put("car_type", order.car_type);
				cv.put("pre_state", order.pre_state);
				cv.put("in_passid", order.in_passid);
				cv.put("out_passid", order.out_passid);
				db.insert(DATABASE_TABLE, null, cv);
			} 
		} finally{
		  db.close(); 
		}
	}

	/**
	 * 批量添加订单；
	 * @param list
	 * @return
	 */
	public boolean addMoreOrder(List<Order_tb> list) {
		if (null == list || list.size() <= 0) {
			return false;
		}
		 SQLiteDatabase db = dbhelper.getWritableDatabase();
		try {
			String sql = "insert into " + DATABASE_TABLE + "(create_time" + ","// 创建时间
					+ "comid" + ","// 车场编号
					+ "uin" + ","// 车主账号
					+ "total" + ","// 价格
					+ "state" + ","// 状态
					+ "end_time" + ","// 结束时间
					+ "auto_pay" + ","// -- 自动结算，0：否，1：是
					+ "pay_type" + ","// -- 0:帐户支付,1:现金支付,2:手机支付 3月卡
					+ "nfc_uuid " + ","//
					+ "c_type" + "," // -- 0:NFC,1:IBeacon,2:照牌 3通道照牌 4直付 5月卡用户
					+ "uid" + ","// -- 收费员帐号
					+ "car_number " + "," // -- 车牌
					+ "imei" + ","// -- 手机串号
					+ "pid" + ","// 计费方式：0按时(0.5/15分钟)，1按次（12小时内10元,前1/30min，后每小时1元）
					+ "car_type" + ","// -- 0：通用，1：小车，2：大车
					+ "pre_state" + ","// -- 预支付状态 0 无，1预支付中，2等待车主支付完成
					+ "in_passid" + "," // -- 进口通道id
					+ "out_passid"// -- 出口通道id
					+ ") " + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			SQLiteStatement stat = db.compileStatement(sql);
			db.beginTransaction();
			for (Order_tb Order_tbinfo : list) {
				stat.bindString(1, Order_tbinfo.create_time);
				stat.bindString(2, Order_tbinfo.comid);
				stat.bindString(3, Order_tbinfo.uin);
				stat.bindString(4, Order_tbinfo.total);
				stat.bindString(5, Order_tbinfo.state);
				stat.bindString(6, Order_tbinfo.end_time);
				stat.bindString(7, Order_tbinfo.auto_pay);
				stat.bindString(8, Order_tbinfo.pay_type);
				stat.bindString(9, Order_tbinfo.nfc_uuid);
				stat.bindString(10, Order_tbinfo.c_type);
				stat.bindString(11, Order_tbinfo.uid);
				stat.bindString(12, Order_tbinfo.car_number);
				stat.bindString(13, Order_tbinfo.imei);
				stat.bindString(14, Order_tbinfo.pid);
				stat.bindString(15, Order_tbinfo.car_type);
				stat.bindString(16, Order_tbinfo.pre_state);
				stat.bindString(17, Order_tbinfo.in_passid);
				stat.bindString(18, Order_tbinfo.out_passid);
				long result = stat.executeInsert();
				if (result < 0) {
					return false;
				}
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (null != db) {
					db.endTransaction();
					db.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * 删除一条订单记录；
	 * @param type 查询类型 1 nfc卡号,2车牌号
	 * @param value
	 */
	public void deleteOrder(String type, String value) {
		if (TextUtils.isEmpty(type) || TextUtils.isEmpty(value)) {
			return;
		}
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		String deletesql;
		if ("1".equals(type)) {
			deletesql = "delete from " + DATABASE_TABLE + " where nfc_uuid=" + "'"+value+"'";
		} else {
			deletesql = "delete from " + DATABASE_TABLE + " where car_number=" + "'"+value+"'";
		}
		try {
			if (db != null) {
				db.execSQL(deletesql);
			} 
		} finally {
		  db.close(); 
		}
	}
	
	/**
	 * 删除一批订单记录by订单号
	 * @param list
	 */
	public void deleteMoreOrder(List<String> list){
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		db.beginTransaction();//开始事务
		try {
			for (int i = 0; i < list.size(); i++) {
				db.execSQL("delete from " + DATABASE_TABLE + " where id =" + list.get(i));
			}
		    db.setTransactionSuccessful();//调用此方法会在执行到endTransaction() 时提交当前事务，如果不调用此方法会回滚事务
		} finally {
		    db.endTransaction();//由事务的标志决定是提交事务，还是回滚事务
		    db.close(); 
		} 
	}
	
	/**
	 * 根据nfc号或车牌号查询定单时间
	 * @param type 查询类型 1 nfc卡号,2车牌号 3,订单编号；
	 * @param value
	 */
	public Long queryOrderTime(String type, String value){
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		String create_timesql;
		Long create_time = null;
		if ("1".equals(type)) {
			create_timesql = "select create_time from order_tb where nfc_uuid = ?";
		} else if ("2".equals(type)) {
			create_timesql = "select create_time from order_tb where car_number = ?";
		}else {
			create_timesql = "select create_time from order_tb where id = ?";
		}
		db.beginTransaction();
		try {
			Cursor rawQuery = db.rawQuery(create_timesql, new String[]{value} );
			if (rawQuery.moveToFirst()) {
				create_time = rawQuery.getLong(rawQuery.getColumnIndex("create_time"));
			}
		    db.setTransactionSuccessful();
		} finally {
		    db.endTransaction();
		    db.close(); 
		} 
		return create_time;
	}
}
