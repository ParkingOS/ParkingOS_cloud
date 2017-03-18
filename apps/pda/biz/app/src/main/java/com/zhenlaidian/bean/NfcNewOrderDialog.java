package com.zhenlaidian.bean;

import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.engine.ShowNfcNewOrder;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.PlayerVoiceUtil;

@SuppressLint("SimpleDateFormat") public class NfcNewOrderDialog extends Dialog  {

	private TextView tv_time;
	private TextView tv_date;
	private Button bt_cancle;
	private Button bt_ok;
	private View view;
	private CheckBox cb_open_carNumber;//开启车牌录入  
	private Context context;
	private ShowNfcNewOrder newOrder;
	private boolean isopen;//是否打开拍照识别；
	public NfcNewOrderDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	public NfcNewOrderDialog(Context context, int theme,ShowNfcNewOrder newOrder) {
		super(context, theme);
		this.context = context;
		this.newOrder = newOrder;
	}

	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.nfc_new_order_dialog);
		tv_time = (TextView) this.findViewById(R.id.tv_nfc_new_time);
		tv_date = (TextView) this.findViewById(R.id.tv_date);
		bt_cancle = (Button) this.findViewById(R.id.bt_cancle);
		bt_ok = (Button) this.findViewById(R.id.bt_ok);
		cb_open_carNumber = (CheckBox) findViewById(R.id.cb_open_car_number);
		view = findViewById(R.id.view_bt_cancle);
		SharedPreferences sp = context.getSharedPreferences("voiceBroadcast", Context.MODE_PRIVATE);
		boolean openBroadcast = sp.getBoolean("broadcast", true);
		
		if (openBroadcast) {
			new PlayerVoiceUtil(context, R.raw.create_order).play();
			MyLog.i("生成订单", "播报语音");
		}
		setView();
	}
	
	@SuppressLint({ "SimpleDateFormat", "NewApi" }) public void setView(){
		SharedPreferences spf = context.getSharedPreferences("autologin", Context.MODE_PRIVATE);
		String iscancle = spf.getString("iscancle", "1");
		
		isopen = context.getSharedPreferences("isopen",Context.MODE_PRIVATE ).getBoolean("isopen", false);
		cb_open_carNumber.setChecked(isopen);
		java.util.Date date = new java.util.Date();
		SimpleDateFormat dateaf = new SimpleDateFormat("yyyy/MM/dd"); 
		SimpleDateFormat timef = new SimpleDateFormat("HH:mm"); 
		tv_time.setText(timef.format(date));
		tv_date.setText(dateaf.format(date));
		if (iscancle.equals("1")) {
			view.setVisibility(View.GONE);
			bt_cancle.setVisibility(View.GONE);
			bt_ok.setOnClickListener(new Button.OnClickListener() {

				public void onClick(View v) {
					newOrder.submitOrder(isopen,NfcNewOrderDialog.this,"");
				}
			});
		}else {
			view.setVisibility(View.VISIBLE);
			bt_cancle.setVisibility(View.VISIBLE);
			bt_cancle.setOnClickListener(new Button.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					NfcNewOrderDialog.this.dismiss();
				}
			});
			bt_ok.setOnClickListener(new Button.OnClickListener() {
				
				public void onClick(View v) {
					newOrder.submitOrder(isopen,NfcNewOrderDialog.this,"");
				}
			});
		}
		//开启车牌录入
		cb_open_carNumber.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					SharedPreferences sp = context.getSharedPreferences("isopen", Context.MODE_PRIVATE);
					Editor edit = sp.edit();
					edit.putBoolean("isopen", true);
					edit.commit();
					isopen = true;
				}else {
					SharedPreferences sp = context.getSharedPreferences("isopen", Context.MODE_PRIVATE);
					Editor edit = sp.edit();
					edit.putBoolean("isopen", false);
					edit.commit();
					isopen = false;
				}
			}
		});
		
	}
	
	
}