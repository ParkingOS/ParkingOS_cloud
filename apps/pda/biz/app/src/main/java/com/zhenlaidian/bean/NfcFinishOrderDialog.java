package com.zhenlaidian.bean;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zhenlaidian.R;
import com.zhenlaidian.engine.ShowNfcFinishOrder;
import com.zhenlaidian.photo.InputCarNumberActivity;
import com.zhenlaidian.util.CheckUtils;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.VoiceSynthesizerUtil;

public class NfcFinishOrderDialog extends Dialog {

	private TextView tv_total;
	private EditText et_total;
	private TextView tv_time;
	private LinearLayout ll_is_user;
	private TextView tv_duration;
	private TextView tv_carnumber;
	private TextView tv_is_user;// 会显示预付费图片；
	private TextView tv_carnumber_warn;// 提示车主有几张车牌
	private TextView tv_carnumber_hint;// 提示切换车牌
	private TextView tv_carnumber_on_carnumber;
	private TextView tv_limitday;
	private LinearLayout ll_write_carnumber;
	private LinearLayout ll_collect;
	private LinearLayout ll_munth;
	private Spinner sp_once_money;// 按次多价格列表；
	private Button bt_cancle;
	private Button bt_ok;
	private View view;
	private Context context;
	private NfcOrder nfcOrder;
	private ShowNfcFinishOrder finishOrder;
	private VoiceSynthesizerUtil vUtil;
	private ArrayAdapter<String> collectAdapter;
	private String[] collect1;// 按次多价格数据；
	private String final_money;
	private int cPosition = 0;// 记录选择多车牌位置

	public NfcFinishOrderDialog(Context context) {
		super(context);
	}

	public NfcFinishOrderDialog(Context context, int theme, ShowNfcFinishOrder finishOrder, NfcOrder nfcOrder) {
		super(context, theme);
		this.context = context;
		this.finishOrder = finishOrder;
		this.nfcOrder = nfcOrder;
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.nfc_finish_order_dialog);
		tv_total = (TextView) this.findViewById(R.id.tv_nfc_finish_order_total);
		et_total = (EditText) this.findViewById(R.id.et_nfc_finish_order_total);
		tv_duration = (TextView) this.findViewById(R.id.tv_nfc_finish_order_duration);
		ll_is_user = (LinearLayout) this.findViewById(R.id.ll_nfc_finish_order_isuser);
		tv_is_user = (TextView) this.findViewById(R.id.tv_nfc_finish_order_isuser);
		tv_time = (TextView) this.findViewById(R.id.tv_nfc_finish_order_time);
		tv_carnumber = (TextView) this.findViewById(R.id.tv_nfc_finish_order_carnunber);
		tv_carnumber_warn = (TextView) this.findViewById(R.id.tv_nfc_finish_carnumber_warn);
		tv_carnumber_hint = (TextView) this.findViewById(R.id.tv_nfc_finish_order_carnunber_hint);
		tv_carnumber_on_carnumber = (TextView) this.findViewById(R.id.tv_nfc_finish_order_carnunber_no_carnumber);
		tv_limitday = (TextView) this.findViewById(R.id.tv_nfc_finish_order_limitday);
		ll_write_carnumber = (LinearLayout) findViewById(R.id.ll_nfc_finish_order_write_carnunber);
		ll_munth = (LinearLayout) findViewById(R.id.ll_nfc_finish_order_month);
		ll_collect = (LinearLayout) findViewById(R.id.ll_nfc_finish_order_collect);
		bt_cancle = (Button) this.findViewById(R.id.bt_nfc_finish_order_cancle);
		bt_ok = (Button) this.findViewById(R.id.bt_nfc_finish_order_ok);
		sp_once_money = (Spinner) findViewById(R.id.sp_nfc_finish_order_collect);
		view = findViewById(R.id.view_nfc_view);
		vUtil = new VoiceSynthesizerUtil(context);
		if (nfcOrder.getHandcash() != null && nfcOrder.getHandcash().equals("0")) {
			if (nfcOrder.getCollect1() != null) {
				if (nfcOrder.getCollect1().length < 2) {
					SharedPreferences sp = context.getSharedPreferences("voiceBroadcast", Context.MODE_PRIVATE);
					boolean openBroadcast = sp.getBoolean("broadcast", true);
					if (openBroadcast) {
						vUtil.playText("结算停车费" + nfcOrder.getCollect() + "元");
						MyLog.i("结算停车费", "播报语音");
					}
				}
			} else {
				SharedPreferences sp = context.getSharedPreferences("voiceBroadcast", Context.MODE_PRIVATE);
				boolean openBroadcast = sp.getBoolean("broadcast", true);
				if (openBroadcast) {
					vUtil.playText("结算停车费" + nfcOrder.getCollect() + "元");
					MyLog.i("结算停车费", "播报语音");
				}
			}
		}
		setView();
	}

	public void setView() {
		SharedPreferences spf = context.getSharedPreferences("autologin", Context.MODE_PRIVATE);
		String iscancle = spf.getString("iscancle", "1");
		
		if (nfcOrder.getLimitday() != null) {//按月卡收费；显示月卡布局；
			ll_collect.setVisibility(View.GONE);
			ll_munth.setVisibility(View.VISIBLE);
			tv_limitday.setText(nfcOrder.getLimitday());
		} else {
			ll_collect.setVisibility(View.VISIBLE);
			ll_munth.setVisibility(View.GONE);
			if (nfcOrder.getBtime() != null && nfcOrder.getEtime() != null) {
				tv_time.setText(nfcOrder.getBtime() + "-" + nfcOrder.getEtime());
			}
			if (nfcOrder.getCollect() != null && nfcOrder.getHandcash() != null && nfcOrder.getHandcash().equals("0")) {//按计时收费， 0 不按次手输结算
				if (nfcOrder.getCollect1() != null && nfcOrder.getCollect1().length > 1) {//有多个价格，显示多个价格
					tv_total.setVisibility(View.GONE);
					et_total.setVisibility(View.GONE);
					sp_once_money.setVisibility(View.VISIBLE);
					collect1 = new String[nfcOrder.getCollect1().length];
					for (int i = 0; i < nfcOrder.getCollect1().length; i++) {
						collect1[i] = String.valueOf(nfcOrder.getCollect1()[i]);
					}
					collectAdapter = new ArrayAdapter<String>(context, R.layout.nfc_finish_dialog_spinner_item, collect1);
					sp_once_money.setAdapter(collectAdapter);
					sp_once_money.setOnItemSelectedListener(new OnceCashSpinnerSelectedListener());
				} else {
					tv_total.setVisibility(View.VISIBLE);
					et_total.setVisibility(View.GONE);
					sp_once_money.setVisibility(View.GONE);
					tv_total.setText(nfcOrder.getCollect());
					final_money = nfcOrder.getCollect();
				}
			} else {//1 按次手输结算；
				tv_total.setVisibility(View.GONE);
				et_total.setVisibility(View.VISIBLE);
				sp_once_money.setVisibility(View.GONE);
			}
		}
		if (nfcOrder.getDuration() != null) {
			tv_duration.setText(nfcOrder.getDuration());
		}
		if (nfcOrder.getUin() != null) {
			if (nfcOrder.getUin().equals("-1")) {// 不是会员
				if (nfcOrder.getPrepay() == null || "0.0".equals(nfcOrder.getPrepay())) {
					ll_is_user.setVisibility(View.INVISIBLE);
				} else {
					tv_is_user.setBackgroundResource(R.drawable.prepayment);
					ll_is_user.setVisibility(View.VISIBLE);
				}
			} else {
				if (nfcOrder.getPrepay() == null || "0.0".equals(nfcOrder.getPrepay())) {
					tv_is_user.setBackgroundResource(R.drawable.finish_order_vip);
				} else {
					tv_is_user.setBackgroundResource(R.drawable.prepayment);
				}
				ll_is_user.setVisibility(View.VISIBLE);
			}
		}
		// NFC--->结算订单返回的数据解析：NfcOrder [total=0.75, etime=19:42, btime=19:39,
		// orderid=176623, collect=0.5, discount=0.25, duration=3分钟, uin=-1,
		// carnumber=车牌号未知]

		if (nfcOrder.getCarnumber() != null) {
			MyLog.i("--->>>", "" + "显示结算的车牌号是：" + nfcOrder.getCarnumber());
			boolean isMatched = CheckUtils.CarChecked(nfcOrder.getCarnumber());
			if (!isMatched) {
				tv_carnumber_hint.setVisibility(View.GONE);
				tv_carnumber.setVisibility(View.GONE);
				tv_carnumber_on_carnumber.setVisibility(View.VISIBLE);
				ll_write_carnumber.setOnClickListener(new Button.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO 点击添加车牌号；
						if (nfcOrder.getHandcash() != null && nfcOrder.getHandcash().equals("1")) {
							if (et_total.getText().toString().trim().isEmpty()) {
								Toast.makeText(context, "请先输入结算金额", 0).show();
							} else {
								nfcOrder.setCollect(et_total.getText().toString().trim());
								Intent intent = new Intent(context, InputCarNumberActivity.class);
								intent.putExtra("add", "cashOrder");
								intent.putExtra("nfcorder", nfcOrder);
								context.startActivity(intent);
//								LeaveActivity activity = (LeaveActivity) context;
//								activity.setNfcChenge("finish");
								NfcFinishOrderDialog.this.dismiss();
							}
						} else {
							Intent intent = new Intent(context, InputCarNumberActivity.class);
							nfcOrder.setCollect(final_money);
							intent.putExtra("add", "cashOrder");
							intent.putExtra("nfcorder", nfcOrder);
							context.startActivity(intent);
//							LeaveActivity activity = (LeaveActivity) context;
//							activity.setNfcChenge("finish");
							NfcFinishOrderDialog.this.dismiss();
						}
					}
				});
			} else {
				tv_carnumber_hint.setVisibility(View.VISIBLE);
				tv_carnumber.setVisibility(View.VISIBLE);
				tv_carnumber_on_carnumber.setVisibility(View.GONE);
				if (nfcOrder.getCards() == null || nfcOrder.getCards().length == 0) {
					tv_carnumber_warn.setText(Html.fromHtml("此卡未绑定车牌"));
					tv_carnumber_hint.setText("普通卡");
					tv_carnumber.setText("车牌未知");
				} else if (nfcOrder.getCards() != null && nfcOrder.getCards().length == 1) {
					tv_carnumber_warn.setText(Html.fromHtml("该车主有 <font color='#36A56A'><big>" + nfcOrder.getCards().length
							+ "</big></font> 张车牌"));
					tv_carnumber_hint.setText("速通卡会员");
					tv_carnumber.setText(nfcOrder.getCards()[0]);
				} else {
					tv_carnumber_warn.setText(Html.fromHtml("该车主有 <font color='#36A56A'><big>" + nfcOrder.getCards().length
							+ "</big></font> 张车牌"));
					tv_carnumber_hint.setText("点击切换车牌");
					tv_carnumber.setText(nfcOrder.getCards()[0]);
					tv_carnumber_hint.setOnClickListener(new TextView.OnClickListener() {
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
		} else {
			tv_carnumber_hint.setVisibility(View.GONE);
			tv_carnumber.setVisibility(View.GONE);
			tv_carnumber_on_carnumber.setVisibility(View.VISIBLE);
			ll_write_carnumber.setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO 点击添加车牌号；
					if (nfcOrder.getHandcash() != null && nfcOrder.getHandcash().equals("1")) {
						if (et_total.getText().toString().trim().isEmpty()) {
							Toast.makeText(context, "请先输入结算金额", 0).show();
						} else {
							nfcOrder.setCollect(et_total.getText().toString().trim());
							Intent intent = new Intent(context, InputCarNumberActivity.class);
							intent.putExtra("add", "cashOrder");
							intent.putExtra("nfcorder", nfcOrder);
							context.startActivity(intent);
//							LeaveActivity activity = (LeaveActivity) context;
//							activity.setNfcChenge("finish");
							NfcFinishOrderDialog.this.dismiss();
						}
					} else {
						Intent intent = new Intent(context, InputCarNumberActivity.class);
						nfcOrder.setCollect(final_money);
						intent.putExtra("add", "cashOrder");
						intent.putExtra("nfcorder", nfcOrder);
						context.startActivity(intent);
//						LeaveActivity activity = (LeaveActivity) context;
//						activity.setNfcChenge("finish");
						NfcFinishOrderDialog.this.dismiss();
					}
				}
			});
		}
		if (iscancle.equals("1")) {//默认不显示关闭按钮
			view.setVisibility(View.GONE);
			bt_cancle.setVisibility(View.GONE);
			bt_ok.setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick(View v) {
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
					if (nfcOrder.getHandcash() != null && nfcOrder.getHandcash().equals("1")) {// 按次输入价格；
						if (nfcOrder.getPrepay() == null || "0.0".equals(nfcOrder.getPrepay())) {// 没有预支付金额，调正常结算接口
							finishOrder.submitCash(et_total.getText().toString().trim(), NfcFinishOrderDialog.this, "0",
									carNumber);
						} else {// 有预支付金额，调预支付结算接口
							finishOrder.cashPrepayOrder(et_total.getText().toString().trim(), NfcFinishOrderDialog.this);
						}
					} else {// 不是按次输入价格；
						if (nfcOrder.getPrepay() == null || "0.0".equals(nfcOrder.getPrepay())) {
							finishOrder.submitCash(final_money, NfcFinishOrderDialog.this, "0", carNumber);
						} else {
							finishOrder.cashPrepayOrder(final_money, NfcFinishOrderDialog.this);
						}
					}
				}
			});
		} else {//显示关闭按钮
			view.setVisibility(View.VISIBLE);
			bt_cancle.setVisibility(View.VISIBLE);
			bt_ok.setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick(View v) {
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
					if (nfcOrder.getHandcash() != null && nfcOrder.getHandcash().equals("1")) {
						if (nfcOrder.getPrepay() == null || "0.0".equals(nfcOrder.getPrepay())) {
							finishOrder.submitCash(et_total.getText().toString().trim(), NfcFinishOrderDialog.this, "0",
									carNumber);
						} else {
							finishOrder.cashPrepayOrder(et_total.getText().toString().trim(), NfcFinishOrderDialog.this);
						}
					} else {// 不是按次输入价格；
						if (nfcOrder.getPrepay() == null || "0.0".equals(nfcOrder.getPrepay())) {
							finishOrder.submitCash(final_money, NfcFinishOrderDialog.this, "0", carNumber);
						} else {
							finishOrder.cashPrepayOrder(final_money, NfcFinishOrderDialog.this);
						}
					}
				}
			});
			bt_cancle.setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					NfcFinishOrderDialog.this.dismiss();

				}
			});
		}

	}

	class OnceCashSpinnerSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

			final_money = collect1[arg2];
			MyLog.i("CarNumberOutOnceDialog", "按次计费选择的价格是：" + collect1[arg2]);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

}