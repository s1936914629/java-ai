package com.deepseek.model;

import java.util.List;

/**
 * DeepSeek API 响应模型
 * <p>
 * 表示从 DeepSeek API 接收到的响应数据
 */
public class DeepSeekResponse {

    /**
     * 响应 ID
     * <p>
     * 唯一标识本次响应
     */
    private String id;
    
    /**
     * 对象类型
     * <p>
     * 响应的对象类型，通常为 "chat.completion"
     */
    private String object;
    
    /**
     * 创建时间
     * <p>
     * 响应创建的时间戳（Unix 时间）
     */
    private Long created;
    
    /**
     * 使用的模型
     * <p>
     * 实际处理请求的模型名称
     */
    private String model;
    
    /**
     * 生成的选择
     * <p>
     * 包含生成的回复选项列表
     */
    private List<Choice> choices;
    
    /**
     * 令牌使用情况
     * <p>
     * 包含提示令牌、完成令牌和总令牌数
     */
    private Usage usage;

    /**
     * 获取响应 ID
     * 
     * @return 响应 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置响应 ID
     * 
     * @param id 响应 ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取对象类型
     * 
     * @return 对象类型
     */
    public String getObject() {
        return object;
    }

    /**
     * 设置对象类型
     * 
     * @param object 对象类型
     */
    public void setObject(String object) {
        this.object = object;
    }

    /**
     * 获取创建时间
     * 
     * @return 创建时间戳
     */
    public Long getCreated() {
        return created;
    }

    /**
     * 设置创建时间
     * 
     * @param created 创建时间戳
     */
    public void setCreated(Long created) {
        this.created = created;
    }

    /**
     * 获取使用的模型
     * 
     * @return 模型名称
     */
    public String getModel() {
        return model;
    }

    /**
     * 设置使用的模型
     * 
     * @param model 模型名称
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * 获取生成的选择
     * 
     * @return 选择列表
     */
    public List<Choice> getChoices() {
        return choices;
    }

    /**
     * 设置生成的选择
     * 
     * @param choices 选择列表
     */
    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    /**
     * 获取令牌使用情况
     * 
     * @return 使用情况
     */
    public Usage getUsage() {
        return usage;
    }

    /**
     * 设置令牌使用情况
     * 
     * @param usage 使用情况
     */
    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    /**
     * 选择类
     * <p>
     * 表示生成的回复选项
     */
    public static class Choice {
        /**
         * 索引
         * <p>
         * 选项在列表中的索引位置
         */
        private Integer index;
        
        /**
         * 消息
         * <p>
         * 生成的消息内容
         */
        private DeepSeekMessage message;
        
        /**
         * 完成原因
         * <p>
         * 生成停止的原因，如 "stop"、"length" 等
         */
        private String finishReason;

        /**
         * 获取索引
         * 
         * @return 索引
         */
        public Integer getIndex() {
            return index;
        }

        /**
         * 设置索引
         * 
         * @param index 索引
         */
        public void setIndex(Integer index) {
            this.index = index;
        }

        /**
         * 获取消息
         * 
         * @return 消息
         */
        public DeepSeekMessage getMessage() {
            return message;
        }

        /**
         * 设置消息
         * 
         * @param message 消息
         */
        public void setMessage(DeepSeekMessage message) {
            this.message = message;
        }

        /**
         * 获取完成原因
         * 
         * @return 完成原因
         */
        public String getFinishReason() {
            return finishReason;
        }

        /**
         * 设置完成原因
         * 
         * @param finishReason 完成原因
         */
        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
    }

    /**
     * 使用情况类
     * <p>
     * 表示令牌使用情况
     */
    public static class Usage {
        /**
         * 提示令牌数
         * <p>
         * 输入提示使用的令牌数
         */
        private Integer promptTokens;
        
        /**
         * 完成令牌数
         * <p>
         * 生成回复使用的令牌数
         */
        private Integer completionTokens;
        
        /**
         * 总令牌数
         * <p>
         * 总使用的令牌数
         */
        private Integer totalTokens;

        /**
         * 获取提示令牌数
         * 
         * @return 提示令牌数
         */
        public Integer getPromptTokens() {
            return promptTokens;
        }

        /**
         * 设置提示令牌数
         * 
         * @param promptTokens 提示令牌数
         */
        public void setPromptTokens(Integer promptTokens) {
            this.promptTokens = promptTokens;
        }

        /**
         * 获取完成令牌数
         * 
         * @return 完成令牌数
         */
        public Integer getCompletionTokens() {
            return completionTokens;
        }

        /**
         * 设置完成令牌数
         * 
         * @param completionTokens 完成令牌数
         */
        public void setCompletionTokens(Integer completionTokens) {
            this.completionTokens = completionTokens;
        }

        /**
         * 获取总令牌数
         * 
         * @return 总令牌数
         */
        public Integer getTotalTokens() {
            return totalTokens;
        }

        /**
         * 设置总令牌数
         * 
         * @param totalTokens 总令牌数
         */
        public void setTotalTokens(Integer totalTokens) {
            this.totalTokens = totalTokens;
        }
    }
}