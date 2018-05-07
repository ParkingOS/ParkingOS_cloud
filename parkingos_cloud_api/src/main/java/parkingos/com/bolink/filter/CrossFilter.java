package parkingos.com.bolink.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class CrossFilter implements Filter{
    public void init(FilterConfig filterConfig) throws ServletException {

    }
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        System.out.println("======进入拦截器");
        HttpServletResponse response= (HttpServletResponse) servletResponse;
//        String origin= servletRequest.getRemoteHost()+":"+servletRequest.getRemotePort();

        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, HEAD");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        response.addHeader("Access-Control-Max-Age", "3600");
        filterChain.doFilter(servletRequest,servletResponse);
    }
    public void destroy() {

    }
}

