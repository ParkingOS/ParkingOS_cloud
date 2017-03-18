package com.zhenlaidian.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.umeng.analytics.MobclickAgent;
import com.zhenlaidian.R;
import com.zhenlaidian.service.BLEService;
import com.zhenlaidian.service.PullMsgService;
import com.zhenlaidian.util.MD5Utils;
import com.zhenlaidian.util.MyLog;

/**
 * 修改密码
 * 
 * @author zhangyunfei 2015年8月24日
 */
public class ChangePassWordActivity extends BaseActivity {

	private EditText et_old_password;
	private EditText et_new_password;
	private EditText et_ok_password;
	private Button bt_ok;
	private String username;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		setContentView(R.layout.change_password_activity);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
		initVeiw();
	}

	private void initVeiw() {
		username = getIntent().getStringExtra("uin");
		et_new_password = (EditText) findViewById(R.id.et_change_new_pass_word);
		et_old_password = (EditText) findViewById(R.id.et_change_old_pass_word);
		et_ok_password = (EditText) findViewById(R.id.et_change_ok_pass_word);
		bt_ok = (Button) findViewById(R.id.bt_change_pass_ok);
		bt_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (et_old_password.getText().toString().length() < 6 || et_new_password.getText().toString().length() < 6
						|| et_ok_password.getText().toString().length() < 6) {
					showTost("密码位数最少6位！");
				} else {
					if (et_new_password.getText().toString().equals(et_ok_password.getText().toString())) {
						try {
							String md5oldpassword = MD5Utils.MD5(MD5Utils.MD5(et_old_password.getText().toString().trim())
									+ "zldtingchebao201410092009");
							String md5newpassword = MD5Utils.MD5(MD5Utils.MD5(et_new_password.getText().toString().trim())
									+ "zldtingchebao201410092009");
							submit(md5oldpassword, md5newpassword);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							MyLog.w("LoginActivity", "MD5加密异常！");
							e.printStackTrace();
						}
					} else {
						showTost("两次输入新密码不一致！");
					}
				}
			}
		});

	}

	// http://127.0.0.1/zld/collectorlogin.do?username=1000005&action=editpass&oldpass=&newpass=；0
	// 失败 1 成功 -1 原密码错误！
	public void submit(String md5oldpassword, String md5newpassword) {
		String path = baseurl;
		String url = path + "collectorlogin.do?username=" + username + "&action=editpass&oldpass=" + md5oldpassword + "&newpass="
				+ md5newpassword;
		MyLog.w("修改密码--URL---->>>", url);
		final ProgressDialog dialog = ProgressDialog.show(this, "提交中...", "提交修改的密码...", true, true);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				super.callback(url, object, status);
				MyLog.i("修改密码返回的信息是---->>>", object);
				if (object != null && object != "") {
					dialog.dismiss();
					if (object.equals("0")) {
						showTost("密码修改失败！");
					} else if (object.equals("1")) {
						// NfcOrderDao ndd = new
						// NfcOrderDao(ChangePassWordActivity.this);
						// Log.e("TAG",
						// et_ok_password.getText().toString()+":"+username);
						// ndd.updatePassword(et_ok_password.getText().toString(),
						// username);
						showTost("密码修改成功！");
						ChangePassWordActivity.this.finish();
						// reLogin();
					} else if (object.equals("-1")) {
						showTost("原密码输入错误！");
					}
				} else {
					dialog.dismiss();
				}
			}
		});
	}

	// 修改密码成功,退出当前页面,重新登录
	public void reLogin() {
		// 清除当前记住的账号密码,以便登录新账号的时候自动登录
		SharedPreferences autologinss = ChangePassWordActivity.this.getSharedPreferences("autologin", Context.MODE_PRIVATE);
		Editor edit = autologinss.edit();
		edit.clear();
		edit.commit();
		// 关闭服务并退出本次登录以及启动下次程序
		Intent pullservice = new Intent(this, PullMsgService.class);
		stopService(pullservice);
		Intent bleservice = new Intent(this, BLEService.class);
		stopService(bleservice);
		Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		MyLog.w("LeaveActivity", "退出当前页面进入登录页面");
	}

	public void showTost(String info) {
		Toast.makeText(ChangePassWordActivity.this, info, 0).show();
	}

	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			ChangePassWordActivity.this.finish();
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
}
