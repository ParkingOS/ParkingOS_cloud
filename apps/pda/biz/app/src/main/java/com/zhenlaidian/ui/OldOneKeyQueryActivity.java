package com.zhenlaidian.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.OldOneKeyQueryInfo;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

/**
 * 第一版主页一检查询;
 */
public class OldOneKeyQueryActivity extends BaseActivity {

    private TextView tv_in_car;
    private TextView tv_out_car;
    private TextView tv_total_money;
    private TextView tv_date_null;
    private TextView tv_all_in_car;
    private RelativeLayout rl_one_key_query;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        setContentView(R.layout.one_key_query_activity);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        initView();
        getOneQuery();
    }

    public void onekeyonclicklistening(View view) {
        Intent intent = new Intent(OldOneKeyQueryActivity.this, CurrentOrderActivity.class);
        startActivity(intent);
        OldOneKeyQueryActivity.this.finish();
    }

    // actionBar的点击回调方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                OldOneKeyQueryActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView() {

        tv_in_car = (TextView) findViewById(R.id.tv_query_inPark_car_count);
        tv_out_car = (TextView) findViewById(R.id.tv_query_outPark_car_count);
        tv_total_money = (TextView) findViewById(R.id.tv_query_total_money);
        tv_date_null = (TextView) findViewById(R.id.tv_query_null);
        tv_all_in_car = (TextView) findViewById(R.id.tv_query_accessPark_car_count);
        rl_one_key_query = (RelativeLayout) findViewById(R.id.rl_location_bottom);
    }

    public void setView(OldOneKeyQueryInfo info) {
        if (info.getCcount() != null) {
            tv_in_car.setText(info.getCcount());
        }
        if (info.getOcount() != null) {
            tv_out_car.setText(info.getOcount());
        }
        if (info.getTotal() != null) {
            tv_total_money.setText(info.getTotal());
        }
        if (info.getTcount() != null) {
            tv_all_in_car.setText(info.getTcount());
        } else {
            tv_all_in_car.setText("0");
        }

    }

    public void setNullView() {
        tv_date_null.setVisibility(View.VISIBLE);
        rl_one_key_query.setVisibility(View.INVISIBLE);
    }

    // http://127.0.0.1/zld/collectorrequest.do?action=corder&token=73de6dcf6987a6c6eb9c28bb8401ef25
    // {"ccount":"8","ocount":"11","total":"177.75"}
    public void getOneQuery() {
        String path = baseurl;
        String url = path + "collectorrequest.do?action=corder&token=" + token;
        MyLog.w("OneKeyQueryActivity", "获取一键查询的URL--->" + url);
        AQuery aQuery = new AQuery(this);
        final ProgressDialog dialog = ProgressDialog.show(this, "一键查询...", "查询中...", true, true);
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "请检查网络", 0).show();
            return;
        }
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                // TODO Auto-generated method stub
                super.callback(url, object, status);
                if (object != null && object != "") {
                    dialog.dismiss();
                    MyLog.v("OneKeyQueryActivity", "获取到的一键查询结果是--->" + object);
                    Gson gson = new Gson();
                    OldOneKeyQueryInfo info = gson.fromJson(object, OldOneKeyQueryInfo.class);
                    MyLog.i("OneKeyQueryActivity", "解析到的一键查询结果是--->" + info.toString());
                    setView(info);
                } else {
                    dialog.dismiss();
                    setNullView();
                }
            }
        });
    }

}
