package com.zld.pcloud_sensor.handler;

public interface IHandler {
	/** 
     * 处理异常时，执行该方法 
     * @return 
     */  
    public Object failed(Exception e);  
      
    /** 
     * 处理正常时，执行该方法 
     * @return 
     */  
    public Object completed(String respBody);  
      
    /** 
     * 处理取消时，执行该方法 
     * @return 
     */  
    public Object cancelled();
}
