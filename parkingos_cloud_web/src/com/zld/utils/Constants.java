package com.zld.utils;

import org.apache.log4j.Logger;
import org.directwebremoting.ScriptSession;

import java.util.HashMap;
import java.util.Map;

public class Constants {
	private Logger logger = Logger.getLogger(Constants.class);
	public   Map<String,ScriptSession> scriptSessionMap = null;
	private Constants() {
		scriptSessionMap = new HashMap<String,ScriptSession>();
		logger.error("初始化 Constants ~~~ ");
	}
	private static Constants single=null;
	public static int AUTH_FLAG_COLLECTOR = 2;


	//静态工厂方法
	public static Constants getInstance() {
		if (single == null) {
			single = new Constants();
		}
		return single;
	}
}



