package com.tq.zld.service;

import java.util.HashMap;

import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.Order;
import com.tq.zld.bean.PayResult;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.TimerUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.map.MapActivity;
import com.tq.zld.wxapi.WXPayEntryActivity;

/**
 * 使用AlarmManager实现轮询，API 19以后，由于系统实现所有repeat方式的Alarm唤醒时间不固定，故此方法轮询带有很大不确定性。
 * 已废弃。
 */
@Deprecated
public class ObtainMsgService extends Service {

    public static final String MSG_TYPE_ORDER = "0";// 订单消息
    public static final String MSG_TYPE_BOOK = "1";// 预定消息
    public static final String MSG_TYPE_PAY = "2";// 支付消息
    public static final String IBEACON_PAY_SUCCESS = "9";// ibeacon支付成功
    public static final int IBEACON_PAY_FAIL = 8;// ibeacon支付失败

    private JsonObjectRequest request;

    private String mMessageUrl;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMessageUrl = initMessageUrl();
    }

    private String initMessageUrl() {
        return TCBApp.mServerUrl.replace("zld", "mserver");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i(getClass(),
                "------------->> start ObtainMsgService <<-------------");

        if (request == null) {
            HashMap<String, String> params = new HashMap<>();
            params.put("mobile", TCBApp.mMobile);
            // params.put("action", "mesg");
            String url = URLUtils.genUrl(mMessageUrl + "carmessage.do",
                    params);
            request = new JsonObjectRequest(url, new Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    if (response != null) {
                        try {
                            LogUtils.i(
                                    ObtainMsgService.class,
                                    "obtainMsg result: --->> "
                                            + response.toString());

                            String type = null;
                            if (response.has("mtype")) {
                                type = response.getString("mtype");
                            }
                            if (!TextUtils.isEmpty(type)) {

                                // 停止轮询
//                                TimerUtils
//                                        .stopPollingService(ObtainMsgService.this);

                                String info = response.getString("info");

                                switch (type) {
                                    case MSG_TYPE_PAY:
                                        // 充值或者包月产品支付成功
                                        // String ptype = response
                                        // .getString("ibeacon");
                                        // String result = response
                                        // .getString("result");
                                        // if ("1".equals(ptype)) {
                                        //
                                        // // IBeacon订单
                                        // if (MapActivity.mHandler != null) {
                                        // int what = 9;
                                        // if (PayResult.PAY_RESULT_FAILED
                                        // .equals(result)) {
                                        // what = 8;
                                        // }
                                        // Message.obtain(
                                        // MapActivity.mHandler, what,
                                        // null);
                                        // }
                                        // } else {

                                        // 普通订单
                                        if (WXPayEntryActivity.mPayResultHandler != null) {
                                            PayResult result = new Gson().fromJson(
                                                    info, PayResult.class);
                                            Message.obtain(
                                                    WXPayEntryActivity.mPayResultHandler,
                                                    0, result).sendToTarget();
                                        }
                                        // }
                                        break;
                                    case MSG_TYPE_ORDER:
                                        // 订单支付成功
                                        if (WXPayEntryActivity.mPayResultHandler != null) {
                                            Message.obtain(
                                                    WXPayEntryActivity.mPayResultHandler,
                                                    0,
                                                    new Gson().fromJson(info,
                                                            Order.class))
                                                    .sendToTarget();
                                        }
                                        break;
                                    case IBEACON_PAY_SUCCESS:
                                        // IBeacon订单
                                        if (MapActivity.mHandler != null) {

                                            Order order = new Gson().fromJson(info,
                                                    Order.class);
                                            String state = order.getState();

                                            int what = 9;
                                            if (Order.STATE_PAY_FAILED
                                                    .equals(state)
                                                    || Order.STATE_PAYING
                                                    .equals(state)) {
                                                // 支付失败
                                                what = 8;
                                            }
                                            Message.obtain(MapActivity.mHandler,
                                                    what, null).sendToTarget();
                                        }
                                        break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, null);
        }
        TCBApp.getAppContext().addToRequestQueue(request, this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.i(getClass(),
                "------------->> destory ObtainMsgService <<-------------");
        TCBApp.getAppContext().cancelPendingRequests(this);
    }
}