package com.tq.zld.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.VerCode;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.protocal.PollingProtocol;
import com.tq.zld.protocal.SimpleVolleyErrorListener;
import com.tq.zld.util.AndroidUtils;
import com.tq.zld.util.Common;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.LoginActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LoginFragment extends BaseFragment implements OnClickListener {

    private static final int ERRCODE_OK = 10;
    private static final int ERRCODE_WRONG_MOBILE = 11;
    private static final int ERRCODE_WRONG_VERCODE = 12;

    private int errCode = ERRCODE_WRONG_MOBILE;

    private Button mVerCodeButton;// 获取验证码按钮
    private EditText mMobileView;// 手机号输入框
    private EditText mVerCodeView; // 验证码输入框
    private Button mLoginButton; // "一键登陆"按钮

    private ProgressDialog mDialog;

    private String mMobile;// 填写的手机号
    private BroadcastReceiver mSMSReceiver;
    private TimeCount mTimeCount;

    private PollingProtocol mPollingService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTimeCount = new TimeCount(30000, 1000);
        mDialog = new ProgressDialog(getActivity());
        mDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 注册短信广播接收者，监听验证码短信
        if (mSMSReceiver == null) {
            mSMSReceiver = new SmsReceiver();
        }
        IntentFilter filter = new IntentFilter(
                "android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(IntentFilter.SYSTEM_LOW_PRIORITY);
        getActivity().registerReceiver(mSMSReceiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

        // 显示输入框
        new Timer().schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mMobileView
                        .getContext().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mMobileView, 0);
            }

        }, 200);

        // 判断是否支持微信登录
//        if (mActivity.isSupportWXLogin()) {
//            View wxLoginView = view.findViewById(R.id.tv_login_wx);
//            wxLoginView.setVisibility(View.VISIBLE);
//            wxLoginView.setOnClickListener(this);
//        }
    }

    private void initView(View root) {
        mVerCodeButton = (Button) root.findViewById(R.id.btn_login_vercode);
        mVerCodeButton.setOnClickListener(this);
        // getCodeBtn.setBackgroundResource(R.color.bg_gray);

        mLoginButton = (Button) root.findViewById(R.id.btn_login_login);
        mLoginButton.setOnClickListener(this);
        // sendCodeBtn.setBackgroundResource(R.color.bg_gray);
        mVerCodeView = (EditText) root.findViewById(R.id.et_login_vercode);
        mVerCodeView.addTextChangedListener(new TextWatcher() {

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
                // 判断是否可以发送验证码
                checkIfCanSendCode();
                if (s.length() == 4) {
                    onClick(mLoginButton);
                    hideSoftKeyboard(mVerCodeView);
                }
            }
        });

        mMobileView = (EditText) root.findViewById(R.id.et_login_mobile);
        mMobileView.setCursorVisible(true);
        mMobileView.addTextChangedListener(new TextWatcher() {

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
                checkIfCanSendCode();
                if (s.length() == 11) {
//                    hideSoftKeyboard(mMobileView);
                }
                // TODO 添加“-”分隔符
                // if (!mKeyDel) {
                // switch (s.length()) {
                // case 3:
                // mMobileView.setText(s.append(" - "));
                // mMobileView.setSelection(s.length());
                // break;
                // case 10:
                // mMobileView.setText(s.append(" - "));
                // mMobileView.setSelection(s.length());
                // break;
                // case 17:
                // hideSoftKeyboard(mMobileView);
                // break;
                // }
                // }
            }
        });
        // mMobileView.setOnKeyListener(new OnKeyListener() {
        //
        // @Override
        // public boolean onKey(View v, int keyCode, KeyEvent event) {
        // switch (keyCode) {
        // case KeyEvent.KEYCODE_DEL:
        // LoginFragment.this.mKeyDel = true;
        // break;
        //
        // default:
        // LoginFragment.this.mKeyDel = false;
        // break;
        // }
        // return false;
        // }
        // });
        mMobile = TCBApp.getAppContext().readString(R.string.sp_login_mobile, "");
        mMobileView.setText(mMobile);
        mMobileView.setSelection(mMobile.length());

        root.findViewById(R.id.tv_login_sms).setOnClickListener(this);
    }

    // 判断“一键登录”按钮是否可用
    private void checkIfCanSendCode() {
        if (!Common.checkMobile(mMobileView.getText().toString())) {
            errCode = ERRCODE_WRONG_MOBILE;
            return;
        }
        if (!mVerCodeView.getText().toString().matches("^\\d{4}$")) {
            errCode = ERRCODE_WRONG_VERCODE;
            return;
        }
        errCode = ERRCODE_OK;
    }

    private void hideSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_login_vercode:
                if (errCode == ERRCODE_WRONG_MOBILE) {
                    Toast.makeText(getActivity(), "请输入正确手机号！", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                mMobile = mMobileView.getText().toString().trim();
                getVerCode(mMobile);
                break;
            case R.id.btn_login_login:
                if (errCode == ERRCODE_WRONG_VERCODE) {
                    Toast.makeText(getActivity(), "请输入正确验证码！", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                String code = mVerCodeView.getText().toString().trim();
                checkVerCode(mMobile, code);
                break;
            case R.id.tv_login_wx:
                onWXLoginViewClicked();
                break;
            case R.id.tv_login_sms:
                onSMSLoginViewClicked();
                break;
            default:
                break;
        }
    }

    private void onSMSLoginViewClicked() {
        if (errCode == ERRCODE_WRONG_MOBILE) {
            Toast.makeText(getActivity(), "请输入正确手机号！", Toast.LENGTH_SHORT).show();
            return;
        }
        mMobile = mMobileView.getText().toString().trim();
        getVerCode2(mMobile);
    }

    /**
     * 主动发送短信验证登录
     * carlogin.do?action=dologin&mobile=18201517240&imei=
     */
    private void getVerCode2(final String mobileNum) {
        mDialog.show();
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "dologin");
        params.put("mobile", mobileNum);
        params.put("imei", AndroidUtils.getIMEI());
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "carlogin.do", params);
        GsonRequest<VerCode> request = new GsonRequest<VerCode>(url, VerCode.class, new Response.Listener<VerCode>() {
            @Override
            public void onResponse(VerCode verCode) {
                mDialog.dismiss();

                // 存储获取验证码的手机号，便于下次回显
                TCBApp.getAppContext().saveString(R.string.sp_login_mobile, mobileNum);

                if ("0".equals(verCode.mesg)) {
                    showSendSMSDialog(verCode);
                } else {
                    Toast.makeText(TCBApp.getAppContext(), "获取验证码失败，请重试！", Toast.LENGTH_SHORT).show();
                }
            }
        }, new SimpleVolleyErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                mDialog.dismiss();
            }
        });
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    private void showSendSMSDialog(final VerCode verCode) {
        String text = "我们需要发送一条短信以完成验证，是否同意？\n(注：短信费用由运营商收取，一般为0.1元/条)";
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).setMessage(text)
                .setNegativeButton("取消", null)
                .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendSMS(verCode);
                    }
                }).create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
    }

    private void sendSMS(VerCode verCode) {
        if (LoginActivity.mHandler != null) {
            ((LoginActivity) getActivity()).showProgressDialog("验证中...", true, false);
            Uri smsToUri = Uri.parse(String.format("smsto:%s", verCode.tomobile));
            Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
            intent.putExtra("sms_body", String.format("%s【停车宝】", verCode.code));
            startActivity(intent);
            // 直接发送短信无需跳转到短信应用
            // TODO 解决双卡问题
//            sendSMSDirectly(verCode);

            // 轮询取登录结果
            HashMap<String, String> params = new HashMap<>();
            params.put("action", "checkcode");
            params.put("mobile", mMobile);
            mPollingService = new PollingProtocol(LoginActivity.mHandler, params);
            mPollingService.startPolling();
        }
    }

    private void sendSMSDirectly(VerCode verCode) {
        String SENT_SMS_ACTION = "SENT_SMS_ACTION";
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        PendingIntent sentPI = PendingIntent.getBroadcast(getActivity(), 0, sentIntent, 0);
// register the Broadcast Receivers
        getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent _intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getActivity(),
                                "短信发送成功", Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        break;
                }
            }
        }, new IntentFilter(SENT_SMS_ACTION));
        String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
// create the deilverIntent parameter
        Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
        PendingIntent deliverPI = PendingIntent.getBroadcast(getActivity(), 0,
                deliverIntent, 0);
        getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent _intent) {
                Toast.makeText(getActivity(),
                        "收信人已经成功接收", Toast.LENGTH_SHORT)
                        .show();
            }
        }, new IntentFilter(DELIVERED_SMS_ACTION));
        SmsManager manager = SmsManager.getDefault();
//        List<String> divideContents = manager.divideMessage(String.format("%s【停车宝】", verCode.code));
//        for (String text : divideContents) {
//            manager.sendTextMessage(verCode.tomobile, null, text, sentPI, deliverPI);
//        }
        manager.sendTextMessage(verCode.tomobile, null, String.format("%s【停车宝】", verCode.code), sentPI, deliverPI);
    }

    private void onWXLoginViewClicked() {
        // send oauth request
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
//        req.scope = "snsapi_base";
        req.state = "tingchebao_wx_login_test";
    }

    /**
     * "点我一键登录"按钮点击事件：检查验证码是否正确
     */
    private void checkVerCode(final String mobileNum, String code) {
        mDialog.show();
        Map<String, String> params = new HashMap<String, String>();
        String url = TCBApp.mServerUrl + "carlogin.do";
        params.put("action", "validcode");
        params.put("mobile", mobileNum);
        params.put("imei", AndroidUtils.getIMEI());
        params.put("code", code);
        LogUtils.i(LoginActivity.class, "checkVerCode url: --->> " + url
                + "\n" + params.toString());
        new AQuery(getActivity()).ajax(url, params, String.class,
                new AjaxCallback<String>() {
                    @Override
                    public void callback(String url, String object,
                                         AjaxStatus status) {
                        super.callback(url, object, status);
                        mDialog.dismiss();
                        LogUtils.i(LoginFragment.class,
                                "checkVerCode result: --->> " + object);
                        if (TextUtils.isEmpty(object)) {
                            result = "-100";
                        }
                        if (LoginActivity.mHandler != null) {
                            Message.obtain(LoginActivity.mHandler,
                                    Integer.parseInt(result), mobileNum)
                                    .sendToTarget();
                        }
                    }
                });
    }

    /**
     * 从服务器获取验证码
     */
    private void getVerCode(final String mobileNum) {
        mDialog.show();
        Map<String, String> params = new HashMap<String, String>();
        String url = TCBApp.mServerUrl + "carlogin.do";
        params.put("action", "login");
        params.put("mobile", mobileNum);
        LogUtils.i(getClass(),
                "获取验证码 url: --->> " + url + "\n" + params.toString());
        new AQuery(getActivity()).ajax(url, params, String.class,
                new AjaxCallback<String>() {
                    @Override
                    public void callback(String url, String object,
                                         AjaxStatus status) {
                        mDialog.dismiss();

                        // 存储获取验证码的手机号，便于下次回显
                        TCBApp.getAppContext().saveString(R.string.sp_login_mobile, mobileNum);

                        LogUtils.i(LoginFragment.class, "获取验证码 result: --->> "
                                + object);

                        if (!TextUtils.isEmpty(object) && "0".equals(object)) {
                            mTimeCount.start();
                            mVerCodeButton
                                    .setBackgroundResource(R.drawable.shape_solid_button_gray);
                            mVerCodeView.requestFocus();
                            Toast.makeText(getActivity(),
                                    "验证码已发送至：" + mobileNum, Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast.makeText(getActivity(), "获取验证码失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        super.callback(url, object, status);
                    }
                });
    }

    @Override
    public void onPause() {

        if (mSMSReceiver != null) {
            getActivity().unregisterReceiver(mSMSReceiver);
        }

        super.onPause();
    }

    /**
     * 倒计时器，用于控制"获取验证码"按钮点击后一分钟内不可再次点击
     *
     * @author Clare
     */
    private class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            mVerCodeButton.setText("重新发送");
            mVerCodeButton.setClickable(true);
            mVerCodeButton.setBackgroundResource(R.drawable.shape_solid_button_light_green);
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            mVerCodeButton.setClickable(false);
            mVerCodeButton.setText("重新发送" + "(" + millisUntilFinished / 1000
                    + "秒)");
            mVerCodeButton.invalidate();
        }
    }

    /**
     * 短信的广播接收者，用于自动填写接收到的注册（或登陆）验证码
     *
     * @author Clare
     */
    private class SmsReceiver extends BroadcastReceiver // implements android.
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getExtras() == null) {
                return;
            }
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            for (Object p : pdus) {
                byte[] pdu = (byte[]) p;
                SmsMessage message = SmsMessage.createFromPdu(pdu);
                String smsBody = message.getMessageBody();
                String smsNumber = message.getDisplayOriginatingAddress();
                LogUtils.i(getClass(), smsNumber + ":" + smsBody);
                if (!TextUtils.isEmpty(smsBody) && smsBody.contains("停车宝")
                        && smsBody.contains("验证码")) {
                    int startIndex = smsBody.indexOf(":");
                    String code = smsBody.substring(startIndex + 1,
                            startIndex + 5);
                    mVerCodeView.setText(code);
                    mVerCodeView.setSelection(code.length());
                }
            }
        }
    }

    @Override
    protected String getTitle() {
        return "登录";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TCBApp.getAppContext().cancelPendingRequests(this);
        if (mPollingService != null) {
            mPollingService.stopPolling();
        }
    }
}
