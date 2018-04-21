package com.zldpark.proxy;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class DelegatingServletProxy extends GenericServlet {

	private String targetBean;
    private Servlet proxy;
    public void init() throws ServletException {
        this.targetBean = this.getServletName();
        getServletBean();
        proxy.init(getServletConfig());
    }
    
    @Override
    public void service(ServletRequest req, ServletResponse res)
            throws ServletException, IOException {
        proxy.service(req, res);
    }
 
    private void getServletBean() {
        //实现一个servlet代理,该代理用WebApplicationContext来获得在applicationContext.xml中定义的servlet的对象，并将任务委托给applicationContext.xml中定义的servlet；
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        this.proxy = (Servlet) wac.getBean(targetBean);
    }

}
