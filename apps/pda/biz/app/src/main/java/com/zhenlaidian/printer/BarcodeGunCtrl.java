package com.zhenlaidian.printer;

import android.fpi.MtGpio;
import android.os.Handler;
import android.os.Looper;

import com.serialport.apis.PortControl;

/**
 * 类名: BarcodeGunCtrl <br/><br/>
 * 
 * 功能: 用于控制条码枪扫描条码。
 * 
 * @author Administrator
 *
 */
public class BarcodeGunCtrl {
	
	/**
	 * 用于读取的一维码 二维码的串口。
	 */
	private PortControl mCom = null;
	
	/**
	 * 是否开始读取。
	 */
	private boolean mIsReadding = false;
	
	/**
	 * 读取二维码的回调函数。
	 */
	private IQRCodeReadCallBack mIQRCodeReadCallBack = null;
	
	/**
	 * 读取二维码的线程。
	 */
	private Thread mQRCodeReadThread = null;
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;构造函数。
	 * 
	 */
	public BarcodeGunCtrl(){
		mCom = new PortControl();
	}
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;点亮二维码读写的灯头。
	 *
	 */
	public void LightQRCodeGun(){
		
		//////////////////////
		//
		// 二维码,reset
		MtGpio.getInstance().sGpioOut(118, 1); 

		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}

		MtGpio.getInstance().sGpioOut(118, 0);

		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}

		MtGpio.getInstance().sGpioOut(118, 1);

		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
		}

		/////////////////
		//
		// 打开二维码头的灯。
		MtGpio.getInstance().sGpioOut(117, 0);

	}
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;熄灭二维码扫描灯。
	 *
	 */
	public void extinctQRCodeGun(){
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		
		MtGpio.getInstance().sGpioOut(117, 1);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
	}
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;点亮二维码的扫描枪。
	 * 
	 */
	public void LightBarcodeGun(){
		
		MtGpio.getInstance().sGpioOut(117, 1);

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}

		MtGpio.getInstance().sGpioOut(117, 0);

	}
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;初始化串口。
	 *
	 * @return
	 */
	private boolean initComPort(){
		
		int iRet = mCom.getSPort().OpenPortWithPath("/dev/ttyMT2", 9600, 8, 'N', 1, 0, 512);
		
		if(iRet == -1){
			return false;
		}
		
		return true;
	}
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;用于试图读取条码。
	 *
	 * @return 试图读取条形码。
	 */
	public String tryToReadBarcode(){
		
		initComPort();
		LightBarcodeGun();
		
		return mCom.getSPort().tryToReadBarcode(8000);
	}

	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;关闭读取串口。
	 *
	 */
	public void closeComport(){
		
		if(mCom == null){
			return ;
		}
		
		mCom.freePortControl();
	}
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;开始读取二维码。
	 *
	 */
	public void startQRCodeRead(IQRCodeReadCallBack iqrCodeReadCallBack ){
		
		if(mQRCodeReadThread != null){
			stopQRCodeRead();
		}
		
		mIsReadding = true;
		
		mIQRCodeReadCallBack = iqrCodeReadCallBack;
		
	
		mQRCodeReadThread = new Thread(mQRCodeReadRunnable);//.start();
		
		mQRCodeReadThread.start();
	}
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;试图刷二维码。<br/>
	 * 
	 * 
	 * @return
	 */
	public String tryToReadQRCode(){
		
		LightQRCodeGun();
		initComPort();
		
		long lTimeMillis = System.currentTimeMillis();
		
		while(System.currentTimeMillis() - lTimeMillis  < 5000){
			
			LightBarcodeGun();
			
			String strCode = mCom.getSPort().tryToReadBarcode(8000);
			
			strCode = (strCode == null ? "" : strCode.trim());
			
			if(strCode.length() > 0){
				
				extinctQRCodeGun();
				return strCode;
			}
		}
		
		extinctQRCodeGun();
		return "";
	}
	
	
	public Runnable mQRCodeReadRunnable = new Runnable(){
		@Override
		public void run() {
			
			LightQRCodeGun();
			initComPort();
			
			while(mIsReadding == true){
				
				LightBarcodeGun();
				
				String strCode = mCom.getSPort().tryToReadBarcode(8000);
				
				strCode = (strCode == null ? "" : strCode.trim());
				
				if(strCode.length() > 0){
					raiseQRCodeReadCallBack(strCode);
				}
			}
			
		}
	};
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;停止二维码读取(必须放到线程中)。
	 *
	 */
	public void stopQRCodeRead(){
		
		mIsReadding = false;
		
		closeComport();
		
		if(mQRCodeReadThread != null){
			try {
				mQRCodeReadThread.join();
			} catch (InterruptedException e) {
			}
		}
		
		mQRCodeReadThread = null;
		
		extinctQRCodeGun();
	    		
	    
	}
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;通知二维码读取的回调函数。
	 *
	 * @param strText 读取到的二维码文本。
	 */
	public void raiseQRCodeReadCallBack(final String strText){
		
		if(mIQRCodeReadCallBack == null){
			return ;
		}
		
		if(Looper.getMainLooper() == Looper.myLooper()){
			mIQRCodeReadCallBack.onQRCodeReadSuccess(strText);
		}
		else{
			Handler handler = new Handler(Looper.getMainLooper());
			
			handler.post(new Runnable() {
				@Override
				public void run() {
					mIQRCodeReadCallBack.onQRCodeReadSuccess(strText);
				}
			});
			
		}
		
	}
	
}
