package com.zhenlaidian.ui.person_account;

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
import com.zhenlaidian.ui.RecommendCashierActivity;
import com.zhenlaidian.util.DataTypeChange;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;
/**
 * 我的账户（钱包）界面
 * @author zhangyunfei
 * 2015年8月24日
 */
public class MyWalletActivity extends BaseActivity implements OnClickListener {

//	private LinearLayout ll_wallet_money;
	private TextView tv_wallet_money;
	private TextView tv_make_money;
	private TextView tv_ischeck;
	private RelativeLayout rl_wallet_withdrawal;
	private RelativeLayout rl_wallet_trading;
	private RelativeLayout rl_wallet_bankcard;
	private RelativeLayout rl_wallet_alipy;
	private Boolean isCardCheck;
	private String banlance;
	private String username;
	private String account;
	private ActionBar actionBar;
	private Intent bankcardIntent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		setContentView(R.layout.my_wallet_activity);
		actionBar.setTitle("我的钱包");
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
				ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
		initVeiw();
		initshare();//我的钱包
		if (getIntent() != null && getIntent().getBooleanExtra("LeaveActivity", false)) {
			getBalanceInfo();
		}else {
			setBalanceView();
		}
		onclickevent();
	}
	
	public void initshare(){
		// 自动登录
		SharedPreferences myparkcard = getSharedPreferences("mycard",Context.MODE_PRIVATE);
		 Editor autoedit = myparkcard.edit();
		 autoedit.putBoolean("detailcard", false);//明细
		 autoedit.putBoolean("withdraw", false);//提现
		 autoedit.putBoolean("myselfcard", false);//我的银行卡
		 autoedit.commit();
	}
	private void initVeiw() {
		username = getIntent().getStringExtra("username");
		account = getIntent().getStringExtra("account");
		MyLog.w("TAG","我的钱包:"+username+ account);
//		ll_wallet_money = (LinearLayout) findViewById(R.id.ll_wallet_money);
		tv_wallet_money = (TextView) findViewById(R.id.tv_wallet_money);
		tv_ischeck = (TextView) findViewById(R.id.tv_wallet_ischeck);
		tv_make_money = (TextView) findViewById(R.id.tv_my_wallet_make_money);
		rl_wallet_withdrawal = (RelativeLayout) findViewById(R.id.rl_wallet_withdrawal);
		rl_wallet_trading = (RelativeLayout) findViewById(R.id.rl_wallet_trading);
		rl_wallet_bankcard = (RelativeLayout) findViewById(R.id.rl_wallet_bankcard);
		rl_wallet_alipy = (RelativeLayout) findViewById(R.id.rl_wallet_alipy);
		
	}

	private void onclickevent() {
		// TODO Auto-generated method stub
		rl_wallet_withdrawal.setOnClickListener(this);
		rl_wallet_trading.setOnClickListener(this);
		rl_wallet_bankcard.setOnClickListener(this);
		rl_wallet_alipy.setOnClickListener(this);
		tv_make_money.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.rl_wallet_withdrawal:
			//提现
			//判断是否有添加银行卡卡账户和支付宝账户，
			//如果都没有：弹出吐司：“您尚未添加银行卡或者支付宝，无法提现”
			//如果有：提现账户，跳转到提现界面
			if(banlance != null && !"{}".equals(banlance) && DataTypeChange.isDouble(banlance) && Double.parseDouble(banlance) > 0){
				Intent intent = new Intent(MyWalletActivity.this, WithdrawalActivity.class);
				startActivity(intent);
			}else{
				showTost("余额不足");
			}
			break;
		case R.id.rl_wallet_trading://收支明细
			Intent tradingIntent = new Intent(MyWalletActivity.this, PaymentDetailActivity.class);
			startActivity(tradingIntent);
			break;
		case R.id.rl_wallet_bankcard://我的银行卡
			if (isCardCheck) {
				Toast.makeText(MyWalletActivity.this, "您已上传图片，等待绑定中...", 0).show();
			}else {
				bankcardIntent = new Intent(MyWalletActivity.this, MyBankCardActivity.class);
				bankcardIntent.putExtra("username", username);
				bankcardIntent.putExtra("account", account);
				startActivity(bankcardIntent);
				MyLog.w("TAG","MyWalletActivity-->我的银行卡,username:"+username+",account"+account);
			}
			break;
		case R.id.rl_wallet_alipy://我的支付宝账户
			//			Intent alipyIntent = new Intent(MyWalletActivity.this, WithdrawalActivity.class);
			//			startActivity(alipyIntent);
			break;
		case R.id.tv_my_wallet_make_money://我的支付宝账户
			Intent intent = new Intent(MyWalletActivity.this, RecommendCashierActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	//获取余额信息：
	// useraccount.do?action=getaccount&uid=11340
	public void getBalanceInfo() {
		SharedPreferences sharedPreferences = getSharedPreferences("autologin",Context.MODE_PRIVATE);
		String uid = sharedPreferences.getString("account", null);
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "余额信息获取失败，请检查网络！", 0).show();
			return;
		}
		String path = baseurl;
		String url = path + "useraccount.do?action=getaccount&uid=" + uid;
		MyLog.w("余额信息--URL---->>>", url);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				super.callback(url, object, status);
				MyLog.i("获取余额信息内容是---->>>", object);
				if (!TextUtils.isEmpty(object) && object.length() < 15) {
					MyLog.i("解析到余额信息内容是---->>>", object.toString());
					banlance = object.toString();
					tv_wallet_money.setText(banlance);
					saveBanlance(banlance);
				} else {
					// dialog.dismiss();
				}
			}
		});
	}
		
	/**
	 * 保存余额
	 */
	private void saveBanlance(String banlance) {
		SharedPreferencesUtils.getIntance(this).setBanlance(banlance);
	}
	
	/**
	 * 设置余额信息
	 */
	private void setBalanceView() {
		banlance = SharedPreferencesUtils.getIntance(this).getBanlance();
		if (banlance != null) {
			tv_wallet_money.setText(banlance);
		}
	}

	public void showTost(String info) {
		Toast.makeText(MyWalletActivity.this, info, 0).show();
	}

	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			MyWalletActivity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {   
		super.onResume();
		isCardCheck = SharedPreferencesUtils.getIntance(this).getIsCardCheck();
		if (isCardCheck ) {
			tv_ischeck.setVisibility(View.VISIBLE);
		}else {
			tv_ischeck.setVisibility(View.INVISIBLE);
		}
	}

}
