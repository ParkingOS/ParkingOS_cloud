package com.tq.zld.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class PayResult implements Parcelable {

    public static final String PAY_RESULT_FAILED = "0";// 服务器返回支付失败的消息
    public static final String PAY_RESULT_SUCCESS = "1";// 服务器返回支付成功的消息

    public String result;// 支付结果
    public String errmsg;// 失败原因
    public String tips;// 操作提示

    public int bonusid;// 充值成功是否有红包，0没有，1有

    public PayResult() {
    }

    public PayResult(String result, String errmsg, String tips) {
        this.result = result;
        this.errmsg = errmsg;
        this.tips = tips;
    }

    public PayResult(Parcel source) {
        this.result = source.readString();
        this.errmsg = source.readString();
        this.tips = source.readString();
    }

    @Override
    public String toString() {
        return "PayResult [result=" + result + ", errmsg=" + errmsg + ", tips="
                + tips + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(result);
        dest.writeString(errmsg);
        dest.writeString(tips);
    }

    public static final Creator<PayResult> CREATOR = new Creator<PayResult>() {

        @Override
        public PayResult[] newArray(int size) {
            return new PayResult[size];
        }

        @Override
        public PayResult createFromParcel(Parcel source) {
            return new PayResult(source);
        }
    };
}