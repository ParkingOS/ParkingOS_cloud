package com.tq.zld.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.im.IMConstant;
import com.tq.zld.im.adapter.EMCallBackAdapter;
import com.tq.zld.im.lib.HXSDKHelper;
import com.tq.zld.util.ChatDateUtils;
import com.tq.zld.util.IMUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.ToastUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.fragment.ChatFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * 聊天消息Adapter
 * Created by GT on 2015/9/11.
 */
public class ChatMessageAdapter extends BaseAdapter {
    static final int TYPE_ACCEPT_TXT = 0;
    static final int TYPE_SEND_TXT = 1;
    static final int TYPE_ACCEPT_MERGE = 2;
    static final int TYPE_SEND_MERGE = 3;

    static final int TYPE_COUNT = 4;

    static final int what_refresh_list = 0x0001;
    static final int what_select_last = 0x0003;
    static final int what_seek_to = 0x0005;

    String cImageUrl;
    String friendImageUrl;
    LayoutInflater inflater;
    DisplayImageOptions options;
    Activity mContext;
    String toChatName;
    EMConversation conversation;
    EMMessage[] messages;
    ListView mListView;
    Handler mHandler;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == what_refresh_list) {
                refreshMessageList();
            } else if (msg.what == what_select_last) {
                selecMessagetLast();
            } else if (msg.what == what_seek_to) {
                int position = msg.arg1;
                mListView.setSelection(position);
            }
        }
    };

    public void refresh() {
        if (handler.hasMessages(what_refresh_list)) {
            return;
        }

        handler.obtainMessage(what_refresh_list).sendToTarget();
    }

    public void selectLast() {
        handler.obtainMessage(what_refresh_list).sendToTarget();
        handler.obtainMessage(what_select_last).sendToTarget();
    }

    void refreshMessageList() {
        List<EMMessage> allMessages = conversation.getAllMessages();
        messages = allMessages.toArray(new EMMessage[allMessages.size()]);
//        LogUtils.d(Arrays.toString(messages));
        notifyDataSetChanged();

        for (int i = 0; i < messages.length; i++) {
            conversation.getMessage(i);
        }

    }

    public void refreshSeekTo(int position){
        handler.sendEmptyMessage(what_refresh_list);
        handler.obtainMessage(what_seek_to,position,0).sendToTarget();
    }

    void selecMessagetLast() {
        if (messages.length > 0) {
            mListView.setSelection(messages.length - 1);
        }
    }

    public void setHandler(Handler handler){
        this.mHandler = handler;
    }

    public ChatMessageAdapter(Activity context, String toChatName, ListView listView) {
        inflater = LayoutInflater.from(context);
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_chat_head_default)
                .showImageOnFail(R.drawable.ic_chat_head_default)
                .showImageOnLoading(R.drawable.ic_chat_head_default)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        mContext = context;
        this.toChatName = toChatName;
        this.conversation = EMChatManager.getInstance().getConversation(toChatName);
        this.mListView = listView;
    }

    @Override
    public int getCount() {
        int count = messages == null ? 0 : messages.length;
//        LogUtils.i(String.format("count=%d", count));
        return count;
    }

    @Override
    public EMMessage getItem(int position) {
        if (messages != null && messages.length > position) {
            return messages[position];
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = createMsgView(position);
            holder = new ViewHolder();
            setHolder(position,holder,convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final EMMessage msg = getItem(position);

        if (msg.getType() == EMMessage.Type.TXT){
            if (msg.getBooleanAttribute(IMConstant.MSG_TYPE_TICKET_MERGE, false)){
                handleMessageMerge(msg, holder, position);
            } else {
                handleMessageText(msg, holder);
            }
        }

        if (msg.direct == EMMessage.Direct.SEND) {
            //发送的消息，需要处理状态，
            // 如果正在发送中，显示ProgressBar
            //发送失败的，点击重新发送
            holder.fail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            msg.status = EMMessage.Status.CREATE;
                            refreshSeekTo(position);
                        }
                    });
                }
            });
        }

        String url = msg.direct == EMMessage.Direct.RECEIVE ? friendImageUrl:cImageUrl;
        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().displayImage(url, holder.head, options);
        } else {
            TCBApp.getAppContext().initImageLoader();
        }

        //距离上一条消息时间超过3分钟需要显示时间
        if (position == 0) {
            holder.time.setText(ChatDateUtils.getTimestampString(msg.getMsgTime(), mContext));
            holder.time.setVisibility(View.VISIBLE);
        } else {
            EMMessage preMsg = getItem(position - 1);
            if (preMsg != null && ChatDateUtils.isCloseEnough(msg.getMsgTime(), preMsg.getMsgTime())) {
                holder.time.setVisibility(View.GONE);
            } else {
                holder.time.setText(ChatDateUtils.getTimestampString(msg.getMsgTime(), mContext));
                holder.time.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    private void setHolder(int position, ViewHolder holder, View convertView) {
        holder.time = (TextView) convertView.findViewById(R.id.tv_chat_time);
        holder.head = (ImageView) convertView.findViewById(R.id.iv_chat_head);

        EMMessage message = getItem(position);

        if (message.direct == EMMessage.Direct.SEND) {//发送才有的属性
            holder.fail = convertView.findViewById(R.id.iv_chat_fail);
            holder.pb = convertView.findViewById(R.id.pb_chat_progress);
        }

        if (message.getType() == EMMessage.Type.TXT) {
            if (message.getBooleanAttribute(IMConstant.MSG_TYPE_TICKET_MERGE, false)) {
                holder.mergeTitle = (TextView) convertView.findViewById(R.id.tv_merge_title);
                holder.mergeDesc = (TextView) convertView.findViewById(R.id.tv_merge_desc);
                holder.mergeLogo = (ImageView) convertView.findViewById(R.id.iv_merge_logo);
                holder.merge = convertView.findViewById(R.id.ll_chat_merge);
            } else {
                holder.content = (TextView) convertView.findViewById(R.id.tv_chat_content);
            }
        }

    }

    /**
     * 根据消息类型来创建View
     * @param position 位置
     * @return
     */
    private View createMsgView(int position) {
        EMMessage message = getItem(position);
        if (message.getType() == EMMessage.Type.TXT) {
            //合体券类型
            if (message.getBooleanAttribute(IMConstant.MSG_TYPE_TICKET_MERGE, false)) {
                return message.direct == EMMessage.Direct.SEND ? inflater.inflate(R.layout.listitem_chat_merge_send, null) : inflater.inflate(R.layout.listitem_chat_merge_accept, null);
            } else {
                //普通消息
                return message.direct == EMMessage.Direct.SEND ? inflater.inflate(R.layout.listitem_chat_txt_send, null) : inflater.inflate(R.layout.listitem_chat_txt_accept, null);
            }
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        EMMessage message = getItem(position);
        if (message == null) {
            return -1;
        }
//        LogUtils.i("position=" + position + ", " + message.direct);


        if (message.getType() == EMMessage.Type.TXT) {
            //合体券类型
            if (message.getBooleanAttribute(IMConstant.MSG_TYPE_TICKET_MERGE, false)) {
                return message.direct == EMMessage.Direct.SEND ? TYPE_SEND_MERGE : TYPE_ACCEPT_MERGE;
            } else {
                //普通消息
                return message.direct == EMMessage.Direct.SEND ? TYPE_SEND_TXT : TYPE_ACCEPT_TXT;
            }
        }

        return -1;
    }

    private void handleMessageText(EMMessage msg, final ViewHolder holder){
        TextMessageBody body = (TextMessageBody) msg.getBody();
        holder.content.setText(body.getMessage());
        holder.content.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CharSequence text = holder.content.getText();
                copyText(text);
                return true;
            }
        });

        handlerMessageStatus(msg, holder);
    }

    private void handleMessageMerge(final EMMessage msg, ViewHolder holder, int position){
        final String mid = msg.getStringAttribute(IMConstant.MSG_ATTR_MERGE_ID, "");
        final boolean mergeReceive = msg.getBooleanAttribute(IMConstant.MSG_ATTR_MERGE_RECEIVE, false);
        String winner = msg.getStringAttribute(IMConstant.MSG_ATTR_MERGE_WINNER, "");

        //TODO index -1错误。
//        if (msg.direct == EMMessage.Direct.RECEIVE) {
//            EMMessage lastMsg = getItem(position - 1);
//
//            if (lastMsg.direct == EMMessage.Direct.RECEIVE && lastMsg.getStringAttribute(IMConstant.MSG_ATTR_MERGE_ID, "-1").equals(mid)) {
//                //错误消息，连续发送两条 相同id的合体请求。
//                conversation.removeMessage(lastMsg.getMsgId());
//                refresh();
//                return;
//            }
//        }

        if (msg.getBooleanAttribute(IMConstant.MSG_ATTR_MERGE_RESULT, false)) {
            if(TCBApp.mMobile.equals(winner)) {
                holder.mergeLogo.setImageResource(R.drawable.btn_chat_merge_ok);
                holder.mergeTitle.setText("合体成功");
                holder.mergeDesc.setText("您赢得一张大券");
            } else if ("-1".equals(winner)) {
                holder.mergeLogo.setImageResource(R.drawable.btn_chat_merge_fail);
                holder.mergeTitle.setText("合体失败");
                holder.mergeDesc.setText("双方失去停车券");
            } else {
                holder.mergeLogo.setImageResource(R.drawable.btn_chat_merge_fail);
                holder.mergeTitle.setText("合体失败");
                holder.mergeDesc.setText("失去停车券");
            }
        } else {
            holder.mergeLogo.setImageResource(R.drawable.btn_chat_merge);
            holder.mergeTitle.setText("停车券求合体");
            holder.mergeDesc.setText("若合体成功，赢家将获取一张大券");
        }

        LogUtils.i(IMUtils.getMsgString(msg));
        holder.merge.setTag(msg);
        holder.merge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EMMessage emsg = (EMMessage) v.getTag();
                LogUtils.i(IMUtils.getMsgString(emsg));
                preOpenMerge(emsg);
            }
        });

        handlerMessageStatus(msg, holder);
    }

    /**
     * 处理消息的状态，成功，失败，重发
     * @param msg
     * @param holder
     */
    private void handlerMessageStatus(EMMessage msg, ViewHolder holder){
        if (msg.direct == EMMessage.Direct.SEND) {
            switch (msg.status) {
                case  SUCCESS://发送成功！
                    holder.fail.setVisibility(View.GONE);
                    holder.pb.setVisibility(View.GONE);
                    break;

                case FAIL://发送失败！
                    holder.fail.setVisibility(View.VISIBLE);
                    holder.pb.setVisibility(View.GONE);
                    break;

                case INPROGRESS://发送中...
                    holder.fail.setVisibility(View.GONE);
                    holder.pb.setVisibility(View.VISIBLE);
                    break;
                default://CREATE新创建的，准备中心发送
                    sendMsgInBackgroud(msg, holder);
                    break;
            }
        }
    }

    private void sendMsgInBackgroud(final EMMessage msg, ViewHolder holder){
        holder.fail.setVisibility(View.GONE);
        holder.pb.setVisibility(View.VISIBLE);

        EMChatManager.getInstance().sendMessage(msg, new EMCallBackAdapter() {
            @Override
            public void onSuccess() {
                updateSendedView(msg);
            }

            @Override
            public void onError(int i, String s) {
                updateSendedView(msg);
            }
        });
    }

    private void updateSendedView(final EMMessage msg){
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (msg.status == EMMessage.Status.FAIL) {
                    if (msg.getError() == EMError.MESSAGE_SEND_INVALID_CONTENT) {
                        Toast.makeText(mContext, "你发送了不合法的消息内容", Toast.LENGTH_SHORT).show();
                    } else if (msg.getError() == EMError.MESSAGE_SEND_NOT_IN_THE_GROUP) {
                        Toast.makeText(mContext, "你已经不在此群了", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, "请检查网络或稍候重试", Toast.LENGTH_SHORT).show();
                    }
                }

                notifyDataSetChanged();
            }
        });
    }

    /**
     * 准备打开合体结果
     * @param msg
     */
    private void preOpenMerge(final EMMessage msg){
        final String mid = msg.getStringAttribute(IMConstant.MSG_ATTR_MERGE_ID, "");
        final boolean mergeReceive = msg.getBooleanAttribute(IMConstant.MSG_ATTR_MERGE_RECEIVE, false);

        //carinter.do?action=preresticketuion&mobile=18811157723&id=426
//        {"result":"-1","errmsg":"停车券已过期"}
//        {"result":"-1","errmsg":"停车券已使用"}
//        {"result":"1","errmsg":"可以合体"}
        Map<String, String> params = URLUtils.createParamsMap();
        params.put("action", "preresticketuion");
        params.put("mobile", TCBApp.mMobile);
        params.put("id", mid);
        params.put("uin", HXSDKHelper.getInstance().getHXId());
        params.put("touin",msg.getFrom());
        String url = URLUtils.createUrl(TCBApp.mServerUrl, "carinter.do", params);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                String result = null;
                try {
                    result = jsonObject.getString("result");
                    String errmsg = jsonObject.getString("errmsg");
                    if ("1".equals(result)) {
                        openMergeResult(msg);
                    } else {
                        ToastUtils.show(mContext, errmsg);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, null);
        TCBApp.getAppContext().addToRequestQueue(request);
    }

    /**
     * 打开合体结果
     * @param msg
     */
    private void openMergeResult(EMMessage msg){
        final String mid = msg.getStringAttribute(IMConstant.MSG_ATTR_MERGE_ID, "");
        final boolean mergeReceive = msg.getBooleanAttribute(IMConstant.MSG_ATTR_MERGE_RECEIVE, false);

        //TODO 打开接受合并请求页面
        if (msg.direct == EMMessage.Direct.RECEIVE && mergeReceive) {
            //
            Intent intent = new Intent();
            intent.setClass(TCBApp.getAppContext(), MainActivity.class);
            intent.putExtra(MainActivity.ARG_FRAGMENT, MainActivity.FRAGMENT_RECEIVE_MERGE);
            Bundle args = new Bundle();
            args.putString("mid", mid);
            args.putString("from", toChatName);
            args.putParcelable("msg", msg);
            intent.putExtra(MainActivity.ARG_FRAGMENT_ARGS, args);
            mHandler.obtainMessage(ChatFragment.MSG_WHAT_START_ACTIVITY, intent).sendToTarget();
        } else {
//                            ToastUtils.show("你发起zhe!");
            Intent intent = new Intent();
            intent.setClass(TCBApp.getAppContext(), MainActivity.class);
            intent.putExtra(MainActivity.ARG_FRAGMENT, MainActivity.FRAGMENT_RESULT_MERGE);
            Bundle args = new Bundle();
            args.putString("mid", mid);
            args.putString("toChatName", toChatName);
            intent.putExtra(MainActivity.ARG_FRAGMENT_ARGS, args);
            mHandler.obtainMessage(ChatFragment.MSG_WHAT_START_ACTIVITY, intent).sendToTarget();
        }
    }

    private void copyText(CharSequence text){
        ClipboardManager cm = (ClipboardManager) this.mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("tcb chat text",text);
        cm.setPrimaryClip(clip);
        ToastUtils.show("已复制");
    }

    public void setImageUrl(String url) {
        this.cImageUrl = url;
    }

    public void setFriendImageUrl(String url){
        this.friendImageUrl = url;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    static class ViewHolder {
        TextView time;
        ImageView head;
        View fail;
        View pb;

        TextView content;//聊天内容

        ImageView mergeLogo;
        TextView mergeTitle;
        TextView mergeDesc;
        View merge;
    }
}
