package parkingos.com.bolink.utlis;

import com.zld.common_dao.dao.CommonDao;
import com.zld.common_dao.qo.PageOrderConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import parkingos.com.bolink.actions.UploadCarPics;
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
    Logger logger = Logger.getLogger(UploadCarPics.class);
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
     * @param mesg 需要发送的消息
     * @param channel 通道
     */
    public boolean doBackMessage(String mesg, Channel channel) {
        if (channel != null && channel.isActive()
                && channel.isWritable()) {
            try {
                logger.error("发消息到SDK，channel:"+channel+",mesg:" + mesg);
                byte[] req= ("\n" + mesg + "\r").getBytes("utf-8");
                ByteBuf buf = Unpooled.buffer(req.length);
                buf.writeBytes(req);
                channel.writeAndFlush(buf);
                return true;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else{
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
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
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
}
