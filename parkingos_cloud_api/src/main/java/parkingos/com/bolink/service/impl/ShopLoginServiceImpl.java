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
}
