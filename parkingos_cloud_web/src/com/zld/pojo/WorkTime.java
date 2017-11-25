package com.zld.pojo;

import java.io.Serializable;

public class WorkTime implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private Integer b_hour;
	private Integer b_minute;
	private Integer e_hour;
	private Integer e_minute;
	private Long role_id;
	private Integer is_delete;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Integer getB_hour() {
		return b_hour;
	}
	public void setB_hour(Integer b_hour) {
		if(b_hour == null)
			b_hour = 0;
		this.b_hour = b_hour;
	}
	public Integer getB_minute() {
		return b_minute;
	}
	public void setB_minute(Integer b_minute) {
		if(b_minute == null)
			b_minute = 0;
		this.b_minute = b_minute;
	}
	public Integer getE_hour() {
		return e_hour;
	}
	public void setE_hour(Integer e_hour) {
		if(e_hour == null)
			e_hour = 0;
		this.e_hour = e_hour;
	}
	public Integer getE_minute() {
		return e_minute;
	}
	public void setE_minute(Integer e_minute) {
		if(e_minute == null)
			e_minute = 0;
		this.e_minute = e_minute;
	}
	public Long getRole_id() {
		return role_id;
	}
	public void setRole_id(Long role_id) {
		if(role_id == null)
			role_id = -1L;
		this.role_id = role_id;
	}
	public Integer getIs_delete() {
		return is_delete;
	}
	public void setIs_delete(Integer is_delete) {
		this.is_delete = is_delete;
	}
	@Override
	public String toString() {
		return "WorkTime [id=" + id + ", b_hour=" + b_hour + ", b_minute="
				+ b_minute + ", e_hour=" + e_hour + ", e_minute=" + e_minute
				+ ", role_id=" + role_id + ", is_delete=" + is_delete + "]";
	}

}
