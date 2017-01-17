package com.tq.zld.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.rey.material.widget.ListView;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.ParkingFeeCollector;
import com.tq.zld.util.DateUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.map.CaptureActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ChooseParkingFeeCollectorFragment extends
        NetworkFragment<ArrayList<ParkingFeeCollector>> {

    /**
     * 分页条数
     */
    private static final int PAGE_SIZE = 10;

    public static final String ARG_PARK_ID = "parkid";
    public static final String ARG_PARK_NAME = "parkname";

    private PayFeeListAdapter mAdapter;

    private String mId;

    private Button mFootView;

    private Map<String, String> mParams;

    private int mPage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mId = getArguments().getString(ARG_PARK_ID);
        mPage = 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chooseparkingfeecollector,
                container, false);
    }

    private void goToPay(ParkingFeeCollector collector) {
        InputMoneyFragment fragment = new InputMoneyFragment();
        Bundle args = new Bundle();
        args.putParcelable(InputMoneyFragment.ARG_COLLECTOR, collector);
        fragment.setArguments(args);
        replace(R.id.fragment_container, fragment, true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_payfee_scan:
                startActivity(new Intent(TCBApp.getAppContext(),
                        CaptureActivity.class));
                break;
        }
        if (v == mFootView) {
            if (getString(R.string.load_more).equals(
                    mFootView.getText().toString())) {
                mFootView.setText(getString(R.string.loading));
                getData();
            }
        }
    }

    @Override
    protected String getTitle() {
        return "选择收费员";
    }

    @Override
    protected TypeToken<ArrayList<ParkingFeeCollector>> getBeanListType() {
        return new TypeToken<ArrayList<ParkingFeeCollector>>() {
        };
    }

    @Override
    protected Class<ArrayList<ParkingFeeCollector>> getBeanClass() {
        return null;
    }

    @Override
    protected String getUrl() {
        String last = "-1".equals(mId) ? "carinter.do" : "carowner.do";
        return TCBApp.mServerUrl + last;
    }

    @Override
    protected Map<String, String> getParams() {
        if (mParams == null) {
            mParams = new HashMap<>();
            mParams.put("mobile", TCBApp.mMobile);
        }
        if ("-1".equals(mId)) {
            // TODO 获取最近收费员
            mParams.put("action", "quickpay");
            mParams.put("page", String.valueOf(mPage));
        } else {
            mParams.put("action", "getparkusers");
            mParams.put("comid", mId);
        }
        return mParams;
    }

    @Override
    protected void initView(View view) {
        View scanButton = view.findViewById(R.id.btn_payfee_scan);
        scanButton.setOnClickListener(this);
        ListView listView = (ListView) view.findViewById(R.id.lv_payfee);

        if ("-1".equals(mId)) {
            // 最近支付过的收费员界面显示hint
            view.findViewById(R.id.tv_payfee_hint).setVisibility(View.VISIBLE);

            // TODO listview可以分页加载
//            View footView = View.inflate(getActivity(), R.layout.listitem_foot,
//                    null);
//            mFootView = (Button) footView.findViewById(R.id.btn_listitem_foot);
//            mFootView.setText(getString(R.string.load_more));
//            listView.addFooterView(footView);
        }

        if (mAdapter == null) {
            mAdapter = new PayFeeListAdapter();
        }
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ParkingFeeCollector collector = (ParkingFeeCollector) mAdapter
                        .getItem(position);
                LogUtils.i(getClass(), "clicked item position: -->> "
                        + position);
                goToPay(collector);
            }
        });

    }

    public void onNetWorkResponse(ArrayList<ParkingFeeCollector> collectors) {

        if (collectors != null && collectors.size() > 0) {
            showDataView();
            for (ParkingFeeCollector c : collectors) {
                // 最近收费员列表无需设置车场名（否则设置为参数传递过来的车场名）
                if (TextUtils.isEmpty(c.parkname)) {
                    c.parkname = getArguments().getString(ARG_PARK_NAME);
                }
                if (TextUtils.isEmpty(c.name)) {
                    c.name = "收费员";
                }
            }
            mAdapter.setCollectors(mPage, collectors);
            // TODO 如果是最近收费员列表，则分页加载
//            if ("quickpay".equals(mParams.get("action"))) {
//                mPage++;
//            }
//            if (collectors.size() < PAGE_SIZE && mFootView != null) {
//                mFootView.setText(getString(R.string.no_more_data));
//            } else {
//                mFootView.setText(getString(R.string.load_more));
//            }
        } else {
            String emptyText = "-1".equals(mId) ? "暂无最近支付纪录～" : "该车场暂无收费员在线～";
            showEmptyView(emptyText, 0, null);
        }
    }

    @Override
    protected int getFragmentContainerResID() {
        return R.id.fragment_container;
    }

    class PayFeeListAdapter extends BaseAdapter {

        private ArrayList<ParkingFeeCollector> collectors;

        public void setCollectors(int page, ArrayList<ParkingFeeCollector> collectors) {
            if (page == 1 || collectors == null) {
                this.collectors = collectors;
            } else {
                this.collectors.addAll(collectors);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return collectors == null ? 0 : collectors.size();
        }

        @Override
        public Object getItem(int position) {
            return collectors == null ? null : collectors.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mHolder;
            ParkingFeeCollector collector = (ParkingFeeCollector) getItem(position);
            if (convertView == null) {
                convertView = View.inflate(getActivity(),
                        R.layout.listitem_payfee, null);
                mHolder = new ViewHolder();
                mHolder.tvName = (TextView) convertView
                        .findViewById(R.id.tv_listitem_payfee_name);
                mHolder.tvParkName = (TextView) convertView
                        .findViewById(R.id.tv_listitem_payfee_park);
                mHolder.ivPhoto = (ImageView) convertView
                        .findViewById(R.id.iv_listitem_payfee_photo);
                mHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_listitem_payfee_time);
                mHolder.calendar = Calendar.getInstance();
                convertView.setTag(mHolder);
            }
            mHolder = (ViewHolder) convertView.getTag();
            Spanned name;
            if (!TextUtils.isEmpty(collector.name)) {
                name = Html.fromHtml("<big><font color='#000000'>"
                        + collector.name
                        + " </font></big><font color='#8B8888'>(编号:"
                        + collector.id + ")</font>");
            } else {
                name = Html.fromHtml("</font><font color='#8B8888'>编号:"
                        + collector.id + "</font>");
            }
            mHolder.tvName.setText(name);
            String parkName = collector.parkname;
            if ("1".equals(collector.payed)) {
                parkName += "(您支付过)";
            }
            mHolder.tvParkName.setText(parkName);

            if ("23".equals(collector.online)) {
//                ImageLoader
//                        .getInstance()
//                        .displayImage(
//                                "drawable://"
//                                        + R.drawable.img_parkingfee_collector_online_list,
//                                mHolder.ivPhoto);
                mHolder.ivPhoto.setImageResource(R.drawable.img_parkingfee_collector_online_list);

            } else {
//                ImageLoader
//                        .getInstance()
//                        .displayImage(
//                                "drawable://"
//                                        + R.drawable.img_parkingfee_collector_offline_list,
//                                mHolder.ivPhoto);
                mHolder.ivPhoto.setImageResource(R.drawable.img_parkingfee_collector_offline_list);
            }

            // 设置时间
            if (collector.paytime != 0) {
                mHolder.calendar.setTimeInMillis(collector.paytime * 1000);
                mHolder.tvTime.setText(DateUtils.formatTime(mHolder.calendar));
            } else {
                mHolder.tvTime.setText("");
            }
            return convertView;
        }
    }

    static class ViewHolder {
        TextView tvName;
        TextView tvParkName;
        ImageView ivPhoto;
        TextView tvTime;
        Calendar calendar;
    }
}
