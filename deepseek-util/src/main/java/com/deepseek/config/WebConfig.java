package com.deepseek.config;

import com.deepseek.interceptor.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * <p>
 * 注册拦截器等Web相关配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 注册拦截器
     * 
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册限流拦截器，设置令牌桶容量为10，令牌生成速率为5个/秒
        // 容量：允许的最大突发请求数
        // 速率：每秒允许的请求数
        registry.addInterceptor(new RateLimitInterceptor(10, 5))
                // 拦截所有/api/llm路径的请求
                .addPathPatterns("/api/llm/**")
                // 排除健康检查接口
                .excludePathPatterns("/api/llm/health");
    }
}
