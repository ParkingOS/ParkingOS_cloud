package com.zld.struts.group;

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
import java.util.List;
import java.util.Map;


/**
 * 车牌车型对应设定
 * @author Administrator
 *
 */
public class GroupCarNumberTypeAction extends Action{

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	private Logger logger = Logger.getLogger(GroupCarNumberTypeAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		Integer authId = RequestUtil.getInteger(request, "authid", -1);
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		request.setAttribute("authid", authId);
		if(comid==-1){
			comid = (Long)request.getSession().getAttribute("comid");
		}
		if(comid==null||comid==-1){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			request.setAttribute("groupid", request.getParameter("groupid"));
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String sql = "select * from car_number_type_tb where comid in(select id from com_info_tb where groupid =? ) ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			//System.out.println(sqlInfo);
			List list = daService.getAll(sql + " order by id desc", new Object[]{groupid});
			int count =0;
			if(list!=null)
				count = list.size();
			String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("query")){
			String sql = "select * from car_number_type_tb where comid in(select id from com_info_tb where groupid =? ) ";
			String countSql = "select count(*) from car_number_type_tb where comid in(select id from com_info_tb where groupid =? ) " ;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo base = new SqlInfo("1=1", new Object[]{groupid});
			SqlInfo sqlInfo = RequestUtil.customSearch(request, "car_number_type_tb");
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
			if(count>0){
				list = daService.getAll(sql + " order by id desc", params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){//添加车型
			String car_number = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "car_number")).toUpperCase();
			if(car_number.length()<6||car_number.length()>8){
				AjaxUtil.ajaxOutput(response, "车牌号错误");
				return null;
			}
			Long typeid = RequestUtil.getLong(request, "typeid", -1L);
			if(typeid<0){
				AjaxUtil.ajaxOutput(response, "请选择正确车型（不能是”全部“）");
				return null;
			}
			if(comid<=0){
				comid = RequestUtil.getLong(request, "comid", -1L);
			}
			int result=0;
			if(comid>0){
				long currTime = System.currentTimeMillis()/1000;
				Map carNumbertType = daService.getMap("select * from car_number_type_tb where car_number = ? and comid = ? ", new Object[]{car_number,comid});
				if(carNumbertType!=null&&carNumbertType.get("id")!=null){
					AjaxUtil.ajaxOutput(response, "该车牌已绑定车型");
					return null;
//					long id =  Long.parseLong(carNumbertType.get("id") + "");
//					result = daService.update("update car_number_type_tb set typeid=?,update_time=? where id = ? ", new Object[]{typeid, currTime, id});
//					if(result==1&&publicMethods.isEtcPark(comid)){
//						daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_number_type_tb",id,currTime,1});
//					}
				}else {
					try {
						Long nextid = daService.getLong(
								"SELECT nextval('seq_car_number_type_tb'::REGCLASS) AS newid", null);
						result = daService.update("insert into car_number_type_tb (id,comid,car_number,typeid,update_time)" +
										" values(?,?,?,?,?)",
								new Object[]{nextid, comid, car_number, typeid, currTime});
						if (result == 1 && publicMethods.isEtcPark(comid)) {
							daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid, "car_number_type_tb", nextid, currTime, 0});
						}
						if (result == 1)
							mongoDbUtils.saveLogs(request, 0, 2, "添加了车牌(" + car_number + ")对应车型(" + typeid + ")(车场编号:" + comid + ")");
					} catch (Exception e) {
						if (e.getMessage().indexOf("car_type_tb_comid_mtype_key") != -1)
							result = -2;
						//e.printStackTrace();
					}
				}
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String car_number = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "car_number")).toUpperCase();
			if(car_number.length()<6||car_number.length()>8){
				AjaxUtil.ajaxOutput(response, "车牌号错误");
				return null;
			}
			Long typeid = RequestUtil.getLong(request, "typeid", -1L);
			if(typeid<0){
				AjaxUtil.ajaxOutput(response, "请选择车型");
				return null;
			}
			if(comid<=0){
				comid = RequestUtil.getLong(request, "comid",-1L);
			}
			Map carNumbertType = daService.getMap("select * from car_number_type_tb where car_number = ? and comid=? and id<>? ", new Object[]{car_number,comid,id});
			if(carNumbertType!=null&&carNumbertType.size()>0){
				AjaxUtil.ajaxOutput(response, "该车牌已绑定车型");
				return null;
			}
			long currTime = System.currentTimeMillis()/1000;
			int	result = daService.update("update car_number_type_tb set car_number =?,typeid=?,update_time=? where id=?",
					new Object[]{car_number, typeid, currTime,id});
			if(result==1&&publicMethods.isEtcPark(comid)){
				daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_number_type_tb",id,currTime,1});
			}
			if(result==1)
				mongoDbUtils.saveLogs(request, 0, 3, "(编号:"+id+")修改成:车牌号"+car_number+",typeid:"+typeid);
			AjaxUtil.ajaxOutput(response, ""+result);
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Map carMap = daService.getMap("select * from  car_number_type_tb  where id=?",	new Object[]{id});
			int	result = daService.update("delete from  car_number_type_tb  where id=?",
					new Object[]{id});
			if(result==1&&publicMethods.isEtcPark(comid)){
				daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_number_type_tb",id,System.currentTimeMillis()/1000,2});
			}
			if(result==1)
				mongoDbUtils.saveLogs(request, 0, 4, "删除了车牌对应车型(编号:"+id+")："+carMap);
			AjaxUtil.ajaxOutput(response, ""+result);
		}
		return null;
	}

}
