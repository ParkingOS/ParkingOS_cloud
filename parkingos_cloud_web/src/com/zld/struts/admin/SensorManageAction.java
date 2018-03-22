package com.zld.struts.admin;

import com.google.gson.Gson;
import com.zld.AjaxUtil;
import com.zld.Constants;
import com.zld.impl.CommonMethods;
import com.zld.impl.MemcacheUtils;
import com.zld.pojo.SensorInfo;
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

public class SensorManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private MemcacheUtils memcacheUtils;

	Logger logger = Logger.getLogger(SensorManageAction.class);

	@SuppressWarnings({ "rawtypes", "unused" })
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select * from dici_tb where is_delete=? " ;
			String countSql = "select count(id) from dici_tb where is_delete=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Map<String, Object>> list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			Long group = RequestUtil.getLong(request, "group", -1L);
			Long city = RequestUtil.getLong(request, "city", -1L);
			Long park = RequestUtil.getLong(request, "park", -1L);
			String dici=RequestUtil.getString(request, "dici");
			List<Object> parks = new ArrayList<Object>();
			if(park > 0){
				parks.add(park);
			}else if(group > 0){
				parks = commonMethods.getParks(group);
			}else if(city > 0){
				parks = commonMethods.getparks(city);
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
				params.addAll(parks);
			}
			if(!dici.equals("")){
				sql += " and  did like ? ";
				countSql += " and  did like ? ";
				params.add("%"+dici+"%");
			}
			count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
				setxyz(list);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("bindsensor")){
			String ids = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "ids"));
			Long comid = RequestUtil.getLong(request, "park_start", -1L);
			int r = 0;
			String sql = "update dici_tb set comid=? where id in(";
			List<Object> params = new ArrayList<Object>();
			if(!ids.equals("")&&comid>0){
				params.add(comid);
				String sid[] = ids.split(",");
				for(String id : sid){
					sql +="?,";
					params.add(Long.valueOf(id));
				}
				if(sql.endsWith(","))
					sql = sql.substring(0,sql.length()-1);
				sql +=")";
			}
			if(params.size()>1){
				r = daService.update(sql, params);
			}
			logger.error("bind sensor ids:"+ids+",comid:"+comid+",更新了"+r+"条数据");
			if(r>0)
				r=1;
			AjaxUtil.ajaxOutput(response, r+"");

		}else if(action.equals("cancalbind")){
			String ids = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "ids"));
			if("".equals(ids)){
				AjaxUtil.ajaxOutput(response, "0");
				return null;
			}
			String[] idStrings = ids.split(",");
			List<Object> params = new ArrayList<Object>();
			String preParam = "";
			for(int i = 0; i < idStrings.length; i++){
				if("".equals(preParam)){
					preParam = "?";
				}else{
					preParam += ",?";
				}
				params.add(Long.valueOf(idStrings[i]));
			}
			List<Object> param1 = new ArrayList<Object>();
			param1.add(-1);
			param1.addAll(params);
			int r = daService.update("update dici_tb set comid=? where id in ("+preParam+") ", param1);
			logger.error("ids:"+ids+",r:"+r);
			if(r > 0){
				List<Object> param2 = new ArrayList<Object>();
				param2.add(-1);
				param2.addAll(params);
				param2.add(0);
				r = daService.update("update com_park_tb set dici_id=? where dici_id in ("+preParam+") and is_delete=? ", param2);
				logger.error("ids:"+ids+",r:"+r);
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "0");
			}
			return null;
		}else if(action.equals("intixyz")){
			int r = initxyz(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}
		return null;
	}

	private void setxyz(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			Gson gson = new Gson();
			for(Map<String, Object> map : list){
				String did = (String)map.get("did");
				if(did != null){
					did = Constants.SNESOR_SIGN + did;
					String json = memcacheUtils.get(did);
					if(json != null){
						SensorInfo sensorInfo = gson.fromJson(json, SensorInfo.class);
						map.put("x0", sensorInfo.getX0());
						map.put("y0", sensorInfo.getY0());
						map.put("z0", sensorInfo.getZ0());
						map.put("rate", sensorInfo.getRate());
					}
				}
			}
		}
	}

	private int initxyz(HttpServletRequest request){
		try {
			Gson gson = new Gson();
			Long id = RequestUtil.getLong(request, "id", -1L);
			Map<String, Object> map = pgOnlyReadService.getMap("select did,site_id from dici_tb " +
					" where id=? ", new Object[]{id});
			String did = (String)map.get("did");
			if(!did.startsWith("TB")){
				return -2;//非天泊公司的车检器，不能设置初始值
			}
			did = Constants.SNESOR_SIGN + did;
			String json = memcacheUtils.get(did);
			SensorInfo sensorInfo = gson.fromJson(json, SensorInfo.class);
			if(sensorInfo == null){
				sensorInfo = new SensorInfo();
				sensorInfo.setId(did);
			}
			sensorInfo.setX0(sensorInfo.getX());
			sensorInfo.setY0(sensorInfo.getY());
			sensorInfo.setZ0(sensorInfo.getZ());
			sensorInfo.setStatus(0);//置为无车
			boolean b = memcacheUtils.set(did, gson.toJson(sensorInfo));
			if(b){
				int r = daService.update("update dici_tb set state=? where id=? ",
						new Object[]{0, id});
				return r;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		Integer state = RequestUtil.getInteger(request, "state_start", -1);
		SqlInfo sqlInfo1 = null;
		if(state > -1){
			sqlInfo1 = new SqlInfo(" w.state=? ",new Object[]{state});
		}
		return sqlInfo1;
	}
}
