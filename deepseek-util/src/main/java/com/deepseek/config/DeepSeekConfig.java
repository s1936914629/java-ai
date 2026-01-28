package com.deepseek.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek AI 配置类
 * <p>
 * 用于从应用配置中读取 DeepSeek API 相关的配置信息
 * 通过 @ConfigurationProperties 注解自动绑定配置文件中的属性
 */
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {

    /**
     * DeepSeek API 密钥
     * <p>
     * 用于认证请求，必填项
     */
    private String apiKey;
    
    /**
     * DeepSeek API 基础 URL
     * <p>
     * 默认值: https://api.deepseek.com/v1
     */
    private String baseUrl = "https://api.deepseek.com/v1";
    
    /**
     * 使用的模型名称
     * <p>
     * 默认值: deepseek-chat
     */
    private String model = "deepseek-chat";
    
    /**
     * 请求超时时间（秒）
     * <p>
     * 默认值: 30秒
     */
    private Integer timeout = 30;

    /**
     * 获取 API 密钥
     * 
     * @return API 密钥
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * 设置 API 密钥
     * 
     * @param apiKey API 密钥
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 获取 API 基础 URL
     * 
     * @return API 基础 URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * 设置 API 基础 URL
     * 
     * @param baseUrl API 基础 URL
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 获取模型名称
     * 
     * @return 模型名称
     */
    public String getModel() {
        return model;
    }

    /**
     * 设置模型名称
     * 
     * @param model 模型名称
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * 获取超时时间
     * 
     * @return 超时时间（秒）
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * 设置超时时间
     * 
     * @param timeout 超时时间（秒）
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}