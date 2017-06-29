package com.zld.schedule;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.zld.CustomDefind;
import com.zld.sdk.doupload.impl.DoUploadImpl;
import com.zld.sdk.tcp.NettyChannelMap;
import com.zld.service.DataBaseService;

public class TcpTaskServlet extends HttpServlet {
	
	Logger logger = Logger.getLogger(TcpTaskServlet.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 6618370766105237297L;

	/**
	 * Constructor of the object.
	 */
	public TcpTaskServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request,response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request,response);
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		String tcpOnOff = CustomDefind.getValue("TCP_ON_OFF");
		logger.error(">>>>>>>>>tcp swith:"+tcpOnOff);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		if(tcpOnOff!=null&&tcpOnOff.equals("on")){
			logger.error("////////////////加载添加的servlet--------start////////////////");
			// 启动定时任务
			DoUploadImpl doUploadImpl = (DoUploadImpl) ctx.getBean("doUploadImpl");
			logger.error(">>>>>>>>>>>>>>>>>>>>>>tcphandel:"+doUploadImpl);
			NettyChannelMap.setUploadUtil(doUploadImpl);
			TimerTask task = new TcpTimerTask();
			Timer timer = new Timer();
			timer.schedule(task, 5*1000L);
			logger.error(">>>>>>>>>>>>>tcp start at 5 secondes later....");
		}
		//启动月卡同步
		logger.error("开始每2分钟的月卡同步功能.......");
		DataBaseService dataBaseService = (DataBaseService) ctx.getBean("dataBaseService");
		ParkSchedule task = new ParkSchedule(dataBaseService);
		Timer timer = new Timer();
		timer.schedule(task, 5*1000L,120000);
	}

}
