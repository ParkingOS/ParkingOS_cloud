package com.zhenlaidian.bean;

import android.app.Dialog;
import android.content.Context;

/**
 * 设置车牌号的dialog
 * 
 * @author zhangyunfei 2015年8月31日
 */
public abstract class BaseDialog extends Dialog {

	public BaseDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	public abstract void setCarnumber(String number);

}
