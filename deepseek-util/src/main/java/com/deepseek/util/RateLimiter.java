package com.deepseek.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 令牌桶限流工具类
 * <p>
 * 实现基于令牌桶算法的限流功能，支持平滑突发流量
 */
public class RateLimiter {

    /**
     * 令牌桶容量
     */
    private final int capacity;

    /**
     * 令牌生成速率（个/秒）
     */
    private final int rate;

    /**
     * 当前令牌数
     */
    private final AtomicInteger tokens;

    /**
     * 最后令牌生成时间
     */
    private final AtomicLong lastRefillTime;

    /**
     * 构造方法
     * 
     * @param capacity 令牌桶容量
     * @param rate 令牌生成速率（个/秒）
     */
    public RateLimiter(int capacity, int rate) {
        this.capacity = capacity;
        this.rate = rate;
        this.tokens = new AtomicInteger(capacity);
        this.lastRefillTime = new AtomicLong(System.currentTimeMillis());
    }

    /**
     * 尝试获取令牌
     * 
     * @return 是否获取成功
     */
    public synchronized boolean tryAcquire() {
        // 先补充令牌
        refillTokens();
        
        // 尝试获取令牌
        if (tokens.get() > 0) {
            tokens.decrementAndGet();
            return true;
        }
        return false;
    }

    /**
     * 补充令牌
     */
    private void refillTokens() {
        long now = System.currentTimeMillis();
        long elapsedTime = now - lastRefillTime.get();
        
        // 计算应该补充的令牌数
        int tokensToAdd = (int) (elapsedTime * rate / 1000);
        
        if (tokensToAdd > 0) {
            int currentTokens = tokens.get();
            int newTokens = Math.min(currentTokens + tokensToAdd, capacity);
            tokens.set(newTokens);
            lastRefillTime.set(now);
        }
    }

    /**
     * 获取当前令牌数
     * 
     * @return 当前令牌数
     */
    public int getCurrentTokens() {
        refillTokens();
        return tokens.get();
    }

    /**
     * 获取令牌桶容量
     * 
     * @return 令牌桶容量
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * 获取令牌生成速率
     * 
     * @return 令牌生成速率（个/秒）
     */
    public int getRate() {
        return rate;
    }
}
