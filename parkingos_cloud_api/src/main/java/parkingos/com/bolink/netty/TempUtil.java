package parkingos.com.bolink.netty;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;
import parkingos.com.bolink.utlis.Check;

import java.util.HashMap;
import java.util.Map;

public class TempUtil {

    //存储http长连接的异步对象
    public static Map<String,Object> httpData = new HashMap<String,Object>();

   // public static Map<String,Integer> dataSendMap = new ConcurrentHashMap<>();

    public static LRUMap dataSendMap = new LRUMap(5000);


    static Logger logger = Logger.getLogger(TempUtil.class);

    //2018-03-15 00:00:00
    private static Long stime = 1521043200L;


    /**
     * 判断是不是是客户端重发的无效请求，主要判断进场和出场
     * @param data 数据包
     * @return 是否是重发的数据
     */
    public static boolean isReSend(JSONObject data){
        /*
       {"service_name":"upload_order","sign":"037CA7F89CF7AFBCE1A01D8D5680A356",
       "token":"791924e4ac2245a2a9af9355d40b9faf",
       "data":{"pay_state":0,"in_time":1521684057,"out_time":1521687033,"uid":"2","duration":50,
       "car_type":"临时车","c_type":"自动识别","out_type":"自动识别","pay_type":"cash","auth_code":"",
       "empty_plot":100,"freereasons":"0","in_channel_id":"1","out_channel_id":"101","worksite_id":0,
       "ticket_id":0,"reduce_amount":"0","amount_receivable":"6","total":"6","electronic_prepay":"0",
       "electronic_pay":"0","cash_prepay":"0","cash_pay":"6","out_uid":"2","user_type":1,"islocked":0,
       "lock_key":2345,"remark":"","state":0,"errmsg":"","outPic":36296,"service_name":"upload_order",
       "data_target":"cloud","order_id":"24442","car_number":"晋A1905Q"}}
         */
        boolean isResend = false;
        try {
            if (data != null && data.containsKey("service_name")) {
                String serviceName = data.getString("service_name");
                if (serviceName != null) {
                    //只拦截inpark或outpark
                    if (serviceName.contains("out_park") ||serviceName.contains("in_park") || serviceName.contains("upload_order")) {
//                        Long ntime = System.currentTimeMillis()/1000;
//                        long abstime = ntime-stime;
//                        if(abstime%3600<2){
//                            logger.info("clear resend mem>>>>>>>清除重发缓存...");
//                            dataSendMap.clear();
//                        }else{
//                            logger.info("resend mem size :"+dataSendMap.size());
//                        }
                        //先判断sign，已经处理过的不处理
                        if (data.containsKey("sign")) {
                            String sign = data.getString("sign");
                            if (dataSendMap.containsKey(sign)) {
                                logger.error("resend mem>>>>>>data:" + data + ",sign已经处理过....");
                                return true;
                            } else {
                                dataSendMap.put(sign, 1);
                            }
                        }
                        //再判断是不是同一个进场或出场请求
                        String token = "";
                        if (data.containsKey("token"))
                            token = data.getString("token");
                        else{
                            logger.info("resend mem  没有token,不处理....");
                            return true;
                        }

                        if (data.containsKey("data")) {
                            JSONObject dataJson = data.getJSONObject("data");
                            if (dataJson != null) {
                                String orderId = "";
                                if (dataJson.containsKey("order_id")) {
                                    orderId = dataJson.getString("order_id");
                                    if (!Check.isEmpty(token) && !Check.isEmpty(serviceName) && !Check.isEmpty(orderId)) {
                                        String key = token + serviceName + orderId;
                                        if (dataSendMap.containsKey(key)) {
                                            Integer times = (Integer)dataSendMap.get(key);
                                            if (times > 1)//只处理两次
                                            {
                                                logger.error("data:" + data + ",data已经处理过....");
                                                return true;
                                            }
                                            else {
                                                dataSendMap.put(key, times + 1);
                                            }
                                        } else {
                                            dataSendMap.put(token + serviceName + orderId, 1);
                                        }
                                    }

                                }

                            }
                        }

                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isResend;
    }

}
