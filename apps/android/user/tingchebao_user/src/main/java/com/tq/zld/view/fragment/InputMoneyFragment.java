package com.tq.zld.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.ParkingFeeCollector;
import com.tq.zld.util.Common;
import com.tq.zld.util.KeyboardUtils;
import com.tq.zld.wxapi.WXPayEntryActivity;

public class InputMoneyFragment extends BaseFragment implements OnClickListener {

    public static final String ARG_COLLECTOR = "collector";

    private ScrollView mScrollView;
    private Button mPayButton;
    private EditText mMoneyEditView;

    private ParkingFeeCollector mCollector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCollector = getArguments().getParcelable(ARG_COLLECTOR);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inputmoney, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 设置查看详情可点击
        view.findViewById(R.id.rl_inputmoney_detail).setOnClickListener(this);

        mScrollView = (ScrollView) view.findViewById(R.id.sv_inputmoney_root);
        TextView nameTextView = (TextView) view.findViewById(R.id.tv_inputmoney_name);
        TextView parkTextView = (TextView) view
                .findViewById(R.id.tv_inputmoney_parkname);
        mPayButton = (Button) view.findViewById(R.id.tv_inputmoney_pay);
        mPayButton.setOnClickListener(this);
        mMoneyEditView = (EditText) view.findViewById(R.id.et_inputmoney_money);
        mMoneyEditView.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // rootView.fullScroll(View.FOCUS_UP);
                    onClick(mPayButton);
                }
                return false;
            }
        });
        mMoneyEditView.addTextChangedListener(new TextWatcher() {

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
                if (s.length() > 0) {
                    mScrollView.fullScroll(View.FOCUS_DOWN);
                }
                if (s.length() > 1 && s.toString().startsWith("0")
                        && !s.toString().startsWith("0.")) {
                    mMoneyEditView.setText(s.toString().substring(1));// 设置首字母不能输入0
                    mMoneyEditView.setSelection(mMoneyEditView.getText()
                            .length());
                    return;
                }
                if (s.toString().contains(".")
                        && s.toString().indexOf(".") < s.length() - 3) {
                    mMoneyEditView.setText(s.toString().substring(0,
                            s.toString().indexOf(".") + 3));// 设置最低充值到：分
                    mMoneyEditView.setSelection(mMoneyEditView.getText()
                            .length());
                }
            }
        });

        // 设置姓名，车场名称
//        String name = "编号:" + mCollector.id;
//        if (!TextUtils.isEmpty(mCollector.name)) {
//            name = mCollector.name + "(" + name + ")";
//        }
//        nameTextView.setText(name);
        nameTextView.setText(String.format("%1s(编号：%2s)", mCollector.name, mCollector.id));
        parkTextView.setText(mCollector.parkname);

        // 设置头像
        ImageView photoImageView = (ImageView) view
                .findViewById(R.id.iv_inputmoney_photo);
        if ("23".equals(mCollector.online)) {
            ImageLoader.getInstance().displayImage(
                    "drawable://" + R.drawable.img_parkingfee_collector_online,
                    photoImageView);
        } else {
            ImageLoader
                    .getInstance()
                    .displayImage(
                            "drawable://"
                                    + R.drawable.img_parkingfee_collector_offline,
                            photoImageView);
        }

        // 弹出输入法键盘
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mMoneyEditView
                        .getContext().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mMoneyEditView, 0);
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        }, 200);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_inputmoney_pay:
                onPayButtonClicked();
                break;
            case R.id.rl_inputmoney_detail:
                onDetailViewClicked();
                break;
        }
    }

    private void onPayButtonClicked() {
        String money = mMoneyEditView.getText().toString();
        money = Common.checkMoney(money);
        if (TextUtils.isEmpty(money)) {
            Toast.makeText(getActivity(), "请输入有效金额！", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        Intent intent = new Intent(TCBApp.getAppContext(),
                WXPayEntryActivity.class);
        intent.putExtra(WXPayEntryActivity.ARG_PRODTYPE,
                WXPayEntryActivity.PROD_PAY_MONEY);
        intent.putExtra(WXPayEntryActivity.ARG_SUBJECT, "直付停车费_"
                + mCollector.name + "(账号: " + mCollector.id + ")");
        intent.putExtra(WXPayEntryActivity.ARG_TOTALFEE, money);
        intent.putExtra(WXPayEntryActivity.ARG_ORDER_UID, mCollector.id);
        startActivity(intent);
    }

    private void onDetailViewClicked() {
        KeyboardUtils.closeKeybord(mMoneyEditView, getActivity());
        replace(R.id.fragment_container, ParkCollectorDetailFragment.newInstance(mCollector), true);
    }

    @Override
    public void onPause() {
        if (mMoneyEditView != null) {
            KeyboardUtils.closeKeybord(mMoneyEditView, getActivity());
        }
        super.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (mMoneyEditView != null) {
                KeyboardUtils.openKeybord(mMoneyEditView, getActivity());
            }
        }
    }

    @Override
    protected String getTitle() {
        return "输入金额";
    }
}
