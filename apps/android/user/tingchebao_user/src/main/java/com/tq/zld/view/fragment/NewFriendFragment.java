package com.tq.zld.view.fragment;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.adapter.BaseArrayAdapter;
import com.tq.zld.bean.InviteUserInfo;
import com.tq.zld.im.IMConstant;
import com.tq.zld.im.bean.InviteMessage;
import com.tq.zld.im.bean.User;
import com.tq.zld.im.db.InviteMessgeDao;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.holder.EmptyViewHolder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Gecko on 2015/10/31.
 */
public class NewFriendFragment extends ListFragment {

    private EmptyViewHolder mEmptyViewHolder;
    private List<InviteUserInfo> mList = new ArrayList<>();
    private NewFriendAdapter mAdapter;
    private InviteMessgeDao mInviteMessgeDao;
    private Map<String, InviteMessage> mMsgMap;
    private String mIds;

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("新的车友");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new NewFriendAdapter(getActivity(), mList);
        mInviteMessgeDao = new InviteMessgeDao(TCBApp.getAppContext());
        List<InviteMessage> messagesList = mInviteMessgeDao.getMessagesList();

        mMsgMap = new HashMap<>();
        StringBuilder ids = new StringBuilder();
        for (InviteMessage msg: messagesList) {
            ids.append(msg.getFrom()).append(',');
            mMsgMap.put(msg.getFrom(), msg);
        }

        if (ids.length() > 1) {
            mIds = ids.substring(0, ids.length() - 1);
        }
        LogUtils.i(messagesList.toString());
        LogUtils.i("mIds " + mIds);
//        mAdapter.setData(messagesList);
        getData();

        //未读数设置为0
        User newFriend = TCBApp.hxsdkHelper.getContactList().get(IMConstant.ITEM_NEW_FRIENDS);
        newFriend.setUnreadMsgCount(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmptyViewHolder = new EmptyViewHolder(view);
        mEmptyViewHolder.setEmptyText("还没有好友邀请", null);
        getListView().setEmptyView(mEmptyViewHolder.mEmptyPageView);
        setListAdapter(mAdapter);
    }

    private void sortData(ArrayList<InviteUserInfo> infos) {
        LogUtils.i("排序前<<，" + infos.toString());
        Collections.sort(infos, new Comparator<InviteUserInfo>() {
            @Override
            public int compare(InviteUserInfo lhs, InviteUserInfo rhs) {
                if (lhs.getTime() == rhs.getTime()) {
                    return 0;
                } else if (lhs.getTime() > rhs.getTime()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        LogUtils.i("排序后>>，" + infos.toString());
    }

    private void getData(){
        if (TextUtils.isEmpty(mIds)) {
            LogUtils.i("没有好友请求");
            return;
        }
        //carinter.do?action=getfriendhead&ids=hx21776,hx21770,hx21783
        Map<String, String> paramsMap = URLUtils.createParamsMap();
        paramsMap.put("action", "getfriendhead");
        paramsMap.put("ids", mIds);
        String url = URLUtils.createUrl(TCBApp.mServerUrl, "carinter.do", paramsMap);
        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "请稍候...", true, true);
        GsonRequest<ArrayList<InviteUserInfo>> request = new GsonRequest<ArrayList<InviteUserInfo>>(url, new TypeToken<ArrayList<InviteUserInfo>>() {
        }, new Response.Listener<ArrayList<InviteUserInfo>>() {
            @Override
            public void onResponse(ArrayList<InviteUserInfo> inviteUserInfos) {
                dialog.dismiss();
                if (inviteUserInfos != null && inviteUserInfos.size() >0) {
                    for (InviteUserInfo info: inviteUserInfos) {
                        info.setParent(mMsgMap.get(info.hx_name));
                    }
                    sortData(inviteUserInfos);
                    mAdapter.setData(inviteUserInfos);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEmptyViewHolder.setEmptyText("网络不好，点击重试", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getData();
                            }
                        });
                    }
                });
                dialog.dismiss();
            }
        });

        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    class NewFriendAdapter extends BaseArrayAdapter<InviteUserInfo> {

        private final DisplayImageOptions options;

        public NewFriendAdapter(Context context, List<InviteUserInfo> list) {
            super(context, list);
            options = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.ic_chat_head_default)
                    .showImageOnFail(R.drawable.ic_chat_head_default)
                    .showImageOnLoading(R.drawable.ic_chat_head_default)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.listitem_new_friend, null);
                holder.headImage = (ImageView) convertView.findViewById(R.id.iv_friend_head);
                holder.button = (Button) convertView.findViewById(R.id.button);
                holder.plate = (TextView) convertView.findViewById(R.id.tv_friend_plate);
                holder.reason = (TextView) convertView.findViewById(R.id.tv_friend_reason);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final InviteUserInfo item = getItem(position);
            holder.reason.setText(item.getReason());
            holder.plate.setText(item.car_number);

            holder.button.setTag(item.getFrom());
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acceptInvite(holder.button, item);
                }
            });

            ImageLoader.getInstance().displayImage(item.wx_imgurl, holder.headImage, options);

            switch (item.getStatus()) {
                case AGREED:
                    holder.button.setText("已接受");
                    holder.button.setEnabled(false);
                    break;
                case BEINVITEED:
                    holder.button.setText("接受");
                    holder.button.setEnabled(true);
                    break;
                case BEAGREED:
                    holder.button.setVisibility(View.GONE);
                    holder.reason.setText("已经同意你的好友请求");
                    break;
                default:
                    break;
            }

            return convertView;
        }

        void acceptInvite(final Button button, final InviteUserInfo msg) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //同意username的好友请求
                    try {
                        EMChatManager.getInstance().acceptInvitation(msg.getFrom());//需异步处理

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button.setText("已接受");
                                button.setEnabled(false);

                                //更新db状态
                                msg.setStatus(InviteMessage.InviteMesageStatus.AGREED);
                                ContentValues values = new ContentValues();
                                values.put(InviteMessgeDao.COLUMN_NAME_STATUS, msg.getStatus().ordinal());
                                mInviteMessgeDao.updateMessage(msg.getId(), values);
                            }
                        });

                        //carinter.do?action=addfriend&id=21667&mobile=13677226466
                        Map<String, String> paramsMap = URLUtils.createParamsMap();
                        paramsMap.put("action", "addfriend");
                        paramsMap.put("id", msg.getFrom());
                        paramsMap.put("mobile", TCBApp.mMobile);

                        //{"result":"1","errmsg":"添加成功！"}
                        String url = URLUtils.createUrl(TCBApp.mServerUrl, "carinter.do", paramsMap);
                        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                try {
                                    String result = jsonObject.getString("result");
                                    String errmsg = jsonObject.getString("errmsg");
                                    if ("1".equals(result)) {
                                        LogUtils.i(errmsg);
                                    } else {
                                        LogUtils.w(errmsg);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, null);
                        TCBApp.getAppContext().addToRequestQueue(request, NewFriendFragment.this);
                    } catch (EaseMobException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    static class ViewHolder {
        public ImageView headImage;
        public TextView plate;
        public TextView reason;
        public Button button;
    }
}
