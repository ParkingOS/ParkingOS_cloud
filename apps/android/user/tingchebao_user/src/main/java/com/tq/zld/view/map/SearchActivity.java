package com.tq.zld.view.map;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.view.BaseActivity;

public class SearchActivity extends BaseActivity implements OnQueryTextListener {

    public static final String ARG_CITY = "city";

    private static final String[] COLUMNS = {BaseColumns._ID, "key",
            "district"};

    private SuggestionsAdapter mSuggestionsAdapter;

    private SearchView mSearchView;

    private SuggestionSearch mSuggestionSearch;
    private MyGetSuggestionResultListener mSuggestionListener;

    private String mCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initToolbar();

        mCity = getIntent().getStringExtra(ARG_CITY);
        // 初始化百度关键字搜索服务
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionListener = new MyGetSuggestionResultListener();
        mSuggestionSearch.setOnGetSuggestionResultListener(mSuggestionListener);

        if (mSuggestionsAdapter == null) {
            MatrixCursor historySearchCursor = updateHistorySearchCurosr();
            mSuggestionsAdapter = new SuggestionsAdapter(getSupportActionBar()
                    .getThemedContext(), historySearchCursor);
        }
        ListView lvContent = (ListView) findViewById(R.id.search_lv);
        lvContent.setAdapter(mSuggestionsAdapter);
        lvContent.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Cursor c = (Cursor) mSuggestionsAdapter.getItem(position);
                String query = c.getString(c.getColumnIndex("key"));// c.getString(c.getColumnIndex("district"))+
                if (position == mSuggestionsAdapter.getCount() - 1
                        && "清空搜索历史".equals(query.trim())) {
                    // 说明是最后一条数据："清除历史记录"
                    clearSearchHistory();
                } else {
                    mSearchView.setQuery(query, true);
                }
            }
        });
    }

    private void initToolbar() {
        Toolbar bar = (Toolbar) findViewById(R.id.widget_toolbar);
        // bar.setTitle("您想去哪儿？");
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        bar.setNavigationOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(true);// 设置搜索图标是否包含在EditText内部
        mSearchView.setIconified(false);// widget不要图标化，以默认方式展开
        mSearchView.setOnQueryTextListener(this);
        // setSearchViewBackground(mSearchView);
        getSupportActionBar().setCustomView(mSearchView);
    }

    /**
     * 清空搜索历史
     */
    private void clearSearchHistory() {
        TCBApp.getAppContext().saveStringSync(R.string.sp_search_history, "");
        MatrixCursor cursor = updateHistorySearchCurosr();
        mSuggestionsAdapter.changeCursor(cursor);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            MatrixCursor historySearchCursor = updateHistorySearchCurosr();
            if (mSuggestionsAdapter == null) {
                mSuggestionsAdapter = new SuggestionsAdapter(
                        getSupportActionBar().getThemedContext(),
                        historySearchCursor);
                mSearchView.setSuggestionsAdapter(mSuggestionsAdapter);
            } else {
                mSuggestionsAdapter.changeCursor(historySearchCursor);
            }
            return true;
        }
        // 创建百度SuggestionSearch实例对象，该对象将在searchView的onCloseListener中被释放
        if (mSuggestionSearch == null) {
            mSuggestionSearch = SuggestionSearch.newInstance();
            if (mSuggestionListener == null) {
                mSuggestionListener = new MyGetSuggestionResultListener();
            }
            mSuggestionSearch
                    .setOnGetSuggestionResultListener(mSuggestionListener);
        }
        if (TextUtils.isEmpty(mCity)) {
            mCity = "全国";
        }
        mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                .keyword(newText).city(this.mCity));
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        // 存储搜索历史记录
        restoreSearchHistory(query);
        mSearchView.clearFocus();
        Intent data = new Intent();
        data.putExtra("keyword", query);
        setResult(RESULT_OK, data);
        finish();
        return true;
    }

    /**
     * 存储搜索历史
     *
     * @param history
     */
    private void restoreSearchHistory(String history) {
        String searchHistory = TCBApp.getAppContext().readString(R.string.sp_search_history, "");
        if (TextUtils.isEmpty(searchHistory)) {
            TCBApp.getAppContext().saveStringSync(R.string.sp_search_history, history + ",");
        } else {
            String[] historyArr = searchHistory.split(",");
            List<String> historyList = Arrays.asList(historyArr);
            ArrayList<String> historyArrayList = new ArrayList<>(historyList);
            if (historyArrayList.contains(history)) {
                // 已经包含该搜索历史，则将此记录移至第一位
                historyArrayList.remove(history);
            }
            historyArrayList.add(history);
            // 存储成功后将list转化为String，存储到sp中
            searchHistory = "";
            for (String historyStr : historyArrayList) {
                searchHistory += historyStr + ",";
            }
            TCBApp.getAppContext().saveStringSync(R.string.sp_search_history, searchHistory);
        }
    }

    /**
     * 添加搜索历史记录到SuggestionsAdapter中
     */
    private MatrixCursor updateHistorySearchCurosr() {
        MatrixCursor mHistorySearchCursor = new MatrixCursor(COLUMNS);
        String history = TCBApp.getAppContext().readString(R.string.sp_search_history, "");
        String[] histories = null;
        if (!TextUtils.isEmpty(history)) {
            histories = history.split(",");
        }
        if (histories != null) {
            int _id = 0;
            for (int i = histories.length; i > 0; i--) {// 将搜索历史倒序取出
                String str = histories[i - 1];
                if (!TextUtils.isEmpty(str)) {
                    mHistorySearchCursor.addRow(new String[]{
                            String.valueOf(_id), str, "(历史记录)"});
                    _id++;
                }
            }
            mHistorySearchCursor.addRow(new String[]{String.valueOf(_id),
                    "\t\t清空搜索历史", ""});
        }
        return mHistorySearchCursor;
    }

    @Override
    protected void onDestroy() {

        // 释放关键字搜索服务
        if (mSuggestionSearch != null) {
            mSuggestionSearch.destroy();
            mSuggestionSearch = null;
        }
        super.onDestroy();
    }

    /**
     * POI搜索推荐词适配器
     *
     * @author Clare
     */
    private class SuggestionsAdapter extends CursorAdapter {

        public SuggestionsAdapter(Context context, Cursor c) {
            super(context, c, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View mSuggestListView = inflater.inflate(R.layout.listitem_search,
                    parent, false);
            return mSuggestListView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView tvKey = (TextView) view
                    .findViewById(R.id.tv_searchlist_key);
            TextView tvDistrict = (TextView) view
                    .findViewById(R.id.tv_searchlist_district);
            ImageView icon = (ImageView) view.findViewById(R.id.iv_searchlist);
            int keyIndex = cursor.getColumnIndex("key");
            int districtIndex = cursor.getColumnIndex("district");
            String key = cursor.getString(keyIndex);
            String district = cursor.getString(districtIndex);
            tvKey.setText(key);
            if ("清空搜索历史".equals(key.trim()) && "".equals(district)) {
                RelativeLayout.LayoutParams layoutParams = (android.widget.RelativeLayout.LayoutParams) tvKey
                        .getLayoutParams();
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                tvKey.setLayoutParams(layoutParams);
                tvKey.setTextColor(Color.rgb(0x8B, 0x88, 0x88));
                tvDistrict.setVisibility(View.GONE);
                icon.setVisibility(View.GONE);
            } else {
                RelativeLayout.LayoutParams layoutParams = (android.widget.RelativeLayout.LayoutParams) tvKey
                        .getLayoutParams();
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
                tvKey.setLayoutParams(layoutParams);
                tvKey.setTextColor(Color.BLACK);
                tvDistrict.setText(district);
                if (district == null || "(历史记录)".equals(district)) {
                    icon.setImageResource(R.drawable.ic_action_search_history);
                } else {
                    icon.setImageResource(R.drawable.ic_action_search);
                }
                icon.setVisibility(View.VISIBLE);
                tvDistrict.setVisibility(View.VISIBLE);
            }
        }
    }

    private class MyGetSuggestionResultListener implements
            OnGetSuggestionResultListener {

        @Override
        public void onGetSuggestionResult(SuggestionResult result) {
            MatrixCursor mSuggestResultCursor = new MatrixCursor(COLUMNS);
            if (result == null || result.getAllSuggestions() == null
                    || result.getAllSuggestions().size() == 0) {
                mSuggestResultCursor.close();
                return;
            }
            for (SuggestionResult.SuggestionInfo info : result
                    .getAllSuggestions()) {
                if (info.key != null) {
                    mSuggestResultCursor.addRow(new String[]{
                            String.valueOf(mSuggestResultCursor.getCount()),
                            info.key, info.city + info.district});
                }
            }
            mSuggestionsAdapter.changeCursor(mSuggestResultCursor);
            mSuggestionsAdapter.notifyDataSetChanged();
            mSuggestResultCursor.close();
        }
    }

    private void setSearchViewBackground(SearchView searchView) {
        try {
            Class<?> argClass = searchView.getClass();
            // 指定某个私有属性
            Field ownField = argClass.getDeclaredField("mSearchPlate"); // 注意mSearchPlate的背景是stateListDrawable(不同状态不同的图片)
            // 所以不能用BitmapDrawable
            ownField.setAccessible(true);
            View mView = (View) ownField.get(searchView);
            mView.setBackgroundResource(R.drawable.shape_green_stroke_r);

            // 指定某个私有属性
            Field mQueryTextView = argClass.getDeclaredField("mQueryTextView");
            mQueryTextView.setAccessible(true);
            Class<?> mTextViewClass = mQueryTextView.get(searchView).getClass()
                    .getSuperclass().getSuperclass().getSuperclass();

            // mCursorDrawableRes光标图片Id的属性
            // 这个属性是TextView的属性，所以要用mQueryTextView（SearchAutoComplete）的父类（AutoCompleteTextView）的父
            // 类( EditText）的父类(TextView)
            Field mCursorDrawableRes = mTextViewClass
                    .getDeclaredField("mCursorDrawableRes");

            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(mQueryTextView.get(searchView),
                    new ColorDrawable(Color.WHITE));// 注意第一个参数持有这个属性(mQueryTextView)的对象(mSearchView)
            // 光标必须是一张图片不能是颜色，因为光标有两张图片，一张是第一次获得焦点的时候的闪烁的图片，一张是后边有内容时候的图片，如果用颜色填充的话，就会失去闪烁的那张图片，颜色填充的会缩短文字和光标的距离（某些字母会背光标覆盖一部分）。
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
