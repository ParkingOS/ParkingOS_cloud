package com.zhenlaidian.ui.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.CorrOrderFragmentListAdapter;
import com.zhenlaidian.bean.AllOrder;
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.bean.HistoryOrder;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

/**
 * 当前订单fragment;
 * Created by zhangyunfei on 15/10/27.
 */
public class CurrnetOrderListFragment extends Fragment {

    ListView lv_current_order;
    TextView tv_currnet_null;
    TextView tv_currnet_park_status;
    private int pagenumber = 1;
    private final int size = 10;
    private int count = 0;
    private int visiblecount = 0;
    private CorrOrderFragmentListAdapter adapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_currorder_list, null);
        lv_current_order = (ListView) view.findViewById(R.id.lv_current_order_list);
        tv_currnet_null = (TextView) view.findViewById(R.id.tv_current_order_null_list);
        tv_currnet_park_status = (TextView) view.findViewById(R.id.tv_current_order_park_status);
        adapter = new CorrOrderFragmentListAdapter(getActivity());
        setView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getOrder();// 从网络上获取数据。
    }

    //刷新订单数据
    public void refresh(){
        adapter.removeOrders();
        count = 0;
        pagenumber = 1;
        getOrder();// 从网络上获取数据。
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.removeOrders();
        count = 0;
        pagenumber = 1;
    }

    public void setView(){
        lv_current_order.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.onItemClick(position, CurrnetOrderListFragment.this);
            }
        });
        lv_current_order.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    // 当不滚动时
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        // 判断滚动到底部
                        if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                            if (count != visiblecount && view.getLastVisiblePosition() >0) {
                                AllOrder allOrders1 = adapter.getAllOrder(view.getLastVisiblePosition());
                                if (allOrders1 == null){
                                    adapter.onItemClick(view.getLastVisiblePosition(), CurrnetOrderListFragment.this);
                                }
                            } else {
                                if (count > 0) {
                                    AllOrder allOrders = adapter.getAllOrders(count);
                                    if (allOrders == null) {
                                        adapter.onItemClick(view.getLastVisiblePosition(), CurrnetOrderListFragment.this);
                                    }
                                }
                            }
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                visiblecount = totalItemCount;
            }
        });
    }

    public void setNullView() {
        tv_currnet_null.setVisibility(View.VISIBLE);
        lv_current_order.setVisibility(View.INVISIBLE);
    }

    // 从网络上获取当前订单；
    public void getOrder() {
        if (!IsNetWork.IsHaveInternet(getActivity())) {
            Toast.makeText(getActivity(), "获取订单失败，请检查网络！", 0).show();
            tv_currnet_null.setVisibility(View.VISIBLE);
            tv_currnet_null.setText("获取订单失败，请检查网络！");
            lv_current_order.setVisibility(View.INVISIBLE);
            return;
        }
        AQuery aQuery = new AQuery(getActivity());
        //http://s.zhenlaidian.com/zld/collectorrequest.do?action=currorders&token=*&page=*&size=*
        String path = Config.getUrl(getActivity());
        String url = path + "collectorrequest.do?action=currorders&token=" + BaseActivity.token
                + "&page=" + pagenumber + "&size=" + size + "&out=" + "json";
        MyLog.w("获取当前订单URL---->>", url);
        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "加载中...", "获取当前订单数据...", true, true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @SuppressLint("DefaultLocale")
            public void callback(String url, String object, AjaxStatus status) {
                dialog.dismiss();
                if (!TextUtils.isEmpty(object)) {
                    MyLog.d("获取到当前订单为", object);
                    Gson gson = new Gson();
                    HistoryOrder orders = gson.fromJson(object, HistoryOrder.class);
                    System.out.println("获取到当前订单的集合的长度" + orders.getInfo().size());
                    MyLog.i("解析到当前订单为", "-->>" + orders.toString());
                    int total = 0;
                    if (!TextUtils.isEmpty(orders.getCount()) && TextUtils.isDigitsOnly(orders.getCount())) {
                        count = Integer.parseInt(orders.getCount());
                        total = Integer.parseInt(orders.getCount());
                        String parktotal = SharedPreferencesUtils.getIntance(getActivity()).getParkTotal();
                        tv_currnet_park_status.setText(total+"/"+parktotal);
                    }
                    if (orders == null || orders.getInfo().size() == 0) {
                        return;
                    }
                    adapter.addOrders(orders.getInfo(), total,CurrnetOrderListFragment.this);
                } else {
                    MyLog.d("获取到当前订单为空!!!",status.getError()+status.hashCode());
                }
            }

        });

    }

    public void setAdapter() {
        lv_current_order.setAdapter(adapter);
    }

    public void setPageNumber() {
        pagenumber++;
    }

}
