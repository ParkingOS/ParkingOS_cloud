package com.tq.zld.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class AccountInfo implements Parcelable {
    // <content>
    // <balance>2344.0</balance>
    // <carNumber>京GPS223</carNumber>
    // <mobile>15801482643</mobile>
    // </content>

    public String balance;
    public String carNumber;
    public String mobile;

    // --------------------v1.0.19更新字段-------------------------
    public ArrayList<Coupon> tickets;// 账户优惠券

    // --------------------v1.1.16更新字段-------------------------
    public String bonusid;// 红包ID

    // --------------------v2.2更新字段-------------------------
    /**
     * 当前车牌是否已认证
     * 0:未认证，1:已认证，2:认证中
     */
    public int state;

    /**
     * 信用额度：元
     */
    public double limit;

    /**
     * 信用额度警告值
     */
    public double limit_warn;

    /**
     * 可用信用额度
     */
    public double limit_balan;

    public AccountInfo() {
        super();
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    @Override
    public String toString() {
        return "MyAccountInfo [balance=" + balance + ", carnumber=" + carNumber
                + ", mobile=" + mobile + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.balance);
        dest.writeString(this.carNumber);
        dest.writeString(this.mobile);
        dest.writeTypedList(tickets);
        dest.writeString(this.bonusid);
        dest.writeInt(this.state);
        dest.writeDouble(this.limit);
        dest.writeDouble(this.limit_warn);
        dest.writeDouble(this.limit_balan);
    }

    protected AccountInfo(Parcel in) {
        this.balance = in.readString();
        this.carNumber = in.readString();
        this.mobile = in.readString();
        this.tickets = in.createTypedArrayList(Coupon.CREATOR);
        this.bonusid = in.readString();
        this.state = in.readInt();
        this.limit = in.readDouble();
        this.limit_warn = in.readDouble();
        this.limit_balan = in.readDouble();
    }

    public static final Parcelable.Creator<AccountInfo> CREATOR = new Parcelable.Creator<AccountInfo>() {
        public AccountInfo createFromParcel(Parcel source) {
            return new AccountInfo(source);
        }

        public AccountInfo[] newArray(int size) {
            return new AccountInfo[size];
        }
    };
}
