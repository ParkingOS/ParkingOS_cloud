package com.zhenlaidian.engine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.zhenlaidian.ui.AdvancedLoginActivity;
import com.zhenlaidian.ui.HistoryOrderActivity;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.ui.MySelfActivity;
import com.zhenlaidian.ui.OpenCardActivity;
import com.zhenlaidian.ui.RecommendOwnersActivity;
import com.zhenlaidian.util.CommontUtils;

/**
 * 主页导航抽屉点击监听事件;
 */
public class DrawerOnItemClick implements OnItemClickListener {
    Context context;
    DrawerLayout drawerLayout;
    Activity acivity;
    public DrawerOnItemClick(Context context, DrawerLayout drawerLayout,Activity acivity) {
        this.context = context;
        this.drawerLayout = drawerLayout;
        this.acivity = acivity;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                Intent intent = new Intent(context, LeaveActivity.class);
                context.startActivity(intent);
                if (LeaveActivity.class.getName() != ((Activity) context).getClass().getName()) {
                    ((Activity) context).finish();
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout.closeDrawers();
                }

                break;

//            case 1:
//                Intent currentIntent = new Intent(context, CurrentOrderActivity.class);
//                context.startActivity(currentIntent);
//                if (LeaveActivity.class.getName() != ((Activity) context).getClass().getName()) {
//                    ((Activity) context).finish();
//                    drawerLayout.closeDrawers();
//                } else {
//                    drawerLayout.closeDrawers();
//                }
//                break;
            case 1:
                Intent historytIntent = new Intent(context, HistoryOrderActivity.class);
                context.startActivity(historytIntent);
                if (LeaveActivity.class.getName() != ((Activity) context).getClass().getName()) {
                    ((Activity) context).finish();
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout.closeDrawers();
                }
                break;
            case 2:
//                Intent ParkingIntent = new Intent(context, ParkingInfoActivity.class);
//                context.startActivity(ParkingIntent);
//                if (LeaveActivity.class.getName() != ((Activity) context).getClass().getName() &&
//                        ParkingInfoActivity.class.getName() != ((Activity) context).getClass().getName()) {
//                    ((Activity) context).finish();
//                    drawerLayout.closeDrawers();
//                } else {
//                    drawerLayout.closeDrawers();
//                }
                Intent homeIntent = new Intent(context, MySelfActivity.class);
                context.startActivity(homeIntent);
                if (LeaveActivity.class.getName() != ((Activity) context).getClass().getName() &&
                        MySelfActivity.class.getName() != ((Activity) context).getClass().getName()) {
                    ((Activity) context).finish();
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout.closeDrawers();
                }
                break;
            case 3:
//                Intent homeIntent = new Intent(context, MySelfActivity.class);
//                context.startActivity(homeIntent);
//                if (LeaveActivity.class.getName() != ((Activity) context).getClass().getName() &&
//                        MySelfActivity.class.getName() != ((Activity) context).getClass().getName()) {
//                    ((Activity) context).finish();
//                    drawerLayout.closeDrawers();
//                } else {
//                    drawerLayout.closeDrawers();
//                }

                if (CommontUtils.Is910()||CommontUtils.Is900()) {
                   String acti = CommontUtils.getTopActivity(acivity);
                    if(acti.contains("OpenCardActivity")){
                        drawerLayout.closeDrawers();
                    }else{
                        Intent opencardintent = new Intent(context, OpenCardActivity.class);
                        context.startActivity(opencardintent);
                    }
                    if (LeaveActivity.class.getName() != ((Activity) context).getClass().getName() &&
                            OpenCardActivity.class.getName() != ((Activity) context).getClass().getName()) {
                        ((Activity) context).finish();
                        drawerLayout.closeDrawers();
                    } else {
                        drawerLayout.closeDrawers();
                    }
                }

//                Intent i = new Intent(context,CardChargeActivity.class);
//                i.putExtra("uuid","");
//                context.startActivity(i);

                break;
            case 4:
//                Intent cardIntent = new Intent(context, CenterMessageActivity.class);
//                context.startActivity(cardIntent);
//                if (LeaveActivity.class.getName() != ((Activity) context).getClass().getName() &&
//                        CenterMessageActivity.class.getName() != ((Activity) context).getClass().getName()) {
//                    ((Activity) context).finish();
//                    drawerLayout.closeDrawers();
//                } else {
//                    drawerLayout.closeDrawers();
//                }
                if (CommontUtils.Is910()) {
                    String acti = CommontUtils.getTopActivity(acivity);
                    if(acti.contains("AdvancedLoginActivity")){
                        drawerLayout.closeDrawers();
                    }else{
                        Intent opencardintent = new Intent(context, AdvancedLoginActivity.class);
                        context.startActivity(opencardintent);
                    }
                    if (LeaveActivity.class.getName() != ((Activity) context).getClass().getName() &&
                            OpenCardActivity.class.getName() != ((Activity) context).getClass().getName()) {
                        ((Activity) context).finish();
                        drawerLayout.closeDrawers();
                    } else {
                        drawerLayout.closeDrawers();
                    }
                }
                break;
            case 5:
                Intent msgIntent = new Intent(context, RecommendOwnersActivity.class);
                context.startActivity(msgIntent);
                if (LeaveActivity.class.getName() != ((Activity) context).getClass().getName() &&
                        RecommendOwnersActivity.class.getName() != ((Activity) context).getClass().getName()) {
                    ((Activity) context).finish();
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout.closeDrawers();
                }
                break;
        }
    }

}
