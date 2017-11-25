package com.zld.pojo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class AccountResp extends BaseResp implements Serializable {
	private List<Map<String, Object>> list;
	private Long count = 0L;//数量
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	public List<Map<String, Object>> getList() {
		return list;
	}
	public void setList(List<Map<String, Object>> list) {
		this.list = list;
	}
}
