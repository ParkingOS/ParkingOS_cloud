package com.zld.struts.admin;

import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthFilter implements Filter {
	private String failurl = null;

	Logger logger = Logger.getLogger(AuthFilter.class);

	@SuppressWarnings("unchecked")
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
						 FilterChain chain) throws IOException, ServletException {
		try {
			HttpServletRequest httpServletRequest = ((HttpServletRequest) request);
			String [] rStrings = httpServletRequest.getRequestURI().split("/");
			String requestUrl ="";
			if(rStrings.length>2)
				requestUrl=rStrings[2];
			String action = httpServletRequest.getParameter("action");
			String curl = (action != null)? (requestUrl + "?action=" +action ) : requestUrl;
			RequestDispatcher dispatcher = request.getRequestDispatcher(failurl);
			List<Map<String, Object>> authList = null;
			List<Map<String, Object>> allauth = null;
			if(httpServletRequest.getSession().getAttribute("authlist") != null){
				authList= (List<Map<String, Object>>)httpServletRequest.getSession().getAttribute("authlist");
			}
			if(httpServletRequest.getSession().getAttribute("allauth") != null){
				allauth= (List<Map<String, Object>>)httpServletRequest.getSession().getAttribute("allauth");
			}
			if(allauth != null ){
				boolean regflag = false;
				List<String> allauthList = new ArrayList<String>();
				List<String> ownauthList = new ArrayList<String>();
				for(Map<String, Object> map : allauth){//加载所有的已注册权限
					String url = (String)map.get("url");
					String actions = null;
					if(map.get("actions") != null){
						actions = (String)map.get("actions");
					}
					allauthList.add(url);
					if(url.contains("?")){//有些url里面带有参数
						url = url.split("\\?")[0];//符号?在正则表达示中有相应的不同意义，所以在使用时要进行转义处理。
					}
					if(actions != null){
						String[] actionarr = actions.split(",");
						for(int i = 0; i< actionarr.length; i++){
							String act = actionarr[i];
							if(act.contains(".")){//有些action是完整的url,比如parkinfo.do?action=withdraw
								allauthList.add(act);
							}else if(!act.equals("")){
								allauthList.add(url + "?action=" + act);
							}
						}
					}
				}
				if(authList != null){
					for(Map<String, Object> map : authList){//加载拥有的权限
						String url = (String)map.get("url");
						String actions = null;
						String sub_auth = null;
						if(map.get("actions") != null){
							actions = (String)map.get("actions");
						}
						if(map.get("sub_auth") != null){
							sub_auth = (String)map.get("sub_auth");
						}
						ownauthList.add(url);
						if(url.contains("?")){
							url = url.split("\\?")[0];//符号?在正则表达示中有相应的不同意义，所以在使用时要进行转义处理。
						}
						if(actions != null && sub_auth != null){
							String[] actionarr = actions.split(",");
							String[] authnum = sub_auth.split(",");
							for(int j=0;j<authnum.length;j++){
								Integer actnum = Integer.valueOf(authnum[j]);
								for(int i = 0; i< actionarr.length; i++){
									if(i == actnum){
										String act = actionarr[i];
										if(act.contains(".")){//有些action是完整的url,比如parkinfo.do?action=withdraw
											ownauthList.add(act);
										}else if(!act.equals("")){
											ownauthList.add(url + "?action=" + act);
										}
									}
								}
							}
						}
					}
				}

				for(String aurl : allauthList){//判断当前请求是否在已注册权限里
					if(aurl.contains(curl)){
						regflag = true;
						break;
					}
				}
				if(regflag){
					boolean authflag = false;
					for(String aurl : ownauthList){//判断当前请求是否在已注册权限里
						if(aurl.contains(curl)){
							authflag = true;
							break;
						}
					}
					if(!authflag){
						dispatcher.forward(request, response);
						return;
					}
				}
			}
		} catch (Exception e) {
			logger.error("auth check filter exception", e);
		}
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		failurl = filterConfig.getInitParameter("failurl");
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}
