package com.zld.pojo;

import java.io.Serializable;

public class ParkEpayAccount implements Serializable {
	private Long id = -1L;
	private Long comid = -1L;//车场编号
	private Double amount = 0d;//金额
	private Integer type = 0;//0充值 1提现，2返现（已废弃）
	private Long create_time;//记录时间
	private String remark;//说明
	private Long uid = -1L;//收费员账户
	private Integer source = 0;//来源，0：停车费（非预付），1：返现，2：泊车费，3：推荐奖，4：补交现金（已废弃），5：车场提现，6：停车宝排行榜周奖，7：追缴停车费，8：车主预付停车费，9：预付退款（预付超额），10：预付补缴（预付金额不足） 11：订单退款
	private Long orderid = -1L;//订单编号
	private Long withdraw_id = -1L;//提现记录编号
	private Long berthseg_id = -1L;//产生这笔流水所在的泊位段编号
	private Long berth_id = -1L;//产生这笔流水所在的泊位编号
	private Long groupid = -1L;//运营集团账号
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
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		if(uid == null)
			uid = -1L;
		this.uid = uid;
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
	public Integer getIs_delete() {
		return is_delete;
	}
	public void setIs_delete(Integer is_delete) {
		this.is_delete = is_delete;
	}


}
