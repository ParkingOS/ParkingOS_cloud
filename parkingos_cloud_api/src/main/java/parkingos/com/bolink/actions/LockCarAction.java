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
import parkingos.com.bolink.service.WeixinCurOrderService;
import parkingos.com.bolink.utlis.Check;
import parkingos.com.bolink.utlis.RequestUtil;
import parkingos.com.bolink.utlis.StringUtils;
import parkingos.com.bolink.vo.LockCarView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;


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
    WeixinCurOrderService weixinCurOrderService;


    @RequestMapping(value = "/dolock", method = RequestMethod.POST)
    public String addpark(HttpServletResponse response, HttpServletRequest request) {
//        ret = "{\"order_id\":\"" + orderId + "\",\"state\":0,\"is_union_user\":0,\"errmsg\":\"请求过快，10秒内不可超过100次\"}";
        logger.info("进入处理锁车接口");
        String ret = "{\"state\":0,\"errmsg\":\"操作失败\"}";

        JSONObject cloudData = getData(request);
        logger.info("处理锁车"+cloudData);
        String data = cloudData.getString("data");

        JSONObject resData = JSONObject.parseObject(data);

        Integer lockStatus = resData.getInteger("lock_status");//RequestUtil.getInteger(request, "lock_status", -1);
        Long oid = resData.getLong("oid");//RequestUtil.getLong(request, "oid", -1L);
        logger.info("dolock:" + lockStatus + "~" + oid );
        //处理解锁车业务
        LockCarView lockCarView = weixinCurOrderService.doLockCar(lockStatus, oid);

        if(lockCarView.getState()==-2){
            //系统异常
            ret = "{\"state\":0,\"errmsg\":\"操作失败\"}";

        }else if(lockCarView.getState()==-1){
            //通知处理失败
            ret = "{\"state\":0,\"errmsg\":\"网络异常\"}";

        }else if(lockCarView.getState()==0){
            //解锁成功
            //修改按钮,改变checked状态
            ret = "{\"state\":1,\"errmsg\":\"解锁成功!您的车辆已经处于解锁状态,可以正常出场\"}";

        }else if(lockCarView.getState()==1){
            //锁定成功
            //修改按钮
            ret = "{\"state\":1,\"errmsg\":\"锁定成功!您的车辆已经处于锁定状态,请在出场前解锁,否则无法出场\"}";

        }else if(lockCarView.getState()==3){
            //锁定失败
            ret = "{\"state\":0,\"errmsg\":\"锁定失败!请稍后再试或下拉刷新查看车辆状态!\"}";

        }else if(lockCarView.getState()==5){
            //解锁失败
            ret = "{\"state\":0,\"errmsg\":\"解锁失败!请稍后再试或下拉刷新查看车辆状态;仍无法解锁请联系车场人员,\"}";

        }else if(lockCarView.getState()==6){
            //已锁定
            ret = "{\"state\":0,\"errmsg\":\"您的车辆已处于锁定状态,下拉刷新车辆状态!\"}";

        }else if(lockCarView.getState()==7){
            //未锁定
            ret = "{\"state\":0,\"errmsg\":\"您的车辆已处于未锁定状态,下拉刷新车辆状态!\"}";

        }else if(lockCarView.getState()==9){
            //车场离线
            ret = "{\"state\":0,\"errmsg\":\"停车场处于断网状态,锁车失败!\"}";
        }
        StringUtils.ajaxOutput(response, ret);
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
