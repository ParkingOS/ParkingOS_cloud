package com.zld.pojo;

import java.io.Serializable;

public class CardCarNumber implements Serializable {
	private Long id = -1L;
	private String car_number;
	private Long card_id = -1L;//卡片编号
	private int is_delete = 0;//状态
	private Long create_time;//记录时间
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCar_number() {
		return car_number;
	}
	public void setCar_number(String car_number) {
		this.car_number = car_number;
	}
	public Long getCard_id() {
		return card_id;
	}
	public void setCard_id(Long card_id) {
		this.card_id = card_id;
	}
	public int getIs_delete() {
		return is_delete;
	}
	public void setIs_delete(int is_delete) {
		this.is_delete = is_delete;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	@Override
	public String toString() {
		return "CardCarNumber [id=" + id + ", car_number=" + car_number
				+ ", card_id=" + card_id + ", is_delete=" + is_delete
				+ ", create_time=" + create_time + "]";
	}
}
