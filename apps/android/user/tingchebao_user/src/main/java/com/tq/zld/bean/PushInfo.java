package com.tq.zld.bean;

import java.math.BigDecimal;
import java.util.ArrayList;

import android.text.TextUtils;

import com.baidu.mapapi.model.LatLng;

public class PushInfo {
	private String suggest;// 推荐停车场
	private String lon;// 停车场经度
	private String lat;// 停车场纬度
	private String id;// 停车场Id
	private String price;// 推荐停车场当前价格
	private String snumber;// 当前推荐车场的空闲车位数

	// ----------------------v1.016版本更新字段-------------------------------
	// private String count;// 停车场总数
	// private String total;// 车位总数
	private ArrayList<String> monthids;// 支持包月的车场ids
	private ArrayList<String> bookids;// 支持预定的车场ids
	private String freeinfo;// 空闲车位情况：0紧张，1较少，2充足
	private String eta;// 预计到达时间：小时

	// ----------------------v1.1.0版本更新字段-------------------------------
	private String epay;// 是否支持手机支付：0不支持，1支持
	private String monthlypay;// 是否支持月卡：0不支持，1支持

	public String getMonthlypay() {
		return monthlypay;
	}

	public void setMonthlypay(String monthlypay) {
		this.monthlypay = monthlypay;
	}

	public String getEpay() {
		return epay;
	}

	public void setEpay(String epay) {
		this.epay = epay;
	}

	public ArrayList<String> getMonthids() {
		return monthids;
	}

	/**
	 * 获取当前推荐车场的位置
	 * 
	 * @return
	 */
	public LatLng getPosition() {
		if (TextUtils.isEmpty(lat) || TextUtils.isEmpty(lon)) {
			return null;
		}
		BigDecimal latitude = new BigDecimal(lat);
		BigDecimal longitude = new BigDecimal(lon);
		return new LatLng(latitude.doubleValue(), longitude.doubleValue());
	}

	public void setMonthids(ArrayList<String> monthids) {
		this.monthids = monthids;
	}

	public ArrayList<String> getBookids() {
		return bookids;
	}

	public void setBookids(ArrayList<String> bookids) {
		this.bookids = bookids;
	}

	public String getFreeinfo() {
		return freeinfo;
	}

	public void setFreeinfo(String freeinfo) {
		this.freeinfo = freeinfo;
	}

	public PushInfo() {
		super();
	}

	public String getSuggest() {
		return suggest;
	}

	public void setSuggest(String suggest) {
		this.suggest = suggest;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getSnumber() {
		return snumber;
	}

	public void setSnumber(String snumber) {
		this.snumber = snumber;
	}

	public String getEta() {
		return eta;
	}

	public void setEta(String eta) {
		this.eta = eta;
	}

}
