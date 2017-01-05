package com.zld.pojo;

import java.io.Serializable;

public class CardAccount implements Serializable {
	private Long id = -1L;
	private Long card_id = -1L;// -- 卡片编号
	private Integer type = 0;// -- 0：充值 1：消费
	private Integer charge_type = 0;//充值方式：0：现金充值 1：微信公众号充值 2：微信客户端充值 3：支付宝充值 4：预支付退款 5：订单退款
	private Integer consume_type = 0;//消费方式 0：支付停车费（非预付） 1：预付停车费 2：补缴停车费
	private Double amount = 0d;//金额
	private Long orderid = -1L;//订单编号
	private Long create_time;//记录时间
	private String remark;//说明
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getCard_id() {
		return card_id;
	}
	public void setCard_id(Long card_id) {
		this.card_id = card_id;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getCharge_type() {
		return charge_type;
	}
	public void setCharge_type(Integer charge_type) {
		if(charge_type == null)
			charge_type = 0;
		this.charge_type = charge_type;
	}
	public Integer getConsume_type() {
		return consume_type;
	}
	public void setConsume_type(Integer consume_type) {
		if(consume_type == null)
			consume_type = 0;
		this.consume_type = consume_type;
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
}
