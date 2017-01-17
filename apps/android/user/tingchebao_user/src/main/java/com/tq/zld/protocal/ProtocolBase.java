package com.tq.zld.protocal;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

public abstract class ProtocolBase<T> {

	protected GsonRequest<T> mRequest;

	public ProtocolBase(String url, Class<T> clazz, Listener<T> listener,
						ErrorListener errorListener) {
		mRequest = new GsonRequest<T>(url, clazz, listener, errorListener);
	}

}
