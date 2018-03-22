package com.zld.struts.scheduler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zld.impl.CommonMethods;
import com.zld.impl.MemcacheUtils;
import com.zld.service.DataBaseService;
import com.zld.struts.dwr.DWRScriptSessionListener;
import com.zld.struts.dwr.Push;
import org.apache.log4j.Logger;
import org.directwebremoting.ScriptSession;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class AppOrderTask extends TimerTask {
	private Logger logger = Logger.getLogger(AppOrderTask.class);
	private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
	DataBaseService daService;
	CommonMethods commonMethods;
	MemcacheUtils memcacheUtils;
	public AppOrderTask(DataBaseService dataBaseService, CommonMethods commonMethods){
		this.daService = dataBaseService;
		this.commonMethods = commonMethods;
	}
	@Override   
	public void run()  { 
    	try {
    		//Map<String, Object> monMap = daService.getMap("select p.main_phone_type, m.play_src,m.id from phone_info_tb p, monitor_info_tb m where m.id=p.monitor_id and p.is_call= ? order by p.call_time asc limit 1", new Object[]{1});
    		Map<String, Object> monMap = new HashMap<String,Object>();
			Collection<ScriptSession> sessions = DWRScriptSessionListener.getScriptSessions();
			if(sessions != null && sessions.size() >0){//有dwr监听事件再推送消息
				Push.sendStatusAuto(gson.toJson(monMap),sessions);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
	}  
}
