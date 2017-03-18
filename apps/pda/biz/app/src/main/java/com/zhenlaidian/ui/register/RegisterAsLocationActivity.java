package com.zhenlaidian.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.zhenlaidian.R;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.util.MyLog;
/**
 * 填写车场信息和定位坐标并上传；
 * @author zhangyunfei
 * 2015年8月24日
 */
public class RegisterAsLocationActivity extends BaseActivity {

	private EditText et_name;
	private EditText et_park_name;
	private EditText et_park_address;
	private EditText et_park_location;
	private EditText et_park_total;
	private Button bt_location;
	private Button bt_next;
	private final int LOCATION = 10;
	private double latitude = 4.9E-324;//经度
	private double longitude = 4.9E-324;//纬度
	private String address;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_location_activity);
		initview();
		setView();
	}
	
	public void initview(){
		et_name = (EditText) findViewById(R.id.et_register_location_name);
		et_park_name = (EditText) findViewById(R.id.et_register_location_parkname);
		et_park_address = (EditText) findViewById(R.id.et_register_location_parkaddress);
		et_park_location = (EditText) findViewById(R.id.et_register_location_location);
		et_park_total = (EditText) findViewById(R.id.et_register_location_parktotal);
		bt_location = (Button) findViewById(R.id.bt_register_location_mylocation);
		bt_next = (Button) findViewById(R.id.bt_register_location_next);
	}
	
	public void setView(){
		bt_location.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 去地图界面获取经纬度；
//				Intent intent = new Intent(RegisterAsLocationActivity.this, LocationActivity.class);
//				startActivityForResult(intent,LOCATION );
			}
		});
		bt_next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 点击下一步去确定车场；
				Intent intent = new Intent(RegisterAsLocationActivity.this, RegisterAsDocumentPhotoActivity.class);
				startActivity(intent);
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
				et_park_location.setText(address);
				MyLog.i("RegisterActivity", "获取的地址是："+address);
			}
			double[] doubleArray = data.getExtras().getDoubleArray("latlng");
			if (doubleArray != null) {
				latitude = doubleArray[0];
				longitude = doubleArray[1];
				MyLog.i("RegisterActivity", "获取的经纬度是："+"latitude"+latitude+"---longitude"+longitude);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
