package com.zld.schedule;

import java.util.Timer;
import java.util.TimerTask;


public class TcpTimerManager {
	
	private static final long PERIOD_DAY = 24*60*60 * 1000;
	public TcpTimerManager(){
		System.out.println(" ////////////////start--tcp≤‚ ‘***//////////////////////");
		TimerTask task = new TcpTimerTask();
		Timer timer = new Timer();
		timer.schedule(task, 10*1000L);
	}
}
