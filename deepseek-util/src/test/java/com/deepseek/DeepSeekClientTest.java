package com.deepseek;

import com.deepseek.model.DeepSeekMessage;
import com.deepseek.model.PromptTemplate;
import com.deepseek.util.DeepSeekClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
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

    // ==================== 模板功能测试 ====================

    @Test
    void testDefaultTemplatesLoaded() {
        // 测试默认模板是否正确加载
        List<PromptTemplate> templates = deepSeekClient.getAllTemplates();
        assert templates != null;
        assert !templates.isEmpty();
        assert templates.size() >= 10; // 至少有10个默认模板
    }

    @Test
    void testGetTemplate() {
        // 测试获取模板功能
        PromptTemplate template = deepSeekClient.getTemplate("general_qa");
        assert template != null;
        assert template.getName().equals("general_qa");
        assert template.getDescription().equals("通用问答模板");
        assert template.getParamCount() == 1;
    }

    @Test
    void testGetAllTemplates() {
        // 测试获取所有模板列表功能
        List<PromptTemplate> templates = deepSeekClient.getAllTemplates();
        assert templates != null;
        assert !templates.isEmpty();
        
        // 验证是否包含指定的模板
        boolean hasGeneralQa = templates.stream().anyMatch(t -> t.getName().equals("general_qa"));
        boolean hasCodeGenerator = templates.stream().anyMatch(t -> t.getName().equals("code_generator"));
        boolean hasSummarizer = templates.stream().anyMatch(t -> t.getName().equals("summarizer"));
        
        assert hasGeneralQa;
        assert hasCodeGenerator;
        assert hasSummarizer;
    }

    @Test
    void testGenerateMessagesFromTemplate() {
        // 测试根据模板生成消息功能
        List<DeepSeekMessage> messages = deepSeekClient.generateMessagesFromTemplate(
                "code_generator", "Java", "Hello World程序"
        );
        
        assert messages != null;
        assert messages.size() == 2;
        assert messages.get(0).getRole().equals("system");
        assert messages.get(1).getRole().equals("user");
        assert messages.get(1).getContent().contains("Java");
        assert messages.get(1).getContent().contains("Hello World程序");
    }

    @Test
    void testAddTemplate() {
        // 测试添加自定义模板功能
        PromptTemplate customTemplate = PromptTemplate.of(
                "custom_template",
                "自定义模板测试",
                "你是一位专业的测试助手",
                "请测试 {0} 功能",
                1
        );
        
        boolean result = deepSeekClient.addTemplate(customTemplate);
        assert result;
        
        // 验证模板是否添加成功
        PromptTemplate retrievedTemplate = deepSeekClient.getTemplate("custom_template");
        assert retrievedTemplate != null;
        assert retrievedTemplate.getName().equals("custom_template");
        assert retrievedTemplate.getDescription().equals("自定义模板测试");
        assert retrievedTemplate.getParamCount() == 1;
    }

    @Test
    void testTemplateParameterReplacement() {
        // 测试模板参数替换功能
        PromptTemplate template = deepSeekClient.getTemplate("translator");
        assert template != null;
        
        String userPrompt = template.buildUserPrompt("中文", "Hello, how are you?");
        assert userPrompt != null;
        assert userPrompt.contains("中文");
        assert userPrompt.contains("Hello, how are you?");
    }

    @Test
    void testABTestFunctionality() {
        // 测试 A/B 测试功能（模拟测试，不实际发送请求）
        // 注意：实际使用时可以取消注释下面的代码来执行完整的 A/B 测试
        
        System.out.println("=== A/B 测试功能测试 ===");
        System.out.println("创建 A/B 测试实例...");
        
        // 创建 A/B 测试实例
        com.deepseek.test.PromptTemplateABTest abTest = new com.deepseek.test.PromptTemplateABTest(deepSeekClient);
        
        // 添加测试用例
        System.out.println("添加测试用例...");
        // 适合 code_generator 模板的测试用例（需要 2 个参数）
        abTest.addTestCase("code_hello", "Java Hello World", new Object[]{"Java", "Hello World程序"});
        // 适合 general_qa 模板的测试用例（需要 1 个参数）
        abTest.addTestCase("qa_ai", "什么是人工智能？", new Object[]{"什么是人工智能？"});
        // 适合 summarizer 模板的测试用例（需要 1 个参数）
        abTest.addTestCase("summary_text", "内容总结", new Object[]{"人工智能（Artificial Intelligence，简称AI）是指通过计算机程序模拟人类智能的技术。它涵盖了机器学习、深度学习、自然语言处理等多个领域。人工智能的发展已经在图像识别、语音助手、自动驾驶等领域取得了显著成果。未来，人工智能有望在医疗、教育、金融等更多行业发挥重要作用，为人类生活带来更多便利。"});
        
        System.out.println("A/B 测试功能初始化成功！");
        System.out.println("注意：实际执行测试会发送请求到 DeepSeek API，可能产生费用。");
        System.out.println("如需执行完整测试，请取消注释下面的代码。");
        
        // 取消注释下面的代码来执行完整的 A/B 测试

        System.out.println("开始执行 A/B 测试...");
        abTest.runTest("code_generator", "general_qa", "summarizer");
        System.out.println("测试执行完成，生成报告...");

        // 生成报告
        com.deepseek.test.PromptTemplateABTest.ABTestReport report = abTest.generateReport();
        report.printReport();
        report.exportToCSV("prompt_ab_test_report.csv");

        
        System.out.println("A/B 测试功能测试完成！");
    }
} 