package parkingos.com.bolink.utils;

import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeTools {

	private static Logger logger = Logger.getLogger(TimeTools.class);

	private static GregorianCalendar gCalendar = null;

	private static SimpleDateFormat dateFormat = null;

	private static String dtime = "";

	public static Date getDateFromString(String str, String pattern) throws ParseException {
		return new SimpleDateFormat(pattern).parse(str);
	}

	/**
	 * @param milliSeconds
	 *            毫秒数
	 * @return 格式化后的时间字符串 yyyy-MM-dd
	 */
	public static String getTimeStr_yyyy_MM_dd(Long milliSeconds) {

		return secondsToDateStr(milliSeconds, "yyyy-MM-dd");
	}

	public static String getTimeYYYYMMDDHHMMSS()
	{
		String s = gettime();
		s = s.replaceAll("-", "").replaceAll(":", "").replaceAll(" ","").trim();
		return s;
	}
	/**
	 * @param milliSeconds
	 *            毫秒数
	 * @return 格式化后的时间字符串 yyyy-MM-dd HH:mm:ss
	 */
	public static String getTime_yyyyMMdd_HHmmss(Long milliSeconds) {

		return secondsToDateStr(milliSeconds, "yyyy-MM-dd HH:mm:ss");
	}

	// 时间格式数组
	private static String[] formatArray = { "yyyy-MM-dd", "yyyy-MM-dd HH:mm",
			"yyyy-MM-dd HH:mm:ss", "yy-MM-dd HH:mm", "yyyyMMdd HH:mm", "yyyy-MM-dd HH" };

	// 检测一个时间格式是否为合法格式
	private static boolean isRightFormat(String formatStr) {
		boolean isRight = false;
		int j = formatArray.length;
		for (int i = 0; i < j; i++) {
			if (formatArray[i].equalsIgnoreCase(formatStr)) {
				isRight = true;
				break;
			}
		}
		return isRight;
	}
	/**
	 * @todo 将数值时间格式化为字符串
	 * @param milliSeconds
	 * @param formatStr
	 * @return
	 */

	public static String secondsToDateStr(Long milliSeconds, String formatStr) {

		if (milliSeconds == null)
			return "";
		if (isRightFormat(formatStr) == false) {
			formatStr = "yyyy-MM-dd HH:mm:ss";
		}
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(formatStr);

			if (milliSeconds.longValue() > 1) {
				GregorianCalendar gCalendar = new GregorianCalendar();
				gCalendar.setTimeInMillis(milliSeconds.longValue());
				return dateFormat.format(gCalendar.getTime());
			} else {
				return "";
			}
		} catch (Exception e) {
			return "";
		}

	}

	public static String gettime()// 获得精确到秒的当前日期 用于oracle
	{
		try {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			gCalendar = new GregorianCalendar();
			dtime = dateFormat.format(gCalendar.getTime());
		} catch (Exception ex) {

		}
		return dtime;
	}

	/**
	 * @param strDate
	 * @return 根据字符串时间得到相应毫秒数
	 */
	public static Long getLongMilliSecondFrom_HHMMDD(String strDate) {
		return getLongMilliSecondFromStrDate(strDate, "yyyy-MM-dd");
	}
	/**
	 * @param strDate
	 * @return 根据字符串时间得到相应秒数
	 */
	public static Long getLongMilliSecondFrom_HHMMDDHHmmss(String strDate) {
		return getLongMilliSecondFromStrDate(strDate, "yyyy-MM-dd HH:mm:ss") / 1000;
	}

	public static Long getLongMilliSecondFromStrDate(String strDate, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);

		long millSeconds = new GregorianCalendar().getTimeInMillis();
		try {
			millSeconds = sdf.parse(strDate).getTime();
		} catch (Exception e) {
			// logger.error("---------get seconds error:"+e.getMessage());
		}
		return new Long(millSeconds);
	}

	/**
	 * @return 当前日期的字符串 yyyy-MM-dd 格式
	 */
	public static String getDate_YY_MM_DD() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(new Date());

	}

	public static Long getToDayBeginTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String today = sdf.format(new Date());
		today = today.substring(0, 10) + " 00:00:00";
		return getStrDateToSecond(today);
	}

	public static Long getStrDateToSecond(String strDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long millSeconds = (new GregorianCalendar()).getTimeInMillis();
		try {
			millSeconds = sdf.parse(strDate).getTime();
		} catch (Exception e) {

		}
		return new Long(millSeconds / 1000);
	}

	/**
	 * @return 得到当前时间的毫秒数(Long型)
	 */
	public static Long getLongMilliSeconds() {
		long d = new Date().getTime();
		return new Long(d / 1000);
	}
}
