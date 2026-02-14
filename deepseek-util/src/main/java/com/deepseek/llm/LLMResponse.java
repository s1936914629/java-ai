package com.deepseek.llm;

import java.util.List;

/**
 * LLM 响应抽象类
 * <p>
 * 定义通用的响应结构，支持不同模型提供商的响应格式
 */
public abstract class LLMResponse {

    /**
     * 响应选择列表
     */
    private List<LLMChoice> choices;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 完成时间（毫秒）
     */
    private Long completionTime;

    /**
     * 获取响应选择列表
     * 
     * @return 响应选择列表
     */
    public List<LLMChoice> getChoices() {
        return choices;
    }

    /**
     * 设置响应选择列表
     * 
     * @param choices 响应选择列表
     */
    public void setChoices(List<LLMChoice> choices) {
        this.choices = choices;
    }

    /**
     * 获取使用的模型
     * 
     * @return 使用的模型
     */
    public String getModel() {
        return model;
    }

    /**
     * 设置使用的模型
     * 
     * @param model 使用的模型
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * 获取完成时间
     * 
     * @return 完成时间（毫秒）
     */
    public Long getCompletionTime() {
        return completionTime;
    }

    /**
     * 设置完成时间
     * 
     * @param completionTime 完成时间（毫秒）
     */
    public void setCompletionTime(Long completionTime) {
        this.completionTime = completionTime;
    }

    /**
     * 获取第一个选择的内容
     * 
     * @return 第一个选择的内容
     */
    public String getFirstChoiceContent() {
        if (choices != null && !choices.isEmpty()) {
            LLMChoice choice = choices.get(0);
            if (choice != null && choice.getMessage() != null) {
                return choice.getMessage().getContent();
            }
        }
        return null;
    }

    /**
     * LLM 选择类
     * <p>
     * 表示响应中的一个选择
     */
    public static class LLMChoice {
        private LLMMessage message;
        private Integer index;

        /**
         * 获取消息
         * 
         * @return 消息
         */
        public LLMMessage getMessage() {
            return message;
        }

        /**
         * 设置消息
         * 
         * @param message 消息
         */
        public void setMessage(LLMMessage message) {
            this.message = message;
        }

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
    }
}
