package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.CustomDefind;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
/**
 * 停车场后台会员，包月卡用户
 *
 * @author Administrator
 */

/**
 * @author yamiao
 * @date 2017-7-3
 */
public class VipManageAction extends Action {

    @Autowired
    private DataBaseService daService;
    @Autowired
    private PublicMethods publicMethods;
    @Autowired
    private CommonMethods commonMethods;
    @Autowired
    private MongoDbUtils mongoDbUtils;
    @Autowired
    private PgOnlyReadService pService;

    private Logger logger = Logger.getLogger(VipManageAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String action = RequestUtil.processParams(request, "action");
        Long comid = (Long) request.getSession().getAttribute("comid");
        Integer role = RequestUtil.getInteger(request, "role", -1);
        String operater = request.getSession().getAttribute("loginuin") + "";
        Long groupid = (Long) request.getSession().getAttribute("groupid");
        request.setAttribute("role", role);
        request.setAttribute("authid", request.getParameter("authid"));
        if (comid == null) {
            response.sendRedirect("login.do");
            return null;
        }
        String uid = request.getSession().getAttribute("userid") + "";
        if (comid == 0)
            comid = RequestUtil.getLong(request, "comid", 0L);
        if (action.equals("")) {
            String firstprovince = "";
            if (groupid != null && groupid > 0) {//集团管理员登录
                request.setAttribute("groupid", groupid);
                if (comid == null || comid == 0) {
                    Map map = daService.getMap("select id,company_name,firstprovince from com_info_tb where groupid=? order by id limit ? ",
                            new Object[]{groupid, 1});
                    if (map != null) {
                        comid = (Long) map.get("id");
                        firstprovince = (String) map.get("firstprovince");
                    } else {
                        comid = -999L;
                    }
                }
            } else {
                Map map = daService.getMap("select firstprovince from com_info_tb where  id = ? ",
                        new Object[]{comid});
                if (map != null) {
                    firstprovince = (String) map.get("firstprovince");
                }
            }
            request.setAttribute("comid", comid);
            request.setAttribute("btime", TimeTools.getTimeStr_yyyy_MM_dd(System.currentTimeMillis()));
            request.setAttribute("firstprovince", firstprovince);
            request.setAttribute("total", 0.0);
            request.setAttribute("act_total", 0.0);
            return mapping.findForward("list");
        } else if (action.equals("query")) {
            List arrayList = query(request, comid);
            List list = (List<Map<String, Object>>) arrayList.get(0);
            Integer pageNum = (Integer) arrayList.get(1);
            long count = Long.valueOf(arrayList.get(2) + "");
            String fieldsstr = arrayList.get(3) + "";
            setList(list);
            String json = JsonUtil.Map2Json(list, pageNum, count, fieldsstr, "id");
            AjaxUtil.ajaxOutput(response, json);
            return null;
        } else if (action.equals("exportExcel")) {
            Map uin = (Map) request.getSession().getAttribute("userinfo");
            if (uin != null && uin.get("auth_flag") != null) {
                if (Integer.valueOf(uin.get("auth_flag") + "") == ZLDType.ZLD_ACCOUNTANT_ROLE || Integer.valueOf(uin.get("auth_flag") + "") == ZLDType.ZLD_CARDOPERATOR) {
                    String ret = "没有权限导出会员数据";
                    logger.error(">>>>" + ret);
                    AjaxUtil.ajaxOutput(response, ret);
                    return null;
                }
            }
            List arrayList = query(request, comid);
            List<Map<String, Object>> list = (List<Map<String, Object>>) arrayList.get(0);
            List<List<String>> bodyList = new ArrayList<List<String>>();
            String[] heards = null;
            if (list != null && list.size() > 0) {
                mongoDbUtils.saveLogs(request, 0, 5, "导出会员数量：" + list.size());
                //setComName(list);
                String[] f = new String[]{"id", "p_name", "mobile"/*,"uin"*/, "name", "car_number", "create_time", "b_time", "e_time", "total", "car_type_id", "limit_day_type", "remark"};
                heards = new String[]{"编号", "包月产品名称", "车主手机"/*,"车主账户"*/, "名字", "车牌号码", "购买时间", "开始时间", "结束时间", "金额", "车型类型", "单双日限行", "备注"};
                for (Map<String, Object> map : list) {
                    List<String> values = new ArrayList<String>();
                    for (String field : f) {
//						if("car_number".equals(field)){
//							if(map.get(3)!= null && !"".equals(map.get(3))){
//								values.add(commonMethods.getcar(Long.parseLong(map.get(3)+"")));
//							}else{
//								values.add("");
//							}
//							//values.add(commonMethods.getcar(Long.valueOf(values.get(3)+"")));
//						}
                        if ("p_name".equals(field)) {
                            if (map.get("pid") != null) {
                                Map cpMap = daService.getMap("select p_name from product_package_tb where id =? ", new Object[]{Long.parseLong(map.get("pid") + "")});
                                if (cpMap != null) {
                                    values.add(cpMap.get("p_name") + "");
                                } else {
                                    values.add("");
                                }
                            } else {
                                values.add("");
                            }
                        } else if ("car_type_id".equals(field)) {
                            if (map.get("car_type_id") != null) {
                                Map cpMap = null;
                                try {
                                    cpMap = daService.getMap("select name from car_type_tb where id =? ", new Object[]{Long.parseLong(map.get("car_type_id") + "")});
                                } catch (Exception e) {
                                    //cpMap = daService.getMap("select name from car_type_tb where id =?",new Object[]{-1L});
                                    values.add(map.get("car_type_id") + "");
                                }
                                if (cpMap != null) {
                                    values.add(cpMap.get("name") + "");
                                } else {
                                    values.add("");
                                }
                            } else {
                                values.add("");
                            }
                        } else if ("limit_day_type".equals(field)) {
                            if (map.get("limit_day_type") != null) {
                                if ((Integer) map.get("limit_day_type") == 0) {
                                    values.add("不限行");
                                } else if ((Integer) map.get("limit_day_type") == 1) {
                                    values.add("限行");
                                }
                            } else {
                                values.add("");
                            }
                        } else {
                            if ("create_time".equals(field) || "b_time".equals(field) || "e_time".equals(field)) {
                                if (map.get(field) != null) {
                                    values.add(TimeTools.getTime_yyyyMMdd_HHmmss(Long.valueOf((map.get(field) + "")) * 1000));
                                } else {
                                    values.add("");
                                }
                            } else {
                                values.add(map.get(field) + "");
                            }
                        }
                    }
                    bodyList.add(values);
                }
            }
            String fname = "会员数据" + com.zld.utils.TimeTools.getDate_YY_MM_DD();
            fname = StringUtils.encodingFileName(fname);
            java.io.OutputStream os;
            try {
                response.reset();
                response.setHeader("Content-disposition", "attachment; filename="
                        + fname + ".xls");
                response.setContentType("application/x-download");
                os = response.getOutputStream();
                ExportExcelUtil importExcel = new ExportExcelUtil("会员数据",
                        heards, bodyList);
                importExcel.createExcelFile(os);
            } catch (IOException e) {
                e.printStackTrace();
            }
//			String json = "";
//			AjaxUtil.ajaxOutput(response, json);
            return null;
        } else if (action.equals("addcar")) {
            String carNumber = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "carnumber"));
            Long id = RequestUtil.getLong(request, "id", -1L);
            /*
			 * 取消车主概念后，修改某一个月卡会员套餐中的车牌号码后
			 * 其他相同车牌的月卡会员套餐中的车牌需要同步修改过来
			 * 修改车牌后需要将对应的信息写入到sync_info_pool_tb表中
			 * 并通过SDK将修改后的车牌信息同步到车场收费系统
			 */
            //查询出当前月卡会员记录中原来对应的车牌号
            Map map = daService.getMap("select * from carower_product where id=?", new Object[]{id});
            String carNumberBefore = "";
            if (map != null && !map.isEmpty()) {
                carNumberBefore = String.valueOf(map.get("car_number"));
            }
            int ret = 0;
            if (carNumber != null && !carNumber.equals("")) {
                if (!carNumber.equals(carNumberBefore)) {
                    //对修改车牌的逻辑加一层校验，验证车牌是否有效
                    //添加修改月卡会员车牌时的车主编号
                    Long uin = -1L;
                    String[] carNumStrings = carNumber.split(",");
                    Long validuin = -1L;
                    String plot = "";
                    if (carNumStrings != null && carNumStrings.length > 0) {
                        for (String strNum : carNumStrings) {
                            strNum.toUpperCase();
                            if (StringUtils.checkPlate(strNum)) {
                                Map mapCarUin = daService.getMap("select uin from car_info_tb where car_number=? ", new Object[]{strNum});
                                if (mapCarUin != null && !mapCarUin.isEmpty()) {
                                    uin = Long.valueOf(String.valueOf(mapCarUin.get("uin")));
                                }
                                if (uin > 0) {
                                    validuin = uin;
                                }
                                //修改或添加车牌时查询此车牌是否已经对应有月卡会员记录
                                String subCar = strNum.startsWith("无") ? strNum : "%" + strNum.substring(1) + "%";
                                List<Map> carinfoList = daService.getAll("select pid, car_number from  carower_product where com_id=? and is_delete=? and car_number like ?", new Object[]{comid, 0, subCar});
                                Long pid = -1L;
                                boolean isMonthUser = false;
                                //定义月卡会员记录中对应的车牌
                                String carNumUnique = "";
                                if (carinfoList != null && !carinfoList.isEmpty() && carinfoList.size() > 0) {
                                    for (Iterator iterator = carinfoList.iterator(); iterator
                                            .hasNext(); ) {
                                        Map uinmap = (Map) iterator.next();
                                        pid = (Long) uinmap.get("pid");
                                        carNumUnique = String.valueOf(uinmap.get("car_number"));
                                        isMonthUser = publicMethods.isMonthUserNew(pid, comid, carNumUnique);
                                        boolean isSameCarNumber = isCarNumberSame(carNumber, carNumUnique);
                                        if (isMonthUser && isSameCarNumber) {
                                            isMonthUser = false;
                                        } else if (isMonthUser && carinfoList.size() < 2) {
                                            isMonthUser = false;
                                        }
                                    }
                                    if (isMonthUser) {
                                        AjaxUtil.ajaxOutput(response, " 该车牌:" + carNumberBefore + "已注册为月卡会员,请修改车牌！");
                                        return null;
                                    }
                                }
                            } else {
                                AjaxUtil.ajaxOutput(response, "车牌号错误");
                                return null;
                            }
                        }
                        uin = validuin;
                    }
                    if (id > 0 && carNumber.length() > 6) {
                        ret = daService.update("update carower_product set car_number=?,uin=? where id=? ", new Object[]{carNumber.toUpperCase(), uin, id});
                    }
                    if (ret > 0) {
                        int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid, "carower_product", id, System.currentTimeMillis() / 1000, 1});
                        logger.error("parkadmin or admin:" + operater + " add comid:" + comid + " vipuser ,add sync ret:" + r);
                        List<Map> list = daService.getAll("select * from carower_product where car_number=? and com_id=? and is_delete=?", new Object[]{carNumberBefore, comid, 0});
                        for (Iterator iterator = list.iterator(); iterator.hasNext(); ) {
                            Map uinmap = (Map) iterator.next();
                            Long idLong = Long.valueOf(String.valueOf(uinmap.get("id")));
                            //根据查询出来对应的月卡会员id修改所有的车牌记录
                            int result = daService.update("update carower_product set car_number=? where id=?", new Object[]{carNumber.toUpperCase(), idLong});
                            if (result == 1) {
                                int resultUpdate = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)",
                                        new Object[]{comid, "carower_product", idLong, System.currentTimeMillis() / 1000, 1});
                                logger.error(">>>>>>>>>>>>>>新添加月卡会员修改记录id:" + idLong);
                            }
                            mongoDbUtils.saveLogs(request, 0, 3, "修改了车牌：" + carNumber);
                        }
                        mongoDbUtils.saveLogs(request, 0, 3, "修改了车牌：" + carNumber);
                    } else {
                        logger.error(">>>>>>>>>>>>>>>>对应的月卡会员车牌编号未修改成功，id：" + id);
                    }
                } else {
                    ret = 1;
                }
            } else {
                AjaxUtil.ajaxOutput(response, "修改车牌，车牌号不能为空");
                return null;
            }




			/*String []cars = new String[]{carNumber};
			if(carNumber.indexOf(",")!=-1){
				cars = carNumber.split(",");//做个限制，要不然出现一个账户下多个车牌
			}
			Long curTime = System.currentTimeMillis()/1000;
			if(cars.length>3){
				AjaxUtil.ajaxOutput(response, "每个账户最多绑定三个车牌");
				return null;
			}
			Long uin = RequestUtil.getLong(request, "uin", -1L);
			int ret = 0;
			if(carNumber!=null&&uin!=-1){
				//修改原进场车场是包月的,未结算的，改为普通进场
				//删除原车牌
//				try {
//					//优化下如果车牌号未变的话不做任何操作
//					List<String> list = daService.getAll("select car_number from car_info_tb where uin = ?",new Object[]{uin} );
//					HashSet<String> set = new HashSet<String>();
//					for (String str : list) {
//						set.add(str);
//					}
//					for (int i = 0; i < cars.length; i++) {
//						boolean b = set.add(cars[i]);
//						if(b==true){
//							int result = daService.update("update order_tb set c_type=? where uin=? and state=? and c_type=? ", new Object[]{3,uin,0,5});
//							logger.error("管理员uid："+uid+"为会员uin:"+uin+"添加车牌号："+carNumber+"，修改原进场车场是包月的,未结算的，改为普通进场记录条数："+result);
//							break;
//						}
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
				String sql = "insert into car_info_tb(uin,car_number,create_time) values(?,?,?)";
				Set<String> set = new HashSet<String>();
				List<Object[]> values = new ArrayList<Object[]>();
				for(String car :cars){
					car = car.toUpperCase();
					if(StringUtils.checkPlate(car)){
						set.add(car);
						Map<String, Object> map = daService.getMap("select uin from car_info_tb where car_number=? and uin<>?", new Object[]{car,uin});
						if(map!=null&&map.size()>0){
//							AjaxUtil.ajaxOutput(response, car+" 已在存在！");
//							return null;
							//如果账户没有登录记录，没有在其它车场有月卡信息，可以删除用户
							for (Map.Entry<String, Object> entry : map.entrySet()) {
//								System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
								int dret = deleteUser(comid,Long.parseLong(entry.getValue()+""),car);

								Map<String, Object> userMap = daService.getMap("select mobile from user_info_tb where id=? ", new Object[]{entry.getValue()});
								String mobile = "";
								if(userMap != null && userMap.get("mobile") != null){
									mobile = (String)userMap.get("mobile");
									mobile = mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4, mobile.length());
								}

								if(dret==-1){
									AjaxUtil.ajaxOutput(response, car+" 已被手机号"+mobile+"绑定,并且已购买了本车场包月产品");
									return null;
								}else if(dret==-2){
									AjaxUtil.ajaxOutput(response, car+" 已被手机号"+mobile+"绑定，请使用该手机号添加月卡");
									return null;
								}else if(dret==-3){
									AjaxUtil.ajaxOutput(response, car+" 车主删除失败");
									return null;
								}else if(dret==1){
									mongoDbUtils.saveLogs( request,0, 4, "删除无效车牌："+car);
								}
							}
						}
						Object []value = new Object[]{uin, car, curTime};
						values.add(value);
					}else{
						AjaxUtil.ajaxOutput(response,"车牌号错误");
						return null;
					}
				}
				if(cars.length!=set.size()){
					AjaxUtil.ajaxOutput(response,"车牌号重复");
					return null;
				}
				if(values.size()==0){
					AjaxUtil.ajaxOutput(response, "车牌号错误");
					return null;
				}
				List ids = daService.getAll("select id,car_number from car_info_tb where uin = ?", new Object[]{uin});
				StringBuffer sBuffer = new StringBuffer();
				for (Object obj : ids) {
					Map map = (Map)obj;
					Long id = Long.valueOf(map.get("id")+"");
					String car = map.get("car_number")+"";
					if(!set.contains(car)){
						Map map1 = daService.getMap("select * from order_tb where car_number = ? and state=? and comid = ?",new Object[]{car,0,comid});
						if(map1!=null){
							AjaxUtil.ajaxOutput(response,"车牌"+car+"车辆在场，请出场或0元结算后再修改车牌");
							return null;
						}
					}
				}
				for (Object obj : ids) {
					Map map = (Map)obj;
					Long id = Long.valueOf(map.get("id")+"");
					sBuffer.append(map.get("car_number"));
					int su = daService.update("delete from car_info_tb where id =? ", new Object[]{id});
					if(su>0){
						if(publicMethods.isEtcPark(comid)){
							int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_info_tb",id,System.currentTimeMillis()/1000,2});
							logger.error("parkadmin or admin:"+operater+" delete comid:"+comid+" car ,add sync ret:"+r);
						}
					}
				}
				if(!values.isEmpty()){
					try{
						ret=daService.bathInsert(sql, values, new int[]{4,12,4});
						logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" car   ret:"+ret);
						if(ret>0){
							List list = daService.getAll("select id from car_info_tb where uin = ?", new Object[]{uin});
							for (Object obj : list) {
								Map map = (Map)obj;
								Object id = map.get("id");
								if(publicMethods.isEtcPark(comid)){
									int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_info_tb",Long.valueOf(id+""),System.currentTimeMillis()/1000,0});
									logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" car ,add sync ret:"+r);
								}
							}
							mongoDbUtils.saveLogs( request,0, 3, "给车主（"+uin+"）修改了车牌："+sBuffer.toString()+"-->"+carNumber);
						}
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			}*/
            AjaxUtil.ajaxOutput(response, ret + "");
            return null;
        } else if (action.equals("create")) {
            String result = buyProduct(request, comid);
            if (result.equals("1")) {//短信通知车主
                //车主手机
				/*String mobile =RequestUtil.processParams(request, "mobile").trim();
				//车牌号码
				String car_number =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number")).toUpperCase();
				Map<String, Object> map = daService.getMap("select company_name from com_info_tb where id=? ", new Object[]{comid});
				String company_name = (String)map.get("company_name");
				String msg = "【重大喜讯】亲爱的"+company_name+"会员：为方便您的停靠，本车场重金引入停车宝，您的车牌号码【"+car_number+"】已录入系统，出入自动抬杆放行。手机下载停车宝APP，随时可更换车牌号，立改立生效，单双号限行也不怕。更可通过APP查看包月详情，办理续费，续费几个月您说了算。另送您会员专享停车券，用本手机号登录停车宝APP即可领取，全市225家车场通用哦！登录www.tingchebao.com火速下载，退订回N【停车宝】";
				//SendMessage.sendMultiMessage(mobile, msg);
*/
            }
            AjaxUtil.ajaxOutput(response, result);
        } else if (action.equals("edit")) {

            Long id = RequestUtil.getLong(request, "id", -1L);
            //车牌号码

            if (id == -1) {
                AjaxUtil.ajaxOutput(response, "-1");
                return null;
            }
            //Long count = daService.getLong("select count(*) from car_info_tb where car_number=? ", new Object[]{car_number});
            String result = editProduct(request, comid);
            //if(result.equals("1") && count == 0){
            //车主手机
//				String mobile =RequestUtil.processParams(request, "mobile").trim();
//				Map<String, Object> map = daService.getMap("select company_name from com_info_tb where id=? ", new Object[]{comid});
//				String company_name = (String)map.get("company_name");
//				String msg = "【重大喜讯】亲爱的"+company_name+"会员：为方便您的停靠，本车场重金引入停车宝，您的车牌号码【"+car_number+"】已录入系统，出入自动抬杆放行。手机下载停车宝APP，随时可更换车牌号，立改立生效，单双号限行也不怕。更可通过APP查看包月详情，办理续费，续费几个月您说了算。另送您会员专享停车券，用本手机号登录停车宝APP即可领取，全市225家车场通用哦！登录www.tingchebao.com火速下载，退订回N【停车宝】";
//				SendMessage.sendMultiMessage(mobile, msg);
            //	}
            AjaxUtil.ajaxOutput(response, result);
        } else if (action.equals("delete")) {
            String id = RequestUtil.processParams(request, "selids");
            String carNumber = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "car_number"));
			/*Map cpMap = daService.getMap("select uin from carower_product where id =? ",new Object[]{Long.valueOf(id)});

			Long uin =-1L;
			if(cpMap!=null)
				uin=(Long)cpMap.get("uin");*/

            int result = daService.update("update carower_product set is_delete =?  where id =?", new Object[]{1, Long.valueOf(id)});
            if (result == 1) {
                //if(publicMethods.isEtcPark(comid)){
                int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid, "carower_product", Long.valueOf(id), System.currentTimeMillis() / 1000, 2});
                logger.error("parkadmin or admin:" + operater + " delete comid:" + comid + " vipuser  ,add sync ret:" + r);
                //}else{
                logger.error("parkadmin or admin:" + operater + " delete comid:" + comid + " vipuser");
                //}
//				result = deleteUser(comid, uin);
                mongoDbUtils.saveLogs(request, 0, 4, "删除了车主（" + carNumber + "）的套餐");
            }
            AjaxUtil.ajaxOutput(response, result + "");
        } else if (action.equals("checkmobile")) {
            String mobile = RequestUtil.processParams(request, "mobile").trim();
            if (!Check.checkMobile(mobile)) {
                AjaxUtil.ajaxOutput(response, "1");
            } else {
                AjaxUtil.ajaxOutput(response, "0");
            }
        } else if (action.equals("getcar")) {
            Long uin = RequestUtil.getLong(request, "uin", -1L);
            String cars = commonMethods.getcar(uin);
            AjaxUtil.ajaxOutput(response, cars);
        } else if (action.equals("renew")) {
            String result = renewProduct(request, comid);
            AjaxUtil.ajaxOutput(response, result);
        }
        return null;
    }

    /**
     * 月卡会员续费实现方法
     * @param request
     * @param comid
     * @return
     */
    private String renewProduct(HttpServletRequest request, Long comid) {
        //包月产品
        Long pid = RequestUtil.getLong(request, "p_name", -1L);
        //车主手机
        String mobile = RequestUtil.processParams(request, "mobile").trim();
        String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name").trim());
        //String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address").trim());
        //起始时间
        String b_time = RequestUtil.processParams(request, "b_time");
        //购买月数
        Integer months = RequestUtil.getInteger(request, "months", 1);
        Long id = RequestUtil.getLong(request, "id", -1L);
        //修改月卡会员编号cardId为主键id
        //Long nextid = daService.getkey("seq_carower_product");
        //修改原来的月卡会员cardId生成及使用逻辑
        //String cardId = String.valueOf(nextid);
		/*Long count = daService.getLong("select count(ID) from carower_product where com_id=? and card_id=? and is_delete =?  ", new Object[]{comid,cardId,0});
		if(count>0){
			return "-3";
		}*/
        //Integer flag = RequestUtil.getInteger(request, "flag", -1);
        //备注
        String remark = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));
        //String carNumber = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number"));
        //实收金额
        String acttotal = RequestUtil.processParams(request, "act_total");

        Long ntime = System.currentTimeMillis() / 1000;
        Long btime = TimeTools.getLongMilliSecondFrom_HHMMDD(b_time) / 1000 + 86400;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(btime * 1000);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + months);
        Long etime = calendar.getTimeInMillis() / 1000 - 1;

        //金额
        Double total = StringUtils.formatDouble(commonMethods.getProdSum(pid, months));//RequestUtil.getDouble(request, "total", 0d);

        Map pMap = daService.getMap("select limitday,price from product_package_tb where id=? ", new Object[]{pid});
        Long limitDay = null;//pMap.get("limitday");
        if (pMap != null && pMap.get("limitday") != null) {
            limitDay = (Long) pMap.get("limitday");
        }
        if (limitDay != null) {
            if (limitDay < etime) {//超出有效期
                return "-2";
            }
        }

        Double act_total = total;
        if (!acttotal.equals("")) {
            act_total = Double.valueOf(acttotal);
        }
        Map<String, Object> carowerPack = daService.getMap("select * from carower_product where id = ?", new Object[]{id});
        List<Map<String, Object>> bathSql = new ArrayList<Map<String, Object>>();
        Map<String, Object> carowerPackMap = new HashMap<String, Object>();

        carowerPackMap.put("sql", "update carower_product set e_time =? where id =? ");
        carowerPackMap.put("values", new Object[]{etime, id});
        bathSql.add(carowerPackMap);

        Map<String, Object> reNewMap = new HashMap<String, Object>();
        reNewMap.put("sql", "insert into card_renew_tb (id,trade_no,card_id,pay_time,amount_receivable,amount_pay," +
                "collector,pay_type,car_number,user_id,resume,buy_month,comid,create_time,update_time,limit_time,start_time) values" +
                "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        Long renewId = daService.getkey("seq_card_renew_tb");
        String tradeNo = TimeTools.getTimeYYYYMMDDHHMMSS() + "" + comid;
        String operater = (String) request.getSession().getAttribute("nickname");
        reNewMap.put("values", new Object[]{renewId, tradeNo, carowerPack.get("card_id"), ntime, total + "", act_total + "", operater, "现金"
                , carowerPack.get("car_number"), name, remark, months, comid, ntime, ntime, etime, btime-86400});
        bathSql.add(reNewMap);

        if (daService.bathUpdate(bathSql)) {
            int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid, "carower_product", id, System.currentTimeMillis() / 1000, 1});
            logger.error("parkadmin or admin:" + operater + " add comid:" + comid + " vipuser ,add sync ret:" + r);
            daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid, "card_renew_tb", renewId, System.currentTimeMillis() / 1000, 0});
            logger.error("parkadmin or admin:" + operater + " add comid:" + comid + " vipuser ,add sync ret:" + r);
            //}
            mongoDbUtils.saveLogs(request, 0, 2, "车主" + mobile + "购买了套餐（编号：" + pid + "）,金额：" + act_total);
            return "1";
        } else {
            return "-1";
        }
    }

    /*private String renewProduct(HttpServletRequest request, Long comid){
        //包月产品
        Long pid =RequestUtil.getLong(request, "p_name",-1L);
        //车主手机
        String mobile =RequestUtil.processParams(request, "mobile").trim();
        String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name").trim());
        String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address").trim());
        //起始时间
        String b_time =RequestUtil.processParams(request, "b_time");
        //购买月数
        Integer months = RequestUtil.getInteger(request, "months", 1);
        //修改月卡会员编号cardId为主键id
        Long nextid = daService.getkey("seq_carower_product");
        //修改原来的月卡会员cardId生成及使用逻辑
        String cardId = String.valueOf(nextid);
        Long count = daService.getLong("select count(ID) from carower_product where com_id=? and card_id=? and is_delete =?  ", new Object[]{comid,cardId,0});
        if(count>0){
            return "-3";
        }
        Integer flag = RequestUtil.getInteger(request, "flag", -1);
        //备注
        String remark = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));
        String carNumber = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number"));
        //实收金额
        String acttotal = RequestUtil.processParams(request, "act_total");

        Long ntime = System.currentTimeMillis()/1000;
        Long btime = TimeTools.getLongMilliSecondFrom_HHMMDD(b_time)/1000+86400;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(btime*1000);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+months);
        Long etime = calendar.getTimeInMillis()/1000-1;

        //金额
        Double total= commonMethods.getProdSum(pid, months);//RequestUtil.getDouble(request, "total", 0d);

        Map pMap = daService.getMap("select limitday,price from product_package_tb where id=? ", new Object[]{pid});
        Long limitDay = null;//pMap.get("limitday");
        if(pMap!=null&&pMap.get("limitday")!=null){
            limitDay = (Long)pMap.get("limitday");
        }
        if(limitDay!=null){
            if(limitDay<etime){//超出有效期
                return "-2";
            }
        }

        Double act_total = total;
        if(!acttotal.equals("")){
            act_total = Double.valueOf(acttotal);
        }
         Long uin =-1L;
         //添加生成月卡会员时的车主编号
         if(carNumber != null && !carNumber.equals("")){
             String [] carNumStrings = carNumber.split(",");
             Long validuin = -1L;
             if(carNumStrings != null && carNumStrings.length>0){
                 for(String strNum :carNumStrings){
                     strNum.toUpperCase();
                     if(StringUtils.checkPlate(strNum)){
                         Map mapCarUin = daService.getMap("select uin from car_info_tb where car_number=? ", new Object[]{strNum});
                         if(mapCarUin != null && !mapCarUin.isEmpty()){
                             uin = Long.valueOf(String.valueOf(mapCarUin.get("uin")));
                         }
                         if(uin>0){
                             validuin = uin;
                         }
                        //修改或添加车牌时查询此车牌是否已经对应有月卡会员记录
                        String subCar = strNum.startsWith("无")?strNum:"%"+strNum.substring(1)+"%";
                        List<Map> carinfoList = daService.getAll("select pid,car_number from  carower_product where com_id=? and is_delete=? and car_number like ?", new Object[]{comid,0,subCar});
                        Long pidMonth = -1L;
                        boolean isMonthUser = false;
                        if(carinfoList != null && !carinfoList.isEmpty() && carinfoList.size()>0){
                            for (Iterator iterator = carinfoList.iterator(); iterator
                                    .hasNext();) {
                                Map uinmap = (Map) iterator.next();
                                pidMonth = (Long)uinmap.get("pid");
                                String carNumUnique = String.valueOf(uinmap.get("car_number"));
                                isMonthUser = publicMethods.isMonthUserNew(pidMonth,comid,carNumUnique);

                                 * 假如改车牌已经注册过月卡会员，车牌一致时才能继续添加，否则提示已注册
                                 * 其中A,B和B,A也代表车牌是一致的

                                boolean isSameCarNumber = isCarNumberSame(carNumber, carNumUnique);
                                if(isMonthUser && isSameCarNumber){
                                    isMonthUser = false;
                                    carNumber = carNumUnique;
                                }
                            }
                            if(isMonthUser){
                                return "车牌"+carNumber+"已注册为月卡会员,请修改车牌！";
                            }
                        }
                     }else{
                         return "车牌号错误";
                     }
                 }
                 uin = validuin;
             }
         }
        List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
        Map<String, Object> carowerPackMap = new HashMap<String, Object>();

        carowerPackMap.put("sql", "insert into carower_product " +
                "(id,uin,pid,create_time,update_time,b_time,e_time,total,remark,name," +
                "address,act_total,com_id,mobile,car_number,card_id)" +
                " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        carowerPackMap.put("values", new Object[]{nextid,uin,pid,ntime,ntime,
                btime,etime,total,remark,name,address,act_total,comid,mobile,carNumber,String.valueOf(nextid)});
        bathSql.add(carowerPackMap);
        if(daService.bathUpdate(bathSql)){
            String operater = request.getSession().getAttribute("loginuin")+"";
                int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"carower_product",nextid,System.currentTimeMillis()/1000,0});
                logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" vipuser ,add sync ret:"+r);
            //}
            mongoDbUtils.saveLogs( request,0, 2, "车主"+mobile+"购买了套餐（编号："+pid+"）,金额："+act_total);
            return "1";
        }else {
            return "-1";
        }
    }*/
    //如果账户没有登录记录，没用绑定微信，没有在其它车场有月卡信息，可以删除用户
    private int deleteUser(HttpServletRequest request, long comid, Long uin, String carnumber) {
        //查是否在其它车场购买了包月产品
        String selids = RequestUtil.getString(request, "selids");

        Map userMap = daService.getMap("select balance,logon_time,wxp_openid from user_info_tb where id =?", new Object[]{uin});
        if (userMap != null) {
            Double balance = StringUtils.formatDouble(userMap.get("balance"));
            Long logoTime = (Long) userMap.get("logon_time");
            String wxp_openid = userMap.get("wxp_openid") + "";
            if (logoTime != null || balance > 0 || (wxp_openid != null && !wxp_openid.equals("") && !wxp_openid.equals("null"))) {//不可以删除用户
                return -2;
            }
            //开始删除
            List<Map<String, Object>> bathSql = new ArrayList<Map<String, Object>>();

            //删除用户信息
            Map<String, Object> userSqlMap = new HashMap<String, Object>();
            List<Map> ids = daService.getAll("select id from car_info_tb where uin = ? and car_number=?", new Object[]{uin, carnumber});
            for (Map map : ids) {
                //删除车牌信息
                long id = Long.parseLong(map.get("id") + "");
                Map<String, Object> carSqlMap = new HashMap<String, Object>();
                carSqlMap.put("sql", "delete from car_info_tb where id = ?");
                carSqlMap.put("values", new Object[]{id});
                bathSql.add(carSqlMap);
            }
//			userSqlMap.put("sql", "delete from user_info_tb where id = ?");
//			userSqlMap.put("values", new Object[]{uin});
//			bathSql.add(userSqlMap);
            boolean ret = daService.bathUpdate(bathSql);
            if (!ret)
                return -3;
            else {
                if (publicMethods.isEtcPark(comid)) {
                    for (Map map : ids) {
                        long id = Long.parseLong(map.get("id") + "");
                        int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid, "car_info_tb", id, System.currentTimeMillis() / 1000, 2});
                        logger.error("parkadmin or admin delete carnumber uin:" + uin + " ,add sync ret:" + re);
                    }
                }
            }
        }

        return 1;
    }

    /*//如果账户没有登录记录，没用绑定微信，没有在其它车场有月卡信息，可以删除用户
        private int deleteUser(long comid,Long uin,String carnumber) {
            //查是否在其它车场购买了包月产品
            List<Map<String,Long>> list = daService.getAll("select p.comid from carower_product c,product_package_tb p where c.uin=? and c.e_time >? and p.id = c.pid ",
                    new Object[]{uin,System.currentTimeMillis()/1000});
            if(list!=null&&list.size()>0){
                for(Map<String,Long> map:list){
                    if(map.get("comid").longValue()==comid)
                        return -1;
                }
                return -2;
            }
            Map userMap = daService.getMap("select balance,logon_time,wxp_openid from user_info_tb where id =?", new Object[]{uin});
            if(userMap!=null){
                Double balance = StringUtils.formatDouble(userMap.get("balance"));
                Long logoTime = (Long)userMap.get("logon_time");
                String wxp_openid = userMap.get("wxp_openid")+"";
                if(logoTime!=null||balance>0||(wxp_openid!=null&&!wxp_openid.equals("")&&!wxp_openid.equals("null"))){//不可以删除用户
                    return -2;
                }
                //开始删除
                List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();

                //删除用户信息
                Map<String, Object> userSqlMap = new HashMap<String, Object>();
                List<Map> ids = daService.getAll("select id from car_info_tb where uin = ? and car_number=?", new Object[]{uin,carnumber});
                for (Map map : ids) {
                    //删除车牌信息
                    long id = Long.parseLong(map.get("id")+"");
                    Map<String, Object> carSqlMap = new HashMap<String, Object>();
                    carSqlMap.put("sql", "delete from car_info_tb where id = ?");
                    carSqlMap.put("values", new Object[]{id});
                    bathSql.add(carSqlMap);
                }
//				userSqlMap.put("sql", "delete from user_info_tb where id = ?");
//				userSqlMap.put("values", new Object[]{uin});
//				bathSql.add(userSqlMap);
                boolean ret = daService.bathUpdate(bathSql);
                if(!ret)
                    return -3;
                else{
                    if (publicMethods.isEtcPark(comid)) {
                        for (Map map : ids) {
                            long id = Long.parseLong(map.get("id")+"");
                            int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_info_tb",id,System.currentTimeMillis()/1000,2});
                            logger.error("parkadmin or admin delete carnumber uin:"+uin+" ,add sync ret:"+re);
                        }
                    }
                }
            }

            return 1;
        }*/
    //注册包月会员
    private String buyProduct(HttpServletRequest request, Long comid) {
        //包月产品
        Long pid = RequestUtil.getLong(request, "p_name", -1L);
        //车主手机
        String mobile = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "mobile").trim());
        String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name").trim());
        String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address").trim());
        //车牌号码
        //String car_number =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number")).toUpperCase();
        //起始时间
        String b_time = RequestUtil.processParams(request, "b_time");
        //购买月数
        Integer months = RequestUtil.getInteger(request, "months", 1);
        //修改月卡会员编号cardId为主键id
        Long nextid = daService.getkey("seq_carower_product");
        //修改原来的月卡会员cardId生成及使用逻辑
//		String cardId =RequestUtil.getString(request, "card_id");
        String cardId = String.valueOf(nextid);
        Long count = daService.getLong("select count(ID) from carower_product where com_id=? and card_id=? and is_delete =?  ", new Object[]{comid, cardId, 0});
        if (count > 0) {
            return "-3";
        }
        Integer flag = RequestUtil.getInteger(request, "flag", -1);
        //备注
        String remark = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));
        String carNumber = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number"));
        //停车位编号
        //String p_lot = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "p_lot")).trim();
        //对停车位的编号添加新的逻辑处理，
		/*if(p_lot==null || p_lot.equals("") || p_lot.equals("null")){
			p_lot = "-1";
		}*/
        //实收金额
        String acttotal = RequestUtil.processParams(request, "act_total");

        Long ntime = System.currentTimeMillis() / 1000;
        Long btime = TimeTools.getLongMilliSecondFrom_HHMMDD(b_time) / 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(btime * 1000);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + months);
        Long etime = calendar.getTimeInMillis() / 1000 - 1;

        //金额
        Double total = StringUtils.formatDouble(commonMethods.getProdSum(pid, months));//RequestUtil.getDouble(request, "total", 0d);

        Map pMap = daService.getMap("select limitday,price,car_type_id from product_package_tb where id=? ", new Object[]{pid});
        Long limitDay = null;//pMap.get("limitday");
        if (pMap != null && pMap.get("limitday") != null) {
            limitDay = (Long) pMap.get("limitday");
        }
        if (limitDay != null) {
            if (limitDay < etime) {//超出有效期
                return "-2";
            }
        }

        Double act_total = total;
        if (!acttotal.equals("")) {
            act_total = StringUtils.formatDouble(Double.valueOf(acttotal));
        }
        Long uin = -1L;
        //添加生成月卡会员时的车主编号
        if (carNumber != null && !carNumber.equals("")) {
            String[] carNumStrings = carNumber.split(",");
            Long validuin = -1L;
            if (carNumStrings != null && carNumStrings.length > 0) {
                for (String strNum : carNumStrings) {
                    strNum.toUpperCase();
                    if (StringUtils.checkPlate(strNum)) {
                        Map mapCarUin = daService.getMap("select uin from car_info_tb where car_number=? ", new Object[]{strNum});
                        if (mapCarUin != null && !mapCarUin.isEmpty()) {
                            uin = Long.valueOf(String.valueOf(mapCarUin.get("uin")));
                        }
                        if (uin > 0) {
                            validuin = uin;
                        }
                        //修改或添加车牌时查询此车牌是否已经对应有月卡会员记录
                        String subCar = strNum.startsWith("无") ? strNum : "%" + strNum.substring(1) + "%";
                        List<Map> carinfoList = daService.getAll("select pid,car_number from  carower_product where com_id=? and is_delete=? and car_number like ?", new Object[]{comid, 0, subCar});
                        Long pidMonth = -1L;
                        boolean isMonthUser = false;
                        if (carinfoList != null && !carinfoList.isEmpty() && carinfoList.size() > 0) {
                            for (Iterator iterator = carinfoList.iterator(); iterator
                                    .hasNext(); ) {
                                Map uinmap = (Map) iterator.next();
                                pidMonth = (Long) uinmap.get("pid");
                                String carNumUnique = String.valueOf(uinmap.get("car_number"));
                                isMonthUser = publicMethods.isMonthUserNew(pidMonth, comid, carNumUnique);
								/*
								 * 假如改车牌已经注册过月卡会员，车牌一致时才能继续添加，否则提示已注册
								 * 其中A,B和B,A也代表车牌是一致的
								 */
                                boolean isSameCarNumber = isCarNumberSame(carNumber, carNumUnique);
                                if (isMonthUser && isSameCarNumber) {
                                    isMonthUser = false;
                                    carNumber = carNumUnique;
                                }
                            }
                            if (isMonthUser) {
                                return "车牌" + carNumber + "已注册为月卡会员,请修改车牌！";
                            }
                        }
                    } else {
                        return "车牌号错误";
                    }
                }
                uin = validuin;
            }
        }

        //Map userMap = daService.getMap("select id from user_info_Tb where mobile=? and auth_flag=? ", new Object[]{mobile,4});

        List<Map<String, Object>> bathSql = new ArrayList<Map<String, Object>>();
//		Map<String, Object> userSqlMap = new HashMap<String, Object>();
//		Map<String, Object> recomSqlMap = new HashMap<String, Object>();
//		Map<String, Object> carInfoMap = new HashMap<String, Object>();
        Map<String, Object> carowerPackMap = new HashMap<String, Object>();
	    /*
	    boolean f = true;
		if(userMap==null){//车主未注册
			uin = daService.getkey("seq_user_info_tb");
			userSqlMap.put("sql", "insert into user_info_tb (id,strid,nickname,mobile,auth_flag,reg_time,media) values(?,?,?,?,?,?,?)");
			userSqlMap.put("values", new Object[]{uin,"carower_"+uin,"车主",mobile,4,ntime,10});
			bathSql.add(userSqlMap);

			//写入记录表，用户通过注册月卡会员注册车主
			recomSqlMap.put("sql", "insert into recommend_tb(nid,type,state,create_time,comid) values(?,?,?,?,?)");
			recomSqlMap.put("values", new Object[]{uin,0,0,System.currentTimeMillis(),comid});
			bathSql.add(recomSqlMap);
		}else {
			f=false;
			uin = (Long)userMap.get("id");
		}
		if(uin==null||uin==-1)
			return "-1";
		String result = commonMethods.checkplot(comid, p_lot, btime, etime, -1L);
		if(result != null){
			return result;
		}*/
        //将之前的车位逻辑修改为适配多个车位存在的情况，
		/*String [] plotStrings = p_lot.split(",");
		if(plotStrings != null && plotStrings.length>0){
			for(String plotstr: plotStrings){
				//判断该车位是否有效
				String result = commonMethods.checkplot(comid, p_lot, btime, etime, -1L);
				if(result != null){
					return result;
				}
			}
		}*/
        Long cartypeId = -1L;
        if (pMap != null && pMap.containsKey("car_type_id") && pMap.get("car_type_id") != null) {
            try {
                cartypeId = Long.parseLong(pMap.get("car_type_id") + "");
            } catch (Exception e) {

            }
        }
        logger.error(">>>>>>>>>>>>>>>>" + cartypeId);
        //Long carTypeId = RequestUtil.getLong(request, "car_type_id", -1L);
        Integer limit_day_type = RequestUtil.getInteger(request, "limit_day_type", 0);
        carowerPackMap.put("sql", "insert into carower_product " +
                "(id,uin,pid,create_time,update_time,b_time,e_time,total,remark,name," +
                "address,act_total,com_id,mobile,car_number,card_id,car_type_id,limit_day_type)" +
                " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        carowerPackMap.put("values", new Object[]{nextid, uin, pid, ntime, ntime,
                btime, etime, total, remark, name, address, act_total, comid, mobile, carNumber.toUpperCase(), String.valueOf(nextid), cartypeId, limit_day_type});

        //******添加月卡消费记录*******
        Map<String, Object> reNewMap = new HashMap<String, Object>();
        reNewMap.put("sql", "insert into card_renew_tb (id,trade_no,card_id,pay_time,amount_receivable,amount_pay," +
                "collector,pay_type,car_number,user_id,resume,buy_month,comid,create_time,update_time) values" +
                "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        Long renewId = daService.getkey("seq_card_renew_tb");
        String tradeNo = TimeTools.getTimeYYYYMMDDHHMMSS() + "" + comid;
        String operater = (String) request.getSession().getAttribute("nickname");
        reNewMap.put("values", new Object[]{renewId, tradeNo, cardId, ntime, total + "", act_total + "", operater, "现金"
                , carNumber.toUpperCase(), name, remark, months, comid, ntime, ntime});
        bathSql.add(reNewMap);
        //************
        bathSql.add(carowerPackMap);
        if (daService.bathUpdate(bathSql)) {
            //if(publicMethods.isEtcPark(comid)){
//				if(f){
					/*int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)",
							new Object[]{comid,"user_info_tb",uin,System.currentTimeMillis()/1000,0});
					logger.error("parkadmin or admin:"+operater+" add  comid:"+comid+" user ,add sync ret:"+re);
					if(uin>-1){
						List list = daService.getAll("select id from car_info_tb where uin = ?", new Object[]{uin});
						for (Object obj : list) {
							Map map = (Map)obj;
							Long carid = Long.parseLong(map.get("id")+"");
							if(carid!=null&&carid>0){
								daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_info_tb",carid,System.currentTimeMillis()/1000,0});
							}
						}
					}*/
//				}
            int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid, "carower_product", nextid, System.currentTimeMillis() / 1000, 0});
            logger.error("parkadmin or admin:" + operater + " add comid:" + comid + " vipuser ,add sync ret:" + r);
//				r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"card_renew_tb",renewId,System.currentTimeMillis()/1000,0});
//				logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" vipuser ,add sync ret:"+r);
            //}
            mongoDbUtils.saveLogs(request, 0, 2, "车主" + mobile + "购买了套餐（编号：" + pid + "）,金额：" + act_total);
            return "1";
        } else {
            return "-1";
        }
    }

    /*//注册包月会员
        private String buyProduct(HttpServletRequest request, Long comid){
            //包月产品
            Long pid =RequestUtil.getLong(request, "p_name",-1L);
            //车主手机
            String mobile =RequestUtil.processParams(request, "mobile").trim();
            String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name").trim());
            String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address").trim());
            //车牌号码
            //String car_number =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number")).toUpperCase();
            //起始时间
            String b_time =RequestUtil.processParams(request, "b_time");
            //购买月数
            Integer months = RequestUtil.getInteger(request, "months", 1);
            //修改月卡会员编号cardId为主键id
            Long nextid = daService.getkey("seq_carower_product");
            //修改原来的月卡会员cardId生成及使用逻辑
//			String cardId =RequestUtil.getString(request, "card_id");
            String cardId = String.valueOf(nextid);
            Long count = daService.getLong("select count(ID) from carower_product where com_id=? and card_id=? and is_delete =?  ", new Object[]{comid,cardId,0});
            if(count>0){
                return "-3";
            }
            Integer flag = RequestUtil.getInteger(request, "flag", -1);
            //备注
            String remark = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));
            String carNumber = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number"));
            //停车位编号
            //String p_lot = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "p_lot")).trim();
            //对停车位的编号添加新的逻辑处理，
            if(p_lot==null || p_lot.equals("") || p_lot.equals("null")){
                p_lot = "-1";
            }
            //实收金额
            String acttotal = RequestUtil.processParams(request, "act_total");

            Long ntime = System.currentTimeMillis()/1000;
            Long btime = TimeTools.getLongMilliSecondFrom_HHMMDD(b_time)/1000;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(btime*1000);
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+months);
            Long etime = calendar.getTimeInMillis()/1000-1;

            //金额
            Double total= commonMethods.getProdSum(pid, months);//RequestUtil.getDouble(request, "total", 0d);

            Map pMap = daService.getMap("select limitday,price from product_package_tb where id=? ", new Object[]{pid});
            Long limitDay = null;//pMap.get("limitday");
            if(pMap!=null&&pMap.get("limitday")!=null){
                limitDay = (Long)pMap.get("limitday");
            }
            if(limitDay!=null){
                if(limitDay<etime){//超出有效期
                    return "-2";
                }
            }

            Double act_total = total;
            if(!acttotal.equals("")){
                act_total = Double.valueOf(acttotal);
            }
             Long uin =-1L;
             //添加生成月卡会员时的车主编号
             if(carNumber != null && !carNumber.equals("")){
                 String [] carNumStrings = carNumber.split(",");
                 Long validuin = -1L;
                 if(carNumStrings != null && carNumStrings.length>0){
                     for(String strNum :carNumStrings){
                         strNum.toUpperCase();
                         if(StringUtils.checkPlate(strNum)){
                             Map mapCarUin = daService.getMap("select uin from car_info_tb where car_number=? ", new Object[]{strNum});
                             if(mapCarUin != null && !mapCarUin.isEmpty()){
                                 uin = Long.valueOf(String.valueOf(mapCarUin.get("uin")));
                             }
                             if(uin>0){
                                 validuin = uin;
                             }
                            //修改或添加车牌时查询此车牌是否已经对应有月卡会员记录
                            String subCar = strNum.startsWith("无")?strNum:"%"+strNum.substring(1)+"%";
                            List<Map> carinfoList = daService.getAll("select pid,car_number from  carower_product where com_id=? and is_delete=? and car_number like ?", new Object[]{comid,0,subCar});
                            Long pidMonth = -1L;
                            boolean isMonthUser = false;
                            if(carinfoList != null && !carinfoList.isEmpty() && carinfoList.size()>0){
                                for (Iterator iterator = carinfoList.iterator(); iterator
                                        .hasNext();) {
                                    Map uinmap = (Map) iterator.next();
                                    pidMonth = (Long)uinmap.get("pid");
                                    String carNumUnique = String.valueOf(uinmap.get("car_number"));
                                    isMonthUser = publicMethods.isMonthUserNew(pidMonth,comid,carNumUnique);

                                     * 假如改车牌已经注册过月卡会员，车牌一致时才能继续添加，否则提示已注册
                                     * 其中A,B和B,A也代表车牌是一致的

                                    boolean isSameCarNumber = isCarNumberSame(carNumber, carNumUnique);
                                    if(isMonthUser && isSameCarNumber){
                                        isMonthUser = false;
                                        carNumber = carNumUnique;
                                    }
                                }
                                if(isMonthUser){
                                    return "车牌"+carNumber+"已注册为月卡会员,请修改车牌！";
                                }
                            }
                         }else{
                             return "车牌号错误";
                         }
                     }
                     uin = validuin;
                 }
             }

            //Map userMap = daService.getMap("select id from user_info_Tb where mobile=? and auth_flag=? ", new Object[]{mobile,4});

            List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
//			Map<String, Object> userSqlMap = new HashMap<String, Object>();
//			Map<String, Object> recomSqlMap = new HashMap<String, Object>();
//			Map<String, Object> carInfoMap = new HashMap<String, Object>();
            Map<String, Object> carowerPackMap = new HashMap<String, Object>();

            boolean f = true;
            if(userMap==null){//车主未注册
                uin = daService.getkey("seq_user_info_tb");
                userSqlMap.put("sql", "insert into user_info_tb (id,strid,nickname,mobile,auth_flag,reg_time,media) values(?,?,?,?,?,?,?)");
                userSqlMap.put("values", new Object[]{uin,"carower_"+uin,"车主",mobile,4,ntime,10});
                bathSql.add(userSqlMap);

                //写入记录表，用户通过注册月卡会员注册车主
                recomSqlMap.put("sql", "insert into recommend_tb(nid,type,state,create_time,comid) values(?,?,?,?,?)");
                recomSqlMap.put("values", new Object[]{uin,0,0,System.currentTimeMillis(),comid});
                bathSql.add(recomSqlMap);
            }else {
                f=false;
                uin = (Long)userMap.get("id");
            }
            if(uin==null||uin==-1)
                return "-1";
            String result = commonMethods.checkplot(comid, p_lot, btime, etime, -1L);
            if(result != null){
                return result;
            }
          //将之前的车位逻辑修改为适配多个车位存在的情况，
            String [] plotStrings = p_lot.split(",");
            if(plotStrings != null && plotStrings.length>0){
                for(String plotstr: plotStrings){
                    //判断该车位是否有效
                    String result = commonMethods.checkplot(comid, p_lot, btime, etime, -1L);
                    if(result != null){
                        return result;
                    }
                }
            }

            carowerPackMap.put("sql", "insert into carower_product " +
                    "(id,uin,pid,create_time,update_time,b_time,e_time,total,remark,name," +
                    "address,act_total,com_id,mobile,car_number,card_id)" +
                    " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            carowerPackMap.put("values", new Object[]{nextid,uin,pid,ntime,ntime,
                    btime,etime,total,remark,name,address,act_total,comid,mobile,carNumber,String.valueOf(nextid)});
            bathSql.add(carowerPackMap);
            if(daService.bathUpdate(bathSql)){
                String operater = request.getSession().getAttribute("loginuin")+"";
                //if(publicMethods.isEtcPark(comid)){
//					if(f){
                        int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)",
                                new Object[]{comid,"user_info_tb",uin,System.currentTimeMillis()/1000,0});
                        logger.error("parkadmin or admin:"+operater+" add  comid:"+comid+" user ,add sync ret:"+re);
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
                    logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" vipuser ,add sync ret:"+r);
                //}
                mongoDbUtils.saveLogs( request,0, 2, "车主"+mobile+"购买了套餐（编号："+pid+"）,金额："+act_total);
                return "1";
            }else {
                return "-1";
            }
        }*/
    //编辑包月会员
    @SuppressWarnings({"rawtypes"})
    private String editProduct(HttpServletRequest request, Long comid) {
        Long id = RequestUtil.getLong(request, "id", -1L);
        //包月产品
        Long pid = RequestUtil.getLong(request, "p_name", -1L);
        //车主手机
        String mobile = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "mobile").trim());
        String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name").trim());
        String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address").trim());
        //车牌号码
        //	String car_number =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number")).toUpperCase();
        //起始时间
        String b_time = RequestUtil.processParams(request, "b_time");
        //购买月数
        Integer months = RequestUtil.getInteger(request, "months", 1);
        Integer flag = RequestUtil.getInteger(request, "flag", -1);
        //备注
        String remark = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));

        //停车位编号
//		String p_lot = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "p_lot"));
        //对停车位编号添加新的逻辑处理
		/*if(p_lot == null || p_lot.equals("") || p_lot.equals("null")){
			p_lot = "-1";
		}*/
        //实收金额
        String acttotal = RequestUtil.processParams(request, "act_total");

        //车牌
//		String car_number =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number")).toUpperCase();
        //金额
        Double total = commonMethods.getProdSum(pid, months);//RequestUtil.getDouble(request, "total", 0d);

        //Map pMap = daService.getMap("select limitday,price from product_package_tb where id=? ", new Object[]{pid});

        Long ntime = System.currentTimeMillis() / 1000;
        Long btime = TimeTools.getLongMilliSecondFrom_HHMMDD(b_time) / 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(btime * 1000);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + months);
        Long etime = calendar.getTimeInMillis() / 1000 - 1;

		/*Long limitDay = null;//pMap.get("limitday");
		if(pMap!=null&&pMap.get("limitday")!=null){
			limitDay = (Long)pMap.get("limitday");
		}
		if(limitDay!=null){
			if(limitDay<etime){//超出有效期
				return "-2";
			}
		}*/

        Double act_total = total;
        if (!acttotal.equals("")) {
            act_total = Double.valueOf(acttotal);
        }

        Long uin = -1L;
        //添加编辑月卡会员时的车主编号
		/*if(car_number != null && !car_number.equals("")){
			 String [] carNumStrings = car_number.split(",");
			 Long validuin = -1L;
			 if(carNumStrings != null && carNumStrings.length>0){
				 for(String strNum :carNumStrings){
					Map mapCarUin = daService.getMap("select uin from car_info_tb where car_number=? ", new Object[]{strNum});
					if(mapCarUin != null && !mapCarUin.isEmpty()){
						uin = Long.valueOf(String.valueOf(mapCarUin.get("uin")));
					}
					if(uin>0){
						validuin = uin;
					}
					//修改或添加车牌时查询此车牌是否已经对应有月卡会员记录
					String subCar = strNum.startsWith("无")?strNum:"%"+strNum.substring(1)+"%";
					List<Map> carinfoList = daService.getAll("select pid,p_lot, car_number from  carower_product where com_id=? and is_delete=? and car_number like ?", new Object[]{comid,0,subCar});
					Long pidMonth = -1L;
					boolean isMonthUser = false;
					for (Iterator iterator = carinfoList.iterator(); iterator
							.hasNext();) {
						Map uinmap = (Map) iterator.next();
						pidMonth = (Long)uinmap.get("pid");
						String carNumUnique = String.valueOf(uinmap.get("car_number"));
						String plotStr = String.valueOf(uinmap.get("p_lot"));
						if(car_number.equals(carNumUnique)){
							isMonthUser = publicMethods.isMonthUserNew(pidMonth,comid,carNumUnique);
							if(!plotStr.equals("") && plotStr.equals(p_lot)){
								isMonthUser = true;
							}else{
								isMonthUser = false;
							}
						}else{
							isMonthUser = false;
						}
					}
					if(isMonthUser){
						return " 该车牌为有效月卡会员,请修改车牌！";
					}
				 }
				 uin = validuin;
			 }
		}*/
        //Map userMap = daService.getMap("select id from user_info_Tb where mobile=? and auth_flag=? ", new Object[]{mobile,4});

        List<Map<String, Object>> bathSql = new ArrayList<Map<String, Object>>();
//		Map<String, Object> userSqlMap = new HashMap<String, Object>();
//		Map<String, Object> recomSqlMap = new HashMap<String, Object>();
        //Map<String, Object> carInfoMap = new HashMap<String, Object>();
        Map<String, Object> carowerPackMap = new HashMap<String, Object>();
		/*if(userMap==null){//车主未注册
			uin = daService.getkey("seq_user_info_tb");
			userSqlMap.put("sql", "insert into user_info_tb (id,strid,nickname,mobile,auth_flag,reg_time,media) values(?,?,?,?,?,?,?)");
			userSqlMap.put("values", new Object[]{uin,"carower_"+uin,"车主",mobile,4,ntime,10});
			bathSql.add(userSqlMap);

			//写入记录表，用户通过注册月卡会员注册车主
			recomSqlMap.put("sql", "insert into recommend_tb(nid,type,state,create_time,comid) values(?,?,?,?,?)");
			recomSqlMap.put("values", new Object[]{uin,0,0,System.currentTimeMillis(),comid});
			bathSql.add(recomSqlMap);
		}else {
			uin = (Long)userMap.get("id");
		}
		if(uin==null||uin==-1)
			return "-1";
			*/
        //将之前的车位逻辑修改为适配多个车位存在的情况，
		/*String [] plotStrings = p_lot.split(",");
		if(plotStrings != null && plotStrings.length>0){
			for(String plotstr: plotStrings){
				//判断该车位是否有效
				String result = commonMethods.checkplot(comid, p_lot, btime, etime, id);
				if(result != null){
					return result;
				}
			}
		}*/
//		String result = commonMethods.checkplot(comid, p_lot, btime, etime, id);
//		if(result != null){
//			return result;
//		}

        carowerPackMap.put("sql", "update carower_product set pid=?,b_time=?,e_time=?," +
                "total=?,remark=?,name=?," +
                "address=?,act_total=?,mobile=?,update_time=? where id=? ");
        carowerPackMap.put("values", new Object[]{pid, btime, etime, total,
                remark, name, address, act_total, mobile, ntime, id});
        bathSql.add(carowerPackMap);
        if (daService.bathUpdate(bathSql)) {
            String operater = request.getSession().getAttribute("loginuin") + "";
            if (bathSql.size() == 1) {
                //if(publicMethods.isEtcPark(comid)){
                int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid, "carower_product", id, System.currentTimeMillis() / 1000, 1});
                logger.error("parkadmin or admin:" + operater + " add comid:" + comid + " vipuser ,add sync ret:" + r);
                //}
            } else {
                //判断是否支持ETCPark
                String isSupportEtcPark = CustomDefind.ISSUPPORTETCPARK;
                if (isSupportEtcPark.equals("1")) {
                    if (publicMethods.isEtcPark(comid)) {
                        int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate,state) values(?,?,?,?,?,?)", new Object[]{comid, "user_info_tb", uin, System.currentTimeMillis() / 1000, 0, 1});
                        logger.error("parkadmin or admin:" + operater + " add comid:" + comid + " user ,add sync ret:" + re);
                        int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid, "carower_product", id, System.currentTimeMillis() / 1000, 1});
                        logger.error("parkadmin or admin:" + operater + " add comid:" + comid + " vipuser ,add sync ret:" + r);
                    }
                } else {
                    int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid, "carower_product", id, System.currentTimeMillis() / 1000, 1});
                    logger.error("parkadmin or admin:" + operater + " add comid:" + comid + " vipuser ,add sync ret:" + r);
                }

            }
            mongoDbUtils.saveLogs(request, 0, 3, "修改了车主" + mobile + "的套餐（编号：" + pid + "）");
            return "1";
        } else {
            return "-1";
        }
    }
	/*@SuppressWarnings({ "rawtypes" })
	private String editProduct(HttpServletRequest request, Long comid){
		Long id = RequestUtil.getLong(request, "id", -1L);
		//包月产品
		Long pid =RequestUtil.getLong(request, "p_name",-1L);
		//车主手机
		String mobile =RequestUtil.processParams(request, "mobile").trim();
		String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name").trim());
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address").trim());
		//车牌号码
	//	String car_number =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number")).toUpperCase();
		//起始时间
		String b_time =RequestUtil.processParams(request, "b_time");
		//购买月数
		Integer months = RequestUtil.getInteger(request, "months", 1);
		Integer flag = RequestUtil.getInteger(request, "flag", -1);
		//备注
		String remark = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));

		//停车位编号
//		String p_lot = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "p_lot"));
		//对停车位编号添加新的逻辑处理
		if(p_lot == null || p_lot.equals("") || p_lot.equals("null")){
			p_lot = "-1";
		}
		//实收金额
		String acttotal = RequestUtil.processParams(request, "act_total");

		//车牌
//		String car_number =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "car_number")).toUpperCase();
		//金额
		Double total = commonMethods.getProdSum(pid, months);//RequestUtil.getDouble(request, "total", 0d);

		//Map pMap = daService.getMap("select limitday,price from product_package_tb where id=? ", new Object[]{pid});

		Long ntime = System.currentTimeMillis()/1000;
		Long btime = TimeTools.getLongMilliSecondFrom_HHMMDD(b_time)/1000;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(btime*1000);
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+months);
		Long etime = calendar.getTimeInMillis()/1000-1;

		Long limitDay = null;//pMap.get("limitday");
		if(pMap!=null&&pMap.get("limitday")!=null){
			limitDay = (Long)pMap.get("limitday");
		}
		if(limitDay!=null){
			if(limitDay<etime){//超出有效期
				return "-2";
			}
		}

		Double act_total = total;
		if(!acttotal.equals("")){
			act_total = Double.valueOf(acttotal);
		}

		Long uin =-1L;
		 //添加编辑月卡会员时的车主编号
		if(car_number != null && !car_number.equals("")){
			 String [] carNumStrings = car_number.split(",");
			 Long validuin = -1L;
			 if(carNumStrings != null && carNumStrings.length>0){
				 for(String strNum :carNumStrings){
					Map mapCarUin = daService.getMap("select uin from car_info_tb where car_number=? ", new Object[]{strNum});
					if(mapCarUin != null && !mapCarUin.isEmpty()){
						uin = Long.valueOf(String.valueOf(mapCarUin.get("uin")));
					}
					if(uin>0){
						validuin = uin;
					}
					//修改或添加车牌时查询此车牌是否已经对应有月卡会员记录
					String subCar = strNum.startsWith("无")?strNum:"%"+strNum.substring(1)+"%";
					List<Map> carinfoList = daService.getAll("select pid,p_lot, car_number from  carower_product where com_id=? and is_delete=? and car_number like ?", new Object[]{comid,0,subCar});
					Long pidMonth = -1L;
					boolean isMonthUser = false;
					for (Iterator iterator = carinfoList.iterator(); iterator
							.hasNext();) {
						Map uinmap = (Map) iterator.next();
						pidMonth = (Long)uinmap.get("pid");
						String carNumUnique = String.valueOf(uinmap.get("car_number"));
						String plotStr = String.valueOf(uinmap.get("p_lot"));
						if(car_number.equals(carNumUnique)){
							isMonthUser = publicMethods.isMonthUserNew(pidMonth,comid,carNumUnique);
							if(!plotStr.equals("") && plotStr.equals(p_lot)){
								isMonthUser = true;
							}else{
								isMonthUser = false;
							}
						}else{
							isMonthUser = false;
						}
					}
					if(isMonthUser){
						return " 该车牌为有效月卡会员,请修改车牌！";
					}
				 }
				 uin = validuin;
			 }
		}
		//Map userMap = daService.getMap("select id from user_info_Tb where mobile=? and auth_flag=? ", new Object[]{mobile,4});

		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
//		Map<String, Object> userSqlMap = new HashMap<String, Object>();
//		Map<String, Object> recomSqlMap = new HashMap<String, Object>();
		//Map<String, Object> carInfoMap = new HashMap<String, Object>();
		Map<String, Object> carowerPackMap = new HashMap<String, Object>();
		if(userMap==null){//车主未注册
			uin = daService.getkey("seq_user_info_tb");
			userSqlMap.put("sql", "insert into user_info_tb (id,strid,nickname,mobile,auth_flag,reg_time,media) values(?,?,?,?,?,?,?)");
			userSqlMap.put("values", new Object[]{uin,"carower_"+uin,"车主",mobile,4,ntime,10});
			bathSql.add(userSqlMap);

			//写入记录表，用户通过注册月卡会员注册车主
			recomSqlMap.put("sql", "insert into recommend_tb(nid,type,state,create_time,comid) values(?,?,?,?,?)");
			recomSqlMap.put("values", new Object[]{uin,0,0,System.currentTimeMillis(),comid});
			bathSql.add(recomSqlMap);
		}else {
			uin = (Long)userMap.get("id");
		}
		if(uin==null||uin==-1)
			return "-1";

		//将之前的车位逻辑修改为适配多个车位存在的情况，
		String [] plotStrings = p_lot.split(",");
		if(plotStrings != null && plotStrings.length>0){
			for(String plotstr: plotStrings){
				//判断该车位是否有效
				String result = commonMethods.checkplot(comid, p_lot, btime, etime, id);
				if(result != null){
					return result;
				}
			}
		}
//		String result = commonMethods.checkplot(comid, p_lot, btime, etime, id);
//		if(result != null){
//			return result;
//		}

		carowerPackMap.put("sql", "update carower_product set pid=?,b_time=?,e_time=?," +
				"total=?,remark=?,name=?," +
				"address=?,act_total=?,mobile=?,update_time=? where id=? ");
		carowerPackMap.put("values", new Object[]{pid,btime,etime,total,
				remark,name,address,act_total,mobile,ntime,id});
		bathSql.add(carowerPackMap);
		if(daService.bathUpdate(bathSql)){
			String operater = request.getSession().getAttribute("loginuin")+"";
			if(bathSql.size()==1){
				//if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"carower_product",id,System.currentTimeMillis()/1000,1});
					logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" vipuser ,add sync ret:"+r);
				//}
			}else{
				//判断是否支持ETCPark
				String isSupportEtcPark = CustomDefind.ISSUPPORTETCPARK;
				if(isSupportEtcPark.equals("1")){
					if(publicMethods.isEtcPark(comid)){
						int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate,state) values(?,?,?,?,?,?)", new Object[]{comid,"user_info_tb",uin,System.currentTimeMillis()/1000,0,1});
						logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" user ,add sync ret:"+re);
						int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"carower_product",id,System.currentTimeMillis()/1000,1});
						logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" vipuser ,add sync ret:"+r);
					}
				}else{
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"carower_product",id,System.currentTimeMillis()/1000,1});
					logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" vipuser ,add sync ret:"+r);
				}

			}
			mongoDbUtils.saveLogs( request,0,3, "修改了车主"+mobile+"的套餐（编号："+pid+"）");
			return "1";
		}else {
			return "-1";
		}
	}*/

	/*private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		String mobile = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "mobile"));
		String p_name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "p_name"));
		String car_nubmer = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "car_number"));
		SqlInfo sqlInfo1 = null;
		SqlInfo sqlInfo2 = null;
		SqlInfo sqlInfo3 = null;
		if(!mobile.equals("")){
			sqlInfo1 = new SqlInfo(" u.mobile like ? ",new Object[]{"%"+mobile+"%"});
		}
		if(!p_name.equals("")){
			sqlInfo3 = new SqlInfo(" p.p_name like ?  ",new Object[]{"%"+p_name+"%"});
		}
		if(!car_nubmer.equals("")){
			sqlInfo2 = new SqlInfo(" c.uin in (select uin from car_info_tb where car_number like ?)  ",new Object[]{"%"+car_nubmer+"%"});
		}
		if(sqlInfo1!=null){
			if(sqlInfo2!=null)
				sqlInfo1 = SqlInfo.joinSqlInfo(sqlInfo1, sqlInfo2, 2);
			if(sqlInfo3!=null)
				sqlInfo1 = SqlInfo.joinSqlInfo(sqlInfo1, sqlInfo3, 2);
			return sqlInfo1;
		}else if(sqlInfo2!=null){
			if(sqlInfo3!=null)
				sqlInfo2 = SqlInfo.joinSqlInfo(sqlInfo2, sqlInfo3, 2);
			return sqlInfo2;
		}
		return sqlInfo3;
	}*/

    private void setList(List<Map<String, Object>> list) {
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {
                Long b_time = (Long) map.get("b_time");
                Long e_time = (Long) map.get("e_time");
                Integer months = Math.round((e_time - b_time) / (30 * 24 * 60 * 60));
                map.put("months", months);
            }
        }
    }

    private List query(HttpServletRequest request, long comid) {
        List<Object> params = new ArrayList<Object>();
        params.add(comid);
        params.add(0);
        String sql = "select * from carower_product c where  com_id=? and is_delete =?  ";
        String countSql = "select count(*)   from carower_product c where  com_id=?  and is_delete =? ";
        Integer pageNum = RequestUtil.getInteger(request, "page", 1);
        Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
        String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
        //SqlInfo sqlInfo = RequestUtil.customSearch(request, "c_product", "c", null);
        SqlInfo sqlInfo = RequestUtil.customSearch(request, "c_product");
        //SqlInfo newsqlInfo =RequestUtil.customSearch(request, "product_package_tb");
        //	SqlInfo ssqlInfo = getSuperSqlInfo(request);
        if (sqlInfo != null) {
			/*if(ssqlInfo!=null)
				sqlInfo = SqlInfo.joinSqlInfo(sqlInfo,ssqlInfo, 2);*/
            countSql += " and " + sqlInfo.getSql();
            sql += " and " + sqlInfo.getSql();
            params.addAll(sqlInfo.getParams());
        }
		/*else if(ssqlInfo!=null){
			countSql+=" and "+ ssqlInfo.getSql();
			sql +=" and "+ssqlInfo.getSql();
			params= ssqlInfo.getParams();
		}*/
        //System.out.println(sqlInfo);
        Long count = daService.getCount(countSql, params);
        List list = null;//daService.getPage(sql, null, 1, 20);
        if (count > 0) {
            String orderby = "id";
            String sort = "desc";
            String orderfield = RequestUtil.processParams(request, "orderfield");
            String reqorderby = RequestUtil.processParams(request, "orderby");
            if (StringUtils.isNotNull(orderfield))
                orderby = orderfield;
            if (StringUtils.isNotNull(orderfield))
                sort = reqorderby;
            list = daService.getAll(sql + " order by " + orderby + " " + sort, params, pageNum, pageSize);

        }
        List<Object> arrayList = new ArrayList<Object>();
        arrayList.add(list);
        arrayList.add(pageNum);
        arrayList.add(count);
        arrayList.add(fieldsstr);
        return arrayList;
    }

    /**
     * 判断新注册月卡会员中的车牌号与模糊匹配月卡会员记录中的车牌号是否一致
     * 其中A,B,C与B,C,A是一致的
     * @param carNumber
     * @param carNumberMonth
     * @return
     */
    private boolean isCarNumberSame(String carNumber, String carNumberMonth) {
        int length1 = carNumber.length();
        int length2 = carNumberMonth.length();
        int length4 = 0;
        //判断月卡会员表中车牌号中所含有的车牌个数
        if (carNumberMonth != null && length2 > 0) {
            String[] carNumberMonths = carNumberMonth.split(",");
            for (String str : carNumberMonths) {
                if (str != null && str.length() > 0) {
                    length4++;
                }
            }
        }
        //先判断车牌号长度是否一致
        if (carNumber != null && length1 > 0 && length1 == length2) {
            int length3 = 0;
            String[] strings = carNumber.split(",");
            for (String str : strings) {
                if (carNumberMonth.contains(str)) {
                    length3++;
                }
            }
            //判断是否一致
            if (length3 == length4) {
                return true;
            }
        } else {
            return false;
        }
        return false;
    }


//	public static void main(String[] args) {
//		String A = "b,c,d,e";
//		String B = "b,c";
//		int num=0;
//		String [] bstrStrings = B.split(",");
//		for(String str:bstrStrings){
//			if(A.contains(str)){
//				num++;
//			}
//		}
//		System.out.println(num);
//		/*
//		 * 当前月卡会员车牌号修改成功后，未修改前车牌号所对应的月卡会员需要做统一修改处理
//		 */
//		/*List<Map> list = daService.getAll("select * from carower_product where car_number=? and com_id=? and is_delete=?", new Object[]{carNumberBefore,comid,0});
//		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
//			Map uinmap = (Map) iterator.next();
//			Long idLong = Long.valueOf(String.valueOf(uinmap.get("id")));
//			//根据查询出来对应的月卡会员id修改所有的车牌记录
//			int result = daService.update("update carower_product set car_number=? where id=?", new Object[]{carNumber,idLong});
//			if(result == 1){
//				int resultUpdate = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)",
//						new Object[]{comid,"carower_product",idLong,System.currentTimeMillis()/1000,1});
//			}
//		}*/
//	}
}