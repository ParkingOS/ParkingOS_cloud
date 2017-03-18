package com.zhenlaidian.ui.register;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
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
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.bean.SMSMassage;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.util.MyLog;
/**
 * 输入手机号填写验证码；
 * @author zhangyunfei
 * 2015年8月24日
 */
public class RegisterAsMobileActivity extends BaseActivity {

	private EditText et_mobile;
	private EditText et_code;
	private Button bt_get;
	private Button bt_next;
	private TextView tv_send;
	private String mobile = "";
	private boolean exit = false;
	private TimeCount mTimeCount;
	private BroadcastReceiver smsReceiver;
	private Timer timer;
	private TimerTask task;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		setContentView(R.layout.register_as_mobile_activity);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
		smsReceiver = new SmsReceiver();
		IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(IntentFilter.SYSTEM_LOW_PRIORITY);
		registerReceiver(smsReceiver, filter);
		mTimeCount = new TimeCount(60000, 1000);
		initVeiw();
		SetVeiw();
		timer= new Timer() ;
        task = new TimerTask(){
           public void run(){
        	   pullMessage(mobile);
            }
        };
	}
	
	public void initVeiw() {
		et_mobile = (EditText) findViewById(R.id.et_register_mobile_mobile);
		et_code = (EditText) findViewById(R.id.et_register_mobile_chick_code);
		bt_get = (Button) findViewById(R.id.bt_register_mobile_get);
		bt_next = (Button) findViewById(R.id.bt_register_mobile_next);
		tv_send = (TextView) findViewById(R.id.tv_register_mobile_send_message);
	}
	
	public void SetVeiw(){
		bt_get.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 点击获取验证码；
				if (et_mobile.getText() != null && isMobileNO(et_mobile.getText().toString().trim())) {
					mobile = et_mobile.getText().toString().trim();
					submitMobile(mobile);
					mTimeCount.start();
				}else {
					Toast.makeText(RegisterAsMobileActivity.this, "请正确输入您的手机号", 0).show();
				}
			}
		});
		bt_next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 点击下一步去注册位置信息
//				Intent intent  = new Intent(RegisterAsMobileActivity.this, RegisterAsDocumentPhotoActivity.class);
//				startActivity(intent);
//				RegisterAsMobileActivity.this.finish();
				if (et_code.getText() != null && ! et_code.getText().toString().trim().equals("")) {
					if ( mobile.equals("")) {
						Toast.makeText(RegisterAsMobileActivity.this, "请先输入手机号获取验证码！", 0).show();
					}else {
						submitCode(et_code.getText().toString().trim(), mobile);
					}
				}else {
					Toast.makeText(RegisterAsMobileActivity.this, "请输入您收到的短信验证码！", 0).show();
				}
			}
		});
		tv_send.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 收不到验证，发短信登录；
				if (et_mobile.getText() != null && isMobileNO(et_mobile.getText().toString().trim())) {
					mobile = et_mobile.getText().toString().trim();
					getCode(mobile);
				}else {
					Toast.makeText(RegisterAsMobileActivity.this, "请正确输入您的手机号", 0).show();
				}
			}
		});
	}
	
	
//	regparker.do?action=getcode&mobile=15801482643
// -1:注册失败,1:成功发送并保存验证码,-2:验证码保存失败,-3:发送验证码失败,-4:已注册,-5:已注册，但已禁用 -6 已注册

	public void submitMobile(String mobile){
		String url = baseurl +"regparker.do?action=getcode&mobile="+mobile;
		MyLog.w("RegisterAsMobileActivity", "获取验证码的URL---"+url);
		AQuery aQuery  = new AQuery(this);
		final ProgressDialog pd = ProgressDialog.show(this, "获取验证码...","网络请求中...", true, true);
		pd.setCanceledOnTouchOutside(false);
		aQuery.ajax(url, String.class, new AjaxCallback<String>(){

			@Override
			public void callback(String url, String object,AjaxStatus status) {
				if(status.getCode() == 200 && object != null){
					pd.dismiss();
					MyLog.i("RegisterAsMobileActivity", "获取验证码的返回结果"+object);
					if (object.equals("1")) {
						Toast.makeText(RegisterAsMobileActivity.this, "请填写您收到的短信验证码！", 1).show();
					}else if(object.equals("-4")){
						Toast.makeText(RegisterAsMobileActivity.this, "手机号已注册过，稍后请查阅短信！", 1).show();
					}else if(object.equals("-5")){
						Toast.makeText(RegisterAsMobileActivity.this, "手机号已注册过，稍后请查阅短信，联系停车宝客服！", 1).show();
					}else if(object.equals("-6")){
						Toast.makeText(RegisterAsMobileActivity.this, "手机号已注册过，稍后请查阅短信！", 1).show();
					}else{
						Toast.makeText(RegisterAsMobileActivity.this, "验证码获取失败，请重新点击获取按钮！", 0).show();
					}
				}else {
					pd.dismiss();
					if (status.getCode() == -101) {
						Toast.makeText(RegisterAsMobileActivity.this, "网络错误！--请再次点击获取按钮！", 0).show();
					}else if (status.getCode() == 500) {
						Toast.makeText(RegisterAsMobileActivity.this, "服务器错误！--请再次点击获取按钮！", 0).show();
					}else {
						Toast.makeText(RegisterAsMobileActivity.this, "网络请求错误!--请再次点击获取按钮！", 0).show();
					}
				}
			}
		});
	}
	
	
//	regparker.do?action=validcode&code=1580&mobile=15801482643
//	返回：1:验证码匹配成功,-1:验证码匹配失败,-2:用户不存在,-3:未产生验证码
	public void submitCode(String code,final String mobile){
		String url = baseurl +"regparker.do?action=validcode&code="+code+"&mobile="+mobile;
		MyLog.w("RegisterAsMobileActivity", "检查验证码的URL---"+url);
		AQuery aQuery  = new AQuery(this);
		final ProgressDialog pd = ProgressDialog.show(this, "提交验证码...","网络请求中...", true, true);
		pd.setCanceledOnTouchOutside(false);
		aQuery.ajax(url, String.class, new AjaxCallback<String>(){

			@Override
			public void callback(String url, String object,AjaxStatus status) {
				if(status.getCode() == 200 && object != null){
					pd.dismiss();
					MyLog.i("RegisterAsMobileActivity", "检查验证码的返回结果"+object);
					if (object.equals("1")) {
						SharedPreferences sp = getSharedPreferences("tingchebao", Context.MODE_PRIVATE);
						sp.edit().putString("regMobile", mobile).commit();
						Intent intent  = new Intent(RegisterAsMobileActivity.this, RegisterAsDocumentPhotoActivity.class);
						startActivity(intent);
						RegisterAsMobileActivity.this.finish();
					}else if(object.equals("-1")){
						Toast.makeText(RegisterAsMobileActivity.this, "验证码匹配失败，请重新获取验证码！", 1).show();
					}else{
						Toast.makeText(RegisterAsMobileActivity.this, "验证码无效，请重新获取验证码！", 1).show();
					}
				}else {
					pd.dismiss();
					if (status.getCode() == -101) {
						Toast.makeText(RegisterAsMobileActivity.this, "网络错误！--请再次点击下一步！", 0).show();
					}else if (status.getCode() == 500) {
						Toast.makeText(RegisterAsMobileActivity.this, "服务器错误！--请再次点击下一步！", 0).show();
					}else {
						Toast.makeText(RegisterAsMobileActivity.this, "网络请求错误!--请再次点击下一步！", 0).show();
					}
				}
			}
		});
	}
	
//	regparker.do?action=getmesgcode&mobile=15801482643
//	返回：{"mesg":"1","code":"248773","tomobile":"1069004270441"}
//	mesg:-1 收费员不存在且注册失败，1成功返回，-2：生成验证码失败
//	code:验证码，在短信内容中写在开头部分，前面不要有其它字符
//	tomobile:发送短信目的地
	public void getCode(final String mobile){
		String url = baseurl +"regparker.do?action=getmesgcode&mobile="+mobile;
		MyLog.w("RegisterAsMobileActivity", "获取发短信内容的URL---"+url);
		AQuery aQuery  = new AQuery(this);
		final ProgressDialog pd = ProgressDialog.show(this, "获取短信验证码...","网络请求中...", true, true);
		pd.setCanceledOnTouchOutside(false);
		aQuery.ajax(url, String.class, new AjaxCallback<String>(){

			@Override
			public void callback(String url, String object,AjaxStatus status) {
				if(status.getCode() == 200 && object != null){
					pd.dismiss();
					MyLog.i("RegisterAsMobileActivity", "获取发短信内容返回结果"+object);
					Gson gson = new Gson();
					SMSMassage info = gson.fromJson(object, SMSMassage.class);
					if (info != null && info.getMesg() != null && info.getCode() != null && info.getTomobile() != null) {
						if (info.getMesg().equals("1")) {
							sendSMS(info.getTomobile(), info.getCode());
							pullMessage(mobile);
						}else {
							Toast.makeText(RegisterAsMobileActivity.this, "发送短信内容获取失败！", 0).show();
						}
					}
				}else {
					pd.dismiss();
					if (status.getCode() == -101) {
						Toast.makeText(RegisterAsMobileActivity.this, "网络错误！--请再重试！", 0).show();
					}else if (status.getCode() == 500) {
						Toast.makeText(RegisterAsMobileActivity.this, "服务器错误！--请重试！", 0).show();
					}else {
						Toast.makeText(RegisterAsMobileActivity.this, "网络请求错误!--请重试！", 0).show();
					}
				}
			}
		});
	}
	
//	http://192.168.199.240/mserver/parkermesg.do?action=checkcode&mobile=15801482643  轮询去取发短信后的结果；0继续请求，1成功，-1失败，
	public void pullMessage(final String mobile){
		String url = Config.getMserver(this) +"parkermesg.do?action=checkcode&mobile="+mobile;
		MyLog.w("RegisterAsMobileActivity", "轮询获取短信验证的URL---"+url);
		AQuery aQuery  = new AQuery(this);
//		final ProgressDialog pd = ProgressDialog.show(this, "短信已发送等待验证结果...","网络请求中...", true, true);
//		pd.setCanceledOnTouchOutside(false);
		aQuery.ajax(url, String.class, new AjaxCallback<String>(){

			@Override
			public void callback(String url, String object,AjaxStatus status) {
				if(status.getCode() == 200 && object != null){
//					pd.dismiss();
					MyLog.i("RegisterAsMobileActivity", "轮询获取短信验证的结果"+object);
					if (object.equals("0")) {
						if (! exit) {
							 timer.schedule(task, 5000, 5000);
						}
					}else if (object.equals("1")) {
						timer.cancel();
						SharedPreferences sp = getSharedPreferences("tingchebao", Context.MODE_PRIVATE);
						sp.edit().putString("regMobile", mobile).commit();
						Intent intent  = new Intent(RegisterAsMobileActivity.this, RegisterAsDocumentPhotoActivity.class);
						startActivity(intent);
						RegisterAsMobileActivity.this.finish();
					}else if (object.equals("-1")) {
						timer.cancel();
						Toast.makeText(RegisterAsMobileActivity.this, "发送短信验证失败，请重试！", 0).show();
					}else {
						timer.cancel();
						Toast.makeText(RegisterAsMobileActivity.this, "发送短信验证失败，请重试！", 0).show();
					}
				}else {
//					pd.dismiss();
					if (status.getCode() == -101) {
						Toast.makeText(RegisterAsMobileActivity.this, "网络错误！--请再重试！", 0).show();
					}else if (status.getCode() == 500) {
						Toast.makeText(RegisterAsMobileActivity.this, "服务器错误！--请重试！", 0).show();
					}else {
						Toast.makeText(RegisterAsMobileActivity.this, "网络请求错误!--请重试！", 0).show();
					}
				}
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
			mDialog.setTitle("操作提示");
			mDialog.setMessage("您确定退出注册吗？");
			mDialog.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					timer.cancel();
					RegisterAsMobileActivity.this.finish();
				}
			});
			mDialog.setNegativeButton("取消", null);
			mDialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
    /** 
    * 发送短信 
    * @param smsBody 
    */  
	private void sendSMS(String number ,String smsBody) {
		Uri smsToUri = Uri.parse("smsto:"+number);
		Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
		intent.putExtra("sms_body", smsBody);
		startActivity(intent);
	}
	
	/** 
	 * 验证手机格式 
	 */  
	public static boolean isMobileNO(String mobiles) {
		/*
		 * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		 * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
		 * 中麦通信；170 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
		 */
		String telRegex = "[1][3578]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
		if (TextUtils.isEmpty(mobiles))
			return false;
		else
			return mobiles.matches(telRegex);
	}
	
	/**
	 * 倒计时器，用于控制"获取验证码"按钮点击后一分钟内不可再次点击
	 * 
	 * @author Clare
	 * 
	 */
	private class TimeCount extends CountDownTimer {

		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {// 计时完毕时触发
			bt_get.setText("重新获取");
			bt_get.setClickable(true);
			bt_get.setBackgroundResource(R.drawable.shape_login_button);
		}

		@Override
		public void onTick(long millisUntilFinished) {// 计时过程显示
			bt_get.setClickable(false);
			bt_get.setBackgroundResource(R.drawable.shape_register_as_mobile);
			bt_get.setText("重新获取" + "(" + millisUntilFinished / 1000 + "秒)");
		}
	}
	
	/**
	 * 短信的广播接收者，用于自动填写接收到的注册（或登陆）验证码
	 */
	private class SmsReceiver extends BroadcastReceiver // implements android.
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			Object[] pdus = (Object[]) intent.getExtras().get("pdus");
			for (Object p : pdus) {
				byte[] pdu = (byte[]) p;
				SmsMessage message = SmsMessage.createFromPdu(pdu);
				String smsBody = message.getMessageBody();
				String smsNumber = message.getDisplayOriginatingAddress();
				MyLog.w("RegisterAsMobileActivity", smsNumber + ":" + smsBody);
				if (!TextUtils.isEmpty(smsBody) && smsBody.contains("停车宝")
						&& smsBody.contains("验证码")) {
					int startIndex = smsBody.indexOf(":");
					String code = smsBody.substring(startIndex + 1, startIndex + 5);
					et_code.setText(code);
					et_code.setSelection(code.length());
				}
			}
		}
	}
	
	// 返回键退出时提示
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
			mDialog.setTitle("操作提示");
			mDialog.setMessage("您确定退出注册吗？");
			mDialog.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					timer.cancel();
					RegisterAsMobileActivity.this.finish();
				}
			});
			mDialog.setNegativeButton("取消", null);
			mDialog.show();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		exit = true;
		unregisterReceiver(smsReceiver);
	}
}
