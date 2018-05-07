package parkingos.com.bolink.actions;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import parkingos.com.bolink.beans.CarInfoTb;
import parkingos.com.bolink.beans.UserInfoTb;
import parkingos.com.bolink.constant.Constants;
import parkingos.com.bolink.constant.WeixinConstants;
import parkingos.com.bolink.service.WeixinAccountService;
import parkingos.com.bolink.utlis.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 微信我的账户接口
 */
@Controller
public class WeixinAccountAction {
    Logger logger = Logger.getLogger(WeixinAccountAction.class);
    @Autowired
    WeixinAccountService weixinAccountService;

    /**
     * 前往微信我的账户
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping("wxpaccount.do")
    public String toAccount(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String bforward = RequestUtil.getString(request,"forward");//topresentorderlist,toparkprod
        logger.info("wxpaccount>>>>>>>>>>>"+bforward);
        //1.获取openid
        String appid = "";//RequestUtil.getString(request,"appid");
        String forward = "";
        if(bforward.indexOf("_")!=-1){
            forward = bforward.split("_")[1];
            appid = bforward.split("_")[0];
        }else
            forward = bforward;
        String local = "0";// Constants.WX_LOCAL;
        logger.error("wxpaccount>>>>>>>>>>>appid>>>>>>>"+appid+",forward:"+forward);
        String secert = Defind.getProperty(appid);
        logger.error("wxpaccount>>>>>>>>>>>appid>>>>>>>"+appid+">>>>>secert:"+secert);
        String cloudAPPID = WeixinConstants.WXPUBLIC_APPID;
        String cloudSECERT= WeixinConstants.WXPUBLIC_SECRET;
        if(Check.isEmpty(appid)|| Check.isEmpty(secert)){
            appid =cloudAPPID;
            secert =cloudSECERT;
        }else{
            Cookie cookie = new Cookie("userappid",appid);
            cookie.setMaxAge(86400*100);//暂定900天
            cookie.setPath("/");
            response.addCookie(cookie);
            logger.error("appid 已保存到cookie>>>>>>"+appid+">>>");
        }
        String openId = "";

        if("1".equals(local)){
            openId = "o809KwgZVcVlGERyPsuHxfgO3gX0";
        }else{
            String code = RequestUtil.processParams(request, "code");
            String accessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+cloudAPPID+"&secret="+cloudSECERT+"&code="+code+"&grant_type=authorization_code";
            String result = WexinPublicUtil.httpsRequest(accessTokenUrl, "GET", null);
            JSONObject map = null;
            JSONObject wxUserInfo = null;
            if(result!=null){
                map = JSONObject.parseObject(result);
            }
            String fromScope = "snsapi_userinfo";
            if(CheckUtil.isNotNull(forward)){
                fromScope = "snsapi_base";
            }
            if(map ==null || map.get("errcode") != null){
                logger.error("获取openid失败....");
                String redirect_url = "http%3a%2f%2f"+ WeixinConstants.WXPUBLIC_REDIRECTURL+"%2fzld%2fwxpaccount.do?forward="+forward;
                String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
                        + cloudAPPID
                        + "&redirect_uri="
                        + redirect_url
                        + "&response_type=code&scope="+fromScope+"&state=123#wechat_redirect";
                try {
                    response.sendRedirect(url);
                } catch (IOException e) {
                    logger.error("获取公众号openid重定向异常=>"+e.getMessage());
                    return null;
                }
                return null;
            }
            openId = map.getString("openid");
            String access_token = map.getString("access_token");
            String scope = map.getString("scope");
            if(scope.equals("snsapi_userinfo")){
                String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token="+access_token+"&openid="+openId+"&lang=zh_CN";
                result = WexinPublicUtil.httpsRequest(userInfoUrl, "GET", null);
                if(result != null){
                    wxUserInfo = JSONObject.parseObject(result);
                }
                if(wxUserInfo ==null || wxUserInfo.get("errcode") != null){
                    logger.error("获取openid失败....");
                    String redirect_url = "http%3a%2f%2f"+ WeixinConstants.WXPUBLIC_REDIRECTURL+"%2fzld%2fwxpaccount.do?forward="+forward;
                    String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
                            + WeixinConstants.WXPUBLIC_APPID
                            + "&redirect_uri="
                            + redirect_url
                            + "&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
                    try {
                        response.sendRedirect(url);
                    } catch (IOException e) {
                        logger.error("获取公众号openid重定向异常=>"+e.getMessage());
                    }
                    return null;
                }
                String wxName = wxUserInfo.getString("nickname");
                String wxImg = wxUserInfo.getString("headimgurl");
                request.setAttribute("wximg", wxImg);
                request.setAttribute("wxname", wxName);
                logger.info(wxImg+"-"+wxName);
            }
        }

        if(openId==null){
            //跳转至错误页面
            return "";
        }
        logger.info("openid:"+openId);
        //2.查询用户信息,无用户则自动注册
        UserInfoTb user = weixinAccountService.getUserInfo(openId,(String)request.getAttribute("wxname"));
        if(user==null){
            //跳转至错误页面
            return "";
        }
        logger.info(user);
        //3.查询车牌数量,如果没有车牌则跳转至添加车牌页面
        Long uin = user.getId();
        Integer count = weixinAccountService.getUserCarCount(uin);
        request.setAttribute("domain", Constants.DOMAIN);
        request.setAttribute("openid", openId);
        request.getSession().setAttribute("openid",openId);
        request.setAttribute("uin",uin);
        logger.info("count:"+count);

        if(count<1){
            //跳转至添加车牌页面
            request.setAttribute("forward", forward);
            return "wxpublic/addcarnumber";
        }

        request.setAttribute("uin",uin);
        request.setAttribute("appid",cloudAPPID);
        //4.跳转页面
        if("topresentorderlist".equals(forward)){
            //跳转至在场订单页面
            return "wxpublic/curorderlist";
        }else if("toparkprod".equals(forward)){
            //跳转至月卡列表页面
            return "wxpublic/parkprod";
        }else{
            //跳转到我的账户页面
            return "wxpublic/account";
        }
    }


    /**
     * 前往我的车牌页面
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("tocarnumbers")
    public String toEditCarNumber(HttpServletRequest request, HttpServletResponse response){
        String openId = RequestUtil.getString(request,"openid");
        Long uin = RequestUtil.getLong(request,"uin",-1L);
        logger.info(openId+"-"+uin);
        //查询车牌数量
        //Integer count = weixinAccountService.getUserCarCount(uin);
        //request.setAttribute("count",count);
        request.setAttribute("uin",uin);
        request.setAttribute("openid",openId);
        return "wxpublic/carnumbers";
    }

    /**
     * 前往添加车牌页面
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("toaddcarnumber")
    public String toAddCarnNumber(HttpServletRequest request, HttpServletResponse response){
        String forward = RequestUtil.getString(request,"forward");
        String openId = RequestUtil.getString(request,"openid");
        Long uin = RequestUtil.getLong(request,"uin",-1L);
        logger.info(forward+"-"+openId+"-"+uin);
        request.setAttribute("action",forward);
        request.setAttribute("openid",openId);
        request.setAttribute("uin",uin);
        return "wxpublic/addcarnumber";
    }

    /**
     * 修改车牌
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("editcarnumber")
    public String editCarNumber(HttpServletRequest request, HttpServletResponse response){
        String openId = RequestUtil.getString(request,"openid");
        String carNumber = StringUtils.decodeUTF8(RequestUtil.getString(request,"carnumber"));
        Long uin = RequestUtil.getLong(request,"uin",-1L);
        logger.info(openId+"-"+carNumber+"-"+uin);
        //添加车牌
        Integer ret = weixinAccountService.addCarNumbers(uin,carNumber);
        AjaxUtil.ajaxOutput(response,ret);
        return null;
    }

    /**
     * 获取用户车牌列表
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("getcarnumbers")
    public String getCarNumbers(HttpServletRequest request, HttpServletResponse response){
        String openId = RequestUtil.getString(request,"openid");
        Long uin = RequestUtil.getLong(request,"uin",-1L);
        logger.info(openId+"-"+uin);
        List<CarInfoTb> cars= weixinAccountService.getUserCars(uin);
        AjaxUtil.ajaxOutputWithSnakeCase(response,cars);
        return null;
    }

    /**
     * 解绑车牌
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("delcarnumber")
    public String delCarNumbers(HttpServletRequest request, HttpServletResponse response){
        Long carId = RequestUtil.getLong(request,"car_id",-1L);
        String carNumber = StringUtils.decodeUTF8(RequestUtil.getString(request,"car_number"));
        logger.info(carId+"-"+carNumber);
        Integer ret = weixinAccountService.delCarNumber(carId,carNumber);
        AjaxUtil.ajaxOutput(response,ret);
        return null;
    }

}
