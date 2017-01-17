package com.tq.zld.view.fragment;

import android.app.Activity;
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
import com.tq.zld.adapter.AccountDetailAdapter;
import com.tq.zld.bean.OperatingStatement;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class AccountDetailFragment extends ListFragment {

    public static final String ARG_TYPE = "type";
    public static final int PAGE_SIZE = 15;// 服务器一次返回数据条数

    public static final int TYPE_ALL = 2;
    public static final int TYPE_RECHARGE = 0;
    public static final int TYPE_PAY = 1;

    private int mPage;

    View mEmptyView;
    TextView mEmptyText;

    private Button mFootView;
    private AccountDetailAdapter mAdapter;

    private ProgressDialog mDialog;

    /**
     * 账户交易明细列表
     *
     * @param type 可取值：TYPE_ALL(全部)，TYPE_RECHARGE(充值)，TYPE_PAY(消费)
     * @return
     */
    public static AccountDetailFragment newInstance(int type) {
        final AccountDetailFragment fragment = new AccountDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogUtils.i(getClass(), this.toString() + "：--->> onCreateView");
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        LogUtils.i(getClass(), this.toString() + "：--->> onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        mEmptyView = view.findViewById(R.id.rl_page_null);
        mEmptyText = (TextView) mEmptyView.findViewById(R.id.tv_page_null);
        mEmptyText.setText("暂无记录~");
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
                    getAccountDetail();
                }
            }
        });
        getListView().addFooterView(footView);
        getListView().setEmptyView(mEmptyView);
        setListAdapter(mAdapter);
    }

    protected void getAccountDetail() {

        int type = getArguments().getInt(ARG_TYPE);

        if (mPage != 1 || TYPE_ALL == type) {
            mDialog = ProgressDialog.show(getActivity(), "", "", false, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    TCBApp.getAppContext().cancelPendingRequests(AccountDetailFragment.this);
                }
            });
            mDialog.setCanceledOnTouchOutside(false);
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("action", "accountdetail");
        params.put("mobile", TCBApp.mMobile);
        params.put("type", String.valueOf(type));
        params.put("page", String.valueOf(mPage));
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "carowner.do", params);
        GsonRequest<ArrayList<OperatingStatement>> request = new GsonRequest<>(
                url, new TypeToken<ArrayList<OperatingStatement>>() {
        }, new Listener<ArrayList<OperatingStatement>>() {

            @Override
            public void onResponse(ArrayList<OperatingStatement> details) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                if (mFootView != null) {
                    if (details.size() < PAGE_SIZE) {
                        mFootView.setText(getString(R.string.no_more_data));
                    } else {
                        mFootView.setText(getString(R.string.load_more));
                    }

                    if (mPage == 1 && details.size() == 0) {
                        mEmptyText.setText("暂无记录~");
                    }
                }

                mAdapter.addData(details);
                mPage++;
            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
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
                        getAccountDetail();
                    }
                });
            }
        });
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    /**
     * Do not use this constructor,use newInstance() instead
     */
    public AccountDetailFragment() {
    }

    @Override
    public void onDestroyView() {
        LogUtils.i(getClass(), this.toString() + "：--->> onDestroyView");
        super.onDestroyView();
        TCBApp.getAppContext().cancelPendingRequests(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        LogUtils.i(getClass(), this.toString() + "：--->> onHiddenChanged，" + hidden);
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (mFootView != null && getString(R.string.loading).equals(mFootView.getText())) {
                mFootView.setText(getString(R.string.load_more));
            }
        } else {
            TCBApp.getAppContext().cancelPendingRequests(this);
        }
    }

    @Override
    public void onStart() {
        LogUtils.i(getClass(), this.toString() + "：--->> onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        LogUtils.i(getClass(), this.toString() + "：--->> onResume");
        super.onResume();
    }

    @Override
    public void onStop() {
        LogUtils.i(getClass(), this.toString() + "：--->> onStop");
        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.i(getClass(), this.toString() + "：--->> onCreate");
        mAdapter = new AccountDetailAdapter();
        mPage = 1;
        getAccountDetail();
    }

    @Override
    public void onDetach() {
        LogUtils.i(getClass(), this.toString() + "：--->> onDetach");
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        LogUtils.i(getClass(), this.toString() + "：--->> onDestroy");
        super.onDestroy();
    }

    @Override
    public void onAttach(Activity activity) {
        LogUtils.i(getClass(), this.toString() + "：--->> onAttach");
        super.onAttach(activity);
    }
}
