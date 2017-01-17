package com.tq.zld.view.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Response;
import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.adapter.TicketAdapter;
import com.tq.zld.bean.Coupon;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.view.fragment.ChatFragment;
import com.tq.zld.view.fragment.SelectTicketMergeFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 选择券，来合体
 *
 * Created by GT on 2015/9/11.
 *
 * @see com.tq.zld.view.fragment.SelectTicketMergeFragment
 */
public class SelectTicketMergeActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initToolbar();
        SelectTicketMergeFragment fragment = new SelectTicketMergeFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment,
                        fragment.getClass().getSimpleName()).commit();
    }

    private void initToolbar() {
        Toolbar bar = (Toolbar) findViewById(R.id.widget_toolbar);
        bar.setTitle("选择停车券");
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtils.i("onNewIntent");
    }
}
