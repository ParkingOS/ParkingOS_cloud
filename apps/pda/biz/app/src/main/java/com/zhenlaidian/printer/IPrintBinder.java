package com.zhenlaidian.printer;

import android.os.Binder;

public class IPrintBinder extends Binder implements IPrint {

	/**
	 * 打印服务。
	 */
	public PrintService mPrintService = null;
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;构造函数。
	 * 
	 * @param printService 
	 */
	public IPrintBinder(PrintService printService){
		mPrintService = printService;
	}
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;用于设置打印的文本。
	 * 
	 * @param strText 需要打印的文本。
	 * 
	 */
	@Override
	public void setPrintText(String strText) {

		if(mPrintService != null){
			mPrintService.setPrintText(strText);
		}
		
	}

	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;停止打印。
	 */
	@Override
	public void stop() {
		
		if(mPrintService != null){
			mPrintService.stopPrint();
		}
		
	}

}
