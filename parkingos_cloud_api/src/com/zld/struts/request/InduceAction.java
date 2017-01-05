package com.zld.struts.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.bouncycastle.jce.provider.symmetric.AES.OFB;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.util.Hash;
import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.RequestUtil;

public class InduceAction extends Action {
	@Autowired
	private DataBaseService daService;
	
	@Autowired
	private PgOnlyReadService pService;
	
	@Autowired
	private PublicMethods publicMethods;
	
	@Autowired
	private CommonMethods commonMethods;
	
	private Logger logger = Logger.getLogger(InduceAction.class);

	/**
	 * weixin
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		if(action.equals("induceinfo")){//获取所有诱导屏
			Long ntime = System.currentTimeMillis()/1000;
			//****************************实时计算道路停车场的总泊位和余位*****************************//
			List<Map<String, Object>> list = pService.getAll("select count(distinct p.id) amount,p.comid,p.berthsec_id from com_park_tb p left join dici_tb d on p.dici_id=d.id where " +
					" p.comid in (select id from com_info_tb where parking_type=? and state<>? ) and (p.order_id is null or p.order_id<?) and (d.state=? or d.state is null) " +
					"and p.is_delete=? group by p.comid,p.berthsec_id ", 
					new Object[]{2, 1, 0, 0, 0});//道路停车场的剩余泊位数
			List<Map<String, Object>> berthlist = pService.getAll("select count(id) total,comid,berthsec_id from com_park_tb where " +
					"comid in (select id from com_info_tb where parking_type=? and state<>? ) and is_delete=? group by comid,berthsec_id ", 
					new Object[]{2, 1, 0});//道路停车场的总泊位
			//**************************更新道路停车场的余位信息*************************************//
			String remainSql = "insert into remain_berth_tb(comid,amount,berthseg_id,total,update_time) values(?,?,?,?,?) ";
			List<Object[]> anlyList = new ArrayList<Object[]>();
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					Long comid = (Long)map.get("comid");
					Long amount = (Long)map.get("amount");
					Long berthsec_id = (Long)map.get("berthsec_id");
					Long total = 0L;
					for(Map<String, Object> map2 : berthlist){
						Long cid = (Long)map2.get("comid");
						Long bid = (Long)map2.get("berthsec_id");
						if(comid.intValue() == cid.intValue() && berthsec_id.intValue() == bid.intValue()){
							total = (Long)map2.get("total");
							break;
						}
					}
					map.put("total", total);
					Object[] values2 = new Object[]{comid,amount,berthsec_id,total,ntime};
					anlyList.add(values2);
				}
			}
			if(!anlyList.isEmpty()){
				int ret = daService.update("delete from remain_berth_tb where comid in (select id from com_info_tb where parking_type=? and state<>? )", 
						new Object[]{2, 1});
				logger.error("delete remain infos>>>ret:"+ret);
				ret = daService.bathInsert(remainSql, anlyList, new int[]{4,4,4,4,4});
				logger.error("insert remain infos>>>ret:"+ret);
			}
			//*********************************把封闭车场和最新的占道车场的余位信息合并*************************************//
			List<Map<String, Object>> rList = new ArrayList<Map<String,Object>>();
			List<Map<String, Object>> remainList = pService.getAll("select comid,berthseg_id,amount,total,update_time from remain_berth_tb where comid not in " +
					" (select id from com_info_tb where parking_type=? and state<>? ) and state=? ", new Object[]{2, 1, 0});
			if(remainList != null){
				rList.addAll(remainList);
			}
			rList.addAll(list);
			//*********************************查询所有诱导屏的绑定车场和广告信息******************************************************//
			List<Map<String, Object>> induceList = pService.getAll("select id,name,type,did,longitude,latitude,address,state " +
					" from induce_tb where is_delete=? ", 
					new Object[]{0});
			List<Map<String, Object>> comList = pService.getAll("select c.id,company_name as parkname,p.induce_id,p.module_id from com_info_tb c,induce_park_tb p where c.id=p.comid order by p.sort nulls last ", 
					new Object[]{});
			List<Map<String, Object>> moduleList = pService.getAll("select * from induce_module_tb where is_delete=? order by sort nulls last ", 
					new Object[]{0});
			List<Map<String, Object>> adList = pService.getAll("select ad,begin_time,end_time,induce_id from induce_ad_tb where isactive=? ", 
					new Object[]{1});
			Map<String, Object> resultMap = new HashMap<String, Object>();
			//**************************************合并信息****************************************************//
			List<Map<String, Object>> indList = new ArrayList<Map<String,Object>>();
			if(induceList != null && !induceList.isEmpty()){
				for(Map<String, Object> map : induceList){
					Long induce_id = (Long)map.get("id");
					Integer type = (Integer)map.get("type");
					List<Map<String, Object>> parkList = new ArrayList<Map<String,Object>>();
					if(comList != null && !comList.isEmpty()){
						for(Map<String, Object> comMap : comList){
							Long ind_id = (Long)comMap.get("induce_id");
							Long comid = (Long)comMap.get("id");
							if(ind_id.intValue() == induce_id.intValue()){
								parkList.add(comMap);
								if(rList != null && !rList.isEmpty()){
									Long remain = 0L;//余位
									Long total = 0L;//总泊位
									for(Map<String, Object> rMap : rList){
										Long cid = (Long)rMap.get("comid");
										if(comid.intValue() == cid.intValue()){
											Long amount = (Long)rMap.get("amount");
											Long btotal = (Long)rMap.get("total");
											remain += amount;
											total += btotal;
										}
									}
									comMap.put("remain", remain);
									comMap.put("total", total);
								}
							}
						}
					}
					map.put("parklist", parkList);
					List<Map<String, Object>> adList2 = new ArrayList<Map<String,Object>>();
					if(adList != null && !adList.isEmpty()){
						for(Map<String, Object> ad : adList){
							Long ind_id = (Long)ad.get("induce_id");
							if(induce_id.intValue() == ind_id.intValue()){
								adList2.add(ad);
								break;
							}
						}
					}
					map.put("ad", adList2);
					if(type == 0 || type == 1){
						List<Map<String, Object>> mList = new ArrayList<Map<String,Object>>();
						if(moduleList != null && !moduleList.isEmpty()){
							for(Map<String, Object> module : moduleList){
								Long mid = (Long)module.get("id");
								Long ind_id = (Long)module.get("induce_id");
								if(induce_id.intValue() == ind_id.intValue()){
									Map<String, Object> moduleMap = new HashMap<String, Object>();
									moduleMap.put("modulename", module.get("name"));
									//moduleMap.put("parkname", module.get("name"));//孙总让改的 暂时先这么处理
									Long remain = 0L;
									Long total = 0L;
									if(comList != null && !comList.isEmpty()){
										for(Map<String, Object> comMap : comList){
											Long module_id = (Long)comMap.get("module_id");
											if(mid.intValue() == module_id.intValue()){
												Long r = (Long)comMap.get("remain");
												Long t = (Long)comMap.get("total");
												remain += r;
												total += t;
											}
										}
									}
									moduleMap.put("remain", remain);
									moduleMap.put("total", total);
									mList.add(moduleMap);
								}
							}
						}
						map.put("module", mList);
						//map.put("parklist", mList);//孙总让改的 暂时先这么处理
					}
				}
			}
			if(induceList != null && !induceList.isEmpty()){
				indList = induceList;
			}
			resultMap.put("success", true);
			resultMap.put("error", null);
			resultMap.put("inducelist", indList);
			resultMap.put("total", indList.size());
			JSONArray json = JSONArray.fromObject(resultMap);
			AjaxUtil.ajaxOutput(response, json.toString());
			//http://127.0.0.1/zld/induceinfo.do?action=induceinfo
		}else if(action.equals("parkinfo")){//获取余位
			String did = RequestUtil.processParams(request, "did");
			Long ntime = System.currentTimeMillis()/1000;
			Map<String, Object> resultMap = new HashMap<String, Object>();
			if(!did.equals("")){
				Map<String, Object> induceMap = pService.getMap("select id,did,type from induce_tb where did=? and is_delete=? limit ? ",
						new Object[]{did, 0, 1});
				if(induceMap != null){
					Long induceId = (Long)induceMap.get("id");
					Integer type = (Integer)induceMap.get("type");
					List<Map<String, Object>> remainList = pService.getAll("select count(distinct p.id) amount,p.comid,p.berthsec_id from com_park_tb p left join dici_tb d on p.dici_id=d.id where " +
							" p.comid in (select c.id from com_info_tb c,induce_park_tb i where c.id=i.comid and c.parking_type=? and c.state<>? and i.induce_id=? )" +
							" and (p.order_id is null or p.order_id<?) and (d.state=? or d.state is null) and p.is_delete=? group by p.comid,p.berthsec_id ", 
							new Object[]{2, 1, induceId, 0, 0, 0});//占道车场的余位查询
					List<Map<String, Object>> berthList = pService.getAll("select count(id) total,comid,berthsec_id from com_park_tb where " +
							"comid in (select c.id from com_info_tb c,induce_park_tb i where c.id=i.comid and c.parking_type=? and c.state<>? and i.induce_id=? )" +
							" and is_delete=? group by comid,berthsec_id ", 
							new Object[]{2, 1, induceId, 0});//占道车场的总泊位查询
					List<Map<String, Object>> parkList = new ArrayList<Map<String,Object>>();
					//**************************更新道路停车场的余位信息*************************************//
					String remainSql = "insert into remain_berth_tb(comid,amount,berthseg_id,total,update_time) values(?,?,?,?,?) ";
					List<Object[]> anlyList = new ArrayList<Object[]>();
					if(remainList != null && !remainList.isEmpty()){
						for(Map<String, Object> map : remainList){
							Long comid = (Long)map.get("comid");
							Long amount = (Long)map.get("amount");
							Long berthsec_id = (Long)map.get("berthsec_id");
							Long total = 0L;
							for(Map<String, Object> map2 : berthList){
								Long cid = (Long)map2.get("comid");
								Long bid = (Long)map2.get("berthsec_id");
								if(comid.intValue() == cid.intValue() && berthsec_id.intValue() == bid.intValue()){
									total = (Long)map2.get("total");
									break;
								}
							}
							map.put("total", total);
							Object[] values2 = new Object[]{comid,amount,berthsec_id,total,ntime};
							anlyList.add(values2);
						}
					}
					if(!anlyList.isEmpty()){
						int ret = daService.update("delete from remain_berth_tb where comid in (select c.id from com_info_tb c,induce_park_tb i where c.id=i.comid and c.parking_type=? and c.state<>? and i.induce_id=? )", 
								new Object[]{2, 1, induceId});
						logger.error("delete remain infos>>>ret:"+ret);
						ret = daService.bathInsert(remainSql, anlyList, new int[]{4,4,4,4,4});
						logger.error("insert remain infos>>>ret:"+ret);
					}
					//*********************************把封闭车场和最新的占道车场的余位信息合并*************************************//
					List<Map<String, Object>> rList = new ArrayList<Map<String,Object>>();
					List<Map<String, Object>> closeList = pService.getAll("select comid,berthseg_id,amount,total,update_time from remain_berth_tb where comid not in " +
							" (select id from com_info_tb where parking_type=? and state<>? ) and state=? ", new Object[]{2, 1, 0});
					if(remainList != null){
						rList.addAll(remainList);
					}
					rList.addAll(closeList);
					List<Map<String, Object>> comList = pService.getAll("select c.id,company_name as parkname,p.induce_id,p.module_id from com_info_tb c,induce_park_tb p where c.id=p.comid and p.induce_id=? order by p.sort nulls last ", 
							new Object[]{induceId});
					List<Map<String, Object>> moduleList = pService.getAll("select * from induce_module_tb where induce_id=? and is_delete=? order by sort nulls last ", 
							new Object[]{induceId,0});
					if(comList != null && !comList.isEmpty()){
						for(Map<String, Object> comMap : comList){
							Long comid = (Long)comMap.get("id");
							parkList.add(comMap);
							if(rList != null && !rList.isEmpty()){
								Long remain = 0L;//余位
								Long total = 0L;//总泊位
								for(Map<String, Object> rMap : rList){
									Long cid = (Long)rMap.get("comid");
									if(comid.intValue() == cid.intValue()){
										Long amount = (Long)rMap.get("amount");
										Long btotal = (Long)rMap.get("total");
										remain += amount;
										total += btotal;
									}
								}
								comMap.put("remain", remain);
								comMap.put("total", total);
							}
						}
					}
					resultMap.put("parklist", parkList);
					if(type == 0 || type == 1){
						List<Map<String, Object>> mList = new ArrayList<Map<String,Object>>();
						if(moduleList != null && !moduleList.isEmpty()){
							for(Map<String, Object> module : moduleList){
								Long mid = (Long)module.get("id");
								Map<String, Object> moduleMap = new HashMap<String, Object>();
								moduleMap.put("modulename", module.get("name"));
								//moduleMap.put("parkname", module.get("name"));//孙总让改的 暂时先这么处理
								Long remain = 0L;
								Long total = 0L;
								if(comList != null && !comList.isEmpty()){
									for(Map<String, Object> comMap : comList){
										Long module_id = (Long)comMap.get("module_id");
										if(mid.intValue() == module_id.intValue()){
											Long r = (Long)comMap.get("remain");
											Long t = (Long)comMap.get("total");
											remain += r;
											total += t;
										}
									}
								}
								moduleMap.put("remain", remain);
								moduleMap.put("total", total);
								mList.add(moduleMap);
							}
						}
						resultMap.put("module", mList);
						
						//resultMap.put("parklist", mList);
					}
					resultMap.put("success", true);
					resultMap.put("error", null);
					resultMap.put("total", parkList.size());
				}else{
					resultMap.put("success", false);
					resultMap.put("error", "编号错误，未找到对应诱导屏");
				}
			}else{
				resultMap.put("success", false);
				resultMap.put("error", "传入参数错误");
			}
			JSONArray json = JSONArray.fromObject(resultMap);
			AjaxUtil.ajaxOutput(response, json.toString());
		}else if(action.equals("updateadstat")){
			String dids = RequestUtil.processParams(request, "did");
			logger.error("updateadstat>>>dids:"+dids);
			Long ntime = System.currentTimeMillis()/1000;
			Map<String, Object> resultMap = new HashMap<String, Object>();
			if(!dids.equals("")){
				String[] didsStrs = dids.split(",");
				List<Object> params = new ArrayList<Object>();
				List<Object> didList = new ArrayList<Object>();
				String preParams  ="";
				for(int i = 0;i<didsStrs.length; i++){
					didList.add(didsStrs[i]);
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				params.add(0);
				params.addAll(didList);
				List<Map<String, Object>> iList = pService.getAllMap("select a.* from induce_tb i,induce_ad_tb a where i.id=a.induce_id and i.is_delete=? " +
						" and did in ("+preParams+") ", params);
				if(iList != null && !iList.isEmpty()){
					List<Object> idList = new ArrayList<Object>();
					List<Object[]> anlyList = new ArrayList<Object[]>();
					String sql = "insert into induce_ad_history_tb(induce_id,create_time,begin_time,end_time,ad,creator_id) values(?,?,?,?,?,?) ";
					preParams  ="";
					for(Map<String, Object> map : iList){
						Long induce_id = (Long)map.get("induce_id");
						idList.add(induce_id);
						if(preParams.equals(""))
							preParams ="?";
						else
							preParams += ",?";
						
						
						Object[] values2 = new Object[]{induce_id,ntime,map.get("begin_time"),map.get("end_time"),map.get("ad"),map.get("updator_id")};
						anlyList.add(values2);
					}
					List<Object> pList = new ArrayList<Object>();
					pList.add(1);
					pList.add(ntime);
					pList.addAll(idList);
					int r = daService.update("update induce_ad_tb set isactive=?,publish_time=? where induce_id in ("+preParams+") ", pList);
					logger.error("update ad publish status>>>did:"+dids+",r:"+r);
					if(r > 0 &&!anlyList.isEmpty()){
						int ret = daService.bathInsert(sql, anlyList, new int[]{4,4,4,4,12,4});
						logger.error("insert ad history>>>ret:"+ret);
					}
					resultMap.put("success", true);
					resultMap.put("error", null);
					resultMap.put("total", r);
				}else{
					resultMap.put("success", false);
					resultMap.put("error", "传入参数错误");
				}
			}else{
				resultMap.put("success", false);
				resultMap.put("error", "传入参数错误");
			}
			JSONArray json = JSONArray.fromObject(resultMap);
			AjaxUtil.ajaxOutput(response, json.toString());
			//http://127.0.0.1/zld/induceinfo.do?action=updateadstat&did=A00000B02,A00000B01
		}else if(action.equals("heartbeats")){
			String did = RequestUtil.processParams(request, "did");
			logger.error("heartbeat did:"+did);
			Long ntime = System.currentTimeMillis()/1000;
			Map<String, Object> resultMap = new HashMap<String, Object>();
			if(!did.equals("")){
				int ret = daService.update("update induce_tb set heartbeat_time=? where did=? ", 
						new Object[]{ntime, did});
				logger.error("heartbeat update ret:"+ret+",did:"+did);
				commonMethods.deviceRecover(2, did, ntime);
				if(ret > 0){
					Map<String, String> map = new HashMap<String, String>();
					map.put("did", did);
					commonMethods.writeToMongodb("zld_induce_logs", map);
					resultMap.put("success", true);
					resultMap.put("error", null);
				}else{
					resultMap.put("success", false);
					resultMap.put("error", "传入参数错误");
				}
			}else{
				resultMap.put("success", false);
				resultMap.put("error", "传入参数错误");
			}
			JSONArray json = JSONArray.fromObject(resultMap);
			AjaxUtil.ajaxOutput(response, json.toString());
			//http://127.0.0.1/zld/induceinfo.do?action=heartbeats&did=A00000B02
		}
		return null;
	}
}
