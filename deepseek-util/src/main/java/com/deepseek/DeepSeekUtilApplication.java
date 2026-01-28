package com.deepseek;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DeepSeek 工具应用主类
 * <p>
 * 应用程序的入口点，启动 Spring Boot 应用
 * 自动配置并初始化所有组件
 */
@SpringBootApplication
public class DeepSeekUtilApplication {

    /**
     * 主方法
     * <p>
     * 应用程序的启动入口
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(DeepSeekUtilApplication.class, args);
    }

}