package parkingos.com.bolink.actions;

import com.alibaba.fastjson.JSONObject;
import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import parkingos.com.bolink.beans.FixCodeTb;
import parkingos.com.bolink.beans.QrCodeTb;
import parkingos.com.bolink.beans.ShopTb;
import parkingos.com.bolink.constant.WeixinConstants;
import parkingos.com.bolink.service.QrFilterService;
import parkingos.com.bolink.utlis.AjaxUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 微信我的账户接口
 */
@Controller
@RequestMapping(value="/qr")
public class QrFilterAction {
    Logger logger = Logger.getLogger(QrFilterAction.class);
    @Autowired
    QrFilterService qrFilterService;
    @Autowired
    CommonDao commonDao;

    /**
     * 二维码过滤
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value="/c/{code}")
    public void getLogoFile(@PathVariable("code") String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("减免劵>>>>>>>>>>>>>>code:"+code);
        if(!code.startsWith("z")&&!code.startsWith("d")&&!code.startsWith("B0liNk"))
            return ;

        //1 根据code获取二维码信息
        QrCodeTb qrCodeTb = qrFilterService.getQrCode(code);
        logger.error("减免劵>>>>>>>>>>>>>>qrCodeTb:"+qrCodeTb);
        Long orderId = -1L;
        if(qrCodeTb!=null){
            Integer type = qrCodeTb.getType();
            //type=5 减免劵二维码
            if(type==5){
                String userAgent = request.getHeader("user-agent");//"AlipayClient";//
                String host = WeixinConstants.WXPUBLIC_REDIRECTURL;
                if(userAgent.indexOf("AlipayClient")!=-1){//支付宝扫码
                    String _rurl = "http://"+host+"/zld/aliprepay?action=useshopticket&parkid="+
                            qrCodeTb.getComid()+"&ticketid="+qrCodeTb.getTicketid();
                    logger.error(_rurl);
//                    http%3a%2f%2ftest.bolink.club%2fzld%2faliprepay%3faction%3dwxuseshopticket%26from%3d%26parkid%3d21782%26ticketid%3d4365143
                    response.sendRedirect(_rurl);
                }else {
                    String rUrl = "http%3a%2f%2f"+host+"%2fzld%2faliprepay%3faction%3dwxuseshopticket%26from%3d"+""
                            +"%26parkid%3d"+qrCodeTb.getComid()+"%26ticketid%3d"+qrCodeTb.getTicketid();
                    String _rUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+WeixinConstants.WXPUBLIC_APPID+"&redirect_uri="+rUrl+
                            "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
                    logger.error(rUrl);
                    response.sendRedirect(_rUrl);
                }
            }
        }
        AjaxUtil.ajaxOutput(response, "code:"+code);
    }

    /*
    * 验证扫描验证码是不是可以下发券
    *
    * */
    @RequestMapping(value="/d/{code}")
    public String  checkFixCode(@PathVariable("code") String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        JSONObject result = new JSONObject();
        logger.error("固定码>>>>>>>>>>>>>>code:"+code);
        if(!code.startsWith("z")&&!code.startsWith("d")&&!code.startsWith("B0liNk"))
            return null;
        FixCodeTb fixCodeTb = qrFilterService.getFixCode(code);
        logger.error("减免劵>>>>>>>>>>>>>>fixCodeTb:"+fixCodeTb);

        if(fixCodeTb!=null){
            if(fixCodeTb.getState()==1){
                request.setAttribute("state", 0);
                request.setAttribute("error", "优惠券已经不能领取，请联系优惠券管理员");
                return "aliprepay/error";
//                result.put("state",0);
//                result.put("error","优惠券已经不能领取，请联系优惠券管理员");
//                StringUtils.ajaxOutput(response,result.toJSONString());
//                return null;
            }

            if(fixCodeTb.getEndTime()<System.currentTimeMillis()/1000){//过期
                request.setAttribute("state", 0);
                request.setAttribute("error", "优惠券已经不能领取，请联系优惠券管理员");
                return "aliprepay/error";
//                result.put("state",0);
//                result.put("error","优惠券已经不能领取，请联系优惠券管理员");
//                StringUtils.ajaxOutput(response,result.toJSONString());
//                return null;

            }
                Long shopid = fixCodeTb.getShopId();
                ShopTb shopTb = new ShopTb();
                shopTb.setId(shopid);
                shopTb = (ShopTb)commonDao.selectObjectByConditions(shopTb);
                Long comid = -1L;
                if(shopTb!=null){
                    comid = shopTb.getComid();
                }
                String userAgent = request.getHeader("user-agent");//"AlipayClient";//
                String host = WeixinConstants.WXPUBLIC_REDIRECTURL;
                if(userAgent.indexOf("AlipayClient")!=-1){//支付宝扫码
                    String _rurl = "http://"+host+"/zld/aliprepay?action=checkFixCode&parkid="+
                            comid+"&fixcode="+code;
                    logger.error(_rurl);
//                    http%3a%2f%2ftest.bolink.club%2fzld%2faliprepay%3faction%3dwxuseshopticket%26from%3d%26parkid%3d21782%26ticketid%3d4365143
                    response.sendRedirect(_rurl);
                }else {
                    String rUrl = "http%3a%2f%2f"+host+"%2fzld%2faliprepay%3faction%3dwxcheckFixCode%26from%3d"+""
                            +"%26parkid%3d"+comid+"%26fixcode%3d"+code;
                    String _rUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+WeixinConstants.WXPUBLIC_APPID+"&redirect_uri="+rUrl+
                            "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
                    logger.error(rUrl);
                    response.sendRedirect(_rUrl);
                }

        }
        return null;
    }

}
