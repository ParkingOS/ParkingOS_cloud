package com.zld.easemob.main;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zld.easemob.apidemo.EasemobIMUsers;
import com.zld.utils.JsonUtil;
import com.zld.utils.StringUtils;


/**
 * Created by GT on 2015/9/6.
 */
public class HXHandle{
    private static final Logger LOGGER = Logger.getLogger(HXHandle.class);
    static String pwd = "7c3dd6115425702492509d98fb68a43b";

    public static void main(String[] args) {
        reg("hx21776");
//        reg("b");
//        reg("e");
        //addFriend("hx21783","hx21782");
//        sendMsg("e","test","");
//        String mesg = "我打中了您放飞的停车券，加您为好机友了~";
//        ObjectNode objectNode=EasemobIMUsers.sendMsg("hx21776", "test", mesg);
//        String statusCode = JsonUtil.getJsonValue(objectNode.toString(), "statusCode");
//    	System.err.println(statusCode);
        /*try {
        	regBach();
		} catch (Exception e) {
			// TODO: handle exception
		}*/
    }

    public static void reg(String name){
        reg(name,pwd);
    }

    public static boolean reg(String name, String password){
		ObjectNode ret = EasemobIMUsers.getIMUsersByUserName(name);
		//{"action":"get","path":"/users","uri":"http://a1.easemob.com/zld2015/imtest/users/hx19614","entities":[{"uuid":"50e1bcea-6656-11e5-a3a2-59db26f8f36b","type":"user","created":1443495661486,"modified":1443495661486,"username":"hx19614","activated":true}],"timestamp":1443507975861,"duration":2,"count":1,"statusCode":200}
		String statusCode = JsonUtil.getJsonValue(ret.toString(), "statusCode");
		System.err.println("查环信账户："+ret);
		if(statusCode.equals("200")){//已经注册过。
			System.out.println(name+">>>>>已经注册过.....");
			return true;
		}
        ObjectNode datanode = JsonNodeFactory.instance.objectNode();
        datanode.put("username",name);
        datanode.put("password", password);
        datanode.put("nickname", "停车宝车主");
        ObjectNode createNewIMUserSingleNode = EasemobIMUsers.createNewIMUserSingle(datanode);
        if (null != createNewIMUserSingleNode) {
        	LOGGER.info( createNewIMUserSingleNode.toString());
        	statusCode = JsonUtil.getJsonValue(createNewIMUserSingleNode.toString(), "statusCode");
        	//System.err.println(createNewIMUserSingleNode.toString());
        	LOGGER.error(statusCode);
        	if(statusCode.equals("200")){
        		System.err.println(name+">>>>>注册成功....");
        		return true;
        	}
        }
        return false;
    }

    public static boolean addFriend(String owner, String friend){
        ObjectNode addFriendSingleNode = EasemobIMUsers.addFriendSingle(owner, friend);
        if (null != addFriendSingleNode) {
        	LOGGER.info( addFriendSingleNode.toString());
        	String statusCode = JsonUtil.getJsonValue(addFriendSingleNode.toString(), "statusCode");
        	System.err.println(statusCode);
        	if(statusCode.equals("200"))
        		return true;
        }
        return false;
    }

    public static boolean sendMsg(String owner,String friend,String msg){
        ObjectNode sendMsgNode = EasemobIMUsers.sendMsg(owner, friend, msg);
        if (null != sendMsgNode) {
        	LOGGER.info( sendMsgNode.toString());
        	String statusCode = JsonUtil.getJsonValue(sendMsgNode.toString(), "statusCode");
        	System.err.println(statusCode);
        	if(statusCode.equals("200"))
        		return true;
        }
        return false;
    }
    
    public static void regBach() throws Exception{
    	JsonNodeFactory factory = new JsonNodeFactory(false);
		//批量注册的机友
		ArrayNode arrayNode = new ArrayNode(factory);
		//加好友列表
		Long uin =21776L;
		List<String> addFriends = new ArrayList<String>();
		List<Long> regUinList = new ArrayList<Long>();
		regUinList.add(21785L);
		regUinList.add(21783L);
		regUinList.add(21782L);
		for(int i=0;i<regUinList.size();i++){
			Long _uin =regUinList.get(i);
			ObjectNode jsonNode = factory.objectNode();
			jsonNode.put("username", "hx"+_uin);
			jsonNode.put("password", StringUtils.MD5(_uin+System.currentTimeMillis()+"zldhxsys"));
			arrayNode.add(jsonNode);
			addFriends.add("hx"+_uin);
		}
		//批量注册机友
		ObjectNode objectNode = EasemobIMUsers.createNewIMUserBatch(arrayNode);
		if(null!=objectNode){
			System.err.println(objectNode);
			String statusCode = JsonUtil.getJsonValue(objectNode.toString(), "statusCode");
			LOGGER.error("批量注册好友成功，"+statusCode);
		}
		//循环加入好友
		for(String f : addFriends){
			boolean ret  = addFriend("hx"+uin, f);
			System.err.println("加好友成功,结果："+ret);
			LOGGER.error("，加好友成功，好友："+f+","+objectNode.toString());
		}
		//int r = daService.update("update user_friend_tb set is_add_hx=? where  buin=? or euin=? ", new Object[]{uin,uin,1});
		//LOGGER.error(">>>>>>更新zld好友表，ret:"+r);
    }

}
