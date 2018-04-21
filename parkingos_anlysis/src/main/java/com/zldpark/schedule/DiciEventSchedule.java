package com.zldpark.schedule;

import com.alibaba.fastjson.JSONObject;
import com.zldpark.service.DataBaseService;
import com.zldpark.service.PgOnlyReadService;
import com.zldpark.utils.HttpProxy;
import com.zldpark.utils.MemcacheUtils;
import com.zldpark.utils.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by drh on 2016/5/9.
 * 用于定时分析车检器上报的情况  处理后生成消息推送给巡查员
 */
public class DiciEventSchedule implements Runnable {
    DataBaseService dataBaseService;
    MemcacheUtils memcacheUtils;
    PgOnlyReadService pgOnlyReadService;

    public DiciEventSchedule(DataBaseService dataBaseService,PgOnlyReadService pgOnlyReadService,MemcacheUtils memcacheUtils ){
        this.dataBaseService = dataBaseService;
        this.memcacheUtils = memcacheUtils;
        this.pgOnlyReadService = pgOnlyReadService;
    }

    private static Logger log = Logger.getLogger(DiciEventSchedule.class);
    private static HttpProxy http = new HttpProxy();

    @Override
    public void run() {
        try {
            long bt = System.currentTimeMillis();
            log.error("DiciEventSchedule开始时间："+bt);
            long currTime = System.currentTimeMillis() / 1000;
            long btime = currTime - 660;
            long etime = currTime - 600;
            List inList = pgOnlyReadService.getAll("select b.*,d.id berthsec_id from berth_order_tb b,com_park_tb c,com_berthsecs_tb d where b.in_time >= ? and b.in_time<?  and b.orderid=? and b.state=? and b.dici_id=c.id " +
                            " and c.berthsec_id = d.id",
                    new Object[]{btime, etime, -1,0});
            log.error("有车未录size："+inList.size());
            if(inList!=null&&inList.size()>0){
                //没有生成订单
                for (Object obj : inList){
                    Map inMap = (Map) obj;
                    String berthid = inMap.get("berthsec_id")+"";
                    String id = inMap.get("orderid") + "";
                    log.error("berthid:"+berthid+",orderid:"+id);
                    if(!berthid.equals("")&&!berthid.equals("null")){
                        long berthsec_id = Long.parseLong(berthid);
                        long workuid = queryuid(berthsec_id);
                        log.error("berthsec_id:" + berthsec_id + ",workuid:" + workuid);
                        if (workuid > 0) {
                            List<Map<String, Object>> list = pgOnlyReadService.getAll("select i.inspector_id uid from work_berthsec_tb b,user_info_tb u,work_inspector_tb i " +
                                            " where b.berthsec_id = ?  and b.inspect_group_id = i.inspect_group_id and i.inspector_id = u.id and u.auth_flag = ? and b.is_delete=? and i.state=?",
                                    new Object[]{berthsec_id, 16, 0, 0});
                            int r = dataBaseService.update("insert into inspect_event_tb(create_time,type,berthsec_id,dici_id,uid,state,detailtype) values(?,?,?,?,?,?,?)",
                                    new Object[]{System.currentTimeMillis() / 1000, 1, berthsec_id, (Long) inMap.get("dici_id"), workuid, 0, 0});
                            for (Map map : list) {
                                long uid = Long.parseLong(map.get("uid") + "");
                                if (r == 1) {
                                    putMesgToCache("11", uid, "\"need_update\"", memcacheUtils);
                                }
                            }
                        }
                    }
                }
            }
//            List outList = dataBaseService.getAll("select * from order_tb o,berth_order_tb b  where b.state=? and o.end_time>=? and o.end_time<?  and b.orderid = o.id and o.state=? ",
//                    new Object[]{0,etime,btime,1});
            List outList = pgOnlyReadService.getAll("select b.*,o.berthsec_id from berth_order_tb b ,order_tb o where  b.out_time >= ? and b.out_time<? and b.orderid=o.id and o.state=?",
                    new Object[]{btime,etime,0});
            log.error("有车未结（order）size："+outList.size());
            if(outList!=null&&outList.size()>0){
                //没有结算订单
                for (Object obj : outList) {
                    Map inMap = (Map) obj;
                    String berthid = inMap.get("berthsec_id")+"";
                    String id = inMap.get("orderid") + "";
                    log.error("berthid:"+berthid+",orderid:"+id);
                    if(!berthid.equals("")&&!berthid.equals("null")){
                        long berthsec_id = Long.parseLong(berthid);
                        long workuid = queryuid(berthsec_id);
                        log.error("berthsec_id:"+berthsec_id+",workuid:"+workuid);
                        if (workuid > 0) {
                            List<Map<String, Object>> list = pgOnlyReadService.getAll("select i.inspector_id uid from work_berthsec_tb b,user_info_tb u,work_inspector_tb i " +
                                            " where b.berthsec_id = ?  and b.inspect_group_id = i.inspect_group_id and i.inspector_id = u.id and u.auth_flag = ? and b.is_delete=? and i.state=?",
                                    new Object[]{berthsec_id, 16, 0, 0});
                            int r = dataBaseService.update("insert into inspect_event_tb(create_time,type,berthsec_id,dici_id,inspectid,uid,state,detailtype) values(?,?,?,?,?,?,?,?)",
                                    new Object[]{System.currentTimeMillis() / 1000, 1, berthsec_id, (Long)inMap.get("dici_id"),  (Long) inMap.get("uid"),workuid, 0, 1});
                            for (Map map : list) {
                                long uid = Long.parseLong(map.get("uid") + "");
                                if (r == 1) {
                                    putMesgToCache("11", uid, "\"need_update\"", memcacheUtils);
                                }
                            }
                        }
                        //车检器订单结算，pos订单置为未缴
                        try {
                            Map userMap = pgOnlyReadService.getMap("select p.uid from parkuser_work_record_tb p,collector_set_tb c,user_info_tb u where p.berthsec_id =? and p.end_time is null and p.state = ? and p.uid = u.id and u.role_id = c.role_id and c.is_sensortime = ?"
                                    , new Object[]{berthsec_id, 0, 0});
                            if(userMap!=null&&userMap.get("uid")!=null){
                                long uid = Long.parseLong(userMap.get("uid")+"");
                                long out_uid = Long.parseLong(inMap.get("out_uid") + "");
                                long comid = Long.parseLong(inMap.get("comid") + "");
                                long endtime = System.currentTimeMillis()/1000;
                                long orderid = Long.parseLong(inMap.get("orderid") + "");
                                long berthorderid = getBerthOrderId(orderid);
                                String url =  "http://s.tingchebao.com/zld/collectorrequest.do?action=orderdetail&token=notoken"
                                        + "&orderid=" + orderid
                                        + "&brethorderid=" + berthorderid
                                        + "&comid=" + comid
                                        + "&uin=" + out_uid
                                        + "&out=json";
                                String result = http.doGet(url);
                                log.error("url:"+url+",result:"+result);
//                            result.charAt()
                                JSONObject jo = JSONObject.parseObject(result);

                                log.error(jo.toString());
                                if(jo.get("total")!=null&& StringUtils.isDouble(jo.get("total")+"")){
                                    log.error("开始置为未缴，orderid:" + orderid + ",out_uid:" + out_uid + ",total:" + jo.getDouble("total"));
                                    if(workuid<0){
                                    	escape(orderid,uid,jo.getDouble("total"),endtime);
                                    }
                                    
                                }
                            }else{
                                log.error("没有收费员在上班或者收费收费设置不支持");
                            }
                        }catch (Exception e){
                            log.error("-----------------定时置为未缴异常"+e.getMessage()+"--------------------");
                        }
                    }
                }
            }
            List outOrderList = pgOnlyReadService.getAll("select * from berth_order_tb b ,order_tb o where b.state=? and o.end_time >=?  and o.end_time<? " +
                            " and b.orderid=o.id and o.state=? and b.orderid >?",
                            new Object[]{0,btime,etime,1,-1});
            log.error("有车未结（berth）size："+outList.size());
            if(outOrderList!=null&&outOrderList.size()>0){
                //没有结算订单
                for (Object obj : outOrderList) {
                    Map inMap = (Map) obj;
                    String berthid = inMap.get("berthsec_id") + "";
                    String orderid = inMap.get("orderid")+"";
                    log.error("berthid:"+berthid+",orderid:"+orderid);
                    if(!berthid.equals("")&&!berthid.equals("null")){
                        long berthsec_id = Long.parseLong(berthid);
                        long workuid = queryuid(berthsec_id);
                        log.error("berthsec_id:" + berthsec_id + ",workuid:" + workuid);
                        if (workuid > 0) {
                            List<Map<String, Object>> list = pgOnlyReadService.getAll("select i.inspector_id uid from work_berthsec_tb b,user_info_tb u,work_inspector_tb i " +
                                            " where b.berthsec_id = ?  and b.inspect_group_id = i.inspect_group_id and i.inspector_id = u.id and u.auth_flag = ? and b.is_delete=? and i.state=?",
                                    new Object[]{berthsec_id, 16, 0, 0});
                            int r = dataBaseService.update("insert into inspect_event_tb(create_time,type,berthsec_id,dici_id,inspectid,uid,state,detailtype) values(?,?,?,?,?,?,?,?)",
                                    new Object[]{System.currentTimeMillis() / 1000, 1, berthsec_id, (Long) inMap.get("dici_id"), (Long) inMap.get("uid"), workuid, 0, 1});

                            for (Map map : list) {
                                long uid = Long.parseLong(map.get("uid") + "");
                                if (r == 1) {
                                    putMesgToCache("11", uid, "\"need_update\"", memcacheUtils);
                                }
                            }
                        }
                    }
                }
            }
            long et = System.currentTimeMillis();
            log.error("DiciEventSchedule开始时间："+et+",耗时："+(et-bt));
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
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
    /**
     *查看在岗收费员
     */
    private long queryuid(Long berthsec_id){
        Map map = pgOnlyReadService.getMap("select * from  parkuser_work_record_tb  where berthsec_id =? and end_time is null and state=?",
                new Object[]{berthsec_id, 0});
        if(map!=null&&map.size()>0){
            return Long.parseLong(map.get("uid")+"");
        }
        return -1;
    }
    /**
     * 置为逃单
     * @param orderid	POS机订单编号
     * @param uid	置为逃单的收费员编号
     * @param money	总金额
     * @return
     */
    public int escape(Long orderid, Long uid, Double money,Long endtime){
        int result = -1;
        try {
            Map<String, Object> orderMap = pgOnlyReadService.getMap("select * from order_tb where id=? and state=? ",
                    new Object[]{orderid, 0});
            if(orderMap != null){
                Long ntime = System.currentTimeMillis()/1000;
                if(endtime>0){
                    ntime = endtime;
                }
                Long brethorderid = getBerthOrderId(orderid);
                Long end_time = getSensorTime(brethorderid, 1, uid, ntime);
                Long comId = (Long)orderMap.get("comid");
                List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
                //更新订单状态，收费成功
                Map<String, Object> escapeSqlMap = new HashMap<String, Object>();
                Map<String, Object> berthSqlMap = new HashMap<String, Object>();
                Map<String, Object> orderSqlMap = new HashMap<String, Object>();
                escapeSqlMap.put("sql", "insert into no_payment_tb (create_time,end_time,order_id,total,car_number,uin,comid,uid)" +
                        "values(?,?,?,?,?,?,?,?)");
                escapeSqlMap.put("values", new Object[]{orderMap.get("create_time"),end_time,orderid,money
                        ,orderMap.get("car_number"),orderMap.get("uin"),orderMap.get("comid"),uid});
                bathSql.add(escapeSqlMap);
                Long berthId = -1L;
                if(orderMap.get("berthnumber") != null){
                    berthId = (Long)orderMap.get("berthnumber");
                }
                if(berthId != null && berthId >0){
                    berthSqlMap.put("sql", "update com_park_tb set state=?,order_id=? where id =? and order_id=? ");
                    berthSqlMap.put("values", new Object[]{0,null,berthId,orderid});
                    bathSql.add(berthSqlMap);
                }
                orderSqlMap.put("sql", "update order_tb set state=?,total=?,end_time=? where id =?");
                orderSqlMap.put("values", new Object[]{2,money,end_time,orderid});
                bathSql.add(orderSqlMap);
                boolean b = dataBaseService.bathUpdate2(bathSql);
                log.error("orderid:"+orderid+",uid:"+uid+",money:"+money+"(update com_park_tb orderid) bathsql result:"+b);
                if(b){
                    result = 1;
                    try {
                        updateRemainBerth(comId, 1);
                    } catch (Exception e) {
                        log.error("update remain error>>>orderid"+orderid+",uid:"+uid+",money:"+money, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("escapeorderid:"+orderid+"", e);
        }

        return result;
    }
    /**
     * 根据POS机订单获取车检器订单编号，如果查到有绑定车检器订单就取绑定的车检器订单编号
     * @param orderId	POS机订单编号
     * @return
     */
    public Long getBerthOrderId(Long orderId){
        Long berthOrderId = -1L;
        try {
            log.error("getBerthOrderId>>>orderId:"+orderId);
            if(orderId != null && orderId > 0){
                Map<String, Object> berthOrderMap = pgOnlyReadService.getMap("select b.id from berth_order_tb b,order_tb o " +
                        " where b.orderid=o.id and o.id=? order by in_time desc limit ? ", new Object[]{orderId, 1});
                if(berthOrderMap != null){
                    berthOrderId = (Long)berthOrderMap.get("id");
                }
            }
            log.error("getBerthOrderId>>>orderId:"+orderId+",berthOrderId:"+berthOrderId);
        } catch (Exception e) {
            log.error("getBerthOrderId>>>orderId:" + orderId + ",berthOrderId:" + berthOrderId, e);
        }
        return berthOrderId;
    }
    /**
     * 获取车检器进出场作为POS机订单出入场时间
     * @param berthOrderId 车检器订单编号
     * @param type 0：进场 1：出场
     * @param uid 收费员编号
     * @param curtime 当前系统时间
     * @return
     */
    public Long getSensorTime(Long berthOrderId, Integer type, Long uid, Long curtime){
        log.error("getSensorTime>>>berthOrderId:"+berthOrderId+",type:"+type+",uid:"+uid+",curtime:"+curtime);
        Long sensortime = curtime;
        try {
            if(uid != null && uid > 0){
                Map<String, Object> setMap = pgOnlyReadService.getMap("select is_sensortime from collector_set_tb s,user_info_tb u " +
                        " where s.role_id=u.role_id and u.id=? ", new Object[]{uid});
                if(setMap != null){
                    Integer is_sensortime = (Integer)setMap.get("is_sensortime");
                    log.error("getSensorTime>>>berthOrderId:"+berthOrderId+",is_sensortime:"+is_sensortime);
                    if(is_sensortime == 1){
                        return sensortime;
                    }
                }
                if(berthOrderId != null && berthOrderId > 0){
                    Map<String, Object> map = pgOnlyReadService.getMap("select * from berth_order_tb " +
                            " where id=? ", new Object[]{berthOrderId});
                    log.error("getSensorTime>>>berthOrderId:"+berthOrderId+",map:"+map);
                    if(map != null){
                        if(type == 0){
                            if(map.get("in_time") != null){
                                Long in_time = (Long)map.get("in_time");
                                if(curtime > in_time && (curtime - in_time < 30 * 60)){
                                    sensortime = in_time;
                                }else{
                                    log.error("berthOrderId:"+berthOrderId+",uid:"+uid+",curtime:"+curtime+",in_time:"+in_time);
                                }
                            }
                        }else if(type == 1){
                            if(map.get("out_time") != null){
                                Long out_time = (Long)map.get("out_time");
                                if(curtime > out_time && (curtime - out_time < 30 * 60)){
                                    sensortime = out_time;
                                }else{
                                    log.error("berthOrderId:"+berthOrderId+",uid:"+uid+",curtime:"+curtime+",out_time:"+out_time);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("getSensorTime>>>berthOrderId:"+berthOrderId,e);
        }
        return sensortime;
    }
    /**
     * 获取泊位剩余数
     * @param comid
     * @param type 0：入场 1：出场
     * @return
     */
    public Long updateRemainBerth(Long comid, Integer type){
        log.error("remain berth>>>comid:"+comid+",type:"+type);
        Long ntime = System.currentTimeMillis()/1000;
        Map<String, Object> comMap = pgOnlyReadService.getMap("select parking_total,share_number,invalid_order from com_info_tb where id=? and parking_type<>? and etc=? ",
                new Object[]{comid, 2, 2});//parking_type -- 车位类型，0地面，1地下，2占道 3室外 4室内 5室内外
        if(comMap == null){
            return null;
        }
        Integer parking_total = 0;
        Integer share_number = 0;
        Long invalid_order = 0L;//未结算的垃圾订单数
        if(comMap.get("parking_total") != null){
            parking_total = (Integer)comMap.get("parking_total");
        }
        if(comMap.get("share_number") != null){
            share_number = (Integer)comMap.get("share_number");
        }
        if(comMap.get("invalid_order") != null){
            invalid_order = (Long)comMap.get("invalid_order");
        }
        if(share_number == 0){
            share_number = parking_total;
        }
        log.error("park info>>>comid:" + comid + ",parking_total:" + parking_total + ",share_number:" + share_number + ",invalid_order:" + invalid_order);
        Map<String, Object> remainMap = pgOnlyReadService.getMap("select amount from remain_berth_tb where comid=? and state=? and berthseg_id<? limit ?",
                new Object[]{comid, 0, 0, 1});
        Long remain = 0L;
        if(remainMap != null){
            remain = (Long)remainMap.get("amount");
            if(type == 0){//入场
                remain--;
            }else if(type == 1){
                remain++;
            }
        }else{//封闭车场,初始化余位数
//            remain = getValidUseCount(comid, share_number, invalid_order);
        }
        if(remain < 0){
            remain = 0L;
        }
        log.error("remain berth >>>comid:"+comid+",remain:"+remain);
        if(remainMap == null){
            int ret = dataBaseService.update("insert into remain_berth_tb(comid,amount,update_time) values(?,?,?) ",
                    new Object[]{comid, remain, ntime});
            log.error("update remain berth >>>comid:"+comid+",remain:"+remain+",ret:"+ret);
        }else{
            int ret = dataBaseService.update("update remain_berth_tb set amount=?,update_time=? where comid=? and berthseg_id<? ",
                    new Object[]{remain, ntime, comid, 0});
            log.error("update remain berth >>>comid:"+comid+",remain:"+remain+",ret:"+ret);
        }
        return remain;
    }
}
