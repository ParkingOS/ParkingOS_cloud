package com.zhenlaidian.photo;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.NfcOrder;
import com.zhenlaidian.util.MyLog;

/**
 * 手机扫拍出场弹框(安河桥-定制车场类型)
 */
public class CarNumberOutAnHeQiaoDialog extends Dialog {

    TextView tv_carnumber_hint;
    TextView tv_carnumber_on_carnumber;
    LinearLayout ll_write_carnumber;
    private Context context;
    private NfcOrder nfcOrder;
    private TextView tv_cash_type;// 结算的价格类型；
    private TextView tv_final_time_money;// 按时段计费不可修改；
    private Spinner sp_once_money;// 按次多价格列表；
    private EditText et_time_money;// 按时段价格可修改；
    private TextView tv_time;
    private LinearLayout ll_is_user;
    private TextView tv_duration;
    private TextView tv_carnumber;
    private Button bt_cancle;
    private Button bt_ok;
    private View view;
    private RelativeLayout rl_timecash;
    private RelativeLayout rl_oncecash;
    private CheckBox cb_once_cash;
    private CarOrderActivity activity;
    private CheckBox cb_time_cash;
    private String final_money;
    private SharedPreferences cashmode_sp;// 结算方式
    private ArrayAdapter<String> collectAdapter;
    private String[] collect1;// 按次多价格数据；

    public CarNumberOutAnHeQiaoDialog(Context context) {
        super(context);
    }

    public CarNumberOutAnHeQiaoDialog(Context context, int theme, NfcOrder nfcOrder) {
        super(context, theme);
        this.context = context;
        this.nfcOrder = nfcOrder;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.photo_finish_order_anheqiao);
        cashmode_sp = context.getSharedPreferences("anheqiaoCash", Context.MODE_PRIVATE);// 安河桥的结算模式；
        activity = (CarOrderActivity) context;
        initView();
        setView();
    }

    public void initView() {
        tv_duration = (TextView) this.findViewById(R.id.tv_nfc_finish_anheqiao_duration);
        tv_cash_type = (TextView) this.findViewById(R.id.tv_nfc_finish_anheqiao_cash_type);
        ll_is_user = (LinearLayout) this.findViewById(R.id.ll_nfc_finish_anheqiao_isuser);
        ll_write_carnumber = (LinearLayout) findViewById(R.id.ll_nfc_finish_anheqiao_write_carnunber);
        tv_time = (TextView) this.findViewById(R.id.tv_nfc_finish_anheqiao_parktime);
        tv_carnumber_hint = (TextView) findViewById(R.id.tv_nfc_finish_anheqiao_carnunber_hint);
        tv_carnumber = (TextView) this.findViewById(R.id.tv_nfc_finish_anheqiao_carnunber);
        tv_final_time_money = (TextView) this.findViewById(R.id.tv_nfc_finish_anheqiao_time_collect);
        tv_carnumber_on_carnumber = (TextView) this.findViewById(R.id.tv_nfc_finish_anheqiao_carnunber_no_carnumber);
        bt_cancle = (Button) this.findViewById(R.id.bt_nfc_finish_anheqiao_cancle);
        bt_ok = (Button) this.findViewById(R.id.bt_nfc_finish_anheqiao_ok);
        view = findViewById(R.id.view_nfc_anheqiao_view);
        cb_once_cash = (CheckBox) findViewById(R.id.cb_nfc_finish_anheqiao_cash);
        cb_time_cash = (CheckBox) findViewById(R.id.cb_nfc_finish_anheqiao_timecash);
        rl_oncecash = (RelativeLayout) findViewById(R.id.rl_nfc_finish_anheqiao_cash);
        rl_timecash = (RelativeLayout) findViewById(R.id.rl_nfc_finish_anheqiao_time_cash);
        et_time_money = (EditText) findViewById(R.id.et_nfc_finish_anheqiao_time_collect);
        sp_once_money = (Spinner) findViewById(R.id.sp_nfc_finish_order_anheqiao_collect);

        if (cashmode_sp.getString("mcash", "time").equals("time")) {
            cb_time_cash.setChecked(true);
            cb_once_cash.setChecked(false);
            tv_cash_type.setText("临时停车：");
            rl_timecash.setBackgroundColor(context.getResources().getColor(R.color.greenff));
            rl_oncecash.setBackgroundColor(context.getResources().getColor(R.color.white));
            tv_final_time_money.setText(nfcOrder.getCollect());
            final_money = nfcOrder.getCollect();
        } else {
            cb_once_cash.setChecked(true);
            cb_time_cash.setChecked(false);
            tv_cash_type.setText("包天停车：");
            rl_oncecash.setBackgroundColor(context.getResources().getColor(R.color.greenff));
            rl_timecash.setBackgroundColor(context.getResources().getColor(R.color.white));
            tv_final_time_money.setText(nfcOrder.getCollect0());
            final_money = nfcOrder.getCollect0();
        }

        cb_time_cash.setClickable(false);
        rl_timecash.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!cb_time_cash.isChecked()) {
                    cb_time_cash.setChecked(true);
                    cb_once_cash.setChecked(false);
                    tv_cash_type.setText("临时停车：");
                    cashmode_sp.edit().putString("mcash", "time").commit();
                    rl_timecash.setBackgroundColor(context.getResources().getColor(R.color.greenff));
                    rl_oncecash.setBackgroundColor(context.getResources().getColor(R.color.white));
                    // if (nfcOrder.getIsedit() != null &&
                    // nfcOrder.getIsedit().equals("0")) {// isedit 按时价格是否可编辑
                    // 0否，1是
                    // sp_once_money.setVisibility(View.GONE);
                    // et_time_money.setVisibility(View.GONE);
                    // tv_final_time_money.setVisibility(View.VISIBLE);
                    // tv_final_time_money.setText(nfcOrder.getCollect());
                    // final_money = nfcOrder.getCollect();
                    // }else {
                    // sp_once_money.setVisibility(View.GONE);
                    // tv_final_time_money.setVisibility(View.GONE);
                    // et_time_money.setVisibility(View.VISIBLE);
                    // et_time_money.setText(nfcOrder.getCollect());
                    // }
                    tv_final_time_money.setText(nfcOrder.getCollect());
                    final_money = nfcOrder.getCollect();
                } else {
                    cb_time_cash.setChecked(true);
                }
            }
        });
        // cb_time_cash.setOnClickListener(new View.OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // // TODO Auto-generated method stub
        // if (cb_time_cash.isChecked()) {
        // cb_time_cash.setChecked(true);
        // cb_once_cash.setChecked(false);
        // tv_cash_type.setText("按时段计费：");
        // cashmode_sp.edit().putString("mcash", "time").commit();
        // rl_timecash.setBackgroundColor(context.getResources().getColor(R.color.greenff));
        // rl_oncecash.setBackgroundColor(context.getResources().getColor(R.color.white));
        // if (nfcOrder.getIsedit() != null && nfcOrder.getIsedit().equals("0"))
        // {// isedit 按时价格是否可编辑 0否，1是
        // sp_once_money.setVisibility(View.GONE);
        // et_time_money.setVisibility(View.GONE);
        // tv_final_time_money.setVisibility(View.VISIBLE);
        // tv_final_time_money.setText(nfcOrder.getCollect());
        // final_money = nfcOrder.getCollect();
        // }else {
        // sp_once_money.setVisibility(View.GONE);
        // tv_final_time_money.setVisibility(View.GONE);
        // et_time_money.setVisibility(View.VISIBLE);
        // et_time_money.setText(nfcOrder.getCollect());
        // }
        // }else {
        // cb_time_cash.setChecked(true);
        // }
        // }
        // });

        cb_once_cash.setClickable(false);
        rl_oncecash.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!cb_once_cash.isChecked()) {
                    cb_once_cash.setChecked(true);
                    cb_time_cash.setChecked(false);
                    tv_cash_type.setText("包天停车：");
                    cashmode_sp.edit().putString("mcash", "once").commit();
                    rl_oncecash.setBackgroundColor(context.getResources().getColor(R.color.greenff));
                    rl_timecash.setBackgroundColor(context.getResources().getColor(R.color.white));
                    tv_final_time_money.setText(nfcOrder.getCollect0());
                    final_money = nfcOrder.getCollect0();
                } else {
                    cb_once_cash.setChecked(true);
                }
            }
        });

    }

    public void setView() {
        SharedPreferences spf = context.getSharedPreferences("autologin", Context.MODE_PRIVATE);
        String iscancle = spf.getString("iscancle", "1");

        if (nfcOrder.getBtime() != null && nfcOrder.getEtime() != null) {
            tv_time.setText(nfcOrder.getBtime() + "-" + nfcOrder.getEtime());
        }
        if (nfcOrder.getDuration() != null) {
            tv_duration.setText(nfcOrder.getDuration());
        }
        if (nfcOrder.getUin() != null) {
            if (nfcOrder.getUin().equals("-1")) {
                ll_is_user.setVisibility(View.INVISIBLE);
            } else {
                ll_is_user.setVisibility(View.VISIBLE);
            }
        }
        if (nfcOrder.getCarnumber() != null) {
            tv_carnumber.setText(nfcOrder.getCarnumber());
        }

        if (iscancle.equals("1")) {
            view.setVisibility(View.GONE);
            bt_cancle.setVisibility(View.GONE);
            bt_ok.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (cb_time_cash.isChecked()) {
                        final_money = nfcOrder.getCollect();
                    } else {
                        final_money = nfcOrder.getCollect0();
                    }
                    activity.sumitCahsOrder(nfcOrder, nfcOrder.getOrderid(), final_money, CarNumberOutAnHeQiaoDialog.this, "0");
                }
            });
        } else {
            view.setVisibility(View.VISIBLE);
            bt_cancle.setVisibility(View.VISIBLE);
            bt_ok.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (cb_time_cash.isChecked()) {
                        final_money = nfcOrder.getCollect();
                    } else {
                        final_money = nfcOrder.getCollect0();
                    }
                    activity.sumitCahsOrder(nfcOrder, nfcOrder.getOrderid(), final_money, CarNumberOutAnHeQiaoDialog.this, "0");
                }
            });
            bt_cancle.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    CarNumberOutAnHeQiaoDialog.this.dismiss();

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
