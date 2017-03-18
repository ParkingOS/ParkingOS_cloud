package com.zhenlaidian.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zhenlaidian.bean.DBNfcOrder;
import com.zhenlaidian.util.MyLog;

/**
 * 本例实现SQLite数据库增加、删除、修改、模糊查询操作。这里不是最好的实现方法, 如想研究SQL如何封装，请详细查看SQLiteDatebase类.
 * 查看SQL语句：String sql = SQLiteQueryBuilder.buildQueryString(); 希望机友们多交流心得!
 */
public class NfcOrderDao {
	private SQLiteDatabase db;
	private SQLiteOpenHelper dbHelper;
	private Context context;
	private DBHelper mDBHelper;
	private String DATABASE_TABLE = "TCBTable";

	public NfcOrderDao(Context context) {
		this.context = context;
		mDBHelper = new DBHelper(context);// 获取SQLiteDatabase对象
		db = mDBHelper.getWritableDatabase();// 获取数据库对象
		MyLog.i("NfcOrderDao", "数据库连接成功");
	}

	// 插入新的订单到数据库
	public void insert(String name, String id, String pwd) {
		// 执行一句SQL语句
		// 第一个参数为SQL语句，第二个参数为SQL语句中占位符参数的值，参数值在数组中的顺序要和占位符的位置对应
		String sql = "insert into " + DATABASE_TABLE + "(u_username,u_userid,u_password) values(?,?,?)";
		db.execSQL(sql, new Object[] { name, id, pwd });
		MyLog.i("NfcOrderDao", "插入数据");
	}

	// 插入新的订单到数据库
	public void insertName() {
		// 获得数据库对象
		db.execSQL("INSERT INTO " + DATABASE_TABLE + "(u_username) VALUES(?)", new Object[] { "新增账号" });
		MyLog.i("NfcOrderDao", "插入数据name====新增账号");
	}

	// //查询所有
	public ArrayList<DBNfcOrder> queryAll() {
		ArrayList<DBNfcOrder> arrayList = new ArrayList<DBNfcOrder>();
		// 还是先得到数据库对象
		Cursor cursor = null;
		String sql = "select * from " + DATABASE_TABLE;
		cursor = db.rawQuery(sql, null);
		while (cursor.moveToNext()) {
			int tid = cursor.getInt(cursor.getColumnIndex("t_id"));
			String name = cursor.getString(cursor.getColumnIndex("u_username"));
			String uid = cursor.getString(cursor.getColumnIndex("u_userid"));
			String pwd = cursor.getString(cursor.getColumnIndex("u_password"));
			DBNfcOrder order = new DBNfcOrder();
			order.setT_id(tid);
			order.setUser_name(name);
			order.setUser_id(uid);
			order.setPassword(pwd);
			arrayList.add(order);
		}
		cursor.close();
		return arrayList;
	}

	// 查询数据库是否存在有当前要插入的账号,点击的是否是当前账号
	public boolean selectIfNum(String str) {
		boolean user_id = true;
		// 还是先得到数据库对象
		Cursor cursor = db.rawQuery("select * from " + DATABASE_TABLE + " where u_userid = ?", new String[] { str });
		int num = cursor.getCount();
		if (num > 0) {
			user_id = false;
		}
		cursor.close();
		return user_id;
	}

	// 查询数据库是否存在有当前要插入的账号,点击的是否是当前账号
	public String selectIfYourself(String name) {
		String user_id = null;
		// 还是先得到数据库对象
		Cursor cursor = db.rawQuery("select * from " + DATABASE_TABLE + " where u_username = ?", new String[] { name });
		while (cursor.moveToNext()) {
			user_id = cursor.getString(cursor.getColumnIndex("u_userid"));
		}
		cursor.close();
		return user_id;
	}

	// 返回账号和密码
	public DBNfcOrder getAcounts(String name) {
		DBNfcOrder order = null;
		// 还是先得到数据库对象
		Cursor cursor = db.rawQuery("select * from " + DATABASE_TABLE + " where u_username = ?", new String[] { name });
		while (cursor.moveToNext()) {
			order = new DBNfcOrder();
			order.setUser_id(cursor.getString(cursor.getColumnIndex("u_userid")));
			order.setPassword(cursor.getString(cursor.getColumnIndex("u_password")));
		}
		cursor.close();
		return order;
	}

	// 返回所有的姓名
	public List<String> selectName() {
		List<String> list = new ArrayList<String>();
		db = mDBHelper.getReadableDatabase();
		Cursor cursor = db.query(DATABASE_TABLE, new String[] { "u_username" }, null, null, null, null, null, null);
		int columIndex = cursor.getColumnIndex("u_username");
		MyLog.i("NfcOrderDao", "返回所有的姓名dddd" + columIndex);
		if (columIndex > -1) {
			list.add(cursor.getString(columIndex));
			MyLog.i("NfcOrderDao", "返回所有的姓名");
		}
		cursor.close();
		return list;
	}

	// 根据账号查姓名
	public String getAccountByName(String nid) {
		String aname = null;
		// 还是先得到数据库对象
		Cursor cursor = db.rawQuery("select * from " + DATABASE_TABLE + " where u_userid = ?", new String[] { nid });
		while (cursor.moveToNext()) {
			aname = cursor.getString(cursor.getColumnIndex("u_username"));
		}
		cursor.close();
		return aname;
	}

	// 修改名字
	public void updateUsername(String uname, String uid) {
		MyLog.i("NfcOrderDao", "开始修改数据" + uname + ":" + uid);
		// ContentValues updateValues = new ContentValues();
		// updateValues.put("u_username",uname);
		// db.update(DATABASE_TABLE, updateValues, "u_userid" +"="+ uid,null);
		// Log.e("NfcOrderDao","修改数据"+uname+":"+uid);
		String sql = "update " + DATABASE_TABLE + " set u_username = ? where u_userid = ?";
		db.execSQL(sql, new Object[] { uname, uid });
	}

	// 修改密码
	public void updatePassword(String password, String uid) {
		// Log.e("NfcOrderDao","开始修改数据"+password+":"+uid);
		ContentValues updateValues = new ContentValues();
		updateValues.put("u_password", password);
		String where = "u_userid" + "=" + uid;
		db.update(DATABASE_TABLE, updateValues, where, null);
		// Log.e("NfcOrderDao","修改数据"+password+":"+uid);
	}

	/**
	 * 关闭数据库
	 */
	public void close() {
		db.close();
	}

}
