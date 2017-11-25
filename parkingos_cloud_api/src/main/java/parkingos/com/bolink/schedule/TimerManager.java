package parkingos.com.bolink.schedule;

import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import parkingos.com.bolink.utlis.CommonUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;


/**
 * @author Administrator
 *
 */
public class TimerManager {
	private static final long PERIOD_DAY = 120* 1000;//间隔两分钟  60S * 1000
	private static final long TICKET_PERIOD_DAY = 24 * 60 * 60 * 1000;//时间间隔(一天)
	private Logger logger = Logger.getLogger(TimerManager.class);

	public TimerManager(CommonDao commonDao, CommonUtils commonUtils) {
		Timer timer = new Timer();
		ParkSchedule task = new ParkSchedule(commonDao,commonUtils);
		// 安排指定的任务在指定的时间开始进行重复的固定延迟执行。
		timer.schedule(task, new Date(), PERIOD_DAY);
		logger.info("启动下行消息发送任务..每分钟发送一次>>>>>>>>>>>>>>>>>>>");

		//回收未使用且过期的减免劵额度
		logger.error("开始每天凌晨两点的回收减免卷额度功能.......");
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 2); //凌晨2点
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		Date date=calendar.getTime(); //第一次执行定时任务的时间
		//如果第一次执行定时任务的时间 小于当前的时间
		//此时要在 第一次执行定时任务的时间加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。
		if (date.before(new Date())) {
			date = this.addDay(date, 1);
		}
		//安排指定的任务在指定的时间开始进行重复的固定延迟执行。
		TicketSchedule ticket = new TicketSchedule(commonDao);
		timer.schedule(ticket,date,TICKET_PERIOD_DAY);
		//timer.schedule(ticket, 5*1000L,1*60*1000);
	}
	// 增加或减少天数
	public Date addDay(Date date, int num) {
		Calendar startDT = Calendar.getInstance();
		startDT.setTime(date);
		startDT.add(Calendar.DAY_OF_MONTH, num);
		return startDT.getTime();
	}
}
