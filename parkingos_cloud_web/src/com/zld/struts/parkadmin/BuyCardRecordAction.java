package com.zld.struts.parkadmin;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
import com.zld.CustomDefind;
import com.zld.impl.MongoDbUtils;
import com.zld.service.DataBaseService;
import com.zld.utils.ExportExcelUtil;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
/**
 * 月卡续费记录
 * @author Liuqb
 *
 */
public class BuyCardRecordAction extends Action{
	
	@Autowired
	private DataBaseService daService;
	private Logger logger = Logger.getLogger(BuyCardRecordAction.class);

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		Integer role = RequestUtil.getInteger(request, "role",-1);
		Integer authId = RequestUtil.getInteger(request, "authid",-1);
		request.setAttribute("authid", authId);
		request.setAttribute("role", role);
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		logger.error("buycard>>>comid:"+comid+",groupid:"+groupid+",action:"+action);
		if(comid == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(comid==0)
			comid = RequestUtil.getLong(request, "comid", 0L);
		logger.error("buycard>>>comid:"+comid);
		if(groupid != null && groupid > 0){
			request.setAttribute("groupid", groupid);
			if(comid == null || comid <= 0){
				Map map = daService.getMap("select id,company_name from com_info_tb where groupid=? order by id limit ? ", 
						new Object[]{groupid, 1});
				logger.error("buycard>>>comid:"+comid+",map:"+map);
				if(map != null){
					comid = (Long)map.get("id");
				}else{
					comid = -999L;
				}
			}
		}
		if(action.equals("")){
			request.setAttribute("comid", comid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			logger.error("buycardrecord>>>comid:"+comid);
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = query(request,comid,pageNum,pageSize);
			long count =(Long)list.get(1);
			String json = JsonUtil.Map2Json((List)list.get(0),pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("exportExcel")){
			Map uin = (Map)request.getSession().getAttribute("userinfo");
			List dataList =query(request,comid,1,500);
			if(dataList.isEmpty()){
				return null;
			}
			List<Map<String, Object>> list = (List)dataList.get(0);
			List<List<String>> bodyList = new ArrayList<List<String>>();
			String [] heards = null;
			if(list!=null&&list.size()>0){
				//setComName(list);
				String [] f = new String[]{"id","collector","amount_pay","car_number","user_id","buy_month","create_time"};
				heards = new String[]{"编号","收费员","实收金额","车牌号","用户编号","购买月数","购买时间"};
				for(Map<String, Object> map : list){
					List<String> values = new ArrayList<String>();
					for(String field : f){
						if("collector".equals(field)){
							values.add(getUinName(Long.valueOf(map.get(field)+"")));
						}else{
							if("create_time".equals(field)){
								if(map.get(field)!=null){
									values.add(TimeTools.getTime_yyyyMMdd_HHmmss(Long.valueOf((map.get(field)+""))*1000));
								}else{
									values.add("null");
								}
							}else{
								values.add(map.get(field)+"");
							}
						}
					}
					bodyList.add(values);
				}
			}
			String fname = "月卡续费记录" + com.zld.utils.TimeTools.getDate_YY_MM_DD();
			fname = StringUtils.encodingFileName(fname);
			java.io.OutputStream os;
			try {
				response.reset();
				response.setHeader("Content-disposition", "attachment; filename="
						+ fname + ".xls");
				response.setContentType("application/x-download");
				os = response.getOutputStream();
				ExportExcelUtil importExcel = new ExportExcelUtil("月卡续费记录",
						heards, bodyList);
				importExcel.createExcelFile(os);
			} catch (IOException e) {
				e.printStackTrace();
			}
//			String json = "";
//			AjaxUtil.ajaxOutput(response, json);
			return null;
		}
		
		return null;
	}
	private String getUinName(Long uin) {
		Map list = daService.getPojo("select * from user_info_tb where id =?  ",new Object[]{uin});
		String uinName = "";
		if(list!=null&&list.get("nickname")!=null){
			uinName = list.get("nickname")+"";
		}
		return uinName;
	}
	private List query(HttpServletRequest request,long comid,Integer pageNum,Integer pageSize){
		ArrayList arrayList = new ArrayList();
		String orderfield = RequestUtil.processParams(request, "orderfield");
		String orderby = RequestUtil.processParams(request, "orderby");
		logger.error("buycard>>>comid:"+comid+",orderfield:"+orderfield+",orderby:"+orderby);
		if(orderfield.equals("")){
			orderfield = "id";
		}
		if(orderby.equals("")){
			orderby = "desc";
		}
		String sql = "select * from card_renew_tb where comid=?  ";
		String countSql = "select count(*) from card_renew_tb where  comid=? " ;
		SqlInfo base = new SqlInfo("1=1", new Object[]{String.valueOf(comid)});
		
		SqlInfo sqlInfo = RequestUtil.customSearch(request,"card_renew");
		List<Object> params =new ArrayList<Object>();
		
		if(sqlInfo!=null){
			sqlInfo = SqlInfo.joinSqlInfo(base,sqlInfo, 2);
			countSql+=" and "+ sqlInfo.getSql();
			sql +=" and "+sqlInfo.getSql();
			params = sqlInfo.getParams();
		}else {
			params= base.getParams();
		}
		sql += " order by " + orderfield + " " + orderby;
		//System.out.println(sqlInfo);
		Long count= daService.getCount(countSql, params);
		logger.error("buycard>>>comid:"+comid+",sql:"+sql+",count:"+count);
		List<Map<String, Object>> list = null;//daService.getPage(sql, null, 1, 20);
		if(count>0){
			list = daService.getAll(sql, params, pageNum, pageSize);
			arrayList.add(list);
			arrayList.add(count);
		}else {
			arrayList.add(new ArrayList());
			arrayList.add(0L);
		}
		return arrayList;
	}
	
}