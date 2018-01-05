package parkingos.com.bolink.utlis.weixinpay.utils;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class JsonUtil {

	public static String getJsonValue(String rescontent, String key) {
		JSONObject jsonObject;
		String v = null;
		try {
			jsonObject = JSONObject.parseObject(rescontent);
			v = jsonObject.getString(key);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return v;
	}

	public static String [] parseJson(String result){
		JSONArray jsonArray ;
		try {
			jsonArray = JSONArray.parseArray(result);
			System.out.println(jsonArray.toString());
			System.out.println(jsonArray.getJSONArray(11));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		String value = "[\"1263\",\"上地华联商厦停车场\",\"116.318700\",\"40.034339\",\"310\",\"1.0元/30分钟\",\"310\",\"北京市北京市海淀区农大南路1号院-1号楼\",\"\",\"1\",\"\",[\"parkpics/1263_1409710102.jpeg\",\"parkpics/1263_1409710102.jpeg\"]]";
		String  [] ret = parseJson(value);
	}
}
