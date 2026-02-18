package com.deepseek.interceptor;

import com.deepseek.util.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 限流拦截器
 * <p>
 * 实现基于令牌桶算法的API限流功能
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    /**
     * 限流工具
     */
    private final RateLimiter rateLimiter;

    /**
     * 构造方法
     * 
     * @param capacity 令牌桶容量
     * @param rate 令牌生成速率（个/秒）
     */
    public RateLimitInterceptor(int capacity, int rate) {
        this.rateLimiter = new RateLimiter(capacity, rate);
    }

    /**
     * 请求处理前进行限流检查
     * 
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @return 是否继续处理请求
     * @throws Exception 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 尝试获取令牌
        if (!rateLimiter.tryAcquire()) {
            // 限流处理
            handleRateLimit(response);
            return false;
        }
        return true;
    }

    /**
     * 限流处理
     * 
     * @param response 响应对象
     * @throws IOException IO异常
     */
    private void handleRateLimit(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().write("{\"error\": \"Too many requests, please try again later\", \"status\": 429}");
    }

    /**
     * 请求处理后
     * 
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @param modelAndView 模型视图
     * @throws Exception 异常
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 空实现
    }

    /**
     * 请求完成后
     * 
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @param ex 异常
     * @throws Exception 异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 空实现
    }
}
