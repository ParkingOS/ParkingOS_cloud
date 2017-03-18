package com.zhenlaidian.db.localdb;

public class Order_tb {

	// order_tb 订单表
	// id bigint NOT NULL,
	// create_time bigint,
	// comid bigint NOT NULL,
	// uin bigint NOT NULL,
	// total numeric(30,2),
	// state integer, -- 0未支付 1已支付 2:逃单
	// end_time bigint,
	// auto_pay integer DEFAULT 0, -- 自动结算，0：否，1：是
	// pay_type integer DEFAULT 0, -- 0:帐户支付,1:现金支付,2:手机支付 3月卡
	// nfc_uuid character varying(36),
	// c_type integer DEFAULT 1, -- 0:NFC,1:IBeacon,2:照牌 3通道照牌 4直付 5月卡用户
	// uid bigint DEFAULT (-1), -- 收费员帐号
	// car_number character varying(50), -- 车牌
	// imei character varying(50), -- 手机串号
	// pid integer DEFAULT (-1), --
	// 计费方式：0按时(0.5/15分钟)，1按次（12小时内10元,前1/30min，后每小时1元）
	// car_type integer DEFAULT 0, -- 0：通用，1：小车，2：大车
	// pre_state integer DEFAULT 0, -- 预支付状态 0 无，1预支付中，2等待车主支付完成
	// in_passid bigint DEFAULT (-1), -- 进口通道id
	// out_passid bigint DEFAULT (-1), -- 出口通道id

	public String id;
	public String create_time;
	public String comid;
	public String uin;
	public String total;
	public String state;
	public String end_time;
	public String auto_pay;
	public String pay_type;
	public String nfc_uuid;
	public String c_type;
	public String uid;
	public String car_number;
	public String imei;
	public String pid;
	public String car_type;
	public String pre_state;
	public String in_passid;
	public String out_passid;

	public Order_tb() {
		super();
	}

	public Order_tb(String id, String create_time, String comid, String uin, String total, String state, String end_time,
			String auto_pay, String pay_type, String nfc_uuid, String c_type, String uid, String car_number, String imei,
			String pid, String car_type, String pre_state, String in_passid, String out_passid) {
		super();
		this.id = id;
		this.create_time = create_time;
		this.comid = comid;
		this.uin = uin;
		this.total = total;
		this.state = state;
		this.end_time = end_time;
		this.auto_pay = auto_pay;
		this.pay_type = pay_type;
		this.nfc_uuid = nfc_uuid;
		this.c_type = c_type;
		this.uid = uid;
		this.car_number = car_number;
		this.imei = imei;
		this.pid = pid;
		this.car_type = car_type;
		this.pre_state = pre_state;
		this.in_passid = in_passid;
		this.out_passid = out_passid;
	}

	@Override
	public String toString() {
		return "Order_tb [id=" + id + ", create_time=" + create_time + ", comid=" + comid + ", uin=" + uin + ", total=" + total
				+ ", state=" + state + ", end_time=" + end_time + ", auto_pay=" + auto_pay + ", pay_type=" + pay_type
				+ ", nfc_uuid=" + nfc_uuid + ", c_type=" + c_type + ", uid=" + uid + ", car_number=" + car_number + ", imei="
				+ imei + ", pid=" + pid + ", car_type=" + car_type + ", pre_state=" + pre_state + ", in_passid=" + in_passid
				+ ", out_passid=" + out_passid + "]";
	}

}
