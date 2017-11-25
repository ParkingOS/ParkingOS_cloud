package parkingos.com.bolink.service.impl;

import com.zld.common_dao.dao.CommonDao;
import com.zld.common_dao.enums.FieldOperator;
import com.zld.common_dao.qo.SearchBean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import parkingos.com.bolink.beans.CarInfoTb;
import parkingos.com.bolink.beans.OrderTb;
import parkingos.com.bolink.beans.TicketTb;
import parkingos.com.bolink.beans.WxpUserTb;
import parkingos.com.bolink.service.AliPrepayService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class AliPrepayServiceImpl implements AliPrepayService {
    Logger logger = Logger.getLogger(AliPrepayServiceImpl.class);
    @Autowired
    CommonDao<WxpUserTb> wxpUserTbCommonDao;
    @Autowired
    CommonDao<CarInfoTb> carInfoTbCommonDao;
    @Autowired
    CommonDao<OrderTb> orderTbCommonDao;
    @Autowired
    CommonDao<TicketTb> ticketTbCommonDao;

    public static void main(String[] args) {
        System.out.println(new Random().nextDouble());
    }

    @Override
    public String getCarNumber(Long uin,Integer bindflag) {
        String carNumber = "";
        if(bindflag == 0){//临时账户
            WxpUserTb wxpUserConditions = new WxpUserTb();
            wxpUserConditions.setUin(uin);
            WxpUserTb wxpUserTb = wxpUserTbCommonDao.selectObjectByConditions(wxpUserConditions);
            if(wxpUserTb != null && wxpUserTb.getCarNumber() != null){
                carNumber = wxpUserTb.getCarNumber();
            }
        }else if(bindflag == 1){
            CarInfoTb carInfoConditions = new CarInfoTb();
            carInfoConditions.setUin(uin);
            carInfoConditions.setState(1);
            CarInfoTb carInfoTb = carInfoTbCommonDao.selectObjectByConditions(carInfoConditions);
           /* Map carMap = service.getMap("select car_number from car_info_tb where uin=? and state=? order by create_time desc limit ?",
                    new Object[] { uin, 1, 1 });*/
            if(carInfoTb != null && carInfoTb.getCarNumber() != null){
                carNumber = carInfoTb.getCarNumber();
            }
        }
        return carNumber;
    }

    @Override
    public OrderTb qryUnpayOrder(String carNumber, Long comid) {
        OrderTb orderConditions = new OrderTb();
        orderConditions.setComid(comid);
        orderConditions.setCarNumber(carNumber);
        orderConditions.setState(0);
        OrderTb orderTb = orderTbCommonDao.selectObjectByConditions(orderConditions);
        logger.error(comid+","+carNumber+","+orderTb);
        if(orderTb == null){
            orderConditions = new OrderTb();
            orderConditions.setComid(comid);
            orderConditions.setState(0);
            List<SearchBean> searchBeans = new ArrayList<SearchBean>();
            SearchBean searchBean = new SearchBean();
            searchBean.setFieldName("car_number");
            searchBean.setOperator(FieldOperator.LIKE);
            searchBean.setBasicValue(carNumber.substring(1));
            searchBeans.add(searchBean);
            orderTb = orderTbCommonDao.selectObjectByConditions(orderConditions,searchBeans);
        }
        return orderTb;
    }

    @Override
    public Integer unpayOrderForTicket(long orderId) {
        TicketTb ticketConditions = new TicketTb();
        ticketConditions.setOrderid(orderId);
        ticketConditions.setState(1);
        return ticketTbCommonDao.selectCountByConditions(ticketConditions);
    }

    @Override
    public TicketTb qryTicket(Long ticketId) {
        TicketTb ticketConditions = new TicketTb();
        ticketConditions.setId(ticketId);
         return ticketTbCommonDao.selectObjectByConditions(ticketConditions);
    }
}
