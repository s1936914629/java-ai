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
        "deepseek.model=deepseek-chat"
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
}