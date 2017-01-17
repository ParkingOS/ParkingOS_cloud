package com.tq.zld.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Author：ClareChen
 * E-mail：ggchaifeng@gmail.com
 * Date：  15/7/9 下午5:23
 */
public class Plate implements Parcelable {

    /**
     * 未认证
     */
    public static final int STATE_CERTIFY = 0;
    /**
     * 已认证
     */
    public static final int STATE_CERTIFIED = 1;
    /**
     * 认证中
     */
    public static final int STATE_CERTIFYING = 2;
    /**
     * 认证失败
     */
    public static final int STATE_CERTIFY_FAILED = -1;
    /**
     * 无效车牌
     */
    public static final int STATE_CERTIFY_BLOCKED = -2;

    /**
     * 车牌号
     */
    public String car_number;

    /**
     * 当前车牌是否已认证
     * 0:未认证，1:已认证，2:认证中
     */
    public int is_auth;

    public int is_default;

    public Plate() {
    }

    public Plate(String plate, int state) {
        this.car_number = plate;
        this.is_auth = state;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.car_number);
        dest.writeInt(this.is_auth);
        dest.writeInt(this.is_default);
    }

    protected Plate(Parcel in) {
        this.car_number = in.readString();
        this.is_auth = in.readInt();
        this.is_default = in.readInt();
    }

    public static final Creator<Plate> CREATOR = new Creator<Plate>() {
        public Plate createFromParcel(Parcel source) {
            return new Plate(source);
        }

        public Plate[] newArray(int size) {
            return new Plate[size];
        }
    };
}
