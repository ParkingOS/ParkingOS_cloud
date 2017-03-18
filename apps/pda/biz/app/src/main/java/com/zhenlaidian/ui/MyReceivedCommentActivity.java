package com.zhenlaidian.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.MyCommentAdapter;
import com.zhenlaidian.bean.CommentInfo;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

import java.util.ArrayList;

/**
 * 我收到的评论;
 */
public class MyReceivedCommentActivity extends BaseActivity {

    ListView lv_my_received_comment;
    TextView tv_comment_null;
    MyCommentAdapter adapter;
    public int page = 1;
    public Boolean isend = false;//到最后一条数据了；

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.activity_my_received_comment);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        lv_my_received_comment = (ListView) findViewById(R.id.lv_my_received_comment);
        tv_comment_null = (TextView) findViewById(R.id.tv_my_received_comment_null);
        setView();
        adapter = new MyCommentAdapter(this);
        getComments();
    }

    /**
     * 没有评论数据时候显示；
     */
    public void setNullVew() {
        lv_my_received_comment.setVisibility(View.GONE);
        tv_comment_null.setVisibility(View.VISIBLE);
    }

    public void setView() {
        lv_my_received_comment.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                switch (scrollState) {
                    // 当不滚动时
                    case OnScrollListener.SCROLL_STATE_IDLE:
                        // 判断滚动到底部
                        if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                            if (view.getCount() > 19 && isend == false) {
                                getComments();
                            } else {
                                isend = false;
                            }
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    public void setAdapter() {
        lv_my_received_comment.setAdapter(adapter);
    }

    public void getComments() {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取评论失败，请检查网络！", 0).show();
            return;
        }
        // carinter.do?action=pusrcomments&uid=10700&page=
        String path = baseurl;
        String url = path + "carinter.do?action=pusrcomments&uid=" + useraccount + "&page=" + page + "&size=20";
        MyLog.w("评论查询--URL---->>>", url);
        final ProgressDialog dialog = ProgressDialog.show(this, "查询评论", "数据请求中...", true, true);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                MyLog.i("获取到的评论数据为----", object);
                if (object != null && object != "" && status.getCode() == 200) {
                    lv_my_received_comment.setVisibility(View.VISIBLE);
                    tv_comment_null.setVisibility(View.GONE);
                    dialog.dismiss();
                    Gson gson = new Gson();
                    ArrayList<CommentInfo> cInfos = gson.fromJson(object, new TypeToken<ArrayList<CommentInfo>>() {
                    }.getType());
                    if (cInfos != null && cInfos.size() != 0) {
                        adapter.addInfos(cInfos);
                        page++;
                    } else {
                        adapter.addInfos(cInfos);
                        isend = true;
                        Toast.makeText(MyReceivedCommentActivity.this, "没有更多数据了...", 0).show();
                    }
                } else {
                    dialog.dismiss();
                    switch (status.getCode()) {
                        case -101:
                            Toast.makeText(MyReceivedCommentActivity.this, "网络错误！--查看评论记录失败！", 0).show();
                            break;
                        case 500:
                            Toast.makeText(MyReceivedCommentActivity.this, "服务器错误！--查看评论记录失败！", 0).show();
                            break;
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                MyReceivedCommentActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
