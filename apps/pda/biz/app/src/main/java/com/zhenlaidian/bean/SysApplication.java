package com.zhenlaidian.bean;

import android.app.Activity;
import android.app.Application;

import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.util.MyLog;

import java.util.ArrayList;

/**
 * 每次打开一个activity都加入到list集合里.
 *
 * @author flyme
 *
 */
public class SysApplication extends Application {
	private static ArrayList<Activity> mList = new ArrayList<Activity>();
	private static SysApplication instance;

	private SysApplication() {
	}

	public synchronized static SysApplication getInstance() {
		if (null == instance) {
			instance = new SysApplication();
		}
		return instance;
	}

	// add Activity
	public void addActivity(Activity activity) {
		boolean flag = false;
		for (int i = 0; i < mList.size(); i++) {
			if (mList.get(i) != null && mList.get(i).getClass() == activity.getClass()) {
				mList.remove(i);
				flag = true;
			}
		}
		if (!flag) {
			MyLog.i("SysApplication", "不存在-保存");
			mList.add(activity);
		} else {
			mList.add(activity);
			MyLog.i("SysApplication", "已存在-替换");
		}
	}

	public void exit() {
		try {
			for (Activity activity : mList) {
				if (activity != null)
					activity.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
			// System.exit(0);
		} finally {
			// System.exit(0);
		}
	}

	// 关闭离场订单activity
	public void finishActivity() {
		try {
			for (Activity activity : mList) {
				if (activity.getClass() != LeaveActivity.class)
					activity.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	public void onLowMemory() {
		super.onLowMemory();
		System.gc();
	}
}