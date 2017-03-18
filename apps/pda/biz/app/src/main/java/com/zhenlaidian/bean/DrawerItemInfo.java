package com.zhenlaidian.bean;

import com.zhenlaidian.R;
import com.zhenlaidian.util.CommontUtils;

import java.util.ArrayList;

public class DrawerItemInfo {

    String name;
    int id;
    private static ArrayList<DrawerItemInfo> instance;

    public DrawerItemInfo(String name, int id) {
        super();
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static ArrayList<DrawerItemInfo> getInstance() {
        if (instance == null) {
            instance = new ArrayList();
//            instance.add(0,new DrawerItemInfo("主页", R.drawable.ic_main));
//            instance.add(1,new DrawerItemInfo("历史订单", R.drawable.ic_history_order));
//            instance.add(2,new DrawerItemInfo("停车场", R.drawable.parking));
//            instance.add(3,new DrawerItemInfo("我", R.drawable.ic_me));
//            instance.add(4,new DrawerItemInfo("消息中心", R.drawable.msg));
//            instance.add(5,new DrawerItemInfo("开通车主会员", R.drawable.vip));
            instance.add(0,new DrawerItemInfo("主页", R.drawable.ic_main));
            instance.add(1,new DrawerItemInfo("历史订单", R.drawable.ic_history_order));
            instance.add(2,new DrawerItemInfo("我", R.drawable.ic_me));
            if (CommontUtils.Is910()) {
                instance.add(3,new DrawerItemInfo("储值卡", R.drawable.vip));
                instance.add(4, new DrawerItemInfo("高级登录", R.drawable.currorder));
            }
            if (CommontUtils.Is900()) {
                instance.add(3,new DrawerItemInfo("储值卡", R.drawable.vip));
//                instance.add(4, new DrawerItemInfo("高级登录", R.drawable.currorder));
            }
//            instance.add(1, new DrawerItemInfo("当前订单", R.drawable.currorder));

        }
        return instance;
    }


}
