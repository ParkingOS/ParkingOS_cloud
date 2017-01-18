package com.zld.pcloud_sensor.handler.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.zld.pcloud_sensor.Constants;
import com.zld.pcloud_sensor.handler.IHandler;
import com.zld.pcloud_sensor.pojo.SensorInfo;
import com.zld.pcloud_sensor.pojo.SensorMsg;
import com.zld.pcloud_sensor.pojo.SiteMsg;
import com.zld.pcloud_sensor.service.MemcacheService;
import com.zld.pcloud_sensor.util.CheckTable;
import com.zld.pcloud_sensor.util.HttpAsyncProxy;
import com.zld.pcloud_sensor.util.NettyChannelMap;
import com.zld.pcloud_sensor.util.TimeTools;
/**
 * 注意，pipeline中的所有context都是私有的，针对context的所有操作都是线程安全的，但context对象包含的handler不一定是私有的。
 * 比如添加了Sharable注解的handler，表示该handler自身可以保证线程安全，这种handler只实例化一个就够了。而对于没有添加Sharable注解的handler，
 * netty就默认该handler是有线程安全问题的，对应实例也不能被多个Context持有。
 * @author whx
 *
 */
@Service
@Sharable
public class SensorServerHandler extends ChannelHandlerAdapter {
	
	Logger logger = Logger.getLogger(SensorServerHandler.class);
	@Autowired
	@Resource(name = "sensorHandler")
	private IHandler sensorHandler;
	@Autowired
	@Resource(name = "siteHandler")
	private IHandler siteHandler;
	@Autowired
	private MemcacheService memcacheService;
	
	@Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.error("一个新的连接");
        ctx.fireChannelActive();
    }
	
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		logger.error(msg);
		sendMsgToTB(msg);
		updateSensor(msg);
		ByteBuf resp = Unpooled.copiedBuffer("OK".getBytes());
        ctx.writeAndFlush(resp);
    }
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
    }
	
	@Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.error("连接断开，释放资源" + ctx.channel());
        ctx.close();
    }
	
	/**
	 * 向天泊同步消息
	 * @param msg
	 */
	private void sendMsgToTB(Object msg){
		if(msg != null && !"".equals(msg)){
			Channel TB_CLIENT = NettyChannelMap.get(Constants.TB_CLIENT);
			logger.error("TB_CLIENT:" + TB_CLIENT);
			if(TB_CLIENT != null && TB_CLIENT.isActive() && TB_CLIENT.isWritable()){
				logger.error("向天泊推送同步消息");
				byte[] req = ("\n" + msg + "\r").getBytes();
				ByteBuf buf = Unpooled.buffer(req.length);
				buf.writeBytes(req);
				TB_CLIENT.writeAndFlush(buf);
			}
		}
	}
	
	/**
	 * 更新p5的车检器信息
	 * @param msg
	 */
	private void updateSensor(Object msg){
		if(msg != null && !"".equals(msg)){
			String message = (String)msg;
			if(message.startsWith("{") && message.endsWith("}")){
				
				Gson gson = new Gson();
				SiteMsg siteMsg = gson.fromJson(message, SiteMsg.class);
				String Err = siteMsg.getErr();
				//一个车检器10分钟才有一条数据，而集中器8秒收不到车检器或采集器的数据就会有ERR信息
				if(Err == null){
					Long curTime = System.currentTimeMillis()/1000;
					logger.error("curTime:" + curTime);
					String siteCID = siteMsg.getCID();//基站CID
					int sv = siteMsg.getV();//集中器电压
					List<SensorMsg> sensorMsgs = siteMsg.getMAG();
					if(sensorMsgs != null && !sensorMsgs.isEmpty()){
						for(SensorMsg sensorMsg : sensorMsgs){
							String sensorID = sensorMsg.getID();//车检器ID
							int x = sensorMsg.getX();
							int y = sensorMsg.getY();
							int z = sensorMsg.getZ();
							int d = sensorMsg.getD();
							int v = sensorMsg.getV();//v除以100表示电压值，比如v=100表示电压1伏
							logger.error("sensorID:"+sensorID);
							String json = memcacheService.get(sensorID);
							SensorInfo sensorInfo = gson.fromJson(json, SensorInfo.class);
							logger.error("sensorInfo:"+sensorInfo);
							if(sensorInfo == null){
								sensorInfo = new SensorInfo();
								sensorInfo.setId(sensorID);
							}
							//-------------------------记录最新的磁场值--------------------------//
							sensorInfo.setX(x);
							sensorInfo.setY(y);
							sensorInfo.setZ(z);
							//-------------------------计算车检器状态--------------------------//
							int x0 = sensorInfo.getX0();
							int y0 = sensorInfo.getY0();
							int z0 = sensorInfo.getZ0();
							int m = (int) Math.floor(Math.sqrt(Math.pow(x - x0, 2) + Math.pow(y - y0, 2)
									+ Math.pow(z - z0, 2)));
							logger.error("m:" + m + ",d:" + d);
							int rate = CheckTable.CalcProbability(m, d);
							logger.error("rate:"+rate);
							sensorInfo.setRate(rate);
							memcacheService.set(sensorID, gson.toJson(sensorInfo));
							if(rate >= 500 && sensorInfo.getStatus() == 0){//无车
								String lock = sensorInCarLock(sensorID);
								logger.error("sensor_incar_lock:"+lock);
								if(memcacheService.lock(lock, 5)){//一个车检器消息只能5秒一次
									logger.error("send in car message>>>snesorID:"+sensorID);
									//---------------------发送进车消息-------------------//
									Map<String, String> param = new HashMap<String, String>();
									param.put("sensornumber", sensorID);
									param.put("carintime", TimeTools.getTime_yyyyMMdd_HHmmss(curTime * 1000));
									param.put("indicate", "00");
									HttpAsyncProxy.post(Constants.SENSOR_IN_CAR, param, sensorHandler);
								}
							}else if(rate < 500 && sensorInfo.getStatus() == 1){
								String lock = sensorOutCarLock(sensorID);
								logger.error("sensor_outcar_lock:"+lock);
								if(memcacheService.lock(lock, 5)){//一个车检器消息只能5秒一次
									logger.error("send out car message>>>snesorID:"+sensorID);
									//---------------------发送出车消息-------------------//
									Map<String, String> param = new HashMap<String, String>();
									param.put("sensornumber", sensorID);
									param.put("carouttime", TimeTools.getTime_yyyyMMdd_HHmmss(curTime * 1000));
									param.put("indicate", "00");
									HttpAsyncProxy.post(Constants.SENSOR_OUT_CAR, param, sensorHandler);
								}
							}
							String lock = sensorHeartLock(sensorID);
							logger.error("sensor_heart_lock:"+lock);
							if(memcacheService.lock(lock, 60)){
								//---------------------发送车检器心跳消息-------------------//
								Map<String, String> param = new HashMap<String, String>();
								param.put("sensornumber", sensorID);
								param.put("battery", ((double)v/100) + "");
								param.put("site_uuid", siteCID);//车检器所绑定的基站唯一编号
								HttpAsyncProxy.post(Constants.SENSOR_HEART_URL, param, sensorHandler);
							}
						} 
					}
					String lock = siteHeartLock(siteCID);
					logger.error("site_heart_lock:"+lock);
					if(memcacheService.lock(lock, 60)){
						//---------------------发送基站心跳消息-------------------//
						Map<String, String> param = new HashMap<String, String>();
						param.put("transmitternumber", siteCID);
						param.put("voltagecaution", ((double)sv/100) + "");
						HttpAsyncProxy.post(Constants.SITE_HEART_URL, param, siteHandler);
					}
				}
			}
		}
	}
	
	private String sensorInCarLock(String key){
		String lock = memcacheService.getLock(key);
		return lock;
	}
	
	private String sensorOutCarLock(String key){
		String lock = memcacheService.getLock(key);
		return lock;
	}
	
	private String sensorHeartLock(String key){
		String lock = memcacheService.getLock(key);
		return lock;
	}
	
	private String siteHeartLock(String key){
		String lock = memcacheService.getLock(key);
		return lock;
	}
}
