package com.tq.zld.view;

import java.math.BigDecimal;
import java.math.RoundingMode;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.ParkInfo;
import com.tq.zld.util.DensityUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.fragment.AuditFragment;
import com.tq.zld.view.fragment.UploadFragment;

public class UGCActivity extends BaseActivity implements
        OnMapStatusChangeListener, OnGetGeoCoderResultListener {

    private Toolbar mBar;
    private MapView mMapView;
    private BaiduMap mMap;
    private TextView tvAddr;
    private TextView tvDist;
    private TextView tvTips;
    private ImageView ivLandmarker;
    private GeoCoder mSearch;
    private ReverseGeoCodeOption reverseGeoCodeOption;
    private Marker mParkMarker;
    private boolean mCanUpdateMap = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ugc);
        initView();
        setDefaultFragment();
    }

    private void setDefaultFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.ugc_content, new UploadFragment(), UploadFragment.class.getSimpleName()).commit();
    }

    private void initView() {

        // 初始化Toolbar
        mBar = (Toolbar) findViewById(R.id.widget_toolbar);
        mBar.setBackgroundColor(getResources().getColor(R.color.bg_toolbar));
        mBar.setTitle("上传停车场");
        setSupportActionBar(mBar);
        mBar.setNavigationOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvAddr = (TextView) findViewById(R.id.tv_ugc_map_addr);
        tvDist = (TextView) findViewById(R.id.tv_ugc_map_distance);
        ivLandmarker = (ImageView) findViewById(R.id.iv_ugc_map_landmarker);
        tvTips = (TextView) findViewById(R.id.tv_ugc_map_tips);

        // 初始化地图控件
        mMapView = (MapView) findViewById(R.id.ugc_mapview);
        mMapView.showZoomControls(false);
        mMapView.showScaleControl(false);
        mMap = mMapView.getMap();
        mMap.setMyLocationEnabled(false);
        mMap.setTrafficEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.setMaxAndMinZoomLevel(20f, 16f);
        mMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                mMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(
                        TCBApp.mLocation, 20F));
            }
        });
        mMap.setOnMapStatusChangeListener(this);
        mSearch = GeoCoder.newInstance();
        reverseGeoCodeOption = new ReverseGeoCodeOption();
        mSearch.reverseGeoCode(reverseGeoCodeOption
                .location(TCBApp.mLocation));
        mSearch.setOnGetGeoCodeResultListener(this);
    }

    public String getAddress() {
        return tvAddr.getText().toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSearch != null) {
            mSearch.destroy();
        }
        mMapView.onDestroy();
    }

    public BaiduMap getMap() {
        return mMap;
    }

    @Override
    public void onMapStatusChange(MapStatus arg0) {
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus newStatus) {

        Fragment auditFragment = getSupportFragmentManager().findFragmentByTag(AuditFragment.class.getSimpleName());

        LogUtils.i(getClass(), "MapStatusChangeFinish: --->> " + auditFragment + "," + isCanUpdateMap());

        if (auditFragment != null && !auditFragment.isDetached() && !isCanUpdateMap()) {
            return;
        }

        // 逆地址编码
        if (reverseGeoCodeOption != null) {
            mSearch.reverseGeoCode(reverseGeoCodeOption
                    .location(newStatus.target));
        }

        // 计算距离
        if (View.VISIBLE == tvDist.getVisibility()) {
            double distance = DistanceUtil.getDistance(TCBApp.mLocation,
                    newStatus.target);
            if (distance != -1) {
                // new Formatter().format("%6.0fm", distance).toString()
                tvDist.setText(new BigDecimal(distance).setScale(0,
                        RoundingMode.HALF_UP).toString()
                        + "m");
            }
        }

        if (auditFragment != null) {
            setCanUpdateMap(false);
        }
    }

    @Override
    public void onMapStatusChangeStart(MapStatus arg0) {
        if (View.VISIBLE == tvTips.getVisibility()
                && tvTips.getText().toString().contains("地图")) {
            tvTips.setVisibility(View.GONE);
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            // 没有检索到结果
        }
        // 获取地理编码结果
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            // 没有找到检索结果
            return;
        }
        // 获取反向地理编码结果
        tvAddr.setText(result.getAddress());
    }

    /**
     * 设置地图模式：上传车场或审核车场
     *
     * @param park 为null,表示上传车场，否则表示审核车场
     */
    public void setMapMode(ParkInfo park) {
        if (park != null) {
            ivLandmarker.setVisibility(View.GONE);
            tvTips.setVisibility(View.GONE);
            LatLng position = new LatLng(new BigDecimal(park.lat).doubleValue(),
                    new BigDecimal(park.lng).doubleValue());
            mMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(position,
                    20.0f));
            if (mParkMarker == null) {
                MarkerOptions options = new MarkerOptions()
                        .position(position)
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.ic_marker_nomal_green_clicked))
                        .visible(true).zIndex(1).flat(false);
                mParkMarker = (Marker) mMap.addOverlay(options);
            } else {
                mParkMarker.setPosition(position);
            }

            showInfoWindow(park.name, position);
        } else {
            setCanUpdateMap(true);
            ivLandmarker.setVisibility(View.VISIBLE);
            tvTips.setVisibility(View.VISIBLE);
            mMap.hideInfoWindow();
            if (mParkMarker != null) {
                mParkMarker.remove();
            }
            mMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(TCBApp.mLocation));
        }
    }

    private void showInfoWindow(String string, LatLng position) {
        TextView tv = new TextView(this);
        tv.setBackgroundResource(R.drawable.bg_infowindow_normal);
        tv.setGravity(Gravity.CENTER);
        tv.setText(string);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        // tv.setTextColor(Color.rgb(0x5F, 0x75, 0xDA));

        InfoWindow infoWindow = new InfoWindow(tv, position,
                DensityUtils.dip2px(getApplicationContext(), -22));
        mMap.showInfoWindow(infoWindow);
    }

    public boolean isCanUpdateMap() {
        return mCanUpdateMap;
    }

    public void setCanUpdateMap(boolean can) {
        this.mCanUpdateMap = can;
    }

    public Toolbar getToolbar() {
        return mBar;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
}
