package parkingos.com.bolink.component.impl;

import com.alibaba.fastjson.JSONObject;
import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import parkingos.com.bolink.beans.ParkTokenTb;
import parkingos.com.bolink.component.TcpComponent;
import parkingos.com.bolink.utlis.CheckUtil;
import parkingos.com.bolink.utlis.HttpProxy;
import parkingos.com.bolink.utlis.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class TcpComponentImpl implements TcpComponent {
    Logger logger = Logger.getLogger(TcpComponentImpl.class);

    @Autowired
    CommonDao<ParkTokenTb> parkTokenCommonDao;
    @Autowired
    HttpProxy httpProxy;

    @Override
    public int sendMessageToSDK(Long comId, String data) {
        logger.error(">>>>>>>>>>>>>>开始处理："+data);
        int ret = 0;
        //1.根据comId查询车场SDK登录信息
        ParkTokenTb parkTokenConditions = new ParkTokenTb();
        parkTokenConditions.setParkId(comId+"");
        List<ParkTokenTb> parkTokens = parkTokenCommonDao.selectListByConditions(parkTokenConditions);
        ParkTokenTb loginInfo = null;
        if(CheckUtil.hasElement(parkTokens)){
            int nextInt =  new Random().nextInt(parkTokens.size());
            loginInfo = parkTokens.get(nextInt);
        }else{
            //没有可用的SDK
            ret = 3;
            return ret;
        }
        //2.发送消息
        String serverIp = loginInfo.getServerIp();
        String localId = loginInfo.getLocalId();
        String[] split = data.split("&");
        Map<String, String> params = new HashMap<String, String>();
        for (int i = 0; i <split.length ; i++) {
            String[] param = split[i].split("=");
            String key = param[0];
            String value = param[1];
            params.put(key,value);
        }
        params.put("local_id",StringUtils.encodeUTF8(localId));
        logger.info(params);
        //data += "&local_id="+ localId;
        //TODO
        //serverIp = "127.0.0.1";
        if(serverIp!=null){
            String url = "http://"+serverIp+"/zld/sendmsgtopark.do";
            try {
                String result = httpProxy.doPost(url,params);
                if(CheckUtil.isNotNull(result)){
                    JSONObject retObject = JSONObject.parseObject(result);
                    boolean isSend = retObject.getBooleanValue("state");
                    if(isSend){
                        ret = 1;
                    }else{
                        ret = 2;
                    }
                    logger.info("发送消息:"+isSend);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return ret;
    }
}
