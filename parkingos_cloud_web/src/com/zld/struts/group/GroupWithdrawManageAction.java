package com.zld.struts.group;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoDbUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupWithdrawManageAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private DataBaseService daService;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	@SuppressWarnings("rawtypes")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			Map<String, Object> groupMap = pgOnlyReadService.getMap("select balance from org_group_tb where id=? ",
					new Object[]{groupid});
			Double balance = 0d;
			if(groupMap != null){
				balance = Double.valueOf(groupMap.get("balance") + "");
			}
			request.setAttribute("balance", balance);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select * from withdrawer_tb where groupid=? " ;
			String countSql = "select count(*) from withdrawer_tb where groupid=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			List<Object> params = new ArrayList<Object>();
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"withdraw");
			params.add(groupid);
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			Long count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("withdraw")){
			int r = withdraw(request, groupid, uin);
			AjaxUtil.ajaxOutput(response, r + "");
		}

		return null;
	}

	private int withdraw(HttpServletRequest request, Long groupid, Long oprator_id){
		Double money = RequestUtil.getDouble(request, "money", 0d);
		Long ntime = System.currentTimeMillis()/1000;
		//检查帐户是否已绑定
		List<Map<String, Object>> accList = pgOnlyReadService.getAll("select id,type from com_account_tb where groupid =? and type in(?,?) and state =? order by id desc",
				new Object[]{groupid, 0, 2, 0});
		Long accId = null;
		Integer type =0;
		if(accList != null && !accList.isEmpty()){
			for(Map<String, Object> m: accList){
				type = (Integer)m.get("type");
				if(type != null && type == 2){
					accId =  (Long)m.get("id");
					break;
				}
			}
			if(accId == null)
				accId=(Long)accList.get(0).get("id");
		}
		if(accId !=null && accId > 0){
			boolean result =false;
			if(money > 0){
				Map<String, Object> groupMap = daService.getMap("select balance,name from org_group_tb where id=? ",
						new Object[]{groupid});
				Double balance = Double.valueOf(groupMap.get("balance") + "");
				String name = (String)groupMap.get("name");
				if(money <= balance){//提现金额不大于余额
					//扣除帐号余额//写提现申请表
					List<Map<String, Object>> sqlList = new ArrayList<Map<String,Object>>();
					Map<String, Object> comSqlMap = new HashMap<String, Object>();
					comSqlMap.put("sql", "update org_group_tb set balance = balance-? where id= ?");
					comSqlMap.put("values", new Object[]{money, groupid});

					Long withdraw_id = daService.getkey("seq_withdrawer_tb");
					Map<String, Object> withdrawSqlMap = new HashMap<String, Object>();
					withdrawSqlMap.put("sql", "insert into withdrawer_tb  (id,groupid,amount,create_time,acc_id,uin,wtype) values(?,?,?,?,?,?,?)");
					withdrawSqlMap.put("values", new Object[]{withdraw_id, groupid, money, ntime, accId, oprator_id, type});

					Map<String, Object> groupAccountSqlMap = new HashMap<String, Object>();
					groupAccountSqlMap.put("sql", "insert into group_account_tb (groupid,amount,create_time,type,remark,withdraw_id,source) values(?,?,?,?,?,?,?)");
					groupAccountSqlMap.put("values", new Object[]{groupid, money, ntime, 1, "提现申请", withdraw_id, 1});

					Map<String, Object> cityAccountSqlMap = new HashMap<String, Object>();
					cityAccountSqlMap.put("sql", "insert into tingchebao_account_tb (amount,create_time,type,remark,withdraw_id,utype,uin) values(?,?,?,?,?,?,?)");
					cityAccountSqlMap.put("values", new Object[]{money, ntime, 1, name+"提现申请", withdraw_id, 8, oprator_id});

					sqlList.add(comSqlMap);
					sqlList.add(withdrawSqlMap);
					sqlList.add(groupAccountSqlMap);
					sqlList.add(cityAccountSqlMap);
					result = daService.bathUpdate(sqlList);
				}
				if(result){
					mongoDbUtils.saveLogs(request, 0, 3, "提现：金额："+money);
					return 1;
				}
			}
			return 0;
		}
		return -1;
	}
}
