package com.zld.struts.request;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.AjaxUtil;
import com.zld.pojo.BaseResp;
import com.zld.pojo.RegCardReq;
import com.zld.pojo.UserToken;
import com.zld.service.CardService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;

public class CardAction extends Action {
	@Autowired
	private CardService cardService;
	@Autowired
	private PgOnlyReadService readService;
	
	private Logger logger = Logger.getLogger(CardAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String token =RequestUtil.processParams(request, "token");
		String action =RequestUtil.processParams(request, "action");
		Map<String,Object> infoMap = new HashMap<String, Object>();
		Long comId = null;
		Long uin = null;
		Long groupId = null;
		response.setContentType("application/json");
		if(token.equals("")){
			infoMap.put("info", "no token");
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
			return null;
		}
		UserToken userToken = readService.getPOJO("select * from " +
				"user_session_tb where token=?", new Object[]{token}, 
				UserToken.class);
		if(userToken == null){
			infoMap.put("info", "token is invalid");
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
			return null;
		}
		comId = userToken.getComid();
		uin = userToken.getUin();
		groupId = userToken.getGroupid();
		logger.error("uin:"+uin+",comid:"+comId+",groupid:"+groupId);
		
		if(action.equals("regcard")){
			String nfc_uuid = RequestUtil.processParams(request, "uuid");
			String cardNo = RequestUtil.processParams(request, "cardno");
			Double money = RequestUtil.getDouble(request, "money", 0d);
			String cardName = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "cardname"));
			String device = RequestUtil.processParams(request, "device");
			
			RegCardReq req = new RegCardReq();
			req.setNfc_uuid(nfc_uuid);
			req.setGroupId(groupId);
			req.setRegId(uin);
			req.setMoney(money);
			req.setCardName(cardName);
			req.setCardNo(cardNo);
			req.setDevice(device);
			req.setParkId(comId);
			
			BaseResp resp = cardService.regCard(req);
			logger.error(resp.toString());
			if(resp.getResult() == 1){
				infoMap.put("result", 1);
			}else{
				infoMap.put("result", 0);
			}
			infoMap.put("errmsg", resp.getErrmsg());
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
			//http://127.0.0.1/zld/card.do?action=regcard&uuid=04c0272a003e80&money=100&token=a31ebf02e3ce4083a5d28c964075fa19
		}
		return null;
	}
}
