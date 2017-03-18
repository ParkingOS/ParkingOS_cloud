package com.zhenlaidian.photo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.CarNumberMadeOrder;
import com.zhenlaidian.bean.MainUiInfo;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.ui.LostOrderRecordActivity;
import com.zhenlaidian.util.MyLog;

public class CheckNumberActivity extends BaseActivity {

	private ImageView iv_car_number;
	private EditText et_hand_write;
	private TextView tv_time;
	private Button bt_ok;
	private Button bt_again;
	private String result;
	private String ordertime;// 添加车牌号的入场时间
	private String orderid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check_number_activity);
		if (getIntent().getExtras() != null) {
			result = getIntent().getStringExtra("carnumber");
			ordertime = getIntent().getExtras().getString("ordertime");
			orderid = getIntent().getExtras().getString("orderid");

		}
		initVeiw();
		setveiw();
		DecodeManager.getinstance().destroyAllMemery();// 释放TCBso库文件资源；
		MyLog.i("CheckNumberActivity", " onCreate---释放TCB.so解析库文件!");
		LeaveActivity.isclosetcb = true;
	}

	private void initVeiw() {
		iv_car_number = (ImageView) findViewById(R.id.iv_check_car_number_img);
		et_hand_write = (EditText) findViewById(R.id.et_check_number_write);
		tv_time = (TextView) findViewById(R.id.tv_check_number_time);
		bt_ok = (Button) findViewById(R.id.bt_check_number_ok);
		if (ordertime != null) {
			bt_ok.setText("添加完成");
		}
		bt_again = (Button) findViewById(R.id.bt_check_number_again);
	}

	@SuppressLint("SimpleDateFormat")
	private void setveiw() {
		String imgfilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ATingCheBao/CarNumber.jpeg";
		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.displayImage("file://" + imgfilePath, iv_car_number);

		et_hand_write.setText(result);
		et_hand_write.setSelection(et_hand_write.getText().length());
		java.util.Date date = new java.util.Date();
		SimpleDateFormat timef = new SimpleDateFormat("HH:mm");
		if (ordertime != null) {
			tv_time.setTextSize(18.0f);
			tv_time.setText(ordertime);

		} else {
			tv_time.setText(timef.format(date));
		}

		bt_again.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CheckNumberActivity.this, MyCaptureActivity.class);
				startActivity(intent);
				CheckNumberActivity.this.finish();
			}
		});

		bt_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (ordertime != null) {
					try {
						addCarNumber(orderid);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						Toast.makeText(CheckNumberActivity.this, "添加车牌添加到当前订单失败！", 0).show();
						e.printStackTrace();
					}

				} else {
					try {
						madeOrder();
					} catch (UnsupportedEncodingException e) {
						Toast.makeText(CheckNumberActivity.this, "提交车牌字符转码异常！", 0).show();
						e.printStackTrace();
					}
				}
			}
		});
	}

	// 按车牌生成订单；正常查询是否有逃单
	// http://192.168.199.240/zld/cobp.do?action=preaddorder&comid=3&uid=100005&carnumber=aaabbdd
	public void madeOrder() throws UnsupportedEncodingException {
		SharedPreferences pfs = this.getSharedPreferences("autologin", Context.MODE_PRIVATE);
		String uid = pfs.getString("account", "");
		String carnumber = URLEncoder.encode(et_hand_write.getText().toString().trim(), "utf-8");
		String path = baseurl;
		String url = path + "cobp.do?action=preaddorder&comid=" + comid + "&uid=" + uid + "&carnumber="
				+ URLEncoder.encode(carnumber, "utf-8") + "&imei=" + imei;
		MyLog.w("CheckNumberActivity", "车牌识别生成订单的URL--->" + url);
		final ProgressDialog dialog = ProgressDialog.show(this, "生成订单", "提交订单数据中...", true, true);
		dialog.setCanceledOnTouchOutside(false);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				if (status.getCode() == 200 && object != null) {
					dialog.dismiss();
					MyLog.i("CheckNumberActivity", "车牌识别生成订单的结果--->" + object);
					Gson gson = new Gson();
					CarNumberMadeOrder info = gson.fromJson(object, CarNumberMadeOrder.class);
					if (info.getInfo().equals("1")) {
						// Toast.makeText(CheckNumberActivity.this,"车牌识别订单生成,可在当前订单查看！",
						// 0).show();
						CheckNumberActivity.this.finish();
					} else if (info.getInfo().equals("0")) {
						// Toast.makeText(CheckNumberActivity.this, "" + object,
						// 1).show();
						if (info.getOther() != null && info.getOwn() != null) {
							int parseInt = Integer.parseInt(info.getOwn());
							int parseInt2 = Integer.parseInt(info.getOther());
							int num = parseInt + parseInt2;
							LostOrderDialog(et_hand_write.getText().toString().trim() + "有" + num + "笔逃单,在您的车场逃单" + parseInt
									+ "次!");
						}
					} else if (info.getInfo().equals("-1")) {
						Toast.makeText(CheckNumberActivity.this, "车场编号错误！", 0).show();
					} else if (info.getInfo().equals("-2")) {
						Toast.makeText(CheckNumberActivity.this, et_hand_write.getText().toString().trim() + "存在未结算订单,请先结算！", 0)
								.show();
					}
				} else {
					dialog.dismiss();
					switch (status.getCode()) {
					case -101:
						Toast.makeText(CheckNumberActivity.this, "网络错误！--请再次确认车牌生成订单！", 0).show();
						break;
					case 500:
						Toast.makeText(CheckNumberActivity.this, "服务器错误！--请再次确认车牌生成订单！", 0).show();
						break;
					}
				}
			}
		});
	}

	// 按车牌生成订单； 强制直接生成订单
	// http://192.168.199.240/zld/cobp.do?action=addorder&comid=3&uid=100005&carnumber=aaabbdd
	public void addOrder() throws UnsupportedEncodingException {
		SharedPreferences pfs = this.getSharedPreferences("autologin", Context.MODE_PRIVATE);
		String uid = pfs.getString("account", "");
		String carnumber = URLEncoder.encode(et_hand_write.getText().toString().trim(), "utf-8");
		String path = baseurl;
		String url = path + "cobp.do?action=addorder&comid=" +comid + "&uid=" + uid + "&carnumber="
				+ URLEncoder.encode(carnumber, "utf-8") + "&imei=" + imei;
		MyLog.w("CheckNumberActivity", "车牌识别生成订单的URL--->" + url);
		final ProgressDialog dialog = ProgressDialog.show(this, "生成订单", "提交订单数据中...", true, true);
		dialog.setCanceledOnTouchOutside(false);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				if (status.getCode() == 200 && object != null) {
					dialog.dismiss();
					MyLog.i("CheckNumberActivity", "车牌识别生成订单的结果--->" + object);
					if (object.equals("1")) {
						Message msg = new Message();
						msg.what = 2;
						msg.obj = new MainUiInfo(true, 4, 1.00);
						LeaveActivity.handler.sendMessage(msg);
						Toast.makeText(CheckNumberActivity.this, "车牌识别订单生成,可在当前订单查看！", 0).show();
						CheckNumberActivity.this.finish();
					} else {
						Toast.makeText(CheckNumberActivity.this, "" + object, 1).show();
					}
				} else {
					dialog.dismiss();
					switch (status.getCode()) {
					case -101:
						Toast.makeText(CheckNumberActivity.this, "网络错误！--请再次确认车牌生成订单！", 0).show();
						break;
					case 500:
						Toast.makeText(CheckNumberActivity.this, "服务器错误！--请再次确认车牌生成订单！", 0).show();
						break;
					}
				}
			}
		});
	}

	// 添加车牌号；
	// cobp.do?action=addcarnumber&comid=10&orderid=&carnumber=
	public void addCarNumber(String orderid) throws UnsupportedEncodingException {

		String carnumber = URLEncoder.encode(et_hand_write.getText().toString().trim(), "utf-8");
		String path = baseurl;
		String url = path + "cobp.do?action=addcarnumber&comid=" + comid + "&orderid=" + orderid + "&carnumber="
				+ URLEncoder.encode(carnumber, "utf-8");
		MyLog.w("CheckNumberActivity", "车牌识别添加车牌号的URL--->" + url);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				if (status.getCode() == 200 && object != null) {
					MyLog.i("CheckNumberActivity", "车牌识别添加车牌号的结果--->" + object);
					if (object.equals("1")) {
						CheckNumberActivity.this.finish();
					} else if (object.equals("0")) {
						Toast.makeText(CheckNumberActivity.this, "添加失败-车牌号已存在订单！", 0).show();
					} else {
						Toast.makeText(CheckNumberActivity.this, "添加车牌失败" + object, 0).show();
					}
				} else {
					switch (status.getCode()) {
					case -101:
						Toast.makeText(CheckNumberActivity.this, "网络错误！--添加车牌失败！", 0).show();
						break;
					case 500:
						Toast.makeText(CheckNumberActivity.this, "服务器错误！--添加车牌失败！", 0).show();
						break;
					}
				}
			}
		});
	}

	// 有逃单提示对话框；
	public void LostOrderDialog(String warn) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setIcon(R.drawable.app_icon_32);
		builder.setTitle("订单尚未生成");
		builder.setMessage(warn);
		builder.setCancelable(false);
		builder.setPositiveButton("查看", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(CheckNumberActivity.this, LostOrderRecordActivity.class);
				intent.putExtra("carnumber", et_hand_write.getText().toString().trim());
				startActivity(intent);
				CheckNumberActivity.this.finish();
			}
		});
		builder.setNegativeButton("继续生成订单", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					addOrder();
				} catch (UnsupportedEncodingException e) {
					Toast.makeText(CheckNumberActivity.this, "提交车牌字符转码异常！", 0).show();
					e.printStackTrace();
				}
			}
		});
		builder.create().show();
	}

}
