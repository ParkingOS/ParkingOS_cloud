package com.zhenlaidian.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.List;

/**
 * Created by xulu on 2016/8/18.
 */
public class ImgAdapter extends FragmentStatePagerAdapter {
    public static List<Fragment> listFra;
    Context context;

    public ImgAdapter(FragmentManager fm, List<Fragment> listFra, Context context) {
        super(fm);
//        mFragmentManager = fm;
        this.listFra = listFra;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {

        return listFra.get(position);
    }

//    @Override
//    public long getItemId(int position) {
//        return Long.parseLong((Math.floor(Math.random()*10000)+"").replace(".0",""));
//    }

//    @Override
//    public long getItemId(int position) {
//        return super.getItemId(position);
//    }

    @Override
    public int getCount() {
        if (listFra != null && listFra.size() > 0)
            return listFra.size();
        else
            return 0;
    }
    @Override
    public int getItemPosition(Object object) {
        // refresh all fragments when data set changed
        return PagerAdapter.POSITION_NONE;
    }
}
