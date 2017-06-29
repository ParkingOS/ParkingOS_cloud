package com.zld.sdk.doupload;

import net.sf.json.JSONObject;

/**
 * @author liuqb
 * @date  2017-3-31
 */
public interface DoUpload {
	
	/**
	 * 上传抬杆记录接口声明
	 * @param data
	 * @return
	 */
	public String uploadLiftrod(String comid,String data);
	/**
	 * 上传绑定车型接口声明
	 * 参数json中包括
	 * 车场编号：comid，绑定车辆信息：carNumberTypeInfo;
	 * 上传返回值结果：
	 * 更新抬杆记录时：成功还是失败：state；lineId：line_id；本地绑定车型ID：localId；
	 * 添加抬杆记录时：成功还是失败：state；
	 * @param data
	 * @return
	 */
	public String doUploadCarType(String sign, String token,JSONObject data);
	/**
	 * 上传员工工作记录接口声明
	 * @param data
	 * @return
	 */
	public String uploadWorkRecord(String comid,String data);
	/**
	 * 上传服务器工作状态接口声明
	 * @param data
	 * @return
	 */
	public String doUploadSeverState(String sign, String token,JSONObject data);
	/**
	 * 上传岗亭端工作状态接口声明
	 * @param data
	 * @return
	 */
	public String doUploadBrakeState(String sign, String token,JSONObject data);
	
	/**
	 * 登录tcp服务的接口声明
	 * @param data
	 * @param sign
	 * @return
	 */
	public String doLogin(JSONObject data,String sign,String sourceIP);
	
	/**
	 * 车场登录成功后验证签名是否一致
	 * 返回值为1表示数据签名一致，其他情况则有异常
	 * @param preSign
	 * @param token
	 * @param data
	 * @return
	 */
	public String checkSign(String preSign,String ukey,String data);
	
	/**
	 * 车辆入场时接口声明
	 * @param token：车场对应的token标识，避免恶意提交
	 * @param data：车场sdk上传数据
	 * @return
	 */
	public String enterPark(String comid,String data);
	
	/**
	 * 完整订单上传时接口声明
	 * @param token：车场对应的token标识，避免恶意提交
	 * @param data：车场sdk上传数据
	 * @return
	 */
	public String exitPark(String comid,String data);
	/**
	 * 车辆出场时接口声明
	 * @param token：车场对应的token标识，避免恶意提交
	 * @param data：车场sdk上传数据
	 * @return
	 */
	public String outPark(String comid,String data);
	
	/**
	 * 车场日志上传接口声明
	 * @param token：车场对应的token标识，避免恶意提交
	 * @param data：车场sdk上传数据
	 * @return
	 */
	public String uploadLog(String comid,String data);
	/**
	 * 上传减免券接口声明
	 * 
	 * @param data
	 * @return
	 */
	public String uploadTicket(String comid,String data);
	
	/**
	 * 上传月卡会员信息
	 * @param data
	 * @return
	 */
	public String uploadMonthMember(String comid,String data);
	
	/**
	 * 上传月卡信息
	 * @param data
	 * @return
	 */
	public String uploadMonthCard(String comid,String data);
	
	/**
	 * 上传价格信息
	 * @param data
	 * @return
	 */
	public String uploadPrice(String token,String data);
	
	/**
	 * 车辆图片上传
	 * @param parkId
	 * @param data
	 * @return
	 */
	public String uploadCarpic(String parkId,String data);
	/**
	 * 月卡续费记录上传
	 * @param parkId
	 * @param data
	 * @return
	 */
	public String monthPayRecord(String parkId,String data);
	
	/**
	 * 价格信息同步后修改数据库状态
	 * @param comid
	 * @param id
	 * @return
	 */
	public String priceSyncAfter(Long comid,String id,Integer state,Integer operate);
	
	/**
	 * 月卡信息同步后修改数据库状态
	 * @param comid
	 * @param id
	 * @return
	 */
	public String packageSyncAfter(Long comid,String id,Integer state,Integer operate);
	
	/**
	 * 月卡会员信息同步后修改数据库状态
	 * @param comid
	 * @param id
	 * @return
	 */
	public String userInfoSyncAfter(Long comid,String id,Integer state,Integer operate);
	/**
	 * 根据token值查询对应的注册车场
	 * @param token
	 * @return
	 */
	public String tokenCheck(String token);
	//退出登录
	public void logout(String parkId );
	//锁车
	public void lockCar(String jsonData);
	public String checkTokenSign(String token, String sign,String data);
	/**
	 * 上传车场收费员信息接口
	 * @param parkId
	 * @param data
	 * @return
	 */
	public String uploadCollector(String parkId, String data);
	
	/**
	 * 接收收费系统上传的月卡套餐价格存储到memcacheUtils中
	 * @param parkId
	 * @param data
	 */
	public String queryProdprice(String parkId,String data);
	
	/**
	 * 通知抬杆操作信息
	 * @param jsonData
	 */
	public void operateLiftrod(Long comid,String channelId,Integer state,Integer operate);
}

