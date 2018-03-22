package com.zld.struts.group;

import com.zld.AjaxUtil;
import com.zld.pojo.*;
import com.zld.service.CardService;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupCardManageAction extends Action{
	@Autowired
	private PgOnlyReadService readService;
	@Autowired
	private DataBaseService writeService;
	@Autowired
	private CardService cardService;

	Logger logger = Logger.getLogger(GroupCardManageAction.class);

	@SuppressWarnings("rawtypes")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("query")){
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Map<String, Object>> list = null;
			List<Object> params = new ArrayList<Object>();
			params.add(groupid);
			params.add(0);
			params.add(2);
			String sql = "select * from com_nfc_tb where group_id=? and is_delete=? and type=? ";
			String countSql = "select count(id) from com_nfc_tb where " +
					"group_id=? and is_delete=? and type=? ";
			SqlInfo sqlInfo = RequestUtil.customSearch(request, "nfc_tb", "", new String[]{"mobile"});
			SqlInfo sqlInfo2 = getSqlInfo1(request);
			SqlInfo sqlInfo3 = getSqlInfo2(request);
			if(sqlInfo != null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			if(sqlInfo2 != null){
				countSql+=" and "+ sqlInfo2.getSql();
				sql +=" and "+sqlInfo2.getSql();
				params.addAll(sqlInfo2.getParams());
			}
			if(sqlInfo3 != null){
				countSql+=" and "+ sqlInfo3.getSql();
				sql +=" and "+sqlInfo3.getSql();
				params.addAll(sqlInfo3.getParams());
			}
			sql += " order by create_time desc ";
			Long count = readService.getCount(countSql, params);
			if(count > 0){
				list = readService.getAll(sql, params, pageNum, pageSize);
				setInfo(list);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("charge")){
			Long cardId = RequestUtil.getLong(request, "id", -1L);
			Double money = RequestUtil.getDouble(request, "money", 0d);
			logger.error("cardId:"+cardId+",money:"+money);
			CardChargeReq req = new CardChargeReq();
			req.setCardId(cardId);
			req.setCashierId(uin);
			req.setChargeType(0);
			req.setMoney(money);
			req.setGroupId(groupid);
			BaseResp resp = cardService.cardCharge(req);
			if(resp.getResult() == 1){
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, resp.getErrmsg());
			}
		}else if(action.equals("return")){//注销
			Long cardId = RequestUtil.getLong(request, "id", -1L);
			ReturnCardReq req = new ReturnCardReq();
			req.setCardId(cardId);
			req.setUnBinder(uin);
			req.setGroupId(groupid);
			BaseResp resp = cardService.returnCard(req);
			if(resp.getResult() == 1){
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, resp.getErrmsg());
			}
		}else if(action.equals("bind")){
			Long cardId = RequestUtil.getLong(request, "id", -1L);
			String mobile = RequestUtil.processParams(request, "mobile");
			String carNumber = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "carnumber")).toUpperCase();
			BindCardReq req = new BindCardReq();
			req.setBinder(uin);
			req.setCardId(cardId);
			req.setMobile(mobile);
			req.setCarNumber(carNumber);
			req.setGroupId(groupid);
			BaseResp resp = null;
			if(mobile == null || "".equals(mobile)){
				resp = cardService.bindPlateCard(req);
			}else{
				resp = cardService.bindUserCard(req);
			}
			if(resp.getResult() == 1){
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, resp.getErrmsg());
			}
		}else if(action.equals("delete")){
			Long cardId = RequestUtil.getLong(request, "id", -1L);
			Long count = readService.getLong("select count(id) from com_nfc_tb " +
							" where state<>? and id=? and is_delete=? and type=? ",
					new Object[]{1, cardId, 0, 2});
			if(count > 0){
				AjaxUtil.ajaxOutput(response, "-2");
				return null;
			}
			int r = writeService.update("update com_nfc_tb set is_delete=? " +
					" where id=? and type=? ", new Object[]{1, cardId, 2});
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			Long cardId = RequestUtil.getLong(request, "id", -1L);
			String card_name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "card_name"));
			if("".equals(card_name)) card_name = null;
			int r = writeService.update("update com_nfc_tb set card_number=? " +
					" where id=?", new Object[]{card_name, cardId});
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("unbind")){
			Long cardId = RequestUtil.getLong(request, "id", -1L);
			UnbindCardReq req = new UnbindCardReq();
			req.setCardId(cardId);
			req.setUnBinder(uin);
			req.setGroupId(groupid);
			BaseResp resp = cardService.unBindCard(req);
			if(resp.getResult() == 1){
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, resp.getErrmsg());
			}
		}
		return null;
	}

	private void setInfo(List<Map<String, Object>> list){
		try {
			if(list != null && !list.isEmpty()){
				List<Object> uinList = new ArrayList<Object>();
				String preParams  ="";
				for(Map<String, Object> map : list){
					Long uin = (Long)map.get("uin");
					Long state = (Long)map.get("state");
					if(state == 2 && uin > 0){
						uinList.add(uin);
						if(preParams.equals(""))
							preParams ="?";
						else
							preParams += ",?";
					}
				}

				List<Map<String, Object>> list2 = null;
				if(!uinList.isEmpty()){
					list2 = readService.getAllMap("select id,mobile from user_info_tb where id in ("+preParams+")", uinList);
					if(list2 != null && !list2.isEmpty()){
						for(Map<String, Object> map : list2){
							Long id  = (Long)map.get("id");
							for(Map<String, Object> map2 : list){
								Long uin = (Long)map2.get("uin");
								if(id.intValue() == uin.intValue()){
									map2.put("mobile", map.get("mobile"));
									//此处没有break，因为一个人可以有多张卡
								}
							}
						}
					}
					List<Object> paramList = new ArrayList<Object>();
					paramList.addAll(uinList);
					paramList.add(1);
					List<Map<String, Object>> list3 = readService.getAllMap(
							"select uin,car_number from car_info_tb where uin in ("+preParams+")" +
									" and state=? ", paramList);
					if(list3 != null && !list3.isEmpty()){
						for(Map<String, Object> map : list3){
							Long uin = (Long)map.get("uin");
							if(uin > 0){
								for(Map<String, Object> map2 : list){
									Long id = (Long)map2.get("uin");
									if(id.intValue() == uin.intValue()){
										if(map2.get("carnumber") == null){
											map2.put("carnumber", map.get("car_number"));
										}else{
											String carnumber = (String)map2.get("carnumber");
											carnumber = carnumber + "," + map.get("car_number");
											map2.put("carnumber", carnumber);
										}
									}
								}
							}
						}
					}
				}
				List<Object> cardIdList = new ArrayList<Object>();
				preParams = "";
				for(Map<String, Object> map : list){
					Long state = (Long)map.get("state");
					if(state == 4){//只绑定车牌号
						cardIdList.add(map.get("id"));
						if(preParams.equals(""))
							preParams ="?";
						else
							preParams += ",?";
					}
				}

				if(!cardIdList.isEmpty()){
					cardIdList.add(0);
					List<Map<String, Object>> list4 = readService.getAllMap("select card_id,car_number " +
							" from card_carnumber_tb where card_id in ("+preParams+") and is_delete=? ", cardIdList);
					if(list4 != null && !list4.isEmpty()){
						for(Map<String, Object> map : list4){
							Long cardId = (Long)map.get("card_id");
							for(Map<String, Object> map2 : list){
								Long id = (Long)map2.get("id");
								if(id.intValue() == cardId.intValue()){
									if(map2.get("carnumber") == null){
										map2.put("carnumber", map.get("car_number"));
									}else{
										String carnumber = (String)map2.get("carnumber");
										carnumber = carnumber + "," + map.get("car_number");
										map2.put("carnumber", carnumber);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private SqlInfo getSqlInfo1(HttpServletRequest request){
		String mobile = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "mobile"));
		SqlInfo sqlInfo = null;
		if(!mobile.equals("")){
			sqlInfo = new SqlInfo(" uin in (select id from user_info_tb" +
					" where mobile like ?)  ",new Object[]{"%"+mobile+"%"});
		}
		return sqlInfo;
	}

	private SqlInfo getSqlInfo2(HttpServletRequest request){
		String car_number = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "carnumber"));
		SqlInfo sqlInfo = null;
		if(!car_number.equals("")){
			sqlInfo = new SqlInfo(" ((state=? and uin in (select uin from car_info_tb where " +
					" car_number like ? and state=? )) or (state=? and id in (select card_id " +
					" from card_carnumber_tb where car_number like ? and is_delete=? ))) ",
					new Object[]{2, "%"+car_number+"%", 1, 4, "%"+car_number+"%", 0});
		}
		return sqlInfo;
	}
}
