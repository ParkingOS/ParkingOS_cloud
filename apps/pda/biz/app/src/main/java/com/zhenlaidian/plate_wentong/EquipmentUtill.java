/**
 * 
 */
package com.zhenlaidian.plate_wentong;

import android.os.Build;

/**   
 *    
 * 项目名称：plate_id_sample_service  
 * 类名称：EquipmentUtill  
 * 类描述：   本类用于获取设备的型号等参数,目前针对荣耀七无法打开相机的问题
 * 创建人：张志朋  
 * 创建时间：2015-11-2 下午3:53:45  
 * 修改人：张志朋  
 * 修改时间：2015-11-2 下午3:53:45  
 * 修改备注：   无
 * @version    
 *    
 */
public class EquipmentUtill {
	public String device_mode=Build.MODEL;
	public boolean isPLKTL01H = false;
	/**
	 * 
	* @Title: CheckPLKTL01H 
	* @Description: TODO(这里用一句话描述这个方法的作用) 检车该设备型号是否为荣耀7
	* @param @return    设定文件 
	* @return boolean    返回类型  是  true  不是  false
	* @throws
	 */
	public boolean CheckPLKTL01H(){
		
		if("PLK-TL01H".equals(device_mode)){
			isPLKTL01H = true;
		}
		return isPLKTL01H;
	}
}