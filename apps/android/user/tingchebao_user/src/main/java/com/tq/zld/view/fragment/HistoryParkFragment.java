package com.tq.zld.view.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.adapter.BaseArrayAdapter;
import com.tq.zld.bean.ParkInfo;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.util.DensityUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.holder.EmptyViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Gecko on 2015/10/30.
 */
public class HistoryParkFragment extends ListFragment {

    HistoryParkAdapter mAdapter;
    List<ParkInfo> mList = new ArrayList<>();
    View mHeader;
    private EmptyViewHolder mEmptyHolder;
    boolean isInitData = false;

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("常去的停车场");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new HistoryParkAdapter(getActivity(), mList);
        getData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mHeader = inflater.inflate(R.layout.fragment_history_park, null);
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmptyHolder = new EmptyViewHolder(view);
        mEmptyHolder.setEmptyText("最近没有去过车场", null);
        getListView().addHeaderView(mHeader);
        getListView().setEmptyView(mEmptyHolder.mEmptyPageView);
        getListView().setDivider(new ColorDrawable(0xfff0f0f0));
        getListView().setDividerHeight(DensityUtils.dip2px(getActivity(), 1));
        setListAdapter(mAdapter);
    }

    private void getData() {
        /*
        //查询去停过车的车场
        carinter.do?action=getparks&mobile=13677226466
        返回：
        [{"id":"3251","parkname":"晓霞测试车场001"},{"id":"1263","parkname":"上地华联商厦停车场"}]
         */
        Map<String, String> paramsMap = URLUtils.createParamsMap();
        paramsMap.put("action", "getparks");
        paramsMap.put("mobile", TCBApp.mMobile);
        String url = URLUtils.createUrl(TCBApp.mServerUrl, "carinter.do", paramsMap);
        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "请稍候...", true, true);
        GsonRequest<ArrayList<ParkInfo>> request = new GsonRequest<ArrayList<ParkInfo>>(url, new TypeToken<ArrayList<ParkInfo>>() {
        }, new Response.Listener<ArrayList<ParkInfo>>() {

            @Override
            public void onResponse(ArrayList<ParkInfo> parkInfos) {
                dialog.dismiss();
                if (parkInfos != null && parkInfos.size() > 0) {
                    mAdapter.setData(parkInfos);
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEmptyHolder.setEmptyText("网络不好，点击重试", new View.OnClickListener() {
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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position == 0) {//headerView
            return;
        }

        ParkInfo info = mAdapter.getItem(position - 1);
        String parkName = info.name;
        String parkId = info.id;
        openParkUserFragment(parkName, parkId);
    }

    private void openParkUserFragment(String parkName, String parkId){
        ParkUserFragment fragment = new ParkUserFragment();
        Bundle args = new Bundle();
        args.putString(ParkUserFragment.ARGS_PARK_NAME, parkName);
        args.putString(ParkUserFragment.ARGS_PARK_ID, parkId);
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    class HistoryParkAdapter extends BaseArrayAdapter<ParkInfo> {

        public HistoryParkAdapter(Context context, List<ParkInfo> list) {
            super(context, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView text = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listitem_history_park, parent, false);
            }
            text = (TextView) convertView.findViewById(R.id.text);
            ParkInfo item = getItem(position);
            text.setText(item.name);
            return convertView;
        }
    }

}
