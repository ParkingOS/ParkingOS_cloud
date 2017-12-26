package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.DataBaseService;
import com.zld.utils.*;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 月卡续费记录
 * @author Liuqb
 *
 */
public class BuyCardRecordAction extends Action{

	@Autowired
	private DataBaseService daService;
	private Logger logger = Logger.getLogger(BuyCardRecordAction.class);
	@Autowired
	private CommonMethods commonMethods;

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
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		logger.error("buycard>>>comid:"+comid+",groupid:"+groupid+",cityid:"+cityid+",action:"+action);
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
					comid = -1L;
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
			Long newGroupid = RequestUtil.getLong(request,"groupid",-1L);
			Long newComid = RequestUtil.getLong(request,"comid",-1L);
			if(newComid!=null){
				request.setAttribute("comid",newComid);
			}
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			if(newComid!=null&&newComid>0){
				list = query(request,comid,null,null,pageNum,pageSize);
			}else if(newGroupid != null && newGroupid > 0){
				list = query(request,comid,newGroupid,null,pageNum,pageSize);
			}else{
				list = query(request,comid,groupid,cityid,pageNum,pageSize);
			}
			long count =(Long)list.get(1);
			Map totalMap = (Map)list.get(2);

			String money = "应收 0元，实收 0元";
			logger.error(totalMap);
			if(totalMap!=null&&!totalMap.isEmpty()){
				money = "应收 "+totalMap.get("recelivagle")+"元，实收 "+totalMap.get("money")+"元";
			}

			//String json = JsonUtil.Map2Json((List)list.get(0),pageNum,count, fieldsstr,"id");
			String json = JsonUtil.anlysisMap2Json((List)list.get(0),pageNum,count, fieldsstr,"id",money);
			logger.error(json);
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("exportExcel")){
			Map uin = (Map)request.getSession().getAttribute("userinfo");
			List dataList =query(request,comid,groupid,cityid,1,500);
			if(dataList.isEmpty()){
				return null;
			}
			List<Map<String, Object>> list = (List)dataList.get(0);
			List<List<String>> bodyList = new ArrayList<List<String>>();
			String [] heards = null;
			if(list!=null&&list.size()>0){
				//setComName(list);
				String [] f = new String[]{"id","trade_no","card_id","pay_time","amount_receivable","amount_pay","collector","pay_type","car_number","user_id","limit_time","resume"};
				heards = new String[]{"编号","购买流水号","月卡编号","月卡续费时间","应收金额","实收金额","收费员","缴费类型","车牌号","用户编号","有效期","备注"};
				for(Map<String, Object> map : list){
					List<String> values = new ArrayList<String>();
					for(String field : f){
						if("collector".equals(field)){
							Object uid = map.get("collector");
							if(Check.isNumber(uid+""))
								if(getUinName(Long.valueOf(map.get(field)+""))!=null){
									values.add(getUinName(Long.valueOf(map.get(field)+"")));
								}else{
									values.add(uid+"");
								}
							else
								values.add(uid+"");
						}else{
							if("create_time".equals(field)||"pay_time".equals(field)||"limit_time".equals(field)){
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
	private List query(HttpServletRequest request,long comid,Long groupid,Long cityid,Integer pageNum,Integer pageSize){
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
		String sql = "select * from card_renew_tb where comid in   ";
		String countSql = "select count(*) from card_renew_tb where  comid in " ;
		String totalSql = "select sum(to_number(amount_receivable,'9999999.99')) recelivagle,sum(to_number(amount_pay,'9999999.99')) money " +
		" from card_renew_tb where comid in " ;
		List<Object> params = new ArrayList<Object>();
		Long _comid = RequestUtil.getLong(request,"comid",-1L);
		List<Object> coms = new ArrayList<>();
		if(_comid<=0){
			if(cityid!=null&&cityid>0){
				coms = commonMethods.getparks(cityid);
			}else if(groupid!=null&&groupid>0){
				coms = commonMethods.getParks(groupid);
			}
		}
		if(coms.isEmpty()){
			if(_comid>0)
				coms.add(_comid);
			else
				coms.add(comid);
		}
		List<String> comids = new ArrayList<>();
		for(Object c : coms){
			comids.add(c+"");
		}
		String preParams  ="";
		for(Object o : comids){
			if(preParams.equals(""))
				preParams ="?";
			else
				preParams += ",?";
		}
		sql += "("+preParams+") ";
		countSql += " ("+preParams+") ";
		totalSql += " ("+preParams+") ";
		params.addAll(comids);
//		SqlInfo base = new SqlInfo("1=1", new Object[]{String.valueOf(comid)});

		SqlInfo sqlInfo = RequestUtil.customSearch(request,"card_renew");

		if(sqlInfo!=null){
			countSql+=" and "+ sqlInfo.getSql();
			totalSql+=" and "+ sqlInfo.getSql();
			sql +=" and "+sqlInfo.getSql();
			params.addAll(sqlInfo.getParams());
		}

		Map totalMap = daService.getMap(totalSql ,params);
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
		arrayList.add(totalMap);
		return arrayList;
	}

}