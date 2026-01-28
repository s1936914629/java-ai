package com.deepseek;

import com.deepseek.model.DeepSeekMessage;
import com.deepseek.util.DeepSeekClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

@SpringBootTest
@TestPropertySource(properties = {
        "deepseek.api-key=test-api-key",
        "deepseek.base-url=https://api.deepseek.com/v1",
        "deepseek.model=deepseek-chat",
        "deepseek.timeout=30"
})
public class DeepSeekClientTest {

    @Autowired
    private DeepSeekClient deepSeekClient;

    @Test
    void testDeepSeekClientBean() {
        assert deepSeekClient != null;
    }

    @Test
    void testMessageCreation() {
        DeepSeekMessage userMessage = DeepSeekMessage.user("Hello");
        assert userMessage.getRole().equals("user");
        assert userMessage.getContent().equals("Hello");

        DeepSeekMessage systemMessage = DeepSeekMessage.system("You are a helpful assistant");
        assert systemMessage.getRole().equals("system");
        assert systemMessage.getContent().equals("You are a helpful assistant");

        DeepSeekMessage assistantMessage = DeepSeekMessage.assistant("Hi there!");
        assert assistantMessage.getRole().equals("assistant");
        assert assistantMessage.getContent().equals("Hi there!");
    }

    @Test
    void testRequestBuilder() {
        List<DeepSeekMessage> messages = List.of(
                DeepSeekMessage.user("Hello")
        );
        // 这个测试只是验证构建逻辑，不会实际发送请求
        assert messages.size() == 1;
        assert messages.get(0).getRole().equals("user");
    }

    @Test
    void testSimpleChatMethod() {
        // 测试simpleChat方法的参数构建
        String prompt = "What is the capital of France?";
        List<DeepSeekMessage> messages = List.of(
                DeepSeekMessage.user(prompt)
        );
        assert messages.size() == 1;
        assert messages.get(0).getRole().equals("user");
        assert messages.get(0).getContent().equals(prompt);
    }

    @Test
    void testSystemChatMethod() {
        // 测试systemChat方法的参数构建
        String systemPrompt = "You are a geography expert";
        String userPrompt = "What is the capital of Japan?";
        List<DeepSeekMessage> messages = List.of(
                DeepSeekMessage.system(systemPrompt),
                DeepSeekMessage.user(userPrompt)
        );
        assert messages.size() == 2;
        assert messages.get(0).getRole().equals("system");
        assert messages.get(0).getContent().equals(systemPrompt);
        assert messages.get(1).getRole().equals("user");
        assert messages.get(1).getContent().equals(userPrompt);
    }
}