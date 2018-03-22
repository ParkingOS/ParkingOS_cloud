package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
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

public class ShopTicketManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	
	private Logger logger = Logger.getLogger(ShopTicketManageAction.class);
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
		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String orderfield = RequestUtil.processParams(request, "orderfield");
			String orderby = RequestUtil.processParams(request, "orderby");
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			List<Object> params = new ArrayList<Object>();
			if("".equals(orderfield)){
				orderfield = " id";
			}
			if("".equals(orderby)){
				orderby = " desc nulls last";
			}else {
				orderby += " nulls last";
			}
			String sql = "select t.*,s.name shop_name from ticket_tb t,shop_tb s where t.comid= ? and t.shop_id = s.id order by t." + orderfield + " " + orderby+" , t.id desc ";
			String countsql = "select count(*) from ticket_tb where comid= ?";
			params.add(comid);
			Long count = daService.getCount(countsql, params);
			if(count > 0){
				list = daService.getAll(sql, params, pageNum, pageSize);
				for(Map<String, Object> map : list){
					Map shopMap = daService.getMap("select * from shop_tb where id=? ", new Object[]{map.get("shop_id")});
					if((int)map.get("type") == 3){//时长
						Integer ticket_unit = shopMap.get("ticket_unit")==null ? 2 : (int) shopMap.get("ticket_unit");
						//默认小时，原先只支持小时
						if(ticket_unit==1){//分钟
							map.put("money_minute",map.get("money"));
						}else if(ticket_unit==2){
							map.put("money_hour",map.get("money"));
						}else if(ticket_unit==3){
							map.put("money_day",map.get("money"));
						}
						map.put("umoney","");
					}else if ((int)map.get("type") == 5){//金额
						map.put("money_minute","");
						map.put("money_hour","");
						map.put("money_day","");
					}else if ((int)map.get("type") == 4){//全免
						map.put("money_minute","");
						map.put("money_hour","");
						map.put("money_day","");
						map.put("umoney","");
					}
				}
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("query")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List ret = query(request,comid);
			for(Map<String, Object> map : (List<Map<String,Object>>)ret.get(0)){
				Map shopMap = daService.getMap("select * from shop_tb where id=? ", new Object[]{map.get("shop_id")});
				if((int)map.get("type") == 3){//时长
					Integer ticket_unit = shopMap.get("ticket_unit")==null ? 2 : (int) shopMap.get("ticket_unit");
					//默认小时，原先只支持小时
					if(ticket_unit==1){//分钟
						map.put("money_minute",map.get("money"));
					}else if(ticket_unit==2){
						map.put("money_hour",map.get("money"));
					}else if(ticket_unit==3){
						map.put("money_day",map.get("money"));
					}
					map.put("umoney","");
				}else if ((int)map.get("type") == 5){//金额
					map.put("money_minute","");
					map.put("money_hour","");
					map.put("money_day","");
				}else if ((int)map.get("type") == 4){//全免
					map.put("money_minute","");
					map.put("money_hour","");
					map.put("money_day","");
					map.put("umoney","");
				}
			}
			List list = (List) ret.get(0);
			Long count =  (Long) ret.get(1);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}
		return null;
	}
	private List query(HttpServletRequest request,Long comid){
		String orderfield = RequestUtil.processParams(request, "orderfield");
		String orderby = RequestUtil.processParams(request, "orderby");
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
		String car_number = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number"));
		String shop_name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "shop_name"));
		Integer ticket_unit = RequestUtil.getInteger(request, "ticket_unit_start", 1);
		Integer money = RequestUtil.getInteger(request, "money_start", -1);
		String money_operate_type = RequestUtil.processParams(request, "money");;
		List<Object> ret = new ArrayList<Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		List<Object> params = new ArrayList<Object>();
		String sql = "select t.*,s.name shop_name from ticket_tb t,shop_tb s where t.comid= ? and t.shop_id = s.id ";
		String countsql = "select count(*) from ticket_tb t,shop_tb s where t.comid= ? and t.shop_id = s.id ";
		SqlInfo base = new SqlInfo("1=1", new Object[]{comid});
		SqlInfo sqlInfo = RequestUtil.customSearch(request,"ticket_tb","t",new String[]{"car_number","ticket_unit","shop_name"});
		if(sqlInfo != null ){
			sqlInfo = SqlInfo.joinSqlInfo(base,sqlInfo, 2);
			sql += " and " + sqlInfo.getSql();
			countsql += " and " + sqlInfo.getSql();
			params.addAll(sqlInfo.getParams());
		}else{
			params.addAll(base.getParams());
		}
		if(!car_number.equals("")){
			car_number = "%" + car_number + "%";
			sql += " and t.car_number like ? ";
			countsql += " and t.car_number like ? ";
			params.add(car_number);
		}
		if(shop_name!=null && !"".equals(shop_name)){
			sql+=" and s.name like ?";
			countsql+=" and s.name like ?";
			params.add("%"+shop_name+"%");
		}
		if(ticket_unit >0 ){
			if(ticket_unit==4){
				sql+=" and (s.ticket_unit=? or s.ticket_unit is null) and t.type=5";
				countsql+=" and (s.ticket_unit=? or s.ticket_unit is null) and t.type=5";
			}else if(ticket_unit==2){
				sql+=" and (s.ticket_unit=? or s.ticket_unit is null) and t.type=3";
				countsql+=" and (s.ticket_unit=? or s.ticket_unit is null) and t.type=3";
			}else{
				sql+=" and s.ticket_unit=? and t.type=3";
				countsql+=" and s.ticket_unit=? and t.type=3";
			}
//			if(ticket_unit == 2 || ticket_unit == 4){
////				sql+=" and (s.ticket_unit=? or s.ticket_unit is null)";
////				countsql+=" and (s.ticket_unit=? or s.ticket_unit is null)";
//				sql+=" and (s.ticket_unit=? )";
//				countsql+=" and (s.ticket_unit=? )";
//			}else{
//				sql+=" and s.ticket_unit=?";
//				countsql+=" and s.ticket_unit=?";
//			}
			params.add(ticket_unit);
		}
		//1 大于等于  between  3 等于  2小于等于
		if("1".equals(money_operate_type) && money >-1){
			sql+=" and t.money >= ? and t.type=3";
			countsql+=" and t.money >= ? and t.type=3";
			params.add(money);
		}
		if("between".equals(money_operate_type) && money >-1){
			Integer moneyEnd = RequestUtil.getInteger(request, "money_end", 0);
			sql+=" and t.money >= ? and t.money <= ? and t.type=3";
			countsql+=" and t.money >= ? and t.money <= ? and t.type=3";
			params.add(money);
			params.add(moneyEnd);
		}
		if("3".equals(money_operate_type) && money >-1){
			sql+=" and t.money = ?";
			countsql+=" and t.money = ?";
			params.add(money);
		}
		if("2".equals(money_operate_type) && money >-1){
			sql+=" and t.money <= ? and t.type=3";
			countsql+=" and t.money <= ? and t.type=3";
			params.add(money);
		}
		if("null".equals(money_operate_type)){
			sql+=" and (t.money is null or t.money = 0) and t.type = 3";
			countsql+=" and (t.money is null or t.money = 0) and t.type = 3";
		}
		if("".equals(orderfield)){
			orderfield = " id";
		}
		if("".equals(orderby)){
			orderby = " desc nulls last";
		}else {
			orderby += " nulls last";
		}
		sql += " order by t." + orderfield + " " + orderby+", t.id desc ";
		Long count = daService.getCount(countsql, params);
		list = daService.getAll(sql, params, pageNum, pageSize);
		ret.add(list);
		ret.add(count);
		return ret;
	}
}
