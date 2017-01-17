package com.tq.zld.view.account;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.adapter.MyItemAdapter;
import com.tq.zld.bean.MyItemInfo;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.NetWorkUtils;
import com.tq.zld.view.BaseActivity;

/**
 * 我的包月产品（已购买的） 显示包月产品名称，有效期。剩余天数；
 *
 * @author zhangyunfei
 */
public class BoughtProductActivity extends BaseActivity {

    private ListView lv_my_item;
    private TextView tv_page_null;
    private View rl_page_null;
    private MyItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_item);
        initToolbar();
        lv_my_item = (ListView) findViewById(R.id.lv_my_item);
        rl_page_null = findViewById(R.id.rl_page_null);
        tv_page_null = (TextView) findViewById(R.id.tv_page_null);
        adapter = new MyItemAdapter(this);
        getInfos();
    }

    private void initToolbar() {
        Toolbar bar = (Toolbar) findViewById(R.id.widget_toolbar);
        bar.setBackgroundColor(getResources().getColor(R.color.bg_toolbar));
        bar.setTitle("我的月卡");
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bar.setNavigationOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void setAdapter() {
        lv_my_item.setAdapter(adapter);
    }

    public void getInfos() {
        // http://192.168.1.106/zld/carowner.do?action=products&mobile=15801482643
        String url = TCBApp.mServerUrl + "carowner.do?action=products&mobile="
                + TCBApp.mMobile;
        if (NetWorkUtils.IsHaveInternet(this)) {
            AQuery aQuery = new AQuery(this);
            LogUtils.i(getClass(), "请求我的包月卡的URL-->>>" + url);
            final ProgressDialog dialog = ProgressDialog.show(this, "",
                    "请稍候...", true, true);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object,
                                     AjaxStatus status) {
                    LogUtils.i(BoughtProductActivity.class,
                            "getMyItem result: --->> " + object);
                    dialog.dismiss();
                    if (object != null) {
                        try {
                            Gson gson = new Gson();
                            ArrayList<MyItemInfo> infos = gson.fromJson(object,
                                    new TypeToken<ArrayList<MyItemInfo>>() {
                                    }.getType());
                            if (infos != null && infos.size() != 0
                                    && infos.get(0).getName() != null
                                    && infos.get(0).getName() != "") {
                                adapter.setinfos(infos,
                                        BoughtProductActivity.this);
                            } else {
                                tv_page_null.setText("您还未买过任何包月产品！赶紧购买一个吧~");
                                rl_page_null.setVisibility(View.VISIBLE);
                                lv_my_item.setVisibility(View.INVISIBLE);
                            }
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }
}
