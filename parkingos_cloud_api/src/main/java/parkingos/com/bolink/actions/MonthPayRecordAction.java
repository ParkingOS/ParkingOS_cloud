package parkingos.com.bolink.actions;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import parkingos.com.bolink.beans.CarTypeTb;
import parkingos.com.bolink.beans.CardRenewTb;
import parkingos.com.bolink.beans.CarowerProduct;
import parkingos.com.bolink.beans.ProductPackageTb;
import parkingos.com.bolink.utlis.Check;
import parkingos.com.bolink.utlis.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;


/**
 * 车场处理
 *
 * @author zq
 */
@RestController
@RequestMapping("/monthpayrecord")
public class MonthPayRecordAction {

    Logger logger = Logger.getLogger(MonthPayRecordAction.class);


    @Autowired
    private CommonDao commonDao;


    @RequestMapping(value = "/getmonthprice", method = RequestMethod.POST)
    public String addpark(HttpServletResponse response, HttpServletRequest request) {
//        ret = "{\"order_id\":\"" + orderId + "\",\"state\":0,\"is_union_user\":0,\"errmsg\":\"请求过快，10秒内不可超过100次\"}";
        String ret = "{\"state\":0,\"errmsg\":\"操作失败\"}";

        JSONObject cloudData = getData(request);
        logger.info("月卡上传数据"+cloudData);
        String data = cloudData.getString("data");

        JSONObject jsonData = JSONObject.parseObject(data);


        String cardId = jsonData.getString("card_id");
        String comId = jsonData.getString("comid");
        String trade_no = jsonData.getString("trade_no");

        if(cardId==null||"".equals(cardId)){
            ret = "{\"state\":0,\"errmsg\":\"处理失败，card_id为空！\"}";
            StringUtils.ajaxOutput(response, ret);
            return null;
        }
        if(comId==null||"".equals(comId)){
            ret = "{\"state\":0,\"errmsg\":\"处理失败，comid为空！\"}";
            StringUtils.ajaxOutput(response, ret);
            return null;
        }
        if(trade_no==null||"".equals(trade_no)){
            ret = "{\"state\":0,\"errmsg\":\"处理失败，trade_no为空！\"}";
            StringUtils.ajaxOutput(response, ret);
            return null;
        }
        CardRenewTb renewTb = new CardRenewTb();
        renewTb.setComid(comId);
        renewTb.setCardId(cardId);
        renewTb.setTradeNo(trade_no);
        //int count = 0;
        int count = commonDao.selectCountByConditions(renewTb);

        if(count==0){
            ret = "{\"state\":0,\"errmsg\":\"查询失败,没有该续费记录！\"}";
            StringUtils.ajaxOutput(response, ret);
            return null;
        }
        renewTb = (CardRenewTb)commonDao.selectObjectByConditions(renewTb);
        JSONObject result = new JSONObject();
        result.put("state",1);
        result.put("errmsg","查询成功");
        result.put("data",renewTb);
        StringUtils.ajaxOutput(response, result.toJSONString());
        return null;

    }





    /**
     * 输入流中获取json数据
     * @param request
     * @return
     */
    private JSONObject getData(HttpServletRequest request){
        JSONObject jsonObj = null;
        try {
            byte[] bytes = new byte[1024 * 1024];
            InputStream is = request.getInputStream();

            int nRead = 1;
            int nTotalRead = 0;
            while (nRead > 0) {
                nRead = is.read(bytes, nTotalRead, bytes.length - nTotalRead);
                if (nRead > 0)
                    nTotalRead = nTotalRead + nRead;
            }
            String str = new String(bytes, 0, nTotalRead, "utf-8");

            jsonObj = JSONObject.parseObject(str, Feature.OrderedField);
        } catch (Exception e) {
            logger.error("json数据格式不正确");
        }
        return jsonObj;
    }
}
