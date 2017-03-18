package com.zhenlaidian.db.localdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zhenlaidian.util.MyLog;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DaoForPrice {

	private LocalDBHelper dbhelper;

	public DaoForPrice(Context context) {
		dbhelper = new LocalDBHelper(context);
	}

//	 * @param priceList    select * from price_tb where comid=? and state=? and pay_type=? and car_type=? order by id desc   new Object[]{comId,0,0,0})
//	 * @param priceListTime  select * from price_tb where comid=? and state=? and pay_type=? and car_type=? order by id desc   new Object[]{comId,0,1,0}
	@SuppressWarnings({ "rawtypes" })
	/**
	 * 查询价格策略，用于计算订单价钱；
	 * @param comId
	 * @return
	 */
	public List<List<Map>> getPriceList(String comId){
		List<List<Map>> priceList = new ArrayList<List<Map>>();
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		Cursor cursor1 = null;
		Cursor cursor2 = null;
		db.beginTransaction();//开始事务
		try {
			cursor1= db.rawQuery("select * from price_tb where comid=? and state=? and pay_type=? and car_type=? order by id desc",new String[]{comId,"0","0","0"});
			if(cursor1.moveToFirst()) {
				priceList = getpriceMpa(cursor1,"1");
			}else {
				cursor2 = db.rawQuery("select * from price_tb where comid=? and state=? and pay_type=? and car_type=? order by id desc",new String[]{comId,"0","1","0"});
				if (cursor2.moveToFirst()) {
					priceList = getpriceMpa(cursor2,"2");
				}
			}
		    db.setTransactionSuccessful();//调用此方法会在执行到endTransaction() 时提交当前事务，如果不调用此方法会回滚事务
		} finally {
		    db.endTransaction();//由事务的标志决定是提交事务，还是回滚事务
		    db.close(); 
		    if (cursor1 != null) {
			  cursor1.close();
		    }
		    if (cursor2 != null) {
			    cursor2.close();
		    }
		} 
		
		return priceList;
	}
	
	@SuppressWarnings("rawtypes")
	public List<List<Map>> getpriceMpa(Cursor cursor, String mprice_type) {
		List<List<Map>> mLists = new ArrayList<List<Map>>();
		List<Map> mList = new ArrayList<Map>();
		MyLog.i("getpriceMpa", "查询游标的记录数：" + cursor.getCount());
		if (cursor.moveToFirst()) {// 判断游标是否为空
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.move(i);// 移动到指定记录
				Map<Object, Object> pricemap = new HashMap<>();
				pricemap.put("id", cursor.getLong(cursor.getColumnIndex("id")));
				pricemap.put("comid", cursor.getLong(cursor.getColumnIndex("comid")));
				pricemap.put("price", cursor.getDouble(cursor.getColumnIndex("price")));
				pricemap.put("state", cursor.getLong(cursor.getColumnIndex("state")));
				pricemap.put("unit", cursor.getInt(cursor.getColumnIndex("unit")));
				pricemap.put("pay_type", cursor.getInt(cursor.getColumnIndex("pay_type")));
				pricemap.put("create_time", cursor.getLong(cursor.getColumnIndex("create_time")));
				pricemap.put("b_time", cursor.getInt(cursor.getColumnIndex("b_time")));
				pricemap.put("e_time", cursor.getInt(cursor.getColumnIndex("e_time")));
				pricemap.put("is_sale", cursor.getInt(cursor.getColumnIndex("is_sale")));
				pricemap.put("first_times", cursor.getInt(cursor.getColumnIndex("first_times")));
				pricemap.put("fprice", cursor.getDouble(cursor.getColumnIndex("fprice")));
				pricemap.put("countless", cursor.getInt(cursor.getColumnIndex("countless")));
				pricemap.put("free_time", cursor.getInt(cursor.getColumnIndex("free_time")));
				pricemap.put("fpay_type", cursor.getInt(cursor.getColumnIndex("fpay_type")));
				pricemap.put("isnight", cursor.getInt(cursor.getColumnIndex("isnight")));
				pricemap.put("isedit", cursor.getInt(cursor.getColumnIndex("isedit")));
				pricemap.put("car_type", cursor.getInt(cursor.getColumnIndex("car_type")));
				pricemap.put("is_fulldaytime", cursor.getInt(cursor.getColumnIndex("is_fulldaytime")));
				pricemap.put("update_time", cursor.getLong(cursor.getColumnIndex("update_time")));
				pricemap.put("mprice_type", mprice_type);
				mList.add(pricemap);
			}
			if (mprice_type.equals("1")) {
				mLists.add(0, mList);
				mLists.add(1, new ArrayList<Map>());
			}else {
				mLists.add(1, mList);
				mLists.add(0, new ArrayList<Map>());
			}
		}
		return mLists;
	}
	
	/**
	 * 批量添加价格策略；
	 * @param list
	 * @return
	 */
	public boolean addMorePrice(List<Price_tb> list) {
		if (null == list || list.size() <= 0) {
			return false;
		}
		 SQLiteDatabase db = dbhelper.getWritableDatabase();
		try {
			db.delete("price_tb", null, null);//清空之前的价格表；
			String sql = "insert into " + "price_tb" + "(comid" + ","
					+ "price" + ","
					+ "state" + ","
					+ "unit" + ","
					+ "pay_type" + ","
					+ "create_time" + ","
					+ "b_time" + ","
					+ "e_time" + ","
					+ "is_sale " + ","
					+ "first_times" + "," 
					+ "fprice" + ","
					+ "countless " + "," 
					+ "free_time" + ","
					+ "fpay_type" + ","
					+ "isnight" + ","
					+ "isedit" + ","
					+ "car_type" + "," 
					+ "is_fulldaytime" + "," 
					+ "update_time"
					+ ") " + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			SQLiteStatement stat = db.compileStatement(sql);
			db.beginTransaction();
			for (Price_tb Price_tbinfo : list) {
				stat.bindString(1, Price_tbinfo.comid);
				stat.bindString(2, Price_tbinfo.price);
				stat.bindString(3, Price_tbinfo.state);
				stat.bindString(4, Price_tbinfo.unit);
				stat.bindString(5, Price_tbinfo.pay_type);
				stat.bindString(6, Price_tbinfo.create_time);
				stat.bindString(7, Price_tbinfo.b_time);
				stat.bindString(8, Price_tbinfo.e_time);
				stat.bindString(9, Price_tbinfo.is_sale);
				stat.bindString(10, Price_tbinfo.first_times);
				stat.bindString(11, Price_tbinfo.fprice);
				stat.bindString(12, Price_tbinfo.countless);
				stat.bindString(13, Price_tbinfo.free_time);
				stat.bindString(14, Price_tbinfo.fpay_type);
				stat.bindString(15, Price_tbinfo.isnight);
				stat.bindString(16, Price_tbinfo.isedit);
				stat.bindString(17, Price_tbinfo.car_type);
				stat.bindString(18, Price_tbinfo.is_fulldaytime);
				stat.bindString(19, Price_tbinfo.update_time);
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
