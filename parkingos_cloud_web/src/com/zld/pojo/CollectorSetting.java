package com.zld.pojo;

import java.io.Serializable;

public class CollectorSetting implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 6355702540911862018L;
	private Long id;
	private Long role_id;
	private String photoset;//拍照设置【num1,num2,num3】分别是入场可拍照片数，出场可拍照片数，未缴可拍照片数
	private Integer change_prepay;//是否可更改预收金额 0不可，1可以
	private Integer view_plot;//0列表，1显示泊位
	private String print_sign;//打印小票信息【入场，出场】
	private String prepayset;//预收设置 【num1,num2,num3...】预收金额选项
	private Integer isprepay;//0不可预收,1可预收
	private Integer hidedetail;//1隐藏 0不隐藏首页收费汇总
	private Integer is_sensortime;//0：取车检器时间作为录入订单时间 1：取当前时间作为录入订单时间
	private String password;//查看汇总的权限密码
	private String signout_password;//签退密码
	private Integer signout_valid;//客户端签退是否需要密码验证 0：不需要 1：需要
	private Integer is_show_card;//是否在收费汇总和打印小票处显示出来卡片的数据（有些运营集团没有卡片） 0：显示 1：不显示
	private Integer print_order_place2;//桂林提出要在点击结算订单的时候就打印小票（已经有一个打印订单小票的地方，此处为第二个地方）0：不打印 1：打印

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		if(id == null)
			id = -1L;
		this.id = id;
	}

	public Long getRole_id() {
		return role_id;
	}

	public void setRole_id(Long role_id) {
		if(role_id == null)
			role_id = -1L;
		this.role_id = role_id;
	}

	public String getPhotoset() {
		return photoset;
	}

	public void setPhotoset(String photoset) {
		this.photoset = photoset;
	}

	public Integer getChange_prepay() {
		return change_prepay;
	}

	public void setChange_prepay(Integer change_prepay) {
		if(change_prepay == null)
			change_prepay = 0;
		this.change_prepay = change_prepay;
	}

	public Integer getView_plot() {
		return view_plot;
	}

	public void setView_plot(Integer view_plot) {
		if(view_plot == null)
			view_plot = 0;
		this.view_plot = view_plot;
	}

	public String getPrint_sign() {
		return print_sign;
	}

	public void setPrint_sign(String print_sign) {
		this.print_sign = print_sign;
	}

	public String getPrepayset() {
		return prepayset;
	}

	public void setPrepayset(String prepayset) {
		this.prepayset = prepayset;
	}

	public Integer getIsprepay() {
		return isprepay;
	}

	public void setIsprepay(Integer isprepay) {
		if(isprepay == null)
			isprepay = 0;
		this.isprepay = isprepay;
	}

	public Integer getHidedetail() {
		return hidedetail;
	}

	public void setHidedetail(Integer hidedetail) {
		if(hidedetail == null)
			hidedetail = 0;
		this.hidedetail = hidedetail;
	}

	public Integer getIs_sensortime() {
		return is_sensortime;
	}

	public void setIs_sensortime(Integer is_sensortime) {
		if(is_sensortime == null)
			is_sensortime = 0;
		this.is_sensortime = is_sensortime;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSignout_password() {
		return signout_password;
	}

	public void setSignout_password(String signout_password) {
		this.signout_password = signout_password;
	}

	public Integer getSignout_valid() {
		return signout_valid;
	}

	public void setSignout_valid(Integer signout_valid) {
		if(signout_valid == null)
			signout_valid = 0;
		this.signout_valid = signout_valid;
	}

	public Integer getIs_show_card() {
		return is_show_card;
	}

	public void setIs_show_card(Integer is_show_card) {
		if(is_show_card == null)
			is_show_card = 0;
		this.is_show_card = is_show_card;
	}

	public Integer getPrint_order_place2() {
		return print_order_place2;
	}

	public void setPrint_order_place2(Integer print_order_place2) {
		if(print_order_place2 == null)
			print_order_place2 = 0;
		this.print_order_place2 = print_order_place2;
	}

	@Override
	public String toString() {
		return "CollecterSetting [id=" + id + ", role_id=" + role_id
				+ ", photoset=" + photoset + ", change_prepay=" + change_prepay
				+ ", view_plot=" + view_plot + ", print_sign=" + print_sign
				+ ", prepayset=" + prepayset + ", isprepay=" + isprepay
				+ ", hidedetail=" + hidedetail + ", is_sensortime="
				+ is_sensortime + ", password=" + password
				+ ", signout_password=" + signout_password + ", signout_valid="
				+ signout_valid + ", is_show_card=" + is_show_card
				+ ", print_order_place2=" + print_order_place2 + "]";
	}
}
