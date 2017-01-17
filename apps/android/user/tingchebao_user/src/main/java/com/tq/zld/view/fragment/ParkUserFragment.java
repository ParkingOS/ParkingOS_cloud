package com.tq.zld.view.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.adapter.BaseArrayAdapter;
import com.tq.zld.bean.ParkInfo;
import com.tq.zld.bean.ParkUser;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.util.DensityUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.holder.EmptyViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Gecko on 2015/10/30.
 */
public class ParkUserFragment extends ListFragment {
    public static final String ARGS_PARK_NAME = "park_name";
    public static final String ARGS_PARK_ID = "park_id";

    public static final String KEY_INVITE_USER_ID = "key_invite_user_id";

    View mFooter;
    EmptyViewHolder mHolder;
    ParkUserAdapter mAdapter;
    List<ParkUser> mList = new ArrayList<>();
    String mId;
    private String mParkName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ParkUserAdapter(getActivity(), mList);
        mId = getArguments().getString(ARGS_PARK_ID, "-1");
        mParkName = getArguments().getString(ARGS_PARK_NAME, "未知车场");

        getData();
    }

    @Override
    public void onResume() {
        super.onResume();
        String title = mParkName + "车友";
        getActivity().setTitle(title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFooter = inflater.inflate(R.layout.listitem_foot, null);
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHolder = new EmptyViewHolder(view);

        mHolder.setEmptyText("该车场最近没有车友来过", null);
        getListView().setEmptyView(mHolder.mEmptyPageView);
        setListAdapter(mAdapter);
        getListView().setDivider(new ColorDrawable(0xfff0f0f0));
        getListView().setDividerHeight(DensityUtils.dip2px(getActivity(), 7));
    }

    private void getData() {
        /*
        //查询这个车场停车的车主
            carinter.do?action=parkcars&id=1197
            返回：
            [{"id":"21617","wx_imgurl":"","car_number":"使SVN***7","isfriend":"1"}]
         */
        Map<String, String> paramsMap = URLUtils.createParamsMap();
        paramsMap.put("action", "parkcars");
        paramsMap.put("id", String.valueOf(mId));
        String url = URLUtils.createUrl(TCBApp.mServerUrl, "carinter.do", paramsMap);
        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "请稍候...", true, true);
        GsonRequest<ArrayList<ParkUser>> request = new GsonRequest<ArrayList<ParkUser>>(url, new TypeToken<ArrayList<ParkUser>>() {
        }, new Response.Listener<ArrayList<ParkUser>>() {
            @Override
            public void onResponse(ArrayList<ParkUser> parkUsers) {
                dialog.dismiss();
                if (parkUsers != null && parkUsers.size() > 0) {
                    mAdapter.setData(parkUsers);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mHolder.setEmptyText("网络不好，点击重试", new View.OnClickListener() {
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

    int requestCode = 0x0011;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.i(String.format("request code is %d", requestCode));

        if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
            String userId = data.getStringExtra(KEY_INVITE_USER_ID);
            LogUtils.i(String.format("userId = %s", userId));
            mAdapter.changeUserStatus(userId);
        }
    }

    class ParkUserAdapter extends BaseArrayAdapter<ParkUser> {


        private final DisplayImageOptions imageOptions;

        public ParkUserAdapter(Context context, List<ParkUser> list) {
            super(context, list);
            imageOptions = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.ic_chat_head_default)
                    .showImageOnFail(R.drawable.ic_chat_head_default)
                    .showImageOnLoading(R.drawable.ic_chat_head_default)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
        }

        public void changeUserStatus(String userId) {
            int change = 0;
            for (ParkUser u:this.mList) {
                if (userId.equals(u.id)) {
                    u.isfriend = 2;
                    change++;
                }
            }

            if (change > 0) {
                notifyDataSetChanged();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.listitem_park_user, null);
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.button = (Button) convertView.findViewById(R.id.button);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ParkUser pu = getItem(position);
            holder.text.setText(pu.car_number);
            holder.button.setTag(pu.id);
            holder.button.setOnClickListener(mAddFriendOnclickListener);
            ImageLoader.getInstance().displayImage(pu.wx_imgurl, holder.image, imageOptions);

            if (pu.isfriend == 1) {
                holder.button.setEnabled(false);
                holder.button.setText("已是好友");
            } else if (pu.isfriend == 0){
                holder.button.setEnabled(true);
                holder.button.setText("加为好友");
            } else if(pu.isfriend == 2) {
                holder.button.setEnabled(false);
                holder.button.setText("已申请");
            } else if(pu.isfriend == 3) {
                holder.button.setEnabled(false);
                holder.button.setText("已被申请");
            }

            return convertView;
        }
    }

    private View.OnClickListener mAddFriendOnclickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String userId = (String) v.getTag();
            Intent intent = new Intent();
            intent.setClass(getActivity(), MainActivity.class);
            intent.putExtra(MainActivity.ARG_FRAGMENT, MainActivity.FRAGMENT_SEND_VIRIFY);

            Bundle bundle = new Bundle();
            bundle.putString(SendVerifyFragment.ARGS_USER_ID, userId);
            bundle.putString(SendVerifyFragment.ARGS_PARK_NAME, mParkName);
            intent.putExtra(MainActivity.ARG_FRAGMENT_ARGS, bundle);

            startActivityForResult(intent, requestCode);
        }
    };

    static class ViewHolder {
        public ImageView image;
        public TextView text;
        public Button button;
    }
}
