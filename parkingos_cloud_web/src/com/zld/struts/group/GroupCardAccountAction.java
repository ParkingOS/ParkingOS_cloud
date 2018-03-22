package com.zld.struts.group;

import com.zld.AjaxUtil;
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

public class GroupCardAccountAction extends Action {
	@Autowired
	private PgOnlyReadService readService;
	
	Logger logger = Logger.getLogger(GroupCardAccountAction.class);
	
	@SuppressWarnings("rawtypes")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//????????id
		request.setAttribute("authid", request.getParameter("authid"));
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("query")){
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Map<String, Object>> list = null;
			List<Object> params = new ArrayList<Object>();
			params.add(groupid);
			params.add(0);
			
			String sql = "select * from card_account_tb where groupid=? and is_delete=? ";
			String countSql = "select count(id) from card_account_tb where groupid=? and is_delete=? ";
			
			SqlInfo sqlInfo = RequestUtil.customSearch(request, "card_account_tb", "", new String[]{"nfc_uuid"});
			SqlInfo sqlInfo3 = getSqlInfo2(request, groupid);
			if(sqlInfo != null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			if(sqlInfo3 != null){
				countSql+=" and "+ sqlInfo3.getSql();
				sql +=" and "+sqlInfo3.getSql();
				params.addAll(sqlInfo3.getParams());
			}
			sql += " order by create_time desc ";
			Long count = readService.getCount(countSql, params);
			if(count > 0){
				list = readService.getAll(sql, params, pageNum, pageSize);
				setList(list);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}
		
		return null;
	}
	
	private void setList(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> idList = new ArrayList<Object>();
			String preParam = "";
			for(Map<String, Object> map : list){
				idList.add(map.get("card_id"));
				if(preParam.equals("")){
					preParam = "?";
				}else{
					preParam += ",?";
				}
			}
			
			List<Map<String, Object>> resultList = readService.getAllMap("select id,nfc_uuid from " +
					" com_nfc_tb where id in ("+preParam+")", idList);
			if(resultList != null && !resultList.isEmpty()){
				for(Map<String, Object> map : resultList){
					Long id = (Long)map.get("id");
					for(Map<String, Object> map2 : list){
						Long cardid = (Long)map2.get("card_id");
						if(id.intValue() == cardid.intValue()){
							map2.put("nfc_uuid", map.get("nfc_uuid"));
						}
					}
				}
			}
		}
	}
	
	private SqlInfo getSqlInfo2(HttpServletRequest request, Long groupId){
		String nfc_uuid = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "nfc_uuid"));
		SqlInfo sqlInfo = null;
		if(!nfc_uuid.equals("")){
			sqlInfo = new SqlInfo(" card_id in (select id from com_nfc_tb where nfc_uuid " +
					" like ? and group_id=? and type=? and is_delete=? )", 
					new Object[]{"%"+nfc_uuid+"%", groupId, 2, 0});
		}
		return sqlInfo;
	}
}
