package parkingos.com.bolink.component;

import parkingos.com.bolink.beans.ComInfoTb;
import parkingos.com.bolink.dto.UnionInfo;
import parkingos.com.bolink.dto.WXUserView;

public interface CommonComponent {

    /**
     * 根据云平台车场编号查询对应厂商平台编号
     * @param comId
     * @return
     */
    UnionInfo getUnionInfo(Long comId);

    /**
     * 根据云平台车场编号查询车场信息
     * @param comId
     * @return
     */
    ComInfoTb getComInfo(Long comId);

    /**
     * 根据openid获取微信用户信息
     * @param openid
     * @return
     */
    WXUserView getUserinfoByOpenid(String openid);

    /**
     * 添加车牌
     * @param uin
     * @param carnumber
     * @return
     */
    Integer addCarnumber(Long uin, String carnumber,Integer flag);

}
