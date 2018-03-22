package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 停车场后台管理员登录后，管理收费价格
 * @author Administrator
 *
 */
public class PriceManageAction extends Action{

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;

	private Logger logger = Logger.getLogger(PriceManageAction.class);

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		String operater = request.getSession().getAttribute("loginuin")+"";
		Integer authId = RequestUtil.getInteger(request, "authid",-1);
		Integer isAdmin =(Integer)request.getSession().getAttribute("isadmin");
		request.setAttribute("authid", authId);
		if(operater==null){
			response.sendRedirect("login.do");
			return null;
		}
		if(comid==0)
			comid = RequestUtil.getLong(request, "comid", 0L);
		if(action.equals("")){
			request.setAttribute("comid", comid);
			Map map = daService.getMap("select * from price_assist_tb where comid = ?", new Object[]{comid});
			if(map!=null&&map.size()>0){
				request.setAttribute("assist_id", map.get("id"));
				request.setAttribute("assist_unit", map.get("assist_unit"));
				request.setAttribute("assist_price", map.get("assist_price"));
			}else{
				request.setAttribute("assist_id", -1);
				request.setAttribute("assist_unit", 0);
				request.setAttribute("assist_price",0);
			}
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String sql = "select * from price_tb where comid=? ";
			String countSql = "select count(*) from price_tb  where comid=? ";
			Long count = daService.getLong(countSql,new Object[]{comid});
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;//daService.getPage(sql, null, 1, 20);
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(comid);
			//添加删除标志
			params.add(0);
			if(count>0){
				try{
					list = daService.getAll(sql+ " and is_delete=? order by id desc ",params, pageNum, pageSize);
				}catch(Exception e){
					logger.error(">>>>>>>>>>>>>>>>>>>>>>>>>>查询价格列表异常"+"sql:"+sql+ " and is_delete=? order by id desc "+"params:"+params);
				}

			}
			String json = JsonUtil.Map2JsonSingleIgnore(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("query")){
			String sql = "select * from price_tb where comid=?  ";
			String countSql = "select count(*) from price_tb where  comid=?  " ;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo base = new SqlInfo("1=1", new Object[]{comid});
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"price_tb");
			Object[] values = null;
			List<Object> params = null;
			if(sqlInfo!=null){
				sqlInfo = SqlInfo.joinSqlInfo(base,sqlInfo, 2);
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				values = sqlInfo.getValues();
				params = sqlInfo.getParams();
			}else {
				values = base.getValues();
				params= base.getParams();
			}
			//System.out.println(sqlInfo);
			Long count= daService.getLong(countSql, values);
			List list = null;//daService.getPage(sql, null, 1, 20);
			//添加删除标志
			params.add(0);
			if(count>0){
				try{
					list = daService.getAll(sql + " and is_delete=? order by id desc", params, pageNum, pageSize);
				}catch(Exception e){
					logger.error(">>>>>>>>>>>>>>>>>>>>>>>>>>查询价格列表异常"+"sql:"+sql+ " and is_delete=? order by id desc "+"params:"+params);
				}
			}
			String json = JsonUtil.Map2JsonSingleIgnore(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			String result = createPrice(request);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("createassist")){
			Integer assistunit =RequestUtil.getInteger(request, "assist_unit", 0);
			Long assistid =RequestUtil.getLong(request, "assist_id", -1L);
			Double assistprice =RequestUtil.getDouble(request, "assist_price", 0d);
			int result = 0;
			String ret = null;
			if(assistunit!=0&&assistprice!=0){
				Long count = daService.getLong("select count(*) from price_assist_tb where comid=?", new Object[]{comid});
				if(count==0){
					Long nextid = daService.getLong(
							"SELECT nextval('seq_price_assist_tb'::REGCLASS) AS newid", null);
					result = daService.update("insert into  price_assist_tb(id,comid,type,assist_unit,assist_price) values (?,?,?,?,?)", new Object[]{nextid,comid,0,assistunit,assistprice});
					if(result==1){
						ret = "成功添加辅助价格1条,"+assistunit+","+assistprice;
						if(publicMethods.isEtcPark(comid)){
							int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"price_assist_tb",nextid,System.currentTimeMillis()/1000,0});
							logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" assist price ,add sync ret:"+re);
						}else{
							logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" price");
						}
						mongoDbUtils.saveLogs(request, 0, 2, "添加了车场("+comid+")辅助价格:"+nextid);
					}
				}else{
					result = daService.update("update price_assist_tb set assist_unit=?, assist_price=? where comid = ?", new Object[]{assistunit,assistprice,comid});
					if(result==1){
						ret = "成功修改辅助价格,"+assistunit+","+assistprice;
						if(publicMethods.isEtcPark(comid)){
							if(assistid==-1){
								assistid = daService.getLong("select id from price_assist_tb where comid = ?", new Object[]{comid});
							}
							int re = 0;
							if(assistid!=-1)
								re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"price_assist_tb",assistid,System.currentTimeMillis()/1000,1});
							logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" assist price ,add sync ret:"+re);
						}else{
							logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" assist price");
						}
						mongoDbUtils.saveLogs(request, 0,3, "修改了车场("+comid+")辅助价格:"+assistid);
					}
				}
			}else if(assistunit==0&&assistprice==0){
				Map tempMap = daService.getMap("select * from price_assist_tb where comid=?", new Object[]{comid});
				result = daService.update("delete from price_assist_tb where comid=?", new Object[]{comid});
				if(result==1){
					if(publicMethods.isEtcPark(comid)){
						int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"price_assist_tb",assistid,System.currentTimeMillis()/1000,2});
						logger.error("parkadmin or admin:"+operater+" delete comid:"+comid+" assist price ,add sync ret:"+re);
					}else{
						logger.error("parkadmin or admin:"+operater+" delete comid:"+comid+" assist price");
					}
					ret = "成功删除辅助价格1条！,0,0";
					mongoDbUtils.saveLogs(request, 0,4, "删除了车场("+comid+")辅助价格:"+tempMap);
				}else{
					ret = "没有设置辅助价格！,0,0";
				}
			}else{
				ret = "设置错误（不能设置0）！,0,0";
			}
			AjaxUtil.ajaxOutput(response, ret);
		}else if(action.equals("edit")){
			//车辆类型
			Long updateTime = System.currentTimeMillis()/1000;
			String carType = URLDecoder.decode(RequestUtil.getString(request, "car_type_zh"),"UTF-8");
			String describe = URLDecoder.decode(request.getParameter("describe"),"UTF-8");
			Long id =RequestUtil.getLong(request, "id", -1l);
			String sql = "update price_tb set car_type_zh=?,update_time=?,describe=? where id=?";
			Object [] values = new Object[]{carType,updateTime,describe,id};
			int result = daService.update(sql, values);
			if(result==1){
				if(comid==0)
					comid = daService.getLong("select comid from price_tb where id = ?", new Object[]{id});
				if(publicMethods.isEtcPark(comid)){
					int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"price_tb",id,System.currentTimeMillis()/1000,1});
					logger.error("parkadmin or admin:"+operater+" edit comid:"+comid+" price ,add sync ret:"+re);
				}else{
					logger.error("parkadmin or admin:"+operater+" edit comid:"+comid+" price ");
				}
				mongoDbUtils.saveLogs(request, 0, 3, "修改了车场("+comid+")价格:"+id);
			}
			logger.error(comid+"从缓存中清除价格....");
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("delete")){
			String id =RequestUtil.processParams(request, "selids");
			Map priceMap = daService.getMap("select * from price_tb where id =?", new Object[]{Long.valueOf(id)});
//			String sql = "delete from price_tb where id =?";
//			Object [] values = new Object[]{Long.valueOf(id)};
			//添加删除操作
			String sql = "update price_tb set is_delete=? where id =?";
			Object [] values = new Object[]{1,Long.valueOf(id)};
			int result = daService.update(sql, values);
			if(result==1){
				if(comid==0)
					comid = daService.getLong("select comid from price_tb where id = ?", new Object[]{Long.valueOf(id)});
				if(publicMethods.isEtcPark(comid)){
					int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"price_tb",Long.valueOf(id),System.currentTimeMillis()/1000,2});
					logger.error("parkadmin or admin:"+operater+" delete comid:"+comid+" price ,add sync ret:"+re);
				}else{
					logger.error("parkadmin or admin:"+operater+" delete comid:"+comid+" price");
				}
				mongoDbUtils.saveLogs(request, 0, 4, "删除了车场价格:"+priceMap);
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("getcartypes")){
			List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> retList = commonMethods.getCarType(comid);
			HashMap<String, Object> hashMap = new HashMap<String, Object>();
			hashMap.put("value_name","全部");
			hashMap.put("value_no", -1);
			resultList.add(hashMap);
			resultList.addAll(retList);
			String result = StringUtils.getJson(resultList);
			AjaxUtil.ajaxOutput(response, result);
		}
		return null;
	}

	/**
	 * 创建价格
	 * @param request
	 * @return
	 */
	private String createPrice(HttpServletRequest request){
		Long createTime = System.currentTimeMillis()/1000;
		String describe = "";
		String carType = "";
		try {
			describe = URLDecoder.decode(request.getParameter("describe"),"UTF-8");
			carType = URLDecoder.decode(request.getParameter("car_type_zh"),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Long comid = Long.valueOf(request.getParameter("comid"));
		Long nextid = daService.getLong(
				"SELECT nextval('seq_price_tb'::REGCLASS) AS newid", null);
		String sql = "insert into  price_tb (id,create_time,comid,car_type_zh,describe,price_id) values" +
				"(?,?,?,?,?,?)";
		Object [] values = new Object[]{nextid,createTime,comid,carType,describe,String.valueOf(nextid)};
		int result = daService.update(sql, values);
		if(result == 1){
			String operater = request.getSession().getAttribute("loginuin")+"";
			if(publicMethods.isEtcPark(comid)){
				int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"price_tb",nextid,System.currentTimeMillis()/1000,0});
				logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" price ,add sync ret:"+re);
			}else{
				logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" price");
			}
			mongoDbUtils.saveLogs(request, 0, 2, "添加了车场("+comid+")价格:"+nextid);
		}
		return result+"";
	}


}