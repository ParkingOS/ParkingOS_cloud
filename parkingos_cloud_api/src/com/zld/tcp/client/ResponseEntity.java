package com.zld.tcp.client;
public class ResponseEntity {

	//响应状态码
	private String statusCode;
	//响应消息
	private String message;
	//响应消息主体，明文形式
	private String jsonResult;

	public String getStatusCode() {
		return statusCode;
	}

	public String getMessage() {
		return message;
	}

	public String getJsonResult() {
		return jsonResult;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setJsonResult(String jsonResult) {
		this.jsonResult = jsonResult;
	}
}