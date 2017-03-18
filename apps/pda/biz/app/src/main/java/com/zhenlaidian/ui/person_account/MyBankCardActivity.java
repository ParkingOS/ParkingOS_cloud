package com.zhenlaidian.ui.person_account;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.umeng.analytics.MobclickAgent;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.BankCard;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

public class MyBankCardActivity extends BaseActivity {

	private ActionBar actionBar;
	private LinearLayout ll_bandcard_binddone;
	private RelativeLayout rl_bankcard_nobinded;
	private TextView tv_bank_name;
	private TextView tv_card_number;
	private String username;
	private String account;
	private BankCard bankcard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_bankcard_activity);
		initActionBar();
//		initShare();
//		initshare();
		initView();
	}
//	public void initShare(){
//		SharedPreferences sharedPreferences = getSharedPreferences("editbank", Context.MODE_PRIVATE);
//		Editor editbank = sharedPreferences.edit();
//		editbank.putBoolean("myeditbank",false);
//		editbank.commit();
//	}
//	public void initshare(){
//		SharedPreferences myparkcard = getSharedPreferences("mycard",Context.MODE_PRIVATE);
//		 Editor autoedit = myparkcard.edit();
//		 autoedit.putBoolean("detailcard", false);
//		 autoedit.putBoolean("withdraw", false);
//		 autoedit.putBoolean("myselfcard", false);
//		 autoedit.commit();
//	}
	public void initActionBar() {
		actionBar = getSupportActionBar();	
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
				ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
	}

	private void initView() {
		ll_bandcard_binddone = (LinearLayout) findViewById(R.id.ll_bandcard_binddone);
		rl_bankcard_nobinded = (RelativeLayout) findViewById(R.id.rl_bankcard_nobinded);
		tv_bank_name = (TextView) findViewById(R.id.tv_bank_name);
		tv_card_number = (TextView) findViewById(R.id.tv_card_number);
		username = getIntent().getStringExtra("username");
		account = getIntent().getStringExtra("account");	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.actionbar, menu);
		getBankCardInfo(menu);
		return true;
	}

	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Intent editBankCardIntent = new Intent(MyBankCardActivity.this, EditBankCardActivity.class);
		editBankCardIntent.putExtra("username", username);
		editBankCardIntent.putExtra("account", account);
		switch (item.getItemId()) {
		case android.R.id.home:
			
			MyBankCardActivity.this.finish();
			return true;
		case R.id.add_bank_card:
			MyLog.w("TAG","添加我的银行卡:姓名-->"+username+";登录账号:---->"+account);
			editBankCardIntent.putExtra("isCardBinded", false);
			startActivity(editBankCardIntent);			
			MyBankCardActivity.this.finish();
			return true;
		case R.id.edit_bank_card:
			MyLog.w("MyBankCardActivity","编辑银行卡"+"username:"+username+"account"+account+"user_id"+bankcard.getUser_id());
			if(username != null&& account != null && bankcard != null){
				editBankCardIntent.putExtra("bankinfo", bankcard);
				editBankCardIntent.putExtra("isCardBinded", true);
				MyLog.w("TAG","编辑我的银行卡:姓名-->"+username+";登录账号:---->"+account+"卡号:--->"+bankcard.getUser_id());
				startActivity(editBankCardIntent);	
				MyBankCardActivity.this.finish();
			}
			break;
		default:
		}
		return super.onOptionsItemSelected(item);
	}

	public void getBankCardInfo(final Menu menu){
		SharedPreferences sharedPreferences = getSharedPreferences("autologin", Context.MODE_PRIVATE);
		String uid = sharedPreferences.getString("account", null);
		if (! IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "银卡卡信息获取失败，请检查网络！", 0).show();
			return;
		}
		String path = baseurl;
		String url = path+"useraccount.do?action=query&from=client&uid="+uid+"&comid="+comid;
		MyLog.w("MyBankCardActivity", "查询银行卡的url："+url);
		final ProgressDialog dialog = ProgressDialog.show(this, "加载中...","获取银行卡信息...", true, true);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>(){

			@SuppressLint("InlinedApi")
			@SuppressWarnings("deprecation")
			@Override
			public void callback(String url, String object, AjaxStatus status) {
				super.callback(url, object, status);
				
				MyLog.w("OOO",object.toString()+"自成");
				if (object != null && object.equals("{}")){
					dialog.dismiss();
					rl_bankcard_nobinded.setVisibility(View.VISIBLE);
					ll_bandcard_binddone.setVisibility(View.GONE);					
					MenuCompat.setShowAsAction(menu.findItem(R.id.add_bank_card),
							MenuItem.SHOW_AS_ACTION_IF_ROOM);
					  MenuItem item = menu.findItem(R.id.edit_bank_card);
				        item.setVisible(false);
					return;
				}
				if (object != null && object != "") {
					MyLog.w("MyBankCardActivity", "查询银行卡返回的信息是："+object);
					String cardNumber;
					dialog.dismiss();
					Gson gson = new Gson();
					bankcard = gson.fromJson(object, BankCard.class);
					MyLog.i("MyBankCardActivity", "查询银行卡解析的结果："+bankcard.toString());
					ll_bandcard_binddone.setVisibility(View.VISIBLE);
					rl_bankcard_nobinded.setVisibility(View.GONE);
					tv_bank_name.setText(bankcard.getBank_name());
					cardNumber = bankcard.getCard_number();
					if (cardNumber.length() > 4){
						cardNumber = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
					}
					tv_card_number.setText(cardNumber);
					MenuCompat.setShowAsAction(menu.findItem(R.id.edit_bank_card),
							MenuItem.SHOW_AS_ACTION_IF_ROOM);
					  MenuItem item = menu.findItem(R.id.add_bank_card);
				        item.setVisible(false);
				}else {
					dialog.dismiss();
				}
			}
		});
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
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
