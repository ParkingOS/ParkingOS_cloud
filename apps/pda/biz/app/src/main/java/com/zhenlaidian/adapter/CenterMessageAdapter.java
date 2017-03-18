package com.zhenlaidian.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.CenterMessage;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.CenterMessageActivity;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * 消息中心适配器;
 */
public class CenterMessageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<CenterMessage> msgs;
    private String maxId;

    public CenterMessageAdapter(Context context) {
        super();
        this.context = context;
        maxId = SharedPreferencesUtils.getIntance(context).getMsgMaxId(BaseActivity.useraccount);
    }

    public void addOrder(ArrayList<CenterMessage> msgs, CenterMessageActivity activity) {
        if (this.msgs == null) {
            this.msgs = msgs;
            activity.setAdapter();
        } else {
            this.msgs.addAll(msgs);
            this.notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return msgs.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return msgs.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CenterMessage message = msgs.get(position);
        ViewHolder mHolder;
        if (convertView != null) {
            mHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = View.inflate(context, R.layout.listitem_message, null);
            mHolder = new ViewHolder();
            mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            mHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
            mHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
            mHolder.ivRead = (ImageView) convertView.findViewById(R.id.iv_read);
            convertView.setTag(mHolder);
        }
        if (message.getTitle() != null) {
            mHolder.tvTitle.setText(message.getTitle());
        }
        if (message.getCtime() != null) {
            mHolder.tvTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(new Date(Long.parseLong(message.getCtime()) * 1000)));
        }
        if (message.getContent() != null) {
            mHolder.tvContent.setText(message.getContent());
        }
        if (!TextUtils.isEmpty(message.getId()) && TextUtils.isDigitsOnly(message.getId())) {
            try {
                Long id = Long.parseLong(message.getId());
                Long maxid = Long.parseLong(maxId);
                if (id > maxid) {
                    mHolder.ivRead.setVisibility(View.VISIBLE);
                } else {
                    mHolder.ivRead.setVisibility(View.INVISIBLE);
                }
            } catch (Exception e) {
                MyLog.w("CenterMessageAdapter", "消息中心maxid类型转换异常！");
            }

        }
        return convertView;
    }

    static class ViewHolder {
        TextView tvTitle;
        TextView tvTime;
        TextView tvContent;
        ImageView ivRead;
    }
}
