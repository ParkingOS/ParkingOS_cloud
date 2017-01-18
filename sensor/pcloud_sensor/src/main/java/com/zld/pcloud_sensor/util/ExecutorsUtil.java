package com.zld.pcloud_sensor.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ExecutorsUtil {
	public static ScheduledExecutorService clientPool = Executors.newScheduledThreadPool(1);
}
