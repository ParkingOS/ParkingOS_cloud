package com.zld.pojo;

import java.io.Serializable;

public class ParkUserEpayAccount implements Serializable {
	private Long id = -1L;
	private Long uin = -1L;//收费员账号
	private Double amount = 0d;//金额
	private Long orderid = -1L;//订单编号
	private Integer type = 0;//0:岗亭收费 1：中央预支付收费 2：卡片操作
	private Long create_time;//记录日期
	private Integer ctype = 0;//0：收入，1：支出
	private Integer target = 0;//（该字段不适用type=1）0：停车费（非预付），1：预付停车费，2：预付退款（预付超额），3：预付补缴（预付不足），4：追缴停车费 5：卡片充值 6：卡片注销
	private Long card_account_id = -1L;//卡片明细编号
	private Long comid = -1L;//车场编号
	private Long berthseg_id = -1L;//泊位段编号
	private Long berth_id = -1L;//泊位编号
	private Long groupid = -1L;//运营集团编号
	private Integer is_delete = 0;//账目流水状态 0：正常 1：删除

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getUin() {
		return uin;
	}
	public void setUin(Long uin) {
		this.uin = uin;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		if(amount == null)
			amount = 0d;
		this.amount = amount;
	}
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		if(orderid == null)
			orderid = -1L;
		this.orderid = orderid;
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
	public Integer getCtype() {
		return ctype;
	}
	public void setCtype(Integer ctype) {
		this.ctype = ctype;
	}
	public Integer getTarget() {
		return target;
	}
	public void setTarget(Integer target) {
		this.target = target;
	}
	public Long getCard_account_id() {
		return card_account_id;
	}
	public void setCard_account_id(Long card_account_id) {
		if(card_account_id == null)
			card_account_id = -1L;
		this.card_account_id = card_account_id;
	}
	public Long getComid() {
		return comid;
	}
	public void setComid(Long comid) {
		if(comid == null)
			comid = -1L;
		this.comid = comid;
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
