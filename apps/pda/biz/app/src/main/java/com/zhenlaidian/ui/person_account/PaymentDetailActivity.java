package com.zhenlaidian.ui.person_account;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuCompat;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.PaymentDetailAdapter;
import com.zhenlaidian.bean.PersonAccountInfo;
import com.zhenlaidian.bean.QueryAccountDetail;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
/**
 * 我的收支明细
 * @author zhangyunfei
 * 2015年8月24日
 */
public class PaymentDetailActivity extends BaseActivity{

	private ActionBar actionBar;
	private Button bt_all,bt_income,bt_withdraw,bt_query_pay_log;//支付记录
	private ListView lv_payment_all;
	private LinearLayout ll_payment_detail_null; //没有数据时显示；
	private int page = 1;
	private int size = 20;
	private int server_count = 0;//服务器端总数；
	private int visiblecount = 0; //当前总条目数；
	private PaymentDetailAdapter adapter ;
	private String position = "";// 当前页卡的位置；"".全部，"0"收入，"1"提现；
	private ArrayList<QueryAccountDetail> allDetail = null;
	private Boolean isbottom  = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		setContentView(R.layout.payment_detail_activity);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
		adapter =  new PaymentDetailAdapter(this);
		initView();
		getAllPaymentInfo("");
	}

	private void initView() {
		bt_all = (Button) findViewById(R.id.bt_payment_detail_all);
		bt_income = (Button) findViewById(R.id.bt_payment_detail_income);
		bt_withdraw = (Button) findViewById(R.id.bt_payment_detail_withdraw);
		bt_query_pay_log = (Button) findViewById(R.id.bt_query_pay_log);
		lv_payment_all = (ListView) findViewById(R.id.lv_payment_detail);
		ll_payment_detail_null = (LinearLayout) findViewById(R.id.ll_payment_detail_null);
		
		bt_all.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 点击查看 -所有- 详情
				if ( !position.equals("") && isbottom == true) {
					page = 1;
					position = "";
					adapter.clearInfo();
					getAllPaymentInfo("");
					bt_all.setBackgroundResource((R.drawable.shape_payment_detail_blue));
					bt_income.setBackgroundResource((R.drawable.shape_payment_detail_gray));
					bt_withdraw.setBackgroundResource((R.drawable.shape_payment_detail_gray));
					bt_all.setTextColor(Color.WHITE);
					bt_income.setTextColor(Color.BLACK);
					bt_withdraw.setTextColor(Color.BLACK);
				}
			}
		});
		bt_income.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 点击查看 -收入- 详情
				if ( ! position.equals("0") && isbottom == true) {
					page = 1;
					position = "0";
					adapter.clearInfo();
					getAllPaymentInfo("0");
					bt_all.setBackgroundResource((R.drawable.shape_payment_detail_gray));
					bt_income.setBackgroundResource((R.drawable.shape_payment_detail_blue));
					bt_withdraw.setBackgroundResource((R.drawable.shape_payment_detail_gray));
					bt_all.setTextColor(Color.BLACK);
					bt_income.setTextColor(Color.WHITE);
					bt_withdraw.setTextColor(Color.BLACK);
				}
			}
		});
		bt_withdraw.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 点击查看 -提现- 详情
				if ( ! position.equals("1") && isbottom == true) {
					page = 1;
					position = "1";
					adapter.clearInfo();
					getAllPaymentInfo("1");
					bt_all.setBackgroundResource((R.drawable.shape_payment_detail_gray));
					bt_income.setBackgroundResource((R.drawable.shape_payment_detail_gray));
					bt_withdraw.setBackgroundResource((R.drawable.shape_payment_detail_blue));
					bt_all.setTextColor(Color.BLACK);
					bt_income.setTextColor(Color.BLACK);
					bt_withdraw.setTextColor(Color.WHITE);
				}
			}
		});
		bt_query_pay_log.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 查询车主支付记录；
				Intent intent  = new Intent(PaymentDetailActivity.this, CarOwnersPayLogActivity.class);
				startActivity(intent);
			}
		});
		lv_payment_all.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				switch (scrollState) {
				case  OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					isbottom = false;
				case OnScrollListener.SCROLL_STATE_FLING:
					isbottom = false;
					break;
				case OnScrollListener.SCROLL_STATE_IDLE: 
					// 判断滚动到底部
					isbottom = true;
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) { 
						if (visiblecount != server_count) {
							if ( adapter.getInfo(visiblecount) == null) {
								getAllPaymentInfo(position);
							}
						}
					}
					break;
				}
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				visiblecount = totalItemCount;
			}
		});
	}

	public void setAdapter() {
		lv_payment_all.setAdapter(adapter);
	}

	public void setPageNumber() {
		page ++;
	}
	
	public void setNullVeiw(){
		ll_payment_detail_null.setVisibility(View.VISIBLE);
		lv_payment_all.setVisibility(View.GONE);
	}
	
	/**
	 *http://192.168.199.240/zld/useraccount.do?action=acountdeail&uid=11336&stype=0&page=&size=
	 *stype=;全部。=0收入；=1提现；
	 */
	public void getAllPaymentInfo(final String stype){
		SharedPreferences sharedPreferences = getSharedPreferences("autologin", Context.MODE_PRIVATE);
		String uid = sharedPreferences.getString("account", null);
		if (! IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "账户明细获取失败，请检查网络！", 0).show();
			return;
		}
		String path = baseurl;
		String url = path+"useraccount.do?action=acountdeail&uid="+uid+"&stype="+stype+"&page="+page+"&size="+size;
		final ProgressDialog dialog = ProgressDialog.show(this, "加载中...","查询账户详情...", true, true);
		MyLog.w("PaymentDetailActivity", "个人账户收支明细的URl--->"+url);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>(){

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				super.callback(url, object, status);
				if (object != null && object != "") {
					MyLog.i("PaymentDetailActivity", "请求个人账户收支明细返回的结果--->"+object);
					dialog.dismiss();
					Gson gson = new Gson();
					PersonAccountInfo info = gson.fromJson(object, PersonAccountInfo.class);
					MyLog.i("PaymentDetailActivity", "解析个人账户收支明细的结果--->"+info.toString());
					if (info != null && info.getCount() != null && info.getInfo() != null ) {
						server_count = Integer.parseInt(info.getCount());
						allDetail = info.getInfo();
					}
					if (allDetail.size() != 0 ) {
						ll_payment_detail_null.setVisibility(View.GONE);
						lv_payment_all.setVisibility(View.VISIBLE);
						adapter.addInfo(allDetail, PaymentDetailActivity.this, server_count);
					}else{
						/* 显示无收支明细的提示界面 */
						setNullVeiw();
					}
				}else {
					dialog.dismiss();
				}
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.payment_detail_income_detail, menu);
		MenuCompat.setShowAsAction(menu.findItem(R.id.income_detail), MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}
	
	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			PaymentDetailActivity.this.finish();
			return true;
		case R.id.income_detail:
		Intent intent = new Intent(PaymentDetailActivity.this, MyIncomeDetailsActivity.class);
		startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		adapter.clearInfo();
	}
	

}
