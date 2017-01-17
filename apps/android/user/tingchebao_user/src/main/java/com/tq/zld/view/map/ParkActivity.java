package com.tq.zld.view.map;

import java.math.BigDecimal;
import java.util.HashMap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.ParkInfo;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.view.fragment.EmptyPageFragment;
import com.tq.zld.view.fragment.ParkFragment;

public class ParkActivity extends BaseActivity implements Listener<ParkInfo>,
        ErrorListener {

    public static final String ARG_NAME = "park_name";
    public static final String ARG_ID = "park_id";
    private ParkInfo mPark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park);
        initData();
        initToolbar();
        initView();
        getParkInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_park, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void initView() {
        findViewById(R.id.btn_park_navi).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (mPark.lat == null || mPark.lng == null) {
                            return;
                        }
                        launchNavigator(TCBApp.mLocation, new double[]{
                                        new BigDecimal(mPark.lat).doubleValue(),
                                        new BigDecimal(mPark.lng).doubleValue()},
                                mPark.name);
                    }
                });
    }

    private void setFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction()
                .replace(R.id.park_content, fragment,
                        fragment.getClass().getSimpleName());
        if (addToBackStack) {
            ft.addToBackStack(fragment.getClass().getSimpleName());
        }
        ft.commitAllowingStateLoss();
    }

    /**
     * 初始化停车场基本信息
     */
    private void initData() {
        Intent intent = getIntent();
        mPark = new ParkInfo();
        mPark.id = intent.getStringExtra(ARG_ID);
        mPark.name = intent.getStringExtra(ARG_NAME);
    }

    private void initToolbar() {
        Toolbar bar = (Toolbar) findViewById(R.id.widget_toolbar);
        bar.setTitle(mPark.name);
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bar.setNavigationOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        bar.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_park_call && mPark != null
                        && !TextUtils.isEmpty(mPark.phone)) {
                    Uri uri = Uri.parse("tel:" + mPark.phone);
                    ParkActivity.this.startActivity(new Intent(
                            Intent.ACTION_DIAL, uri));
                    return true;
                } else {
                    Toast.makeText(ParkActivity.this, "车场未提供电话！",
                            Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }

    /**
     * 从网络上获取停车场详情信息
     */
    private void getParkInfo() {
        // http://s.zhenlaidian.com/zld/carinter.do?action=parkinfo&comid=3
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "parkinfo");
        params.put("comid", mPark.id);
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "carinter.do", params);
        GsonRequest<ParkInfo> request = new GsonRequest<>(url,
                ParkInfo.class, this, this);
        TCBApp.getAppContext().addToRequestQueue(request, this);

    }

    /**
     * 开启导航
     *
     * @param currPoint "我的位置"
     * @param parkPoint 停车场位置(目的地)
     * @param endName   目的地名称
     */
    private void launchNavigator(LatLng currPoint, final double[] parkPoint,
                                 String endName) {
        if (parkPoint == null) {
            Toast.makeText(ParkActivity.this, "该停车场已停止服务，暂不支持导航！敬请谅解~",
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (currPoint == null) {
            Toast.makeText(ParkActivity.this, "暂未获取到当前位置，请稍后再试！",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (!TCBApp.mIsEngineInitSuccess) {
            Toast.makeText(this, "导航引擎准备中，请稍候...", Toast.LENGTH_SHORT).show();
            return;
        }
        BNaviPoint startPoint = new BNaviPoint(currPoint.longitude,
                currPoint.latitude, "我的位置", BNaviPoint.CoordinateType.BD09_MC);
        BNaviPoint endPoint = new BNaviPoint(parkPoint[1], parkPoint[0],
                endName, BNaviPoint.CoordinateType.BD09_MC);
        LogUtils.i(ParkActivity.class, "起始点：" + startPoint.getLatitude()
                + startPoint.getLongitude());
        LogUtils.i(ParkActivity.class, "终点：" + endPoint.getLatitude()
                + endPoint.getLongitude());
        BaiduNaviManager.getInstance().launchNavigator(this,
                startPoint, // 起点（可指定坐标系）
                endPoint, // 终点（可指定坐标系）
                NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME,// 算路方式
                true, // 真实导航
                BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY,
                new OnStartNavigationListener() { // 跳转监听

                    @Override
                    public void onJumpToNavigator(Bundle configParams) {
                        Intent intent = new Intent(ParkActivity.this,
                                BNavigatorActivity.class);
                        intent.putExtras(configParams);
                        intent.putExtra(BNavigatorActivity.ARG_FROM_WHERE,
                                BNavigatorActivity.VALUE_FROM_PARKDETAIL);
                        intent.putExtra(BNavigatorActivity.ARG_ENDPOINT,
                                parkPoint);
                        startActivity(intent);
                    }

                    @Override
                    public void onJumpToDownloader() {
                    }
                });
    }

    @Override
    public void onErrorResponse(VolleyError arg0) {
        EmptyPageFragment fragment = new EmptyPageFragment();
        setFragment(fragment, false);
        fragment.showEmptyView(getString(R.string.err_msg_network_error), 0,
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        getParkInfo();
                    }
                });
    }

    @Override
    public void onResponse(ParkInfo park) {

        if (!TextUtils.isEmpty(mPark.id)) {
            mPark = park;
            ParkFragment parkFragment = new ParkFragment();
            Bundle args = new Bundle();
            args.putParcelable(ParkFragment.ARG_PARK, mPark);
            parkFragment.setArguments(args);
            setFragment(parkFragment, false);

            // 如果车场未提供电话，则不显示拨号按钮
            // if(TextUtils.isEmpty(mPark.phone)){
            // }
        } else {
            EmptyPageFragment fragment = new EmptyPageFragment();
            setFragment(fragment, false);
            fragment.showEmptyView("车场数据异常~", 0, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TCBApp.getAppContext().cancelPendingRequests(this);
    }
}
