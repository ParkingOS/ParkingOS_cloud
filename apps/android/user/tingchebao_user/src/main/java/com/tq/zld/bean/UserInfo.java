package com.tq.zld.bean;

import com.tq.zld.im.bean.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GT on 2015/9/2.
 */
public class UserInfo {
    public String plate;
    public String wxid;
    public String reason;
    public String head;

    public UserInfo(String plate) {
        this.plate = plate;
    }

    public static List<UserInfo> getUsers(List<User> users){
        List<UserInfo>  infos = new ArrayList<>();
        for (User u : users) {
            infos.add(getUser(u));
        }

        return infos;
    }

    public static UserInfo getUser(User user){
        return new UserInfo(user.getUsername());
    }
}
