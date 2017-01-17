package com.tq.zld.util;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.tq.zld.TCBApp;

public class BitmapUtils {

	/**
	 * 回收Bitmap资源
	 * 
	 * @param target
	 */
	public static void recycle(Bitmap target) {
		if (target != null && !target.isRecycled()) {
			target.recycle();
			target = null;
		}
		System.gc();
	}

	/**
	 * 从资源文件加载图片：自动降低图片质量，可有效防止OOM
	 * 
	 * @param context
	 * @param drawableid
	 * @return
	 */
	public static Bitmap decodeResource(int drawableid) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.ARGB_4444;
		opts.inPurgeable = true;
		opts.inInputShareable = true;
		// opts.inSampleSize = 2;
		// 获取资源图片
		InputStream is = TCBApp.getAppContext().getResources()
				.openRawResource(drawableid);
		final Bitmap background = BitmapFactory.decodeStream(is, null, opts);
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return background;
	}

	/**
	 * 根据指定的比例缩放图片
	 * 
	 * @param bitmap
	 * @param scale
	 * @return
	 */
	public static Bitmap scaleBitmap(Bitmap bitmap, float scale) {
		Bitmap output = Bitmap.createBitmap((int) (bitmap.getWidth() * scale),
				(int) (bitmap.getHeight() * scale), Config.RGB_565);

		Canvas canvas = new Canvas(output);

		Paint paint = new Paint();
		Matrix cm = new Matrix();

		float[] array = { 1 * scale, 0, 0, 0, 1 * scale, 0, 0, 0, 1 };
		cm.setValues(array);
		canvas.drawBitmap(bitmap, cm, paint);
		bitmap.recycle();
		return output;
	}

	/**
	 * 图像灰度化
	 * 
	 * @param bmSrc
	 * @return
	 */
	public static Bitmap bitmap2Gray(Bitmap bmSrc) {
		// 得到图片的长和宽
		int width = bmSrc.getWidth();
		int height = bmSrc.getHeight();
		// 创建目标灰度图像
		Bitmap bmpGray = null;
		bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		// 创建画布
		Canvas c = new Canvas(bmpGray);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmSrc, 0, 0, paint);
		return bmpGray;
	}

	/**
	 * 图像线性灰度化
	 * 
	 * @param image
	 * @return
	 */
	public static Bitmap lineGrey(Bitmap image) {
		// 得到图像的宽度和长度
		int width = image.getWidth();
		int height = image.getHeight();
		// 创建线性拉升灰度图像
		Bitmap linegray = null;
		linegray = image.copy(Config.ARGB_8888, true);
		// 依次循环对图像的像素进行处理
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				// 得到每点的像素值
				int col = image.getPixel(i, j);
				int alpha = col & 0xFF000000;
				int red = (col & 0x00FF0000) >> 16;
				int green = (col & 0x0000FF00) >> 8;
				int blue = (col & 0x000000FF);
				// 增加了图像的亮度
				red = (int) (1.1 * red + 30);
				green = (int) (1.1 * green + 30);
				blue = (int) (1.1 * blue + 30);
				// 对图像像素越界进行处理
				if (red >= 255) {
					red = 255;
				}

				if (green >= 255) {
					green = 255;
				}

				if (blue >= 255) {
					blue = 255;
				}
				// 新的ARGB
				int newColor = alpha | (red << 16) | (green << 8) | blue;
				// 设置新图像的RGB值
				linegray.setPixel(i, j, newColor);
			}
		}
		return linegray;
	}

	/**
	 * 图像二值化
	 * 
	 * @param graymap
	 * @return
	 */
	public static Bitmap gray2Binary(Bitmap graymap) {
		// 得到图形的宽度和长度
		int width = graymap.getWidth();
		int height = graymap.getHeight();
		// 创建二值化图像
		Bitmap binarymap = null;
		binarymap = graymap.copy(Config.ARGB_8888, true);
		// 依次循环，对图像的像素进行处理
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				// 得到当前像素的值
				int col = binarymap.getPixel(i, j);
				// 得到alpha通道的值
				int alpha = col & 0xFF000000;
				// 得到图像的像素RGB的值
				int red = (col & 0x00FF0000) >> 16;
				int green = (col & 0x0000FF00) >> 8;
				int blue = (col & 0x000000FF);
				// 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB
				int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
				// 对图像进行二值化处理
				if (gray <= 95) {
					gray = 0;
				} else {
					gray = 255;
				}
				// 新的ARGB
				int newColor = alpha | (gray << 16) | (gray << 8) | gray;
				// 设置新图像的当前像素值
				binarymap.setPixel(i, j, newColor);
			}
		}
		return binarymap;
	}
}
