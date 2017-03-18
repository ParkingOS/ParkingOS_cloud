package com.zhenlaidian.bean;

public class MyAccountInfo {

//	{"name":"赵威","uin":"10414","role":"管理员","mobile":"18710233083"}
	private String name;
	private String uin;
	private String role;
	private String mobile;
	private String pic; //0可以上传，1已上传，在审核
	public MyAccountInfo() {
		super();
	}
	
	public String getPic() {
		return pic;
	}
	public void setPic(String pic) {
		this.pic = pic;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUin() {
		return uin;
	}
	public void setUin(String uin) {
		this.uin = uin;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	@Override
	public String toString() {
		return "MyAccountInfo [name=" + name + ", uin=" + uin + ", role="
				+ role + ", mobile=" + mobile + "]";
	}
	
	
}
