package com.zld.struts.request;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pay.Constants;

import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.weixinpay.utils.util.Sha1Util;
import com.zld.wxpublic.response.Article;
import com.zld.wxpublic.response.BaseMessage;
import com.zld.wxpublic.response.NewsMessage;
import com.zld.wxpublic.response.TextMessage;
import com.zld.wxpublic.util.CommonUtil;
import com.zld.wxpublic.util.MessageUtil;

public class WeixinPublicServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	DataBaseService dataBaseService =null;
	
	PgOnlyReadService pgOnlyReadService = null;
	
	PublicMethods publicMethods = null;
	
	String TOKEN = "zhenlaidian";
	
	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 微信加密签名
        String signature = request.getParameter("signature");
        // 随机字符串
        String echostr = request.getParameter("echostr");
        // 时间戳
        String timestamp = request.getParameter("timestamp");
        // 随机数
        String nonce = request.getParameter("nonce");
        String[] str = { TOKEN, timestamp, nonce };
        Arrays.sort(str); // 字典序排序
        String bigStr = str[0] + str[1] + str[2];
        // SHA1加密
        String digest = Sha1Util.getSha1(bigStr);
        // 确认请求来至微信
        if (digest.equals(signature)) {
            response.getWriter().print(echostr);
            System.out.println("请求成功。。。");
        }
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 将请求、响应的编码均设置为UTF-8（防止中文乱码）  
        request.setCharacterEncoding("UTF-8");  
        response.setCharacterEncoding("UTF-8");  
  
        // 调用核心业务类接收消息、处理消息  
        String respMessage = processRequest(request);  
          
        // 响应消息  
        PrintWriter out = response.getWriter();  
        out.print(respMessage);  
        out.close(); 
	}
	
	/** 
     * 处理微信发来的请求 
     *  
     * @param request 
     * @return 
     */  
    private String processRequest(HttpServletRequest request) {  
        String respMessage = null;  
        try {  
            // 默认返回的文本消息内容  
            String respContent = "";  
  
            // xml请求解析  
            Map<String, String> requestMap = MessageUtil.parseXml(request);  
  
            // 发送方帐号（open_id）  
            String fromUserName = requestMap.get("FromUserName");  
            // 公众帐号  
            String toUserName = requestMap.get("ToUserName");  
            // 消息类型  
            String msgType = requestMap.get("MsgType");  
  
            // 回复文本消息  
            TextMessage textMessage = new TextMessage();  
            textMessage.setToUserName(fromUserName);  
            textMessage.setFromUserName(toUserName);  
            textMessage.setCreateTime(new Date().getTime());  
            textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);  
            
            Map<String, Object> userMap = pgOnlyReadService.getMap(
					"select * from user_info_tb where wxp_openid=? and state=? ",
					new Object[] { fromUserName, 0 });
            String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXPUBLIC_APPID+"&redirect_uri=http%3a%2f%2f"+Constants.WXPUBLIC_REDIRECTURL+"%2fzld%2fwxpaccount.do&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
            // 文本消息  
            if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {  
                String content = requestMap.get("Content");
                if(content.contains("奖")){
                	StringBuffer buffer = new StringBuffer();
                	buffer.append("4.20日起奖金发放规则调整如下：").append("\n");
                	buffer.append("一等奖二等奖不再由排名决定，而是通过收费员表现决定 ，由停车宝评选发放给谁。").append("\n");
                	buffer.append("评选主要依靠车主反馈，收费员服务态度，在线支付数等，每周微信公众号会发放获名单。").append("\n\n");
                	buffer.append("4.20日起积分规则调整如下：").append("\n");
                	buffer.append("非在线支付订单积分改为每笔0.01分。").append("\n");
                	buffer.append("在线支付超过一元积分改为每笔2分。").append("\n\n");
                	buffer.append("收费员推荐车主奖励已经调整如下：").append("\n");
                	buffer.append("推荐的车主完成一元在线支才才发放5元推荐奖励。");
					textMessage.setContent(buffer.toString());
					respMessage = MessageUtil.textMessageToXml(textMessage);
					return respMessage;
                }
                if(content.contains("调整详情")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "收费员规则调整详情【7.13日】");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3FmEYU4ibrwQicTnnFTIudHZhY5aN7CjIw6GDbIcaLSja8PWPicrP3H9dCqxYCZNibK2jutH4mq1ianhlw/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208349486&idx=1&sn=a59b4c5250cc4ca34941750d1bffc925#rd");
            		map.put("descp", "收费员规则调整详情。");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
                    return respMessage;
                }
                if(content.contains("游戏")){
                	StringBuffer buffer = new StringBuffer();
                	if(userMap == null){
                		buffer.append("<a href=\""+url+"\">点击注册</a>");
                	}else{
                		String url_game = "http://"+Constants.WXPUBLIC_REDIRECTURL+"/zld/cargame.do?action=playagin&uin="+userMap.get("id");
                		buffer.append("<a href=\""+url_game+"\">点击进入游戏</a>");
                	}
                	textMessage.setContent(buffer.toString());
					respMessage = MessageUtil.textMessageToXml(textMessage);
					return respMessage;
                }
                
                if(content.contains("解除绑定")){
                	/*StringBuffer buffer = new StringBuffer();
					int r = dataBaseService
							.update("update user_info_tb set wxp_openid=null where wxp_openid=? ",
									new Object[] { fromUserName });
					buffer.append("您好，绑定关系已经解除");
					textMessage.setContent(buffer.toString());
					respMessage = MessageUtil.textMessageToXml(textMessage);
					return respMessage;*/
                }
                
                if(content.contains("更多")){
                	StringBuffer buffer = new StringBuffer();
                	buffer.append("回复【车友会】，参加 VIP·车友会").append("\n\n");
                	buffer.append("回复【速通卡】，了解速通卡详情").append("\n\n");
                	buffer.append("回复【打灰机】，用停车券赚余额").append("\n\n");
                	buffer.append("回复【下载】，下载停车宝APP");
                	textMessage.setContent(buffer.toString());
					respMessage = MessageUtil.textMessageToXml(textMessage);
					return respMessage;
                }
                
                if(content.contains("车友会")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "有趣、好玩、享优惠！——停车宝车友会，等待您的加入！");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3Fr5wJQc0VltxVc4St3dPIXWn7ect1hXNKUgRoCns0TSyZWmRPhhEuYsZ5faY5ZuhwVPibzurL8LgA/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=207160377&idx=1&sn=b987bf7393c5b029289ff7ac5111d34c#rd");
            		map.put("descp", "“VIP车友会是神马？”停车宝车友会，是停车宝车主用户之间的交流平台，也是停车宝优惠活动的信息发布平台。");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
                    return respMessage;
                }
                
                if(content.contains("速通卡")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "“速通卡”用户，点我了解更多~");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3Fr5wJQc0VltxVc4St3dPIXWKT0qzLlU14wy2gibJ2NBNbze27vhgiaE9XGQIFK9BR1VBNurZrH9IAQ/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=207347884&idx=1&sn=22adb0b358047eb1e9c14c0c1eeb36b9#rd");
            		map.put("descp", "1速通卡是神马东东？速通卡，是在使用NFC手机刷卡的停车场，为VIP车主提供的储值会员卡；2速通卡有什么用？");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.contains("代客泊车")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "点我了解代客泊车~");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3Fr5wJQc0VltxVc4St3dPIXiaNyYM1mojicQ390U9DIMJohHCUicylZicXs6QMy6ch5eaIysXuFnJ2icdw/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=207349012&idx=1&sn=1bb40999f213b15a1a54008dcab89b31#rd");
            		map.put("descp", "1“代客泊车”，是神马东东？“代客泊车”，一般常见于高级酒店、餐饮、会所等地方，是这些场所为VIP用户等特定");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.contains("下载")){
                	StringBuffer buffer = new StringBuffer();
                	url = "http://a.app.qq.com/o/simple.jsp?pkgname=com.tq.zld";
                	buffer.append("<a href=\""+url+"\">点击下载App</a>");
                	textMessage.setContent(buffer.toString());
					respMessage = MessageUtil.textMessageToXml(textMessage);
					return respMessage;
                }
                
                if(content.contains("刷单")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "7.17日刷单通告");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3GjAv77GPICy1kDO4kRuo6EUFCNqB3mocta2EViaziaSTjJAgia9sEbpryTAS7nproBZHIvuM0QlHwVQ/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208451367&idx=1&sn=a89626c3c1c19fffce3b5c67d032e21e#rd");
            		map.put("descp", "7月16日开始，有极少部分车场，又开始严重刷单。上一次，停车宝因为防范刷单机制相当不成熟。因为部分车场的刷单");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.contains("积分")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "7月下旬上线：打赏积分与积分榜&车场专用券");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3GjAv77GPICy1kDO4kRuo6ERiaySfAhWYz2sSEmKSglfWysDhzq9LJPeFoJcw5ovhicvcnf5bjC7icKQ/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208445618&idx=1&sn=b4d99d5233921ae53c847165c62dec2b#rd");
            		map.put("descp", "积分的用途与来源：1，积分的用途：        积分可以用来兑换车场专用券，发给车主。 2，积分的来源：");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.contains("优秀")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "优秀收费员");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3ErwxRu6Nic9klguqWd4YkMzo0ibC0ewM7S0MRDPjWLaeqq3hztp4lkQ1Q0Hv1uqkDTxjlODsgfYOBw/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208550167&idx=1&sn=efa9c7fbafc28635324946590380e2ae#rd");
            		map.put("descp", "优秀的人，理应得到更多。。。。");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("认证")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "一大波福利即将到来，停车宝邀您完成认证");
            		map.put("picurl", "http://mmbiz.qpic.cn/mmbiz/zg069SDrV3EyRX6J6icAOptJ08OZj1GkibEXKXbWAOy83OibYQsibNOYMfib2icVUuUJSHHC95XicTGn2L5e1pU2Z1Jxw/640?wx_fmt=jpeg&tp=webp&wxfrom=5");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208679773&idx=1&sn=43d1fe06680c90efb11444f8b72bdff2#rd");
            		map.put("descp", "一大波福利即将到来，停车宝邀您完成认证。");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("停车券") || content.equals("停车卷")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "停车宝停车券使用说明。");
            		map.put("picurl", "http://mmbiz.qpic.cn/mmbiz/zg069SDrV3EyRX6J6icAOptJ08OZj1GkibB79AmfDuWicE1vu3icU5hOyQ3yGV9RWcsrrqob4B1YFeJdSGK2sXk7VA/640?wx_fmt=jpeg&tp=webp&wxfrom=5");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208427587&idx=1&sn=6cec3794e585e4d31b5079f919b01614#rd");
            		map.put("descp", "停车券使用说明。");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("信用额度")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "停车宝信用额度说明。");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3EyRX6J6icAOptJ08OZj1Gkib8xgyTgvzjbSJ9t24rmoSicxDUOvO7wIBbZwXmhyhWz1Q4oK9jTvxXjQ/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208427120&idx=1&sn=6cb6719bf1520ef5a72097fe5c7fe56a#rd");
            		map.put("descp", "信用额度：信用额度仅限有有车牌的，计时订单使用。");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("打灰机") || content.equals("打飞机")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "停车宝邀您打灰机，用券赚余额");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3FautOBMESjlIias5CGLnj3R8LakMXaFibcKIR1k7FRGrCJaazVDWuzrnHfbgG1BbyHlPVrRFNVmh2A/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209080514&idx=1&sn=a0951330910bf2c4e41fca840a562d7e&ptlang=2052&ADUIN=2285180450&ADSESSION=1439946886&ADTAG=CLIENT.QQ.5425_.0&ADPUBNO=26509#rd");
            		map.put("descp", "打中谁的灰机，就能加谁为好机友。。。 更有余额，大额停车券任你打。。。");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("第二关")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "资格卡在第二关上线后可用。。。");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3HUvy5xt9VREHgyNTVQYTuc1LRxmxU3vibrQVYTw2cO4NfkXKMLH5pWmVYJvXErZk5zTNfDxSEBIew/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209280360&idx=1&sn=c84f017607c19f6bbb0870ca7706fee2#rd");
            		map.put("descp", "打中的资格卡永不过期，第二关上线后可使用。 ps:第二关还在策划中。。。。求高手指点。。。。");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("退款") || content.equals("提现")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "关于车主退还余额的说明");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3HZL7EuRDahAt6DUjWnlCBD7nQ9aFPkichorcoRTZEVDStZEjibiczCvcSO7MfZRQTibQyhR14LQPyh5Q/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209376960&idx=1&sn=369c4bea18d70d656c4f3e30b86cc843#rd");
            		map.put("descp", "关于车主退还余额的说明");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("非购买停车券")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "非购买停车券规则调整");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3GL94m1emzoHNA2gibgRAgNKJRURHLkbrZwaoRITwzRW5yHfx3WQt6Yz02uyyphMkSuTU2DbbpHpicA/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209488541&idx=1&sn=1b21a9f433bf0f7cc873f742e799b08b#rd");
            		map.put("descp", "近期，大量车主通过打灰机获得12元券后，配合车场刷大额停车券。6元以下的停车费，却故意支付13元，以使用12元的停车券。所以停车券使用规则将调整。");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("规则调整")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "停车宝规则全面调整");
            		map.put("picurl", "http://mmbiz.qpic.cn/mmbiz/zg069SDrV3EP4Vk0FK42CT3MtNaWhSPpJJh0Es7ibrW2urAJiaGDrkaaNmILlGLJgkkBhdAiarGFbfKY4gB2zahHw/640?wx_fmt=jpeg&tp=webp&wxfrom=5");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209615004&idx=1&sn=f94fd688b80e71f944efe61b60f7700b#rd");
            		map.put("descp", "由于O2O补贴模式并不被投资人和市场特别看好。 所以，停车宝规则全面调整。");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("打赏规则")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "打赏规则调整");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3HwfpcpDUGD5OjXicxXJbaDdUtRWeKzCvJTsiaJWf3mjyrumlH4LqxJTOa0HegXujCcvEEwQib2q9Giaw/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209684836&idx=1&sn=99540a278fccd17750f5591a1330c443#rd");
            		map.put("descp", "暂时调整打赏规则");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("过冬")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "停车宝进入过冬模式");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3H9qRCAn04s14RMRB7jzHwQ4H73qg08B3FXtanjjpr16Q9VsicVgKDuPVpgLQesgln37jxtPpDorJw/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209755973&idx=1&sn=e28c267e19e14c2c099e79b1c848abaa#rd");
            		map.put("descp", "寒冬已至，聪明的物种，应该学会储存能量，苦练内功，静待暖春。。。。。。");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("十月预告")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "国庆快乐&停车宝10月新功能预告");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3HHpYTzklnRygibjxPHxmRy8VY3679ekRibtdsyibsOLPcNbgjvvHy54RruxH4valO6kQWOKtks8Frdw/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209772715&idx=1&sn=25ff40c14225fcf23e754185c2c242e4#rd");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("补贴分配")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "补贴分配规则调整");
            		map.put("picurl", "http://mmbiz.qpic.cn/mmbiz/zg069SDrV3GMhiaw92auib7JeSmwuX5qLia253foPYUMBCOMI4qVpx4G3JA769oiaNS5Nz72icCtSClZsnNCenDnozQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209867877&idx=1&sn=ad453eaddeeafe30a18188ddcca9f6bd#rd");
            		map.put("descp", "不再所有车场抢5000补贴名额， 而是根据车场需求更合理分配。");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.contains("联系客服")){
                	System.out.println("to kefu openid:"+fromUserName);
                	BaseMessage kefuMessage = new BaseMessage();  
                	kefuMessage.setToUserName(fromUserName);  
                	kefuMessage.setFromUserName(toUserName);  
                	kefuMessage.setCreateTime(new Date().getTime());  
                	kefuMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_KEFU); 
                	
                	respMessage = MessageUtil.baseMessageToXml(kefuMessage);
					return respMessage;
                }
                
                if(content.contains("找回车牌") || content.contains("验证码") || content.contains("停车费")){
                	System.out.println("to kefu openid:"+fromUserName);
            		
            		boolean online = true;
        			String access_token = publicMethods.getWXPAccessToken();
        			String urlString = "https://api.weixin.qq.com/cgi-bin/customservice/getonlinekflist?access_token="+access_token;
        			String result = CommonUtil.httpsRequest(urlString, "GET", null);
        			JSONObject jsonObject = JSONObject.fromObject(result);
        			if(jsonObject != null && jsonObject.get("errcode") == null){
        				String kefulist = jsonObject.getString("kf_online_list");
        				JSONArray jsonArray = JSONArray.fromObject(kefulist);
        				if(jsonArray.size() > 0){
        					List<Object> stateList = new ArrayList<Object>();
        					for(int i=0; i<jsonArray.size();i++){
        						JSONObject jObject = jsonArray.getJSONObject(i);
        						int state = jObject.getInt("status");
        						if(state == 2){
        							stateList.add(state);
        						}
        					}
        					if(stateList.size() == jsonArray.size()){
        						online = false;
        					}
        				}else{
        					online = false;
        				}
        			}
            		if(online){
            			BaseMessage kefuMessage = new BaseMessage();  
                    	kefuMessage.setToUserName(fromUserName);  
                    	kefuMessage.setFromUserName(toUserName);  
                    	kefuMessage.setCreateTime(new Date().getTime());  
                    	kefuMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_KEFU); 
                    	
                    	respMessage = MessageUtil.baseMessageToXml(kefuMessage);
            		}else{
            			textMessage.setContent("抱歉，客服不在线，在线时间：工作日9:30-17:30，请您稍后咨询，谢谢！");
            			respMessage = MessageUtil.textMessageToXml(textMessage);
            		}
                	
					return respMessage;
                }
            }  
            // 图片消息  
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_IMAGE)) {  
//                respContent = "您发送的是图片消息！";  
            }  
            // 地理位置消息  
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {  
            	StringBuffer buffer = new StringBuffer();
            	String location_X = requestMap.get("Location_X");
            	String location_Y = requestMap.get("Location_Y");
            	String label = requestMap.get("Label");
//            	respContent = buffer.toString();
            }  
            // 链接消息  
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LINK)) {  
//                respContent = "您发送的是链接消息！";  
            }  
            // 音频消息  
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {  
//                respContent = "您发送的是音频消息！";  
            }  
            // 事件推送  
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {  
                // 事件类型  
                String eventType = requestMap.get("Event");
                System.out.println("wxpublic event >>>openid:"+fromUserName+",eventType:"+eventType);
                // 订阅  
                if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {  
                	StringBuffer buffer = new StringBuffer();
                	if(userMap == null){
                		buffer.append("<a href=\""+url+"\">点击注册</a>，立即领取10元礼包");
                	}else{
                		buffer.append("一大波认证用户专享福利即将来袭，回复“认证”了解详情。").append("\n\n");
                		buffer.append("回复“停车券”了解停车券使用详情").append("\n\n");
                		buffer.append("回复“游戏”，游戏翻倍小额停车券。").append("\n\n");
                		buffer.append("回复“更多”，了解更多。");
                	}
                	respContent = buffer.toString();  
                }  
                // 取消订阅  
                else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {  
                    // TODO 取消订阅后用户再收不到公众号发送的消息，因此不需要回复消息  
                }  
                // 自定义菜单点击事件  
                else if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) {  
                	String key = requestMap.get("EventKey");
                	if(key.equals("aboutus")){
                		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
                		Map<String, String> map = new HashMap<String, String>();
                		map.put("title", "让每位车主,享受自由停靠");
                		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3GyKvMNS7wzicsHafr1u1JzSvkxXpR9K3pldhT3GYDpmcicN2AAeYBEJcTxpQxXBd9nBE1h4s7a44AQ/0?wx_fmt=jpeg");
                		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=205960173&idx=1&sn=3750c6db17c115041a77eef72988a53a#rd");
                		list.add(map);
                		Map<String, String> map1 = new HashMap<String, String>();
                		map1.put("title", "需要帮助,点这里");
                		map1.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3GyKvMNS7wzicsHafr1u1JzS3Rt0iaQ3hH1N82apWOZdlic3KWicZpXicvwuse8MDew1dzHZ9Asa8mNibWw/0?wx_fmt=jpeg");
                		map1.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=205960173&idx=2&sn=de3da5f5af2504c66cdf56344b9170fe#rd");
                		list.add(map1);
                		respMessage = articleMessage(list,fromUserName,toUserName);
                        return respMessage;
                	}else if(key.equals("kefu")){
                		StringBuffer buffer = new StringBuffer();
            			buffer.append("以下问题将会转接到人工客服:").append("\n\n");
                    	buffer.append("回复【找回车牌】，解决车牌被别人注册的问题;").append("\n\n");
                    	buffer.append("回复【验证码】，解决验证码收不到的问题;").append("\n\n");
                    	buffer.append("回复【停车费】，解决停车费支付问题;");
            			
            			textMessage.setContent(buffer.toString());
            			respMessage = MessageUtil.textMessageToXml(textMessage);
            			return respMessage;
                	}
                }  
                //上报地理位置事件
                else if(eventType.equals(MessageUtil.EVENT_TYPE_LOCATION)){
                	StringBuffer buffer = new StringBuffer();
                	String latitude = requestMap.get("Latitude");
                	String longitude = requestMap.get("Longitude");
                	String precision = requestMap.get("Precision");
//                	respContent = buffer.toString();
                }
                //点击菜单跳转链接事件推送
                else if(eventType.equals(MessageUtil.EVENT_TYPE_VIEW)){
//                	respContent = "这是菜单跳转链接";
                }
                //客服关闭会话窗口
                else if(eventType.equals(MessageUtil.EVENT_TYPE_KF_CLOSE_SESSION)){
                	System.out.println("kf_close_session>>>openid:"+fromUserName);
                	StringBuffer buffer = new StringBuffer();
                	buffer.append("此次会话已结束，祝您生活愉快，谢谢！");
                	textMessage.setContent(buffer.toString());
                	respMessage = MessageUtil.textMessageToXml(textMessage);
					return respMessage;
                }
            }  
            if(respContent.equals("")){
            	StringBuffer buffer = new StringBuffer();
            	
            	if(userMap == null){
            		buffer.append("<a href=\""+url+"\">点击注册</a>，立即领取10元礼包");
            	}else{
            		buffer.append("一大波认证用户专享福利即将来袭，回复“认证”了解详情。").append("\n\n");
            		buffer.append("回复“停车券”了解停车券使用详情").append("\n\n");
            		buffer.append("回复“打灰机”，用停车券赚余额。").append("\n\n");
            		buffer.append("回复“更多”，了解更多。");
            	}
            	respContent = buffer.toString();  
            }
            textMessage.setContent(respContent);  
            respMessage = MessageUtil.textMessageToXml(textMessage);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
  
        return respMessage;  
    }  
	
    private String articleMessage(List<Map<String, String>> list, String fromUserName, String toUserName){
    	List<Article> articleList = new ArrayList<Article>(); 
    	String respMessage = null; 
    	// 创建图文消息  
        NewsMessage newsMessage = new NewsMessage();  
        newsMessage.setToUserName(fromUserName);  
        newsMessage.setFromUserName(toUserName);  
        newsMessage.setCreateTime(new Date().getTime());  
        newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);  
        newsMessage.setFuncFlag(0);
        for(Map<String, String> map : list){
        	Article article = new Article();  
        	article.setTitle(map.get("title"));   
            article.setPicUrl(map.get("picurl"));  
            article.setUrl(map.get("url"));
            article.setDescription(map.get("descp"));
            articleList.add(article);
        }
        // 设置图文消息个数  
        newsMessage.setArticleCount(articleList.size());  
        // 设置图文消息包含的图文集合  
        newsMessage.setArticles(articleList);  
        // 将图文消息对象转换成xml字符串  
        respMessage = MessageUtil.newsMessageToXml(newsMessage); 
    	return respMessage;
    }
    
	@Override
	public void init() throws ServletException {
		ApplicationContext ctx = WebApplicationContextUtils
				.getWebApplicationContext(getServletContext());
		dataBaseService= (DataBaseService) ctx.getBean("dataBaseService");
		pgOnlyReadService = (PgOnlyReadService)ctx.getBean("pgOnlyReadService");
		pgOnlyReadService = (PgOnlyReadService)ctx.getBean("pgOnlyReadService");
		publicMethods = (PublicMethods)ctx.getBean("publicMethods");
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
	}
}
