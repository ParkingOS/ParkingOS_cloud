package parkingos.com.bolink.actions;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import parkingos.com.bolink.beans.ComInfoTb;
import parkingos.com.bolink.component.CommonComponent;
import parkingos.com.bolink.constant.Constants;
import parkingos.com.bolink.dto.UnionInfo;
import parkingos.com.bolink.service.WeixinCurOrderService;
import parkingos.com.bolink.utlis.AjaxUtil;
import parkingos.com.bolink.utlis.CheckUtil;
import parkingos.com.bolink.utlis.RequestUtil;
import parkingos.com.bolink.utlis.StringUtils;
import parkingos.com.bolink.vo.CurOrderListView;
import parkingos.com.bolink.vo.LockCarView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信在场订单接口
 */
@Controller
public class WeixinCurOrderAction {
    Logger logger = Logger.getLogger(WeixinCurOrderAction.class);

    @Autowired
    WeixinCurOrderService weixinCurOrderService;

    @Autowired
    CommonComponent commonComponent;

    /**
     * 获取在场订单数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("getwxcurorderlist")
    public String getWxCurOrderList(HttpServletRequest request, HttpServletResponse response) {
        Long uin = RequestUtil.getLong(request, "uin", -1L);
        logger.info("weixin search orders .>>>>>>>");
        Integer pageNum = RequestUtil.getInteger(request, "page", 1);
        Integer pageSize = RequestUtil.getInteger(request, "size", 20);
        //获取用户在场订单
        List<CurOrderListView> curOrderList = weixinCurOrderService.getCurOrderList(uin);
        logger.info("weixin orders :"+curOrderList);
        AjaxUtil.ajaxOutputWithSnakeCase(response, curOrderList);
        return null;
    }

    /**
     * 前往第三方预付订单
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("toprepaycurorder")
    public String toPrepayOrder(HttpServletRequest request, HttpServletResponse response) {
        Long comId = RequestUtil.getLong(request, "com_id", -1L);
        ComInfoTb comInfoTb = commonComponent.getComInfo(comId);
        UnionInfo unionInfo = commonComponent.getUnionInfo(comId);
        Long unionId = unionInfo.getUnionId();
        String orderId = RequestUtil.getString(request, "order_id");
        String carNumber = StringUtils.decodeUTF8(RequestUtil.getString(request, "car_number"));
        logger.info("toprepaycurorder=>" + comId + "~" + orderId + "~" + carNumber);
        String backUrl = "http://"+ Constants.DOMAIN+"/zld/thirdsuccess";

        String parkid = "";
        //组织参数
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("order_id", orderId);
        if(comInfoTb.getBolinkId()!=null&&!"".equals(comInfoTb.getBolinkId())){
            parkid = comInfoTb.getBolinkId();
            paramsMap.put("park_id", parkid);
        }else {
            paramsMap.put("park_id", comId);
        }
        logger.error("去泊链预付金额parkid:"+parkid+"~~~~comid:"+comId);
        paramsMap.put("plate_number", StringUtils.encodeUTF8(carNumber));
        paramsMap.put("union_id", unionId);
        String sign = CheckUtil.createSign(paramsMap, unionInfo.getUnionKey());

        //重定向到泊链第三方预付中转接口
        String unionIp = Constants.UNIONIP;
        //unionIp = "http://jarvisqh.vicp.io/api-web";
        String url = "";
        if(parkid!=null&&!"".equals(parkid)){
            url = unionIp + "/toorderprepay?park_id=" + parkid + "&union_id=" + unionId +
                    "&order_id=" + orderId + "&car_number=" + StringUtils.encodeUTF8(carNumber) + "&sign=" + sign + "&backurl=" + backUrl;
        }else{
            url = unionIp + "/toorderprepay?park_id=" + comId + "&union_id=" + unionId +
                    "&order_id=" + orderId + "&car_number=" + StringUtils.encodeUTF8(carNumber) + "&sign=" + sign + "&backurl=" + backUrl;
        }
//        String url = unionIp + "/toorderprepay?park_id=" + comId + "&union_id=" + unionId +
//                "&order_id=" + orderId + "&car_number=" + StringUtils.encodeUTF8(carNumber) + "&sign=" + sign + "&backurl=" + backUrl;
        logger.info("toprepaycurorder=>" + url);
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解/锁车
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("lockcar")
    public String lockCar(HttpServletRequest request, HttpServletResponse response) {
        Integer lockStatus = RequestUtil.getInteger(request, "lock_status", -1);
        Long oid = RequestUtil.getLong(request, "oid", -1L);
        logger.info("lockcar:" + lockStatus + "~" + oid );
        //处理解锁车业务
        LockCarView lockCarView = weixinCurOrderService.doLockCar(lockStatus, oid);
        AjaxUtil.ajaxOutputWithSnakeCase(response,lockCarView);
        return null;
        //return "redirect:/wxpaccount.do?forward=topresentorderlist";
    }

    /**
     * 发送验证码
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("sendcode")
    public String sendCode(HttpServletRequest request, HttpServletResponse response) {

        String openid = request.getSession().getAttribute("openid")+"";
        String mobile = RequestUtil.getString(request,"mobile");
        logger.info("sendcode:~~~~~~"+openid+"~~~~~~"+mobile);
        //处理发送验证码
        int result = weixinCurOrderService.sendCode(openid,mobile);
        AjaxUtil.ajaxOutputWithSnakeCase(response,result);
        return null;
    }
//    validcode


    /**
     * 验证验证码
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("validcode")
    public String validCode(HttpServletRequest request, HttpServletResponse response) {
        logger.info("validCode:~~~~~~进入验证方法");

        String openid = request.getSession().getAttribute("openid")+"";
        String code = RequestUtil.getString(request,"code");
        String mobile = RequestUtil.getString(request,"mobile");
        int result = weixinCurOrderService.validCode(code,mobile,openid);
        AjaxUtil.ajaxOutputWithSnakeCase(response,result);
        return null;
    }



    /**
     * 去添加手机号页面
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("toadduser")
    public String toAddUser(HttpServletRequest request, HttpServletResponse response) {
        String openid = request.getSession().getAttribute("openid")+"";
        Integer lockStatus = RequestUtil.getInteger(request, "lock_status", -1);
        Long oid = RequestUtil.getLong(request, "oid", -1L);
        logger.info("toadduser:" + lockStatus + "~" + oid + "~~~~~~"+openid);
        request.setAttribute("lock_status",lockStatus);
        request.setAttribute("oid",oid);


//        logger.info("toadduser:" + lockStatus + "~" + oid + "~~~~~~"+openid);
//        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + WeixinConstants.WXPUBLIC_APPID + "&secret=" + WeixinConstants.WXPUBLIC_SECRET;
//        String result = HttpRequestProxy.doGet(url,"UTF-8");
//        logger.error("用户是不是关注公众号token===="+result);
//
//        JSONObject jsonObject = JSONObject.parseObject(result);
//
//
//        String resurl = "https://api.weixin.qq.com/cgi-bin/user/info?access_token="+jsonObject.get("access_token")+"&openid="+openid+"&lang=zh_CN";
//        String subcribe = HttpRequestProxy.doGet(resurl,"UTF-8");
//        logger.error("用户是不是关注公众号sub===="+subcribe);
//
//
//        String menuUrl = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token="+jsonObject.get("access_token");
//        String menures = HttpRequestProxy.doGet(menuUrl,"UTF-8");
//        JSONObject menuObject = JSONObject.parseObject(menures);
//        if(menuObject.get("subscribe")==1){//已经关注}
//        logger.error("公众号菜单选项===="+menures);



        LockCarView lockCarView = weixinCurOrderService.checkMobile(openid);

        if(lockCarView.getState()==1){
            lockCar(request,response);
        }

        lockCarView.setOpenid(openid);
        lockCarView.setLockStatus(lockStatus);
        lockCarView.setOid(oid);
        logger.info("toadduser:" + lockCarView);
        AjaxUtil.ajaxOutputWithSnakeCase(response,lockCarView);
        return null;
    }



    @RequestMapping("adduser")
    public String addUser(HttpServletRequest request, HttpServletResponse response) {
        String openid = request.getSession().getAttribute("openid")+"";
        Integer lockStatus = RequestUtil.getInteger(request, "lock_status", -1);
        Long oid = RequestUtil.getLong(request, "oid", -1L);


        request.setAttribute("lock_status",lockStatus);
        request.setAttribute("oid",oid);
        return "/wxpublic/adduser";
    }
}
