package parkingos.com.bolink.service;

import parkingos.com.bolink.vo.CurOrderListView;
import parkingos.com.bolink.vo.LockCarView;

import java.util.List;

public interface WeixinCurOrderService {

    /**
     * 根据uin用户编号获取用户在场订单列表
     * @param uin
     * @return
     */
    List<CurOrderListView> getCurOrderList(Long uin);

    /**
     * 执行锁定车辆业务
     * @param lockStatus 1为锁车,0为解锁
     * @param oid 云平台订单主键
     * @return -2系统异常 -1网络异常  0解锁成功  1锁定成功  3锁定失败 5解锁失败 6已锁定 7未锁定 9车场离线
     */
    LockCarView doLockCar(Integer lockStatus, Long oid);

    int sendCode(String openid, String mobile);

    void addphone(String openid, String mobile);

    int validCode(String code, String mobile, String openid);

    LockCarView checkMobile(String openid);
}
