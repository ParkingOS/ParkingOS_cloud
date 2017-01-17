package com.tq.zld.view.map;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.util.LogUtils;

public class WalkingNaviActivity extends AppCompatActivity implements BaiduMap.OnMapLoadedCallback, BDLocationListener {

    private BaiduMap mMap;
    private Marker mRemarkMarker;
    private LocationClient mLocationClient;
    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking_navi);
        initToolbar();
        initMapView();
        startLocating();
    }

    private void initToolbar() {
        Toolbar bar = (Toolbar) findViewById(R.id.toolbar_walking_navi);
        bar.setTitle("");
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initMapView() {
        mMapView = (MapView) findViewById(R.id.map_walking_navi);
        mMapView.showZoomControls(false);
        mMapView.showScaleControl(false);
        mMap = mMapView.getMap();
        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(false);
        mMap.setBuildingsEnabled(false);
        // mBaiduMap.setMaxAndMinZoomLevel(19.0f, 9.0f);
        mMap.setOnMapLoadedCallback(this);
//        mMap.setOnMyLocationClickListener(this);
//        mMap.setOnMapClickListener(this);
        // mBaiduMap.setOnMapTouchListener(this);
//        mMap.setOnMapStatusChangeListener(this);
    }

    private void startLocating() {
        mLocationClient = new LocationClient(getApplicationContext());
        // 设置“我的位置”自定义图标
        // BitmapDescriptor mLocationMarker = BitmapDescriptorFactory
        // .fromResource(R.drawable.ic_my_location);
        // mMap.setMyLocationConfigeration(new MyLocationConfiguration(
        // MyLocationConfiguration.LocationMode.NORMAL, true,
        // mLocationMarker));
        mLocationClient.registerLocationListener(this);
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);// 设置是否需要地址信息
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setScanSpan(3000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setIgnoreKillProcess(false);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        mLocationClient.requestLocation();
    }

    @Override
    public void onMapLoaded() {
        addRemarkMarker();
        if (mRemarkMarker == null || mRemarkMarker.getPosition() == null) {
            return;
        }
        showWalkingRouteLine(TCBApp.mLocation, mRemarkMarker.getPosition());
    }

    /**
     * 添加停车标记Marker到地图上
     */
    public void addRemarkMarker() {

        // 如果停车标记已删除，则不添加到地图上
        if (TCBApp.getAppContext().readBoolean(R.string.sp_remark_delete, true)) {
            return;
        }

        LatLng position = TCBApp.mLocation;
        String lat = TCBApp.getAppContext().readString(R.string.sp_remark_latitude, "");
        String lng = TCBApp.getAppContext().readString(R.string.sp_remark_longitude, "");
        if (!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lng)) {
            position = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        }

        if (position == null) {
            LogUtils.e(getClass(), "--->> remark marker's position can't be null!!!");
            return;
        }

        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_remark);
        MarkerOptions options = new MarkerOptions().position(position)
                .icon(icon)
                .visible(true);
        mRemarkMarker = (Marker) mMap.addOverlay(options);
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {

        if (bdLocation == null || bdLocation.getAltitude() == 0
                || mMap == null)
            return;

        MyLocationData mLocationData = new MyLocationData.Builder()
                .latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude()).accuracy(0)
                .direction(bdLocation.getDirection())// 不需要方向信息
                .build();

        mMap.setMyLocationData(mLocationData);
    }

    private void showWalkingRouteLine(LatLng start, LatLng end) {
        final RoutePlanSearch search = RoutePlanSearch.newInstance();
        OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                search.destroy();
                WalkingRouteOverlay overlay = new WalkingRouteOverlay(mMap);

                WalkingRouteLine line = walkingRouteResult.getRouteLines().get(0);
                if (line == null) {
                    Toast.makeText(TCBApp.getAppContext(), "没有推荐路线！", Toast.LENGTH_SHORT).show();
                    return;
                }

                overlay.setData(line);
                overlay.addToMap();
                overlay.zoomToSpan();
            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
            }
        };
        search.setOnGetRoutePlanResultListener(listener);
        PlanNode stNode = PlanNode.withLocation(start);
        PlanNode enNode = PlanNode.withLocation(end);
        search.walkingSearch(new WalkingRoutePlanOption().from(stNode).to(enNode));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.stop();
            mLocationClient.unRegisterLocationListener(this);
        }
        mMapView.onDestroy();
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
