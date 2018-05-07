package parkingos.com.bolink.component.impl;

import com.alibaba.fastjson.JSONObject;
import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import parkingos.com.bolink.beans.ComInfoTb;
import parkingos.com.bolink.component.CommonComponent;
import parkingos.com.bolink.component.OrderComponent;
import parkingos.com.bolink.constant.Constants;
import parkingos.com.bolink.dto.CurOrderPrice;
import parkingos.com.bolink.dto.UnionInfo;
import parkingos.com.bolink.utlis.CheckUtil;
import parkingos.com.bolink.utlis.HttpsProxy;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrderComponentImpl implements OrderComponent {

    Logger logger = Logger.getLogger(OrderComponentImpl.class);

    @Autowired
    CommonComponent commonComponent;
    @Autowired
    HttpsProxy httpsProxy;
    @Autowired
    CommonDao commonDao;

    @Override
    public CurOrderPrice getCurOrderPrice(Long unionId, Long comId, String carNumber, String orderId) {
        CurOrderPrice curOrderPrice = new CurOrderPrice();
        curOrderPrice.setState(0);
        String url = Constants.UNIONIP+"/tothirdprice";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("order_id", orderId);
        paramMap.put("plate_number",carNumber);

        ComInfoTb comInfoTb = new ComInfoTb();
        comInfoTb.setId(comId);
        comInfoTb = (ComInfoTb)commonDao.selectObjectByConditions(comInfoTb);
        if(comInfoTb!=null&&comInfoTb.getBolinkId()!=null&&!"".equals(comInfoTb.getBolinkId())){
            paramMap.put("park_id",comInfoTb.getBolinkId());
        }else {
            paramMap.put("park_id",comId);
        }
        UnionInfo unionInfo = commonComponent.getUnionInfo(comId);
        paramMap.put("union_id", unionInfo.getUnionId());

        Double money = 0.0d;
        Double prepay = 0.0d;

        CheckUtil.createSign(paramMap, unionInfo.getUnionKey());
        logger.info("getCurOrderPrice=>"+paramMap);
        try {
            //CommonUtils.trustAllHosts();
            String ret = httpsProxy.doPost(url, JSONObject.toJSONString(paramMap),10000,10000);
            JSONObject retObject = JSONObject.parseObject(ret);
            logger.info("getCurOrderPrice=>postResult:"+retObject);
            //TODO 验签
            //0失败，1成功,2已预付，3网络不通
            Integer state = retObject.getInteger("state");
            if(state>0){
                money = retObject.getDouble("money");
                if(state==2){
                    prepay = retObject.getDouble("prepay");
                    curOrderPrice.setPrepay(prepay);
                }
                curOrderPrice.setState(state);
                curOrderPrice.setMoney(money);
            }
        } catch (Exception e) {
            logger.error("查询在场订单异常=>"+e.getMessage());
        }
        logger.info("getCurOrderPrice=>"+curOrderPrice);
        return curOrderPrice;
    }
}
