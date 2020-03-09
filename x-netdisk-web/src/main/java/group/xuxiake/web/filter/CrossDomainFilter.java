/**
 * Description: 处理http跨域请求
 * Author: baishi
 * Date: 2018/7/12 16:00
 */

package group.xuxiake.web.filter;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CrossDomainFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
    	HttpServletResponse res = (HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        res.setHeader("Access-Control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        res.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");//允许跨域的请求方式
        res.setHeader("Access-Control-Max-Age", "3600");//预检请求的间隔时间
        res.setHeader("Access-Control-Allow-Headers", "X-Token, Origin, No-Cache, X-Requested-With, If-Modified-Since, Pragma, Last-Modified, Cache-Control, Expires, Content-Type, X-E4M-With,userId,token,Access-Control-Allow-Headers");//允许跨域请求携带的请求头
        res.setHeader("Access-Control-Allow-Credentials","true");//若要返回cookie、携带seesion等信息则将此项设置我true
        
        if (httpServletRequest.getMethod().equals("OPTIONS")) {

            res.setStatus(200);

            // hresp.setContentLength(0);

            res.getWriter().write("OPTIONS returns OK");

            return;

        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}