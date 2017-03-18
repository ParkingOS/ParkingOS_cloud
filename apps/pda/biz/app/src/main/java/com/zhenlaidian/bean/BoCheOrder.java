package com.zhenlaidian.bean;

import java.io.Serializable;
/*
 * 
 * 接车订单实体类
 */
@SuppressWarnings("serial")
public class BoCheOrder implements Serializable{
	/*
	 *  {"result":"1","time":"2015-03-26 16:02:14","state":"等待还车",
	 *  "lng":"116.313534","lat":"40.041920","id":"66","carnumber":"新Z449ZZ",
	 *  "mobile":"18510341966","keyno":"","carlocal":"北京市海淀区上地三街9号-d座-801室"}
	 */
	private String result;
	private String time;
	private String state;
	private String distance;
	private String carnumber;
	private String mobile;
	private String lng;
	private String lat;
	private String id;
	private String keyno;
	private String carlocal;
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getDistance() {
		return distance;
	}
	public void setDistance(String distance) {
		this.distance = distance;
	}
	public String getCarnumber() {
		return carnumber;
	}
	public void setCarnumber(String carnumber) {
		this.carnumber = carnumber;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getLng() {
		return lng;
	}
	public void setLng(String lng) {
		this.lng = lng;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getKeyno() {
		return keyno;
	}
	public void setKeyno(String keyno) {
		this.keyno = keyno;
	}
	
	public String getCarlocal() {
		return carlocal;
	}
	public void setCarlocal(String carlocal) {
		this.carlocal = carlocal;
	}
	public BoCheOrder() {
		super();
	}
	public BoCheOrder(String result, String time, String state,
			String distance, String carnumber, String mobile, String lng,
			String lat, String id, String keyno, String carlocal) {
		super();
		this.result = result;
		this.time = time;
		this.state = state;
		this.distance = distance;
		this.carnumber = carnumber;
		this.mobile = mobile;
		this.lng = lng;
		this.lat = lat;
		this.id = id;
		this.keyno = keyno;
		this.carlocal = carlocal;
	}
	@Override
	public String toString() {
		return "BoCheOrder [result=" + result + ", time=" + time + ", state="
				+ state + ", distance=" + distance + ", carnumber=" + carnumber
				+ ", mobile=" + mobile + ", lng=" + lng + ", lat=" + lat
				+ ", id=" + id + ", keyno=" + keyno + ", carlocal=" + carlocal
				+ "]";
	}
}
