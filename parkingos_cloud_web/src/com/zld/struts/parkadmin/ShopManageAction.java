package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.impl.MongoDbUtils;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.Check;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
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

public class ShopManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	private Logger logger = Logger.getLogger(ShopManageAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		request.setAttribute("authid", request.getParameter("authid"));
		if(comid==null){
			response.sendRedirect("login.do");
			return null;
		}
		if(comid==0)
			comid = RequestUtil.getLong(request, "comid", 0L);

		if(action.equals("")){
			request.setAttribute("comid", comid);
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			List<Object> params = new ArrayList<Object>();
			String sql = "select * from shop_tb where state=? and comid=? order by create_time desc";
			String countsql = "select count(*) from shop_tb where state=? and comid=? ";
			params.add(0);
			params.add(comid);
			Long count = daService.getCount(countsql, params);
			if(count > 0){
				list = daService.getAll(sql, params, pageNum, pageSize);
				for(Map<String, Object> map : list){
					if((int)map.get("ticket_type") == 1){//时长
						Integer ticket_unit = map.get("ticket_unit")==null ? 2 : (int) map.get("ticket_unit");
						//默认小时，原先只支持小时
						if(ticket_unit==1){//分钟
							map.put("ticket_limit_minute",map.get("ticket_limit"));
						}else if(ticket_unit==2){
							map.put("ticket_limit_hour",map.get("ticket_limit"));
						}else if(ticket_unit==3){
							map.put("ticket_limit_day",map.get("ticket_limit"));
						}
						map.put("ticket_money","");
					}else{//金额
						map.put("ticket_limit_minute","");
						map.put("ticket_limit_hour","");
						map.put("ticket_limit_day","");
					}
				}
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			Integer handInputEnable = RequestUtil.getInteger(request,"hand_input_enable",1);
			String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
			String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
			String mobile = RequestUtil.processParams(request, "mobile");
			String phone = RequestUtil.processParams(request, "phone");
			Integer ticket_type = RequestUtil.getInteger(request, "ticket_type",1);
			Integer ticket_unit = RequestUtil.getInteger(request, "ticket_unit", 1);//单位
			//Integer ticket_limit = RequestUtil.getInteger(request, "ticket_limit", 0);
			//Integer ticketfree_limit = RequestUtil.getInteger(request, "ticketfree_limit", 0);
			String default_limit = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "default_limit"));
			double discount_percent = RequestUtil.getDouble(request, "discount_percent",100.00);//商户折扣/%
			double discount_money = RequestUtil.getDouble(request, "discount_money",1.00);//商户折扣---每小时/元
			double free_money = RequestUtil.getDouble(request, "free_money",1.00);//全免劵单价---每张/元
			Integer validite_time = RequestUtil.getInteger(request, "validite_time", 0);//有效期/小时
			if(!Check.isEmpty(default_limit) && default_limit.contains("，")){
				default_limit.replaceAll("，", ",");
			}
			//数据校验
			if(0>=validite_time){
				AjaxUtil.ajaxOutput(response, "有效期必须输入正整数");
				return null;
			}
			if(!Check.isEmpty(default_limit) && !default_limit.contains(",")){
				AjaxUtil.ajaxOutput(response, "默认额度数据格式不对");
				return null;
			}
			String[] default_limits = default_limit.split(",");
			if(default_limits.length>3){
				AjaxUtil.ajaxOutput(response, "默认额度最多不能超过3个");
				return null;
			}
			int r = daService.update("insert into shop_tb(name,address,mobile,phone,comid,ticket_type,create_time,default_limit,discount_percent,discount_money,free_money,validite_time,ticket_unit,hand_input_enable) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
					new Object[] { name, address, mobile, phone, comid, ticket_type,System.currentTimeMillis() / 1000, default_limit, discount_percent, discount_money,free_money,validite_time,ticket_unit,handInputEnable});
			if(r==1)
				mongoDbUtils.saveLogs( request,0, 2, "添加了商户："+name+",地址："+address+",手机:"+mobile+",电话:"+phone);
			AjaxUtil.ajaxOutput(response, r+"");
			return null;
		}else if(action.equals("addmoney")){
			Long shoppingmarket_id = RequestUtil.getLong(request, "shop_id", -1L);
			if(shoppingmarket_id == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Map shopMap = daService.getMap("select * from shop_tb where id=? ", new Object[]{shoppingmarket_id});
			Integer ticket_time = RequestUtil.getInteger(request, "ticket_time", 0);
			//Integer ticket_time_type = RequestUtil.getInteger(request, "ticket_time_type",1);
			Integer ticket_money = RequestUtil.getInteger(request, "ticket_money", 0);
			Integer ticket_free = RequestUtil.getInteger(request, "ticketfree_limit", 0);
			double addmoney = RequestUtil.getDouble(request, "addmoney",0.00);
			//减免类型
			Integer ticket_type = Integer.parseInt(shopMap.get("ticket_type")+"");
			if(ticket_type == 1){
				if(0>ticket_time){
					AjaxUtil.ajaxOutput(response, "减免小时必须输入正整数");
					return null;
				}
			}else{
				if(0>ticket_money){
					AjaxUtil.ajaxOutput(response, "减免劵金额必须输入正整数");
					return null;
				}
			}
			if(0>ticket_free){
				AjaxUtil.ajaxOutput(response, "减免劵张数必须输入正整数");
				return null;
			}
			Integer ticket_limit = 0;
			Integer ticketfree_limit = 0;
			//减免劵(小时)
			ticket_limit += ticket_time;
			//全免劵(张)
			ticketfree_limit += ticket_free;
			int r = daService.update("update shop_tb set ticket_limit=ticket_limit+?, ticketfree_limit =ticketfree_limit+?, ticket_money=ticket_money+? where id=? ",
					new Object[] {ticket_limit, ticketfree_limit, ticket_money, shoppingmarket_id});
			mongoDbUtils.saveLogs( request,0,3, "商户:"+shopMap.get("name")+"续费:"+addmoney+"元");
			//记录缴费流水
			String username = (String)request.getSession().getAttribute("userid");
			long userid = -1;
			String strid = "";
			if(Check.checkUin(username)){
				userid = Long.parseLong(username);
			}else{
				strid = username;
			}


			int s =	daService.update("insert into shop_account_tb(shop_id,shop_name,ticket_limit,ticketfree_limit,ticket_money,add_money,operate_time,operator,park_id,strid,operate_type) values(?,?,?,?,?,?,?,?,?,?,?)",
					new Object[] { shopMap.get("id"), shopMap.get("name"), ticket_limit, ticketfree_limit, ticket_money,addmoney,System.currentTimeMillis() / 1000, userid,comid,strid,1});
			AjaxUtil.ajaxOutput(response, r+"");
			return null;
		}else if(action.equals("edit")){
			Long shoppingmarket_id = RequestUtil.getLong(request, "id", -1L);
			if(shoppingmarket_id == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Integer handInputEnable = RequestUtil.getInteger(request,"hand_input_enable",1);
			String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
			String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
			String mobile = RequestUtil.processParams(request, "mobile");
			String phone = RequestUtil.processParams(request, "phone");
			Integer ticket_type = RequestUtil.getInteger(request, "ticket_type",1);
			Integer ticket_unit = RequestUtil.getInteger(request, "ticket_unit", 1);//单位
			//Integer ticket_limit = RequestUtil.getInteger(request, "ticket_limit", 0);
			//Integer ticketfree_limit = RequestUtil.getInteger(request, "ticketfree_limit", 0);
			String default_limit = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "default_limit"));
			double discount_percent = RequestUtil.getDouble(request, "discount_percent",100.00);//商户折扣/%
			double discount_money = RequestUtil.getDouble(request, "discount_money",1.00);//商户折扣---每小时/元
			double free_money = RequestUtil.getDouble(request, "free_money",1.00);//全免劵单价---每张/元
			Integer validite_time = RequestUtil.getInteger(request, "validite_time", 0);//有效期/小时
			if(0>=validite_time){
				AjaxUtil.ajaxOutput(response, "有效期必须输入正整数");
				return null;
			}
			if(!Check.isEmpty(default_limit) && default_limit.contains("，")){
				default_limit = default_limit.replaceAll("，", ",");
			}
			int r = daService.update("update shop_tb set name=?,address=?,mobile=?,phone=?," +
							"default_limit=?,ticket_type=?,discount_percent=?, discount_money=?, free_money=?,validite_time=?, ticket_unit=?,hand_input_enable=? where id=? ",
					new Object[] { name, address, mobile, phone, default_limit, ticket_type,discount_percent,discount_money,free_money,validite_time,ticket_unit,handInputEnable,shoppingmarket_id});
			if(r==1)
				mongoDbUtils.saveLogs( request,0, 3, "修改了商户："+name+",地址："+address+",手机:"+mobile+",电话:"+phone);
			AjaxUtil.ajaxOutput(response, r+"");
		}else if(action.equals("delete")){
			Long shoppingmarket_id = RequestUtil.getLong(request, "selids", -1L);
			if(shoppingmarket_id == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Map shopMap = daService.getMap("select * from shop_tb where id=? ", new Object[]{shoppingmarket_id});
			int r = daService.update("update shop_tb set state=? where id=? ",
					new Object[] { 1, shoppingmarket_id });
			if(r==1)
				mongoDbUtils.saveLogs( request,0, 4, "删除了商户："+shopMap);
			AjaxUtil.ajaxOutput(response, r+"");
		}else if(action.equals("setting")){
			Long shop_id = RequestUtil.getLong(request, "id", -1L);
			request.setAttribute("shop_id", shop_id);
			return mapping.findForward("setlist");
		}else if(action.equals("getShop")){
			Long shop_id = RequestUtil.getLong(request, "shop_id", -1L);
			Map shopMap = daService.getMap("select * from shop_tb where id=? ", new Object[]{shop_id});
			String json = JsonUtil.createJsonforMap(shopMap);
			AjaxUtil.ajaxOutput(response, json);
		}
		return null;
	}

}
