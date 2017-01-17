package com.tq.zld.util;
/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

/**
 * This class does the work of decoding the user's request and extracting all the data
 * to be encoded in a barcode.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public class QRCodeEncoder {

  private static final String TAG = QRCodeEncoder.class.getSimpleName();

  public QRCodeEncoder(){
	 
 }
 
 public Bitmap encode2BitMap(String contents, int QR_WIDTH, int QR_HEIGHT){
	 Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		//hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		BitMatrix result;

		try {
			result = new MultiFormatWriter().encode(contents,
					BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
			int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
      
         for (int y = 0; y < QR_HEIGHT; y++)
         {
             for (int x = 0; x < QR_WIDTH; x++)
             {
                 if (result.get(x, y))
                 {
                     pixels[y * QR_WIDTH + x] = 0xff000000;
                 }
                 else
                 {
                     pixels[y * QR_WIDTH + x] = 0xffffffff;
                 }
             }
         }
         
         Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
         bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
         return bitmap;
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
 }
 
 public void encode2file(String contents, int QR_WIDTH, int QR_HEIGHT, String imgPath) {
		   saveMyBitmap(imgPath+".png",encode2BitMap(contents, QR_WIDTH, QR_HEIGHT));
 }
 
 public void saveMyBitmap(String path,Bitmap mBitmap){
		File f = new File(path);
		FileOutputStream fOut = null;
		try {
			f.createNewFile();
			fOut = new FileOutputStream(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
