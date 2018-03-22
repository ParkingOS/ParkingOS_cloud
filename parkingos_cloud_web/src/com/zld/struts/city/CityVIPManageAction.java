package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityVIPManageAction extends Action {
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
	Logger logger = Logger.getLogger(CityVIPManageAction.class);
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
			/*String sql = "select v.*,u.mobile,u.nickname,u.address from vip_tb v,user_info_tb u where v.uin=u.id " ;
			String countSql = "select count(v.id) from vip_tb v,user_info_tb u where v.uin=u.id " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"vip_tb","v",new String[]{"mobile","car_number","nickname","address"});
			SqlInfo sqlInfo1 = RequestUtil.customSearch(request,"user_info","u",new String[]{"car_number","uin","comid","bcount","acttotal","atotal","create_time","e_time","id"});
			SqlInfo sqlInfo2 = getSuperSqlInfo(request);
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
				sql += " and v.comid in ("+preParams+") ";
				countSql += " and v.comid in ("+preParams+") ";
				params.addAll(parks);

				if(sqlInfo != null){
					countSql+=" and "+ sqlInfo.getSql();
					sql +=" and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				if(sqlInfo1 != null){
					countSql+=" and "+ sqlInfo1.getSql();
					sql +=" and "+sqlInfo1.getSql();
					params.addAll(sqlInfo1.getParams());
				}
				if(sqlInfo2 != null){
					countSql+=" and "+ sqlInfo2.getSql();
					sql +=" and "+sqlInfo2.getSql();
					params.addAll(sqlInfo2.getParams());
				}
				count = daService.getCount(countSql,params);
				if(count>0){
					list = daService.getAll(sql +" order by v.create_time desc ",params, pageNum, pageSize);
				}
			}*/
			//String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			List arrayList = query(request,cityid,groupid);
			List list = (List<Map<String, Object>>) arrayList.get(0);
			Integer pageNum = (Integer) arrayList.get(1);
			long count = Long.valueOf(arrayList.get(2)+"");
			String fieldsstr = arrayList.get(3)+"";
			setList(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
//			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("create")){
			String r = buyProduct(request,groupid);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			int r = editUser(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("renew")){
			String r = buyProduct(request,groupid);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("detail")){
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			String mobile = RequestUtil.processParams(request, "mobile");
			request.setAttribute("comid", comid);
			request.setAttribute("mobile", mobile);
			return mapping.findForward("detail");
		}else if(action.equals("vipdetail")){
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			String mobile = RequestUtil.processParams(request, "mobile");
			if(comid == -1 || mobile.equals("")){
				return null;
			}
			String sql = "select c.id,p.id p_name,c.uin,c.name,c.address,c.create_time,c.b_time ,c.e_time ,c.remark,c.total,u.mobile,c.p_lot,c.act_total from " +
					"product_package_tb p,carower_product c ,user_info_tb u " +
					"where c.pid=p.id and u.id=c.uin and p.comid=? and u.mobile=? and u.auth_flag=? order by c.create_time desc ";
			String countSql = "select count(c.id) from product_package_tb p,carower_product c ,user_info_tb u " +
					"where c.pid=p.id and u.id=c.uin and p.comid=? and u.mobile=? and u.auth_flag=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Map<String, Object>> list = null;
			List<Object> params = new ArrayList<Object>();
			params.add(comid);
			params.add(mobile);
			params.add(4);
			Long count = pgOnlyReadService.getCount(countSql, params);
			if(count > 0){
				list = pgOnlyReadService.getAll(sql, params, pageNum, pageSize);
			}
			setList(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("inportexcel")){
			String ret = importExcel(request,groupid);
			AjaxUtil.ajaxOutput(response,ret);
		}

		return null;
	}

	private String importExcel(HttpServletRequest request,Long groupid) throws  Exception {
		String errmsg ="";
		System.out.println("月卡上传:"+groupid);
		request.setCharacterEncoding("UTF-8"); // 设置处理请求参数的编码格式
		DiskFileItemFactory factory = new DiskFileItemFactory(); // 建立FileItemFactory对象
		factory.setSizeThreshold(16 * 4096 * 1024);
		ServletFileUpload upload = new ServletFileUpload(factory);
		// 分析请求，并得到上传文件的FileItem对象
		upload.setSizeMax(16 * 4096 * 1024);
		List<FileItem> items = null;
		try {
			items = upload.parseRequest(request);
		} catch (FileUploadException e) {
			e.printStackTrace();
			return "-1";
		}
		String filename = ""; // 上传文件保存到服务器的文件名
		InputStream is = null; // 当前上传文件的InputStream对象
		// 循环处理上传文件

		for (FileItem item : items) {
			// 处理普通的表单域
			if (!item.isFormField()) {
				// 从客户端发送过来的上传文件路径中截取文件名
				// logger.error(item.getName());
				filename = item.getName().substring(
						item.getName().lastIndexOf("\\") + 1);
				is = item.getInputStream(); // 得到上传文件的InputStream对象
			}
		}
		if(is!=null&&filename!=null){
			String syncSql = "insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)";
			List<Object[]> syncValues = new ArrayList<>();
			String insertsql ="insert into carower_product (id,com_id,create_time,b_time,e_time,remark,name,act_total,car_number,card_id)" +
					" values(?,?,?,?,?,?,?,?,?,?) " ;
			String updateSql = "update carower_product set b_time=?, e_time=?,remark=?,name=?,act_total=? where com_id=? and car_number=? ";
			int [] insettypes = new int[]{4,4,4,4,4,12,12,3,12,12};//插入时字段类型
			int [] syncTypes = new int[]{4,12,4,4,4};//同步数据字段类型
			int [] updateTypes = new int[]{4,4,12,12,3,4,12};//更新时字段类型
			List<Object[]> insertValues = new ArrayList<>();
			List<Object[]> updateValues = new ArrayList<>();
			Long ntime= TimeTools.getToDayBeginTime();
			List<Object[]> datas = ImportExcelUtil.generateUserSql(is,filename,1);
			List<Object[]> newdatas = new ArrayList<>();
			List<Object[]> updateDatas = new ArrayList<>();
			Map<Long,Integer> comMaps = new HashMap<>();
			if(datas!=null&&!datas.isEmpty()){
				//数据格式：车场编号*	开始时间*	结束时间*	备注	姓名	金额	车牌号*
				//*是必传
				//过滤同一车场同一车牌的数据
				Map<String,Integer> comCarMap = new HashMap<>();
				int i=1;
				for(Object[] o : datas){
					if(o.length!=7){
						errmsg+=i+"行，数据长度不对，应该为7列,当前"+o.length+"列</br>";
						i++;
						continue;
					}
					boolean isValid = true;
					Long comid =null;//车场编号
					String car_number = o[6]+"";//车牌
					String btime = o[1]+"";
					String etime = o[2]+"";
					if(Check.isLong(o[0]+"")){
						comid = Long.valueOf(o[0]+"");
					}
					if(Check.isEmpty(comid+"")){
						errmsg+=i+"行，车场编号错误："+comid;
						//isValid = false;
						i++;
						continue;
					}
					if(Check.isEmpty(btime)){
						errmsg+=i+"行，开始时间错误："+btime;
						isValid = false;
					}
					if(Check.isEmpty(etime)){
						errmsg+=i+"行，结束时间错误："+etime;
						isValid = false;
					}
					if(Check.isEmpty(car_number)){
						errmsg+=i+"行，车牌错误："+car_number;
						isValid = false;
					}
					if(isValid){
						//数据库检验车场编号和月卡会员中同一车场同一车牌是否存在
						//校验车场
						if(comMaps.containsKey(comid)){
							Integer ha = comCarMap.get(comid);
							if(ha!=null&&ha==0){
								errmsg+=i+"行，车场编号不存在："+comid;
								isValid = false;
							}
						}else{
							Long count = daService.getLong("select count(id) from com_info_tb where id =? and groupid =? ",new Object[]{comid,groupid});
							if(count==null||count<1){
								errmsg+=i+"行，车场编号不存在："+comid;
								isValid = false;
							}
							comMaps.put(comid,count.intValue());
						}
						if(comCarMap.containsKey(comid+car_number)){
							Integer ha = comCarMap.get(comid+car_number);
							if(ha!=null&&ha==1){
								//errmsg+=i+"行，数据已存在：(车场-车牌)"+comid+"-"+car_number;
								isValid = false;
							}
						}else{
							Long count = daService.getLong("select count(id) from carower_product where com_id =? and car_number=?  ",new Object[]{comid,car_number});
							if(count!=null&&count>0){
								//errmsg+=i+"行，数据已存在：(车场-车牌)"+comid+"-"+car_number;
								isValid = false;
								updateDatas.add(o);//加入到更新数据中
							}
							comCarMap.put(comid+car_number,count.intValue());
						}

						if(isValid){
							newdatas.add(o);//加入到插入数据中
						}
						if(!Check.isEmpty(errmsg)&&!errmsg.endsWith("</br>"))
							errmsg+="</br>";
					}
					i++;
				}
				if(!newdatas.isEmpty())//处理插入数据
					for(Object[] v : newdatas){
						System.out.println(StringUtils.objArry2String(v));
						//System.out.println(v.length);
						Long comid =Long.valueOf(v[0]+"");
						Long btime = ntime;
						if(!Check.isEmpty(v[1]+"")){
							btime = TimeTools.getLongMilliSecondFrom_HHMMDD(v[1]+"")/1000;
						}
						Long etime = ntime;
						if(!Check.isEmpty(v[2]+""))
							etime = TimeTools.getLongMilliSecondFrom_HHMMDD(v[2]+"")/1000+86399;
						Double total = StringUtils.formatDouble(v[5]);
						Long id = daService.getkey("seq_carower_product");
						Object[] va = new Object[]{id,comid,System.currentTimeMillis()/1000,btime,etime,v[3],v[4],total,v[6],id+""};
						insertValues.add(va);
						Object[] syncVa = new Object[]{comid,"carower_product",id,ntime,0};
						syncValues.add(syncVa);

					}
				if(!updateDatas.isEmpty()){//处理更新数据
					for(Object[] o : updateDatas){
						System.out.println(StringUtils.objArry2String(o));
						Long comid =Long.valueOf(o[0]+"");
						Long btime = ntime;
						if(!Check.isEmpty(o[1]+"")){
							btime = TimeTools.getLongMilliSecondFrom_HHMMDD(o[1]+"")/1000;
						}
						Long etime = ntime;
						if(!Check.isEmpty(o[2]+""))
							etime = TimeTools.getLongMilliSecondFrom_HHMMDD(o[2]+"")/1000+86399;
						Double total = StringUtils.formatDouble(o[5]);
						Object[] va = new Object[]{btime,etime,o[3],o[4],total,comid,o[6]};
						updateValues.add(va);
						Long id = daService.getLong("select id from carower_product where com_id=? and car_number=? ",new Object[]{comid,o[6]});
						Object[] syncVa = new Object[]{comid,"carower_product",id,ntime,1};
						syncValues.add(syncVa);
					}
				}
			}
			if(!insertValues.isEmpty()) {
				int r = daService.bathInsert(insertsql, insertValues, insettypes);
				logger.error("批量导入月卡结果：" + r);
				errmsg +="</br>新建"+r+"条";
			}
			if(!updateValues.isEmpty()) {
				int r = daService.bathInsert(updateSql, updateValues, updateTypes);
				logger.error("批量更新月卡结果：" + r);
				errmsg +="</br>更新"+r+"条";
			}
			if(!syncValues.isEmpty()){
				int e = daService.bathInsert(syncSql,syncValues,syncTypes);
				logger.error("批量下发："+e);
				errmsg +="</br>下发"+e+"条";
			}
			logger.error(errmsg);
//			if(Check.isEmpty(errmsg))
//				errmsg
		}
		return errmsg;
	}

	private List query(HttpServletRequest request,long cityid,Long grouId){
		Long comid = RequestUtil.getLong(request,"comid",-1L);
		List parks =null;
		if(comid>0){
			parks = new ArrayList();
			parks.add(comid);
		}else if(grouId!=null&&grouId>0)
			parks =commonMethods.getParks(grouId) ;
		else if(cityid>0){
			parks =commonMethods.getparks(cityid) ;
		}
		List<Object> params = new ArrayList<Object>();
		params.add(0);
		String sql = "select * from carower_product c where  is_delete =?  ";
		String countSql = "select count(*)   from carower_product c where   is_delete =? ";
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
		String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
		SqlInfo sqlInfo = RequestUtil.customSearch(request, "c_product", "c", null);
		//	SqlInfo ssqlInfo = getSuperSqlInfo(request);
		if(sqlInfo!=null) {
			/*if(ssqlInfo!=null)
				sqlInfo = SqlInfo.joinSqlInfo(sqlInfo,ssqlInfo, 2);*/
			countSql += " and " + sqlInfo.getSql();
			sql += " and " + sqlInfo.getSql();
			params.addAll(sqlInfo.getParams());
		/*else if(ssqlInfo!=null){
			countSql+=" and "+ ssqlInfo.getSql();
			sql +=" and "+ssqlInfo.getSql();
			params= ssqlInfo.getParams();
		}*/
		}
		boolean isHavePark = false;
		if(parks != null && !parks.isEmpty()){
			isHavePark = true;
			String preParams  ="";
			for(Object parkid : parks){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			sql += " and  com_id in ("+preParams+") ";
			countSql += " and com_id in ("+preParams+") ";
			params.addAll(parks);
		}
			//System.out.println(sqlInfo);
		logger.error(sql);
		logger.error(params);
		Long count= 0L;
		if(isHavePark){
			count=daService.getCount(countSql, params);
		}
		List list = null;//daService.getPage(sql, null, 1, 20);
		if(count>0){
			String orderby="id";
			String sort="desc";
			String orderfield = RequestUtil.processParams(request, "orderfield");
			String reqorderby = RequestUtil.processParams(request, "orderby");
			if(StringUtils.isNotNull(orderfield))
				orderby=orderfield;
			if(StringUtils.isNotNull(orderfield))
				sort=reqorderby;
			list = daService.getAll(sql+" order by "+orderby+" "+sort, params, pageNum, pageSize);

		}

		List<Object> arrayList = new ArrayList<Object>();
		arrayList.add(list);
		arrayList.add(pageNum);
		arrayList.add(count);
		arrayList.add(fieldsstr);
		return arrayList;
	}
	private void setList(List<Map<String,Object>> list){
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				Long b_time = (Long)map.get("b_time");
				Long e_time = (Long)map.get("e_time");
				Integer months = Math.round((e_time - b_time)/(30*24*60*60));
				map.put("months", months);
			}
		}
	}

	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		String car_nubmer = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "car_number"));
		SqlInfo sqlInfo1 = null;
		if(!car_nubmer.equals("")){
			sqlInfo1 = new SqlInfo(" v.uin in (select uin from car_info_tb where car_number like ?)  ",new Object[]{"%"+car_nubmer+"%"});
		}
		return sqlInfo1;
	}

	private int editUser(HttpServletRequest request){
		Long uin = RequestUtil.getLong(request, "uin", -1L);
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname"));
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		int r = daService.update("update user_info_tb set nickname=?,address=? where id=? ",
				new Object[]{name, address, uin});
		return r;
	}

	/*//注册包月会员
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String buyProduct(HttpServletRequest request){
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		String coms = request.getParameter("comid");
		//包月产品
		Long pid =RequestUtil.getLong(request, "p_name",-1L);
		//车主手机
		String mobile =RequestUtil.processParams(request, "mobile").trim();
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname").trim());
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address").trim());
		//起始时间
		String b_time =RequestUtil.processParams(request, "b_time");
		//购买月数
		Integer months = RequestUtil.getInteger(request, "months", 1);
		//备注
		String remark = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));
		//停车位编号
		String p_lot = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "p_lot")).trim();
		//实收金额
		String acttotal = RequestUtil.processParams(request, "act_total");

		Long ntime = System.currentTimeMillis()/1000;
		Long btime = TimeTools.getLongMilliSecondFrom_HHMMDD(b_time)/1000;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(btime*1000);
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+months);
		Long etime = calendar.getTimeInMillis()/1000;

		if(comid == -1 || pid == -1){
			return "-1";
		}

		//金额
		Double total= commonMethods.getProdSum(pid, months);

		Map<String, Object> pMap = daService.getMap("select limitday,price from product_package_tb where id=? ",
				new Object[]{pid});
		if(pMap != null && pMap.get("limitday") != null){
			Long limitDay = (Long)pMap.get("limitday");
			if(limitDay<etime){//超出有效期
				return "-2";
			}
		}

		Double act_total = total;
		if(!acttotal.equals("")){
			act_total = Double.valueOf(acttotal);
		}

		Map<String, Object> userMap = daService.getMap("select id from user_info_Tb where mobile=? and auth_flag=? ",
				new Object[]{mobile,4});

		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		Map<String, Object> userSqlMap = new HashMap<String, Object>();
		Map<String, Object> recomSqlMap = new HashMap<String, Object>();
		Map<String, Object> carowerPackMap = new HashMap<String, Object>();
		if(name.equals("")){
			name = "车主";
		}
		Long uin =-1L;
		if(userMap==null){//车主未注册
			uin = daService.getkey("seq_user_info_tb");
			userSqlMap.put("sql", "insert into user_info_tb (id,strid,nickname,mobile,auth_flag,reg_time,media,address) values(?,?,?,?,?,?,?,?)");
			userSqlMap.put("values", new Object[]{uin,"carower_"+uin,name,mobile,4,ntime,10,address});
			bathSql.add(userSqlMap);

			//写入记录表，用户通过注册月卡会员注册车主
			recomSqlMap.put("sql", "insert into recommend_tb(nid,type,state,create_time,comid) values(?,?,?,?,?)");
			recomSqlMap.put("values", new Object[]{uin,0,0,System.currentTimeMillis(),comid});
			bathSql.add(recomSqlMap);
		}else {
			userSqlMap.put("sql", "update user_info_tb set nickname=?,address=? where mobile=? and auth_flag=? ");
			userSqlMap.put("values", new Object[]{name,address,mobile,4});
			bathSql.add(userSqlMap);
			uin = (Long)userMap.get("id");
		}
		if(uin == null || uin == -1)
			return "-1";

		String result = commonMethods.checkplot(comid, p_lot, btime, etime, -1L);
		if(result != null){
			return result;
		}

		Long nextid = daService.getkey("seq_carower_product");
		carowerPackMap.put("sql", "insert into carower_product (id,uin,pid,create_time,b_time,e_time,total,remark,name,address,p_lot,act_total) values(?,?,?,?,?,?,?,?,?,?,?,?)");
		carowerPackMap.put("values", new Object[]{nextid,uin,pid,ntime,btime,etime,total,remark,name,address,p_lot,act_total});
		bathSql.add(carowerPackMap);
		if(daService.bathUpdate(bathSql)){
			String operater = request.getSession().getAttribute("loginuin")+"";
			if(publicMethods.isEtcPark(comid)){
//					if(f){
				int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"user_info_tb",uin,System.currentTimeMillis()/1000,0});
				if(uin>-1){
					List list = daService.getAll("select id from car_info_tb where uin = ?", new Object[]{uin});
					for (Object obj : list) {
						Map map = (Map)obj;
						Long carid = Long.parseLong(map.get("id")+"");
						if(carid!=null&&carid>0){
							daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_info_tb",carid,System.currentTimeMillis()/1000,0});
						}
					}
				}
//					}
				int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"carower_product",nextid,System.currentTimeMillis()/1000,0});
			}
			mongoDbUtils.saveLogs( request,0, 2, "车主"+mobile+"购买了套餐（编号："+pid+"）,金额："+act_total);
			return "1";
		}else {
			return "-1";
		}
	}*/
	//注册包月会员,支持同时选择多个车场，但不能绑定月卡套餐 20171011，guanghuitong
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String buyProduct(HttpServletRequest request,Long groupid){
		//Long comid = RequestUtil.getLong(request, "comid", -1L);
		String[] coms = request.getParameterMap().get("comid");
		boolean isAll=false;//是否添加到所有车场
		if(coms!=null&&coms.length>0){
			for(String c : coms){
				if(c.equals("-1")){
					isAll = true;
					break;
				}
			}
		}
		//包月产品
		Long pid =RequestUtil.getLong(request, "p_name",-1L);
		//车主手机
		String mobile =RequestUtil.processParams(request, "mobile").trim();
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname").trim());
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address").trim());
		//起始时间
		String b_time =RequestUtil.processParams(request, "b_time");
		String e_time =RequestUtil.processParams(request, "e_time");
		//购买月数
//		Integer months = RequestUtil.getInteger(request, "months", 1);
		//备注
		String remark = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));
		//停车位编号
		String p_lot = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "p_lot")).trim();
		//实收金额
		String acttotal = RequestUtil.processParams(request, "act_total");

		String car_number = AjaxUtil.decodeUTF8(RequestUtil.processParams(request,"car_number"));

		Long ntime = System.currentTimeMillis()/1000;
		Long btime = TimeTools.getLongMilliSecondFrom_HHMMDD(b_time)/1000;
//		Calendar calendar = Calendar.getInstance();
//		calendar.setTimeInMillis(btime*1000);
//		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+months);

		Long etime = TimeTools.getLongMilliSecondFrom_HHMMDD(e_time)/1000+86399;

//		if(!isAll&&(comid == -1 || pid == -1)){
//			return "-1";
//		}

		//金额
		Double total=StringUtils.formatDouble(acttotal);//commonMethods.getProdSum(pid, months);
		Double act_total = total;
		/*Map<String, Object> pMap = daService.getMap("select limitday,price from product_package_tb where id=? ",
				new Object[]{pid});
		if(pMap != null && pMap.get("limitday") != null){
			Long limitDay = (Long)pMap.get("limitday");
			if(limitDay<etime){//超出有效期
				return "-2";
			}
		}


		if(!acttotal.equals("")){
			act_total = Double.valueOf(acttotal);
		}*/

		Map<String, Object> userMap = daService.getMap("select id from user_info_Tb where mobile=? and auth_flag=? ",
				new Object[]{mobile,4});

		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		Map<String, Object> userSqlMap = new HashMap<String, Object>();
		Map<String, Object> recomSqlMap = new HashMap<String, Object>();
		Map<String, Object> carowerPackMap = new HashMap<String, Object>();
		if(name.equals("")){
			name = "车主";
		}
		Long uin =-1L;
		if(userMap==null){//车主未注册
			uin = daService.getkey("seq_user_info_tb");
			userSqlMap.put("sql", "insert into user_info_tb (id,strid,nickname,mobile,auth_flag,reg_time,media,address) values(?,?,?,?,?,?,?,?)");
			userSqlMap.put("values", new Object[]{uin,"carower_"+uin,name,mobile,4,ntime,10,address});
			bathSql.add(userSqlMap);

			//写入记录表，用户通过注册月卡会员注册车主
			/*recomSqlMap.put("sql", "insert into recommend_tb(nid,type,state,create_time,comid) values(?,?,?,?,?)");
			recomSqlMap.put("values", new Object[]{uin,0,0,System.currentTimeMillis(),comid});
			bathSql.add(recomSqlMap);*/
		}else {
			userSqlMap.put("sql", "update user_info_tb set nickname=?,address=? where mobile=? and auth_flag=? ");
			userSqlMap.put("values", new Object[]{name,address,mobile,4});
			bathSql.add(userSqlMap);
			uin = (Long)userMap.get("id");
		}
		if(uin == null || uin == -1)
			return "-1";

		/*String result = commonMethods.checkplot(comid, p_lot, btime, etime, -1L);
		if(result != null){
			return result;
		}*/
		logger.error("注册用户："+daService.bathUpdate(bathSql));
		List<Long> comids = new ArrayList<>();

		if(coms.length>1){
			for(String s : coms){
				if(!"-1".equals(s)&&Check.isLong(s))
					comids.add(Long.valueOf(s));
			}
		}
		if(comids.isEmpty()&&isAll){
			List<Object> groupComids = commonMethods.getParks(groupid);
			if(groupComids!=null&&!groupComids.isEmpty()){
				for(Object s : groupComids){
					if(s!=null&&!"-1".equals(s.toString())&&Check.isLong(s+"")){
						comids.add(Long.valueOf(s+""));
					}
				}
			}
		}
		if(!comids.isEmpty()){
			int i = 0;
			for(Long comid : comids){
				bathSql.clear();
				Long nextid = daService.getkey("seq_carower_product");
				carowerPackMap.put("sql", "insert into carower_product (id,com_id,uin,pid,create_time,b_time,e_time,total,remark,name,address,p_lot,act_total,car_number,card_id) " +
						"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				carowerPackMap.put("values", new Object[]{nextid,comid,uin,pid,ntime,btime,etime,total,remark,name,address,p_lot,act_total,car_number,nextid+""});
				bathSql.add(carowerPackMap);
				if(daService.bathUpdate(bathSql)){
					String operater = request.getSession().getAttribute("loginuin")+"";
					if(publicMethods.isEtcPark(comid)){
//					if(f){
						int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"user_info_tb",uin,System.currentTimeMillis()/1000,0});
						if(uin>-1){
							List list = daService.getAll("select id from car_info_tb where uin = ?", new Object[]{uin});
							for (Object obj : list) {
								Map map = (Map)obj;
								Long carid = Long.parseLong(map.get("id")+"");
								if(carid!=null&&carid>0){
									daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_info_tb",carid,System.currentTimeMillis()/1000,0});
								}
							}
						}
//					}
						int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"carower_product",nextid,System.currentTimeMillis()/1000,0});
					}
					mongoDbUtils.saveLogs( request,0, 2, "车主"+mobile+"购买了套餐（编号："+pid+"）,金额："+act_total);
					i++;
				}
			}
			return i+"";
		}else {
			return "-1";
		}

	}
}
