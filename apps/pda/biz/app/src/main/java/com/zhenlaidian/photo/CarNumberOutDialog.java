package com.zhenlaidian.photo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.zhenlaidian.bean.NfcOrder;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.VoiceSynthesizerUtil;

public class CarNumberOutDialog extends Dialog {

    private TextView tv_total;
    private EditText et_total;
    private TextView tv_time;
    private LinearLayout ll_is_user;
    private TextView tv_duration;
    private TextView tv_limitday;
    private TextView tv_carnumber;
    private Button bt_cancle;
    private Button bt_ok;
    private View view;
    private LinearLayout ll_collect;
    private LinearLayout ll_munth;
    private Context context;
    private NfcOrder order;
    private ArrayAdapter<String> collectAdapter;
    private String[] collect1;// 按次多价格数据；
    private String final_money;
    private Spinner sp_once_money;// 按次多价格列表；
    private VoiceSynthesizerUtil vUtil;

    public CarNumberOutDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.context = context;
    }

    public CarNumberOutDialog(Context context, int theme, NfcOrder order) {
        super(context, theme);
        this.context = context;
        this.order = order;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.photo_finish_order_dialog);
        tv_total = (TextView) this.findViewById(R.id.tv_nfc_finish_order_total);
        et_total = (EditText) this.findViewById(R.id.et_nfc_finish_order_total);
        tv_duration = (TextView) this.findViewById(R.id.tv_nfc_finish_order_duration);
        ll_is_user = (LinearLayout) this.findViewById(R.id.ll_nfc_finish_order_isuser);
        tv_time = (TextView) this.findViewById(R.id.tv_nfc_finish_order_time);
        tv_carnumber = (TextView) this.findViewById(R.id.tv_nfc_finish_order_carnunber);
        bt_cancle = (Button) this.findViewById(R.id.bt_nfc_finish_order_cancle);
        tv_limitday = (TextView) this.findViewById(R.id.tv_nfc_finish_order_limitday);
        ll_munth = (LinearLayout) findViewById(R.id.ll_nfc_finish_order_month);
        ll_collect = (LinearLayout) findViewById(R.id.ll_nfc_finish_order_collect);
        bt_ok = (Button) this.findViewById(R.id.bt_nfc_finish_order_ok);
        sp_once_money = (Spinner) findViewById(R.id.sp_nfc_finish_order_collect);
        view = findViewById(R.id.view_nfc_view);
        if (order != null) {
            setView();
        }
        vUtil = new VoiceSynthesizerUtil(context);
        if (order.getHandcash() != null && order.getHandcash().equals("0")) {
            if (order.getCollect1() != null) {
                if (order.getCollect1().length < 1) {
                    SharedPreferences sp = context.getSharedPreferences("voiceBroadcast", Context.MODE_PRIVATE);
                    boolean openBroadcast = sp.getBoolean("broadcast", true);
                    if (openBroadcast) {
                        vUtil.playText("结算停车费" + order.getCollect() + "元");
                    }
                }
            } else {
                SharedPreferences sp = context.getSharedPreferences("voiceBroadcast", Context.MODE_PRIVATE);
                boolean openBroadcast = sp.getBoolean("broadcast", true);
                if (openBroadcast) {
                    vUtil.playText("结算停车费" + order.getCollect() + "元");
                }
            }
        }
    }

    public void setView() {
        SharedPreferences spf = context.getSharedPreferences("autologin", Context.MODE_PRIVATE);
        String iscancle = spf.getString("iscancle", "1");

        if (order.getLimitday() != null) {
            ll_collect.setVisibility(View.GONE);
            ll_munth.setVisibility(View.VISIBLE);
            tv_limitday.setText(order.getLimitday());
        } else {
            ll_collect.setVisibility(View.VISIBLE);
            ll_munth.setVisibility(View.GONE);
            if (order.getBtime() != null && order.getEtime() != null) {
                tv_time.setText(order.getBtime() + "-" + order.getEtime());
            }
            if (order.getCollect() != null && order.getHandcash() != null && order.getHandcash().equals("0")) {
                if (order.getCollect1() != null && order.getCollect1().length > 1) {
                    tv_total.setVisibility(View.GONE);
                    et_total.setVisibility(View.GONE);
                    sp_once_money.setVisibility(View.VISIBLE);
                    collect1 = new String[order.getCollect1().length];
                    for (int i = 0; i < order.getCollect1().length; i++) {
                        collect1[i] = String.valueOf(order.getCollect1()[i]);
                    }
                    collectAdapter = new ArrayAdapter<String>(context, R.layout.nfc_finish_dialog_spinner_item, collect1);
                    sp_once_money.setAdapter(collectAdapter);
                    sp_once_money.setOnItemSelectedListener(new OnceCashSpinnerSelectedListener());
                } else {
                    tv_total.setVisibility(View.VISIBLE);
                    et_total.setVisibility(View.GONE);
                    sp_once_money.setVisibility(View.GONE);
                    tv_total.setText(order.getCollect());
                    final_money = order.getCollect();
                }
            } else {
                tv_total.setVisibility(View.GONE);
                et_total.setVisibility(View.VISIBLE);
                sp_once_money.setVisibility(View.GONE);
            }
        }
        if (order.getDuration() != null) {
            tv_duration.setText(order.getDuration());
        }
        if (order.getCarnumber() != null) {
            tv_carnumber.setText(order.getCarnumber());
        }
        if (order.getUin() != null) {
            if (order.getUin().equals("-1")) {
                ll_is_user.setVisibility(View.INVISIBLE);
            } else {
                ll_is_user.setVisibility(View.VISIBLE);
            }
        } else {
            ll_is_user.setVisibility(View.INVISIBLE);
        }
        if (iscancle.equals("1")) {
            view.setVisibility(View.GONE);
            bt_cancle.setVisibility(View.GONE);
            bt_ok.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!IsNetWork.IsHaveInternet(context)) {
                        Toast.makeText(context, "结算定单失败，请检查网络！", 0).show();
                        return;
                    }
                    if (context != null) {
                        CarOrderActivity activity = (CarOrderActivity) context;
                        if (order.getHandcash() != null && order.getHandcash().equals("1")) {
                            if (order.getPrepay() == null || "0.0".equals(order.getPrepay())) {
                                activity.sumitCahsOrder(order, order.getOrderid(), et_total.getText().toString(),
                                        CarNumberOutDialog.this, "0");
                            } else {
                                activity.cashPrepayOrder(et_total.getText().toString(), order, CarNumberOutDialog.this);
                            }
                        } else {// 不是按次输入价格；
                            if (order.getPrepay() == null || "0.0".equals(order.getPrepay())) {
                                activity.sumitCahsOrder(order, order.getOrderid(), final_money, CarNumberOutDialog.this, "0");
                            } else {
                                activity.cashPrepayOrder(final_money, order, CarNumberOutDialog.this);
                            }
                        }
                    }
                }
            });
        } else {
            view.setVisibility(View.VISIBLE);
            bt_cancle.setVisibility(View.VISIBLE);
            bt_ok.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!IsNetWork.IsHaveInternet(context)) {
                        Toast.makeText(context, "结算定单失败，请检查网络！", 0).show();
                        return;
                    }
                    if (context != null) {
                        CarOrderActivity activity = (CarOrderActivity) context;
                        if (order.getHandcash() != null && order.getHandcash().equals("1")) {//按次手输入;
                            if (order.getPrepay() == null || "0.0".equals(order.getPrepay())) {
                                activity.sumitCahsOrder(order, order.getOrderid(), et_total.getText().toString(),
                                        CarNumberOutDialog.this, "0");
                            } else {
                                activity.cashPrepayOrder(et_total.getText().toString(), order, CarNumberOutDialog.this);
                            }
                        } else {// 不是按次输入价格；
                            if (order.getPrepay() == null || "0.0".equals(order.getPrepay())) {
                                activity.sumitCahsOrder(order, order.getOrderid(), final_money, CarNumberOutDialog.this, "0");
                            } else {
                                activity.cashPrepayOrder(final_money, order, CarNumberOutDialog.this);
                            }
                        }
                    }
                }
            });

            bt_cancle.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {

                    CarNumberOutDialog.this.dismiss();

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
