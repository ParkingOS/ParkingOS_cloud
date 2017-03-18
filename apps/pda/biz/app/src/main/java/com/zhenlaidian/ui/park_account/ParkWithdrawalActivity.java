package com.zhenlaidian.ui.park_account;

import java.util.Calendar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.BankCard;
import com.zhenlaidian.bean.WithdrawalNum;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

/**
 * 车场提现
 * 
 * @author zhangyunfei 2015年8月24日
 */
public class ParkWithdrawalActivity extends BaseActivity {

	private TextView tv_bank;
	private TextView tv_bank_account;
	private TextView tv_bank_card;
	private TextView tv_withdrawal_balance;
	private EditText et_input_withdrawal_money;
	private TextView tv_withdrawal_num;
	private Button btn_transfer_ok;

	private String banlance;
	private String withdrawalNum;
	private String currentData;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		setContentView(R.layout.withdrawal_activity);
		actionBar.setTitle("提现");
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
		getBanlance();
		initVeiw();
		getAccountBalanceInfo();
	}

	private void initVeiw() {
		tv_bank = (TextView) findViewById(R.id.tv_bank);
		tv_bank_account = (TextView) findViewById(R.id.tv_bank_account);
		tv_bank_card = (TextView) findViewById(R.id.tv_bank_card);
		tv_withdrawal_balance = (TextView) findViewById(R.id.tv_withdrawal_balance);
		et_input_withdrawal_money = (EditText) findViewById(R.id.et_input_withdrawal_money);
		tv_withdrawal_num = (TextView) findViewById(R.id.tv_withdrawal_num);
		btn_transfer_ok = (Button) findViewById(R.id.btn_transfer_ok);
		et_input_withdrawal_money.setHint("请输入100元以上提现金额");
		et_input_withdrawal_money.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

				if (s.toString().equals("")) {
					changeBackColor();
				} else {
					btn_transfer_ok.setBackgroundColor(getResources().getColor(R.color.tv_leaveItem_state_green));
					btn_transfer_ok.setTextColor(android.graphics.Color.parseColor("#ffffff"));
				}
			}
		});
		btn_transfer_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-确认转出
				// 如果提现金额大于余额，点击“确认转出”的时候，吐司提示“余额不足”
				// 提现金额小于10的话，吐司提示“请输入大于10的提现金额”
				String money = et_input_withdrawal_money.getText().toString();
				System.out.println("money:" + money.length() + "money");
				if (money.length() != 0 && money != null) {
					if (Double.parseDouble(money) < 100) {
						showTost("请输入大于100元的提现金额");
						return;
					}
					if (Double.parseDouble(banlance) < Double.parseDouble(money)) {
						showTost("余额不足");
					} else {
						if (Double.parseDouble(money) < 49999.00) {
							getWithdrawalInfo(money);
						} else {
							showTost("超出限额");
						}
					}
					changeBackColor();
				} else {
					Toast.makeText(ParkWithdrawalActivity.this, "请输入提现金额", Toast.LENGTH_LONG).show();
				}
			}

		});
	}

	private void changeBackColor() {
		btn_transfer_ok.setBackgroundColor(android.graphics.Color.parseColor("#DCDCDC"));
		btn_transfer_ok.setTextColor(android.graphics.Color.parseColor("#939393"));
	}

	/**
	 * 设置银行卡信息 卡的类型 默认为储蓄卡
	 * 
	 * @param bankcard
	 */
	public void setBankcardView(BankCard bankcard) {
		if (banlance != null) {
			tv_withdrawal_balance.setText(banlance);
		}
		if (withdrawalNum != null) {
			if (currentData != null && currentData.equals("" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH))) {
				tv_withdrawal_num.setText(withdrawalNum);
			} else {
				tv_withdrawal_num.setText("" + 3);
			}
		}
		if (bankcard.getBank_name() != null) {
			tv_bank.setText(bankcard.getBank_name());
		}
		if (bankcard.getCard_number() != null) {
			String cardNumber = bankcard.getCard_number();
			if (cardNumber.length() > 4) {
				cardNumber = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
			}
			tv_bank_account.setText(cardNumber);
		}
	}

	// collectorrequest.do?action=getparkbank&token=aa9a48d2f41bb2722f29c8714cbc754c
	// 返回：{"id":"115","card_number":"6222000200113996540","name":"马永涛","mobile":"13718933660","bank_name":"工商银行","area":"北京","bank_pint":"朝阳东苇路支行","user_id":"32033434390335030432"}
	// atype: 0银行卡，1支付宝，2微信
	// user_id:身份证号
	// 没有绑定时返回{}
	public void getAccountBalanceInfo() {
		// SharedPreferences sharedPreferences =
		// getSharedPreferences("autologin", Context.MODE_PRIVATE);
		// String uid = sharedPreferences.getString("account", null);
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "银卡卡信息获取失败，请检查网络！", 0).show();
			return;
		}
		String path = baseurl;
		String url = path + "collectorrequest.do?action=getparkbank&token=" + token;
		MyLog.w("银行卡信息--URL---->>>", url);
		final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取银行卡信息...", true, true);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				super.callback(url, object, status);
				MyLog.i("获取银行卡信息内容是---->>>", object);
				if (object != null && object != "") {
					dialog.dismiss();
					Gson gson = new Gson();
					BankCard bankcard = gson.fromJson(object, BankCard.class);
					MyLog.i("解析到银行卡信息内容是---->>>", bankcard.toString());
					if (bankcard != null) {
						setBankcardView(bankcard);
					}
				} else {
					dialog.dismiss();
				}
			}
		});
	}

	/**
	 * 收费员提现操作 useraccount.do?action=withdraw&uid=11340&comid=&money=
	 * uid:收费员账户，comid：车场编号，money:提现金额 {"result":0,"times":1}
	 * 返回result:1成功，-1未设置银行账户 ，-2:已提现三次 ，0:余额不足，其它失败 times:提现次数
	 * 
	 * 车场账户提现； collectorrequest.do?action=withdraw&money=100&token=
	 * aa9a48d2f41bb2722f29c8714cbc754c 返回：{"result":-1,"times":0}
	 * result：0失败，1成功，-1车场账户不存在，-2超过提现3次 times：提现次数
	 */
	public void getWithdrawalInfo(final String money) {
		SharedPreferences sharedPreferences = getSharedPreferences("autologin", Context.MODE_PRIVATE);
		String uid = sharedPreferences.getString("account", null);
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "转出失败，请检查网络！", 0).show();
			return;
		}
		String path = baseurl;
		String url = path + "collectorrequest.do?action=withdraw&money=" + money + "&token=" + token;
		MyLog.w("车场提现的-URL---->>>", url);
		final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "转出余额...", true, true);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				super.callback(url, object, status);
				if (object != null && object != "") {
					MyLog.i("请求车场提现返回的内容是---->>>", object);
					dialog.dismiss();
					Gson gson = new Gson();
					WithdrawalNum withdrawalNum = gson.fromJson(object, WithdrawalNum.class);
					MyLog.i("解析车场提现的内容是---->>>", withdrawalNum.toString());
					SharedPreferencesUtils spu = SharedPreferencesUtils.getIntance(ParkWithdrawalActivity.this);
					if (withdrawalNum != null) {
						if (withdrawalNum.getResult().equals("1")) {
							showTost("成功");
							if (withdrawalNum.getTimes() != null) {
								spu.setWithdrawalNum("" + (2 - Integer.parseInt(withdrawalNum.getTimes())));
								spu.setCurrentData();
							}
							double sbalance = Double.parseDouble(banlance) - Double.parseDouble(money);
							tv_withdrawal_balance.setText(new java.text.DecimalFormat("#.00").format(sbalance) + "");
							spu.setBanlance(tv_withdrawal_balance.getText().toString().trim());
							ParkWithdrawalActivity.this.finish();
						} else if (withdrawalNum.getResult().equals("-1")) {
							showTost("未设置银行账户");
						} else if (withdrawalNum.getResult().equals("0")) {
							showTost("余额不足");
						} else if (withdrawalNum.getResult().equals("-2")) {
							showTost("已提现3次");
							tv_withdrawal_num.setText(0 + "");
							spu.setWithdrawalNum(0 + "");
						} else {
							showTost("转出失败");
						}
					}
				} else {
					dialog.dismiss();
				}
			}
		});
	}

	public void showTost(String info) {
		Toast.makeText(ParkWithdrawalActivity.this, info, 0).show();
	}

	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			ParkWithdrawalActivity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * 获取余额信息
	 */
	private void getBanlance() {
		SharedPreferencesUtils spu = SharedPreferencesUtils.getIntance(ParkWithdrawalActivity.this);
		banlance = spu.getBanlance();
		withdrawalNum = spu.getWithdrawalNum();
		currentData = spu.getCurrentData();
	}

	// private void saveWithdrawalNum(String withdrawalNum){
	// SharedPreferencesUtils spu = new
	// SharedPreferencesUtils(WithdrawalActivity.this, "banlance");
	// spu.setWithdrawalNum(withdrawalNum);
	// }

	// private void getWithdrawalNum(){
	// SharedPreferencesUtils spu = new
	// SharedPreferencesUtils(WithdrawalActivity.this, "banlance");
	// spu.getWithdrawalNum();
	// }

	@Override
	public void onResume() {
		super.onResume();
		getBanlance();
		// getWithdrawalNum();
	}

}
