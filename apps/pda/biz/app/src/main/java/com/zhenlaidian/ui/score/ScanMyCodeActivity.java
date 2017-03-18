/**
 * 
 */
package com.zhenlaidian.ui.score;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.lswss.QRCodeEncoder;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.RedPacketInfo;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.util.IsNetWork;

/**
 * 让车主扫我的二维码送券
 * 
 * @author zhangyunfei 2015年7月18日
 */
public class ScanMyCodeActivity extends BaseActivity {

	private ImageView iv_scan_code_code;// 二维码
	private TextView tv_scan_code_money;
	private TextView tv_scan_code_parkname;
	private TextView tv_scan_code_score;
	private Button bt_refresh_qrcode;
	private Button bt_change_money;
	public RedPacketInfo redpacket;// 选券后传过来的；
	private ArrayList<RedPacketInfo> info;
	public int mWhich = 0;
	public String[] qrcode = new String[2];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		setContentView(R.layout.activity_scan_my_code);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
		redpacket = (RedPacketInfo) getIntent().getExtras().getSerializable("redpacket");
		if (redpacket != null && !TextUtils.isEmpty(redpacket.getBmoney())) {
			qrcode[0] = redpacket.getBmoney();
			qrcode[1] = redpacket.getScore();
			getCode(qrcode[0], qrcode[1]);
		}
		initView();
		getRewardScoreInfo(false);
	}

	public void initView() {
		iv_scan_code_code = (ImageView) findViewById(R.id.iv_scan_code_code);
		tv_scan_code_money = (TextView) findViewById(R.id.tv_scan_code_money);
		tv_scan_code_parkname = (TextView) findViewById(R.id.tv_scan_code_parkname);
		tv_scan_code_score = (TextView) findViewById(R.id.tv_scan_code_score);
		bt_refresh_qrcode = (Button) findViewById(R.id.bt_scan_refersh_qrcode);
		bt_change_money = (Button) findViewById(R.id.bt_scan_change_money);
		bt_refresh_qrcode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (qrcode[0].equals("3")) {
					getCode(qrcode[0], qrcode[1]);
				} else {
					getRewardScoreInfo(true);
				}
			}
		});
		bt_change_money.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showTicketDialog();
			}
		});
		if (redpacket != null) {
			tv_scan_code_money.setText(redpacket.getBmoney() + ".00");
			tv_scan_code_score.setText(Html.fromHtml("消耗收费员积分：<font color='#ff0000'>" + redpacket.getScore() + "</font>"));
		}
	}

	public void setView(String cname, String code) {
		tv_scan_code_parkname.setText(cname + "专用券");
		QRCodeEncoder d = new QRCodeEncoder();
		Bitmap codebmp = d.encode2BitMap(code, 400, 400);
		iv_scan_code_code.setImageBitmap(codebmp);
	}

	// 59：扫码领停车券
	// collectorrequest.do?action=sweepticket&bmoney=3&score=1&token=5286f078c6d2ecde9b30929f77771149
	// 输入参数：
	// bmoney：券金额
	// score：消耗积分
	// 返回值类型：JSON对象
	// result：-1：出错了 1：成功 -3积分不足
	// code：券二维码
	// cname：券二维码
	public void getCode(String money, String score) {
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "请检查网络", 0).show();
			return;
		}
		String url = baseurl + "collectorrequest.do?action=sweepticket&bmoney=" + money + "&score=" + score + "&token="
				+ token;
		Log.e("ScanMyCodeActivity", "扫码领停车券的URL--->" + url);
		final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取停车券二维码...", true, true);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				dialog.dismiss();
				if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
					Log.e("ScanMyCodeActivity", "获取到扫码领停车券--->" + object);
					try {
						JSONObject json = new JSONObject(object);
						if (json != null) {
							int result = json.getInt("result");
							switch (result) {
							case 1:
								setView(json.getString("cname"), json.getString("code"));
								break;
							case -1:
								iv_scan_code_code.setImageResource(R.drawable.net_error_noqrcode);
								Toast.makeText(ScanMyCodeActivity.this, "出错了，请点击刷新二维码", 0).show();
								break;
							case -3:
								iv_scan_code_code.setImageResource(R.drawable.net_error_noqrcode);
								Toast.makeText(ScanMyCodeActivity.this, "积分不足", 0).show();
								break;
							}
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					switch (status.getCode()) {
					case 500:
						Toast.makeText(ScanMyCodeActivity.this, "服务器错误！", 0).show();
						break;
					case 404:
						Toast.makeText(ScanMyCodeActivity.this, "服务器不可用！", 0).show();
						break;
					}
					Toast.makeText(ScanMyCodeActivity.this, "网络请求错误！", 0).show();
				}
			}
		});
	}

	/**
	 * 弹出选择专用券金额的对话框；
	 */
	public void showTicketDialog() {
		if (info == null || info.size() == 0) {
			Toast.makeText(this, "获取金额失败，请重新进入此界面", 0).show();
			return;
		}
		int size = 0;
		for (int i = 0; i < info.size(); i++) {
			if (info.get(i).getType().equals("1")) {
				size = size + 1;
			}
		}
		final String[] tickets = new String[size];
		for (int i = 0; i < info.size(); i++) {
			if (info.get(i).getType().equals("1")) {
				if (tv_scan_code_money.getText().toString().equals(info.get(i).getBmoney() + ".00")) {
					mWhich = i;
				}
				tickets[i] = info.get(i).getBmoney() + "元";
			}
		}
		new AlertDialog.Builder(this).setTitle("请选择专用券面值").setIcon(R.drawable.app_icon_32)
				.setSingleChoiceItems(tickets, mWhich, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						mWhich = which;
						tv_scan_code_money.setText(tickets[which].replace("元", ".00"));
						if ("5".equals(tickets[which].replace("元", ""))) {
							getRewardScoreInfo(true);
						} else {
							for (int i = 0; i < info.size(); i++) {
								if (info.get(i).getType().equals("1")) {
									if (info.get(i).getBmoney().equals(tickets[which].replace("元", ""))) {
										tv_scan_code_score.setText(Html.fromHtml("消耗收费员积分：<font color='#ff0000'>"
												+ info.get(i).getScore() + "</font>"));
										qrcode[0] = tickets[which].replace("元", "");
										qrcode[1] = info.get(i).getScore();
										getCode(qrcode[0], qrcode[1]);
									}
								}
							}
						}
						dialog.dismiss();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create().show();
	}

	// 获取红包详情collectorrequest.do?action=bonusinfo&token=
	public void getRewardScoreInfo(final Boolean isfive) {
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "请检查网络", 0).show();
			return;
		}
		String url = baseurl + "collectorrequest.do?action=bonusinfo&token=" + token;
		Log.e("ScanMyCodeActivity", "获取红包详情的URL--->" + url);
		// final ProgressDialog dialog = ProgressDialog.show(this, "获取红包详情",
		// "加载中...", true, true);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
					Log.e("ScanMyCodeActivity", "获取到红包详情--->" + object);
					Gson gson = new Gson();
					info = gson.fromJson(object, new TypeToken<ArrayList<RedPacketInfo>>() {
					}.getType());
					Log.e("ScanMyCodeActivity", "解析到红包详情--->" + info.toString());
					if (isfive && info != null && info.size() > 0) {
						for (int i = 0; i < info.size(); i++) {
							if (info.get(i).getType().equals("1")) {
								if ("5".equals(info.get(i).getBmoney())) {

									if (info.get(i).getLimit() != null && "1".equals(info.get(i).getLimit())) {
										tv_scan_code_money.setText("3.00");
										tv_scan_code_score.setText(Html
												.fromHtml("消耗收费员积分：<font color='#ff0000'>" + 6 + "</font>"));
										Toast.makeText(ScanMyCodeActivity.this, "今日五元券发放数量已达到上限", 0).show();
										qrcode[0] = "3";
										qrcode[1] = "6";
										return;
									} else {
										tv_scan_code_score.setText(Html.fromHtml("消耗收费员积分：<font color='#ff0000'>"
												+ info.get(i).getScore() + "</font>"));
										qrcode[0] = "5";
										qrcode[1] = info.get(i).getScore();
									}
								}
							}
						}
						getCode(qrcode[0], qrcode[1]);
					}
				} else {
					switch (status.getCode()) {
					case 500:
						Toast.makeText(ScanMyCodeActivity.this, "服务器错误！", 0).show();
						break;
					case 404:
						Toast.makeText(ScanMyCodeActivity.this, "服务器不可用！", 0).show();
						break;
					}
					Toast.makeText(ScanMyCodeActivity.this, "网络请求错误！", 0).show();
				}
			}
		});
	}

	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			ScanMyCodeActivity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
