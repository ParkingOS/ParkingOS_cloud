package com.tq.zld.view.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.adapter.FriendAdapter;
import com.tq.zld.bean.UserHead;
import com.tq.zld.im.IMConstant;
import com.tq.zld.im.bean.InviteMessage;
import com.tq.zld.im.bean.User;
import com.tq.zld.im.db.InviteMessgeDao;
import com.tq.zld.im.db.UserDao;
import com.tq.zld.im.lib.HXSDKHelper;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.holder.MenuHolder;
import com.tq.zld.view.im.ChatActivity;
import com.tq.zld.view.map.X5WebActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by GT on 2015/9/2.
 */
public class FriendFragment extends BaseFragment {

    private UserDao mUserDao;
    private InviteMessgeDao mInviteDao;
    private View mEmptyView;
    private Button mEmptyButton;
    private TextView mEmptyText;
    private PlayGameOnClickListener playGameOnClickListener = new PlayGameOnClickListener();;
    IMEventListener eventListener = new IMEventListener();
    private boolean mIsPlayGame = false;
    private ListView mListView;
    private FriendAdapter mAdapter;
    private NewFriendFragment mNewFriendFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserDao = new UserDao(TCBApp.getAppContext());
        mInviteDao = new InviteMessgeDao(TCBApp.getAppContext());
        mNewFriendFragment = new NewFriendFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
//        initData();
    }

    private List<User> initData() {
        Map<String, User> contactList = TCBApp.getAppContext().hxsdkHelper.getContactList();
        LogUtils.i(contactList.values().toString());
        List<User> infos = new ArrayList<>();
        infos.addAll(contactList.values());

        if (infos.size() > 0) {
            if (TextUtils.isEmpty(infos.get(0).getPlate())) {
                LogUtils.i("并没有获取到user详细信息，重置，flag");
                TCBApp.getAppContext().saveBoolean(R.string.sp_im_user_head_init, false);
            }
        }

        //重新获取
//        TCBApp.getAppContext().saveBoolean(R.string.sp_im_user_head_init, false);
        return sortUser(infos);
    }

    private void initView(View v) {
        mListView = (ListView) v.findViewById(R.id.listview);
        mAdapter = new FriendAdapter(getActivity(), initData());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAdapter.getItemViewType(position) == FriendAdapter.TYPE_NEW_FRIEND) {
//                    Intent intent = new Intent();
//                    intent.setClass(getActivity(), MainActivity.class);
//                    intent.putExtra(MainActivity.ARG_FRAGMENT, MainActivity.FRAGMENT_NEW_FRIEND);
//                    startActivity(intent);
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.fragment_container, mNewFriendFragment);
                    fragmentTransaction.commit();
                } else {
                    LogUtils.i("onItemClick " + position);
                    openChatFragment(mAdapter.getItem(position));
                }
            }
        });

        mEmptyView = v.findViewById(R.id.rl_page_null);
        mEmptyButton = (Button) v.findViewById(R.id.btn_page_null);
        mEmptyText = (TextView) v.findViewById(R.id.tv_page_null);

        mListView.setEmptyView(mEmptyView);
        mEmptyButton.setText("去打飞机，加机友");
        mEmptyButton.setVisibility(View.VISIBLE);
        mEmptyButton.setOnClickListener(playGameOnClickListener);
        mEmptyText.setText("你还没有车友");
    }

    private void refreshFriend(){
        //carinter.do?action=gethxheads&mobile=13641309140&hxname=hx21691
        LogUtils.i("获取user详细信息");
        Map<String, String> params = URLUtils.createParamsMap();
        params.put("action", "gethxheads");
        params.put("mobile", TCBApp.mMobile);
        String url = URLUtils.createUrl(TCBApp.mServerUrl, "carinter.do", params);
        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "请稍候...", true, true);
        GsonRequest<ArrayList<UserHead>> request = new GsonRequest<ArrayList<UserHead>>(url, new TypeToken<ArrayList<UserHead>>() {
        }, new Response.Listener<ArrayList<UserHead>>() {
            @Override
            public void onResponse(ArrayList<UserHead> userHeads) {
                dialog.dismiss();

                if (userHeads != null && userHeads.size() > 0) {
                    User newFriend = TCBApp.hxsdkHelper.getContactList().get(IMConstant.ITEM_NEW_FRIENDS);
                    List<User> users = UserHead.getUsers(userHeads);
                    LogUtils.i("存数据库" + users.toString());
                    mUserDao.updateContactList(users);
                    TCBApp.getAppContext().saveBoolean(R.string.sp_im_user_head_init, true);
                    Map<String, User> data = mUserDao.getContactList();
                    data.remove(IMConstant.ITEM_NEW_FRIENDS);
                    data.put(IMConstant.ITEM_NEW_FRIENDS, newFriend);

                    TCBApp.getAppContext().hxsdkHelper.setContactList(data);
                    mAdapter.setData(sortUser(new ArrayList<>(data.values())));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dialog.dismiss();
            }
        });

        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    private void openChatFragment(User friend) {
        LogUtils.i(String.format("chat to %s", friend.getUsername()));

        //之前就是fragment之间的切换，会出现bug，几率很小
        Intent intent = new Intent();
        intent.setClass(getActivity(), ChatActivity.class);
        intent.putExtra(ChatFragment.USER, friend);
        startActivity(intent);
    }

    @Override
    protected String getTitle() {
//        String myName = HXSDKHelper.getInstance().getHXId();
        String myName = EMChatManager.getInstance().getCurrentUser();
//        String title = String.format("%s的基友", myName);
        String title = "我的机友";
        return title;
    }

    private void refreshUIWithNewMsg() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.sortAndRefresh(sortUser(mAdapter.getData()));
                //清楚侧滑菜单中的消息提示
                MenuHolder.getInstance().refreshMenu(R.id.rl_menu_friend, false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //刷新消息未读数量
        mAdapter.sortAndRefresh(sortUser(mAdapter.getData()));
        EMChatManager.getInstance().registerEventListener(
                eventListener,
                new EMNotifierEvent.Event[]{
                        EMNotifierEvent.Event.EventNewMessage
                });
//        if (mIsPlayGame) {
//            mIsPlayGame = false;
//            initData();
//        }

        if (!TCBApp.getAppContext().readBoolean(R.string.sp_im_user_head_init, false)) {
            refreshFriend();
        }

        //清楚侧滑菜单中的消息提示
        MenuHolder.getInstance().refreshMenu(R.id.rl_menu_friend, false);
    }

    @Override
    public void onStop() {
        super.onStop();
        EMChatManager.getInstance().unregisterEventListener(eventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TCBApp.getAppContext().cancelPendingRequests(this);
    }

    private List<User> sortUser(List<User> users) {
        if (users == null || users.size() == 0) {
            return new ArrayList<>();
        }

        long start = SystemClock.uptimeMillis();
        LogUtils.i("开始排序" + start);
        List<Pair<Long, User>> sortList = new ArrayList<Pair<Long, User>>();
        EMConversation conversation;
        List<User> rest = new ArrayList<>();
        for (User u : users) {
            if (u.getUsername().equals(IMConstant.ITEM_NEW_FRIENDS)) {
                continue;
            }

            conversation = EMChatManager.getInstance().getConversation(u.getUsername());
            if (conversation.getAllMessages().size() != 0) {
                sortList.add(new Pair<Long, User>(conversation.getLastMessage().getMsgTime(),u));
            } else {
                rest.add(u);
            }
        }

        Collections.sort(sortList, new Comparator<Pair<Long, User>>() {
            @Override
            public int compare(final Pair<Long, User> con1, final Pair<Long, User> con2) {

                if (con1.first == con2.first) {
                    return 0;
                } else if (con2.first > con1.first) {
                    return 1;
                } else {
                    return -1;
                }
            }

        });

        List<User> sort = new ArrayList<>();
        for (Pair<Long, User> p: sortList) {
            sort.add(p.second);
        }

        sort.addAll(rest);

        LogUtils.i("排序结束,耗时 " + (SystemClock.uptimeMillis() - start) + " ms");

        User newFriend = TCBApp.getAppContext().hxsdkHelper.getContactList().get(IMConstant.ITEM_NEW_FRIENDS);
        sort.remove(newFriend);
        List<InviteMessage> messagesList = mInviteDao.getMessagesList();
        //有新的好友才会显示。有需要接受的请求才显示。
        if (messagesList != null && messagesList.size() > 0) {
            for (InviteMessage msg:messagesList) {
                if (msg.getStatus() == InviteMessage.InviteMesageStatus.BEINVITEED) {
                    sort.add(0, newFriend);
                    break;
                }
            }
        }

        return sort;
    }

    class PlayGameOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
//            mIsPlayGame = true;
            Intent intent = new Intent(TCBApp.getAppContext(), X5WebActivity.class);
            intent.putExtra(X5WebActivity.ARG_TITLE, "打灰机");// 标题改变时会影响游戏界面逻辑，请参考WebActivity
            intent.putExtra(X5WebActivity.ARG_URL, TCBApp.mServerUrl + "flygame.do?action=pregame&mobile=" + TCBApp.mMobile);
            startActivity(intent);
        }
    }

    class IMEventListener implements EMEventListener {

        @Override
        public void onEvent(EMNotifierEvent event) {
            switch (event.getEvent()) {
                case EventNewMessage:
                    LogUtils.i("EventNewMessage");
                    EMMessage message = (EMMessage) event.getData();
                    String username = message.getFrom();
                    HXSDKHelper.getInstance().getNotifier().onNewMsg(message);
                    refreshUIWithNewMsg();
                    break;

                default:
                    break;
            }
        }
    }



}
