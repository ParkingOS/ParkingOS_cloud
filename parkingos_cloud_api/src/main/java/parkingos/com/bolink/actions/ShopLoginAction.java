package parkingos.com.bolink.actions;

import com.alibaba.fastjson.JSONArray;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import parkingos.com.bolink.beans.ComInfoTb;
import parkingos.com.bolink.beans.ShopTb;
import parkingos.com.bolink.beans.UserInfoTb;
import parkingos.com.bolink.service.ShopLoginService;
import parkingos.com.bolink.utlis.AjaxUtil;
import parkingos.com.bolink.utlis.Check;
import parkingos.com.bolink.utlis.RequestUtil;
import parkingos.com.bolink.utlis.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 商户小程序登录
 */
@Controller
public class ShopLoginAction {
    Logger logger = Logger.getLogger(ShopLoginAction.class);
    @Autowired
    ShopLoginService shopLoginService;


    /**
     *
     */
    @RequestMapping(value="/shoplogin")
    public void shopLogin(HttpServletRequest request, HttpServletResponse response){
        Map<String,Object> infoMap = new HashMap<String, Object>();
        String action = RequestUtil.processParams(request, "action");
        logger.error("action:"+action);
        //1 根据入参查询用户
        String username =RequestUtil.processParams(request, "username");
        String pass =RequestUtil.processParams(request, "password");
        String version = RequestUtil.getString(request, "version");
        logger.error("shoplogin user:"+username+",pass:"+pass);
        if(pass.length()<32){
            //md5密码 ，生成规则：原密码md5后，加上'zldtingchebao201410092009'再次md5
            pass =StringUtils.MD5(pass);
            pass = StringUtils.MD5(pass +"zldtingchebao201410092009");
        }
        if(!StringUtils.isNumber(username)){
            infoMap.put("info", "用户名错误");
            AjaxUtil.ajaxOutput(response, infoMap);
        }
        UserInfoTb user = shopLoginService.qryShopUserByIdAndPass(Long.valueOf(username),pass);
        //logger.error(user);
        // 2 判断用户是否存在
        if(user==null){
            infoMap.put("info", "用户名或密码错误");
        }else {//3 封装用户信息
            Long uin = user.getId();
            String token = StringUtils.MD5(username+pass+System.currentTimeMillis());
            infoMap.put("info", "success");
            infoMap.put("token", token);
            infoMap.put("role", user.getAuthFlag());
            infoMap.put("name", user.getNickname());
            infoMap.put("shop_id", user.getShopId());
            //4 取商户减免劵信息
            ShopTb shopInfo =  shopLoginService.qryShopInfoById(user.getShopId());
            if(shopInfo!=null){
                //查询车场信息
                ComInfoTb comInfo =  shopLoginService.qryComInfoById(shopInfo.getComid());
                //车场名称名称
                infoMap.put("park_name", comInfo.getCompanyName());
                //商户名称
                infoMap.put("shop_name", shopInfo.getName());
                //减免劵类型 1-时长 2-金额
                infoMap.put("ticket_type", shopInfo.getTicketType());
                //减免劵单位 1-分钟 2-小时 3-天 4 元
                Integer ticket_unit = shopInfo.getTicketUnit();
                if(ticket_unit == null){
                    if(shopInfo.getTicketType() == 1){
                        ticket_unit = 2;
                    }
                    if(shopInfo.getTicketType() == 2){
                        ticket_unit = 4;
                    }
                }
                infoMap.put("ticket_unit", ticket_unit);
                //获取默认显示额度
                JSONArray ranges = new JSONArray();
                String defalut_limit = shopInfo.getDefaultLimit();
                if(!Check.isEmpty(defalut_limit)){
                    String[] defaluts = defalut_limit.split(",");
                    defaluts = Arrays.copyOf(defaluts, 3);//取前三个额度
                    for(String defalut : defaluts){
                        Map<String, Object> range = new HashMap<String, Object>();
                        range.put("range", defalut);
                        ranges.add(range);
                    }
                    Map<String, Object> range = new HashMap<String, Object>();
                    range.put("range", "全免");
                    ranges.add(range);
					/*if("1".equals(shopMap.get("ticket_type")+"")){
						Map<String, Object> range = new HashMap<String, Object>();
						range.put("range", "全免");
						ranges.add(range);
					}*/
                }
                infoMap.put("ticket_range", ranges);
                //是否可以手输额度
                infoMap.put("handInputEnable",shopInfo.getHandInputEnable());
            }
            //5 保存session，更新登录时间
            doSaveSession(uin,token,version);
            shopLoginService.updateShopUserById(System.currentTimeMillis()/1000,user.getId());
            logger.error(username+"登录成功...");
        }
        AjaxUtil.ajaxOutput(response, infoMap);


        //http://192.168.199.239/zld/shoplogin.do?username=21629&password=111111
    }


    /**
     * 保存token到数据库中
     * @param uin
     * @param token
     */
    private void doSaveSession(Long uin,String token,String version ){
        //先删除市场专员上次登录时的token
        shopLoginService.delUserSessionByUin(uin);
        //保存本次登录的token
        shopLoginService.SaveUserSession(uin,token,System.currentTimeMillis()/1000,version);
    }


    /*
    * 2:发送失败
    * 1:发送成功
    * 0:验证机器码错误
    *
    * */
    @RequestMapping(value="/sendshopcode")
    public void shopSendCode(HttpServletRequest request, HttpServletResponse response){
        String mobile = RequestUtil.getString(request,"mobile");
        String ckey = RequestUtil.getString(request, "ckey");
        logger.error("商户小程序发送验证码手机号:"+mobile);
        int result = shopLoginService.sendCode(mobile,ckey);
        logger.error("商户小程序修改密码发送验证码:"+result);

        Map<String,Object> resMap = new HashMap<>();
        resMap.put("state",result);

        AjaxUtil.ajaxOutputWithSnakeCase(response,resMap);
    }


    @RequestMapping(value="/getckey")
    public void getckey(HttpServletRequest request, HttpServletResponse response){
        String mobile = RequestUtil.getString(request,"mobile");
        Long userid = RequestUtil.getLong(request,"userid",-1L);
        String domain = RequestUtil.getString(request,"domain");
        logger.error("商户小程序获取机器码:"+mobile+"~~~userid:"+userid);

        Map<String,Object> resMap = shopLoginService.getckey(mobile,userid);

        Pattern letter = Pattern.compile("[a-zA-Z]+");
        String[] bytes = domain.split("");
        int index = -1;
        for(int i=1;i<bytes.length;i++){
            Matcher matcher = letter.matcher(bytes[i]);
            if(!matcher.matches()){
                index = i-1;
                break;
            }
        }
        String cloudAdd = domain.substring(0, index);
        resMap.put("userid",cloudAdd+userid);

        AjaxUtil.ajaxOutputWithSnakeCase(response,resMap);
    }

    /*
    *
    * 2:验证失败
    * 1:验证成功
    * -1:用户名错误或没有该用户,超时
    *
    * */
    @RequestMapping(value="/checkcode")
    public void shopValidCode(HttpServletRequest request, HttpServletResponse response){
        Map<String,Object> infoMap = new HashMap<String, Object>();
        String mobile = RequestUtil.getString(request,"mobile");
        String code = RequestUtil.getString(request,"code");
        String userid = RequestUtil.getString(request,"userid");
        String domain = RequestUtil.getString(request,"domain");
        logger.error("商户验证验证码是否正确mobile:"+mobile+"~~~code:"+code);
        int result = shopLoginService.validCode(userid,mobile,code);
        logger.error("商户验证验证码是否正确:"+result);
        String token ="";
        if(result==1){
            token = StringUtils.MD5(userid+System.currentTimeMillis());
            doSaveSession(Long.parseLong(userid),token,"");
        }
        logger.error("shopValidCode商户验证验证码是否正确生成token:"+token);

        Pattern letter = Pattern.compile("[a-zA-Z]+");
        String[] bytes = domain.split("");
        int index = -1;
        for(int i=1;i<bytes.length;i++){
            Matcher matcher = letter.matcher(bytes[i]);
            if(!matcher.matches()){
                index = i-1;
                break;
            }
        }
        String cloudAdd = domain.substring(0, index);

        infoMap.put("userid",cloudAdd+userid);
        infoMap.put("state",result);
        infoMap.put("token",token);
        logger.error("shopValidCode商户验证验证码infomap:"+infoMap);
//        String res = JSONObject.toJSONString(infoMap);
        AjaxUtil.ajaxOutputWithSnakeCase(response,infoMap);
    }


    @RequestMapping(value="/resetpwd")
    public void resetPass(HttpServletRequest request, HttpServletResponse response){
        Map<String,Object> infoMap = new HashMap<String, Object>();
        String userid = RequestUtil.getString(request,"userid");
        String pass = RequestUtil.getString(request,"passwd");
        String token = RequestUtil.getString(request,"token");
        logger.error("商户重置密码:"+userid+"~~~pass:"+pass+"~~~~~token:"+token);
        String md5Pass = "";
        if(pass.length()<32){
            //md5密码 ，生成规则：原密码md5后，加上'zldtingchebao201410092009'再次md5
            md5Pass =StringUtils.MD5(pass);
            md5Pass = StringUtils.MD5(md5Pass +"zldtingchebao201410092009");
        }
        logger.error("商户重置密码md5pass:"+md5Pass);
        int update = shopLoginService.resetPass(userid,pass,md5Pass,token);
        logger.error("商户重置密码结果:"+update);
        infoMap.put("state",update);
//        String result = JSONObject.toJSONString(infoMap);
        AjaxUtil.ajaxOutputWithSnakeCase(response,infoMap);
    }

}
