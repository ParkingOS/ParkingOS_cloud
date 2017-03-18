package com.zhenlaidian.ui.park_account;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.zhenlaidian.R;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;
/**
 * 车场账户界面；
 * @author zhangyunfei
 * 2015年8月24日
 */
public class ParkingAccountActivity extends BaseActivity implements OnClickListener{
	private ActionBar actionBar;
	private SharedPreferences myparkcard;
	private RelativeLayout rl_withdrawal,rl_trading,rl_bankcard;
	private TextView allParkMoney;
//	private TextView allParkMoney,tv_image,tv_power;
//	private String Roles ,id= null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parking_account_item);
		initshare();
		initActionBar() ;
		initView();
		getBalanceInfo();
	}
	public void initshare(){
		// 自动登录
		 myparkcard = getSharedPreferences("mycard",Context.MODE_PRIVATE);
		 Editor autoedit = myparkcard.edit();
		 autoedit.putBoolean("detailcard", true);//明细
		 autoedit.putBoolean("withdraw", true);//提现
		 autoedit.putBoolean("myselfcard", true);//我的银行卡
		 autoedit.commit();
	}
	public void initActionBar() {
		actionBar = getSupportActionBar();	
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
				ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
	}
	public void initView(){
		allParkMoney = (TextView) findViewById(R.id.tv_all_park_money);
		rl_withdrawal = (RelativeLayout) findViewById(R.id.role_wallet_withdrawal);
		rl_withdrawal.setOnClickListener(this);
		rl_trading = (RelativeLayout) findViewById(R.id.role_wallet_trading);
		rl_trading.setOnClickListener(this);
		rl_bankcard = (RelativeLayout) findViewById(R.id.role_wallet_bankcard);
		rl_bankcard.setOnClickListener(this);
	}
	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			ParkingAccountActivity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * 获取车场账户余额；
	 */
	public void getBalanceInfo() {
		if ( ! IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "获取信息失败，请检查网络！", 0).show();
			return;
		}
		AQuery aQuery = new AQuery(ParkingAccountActivity.this);
		String url = null;
		url = baseurl+"collectorrequest.do?action=getpaccount&token="+token;
		System.out.println("请求车场信息的URL-->>"+url);
		MyLog.w("ParkingAccountActivity","返回的数据:rul"+url+"");
		final ProgressDialog dialog = ProgressDialog.show(this, "加载中...","获取车场信息数据...", true, true);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {
			@Override
			public void callback(String url, String object, AjaxStatus status) {
				if (!TextUtils.isEmpty(object)) {
					dialog.dismiss();
					MyLog.i("ParkingAccountActivity","返回的数据:查询停车场账户总额"+object.toString());
					allParkMoney.setText(object.toString());
					saveBanlance(object);
				}else {
					dialog.dismiss();
					return;
				}
			}
	});
}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.role_wallet_withdrawal://提现；
			Intent intent1 =new Intent(ParkingAccountActivity.this ,ParkWithdrawalActivity.class);
			startActivity(intent1);
			break;
		case R.id.role_wallet_trading://收支明细
			Intent intent2 =new Intent(ParkingAccountActivity.this ,ParkPaymentDetailActivity.class);
			startActivity(intent2);
			break;
		case R.id.role_wallet_bankcard://我的银行卡
			Intent intent3 =new Intent(ParkingAccountActivity.this ,ParkBankCardActivity.class);
			startActivity(intent3);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 保存余额
	 */
	private void saveBanlance(String banlance) {
		SharedPreferencesUtils spu = SharedPreferencesUtils.getIntance(ParkingAccountActivity.this);
		spu.setBanlance(banlance);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		SharedPreferencesUtils spu = SharedPreferencesUtils.getIntance(ParkingAccountActivity.this);
		allParkMoney.setText(spu.getBanlance());
	}
}