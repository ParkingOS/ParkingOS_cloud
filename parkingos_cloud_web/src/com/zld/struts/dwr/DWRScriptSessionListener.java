package com.zld.struts.dwr;

import com.zld.utils.Constants;
import org.apache.log4j.Logger;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.event.ScriptSessionEvent;
import org.directwebremoting.event.ScriptSessionListener;

import javax.servlet.http.HttpSession;
import java.util.Collection;

;


/**
 * ScriptSession 监听器 ，监听ScriptSession的创建与销毁
 * @author 
 *
 */
public class DWRScriptSessionListener implements ScriptSessionListener {
	private Logger logger = Logger.getLogger(DWRScriptSessionListener.class);
	//维护一个Map key为session的id,value为ScriptSession 对象

	/**
	 * ScriptSession创建事件
	 */
	@Override
	public void sessionCreated(ScriptSessionEvent event) {
		// TODO Auto-generated method stub
		WebContext webContext = WebContextFactory.get();
		HttpSession session = webContext.getSession();
		ScriptSession scriptSession = event.getSession();
		//添加ScriptSession
		Constants.getInstance().scriptSessionMap.put(session.getId(), scriptSession);
		logger.error("zhangqiang 监控事件map session.getId()"+session.getId()+" "+Constants.getInstance().scriptSessionMap.toString());
		logger.error("zhangqiang 监控事件map换个方法取" +session.getId()+" "+Constants.getInstance().scriptSessionMap.toString());
		logger.error("zhangqiang session:"+session.getId()+" scriptSession:"+scriptSession.getId()+" is created!");
	}
	/**
	 * ScriptSession销毁事件
	 */
	@Override
	public void sessionDestroyed(ScriptSessionEvent event) {
		// TODO Auto-generated method stub
		WebContext webContext = WebContextFactory.get();
		HttpSession session = webContext.getSession();
		logger.error("监控事件有没有销毁"+Constants.getInstance().scriptSessionMap.toString());
		ScriptSession scriptSession = Constants.getInstance().scriptSessionMap.remove(session.getId());
		logger.error("session:"+session.getId()+" scriptSession:"+scriptSession.getId()+" is destoryed!");
	}
	
	public static Collection<ScriptSession> getScriptSessions(){
		return Constants.getInstance().scriptSessionMap.values();
		
	}

}
