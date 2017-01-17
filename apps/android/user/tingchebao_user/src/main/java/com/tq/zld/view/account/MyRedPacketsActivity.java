package com.tq.zld.view.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.adapter.RedpacketAdapter;
import com.tq.zld.bean.RedpacketsInfo;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.NetWorkUtils;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.view.map.ParkingRedPacketsActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MyRedPacketsActivity extends BaseActivity {

    private TextView mHintTextView;// 待领红包个数；
    private ListView mListView;// 我的停车卷列表；
    private RedpacketAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_redpackets);
        initToolbar();
        initView();
    }

    private void initToolbar() {
        Toolbar bar = (Toolbar) findViewById(R.id.widget_toolbar);
        bar.setTitle("我的红包");
        bar.setBackgroundColor(getResources().getColor(R.color.bg_toolbar));
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bar.setNavigationOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void initView() {
        mHintTextView = (TextView) findViewById(R.id.tv_my_redpackets_number);
        mListView = (ListView) findViewById(R.id.lv_my_redpackets);
        View emptyView = findViewById(R.id.rl_page_null);
        ((TextView) emptyView.findViewById(R.id.tv_page_null)).setText("暂无红包记录～");
        mListView.setEmptyView(emptyView);
        mAdapter = new RedpacketAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mHintTextView.setText("亲，领完礼包可以去我的停车券查看！");
                shareRedPackets(((RedpacketsInfo) mAdapter.getItem(position)).id);
            }
        });
    }

    public void shareRedPackets(String id) {
        Intent intent = new Intent(this, ParkingRedPacketsActivity.class);
        intent.putExtra(ParkingRedPacketsActivity.ARG_PID, id);
        startActivity(intent);
    }

    // 查看我的红包接口---->> carowner.do?action=bonusinfo&mobile=18001379797
    // 返回：{"id":"12","exptime":"1425916800","is_auth":"0"} is_auth :
    // 0:已过期，1未过期，可以领取，2，已领取
    public void getMyRedpackets() {
        String url = TCBApp.mServerUrl + "carowner.do?action=bonusinfo&mobile="
                + TCBApp.mMobile;
        LogUtils.i(getClass(), "getMyRedpackets url: --->> " + url);
        if (NetWorkUtils.IsHaveInternet(this)) {
            showProgressDialog("请稍候... ", true, false);
            AQuery aQuery = new AQuery(this);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {
                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    dismissProgressDialog();
                    LogUtils.i(getClass(), "getMyRedpackets result: --->> " + object);
                    if (!TextUtils.isEmpty(object)) {
                        try {
                            ArrayList<RedpacketsInfo> redPackets = new Gson().fromJson(object,
                                    new TypeToken<ArrayList<RedpacketsInfo>>() {
                                    }.getType());
                            if (redPackets != null && redPackets.size() > 0) {

                                // 排序
                                Collections.sort(redPackets, new Comparator<RedpacketsInfo>() {
                                    @Override
                                    public int compare(RedpacketsInfo lhs, RedpacketsInfo rhs) {
                                        if (lhs.state == 1 && rhs.state != 1) {
                                            return -1;
                                        } else if (lhs.state != 1 && rhs.state == 1) {
                                            return 1;
                                        } else {
                                            try {
                                                int id1 = Integer.parseInt(lhs.id);
                                                int id2 = Integer.parseInt(rhs.id);
                                                return id1 > id2 ? -1 : 1;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                return 0;
                                            }
                                        }
                                    }
                                });

                                // 设置HintTextView提示信息
                                int number = 0;
                                for (RedpacketsInfo redpacketsInfo : redPackets) {

                                    if (redpacketsInfo.state != 1) {
                                        break;
                                    }
                                    number++;
                                }
                                if (number == 0) {
                                    mHintTextView.setText("您没有未领取的礼包！");
                                } else {
                                    mHintTextView.setText(String.format("您还有%d个礼包未领取！", number));
                                }
                                mHintTextView.setVisibility(View.VISIBLE);
                            } else {
                                mHintTextView.setVisibility(View.GONE);
                            }
                            mAdapter.setData(redPackets);
                        } catch (Exception e) {
                            Toast.makeText(TCBApp.getAppContext(), "数据格式错误！", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(MyRedPacketsActivity.this,
                                "网络异常，请稍后再试...", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else

        {
            Toast.makeText(MyRedPacketsActivity.this, "当前没有网络连接",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        getMyRedpackets();
    }
}
