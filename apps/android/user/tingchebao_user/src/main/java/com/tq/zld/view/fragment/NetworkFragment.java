package com.tq.zld.view.fragment;

import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;

/**
 * 不好，待重写
 *
 * @param <T>
 */
public abstract class NetworkFragment<T> extends BaseFragment implements
        Listener<T>, ErrorListener, OnClickListener {

    private EmptyPageFragment mFragment;

    public EmptyPageFragment getEmptyFragment() {
        return mFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showProgressView();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        getData();
    }

    protected void getData() {
        String url = URLUtils.genUrl(getUrl(), getParams());
        if (getBeanClass() == null) {
            TCBApp.getAppContext().addToRequestQueue(
                    getGsonRequest(url, getBeanListType()), this);
        } else {
            TCBApp.getAppContext().addToRequestQueue(
                    getGsonRequest(url, getBeanClass()), this);
        }
    }

    /**
     * 当网络返回结果是一个集合时，通过此方法返回集合的泛型
     *
     * @return
     */
    protected abstract TypeToken<T> getBeanListType();

    /**
     * 获取用于封装网络访问结果的Bean，如果此方法返回null,则尝试使用getBeanListType()方法获取Gson解析实体封装类
     *
     * @return
     */
    protected abstract Class<T> getBeanClass();

    /**
     * 子类重写用于设置网络访问url
     *
     * @return
     */
    protected abstract String getUrl();

    /**
     * 子类重写用于添加网络请求参数
     *
     * @return
     */
    protected abstract Map<String, String> getParams();

    /**
     * 子类重写用于findViewById()
     *
     * @param view
     */
    protected abstract void initView(View view);

    private GsonRequest<T> getGsonRequest(String url, Class<T> clazz) {
        return new GsonRequest<>(url, clazz, this, this);
    }

    private GsonRequest<T> getGsonRequest(String url, TypeToken<T> type) {
        return new GsonRequest<>(url, type, this, this);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        String text = "";
        OnClickListener listener = null;
        if (error instanceof NoConnectionError) {
            // 客户端无网络连接
            text = getString(R.string.err_msg_no_connection);
            listener = new OnClickListener() {

                @Override
                public void onClick(View v) {
                    getData();
                }
            };
        } else if (error instanceof NetworkError) {
            // Socket关闭，服务器宕机，找不到DNS等网络错误
            text = getString(R.string.err_msg_network_error);
            listener = new OnClickListener() {

                @Override
                public void onClick(View v) {
                    getData();
                }
            };
        } else if (error instanceof ParseError) {
            // 数据格式错误
            text = getString(R.string.err_msg_parse_error);
        } else if (error instanceof ServerError) {
            // 服务器错误，一般返回4xx或5xx等HTTP状态码
            text = getString(R.string.err_msg_server_error)
                    + error.networkResponse.statusCode;
        } else if (error instanceof TimeoutError) {
            // 网络超时
            text = getString(R.string.err_msg_timeout);
            listener = new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (getString(R.string.loading).equals(getEmptyFragment().getEmptyText())) {
                        return;
                    }
                    getEmptyFragment()
                            .setEmptyText(getString(R.string.loading));
                    getData();
                }
            };
        } else if (error instanceof AuthFailureError) {
            // HTTP身份验证错误
            text = getString(R.string.err_msg_auth_failure);
        } else {
            // 未知错误
            text = "未知错误";
        }
        showEmptyView(text, 0, listener);
        LogUtils.i(getClass(), "Network err: --->> " + text);
    }

    /**
     * 显示空数据布局
     *
     * @param tips       提示信息
     * @param imageResID 图片资源ID，采用默认传0
     * @param listener   界面点击事件，可用于重新发起网络请求
     */
    protected void showEmptyView(String tips, int imageResID,
                                 View.OnClickListener listener) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(this);
        getEmptyFragment().showEmptyView(tips, 0, listener);
        ft.show(getEmptyFragment());
        ft.commitAllowingStateLoss();
    }

    /**
     * 显示进度框
     */
    protected void showProgressView() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (mFragment == null) {
            mFragment = new EmptyPageFragment();
            ft.add(getFragmentContainerResID(), mFragment, "EmptyFragment");
        } else {
            mFragment.showProgressView();
        }
        ft.hide(this);
        ft.commit();
    }

    /**
     * 显示当前布局
     */
    protected void showDataView() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(getEmptyFragment());
        ft.show(this);
        ft.commitAllowingStateLoss();
    }

    @Override
    protected abstract String getTitle();

    /**
     * 获取到网络结果回调
     */
    public void onResponse(T response) {

        // TODO 如果界面已关闭，则不响应网络结果回调
        if (!isVisible() && !mFragment.isVisible()) {
            return;
        }

        onNetWorkResponse(response);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TCBApp.getAppContext().cancelPendingRequests(this);
    }

    /**
     * 子类实现网络结果回调
     */
    protected abstract void onNetWorkResponse(T response);

    /**
     * 子类重写以获取当前Fragment存放的ViewGroup的资源ID
     * 待处理！！！
     *
     * @return
     */
    protected abstract int getFragmentContainerResID();
}
