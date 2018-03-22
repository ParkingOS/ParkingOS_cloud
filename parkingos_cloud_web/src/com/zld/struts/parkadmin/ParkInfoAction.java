package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.utils.RequestUtil;
import com.zld.utils.ZLDType;
import org.apache.log4j.Logger;
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
/**
 * 停车场修改
 * @author Administrator
 *
 */
public class ParkInfoAction extends Action {

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	private Logger logger = Logger.getLogger(ParkInfoAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
//		Long id = RequestUtil.getLong(request, "id", -1L);
		Long comid = (Long)request.getSession().getAttribute("comid");
		String operater= request.getSession().getAttribute("loginuin")+"";
		Integer authId = RequestUtil.getInteger(request, "authid",-1);
		request.setAttribute("authid", authId);
		if(action.equals("")){
			Map<String, Object> comMap = daService.getPojo("select * from com_info_tb  where id=?",new Object[]{comid});
			StringBuffer comBuffer = new StringBuffer("[");
			for (String  key : comMap.keySet()) {
				comBuffer.append("{\"name\":\""+key+"\",\"value\":\""+comMap.get(key)+"\"},");
			}
			String result = comBuffer.toString();
			result = result.substring(0,result.length()-1)+"]";
			result =result.replace("null", "");
			request.setAttribute("cominfo", result);
			String type =  RequestUtil.getString(request, "type");
			if(type.equals("set")){//车场设置
				return mapping.findForward("parkset");
			}else {
				return mapping.findForward("success");
			}
		}else if(action.equals("edit")){
			String company =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "company_name"));
			String address =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
			String phone =RequestUtil.processParams(request, "phone");
			String mobile =RequestUtil.processParams(request, "mobile");
			String property =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "property"));
			String id =RequestUtil.processParams(request, "id");
			Integer type = RequestUtil.getInteger(request, "type", 0);
			Integer parking_type = RequestUtil.getInteger(request, "parking_type", 0);
			Integer parking_total = RequestUtil.getInteger(request, "parking_total", 0);
			Integer share_number = RequestUtil.getInteger(request, "share_number", 0);
			Integer nfc = RequestUtil.getInteger(request, "nfc", 0);
			Integer etc = RequestUtil.getInteger(request, "etc", 0);
			Integer book = RequestUtil.getInteger(request, "book", 0);
			Integer navi = RequestUtil.getInteger(request, "navi", 0);
			Integer monthlypay = RequestUtil.getInteger(request, "monthlypay", 0);
			Integer epay = RequestUtil.getInteger(request, "epay", 0);
			Integer isnight = RequestUtil.getInteger(request, "isnight", 0);//夜晚停车，0:支持，1不支持
			Long invalid_order = RequestUtil.getLong(request, "invalid_order", 0L);
			String sql = "update com_info_tb set company_name=?,address=?,phone=?,mobile=?,property=?," +
					"parking_total=?,share_number=?,parking_type=?,type=?,update_time=?,nfc=?,etc=?,book=?,navi=?,monthlypay=?" +
					",isnight=?,epay=?,invalid_order=? where id=?";
			Object [] values = new Object[]{company,address,phone,mobile,property,parking_total,share_number,parking_type,type,
					System.currentTimeMillis()/1000,nfc,etc,book,navi,monthlypay,isnight,epay,invalid_order,Long.valueOf(id)};
			int result = daService.update(sql, values);
			if(result==1){
//				Map comMap = daService.getMap("select * from com_info_tb where id=?", new Object[]{Long.valueOf(id)});
//				ParkingMap.updateParkingMap(comMap);
				if(publicMethods.isEtcPark(comid)){
					int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_info_tb",Long.valueOf(id),System.currentTimeMillis()/1000,1});
					logger.error("parkadmin or admin:"+operater+" edit comid:"+comid+" com_info_tb ,add sync ret:"+re);
				}else{
					logger.error("parkadmin or admin:"+operater+" edit comid:"+comid+" com_info_tb");
				}
				mongoDbUtils.saveLogs(request, 0, 3, "修改了车场（编号："+id+"）");
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("parkset")){
			String id =RequestUtil.processParams(request, "id");
			Integer car_type = RequestUtil.getInteger(request, "car_type", 0);//是否区分大小车，0:不区分，1：区分
			Integer passfree = RequestUtil.getInteger(request, "passfree", 0);//免费结算订单
			Integer isautopay = RequestUtil.getInteger(request, "isautopay", 0);//是否自动结算
			Integer full_set = RequestUtil.getInteger(request, "full_set", 0);//是否自动结算
			Integer leave_set = RequestUtil.getInteger(request, "leave_set", 0);//是否自动结算
			Integer ishidehdbutton = RequestUtil.getInteger(request, "ishidehdbutton", 1);
			String firstprovince =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "firstprovince"));
			Integer entry_set = RequestUtil.getInteger(request, "entry_set", 0);
			Integer ishdmoney = RequestUtil.getInteger(request, "ishdmoney", 0);
			Integer entry_month2_set = RequestUtil.getInteger(request, "entry_month2_set", 0);
			String sql = "update com_info_tb set car_type=?,passfree=?,ishidehdbutton=?,isautopay=?,full_set=?,leave_set=?,firstprovince=?,entry_set=?,entry_month2_set=?,ishdmoney=? where id=?";
			Object [] values = new Object[]{car_type,passfree,ishidehdbutton,isautopay,full_set,leave_set,firstprovince, entry_set, entry_month2_set,ishdmoney,Long.valueOf(id)};
			int result = daService.update(sql, values);
			if(result==1){
//				Map comMap = daService.getMap("select * from com_info_tb where id=?", new Object[]{Long.valueOf(id)});
//				ParkingMap.updateParkingMap(comMap);
				if(publicMethods.isEtcPark(comid)){
					int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_info_tb",Long.valueOf(id),System.currentTimeMillis()/1000,1});
					logger.error("parkadmin or admin:"+operater+" edit comid:"+comid+" com_info_tb ,add sync ret:"+re);
				}else{
					logger.error("parkadmin or admin:"+operater+" edit comid:"+comid+" com_info_tb");
				}
				mongoDbUtils.saveLogs(request, 0, 3, "设置了车场参数（编号："+id+"），car_type："+car_type+",passfree:"+passfree+",isautopay:"+isautopay+",full_set:"+full_set+",leave_set:"+leave_set+",firstprovince:"+firstprovince+
						",entry_set:"+entry_set+",entry_month2_set:"+entry_month2_set+",ishdmoney:"+ishdmoney);
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}
		else if(action.equals("editcontactor")){
			String mobile =RequestUtil.processParams(request, "mobile");
			String strid =RequestUtil.processParams(request, "strid");
			String pass =RequestUtil.processParams(request, "pass");
			if(pass.equals(""))
				pass  = strid;
			String nickname =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname"));
			String sql = "update user_info_tb set strid=?,password=?,mobile=?,nickname=? where comid=? and auth_flag=?";
			int result = daService.update(sql, new Object[]{strid,pass,mobile,nickname,comid,ZLDType.ZLD_PARKADMIN_ROLE});
//			if(result == 1){
//				int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_led_tb",Long.valueOf(id),System.currentTimeMillis()/1000,0});
//				logger.error("parkadmin or admin:"+nickname+" add comid:"+comid+" parkuser ,add sync ret:"+r);
//			}
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("withdraw")){
			Double money = RequestUtil.getDouble(request, "money", 0d);
			Long ntime = System.currentTimeMillis()/1000;
			//检查帐户是否已绑定
			List<Map<String, Object>> accList = daService.getAll("select id,type from com_account_tb where comid =? and type in(?,?) and state =? order by id desc",
					new Object[]{comid, 0, 2, 0});
			Long accId = null;
			Integer type =0;
			if(accList!=null&&!accList.isEmpty()){
				accId = null;
				for(Map<String, Object> m: accList){
					type = (Integer)m.get("type");
					if(type!=null&&type==2){
						accId =  (Long)m.get("id");
						break;
					}
				}
				if(accId==null)
					accId=(Long)accList.get(0).get("id");
			}
			if(accId!=null&&accId>0){
				boolean result =false;
				if(money>0){
					Map<String, Object> comMap = daService.getMap("select money,company_name from com_info_Tb where id=? ", new Object[]{comid});
					Double balance = Double.valueOf(comMap.get("money")+"");
					String name = (String)comMap.get("company_name");
					Long uin = (Long)request.getSession().getAttribute("loginuin");
					if(money<=balance){//提现金额不大于余额
						//扣除帐号余额//写提现申请表
						List<Map<String, Object>> sqlList = new ArrayList<Map<String,Object>>();
						Map<String, Object> comSqlMap = new HashMap<String, Object>();
						comSqlMap.put("sql", "update com_info_tb set money = money-? where id= ?");
						comSqlMap.put("values", new Object[]{money,comid});
						Long withdraw_id = daService.getkey("seq_withdrawer_tb");
						Map<String, Object> withdrawSqlMap = new HashMap<String, Object>();
						withdrawSqlMap.put("sql", "insert into withdrawer_tb(id,comid,amount,create_time,acc_id,uin,wtype) values(?,?,?,?,?,?,?)");
						withdrawSqlMap.put("values", new Object[]{withdraw_id,comid,money,ntime,accId,uin,type});
						Map<String, Object> moneySqlMap = new HashMap<String, Object>();
						moneySqlMap.put("sql", "insert into money_record_tb (comid,amount,create_time,type,remark) values(?,?,?,?,?)");
						moneySqlMap.put("values", new Object[]{comid,money,ntime,2,name+"提现申请"});

						Map<String, Object> parkAccountSqlMap = new HashMap<String, Object>();
						parkAccountSqlMap.put("sql", "insert into park_account_tb (comid,amount,create_time,type,remark,uid,source,withdraw_id) values(?,?,?,?,?,?,?,?)");
						parkAccountSqlMap.put("values", new Object[]{comid,money,ntime,1,"提现申请",uin,5, withdraw_id});

						Map<String, Object> cityAccountSqlMap = new HashMap<String, Object>();
						cityAccountSqlMap.put("sql", "insert into tingchebao_account_tb (amount,create_time,type,remark,withdraw_id,utype,uin) values(?,?,?,?,?,?,?)");
						cityAccountSqlMap.put("values", new Object[]{money, ntime, 1, name+"提现申请", withdraw_id, 8, uin});
						sqlList.add(comSqlMap);
						sqlList.add(withdrawSqlMap);
						sqlList.add(moneySqlMap);
						sqlList.add(parkAccountSqlMap);
						sqlList.add(cityAccountSqlMap);
						result = daService.bathUpdate(sqlList);
					}
					if(result){
						AjaxUtil.ajaxOutput(response, "1");
						mongoDbUtils.saveLogs(request, 0, 3, "提现：金额："+money);
					}else {
						AjaxUtil.ajaxOutput(response, "0");
					}
				}
			}else {
				AjaxUtil.ajaxOutput(response, "-1");
			}
		}
		return null;
	}
}
