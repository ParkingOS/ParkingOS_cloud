package com.zhenlaidian.bean;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ParkingInfo implements Serializable {

	private String parkingtotal;// 总车位数
	private String phone; // 车场电话
	private String parktype; // 车场类型//0:地面1:地下2占道
	private String price; // 车场价格
	private String address; // 地址
	private String name; // 车场名称
	private String mobile; // 手机号
	private String stoptype; // 停车类型
	private String timebet; // 开放时间段
	private String id; // 公司编号
	private String picurls;
	private String resume; // 车场描述
	private String isfixed;// -- 0:未定位 1已定位
	private String longitude;// 经度
	private String latitude;// 纬度
	private String mesgurl;// 主页通知url；

	public ParkingInfo() {
		super();
	}

	public String getPicurls() {
		return picurls;
	}

	public void setPicurls(String picurls) {
		this.picurls = picurls;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParkingtotal() {
		return parkingtotal;
	}

	public void setParkingtotal(String parkingtotal) {
		this.parkingtotal = parkingtotal;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getParktype() {
		return parktype;
	}

	public String getMesgurl() {
		return mesgurl;
	}

	public void setMesgurl(String mesgurl) {
		this.mesgurl = mesgurl;
	}

	public void setParktype(String parktype) {
		this.parktype = parktype;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getStoptype() {
		return stoptype;
	}

	public void setStoptype(String stoptype) {
		this.stoptype = stoptype;
	}

	public String getTimebet() {
		return timebet;
	}

	public void setTimebet(String timebet) {
		this.timebet = timebet;
	}

	public String getResume() {
		return resume;
	}

	public void setResume(String resume) {
		this.resume = resume;
	}

	public String getIsfixed() {
		return isfixed;
	}

	public void setIsfixed(String isfixed) {
		this.isfixed = isfixed;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	@Override
	public String toString() {
		return "ParkingInfo [parkingtotal=" + parkingtotal + ", phone=" + phone + ", parktype="
				+ parktype + ", price=" + price + ", address=" + address + ", name=" + name
				+ ", mobile=" + mobile + ", stoptype=" + stoptype + ", timebet=" + timebet
				+ ", id=" + id + ", picurls=" + picurls + ", resume=" + resume + ", isfixed="
				+ isfixed + ", longitude=" + longitude + ", latitude=" + latitude + ", mesgurl="
				+ mesgurl + "]";
	}

	

}
