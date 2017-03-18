package com.zhenlaidian.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.OnekeyQurryAdapter;
import com.zhenlaidian.bean.OnekeyQueryParkInfo;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

/**
 * 停车场的收费明细
 * 
 * @author zhangyunfei 2015年9月6日
 */
public class OnekeyQurryParkActivity extends BaseActivity {

	private ActionBar actionBar;
	private TextView tv_park_today_total;
	private TextView tv_park_phone_total;
	private TextView tv_park_cash_total;
	private TextView tv_onekey_check_park_query_null;
	private ListView lv_park_employee;
	private LinearLayout ll_onekey_park_checking;
	private OnekeyQurryAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		setContentView(R.layout.activity_onekey_checking_park);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
		initVeiw();
		getOneQuery();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			OnekeyQurryParkActivity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void initVeiw() {
		tv_park_today_total = (TextView) findViewById(R.id.tv_onekey_checking_park_today_total);
		tv_park_phone_total = (TextView) findViewById(R.id.tv_onekey_checking_park_phone_total);
		tv_park_cash_total = (TextView) findViewById(R.id.tv_onekey_checking_park_cash_total);
		tv_onekey_check_park_query_null = (TextView) findViewById(R.id.tv_onekey_check_park_query_null);
		lv_park_employee = (ListView) findViewById(R.id.lv_onekey_checking_park_employee);
		ll_onekey_park_checking = (LinearLayout) findViewById(R.id.ll_onekey_park_checking);
	}

	public void setView(OnekeyQueryParkInfo info) {
		if (info == null) {
			setNullView();
			return;
		}
		tv_park_today_total.setText(info.getTotal() == null ? "" : info.getTotal());
		tv_park_phone_total.setText(info.getMmoeny() == null ? "" : info.getMmoeny());
		tv_park_cash_total.setText(info.getCashmoney() == null ? "" : info.getCashmoney());
		if (info.getDetail() != null && info.getDetail().size() != 0) {
			adapter = new OnekeyQurryAdapter(this, info.getDetail());
			lv_park_employee.setAdapter(adapter);
		}
	}

	public void setNullView() {
		ll_onekey_park_checking.setVisibility(View.GONE);
		tv_onekey_check_park_query_null.setVisibility(View.VISIBLE);
	}

	// collectorrequest.do?action=getparkdetail&token= 车场停车费明细
	public void getOneQuery() {
		String path = baseurl;
		String url = path + "collectorrequest.do?action=getparkdetail&token=" + token;
		MyLog.w("OnekeyQurryParkActivity", "获取一键查询的URL--->" + url);
		AQuery aQuery = new AQuery(this);
		final ProgressDialog dialog = ProgressDialog.show(this, "一键查询...", "查询中...", true, true);
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "请检查网络", 0).show();
			return;
		}
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				// TODO Auto-generated method stub
				super.callback(url, object, status);
				if (object != null && object != "") {
					dialog.dismiss();
					MyLog.v("OnekeyQurryParkActivity", "获取到的一键查询结果是--->" + object);
					Gson gson = new Gson();
					OnekeyQueryParkInfo info = gson.fromJson(object, OnekeyQueryParkInfo.class);
					MyLog.i("OnekeyQurryParkActivity", "解析到的一键查询结果是--->" + info.toString());
					setView(info);
				} else {
					dialog.dismiss();
					setNullView();
				}
			}
		});
	}
}
