package com.zhenlaidian.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.umeng.analytics.MobclickAgent;
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

/**
 * 所有Activity的父类。
 * 
 * @author zhangyunfei 2015年8月24日
 */
public class BaseActivity extends ActionBarActivity {

	public static String baseurl;
	public static String mserver;
	public static String token;
	public static String parkname;// 车场名子
	public static String imei;// 手机串号
	public static String comid;// 车场编号
	public static String useraccount;// 用户账号
	public Context context;
	public ActionBar actionBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
//		Constant.listactivity.add(this);
		actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		sharedPreferences = CommontUtils.getSharedPreferences(BaseActivity.this);
		MyLog.d("BaseActivity", "onCreate");
		baseurl = Config.getUrl(this);
		mserver = Config.getMserver(this);
		token = SharedPreferencesUtils.getIntance(this).getToken();
		useraccount = SharedPreferencesUtils.getIntance(this).getAccount();
		comid = SharedPreferencesUtils.getIntance(this).getComid();
		parkname = SharedPreferencesUtils.getIntance(this).getParkname();
//		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//		imei = telephonyManager.getDeviceId();
		imei = CommontUtils.GetHardWareAddress(this);

	}
	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);

	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	// 与主页LeaveActivity交互的接口
	public interface MsgToMainListener {
		public void onSendMsg(Message msg);
	}
	private SharedPreferences sharedPreferences;
	/**
	 * 把boolean类型的变量存进xml
	 *
	 * @param s
	 *            键
	 * @param boolean1
	 *            值
	 */
	public void putBooleanToPreference(String s, Boolean boolean1) {
		android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
		boolean flag = boolean1.booleanValue();
		editor.putBoolean(s, flag).commit();
	}

	/**
	 * 把string类型的变量存进xml
	 *
	 * @param s
	 *            键
	 * @param s1
	 *            值
	 */
	public void putStringToPreference(String s, String s1) {
		sharedPreferences.edit().putString(s, s1).commit();
	}

	public void putIntToPreference(String s, int s1) {
		sharedPreferences.edit().putInt(s, s1).commit();
	}

	/**
	 * 获取xml中键值是s对应的值(字符串)
	 *
	 * @param s
	 *            键
	 * @return value
	 */
	public String getStringFromPreference(String s) {
		return sharedPreferences.getString(s, "");
	}

	/**
	 * 获取xml中键值是s对应的值(字符串)
	 *
	 * @param s
	 *            键
	 * @param s1
	 *            默认值
	 * @return value
	 */
	public String getStringFromPreference(String s, String s1) {
		return sharedPreferences.getString(s, s1);
	}

	/**
	 * 获取xml中键值是s对应的值(boolean型值)
	 *
	 * @param s
	 *            键
	 * @return
	 */
	public boolean getBooleanFromPreference(String s) {

		return sharedPreferences.getBoolean(s, false);
	}

	/**
	 * 获取xml中键值是s对应的值(boolean型值)
	 *
	 * @param s
	 *            键
	 * @param flag
	 *            默认值
	 * @return value
	 */
	public boolean getBooleanFromPreference(String s, boolean flag) {
		return sharedPreferences.getBoolean(s, flag);
	}

	public int getIntFromPreference(String s, int flag) {
		return sharedPreferences.getInt(s, flag);
	}

	/**
	 *
	 * @param s
	 *            键
	 * @param flag
	 *            默认值
	 * @return
	 */
	public long getLongFromPreference(String s, long flag) {
		return sharedPreferences.getLong(s, flag);
	}

	public void putLongToPreference(String s, long s1) {
		sharedPreferences.edit().putLong(s, s1).commit();
	}


}
