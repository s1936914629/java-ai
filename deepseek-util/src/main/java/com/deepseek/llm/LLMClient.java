package com.deepseek.llm;

import com.deepseek.model.PromptTemplate;

import java.util.List;

/**
 * LLM 客户端抽象接口
 * <p>
 * 定义通用的大语言模型操作方法，支持不同模型提供商的实现
 */
public interface LLMClient {

    /**
     * 多轮对话
     * <p>
     * 使用默认模型进行多轮对话
     * 
     * @param messages 消息列表
     * @return 响应结果
     */
    LLMResponse chat(List<LLMMessage> messages);

    /**
     * 多轮对话
     * <p>
     * 使用指定模型进行多轮对话
     * 
     * @param model 模型名称
     * @param messages 消息列表
     * @return 响应结果
     */
    LLMResponse chat(String model, List<LLMMessage> messages);

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
    LLMResponse chat(String model, List<LLMMessage> messages, Double temperature, Integer maxTokens);

    /**
     * 简单对话
     * <p>
     * 发送单个用户消息并获取回复
     * 
     * @param prompt 用户提示
     * @return 回复内容
     */
    String simpleChat(String prompt);

    /**
     * 带系统提示的简单对话
     * <p>
     * 发送系统提示和用户消息并获取回复
     * 
     * @param systemPrompt 系统提示
     * @param userPrompt 用户提示
     * @return 回复内容
     */
    String simpleChat(String systemPrompt, String userPrompt);

    /**
     * 获取默认模型名称
     * 
     * @return 默认模型名称
     */
    String getDefaultModel();

    // ==================== 模板管理方法 ====================

    /**
     * 添加模板
     * 
     * @param template 模板对象
     * @return 是否添加成功
     */
    boolean addTemplate(PromptTemplate template);

    /**
     * 获取模板
     * 
     * @param name 模板名称
     * @return 模板对象，如果不存在则返回 null
     */
    PromptTemplate getTemplate(String name);

    /**
     * 获取所有模板列表
     * 
     * @return 模板列表
     */
    List<PromptTemplate> getAllTemplates();

    /**
     * 根据模板生成消息
     * 
     * @param templateName 模板名称
     * @param params 模板参数
     * @return 消息列表
     */
    List<LLMMessage> generateMessagesFromTemplate(String templateName, Object... params);

    /**
     * 使用模板发送请求
     * 
     * @param templateName 模板名称
     * @param params 模板参数
     * @return 回复内容
     */
    String chatWithTemplate(String templateName, Object... params);
}
