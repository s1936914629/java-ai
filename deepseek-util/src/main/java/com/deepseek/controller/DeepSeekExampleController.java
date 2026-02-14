package com.deepseek.controller;

import com.deepseek.llm.LLMClient;
import com.deepseek.llm.LLMMessage;
import com.deepseek.llm.LLMResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LLM 工具接口控制器
 * <p>
 * 提供与大语言模型交互的 RESTful 接口，支持多种聊天模式
 */
@RestController
@RequestMapping("/api/llm")
public class DeepSeekExampleController {

    private final LLMClient llmClient;

    @Autowired
    public DeepSeekExampleController(@Qualifier("deepSeekClient") LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    /**
     * 多轮对话接口
     * <p>
     * 支持上下文连续的多轮对话，需要传递完整的消息历史
     * 
     * @param messages 消息列表，包含系统消息、用户消息和助手消息
     * @return LLM 模型的响应结果
     */
    @PostMapping("/chat")
    public LLMResponse chat(@RequestBody List<LLMMessage> messages) {
        return llmClient.chat(messages);
    }

    /**
     * 简单对话接口
     * <p>
     * 适用于单次问答场景，直接传递用户问题即可
     * 
     * @param prompt 用户问题或提示
     * @return 模型生成的回答
     */
    @PostMapping("/simple-chat")
    public String simpleChat(@RequestBody String prompt) {
        return llmClient.simpleChat(prompt);
    }

    /**
     * 健康检查接口
     * <p>
     * 用于检查服务是否正常运行
     * 
     * @return 服务状态信息
     */
    @GetMapping("/health")
    public String health() {
        return "LLM Util Service is running!";
    }

    /**
     * 带系统提示的对话接口
     * <p>
     * 允许设置系统提示来指导模型的行为和回答风格
     * 
     * @param systemPrompt 系统提示，用于设定模型的角色和行为
     * @param userPrompt 用户问题或提示
     * @return 模型生成的回答
     */
    @PostMapping("/system-chat")
    public String systemChat(@RequestParam String systemPrompt, @RequestParam String userPrompt) {
        return llmClient.simpleChat(systemPrompt, userPrompt);
    }
}