package com.tq.zld.view.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.util.AndroidUtils;
import com.tq.zld.util.UpdateManager;
import com.tq.zld.view.map.WebActivity;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

public class AboutFragment extends BaseFragment implements OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.tv_about_help).setOnClickListener(this);
//        view.findViewById(R.id.tv_about_feedback).setOnClickListener(this);
        view.findViewById(R.id.tv_about_checkupdate).setOnClickListener(this);
        TextView tv_version = (TextView) view.findViewById(R.id.tv_about_version);
        String version = AndroidUtils.getVersionName();
        if (TextUtils.isEmpty(version)) {
            version = "版本号未知";
        }
        tv_version.setText(version);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.tv_about_feedback:
            // 开启友盟意见反馈
            // agent.startFeedbackActivity();
//                break;
            case R.id.tv_about_help:
                startHelpActivity();
                break;
            case R.id.tv_about_checkupdate:
//                new UpdateManager(getActivity()).checkUpdate();
                final ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "请稍候...", true, true);
                dialog.setCanceledOnTouchOutside(false);
                UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
                    @Override
                    public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                        dialog.dismiss();
                        switch (updateStatus) {
                            case UpdateStatus.Yes: // has update
//                                UmengUpdateAgent.showUpdateDialog(mContext, updateInfo);
                                break;
                            case UpdateStatus.No: // has no update
                                Toast.makeText(TCBApp.getAppContext(), "恭喜您使用的已经是最新版本！", Toast.LENGTH_SHORT).show();
                                break;
                            case UpdateStatus.NoneWifi: // none wifi
                                Toast.makeText(TCBApp.getAppContext(), "注意：当前未连接WiFi网络！", Toast.LENGTH_SHORT).show();
                                break;
                            case UpdateStatus.Timeout: // time out
                                Toast.makeText(TCBApp.getAppContext(), "请求超时，请重试！", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
                UmengUpdateAgent.forceUpdate(TCBApp.getAppContext());
                break;
        }
    }

    private void startHelpActivity() {
        Intent intent = new Intent(TCBApp.getAppContext(), WebActivity.class);
        intent.putExtra(WebActivity.ARG_TITLE, "常见问题");
        intent.putExtra(WebActivity.ARG_URL, TCBApp.mServerUrl + "help.jsp");
        startActivity(intent);
    }

    @Override
    protected String getTitle() {
        return "关于";
    }
}
