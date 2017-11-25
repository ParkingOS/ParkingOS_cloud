package com.zld.pojo;

import java.io.Serializable;

public class Car implements Serializable {
	private Long id;
	private Long uin = -1L;//所属车主账号
	private String car_number;//车牌号码
	private Integer is_comuse = 0;//-- 是否是常用车牌，0 不是，1是
	private String remark;//说明
	private Integer is_auth = 0;//-- 是否已认证 0未认证，1已认证 2认证中
	private Long create_time;//记录时间
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getUin() {
		return uin;
	}
	public void setUin(Long uin) {
		if(uin == null)
			uin = -1L;
		this.uin = uin;
	}
	public String getCar_number() {
		return car_number;
	}
	public void setCar_number(String car_number) {
		this.car_number = car_number;
	}
	public Integer getIs_comuse() {
		return is_comuse;
	}
	public void setIs_comuse(Integer is_comuse) {
		this.is_comuse = is_comuse;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Integer getIs_auth() {
		return is_auth;
	}
	public void setIs_auth(Integer is_auth) {
		this.is_auth = is_auth;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	@Override
	public String toString() {
		return "Car [id=" + id + ", uin=" + uin + ", car_number=" + car_number
				+ ", is_comuse=" + is_comuse + ", remark=" + remark
				+ ", is_auth=" + is_auth + ", create_time=" + create_time + "]";
	}
}
