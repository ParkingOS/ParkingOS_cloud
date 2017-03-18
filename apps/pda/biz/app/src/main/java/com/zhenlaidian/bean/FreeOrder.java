package com.zhenlaidian.bean;
//<content>
//<message>优惠成功!</message> 
//<info>success</info> 
//</content>
public class FreeOrder {
	private String info;
	private String message;
	public FreeOrder() {
		super();
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
