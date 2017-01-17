package com.tq.zld.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.rey.material.drawable.RippleDrawable;
import com.rey.material.widget.RippleManager;

public class RippleLinearLayout extends LinearLayout {

	private RippleManager mRippleManager;

	public RippleLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mRippleManager = new RippleManager();
		mRippleManager.onCreate(this, context, attrs, defStyle, 0);
	}

	public RippleLinearLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RippleLinearLayout(Context context) {
		this(context, null);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setBackgroundDrawable(Drawable drawable) {
		Drawable background = getBackground();
		if (background instanceof RippleDrawable
				&& !(drawable instanceof RippleDrawable))
			((RippleDrawable) background).setBackgroundDrawable(drawable);
		else
			super.setBackgroundDrawable(drawable);
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		if (l == mRippleManager)
			super.setOnClickListener(l);
		else {
			mRippleManager.setOnClickListener(l);
			setOnClickListener(mRippleManager);
		}
	}

	// @SuppressLint("ClickableViewAccessibility")
	// @Override
	// public boolean onTouchEvent(@NonNull MotionEvent event) {
	// mRippleManager.onTouchEvent(event);
	// return super.onTouchEvent(event);
	// }

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		boolean result = super.onTouchEvent(event);
		return mRippleManager.onTouchEvent(event) || result;
	}
}
