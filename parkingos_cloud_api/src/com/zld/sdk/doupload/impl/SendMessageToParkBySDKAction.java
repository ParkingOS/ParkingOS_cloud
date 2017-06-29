package com.zld.sdk.doupload.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.sdk.tcp.NettyChannelMap;
import com.zld.service.DataBaseService;
import com.zld.utils.Check;
import com.zld.utils.StringUtils;

public class SendMessageToParkBySDKAction extends Action {
	private Logger logger = Logger.getLogger(SendMessageToParkBySDKAction.class);
	@Autowired
	DataBaseService daService ;
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		//轮询触发下传数据接口，有数据需要同步，查询出对应的数据将数据下发到收费系统
		//查询需要同步的数据
		List list = new ArrayList();
		list.add(0);
		List dataNeedSyncList = daService.getAllMap("select * from sync_info_pool_tb where state=? ", list);
		if(dataNeedSyncList != null && dataNeedSyncList.size()>0){
			for(int i=0; i<dataNeedSyncList.size(); i++){
				Map mapNeedSync = (Map) dataNeedSyncList.get(i);
				if(mapNeedSync != null && !mapNeedSync.isEmpty()){
					String tableName = String.valueOf(mapNeedSync.get("table_name"));
					Long tableId = Long.valueOf(String.valueOf(mapNeedSync.get("table_id")));
					Long comid = Long.valueOf(String.valueOf(mapNeedSync.get("comid")));
					Integer operate = Integer.valueOf(String.valueOf(mapNeedSync.get("operate")));
					if(tableName.equals("carower_product")){
						String result = sendcardMember(tableName,tableId,comid,operate);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送月卡会员信息结果"+result);
					}else if(tableName.equals("product_package_tb")){
						String result = sendcardPackage(tableName,tableId,comid,operate);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送月卡套餐信息结果"+result);
					}else if(tableName.equals("price_tb")){
						String result = sendPricInfo(tableName,tableId,comid,operate);
						logger.error(">>>>>>>>>>>>>>>>>>>>>发送价格信息结果"+result);
					}
				}
			}
		}else{
			StringUtils.ajaxOutput(response, "没有需要同步的数据");
		}
		return null;
	}
	
	private String sendPricInfo(String tableName, Long tableId, Long comid,
			Integer operate) {
		String result="0";
		//获取下传数据的通道信息
		Channel channel = NettyChannelMap.get(getChannel(String.valueOf(comid)));
		//定义封装下传信息的json对象
		JSONObject jsonObj = null;
		//操作类型
		if(operate == 0){
			operate = 1;
		}else if(operate == 1){
			operate = 2;
		}else if(operate == 2){
			operate = 3;
		}
		//查询出对应的需要下传的数据
		Map mapNeed = daService.getMap("select * from "+tableName+" where id=? and comid=?", new Object[]{tableId,comid});
		if(mapNeed != null && !mapNeed.isEmpty()){
			//暂时定义为按照数据库进行数据下传
			String mapJSONStr = StringUtils.createJson(mapNeed);
			jsonObj = new JSONObject().fromObject(mapJSONStr);
			/*jsonObj.put("card_id", String.valueOf(mapNeed.get("card_id")));
			Long createTime = -1L;
			if(String.valueOf(mapNeed.get("create_time"))!=null && !(String.valueOf(mapNeed.get("create_time"))).equals("null")){
				createTime = Long.valueOf(String.valueOf(mapNeed.get("update_time")));
			}
			jsonObj.put("create_time", createTime);
			Long updateTime = -1L;
			if(String.valueOf(mapNeed.get("update_time"))!=null && !(String.valueOf(mapNeed.get("update_time"))).equals("null")){
				updateTime = Long.valueOf(String.valueOf(mapNeed.get("update_time")));
			}
			jsonObj.put("update_time", updateTime);
			jsonObj.put("describe",String.valueOf(mapNeed.get("describe")));
			jsonObj.put("name", String.valueOf(mapNeed.get("p_name")));*/
			jsonObj.put("operate_type", operate);
		}else{
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息");
			return result;
		}
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "price_sync");
		jsonMesg.put("data", jsonObj.toString());
		boolean isSend = doBackMessage(jsonMesg.toString(), channel);
		logger.error(">>>>>>>>>>>>>>云端发送数据到停车收费系统结果："+isSend);
		if(isSend){
			result = "1";
		}
		return result;
	}

	/**
	 * 发送月卡套餐信息到停车收费系统
	 * @param tableName
	 * @param tableId
	 * @param comid
	 * @param operate
	 * @return
	 */
	private String sendcardPackage(String tableName, Long tableId, Long comid,
			Integer operate) {
		String result="0";
		//获取下传数据的通道信息
		Channel channel = NettyChannelMap.get(getChannel(String.valueOf(comid)));
		//定义封装下传信息的json对象
		JSONObject jsonObj = null;
		//操作类型
		if(operate == 0){
			operate = 1;
		}else if(operate == 1){
			operate = 2;
		}else if(operate == 2){
			operate = 3;
		}
		//查询出对应的需要下传的数据
		Map mapNeed = daService.getMap("select * from "+tableName+" where id=? and comid=?", new Object[]{tableId,comid});
		if(mapNeed != null && !mapNeed.isEmpty()){
			//暂时定义为按照数据库字段对数据进行下传操作
			String mapNeedStr = StringUtils.createJson(mapNeed);
			jsonObj = new JSONObject().fromObject(mapNeedStr);
			
			/*jsonObj.put("card_id", String.valueOf(mapNeed.get("card_id")));
			Long createTime = -1L;
			if(String.valueOf(mapNeed.get("create_time"))!=null && !(String.valueOf(mapNeed.get("create_time"))).equals("null")){
				createTime = Long.valueOf(String.valueOf(mapNeed.get("update_time")));
			}
			jsonObj.put("create_time", createTime);
			Long updateTime = -1L;
			if(String.valueOf(mapNeed.get("update_time"))!=null && !(String.valueOf(mapNeed.get("update_time"))).equals("null")){
				updateTime = Long.valueOf(String.valueOf(mapNeed.get("update_time")));
			}
			jsonObj.put("update_time", updateTime);
			jsonObj.put("describe",String.valueOf(mapNeed.get("describe")));
			jsonObj.put("name", String.valueOf(mapNeed.get("p_name")));*/
			jsonObj.put("operate_type", operate);
		}else{
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息");
			return result;
		}
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "month_card_sync");
		jsonMesg.put("data", jsonObj.toString());
		boolean isSend = doBackMessage(jsonMesg.toString(), channel);
		logger.error(">>>>>>>>>>>>>>云端发送数据到停车收费系统结果："+isSend);
		if(isSend){
			result = "1";
		}
		return result;
	}

	/**
	 * 将云后台修改的月卡会员的信息发送到车场收费系统
	 * @param tableName
	 * @param tableId
	 * @param comid
	 * @param operate
	 * @return
	 */
	private String sendcardMember(String tableName, Long tableId, Long comid,
			Integer operate) {
		String result="0";
		//获取下传数据的通道信息
		Channel channel = NettyChannelMap.get(getChannel(String.valueOf(comid)));
		//定义封装下传信息的json对象
		JSONObject jsonObj = new JSONObject();
		//操作类型
		if(operate == 0){
			operate = 1;
		}else if(operate == 1){
			operate = 2;
		}else if(operate == 2){
			operate = 3;
		}
		//查询出对应的需要下传的数据
		Map mapNeed = daService.getMap("select * from "+tableName+" where id=? and com_id=?", new Object[]{tableId,comid});
		if(mapNeed != null && !mapNeed.isEmpty()){
			logger.error(">>>>>>>查询需要同步的月卡会员信息："+mapNeed.toString());
			Long beginTime = -1L;
			if(String.valueOf(mapNeed.get("b_time"))!=null && !(String.valueOf(mapNeed.get("b_time"))).equals("null")){
				beginTime = Long.valueOf(String.valueOf(mapNeed.get("b_time")));
			}
			Long endTime = -1L;
			if(String.valueOf(mapNeed.get("e_time"))!=null && !(String.valueOf(mapNeed.get("e_time"))).equals("null")){
				endTime = Long.valueOf(String.valueOf(mapNeed.get("e_time")));
			}
			//修改下传数据的内容，适配文档需求，将表中的数据都下传至收费系统
			String mapNeedStr = StringUtils.createJson(mapNeed);
			jsonObj = new JSONObject().fromObject(mapNeedStr);
			jsonObj.put("begin_time", beginTime);
			jsonObj.put("end_time", endTime);
			jsonObj.put("price", String.valueOf(mapNeed.get("act_total")));
			jsonObj.put("operate_type", operate);
			logger.error(">>>>>>>>>>>>>>>>>>>>>>>>>>传输的数据内容为："+jsonObj.toString());
		}else{
			logger.error(">>>>>>>>>>>>>没查到对应的需要下传的信息，可能是删除操作");
			return result;
		}
		JSONObject jsonMesg = new JSONObject();
		jsonMesg.put("service_name", "month_member_sync");
		jsonMesg.put("data", jsonObj.toString());
		boolean isSend = doBackMessage(jsonMesg.toString(), channel);
		logger.error(">>>>>>>>>>>>>>>>>>>>同步月卡会员数据到收费系统："+jsonMesg.toString());
		logger.error(">>>>>>>>>>>>>>云端发送数据到停车收费系统结果："+isSend);
		if(isSend){
			result = "1";
		}
		return result;
	}

	/**
	 * 下传消息到收费系统的方法
	 * @param mesg
	 * @param data
	 */
	private boolean doBackMessage(String mesg, Channel channel) {
		if (channel != null && channel.isActive()
				&& channel.isWritable()) {
			try {
				logger.error("发消息到SDK，channel:"+channel+",mesg:" + mesg);
				byte[] req= ("\n" + mesg + "\r").getBytes("utf-8");
				ByteBuf buf = Unpooled.buffer(req.length);
				buf.writeBytes(req);
				ChannelFuture future = channel.writeAndFlush(buf);
				return true;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else{
			logger.error("客户端已断开连接...");
		}
		return false;
	}
	
	/**
	 * 获取下发数据的TCP通道
	 * @param comid
	 * @return
	 */
	private String getChannel(String comid){
		String channelPass = "";
		Map parkMap = daService.getMap("select * from park_token_tb where park_id=?", new Object[]{comid});
		if(parkMap != null && !parkMap.isEmpty()){
			String localId = String.valueOf(parkMap.get("local_id"));
			if(!Check.isEmpty(localId)){
				channelPass += "_"+localId;
			}
		}else{
			channelPass = comid;
		}
		return channelPass;
	}
}
