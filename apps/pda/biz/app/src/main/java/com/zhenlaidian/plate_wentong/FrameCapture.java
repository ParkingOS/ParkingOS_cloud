package com.zhenlaidian.plate_wentong;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FrameCapture {

	private int WIDTH = 0;
	private int HEIGHT = 0;
	private ByteArrayOutputStream baos;
	private byte[] data;
	private String capturePath;
	private String type = null;
	private String XmlPath = Environment.getExternalStorageDirectory()
			.toString() + "/AndroidWT/CaptureInfo.xml";
	private String projectType;
	private Bitmap bitmap;
	private BufferedOutputStream out;

	public FrameCapture(byte[] data, int width, int height, String projectType) {
		this.projectType = projectType;
		this.data = data;
		this.HEIGHT = height;
		this.WIDTH = width;

		if (!startParseXml(XmlPath)) {
			return;
		}

		capturePath = Environment.getExternalStorageDirectory().toString()
				+ "/WintoneSimpleCapture/" + type;

		File file = new File(capturePath);
		if (!file.exists()) {
			file.mkdirs();
		}

		NV21DataSave();

	}

	public FrameCapture(Bitmap bitmap, String projectType) {
		this.bitmap = bitmap;
		this.projectType = projectType;
		if (!startParseXml(XmlPath)) {
			return;
		}

		capturePath = Environment.getExternalStorageDirectory().toString()
				+ "/WintoneSimpleCapture/" + type;

		File file = new File(capturePath);
		if (!file.exists()) {
			file.mkdirs();
		}

		RGBDataSave();

	}

	private boolean NV21DataSave() {
		if (WIDTH == 0 && HEIGHT == 0) {
			return false;
		}
		YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, WIDTH, HEIGHT,
				null);
		baos = new ByteArrayOutputStream();
		yuvimage.compressToJpeg(new Rect(0, 0, WIDTH, HEIGHT), 100, baos);
		FileOutputStream outStream;
		String timeTag = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date(System.currentTimeMillis()));
		try {
			outStream = new FileOutputStream(capturePath + "/" + timeTag
					+ ".jpg");
			outStream.write(baos.toByteArray());
			outStream.close();
			baos.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean RGBDataSave() {
		if (bitmap == null) {
			return false;
		}

		String timeTag = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date(System.currentTimeMillis()));
		try {
			out = new BufferedOutputStream(new FileOutputStream(capturePath
					+ "/" + timeTag + ".jpg"));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}

	private boolean startParseXml(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return false;
		}

		XmlPullParser parser = Xml.newPullParser();

		try {
			InputStream in = new FileInputStream(file);
			parser.setInput(in, "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		try {
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String nodeName = parser.getName();
				if (eventType == XmlPullParser.START_TAG) {
					if (nodeName.equals("tags")) {
						String type = parser.getAttributeValue(0);
						Log.i("TAG", "type=" + type);
						if (type.equals(projectType)) {
							String capture = parser.getAttributeValue(1);
							if (capture.equals("yes")) {
								typePath(projectType);
								return true;
							} else {
								return false;
							}
						}
					}
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	private void typePath(String type) {
		int temp = Integer.valueOf(type);
		switch (temp) {
		case 10:
			this.type = "plateid";
			break;
		case 11:
			this.type = "idcard";
			break;
		case 14:
			this.type = "bankcard";
			break;
		}
	}
}
