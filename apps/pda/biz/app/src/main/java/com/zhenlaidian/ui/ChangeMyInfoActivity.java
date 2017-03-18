package com.zhenlaidian.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuCompat;
import android.support.v7.app.ActionBar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.zhenlaidian.R;
import com.zhenlaidian.db.NfcOrderDao;
import com.zhenlaidian.util.CheckUtils;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

/**
 * 修改手机号||姓名
 * 
 * @author zhangyunfei 2015年8月21日
 */
public class ChangeMyInfoActivity extends BaseActivity {

	private EditText et_info;
	private TextView tv_change_hint;
	private ActionBar actionBar;
	private String titleinfo = null;
	private String uin;// 账户名
	private String phonenum;// 电话号码
	private NfcOrderDao ndd;
	private String parameter = null;// 所修改的内容

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		setContentView(R.layout.change_my_info_activity);
		titleinfo = getIntent().getStringExtra("changeinfo");
		tv_change_hint = (TextView) findViewById(R.id.tv_change_info_hint_name);
		et_info = (EditText) findViewById(R.id.et_change_info_info);

		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		if (titleinfo != null && titleinfo.equals("mobile")) {
			phonenum = getIntent().getStringExtra("phonenum");
			actionBar.setTitle("修改手机号");
			tv_change_hint.setText("手机号：");
			et_info.setInputType(InputType.TYPE_CLASS_PHONE);
		} else if (titleinfo != null) {
			actionBar.setTitle("修改姓名");
			tv_change_hint.setText("姓名：");
			et_info.setInputType(InputType.TYPE_CLASS_TEXT);
		}
		actionBar.show();
		SharedPreferences pfs = getSharedPreferences("autologin", Context.MODE_PRIVATE);
		uin = pfs.getString("account", "");
	}

	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.change_my_info, menu);
		MenuCompat.setShowAsAction(menu.findItem(R.id.submit), MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			ChangeMyInfoActivity.this.finish();
			return true;
		case R.id.submit:
			if (et_info.getText().toString().trim() == null || et_info.getText().toString().trim().equals("")) {
				if (actionBar.getTitle().equals("修改手机号")) {
					Toast.makeText(this, "您输入的手机号有误", Toast.LENGTH_LONG).show();
				}
				if (actionBar.getTitle().equals("修改姓名")) {
					Toast.makeText(this, "您输入的姓名有误", Toast.LENGTH_LONG).show();
				}
				return false;
			}
			if (titleinfo.endsWith("name")) {
				String check = "^[\u4e00-\u9fa5]*$";
				Pattern regex = Pattern.compile(check);
				Matcher matcher = regex.matcher(et_info.getText().toString().trim());
				boolean isMatched = matcher.matches();
				if (isMatched) {
					try {
						Submit("name");
					} catch (UnsupportedEncodingException e) {
						Toast.makeText(ChangeMyInfoActivity.this, "中文转码异常！", 0).show();
						e.printStackTrace();
					}
				} else {
					Toast.makeText(ChangeMyInfoActivity.this, "请输入汉字姓名！", 0).show();
				}

			} else {
				if (et_info.getText().toString().trim().equals(phonenum)) {
					Toast.makeText(ChangeMyInfoActivity.this, "对不起，该手机号已经存在！", 0).show();
					return false;
				}
				boolean isMatched = CheckUtils.MobileChecked(et_info.getText().toString().trim());
				if (isMatched) {
					try {
						Submit("mobile");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				} else {
					Toast.makeText(ChangeMyInfoActivity.this, "输入手机号有误！", 0).show();
				}
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// http://192.168.199.240/zld/collectorlogin.do?action=editname&name=1020005&uin=1000005
	// http://192.168.199.240/zld/collectorlogin.do?action=editphone&mobile=18003005000&uin=1000005

	private void Submit(String info) throws UnsupportedEncodingException {
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "请检查网络！", 0).show();
			return;
		}
		String path = baseurl;
		String url = "";
		parameter = URLEncoder.encode(et_info.getText().toString().trim(), "utf-8");
		if (info.equals("name")) {
			url = path + "collectorlogin.do?action=editname&name=" + URLEncoder.encode(parameter, "utf-8") + "&uin=" + uin;
			// ndd = new NfcOrderDao(ChangeMyInfoActivity.this);
		} else {
			url = path + "collectorlogin.do?action=editphone&mobile=" + et_info.getText().toString().trim() + "&uin=" + uin;
		}
		MyLog.w("com.tingchebao", "修改我的名字或手机号--URL-->>>" + url);
		final ProgressDialog dialog = ProgressDialog.show(this, "访问中...", "提交修改信息...", true, true);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				super.callback(url, object, status);
				MyLog.i("ChangeMyInfoActivity", "修改结果为：" + object);
				if (object != null && object.equals("1")) {
					dialog.dismiss();
					String s = new String();
					s = et_info.getText().toString().trim();
					MyLog.i("ChangeMyInfoActivity", "更新姓名!");
					// ndd.updateUsername(s, uin);
					Toast.makeText(ChangeMyInfoActivity.this, "修改成功", 0).show();
					// Intent data = new Intent();
					// data.putExtra("myinfo", "ok");
					// setResult(20,data);
					// Intent intent = new Intent(ChangeMyInfoActivity.this,
					// MyHomeActivity.class);
					// startActivity(intent);
					ChangeMyInfoActivity.this.finish();
				} else {
					dialog.dismiss();
					Toast.makeText(ChangeMyInfoActivity.this, "号码已存在", 0).show();
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// ndd.close();
	}
}
