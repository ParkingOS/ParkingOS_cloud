package com.tq.zld.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * 实现可监听滚动事件的ScrollView
 * 
 * @author Clare
 * 
 */
public class StickyScrollView extends ScrollView {

	private OnScrollListener onScrollListener;

	public StickyScrollView(Context context) {
		this(context, null);
	}

	public StickyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public StickyScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * 设置滚动接口
	 * 
	 * @param onScrollListener
	 */
	public void setOnScrollListener(OnScrollListener onScrollListener) {
		this.onScrollListener = onScrollListener;
	}

	@Override
	public int computeVerticalScrollRange() {
		return super.computeVerticalScrollRange();
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (onScrollListener != null) {
			onScrollListener.onScroll(t);
		}
	}

	/**
	 * 
	 * 滚动的回调接口
	 * 
	 * @author clare
	 * 
	 */
	public interface OnScrollListener {
		/**
		 * 回调方法， 返回MyScrollView滑动的Y方向距离
		 * 
		 * @param scrollY
		 */
		public void onScroll(int scrollY);
	}

}