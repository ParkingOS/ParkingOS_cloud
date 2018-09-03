package parkingos.com.bolink.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.google.common.util.concurrent.RateLimiter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.prometheus.client.Counter;
import org.apache.log4j.Logger;
import parkingos.com.bolink.service.DoUpload;
import parkingos.com.bolink.utlis.Check;
import parkingos.com.bolink.utlis.Defind;
import parkingos.com.bolink.utlis.ExecutorsUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
	//包请求次数限流器池，针对每个车场建立一个限流器，默认每个车场每秒中不超过10个请求，心跳除外，parkid+'login'说明是登陆限制
	static ConcurrentHashMap<String, RateLimiter> rateLimiterPool = new  ConcurrentHashMap<String, RateLimiter>();

	private static final Counter requestTotal = Counter.build()
			.name("parkingos_tcp_total")
			.labelNames("event", "tag")
			.help("parkingos_total").register();
	Logger logger = Logger.getLogger(SensorServerHandler.class);


	 @Override 
	 public void channelActive(ChannelHandlerContext ctx) throws Exception {
	 	 //连接累计数量
		 requestTotal.labels("connect","").inc();
		 logger.info("一个新的连接>>>>>>>>>>>"+ctx.channel().remoteAddress().toString());
		 ctx.fireChannelActive(); 
		 ctx.flush();
	 }
	 
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)	throws Exception {
		handleMessage(ctx,msg);
	}

	private void  handleMessage(final ChannelHandlerContext ctx,final Object msg){

//		logger.info("tcp 开始处理消息>>>>"+msg.toString()+">>>>>>"+ctx.channel().remoteAddress().toString());
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
						//心跳统计
						requestTotal.labels("msg","heartbeat").inc();
						//获取该通道的key parkid_localid
						String key = NettyChannelMap.getParkByChannel(ctx.channel());
						logger.info("===>>beat:"+key);
						if(key!=null&&!"".equals(key)){
							doBackMessage("0x12", ctx.channel());
							doBeat(ctx.channel());
							return;
						}
						return ;
					}
					JSONObject jsonMesg = JSON.parseObject(mesg, Feature.OrderedField);
					//限流器
					try{
						if (jsonMesg != null) {//如果不为空，
							String data = null;
							if (jsonMesg.containsKey("data")) {
								data = jsonMesg.getString("data");
								JSONObject jsonObj = JSONObject.parseObject(data);
								String parkId = jsonObj.getString("park_id");
                                if(!Check.isEmpty(parkId)){//只有有车场id才能作为key限流
                                    if (rateLimiterPool.containsKey(parkId)) {//这个车场是否设置过限流
                                        //如果流量达到限制，则先打印出车场来，先不阻拦
                                        if (!rateLimiterPool.get(parkId).tryAcquire()) {//后续的限流消息可以通过这里下发
                                            logger.info("rateLimiter  tryAcquire fail,parkId:"+parkId);
                                        }

                                    }
                                    else{
                                        //如果设置了限流速度
                                        String strRatelimit = Defind.getProperty("TCPRATELIMIT");
                                        if(strRatelimit != null){
                                            RateLimiter rateLimiter = RateLimiter.create(Double.valueOf(strRatelimit));
                                            rateLimiterPool.put(parkId,rateLimiter);
                                        }

                                    }
                                }


							}
						}
					}
					catch (Exception e){
							logger.error("rete limit error:",e);
					}

//					logger.info(jsonMesg);
					JSONObject result = new JSONObject();
					try {
						if(TempUtil.isReSend(jsonMesg)){
							//重复统计
							requestTotal.labels("msg","resend").inc();
							result.put("state",0);
							result.put("errmsg","数据已处理过...");
							result.put("service_name",jsonMesg.getString("service_name"));
							if(jsonMesg.containsKey("data")){
								JSONObject data = jsonMesg.getJSONObject("data");
								if(data.containsKey("order_id")) {
									String orderId = data.getString("order_id");
									result.put("order_id",orderId);
								}
								if(data.containsKey("car_number")) {
									String carNumber = data.getString("car_number");
									result.put("car_number",carNumber);
								}
							}
//							result.put("service_name",JSONObject.parseObject(jsonMesg.get("data")+"").get("service_name"));
//							result.put("car_number",JSONObject.parseObject(jsonMesg.get("data")+"").get("car_number"));
//							result.put("order_id",JSONObject.parseObject(jsonMesg.get("data")+"").get("order_id"));
							logger.error("发送数据给车场:"+result.toString());
							doBackMessage(result.toString(), ctx.channel());
							//logger.error(jsonMesg+",已处理过,不再处理...");
							return;
						}
					} catch (Exception e) {
						//数据格式错误
						requestTotal.labels("msg","errorFormat").inc();
						result.put("state",0);
						result.put("errmsg","数据格式错误!");
						doBackMessage(result.toString(), ctx.channel());
						logger.error(mesg+">>>>>>>"+e.getMessage());
						return;
					}

					if (jsonMesg != null) {
						String service_name = null;
						if (jsonMesg.containsKey("service_name")) {
							service_name = jsonMesg.getString("service_name");
						} else {
							String mess = "{\"state\":0,\"errmsg\":\"未识别到service_name标记\"}";
							doBackMessage(mess, ctx.channel());
							//未识别服务名
							requestTotal.labels("msg","no_service_name").inc();
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
							//未识签名
							requestTotal.labels("msg","no_sign").inc();
							logger.error("data error  ,error:未识别到sign标记");
							String mess = "{\"state\":0,\"errmsg\":\"未识别到sign标记\"}";
							doBackMessage(mess, ctx.channel());
							return;
						}

						if (service_name.equals("login")) {// 登录数据
							requestTotal.labels("msg","login").inc();
							JSONObject jsonObj = JSONObject.parseObject(data);
							String parkId = jsonObj.getString("park_id");
							if (Check.isEmpty(parkId)) {// 账户为空直接返回
								requestTotal.labels("msg","login_error").inc();
								logger.error("tcp park login ,error:未识别到park_id标记");
								String mess = "{\"state\":0,\"errmsg\":\"未识别到park_id标记\"}";
								doBackMessage(mess, ctx.channel());
								try {
									requestTotal.labels("msg","close_tcp").inc();
									logger.error("关闭链接>>>>>"+ctx.channel().disconnect().isSuccess());
								}catch (Exception e){
									e.printStackTrace();
								}
								return;
							}
							//对登陆接口进行限流，默认是不能超过10秒钟一次
                            try{
                                String ratelimitKey = parkId+"login";
                                if (rateLimiterPool.containsKey(ratelimitKey)) {//这个车场是否设置过限流
                                    //如果流量达到限制，则先打印出车场来，先不阻拦
                                    if (!rateLimiterPool.get(ratelimitKey).tryAcquire()) {//后续的限流消息可以通过这里下发
                                        logger.info("rateloginlimiter  tryAcquire fail,parkId:"+parkId);
                                    }

                                }
                                else{
                                    //如果设置了限流速度
                                    String strRatelimit = Defind.getProperty("TCPLOGINRATELIMIT");
                                    if(strRatelimit != null){
                                        RateLimiter rateLimiter = RateLimiter.create(Double.valueOf(strRatelimit));
                                        rateLimiterPool.put(ratelimitKey,rateLimiter);
                                    }
                                }
                            }
                            catch (Exception e){
                                logger.error("rete login limit error:",e);
                            }

							String localId = null;
//							String key = parkId;
							DoUpload doUpload = NettyChannelMap.doUpload;
							String key = doUpload.getComId(parkId);
							if("".equals(key)){//车场编号是字符型，并且不是手输泊链编号的
								requestTotal.labels("msg","login_error").inc();
								logger.error("data error  ,error:车场编号是字符类型");
								String mess = "{\"state\":0,\"errmsg\":\"车场不存在\"}";
								doBackMessage(mess, ctx.channel());
								return;
							}
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
								//统计token错误次数
								requestTotal.labels("msg","token_error").inc();
								logger.error("data error ,error:未识别到token标记");
								String mess = "{\"state\":0,\"errmsg\":\"未识别到token标记\"}";
								doBackMessage(mess, ctx.channel());
								//验证token
								return;
							}

							DoUpload doUpload = NettyChannelMap.doUpload;
							if (doUpload == null) {
								//统计服务初始化错误次数
								requestTotal.labels("msg","service_init_error").inc();
								logger.error("server error ,初始化失败....");
								String mess = "{\"state\":0,\"errmsg\":\"服务器初始化失败\"}";
								doBackMessage(mess, ctx.channel());
								return;
							}

							String parkId = doUpload.checkTokenSign(token, sign, data);
							if (parkId.indexOf("error") != -1) {//验证有错误

								requestTotal.labels("msg","check_token_error").inc();
								logger.error("server error ," + parkId);
								String mess = "{\"state\":0,\"errmsg\":\"" + parkId + "\"}";
								doBackMessage(mess, ctx.channel());
								return;
							}

							//token有效  判断是不是攻击  频率最高10s 1000次
							if(isHit(token)){
								//统计攻击次数
								requestTotal.labels("msg","is_attack").inc();
								logger.error("data error ,error:request_over_times");
								String mess = "{\"state\":0,\"errmsg\":\"request_over_times\"}";
								doBackMessage(mess, ctx.channel());
								return;
							}

							String backMesg = "";
							JSONObject jsonData = JSONObject.parseObject(data);
							if(jsonData!=null){
//								Long id = doUpload.getComId(parkId);
								jsonData.put("comid",parkId);
							}else {
								logger.error("data error:" + data);
								//统计数据错误
								requestTotal.labels("msg","data_error").inc();
								doBackMessage("{\"state\":0,\"errmsg\":\"data error\"}", ctx.channel());
								return ;
							}
							List<String> syncParks= null;
							//统计各个请求量
							requestTotal.labels("msg",service_name).inc();
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
								backMesg = doUpload.UploadConfirmOrder(parkId, data);//手动匹配订单上传2.17
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
							}else if(service_name.equals("visitor_sync")){
								doUpload.visitorSync(jsonData);
							}/*else if(service_name.equals("completezero_order")){//零元结算订单
								doUpload.zeroOrderSync(jsonData);
							}*/
							logger.error("return message"+backMesg);
							doBackMessage(backMesg, ctx.channel());

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

	private boolean isHit(String token) {

//		return true;
		logger.info("SensorServerHandler recive data，token:"+token);
		Map<String,List<Long>> tokenAccessMap =NettyChannelMap.tokenAccessMap;
		Long ntime = System.currentTimeMillis()/1000;
		if(tokenAccessMap.containsKey(token)){
			List<Long> requestTimes = tokenAccessMap.get(token);
			//logger.info("tcp recive data，request cache:"+requestTimes);
			if(requestTimes==null){
				requestTimes = new ArrayList<Long>();
				tokenAccessMap.put(token, requestTimes);
			}
			while (requestTimes.size()>100) {
				requestTimes.remove(0);
			}
			//logger.info("tcp recive data，request cache:"+requestTimes);
			tokenAccessMap.put(token, requestTimes);
			if(requestTimes.size()==100){
				Long preRequest = requestTimes.get(0);
				if(preRequest!=null&&ntime-preRequest<11){//10秒内100请求限制
					logger.error("tcp recive data，10秒内超过100次，返回:"+token);
					return true;
				}
			}
			requestTimes.add(ntime);
		}else {
			List<Long> requestTimes = new ArrayList<Long>();
			requestTimes.add(ntime);
			tokenAccessMap.put(token, requestTimes);
			//logger.info("tcp recive data，request cache:"+requestTimes);
		}
		//logger.info("tcp recive data，没有超过10秒100次限制");

		return false;
	}

	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		requestTotal.labels("disconnect","").inc();
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
//				logger.error("服务器处理返回:"+mesg+","+channel);
				byte[] req = ("\n" + mesg + "\r").getBytes("UTF-8");
				ByteBuf buf = Unpooled.buffer(req.length);
				buf.writeBytes(req);
				channel.writeAndFlush(buf);
				requestTotal.labels("msg","back_msg_succ").inc();
			} catch (UnsupportedEncodingException e) {
				requestTotal.labels("msg","back_msg_error").inc();
				e.printStackTrace();
				logger.error("返回消息到客户端出现异常");
			}
		}else{
			requestTotal.labels("msg","msg_client_down").inc();
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
					requestTotal.labels("disconnect","").inc();
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
				requestTotal.labels("disconnect","").inc();
				logger.error("关闭链接>>>>>"+clientChannel.disconnect().isSuccess());
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	public  void doBeat(Channel channel){
		DoUpload doUpload = NettyChannelMap.doUpload;
		if (doUpload != null) {
			doUpload.doBeat(channel.remoteAddress().toString());
			//doBackMessage("",channel);
		}
	}


}
