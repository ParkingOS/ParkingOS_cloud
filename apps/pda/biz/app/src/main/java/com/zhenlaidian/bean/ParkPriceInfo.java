package com.zhenlaidian.bean;

import java.io.Serializable;


//{"id":"8","price":"3.00","unit":"30","pay_type":"0","b_time":"8","e_time":"18","first_times":"60","fprice":"2.50",
//	"countless":"5","nprice":"2.00","nuint":"60","nid":"9"}
@SuppressWarnings("serial")
public class ParkPriceInfo implements Serializable{
////{"id":"8","price":"3.00","unit":"60","pay_type":"0","b_time":"7","e_time":"21","first_times
//
//":"15","fprice":"2.50","countless":"0","fpay_type":"0","free_time":"15","nid":"9","nprice":
//
//"2.00","nunit":"30","nftime":"0","ncountless":"0","npay_type":"0","nfpay_type":"0",
//
//"nfree_time":"0","nfprice":"0.00","isnight":"0"}

//
	private String id;//白天价格编号
	private String price;  //日间价格
	private String unit; //日间计费单位
	private String pay_type; // 计费方式；
	private String b_time;   //日间开始时间
	private String e_time;   //日间结束时间
	private String first_times;//日间首优惠时长
	private String nfirst_times;//夜间首优惠时长
	private String fprice;   //首优惠价格
	private String countless;  //最小计价时间
	private String ncountless;  //夜晚最小计价时间
	private String nprice;  //夜间价格   =-----------没有夜间价格返回-1；
	private String nunit; //夜间计费单位
	private String nid;//夜晚价格编号
	private String fpay_type;//白天超过免费时长后，免费时长是否计费？1-->免费；0-->收费
	private String free_time;// 白天免费时长，单位：分钟
	private String nfree_time;// 夜间免费时长，单位：分钟
	private String npay_type;//夜间计费方式；
	private String nfpay_type;// 夜间超过免费时长后，免费时长是否计费？1-->免费；0-->收费
	private String nfprice;// 夜间首优惠价格
	private String isnight;//是否支持夜间停车，0:支持，1不支持
	
	
	public ParkPriceInfo() {
		super();
	}
	
	
	public String getNfirst_times() {
		return nfirst_times;
	}


	public void setNfirst_times(String nfirst_times) {
		this.nfirst_times = nfirst_times;
	}


	public String getNunit() {
		return nunit;
	}


	public void setNunit(String nunit) {
		this.nunit = nunit;
	}


	public String getNcountless() {
		return ncountless;
	}
	public void setNcountless(String ncountless) {
		this.ncountless = ncountless;
	}
	public String getFpay_type() {
		return fpay_type;
	}
	public void setFpay_type(String fpay_type) {
		this.fpay_type = fpay_type;
	}
	public String getFree_time() {
		return free_time;
	}
	public void setFree_time(String free_time) {
		this.free_time = free_time;
	}
	public String getNfree_time() {
		return nfree_time;
	}
	public void setNfree_time(String nfree_time) {
		this.nfree_time = nfree_time;
	}
	public String getNpay_type() {
		return npay_type;
	}
	public void setNpay_type(String npay_type) {
		this.npay_type = npay_type;
	}
	public String getNfpay_type() {
		return nfpay_type;
	}
	public void setNfpay_type(String nfpay_type) {
		this.nfpay_type = nfpay_type;
	}
	public String getNfprice() {
		return nfprice;
	}
	public void setNfprice(String nfprice) {
		this.nfprice = nfprice;
	}
	public String getIsnight() {
		return isnight;
	}
	public void setIsnight(String isnight) {
		this.isnight = isnight;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getPay_type() {
		return pay_type;
	}
	public void setPay_type(String pay_type) {
		this.pay_type = pay_type;
	}
	public String getB_time() {
		return b_time;
	}
	public void setB_time(String b_time) {
		this.b_time = b_time;
	}
	public String getE_time() {
		return e_time;
	}
	public void setE_time(String e_time) {
		this.e_time = e_time;
	}
	public String getFirst_times() {
		return first_times;
	}
	public void setFirst_times(String first_times) {
		this.first_times = first_times;
	}
	public String getFprice() {
		return fprice;
	}
	public void setFprice(String fprice) {
		this.fprice = fprice;
	}
	public String getCountless() {
		return countless;
	}
	public void setCountless(String countless) {
		this.countless = countless;
	}
	public String getNprice() {
		return nprice;
	}
	public void setNprice(String nprice) {
		this.nprice = nprice;
	}
	public String getNuint() {
		return nunit;
	}
	public void setNuint(String nuint) {
		this.nunit = nuint;
	}
	public String getNid() {
		return nid;
	}
	public void setNid(String nid) {
		this.nid = nid;
	}


	@Override
	public String toString() {
		return "ParkPriceInfo [id=" + id + ", price=" + price + ", unit="
				+ unit + ", pay_type=" + pay_type + ", b_time=" + b_time
				+ ", e_time=" + e_time + ", first_times=" + first_times
				+ ", nfirst_times=" + nfirst_times + ", fprice=" + fprice
				+ ", countless=" + countless + ", ncountless=" + ncountless
				+ ", nprice=" + nprice + ", nunit=" + nunit + ", nid=" + nid
				+ ", fpay_type=" + fpay_type + ", free_time=" + free_time
				+ ", nfree_time=" + nfree_time + ", npay_type=" + npay_type
				+ ", nfpay_type=" + nfpay_type + ", nfprice=" + nfprice
				+ ", isnight=" + isnight + "]";
	}

	

	
	
	
}
