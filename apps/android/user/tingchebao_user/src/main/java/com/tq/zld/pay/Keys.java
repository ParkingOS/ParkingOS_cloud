package com.tq.zld.pay;

import com.tq.zld.BuildConfig;
import com.tq.zld.R;
import com.tq.zld.TCBApp;

public final class Keys {
    static {
        System.loadLibrary("tingchebao_user");
    }

    // ----------------------微信支付Key--------------------------------------
    public static final String WXPAY_APPID = "wx73454d7f61f862a5";
    public static final String WXPAY_PARTNERID = "1220886701";

    // 支付宝公钥
    public static final String ALIPAY_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

    public native String getPrivate();

    public native String getPartner();

    public native String getSeller();

    private native String getNotifyUrl();

    // 测试：http://yxiudongyeahnet.vicp.cc/zld/payhandle
    private String getNotifyUrl2() {
        String localUrl = TCBApp.getAppContext().getString(R.string.url_local);
        if (TCBApp.mServerUrl.equals(localUrl)) {
            return "http://yxiudongyeahnet.vicp.cc/zld/payhandle";
        }
        return getNotifyUrl();
    }

    public String getAliPayNotifyUrl() {
        switch (BuildConfig.BUILD_TYPE) {
            case "debug":
            case "alpha":
                return getNotifyUrl2();

            default:
                return getNotifyUrl();
        }
    }
}
