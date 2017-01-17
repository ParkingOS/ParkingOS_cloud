package com.tq.zld.view.park;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.tq.zld.R;
import com.tq.zld.bean.ParkRecord;
import com.tq.zld.view.BaseActivity;

import java.util.List;

/**
 * 车场停车记录
 * Created by GT on 2015/9/2.
 */
public class ParkingRecordsActivity extends BaseActivity{
    public final static String ARG_PARK_ID = "arg_park_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_record);
        
        initToolbar();
        initView();
    }

    private void initToolbar() {

    }

    private ListView mListView;
    private ParkingRecordsAdapter adapter;
    private void initView() {
        mListView = (ListView) findViewById(R.id.listview);
        adapter = new ParkingRecordsAdapter(ParkingRecordsActivity.this);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //其他 车主详情
            }
        });
    }

    class ParkingRecordsAdapter extends BaseAdapter{
        LayoutInflater inflater;
        List<ParkRecord> records;

        public ParkingRecordsAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void setData(List<ParkRecord> list) {
            records = list;
        }

        @Override
        public int getCount() {
            return records == null ? 0 : records.size();
        }

        @Override
        public ParkRecord getItem(int position) {
            if (records != null && records.size() > position) {
                return records.get(position);
            }

            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.listitem_park_record, null);
                holder.plate = (TextView) convertView.findViewById(R.id.tv_park_record_plate);
                holder.time = (TextView) convertView.findViewById(R.id.tv_park_record_time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            return convertView;
        }
    }

    static class ViewHolder {
        TextView plate;
        TextView time;
    }


}
