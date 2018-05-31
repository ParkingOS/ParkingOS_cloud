package parkingos.com.bolink.actions;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.zld.common_dao.dao.CommonDao;
import com.zld.common_dao.enums.FieldOperator;
import com.zld.common_dao.qo.SearchBean;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import parkingos.com.bolink.beans.CardRenewTb;
import parkingos.com.bolink.beans.CarowerProduct;
import parkingos.com.bolink.beans.SyncInfoPoolTb;
import parkingos.com.bolink.service.OpenService;
import parkingos.com.bolink.utlis.Check;
import parkingos.com.bolink.utlis.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    @Autowired
    private OpenService openService;


    @RequestMapping(value = "/monthrecord", method = RequestMethod.POST)
    public String addpark(HttpServletResponse response, HttpServletRequest request) throws Exception{
//        ret = "{\"order_id\":\"" + orderId + "\",\"state\":0,\"is_union_user\":0,\"errmsg\":\"请求过快，10秒内不可超过100次\"}";
        //String ret = "{\"state\":0,\"errmsg\":\"操作失败\"}";
        JSONObject jsonObject = new JSONObject();

        JSONObject cloudData = getData(request);
        logger.info("月卡上传数据"+cloudData);
        String data = cloudData.getString("data");

        JSONObject jsonData = JSONObject.parseObject(data);

        Map<String,Object> resMap = new HashMap<>();

        boolean isChecked = openService.checkSign(data,cloudData.getString("sign"),null);
        if (!isChecked) {
            resMap.put("state",0);
            resMap.put("message","签名错误");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;
        }




        String cardId = jsonData.getString("card_id");
        String comId = jsonData.getString("comid");
        String trade_no = jsonData.getString("trade_no");
        String carNumber = jsonData.getString("car_number");
        if(carNumber==null){
            carNumber="";
        }
        String buyMonth = jsonData.getString("buy_month");
        String btime = jsonData.getString("begin_time");
        String etime = jsonData.getString("end_time");
        String amountPay = jsonData.getString("amount_pay");
        String amountRec = jsonData.getString("amount_receivable");
        //验证必传字段
        if(Check.isEmpty(btime)||!Check.isLong(btime)){
            resMap.put("state",0);
            resMap.put("message","begin_time参数错误");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;
        }
        if(Check.isEmpty(etime)||!Check.isLong(etime)){
            resMap.put("state",0);
            resMap.put("message","end_time参数错误");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;
        }

        if(Check.isEmpty(buyMonth)||!Check.isNumber(buyMonth)){
            resMap.put("state",0);
            resMap.put("message","buy_month参数错误");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;
        }
        if(Check.isEmpty(amountRec)||!Check.isDouble(amountRec)){
            resMap.put("state",0);
            resMap.put("message","amount_receivable参数错误");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;
        }

        if(Check.isEmpty(amountPay)||!Check.isDouble(amountPay)){
            resMap.put("state",0);
            resMap.put("message","amount_pay参数错误");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;
        }

        if(!Check.isLong(jsonData.getString("pay_time"))){
            resMap.put("state",0);
            resMap.put("message","pay_time参数错误");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;

        }

        if(Check.isEmpty(jsonData.getString("pay_type"))){
            resMap.put("state",0);
            resMap.put("message","pay_type参数错误");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;

        }

        if(comId==null|| !Check.isNumber(comId+"")){
            resMap.put("state",0);
            resMap.put("message","comid参数错误");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;
        }

        if(Check.isEmpty(carNumber)&&Check.isEmpty(cardId)){
            resMap.put("state",0);
            resMap.put("message","card_id或者car_number参数错误");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;
        }

        //验证车牌号
        if(!Check.isEmpty(carNumber)){
            if(carNumber.length()<=8){
                if(!StringUtils.checkPlate(carNumber)){
                    resMap.put("state",0);
                    resMap.put("message","请输入正确的car_number");
                    jsonObject.put("sign",cloudData.getString("sign"));
                    jsonObject.put("data",resMap);
                    byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
                    StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
                    return null;
                }
            }else if(carNumber.length()>8){
                String[] carArr = carNumber.split(",");
                boolean flag = true;
                for(int i = 0;i<carArr.length;i++){
                    if(!StringUtils.checkPlate(carArr[i])){
                        flag=false;
                        break;
                    }
                }
                if(!flag){
                    resMap.put("state",0);
                    resMap.put("message","请输入正确的车牌号");
                    jsonObject.put("sign",cloudData.getString("sign"));
                    jsonObject.put("data",resMap);
                    byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
                    StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
                    return null;
                }
            }
        }

        if(Check.isEmpty(trade_no)){
            resMap.put("state",0);
            resMap.put("message","处理失败，trade_no为空！");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;
        }


        CardRenewTb renewTb = new CardRenewTb();


        renewTb.setCardId(cardId);
        renewTb.setCarNumber(carNumber.toUpperCase());
        renewTb.setComid(comId);
        renewTb.setTradeNo(trade_no);


        renewTb.setStartTime(jsonData.getLong("begin_time"));
        renewTb.setLimitTime(jsonData.getLong("end_time"));
        renewTb.setPayTime(jsonData.getInteger("pay_time"));
        renewTb.setAmountReceivable(jsonData.getString("amount_receivable"));
        renewTb.setAmountPay(jsonData.getString("amount_pay"));
        renewTb.setPayType(jsonData.getString("pay_type"));
        renewTb.setCollector(jsonData.getString("collector"));
        renewTb.setBuyMonth(jsonData.getInteger("buy_month"));
//        renewTb.setCarNumber(jsonData.getString("car_number"));
        renewTb.setUserId(jsonData.getString("user_id"));
        renewTb.setResume(jsonData.getString("resume"));


        CardRenewTb con = new CardRenewTb();
        con.setComid(comId);
        con.setTradeNo(trade_no);
        //根据流水号判断这条续费记录是不是唯一，
        int count = commonDao.selectCountByConditions(con);

        int result = 0;
        if(count>0){
            resMap.put("state",0);
            resMap.put("message","操作失败，请不要重复续费");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
        }else{ //根据该流水进行续费操作

            Long id = commonDao.selectSequence(CardRenewTb.class);

            renewTb.setId(id.intValue());

            CarowerProduct carowerProduct = new CarowerProduct();
            carowerProduct.setComId(Long.parseLong(comId));
            if(!Check.isEmpty(cardId)){
                carowerProduct.setCardId(cardId);
            }
            List<SearchBean> searchBeans =null;
            if(!Check.isEmpty(carNumber)){
                searchBeans = new ArrayList<SearchBean>();
                SearchBean searchBean = new SearchBean();
                searchBean.setBasicValue("%"+carNumber.toUpperCase()+"%");
                searchBean.setOperator(FieldOperator.LIKE);
                searchBean.setFieldName("car_number");
                searchBeans.add(searchBean);
            }
            carowerProduct.setIsDelete(0L);

            int vipCount = commonDao.selectCountByConditions(carowerProduct,searchBeans);
            int update = 0;
            if(vipCount==1){

                carowerProduct = (CarowerProduct)commonDao.selectObjectByConditions(carowerProduct);

                carNumber = carowerProduct.getCarNumber();
                cardId = carowerProduct.getCardId();

                //进行续费操作
                CarowerProduct carowerProduct1 = new CarowerProduct();

                carowerProduct1.setbTime(Long.parseLong(btime));
                carowerProduct1.seteTime(Long.parseLong(etime));
                carowerProduct1.setTotal(new BigDecimal(amountPay));//实收
                carowerProduct1.setActTotal(new BigDecimal(amountRec));//应收
                carowerProduct1.setComId(Long.parseLong(comId));

                update= commonDao.updateByConditions(carowerProduct1,carowerProduct);

            }else if(vipCount==0){
                resMap.put("state",0);
                resMap.put("message","不存在该月卡");
                jsonObject.put("sign",cloudData.getString("sign"));
                jsonObject.put("data",resMap);

                byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
                StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
                return null;
            }else{
                resMap.put("state",0);
                resMap.put("message","说好的保证唯一呢？");
                jsonObject.put("sign",cloudData.getString("sign"));
                jsonObject.put("data",resMap);

                byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
                StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
                return null;
            }

            if(update==1){
                renewTb.setCarNumber(carNumber);
                renewTb.setCardId(cardId);
                result = commonDao.insert(renewTb);
                logger.info("插入续费记录"+result);
                resMap.put("state",1);
                resMap.put("message","续费成功");
                jsonObject.put("sign",cloudData.getString("sign"));
                jsonObject.put("data",resMap);
                int ins = insertSysn(carowerProduct, 1, Long.parseLong(comId));
                //下发 月卡续费记录
                int res = insertCardSysn(renewTb, 0, Long.parseLong(comId));
            }else{
                resMap.put("state",0);
                resMap.put("message","续费失败");
                jsonObject.put("sign",cloudData.getString("sign"));
                jsonObject.put("data",resMap);
            }

        }
        byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
        StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
        return null;

    }



    private int insertCardSysn(CardRenewTb cardRenewTb, int operater, Long comid) {
        SyncInfoPoolTb syncInfoPoolTb = new SyncInfoPoolTb();
        syncInfoPoolTb.setComid(comid);
        syncInfoPoolTb.setTableId((cardRenewTb.getId()).longValue());
        syncInfoPoolTb.setTableName("card_renew_tb");
        syncInfoPoolTb.setCreateTime(System.currentTimeMillis() / 1000);
        syncInfoPoolTb.setOperate(operater);
        return commonDao.insert(syncInfoPoolTb);
    }

    private int insertSysn(CarowerProduct carowerProduct, Integer operater, Long comid) {
        SyncInfoPoolTb syncInfoPoolTb = new SyncInfoPoolTb();
        syncInfoPoolTb.setComid(comid);
        syncInfoPoolTb.setTableId(carowerProduct.getId());
        syncInfoPoolTb.setTableName("carower_product");
        syncInfoPoolTb.setCreateTime(System.currentTimeMillis() / 1000);
        syncInfoPoolTb.setOperate(operater);
        return commonDao.insert(syncInfoPoolTb);
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
            logger.error("获取bytes:"+new String(bytes));
            bytes = Base64.decodeBase64(bytes);
            String str = new String(bytes, 0, bytes.length, "utf-8");
            logger.error("Base64位后的字符串:"+str);
            jsonObj = com.alibaba.fastjson.JSONObject.parseObject(str, Feature.OrderedField);
        } catch (Exception e) {
            logger.error("json数据格式不正确");
        }
        return jsonObj;
    }
}
