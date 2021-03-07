package com.tanhua.server.interceptor;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.Nullable;
import com.tanhua.domain.db.User;
import com.tanhua.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 统一token处理拦截器类
 */
@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private UserService userService;

    private static final ObjectMapper mapper = new ObjectMapper();

    private NamedThreadLocal<Long> startTimeThreadLocal = new NamedThreadLocal<>("StopWatch-startTimed");

    /**
     * controller之前拦截token处理
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler){
        log.info("*************统一token处理拦截器类*****************");
        try {
            long timed = System.currentTimeMillis();
            startTimeThreadLocal.set(timed);
            StringBuffer requestURL = request.getRequestURL();
            log.info("111****preHandle****当前请求的URL：{}****",requestURL);
            log.info("111****preHandle****执行目标方法: {}****", handler);
            //获取请求参数
            String queryString = request.getQueryString();
            log.info("111****preHandle****请求参数:{}****", queryString);
        } catch (Exception e) {
            log.error("111****preHandle****异常：****",e);
        }
        //1.获取请求头token
        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)){
            //4.获取不到用户信息，return false; 401 权限不足
            response.setStatus(401);
            return false;
        }
        //2.从token中获取用户信息
        //a.根据TOKEN_xxxx获取用户对象
        User user = userService.getUserByToken(token);
        if (user ==null) {
            //4.获取不到用户信息，return false; 401 权限不足
            response.setStatus(401);
            return false;
        }
        //
        //3.获取到用户信息，存入ThreadLocal
        UserHolder.setUser(user);
        return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        try {
            long timeend = System.currentTimeMillis();
            log.info("333****postHandle****执行业务逻辑代码耗时：【{}】", timeend - startTimeThreadLocal.get());
        } catch (Exception e) {
            log.error("333****postHandle***异常：", e);
        }
    }

}
