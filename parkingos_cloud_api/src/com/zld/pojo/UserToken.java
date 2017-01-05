package com.zld.pojo;

import java.io.Serializable;

public class UserToken implements Serializable {
	private Long id;
	private String token;//口令
	private Long uin = -1L;//登录的用户账号
	private Long create_time;//登录时间
	private Long comid = -1L;//登录的车场编号
	private Long groupid = -1L;//登录的运营集团编号
	private String version;//客户端版本号
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Long getUin() {
		return uin;
	}
	public void setUin(Long uin) {
		if(uin == null)
			uin = -1L;
		this.uin = uin;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	public Long getComid() {
		return comid;
	}
	public void setComid(Long comid) {
		if(comid == null)
			comid = -1L;
		this.comid = comid;
	}
	public Long getGroupid() {
		return groupid;
	}
	public void setGroupid(Long groupid) {
		if(groupid == null)
			groupid = -1L;
		this.groupid = groupid;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
}
