package parkingos.com.bolink.actions;

import com.alibaba.fastjson.JSONObject;
import com.zld.common_dao.dao.CommonDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import parkingos.com.bolink.utils.Encryption;
import parkingos.com.bolink.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/user")
public class LoginAction {

	Logger logger = Logger.getLogger(LoginAction.class);
	@Autowired
	private CommonDao commonDao;

	@RequestMapping(value = "/dologin")
	public String dologin(HttpServletRequest req, HttpServletResponse resp) {

		//测试期间设置登录有效期为1小时
		JSONObject result = JSONObject.parseObject("{}");
		String userId = req.getParameter("username");
		String cpasswd = req.getParameter("password");
		String passwd = Encryption.decryptToAESPKCS5(cpasswd, Encryption.KEY);

		logger.info(">>>>>>>用户 " + userId + " 正在登录系统!");
		Long uid = -1L;
		result.put("state",true);
		result.put("token","xuluxuluxluxewrwerwe");
		JSONObject user = JSONObject.parseObject("{}");
		user.put("comid",1222);
		user.put("nickname","liu");
		user.put("roleid",4);
		user.put("userid",userId);
		user.put("lastlogin",System.currentTimeMillis()/1000);
		user.put("parkid",21879);
		result.put("user",user);
		//根据用户名查询用户
		logger.info(result);
		StringUtils.ajaxOutput(resp,result.toJSONString());
		return null;
	}
}