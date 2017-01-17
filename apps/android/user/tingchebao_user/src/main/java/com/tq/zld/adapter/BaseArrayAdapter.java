package com.tq.zld.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Gecko on 2015/10/30.
 */
public abstract class BaseArrayAdapter<T> extends BaseAdapter{
    protected Context mContext;
    protected List<T> mList;
    protected LayoutInflater mInflater;

    public BaseArrayAdapter(Context context, List<T> list){
        this.mContext = context;
        this.mList = list;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public T getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addData(List<T> list) {
        if (mList != null && list != null && list.size() > 0) {
            mList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void setData(List<T> list) {
        if (list != null && list.size() > 0) {
            mList = list;
            notifyDataSetChanged();
        }
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);
}
