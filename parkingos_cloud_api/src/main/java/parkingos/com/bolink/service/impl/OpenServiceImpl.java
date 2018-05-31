package parkingos.com.bolink.service.impl;


import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import parkingos.com.bolink.beans.ComInfoTb;
import parkingos.com.bolink.service.OpenService;
import parkingos.com.bolink.utlis.Check;
import parkingos.com.bolink.utlis.StringUtils;



//
@Service
public class OpenServiceImpl implements OpenService{
    private Logger logger = Logger.getLogger(OpenServiceImpl.class);

    @Autowired
    private CommonDao commonDao;

    @Override
    public boolean checkSign(String data, String sign, Object key) {

            if(key == null){
                com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(data);
                Long parkId = jsonObject.getLong("comid");
//
                if(Check.isLong(parkId+"")){
                    ComInfoTb comInfoTb = new ComInfoTb();
                    comInfoTb.setId(Long.valueOf(parkId));
                    comInfoTb = (ComInfoTb)commonDao.selectObjectByConditions(comInfoTb);
//
                    if (comInfoTb != null) {
                        String ukey = comInfoTb.getUkey();//String.valueOf(comInfoMap.get("ukey"));
                        String strKey = data+ "key="+ ukey;
                        try {
                            logger.error("====准备验签:"+strKey);
                            String _sign = StringUtils.MD5(strKey,"utf-8").toUpperCase();
//                            String _sign =  MD5.getMessageDigest(strKey.getBytes("UTF-8")).toUpperCase();
                            logger.error("验签:"+parkId+strKey + "," + _sign + ":" + sign
                                    + ",ret:" + _sign.equals(sign));
                            if (_sign.equals(sign)) {
                                return true;
                            }else{
                                logger.error("error:签名错误");
                                return false;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("error:md5加密出现异常,请联系后台管理员！！！");
                        }
                    }else{
                        logger.error("车场不存在......");
                        return false;
                    }
                }
            }
            String _sign = StringUtils.MD5(data + "key=" + key).toUpperCase();
            logger.info("cloud check sign>>>>"+sign + ":" + _sign + ",ret:" + _sign.equals(sign));
            return _sign.equals(sign);
    }
}
