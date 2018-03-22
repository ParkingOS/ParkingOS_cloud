package com.zld.struts.request;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.HttpProxy;
import com.zld.utils.RequestUtil;
import com.zld.utils.TimeTools;
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

public class CheatSensorAction extends Action {
	@Autowired
	private PgOnlyReadService readService;
	@Autowired
	private DataBaseService writeService;
	@Autowired
	private CommonMethods commonMethods;

	long groupId = 28;

	Logger logger = Logger.getLogger(CheatSensorAction.class);

	@SuppressWarnings("rawtypes")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		String berth = RequestUtil.processParams(request, "berth").trim();
		Long parkId = RequestUtil.getLong(request, "parkid", -1L);
		logger.error("action:"+action+",berth:"+berth+",patkid:"+parkId);
		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("genSensorOrder")){
			Long curTime = System.currentTimeMillis()/1000;
			String re = checkSesnor(berth, parkId);
			if(re != null){
				AjaxUtil.ajaxOutput(response, re);
				return null;
			}
			Map<String, Object> berthMap = readService.getMap("select * from com_park_tb where cid=? and comid=? " +
					" and is_delete=? ", new Object[]{berth, parkId, 0});
			if(berthMap.get("dici_id") == null){
				AjaxUtil.ajaxOutput(response, "该泊位没有绑定车检器");
				return null;
			}
			Integer sensorId = (Integer)berthMap.get("dici_id");
			Map<String, Object> sensorMap = readService.getMap("select * from dici_tb where id=? ",
					new Object[]{sensorId.longValue()});
			if(sensorMap == null){
				AjaxUtil.ajaxOutput(response, "该泊位没有绑定车检器");
				return null;
			}
			String did = (String)sensorMap.get("did");
			logger.error("did:"+did);
			Map<String, String> params = new HashMap<String, String>();
			params.put("sensornumber", did);
			params.put("carintime", TimeTools.getTime_yyyyMMdd_HHmmss(curTime * 1000));
			params.put("indicate", "00");
			logger.error("params:"+params.toString());
			String result = new HttpProxy().doPost("http://180.150.188.224:8080/zld/api/hdinfo/InsertCarAdmission", params);
			logger.error("result:"+result);
			if(result != null){
				AjaxUtil.ajaxOutput(response, "入场操作成功");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "入场操作失败");
			return null;
		}else if(action.equals("settleSensorOrder")){
			Long curTime = System.currentTimeMillis()/1000;
			String re = checkSesnor(berth, parkId);
			if(re != null){
				AjaxUtil.ajaxOutput(response, re);
				return null;
			}
			Map<String, Object> berthMap = readService.getMap("select * from com_park_tb where cid=? and comid=? " +
					" and is_delete=? ", new Object[]{berth, parkId, 0});
			if(berthMap.get("dici_id") == null){
				AjaxUtil.ajaxOutput(response, "该泊位没有绑定车检器");
				return null;
			}
			Integer sensorId = (Integer)berthMap.get("dici_id");
			Map<String, Object> sensorMap = readService.getMap("select * from dici_tb where id=? ",
					new Object[]{sensorId.longValue()});
			if(sensorMap == null){
				AjaxUtil.ajaxOutput(response, "该泊位没有绑定车检器");
				return null;
			}
			String did = (String)sensorMap.get("did");
			logger.error("did:"+did);
			Map<String, Object> berthOrderMap = writeService.getMap("select indicate from berth_order_tb where berth_id=? " +
					" and state=? order by in_time desc limit ? ", new Object[]{did, 0, 1});
			if(berthOrderMap == null){
				AjaxUtil.ajaxOutput(response, "无在场订单");
				return null;
			}

			Map<String, String> params = new HashMap<String, String>();
			params.put("sensornumber", did);
			params.put("carouttime", TimeTools.getTime_yyyyMMdd_HHmmss(curTime * 1000));
			params.put("indicate", berthOrderMap.get("indicate") + "");
			logger.error("params:"+params.toString());
			String result = new HttpProxy().doPost("http://180.150.188.224:8080/zld/api/hdinfo/InsertCarEntrance", params);
			logger.error("result:"+result);
			if(result != null){
				AjaxUtil.ajaxOutput(response, "出场操作成功");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "出场操作失败");
			return null;
		}else if(action.equals("getparks")){
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			String sql = "select id,company_name from com_info_tb ";
			List<Object> params = new ArrayList<Object>();
			List<Object> parks = commonMethods.getParks(groupId);
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " where id in ("+preParams+") ";
				params.addAll(parks);
				list = readService.getAllMap(sql, params);
			}
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择停车场\"}";
			if(list != null && !list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("company_name")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}
		return null;
	}

	private String checkSesnor(String uuid, long parkId){
		try {
			if(uuid == null || "".equals(uuid)){
				return "泊位编号不能为空";
			}
			if(parkId < 0){
				return "请选择车场";
			}
			Long count = readService.getLong("select count(id) from com_park_tb where cid=? and " +
					" comid =? and is_delete=?", new Object[]{uuid, parkId, 0});
			if(count == 0){
				return "泊位不存在";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
