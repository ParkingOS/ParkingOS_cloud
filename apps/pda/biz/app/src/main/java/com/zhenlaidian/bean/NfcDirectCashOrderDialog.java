package com.zhenlaidian.bean;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.engine.ShowNfcNewOrder;
import com.zhenlaidian.photo.InPutCarNumberDialog;
import com.zhenlaidian.ui.LeaveActivity;

public class NfcDirectCashOrderDialog extends BaseDialog {

	// 直接结算：{info:2,carnumber:京A88888,ctime:2015-02-04
	// 12：00：00,total:5.00,uin:10218,uuid:0484894A9A3D81}
	private TextView tv_total;
	private TextView tv_carnumber;
	private RelativeLayout Rl_carnumber;
	private TextView tv_change;
	private Button bt_input_carnumber;
	private Button bt_ok;
	private Button bt_cancle;
	private NfcOrder nfcOrder;
	private ShowNfcNewOrder newOrder;
	private Context context;
	private int cPosition;

	public NfcDirectCashOrderDialog(Context context, int theme, ShowNfcNewOrder newOrder, NfcOrder nfcOrder) {
		super(context, theme);
		this.context = context;
		this.newOrder = newOrder;
		this.nfcOrder = nfcOrder;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_direct_cashorder_dialog);
		initView();
		setView();
	}

	@Override
	public void setCarnumber(String number) {
		nfcOrder.setCards(new String[] { number });
		tv_carnumber.setText(number);
		Rl_carnumber.setVisibility(View.VISIBLE);
		bt_input_carnumber.setVisibility(View.GONE);
		tv_change.setVisibility(View.GONE);
	}

	public void initView() {
		tv_total = (TextView) findViewById(R.id.tv_nfc_direct_cash_total);
		tv_carnumber = (TextView) findViewById(R.id.tv_nfc_direct_cash_carnumber);
		tv_change = (TextView) findViewById(R.id.tv_nfc_direct_cash_change_carnumber);
		bt_input_carnumber = (Button) findViewById(R.id.bt_nfc_direct_cash_input_carnumber);
		bt_ok = (Button) findViewById(R.id.bt_nfc_direct_cash_ok);
		Rl_carnumber = (RelativeLayout) findViewById(R.id.rl_nfc_direct_cash_carnumber);
		bt_cancle = (Button) findViewById(R.id.bt_nfc_direct_cash_cancle);

		if (nfcOrder.getCards() == null || nfcOrder.getCards().length == 0) {
			Rl_carnumber.setVisibility(View.GONE);
			bt_input_carnumber.setVisibility(View.VISIBLE);
		} else {
			if (nfcOrder.getCards().length == 1) {
				tv_carnumber.setText(nfcOrder.getCards()[0]);
				tv_change.setVisibility(View.INVISIBLE);
			} else {
				tv_carnumber.setText(nfcOrder.getCards()[0]);
				tv_change.setVisibility(View.VISIBLE);
				tv_change.setOnClickListener(new TextView.OnClickListener() {
					@Override
					public void onClick(View v) {
						for (int i = 0; i < nfcOrder.getCards().length; i++) {
							if (tv_carnumber.getText().toString().equals(nfcOrder.getCards()[i])) {
								if (i == nfcOrder.getCards().length - 1) {
									tv_carnumber.setText(nfcOrder.getCards()[0]);
									cPosition = 0;
								} else {
									tv_carnumber.setText(nfcOrder.getCards()[i + 1]);
									cPosition = i + 1;
								}
								return;
							}
						}
					}
				});
			}
		}

	}

	public void setView() {
		if (nfcOrder.getTotal() != null) {
			tv_total.setText(Html.fromHtml("本次收费<font color='#31A667'> " + nfcOrder.getTotal() + " </font>" + "元"));
		}
		bt_input_carnumber.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO 去编辑车牌号
				if (context != null) {
					LeaveActivity activity = (LeaveActivity) context;
					if (!activity.isFinishing()) {
						new InPutCarNumberDialog(context, false, "", mListener).show();
					}
				}
			}
		});
		bt_ok.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO 确认直接结算
				String carNumber;
				if (nfcOrder.getCards() == null) {
					carNumber = "";
				} else {
					if (nfcOrder.getCards().length >= 1) {
						carNumber = nfcOrder.getCards()[cPosition];
					} else {
						carNumber = "";
					}
				}
				newOrder.onceCashOrder(nfcOrder.getTotal(), NfcDirectCashOrderDialog.this, carNumber);
			}
		});
		bt_cancle.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO 取消操作
				NfcDirectCashOrderDialog.this.dismiss();
			}
		});

	}

	OnCancelListener mListener = new OnCancelListener() {

		@Override
		public void onCancel(DialogInterface dialog) {
			// TODO Auto-generated method stub
			InPutCarNumberDialog d = (InPutCarNumberDialog) dialog;
			setCarnumber(d.getcarnumber());
		}
	};

}
