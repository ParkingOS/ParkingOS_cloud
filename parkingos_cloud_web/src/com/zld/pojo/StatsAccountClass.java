package com.zld.pojo;

import java.io.Serializable;

public class StatsAccountClass implements Serializable {
	//统计分类
	private long id = -1;//统计编号
	//停车费现金账目统计
	private double cashParkingFee = 0;//停车费（非预付）
	private double cashPrepayFee = 0;//预付停车费
	private double cashRefundFee = 0;//预付退款（预付超额）
	private double cashAddFee = 0;//预付补缴（预付不足）
	private double cashPursueFee = 0;//追缴停车费
	//停车费电子账目统计
	private double ePayParkingFee = 0;//停车费（非预付）
	private double ePayPrepayFee = 0;//预付停车费
	private double ePayRefundFee = 0;//预付退款（预付超额）
	private double ePayAddFee = 0;//预付补缴（预付不足）
	private double ePayPursueFee = 0;//追缴停车费
	//停车费卡片账目统计
	private double cardParkingFee = 0;//停车费（非预付）
	private double cardPrepayFee = 0;//预付停车费
	private double cardRefundFee = 0;//预付退款（预付超额）
	private double cardAddFee = 0;//预付补缴（预付不足）
	private double cardPursueFee = 0;//追缴停车费
	//卡片统计
	private double cardRegFee = 0;//开卡初始化金额
	private double cardChargeCashFee = 0;//卡片充值金额
	private double cardReturnFee = 0;//退卡退还金额
	private double cardActFee = 0;//激活卡片初始化金额
	private long cardReturnCount = 0;//退卡数量
	private long cardActCount = 0;//激活卡片数量
	private long cardRegCount = 0;//开卡数量
	private long cardBindCount = 0;//绑定用户数量

	//逃单账目统计
	private double escapeFee = 0;//未追缴逃单金额
	//车检器订单账目统计
	private double sensorOrderFee = 0;//未追缴逃单金额

	public double getCardRegFee() {
		return cardRegFee;
	}
	public void setCardRegFee(double cardRegFee) {
		this.cardRegFee = cardRegFee;
	}
	public double getCardChargeCashFee() {
		return cardChargeCashFee;
	}
	public void setCardChargeCashFee(double cardChargeCashFee) {
		this.cardChargeCashFee = cardChargeCashFee;
	}
	public double getCardReturnFee() {
		return cardReturnFee;
	}
	public void setCardReturnFee(double cardReturnFee) {
		this.cardReturnFee = cardReturnFee;
	}
	public double getCardActFee() {
		return cardActFee;
	}
	public void setCardActFee(double cardActFee) {
		this.cardActFee = cardActFee;
	}
	public long getCardReturnCount() {
		return cardReturnCount;
	}
	public void setCardReturnCount(long cardReturnCount) {
		this.cardReturnCount = cardReturnCount;
	}
	public long getCardActCount() {
		return cardActCount;
	}
	public void setCardActCount(long cardActCount) {
		this.cardActCount = cardActCount;
	}
	public long getCardRegCount() {
		return cardRegCount;
	}
	public void setCardRegCount(long cardRegCount) {
		this.cardRegCount = cardRegCount;
	}
	public long getCardBindCount() {
		return cardBindCount;
	}
	public void setCardBindCount(long cardBindCount) {
		this.cardBindCount = cardBindCount;
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public double getCashParkingFee() {
		return cashParkingFee;
	}
	public void setCashParkingFee(double cashParkingFee) {
		this.cashParkingFee = cashParkingFee;
	}
	public double getCashPrepayFee() {
		return cashPrepayFee;
	}
	public void setCashPrepayFee(double cashPrepayFee) {
		this.cashPrepayFee = cashPrepayFee;
	}
	public double getCashRefundFee() {
		return cashRefundFee;
	}
	public void setCashRefundFee(double cashRefundFee) {
		this.cashRefundFee = cashRefundFee;
	}
	public double getCashAddFee() {
		return cashAddFee;
	}
	public void setCashAddFee(double cashAddFee) {
		this.cashAddFee = cashAddFee;
	}
	public double getCashPursueFee() {
		return cashPursueFee;
	}
	public void setCashPursueFee(double cashPursueFee) {
		this.cashPursueFee = cashPursueFee;
	}
	public double getePayParkingFee() {
		return ePayParkingFee;
	}
	public void setePayParkingFee(double ePayParkingFee) {
		this.ePayParkingFee = ePayParkingFee;
	}
	public double getePayPrepayFee() {
		return ePayPrepayFee;
	}
	public void setePayPrepayFee(double ePayPrepayFee) {
		this.ePayPrepayFee = ePayPrepayFee;
	}
	public double getePayRefundFee() {
		return ePayRefundFee;
	}
	public void setePayRefundFee(double ePayRefundFee) {
		this.ePayRefundFee = ePayRefundFee;
	}
	public double getePayAddFee() {
		return ePayAddFee;
	}
	public void setePayAddFee(double ePayAddFee) {
		this.ePayAddFee = ePayAddFee;
	}
	public double getePayPursueFee() {
		return ePayPursueFee;
	}
	public void setePayPursueFee(double ePayPursueFee) {
		this.ePayPursueFee = ePayPursueFee;
	}
	public double getCardParkingFee() {
		return cardParkingFee;
	}
	public void setCardParkingFee(double cardParkingFee) {
		this.cardParkingFee = cardParkingFee;
	}
	public double getCardPrepayFee() {
		return cardPrepayFee;
	}
	public void setCardPrepayFee(double cardPrepayFee) {
		this.cardPrepayFee = cardPrepayFee;
	}
	public double getCardRefundFee() {
		return cardRefundFee;
	}
	public void setCardRefundFee(double cardRefundFee) {
		this.cardRefundFee = cardRefundFee;
	}
	public double getCardAddFee() {
		return cardAddFee;
	}
	public void setCardAddFee(double cardAddFee) {
		this.cardAddFee = cardAddFee;
	}
	public double getCardPursueFee() {
		return cardPursueFee;
	}
	public void setCardPursueFee(double cardPursueFee) {
		this.cardPursueFee = cardPursueFee;
	}
	public double getEscapeFee() {
		return escapeFee;
	}
	public void setEscapeFee(double escapeFee) {
		this.escapeFee = escapeFee;
	}
	public double getSensorOrderFee() {
		return sensorOrderFee;
	}
	public void setSensorOrderFee(double sensorOrderFee) {
		this.sensorOrderFee = sensorOrderFee;
	}
	@Override
	public String toString() {
		return "StatsAccountClass [id=" + id + ", cashParkingFee="
				+ cashParkingFee + ", cashPrepayFee=" + cashPrepayFee
				+ ", cashRefundFee=" + cashRefundFee + ", cashAddFee="
				+ cashAddFee + ", cashPursueFee=" + cashPursueFee
				+ ", ePayParkingFee=" + ePayParkingFee + ", ePayPrepayFee="
				+ ePayPrepayFee + ", ePayRefundFee=" + ePayRefundFee
				+ ", ePayAddFee=" + ePayAddFee + ", ePayPursueFee="
				+ ePayPursueFee + ", cardParkingFee=" + cardParkingFee
				+ ", cardPrepayFee=" + cardPrepayFee + ", cardRefundFee="
				+ cardRefundFee + ", cardAddFee=" + cardAddFee
				+ ", cardPursueFee=" + cardPursueFee + ", cardRegFee="
				+ cardRegFee + ", cardChargeCashFee=" + cardChargeCashFee
				+ ", cardReturnFee=" + cardReturnFee + ", cardActFee="
				+ cardActFee + ", cardReturnCount=" + cardReturnCount
				+ ", cardActCount=" + cardActCount + ", cardRegCount="
				+ cardRegCount + ", cardBindCount=" + cardBindCount
				+ ", escapeFee=" + escapeFee + ", sensorOrderFee="
				+ sensorOrderFee + "]";
	}
}
