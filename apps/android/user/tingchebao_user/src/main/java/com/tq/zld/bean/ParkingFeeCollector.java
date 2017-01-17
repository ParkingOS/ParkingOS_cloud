package com.tq.zld.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class ParkingFeeCollector implements Parcelable {

    public String name;// 姓名
    public String id;// 编号
    public String parkname;// 所属车场
    public String payed;// 用户是否支付过

    // -------------------------v1.1.15----------------------------
    public String online;// 当前是否在线：23在线

    // -------------------------v2.0----------------------------
    public String mobile;// 手机号

    // -------------------------v2.0.3-----------------------------
    public int rcount;//打赏数
    public int scount;//服务次数
    public int wcount;//最近一周服务次数
    public int ccount;//评论数
    public double money;//总共收到的打赏金额

    // -------------------------v2.2.2-----------------------------
    public long paytime;// 当前用户最后一次支付时间
    public String total;// 用户默认支付多少钱

    public ParkingFeeCollector() {
    }

    public ParkingFeeCollector(String uid, String name, String parkname) {
        this.id = uid;
        this.name = name;
        this.parkname = parkname;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.id);
        dest.writeString(this.parkname);
        dest.writeString(this.payed);
        dest.writeString(this.online);
        dest.writeString(this.mobile);
        dest.writeInt(this.rcount);
        dest.writeInt(this.scount);
        dest.writeInt(this.wcount);
        dest.writeInt(this.ccount);
        dest.writeDouble(this.money);
        dest.writeLong(this.paytime);
        dest.writeString(this.total);
    }

    protected ParkingFeeCollector(Parcel in) {
        this.name = in.readString();
        this.id = in.readString();
        this.parkname = in.readString();
        this.payed = in.readString();
        this.online = in.readString();
        this.mobile = in.readString();
        this.rcount = in.readInt();
        this.scount = in.readInt();
        this.wcount = in.readInt();
        this.ccount = in.readInt();
        this.money = in.readDouble();
        this.paytime = in.readLong();
        this.total = in.readString();
    }

    public static final Creator<ParkingFeeCollector> CREATOR = new Creator<ParkingFeeCollector>() {
        public ParkingFeeCollector createFromParcel(Parcel source) {
            return new ParkingFeeCollector(source);
        }

        public ParkingFeeCollector[] newArray(int size) {
            return new ParkingFeeCollector[size];
        }
    };
}
