package parkingos.com.bolink.utlis;

import java.util.Collection;
import java.util.Map;

/**
 * 做检查的util,判断是否,或有无
 * @time 2017-8-18
 * @author QuanHao
 */
public class CheckUtil {

	/**
	 * 判断集合是否有元素
	 * @return
	 * @time 2017年 2017-8-19 下午3:06:44
	 * @author QuanHao
	 */
	public static boolean hasElement(Collection<?> collection){
		if(collection!=null&&collection.size()>0){
			return true;
		}
		return false;
	}
	
	/**
	 * 判断集合是否有元素
	 * @return
	 * @time 2017年 2017-8-19 下午3:06:44
	 * @author QuanHao
	 */
	public static boolean hasElement(Map<?,?> map){
		if(map!=null&&map.size()>0){
			return true;
		}
		return false;
	}
	
	/**
	 * 判断字符串是否为空
	 * @param value
	 * @return
	 * @time 2017年 2017-8-19 下午3:09:28
	 * @author QuanHao
	 */
	public static boolean isNotNull(String value) {
		if (value == null || value.equals(""))
			return false;
		return true;
	}

	public static String createSign(Map<String, Object> paramMap, String unionKey){
		String linkParams = StringUtils.createLinkString(paramMap);
		String sign =StringUtils.MD5(linkParams+"key="+unionKey,"utf-8").toUpperCase();
		paramMap.put("sign", sign);
		return sign;
	}

}
