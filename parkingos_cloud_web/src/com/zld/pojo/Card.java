package com.zld.pojo;

import java.io.Serializable;

public class Card implements Serializable {
	private Long id = -1L;
	private String nfc_uuid;//卡片内置的唯一编号
	private Long comid = -1L;//发行卡片的车场编号
	private Long create_time;//开卡时间
	private Integer state = 0;//-- 0激活，1锁定，2退卡
	private Integer use_times;//使用次数
	private Long uin = -1L;//卡片所属车主账户
	private Long uid = -1L;//开卡操作人编号
	private Long update_time;//卡片信息更新时间
	private Long nid = 0L;//扫描NFC的二维码号
	private String qrcode;//卡片二维码
	private Integer type;//卡片类型0：NFC  1：电子标签
	private String card_name;//卡片名称
	private String device;//开卡设备
	private Integer is_delete = 0;//0：正常 1：已删除
	private Double balance = 0d;//余额
	private String card_number;//卡面号（印在卡面上的编号）
	private Long tenant_id = -1L;//城市商户编号
	private Long group_id = -1L;//运营集团编号
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNfc_uuid() {
		return nfc_uuid;
	}
	public void setNfc_uuid(String nfc_uuid) {
		this.nfc_uuid = nfc_uuid;
	}
	public Long getComid() {
		return comid;
	}
	public void setComid(Long comid) {
		if(comid == null)
			comid = -1L;
		this.comid = comid;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		if(state == null)
			state = 0;
		this.state = state;
	}
	public Integer getUse_times() {
		return use_times;
	}
	public void setUse_times(Integer use_times) {
		this.use_times = use_times;
	}
	public Long getUin() {
		return uin;
	}
	public void setUin(Long uin) {
		if(uin == null)
			uin = -1L;
		this.uin = uin;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		if(uid == null)
			uid = -1L;
		this.uid = uid;
	}
	public Long getUpdate_time() {
		return update_time;
	}
	public void setUpdate_time(Long update_time) {
		this.update_time = update_time;
	}
	public Long getNid() {
		return nid;
	}
	public void setNid(Long nid) {
		if(nid == null)
			nid = 0L;
		this.nid = nid;
	}
	public String getQrcode() {
		return qrcode;
	}
	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public String getCard_name() {
		return card_name;
	}
	public void setCard_name(String card_name) {
		this.card_name = card_name;
	}
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public Integer getIs_delete() {
		return is_delete;
	}
	public void setIs_delete(Integer is_delete) {
		this.is_delete = is_delete;
	}
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		if(balance == null)
			balance = 0d;
		this.balance = balance;
	}
	public String getCard_number() {
		return card_number;
	}
	public void setCard_number(String card_number) {
		this.card_number = card_number;
	}
	public Long getTenant_id() {
		return tenant_id;
	}
	public void setTenant_id(Long tenant_id) {
		if(tenant_id == null)
			tenant_id = -1L;
		this.tenant_id = tenant_id;
	}
	public Long getGroup_id() {
		return group_id;
	}
	public void setGroup_id(Long group_id) {
		if(group_id == null)
			group_id = -1L;
		this.group_id = group_id;
	}
	@Override
	public String toString() {
		return "Card [id=" + id + ", nfc_uuid=" + nfc_uuid + ", comid=" + comid
				+ ", create_time=" + create_time + ", state=" + state
				+ ", use_times=" + use_times + ", uin=" + uin + ", uid=" + uid
				+ ", update_time=" + update_time + ", nid=" + nid + ", qrcode="
				+ qrcode + ", type=" + type + ", card_name=" + card_name
				+ ", device=" + device + ", is_delete=" + is_delete
				+ ", balance=" + balance + ", card_number=" + card_number
				+ ", tenant_id=" + tenant_id + ", group_id=" + group_id + "]";
	}

}
