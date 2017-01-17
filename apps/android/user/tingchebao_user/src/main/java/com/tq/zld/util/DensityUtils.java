package com.tq.zld.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * 实现android下dp和px单位的相互转化
 * 
 * @author Clare
 * 
 */
public class DensityUtils {

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dpValue, context.getResources().getDisplayMetrics());
		return (int) px;
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
}
