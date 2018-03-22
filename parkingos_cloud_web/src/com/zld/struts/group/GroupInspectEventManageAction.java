package com.zld.struts.group;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.zld.AjaxUtil;
import com.zld.impl.MemcacheUtils;
import com.zld.impl.MongoClientFactory;
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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupInspectEventManageAction extends Action {

    @Autowired
    private DataBaseService daService;
    @Autowired
    private PgOnlyReadService pgOnlyReadService;
    @Autowired
    private MemcacheUtils memcacheUtils;


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
            request.setAttribute("cityid", cityid);
            return mapping.findForward("list");
        }else if(action.equals("quickquery")){
            String sql = "select * from inspect_event_tb where berthsec_id in(select id from com_berthsecs_tb where comid in(" +
                    "select id from com_info_tb where groupid = ? ))";
            String countSql = "select count(*) from inspect_event_tb where berthsec_id in(select id from com_berthsecs_tb where comid in(" +
                    "select id from com_info_tb where groupid = ? ))";
            String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
            Integer pageNum = RequestUtil.getInteger(request, "page", 1);
            Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
            List list = null;
            Long count = 0L;
            List<Object> params = new ArrayList<Object>();
            params.add(groupid);
            count = daService.getCount(countSql,params);
            if(count>0){
                list = daService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
            }
//			}
            String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");

            AjaxUtil.ajaxOutput(response, json);
        }else if(action.equals("query")){
            String sql = "select * from inspect_event_tb where berthsec_id in(select id from com_berthsecs_tb where comid in(" +
                    "select id from com_info_tb where groupid = ? ))";
            String countSql = "select count(*) from inspect_event_tb where berthsec_id in(select id from com_berthsecs_tb where comid in(" +
                    "select id from com_info_tb where groupid = ? ))";
            String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
            Integer pageNum = RequestUtil.getInteger(request, "page", 1);
            Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
            SqlInfo base = new SqlInfo("1=1", new Object[]{groupid});
            SqlInfo sqlInfo = RequestUtil.customSearch(request,"inspect_event_tb");
            Object[] values = null;
            List<Object> params = null;
            if(sqlInfo!=null){
                sqlInfo = SqlInfo.joinSqlInfo(base,sqlInfo, 2);
                countSql+=" and "+ sqlInfo.getSql();
                sql +=" and "+sqlInfo.getSql();
                values = sqlInfo.getValues();
                params = sqlInfo.getParams();
            }else {
                values = base.getValues();
                params= base.getParams();
            }
            //System.out.println(sqlInfo);
            Long count= daService.getLong(countSql, values);
            List list = null;//daService.getAll(sql, null, 1, 20);
            if(count>0){
                list = daService.getAll(sql + " order by id desc", params, pageNum, pageSize);
            }
            String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
            AjaxUtil.ajaxOutput(response, json);

        }else if(action.equals("create")){
            Integer type = RequestUtil.getInteger(request, "type", -1);
            Integer detailtype = RequestUtil.getInteger(request, "detailtype", -1);
            Long berthsec_id = RequestUtil.getLong(request, "berthsec_id", -1L);
            Long dici_id = RequestUtil.getLong(request, "dici_id", -1L);
            Long inspectid = RequestUtil.getLong(request, "inspectid", -1L);
            String remark =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));
            if(type<0){
                AjaxUtil.ajaxOutput(response,"请选择类型");
                return null;
            }
            if(detailtype<0){
                AjaxUtil.ajaxOutput(response,"请选择任务详情");
                return null;
            }
            if(berthsec_id<0){
                AjaxUtil.ajaxOutput(response,"请选择泊位段");
                return null;
            }
            if(inspectid<0){
                AjaxUtil.ajaxOutput(response,"请分配巡检员");
                return null;
            }
            if (remark.length()>1000){
                AjaxUtil.ajaxOutput(response,"备注太长");
                return null;
            }
            if(type==0||type==1){
                if (dici_id<0){
                    AjaxUtil.ajaxOutput(response,"请选择泊位");
                    return null;
                }
            }
            Map<String, Object> map = pgOnlyReadService.getMap("select nickname from user_info_tb where id = ?", new Object[]{uin});
            if(map!=null&&map.get("nickname")!=null){
                remark = map.get("nickname")+":"+remark+"\n";
            }
            int update = daService.update("insert into inspect_event_tb(create_time,type,berthsec_id,dici_id,inspectid,state,remark,detailtype) values(?,?,?,?,?,?,?,?)", new Object[]{System.currentTimeMillis() / 1000, type, berthsec_id, dici_id, inspectid, 0, remark,detailtype});
            if(update==1){
                putMesgToCache("11",inspectid,"\"need_update\"",memcacheUtils);
            }
            AjaxUtil.ajaxOutput(response,update+"");

        }else if("downinspectpic".equals(action)){
            downinspectPics2Mongodb(request,response);
        }
        return null;
    }
    /**
     * 下载巡场图片
     * @param request
     * @param response
     * @throws Exception
     */
    private void downinspectPics2Mongodb (HttpServletRequest request,HttpServletResponse response) throws Exception{
        long id = RequestUtil.getLong(request, "eventid", -1L);
        System.err.println("downloadinspectPics from mongodb file:id="+id);
        if(id>0){
            DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
            DBCollection collection = db.getCollection("inspectevent_pics");
            BasicDBObject document = new BasicDBObject();
            document.put("inspectevent_id", id);
            //按生成时间查最近的数据
            //	condation.put("ctime", -1);
            //DBCursor objs = collection.find(document).sort(condation).limit(1);
            DBObject obj  = collection.findOne(document);
            if(obj == null){
                AjaxUtil.ajaxOutput(response, "");
                return;
            }
            byte[] content = (byte[])obj.get("content");
            db.requestDone();
            response.setDateHeader("Expires", System.currentTimeMillis()+12*60*60*1000);
            response.setContentLength(content.length);
            response.setContentType("image/jpeg");
            OutputStream o = response.getOutputStream();
            o.write(content);
            o.flush();
            o.close();
            System.out.println("mongdb over.....");
        }else {
            response.sendRedirect("http://sysimages.tq.cn/images/webchat_101001/common/kefu.png");
        }
    }
    /**
     * 写消息到缓存
     * @param mesgtype 消息类型 11巡场事件消息
     * @param key 收消息人编号，巡场员编号
     * @param putmesg 消息内容
     * @param memcacheUtils
     */
    private void putMesgToCache(String mesgtype,Long key,String putmesg, MemcacheUtils memcacheUtils){
        Map<Long, String> messCacheMap = memcacheUtils.doMapLongStringCache("parkuser_messages", null, null);
        String ret = "{\"mtype\":"+mesgtype+",\"info\":"+putmesg+"}";
        if(messCacheMap==null)
            messCacheMap = new HashMap<Long, String>();
        messCacheMap.put(key, ret);
        memcacheUtils.doMapLongStringCache("parkuser_messages", messCacheMap, "update");
    }
}
	
