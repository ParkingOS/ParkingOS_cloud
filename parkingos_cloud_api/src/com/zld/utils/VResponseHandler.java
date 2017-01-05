package com.zld.utils;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

public class VResponseHandler implements ResponseHandler<String>{
	private String mark = null;
	public VResponseHandler(String str){
		mark = str;
	}
	
	@Override
	public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		 int status = response.getStatusLine().getStatusCode();
         if (status >= 200 && status < 300){
             HttpEntity entity = response.getEntity();
             return entity !=null ? EntityUtils.toString(entity) : null;
         }else{
        	 StringBuffer sb = new StringBuffer();
        	 sb.append('[');
        	 sb.append(mark);
        	 sb.append("] unexpected response status : ");
        	 sb.append(status);
             throw new ClientProtocolException(sb.toString());
         }
	}
}
