package com.zhenlaidian.bean;

import java.io.Serializable;

import android.annotation.SuppressLint;

@SuppressWarnings("serial")
@SuppressLint("ParcelCreator")
public class LostOrderRecordInfo implements Serializable {

//查看逃单记录的结果--->[{"id":"381","order_id":"176520","create_time":"1413950599","end_time":"1413950613","comid":"1130","total":"0.00","car_number":"苏Y38497","parkname":"测试和谐家园"}]

	public String id;//逃单编号；
	public String order_id;//订单编号
	public String create_time;//入场时间
	public String end_time;//被置为逃单时间
	public String comid;//车场编号
	public String total;//总金额
	public String parkname;//车场名字
	public String car_number;
	
	public LostOrderRecordInfo() {
		super();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOrder_id() {
		return order_id;
	}
	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}
	public String getCreate_time() {
		return create_time;
	}
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
	public String getComid() {
		return comid;
	}
	public void setComid(String comid) {
		this.comid = comid;
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
	
	public String getCar_number() {
		return car_number;
	}
	public void setCar_number(String car_number) {
		this.car_number = car_number;
	}
	public String getEnd_time() {
		return end_time;
	}
	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}
	@Override
	public String toString() {
		return "LostOrderRecordInfo [id=" + id + ", order_id=" + order_id
				+ ", create_time=" + create_time + ", end_time=" + end_time
				+ ", comid=" + comid + ", total=" + total + ", parkname="
				+ parkname + ", car_number=" + car_number + "]";
	}
	
	
	
	
}
