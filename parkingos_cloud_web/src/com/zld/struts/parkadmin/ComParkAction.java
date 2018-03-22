package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.impl.MongoDbUtils;
import com.zld.service.DataBaseService;
import com.zld.utils.*;
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
/**
 * 停车场后台管理员登录后，管理员工，员工分为收费员和财务
 * @author Administrator
 *
 */
public class ComParkAction extends Action{

	@Autowired
	private DataBaseService daService;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	private Logger logger = Logger.getLogger(ComParkAction.class);

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		request.setAttribute("authid", request.getParameter("authid"));
		String loginuin = request.getSession().getAttribute("loginuin")+"";
		Integer isAdmin =(Integer)request.getSession().getAttribute("isadmin");
		if(loginuin==null){
			response.sendRedirect("login.do");
			return null;
		}
		if(comid==0)
			comid = RequestUtil.getLong(request, "comid", 0L);
		if(action.equals("")){
			request.setAttribute("comid", comid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select c.*,d.did from com_park_tb c left join  dici_tb d on c.dici_id= d.id ";
			String countSql = "select count(c.*) from com_park_tb c left join  dici_tb d on c.dici_id= d.id " ;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo base = new SqlInfo(" c.comid=? ", new Object[]{comid});
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_park","c",null);
			List<Object> params = null;
			if(sqlInfo!=null){
				sqlInfo = SqlInfo.joinSqlInfo(base,sqlInfo, 2);
				countSql+=" where "+ sqlInfo.getSql();
				sql +=" where "+sqlInfo.getSql();
				params= sqlInfo.getParams();
			}else {
				countSql+=" where  "+ base.getSql();
				sql +=" where "+base.getSql();
				params= base.getParams();
			}
			//System.out.println(sqlInfo);
			Long count= daService.getCount(countSql, params);
			List list = null;//daService.getPage(sql, null, 1, 20);
			if(count>0){
				list = daService.getAll(sql+ " order by c.id desc", params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			String cids = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "cid"));
			Long berthsec_id = RequestUtil.getLong(request, "berthsec_id", -1L);
			int result =0;
			if(cids.indexOf("-")!=-1){
				String []cs = cids.split("-");
				int index = getNumberStart(cs[0]);
				String pre = cs[0].substring(0,index);
				int start = Integer.valueOf(cs[0].substring(index));
				int end = Integer.valueOf(cs[1].substring(index));
				logger.error(comid+",addcompark,pre:"+pre+",s:"+start+",e:"+end);
				String []codes=null;
				Long ids[]=null;
				int count = end-start+1;
				if(count>0){
					ids=new Long[count];
					for(int i=0;i<count;i++){
						Long newId = daService.getkey("seq_qr_code_tb");
						ids[i]=newId;
					}
					codes = StringUtils.getGRCode(ids);
				}else {
					AjaxUtil.ajaxOutput(response, result+"");
					return null;
				}
				Long ntime  = System.currentTimeMillis()/1000;
				String sql ="insert into com_park_tb(comid,cid,state,qid,berthsec_id) values(?,?,?,?,?) ";
				List<Object[]> values=new ArrayList<Object[]>();
				int ret =1;
				List<Object> params = new ArrayList<Object>();
				params.add(0);
				params.add(comid);
				String preParams  ="";
				for(int i=0;i<count;i++){
					if(preParams.equals("")){
						preParams ="?";
					}else{
						preParams += ",?";
					}

					String cid = pre+(start+i);
					String en = pre+end;
					int d = cs[1].length()-cid.length();
					if(d>0){
						cid = ""+(start+i);
						for(int j =0;j<d;j++)
							cid ="0"+cid;
						cid = pre+cid;
					}
					params.add(cid);
					values.add(new Object[]{comid,cid,0,ids[i],berthsec_id});
				}
				Long pcount = daService.getCount("select count(id) from com_park_tb where is_delete=? and " +
						"comid=? and cid in ("+preParams+")  ", params);
				if(pcount > 0){
					AjaxUtil.ajaxOutput(response, "-2");
					return null;
				}
				daService.bathInsert(sql, values, new int[]{4,12,4,4,4});
				if(ret>0){//加二维码
					sql = "insert into qr_code_tb(id,comid,ctime,type,code) values(?,?,?,?,?)";
					values.clear();
					for(int i=0;i<codes.length;i++){
						values.add(new Object[]{ids[i],comid,ntime,2,codes[i]});
					}
					result=daService.bathInsert(sql, values, new int[]{4,4,4,4,12});
				}

			}else if(cids.indexOf(",")!=-1){
				String []cs = cids.split(",");
				int count =cs.length;
				String []codes=null;
				Long ids[]=null;
				if(count>0){
					ids=new Long[count];
					for(int i=0;i<count;i++){
						Long newId = daService.getkey("seq_qr_code_tb");
						ids[i]=newId;
					}
					codes = StringUtils.getGRCode(ids);
				}else {
					AjaxUtil.ajaxOutput(response, result+"");
					return null;
				}
				Long ntime  = System.currentTimeMillis()/1000;
				String sql ="insert into com_park_tb(comid,cid,state,qid) values(?,?,?,?) ";
				List<Object[]> values=new ArrayList<Object[]>();
				int ret =1;
				for(int j=0;j<count;j++){
					values.add(new Object[]{comid,cs[j],0,ids[j]});
				}
				daService.bathInsert(sql, values, new int[]{4,12,4,4});
				if(ret>0){//加二维码
					sql = "insert into qr_code_tb(id,comid,ctime,type,code) values(?,?,?,?,?)";
					values.clear();
					for(int i=0;i<codes.length;i++){
						values.add(new Object[]{ids[i],comid,ntime,2,codes[i]});
					}
					result=daService.bathInsert(sql, values, new int[]{4,4,4,4,12});
				}

			}else {
				String []codes=null;
				Long newId = daService.getkey("seq_qr_code_tb");
				codes = StringUtils.getGRCode(new Long[]{newId});
				Long ntime  = System.currentTimeMillis()/1000;
				String sql ="insert into com_park_tb(comid,cid,state,qid) values(?,?,?,?) ";
				List<Object[]> values=new ArrayList<Object[]>();
				int ret =1;
				values.add(new Object[]{comid,cids,0,newId});
				daService.bathInsert(sql, values, new int[]{4,12,4,4});
				if(ret>0){//加二维码
					sql = "insert into qr_code_tb(id,comid,ctime,type,code) values(?,?,?,?,?)";
					values.clear();
					values.add(new Object[]{newId,comid,ntime,2,codes[0]});
					result=daService.bathInsert(sql, values, new int[]{4,4,4,4,12});
				}
			}
			mongoDbUtils.saveLogs( request,0, 2, "添加了车位："+cids);
			AjaxUtil.ajaxOutput(response, result+"");
			return null;
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "ids", -1L);
			Map tempMap = daService.getMap("select * from com_park_tb where id =?", new Object[]{id});
			int ret = 0;
			if(id>0){
				ret = daService.update("delete from com_park_tb where id =? ", new Object[]{id});
				mongoDbUtils.saveLogs( request,0, 4, "删除了车位："+tempMap);
			}
			AjaxUtil.ajaxOutput(response, ret+"");
		}
		else if(action.equals("edit")){
			String cid =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "cid"));
			Long qid =RequestUtil.getLong(request, "qid", -1L);
			Integer dici_id =RequestUtil.getInteger(request, "dici_id",-1);
			Integer state = RequestUtil.getInteger(request, "state", -1);
			Long id =RequestUtil.getLong(request, "id", -1L);
			int ret = 0;
			if(id>0){
				ret = daService.update("update com_park_tb set cid=?,qid=?,dici_id=?,state=? where id =? ",
						new Object[]{cid,qid,dici_id,state,id});
				//mongoDbUtils.saveLogs( request,0, 3, "修改了车位："+cid+",编号："+id);
			}
			AjaxUtil.ajaxOutput(response, ret+"");
		}
		return null;
	}

	private int getNumberStart(String v){
		for(int i=0;i<v.length();i++){
			char c = v.charAt(i);
			if(Check.isNumber(c+"")){
				return i;
			}
		}
		return 0;
	}












}