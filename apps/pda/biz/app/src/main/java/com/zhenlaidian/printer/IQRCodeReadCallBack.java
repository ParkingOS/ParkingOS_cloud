package com.zhenlaidian.printer;

/**
 * 接口名: IQRCodeReadCallBack <br/><br/>
 * 
 * 功能: 用于回调二维码的读取结果。<br/>
 * 
 *
 */
public interface IQRCodeReadCallBack {

	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;当读取成功回调的接口。
	 *
	 * @param strText 读取到的文本。
	 * 
	 */
	public abstract void onQRCodeReadSuccess(String strText);
	
}
