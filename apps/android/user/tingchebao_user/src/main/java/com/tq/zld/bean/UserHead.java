package com.tq.zld.bean;

import com.tq.zld.im.bean.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GT on 2015/9/28.
 */
public class UserHead {
    public String carnumber;
    public String id;
    public String source;
    public String wx_imgurl;
    public String wx_name;

    public static List<User> getUsers(List<UserHead> list) {
        List<User> users = new ArrayList<>();
        for (UserHead u : list ) {
            users.add(getUser(u));
        }

        return users;
    }

    public static User getUser(UserHead userHead) {
        User user = new User();
        user.setUsername(userHead.id);
        user.setReason(userHead.source);
        user.setAvatar(userHead.wx_imgurl);
        user.setNick(userHead.wx_name);
        user.setPlate(userHead.carnumber);
        return user;
    }
}
