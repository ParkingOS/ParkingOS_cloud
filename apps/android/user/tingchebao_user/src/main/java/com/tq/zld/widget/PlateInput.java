package com.tq.zld.widget;

import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class PlateInput extends ViewPager {

	private List<View> pages;

	public PlateInput(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PlateInput(Context context) {
		super(context);
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {

		// 初始化三种输入界面
		
		
		setAdapter(new PlateAdapter());
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		switch (arg0.getAction()) {
		case MotionEvent.ACTION_DOWN:

			break;
		case MotionEvent.ACTION_MOVE:

			break;
		case MotionEvent.ACTION_UP:

			break;

		}
		return super.onTouchEvent(arg0);
	}

	@Override
	public void setAdapter(PagerAdapter arg0) {
		// TODO Auto-generated method stub
		super.setAdapter(arg0);
	}

	class PlateAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return pages == null ? 0 : pages.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {

			container.addView(pages.get(position), position);
			return super.instantiateItem(container, position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			return;
		}
	}
}
