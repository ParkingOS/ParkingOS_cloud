package com.zhenlaidian.ui;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.ParkingInfo;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
/**
 * 修改停车场信息
 * @author zhangyunfei
 * 2015年8月24日
 */
public class ChangeParkingInfo extends BaseActivity {
	private static final String[] parkType = { "地面停车场", "地下停车场", "路边占道停车" };
	private static final String[] ParkSpaceType = { "平面排列", "立体排列" };
	private ArrayAdapter<String> parkTypeAdapter;
	private ArrayAdapter<String> ParkSpaceTypeAdapter;
	private Spinner sp_parkType; // 车场类型
	private Spinner sp_parkSpaceType; // 车位类型
	private EditText et_parkname; // 车场名称
	private EditText et_address;// 车场地址
	private EditText et_parktotal;// 车位总数
	private EditText et_phone;// 车场电话
	private EditText et_detail;// 车场详情
	private Button bt_change;
	private Button bt_my_location;//我的位置
	private String park_type; // 车场类型
	private String parkSpace_type;// 车位类型
	private ParkingInfo parkinfo;
	private String uin;//登录账号；
	private final int LOCATION = 20;
	private String address;
	private double longitude;//经度
	private double latitude ;//纬度
	private double[] location ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub  
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		setContentView(R.layout.change_parkinginfo_activity);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.show();
		parkinfo = (ParkingInfo) getIntent().getExtras().getSerializable("parkinfo");
		initView();
		// 将可选内容与ArrayAdapter连接起来
		parkTypeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, parkType);
		ParkSpaceTypeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, ParkSpaceType);
		// 设置下拉列表的风格
		parkTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ParkSpaceTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		sp_parkType.setAdapter(parkTypeAdapter);
		sp_parkSpaceType.setAdapter(ParkSpaceTypeAdapter);
		// 添加事件Spinner事件监听
		sp_parkType.setOnItemSelectedListener(new ParkTypeSpinnerSelectedListener());
		sp_parkSpaceType.setOnItemSelectedListener(new ParkSpaceTypeSpinnerSelectedListener());
		setView();
		bt_change.setOnClickListener(new OnClickListener() {
			long lasttime;
			@Override
			public void onClick(View arg0) {
				if (System.currentTimeMillis() - lasttime >= 2000) {
					submit();
				}
				lasttime = System.currentTimeMillis();
			}
		});
		bt_my_location.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if ( parkinfo.getIsfixed() != null && parkinfo.getIsfixed().equals("1") ) {
					Toast.makeText(ChangeParkingInfo.this, "车场位置已审核，要修改请联系客服！", 0).show();
				}else if (parkinfo.getIsfixed() != null && parkinfo.getIsfixed().equals("0")) {
//					Intent intent = new Intent(ChangeParkingInfo.this, LocationActivity.class);
//					intent.putExtra("parkLatLng", location);
//					startActivityForResult(intent,LOCATION );
				}
				
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
	
		MyLog.w("RegisterActivity", "onActivityResult.........");
		if (resultCode == RESULT_OK ) {
			address = data.getStringExtra("address");
			if (address != null) {
				et_address.setText(address);
				MyLog.i("ChangeParkingInfo", "获取的地址是："+address);
			}
			double[] doubleArray = data.getExtras().getDoubleArray("latlng");
			if (doubleArray != null) {
				latitude = doubleArray[0];
				longitude = doubleArray[1];
				MyLog.i("ChangeParkingInfo", "获取的经纬度是："+"latitude"+latitude+"---longitude"+longitude);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			ChangeParkingInfo.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	// 停车场类型选择监听
	class ParkTypeSpinnerSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			park_type = arg2 + "";
			MyLog.i("ChangeParkingInfo", "车场类型选择位置是" + arg2);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	// 车位类型选择监听
	class ParkSpaceTypeSpinnerSelectedListener implements
			OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			parkSpace_type = arg2 + "";
			MyLog.i("ChangeParkingInfo", "车位类型选择位置是" + arg2);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	public void initView() {
		SharedPreferences autologin = getSharedPreferences("autologin",Context.MODE_PRIVATE);
		uin = autologin.getString("account", "");
		et_parkname = (EditText) findViewById(R.id.et_parking_name);
		et_address = (EditText) findViewById(R.id.et_parking_address);
		et_parktotal = (EditText) findViewById(R.id.et_parking_total);
		et_phone = (EditText) findViewById(R.id.et_parking_phone);
		et_detail = (EditText) findViewById(R.id.et_parking_detail);
		bt_change = (Button) findViewById(R.id.bt_parkinginfo_submit);
		bt_my_location = (Button) findViewById(R.id.bt_parking_location);
		sp_parkSpaceType = (Spinner) findViewById(R.id.sp_change_parkinfo_stoptype);
		sp_parkType = (Spinner) findViewById(R.id.sp_change_parkinfo_parktype);
		
	}

	public void setView() {
		if (parkinfo == null && parkinfo.getName() == null && parkinfo.getAddress() == null && parkinfo.getParkingtotal() == null) {
			return;
		}
		if (parkinfo.getLatitude() != null && parkinfo.getLongitude() != null) {
			latitude= Double.parseDouble(parkinfo.getLatitude());
			longitude = Double.parseDouble(parkinfo.getLongitude());
			location = new double[]{latitude,longitude};
		}
		et_parkname.setText(parkinfo.getName());
		et_address.setText(parkinfo.getAddress());
		et_parktotal.setText(parkinfo.getParkingtotal());
		if (parkinfo.getPhone() != null) {
			et_phone.setText(parkinfo.getPhone());
		}
		if (parkinfo.getResume() != null) {
			et_detail.setText(parkinfo.getResume());
		}
		if (parkinfo.getParktype() != null && parkinfo.getStoptype() != null) {
			if (parkinfo.getParktype().equals("地面")) {
				sp_parkType.setSelection(0);
			}
			if (parkinfo.getParktype().equals("地下")) {
				sp_parkType.setSelection(1);
			}
			if (parkinfo.getParktype().equals("占道")) {
				sp_parkType.setSelection(2);
			}
			if (parkinfo.getStoptype().equals("平面排列")) {
				sp_parkSpaceType.setSelection(0);
			}
			if (parkinfo.getStoptype().equals("立体排列")) {
				sp_parkSpaceType.setSelection(0);
			}
		}
	}
	//http://192.168.1.102/zld/parking.do?action=edit&uin=&from=client
	public void submit() {
		String path = baseurl;
		String url = path +"parking.do?action=edit&uin="+uin;
		Map<String, String> params = new HashMap<String, String>();
		try {
			params.put("company_name",URLEncoder.encode(et_parkname.getText().toString().trim(), "utf-8"));
			params.put("address", URLEncoder.encode(et_address.getText().toString().trim(), "utf-8"));
			params.put("resume", URLEncoder.encode(et_detail.getText().toString().trim(), "utf-8"));
			params.put("parking_total", et_parktotal.getText().toString().trim());
			params.put("phone", et_phone.getText().toString().trim());
			params.put("parking_type",park_type);
			params.put("stop_type", parkSpace_type);
			params.put("id", parkinfo.getId());
			params.put("latitude", latitude + "");//纬度
			params.put("longitude",longitude + "");//经度
		} catch (Exception e) {
			e.printStackTrace();
		}
		MyLog.w("ChangeParkingInfo", "修改的车场信息--->"+url+params.toString());
		AQuery aQuery  = new AQuery(this);
		if (IsNetWork.IsHaveInternet(ChangeParkingInfo.this)) {
			final ProgressDialog dialog = ProgressDialog.show(this, "提交中...", "提交修改信息...");
			aQuery.ajax(url, params, String.class, new AjaxCallback<String>(){

				@Override
				public void callback(String url, String object,AjaxStatus status) {
					if (object != null && object != "") {
						MyLog.i("ChangeParkingInfo", "返回的信息是--->"+object);
						dialog.dismiss();
						if (object.equals("1")) {
							Toast.makeText(ChangeParkingInfo.this, "修改成功！", 0).show();
							Message msg = new Message();
							msg.what = 3;
							LeaveActivity.handler.sendMessage(msg);
							Intent intent = new Intent(ChangeParkingInfo.this, ParkingActivity.class);
							ChangeParkingInfo.this.startActivity(intent);
							finish();
						}else if(object.equals("0")) {
							Toast.makeText(ChangeParkingInfo.this, "修改失败！", 0).show();
						}
					}else {
						dialog.dismiss();
					}
				}
				
			});
		}else {
			Toast.makeText(ChangeParkingInfo.this, "修改失败，网络不给力！", 0).show();
		}
	}
}
