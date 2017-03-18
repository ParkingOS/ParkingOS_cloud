package com.zhenlaidian.bean;

public class AccountInfo {

	//返回的账号和密码是---{"uin":"10082","password":"lianxiang"} 

	private String uin;
	private String password;
	private String comid;
	public AccountInfo() {
		super();
	}
	
	public String getComid() {
		return comid;
	}

	public void setComid(String comid) {
		this.comid = comid;
	}

	public String getUin() {
		return uin;
	}
	public void setUin(String uin) {
		this.uin = uin;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "AccountInfo [uin=" + uin + ", password=" + password
				+ ", comid=" + comid + "]";
	}


	
	
	
	
}
