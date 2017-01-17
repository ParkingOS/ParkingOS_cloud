package com.tq.zld.wxapi;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.AccountInfo;
import com.tq.zld.bean.Coupon;
import com.tq.zld.bean.Order;
import com.tq.zld.bean.PayResult;
import com.tq.zld.pay.AliPayResult;
import com.tq.zld.pay.Keys;
import com.tq.zld.pay.Rsa;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.protocal.PollingProtocol;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.MathUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.view.LoginActivity;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.ShareActivity;
import com.tq.zld.view.fragment.ChooseTicketFragment;
import com.tq.zld.view.fragment.PayResultFragment;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WXPayEntryActivity extends BaseActivity implements
        IWXAPIEventHandler, android.view.View.OnClickListener {

    /**
     * 支付结果
     */
    public static final int MSG_WHAT_PAYRESULT = 0;
    public static final int MSG_WHAT_SHOW_DIALOG = 1;

    // 通用属性
    public static final String ARG_PRODTYPE = "ptype";// 产品类型
    public static final String ARG_SUBJECT = "subject";// 产品名称
    public static final String ARG_TOTALFEE = "total";// 金额

    // 购买包月产品属性
    public static final String ARG_MONTYLYPAY_ID = "productid";// 月卡ID
    public static final String ARG_MONTYLYPAY_NUMBER = "number";// 购买时长
    public static final String ARG_MONTYLYPAY_START = "start";// 起始月份

    // 支付停车费属性
    public static final String ARG_ORDER_ID = "orderid";// 订单ID

    // 直付停车费属性
    public static final String ARG_ORDER_UID = "uid";// 收费员编号

    // 购买停车券属性
    public static final String ARG_TICKET_VALUE = "value";
    public static final String ARG_TICKET_NUMBER = "number";

    // 产品类型：0-->账户充值；1-->包月产品；2-->停车费结算；3-->直付;4-->付小费
    public static final String PROD_RECHARGE = "0";
    public static final String PROD_MONTHLY_PAY = "1";
    public static final String PROD_PARKING_FEE = "2";
    public static final String PROD_PAY_MONEY = "3";
    public static final String PROD_PAY_TIP = "4";
    public static final String PROD_BUY_TICKET = "5";

    // 支付方式：
    public static final int PAYTYPE_BALANCE = 0;// 余额支付
    public static final int PAYTYPE_ALIPAY = 1;// 支付宝支付
    public static final int PAYTYPE_WXPAY = 2;// 微信支付

    private boolean mWXPayResultGetted = true;// 获取到微信支付结果的标志位

    private TextView mCouponTextView;
    private TextView mBalanceTextView;
    private TextView mWXPayTextView;
    private TextView mAliPayTextView;
    private CheckBox mBalanceCheckBox;
    private CheckBox mWXPayCheckBox;
    private CheckBox mAliPayCheckBox;
    private View mCouponView;
    private View mBalanceView;
    private View mAliPayView;
    private View mWXPayView;

    private IWXAPI iWXApi;
    private Keys mKey;

    private HashMap<String, String> mOrder;// 订单

    private int mPayType;// 支付方式：默认余额支付

    private ProgressDialog mPayDialog;

    private boolean mStartedThreePartPay;
    private boolean mResumed;

    private AccountInfo mAccountInfo;// 用户账户信息

    public static PayResultHandler mPayResultHandler;
    public static final int REQUEST_CODE_CHOOSE_TICKET = 0;

    /**
     * 利用Handler轮询取消息
     */
    private PollingProtocol mPollingProtocol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        initToolbar();
        if (savedInstanceState != null) {
            setIntent((Intent) savedInstanceState.getParcelable("intent"));
        }
        initData();
        initView();

        // 判断是否登陆
        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            doLogin();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("intent", getIntent());
    }

    private void doLogin() {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    // 从Intent中解析出订单参数
    private void initData() {
        mKey = new Keys();
        mPayResultHandler = new PayResultHandler(this);

        // 初始化支付参数
        mOrder = new HashMap<>();
        mOrder.put(ARG_SUBJECT, getIntent().getStringExtra(ARG_SUBJECT));
        mOrder.put(ARG_TOTALFEE, getIntent().getStringExtra(ARG_TOTALFEE));// 订单总金额
        mOrder.put("money", getIntent().getStringExtra(ARG_TOTALFEE));// 扣除停车券还需实际支付金额
        mOrder.put(ARG_PRODTYPE, getIntent().getStringExtra(ARG_PRODTYPE));
        mOrder.put("mobile", TCBApp.mMobile);
    }

    private void getAccountInfo() {

        final ProgressDialog dialog = ProgressDialog.show(this, "", "", false,
                false);
        String url = TCBApp.mServerUrl + "carowner.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "getaccount");
        params.put("mobile", TCBApp.mMobile);
        params.put("orderid", mOrder.get(ARG_ORDER_ID));
        params.put("uid", mOrder.get(ARG_ORDER_UID));
        params.put("total", mOrder.get(ARG_TOTALFEE));
        params.put("ptype", mOrder.get(ARG_PRODTYPE));
        // 新版选券，可选大金额券，但抵扣金额有限制
        //老版utype:1,2.3.1 utype:2
        params.put("utype", "2");
        GsonRequest<AccountInfo> request = new GsonRequest<>(
                URLUtils.genUrl(url, params), AccountInfo.class,
                new Listener<AccountInfo>() {

                    @Override
                    public void onResponse(AccountInfo account) {

                        dialog.dismiss();
                        mAccountInfo = account;
                        // 设置所选停车券
                        Coupon c = null;
                        if (mAccountInfo.tickets != null && mAccountInfo.tickets.size() > 0) {
                            c = mAccountInfo.tickets.get(0);
                        }
                        refreshCouponView(c);
                    }
                }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                String text = "未知错误";
                if (error instanceof NoConnectionError
                        || error instanceof NetworkError) {
                    text = "网络错误！";
                } else if (error instanceof ParseError) {
                    text = "数据格式错误！";
                } else if (error instanceof TimeoutError) {
                    text = "连接超时！";
                } else if (error instanceof ServerError) {
                    text = getString(R.string.err_msg_server_error);
                }
                Toast.makeText(getApplicationContext(), text,
                        Toast.LENGTH_SHORT).show();
            }
        });
        request.setRetryPolicy(
                new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    private void initView() {

        TextView subjectTextView = (TextView) findViewById(R.id.tv_pay_subject);
        subjectTextView.setText(mOrder.get(ARG_SUBJECT));
        TextView totalTextView = (TextView) findViewById(R.id.tv_pay_total);
        totalTextView.setText("￥" + mOrder.get(ARG_TOTALFEE));
        findViewById(R.id.btn_pay_pay).setOnClickListener(this);

        String prodType = mOrder.get(ARG_PRODTYPE);

        // TODO NullPointException？？
        if (TextUtils.isEmpty(prodType)) {
            LogUtils.e(getClass(), "--->> product type can't be empty!!!");
            finish();
            return;
        }

        showWhichView(prodType);
    }

    /**
     * 根据购买的产品类型显示不同的布局方式
     *
     * @param prodType
     */
    private void showWhichView(String prodType) {

        showAliPayView(true);
        showWXPayView(true);

        switch (prodType) {
            case PROD_PAY_MONEY:// 直付停车费的布局初始化
            case PROD_PAY_TIP:// 支付小费的布局初始化
            case PROD_PARKING_FEE:// 支付停车费的布局初始化

                showCouponView(true);
                showBalanceView(true);

                mOrder.put(ARG_ORDER_ID, getIntent().getStringExtra(ARG_ORDER_ID));
                mOrder.put(ARG_ORDER_UID, getIntent().getStringExtra(ARG_ORDER_UID));

                break;
            case PROD_RECHARGE:// 充值的布局初始化
                // 无额外参数

                showCouponView(false);
                showBalanceView(false);

                refreshPayView(TCBApp.getAppContext().readInt(R.string.sp_pay_type, PAYTYPE_WXPAY));

                break;
            case PROD_BUY_TICKET:
                showCouponView(false);
                showBalanceView(true);
                mOrder.put(ARG_TICKET_NUMBER, getIntent().getStringExtra(ARG_TICKET_NUMBER));
                mOrder.put(ARG_TICKET_VALUE, getIntent().getStringExtra(ARG_TICKET_VALUE));
                break;
            case PROD_MONTHLY_PAY:// 购买月卡的布局初始化

                showCouponView(false);
                showBalanceView(true);

                mOrder.put(ARG_MONTYLYPAY_ID,
                        getIntent().getStringExtra(ARG_MONTYLYPAY_ID));
                mOrder.put(ARG_MONTYLYPAY_NUMBER,
                        getIntent().getStringExtra(ARG_MONTYLYPAY_NUMBER));
                mOrder.put(ARG_MONTYLYPAY_START,
                        getIntent().getStringExtra(ARG_MONTYLYPAY_START));

                break;
            default:
                break;
        }
    }

    // 是否显示停车券布局
    private void showCouponView(boolean show) {

        mCouponView = findViewById(R.id.ll_pay_coupon);

        if (show) {
            mCouponView.setVisibility(View.VISIBLE);
            mCouponView.setOnClickListener(this);
            mCouponTextView = (TextView) findViewById(R.id.tv_pay_coupon);
        } else {
            mCouponView.setVisibility(View.GONE);
        }
    }

    // 是否显示支付宝支付布局
    private void showAliPayView(boolean show) {
        mAliPayView = findViewById(R.id.rl_pay_alipay);
        if (show) {
            mAliPayView.setVisibility(View.VISIBLE);
            mAliPayView.setOnClickListener(this);
            mAliPayTextView = (TextView) findViewById(R.id.tv_pay_alipay);
            mAliPayCheckBox = (CheckBox) findViewById(R.id.cb_pay_alipay);
        } else {
            mAliPayView.setVisibility(View.GONE);
        }
    }

    // 是否显示微信支付布局
    private void showWXPayView(boolean show) {
        // 初始化微信支付布局
        mWXPayView = findViewById(R.id.rl_pay_wxpay);
        iWXApi = WXAPIFactory.createWXAPI(this, Keys.WXPAY_APPID);
        if (!(iWXApi.isWXAppInstalled() && iWXApi.isWXAppSupportAPI())) {
            mWXPayView.setVisibility(View.GONE);
            return;
        }
        if (show) {
            mWXPayView.setVisibility(View.VISIBLE);
            mWXPayView.setOnClickListener(this);
            mWXPayTextView = (TextView) findViewById(R.id.tv_pay_wxpay);
            mWXPayCheckBox = (CheckBox) findViewById(R.id.cb_pay_wxpay);
        } else {
            mWXPayView.setVisibility(View.GONE);
        }
    }

    // 是否显示余额支付布局
    private void showBalanceView(boolean show) {
        mBalanceView = findViewById(R.id.rl_pay_balance);
        if (show) {
            mBalanceView.setVisibility(View.VISIBLE);
            mBalanceView.setOnClickListener(this);
            mBalanceTextView = (TextView) findViewById(R.id.tv_pay_balance);
            mBalanceCheckBox = (CheckBox) findViewById(R.id.cb_pay_balance);
        } else {
            mBalanceView.setVisibility(View.GONE);
        }
    }

    // 更新停车券布局
    private void refreshCouponView(Coupon coupon) {

        if (View.VISIBLE == mCouponView.getVisibility()) {
            if (coupon != null) {

                if ("-1".equals(coupon.id)) {
                    mCouponTextView.setText("不使用停车券");
                    mCouponTextView.setTag(null);
                    mOrder.put("money", mOrder.get(ARG_TOTALFEE));
                } else {
                    // 默认所选停车券金额小于限额
                    String chooseCoupon = null;

                    if (coupon.isbuy == 1) {
                        chooseCoupon = String.format("已选择%s元<font color='#32a669'>购买券</font>", coupon.money);
                    } else {
                        if ("1".equals(coupon.type)) {
                            chooseCoupon = String.format("已选择%s元<font color='#32a669'>专用券</font>", coupon.money);
                        } else {
                            chooseCoupon = String.format("已选择%s元停车券", coupon.money);
                        }
                    }

                    //2.3.1新规则，如果券面额 > 限制 则提示。
                    if (Double.parseDouble(coupon.money) > coupon.limit) {
                        chooseCoupon += "<br><small><font color='#e37479'>此次仅可抵扣"+ MathUtils.parseIntString(coupon.limit) + "元</font></small>";
                        coupon.money = String.valueOf(coupon.limit);
                    }

                    mCouponTextView.setText(Html.fromHtml(chooseCoupon));
                    mCouponTextView.setTag(coupon);
                }
                mOrder.put("ticketid", String.valueOf(coupon.id));

                // 计算实际需要支付的金额
                BigDecimal totalFee = new BigDecimal(mOrder.get(ARG_TOTALFEE));
                BigDecimal couponMoney = new BigDecimal(coupon.money);
                BigDecimal payMoney = totalFee.subtract(couponMoney).setScale(2, BigDecimal.ROUND_CEILING);
                String money = payMoney.toString();
                if (payMoney.doubleValue() < 0) {
                    money = "0.00";
                }
                mOrder.put("money", money);
            } else {
                // 无可用停车券
                mCouponTextView.setText("暂无可用停车券");
                mCouponTextView.setTag(null);
                mCouponTextView.setCompoundDrawablesWithIntrinsicBounds(null,
                        null, null, null);
                mCouponView.setClickable(false);
                mOrder.put("ticketid", "-1");
                mOrder.put("money", mOrder.get(ARG_TOTALFEE));
            }

        }
        // 更新支付方式布局
        refreshPayView(PAYTYPE_BALANCE);
    }

    private void refreshPayView(int expectedPayType) {
        BigDecimal totalPay = new BigDecimal(mOrder.get(ARG_TOTALFEE)).setScale(2, BigDecimal.ROUND_CEILING);// 总停车费金额
        BigDecimal balance = new BigDecimal(0);
        if (mAccountInfo != null && !TextUtils.isEmpty(mAccountInfo.balance)) {
            balance = new BigDecimal(mAccountInfo.balance);
        }
        if (mCouponTextView != null && mCouponTextView.getTag() != null) {
            Coupon c = (Coupon) mCouponTextView.getTag();
            totalPay = totalPay.subtract(new BigDecimal(c.money)).setScale(2, BigDecimal.ROUND_CEILING);
        }

        setSelectedPayView(totalPay, balance, expectedPayType);

    }

    /**
     * 设置选择的支付方式
     *
     * @param totalPay        用户实际支付的金额：总金额减掉停车券面额（如果停车券可用的话）
     * @param balance         用户账户余额：如果余额不可用，则为0
     * @param expectedPayType 期望的支付方式：用户主动点选的（微信或者支付宝，余额则忽略）
     */
    private void setSelectedPayView(BigDecimal totalPay, BigDecimal balance,
                                    int expectedPayType) {

        // 先清除已选择的支付方式
        clearSelectedPayView();

        if (totalPay.doubleValue() <= 0) {// 停车券大于停车费
            mBalanceView.setClickable(false);
            mBalanceTextView.setText("无需余额");
            mBalanceCheckBox.setChecked(true);
            mWXPayView.setClickable(false);
            mAliPayView.setClickable(false);
            return;
        }

        // 计算余额+停车券是否够支付停车费
        BigDecimal extraMoney = totalPay.subtract(balance).setScale(2);
        if (extraMoney.compareTo(new BigDecimal(0)) == 1) {// 余额不足

            String extra = extraMoney.toPlainString();

            mWXPayView.setClickable(true);
            mAliPayView.setClickable(true);

            if (View.VISIBLE == mBalanceView.getVisibility()) {
                mBalanceView.setClickable(false);
                mBalanceCheckBox.setChecked(true);
                mBalanceTextView.setText("￥" + balance.toString());
            }

            switch (expectedPayType) {
                case PAYTYPE_WXPAY:
                    // 微信支付
                    setPayTypeToWX(extra);
                    break;
                case PAYTYPE_ALIPAY:
                    // 支付宝
                    setPayTypeToAli(extra);
                    break;

                default:
                    if (View.VISIBLE != mWXPayView.getVisibility()
                            || PAYTYPE_ALIPAY == TCBApp.getAppContext().readInt(R.string.sp_pay_type, PAYTYPE_WXPAY)) {
                        setPayTypeToAli(extra);
                    } else {
                        setPayTypeToWX(extra);
                    }
                    break;
            }
        } else {// 余额充足
            setPayTypeToBalance(totalPay.toString());
        }
    }

    private void setPayTypeToBalance(String money) {
        mWXPayView.setClickable(false);
        mAliPayView.setClickable(false);
        mBalanceView.setClickable(false);
        mBalanceCheckBox.setChecked(true);
        mBalanceTextView.setText("￥" + money);
        mPayType = PAYTYPE_BALANCE;
    }

    private void setPayTypeToAli(String money) {
        mAliPayTextView.setText("￥" + money);
        mAliPayCheckBox.setChecked(true);
        mPayType = PAYTYPE_ALIPAY;
    }

    private void setPayTypeToWX(String money) {
        if (mWXPayTextView != null) {
            mWXPayTextView.setText("￥" + money);
        }
        mWXPayCheckBox.setChecked(true);
        mPayType = PAYTYPE_WXPAY;
    }

    // 清除当前选中的支付方式
    private void clearSelectedPayView() {

        if (mWXPayTextView != null) {
            mWXPayTextView.setText("");
            mWXPayCheckBox.setChecked(false);
        }
        if (mAliPayTextView != null) {
            mAliPayTextView.setText("");
            mAliPayCheckBox.setChecked(false);
        }
        mWXPayView.setClickable(true);
        mAliPayView.setClickable(true);
    }

    private void initToolbar() {
        Toolbar mBar = (Toolbar) findViewById(R.id.widget_toolbar);
        mBar.setTitle("选择支付方式");
        setSupportActionBar(mBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        setWXPayEntryActivityResumed(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setWXPayEntryActivityResumed(true);
        if (mPayResultHandler != null) {
            mPayResultHandler.sendEmptyMessage(MSG_WHAT_SHOW_DIALOG);
            // 开启轮询获取支付结果
            if (mStartedThreePartPay) {
//                TimerUtils.startPollingService(this, 0, 2);
                if (mPollingProtocol == null) {
                    mPollingProtocol = new PollingProtocol(mPayResultHandler);
                }
                mPollingProtocol.startPolling();
            }
        }

        LogUtils.i(getClass(), "payType: --->> " + mPayType
                + "\nwxPayResultGetted: --->> " + mWXPayResultGetted);
        // 微信未登录时取消支付
        if (PAYTYPE_WXPAY == mPayType && !mWXPayResultGetted) {
            Message.obtain(
                    mPayResultHandler,
                    MSG_WHAT_PAYRESULT,
                    new PayResult(PayResult.PAY_RESULT_FAILED, "支付取消",
                            AliPayResult.TIP_PAY_ERROR)).sendToTarget();
        }
        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            Toast.makeText(this, "您需要登陆才能继续支付！", Toast.LENGTH_SHORT).show();
        } else {

            // 当非账户充值时获取账户信息
            switch (mOrder.get(ARG_PRODTYPE)) {
                case PROD_MONTHLY_PAY:
                case PROD_PARKING_FEE:
                case PROD_PAY_TIP:
                case PROD_PAY_MONEY:
                case PROD_BUY_TICKET:
                    if (mAccountInfo == null) {
                        getAccountInfo();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    // 返回：0失败，1成功，-8重复支付
    public void balancePay() {

        // 余额支付不接受消息
        onPrePay(PAYTYPE_BALANCE);

        String url = TCBApp.mServerUrl + "carowner.do";
        LogUtils.i(WXPayEntryActivity.class, "balancePay url: --->> " + url
                + "\n" + mOrder.toString());
        new AQuery(this).ajax(url, mOrder, String.class,
                new AjaxCallback<String>() {
                    @Override
                    public void callback(String url, String object,
                                         AjaxStatus status) {
                        LogUtils.i(getClass(), "balancePay result: --->> "
                                + object);
                        PayResult payResult = new PayResult(
                                PayResult.PAY_RESULT_FAILED, "网络异常", "请稍后重试。");
                        if (!TextUtils.isEmpty(object)) {
                            try {
                                payResult = new Gson().fromJson(object,
                                        PayResult.class);

                                switch (payResult.result) {
                                    case "-8":
                                        payResult.errmsg = "重复支付！";
                                        payResult.tips = "此订单已经支付过，请勿重复支付！";
                                        break;
                                    case "-12":
                                        payResult.errmsg = "支付失败！";
                                        payResult.tips = "您的账户余额不足！请先充值或选择其他支付方式。";
                                        break;
                                    case "-13":
                                        payResult.errmsg = "支付失败！";
                                        payResult.tips = "您今天使用停车券的次数已达次数限制，请选择其他支付方式！";
                                        break;
                                }
                            } catch (Exception e) {
                                if ("1".equals(object)) {
                                    payResult = new PayResult(
                                            PayResult.PAY_RESULT_SUCCESS, "",
                                            "");
                                } else {
                                    payResult = new PayResult(
                                            PayResult.PAY_RESULT_FAILED,
                                            "支付失败！", "您的账户余额并未扣除。");
                                }
                            }
                        }
                        Message.obtain(mPayResultHandler, MSG_WHAT_PAYRESULT,
                                payResult).sendToTarget();
                        super.callback(url, object, status);
                    }
                }.timeout(10 * 1000));
    }

    /**
     * 微信支付
     */
    public void wxPay() {
        // 微信支付
        // http://192.168.199.240/zld/wxpreorder.do?action=preorder&body=producttest&total_fee=100&attach="";
        iWXApi.registerApp(Keys.WXPAY_APPID);

        Map<String, String> wxOrder = new HashMap<>();
        try {
            wxOrder.put("body",
                    URLEncoder.encode(mOrder.get(ARG_SUBJECT), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        wxOrder.put("total_fee",
                mWXPayTextView.getText().toString().replace("￥", ""));
        wxOrder.put("action", "preorder");
        // 微信支付分两步：先给账户充值，再判断需不需要支付停车费或者购买包月产品
        wxOrder.put("attach", getBody());
        String url = TCBApp.mServerUrl + "wxpreorder.do";
        LogUtils.i(WXPayEntryActivity.class, "getPrepayOrder url: --->> " + url
                + "\nparams: --->> " + wxOrder.toString());

        onPrePay(PAYTYPE_WXPAY);

        new AQuery(this).ajax(url, wxOrder, String.class,
                new AjaxCallback<String>() {
                    @Override
                    public void callback(String url, String object,
                                         AjaxStatus status) {
                        LogUtils.i(this.getClass(), "wxPay result: --->>"
                                + object);
                        String errMsg = "网络超时";
                        if (!TextUtils.isEmpty(object)) {
                            try {
                                PayReq req = new Gson().fromJson(object,
                                        PayReq.class);
                                if (!TextUtils.isEmpty(req.prepayId)) {
                                    req.appId = Keys.WXPAY_APPID;
                                    req.partnerId = Keys.WXPAY_PARTNERID;
                                    req.packageValue = "Sign=WXpay";
                                    iWXApi.sendReq(req);
                                    return;
                                } else {
                                    errMsg = "获取预支付订单异常";
                                }
                            } catch (Exception e) {
                                errMsg = "服务器错误";
                                e.printStackTrace();
                            }
                        }
                        Message.obtain(
                                mPayResultHandler,
                                MSG_WHAT_PAYRESULT,
                                new PayResult(PayResult.PAY_RESULT_FAILED,
                                        errMsg, AliPayResult.TIP_PAY_ERROR))
                                .sendToTarget();
                        super.callback(url, object, status);
                    }
                });
    }

    public void aliPay() {

        onPrePay(PAYTYPE_ALIPAY);

        String info = getOrderInfo();

        String sign = Rsa.sign(info, mKey.getPrivate());

        try {
            sign = URLEncoder.encode(sign, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        info += "&sign=\"" + sign + "\"&" + getSignType();

        LogUtils.i(getClass(), "aliPay params: --->> " + info);

        final String aliPayOrder = info;
        final PayTask aliPay = new PayTask(this);
        new Thread(new Runnable() {

            @Override
            public void run() {

                AliPayResult aliPayResult = new AliPayResult(
                        aliPay.pay(aliPayOrder));
                try {
                    LogUtils.i(this.getClass(), "AliPay Result: --->> "
                            + aliPayResult.getResult());
                    aliPayResult.parseResult();
                    switch (aliPayResult.resultStatus) {
                        case AliPayResult.ERR_SUCCESS:// 支付宝支付成功，不错处理，等待服务器通知
                            if (aliPayResult.isSignOk) {// 判断签名是否正确
                                LogUtils.i(this.getClass(),
                                        "AliPay Result: --->> Success!");
                                mStartedThreePartPay = true;
                            }
                            return;
                    }
                    Message.obtain(
                            mPayResultHandler,
                            MSG_WHAT_PAYRESULT,
                            new PayResult(PayResult.PAY_RESULT_FAILED,
                                    aliPayResult.resultStatus,
                                    aliPayResult.tips)).sendToTarget();
                } catch (Exception e) {
                    Message.obtain(
                            mPayResultHandler,
                            MSG_WHAT_PAYRESULT,
                            new PayResult(PayResult.PAY_RESULT_FAILED,
                                    aliPayResult.resultStatus,
                                    aliPayResult.tips)).sendToTarget();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 支付前的预处理：显示"支付中..."Dialog，开启线程从服务器获取支付结果
     *
     * @param payType
     */
    private void onPrePay(int payType) {
        if (mPayDialog != null && mPayDialog.isShowing()) {
            return;
        }

        this.mPayType = payType;

        String paying = "支付中...";
        switch (payType) {
            case PAYTYPE_BALANCE:
                paying = "余额支付中...";
                // 是否需要获取红包
                mOrder.put("version", "2");
                switch (mOrder.get(ARG_PRODTYPE)) {
                    case PROD_PAY_TIP:
                        mOrder.put("action", "puserreward");
                        break;
                    case PROD_PAY_MONEY:
                        mOrder.put("action", "epay");
                        break;
                    case PROD_PARKING_FEE:
                        mOrder.put("action", "payorder");
                        break;
                    case PROD_BUY_TICKET:
                        mOrder.put("action", "buyticket");
                        break;
                    default:
                        break;
                }
                break;
            case PAYTYPE_ALIPAY:
                paying = "支付宝支付中...";
                break;
            case PAYTYPE_WXPAY:
                paying = "微信支付中...";
                mWXPayResultGetted = false;
                break;
        }
        mPayDialog = ProgressDialog.show(this, "", paying, false, true);
        mPayDialog.setCanceledOnTouchOutside(false);
    }

    // 支付宝：获取签名方法
    private String getSignType() {
        return "sign_type=\"RSA\"";
    }

    // 支付宝：生成交易的唯一订单号
    private String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
                Locale.CHINA);
        Date date = new Date();
        String key = format.format(date);

        java.util.Random r = new java.util.Random();
        key += r.nextInt();
        key = key.substring(0, 15);
        return key;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setMessage("确认取消？")
                .setPositiveButton("取消", null)
                .setNegativeButton("确定", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (mPayType == PAYTYPE_WXPAY && iWXApi != null) {
            iWXApi.handleIntent(intent, this);
        }
    }

    @Override
    public void onReq(BaseReq req) {
    }

    /**
     * 处理微信支付结果
     */
    @Override
    public void onResp(BaseResp resp) {
        mWXPayResultGetted = true;
        LogUtils.i(WXPayEntryActivity.class, "WXPay Result: --->> "
                + resp.errStr);
        String errMsg = "与微信通信异常！";
        String tips = AliPayResult.TIP_PAY_ERROR;
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:// 微信支付成功，不做处理，等待停车宝服务器通知处理结果
                LogUtils.i(WXPayEntryActivity.class, "WXPay Result: --->> Success!");
                mStartedThreePartPay = true;
                return;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                LogUtils.i(WXPayEntryActivity.class,
                        "WXPay Result: --->> 用户取消支付！！！");
                errMsg = "支付取消！";
                break;
        }
        Message.obtain(mPayResultHandler, MSG_WHAT_PAYRESULT,
                new PayResult(PayResult.PAY_RESULT_FAILED, errMsg, tips))
                .sendToTarget();
    }

    @Override
    protected void onDestroy() {

        // 取消支付通知
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

        // 停止轮询获取支付消息
//        TimerUtils.stopPollingService(this);

        // 取消网络请求
        TCBApp.getAppContext().cancelPendingRequests(this);

        if (mPollingProtocol != null) {
            mPollingProtocol.stopPolling();
        }
        mPayResultHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_pay_coupon:
                onCouponViewClicked();
                break;
            case R.id.rl_pay_balance:
                onBalanceViewClicked();
                break;
            case R.id.rl_pay_alipay:
                onAliPayViewClicked();
                break;
            case R.id.rl_pay_wxpay:
                onWXPayViewClicked();
                break;
            case R.id.btn_pay_pay:
                onPayButtonClicked();
                break;
        }
    }

    private void onPayButtonClicked() {

        // 判断是否登陆
        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            doLogin();
            return;
        }

        if (!PROD_RECHARGE.equals(mOrder.get(ARG_PRODTYPE))
                && mAccountInfo == null) {
            Toast.makeText(this, "账户异常，请稍后重试~", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (mPayType) {
            case PAYTYPE_BALANCE:
                balancePay();
                break;
            case PAYTYPE_ALIPAY:
                aliPay();
                break;
            case PAYTYPE_WXPAY:
                wxPay();
                break;
        }

        // 记住默认的支付方式
        TCBApp.getAppContext().saveInt(R.string.sp_pay_type, mPayType);
    }

    private void onWXPayViewClicked() {
        refreshPayView(PAYTYPE_WXPAY);
    }

    private void onAliPayViewClicked() {
        refreshPayView(PAYTYPE_ALIPAY);
    }

    private void onBalanceViewClicked() {
        // Do Nothing
    }

    /**
     * 选择停车券
     */
    private void onCouponViewClicked() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.ARG_FRAGMENT, MainActivity.FRAGMENT_TICKET);
        Bundle args = new Bundle();
        args.putString(ChooseTicketFragment.ARG_TOTAL, mOrder.get(ARG_TOTALFEE));
        args.putString(ChooseTicketFragment.ARG_ORDERID,
                mOrder.get(ARG_ORDER_ID));
        args.putString(ChooseTicketFragment.ARG_UID, mOrder.get(ARG_ORDER_UID));
        args.putString(ChooseTicketFragment.ARG_CHOOSED_ID,
                mOrder.get("ticketid"));
        args.putString(ChooseTicketFragment.ARG_PROD_TYPE, mOrder.get(ARG_PRODTYPE));
        intent.putExtra(MainActivity.ARG_FRAGMENT_ARGS, args);
        startActivityForResult(intent, REQUEST_CODE_CHOOSE_TICKET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.i(getClass(), "onActivityResult: --->> requestCode:"
                + requestCode + ",resultCode:" + resultCode);
        if (requestCode == REQUEST_CODE_CHOOSE_TICKET
                && resultCode == RESULT_OK) {
            Coupon coupon = data.getParcelableExtra("coupon");
            refreshCouponView(coupon);
        }
    }

    /**
     * 取消payDialog的显示
     */
    public void dismissDialog() {
        if (mPayDialog != null && mPayDialog.isShowing()) {
            mPayDialog.dismiss();
        }
    }

    // 支付宝&微信： 拼装支付宝请求参数中的"body"参数&微信支付中的"attach"参数
    private String getBody() {
        String extra = "";
        switch (mOrder.get(ARG_PRODTYPE)) {
            case PROD_RECHARGE:// 账户充值
                break;
            case PROD_MONTHLY_PAY:// 购买包月产品
                extra = "_" + mOrder.get(ARG_MONTYLYPAY_ID) + "_"
                        + mOrder.get(ARG_MONTYLYPAY_NUMBER) + "_"
                        + mOrder.get(ARG_MONTYLYPAY_START);
                break;
            case PROD_PARKING_FEE:// 停车费结算
                extra = "_" + mOrder.get(ARG_ORDER_ID) + "_"
                        + mOrder.get("ticketid");
                break;
            case PROD_PAY_MONEY:// 直接支付停车费
                extra = "_" + mOrder.get(ARG_ORDER_UID) + "_"
                        + mOrder.get(ARG_TOTALFEE) + "_" + mOrder.get("ticketid");
                break;
            case PROD_PAY_TIP:// 付小费
                extra = "_" + mOrder.get(ARG_ORDER_UID) + "_"
                        + mOrder.get(ARG_TOTALFEE) + "_"
                        + mOrder.get(ARG_ORDER_ID) + "_" + mOrder.get("ticketid");
                break;
            case PROD_BUY_TICKET:
                extra = "_" + mOrder.get(ARG_TICKET_VALUE) + "_" + mOrder.get(ARG_TICKET_NUMBER);
                break;
        }
        return mOrder.get("mobile") + "_" + mOrder.get(ARG_PRODTYPE) + extra;
    }

    // 拼装支付宝请求的请求参数
    private String getOrderInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("partner=\"");
        sb.append(mKey.getPartner());
        sb.append("\"&out_trade_no=\"");
        sb.append(getOutTradeNo());
        sb.append("\"&subject=\"");
        sb.append(mOrder.get(ARG_SUBJECT));
        sb.append("\"&body=\"");
        // 支付宝支付分两步：1账户充值，2余额支付
        sb.append(getBody());
        sb.append("\"&total_fee=\"");
        sb.append(mAliPayTextView.getText().toString().replace("￥", ""));
        sb.append("\"&service=\"mobile.securitypay.pay");
        sb.append("\"&_input_charset=\"UTF-8");

        // TODO 新版支付宝貌似不再需要此参数
        sb.append("\"&return_url=\"");
        try {
            sb.append(URLEncoder.encode("http://m.alipay.com", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // Do Nothing
            e.printStackTrace();
        }
        sb.append("\"&payment_type=\"1");
        sb.append("\"&seller_id=\"");
        sb.append(mKey.getSeller());
        sb.append("\"&notify_url=\"");
        sb.append(mKey.getAliPayNotifyUrl());
        sb.append("\"");
        return new String(sb);
    }

    public boolean isWXPayEntryActivityResumed() {
        return mResumed;
    }

    public void setWXPayEntryActivityResumed(boolean mResumed) {
        this.mResumed = mResumed;
    }

    public static class PayResultHandler extends Handler {

        // private PayResultFragment payResultDialog;
        private WXPayEntryActivity mActivity;
        private PayResultFragment payResultDialog;

        public PayResultHandler(WXPayEntryActivity activity) {
            this.mActivity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_WHAT_SHOW_DIALOG:
                    if (payResultDialog != null) {
                        if (mActivity.isWXPayEntryActivityResumed()
                                && !payResultDialog.isAlreadyShown()) {
                            payResultDialog.show(
                                    mActivity.getSupportFragmentManager(),
                                    "PayResult");
                        } else {
                            LogUtils.i(PayResultHandler.class,
                                    "--->> activity isn't resumed or payResultDialog already shown");
                        }
                    } else {
                        LogUtils.i(PayResultHandler.class,
                                "--->> payResultDialog is null");
                    }
                    break;

                default:
                    PayResult payResult = new PayResult();
                    try {
                        if (msg.obj instanceof PayResult) {
                            payResult = (PayResult) msg.obj;
                        } else if (msg.obj instanceof Order) {
                            Order order = (Order) msg.obj;
                            if (Order.STATE_PAYED.equals(order.getState())) {
                                // 支付成功
                                payResult.errmsg = order.getOrderid();
                                payResult.tips = order.getBonusid();
                                payResult.result = PayResult.PAY_RESULT_SUCCESS;
                            } else if (Order.STATE_PAY_FAILED.equals(order
                                    .getState())) {
                                // 支付失败
                                payResult.result = PayResult.PAY_RESULT_FAILED;
                            }
                        } else {
                            throw new IllegalArgumentException("支付结果数据错误");
                        }
                        showPayResultDialog(payResult,
                                mActivity.mOrder.get(ARG_PRODTYPE));
                    } catch (Exception e) {
                        payResult.result = PayResult.PAY_RESULT_FAILED;
                        payResult.errmsg = "数据解析错误~";
                        payResult.tips = "";
                        showPayResultDialog(payResult,
                                mActivity.mOrder.get(ARG_PRODTYPE));
                        e.printStackTrace();
                    }
                    break;
            }
        }

        /**
         * 弹出支付结果对话框
         *
         * @param payResult 支付结果
         * @param prodType  购买的产品类型，可取值：PROD_TYPE_MONTHLYPAY，PROD_TYPE_PARKINGFEE，
         *                  PROD_TYPE_RECHARGE，PROD_TYPE_PAYMONEY
         */
        public void showPayResultDialog(final PayResult payResult,
                                        final String prodType) {

            // 隐藏payDialog
            mActivity.dismissDialog();

            // 将WXPayEntryActivity中微信支付结果标志位置为true
            mActivity.mWXPayResultGetted = true;
            mActivity.mStartedThreePartPay = false;

            payResultDialog = PayResultFragment.newInstance(payResult, prodType);
            if (mActivity.isWXPayEntryActivityResumed()
                    && !payResultDialog.isAlreadyShown()) {
                payResultDialog.show(mActivity.getSupportFragmentManager(),
                        "PayResult");
            }
        }

        private void showShareDialog(String bonusid) {
            Intent intent = new Intent(TCBApp.getAppContext(),
                    ShareActivity.class);
            intent.putExtra("bonusid", bonusid);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            mActivity.startActivity(intent);
        }
    }
}
