package parkingos.com.bolink.service;

import parkingos.com.bolink.beans.OrderTb;
import parkingos.com.bolink.beans.TicketTb;

public interface AliPrepayService {

    /**
     * 获取车主车牌号
     */
    String getCarNumber(Long uin, Integer bindflag);

    /**
     * 查询在场订单
     */
    OrderTb qryUnpayOrder(String carNumber, Long comid);

    /**
     * 订单是否用过减免劵
     */
    Integer unpayOrderForTicket(long orderId);

    /**
     *根据id查询减免劵信息
     */
    TicketTb qryTicket(Long ticketId);

    OrderTb qryUnpayOrder2(String carNumber, Long comid);

    Integer shopTicketCount(Long id,Long shopid);
}
