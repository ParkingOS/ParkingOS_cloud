package parkingos.com.bolink.service.impl;

import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import parkingos.com.bolink.beans.ComInfoTb;
import parkingos.com.bolink.beans.ShopTb;
import parkingos.com.bolink.beans.UserInfoTb;
import parkingos.com.bolink.beans.UserSessionTb;
import parkingos.com.bolink.service.ShopLoginService;
import parkingos.com.bolink.utlis.Check;
import parkingos.com.bolink.utlis.Defind;
import parkingos.com.bolink.utlis.Encryption;
import parkingos.com.bolink.utlis.MessageUtils;
import parkingos.com.bolink.utlis.weixinpay.memcachUtils.MemcacheUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class ShopLoginServiceImpl implements ShopLoginService {
    Logger logger = Logger.getLogger(ShopLoginServiceImpl.class);
    @Autowired
    CommonDao<UserInfoTb> userInfoTbCommonDao;
    @Autowired
    CommonDao<ShopTb> shopTbCommonDao;
    @Autowired
    CommonDao<ComInfoTb> comInfoTbCommonDao;
    @Autowired
    CommonDao<UserSessionTb> userSessionTbCommonDao;
    @Autowired
    private MemcacheUtils memcacheUtils;

    @Override
    public UserInfoTb qryShopUserByIdAndPass(Long id, String md5pass) {
        UserInfoTb userConditions = new UserInfoTb();
        userConditions.setId(id);
        userConditions.setMd5pass(md5pass);
        return userInfoTbCommonDao.selectObjectByConditions(userConditions);
    }

    @Override
    public Integer updateShopUserById(Long logintime, Long id) {
        UserInfoTb userConditions = new UserInfoTb();
        userConditions.setId(id);
        UserInfoTb userInfo = new UserInfoTb();
        userInfo.setId(id);
        userInfo.setLogonTime(logintime);
        return userInfoTbCommonDao.updateByConditions(userInfo, userConditions);
    }

    @Override
    public ShopTb qryShopInfoById(Long id) {
        ShopTb shopConditions = new ShopTb();
        shopConditions.setId(id);
        return shopTbCommonDao.selectObjectByConditions(shopConditions);
    }

    @Override
    public ComInfoTb qryComInfoById(Long id) {
       ComInfoTb comInfoConditions = new ComInfoTb();
        comInfoConditions.setId(id);
        return comInfoTbCommonDao.selectObjectByConditions(comInfoConditions);
    }

    @Override
    public Integer delUserSessionByUin(Long uin) {
        UserSessionTb userSessionConditions = new UserSessionTb();
        userSessionConditions.setUin(uin);
        return userSessionTbCommonDao.deleteByConditions(userSessionConditions);
    }

    @Override
    public Integer SaveUserSession(Long uin, String token, Long time, String version) {
        UserSessionTb userSessionConditions = new UserSessionTb();
        userSessionConditions.setUin(uin);
        userSessionConditions.setToken(token);
        userSessionConditions.setCreateTime(time);
        userSessionConditions.setVersion(version);
        return userSessionTbCommonDao.insert(userSessionConditions);
    }

    @Override
    public int sendCode(String mobile,String ckey) {

        String memkey = (String) memcacheUtils.getCache(mobile+ckey);
        logger.error("ckey:"+ckey+" memkey:"+memkey);
        if(!ckey.equals(memkey)){
            //验证码错误
            return 0;
        }

        long code = Math.round(Math.random() * (9999 - 1000 + 1) + 1000);
        boolean setCache = memcacheUtils.setCache(mobile, code + "", 60 * 5);
        logger.error("getcode>>>" + setCache);
        if (setCache) {
            String content = Defind.getProperty("MESSAGESIGN") + code + " (请完成验证,有效期5分钟),如非本人操作,请忽略本短信。";
            try {
                MessageUtils.sendMsg(mobile, content);
                logger.error("reguser>>>验证码已发送:" + code);
                return 1;
            } catch (Exception e) {
                logger.error("验证码发送异常");
                //验证码发送失败
            }
        } else {
            logger.error("验证码存入缓存失败");
        }
        return 2;
    }

    @Override
    public int validCode(String username,String mobile, String code) {

        if(Check.isNumber(username)){
            UserInfoTb userInfoTb = new UserInfoTb();
            userInfoTb.setId(Long.parseLong(username));
            userInfoTb = userInfoTbCommonDao.selectObjectByConditions(userInfoTb);
            if(userInfoTb!=null){
                String codeMem = (String) memcacheUtils.getCache(mobile);
                if (code.equals(codeMem)) {
                    //验证码验证成功
                    return 1;
                } else {
                    return 2;
                }
            }else {
               return -1;
            }

        }else{
            return -1;
        }
    }

    @Override
    public int resetPass(String username, String pass, String md5Pass, String token) {

        if(Check.isNumber(username)){
//            UserSessionTb userSessionTb = new UserSessionTb();
//            userSessionTb.setUin(Long.parseLong(username));
//            userSessionTb = userSessionTbCommonDao.selectObjectByConditions(userSessionTb);
//            if(userSessionTb==null){
//                logger.error("商户修改密码token错误");
//                return 2;
//            }
            UserInfoTb userInfoTb = new UserInfoTb();
            userInfoTb.setId(Long.parseLong(username));
            userInfoTb.setPassword(pass);
            userInfoTb.setMd5pass(md5Pass);
            return userInfoTbCommonDao.updateByPrimaryKey(userInfoTb);
        }
        return 2;
    }

    @Override
    public Map<String, Object> getckey(String mobile, Long userid) {

        Map<String, Object> retMap = new HashMap<>();

        UserInfoTb userInfoTb = new UserInfoTb();
        userInfoTb.setId(userid);
        userInfoTb = userInfoTbCommonDao.selectObjectByConditions(userInfoTb);
        if(userInfoTb!=null){
            if(userInfoTb.getMobile()==null||"".equals(userInfoTb.getMobile())){
                retMap.put("state", 0);
                retMap.put("errmsg","请前往后台绑定员工手机号!");
                return retMap;
            }else if(!userInfoTb.getMobile().equals(mobile)){
                retMap.put("state", 0);
                retMap.put("errmsg","手机号与绑定手机号不符!");
                return retMap;
            }
        }else{
            retMap.put("state", 0);
            retMap.put("errmsg","账号错误!");
            return retMap;
        }


        long code = Math.round(Math.random()*(9999-1000+1)+1000);
        memcacheUtils.setCache(mobile+code, code+"", 300);
        String encode = Encryption.encryptToAESPKCS5(code+"", Encryption.KEY);
        logger.error("ckey:"+code+" encode:"+encode);
        retMap.put("state", 1);
        retMap.put("ckey", encode);

        return retMap;
    }
}
