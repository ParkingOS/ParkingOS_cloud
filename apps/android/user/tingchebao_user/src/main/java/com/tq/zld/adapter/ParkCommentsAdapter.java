package com.tq.zld.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.ParkComment;
import com.tq.zld.util.DateUtils;
import com.tq.zld.util.DensityUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class ParkCommentsAdapter extends BaseAdapter {

    private ArrayList<ParkComment> mCommentList;
    private Context mContext;

    public ParkCommentsAdapter(Context context) {
        mContext = context;
    }

    public void setData(int page, ArrayList<ParkComment> comments) {

        if (page == 1 && mCommentList != null) {
            mCommentList.clear();
        }

        if (mCommentList == null) {
            mCommentList = comments;
        } else {
            if (comments == null || comments.size() == 0) {
                return;
            }
            mCommentList.addAll(comments);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCommentList == null ? 0 : mCommentList.size();
    }

    @Override
    public Object getItem(int position) {
        return mCommentList == null ? null : mCommentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder mHolder;
        ParkComment parkComment = mCommentList.get(position);
        if (convertView != null) {
            mHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = View.inflate(mContext,
                    R.layout.listitem_comment, null);
            mHolder = new ViewHolder();
            mHolder.timeView = (TextView) convertView
                    .findViewById(R.id.tv_listitem_comment_time);
            mHolder.userView = (TextView) convertView
                    .findViewById(R.id.tv_listitem_comment_plate);
            mHolder.infoView = (TextView) convertView
                    .findViewById(R.id.tv_listitem_comment_user);
            mHolder.infoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mHolder.infoView.getLineCount() == 4) {
                        //当评论大于四行时，点击查看更多
                        showCommentDialog(mHolder.infoView.getText().toString());
                    }
                }
            });
            String key = TCBApp.getAppContext().getString(R.string.sp_plate_all);
            mHolder.userPlates = TCBApp.getAppContext().getAccountPrefs().getStringSet(key, new HashSet<String>());
            convertView.setTag(mHolder);
        }

        // 设置时间
        mHolder.commentTime.setTimeInMillis(parkComment.ctime * 1000);
        mHolder.timeView.setText(DateUtils.formatTime(mHolder.commentTime));

        //设置车牌号
        String userPlate = parkComment.user;
        if (TextUtils.isEmpty(userPlate)) {
            userPlate = "匿名用户";
        } else if (mHolder.userPlates.contains(userPlate)) {
            userPlate = "我的评价";
        } else {
            userPlate = userPlate.replace(userPlate.substring(2, 5), "***");
        }
        mHolder.userView.setText(userPlate);

        //设置评论内容
        mHolder.infoView.setText(parkComment.info);
        return convertView;
    }

    private void showCommentDialog(String comment) {
        TextView view = new TextView(mContext);
        int padding = DensityUtils.dip2px(mContext, 4);
        view.setPadding(padding, padding, padding, padding);
        view.setText(comment);
        new AlertDialog.Builder(mContext).setView(view).show();
    }

    static class ViewHolder {
        TextView timeView;
        TextView infoView;
        TextView userView;
        Calendar commentTime = Calendar.getInstance();
        Set<String> userPlates;
    }
}
