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
import parkingos.com.bolink.beans.OrderTb;
import parkingos.com.bolink.service.OpenService;
import parkingos.com.bolink.service.WeixinCurOrderService;
import parkingos.com.bolink.utlis.Check;
import parkingos.com.bolink.utlis.StringUtils;
import parkingos.com.bolink.vo.LockCarView;

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
@RequestMapping("/lockcar")
public class LockCarAction {

    Logger logger = Logger.getLogger(LockCarAction.class);


    @Autowired
    private CommonDao commonDao;
    @Autowired
    private OpenService openService;
    @Autowired
    WeixinCurOrderService weixinCurOrderService;


    @RequestMapping(value = "/dolock", method = RequestMethod.POST)
    public String dolock(HttpServletResponse response, HttpServletRequest request) throws Exception{
//        ret = "{\"order_id\":\"" + orderId + "\",\"state\":0,\"is_union_user\":0,\"errmsg\":\"请求过快，10秒内不可超过100次\"}";
        logger.info("进入处理锁车接口");
        JSONObject jsonObject = new JSONObject();
        Map<String,Object> resMap = new HashMap<>();

        JSONObject cloudData = getData(request);
//

        logger.info("处理锁车"+cloudData);

        if(cloudData.get("sign")==null||"".equals(cloudData.get("sign"))){
            resMap.put("state",0);
            resMap.put("message","无效sign值");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;
        }
        String data = cloudData.getString("data");
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

        Long comid = resData.getLong("comid");
        Integer lockStatus = resData.getInteger("lock_status");//RequestUtil.getInteger(request, "lock_status", -1);
        String carNumber = resData.getString("car_number").toUpperCase();

        if(Check.isEmpty(comid+"")||comid<0){
            resMap.put("state",0);
            resMap.put("message","车场编号错误");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;
        }

        if(lockStatus!=1&&lockStatus!=0){
            resMap.put("state",0);
            resMap.put("message","锁车状态错误");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;

        }

        if(Check.isEmpty(carNumber)){
            resMap.put("state",0);
            resMap.put("message","车牌号错误");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;

        }




        OrderTb orderTb = new OrderTb();
        orderTb.setCarNumber(carNumber);
        orderTb.setComid(comid);
        orderTb.setState(0);
        orderTb.setIshd(0);
        orderTb=(OrderTb)commonDao.selectObjectByConditions(orderTb);
        Long oid = null;
        if(orderTb!=null){
            oid = orderTb.getId();
        }

        if(oid==null){
            //系统异常
            resMap.put("state",0);
            resMap.put("message","操作失败，没有在场订单");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;
        }
        logger.error("dolock:" + lockStatus + "~" + oid+"~~"+comid );
        //处理解锁车业务
        LockCarView lockCarView = weixinCurOrderService.doLockCar(lockStatus, oid);

        if(lockCarView.getState()==-2){

            //系统异常
            resMap.put("state",0);
            resMap.put("message","操作失败");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;

        }else if(lockCarView.getState()==-1){
            //通知处理失败
            resMap.put("state",0);
            resMap.put("message","网络异常");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;

        }else if(lockCarView.getState()==0){
            //解锁成功
            //修改按钮,改变checked状态
            resMap.put("state",1);
            resMap.put("message","解锁成功!您的车辆已经处于解锁状态,可以正常出场");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;

        }else if(lockCarView.getState()==1){
            //锁定成功
            //修改按钮
            resMap.put("state",1);
            resMap.put("message","锁定成功!您的车辆已经处于锁定状态,请在出场前解锁,否则无法出场");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;


        }else if(lockCarView.getState()==3){
            //锁定失败
            resMap.put("state",0);
            resMap.put("message","锁定失败!请稍后再试!");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;

        }else if(lockCarView.getState()==5){
            //解锁失败
            resMap.put("state",0);
            resMap.put("message","解锁失败!请稍后再试;仍无法解锁请联系车场人员");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;

        }else if(lockCarView.getState()==6){
            //已锁定
            resMap.put("state",0);
            resMap.put("message","您的车辆已处于锁定状态");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;

        }else if(lockCarView.getState()==7){
            //未锁定
            resMap.put("state",0);
            resMap.put("message","您的车辆已处于未锁定状态");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;

        }else if(lockCarView.getState()==9){
            //车场离线
            resMap.put("state",0);
            resMap.put("message","停车场处于断网状态,锁车失败!");
            jsonObject.put("sign",cloudData.getString("sign"));
            jsonObject.put("data",resMap);
            byte[] textByte = jsonObject.toJSONString().getBytes("UTF-8");
            StringUtils.ajaxOutput(response, new String(Base64.encodeBase64(textByte)));
            return null;
        }

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
            logger.error("获取bytes:"+new String(bytes));
            bytes = Base64.decodeBase64(bytes);
            String str = new String(bytes, 0, bytes.length, "utf-8");
            jsonObj = JSONObject.parseObject(str, Feature.OrderedField);

        } catch (Exception e) {
            logger.error("json数据格式不正确");
        }
        return jsonObj;
    }
}
