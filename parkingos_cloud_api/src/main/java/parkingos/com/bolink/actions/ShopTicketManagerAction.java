package parkingos.com.bolink.actions;

import com.zld.common_dao.dao.CommonDao;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import parkingos.com.bolink.beans.*;
import parkingos.com.bolink.component.CommonComponent;
import parkingos.com.bolink.netty.NettyChannelMap;
import parkingos.com.bolink.service.AliPrepayService;
import parkingos.com.bolink.service.QrFilterService;
import parkingos.com.bolink.service.ShopTicketManagerService;
import parkingos.com.bolink.utlis.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户小程序登录
 */
@Controller
public class ShopTicketManagerAction {
    Logger logger = Logger.getLogger(ShopTicketManagerAction.class);
    @Autowired
    CommonComponent commonComponent;
    @Autowired
    ShopTicketManagerService shopTicketManagerService;
    @Autowired
    AliPrepayService aliPrepayService;
    @Autowired
    CommonUtils commonUtils;
    @Autowired
    public CommonDao commonDao;
    @Autowired
    QrFilterService qrFilterService;


    @RequestMapping(value="/shopticket")
    public  void shopTicket(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String action = RequestUtil.processParams(request, "action");
        String token =RequestUtil.processParams(request, "token");
        Long shop_id = RequestUtil.getLong(request, "shop_id", -1L);
        Map<String,Object> infoMap  = new HashMap<String, Object>();
        if(token==null||"null".equals(token)||"".equals(token)){
            infoMap.put("result", "fail");
            infoMap.put("message", "登录失效请重新登录");
            AjaxUtil.ajaxOutput(response, infoMap);
            return;
        }
        Long uin = validToken(token);
        if(uin == null){
            infoMap.put("result", "fail");
            infoMap.put("message", "登录失效请重新登录");
            AjaxUtil.ajaxOutput(response, infoMap);
            return;
        }
        if(action.equals("create")){
            Map<String,Object> rMap = createTicket(request,response,shop_id);
            AjaxUtil.ajaxOutput(response, rMap);
            return ;
            //http://127.0.0.1/zld/shopticket.do?action=create&shop_id=4&time=2&type=4&token=69213a6a50aff2b402a1dd13149a7c44
        }else if(action.equals("query")){
            logger.info("ticketManager query 进入获取商户今天统计的方法");
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
            String nowtime= df2.format(System.currentTimeMillis());
            String btime = RequestUtil.processParams(request, "btime");
            if(btime.equals(""))
                btime = nowtime;
            Long b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
            //结束时间取当前时间
            Long e = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime+" 23:59:59");
            logger.error("ticketManager query 结束时间"+e);
            Integer pageNum = RequestUtil.getInteger(request, "page", 1);
            Integer pageSize = RequestUtil.getInteger(request, "size", 20);
            Integer type = RequestUtil.getInteger(request, "type", -1);
            List<TicketTb> list = shopTicketManagerService.queryShopTicketPage(shop_id,b,e,type,0,0);
            logger.info("ticketManager query 商户shop_id:"+shop_id+"发行的优惠卷数量count:"+list.size());
            List<TicketTb> pageList = shopTicketManagerService.queryShopTicketPage(shop_id,b,e,type,pageNum,pageSize);
            logger.info("ticketManager query 商户shop_id:"+shop_id+"发行的优惠卷集合list:"+list);
            Long mcount = 0L;//优惠券-时长已打印数量
            Long mtotal = 0L;//优惠券-时长已打印额度
            Long acount = 0L;//优惠券-金额已打印数量
            Double amount = 0.0;//优惠券-金额已打印额度
            Long ecount = 0L;//全免券打印数量
            List<TicketTb> monthTicketList = shopTicketManagerService.queryShopTicketPage(shop_id,b,e,type,0,0);
            List<Map<String,Object>> ticketList = new ArrayList<Map<String,Object>>();
            if(monthTicketList != null && !monthTicketList.isEmpty()){
                for(TicketTb ticketTb : monthTicketList){
                    Map<String,Object> map = turnTicketInfoToMap(ticketTb);
                    ticketList.add(map);
                    Integer ticketType = ticketTb.getType();
                    if(ticketType == 3){
                        //减免时长
                        mcount++;
                        mtotal += ticketTb.getMoney();
                    }else if(ticketType == 4){
                        //全免
                        ecount++;
                    }else if(ticketType == 5){
                        //减免金额
                        acount++;
                        amount += ticketTb.getUmoney().longValue();
                    }
                }
            }
            Map<String,Object> infomap  = new HashMap<String, Object>();
            infomap.put("total", list.size()+"");
            infomap.put("mcount", (mcount+ecount)+"");//加上全免劵数量
            infomap.put("mtotal", mtotal+"");
            infomap.put("ecount", ecount+"");
            infomap.put("count", (acount+ecount)+"");//加上全免劵数量
            infomap.put("amount", amount+"");

            infomap.put("cell", ticketList);
            logger.info("ticketManager query 商户shop_id:"+shop_id+"查询今日统计成功,集合"+StringUtils.createJson(infomap));
            AjaxUtil.ajaxOutput(response, infomap);
            //http://127.0.0.1/zld/shopticket.do?action=query&shop_id=4&type=4&btime=2015-05-01&etime=2015-06-01&token=69213a6a50aff2b402a1dd13149a7c44
        }else if(action.equals("getinfo")){
            logger.info("ticketManager getinfo 进入获取商户使用优惠券信息的方法");
            ShopTb shopInfo =  shopTicketManagerService.qryShopInfoById(shop_id);
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
            String nowtime= df2.format(System.currentTimeMillis());
            Long btime = TimeTools.getLongMilliSecondFrom_HHMMDD(StringUtils.getFistdayOfMonth())/1000;
            Long etime =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(nowtime+" 23:59:59");
            List<TicketTb> monthTicketList = shopTicketManagerService.queryShopTicketPage(shop_id,btime,etime,-1,0,0);
            Long mcount = 0L;//优惠券-时长已打印数量
            Long mtotal = 0L;//优惠券-时长已打印额度
            Long acount = 0L;//优惠券-金额已打印数量
            Long amount = 0L;//优惠券-金额已打印额度
            Long ecount = 0L;//全免券打印数量
            if(monthTicketList != null && !monthTicketList.isEmpty()){
                for(TicketTb ticketTb : monthTicketList){
                    Integer ticketType = ticketTb.getType();
                    if(ticketType == 3){
                        //减免时长
                        mcount++;
                        mtotal += ticketTb.getMoney();
                    }else if(ticketType == 4){
                        //全免
                        ecount++;
                    }else if(ticketType == 5){
                        //减免金额
                        acount++;
                        amount += ticketTb.getUmoney().longValue();
                    }
                }
            }
            Map<String,Object> shopMap  = turnShopInfoToMap(shopInfo);
            shopMap.put("mcount", mcount+"");
            shopMap.put("mtotal", mtotal+"");
            shopMap.put("ecount", ecount+"");
            shopMap.put("count", acount+"");
            shopMap.put("amount", amount+"");
            logger.info("ticketManager getinfo 查询商户使用优惠券信息返回的数据shopMap:"+shopMap);
            AjaxUtil.ajaxOutput(response, shopMap);
            //http://127.0.0.1/zld/shopticket.do?action=getinfo&shop_id=4&token=69213a6a50aff2b402a1dd13149a7c44
        }else if(action.equals("noscan")){//不扫码使用减免劵
            String car_number = StringUtils.decodeUTF8(RequestUtil.processParams(request, "car_number"));
            logger.info("ticketManager noscan 车牌>>>>>>>>"+car_number+"非扫码用劵");
            Map<String, Object> rMap = new HashMap<String, Object>();
            rMap.put("result",-1);
            rMap.put("error","用劵失败");
            //1 查询在场订单
            ShopTb shopInfo =  shopTicketManagerService.qryShopInfoById(shop_id);
            OrderTb unpayOrder = aliPrepayService.qryUnpayOrder(car_number,shopInfo.getComid());
            if(unpayOrder==null){
                rMap.put("result",-1);
                rMap.put("error","车辆没有入场");
                logger.error("车辆没有入场");
                AjaxUtil.ajaxOutput(response, rMap);
                return;
            }
            //2判断是否用过劵
            Integer count = aliPrepayService.unpayOrderForTicket(unpayOrder.getId());
            logger.error("是否已使用过券："+count);
            if(count>0){
                rMap.put("result",-1);
                rMap.put("error","本次停车已使用过优惠券");
                logger.error("本次停车已使用过优惠券");
                AjaxUtil.ajaxOutput(response, rMap);
                return;
            }
            //3 生成劵
            Map<String,Object> retMap = createTicket(request,response,shop_id);
            if(retMap.get("result") == -1 || retMap.get("result") == -2){
                logger.info("ticketManager noscan 车牌>>>>>>>>"+car_number+"生成减免劵出错，用劵失败");
                AjaxUtil.ajaxOutput(response, retMap);
                return;
            }
            //4 用劵
            QrCodeTb qrCodeTb = qrFilterService.getQrCode((String)retMap.get("result"));
            retMap = noScanUseTicket(request,response,qrCodeTb.getTicketid(),car_number,qrCodeTb.getComid(),unpayOrder);
            AjaxUtil.ajaxOutput(response, retMap);
        }
        return;
    }

    /**
     * 生成减免劵
     * @param request
     * @param response
     * @param shop_id
     * @return
     */
    private Map<String,Object> createTicket(HttpServletRequest request, HttpServletResponse response,Long shop_id){
        Integer time = RequestUtil.getInteger(request, "time", 0);
        Integer type = RequestUtil.getInteger(request, "type", 3);
        Integer amount = RequestUtil.getInteger(request, "amount", 0);
        String remark = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));//备注
        Integer free = 0;//全免劵(张)
        Map<String, Object> rMap = new HashMap<String, Object>();
        if(shop_id == -1){
            rMap.put("result", -1);
            rMap.put("error", "商户编号>>"+shop_id+"不存在");
            return rMap;
        }
        logger.error(">>>>>>>>>>打印优惠券，优惠券类型type:"+type+",优惠时长time："+time+",优惠金额amount："+amount+",商户shop_id:"+shop_id+"备注remark:"+remark);
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
        ShopTb shopInfo =  shopTicketManagerService.qryShopInfoById(shop_id);
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
            amount = 0;
            if(time <= 0){
                logger.error("优惠券额度必须为正数"+",商户shop_id:"+shop_id);
                rMap.put("result", -2);
                rMap.put("error", "优惠券额度必须为正数");
                return rMap;
            }
            if(ticket_limit < (time)){
                logger.error("优惠券额度已用完，还剩余额度"+ticket_limit+",优惠券时长time："+time+",商户shop_id:"+shop_id);
                rMap.put("result", -2);
                rMap.put("error", "优惠券额度不够");
                return rMap;
            }
        }else if(type == 4){//全免券
            time = 0;//全免券
            free = 1;
            amount = 0;
            if(ticketfree_limit <= 0){
                logger.error("全免券额度已用完，还剩余额度"+ticketfree_limit+",商户shop_id:"+shop_id);
                rMap.put("result", -2);
                rMap.put("error", "全免券额度已用完");
                return rMap;
            }
        }else if(type == 5){//优惠券-金额
            time = 0;
            if(amount <= 0){
                logger.error("优惠券额度必须为正数"+",商户shop_id:"+shop_id);
                rMap.put("result", -2);
                rMap.put("error", "优惠券额度必须为正数");
                return rMap;
            }
            if(ticket_money < (amount)){
                logger.error("优惠券额度已用完，还剩余额度"+ticket_money+",优惠券金额amount："+amount+",商户shop_id:"+shop_id);
                rMap.put("result", -2);
                rMap.put("error", "优惠券额度不够");
                return rMap;
            }
        }
        List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
        //取当前最大减免劵的id然后+1
        Long ticketid = shopTicketManagerService.qryMaxTicketId()+1;
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
            ticketTb.setMoney(time);
            BigDecimal amountDecimal = new BigDecimal(amount+"");
            ticketTb.setUmoney(amountDecimal);
            ticketTb.setState(0);
            ticketTb.setComid(shopInfo.getComid());
            ticketTb.setType(type);
            ticketTb.setShopId(shop_id);
            ticketTb.setRemark(remark);
            Integer insertTicket = shopTicketManagerService.insertTicket(ticketTb);

            //生成code
            QrCodeTb qrCodeTb = new QrCodeTb();
            qrCodeTb.setCtime(System.currentTimeMillis()/1000);
            qrCodeTb.setType(5);
            qrCodeTb.setCode(code);
            qrCodeTb.setTicketid(ticketid);
            qrCodeTb.setComid(shopInfo.getComid());
            Integer insertQrcode = shopTicketManagerService.insertQrcode(qrCodeTb);

            //更新商户额度
            ShopTb shopTbInfo = new ShopTb();
            shopTbInfo.setTicketLimit(shopInfo.getTicketLimit()-time);
            shopTbInfo.setTicketfreeLimit(shopInfo.getTicketfreeLimit()-free);
            shopTbInfo.setTicketMoney(shopInfo.getTicketMoney()-amount);
            ShopTb shopConditions = new ShopTb();
            shopConditions.setId(shopInfo.getId());
            Integer updateShop = shopTicketManagerService.updateShopInfo(shopTbInfo,shopConditions);
            logger.error("打印优惠券结果："+insertTicket+","+insertQrcode+","+updateShop+",商户shop_id:"+shop_id);
            if(insertTicket==1 && insertQrcode==1 && updateShop==1){
                rMap.put("result", code);
                return rMap;
            }
        }
        rMap.put("result", -1);
        rMap.put("error","生成减免劵出错，用劵失败");
        return rMap;
    }
    /**
     * 非扫码用劵
     * @param request
     * @param response
     * @param ticketId 减免券编号
     * @param carNumber 车牌号
     * @param comid 车场编号
     * @return
     * @throws Exception
     */
    private Map<String,Object> noScanUseTicket(HttpServletRequest request,HttpServletResponse response,
                                Long ticketId,String carNumber,Long comid,OrderTb unpayOrder) throws Exception{
        Map<String, Object> rMap = new HashMap<String, Object>();
        rMap.put("result",-1);
        rMap.put("error","用劵失败");
        //1 判断减免券是否可用
        Integer ticketType = -1;
        BigDecimal money = new BigDecimal("0");
        Integer duration = 0;
        TicketTb ticketTb =null;
        Long shopId = -1L;
        String error="车场网络异常，请稍候重试";
        if(ticketId!=-1){
            {
                ticketTb= aliPrepayService.qryTicket(ticketId);
                shopId = ticketTb.getShopId();
                if(ticketTb!=null){
                    //判断有效期
                    long limit_day = ticketTb.getLimitDay();
                    if(System.currentTimeMillis()/1000 > limit_day){
                        error="该优惠券已过期";
                        logger.error("该优惠券已过期");
                    }else{
                        Integer state = ticketTb.getState();
                        if(state!=null){
                            if(state==0){
                                Integer type = ticketTb.getType();
                                if(type!=null){
                                    if(type==3){//3减时券
                                        ticketType=1;
                                        duration = ticketTb.getMoney();
                                    }else if(type==5){//减免金额劵
                                        ticketType=0;
                                        money = ticketTb.getUmoney();
                                        logger.error("该优惠劵："+ticketId+",money："+money);
                                    }else if(type==4){//全免劵
                                        ticketType=2;
                                        logger.error("该优惠劵："+ticketId+",money："+money);
                                    }else {
                                        error="优惠券无效";
                                        rMap.put("result",-1);
                                        rMap.put("error",error);
                                        logger.error(error);
                                    }
                                }else {
                                    error="优惠券无效";
                                    rMap.put("result",-1);
                                    rMap.put("error",error);
                                    logger.error(error);
                                }
                            }else if(state==2){
                                error="该优惠券已过期";
                                rMap.put("result",-1);
                                rMap.put("error",error);
                                logger.error(error);
                            }else if(state==1){
                                error="该优惠券已使用";
                                rMap.put("result",-1);
                                rMap.put("error",error);
                                logger.error(error);
                            }
                        }else {
                            error="该优惠券已使用";
                            rMap.put("result",-1);
                            rMap.put("error",error);
                            logger.error(error);
                        }
                    }
                }else{
                    error="优惠券不存在";
                    rMap.put("result",-1);
                    rMap.put("error",error);
                    logger.error(error);
                }
            }
        }else {
            error="优惠券不存在";
            rMap.put("result",-1);
            rMap.put("error",error);
            logger.error(error);
        }
        //3 下发减免劵
        boolean isSend = false;//是否已发送的SDK
        if(ticketType!=-1){//取到了券信息并且有通道，发送消息到sdk
            Channel channel = NettyChannelMap.get(commonUtils.getChannel(ticketTb.getComid()+""));
            if(channel!=null){
                Map<String, Object> messageMap = new HashMap<String, Object>();
                messageMap.put("service_name", "deliver_ticket");
                messageMap.put("ticket_id", ticketId+"");
                messageMap.put("create_time", ticketTb.getCreateTime());
                messageMap.put("limit_day", ticketTb.getLimitDay());
                messageMap.put("duration", duration);
                messageMap.put("money", money+"");
                messageMap.put("car_number", carNumber);
                messageMap.put("ticket_type", ticketType);
                ShopTb shopTb = new ShopTb();
                shopTb.setId(shopId);
                shopTb = (ShopTb) commonDao.selectObjectByConditions(shopTb);
                messageMap.put("shop_name",shopTb.getName());
                if(ticketType==1){//时长减免，加上减免单位
                    logger.error("发送减免券得到商户信息："+shopTb);
                    messageMap.put("ticket_unit", shopTb.getTicketUnit());
                }
                messageMap.put("order_id", unpayOrder.getOrderIdLocal());
                messageMap.put("remark", ticketTb.getRemark());
                String mesg = StringUtils.createJson(messageMap);
                isSend=commonUtils.doBackMessage(mesg, channel);
                logger.error("发送减免券数据到SDK："+mesg+",ret:"+isSend);
                if(isSend){
                    // 4查询车场是否确认使用减免劵
                    int temp = 0;
                    while (temp<11){
                        Thread.sleep(700);
                        TicketTb ticket = new TicketTb();
                        ticket.setId(ticketId);
                        ticket = (TicketTb)commonDao.selectObjectByConditions(ticket);
                        if(ticket.getState()==1 && ticket.getOrderid() !=null ){
                            rMap.put("result",1);
                            rMap.put("error","使用减免劵成功..==>");
                            logger.info("使用减免劵成功..==>");
                            return rMap;
                        }
                        temp++;
                    }
                    rMap.put("result",-1);
                    rMap.put("error","车场确认使用减免劵返回超时..==>");
                    logger.info("车场确认使用减免劵超时返回..==>");
                }

            }
        }
        return rMap;
    }


    /**
     * 验证token是否有效
     * @param token
     * @return uin
     */
    private Long validToken(String token) {
        UserSessionTb userSessionConditions = new UserSessionTb();
        userSessionConditions.setToken(token);
        UserSessionTb userSessionTb = shopTicketManagerService.qryUserSessionInfo(userSessionConditions);
        Long uin = null;
        if(userSessionTb!=null&&userSessionTb.getUin()!=null){
            uin = userSessionTb.getUin();
        }
        return uin;
    }

    /**
     * shopTb转换成map
     * @return
     */
    private Map<String,Object> turnShopInfoToMap(ShopTb shop){
            Map<String,Object> map = new HashMap<String,Object>();
            if(shop == null ){
                return map;
            }
            map.put("id",shop.getId()+"");
            map.put("name",shop.getName()+"");
            map.put("address",shop.getAddress()+"");
            map.put("mobile",shop.getMobile()+"");
            map.put("phone",shop.getPhone()+"");
            map.put("comid",shop.getComid()+"");
            map.put("ticket_limit",shop.getTicketLimit()+"");
            map.put("descrition",shop.getDescription()+"");
            map.put("state",shop.getState()+"");
            map.put("create_time",shop.getCreateTime()+"");
            map.put("ticketfree_limit",shop.getTicketfreeLimit()+"");
            map.put("ticket_type",shop.getTicketType()+"");
            map.put("ticket_money",shop.getTicketMoney()+"");
            map.put("default_limit",shop.getDefaultLimit()+"");
            map.put("discount_percent",shop.getDiscountPercent()+"");
            map.put("discount_money",shop.getDiscountMoney()+"");
            return map;
    }

    /**
     * ticketTb转换成map
     * @return
     */
    private Map<String,Object> turnTicketInfoToMap(TicketTb ticket){
        Map<String,Object> map = new HashMap<String,Object>();
        if(ticket == null ){
            return map;
        }
        map.put("id",ticket.getId());
        map.put("create_time",ticket.getCreateTime());
        map.put("limit_day",ticket.getLimitDay());
        map.put("money",ticket.getMoney());
        map.put("state",ticket.getState());
        map.put("uin",ticket.getUin());
        map.put("comid",ticket.getComid());
        map.put("utime",ticket.getUtime());
        map.put("umoney",ticket.getUmoney());
        map.put("type",ticket.getType());
        map.put("orderid",ticket.getOrderid());
        map.put("bmoney",ticket.getBmoney());
        map.put("wxp_orderid",ticket.getWxpOrderid());
        map.put("shop_id",ticket.getShopId());
        map.put("resources",ticket.getResources());
        map.put("is_back_money",ticket.getIsBackMoney());
        map.put("pmoney",ticket.getPmoney());
        map.put("need_sync",ticket.getNeedSync());
        map.put("ticket_id",ticket.getTicketId());
        map.put("operate_user",ticket.getOperateUser());
        map.put("state_zh",ticket.getStateZh());
        map.put("car_number",ticket.getCarNumber());
        map.put("type_zh",ticket.getTypeZh());
        map.put("remark",ticket.getRemark());
       return map;
    }


}
