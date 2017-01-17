package com.tq.zld.view.map;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.QRCodeEncoder;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.view.LoginActivity;

import java.util.HashMap;

public class RecommendActivity extends BaseActivity implements OnClickListener {

	private View tvSMS;
	private ImageView ivQRCode;
	private String registerUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recommend);
		initActionBar();
		initView();
		getRecommendUrl();
	}

	private void getRecommendUrl() {
		showProgressDialog("", true, false);
		// carowner.do?action=regcarmsg&mobile=%@
		String url = TCBApp.mServerUrl + "carowner.do";
		LogUtils.i(getClass(), "获取推荐短连接url: --->> " + url);
		HashMap<String, String> params = new HashMap<>();
		params.put("action", "regcarmsg");
		params.put("mobile", TCBApp.mMobile);
		new AQuery(this).ajax(url, params, String.class,
				new AjaxCallback<String>() {
					@Override
					public void callback(String url, String object,
							AjaxStatus status) {
						LogUtils.i(RecommendActivity.class,
								"获取推荐短连接result: --->> " + object);
						dismissProgressDialog();
						if (!TextUtils.isEmpty(object)) {
							registerUrl = object;
							refreshView(registerUrl);
						} else {
							Toast.makeText(RecommendActivity.this,
									"网络异常，请稍后再试...", Toast.LENGTH_SHORT).show();
						}
						super.callback(url, object, status);
					}
				});
	}

	@Override
	protected void onResume() {
		if (TextUtils.isEmpty(TCBApp.mMobile)) {
			Toast.makeText(this, "请先登录！", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(TCBApp.getAppContext(),
					LoginActivity.class));
		}
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("推荐记录")
				.setIntent(
						new Intent(getApplicationContext(),
								RecommendRecordActivity.class))
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	private void initView() {
		((TextView) findViewById(R.id.tv_recommend_1))
				.setText(Html
						.fromHtml("<font color='#329762'>推荐一个<big>收费员，</big>成功后<br>奖励您30元</font>"));
		ivQRCode = (ImageView) findViewById(R.id.iv_recommend_qrcode);
		tvSMS = findViewById(R.id.tv_recommend_sms);
		tvSMS.setOnClickListener(this);
	}

	private void initActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("推荐收费员");
		getActionBar().show();
	}

	private void refreshView(String url) {
		url = TextUtils.isEmpty(url) ? "http://www.tingchebao.com" : url;
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels * 3 / 5;
		ivQRCode.setImageBitmap(new QRCodeEncoder().encode2BitMap(url, width,
				width));
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_recommend_sms:
			sendSMS(registerUrl);
			break;
		}
	}

	private void sendSMS(String url) {
		Uri smsToUri = Uri.parse("smsto:" + "");
		Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
		intent.putExtra("sms_body", "停车宝收费，注册送10元，每单奖两元！注册地址：" + url);
		startActivity(intent);
	}
}
