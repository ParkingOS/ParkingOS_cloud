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

import com.cdtemplar.parking_sensor.*;
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
		updateSensor(ctx,msg);
		//ByteBuf resp = Unpooled.copiedBuffer("OK".getBytes());
        //ctx.writeAndFlush(resp);
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
	private void updateSensor(ChannelHandlerContext arg0,Object msg)
	{
		if(msg != null && !"".equals(msg))
		{
			Gson gson = new Gson();
			String arg1 = (String)msg;
			if(arg1.startsWith("{") && arg1.endsWith("}"))
			{
				CNetGateMsg smv2 = SensorInterface.GetSiteMsg(arg1);
				if(smv2 != null)
				{
					String siteCID = smv2.getNetgateKey();
					int sv = 0;
					if(smv2.VAR == 0)	//版本0,只返回OK
					{
						arg0.writeAndFlush("OK\r\n");
					}
					else
					{
						String jsonMG = memcacheService.get(smv2.getNetgateKey());
						CNetgate ng = gson.fromJson(jsonMG, CNetgate.class);
						if(ng == null)
						{
							ng = new CNetgate();
							ng.CID = smv2.CID;
							ng.POT = smv2.POT + smv2.SensorNum();
							ng.POT %= SensorInterface.getMaxPoint();
							
							memcacheService.set(smv2.getNetgateKey(),gson.toJson(ng));
							arg0.writeAndFlush(SensorInterface.GetReadString(-1));
						}
						else
						{
							if(smv2.PD == ng.POT)
							{
								arg0.writeAndFlush(SensorInterface.GetReadString(-1));
								ng.POT += smv2.SensorNum();
								ng.POT %= SensorInterface.getMaxPoint();	//防止超过最大值
								
								memcacheService.set(smv2.getNetgateKey(),gson.toJson(ng));
							}
							else
							{
								arg0.writeAndFlush(SensorInterface.GetReadString(ng.POT));
								return;			//不连接的数据不做处理
							}
						}
					}
					for(int i=0; i<smv2.SensorNum(); i++)
					{
						
						CSensorValues csvGet = smv2.getSensorValues(i);
						sv = csvGet.CV;
						String sensorID = csvGet.getSensorKey();
						String json = memcacheService.get(sensorID);
						SensorInfo sensorInfo = gson.fromJson(json, SensorInfo.class);
						CSensorValues csv = new CSensorValues();
						csv.DT = csvGet.DT;
						csv.ID = csvGet.ID;
						if(sensorInfo == null)
						{
							sensorInfo = new SensorInfo();
							sensorInfo.setId("" + csv.ID);
							sensorInfo.setX(csv.X);
							sensorInfo.setY(csv.Y);
							sensorInfo.setZ(csv.Z);
							sensorInfo.setD(csv.D);
							
							sensorInfo.setX0(csv.X);
							sensorInfo.setY0(csv.Y);
							sensorInfo.setZ0(csv.Z);
							
							sensorInfo.setRate(0);
							sensorInfo.setStatus(0);

						}
						//else
						//{
							//if(csv.ID == 6094)
							//	System.out.print("\r\n======" + csv);
							csv.BusyRate = sensorInfo.getRate();
							csv.X0 = sensorInfo.getX0();
							csv.Y0 = sensorInfo.getY0();
							csv.Z0 = sensorInfo.getZ0();
							csv.X = sensorInfo.getX();
							csv.Y = sensorInfo.getY();
							csv.Z = sensorInfo.getZ();
							csv.D = sensorInfo.getD();
							

							csv.OnUpdate(csvGet.X, csvGet.Y, csvGet.Z, csvGet.D);
							
							sensorInfo.setX(csv.X);
							sensorInfo.setY(csv.Y);
							sensorInfo.setZ(csv.Z);
							sensorInfo.setD(csv.D);
							sensorInfo.setRate(csv.BusyRate);
							memcacheService.set(sensorID, gson.toJson(sensorInfo));
						//}
						int rate = sensorInfo.getRate();
						if(rate >= 500 && sensorInfo.getStatus() == 0){//无车
							String lock = sensorInCarLock(sensorID);
							logger.error("sensor_incar_lock:"+lock);
							if(memcacheService.lock(lock, 5)){//一个车检器消息只能5秒一次
								logger.error("send in car message>>>snesorID:"+sensorID);
								//---------------------发送进车消息-------------------//
								Map<String, String> param = new HashMap<String, String>();
								param.put("sensornumber", sensorID);
								//param.put("carintime", TimeTools.getTime_yyyyMMdd_HHmmss(curTime * 1000));
								param.put("carintime", csvGet.getTimeString());
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
								//param.put("carouttime", TimeTools.getTime_yyyyMMdd_HHmmss(curTime * 1000));
								param.put("carintime", csvGet.getTimeString());
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
							param.put("battery", ((double)csvGet.V/100.0) + "");
							param.put("site_uuid", siteCID);//车检器所绑定的基站唯一编号
							HttpAsyncProxy.post(Constants.SENSOR_HEART_URL, param, sensorHandler);
						}
					} 
					String lock = siteHeartLock(siteCID);
					logger.error("site_heart_lock:"+lock);
					if(sv > 0)
					{
						if(memcacheService.lock(lock, 60)){
							//---------------------发送基站心跳消息-------------------//
							Map<String, String> param = new HashMap<String, String>();
							param.put("transmitternumber", siteCID);
							param.put("voltagecaution", ((double)sv/100.0) + "");
							HttpAsyncProxy.post(Constants.SITE_HEART_URL, param, siteHandler);
						}
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
