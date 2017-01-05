package com.zld.pojo;

import java.io.Serializable;

/**
 * 诱导屏映射对象
 * @author whx
 *
 */
public class Induce implements Serializable {
	private Long id = -1L;
	private String name;//诱导屏名称
	private Integer type = 0;//-- 诱导类型 0：一级诱导 1：二级诱导 2：三级诱导
	private Double longitude;//
	private Double latitude;//
	private String address;//地址
	private Long cityid = -1L;//商户编号
	private Integer state = 0;//-- 状态 0：正常 1：故障
	private Long create_time;//
	private Long update_time;//
	private Long creator_id = -1L;//-- 创建者编号
	private Long updator_id = -1L;//-- 修改者编号
	private Long deletor_id = -1L;//-- 删除者编号
	private Integer is_delete = 0;//-- 0：正常 1：删除
	private String did;//硬件唯一编号
	private Long heartbeat_time;//最后一次心跳时间
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
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
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Long getCityid() {
		return cityid;
	}
	public void setCityid(Long cityid) {
		if(cityid == null)
			cityid = -1L;
		this.cityid = cityid;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		this.state = state;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	public Long getUpdate_time() {
		return update_time;
	}
	public void setUpdate_time(Long update_time) {
		this.update_time = update_time;
	}
	public Long getCreator_id() {
		return creator_id;
	}
	public void setCreator_id(Long creator_id) {
		if(creator_id == null)
			creator_id = -1L;
		this.creator_id = creator_id;
	}
	public Long getUpdator_id() {
		return updator_id;
	}
	public void setUpdator_id(Long updator_id) {
		if(updator_id == null)
			updator_id = -1L;
		this.updator_id = updator_id;
	}
	public Long getDeletor_id() {
		return deletor_id;
	}
	public void setDeletor_id(Long deletor_id) {
		if(deletor_id == null)
			deletor_id = -1L;
		this.deletor_id = deletor_id;
	}
	public Integer getIs_delete() {
		return is_delete;
	}
	public void setIs_delete(Integer is_delete) {
		this.is_delete = is_delete;
	}
	public String getDid() {
		return did;
	}
	public void setDid(String did) {
		this.did = did;
	}
	public Long getHeartbeat_time() {
		return heartbeat_time;
	}
	public void setHeartbeat_time(Long heartbeat_time) {
		this.heartbeat_time = heartbeat_time;
	}
	@Override
	public String toString() {
		return "Induce [id=" + id + ", name=" + name + ", type=" + type
				+ ", longitude=" + longitude + ", latitude=" + latitude
				+ ", address=" + address + ", cityid=" + cityid + ", state="
				+ state + ", create_time=" + create_time + ", update_time="
				+ update_time + ", creator_id=" + creator_id + ", updator_id="
				+ updator_id + ", deletor_id=" + deletor_id + ", is_delete="
				+ is_delete + ", did=" + did + ", heartbeat_time="
				+ heartbeat_time + "]";
	}
	
	
}
