package parkingos.com.bolink.actions;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.zld.common_dao.dao.CommonDao;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import parkingos.com.bolink.beans.CarTypeTb;
import parkingos.com.bolink.beans.CarowerProduct;
import parkingos.com.bolink.beans.ProductPackageTb;
import parkingos.com.bolink.beans.SyncInfoPoolTb;
import parkingos.com.bolink.service.OpenService;
import parkingos.com.bolink.utlis.Check;
import parkingos.com.bolink.utlis.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * 车场处理
 *
 * @author zq
 */
@RestController
@RequestMapping("/monthcard")
public class MonthCardAction {

    Logger logger = Logger.getLogger(MonthCardAction.class);


    @Autowired
    private CommonDao commonDao;
    @Autowired
    private OpenService openService;


    @RequestMapping(value = "/handlemonthcard", method = RequestMethod.POST)
    public String handlemonthcard(HttpServletResponse response, HttpServletRequest request) throws Exception{

        logger.info("进入处理月卡会员接口");
//        ret = "{\"order_id\":\"" + orderId + "\",\"state\":0,\"is_union_user\":0,\"errmsg\":\"请求过快，10秒内不可超过100次\"}";

        JSONObject jsonObject = new JSONObject();
        Map<String,Object> resMap = new HashMap<>();

        JSONObject cloudData = getData(request);
        logger.info("月卡上传数据"+cloudData);
        String data = cloudData.getString("data");

        if(cloudData.get("sign")==null||"".equals(cloudData.get("sign"))){
            resMap.put("state",0);
            resMap.put("message","无效sign值");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;
        }

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

        JSONObject resData = JSONObject.parseObject(data);

        String cardId = resData.getString("card_id");
        String carNumber = resData.getString("car_number");
        //1添加2修改3删除
        Integer operateType = resData.getInteger("operate_type");
        Long comId = resData.getLong("comid");

        if(operateType==null||!(operateType==1||operateType==2||operateType==3)){
            resMap.put("state",0);
            resMap.put("message","operate_type参数错误");
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



        logger.info("上传月卡参数:"+cardId+"~~~"+operateType+"~~~"+comId);




        CarowerProduct carowerProduct = new CarowerProduct();
        if(!Check.isEmpty(cardId)){
            carowerProduct.setCardId(cardId);
        }
        if(!Check.isEmpty(carNumber)){
            carowerProduct.setCarNumber(carNumber);
        }
        carowerProduct.setComId(comId);
        carowerProduct.setIsDelete(0L);

        int count = commonDao.selectCountByConditions(carowerProduct);
        int res = 0;
        if(operateType==3){//删除
            CarowerProduct fields = new CarowerProduct();
            fields.setIsDelete(1L);//is_delete 0正常1删除
            if(count==1){
                carowerProduct = (CarowerProduct)commonDao.selectObjectByConditions(carowerProduct);
                res = commonDao.updateByConditions(fields,carowerProduct);
                if(res==1){
                    int ins = insertSysn(carowerProduct, 2, comId);
                    resMap.put("state",res);
                    resMap.put("message","删除成功");
                    jsonObject.put("sign",cloudData.getString("sign"));
                    jsonObject.put("data",resMap);

                }else{
                    resMap.put("state",0);
                    resMap.put("message","删除失败，不存在该月卡");
                    jsonObject.put("sign",cloudData.getString("sign"));
                    jsonObject.put("data",resMap);

                    logger.info("删除失败,不存在该会员");
                }
            }else{
                resMap.put("state",0);
                resMap.put("message","删除失败，不存在该月卡或者月卡条件不唯一");
                jsonObject.put("sign",cloudData.getString("sign"));
                jsonObject.put("data",resMap);
            }

            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;
        }



        //组装所有参数  用来更新或者插入  operate不是3判断必传字段
        if(!Check.isLong(resData.getString("begin_time"))){
            resMap.put("state",0);
            resMap.put("message","begin_time参数错误");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;

        }
        if(!Check.isLong(resData.getString("end_time"))){
            resMap.put("state",0);
            resMap.put("message","end_time参数错误");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;
        }


        carowerProduct.setbTime(resData.getLong("begin_time"));
        carowerProduct.seteTime(resData.getLong("end_time"));



        if(resData.getString("pid")!=null){
            ProductPackageTb productPackageTb = new ProductPackageTb();
            productPackageTb.setComid(comId);
            productPackageTb.setCardId(resData.getString("pid"));
            productPackageTb = (ProductPackageTb)commonDao.selectObjectByConditions(productPackageTb);
            if(productPackageTb!=null)
                carowerProduct.setPid(productPackageTb.getId());
        }
        if(resData.getString("car_type")!=null){
            CarTypeTb carTypeTb = new CarTypeTb();
            carTypeTb.setComid(comId);
            carTypeTb.setCartypeId(resData.getString("car_type"));
            carTypeTb = (CarTypeTb)commonDao.selectObjectByConditions(carTypeTb);
            if(carTypeTb!=null)
                carowerProduct.setCarTypeId(carTypeTb.getId());
        }


        carowerProduct.setCreateTime(resData.getLong("create_time"));
        carowerProduct.setUpdateTime(resData.getLong("update_time"));
        carowerProduct.setName(resData.getString("name"));
        carowerProduct.setRemark(resData.getString("remark"));
        carowerProduct.setAddress(resData.getString("address"));
        carowerProduct.setMobile(resData.getString("tel"));
        carowerProduct.setLimitDayType(resData.getInteger("limit_day_type"));
        carowerProduct.setpLot(resData.getString("p_lot"));



        if(operateType==1){//添加
            if(count>0){
                resMap.put("state",0);
                resMap.put("message","添加失败,card_id或者car_number已存在");
                jsonObject.put("sign",cloudData.getString("sign"));
                jsonObject.put("data",resMap);
                byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
                StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
                return null;
            }
            Long id = commonDao.selectSequence(CarowerProduct.class);
            carowerProduct.setId(id);
            res = commonDao.insert(carowerProduct);
            if(res==1){
                int ins = insertSysn(carowerProduct, 0, comId);
                resMap.put("state",res);
                resMap.put("message","添加成功");
                jsonObject.put("sign",cloudData.getString("sign"));
                jsonObject.put("data",resMap);
                byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
                StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
                return null;
            }else{
                resMap.put("state",0);
                resMap.put("message","添加失败");
                jsonObject.put("sign",cloudData.getString("sign"));
                jsonObject.put("data",resMap);
                byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
                StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
                return null;
            }

        }else if(operateType==2){//更新

            if(count==0){
                resMap.put("state",0);
                resMap.put("message","更新失败,card_id或者car_number不存在");
                jsonObject.put("sign",cloudData.getString("sign"));
                jsonObject.put("data",resMap);
                byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
                StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
                return null;

            }
            CarowerProduct con = new CarowerProduct();
            con.setIsDelete(0L);
            con.setCardId(cardId);
            con.setComId(comId);
            res = commonDao.updateByConditions(carowerProduct,con);
            if(res==1){
                carowerProduct = (CarowerProduct)commonDao.selectObjectByConditions(con);
                int ins = insertSysn(carowerProduct, 1, comId);
                resMap.put("state",res);
                resMap.put("message","修改成功");
                jsonObject.put("sign",cloudData.getString("sign"));
                jsonObject.put("data",resMap);
                byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
                StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
                return null;
            }
        }
       return null;
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
    private com.alibaba.fastjson.JSONObject getData(HttpServletRequest request){
        com.alibaba.fastjson.JSONObject jsonObj = null;
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

            jsonObj = com.alibaba.fastjson.JSONObject.parseObject(str, Feature.OrderedField);
        } catch (Exception e) {
            logger.error("json数据格式不正确");
        }
        return jsonObj;
    }
}
