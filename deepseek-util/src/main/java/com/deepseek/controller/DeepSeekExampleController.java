package com.deepseek.controller;

import com.deepseek.model.DeepSeekMessage;
import com.deepseek.model.DeepSeekResponse;
import com.deepseek.util.DeepSeekClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deepseek")
public class DeepSeekExampleController {

    private final DeepSeekClient deepSeekClient;

    @Autowired
    public DeepSeekExampleController(DeepSeekClient deepSeekClient) {
        this.deepSeekClient = deepSeekClient;
    }

    @PostMapping("/chat")
    public DeepSeekResponse chat(@RequestBody List<DeepSeekMessage> messages) {
        return deepSeekClient.chat(messages);
    }

    @PostMapping("/simple-chat")
    public String simpleChat(@RequestBody String prompt) {
        return deepSeekClient.simpleChat(prompt);
    }

    @GetMapping("/health")
    public String health() {
        return "DeepSeek Util Service is running!";
    }

    @PostMapping("/system-chat")
    public String systemChat(@RequestParam String systemPrompt, @RequestParam String userPrompt) {
        return deepSeekClient.simpleChat(systemPrompt, userPrompt);
    }
}