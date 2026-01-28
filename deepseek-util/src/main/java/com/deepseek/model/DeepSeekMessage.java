package com.deepseek.model;

/**
 * DeepSeek 消息模型
 * <p>
 * 表示与 DeepSeek API 交互时的消息对象
 * 包含角色和内容两个核心字段
 */
public class DeepSeekMessage {

    /**
     * 消息角色
     * <p>
     * 可选值: user, assistant, system
     */
    private String role;
    
    /**
     * 消息内容
     * <p>
     * 消息的具体文本内容
     */
    private String content;

    /**
     * 默认构造方法
     */
    public DeepSeekMessage() {
    }

    /**
     * 构造方法
     * 
     * @param role 消息角色
     * @param content 消息内容
     */
    public DeepSeekMessage(String role, String content) {
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
     * 创建消息实例
     * 
     * @param role 消息角色
     * @param content 消息内容
     * @return DeepSeekMessage 实例
     */
    public static DeepSeekMessage of(String role, String content) {
        return new DeepSeekMessage(role, content);
    }

    /**
     * 创建用户消息
     * 
     * @param content 消息内容
     * @return DeepSeekMessage 实例，角色为 user
     */
    public static DeepSeekMessage user(String content) {
        return new DeepSeekMessage("user", content);
    }

    /**
     * 创建助手消息
     * 
     * @param content 消息内容
     * @return DeepSeekMessage 实例，角色为 assistant
     */
    public static DeepSeekMessage assistant(String content) {
        return new DeepSeekMessage("assistant", content);
    }

    /**
     * 创建系统消息
     * 
     * @param content 消息内容
     * @return DeepSeekMessage 实例，角色为 system
     */
    public static DeepSeekMessage system(String content) {
        return new DeepSeekMessage("system", content);
    }
}