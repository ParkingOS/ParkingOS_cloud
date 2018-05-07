package parkingos.com.bolink.service;

import parkingos.com.bolink.beans.ComInfoTb;
import parkingos.com.bolink.beans.ShopTb;
import parkingos.com.bolink.beans.UserInfoTb;

import java.util.Map;

public interface ShopLoginService {
    /**
     * 获取商户操作员信息
     * @param id
     * @param pass
     * @return
     */
    UserInfoTb qryShopUserByIdAndPass(Long id, String pass);

    /**
     * 更新商户操作员登录时间
     * @param id
     * @return
     */
    Integer updateShopUserById(Long logintime, Long id);

    /**
     * 获取商户信息
     * @param id
     * @return
     */
    ShopTb qryShopInfoById(Long id);

    /**
     * 获取车场信息
     * @param id
     * @return
     */
    ComInfoTb qryComInfoById(Long id);

    /**
     * 删除登陆token信息
     * @param uin
     * @return
     */
    Integer delUserSessionByUin(Long uin);

    /**
     * 保存登陆token信息
     * @param uin
     * @param token
     * @param time
     * @param version
     * @return
     */
    Integer SaveUserSession(Long uin, String token, Long time, String version);

    int sendCode(String mobile,String ckey);

    int validCode(String username,String mobile, String code);

    int resetPass(String username, String pass, String md5Pass,String token);

    Map<String,Object> getckey(String mobile, Long userid);
}
