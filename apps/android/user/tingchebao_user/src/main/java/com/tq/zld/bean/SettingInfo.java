package com.tq.zld.bean;

public class SettingInfo {
	public String limit_money;// 0:不自动支付，-1总是自动支付,正整数：小于时自动支付
	public String low_recharge;// 0:不提醒，其他：低于时提醒
}
