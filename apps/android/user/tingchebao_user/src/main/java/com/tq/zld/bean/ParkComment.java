package com.tq.zld.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 停车场评价信息
 *
 * @author Clare
 */
public class ParkComment implements Parcelable {
    /**
     * 评价内容：巴拉巴拉一大串废话。。。
     */
    public String info;
    /**
     * 评价者：车主（车牌号：京A***A111）
     */
    public String user;

    //---------------------------v2.0.3--------------------------
    /**
     * 评论时间
     */
    public long ctime;

    public ParkComment() {
    }

    public ParkComment(Parcel source) {
        this.info = source.readString();
        this.user = source.readString();
        this.ctime = source.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(info);
        dest.writeString(user);
        dest.writeLong(ctime);
    }

    public static final Creator<ParkComment> CREATOR = new Creator<ParkComment>() {
        @Override
        public ParkComment createFromParcel(Parcel source) {
            return new ParkComment(source);
        }

        @Override
        public ParkComment[] newArray(int size) {
            return new ParkComment[size];
        }
    };
}
