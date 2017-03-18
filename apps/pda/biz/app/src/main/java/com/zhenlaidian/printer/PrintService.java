package com.zhenlaidian.printer;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 类名: PrintService <br/><br/>
 * 
 * 功能: 用于自动打印业务的服务。
 * 
 */
public class PrintService extends Service {

	public IPrintBinder mIPrintBinder = null;
	
	/**
	 * 用于打印的线程。
	 */
	private PrintThread mThread = new PrintThread();
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;用于绑定服务的事件响应。
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		
		if(mIPrintBinder != null){
			mIPrintBinder = new IPrintBinder(PrintService.this);
		}
		
		return mIPrintBinder;
	}

	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;将该服务设置为前台服务.
	 */
	public void StartForeground(){
		
		//this.setForeground(true);
		
		Notification notification = new Notification();
		startForeground(1, notification);
		
	}
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;响应启动消息。
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		
		StartForeground();
		
		startPrintThread();
	}
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;
	 *
	 */
	private void startPrintThread(){
		mThread.start();
	}
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;设置打印的文本。
	 *
	 * @param strText
	 */
	public void setPrintText(String strText){
		
		if(mThread != null){
			mThread.setPrintText(strText);
		}
	}
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;停止打印。
	 *
	 */
	public void stopPrint(){
		
		if(mThread != null){
			mThread.Stop();
		}
		
	}
}
