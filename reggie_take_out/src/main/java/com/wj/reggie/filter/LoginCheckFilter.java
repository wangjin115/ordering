package com.wj.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.wj.reggie.common.BaseContext;
import com.wj.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author wj
 * @version 1.0
 * ユーザーがログインを終了したかどうかを確認する
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //パスマッチャー、ワイルドカードをサポート
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request =(HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;
//1、リクエストURIの取得
        String requestURI = request.getRequestURI();

        log.info("リクエストはインターセプトされたのは：{}",requestURI);
//処理不要なリクエストパスの定義
        String[] urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**"
        };
//2、このリクエストの処理が必要かどうかを判断
        boolean check = check(urls, requestURI);
//3、処理が不要な場合は直接放行
        if (check){
            log.info("現在のリクエスト{}扱う必要がない",request);
             filterChain.doFilter(request,response);
             return;
        }
//4、ログイン状態をチェックし、ログイン済みの場合は直接放行
       if( request.getSession().getAttribute("employee")!=null){
            log.info("ユーザーはログインしており、ユーザーIDは：{}",request.getSession().getAttribute("employee"));

           Long empId = (Long)request.getSession().getAttribute("employee");
           BaseContext.setCurrentId(empId);


           filterChain.doFilter(request,response);
           return;
       }
        //5、未ログインの場合、ログインしていない結果を返し、出力ストリームを使用してクライアントページにデータを応答する
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

        log.info("ユーザーがログインしていない");

    }

    public boolean check(String[] urls,String requestURI){
        for (String url:urls){
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;

            }
        }

        return false;
    }
}
