package com.zhenlaidian.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	public DBHelper(Context context) {
		super(context, "tingchebaouser.db", null,1);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		//创建一个表
		String sql = "create table if not exists TCBTable(t_id integer primary key autoincrement,u_username varchar(10),u_userid varchar(10),u_password varchar(10))";
		db.execSQL(sql);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + "TCBTable");
		onCreate(db);
	}
}
