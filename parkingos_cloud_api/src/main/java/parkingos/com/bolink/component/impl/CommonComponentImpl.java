package parkingos.com.bolink.component.impl;

import com.alibaba.fastjson.JSONObject;
import com.zld.common_dao.dao.CommonDao;
import com.zld.common_dao.enums.FieldOperator;
import com.zld.common_dao.qo.SearchBean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import parkingos.com.bolink.beans.*;
import parkingos.com.bolink.component.CommonComponent;
import parkingos.com.bolink.constant.Constants;
import parkingos.com.bolink.dto.UnionInfo;
import parkingos.com.bolink.dto.WXUserView;
import parkingos.com.bolink.utlis.HttpsProxy;
import parkingos.com.bolink.utlis.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommonComponentImpl implements CommonComponent {

    @Autowired
    CommonDao<ComInfoTb> comInfoCommonDao;
    @Autowired
    CommonDao<OrgGroupTb> orgGroupTbCommonDao;
    @Autowired
    CommonDao<OrgCityMerchants> orgCityMerchantCommonDao;
    @Autowired
    CommonDao<UserInfoTb> userInfoTbCommonDao;
    @Autowired
    CommonDao<WxpUserTb> wxpUserTbCommonDao;
    @Autowired
    CommonDao<CarInfoTb> carInfoTbCommonDao;
    @Autowired
    CommonDao<UserProfileTb> userProfileTbCommonDao;

    private Logger logger = Logger.getLogger(CommonComponentImpl.class);
    @Override
    public UnionInfo getUnionInfo(Long comId) {
        Long unionId = Long.valueOf(Constants.UNIONID);
        String unionKey = Constants.UNIONKEY;

        UnionInfo unionInfo = new UnionInfo();

        ComInfoTb comInfoConditions = new ComInfoTb();
        comInfoConditions.setId(comId);

        //获取车场信息
        ComInfoTb comInfo = comInfoCommonDao.selectObjectByConditions(comInfoConditions);
        if(comInfo!=null){
            //取集团用户信息
            Long groupid = comInfo.getGroupid();
            if(groupid!=null && groupid>0){
                OrgGroupTb orgGroupConditions = new OrgGroupTb();
                orgGroupConditions.setId(groupid);
                OrgGroupTb orgGroupTb = orgGroupTbCommonDao.selectObjectByConditions(orgGroupConditions);
                Long cityId = orgGroupTb.getCityid();
                if(cityId!=null&&cityId>0){
                    OrgCityMerchants orgCityMerchantConditions = new OrgCityMerchants();
                    orgCityMerchantConditions.setId(cityId);
                    //获取城市用户信息
                    OrgCityMerchants orgCityMerchant = orgCityMerchantCommonDao.selectObjectByConditions(orgCityMerchantConditions);
                    if(orgCityMerchant!=null){
                        //获取对应的unionId和unionKey
                        unionId = Long.valueOf(orgCityMerchant.getUnionId());
                        unionKey = orgCityMerchant.getUkey();
                    }
                }
            }
        }
        unionInfo.setUnionId(unionId);
        unionInfo.setUnionKey(unionKey);
        return unionInfo;
    }

    @Override
    public ComInfoTb getComInfo(Long comId) {
        ComInfoTb comInfoConditions = new ComInfoTb();
        comInfoConditions.setId(comId);
        return comInfoCommonDao.selectObjectByConditions(comInfoConditions);
    }

    @Override
    public WXUserView getUserinfoByOpenid(String openid) {
        WXUserView wxUserView = new WXUserView();
        Integer bindflag = 0;
        Long uin = -1L;
        String mobile = "";
        BigDecimal balance = new BigDecimal("0");
        // 1 查询注册微信用户信息
        UserInfoTb userInfoConditions = new UserInfoTb();
        userInfoConditions.setWxpOpenid(openid);
        UserInfoTb userInfoTb = userInfoTbCommonDao.selectObjectByConditions(userInfoConditions);
        if(userInfoTb != null){
            bindflag = 1;
            uin = userInfoTb.getId();
            mobile = userInfoTb.getMobile();
            balance = userInfoTb.getBalance();
        }else{ //2 查询临时微信用户信息
            WxpUserTb wxpUserConditions = new WxpUserTb();
            wxpUserConditions.setOpenid(openid);
            WxpUserTb wxpUserTb = wxpUserTbCommonDao.selectObjectByConditions(wxpUserConditions);
            if(wxpUserTb == null){ //3 用户不存在，注册为临时用户
                uin = wxpUserTbCommonDao.selectSequence(WxpUserTb.class);
                wxpUserConditions.setUin(uin);
                wxpUserConditions.setCreateTime(System.currentTimeMillis() / 1000);
                wxpUserTbCommonDao.insert(wxpUserConditions);
            }else{
                uin = wxpUserTb.getUin();
                balance = wxpUserTb.getBalance();
            }
        }

        wxUserView.setBindflag(bindflag);
        wxUserView.setBalance(balance);
        wxUserView.setMobile(mobile);
        wxUserView.setUin(uin);
        return wxUserView;
    }

    @Override
    public Integer addCarnumber(Long uin, String carnumber) {
        Long curTime = System.currentTimeMillis()/1000;
        // 1 判断是否微信注册用户
        UserInfoTb userInfoConditions = new UserInfoTb();
        userInfoConditions.setId(uin);
        userInfoConditions.setAuthFlag(4L);
        Integer bindflag = userInfoTbCommonDao.selectCountByConditions(userInfoConditions);
        if(bindflag == 1){
            //1.1 车牌是否被别人注册
            CarInfoTb carInfoTbConditions = new CarInfoTb();
            carInfoTbConditions.setCarNumber(carnumber);
            List<CarInfoTb> carInfoTbList = carInfoTbCommonDao.selectListByConditions(carInfoTbConditions);
            for(CarInfoTb qryCarInfoTb : carInfoTbList){
                if(uin != qryCarInfoTb.getUin()){
                    //该车牌号已被别人注册,删除该车牌
                    carInfoTbConditions = new CarInfoTb();
                    carInfoTbConditions.setCarNumber(carnumber);
                    carInfoTbConditions.setUin(qryCarInfoTb.getUin());
                    Integer del = carInfoTbCommonDao.deleteByConditions(carInfoTbConditions);
                }
            }
            //1.2 车牌是否被该车主注册过
            carInfoTbConditions = new CarInfoTb();
            carInfoTbConditions.setUin(uin);
            carInfoTbConditions.setCarNumber(carnumber);
            Integer count = carInfoTbCommonDao.selectCountByConditions(carInfoTbConditions);
            if(count > 0){//该车主已经注册过该车牌号
                //更新时间
                CarInfoTb carInfoTb = new CarInfoTb();
                carInfoTb.setCreateTime(curTime);
                carInfoTbConditions = new CarInfoTb();
                carInfoTbConditions.setUin(uin);
                carInfoTbConditions.setCarNumber(carnumber);
                int update = carInfoTbCommonDao.updateByConditions(carInfoTb,carInfoTbConditions);
                if(update>0){
                    return 1;
                }
            }else{
                //1.3 查询该车主注册车牌号个数
                carInfoTbConditions = new CarInfoTb();
                carInfoTbConditions.setUin(uin);
                count = carInfoTbCommonDao.selectCountByConditions(carInfoTbConditions);
                if(count >= 3){//该车主注册的车牌号的个数超过限制
                    return -3;
                }
                carInfoTbConditions = new CarInfoTb();
                carInfoTbConditions.setUin(uin);
                carInfoTbConditions.setCarNumber(carnumber);
                carInfoTbConditions.setCreateTime(curTime);
                int r = carInfoTbCommonDao.insert(carInfoTbConditions);
                if(r > 0){
                    return 1;
                }
            }
        }else if(bindflag == 0){// 2 微信临时用户
            //2.1 解绑该车牌号
            WxpUserTb wxpUserConditions = new WxpUserTb();
            wxpUserConditions.setCarNumber(carnumber);
            WxpUserTb wxpUserTb = new WxpUserTb();
            wxpUserTb.setCarNumber("");
            int update = wxpUserTbCommonDao.updateByConditions(wxpUserTb, wxpUserConditions);
            //2.2 查询该用户下车牌号
            wxpUserConditions = new WxpUserTb();
            wxpUserConditions.setUin(uin);
            WxpUserTb qryWxUserTb = wxpUserTbCommonDao.selectObjectByConditions(wxpUserConditions);
            String oldPlateNumber = "";
            if(qryWxUserTb!=null){
                oldPlateNumber = qryWxUserTb.getCarNumber();
            }
            //2.3 更新该车主车牌号
            wxpUserConditions = new WxpUserTb();
            wxpUserConditions.setUin(uin);
            wxpUserTb = new WxpUserTb();
            wxpUserTb.setCarNumber(carnumber);
            int r = wxpUserTbCommonDao.updateByConditions(wxpUserTb, wxpUserConditions);
            if(r > 0){
                //去泊链更新车牌
                if(!StringUtils.isNotNull(oldPlateNumber)){
                    syncUserAddPlateNumber(uin, carnumber, "");//添加
                }else{
                    syncUserAddPlateNumber(uin, oldPlateNumber, carnumber);//更新
                }
                return 1;
            }
        }
        return -4;
    }



    /**
     * 同步车牌到泊链
     * @param uin
     * @param plateNumber
     * @param newPlateNumber 为空时，是新加一个车牌
     */
    private void syncUserAddPlateNumber(final Long uin,final String plateNumber,final String newPlateNumber){
        // 1 判断是否微信注册用户
        UserInfoTb userInfoConditions = new UserInfoTb();
        userInfoConditions.setId(uin);
        userInfoConditions.setAuthFlag(4L);
        Integer isBind = userInfoTbCommonDao.selectCountByConditions(userInfoConditions);
        if(isBind==1){
            //1.1 查询微信用户状态
            userInfoConditions = new UserInfoTb();
            userInfoConditions.setId(uin);
            List<SearchBean> searchBeans = new ArrayList<SearchBean>();
            SearchBean searchBean = new SearchBean();
            searchBean.setFieldName("union_state");
            searchBean.setStartValue(0);
            searchBean.setOperator(FieldOperator.GREATER_THAN);
            searchBeans.add(searchBean);
            Integer count = userInfoTbCommonDao.selectCountByConditions(userInfoConditions,searchBeans);
            if(count==0){ //2.1 车主未同步到泊链平台,开始同步车主;
                syncUserToBolink(uin);
                return;
            }
        }else {
            //1.2  查询临时微信用户状态
            WxpUserTb wxUserConditions = new WxpUserTb();
            wxUserConditions.setUin(uin);
            List<SearchBean> searchBeans = new ArrayList<SearchBean>();
            SearchBean searchBean = new SearchBean();
            searchBean.setFieldName("union_state");
            searchBean.setStartValue(0);
            searchBean.setOperator(FieldOperator.GREATER_THAN);
            searchBeans.add(searchBean);
            Integer count = wxpUserTbCommonDao.selectCountByConditions(wxUserConditions,searchBeans);
            if(count==0){//2.2 微信临时车主未同步到泊链平台,开始同步车主
                syncUserToBolink(uin);
                return;
            }
        }
        bolinkUpdatePlateNumber(uin, plateNumber,newPlateNumber);
    }


    /**
     * 同步车主到泊链 //已同步过的车主，同步余额
     * @param uin 车主账户
     */
    private void syncUserToBolink(final Long uin){
        String loggstr ="upload user : ";
        BigDecimal money = new BigDecimal("0");
        String plateNumber = "";
        int isRegUser = 0;//用户是否注册用户 0未注册 1已注册
        List<CarInfoTb> carInfoTbList = null;//注册车主车牌
        //1 查询车主是否注册用户
        UserInfoTb userInfoConditions = new UserInfoTb();
        userInfoConditions.setId(uin);
        UserInfoTb userInfo = userInfoTbCommonDao.selectObjectByConditions(userInfoConditions);
        if(userInfo==null){//1.1 账户不存在 ，查询微信虚拟账户
            WxpUserTb wxpUserConditions = new WxpUserTb();
            wxpUserConditions.setUin(uin);
            WxpUserTb wxpUserTb = wxpUserTbCommonDao.selectObjectByConditions(wxpUserConditions);
            if(wxpUserTb!=null){
                Integer unionState  = wxpUserTb.getUnionState();
                if(unionState>0){//2.1 微信用户已是泊链会员，开始同步余额
                    syncUserBalance(uin);
                    return ;
                }
                plateNumber = wxpUserTb.getCarNumber();
                money = wxpUserTb.getBalance();
            }
        }else {//1.2 微信注册用户
            Integer unionState  = userInfo.getUnionState();
            isRegUser = 1;//是注册用户
            if(unionState>0){//2.2 用户已是泊链会员，开始同步余额
                syncUserBalance(uin);
                return ;
            }
            //2.3 微信车主车牌列表
            CarInfoTb carInfoConditions = new CarInfoTb();
            carInfoConditions.setUin(uin);
            carInfoTbList = carInfoTbCommonDao.selectListByConditions(carInfoConditions);
            logger.error(loggstr+"同步微信车主车牌号列表>>>"+carInfoTbList);
            //2.4 微信用户额度限制
            UserProfileTb userProfileConditions = new UserProfileTb();
            userProfileConditions.setUin(uin);
            UserProfileTb userProfileTb =  userProfileTbCommonDao.selectObjectByConditions(userProfileConditions);
            logger.error(loggstr+"同步微信车主额度限制>>>"+userProfileTb);
            Integer auto = userProfileTb.getAutoCash();
            Integer limit = userProfileTb.getLimitMoney();
            money = userInfo.getBalance();
            if(auto==1){//用户设置了限额时，余额和限额比较，哪个小，传给泊链哪个
                BigDecimal limitBig = new BigDecimal(String.valueOf(limit==null ? 0 : limit));
                logger.error(loggstr+"同步微信车主同步余额限制>>>"+limitBig);
                if(limit!=-1&&limitBig.compareTo(money) == -1)//0 等于 1 大于 -1小于
                    money = limitBig;
            }
        }
        //3 用户不存在
        if(userInfo==null){
            logger.error(loggstr+"同步车主错误，用户不存在....");
            return ;
        }
        //4 同步车住到泊链
        if(isRegUser==0){
            if(plateNumber==null||plateNumber.length()<7||plateNumber.length()>8){
                logger.error(loggstr+"同步微信车主错误，车牌不合法...."+plateNumber);
                return ;
            }
            //4.1 同步车主，车牌
            bolinkRegUser(uin,money,plateNumber,0);
        }else {//4.2 同步车主的车牌
            logger.error(loggstr+"开始上传车主车牌："+carInfoTbList);
            if(carInfoTbList!=null&&!carInfoTbList.isEmpty()){
                CarInfoTb syncCarInfo = carInfoTbList.get(0);//第一个车牌，在同步用户时传入
                plateNumber = syncCarInfo.getCarNumber();
                boolean isUpdated=false;//第一个车牌是否上传成功
                if(plateNumber!=null&&(plateNumber.length()==7||plateNumber.length()==8)){
                    int r = bolinkRegUser(uin,money,plateNumber,1);
                    if(r==1)
                        isUpdated=true;
                    logger.error(loggstr+"第一个车牌上传结果："+r);
                }else {
                    logger.error(loggstr+"同步车主错误，车牌不合法...."+plateNumber);
                }
                if(carInfoTbList.size()>1){//第二个以上车牌通过车牌添加接口上传
                    for(int i=1;i<carInfoTbList.size();i++){
                        CarInfoTb syncCar = carInfoTbList.get(i);
                        plateNumber = syncCar.getCarNumber();
                        if(plateNumber!=null&&(plateNumber.length()==7||plateNumber.length()==8)){
                            if(isUpdated){//第一个车牌已上传成功，调用上传更新车牌
                                logger.error(loggstr+"上传车主车牌："+plateNumber);
                                bolinkUpdatePlateNumber(uin,plateNumber,null);
                            }else {
                                int r = bolinkRegUser(uin,money,plateNumber,1);
                                logger.error(loggstr+"上传车主,车牌："+plateNumber+",结果 ："+r);
                                if(r==1)
                                    isUpdated = true;
                            }
                        }else {
                            logger.error(loggstr+"同步车主错误，车牌不合法...."+plateNumber);
                        }
                    }
                }
            }
        }
    }


    /**
     * 同步车主余额到泊链
     * @param uin
     */
    private void syncUserBalance(final Long uin){
        logger.error("update balance ,需要同步到泊链平台 uin:"+uin);
        BigDecimal money = new BigDecimal("0");
        // 1 判断是否微信注册用户
        UserInfoTb userInfoConditions = new UserInfoTb();
        userInfoConditions.setId(uin);
        userInfoConditions.setAuthFlag(4L);
        Integer isBindUser = userInfoTbCommonDao.selectCountByConditions(userInfoConditions);
        //WXUserView wxUserView =null;
        boolean isSend = false;
        if(isBindUser==0){//2.1 微信虚拟用户
            WxpUserTb wxUserConditions = new WxpUserTb();
            wxUserConditions.setUin(uin);
            WxpUserTb wxpUserTb = wxpUserTbCommonDao.selectObjectByConditions(wxUserConditions);
            if(wxpUserTb==null){
                logger.error("update balance 同步用户余额出错，微信用户不存在 ....");
                return ;
            }
            money = wxpUserTb.getBalance();
            isSend = true;
        }else {//3.1 微信注册用户
            userInfoConditions = new UserInfoTb();
            userInfoConditions.setId(uin);
            userInfoConditions.setAuthFlag(4L);
            UserInfoTb userInfoTb = userInfoTbCommonDao.selectObjectByConditions(userInfoConditions);
            if(userInfoTb==null){
                logger.error("update balance 同步用户余额出错，用户不存在 ....");
                return ;
            }
            // 3.2 微信用户额度限制
            UserProfileTb userProfileConditions = new UserProfileTb();
            userProfileConditions.setUin(uin);
            UserProfileTb userProfileTb =  userProfileTbCommonDao.selectObjectByConditions(userProfileConditions);
            if(userProfileTb==null){
                logger.error("update balance 同步用户余额出错，用户没有设置文件 ....");
                return;
            }
            BigDecimal balance = userInfoTb.getBalance();//用户当前余额
            Integer auto_cash = userProfileTb.getAutoCash();//是否自动支付，0不支付，1自动支付
            BigDecimal bolinkLimit = userProfileTb.getBolinkLimit();//当前泊链设置限额
            Integer limit_money = userProfileTb.getLimitMoney();//用户在停车宝平台限额，-1时是不限额度
            if(auto_cash==0){//用户关闭了自动支付
                money= new BigDecimal("0");
            }else {
                if(limit_money==-1){//用户设置了不封顶
                    money = balance;
                }else {//用户设置了限额值
                    BigDecimal limitBig = new BigDecimal(String.valueOf(limit_money== null ? 0 : limit_money));
                    money = balance.compareTo(limitBig) == 1 ? limitBig : balance;//余额和限额比较，取小的
                }
            }
            logger.error("update balance 同步余额，需要同步的余额："+money+"，泊链当前余额："+bolinkLimit);
            if(money!=bolinkLimit)//需要同步的余额与上次不同时，需要同步
                isSend = true;
        }
        if(!isSend){
            logger.error("update balance 同步用户余额，账户与泊链账户一致，不需要同步 ....");
            return;
        }
        //4 同步用户
        String url = Constants.UNIONIP+"user/updateuser";

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("user_id", uin);
        paramMap.put("type",1);//1更新余额，2删除会员 3更新或添加新车牌 4删除车牌
        paramMap.put("balance",money);
        paramMap.put("union_id", Constants.UNIONID);
        paramMap.put("rand", Math.random());
        String ret = "";
        try {
            StringUtils.createSign(paramMap);
            String param = StringUtils.createJson(paramMap);
            logger.error("update balance"+paramMap);
            HttpsProxy httpsProxy = new HttpsProxy();
            ret = httpsProxy.doPost(url, param, 20000, 20000);
            JSONObject object = JSONObject.parseObject(ret);
            logger.error("update balance>>>>>>>>>>>>>>>>>>"+ret);
            if(object!=null){
                Integer uploadState = object.getInteger("state");
                if(uploadState==1&&isBindUser==1){
                    UserProfileTb userProfileConditions = new UserProfileTb();
                    userProfileConditions.setUin(uin);
                    UserProfileTb userProfileTb = new UserProfileTb();
                    userProfileTb.setBolinkLimit(money);
                    int r = userProfileTbCommonDao.updateByConditions(userProfileTb,userProfileConditions);
                    logger.error("update balance,update bolink_limit :"+r);
                }else {
                    logger.error("update balance:"+object.get("errmsg"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 注册车主到泊链
     * @param uin 账户
     * @param money 余额
     * @param carNumber 车牌
     * @param type 0微信用户，1注册车主
     * @param type 0普通同步，1预付
     * @return
     */
    private int bolinkRegUser(Long uin,BigDecimal money,String carNumber,int type){
        String loggstr = "upload user to bolink : ";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("user_id", uin);
        paramMap.put("plate_number", carNumber);
        paramMap.put("balance", money);
        paramMap.put("union_id", Constants.UNIONID);
        paramMap.put("rand", Math.random());
        String ret = "";
        int uploadState=0;
        try {
            logger.error(loggstr+paramMap);
            StringUtils.createSign(paramMap);;
            String param = StringUtils.createJson(paramMap);
            logger.error(loggstr+param);
            String url = Constants.UNIONIP+"user/adduser";
            HttpsProxy httpsProxy = new HttpsProxy();
            ret = httpsProxy.doPost(url, param, 20000, 20000);
            JSONObject object = JSONObject.parseObject(ret);
            if(object!=null){
                uploadState = object.getInteger("state");
                if(uploadState==1){
                    if(type==1){
                        UserInfoTb userInfoCOnditions = new UserInfoTb();
                        userInfoCOnditions.setId(uin);
                        UserInfoTb userInfoTb = new UserInfoTb();
                        userInfoTb.setUploadUnionTime(System.currentTimeMillis()/1000);
                        userInfoTb.setUnionState(1);
                        int r = userInfoTbCommonDao.updateByConditions(userInfoTb,userInfoCOnditions);
                        logger.error(loggstr+"update user:"+r);
                        UserProfileTb userProfileConditions = new UserProfileTb();
                        userProfileConditions.setUin(uin);
                        UserProfileTb userProfileTb = new UserProfileTb();
                        userProfileTb.setBolinkLimit(money);
                        r = userProfileTbCommonDao.updateByConditions(userProfileTb,userProfileConditions);
                        logger.error(loggstr+"update userprofile:"+r);
                        return r;
                    }else {
                        WxpUserTb wxpUserConditios = new WxpUserTb();
                        wxpUserConditios.setUin(uin);
                        WxpUserTb wxpUserTb = new WxpUserTb();
                        wxpUserTb.setUnionState(1);
                        int r = wxpUserTbCommonDao.updateByConditions(wxpUserTb,wxpUserConditios);
                        logger.error(loggstr+"update weixin user :"+r);
                        return r;
                    }
                }else {
                    logger.error(object.get("errmsg"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * 添加车牌到泊链
     * @param uin
     * @param plateNumber
     */
    private void bolinkUpdatePlateNumber(Long uin,String plateNumber,String newPlateNumber){
        String logstr="upload user car_number : ";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("user_id", uin);
        paramMap.put("type", 3);
        paramMap.put("plate_number", plateNumber);
        paramMap.put("new_plate_number", newPlateNumber);
        paramMap.put("union_id", Constants.UNIONID);
        paramMap.put("rand", Math.random());
        String ret = "";
        int uploadState=0;
        try {
            logger.error(logstr+paramMap);
            StringUtils.createSign(paramMap);;
            String param = StringUtils.createJson(paramMap);
            logger.error(logstr+param);
            HttpsProxy httpsProxy = new HttpsProxy();
            ret = httpsProxy.doPost(Constants.UNIONIP+"user/updateuser", param, 20000, 20000);
            logger.error(logstr+ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
