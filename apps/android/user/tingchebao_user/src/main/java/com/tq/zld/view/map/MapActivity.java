package com.tq.zld.view.map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.aprilbrother.aprilbrothersdk.Beacon;
import com.aprilbrother.aprilbrothersdk.BeaconManager;
import com.aprilbrother.aprilbrothersdk.BeaconManager.RangingListener;
import com.aprilbrother.aprilbrothersdk.Region;
import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.navisdk.BNaviEngineManager;
import com.baidu.navisdk.BaiduNaviManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tq.zld.BuildConfig;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.AccountInfo;
import com.tq.zld.bean.Coupon;
import com.tq.zld.bean.FestivalBonus;
import com.tq.zld.bean.IbeaconInOutInfo;
import com.tq.zld.bean.IbeaconOrderInfo;
import com.tq.zld.bean.Order;
import com.tq.zld.bean.ParkInfo;
import com.tq.zld.bean.ParkingFeeCollector;
import com.tq.zld.im.adapter.EMCallBackAdapter;
import com.tq.zld.im.lib.HXSDKHelper;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.protocal.PollingProtocol;
import com.tq.zld.util.Coverter;
import com.tq.zld.util.DensityUtils;
import com.tq.zld.util.KeyboardUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.MathUtils;
import com.tq.zld.util.NetWorkUtils;
import com.tq.zld.util.ScreenUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.util.ViewUtils;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.view.LoginActivity;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.ShareActivity;
import com.tq.zld.view.UGCActivity;
import com.tq.zld.view.account.AccountTicketsActivity;
import com.tq.zld.view.account.MyRedPacketsActivity;
import com.tq.zld.view.fragment.ChooseParkingFeeCollectorFragment;
import com.tq.zld.view.fragment.InputMoneyFragment;
import com.tq.zld.view.fragment.OrderFragment;
import com.tq.zld.view.fragment.RechargeFragment;
import com.tq.zld.view.holder.MenuHolder;
import com.tq.zld.view.manager.IMManager;
import com.tq.zld.view.manager.MarkerManager;
import com.umeng.update.UmengUpdateAgent;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 程序主界面 查看用户当前地理位置，附近停车场信息，搜索停车场
 *
 * @author Clare
 */
public class MapActivity extends BaseActivity implements OnClickListener,
        OnMapLoadedCallback, OnMapClickListener,
        OnMapStatusChangeListener, BDLocationListener,
        OnGetPoiSearchResultListener, BaiduMap.OnMyLocationClickListener {
    // ibeacon
    public static final int REQUEST_ENABLE_BT = 1234;// 去打开蓝牙的标记码；
    public static final Region ALL_BEACONS_REGION = new Region("apr", null,
            null, null);
    public BeaconManager beaconManager;
    public Beacon beaconinfo;
    public Dialog makeorderDialog = null;
    private ShakeListener mShakeListener = null;
    private SoundPool soundPool;// 音频池
    private int hitOkSfx;
    public int dialogState;// 蓝牙弹框状态
    public boolean hasBluetooth = false;// 是否支持蓝牙4.0
    public boolean closeQueryLocation = false;// 是否关闭访问网络查询蓝牙车场
    public boolean canCompare = false;// 是否可以去对比距离；
    public boolean hasBluetoothDialog = false;// 是否已弹出蓝牙对话框；
    public boolean isOpenSharkLisener = false;// 是否已经打开摇一摇监听；
    public boolean isShake = false;// 是否手动摇一摇发起；
    public long curtime;// ibeacon关闭后开始计时10分钟后自动开启；摇一摇立刻开启；
    private LatLng mLastLocation;
    public IbeaconInOutInfo ibeaconInOut;// ibeacon进出场返回的信息；
    public int mDistance = 0;// 距离（记载距蓝牙车场的距离）
    private Vibrator vibrator;

    public static final int MSG_WHAT_HANDLER_ORDER = 1;// 结算NFC订单
    public static final int MSG_WHAT_IBEACON_PAY_FAIL = 8;// ibeacon支付失败
    public static final int MSG_WHAT_IBEACON_PAY_SUCCESS = 9;// ibeacon支付成功

    /**
     * 打开搜索界面的REQUEST_CODE
     */
    public static final int REQUEST_CODE_SUGGEST = 0;
//    /**
//     * 扫描二维码的REQUEST_CODE
//     */
//    public static final int REQUEST_CODE_QRSCAN = 1;
    /**
     * 拍照标记停车位置的REQUEST_CODE
     */
    public static final int REQUEST_CODE_CAMERA = 2;
    /**
     * 修改标记停车位置的REQUEST_CODE
     */
    public static final int REQUEST_CODE_UPDATE_REMARK_LOCATION = 3;

    /**
     * 默认的地图缩放级别
     */
    public static final float MAP_ZOOM_DEFAULT = 16.0f;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    private MapView mMapView;
    private BaiduMap mMap;
    private LocationClient mLocationClient;

    private PoiSearch mPoiSearch;
    private long exitTime = 0;

    // ---------------------------- 底部显示停车场布局 ---------------------
    private ImageButton ibLocation;// “我的位置”按钮
    private ImageButton ibRemark;// “停车标记”按钮
    private ImageButton ibOrder;// “查询订单”按钮
    private TextView tvPayable;// “显示可支付车场”按钮
    private TextView tvAll;// “显示全部车场”按钮
    // private Button btnNearBy;// 附近
    private View mUGCView;// 上传车场布局
    private TextView mUGCTipsTextView; // 车场较少提示信息
    private TextView mUGCLeftButton; // 上传按钮
    private TextView mUGCRightButton; // 关闭按钮

    // ---------------------------- 底部菜单按钮 ---------------------
    private ImageButton mMenuButton;
    private View mBottomMenuView;
    private Button mBottomMenu1;
    private TextView mBottomMenu1Num;
    private Button mBottomMenu2;
    private Button mBottomMenu3;

    private MarkerManager mMarkerManager;// 地图上所有停车场Marker的管理者
    private String city;

    // -----------------------------NFC 模块-----------------------------
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    public static IntentFilter[] sNFCFilters;
    public static String[][] sTechList;

    private IMManager mIMManager;

    public static Handler mHandler;

    static {
        try {
            sTechList = new String[][]{{NfcA.class.getName(),
                    Ndef.class.getName(), MifareUltralight.class.getName()}};
            IntentFilter ndef = new IntentFilter(
                    NfcAdapter.ACTION_NDEF_DISCOVERED, "text/html");
            ndef.addDataScheme("http");
            ndef.addDataAuthority("www.tingchebao.com", "80");
            sNFCFilters = new IntentFilter[]{ndef};
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_main);
        initToolbar();
        initData();
        initView();
        initMapView();
        initIbeacon();
        // 开启定位服务
        startLocating();
        // 初始化地图导航
        initBaiduNavigationManager();
        // --------------------读取NFC TAG------------------------------------
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 1, new Intent(this,
                        getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT);
        onNewIntent(getIntent());

        //如果已经登录环信，则初始化
        mIMManager = IMManager.getInstance(this);
        if (BuildConfig.IM_DEBUG && HXSDKHelper.getInstance().isLogined()) {
            LogUtils.i("已经登录，初始化环信");
            mIMManager.initHX();
//            initHX();
        } else {
            LogUtils.w("并没有初始化环信");
        }
    }

    private void initBaiduNavigationManager() {
        BaiduNaviManager.getInstance().initEngine(this, getSdcardDir(),
                new BNaviEngineManager.NaviEngineInitListener() {

                    public void engineInitSuccess() {
                        // 导航初始化是异步的，需要一小段时间，以这个标志来识别引擎是否初始化成功，为true时候才能发起导航
                        TCBApp.mIsEngineInitSuccess = true;
                        LogUtils.i(getClass(),
                                "--->> init baidu navigation engine SUCCESS!");
                    }

                    public void engineInitStart() {
                    }

                    public void engineInitFail() {
                        TCBApp.mIsEngineInitSuccess = false;
                    }
                }, new LBSAuthManagerListener() {
                    @Override
                    public void onAuthResult(int i, String s) {
                        LogUtils.d("Baidu LBS Auth result: --->> " + i + "," + s);
                    }
                });
    }

    /**
     * 获取SD卡目录
     *
     * @return
     */
    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(
                Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    // ***fly***---检测系统是否支持低功耗蓝牙；然后根据位置访问服务器寻找蓝牙车场；
    @SuppressWarnings("deprecation")
    public void initIbeacon() {
        makeorderDialog = new Builder(this).create();
        beaconManager = new BeaconManager(this);
        try {
            if (android.os.Build.VERSION.SDK_INT >= 18
                    && beaconManager.hasBluetooth()) {
                LogUtils.i(getClass(), ": --->> 手机支持蓝牙，初始化蓝牙,摇一摇监听！");
                hasBluetooth = true;
                soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5) {
                };
                hitOkSfx = soundPool.load(MapActivity.this, R.raw.shake, 0);// 载入音频流
                mShakeListener = new ShakeListener(MapActivity.this);
                mShakeListener
                        .setOnShakeListener(new ShakeListener.OnShakeListener() {
                            @Override
                            public void onShake() {
                                setShake();
                            }
                        });
                initBluetooth();
                if (TextUtils.isEmpty(TCBApp.mMobile) || !closeQueryLocation) {
                    mShakeListener.stop();
                }
            }
        } catch (Exception e) {
            LogUtils.i(getClass(), ": --->> 获取系统版本失败，视为不能使用ibeacon！");
        }
    }

    /**
     * 蓝牙扫描到ibeancon的回调；
     */
    long count = 0;// 蓝牙扫描回调的次数；

    private void initBluetooth() {
        curtime = System.currentTimeMillis();
        beaconManager.setForegroundScanPeriod(5000, 0);
        beaconManager.setRangingListener(new RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region,
                                            final List<Beacon> beacons) {
                count++;
                if (beacons != null && beacons.size() > 0) {
                    LogUtils.i(getClass(),
                            "扫描到ibeacon的个数--->> ： " + beacons.size());
                    if (!hasBluetoothDialog) {
                        if (beacons.size() == 1) {
                            hasBluetoothDialog = true;
                            LogUtils.i(getClass(),
                                    "RangingListener扫描的ibeacon的Mac地址是：--->> "
                                            + beacons.get(0).getMacAddress());
                            getIbeaconInOut(beacons.get(0), isShake);// 请求服务器判断是进场还是出场；
                            isShake = false;
                        } else {
                            int select = 3;
                            Beacon beacon = null;
                            for (int i = 0; i < beacons.size(); i++) {
                                // ibeacon距离// CLProximityUnknown,0//
                                // CLProximityImmediate,1//
                                // CLProximityNear,2// CLProximityFar3
                                if (beacons.get(i).getProximity() <= select) {
                                    LogUtils.i(getClass(),
                                            "RangingListener扫描的ibeacon的Mac地址是：--->> "
                                                    + beacons.get(i)
                                                    .getMacAddress());
                                    if (beacons.get(i).getProximity() == 1) {
                                        beacon = beacons.get(i);
                                        break;
                                    } else {
                                        select = beacons.get(i).getProximity();
                                        beacon = beacons.get(i);
                                    }
                                }
                            }
                            hasBluetoothDialog = true;
                            getIbeaconInOut(beacon, isShake);// 请求服务器判断是进场还是出场；
                            isShake = false;
                        }
                    } else {
                        LogUtils.i(
                                MapActivity.class,
                                "当前时间--->>："
                                        + System.currentTimeMillis()
                                        + "保留时间："
                                        + curtime
                                        + "间隔时间:"
                                        + (System.currentTimeMillis() - curtime)
                                        / 1000);
                        if ((System.currentTimeMillis() - curtime) > 1000 * 60 * 10) {
                            hasBluetoothDialog = false;
                            curtime = System.currentTimeMillis();
                        }
                    }
                }
                LogUtils.i(getClass(), "扫描的ibeacon的次数：--->> " + count);
                if (count % 5 == 0 && 7 == dialogState
                        && makeorderDialog.isShowing()) {
                    showMakeOrder(10, "");
                }
            }
        });
    }

    // 蓝牙扫到ibeacon时去请求服务器判断是生成还是结算；ibeaconhandle.do?major=0&minor=1&action=ibcincom&mobile=15801482643
    // 返回：{"inout":"0","uid":"-1",} inout:入场/出场，0入口，1出口
    // -1通道不存在，uid:收费员编号，不处理，生成或结算订单时传回
    public void getIbeaconInOut(final Beacon beacon, final boolean isShake) {
        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            LogUtils.i(getClass(), "ibeacon判断进出场手机号为空不处理--->>： ");
            return;
        }
        // 摇一摇弹框存在，是订单生成中，订单结算中，等待收费员结算，结算成功，之一的状态下，就不再调用此方法；
        if (makeorderDialog.isShowing()) {
            if (dialogState == 2 || dialogState == 4 || dialogState == 6
                    || dialogState == 8) {
                return;
            }
        }
        Map<String, String> params = new HashMap<>();
        params.put("action", "ibcincom");
        params.put("major", String.valueOf(beacon.getMajor()));
        params.put("minor", String.valueOf(beacon.getMinor()));
        params.put("mobile", TCBApp.mMobile);
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "ibeaconhandle.do", params);

        GsonRequest<IbeaconInOutInfo> request = new GsonRequest<>(url, IbeaconInOutInfo.class, new Listener<IbeaconInOutInfo>() {

            @Override
            public void onResponse(IbeaconInOutInfo ibeaconInOutInfo) {
                if (ibeaconInOutInfo != null && ibeaconInOutInfo.getUid() != null) {
                    ibeaconInOut = ibeaconInOutInfo;// 暂时存到本地，生成或者结算订单时候回传到服务器；
                    if ("0".equals(ibeaconInOutInfo.getInout())) { // 入口ibeacon
                        beaconinfo = beacon;
                        if ("0".equals(ibeaconInOutInfo.getOrderid())) { // 没有订单
                            LogUtils.i(getClass(),
                                    "入口没有订单--->>是否手摇： " + isShake);
                            if (isShake) {
                                showMakeOrder(2, "");
                                makeIbeaconOrder(beaconinfo);
                            } else {
                                showMakeOrder(1, "");// 弹出摇一摇生成订单对话；
                            }
                        } else {
                            LogUtils.i(getClass(),
                                    "入口有订单--->>是否手摇： " + isShake);
                            if (isShake) { // 有订单，手动摇一摇去当前订单；
                                new OrderFragment()
                                        .show(getSupportFragmentManager(),
                                                OrderFragment.class.getSimpleName());
                                beaconinfo = null;
                                makeorderDialog.dismiss();
                            }
                        }
                    } else if ("1".equals(ibeaconInOutInfo.getInout())) {// 出口ibeacon
                        beaconinfo = beacon;
                        if ("0".equals(ibeaconInOutInfo.getOrderid())) { // 没有订单
                            LogUtils.i(getClass(),
                                    "出口没有订单--->>是否手摇： " + isShake);
                            if (isShake) { // 出口没有订单主动摇一摇；//有收费员绑定走支付，没有提示收费员不在岗；
                                if ("-1".equals(ibeaconInOutInfo.getUid())) {
                                    showMakeOrder(11, "");// 提示没有收费员在岗
                                } else {
                                    ParkingFeeCollector collector = new ParkingFeeCollector(
                                            ibeaconInOutInfo.getUid(), ibeaconInOutInfo
                                            .getName(),
                                            ibeaconInOutInfo.getParkname());
                                    Intent intent = new Intent(
                                            getApplicationContext(),
                                            MainActivity.class);
                                    Bundle args = new Bundle();
                                    args.putParcelable(
                                            InputMoneyFragment.ARG_COLLECTOR,
                                            collector);
                                    intent.putExtra(
                                            MainActivity.ARG_FRAGMENT,
                                            MainActivity.FRAGMENT_INPUT_MONEY);
                                    intent.putExtra(
                                            MainActivity.ARG_FRAGMENT_ARGS,
                                            args);
                                    startActivity(intent);
                                    beaconinfo = null;
                                    makeorderDialog.dismiss();
                                }
                            }
                        } else {// 出口有
                            LogUtils.i(getClass(),
                                    "出口有订单--->>是否手摇： " + isShake);
                            if (isShake) {
                                if ("-1".equals(ibeaconInOutInfo.getUid())) {
                                    showMakeOrder(11, "");// 提示没有收费员在岗
                                } else {
                                    showMakeOrder(4, "");
                                    cashIbeaconOrder(beaconinfo);
                                }
                            } else {
                                if ("-1".equals(ibeaconInOutInfo.getUid())) {
                                    showMakeOrder(11, "");// 提示没有收费员在岗
                                } else {
                                    showMakeOrder(3, "");// 弹出摇一摇结算订单对话框；
                                }
                            }
                        }
                    } else if ("2".equals(ibeaconInOutInfo.getInout())) {// 进出口一体的ibeacon
                        beaconinfo = beacon;
                        if ("0".equals(ibeaconInOutInfo.getOrderid())) {// 没有订单；
                            if (isShake) {
                                showMakeOrder(2, "");
                                makeIbeaconOrder(beaconinfo);
                            } else {
                                showMakeOrder(1, "");// 弹出摇一摇生成订单对话；
                            }
                        } else { // 有订单
                            if (isShake) {
                                if ("-1".equals(ibeaconInOutInfo.getUid())) {
                                    showMakeOrder(11, "");// 提示没有收费员在岗
                                } else {
                                    showMakeOrder(4, "");
                                    cashIbeaconOrder(beaconinfo);
                                }
                            } else {
                                if ("-1".equals(ibeaconInOutInfo.getUid())) {
                                    showMakeOrder(11, "");// 提示没有收费员在岗
                                } else {
                                    showMakeOrder(3, "");// 弹出摇一摇结算订单对话框；
                                }
                            }
                        }
                    } else {
                        hasBluetoothDialog = false;
                    }
                } else {
                    hasBluetoothDialog = false;
                }
            }
        }, null);

        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    // 根据state的不同，绘制三种不同dialog的布局；
    public void showMakeOrder(int state, String failinfo) {
        View makeorderView = View.inflate(this, R.layout.dialog_yaoyiyao, null);
        TextView tv_cancle = (TextView) makeorderView
                .findViewById(R.id.tv_dialog_yaoyiyao_cancle);
        TextView tv_text = (TextView) makeorderView
                .findViewById(R.id.tv_dialog_yaoyiyao_text_warn);
        ProgressBar pb_yaoyiyao = (ProgressBar) makeorderView
                .findViewById(R.id.pb_dialog_yaoyiyao_progressbar);
        switch (state) {
            case 1:
                dialogState = 1;
                tv_text.setText("摇一摇生成订单");
                pb_yaoyiyao.setVisibility(View.GONE);
                break;
            case 2:
                dialogState = 2;
                tv_text.setText("订单生成中...");
                pb_yaoyiyao.setVisibility(View.VISIBLE);
                break;
            case 3:
                dialogState = 3;
                tv_text.setText("摇一摇结算订单");
                pb_yaoyiyao.setVisibility(View.GONE);
                break;
            case 4:
                dialogState = 4;
                tv_text.setText("订单结算中...");
                pb_yaoyiyao.setVisibility(View.VISIBLE);
                break;
            case 5:
                dialogState = 5;
                tv_text.setText("订单生成失败--" + failinfo);
                pb_yaoyiyao.setVisibility(View.GONE);
                break;
            case 6:
                dialogState = 6;
                tv_text.setText("订单结算中..." + failinfo);
                pb_yaoyiyao.setVisibility(View.GONE);
                break;
            case 7:
                dialogState = 7;
                tv_text.setText("正在搜索中...");
                pb_yaoyiyao.setVisibility(View.VISIBLE);
                break;
            case 8:
                dialogState = 8;
                tv_text.setText("订单结算成功！");
                pb_yaoyiyao.setVisibility(View.GONE);
                break;
            case 9:
                dialogState = 9;
                tv_text.setText("订单支付失败！");
                pb_yaoyiyao.setVisibility(View.GONE);
                break;
            case 10:
                dialogState = 10;
                tv_text.setText("搜索失败，请确认您在蓝牙车场附近！");
                pb_yaoyiyao.setVisibility(View.GONE);
                break;
            case 11:
                dialogState = 11;
                tv_text.setText("没有收费员在岗！");
                pb_yaoyiyao.setVisibility(View.GONE);
                break;
            default:
                break;
        }
        tv_cancle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                beaconinfo = null;
                makeorderDialog.dismiss();
            }
        });
        makeorderDialog.setCancelable(false);
        makeorderDialog.show();
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = (int) (displayMetrics.heightPixels * 0.55); // 高度设置为屏幕的0.55
        int width = (int) (displayMetrics.widthPixels * 0.8); // 宽度设置为屏幕的0.8
        makeorderDialog.getWindow().setLayout(width, height);
        makeorderDialog.setContentView(makeorderView);
    }

    // 摇一摇生成订单的接口：ibeaconhandle.do?major=0&minor=1&action=addorder&mobile=15801482643&uid=1000004
    // 返回：{"result":"0","info":""} result 0失败，1成功 info:信息
    private void makeIbeaconOrder(final Beacon beacon) {

        HashMap<String, String> params = new HashMap<>();
        params.put("action", "addorder");
        params.put("major", String.valueOf(beacon.getMajor()));
        params.put("minor", String.valueOf(beacon.getMinor()));
        params.put("mobile", TCBApp.mMobile);
        params.put("uid", ibeaconInOut.getUid());
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "ibeaconhandle.do", params);

        GsonRequest<IbeaconOrderInfo> request = new GsonRequest<>(url, IbeaconOrderInfo.class, new Listener<IbeaconOrderInfo>() {
            @Override
            public void onResponse(IbeaconOrderInfo ibeaconOrderInfo) {
                if (ibeaconOrderInfo != null && "1".equals(ibeaconOrderInfo.getResult())) {// 1生成订单成功，
                    beaconinfo = null;
                    makeorderDialog.dismiss();
                    new OrderFragment().show(getSupportFragmentManager(),
                            OrderFragment.class.getSimpleName());
                } else if (ibeaconOrderInfo != null && "0".equals(ibeaconOrderInfo.getResult())) {// 0生成订单失败，
                    showMakeOrder(5, ibeaconOrderInfo.getInfo());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(MapActivity.this, "网络错误!", Toast.LENGTH_SHORT).show();
                showMakeOrder(5, "");
            }
        });
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    // 摇一摇结算订单的接口：ibeaconhandle.do?major=0&minor=1&action=doorder&mobile=15801482643&uid=22003&orderid=；
    // 返回：{"result":"0","info":""} result 0失败，1结算中，2已结算过，3余额不足 ，info:信息
    public void cashIbeaconOrder(final Beacon beacon) {
//        TimerUtils.startPollingService(this, 0, 2);

        PollingProtocol protocol = new PollingProtocol(mHandler);
        protocol.startPolling();

        HashMap<String, String> params = new HashMap<>();
        params.put("action", "doorder");
        params.put("mobile", TCBApp.mMobile);
        params.put("major", String.valueOf(beacon.getMajor()));
        params.put("minor", String.valueOf(beacon.getMinor()));
        params.put("uid", ibeaconInOut.getUid());
        params.put("orderid", ibeaconInOut.getOrderid());
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "ibeaconhandle.do", params);

        GsonRequest<IbeaconOrderInfo> request = new GsonRequest<>(url, IbeaconOrderInfo.class, new Listener<IbeaconOrderInfo>() {
            @Override
            public void onResponse(IbeaconOrderInfo ibeaconOrderInfo) {
                if (ibeaconOrderInfo != null && "1".equals(ibeaconOrderInfo.getResult())) { // 1结算中，
                    showMakeOrder(6,
                            ibeaconOrderInfo.getInfo() == null ? "" : ibeaconOrderInfo.getInfo());
                } else if (ibeaconOrderInfo != null && "0".equals(ibeaconOrderInfo.getResult())) {// 0结算订单失败，
                    showMakeOrder(9,
                            ibeaconOrderInfo.getInfo() == null ? "" : ibeaconOrderInfo.getInfo());
                } else if (ibeaconOrderInfo != null && "2".equals(ibeaconOrderInfo.getResult())) {// 2已结算过，
                    showMakeOrder(9, "已结算过！");
                } else if (ibeaconOrderInfo != null && "3".equals(ibeaconOrderInfo.getResult())) {// 3余额不足
                    notBalanceDialog();
                    makeorderDialog.dismiss();
                } else {
                    showMakeOrder(9,
                            ibeaconOrderInfo.getInfo() == null ? "" : ibeaconOrderInfo.getInfo());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(MapActivity.this, "网络错误!", Toast.LENGTH_SHORT).show();
                showMakeOrder(9, "");
            }
        });
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    // 蓝牙订单那余额不足弹框提醒；
    public void notBalanceDialog() {
        View open_dialog_view = View.inflate(this,
                R.layout.dialog_open_bluetooth, null);
        TextView tv_open = (TextView) open_dialog_view
                .findViewById(R.id.tv_dialog_open_buletooth_install);
        TextView tv_title = (TextView) open_dialog_view
                .findViewById(R.id.tv_dialog_open_buletooth_title);
        TextView tv_cancle = (TextView) open_dialog_view
                .findViewById(R.id.tv_dialog_open_buletooth_cancle);
        final Dialog openDialog = new Builder(this).create();
        openDialog.setCancelable(false);
        tv_title.setText("账户余额不足，无法支付订单，请充值");
        tv_open.setText("充值");
        tv_cancle.setText("查看订单");
        tv_open.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openDialog.dismiss();
                Intent intent = new Intent(getApplicationContext(),
                        MainActivity.class);
                intent.putExtra(MainActivity.ARG_FRAGMENT,
                        MainActivity.FRAGMENT_ACCOUNT);
                startActivity(intent);
            }
        });
        tv_cancle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();
                new OrderFragment().show(getSupportFragmentManager(),
                        OrderFragment.class.getSimpleName());
            }
        });
        openDialog.show();
        openDialog.setContentView(open_dialog_view);
    }

    /**
     * 连接服务 开始搜索beacon connect service start scan beacons
     */
    private void connectToService() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_BEACONS_REGION);
                    // beaconManager.startMonitoring(ALL_BEACONS_REGION);
                } catch (RemoteException e) {
                }
            }
        });
    }

    /**
     * 定义震动
     */
    public void startVibrato() {
        // 第一个｛｝里面是节奏数组， 第二个参数是重复次数，-1为不重复，非-1俄日从pattern的指定下标开始重复
        vibrator.vibrate(new long[]{500, 200, 500, 200}, -1);
        // 播放音频，可以对左右音量分别设置，还可以设置优先级，循环次数以及速率
        // 速率最低0.5最高为2，1代表 正常速度
        soundPool.play(hitOkSfx, 1, 1, 0, 0, 1);
    }

    /**
     * 摇一摇成功后回调；
     */
    private void setShake() {
        mShakeListener.stop();
        startVibrato();
        curtime = System.currentTimeMillis();
        if (!beaconManager.isBluetoothEnabled()) { // 蓝牙没打开，提醒用户去开蓝牙；
            openBluetoothDialog();
        } else {
            if (!hasBluetoothDialog) {
                LogUtils.i(MapActivity.class, "没有发现蓝牙设备--->>");
                isShake = true;
                showMakeOrder(7, "");
                count = 0;
            } else {
                if (makeorderDialog.isShowing()) {
                    LogUtils.i(MapActivity.class, "蓝牙功能弹框的状态--->>："
                            + dialogState);
                    switch (dialogState) {
                        case 1: // 摇一摇生成订单
                            showMakeOrder(2, "");
                            makeIbeaconOrder(beaconinfo);
                            break;
                        case 2: // 生成订单中...

                            break;
                        case 3: // 摇一摇结算订单
                            showMakeOrder(4, "");
                            cashIbeaconOrder(beaconinfo);
                            break;
                        case 4: // 结算订单中...（请求服务器要求结算）

                            break;
                        case 5: // 订单生成失败
                            showMakeOrder(2, "");
                            makeIbeaconOrder(beaconinfo);
                            break;
                        case 6: // 订单结算中，收费员在调整价格；

                            break;
                        case 8: // 订单结算成功

                            break;
                        default:
                            break;
                    }
                } else {
                    if (beaconinfo != null) {
                        LogUtils.i(MapActivity.class, "手动摇一摇判断进入口--->>"
                                + beaconinfo.toString());
                        getIbeaconInOut(beaconinfo, true);
                    } else {
                        showMakeOrder(7, "");
                        count = 0;
                        isShake = true;
                        hasBluetoothDialog = false;
                    }
                }
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                vibrator.cancel();
                mShakeListener.start();
            }
        }, 2000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mShakeListener != null) {
            mShakeListener.stop();
            isOpenSharkLisener = false;
        }

        if (BuildConfig.IM_DEBUG) {
            if (HXSDKHelper.getInstance().isLogined()) {
                mIMManager.unregEventListener();
//            EMChatManager.getInstance().unregisterEventListener(eventListener);
                TCBApp.getAppContext().hxsdkHelper.popActivity(this);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!TextUtils.isEmpty(TCBApp.mMobile) && !isOpenSharkLisener
                && closeQueryLocation) {// 手机号不为空，没有打开摇一摇监听，已经关闭位置对比；
            mShakeListener.start();
            isOpenSharkLisener = true;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!TextUtils.isEmpty(TCBApp.mMobile) && !isOpenSharkLisener
                && closeQueryLocation) {// 手机号不为空，没有打开摇一摇监听，已经关闭位置对比；
            mShakeListener.start();
            isOpenSharkLisener = true;
        }
    }

    private void getAroundParks(final LatLng position) {
        if (position == null) {
            dismissProgressDialog();
            return;
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("action", "get2kpark");
        params.put("payable", String.valueOf(mMarkerManager.getShowingMode()));
        params.put("lat", String.valueOf(position.latitude));
        params.put("lng", String.valueOf(position.longitude));
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "getpark.do", params);

        Listener<JSONObject> listener = new Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject object) {
                mMarkerManager.hideInfoWindow();
                dismissProgressDialog();
                LogUtils.d("获取周围车场信息 result: --->> "+ object);
                if (object != null) {
                    try {
                        String sParkId = object.getString("suggid");
                        String parkList = object.getString("data");
                        String lack = object.getString("lack");
                        ArrayList<ParkInfo> parkInfoList = new Gson().fromJson(
                                parkList, new TypeToken<ArrayList<ParkInfo>>() {
                                }.getType());
                        mMarkerManager.addMarkers(parkInfoList, sParkId);

                        // 将推荐车场及当前位置包含到地图视野范围内
//                        LatLng sParkPosition = null;
//                        if (parkInfoList != null && parkInfoList.size() > 0) {
//                            for (ParkInfo parkInfo : parkInfoList) {
//                                if (TextUtils.equals(sParkId, parkInfo.id)) {
//                                    sParkPosition = new LatLng(
//                                            Double.parseDouble(parkInfo.lat),
//                                            Double.parseDouble(parkInfo.lng));
//                                    break;
//                                }
//                            }
//                        }
//                        LatLngBounds.Builder builder = new LatLngBounds.Builder()
//                                .include(position);
//                        if (sParkPosition != null) {
//                            builder.include(sParkPosition);
//                            LatLngBounds bounds = builder.build();
//                            mMap.animateMapStatus(MapStatusUpdateFactory
//                                    .newLatLngBounds(bounds,
//                                            mMapView.getWidth() / 2,
//                                            mMapView.getHeight() / 2));
//                        } else {
                        mMap.animateMapStatus(MapStatusUpdateFactory
                                .newLatLngZoom(position, MAP_ZOOM_DEFAULT));
//                        }
                        // 判断是否可以上传车场
                        showUGCView(true, "1".equals(lack));
                    } catch (Exception e) {
                        e.printStackTrace();
                        mMap.animateMapStatus(MapStatusUpdateFactory
                                .newLatLngZoom(position, MAP_ZOOM_DEFAULT));
                    }
                } else {
                    mMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(
                            position, MAP_ZOOM_DEFAULT));
                }
            }
        };
        JsonObjectRequest request = new JsonObjectRequest(url, listener,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        mMarkerManager.hideInfoWindow();
                        dismissProgressDialog();
                        if (volleyError instanceof TimeoutError) {
                            Toast.makeText(MapActivity.this, "网络超时！", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MapActivity.this, "网络错误！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        TCBApp.getAppContext().addToRequestQueue(request, "getAroundPark");
    }

    /**
     * 根据有无订单和订单状态更新查询订单按钮界面
     *
     * @param orderState
     */
    public void updateOrderButton(String orderState) {
        if (TextUtils.isEmpty(orderState)) {
            return;
        }
        switch (orderState) {
            case Order.STATE_PENDING:// 有未结算订单
                ibOrder.setImageResource(R.drawable.ic_map_order_stopping);
                break;
            case Order.STATE_PAYING:// 有等待支付订单
            case Order.STATE_PAY_FAILED:// 有支付失败订单
                ibOrder.setImageResource(R.drawable.ic_map_order_pending);
                break;
            default:
                ibOrder.setImageResource(R.drawable.ic_map_order_nomal);
                break;
        }
    }

    // 检查是否有节日红包
    private void checkIfHaveFestivalBonus() {
        Calendar currentDay = Calendar.getInstance();
        currentDay.setTimeInMillis(System.currentTimeMillis());
        Calendar lastFestival = Calendar.getInstance();
        lastFestival.setTimeInMillis(TCBApp.getAppContext().readLong(R.string.sp_festival, 0));
        if (currentDay.get(Calendar.YEAR) != lastFestival.get(Calendar.YEAR)
                || currentDay.get(Calendar.MONTH) != lastFestival
                .get(Calendar.MONTH)
                || currentDay.get(Calendar.DAY_OF_MONTH) != lastFestival
                .get(Calendar.DAY_OF_MONTH)) {
            getFestivalBonus();
        }
    }

    private void getFestivalBonus() {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "hbonus");
        params.put("mobile", TCBApp.mMobile);
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "carowner.do", params);
        Listener<FestivalBonus> listener = new Listener<FestivalBonus>() {

            @Override
            public void onResponse(final FestivalBonus bonus) {
                if (bonus != null && !TextUtils.isEmpty(bonus.imgurl)) {
                    final DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(
                            displayMetrics);
                    ImageSize imageSize = new ImageSize(
                            displayMetrics.widthPixels,
                            displayMetrics.widthPixels * 2 / 3);
                    ImageLoader.getInstance().loadImage(
                            TCBApp.mServerUrl + bonus.imgurl, imageSize,
                            new SimpleImageLoadingListener() {
                                public void onLoadingComplete(String imageUri,
                                                              View view, Bitmap loadedImage) {
                                    showFestivalBonusDialog(bonus, loadedImage,
                                            displayMetrics.widthPixels);
                                }
                            });
                }

            }
        };
        GsonRequest<FestivalBonus> request = new GsonRequest<>(url,
                FestivalBonus.class, listener, null);
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    private void showFestivalBonusDialog(FestivalBonus bonus, Bitmap bitmap,
                                         int width) {
        if (bitmap == null) {
            return;
        }
        final AlertDialog dialog = new AlertDialog.Builder(this,
                R.style.TransparentDialog).create();
        dialog.setCancelable(false);
        View contentView = View.inflate(this, R.layout.dialog_festival, null);
        View rlContainer = contentView
                .findViewById(R.id.rl_dialog_festival_container);
        RelativeLayout.LayoutParams rlContainerParams = (LayoutParams) rlContainer
                .getLayoutParams();
        rlContainerParams.width = width;
        rlContainerParams.height = rlContainerParams.width * 2 / 3;
        rlContainer.setLayoutParams(rlContainerParams);

        ImageView ivContent = (ImageView) contentView
                .findViewById(R.id.iv_dialog_festival_content);
        RelativeLayout.LayoutParams ivContentParams = (LayoutParams) ivContent
                .getLayoutParams();
        int ivCancelWidth = DensityUtils.dip2px(this, 24);
        ivContentParams.width = rlContainerParams.width - ivCancelWidth;
        ivContentParams.height = rlContainerParams.height - ivCancelWidth;
        ivContent.setLayoutParams(ivContentParams);

        ImageView ivCancel = (ImageView) contentView
                .findViewById(R.id.iv_dialog_festival_cancel);
        ivContentParams = (LayoutParams) ivCancel.getLayoutParams();
        ivContentParams.width = ivCancelWidth;
        ivContentParams.height = ivCancelWidth;
        ivContentParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ivCancel.setLayoutParams(ivContentParams);

        ivContent.setImageBitmap(bitmap);
        if ("1".equals(bonus.sharable)) {
            ivContent.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(),
                            ShareActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            });
        }
        contentView.findViewById(R.id.iv_dialog_festival_cancel)
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
        TCBApp.getAppContext().saveLong(R.string.sp_festival, System.currentTimeMillis());
        dialog.setContentView(contentView);
    }

    private void getOrder() {
        // http://s.tingchebao.com/zld/carowner.do?action=currentorder&mobile=15375242041
        String url = TCBApp.mServerUrl
                + "carowner.do?action=currentorder&mobile=" + TCBApp.mMobile;
        LogUtils.i(getClass(), "getOrderMsg url: --->> " + url);
        Listener<Order> listener = new Listener<Order>() {

            @Override
            public void onResponse(Order order) {

                if (order != null && !TextUtils.isEmpty(order.getOrderid())) {
                    updateOrderButton(order.getState());
                    if (Order.STATE_PAYING.equals(order.getState())) {
                        showOrderFragmentDialog(order);
                    }
                }
            }
        };
        GsonRequest<Order> request = new GsonRequest<>(url, Order.class,
                listener, null);
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    private void initToolbar() {

        // Spinner spinner = (Spinner) findViewById(R.id.spinner_map);
        // String[] items = getResources().getStringArray(R.array.action_list);
        // ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
        // R.layout.simple_spinner_item, items);
        // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // spinner.setAdapter(adapter);
        // spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
        //
        // @Override
        // public void onItemSelected(AdapterView<?> parent, View view,
        // int position, long id) {
        //
        // }
        //
        // @Override
        // public void onNothingSelected(AdapterView<?> parent) {
        //
        // }
        // });

        Toolbar mBar = (Toolbar) findViewById(R.id.toolbar_map);
//        ViewCompat.setElevation(mBar, DensityUtils.dip2px(this, 10));
        mBar.setBackgroundColor(getResources().getColor(R.color.bg_toolbar));
        mBar.setTitle(getString(R.string.app_name));
        mBar.inflateMenu(R.menu.menu_map);

        //调试模式可自由选择服务器
        switch (BuildConfig.BUILD_TYPE) {
            case "debug":
            case "alpha":
                StringBuilder title = new StringBuilder("选择服务器");
                if (TCBApp.mServerUrl.equals(getString(R.string.url_release))) {
                    title.append("(线上)");
                } else if (TCBApp.mServerUrl.equals(getString(R.string.url_local))) {
                    title.append("(本地)");
                } else if (TCBApp.mServerUrl.equals(getString(R.string.url_beta))) {
                    title.append("(预上线)");
                } else if(TCBApp.mServerUrl.equals(getString(R.string.url_zld_local))){
                    title.append("zldlocal");
                } else if(TCBApp.mServerUrl.equals(getString(R.string.url_zld_line))){
                    title.append("zldline");
                }
                else{
                    String url = "(" + TCBApp.mServerUrl.replace("/zld/", "") + ")";
                    title.append(url);
                }
                mBar.getMenu().add(Menu.NONE, -1, Menu.NONE, title);
        }

        mBar.setOnMenuItemClickListener(
                new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case android.R.id.home:
                                changeDrawerLayoutState();
                                return true;
                            case R.id.action_qrscan:
                                if (TextUtils.isEmpty(TCBApp.mMobile)) {
                                    Toast.makeText(MapActivity.this, "请先登录！", Toast.LENGTH_SHORT).show();
                                    openDrawer();
                                    return true;
                                }
                                startActivity(new Intent(getApplicationContext(),
                                        CaptureActivity.class));
                                return true;
                            case -1:
                                showChooseServerDialog();
                                return true;
                        }
                        return false;
                    }
                }
        );

        mDrawerLayout = (DrawerLayout)

                findViewById(R.id.widget_drawer);

        RelativeLayout drawerView = (RelativeLayout) findViewById(R.id.drawer_menu);

        // 设置侧滑菜单宽度为屏幕的4/5大小
        android.view.ViewGroup.LayoutParams layoutParams = drawerView
                .getLayoutParams();
        layoutParams.width = ScreenUtils.getScreenWidth(this) * 4 / 5;
        drawerView.setLayoutParams(layoutParams);

        MenuHolder sildingMenu = MenuHolder.getInstance();
        sildingMenu.setActivity(this);
        drawerView.addView(sildingMenu.getMenu());
        mDrawerToggle = new

                ActionBarDrawerToggle(this, mDrawerLayout, mBar,
                        R.string.drawer_open, R.string.drawer_close) {
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                        invalidateOptionsMenu();
                        syncState();
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                        invalidateOptionsMenu();
                        //如果用户头像获取到了，则刷新侧滑菜单中头像
//                        if (MenuHolder.getInstance().isImageUrlChange()) {
//                            MenuHolder.getInstance().refreshPhotoView();
//                        }
                        syncState();
                    }
                }

        ;
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    private boolean switchServerUrl = false;
    /**
     * 选择服务器，只在alpha和debug版中有效
     */
    private void showChooseServerDialog() {
        new AlertDialog.Builder(this).setItems(new String[]{
                "线上",
                "本地",
                "预上线",
                "zldlocal",
                "zldline",
                "自定义"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        saveUrlToLocal(getString(R.string.url_release));
                        break;
                    case 1:
                        saveUrlToLocal(getString(R.string.url_local));
                        break;
                    case 2:
                        saveUrlToLocal(getString(R.string.url_beta));
                        break;
                    case 3:
                        saveUrlToLocal(getString(R.string.url_zld_local));
                    case 4:
                        saveUrlToLocal(getString(R.string.url_zld_line));
                        break;
                    case 5:
                        showInputUrlDialog();
                        break;
                    default:
                        break;
                }
            }

            private void saveUrlToLocal(String url) {
                //重新获取好友列表信息
                TCBApp.getAppContext().saveBoolean(R.string.sp_im_user_head_init, false);
                TCBApp.getAppContext().saveStringSync(R.string.sp_url_server, url);
//                Toast.makeText(MapActivity.this, "重启软件以生效", Toast.LENGTH_SHORT).show();
                switchServerUrl = true;
                finish();
            }

            private void showInputUrlDialog() {
                AlertDialog.Builder mDialog = new AlertDialog.Builder(MapActivity.this);
                View view = View.inflate(MapActivity.this, R.layout.dialog_simple_input, null);
                final EditText etPlate = (EditText) view
                        .findViewById(R.id.dialog_edittext);
                etPlate.setText("http://192.168.");
                etPlate.setSelection(etPlate.getText().length());

                // 设置InputType
                etPlate.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                etPlate.setKeyListener(DigitsKeyListener.getInstance("0123456789.:"));
                KeyboardUtils.openKeybord(etPlate, MapActivity.this);
                mDialog.setView(view).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        KeyboardUtils.closeKeybord(etPlate, MapActivity.this);
                        Editable text = etPlate.getText();
                        Pattern ipPattern = Pattern.compile(
                                "^http://(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");
                        Matcher matcher = ipPattern.matcher(text);
                        if (matcher.find()) {
                            saveUrlToLocal(text.toString() + "/zld/");
                        } else {
                            Toast.makeText(MapActivity.this, "非法的URL地址!", Toast.LENGTH_SHORT).show();
                            showInputUrlDialog();
                        }
                    }
                }).setNegativeButton("取消", null).show();
            }
        }).show();
    }

    private void initData() {

        mHandler = new MapHandler(this);

        // 初始化百度地图PoiSearch引擎
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        // 初始化系统震动服务
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void initView() {

        // 屏蔽地图底部布局的点击事件
        findViewById(R.id.rl_map_bottom).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        ibLocation = (ImageButton) findViewById(R.id.ib_map_location);
        ibLocation.setOnClickListener(this);
        ibLocation.setVisibility(View.GONE);
        ibRemark = (ImageButton) findViewById(R.id.ib_map_remark);
        ibRemark.setOnClickListener(this);
        ibRemark.setVisibility(View.GONE);
        ibOrder = (ImageButton) findViewById(R.id.ib_map_order);
        ibOrder.setOnClickListener(this);
        tvPayable = (TextView) findViewById(R.id.tv_map_payable);
        tvPayable.setOnClickListener(this);
        // tvPayable.setText("显示可支付车场");
        // tvPayable.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvPayable.setCompoundDrawablesWithIntrinsicBounds(0,
                R.drawable.ic_switcher_payable, 0, 0);
        tvAll = (TextView) findViewById(R.id.tv_map_all);
        tvAll.setOnClickListener(this);
        // tvAll.setText("全部车场");
        // tvAll.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvAll.setCompoundDrawablesWithIntrinsicBounds(0,
                R.drawable.ic_switcher_default, 0, 0);
        EditText etSearch = (EditText) findViewById(R.id.et_map_search);
        etSearch.setOnClickListener(this);
        etSearch.setInputType(EditorInfo.TYPE_NULL);
        mUGCView = findViewById(R.id.ll_map_ugc);
        mUGCTipsTextView = (TextView) findViewById(R.id.tv_map_ugc_hint);
        mUGCLeftButton = (TextView) findViewById(R.id.tv_map_ugc);
        mUGCLeftButton.setOnClickListener(this);
        mUGCRightButton = (TextView) findViewById(R.id.tv_map_ugc_close);
        mUGCRightButton.setOnClickListener(this);

        mMenuButton = (ImageButton) findViewById(R.id.ib_map_menu);
        mMenuButton.setOnClickListener(this);
        mBottomMenuView = findViewById(R.id.ll_map_menu);
        mBottomMenu1 = (Button) findViewById(R.id.btn_map_menu1);
        mBottomMenu1.setOnClickListener(this);
        mBottomMenu2 = (Button) findViewById(R.id.btn_map_menu2);
        mBottomMenu2.setOnClickListener(this);
        mBottomMenu3 = (Button) findViewById(R.id.btn_map_menu3);
        mBottomMenu3.setOnClickListener(this);
        mBottomMenu1Num = (TextView) findViewById(R.id.tv_map_menu1_number);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        hideBottomMenu();
        return super.dispatchTouchEvent(ev);
    }

    private void startLoginActivity() {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    /**
     * 打开或关闭侧滑菜单
     */
    private void changeDrawerLayoutState() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    /**
     * 关闭侧滑菜单
     */
    public void closeDrawer() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    /**
     * 打开侧滑菜单
     */
    public void openDrawer() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            return;
        }
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    private void initMapView() {
        mMapView = (MapView) findViewById(R.id.map_map);
        mMapView.setVisibility(View.INVISIBLE);
        mMapView.showZoomControls(false);
        mMapView.showScaleControl(false);
        mMap = mMapView.getMap();
        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(false);
        mMap.setBuildingsEnabled(false);
        // mBaiduMap.setMaxAndMinZoomLevel(19.0f, 9.0f);
        mMarkerManager = MarkerManager.getInstance(mMap, this);
        mMap.setOnMarkerClickListener(mMarkerManager);
        mMap.setOnMapLoadedCallback(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMapClickListener(this);
        // mBaiduMap.setOnMapTouchListener(this);
        mMap.setOnMapStatusChangeListener(this);
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
        option.setLocationMode(LocationMode.Hight_Accuracy);
        option.setIgnoreKillProcess(false);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        mLocationClient.requestLocation();
    }

    private void updateOfflineMap(final int cityId) {
        if (cityId == -1) {
            return;
        }
        if (NetWorkUtils.isWifiConnected(MapActivity.this)) {
            final MKOfflineMap offlineMap = new MKOfflineMap();
            offlineMap.init(new MKOfflineMapListener() {

                @Override
                public void onGetOfflineMapState(int type, int state) {
                    switch (type) {
                        case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                            MKOLUpdateElement update = offlineMap
                                    .getUpdateInfo(state);
                            LogUtils.i(MapActivity.class,
                                    "离线地图下载进度: --->> "
                                            + (update == null ? "null"
                                            : update.ratio).toString());
                            // 用户切换网络到非WiFi时即暂停下载
                            if (!NetWorkUtils.isWifiConnected(MapActivity.this)) {
                                offlineMap.pause(cityId);
                                offlineMap.destroy();
                                LogUtils.i(MapActivity.class,
                                        "--->> 切换到3G网络，停止下载 <<---");
                            }
                            if (update != null && update.ratio == 100) {
                                LogUtils.i(MapActivity.class, "离线地图下载完成: --->> "
                                        + update.cityName);
                                offlineMap.destroy();
                            }
                        }
                        break;
                        case MKOfflineMap.TYPE_NEW_OFFLINE:
                            // 有新离线地图安装
                            LogUtils.i(MapActivity.class,
                                    "add offlineMap num: --->> " + state);
                            break;
                        case MKOfflineMap.TYPE_VER_UPDATE:
                            // 版本更新提示
                            if (offlineMap.remove(cityId)) {
                                offlineMap.start(cityId);
                                LogUtils.i(getClass(), "离线地图数据版本更新：--->> 重新下载！");
                            }
                            break;
                    }
                }
            });
            MKOLUpdateElement updateElement = offlineMap.getUpdateInfo(cityId);
            if (updateElement == null || updateElement.update
                    || updateElement.ratio != 100) {
                offlineMap.remove(cityId);
                offlineMap.start(cityId);
                LogUtils.i(MapActivity.class, "开始下载离线地图: --->> " + cityId);
            }
            if (updateElement != null) {
                LogUtils.i(MapActivity.class, "离线地图更新信息: --->> "
                        + updateElement.cityName + "，" + updateElement.update);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.i("onResume");
        mMapView.onResume();

        // 将底部菜单按钮复位
        mMenuButton.setImageResource(R.drawable.ic_map_menu_open_normal);

        // 获取账户信用信息
        checkAccount();

        // TODO 刷新周边车场信息？？？
        // getAroundParks(TCBApp.mLocation);

        if (mLocationClient != null && !mLocationClient.isStarted()) {
            mLocationClient.start();
        }

        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, MapActivity.sNFCFilters, sTechList);
        }

        //是否删除停车标记
        if (TCBApp.getAppContext().readBoolean(R.string.sp_remark_delete, true)) {
            mMarkerManager.removeRemarkMarker();
        } else {
            mMarkerManager.addRemarkMarker();
        }

        if (BuildConfig.IM_DEBUG) {
            //如果并没有初始化，
            if (HXSDKHelper.getInstance().isLogined()){
                LogUtils.i("已经登录，初始化环信");
                mIMManager.initHX();
//                initHX();
                TCBApp.getAppContext().hxsdkHelper.pushActivity(this);
//            EMChatManager.getInstance().registerEventListener(eventListener, new EMNotifierEvent.Event[]{EMNotifierEvent.Event.EventNewMessage, EMNotifierEvent.Event.EventOfflineMessage});
                mIMManager.regEventListener();
            }

        }

    }

    private void checkAccount() {
        if (TextUtils.isEmpty(TCBApp.mMobile)) {

            // 隐藏去充值提示框
            showUGCView(false, false);

            // 将订单按钮状态设为原始状态
            updateOrderButton(Order.STATE_PAYED);

            // 将菜单布局“待领取红包”按钮初始化
            mBottomMenu1Num.setText("");
            mBottomMenu1Num.setVisibility(View.GONE);

            // 更新底部菜单按钮
            if (mBottomMenuView.getHeight() == 0) {
                mMenuButton.setImageResource(R.drawable.ic_map_menu_open_normal);
            }
            return;
        }
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "detail");
        params.put("mobile", TCBApp.mMobile);
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "carowner.do", params);
        GsonRequest<AccountInfo> request = new GsonRequest<AccountInfo>(url, AccountInfo.class, new Listener<AccountInfo>() {
            @Override
            public void onResponse(AccountInfo accountInfo) {
                // 存储账户认证状态
                if (accountInfo != null) {

                    if (!TextUtils.isEmpty(accountInfo.bonusid)) {
                        // 设置底部菜单“领取红包按钮”
                        mBottomMenu1Num.setText(accountInfo.bonusid);
                        mBottomMenu1Num.setVisibility(View.VISIBLE);
                        // 更新底部菜单按钮
                        if (mBottomMenuView.getHeight() == 0) {
                            mMenuButton.setImageResource(R.drawable.ic_map_menu_open_new_message);
                        }
                    } else {
                        mBottomMenu1Num.setText("");
                        mBottomMenu1Num.setVisibility(View.GONE);
                    }

                    // 判断是否要显示充值提示
                    boolean show = accountInfo.limit != 0 && accountInfo.limit_balan < accountInfo.limit_warn;
                    mUGCLeftButton.setTag(accountInfo);
                    showUGCView(false, show);

                    TCBApp.getAppContext().getAccountPrefs().edit()
                            .putInt(getString(R.string.sp_plate_state), accountInfo.state).commit();
                    //  刷新侧滑菜单账户信息
                    MenuHolder.getInstance().refreshAccountInfo();
                }
            }
        }, null);

        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    /**
     * 设置UGCView的显示状态
     *
     * @param ugc  是否显示UGC布局，true：显示，false：显示Recharge布局
     * @param show 是否显示
     */
    private void showUGCView(boolean ugc, boolean show) {
        int minHeight = DensityUtils.dip2px(MapActivity.this, 0.5f);
        int maxHeight = DensityUtils.dip2px(MapActivity.this, 24f);
        String leftButtonText = mUGCLeftButton.getText().toString();
        if (show) {
            if (ugc) {
                // 显示UGC布局
                // 判断充值布局是否处于显示状态
                if (mUGCView.getHeight() == maxHeight && leftButtonText.contains("充值")) {
                    return;
                }
                mUGCTipsTextView.setText("当前位置车场较少，你可以选择上传一个。");
                mUGCLeftButton.setText(getString(R.string.map_ugc));
                mUGCRightButton.setVisibility(View.VISIBLE);
            } else {
                // 显示充值布局
                if (mUGCView.getHeight() == maxHeight) {
                    // 已经处于显示状态
                    return;
                }
                if (mUGCLeftButton.getTag() == null) {
                    LogUtils.e(getClass(), "--->> AccountInfo is null!!!");
                    return;
                }
                AccountInfo accountInfo = (AccountInfo) mUGCLeftButton.getTag();
                mUGCTipsTextView.setText(Html.fromHtml(
                        "您的可用信用额度<font color='#D25343'>不足" + MathUtils.parseIntString(accountInfo.limit_warn) + "元</font>，请尽快充值还款"));
                mUGCLeftButton.setText(Html.fromHtml("<u>去充值</u>"));
                mUGCRightButton.setVisibility(View.GONE);
            }
            ViewUtils.performAnimate(mUGCView, false, minHeight, maxHeight, 800, null);
            return;
        }

        // 隐藏布局
        // 先判断当前是否处于隐藏状态
        if (mUGCView.getHeight() < maxHeight / 2) {
            return;
        }
        if ((ugc && leftButtonText.contains("上传")) || ((!ugc && leftButtonText.contains("充值")))) {
            ViewUtils.performAnimate(mUGCView, false, maxHeight, minHeight, 800, null);
        }
    }

    private Order mOrder;
    private boolean mSettle = false;

    @Override
    protected void onNewIntent(Intent intent) {
        // ---------------------------处理支付订单的Notification-----------------------------------
        Order order = intent.getParcelableExtra("order");// 开启此activity的intent携带过来的订单信息
        if (order != null) {
            if (TextUtils.isEmpty(TCBApp.mMobile)) {
                Toast.makeText(this, "请先登录！", Toast.LENGTH_SHORT).show();
                startLoginActivity();
                return;
            }

            this.mOrder = order;
            this.mSettle = intent.getBooleanExtra("settle", false);
            return;
        } else {
            getOrder();
        }

        Coupon coupon = intent.getParcelableExtra("coupon");
        if (coupon != null && !TextUtils.isEmpty(coupon.id)) {
            showCouponDialog(coupon);
        }

        //车费进场
        String cid = intent.getStringExtra("cid");
        if (cid != null && !TextUtils.isEmpty(cid)) {
            showEnterParkDialog(cid);
        }

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            LogUtils.i(
                    getClass(),
                    "--------------------------->> NFC NDEF-TAG DISCOVERED <<---------------------------");
            if (TextUtils.isEmpty(TCBApp.mMobile)) {
                Toast.makeText(this, "请先登录！", Toast.LENGTH_SHORT).show();
                startLoginActivity();
                return;
            }
            // setIntent(intent);
            // Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // if (tag != null) {
            // String[] techList = tag.getTechList();
            // for (String str : techList) {
            // LogUtils.i(getClass(), str);
            // }
            // }
            String uuid = Coverter.getUid(intent);
            if (!TextUtils.isEmpty(uuid)) {
                getOrderUseUUID(uuid);
            } else {
                Toast.makeText(this, "请使用停车宝专用停车卡！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 入场确认Dialog
     * @param cid
     */
    private void showEnterParkDialog(final String cid) {
        View v = getLayoutInflater().inflate(R.layout.dialog_enter_park,null);
        TextView title = (TextView) v.findViewById(R.id.text);
        final Dialog dialog = new AlertDialog.Builder(this).setView(v).setCancelable(false).create();
        v.findViewById(R.id.btn_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        v.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> params = new HashMap<>();
                params.put("action", "addorder");
                params.put("mobile", TCBApp.mMobile);
                params.put("cid", cid);
                String url = URLUtils.genUrl(TCBApp.mServerUrl + "carinter.do", params);
                StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        LogUtils.i(s);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(TCBApp.getAppContext(), "停车成功", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }, null);

                TCBApp.getAppContext().addToRequestQueue(stringRequest);
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    /**
     * 领取到专用券的Dialog
     *
     * @param coupon
     */
    private void showCouponDialog(Coupon coupon) {

        if (TextUtils.isEmpty(coupon.fee.name)) {
            coupon.fee.name = "收费员";
        }

        View view = getLayoutInflater().inflate(R.layout.dialog_coupon, null, false);
        final Dialog dialog = new AlertDialog.Builder(this).setView(view).setCancelable(false).create();
        view.findViewById(R.id.ib_dialog_coupon_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.btn_dialog_coupon_detail).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TCBApp.getAppContext(), AccountTicketsActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        TextView peopleTextView = (TextView) view.findViewById(R.id.tv_dialog_coupon_people);
        peopleTextView.setText(String.format(peopleTextView.getText().toString(), coupon.fee.name, coupon.fee.id));
        TextView moneyTextView = (TextView) view.findViewById(R.id.tv_dialog_coupon_money);
        moneyTextView.setText(Html.fromHtml("专用券金额：<font color='#e37479'>" + coupon.money + "元</font>"));
        TextView parkTextView = (TextView) view.findViewById(R.id.tv_dialog_coupon_park);
        parkTextView.setText(Html.fromHtml("可使用停车场：<font color='#e37479'>" + coupon.cname + "</font>"));
        dialog.show();
    }

    private void getOrderUseUUID(String uuid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "coswipe");
        params.put("uid", uuid);
        params.put("mobile", TCBApp.mMobile);
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "nfchandle.do", params);
        GsonRequest<Order> request = new GsonRequest<>(url, Order.class,
                new Response.Listener<Order>() {

                    @Override
                    public void onResponse(Order order) {
                        if (order != null
                                && !TextUtils.isEmpty(order.getOrderid())) {
                            showOrderFragmentDialog(order);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "未查询到订单信息！", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof ParseError
                        && error.networkResponse != null) {
                    try {
                        String errMsg = new String(
                                error.networkResponse.data,
                                HttpHeaderParser
                                        .parseCharset(error.networkResponse.headers));
                        Toast.makeText(getApplicationContext(), errMsg,
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),
                                "数据错误！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "网络错误！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mOrder != null) {
            if (getSupportFragmentManager().findFragmentByTag(OrderFragment.class.getSimpleName()) == null) {
                OrderFragment fragment = new OrderFragment();
                Bundle args = new Bundle();
                args.putParcelable(OrderFragment.ARG_ORDER, mOrder);
                final boolean settle = mSettle;
                args.putBoolean(OrderFragment.ARG_SETTLE, settle);
                fragment.setArguments(args);
                fragment.show(getSupportFragmentManager(), OrderFragment.class.getSimpleName());
                mOrder = null;
                mSettle = false;
            }
        }
    }

    private void showOrderFragmentDialog(Order order) {
        final OrderFragment fragment = new OrderFragment();
        Bundle args = new Bundle();
        args.putParcelable(OrderFragment.ARG_ORDER, order);
        fragment.setArguments(args);

        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                fragment.show(getSupportFragmentManager(), OrderFragment.class.getSimpleName());
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        LogUtils.i("onPause");
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }

        mMapView.onPause();
        stopLocating();

        super.onPause();
    }

    private void stopLocating() {
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SUGGEST:
                switch (resultCode) {
                    case RESULT_OK:
                        String query = data.getStringExtra("keyword");
                        if (!TextUtils.isEmpty(query)) {
                            if (!TextUtils.isEmpty(this.city)) {
                                showProgressDialog("请稍候...", true, false);
                                mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        TCBApp.getAppContext().cancelPendingRequests("getAroundPark");
                                    }
                                });
                                mPoiSearch.searchInCity((new PoiCitySearchOption())
                                        .city(this.city).keyword(query).pageCapacity(1));
                            } else {
                                Toast.makeText(MapActivity.this, "尚未定位到所处城市，请稍后再试...", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                }
                break;
            case REQUEST_ENABLE_BT:
                showIbeaconWarnDialog();
                connectToService();
                break;
//            case REQUEST_CODE_QRSCAN:
//                if (resultCode == RESULT_OK) {
//                    onNewIntent(data);
//                }
//                break;
            case REQUEST_CODE_CAMERA:
                if (resultCode == RESULT_OK) {
                    mMarkerManager.hideInfoWindow();

                    // 存储停车标记信息到本地
                    initRemarkSharedPrefs();

                    mMarkerManager.addRemarkMarker();

                    startRemarkActivity();
                }
                break;
            case REQUEST_CODE_UPDATE_REMARK_LOCATION:
                mMarkerManager.addRemarkMarker();
                mMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(mMarkerManager.getRemarkMarker().getPosition()));
                mMarkerManager.showRemarkPositionInfoWindow();
                break;
            default:
                break;
        }
    }

    private void initRemarkSharedPrefs() {
        String delete = getString(R.string.sp_remark_delete);
        String located = getString(R.string.sp_remark_located);
        String floor = getString(R.string.sp_remark_floor);
        String time = getString(R.string.sp_remark_time);
        String lat = getString(R.string.sp_remark_latitude);
        String lng = getString(R.string.sp_remark_longitude);
        String tips = getString(R.string.sp_remark_tips);
        TCBApp.getAppContext().getConfigPrefs().edit()
                .putBoolean(delete, false)
                .putBoolean(located, false)
                .putInt(floor, -1)
                .putLong(time, System.currentTimeMillis())
                .putString(lat, String.valueOf(TCBApp.mLocation.latitude))
                .putString(lng, String.valueOf(TCBApp.mLocation.longitude))
                .putString(tips, "")
                .commit();
    }

    public void startRemarkActivity() {
        Intent intent = new Intent(getApplicationContext(), RemarkActivity.class);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_REMARK_LOCATION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 取消下载通知ID
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        manager.cancel(UpdateManager.NOTIFICATION_ID);

        // 存储本次位置信息到本地
        if (TCBApp.mLocation != null) {
            TCBApp.getAppContext().saveString(R.string.sp_last_latitude, String.valueOf(TCBApp.mLocation.latitude));
            TCBApp.getAppContext().saveString(R.string.sp_last_longitude, String.valueOf(TCBApp.mLocation.longitude));
        }

        if (mLocationClient != null) {
            mLocationClient.stop();
            mLocationClient.unRegisterLocationListener(this);
        }

        mMapView.onDestroy();
        MarkerManager.onDestory();

        disableGps();

        if (mPoiSearch != null) {
            mPoiSearch.destroy();
            mPoiSearch = null;
        }

        // 释放ImageLoader资源
        ImageLoader imageLoader = ImageLoader.getInstance();
        if (imageLoader != null && imageLoader.isInited()) {
            imageLoader.destroy();
            imageLoader = null;
        }

//        TimerUtils.stopPollingService(this);

        mIMManager.destoryHX();
        TCBApp.getAppContext().cancelPendingRequests(this);
        TCBApp.getAppContext().cancelPendingRequests("getAroundPark");

//        //换一个位置，防止destory之后，调用百度地图相关方法。
//        mMapView.onDestroy();

        LogUtils.i(getClass(), "--->> MapActivity destoryed <<---");
        if (switchServerUrl) {
            System.exit(0);
        }
    }

    private void disableGps() {
        try {
            Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
            intent.putExtra("disabled", true);
            MapActivity.this.sendBroadcast(intent);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:

                    // 如果此时侧滑菜单是开启状态，则关闭侧滑菜单
                    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    }
                    if ((System.currentTimeMillis() - exitTime) > 2000) {
                        mMarkerManager.hideInfoWindow();

//                        ToastUtils.show("再按一次退出停车宝");
                        Toast.makeText(MapActivity.this, "再按一次退出停车宝",Toast.LENGTH_SHORT).show();
                        exitTime = System.currentTimeMillis();
                    } else {
                        //这里退出环信会出问题。
//                        HXSDKHelper.getInstance().logout(true, new EMCallBackAdapter() {
//                            @Override
//                            public void onSuccess() {
//                                LogUtils.w("退出环信成功");
//                            }
//                        });
                        finish();
                    }
                    return true;
                case KeyEvent.KEYCODE_MENU:
                    changeDrawerLayoutState();
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        mDrawerLayout.closeDrawers();
        switch (v.getId()) {
            case R.id.ib_map_location:

                // 移动地图中心到“我的位置”
                mMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(
                        TCBApp.mLocation, MAP_ZOOM_DEFAULT));
                v.setVisibility(View.GONE);

                getAroundParks(TCBApp.mLocation);
                break;
            case R.id.ib_map_remark:

                // 移动地图到停车标记的位置
                mMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(
                        mMarkerManager.getRemarkMarker().getPosition(), MAP_ZOOM_DEFAULT));
                mMarkerManager.showRemarkPositionInfoWindow();
                v.setVisibility(View.GONE);
                break;
            case R.id.ib_map_order:
                onOrderButtonClicked();
                break;
            case R.id.tv_map_payable:
                onPayableSwitcherClicked();
                break;
            case R.id.tv_map_all:
                onAllSwitcherClicked();
                break;
            case R.id.tv_map_ugc:
                onUGCButtonClicked();
                break;
            case R.id.tv_map_ugc_close:
                onUGCCloseButtonClicked();
                break;
            case R.id.et_map_search:
                onSearchButtonClicked();
                break;
            case R.id.ib_map_menu:
                onBottomMenuViewClicked();
                break;
            case R.id.btn_map_menu1:
                onMenuItem1Clicked();
                break;
            case R.id.btn_map_menu2:
                onMenuItem2Clicked();
                break;
            case R.id.btn_map_menu3:
                onMenuItem3Clicked();
                break;
        }
    }

    private void onMenuItem1Clicked() {
        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            Toast.makeText(this, "请先登录！", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        startActivity(new Intent(this, MyRedPacketsActivity.class));
    }

    private void onMenuItem2Clicked() {
        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            Toast.makeText(this, "请先登录！", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        Intent intent = new Intent(TCBApp.getAppContext(), X5WebActivity.class);
        intent.putExtra(WebActivity.ARG_TITLE, "打灰机");// 标题改变时会影响游戏界面逻辑，请参考WebActivity
//        intent.putExtra(WebActivity.ARG_URL, TCBApp.mServerUrl + "cargame.do?action=playgame&mobile=" + TCBApp.mMobile);
        intent.putExtra(WebActivity.ARG_URL, TCBApp.mServerUrl + "flygame.do?action=pregame&mobile=" + TCBApp.mMobile);
        startActivity(intent);
    }

    private void onMenuItem3Clicked() {
        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            Toast.makeText(this, "请先登录！", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        Intent intent = new Intent(TCBApp.getAppContext(), MainActivity.class);
        intent.putExtra(MainActivity.ARG_FRAGMENT, MainActivity.FRAGMENT_CHOOSE_COLLECTOR);
        Bundle args = new Bundle();
        // "-1"表示查看最近收费员列表
        args.putString(ChooseParkingFeeCollectorFragment.ARG_PARK_ID, "-1");
        args.putString(ChooseParkingFeeCollectorFragment.ARG_PARK_NAME, "");
        intent.putExtra(MainActivity.ARG_FRAGMENT_ARGS, args);
        startActivity(intent);
    }

    private void onBottomMenuViewClicked() {

        if (!mMenuButton.isClickable()) {
            // 屏蔽用户长按屏幕时，菜单晃动的问题（即动画播放结束前，屏蔽再一次播放）
            return;
        }

        int maxHeight = DensityUtils.dip2px(this, 144);
        Animator.AnimatorListener listener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mMenuButton.setClickable(true);

                // 重新设置菜单按钮图片
                int imageResId = mBottomMenuView.getHeight() > 72 ? R.drawable.ic_map_menu_close : R.drawable.ic_map_menu_open_normal;
                mMenuButton.setImageResource(imageResId);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mMenuButton.setClickable(false);
            }
        };

//        int duration = 500;
        int duration = 200;
        if (mBottomMenuView.getHeight() > 72) {
            // 隐藏菜单布局
            ViewUtils.performAnimate(mBottomMenuView, false, maxHeight, 0, duration, listener);
        } else {

            // 显示菜单布局
            ViewUtils.performAnimate(mBottomMenuView, false, 0, maxHeight, duration, listener);
        }
    }

    private void onUGCCloseButtonClicked() {
        ViewUtils.performAnimate(mUGCView, false, mUGCView.getHeight(),
                DensityUtils.dip2px(this, 0.5f), 500, null);
    }

    private void onOrderButtonClicked() {

        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            Toast.makeText(MapActivity.this, "请先登录！", Toast.LENGTH_SHORT).show();
            openDrawer();
            return;
        }

        new OrderFragment().show(getSupportFragmentManager(), OrderFragment.class.getSimpleName());
    }

    private void onSearchButtonClicked() {
        Intent intent = new Intent(TCBApp.getAppContext(), SearchActivity.class);
        intent.putExtra(SearchActivity.ARG_CITY, city);
        startActivityForResult(intent, REQUEST_CODE_SUGGEST);
    }

    private void onUGCButtonClicked() {

        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            Toast.makeText(this, "请先登录！", Toast.LENGTH_SHORT).show();
            startLoginActivity();
            return;
        }
        String text = mUGCLeftButton.getText().toString();
        if (text.contains("充值")) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.ARG_FRAGMENT, MainActivity.FRAGMENT_RECHARGE);
            Bundle args = new Bundle();
            args.putParcelable(RechargeFragment.ARG_WHO, (Parcelable) mUGCLeftButton.getTag());
            intent.putExtra(MainActivity.ARG_FRAGMENT_ARGS, args);
            startActivity(intent);
            return;
        }

        // 上传车场
        Intent intent = new Intent(getApplicationContext(), UGCActivity.class);
        startActivity(intent);
    }

    // "全部车场"选择器的点击事件
    private void onAllSwitcherClicked() {

        if (MarkerManager.MODE_SHOW_ALL == mMarkerManager.getShowingMode()) {
            return;
        }
        // tvPayable.setText("可支付车场");
        // tvPayable.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvPayable.setCompoundDrawablesWithIntrinsicBounds(0,
                R.drawable.ic_switcher_default, 0, 0);
        // tvAll.setText("显示全部车场");
        // tvAll.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvAll.setCompoundDrawablesWithIntrinsicBounds(0,
                R.drawable.ic_switcher_all, 0, 0);
        mMarkerManager.showAllMarker();
    }

    // "可支付车场"选择器的点击事件
    private void onPayableSwitcherClicked() {

        if (MarkerManager.MODE_SHOW_PAYABLE == mMarkerManager.getShowingMode()) {
            return;
        }

        // tvPayable.setText("显示可支付车场");
        // tvPayable.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvPayable.setCompoundDrawablesWithIntrinsicBounds(0,
                R.drawable.ic_switcher_payable, 0, 0);
        // tvAll.setText("全部车场");
        // tvAll.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvAll.setCompoundDrawablesWithIntrinsicBounds(0,
                R.drawable.ic_switcher_default, 0, 0);
        mMarkerManager.showPayableMarker();
    }

    /**
     * “我的位置”图层的点击事件
     */
    @Override
    public boolean onMyLocationClick() {
        mMarkerManager.showRemarkInfoWindow();
        return true;
    }

    public void takePhoto(File remarkImage) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // path为保存图片的路径，执行完拍照以后能保存到指定的路径下
        Uri imageUri = Uri.fromFile(remarkImage);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    /**
     * 地图加载完成的回调
     */
    @Override
    public void onMapLoaded() {
        if (TCBApp.mLocation != null) {
            mMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(
                    TCBApp.mLocation, MAP_ZOOM_DEFAULT));
            mMarkerManager.showInfoWindow("正在加载周围车场...");
            getAroundParks(TCBApp.mLocation);
        } else {
            String lat = TCBApp.getAppContext().readString(R.string.sp_last_latitude, "39.915182");
            String lng = TCBApp.getAppContext().readString(R.string.sp_last_longitude, "116.403876");
            double latDouble = new BigDecimal(lat).doubleValue();
            double lngDouble = new BigDecimal(lng).doubleValue();
            mMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(
                    new LatLng(latDouble, lngDouble), MAP_ZOOM_DEFAULT));
        }
        mMapView.setVisibility(View.VISIBLE);

        // 添加停车位置标记Marker
//        mMarkerManager.addRemarkMarker();

        // 获取有无最新活动
        checkIfHaveFestivalBonus();

        // 友盟自动检查更新
        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.update(TCBApp.getAppContext());
        // new UpdateManager(this).checkUpdate();
    }

    /**
     * 地图的单击点击事件
     */
    @Override
    public void onMapClick(LatLng position) {

        // hideBottomMenu();// 放OnTouchEven()中统一处理

        if (mMap != null && mMarkerManager != null) {
            switch (mMarkerManager.getInfoWindowType()) {
                case MarkerManager.INFOWINDOW_PARK:
                    if (mMarkerManager.getRemarkMarker() != null) {
                        mMarkerManager.showRemarkPositionInfoWindow();
                    } else {
                        mMarkerManager.showRemarkInfoWindow();
                    }
                    break;
                default:
                    mMarkerManager.hideInfoWindow();
                    break;
            }
        }
    }

    /**
     * 地图上搜索的兴趣点的点击事件
     */
    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    @Override
    public void onMapStatusChange(MapStatus arg0) {
    }

    /**
     * 地图状态改变完成回调
     */
    @Override
    public void onMapStatusChangeFinish(MapStatus newStatus) {

        // hideBottomMenu();// 放OnTouchEven()中统一处理

        // 判断是否显示定位图标
        if (newStatus.zoom < 15
                || !newStatus.bound.contains(TCBApp.mLocation)) {// 地图缩放级别比较小时显示
            ibLocation.setVisibility(View.VISIBLE);
        } else {
            ibLocation.setVisibility(View.GONE);
        }

        // 判断是否显示停车标记图标
        if (mMarkerManager.getRemarkMarker() != null) {
            if (newStatus.zoom < 15
                    || !newStatus.bound.contains(mMarkerManager.getRemarkMarker().getPosition())) {// 地图缩放级别比较小时显示
                ibRemark.setVisibility(View.VISIBLE);
            } else {
                ibRemark.setVisibility(View.GONE);
            }
        }
    }

    private void hideBottomMenu() {
        // 如果底部菜单处于显示状态，则隐藏
        if (mBottomMenuView.getHeight() > 72) {
            onBottomMenuViewClicked();
        }
    }

    /**
     * 地图状态改变开始回调
     */
    @Override
    public void onMapStatusChangeStart(MapStatus arg0) {
    }

    private int first = 0;// 第一次回调

    @Override
    public void onReceiveLocation(BDLocation bDlocation) {

        // 定位不在中国则视为错误的定位
        if (!"0".equals(bDlocation.getCountryCode()) || mMapView == null) {
            LogUtils.e(getClass(), "--->> wrong country: " + bDlocation.getCountry());
            return;
        }
        if (first < 4) {
            first++;
        }
        // bDlocation.setRadius(500);// 设置“我的位置”周围圆圈半径为500米
        MyLocationData mLocationData = new MyLocationData.Builder()
                .latitude(bDlocation.getLatitude())
                .longitude(bDlocation.getLongitude()).accuracy(0)
                .direction(bDlocation.getDirection())// 不需要方向信息
                .build();

        mMap.setMyLocationData(mLocationData);

        if (TextUtils.isEmpty(city)) {

            //第一次加载
            if (TCBApp.mLocation == null) {

                TCBApp.mLocation = new LatLng(bDlocation.getLatitude(),
                        bDlocation.getLongitude());

                mMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(TCBApp.mLocation,
                        MAP_ZOOM_DEFAULT));

                if (mMapView != null && View.VISIBLE == mMapView.getVisibility()) {
                    mMarkerManager.showInfoWindow("正在加载周围车场...");
                    getAroundParks(TCBApp.mLocation);
                }
            }

            // showRecommendInfoWindow();
            MapActivity.this.city = bDlocation.getCity();

            // 下载离线地图
            String cityCode = bDlocation.getCityCode();
            LogUtils.i(MapActivity.class, "cityCode: --->> " + cityCode);
            int cityId = -1;
            if (!TextUtils.isEmpty(cityCode)
                    && TextUtils.isDigitsOnly(cityCode)) {

                // 首次定位成功后，修改定位间隔为1分钟
//                mLocationClient.getLocOption().setScanSpan(60000);

                cityId = Integer.parseInt(cityCode);
            }

            // 下载离线地图
            if (cityId != -1) {
                updateOfflineMap(cityId);
            }
        }

        TCBApp.mLocation = new LatLng(bDlocation.getLatitude(),
                bDlocation.getLongitude());

        // 第一次打开程序，直接传经纬度查询附近蓝牙车场；
        if (first == 1 && hasBluetooth) {
            LogUtils.i(getClass(), ": --->>第一次打开程序，直接传经纬度查询附近蓝牙车场");
//            QueryIbeaconParking(bDlocation.getLongitude(), bDlocation.getLatitude());
            mLastLocation = new LatLng(bDlocation.getLatitude(), bDlocation.getLongitude());
        } else {
            if (first > 3 && hasBluetooth && !closeQueryLocation) {
                if (canCompare) {
                    double distances = DistanceUtil.getDistance(mLastLocation, TCBApp.mLocation);
//                    LogUtils.i(getClass(), ": --->>我走的距离=" + distances
//                            + "目标距离=" + mDistance);
                    if (distances >= mDistance) {
//                        QueryIbeaconParking(bDlocation.getLongitude(), bDlocation.getLatitude());
                    }
                } else {
//                    QueryIbeaconParking(bDlocation.getLongitude(), bDlocation.getLatitude());
                }
            }
        }
    }

    // 蓝牙未开启弹框提醒；

    private void openBluetoothDialog() {
        View open_dialog_view = View.inflate(this,
                R.layout.dialog_open_bluetooth, null);
        TextView tv_open = (TextView) open_dialog_view
                .findViewById(R.id.tv_dialog_open_buletooth_install);
        TextView tv_cancle = (TextView) open_dialog_view
                .findViewById(R.id.tv_dialog_open_buletooth_cancle);
        final Dialog openDialog = new Builder(this).create();
        openDialog.setCancelable(false);
        tv_open.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                MapActivity.this.startActivityForResult(enableBtIntent,
                        REQUEST_ENABLE_BT);
                openDialog.dismiss();
            }
        });
        tv_cancle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();
            }
        });
        openDialog.show();
        openDialog.setContentView(open_dialog_view);
    }

    // 根据当前位置访问服务器，查周围200米是否有蓝牙车场；有就返回距离，没有就返回200；if距离小于30米且蓝牙没开--就提醒用户打开蓝牙体验Ibencon生成订单；
    // else提醒摇一摇生成订单； ibeacon位置lng=116.306970&lat=40.042474
    // 古塘咖啡116.31356,40.042301
    private void QueryIbeaconParking(final Double Longitude,
                                     final Double Latitude) {
        // http://192.168.199.240/zld/carservice.do?action=scanibeacon&lng=116.306970&lat=40.042474
        String url = TCBApp.mServerUrl + "carservice.do?action=scanibeacon&lng="
                + Longitude + "&lat=" + Latitude;
        LogUtils.i(this.getClass(), "查周围200米是否有蓝牙车场 url： --->> " + url);
        StringRequest request = new StringRequest(url, new Listener<String>() {
            @Override
            public void onResponse(String object) {
                if (!TextUtils.isEmpty(object)) {
                    try {
                        LogUtils.i(this.getClass(), "查询蓝牙车场的距离是： --->> " + object);
                        int distance = Integer.parseInt(object);
                        if (distance <= 200) {// 小于30米说明就在车场门口可以提示摇一摇生成订单；
                            LogUtils.i(getClass(), "我的位置在蓝牙车场入口--->>");
                            closeQueryLocation = true;
                            if (!TextUtils.isEmpty(TCBApp.mMobile)) {
                                mShakeListener.start();
                                isOpenSharkLisener = true;// 已经打开过摇一摇监听
                            }
                            if (!beaconManager.isBluetoothEnabled()) { // 蓝牙没打开，提醒用户去开蓝牙；
                                openBluetoothDialog();
                            }
                            connectToService(); // 开始搜索周围ibeacon设备；
                        } else {
                            mDistance = distance;
                            canCompare = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, null);
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    // 第一次打开蓝牙后提示摇一摇操作示意图；
    public void showIbeaconWarnDialog() {
        AlertDialog qrCodeDialog = new AlertDialog.Builder(this).create();
        ImageView qrCode = new ImageView(this);
        qrCode.setScaleType(ScaleType.FIT_XY);
        qrCode.setImageResource(R.drawable.dialog_yaoyiyao_big);
        qrCodeDialog.setView(qrCode);
        qrCodeDialog.setCancelable(true);
        qrCodeDialog.setCanceledOnTouchOutside(false);
        qrCodeDialog.setButton(AlertDialog.BUTTON_POSITIVE, "知道了",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        qrCodeDialog.show();
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = (int) (displayMetrics.heightPixels * 0.7); // 高度设置为屏幕的0.6
        int width = (int) (displayMetrics.widthPixels * 0.9); // 宽度设置为屏幕的0.65
        qrCodeDialog.getWindow().setLayout(width, height);
    }

    public static class MapHandler extends Handler {

        private SoftReference<MapActivity> mActivity;

        public MapHandler(MapActivity activity) {
            mActivity = new SoftReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            switch (msg.what) {
                // case MSG_WHAT_QRSCAN:
                // mActivity.get().startActivityForResult(
                // new Intent(TCBApp.getAppContext(),
                // CaptureActivity.class), REQUEST_CODE_QRSCAN);
                // break;
                case MSG_WHAT_HANDLER_ORDER:
                    Order order = (Order) msg.obj;
                    if (order != null && !TextUtils.isEmpty(order.getOrderid()) && !Order.STATE_PAYED.equals(order.getState())) {
                        mActivity.get().showOrderFragmentDialog(order);
                    }
                    break;
                case MSG_WHAT_IBEACON_PAY_SUCCESS:// ibeacon自动支付成功的消息；
                    if (mActivity.get().makeorderDialog != null
                            && mActivity.get().makeorderDialog.isShowing()) {
//                        TimerUtils.stopPollingService(mActivity.get());
                        mActivity.get().showMakeOrder(8, "");
                    }
                    break;
                case MSG_WHAT_IBEACON_PAY_FAIL:// ibeacon自动支付失败的消息；
                    if (mActivity.get().makeorderDialog != null
                            && mActivity.get().makeorderDialog.isShowing()) {
//                        TimerUtils.stopPollingService(mActivity.get());
                        mActivity.get().showMakeOrder(9, "");
                    }
                    break;
            }
            super.handleMessage(msg);
        }

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {
        if (result.error != SearchResult.ERRORNO.NO_ERROR)
            LogUtils.i(MapActivity.class,
                    "onGetPoiDetailResult(PoiDetailResult: 无此结果");
        else
            LogUtils.i(MapActivity.class,
                    "onGetPoiDetailResult(PoiDetailResult: 查看详情");
    }

    @Override
    public void onGetPoiResult(final PoiResult poiResult) {
        if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
            PoiInfo poiInfo = poiResult.getAllPoi().get(0);
            LatLng poiLatLng = poiInfo.location;
            if (poiLatLng == null) {
                dismissProgressDialog();
                Toast.makeText(MapActivity.this, "目的地不明确，请重新搜索！", Toast.LENGTH_LONG)
                        .show();
                return;
            }

            mMarkerManager.updatePOIMarker(poiInfo);

            // 获取附近推荐车场
            getAroundParks(poiLatLng);
        } else if (poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            dismissProgressDialog();
            Toast.makeText(MapActivity.this, "没有任何查询结果！", Toast.LENGTH_SHORT)
                    .show();
        } else if (poiResult.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
            dismissProgressDialog();
            Toast.makeText(MapActivity.this, "关键字有歧义，请输入更详细的关键字！", Toast.LENGTH_LONG)
                    .show();
        } else {
            dismissProgressDialog();
            Toast.makeText(MapActivity.this, "搜索出错了！", Toast.LENGTH_SHORT)
                    .show();
        }
    }

}