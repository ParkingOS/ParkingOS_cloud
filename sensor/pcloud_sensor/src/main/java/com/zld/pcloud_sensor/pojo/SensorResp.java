package com.zld.pcloud_sensor.pojo;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SensorResp {
	private String sensornumber;//车检器编号
	private String error;//错误信息
	private String type;// in:进车 out:出车
	public String getSensornumber() {
		return sensornumber;
	}
	public void setSensornumber(String sensornumber) {
		this.sensornumber = sensornumber;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@Override
	public String toString() {
		return "SensorResp [sensornumber=" + sensornumber + ", error=" + error
				+ ", type=" + type + "]";
	}
	
}
