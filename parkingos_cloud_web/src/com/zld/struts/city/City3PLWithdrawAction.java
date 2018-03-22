package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
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

public class City3PLWithdrawAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private DataBaseService daService;
	@SuppressWarnings("rawtypes")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			request.setAttribute("cityid", cityid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select w.*,a.name,g.name gname,c.company_name from withdrawer_tb w left join org_group_tb g on w.groupid=g.id " +
					"left join com_info_tb c on w.comid=c.id left join com_account_tb a on w.acc_id=a.id where 1=1 " ;
			String countSql = "select count(w.*) from withdrawer_tb w left join org_group_tb g on w.groupid=g.id " +
					"left join com_info_tb c on w.comid=c.id left join com_account_tb a on w.acc_id=a.id where 1=1 " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			List<Object> params = new ArrayList<Object>();
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"withdraw","w",new String[]{"name"});
			SqlInfo sqlInfo1 = getSuperSqlInfo(request);
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			if(sqlInfo1 != null){
				countSql+=" and "+ sqlInfo1.getSql();
				sql +=" and "+sqlInfo1.getSql();
				params.addAll(sqlInfo1.getParams());
			}
			Long count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by w.create_time desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("edit")){
			int r = edit(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("multiedit")){
			int r = editBath(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}

		return null;
	}

	private int editBath(HttpServletRequest request){
		String ids = RequestUtil.getString(request, "ids");
		Integer state = RequestUtil.getInteger(request, "state", 0);
		if(!ids.equals("")){
			String[] idArr = ids.split(",");
			List<Object> params = new ArrayList<Object>();
			params.add(state);
			params.add(System.currentTimeMillis()/1000);
			String preParams  ="";
			for(int i = 0; i< idArr.length; i++){
				params.add(Long.valueOf(idArr[i]));
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			int r = daService.update("update withdrawer_tb set state = ?,update_time = ?  where id in("+preParams+")", params);
			if(r > 0){
				return 1;
			}
		}
		return 0;
	}

	private int edit(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		Integer state = RequestUtil.getInteger(request, "state", 0);
		int r = daService.update("update withdrawer_tb set state =?,update_time=? where id =? ",
				new Object[]{state, System.currentTimeMillis()/1000, id});
		return r;
	}

	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		String name = RequestUtil.processParams(request, "name");
		SqlInfo sqlInfo1 = null;
		if(!name.equals("")){
			sqlInfo1 = new SqlInfo(" a.name=? ",new Object[]{"%"+name+"%"});
		}
		return sqlInfo1;
	}
}
