package com.tq.zld.view.fragment;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.BuyTicketDiscount;
import com.tq.zld.util.KeyboardUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.MathUtils;
import com.tq.zld.wxapi.WXPayEntryActivity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BuyTicketFragment extends NetworkFragment<BuyTicketDiscount> {

    private EditText mMoneyView;
    private EditText mNumberView;
    private TextView mTotalView;
    private TextView mOriginalTotalView;
    private Button mPayButton;
    private TextView mHintView;

    private BigDecimal mDiscount;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BuyTicketFragment.
     */
    public static BuyTicketFragment newInstance() {
        BuyTicketFragment fragment = new BuyTicketFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public BuyTicketFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getTitle() {
        return "购买停车券";
    }

    @Override
    protected void onNetWorkResponse(BuyTicketDiscount response) {
        if (response != null) {

            // 仅认证用户能设置停车券面额
            if (response.isauth == 1) {
                mMoneyView.setEnabled(true);
                mMoneyView.setTextColor(getResources().getColor(R.color.text_green));
                mMoneyView.requestFocus();
//                mMoneyView.setTextIsSelectable(true);
            } else {
                mNumberView.requestFocus();
//                mNumberView.setTextIsSelectable(true);
            }

            // 如果不打折，则隐藏hintView
            if (10 == response.auth) {
                mHintView.setVisibility(View.INVISIBLE);
                return;
            }

            // 认证未认证优惠相同
            BigDecimal tmp = new BigDecimal(0.1);
            if (response.auth == response.notauth) {
                mHintView.setText(String.format("享受%s限时优惠", MathUtils.parseIntString(response.auth)));
                mDiscount = new BigDecimal(response.auth).multiply(tmp).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            } else {
                // 区分认证未认证
                if (1 == response.isauth) {
                    // 认证用户
                    mHintView.setText(String.format("认证用户享受%s折优惠", MathUtils.parseIntString(response.auth)));
                    mDiscount = new BigDecimal(response.auth).multiply(tmp).setScale(2, BigDecimal.ROUND_HALF_EVEN);
                } else {
                    // 非认证用户
                    if (10 == response.notauth) {
                        mHintView.setText(String.format("认证后可享受%s折优惠", response.auth));
                    } else {
                        mHintView.setText(String.format("未认证%1$s折，认证后%2$s折",
                                MathUtils.parseIntString(response.notauth), MathUtils.parseIntString(response.auth)));
                    }
                    mDiscount = new BigDecimal(response.notauth).multiply(tmp).setScale(2, BigDecimal.ROUND_HALF_EVEN);
                }
            }
            mHintView.setVisibility(View.VISIBLE);

        } else {
            mHintView.setVisibility(View.INVISIBLE);
        }
        showDataView();
    }

    @Override
    protected int getFragmentContainerResID() {
        return R.id.fragment_container;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDiscount = new BigDecimal(1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_buy_ticket, container, false);
    }

    @Override
    protected TypeToken<BuyTicketDiscount> getBeanListType() {
        return null;
    }

    @Override
    protected Class<BuyTicketDiscount> getBeanClass() {
        return BuyTicketDiscount.class;
    }

    @Override
    protected String getUrl() {
        return TCBApp.mServerUrl + "carinter.do";
    }

    @Override
    protected Map<String, String> getParams() {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "prebuyticket");
        params.put("mobile", TCBApp.mMobile);
        return params;
    }

    @Override
    protected void initView(View view) {
        mMoneyView = (EditText) view.findViewById(R.id.et_buy_ticket_money);
        mMoneyView.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        mMoneyView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    mNumberView.requestFocus();
                    return true;
                }
                return false;
            }
        });
        mNumberView = (EditText) view.findViewById(R.id.et_buy_ticket_number);
        mNumberView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mNumberView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    KeyboardUtils.closeKeybord(mNumberView, getActivity());
                    return true;
                }
                return false;
            }
        });
        mTotalView = (TextView) view.findViewById(R.id.tv_buy_ticket_total);
        mOriginalTotalView = (TextView) view.findViewById(R.id.tv_buy_ticket_original);
        mOriginalTotalView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        mOriginalTotalView.setVisibility(View.GONE);
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String money = mMoneyView.getText() == null ? "" : mMoneyView.getText().toString();
                String number = mNumberView.getText() == null ? "" : mNumberView.getText().toString();
                int moneyInt = 0;
                int numberInt = 0;

                // 判断输入面额是否合法
                if (!TextUtils.isEmpty(money) && TextUtils.isDigitsOnly(money)) {
                    moneyInt = Integer.parseInt(money);
                    if (moneyInt > 20) {
                        Toast.makeText(getActivity(), "停车券面额最大为20元！", Toast.LENGTH_SHORT).show();
                        mMoneyView.setText("20");
                        mMoneyView.setSelection(2);
                        moneyInt = 20;
                    }
                }

                // 输完金额焦点自动切换到数量
                if (money.length() == 2 && mMoneyView.isFocused()) {
                    mNumberView.requestFocus();
                }

                // 判断输入数量是否合法
                if (!TextUtils.isEmpty(number) && TextUtils.isDigitsOnly(number)) {
                    numberInt = Integer.parseInt(number);
                    if (numberInt > 99) {
                        Toast.makeText(getActivity(), "单次购买数量上限为99张！", Toast.LENGTH_SHORT).show();
                        mNumberView.setText("99");
                        mNumberView.setSelection(2);
                        numberInt = 99;
                    }
                }
                // 输完数量关闭输入框
                if (number.length() == 2 && mNumberView.isFocused()) {
                    KeyboardUtils.closeKeybord(mNumberView, getActivity());
                    mPayButton.requestFocus();
                }
                updateTotalView(moneyInt, numberInt);
            }
        };
        mMoneyView.addTextChangedListener(watcher);
        mMoneyView.setText("1");
        mMoneyView.setEnabled(false);
        mMoneyView.setTextColor(getResources().getColor(R.color.text_gray));
        mMoneyView.setSelection(mMoneyView.getText().length());
        mNumberView.addTextChangedListener(watcher);
        mNumberView.setText("");
        mNumberView.setSelection(mNumberView.getText().length());
        mPayButton = (Button) view.findViewById(R.id.btn_buy_ticket_buy);
        mPayButton.setOnClickListener(this);
        mHintView = (TextView) view.findViewById(R.id.tv_buy_ticket_hint);
        view.findViewById(R.id.ll_buy_ticket_money).setOnClickListener(this);
        view.findViewById(R.id.ll_buy_ticket_number).setOnClickListener(this);
    }

    private void updateTotalView(int money, int number) {
        if (0 == money || 0 == number) {
            mTotalView.setText("0.0");
            mTotalView.setTag("0");
            mOriginalTotalView.setVisibility(View.GONE);
        } else {
            int total = money * number;

            // 如果没有折扣优惠，则不显示原始金额
            if (mDiscount.intValue() == 1) {
                mOriginalTotalView.setVisibility(View.GONE);
            } else {
                mOriginalTotalView.setText("¥" + total);
                mOriginalTotalView.setVisibility(View.VISIBLE);
            }
            BigDecimal totalDecimal = new BigDecimal(total).multiply(mDiscount).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            String totalStr = MathUtils.parseIntString(totalDecimal.doubleValue());
            mTotalView.setText(totalStr);
            mTotalView.setTag(totalStr);

            LogUtils.i(getClass(), "discount: --->> " + mDiscount.toString());
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mPayButton) {
            onPayButtonClicked();
            return;
        }
        switch (v.getId()) {
            case R.id.ll_buy_ticket_money:

                // 仅当用户认证过时才可以改变面额大小
                if (mMoneyView.isEnabled()) {
                    mMoneyView.requestFocus();
                    KeyboardUtils.openKeybord(mMoneyView, getActivity());
                }
                break;
            case R.id.ll_buy_ticket_number:
                mNumberView.requestFocus();
                KeyboardUtils.openKeybord(mNumberView, getActivity());
                break;
            default:
                break;
        }
    }

    private void onPayButtonClicked() {
        String total = (String) mTotalView.getTag();
        if ("0".equals(total)) {
            Toast.makeText(TCBApp.getAppContext(), "请输入正确的面额及数量！", Toast.LENGTH_SHORT).show();
            return;
        }
        startWXPayEntryActivity(total);
    }

    private void startWXPayEntryActivity(String total) {
        String number = mNumberView.getText().toString();
        String value = mMoneyView.getText().toString();
        Intent intent = new Intent(TCBApp.getAppContext(), WXPayEntryActivity.class);
        intent.putExtra(WXPayEntryActivity.ARG_SUBJECT, String.format("购买%1$s张%2$s元停车券", number, value));
        intent.putExtra(WXPayEntryActivity.ARG_TOTALFEE, total);
        intent.putExtra(WXPayEntryActivity.ARG_PRODTYPE, WXPayEntryActivity.PROD_BUY_TICKET);
        intent.putExtra(WXPayEntryActivity.ARG_TICKET_NUMBER, number);
        intent.putExtra(WXPayEntryActivity.ARG_TICKET_VALUE, value);
        startActivity(intent);
    }

}
