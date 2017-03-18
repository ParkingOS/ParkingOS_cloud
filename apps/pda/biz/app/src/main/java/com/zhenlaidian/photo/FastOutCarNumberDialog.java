package com.zhenlaidian.photo;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.AllOrder;
import com.zhenlaidian.bean.NfcOrder;
import com.zhenlaidian.util.IsNetWork;

/**
 * 极速通订单出场弹框;
 */
public class FastOutCarNumberDialog extends Dialog {
    private EditText et_total;
    private TextView tv_time;
    private LinearLayout ll_is_user;
    private TextView tv_duration;
    private TextView tv_limitday;
    private TextView tv_carnumber;
    private TextView tv_car_number_hint;
    private Button bt_cancle;
    private Button bt_ok;
    private View view;
    private LinearLayout ll_collect;
    private LinearLayout ll_munth;
    private Context context;
    private NfcOrder order;
    private AllOrder allorder;

    public FastOutCarNumberDialog(Context context) {
        super(context);
        this.context = context;
    }

    public FastOutCarNumberDialog(Context context, int theme, NfcOrder order, AllOrder allorder) {
        super(context, theme);
        this.context = context;
        this.order = order;
        this.allorder = allorder;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_fast_out_carnumber);
        et_total = (EditText) this.findViewById(R.id.et_fastout_finish_order_total);
        tv_duration = (TextView) this.findViewById(R.id.tv_fast_finish_order_duration);
        tv_car_number_hint = (TextView) this.findViewById(R.id.tv_fastout_finish_order_carnunber_hint);
        ll_is_user = (LinearLayout) this.findViewById(R.id.ll_fastout_finish_order_isuser);
        tv_time = (TextView) this.findViewById(R.id.tv_fastout_finish_order_time);
        tv_carnumber = (TextView) this.findViewById(R.id.tv_fastout_finish_order_carnunber);
        tv_limitday = (TextView) this.findViewById(R.id.tv_fastout_finish_order_limitday);
        ll_munth = (LinearLayout) findViewById(R.id.ll_fastout_finish_order_month);
        ll_collect = (LinearLayout) findViewById(R.id.ll_fastout_finish_order_collect);
        bt_ok = (Button) this.findViewById(R.id.bt_fastout_finish_order_ok);
        bt_cancle = (Button) this.findViewById(R.id.bt_fastout_finish_order_cancle);
        view = findViewById(R.id.view_fastout_finish_bar);
        if (order != null) {
            setView();
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
            et_total.setVisibility(View.VISIBLE);
        }
        if (order.getIsfast() != null) {
            if ("1".equals(order.getIsfast()) || "2".equals(order.getIsfast())) {
                tv_car_number_hint.setText("卡号");
            } else {
                tv_car_number_hint.setText("车牌号");
            }
        } else {
            tv_car_number_hint.setText("车牌号");
        }
        if (order.getBtime() != null && order.getEtime() != null) {
            tv_time.setText(order.getBtime() + "-" + order.getEtime());
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
                        if (TextUtils.isEmpty(et_total.getText().toString().trim())) {
                            Toast.makeText(context, "请输入金额！", 0).show();
                        } else {
                            order.setTotal(et_total.getText().toString().trim());
                            CarOrderActivity activity = (CarOrderActivity) context;
                            activity.CashoutBLEOrder(order, FastOutCarNumberDialog.this, allorder);
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
                        if (TextUtils.isEmpty(et_total.getText().toString().trim())) {
                            Toast.makeText(context, "请输入金额！", 0).show();
                        } else {
                            order.setTotal(et_total.getText().toString().trim());
                            CarOrderActivity activity = (CarOrderActivity) context;
                            activity.CashoutBLEOrder(order, FastOutCarNumberDialog.this, allorder);
                        }
                    }
                }
            });

            bt_cancle.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {

                    FastOutCarNumberDialog.this.dismiss();

                }
            });
        }
    }
}
