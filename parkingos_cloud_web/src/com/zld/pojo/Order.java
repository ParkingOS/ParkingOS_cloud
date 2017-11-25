package com.zld.pojo;

import java.io.Serializable;

public class Order implements Serializable {
	private Long id;
	private Long comid = -1L;//停车场编号
	private Long uin = -1L;//车主编号
	private Double total = 0d;//结算金额
	private Integer state = 0;//-- 0未支付 1已支付 2:逃单
	private Long create_time;//订单生成时间
	private Long end_time;//订单结算时间
	private Integer auto_pay = 0;//-- 0:正常结算，1：进场异常结算的订单，2：更正过车牌的订单，3:补录来车生成的订单
	private Integer pay_type = 0;//-- 0:帐户支付,1:现金支付,2:手机支付 3:包月 4:现金预支付 5：银联卡 6：商家卡8:免费放行
	private String nfc_uuid;//NFC卡内置的唯一编号
	private Integer c_type = 1;//-- 0:NFC,1:IBeacon,2:照牌,3:通道扫牌 4直付 5月卡用户6:车位二维码 7：月卡用户第2..3辆车入场 8：分段月卡
	private Long uid = -1L;// -- 入场收费员帐号
	private String car_number;//车牌号
	private String imei;//手机串号
	private Integer pid = -1;//-- 计费方式：0按时(0.5/15分钟)，1按次（12小时内10元,前1/30min，后每小时1元）
	private Integer car_type = 0;//-- 0：通用，1：小车，2：大车
	private Long in_passid = -1L;//-- 进口通道id
	private Long out_passid = -1L;//-- 出口通道id
	private Integer pre_state = 0;//-- 0:默认状态 1：车主预支付中 2：车主预支付中并且收费员刷卡 3：预支付完成
	private Integer type = 0;//-- 类型：0普通订单，1极速通，3本地化订单 4本地服务器订单 5本地生成线上结算订单
	private Integer need_sync = 0;//-- 预支付订单需要同步到线下  0:不需要  1:需要  2同步完成   3本地切换到线上线上生成的需要  4:线上结算的都需要同步下去
	private Integer ishd = 0;// -- 0否 1是不显示
	private Long freereasons = -1L;//-- 免费原因   默认-1 不免费
	private Integer isclick = 0;// -- 0系统结算，1手动结算
	private Double prepaid = 0d;//预付金额
	private Long prepaid_pay_time;//预付时间
	private Long berthnumber = -1L;//-- 泊位编号
	private Long berthsec_id = -1L;//-- 泊位段编号
	private Long groupid = -1L;//-- 所属集团编号
	private Long out_uid = -1L;//-- 出场收费员
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
	public Long getUin() {
		return uin;
	}
	public void setUin(Long uin) {
		if(uin == null)
			uin = -1L;
		this.uin = uin;
	}
	public Double getTotal() {
		return total;
	}
	public void setTotal(Double total) {
		if(total == null)
			total = 0d;
		this.total = total;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		if(state == null)
			state = 0;
		this.state = state;
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
	public Integer getAuto_pay() {
		return auto_pay;
	}
	public void setAuto_pay(Integer auto_pay) {
		if(auto_pay == null)
			auto_pay = 0;
		this.auto_pay = auto_pay;
	}
	public Integer getPay_type() {
		return pay_type;
	}
	public void setPay_type(Integer pay_type) {
		if(pay_type == null)
			pay_type = 0;
		this.pay_type = pay_type;
	}
	public String getNfc_uuid() {
		return nfc_uuid;
	}
	public void setNfc_uuid(String nfc_uuid) {
		this.nfc_uuid = nfc_uuid;
	}
	public Integer getC_type() {
		return c_type;
	}
	public void setC_type(Integer c_type) {
		if(c_type == null)
			c_type = 1;
		this.c_type = c_type;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		if(uid == null)
			uid = -1L;
		this.uid = uid;
	}
	public String getCar_number() {
		return car_number;
	}
	public void setCar_number(String car_number) {
		this.car_number = car_number;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public Integer getPid() {
		return pid;
	}
	public void setPid(Integer pid) {
		if(pid == null)
			pid = -1;
		this.pid = pid;
	}
	public Integer getCar_type() {
		return car_type;
	}
	public void setCar_type(Integer car_type) {
		if(car_type == null)
			car_type = 0;
		this.car_type = car_type;
	}
	public Long getIn_passid() {
		return in_passid;
	}
	public void setIn_passid(Long in_passid) {
		if(in_passid == null)
			in_passid = -1L;
		this.in_passid = in_passid;
	}
	public Long getOut_passid() {
		return out_passid;
	}
	public void setOut_passid(Long out_passid) {
		if(out_passid == null)
			out_passid = -1L;
		this.out_passid = out_passid;
	}
	public Integer getPre_state() {
		return pre_state;
	}
	public void setPre_state(Integer pre_state) {
		if(pre_state == null)
			pre_state = 0;
		this.pre_state = pre_state;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		if(type == null)
			type = 0;
		this.type = type;
	}
	public Integer getNeed_sync() {
		return need_sync;
	}
	public void setNeed_sync(Integer need_sync) {
		if(need_sync == null)
			need_sync = 0;
		this.need_sync = need_sync;
	}
	public Integer getIshd() {
		return ishd;
	}
	public void setIshd(Integer ishd) {
		if(ishd == null)
			ishd = 0;
		this.ishd = ishd;
	}
	public Long getFreereasons() {
		return freereasons;
	}
	public void setFreereasons(Long freereasons) {
		if(freereasons == null)
			freereasons = -1L;
		this.freereasons = freereasons;
	}
	public Integer getIsclick() {
		return isclick;
	}
	public void setIsclick(Integer isclick) {
		if(isclick == null)
			isclick = 0;
		this.isclick = isclick;
	}
	public Double getPrepaid() {
		return prepaid;
	}
	public void setPrepaid(Double prepaid) {
		if(prepaid == null)
			prepaid = 0d;
		this.prepaid = prepaid;
	}
	public Long getPrepaid_pay_time() {
		return prepaid_pay_time;
	}
	public void setPrepaid_pay_time(Long prepaid_pay_time) {
		this.prepaid_pay_time = prepaid_pay_time;
	}
	public Long getBerthnumber() {
		return berthnumber;
	}
	public void setBerthnumber(Long berthnumber) {
		if(berthnumber == null)
			berthnumber = -1L;
		this.berthnumber = berthnumber;
	}
	public Long getBerthsec_id() {
		return berthsec_id;
	}
	public void setBerthsec_id(Long berthsec_id) {
		if(berthsec_id == null)
			berthsec_id = -1L;
		this.berthsec_id = berthsec_id;
	}
	public Long getGroupid() {
		return groupid;
	}
	public void setGroupid(Long groupid) {
		if(groupid == null)
			groupid = -1L;
		this.groupid = groupid;
	}
	public Long getOut_uid() {
		return out_uid;
	}
	public void setOut_uid(Long out_uid) {
		if(out_uid == null)
			out_uid = -1L;
		this.out_uid = out_uid;
	}
	@Override
	public String toString() {
		return "Order [id=" + id + ", comid=" + comid + ", uin=" + uin
				+ ", total=" + total + ", state=" + state + ", create_time="
				+ create_time + ", end_time=" + end_time + ", auto_pay="
				+ auto_pay + ", pay_type=" + pay_type + ", nfc_uuid="
				+ nfc_uuid + ", c_type=" + c_type + ", uid=" + uid
				+ ", car_number=" + car_number + ", imei=" + imei + ", pid="
				+ pid + ", car_type=" + car_type + ", in_passid=" + in_passid
				+ ", out_passid=" + out_passid + ", pre_state=" + pre_state
				+ ", type=" + type + ", need_sync=" + need_sync + ", ishd="
				+ ishd + ", freereasons=" + freereasons + ", isclick="
				+ isclick + ", prepaid=" + prepaid + ", prepaid_pay_time="
				+ prepaid_pay_time + ", berthnumber=" + berthnumber
				+ ", berthsec_id=" + berthsec_id + ", groupid=" + groupid
				+ ", out_uid=" + out_uid + "]";
	}

}
