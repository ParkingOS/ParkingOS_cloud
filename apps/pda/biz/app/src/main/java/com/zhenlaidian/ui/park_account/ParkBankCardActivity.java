package com.zhenlaidian.ui.park_account;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import com.zhenlaidian.bean.MyBankCard;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
//我的银行卡页面
public class ParkBankCardActivity extends BaseActivity{

	private ActionBar actionBar;
	private LinearLayout ll_pb_binddone;//银行卡显示页面
	private RelativeLayout rl_pb_nobinded;//没有银行卡页面
	private TextView tv_pb_name;//所属银行
	private TextView tv_pb_number;//银行卡号
	private MyBankCard bankcard;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parkback_activity);
		initShare();
		initActionBar();
		initView();
	}
	public void initShare(){
		SharedPreferences sharedPreferences = getSharedPreferences("editbank", Context.MODE_PRIVATE);
		Editor editbank = sharedPreferences.edit();
		editbank.putBoolean("myeditbank",true);
		editbank.commit();
	}
	
	
	public void initActionBar() {
		actionBar = getSupportActionBar();	
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
				ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.actionbar, menu);
		getBankCardInfo(menu);
		return true;
	}

	private void initView() {
		ll_pb_binddone = (LinearLayout) findViewById(R.id.ll_pb_binddone);
		rl_pb_nobinded = (RelativeLayout) findViewById(R.id.rl_pb_nobinded);
		tv_pb_name = (TextView) findViewById(R.id.tv_pb_name);
		tv_pb_number = (TextView) findViewById(R.id.tv_pb_number);
	}
	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences sharedPreferences = getSharedPreferences("card_master", Context.MODE_PRIVATE);
		Editor cardeditor = sharedPreferences.edit();
		cardeditor.putString("master","ok");
		cardeditor.commit();
		Intent editBankCardIntent = new Intent(ParkBankCardActivity.this, ParkEditBankCardActivity.class);
		switch (item.getItemId()) {
		case android.R.id.home:
			ParkBankCardActivity.this.finish();
			return true;
		case R.id.add_bank_card:
			editBankCardIntent.putExtra("isCardBinded", false);
			startActivity(editBankCardIntent);			
			ParkBankCardActivity.this.finish();
			return true;
		case R.id.edit_bank_card:
			editBankCardIntent.putExtra("isCardBinded", true);
			editBankCardIntent.putExtra("mybankcard", bankcard);
			startActivity(editBankCardIntent);	
			ParkBankCardActivity.this.finish();
			break;
		default:
		}
		return super.onOptionsItemSelected(item);
	}
	public void getBankCardInfo(final Menu menu){
		if (! IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "银卡卡信息获取失败，请检查网络！", 0).show();
			return;
		}
		String path = baseurl;
		String url = path+"collectorrequest.do?action=getparkbank&token="+token;
		final ProgressDialog dialog = ProgressDialog.show(this, "加载中...","获取银行卡信息...", true, true);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>(){
			@SuppressLint("InlinedApi")
			@SuppressWarnings("deprecation")
			@Override
			public void callback(String url, String object, AjaxStatus status) {
				super.callback(url, object, status);
				if (object != null && object.equals("{}")){
					dialog.dismiss();
					rl_pb_nobinded.setVisibility(View.VISIBLE);
					ll_pb_binddone.setVisibility(View.GONE);					
					MenuCompat.setShowAsAction(menu.findItem(R.id.add_bank_card),
							MenuItem.SHOW_AS_ACTION_IF_ROOM);
					  MenuItem item = menu.findItem(R.id.edit_bank_card);
				        item.setVisible(false);
					return;
				}
				if (object != null && object != "") {
					String cardNumber;
					dialog.dismiss();
					Gson gson = new Gson();
					bankcard = gson.fromJson(object, MyBankCard.class);
					MyLog.w("TAG","加载账户信息"+bankcard.toString());//atype: 0银行卡，1支付宝，2微信
					ll_pb_binddone.setVisibility(View.VISIBLE);
					rl_pb_nobinded.setVisibility(View.GONE);
					tv_pb_name.setText(bankcard.getBank_name());//工商银行 
					cardNumber = bankcard.getCard_number();//银行卡号
					
					if (cardNumber.length() > 4){
						cardNumber = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
					}
					tv_pb_number.setText(cardNumber);
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
		super.onDestroy();
	}
}
