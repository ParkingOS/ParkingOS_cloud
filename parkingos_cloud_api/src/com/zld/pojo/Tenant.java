package com.zld.pojo;

import java.io.Serializable;

/**
 * 城市商户
 * @author Administrator
 *
 */
public class Tenant implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private Long ctime;//创建时间
	private String name; //-- 名称
	private Integer state;//-- 0:正常 1：禁用
	private Double balance;// -- 余额
	private String gps;//-- 位置
	private String address;// -- 地理位置
	private Integer is_group_pursue;//-- 是否能跨集团追缴订单 0：不可以 1：可以
	private Integer is_inpark_incity;//-- 同一车牌可否在城市内重复入场 0：不可以 1：可以
	public Integer getIs_inpark_incity() {
		return is_inpark_incity;
	}
	public void setIs_inpark_incity(Integer is_inpark_incity) {
		this.is_inpark_incity = is_inpark_incity;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getCtime() {
		return ctime;
	}
	public void setCtime(Long ctime) {
		this.ctime = ctime;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		if(state == null)
			state = 0;
		this.state = state;
	}
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		if(balance == null)
			balance = 0d;
		this.balance = balance;
	}
	public String getGps() {
		return gps;
	}
	public void setGps(String gps) {
		this.gps = gps;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Integer getIs_group_pursue() {
		return is_group_pursue;
	}
	public void setIs_group_pursue(Integer is_group_pursue) {
		if(is_group_pursue == null)
			is_group_pursue = 0;
		this.is_group_pursue = is_group_pursue;
	}
	@Override
	public String toString() {
		return "Tenant [id=" + id + ", ctime=" + ctime + ", name=" + name
				+ ", state=" + state + ", balance=" + balance + ", gps=" + gps
				+ ", address=" + address + ", is_group_pursue="
				+ is_group_pursue + "]";
	}
}
