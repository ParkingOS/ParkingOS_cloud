package com.tq.zld.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.reflect.TypeToken;
import com.tq.zld.BuildConfig;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.AccountInfo;
import com.tq.zld.bean.Plate;
import com.tq.zld.im.adapter.EMCallBackAdapter;
import com.tq.zld.im.lib.HXSDKHelper;
import com.tq.zld.protocal.SimpleVolleyErrorListener;
import com.tq.zld.util.Common;
import com.tq.zld.util.KeyboardUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.MathUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.LoginActivity;
import com.tq.zld.view.SplashActivity;
import com.tq.zld.view.account.AccountDetailActivity;
import com.tq.zld.view.account.AccountTicketsActivity;
import com.tq.zld.view.account.BoughtProductActivity;
import com.tq.zld.view.account.MyRedPacketsActivity;
import com.tq.zld.view.holder.MenuHolder;
import com.tq.zld.view.manager.IMManager;
import com.tq.zld.view.map.WebActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AccountFragment extends NetworkFragment<AccountInfo> implements
        OnClickListener {

    private TextView tvBalance;
    //    private TextView tvPlate;
    private TextView tvMobile;
    private TextView mCreditTextView;
    private TextView mCertifyTextView;

    private AccountInfo mAccountInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    private void refreshView(AccountInfo info) {
        String value = info.getBalance();
//        String carNumber = info.getCarNumber();
        String mobileNum = info.getMobile();
        if (TextUtils.isEmpty(value) || TextUtils.isEmpty(mobileNum)) {
            return;
        }
        tvBalance.setText(value);
//        tvPlate.setText(carNumber);
        tvMobile.setText(mobileNum);

        // 设置信用额度
        if (info.limit == 0) {
            mCreditTextView.setText("信用额度: 0/0");
        } else {
            String color = "#32a669";
            if (info.limit_balan < info.limit_warn) {
                color = "#e37479";
            }
            mCreditTextView.setText(
                    Html.fromHtml("信用额度: <font color=" + color + ">"
                            + MathUtils.parseIntString(info.limit_balan)
                            + "</font>/"
                            + MathUtils.parseIntString(info.limit)));
        }

        // 设置车牌认证状态
        switch (info.state) {
            case Plate.STATE_CERTIFY:
                //未认证
                mCertifyTextView.setText("未认证，无信用额度");
                mCertifyTextView.setTextColor(getResources().getColor(R.color.text_red));
                mCertifyTextView.setBackgroundResource(R.drawable.shape_account_plate_certify);
                break;
            case Plate.STATE_CERTIFIED:
                //已认证
                mCertifyTextView.setText("已认证");
                mCertifyTextView.setTextColor(getResources().getColor(R.color.text_green));
                mCertifyTextView.setBackgroundResource(R.drawable.shape_account_plate_certified);
                break;
            case Plate.STATE_CERTIFYING:
                //认证中
                mCertifyTextView.setText("审核中(1-3天)");
                mCertifyTextView.setTextColor(getResources().getColor(R.color.text_orange));
                mCertifyTextView.setBackgroundResource(R.drawable.shape_account_plate_certifying);
                break;
            case Plate.STATE_CERTIFY_FAILED:
                //未认证
                mCertifyTextView.setText("认证未通过，请重新上传证件照");
                mCertifyTextView.setTextColor(getResources().getColor(R.color.text_red));
                mCertifyTextView.setBackgroundResource(R.drawable.shape_account_plate_certify);
                break;
            case Plate.STATE_CERTIFY_BLOCKED:
                //车牌无效
                mCertifyTextView.setText("车牌无效，请联系停车宝客服");
                mCertifyTextView.setTextColor(getResources().getColor(R.color.text_red));
                mCertifyTextView.setBackgroundResource(R.drawable.shape_account_plate_certify);
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    @Override
    public void onClick(View v) {

        if (R.id.btn_account_quit != v.getId()
                && (mAccountInfo == null || TextUtils
                .isEmpty(mAccountInfo.mobile))) {
            Toast.makeText(TCBApp.getAppContext(), "账户信息异常！", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = null;
        switch (v.getId()) {
            case R.id.btn_account_recharge:
                RechargeFragment rechargeFragment = new RechargeFragment();
                Bundle args = new Bundle();
                args.putParcelable(RechargeFragment.ARG_WHO, mAccountInfo);
                rechargeFragment.setArguments(args);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(this);
                ft.add(R.id.fragment_container, rechargeFragment, rechargeFragment
                        .getClass().getSimpleName());
                ft.addToBackStack(null);
                ft.commit();
                break;
            case R.id.rl_account_coupon:
                intent = new Intent(getActivity(), AccountTicketsActivity.class);
                break;
            case R.id.ll_account_monthlypay:
                intent = new Intent(getActivity(), BoughtProductActivity.class);
                break;
            case R.id.ll_account_detail:
                intent = new Intent(getActivity(), AccountDetailActivity.class);
                break;
            case R.id.rl_account_plate:
                onPlateViewClicked();
                break;
            case R.id.btn_account_quit:
                showQuitDialog();
                break;
            case R.id.ll_account_redpacket:
                intent = new Intent(getActivity(), MyRedPacketsActivity.class);
                break;
            case R.id.tv_account_credit:
                intent = new Intent(getActivity(), WebActivity.class);
                intent.putExtra(WebActivity.ARG_TITLE, "信用额度帮助");
                intent.putExtra(WebActivity.ARG_URL, getString(R.string.url_credit_help));
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    private void showQuitDialog() {
        AlertDialog.Builder mDialog = new AlertDialog.Builder(getActivity());
        mDialog.setMessage("确认退出？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Map<String, String> params = new HashMap<>();
                        params.put("cid", "");
                        params.put("mobile", TCBApp.mMobile);
                        params.put("action", "addcid");
                        new SplashActivity().updateClientId(params);

                        TCBApp.getAppContext().getConfigPrefs().edit()
                                .remove(getString(R.string.sp_mobile))
                                .remove(getString(R.string.sp_login_mobile))
                                .apply();
                        TCBApp.mMobile = "";
                        MenuHolder.getInstance().refreshAccountInfo();
                        //退出环信
                        if(BuildConfig.IM_DEBUG){
                            HXSDKHelper.getInstance().logout(true, new EMCallBackAdapter(){
                                @Override
                                public void onSuccess() {
                                    LogUtils.w("退出环信成功");
                                    IMManager.getInstance().unregEventListener();
                                    IMManager.getInstance().destoryHX();
                                }
                            });
                        }
                        getActivity().finish();
                    }
                }).setNegativeButton("取消", null).show();
    }

    private void onPlateViewClicked() {
        // http://s.zhenlaidian.com/zld/carowner.do?action=editcarnumber&carnumber=**&mobile=**
//        showEditPlateDialog();
        replace(R.id.fragment_container, new PlateFragment(), true);
    }

    private void showEditPlateDialog() {
        View view = View.inflate(getActivity(), R.layout.dialog_simple_input, null);
        final EditText etPlate = (EditText) view
                .findViewById(R.id.dialog_edittext);
//        etPlate.setText(tvPlate.getText());
        etPlate.setSelection(etPlate.getText().length());
        new AlertDialog.Builder(getActivity()).setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        KeyboardUtils.closeKeybord(etPlate, getActivity());
                        final String carNumber = etPlate.getText().toString()
                                .replaceAll(" ", "")
                                .toUpperCase(Locale.SIMPLIFIED_CHINESE);
                        if (Common.checkPlate(carNumber)) {
                            commitNewPlate(carNumber);
                        } else {
                            Toast.makeText(TCBApp.getAppContext(), "请输入正确的车牌号！",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                KeyboardUtils.closeKeybord(etPlate, getActivity());
            }
        }).show();
        KeyboardUtils.openKeybord(etPlate, getActivity());
    }

    private void commitNewPlate(final String carNumber) {

        StringRequest request = new StringRequest(Request.Method.POST, getUrl(), new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {

                switch (result) {
                    case "1":

//                        tvPlate.setText(carNumber
//                                .toUpperCase(Locale.SIMPLIFIED_CHINESE));
                        Toast.makeText(TCBApp.getAppContext(),
                                "修改成功！",
                                Toast.LENGTH_SHORT)
                                .show();

                        // 保存车牌号和手机号到本地，并刷新侧滑菜单
                        TCBApp.getAppContext().saveStringSync(R.string.sp_mobile, mAccountInfo.mobile);
//                        TCBApp.getAppContext().saveStringSync(R.string.sp_plate, tvPlate.getText()
//                                .toString());
                        MenuHolder
                                .getInstance()
                                .refreshAccountInfo();

                        break;
                    case "2":
                        showSamePlateDialog();
                        break;
                    default:
                        Toast.makeText(TCBApp.getAppContext(),
                                "修改失败！",
                                Toast.LENGTH_SHORT)
                                .show();
                        break;
                }
            }
        }, new SimpleVolleyErrorListener()) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("action", "editcarnumber");
                params.put("carnumber", carNumber);
                params.put("mobile", TCBApp.mMobile);
                URLUtils.decode(params);
                return params;
            }
        };

        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    private void showSamePlateDialog() {
        // 车牌号重复
        new AlertDialog.Builder(getActivity())
                .setTitle("注意！")
                .setMessage(
                        "此车牌已经注册过，您可直接使用注册此车牌的手机号进行登录。如有疑问，请联系停车宝客服：\n010-53618108")
                .setPositiveButton("重新填写",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                onPlateViewClicked();
                            }
                        })
                .setNegativeButton("联系客服",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Uri uri = Uri.parse("tel:010-53618108");
                                startActivity(new Intent(Intent.ACTION_DIAL,
                                        uri));
                            }
                        }).show();
    }

    @Override
    protected String getTitle() {
        return "我的账户";
    }

    @Override
    public void onNetWorkResponse(AccountInfo response) {

        mAccountInfo = response;
        if (mAccountInfo != null && !TextUtils.isEmpty(mAccountInfo.mobile)) {
            showDataView();
            refreshView(mAccountInfo);
            // 保存车牌号和手机号到本地
            TCBApp.getAppContext().saveStringSync(R.string.sp_mobile, mAccountInfo.mobile);
//            TCBApp.getAppContext().saveString(R.string.sp_plate, mAccountInfo.carNumber);
//            TCBApp.getAppContext().saveIntSync(R.string.sp_plate_state, mAccountInfo.state);
            String key = getString(R.string.sp_plate_state);
            TCBApp.getAppContext().getAccountPrefs().edit().putInt(key, mAccountInfo.state).commit();
            MenuHolder.getInstance().refreshAccountInfo();
        } else {
            showEmptyView("账户信息异常，点击重新登录", 0, new OnClickListener() {

                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                }
            });
        }
    }

    @Override
    protected int getFragmentContainerResID() {
        return R.id.fragment_container;
    }

    @Override
    protected void initView(View view) {
//        tvPlate = (TextView) view.findViewById(R.id.tv_account_plate);
        tvMobile = (TextView) view.findViewById(R.id.tv_account_mobile);
        tvBalance = (TextView) view.findViewById(R.id.tv_account_balance);
        view.findViewById(R.id.btn_account_quit).setOnClickListener(this);
        view.findViewById(R.id.btn_account_recharge).setOnClickListener(this);
        view.findViewById(R.id.rl_account_coupon).setOnClickListener(this);
        view.findViewById(R.id.ll_account_monthlypay).setOnClickListener(this);
        view.findViewById(R.id.ll_account_detail).setOnClickListener(this);
        view.findViewById(R.id.ll_account_mobile).setOnClickListener(this);
        view.findViewById(R.id.rl_account_plate).setOnClickListener(this);
        view.findViewById(R.id.ll_account_redpacket).setOnClickListener(this);

        mCertifyTextView = (TextView) view.findViewById(R.id.tv_account_plate_state);
        mCreditTextView = (TextView) view.findViewById(R.id.tv_account_credit);
        mCreditTextView.setOnClickListener(this);
    }

    @Override
    protected TypeToken<AccountInfo> getBeanListType() {
        return null;
    }

    @Override
    protected Class<AccountInfo> getBeanClass() {
        return AccountInfo.class;
    }

    @Override
    protected String getUrl() {
        return TCBApp.mServerUrl + "carowner.do";
    }

    @Override
    protected Map<String, String> getParams() {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "detail");
        params.put("mobile", TCBApp.mMobile);
        return params;
    }

}
