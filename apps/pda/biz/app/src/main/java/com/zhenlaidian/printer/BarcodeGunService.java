package com.zhenlaidian.printer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.zsd.barcode.gun.BarcodeGunCtrl;

import java.io.IOException;

public class BarcodeGunService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		tryScanBarcode();
	}

	public void tryScanBarcode(){
		
		new Thread(){
			public void run() {
				
				BarcodeGunCtrl barcodeGunCtrl = new BarcodeGunCtrl();
				String strBarcode = barcodeGunCtrl.tryToReadBarcode();
			
				Log.i("inputtext", strBarcode);
				
				//CommonFunc.MessageBox(BarcodeGunService.this, "inputtext", strBarcode);
				
				try {
					String strInputText = "input text " + strBarcode;
					Runtime.getRuntime().exec(strInputText);
					
					//Log.i("inputtext", strInputText);
				} catch (IOException e) {
					//Log.i("inputtext", e.getMessage());
				} 
				
				
			};
		}.start();
		
	}
}
