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
 * 手机扫拍出场弹框(按次结算)
 */
public class CarNumberOutOnceDialog extends Dialog {

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
    private CheckBox cb_time_cash;
    private String final_money;
    private SharedPreferences cashmode_sp;// 结算方式
    private CarOrderActivity activity;
    private ArrayAdapter<String> collectAdapter;
    private String[] collect1;// 按次多价格数据；

    public CarNumberOutOnceDialog(Context context) {
        super(context);
    }

    public CarNumberOutOnceDialog(Context context, int theme, NfcOrder nfcOrder) {
        super(context, theme);
        this.context = context;
        this.nfcOrder = nfcOrder;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.photo_finish_order_once_dialog);
        cashmode_sp = context.getSharedPreferences("cashmode", Context.MODE_PRIVATE);
        activity = (CarOrderActivity) context;
        initView();
        setView();
    }

    public void initView() {
        tv_duration = (TextView) this.findViewById(R.id.tv_nfc_finish_once_duration);
        tv_cash_type = (TextView) this.findViewById(R.id.tv_nfc_finish_once_cash_type);
        ll_is_user = (LinearLayout) this.findViewById(R.id.ll_nfc_finish_once_isuser);
        tv_time = (TextView) this.findViewById(R.id.tv_nfc_finish_once_parktime);
        tv_carnumber = (TextView) this.findViewById(R.id.tv_nfc_finish_once_carnunber);
        tv_final_time_money = (TextView) this.findViewById(R.id.tv_nfc_finish_once_time_collect);
        bt_cancle = (Button) this.findViewById(R.id.bt_nfc_finish_once_cancle);
        bt_ok = (Button) this.findViewById(R.id.bt_nfc_finish_once_ok);
        view = findViewById(R.id.view_nfc_once_view);
        cb_once_cash = (CheckBox) findViewById(R.id.cb_nfc_finish_once_cash);
        cb_time_cash = (CheckBox) findViewById(R.id.cb_nfc_finish_once_timecash);
        rl_oncecash = (RelativeLayout) findViewById(R.id.rl_nfc_finish_once_cash);
        rl_timecash = (RelativeLayout) findViewById(R.id.rl_nfc_finish_once_time_cash);
        et_time_money = (EditText) findViewById(R.id.et_nfc_finish_once_time_collect);
        sp_once_money = (Spinner) findViewById(R.id.sp_nfc_finish_order_once_collect);

        if (cashmode_sp.getString("mcash", "time").equals("time")) {
            cb_time_cash.setChecked(true);
            cb_once_cash.setChecked(false);
            tv_cash_type.setText("按时段计费：");
            rl_timecash.setBackgroundColor(context.getResources().getColor(R.color.greenff));
            rl_oncecash.setBackgroundColor(context.getResources().getColor(R.color.white));
            if (nfcOrder.getIsedit() != null && nfcOrder.getIsedit().equals("0")) {// isedit
                // 按时价格是否可编辑
                // 0否，1是
                sp_once_money.setVisibility(View.GONE);
                et_time_money.setVisibility(View.GONE);
                tv_final_time_money.setVisibility(View.VISIBLE);
                tv_final_time_money.setText(nfcOrder.getCollect());
                final_money = nfcOrder.getCollect();
            } else {
                sp_once_money.setVisibility(View.GONE);
                tv_final_time_money.setVisibility(View.GONE);
                et_time_money.setVisibility(View.VISIBLE);
                et_time_money.setText(nfcOrder.getCollect());
            }
        } else {
            cb_once_cash.setChecked(true);
            cb_time_cash.setChecked(false);
            tv_cash_type.setText("按次计费：");
            rl_oncecash.setBackgroundColor(context.getResources().getColor(R.color.greenff));
            rl_timecash.setBackgroundColor(context.getResources().getColor(R.color.white));

            if (nfcOrder.getCollect1() != null && nfcOrder.getCollect1().length > 1) {
                sp_once_money.setVisibility(View.VISIBLE);
                et_time_money.setVisibility(View.GONE);
                tv_final_time_money.setVisibility(View.GONE);
                collect1 = new String[nfcOrder.getCollect1().length];
                for (int i = 0; i < nfcOrder.getCollect1().length; i++) {
                    collect1[i] = String.valueOf(nfcOrder.getCollect1()[i]);
                }
                collectAdapter = new ArrayAdapter<String>(context, R.layout.nfc_finish_dialog_spinner_item, collect1);
                sp_once_money.setAdapter(collectAdapter);
                sp_once_money.setOnItemSelectedListener(new OnceCashSpinnerSelectedListener());
            } else {
                sp_once_money.setVisibility(View.GONE);
                et_time_money.setVisibility(View.GONE);
                tv_final_time_money.setVisibility(View.VISIBLE);
                tv_final_time_money.setText(nfcOrder.getCollect0());
                final_money = nfcOrder.getCollect0();
            }
        }

        cb_time_cash.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (cb_time_cash.isChecked()) {
                    cb_time_cash.setChecked(true);
                    cb_once_cash.setChecked(false);
                    tv_cash_type.setText("按时段计费：");
                    cashmode_sp.edit().putString("mcash", "time").commit();
                    rl_timecash.setBackgroundColor(context.getResources().getColor(R.color.greenff));
                    rl_oncecash.setBackgroundColor(context.getResources().getColor(R.color.white));
                    if (nfcOrder.getIsedit() != null && nfcOrder.getIsedit().equals("0")) {// isedit
                        // 按时价格是否可编辑
                        // 0否，1是
                        sp_once_money.setVisibility(View.GONE);
                        et_time_money.setVisibility(View.GONE);
                        tv_final_time_money.setVisibility(View.VISIBLE);
                        tv_final_time_money.setText(nfcOrder.getCollect());
                        final_money = nfcOrder.getCollect();
                    } else {
                        sp_once_money.setVisibility(View.GONE);
                        tv_final_time_money.setVisibility(View.GONE);
                        et_time_money.setVisibility(View.VISIBLE);
                        et_time_money.setText(nfcOrder.getCollect());
                    }
                } else {
                    cb_time_cash.setChecked(true);
                }
            }
        });

        cb_once_cash.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (cb_once_cash.isChecked()) {
                    cb_once_cash.setChecked(true);
                    cb_time_cash.setChecked(false);
                    tv_cash_type.setText("按次计费：");
                    cashmode_sp.edit().putString("mcash", "once").commit();
                    rl_oncecash.setBackgroundColor(context.getResources().getColor(R.color.greenff));
                    rl_timecash.setBackgroundColor(context.getResources().getColor(R.color.white));
                    if (nfcOrder.getCollect1() != null && nfcOrder.getCollect1().length > 1) {
                        sp_once_money.setVisibility(View.VISIBLE);
                        et_time_money.setVisibility(View.GONE);
                        tv_final_time_money.setVisibility(View.GONE);
                        collect1 = new String[nfcOrder.getCollect1().length];
                        for (int i = 0; i < nfcOrder.getCollect1().length; i++) {
                            collect1[i] = String.valueOf(nfcOrder.getCollect1()[i]);
                        }
                        collectAdapter = new ArrayAdapter<String>(context, R.layout.nfc_finish_dialog_spinner_item, collect1);
                        sp_once_money.setAdapter(collectAdapter);
                        sp_once_money.setOnItemSelectedListener(new OnceCashSpinnerSelectedListener());
                    } else {
                        sp_once_money.setVisibility(View.GONE);
                        et_time_money.setVisibility(View.GONE);
                        tv_final_time_money.setVisibility(View.VISIBLE);
                        tv_final_time_money.setText(nfcOrder.getCollect0());
                        final_money = nfcOrder.getCollect0();
                    }
                } else {
                    cb_once_cash.setChecked(true);
                }
            }
        });
    }

    public void setView() {

        if (nfcOrder.getCarnumber() != null) {
            tv_carnumber.setText(nfcOrder.getCarnumber());
        }
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

        SharedPreferences spf = context.getSharedPreferences("autologin", Context.MODE_PRIVATE);
        String iscancle = spf.getString("iscancle", "1");
        if (iscancle.equals("1")) {
            view.setVisibility(View.GONE);
            bt_cancle.setVisibility(View.GONE);
            bt_ok.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (nfcOrder.getIsedit() != null && nfcOrder.getIsedit().equals("1") && cb_time_cash.isChecked()) {
                        final_money = et_time_money.getText().toString().trim();
                    }
                    activity.sumitCahsOrder(nfcOrder, nfcOrder.getOrderid(), final_money, CarNumberOutOnceDialog.this, "0");
                }
            });
        } else {
            view.setVisibility(View.VISIBLE);
            bt_cancle.setVisibility(View.VISIBLE);
            bt_ok.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (nfcOrder.getIsedit() != null && nfcOrder.getIsedit().equals("1") && cb_time_cash.isChecked()) {
                        final_money = et_time_money.getText().toString().trim();
                    }
                    activity.sumitCahsOrder(nfcOrder, nfcOrder.getOrderid(), final_money, CarNumberOutOnceDialog.this, "0");
                }
            });
            bt_cancle.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    CarNumberOutOnceDialog.this.dismiss();

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
