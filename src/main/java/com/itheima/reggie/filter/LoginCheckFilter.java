package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName="LoginCheckFilter", urlPatterns = "/*")//拦截所有请求
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1.获取请求路径
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);

        //2.判单请求路径是否直接放行（不需要登录就能访问的请求）
        String[] urls = new String[]{//直接放行的请求
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        };
        if(check(requestURI,urls)){
            filterChain.doFilter(request,response);//放行
            log.info("请求：{}直接放行",requestURI);
            return;
        }
        //3.对于要求登录后才能访问的请求，判断用户是否登录
        if(request.getSession().getAttribute("employee")!=null){
            log.info("用户已登录，id:{}",request.getSession().getAttribute("employee"));
            filterChain.doFilter(request,response);//放行
            return;
        }

        //4.返回登录页面：通过输出流方式向客户端页面响应数据
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));//将R对象转成json，通过输出流的方式返回
        return;
    }

    /**
     * 匹配路径，查看本次请求是否直接放行
     * @param uri
     * @param urls
     * @return
     */
    public boolean check(String uri,String[] urls){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, uri);
            if(match){
                return true;
            }
        }
        return false;
    }
}
