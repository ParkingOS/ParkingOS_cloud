package com.zld.struts.anlysis;

import com.zld.AjaxUtil;
import com.zld.dao.PgOnlyReadDao;
import com.zld.service.DataBaseService;
import com.zld.utils.*;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlotProdAnlysisAction extends Action{
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadDao pgOnlyReadDao;
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		Integer role = RequestUtil.getInteger(request, "role",-1);
		if(ZLDType.ZLD_ACCOUNTANT_ROLE==role||ZLDType.ZLD_CARDOPERATOR==role)
			request.setAttribute("role", role);
		if(comid==null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			String monday = StringUtils.getFistdayOfMonth();
			request.setAttribute("btime", monday);
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			request.setAttribute("etime",  df2.format(System.currentTimeMillis()));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Map<String, Object>> list = plotprodquery(request, comid);
			int count = list!=null?list.size():0;
			String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"p_lot");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("detail")){
			request.setAttribute("p_lot", RequestUtil.processParams(request, "p_lot"));
			request.setAttribute("btime", RequestUtil.processParams(request, "btime"));
			request.setAttribute("etime", RequestUtil.processParams(request, "etime"));
			return mapping.findForward("plotproddetail");
		}else if(action.equals("ppdetail")){
			String p_lot = RequestUtil.processParams(request, "p_lot");
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			String year = RequestUtil.processParams(request, "time");
			if(year.equals("")){
				year = "curyear";
			}
			String  firday = StringUtils.getFistdayOfYear();//今年开始时间
			Integer curyear = Integer.valueOf(firday.split("-")[0]);
			String nextfirday = (curyear + 1) + "-01-01";//明年的开始时间
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDD(firday)/1000;
			Long e = TimeTools.getLongMilliSecondFrom_HHMMDD(nextfirday)/1000;
			if(year.equals("lastyear")){
				String lastfirday = (curyear - 1) + "-01-01";//去年的开始时间
				b = TimeTools.getLongMilliSecondFrom_HHMMDD(lastfirday)/1000;
				e = TimeTools.getLongMilliSecondFrom_HHMMDD(firday)/1000;
			}

			String sql = "select cp.*,p.p_name,p.price from carower_product cp,product_package_tb p where " +
					"cp.pid=p.id and cp.create_time between ? and ? and p.comid=? and cp.p_lot=? order by cp.create_time desc ";
			List<Object> params = new ArrayList<Object>();
			params.add(b);
			params.add(e);
			params.add(comid);
			params.add(p_lot);
			List<Map<String, Object>> list = daService.getAllMap(sql, params);
			int count = list!=null?list.size():0;
			getcarinfo(list);
			String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("expexcel")){
			try {
				String fname = "停车位月卡收费台账" + com.zld.utils.TimeTools.getDate_YY_MM_DD();
				fname = StringUtils.encodingFileName(fname);
				java.io.OutputStream os;
				os = response.getOutputStream();
				response.reset();
				response.setHeader("Content-disposition", "attachment; filename="
						+ fname + ".xls");
				//首先要使用Workbook类的工厂方法创建一个可写入的工作薄(Workbook)对象
				WritableWorkbook wwb = Workbook.createWorkbook(os);
				//创建一个可写入的工作表
				//Workbook的createSheet方法有两个参数，第一个是工作表的名称，第二个是工作表在工作薄中的位置
				WritableSheet sheetOne = wwb.createSheet("sheet1", 0);
				WritableFont wf = new WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD, false);
				WritableCellFormat wff = new WritableCellFormat(wf);
				wff.setWrap(true);
				wff.setAlignment(Alignment.CENTRE);
				wff.setVerticalAlignment(VerticalAlignment.CENTRE);
				//label第一个参数代表列，第二个参数代表行，第三个是数据，第四个是格式
				//第一行
				sheetOne.addCell(new Label(0,0,"停车位月卡收费台账",wff));
				sheetOne.addCell(new Label(0,1,"车位号",wff));
				sheetOne.addCell(new Label(1,1,"房号",wff));
				sheetOne.addCell(new Label(2,1,"会员姓名",wff));
				sheetOne.addCell(new Label(3,1,"会员手机号",wff));
				sheetOne.addCell(new Label(4,1,"收费标准（元/月）",wff));
				sheetOne.addCell(new Label(5,1,"月卡开通情况",wff));
				sheetOne.addCell(new Label(7,1,"收取时间/收取周期/收取金额",wff));
				sheetOne.addCell(new Label(8,1,"收取合计",wff));
				sheetOne.addCell(new Label(9,1,"分配到各期",wff));
				sheetOne.addCell(new Label(12,1,"优惠金额合计",wff));
				//第二行
				sheetOne.addCell(new Label(5,2,"开通日期",wff));
				sheetOne.addCell(new Label(6,2,"车牌号",wff));
				sheetOne.addCell(new Label(9,2,"上一年",wff));
				sheetOne.addCell(new Label(10,2,"本年",wff));
				sheetOne.addCell(new Label(11,2,"下一年",wff));
				sheetOne.mergeCells(0, 0, 12, 0);//第一行所有列合并
				sheetOne.mergeCells(0, 1, 0, 2);//第一列的第二行和第三行合并
				sheetOne.mergeCells(1, 1, 1, 2);//第二列的第二行和第三行合并
				sheetOne.mergeCells(2, 1, 2, 2);//第三列的第二行和第三行合并
				sheetOne.mergeCells(3, 1, 3, 2);//第四列的第二行和第三行合并
				sheetOne.mergeCells(4, 1, 4, 2);//第五列的第二行和第三行合并
				sheetOne.mergeCells(5, 1, 6, 1);//第六列和第七列的第二行合并
				sheetOne.mergeCells(7, 1, 7, 2);//第八列的第二行和第三行合并
				sheetOne.mergeCells(8, 1, 8, 2);//第九列的第二行和第三行合并
				sheetOne.mergeCells(9, 1, 11, 1);//第十列和第十二列的第二行合并
				sheetOne.mergeCells(12, 1, 12, 2);//第十三列的第二行和第三行合并

				List<Map<String, Object>> list = plotprodquery(request, comid);
				if(list != null && !list.isEmpty()){
					String [] f = new String[]{"p_lot","address","names","mobiles","unitprice","opentime","carnumbers","record","atotal","lasttotal","curtotal","nexttotal","dtotal"};
					for(int i=0; i<list.size();i++){
						Map<String, Object> map = list.get(i);
						for(int j=0;j<f.length;j++){
							Label data=new Label(j,i+3,map.get(f[j])+"");//数据从第三行开始添加
							sheetOne.addCell(data);
						}
					}
				}
				//从内存中读出
				wwb.write();
				//关闭资源，释放内存
				wwb.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> plotprodquery(HttpServletRequest request, Long comid){
		String year = RequestUtil.processParams(request, "time");
		if(year.equals("")){
			year = "curyear";
		}
		String  firday = StringUtils.getFistdayOfYear();//今年开始时间
		Integer curyear = Integer.valueOf(firday.split("-")[0]);
		String nextfirday = (curyear + 1) + "-01-01";//明年的开始时间
		Long b = TimeTools.getLongMilliSecondFrom_HHMMDD(firday)/1000;
		Long e = TimeTools.getLongMilliSecondFrom_HHMMDD(nextfirday)/1000;
		if(year.equals("lastyear")){
			String lastfirday = (curyear - 1) + "-01-01";//去年的开始时间
			b = TimeTools.getLongMilliSecondFrom_HHMMDD(lastfirday)/1000;
			e = TimeTools.getLongMilliSecondFrom_HHMMDD(firday)/1000;
		}

		String sql = "select cp.*,p.p_name,p.price from carower_product cp,product_package_tb p where " +
				"cp.pid=p.id and cp.p_lot!=? and cp.create_time between ? and ? and p.comid=? order by cp.create_time ";
		List<Object> params = new ArrayList<Object>();
		params.add("");
		params.add(b);
		params.add(e);
		params.add(comid);
		List<Map<String, Object>> list = daService.getAllMap(sql, params);
		List<Map<String, Object>> rList = new ArrayList<Map<String,Object>>();
		List<String> plots = new ArrayList<String>();
		List<Long> uinList = new ArrayList<Long>();
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				String p_lot = (String)map.get("p_lot");
				Long create_time = (Long)map.get("create_time");
				Long b_time = (Long)map.get("b_time");
				Long e_time = (Long)map.get("e_time");
				Double total = Double.valueOf(map.get("total") + "");
				Double act_total = Double.valueOf(map.get("act_total") + "");
				Double d_total = total > act_total ? total - act_total : 0d;
				String name = (map.get("name") == null) ? "" : (String)map.get("name");
				String p_name = (String)map.get("p_name");
				Double price = Double.valueOf(map.get("price") + "");
				Long uin = (Long)map.get("uin");
				String address = "";
				if(map.get("address") != null){
					address = (String)map.get("address");
				}
				String onerec = TimeTools.getTimeStr_yyyy_MM_dd(create_time*1000)+
						"/"+TimeTools.getTimeStr_yyyy_MM_dd(b_time*1000)+
						"至"+TimeTools.getTimeStr_yyyy_MM_dd(e_time*1000)+
						"/"+act_total;

				Map<String, Object> stgMap = getAcctByYear(map);
				Double lasttotal = Double.valueOf(stgMap.get("lasttotal") + "");//分配到去年的金额
				Double curtotal = Double.valueOf(stgMap.get("curtotal") + "");//分配到今年的金额
				Double nexttotal = Double.valueOf(stgMap.get("nexttotal") + "");//分配到明年的金额
				if(plots.contains(p_lot)){
					for(Map<String, Object> map2 : rList){
						String plot = (String)map2.get("p_lot");
						if(p_lot.equals(plot)){
							String record = (String)map2.get("record");//收取记录
							String names = (String)map2.get("names");//会员姓名
							String uins = (String)map2.get("uins");//会员编号
							Double ototal = Double.valueOf(map2.get("ototal") + "");//应收总金额
							Double atotal = Double.valueOf(map2.get("atotal") + "");//实收总金额
							Double dtotal = Double.valueOf(map2.get("dtotal") + "");//优惠金额
							Double lsttal = Double.valueOf(map2.get("lasttotal") + "");
							Double curtal = Double.valueOf(map2.get("curtotal") + "");
							Double nxttal = Double.valueOf(map2.get("nexttotal") + "");
							Integer count = (Integer)map2.get("count");
							String addresses = (String)map2.get("address");
							record += "\n"+onerec;
							map2.put("record", record);
							if(!name.equals("") && !names.contains(name)){
								if(names.equals("")){
									names = name;
								}else{
									names += ","+name;
								}
							}
							map2.put("names", names);
							if(!uins.contains(uin+"")){
								uins += ","+uin;
							}
							map2.put("uins", uins);
							map2.put("ototal", StringUtils.formatDouble(ototal + total));
							map2.put("atotal", StringUtils.formatDouble(atotal + act_total));
							map2.put("dtotal", StringUtils.formatDouble(dtotal + d_total));
							map2.put("lasttotal", StringUtils.formatDouble(lsttal + lasttotal));
							map2.put("curtotal", StringUtils.formatDouble(curtal + curtotal));
							map2.put("nexttotal", StringUtils.formatDouble(nxttal + nexttotal));
							map2.put("count", count + 1);
							if(!address.equals("") && !addresses.contains(address)){
								if(addresses.equals("")){
									addresses = address;
								}else{
									addresses += ","+address;
								}
							}
							map2.put("address", addresses);
							break;
						}
					}
				}else{
					Map<String, Object> map2 = new HashMap<String, Object>();
					map2.put("p_lot", p_lot);
					map2.put("opentime", TimeTools.getTimeStr_yyyy_MM_dd(create_time*1000));
					map2.put("unitprice", price);
					map2.put("names", name);
					map2.put("uins", uin+"");
					map2.put("record", onerec);
					map2.put("ototal", total);
					map2.put("atotal", act_total);
					map2.put("lasttotal", lasttotal);
					map2.put("curtotal", curtotal);
					map2.put("nexttotal", nexttotal);
					map2.put("carnumbers", "");
					map2.put("mobiles", "");
					map2.put("dtotal", d_total);
					map2.put("count", 1);
					map2.put("address", address);
					plots.add(p_lot);
					rList.add(map2);
				}

				if(!uinList.contains(uin)){
					uinList.add(uin);
				}
			}
			List<Map<String, Object>> resultlList = getUserInfo(uinList);
			if(resultlList != null && !resultlList.isEmpty()){
				for(Map<String, Object> map : rList){
					String uins = (String)map.get("uins");
					String[] uinArr = uins.split(",");
					String carnumbers = "";
					String mobiles = "";
					for(int i=0; i<uinArr.length; i++){
						for(Map<String, Object> map2 : resultlList){
							String id = map2.get("id") + "";
							String mobile = (String)map2.get("mobile");
							if(uinArr[i].equals(id)){
								if(mobiles.equals("")){
									mobiles = mobile;
								}else{
									mobiles += "," + mobile;
								}

								if(map2.get("car_number") != null){
									String car_number = (String)map2.get("car_number");
									if(car_number.equals("")){
										carnumbers = car_number;
									}else{
										carnumbers += "\n" + car_number;
									}
								}
								break;
							}
						}
					}
					map.put("mobiles", mobiles);
					map.put("carnumbers", carnumbers);
				}
			}

//			getPlotinfo(rList,comid);
		}
		return rList;
	}

	private Map<String, Object> getAcctByYear(Map<String, Object> plotMap){
		Map<String, Object> rmap = new HashMap<String, Object>();
		String  firday = StringUtils.getFistdayOfYear();//今年开始时间
		Integer year = Integer.valueOf(firday.split("-")[0]);
		String nextfirday = (year + 1) + "-01-01";//明年的开始时间
		Long fir = TimeTools.getLongMilliSecondFrom_HHMMDD(firday)/1000;
		Long nextfir = TimeTools.getLongMilliSecondFrom_HHMMDD(nextfirday)/1000;
		Long btime = (Long)plotMap.get("b_time");
		Long etime = (Long)plotMap.get("e_time");
		Double act_total = Double.valueOf(plotMap.get("act_total") + "");
		Long lsttime = 0L;//分配到去年的时间
		Long curtime = 0L;//分配到今年的时间
		Long nexttime = 0L;//分配到明年的时间
		//计算去年分配的时间
		if(etime <= fir){
			lsttime = etime - btime;
		}else if(btime < fir){
			lsttime = fir - btime;
		}
		//计算今年分配的时间
		if(etime > fir && btime < nextfir){
			if(btime < fir){
				if(etime <= nextfir){
					curtime = etime - fir;
				}else{
					curtime = nextfir - fir;
				}
			}else{
				if(etime > nextfir){
					curtime = nextfir - btime;
				}else{
					curtime = etime - btime;
				}
			}
		}
		//计算明年分配的时间
		if(etime > nextfir){
			if(btime <= nextfir){
				nexttime = etime - nextfir;
			}else{
				nexttime = etime - btime;
			}
		}

		Double lasttotal = StringUtils.formatDouble((((double)lsttime)/(etime - btime))*act_total);//分配到今年之前的金额
		Double curtotal = StringUtils.formatDouble((((double)curtime)/(etime - btime))*act_total);//分配到今年的金额
		Double nexttotal = StringUtils.formatDouble(act_total-lasttotal - curtotal);//分配到明年的金额

		rmap.put("lasttotal", lasttotal);
		rmap.put("curtotal", curtotal);
		rmap.put("nexttotal", nexttotal);
		return rmap;
	}

	private void getPlotinfo(List<Map<String, Object>> list, Long comid){
		if(list != null && !list.isEmpty()){
			List<Object> plots = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> map : list){
				String p_lot = (String)map.get("p_lot");
				plots.add(p_lot);
				if(preParams.equals("")){
					preParams ="?";
				}else{
					preParams += ",?";
				}
			}
			plots.add(comid);
			String sql = "select cid,address from com_park_tb where cid in ("+preParams+") and comid=? ";
			List<Map<String, Object>> list2 = daService.getAllMap(sql, plots);
			if(list2 != null && !list2.isEmpty()){
				for(Map<String, Object> map : list2){
					String cid = "";
					if(map.get("cid") != null){
						cid = (String)map.get("cid");
					}
					String address = "";
					if(map.get("address") != null){
						address = (String)map.get("address");
					}
					for(Map<String, Object> map2 : list){
						String p_lot = (String)map2.get("p_lot");
						if(cid.equals(p_lot)){
							map2.put("address", address);
							break;
						}
					}
				}
			}
		}
	}

	private void getcarinfo(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Long> uins = new ArrayList<Long>();
			for(Map<String, Object> map : list){
				Long uin = (Long)map.get("uin");
				Double total = Double.valueOf(map.get("total") + "");
				Double act_total = Double.valueOf(map.get("act_total") + "");
				Double favtotal = 0d;
				if(total > act_total){
					favtotal = StringUtils.formatDouble(total - act_total);
				}
				map.put("favtotal", favtotal);

				if(!uins.contains(uin)){
					uins.add(uin);
				}
			}
			List<Map<String, Object>> resultlList = getUserInfo(uins);
			for(Map<String, Object> map : list){
				Long uin = (Long)map.get("uin");
				for(Map<String, Object> map2 : resultlList){
					Long id = (Long)map2.get("id");
					if(uin.intValue() == id.intValue()){
						if(map2.get("nickname") != null){
							map.put("nickname", map2.get("nickname"));
						}
						if(map2.get("mobile") != null){
							map.put("mobile", map2.get("mobile"));
						}
						if(map2.get("car_number") != null){
							map.put("car_number", map2.get("car_number"));
						}
					}
				}
			}
		}
	}

	private List<Map<String, Object>> getUserInfo(List<Long> uins){
		List<Map<String, Object>> resultlList = new ArrayList<Map<String,Object>>();
		if(uins != null && !uins.isEmpty()){
			String preParams  ="";
			for(Long uin : uins){
				if(preParams.equals("")){
					preParams ="?";
				}else{
					preParams += ",?";
				}
			}
			List<Object> params = new ArrayList<Object>();
			params.addAll(uins);
			String sql = "select c.car_number,u.id,u.nickname,u.mobile from user_info_tb u left join car_info_tb c on u.id=c.uin where " +
					"u.id in ("+preParams+") ";
			List<Map<String, Object>> list2 = daService.getAllMap(sql, params);

			List<Long> rsltuins = new ArrayList<Long>();
			for(Map<String, Object> map : list2){
				Long uin = (Long)map.get("id");
				if(rsltuins.contains(uin)){
					String carnumber = (String)map.get("car_number");
					for(Map<String, Object> map2 : resultlList){
						Long id = (Long)map2.get("id");
						if(uin.intValue() == id.intValue()){
							String cnum = (String)map2.get("car_number");
							cnum += "," + carnumber;
							map2.put("car_number", cnum);
							break;
						}
					}
				}else{
					rsltuins.add(uin);
					resultlList.add(map);
				}
			}
		}
		return resultlList;
	}
}
