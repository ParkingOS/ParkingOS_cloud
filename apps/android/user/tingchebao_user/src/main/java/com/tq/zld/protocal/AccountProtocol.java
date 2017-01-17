package com.tq.zld.protocal;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.tq.zld.bean.AccountInfo;

public class AccountProtocol extends ProtocolBase<AccountInfo> {

	public AccountProtocol(String url, Class<AccountInfo> clazz,
						   Listener<AccountInfo> listener, ErrorListener errorListener) {
		super(url, clazz, listener, errorListener);
	}
}