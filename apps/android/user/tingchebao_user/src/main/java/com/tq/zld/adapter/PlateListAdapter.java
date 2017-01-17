package com.tq.zld.adapter;

import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.Plate;

import java.util.ArrayList;

/**
 * Author：ClareChen
 * E-mail：ggchaifeng@gmail.com
 * Date：  15/7/9 下午6:00
 */
public class PlateListAdapter extends BaseAdapter {

    /**
     * 最大支持的车牌数
     */
    private static final int MAX_COUNT = 3;

    private ArrayList<Plate> mPlates;

    private boolean mAlreadyHasBonus;

    public void setPlates(ArrayList<Plate> plates) {
        if (plates == null) {
            plates = new ArrayList<>();
        }
        if (plates.size() < MAX_COUNT) {
            plates.add(new Plate("", Plate.STATE_CERTIFY));
        }
        mPlates = plates;
        mAlreadyHasBonus = checkBonusState(plates);
        notifyDataSetChanged();
    }

    private boolean checkBonusState(ArrayList<Plate> plates) {
        if (plates != null && plates.size() > 0) {
            for (Plate plate : plates) {
                if (Plate.STATE_CERTIFIED == plate.is_auth) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        Plate plate = (Plate) getItem(position);
        return plate.is_auth == Plate.STATE_CERTIFY || plate.is_auth == Plate.STATE_CERTIFY_FAILED;
    }

    @Override
    public int getCount() {
        return mPlates == null ? 1 : mPlates.size();
    }

    @Override
    public Object getItem(int position) {
        return mPlates == null ? null : mPlates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (getItem(position) != null) {
            Plate plate = (Plate) getItem(position);
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(TCBApp.getAppContext(), R.layout.listitem_plate, null);
                holder = new ViewHolder();
                holder.mIconView = (ImageView) convertView.findViewById(R.id.iv_listitem_plate);
                holder.mPlateView = (TextView) convertView.findViewById(R.id.tv_listitem_plate);
                holder.mStateView = (TextView) convertView.findViewById(R.id.tv_listitem_plate_state);
                holder.mTipsView = (TextView) convertView.findViewById(R.id.tv_listitem_plate_tips);
                holder.mArrowView = (ImageView) convertView.findViewById(R.id.iv_listitem_plate_arrow);

                Resources resources = TCBApp.getAppContext().getResources();
                holder.colorCertify = resources.getColor(R.color.text_red);
                holder.colorCertified = resources.getColor(R.color.text_green);
                holder.colorCertifying = resources.getColor(R.color.text_orange);
                holder.unCertifyTips = mAlreadyHasBonus ? "认证后优惠多多" : "认证后可获30元信用额度";
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            //如果车牌为空，表示可以添加车牌
            if (TextUtils.isEmpty(plate.car_number)) {
                holder.mIconView.setImageResource(R.drawable.ic_action_add_green);
                holder.mPlateView.setText("点击添加车牌");
                holder.mTipsView.setText("");
                holder.mStateView.setVisibility(View.GONE);
                holder.mArrowView.setVisibility(View.VISIBLE);
            } else {
                // 否则显示车牌布局

                //设置车牌
                holder.mPlateView.setText(plate.car_number);
                holder.mIconView.setImageResource(R.drawable.ic_account_plate);
                holder.mArrowView.setVisibility(View.VISIBLE);

                holder.mStateView.setVisibility(View.VISIBLE);
                //根据state判断车牌的认证状态
                switch (plate.is_auth) {
                    case Plate.STATE_CERTIFY:
                        //未认证
                        holder.mStateView.setText("未认证");
                        holder.mStateView.setTextColor(holder.colorCertify);
                        holder.mStateView.setBackgroundResource(R.drawable.shape_account_plate_certify);

                        holder.mTipsView.setText(holder.unCertifyTips);
                        holder.mArrowView.setVisibility(View.VISIBLE);
                        break;
                    case Plate.STATE_CERTIFIED:
                        //已认证
                        holder.mStateView.setText("已认证");
                        holder.mStateView.setTextColor(holder.colorCertified);
                        holder.mStateView.setBackgroundResource(R.drawable.shape_account_plate_certified);

                        holder.mTipsView.setText("");
                        holder.mArrowView.setVisibility(View.GONE);
                        break;
                    case Plate.STATE_CERTIFYING:
                        //认证中
                        holder.mStateView.setText("审核中(1-3天)");
                        holder.mStateView.setTextColor(holder.colorCertifying);
                        holder.mStateView.setBackgroundResource(R.drawable.shape_account_plate_certifying);

                        holder.mTipsView.setText("");
                        holder.mArrowView.setVisibility(View.GONE);
                        break;
                    case Plate.STATE_CERTIFY_FAILED:
                        // 认证失败
                        holder.mStateView.setText("认证未通过");
                        holder.mStateView.setTextColor(holder.colorCertify);
                        holder.mStateView.setBackgroundResource(R.drawable.shape_account_plate_certify);

                        holder.mTipsView.setText("请重新上传证件照片");
                        holder.mArrowView.setVisibility(View.VISIBLE);
                        break;
                    case Plate.STATE_CERTIFY_BLOCKED:
                        // 认证不通过
                        holder.mStateView.setText("无效车牌");
                        holder.mStateView.setTextColor(holder.colorCertify);
                        holder.mStateView.setBackgroundResource(R.drawable.shape_account_plate_certify);

                        holder.mTipsView.setText("请联系停车宝客服");
                        holder.mArrowView.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }

            }
        }
        return convertView;
    }

    class ViewHolder {
        ImageView mIconView;
        TextView mPlateView;
        TextView mStateView;
        TextView mTipsView;
        ImageView mArrowView;

        int colorCertify;
        int colorCertified;
        int colorCertifying;

        String unCertifyTips;
    }
}
