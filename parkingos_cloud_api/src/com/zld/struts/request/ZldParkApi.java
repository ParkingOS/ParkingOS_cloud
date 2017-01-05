package com.zld.struts.request;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import sun.invoke.empty.Empty;

import com.zld.AjaxUtil;
import com.zld.impl.MemcacheUtils;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.Check;
import com.zld.utils.StringUtils;


/**
 * 查询车场信息
 * @author laoyao
 *
 */
@Path("parkinfo")
public class ZldParkApi {
	
	Logger logger = Logger.getLogger(ZldParkApi.class);
	/**
	 * 停车场数据
	 * http://127.0.0.1/zld/api/parkinfo/queryPark/
	 */
	@POST
	@Path("/queryPark")//北京查询
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void queryPark( @FormParam("local") String local,@FormParam("time") String timeStamp,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		//List<Map<String, Object>> returnMap =null;
		//System.out.println(timeStamp);
		//System.out.println(local);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		PgOnlyReadService daService = (PgOnlyReadService) ctx.getBean("pgOnlyReadService");
		MemcacheUtils memcacheUtils = (MemcacheUtils) ctx.getBean("memcacheUtils");
		//车场基本信息 图片 价格
		/*
		 * id bigint NOT NULL, —车场编号
		  company_name character varying(100), —车场名称
		  longitude numeric(10,6),—经度
		  latitude numeric(10,6),—纬度，
		  record_number character varying(200), — 备案号
		  address character varying(100),—地址
		  parking_type integer, — 车位类型，0地面，1地下，2占道
		  parking_total integer DEFAULT 0,--总车位数
		  图片(一对多) 前缀http://s.tingchebao.com/tcbcloud/
		 价格
		 */
		Long time =1398873600L;//2014-05-01
		Map<Long, String> preResult = memcacheUtils.doMapLongStringCache("getparkinfo_lasttime", null, null);
		Long ntime = System.currentTimeMillis()/1000;
		//logger.error("pretime:"+preTime+",ntime:"+ntime);
		if(preResult!=null&&preResult.size()==1){
			Long ptime =-1L;
			for(Long key : preResult.keySet()){
				ptime = key ;
			}
			if(ntime-ptime<180){
				AjaxUtil.ajaxOutput(response,preResult.get(ptime));
				logger.error("请求时间小于3分钟，返回缓存数据");
				//logger.error(preResult.get(ptime));
				return ;
			}
		}
		
		if(Check.isLong(timeStamp)){
			time= Long.valueOf(timeStamp);
		}
		String sql = "select  c.id ,company_name,longitude,latitude,record_number,address,parking_type,parking_total "+
				"from com_info_tb  c "+
				"where update_time>? and  state=0 and  city between ? and ? "+
				"and address like ? and longitude > ?  and position('测试' in company_name) =0  "+
				"and position('小刘' in company_name) =0 "+
				"and char_length(company_name)>2";
		//查询所有车场
		Object[] params =null;
		String errmesg = "";
		if(local==null){
			errmesg="[{errmsg:\"参数local未赋值\"}]";
			System.out.println("------->>>>>>>>>>>>>>queryPark,errmesg:"+errmesg);
			AjaxUtil.ajaxOutput(response, errmesg);
			return ;
		}
		
		if(local.equals("beijing")){//北京的车场
			params = new Object[]{time,110000,110117,"北京市%",116.0};
		}else {
			errmesg="[{errmsg:\"参数local错误，请传入地区全拼，如:beijing\"}]";
			System.out.println("------->>>>>>>>>>>>>>queryPark,errmesg:"+errmesg);
			AjaxUtil.ajaxOutput(response, errmesg);
			return ;
		}
		List<Map<String, Object>> allList = daService.getAll(sql,params);
		
		//空闲车位数
		List<Map<String, Object>> parkUseList = daService.getAll("select sum(total) as total,comid " +
				"from remain_berth_tb where comid in(select  id " +
				"from com_info_tb  c " +
				"where update_time>? and  state=0 and  city between ? and ? " +
				"and address like ? and longitude > ?  and position('测试' in company_name) =0  " +
				"and position('小刘' in company_name) =0 " +
				"and char_length(company_name)>2) group by comid", new Object[]{time,110000,110117,"北京市%",116.0});
		
		Map<Long, Map<String , Object>> parkMap = new HashMap<Long, Map<String,Object>>();
		
	/*	for(Map<String, Object> map : allList){
			Long parkId = (Long)map.get("id");
			for(String key : map.keySet()){
				Object value = map.get(key);
				if(value==null)
					map.put(key, "");
			}
			if(parkMap.containsKey(parkId)){
				Map<String, Object> preMap = parkMap.get(parkId);
				String picUrls =(String)preMap.get("picurl");
				String picUrl = (String)map.get("picurl");
				if(picUrl!=null){
					picUrls =picUrls.substring(0,picUrls.length()-1)+",\"http://s.tingchebao.com/tcbcloud/"+picUrl+"\"]";
					preMap.put("picurl", picUrls);
				}
			}else {
				String price = getPrice(parkId, daService);
				map.put("price", price);
				String picUrl = (String)map.get("picurl");
				if(picUrl==null)
					picUrl = "";
				else {
					picUrl = "http://s.tingchebao.com/tcbcloud/"+picUrl;
				}
				map.put("picurl", "[\""+picUrl+"\"]");
				parkMap.put(parkId, map);
			}
		}*/
		
		for(Map<String, Object> map : allList){
			Long parkId = (Long)map.get("id");
			String price = getPrice(parkId, daService);
			map.put("price", price);
			map.put("picurl", "[]");
			Integer total=(Integer)map.get("parking_total");
			map.put("empty",total);
			//parkMap.put(parkId, map);
			for(Map<String, Object> eMap: parkUseList){
				Long comid = (Long)eMap.get("comid");
				if(parkId.equals(comid)){
					BigDecimal empty = (BigDecimal)eMap.get("total");
					if(empty.intValue()>total)
						empty = new BigDecimal(total);
					map.put("empty",empty);
					break;
				}
			}
		}
//		returnMap = new ArrayList<Map<String,Object>>();
//		for(Long key : parkMap.keySet()){
//			returnMap.add(parkMap.get(key));
//		}
		String ret = StringUtils.getJson(allList);
		//logger.error(ret);
		ret = ret.replace("null", "");
		if(preResult==null){
			preResult = new HashMap<Long, String>();
		}else {
			preResult.clear();
		}
		preResult.put(ntime, ret);
		memcacheUtils.doMapLongStringCache("getparkinfo_lasttime", preResult, "update");
		logger.error("放入缓存..");
		AjaxUtil.ajaxOutput(response,ret);
	}
	/**
	 * 取首小时价格
	 * @param parkId
	 * @return
	 */
	private String getPrice(Long parkId,PgOnlyReadService daService){
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		//开始小时
		int bhour = calendar.get(Calendar.HOUR_OF_DAY);
		List<Map<String, Object>> priceList=daService.getAll("select * from price_tb where comid=? " +
				"and state=? and pay_type=? order by id desc", new Object[]{parkId,0,0});
		if(priceList==null||priceList.size()==0){//没有按时段策略
			//查按次策略
			priceList=daService.getAll("select * from price_tb where comid=? " +
					"and state=? and pay_type=? order by id desc", new Object[]{parkId,0,1});
			if(priceList==null||priceList.size()==0){//没有按次策略，返回提示
				return "0元/次";
			}else {//有按次策略，直接返回一次的收费
				Map timeMap =priceList.get(0);
				Integer unit = (Integer)timeMap.get("unit");
				if(unit!=null&&unit>0){
					if(unit>60){
						String t = "";
						if(unit%60==0)
							t = unit/60+"小时";
						else
							t = unit/60+"小时 "+unit%60+"分钟";
						return timeMap.get("price")+"元/"+t;
					}else {
						return timeMap.get("price")+"元/"+unit+"分钟";
					}
				}else {
					return timeMap.get("price")+"元/次";
				}
				//return timeMap.get("price")+"元/次";
			}
			//发短信给管理员，通过设置好价格
		}else {//从按时段价格策略中分拣出日间和夜间收费策略
			if(priceList.size()>0){
				for(Map map : priceList){
					Integer btime = (Integer)map.get("b_time");
					Integer etime = (Integer)map.get("e_time");
					Double price = Double.valueOf(map.get("price")+"");
					Double fprice = Double.valueOf(map.get("fprice")+"");
					Integer ftime = (Integer)map.get("first_times");
					if(ftime!=null&&ftime>0){
						if(fprice>0)
							price = fprice;
					}
					if(btime<etime){//日间 
						if(bhour>=btime&&bhour<etime){
							return price+"元/"+map.get("unit")+"分钟";
						}
					}else {
						if(bhour>=btime||bhour<etime){
							return price+"元/"+map.get("unit")+"分钟";
						}
					}
				}
			}
		}
		return "0.0元/小时";
	}
}
