package com.tq.zld.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.Message;
import com.tq.zld.view.map.WebActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MessageFragment extends NetworkFragment<ArrayList<Message>> {

    public static final int PAGE_SIZE = 10;

    private int mPage = 1;
    private MessageAdapter mAdapter;

    private Button mFootView;

    private long mLastReadID;

    private HashMap<String, String> params;

    private SharedPreferences mAccountSharedPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountSharedPrefs = getActivity().getSharedPreferences(TCBApp.mMobile, Context.MODE_PRIVATE);
        // 老版本存储的msgid类型为int可能造成类型转换异常
        try {
            mLastReadID = mAccountSharedPrefs.getLong(getString(R.string.sp_recently_msg_id), 0);
        } catch (ClassCastException e) {
            e.printStackTrace();
            mLastReadID = Long.parseLong(String.valueOf(mAccountSharedPrefs.getInt(getString(R.string.sp_recently_msg_id), 0)));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    protected String getTitle() {
        return "消息中心";
    }

    @Override
    public void onClick(View v) {
        if (v == mFootView) {
            if (getString(R.string.load_more).equals(
                    mFootView.getText().toString())) {
                mFootView.setText(getString(R.string.loading));
                getData();
            }
        }
    }

    @Override
    protected TypeToken<ArrayList<Message>> getBeanListType() {
        return new TypeToken<ArrayList<Message>>() {
        };
    }

    @Override
    protected Class<ArrayList<Message>> getBeanClass() {
        return null;
    }

    @Override
    protected String getUrl() {
        return TCBApp.mServerUrl + "carowner.do";
    }

    @Override
    protected Map<String, String> getParams() {
        if (params == null) {
            params = new HashMap<>();
            params.put("mobile", TCBApp.mMobile);
            params.put("action", "getmesg");
        }
        params.put("page", String.valueOf(mPage));
        return params;
    }

    @Override
    protected void initView(View view) {

        ListView listView = (ListView) view.findViewById(R.id.lv_messages);
        View footView = View.inflate(getActivity(), R.layout.listitem_foot,
                null);
        mFootView = (Button) footView.findViewById(R.id.btn_listitem_foot);
        mFootView.setText(getString(R.string.load_more));
        mFootView.setOnClickListener(this);
        listView.addFooterView(footView);
        if (mAdapter == null) {
            mAdapter = new MessageAdapter();
        }
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Message message = (Message) mAdapter.getItem(position);
                if (message != null && Message.TYPE_ACTIVITY == message.type) {
                    startWebActivity(message);
                }
            }

            private void startWebActivity(Message message) {
                Intent intent = new Intent(TCBApp.getAppContext(),
                        WebActivity.class);
                intent.putExtra(WebActivity.ARG_TITLE, message.title);
                intent.putExtra(WebActivity.ARG_URL, TCBApp.mServerUrl
                        + "activity.do?action=detail&id=" + message.id
                        + "&mobile=" + TCBApp.mMobile);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onNetWorkResponse(ArrayList<Message> mMessages) {
        if (mPage == 1) {
            if (mMessages == null || mMessages.size() == 0) {
                showEmptyView("暂无任何消息", 0, null);
            } else {
                showDataView();
                updateListView(mMessages);
            }
        } else {
            // showDataView();
            updateListView(mMessages);
        }
    }

    private void updateListView(ArrayList<Message> mMessages) {
        mAdapter.setData(mMessages);
        mPage++;
        if (mMessages.size() < PAGE_SIZE) {
            mFootView.setText(getString(R.string.no_more_data));
        } else {
            mFootView.setText(getString(R.string.load_more));
        }
    }

    @Override
    protected int getFragmentContainerResID() {
        return R.id.fragment_container;
    }

    @Override
    public void onDestroyView() {
        // 保存最大消息编号到本地
        Message message = (Message) mAdapter.getItem(0);
        if (message != null) {
            mLastReadID = message.id;
            mAccountSharedPrefs.edit().putLong(getString(R.string.sp_recently_msg_id), mLastReadID).apply();
        }
        super.onDestroyView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (mFootView != null && getString(R.string.loading).equals(mFootView.getText())) {
                mFootView.setText(getString(R.string.load_more));
            }
        }
    }

    class MessageAdapter extends BaseAdapter {

        private ArrayList<Message> mMessages;

        public void setData(ArrayList<Message> messages) {
            if (messages == null || messages.size() == 0) {
                return;
            }
            if (mMessages == null) {
                this.mMessages = messages;
            } else {
                this.mMessages.addAll(messages);
            }
            notifyDataSetChanged();
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public int getCount() {
            return mMessages == null ? 0 : mMessages.size();
        }

        @Override
        public Object getItem(int position) {
            return mMessages == null ? null : mMessages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Message message = mMessages.get(position);
            ViewHolder mHolder;
            if (convertView != null) {
                mHolder = (ViewHolder) convertView.getTag();
            } else {
                convertView = View.inflate(TCBApp.getAppContext(),
                        R.layout.listitem_message, null);
                mHolder = new ViewHolder();
                mHolder.tvTitle = (TextView) convertView
                        .findViewById(R.id.tv_title);
                mHolder.tvTime = (TextView) convertView
                        .findViewById(R.id.tv_time);
                mHolder.tvContent = (TextView) convertView
                        .findViewById(R.id.tv_content);
                mHolder.ivRead = (ImageView) convertView
                        .findViewById(R.id.iv_read);
                convertView.setTag(mHolder);
            }
            mHolder.tvTitle.setText(message.title);
            mHolder.tvTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm",
                    Locale.CHINA).format(new Date(
                    Long.parseLong(message.ctime) * 1000)));
            mHolder.tvContent.setText(message.content);

            // 设置已读未读
            int ivReadVisibility = message.id > mLastReadID ? View.VISIBLE
                    : View.INVISIBLE;
            mHolder.ivRead.setVisibility(ivReadVisibility);

            return convertView;
        }
    }

    static class ViewHolder {
        TextView tvTitle;
        TextView tvTime;
        TextView tvContent;
        ImageView ivRead;
    }
}