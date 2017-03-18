package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.umeng.analytics.MobclickAgent;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.MyAccountInfo;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

/**
 * 我的个人资料;
 */
@SuppressLint("HandlerLeak")
public class MyHomeActivity extends BaseActivity {
    private RelativeLayout rl_name;
    private RelativeLayout rl_moblie;
    private RelativeLayout rl_password;
    private RelativeLayout rl_role;
    private TextView tv_name;
    private TextView tv_mobile;
    private TextView tv_role;
    private TextView tv_account;
    private String username;
    private String account;
    private boolean change_ok = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.my_home_activity_main);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        initView();
    }

    private void initView() {
        rl_name = (RelativeLayout) findViewById(R.id.rl_my_home_name);
        rl_role = (RelativeLayout) findViewById(R.id.rl_my_home_role);
        rl_moblie = (RelativeLayout) findViewById(R.id.rl_my_home_mobile);
        rl_password = (RelativeLayout) findViewById(R.id.rl_my_home_password);
        tv_name = (TextView) findViewById(R.id.tv_my_houme_name);
        tv_mobile = (TextView) findViewById(R.id.tv_my_home_mobile);
        tv_role = (TextView) findViewById(R.id.tv_my_home_role);
        tv_account = (TextView) findViewById(R.id.tv_my_home_accont);
    }

    @SuppressLint("NewApi")
    public void setView(final MyAccountInfo info) {
        if (info.getName() == null || info.getName().equals("")) {
            tv_name.setTextColor(Color.RED);
            tv_name.setText(getString(R.string.please_name));
        } else {
            username = info.getName();
            tv_name.setText(username);
            tv_name.setTextColor(Color.BLACK);
        }
        rl_name.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-修改姓名
                Intent intent = new Intent(MyHomeActivity.this, ChangeMyInfoActivity.class);
                intent.putExtra("changeinfo", "name");
                startActivityForResult(intent, 100);
            }
        });
        if (info.getMobile() == null || info.getMobile().equals("")) {
            tv_mobile.setText(getString(R.string.please_mobile));
            tv_mobile.setTextColor(Color.RED);
        } else {
            tv_mobile.setText(info.getMobile());
            tv_mobile.setTextColor(Color.BLACK);
        }
        rl_moblie.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-修改手机号
                Intent intent = new Intent(MyHomeActivity.this, ChangeMyInfoActivity.class);
                intent.putExtra("changeinfo", "mobile");
                intent.putExtra("phonenum", info.getMobile());
                startActivity(intent);
            }
        });
        if (info.getRole() != null && info.getUin() != null) {
            tv_role.setText(info.getRole());
            tv_account.setText(info.getUin());
            account = info.getUin();
        }
        if (info.getUin() != null) {
            rl_password.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto- 修改密码
                    Intent intent = new Intent(MyHomeActivity.this,
                            ChangePassWordActivity.class);
                    intent.putExtra("uin", info.getUin());
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        //可以根据多个请求代码来作相应的操作
        if (20 == resultCode) {
            String change_ok = data.getExtras().getString("myinfo");
            if (change_ok.equals("ok")) {
                LeaveActivity.change_name = true;
                MyHomeActivity.this.finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // http://127.0.0.1/zld/collectorrequest.do?action=myinfo&token=747077c9c5456091217f16b36a50403f
    // {"name":"赵威","uin":"10414","role":"管理员","mobile":"18710233083"}
    public void getAccountInfo() {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "我的信息获取失败，请检查网络！", 0).show();
            return;
        }
        String path = baseurl;
        String url = path + "collectorrequest.do?action=myinfo&token="
                + token;
        MyLog.w("我的信息--URL---->>>", url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...",
                "获取我的信息...", true, true);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                super.callback(url, object, status);
                MyLog.v("获取的我的信息内容是---->>>", object);
                if (object != null && object != "") {
                    dialog.dismiss();
                    Gson gson = new Gson();
                    MyAccountInfo info = gson.fromJson(object,
                            MyAccountInfo.class);
                    MyLog.i("解析到我的信息内容是---->>>", info.toString());
                    if (info != null) {
                        setView(info);
                    }
                } else {
                    dialog.dismiss();
                }
            }
        });
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    // actionBar的点击回调方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                MyHomeActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        getAccountInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
