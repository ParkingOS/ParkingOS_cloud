package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.pojo.QueryCount;
import com.zld.pojo.QueryList;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
/**
 * 停车场后台管理员登录后，管理员工，员工分为收费员和财务
 * @author Administrator
 *
 */
public class ParkEscapeAction extends Action{

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pService;
	private Logger logger = Logger.getLogger(ParkEscapeAction.class);
	@Autowired  CommonMethods commonMethods;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		Long  cityid=(Long)request.getSession().getAttribute("cityid");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		request.setAttribute("authid", request.getParameter("authid"));
		Long loginuin = (Long)request.getSession().getAttribute("loginuin");
		logger.error(action);
		//Integer isAdmin =(Integer)request.getSession().getAttribute("isadmin");
		if(loginuin == null||loginuin==-1){
			response.sendRedirect("login.do");
			return null;
		}
		if(comid==0)
			comid = RequestUtil.getLong(request, "comid", 0L);
		if(cityid == null) cityid = -1L;
		if(groupid == null) groupid = -1L;
		ExecutorService pool = ExecutorsUtil.getExecutorService();//获取线程池
		if(action.equals("")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Long today = TimeTools.getToDayBeginTime();
			request.setAttribute("btime", df2.format(today*1000));
			request.setAttribute("etime",  df2.format(System.currentTimeMillis()));
			request.setAttribute("comid", comid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select *,(total-prepay) overdue from no_payment_tb where is_delete=? and " +
					" end_time between ? and ? ";
			String countSql = "select count(id) from no_payment_tb where is_delete=? and end_time " +
					" between ? and ? ";
			String sumSql = "select sum(total) total,sum(prepay) prepay,sum(act_total) act_total,state from " +
					" no_payment_tb where is_delete=? and end_time between ? and ? ";

			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime);
			Long e = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime);
			if(groupid == -1){
				groupid= RequestUtil.getLong(request, "groupid_start", -1L);
			}
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"no_payment_tb", "", new String[]{"cid","nickname"});
			SqlInfo sqlInfo2 = getSuperSqlInfo(request);
			SqlInfo sqlinfo3 = getSuperSqlInfo1(request);
			List<Map<String, Object>> list = null;
			Long count = 0L;
			List<Map<String, Object>> sumList = null;

			ArrayList<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(b);
			params.add(e);
			List<Object> parks = new ArrayList<Object>();
			if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}else if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(comid > 0){
				parks.add(comid);
			}
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " and comid in ("+preParams+") ";
				countSql += " and comid in ("+preParams+") ";
				sumSql +=" and comid in ("+preParams+") ";
				params.addAll(parks);
				if(sqlInfo != null){
					countSql+=" and "+ sqlInfo.getSql();
					sql +=" and "+sqlInfo.getSql();
					sumSql += " and " + sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				if(sqlInfo2 != null){
					countSql+=" and "+ sqlInfo2.getSql();
					sql +=" and "+sqlInfo2.getSql();
					sumSql += " and " + sqlInfo2.getSql();
					params.addAll(sqlInfo2.getParams());
				}if(sqlinfo3 != null){
					countSql+=" and "+ sqlinfo3.getSql();
					sql +=" and "+sqlinfo3.getSql();
					sumSql += " and " + sqlinfo3.getSql();
					params.addAll(sqlinfo3.getParams());
				}
				sql += " order by id desc ";
				sumSql += " group by state ";
				QueryCount queryCount = new QueryCount(pService, countSql, params);
				QueryList queryList = new QueryList(pService, sql, params, pageNum, pageSize);
				QueryList querySum = new QueryList(pService, sumSql, params, -1, -1);
				Future<Long> future0 = pool.submit(queryCount);
				Future<List> future1 = pool.submit(queryList);
				Future<List> future2 = pool.submit(querySum);
				count = future0.get();
				list = future1.get();
				sumList = future2.get();

				setBerthNum(list);
				setParkUser(list);
			}
			Double atotal = 0d;//欠费总金额
			Double ptotal = 0d;//已追缴金额
			Double etotal = 0d;//未追缴金额
			if(sumList != null && !sumList.isEmpty()){
				for(Map<String, Object> map : sumList){
					Integer state = (Integer)map.get("state");
					Double total = 0d;
					Double prepay = 0d;
					Double act_total = 0d;
					if(map.get("total") != null){
						total = Double.valueOf(map.get("total") + "");
					}
					if(map.get("prepay") != null){
						prepay = Double.valueOf(map.get("prepay") + "");
					}
					if(map.get("act_total") != null){
						act_total = Double.valueOf(map.get("act_total") + "");
					}
					atotal += (total - prepay);//欠费总金额
					if(state == 0){//未追缴金额
						etotal = total - prepay;
					}else if(state == 1){//已追缴金额
						ptotal = act_total - prepay;
					}
				}
			}
			String res = "总欠费："+atotal+"元，已追缴："+ptotal+"元，未追缴："+etotal+"元";
			String json = JsonUtil.anlysisMap3Json(list, pageNum, count, fieldsstr,"id",res);
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("recover")){
			int r = Recover(request, groupid);
			AjaxUtil.ajaxOutput(response, "" + r);
		}else if(action.equals("exportExcel")){
			List<Map<String, Object>> list = null;
			list = queryData(request,groupid,cityid,comid);
			exportExcel(response, list);
		}else if(action.equals("exportExcel")){
			String sql = "select *,(total-prepay) overdue from no_payment_tb where is_delete=? " +
					" and groupid=? and state=? and end_time<?";
			List<Map<String, Object>> list = null;
			ArrayList<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(28);
			params.add(0);
			params.add(1451577600);
			List<Object> parks = new ArrayList<Object>();
			if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}else if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(comid > 0){
				parks.add(comid);
			}
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " and comid in ("+preParams+") ";
				params.addAll(parks);
				sql += " order by id desc ";
				list = pService.getAllMap(sql, params);
				setName(list);
				export(response, list);
			}
		}
		return null;
	}

	private void export(HttpServletResponse response, List<Map<String, Object>> list){
		try {
			if(list != null && !list.isEmpty()){
				String heards[] = new String[]{"所属车场","所属泊位段","泊位编号","订单生成时间","订单结算时间","车牌号","车主编号","订单编号",
						"订单金额","预付金额","欠费金额"};
				List<List<String>> bodyList = new ArrayList<List<String>>();
				for(Map<String, Object> map : list){
					List<String> valueList = new ArrayList<String>();
					valueList.add(map.get("company_name") + "");
					valueList.add(map.get("berthsec_name") + "");

					valueList.add(map.get("cid") + "");
					valueList.add(map.get("ctime") + "");
					valueList.add(map.get("etime") + "");

					valueList.add(map.get("car_number") + "");
					valueList.add(map.get("uin") + "");
					valueList.add(map.get("order_id") + "");

					valueList.add(map.get("total") + "");
					valueList.add(map.get("prepay") + "");
					valueList.add(map.get("overdue") + "");
					bodyList.add(valueList);
				}
				String fname = "晋中逃单记录";
				java.io.OutputStream os = response.getOutputStream();
				response.reset();
				response.setHeader("Content-disposition", "attachment; filename="
						+ StringUtils.encodingFileName(fname) + ".xls");
				ExportExcelUtil importExcel = new ExportExcelUtil("晋中逃单记录",
						heards, bodyList);
				importExcel.mulitHeadList = null;
				Map<String, String> headInfoMap=new HashMap<String, String>();
				headInfoMap.put("length", heards.length - 1 + "");
				headInfoMap.put("content", fname);
				importExcel.headInfo = headInfoMap;
				importExcel.createExcelFile(os);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void exportExcel(HttpServletResponse response, List<Map<String, Object>> list){
		try {
			if(list != null && !list.isEmpty()){
				String heards[] = new String[]{"所属车场","所属泊位段","泊位编号","进场时间"
						,"出场时间"
						,"车牌号"
						//,"车主编号","订单编号",
						//"订单金额","预付金额"
						,"欠费金额"};
				List<List<String>> bodyList = new ArrayList<List<String>>();
				for(Map<String, Object> map : list){
					List<String> valueList = new ArrayList<String>();
					valueList.add(map.get("company_name") + "");
					valueList.add(map.get("berthsec_name") + "");

					valueList.add(map.get("cid") + "");
					valueList.add(map.get("ctime") + "");
					valueList.add(map.get("etime") + "");

					valueList.add(map.get("car_number") + "");
					//valueList.add(map.get("uin") + "");
					//valueList.add(map.get("order_id") + "");

					//valueList.add(map.get("total") + "");
					//valueList.add(map.get("prepay") + "");
					valueList.add(map.get("overdue") + "");
					bodyList.add(valueList);
				}
				String fname = "逃单记录";
				java.io.OutputStream os = response.getOutputStream();
				response.reset();
				response.setHeader("Content-disposition", "attachment; filename="
						+ StringUtils.encodingFileName(fname) + ".xls");
				ExportExcelUtil importExcel = new ExportExcelUtil("逃单记录",
						heards, bodyList);
				importExcel.mulitHeadList = null;
				Map<String, String> headInfoMap=new HashMap<String, String>();
				headInfoMap.put("length", heards.length - 1 + "");
				headInfoMap.put("content", fname);
				importExcel.headInfo = headInfoMap;
				importExcel.createExcelFile(os);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void setName(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> parkIdList = new ArrayList<Object>();
			List<Object> berthSegIdList = new ArrayList<Object>();
			List<Object> berthIdList = new ArrayList<Object>();
			String park = "";
			String berthSeg = "";
			String berth = "";
			for(Map<String, Object> map : list){
				Long create_time = (Long)map.get("create_time");
				Long end_time = (Long)map.get("end_time");
				map.put("ctime", TimeTools.getTime_yyyyMMdd_HHmmss(create_time * 1000));
				map.put("etime", TimeTools.getTime_yyyyMMdd_HHmmss(end_time * 1000));

				Long comid = (Long)map.get("comid");
				Long berthseg_id = (Long)map.get("berthseg_id");
				Long berth_id = (Long)map.get("berth_id");
				if(!parkIdList.contains(comid)){
					parkIdList.add(comid);
					if(park.equals("")){
						park = "?";
					}else{
						park += ",?";
					}
				}
				if(!berthSegIdList.contains(berthseg_id)){
					berthSegIdList.add(berthseg_id);
					if(berthSeg.equals("")){
						berthSeg = "?";
					}else{
						berthSeg += ",?";
					}
				}
				if(!berthIdList.contains(berth_id)){
					berthIdList.add(berth_id);
					if(berth.equals("")){
						berth = "?";
					}else{
						berth += ",?";
					}
				}
			}

			List<Map<String, Object>> parkList = pService.getAllMap("select id,company_name from " +
					" com_info_tb where id in ("+park+")", parkIdList);
			List<Map<String, Object>> berthSegList = pService.getAllMap("select id,berthsec_name from " +
					" com_berthsecs_tb where id in ("+berthSeg+")", berthSegIdList);
			List<Map<String, Object>> berthList = pService.getAllMap("select id,cid from " +
					" com_park_tb where id in ("+berth+")", berthIdList);

			for(Map<String, Object> map : list){
				Long comid = (Long)map.get("comid");
				Long berthseg_id = (Long)map.get("berthseg_id");
				Long berth_id = (Long)map.get("berth_id");
				for(Map<String, Object> map2 : parkList){
					Long id = (Long)map2.get("id");
					if(id.intValue() == comid.intValue()){
						map.put("company_name", map2.get("company_name"));
						break;
					}
				}
				for(Map<String, Object> map2 : berthSegList){
					Long id = (Long)map2.get("id");
					if(id.intValue() == berthseg_id.intValue()){
						map.put("berthsec_name", map2.get("berthsec_name"));
						break;
					}
				}
				for(Map<String, Object> map2 : berthList){
					Long id = (Long)map2.get("id");
					if(id.intValue() == berth_id.intValue()){
						map.put("cid", map2.get("cid"));
						break;
					}
				}
			}
		}
	}

	private void setParkUser(List<Map<String, Object>> list){
		try {
			if(list != null && !list.isEmpty()){
				String preParam = "";
				ArrayList<Object> idList = new ArrayList<Object>();
				for(Map<String, Object> map : list){
					Long pursue_uid = (Long)map.get("pursue_uid");
					if(pursue_uid > 0){
						idList.add(map.get("pursue_uid"));
						if(preParam.equals("")){
							preParam = "?";
						}else{
							preParam += ",?";
						}
					}
				}
				if(!idList.isEmpty()){
					List<Map<String, Object>> berthList = pService.getAllMap("select id,nickname from user_info_tb " +
							" where id in ("+preParam+")", idList);
					if(berthList != null && !berthList.isEmpty()){
						for(Map<String, Object> map : berthList){
							Long userId = (Long)map.get("id");
							for(Map<String, Object> map2 : list){
								Long id = (Long)map2.get("pursue_uid");
								if(userId.intValue() == id.intValue()){
									map2.put("nickname", map.get("nickname"));
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

	private void setBerthNum(List<Map<String, Object>> list){
		try {
			if(list != null && !list.isEmpty()){
				String preParam = "";
				ArrayList<Object> idList = new ArrayList<Object>();
				for(Map<String, Object> map : list){
					Long berth_id = (Long)map.get("berth_id");
					if(berth_id > 0){
						idList.add(map.get("berth_id"));
						if(preParam.equals("")){
							preParam = "?";
						}else{
							preParam += ",?";
						}
					}
				}
				if(!idList.isEmpty()){
					List<Map<String, Object>> berthList = pService.getAllMap("select id,cid from com_park_tb " +
							" where id in ("+preParam+")", idList);
					if(berthList != null && !berthList.isEmpty()){
						for(Map<String, Object> map : berthList){
							Long berthId = (Long)map.get("id");
							for(Map<String, Object> map2 : list){
								Long id = (Long)map2.get("berth_id");
								if(berthId.intValue() == id.intValue()){
									map2.put("cid", map.get("cid"));
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

	private int Recover(HttpServletRequest request, Long groupId){
		long  loginuin =Long.parseLong(request.getSession().getAttribute("loginuin")+"");
		String ids =  AjaxUtil.decodeUTF8(RequestUtil.getString(request, "ids"));
		Long curTime = System.currentTimeMillis()/1000;
		String cids []= ids.split(",");
		int ret=0;
		logger.error("Recover  后台0元结算，编号："+ids+",操作人："+loginuin+",groupid:"+groupId);
		for(String id : cids){
			if(Check.isLong(id))
				ret=daService.update("update no_payment_tb set pursue_uid=?,pursue_time=?,state=?," +
								"pursue_groupid=?,act_total=? where id=? ",
						new Object[]{loginuin, curTime, 1, groupId, 0d, Long.valueOf(id)});
			logger.error("Recover 后台0元结算，编号："+id+",操作结果:"+ret);
		}
		return ret;

	}
	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		String cid = RequestUtil.processParams(request, "cid");
		SqlInfo sqlInfo1 = null;
		if(!cid.equals("")){
			sqlInfo1 = new SqlInfo(" berth_id in (select id from com_park_tb where cid like ?)",
					new Object[]{"%" + cid + "%"});
		}
		return sqlInfo1;
	}

	private SqlInfo getSuperSqlInfo1(HttpServletRequest request){
		String nickname = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "nickname"));
		SqlInfo sqlInfo1 = null;
		if(!nickname.equals("")){
			sqlInfo1 = new SqlInfo(" pursue_uid in(select id from user_info_tb where nickname like ? )",
					new Object[]{ "%"+nickname+"%" });
		}
		return sqlInfo1;
	}

	public List queryData(HttpServletRequest request,Long groupid ,Long cityid,Long comid) throws InterruptedException, ExecutionException{
		String sql = "select *,(total-prepay) overdue from no_payment_tb where is_delete=? and " +
				" state=?  ";
		if(groupid == -1){
			groupid= RequestUtil.getLong(request, "groupid_start", -1L);
		}
		SqlInfo sqlInfo = RequestUtil.customSearch(request,"no_payment_tb", "", new String[]{"cid","nickname"});
		SqlInfo sqlInfo2 = getSuperSqlInfo(request);
		SqlInfo sqlinfo3 = getSuperSqlInfo1(request);
		List<Map<String, Object>> list = null;

		ArrayList<Object> params = new ArrayList<Object>();
		params.add(0);
		params.add(0);

		List<Object> parks = new ArrayList<Object>();
		if(groupid > 0){
			sql += " and groupid =? ";
			params.add(groupid);
		}else if(cityid > 0){
			parks = commonMethods.getparks(cityid);
		}else if(comid > 0){
			parks.add(comid);
		}
		if(parks != null && !parks.isEmpty()){
			String preParams  ="";
			for(Object parkid : parks){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			sql += " and comid in ("+preParams+") ";
			params.addAll(parks);

			//setParkUser(list);
		}
		if(sqlInfo != null){
			sql +=" and "+sqlInfo.getSql();
			params.addAll(sqlInfo.getParams());
		}
		if(sqlInfo2 != null){
			sql +=" and "+sqlInfo2.getSql();
			params.addAll(sqlInfo2.getParams());
		}if(sqlinfo3 != null){
			sql +=" and "+sqlinfo3.getSql();
			params.addAll(sqlinfo3.getParams());
		}
		sql += " order by id desc ";
		logger.error(sql+":"+StringUtils.objArry2String(params.toArray()));
		list = pService.getAll(sql, params,1,60000);
		logger.error(list==null?"0":list.size());
		setName(list);
		return list;
	}
}