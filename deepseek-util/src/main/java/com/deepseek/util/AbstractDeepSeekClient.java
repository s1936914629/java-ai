package com.deepseek.util;

import com.deepseek.config.DeepSeekConfig;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.function.Function;

/**
 * DeepSeek 客户端抽象基类
 * 提取共同的代码和功能，减少重复
 */
public abstract class AbstractDeepSeekClient {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractDeepSeekClient.class);
    protected static final int MAX_RETRY_ATTEMPTS = 3;
    protected static final long RETRY_BACKOFF_MS = 1000;

    protected final DeepSeekConfig deepSeekConfig;
    protected final RestTemplate restTemplate;
    protected final ObjectMapper objectMapper;

    /**
     * 构造方法
     * 
     * @param deepSeekConfig DeepSeek 配置
     * @param objectMapper 对象映射器
     */
    protected AbstractDeepSeekClient(DeepSeekConfig deepSeekConfig, ObjectMapper objectMapper) {
        this.deepSeekConfig = deepSeekConfig;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate(createRequestFactory());
    }

    /**
     * 创建请求工厂
     * 配置连接超时和读取超时
     * 
     * @return 客户端请求工厂
     */
    protected ClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(deepSeekConfig.getTimeout() * 1000);
        factory.setReadTimeout(deepSeekConfig.getTimeout() * 1000);
        return factory;
    }

    /**
     * 创建 HTTP 头
     * 设置内容类型、认证信息和接受类型
     * 
     * @return HTTP 头
     */
    protected HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepSeekConfig.getApiKey());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    /**
     * 发送请求（带重试机制）
     * 
     * @param url 请求 URL
     * @param httpEntity HTTP 实体
     * @param responseType 响应类型
     * @param logMessage 日志消息
     * @param <T> 响应类型泛型
     * @return 响应结果
     */
    protected <T> T sendRequestWithRetry(String url, HttpEntity<?> httpEntity, 
                                         Class<T> responseType, String logMessage) {
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                logger.debug("{}(尝试 {} / {})", logMessage, attempt, MAX_RETRY_ATTEMPTS);
                
                long startTime = System.currentTimeMillis();
                T response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, responseType).getBody();
                long endTime = System.currentTimeMillis();
                
                logger.info("从 DeepSeek API 接收响应，耗时 {}ms", endTime - startTime);
                
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

    /**
     * 发送请求（带重试机制和自定义响应处理）
     * 
     * @param url 请求 URL
     * @param httpEntity HTTP 实体
     * @param responseType 响应类型
     * @param logMessage 日志消息
     * @param responseProcessor 响应处理器
     * @param <T> 响应类型泛型
     * @param <R> 处理结果类型泛型
     * @return 处理后的结果
     */
    protected <T, R> R sendRequestWithRetry(String url, HttpEntity<?> httpEntity, 
                                           Class<T> responseType, String logMessage, 
                                           Function<T, R> responseProcessor) {
        T response = sendRequestWithRetry(url, httpEntity, responseType, logMessage);
        return responseProcessor.apply(response);
    }
}