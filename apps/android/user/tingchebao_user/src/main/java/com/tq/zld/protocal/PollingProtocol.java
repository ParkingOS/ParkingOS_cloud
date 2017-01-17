package com.tq.zld.protocal;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.Order;
import com.tq.zld.bean.PayResult;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.LoginActivity;
import com.tq.zld.view.map.MapActivity;
import com.tq.zld.wxapi.WXPayEntryActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Clare on 2015/5/27.
 * 利用Handler从服务器轮询取消息
 */
public class PollingProtocol implements Response.ErrorListener, Response.Listener<JSONObject> {

    public static final String MSG_TYPE_ORDER = "0";// 订单消息
    public static final String MSG_TYPE_BOOK = "1";// 预定消息
    public static final String MSG_TYPE_PAY = "2";// 支付消息
    public static final String MSG_TYPE_LOGIN = "3";// 支付消息
    public static final String IBEACON_PAY_SUCCESS = "9";// ibeacon支付成功
    public static final int IBEACON_PAY_FAIL = 8;// ibeacon支付失败

    private Handler mHandler;
    private JsonObjectRequest mRequest;

    private boolean stop;

    private Map<String, String> mParams;

    public PollingProtocol(Handler handler) {
        this(handler, null);
    }

    public PollingProtocol(Handler handler, Map<String, String> params) {
        this.mHandler = handler;
        if (params == null) {
            mParams = new HashMap<>();
            mParams.put("mobile", TCBApp.mMobile);
        } else {
            this.mParams = params;
        }
        String url = URLUtils.genUrl(TCBApp.mServerUrl.replace("zld", "mserver") + "carmessage.do", mParams);
        this.mRequest = new JsonObjectRequest(url, this, this);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        if (volleyError instanceof ServerError || volleyError instanceof AuthFailureError) {
            if (mHandler instanceof WXPayEntryActivity.PayResultHandler) {
                PayResult result = new PayResult(PayResult.PAY_RESULT_FAILED, "服务器出错了~", "请稍后再试~");
                Message.obtain(
                        mHandler,
                        WXPayEntryActivity.MSG_WHAT_PAYRESULT, result).sendToTarget();
            } else if (mHandler instanceof MapActivity.MapHandler) {
                Message.obtain(mHandler,
                        IBEACON_PAY_FAIL, null).sendToTarget();
            }
            return;
        }
        if (stop) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                startPolling();
            }
        });
    }

    @Override
    public void onResponse(JSONObject jsonObject) {
        try {
            LogUtils.i(PollingProtocol.class, "polling result: --->> " + jsonObject.toString());

            String type = null;
            if (jsonObject.has("mtype")) {
                type = jsonObject.getString("mtype");
            }
            if (!TextUtils.isEmpty(type)) {

                String info = jsonObject.getString("info");

                switch (type) {
                    case MSG_TYPE_PAY:
                        if (!(mHandler instanceof WXPayEntryActivity.PayResultHandler)) {
                            return;
                        }
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
                        PayResult result = new Gson().fromJson(
                                info, PayResult.class);
                        Message.obtain(mHandler,
                                WXPayEntryActivity.MSG_WHAT_PAYRESULT, result).sendToTarget();
                        stopPolling();
                        // }
                        break;
                    case MSG_TYPE_ORDER:

                        if (!(mHandler instanceof WXPayEntryActivity.PayResultHandler)) {
                            return;
                        }

                        // 仅当取到支付订单结果的消息，才通知WXPayEntryActivity
                        Order order = new Gson().fromJson(info, Order.class);
                        switch (order.getState()) {
                            case Order.STATE_PAY_FAILED:
                            case Order.STATE_PAYED:
                                Message.obtain(
                                        mHandler,
                                        WXPayEntryActivity.MSG_WHAT_PAYRESULT, order)
                                        .sendToTarget();
                                stopPolling();
                                break;
                            default:
                                break;
                        }

                        break;
                    case IBEACON_PAY_SUCCESS:
                        // IBeacon订单
                        if (!(mHandler instanceof MapActivity.MapHandler)) {
                            return;
                        }

                        Order ibeaconOrder = new Gson().fromJson(info,
                                Order.class);
                        String state = ibeaconOrder.getState();
                        int what = 9;
                        if (Order.STATE_PAY_FAILED.equals(state)
                                || Order.STATE_PAYING.equals(state)) {
                            // 支付失败
                            what = IBEACON_PAY_FAIL;
                        }
                        Message.obtain(mHandler,
                                what, null).sendToTarget();
                        stopPolling();
                        break;

                    case MSG_TYPE_LOGIN:
                        if (mHandler instanceof LoginActivity.LoginHandler) {
                            PayResult result1 = new Gson().fromJson(info, PayResult.class);
                            Message.obtain(mHandler, Integer.parseInt(result1.result), mParams.get("mobile")).sendToTarget();
                        }
                        stopPolling();
                        break;
                    default:
                        break;
                }
            } else {
                if (!stop) {
                    startPolling();
                }
            }
        } catch (Exception e) {
            if (!stop) {
                startPolling();
            }
            e.printStackTrace();
        }
    }

    /**
     * 开启轮询
     */
    public void startPolling() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TCBApp.getAppContext().addToRequestQueue(mRequest, this);
            }
        }, 1000);
    }

    /**
     * 停止轮询
     */
    public void stopPolling() {
        TCBApp.getAppContext().cancelPendingRequests(this);
        // mHandler.removeCallbacksAndMessages(null);
        this.stop = true;
    }
}
