package parkingos.com.bolink.service;



import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author liuqb
 * @date  2017-3-31
 */
public interface DoUpload {

	String checkTokenSign(String token, String sign, String data);

	String doLogin(String data, String sign, String ip);

	String logout(String parkId, String soureIp);

	String inPark(JSONObject jsonData);

	String outPark(JSONObject jsonData);

	String uploadOrder(JSONObject jsonData);

	String uploadCollector(JSONObject jsonData);

	String workRecord(JSONObject jsonData);

	String uploadLiftrod(JSONObject jsonData);

	String uploadGate(JSONObject jsonData);

	String uploadMonthMember(JSONObject jsonData);

	String carTypeUpload(JSONObject jsonData);

	String uploadMonthCard(JSONObject jsonData);

	String monthPayRecord(JSONObject jsonData);

	String blackUpload(JSONObject jsonData);

	void UploadConfirmOrder(String comid, String data);

	void monthMemberSync(JSONObject jsonData);

	void carTypeSync(JSONObject jsonData);

	void monthCardSync(JSONObject jsonData);

	void blackuserSync(JSONObject jsonData);

	void monthPaySync(JSONObject jsonData);

	void queryProdprice(JSONObject jsonData);

	void priceSync(JSONObject jsonData);

	void lockCar(JSONObject jsonData);

	void collectSync(JSONObject jsonData);

	void gateSync(JSONObject jsonData);

	//void zeroOrderSync(JSONObject jsonData);

	void operateLiftrod(String comid, String channelId, Integer state, Integer operate);

	void deliverTicket(String parkId, String jsonData);

	void confirmOrderInform(String parkId, String jsonData);

	List<String> syncData(String parkId, String token, String data);

	/**
	 * 车场日志上传接口声明
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
	 * 上传价格信息
	 * @param data
	 * @return
	 */
	public String uploadPrice(String token,String data);

	void doBeat(String sourceIp);
    String getComId(String parkId);
}

