package com.zhenlaidian.printer;

import com.zsd.printer.PrinterCtrl;

/**
 * 类名: PrintThread <br/><br/>
 * 
 * 功能: 用于打印的线程。
 * 
 * @author Administrator
 *
 */
public class PrintThread extends Thread {

	/**
	 * 需要打印的文本。
	 */
	private String mstrText = ""; 
	
	/**
	 * 是否停止打印。
	 */
	private boolean mIsStop = false;
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;设置打印的文本。
	 *
	 * @param strText 文本。
	 */
	public void setPrintText(String strText){
		
		strText = (strText == null ? "" : strText.trim());
		
		mstrText = strText;
	}
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;用于开始打印。
	 */
	@Override
	public synchronized void start() {
		super.start();
		
		mIsStop = false;
	}
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;用于执行打印业务的任务。
	 * 
	 */
	@Override
	public void run() {
		super.run();
		
		while (mIsStop == false) {
			
			PrinterCtrl.powerOn();
			PrinterCtrl.PrintText(mstrText + "\n\n\n\n");
			
			try {
				sleep(1000);
			} catch (InterruptedException e) {
			}
			
		}
	}
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;用于停止打印服务。
	 *
	 */
    public void Stop(){
    	mIsStop = false;
    }
	
}
