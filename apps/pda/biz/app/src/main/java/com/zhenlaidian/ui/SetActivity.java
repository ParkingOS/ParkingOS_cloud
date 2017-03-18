package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.umeng.fb.FeedbackAgent;
import com.zhenlaidian.MyApplication;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.LoginInfo.WorksiteInfo;
import com.zhenlaidian.bean.SysApplication;
import com.zhenlaidian.bean.UpdateInfo;
import com.zhenlaidian.engine.UpdataInfoParser;
import com.zhenlaidian.engine.UpdateManager;
import com.zhenlaidian.service.BLEService;
import com.zhenlaidian.service.PullMsgService;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 设置页面;
 */
public class SetActivity extends BaseActivity {
    public static final String TAG = "SetActivity";
    private LinearLayout ll_feedback;// 友盟用户反馈
    private LinearLayout ll_check_update;// 检查更新
    private LinearLayout ll_ibeacon_set;// 蓝牙车场出口绑定
    private LinearLayout ll_check_exit;// 退出
    private CheckBox cb_voice_broadcast;// 订单语音播报
    private TextView tv_version_nummber;// 版本号
    private TextView tv_version_new;// 新版提醒
    private SharedPreferences broadcastsp;// 获取广播是否开启
    private String versiontext;// 当前应用版本号
    private UpdateInfo info;
    private UpdateManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyLog.w("SetActivity", "onCreate-----");
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.activity_my_setting);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        initView();
        setVeiw();
        manager = new UpdateManager(this);
    }

    public void initView() {
        versiontext = getVersion();
        ll_feedback = (LinearLayout) findViewById(R.id.ll_setting_feed_back);
        ll_check_update = (LinearLayout) findViewById(R.id.ll_setting_check_update);
        ll_check_exit = (LinearLayout) findViewById(R.id.ll_setting_check_exit);
        ll_ibeacon_set = (LinearLayout) findViewById(R.id.ll_setting_ibeacon_set);
        cb_voice_broadcast = (CheckBox) findViewById(R.id.cb_setting_voice_broadcast);
        tv_version_nummber = (TextView) findViewById(R.id.tv_setting_version_number);
        tv_version_new = (TextView) findViewById(R.id.tv_setting_version_new);
        broadcastsp = getSharedPreferences("voiceBroadcast", Context.MODE_PRIVATE);
        boolean isOpenBroadcast = broadcastsp.getBoolean("broadcast", false);
        if (isOpenBroadcast) {
            cb_voice_broadcast.setChecked(true);
        } else {
            cb_voice_broadcast.setChecked(false);
        }
    }

    public void setVeiw() {

        if (SharedPreferencesUtils.getIntance(this).getNewVersion()) {
            tv_version_new.setVisibility(View.VISIBLE);
        } else {
            tv_version_new.setVisibility(View.INVISIBLE);
        }

        tv_version_nummber.setText("V" + getVersions());

        // 友盟意见反馈
        ll_feedback.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                FeedbackAgent agent = new FeedbackAgent(SetActivity.this);
                agent.startFeedbackActivity();
            }
        });
        // 蓝牙车场出口绑定；
        ll_ibeacon_set.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (LeaveActivity.worksites != null) {
                    showIbeaconDialog();
                } else {
                    Toast.makeText(SetActivity.this, "请重新登录软件后再用此功能！", Toast.LENGTH_LONG).show();
                }

            }
        });
        // 点击检查更新
        ll_check_update.setOnClickListener(new OnClickListener() {
            long lasttime;

            @Override
            public void onClick(View v) {
                // TODO 检查更新；
                if (System.currentTimeMillis() - lasttime >= 2000) {
                    isNeedUpdate(versiontext);
                }
                lasttime = System.currentTimeMillis();
            }
        });
        // 点击退出
        ll_check_exit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder mDialog = new AlertDialog.Builder(SetActivity.this);
                mDialog.setTitle("操作提示");
                mDialog.setMessage("关闭软件 收不到停车宝消息。\n退出登录 切换其他账号登录。");
                mDialog.setNegativeButton("关闭软件", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        quit();
                    }
                });
                mDialog.setPositiveButton("退出登录", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences autologin = SetActivity.this.getSharedPreferences("autologin", Context.MODE_PRIVATE);
                        autologin.edit().clear().commit();
                        MyApplication.isrunning = false;
                        Intent pullservice = new Intent(SetActivity.this, PullMsgService.class);
                        stopService(pullservice);
                        Intent bleservice = new Intent(SetActivity.this, BLEService.class);
                        stopService(bleservice);
                        Intent intent = new Intent(SetActivity.this, LoginActivity.class);
                        SetActivity.this.startActivity(intent);
                        SysApplication.getInstance().exit();
                        SetActivity.this.finish();
                    }
                });
                mDialog.create().show();
            }
        });

        cb_voice_broadcast.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Editor edit = broadcastsp.edit();
                if (isChecked) {
                    edit.putBoolean("broadcast", true);
                    edit.commit();
                } else {
                    edit.putBoolean("broadcast", false);
                    edit.commit();
                }

            }
        });

    }

    public void showIbeaconDialog() {
        ArrayList<WorksiteInfo> workinfo = LeaveActivity.worksites;
        if (workinfo != null && workinfo.size() > 0) {
            String[] worksite = new String[workinfo.size()];
            final String[] id = new String[workinfo.size()];

            for (int i = 0; i < workinfo.size(); i++) {
                worksite[i] = workinfo.get(i).getWorksite_name();
                id[i] = workinfo.get(i).getId();
            }
            int worksited = SharedPreferencesUtils.getIntance(this).getWorksite();
            new AlertDialog.Builder(SetActivity.this).setTitle("请选择要绑定的出口").setIcon(R.drawable.app_icon_32)
                    .setSingleChoiceItems(worksite, worksited, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            Log.e("SetActivity", "点击的which是" + which);
                            bindingIbeacon(id[which]);
                            SharedPreferencesUtils.getIntance(SetActivity.this).setWorksite(which);
                            dialog.dismiss();
                        }
                    }).setNegativeButton("取消", null).create().show();
        }
    }

    // 选择蓝牙出口后确认绑定的接口：//collectorrequest.do?action=bindworksite&wid=&token=198f697eb27de5515e91a70d1f64cec7
    // {\"result\":\"1\"} result ：0失败，1成功
    public void bindingIbeacon(String export) {
        AQuery aQuery = new AQuery(this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=bindworksite&wid=" + export + "&token=" + token;
        MyLog.w("SetActivity-->>", "选择蓝牙出口后确认绑定的接口" + url);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                try {
                    JSONObject resultjson = new JSONObject(object);
                    String result = resultjson.getString("result");
                    MyLog.i("SetActivity-->>", "绑定蓝牙车场的结果是：" + result);
                    if ("1".equals(result)) {
                        Toast.makeText(SetActivity.this, "操作成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SetActivity.this, "操作失败,请重新绑定！", Toast.LENGTH_SHORT).show();
                        showIbeaconDialog();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // actionBar的点击回调方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    // 获取当前应用程序的版本号
    private String getVersion() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), Toast.LENGTH_SHORT);
            return String.valueOf(info.versionCode);
        } catch (Exception e) {
            e.printStackTrace();
            return "版本号未知";
        }
    }

    public void toastNoUpdate(String info) {
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
    }

    /**
     * 检查是否需要更新版本
     *
     * @param versiontext 当前客户端的版本号信息
     * @return 是否需要更新
     */
    private void isNeedUpdate(final String versiontext) {

        String url = this.getResources().getString(R.string.updataurl);
        System.out.println("访问更新信息的url--------->>>>>>" + url);
        AQuery aQuery = new AQuery(this);
        if (IsNetWork.IsHaveInternet(this)) {
            final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取检查更新数据...", true, true);
            aQuery.ajax(url, byte[].class, new AjaxCallback<byte[]>() {

                @Override
                public void callback(String url, byte[] object, AjaxStatus status) {

                    if (object != null) {
                        dialog.dismiss();
                        InputStream is = new ByteArrayInputStream(object);
                        try {
                            info = UpdataInfoParser.getUpdataInfo(is);
                            Log.e("SetActivity", "获取的升级信息是：" + info.toString());
                            is.close();
                            String version = info.getVersion();
                            MyLog.i("SetActivity", "服务器端的版本为" + version);
                            MyLog.i("SetActivity", "客户端的版本为" + versiontext);
                            if (version == null || version.equals("")) {
                                MyLog.w(TAG, "获取服务端版本号异常！");
                                toastNoUpdate("获取服务端版本号异常!");
                            } else {
                                if (Integer.parseInt(versiontext) >= Integer.parseInt(version)) {
                                    MyLog.w(TAG, "已是最新版,无需升级, 进入主界面"+versiontext+"---"+version);
                                    toastNoUpdate("已是最新版,无需升级!");
                                } else {
                                    MyLog.w(TAG, "版本不同,需要升级");
                                    showUpdataDialog();
                                }
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            toastNoUpdate("获取更新异常!");
                        }
                    } else {
                        dialog.dismiss();
                        MyLog.w(TAG, "获取更新超时，进入主界面");
                        toastNoUpdate("获取更新超时!");
                    }
                }
            });
        } else {
            MyLog.i(TAG, "没有网络, 进入主界面");
            toastNoUpdate("请检查网络!");
        }

    }

    // 需要更新时弹出升级对话框；
    @SuppressLint("SdCardPath")
    private void showUpdataDialog() {
        AlertDialog.Builder builder = new Builder(this);
        builder.setIcon(R.drawable.app_icon_32);
        builder.setTitle("升级提醒");
        builder.setMessage(info.getDescription());
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyLog.w(TAG, "下载真来电apk文件" + info.getApkurl());
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    manager.new DownLoadApkAsyncTask().execute(info.getApkurl());
                } else {
                    Toast.makeText(getApplicationContext(), "sd卡不可用", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyLog.w(TAG, "用户取消进入登陆界面");
            }
        });
        builder.setCancelable(false).create().show();
    }

    public void quit() {
        // 关闭轮询获取网的服务;
        Intent pullservice = new Intent(SetActivity.this, PullMsgService.class);
        stopService(pullservice);
        Intent bleservice = new Intent(SetActivity.this, BLEService.class);
        stopService(bleservice);
        MyLog.w("DrawerOnItemClick", "服务已关闭");
        SysApplication.getInstance().exit();// finish所有activity===>退出软件
        MyLog.w("DrawerOnItemClick", "全部finish");
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                MyLog.w("DrawerOnItemClick", "退出虚拟机");
                SetActivity.this.finish();
                MyApplication.getInstance().exit();
                System.exit(0);// 1秒钟后退出虚拟机；
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 1000);
    }

    // 获取当前应用程序的版本号
    private String getVersions() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), Toast.LENGTH_SHORT);
            return String.valueOf(info.versionName);
        } catch (Exception e) {
            e.printStackTrace();
            return "版本号未知";
        }
    }

}
