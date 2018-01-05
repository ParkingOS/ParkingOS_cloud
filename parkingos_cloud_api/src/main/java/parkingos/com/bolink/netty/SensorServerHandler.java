package parkingos.com.bolink.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import parkingos.com.bolink.service.DoUpload;
import parkingos.com.bolink.utlis.Check;
import parkingos.com.bolink.utlis.ExecutorsUtil;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutorService;


/**
 * 注意，pipeline中的所有context都是私有的，针对context的所有操作都是线程安全的，但context对象包含的handler不一定是私有的
 * 。 比如添加了Sharable注解的handler，表示该handler自身可以保证线程安全，这种handler只实例化一个就够了。
 * 而对于没有添加Sharable注解的handler， netty就默认该handler是有线程安全问题的，对应实例也不能被多个Context持有。
 * 
 * @author whx
 * 
 */

@Sharable
public class SensorServerHandler extends ChannelHandlerAdapter {

	Logger logger = Logger.getLogger(SensorServerHandler.class);


	 @Override 
	 public void channelActive(ChannelHandlerContext ctx) throws Exception { 
		 logger.info("一个新的连接>>>>>>>>>>>"+ctx.channel().remoteAddress().toString());
		 ctx.fireChannelActive(); 
		 ctx.flush();
	 }
	 
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)	throws Exception {
		handleMessage(ctx,msg);
	}

	private void  handleMessage(final ChannelHandlerContext ctx,final Object msg){
		logger.info("tcp 开始处理消息>>>>"+msg.toString()+">>>>>>"+ctx.channel().remoteAddress().toString());
		ExecutorService es  = ExecutorsUtil.getExecutorPool();
		es.execute(new Runnable() {
			public void run() {
				if (msg != null && !"".equals(msg.toString())) {
					//接收客户端消息
					String mesg = msg.toString();
					/*try {
						mesg = new String(msg.toString().getBytes(), "UTF-8");
					} catch (Exception e) {
						e.printStackTrace();
					}
*/
					if (mesg.length() > 10)
						logger.error("服务器收到消息：" + mesg);
					if (mesg.indexOf("0x11") != -1 && mesg.length() < 6) {// 心跳
						doBackMessage("0x12", ctx.channel());
						return;
					}
					JSONObject jsonMesg = JSON.parseObject(mesg, Feature.OrderedField);
					logger.info(jsonMesg);
					if (jsonMesg != null) {
						String service_name = null;
						if (jsonMesg.containsKey("service_name")) {
							service_name = jsonMesg.getString("service_name");
						} else {
							String mess = "{\"state\":0,\"errmsg\":\"未识别到service_name标记\"}";
							doBackMessage(mess, ctx.channel());
							logger.error("**************缺少必须参数：service_name,请检查上传数据********");
							return;
						}
						String data = null;
						if (jsonMesg.containsKey("data")) {
							data = jsonMesg.getString("data");
						}
						String sign = null;
						if (jsonMesg.containsKey("sign")) {
							sign = jsonMesg.getString("sign");
						}
						if (Check.isEmpty(sign)) {
							logger.error("data error  ,error:未识别到sign标记");
							String mess = "{\"state\":0,\"errmsg\":\"未识别到sign标记\"}";
							doBackMessage(mess, ctx.channel());
							return;
						}


						logger.error("开始执行tcp接口协议的方法调用");
						if (service_name.equals("login")) {// 登录数据
							JSONObject jsonObj = JSONObject.parseObject(data);
							String parkId = jsonObj.getString("park_id");
							if (Check.isEmpty(parkId)) {// 账户为空直接返回
								logger.error("tcp park login ,error:未识别到park_id标记");
								String mess = "{\"state\":0,\"errmsg\":\"未识别到park_id标记\"}";
								doBackMessage(mess, ctx.channel());
								try {
									logger.error("关闭链接>>>>>"+ctx.channel().disconnect().isSuccess());
								}catch (Exception e){
									e.printStackTrace();
								}
								return;
							}
							String localId = null;
							String key = parkId;
							if (jsonObj.containsKey("local_id")) {//收费系统编号
								localId = jsonObj.getString("local_id");
								if (!Check.isEmpty(localId)) {
									key += "_" + localId;
								}
							}
							NettyChannelMap.add(key, ctx.channel());
							logger.info(data);
							doLogin(data, sign, key);
						} else {
							String token = jsonMesg.getString("token");
							if (Check.isEmpty(token)) {// token值为空直接返回
								logger.error("data error ,error:未识别到token标记");
								String mess = "{\"state\":0,\"errmsg\":\"未识别到token标记\"}";
								doBackMessage(mess, ctx.channel());
								//验证token
								return;
							}
							DoUpload doUpload = NettyChannelMap.doUpload;
							if (doUpload == null) {
								logger.error("server error ,初始化失败....");
								String mess = "{\"state\":0,\"errmsg\":\"服务器初始化失败\"}";
								doBackMessage(mess, ctx.channel());
								return;
							}

							String parkId = doUpload.checkTokenSign(token, sign, data);
							if (parkId.indexOf("error") != -1) {//验证有错误
								logger.error("server error ," + parkId);
								String mess = "{\"state\":0,\"errmsg\":\"" + parkId + "\"}";
								doBackMessage(mess, ctx.channel());
								return;
							}

							String backMesg = "";
							JSONObject jsonData = JSONObject.parseObject(data);
							if(jsonData!=null){
								jsonData.put("comid",parkId);
							}else {
								logger.error("data error:" + data);
								doBackMessage("{\"state\":0,\"errmsg\":\"data error\"}", ctx.channel());
								return ;
							}
							List<String> syncParks= null;
							if (service_name.equals("in_park")) {//进场 2.1
								backMesg=doUpload.inPark(jsonData);
								syncParks=doUpload.syncData(parkId,token,data);
							}else if(service_name.equals("upload_collector")){//收费员信息 2.4
								backMesg=doUpload.uploadCollector(jsonData);
							}else if (service_name.equals("out_park")) {//出场 2.2
								backMesg=doUpload.outPark(jsonData);
								syncParks=doUpload.syncData(parkId,token,data);
							}else if(service_name.equals("work_record")){//上下班记录
								backMesg=doUpload.workRecord(jsonData);
							}else if(service_name.equals("upload_liftrod")){//异常抬杆 2.11
								backMesg=doUpload.uploadLiftrod(jsonData);
							}else if(service_name.equals("upload_gate")){//通道信息 2.14
								backMesg=doUpload.uploadGate(jsonData);
							}else if(service_name.equals("upload_month_member")){//月卡会员 2.10
								backMesg = doUpload.uploadMonthMember(jsonData);
							}else if(service_name.equals("upload_month_card")){//月卡套餐 2.8
								backMesg=doUpload.uploadMonthCard(jsonData);
							}else if(service_name.equals("month_pay_record")){//月卡充值记录 2.13
								backMesg=doUpload.monthPayRecord(jsonData);
							}else if(service_name.equals("upload_car_type")){//车辆类型 2.15
								backMesg = doUpload.carTypeUpload(jsonData);
							}else if(service_name.equals("upload_blackuser")){//黑名单 2.15
								backMesg = doUpload.blackUpload(jsonData);
							}else if (service_name.equals("upload_order")) {//完整订单上传 2.3
								backMesg = doUpload.uploadOrder(jsonData);
							}else if(service_name.equals("upload_price")){//价格同步
								backMesg=doUpload.uploadPrice(parkId, data);
							}else if(service_name.equals("upload_ticket")){//停车券上传
								logger.error("未实现接口");
							}else if(service_name.equals("park_log")){//日志上传
								backMesg=doUpload.uploadLog(parkId, data);
							}else if(service_name.equals("upload_confirm_order")){
								doUpload.UploadConfirmOrder(parkId, data);//手动匹配订单上传2.17
							}
							//下行数据，异步返回，
							else if  (service_name.equals("month_member_sync")) {//月卡会员下发返回
								doUpload.monthMemberSync(jsonData);
							}else if (service_name.equals("car_type_sync")){//车型数据下发返回
								doUpload.carTypeSync(jsonData);
							}else if (service_name.equals("month_card_sync")){//月卡套餐下发返回
								doUpload.monthCardSync(jsonData);
							}else if (service_name.equals("blackuser_sync")){ //黑名单下发返回
								doUpload.blackuserSync(jsonData);
							}else if (service_name.equals("month_pay_sync")){//月卡续费记录下发返回
								doUpload.monthPaySync(jsonData);
							}else if(service_name.equals("query_prodprice")){//查询月卡价格返回
								doUpload.queryProdprice(jsonData);
							}else if(service_name.equals("lock_car")){//锁车业务
								doUpload.lockCar(jsonData);
							}else if(service_name.equals("collector_sync")){//收费员同步返回
								doUpload.collectSync(jsonData);
							}else if(service_name.equals("operate_liftrod")){//抬杆通知返回
								JSONObject jsonObj = JSONObject.parseObject(data);
								Integer state = jsonObj.getInteger("state");
								String channelId = jsonObj.getString("channel_id");
								Integer operate = jsonObj.getInteger("operate");
								doUpload.operateLiftrod(parkId, channelId,state,operate);
							}else if(service_name.equals("deliver_ticket")){//优惠卷下发
								doUpload.deliverTicket(parkId,data);
							}else if(service_name.equals("confirm_order_inform")){//匹配订单确认通知
								doUpload.confirmOrderInform(parkId,data);
							}else if(service_name.equals("gate_sync")){ //通道信息下发
								doUpload.gateSync(jsonData);
							}else if(service_name.equals("price_sync")){//价格同步
								doUpload.priceSync(jsonData);
							}/*else if(service_name.equals("completezero_order")){//零元结算订单
								doUpload.zeroOrderSync(jsonData);
							}*/
							logger.info("return message"+backMesg);
							doBackMessage(backMesg, ctx.channel());

							logger.error("需要同步的sdk："+syncParks);
							if(syncParks!=null&&!syncParks.isEmpty()){
								//logger.error(NettyChannelMap.map);
								for(String localId : syncParks){
									Channel channel = NettyChannelMap.get(parkId+"_"+localId);
									logger.error("================>>>>>>>同步消息到："+channel);
									//logger.error(channel+",data:"+data);
									JSONObject jsonObj = JSONObject.parseObject(data);
									jsonObj.put("service_name", service_name);
									doBackMessage(jsonObj.toString(), channel);
								}
							}
						}
					}
				}
			}});
	}

	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.error("连接断开，释放资源" + ctx.channel());
		DoUpload doUpload = NettyChannelMap.doUpload;
		logger.info("tcpHandle:" + doUpload);
		if (doUpload != null) {
			String parkId = NettyChannelMap.getParkByChannel(ctx.channel());
			if (parkId != null)
				doUpload.logout(parkId,ctx.channel().remoteAddress().toString());
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
		 * @param mesg 原始数据
		 */
	private void doBackMessage(String mesg, Channel channel) {
		if (channel != null && channel.isActive()&& channel.isWritable()) {
			try {
				//发送消息给客户端
//				if(mesg.length()>10)
//					logger.error("服务器处理返回:"+mesg+","+channel);
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
	 * @param data 原始数据
	 * @param key localid
	 */
	private void doLogin(String data, String sign,String key) {
		DoUpload doUpload = NettyChannelMap.doUpload;
		Channel clientChannel = NettyChannelMap.get(key);
		if (doUpload != null) {
			String token = doUpload.doLogin(data, sign,clientChannel.remoteAddress().toString());
			String result = "{\"state\":1,\"token\":\"" + token + "\","+"\"service_name\":\"login\"}";
			boolean isLongin = true;
			if (token != null && token.indexOf("error") != -1) {// 登录失败
				result = "{\"state\":0,\"errmsg\":\"" + token + "\","+"\"service_name\":\"login\"}";
				isLongin = false;
			}
			logger.error("返回登录消息" + result + ",channel:" + clientChannel);
			doBackMessage(result, clientChannel);
			if(!isLongin){
				try {
					logger.error("关闭链接>>>>>"+clientChannel.disconnect().isSuccess());
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		} else {
			logger.error("服务器初始化失败");
			String result = "{\"state\":0,\"errmsg\":\"服务器初始化异常\"}";
			doBackMessage(result, clientChannel);
			try {
				logger.error("关闭链接>>>>>"+clientChannel.disconnect().isSuccess());
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}


}
