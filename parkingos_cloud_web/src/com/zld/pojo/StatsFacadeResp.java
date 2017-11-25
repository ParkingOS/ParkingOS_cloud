package com.zld.pojo;

import java.io.Serializable;
import java.util.List;

public class StatsFacadeResp extends BaseResp implements Serializable {
	private List<StatsAccountClass> classes;

	public List<StatsAccountClass> getClasses() {
		return classes;
	}

	public void setClasses(List<StatsAccountClass> classes) {
		this.classes = classes;
	}

	@Override
	public String toString() {
		return "StatsFacadeResp [classes=" + classes + "]";
	}

}
