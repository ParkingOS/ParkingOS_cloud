package com.zld.pcloud_sensor.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringContextUtil {
	
	private static ApplicationContext context = null;
	private static Byte[] syncLock = new Byte[]{};
	
	public static void initContext(){
		if(context != null){
			return;
		}
		synchronized (syncLock) {
			if(context == null){
				context = new ClassPathXmlApplicationContext("classpath:/application-context.xml");
			}
		}
	}
	
	public static <T> T getBean(String beanName, Class<T> type){
		return (T)context.getBean(beanName);
	}
}
