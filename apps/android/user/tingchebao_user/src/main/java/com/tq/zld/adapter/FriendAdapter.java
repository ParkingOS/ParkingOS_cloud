package com.tq.zld.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.im.IMConstant;
import com.tq.zld.im.bean.User;

import java.util.List;

/**
 * Created by Gecko on 2015/11/2.
 */
public class FriendAdapter extends BaseAdapter {
    public static final int TYPE_NEW_FRIEND = 0;
    public static final int TYPE_FRIEND = 1;

    static final int TYPE_COUNT = 2;

    LayoutInflater inflater;
    List<User> users;
    DisplayImageOptions options;

    public FriendAdapter(Context ctx, List<User> list) {
        inflater = LayoutInflater.from(ctx);
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_chat_head_default)
                .showImageOnFail(R.drawable.ic_chat_head_default)
                .showImageOnLoading(R.drawable.ic_chat_head_default)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        this.users = list;
    }

    public List<User> getData() {
        return this.users;
    }

    public void setData(List<User> list) {
        users = list;
        notifyDataSetChanged();
    }

    public void sortAndRefresh(List<User> list){
        this.setData(list);
    }

    @Override
    public int getItemViewType(int position) {
        User item = getItem(position);
        if (item.getUsername().equals(IMConstant.ITEM_NEW_FRIENDS)) {
            return TYPE_NEW_FRIEND;
        } else {
            return TYPE_FRIEND;
        }
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    @Override
    public int getCount() {
        return users == null ? 0 : users.size();
    }

    @Override
    public User getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private View createView(int position){
        int itemViewType = getItemViewType(position);
        View view = null;
        switch (itemViewType) {
            case TYPE_FRIEND:
                view = inflater.inflate(R.layout.listitem_friend, null);

                break;
            case TYPE_NEW_FRIEND:
                view = inflater.inflate(R.layout.listitem_invite_friend, null);
                break;
        }

        return view;
    }

    private void setHolder(int position, ViewHolder holder, View convertView) {
        if (getItemViewType(position) == TYPE_NEW_FRIEND) {
            holder.unread = (TextView) convertView.findViewById(R.id.tv_friend_unread);
        } else if(getItemViewType(position) == TYPE_FRIEND) {
            holder.head = (ImageView) convertView.findViewById(R.id.iv_friend_head);
            holder.plate = (TextView) convertView.findViewById(R.id.tv_friend_plate);
            holder.reason = (TextView) convertView.findViewById(R.id.tv_friend_reason);
            holder.unread = (TextView) convertView.findViewById(R.id.tv_friend_unread);
            holder.wx = (TextView) convertView.findViewById(R.id.tv_friend_wx);
        }

    }

    private void setData(User user, ViewHolder holder) {
        if (user.getUsername().equals(IMConstant.ITEM_NEW_FRIENDS)) {
            if (user.getUnreadMsgCount() > 0) {
                holder.unread.setVisibility(View.VISIBLE);
                holder.unread.setText(String.valueOf(user.getUnreadMsgCount()));
            } else {
                holder.unread.setVisibility(View.INVISIBLE);
            }

            return;
        }

        if (user == null) {
            return;
        }

        holder.plate.setText(user.getPlate());
        if (TextUtils.isEmpty(user.getPlate())) {
            holder.plate.setText("车牌号未知");
        }

        EMConversation conversation = EMChatManager.getInstance().getConversation(user.getUsername());
        if (conversation.getUnreadMsgCount() > 0) {
            holder.unread.setText(String.valueOf(conversation.getUnreadMsgCount()));
            holder.unread.setVisibility(View.VISIBLE);
        } else {
            holder.unread.setVisibility(View.INVISIBLE);
        }
        holder.reason.setText(user.getReason());
        //不被点击
//            holder.reason.setOnClickListener(playGameOnClickListener);
        if (TextUtils.isEmpty(user.getNick()) || user.getNick().startsWith("hx")) {
            holder.wx.setVisibility(View.GONE);
        } else {
            holder.wx.setVisibility(View.VISIBLE);
            holder.wx.setText(user.getNick());
        }
        //加载图片
        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().displayImage(user.getAvatar(), holder.head, options);
        } else {
            TCBApp.getAppContext().initImageLoader();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        User user = getItem(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = createView(position);
            setHolder(position, holder, convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

//        LogUtils.i(String.format("position = %d, user = %s", position, user.toString()));
        setData(user, holder);

        return convertView;
    }

    static class ViewHolder {
        TextView wx;
        TextView plate;
        TextView reason;
        TextView unread;
        ImageView head;
    }

}