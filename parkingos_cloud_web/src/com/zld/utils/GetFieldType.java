package com.zld.utils;

public class GetFieldType {
	
	public static Integer getFieldType(String table ,String field){
		
		if(table.equals("com_info")){
			return getComInfoFieldType(field);
		}else if(table.equals("user_info")){
			return getUserInfoFieldType(field);
		}else if(table.equals("product_package_tb")){
			return getPackageFieldType(field);
		}else if(table.equals("order_tb")){
			return getOrderFieldType(field);
		}else if(table.equals("price_tb")){
			return getPriceFieldType(field);
		}else if(table.equals("money_record")){
			return getMoneyRecordFieldType(field);
		}else if(table.equals("recommend_tb")){
			return getrecomTbFieldType(field);
		}else if(table.equals("nfc_tb")){
			return getNfcTbFieldType(field);
		}else if(table.equals("withdraw")){
			return getWithDrawFieldType(field);
		}else if(table.equals("mobile_tb")){
			return getMobilieFieldType(field);
		}else if(table.equals("c_product")){
			return getCarOwerProduetFieldType(field);
		}else if(table.equals("park_account")){
			return getParkAccountFieldType(field);
		}else if(table.equals("com_praise_tb")){
			return getParkAccountFieldType(field);
		}else if(table.equals("recommend_tb")){
			return getrecommendTbFieldType(field);
		}else if(table.equals("ticket_tb")){
			return getticketTbFieldType(field);
		}else if(table.equals("black_tb")){
			return getBlackFieldType(field);
		}else if(table.equals("car_stops")){
			return getCarStopsFieldType(field);
		}else if(table.equals("carstops_price")){
			return getCarStopsPriceFieldType(field);
		}else if(table.equals("carstops_order")){
			return getCarStopsOrderFieldType(field);
		}else if(table.equals("ibeacon_tb")){
			return getCarIbeaconFieldType(field);
		}else if(table.equals("park_ticket")){
			return getParkTicketFieldType(field);
		}else if(table.equals("qr_code")){
			return getQRFieldType(field);
		}else if(table.equals("tcb_account")){
			return getTcbAccountFieldType(field);
		}else if(table.equals("park_account")){
			return getParkAccountFieldType(field);
		}else if(table.equals("user_account")){
			return getUserAccountFieldType(field);
		}else if(table.equals("puser_account")){
			return getPuserAccountFieldType(field);
		}else if(table.equals("com_park")){
			return getComParkFieldType(field);
		}else if(table.equals("lift_rod")){
			return getLiftRodFieldType(field);
		}else if(table.equals("com_berthsecs_tb")){
			return getBerthSegFieldType(field);
		}else if(table.equals("com_park_tb")){
			return getBerthFieldType(field);
		}else if(table.equals("com_camera_tb")){
			return getCameraFieldType(field);
		}else if(table.equals("com_brake_tb")){
			return getBrakeFieldType(field);
		}else if(table.equals("vip_tb")){
			return getVIPFieldType(field);
		}else if(table.equals("com_pos_tb")){
			return getPosFieldType(field);
		}else if(table.equals("com_alert_tb")){
			return getAlertFieldType(field);
		}else if(table.equals("city_hotarea_tb")){
			return getHotAreaFieldType(field);
		}else if(table.equals("sites_tb")){
			return  gettransmitterFieldType(field);
		}else if(table.equals("dici_tb")){
			return  getsensorFieldType(field);
		}else if(table.equals("user_info_tb")){
			return  getplanmemberFieldType(field);
		}else if(table.equals("no_payment_tb")){
			return  getparkescapeFieldType(field);
		}else if(table.equals("order_reserve_tb")){
			return  getreserveorderFieldType(field);
		}else if(table.equals("induce_tb")){
			return getInduceFieldType(field);
		}else if(table.equals("city_account_tb")){
			return getCityAccFieldType(field);
		}else if(table.equals("group_account_tb")){
			return getGroupAccFieldType(field);
		}else if(table.equals("money_set_tb")){
			return getMonSetFieldType(field);
		}else if(table.equals("com_account_tb")){
			return getBankFieldType(field);
		}else if(table.equals("city_video_tb")){
			return getVideoFieldType(field);
		}else if(table.equals("local_info_tb")){
			return getLocalInfoFieldType(field);
		}else if(table.equals("city_peakalert_tb")){
			return getPeakFieldType(field);
		}else if(table.equals("city_peakalert_tb")){
			return getCityPeakAlertFieldType(field);
		}else if(table.equals("city_bike_tb")){
			return getCityBikeType(field);
		}else if(table.equals("berth_order_tb")){
			return getBerthOrderType(field);
		}else if(table.equals("car_number_type_tb")){
			return getNumberTypeType(field);
		}else if(table.equals("remain_berth_tb")){
			return getRemainBerthFieldType (field);
		}else if(table.equals("inspect_event_tb")){
			return getInspectEventFieldType (field);
		}else if(table.equals("card_account_tb")){
			return getCardAccountFieldType(field);
		}else if(table.equals("device_fault_tb")){
			return getDeviceFaultFieldType(field);
		}else if(table.equals("parkuser_work_record_tb")){
			return getParkUserWrokRecordType(field);
		}else if(table.equals("parkuser_cash_tb")){
			return getParkUserCash(field);
		}else if(table.equals("parkuser_account_tb")){
			return getParkUserAccount(field);
		}else if(table.equals("park_account_tb")){
			return getParkAccount(field);
		}else if(table.equals("group_account_tb")){
			return getGroupAccount(field);
		}else if(table.equals("city_account_tb")){
			return getTenantAccount(field);
		}else if(table.equals("sync_info_pool_tb")){
			return getSyncServer(field);
		}else if(table.equals("org_group_tb")){
			return org_group_tb(field);
		}else if(table.equals("card_renew")){
			return cardRenew(field);
		}else if(table.equals("monitor_info_tb")){
			return getMonitorInfoFieldType(field);
		}else if(table.equals("phone_info_tb")){
			return getPhoneInfoFieldType(field);
		}
		return 12;
	}
	private static Integer getMonitorInfoFieldType(String field) {
		if(field.equals("channel_id")
				|| field.equals("channel_name")
				|| field.equals("play_src")
				|| field.equals("comid")){
			return 12;
		}else if(field.equals("net_status")
				|| field.equals("is_show")
				|| field.equals("show_order")){
			return 3;
		}else
			return 4;
	}
	private static Integer cardRenew(String field) {
		if(field.equals("id")
				|| field.equals("buy_month")){
			return 4;
		}else if(field.equals("create_time")||
				field.equals("update_time")
				|| field.equals("pay_time")
				|| field.equals("limit_time")){
			return 93;
		}else
			return 12;
	}

	private static Integer org_group_tb(String field) {
		if(field.equals("name")
				|| field.equals("address")){
			return 12;
		}else if(field.equals("balance")
				|| field.equals("longitude")
				|| field.equals("latitude")){
			return 3;
		}else
			return 4;
	}
	
	private static Integer getSyncServer(String field) {
		if(field.equals("table_name")){
			return 12;
		}else
			return 4;
	}
	
	private static Integer getTenantAccount(String field) {
		if(field.equals("create_time")){
			return 93;
		}else if(field.equals("amount")){
			return 3;
		}else if(field.equals("remark")){
			return 12;
		}else
			return 4;
	}
	
	private static Integer getGroupAccount(String field) {
		if(field.equals("create_time")){
			return 93;
		}else if(field.equals("amount")){
			return 3;
		}else if(field.equals("remark")){
			return 12;
		}else
			return 4;
	}
	
	private static Integer getParkAccount(String field) {
		if(field.equals("create_time")){
			return 93;
		}else if(field.equals("amount")){
			return 3;
		}else if(field.equals("remark")){
			return 12;
		}else
			return 4;
	}
	
	private static Integer getParkUserAccount(String field) {
		if(field.equals("create_time")){
			return 93;
		}else if(field.equals("amount")){
			return 3;
		}else if(field.equals("remark")){
			return 12;
		}else
			return 4;
	}
	
	private static Integer getParkUserCash(String field) {
		if(field.equals("create_time")){
			return 93;
		}else if(field.equals("amount")){
			return 3;
		}else
			return 4;
	}
	
	private static Integer getParkUserWrokRecordType(String field) {
		if(field.equals("device_code")
				||field.equals("uuid")
				||field.equals("out_log")){
			return 12;
		}else if(field.equals("start_time")
				|| field.equals("end_time")){
			return 93;
		}else if(field.equals("history_money")){
			return 3;
		}
		else
			return 4;
	}
	private static Integer getDeviceFaultFieldType(String field) {
		if(field.equals("id")
				||field.equals("sensor_id")
				||field.equals("site_id")
				||field.equals("induce_id")){
			return 4;
		}else if(field.equals("create_time")
				|| field.equals("end_time")){
			return 93;
		}else
			return 12;
	}
	
	private static Integer getCardAccountFieldType(String field) {
		if(field.equals("create_time")){
			return 93;
		}else if(field.equals("amount")){
			return 3;
		}else if(field.equals("remark")){
			return 12;
		} else
			return 4;
	}

	private static Integer getInspectEventFieldType(String field) {
		if(field.equals("id")
				||field.equals("type")
				||field.equals("berthsec_id")
				||field.equals("uid")
				||field.equals("inspectid")
				||field.equals("state")){
			return 4;
		}else if(field.equals("create_time")
				|| field.equals("end_time")){
			return 93;
		}else
			return 12;
	}

	private static Integer getPeakFieldType(String field) {
		if(field.equals("id")
				||field.equals("state")
				||field.equals("cityid")
				||field.equals("comid")
				||field.equals("hotarea_id")){
			return 4;
		}else if(field.equals("create_time") 
				|| field.equals("handle_time")){
			return 93;
		}else 
			return 12;
	}

	private static Integer getNumberTypeType(String field) {
		if(field.equals("id")
				||field.equals("comid")
				||field.equals("cityid")
				||field.equals("typeid")){
			return 4;
		}else if(field.equals("update_time")
				){
			return 93;
		}else
			return 12;
	}
	
	private static Integer getBerthOrderType(String field) {
		if(field.equals("id")
				||field.equals("state")
				||field.equals("orderid")
				||field.equals("in_uid")
				||field.equals("out_uid")
				||field.equals("comid")
				||field.equals("dici_id")){
			return 4;
		}else if(field.equals("in_time") 
				|| field.equals("out_time")){
			return 93;
		}else if(field.equals("total")
				||field.equals("order_total")){
			return 3;
		}else 
			return 12;
	}
	
	private static Integer getVideoFieldType(String field) {
		if(field.equals("id")
				||field.equals("state")
				||field.equals("cityid")
				||field.equals("comid")
				||field.equals("type")){
			return 4;
		}else if(field.equals("create_time") 
				|| field.equals("update_time")){
			return 93;
		}else if(field.equals("latitude")
				||field.equals("longitude")){
			return 3;
		}else 
			return 12;
	}
	
	private static Integer getCityPeakAlertFieldType(String field) {
		if(field.equals("title ")||field.equals("content")){
			return 12;
		}
		return 4;
	}

	private static Integer getBankFieldType(String field) {
		if(field.equals("id")
				||field.equals("comid")
				||field.equals("uin")
				||field.equals("atype")
				||field.equals("type")
				||field.equals("state")
				||field.equals("groupid")
				||field.equals("cityid")){
			return 4;
		}else 
			return 12;
	}
	
	private static Integer getMonSetFieldType(String field) {
		if(field.equals("id")
				||field.equals("comid")
				||field.equals("uin")
				||field.equals("mtype")
				||field.equals("giveto")){
			return 4;
		}else 
			return 12;
	}
	
	private static Integer getGroupAccFieldType(String field) {
		if(field.equals("type")
				||field.equals("comid")
				||field.equals("groupid")
				||field.equals("source")
				||field.equals("uid")
				||field.equals("orderid")
				||field.equals("withdraw_id")
				||field.equals("id")){
			return 4;
		}else if(field.equals("create_time")){
			return 93;
		}else if(field.equals("amount")){
			return 3;
		}else 
			return 12;
	}
	
	private static Integer getCityAccFieldType(String field) {
		if(field.equals("type")
				||field.equals("cityid")
				||field.equals("source")
				||field.equals("withdraw_id")
				||field.equals("orderid")
				||field.equals("uid")
				||field.equals("comid")
				||field.equals("id")){
			return 4;
		}else if(field.equals("create_time")){
			return 93;
		}else if(field.equals("amount")){
			return 3;
		}else 
			return 12;
	}
	
	private static Integer getInduceFieldType(String field) {
		if(field.equals("type")
				||field.equals("cityid")
				||field.equals("state")
				||field.equals("creator_id")
				||field.equals("updator_id")
				||field.equals("deletor_id")
				||field.equals("id")
				||field.equals("is_delete")){
			return 4;
		}else if(field.equals("create_time")
				||field.equals("update_time")
				||field.equals("heartbeat_time")){
			return 93;
		}else if(field.equals("longitude")
				||field.equals("latitude")){
			return 3;
		}else 
			return 12;
	}
	private static Integer getInduceFlagFieldType(String field) {
		if(field.equals("type")
				||field.equals("cityid")
				||field.equals("state")
				||field.equals("creator_id")
				||field.equals("updator_id")
				||field.equals("deletor_id")
				||field.equals("id")){
			return 4;
		}else if(field.equals("create_time")
				||field.equals("update_time")){
			return 93;
		}else if(field.equals("longitude")
				||field.equals("latitude")){
			return 3;
		}else 
			return 12;
	}
	
	private static Integer getHotAreaFieldType(String field) {
		if(field.equals("name")||field.equals("adress")||field.equals("reason")){
			return 12;
		}else 
			return 4;
	}

	private static int getVIPFieldType(String field){
		if(field.equals("uin")
				||field.equals("bcount")
				||field.equals("comid")
				||field.equals("id")){
			return 4;
		}else if(field.equals("create_time")
				||field.equals("e_time")){
			return 93;
		}else if(field.equals("acttotal")
				||field.equals("atotal")){
			return 3;
		}else 
			return 12;
	}
	
	private static Integer getAlertFieldType(String field) {
		if(field.equals("source")||field.equals("content")){
			return 12;
		}else 
			return 4;
	}

	private static Integer getPosFieldType(String field) {
		if(field.equals("pda")){
			return 12;
		}else 
			return 4;
	}

	private static int getBrakeFieldType(String field){
		if(field.equals("id")
				||field.equals("comid")
				||field.equals("state")
				||field.equals("passid")){
			return 4;
		}else if(field.equals("upload_time")){
			return 93;
		}else 
			return 12;
	}
	private static int getreserveorderFieldType(String field){
		if(field.equals("id")
				||field.equals("uin")
				||field.equals("order_id")||field.equals("state")||field.equals("pay_type")||field.equals("type")){
			return 4;
		}else if(field.indexOf("time")!=-1){
			return 93;
		}
		else if(field.equals("prepaid")){
			return 3;
		}else 
			return 12;
	}
	private static int getCityBikeType(String field){
		if(field.equals("id")){
			return 4;
		}else  if(field.equals("plot_count")||field.equals("surplus")){
			return 3;
		}
		else 
			return 12;
	}
	private static int getparkescapeFieldType(String field){
		if(field.equals("car_number")){
			return 12;
		}else if(field.equals("end_time") 
				|| field.equals("create_time") 
				|| field.equals("pursue_time")){
			return 93;
		}else if(field.equals("total")
				|| field.equals("act_total")
				|| field.equals("prepay")){
			return 3;
		}else 
			return 4;
	}
	private static int getplanmemberFieldType(String field){
		if(field.equals("id")
				||field.equals("sex")
				||field.equals("state")
				||field.equals("passid")){
			return 4;
		}
		else 
			return 12;
	}
	private static int gettransmitterFieldType(String field){
		if(field.equals("id")
				||field.equals("comid")
				||field.equals("voltage")||field.equals("longitude")||field.equals("latitude")||field.equals("state")){
			return 4;
		}else if(field.equals("update_time")||field.equals("create_time")||field.equals("heartbeat")){
			return 93;
		}else 
			return 12;
	}
	private static int getsensorFieldType(String field){
		if(field.equals("id")
				||field.equals("comid")||field.equals("state")||field.equals("cp_id")){
			return 4;
		}else if(field.equals("operate_time")||field.equals("beart_time")){
			return 93;
		}else if(field.equals("battery")||field.equals("magnetism")){
			return 3;
		}else 
			return 12;
	}
	
	
	private static int getCameraFieldType(String field){
		if(field.equals("id")
				||field.equals("comid")
				||field.equals("state")
				||field.equals("passid")){
			return 4;
		}else if(field.equals("upload_time")){
			return 93;
		}else 
			return 12;
	}
	
	private static int getBerthFieldType(String field){
		if(field.equals("id")
				||field.equals("comid")
				||field.equals("state")
				||field.equals("berth_id")
				||field.equals("berthsec_id")){
			return 4;
		}else if(field.equals("longitude")||field.equals("latitude")){
			return 3;
		}else if(field.equals("create_time")||field.equals("enter_time")||field.equals("end_time")){
			return 93;
		}else 
			return 12;
	}
	private static int getRemainBerthFieldType(String field){
		if(field.equals("id")
				||field.equals("comid")
				||field.equals("berthseg_id")){
			return 4;
		}else if(field.equals("amount")||field.equals("total")){
			return 3;
		}else if(field.equals("update_time")){
			return 93;
		}else 
			return 12;
	}
	
	private static int getBerthSegFieldType(String field){
		if(field.equals("id")
				||field.equals("comid")
				||field.equals("is_active")){
			return 4;
		}else if(field.equals("longitude")||field.equals("latitude")){
			return 3;
		}else if(field.equals("create_time")){
			return 93;
		}else 
			return 12;
	}
	
	private static Integer getLiftRodFieldType(String field) {
		if(field.equals("id")||field.equals("uin")||field.equals("comid"))
			return 4;
		else if(field.equals("ctime")||field.equals("update_time"))
			return 93;
		return 12;
	}

	private static Integer getComParkFieldType(String field) {
		if(field.equals("cid"))
			return 12;
		return 4;
	}

	private static Integer getPuserAccountFieldType(String field) {
		if(field.equals("remark"))
			return 12;
		else if(field.equals("amount"))
			return 3;
		return 4;
	}

	private static Integer getUserAccountFieldType(String field) {
		if(field.equals("remark"))
			return 12;
		else if(field.equals("amount"))
			return 3;
		return 4;
	}

	private static Integer getTcbAccountFieldType(String field) {
		if(field.equals("remark")
			||field.equals("online_orderid"))
			return 12;
		else if(field.equals("create_time")){
			return 93;
		}else if(field.equals("amount")){
			return 3;
		}
		return 4;
	}

	private static Integer getQRFieldType(String field) {
		return 4;
	}

	private static Integer getParkTicketFieldType(String field) {
		if(field.equals("money"))
			return 3;
		else
			return 4;
	}

	private static Integer getCarIbeaconFieldType(String field) {
		if(field.equals("lng")||field.equals("lat"))
			return 3;
		else if(field.equals("ibcid"))
			return 12;
		else
			return 4;
	}

	private static Integer getCarStopsOrderFieldType(String field) {
		if(field.equals("amount"))
			return 3;
		else if(field.equals("car_number")||field.equals("pic")||field.equals("keyno")||field.equals("car_local"))
			return 12;
		else
			return 4;
	}

	private static Integer getCarStopsPriceFieldType(String field) {
		if(field.equals("first_price")||field.equals("next_price")
				||field.equals("top_price")
				||field.equals("fav_price"))
			return 3;
		else if(field.equals("creator")||field.equals("resume"))
			return 12;
		else
			return 4;
	}

	private static Integer getCarStopsFieldType(String field) {
		if(field.equals("lng")||field.equals("lat")||field.equals("start_price")
				||field.equals("next_price")||field.equals("max_price"))
			return 3;
		else if(field.equals("name")||field.equals("resume")||field.equals("pic"))
			return 12;
		else
			return 4;
	}

	private static Integer getBlackFieldType(String field) {
		if(field.equals("comid")
			||field.equals("uin")
			||field.equals("id")
			||field.equals("state")
			||field.equals("car_type")){
			return 4;
		} else if(field.equals("ctime")
				||field.equals("utime")){
			return 93;
		}else {
			return 12;
		}
	}

	private static Integer getParkPraiseFieldType(String field) {
		return 4;
	}
	
	private static Integer getParkAccountFieldType(String field) {
		if(field.indexOf("time")!=-1)
			return 93;
		else if(field.equals("amount"))
			return 3;
		else if(field.equals("remark"))
			return 12;
		else
			return 4;
	}

	private static Integer getCarOwerProduetFieldType(String field) {
		if(field.indexOf("time")!=-1)
			return 93;
		else if(field.equals("total"))
			return 3;
		else if(field.equals("name")||field.equals("address")||field.equals("p_lot")||field.equals("car_number"))
			return 12;
		else 
			return 4;
	}

	private static Integer getMobilieFieldType(String field) {
		if(field.indexOf("time")!=-1||field.indexOf("date")!=-1)
			return 93;
		else if(field.equals("money_3")||field.equals("price"))
			return 3;
		else if(field.equals("imei")||field.equals("num")||field.equals("divice_code")
				||field.equals("mode")||field.equals("editor"))
			return 12;
		return 4;
	}

	private static Integer getWithDrawFieldType(String field) {
		if(field.indexOf("time")!=-1)
			return 93;
		else if(field.equals("amount"))
			return 3;
		return 4;
	}

	private static Integer getNfcTbFieldType(String field) {
		if(field.equals("nfc_uuid")
			||field.equals("qrcode")
			||field.equals("card_name")
			||field.equals("device")
			||field.equals("card_number")){
			return 12;
		}
		if(field.equals("balance")){
			return 3;
		}else if(field.equals("create_time")
				|| field.equals("update_time")
				|| field.equals("cancel_time")
				|| field.equals("activate_time")){
			return 93;
		}
		return 4;
	}

	private static Integer getrecomTbFieldType(String field) {
			if(field.equals("openid")){
				return 12;
			}else{
				return 4;
			}
	}
	
	private static Integer getrecommendTbFieldType(String field) {
		if(field.equals("create_time")){
			return 93;
		}else 
			return 4;
	}
	
	private static Integer getticketTbFieldType(String field){
		if(field.equals("limit_day")){
			return 93;
		}else if(field.equals("money")){
			return 3;
		}else{
			return 4;
		}
	}

	private static Integer getMoneyRecordFieldType(String field) {
		if(field.equals("amount")){
			return 3;
		}else if(field.equals("create_time")){
			return 93;
		}else if(field.equals("remark") || field.equals("car_number") || field.equals("mobile"))
			return 12;
		else 
			return 4;
	}

	private static Integer getPriceFieldType(String field) {
		if(field.equals("price")){
			return 3;
		}else if(field.equals("create_time")){
			return 93;
		}else 
			return 4;
	}

	private static Integer getPackageFieldType(String field) {
		if(field.equals("valid_time")||field.equals("p_name")){
			return 12;
		}else if(field.equals("limitday")){
			return 93;
		}else if(field.equals("price")||field.equals("old_price")){
			return 3;
		}else 
			return 4;
	}

	private static Integer getOrderFieldType(String field) {
		if(field.equals("end_time")||field.equals("create_time")){
			return 93;
		}else if(field.equals("total")||field.equals("amount")){
			return 3;
		}else if(field.equals("car_number")||field.equals("c_type")||
				field.equals("in_passid")||field.equals("out_passid")||field.equals("order_id_local")){
			return 12;
		}else 
			return 4;
	}

	private static int getComInfoFieldType(String field){
		if(field.equals("id")
				||field.equals("parking_type")
				||field.equals("parking_total")
				||field.equals("share_number")
				||field.equals("auto_order")
				||field.equals("type")
				||field.equals("state")
				||field.equals("city")
				||field.equals("uid")
				||field.equals("biz_id")
				||field.equals("nfc")
				||field.equals("etc")
				||field.equals("book")
				||field.equals("navi")
				||field.equals("monthlypay")
				||field.equals("stop_type")
				||field.equals("epay")
				||field.equals("isnight")
				||field.equals("isfixed")
				||field.equals("is_hasparker")
				||field.equals("activity")
				||field.equals("chanid")
				||field.equals("groupid")
				||field.equals("areaid")
				||field.equals("cityid")
				||field.equals("union_state")){
			return 4;
		}else if(field.equals("longitude")||field.equals("latitude")
				||field.equals("total_money")||field.equals("money")){
			return 3;
		}else if(field.equals("create_time")
				||field.equals("update_time")
				||field.equals("upload_union_time")){
			return 93;
		}else 
			return 12;
	}
	
	private static int getUserInfoFieldType(String field){
		if(field.equals("id")
				||field.equals("sex")
				||field.equals("online_flag")
				||field.equals("comid")
				||field.equals("state")
				||field.equals("auth_flag")
				||field.equals("isview")
				||field.equals("client_type")
				||field.equals("media")
				||field.equals("collector_auditor")
				||field.equals("role_id")){
			return 4;
		}else if(field.equals("balance")||field.equals("firstorderquota")||field.equals("rewardquota")||field.equals("recommendquota")){
			return 3;
		}else if(field.equals("reg_time")||field.equals("logon_time")
				||field.equals("logoff_time")){
			return 93;
		}else 
			return 12;
	}
	
	private static int getLocalInfoFieldType(String field){
		 
		if(field.equals("id")
				||field.equals("comid")
				||field.equals("version")
				||field.equals("is_update")
				){
			return 4;
		}else if(field.equals("create_time")||field.equals("limit_time")
				){
			return 93;
		}else 
			return 12;
	}
	private static Integer getPhoneInfoFieldType(String field) {
		if(field.equals("name")
				|| field.equals("comid")
				|| field.equals("groupid")){
			return 12;
		}else if(field.equals("tele_phone")
				|| field.equals("park_phone")
				|| field.equals("group_phone")
				|| field.equals("monitor_id")){
			return 3;
		}else
			return 4;
	}

}
