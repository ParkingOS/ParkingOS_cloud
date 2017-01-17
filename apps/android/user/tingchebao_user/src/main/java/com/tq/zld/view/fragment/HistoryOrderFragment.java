package com.tq.zld.view.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.adapter.HistoryOrderAdapter;
import com.tq.zld.bean.HistoryOrder;
import com.tq.zld.util.LogUtils;

public class HistoryOrderFragment extends
        NetworkFragment<ArrayList<HistoryOrder>> {

    public static final int PAGE_SIZE = 10;

    private HistoryOrderAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    private int mPage = 1;

    private Button mFootView;
    private HashMap<String, String> params;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history_order, container,
                false);
    }

    @Override
    protected String getTitle() {
        return "历史订单";
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
    protected TypeToken<ArrayList<HistoryOrder>> getBeanListType() {
        return new TypeToken<ArrayList<HistoryOrder>>() {
        };
    }

    @Override
    protected Class<ArrayList<HistoryOrder>> getBeanClass() {
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
            params.put("action", "historyroder");
            params.put("size", "10");
            params.put("mobile", TCBApp.mMobile);
        }
        params.put("page", String.valueOf(mPage));
        return params;
    }

    @Override
    protected void initView(View view) {
        mRefreshLayout = (SwipeRefreshLayout) view
                .findViewById(R.id.swipe_container);
        mRefreshLayout.setColorSchemeResources(R.color.primary_green);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                mPage = 1;
                getData();
            }
        });
        ListView lvOrders = (ListView) view.findViewById(R.id.list_history_order);
        View footView = View.inflate(getActivity(), R.layout.listitem_foot,
                null);
        mFootView = (Button) footView.findViewById(R.id.btn_listitem_foot);
        mFootView.setText(getString(R.string.load_more));
        mFootView.setOnClickListener(this);
        lvOrders.addFooterView(footView);
        mAdapter = new HistoryOrderAdapter(getActivity());
        lvOrders.setAdapter(mAdapter);
        lvOrders.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String orderId = ((HistoryOrder) mAdapter.getItem(position))
                        .getOrderid();
                OrderDetailFragment fragment = new OrderDetailFragment();
                Bundle args = new Bundle();
                args.putString(OrderDetailFragment.ARG_ORDER_ID, orderId);
                fragment.setArguments(args);
                replace(R.id.fragment_container, fragment, true);
                LogUtils.i(HistoryOrderFragment.class,
                        "clicked OrderItem position: --->> " + position
                                + "\norderId: --->> " + orderId);
            }
        });
    }

    @Override
    public void onNetWorkResponse(ArrayList<HistoryOrder> orders) {

        mRefreshLayout.setRefreshing(false);

        if (mPage == 1) {
            if (orders == null || orders.size() == 0) {
                showEmptyView("暂无历史订单", 0, null);
            } else {
                showDataView();
                updateListView(orders);
            }
        } else {
            updateListView(orders);
        }
    }

    private void updateListView(ArrayList<HistoryOrder> orders) {
        mAdapter.setData(mPage, orders);
        mPage++;
        if (orders.size() < PAGE_SIZE) {
            mFootView.setText(getString(R.string.no_more_data));
        } else {
            mFootView.setText(getString(R.string.load_more));
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    protected int getFragmentContainerResID() {
        return R.id.fragment_container;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && mFootView != null && getString(R.string.loading).equals(mFootView.getText())) {
            mFootView.setText(getString(R.string.load_more));
        }
    }
}
