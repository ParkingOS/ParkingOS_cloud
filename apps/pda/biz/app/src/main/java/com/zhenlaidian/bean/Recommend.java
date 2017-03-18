package com.zhenlaidian.bean;

public class Recommend {
	private String mobile;
	private String create_time;

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	@Override
	public String toString() {
		return "Recommend [mobile=" + mobile + ", create_time="
				+ create_time + "]";
	}

}