package parkingos.com.bolink.utlis;

import com.alibaba.fastjson.JSONObject;
import com.zld.common_dao.dao.CommonDao;
import com.zld.common_dao.qo.PageOrderConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import parkingos.com.bolink.beans.ParkTokenTb;

import javax.net.ssl.*;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;
import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.util.List;

@Component
public class CommonUtils {
    Logger logger = Logger.getLogger(CommonUtils.class);
    @Autowired
    CommonDao commonDao;

    /**
     * 获取下发数据的TCP通道
     *
     * @param comid
     * @return
     */
    public String getChannel(String comid) {
       /* String channelPass = "";
        ParkTokenTb tokenTb = new ParkTokenTb();
        tokenTb.setParkId(comid);
        tokenTb = commonDao.selectObjectBySelective(tokenTb);
//		Map parkMap = null;// daService.getMap("select * from park_token_tb where park_id=? order by id desc ", new Object[]{comid});
        if (tokenTb != null ) {
            String localId = tokenTb.getLocalId();
            if (!Check.isEmpty(localId)) {
                channelPass = comid + "_" + localId;
            }
        } else {
            channelPass = comid;
        }
        logger.error("sdk comid:" + channelPass);
        return channelPass;*/
        String channelPass = "";
        ParkTokenTb tokenTb = new ParkTokenTb();
        tokenTb.setParkId(comid);
        PageOrderConfig orderConfig = new PageOrderConfig();
        orderConfig.setOrderInfo("id","desc");
        orderConfig.setPageInfo(1,null);
//        tokenTb.setOrderField("id");
//        tokenTb.setOrderType("desc");
//        tokenTb.setPageSize(0);
        List<ParkTokenTb> tokenTbs = commonDao.selectListByConditions(tokenTb,orderConfig);//.selectObjectBySelective(tokenTb);
        //Map parkMap = dataBaseService.getMap("select * from park_token_tb where park_id=?", new Object[]{comid});
        if(tokenTbs!=null&&!tokenTbs.isEmpty())
            tokenTb = tokenTbs.get(0);
        if(tokenTb != null ){
            String localId =tokenTb.getLocalId();// String.valueOf(parkMap.get("local_id"));
            if(!Check.isEmpty(localId)){
                channelPass += comid+"_"+localId;
            }
        }else{
            channelPass = comid;
        }
        return channelPass;
    }
    public static String httpsRequest(String requestUrl, String requestMethod, String outputStr) {
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = { new MyX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            URL url = new URL(requestUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(ssf);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            // 设置请求方式（GET/POST）
            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            // 当outputStr不为null时向输出流写数据
            if (null != outputStr) {
                OutputStream outputStream = conn.getOutputStream();
                // 注意编码格式
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }
            // 从输入流读取返回内容
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            StringBuffer buffer = new StringBuffer();
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            // 释放资源
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            inputStream = null;
            conn.disconnect();
            return buffer.toString();
        } catch (ConnectException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 消息返回
     *
     * @param mesg    需要发送的消息
     * @param channel 通道
     */
    public boolean doBackMessage(String mesg, Channel channel) {
        if (channel != null && channel.isActive()
                && channel.isWritable()) {
            try {
                logger.error("发消息到SDK，channel:" + channel + ",mesg:" + mesg);
                byte[] req = ("\n" + mesg + "\r").getBytes("utf-8");
                ByteBuf buf = Unpooled.buffer(req.length);
                buf.writeBytes(req);
                channel.writeAndFlush(buf);
                return true;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            logger.error("客户端已断开连接...");
        }
        return false;
    }

    /*private boolean doBackMessage(String mesg, Channel channel) {
        if (channel != null && channel.isActive()
				&& channel.isWritable()) {
			try {
				logger.error("发消息到SDK，channel:" + channel + ",mesg:" + mesg);
				byte[] req = ("\n" + mesg + "\r").getBytes("utf-8");
				ByteBuf buf = Unpooled.buffer(req.length);
				buf.writeBytes(req);
				ChannelFuture future = channel.writeAndFlush(buf);
				return true;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			logger.error("客户端已断开连接...");
		}
		return false;
	}*/

    public static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] arg0,
                    String arg1)
                    throws java.security.cert.CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] arg0,
                    String arg1)
                    throws java.security.cert.CertificateException {
            }
        }
        };
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取下发数据的TCP通道
     *
     * @param comid
     * @return
     */
    public ParkTokenTb getChannel(Long comid) {
        ParkTokenTb tokenTb = new ParkTokenTb();
        tokenTb.setParkId(comid + "");
        tokenTb.setReceiveCloud(1);
        List<ParkTokenTb> tokens = commonDao.selectListByConditions(tokenTb);
        logger.info(tokens);
        if (tokens != null && !tokens.isEmpty()) {
            return tokens.get(0);
        }
        return null;
    }

    public boolean doSendMessage(String message, ParkTokenTb parkTokenTb) {
        logger.error(parkTokenTb);
        //{id=30223, park_id=21836, token=6f5967a3de4b46389443e647a2a3d4f2, login_time=1521001258, beat_time=null,
        //server_ip=10.24.217.9, source_ip=/59.173.116.96:6529, local_id=00e18c926756_1200_01}
        String ip = parkTokenTb.getServerIp();//(String)parkTokenTb.get("server_ip");
        String localId = parkTokenTb.getLocalId();// (String)parkTokenTb.get("local_id");
        //logger.error(ip+","+localId);
        if (Check.isEmpty(localId))
//            localId =parkTokenTb.getLocalId();// parkTokenTb.get("park_id")+"";
            localId = parkTokenTb.getParkId();
        else {
            localId = parkTokenTb.getParkId() + "_" + parkTokenTb.getLocalId();// parkTokenTb.get("park_id")+"_"+localId;
        }
        logger.error(localId);
        JSONObject jsonObject = JSONObject.parseObject("{}");
        jsonObject.put("channelid", localId);
        jsonObject.put("data", jsonObject.parseObject(message));
        String url = "http://" + ip + ":8080/zld/sendmesgtopark";
        logger.error(url);
        logger.error(jsonObject);
        String ret = new HttpProxy().doHeadPost(url, jsonObject.toString());
        logger.error(ret);
        if (ret != null) {
            JSONObject result = JSONObject.parseObject(ret);
            if (result.containsKey("result"))
                return result.getBooleanValue("result");
        }
        return false;
    }


    /**
     * 获取TCP通道信息
     *
     * @param parkId        车场编号
     * @param parkChannelId 车场通道
     * @return
     */
    public ParkTokenTb getChannelInfo(String parkId, String parkChannelId) {


        if(parkChannelId==null||"".equals(parkChannelId)){
            logger.error("通道为空，发不下去呀大兄弟！");
            return null;
        }

        //1.查询通道信息
        ParkTokenTb conditions = new ParkTokenTb();
        conditions.setReceiveCloud(1);
        conditions.setParkId(parkId);

        List<ParkTokenTb> parkTokens = null;
        ParkTokenTb channelInfo = null;
        PageOrderConfig config = new PageOrderConfig();
        config.setOrderInfo("beat_time", "desc");
        boolean isSetChannels = false;//是否在登录时设置了出场通道编号
        //如果有parkChannelId,则根据unionid和parkid查找所有sdk,匹配local_id中包含parkChannelId的通道号
        parkTokens = commonDao.selectListByConditions(conditions, config);
        if (CheckUtil.hasElement(parkTokens)) {

            for (int i = 0; i < parkTokens.size(); i++) {
                ParkTokenTb parkToken = parkTokens.get(i);
                String currentLocalId = parkToken.getLocalId() == null ? "" : parkToken.getLocalId();
                isSetChannels = isSetChannels | currentLocalId.contains("channels");

                logger.info(parkToken + ",channel:" + parkChannelId + ",isSetChannels:" + isSetChannels);
                if (CheckUtil.isNotNull(currentLocalId)) {//local_id不为空并且sdk有心跳// && SDKUtil.isSDKOnline(loginTime, lastbeatTime)
                    if (currentLocalId.contains("channels")) {
                        //_channels_2_102_888 channelId = 102
                        currentLocalId = currentLocalId.substring(currentLocalId.indexOf("channels") + 8);
                        logger.info(currentLocalId);
                        if (currentLocalId.contains("_" + parkChannelId + "_")) {
                            channelInfo = parkToken;
                            //_channels_2_102_888 channelId = 888
                            break;
                        } else if (currentLocalId.contains("_" + parkChannelId) && currentLocalId.endsWith(parkChannelId)) {
                            channelInfo = parkToken;
                            break;
                        }
                    }
                }
            }

//                try {//如果没有匹配到出场通道，但所有的车场sdk登录都没有local_id时，取第一个
//                    if(channelInfo==null&&!isSetChannels) {
//                        for(ParkTokenTb p : parkTokens){
//                            channelInfo = p;
//                            break;
//                        }
//                        // channelInfo = parkTokens.get(0);
//                        logger.info("没有匹配到通道，取最近心跳的第一个===>>"+channelInfo.getLocalId());
//                    }else{
//                        if(channelInfo!=null)
//                            logger.info("匹配到通道====>>>"+channelInfo.getLocalId());
//                        else {
//                            logger.info("没有匹配到通道====>>>....");
//                        }
//                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                }

            // logger.info(channelInfo);
        }
        logger.info("获取到的通道信息："+channelInfo);
        return channelInfo;
    }

}
