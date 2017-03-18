package com.zhenlaidian.bean;

public class UpdateInfo {
	private String version;//服务端版本号
	private String description;//升级描述
	private String apkurl;//升级地址
	public String force;// 是否强制更新：1强制，其他不强制
	public String remind;// 是否提醒更新：1不提醒，其他提醒

	public String getVersion() {
		return version;
	}

	public UpdateInfo() {
		super();
	}

	public UpdateInfo(String version, String description, String apkurl, String force, String remind) {
		super();
		this.version = version;
		this.description = description;
		this.apkurl = apkurl;
		this.force = force;
		this.remind = remind;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getApkurl() {
		return apkurl;
	}

	public void setApkurl(String apkurl) {
		this.apkurl = apkurl;
	}

	public String getForce() {
		return force;
	}

	public void setForce(String force) {
		this.force = force;
	}

	public String getRemind() {
		return remind;
	}

	public void setRemind(String remind) {
		this.remind = remind;
	}

	@Override
	public String toString() {
		return "UpdataInfo [version=" + version + ", description="
				+ description + ", apkurl=" + apkurl + ", force=" + force
				+ ", remind=" + remind + "]";
	}
	
}
