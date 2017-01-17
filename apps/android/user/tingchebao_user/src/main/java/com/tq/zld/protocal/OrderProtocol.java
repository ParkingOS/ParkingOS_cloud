package com.tq.zld.protocal;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.tq.zld.bean.Order;

public class OrderProtocol extends ProtocolBase<Order> {

	public OrderProtocol(String url, Class<Order> clazz,
						 Listener<Order> listener, ErrorListener errorListener) {
		super(url, clazz, listener, errorListener);
		// TODO Auto-generated constructor stub
	}

}
