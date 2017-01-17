package com.tq.zld.view.map;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tq.zld.TCBApp;
import com.tq.zld.R;
import com.tq.zld.adapter.MonthlyPayAdapter;
import com.tq.zld.bean.MonthlyPay;
import com.tq.zld.bean.ParkMonthlyPay;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.NetWorkUtils;
import com.tq.zld.view.BaseActivity;

/**
 * 包月产品 列出本车场所有的包月产品
 * 
 * @author zhangyunfei
 * 
 */
public class MonthlyPayListActivity extends BaseActivity {

	private ExpandableListView elvMonthlyPay;
	private View rlPageNull;
	private TextView tvPageNull;
	private TextView tvCurrentAddr;
	private MonthlyPayAdapter adapter;
	private ArrayList<String> parkids;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monthlypay_list);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("月卡团购");
		getActionBar().show();

		parkids = getIntent().getStringArrayListExtra("parkids");
		elvMonthlyPay = (ExpandableListView) findViewById(R.id.elv_the_month_product);
		if (adapter == null) {
			adapter = new MonthlyPayAdapter(this);
		}
		elvMonthlyPay.setAdapter(adapter);
		elvMonthlyPay.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				return true;
			}
		});
		elvMonthlyPay.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Intent intent = new Intent(getApplicationContext(),
						MonthlyPayDetailActivity.class);
				intent.putExtra("monthlypay", (MonthlyPay) adapter.getChild(
						groupPosition, childPosition));
				startActivity(intent);
				return true;
			}
		});
		rlPageNull = findViewById(R.id.rl_page_null);
		tvPageNull = (TextView) findViewById(R.id.tv_page_null);
		tvCurrentAddr = (TextView) findViewById(R.id.tv_monthly_product_current_address);
		String address = getIntent().getStringExtra("address");
		if (TextUtils.isEmpty(address)) {
			tvCurrentAddr.setVisibility(View.GONE);
		} else {
			tvCurrentAddr.setText(address);
		}
	}

	@Override
	protected void onResume() {
		getMonthlyPay();
		super.onResume();
	}

	private void getMonthlyPay() {
		// http://192.168.199.209/zld/getpark.do?action=getproducts&lon=116.393659&lat=39.849474&action=months&parkids=3,1197,1327&mobile=13855151555
		String lat = "";
		String lon = "";
		if (TCBApp.mLocation != null) {
			lat = String.valueOf(TCBApp.mLocation.latitude);
			lon = String.valueOf(TCBApp.mLocation.longitude);
		}
		String comid = "";
		for (int i = 0; i < parkids.size(); i++) {
			comid += parkids.get(i) + ",";
		}
		String url = TCBApp.mServerUrl + "getpark.do?action=getproducts&parkids="
				+ comid + "&mobile=" + TCBApp.mMobile + "&lat=" + lat + "&lon="
				+ lon;
		LogUtils.i(MonthlyPayListActivity.class, "访问包月产品的url-->>>" + url);
		if (NetWorkUtils.IsHaveInternet(this)) {
			final ProgressDialog dialog = new ProgressDialog(
					MonthlyPayListActivity.this);
			dialog.setMessage("请稍候...");
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
			AQuery aQuery = new AQuery(this);
			aQuery.ajax(url, String.class, new AjaxCallback<String>() {
				@Override
				public void callback(String url, String object,
						AjaxStatus status) {
					LogUtils.i(MonthlyPayListActivity.class,
							"getMonthlyPay result: --->> " + object);
					if (object != null) {
						dialog.dismiss();
						try {
							Gson gson = new Gson();
							ArrayList<ParkMonthlyPay> mProdInfo = gson
									.fromJson(
											object,
											new TypeToken<ArrayList<ParkMonthlyPay>>() {
											}.getType());
							if (mProdInfo != null && mProdInfo.size() > 0) {
								adapter.setinfos(mProdInfo);
								// 设置ExpandableListView默认全部展开
								for (int i = 0; i < mProdInfo.size(); i++) {
									elvMonthlyPay.expandGroup(i);
								}
								rlPageNull.setVisibility(View.INVISIBLE);
								elvMonthlyPay.setVisibility(View.VISIBLE);
							} else {
								setNullView();
							}
						} catch (Exception e) {
							setNullView();
							e.printStackTrace();
						}
					} else {
						dialog.dismiss();
						setNullView();
						return;
					}
				}

				private void setNullView() {
					elvMonthlyPay.setVisibility(View.INVISIBLE);
					tvPageNull.setText("本车场暂未提供任何包月产品哦！");
					rlPageNull.setVisibility(View.VISIBLE);
				}
			});
		} else {
			Toast.makeText(this, "请检查网络!", Toast.LENGTH_SHORT).show();
		}
	}

	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
