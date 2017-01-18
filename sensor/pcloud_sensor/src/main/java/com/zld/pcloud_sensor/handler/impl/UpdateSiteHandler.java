package com.zld.pcloud_sensor.handler.impl;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zld.pcloud_sensor.handler.IHandler;
@Service("siteHandler")
public class UpdateSiteHandler implements IHandler {
	
	Logger logger = Logger.getLogger(UpdateSiteHandler.class);
	
	@Override
	public Object failed(Exception e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object completed(String respBody) {
		try {
			logger.error(respBody);
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

}
