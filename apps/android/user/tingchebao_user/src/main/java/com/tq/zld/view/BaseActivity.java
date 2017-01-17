package com.tq.zld.view;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.umeng.analytics.MobclickAgent;

public abstract class BaseActivity extends AppCompatActivity {

    protected ProgressDialog mDialog;

    @Override
    protected void onResume() {
        MobclickAgent.onResume(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // app 从后台唤醒，进入前台
        // Config.isActive = true;
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * 程序是否在前台运行
     *
     * @return
     */
    private boolean isAppOnForeground() {
        // Returns a list of application processes that are running on the
        // device

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();

        List<RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    /**
     * 显示网络请求进度框
     *
     * @param message              显示的提示信息，为空则显示“请稍候...”
     * @param cancelable           是否可取消
     * @param cancelOnTouchOutside 点击对话框外部可否取消：只有cancelable为true才有效
     */
    public void showProgressDialog(CharSequence message, boolean cancelable,
                                   boolean cancelOnTouchOutside) {
        if (mDialog == null) {
            message = TextUtils.isEmpty(message) ? "请稍候..." : message;
            mDialog = ProgressDialog.show(this, "", message, false, cancelable);
            if (cancelable) {
                mDialog.setCanceledOnTouchOutside(cancelOnTouchOutside);
            }
        }
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    /**
     * 隐藏网络请求进度框
     */
    public void dismissProgressDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
}
