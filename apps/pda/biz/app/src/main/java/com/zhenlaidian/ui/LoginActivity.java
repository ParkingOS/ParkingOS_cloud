package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.device.DeviceManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.MyApplication;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.Berths;
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.bean.LoginInfo;
import com.zhenlaidian.bean.LoginInfo.WorksiteInfo;
import com.zhenlaidian.ui.register.RegisterAsDocumentPhotoActivity;
import com.zhenlaidian.ui.register.RegisterAsMobileActivity;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.DataTypeChange;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MD5Utils;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesHandler;
import com.zhenlaidian.util.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * 登陆界面;
 */
@SuppressLint("CommitPrefEdits")
public class LoginActivity extends BaseActivity {

    private AutoCompleteTextView at_login_username;// 用户名
    private EditText et_login_password;// 密码
    private EditText et_login_voice;// 收集语音合成；
    private Button bt_login;
    private Button bt_register;
    private Button bt_forget;
    private LinearLayout ll_changeurl;
    private Button bt_server;
    private Button bt_laoyao;
    private Button bt_laowang;
    private Button bt_xiaohui;
    private Button bt_ceshi;
    private TextView tv_version_name;
    private ArrayList<String> accounts;
    private ProgressDialog dialog;
    final static String regularEx = "|";
    private Set<String> users = new HashSet<String>();

    // private NfcOrderDao ndd;
    // private DBNfcOrder order;

    @SuppressLint("UseValueOf")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.login_activity);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        initView();
        setView();

        putStringToPreference("name", "");
        if (getIntent().getStringExtra("token") != null && getIntent().getStringExtra("token").equals("false")) {
            Toast.makeText(this, "你的账号在别处登录，请修改密码确保安全！", Toast.LENGTH_SHORT).show();
        } else {
            // 自动登录
            SharedPreferences autologin = getSharedPreferences("autologin", Context.MODE_PRIVATE);
            if (!(autologin.getString("account", "").equals("") && autologin.getString("password", "").equals(""))) {

                if (autologin.getString("account", "") != null && autologin.getString("passwd", "") != null) {
                    at_login_username.setText(autologin.getString("account", ""));
                    et_login_password.setText(autologin.getString("passwd", ""));
                    try {
                        if (!getBooleanFromPreference("already")) {
                            String md5password = MD5Utils.MD5(MD5Utils.MD5(autologin.getString("passwd", "")) + "zldtingchebao201410092009");
                            longinServes(autologin.getString("account", ""), md5password, autologin.getString("passwd", ""), users);
                        }
                    } catch (Exception e) {
                        MyLog.w("LoginActivity", "MD5加密异常！");
                        e.printStackTrace();
                    }
                }
            }
        }
        if (CommontUtils.Is910()) {
            new DeviceManager().enableHomeKey(false);
            new DeviceManager().enableStatusBar(false);
        }
    }

    public void initView() {
        at_login_username = (AutoCompleteTextView) findViewById(R.id.at_login_account);
        et_login_password = (EditText) findViewById(R.id.et_login_password);
        et_login_voice = (EditText) findViewById(R.id.et_login_voice);
        tv_version_name = (TextView) findViewById(R.id.tv_version_name);
        ll_changeurl = (LinearLayout) findViewById(R.id.ll_login_change_url);
        bt_login = (Button) findViewById(R.id.bt_longin_login);
        bt_register = (Button) findViewById(R.id.bt_longin_register);
        bt_forget = (Button) findViewById(R.id.bt_longin_forget);
        bt_server = (Button) findViewById(R.id.bt_longin_url_server);
        bt_laoyao = (Button) findViewById(R.id.bt_longin_url_oldyao);
        bt_laowang = (Button) findViewById(R.id.bt_longin_url_oldwang);
        bt_xiaohui = (Button) findViewById(R.id.bt_longin_url_xiaohui);
        bt_ceshi = (Button) findViewById(R.id.bt_longin_url_ceshi);
        String url = SharedPreferencesUtils.getIntance(this).getUrl();
        if (url.equals("1")) {
            tv_version_name.setText("当前版本：V" + getVersions() + " -S");
            bt_server.setBackgroundColor(Color.parseColor("#ffa500"));
        } else if (url.equals("2")) {
            tv_version_name.setText("当前版本：V" + getVersions() + " -240");
            bt_laoyao.setBackgroundColor(Color.parseColor("#ffa500"));
        } else if (url.equals("3")) {
            tv_version_name.setText("当前版本：V" + getVersions() + " -239");
            bt_laowang.setBackgroundColor(Color.parseColor("#ffa500"));
        } else if (url.equals("4")) {
            tv_version_name.setText("当前版本：V" + getVersions() + " -251");
            bt_xiaohui.setBackgroundColor(Color.parseColor("#ffa500"));
        }
        // 定义字符串作为提示的文本；
        SharedPreferences prefs = getSharedPreferences("usernames", Context.MODE_PRIVATE);
        users = SharedPreferencesHandler.getStringSet(prefs, "usernames", users);
        users.remove("");
        MyLog.i("LoginActivity", "获取的sharedpreferences已存账号为" + users.toString());
        accounts = new ArrayList<>();
        accounts.addAll(users);
        dialog = new ProgressDialog(this);
        dialog.setMessage("登录中...");
        dialog.setCanceledOnTouchOutside(false);
        // 创建一个arrayadapter；
        ArrayAdapter<String> av = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, accounts);
        at_login_username.setAdapter(av);
        // 将AutoCompletetextViwe与ArrayAdapter进行绑定；
        at_login_username.setThreshold(0);// 出入一个字符就开始提示
        if (accounts != null && accounts.size() > 0) {
            at_login_username.setText(accounts.get(0));
        }
    }

    public void setView() {
        bt_login.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 获取到输入的账号密码。与服务器对比；
                String username = at_login_username.getText().toString().trim();
                String password = et_login_password.getText().toString().trim();
                System.out.println("获取到的账号是-->>" + username);
                System.out.println("获取到的密码是-->>" + password);
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "账号或密码不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    // 传入用户名和密码提交给服务器;
                    try {
                        String md5password = MD5Utils.MD5(MD5Utils.MD5(password) + "zldtingchebao201410092009");
                        longinServes(username, md5password, password, users);
                    } catch (Exception e) {
                        MyLog.w("LoginActivity", "MD5加密异常！");
                        e.printStackTrace();
                    }
                }
            }

        });

        bt_forget.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, GetPasswordActivity.class);
                startActivity(intent);
            }
        });

        if (getResources().getString(R.string.baidukey).equals("k0XC18nUQ0Mdl24zg1VPCnW5")) {
            bt_server.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferencesUtils.getIntance(LoginActivity.this).setUrl("1");
                    bt_server.setBackgroundColor(Color.parseColor("#ffa500"));
                    bt_laowang.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    bt_laoyao.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    bt_xiaohui.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    bt_ceshi.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    tv_version_name.setText("当前版本：V" + getVersions() + " -S");
                }
            });
            bt_laoyao.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    SharedPreferencesUtils.getIntance(LoginActivity.this).setUrl("2");
                    bt_laoyao.setBackgroundColor(Color.parseColor("#ffa500"));
                    bt_server.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    bt_laowang.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    bt_xiaohui.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    bt_ceshi.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    tv_version_name.setText("当前版本：V" + getVersions() + " -240");
                }
            });
            bt_laowang.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    SharedPreferencesUtils.getIntance(LoginActivity.this).setUrl("3");
                    bt_laowang.setBackgroundColor(Color.parseColor("#ffa500"));
                    bt_server.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    bt_laoyao.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    bt_xiaohui.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    bt_ceshi.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    tv_version_name.setText("当前版本：V" + getVersions() + " -239");
                }
            });
            bt_xiaohui.setOnClickListener(new OnClickListener() {

                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View v) {
                    SharedPreferencesUtils.getIntance(LoginActivity.this).setUrl("4");
                    bt_xiaohui.setBackgroundColor(Color.parseColor("#ffa500"));
                    bt_server.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    bt_laoyao.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    bt_laowang.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    bt_ceshi.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    tv_version_name.setText("当前版本：V" + getVersions() + " -251");
                }
            });
            bt_ceshi.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    SharedPreferencesUtils.getIntance(LoginActivity.this).setUrl("5");
                    bt_ceshi.setBackgroundColor(Color.parseColor("#ffa500"));
                    bt_xiaohui.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    bt_server.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    bt_laoyao.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    bt_laowang.setBackgroundColor(Color.parseColor("#c9c9c9"));
                    tv_version_name.setText("当前版本：V" + getVersions() + " -224");
                }
            });
        } else {
            tv_version_name.setText("当前版本：V" + getVersions());
            ll_changeurl.setVisibility(View.GONE);
        }
    }

    // 把用户输入的账号和密码提交给服务器，验证账户和密码是否正确；
    // >http://s.zhenlaidian.com/zld/collectorlogin.do?username=&password=
    @SuppressLint("NewApi")
    public void longinServes(final String username, final String MD5password, final String password, final Set<String> users) {

        if (IsNetWork.IsHaveInternet(this)) {
            dialog.show();
            AQuery aQuery = new AQuery(this);
            String url = Config.getUrl(this) + "collectorlogin.do?username=" + username + "&password=" + MD5password
                    + "&version=" + CommontUtils.getVersion(LoginActivity.this) + "&devicecode=" + CommontUtils.GetHardWareAddress(this) + "&out=json";
            MyLog.w("LoginActivity", "登录的URL-->>" + url);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    dialog.dismiss();
                    if (object != null && status.getCode() == 200) {
                        MyLog.i("LoginActivity", "登陆的返回信息是---" + object);
                        Gson gson = new Gson();
                        LoginInfo info = gson.fromJson(object, LoginInfo.class);
                        MyLog.i("LoginActivity", "解析登录信息：" + info.toString());
                        loginState(info, username, password);
                        putStringToPreference("uid", username);
                    } else {
                        dialog.dismiss();
                        switch (status.getCode()) {
                            case -101:
                                Toast.makeText(LoginActivity.this, "网络错误！", Toast.LENGTH_SHORT).show();
                                break;
                            case 500:
                                Toast.makeText(LoginActivity.this, "服务器错误！", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }

            });
        } else {
            Toast.makeText(this, "当前网络不可用", Toast.LENGTH_SHORT).show();
            bt_login.setClickable(true);
        }
    }

    //处理登陆地返回结果;
    public void loginState(LoginInfo info, String username, String password) {
        putBooleanToPreference("alreadyalert", false);
        if (info.getInfo().equals("success")) {
            // 自动账号提示记录
            if (!users.contains(username)) {
                SharedPreferences sp1 = getSharedPreferences("usernames", Context.MODE_PRIVATE);
                Editor edit = sp1.edit();
                users.add(username);
                MyLog.i("要插入的set集合数据为---", users.toString());
                SharedPreferencesHandler.putStringSet(edit, "usernames", users).commit();
            }
            if (info.getState() != null && info.getState().equals("0")) {// 正常用户登录
                if (!TextUtils.isEmpty(info.getIsshowepay())) {// 保存是否可以查看直付订单；
                    SharedPreferencesUtils.getIntance(LoginActivity.this).setIsShowEpay(info.getIsshowepay());
                }
                if (!TextUtils.isEmpty(info.getCtotal())) {//保存车场总数,主页订单页面需要此数据;
                    SharedPreferencesUtils.getIntance(LoginActivity.this).setParkTotal(info.getCtotal());
                }
                saveAccountAndPwd(username, password, info.getRole(), info.getIscancel());
                SharedPreferences sp = getSharedPreferences("tingchebao", Context.MODE_PRIVATE);
                sp.edit().putBoolean("iswait", false).commit();
                sp.edit().putString("regMobile", null).commit();
                SharedPreferencesUtils.getIntance(LoginActivity.this).setToken(info.getToken());
                SharedPreferencesUtils.getIntance(LoginActivity.this).setAccount(username);
                SharedPreferencesUtils.getIntance(LoginActivity.this).setParkname(info.getCname());
                SharedPreferencesUtils.getIntance(LoginActivity.this).setComid(info.getComid());
                SharedPreferencesUtils.getIntance(LoginActivity.this).setName(info.getName());

                SharedPreferencesUtils.getIntance(LoginActivity.this).setchange_prepay(info.getChange_prepay());
                SharedPreferencesUtils.getIntance(LoginActivity.this).setview_plot(info.getView_plot());
                SharedPreferencesUtils.getIntance(LoginActivity.this).setlogontime(info.getLogontime());
                SharedPreferencesUtils.getIntance(LoginActivity.this).setberthid(info.getBerthid());
                SharedPreferencesUtils.getIntance(LoginActivity.this).setberth_name(info.getBerth_name());
                SharedPreferencesUtils.getIntance(LoginActivity.this).setberthsec_name(info.getBerthsec_name());
                SharedPreferencesUtils.getIntance(LoginActivity.this).setfirstprovince(info.getFirstprovince());
                SharedPreferencesUtils.getIntance(LoginActivity.this).setisprepay(info.getIsprepay());
                SharedPreferencesUtils.getIntance(LoginActivity.this).sethidedetail(info.getHidedetail());
                SharedPreferencesUtils.getIntance(LoginActivity.this).setmobile(info.getMobile());
                SharedPreferencesUtils.getIntance(LoginActivity.this).setsignoutvalid(info.getSignoutvalid());
                SharedPreferencesUtils.getIntance(LoginActivity.this).setprint_order_place2(info.getPrint_order_place2());
                SharedPreferencesUtils.getIntance(LoginActivity.this).setisprintName(info.getIs_print_name());

                ArrayList<Integer> photoset = info.getPhotoset();
                ArrayList<Berths> berths = info.getBerths();
                ArrayList<String> prepayset = info.getPrepayset();
                ArrayList<String> print_sign = info.getPrint_sign();
                if (CommontUtils.checkList(photoset)) {
                    for (int i = 0; i < photoset.size(); i++) {
                        SharedPreferencesUtils.getIntance(LoginActivity.this).setphotoset(i, photoset.get(i));
                    }
                }
                if (CommontUtils.checkList(prepayset)) {
                    for (int i = 0; i < prepayset.size(); i++) {
                        SharedPreferencesUtils.getIntance(LoginActivity.this).setprepayset(i, prepayset.get(i));
                    }
                }
//                if (CommontUtils.checkList(print_sign)) {
//                    SharedPreferencesUtils.getIntance(LoginActivity.this).setprint_signIn(print_sign.get(0));
//                    SharedPreferencesUtils.getIntance(LoginActivity.this).setprint_signOut(print_sign.get(1));
//                }
                //如果返回 进场、出场票头 ，注释掉上面的方法，用这个方法
                if (CommontUtils.checkList(print_sign)) {
                    if (CommontUtils.checkList(print_sign) && print_sign.size() > 2) {
                        SharedPreferencesUtils.getIntance(LoginActivity.this).setprint_signInHead(print_sign.get(2));
                        SharedPreferencesUtils.getIntance(LoginActivity.this).setprint_signOutHead(print_sign.get(3));
                        SharedPreferencesUtils.getIntance(LoginActivity.this).setprint_signIn(print_sign.get(0));
                        SharedPreferencesUtils.getIntance(LoginActivity.this).setprint_signOut(print_sign.get(1));
                    } else {
                        SharedPreferencesUtils.getIntance(LoginActivity.this).setprint_signIn(print_sign.get(0));
                        SharedPreferencesUtils.getIntance(LoginActivity.this).setprint_signOut(print_sign.get(1));
                    }
                }

                //测试是否储存成功
//                MyLog.i("LoginActivity", "0=" + SharedPreferencesUtils.getIntance(LoginActivity.this).getphotoset(0));
//                MyLog.i("LoginActivity", "1=" + SharedPreferencesUtils.getIntance(LoginActivity.this).getphotoset(1));
//                MyLog.i("LoginActivity", "2=" + SharedPreferencesUtils.getIntance(LoginActivity.this).getphotoset(2));
//
//                MyLog.i("LoginActivity", "0=" + SharedPreferencesUtils.getIntance(LoginActivity.this).getprepayset(0));
//                MyLog.i("LoginActivity", "1=" + SharedPreferencesUtils.getIntance(LoginActivity.this).getprepayset(1));
//                MyLog.i("LoginActivity", "2=" + SharedPreferencesUtils.getIntance(LoginActivity.this).getprepayset(2));
//
//                MyLog.i("LoginActivity", "1=" + SharedPreferencesUtils.getIntance(LoginActivity.this).getprint_sign1());
//                MyLog.i("LoginActivity", "2=" + SharedPreferencesUtils.getIntance(LoginActivity.this).getprint_sign2());
//
//                MyLog.i("LoginActivity", "milserver=" + info.getLogontime());
//                MyLog.i("LoginActivity", "milnow=" + System.currentTimeMillis());
//                MyLog.i("LoginActivity", "mil=" + SharedPreferencesUtils.getIntance(LoginActivity.this).getlogontime());

                putStringToPreference("name", info.getName());
                token = info.getToken();
                useraccount = username;
                comid = info.getComid();
                parkname = info.getCname();
                //跳转到主界面
                Intent intent = new Intent(LoginActivity.this, LeaveActivity.class);
                intent.putExtra("berths", berths);
                intent.putExtra("swipe", info.getSwipe());
                intent.putExtra("name", info.getName());
                intent.putExtra("authflag", info.getAuthflag());
                intent.putExtra("qrcode", info.getQr());
                if ("1".equals(info.getEtc()) && info.getWorksite() != null) {
                    ArrayList<WorksiteInfo> worksite = info.getWorksite();
                    worksite.add(new WorksiteInfo("-1", "离岗"));
                    intent.putExtra("worksite", worksite);
                }
                startActivity(intent);
                finish();
            } else if (info.getState() != null && info.getState().equals("1")) {// 禁用账户
                Toast.makeText(LoginActivity.this, "您的账户异常，请联系停车宝！", Toast.LENGTH_SHORT).show();
            } else if (info.getState() != null && info.getState().equals("5")) {// 无价值账户
                Toast.makeText(LoginActivity.this, "您的账户异常，请联系停车宝！", Toast.LENGTH_SHORT).show();
            } else if (info.getState() != null && DataTypeChange.toInt(info.getState()) > 1) {// 待审核用户大于1不等于5；
                saveAccountAndPwd(username, password, info.getRole(), info.getIscancel());
                Intent intent = new Intent(LoginActivity.this, RegisterAsDocumentPhotoActivity.class);
                intent.putExtra("mobile", info.getMobile());
                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(LoginActivity.this, info.getInfo(), Toast.LENGTH_SHORT).show();
        }
    }

    public void saveAccountAndPwd(String username, String password, String role, String iscancle) {
        // 自动登录；
        SharedPreferences autologin = getSharedPreferences("autologin", Context.MODE_PRIVATE);
        Editor autoedit = autologin.edit();
        autoedit.putString("account", username);
        autoedit.putString("passwd", password);
        autoedit.putString("role", role == null ? "2" : role);// 默认收费员
        autoedit.putString("iscancle", iscancle == null ? "0" : iscancle);// 默认存在取消按钮
        autoedit.commit();
    }

    // 获取当前应用程序的版本号
    private String getVersions() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), Toast.LENGTH_SHORT);
            return String.valueOf(info.versionName);
//            return String.valueOf(info.versionCode);
        } catch (Exception e) {
            e.printStackTrace();
            return "no_version";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("tingchebao", Context.MODE_PRIVATE);
        final String mobile = sp.getString("regMobile", null);
        if (mobile != null) {
            bt_register.setText("补充注册资料");
            bt_register.setTextColor(getResources().getColor(R.color.red));
        } else {
            bt_register.setText(getResources().getString(R.string.regist));
            bt_register.setTextColor(getResources().getColor(R.color.tv_leaveItem_state_green));
        }
        bt_register.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mobile != null) {
                    Intent intent = new Intent(LoginActivity.this, RegisterAsDocumentPhotoActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(LoginActivity.this, RegisterAsMobileActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (CommontUtils.Is910()) {
                    return true;
                } else {
                    AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
                    mDialog.setTitle("操作提示");
                    mDialog.setMessage("您要退出应用吗？");
                    mDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            LoginActivity.this.finish();
                            MyApplication.getInstance().exit();
                        }
                    });
                    mDialog.setNegativeButton("取消", null);
                    mDialog.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // 返回键退出时提示
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (CommontUtils.Is910()) {
                return true;
            } else {
                AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
                mDialog.setTitle("操作提示");
                mDialog.setMessage("您要退出应用吗？");
                mDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        LoginActivity.this.finish();
                        MyApplication.getInstance().exit();
                    }
                });
                mDialog.setNegativeButton("取消", null);
                mDialog.show();
            }

        }
        return super.onKeyDown(keyCode, event);

    }
}
