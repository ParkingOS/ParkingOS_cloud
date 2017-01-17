package com.tq.zld.bean;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class MonthlyPay implements Parcelable {

	public String id;// 产品ID
	public String price;// 购买价格
	public String name;// 产品名称
	public String number;// 剩余数量
	public String limittime;// 时间限制：全天：00:00-24:00;夜间：21:00-07:00;日间：07:00-21:00
	public String isbuy;// 该用户是否已购买：1购买，0未购买
	public String resume;// 低版本定义的"产品描述"字段

	// ------------------------v1.016更新字段-----------------------
	public String type;// 产品类型：全天包月（0），夜间包月（1），日间包月（2）
	public String price0;// 产品原价
	public String reserved;// 是否固定车位：0不固定；1固定
	public String limitday;// 有效期限：表示日期的数值：单位（s）
	public ParkInfo parkinfo;// 所属车场
	public ArrayList<String> photoUrl;// 车场照片的url地址集合

	public MonthlyPay() {
	};

	public MonthlyPay(Parcel source) {
		id = source.readString();
		price = source.readString();
		name = source.readString();
		number = source.readString();
		limittime = source.readString();
		isbuy = source.readString();
		resume = source.readString();
		type = source.readString();
		price0 = source.readString();
		reserved = source.readString();
		limitday = source.readString();
		photoUrl = new ArrayList<String>();
		source.readStringList(photoUrl);
		parkinfo = source.readParcelable(ParkInfo.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(price);
		dest.writeString(name);
		dest.writeString(number);
		dest.writeString(limittime);
		dest.writeString(isbuy);
		dest.writeString(resume);
		dest.writeString(type);
		dest.writeString(price0);
		dest.writeString(reserved);
		dest.writeString(limitday);
		dest.writeStringList(photoUrl);
		dest.writeParcelable(parkinfo, flags);
	}

	public static final Parcelable.Creator<MonthlyPay> CREATOR = new Creator<MonthlyPay>() {

		@Override
		public MonthlyPay createFromParcel(Parcel source) {
			return new MonthlyPay(source);
		}

		@Override
		public MonthlyPay[] newArray(int size) {
			return new MonthlyPay[size];
		}
	};
}
