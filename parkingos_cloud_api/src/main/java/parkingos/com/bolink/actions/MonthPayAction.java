package parkingos.com.bolink.actions;

import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import parkingos.com.bolink.beans.CarInfoTb;
import parkingos.com.bolink.beans.UserInfoTb;
import parkingos.com.bolink.service.WeixinProdService;
import parkingos.com.bolink.utlis.AjaxUtil;
import parkingos.com.bolink.utlis.Check;
import parkingos.com.bolink.utlis.CommonUtils;
import parkingos.com.bolink.utlis.RequestUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class MonthPayAction {

	Logger logger = Logger.getLogger(UploadCarPics.class);
	@Autowired
	CommonDao commonDao;
	@Autowired
	CommonUtils commonUtils;
	@Autowired
	WeixinProdService weixinProdService;

	/**
	 * 非公众号月卡续费扫码入口
	 * @throws Exception
	 */
	@RequestMapping(value = "/elecpay")
	public String prepayMonth(HttpServletRequest request) throws Exception{
		String userAgent = request.getHeader("user-agent");//"AlipayClient";//
		boolean isAlipay = userAgent.indexOf("AlipayClient") != -1;
		logger.error("user-agent:" + userAgent);

		//判断是否是支付宝扫码支付
		if(isAlipay) {
			request.setAttribute("errmsg","请用微信扫码，暂不支持支付宝扫码~");
			return "error";
		}
		//去车牌添加页面
		return "/wxpublic/addcar";
	}

	/**
	 * 查询月卡信息
	 * @throws Exception
	 */
	@RequestMapping(value = "/querymonth")
	public String queryMonth(HttpServletRequest request) throws Exception{
		//取车牌
		String carNumber = AjaxUtil.decodeUTF8(RequestUtil.getString(request,"carnumber"));
		if(Check.isEmpty(carNumber)){
			return "/wxpublic/addcar";
		}
		Integer payType = RequestUtil.getInteger(request,"paytype",0);
		Long uin = -1L;
		logger.info(carNumber);
		CarInfoTb carInfoTb = new CarInfoTb();
		carInfoTb.setCarNumber(carNumber);
		List<CarInfoTb> infoTbList = commonDao.selectListByConditions(carInfoTb);
		if(infoTbList!=null&&!infoTbList.isEmpty()){
			carInfoTb = infoTbList.get(0);
			uin = carInfoTb.getUin();
		}else{
			uin = commonDao.selectSequence(UserInfoTb.class);
			carInfoTb.setUin(uin);
			carInfoTb.setCreateTime(System.currentTimeMillis()/1000);
			int r = commonDao.insert(carInfoTb);
			logger.info("写入车牌："+r);
		}
		logger.info(uin);
		request.setAttribute("uin",uin);
		request.setAttribute("from","page");
		if(payType==1)
			return "wxpublic/curorderlist";
		return "wxpublic/parkprod";
//		List<ProdView> prodList = weixinProdService.getProdList(carNumber);
//		AjaxUtil.ajaxOutputWithSnakeCase(response,prodList);
//		return "error";
	}

}
