package parkingos.com.bolink.service.impl;

import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import parkingos.com.bolink.beans.CarInfoTb;
import parkingos.com.bolink.beans.ComInfoTb;
import parkingos.com.bolink.beans.OrderTb;
import parkingos.com.bolink.component.CommonComponent;
import parkingos.com.bolink.component.OrderComponent;
import parkingos.com.bolink.component.TcpComponent;
import parkingos.com.bolink.dto.CurOrderPrice;
import parkingos.com.bolink.dto.UnionInfo;
import parkingos.com.bolink.service.WeixinCurOrderService;
import parkingos.com.bolink.utlis.CheckUtil;
import parkingos.com.bolink.utlis.StringUtils;
import parkingos.com.bolink.utlis.TimeTools;
import parkingos.com.bolink.vo.CurOrderListView;
import parkingos.com.bolink.vo.LockCarView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeixinCurOrderServiceImpl implements WeixinCurOrderService {
    Logger logger = Logger.getLogger(WeixinCurOrderServiceImpl.class);
    @Autowired
    CommonDao<CarInfoTb> carInfoCommonDao;
    @Autowired
    CommonDao<OrderTb> orderCommonDao;
    @Autowired
    OrderComponent orderComponent;
    @Autowired
    CommonComponent commonComponent;
    @Autowired
    TcpComponent tcpComponent;


    @Override
    public List<CurOrderListView> getCurOrderList(Long uin) {
        List<CurOrderListView> curOrderListViews = new ArrayList<CurOrderListView>();
        //根据uin查询用户车牌信息
        CarInfoTb carInfoConditions = new CarInfoTb();
        carInfoConditions.setUin(uin);
        List<CarInfoTb> carInfos = carInfoCommonDao.selectListByConditions(carInfoConditions);

        //根据车牌和uin查询在场订单
        if (CheckUtil.hasElement(carInfos)) {
            for (CarInfoTb carInfo : carInfos) {
                String carNumber = carInfo.getCarNumber();
                OrderTb orderConditions = new OrderTb();
                orderConditions.setCarNumber(carNumber);
                orderConditions.setState(0);//在场
                List<OrderTb> orders = orderCommonDao.selectListByConditions(orderConditions);
                if (CheckUtil.hasElement(orders)) {
                    for (OrderTb order : orders) {
                        CurOrderListView curOrderListView = new CurOrderListView();
                        Long comId = order.getComid();
                        //获取车场信息
                        ComInfoTb comInfo = commonComponent.getComInfo(comId);
                        UnionInfo unionInfo = commonComponent.getUnionInfo(comId);
                        Long createTime = order.getCreateTime();
                        String orderId = order.getOrderIdLocal();
                        Long id = order.getId();
                        //每个订单调用bolink查询价格api获取价格
                        CurOrderPrice curOrderPrice = orderComponent.getCurOrderPrice(unionInfo.getUnionId(), comId, carNumber, orderId);
//                        CurOrderPrice curOrderPrice = new CurOrderPrice();
//                        curOrderPrice.setState(1);
//                        curOrderPrice.setMoney(12.0);
                        Integer state = curOrderPrice.getState();
                        if (state == 1 || state == 2) {
                            //成功
                            curOrderListView.setState(1);
                            curOrderListView.setTotal(curOrderPrice.getMoney()+"");
                        } else {
                            //查询失败
                            curOrderListView.setState(0);
                            curOrderListView.setTotal("未知");
                        }
                        curOrderListView.setComId(comId);
                        curOrderListView.setParkName(comInfo.getCompanyName());
                        curOrderListView.setCarNumber(carNumber);
                        curOrderListView.setId(id);
                        curOrderListView.setInParkTime(TimeTools.getTime_yyyyMMdd_HHmmss(createTime * 1000));
                        curOrderListView.setIsLocked(order.getIslocked());
                        curOrderListView.setOrderId(orderId);
                        curOrderListViews.add(curOrderListView);
                    }
                } else {
                    continue;
                }
            }
        } else {
            //TODO 无车牌号
        }

        return curOrderListViews;
    }

    @Override
    public LockCarView doLockCar(Integer lockStatus, Long oid) {

        LockCarView lockCarView = new LockCarView();

        //1.查询订单信息,根据订单信息,初始化锁车参数
        OrderTb orderConditions = new OrderTb();
        orderConditions.setId(oid);
        OrderTb order = orderCommonDao.selectObjectByConditions(orderConditions);
        if(order==null){
            lockCarView.setState(-1);
            return lockCarView;
        }
        Integer isLocked = order.getIslocked();
        String orderId = order.getOrderIdLocal();
        String lockKey = order.getLockKey();
        Long comId = order.getComid();

        //组织锁车消息参数
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("service_name", "lock_car");
        params.put("order_id", orderId);
        params.put("is_locked", lockStatus);
        params.put("comid", comId);

        //2.根据isLocked和lockStatus状态先更新订单中锁车状态,再发送锁车消息
        int ret = -2;
        if(lockStatus==1){
            //锁车
            if(isLocked==0||isLocked==3){
                //可以锁定
                lockKey = (int)(Math.random()*9000+1000)+"";
            }
            if(isLocked==0||isLocked==3||isLocked==2) {
                logger.error(">>>>>>>可以锁定,锁定中...");
                orderConditions.setLockKey(lockKey);
                orderConditions.setIslocked(2);//锁定中
                int update = orderCommonDao.updateByPrimaryKey(orderConditions);
                if(update<1){//更新失败
                    logger.error("修改订单为锁定中失败");
                    ret = 3;
                    lockCarView.setState(ret);
                    lockCarView.setLockKey(lockKey);
                    return lockCarView;
                }
            }else {
                //已锁定
                logger.error("订单已锁定");
                ret = 6;
                lockCarView.setState(ret);
                lockCarView.setLockKey(lockKey);
                return lockCarView;
            }
        }else if(lockStatus==0){
            //解锁
            if(isLocked==1||isLocked==4||isLocked==5){
                logger.error(">>>>>>>可以解锁,解锁中...");
                orderConditions.setLockKey(lockKey);
                orderConditions.setIslocked(4);//解锁中
                int update = orderCommonDao.updateByPrimaryKey(orderConditions);
                if(update<1){//更新失败
                    logger.error("修改订单为解锁中失败");
                    ret = 5;
                    lockCarView.setState(ret);
                    lockCarView.setLockKey(lockKey);
                    return lockCarView;
                }
            }else {
                //返回未锁定
                ret = 7;
                lockCarView.setState(ret);
                lockCarView.setLockKey(lockKey);
                return lockCarView;
            }
        }
        params.put("lock_key", lockKey);
        String data = StringUtils.createLinkString(params);
        logger.error(">>>>>>>>>>>>>>锁车消息："+data);

       // logger.error(">>>>>>>>>>>>>>第二次锁车消息："+data);
        //向SDK发送解锁消息
        int result = tcpComponent.sendMessageToSDK(comId, data);
        //0系统错误;1发送成功;2发送失败;3车场离线
        if(result==1){
            //发送成功,轮询查询结果
            int i = 1;
            long start = System.currentTimeMillis()/1000;
            for(long s=start;s<=start+2;s=System.currentTimeMillis()/1000){
                //从数据库中查询订单信息
                orderConditions = new OrderTb();
                orderConditions.setId(oid);
                OrderTb orderResult = orderCommonDao.selectObjectByConditions(orderConditions);
                if(orderResult!=null){
                    //locksatatus=1,如果islocked为1,则锁定成功;如果islocked值为2,3,则锁定失败"
                    //locksatatus=0,如果islocked为0,则解锁成功;如果islocked值为4,5,则解锁失败
                    logger.info(">>>>>>>查询解锁状态第"+i+"次");
                    Integer lockResult = orderResult.getIslocked();
                    if(lockStatus==1){
                        if(lockResult==1){
                            //锁定成功
                            logger.info("锁定成功!锁车密码:"+lockKey);
                            ret = 1;
                            break;
                        }else if(lockResult==2||lockResult==3){
                            //锁定失败
                            logger.error("锁定失败");
                            ret = 3;
                        }
                    }else if(lockStatus==0){
                        if(lockResult==0){
                            //解锁成功
                            logger.error("解锁成功!");
                            ret = 0;
                            break;
                        }else if(lockResult==4||lockResult==5){
                            //解锁失败
                            logger.error("解锁失败");
                            ret = 5;
                        }
                    }
                }
                try {
                    Thread.currentThread().sleep(300);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
                i++;
            }
        }else if(result==3){
            //车场离线
            ret = 9;
        }else {
            //网络异常
            ret = -1;
        }
        lockCarView.setState(ret);
        lockCarView.setLockKey(lockKey);
        return lockCarView;
    }
}
