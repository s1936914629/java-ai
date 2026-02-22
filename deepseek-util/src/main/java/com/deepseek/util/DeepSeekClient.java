package com.deepseek.util;

import com.deepseek.config.DeepSeekConfig;
import com.deepseek.model.DeepSeekMessage;
import com.deepseek.model.DeepSeekRequest;
import com.deepseek.model.DeepSeekResponse;
import com.deepseek.model.PromptTemplate;
import com.deepseek.llm.LLMClient;
import com.deepseek.llm.LLMMessage;
import com.deepseek.llm.LLMResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek 客户端工具类
 * 用于与 DeepSeek API 进行交互，发送请求并处理响应
 * 包含重试机制和详细的日志记录
 */
@Component
public class DeepSeekClient extends AbstractDeepSeekClient implements LLMClient {

    /**
     * 模板存储
     * 键：模板名称
     * 值：PromptTemplate 对象
     */
    private final Map<String, PromptTemplate> templateMap;

    /**
     * 默认模板列表
     * 提供常用的 10 个 prompt 模板
     */
    private static final List<PromptTemplate> DEFAULT_TEMPLATES = List.of(
        // 1. 通用问答模板
        PromptTemplate.of(
            "general_qa",
            "通用问答模板",
            "你是一个智能助手，能够回答各种问题，提供准确、详细的信息。",
            "{0}",
            1
        ),
        // 2. 代码生成模板
        PromptTemplate.of(
            "code_generator",
            "代码生成模板",
            "你是一位资深程序员，擅长多种编程语言，能够生成高质量、可维护的代码。",
            "请用 {0} 语言实现 {1} 功能",
            2
        ),
        // 3. 内容总结模板
        PromptTemplate.of(
            "summarizer",
            "内容总结模板",
            "你是一位专业的内容总结专家，能够提取核心信息，生成简洁明了的总结。",
            "请总结以下内容：\n{0}",
            1
        ),
        // 4. 邮件撰写模板
        PromptTemplate.of(
            "email_writer",
            "邮件撰写模板",
            "你是一位专业的邮件撰写专家，能够根据不同场景撰写恰当的邮件。",
            "请为 {0} 场景撰写一封邮件，内容是 {1}",
            2
        ),
        // 5. 创意写作模板
        PromptTemplate.of(
            "creative_writing",
            "创意写作模板",
            "你是一位创意写作大师，能够创作引人入胜的故事和内容。",
            "请以 {0} 为主题，创作一篇 {1} 类型的内容",
            2
        ),
        // 6. 翻译模板
        PromptTemplate.of(
            "translator",
            "翻译模板",
            "你是一位专业的翻译专家，能够准确翻译各种文本，保持原文的风格和含义。",
            "请将以下内容翻译成 {0}：\n{1}",
            2
        ),
        // 7. 问题分析模板
        PromptTemplate.of(
            "problem_analyzer",
            "问题分析模板",
            "你是一位问题分析专家，能够深入分析问题，提供全面的解决方案。",
            "请分析以下问题：\n{0}\n并提供解决方案",
            1
        ),
        // 8. 学习辅导模板
        PromptTemplate.of(
            "learning_tutor",
            "学习辅导模板",
            "你是一位耐心的学习辅导老师，能够清晰解释各种知识点，帮助学生理解。",
            "请解释 {0} 概念，并提供 {1} 例子",
            2
        ),
        // 9. 产品描述模板
        PromptTemplate.of(
            "product_description",
            "产品描述模板",
            "你是一位专业的产品描述撰写专家，能够突出产品特点，吸引潜在客户。",
            "请为 {0} 产品撰写一段吸引人的描述，突出其 {1} 特点",
            2
        ),
        // 10. 面试准备模板
        PromptTemplate.of(
            "interview_prep",
            "面试准备模板",
            "你是一位资深的面试教练，能够帮助求职者准备面试，提供专业的建议。",
            "请针对 {0} 职位，准备 {1} 个常见面试问题及回答",
            2
        )
    );

    /**
     * 构造方法
     * 
     * @param deepSeekConfig DeepSeek 配置
     * @param objectMapper 对象映射器
     */
    public DeepSeekClient(DeepSeekConfig deepSeekConfig, ObjectMapper objectMapper) {
        super(deepSeekConfig, objectMapper);
        
        // 初始化模板存储并加载默认模板
        this.templateMap = new HashMap<>();
        loadDefaultTemplates();
        
        logger.info("DeepSeekClient 初始化完成，使用模型: {}, 加载了 {} 个默认模板", 
            deepSeekConfig.getModel(), DEFAULT_TEMPLATES.size());
    }

    /**
     * 加载默认模板
     */
    private void loadDefaultTemplates() {
        for (PromptTemplate template : DEFAULT_TEMPLATES) {
            templateMap.put(template.getName(), template);
            logger.debug("加载默认模板: {}", template.getName());
        }
    }

    /**
     * 多轮对话
     * 使用默认模型进行多轮对话
     */
    @Override
    public LLMResponse chat(List<LLMMessage> messages) {
        return chat(deepSeekConfig.getModel(), messages);
    }

    /**
     * 多轮对话
     * 使用指定模型进行多轮对话
     */
    @Override
    public LLMResponse chat(String model, List<LLMMessage> messages) {
        List<DeepSeekMessage> deepSeekMessages = convertToDeepSeekMessages(messages);
        DeepSeekRequest request = DeepSeekRequest.builder()
                .model(model)
                .messages(deepSeekMessages)
                .build();
        DeepSeekResponse deepSeekResponse = sendRequest(request);
        return convertToLLMResponse(deepSeekResponse);
    }

    /**
     * 多轮对话
     * 使用指定模型和参数进行多轮对话
     */
    @Override
    public LLMResponse chat(String model, List<LLMMessage> messages, Double temperature, Integer maxTokens) {
        List<DeepSeekMessage> deepSeekMessages = convertToDeepSeekMessages(messages);
        DeepSeekRequest request = DeepSeekRequest.builder()
                .model(model)
                .messages(deepSeekMessages)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
        DeepSeekResponse deepSeekResponse = sendRequest(request);
        return convertToLLMResponse(deepSeekResponse);
    }

    /**
     * 获取默认模型名称
     */
    @Override
    public String getDefaultModel() {
        return deepSeekConfig.getModel();
    }

    /**
     * 将通用 LLMMessage 转换为 DeepSeekMessage
     */
    private List<DeepSeekMessage> convertToDeepSeekMessages(List<LLMMessage> messages) {
        List<DeepSeekMessage> deepSeekMessages = new ArrayList<>();
        for (LLMMessage message : messages) {
            DeepSeekMessage deepSeekMessage = new DeepSeekMessage();
            deepSeekMessage.setRole(message.getRole());
            deepSeekMessage.setContent(message.getContent());
            deepSeekMessages.add(deepSeekMessage);
        }
        return deepSeekMessages;
    }

    /**
     * 将 DeepSeekResponse 转换为通用 LLMResponse
     */
    private LLMResponse convertToLLMResponse(DeepSeekResponse deepSeekResponse) {
        return new LLMResponse() {
            {
                if (deepSeekResponse.getChoices() != null) {
                    List<LLMResponse.LLMChoice> choices = new ArrayList<>();
                    for (DeepSeekResponse.Choice choice : deepSeekResponse.getChoices()) {
                        LLMResponse.LLMChoice llmChoice = new LLMResponse.LLMChoice();
                        if (choice.getMessage() != null) {
                            LLMMessage llmMessage = LLMMessage.user(choice.getMessage().getContent());
                            llmMessage.setRole(choice.getMessage().getRole());
                            llmChoice.setMessage(llmMessage);
                        }
                        llmChoice.setIndex(choice.getIndex());
                        choices.add(llmChoice);
                    }
                    setChoices(choices);
                }
                setModel(deepSeekResponse.getModel());
            }
        };
    }

    /**
     * 简单对话
     * 发送单个用户消息并获取回复
     */
    @Override
    public String simpleChat(String prompt) {
        List<LLMMessage> messages = Collections.singletonList(LLMMessage.user(prompt));
        LLMResponse response = chat(messages);
        return response.getFirstChoiceContent();
    }

    /**
     * 带系统提示的简单对话
     * 发送系统提示和用户消息并获取回复
     */
    @Override
    public String simpleChat(String systemPrompt, String userPrompt) {
        List<LLMMessage> messages = List.of(
                LLMMessage.system(systemPrompt),
                LLMMessage.user(userPrompt)
        );
        LLMResponse response = chat(messages);
        return response.getFirstChoiceContent();
    }

    // ==================== 模板管理方法 ====================

    /**
     * 添加模板
     */
    public boolean addTemplate(PromptTemplate template) {
        if (template == null || template.getName() == null) {
            logger.warn("模板或模板名称不能为空");
            return false;
        }
        
        templateMap.put(template.getName(), template);
        logger.info("添加模板成功: {}", template.getName());
        return true;
    }

    /**
     * 获取模板
     */
    public PromptTemplate getTemplate(String name) {
        return templateMap.get(name);
    }

    /**
     * 获取所有模板列表
     */
    public List<PromptTemplate> getAllTemplates() {
        return List.copyOf(templateMap.values());
    }

    /**
     * 根据模板生成消息
     */
    public List<LLMMessage> generateMessagesFromTemplate(String templateName, Object... params) {
        PromptTemplate template = getTemplate(templateName);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在: " + templateName);
        }
        
        String userPrompt = template.buildUserPrompt(params);
        return List.of(
                LLMMessage.system(template.getSystemPrompt()),
                LLMMessage.user(userPrompt)
        );
    }

    /**
     * 使用模板发送请求
     */
    public String chatWithTemplate(String templateName, Object... params) {
        List<LLMMessage> messages = generateMessagesFromTemplate(templateName, params);
        LLMResponse response = chat(messages);
        return response.getFirstChoiceContent();
    }

    /**
     * 发送请求
     * 发送请求到 DeepSeek API 并处理响应
     */
    private DeepSeekResponse sendRequest(DeepSeekRequest request) {
        String url = deepSeekConfig.getBaseUrl() + "/chat/completions";
        HttpEntity<DeepSeekRequest> httpEntity = new HttpEntity<>(request, createHeaders());
        
        return sendRequestWithRetry(url, httpEntity, DeepSeekResponse.class, 
            "发送请求到 DeepSeek API: 模型=" + request.getModel() + ", 消息数量=" + request.getMessages().size(), 
            response -> {
                if (response != null && response.getChoices() != null) {
                    logger.debug("响应包含 {} 个选项", response.getChoices().size());
                }
                return response;
            });
    }
}