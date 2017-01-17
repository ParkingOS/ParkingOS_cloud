package com.tq.zld.view.fragment;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.baidu.mapapi.model.LatLng;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.protocal.SimpleVolleyErrorListener;
import com.tq.zld.util.Common;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.UGCActivity;

public class UploadFragment extends BaseFragment implements OnClickListener,
        Listener<String> {

    private EditText mParkNameEditText;
    private CheckBox mPaytypeCheckBox;
    private EditText mDescEditText;
    // private View mMoreInfoTextView;
    private View mRuleView;
    private View mAuditView;
    private View mUploadView;
    // private View mMoreInfoView;

    private UGCActivity mActivity;

    @Override
    protected String getTitle() {
        return "上传停车场";
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof UGCActivity) {
            mActivity = (UGCActivity) activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upload2, container, false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            mActivity.setMapMode(null);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mParkNameEditText = (EditText) view
                .findViewById(R.id.et_upload_parkname);
        mDescEditText = (EditText) view.findViewById(R.id.et_upload_desc);
        mPaytypeCheckBox = (CheckBox) view.findViewById(R.id.cb_upload_paytype);
        mRuleView = view.findViewById(R.id.tv_upload_rule);
        mRuleView.setOnClickListener(this);
        mAuditView = view.findViewById(R.id.tv_upload_audit);
        mAuditView.setOnClickListener(this);
        mUploadView = view.findViewById(R.id.btn_upload);
        mUploadView.setOnClickListener(this);
        // mMoreInfoTextView = view.findViewById(R.id.tv_upload_moreinfo);
        // mMoreInfoTextView.setOnClickListener(this);
        // mMoreInfoView = view.findViewById(R.id.ll_upload_moreinfo);
    }

    @Override
    public void onClick(View v) {
        if (v == mRuleView) {
            onRuleViewCLicked();
        } else if (v == mAuditView) {
            onAuditViewClicked();
        } else if (v == mUploadView) {
            onUploadViewClicked();
        }
        // else if (v == mMoreInfoTextView) {
        // // onMoreInfoTextViewClicked();
        // }
    }

    // private void onMoreInfoTextViewClicked() {
    // int start = 0;
    // int end = DensityUtils.dip2px(mActivity, 96);
    // if (mMoreInfoTextView.isSelected()) {
    // int temp;
    // temp = start;
    // start = end;
    // end = temp;
    // }
    // ViewUtils.performAnimate(mMoreInfoView, false, start, end, 200,
    // new AnimatorListener() {
    //
    // @Override
    // public void onAnimationStart(Animator animation) {
    // }
    //
    // @Override
    // public void onAnimationRepeat(Animator animation) {
    // }
    //
    // @Override
    // public void onAnimationEnd(Animator animation) {
    // mMoreInfoTextView.setSelected(!mMoreInfoTextView
    // .isSelected());
    // }
    //
    // @Override
    // public void onAnimationCancel(Animator animation) {
    // }
    // });
    // }

    private void onRuleViewCLicked() {
        final ProgressDialog dialog = ProgressDialog.show(mActivity, "",
                "请稍候...");
        WebView view = new WebView(mActivity);
        view.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                dialog.dismiss();
                new AlertDialog.Builder(mActivity).setView(view)
                        .setPositiveButton("知道了", null).show();
            }
        });
        WebSettings settings = view.getSettings();
        settings.setUseWideViewPort(false);
        settings.setLoadWithOverviewMode(false);
        view.loadUrl(TCBApp.mServerUrl + "carinter.do?action=upfine");
    }

    private void onAuditViewClicked() {
        // mActivity.getSupportFragmentManager().beginTransaction()
        // .replace(R.id.ugc_content, new AuditFragment()).commit();
        replace(R.id.ugc_content, new AuditFragment(), true);
    }

    private void onUploadViewClicked() {
        String parkName = mParkNameEditText.getText().toString().trim();
        if (!Common.checkParkName(parkName)) {
            Toast.makeText(getActivity(), "停车场名称不合法！", Toast.LENGTH_SHORT).show();
            return;
        }
        LatLng position = mActivity.getMap().getMapStatus().target;
        String address = mActivity.getAddress();
        String freePark = mPaytypeCheckBox.isChecked() ? "0" : "1";
        String desc = mDescEditText.getText().toString().trim();

        // 上传停车场 POST请求，否则中文乱码
        final HashMap<String, String> params = new HashMap<>();
        params.put("action", "uppark");
        params.put("mobile", TCBApp.mMobile);
        params.put("lat", String.valueOf(position.latitude));
        params.put("lng", String.valueOf(position.longitude));
        params.put("addr", address);
        params.put("parkname", parkName);
        params.put("type", freePark);
        params.put("desc", desc);
        URLUtils.decode(params);
        // String url = URLUtils.genUrl(TCBApp.mServerUrl + "carinter.do",
        // params);
        String url = TCBApp.mServerUrl + "carinter.do";

        StringRequest request = new StringRequest(Method.POST, url, this,
                new SimpleVolleyErrorListener()) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    @Override
    public void onResponse(String result) {
        if ("1".equals(result)) {
            Toast.makeText(TCBApp.getAppContext(), "感谢您提供的信息！",
                    Toast.LENGTH_SHORT).show();
            mDescEditText.setText("");
            mParkNameEditText.setText("");
        } else if ("-1".equals(result)) {
            Toast.makeText(TCBApp.getAppContext(), "您今天上传车场数量已达限制！",
                    Toast.LENGTH_SHORT).show();
        } else if ("-2".equals(result)) {
            Toast.makeText(TCBApp.getAppContext(), "车场位置重复！请重新选择~",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(TCBApp.getAppContext(), "上传失败！", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TCBApp.getAppContext().cancelPendingRequests(this);
    }
}
