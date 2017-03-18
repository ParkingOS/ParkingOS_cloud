package com.zhenlaidian.printer;

/**
 * 功能:
 * &nbsp;&nbsp;&nbsp;用于服务通讯的接口。
 * 
 */
public interface IPrint {
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;用于设置打印的文本。
	 *
	 * @param strText
	 */
	public abstract void setPrintText(String strText);
	
	/**
	 * 功能:<br/>
	 * &nbsp;&nbsp;&nbsp;用于停止打印。
	 *
	 */
	public abstract void stop();
}
