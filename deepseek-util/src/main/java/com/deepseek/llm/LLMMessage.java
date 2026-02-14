package com.deepseek.llm;

/**
 * LLM 消息抽象类
 * <p>
 * 定义通用的消息结构，支持不同模型提供商的消息格式
 */
public abstract class LLMMessage {

    /**
     * 消息角色
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 构造方法
     * 
     * @param role 消息角色
     * @param content 消息内容
     */
    protected LLMMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    /**
     * 获取消息角色
     * 
     * @return 消息角色
     */
    public String getRole() {
        return role;
    }

    /**
     * 设置消息角色
     * 
     * @param role 消息角色
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * 获取消息内容
     * 
     * @return 消息内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置消息内容
     * 
     * @param content 消息内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 创建系统消息
     * 
     * @param content 消息内容
     * @return 系统消息
     */
    public static LLMMessage system(String content) {
        return new DefaultLLMMessage("system", content);
    }

    /**
     * 创建用户消息
     * 
     * @param content 消息内容
     * @return 用户消息
     */
    public static LLMMessage user(String content) {
        return new DefaultLLMMessage("user", content);
    }

    /**
     * 创建助手消息
     * 
     * @param content 消息内容
     * @return 助手消息
     */
    public static LLMMessage assistant(String content) {
        return new DefaultLLMMessage("assistant", content);
    }

    /**
     * 默认 LLM 消息实现
     */
    private static class DefaultLLMMessage extends LLMMessage {
        public DefaultLLMMessage(String role, String content) {
            super(role, content);
        }
    }
}
