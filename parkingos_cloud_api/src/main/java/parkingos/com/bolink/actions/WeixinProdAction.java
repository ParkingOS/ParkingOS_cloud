package parkingos.com.bolink.actions;

import com.alibaba.fastjson.JSONObject;
import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import parkingos.com.bolink.beans.ComInfoTb;
import parkingos.com.bolink.component.CommonComponent;
import parkingos.com.bolink.constant.Constants;
import parkingos.com.bolink.constant.WeixinConstants;
import parkingos.com.bolink.dto.UnionInfo;
import parkingos.com.bolink.service.WeixinProdService;
import parkingos.com.bolink.utlis.*;
import parkingos.com.bolink.vo.ProdPriceView;
import parkingos.com.bolink.vo.ProdView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信月卡续费接口
 */
@Controller
public class WeixinProdAction {
    Logger logger = Logger.getLogger(WeixinProdAction.class);
    @Autowired
    WeixinProdService weixinProdService;

    @Autowired
    CommonComponent commonComponent;
    @Autowired
    CommonDao commonDao;

    /**
     * 获取月卡列表数据
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("getwxprodlist")
    public String getProdList(HttpServletRequest request, HttpServletResponse response){
        Long uin = RequestUtil.getLong(request,"uin",-1L);
        List<ProdView> prodList = weixinProdService.getProdList(uin);
        AjaxUtil.ajaxOutputWithSnakeCase(response,prodList);
        return null;
    }

    /**
     * 获取月卡价格
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("getprodprice")
    public String getProdPrice(HttpServletRequest request, HttpServletResponse response){
        String cardId = RequestUtil.getString(request, "card_id");
        String startTime = RequestUtil.getString(request, "start_time");
        Long beginTime = TimeTools.getLongMilliSecondFrom_HHMMDD(startTime)/1000;
        Long comId = RequestUtil.getLong(request, "com_id", -1l);
        Long uin = RequestUtil.getLong(request,"uin",-1L);
        Integer months = RequestUtil.getInteger(request, "months", -1);
        logger.info("getprodprice:"+cardId+"~"+startTime+"~"+comId+"~"+uin+"~"+months);
        ProdPriceView prodPrice = weixinProdService.getProdPrice(comId, cardId, beginTime, months);
        logger.info(prodPrice);
        AjaxUtil.ajaxOutputWithSnakeCase(response,prodPrice);
        return null;
    }

    /**
     * 前往选择月卡续费信息的页面
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("tobuyprod")
    public String toBuyProd(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        Long uin = RequestUtil.getLong(request,"uin",-1L);
        String cardId = RequestUtil.getString(request, "card_id");
        Long comId = RequestUtil.getLong(request, "com_id", -1l);
        Long endTime = RequestUtil.getLong(request, "end_time", -1l);
        Long carOwnerProductId = RequestUtil.getLong(request, "car_owner_product_id", -1l);
        String prodId = RequestUtil.getString(request, "prod_id");
        Integer type = RequestUtil.getInteger(request, "type", 0);//0:购买 1：续费
        String carNumber = StringUtils.decodeUTF8(RequestUtil.getString(request,"car_number"));
        logger.info("编yici码："+carNumber+"编2次"+StringUtils.decodeUTF8(carNumber));
        logger.info("月卡续费车牌"+carNumber);
        ComInfoTb comInfoTb = new ComInfoTb();
        comInfoTb.setId(comId);
        comInfoTb = (ComInfoTb) commonDao.selectObjectByConditions(comInfoTb);
        logger.info(comInfoTb);
        String parkName = comInfoTb.getCompanyName();
//        String parkName = RequestUtil.getString(request,"park_name");
//        logger.info(parkName);
//        parkName = StringUtils.decodeUTF8(parkName);
//        logger.info(parkName);
//        parkName = new String(parkName.getBytes("GBK"), "utf-8");
        logger.info("tobuyprod=>"+uin+"~"+cardId+"~"+comId+"~"+carOwnerProductId+"~"+prodId+"~"+type+"~"+endTime+"~"+parkName+"~"+carNumber);

        String bTime = TimeTools.getDate_YY_MM_DD();
        //获取月卡结束时间
        if(endTime > TimeTools.getToDayBeginTime()){
            bTime = TimeTools.getTimeStr_yyyy_MM_dd((endTime+60*60*24)*1000);
        }

        String[] minStr = bTime.split("-");
        request.setAttribute("minyear", Integer.valueOf(minStr[0]));
        request.setAttribute("minmonth", Integer.valueOf(minStr[1])-1);
        request.setAttribute("minday", Integer.valueOf(minStr[2]));
        request.setAttribute("btime", bTime);
        request.setAttribute("exptime", -1);
        request.setAttribute("pname", -1);
        request.setAttribute("maxyear", 2020);
        request.setAttribute("maxmonth", 12);
        request.setAttribute("maxday", 31);
        request.setAttribute("title", "月卡续费");
        request.setAttribute("money", "0.0");
        request.setAttribute("com_id", comId);
        request.setAttribute("card_id", StringUtils.encodeUTF8(cardId));
        request.setAttribute("prod_id", prodId);
        request.setAttribute("uin", uin);
        request.setAttribute("park_name", parkName);
        request.setAttribute("car_number",StringUtils.encodeUTF8(carNumber));
        return "wxpublic/buyprod";
    }

    /**
     * 前往第三方支付月卡
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("topayprod")
    public String toPayProd(HttpServletRequest request, HttpServletResponse response) throws  Exception{
        logger.info("进入第三方支付月卡方法");
        String cardId = StringUtils.decodeUTF8(RequestUtil.getString(request, "card_id"));
        Long comId = RequestUtil.getLong(request, "com_id", -1L);
        Integer months = RequestUtil.getInteger(request, "months", 1);
        Long uin = RequestUtil.getLong(request,"uin",-1L);
        String tradeNo = RequestUtil.processParams(request, "trade_no");
        Double price = RequestUtil.getDouble(request, "price", -1d);
        String startTime = RequestUtil.processParams(request, "start_time");
        Long sTime = TimeTools.getLongMilliSecondFrom_HHMMDD(startTime)/1000;
        String carNumber = StringUtils.decodeUTF8(RequestUtil.getString(request,"car_number"));
//        logger.info("车牌解码"+StringUtils.encodeUTF8(carNumber)+"~~~"+StringUtils.encodeUTF8(StringUtils.encodeUTF8(carNumber)));
//        logger.info("aaaaaaa"+StringUtils.decodeUTF8(carNumber)+"~~"+StringUtils.decodeUTF8(StringUtils.decodeUTF8(carNumber)));
        logger.info("topayprod:"+uin+"~"+cardId+"~"+comId+"~"+months+"~"+tradeNo+"~"+price+"~"+startTime+"~"+carNumber);
        String openid =   RequestUtil.processParams(request, "openid");
        String appid = "";
        Cookie[] cookies = request.getCookies();//这样便可以获取一个cookie数组
        if(cookies!=null){
            for(Cookie cookie : cookies){
                if(cookie!=null&&cookie.getName().equals("userappid"))
                    appid =cookie.getValue();
            }
        }
        //userAgent = "E6A32CR";
        logger.info("cookie中用户appid="+appid);
        String secert = Defind.getProperty(appid);
        if(Check.isEmpty(appid)||Check.isEmpty(secert)){
            appid = Defind.getProperty("WXPUBLIC_APPID");
            secert = Defind.getProperty("WXPUBLIC_SECRET");
        }
        boolean isHaveWxAppid = "1".equals(WeixinConstants.IS_HAVE_WXAPPID);
        logger.info("openid:"+openid+",isHaveWxAppid:"+isHaveWxAppid);
        if(openid.equals("")&&isHaveWxAppid){
            String code = RequestUtil.processParams(request, "code");
            String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+appid+"&secret="+secert+"&code="+code+"&grant_type=authorization_code";
            String result = CommonUtils.httpsRequest(access_token_url, "GET", null);
            JSONObject map = JSONObject.parseObject(result);
            logger.error("third pay map:"+map);
            if(map == null || map.get("errcode") != null){
                logger.error("payMonthpay:>>>>>>>>>>>>获取openid失败....,重新获取>>>>>>>>>>>");
                String redirect_url = "http%3a%2f%2f"+Defind.getProperty("DOMAIN")+"%2fzld%2ftopayprod%3f" +
                        "price%3d"+price+
                        "%26months%3d"+months+
                        "%26card_id%3d"+StringUtils.encodeUTF8(StringUtils.encodeUTF8(StringUtils.encodeUTF8(cardId)))+
                        "%26com_id%3d"+comId+
                        "%26car_number%3d"+StringUtils.encodeUTF8(StringUtils.encodeUTF8(StringUtils.encodeUTF8(carNumber)))+
                        "%26start_time%3d"+startTime+
                        "%26trade_no%3d"+tradeNo;
                String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
                        + appid
                        + "&redirect_uri="
                        + redirect_url
                        + "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
                logger.error("payMonthpay:重新调用微信，取OPENID，url:"+url);
                response.sendRedirect(url);
                return null;
            }
            openid = (String)map.get("openid");
            logger.error("third pay: 获取OPENID:"+openid);
        }

        boolean isThirdPay = "1".equals(WeixinConstants.IS_TO_THIRD_WXPAY);
        if(price<=0){
            //价格小于0
            return null;
        }

        //根据车场编号找到对应的厂商平台编号和ukey
        UnionInfo unionInfo = commonComponent.getUnionInfo(comId);
        logger.info("unionInfo:"+unionInfo);
        Long unionId = unionInfo.getUnionId();
        String unionKey = unionInfo.getUnionKey();

        //根据comid获得车场信息
        ComInfoTb comInfoTb = new ComInfoTb();
        comInfoTb.setId(comId);
        comInfoTb = (ComInfoTb)commonDao.selectObjectByConditions(comInfoTb);

        String parkid ="";

        //组织公众号基本参数
        Map<String, Object> attachMap = new HashMap<String, Object>();
        attachMap.put("type", 11);//第三方月卡续费
        attachMap.put("params", sTime+"__"+months+"__"+tradeNo+"__"+price+"__"+comId+"__"+unionId+"__"+cardId+"__"+carNumber);

        if(isThirdPay){
            // 组织第三方参数,签名
            String attach = JSONObject.toJSONString(attachMap);
            // 提供支付完成回调页面
            String backUrl = "http://"+ Constants.DOMAIN+"/zld/thirdsuccess";
            Map<String, Object> paramsMap = new HashMap<String, Object>();
            paramsMap.put("attch", attach);
            paramsMap.put("union_id", unionId);
            paramsMap.put("money", price);
            if(comInfoTb.getBolinkId()!=null&&!"".equals(comInfoTb.getBolinkId())){
                parkid = comInfoTb.getBolinkId();
                paramsMap.put("park_id", parkid);
            }else {
                paramsMap.put("park_id", comId);
            }
//            paramsMap.put("park_id", comId);
            paramsMap.put("trade_no", tradeNo);
            paramsMap.put("backurl", backUrl);

            logger.error("topayprod:" + paramsMap);
//            paramsMap.put("appid",Defind.getProperty("WXPUBLIC_APPID"));
//            paramsMap.put("openid",openid);
            // 获取unionKey
            String sign = CheckUtil.createSign(paramsMap, unionKey);

            String url = "";
            if(parkid!=null&&!"".equals(parkid)){
                url = Constants.UNIONIP +
                        "/handlemonthpay?openid="+openid+"&appid="+appid+"&money="+price+"&union_id="+unionId+"&sign="+sign+"&backurl="+backUrl+"&park_id="+parkid+"&trade_no="+tradeNo+"&attch="+StringUtils.encodeUTF8(attach);
            }else{
                url = Constants.UNIONIP +
                        "/handlemonthpay?openid="+openid+"&appid="+appid+"&money="+price+"&union_id="+unionId+"&sign="+sign+"&backurl="+backUrl+"&park_id="+comId+"&trade_no="+tradeNo+"&attch="+StringUtils.encodeUTF8(attach);
            }

//            String url = Constants.UNIONIP +
//                    "/handlemonthpay?openid="+openid+"&appid="+appid+"&money="+price+"&union_id="+unionId+"&sign="+sign+"&backurl="+backUrl+"&park_id="+comId+"&trade_no="+tradeNo+"&attch="+attach;
            logger.info("topayprod:"+url);
            try {
                response.sendRedirect(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }else{
            //本平台微信公众号支付

            return null;
        }
    }

    /**
     * 返回支付成功页面
     * @param request
     * @return
     */
    @RequestMapping("thirdsuccess")
    public String getPaySuccessPage(HttpServletRequest request){
        logger.error("thirdsuccess 进入成功回调页面");
        String content = "月卡续费成功!系统处理可能会有延迟</br>请稍后在月卡页面查看续费情况";
        request.setAttribute("content", content);
        return "wxpublic/thirdsuccess";
    }

}
