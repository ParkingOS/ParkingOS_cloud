package com.tq.zld.view.map;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.Coupon;
import com.tq.zld.bean.Order;
import com.tq.zld.bean.ParkingFeeCollector;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.fragment.ChooseParkingFeeCollectorFragment;
import com.tq.zld.view.fragment.InputMoneyFragment;
import com.tq.zld.wxapi.WXPayEntryActivity;
import com.zbar.lib.camera.CameraManager;
import com.zbar.lib.decode.CaptureActivityHandler;
import com.zbar.lib.decode.InactivityTimer;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * 作者: 陈涛(1076559197@qq.com)
 * <p/>
 * 时间: 2014年5月9日 下午12:25:31
 * <p/>
 * 版本: V_1.0.0
 * <p/>
 * 描述: 扫描界面
 */
public class CaptureActivity extends BaseActivity implements Callback,
        ErrorListener {

    private CaptureActivityHandler handler;
    private boolean hasSurface;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.50f;
    private boolean vibrate;
    private int x = 0;
    private int y = 0;
    private int cropWidth = 0;
    private int cropHeight = 0;
    private RelativeLayout mContainer = null;
    private RelativeLayout mCropLayout = null;
    private boolean isNeedCapture = false;

    private TextView tvTip;

    public boolean isNeedCapture() {
        return isNeedCapture;
    }

    public void setNeedCapture(boolean isNeedCapture) {
        this.isNeedCapture = isNeedCapture;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getCropWidth() {
        return cropWidth;
    }

    public void setCropWidth(int cropWidth) {
        this.cropWidth = cropWidth;
    }

    public int getCropHeight() {
        return cropHeight;
    }

    public void setCropHeight(int cropHeight) {
        this.cropHeight = cropHeight;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_capture);
        initToolbar();

        // 初始化 CameraManager
        CameraManager.init(getApplication());
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        mContainer = (RelativeLayout) findViewById(R.id.capture_containter);
        mCropLayout = (RelativeLayout) findViewById(R.id.capture_crop_layout);

        tvTip = (TextView) findViewById(R.id.tv_qrscan_tip);

        ImageView mQrLineView = (ImageView) findViewById(R.id.capture_scan_line);
        TranslateAnimation mAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE,
                0f, TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.9f);
        mAnimation.setDuration(1500);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());
        mQrLineView.setAnimation(mAnimation);
    }

    private void initToolbar() {
        Toolbar bar = (Toolbar) findViewById(R.id.toolbar_capture);
        bar.setTitle("");
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bar.setNavigationOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    boolean flag = true;

    protected void light() {
        if (flag) {
            flag = false;
            // 开闪光灯
            CameraManager.get().openLight();
        } else {
            flag = true;
            // 关闪光灯
            CameraManager.get().offLight();
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.capture_preview);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
        TCBApp.getAppContext().cancelPendingRequests(this);
    }

    /**
     * 处理扫描结果
     *
     * @param result
     */
    public void handleDecode(String result) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        LogUtils.i("QR_Scan result: --->> " + result);
        String regularExpression = "http://www.tingchebao.com";
        String regularExpression2 = TCBApp.mServerUrl + "qr/c/";

        if (!TextUtils.isEmpty(result) && result.startsWith(regularExpression)) {
            resolveScanResult(result);
        } else if (!TextUtils.isEmpty(result) && result.startsWith(regularExpression2)) {
            resolveScanResult2(result);
        } else {
            Toast.makeText(this, "请扫描停车宝专用停车卡！", Toast.LENGTH_LONG).show();
            reScan();
        }
    }

    private void reScan() {
        tvTip.setText("点击重新扫描");
        tvTip.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 连续扫描，不发送此消息扫描一次结束后就不能再次扫描
                handler.sendEmptyMessage(R.id.restart_preview);
                tvTip.setText(getString(R.string.scan_tips));
            }
        });
    }

    private void resolveScanResult2(String result) {
        showProgressDialog("请稍候...", true, false);
        String url = result + "&mobile=" + TCBApp.mMobile;
        JsonObjectRequest request = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        dismissProgressDialog();
                        if (response != null) {
                            LogUtils.i("QRScan response: --->> " + response.toString());
                            try {
                                String type = response.getString("type");
                                if (!TextUtils.isEmpty(type)) {
                                    String info = response.getString("info");
                                    Gson gson = new Gson();
                                    switch (type) {
                                        case "1":// 直付
                                            // 收费员
                                            ParkingFeeCollector collector = gson.fromJson(info,
                                                    ParkingFeeCollector.class);
                                            handleScanResult(collector);
                                            break;
                                        case "2":// 查询订单
                                            // 订单数据
                                            Order order = gson.fromJson(info, Order.class);
                                            handleScanResult(order);
                                            break;
                                        case "3"://领取专用券
                                            Coupon coupon = gson.fromJson(info, Coupon.class);
                                            handleScanResult(coupon);
                                            break;
                                        case "4"://入场
                                            //{"type":"4","info":{"cid":"121487"}}
                                            JSONObject infoObject = response.getJSONObject("info");
                                            if (infoObject != null && infoObject.has("cid")) {
                                                String cid = infoObject.getString("cid");
                                                handleScanEnterPark(cid);
                                            }
                                            break;
                                        case "5"://结算
                                            handleScanOrderSettle(gson.fromJson(info, Order.class));
                                            break;
                                        default:
                                            break;
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "数据错误！", Toast.LENGTH_SHORT).show();
                                    reScan();
                                }
                            } catch (Exception e) {
                                reScan();
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "网络错误！",
                                    Toast.LENGTH_SHORT).show();
                            reScan();
                        }
                    }
                }, this);
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    /**
     * 处理入场结果
     * @param cid
     */
    private void handleScanEnterPark(String cid){
        LogUtils.i("handleScanEnterPark cid:" + cid);
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("cid", cid);
        startActivity(intent);
    }

    private void handleScanOrderSettle(Order order){
        if (order == null) {
            Toast.makeText(getApplicationContext(), "该车位已经被占用",
                    Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("order", order);
        intent.putExtra("settle", true);
        startActivity(intent);
    }

    private void handleScanResult(Coupon coupon) {
        if (coupon != null && !TextUtils.isEmpty(coupon.id)) {
            switch (coupon.id) {
                case "-1":
                    Toast.makeText(getApplicationContext(), "该专用券已过期！", Toast.LENGTH_SHORT).show();
                    reScan();
                    break;
                case "-2":
                    Toast.makeText(getApplicationContext(), "该专用券已被领过了哦～", Toast.LENGTH_SHORT).show();
                    reScan();
                    break;
                default:
                    if (!coupon.id.startsWith("-")) {
                        startMapActivity(coupon);
                    } else {
                        // 出现了其他错误
                        Toast.makeText(getApplicationContext(), "专用券领取失败！",
                                Toast.LENGTH_SHORT).show();
                        reScan();
                    }
                    break;
            }
        } else {
            Toast.makeText(getApplicationContext(), "专用券领取失败！",
                    Toast.LENGTH_SHORT).show();
            reScan();
        }
    }

    private void handleScanResult(Order order) {
        if (order != null && !TextUtils.isEmpty(order.getOrderid())) {
            startMapActivity(order);
        } else {
            Toast.makeText(getApplicationContext(), "未查询到订单信息！", Toast.LENGTH_SHORT).show();
            reScan();
        }
    }

    private void handleScanResult(ParkingFeeCollector collector) {
        if (collector != null && !TextUtils.isEmpty(collector.id)) {
            if (!TextUtils.isEmpty(collector.total)) {
                // 直接支付
                startWXPayEntryActivity(collector);
            } else {
                // 跳转到输入金额界面
                startPayFeeActivity(collector);
            }
        } else {
            Toast.makeText(getApplicationContext(), "未查询到收费员信息！",
                    Toast.LENGTH_SHORT).show();
            reScan();
        }
    }

    private void startWXPayEntryActivity(ParkingFeeCollector collector) {
        Intent intent = new Intent(TCBApp.getAppContext(), WXPayEntryActivity.class);
        intent.putExtra(WXPayEntryActivity.ARG_PRODTYPE, WXPayEntryActivity.PROD_PAY_MONEY);
        intent.putExtra(WXPayEntryActivity.ARG_SUBJECT, "直付停车费_" + collector.name + "(账号: " + collector.id + ")");
        intent.putExtra(WXPayEntryActivity.ARG_TOTALFEE, collector.total);
        intent.putExtra(WXPayEntryActivity.ARG_ORDER_UID, collector.id);
        finish();
        startActivity(intent);
    }

    private void resolveScanResult(String result) {
        HashMap<String, String> params = URLUtils.getParameters(result);
        LogUtils.i(getClass(), "QR_Scan params: --->> " + params.toString());
        if (!TextUtils.isEmpty(params.get("pid"))) {
            getParkingFeeCollector(params.get("pid"));
        } else if (!TextUtils.isEmpty(params.get("nid"))) {
            getOrderUseNid(params.get("nid"));
        } else {
            Toast.makeText(this, "请扫描停车宝新版停车卡\n或车场端主页二维码付车费", Toast.LENGTH_LONG)
                    .show();
            reScan();
        }
    }

    private void getOrderUseNid(String nid) {
        showProgressDialog("请稍候...", true, false);
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "coswipe");
        params.put("nid", nid);
        params.put("mobile", TCBApp.mMobile);
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "nfchandle.do", params);
        GsonRequest<Order> request = new GsonRequest<>(url, Order.class,
                new Response.Listener<Order>() {

                    @Override
                    public void onResponse(Order order) {
                        dismissProgressDialog();
                        handleScanResult(order);
                    }
                }, this);
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    private void getParkingFeeCollector(String pid) {
        showProgressDialog("请稍候...", true, false);
        final HashMap<String, String> params = new HashMap<>();
        params.put("action", "getpkuser");
        params.put("uid", pid);
        params.put("mobile", TCBApp.mMobile);
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "carowner.do", params);
        JsonObjectRequest request = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        dismissProgressDialog();
                        if (response != null) {
                            LogUtils.i(ChooseParkingFeeCollectorFragment.class,
                                    "getParkingFeeCollector result: --->> "
                                            + response.toString());
                            try {
                                Gson gson = new Gson();
                                // 判断返回的是收费员还是订单
                                if (response.has("name")) {
                                    // 收费员
                                    ParkingFeeCollector collector = gson
                                            .fromJson(response.toString(),
                                                    ParkingFeeCollector.class);
                                    handleScanResult(collector);
                                } else {
                                    // 订单数据
                                    Order order = gson.fromJson(
                                            response.toString(), Order.class);
                                    handleScanResult(order);
                                }
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(),
                                        "解析错误！", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                                reScan();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "网络错误！",
                                    Toast.LENGTH_SHORT).show();
                            reScan();
                        }
                    }
                }, this);
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    private void startMapActivity(Order order) {
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("order", order);
        startActivity(intent);
        // Intent intent = new Intent();
        // intent.putExtra("order", order);
        // setResult(RESULT_OK, intent);
        // finish();
    }

    private void startMapActivity(Coupon coupon) {
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("coupon", coupon);
        startActivity(intent);
        // Intent intent = new Intent();
        // intent.putExtra("order", order);
        // setResult(RESULT_OK, intent);
        // finish();
    }

    private void startPayFeeActivity(ParkingFeeCollector collector) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.ARG_FRAGMENT, MainActivity.FRAGMENT_INPUT_MONEY);
        Bundle args = new Bundle();
        args.putParcelable(InputMoneyFragment.ARG_COLLECTOR, collector);
        intent.putExtra(MainActivity.ARG_FRAGMENT_ARGS, args);
        finish();
        startActivity(intent);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);

            Point point = CameraManager.get().getCameraResolution();
            int width = point.y;
            int height = point.x;

            int x = mCropLayout.getLeft() * width / mContainer.getWidth();
            int y = mCropLayout.getTop() * height / mContainer.getHeight();

            int cropWidth = mCropLayout.getWidth() * width
                    / mContainer.getWidth();
            int cropHeight = mCropLayout.getHeight() * height
                    / mContainer.getHeight();

            setX(x);
            setY(y);
            setCropWidth(cropWidth);
            setCropHeight(cropHeight);
            // 设置是否需要截图
            setNeedCapture(false);
        } catch (IOException | RuntimeException ioe) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(CaptureActivity.this);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public Handler getHandler() {
        return handler;
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        dismissProgressDialog();
        if (error instanceof ParseError && error.networkResponse != null) {
            try {
                String errMsg = new String(error.networkResponse.data,
                        HttpHeaderParser
                                .parseCharset(error.networkResponse.headers));
                Toast.makeText(getApplicationContext(), errMsg,
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "数据错误！",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "网络错误！", Toast.LENGTH_SHORT)
                    .show();
        }

    }
}