package com.zldpark.schedule;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.zldpark.facade.StatsAccountFacade;
import com.zldpark.impl.CommonMethods;
import com.zldpark.service.DataBaseService;
import com.zldpark.service.PgOnlyReadService;
import com.zldpark.service.StatsCardService;
import com.zldpark.utils.MemcacheUtils;
import com.zldpark.utils.TimeTools;

/**
 * 每十五分钟统计lala趋势
 * @author Administrator
 *
 */

public class TimerManager {
	
	Logger logger = Logger.getLogger(TimerManager.class);
	
	private static final long period1 = 10 * 60;//10分钟
	
	private static final long period2 = 30 * 60;//30分钟
	
	private static final long period3 = 60 * 60;//60分钟
	
	private static final long period4 = 24 * 60 * 60;//一天
	
	private static final long period5 = 1 * 60;//1分钟
	
	private static final long period6= 2 * 60;//2分钟
	
	
	

	public TimerManager(DataBaseService dataBaseService, PgOnlyReadService pgOnlyReadService,
			MemcacheUtils memcacheUtils, CommonMethods commonMethods, StatsAccountFacade accountFacade,
			StatsCardService cardService) {
		/*不管任务执行耗时是否大于间隔时间，scheduleAtFixedRate和scheduleWithFixedDelay都不会导致同一个任务并发地被执行。
		唯一不同的是scheduleWithFixedDelay是当前一个任务结束的时刻，开始结算间隔时间，如0秒开始执行第一次任务，任务耗时5秒，任务间隔时间3秒，
		那么第二次任务执行的时间是在第8秒开始。*/
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(20);
		//----------------------------1分钟运行一次-----------------------------------//
		DiciEventSchedule dicitask = new DiciEventSchedule(dataBaseService, pgOnlyReadService,
				memcacheUtils);
		executor.scheduleAtFixedRate(dicitask, getDelayTime(period5), period5, TimeUnit.SECONDS);
		//----------------------------10分钟运行一次-----------------------------------//
		TaskPeriod10m task1 = new TaskPeriod10m(dataBaseService, pgOnlyReadService,
				memcacheUtils, commonMethods);
		executor.scheduleAtFixedRate(task1, getDelayTime(period1), period1, TimeUnit.SECONDS);
		//----------------------------30分钟运行一次-----------------------------------//
		TaskPeriod30m task2 = new TaskPeriod30m(dataBaseService, pgOnlyReadService,
				memcacheUtils, commonMethods);
		executor.scheduleAtFixedRate(task2, getDelayTime(period2), period2, TimeUnit.SECONDS);
		//----------------------------1小时运行一次-----------------------------------//
		TaskPeriod1h task3 = new TaskPeriod1h(dataBaseService, pgOnlyReadService,
				memcacheUtils, commonMethods);
		executor.scheduleAtFixedRate(task3, getDelayTime(period3), period3, TimeUnit.SECONDS);
		//----------------------------1天运行一次-----------------------------------//
		TaskPeriod1d task4 = new TaskPeriod1d(dataBaseService, pgOnlyReadService,
				memcacheUtils, commonMethods, accountFacade, cardService);
		executor.scheduleAtFixedRate(task4, getDailyDelayTime(), period4, TimeUnit.SECONDS);
		
		//--------------------------之前的定时任务-------------------------------//
		ParkSchedule task5 = new ParkSchedule(dataBaseService, pgOnlyReadService,
				memcacheUtils, commonMethods);
		executor.scheduleAtFixedRate(task5, getDailyDelayTime(), period4, TimeUnit.SECONDS);
		

		Long ntime = System.currentTimeMillis();
		ParkSendMessage task6 = new ParkSendMessage(dataBaseService, pgOnlyReadService);
		executor.scheduleAtFixedRate(task6, 30, period6, TimeUnit.SECONDS);
		logger.error(">>>>>>>>>下发同步车场数据执行时间："+TimeTools.getTime_yyyyMMdd_HHmmss(ntime+30*1000));
		
		TicketSchedule task7 = new TicketSchedule(dataBaseService, pgOnlyReadService);
		executor.scheduleAtFixedRate(task7, getDailyDelayTime(), period4, TimeUnit.SECONDS);
		logger.error(">>>>>>>>>回收减免卷执行时间："+TimeTools.getTime_yyyyMMdd_HHmmss(ntime+getDailyDelayTime()*1000));
		

	}
	
	
	/**
	 * 获取第一次执行定时任务的起始时间
	 * @param period 间隔时间(单位/秒)
	 * @return
	 */
	private Long getDelayTime(long period){
		Long time = System.currentTimeMillis() / 1000;
		time = period - time % period;
		return time;
	}
	
	private Long getDailyDelayTime(){
		Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);//每天
        //每天的凌晨一点执行
        calendar.set(year, month, day, 01, 00, 00);
        Date date = calendar.getTime();
        if(date.before(new Date())){//如果凌晨一点的时间比当前时间早，就取第二天凌晨一点开始
        	calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        long time = calendar.getTimeInMillis() / 1000;
        Long curTime = System.currentTimeMillis() / 1000;
        long delay = time - curTime;
        logger.error("延迟"+delay+"秒执行每天一次的任务");
        return delay;
	}
}
