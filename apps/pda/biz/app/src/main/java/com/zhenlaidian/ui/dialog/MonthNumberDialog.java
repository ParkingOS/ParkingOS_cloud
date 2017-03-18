package com.zhenlaidian.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.zhenlaidian.R;

/**
 * Created by xulu on 2016/5/10.
 */
public class MonthNumberDialog extends Dialog {
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
//    private NfcOrder order;
//    private AllOrder allorder;

    private String duration;
    private String carnumber;
    private Handler h;
    public MonthNumberDialog(Context context) {
        super(context);
        this.context = context;
    }

    //
//    public MonthNumberDialog(Context context, int theme, NfcOrder order, AllOrder allorder) {
//        super(context, theme);
//        this.context = context;
//        this.order = order;
//        this.allorder = allorder;
//    }
    public MonthNumberDialog(Context context, int theme, String duration, String carnumber,Handler h) {
        super(context, theme);
        this.context = context;
        this.duration = duration;
        this.carnumber = carnumber;
        this.h = h;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.x_dialog_mouthnumber_carnumber);
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
//        if (order != null) {
//            setView();
//        }

        tv_carnumber.setText(carnumber);
        tv_duration.setText(duration);

        bt_ok.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                MonthNumberDialog.this.dismiss();
                h.sendEmptyMessage(1000);
            }
        });

    }


}
