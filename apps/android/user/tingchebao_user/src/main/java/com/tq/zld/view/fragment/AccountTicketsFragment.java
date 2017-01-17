package com.tq.zld.view.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.adapter.TicketAdapter;
import com.tq.zld.bean.Coupon;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.account.AccountTicketsActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class AccountTicketsFragment extends ListFragment {

    public static final String ARG_TYPE = "type";
    public static final int PAGE_SIZE = 20;// 服务器一次返回数据条数

    private int mPage;

    View mEmptyView;
    TextView mEmptyText;

    private Button mFootView;
    private TicketAdapter mAdapter;

    private ProgressDialog mDialog;

    /**
     * 账户交易明细列表
     *
     * @param type 可取值：TYPE_CURRENT(当前)，TYPE_HISTORY(历史)
     * @return
     */
    public static AccountTicketsFragment newInstance(int type) {
        final AccountTicketsFragment fragment = new AccountTicketsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, String.valueOf(type));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new TicketAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPage = 1;
        getTickets();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmptyView = view.findViewById(R.id.rl_page_null);
        // mEmptyView = getActivity().getLayoutInflater().inflate(
        // R.layout.page_null, (ViewGroup) view, false);
        mEmptyText = (TextView) mEmptyView.findViewById(R.id.tv_page_null);
        mEmptyText.setText("您还没有停车券。\n点击右上角“ ? ”查看如何获得。");
        getListView().setEmptyView(mEmptyView);
        View footView = getActivity().getLayoutInflater().inflate(
                R.layout.listitem_foot, null);
        mFootView = (Button) footView.findViewById(R.id.btn_listitem_foot);
        if (mAdapter.getCount() % PAGE_SIZE == 0) {
            mFootView.setText(getString(R.string.load_more));
        } else {
            mFootView.setText(getString(R.string.no_more_data));
        }
        mFootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getString(R.string.load_more).equals(mFootView.getText())) {
                    mFootView.setText(getString(R.string.loading));
                    getTickets();
                }
            }
        });
        getListView().addFooterView(footView);
        setListAdapter(mAdapter);
    }

    protected void getTickets() {

        String type = getArguments().getString(ARG_TYPE);

        if (mPage != 1 || "0".equals(type)) {
            mDialog = ProgressDialog.show(getActivity(), "", "", false, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    TCBApp.getAppContext().cancelPendingRequests(AccountTicketsFragment.this);
                }
            });
            mDialog.setCanceledOnTouchOutside(false);
        }

        final HashMap<String, String> params = new HashMap<>();
        params.put("action", "gettickets");
        params.put("mobile", TCBApp.mMobile);
        params.put("type", type);
        params.put("page", String.valueOf(mPage));
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "carinter.do", params);
        GsonRequest<ArrayList<Coupon>> request = new GsonRequest<>(url,
                new TypeToken<ArrayList<Coupon>>() {
                }, new Listener<ArrayList<Coupon>>() {
            @Override
            public void onResponse(ArrayList<Coupon> coupons) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                if ("0".equals(params.get("type")) && mPage == 1 && coupons.size() > 0) {
                    // 没有可用停车券
                    ((AccountTicketsActivity) getActivity()).setPlayButtonVisibility(View.VISIBLE);
                }
                if (mFootView != null) {
                    if (coupons.size() < PAGE_SIZE) {
                        mFootView.setText(getString(R.string.no_more_data));
                    } else {
                        mFootView.setText(getString(R.string.load_more));
                    }
                    if (mPage == 1 && coupons.size() == 0) {
                        mEmptyText.setText("您还没有停车劵。\n点击右上角“ ? ”查看如何获得。");
                    }
                }
                mAdapter.setData(mPage,coupons);
                mPage++;

            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                mEmptyText.setText("网络错误，点击重试~");
                mEmptyView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String loading = getString(R.string.loading);
                        if (loading.equals(mEmptyText.getText().toString())) {
                            return;
                        }
                        mEmptyText.setText(loading);
                        getTickets();
                    }
                });
            }
        });
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    /**
     * Use newInstance() instead
     */
    public AccountTicketsFragment() {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TCBApp.getAppContext().cancelPendingRequests(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (mFootView != null && getString(R.string.loading).equals(mFootView.getText())) {
                mFootView.setText(getString(R.string.load_more));
            }
        } else {
            TCBApp.getAppContext().cancelPendingRequests(this);
        }
    }
}
