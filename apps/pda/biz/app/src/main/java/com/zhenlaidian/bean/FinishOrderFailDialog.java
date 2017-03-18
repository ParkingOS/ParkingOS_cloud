package com.zhenlaidian.bean;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.photo.InputCarNumberActivity;

public class FinishOrderFailDialog extends Dialog {

	private NfcOrder nfcOrder;
	private TextView tv_orderid;
	private TextView tv_errormsg;
	private TextView tv_warn;
	private Button bt_cancle;
	private Context context;
	public String cash_state;//结算类型
	private InputCarNumberActivity inactivity;

	public FinishOrderFailDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public FinishOrderFailDialog(Context context, int theme, NfcOrder nfcOrder,String cash_state) {
		super(context, theme);
		this.context = context;
		this.nfcOrder = nfcOrder;
		this.cash_state = cash_state;
	}
	public FinishOrderFailDialog(Context context, int theme, NfcOrder nfcOrder,InputCarNumberActivity inactivity,String cash_state) {
		super(context, theme);
		this.context = context;
		this.nfcOrder = nfcOrder;
		this.inactivity = inactivity;
		this.cash_state = cash_state;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.finish_order_fail_dialog);
		initView();
		if (this.nfcOrder != null) {
			setVeiw();
		}
	}

	public void initView() {
		tv_orderid = (TextView) findViewById(R.id.tv_finish_order_dialog_id);
		tv_errormsg = (TextView) findViewById(R.id.tv_finish_order_dialog_error);
		tv_warn = (TextView) findViewById(R.id.tv_finish_order_dialog_warn);
		bt_cancle = (Button) findViewById(R.id.bt_finish_order_cancle);
	}

	public void setVeiw() {
		tv_orderid.setText("订单编号：" + nfcOrder.getOrderid());
		tv_errormsg.setText("错误代码：" + nfcOrder.getNetError());
		if ("nfc".equals(cash_state)) {//nfc结算
			tv_warn.setText("请截屏或拍照给停车宝处理。或者尝试再次刷卡结算，谢谢！");
		}else if ("fast".equals(cash_state)) {//极速通结算
			tv_warn.setText("请收现金结算。也可截屏或拍照给停车宝处理，谢谢！");
		}
		bt_cancle.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				FinishOrderFailDialog.this.dismiss();
				if (inactivity != null) {
					inactivity.finish();
				}
			}
		});

	}
}
