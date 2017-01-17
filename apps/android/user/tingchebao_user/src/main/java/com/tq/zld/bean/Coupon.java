package com.tq.zld.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Coupon implements Parcelable {

    public String id;//
    public String beginday;// 获得日期
    public String limitday;// 有效期,秒
    public String state;// 0：未使用;1:已使用;
    public String money;// 金额
    public String exp;// 0：已过期;1:未过期

    // -----------------------------V1.1.19-------------------------------------
    public String utime;// 使用时间
    public String cname;// 车场名称
    public String umoney;// 使用的金额

    // -----------------------------V2.0-------------------------------------
    public String desc;// 规则描述
    public String iscanuse;// 是否可用，1可用，0不可用
    public String type;// 停车券类型：0普通，1专用，2打折

    // -----------------------------v2.2-------------------------------------
    /**
     * 专用券收费员
     */
    public ParkingFeeCollector fee;

    // -----------------------------v2.2.1-------------------------------------
    /**
     * 使用限额
     */
    public double limit;

    // ------------------------------v2.3-----------------------------------

    /**
     * 该券是否购买的,1是
     */
    public int isbuy;

    public Coupon() {
        super();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.beginday);
        dest.writeString(this.limitday);
        dest.writeString(this.state);
        dest.writeString(this.money);
        dest.writeString(this.exp);
        dest.writeString(this.utime);
        dest.writeString(this.cname);
        dest.writeString(this.umoney);
        dest.writeString(this.desc);
        dest.writeString(this.iscanuse);
        dest.writeString(this.type);
        dest.writeParcelable(this.fee, 0);
        dest.writeDouble(this.limit);
        dest.writeInt(this.isbuy);
    }

    protected Coupon(Parcel in) {
        this.id = in.readString();
        this.beginday = in.readString();
        this.limitday = in.readString();
        this.state = in.readString();
        this.money = in.readString();
        this.exp = in.readString();
        this.utime = in.readString();
        this.cname = in.readString();
        this.umoney = in.readString();
        this.desc = in.readString();
        this.iscanuse = in.readString();
        this.type = in.readString();
        this.fee = in.readParcelable(ParkingFeeCollector.class.getClassLoader());
        this.limit = in.readDouble();
        this.isbuy = in.readInt();
    }

    public static final Creator<Coupon> CREATOR = new Creator<Coupon>() {
        public Coupon createFromParcel(Parcel source) {
            return new Coupon(source);
        }

        public Coupon[] newArray(int size) {
            return new Coupon[size];
        }
    };

    @Override
    public String toString() {
        return "Coupon{" +
                "id='" + id + '\'' +
                ", money='" + money + '\'' +
                ", exp='" + exp + '\'' +
                ", isbug='" +isbuy + '\'' +
                '}';
    }
}
