package com.zhenlaidian.bean;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.photo.InputCarNumberActivity;
import com.zhenlaidian.ui.person_account.MyWalletActivity;

public class EPayMessageDialog extends Dialog {

	private LeaveOrder order;
	private TextView tv_carnumber;
	private TextView tv_money;
//	private TextView tv_warn;
	private Button bt_ok;
	private Button bt_check;
	private Context context;
	private InputCarNumberActivity inactivity;

	public EPayMessageDialog(Context context) {
		super(context);
	}

	public EPayMessageDialog(Context context, int theme, LeaveOrder order) {
		super(context, theme);
		this.context = context;
		this.order = order;
	}
	public EPayMessageDialog(Context context, int theme, LeaveOrder order,InputCarNumberActivity activity) {
		super(context, theme);
		this.context = context;
		this.order = order;
		this.inactivity = activity;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.e_pay_message_dailog);
		initView();
		if (this.order != null) {
			setVeiw();
		}
	}

	public void initView() {
		tv_carnumber = (TextView) findViewById(R.id.tv_epay_message_carnumber);
		tv_money = (TextView) findViewById(R.id.tv_epay_message_money);
//		tv_warn = (TextView) findViewById(R.id.tv_epay_message_warn);
		bt_ok = (Button) findViewById(R.id.bt_epay_message_ok);
		bt_check = (Button) findViewById(R.id.bt_epay_message_check);
	}

	public void setVeiw() {
		if (order.getCarnumber() != null) {
			tv_carnumber.setText("车主："+order.getCarnumber());
		}
		if (order.getTotal() != null) {
			tv_money.setText("付款："+order.getTotal()+"元");
		}
//		if (order.getTotal() != null && Double.parseDouble(order.getTotal()) >= 4.00) {
//			tv_warn.setVisibility(View.VISIBLE);
//		}

		bt_ok.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				EPayMessageDialog.this.dismiss();
				if (inactivity != null) {
					inactivity.finish();
				}
//				if (context != null ) {
//					LeaveActivity activity = (LeaveActivity) context;
//					activity.setNullView();
//				}
			}
		});
		bt_check.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				if (context != null ) {
//					LeaveActivity activity = (LeaveActivity) context;
//					activity.setNullView();
//				}
				if (inactivity != null) {
					inactivity.finish();
				}
				Intent intent = new Intent(context, MyWalletActivity.class);
				intent.putExtra("LeaveActivity", true);
				context.startActivity(intent);
				
				EPayMessageDialog.this.dismiss();
			}
		});
	}
}
