package com.zhenlaidian.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.CommentInfo;
import com.zhenlaidian.ui.MyReceivedCommentActivity;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.TimeTypeUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * 我收到的评论列表适配器;
 */
public class MyCommentAdapter extends BaseAdapter {

    public ArrayList<CommentInfo> infos;
    public MyReceivedCommentActivity activity;

    public MyCommentAdapter(MyReceivedCommentActivity activity) {
        this.activity = activity;
    }

    public void addInfos(ArrayList<CommentInfo> infos) {
        if (infos == null || infos.size() == 0) {
            if (this.infos == null || this.infos.size() == 0) {
                activity.setNullVew();
            }
            return;
        }
        if (this.infos == null) {
            this.infos = infos;
            activity.setAdapter();
        } else {
            this.infos.addAll(infos);
            this.notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return infos.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(activity, R.layout.itme_my_received_comment, null);
            holder = new ViewHolder();
            holder.tv_my_comment_carnumber = (TextView) convertView.findViewById(R.id.tv_my_comment_carnumber);
            holder.tv_my_comment_time = (TextView) convertView.findViewById(R.id.tv_my_comment_time);
            holder.tv_my_comment_comment = (TextView) convertView.findViewById(R.id.tv_my_comment_comment);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (infos.get(position).getUser() != null) {
            holder.tv_my_comment_carnumber.setText(infos.get(position).getUser());
        }
        if (infos.get(position).getCtime() != null) {
            MyLog.i("时间是：", TimeTypeUtil.getStringTime(Long.parseLong(infos.get(position).getCtime())));
            boolean dayOfMillis = TimeTypeUtil.isSameDayOfMillis(Long.parseLong(infos.get(position).getCtime()) * 1000, System.currentTimeMillis());
            if (dayOfMillis) {
                SimpleDateFormat timef = new SimpleDateFormat("HH:mm");
                String date = timef.format(Long.parseLong(infos.get(position).getCtime()) * 1000);
                if (TimeTypeUtil.isAM(Long.parseLong(infos.get(position).getCtime()) * 1000)) {
                    holder.tv_my_comment_time.setText("上午 " + date);
                } else {
                    holder.tv_my_comment_time.setText("下午 " + date);
                }
            } else {
                holder.tv_my_comment_time.setText(TimeTypeUtil.getStringTime(Long.parseLong(infos.get(position).getCtime())));
            }
        }
        if (infos.get(position).getInfo() != null) {
            holder.tv_my_comment_comment.setText(infos.get(position).getInfo());
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView tv_my_comment_carnumber;
        TextView tv_my_comment_time;
        TextView tv_my_comment_comment;
    }
}
