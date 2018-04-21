package com.zldpark.servlet;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.zldpark.facade.StatsAccountFacade;
import com.zldpark.impl.CommonMethods;
import com.zldpark.schedule.TimerManager;
import com.zldpark.service.DataBaseService;
import com.zldpark.service.PgOnlyReadService;
import com.zldpark.service.StatsCardService;
import com.zldpark.utils.MemcacheUtils;

public class TaskServlet extends HttpServlet {
	
	@Autowired
	private DataBaseService dataBaseService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private StatsAccountFacade accountFacade;
	@Autowired
	@Resource(name = "card")
	private StatsCardService cardService;
	
	
	Logger logger = Logger.getLogger(TaskServlet.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 9122771659780215777L;

	@Override
	public void init() throws ServletException {
		//启动定时任务
		logger.error("启动定时任务");
		new TimerManager(dataBaseService, pgOnlyReadService,
				memcacheUtils, commonMethods, accountFacade, cardService);
	}

}
