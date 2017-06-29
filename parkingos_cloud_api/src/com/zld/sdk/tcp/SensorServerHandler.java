package com.zld.sdk.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.io.UnsupportedEncodingException;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.zld.sdk.doupload.DoUpload;
import com.zld.utils.Check;


/**
 * 
 * @author laoyao
 * 
 */

@Sharable
public class SensorServerHandler extends ChannelHandlerAdapter {

	Logger logger = Logger.getLogger(SensorServerHandler.class);

	 @Override 
	 public void channelActive(ChannelHandlerContext ctx) throws Exception { 
		 logger.error("一个新的连接:"+ctx.channel()); 
		 ctx.fireChannelActive(); 
		 ctx.flush();
	 }
	 
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)	throws Exception {
		//logger.error("服务器收到消息：" + msg);
		if (msg != null && !"".equals(msg.toString())) {
			//接收客户端消息
			ByteBuf bytebuf = (ByteBuf)msg;
			int count = bytebuf.readableBytes();
			byte[] arrbyte = new byte[count];
			bytebuf.readBytes(arrbyte);
			String mesg = new String(arrbyte,"UTF-8");
			if(mesg.length()>10)
				logger.error("服务器收到消息：" + mesg);
			if (mesg.indexOf("0x11") != -1&&mesg.length()<6) {// 心跳
				doBackMessage("0x12", ctx.channel());
				return;
			}
			JSONObject jsonMesg = JSONObject.fromObject(mesg);
			if (jsonMesg != null) {
				String service_name = null;
				if(jsonMesg.containsKey("service_name")){
					service_name = jsonMesg.getString("service_name");
				}else{
					String mess = "{\"state\":0,\"errmsg\":\"未识别到service_name标记\"}";
					doBackMessage(mess,ctx.channel());
					logger.error("**************缺少必须参数：service_name,请检查上传数据********");
					return;
				}
				String data = null;
				if(jsonMesg.containsKey("data")){
					data = jsonMesg.getString("data");
				}
				String sign = null;
				if(jsonMesg.containsKey("sign")){
					sign=jsonMesg.getString("sign");
				}
				if(Check.isEmpty(sign)){
					logger.error("data error  ,error:未识别到sign标记");
					String mess = "{\"state\":0,\"errmsg\":\"未识别到sign标记\"}";
					doBackMessage(mess,ctx.channel());
					return;
				}
				//防止重复发送消息，登录的请求不过滤
				if(CacheHandle.containsItem(sign)&&!service_name.equals("login")
						&&!service_name.equals("lock_car")&&!service_name.equals("month_member_sync")){
					logger.error("data error ,error:sing重复");
					String mess = "{\"state\":1,\"errmsg\":\"error:sign重复\"}";
					doBackMessage(mess,ctx.channel());
					return ;
				}
				
				logger.error("开始执行tcp接口协议的方法调用");
				if (service_name.equals("login")) {// 登录数据
					JSONObject jsonObj = JSONObject.fromObject(data);
					String parkId = jsonObj.getString("park_id");
					if (Check.isEmpty(parkId)) {// 账户为空直接返回
						logger.error("tcp park login ,error:未识别到park_id标记");
						String mess = "{\"state\":0,\"errmsg\":\"未识别到park_id标记\"}";
						doBackMessage(mess,ctx.channel());
						return;
					}
					String localId = null;
					String key = parkId;
					if(jsonObj.containsKey("local_id")){//收费系统编号
						localId = jsonObj.getString("local_id");
						if(!Check.isEmpty(localId)){
							key +="_"+localId;
						}
					}
					NettyChannelMap.add(key, ctx.channel());
					doLogin(jsonObj, sign,key);
				}else {
					String token =  jsonMesg.getString("token");
					if (Check.isEmpty(token)) {// token值为空直接返回
						logger.error("data error ,error:未识别到token标记");
						String mess = "{\"state\":0,\"errmsg\":\"未识别到token标记\"}";
						doBackMessage(mess,ctx.channel());
						//验证token
						return;
					}
					DoUpload doUpload= NettyChannelMap.doUpload;
					if(doUpload==null){
						logger.error("server error ,初始化失败....");
						String mess = "{\"state\":0,\"errmsg\":\"服务器初始化失败\"}";
						doBackMessage(mess,ctx.channel());
						return;
					}
					
					String parkId = doUpload.checkTokenSign(token,sign,data);
					if(parkId.indexOf("error")!=-1){//验证有错误
						logger.error("server error ,"+parkId);
						String mess = "{\"state\":0,\"errmsg\":\""+parkId+"\"}";
						doBackMessage(mess,ctx.channel());
						return;
					}
					
					String backMesg = "";
					if(service_name.equals("in_park")) {
						backMesg=doUpload.enterPark(parkId,data);
					}else if(service_name.equals("out_park")){
						backMesg=doUpload.outPark(parkId, data);
					}else if(service_name.equals("upload_order")){
						backMesg=doUpload.exitPark(parkId, data);
					}else if(service_name.equals("work_record")){
						backMesg=doUpload.uploadWorkRecord(parkId, data);
					}else if(service_name.equals("park_log")){
						backMesg=doUpload.uploadLog(parkId, data);
					}else if(service_name.equals("upload_liftrod")){
						backMesg=doUpload.uploadLiftrod(parkId, data);
					}else if(service_name.equals("upload_ticket")){
						backMesg=doUpload.uploadTicket(parkId, data);
					}else if(service_name.equals("upload_month_member")){
						backMesg=doUpload.uploadMonthMember(parkId, data);
					}else if(service_name.equals("upload_month_card")){
						backMesg=doUpload.uploadMonthCard(parkId, data);
					}else if(service_name.equals("upload_price")){
						backMesg=doUpload.uploadPrice(parkId, data);
					}else if(service_name.equals("upload_collector")){
						backMesg=doUpload.uploadCollector(parkId, data);
					}else if(service_name.equals("upload_carpic")){
						backMesg=doUpload.uploadCarpic(parkId, data);
					}else if(service_name.equals("month_pay_record")){
						backMesg=doUpload.monthPayRecord(parkId, data);
					}else if(service_name.equals("query_prodprice")){
						backMesg=doUpload.queryProdprice(parkId, data);
					}else if(service_name.equals("doUploadCarType")){
//					String res = doUpload.doUploadCarType(sign,token,data);
					}else if(service_name.equals("doUploadSeverState")){
//					doUpload.doUploadSeverState(sign,token,data);
					}else if(service_name.equals("doUploadBrakeState")){
//					doUpload.doUploadBrakeState(sign,token,data);
					}
					//下行数据，异步返回
					else if(service_name.equals("price_sync")){
						JSONObject jsonObj = JSONObject.fromObject(data);
						Integer state = jsonObj.getInt("state");
						String priceId = jsonObj.getString("price_id");
						Integer operate = jsonObj.getInt("operate_type");
						priceSyncAfter(Long.valueOf(parkId),priceId,state,operate);
					}else if(service_name.equals("month_card_sync")){
						JSONObject jsonObj = JSONObject.fromObject(data);
						Integer state = jsonObj.getInt("state");
						String cardId = jsonObj.getString("card_id");
						Integer operate = jsonObj.getInt("operate_type");
						packageSyncAfter(Long.valueOf(parkId), cardId,state,operate);
					}else if(service_name.equals("month_member_sync")){
						JSONObject jsonObj = JSONObject.fromObject(data);
						Integer state = jsonObj.getInt("state");
						String card_id = jsonObj.getString("card_id");
						Integer operate = jsonObj.getInt("operate_type");
						userInfoSyncAfter(Long.valueOf(parkId), card_id,state,operate);
					}else if(service_name.equals("lock_car")){//锁车通知返回
						doUpload.lockCar(data);
					}else if(service_name.equals("operate_liftrod")){//抬杆通知返回
						JSONObject jsonObj = JSONObject.fromObject(data);
						Integer state = jsonObj.getInt("state");
						String channelId = jsonObj.getString("channel_id");
						Integer operate = jsonObj.getInt("operate");
						operateLiftrod(Long.valueOf(parkId), channelId,state,operate);
					}else{
						logger.error("没有正确的service_name");
						backMesg="{\"state\":0,\"errmsg\":\"没有正确的service_name\"}";
					}
					doBackMessage(backMesg, ctx.channel());
				}
			}
		}else {
			logger.error("接收消息异常,消息内容:"+msg);
		}
	}



	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.error("连接断开，释放资源" + ctx.channel());
		DoUpload doUpload = NettyChannelMap.doUpload;
		logger.info("tcpHandle:" + doUpload);
		if (doUpload != null) {
			String parkId = NettyChannelMap.getParkByChannel(ctx.channel());
			if (parkId != null)
				doUpload.logout(parkId);
			logger.error(parkId + ",退出登录！");
		}
		ctx.close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.error(">>>>>>>>>>>>>>>>>"+cause.getMessage()+"--"+ctx.channel());
	}


	/**
	 * 服务端消息返回到客户端
	 * 
	 * @param mesg
	 * @param data
	 */
	private void doBackMessage(String mesg, Channel channel) {
		if (channel != null && channel.isActive()&& channel.isWritable()) {
			try {
				//发送消息给客户端
				if(mesg.length()>10)
					logger.error("服务器处理返回:"+mesg+","+channel);
				byte[] req = ("\n" + mesg + "\r").getBytes("UTF-8");
				ByteBuf buf = Unpooled.buffer(req.length);
				buf.writeBytes(req);
				channel.writeAndFlush(buf);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				logger.error("返回消息到客户端出现异常");
			}
		}else{
			logger.error("客户端已掉线...");
		}
	}
	/**
	 * 登录处理
	 * 
	 * @param data
	 * @param ctx
	 */
	private void doLogin(JSONObject data, String sign,String key) {
		DoUpload doUpload = NettyChannelMap.doUpload;
		Channel clientChannel = NettyChannelMap.get(key);
		if (doUpload != null) {
			String token = doUpload.doLogin(data, sign,clientChannel.remoteAddress().toString());
			String result = "{\"state\":1,\"token\":\"" + token + "\","+"\"service_name\":\"login\"}";
			if (token != null && token.indexOf("error") != -1) {// 登录失败
				result = "{\"state\":0,\"errmsg\":\"" + token + "\","+"\"service_name\":\"login\"}";
			}
			logger.error("返回登录消息" + result + ",channel:" + clientChannel);
			doBackMessage(result, clientChannel);
		} else {
			logger.error("服务器初始化失败");
			String result = "{\"state\":0,\"errmsg\":\"服务器初始化异常\"}";
			doBackMessage(result, clientChannel);
		}
	}
	
	/**
	 * 价格同步后修改价格表中的同步状态
	 * @param comid
	 * @param id
	 */
	private void priceSyncAfter(Long comid,String id,Integer state,Integer operate){
		DoUpload doUpload = NettyChannelMap.doUpload;
		if(doUpload != null){
			String ret = doUpload.priceSyncAfter(comid, id,state,operate);
			logger.info(ret);
		}else{
			logger.error("********价格同步后修改同步状态失败，doUpload对象实例异常");
		}
	}
	/**
	 * 月卡同步后修改月卡表中的同步状态
	 * @param comid
	 * @param id
	 */
	private void packageSyncAfter(Long comid,String id,Integer state,Integer operate){
		DoUpload doUpload = NettyChannelMap.doUpload;
		if(doUpload != null){
			String ret = doUpload.packageSyncAfter(comid, id,state,operate);
			logger.info(ret);
		}else{
			logger.error("********月卡同步后修改同步状态失败，doUpload对象实例异常");
		}
	}
	/**
	 * 月卡会员同步后修改会员表中的同步状态
	 * @param comid
	 * @param id
	 */
	private void userInfoSyncAfter(Long comid,String cardId,Integer state,Integer operate){
		DoUpload doUpload = NettyChannelMap.doUpload;
		if(doUpload != null){
			String ret = doUpload.userInfoSyncAfter(comid, cardId,state,operate);
			logger.info(ret);
		}else{
			logger.error("********月卡会员同步后修改同步状态失败，doUpload对象实例异常");
		}
	}
	/**
	 * 发送抬杆记录后收到返回消息
	 * @param comid
	 * @param id
	 */
	private void operateLiftrod(Long comid,String channelId,Integer state,Integer operate){
		DoUpload doUpload = NettyChannelMap.doUpload;
		if(doUpload != null){
			doUpload.operateLiftrod(comid, channelId,state,operate);
		}else{
			logger.error("********月卡会员同步后修改同步状态失败，doUpload对象实例异常");
		}
	}
}
