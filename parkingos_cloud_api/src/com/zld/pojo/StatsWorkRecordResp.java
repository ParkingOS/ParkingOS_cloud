package com.zld.pojo;

import java.util.List;

public class StatsWorkRecordResp extends BaseResp {
	
	private List<StatsWorkRecordInfo> infos;
	

	public List<StatsWorkRecordInfo> getInfos() {
		return infos;
	}

	public void setInfos(List<StatsWorkRecordInfo> infos) {
		this.infos = infos;
	}

	@Override
	public String toString() {
		return "StatsWorkRecordResp [infos=" + infos + "]";
	}
	
}
