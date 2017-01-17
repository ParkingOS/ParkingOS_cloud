package com.tq.zld.view.manager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
import com.google.gson.Gson;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.Order;
import com.tq.zld.bean.ParkInfo;
import com.tq.zld.util.DensityUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.fragment.ChooseParkingFeeCollectorFragment;
import com.tq.zld.view.map.BNavigatorActivity;
import com.tq.zld.view.map.MapActivity;
import com.tq.zld.view.map.ParkActivity;
import com.tq.zld.view.map.RemarkActivity;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 地图上停车场图标Marker的管理类，单例
 *
 * @author Clare
 */
public class MarkerManager implements OnMarkerClickListener {

    private static final String EXTRA_PARK = "park";

    /**
     * 车场marker的title
     */
    private static final String MARKER_TITLE_PARK = "park";
    /**
     * 兴趣点marker的title
     */
    private static final String MARKER_TITLE_POI = "poi";
    /**
     * 停车标记marker的title
     */
    private static final String MARKER_TITLE_REMARK = "remark";
    /**
     * 兴趣点名称
     */
    private static final String POI_NAME = "poiName";

    /**
     * 地图显示模式：仅显示可支付车场
     */
    public static final int MODE_SHOW_PAYABLE = 1;
    /**
     * 地图显示模式：显示全部车场
     */
    public static final int MODE_SHOW_ALL = 0;

    /**
     * 当前地图显示模式：默认显示可支付车场
     */
    private int mShowingMode = MODE_SHOW_PAYABLE;

    private int mInfoWindowType = 0;

    /**
     * 没有任何显示的InfoWindow
     */
    public static final int INFOWINDOW_NULL = -2;

    /**
     * 其他类型的InfoWindow
     */
    public static final int INFOWINDOW_OTHER = -1;

    /**
     * 普通停车场Marker的InfoWindow
     */
    public static final int INFOWINDOW_PARK = 0;
    /**
     * "我的位置"拍照标记的InfoWindow
     */
    public static final int INFOWINDOW_REMARK_CAMERA = 1;
    /**
     * 停车标记位置的InfoWindow
     */
    public static final int INFOWINDOW_REMARK_LOCATION = 2;

    private ArrayList<Marker> mMarkers;
    private Marker mClickedMarker;
    private Marker mRemarkMarker;
    private Marker mPOIMarker;
    private static MarkerManager instance;
    private BaiduMap mMap;// 地图对象
    private MapActivity mActivity;

    private ExecutorService mExecutorService;

    private MarkerManager(BaiduMap baiduMap, MapActivity activity) {
        mMarkers = new ArrayList<>();
        this.mMap = baiduMap;
        this.mActivity = activity;
        mExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public static MarkerManager getInstance(BaiduMap baiduMap,
                                            MapActivity activity) {
        if (instance == null) {
            synchronized (MarkerManager.class) {
                if (instance == null) {
                    instance = new MarkerManager(baiduMap, activity);
                }
            }
        }
        return instance;
    }

    /**
     * 如果退出，MapActivity没有杀进程，会导致，覆盖物管理者，还是上一次的引用，导致无效。
     */
    public static void onDestory() {
        instance = null;
    }

    /**
     * 获取当前显示的InfoWindow的类型
     *
     * @return
     */
    public int getInfoWindowType() {
        return mInfoWindowType;
    }

    /**
     * 获取停车标记Marker
     *
     * @return
     */
    public Marker getRemarkMarker() {
        return mRemarkMarker;
    }

    /**
     * 获取当前显示的模式：可支付车场还是全部车场
     *
     * @return
     */
    public int getShowingMode() {
        return mShowingMode;
    }

    /**
     * 添加POIMarker
     *
     * @param poiInfo
     */
    public void updatePOIMarker(PoiInfo poiInfo) {
        if (mPOIMarker == null) {
            Bundle extra = new Bundle();
            MarkerOptions options = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_poi))
                    .position(poiInfo.location)
                    .zIndex(Integer.MAX_VALUE)
                    .extraInfo(extra)
                    .visible(true);

            try {
                mPOIMarker = (Marker) mMap.addOverlay(options);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // mPOIMarker = new PoiOverlay(mMap) {
            // @Override
            // public boolean onPoiClick(int index) {
            // Toast.makeText(MapActivity.this,
            // getPoiResult().getAllPoi().get(index).name,
            // Toast.LENGTH_LONG).show();
            // return super.onPoiClick(index);
            // }
            // };
        }

        mPOIMarker.setTitle(MarkerManager.MARKER_TITLE_POI);
        mPOIMarker.getExtraInfo().putString(MarkerManager.POI_NAME, poiInfo.name);

        try {
            mPOIMarker.setPosition(poiInfo.location);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mPOIMarker.setToTop();
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

        if (mRemarkMarker == null) {
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_remark);
            MarkerOptions options = new MarkerOptions().position(position)
                    .icon(icon)
                    .title(MARKER_TITLE_REMARK)
                    .visible(true);
            try {
                mRemarkMarker = (Marker) mMap.addOverlay(options);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                mRemarkMarker.setPosition(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除停车标记Marker
     */
    public void removeRemarkMarker() {
        if (mRemarkMarker != null) {
            mRemarkMarker.remove();
            hideInfoWindow();
            mRemarkMarker = null;
        }
    }

    /**
     * 隐藏地图上的所有InfoWindow
     */
    public void hideInfoWindow() {
        try {
            mMap.hideInfoWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mInfoWindowType = INFOWINDOW_NULL;
    }

    public void showRemarkInfoWindow() {

        if (TCBApp.mLocation == null) {
            LogUtils.e(getClass(), "--->> 尚未定位成功！！！");
            return;
        }

        File dir = mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null) {
            LogUtils.e(MapActivity.class, "--->> Pictures dir is null!!!");
            return;
        }
        if (!dir.exists() && !dir.mkdirs()) {
            LogUtils.e(MapActivity.class, "--->> make Pictures dir failed!!!");
            return;
        }
        final File remarkImage = new File(dir, "remark.jpg");

        View infoWindowView = View.inflate(TCBApp.getAppContext(), R.layout.infowindow_remark, null);

        // 判断是否已有停车标记
        String text;
        if (TCBApp.getAppContext().readBoolean(R.string.sp_remark_delete, true)) {
            text = "停车之后，拍一张 <br /><small>回头找车，快一点</small>";
        } else {
            text = " 拍照记录新位置 <br /><small> 同时清空旧位置 </small>";
        }
        ((TextView) infoWindowView.findViewById(R.id.tv_infowindow_remark))
                .setText(Html.fromHtml(text));
        infoWindowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.takePhoto(remarkImage);
            }
        });

        InfoWindow infoWindow
                = new InfoWindow(infoWindowView, TCBApp.mLocation, DensityUtils.dip2px(TCBApp.getAppContext(), -8));
        try {
            mMap.showInfoWindow(infoWindow);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mInfoWindowType = INFOWINDOW_REMARK_CAMERA;
    }

    // 显示加载数据infoWindow
    public void showInfoWindow(String string) {
        TextView tv = new TextView(mActivity);
        tv.setBackgroundResource(R.drawable.bg_infowindow_normal);
        tv.setGravity(Gravity.CENTER);
        tv.setText(string);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        // tv.setTextColor(Color.rgb(0x5F, 0x75, 0xDA));

        InfoWindow infoWindow = new InfoWindow(tv, TCBApp.mLocation,
                DensityUtils.dip2px(TCBApp.getAppContext(), -8));
        try {
            mMap.showInfoWindow(infoWindow);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mInfoWindowType = INFOWINDOW_OTHER;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        // 如果当前marker不可见，则点击无效
        if (!marker.isVisible()) {
            return false;
        }

        String title = marker.getTitle();
        if (!TextUtils.isEmpty(title)) {

            switch (title) {
                case MARKER_TITLE_PARK:

                    // 点击的是停车场Marker
                    ParkInfo pInfo = marker.getExtraInfo().getParcelable(EXTRA_PARK);
                    showInfoWindow(pInfo);

                    // 当前点击的Marker已经处于选中状态时，则直接返回
                    if (marker == mClickedMarker) {
                        return true;
                    }

                    // String parkId = pInfo.id;
                    // updateClickedMarker(parkId);
                    marker.setToTop();
                    mClickedMarker = marker;
                    LogUtils.i(getClass(),
                            "marker infomation: --->> " + pInfo.toString());
                    return true;
                case MARKER_TITLE_POI:
                    // 点击的是POI
                    try {
                        mMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(
                                marker.getPosition(), MapActivity.MAP_ZOOM_DEFAULT));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(mActivity, marker.getExtraInfo().getString(POI_NAME), Toast.LENGTH_SHORT)
                            .show();
                    return true;
                case MARKER_TITLE_REMARK:

                    // 点击的是停车位置标记
                    showRemarkPositionInfoWindow();
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    public void showRemarkPositionInfoWindow() {
        if (getRemarkMarker() == null) {
            LogUtils.e(getClass(), "--->> remark marker is null!!!");
            return;
        }
        TextView tv = new TextView(mActivity);
        tv.setBackgroundResource(R.drawable.bg_infowindow_remark2);
        tv.setGravity(Gravity.CENTER);
        tv.setText("车停在这");
        tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_white, 0);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                showWalkingRouteLine(mMarkers.get(mMarkers.size() - 1).getPosition(), position);
                mActivity.startRemarkActivity();
            }
        });
        tv.setTextColor(Color.WHITE);

        InfoWindow infoWindow = new InfoWindow(tv, getRemarkMarker().getPosition(), DensityUtils.dip2px(mActivity, -22));
        try {
            mMap.showInfoWindow(infoWindow);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mInfoWindowType = INFOWINDOW_REMARK_LOCATION;
    }

    /**
     * 向地图中添加附近的停车场图标
     *
     * @param parks
     * @param suggestID 推荐的车场ID
     */
    public void addMarkers(final List<ParkInfo> parks, final String suggestID) {
        LogUtils.i(getClass(), "附近车场个数：--->> " + parks.size());
        if (parks == null || parks.size() == 0) {
            return;
        }
        mExecutorService.execute(new Runnable() {

            @Override
            public void run() {

                // 从地图上移除旧的Marker
                if (mMarkers != null && mMarkers.size() != 0) {
                    for (Marker marker : mMarkers) {
                        marker.remove();
                    }
                    mMarkers.clear();
                }

                // 添加新的Marker
                for (ParkInfo pInfo : parks) {
                    mMarkers.add(createMarker(pInfo));
                }

                // 突出显示推荐车场
                mActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (!TextUtils.isEmpty(suggestID)) {
                            ParkInfo pInfo;
                            for (Marker marker : mMarkers) {
                                pInfo = marker.getExtraInfo().getParcelable(
                                        EXTRA_PARK);
                                if (TextUtils.equals(suggestID, pInfo.id)) {
                                    LogUtils.i(MarkerManager.class,
                                            "推荐车场的id：--->> " + suggestID);
                                    onMarkerClick(marker);
                                    break;
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * 仅显示支持支付的停车场
     */
    public void showPayableMarker() {

        MarkerManager.this.mShowingMode = MODE_SHOW_PAYABLE;
        // 如果点击的是不可支付的车场，则此时应该隐藏infowindow
        if (!isClickedMarkerNull() && !"1".equals(getClickedMarkerExtra().epay)) {
            hideInfoWindow();
        }

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                ParkInfo pInfo;
                for (Marker marker : mMarkers) {
                    pInfo = marker.getExtraInfo().getParcelable(EXTRA_PARK);
                    if (pInfo != null && "1".equals(pInfo.epay)) {
                        marker.setVisible(true);
                    } else {
                        marker.setVisible(false);
                    }
                }
            }
        });
    }

    /**
     * 开启导航
     *
     * @param parkPoint 停车场位置(目的地)
     * @param endName   目的地名称
     */
    private void launchNavigator(final double[] parkPoint, String endName) {
        if (parkPoint == null) {
            Toast.makeText(TCBApp.getAppContext(), "该停车场已停止服务，暂不支持导航！敬请谅解~",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (TCBApp.mLocation == null) {
            Toast.makeText(TCBApp.getAppContext(), "暂未获取到当前位置，请稍后再试！",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (!TCBApp.mIsEngineInitSuccess) {
            Toast.makeText(TCBApp.getAppContext(), "导航准备中，请稍候...",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        BNaviPoint startPoint = new BNaviPoint(
                TCBApp.mLocation.longitude,
                TCBApp.mLocation.latitude, "我的位置",
                BNaviPoint.CoordinateType.BD09_MC);
        BNaviPoint endPoint = new BNaviPoint(parkPoint[1], parkPoint[0],
                endName, BNaviPoint.CoordinateType.BD09_MC);
        BaiduNaviManager.getInstance().launchNavigator(mActivity,
                startPoint, // 起点（可指定坐标系）
                endPoint, // 终点（可指定坐标系）
                NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME,// 算路方式
                true, // 真实导航
                BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY,
                new OnStartNavigationListener() { // 跳转监听

                    @Override
                    public void onJumpToNavigator(Bundle configParams) {
                        Intent intent = new Intent(TCBApp.getAppContext(),
                                BNavigatorActivity.class);
                        intent.putExtras(configParams);
                        intent.putExtra(BNavigatorActivity.ARG_FROM_WHERE,
                                BNavigatorActivity.VALUE_FROM_PARKDETAIL);
                        intent.putExtra(BNavigatorActivity.ARG_ENDPOINT,
                                parkPoint);
                        mActivity.startActivity(intent);
                    }

                    @Override
                    public void onJumpToDownloader() {
                    }
                });
    }

    /**
     * 显示全部车场
     */
    public void showAllMarker() {
        MarkerManager.this.mShowingMode = MODE_SHOW_ALL;
        mExecutorService.execute(new Runnable() {

            @Override
            public void run() {
                for (Marker marker : mMarkers) {
                    if (!marker.isVisible()) {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    /**
     * 更新当前选中的停车场图标
     */
    private void updateClickedMarker(final String parkId) {
        if (TextUtils.isEmpty(parkId) || mMarkers == null
                || mMarkers.size() == 0) {
            return;
        }

        mExecutorService.execute(new Runnable() {

            @Override
            public void run() {
                ParkInfo pInfo;
                BitmapDescriptor markerIcon;
                if (!isClickedMarkerNull()) {
                    mClickedMarker
                            .setIcon(genMarkerIcon((ParkInfo) mClickedMarker
                                    .getExtraInfo().getParcelable(EXTRA_PARK)));
                }

                for (Marker marker : mMarkers) {

                    pInfo = marker.getExtraInfo().getParcelable(EXTRA_PARK);
                    if (TextUtils.equals(pInfo.id, parkId)) {

                        markerIcon = getClickedMarkerIcon(pInfo);
                        // 更新Bundle数据
                        marker.setToTop();
                        marker.setIcon(markerIcon);
                        mClickedMarker = marker;
                        break;
                    }
                }
            }
        });
    }

    // 获取点击的marker图标
    private BitmapDescriptor getClickedMarkerIcon(ParkInfo pInfo) {
        int icon = setIconType(pInfo);
        int clickedIcon = R.drawable.ic_marker_nomal_green_clicked;
        switch (icon) {
            case R.drawable.ic_marker_payable_green:
                clickedIcon = R.drawable.ic_marker_payable_green_clicked;
                break;
            case R.drawable.ic_marker_payable_red:
                clickedIcon = R.drawable.ic_marker_payable_red_clicked;
                break;
            case R.drawable.ic_marker_nomal_green:
                clickedIcon = R.drawable.ic_marker_nomal_green_clicked;
                break;
            case R.drawable.ic_marker_nomal_red:
                clickedIcon = R.drawable.ic_marker_nomal_red_clicked;
                break;
            case R.drawable.ic_marker_free_green:
                clickedIcon = R.drawable.ic_marker_free_green_clicked;
                break;
            case R.drawable.ic_marker_free_red:
                clickedIcon = R.drawable.ic_marker_free_red_clicked;
                break;
        }
        return BitmapDescriptorFactory.fromResource(clickedIcon);
    }

    private boolean isClickedMarkerNull() {
        return mClickedMarker == null || mClickedMarker.getExtraInfo() == null;
    }

    /**
     * 在地图上创建停车场marker
     *
     * @param pInfo 封装了停车场信息的Bean
     * @return
     */
    private Marker createMarker(ParkInfo pInfo) {
        if (pInfo == null) {
            return null;
        }
        BitmapDescriptor bd = genMarkerIcon(pInfo);
        LatLng ll = new LatLng(new BigDecimal(pInfo.lat).doubleValue(),
                new BigDecimal(pInfo.lng).doubleValue());
        Bundle extraInfo = new Bundle();
        extraInfo.putParcelable(EXTRA_PARK, pInfo);
        MarkerOptions mo = new MarkerOptions().icon(bd).position(ll)
                .extraInfo(extraInfo).title(MARKER_TITLE_PARK);
        boolean show = MODE_SHOW_ALL == mShowingMode || "1".equals(pInfo.epay);
        mo.visible(show);
        // bd.recycle();
        Marker marker = null;
        try {
            marker = (Marker) mMap.addOverlay(mo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return marker;
    }

    private BitmapDescriptor mNormalMarkerGreen;
    private BitmapDescriptor mNormalMarkerRed;
    private BitmapDescriptor mFreeMarkerGreen;
    private BitmapDescriptor mFreeMarkerRed;
    private BitmapDescriptor mPayableMarkerGreen;
    private BitmapDescriptor mPayableMarkerRed;

    private BitmapDescriptor genMarkerIcon(ParkInfo park) {
        int icon = setIconType(park);
        switch (icon) {
            case R.drawable.ic_marker_nomal_green:
                if (mNormalMarkerGreen == null) {
                    mNormalMarkerGreen = BitmapDescriptorFactory.fromResource(icon);
                }
                return mNormalMarkerGreen;
            case R.drawable.ic_marker_nomal_red:
                if (mNormalMarkerRed == null) {
                    mNormalMarkerRed = BitmapDescriptorFactory.fromResource(icon);
                }
                return mNormalMarkerRed;
            case R.drawable.ic_marker_free_green:
                if (mFreeMarkerGreen == null) {
                    mFreeMarkerGreen = BitmapDescriptorFactory.fromResource(icon);
                }
                return mFreeMarkerGreen;
            case R.drawable.ic_marker_free_red:
                if (mFreeMarkerRed == null) {
                    mFreeMarkerRed = BitmapDescriptorFactory.fromResource(icon);
                }
                return mFreeMarkerRed;
            case R.drawable.ic_marker_payable_green:
                if (mPayableMarkerGreen == null) {
                    mPayableMarkerGreen = BitmapDescriptorFactory
                            .fromResource(icon);
                }
                return mPayableMarkerGreen;
            case R.drawable.ic_marker_payable_red:
                if (mPayableMarkerRed == null) {
                    mPayableMarkerRed = BitmapDescriptorFactory.fromResource(icon);
                }
                return mPayableMarkerRed;
        }
        return BitmapDescriptorFactory.fromResource(icon);
    }

    /**
     * 设置地图上停车场Marker的图标
     *
     * @param pInfo
     * @return
     */
    private int setIconType(ParkInfo pInfo) {
        if (pInfo == null || TextUtils.isEmpty(pInfo.id)) {
            return R.drawable.ic_marker_nomal_green_clicked;
        }

        if (!"-1".equals(pInfo.price)) {// 收费车场

            if ("1".equals(pInfo.epay)) {// 支持收费
                if (isParkSpaceLow(pInfo.free, pInfo.total)) {
                    return R.drawable.ic_marker_payable_red_clicked;
                }
                return R.drawable.ic_marker_payable_green_clicked;
            } else {
                if (isParkSpaceLow(pInfo.free, pInfo.total)) {
                    return R.drawable.ic_marker_nomal_red_clicked;
                }
                return R.drawable.ic_marker_nomal_green_clicked;
            }
        } else {// 免费车场
            if (isParkSpaceLow(pInfo.free, pInfo.total)) {
                return R.drawable.ic_marker_free_red_clicked;
            }
            return R.drawable.ic_marker_free_green_clicked;
        }
    }

    // 判断车位是否紧张
    private boolean isParkSpaceLow(String free, String total) {

        if (TextUtils.isEmpty(free) || TextUtils.isEmpty(total)) {
            return true;
        }
        if (!(TextUtils.isDigitsOnly(free) && TextUtils.isDigitsOnly(total))) {
            return true;
        }

        BigDecimal freeBD = new BigDecimal(free);
        BigDecimal totalBD = new BigDecimal(total);

        return freeBD.intValue() < 10
                || freeBD.divide(totalBD, 2, RoundingMode.HALF_EVEN)
                .floatValue() < 0.1;
    }

    private void getOrder(String parkID) {

        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            Toast.makeText(mActivity, "请先登录！", Toast.LENGTH_SHORT).show();
            mActivity.openDrawer();
            return;
        }

        // http://192.168.1.106/zld/carowner.do?action=currorder&mobile=15801482643
        String url = TCBApp.mServerUrl
                + "carowner.do?action=currentorder&mobile=" + TCBApp.mMobile
                + "&comid=" + parkID;
        LogUtils.i(getClass(), "currentOrder url: --->> " + url);
        final ProgressDialog dialog = ProgressDialog.show(mActivity, "",
                "请稍候...", false, true);
        dialog.setCanceledOnTouchOutside(false);
        new AQuery(mActivity).ajax(url, String.class,
                new AjaxCallback<String>() {

                    @Override
                    public void callback(String url, String object,
                                         AjaxStatus status) {
                        dialog.dismiss();
                        LogUtils.i(MarkerManager.class,
                                "currentOrder result: --->> " + object);
                        if (!TextUtils.isEmpty(object)) {
                            try {
                                Order mOrder = new Gson().fromJson(object,
                                        Order.class);
                                if (mOrder != null
                                        && !TextUtils.isEmpty(mOrder
                                        .getOrderid())) {
                                    switch (mOrder.getState()) {
                                        case Order.STATE_PAYING:
                                        case Order.STATE_PAY_FAILED:
                                            Intent intent = new Intent(TCBApp
                                                    .getAppContext(),
                                                    MapActivity.class);
                                            intent.putExtra("order", mOrder);
                                            mActivity.startActivity(intent);
                                            break;
                                        case Order.STATE_PENDING:
                                            Toast.makeText(mActivity,
                                                    "您有未结算的订单！请等待停车员结算~",
                                                    Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                } else {
                                    startPayFeeActivity();
                                }
                            } catch (Exception e) {
                                startPayFeeActivity();
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(mActivity, "网络异常！请稍后再试~",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void startPayFeeActivity() {
        ParkInfo park = getClickedMarkerExtra();
        if (park == null) {
            return;
        }
        Intent intent = new Intent(TCBApp.getAppContext(), MainActivity.class);
        intent.putExtra(MainActivity.ARG_FRAGMENT,
                MainActivity.FRAGMENT_CHOOSE_COLLECTOR);
        Bundle args = new Bundle();
        args.putString(ChooseParkingFeeCollectorFragment.ARG_PARK_ID, park.id);
        args.putString(ChooseParkingFeeCollectorFragment.ARG_PARK_NAME,
                park.name);
        intent.putExtra(MainActivity.ARG_FRAGMENT_ARGS, args);
        mActivity.startActivity(intent);
    }

    public ParkInfo getClickedMarkerExtra() {
        if (mClickedMarker == null) {
            return null;
        }
        return mClickedMarker.getExtraInfo().getParcelable(EXTRA_PARK);
    }

    private InfoWindowViewHolder mViewHolder;

    private void showInfoWindow(final ParkInfo park) {
        if (park == null) {
            return;
        }

        if (mViewHolder == null) {
            mViewHolder = new InfoWindowViewHolder();
            mViewHolder.infowindow = View.inflate(TCBApp.getAppContext(),
                    R.layout.dialog_infowindow, null);
            mViewHolder.parkView = mViewHolder.infowindow
                    .findViewById(R.id.infowindow_park);
            mViewHolder.tvName = (TextView) mViewHolder.infowindow
                    .findViewById(R.id.infowindow_name);
            mViewHolder.tvPrice = (TextView) mViewHolder.infowindow
                    .findViewById(R.id.infowindow_price);
            mViewHolder.tvFreeSpace = (TextView) mViewHolder.infowindow
                    .findViewById(R.id.infowindow_space);
            mViewHolder.btnNavi = (Button) mViewHolder.infowindow
                    .findViewById(R.id.infowindow_navi);
            mViewHolder.btnPay = (Button) mViewHolder.infowindow
                    .findViewById(R.id.infowindow_pay);
            mViewHolder.llPay = mViewHolder.infowindow
                    .findViewById(R.id.infowindow_pay_ll);
            mViewHolder.divider = mViewHolder.infowindow
                    .findViewById(R.id.infowindow_divider);
        }

        final double lat = Double.parseDouble(park.lat);
        final double lng = Double.parseDouble(park.lng);

        // 设置停车场信息
        mViewHolder.parkView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TCBApp.getAppContext(),
                        ParkActivity.class);
                intent.putExtra(ParkActivity.ARG_ID, park.id);
                intent.putExtra(ParkActivity.ARG_NAME, park.name);
                mActivity.startActivity(intent);
            }
        });

        // 设置车场名称
        mViewHolder.tvName.setText(park.name);

        // 设置车场价格
        String price = "价格: 免费";
        if (!"-1".equals(park.price)) {

            // 收费车场
            if (!TextUtils.isEmpty(park.price) && !"0".equals(park.price) && !park.price.startsWith("0元")) {
                // 价格明确
                price = String.format("价格: %s", park.price);
            } else {
                // 价格未知
                price = "价格: 未知";
            }

            // 设置空闲车位
            String total = park.total;
            String free = park.free;
            boolean isFreeLow = isParkSpaceLow(free, total);
            if (TextUtils.isEmpty(total)) {
                total = "未知";
            }
            if (TextUtils.isEmpty(free)) {
                free = "未知";
            }
            if (!"未知".equals(free)) {
                if (!isFreeLow) {
                    free = "<font color='#32a669'>" + free + "</font>";
                } else {
                    free = "<font color='#e37479'>" + free + "</font>";
                }
            }
            mViewHolder.tvFreeSpace.setText(Html.fromHtml("车位: " + free + "/"
                    + total));
        } else {
            // 免费车场
            mViewHolder.tvFreeSpace.setText("车位: 未知");
        }
        mViewHolder.tvPrice.setText(price);

        // 设置导航
        mViewHolder.btnNavi.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                launchNavigator(new double[]{lat, lng}, park.name);
            }
        });

        // 设置付车费按钮
        if ("-1".equals(park.price) || !"1".equals(park.epay)) {// 免费车场或不可支付
            mViewHolder.llPay.setVisibility(View.GONE);
            mViewHolder.divider.setVisibility(View.GONE);
        } else {
            // 收费车场
            mViewHolder.llPay.setVisibility(View.VISIBLE);
            mViewHolder.divider.setVisibility(View.VISIBLE);
            mViewHolder.btnPay.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    getOrder(park.id);
                }
            });
        }

        InfoWindow infoWindow = new InfoWindow(mViewHolder.infowindow,
                new LatLng(lat, lng), DensityUtils.dip2px(mActivity, -22));
        try {
            mMap.showInfoWindow(infoWindow);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mInfoWindowType = INFOWINDOW_PARK;
    }

    static class InfoWindowViewHolder {
        View infowindow;
        View parkView;
        View llPay;
        View divider;
        TextView tvName;
        TextView tvPrice;
        TextView tvFreeSpace;
        Button btnNavi;
        Button btnPay;
    }
}