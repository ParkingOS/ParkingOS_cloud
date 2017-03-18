package com.zhenlaidian.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.bean.GetPasswordInfo;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

/**
 * 联系客服;
 */
public class GetPasswordActivity extends BaseActivity {

    private Button bt_find;
    private EditText et_account;
    private TextView tv_info;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.get_password_activity);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
                ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        initVeiw();
        setView();
    }

    public void initVeiw() {
        bt_find = (Button) findViewById(R.id.bt_getpassword_ok);
        tv_info = (TextView) findViewById(R.id.tv_getpassword_info);
        et_account = (EditText) findViewById(R.id.et_getpassword_account);
    }

    public void setView() {
        bt_find.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//				if (et_account.getText() == null && et_account.getText().toString().equals("")) {
//					Toast.makeText(GetPasswordActivity.this, "账号不能为空", 0).show();
//				}else {
//					username = et_account.getText().toString().trim();
//					getPassword();
//				}
                tall();
            }
        });
    }

    public void tall() {
        Intent phoneintent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + "01056450508"));
        startActivity(phoneintent);
    }

    //
    public void getPassword() {

        String path = Config.getUrl(this);
        String url = path + "collectorlogin.do?username=" + username + "&action=forpass";
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "请检查网络", 0).show();
            return;
        }
        MyLog.w("RankingActivity", "获取密码的URL--->" + url);
        AQuery aQuery = new AQuery(this);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取我的密码...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (object != null && object != "") {
                    MyLog.i("RankingActivity", "获取到的密码信息为--->" + object.toString());
                    dialog.dismiss();
                    Gson gson = new Gson();
                    GetPasswordInfo info = gson.fromJson(object, GetPasswordInfo.class);
                    tv_info.setText(info.getInfo());
                } else {
                    dialog.dismiss();
                }
            }

        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                GetPasswordActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
