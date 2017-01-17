package com.tq.zld.view.manager;

import com.easemob.EMConnectionListener;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.EMValueCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMMessage;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.im.IMConstant;
import com.tq.zld.im.adapter.IMContactListenerAdapter;
import com.tq.zld.im.bean.InviteMessage;
import com.tq.zld.im.bean.User;
import com.tq.zld.im.db.InviteMessgeDao;
import com.tq.zld.im.db.UserDao;
import com.tq.zld.im.lib.HXSDKHelper;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.holder.MenuHolder;
import com.tq.zld.view.map.MapActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by GT on 2015/10/8.
 * IM管理，单例
 */
public class IMManager {
    private static IMManager instance;
    private MapActivity mActivity;
    private UserDao mUserDao;
    private InviteMessgeDao mInviteMessageDao;
    private boolean mIsHXInit = false;

    private IMManager(MapActivity activity){
        this.mActivity = activity;
        this.mUserDao = new UserDao(TCBApp.getAppContext());
        this.mInviteMessageDao = new InviteMessgeDao(TCBApp.getAppContext());
    }

    /**
     * 可能返回实例，MapActivity初始化的话。
     * @return 可能为null
     */
    public static synchronized IMManager getInstance(){
        return instance;
    }

    /**
     * MapActivity类才能调用,
     * @param activity
     * @return 一定会返回实例。
     */
    public static synchronized IMManager getInstance(MapActivity activity) {
        if (instance == null) {
            instance = new IMManager(activity);
        }

        return instance;
    }

    public void regEventListener(){
        EMChatManager.getInstance().registerEventListener(eventListener,
                new EMNotifierEvent.Event[]{
                    EMNotifierEvent.Event.EventNewMessage,
                    EMNotifierEvent.Event.EventOfflineMessage
                });
    }

    public void unregEventListener(){
        EMChatManager.getInstance().unregisterEventListener(eventListener);
    }

    public void initHX(){
        if (!mIsHXInit) {
            mIsHXInit = true;
            EMChatManager.getInstance().addConnectionListener(connectionListener);
            EMContactManager.getInstance().setContactListener(contactListener);
        }
    }

    public void destoryHX(){
        if (mIsHXInit) {
            EMChatManager.getInstance().removeConnectionListener(connectionListener);
            EMContactManager.getInstance().removeContactListener();
            mIsHXInit = false;
        }
    }

    void notifyNewIviteMessage(InviteMessage msg){
        // 提示有新消息
        HXSDKHelper.getInstance().getNotifier().viberateAndPlayTone(null);

        mInviteMessageDao.saveMessage(msg);
        User newFriend = TCBApp.hxsdkHelper.getContactList().get(IMConstant.ITEM_NEW_FRIENDS);
        if (newFriend != null) {
            newFriend.unreadMsgCountIncrease();
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MenuHolder.getInstance().refreshMenu(R.id.rl_menu_friend, true);
                }
            });
        }
    }

    void asyncFetchContactsFromServer(){
        LogUtils.i("asyncFetchContactsFromServer");
        HXSDKHelper.getInstance().asyncFetchContactsFromServer(new EMValueCallBack<List<String>>() {
            @Override
            public void onSuccess(List<String> usernames) {
                LogUtils.i("onSuccess" + usernames.toString());
                Map<String, User> userlist = new HashMap<String, User>();
                Map<String, User> localUsers = TCBApp.getAppContext().hxsdkHelper.getContactList();
                for (String username : usernames) {
                    if (!localUsers.containsKey(username)) {
                        User user = new User();
                        user.setUsername(username);
//                    setUserHearder(username, user);
                        userlist.put(username, user);
                    }

                }

                //添加通知信息User
                User newFriend = new User();
                newFriend.setUsername(IMConstant.ITEM_NEW_FRIENDS);
                newFriend.setNick("new_friend");
                userlist.put(newFriend.getUsername(), newFriend);

                if (userlist.size() > 0) {
                    localUsers.putAll(userlist);
                    List<User> users = new ArrayList<User>(userlist.values());
                    mUserDao.saveContactList(users);

                    if (userlist.size() > 1) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MenuHolder.getInstance().refreshMenu(R.id.rl_menu_friend, true);
                            }
                        });
                        //需要获取用户的额外信息
                        TCBApp.getAppContext().saveBoolean(R.string.sp_im_user_head_init, false);
                    }
                }

//                HXSDKHelper.getInstance().notifyContactsSyncListener(true);
                HXSDKHelper.getInstance().notifyForRecevingEvents();
            }

            @Override
            public void onError(int i, String s) {
                LogUtils.e(s + " >> " + i);
            }
        });
    }

    class IMContactListener extends IMContactListenerAdapter {

        @Override
        public void onContactAdded(List<String> list) {
            LogUtils.i("onContactAdded" + list.toString());
            Map<String, User> localUsers = TCBApp.getAppContext().hxsdkHelper.getContactList();
            Map<String, User> toAddUsers = new HashMap<String, User>();
            for (String username : list) {
                User user = new User(username);
                // 添加好友时可能会回调added方法两次
                if (!localUsers.containsKey(username)) {
                    toAddUsers.put(username, user);
                    mUserDao.saveContact(user);
                }
            }

            if (toAddUsers.size() > 0) {
                localUsers.putAll(toAddUsers);
                //需要获取用户的额外信息
                TCBApp.getAppContext().saveBoolean(R.string.sp_im_user_head_init,false);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MenuHolder.getInstance().refreshMenu(R.id.rl_menu_friend, true);
                    }
                });
            }

            //打飞机的会设置，请求状态，但是没有设置同意状态。在这里设置哪些没有同意，就被添加的好友。
            List<InviteMessage> messagesList = mInviteMessageDao.getMessagesList();
            for (String username : list) {
                for (InviteMessage msg:messagesList) {
                    if (msg.getFrom().equals(username)) {
                        if (msg.getStatus() == InviteMessage.InviteMesageStatus.BEINVITEED) {
                            msg.setStatus(InviteMessage.InviteMesageStatus.AGREED);
                            mInviteMessageDao.saveMessage(msg);
                        }
                        break;
                    }
                }
            }

        }

        @Override
        public void onContactInvited(String username, String reason) {
            LogUtils.i("onContactInvited>>" + "username>>" + username + ", reason >>" + reason);
            List<InviteMessage> messagesList = mInviteMessageDao.getMessagesList();
            for (InviteMessage msg:messagesList) {
                if (msg.getGroupId() == null && msg.getFrom().equals(username)) {
                    mInviteMessageDao.deleteMessage(username);
                }
            }

            InviteMessage inviteMsg = new InviteMessage();
            inviteMsg.setFrom(username);
            inviteMsg.setTime(System.currentTimeMillis());
            inviteMsg.setReason(reason);
            inviteMsg.setStatus(InviteMessage.InviteMesageStatus.BEINVITEED);
            notifyNewIviteMessage(inviteMsg);
        }

        @Override
        public void onContactAgreed(String username) {
//            List<InviteMessage> messagesList = mInviteMessageDao.getMessagesList();
//            for (InviteMessage msg:messagesList) {
//                if (msg.getGroupId() == null && msg.getFrom().equals(username)) {
//                    return;
//                }
//            }
//
//            InviteMessage inviteMsg = new InviteMessage();
//            inviteMsg.setFrom(username);
//            inviteMsg.setTime(System.currentTimeMillis());
//            inviteMsg.setStatus(InviteMessage.InviteMesageStatus.BEAGREED);
//            notifyNewIviteMessage(inviteMsg);
        }
    }

    class IMConnectionListener implements EMConnectionListener {
        @Override
        public void onConnected() {
            LogUtils.i("onConnected");
//            if (HXSDKHelper.getInstance().isContactsSyncedWithServer()){
//                LogUtils.i("不加载通讯录");
//                //通知HX，可以接受EVENT，否则接受不到，消息和通讯录的变化
//                HXSDKHelper.getInstance().notifyForRecevingEvents();
//            } else {
//                if (!HXSDKHelper.getInstance().isContactsSyncedWithServer()){
//                    LogUtils.i("加载通讯录");
//                    asyncFetchContactsFromServer();
//                }
//            }

            LogUtils.i("加载通讯录");
            //需要获取用户的额外信息
            TCBApp.getAppContext().saveBoolean(R.string.sp_im_user_head_init, false);
            asyncFetchContactsFromServer();

        }

        @Override
        public void onDisconnected(int i) {
            //账号被移除，多出登录挤掉。
            LogUtils.e(">>>" + i);
        }
    }

    private EMEventListener eventListener = new EMEventListener() {
        @Override
        public void onEvent(EMNotifierEvent event) {
            switch (event.getEvent()) {
                case EventNewMessage:
                    EMMessage message = (EMMessage) event.getData();
                    HXSDKHelper.getInstance().getNotifier().onNewMsg(message);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MenuHolder.getInstance().refreshMenu(R.id.rl_menu_friend, true);
                        }
                    });

                    break;
                case EventOfflineMessage:
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MenuHolder.getInstance().refreshMenu(R.id.rl_menu_friend, true);
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    };

    private IMConnectionListener connectionListener = new IMConnectionListener();
    private IMContactListener contactListener = new IMContactListener();

}
