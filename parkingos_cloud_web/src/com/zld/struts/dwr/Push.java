package com.zld.struts.dwr;

import org.apache.log4j.Logger;
import org.directwebremoting.*;

import java.util.Collection;

/**
 * 推送测试
 * @author 
 *
 */
public class Push {
	private static Logger logger = Logger.getLogger(Push.class);
	private static WebContext webContext = null;
	public static void onPageLoad(final String tag){
		webContext = WebContextFactory.get();
		ScriptSession scriptSession = WebContextFactory.get().getScriptSession();
		scriptSession.setAttribute("tag", tag);
		logger.error("dwr tag>>>>>>>>>>>>>>>>"+tag);
	}
	
	public static void test(String json) {
        final String msg = json;
        Browser.withAllSessions(new Runnable() {
 
            private ScriptBuffer script = new ScriptBuffer();
 
            public void run() {
                script.appendCall("showMessage", msg);
 
                Collection<ScriptSession> sessions = Browser.getTargetSessions();
 
                for (ScriptSession scriptSession : sessions) {
                    logger.error("dwr ===>>>loop execute sessions userId:"+scriptSession.getAttribute("userId"));
                    scriptSession.addScript(script);
                }
            }
        });
    }
	public static void popCenteVideo(final String msg, final Collection<ScriptSession> sessions) {
		logger.error("dwr>>>>>>popCenteVideo>>>>>>进入推送");
			ScriptSessionFilter filter = new ScriptSessionFilter() {
				
				@Override
				public boolean match(ScriptSession scriptSession) {
					logger.error("dwr>>>>>>popCenteVideo>>>>>>获取tag");
					String tag = (String) scriptSession.getAttribute("tag");
					logger.error("dwr>>>>>>popCenteVideo>>>>>>对比"+tag);
					return "statusTag".equals(tag);
				}
			};
			
			Runnable run = new Runnable() {
				private ScriptBuffer scriptBuffer = new ScriptBuffer();
				@Override
				public void run() {
					logger.error("dwr>>>>>>popCenteVideo>>>>>>获取sessions"+sessions);
					scriptBuffer.appendCall("popCenterVideo", msg);
					for (ScriptSession scriptSession : sessions) {
						scriptSession.addScript(scriptBuffer);
					}
				}
				
			};
			//执行推送
		logger.error("dwr>>>>>>popCenteVideo>>>>>>进入推送"+filter+">>>>run"+run);
			Browser.withAllSessionsFiltered(filter, run);
	}
	public static void sendStatusAuto(final String msg, final Collection<ScriptSession> sessions) {
		ScriptSessionFilter filter = new ScriptSessionFilter() {
			
			@Override
			public boolean match(ScriptSession scriptSession) {
				String tag = (String) scriptSession.getAttribute("tag");
				logger.error("dwr>>>>>>sendStatusAuto>>>>>>>对比"+tag);
				return "statusTag".equals(tag);
			}
		};
		
		Runnable run = new Runnable() {
			private ScriptBuffer scriptBuffer = new ScriptBuffer();
			@Override
			public void run() {
				scriptBuffer.appendCall("showMessage", msg);
				//Collection<ScriptSession> sessions = DWRScriptSessionListener.getScriptSessions();
				for (ScriptSession scriptSession : sessions) {
					scriptSession.addScript(scriptBuffer);
				}
			}
			
		};
		//执行推送
		Browser.withAllSessionsFiltered(filter, run);
    }
}
