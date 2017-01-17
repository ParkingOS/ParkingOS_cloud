package com.tq.zld.util;

import android.animation.Animator.AnimatorListener;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;

public class ViewUtils {

	/**
	 * 播放属性动画
	 * 
	 * @param target
	 *            待播放动画的控件
	 * @param width
	 *            true表示改变控件宽度，false表示改变控件高度
	 * @param start
	 *            属性改变起点
	 * @param end
	 *            属性改变终点
	 * @param duration
	 *            动画时长
	 */
	public static void performAnimate(final View target, final boolean width,
			final int start, final int end, long duration,
			AnimatorListener listener) {
		ValueAnimator valueAnimator = ValueAnimator.ofInt(1, 100);

		valueAnimator.addUpdateListener(new AnimatorUpdateListener() {

			// 持有一个IntEvaluator对象，方便下面估值的时候使用
			private IntEvaluator mEvaluator = new IntEvaluator();

			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				// 获得当前动画的进度值，整型，1-100之间
				int currentValue = (Integer) animator.getAnimatedValue();
				// 计算当前进度占整个动画过程的比例，浮点型，0-1之间
				float fraction = currentValue / 100f;
				// 直接调用整型估值器通过比例计算出高度或宽度，然后再设给target
				if (width) {
					target.getLayoutParams().width = mEvaluator.evaluate(
							fraction, start, end);
				} else {
					target.getLayoutParams().height = mEvaluator.evaluate(
							fraction, start, end);
				}
				target.requestLayout();
			}
		});
		if (listener != null) {
			valueAnimator.addListener(listener);
		}
		valueAnimator.setDuration(duration).start();
	}

	/**
	 * 在OnCreate()方法中获取控件宽度
	 * 
	 * @param v
	 * @return
	 */
	public static int getMeasuredWidth(View v) {
		if (v == null) {
			return 0;
		}
		int w = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		v.measure(w, h);
		return v.getMeasuredWidth();
	}

	/**
	 * 在OnCreate()方法中获取控件高度
	 * 
	 * @param v
	 * @return
	 */
	public static int getMeasuredHeight(View v) {
		if (v == null) {
			return 0;
		}
		int w = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		v.measure(w, h);
		return v.getMeasuredHeight();
	}
}
