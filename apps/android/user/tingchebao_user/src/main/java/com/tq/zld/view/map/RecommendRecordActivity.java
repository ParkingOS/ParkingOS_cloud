package com.tq.zld.view.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.RecommendRecord;
import com.tq.zld.util.DensityUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.view.MainActivity;

public class RecommendRecordActivity extends BaseActivity implements
		OnClickListener {

	private View rootView;
	private View rlPageNull;
	private TextView tvSee;
	private TextView tvPageNull;
	private TextView tvTotal;
	private ListView lvRecord;

	private RecommendRecordAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recommend_record);
		initActionBar();
		initView();
		getRecords();
	}

	private void getRecords() {
		// http://127.0.0.1/zld/carowner.do?action=recominfo&mobile=
		String url = TCBApp.mServerUrl + "carowner.do";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("mobile", TCBApp.mMobile);
		params.put("action", "recominfo");
		LogUtils.i(getClass(), "获取推荐记录 url: --->> " + url + "\nparams: --->> "
				+ params.toString());
		final ProgressDialog dialog = ProgressDialog.show(this, "", "请稍候...",
				false, true);
		dialog.setCanceledOnTouchOutside(false);
		new AQuery(this).ajax(url, params, String.class,
				new AjaxCallback<String>() {
					@Override
					public void callback(String url, String object,
							AjaxStatus status) {
						dialog.dismiss();
						LogUtils.i(RecommendRecordActivity.class,
								"获取推荐记录 result: --->> " + object);
						if (!TextUtils.isEmpty(object)) {
							try {
								ArrayList<RecommendRecord> records = new Gson()
										.fromJson(
												object,
												new TypeToken<ArrayList<RecommendRecord>>() {
												}.getType());
								if (records != null && records.size() > 0) {
									mAdapter.setRecords(records);
									refreshView(records, "");
								} else {
									refreshView(null, "您还没有推荐收费员，快去推荐吧！");
								}
							} catch (Exception e) {
								// TODO 测试假数据
								// ArrayList<RecommendRecord> records = new
								// ArrayList<>();
								// records.add(new RecommendRecord("10000",
								// "1"));
								// records.add(new RecommendRecord("10003",
								// "0"));
								// records.add(new RecommendRecord("10001",
								// "1"));
								// records.add(new RecommendRecord("10004",
								// "0"));
								// records.add(new RecommendRecord("10012",
								// "1"));
								// records.add(new RecommendRecord("10011",
								// "0"));
								// mAdapter.setRecords(records);
								// refreshView(records, "");
								refreshView(null, "数据解析异常，请稍后重试！");
								e.printStackTrace();
							}
						} else {
							refreshView(null, "网络超时，请稍后重试！");
						}
						super.callback(url, object, status);
					}
				});
	}

	private void refreshView(ArrayList<RecommendRecord> records, String errmsg) {
		LogUtils.i(RecommendRecordActivity.class, "获取推荐记录错误信息：--->> " + errmsg);
		if (TextUtils.isEmpty(errmsg)) {// 数据正常
			tvTotal.setText(Html.fromHtml("共获得奖励 <font color='#329762'><big>"
					+ getTotal(records) + "</big></font> 元"));
			rlPageNull.setVisibility(View.GONE);
		} else {
			tvPageNull.setText(errmsg);
			rlPageNull.setVisibility(View.VISIBLE);
		}
		rootView.setVisibility(View.VISIBLE);
	}

	private int getTotal(ArrayList<RecommendRecord> records) {
		if (records == null || records.size() == 0) {
			return 0;
		}
		int total = 0;
		for (RecommendRecord record : records) {
			if ("1".equals(record.state)) {
				total += 30;
			} else {
				total += 5;
			}
		}
		return total;
	}

	private void initView() {
		rootView = findViewById(R.id.rl_recommend_record_root);
		rootView.setVisibility(View.INVISIBLE);
		rlPageNull = findViewById(R.id.rl_page_null);
		rlPageNull.setVisibility(View.VISIBLE);
		tvSee = (TextView) findViewById(R.id.tv_recommend_record_gotosee);
		tvSee.setOnClickListener(this);
		tvSee.setText(Html.fromHtml("<font color='#5F75DA'><u>去查看</u></font>"));
		tvPageNull = (TextView) findViewById(R.id.tv_page_null);
		tvPageNull.setText("您还没有推荐过收费员，快去推荐吧！");
		tvTotal = (TextView) findViewById(R.id.tv_recommend_record_total);
		lvRecord = (ListView) findViewById(R.id.lv_recommend_record);
		mAdapter = new RecommendRecordAdapter();
		lvRecord.setAdapter(mAdapter);
	}

	private void initActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("推荐记录");
		getActionBar().show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	class RecommendRecordAdapter extends BaseAdapter {

		private ArrayList<RecommendRecord> records;

		public void setRecords(ArrayList<RecommendRecord> records) {
			// 先排序
			Collections.sort(records, new Comparator<RecommendRecord>() {

				@Override
				public int compare(RecommendRecord lhs, RecommendRecord rhs) {
					try {
						int lhsState = Integer.parseInt(lhs.state);
						int rhsState = Integer.parseInt(rhs.state);
						Integer lhsId = Integer.parseInt(lhs.uin);
						Integer rhsId = Integer.parseInt(rhs.uin);
						if (lhsState > rhsState) {
							return -1;
						} else if (lhsState < rhsState) {
							return 1;
						} else {
							return lhsId.compareTo(rhsId);
						}
					} catch (Exception e) {
						e.printStackTrace();
						return 0;
					}
				}
			});
			if (this.records == null) {
				this.records = new ArrayList<RecommendRecord>();
			} else {
				this.records.clear();
			}
			this.records.addAll(records);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return records == null ? 0 : records.size();
		}

		@Override
		public Object getItem(int position) {
			return records == null ? null : records.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RecommendRecord record = (RecommendRecord) getItem(position);
			if (record == null) {
				return null;
			}
			if (convertView == null) {
				convertView = new TextView(RecommendRecordActivity.this);
				((TextView) convertView).setGravity(Gravity.CENTER_HORIZONTAL);
				((TextView) convertView).setTextColor(Color.DKGRAY);
				int padding = DensityUtils.dip2px(RecommendRecordActivity.this,
						8);
				((TextView) convertView).setPadding(padding * 3, padding,
						padding, padding);
				((TextView) convertView).setTextSize(
						TypedValue.COMPLEX_UNIT_SP, 16);
				((TextView) convertView).setGravity(Gravity.CENTER_VERTICAL
						| Gravity.LEFT);
			}
			((TextView) convertView).setText(String.format(
					"已推荐收费员编号（%1$s）,%2$s", record.uin,
					"1".equals(record.state) ? "获得奖励30元" : "正在审核中..."));
			return convertView;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_recommend_record_gotosee:
			Intent intent = new Intent(getApplicationContext(),
					MainActivity.class);
			intent.putExtra(MainActivity.ARG_FRAGMENT,
					MainActivity.FRAGMENT_ACCOUNT);
			startActivity(intent);
			break;
		}
	}
}
