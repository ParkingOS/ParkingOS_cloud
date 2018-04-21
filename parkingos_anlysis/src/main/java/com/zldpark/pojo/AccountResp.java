package com.zldpark.pojo;

import java.io.Serializable;
import java.util.List;

public class AccountResp extends BaseResp implements Serializable {
	private List<ParkUserEpayAccount> parkUserEpayAccounts;
	private List<ParkEpayAccount> parkEpayAccounts;
	private List<GroupEpayAccount> groupEpayAccounts;
	private List<TenantEpayAccount> tenantEpayAccounts;
	private List<ParkUserCashAccount> parkUserCashAccounts;
	
	public List<ParkUserEpayAccount> getParkUserEpayAccounts() {
		return parkUserEpayAccounts;
	}
	public void setParkUserEpayAccounts(
			List<ParkUserEpayAccount> parkUserEpayAccounts) {
		this.parkUserEpayAccounts = parkUserEpayAccounts;
	}
	public List<ParkEpayAccount> getParkEpayAccounts() {
		return parkEpayAccounts;
	}
	public void setParkEpayAccounts(List<ParkEpayAccount> parkEpayAccounts) {
		this.parkEpayAccounts = parkEpayAccounts;
	}
	public List<GroupEpayAccount> getGroupEpayAccounts() {
		return groupEpayAccounts;
	}
	public void setGroupEpayAccounts(List<GroupEpayAccount> groupEpayAccounts) {
		this.groupEpayAccounts = groupEpayAccounts;
	}
	public List<TenantEpayAccount> getTenantEpayAccounts() {
		return tenantEpayAccounts;
	}
	public void setTenantEpayAccounts(List<TenantEpayAccount> tenantEpayAccounts) {
		this.tenantEpayAccounts = tenantEpayAccounts;
	}
	public List<ParkUserCashAccount> getParkUserCashAccounts() {
		return parkUserCashAccounts;
	}
	public void setParkUserCashAccounts(
			List<ParkUserCashAccount> parkUserCashAccounts) {
		this.parkUserCashAccounts = parkUserCashAccounts;
	}
	
	
}
