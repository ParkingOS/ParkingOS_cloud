package com.zld.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pay.AlipayUtil;

import com.zld.AjaxUtil;
import com.zld.CustomDefind;
import com.zld.TableField;
import com.zld.service.DataBaseService;

/**
 * 接口上传数据工具
 * @author Gecko
 *
 */
public class ZldUploadUtils {

	/**
	 * http提交参数转为Map
	 * 
	 * @param params
	 * @return Map<String, String>
	 */
	public static Map<String, String> stringToMap(String params) {
		Map<String, String> map = new HashMap<String, String>();
		if (params != null) {
			String param[] = params.split("&");
			for (int i = 0; i < param.length; i++) {
				String pString =  AjaxUtil.decodeUTF8(param[i]);
				String p[] = pString.split("=");
				if (p != null && p.length == 2) {
					String key = getFieldName(p[0]);//参数名转数据库字段名
					if(key!=null&&!"".equals(key)){
						if(key.equals("gps")&&p[1].indexOf(",")!=-1){//上传的经纬度是一个字段，用逗号隔开
							String vs []= p[1].split(",");
							if(vs.length==2){
								map.put("longitude",vs[0]);
								map.put("latitude", vs[1]);
							}
						}else {
							map.put(key.toLowerCase(), p[1]);
						}
					}
				}
			}
		}
		return map;
	}

	/**
	 * 验证签名
	 * @param paramsMap
	 * @return
	 */
	public static boolean validateSign(String params){
		Map<String, String> paramsMap = new HashMap<String, String>();
		if (params != null) {
			String param[] = params.split("&");
			for (int i = 0; i < param.length; i++) {
				String pString = param[i];
				String p[] = pString.split("=");
				if (p != null && p.length == 2) {
						paramsMap.put(p[0],p[1]);//AjaxUtil.decodeUTF8(p[1]));
					}
				}
			}
		String sign = paramsMap.get("sign");
		if(sign==null||"".equals(sign))
			return false;
		paramsMap.remove("sign");
		String channelId = paramsMap.get("chanid");
		String key =CustomDefind.getValue("RSA"+channelId);//"yangzhou_guomai_201602";
		String pamString = AlipayUtil.createLinkString(paramsMap);
		System.out.println(pamString);
		try {
			String newSign = StringUtils.MD5(pamString+"key="+key).toUpperCase();
			System.out.println("sign:"+sign+",mysign:"+newSign);
			if(sign.equals(newSign))
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

	public static void main(String[] args) {
		String pString = "parking_total=20&company_id=1009&parkingNo=35rf788&timeStamp=1457693090&city_merchants_id=321000&parkingName=asd&state=0&sign=30FE309B987AE1662524206DBB452447&address=%E6%B5%8B%E8%AF%95%E6%B5%8B%E8%AF%95&token=29f5bf9e4134492d5cd8d937e4870c9e&parkingType=1&operate=0&create_time=1457693090&gps=119.91753%2C93.1778040&chanid=1008";
		validateSign(pString);
	}
	
	/**
	 * 处理上传数据
	 * @param paramMap
	 * @param tableName 数据表名
	 * @param type 操作类型 0注册，1更新 ，2删除
	 * @return map {sql=string,params=object[],errmesg=string}
	 */
	public static Map<String, Object> getData(Map<String, String> paramMap,String tableName,int type ) {
		List<TableField> companyFields = getFields(tableName);
		String keyName ="uuid";
		if(tableName.equals("com_info_tb"))
			keyName = "park_uuid";
		if(tableName.equals("com_park_tb")){
			keyName = "cid";
			paramMap.put("create_time", System.currentTimeMillis()/1000+"");
		}
		String keyValue =null;
		Map<String, Object> preUpdateMap = new HashMap<String, Object>();
		String errmesg = "";
		String sql = "";
		List<Object> values = new ArrayList<Object>();
		if(type==0){//注册
			sql = "insert into "+tableName+" (";
		}else if(type==2){//更新
			sql = "update "+ tableName+ " set ";
		}else if(type==1){//删除
			sql ="delete from "+tableName+" where ";
		}
		//解析每个字段值并判断合法性
		for(TableField field: companyFields){
			String fieldName  = field.getName();
			String fieldvalue = paramMap.get(fieldName);
			int fieldType = field.getFieldType();
			int length = field.getFieldLength();
			boolean isInsetNull = field.isInsertNull();
			boolean isUpdateNull= field.isUpdateNull();
			Object value = getValues(fieldType, fieldvalue, length);
			if(fieldName.equals(keyName)&&value!=null)
				keyValue = value.toString();
			if(fieldType==12){//验证字符串长度
				if(value!=null){
					if(value.toString().getBytes().length>length){
						errmesg += fieldName+"="+value+",长度不合法,限制为"+length+",实际为"+value.toString().getBytes().length+";";
					}
				}
			}
			if(type==0){//注册
				if(isInsetNull){//允许为空
					if(value==null){
						continue;
					}else {
						sql +=fieldName+",";
						values.add(value);
					}
				}else {//不允许为空
					if(value==null){
						errmesg +=fieldName+"为空或不合法;";
					}else {
						sql +=fieldName+",";
						values.add(value);
					}
				}
			}else if(type==1){//更新
				if(isUpdateNull){
					if(value==null){
						continue;
					}else{
						sql +=fieldName+"=?,";
						values.add(value);
					}
				}else{//不允许为空
					if(value==null){
						errmesg +=fieldName+"为空或不合法;";
					}else {
						sql +=fieldName+"=?,";
						values.add(value);
					}
				}
			}
		}
		
		if(!values.isEmpty()){
			if(sql.endsWith(",")){
				sql = sql.substring(0,sql.length()-1);
			}
			if(type==0){
				sql +=") values(";
				for(int i=0;i<values.size();i++){
					sql +="?,";
				}
				if(sql.endsWith(","))
					sql =sql.substring(0,sql.length()-1);
				sql +=")";
			}else if(type==1){
				sql +=" where "+keyName+"=? ";
				values.add(keyValue);
			}
		}else {//字段都为空
			if(type==2){//删除
				if(keyValue!=null){
					sql +="  "+keyName+"=? ";
					values.add(keyValue);
				}else {
					errmesg +="删除失败,uuid不能为空";
				}
			}else {//注册或更新时，没有可用数据
				errmesg +="注册或更新失败,"+keyName+"没有可用的数据";
			}
		}
		
		preUpdateMap.put("errmesg",errmesg);
		if(errmesg.equals("")&&!values.isEmpty()){//数据正常解析
			preUpdateMap.put("sql", sql);
			Object[] params = new Object[values.size()];
			int index =0;
			for(Object v : values){
				params[index]=v;
				index++;
			}
			preUpdateMap.put("params", params);
		}
		return preUpdateMap;
	}
	
	/**
	 * 取上传参数值
	 * @param fieldType 字段类型 2枚举 3浮点 4整数 5长整数 12字符串
	 * @param value 
	 * @param length 浮点数的长度
	 * @return
	 */
	private static Object getValues(int fieldType,String value,int length){
		if(value==null)
			return null;
		if(fieldType==2){//枚举
			if(Check.isNumber(value)){
				Integer num = Integer.valueOf(value);
				return num;
			}
		}else if(fieldType==3){//浮点
			if(Check.isDouble(value)&&length>0){
				return formatDouble(value, length);
			}
		}else if(fieldType==4){//整数
			if(Check.isNumber(value)){
				return Integer.valueOf(value);
			}
		}else if(fieldType==5){//长整数
			if(Check.isLong(value)){
				return Long.valueOf(value);
			}
		}else if(fieldType==12){
			value = AjaxUtil.decodeUTF8(value);
			//过滤特殊字符
			value = value.replaceAll("\"", "").replaceAll("'", "").replaceAll(",", "，").replace("[", "【").replace("]", "】")
					.replace("{", "｛").replace("}", "｝").replaceAll(";", "；");
			return value;
		}
		return null;
	}
	/**
	 * 格式化浮点数
	 * @param value
	 * @param length 浮点数的精度
	 * @return
	 */
	public static Double formatDouble(Object value,int length){
		String len = "#.";
		for(int i=0;i<length;i++){
			len +="0";
		}
		if(Check.isDouble(value+"")){
			DecimalFormat df=new DecimalFormat(len); 
			String dv = df.format(Double.valueOf(value+""));
			if(Check.isDouble(dv))
				return Double.valueOf(dv);
		}
		return 0.0d;
	}
	/**
	 * 根据表名取表字段属性
	 * @param tablaName
	 * @return
	 */
	private static List<TableField> getFields(String tablaName){
		if(tablaName.equals("company_tb")){
			return companyFliedType();
		}else if(tablaName.equals("com_info_tb")){
			return comInfoTbFliedType();
		}else if(tablaName.equals("com_berthsecs_tb")){
			return comBerthsesTbFliedType();
		}else if(tablaName.equals("park_daypay_tb")){
			return parkDaypayTbFieldType();
		}else if(tablaName.equals("park_dayuse_tb")){
			return parkDayuseTbFieldType();
		}else if(tablaName.equals("com_hd_tb")){
			return comHdTbFieldType();
		}else if(tablaName.equals("dici_tb")){
			return diciTbFieldType();
		}else if(tablaName.equals("com_camera_tb")){
			return comCameraTbFieldType();
		}else if(tablaName.equals("com_led_tb")){
			return comLedTbFieldType();
		}else if(tablaName.equals("com_brake_tb")){
			return comBrakeTbFieldType();
		}else if(tablaName.equals("com_park_tb")){
			return comParkTbFliedType();
		}else if(tablaName.equals("user_info_tb")){
			return userInfoTbFliedType();
		}else if(tablaName.equals("com_etc_tb")){
			return comEtcTbFieldType();
		}else if(tablaName.equals("order_tb")){
			return orderTbFieldType();
		}else if(tablaName.equals("com_parkuser_check")){
			return comParkuserCheckFieldType();
		}else if(tablaName.equals("work_group_tb")){
			return workGroupTbFieldType();
		}else if(tablaName.equals("zld_black_tb")){
			return comBlackTbFieldType();
		}else if(tablaName.equals("com_parkstatus_tb")){
			return comParkStatusTbFieldType();
		}else if(tablaName.equals("sites_tb")){
			return sitesTbFieldType();
		}
		return null;
	}
	
	private static List<TableField> comParkStatusTbFieldType() {
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("comid", 5,0, false, true));
		tableFields.add(new TableField("total", 4,0, false, true));
		tableFields.add(new TableField("empty", 4,0, false, true));
		tableFields.add(new TableField("sheduled", 4,0, true, true));
		tableFields.add(new TableField("rscheduled", 4,0, true, true));
		tableFields.add(new TableField("rinternal", 4,0, true, true));
		tableFields.add(new TableField("internal", 4,0, true, true));
		tableFields.add(new TableField("ctime", 5,0, false, true));
		tableFields.add(new TableField("chanid", 5,0, false, true));
		tableFields.add(new TableField("cityid", 5,0, false, true));
		return tableFields;
	}

	//黑名单字段
	private static List<TableField> comBlackTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("uuid", 12, 50, false, false));
		tableFields.add(new TableField("ctime", 5,0, false, true));
		tableFields.add(new TableField("car_number", 12, 50, false, false));
		tableFields.add(new TableField("remark", 12, 50, true, false));
		tableFields.add(new TableField("car_type", 2,0, true, true));//按照下行接口中定义
		tableFields.add(new TableField("chanid", 5, 0, false, true));
		
		return tableFields;
	}
	
	//收费员上下岗字段
	private static List<TableField> comParkuserCheckFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("uuid", 12, 50, false, false));
		tableFields.add(new TableField("uid", 5,0, false, true));
		tableFields.add(new TableField("comid", 5, 0, false, true));
		tableFields.add(new TableField("company_id", 5, 0, false, true));
		tableFields.add(new TableField("check_in_time", 5, 0, true, true));
		tableFields.add(new TableField("check_out_time", 5, 0, true, true));
		tableFields.add(new TableField("device_code", 2,0, true, true));
		tableFields.add(new TableField("berthsec_id", 5, 0, true, true));
		tableFields.add(new TableField("chanid", 5, 0, false, true));

		return tableFields;
	}
	//工作组表字段
	private static List<TableField> workGroupTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("uuid", 12, 50, false, false));
		tableFields.add(new TableField("workgroup_name", 12, 10, false, true));
		tableFields.add(new TableField("company_id", 5, 0, false, true));
		tableFields.add(new TableField("create_time", 5,0, false, true));
		tableFields.add(new TableField("update_time", 5,0, false, true));
		tableFields.add(new TableField("is_active", 2,0, true, true));
		tableFields.add(new TableField("chanid", 5,0, false, true));
		
		return tableFields;
	}
	//订单字段
	private static List<TableField> orderTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		//添加或修改时的字段
		tableFields.add(new TableField("id", 5, 0, false, true));
		tableFields.add(new TableField("order_uuid", 12, 50, false, true));
		tableFields.add(new TableField("create_time", 5, 0, false, true));
		tableFields.add(new TableField("car_number", 12, 50, false, true));
		tableFields.add(new TableField("comid", 5, 0, false, true));
		//tableFields.add(new TableField("car_type", 2,0, false, true));
		//tableFields.add(new TableField("uid", 5,0, false, true));
		//tableFields.add(new TableField("out_uid", 5,0, false, true));
		//tableFields.add(new TableField("prepaid", 3, 2, false, true));
		//tableFields.add(new TableField("in_equipment", 12, 50, false, true));
		//tableFields.add(new TableField("car_intime", 5, 0, false, true));
		tableFields.add(new TableField("pay_type", 2,0, false, true));
		//tableFields.add(new TableField("prepaid_pay_time", 5, 0, false, true));
		//tableFields.add(new TableField("berthnumber", 12, 50, false, true));
		tableFields.add(new TableField("chanid", 5,0, false, true));
		//结算时要加的字段
		tableFields.add(new TableField("end_time", 5, 0, true, true));
		//tableFields.add(new TableField("out_paytime", 2,0, false, false));
		//tableFields.add(new TableField("out_equipmen", 5,0, true, false));
		tableFields.add(new TableField("total", 3, 2, true, true));
		tableFields.add(new TableField("state", 2, 0, false, true));
		tableFields.add(new TableField("c_type", 2, 0, false, true));
		//tableFields.add(new TableField("pay_total", 3, 2, true, false));
		tableFields.add(new TableField("uin", 5, 0, false, true));
		return tableFields;
	}
	//电子标签字段
	private static List<TableField> comEtcTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("uuid", 12, 50, false, false));
		tableFields.add(new TableField("car_number", 12, 10, false, true));
		tableFields.add(new TableField("card_id", 12, 50, false, true));
		tableFields.add(new TableField("name", 12, 50, true, true));
		tableFields.add(new TableField("mobile", 12,15, true, true));
		tableFields.add(new TableField("balance", 3, 2, true, true));
		return tableFields;
	}
	//收费员字段
	private static List<TableField> userInfoTbFliedType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("uuid", 12, 50, false, false));
		tableFields.add(new TableField("chanid", 4, 0, false, true));
		tableFields.add(new TableField("company_id", 4,0 , false, true));
		tableFields.add(new TableField("reg_time", 5, 0, false, true));
		tableFields.add(new TableField("mobile", 12, 15, false, true));
		tableFields.add(new TableField("sex", 2,0, true, true));
		tableFields.add(new TableField("nickname", 12, 50, true, true));
		tableFields.add(new TableField("email", 12, 50, true, true));
		tableFields.add(new TableField("md5pass", 12, 50, true, false));
		tableFields.add(new TableField("strid", 12, 50, false, false));
		return tableFields;
	}
	//公司表字段
	private static List<TableField> companyFliedType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("uuid", 12, 50, false, false));
		tableFields.add(new TableField("company_name", 12, 100, false, true));
		tableFields.add(new TableField("corporation", 12, 20, false, true));
		tableFields.add(new TableField("phone", 12, 50, false, true));
		tableFields.add(new TableField("address", 12, 100, false, true));
		tableFields.add(new TableField("chanid", 4, 0, false, true));
		tableFields.add(new TableField("city_merchants_id", 5, 0, false, true));
		tableFields.add(new TableField("update_time", 5, 0, true, false));
		tableFields.add(new TableField("lon", 3, 6, true, true));
		tableFields.add(new TableField("create_time", 5, 0, false, true));
		return tableFields;
	}
	//停车场表字段
	private static List<TableField> comInfoTbFliedType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("park_uuid", 12, 50, false, false));
		tableFields.add(new TableField("company_name", 12, 100, false, true));
		tableFields.add(new TableField("phone", 12, 50, true, true));
		tableFields.add(new TableField("fax", 12, 50, true, true));
		tableFields.add(new TableField("address", 12, 100, true, true));
		tableFields.add(new TableField("zipcode", 12, 10, true, true));
		tableFields.add(new TableField("homepage", 12, 50, true, true));
		tableFields.add(new TableField("remarks", 12, 1000, true, true));
		tableFields.add(new TableField("create_time", 5, 0, false, true));
		tableFields.add(new TableField("longitude", 3, 6, false, true));
		tableFields.add(new TableField("latitude", 3, 6, false, true));
		tableFields.add(new TableField("parking_type", 4, 0, false, true));
		tableFields.add(new TableField("parking_total", 4, 0, false, true));
		tableFields.add(new TableField("share_number", 4, 0, true, true));
		tableFields.add(new TableField("mobile", 12, 15, true, true));
		tableFields.add(new TableField("mcompany", 12, 100, true, true));
		tableFields.add(new TableField("type", 2, 0, true, true));
		tableFields.add(new TableField("stop_type", 2, 0, true, true));
		tableFields.add(new TableField("state", 2, 0, true, true));
		tableFields.add(new TableField("city", 4, 0, true, true));
		tableFields.add(new TableField("record_number", 12, 100, true, true));
		tableFields.add(new TableField("company_uuid", 12, 50, true, true));
		tableFields.add(new TableField("chanid", 4, 0, false, true));
		//tableFields.add(new TableField("city_merchants_id", 5, 0, false, true));
		tableFields.add(new TableField("update_time", 5, 0, true, false));
		return tableFields;
	}
	//泊位段表
	private static List<TableField> comBerthsesTbFliedType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("uuid", 12, 50, false, false));
		tableFields.add(new TableField("berthsec_name", 12, 100, false, true));
		tableFields.add(new TableField("park_uuid", 12, 20, false, true));
		tableFields.add(new TableField("create_time", 5, 0, false, true));
		tableFields.add(new TableField("is_active", 12, 50, false, true));
		tableFields.add(new TableField("address", 12, 100, false, true));
		tableFields.add(new TableField("longitude", 3, 6, false, true));
		tableFields.add(new TableField("latitude", 3, 6, false, true));
		return tableFields;
	}
	//车位表
	private static List<TableField> comParkTbFliedType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("comid", 5, 0, false, true));
		tableFields.add(new TableField("cid", 12, 50, true, true));
		tableFields.add(new TableField("create_time", 5, 0, false, true));
		return tableFields;
	}
	//停车场每日收费
	private static List<TableField> parkDaypayTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("comid", 5, 0, false, true));
		tableFields.add(new TableField("internalIC".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("internalLabel".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("cashPay".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("labelPay".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("cardPay".toLowerCase(),5, 0, false, true));
		tableFields.add(new TableField("onlinePay".toLowerCase(),5, 0, false, true));
		tableFields.add(new TableField("unpaid".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("hbTime".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("cityElecAcctPay".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("escArrsAmt".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("parkingFee".toLowerCase(),12, 30, false, true));
		tableFields.add(new TableField("chanid", 4, 0, false, true));
		return tableFields;
	}
	//停车场每日停车量
	private static List<TableField> parkDayuseTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("comid", 5, 0, false, true));
		tableFields.add(new TableField("count".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("hbTime".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("chanid", 4, 0, false, true));
		return tableFields;
	}
	
	//设备
	private static List<TableField> comHdTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("comid", 5, 0, false, true));
		tableFields.add(new TableField("no", 12, 50, false, false));
		tableFields.add(new TableField("type", 2, 0, false, true));
		tableFields.add(new TableField("operate_time", 5, 0, false, true));
		tableFields.add(new TableField("state", 4, 0, false, true));
		return tableFields;
	}
	//地磁
	private static List<TableField> diciTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("comid", 5, 0, false, true));
		tableFields.add(new TableField("operate_time", 5, 0, false, true));
		tableFields.add(new TableField("state", 4, 0, false, true));
		return tableFields;
	}
	
	//摄像头
	private static List<TableField> comCameraTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("passid", 5, 0, false, true));
		tableFields.add(new TableField("upload_time", 5, 0, false, true));
		tableFields.add(new TableField("state", 4, 0, false, true));
		tableFields.add(new TableField("comid", 5, 0, false, true));
		return tableFields;
	}
	
	//诱导屏
	private static List<TableField> comLedTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("passid", 5, 0, false, true));
		tableFields.add(new TableField("upload_time", 5, 0, false, true));
		tableFields.add(new TableField("state", 4, 0, false, true));
		tableFields.add(new TableField("comid", 5, 0, false, true));
		return tableFields;
	}
	
	//道闸
	private static List<TableField> comBrakeTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("passid", 5, 0, false, true));
		tableFields.add(new TableField("upload_time", 5, 0, false, true));
		tableFields.add(new TableField("state", 4, 0, false, true));
		tableFields.add(new TableField("comid", 5, 0, false, true));
		return tableFields;
	}
	//道闸
	private static List<TableField> sitesTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("uuid", 5, 0, false, true));
		tableFields.add(new TableField("comid", 5, 0, false, true));
		tableFields.add(new TableField("cityid", 5, 0, false, true));
		tableFields.add(new TableField("state", 4, 0, false, true));
		return tableFields;
	}
	//字段名称转换
	private static String getFieldName(String paramName){
		Map<String, String> pfMap = new HashMap<String, String>();
		pfMap.put("parkingNo", "park_uuid");
		pfMap.put("parkingName", "company_name");
		pfMap.put("parkingType", "parking_type");
		pfMap.put("berth", "cid");
		pfMap.put("plot_id", "cp_id");
		pfMap.put("operTime", "operate_time");
		pfMap.put("arriveTime", "create_time");
		pfMap.put("license", "car_number");
		pfMap.put("hbTime", "ctime");
		pfMap.put("city_merchants_id", "cityid");
		pfMap.put("status", "state");
		pfMap.put("out_paytime", "end_time");
		if(pfMap.get(paramName)!=null)
			return pfMap.get(paramName);
		return paramName;
	}
	
	/**
	 * 计算订单金额
	 * @param start
	 * @param end
	 * @param comId
	 * @param car_type 0：通用，1：小车，2：大车
	 * @return 订单金额_是否优惠
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public  String getPrice(Long start,Long end,Long comId,Integer car_type,DataBaseService daService){
//		String pid = CustomDefind.CUSTOMPARKIDS;
//		if(pid.equals(comId.toString())){//定制价格策略
//			return "待结算";
//		}
//		
		if(car_type == 0){//0:通用
			Long count = daService.getLong("select count(*) from com_info_tb where id=? and car_type=?", new Object[]{comId,1});
			if(count > 0){//区分大小车
				car_type = 1;//默认成小车计费策略
			}
		}
		Map priceMap1=null;
		Map priceMap2=null;
		List<Map> priceList=daService.getAll("select * from price_tb where comid=? " +
				"and state=? and pay_type=? and car_type=? order by id desc", new Object[]{comId, 0, 0, car_type});
		if(priceList==null||priceList.size()==0){
			//查按次策略
			priceList=daService.getAll("select * from price_tb where comid=? " +
					"and state=? and pay_type=? and car_type=? order by id desc", new Object[]{comId,0,1,car_type});
			if(priceList==null||priceList.size()==0){//没有任何策略
				return "0.0";
			}else {//有按次策略，返回N次的收费
				Map timeMap =priceList.get(0);
				Object ounit  = timeMap.get("unit");
				Double total = Double.valueOf(timeMap.get("price")+"");
				try {
					if(ounit!=null){
						Integer unit = Integer.valueOf(ounit.toString());
						if(unit>0){
							Long du = (end-start)/60;//时长秒
							int times = du.intValue()/unit;
							if(du%unit!=0)
								times +=1;
							total = times*total;
							
						}
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				return StringUtils.formatDouble(total)+"";
			}
		}else {
			priceMap1=priceList.get(0);
			boolean pm1 = false;//找到map1,必须是结束时间大于开始时间
			boolean pm2 = false;//找到map2
			Integer payType = (Integer)priceMap1.get("pay_type");
			if(payType==0&&priceList.size()>1){
				for(Map map : priceList){
					if(pm1&&pm2)
						break;
					payType = (Integer)map.get("pay_type");
					Integer btime = (Integer)map.get("b_time");
					Integer etime = (Integer)map.get("e_time");
					if(payType==0&&etime>btime){
						if(!pm1){
							priceMap1 = map;
							pm1=true;
						}else {
							priceMap2=map;
							pm2=true;
						}
					}else {
						if(!pm2){
							priceMap2=map;
							pm2=true;
						}
					}
				}
			}
		}
		double minPriceUnit = getminPriceUnit(comId,daService);
		Map assistMap = daService.getMap("select * from price_assist_tb where comid = ? and type = ?", new Object[]{comId, 0});
		priceMap1 = getPriceMap1(priceMap1,comId);
		Map orderInfp = CountPrice.getAccount(start, end, priceMap1, priceMap2, minPriceUnit, assistMap);

		//Double count= StringUtils.getAccount(start, end, priceMap1, priceMap2);
		return StringUtils.formatDouble(orderInfp.get("collect"))+"";	
	}
	
	private double getminPriceUnit(Long comId,DataBaseService daService){
		Map com =daService.getPojo("select * from com_info_tb where id=? "
				, new Object[]{comId});
		double minPriceUnit = Double.valueOf(com.get("minprice_unit") + "");
		return minPriceUnit;
	}
	
	/**
	 * 如果是桂林1,2,3类车场   价格为空将comid传过去 （20160719目前不支持那边价格）
	 * @param priceMap1
	 * @param comId
	 * @return
	 */
	private static Map<String, Object> getPriceMap1(Map priceMap1,Long comId) {
		if ((priceMap1 == null || priceMap1.size() == 0)) {
			String GLCOMIDS1 = CustomDefind.GLCOMIDS1+"|"+CustomDefind.GLCOMIDS2+"|"+CustomDefind.GLCOMIDS3;
			if (GLCOMIDS1 != null && comId != null) {
				String ids[] = GLCOMIDS1.split("\\|");
				if (ids.length > 0) {
					for (String id : ids) {
						if (id != null && Check.isLong(id)) {
							if (comId.equals(Long.valueOf(id))) {
								priceMap1 = new HashMap();
								priceMap1.put("comid",comId);
							}
						}
					}
				}
			}
		}
		return priceMap1;
	}
}
