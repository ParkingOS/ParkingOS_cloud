package com.tq.zld.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class UpdateInfo implements Parcelable {

    public String description;
    public String apkurl;
    // -----------------------------v1.1.0------------------------------
    public String force;// 是否强制更新：0强制，其他不强制

    // -----------------------------v1.1.1------------------------------
    public String remind;// 是否提醒更新：0不提醒，其他提醒

    // -----------------------------v2.0------------------------------
    public String md5;// 文件的MD5值
    public int versionCode;// 版本号
    public String versionName;// 版本名

    // -----------------------------v2.0 废弃------------------------------
    // public String version;//版本名

    public UpdateInfo() {
    }

    public UpdateInfo(Parcel source) {
        //	version = source.readString();
        description = source.readString();
        apkurl = source.readString();
        force = source.readString();
        remind = source.readString();
        md5 = source.readString();
        versionCode = source.readInt();
        versionName = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //	dest.writeString(version);
        dest.writeString(description);
        dest.writeString(apkurl);
        dest.writeString(force);
        dest.writeString(remind);
        dest.writeString(md5);
        dest.writeInt(versionCode);
        dest.writeString(versionName);
    }

    public static final Parcelable.Creator<UpdateInfo> CREATOR = new Creator<UpdateInfo>() {

        @Override
        public UpdateInfo[] newArray(int size) {
            return new UpdateInfo[size];
        }

        @Override
        public UpdateInfo createFromParcel(Parcel source) {
            return new UpdateInfo(source);
        }
    };
}
