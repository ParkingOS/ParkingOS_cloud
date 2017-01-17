package com.tq.zld.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.AccountInfo;
import com.tq.zld.bean.Order;
import com.tq.zld.util.Common;
import com.tq.zld.util.KeyboardUtils;
import com.tq.zld.util.MathUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.MainActivity;
import com.tq.zld.wxapi.WXPayEntryActivity;

import java.math.BigDecimal;
import java.util.HashMap;

public class RechargeFragment extends BaseFragment implements OnClickListener {

    public static final String ARG_WHO = "who";

    private EditText mMoneyEditText;
    private Button mPayButton;
    private TextView mHintTextView;
    /**
     * 充值送礼包提示信息
     */
    private TextView mRechargeActivityTextView;
    private TextView mQuickInputTextView1;
    private TextView mQuickInputTextView2;
    private TextView mQuickInputTextView3;
    private TextView mQuickInputTextView4;

    private Order mOrder;
    private AccountInfo mAccount;

    private String mTitle = "充值";
    private BigDecimal mLimit = new BigDecimal(0);

    private MainActivity mActivity;

    public static RechargeFragment newInstance(Order order) {
        RechargeFragment fragment = new RechargeFragment();
        if (order != null) {
            Bundle args = new Bundle();
            args.putParcelable(ARG_WHO, order);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_WHO)) {
            Parcelable arg = getArguments().getParcelable(ARG_WHO);
            if (arg instanceof Order) {
                this.mOrder = (Order) arg;
                if (TextUtils.isEmpty(mOrder.payee.name)) {
                    mOrder.payee.name = "收费员";
                }
            } else if (arg instanceof AccountInfo) {
                this.mAccount = (AccountInfo) arg;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recharge, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mHintTextView = (TextView) view.findViewById(R.id.tv_recharge_hint);

        mMoneyEditText = (EditText) view.findViewById(R.id.et_recharge_money);
        mMoneyEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                String str = s.toString();

                if (s.length() > 1 && str.startsWith("0")
                        && !str.startsWith("0.")) {
                    mMoneyEditText.setText(str.substring(1));// 设置首字母不能输入0
                    mMoneyEditText.setSelection(mMoneyEditText.getText()
                            .length());
                    return;
                }
                if (str.contains(".")
                        && str.indexOf(".") < s.length() - 3) {
                    mMoneyEditText.setText(str.substring(0,
                            str.indexOf(".") + 3));// 设置最低充值到：分
                    mMoneyEditText.setSelection(mMoneyEditText.getText()
                            .length());
                    return;
                }

                if (!str.equals(mQuickInputTextView1.getText())
                        && !str.equals(mQuickInputTextView2.getText())
                        && !str.equals(mQuickInputTextView3.getText())
                        && !str.equals(mQuickInputTextView4.getText())) {
                    clearSelection();
                }
            }
        });
        mPayButton = (Button) view.findViewById(R.id.bt_recharge_pay);
        mPayButton.setOnClickListener(this);
        mQuickInputTextView1 = (TextView) view.findViewById(R.id.tv_recharge_1);
        mQuickInputTextView1.setOnClickListener(this);
        mQuickInputTextView2 = (TextView) view.findViewById(R.id.tv_recharge_2);
        mQuickInputTextView2.setOnClickListener(this);
        mQuickInputTextView3 = (TextView) view.findViewById(R.id.tv_recharge_3);
        mQuickInputTextView3.setOnClickListener(this);
        mQuickInputTextView4 = (TextView) view.findViewById(R.id.tv_recharge_4);
        mQuickInputTextView4.setOnClickListener(this);

        initPayTipView(view);
        if (mAccount != null && mAccount.limit_balan < mAccount.limit) {
            BigDecimal limit = new BigDecimal(mAccount.limit).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal balan = new BigDecimal(mAccount.limit_balan).setScale(2, BigDecimal.ROUND_HALF_UP);
            mLimit = limit.subtract(balan).setScale(2, BigDecimal.ROUND_CEILING);
//            String limitStr = mLimit.toString();
//            mMoneyEditText.setText(limitStr);
//            mMoneyEditText.setSelection(limitStr.length());
        }
// else {
        onClick(mQuickInputTextView3);
//        }

        // TODO 解决Activity onCreateOption未初始化完成导致mActivity.disableAllMenuItems()空指针异常的问题
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 显示帮助菜单项
                showHelpMenuItem(true);
            }
        }, 500);
    }


    private void initPayTipView(@NonNull View fragmentView) {
        if (mOrder != null) {
            getTicketMax();
            // 设置hint
            mHintTextView.setText("对该收费员停车券最多抵扣2元打赏");
            mMoneyEditText.setHint("输入打赏金额（元）");

            // 设置快速输入金额
            mQuickInputTextView1.setText("1");
            mQuickInputTextView2.setText("2");
            mQuickInputTextView3.setText("3");
            mQuickInputTextView4.setText("4");

            // 设置按钮文字
            mPayButton.setText("去打赏");

            // 设置标题
            mTitle = String.format("打赏给%1s(编号：%2s)", mOrder.payee.name, mOrder.payee.id);
        } else {
            mRechargeActivityTextView = (TextView) fragmentView.findViewById(R.id.tv_recharge_activity);
            getRechargeActivity();
        }
    }

    private void getTicketMax() {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "getrewardquota");
        params.put("pid", mOrder.payee.id);
        params.put("mobile", TCBApp.mMobile);
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "carinter.do", params);
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (!TextUtils.isEmpty(s)) {
                    mHintTextView.setText("对该收费员停车券最多抵扣" + MathUtils.parseIntString(s) + "元打赏");
                }
            }
        }, null);
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    private void getRechargeActivity() {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "getchargewords");
        params.put("mobile", TCBApp.mMobile);
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "carinter.do", params);
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (!TextUtils.isEmpty(s)) {
                    mRechargeActivityTextView.setText(s);
                }
            }
        }, null);
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TCBApp.getAppContext().cancelPendingRequests(this);
        showHelpMenuItem(false);
    }

    private void clearSelection() {
        mQuickInputTextView1.setSelected(false);
        mQuickInputTextView2.setSelected(false);
        mQuickInputTextView3.setSelected(false);
        mQuickInputTextView4.setSelected(false);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bt_recharge_pay:
                onPayButtonClicked();
                break;
            default:
                clearSelection();
                v.setSelected(true);
                mMoneyEditText.setText(((TextView) v).getText());
                mMoneyEditText.setSelection(mMoneyEditText.getText().length());
                break;
        }
    }

    private void onPayButtonClicked() {
        String money = mMoneyEditText.getText().toString();
        money = Common.checkMoney(money);
        if (TextUtils.isEmpty(money)) {
            Toast.makeText(getActivity(), "充值金额不正确！", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        BigDecimal moneyDecimal = new BigDecimal(money);
        if (moneyDecimal.compareTo(mLimit) == -1) {
            Toast.makeText(TCBApp.getAppContext(), "充值金额不得低于信用欠费: " + mLimit.toString() + "元", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mPayButton.getText().toString().contains("打赏") && moneyDecimal.compareTo(new BigDecimal(500)) == 1) {
            Toast.makeText(TCBApp.getAppContext(), "单次充值金额不得超过500元！", Toast.LENGTH_SHORT).show();
            mMoneyEditText.setText("500");
            return;
        }
        Intent intent = new Intent(TCBApp.getAppContext(),
                WXPayEntryActivity.class);

        //设置参数
        intent.putExtra(WXPayEntryActivity.ARG_TOTALFEE, money);

        String subject = "账户充值";
        String prodType = WXPayEntryActivity.PROD_RECHARGE;
        if (mOrder != null) {
            // 付小费
            subject = String.format("支付打赏_%1s(编号：%2s)", mOrder.payee.name, mOrder.payee.id);
            intent.putExtra(WXPayEntryActivity.ARG_ORDER_UID, mOrder.payee.id);
            intent.putExtra(WXPayEntryActivity.ARG_ORDER_ID, mOrder.getOrderid());
            prodType = WXPayEntryActivity.PROD_PAY_TIP;
        }
        intent.putExtra(WXPayEntryActivity.ARG_PRODTYPE,
                prodType);
        intent.putExtra(WXPayEntryActivity.ARG_SUBJECT, subject);

        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        KeyboardUtils.closeKeybord(mMoneyEditText, getActivity());
    }

    @Override
    protected String getTitle() {
        return mTitle;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof MainActivity) {
            this.mActivity = (MainActivity) activity;
        }
    }

    private void showHelpMenuItem(boolean show) {
        if (show && "充值".equals(mTitle) && mActivity != null) {
            mActivity.disableAllMenuItems();
            mActivity.setMenuItemEnabled(R.id.action_recharge_help);
        } else if (mActivity != null) {
            mActivity.setMenuItemDisabled(R.id.action_recharge_help);
        }
    }
}
