package parkingos.com.bolink.service.impl;

import com.zld.common_dao.dao.CommonDao;
import com.zld.common_dao.enums.FieldOperator;
import com.zld.common_dao.qo.PageOrderConfig;
import com.zld.common_dao.qo.SearchBean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import parkingos.com.bolink.beans.QrCodeTb;
import parkingos.com.bolink.beans.ShopTb;
import parkingos.com.bolink.beans.TicketTb;
import parkingos.com.bolink.beans.UserSessionTb;
import parkingos.com.bolink.service.ShopTicketManagerService;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShopTicketManagerServiceImpl implements ShopTicketManagerService {
    Logger logger = Logger.getLogger(ShopTicketManagerServiceImpl.class);

    @Autowired
    CommonDao<UserSessionTb> userSessionTbCommonDao;
    @Autowired
    CommonDao<ShopTb> shopTbCommonDao;
    @Autowired
    CommonDao<TicketTb> ticketTbCommonDao;
    @Autowired
    CommonDao<QrCodeTb> qrCodeTbCommonDao;

    @Override
    public UserSessionTb qryUserSessionInfo(UserSessionTb uerSession) {
        return userSessionTbCommonDao.selectObjectByConditions(uerSession);
    }

    @Override
    public ShopTb qryShopInfoById(Long id) {
        ShopTb shopConditions = new ShopTb();
        shopConditions.setId(id);
        return shopTbCommonDao.selectObjectByConditions(shopConditions);
    }

    @Override
    public Integer updateShopInfo(ShopTb shopInfo, ShopTb shopConditions) {
        return shopTbCommonDao.updateByConditions(shopInfo,shopConditions);
    }

    @Override
    public Long qryMaxTicketId() {
        TicketTb ticketConditions = new TicketTb();
        PageOrderConfig pageOrderConfig = new PageOrderConfig();
        pageOrderConfig.setOrderInfo("id","desc");
        List<TicketTb> ticketTbs = ticketTbCommonDao.selectListByConditions(ticketConditions,pageOrderConfig);
        if(ticketTbs!=null && ticketTbs.size()>0){
            return ticketTbs.get(0).getId();
        }else{
            return 0L;//没有生成过减免劵
        }
    }

    @Override
    public Integer insertTicket(TicketTb ticketTb) {
       return ticketTbCommonDao.insert(ticketTb);
    }

    @Override
    public Integer insertQrcode(QrCodeTb qrCodeTb) {
        return qrCodeTbCommonDao.insert(qrCodeTb);
    }

    @Override
    public List<TicketTb> queryShopTicketPage(Long shopId, Long bTime, Long eTime, Integer type,Integer pageNum,Integer pageSize) {
        TicketTb ticketConditions = new TicketTb();
        ticketConditions.setShopId(shopId);
        if(type != -1){
            ticketConditions.setType(type);
        }
        List<SearchBean> list = new ArrayList<SearchBean>();
        SearchBean searchBean = new SearchBean();
        searchBean.setFieldName("create_time");
        searchBean.setOperator(FieldOperator.BETWEEN);
        searchBean.setStartValue(bTime);
        searchBean.setEndValue(eTime);
        list.add(searchBean);
        PageOrderConfig pageOrderConfig = new PageOrderConfig();
        pageOrderConfig.setOrderInfo("create_time","desc");
        if(pageNum!=0 && pageSize!=0){
            pageOrderConfig.setPageInfo(pageNum,pageSize);
        }
       return  ticketTbCommonDao.selectListByConditions(ticketConditions,list,pageOrderConfig);
    }
}
