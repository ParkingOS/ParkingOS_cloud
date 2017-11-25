package com.zld.pojo;

import java.io.Serializable;

public class BaseResp implements Serializable {
	private Integer result;
	private String errmsg;//消息

	public Integer getResult() {
		return result;
	}

	public void setResult(Integer result) {
		this.result = result;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	@Override
	public String toString() {
		return "BaseResp [result=" + result + ", errmsg=" + errmsg + "]";
	}

}
