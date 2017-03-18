/**
 *
 */
package com.zhenlaidian.ui.person_account;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.OwnersPayLogAdapter;
import com.zhenlaidian.bean.OwnerPayLogsInfo;
import com.zhenlaidian.photo.InPutCarNumberDialog;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.util.CheckUtils;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author zhangyunfei 2015年9月19日 车主支付记录
 */
public class CarOwnersPayLogActivity extends BaseActivity {

    private TextView tv_carnumber;
    private LinearLayout ll_delete;
    private LinearLayout ll_back;
    private TextView tv_pay_log_null;// 没有查询结果
    public TextView tv_pay_log_carnumbers;// 多车牌
    public ListView lv_pay_log;// 车主支付记录列表
    public OwnersPayLogAdapter adapter;
    private int page = 1;
    private int size = 20;
    private int server_count = 0;// 服务器端总数；
    private int visiblecount = 0; // 当前总条目数；

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_carowner_pay_log);
        initView();
        setView();
        adapter = new OwnersPayLogAdapter(this);
    }

    /**
     * 初始化控件；
     */
    private void initView() {
        tv_pay_log_carnumbers = (TextView) findViewById(R.id.tv_pay_log_carnumbers);
        lv_pay_log = (ListView) findViewById(R.id.lv_pay_log);
        tv_carnumber = (TextView) findViewById(R.id.tv_pay_log_number);
        ll_delete = (LinearLayout) findViewById(R.id.ll_pay_log_delete);
        ll_back = (LinearLayout) findViewById(R.id.ll_pay_log_back);
        tv_pay_log_null = (TextView) findViewById(R.id.tv_pay_log_null);
    }

    // 车牌输入法关闭时的回调；
    OnCancelListener mListener = new OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
            // TODO Auto-generated method stub
            InPutCarNumberDialog d = (InPutCarNumberDialog) dialog;
            tv_carnumber.setText(d.getcarnumber());
            if (!CheckUtils.CarChecked(d.getcarnumber())) {
                Toast.makeText(CarOwnersPayLogActivity.this, "请输入正确的车牌", 0).show();
            } else {
                page = 1;
                getAllPaymentInfo(d.getcarnumber(), true);
            }
        }
    };

    private void setView() {
        tv_carnumber.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 点击调用车牌输入法；
                new InPutCarNumberDialog(CarOwnersPayLogActivity.this, true, tv_carnumber.getText().toString(), mListener).show();
            }
        });
        ll_delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 一键删除车牌号
                tv_carnumber.setText("");
            }
        });
        ll_back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 返回主界面；
                CarOwnersPayLogActivity.this.finish();
            }
        });
        lv_pay_log.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                switch (scrollState) {
                    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    case OnScrollListener.SCROLL_STATE_FLING:
                        break;
                    case OnScrollListener.SCROLL_STATE_IDLE:
                        // 判断滚动到底部
                        if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                            if (visiblecount != server_count) {
                                if (adapter.getInfo(visiblecount) == null) {
                                    getAllPaymentInfo(tv_carnumber.getText().toString(), false);
                                }
                            }
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                visiblecount = totalItemCount;
            }
        });
    }

    public void setAdapter() {
        lv_pay_log.setAdapter(adapter);
    }

    public void setPageNumber() {
        page++;
    }

    // 设置查询后的结果显示到界面；
    public void setQueryVeiw(OwnerPayLogsInfo info, String carnumber) {
        if (info != null && TextUtils.isEmpty(info.getCarinfo())) {
            tv_pay_log_carnumbers.setVisibility(View.GONE);
        } else {
            tv_pay_log_carnumbers.setVisibility(View.VISIBLE);
            tv_pay_log_carnumbers.setText(info.getCarinfo().replace("/n", "\n"));
        }
        if (info != null && !TextUtils.isEmpty(info.getCount())) {
            server_count = Integer.parseInt(info.getCount());
        }
        if (info != null && info.getInfo() != null && info.getInfo().size() > 0) {
            if (!TextUtils.isEmpty(info.getCount())) {
                lv_pay_log.setVisibility(View.VISIBLE);
                tv_pay_log_null.setVisibility(View.GONE);
                adapter.addInfo(info.getInfo(), this, Integer.parseInt(info.getCount()), carnumber);
            } else {
                if (adapter.getCount() <= 0) {
                    lv_pay_log.setVisibility(View.GONE);
                    tv_pay_log_null.setVisibility(View.VISIBLE);
                }
            }
        } else {
            setNullVeiw();
        }

    }

    // 按车牌车搜支付记录没有结果设置界面；
    private void setNullVeiw() {
        tv_pay_log_carnumbers.setVisibility(View.GONE);
        lv_pay_log.setVisibility(View.GONE);
        tv_pay_log_null.setVisibility(View.VISIBLE);
    }

    /**
     * 查询车主消费记录 collectorrequest.do?action=queryaccount&token=&carnumber=
     */
    public void getAllPaymentInfo(final String carnumber, Boolean isClean) {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "车主消费记录获取失败，请检查网络！", 0).show();
            return;
        }
        if (isClean) {
            adapter.cleanInfo();// 清除以前的查询结果；
        }
        String mcarnumber = carnumber;
        try {
            mcarnumber = URLEncoder.encode(URLEncoder.encode(carnumber, "utf-8"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String path = baseurl;
        String url = path + "collectorrequest.do?action=queryaccount&token=" + token + "&carnumber=" + mcarnumber + "&page="
                + page + "&size=" + size;
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "查询车主消费记录...", true, true);
        MyLog.w("CarOwnersPayLogActivity", "车主消费记录的URl--->" + url);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                super.callback(url, object, status);
                if (!TextUtils.isEmpty(object)) {
                    MyLog.i("CarOwnersPayLogActivity", "请求个人账户收支明细返回的结果--->" + object);
                    dialog.dismiss();
                    if ("-1".equals(object)) {
                        setNullVeiw();
                        return;
                    }
                    Gson gson = new Gson();
                    OwnerPayLogsInfo info = gson.fromJson(object, OwnerPayLogsInfo.class);
                    MyLog.d("CarOwnersPayLogActivity", "解析个人账户收支明细的结果--->" + info.toString());
                    setQueryVeiw(info, carnumber);
                } else {
                    dialog.dismiss();
                }
            }
        });
    }
}
