package com.zld.pojo;

import java.io.Serializable;

public class TenantEpayAccount implements Serializable {
	private Long id = -1L;
	private Long cityid = -1L;//城市商户编号
	private Long comid = -1L;//车场编号
	private Double amount = 0d;//金额
	private Long create_time;//记录时间
	private Long uid = -1L;//收费员账号
	private Integer type = 0;//类型 0：充值 1：支出
	private Integer source = 0;//来源 0：停车费（非预付），1：提现，2：追缴停车费，3：预付停车费，4：预付退款（预付），5：预付补缴（预付金额不足）
	private Long orderid = -1L;//订单编号
	private Long withdraw_id = -1L;//提现记录编号
	private Long groupid = -1L;//运营集团编号
	private String remark;//说明
	private Long berthseg_id = -1L;//产生这笔流水所在的泊位段编号
	private Long berth_id = -1L;//产生这笔流水所在的泊位编号
	private Integer is_delete = 0;//账目流水状态 0：正常 1：删除
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getComid() {
		return comid;
	}
	public void setComid(Long comid) {
		if(comid == null)
			comid = -1L;
		this.comid = comid;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		if(amount == null)
			amount = 0d;
		this.amount = amount;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		if(uid == null)
			uid = -1L;
		this.uid = uid;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getSource() {
		return source;
	}
	public void setSource(Integer source) {
		this.source = source;
	}
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		if(orderid == null)
			orderid = -1L;
		this.orderid = orderid;
	}
	public Long getWithdraw_id() {
		return withdraw_id;
	}
	public void setWithdraw_id(Long withdraw_id) {
		if(withdraw_id == null)
			withdraw_id = -1L;
		this.withdraw_id = withdraw_id;
	}
	public Long getGroupid() {
		return groupid;
	}
	public void setGroupid(Long groupid) {
		if(groupid == null)
			groupid = -1L;
		this.groupid = groupid;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
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
	public Integer getIs_delete() {
		return is_delete;
	}
	public void setIs_delete(Integer is_delete) {
		this.is_delete = is_delete;
	}
	public Long getCityid() {
		return cityid;
	}
	public void setCityid(Long cityid) {
		if(cityid == null)
			cityid = -1L;
		this.cityid = cityid;
	}
}
