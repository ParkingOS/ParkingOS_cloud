package com.zld.pojo;

import java.io.Serializable;

public class NoPayment implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private Long create_time;
	private Long end_time;
	private Long order_id;
	private String car_number;
	private Integer state;
	private Long uin;
	private Long comid;
	private Double total;
	private Long uid;
	private Long pursue_time;
	private Double act_total;
	private Long pursue_uid;
	private Long pursue_comid;
	private Long pursue_berthseg_id;
	private Long pursue_berth_id;
	private Long berthseg_id;
	private Long berth_id;
	private Long groupid;
	private Double prepay;
	private Integer is_delete;
	private Long pursue_groupid;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	public Long getEnd_time() {
		return end_time;
	}
	public void setEnd_time(Long end_time) {
		this.end_time = end_time;
	}
	public Long getOrder_id() {
		return order_id;
	}
	public void setOrder_id(Long order_id) {
		if(order_id == null)
			order_id = -1L;
		this.order_id = order_id;
	}
	public String getCar_number() {
		return car_number;
	}
	public void setCar_number(String car_number) {
		this.car_number = car_number;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		if(state == null)
			state = 0;
		this.state = state;
	}
	public Long getUin() {
		return uin;
	}
	public void setUin(Long uin) {
		if(uin == null)
			uin = -1L;
		this.uin = uin;
	}
	public Long getComid() {
		return comid;
	}
	public void setComid(Long comid) {
		if(comid == null)
			comid = -1L;
		this.comid = comid;
	}
	public Double getTotal() {
		return total;
	}
	public void setTotal(Double total) {
		if(total == null)
			total = 0d;
		this.total = total;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		if(uid == null)
			uid = -1L;
		this.uid = uid;
	}
	public Long getPursue_time() {
		return pursue_time;
	}
	public void setPursue_time(Long pursue_time) {
		this.pursue_time = pursue_time;
	}
	public Double getAct_total() {
		return act_total;
	}
	public void setAct_total(Double act_total) {
		if(act_total == null)
			act_total = 0d;
		this.act_total = act_total;
	}
	public Long getPursue_uid() {
		return pursue_uid;
	}
	public void setPursue_uid(Long pursue_uid) {
		if(pursue_uid == null)
			pursue_uid = -1L;
		this.pursue_uid = pursue_uid;
	}
	public Long getPursue_comid() {
		return pursue_comid;
	}
	public void setPursue_comid(Long pursue_comid) {
		if(pursue_comid == null)
			pursue_comid = -1L;
		this.pursue_comid = pursue_comid;
	}
	public Long getPursue_berthseg_id() {
		return pursue_berthseg_id;
	}
	public void setPursue_berthseg_id(Long pursue_berthseg_id) {
		if(pursue_berthseg_id == null)
			pursue_berthseg_id = -1L;
		this.pursue_berthseg_id = pursue_berthseg_id;
	}
	public Long getPursue_berth_id() {
		return pursue_berth_id;
	}
	public void setPursue_berth_id(Long pursue_berth_id) {
		if(pursue_berth_id == null)
			pursue_berth_id = -1L;
		this.pursue_berth_id = pursue_berth_id;
	}
	public Long getBerthseg_id() {
		return berthseg_id;
	}
	public void setBerthseg_id(Long berthseg_id) {
		if(berthseg_id == null)
			berthseg_id = -1L;
		this.berthseg_id = berthseg_id;
	}
	public Long getBerth_id() {
		return berth_id;
	}
	public void setBerth_id(Long berth_id) {
		if(berth_id == null)
			berth_id = -1L;
		this.berth_id = berth_id;
	}
	public Long getGroupid() {
		return groupid;
	}
	public void setGroupid(Long groupid) {
		if(groupid == null)
			groupid = -1L;
		this.groupid = groupid;
	}
	public Double getPrepay() {
		return prepay;
	}
	public void setPrepay(Double prepay) {
		if(prepay == null)
			prepay = 0d;
		this.prepay = prepay;
	}
	public Integer getIs_delete() {
		return is_delete;
	}
	public void setIs_delete(Integer is_delete) {
		if(is_delete == null)
			is_delete = 0;
		this.is_delete = is_delete;
	}
	public Long getPursue_groupid() {
		return pursue_groupid;
	}
	public void setPursue_groupid(Long pursue_groupid) {
		if(pursue_groupid == null)
			pursue_groupid = -1L;
		this.pursue_groupid = pursue_groupid;
	}
	@Override
	public String toString() {
		return "NoPayment [id=" + id + ", create_time=" + create_time
				+ ", end_time=" + end_time + ", order_id=" + order_id
				+ ", car_number=" + car_number + ", state=" + state + ", uin="
				+ uin + ", comid=" + comid + ", total=" + total + ", uid="
				+ uid + ", pursue_time=" + pursue_time + ", act_total="
				+ act_total + ", pursue_uid=" + pursue_uid + ", pursue_comid="
				+ pursue_comid + ", pursue_berthseg_id=" + pursue_berthseg_id
				+ ", pursue_berth_id=" + pursue_berth_id + ", berthseg_id="
				+ berthseg_id + ", berth_id=" + berth_id + ", groupid="
				+ groupid + ", prepay=" + prepay + ", is_delete=" + is_delete
				+ ", pursue_groupid=" + pursue_groupid + "]";
	}
	
}
