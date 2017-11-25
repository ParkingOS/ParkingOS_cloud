package com.zld.pojo;

import java.io.Serializable;
import java.util.List;

public class StatsAccountResp extends BaseResp implements Serializable {
	private List<StatsAccount> accounts;

	public List<StatsAccount> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<StatsAccount> accounts) {
		this.accounts = accounts;
	}

	@Override
	public String toString() {
		return "StatsResp [accounts=" + accounts + "]";
	}

}
