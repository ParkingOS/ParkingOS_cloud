package com.zhenlaidian.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.zhenlaidian.R;
import com.zhenlaidian.util.SharedPreferencesHandler;

import java.util.HashSet;

/**
 * 注册成功后的页面;
 */
public class RegisterSuccessActivity extends Activity {

    private String uin;
    private String password;
    private TextView tv_uin;
    private TextView tv_password;
    private Button bt_ok;
    private HashSet<String> users = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_success_activity);
        SharedPreferences autologin = getSharedPreferences("autologin", Context.MODE_PRIVATE);
        uin = autologin.getString("account", "");
        password = autologin.getString("password", "");
        tv_uin = (TextView) findViewById(R.id.tv_register_success_uin);
        tv_password = (TextView) findViewById(R.id.tv_register_success_password);
        bt_ok = (Button) findViewById(R.id.bt_register_success_ok);
        tv_uin.setText(uin);
        tv_password.setText(password);
        SharedPreferences sp1 = getSharedPreferences("usernames", Context.MODE_PRIVATE);
        Editor edit = sp1.edit();
        users.add(uin);
        SharedPreferencesHandler.putStringSet(edit, "usernames", users).commit();
        bt_ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(RegisterSuccessActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
