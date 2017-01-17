package com.tq.zld.protocal;

import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.tq.zld.R;
import com.tq.zld.TCBApp;

public class SimpleVolleyErrorListener implements ErrorListener {

	@Override
	public void onErrorResponse(VolleyError error) {
		String text = "";
		if (error instanceof NoConnectionError) {
			// 客户端无网络连接
			text = "无网络连接！";
		} else if (error instanceof NetworkError) {
			// Socket关闭，服务器宕机，找不到DNS等网络错误
			text = "网络异常！";
		} else if (error instanceof ParseError) {
			// 数据格式错误
			if (error.networkResponse != null) {
				try {
					String errmsg = new String(
							error.networkResponse.data,
							HttpHeaderParser
									.parseCharset(error.networkResponse.headers));
					Toast.makeText(TCBApp.getAppContext(), errmsg,
							Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(TCBApp.getAppContext(), "数据错误！",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				text = TCBApp.getAppContext().getString(
						R.string.err_msg_parse_error);
			}
		} else if (error instanceof ServerError) {
			// 服务器错误，一般返回4xx或5xx等HTTP状态码
			text = TCBApp.getAppContext().getString(
					R.string.err_msg_server_error)
					+ error.networkResponse.statusCode;
		} else if (error instanceof TimeoutError) {
			// 网络超时
			text = "网络超时！";
		} else if (error instanceof AuthFailureError) {
			// HTTP身份验证错误
			text = TCBApp.getAppContext().getString(
					R.string.err_msg_auth_failure);
		} else {
			// 未知错误
			text = "未知错误";
		}
		Toast.makeText(TCBApp.getAppContext(), text, Toast.LENGTH_SHORT).show();
	}
}
