package com.zhenlaidian.bean;

public class DBNfcOrder {

	private int T_id;
	private String user_id;
	private String user_name;
	private String password;
	public int getT_id() {
		return T_id;
	}
	public void setT_id(int t_id) {
		T_id = t_id;
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Override
	public String toString() {
		return "DBNfcOrder [T_id=" + T_id + ", user_id=" + user_id
				+ ", user_name=" + user_name + ", password=" + password + "]";
	}
}
