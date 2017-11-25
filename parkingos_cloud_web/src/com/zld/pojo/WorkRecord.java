package com.zld.pojo;

import java.io.Serializable;

public class WorkRecord implements Serializable {
	private Long id;
	private Long start_time;//上班开始时间
	private Long end_time;//下班时间
	private Long worksite_id = -1L;//工作站编号
	private Long uid = -1L;//收费员编号
	private Long berthsec_id = -1L;//泊位段编号
	private String device_code;//设备号
	private Integer state = 0;//0已签到  1已签退
	private Double history_money = 0d;//上岗时，泊位段上的已预收金额
	private String out_log;//签退小票内容
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getStart_time() {
		return start_time;
	}
	public void setStart_time(Long start_time) {
		this.start_time = start_time;
	}
	public Long getEnd_time() {
		return end_time;
	}
	public void setEnd_time(Long end_time) {
		this.end_time = end_time;
	}
	public Long getWorksite_id() {
		return worksite_id;
	}
	public void setWorksite_id(Long worksite_id) {
		if(worksite_id == null)
			worksite_id = -1L;
		this.worksite_id = worksite_id;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		if(uid == null)
			uid = -1L;
		this.uid = uid;
	}
	public Long getBerthsec_id() {
		return berthsec_id;
	}
	public void setBerthsec_id(Long berthsec_id) {
		if(berthsec_id == null)
			berthsec_id = -1L;
		this.berthsec_id = berthsec_id;
	}
	public String getDevice_code() {
		return device_code;
	}
	public void setDevice_code(String device_code) {
		this.device_code = device_code;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		if(state == null)
			state = 0;
		this.state = state;
	}
	public Double getHistory_money() {
		return history_money;
	}
	public void setHistory_money(Double history_money) {
		if(history_money == null)
			history_money = 0d;
		this.history_money = history_money;
	}
	public String getOut_log() {
		return out_log;
	}
	public void setOut_log(String out_log) {
		this.out_log = out_log;
	}

}
