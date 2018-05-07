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
import parkingos.com.bolink.beans.CarowerProduct;
import parkingos.com.bolink.beans.ProductPackageTb;
import parkingos.com.bolink.utlis.Check;
import parkingos.com.bolink.utlis.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
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


    @RequestMapping(value = "/handlemonthcard", method = RequestMethod.POST)
    public String addpark(HttpServletResponse response, HttpServletRequest request) {

        logger.info("进入处理月卡会员接口");
//        ret = "{\"order_id\":\"" + orderId + "\",\"state\":0,\"is_union_user\":0,\"errmsg\":\"请求过快，10秒内不可超过100次\"}";
        String ret = "{\"state\":0,\"errmsg\":\"操作失败\"}";

        JSONObject cloudData = getData(request);
        logger.info("月卡上传数据"+cloudData);
        String data = cloudData.getString("data");

        JSONObject resData = JSONObject.parseObject(data);

        String cardId = resData.getString("card_id");
        //1添加2修改3删除
        Integer operateType = resData.getInteger("operate_type");
        Long comId = resData.getLong("comid");

        if(comId==null|| !Check.isNumber(comId+"")){
            logger.error("comId参数错误！");
            StringUtils.ajaxOutput(response, ret);
            return null;
        }

        if(cardId==null||"".equals(cardId)){
            logger.error("card_id参数错误！");
            StringUtils.ajaxOutput(response, ret);
            return null;
        }

        if(operateType==null){
            logger.error("operateType参数错误！");
            StringUtils.ajaxOutput(response, ret);
            return null;
        }

        logger.info("上传月卡参数:"+cardId+"~~~"+operateType+"~~~"+comId);

        CarowerProduct carowerProduct = new CarowerProduct();
        carowerProduct.setCardId(cardId);
        carowerProduct.setComId(comId);
        carowerProduct.setIsDelete(0L);

        int count = commonDao.selectCountByConditions(carowerProduct);
        int res = 0;
        JSONObject result = new JSONObject();
        if(operateType==3){//删除
            CarowerProduct fields = new CarowerProduct();
            fields.setIsDelete(1L);//is_delete 0正常1删除
            res = commonDao.updateByConditions(fields,carowerProduct);

            if(res==1){
                result.put("state", res);
                result.put("errmsg", "删除成功");
                ret = result.toJSONString();
            }else{
                ret = "{\"state\":0,\"errmsg\":\"删除失败，不存在该月卡\"}";
                logger.info("删除失败,不存在该会员");
            }
            StringUtils.ajaxOutput(response, ret);
            return null;

        }

        //组装所有参数  用来更新或者插入
        carowerProduct.setCreateTime(resData.getLong("create_time"));
        carowerProduct.setUpdateTime(resData.getLong("update_time"));
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


        carowerProduct.setCarNumber(resData.getString("car_number"));
        carowerProduct.setTotal(resData.getBigDecimal("price"));
        if(resData.containsKey("amount_receivable")) {
            carowerProduct.setActTotal(resData.getBigDecimal("amount_receivable"));
        }else{
            carowerProduct.setActTotal(resData.getBigDecimal("price"));
        }
        carowerProduct.setName(resData.getString("name"));
        carowerProduct.setRemark(resData.getString("remark"));
        carowerProduct.setAddress(resData.getString("address"));
        carowerProduct.setMobile(resData.getString("tel"));
        carowerProduct.setLimitDayType(resData.getInteger("limit_day_type"));
        carowerProduct.setpLot(resData.getString("p_lot"));



        if(operateType==1){//添加
            if(count>0){
                ret = "{\"state\":0,\"errmsg\":\"添加失败,card_id已存在\"}";
                StringUtils.ajaxOutput(response, ret);
                return null;
            }
            res = commonDao.insert(carowerProduct);
            if(res==1){
                result.put("state", res);
                result.put("errmsg", "删除成功");
                ret = result.toJSONString();
            }else{
                logger.error("新建会员失败");
            }
            StringUtils.ajaxOutput(response, ret);
            return null;

        }else if(operateType==2){//更新

            if(count==0){
                ret = "{\"state\":0,\"errmsg\":\"更新失败,card_id不存在\"}";
                StringUtils.ajaxOutput(response, ret);
                return null;
            }
            CarowerProduct con = new CarowerProduct();
            con.setIsDelete(0L);
            con.setCardId(cardId);
            con.setComId(comId);
            res = commonDao.updateByConditions(carowerProduct,con);
            if(res==1){
                result.put("state", res);
                result.put("errmsg", "修改成功");
                ret = result.toJSONString();
            }
            StringUtils.ajaxOutput(response, ret);
            return null;
        }
        StringUtils.ajaxOutput(response, ret);
        return null;
    }


    @RequestMapping(value = "/updatepark", method = RequestMethod.POST)
    public String updatepark(HttpServletResponse response, HttpServletRequest request) {

        return null;
    }

    @RequestMapping(value = "/queryparks", method = RequestMethod.POST)
    public String querypark(HttpServletResponse response, HttpServletRequest request) {

        return null;
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
            String str = new String(bytes, 0, nTotalRead, "utf-8");

            jsonObj = com.alibaba.fastjson.JSONObject.parseObject(str, Feature.OrderedField);
        } catch (Exception e) {
            logger.error("json数据格式不正确");
        }
        return jsonObj;
    }
}
