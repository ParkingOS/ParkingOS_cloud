package com.zhenlaidian.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.ParkPriceInfo;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

public class ShowPriceSettingActivity extends BaseActivity {

	private TextView tv_time_day;// 日间时段
	private TextView tv_day_priceType;// 日间价格单位
	private TextView tv_night_priceType;// 夜间价格单位
	private TextView tv_time_night; // 夜间时段
	private TextView tv_first_time; // 日间首优惠时间
	private TextView tv_out_time; // 日间首优惠外
	private TextView tv_nignt_first_time; // 夜间首优惠时间
	private TextView tv_nignt_out_time; // 夜间首优惠外
	private TextView tv_day_fprice;// 日间首优惠价格
	private TextView tv_day_price;// 日间价格
	private TextView tv_night_fprice;// ----夜间首优惠价格
	private TextView tv_night_price;// 夜间价格
	private TextView tv_freeTime; // 日间可免费时长
	private TextView tv_nfreeTime; // 夜间可免费时长
	private CheckBox cb_day_ftime;
	private CheckBox cb_night_ftime;
	private CheckBox cb_night_sotp;
	private ImageView iv_no_night_stop;//不支持夜间停车
	private Button bt_ok;
	private ParkPriceInfo priceInfo;
	private ActionBar actionBar;
	private SharedPreferences autologin; 
	private String role;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		setContentView(R.layout.show_price_setting_activity);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
		autologin = getSharedPreferences("autologin",Context.MODE_PRIVATE);
		role = autologin.getString("role", "0");
		initView();
		getPrice();
		
	}

	private void initView() {
		MyLog.e("ShowPriceSettingActivity", "接收的comid是------->" + comid);
		tv_time_day = (TextView) findViewById(R.id.tv_price_setting_day);
		tv_time_night = (TextView) findViewById(R.id.tv_show_price_setting_night);
		tv_day_priceType = (TextView) findViewById(R.id.tv_price_stting_day_price);
		tv_night_priceType = (TextView) findViewById(R.id.tv_price_setting_night_price);
		tv_first_time = (TextView) findViewById(R.id.tv_price_setting_first_time);
		tv_nignt_first_time = (TextView) findViewById(R.id.tv_price_setting_Nfirst_time);
		tv_nignt_out_time = (TextView) findViewById(R.id.tv_price_setting_show_Nout_time);
		tv_out_time = (TextView) findViewById(R.id.tv_show_price_setting_out_time);
		tv_freeTime = (TextView) findViewById(R.id.tv_price_setting_freeTime);
		tv_nfreeTime = (TextView) findViewById(R.id.tv_price_setting_NfreeTime);
		tv_day_fprice = (TextView) findViewById(R.id.tv_price_setting_day_inTime);
		tv_day_price = (TextView) findViewById(R.id.tv_price_setting_day_outTime);
		tv_night_fprice = (TextView) findViewById(R.id.tv_price_setting_night_inTime);
		tv_night_price = (TextView) findViewById(R.id.tv_price_setting_night_outTime);
		bt_ok = (Button) findViewById(R.id.bt_show_price_setting_ok);
		cb_day_ftime = (CheckBox) findViewById(R.id.cb_show_price_setting_day_ftime);
		cb_night_ftime = (CheckBox) findViewById(R.id.cb_show_price_setting_night_ftime);
		cb_night_sotp = (CheckBox) findViewById(R.id.cb_show_is_night_parking);
		iv_no_night_stop = (ImageView) findViewById(R.id.iv_price_setting_show_night_null);
		
	}
	//{"id":"8","price":"3.00","unit":"30","pay_type":"0","b_time":"8","e_time":"18","first_times":"60","fprice":"2.50",
//	"countless":"5","nprice":"2.00","nuint":"60","nid":"9"}
	public void setView(ParkPriceInfo info) {
		if (info.getB_time() != null) {
			if (Integer.parseInt(info.getB_time()) >= 10) {
				tv_time_day.setText("("+info.getB_time()+":00-"+info.getE_time()+":00)");
			}else {
				tv_time_day.setText("(0"+info.getB_time()+":00-"+info.getE_time()+":00)");
			}
			if (Integer.parseInt(info.getB_time()) >= 10) {
				tv_time_night.setText("("+info.getE_time()+":00-"+info.getB_time()+":00)");
			}else {
				tv_time_night.setText("("+info.getE_time()+":00-0"+info.getB_time()+":00)");
			}
		}
		if (info.getUnit() != null) {
			if (info.getUnit().equals("15")) {
				tv_day_priceType.setText("元/"+info.getUnit()+"分钟");
			}else if (info.getUnit().equals("30")) {
				tv_day_priceType.setText("元/"+info.getUnit()+"分钟");
			}else if (info.getUnit().equals("60")) {
				tv_day_priceType.setText("元/1小时");
			}else if (info.getUnit().equals("120")) {
				tv_day_priceType.setText("元/2小时");
			}
		}
		if (info.getNuint() != null) {
			if (info.getNuint().equals("60")) {
				tv_night_priceType.setText("元/1小时");
			}else if (info.getNuint().equals("120")) {
				tv_night_priceType.setText("元/2小时");
			}else if (info.getNuint().equals("180")) {
				tv_night_priceType.setText("元/3小时");
			}else if (info.getNuint().equals("30")) {
				tv_night_priceType.setText("元/30分钟");
			}else if (info.getNuint().equals("15")) {
				tv_night_priceType.setText("元/15分钟");
			}
		}
		if (info.getFirst_times() != null ) {
			if (info.getFirst_times().equals("15")) {
				tv_first_time.setText(info.getFirst_times()+"分内");
				tv_out_time.setText(info.getFirst_times()+"分外");
			}else if (info.getFirst_times().equals("30")) {
				tv_first_time.setText(info.getFirst_times()+"分内");
				tv_out_time.setText(info.getFirst_times()+"分外");
			}else if (info.getFirst_times().equals("60")) {
				tv_first_time.setText("1小时内");
				tv_out_time.setText("1小时外");
			}else if (info.getFirst_times().equals("120")) {
				tv_first_time.setText("2小时内");
				tv_out_time.setText("2小时外");
			}else if (info.getFirst_times().equals("180")) {
				tv_first_time.setText("3小时内");
				tv_out_time.setText("3小时外");
			}
		}
		// "30分内","1小时内","2小时内", "3小时内" 
		if (info.getNfirst_times() != null ) {
			if (info.getNfirst_times().equals("30")) {
				tv_nignt_first_time.setText(info.getNfirst_times()+"分内");
				tv_nignt_out_time.setText(info.getNfirst_times()+"分外");
			}else if (info.getNfirst_times().equals("60")) {
				tv_nignt_first_time.setText("1小时内");
				tv_nignt_out_time.setText("1小时内");
			}else if (info.getNfirst_times().equals("120")) {
				tv_nignt_first_time.setText("2小时内");
				tv_nignt_out_time.setText("2小时外");
			}else if (info.getNfirst_times().equals("180")) {
				tv_nignt_first_time.setText("3小时内");
				tv_nignt_out_time.setText("3小时外");
			}else if (info.getNfirst_times().equals("0")) {
				info.setNfirst_times("120");
				tv_nignt_first_time.setText("2小时内");
				tv_nignt_out_time.setText("2小时外");
			}
		}
		if (info.getFree_time() != null) {
			tv_freeTime.setText(info.getFree_time());
		}
		if (info.getNfree_time() != null) {
			tv_nfreeTime.setText(info.getNfree_time());
		}
		if (info.getFprice() != null) {
			tv_day_fprice.setText(info.getFprice());
		}
		if (info.getPrice() != null) {
			tv_day_price.setText(info.getPrice());
		}
		if (info.getNfprice()!= null) {
			if (info.getNfprice().equals("0.00")) {
				tv_night_fprice.setText(info.getNprice());
				info.setNfprice(info.getNprice());
			}else {
				tv_night_fprice.setText(info.getNfprice());
			}
		}
		if (info.getNprice() != null) {
			tv_night_price.setText(info.getNprice());
		}
		if (info.getIsnight() != null) {
			if (info.getIsnight().equals("0")) {//0:支持，1不支持
				cb_night_sotp.setChecked(true);
				cb_night_sotp.setClickable(false);
			}else if (info.getIsnight().equals("1")) {
				cb_night_sotp.setChecked(false);
				cb_night_sotp.setClickable(false);
				iv_no_night_stop.setVisibility(View.VISIBLE);
			}
		}
		if (info.getFpay_type() != null) {//1免费 0 收费
			if (info.getFpay_type().equals("0")) {
				cb_day_ftime.setChecked(true);
				cb_day_ftime.setClickable(false);
				MyLog.w("ShowPriceSettingActivity", "日间超出临停收完整费用");
			}else if (info.getFpay_type().equals("1")) {
				cb_day_ftime.setChecked(false);
				cb_day_ftime.setClickable(false);
				MyLog.w("ShowPriceSettingActivity", "日间超出临停开始收费");
			}
		}
		if (info.getNfpay_type() != null) {
			if (info.getNfpay_type().equals("0")) {
				cb_night_ftime.setChecked(true);
				cb_night_ftime.setClickable(false);
				MyLog.w("ShowPriceSettingActivity", "夜间超出临停收完整费用");
			}else if (info.getNfpay_type().equals("1")) {
				cb_night_ftime.setChecked(false);
				cb_night_ftime.setClickable(false);
				MyLog.w("ShowPriceSettingActivity", "夜间超出临停开始收费");
			}
		}
	}

	// http://192.168.1.148/zld/parkedit.do?action=queryprice&comid=3 查询价格；
	private void getPrice() {
		String path = baseurl;
		String url = path + "parkedit.do?action=queryprice&comid=" + comid;
		MyLog.w("ShowPriceSettingActivity", "查询定价标准的URL--->" + url);
		AQuery aQuery = new AQuery(this);
		if (IsNetWork.IsHaveInternet(ShowPriceSettingActivity.this)) {
			final ProgressDialog dialog = ProgressDialog.show(this, "定价详情...","获取定价信息...");
			aQuery.ajax(url, String.class, new AjaxCallback<String>() {

				@Override
				public void callback(String url, String object,AjaxStatus status) {
					if (object != null && object != "" && object.length() > 10 ) {
						MyLog.v("ShowPriceSettingActivity", "返回的信息是--->" + object);
						dialog.dismiss();
						Gson gson = new Gson();
						priceInfo = gson.fromJson(object,ParkPriceInfo.class);
						MyLog.i("ShowPriceSettingActivity", "解析到的定价信息是--->" + priceInfo.toString());
						if (priceInfo.getB_time()!= null && priceInfo.getCountless() != null) {
							setView(priceInfo);
							bt_ok.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									
									if (role != null && role.equals("1")) {
										Intent priceintent = new Intent(ShowPriceSettingActivity.this,PriceSettingActivity.class);
										priceintent.putExtra("comid", comid);
										priceintent.putExtra("change", "change");
										priceintent.putExtra("priceInfo", priceInfo);
										ShowPriceSettingActivity.this.startActivity(priceintent);
										finish();
									}else {
										Toast.makeText(ShowPriceSettingActivity.this, "请用管理员账号登录修改价格！", 0).show();
									}
								}
							});
						}else {
//							Toast.makeText(ShowPriceSettingActivity.this, "点击修改设定价格！", 0).show();
							bt_ok.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									if (role != null && role.equals("1")) {
										Intent priceintent = new Intent(ShowPriceSettingActivity.this,PriceSettingActivity.class);
										priceintent.putExtra("comid", comid);
										priceintent.putExtra("changeNull", "changeNull");
										ShowPriceSettingActivity.this.startActivity(priceintent);
										finish();
									}else {
										Toast.makeText(ShowPriceSettingActivity.this, "请用管理员账号登录修改价格！", 0).show();
									}
								}
							});
						}
					} else {
						dialog.dismiss();
//						Toast.makeText(ShowPriceSettingActivity.this, "点击修改设定价格！", 0).show();
						bt_ok.setOnClickListener(new OnClickListener() {
							long lasttime;
							@Override
							public void onClick(View v) {
								if (role != null && role.equals("1")) {
									Intent priceintent = new Intent(ShowPriceSettingActivity.this,PriceSettingActivity.class);
									priceintent.putExtra("comid", comid);
									priceintent.putExtra("changeNull", "changeNull");
									ShowPriceSettingActivity.this.startActivity(priceintent);
									finish();
								}else {
									if (System.currentTimeMillis() - lasttime >= 1000) {
										Toast.makeText(ShowPriceSettingActivity.this, "请用管理员账号登录修改价格！", 0).show();
									}
								}
								lasttime = System.currentTimeMillis();
							}
						});
					}
				}

			});
		} else {
			Toast.makeText(ShowPriceSettingActivity.this, "定价获取失败，网络不给力！", 0).show();
		}
	}
	
	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			ShowPriceSettingActivity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
}
