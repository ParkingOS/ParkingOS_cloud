package com.zld.struts.scheduler;

import com.zld.impl.CommonMethods;
import com.zld.service.DataBaseService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Timer;
  
public class TimeTaskServlet extends HttpServlet{  
	private static final long serialVersionUID = 1L;

	public TimeTaskServlet() {
		super();
	}

	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request,response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request,response);
	}

	public void init() throws ServletException {
        try {
        	ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
    		DataBaseService dataBaseService = (DataBaseService) ctx.getBean("dataBaseService");
    		CommonMethods commonMethods = (CommonMethods) ctx.getBean("commonMethods");
    		AppOrderTask task = new AppOrderTask(dataBaseService,commonMethods);
    		Timer timer = new Timer();
    		timer.schedule(task, 2*1000L,5*1000);//频率：30分钟/次
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
         
	}
} 
