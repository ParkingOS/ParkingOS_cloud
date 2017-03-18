package com.zhenlaidian.plate_wentong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;


public final class ViewfinderView extends View {
	private final Paint paint;
	private final Paint paintLine;
	public Rect frame;
	int w, h;
	private boolean boo;
	public     int  length =0;
	public int t, b, l, r;

	public ViewfinderView(Context context, int w, int h, boolean boo) {
		super(context);
		this.w = w;
		this.h = h;
		this.boo = boo;
		paint = new Paint();
		paintLine = new Paint();
	}

	@Override
	public void onDraw(Canvas canvas) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		 //这个矩形就是中间的扫描框
		if (boo) {

//			int $t = h / 3;
//			t = $t;
//			b = h - t;
//			int $l = (int) ((b - t) * 1.585);
//			l = (w - $l) / 2;
//			r = w - l;
//
//			l = (int) (l -(w*0.026));
//			t = (int) (t + (h*0.046));
//			r = r - 0;
//			b = (int) (b - (h*0.046));
			if(height<1080||height>1620){
				length = height/4;
			}else{
				length = 250;
			}		
		} else {
//			int $t = h / 3;
//			t = $t;
//			b = h - t;
//			int $l = (int) ((b - t) * 1.585);
//			l = (w - $l) / 2;
//			r = w - l;
//
//			l = (int) (l + (w*0.138));
//			t = (int) (t + (h*0.098));
//			r = (int) (r - (w*0.138));
//			b = (int) (b - (h*0.098));\
			if(width<1080||width>1620){
				length = width/4;
			}else{
				length=250;	
			}
			
			
		}
//		l = w/2-length;
//		r = w/2+length;
		l = length/2;
		r = w-length/2;
		t = h/2-length;
		b = h/2+length;
		frame = new Rect(l, t, r, b);
		// 画阴影部分，分四部分，从屏幕上方到扫描框的上方，从屏幕左边到扫描框的左边
		// 从扫描框右边到屏幕右边，从扫描框底部到屏幕底部
		paint.setColor(Color.argb(128, 0, 0, 0));
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
				paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);

		paintLine.setColor(Color.rgb(0, 255, 0));
		paintLine.setStrokeWidth(4);
		paintLine.setAntiAlias(true);
		canvas.drawLine(l, t, l +50, t, paintLine);
		canvas.drawLine(l, t, l, t + 50, paintLine);
		canvas.drawLine(r, t, r - 50, t, paintLine);
		canvas.drawLine(r, t, r, t + 50, paintLine);
		canvas.drawLine(l, b, l + 50, b, paintLine);
		canvas.drawLine(l, b, l, b - 50, paintLine);
		canvas.drawLine(r, b, r - 50, b, paintLine);
		canvas.drawLine(r, b, r, b - 50, paintLine);
		// }

		if (frame == null) {
			return;
		}

	}

}
