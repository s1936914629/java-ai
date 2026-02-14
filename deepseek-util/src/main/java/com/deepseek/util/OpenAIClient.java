package com.deepseek.util;

import com.deepseek.config.OpenAIConfig;
import com.deepseek.llm.LLMClient;
import com.deepseek.llm.LLMMessage;
import com.deepseek.llm.LLMResponse;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 客户端工具类
 * <p>
 * 用于与 OpenAI API 进行交互，发送请求并处理响应
 * 包含重试机制和详细的日志记录
 */
@Component
public class OpenAIClient implements LLMClient {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(OpenAIClient.class);
    
    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    /**
     * 重试退避时间（毫秒）
     */
    private static final long RETRY_BACKOFF_MS = 1000;

    /**
     * OpenAI 配置
     */
    private final OpenAIConfig openAIConfig;
    
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
     * @param openAIConfig OpenAI 配置
     * @param objectMapper 对象映射器
     */
    public OpenAIClient(OpenAIConfig openAIConfig, ObjectMapper objectMapper) {
        this.openAIConfig = openAIConfig;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate(this.createRequestFactory());
        
        // 加载默认模板
        loadDefaultTemplates();
        
        logger.info("OpenAIClient 初始化完成，使用模型: {}, 加载了 {} 个默认模板", 
            openAIConfig.getModel(), DEFAULT_TEMPLATES.size());
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
        factory.setConnectTimeout(openAIConfig.getTimeout() * 1000);
        factory.setReadTimeout(openAIConfig.getTimeout() * 1000);
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
        headers.setBearerAuth(openAIConfig.getApiKey());
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
    @Override
    public LLMResponse chat(List<LLMMessage> messages) {
        return chat(openAIConfig.getModel(), messages);
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
    @Override
    public LLMResponse chat(String model, List<LLMMessage> messages) {
        OpenAIRequest request = OpenAIRequest.builder()
                .model(model)
                .messages(convertToOpenAIMessages(messages))
                .build();
        OpenAIResponse response = sendRequest(request);
        return convertToLLMResponse(response);
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
    @Override
    public LLMResponse chat(String model, List<LLMMessage> messages, Double temperature, Integer maxTokens) {
        OpenAIRequest request = OpenAIRequest.builder()
                .model(model)
                .messages(convertToOpenAIMessages(messages))
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
        OpenAIResponse response = sendRequest(request);
        return convertToLLMResponse(response);
    }

    /**
     * 简单对话
     * <p>
     * 发送单个用户消息并获取回复
     * 
     * @param prompt 用户提示
     * @return 回复内容
     */
    @Override
    public String simpleChat(String prompt) {
        List<LLMMessage> messages = Collections.singletonList(LLMMessage.user(prompt));
        LLMResponse response = chat(messages);
        return response.getFirstChoiceContent();
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
    @Override
    public String simpleChat(String systemPrompt, String userPrompt) {
        List<LLMMessage> messages = List.of(
                LLMMessage.system(systemPrompt),
                LLMMessage.user(userPrompt)
        );
        LLMResponse response = chat(messages);
        return response.getFirstChoiceContent();
    }

    /**
     * 获取默认模型名称
     * 
     * @return 默认模型名称
     */
    @Override
    public String getDefaultModel() {
        return openAIConfig.getModel();
    }

    // ==================== 模板管理方法 ====================

    /**
     * 模板存储
     * <p>
     * 键：模板名称
     * 值：PromptTemplate 对象
     */
    private final Map<String, com.deepseek.model.PromptTemplate> templateMap = new HashMap<>();

    /**
     * 默认模板列表
     * <p>
     * 提供常用的 10 个 prompt 模板
     */
    private static final List<com.deepseek.model.PromptTemplate> DEFAULT_TEMPLATES = List.of(
        // 1. 通用问答模板
        com.deepseek.model.PromptTemplate.of(
            "general_qa",
            "通用问答模板",
            "你是一个智能助手，能够回答各种问题，提供准确、详细的信息。",
            "{0}",
            1
        ),
        // 2. 代码生成模板
        com.deepseek.model.PromptTemplate.of(
            "code_generator",
            "代码生成模板",
            "你是一位资深程序员，擅长多种编程语言，能够生成高质量、可维护的代码。",
            "请用 {0} 语言实现 {1} 功能",
            2
        ),
        // 3. 内容总结模板
        com.deepseek.model.PromptTemplate.of(
            "summarizer",
            "内容总结模板",
            "你是一位专业的内容总结专家，能够提取核心信息，生成简洁明了的总结。",
            "请总结以下内容：\n{0}",
            1
        ),
        // 4. 邮件撰写模板
        com.deepseek.model.PromptTemplate.of(
            "email_writer",
            "邮件撰写模板",
            "你是一位专业的邮件撰写专家，能够根据不同场景撰写恰当的邮件。",
            "请为 {0} 场景撰写一封邮件，内容是 {1}",
            2
        ),
        // 5. 创意写作模板
        com.deepseek.model.PromptTemplate.of(
            "creative_writing",
            "创意写作模板",
            "你是一位创意写作大师，能够创作引人入胜的故事和内容。",
            "请以 {0} 为主题，创作一篇 {1} 类型的内容",
            2
        ),
        // 6. 翻译模板
        com.deepseek.model.PromptTemplate.of(
            "translator",
            "翻译模板",
            "你是一位专业的翻译专家，能够准确翻译各种文本，保持原文的风格和含义。",
            "请将以下内容翻译成 {0}：\n{1}",
            2
        ),
        // 7. 问题分析模板
        com.deepseek.model.PromptTemplate.of(
            "problem_analyzer",
            "问题分析模板",
            "你是一位问题分析专家，能够深入分析问题，提供全面的解决方案。",
            "请分析以下问题：\n{0}\n并提供解决方案",
            1
        ),
        // 8. 学习辅导模板
        com.deepseek.model.PromptTemplate.of(
            "learning_tutor",
            "学习辅导模板",
            "你是一位耐心的学习辅导老师，能够清晰解释各种知识点，帮助学生理解。",
            "请解释 {0} 概念，并提供 {1} 例子",
            2
        ),
        // 9. 产品描述模板
        com.deepseek.model.PromptTemplate.of(
            "product_description",
            "产品描述模板",
            "你是一位专业的产品描述撰写专家，能够突出产品特点，吸引潜在客户。",
            "请为 {0} 产品撰写一段吸引人的描述，突出其 {1} 特点",
            2
        ),
        // 10. 面试准备模板
        com.deepseek.model.PromptTemplate.of(
            "interview_prep",
            "面试准备模板",
            "你是一位资深的面试教练，能够帮助求职者准备面试，提供专业的建议。",
            "请针对 {0} 职位，准备 {1} 个常见面试问题及回答",
            2
        )
    );

    /**
     * 初始化模板存储并加载默认模板
     */
    private void loadDefaultTemplates() {
        for (com.deepseek.model.PromptTemplate template : DEFAULT_TEMPLATES) {
            templateMap.put(template.getName(), template);
            logger.debug("加载默认模板: {}", template.getName());
        }
    }

    /**
     * 添加模板
     * 
     * @param template 模板对象
     * @return 是否添加成功
     */
    @Override
    public boolean addTemplate(com.deepseek.model.PromptTemplate template) {
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
     * 
     * @param name 模板名称
     * @return 模板对象，如果不存在则返回 null
     */
    @Override
    public com.deepseek.model.PromptTemplate getTemplate(String name) {
        return templateMap.get(name);
    }

    /**
     * 获取所有模板列表
     * 
     * @return 模板列表
     */
    @Override
    public List<com.deepseek.model.PromptTemplate> getAllTemplates() {
        return List.copyOf(templateMap.values());
    }

    /**
     * 根据模板生成消息
     * 
     * @param templateName 模板名称
     * @param params 模板参数
     * @return 消息列表
     */
    @Override
    public List<LLMMessage> generateMessagesFromTemplate(String templateName, Object... params) {
        com.deepseek.model.PromptTemplate template = getTemplate(templateName);
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
     * 
     * @param templateName 模板名称
     * @param params 模板参数
     * @return 回复内容
     */
    @Override
    public String chatWithTemplate(String templateName, Object... params) {
        List<LLMMessage> messages = generateMessagesFromTemplate(templateName, params);
        LLMResponse response = chat(messages);
        return response.getFirstChoiceContent();
    }

    /**
     * 将通用 LLMMessage 转换为 OpenAIMessage
     * 
     * @param messages 通用消息列表
     * @return OpenAI 消息列表
     */
    private List<OpenAIMessage> convertToOpenAIMessages(List<LLMMessage> messages) {
        List<OpenAIMessage> openAIMessages = new ArrayList<>();
        for (LLMMessage message : messages) {
            OpenAIMessage openAIMessage = new OpenAIMessage();
            openAIMessage.setRole(message.getRole());
            openAIMessage.setContent(message.getContent());
            openAIMessages.add(openAIMessage);
        }
        return openAIMessages;
    }

    /**
     * 将 OpenAIResponse 转换为通用 LLMResponse
     * 
     * @param openAIResponse OpenAI 响应
     * @return 通用 LLM 响应
     */
    private LLMResponse convertToLLMResponse(OpenAIResponse openAIResponse) {
        return new LLMResponse() {
            {
                if (openAIResponse.getChoices() != null) {
                    List<LLMResponse.LLMChoice> choices = new ArrayList<>();
                    for (OpenAIResponse.Choice choice : openAIResponse.getChoices()) {
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
                setModel(openAIResponse.getModel());
            }
        };
    }

    /**
     * 发送请求
     * <p>
     * 发送请求到 OpenAI API 并处理响应
     * 包含重试机制和错误处理
     * 
     * @param request 请求对象
     * @return 响应结果
     */
    private OpenAIResponse sendRequest(OpenAIRequest request) {
        String url = openAIConfig.getBaseUrl() + "/chat/completions";
        HttpEntity<OpenAIRequest> httpEntity = new HttpEntity<>(request, createHeaders());
        
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                logger.debug("发送请求到 OpenAI API (尝试 {} / {}): 模型={}, 消息数量={}", 
                    attempt, MAX_RETRY_ATTEMPTS, request.getModel(), request.getMessages().size());
                
                long startTime = System.currentTimeMillis();
                OpenAIResponse response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, OpenAIResponse.class).getBody();
                long endTime = System.currentTimeMillis();
                
                logger.info("从 OpenAI API 接收响应，耗时 {}ms", endTime - startTime);
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
                    logger.error("OpenAI API 服务器错误: {}", e.getStatusCode(), e);
                    throw e;
                }
            } catch (HttpClientErrorException e) {
                // 客户端错误，不重试
                logger.error("OpenAI API 客户端错误: {}", e.getStatusCode(), e);
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
                    logger.error("OpenAI API 发生意外错误", e);
                    throw new RuntimeException("发送请求到 OpenAI API 失败，已尝试 " + MAX_RETRY_ATTEMPTS + " 次", e);
                }
            }
        }
    }

    /**
     * OpenAI 请求类
     */
    public static class OpenAIRequest {
        private String model;
        private List<OpenAIMessage> messages;
        private Double temperature;
        private Integer maxTokens;

        public static Builder builder() {
            return new Builder();
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public List<OpenAIMessage> getMessages() {
            return messages;
        }

        public void setMessages(List<OpenAIMessage> messages) {
            this.messages = messages;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Integer getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
        }

        public static class Builder {
            private OpenAIRequest request = new OpenAIRequest();

            public Builder model(String model) {
                request.setModel(model);
                return this;
            }

            public Builder messages(List<OpenAIMessage> messages) {
                request.setMessages(messages);
                return this;
            }

            public Builder temperature(Double temperature) {
                request.setTemperature(temperature);
                return this;
            }

            public Builder maxTokens(Integer maxTokens) {
                request.setMaxTokens(maxTokens);
                return this;
            }

            public OpenAIRequest build() {
                return request;
            }
        }
    }

    /**
     * OpenAI 消息类
     */
    public static class OpenAIMessage {
        private String role;
        private String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    /**
     * OpenAI 响应类
     */
    public static class OpenAIResponse {
        private List<Choice> choices;
        private String model;

        public List<Choice> getChoices() {
            return choices;
        }

        public void setChoices(List<Choice> choices) {
            this.choices = choices;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public static class Choice {
            private OpenAIMessage message;
            private Integer index;

            public OpenAIMessage getMessage() {
                return message;
            }

            public void setMessage(OpenAIMessage message) {
                this.message = message;
            }

            public Integer getIndex() {
                return index;
            }

            public void setIndex(Integer index) {
                this.index = index;
            }
        }
    }
}
