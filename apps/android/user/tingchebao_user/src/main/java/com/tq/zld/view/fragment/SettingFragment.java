package com.tq.zld.view.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.SettingInfo;
import com.tq.zld.protocal.SimpleVolleyErrorListener;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingFragment extends NetworkFragment<SettingInfo> {

    private TextView tvAutoPay;// "自动支付"设置条目(金额)
    private TextView tvLowBalance;// "余额充值"设置条目(金额)

    private String[] autoPayItems;
    private String[] lowBalanceItems;

    private int checkedAutoPayItemId;
    private int checkedLowBalanceItemId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        autoPayItems = new String[]{"5元", "10元", "25元", "50元", "总是自动支付",
                "不自动支付"};
        lowBalanceItems = new String[]{"10元", "25元", "50元", "100元", "不提醒"};
        checkedAutoPayItemId = autoPayItems.length - 1;
        checkedLowBalanceItemId = lowBalanceItems.length - 1;
        pattern = Pattern.compile("\\d+");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    protected TypeToken<SettingInfo> getBeanListType() {
        return null;
    }

    @Override
    protected Class<SettingInfo> getBeanClass() {
        return SettingInfo.class;
    }

    @Override
    protected String getUrl() {
        return TCBApp.mServerUrl + "carowner.do";
    }

    @Override
    protected Map<String, String> getParams() {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "getprof");
        params.put("mobile", TCBApp.mMobile);
        return params;
    }

    @Override
    protected void initView(View view) {

        ((TextView) view.findViewById(R.id.tv_autopay_tips)).setText(Html
                .fromHtml("<font color='#D25343'>(仅速通卡用户或照牌车场有效)</font>"));
        view.findViewById(R.id.tv_autopay_tips)
                .setOnClickListener(this);
        view.findViewById(R.id.tv_about).setOnClickListener(this);
        view.findViewById(R.id.rl_autopay).setOnClickListener(this);
        view.findViewById(R.id.ll_lowbalance).setOnClickListener(this);
        tvAutoPay = (TextView) view.findViewById(R.id.tv_autopay);
        tvLowBalance = (TextView) view.findViewById(R.id.tv_lowbalance);
    }

    private void setView(SettingInfo data) {

        if (data == null) {
            return;
        }

        // 设置自动支付限额
        if ("0".equals(data.limit_money)) {
            checkedAutoPayItemId = autoPayItems.length - 1;
        } else if ("-1".equals(data.limit_money)) {
            checkedAutoPayItemId = autoPayItems.length - 2;
        } else {
            for (int i = 0; i < autoPayItems.length; i++) {
                if (autoPayItems[i].contains(data.limit_money)) {
                    checkedAutoPayItemId = i;
                    break;
                }
            }
        }
        String autoPayText = autoPayItems[checkedAutoPayItemId];
        if (pattern.matcher(autoPayText).find()) {
            autoPayText = String.format("不大于%s时", autoPayText);
        }
        tvAutoPay.setText(autoPayText);

        // 设置余额充值提醒
        if ("0".equals(data.low_recharge)) {
            checkedLowBalanceItemId = lowBalanceItems.length - 1;
        } else {
            for (int i = 0; i < lowBalanceItems.length; i++) {
                if (lowBalanceItems[i].contains(data.low_recharge)) {
                    checkedLowBalanceItemId = i;
                    break;
                }
            }
        }
        String lowBalanceText = lowBalanceItems[checkedLowBalanceItemId];
        if (pattern.matcher(lowBalanceText).find()) {
            lowBalanceText = String.format("小于%s时", lowBalanceText);
        }
        tvLowBalance.setText(lowBalanceText);

        // 提交成功，保存余额充值提醒
        TCBApp.getAppContext()
                .getSharedPreferences(TCBApp.mMobile, Context.MODE_PRIVATE)
                .edit()
                .putString(
                        getString(R.string.sp_setting_low_recharge),
                        TextUtils.isEmpty(data.low_recharge) ? "0"
                                : data.low_recharge).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_autopay_tips:
                showAutoPayTipsDialog();
                break;
            case R.id.tv_about:
                onAboutViewClicked();
                break;
            case R.id.ll_lowbalance:
                showLowBalanceDialog();
                break;
            case R.id.rl_autopay:
                showAutoPayDialog();
                break;
        }
    }

    private void onAboutViewClicked() {
        replace(R.id.fragment_container, new AboutFragment(), true);
    }

    private void showAutoPayTipsDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("停车费自动支付说明")
                .setMessage(
                        "为保证用户资金安全，停车宝仅支持两类用户自动支付:\n1、办理了停车宝速通卡的用户。\n2、在支持停车宝手机支付的照牌车场的用户。")
                .setPositiveButton("知道了", null).show();
    }

    private void showAutoPayDialog() {
        AlertDialog autoPayDialog = new AlertDialog.Builder(getActivity())
                .setTitle("停车费自动支付")
                .setSingleChoiceItems(autoPayItems, checkedAutoPayItemId,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                                checkedAutoPayItemId = which;
                                String checkedItem = autoPayItems[checkedAutoPayItemId];
                                if (pattern.matcher(checkedItem).find()) {
                                    checkedItem = String.format("不大于%s时", checkedItem);
                                }
                                tvAutoPay.setText(checkedItem);

                                HashMap<String, String> params = new HashMap<>();
                                params.put("money", checkedItem);
                                // 友盟事件统计：设置停车费自动支付（id：5）
                                MobclickAgent.onEvent(getActivity(), "5", params);
                                postSetting();
                            }
                        }).show();
        autoPayDialog.setCancelable(true);
        autoPayDialog.setCanceledOnTouchOutside(false);
    }

    private void showLowBalanceDialog() {
        AlertDialog lowBalanceDialog = new AlertDialog.Builder(getActivity())
                .setTitle("余额充值提醒")
                .setSingleChoiceItems(lowBalanceItems, checkedLowBalanceItemId,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                checkedLowBalanceItemId = which;
                                String checkedItem = lowBalanceItems[checkedLowBalanceItemId];
                                if (pattern.matcher(checkedItem).find()) {
                                    checkedItem = String.format("小于%s时", checkedItem);
                                }
                                tvLowBalance.setText(checkedItem);
                                dialog.dismiss();
                                postSetting();
                            }
                        }).show();
        lowBalanceDialog.setCancelable(true);
        lowBalanceDialog.setCanceledOnTouchOutside(false);
    }

    private Pattern pattern;

    // 提交设置数据
    private void postSetting() {
        final Map<String, String> params = new HashMap<>();
        params.put("mobile", TCBApp.mMobile);
        params.put("action", "setprof");

        String autoPay = tvAutoPay.getText().toString();
        Matcher matcher = pattern.matcher(autoPay);
        if (matcher.find()) {
            autoPay = matcher.group();
        } else if (autoPay.contains("总是")) {
            autoPay = "-1";// 总是自动支付
        } else {
            autoPay = "0";// 不自动支付
        }
        params.put("limit_money", autoPay);

        // 判断充值提醒方式
        String lowBalance = tvLowBalance.getText().toString();
        matcher = pattern.matcher(lowBalance);
        if (matcher.find()) {
            lowBalance = matcher.group();
        } else {
            lowBalance = "0";// 不提醒
        }
        params.put("low_recharge", lowBalance);
        String url = URLUtils.genUrl(getUrl(), params);
        LogUtils.i(SettingFragment.class,
                "postSetting url: --->> " + url);
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String object) {
                LogUtils.i(SettingFragment.class,
                        "postSetting result: --->> " + object);
                if (!TextUtils.equals("1", object)) {
                    Toast.makeText(TCBApp.getAppContext(), "保存失败!", Toast.LENGTH_SHORT).show();
                } else {
                    // 提交成功，保存余额充值提醒
                    TCBApp.getAppContext()
                            .getSharedPreferences(TCBApp.mMobile,
                                    Context.MODE_PRIVATE)
                            .edit()
                            .putString(TCBApp.getAppContext().getString(R.string.sp_setting_low_recharge),
                                    params.get("low_recharge"))
                            .apply();
                }
            }
        }, new SimpleVolleyErrorListener());
        TCBApp.getAppContext().addToRequestQueue(request);
    }

    @Override
    protected String getTitle() {
        return "设置";
    }

    @Override
    protected void onNetWorkResponse(SettingInfo response) {
        if (response != null) {
            showDataView();
            setView(response);
        } else {
            showEmptyView("网络错误,点击重试~", 0, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getData();
                }
            });
        }
    }

    @Override
    protected int getFragmentContainerResID() {
        return R.id.fragment_container;
    }
}
