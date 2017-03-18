package com.zhenlaidian.util;

import java.util.List;

import android.content.Context;
import android.location.LocationManager;

public class GPSHandler {

	//判断手机是否支持GPS硬件设备；
	public static boolean hasGPSDevice(Context context){
		final LocationManager mgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		if ( mgr == null ) 
			return false;
		final List<String> providers = mgr.getAllProviders();
		if ( providers == null ) 
			return false;
		return providers.contains(LocationManager.GPS_PROVIDER);
	}
	
	//判断手机GPS是否打开；
	public static  boolean isOPen(final Context context) { 
		 
         LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); 
         // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快） 
         boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); 
         // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位） 
//	         boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER); 
         if (gps) { 
             return true; 
         } 
         return false; 
 
	}
}
