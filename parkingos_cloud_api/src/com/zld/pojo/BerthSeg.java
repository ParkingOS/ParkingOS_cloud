package com.zld.pojo;

import java.io.Serializable;

public class BerthSeg implements Serializable {
	private Long id;//
	private String uuid;// 唯一标识
	private String berthsec_name;//泊位段名称
	private String park_uuid;//所属停车场uuid
	private Long create_time;// 创建日期
	private String address;//详细地址
	private Double longitude = 0d;//经度
	private Double latitude = 0d;//纬度
	private Integer is_active = 0;//状态 0：正常 1：禁用
	private Long comid = -1L;//车场编号
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getBerthsec_name() {
		return berthsec_name;
	}
	public void setBerthsec_name(String berthsec_name) {
		this.berthsec_name = berthsec_name;
	}
	public String getPark_uuid() {
		return park_uuid;
	}
	public void setPark_uuid(String park_uuid) {
		this.park_uuid = park_uuid;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
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
	public Integer getIs_active() {
		return is_active;
	}
	public void setIs_active(Integer is_active) {
		this.is_active = is_active;
	}
	public Long getComid() {
		return comid;
	}
	public void setComid(Long comid) {
		this.comid = comid;
	}
	@Override
	public String toString() {
		return "BerthSeg [id=" + id + ", uuid=" + uuid + ", berthsec_name="
				+ berthsec_name + ", park_uuid=" + park_uuid + ", create_time="
				+ create_time + ", address=" + address + ", longitude="
				+ longitude + ", latitude=" + latitude + ", is_active="
				+ is_active + ", comid=" + comid + "]";
	}
	
}
