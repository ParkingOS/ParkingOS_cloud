package com.tq.zld.pay;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.tq.zld.util.LogUtils;

public class AliPayResult {

	public static final String ERR_SUCCESS = "操作成功";
	public static final String ERR_SYSTEM_ERROR = "系统异常！";
	public static final String ERR_WRONG_DATA = "数据格式不正确！";
	public static final String ERR_ALIPAY_ACCOUNT_FREEZING = "支付宝账户冻结或不允许支付！";
	public static final String ERR_ALIPAY_ACCOUNT_UNBIND = "该用户已解除绑定！";
	public static final String ERR_ALIPAY_ACCOUNT_BINDERR = "账户绑定失败或没有绑定！";
	public static final String ERR_ALIPAY_ACCOUNT_REBIND = "重新绑定账户！";
	public static final String ERR_ALIPAY_SERVERUPDATING = "支付宝服务器维护中！";
	public static final String ERR_PAY_FAILED = "订单支付失败！";
	public static final String ERR_PAY_FAILED_WEB = "网页支付失败！";
	public static final String ERR_PAY_CACELED = "支付取消！";
	public static final String ERR_OTHER = "网络异常！";
	public static final String TIP_SUCCESS = "付款成功，已自动生成订单。";
	public static final String TIP_ACCOUNT_ERROR = "您可尝试更换支付宝账户或使用其他方式支付。";
	public static final String TIP_PAY_ERROR = "您可尝试使用其他支付方式或稍后再试。";

	private String mResult;

	private static final Map<String, String> sResultStatus;
	private static final Map<String, String> tipsMap;

	public String resultStatus = null;
	public String tips = null;
	public String memo = null;
	private String result = null;
	public boolean isSignOk = false;

	public AliPayResult(String result) {
		this.mResult = result;
	}

	static {
		sResultStatus = new HashMap<String, String>();
		sResultStatus.put("9000", ERR_SUCCESS);
		sResultStatus.put("4000", ERR_SYSTEM_ERROR);
		sResultStatus.put("4001", ERR_WRONG_DATA);
		sResultStatus.put("4003", ERR_ALIPAY_ACCOUNT_FREEZING);
		sResultStatus.put("4004", ERR_ALIPAY_ACCOUNT_UNBIND);
		sResultStatus.put("4005", ERR_ALIPAY_ACCOUNT_BINDERR);
		sResultStatus.put("4006", ERR_PAY_FAILED);
		sResultStatus.put("4010", ERR_ALIPAY_ACCOUNT_REBIND);
		sResultStatus.put("6000", ERR_ALIPAY_SERVERUPDATING);
		sResultStatus.put("6001", ERR_PAY_CACELED);
		sResultStatus.put("7001", ERR_PAY_FAILED_WEB);
		tipsMap = new HashMap<String, String>();
		tipsMap.put("9000", TIP_SUCCESS);
		tipsMap.put("4000", TIP_PAY_ERROR);
		tipsMap.put("4001", TIP_PAY_ERROR);
		tipsMap.put("4003", TIP_ACCOUNT_ERROR);
		tipsMap.put("4004", TIP_ACCOUNT_ERROR);
		tipsMap.put("4005", TIP_ACCOUNT_ERROR);
		tipsMap.put("4006", TIP_PAY_ERROR);
		tipsMap.put("4010", TIP_ACCOUNT_ERROR);
		tipsMap.put("6000", TIP_ACCOUNT_ERROR);
		tipsMap.put("6001", TIP_PAY_ERROR);
		tipsMap.put("7001", TIP_PAY_ERROR);
	}

	public String getResult() {
		String src = mResult.replace("{", "");
		src = src.replace("}", "");
		return getContent(src, "memo=", ";result");
	}

	public void parseResult() {

		try {
			String src = mResult.replace("{", "");
			src = src.replace("}", "");
			String rs = getContent(src, "resultStatus=", ";memo");
			if (sResultStatus.containsKey(rs)) {
				resultStatus = sResultStatus.get(rs);
				tips = tipsMap.get(rs);
			} else {
				resultStatus = ERR_OTHER;
				tips = TIP_PAY_ERROR;
			}
			memo = getContent(src, "memo=", ";result");
			result = getContent(src, "result=", null);
			isSignOk = checkSign(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean checkSign(String result) {
		boolean retVal = false;
		try {
			JSONObject json = string2JSON(result, "&");

			int pos = result.indexOf("&sign_type=");
			String signContent = result.substring(0, pos);

			String signType = json.getString("sign_type");
			signType = signType.replace("\"", "");

			String sign = json.getString("sign");
			sign = sign.replace("\"", "");

			if (signType.equalsIgnoreCase("RSA")) {
				retVal = Rsa.doCheck(signContent, sign, Keys.ALIPAY_PUBLIC);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.i(getClass(), "checkSignException e: --->> " + e);
		}
		LogUtils.i(getClass(), "checkSign: --->> " + retVal);
		return retVal;
	}

	public JSONObject string2JSON(String src, String split) {
		JSONObject json = new JSONObject();

		try {
			String[] arr = src.split(split);
			for (int i = 0; i < arr.length; i++) {
				String[] arrKey = arr[i].split("=");
				json.put(arrKey[0], arr[i].substring(arrKey[0].length() + 1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}

	private String getContent(String src, String startTag, String endTag) {
		String content = src;
		int start = src.indexOf(startTag);
		start += startTag.length();

		try {
			if (endTag != null) {
				int end = src.indexOf(endTag);
				content = src.substring(start, end);
			} else {
				content = src.substring(start);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return content;
	}
}
