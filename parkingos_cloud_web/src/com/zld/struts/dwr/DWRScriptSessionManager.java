package com.zld.struts.dwr;

import org.directwebremoting.impl.DefaultScriptSessionManager;
/**
 * ScriptSessionManager管理器 初始化注册监听器
 * @author 
 *
 */
public class DWRScriptSessionManager extends DefaultScriptSessionManager {
	public DWRScriptSessionManager(){
		//绑定一个ScriptSession增加销毁事件的监听器
		this.addScriptSessionListener(new DWRScriptSessionListener());
	}

}
