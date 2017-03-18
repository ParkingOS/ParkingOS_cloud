package com.zhenlaidian.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.OneKeyQueryInfo;
import com.zhenlaidian.ui.person_account.MyIncomeDetailsActivity;
import com.zhenlaidian.ui.person_account.PaymentDetailActivity;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

public class OneKeyQueryActivity extends BaseActivity {

	private ActionBar actionBar;
	private TextView tv_today_total;
	private TextView tv_phone_total;
	private TextView tv_cash_total;
	private TextView tv_my_account;
	private TextView tv_park_account;
	private TextView tv_checking_me_details;// 查看我的账户明细
	private TextView tv_checking_park_details;// 查看车场的收入统计
	private TextView tv_time_order;
	private TextView tv_time_order_money;
	private TextView tv_pay_order;
	private TextView tv_pay_order_money;
	private TextView tv_tv_query_null;
	private Button bt_allparking_details;
	private LinearLayout ll_onekey_checking;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		setContentView(R.layout.activity_onekey_checking);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
		initView();
		getOneQuery();
	}

	// 注释打开即可开始原一键查询的入口
	// @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	// @SuppressWarnings("deprecation")
	// public boolean onCreateOptionsMenu(Menu menu) {
	// getMenuInflater().inflate(R.menu.old_one_key_query, menu);
	// MenuCompat.setShowAsAction(menu.findItem(R.id.old_query),
	// MenuItem.SHOW_AS_ACTION_IF_ROOM);
	// return true;
	// }

	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			OneKeyQueryActivity.this.finish();
			return true;
		case R.id.old_query:
			Intent intent = new Intent(OneKeyQueryActivity.this, OldOneKeyQueryActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void initView() {
		tv_today_total = (TextView) findViewById(R.id.tv_onekey_checking_today_total);
		tv_phone_total = (TextView) findViewById(R.id.tv_onekey_checking_phone_total);
		tv_cash_total = (TextView) findViewById(R.id.tv_onekey_checking_cash_total);
		tv_my_account = (TextView) findViewById(R.id.tv_onekey_checking_my_account);
		tv_park_account = (TextView) findViewById(R.id.tv_onekey_checking_park_account);
		tv_checking_me_details = (TextView) findViewById(R.id.tv_onekey_checking_me_details);
		tv_checking_park_details = (TextView) findViewById(R.id.tv_onekey_checking_park_details);
		tv_time_order = (TextView) findViewById(R.id.tv_onekey_checking_time_order);
		tv_time_order_money = (TextView) findViewById(R.id.tv_onekey_checking_time_order_money);
		tv_pay_order = (TextView) findViewById(R.id.tv_onekey_pay_order);
		tv_pay_order_money = (TextView) findViewById(R.id.tv_onekey_pay_order_money);
		tv_tv_query_null = (TextView) findViewById(R.id.tv_onekey_check_query_null);
		bt_allparking_details = (Button) findViewById(R.id.bt_onekey_check_allparking_details);
		ll_onekey_checking = (LinearLayout) findViewById(R.id.ll_onekey_checking);
		SharedPreferences autologin = getSharedPreferences("autologin", Context.MODE_PRIVATE);
		String role = autologin.getString("role", "2");
		if (role.equals("1")) {
			bt_allparking_details.setVisibility(View.VISIBLE);
		} else {
			bt_allparking_details.setVisibility(View.GONE);
		}
	}

	public void setView(OneKeyQueryInfo info) {
		if (info == null) {
			setNullView();
			return;
		}
		tv_checking_me_details.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 点击去查看账户明细
				Intent intent = new Intent(OneKeyQueryActivity.this, PaymentDetailActivity.class);
				startActivity(intent);
			}
		});
		tv_checking_park_details.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 点击去查看车场的收入统计 :设置收入统计默认查看车场。
				SharedPreferencesUtils.getIntance(OneKeyQueryActivity.this).setdefaultCheck(true);
				Intent intent = new Intent(OneKeyQueryActivity.this, MyIncomeDetailsActivity.class);
				startActivity(intent);
			}
		});
		bt_allparking_details.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 收费员可以查看整个车场的详情
				Intent intent = new Intent(OneKeyQueryActivity.this, OnekeyQurryParkActivity.class);
				startActivity(intent);

			}
		});
		tv_today_total.setText(info.getTotalmoney() == null ? "" : info.getTotalmoney());
		tv_phone_total.setText(info.getMobilemoney() == null ? "" : info.getMobilemoney());
		tv_cash_total.setText(info.getCashmoney() == null ? "" : info.getCashmoney());
		tv_my_account.setText(info.getMycount() == null ? "" : "进入你的账户：" + info.getMycount() + "元");
		tv_park_account.setText(info.getParkaccout() == null ? "" : "进入停车场账户：" + info.getParkaccout() + "元");
		tv_time_order.setText(info.getTimeordercount() == null ? "" : "计时计费订单(" + info.getTimeordercount() + ")笔");
		tv_time_order_money.setText(info.getTimeordermoney() == null ? "" : info.getTimeordermoney() + "元");
		tv_pay_order.setText(info.getEpayordercount() == null ? "" : "直付订单(" + info.getEpayordercount() + ")笔");
		tv_pay_order_money.setText(info.getEpaymoney() == null ? "" : info.getEpaymoney() + "元");
	}

	public void setNullView() {
		ll_onekey_checking.setVisibility(View.GONE);
		tv_tv_query_null.setVisibility(View.VISIBLE);
	}

	// http://127.0.0.1/zld/collectorrequest.do?action=corder&token=73de6dcf6987a6c6eb9c28bb8401ef25
	// 一键对账新接口 collectorrequest.do?action=akeycheckaccount&token=
	// {"totalmoney":0.0,"mobilemoney":0.0,"cashmoney":0.0,"mycount":0.0,"parkaccout":0.0,"timeordercount":0,
	// "timeordermoney":0.0,"epayordercount":0,"epaymoney":0.0}

	public void getOneQuery() {
		String path = baseurl;
		String url = path + "collectorrequest.do?action=akeycheckaccount&token=" + token;
		MyLog.w("OneKeyQueryActivity", "获取一键查询的URL--->" + url);
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
					MyLog.v("OneKeyQueryActivity", "获取到的一键查询结果是--->" + object);
					Gson gson = new Gson();
					OneKeyQueryInfo info = gson.fromJson(object, OneKeyQueryInfo.class);
					MyLog.i("OneKeyQueryActivity", "解析到的一键查询结果是--->" + info.toString());
					setView(info);
				} else {
					dialog.dismiss();
					setNullView();
				}
			}
		});
	}

}
