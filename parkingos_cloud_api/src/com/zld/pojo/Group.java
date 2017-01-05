package com.zld.pojo;

import java.io.Serializable;

public class Group implements Serializable {
	private Long id = -1L;
	private Integer state = 0;
	private String name;
	private Long chanid = -1L;//渠道编号
	private Long create_time;//创建时间
	private Long cityid = -1L;//城市商户编号
	private Integer type = 0;//公司属性 0：普通运营公司 1：充电桩运营公司 2：自行车运营公司
	private Double balance = 0d;//运营集团电子账户余额
	private String address;//地址
	private Double longitude;//经度
	private Double latitude;//纬度
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		if(state == null)
			state = 0;
		this.state = state;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getChanid() {
		return chanid;
	}
	public void setChanid(Long chanid) {
		if(chanid == null)
			chanid = -1L;
		this.chanid = chanid;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	public Long getCityid() {
		return cityid;
	}
	public void setCityid(Long cityid) {
		if(cityid == null)
			cityid = -1L;
		this.cityid = cityid;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		if(type == null)
			type = 0;
		this.type = type;
	}
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		if(balance == null)
			balance = 0d;
		this.balance = balance;
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
	@Override
	public String toString() {
		return "Group [id=" + id + ", state=" + state + ", name=" + name
				+ ", chanid=" + chanid + ", create_time=" + create_time
				+ ", cityid=" + cityid + ", type=" + type + ", balance="
				+ balance + ", address=" + address + ", longitude=" + longitude
				+ ", latitude=" + latitude + "]";
	}
	
}
