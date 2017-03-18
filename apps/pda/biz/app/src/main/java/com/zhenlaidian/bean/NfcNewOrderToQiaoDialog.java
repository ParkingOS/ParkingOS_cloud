package com.zhenlaidian.bean;

import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.engine.ShowNfcNewOrder;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.PlayerVoiceUtil;

public class NfcNewOrderToQiaoDialog extends Dialog{
	private ShowNfcNewOrder newOrder;
	private Context context;
	private TextView tv_time;
	private TextView tv_date;
	private Button bt_cancle;
	private Button bt_ok;
	private View view;
	private RelativeLayout rl_timecash;
	private RelativeLayout rl_oncecash;
	private CheckBox cb_once_cash;
	private CheckBox cb_time_cash;
	private SharedPreferences cashmode_sp;// 结算方式
	private String ctype ;//ctype:计费方式：0按时(0.5/15分钟)，1按次（12小时内10元,前1/30min，后每小时1元）
	public NfcNewOrderToQiaoDialog(Context context) {
		super(context);
		
	}
	public NfcNewOrderToQiaoDialog(Context context,int theme,ShowNfcNewOrder newOrder) {
		super(context,theme);
		this.context = context;
		this.newOrder = newOrder;
		cashmode_sp = context.getSharedPreferences("toqiaoCash", Context.MODE_PRIVATE);//安河桥的结算模式；
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.nfc_new_order_toqiao_dialog);
		initView();
		SharedPreferences sp = context.getSharedPreferences("voiceBroadcast", Context.MODE_PRIVATE);
		boolean openBroadcast = sp.getBoolean("broadcast", true);
		if (openBroadcast) {
			new PlayerVoiceUtil(context, R.raw.create_order).play();
			MyLog.i("生成订单", "播报语音");
		}
		setView();
	}
	public void initView() {
		tv_date = (TextView) this.findViewById(R.id.tv_nfc_new_anheqiao_data);
		tv_time = (TextView) this.findViewById(R.id.tv_nfc_new_anheqiao_time);
		bt_cancle = (Button) this.findViewById(R.id.bt_nfc_new_anheqiao_cancle);
		bt_ok = (Button) this.findViewById(R.id.bt_nfc_new_anheqiao_ok);
		view = findViewById(R.id.view_nfc_new_anheqiao_view);
		cb_once_cash = (CheckBox) findViewById(R.id.cb_nfc_new_anheqiao_cash);
		cb_time_cash = (CheckBox) findViewById(R.id.cb_nfc_new_anheqiao_timecash);
		rl_oncecash = (RelativeLayout) findViewById(R.id.rl_nfc_new_anheqiao_cash);
		rl_timecash = (RelativeLayout) findViewById(R.id.rl_nfc_new_anheqiao_time_cash);
		
		if (cashmode_sp.getString("mcash", "time").equals("time")) {
			cb_time_cash.setChecked(true);
			cb_once_cash.setChecked(false);
			ctype = "0";
			rl_timecash.setBackgroundColor(context.getResources().getColor(R.color.greenff));
			rl_oncecash.setBackgroundColor(context.getResources().getColor(R.color.white));
		} else {
			cb_once_cash.setChecked(true);
			cb_time_cash.setChecked(false);
			ctype = "1";
			rl_oncecash.setBackgroundColor(context.getResources().getColor(R.color.greenff));
			rl_timecash.setBackgroundColor(context.getResources().getColor(R.color.white));
		}
		
		cb_time_cash.setClickable(false);
		rl_timecash.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!cb_time_cash.isChecked()) {
					cb_time_cash.setChecked(true);
					cb_once_cash.setChecked(false);
					ctype = "0";
					cashmode_sp.edit().putString("mcash", "time").commit();
					rl_timecash.setBackgroundColor(context.getResources().getColor(R.color.greenff));
					rl_oncecash.setBackgroundColor(context.getResources().getColor(R.color.white));
				}else {
					cb_time_cash.setChecked(true);
				}
			}
		});
		
		cb_once_cash.setClickable(false);
		rl_oncecash.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if ( !cb_once_cash.isChecked()) {
					cb_once_cash.setChecked(true);
					cb_time_cash.setChecked(false);
					ctype = "1";
					cashmode_sp.edit().putString("mcash", "once").commit();
					rl_oncecash.setBackgroundColor(context.getResources().getColor(R.color.greenff));
					rl_timecash.setBackgroundColor(context.getResources().getColor(R.color.white));
				}else {
					cb_once_cash.setChecked(true);
				}
			}
		});
	}

	@SuppressLint("SimpleDateFormat")
	public void setView(){
		SharedPreferences spf = context.getSharedPreferences("autologin", Context.MODE_PRIVATE);
		String iscancle = spf.getString("iscancle", "1");
		java.util.Date date = new java.util.Date();
		SimpleDateFormat dateaf = new SimpleDateFormat("yyyy/MM/dd"); 
		SimpleDateFormat timef = new SimpleDateFormat("HH:mm"); 
		tv_time.setText(timef.format(date));
		tv_date.setText(dateaf.format(date));
		if (iscancle.equals("1")) {
			view.setVisibility(View.GONE);
			bt_cancle.setVisibility(View.GONE);
			bt_ok.setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick(View v) {
					newOrder.submitOrder(false,NfcNewOrderToQiaoDialog.this,ctype);
				}
			});
		}else {
			view.setVisibility(View.VISIBLE);
			bt_cancle.setVisibility(View.VISIBLE);
			bt_ok.setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick(View v) {
					newOrder.submitOrder(false,NfcNewOrderToQiaoDialog.this,ctype);
				}
			});
			bt_cancle.setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					NfcNewOrderToQiaoDialog.this.dismiss();
				}
			});
		}
		
	}
}
