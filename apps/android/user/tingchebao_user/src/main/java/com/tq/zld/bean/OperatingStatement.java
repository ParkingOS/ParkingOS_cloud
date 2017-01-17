package com.tq.zld.bean;

public class OperatingStatement {

	public String remark;// 产品名称
	public String pay_type;// 支付类型
	public String create_time;// 交易时间：long型值（单位：秒）
	public String amount;// 交易金额
	public String type;// 消费类型：1-->消费；0-->充值

	// ---------------------v1.0.19新增字段-------------------------------------
	public String pay_name;// 支付类型：字符串名称
}
