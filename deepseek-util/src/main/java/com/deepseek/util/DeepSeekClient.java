package com.deepseek.util;

import com.deepseek.config.DeepSeekConfig;
import com.deepseek.model.DeepSeekMessage;
import com.deepseek.model.DeepSeekRequest;
import com.deepseek.model.DeepSeekResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * DeepSeek 客户端工具类
 * <p>
 * 用于与 DeepSeek API 进行交互，发送请求并处理响应
 * 包含重试机制和详细的日志记录
 */
@Component
public class DeepSeekClient {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(DeepSeekClient.class);
    
    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    /**
     * 重试退避时间（毫秒）
     */
    private static final long RETRY_BACKOFF_MS = 1000;

    /**
     * DeepSeek 配置
     */
    private final DeepSeekConfig deepSeekConfig;
    
    /**
     * REST 模板
     */
    private final RestTemplate restTemplate;
    
    /**
     * 对象映射器
     */
    private final ObjectMapper objectMapper;

    /**
     * 构造方法
     * 
     * @param deepSeekConfig DeepSeek 配置
     * @param objectMapper 对象映射器
     */
    public DeepSeekClient(DeepSeekConfig deepSeekConfig, ObjectMapper objectMapper) {
        this.deepSeekConfig = deepSeekConfig;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate(this.createRequestFactory());
        logger.info("DeepSeekClient 初始化完成，使用模型: {}", deepSeekConfig.getModel());
    }

    /**
     * 创建请求工厂
     * <p>
     * 配置连接超时和读取超时
     * 
     * @return 客户端请求工厂
     */
    private ClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(deepSeekConfig.getTimeout() * 1000);
        factory.setReadTimeout(deepSeekConfig.getTimeout() * 1000);
        return factory;
    }

    /**
     * 创建 HTTP 头
     * <p>
     * 设置内容类型、认证信息和接受类型
     * 
     * @return HTTP 头
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepSeekConfig.getApiKey());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    /**
     * 多轮对话
     * <p>
     * 使用默认模型进行多轮对话
     * 
     * @param messages 消息列表
     * @return 响应结果
     */
    public DeepSeekResponse chat(List<DeepSeekMessage> messages) {
        return chat(deepSeekConfig.getModel(), messages);
    }

    /**
     * 多轮对话
     * <p>
     * 使用指定模型进行多轮对话
     * 
     * @param model 模型名称
     * @param messages 消息列表
     * @return 响应结果
     */
    public DeepSeekResponse chat(String model, List<DeepSeekMessage> messages) {
        DeepSeekRequest request = DeepSeekRequest.builder()
                .model(model)
                .messages(messages)
                .build();
        return sendRequest(request);
    }

    /**
     * 多轮对话
     * <p>
     * 使用指定模型和参数进行多轮对话
     * 
     * @param model 模型名称
     * @param messages 消息列表
     * @param temperature 温度参数
     * @param maxTokens 最大令牌数
     * @return 响应结果
     */
    public DeepSeekResponse chat(String model, List<DeepSeekMessage> messages, Double temperature, Integer maxTokens) {
        DeepSeekRequest request = DeepSeekRequest.builder()
                .model(model)
                .messages(messages)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
        return sendRequest(request);
    }

    /**
     * 简单对话
     * <p>
     * 发送单个用户消息并获取回复
     * 
     * @param prompt 用户提示
     * @return 回复内容
     */
    public String simpleChat(String prompt) {
        List<DeepSeekMessage> messages = Collections.singletonList(DeepSeekMessage.user(prompt));
        DeepSeekResponse response = chat(messages);
        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
            return response.getChoices().get(0).getMessage().getContent();
        }
        return null;
    }

    /**
     * 带系统提示的简单对话
     * <p>
     * 发送系统提示和用户消息并获取回复
     * 
     * @param systemPrompt 系统提示
     * @param userPrompt 用户提示
     * @return 回复内容
     */
    public String simpleChat(String systemPrompt, String userPrompt) {
        List<DeepSeekMessage> messages = List.of(
                DeepSeekMessage.system(systemPrompt),
                DeepSeekMessage.user(userPrompt)
        );
        DeepSeekResponse response = chat(messages);
        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
            return response.getChoices().get(0).getMessage().getContent();
        }
        return null;
    }

    /**
     * 发送请求
     * <p>
     * 发送请求到 DeepSeek API 并处理响应
     * 包含重试机制和错误处理
     * 
     * @param request 请求对象
     * @return 响应结果
     */
    private DeepSeekResponse sendRequest(DeepSeekRequest request) {
        String url = deepSeekConfig.getBaseUrl() + "/chat/completions";
        HttpEntity<DeepSeekRequest> httpEntity = new HttpEntity<>(request, createHeaders());
        
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                logger.debug("发送请求到 DeepSeek API (尝试 {} / {}): 模型={}, 消息数量={}", 
                    attempt, MAX_RETRY_ATTEMPTS, request.getModel(), request.getMessages().size());
                
                long startTime = System.currentTimeMillis();
                DeepSeekResponse response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, DeepSeekResponse.class).getBody();
                long endTime = System.currentTimeMillis();
                
                logger.info("从 DeepSeek API 接收响应，耗时 {}ms", endTime - startTime);
                if (response != null && response.getChoices() != null) {
                    logger.debug("响应包含 {} 个选项", response.getChoices().size());
                }
                
                return response;
            } catch (HttpServerErrorException e) {
                // 服务器错误，进行重试
                if (attempt < MAX_RETRY_ATTEMPTS && 
                    (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE || 
                     e.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT || 
                     e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS)) {
                    long backoffTime = RETRY_BACKOFF_MS * (1L << (attempt - 1)); // 指数退避
                    logger.warn("服务器错误 ({}), 将在 {}ms 后重试 (尝试 {}/{})", 
                        e.getStatusCode(), backoffTime, attempt, MAX_RETRY_ATTEMPTS);
                    try {
                        Thread.sleep(backoffTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.error("重试被中断", ie);
                        throw new RuntimeException("重试被中断", ie);
                    }
                } else {
                    logger.error("DeepSeek API 服务器错误: {}", e.getStatusCode(), e);
                    throw e;
                }
            } catch (HttpClientErrorException e) {
                // 客户端错误，不重试
                logger.error("DeepSeek API 客户端错误: {}", e.getStatusCode(), e);
                throw e;
            } catch (Exception e) {
                // 其他错误，进行重试
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    long backoffTime = RETRY_BACKOFF_MS * (1L << (attempt - 1)); // 指数退避
                    logger.warn("发生意外错误，将在 {}ms 后重试 (尝试 {}/{})", 
                        backoffTime, attempt, MAX_RETRY_ATTEMPTS);
                    try {
                        Thread.sleep(backoffTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.error("重试被中断", ie);
                        throw new RuntimeException("重试被中断", ie);
                    }
                } else {
                    logger.error("DeepSeek API 发生意外错误", e);
                    throw new RuntimeException("发送请求到 DeepSeek API 失败，已尝试 " + MAX_RETRY_ATTEMPTS + " 次", e);
                }
            }
        }
    }
}