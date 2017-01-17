package com.tq.zld.protocal;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.tq.zld.util.LogUtils;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadRequest extends Request<String> {

    private MultipartEntity mEntity;

    private final Response.Listener<String> mListener;

    private List<File> mFileParts;
    private String mFilePartName;
    private Map<String, String> mParams;

    /**
     * 单个文件
     *
     * @param url
     * @param errorListener
     * @param listener
     * @param filePartName
     * @param file
     * @param params
     */
    public UploadRequest(String url, Response.ErrorListener errorListener,
                         Response.Listener<String> listener, String filePartName, File file,
                         Map<String, String> params) {
        super(Method.POST, url, errorListener);

        mFileParts = new ArrayList<>();
        if (file != null) {
            mFileParts.add(file);
        }
        mFilePartName = filePartName;
        mListener = listener;
        mParams = params;
        buildMultipartEntity();
    }

    /**
     * 多个文件，对应一个key
     *
     * @param url
     * @param errorListener
     * @param listener
     * @param filePartName
     * @param files
     * @param params
     */
    public UploadRequest(String url, Response.ErrorListener errorListener,
                         Response.Listener<String> listener, String filePartName,
                         List<File> files, Map<String, String> params) {
        super(Method.POST, url, errorListener);
        mFilePartName = filePartName;
        mListener = listener;
        mFileParts = files;
        mParams = params;
        buildMultipartEntity();
    }

    private void buildMultipartEntity() {
        mEntity = new MultipartEntity();
        if (mFileParts != null && mFileParts.size() > 0) {
            for (int i = 0; i < mFileParts.size(); i++) {
                mEntity.addPart(mFilePartName + i, new FileBody(mFileParts.get(i)));
            }

            LogUtils.i(getClass(), "file number: --->> " + mFileParts.size() + "size: --->> " + mEntity.getContentLength());
        }

        try {
            if (mParams != null && mParams.size() > 0) {
                for (Map.Entry<String, String> entry : mParams.entrySet()) {
                    mEntity.addPart(
                            entry.getKey(),
                            new StringBody(entry.getValue(), Charset
                                    .forName("UTF-8")));
                }
            }
        } catch (UnsupportedEncodingException e) {
            VolleyLog.e("UnsupportedEncodingException");
        }
    }

    @Override
    public String getBodyContentType() {
        return mEntity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            mEntity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        LogUtils.i(getClass(), "upload response: --->> " + response);
        if (VolleyLog.DEBUG) {
            if (response.headers != null) {
                for (Map.Entry<String, String> entry : response.headers.entrySet()) {
                    VolleyLog.d(entry.getKey() + "=" + entry.getValue());
                }
            }
        }

        String parsed;
        try {
            parsed = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed,
                HttpHeaderParser.parseCacheHeaders(response));
    }


    /*
     * (non-Javadoc)
     *
     * @see com.android.volley.Request#getHeaders()
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();

        if (headers == null || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<>();
        }
        VolleyLog.d(headers.toString());
        return headers;
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}