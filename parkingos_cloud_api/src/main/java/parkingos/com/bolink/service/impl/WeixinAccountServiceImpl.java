package parkingos.com.bolink.service.impl;

import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import parkingos.com.bolink.beans.CarInfoTb;
import parkingos.com.bolink.beans.CarowerProduct;
import parkingos.com.bolink.beans.OrderTb;
import parkingos.com.bolink.beans.UserInfoTb;
import parkingos.com.bolink.service.WeixinAccountService;
import parkingos.com.bolink.utlis.CheckUtil;

import java.util.List;
import java.util.Random;

@Service
public class WeixinAccountServiceImpl implements WeixinAccountService {
    Logger logger = Logger.getLogger(WeixinAccountServiceImpl.class);
    @Autowired
    CommonDao<UserInfoTb> userInfoCommonDao;
    @Autowired
    CommonDao<CarInfoTb> carInfoCommonDao;
    @Autowired
    CommonDao<OrderTb> orderCommonDao;
    @Autowired
    CommonDao<CarowerProduct> carOwnerProductCommonDao;

    public static void main(String[] args) {
        System.out.println(new Random().nextDouble());
    }

    @Override
    @Transactional
    public UserInfoTb getUserInfo(String openId, String wxName,String appid) {
        UserInfoTb conditions = new UserInfoTb();
        conditions.setWxpOpenid(openId);
        conditions.setState(0);
        conditions.setAuthFlag(4L);
        UserInfoTb userInfo = userInfoCommonDao.selectObjectByConditions(conditions);
        if(userInfo==null){
            //注册用户
            Long currentTime = System.currentTimeMillis()/1000;
            Long id = userInfoCommonDao.selectSequence(UserInfoTb.class);
            UserInfoTb addUser = new UserInfoTb();
            addUser.setId(id);
            addUser.setRegTime(currentTime);
            addUser.setAuthFlag(4L);
            addUser.setNickname("车主:"+wxName);
            addUser.setComid(0L);
            addUser.setPassword("123456");
            addUser.setMedia(11);
            addUser.setWxpOpenid(openId);
            addUser.setAppid(appid);
            userInfo = addUser;
            System.out.println(addUser);
            userInfoCommonDao.insert(addUser);
        }
        else {//更新用户的appid以及openid

//            if(!Check.isEmpty(userInfo.getWxpOpenid())){
//                if(!userInfo.getWxpOpenid().contains(openId)){
//                    openId = userInfo.getWxpOpenid()+","+openId;
//                }
//            }
//            if(userInfo.getAppid()!=null&&!"".equals(userInfo.getAppid())){
//                if(!userInfo.getAppid().contains(appid)){
//                    appid = userInfo.getAppid()+","+appid;
//                }
//            }
//            userInfo.setWxpOpenid(openId);
//            userInfo.setAppid(appid);
//            int i =userInfoCommonDao.updateByPrimaryKey(userInfo);
            UserInfoTb userInfoTb = new UserInfoTb();
            userInfoTb.setAppid(appid);
            int i = userInfoCommonDao.updateByConditions(userInfoTb,conditions);
            logger.info("更新用户appid:"+i);
        }
        return userInfo;
    }

    @Override
    public List<CarInfoTb> getUserCars(Long uin) {
        CarInfoTb carInfoConditions = new CarInfoTb();
        carInfoConditions.setUin(uin);
        return carInfoCommonDao.selectListByConditions(carInfoConditions);
    }

    @Override
    public Integer getUserCarCount(Long uin) {
        CarInfoTb conditions = new CarInfoTb();
        conditions.setUin(uin);
        return carInfoCommonDao.selectCountByConditions(conditions);
    }

    @Override
    @Transactional
    public Integer addCarNumbers(Long uin, String carNumber,String appid) {

        //1.是否已经是自己的车牌
        CarInfoTb carInfoConditions = new CarInfoTb();
        carInfoConditions.setCarNumber(carNumber);
        carInfoConditions.setUin(uin);
        Integer count = carInfoCommonDao.selectCountByConditions(carInfoConditions);
        if(count>0){
            return -3;
        }
        //2.绑定个数
        carInfoConditions = new CarInfoTb();
        carInfoConditions.setUin(uin);
        carInfoConditions.setState(0);
        count = carInfoCommonDao.selectCountByConditions(carInfoConditions);
        if(count>3){
            return -4;
        }
        //3.没有锁定的在场订单
        OrderTb orderConditions = new OrderTb();
        orderConditions.setCarNumber(carNumber);
        orderConditions.setState(0);
        orderConditions.setIslocked(1);
        orderConditions.setIshd(0);
        count = orderCommonDao.selectCountByConditions(orderConditions);
        if(count>0){
            return -2;
        }
        //4.可以绑定
        /*************处理绑定逻辑**************/
        //1.删除之前车牌   增加公众号区分
        carInfoConditions = new CarInfoTb();
        carInfoConditions.setCarNumber(carNumber);
        carInfoConditions.setAppid(appid);
        carInfoCommonDao.deleteByConditions(carInfoConditions);
        //2.更新月卡信息(模糊查月卡信息)
//        List<SearchBean> searchBeans = new ArrayList<SearchBean>();
//        SearchBean searchBean = new SearchBean();
//        searchBean.setFieldName("car_number");
//        searchBean.setOperator(FieldOperator.LIKE);
//        searchBean.setBasicValue(carNumber);
//        searchBeans.add(searchBean);
//        List<CarowerProduct> carOwnerProducts = carOwnerProductCommonDao.selectListByConditions(new CarowerProduct(),searchBeans);
//        if(CheckUtil.hasElement(carOwnerProducts)){
//            CarowerProduct carOwnerProductUpdate = new CarowerProduct();
//            carOwnerProductUpdate.setUin(uin);
//            for (CarowerProduct carOwnerProduct:
//                    carOwnerProducts) {
//                CarowerProduct carOwnerProductConditions = new CarowerProduct();
//                carOwnerProductConditions.setId(carOwnerProduct.getId());
//                carOwnerProductCommonDao.updateByConditions(carOwnerProductUpdate,carOwnerProductConditions);
//            }
//        }

        //3.更新在场订单
        OrderTb orderUpdate = new OrderTb();
        orderUpdate.setUin(uin);
        orderConditions = new OrderTb();
        orderConditions.setCarNumber(carNumber);
        orderConditions.setIshd(0);
        orderCommonDao.updateByConditions(orderUpdate,orderConditions);

        //4.写入新车牌
        Long currentTime = System.currentTimeMillis()/1000;
        CarInfoTb addCarInfo = new CarInfoTb();
        addCarInfo.setCarNumber(carNumber);
        addCarInfo.setUin(uin);
        addCarInfo.setAppid(appid);
        addCarInfo.setCreateTime(currentTime);
        return carInfoCommonDao.insert(addCarInfo);
    }

    @Override
    public Integer delCarNumber(Long carId, String carNumber) {
        if(CheckUtil.isNotNull(carNumber)){
            //锁定的在场订单则不可删除
            OrderTb orderConditions = new OrderTb();
            orderConditions.setCarNumber(carNumber);
            orderConditions.setState(0);
            orderConditions.setIslocked(1);
            orderConditions.setIshd(0);
            List<OrderTb> orders = orderCommonDao.selectListByConditions(orderConditions);
            if(CheckUtil.hasElement(orders)){
                return -2;
            }
        }
        //删除车牌
        CarInfoTb delCarInfo = new CarInfoTb();
        delCarInfo.setId(carId);
        return carInfoCommonDao.deleteByConditions(delCarInfo);
    }
}
