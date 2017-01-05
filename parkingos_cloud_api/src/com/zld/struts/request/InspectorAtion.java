package com.zld.struts.request;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.zld.AjaxUtil;
import com.zld.CustomDefind;
import com.zld.impl.MongoClientFactory;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
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
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by drh on 2016/5/19.
 * 巡查员接口
 */
public class InspectorAtion extends Action {

    @Autowired
    private DataBaseService daService;
//    @Autowired
//    private LogService logService;
//    @Autowired
//    private PublicMethods publicMethods;
    @Autowired
    private PgOnlyReadService pService;
//    @Autowired
//    private CommonMethods commonMethods;
//    @Autowired
//    private MongoDbUtils mongoDbUtils;

    private Logger logger = Logger.getLogger(InspectorAtion.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String token = RequestUtil.processParams(request, "token");
        String action =RequestUtil.processParams(request, "action");
        String out= RequestUtil.processParams(request, "out");
        Map<String,Object> infoMap = new HashMap<String, Object>();
        Long uin = null;
        Long groupId=null;
        response.setContentType("application/json");
        if(token.equals("")){
            infoMap.put("info", "no token");
        }else {
            Map comMap = pService.getPojo("select * from user_session_tb where token=?", new Object[]{token});
            if(comMap!=null&&comMap.get("comid")!=null){
                uin =(Long) comMap.get("uin");
                groupId =(Long)comMap.get("groupid");
                //authFlag = daService.getLong("select auth_flag from user_info_tb where id =? ", new Object[]{uin});
            }else {
                infoMap.put("info", "token is invalid");
            }
        }
        logger.error("token="+token+",action="+action+",uin="+uin);
		/*token为空或token无效时，返回错误*/
        if(token.equals("")||uin==null){
            AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
            return null;
        }
        String result ="";//返回结果
        if("inspectworkout".equals(action)){//巡查员签退
            result = InspectWorkOut(request, uin);
            //http://127.0.0.1/zld/inspector.do?action=inspectworkout&token=149032374fcb20d719a08acb237cfc4d
        }else if("getinspectevent".equals(action)){//巡查员获取事件
            result = getInspectEvent(request, uin);
            //http://127.0.0.1/zld/inspector.do?action=getinspectevent&token=149032374fcb20d719a08acb237cfc4d
        }else if("getberths".equals(action)){//获取500m内的泊位段的所有泊位
            result = getBerths(request);
            //http://127.0.0.1/zld/inspector.do?action=getinspectevent&token=149032374fcb20d719a08acb237cfc4d
        }else if("queryberthsec".equals(action)){
            //2.搜索泊位段 参数：泊位段编号/名称（keyword） 返回：相关的泊位段列表
            result = queryBerthsec(request,uin);
        }else if("gettaskdetail".equals(action)){
            result = getTaskDetail(request);
            //http://192.168.199.152:8088/zld/inspector.do?action=gettaskdetail&parkid=117&isparkuser=1
        }else if("dealinspect".equals(action)){
            result = dealInspect(request,response,uin);
        }else if("downinspectpic".equals(action)){
            downInspectPics2Mongodb(request, response);
            return null;
        }else if("getinspectdetail".equals(action)){
            result = getInspectDetail(request);
        }else if("getberthdetail".equals(action)){
            result = getBerthDetail(request);
        }
        logger.error(action+":-----------"+result);
        AjaxUtil.ajaxOutput(response, result);
        return null;
    }

    /**
     * 获取车检器的详情
     * @param request
     * @return
     */
    private String getBerthDetail(HttpServletRequest request) {
        Long id = RequestUtil.getLong(request, "id", -1L);
        Map map = pService.getMap("select * from berth_order_tb where id = (select max(id)from berth_order_tb where dici_id = ?)", new Object[]{id});
        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        if(map!=null){
            if(map.get("out_time")!=null){
                resultMap.put("berth_otime",map.get("out_time"));
            }else{
                if(map.get("in_time")!=null){
                    resultMap.put("berth_itime",map.get("in_time"));
                }
            }
        }
        Map orderMap = pService.getMap("select * from order_tb where id = (select max(id)from order_tb where berthnumber = ?)", new Object[]{id});
        if(orderMap!=null){
            if(orderMap.get("end_time")!=null){
                resultMap.put("order_etime",orderMap.get("end_time"));
            }else{
                if(orderMap.get("create_time")!=null){
                    resultMap.put("order_ctime",orderMap.get("create_time"));
                }
            }
        }
        return StringUtils.createJson(resultMap);
    }

    /**
     * 获取事件详情
     * @param request
     * @return
     */
    private String getInspectDetail(HttpServletRequest request) {
        Long id = RequestUtil.getLong(request, "id", -1L);
        Map map = pService.getMap("select * from inspect_event_tb where id = ?", new Object[]{id});
        return StringUtils.createJson(map);
    }

    /**
     * 处理巡场任务
     * @param request
     * @param response
     */
    private String dealInspect(HttpServletRequest request, HttpServletResponse response,Long uin) {
        long type = RequestUtil.getLong(request, "type", -1L);
        Map<String,Object> map = new TreeMap<String,Object>();
        long result = 0;
        String uresult = "";
        String remark =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));
        String inspectorname =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "inspectorname"));
//        Long inspectid = RequestUtil.getLong(request, "inspectid", -1L);
        remark = "  \n"+inspectorname+":"+remark;
        long currTime = System.currentTimeMillis() / 1000;
        if(type==0){//巡场列表事件
            int state = RequestUtil.getInteger(request, "state", -1);
            int eventid = RequestUtil.getInteger(request, "eventid", -1);
            if(state==0){
                state = 2;
            }else {
                state = 1;
            }
            try {
                uresult = uploadinspectPics2Mongodb(request,-1L);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(uresult.startsWith("1")){
                String[] split = uresult.split("-");
                if(split.length==2){
                    String sql = "update inspect_event_tb set end_time=?,inspectid=?, event_pic=?,state=?,remark=remark||? where id=? ";
                    result = daService.update(sql, new Object[]{currTime,uin,split[1],state,remark,eventid});
                }
            }
        }else if(type==1){//收费员
            String typeid = RequestUtil.getString(request, "typeid");
            Long berthsec_id = RequestUtil.getLong(request, "berthsec_id", -1L);
            Long uid = RequestUtil.getLong(request, "uid", -1L);
            String[] split = typeid.split("_");
            int etype = Integer.parseInt(split[0] + "");
            int detaltype = Integer.parseInt(split[1]+"");
            Long id = daService.getLong(
                    "SELECT nextval('seq_inspect_event_tb'::REGCLASS) AS newid", null);
            result = daService.update("insert into inspect_event_tb(id,create_time,end_time,type,berthsec_id,uid,inspectid,state,remark,detailtype) values(?,?,?,?,?,?,?,?,?,?)",
                    new Object[]{id, currTime,currTime, etype, berthsec_id, uid, uin, 1, remark, detaltype});

            try {
                if(result==1){
                    uresult = uploadinspectPics2Mongodb(request,id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(uresult.startsWith("1")){
                String[] uresults = uresult.split("-");
                if(split.length==2){
                    String sql = "update inspect_event_tb set event_pic=? where id=? ";
                    result = daService.update(sql, new Object[]{uresults[1],id});
                }
            }
        }else if(type==2){//泊位
            String typeid = RequestUtil.getString(request, "typeid");
            Long uid = RequestUtil.getLong(request, "uid", -1L);
            Long dici_id = RequestUtil.getLong(request, "dici_id", -1L);
            Long berthsec_id = RequestUtil.getLong(request, "berthsec_id", -1L);
            String[] split = typeid.split("_");
            int etype = Integer.parseInt(split[0] + "");
            int detaltype = Integer.parseInt(split[1] + "");
            Long id = daService.getLong(
                    "SELECT nextval('seq_inspect_event_tb'::REGCLASS) AS newid", null);
            result = daService.update("insert into inspect_event_tb(id,create_time,end_time,type,berthsec_id,uid,dici_id,inspectid,state,remark,detailtype) values(?,?,?,?,?,?,?,?,?,?,?)",
                    new Object[]{id, currTime,currTime, etype, berthsec_id, uid,dici_id, uin, 1, remark, detaltype});
            try {
                if(result==1){
                    uresult = uploadinspectPics2Mongodb(request,id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(uresult.startsWith("1")){
                String[] uresults = uresult.split("-");
                if(split.length==2){
                    String sql = "update inspect_event_tb set event_pic=? where id=? ";
                    result = daService.update(sql, new Object[]{uresults[1], id});
                }
            }
        } else if (type ==3){//

        }
        map.put("result", result);
        map.put("uploadresult", uresult);
        return StringUtils.createJson(map);
    }

    /**
     * 下载巡场图片
     * @param request
     * @param response
     * @throws Exception
     */
    private void downInspectPics2Mongodb (HttpServletRequest request,HttpServletResponse response) throws Exception{
        logger.error("download from mongodb....");
        long id = RequestUtil.getLong(request,"eventid",-1L);
        System.err.println("downloadinspectPics from mongodb file:id=" + id);
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
                logger.error("取图片错误.....");
                return;
            }
            byte[] content = (byte[])obj.get("content");
            logger.error("取图片成功.....大小:"+content.length);
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
     * 上传巡场图片
     * @param request
     * @throws Exception
     */
    private String uploadinspectPics2Mongodb (HttpServletRequest request,Long eventid) throws Exception{
        logger.error("begin upload inspectevent picture....");
        long inspectevent_id = RequestUtil.getLong(request,"eventid",-1L);
        if(eventid>0){
            inspectevent_id = eventid;
        }
        Map<String, String> extMap = new HashMap<String, String>();
        extMap.put(".jpg", "image/jpeg");
        extMap.put(".jpeg", "image/jpeg");
        extMap.put(".png", "image/png");
        extMap.put(".gif", "image/gif");
        request.setCharacterEncoding("UTF-8"); // 设置处理请求参数的编码格式
        DiskFileItemFactory factory = new DiskFileItemFactory(); // 建立FileItemFactory对象
        factory.setSizeThreshold(16*4096*1024);
        ServletFileUpload upload = new ServletFileUpload(factory);
        // 分析请求，并得到上传文件的FileItem对象
        upload.setSizeMax(16*4096*1024);
        List<FileItem> items = null;
        try {
            items =upload.parseRequest(request);
        } catch (FileUploadException e) {
            e.printStackTrace();
            return "-1";
        }
        String filename = ""; // 上传文件保存到服务器的文件名
        InputStream is = null; // 当前上传文件的InputStream对象
        // 循环处理上传文件
        String comId = "";
        String orderId = "";
        for (FileItem item : items){
            // 处理普通的表单域
            if (item.isFormField()){
                if(item.getFieldName().equals("comid")){
                    if(!item.getString().equals(""))
                        comId = item.getString("UTF-8");
                }else if(item.getFieldName().equals("orderid")){
                    if(!item.getString().equals("")){
                        orderId = item.getString("UTF-8");
                    }
                }

            }else if (item.getName() != null && !item.getName().equals("")){// 处理上传文件
                // 从客户端发送过来的上传文件路径中截取文件名
                logger.error(item.getName());
                filename = item.getName().substring(
                        item.getName().lastIndexOf("\\")+1);
                is = item.getInputStream(); // 得到上传文件的InputStream对象

            }
        }
        if(inspectevent_id<-1){
            return "-1";
        }
        String file_ext =filename.substring(filename.lastIndexOf(".")).toLowerCase();// 扩展名
        String picurl = inspectevent_id+ "_" + System.currentTimeMillis()/1000 + file_ext;
        BufferedInputStream in = null;
        ByteArrayOutputStream byteout =null;
        try {
            in = new BufferedInputStream(is);
            byteout = new ByteArrayOutputStream(1024);

            byte[] temp = new byte[1024];
            int bytesize = 0;
            while ((bytesize = in.read(temp)) != -1) {
                byteout.write(temp, 0, bytesize);
            }

            byte[] content = byteout.toByteArray();
            DB mydb = MongoClientFactory.getInstance().getMongoDBBuilder("zld");
            mydb.requestStart();

            DBCollection collection = mydb.getCollection("inspectevent_pics");
            //  DBCollection collection = mydb.getCollection("records_test");

            BasicDBObject document = new BasicDBObject();
            document.put("inspectevent_id", inspectevent_id);
            document.put("ctime",  System.currentTimeMillis()/1000);
            document.put("type", extMap.get(file_ext));
            document.put("content", content);
            document.put("filename", picurl);
            //开始事务
            mydb.requestStart();
            collection.insert(document);
            //结束事务
            mydb.requestDone();
            in.close();
            is.close();
            byteout.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "-1";
        }finally{
            if(in!=null)
                in.close();
            if(byteout!=null)
                byteout.close();
            if(is!=null)
                is.close();
        }

        return "1-"+picurl;
    }
    /**
     * 查询泊位段
     * @param request
     * @return
     */

    private String getTaskDetail(HttpServletRequest request) {
        Long id = RequestUtil.getLong(request, "parkid", -1L);
        Integer isparkuser = RequestUtil.getInteger(request, "isparkuser", 0);
        ArrayList<Map<String,Object>> list0 = new ArrayList<Map<String,Object>>();
        ArrayList<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();
        ArrayList<Map<String,Object>> list2 = new ArrayList<Map<String,Object>>();
        String taskdetail0 = CustomDefind.taskdetail0;
        String taskdetail1 = CustomDefind.taskdetail1;
        String taskdetail2 = CustomDefind.taskdetail2;
//        String types[] = CustomDefind.TASKTYPE.split("\\|");
        String res0[] = taskdetail0.split("\\|");
        String res1[] = taskdetail1.split("\\|");
        String res2[] = taskdetail2.split("\\|");
        if(isparkuser==0){
            Map<String, Object> map = pService.getMap("select * from com_park_tb c,dici_tb d where c.id = ? " +
            		" and c.dici_id = d.id and d.is_delete=? ", new Object[]{id, 0});
            if(map!=null&&map.size()>0){
                if(taskdetail0!=null){
                    for(int i=0;i<res0.length;i++){
                        Map<String,Object> map0 = new TreeMap<String,Object>();
                        map0.put("id",0+"_"+i);
                        map0.put("value",res0[i]);
                        list0.add(map0);
                    }
                }
                if(taskdetail1!=null){
                    for(int i=0;i<res1.length;i++){
                        Map<String,Object> map1 = new TreeMap<String,Object>();
                        map1.put("id",1+"_"+i);
                        map1.put("value",res1[i]);
                        list1.add(map1);
                    }
                }
            }else{
//                String key1 = "TASKDETAIl1";
//                String taskdetail1 = CustomDefind.getValue(key1);
                if(taskdetail1!=null){
//                    String res[] = taskdetail1.split("\\|");
                    for(int i=0;i<res1.length;i++){
                        Map<String,Object> map1 = new TreeMap<String,Object>();
                        map1.put("id",1+"_"+i);
                        map1.put("value",res1[i]);
                        list1.add(map1);
                    }
                }
            }
        }else{
            if(taskdetail2!=null){
                for(int i=0;i<res2.length;i++){
                    Map<String,Object> map2 = new TreeMap<String,Object>();
                    map2.put("id",2+"_"+i);
                    map2.put("value",res2[i]);
                    list2.add(map2);
                }
            }
        }
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"dici\":").append(StringUtils.createJson(list0));
        sb.append(",").append("\"berth\":").append(StringUtils.createJson(list1));
        sb.append(",").append("\"parkuser\":").append(StringUtils.createJson(list2));
        sb.append("}");
        return sb.toString();
    }

    /**
     * 查询泊位段
     * @param request
     * @return
     */
    private String queryBerthsec(HttpServletRequest request, Long uin) {
        List list = pService.getAll("select u.id,u.berthsec_name from work_berthsec_tb b,work_inspector_tb i,com_berthsecs_tb u  where  b.inspect_group_id = i.inspect_group_id" +
                " and u.id = b.berthsec_id and i.inspector_id = ? group by u.id,u.berthsec_name", new Object[]{uin});
        return StringUtils.createJson(list);
    }

    /**
     * 巡查员获取泊位列表  参数：经度，纬度，泊位段id  返回：500米范围内最近的泊位段泊位列表，没有的话返回空list
     * @param request
     * @return
     */
    private String getBerths(HttpServletRequest request) {
        Double lon = RequestUtil.getDouble(request, "lng", 0d);
        Double lat = RequestUtil.getDouble(request, "lat", 0d);
        long id = RequestUtil.getLong(request, "berthsec_id", -1L);
        String result = "{}";
        if(id<0){
            List<Map<String, Object>> list = getBerths500mList(lat, lon);
            double d  =0d;
            if(list!=null&&list.size()>0) {
                logger.error(">>>>>>>>>getBerths500m,lng=" + lon + ",lat=" + lat + ",return size:" + list.size());
                id = -1;
                for (Map<String, Object> map : list) {
                    double lon2 = Double.valueOf(map.get("longitude") + "");
                    double lat2 = Double.valueOf(map.get("latitude") + "");
                    long idtemp = Long.parseLong(map.get("id") + "");
                    double distance = StringUtils.distanceByLnglat(lon, lat, lon2, lat2);
                    if (distance <= d || d < 0) {
                        id = idtemp;
                    }
                }
            }
        }
        if (id > 0) {
//            List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
            List<Map<String, Object>> Berthslist = pService.getAll("select c.id,cid as ber_name,o.id as orderid,o.car_number,o.prepaid as prepay,o.c_type as ismonthuser  from com_park_tb c " +
                    "left join order_tb o on c.order_id= o.id where c.is_delete=? and  c.berthsec_id=? order by c.cid", new Object[]{0, id});
            Map userMap = pService.getMap("select p.uid,u.nickname from parkuser_work_record_tb p,user_info_tb u where p.berthsec_id = ? and p.end_time is null and p.uid = u.id order by p.id desc",
                    new Object[]{id});
            long uid = -1;
            String nickname = "";
            if(userMap!=null&&userMap.get("uid")!=null){
                uid = Long.parseLong(userMap.get("uid")+"");
                nickname = userMap.get("nickname")+"";
            }
//            ArrayList berthlist = new ArrayList();
//            List orderlist = new ArrayList();
//            for(Map<String,Object> berthMap : Berthslist){
//                Long dici_id = (Long)berthMap.get("id");
//                berthlist.add(dici_id);
//                orderlist.add(dici_id);
//            }
//            try{
//                String berthsql2 = "";
//                for (int i = 0; i < berthlist.size(); i++) {
//                    berthsql2+="?,";
//                }
//                if(berthsql2.endsWith(",")){
//                    berthsql2 = berthsql2.substring(0,berthsql2.length()-1);
//                }
//                String berthsql1 = "select * from berth_order_tb o,(select max(id)id,dici_id from berth_order_tb where dici_id in (";
//                String berthsql3 = ") group by dici_id) t where o.id = t.id";
//                List<Map<String, Object>> timeList1 = new ArrayList<Map<String, Object>>();
//                if(berthsql2.length()>0){
//                    timeList1 = daService.getAll(berthsql1+berthsql2+berthsql3,berthlist,1,Integer.MAX_VALUE);
//                }
//                String ordersql1 = "select * from order_tb o,(select max(id)id,berthnumber from order_tb where berthnumber in (";
//                String ordersql3 = ") group by berthnumber) t where o.id = t.id";
//                List<Map<String, Object>> timeList2 = new ArrayList<Map<String, Object>>();
//                if(berthsql2.length()>0){
//                    timeList2 = daService.getAll(ordersql1+berthsql2+ordersql3,orderlist,1,Integer.MAX_VALUE);
//                }
//
//
//                for(Map<String,Object> berthMap : Berthslist){
//                    Long dici_id = Long.parseLong(berthMap.get("id") + "");
//                    for(Map timemap : timeList1){
//                        long timeid = Long.parseLong(timemap.get("dici_id")+"");
//                        if(dici_id==timeid){
//                            if(timemap.get("out_time")!=null){
//                                berthMap.put("berthotime",timemap.get("out_time"));
//                            }else{
//                                if(timemap.get("in_time")!=null){
//                                    berthMap.put("berthitime",timemap.get("in_time"));
//                                }
//                            }
//                        }
//                    }
//                    for(Map timemap : timeList2){
//                        long timeid = Long.parseLong(timemap.get("berthnumber")+"");
//                        if(dici_id==timeid){
//                            if(timemap.get("end_time")!=null){
//                                berthMap.put("orderetime",timemap.get("end_time"));
//                            }else{
//                                if(timemap.get("create_time")!=null){
//                                    berthMap.put("orderctime",timemap.get("create_time"));
//                                }
//                            }
//                        }
//                    }
//                    resultList.add(berthMap);
//                }
//            }catch (Exception e){
//                logger.error(e.getMessage());
//            }

            result = "{\"uid\":\""+uid+"\",\"nickname\":\""+nickname+"\",\"berthsec_id\":\""+id+"\",\"berths\":"+StringUtils.createJson(Berthslist)+"}";
        }
        return result;
    }

    /**
     * 获取巡查组的所有事件
     * @param request
     * @param uin 巡查员编号
     * @return
     */
    private String getInspectEvent(HttpServletRequest request, Long uin) {
        Integer pageNum = RequestUtil.getInteger(request, "page", 1);
        Integer pageSize = RequestUtil.getInteger(request, "size", 20);
        Integer state = RequestUtil.getInteger(request, "state", 0);
        List param = new ArrayList();
        param.add(uin);
        param.add(0);
        param.add(state);
        String sql = "";
        if(state==0){
            sql = "select i.*,c.berthsec_name,b.company_name from inspect_event_tb i ,com_berthsecs_tb c,com_info_tb b where i.berthsec_id in(select berthsec_id from work_berthsec_tb w ," +
                    " work_inspector_tb i where i.inspector_id = ? and w.inspect_group_id = i.inspect_group_id and i.state=?) and i.state=? and c.comid = b.id" +
                    " and i.berthsec_id=c.id order by i.create_time desc";
        }else{
            sql = "select i.*,c.berthsec_name,b.company_name from inspect_event_tb i ,com_berthsecs_tb c,com_info_tb b where i.berthsec_id in(select berthsec_id from work_berthsec_tb w ," +
                    " work_inspector_tb i where i.inspector_id = ? and w.inspect_group_id = i.inspect_group_id and i.state=? ) and (i.state=? or i.state=?) and c.comid = b.id" +
                    " and i.berthsec_id=c.id order by i.end_time desc";
            param.add(2);//处理失败
        }
        List<Map<String,Object>> list = pService.getAll(sql,param,pageNum,pageSize);
        String taskdetail0 = CustomDefind.taskdetail0;
        String taskdetail1 = CustomDefind.taskdetail1;
        String taskdetail2 = CustomDefind.taskdetail2;
        String types[] = CustomDefind.TASKTYPE.split("\\|");
        String res0[] = taskdetail0.split("\\|");
        String res1[] = taskdetail1.split("\\|");
        String res2[] = taskdetail2.split("\\|");
        for(Map map : list){
            //处理客户端显示的内容   泊位段-任务类别-任务详情
            int type = Integer.parseInt(map.get("type")+"");
            int detailtype = Integer.parseInt(map.get("detailtype")+"");
            if(type==0&&detailtype>=0){
                long dici_id = Long.parseLong(map.get("dici_id")+"");
                String cid = "";
                Map<String, Object> parkMap = pService.getMap("select cid from com_park_tb where id = ?", new Object[]{dici_id});
                if(parkMap!=null&& parkMap.get("cid")!=null){
                    cid = parkMap.get("cid")+"";
                }
                map.put("typename", types[0]+cid);
                map.put("detaltypename", res0[detailtype]);
            }else if (type==1&&detailtype>=0){
                long dici_id = Long.parseLong(map.get("dici_id")+"");
                String cid = "";
                Map<String, Object> parkMap = pService.getMap("select cid from com_park_tb where id = ?", new Object[]{dici_id});
                if(parkMap!=null&& parkMap.get("cid")!=null){
                    cid = parkMap.get("cid")+"";
                }
                map.put("typename", types[1]+cid);
                map.put("detaltypename", res1[detailtype]);
            }else if(type==2&&detailtype>=0){
                String uid = map.get("uid")+"";
                if(uid.equals("-1")){
                    uid = "";
                }
                map.put("typename", types[2]+uid);
                map.put("detaltypename", res2[detailtype]);
            }else{
                map.put("typename", "未知类型");
                map.put("detaltypename", "未知类型");
            }
        }
//        List list = daService.getAll("select * from inspect_event_tb where inspectid =? and state=? order by end_time,create_time desc",
//                param,pageNum,pageSize);
        String result = StringUtils.createJson(list);
//        logger.error("inspector uid:"+uin+",return inspect_event result:"+result);
        return result;
    }

    /**
     *
     * @param request
     * @param uin 巡查员编号
     * @return
     */
    private String InspectWorkOut(HttpServletRequest request, Long uin) {
    	try {
    		int ret = 0;
        	List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
        	//签退操作
    		Map<String, Object> workRecordSqlMap = new HashMap<String, Object>();
    		workRecordSqlMap.put("sql", "update parkuser_work_record_tb set end_time=?,state=? where uid =? and end_time is null and state=? ");
    		workRecordSqlMap.put("values", new Object[]{System.currentTimeMillis() / 1000, 1, uin, 0});
    		bathSql.add(workRecordSqlMap);
    		//标为离线
    		Map<String, Object> onlineSqlMap = new HashMap<String, Object>();
    		onlineSqlMap.put("sql", "update user_info_tb set online_flag=? where id=? ");
    		onlineSqlMap.put("values", new Object[]{21, uin});
    		bathSql.add(onlineSqlMap);
    		boolean b = daService.bathUpdate2(bathSql);
    		if(b){
    			ret = 1;
    		}
            logger.error("inspectworkout 签退:" + ret);
            Map result = new HashMap<String,String>();
            result.put("result", ret);
            if(ret==0){
                result.put("mes","不可重复签退");
            } else {
            	result.put("mes","签退成功");
            }
            return StringUtils.createJson(result);
		} catch (Exception e) {
			logger.error("", e);
		}
    	return null;
    }

    /**
     * 取附近500m泊位段
     * @param lat
     * @param lon
     * @return 2000以内的停车场
     */
    public List<Map<String, Object>> getBerths500mList(Double lat,Double lon){
        //500ms的偏移量
        double lon1 = 0.009536;
        double lat1 = 0.007232;
        String sql = "select * from com_berthsecs_tb where " +
                "longitude between ? and ? " +
                "and latitude between ? and ? and " +
                "is_active=?";//and isfixed=? ";
        List<Object> params = new ArrayList<Object>();
        params.add(lon-lon1);
        params.add(lon+lon1);
        params.add(lat-lat1);
        params.add(lat+lat1);
        params.add(0);
        List list = null;//daService.getPage(sql, null, 1, 20);
        list = pService.getAll(sql, params, 0, 0);
        return list;
    }
}
