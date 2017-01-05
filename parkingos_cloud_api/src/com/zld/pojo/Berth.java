package com.zld.pojo;

import java.io.Serializable;

public class Berth implements Serializable {
	private Long id;//
	private Long comid = -1L;//车场编号
	private String cid;//-- 车位编号
	private Integer state = 0;// -- 0空闲 1占用
	private Long qid = -1L;//二维码表主键
	private Long order_id = -1L;//该泊位正在绑定的订单编号
	private Long dici_id = -1L;//该泊位绑定的车检器编号
	private Long enter_time;//-- 入场时间
	private Long end_time;//-- 入场时间
	private String address;//地址
	private Double longitude;//经度
	private Double latitude;//纬度
	private Long berthsec_id = -1L;//泊位段编号
	private Long create_time;//记录时间
	private Integer is_delete = 0;//-- 0正常 1已删除
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getComid() {
		return comid;
	}
	public void setComid(Long comid) {
		if(comid == null)
			comid = -1L;
		this.comid = comid;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		this.state = state;
	}
	public Long getQid() {
		return qid;
	}
	public void setQid(Long qid) {
		this.qid = qid;
	}
	public Long getOrder_id() {
		return order_id;
	}
	public void setOrder_id(Long order_id) {
		if(order_id == null)
			order_id = -1L;
		this.order_id = order_id;
	}
	public Long getDici_id() {
		return dici_id;
	}
	public void setDici_id(Long dici_id) {
		this.dici_id = dici_id;
	}
	public Long getEnter_time() {
		return enter_time;
	}
	public void setEnter_time(Long enter_time) {
		this.enter_time = enter_time;
	}
	public Long getEnd_time() {
		return end_time;
	}
	public void setEnd_time(Long end_time) {
		this.end_time = end_time;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Long getBerthsec_id() {
		return berthsec_id;
	}
	public void setBerthsec_id(Long berthsec_id) {
		this.berthsec_id = berthsec_id;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	public Integer getIs_delete() {
		return is_delete;
	}
	public void setIs_delete(Integer is_delete) {
		this.is_delete = is_delete;
	}
	
}
