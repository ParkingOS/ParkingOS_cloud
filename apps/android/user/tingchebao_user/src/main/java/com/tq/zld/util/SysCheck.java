package com.tq.zld.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;

public class SysCheck {
	CharSequence toastText = null;
	PackageManager packageMgr = null;

	public SysCheck() {
	}

	public SysCheck(Context context) {
		packageMgr = context.getPackageManager();
	}

	private int getSysSupportCode() {
		return 10;
	}

	public void openSysFeatures() {
		switch (getSysSupportCode()) {
		case 10:
		}
	}

	public int sysCheckPassStatus() {
		return 1;
	}

	public static boolean osVerChk() {
		return Build.VERSION_CODES.JELLY_BEAN_MR2 <= Build.VERSION.SDK_INT;
	}

	public static boolean isNetOn(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connMgr != null) {
			NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
			return netInfo != null && netInfo.isConnected()
					&& netInfo.getState() == NetworkInfo.State.CONNECTED;
		}

		return false;
	}

	public boolean btLeChk() {
		if (packageMgr.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
			if (packageMgr
					.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
				return true;
		return false;
	}

	public boolean locChk() {
		if (packageMgr.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS))
			if (packageMgr.hasSystemFeature(PackageManager.FEATURE_LOCATION))
				return true;
		return false;
	}

	public boolean cameraChk() {
		if (packageMgr.hasSystemFeature(PackageManager.FEATURE_CAMERA))
			return true;
		return false;
	}

	public boolean nfcChk() {
		if (packageMgr.hasSystemFeature(PackageManager.FEATURE_NFC))
			return true;
		return false;
	}

	public boolean wifiChk() {
		if (packageMgr.hasSystemFeature(PackageManager.FEATURE_WIFI))
			return true;
		return false;
	}

	public boolean netChk() {
		if (packageMgr.hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
			return true;
		return false;
	}

	public double getLocAppVer(Context context) {
		String verNum = null;
		try {
			PackageInfo packageInfo = packageMgr.getPackageInfo(
					context.getPackageName(), 0);
			verNum = packageInfo.versionName;
			if (!TextUtils.isEmpty(verNum)) {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		double num = Double.parseDouble(verNum);
		return num;
	}

	public void newAppVerChk(Context context) {
		double curVerNum = getLocAppVer(context);
		double serVerNum = getServerAppVer();

		if (curVerNum == 0 || serVerNum == 0)
			return;

		if (serVerNum > curVerNum) {
			if (isStorageOK()) {
			}
		}
	}

	public double getServerAppVer() {
		HttpClientHelper hch = new HttpClientHelper("");
		return 0;
	}

	public boolean isStorageOK() {
		return true;
	}

	public boolean hasNewVer(String currVer) {
		if (currVer != null && !currVer.equals("")) {
			double d = Double.parseDouble(currVer);
			double serverVerNum = 0;
			if (d < serverVerNum)
				return true;
		} else {
		}
		return false;
	}
}
