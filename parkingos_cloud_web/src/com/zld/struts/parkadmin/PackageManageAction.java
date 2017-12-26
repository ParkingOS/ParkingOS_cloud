package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * 停车场后台管理员登录后，查看订单，不能修改和删除
 * @author Administrator
 *
 */
public class PackageManageAction extends Action{

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	private Logger logger = Logger.getLogger(PackageManageAction.class);

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		Integer role = RequestUtil.getInteger(request, "role",-1);
		request.setAttribute("role", role);
		String nickname = request.getSession().getAttribute("loginuin")+"";
		Integer authId = RequestUtil.getInteger(request, "authid",-1);
		Integer isAdmin =(Integer)request.getSession().getAttribute("isadmin");
		request.setAttribute("authid", authId);
		if(nickname == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(comid==0)
			comid = RequestUtil.getLong(request, "comid", 0L);
		if(action.equals("")){
			request.setAttribute("comid", comid);
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String sql = "select * from product_package_tb where comid=? ";
			String countSql = "select count(*) from product_package_tb  where comid=? and is_delete=0 ";
			List comsList = daService.getAll("select * from com_info_tb where pid = ?",new Object[]{comid});
			Object[] parm = new Object[comsList.size()+1];
			parm[0] = comid;
			List<Object> params = new ArrayList<Object>();
			params.add(comid);
			for (int i = 1; i < parm.length; i++) {
				long comidoth = Long.parseLong(((Map)comsList.get(i-1)).get("id")+"");
				parm[i] = comidoth;
				params.add(comidoth);
				sql += " or comid = ? ";
				countSql += " or comid = ? ";
			}
			Long count = daService.getLong(countSql, parm);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;//daService.getAll(sql, null, 1, 20);
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			//添加删除标志
			params.add(0);
			if(count>0){
				list = daService.getAll(sql+ " and is_delete=? order by id desc", params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2JsonSingleIgnore(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("query")){
			String sql = "select * from product_package_tb where comid=?  ";
			String countSql = "select count(*) from product_package_tb where comid=? and is_delete=0 " ;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo base = new SqlInfo("1=1", new Object[]{comid});
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"product_package_tb");
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
			List list = null;//daService.getAll(sql, null, 1, 20);
			//添加删除标志、
			params.add(0);
			if(count>0){
				list = daService.getAll(sql+ " and is_delete=? order by id desc", params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2JsonSingleIgnore(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			//创建时间
			Long createTime = System.currentTimeMillis()/1000;
			//车场编号
			comid = RequestUtil.getLong(request, "comid", comid);
			String period = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "period"));
			Long carTypeId =RequestUtil.getLong(request, "car_type_id", -1L);
			Double price = RequestUtil.getDouble(request, "price", 0.0);
			//月卡名称
			String p_name =URLDecoder.decode(AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "p_name")),"UTF-8");
			//月卡描述
			String describe = URLDecoder.decode(RequestUtil.getString(request, "describe"),"UTF-8");
			Long nextid = daService.getLong(
					"SELECT nextval('seq_product_package_tb'::REGCLASS) AS newid", null);
			String sql = "insert into  product_package_tb (id,comid,create_time,p_name," +
					"describe,card_id,car_type_id,period,price) " +
					"values(?,?,?,?,?,?,?,?,?)";
			Object [] values = new Object[]{nextid,comid,createTime,p_name,describe,String.valueOf(nextid),carTypeId,period,price};
			int result = daService.update(sql, values);
			if(result==1){//添加成功后，更新车场支持包月功能
				if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"product_package_tb",nextid,System.currentTimeMillis()/1000,0});
					logger.error("parkadmin or admin:"+nickname+" add comid:"+comid+" package ,add sync ret:"+r);
				}else{
					logger.error("parkadmin or admin:"+nickname+" add comid:"+comid+" package");
				}
				mongoDbUtils.saveLogs(request, 0, 2, "添加了车场（" + comid + "）套餐：" + p_name);
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("edit")){
			//月卡套餐id
			String id =RequestUtil.processParams(request, "id");
			//车场编号
			comid = RequestUtil.getLong(request, "comid", comid);
			//修改时间
			Long updateTime = System.currentTimeMillis()/1000;
			//月卡套餐描述
			String describe = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "describe"));
			String p_name =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "p_name"));
			String period = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "period"));
			Long carTypeId =RequestUtil.getLong(request, "car_type_id", -1L);
			Double price = RequestUtil.getDouble(request, "price", 0.0);
			String sql = "update product_package_tb set p_name=?,comid=?,update_time=?,describe=?,period=?,car_type_id=?,price=? where id=?";
			Object [] values = new Object[]{p_name,comid,updateTime,describe,period,carTypeId,price,Long.valueOf(id)};
			int result = daService.update(sql, values);
			if(result==1){
				if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"product_package_tb",Long.valueOf(id),System.currentTimeMillis()/1000,1});
					logger.error("parkadmin or admin:"+nickname+" edit comid:"+comid+" package ,add sync ret:"+r);
				}else{
					logger.error("parkadmin or admin:"+nickname+" edit comid:"+comid+" package ");
				}
				mongoDbUtils.saveLogs(request, 0, 3, "修改了车场（"+comid+"）套餐："+p_name);
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("delete")){
			Long id =RequestUtil.getLong(request, "id", -1L);
			comid = RequestUtil.getLong(request, "comid", comid);
			Long count = daService.getLong("select count(id) from carower_product where pid=? and is_delete=?", new Object[]{id,0});
			if(count>0){
				AjaxUtil.ajaxOutput(response, "该套餐已被使用，共"+count+"条，请在会员管理中解除绑定后删除");
				return null;
			}
			Map parkMap = daService.getMap("select * from product_package_tb where id =?", new Object[]{Long.valueOf(id)});
//			String sql = "delete from product_package_tb where id =?";
//			Object [] values = new Object[]{Long.valueOf(id)};
			//添加删除逻辑
			String sql = "update product_package_tb set is_delete=? where id =?";
			Object [] values = new Object[]{1,Long.valueOf(id)};
			int result = daService.update(sql, values);
			if(result==1){
				if(comid==0){
					comid = daService.getLong("select comid from product_package_tb where id=? ", new Object[]{Long.valueOf(id)});
				}
				if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"product_package_tb",Long.valueOf(id),System.currentTimeMillis()/1000,2});
				}
				mongoDbUtils.saveLogs(request, 0, 4, nickname + "删除了车场（" + comid + "）套餐：" + parkMap);
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("week")){
			Long id =RequestUtil.getLong(request, "id", -1L);
			if(comid>0){
				Map map = daService.getMap("select exclude_date from product_package_tb where id =? ",new Object[]{id});
				if(map!=null&&map.get("exclude_date")!=null){
					request.setAttribute("week", map.get("exclude_date"));
				}
				request.setAttribute("id",id);
				request.setAttribute("comid",comid);
			}
			return mapping.findForward("week");
		}else if(action.equals("editweek")){
			Long id =RequestUtil.getLong(request, "id", -1L);
			String week =RequestUtil.getString(request, "week");
			int result = 0;
			if(id>0){
				result = daService.update("update product_package_tb set exclude_date= ? where id =? ",new Object[]{week,id});
				if(result==1){
					if(publicMethods.isEtcPark(comid)) {
						daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid, "product_package_tb", Long.valueOf(id), System.currentTimeMillis() / 1000, 1});
					}
					mongoDbUtils.saveLogs(request, 0, 3, "编辑了车场（"+comid+"）套餐：添加了收费包月套餐（"+id+"）的收费日期："+week);
				}
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}
		return null;
	}

}