package com.tq.zld.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class Order implements Parcelable {

    /**
     * 订单状态：未结算
     */
    public static final String STATE_PENDING = "0";
    /**
     * 订单状态：已结算，未支付（等待车主支付）
     */
    public static final String STATE_PAYING = "1";
    /**
     * 订单状态：支付成功
     */
    public static final String STATE_PAYED = "2";
    /**
     * 订单状态：支付失败
     */
    public static final String STATE_PAY_FAILED = "-1";

    private String total; // 总金额
    private String parkname;// 停车场名字
    private String address;// 车场地址
    private String etime;// 结束时间
    private String state;// 订单状态
    private String btime;// 入场时间
    private String orderid;// 订单号
    private String parkid;// 停车场Id

    // ----------------------------v1.1.14新加字段-------------------------------
    private String bonusid;// 红包

    // ----------------------------v2.0------------------------------
    public ParkingFeeCollector payee;// 收款人

    // ----------------------------v2.0.3------------------------------
    public String comment;//1评价过，0未评价
    public String reward;//1打赏过，0未打赏

    // ----------------------------v2.2------------------------------
    public String ctype;//订单类型（4:支付订单）

    public Order() {
    }

    public Order(Parcel source) {
        total = source.readString();
        parkname = source.readString();
        address = source.readString();
        etime = source.readString();
        state = source.readString();
        btime = source.readString();
        orderid = source.readString();
        parkid = source.readString();
        bonusid = source.readString();
        comment = source.readString();
        reward = source.readString();
        ctype = source.readString();
        payee = source.readParcelable(getClass().getClassLoader());
    }

    @Override
    public String toString() {
        return "Order [total=" + total + ", parkname=" + parkname
                + ", address=" + address + ", etime=" + etime + ", is_auth="
                + state + ", btime=" + btime + ", orderid=" + orderid
                + ", parkid=" + parkid + ", bonusid=" + bonusid + ", payee="
                + payee + "]";
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getParkname() {
        return parkname;
    }

    public void setParkname(String parkname) {
        this.parkname = parkname;
    }

    /**
     * 返回时间毫秒值
     *
     * @return 服务器返回的数据单位是：秒（s），故此处返回：etime*1000
     */
    public String getEtime() {
        return TextUtils.isEmpty(etime) ? "0" : String.valueOf(Long
                .parseLong(etime) * 1000);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * 返回时间毫秒值
     *
     * @return 服务器返回的数据单位是：秒（s），故此处返回：btime*1000
     */
    public String getBtime() {
        return TextUtils.isEmpty(btime) ? "0" : String.valueOf(Long
                .parseLong(btime) * 1000);
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getParkid() {
        return parkid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(total);
        dest.writeString(parkname);
        dest.writeString(address);
        dest.writeString(etime);
        dest.writeString(state);
        dest.writeString(btime);
        dest.writeString(orderid);
        dest.writeString(parkid);
        dest.writeString(bonusid);
        dest.writeString(comment);
        dest.writeString(reward);
        dest.writeString(ctype);
        dest.writeParcelable(payee, flags);
    }

    public String getBonusid() {
        return bonusid;
    }

    public static final Parcelable.Creator<Order> CREATOR = new Creator<Order>() {

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }

        @Override
        public Order createFromParcel(Parcel source) {
            return new Order(source);
        }
    };

}
