/**
 *
 */
package com.zhenlaidian.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.InVehicleInfo;
import com.zhenlaidian.util.CommontUtils;

import java.util.ArrayList;

/**
 * 车位显示在场车辆;
 *
 * @author zhangyunfei 2015年9月9日
 */
public class InTheVehicleAdapter extends BaseAdapter {

    public Context context;
    public ArrayList<InVehicleInfo> infos;

    public InTheVehicleAdapter(Context context, ArrayList<InVehicleInfo> infos) {
        super();
        this.context = context;
        this.infos = infos;
    }

    @Override
    public int getCount() {
        return CommontUtils.checkList(infos) ? infos.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_in_the_vehicle, null);
            holder = new ViewHolder();
            holder.carnumber = (TextView) convertView.findViewById(R.id.tv_in_vehicle_carnumber);
            holder.duration = (TextView) convertView.findViewById(R.id.tv_in_vehicle_duration);
            holder.number = (TextView) convertView.findViewById(R.id.tv_in_vehicle_number);
            holder.rl_vehicle_bg = (RelativeLayout) convertView.findViewById(R.id.rl_in_vehicle_bg);
            holder.lnempty = ((LinearLayout) convertView.findViewById(R.id.boweiumpty));
            holder.txtempty = ((TextView) convertView.findViewById(R.id.boweiumptytxt));
            holder.monthnumber = (ImageView) convertView.findViewById(R.id.monthnumber);
            holder.bondcard = (ImageView) convertView.findViewById(R.id.bondcard);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (CommontUtils.checkList(infos)) {
//            if (CommontUtils.checkString(infos.get(position).getState())) {
//                if (infos.get(position).getState().equals("1")) {
//                    holder.rl_vehicle_bg.setBackgroundResource(R.drawable.vehicle_preiny);
//                }
////                if (infos.get(position).getState().equals("0")) {
////                    holder.rl_vehicle_bg.setBackgroundResource(R.drawable.vehicle_preout);
////                }
//            }
            if (CommontUtils.checkString(infos.get(position).getOrderid()) || CommontUtils.checkString(infos.get(position).getCar_number())) {
                holder.rl_vehicle_bg.setBackgroundResource(R.drawable.vehicle_in);
                holder.carnumber.setText(infos.get(position).getCar_number());
                holder.duration.setText(infos.get(position).getBer_name());
                holder.lnempty.setVisibility(View.GONE);
                //正常情况车牌字体颜色和泊位字体颜色都是白色
                holder.carnumber.setTextColor(Color.WHITE);
                holder.duration.setTextColor(Color.WHITE);
                if (infos.get(position).getIsmonthuser().equals("5")) {
                    holder.monthnumber.setVisibility(View.VISIBLE);
                } else {
                    holder.monthnumber.setVisibility(View.GONE);
                }
                if (!TextUtils.isEmpty(infos.get(position).getIs_card()) &&infos.get(position).getIs_card().equals("1")) {
                    holder.bondcard.setVisibility(View.VISIBLE);
                } else {
                    holder.bondcard.setVisibility(View.GONE);
                }

                if (CommontUtils.checkString(infos.get(position).getSensor_state())) {
//                    if (infos.get(position).getSensor_state().equals("1")) {
//                        holder.rl_vehicle_bg.setBackgroundResource(R.drawable.vehicle_preiny);
//                        if (CommontUtils.checkString(infos.get(position).getOrderid()) || CommontUtils.checkString(infos.get(position).getCar_number())){
//                            holder.rl_vehicle_bg.setBackgroundResource(R.drawable.vehicle_in);
//                        }
//                    }
                    if (infos.get(position).getSensor_state().equals("0")) {
                        holder.rl_vehicle_bg.setBackgroundResource(R.drawable.vehicle_preout);
                        //离场时车牌字体颜色和泊位字体颜色都变黑
                        holder.carnumber.setTextColor(Color.BLACK);
                        holder.duration.setTextColor(Color.BLACK);
                    }
                }
            } else {
                holder.rl_vehicle_bg.setBackgroundResource(R.drawable.vehicle_out);
                holder.carnumber.setText("");
//            holder.number.setText(infos.get(position).getCid());
                holder.duration.setText("");
                holder.lnempty.setVisibility(View.VISIBLE);
                holder.txtempty.setText(infos.get(position).getBer_name());
                holder.monthnumber.setVisibility(View.GONE);
                holder.bondcard.setVisibility(View.GONE);
                if (CommontUtils.checkString(infos.get(position).getSensor_state())) {
                    if (infos.get(position).getSensor_state().equals("1")) {
                        holder.rl_vehicle_bg.setBackgroundResource(R.drawable.vehicle_preiny);
//                        if (CommontUtils.checkString(infos.get(position).getOrderid()) || CommontUtils.checkString(infos.get(position).getCar_number())){
//                            holder.rl_vehicle_bg.setBackgroundResource(R.drawable.vehicle_in);
//                        }
                    }

                }
            }
//            if (CommontUtils.checkString(infos.get(position).getSensor_state())) {
//                if (infos.get(position).getSensor_state().equals("1")) {
//                    holder.rl_vehicle_bg.setBackgroundResource(R.drawable.vehicle_preiny);
//                    if (CommontUtils.checkString(infos.get(position).getOrderid()) || CommontUtils.checkString(infos.get(position).getCar_number())){
//                        holder.rl_vehicle_bg.setBackgroundResource(R.drawable.vehicle_in);
//                    }
//                }
//                if (infos.get(position).getSensor_state().equals("0")) {
//                    holder.rl_vehicle_bg.setBackgroundResource(R.drawable.vehicle_preout);
//                    //离场时车牌字体颜色和泊位字体颜色都变黑
//                    holder.carnumber.setTextColor(Color.BLACK);
//                    holder.duration.setTextColor(Color.BLACK);
//                }
//            }
        }


//        if(position == 2){
//            holder.rl_vehicle_bg.setBackgroundResource(R.drawable.vehicle_preinr);
//        }if(position == 3){
//            holder.rl_vehicle_bg.setBackgroundResource(R.drawable.vehicle_preout);
//        }if(position == 4){
//            holder.rl_vehicle_bg.setBackgroundResource(R.drawable.vehicle_preiny);
//        }

        return convertView;
    }

    private static class ViewHolder {
        TextView carnumber;
        TextView duration;
        TextView number;
        RelativeLayout rl_vehicle_bg;
        LinearLayout lnempty;
        TextView txtempty;
        ImageView monthnumber;
        ImageView bondcard;
    }

}
