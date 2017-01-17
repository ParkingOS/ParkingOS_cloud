package com.tq.zld.view.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.util.Common;
import com.tq.zld.util.KeyboardUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.LoginActivity;
import com.tq.zld.view.map.WebActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterFragment extends BaseFragment implements OnClickListener {

    public static final String ARG_MOBILE = "mobile";

    private Button mRegisterButton;
    private EditText mPlateEditView;

    @Override
    protected String getTitle() {
        return "输入车牌号";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRegisterButton = (Button) view.findViewById(R.id.btn_register);
        mRegisterButton.setOnClickListener(this);
        mPlateEditView = (EditText) view.findViewById(R.id.et_register_plate);
        mPlateEditView.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(7)});
        mPlateEditView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 7) {
                    KeyboardUtils.closeKeybord(mPlateEditView, getActivity());
                }
            }
        });
        view.findViewById(R.id.tv_register_terms).setOnClickListener(this);
        // wv_terms_and_conditions = (WebView)
        // findViewById(R.id.wv_terms_and_conditions);
        CheckBox termsCheckBox = (CheckBox) view.findViewById(R.id.cb_register_terms);
        termsCheckBox
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (isChecked) {
                            mRegisterButton
                                    .setBackgroundResource(R.drawable.shape_solid_button_light_green);
                            mRegisterButton.setClickable(true);
                            mRegisterButton.invalidate();
                            LogUtils.i(RegisterFragment.class, "isChecked --->> " + isChecked);
                        } else {
                            mRegisterButton
                                    .setBackgroundResource(R.drawable.shape_solid_button_gray);
                            mRegisterButton.setClickable(false);
                            mRegisterButton.invalidate();
                        }
                    }
                });
        termsCheckBox.setChecked(true);
        KeyboardUtils.openKeybord(mPlateEditView, getActivity());

        view.findViewById(R.id.tv_register_failed).setOnClickListener(this);

        view.findViewById(R.id.tv_register_tips).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                String plate = mPlateEditView.getText().toString()
                        .replaceAll(" ", "").toUpperCase(Locale.SIMPLIFIED_CHINESE);
                if (Common.checkPlate(plate)) {
                    sendPlate(plate);
                } else {
                    Toast.makeText(getActivity(), "请输入正确的车牌号!", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                break;
            case R.id.tv_register_terms:// 用户点击了"使用条款和隐私声明"
                showTermsAndConditionsDialog();
                break;
            case R.id.tv_register_failed:
                onFailedViewClicked();
                break;
            case R.id.tv_register_tips:
                onTipsViewClicked();
                break;
            default:
                break;
        }
    }

    private void onFailedViewClicked() {
        // 进入公众号说明页
        Intent intent = new Intent(getActivity(), WebActivity.class);
        intent.putExtra(WebActivity.ARG_TITLE, "车牌号已注册？");
        intent.putExtra(WebActivity.ARG_URL,
                "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209112920&idx=1&sn=2d5faa08b2075e6d8471dc6ce0955caf#rd");
        startActivity(intent);
    }

    private void onTipsViewClicked() {
        new AlertDialog.Builder(getActivity())
                .setTitle("请勿填写他人车牌号")
                .setMessage("当你填写的车牌的真实车主上传行驶证通过认证时，你的所有车牌号将被清空，你的停车宝账户将被拉入用券黑名单一个月。")
                .setPositiveButton("关闭", null)
                .create().show();
    }

    private void sendPlate(String plate) {
        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "",
                "请稍候...", false, true);
        dialog.setCanceledOnTouchOutside(false);

        final Map<String, String> params = new HashMap<String, String>();
        params.put("action", "addcar");
        params.put("carnumber", plate);
        params.put("mobile", getArguments().getString(ARG_MOBILE));
        URLUtils.decode(params);
        URLUtils.putPublicParam(params);
        String url = TCBApp.mServerUrl + "carlogin.do";
        StringRequest request = new StringRequest(Method.POST, url,
                new Listener<String>() {

                    @Override
                    public void onResponse(String result) {

                        dialog.dismiss();
                        LogUtils.i(RegisterFragment.class,
                                "sendPlate result: --->> " + result);
                        if (TextUtils.isEmpty(result))
                            result = "-100";
                        if (LoginActivity.mHandler != null) {
                            Message.obtain(LoginActivity.mHandler,
                                    Integer.parseInt(result),
                                    params.get("mobile")).sendToTarget();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError arg0) {
                dialog.dismiss();
                Toast.makeText(getActivity(), "网络错误~",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    private void showTermsAndConditionsDialog() {
        // 设置webview参数
        WebView view = new WebView(getActivity());
        WebSettings settings = view.getSettings();
        // settings.setUseWideViewPort(true);
        // settings.setLoadWithOverviewMode(true);
        settings.setDisplayZoomControls(false);
        settings.setBuiltInZoomControls(false);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        view.loadUrl("file:///android_asset/terms_and_conditions.html");
        new AlertDialog.Builder(getActivity(),
                android.app.AlertDialog.THEME_HOLO_DARK).setView(view)
                .setTitle("使用条款和隐私政策").setPositiveButton("确定", null).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TCBApp.getAppContext().cancelPendingRequests(this);
    }
}
