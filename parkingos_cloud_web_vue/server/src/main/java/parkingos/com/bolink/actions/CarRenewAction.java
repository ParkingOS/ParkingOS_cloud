package parkingos.com.bolink.actions;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zld.common_dao.dao.CommonDao;
import com.zld.common_dao.qo.PageOrderConfig;
import com.zld.common_dao.util.OrmUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import parkingos.com.bolink.models.CardRenewTb;
import parkingos.com.bolink.utils.Encryption;
import parkingos.com.bolink.utils.RequestUtil;
import parkingos.com.bolink.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/carrenew")
public class CarRenewAction {

	Logger logger = Logger.getLogger(CarRenewAction.class);
	@Autowired
	private CommonDao commonDao;

	@RequestMapping(value = "/query")
	public String query(HttpServletRequest req, HttpServletResponse resp) {

		//测试期间设置登录有效期为1小时
		String str = "{\"total\":12,\"page\":1,\"money\":\"应收 222.03元，实收 41.23元\",\"rows\":[]}";
		JSONObject result = JSONObject.parseObject(str);
		Integer pageNum = RequestUtil.getInteger(req,"page",1);
		Integer pageSize = RequestUtil.getInteger(req,"rp",20);
		CardRenewTb cardRenewTb  = new CardRenewTb();
		cardRenewTb.setComid("21782");
		int count = commonDao.selectCountByConditions(cardRenewTb);
		if(count>0){
			/**分页处理*/
			PageOrderConfig config = new PageOrderConfig();
			config.setPageInfo(pageNum,pageSize);
			List<CardRenewTb> list  =commonDao.selectListByConditions(cardRenewTb,config);
			List<Map<String,Object>> resList  = new ArrayList<>();
			if(list!=null&&!list.isEmpty()){
				for(CardRenewTb renewTb : list){
					OrmUtil<CardRenewTb> otm = new OrmUtil<>();
					Map<String,Object> map = otm.pojoToMap(renewTb);
					resList.add(map);
				}
				result.put("rows", JSON.toJSON(resList));
			}
			result.put("total",count);
			result.put("page",pageNum);
		}
		logger.info(result);
		StringUtils.ajaxOutput(resp,result.toJSONString());
		return null;
	}
}