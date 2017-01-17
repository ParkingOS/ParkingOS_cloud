package com.tq.zld.bean;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class ParkInfo implements Parcelable {

	public String id;// 停车场ID
	public String name;// 停车场名称
	public String lng;// 纬度
	public String lat;// 经度
	public String free;// 空闲车位数（等同于freespace）
	public String price;// 当前价格（元/每小时，负数表示免费，正数表示有价格，0或“”表示没有价格信息）
	public String total;// 总车位
	public String addr;// 停车场地址
	public String phone;// 停车场电话
	// public String monthlypay;// 是否支持月卡
	public String epay;// 是否支持手机支付
	public String desc;// 车场描述
	public List<String> photo_url;// 车场照片的url地址集合

	// 上传车场的类型，0收费，1免费
	public String type;

	// ---------------------------------保 留-------------------------------------
	// public String is_auth;// 状态：已审核，未审核，已删除
	// public String type;// 停车场类型（等同于parking_type）
	// public String stop_type;// 停车类型
	// public String updatetime;// 更新时间
	// public String nfc;// 是否支持nfc：1--支持，0-->不支持，下同
	// public String etc;// 是否支持etc
	// public String book;// 是否支持预定
	// public String navi;// 是否支持室内导航

	// --------------------------------废 弃-------------------------------------
	// private String parking_type;// 停车场类型
	// private String freeSpace;// 空闲车位数：停车场详情界面使用
	// private String praiseNum;// "赞一个"次数
	// private String disparageNum;// "贬一个"次数
	// private String commentnum;// 评论数量
	// private String hasPraise;// 当前用户是否赞过该停车场：1-->赞过，0-->没赞过
	// private String update_time;// 最后更新时间

	public ParkInfo() {
		super();
	}

	public ParkInfo(Parcel source) {
		id = source.readString();
		name = source.readString();
		// type = source.readString();
		lng = source.readString();
		lat = source.readString();
		// is_auth = source.readString();
		free = source.readString();
		// freeSpace = source.readString();
		price = source.readString();
		// stop_type = source.readString();
		total = source.readString();
		addr = source.readString();
		// parking_type = source.readString();
		phone = source.readString();
		// updatetime = source.readString();
		// praiseNum = source.readString();
		// disparageNum = source.readString();
		// commentnum = source.readString();
		// hasPraise = source.readString();
		desc = source.readString();
		// nfc = source.readString();
		// etc = source.readString();
		// book = source.readString();
		// navi = source.readString();
		// monthlypay = source.readString();
		// update_time = source.readString();
		epay = source.readString();
		type = source.readString();
		photo_url = new ArrayList<String>();
		source.readStringList(photo_url);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		// dest.writeString(type);
		dest.writeString(lng);
		dest.writeString(lat);
		// dest.writeString(is_auth);
		dest.writeString(free);
		// dest.writeString(freeSpace);
		dest.writeString(price);
		// dest.writeString(stop_type);
		dest.writeString(total);
		dest.writeString(addr);
		// dest.writeString(parking_type);
		dest.writeString(phone);
		// dest.writeString(updatetime);
		// dest.writeString(praiseNum);
		// dest.writeString(disparageNum);
		// dest.writeString(commentnum);
		// dest.writeString(hasPraise);
		dest.writeString(desc);
		// dest.writeString(nfc);
		// dest.writeString(etc);
		// dest.writeString(book);
		// dest.writeString(navi);
		// dest.writeString(monthlypay);
		// dest.writeString(update_time);
		dest.writeString(epay);
		dest.writeString(type);
		dest.writeStringList(photo_url);
	}

	@Override
	public String toString() {
		return "ParkInfo [id=" + id + ", name=" + name + ", lng=" + lng
				+ ", lat=" + lat + ", free=" + free + ", price=" + price
				+ ", total=" + total + ", addr=" + addr + ", phone=" + phone
				+ ", desc=" + desc + ", epay=" + epay + ", photo_url="
				+ photo_url + "]";
	}

	public static final Parcelable.Creator<ParkInfo> CREATOR = new Creator<ParkInfo>() {

		@Override
		public ParkInfo createFromParcel(Parcel source) {
			return new ParkInfo(source);
		}

		@Override
		public ParkInfo[] newArray(int size) {
			return new ParkInfo[size];
		}
	};
}
