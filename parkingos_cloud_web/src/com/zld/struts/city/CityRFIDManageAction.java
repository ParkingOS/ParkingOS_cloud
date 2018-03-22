package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
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

public class CityRFIDManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;

	@SuppressWarnings({ "rawtypes", "unused" })
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(cityid == null && groupid == null){
			return null;
		}
		if(cityid == null) cityid = -1L;
		if(groupid == null) groupid = -1L;

		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select c.*,u.balance,u.mobile from com_nfc_tb c,user_info_tb u where c.uin=u.id " ;
			String countSql = "select count(*) from com_nfc_tb c,user_info_tb u where c.uin=u.id " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"nfc_tb","c",new String[]{"balance"});
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			List<Object> parks = null;
			if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " and c.comid in ("+preParams+") ";
				countSql += " and c.comid in ("+preParams+") ";
				params.addAll(parks);

				if(sqlInfo!=null){
					countSql+=" and "+ sqlInfo.getSql();
					sql +=" and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				count = daService.getCount(countSql,params);
				if(count>0){
					list = daService.getAll(sql +" order by update_time desc ",params, pageNum, pageSize);
				}
			}
			setList(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}

		return null;
	}
	private void setList(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> uids = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> map : list){
				uids.add(map.get("uid"));
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}

			List<Map<String, Object>> rList = pgOnlyReadService.getAllMap("select id,nickname from user_info_tb where id in ("+preParams+")  ", uids);
			if(rList != null && !rList.isEmpty()){
				for(Map<String, Object> map : list){
					Long uid = (Long)map.get("uid");
					Long uin = (Long)map.get("uin");
					map.put("carnumber", commonMethods.getcar(uin));
					for(Map<String, Object> map2 : rList){
						Long id = (Long)map2.get("id");
						if(uid.intValue() == id.intValue()){
							map.put("nickname", map2.get("nickname"));
							break;
						}
					}
				}
			}
		}
	}
}
