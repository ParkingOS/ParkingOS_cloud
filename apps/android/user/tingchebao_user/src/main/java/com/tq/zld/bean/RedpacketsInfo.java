package com.tq.zld.bean;

public class RedpacketsInfo {

    // {"id":"12","exptime":"1425916800","is_auth":"0"}
    public String id;// 去查询红包详情的编号；
    public long exptime;// 过期时间；
    public int state;//is_auth : 0:已过期，1未过期，可以领取，2，已领取
    public String title;// 标题
}
