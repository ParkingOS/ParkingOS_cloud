package com.zld.pcloud_sensor.handler.impl;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.zld.pcloud_sensor.handler.IHandler;
import com.zld.pcloud_sensor.pojo.SensorInfo;
import com.zld.pcloud_sensor.pojo.SensorResp;
import com.zld.pcloud_sensor.service.MemcacheService;
@Service("sensorHandler")
public class UpdateSensorHandler implements IHandler {
	Logger logger = Logger.getLogger(UpdateSensorHandler.class);
	@Autowired
	private MemcacheService memcacheService;
	
	@Override
	public Object failed(Exception e) {
		try {
			e.printStackTrace();
		} catch (Exception e2) {
			// TODO: handle exception
		}
		return null;
	}
	
	@Override
	public Object completed(String respBody) {
		try {
			logger.error(respBody);
			if(respBody != null && !"".equals(respBody)){
				Document document = DocumentHelper.parseText(respBody);
				//获取根节点元素对象  
		        Element root = document.getRootElement();
		        SensorResp resp = new SensorResp();
		        listNodes(root, resp);
		        if(resp.getError() == null){
		        	logger.error("parseXML success!");
		        	String type = resp.getType();
		        	String sensorID = resp.getSensornumber();
		        	Gson gson = new Gson();
		        	String json = memcacheService.get(sensorID);
					SensorInfo sensorInfo = gson.fromJson(json, SensorInfo.class);
		        	logger.error("sensorInfo:"+sensorInfo);
		        	if("in".equals(type)){
		        		sensorInfo.setStatus(1);
		        		boolean b = memcacheService.set(sensorID, gson.toJson(sensorInfo));
			        	logger.error("进车成功：b:"+b);
		        	}else if("out".equals(type)){
		        		sensorInfo.setStatus(0);
		        		boolean b = memcacheService.set(sensorID, gson.toJson(sensorInfo));
		        		logger.error("出车成功：b:"+b);
		        	}
		        }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Object cancelled() {
		// TODO Auto-generated method stub
		return null;
	}
	
	//遍历当前节点下的所有节点  
    @SuppressWarnings("unchecked")
	public void listNodes(Element node, SensorResp resp){
        //如果当前节点内容不为空，则输出  
        if(!(node.getTextTrim().equals(""))){  
             String name = node.getName();
             String text = node.getText();
             if("error".equals(name)){
            	 resp.setError(text);
             }else if("sensornumber".equals(name)){
            	 resp.setSensornumber(text);
             }else if("type".equals(name)){
            	 resp.setType(text);
             }
        }  
        //同时迭代当前节点下面的所有子节点  
        //使用递归  
        Iterator<Element> iterator = node.elementIterator();  
        while(iterator.hasNext()){  
            Element e = iterator.next();  
            listNodes(e, resp);  
        }
    }
}
