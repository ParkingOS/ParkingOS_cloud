package com.tq.zld.bean;

import com.tq.zld.im.bean.InviteMessage;

/**
 * Created by Gecko on 2015/11/2.
 */
public class InviteUserInfo extends InviteMessage {
    public String car_number;
    public String hx_name;
    public String wx_imgurl;

    public void setParent(InviteMessage msg) {
        if (msg == null) {
            return ;
        }

        this.id = msg.getId();
        this.groupId = msg.getGroupId();
        this.groupName = msg.getGroupName();
        this.from = msg.getFrom();
        this.reason = msg.getReason();
        this.status = msg.getStatus();
        this.time = msg.getTime();
    }
}
