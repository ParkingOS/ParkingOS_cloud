package com.zld.struts.group;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
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

public class EmployeeLogsAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Override
	public ActionForward execute(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response) throws Exception{
		String action = RequestUtil.processParams(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}

		if(cityid == null) cityid = -1L;
		if(groupid == null) groupid = -1L;

		if(action.equals("")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			request.setAttribute("btime", df2.format(TimeTools.getToDayBeginTime()*1000));
			request.setAttribute("etime",  df2.format(System.currentTimeMillis()));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			String sql = "select a.logon_state,a.logoff_state,a.state,a.start_time,a.end_time,a.device_code,b.berthsec_name,b.comid,a.uid,a.uid as nickname from  " +
					"parkuser_work_record_tb  as  a left  join   " +
					"com_berthsecs_tb as  b  on  a. berthsec_id=b.id   where 1=1  ";
			String countSql = "select count(*) from  parkuser_work_record_tb  as  a left  join   " +
					"com_berthsecs_tb as  b  on  a. berthsec_id=b.id   where 1=1  " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo sqlInfo = RequestUtil.customSearch(request, "parkuser_work_record_tb", "a", new String[]{"comid","nickname"});
			SqlInfo sqlInfo2 = getSqlInfo1(request);
			SqlInfo sqlInfo3 = getSqlInfo2(request);

			List<Map<String, Object>> list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();

			List<Object> collectors = null;
			if(cityid > 0){
				collectors = commonMethods.getcollctors(cityid);
			}else if(groupid > 0){
				collectors = commonMethods.getCollctors(groupid);
			}
			if(collectors != null && !collectors.isEmpty()){
				String preParams = "";
				for(Object o : collectors){
					if(preParams.equals("")){
						preParams = "?";
					}else{
						preParams += ",?";
					}
				}

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

				sql += " and a.uid in ("+preParams+") order by a.start_time desc";
				countSql += " and a.uid in ("+preParams+") ";
				params.addAll(collectors);
				count = pgOnlyReadService.getCount(countSql,params);
				if(count>0){
					list = pgOnlyReadService.getAll(sql, params, pageNum, pageSize);
				}
			}
			String json = JsonUtil.Map2Json(list, pageNum, count, fieldsstr, "id");

			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("export")){
			List<Object> collectors = null;
			if(cityid > 0){
				collectors = commonMethods.getcollctors(cityid);
			}else if(groupid > 0){
				collectors = commonMethods.getCollctors(groupid);
			}
			if(collectors != null && !collectors.isEmpty()){
				List<Map<String, Object>> list = getIncomeinfo(request, collectors);
				export(response, list);
			}

		}
		return null;
	}

	private void export(HttpServletResponse response, List<Map<String, Object>> list){
		try {
			if(list != null && !list.isEmpty()){
				String heards[] = new String[]{"收费员","账号","停车场","泊位段名称","签入日期","签出日期","设备编号"};
				List<List<String>> bodyList = new ArrayList<List<String>>();
				for(Map<String, Object> map : list){
					List<String> valueList = new ArrayList<String>();
					String nickname = "";
					if(map.get("nickname") != null){
						nickname = (String)map.get("nickname");
					}
					valueList.add(nickname);
					valueList.add(map.get("uid") + "");
					String company_name = "";
					if(map.get("company_name") != null){
						company_name = (String)map.get("company_name");
					}
					valueList.add(company_name);
					String berthsec_name = "";
					if(map.get("berthsec_name") != null){
						berthsec_name = (String)map.get("berthsec_name");
					}
					valueList.add(berthsec_name);
					String start_time = "";
					if(map.get("start_time") != null){
						start_time = TimeTools.getTime_yyyyMMdd_HHmmss(Long.parseLong(map.get("start_time")+"")*1000);
					}
					valueList.add(start_time);
					String end_time = "";
					if(map.get("end_time") != null){
						end_time = TimeTools.getTime_yyyyMMdd_HHmmss(Long.parseLong(map.get("end_time")+"")*1000);
					}
					valueList.add(end_time);
					valueList.add(map.get("device_code") + "");
					bodyList.add(valueList);
				}
				String fname = "收费员上班报表";
				java.io.OutputStream os = response.getOutputStream();
				response.reset();
				response.setHeader("Content-disposition", "attachment; filename="
						+ StringUtils.encodingFileName(fname) + ".xls");
				ExportExcelUtil importExcel = new ExportExcelUtil("收费员上班报表",
						heards, bodyList);

				importExcel.createExcelFile(os);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private SqlInfo getSqlInfo1(HttpServletRequest request){
		Long comid =RequestUtil.getLong(request, "comid_start", -1L);
		SqlInfo sqlInfo = null;
		if(comid>0){
			sqlInfo = new SqlInfo(" b.comid=?   ",new Object[]{comid});
		}
		return sqlInfo;
	}

	private SqlInfo getSqlInfo2(HttpServletRequest request){
		Long uid =RequestUtil.getLong(request, "nickname_start", -1L);
		SqlInfo sqlInfo = null;
		if(uid>0){
			sqlInfo = new SqlInfo(" a.uid=? ",new Object[]{uid});
		}
		return sqlInfo;
	}


	private List<Map<String, Object>> getIncomeinfo(HttpServletRequest request, List<Object> idList) {
		try {
			if(idList != null && !idList.isEmpty()){
				SqlInfo sqlInfo = RequestUtil.customSearch(request, "parkuser_work_record_tb");

				String preParams = "";
				for(Object o : idList){
					if(preParams.equals("")){
						preParams = "?";
					}else{
						preParams += ",?";
					}
				}
				List<Object> params = new ArrayList<Object>();
				params.addAll(idList);
				String sql = "select * from parkuser_work_record_tb where uid in ("+preParams+") ";
				if(sqlInfo != null){
					sql +=" and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				sql += " order by start_time desc ";
				List<Map<String, Object>> workList = pgOnlyReadService.getAllMap(sql, params);
				if(workList != null && !workList.isEmpty()){
					List<Object> berthsegIdList = new ArrayList<Object>();
					String preParams2 = "";
					for(Map<String, Object> map : workList){
						if(!berthsegIdList.contains(map.get("berthsec_id"))){
							berthsegIdList.add(map.get("berthsec_id"));
							if(preParams2.equals("")){
								preParams2 = "?";
							}else{
								preParams2 += ",?";
							}
						}
					}
					if(!berthsegIdList.isEmpty()){
						List<Map<String, Object>> berthSegList = pgOnlyReadService.getAllMap("select id,berthsec_name,comid from com_berthsecs_tb " +
								" where id in ("+preParams2+")", berthsegIdList);
						if(berthSegList != null && !berthSegList.isEmpty()){
							List<Object> parkIdList = new ArrayList<Object>();
							String preParams3 = "";
							for(Map<String, Object> map : berthSegList){
								if(!parkIdList.contains(map.get("comid"))){
									parkIdList.add(map.get("comid"));
									if(preParams3.equals("")){
										preParams3 = "?";
									}else{
										preParams3 += ",?";
									}
								}
							}
							if(!parkIdList.isEmpty()){
								List<Map<String, Object>> parkList = pgOnlyReadService.getAllMap("select id,company_name from com_info_tb " +
										" where id in ("+preParams3+")", parkIdList);
								if(parkList != null && !parkList.isEmpty()){
									for(Map<String, Object> map : berthSegList){
										Long comid = (Long)map.get("comid");
										for(Map<String, Object> map2 : parkList){
											Long id = (Long)map2.get("id");
											if(comid.intValue() == id.intValue()){
												map.put("company_name", map2.get("company_name"));
												break;
											}
										}
									}
								}

							}

							for(Map<String, Object> map : workList){
								Long berthsec_id = (Long)map.get("berthsec_id");
								for(Map<String, Object> map2 : berthSegList){
									Long id = (Long)map2.get("id");
									if(berthsec_id.intValue() == id.intValue()){
										map.put("berthsec_name", map2.get("berthsec_name"));
										map.put("company_name", map2.get("company_name"));
										break;
									}
								}
							}
						}

					}

					List<Map<String, Object>> collectList = pgOnlyReadService.getAllMap("select id,nickname from user_info_tb where " +
							" id in ("+preParams+")", idList);
					if(collectList != null && !collectList.isEmpty()){
						for(Map<String, Object> map : workList){
							Long uid = (Long)map.get("uid");
							for(Map<String, Object> map2 : collectList){
								Long id = (Long)map2.get("id");
								if(uid.intValue() == id.intValue()){
									map.put("nickname", map2.get("nickname"));
									break;
								}
							}
						}
					}
				}
				return workList;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


}
