package parkingos.com.bolink.service;

import parkingos.com.bolink.vo.ProdPriceView;
import parkingos.com.bolink.vo.ProdView;

import java.util.List;

public interface WeixinProdService {

    /**
     * 根据用户id查询用户所属月卡信息
     * @param uin
     * @return
     */
    List<ProdView> getProdList(Long uin);

    /**
     * 查询月卡价格
     * @param comId
     * @param cardId
     * @param startTime
     * @param months
     * @return
     */
    ProdPriceView getProdPrice(Long comId, String cardId, Long startTime, Integer months);

}
