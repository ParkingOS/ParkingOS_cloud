package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.zhenlaidian.MyApplication;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.UpdateInfo;
import com.zhenlaidian.engine.UpdataInfoParser;
import com.zhenlaidian.engine.UpdateManager;
import com.zhenlaidian.util.ChannelUtil;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 欢迎界面;检查版本更新;
 */
@SuppressLint("SdCardPath")
public class HelloActivity extends Activity {

    private static final String TAG = "HelloActivity";
    private TextView tv_hello_version;
    private LinearLayout ll_hello_main;
    private UpdateInfo info;
    private String versiontext;
    private String updateurl;
    private Handler handler;
    private final int UPDATE = 888;
    private UpdateManager manager;
    private String version;

    @SuppressLint("HandlerLeak")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置无标题
        MyLog.v("HelloActivity", "onCreate-----");
        if (MyApplication.isrunning) {
            Intent intent = new Intent(this, LeaveActivity.class);
            startActivity(intent);
            this.finish();
            return;
        }
        manager = new UpdateManager(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏
        setContentView(R.layout.hello_activity);
        String channel = ChannelUtil.getChannel(this, "Website");
        AnalyticsConfig.setChannel(channel);
//        NBSAppAgent.setLicenseKey("a04ad42a66984f4391c6a23596a9dc9c").withLocationServiceEnabled(true).start(this);
        ll_hello_main = (LinearLayout) findViewById(R.id.ll_hello);
        tv_hello_version = (TextView) findViewById(R.id.tv_hello_version);
        versiontext = CommontUtils.getVersion(getApplicationContext());
        tv_hello_version.setText(versiontext);
        AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);
        aa.setDuration(2000);
        ll_hello_main.startAnimation(aa);
        updateurl = getString(R.string.updataurl);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case UPDATE:
                        showUpdataDialog();
                }
            }
        };
//        isNeedUpdate(); // 检查更新；
        loadMainUI();
    }

    // 需要更新时弹出升级对话框；
    private void showUpdataDialog() {
        AlertDialog.Builder builder = new Builder(this);
        builder.setIcon(R.drawable.app_icon_32);
        builder.setTitle("升级提醒");
        builder.setMessage(info.getDescription().replace("|", "\n"));
        builder.setPositiveButton("确定", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyLog.w(TAG, "下载真来电apk文件" + info.getApkurl());
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    manager.new DownLoadApkAsyncTask().execute(info.getApkurl());
                } else {
                    Toast.makeText(getApplicationContext(), "sd卡不可用", Toast.LENGTH_LONG).show();
                    loadMainUI();
                }
            }
        });
        builder.setNegativeButton("取消", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyLog.w(TAG, "用户取消进入登陆界面");
                SharedPreferencesUtils.getIntance(HelloActivity.this).setVersion(version);
                loadMainUI();
            }
        });
        builder.setCancelable(false).create().show();
    }

    public void isNeedUpdate() {
        new Thread(runnable).start();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            HttpURLConnection conn = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(updateurl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(8000);
                conn.setDoInput(true);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "text/html");
                conn.setRequestProperty("Accept-Charset", "utf-8");
                conn.setRequestProperty("contentType", "utf-8");
                inputStream = conn.getInputStream();
                byte[] buffer = null;
                if (conn.getResponseCode() == 200) {
                    buffer = new byte[1024];
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    buffer = out.toByteArray();
                }
                InputStream is = new ByteArrayInputStream(buffer);
                info = UpdataInfoParser.getUpdataInfo(is);
                MyLog.w("HelloActivity", "获取的升级信息是：" + info.toString());
                is.close();
                version = info.getVersion();
                MyLog.i("HelloActivity", "服务器端的版本为" + version);
                MyLog.i("HelloActivity", "客户端的版本为" + versiontext);
                if (version == null || version.equals("")) {
                    MyLog.w(TAG, "获取服务端版本错误，进入主界面");
                    loadMainUI();
                } else {
                    if (Integer.parseInt(versiontext) >= Integer.parseInt(version)) {
                        MyLog.d(TAG, "已是最新版,无需升级, 进入主界面");
                        SharedPreferencesUtils.getIntance(HelloActivity.this).setNewVersion(false);
                        loadMainUI();
                    } else {
                        if (info.getRemind() != null) {
                            if (info.getRemind().equals("0")) { // 0不提醒06-19修改;
                                MyLog.w(TAG, "弱升级，不提醒升级");
                                SharedPreferencesUtils.getIntance(HelloActivity.this).setNewVersion(true);
                                loadMainUI();
                            } else {
                                if (SharedPreferencesUtils.getIntance(HelloActivity.this).getVersion().equals(version)) {
                                    MyLog.i(TAG, "此版本为忽略版本，不提醒升级");
                                    loadMainUI();
                                } else {
                                    MyLog.i(TAG, "版本不同,需要升级");
                                    Message message = new Message();
                                    message.what = UPDATE;
                                    handler.sendMessage(message);
                                }
                            }
                        }else{
                            loadMainUI();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                MyLog.w("HelloActivity", "Network-error");
                loadMainUI();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    MyLog.w("HelloActivity", "释放资源出错");
                }
            }
        }
    };



    public void loadMainUI() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); // 把当前activity从任务栈里面移除

    }

    public void onResume() {
        super.onResume();
        MobclickAgent.updateOnlineConfig(this);// 友盟发送策略；
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        MyLog.v("HelloActivity", "onDestroy");
    }
}
