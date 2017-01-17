package com.tq.zld.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.adapter.ChatMessageAdapter;
import com.tq.zld.bean.Coupon;
import com.tq.zld.im.IMConstant;
import com.tq.zld.im.adapter.EMCallBackAdapter;
import com.tq.zld.im.bean.User;
import com.tq.zld.im.db.UserDao;
import com.tq.zld.im.lib.HXSDKHelper;
import com.tq.zld.util.IMUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.holder.MenuHolder;
import com.tq.zld.view.im.ChatActivity;
import com.tq.zld.view.map.SelectTicketMergeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;

/**
 * Created by GT on 2015/9/2.
 */
public class ChatFragment extends BaseFragment implements View.OnClickListener {
    public static String USER = "user";
    public static String USER_ID = "user_id";
    public static String USER_PLATE = "user_plate";
    public static String USER_IMAGE = "user_image";
    public static String USER_NICK = "user_nick";

    public static String IS_NOTIFICATION = "notification";
    IMEventListener eventListener = new IMEventListener();
    static Handler mHandler = null;
    private View mergeView;
    private View sendView;
    private ListView mListView;
    private ChatMessageAdapter adapter;
    private EditText etContent;

    private boolean sendViewIsShow = false;
    private int pageSize = 20;
    private boolean isloading = false;
    private SwipeRefreshLayout mRefreshView;

    private User friend;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friend = getArguments().getParcelable(USER);
        toChatName = friend.getUsername();
        LogUtils.i("chat >> " + friend.toString());
        mHandler = new ChatHandler(this);

        //如果没有微信和没有车牌号同时存在，则有问题，刷新flag下次进入列表获取。
        String nick = friend.getNick();
        String plate = friend.getPlate();
        if (TextUtils.isEmpty(nick) && TextUtils.isEmpty(plate)) {
            //需要获取用户的额外信息
            TCBApp.getAppContext().saveBoolean(R.string.sp_im_user_head_init, false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.selectLast();
        EMChatManager.getInstance().registerEventListener(
                eventListener,
                new EMNotifierEvent.Event[]{
                        EMNotifierEvent.Event.EventNewMessage,
                        EMNotifierEvent.Event.EventOfflineMessage,
                        EMNotifierEvent.Event.EventDeliveryAck,
                        EMNotifierEvent.Event.EventReadAck});
    }

    @Override
    public void onStop() {
        super.onStop();
        EMChatManager.getInstance().unregisterEventListener(eventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler = null;
    }

    String toChatName;
//    String friendImageUrl;
    EMConversation conversation;
    private void initData() {
        conversation = EMChatManager.getInstance().getConversation(toChatName);
        conversation.markAllMessagesAsRead();

        adapter = new ChatMessageAdapter(getActivity(), toChatName, mListView);
        adapter.setHandler(mHandler);
        mListView.setAdapter(adapter);

        String myImageUrl = IMUtils.getHead();
        adapter.setImageUrl(myImageUrl);
        adapter.setFriendImageUrl(friend.getAvatar());
        adapter.selectLast();
    }

    private void initView(View view) {
        mRefreshView = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mRefreshView.setColorSchemeResources(R.color.primary_green);
        mListView = (ListView) view.findViewById(R.id.listview);
        mListView.setDivider(null);
        etContent = (EditText) view.findViewById(R.id.et_chat_content);
        mergeView = view.findViewById(R.id.btn_chat_merge);
        mergeView.setOnClickListener(this);
        sendView = view.findViewById(R.id.btn_chat_send);
        sendView.setOnClickListener(this);

        mRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mListView.getFirstVisiblePosition() == 0 && !isloading && adapter.getCount() > 0) {
                            isloading = true;
                            List<EMMessage> messages = conversation.loadMoreMsgFromDB(adapter.getItem(0).getMsgId(), pageSize);
                            if (messages != null && messages.size() > 0) {
                                adapter.notifyDataSetChanged();
                                adapter.refreshSeekTo(messages.size() - 1);
                            }

                            isloading = false;
                        }

                        mRefreshView.setRefreshing(false);
                    }
                }, 1000);

            }
        });

        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (sendViewIsShow) {
                    //已经显示,判断为空的时候
                    if (TextUtils.isEmpty(s)) {
                        //隐藏
                        hideSendView();
                    }

                } else {
                    //未显示
                    if (!TextUtils.isEmpty(s)) {
                        //显示
                        showSendView();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etContent.clearFocus();

    }

    /**
     * 检查是否可以合并
     */
    private void checkCanMerge(){
        //防止多次点击，合并按钮。
        mergeView.setEnabled(false);
        //carinter.do?action=preticketuion&uin=hx21783&touin=hx21691
        //{"result":"1","errmsg":"可以合体"}

        Map<String, String> params = URLUtils.createParamsMap();
        params.put("action","preticketuion");
        params.put("uin", EMChatManager.getInstance().getCurrentUser());
        params.put("touin",toChatName);
        String url = URLUtils.createUrl(TCBApp.mServerUrl, "carinter.do", params);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                mergeView.setEnabled(true);
                try {
                    String result = jsonObject.getString("result");
                    String error = jsonObject.getString("errmsg");
                    if ("1".equals(result)) {
                        preSendMerge();
                    } else if ("0".equals(result)) {
                        if (!TextUtils.isEmpty(error)) {
                            Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mergeView.setEnabled(true);
            }
        });

        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    private void preSendMerge(){
        Intent intent = new Intent();
        intent.setClass(getActivity(), SelectTicketMergeActivity.class);

        startActivityForResult(intent, REQUEST_CODE_SELECT_TICKET);

//        Intent intent = new Intent();
//        intent.setClass(getActivity(), MainActivity.class);
//        intent.putExtra(MainActivity.ARG_FRAGMENT, MainActivity.FRAGMENT_TICKET_MERGE);
//        startActivityForResult(intent, REQUEST_CODE_SELECT_TICKET);
    }

    static int REQUEST_CODE_SELECT_TICKET = 0x003;
    public static int REQUEST_CODE_RECEIVE_MERGE = 0x005;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.i("onActivityResult");
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_TICKET){
                Coupon coupon = data.getParcelableExtra("coupon");
                LogUtils.i(coupon.toString());
                sendMergeMsg(coupon.id);
            } else if(requestCode == REQUEST_CODE_RECEIVE_MERGE) {
                LogUtils.i("合并返回");
                adapter.selectLast();
            }
        }

    }
    
    private void sendMergeMsg(String tid){
        //carinter.do?action=reqticketuion&mobile=15210932334&tid=44807
        Map<String, String> params = URLUtils.createParamsMap();
        params.put("action","reqticketuion");
        params.put("mobile",TCBApp.mMobile);
        params.put("tid",tid);
        //合并券，控制一次参数
        params.put("touin",toChatName);

        String url = URLUtils.createUrl(TCBApp.mServerUrl, "carinter.do", params);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                //{"id":"8","result":"1","carnumber":"null","wxname":"邓功财","wximgurl":"http://wx.qlogo.cn/mmopen/PiajxSqBRaELVJXzrE92vUeEVsTWySHibJ2GbtQY58cyqD8nmN1uLl3mdZuNhXP7TrZM7riaVQZgl6QgLvC9TTlqQ/0","errmsg":"合体请求成功"}
                try {
                    //result :1可以体，-1已经与对方合体，0没有可合体的停车券
                    String result = jsonObject.getString("result");
                    if ("-1".equals(result)) {
                        Toast.makeText(getActivity(),"已经与对方合体",Toast.LENGTH_SHORT).show();
                    } else if("0".equals(result)) {
                        Toast.makeText(getActivity(),"没有可合体的停车券",Toast.LENGTH_SHORT).show();
                    } else if ("1".equals(result)) {
                        EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
                        TextMessageBody txtBody = new TextMessageBody("发起合并请求");
                        message.addBody(txtBody);
                        message.setAttribute(IMConstant.MSG_TYPE_TICKET_MERGE, true);
                        message.setAttribute(IMConstant.MSG_ATTR_MERGE_ID, jsonObject.getString("id"));
                        message.setAttribute(IMConstant.MSG_ATTR_MERGE_RECEIVE, true);
                        message.setReceipt(toChatName);

                        conversation.addMessage(message);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.selectLast();
                            }
                        });
                        EMChatManager.getInstance().sendMessage(message, new EMCallBackAdapter(){
                            @Override
                            public void onSuccess() {
                                LogUtils.i("onSuccess");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.selectLast();
                                    }
                                });
                            }

                            @Override
                            public void onError(int i, String s) {
                                LogUtils.i(String.format("%s >> %d", s, i));
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.selectLast();
                                    }
                                });
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, null);

        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    void handleStartReslutActivity(Intent intent){
        startActivityForResult(intent, REQUEST_CODE_RECEIVE_MERGE);
    }


    private void sendMsg(){
        //关闭软键盘
//        InputMethodManager imm =(InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(etContent.getWindowToken(), 0);

        String content = etContent.getText().toString();
        if (TextUtils.isEmpty(content)){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(TCBApp.getAppContext(), "内容不能为空", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        //发送
        EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
        TextMessageBody txtBody = new TextMessageBody(etContent.getText().toString());
        message.addBody(txtBody);

        message.setReceipt(toChatName);

        //允许失败重新发送
        conversation.addMessage(message);
        adapter.selectLast();
        etContent.setText("");

        EMChatManager.getInstance().sendMessage(message,new EMCallBackAdapter(){
            @Override
            public void onSuccess() {
                Activity activity = getActivity();
                if (activity != null && activity instanceof ChatActivity) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.selectLast();
                        }
                    });

                }

            }

            @Override
            public void onError(int i, String s) {
                LogUtils.i(String.format("%s >> %d", s, i));
                Activity activity = getActivity();
                if (activity != null && activity instanceof ChatActivity) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.selectLast();
                        }
                    });
                }
            }
        });

    }

    private void refreshUIWithNewMsg(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.selectLast();
            }
        });
    }

    private void showSendView() {
        sendViewIsShow = true;
        sendView.setVisibility(View.VISIBLE);
        mergeView.setVisibility(View.GONE);
    }

    private void hideSendView() {
        sendViewIsShow = false;
        sendView.setVisibility(View.GONE);
        mergeView.setVisibility(View.VISIBLE);
    }

    @Override
    protected String getTitle() {
        return "";
    }

    public final static int MSG_WHAT_START_RESLUT_ACTIVITY = 1;
    public final static int MSG_WHAT_START_ACTIVITY = 2;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_chat_merge) {
//            preSendMerge();
            checkCanMerge();
        } else if (v.getId() == R.id.btn_chat_send) {
            sendMsg();
        }
    }

    class ChatHandler extends Handler{
        SoftReference<ChatFragment> mChatFragment;
        public ChatHandler(ChatFragment cf){
            mChatFragment = new SoftReference<ChatFragment>(cf);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent = (Intent) msg.obj;
            if (MSG_WHAT_START_RESLUT_ACTIVITY == msg.what){
                if (mChatFragment.get() != null) {
                    mChatFragment.get().handleStartReslutActivity(intent);
                }
            } else if (MSG_WHAT_START_ACTIVITY == msg.what) {
                if (mChatFragment.get() != null) {
                    mChatFragment.get().startActivity(intent);
                }
            }
        }
    }
    class IMEventListener implements EMEventListener{

        @Override
        public void onEvent(EMNotifierEvent event) {
            switch (event.getEvent()) {
                case EventNewMessage:
                    LogUtils.i("EventNewMessage");
                    EMMessage message = (EMMessage) event.getData();
                    String username = message.getFrom();

                    if (username.equals(toChatName)){
                        refreshUIWithNewMsg();
                    } else {
                        HXSDKHelper.getInstance().getNotifier().onNewMsg(message);
                    }

                    break;

                case EventDeliveryAck:
                    LogUtils.i("EventDeliveryAck");
                    break;

                case EventReadAck:
                    LogUtils.i("EventReadAck");
                    break;

                case EventOfflineMessage:
                    LogUtils.i("EventOfflineMessage");
                    break;

                default:
                    break;
            }
        }
    }

}
