package com.zld.struts.anlysis;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.TimeTools;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopTicketAnlysisAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;

	private Logger logger = Logger.getLogger(ShopTicketAnlysisAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		if(comid==null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			request.setAttribute("btime", df2.format(System.currentTimeMillis()));
			request.setAttribute("etime",  df2.format(System.currentTimeMillis()));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			String nowtime= df2.format(System.currentTimeMillis());
			String sql = "select * from shop_tb where comid=? ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Integer type = RequestUtil.getInteger(request, "type", 3);
			if(btime.equals(""))
				btime = nowtime;
			if(etime.equals(""))
				etime = nowtime;
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
			Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
			List<Object> params = new ArrayList<Object>();
			params.add(comid);
			list = pgOnlyReadService.getAllMap(sql, params);
			setList(list,b,e,type);
			int count = list!=null?list.size():0;
			String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("detail")){
			request.setAttribute("shop_id", RequestUtil.processParams(request, "shop_id"));
			request.setAttribute("ttype", RequestUtil.processParams(request, "ttype"));
			request.setAttribute("flag", RequestUtil.processParams(request, "flag"));
			request.setAttribute("btime", RequestUtil.processParams(request, "btime"));
			request.setAttribute("etime", RequestUtil.processParams(request, "etime"));
			return mapping.findForward("detail");
		}else if(action.equals("quickquerydetail")){
			Long shop_id = RequestUtil.getLong(request, "shop_id", -1L);
			String type = RequestUtil.processParams(request, "type");
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			String nowtime= df2.format(System.currentTimeMillis());
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			if(btime.equals(""))
				btime = nowtime;
			if(etime.equals(""))
				etime = nowtime;
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
			Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			List<Object> params = new ArrayList<Object>();
			String sql = "select * from ticket_tb where shop_id=? and create_time between ? and ? and type=? order by create_time desc ";
			String sqlcount = "select count(*) from ticket_tb where shop_id=? and create_time between ? and ? and type=? ";
			Integer ttype = 3;
			if(type.equals("e")){//减免券
				ttype = 4;
			}
			params.add(shop_id);
			params.add(b);
			params.add(e);
			params.add(ttype);
			Long count = pgOnlyReadService.getCount(sqlcount, params);
			if(count > 0){
				list = pgOnlyReadService.getAll(sql, params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}
		return null;
	}

	private void setList(List<Map<String, Object>> list,Long b, Long e, Integer type){
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				Long id = (Long)map.get("id");
				List<Map<String, Object>> list2 = pgOnlyReadService
						.getAll("select sum(money) ptotal,count(*) pcount,sum(bmoney) dtotal,sum(umoney) dmoney,state from ticket_tb where shop_id=? and create_time between ? and ? and type=? group by state order by state ",
								new Object[] { id, b ,e ,type });
				Long allptotal = 0L;
				Long uuptotal = 0l;//未使用的减免券总额
				Long allpcount = 0L;
				Long uupcount = 0L;//未使用的减免券张数
				Long uptotal = 0L;//已使用的减免券的总额
				Long upcount = 0L;//已使用的减免券的张数
				Double dmoney = 0d;//实际减免券抵扣的金额
				Double dtotal = 0d;//实际使用的减免券的额度（小时）
				for(Map<String, Object> map2 : list2){
					Integer state = (Integer)map2.get("state");
					if(state == 0){
						uupcount = (Long)map2.get("pcount");
						allpcount += uupcount;
						if(type == 3){
							uuptotal = (Long)map2.get("ptotal");
							allptotal += uuptotal;
						}
					}else if(state == 1){
						dmoney = Double.valueOf(map2.get("dmoney") + "");
						dtotal = Double.valueOf(map2.get("dtotal") + "");
						upcount = (Long)map2.get("pcount");
						allpcount += upcount;
						if(type == 3){
							uptotal = (Long)map2.get("ptotal");
							allptotal += uptotal;
						}
					}
					map.put("allpcount", allpcount);
					map.put("uupcount", uupcount);
					map.put("upcount", upcount);
					map.put("dmoney", dmoney);
					map.put("dtotal", dtotal);
					if(type == 3){
						map.put("allptotal", allptotal);
						map.put("uuptotal", uuptotal);
						map.put("uptotal", uptotal);

						map.put("uplimit", map.get("ticket_limit"));
					}else if(type == 4){
						map.put("uplimit", map.get("ticketfree_limit"));
					}
				}
			}
		}
	}
}
