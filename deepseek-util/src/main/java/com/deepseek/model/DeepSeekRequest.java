package com.deepseek.model;

import java.util.List;

/**
 * DeepSeek API 请求模型
 * <p>
 * 用于构建发送给 DeepSeek API 的请求参数
 * 包含模型名称、消息列表以及各种生成参数
 */
public class DeepSeekRequest {

    /**
     * 模型名称
     * <p>
     * 指定要使用的 DeepSeek 模型
     */
    private String model;
    
    /**
     * 消息列表
     * <p>
     * 包含系统消息、用户消息和助手消息的历史记录
     */
    private List<DeepSeekMessage> messages;
    
    /**
     * 温度参数
     * <p>
     * 控制生成文本的随机性，值越高越随机
     * 默认值: 0.7
     */
    private Double temperature = 0.7;
    
    /**
     * Top-P 参数
     * <p>
     * 控制生成文本的多样性，值越低越集中
     * 默认值: 1.0
     */
    private Double topP = 1.0;
    
    /**
     * 最大令牌数
     * <p>
     * 限制生成文本的最大长度
     * 默认值: 1024
     */
    private Integer maxTokens = 1024;
    
    /**
     * 流式输出
     * <p>
     * 是否使用流式输出模式
     * 默认值: false
     */
    private Boolean stream = false;
    
    /**
     * 停止词
     * <p>
     * 当生成到指定词时停止
     */
    private String stop;

    /**
     * 默认构造方法
     */
    public DeepSeekRequest() {
    }

    /**
     * 构造方法
     * 
     * @param model 模型名称
     * @param messages 消息列表
     */
    public DeepSeekRequest(String model, List<DeepSeekMessage> messages) {
        this.model = model;
        this.messages = messages;
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
     * 获取消息列表
     * 
     * @return 消息列表
     */
    public List<DeepSeekMessage> getMessages() {
        return messages;
    }

    /**
     * 设置消息列表
     * 
     * @param messages 消息列表
     */
    public void setMessages(List<DeepSeekMessage> messages) {
        this.messages = messages;
    }

    /**
     * 获取温度参数
     * 
     * @return 温度参数
     */
    public Double getTemperature() {
        return temperature;
    }

    /**
     * 设置温度参数
     * 
     * @param temperature 温度参数
     */
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    /**
     * 获取 Top-P 参数
     * 
     * @return Top-P 参数
     */
    public Double getTopP() {
        return topP;
    }

    /**
     * 设置 Top-P 参数
     * 
     * @param topP Top-P 参数
     */
    public void setTopP(Double topP) {
        this.topP = topP;
    }

    /**
     * 获取最大令牌数
     * 
     * @return 最大令牌数
     */
    public Integer getMaxTokens() {
        return maxTokens;
    }

    /**
     * 设置最大令牌数
     * 
     * @param maxTokens 最大令牌数
     */
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    /**
     * 获取流式输出设置
     * 
     * @return 是否启用流式输出
     */
    public Boolean getStream() {
        return stream;
    }

    /**
     * 设置流式输出
     * 
     * @param stream 是否启用流式输出
     */
    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    /**
     * 获取停止词
     * 
     * @return 停止词
     */
    public String getStop() {
        return stop;
    }

    /**
     * 设置停止词
     * 
     * @param stop 停止词
     */
    public void setStop(String stop) {
        this.stop = stop;
    }

    /**
     * DeepSeekRequest 构建器类
     * <p>
     * 使用构建器模式创建 DeepSeekRequest 实例
     */
    public static class Builder {
        private String model;
        private List<DeepSeekMessage> messages;
        private Double temperature = 0.7;
        private Double topP = 1.0;
        private Integer maxTokens = 1024;
        private Boolean stream = false;
        private String stop;

        /**
         * 设置模型名称
         * 
         * @param model 模型名称
         * @return 构建器实例
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * 设置消息列表
         * 
         * @param messages 消息列表
         * @return 构建器实例
         */
        public Builder messages(List<DeepSeekMessage> messages) {
            this.messages = messages;
            return this;
        }

        /**
         * 设置温度参数
         * 
         * @param temperature 温度参数
         * @return 构建器实例
         */
        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        /**
         * 设置 Top-P 参数
         * 
         * @param topP Top-P 参数
         * @return 构建器实例
         */
        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        /**
         * 设置最大令牌数
         * 
         * @param maxTokens 最大令牌数
         * @return 构建器实例
         */
        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        /**
         * 设置流式输出
         * 
         * @param stream 是否启用流式输出
         * @return 构建器实例
         */
        public Builder stream(Boolean stream) {
            this.stream = stream;
            return this;
        }

        /**
         * 设置停止词
         * 
         * @param stop 停止词
         * @return 构建器实例
         */
        public Builder stop(String stop) {
            this.stop = stop;
            return this;
        }

        /**
         * 构建 DeepSeekRequest 实例
         * 
         * @return DeepSeekRequest 实例
         */
        public DeepSeekRequest build() {
            DeepSeekRequest request = new DeepSeekRequest();
            request.setModel(model);
            request.setMessages(messages);
            request.setTemperature(temperature);
            request.setTopP(topP);
            request.setMaxTokens(maxTokens);
            request.setStream(stream);
            request.setStop(stop);
            return request;
        }
    }

    /**
     * 创建构建器实例
     * 
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }
}