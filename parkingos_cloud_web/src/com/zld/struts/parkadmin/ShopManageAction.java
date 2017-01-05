package com.zld.struts.parkadmin;

import java.util.ArrayList;
import java.util.List;
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
import com.zld.impl.MongoDbUtils;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;

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
			String sql = "select * from shop_tb where state=? and comid=? ";
			String countsql = "select count(*) from shop_tb where state=? and comid=? ";
			params.add(0);
			params.add(comid);
			Long count = daService.getCount(countsql, params);
			if(count > 0){
				list = daService.getAll(sql, params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
			String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
			String mobile = RequestUtil.processParams(request, "mobile");
			String phone = RequestUtil.processParams(request, "phone");
			Integer ticket_limit = RequestUtil.getInteger(request, "ticket_limit", 0);
			Integer ticketfree_limit = RequestUtil.getInteger(request, "ticketfree_limit", 0);
			int r = daService.update("insert into shop_tb(name,address,mobile,phone,comid,ticket_limit,create_time,ticketfree_limit) values(?,?,?,?,?,?,?,?)",
							new Object[] { name, address, mobile, phone, comid, ticket_limit, System.currentTimeMillis() / 1000, ticketfree_limit });
			if(r==1)
				mongoDbUtils.saveLogs( request,0, 2, "添加了商户："+name+",地址："+address+",手机:"+mobile+",电话:"+phone);
			AjaxUtil.ajaxOutput(response, r+"");
			return null;
		}else if(action.equals("edit")){
			Long shoppingmarket_id = RequestUtil.getLong(request, "id", -1L);
			if(shoppingmarket_id == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
			String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
			String mobile = RequestUtil.processParams(request, "mobile");
			String phone = RequestUtil.processParams(request, "phone");
			Integer ticket_limit = RequestUtil.getInteger(request, "ticket_limit", 0);
			Integer ticketfree_limit = RequestUtil.getInteger(request, "ticketfree_limit", 0);
			int r = daService.update("update shop_tb set name=?,address=?,mobile=?,phone=?,ticket_limit=?,ticketfree_limit=? where id=? ",
					new Object[] { name, address, mobile, phone, ticket_limit, ticketfree_limit, shoppingmarket_id });
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
		}
		return null;
	}
	
}
