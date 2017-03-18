package com.zhenlaidian.camera;


import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

/**
 *
 * @author Administrator
 *
 */
public class ZoomImageView extends ImageView implements OnGlobalLayoutListener,
		OnScaleGestureListener, OnTouchListener {
	/**
	 * 值进行一次的图片缩放处理
	 */
	private boolean mOnce;

	/**
	 * 初始化时缩放的值
	 */
	private float mInitScale;
	/**
	 * 双击放大达到的值
	 */
	private float mMidScale; // 缩放的值在mInitScale和mMaxScale之间
	/**
	 * 放大的极限
	 */
	private float mMaxScale;

	/**
	 * 控制缩放位移的矩阵
	 */
	private Matrix mScaleMatrix;

	// --------------------------------

	/**
	 * 捕获用户多指触控时缩放的比例
	 */
	private ScaleGestureDetector mScaleGestureDetector;

	// -------------------->自由移动------------------

	/**
	 * 记录上一次多点触控的数量
	 */
	private int mLastPointCount;

	// 最后触控的中心点位置
	private float mLastX;
	private float mLastY;
	/**
	 * 一个值，用于判断是否是移动的
	 */
	private int mTouchSlop;
	/**
	 * 是否可以移动
	 */
	private boolean isCanDrag;

	private boolean isCheckLeftAndRight;
	private boolean isCheckTopAndBottom;

	// ----------------------双击放大缩小--------

	private GestureDetector mGestureDetector;

	/**
	 * 用于判断当前是否处于缩放状态。防止用户重复双击
	 */
	private boolean isAutoScale;

	public ZoomImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// init
		mScaleMatrix = new Matrix();
		setScaleType(ScaleType.MATRIX);

		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		// 设置监听事件
		setOnTouchListener(this);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mGestureDetector = new GestureDetector(context,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onDoubleTap(MotionEvent e) {

						if (isAutoScale) {
							return true;
						}

						// 点击的中心
						float x = e.getX();
						float y = e.getY();

						if (getScale() < mMidScale) {
							// mScaleMatrix.postScale(mMidScale / getScale(),
							// mMidScale / getScale(), x, y);// 放大到指定大小
							// setImageMatrix(mScaleMatrix);
							postDelayed(new AutoScaleRunnable(mMidScale, x, y),
									16);
//							isAutoScale = true;
						} else {
							// 缩小
							// mScaleMatrix.postScale(mInitScale / getScale(),
							// mInitScale / getScale(), x, y);// 放大到指定大小
							// setImageMatrix(mScaleMatrix);
							postDelayed(
									new AutoScaleRunnable(mInitScale, x, y), 16);
//							isAutoScale = true;
						}

						return true;
					}
				});
	}

	/**
	 * 自动放大与缩小
	 *
	 * @author Zhang
	 *
	 */
	private class AutoScaleRunnable implements Runnable {

		/**
		 * 缩放的目标值
		 */
		private float mTargetScale;
		// 缩放的中心点
		private float x;
		private float y;
		/**
		 * 缩放的梯度，每次1.07f 放大，0.93的缩小
		 */
		private final float BIGGER = 1.07f;
		private final float SMALL = 0.93f;

		private float temScale;// 临时

		public AutoScaleRunnable(float mTargetScale, float x, float y) {
			this.mTargetScale = mTargetScale;
			this.x = x;
			this.y = y;

			if (getScale() < mTargetScale) {
				temScale = BIGGER;// 代表还要放大
			}
			if (getScale() >mTargetScale) {
				temScale = SMALL;
			}
		}

		@Override
		public void run() {
			// 进行缩放
			mScaleMatrix.postScale(temScale, temScale, x, y);
			checkBorderAndCenterWhenScale();// 检测
			setImageMatrix(mScaleMatrix);

			float currentScale = getScale();
			// 当temScale大于1.且当前的scale还小于目标值，可以放大,反之可以缩小
			if ((temScale > 1.0f && currentScale < mTargetScale)
					|| (temScale < 1.0f && currentScale > mTargetScale)) {
				postDelayed(this, 16);// 每16秒执行这个方法，肉眼看起来是顺畅的缩放动作
			} else {
				// 达到目标值,设置目标值
				float scale = mTargetScale / currentScale;
				mScaleMatrix.postScale(scale, scale, x, y);
				checkBorderAndCenterWhenScale();
				setImageMatrix(mScaleMatrix);

				// 结束
				isAutoScale = false;
			}
		}

	}

	public ZoomImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ZoomImageView(Context context) {
		this(context, null);
	}

	/**
	 * 当onAttachedToWindow
	 */
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	/**
	 * 当onDetachedFromWindow
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

	/**
	 * 全局的布局加载完成后，调用此方法 。 获取ImageView加载完成的图片,使图片居中缩放
	 */
	@Override
	public void onGlobalLayout() {
		if (!mOnce) {
			// 得到图片的宽高
			int width = getWidth();
			int height = getHeight();
			// 得到图片，以及宽和高
			Drawable d = getDrawable();
			if (d == null)
				return;
			// 拉伸后的宽度.而不是真正图片的宽度
			int dw = d.getIntrinsicWidth();
			int dh = d.getIntrinsicHeight();

			Log.d("Debug", "width:" + width + ",height:" + height + ",dw:" + dw
					+ ",dh:" + dh);

			float scale = 1.0f;// 默认缩放的值

			// 将图片的宽高和控件的宽高作对比，如果图片比较小，则将图片放大，反之亦然。
			// 如果图片的宽度大于控件的宽度,并且图片高度小于控件高度
			if (dw > width && dh < height) {
				scale = width * 1.0f / dw;// 图片太宽，宽度缩放
				Log.d("Debug", "图片宽大，高小");
			}

			if (dh > height && dw < width) {
				scale = height * 1.0f / dh;// 图片太高，高度缩放
				Log.d("Debug", "图片高大，宽小");
			}
			// // 如果宽高都大于控件宽高??
			// if (dw > width && dh > height) {
			// scale = Math.min(width * 1.0f / dw, height * 1.0f / dw);
			// }
			// // 图片宽高都小于控件的宽高 上下两个一样
			// if (dw < width && dh < height) {
			// scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
			// }

			if (dw < width && dh < height || dw > width && dh > height) {
				scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
				Log.d("Debug", "图片宽高都大，或都小");
			}

			// 得到初始化缩放的比例
			mInitScale = scale;// 原大小
			mMaxScale = mInitScale * 4;// 4倍
			mMidScale = mInitScale * 2;// 2倍，双击放大达到的值

			// 将图片移动到控件的中心
			int dx = getWidth() / 2 - dw / 2;// 向x轴移动dx距离
			int dy = getHeight() / 2 - dh / 2;// 向y轴移动dx距离

			/**
			 * matrix: xScale xSkew xTrans 需要9个 ySkew yScale yTrans 0 0 0
			 */
			mScaleMatrix.postTranslate(dx, dy);// 平移
			mScaleMatrix.postScale(mInitScale, mInitScale, width / 2,
					height / 2);// 缩放,正常显示width/2,height/2中心
			setImageMatrix(mScaleMatrix);

			mOnce = true;
		}
	}

	/**
	 * 得到当前缩图片放值
	 *
	 * @return
	 */
	public float getScale() {
		float[] values = new float[9];
		mScaleMatrix.getValues(values);
		return values[Matrix.MSCALE_X];
	}

	// 缩放----------------------------------
	// 缩放的区间，initScale 和maxScale之间
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		float scale = getScale();
		// 得到缩放的值
		// getScaleFactor()这个方法很重要，它的含义是根据你的手势缩放程度预期得到的图片大小和当前图片大小的一个比值，
		// 当达到最大或最小值时让缩放的量为1就行，按老师那样的计算，在缩放到最大值或最小值后，有可能出现不能再缩放的情况
		float scaleFactor = detector.getScaleFactor();// 返回从前一个伸缩事件至当前伸缩事件的伸缩比率,
		if (getDrawable() == null) {
			return true;
		}
		// Log.d("Debug", "scaleFactor--> " + scaleFactor + ".");
		// 缩放范围的控制
		// 当前缩放在缩放极限内，缩放一因子大于1，说明想放大，小于1说明想缩小
		if ((scale < mMaxScale && scaleFactor > 1.0f)
				|| (scale > mInitScale && scaleFactor < 1.0f)) {
			// 不能小于最小值
			if (scale * scaleFactor < mInitScale) {
				// 小于的话重置
				scaleFactor = mInitScale / scale;// scale=mInitScale/scaleFactory
			}

			if (scale * scaleFactor > mMaxScale) {
				// 大于也充值
				scaleFactor = mMaxScale / scale;// scale当前缩放
			}
			// 缩放，大的或太小的复原
			/*
			 * scale 表示的是当前图片基于原图放大的比例 mScaleMatix.postScale(scaleFactor,
			 * scaleFactor, getWidth() / 2,getHeight() /
			 * 2);中的scaleFactor参数表示基于当前已放大的图片再放大scaleFactor倍
			 * 。所以图片的实际放大的大小是原图的scaleFactor
			 * *scale倍。也就是说，当前方法postScale传入的参数是scaleFactor
			 * ，则图片实际基于原图放大的倍数是scaleFactor*scale 等式scaleFactor=mInitScale/scale
			 * 可以推导出---》 scaleFactor*scale = mInitScale -----》 此时的scaleFactor
			 * 作为postScale的参数，实际图片的缩放大小就是mInitScale
			 */
			mScaleMatrix.postScale(scaleFactor, scaleFactor,
					detector.getFocusX(), detector.getFocusY());// detector.getFocusX(),
			// detector.getFocusY()获得触摸的中心点，这样放大是可以按照触摸的中心放大，缩小就会有问题，出现图片显示偏移。
			// 不断的检测，
			checkBorderAndCenterWhenScale();
			setImageMatrix(mScaleMatrix);
		}

		return true;
	}

	/**
	 * 要拿到图片放大缩小以后的四角的坐标，以及宽高
	 *
	 * @return
	 */
	private RectF getMatrixRectF() {
		Matrix matrix = mScaleMatrix;
		RectF rectF = new RectF();

		Drawable d = getDrawable();
		// 得到图片放大或缩小以后的宽和高
		if (d != null) {
			rectF.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			matrix.mapRect(rectF);
		}

		return rectF;
	}

	/**
	 * 在缩放的时候进行边界控制，以及我们的位置的控制
	 */
	private void checkBorderAndCenterWhenScale() {
		RectF rect = getMatrixRectF();

		// 要移动的偏移距离。
		float deltaX = 0;
		float deltaY = 0;

		int width = getWidth();
		int height = getHeight();

		// 缩放时，进行边界检测，防止出现白边。
		if (rect.width() >= width) {// rect.width()放大或缩小后的宽度
			// 放大缩小后的图片和屏幕左边的空隙。
			if (rect.left > 0) {// 加入图片没有完全在屏幕中，而是往右边移动了一小段距离，rect.left：估计是个坐标
				// Log.d("Debug", "rect.left的偏移距离是：" + rect.left);
				deltaX = -(rect.left);
			}
			// 右边里屏幕有空隙
			if (rect.right < width) {
				deltaX = width - rect.right;// 需要向右边移动
				// Log.d("Debug", " X轴rect.right < width");
			}
		}

		if (rect.height() >= height) {
			if (rect.top > 0) {
				deltaY = -rect.top;
				// Log.d("Debug", " Y轴rect.height() >= height");
			}

			if (rect.bottom < height) {
				deltaY = height - rect.bottom;
				// Log.d("Debug", " Y轴rect.bottom < height");
			}
		}

		// 如果宽度或高度小于控件的宽度或高度，则让其居中
		if (rect.width() < width) {
			deltaX = width / 2f - rect.right + rect.width() / 2f;// x轴移动的距离。移到中心点
			// Log.d("Debug", " 宽度小于控件的宽度rect.right < width");
		}
		if (rect.height() < height) {
			deltaY = height / 2f - rect.bottom + rect.height() / 2f;
			// Log.d("Debug", " 高度小于控件的高度rect.height() < height");
		}

		mScaleMatrix.postTranslate(deltaX, deltaY);
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;// 得return true；
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event)) {
			return true;
		}
		mScaleGestureDetector.onTouchEvent(event);// 给mScaleGestureDetector处理

		// 多点触控的中心点
		float x = 0;
		float y = 0;
		// 拿到多点触控的数量
		int pointerCount = event.getPointerCount();
		for (int i = 0; i < pointerCount; i++) {
			// 拿到所有点的x，y轴的值，除以pointerCount，得到中心
			x += event.getX(i);
			y += event.getY(i);
		}

		x /= pointerCount;
		y /= pointerCount;

		// 如果最后触控点数量,,一开始是不相等的
		if (mLastPointCount != pointerCount) {
			isCanDrag = false;
			mLastX = x;
			mLastY = y;
		}

		mLastPointCount = pointerCount;

		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				// 记录偏移量
				float dx = x - mLastX;
				float dy = y - mLastY;

				if (!isCanDrag) {
					isCanDrag = isMoveAction(dx, dy);
				}

				// 判断是否可以移动
				if (isCanDrag) {
					RectF rectF = getMatrixRectF();// 得到缩放后的图片的宽高
					if (getDrawable() != null) {
						// 默认可以全部检测
						isCheckLeftAndRight = isCheckTopAndBottom = true;

						// 如果图片小于控件的宽度
						if (rectF.width() < getWidth()) {
							isCheckLeftAndRight = false;
							dx = 0;// 不允许横向移动
						}
						// 如果图片的高度小于控件的高度
						if (rectF.height() < getHeight()) {
							isCheckTopAndBottom = false;// 不用检测，不移动的时候
							dy = 0;// 不允许纵向移动
						}

						mScaleMatrix.postTranslate(dx, dy);
						checkBorderWhenTranslate();// 判断移动时，是否到边界
						setImageMatrix(mScaleMatrix);// 移动
					}
				}
				// 记录
				mLastX = x;
				mLastY = y;
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mLastPointCount = 0;
				break;
		}

		return true;
	}

	/**
	 * 当移动时，进行边界检查
	 */
	private void checkBorderWhenTranslate() {
		RectF rectF = getMatrixRectF();

		float deltaX = 0;
		float deltaY = 0;

		int width = getWidth();
		int height = getHeight();

		if (rectF.top > 0 && isCheckTopAndBottom) {
			Log.d("Debug", " top大于0：rectF.top=" + rectF.top);
			deltaY = -rectF.top;
		}

		if (rectF.bottom < height && isCheckTopAndBottom) {
			deltaY = height - rectF.bottom;
			Log.d("Debug", " bottom小于高度，rectF.bottom=" + rectF.bottom);
		}
		if (rectF.left > 0 && isCheckLeftAndRight) {
			deltaX = -rectF.left;
			Log.d("Debug", " left大于0，rectF.left=" + rectF.left);
		}
		if (rectF.right < width && isCheckLeftAndRight) {
			deltaX = width - rectF.right;
			Log.d("Debug", " 右边小于宽度，rectF.right=" + rectF.right);
		}
		mScaleMatrix.postTranslate(deltaX, deltaY);
	}

	/**
	 * 判断是否足矣触发移动
	 *
	 * @param dx
	 * @param dy
	 * @return
	 */
	private boolean isMoveAction(float dx, float dy) {

		return Math.sqrt(dx * dx + dy * dy) > mTouchSlop;// 对角线的长度
	}

}
