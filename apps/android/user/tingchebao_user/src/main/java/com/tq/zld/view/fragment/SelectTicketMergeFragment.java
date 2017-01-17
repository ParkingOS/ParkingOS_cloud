package com.tq.zld.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.adapter.TicketAdapter;
import com.tq.zld.bean.Coupon;
import com.tq.zld.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by GT on 2015/10/8.
 */
public class SelectTicketMergeFragment extends NetworkFragment<ArrayList<Coupon>> {
    int mPage = 1;
    private ListView mListView;
    private TicketAdapter mAdapter;

    @Override
    protected TypeToken<ArrayList<Coupon>> getBeanListType() {
        return new TypeToken<ArrayList<Coupon>>(){};
    }

    @Override
    protected Class<ArrayList<Coupon>> getBeanClass() {
        return null;
    }

    @Override
    protected String getUrl() {
        return TCBApp.mServerUrl + "carinter.do";
    }

    @Override
    protected Map<String, String> getParams() {
        Map<String,String> params = new HashMap<>();
        params.put("action", "getuiontickets");
        params.put("mobile", TCBApp.mMobile);
        params.put("page", String.valueOf(mPage));
        return params;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_select_ticket_merge,container,false);
    }

    @Override
    protected void initView(View view) {
        mListView = (ListView) view.findViewById(R.id.listview);
        mAdapter = new TicketAdapter();
        mAdapter.setShowLimit(false);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == mAdapter.getSelection()) {
                    return;
                } else {
                    mAdapter.setSelection(position);
                }
            }
        });

        view.findViewById(R.id.btn_select_ticket_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Coupon coupon = mAdapter.getItem(mAdapter.getSelection());
                Intent intent = new Intent();
                intent.putExtra("coupon", coupon);
                getActivity().setResult(Activity.RESULT_OK, intent);
                LogUtils.i("set result >> " + coupon.toString());

                getActivity().finish();
            }
        });
    }

    @Override
    protected String getTitle() {
        return "选择停车券";
    }

    @Override
    protected void onNetWorkResponse(ArrayList<Coupon> coupons) {
        if (coupons != null && coupons.size() > 0) {
            mAdapter.setData(mPage,coupons);
            mAdapter.setSelection(0);
            mPage++;
            showDataView();
        } else {
            showEmptyView("没有可以合并的券\n点击关闭页面", 0, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
        }
    }

    @Override
    protected int getFragmentContainerResID() {
        return R.id.fragment_container;
    }

    @Override
    public void onClick(View v) {

    }
}
