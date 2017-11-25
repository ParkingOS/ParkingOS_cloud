package parkingos.com.bolink.service.impl;

import com.zld.common_dao.dao.CommonDao;
import com.zld.common_dao.enums.FieldOperator;
import com.zld.common_dao.qo.SearchBean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import parkingos.com.bolink.beans.CarInfoTb;
import parkingos.com.bolink.beans.CarowerProduct;
import parkingos.com.bolink.beans.ComInfoTb;
import parkingos.com.bolink.beans.ProductPackageTb;
import parkingos.com.bolink.component.TcpComponent;
import parkingos.com.bolink.service.WeixinProdService;
import parkingos.com.bolink.utlis.CheckUtil;
import parkingos.com.bolink.utlis.StringUtils;
import parkingos.com.bolink.utlis.TempDataUtil;
import parkingos.com.bolink.utlis.TimeTools;
import parkingos.com.bolink.vo.ProdPriceView;
import parkingos.com.bolink.vo.ProdView;

import java.util.*;

@Service
public class WeixinProdServiceImpl implements WeixinProdService {

    Logger logger = Logger.getLogger(WeixinProdServiceImpl.class);

    @Autowired
    CommonDao<CarowerProduct> carOwnerProductCommonDao;
    @Autowired
    CommonDao<CarInfoTb> carInfoCommonDao;
    @Autowired
    CommonDao<ComInfoTb> comInfoCommonDao;
    @Autowired
    CommonDao<ProductPackageTb> productPackageCommonDao;

    @Autowired
    TcpComponent tcpComponent;

    @Override
    public List<ProdView> getProdList(Long uin) {

        //1.查询该用户的所有车牌
        CarInfoTb carInfoConditions = new CarInfoTb();
        carInfoConditions.setUin(uin);
        List<CarInfoTb> carInfos = carInfoCommonDao.selectListByConditions(carInfoConditions);
        //2.遍历车牌,对应每个车牌查询月卡信息
        List<Long> uList = new ArrayList<Long>();
        List<ProdView> prodViews = new ArrayList<ProdView>();
        Long currentTime = System.currentTimeMillis()/1000;
        if(CheckUtil.hasElement(carInfos)){
            for (CarInfoTb carInfo : carInfos) {
                CarowerProduct carOwnerProductConditions = new CarowerProduct();
                //carOwnerProductConditions.setUin(uin);
                List<SearchBean> searchBeans = new ArrayList<SearchBean>();
                SearchBean searchBean = new SearchBean();
                searchBean.setBasicValue(carInfo.getCarNumber());
                searchBean.setOperator(FieldOperator.LIKE);
                searchBean.setFieldName("car_number");
                searchBeans.add(searchBean);
                List<CarowerProduct> carOwnerProducts = carOwnerProductCommonDao.selectListByConditions(carOwnerProductConditions, searchBeans);
                if(CheckUtil.hasElement(carOwnerProducts)){
                    for (CarowerProduct carOwnerProduct : carOwnerProducts) {
                        ProdView prodView = new ProdView();//月卡视图对象
                        Long carOwnerProductId = carOwnerProduct.getId();
                        //如果包含id,说明该用户已经获取这条月卡记录,调过
                        if(uList.contains(carOwnerProductId)){
                            //break;
                            continue;
                        }
                        uList.add(carOwnerProductId);
                        //获取月卡对应车牌,多个
                        String carNumber = carOwnerProduct.getCarNumber();
                        String[] carNumbers = carNumber.split("\\|");
                        String newCarNumber = "";
                        for (String number : carNumbers) {
                            newCarNumber += number+",";
                        }
                        newCarNumber = newCarNumber.substring(0, newCarNumber.length()-1);

                        //月卡套餐
                        Long pid = carOwnerProduct.getPid();
                        //pid=-1L;
                        ProductPackageTb  productPackageTb = null;
                        if(pid!=null&&pid>0){
                            //TODO 有月卡套餐
                            ProductPackageTb productPackageCondition = new ProductPackageTb();
                            productPackageCondition.setId(pid);
                            productPackageTb = productPackageCommonDao.selectObjectByConditions(productPackageCondition);
                            prodView.setProdName(productPackageTb.getpName());
                            prodView.setProdId(pid);

                        }else{
                            prodView.setProdName("月卡");
                            prodView.setProdId(-1L);
                        }

                        Long beginTime = carOwnerProduct.getbTime();
                        Long endTime = carOwnerProduct.geteTime();
                        String cardId = carOwnerProduct.getCardId();
                        Long comId = carOwnerProduct.getComId();
                        ComInfoTb comInfoConditions = new ComInfoTb();
                        comInfoConditions.setId(comId);
                        ComInfoTb comInfo = comInfoCommonDao.selectObjectByConditions(comInfoConditions);
                        String parkName = comInfo.getCompanyName();
                        //月卡价格
                        Double price = carOwnerProduct.getTotal().doubleValue();

                        String limitDate = TimeTools.getTimeStr_yyyy_MM_dd(beginTime*1000) +" 至  "+ TimeTools.getTimeStr_yyyy_MM_dd(endTime*1000);
                        int state = 0;//月卡状态 0：未开始 1:使用中 2已过期
                        if(beginTime <= currentTime){
                            if(endTime > currentTime){
                                state = 1;//正在使用中
                            }else{
                                state = 2;//已过期
                            }
                        }
                        prodView.setEndTime(endTime);
                        prodView.setComId(comId);
                        prodView.setCardId(cardId);
                        prodView.setPrice(price);
                        prodView.setCarNumber(newCarNumber);
                        prodView.setCarOwnerProductId(carOwnerProductId);
                        prodView.setLimitDate(limitDate);
                        prodView.setState(state);
                        prodView.setParkName(parkName);
                        logger.info(prodView);
                        prodViews.add(prodView);
                    }
                }
            }
        }
        //3.返回月卡信息
        return prodViews;
    }

    @Override
    public ProdPriceView getProdPrice(Long comId, String cardId, Long startTime, Integer months) {

        String seed = (new Random().nextDouble()+"").substring(2, 6);
        String tradeNo = TimeTools.getTimeYYYYMMDDHHMMSS()+seed+comId;

        //组织查询月卡价格参数
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("service_name", "query_prodprice");
        params.put("card_id", cardId);
        params.put("months",months);
        params.put("start_time", startTime);
        params.put("trade_no", tradeNo);
        params.put("comid", comId);
        String data = StringUtils.createLinkString(params);
        int ret = tcpComponent.sendMessageToSDK(comId, data);

        int state = -1;
        String money = "0.000";
        String errMsg = "查询价格成功";
        ProdPriceView prodPriceView = new ProdPriceView();
        prodPriceView.setState(ret);
        if(ret==0){
           //系统错误
            errMsg = "网络错误";
        }else if(ret==1){
            //发送成功,循环从redis中查询价格
            int i = 1;
            long start = System.currentTimeMillis()/1000;
            for(long s=start;s<=start+2;s=System.currentTimeMillis()/1000){
                Map<String, Object> monthPrice = TempDataUtil.monthPrice;
                Object value = monthPrice.get(tradeNo);
                logger.error("getlocalprice 从缓存查询价格第"+i+"次"+"=>"+value);
                if(value!=null){
                    String[] split = value.toString().split("_");
                    state = Integer.valueOf(split[0]);
                    if(state==1){
                        money = split[1];
                    }else if(state==0){
                        errMsg = StringUtils.decodeUTF8(split[1]);
                    }
                    monthPrice.remove(tradeNo);
                    break;
                }
                try {
                    Thread.currentThread().sleep(250);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
                i++;
            }
            logger.error("getlocalprice 调用sdk查询价格,开始查询缓存中价格："+money);
            if("0.000".equals(money)){
                prodPriceView.setState(0);
            }
        }else if(ret==2){
            //发送失败
            errMsg = "网络异常";
        }else if(ret==3){
            //车场离线
            errMsg = "车场网络异常";
        }
        prodPriceView.setErrmsg(errMsg);
        prodPriceView.setPrice(money);
        prodPriceView.setTradeNo(tradeNo);
        return prodPriceView;
    }

}
