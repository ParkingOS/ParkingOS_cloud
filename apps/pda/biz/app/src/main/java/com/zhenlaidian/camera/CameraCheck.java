package com.zhenlaidian.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.widget.Toast;

public class CameraCheck {

	public static boolean CheckCamera(Context mContext) {
		if (mContext.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			Toast.makeText(mContext, "相机不存在！", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(Context mContext) {
		Camera c = null;
		if (CheckCamera(mContext)) {
			try {
				c = Camera.open();
			} catch (Exception e) {
				c=null;
			}
		}
		return c; // returns null if camera is unavailable
	}
}
