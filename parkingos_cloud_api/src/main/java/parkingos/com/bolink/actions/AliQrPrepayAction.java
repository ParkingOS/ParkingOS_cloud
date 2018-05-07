package parkingos.com.bolink.actions;

import com.alibaba.fastjson.JSONObject;
import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import parkingos.com.bolink.beans.*;
import parkingos.com.bolink.component.CommonComponent;
import parkingos.com.bolink.constant.Constants;
import parkingos.com.bolink.constant.WeixinConstants;
import parkingos.com.bolink.dto.UnionInfo;
import parkingos.com.bolink.dto.WXUserView;
import parkingos.com.bolink.service.AliPrepayService;
import parkingos.com.bolink.service.QrFilterService;
import parkingos.com.bolink.service.ShopTicketManagerService;
import parkingos.com.bolink.utlis.*;
import parkingos.com.bolink.utlis.weixinpay.utils.XMLUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 减免劵使用
 */
@Controller
public class AliQrPrepayAction {
    Logger logger = Logger.getLogger(AliQrPrepayAction.class);
    @Autowired
    CommonComponent commonComponent;
    @Autowired
    AliPrepayService aliPrepayService;
    @Autowired
    CommonUtils commonUtils;
    @Autowired
    public CommonDao commonDao;
    @Autowired
    private ShopTicketManagerService shopTicketManagerService;
    @Autowired
    private QrFilterService qrFilterService;

    /**
     * 开始用劵
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("/aliprepay")
    public String startUseTicket(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String action = RequestUtil.processParams(request, "action");
        String target = null;
        if (action.equals("useshopticket")) {//ali使用商户减免券业务
            target = useShopTicket(request, response);
        } else if (action.equals("wxuseshopticket")) {//微信使用商户减免券业务
            target = wxuseShopTicket(request, response);
        } else if (action.equals("toddcar")) {
            target = toAddCar(request);
        } else if(action.equals("checkFixCode")){//支付宝校验
            target = checkFixCode(request, response);
        } else if(action.equals("wxcheckFixCode")){//微信校验
            target = wxcheckFixCode(request, response);
        }
        return target;
    }

    //发红包测试
//    @ResponseBody
    @RequestMapping("/bonstest")
    public String bonsTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String total_amount = RequestUtil.getString(request, "total_amount");//找零金额
        logger.error("=========================>>>>>>>>>>>>total_amount" + total_amount);
        String change = RequestUtil.getString(request, "change");//实际车场出的金额,红包最小为1元
        String orderId = RequestUtil.getString(request, "orderId");
        Integer type = RequestUtil.getInteger(request, "type", -1);
//        String machineId = RequestUtil.getString(request,"machineId");
        logger.error("==============>>>>>type" + type);
        Map<String, String> resultmap = new HashMap<>();
        if (type == null || type == -1) {
//            String secret = "3eb470a03d517097e6c887efa368c9c4";
//            String appid = "wx962fe9d5c0e2a2c7";
//            String openId = "ouc2o08_DvoSQqo-eSmk-ZElqIik";
            String secret = WeixinConstants.WXPUBLIC_SECRET;
            String appid = WeixinConstants.WXPUBLIC_APPID;
            String openId = "";
            String code = RequestUtil.processParams(request, "code");
            String accessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appid + "&secret=" + secret + "&code=" + code + "&grant_type=authorization_code";
            String result = WexinPublicUtil.httpsRequest(accessTokenUrl, "GET", null);
            logger.error("============>>>>result" + result);
            JSONObject map = null;
            if (result != null) {
                map = JSONObject.parseObject(result);
            }
            String fromScope = "snsapi_base";
            if (map == null || map.get("errcode") != null) {
                logger.error("获取openid失败....");
                String redirect_url = "http%3a%2f%2f" + WeixinConstants.WXPUBLIC_REDIRECTURL + "%2fzld%2fbonstest.do?total_amount%3d" + total_amount + "%26orderId%3d" + orderId + "%26change%3d" + change;
                logger.error("=============>>>>....redirect_url" + redirect_url);
                String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
                        + appid
                        + "&redirect_uri="
                        + redirect_url
                        + "&response_type=code&scope=" + fromScope + "&state=123#wechat_redirect";
                logger.error("url===========>>>>>>...." + url);
                try {
                    response.sendRedirect(url);
                    return null;
                } catch (IOException e) {
                    logger.error("获取公众号openid重定向异常=>" + e.getMessage());
                }
            }
            openId = map.getString("openid");
            logger.error(openId);
            String url = "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack";
            SortedMap<String, String> packageParams = new TreeMap<String, String>();
            packageParams.put("nonce_str", System.currentTimeMillis() + "");//随机字符串
            packageParams.put("mch_billno", TimeTools.getTimeYYYYMMDDHHMMSS());//商户订单号
            packageParams.put("mch_id", "1481594592");//商户号
            packageParams.put("wxappid", appid);//公众账号appid
            //packageParams.put("msgappid", "wxe9f12586b0e3b7d0");
            packageParams.put("send_name", "泊链联盟");
            packageParams.put("re_openid", openId);//用户openid
            packageParams.put("total_amount", total_amount);//付款金额
            packageParams.put("total_num", "1");//红包发放总人数
            packageParams.put("wishing", "红包找零");//红包祝福语
            packageParams.put("client_ip", Inet4Address.getLocalHost().getHostAddress().toString());//ip地址
            packageParams.put("act_name", "抢红包活动");//活动名称
            packageParams.put("remark", "快来抢");//备注
            //获取package包
            RequestHandler reqHandler = new RequestHandler(request, response);
            String packageValue = reqHandler.genPackage(packageParams);
            logger.error("==============>>>>>>>>>>红包packageValue" + packageValue);
            String resContent = reqHandler.sendBons(url, packageValue);
            logger.info("=============>>>红包resContent" + resContent);
            resultmap = XMLUtil.doXMLParse(resContent);
        }
        logger.info("============>>>>>红包resultmap" + resultmap);
        if ("FAIL".equals(resultmap.get("result_code")) && resultmap.get("return_msg").indexOf("余额") != -1) {
            request.setAttribute("type", 5);
            request.setAttribute("change", Double.parseDouble(total_amount) / 100);
            return "redpacket";
        }
        HttpProxy httpProxy = new HttpProxy();
//            String callbackurl = Defind.getProperty("HBUNIONIP")+"bonstestcallback.do";//s.bolink
        String callbackurl = Defind.getProperty("UNIONIP") + "bonstestcallback.do";//beta.bolink
        Map<String, String> callbackmap = new HashMap<>();
        if (resultmap != null) {
            callbackmap.put("data", resultmap.get("result_code"));
        } else {
            callbackmap.put("data", "");
        }
        callbackmap.put("type", type + "");
        callbackmap.put("orderId", orderId);
        callbackmap.put("total_amount", total_amount);//红包金额
        callbackmap.put("change", change);//车场实际找零
//            callbackmap.put("machineId",machineId);
        httpProxy.doPost(callbackurl, callbackmap);
        logger.error("===========>>>>>>>>>返回beta" + callbackmap);
        request.setAttribute("type", type);
        request.setAttribute("change", Double.parseDouble(total_amount) / 100);
        return "redpacket";
    }

    /**
     * 微信i使用商户减免券业务
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String wxuseShopTicket(HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {

        logger.error("进入微信使用减免券方法");
        Long ticketId = RequestUtil.getLong(request, "ticketid", -1L);
        Long comid = RequestUtil.getLong(request, "parkid", -1L);
        String openid = RequestUtil.processParams(request, "openid");//"oRoekt9RN8LxHDLq43QJqRhoc0t8";// "oRoekt9RN8LxHDLq43QJqRhoc0t8";//
        logger.error("进入微信使用减免券方法"+ticketId+"~~~~"+comid);
        //上线去掉
        // String openid ="oGDN-04sKcopIa-aC8CT_xft9CSg";
        logger.error("openid" + openid);
        if (openid.equals("")) {
            String code = RequestUtil.processParams(request, "code");
            String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + WeixinConstants.WXPUBLIC_APPID + "&secret=" + WeixinConstants.WXPUBLIC_SECRET + "&code=" + code + "&grant_type=authorization_code";
            String result = WexinPublicUtil.httpsRequest(access_token_url, "GET", null);
            JSONObject map = JSONObject.parseObject(result);
            if (map == null || map.get("errcode") != null) {
                String redirect_url = "http%3a%2f%2f" + WeixinConstants.WXPUBLIC_REDIRECTURL + "%2fzld%2faliprepay%3faction%3dwxuseshopticket"
                        + "%26parkid%3d" + comid + "%26ticketid%3d" + ticketId;
                logger.error(">>>>>>>>>>>>获取openid失败....,重新获取>>>>>>>>>>>redirect_url=" + redirect_url);
                String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
                        + WeixinConstants.WXPUBLIC_APPID
                        + "&redirect_uri="
                        + redirect_url
                        + "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
                response.sendRedirect(url);
                return null;
            }
            openid = map.getString("openid");
        }
        request.setAttribute("openid", openid);
        String carNumber = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "licence"));
        WXUserView wxUserView = commonComponent.getUserinfoByOpenid(openid);
        Long uin = wxUserView.getUin();
        Integer result = 0;
        if (!Check.isEmpty(carNumber)) {
            result = commonComponent.addCarnumber(uin, carNumber);
            logger.error("add car:" + result);
        } else {
            Integer bindflag = wxUserView.getBindflag();
            //获取车主车牌号
            carNumber = aliPrepayService.getCarNumber(uin, bindflag);
        }
        if (Check.isEmpty(carNumber)) {
            request.setAttribute("from", "useticket");
            request.setAttribute("furl", "aliprepay.do");
            request.setAttribute("ticketid", ticketId);
            request.setAttribute("comid", comid);
            request.setAttribute("action", "wxuseshopticket");
            return "aliprepay/carnumber";
        }
        return handelPrepay(request, response, ticketId, carNumber, comid, "wxuseshopticket");
    }

    /**
     * ali使用商户减免券业务
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    private String useShopTicket(HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        Long ticketId = RequestUtil.getLong(request, "ticketid", -1L);
        //取车牌查订单
        String carNumber = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "licence"));
        Long comid = RequestUtil.getLong(request, "parkid", -1L);
        if (Check.isEmpty(carNumber)) {
            Cookie[] cookies = request.getCookies();//这样便可以获取一个cookie数组
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie != null && cookie.getName().equals("lience"))
                        carNumber = AjaxUtil.decodeUTF8(cookie.getValue());
                }
            }
        } else {
            Cookie cookie = new Cookie("lience", AjaxUtil.encodeUTF8(carNumber));
            cookie.setMaxAge(8640000);//暂定100天
            //设置路径，这个路径即该工程下都可以访问该cookie 如果不设置路径，那么只有设置该cookie路径及其子路径可以访问
            cookie.setPath("/");
            response.addCookie(cookie);
            logger.error("已保存到cookie,lience=" + carNumber);
        }
        if (Check.isEmpty(carNumber)) {//没有车牌，到添加车牌页面
            request.setAttribute("from", "useticket");
            request.setAttribute("furl", "aliprepay.do");
            request.setAttribute("ticketid", ticketId);
            request.setAttribute("comid", comid);
            request.setAttribute("action", "useshopticket");
            return "aliprepay/carnumber";
        }
        return handelPrepay(request, response, ticketId, carNumber, comid, "useshopticket");
    }

    /**
     * 处理订单减免和预付
     *
     * @param request
     * @param response
     * @param ticketId  减免券编号
     * @param carNumber 车牌号
     * @param comid     车场编号
     * @return
     * @throws Exception
     */
    private String handelPrepay(HttpServletRequest request, HttpServletResponse response,
                                Long ticketId, String carNumber, Long comid, String action) throws Exception {

        //1 查询在场订单
        request.setAttribute("ticketid", ticketId);
        request.setAttribute("parkid", comid);
        request.setAttribute("carnumber", carNumber);
        request.setAttribute("forward", action);
        OrderTb unpayOrder = aliPrepayService.qryUnpayOrder(carNumber, comid);
        if (unpayOrder == null) {
            request.setAttribute("noorder", 1);
            request.setAttribute("error", "车辆未入场");
            return "aliprepay/prepay";
        }

        //获取车场是否支持多个叠加券下发
        ComInfoTb comInfoTb = new ComInfoTb();
        comInfoTb.setId(comid);
        comInfoTb = (ComInfoTb) commonDao.selectObjectByConditions(comInfoTb);
        Integer superimposed = 0;//默认不支持
        if (comInfoTb != null) {
            superimposed = comInfoTb.getSuperimposed();
        }
        logger.error("车场是否支持叠加用券：" + superimposed);
        //2 判断减免券是否可用
        Integer count = aliPrepayService.unpayOrderForTicket(unpayOrder.getId());
        logger.error("是否已使用过券：" + count);
        //查询券的类型
        Integer ticketType = -1;
        BigDecimal money = new BigDecimal("0");
        Integer duration = 0;
        TicketTb ticketTb = null;
        Long shopId = -1L;
        String error = "车场网络异常，请稍候重试";
        if (ticketId != -1) {
            if (superimposed == 0) {//如果不支持多张叠加下发,判断是不是已经用过了优惠券
                if (count >= 1) {
                    error = "本次停车已使用过优惠券";
                    logger.error("本次停车已使用过优惠券");
                    request.setAttribute("noorder", -1);
                    request.setAttribute("error", error);
                    return "aliprepay/prepay";
                }
            }
            ticketTb = aliPrepayService.qryTicket(ticketId);
            shopId = ticketTb.getShopId();
            if (ticketTb != null) {
                //判断有效期
                long limit_day = ticketTb.getLimitDay();
                if (System.currentTimeMillis() / 1000 > limit_day) {
                    error = "该优惠券已过期";
                    logger.error("该优惠券已过期");
                } else {
                    Integer state = ticketTb.getState();
                    if (state != null) {
                        if (state == 0) {
                            Integer type = ticketTb.getType();
                            if (type != null) {
                                if (type == 3) {//3减时券
                                    ticketType = 1;
                                    duration = ticketTb.getMoney();
                                } else if (type == 5) {//减免金额劵
                                    ticketType = 0;
                                    money = ticketTb.getUmoney();
                                    logger.error("该优惠劵：" + ticketId + ",money：" + money);
                                } else if (type == 4) {//全免劵
                                    ticketType = 2;
                                    logger.error("该优惠劵：" + ticketId + ",money：" + money);
                                } else {
                                    error = "优惠券无效";
                                    logger.error("优惠券无效");
                                }
                            } else {
                                error = "优惠券无效";
                                logger.error("优惠券无效");
                            }
                        } else if (state == 2) {
                            error = "该优惠券已过期";
                            logger.error("该优惠券已过期");
                        } else if (state == 1) {
                            error = "该优惠券已使用";
                            logger.error("该优惠券已使用");
                        }
                    } else {
                        error = "该优惠券已使用";
                        logger.error("该优惠券已使用");
                    }
                }
            } else {
                error = "优惠券不存在";
                logger.error("该优惠券不存在");
            }
        } else {
            error = "优惠券不存在";
            logger.error("该优惠券不存在");
        }
        //3 下发减免劵
        boolean isSend = false;//是否已发送的SDK
        if (ticketType != -1) {//取到了券信息并且有通道，发送消息到sdk
            //Channel channel = NettyChannelMap.get(commonUtils.getChannel(ticketTb.getComid()+""));
            ParkTokenTb tokenTb = commonUtils.getChannel(comid);
            if (tokenTb != null) {
                Map<String, Object> messageMap = new HashMap<String, Object>();
                messageMap.put("service_name", "deliver_ticket");
                messageMap.put("ticket_id", ticketId + "");
                messageMap.put("create_time", ticketTb.getCreateTime());
                messageMap.put("limit_day", ticketTb.getLimitDay());
                messageMap.put("duration", duration);
                messageMap.put("money", money + "");
                messageMap.put("car_number", carNumber);
                messageMap.put("ticket_type", ticketType);
                ShopTb shopTb = new ShopTb();
                shopTb.setId(shopId);
                shopTb = (ShopTb) commonDao.selectObjectByConditions(shopTb);
                messageMap.put("shop_name", shopTb.getName());
                if (ticketType == 1) {//时长减免，加上减免单位
                    logger.error("发送减免券得到商户信息：" + shopTb);
                    messageMap.put("ticket_unit", shopTb.getTicketUnit());
                }
                messageMap.put("order_id", unpayOrder.getOrderIdLocal());
                messageMap.put("remark", ticketTb.getRemark());
                String mesg = StringUtils.createJson(messageMap);
                isSend = commonUtils.doSendMessage(mesg, tokenTb);//commonUtils.doBackMessage(mesg, channel);
                logger.error("发送减免券数据到SDK：" + mesg + ",ret:" + isSend);
            }
        }
        // 4 判断是不是用劵用户再扫同一张劵
        TicketTb ticketTbCondition = new TicketTb();
        ticketTbCondition.setId(ticketId);
        ticketTbCondition.setOrderid(unpayOrder.getId());
        Integer orderCount = commonDao.selectCountByConditions(ticketTbCondition);//返回1 表示是
        logger.error("是不是用劵用户再扫同一张劵>>>" + orderCount);
        // 5 跳转到泊链支付
        if (isSend || orderCount == 1) {
            //根据车场编号找到对应的厂商编号
            UnionInfo unionInfo = commonComponent.getUnionInfo(comid);
            Long unionid = unionInfo.getUnionId();

            ComInfoTb comInfoTb1 = new ComInfoTb();
            comInfoTb1.setId(comid);
            comInfoTb1 = (ComInfoTb)commonDao.selectObjectByConditions(comInfoTb1);
            String url = "";
            if(comInfoTb1!=null&&comInfoTb1.getBolinkId()!=null&&!"".equals(comInfoTb1.getBolinkId())){
                String parkid = comInfoTb1.getBolinkId();
                url = Constants.UNIONIP +
                        "thirdpay?comid=" + parkid + "&unionid=" + unionid + "&orderid=" + unpayOrder.getOrderIdLocal() +"&useticket=1";
            }else{
                url = Constants.UNIONIP +
                        "thirdpay?comid=" + comid + "&unionid=" + unionid + "&orderid=" + unpayOrder.getOrderIdLocal() +"&useticket=1";
            }

//            String url = Constants.UNIONIP +
//                    "thirdpay?comid=" + comid + "&unionid=" + unionid + "&orderid=" + unpayOrder.getOrderIdLocal();
            logger.error("去泊链平台查询使用减免劵的订单信息：" + url);
            try {
                response.sendRedirect(url);
            } catch (IOException e) {
                logger.error("去泊链平台查询使用减免劵的订单信息出错" + e.getMessage());
                e.printStackTrace();
            }
            return null;
        } else {
            request.setAttribute("noorder", -1);
            request.setAttribute("error", error);
            return "aliprepay/prepay";
        }
    }

    private String toAddCar(HttpServletRequest request) {
        logger.info("进入添加车牌的方法");
        Long ticketId = RequestUtil.getLong(request, "ticketid", -1L);
        Long comid = RequestUtil.getLong(request, "parkid", -1L);
        String forward = RequestUtil.getString(request, "forward");
        String openid = RequestUtil.getString(request, "openid");
        String fixcode = RequestUtil.getString(request, "fixcode");
        logger.info("进入添加车牌的方法"+fixcode+comid);
        request.setAttribute("from", "useticket");
        request.setAttribute("furl", "aliprepay.do");
        request.setAttribute("ticketid", ticketId);
        request.setAttribute("comid", comid);
        request.setAttribute("openid", openid);
        request.setAttribute("fixcode", fixcode);
        request.setAttribute("action", forward);
        return "aliprepay/carnumber";
    }


    private String wxcheckFixCode(HttpServletRequest request, HttpServletResponse response) throws Exception{
        logger.error("进入微信使用固定码方法");
        Long ticketId = RequestUtil.getLong(request, "ticketid", -1L);
        String fixcode = RequestUtil.getString(request,"fixcode");
        Long comid = RequestUtil.getLong(request, "parkid", -1L);
        String openid = RequestUtil.processParams(request, "openid");//"oRoekt9RN8LxHDLq43QJqRhoc0t8";// "oRoekt9RN8LxHDLq43QJqRhoc0t8";//
        logger.error("进入微信使用减免券方法"+fixcode+"~~~~"+ticketId+"~~~~"+comid);
        //上线去掉
        // String openid ="oGDN-04sKcopIa-aC8CT_xft9CSg";
        logger.error("openid" + openid);
        if (openid.equals("")) {
            String code = RequestUtil.processParams(request, "code");
            String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + WeixinConstants.WXPUBLIC_APPID + "&secret=" + WeixinConstants.WXPUBLIC_SECRET + "&code=" + code + "&grant_type=authorization_code";
            String result = WexinPublicUtil.httpsRequest(access_token_url, "GET", null);
            JSONObject map = JSONObject.parseObject(result);
            if (map == null || map.get("errcode") != null) {
                String redirect_url = "http%3a%2f%2f" + WeixinConstants.WXPUBLIC_REDIRECTURL + "%2fzld%2faliprepay%3faction%3dwxuseshopticket"
                        + "%26parkid%3d" + comid + "%26ticketid%3d" + ticketId+"%26fixcode%3d"+fixcode;
                logger.error(">>>>>>>>>>>>获取openid失败....,重新获取>>>>>>>>>>>redirect_url=" + redirect_url);
                String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
                        + WeixinConstants.WXPUBLIC_APPID
                        + "&redirect_uri="
                        + redirect_url
                        + "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
                response.sendRedirect(url);
                return null;
            }
            openid = map.getString("openid");
        }
        request.setAttribute("openid", openid);
        String carNumber = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "licence"));
        logger.info("修改车牌号11111:"+carNumber);
        WXUserView wxUserView = commonComponent.getUserinfoByOpenid(openid);
        Long uin = wxUserView.getUin();
        Integer result = 0;
        if (!Check.isEmpty(carNumber)) {
            result = commonComponent.addCarnumber(uin, carNumber);
            logger.error("add car:" + result);
        } else {
            Integer bindflag = wxUserView.getBindflag();
            //获取车主车牌号
            carNumber = aliPrepayService.getCarNumber(uin, bindflag);
        }
        logger.info("修改车牌号22222:"+carNumber);
        if (Check.isEmpty(carNumber)) {
            request.setAttribute("from", "useticket");
            request.setAttribute("furl", "aliprepay.do");
            request.setAttribute("ticketid", ticketId);
            request.setAttribute("comid", comid);
            request.setAttribute("fixcode", fixcode);
            request.setAttribute("action", "wxcheckFixCode");
            return "aliprepay/carnumber";
        }
        //1 查询在场订单
        request.setAttribute("ticketid", ticketId);
        request.setAttribute("parkid", comid);
        request.setAttribute("carnumber", carNumber);
        request.setAttribute("fixcode", fixcode);
        request.setAttribute("forward", "wxcheckFixCode");
        OrderTb unpayOrder = aliPrepayService.qryUnpayOrder(carNumber, comid);
        if (unpayOrder == null) {
            request.setAttribute("noorder", 1);
            request.setAttribute("error", "车辆未入场");
            return "aliprepay/prepay";
        }

        //获取车场是否支持多个叠加券下发
        ComInfoTb comInfoTb = new ComInfoTb();
        comInfoTb.setId(comid);
        comInfoTb = (ComInfoTb) commonDao.selectObjectByConditions(comInfoTb);
        Integer superimposed = 0;//默认不支持
        if (comInfoTb != null) {
            superimposed = comInfoTb.getSuperimposed();
        }
        logger.error("车场是否支持叠加用券：" + superimposed);
        //2 判断减免券是否可用
        Integer count = aliPrepayService.unpayOrderForTicket(unpayOrder.getId());
        logger.error("是否已使用过券：" + count);
        String error = "车场网络异常，请稍候重试";

        if (superimposed == 0) {//如果不支持多张叠加下发,判断是不是已经用过了优惠券
            if (count >= 1) {
                error = "本次停车已使用过优惠券";
                logger.error("本次停车已使用过优惠券");
                request.setAttribute("noorder", -1);
                request.setAttribute("error", error);
                return "aliprepay/prepay";
            }
        }


        //3 生成劵
        Map<String,Object> retMap = createTicketByCode(request,response,fixcode);
        if(retMap.get("state") == 0 ||retMap.get("state") == -1){
//            logger.info("ticketManager noscan 车牌>>>>>>>>"+car_number+"生成减免劵出错，用劵失败");
//            AjaxUtil.ajaxOutput(response, retMap);
            request.setAttribute("state", 1);
            request.setAttribute("error", "优惠券额度已经用完，请联系优惠券管理员");
            return "aliprepay/error";
        }

        QrCodeTb qrCodeTb = qrFilterService.getQrCode((String)retMap.get("result"));

        logger.error("sadddddddddddddddddddddddddddddddddddd");
//        response.sendRedirect("http://test.bolink.club/zld/qr/c/"+qrCodeTb.getCode());
        response.sendRedirect("http://"+WeixinConstants.WXPUBLIC_REDIRECTURL+"/zld/qr/c/"+qrCodeTb.getCode());
//        retMap = noScanUseTicket(request,response,qrCodeTb.getTicketid(),carNumber,qrCodeTb.getComid(),unpayOrder);
//        AjaxUtil.ajaxOutput(response, retMap);
        return null;


    }

    private String checkFixCode(HttpServletRequest request, HttpServletResponse response) throws Exception{

        //取车牌查订单
        String carNumber = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "licence"));
        String code = RequestUtil.getString(request,"fixcode");
        Long comid = RequestUtil.getLong(request, "parkid", -1L);
        if (Check.isEmpty(carNumber)) {
            Cookie[] cookies = request.getCookies();//这样便可以获取一个cookie数组
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie != null && cookie.getName().equals("lience"))
                        carNumber = AjaxUtil.decodeUTF8(cookie.getValue());
                }
            }
        } else {
            Cookie cookie = new Cookie("lience", AjaxUtil.encodeUTF8(carNumber));
            cookie.setMaxAge(8640000);//暂定100天
            //设置路径，这个路径即该工程下都可以访问该cookie 如果不设置路径，那么只有设置该cookie路径及其子路径可以访问
            cookie.setPath("/");
            response.addCookie(cookie);
            logger.error("已保存到cookie,lience=" + carNumber);
        }
        if (Check.isEmpty(carNumber)) {//没有车牌，到添加车牌页面
            request.setAttribute("from", "useticket");
            request.setAttribute("furl", "aliprepay.do");
//            request.setAttribute("ticketid", ticketId);
            request.setAttribute("comid", comid);
            request.setAttribute("fixcode", code);
            request.setAttribute("action", "checkFixCode");
            return "aliprepay/carnumber";
        }

        //1 查询在场订单
//        request.setAttribute("ticketid", ticketId);
        request.setAttribute("parkid", comid);
        request.setAttribute("carnumber", carNumber);
        request.setAttribute("fixcode", code);
        request.setAttribute("forward", "wxcheckFixCode");
        OrderTb unpayOrder = aliPrepayService.qryUnpayOrder(carNumber, comid);
        if (unpayOrder == null) {
            request.setAttribute("noorder", 1);
            request.setAttribute("error", "车辆未入场");
            return "aliprepay/prepay";
        }


        //获取车场是否支持多个叠加券下发
        ComInfoTb comInfoTb = new ComInfoTb();
        comInfoTb.setId(comid);
        comInfoTb = (ComInfoTb) commonDao.selectObjectByConditions(comInfoTb);
        Integer superimposed = 0;//默认不支持
        if (comInfoTb != null) {
            superimposed = comInfoTb.getSuperimposed();
        }
        logger.error("车场是否支持叠加用券：" + superimposed);
        //2 判断减免券是否可用
        Integer count = aliPrepayService.unpayOrderForTicket(unpayOrder.getId());
        logger.error("是否已使用过券：" + count);
        String error = "车场网络异常，请稍候重试";

        if (superimposed == 0) {//如果不支持多张叠加下发,判断是不是已经用过了优惠券
            if (count >= 1) {
                error = "本次停车已使用过优惠券";
                logger.error("本次停车已使用过优惠券");
                request.setAttribute("noorder", -1);
                request.setAttribute("error", error);
                return "aliprepay/prepay";
            }
        }


        //3 生成劵
        Map<String,Object> retMap = createTicketByCode(request,response,code);
        if(retMap.get("state") == 0 ||retMap.get("state") == -1){
//            logger.info("ticketManager noscan 车牌>>>>>>>>"+car_number+"生成减免劵出错，用劵失败");
//            AjaxUtil.ajaxOutput(response, retMap);
            request.setAttribute("state", 1);
            request.setAttribute("error", "优惠券额度已经用完，请联系优惠券管理员");
            return "aliprepay/error";
        }

        QrCodeTb qrCodeTb = qrFilterService.getQrCode((String)retMap.get("result"));
//        logger.error("sadddddddddddddddddddddddddddddddddddd");
        response.sendRedirect("http://"+WeixinConstants.WXPUBLIC_REDIRECTURL+"/zld/qr/c/"+qrCodeTb.getCode());
//        retMap = noScanUseTicket(request,response,qrCodeTb.getTicketid(),carNumber,qrCodeTb.getComid(),unpayOrder);
//        AjaxUtil.ajaxOutput(response, retMap);
        return null;
    }

    private Map<String,Object> createTicketByCode(HttpServletRequest request, HttpServletResponse response, String code) {
        logger.info("后台创建优惠券:");

        FixCodeTb fixCodeTb = new FixCodeTb();
        fixCodeTb.setCode(code);
        fixCodeTb = (FixCodeTb)commonDao.selectObjectByConditions(fixCodeTb);

        Long shop_id = fixCodeTb.getShopId();
        ShopTb shopTb = new ShopTb();
        shopTb.setId(shop_id);
        shopTb =(ShopTb)commonDao.selectObjectByConditions(shopTb);

        Map<String, Object> rMap = new HashMap<String, Object>();
        if(shop_id == -1){
            rMap.put("result", -1);
            rMap.put("error", "商户编号>>"+shop_id+"不存在");
            return rMap;
        }

        if(fixCodeTb.getType()==1){//减免券
            //获取单张金额(时长或者金额)
            Integer amount = fixCodeTb.getAmount();
            //获取该商户的减免单位,判断是时长还是金额
            Integer ticketUnit = shopTb.getTicketUnit();
            if(ticketUnit==4){//减免金额
                if(amount>fixCodeTb.getMoneyLimit()){
                    rMap.put("state",0);
                    rMap.put("error","固定码金额余额不足");
                    return rMap;
                }else{

                    Map<String,Object> retMap = createTicket(shop_id,amount,5,0);
                    if(retMap.get("result") == -1 || retMap.get("result") == -2){
                        logger.info("生成减免劵出错，用劵失败");
                        rMap.put("state",0);
                        rMap.put("error",retMap.get("error"));
                        return rMap;
                    }

                    fixCodeTb.setMoneyLimit(fixCodeTb.getMoneyLimit()-amount);
                    fixCodeTb.setFreeLimit(fixCodeTb.getFreeLimit()-1);
                    commonDao.updateByPrimaryKey(fixCodeTb);


                    String ticketCode = retMap.get("code")+"";
//                    String ticketurl = CustomDefind.getValue("TICKETURL")+ticketCode;
                    rMap.put("state",1);
                    rMap.put("result",ticketCode);
//                    rMap.put("ticketurl",ticketurl);
                    return rMap;
                }
            }else{
                if(amount>fixCodeTb.getTimeLimit()){
                    rMap.put("state",0);
                    rMap.put("error","固定码时长余额不足");
                    return rMap;
                }else{
                    Map<String,Object> retMap = createTicket(shop_id,amount,3,0);
                    if(retMap.get("result") == -1 || retMap.get("result") == -2){
                        logger.info("生成减免劵出错，用劵失败");
                        rMap.put("state",0);
                        rMap.put("error",retMap.get("error"));
                        return rMap;
                    }

                    fixCodeTb.setTimeLimit(fixCodeTb.getTimeLimit()-amount);
                    fixCodeTb.setFreeLimit(fixCodeTb.getFreeLimit()-1);
                    commonDao.updateByPrimaryKey(fixCodeTb);

                    String ticketCode = retMap.get("code")+"";
//                    String ticketurl = CustomDefind.getValue("TICKETURL")+ticketCode;
                    rMap.put("state",1);
                    rMap.put("result",ticketCode);
//                    rMap.put("ticketurl",ticketurl);
                    return rMap;
                }
            }

        }else if(fixCodeTb.getType()==2){//全免券
            if(fixCodeTb.getFreeLimit()<1){
                rMap.put("state",0);
                rMap.put("error","固定码全免券余额不足");
                return rMap;
            }else{

                Map<String,Object> retMap = createTicket(shop_id,0,4,0);
                if(retMap.get("result") == -1 || retMap.get("result") == -2){
                    logger.info("生成减免劵出错，用劵失败");
                    rMap.put("state",0);
                    rMap.put("error",retMap.get("error"));
                    return rMap;
                }

                fixCodeTb.setFreeLimit(fixCodeTb.getFreeLimit()-1);
                commonDao.updateByPrimaryKey(fixCodeTb);

                String ticketCode = retMap.get("code")+"";
//                String ticketurl = CustomDefind.getValue("TICKETURL")+ticketCode;
                rMap.put("state",1);
                rMap.put("result",ticketCode);
//                rMap.put("ticketurl",ticketurl);
                return rMap;
            }
        }
        return rMap;
    }

    private Map<String,Object> createTicket(Long shop_id,Integer reduce,Integer type,Integer isAuto) {

        logger.info("后台创建优惠券:"+isAuto+"~~~~"+type+"~~~~"+reduce);

        Integer free = 0;//全免劵(张)
        Map<String, Object> rMap = new HashMap<String, Object>();
        if(shop_id == -1){
            rMap.put("result", -1);
            rMap.put("error", "商户编号>>"+shop_id+"不存在");
            return rMap;
        }
        logger.error(">>>>>>>>>>打印优惠券，优惠券类型type:"+type+",优惠时长time："+reduce+",优惠金额amount："+reduce+",商户shop_id:"+shop_id);
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");

        ShopTb shopTb = new ShopTb();
        shopTb.setId(shop_id);
        ShopTb shopInfo =  (ShopTb) commonDao.selectObjectByConditions(shopTb);
        Integer ticket_limit = shopInfo.getTicketLimit()== null ? 0: shopInfo.getTicketLimit();
        Integer ticketfree_limit = shopInfo.getTicketfreeLimit() == null ? 0 : shopInfo.getTicketfreeLimit();
        Integer ticket_money = shopInfo.getTicketMoney() == null ? 0 : shopInfo.getTicketMoney();
        //未设置有效期,默认24小时
        Integer validite_time = shopInfo.getValiditeTime() == null ? 24: shopInfo.getValiditeTime();
        Long btime = System.currentTimeMillis()/1000;
        //截止有效时间
        Long etime =  btime + validite_time*60*60;
        //判断商户额度是否可以发劵
        if(type == 3){//优惠券-时长
            if(reduce <= 0){
                logger.error("优惠券额度必须为正数"+",商户shop_id:"+shop_id);
                rMap.put("result", -2);
                rMap.put("error", "优惠券额度必须为正数");
                return rMap;
            }
            if(ticket_limit < (reduce)){
                logger.error("优惠券额度已用完，还剩余额度"+ticket_limit+",优惠券时长time："+reduce+",商户shop_id:"+shop_id);
                rMap.put("result", -2);
                rMap.put("error", "优惠券额度不够");
                return rMap;
            }
        }else if(type == 5){//优惠券-金额
            if(reduce <= 0){
                logger.error("优惠券额度必须为正数"+",商户shop_id:"+shop_id);
                rMap.put("result", -2);
                rMap.put("error", "优惠券额度必须为正数");
                return rMap;
            }
            if(ticket_money < (reduce)){
                logger.error("优惠券额度已用完，还剩余额度"+ticket_money+",优惠券金额amount："+reduce+",商户shop_id:"+shop_id);
                rMap.put("result", -2);
                rMap.put("error", "优惠券额度不够");
                return rMap;
            }
        }else if(type==4){
            free = 1;
            reduce = 0;
            if(ticketfree_limit <= 0){
                logger.error("全免券额度已用完，还剩余额度"+ticketfree_limit+",商户shop_id:"+shop_id);
                rMap.put("result", -2);
                rMap.put("error", "全免券额度已用完");
                return rMap;
            }
        }
        List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
        //取当前最大减免劵的id然后+1
        Long ticketid = shopTicketManagerService.qryMaxTicketId()+1;
//        Long ticketid = commonDao.selectSequence(TicketTb.class);
        String code = null;
        Long ticketids[] = new Long[]{ticketid};
        String []codes = StringUtils.getGRCode(ticketids);
        if(codes.length > 0){
            code = codes[0];
        }
        if(code != null){
            //生成一张劵
            TicketTb ticketTb = new TicketTb();
            ticketTb.setId(ticketid);
            ticketTb.setCreateTime(btime);
            ticketTb.setLimitDay(etime);
            if(type==3){
                ticketTb.setMoney(reduce);
            }else if(type==5){
                BigDecimal amountDecimal = new BigDecimal(reduce+"");
                ticketTb.setUmoney(amountDecimal);
            }
            ticketTb.setState(0);
            ticketTb.setComid(shopInfo.getComid());
            ticketTb.setType(type);
            ticketTb.setRemark("");
            ticketTb.setShopId(shop_id);
            Integer insertTicket = commonDao.insert(ticketTb);

            //生成code
            QrCodeTb qrCodeTb = new QrCodeTb();
            qrCodeTb.setCtime(System.currentTimeMillis()/1000);
            qrCodeTb.setType(5);
            qrCodeTb.setCode(code);
            qrCodeTb.setIsauto(isAuto);
            qrCodeTb.setTicketid(ticketid);
            qrCodeTb.setComid(shopInfo.getComid());
            Integer insertQrcode = commonDao.insert(qrCodeTb);

            //更新商户额度
            ShopTb shopTbInfo = new ShopTb();
            if(type==3){
                shopTbInfo.setTicketLimit(shopInfo.getTicketLimit()-reduce);
            }else if(type==5){
                shopTbInfo.setTicketMoney(shopInfo.getTicketMoney()-reduce);
            }else if(type==4){
                shopTbInfo.setTicketfreeLimit(shopInfo.getTicketfreeLimit()-free);
            }
            ShopTb shopConditions = new ShopTb();
            shopConditions.setId(shopInfo.getId());
            Integer updateShop = commonDao.updateByConditions(shopTbInfo,shopConditions);
            logger.error("打印优惠券结果："+insertTicket+","+insertQrcode+","+updateShop+",商户shop_id:"+shop_id);
            if(insertTicket==1 && insertQrcode==1 && updateShop==1){
//                rMap.put("ticket_url", CustomDefind.getValue("TICKETURL")+code);
                rMap.put("state", 1);
                rMap.put("code", code);
                return rMap;
            }
        }
        rMap.put("result", -1);
        rMap.put("error","生成减免劵出错，用劵失败");
        return rMap;

    }

}
