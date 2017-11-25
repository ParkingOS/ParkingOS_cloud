package parkingos.com.bolink.service;

import parkingos.com.bolink.beans.QrCodeTb;
import parkingos.com.bolink.beans.ShopTb;
import parkingos.com.bolink.beans.TicketTb;
import parkingos.com.bolink.beans.UserSessionTb;

import java.util.List;

public interface ShopTicketManagerService {
    /**
     * 获取登录token信息
     * @param uerSession
     * @return
     */
    UserSessionTb qryUserSessionInfo(UserSessionTb uerSession);
    /**
     * 获取商户信息
     * @param id
     * @return
     */
    ShopTb qryShopInfoById(Long id);

    /**
     * 更新商户信息
     * @return
     */
    Integer updateShopInfo(ShopTb shopInfo, ShopTb shopConditions);

    /**
     * 获取减免劵最大id
     * @return
     */
    Long qryMaxTicketId();

    /**
     * 生成减免劵
     * @param ticketTb
     * @return
     */
    Integer insertTicket(TicketTb ticketTb);

    /**
     * 生成减免劵扫码code
     * @param qrCodeTb
     * @return
     */
    Integer insertQrcode(QrCodeTb qrCodeTb);


    /**
     * 商户版小程序获取已发减免劵
     * @param shopId
     * @param bTime
     * @param eTime
     * @param type
     * @return
     */
    List<TicketTb> queryShopTicketPage(Long shopId, Long bTime, Long eTime, Integer type, Integer pageNum, Integer pageSize);


}
