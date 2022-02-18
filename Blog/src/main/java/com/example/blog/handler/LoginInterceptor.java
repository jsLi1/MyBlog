package com.example.blog.handler;

import com.alibaba.fastjson.JSON;
import com.example.blog.vo.utils.CheckToken;
import com.example.blog.vo.utils.UserThreadLocal;
import com.example.blog.vo.ErrorCode;
import com.example.blog.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
//    @Autowired
//    private LoginService loginService;
    @Autowired
    CheckToken checkToken;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //如果不删除，ThreadLocal中用完的信息会有内存泄露的风险
        UserThreadLocal.remove();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //在执行controller之前进行执行，判断token是否为空
        if(!(handler instanceof HandlerMethod)){
            return true;
        }

        String token= request.getHeader("Authorization");
        log.info("=================request start===========================");
        String requestURI = request.getRequestURI();
        log.info("request uri:{}",requestURI);
        log.info("request method:{}",request.getMethod());
        log.info("token:{}", token);
        log.info("=================request end===========================");
        if(StringUtils.isBlank(token)){
           Result result= Result.fail(ErrorCode.NO_LOGIN.getCode(),ErrorCode.NO_LOGIN.getMsg());
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }
        var sysUser = checkToken.check(token);
        if(sysUser==null){
            Result result= Result.fail(ErrorCode.NO_LOGIN.getCode(),ErrorCode.NO_LOGIN.getMsg());
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }

        //在controller中获取用户信息
        UserThreadLocal.put(sysUser);
        return true;
    }
}
