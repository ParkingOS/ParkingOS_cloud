package parkingos.com.bolink.service;

import parkingos.com.bolink.beans.CarInfoTb;
import parkingos.com.bolink.beans.UserInfoTb;

import java.util.List;

public interface WeixinAccountService {

    /**
     * 查询用户信息,无则注册
     * @param openId
     * @return
     */
    public UserInfoTb getUserInfo(String openId, String wxName,String appid);

    public List<CarInfoTb> getUserCars(Long uin);

    public Integer getUserCarCount(Long uin);

    public Integer addCarNumbers(Long uin, String carNumber,String appid);

    public Integer delCarNumber(Long carId, String carNumber);
}
