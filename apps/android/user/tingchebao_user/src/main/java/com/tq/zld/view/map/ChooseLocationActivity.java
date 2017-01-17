package com.tq.zld.view.map;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
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
import com.tq.zld.util.LogUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ChooseLocationActivity extends AppCompatActivity implements BaiduMap.OnMapStatusChangeListener, OnGetGeoCoderResultListener {

    private Toolbar mBar;
    private MapView mMapView;
    private BaiduMap mMap;
    private TextView tvAddr;
    private TextView tvDist;
    private TextView tvTips;
    private ImageView ivLandmarker;
    private GeoCoder mSearch;
    private ReverseGeoCodeOption reverseGeoCodeOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_location);
        initView();
    }

    private void initView() {

        // 初始化Toolbar
        mBar = (Toolbar) findViewById(R.id.widget_toolbar);
        mBar.setBackgroundColor(getResources().getColor(R.color.bg_toolbar));
        mBar.setTitle("选择位置");
        setSupportActionBar(mBar);
        mBar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvAddr = (TextView) findViewById(R.id.tv_choose_location_addr);
        tvDist = (TextView) findViewById(R.id.tv_choose_location_distance);
        ivLandmarker = (ImageView) findViewById(R.id.iv_choose_location_landmarker);
        tvTips = (TextView) findViewById(R.id.tv_choose_location_tips);
        tvTips.setText("拖动地图，选择停车位置");

        // 初始化地图控件
        mSearch = GeoCoder.newInstance();
        reverseGeoCodeOption = new ReverseGeoCodeOption();
        mSearch.setOnGetGeoCodeResultListener(this);
        mMapView = (MapView) findViewById(R.id.choose_location_map);
        mMapView.showZoomControls(false);
        mMapView.showScaleControl(false);
        mMap = mMapView.getMap();
        mMap.setMyLocationEnabled(false);
        mMap.setTrafficEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.setMaxAndMinZoomLevel(20f, 16f);
        mMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                String latitude = TCBApp.getAppContext().readString(R.string.sp_remark_latitude, "");
                String longitude = TCBApp.getAppContext().readString(R.string.sp_remark_longitude, "");
                LatLng target = TCBApp.mLocation;
                if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
                    target = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                }
                mSearch.reverseGeoCode(reverseGeoCodeOption.location(target));
                mMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(target, 20F));
            }
        });
        mMap.setOnMapStatusChangeListener(this);

        findViewById(R.id.btn_choose_location_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng target = mMap.getMapStatus().target;
                if (target == null) {
                    LogUtils.e(ChooseLocationActivity.class, "--->> map status target is null!!!");
                    return;
                }
                TCBApp.getAppContext().saveString(R.string.sp_remark_latitude, String.valueOf(target.latitude));
                TCBApp.getAppContext().saveString(R.string.sp_remark_longitude, String.valueOf(target.longitude));
                TCBApp.getAppContext().saveBooleanSync(R.string.sp_remark_located, true);
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {

        // 逆地址编码
        if (reverseGeoCodeOption != null) {
            mSearch.reverseGeoCode(reverseGeoCodeOption
                    .location(mapStatus.target));
        }

        // 计算距离
        if (View.VISIBLE == tvDist.getVisibility()) {
            double distance = DistanceUtil.getDistance(TCBApp.mLocation,
                    mapStatus.target);
            if (distance != -1) {
                // new Formatter().format("%6.0fm", distance).toString()
                tvDist.setText(new BigDecimal(distance).setScale(0,
                        RoundingMode.HALF_UP).toString()
                        + "m");
            }
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            // 没有找到检索结果
            return;
        }
        // 获取反向地理编码结果
        tvAddr.setText(reverseGeoCodeResult.getAddress());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (mSearch != null) {
            mSearch.destroy();
        }
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
