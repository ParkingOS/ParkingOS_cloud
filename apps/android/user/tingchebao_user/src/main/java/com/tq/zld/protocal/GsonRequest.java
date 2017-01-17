package com.tq.zld.protocal;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tq.zld.util.LogUtils;

import java.lang.reflect.Type;

public class GsonRequest<T> extends Request<T> {

    private final Listener<T> mListener;

    private Gson mGson;

    private Class<T> mClass;

    private Type mType;

    public GsonRequest(String url, Class<T> clazz, Listener<T> listener,
                       ErrorListener errorListener) {
        this(Method.GET, url, clazz, listener, errorListener);
    }

    public GsonRequest(int method, String url, Class<T> clazz,
                       Listener<T> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        mGson = new Gson();
        mClass = clazz;
        mListener = listener;
    }

    public GsonRequest(String url, TypeToken<T> type, Listener<T> listener,
                       ErrorListener errorListener) {
        this(Method.GET, url, type, listener, errorListener);
    }

    public GsonRequest(int method, String url, TypeToken<T> type,
                       Listener<T> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        mGson = new Gson();
        mType = type.getType();
        mListener = listener;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            LogUtils.d(String.format("Network response: --->> %s", jsonString));
            if (mClass == null) {
                return (Response<T>) Response.success(
                        mGson.fromJson(jsonString, mType),
                        HttpHeaderParser.parseCacheHeaders(response));
            }
            return Response.success(mGson.fromJson(jsonString, mClass),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            e.printStackTrace();
            ParseError error;
            if (response.data != null) {
                error = new ParseError(response);
            } else {
                error = new ParseError(e);
            }
            return Response.error(error);
        }
    }

    @Override
    protected void deliverResponse(T response) {
        if (mListener == null) {
            return;
        }
        mListener.onResponse(response);
    }
}
