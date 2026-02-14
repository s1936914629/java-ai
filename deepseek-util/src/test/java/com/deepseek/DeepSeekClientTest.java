package com.deepseek;

import com.deepseek.model.PromptTemplate;
import com.deepseek.llm.LLMClient;
import com.deepseek.llm.LLMMessage;
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
        LLMMessage userMessage = LLMMessage.user("Hello");
        assert userMessage.getRole().equals("user");
        assert userMessage.getContent().equals("Hello");

        LLMMessage systemMessage = LLMMessage.system("You are a helpful assistant");
        assert systemMessage.getRole().equals("system");
        assert systemMessage.getContent().equals("You are a helpful assistant");

        LLMMessage assistantMessage = LLMMessage.assistant("Hi there!");
        assert assistantMessage.getRole().equals("assistant");
        assert assistantMessage.getContent().equals("Hi there!");
    }

    @Test
    void testRequestBuilder() {
        List<LLMMessage> messages = List.of(
                LLMMessage.user("Hello")
        );
        // 这个测试只是验证构建逻辑，不会实际发送请求
        assert messages.size() == 1;
        assert messages.get(0).getRole().equals("user");
    }

    @Test
    void testSimpleChatMethod() {
        // 测试simpleChat方法的参数构建
        String prompt = "What is the capital of France?";
        List<LLMMessage> messages = List.of(
                LLMMessage.user(prompt)
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
        List<LLMMessage> messages = List.of(
                LLMMessage.system(systemPrompt),
                LLMMessage.user(userPrompt)
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
        List<LLMMessage> messages = deepSeekClient.generateMessagesFromTemplate(
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
        // 测试 A/B 测试功能（实际执行测试）
        
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
        // 适合 translator 模板的测试用例（需要 2 个参数）
        abTest.addTestCase("translate_test", "翻译测试", new Object[]{"中文", "Hello, how are you?"});
        // 适合 problem_analyzer 模板的测试用例（需要 1 个参数）
        abTest.addTestCase("problem_solve", "问题分析", new Object[]{"如何提高学习效率？"});
        // 适合 learning_tutor 模板的测试用例（需要 2 个参数）
        abTest.addTestCase("learn_math", "学习辅导", new Object[]{"微积分", "3个"});
        // 适合 product_description 模板的测试用例（需要 2 个参数）
        abTest.addTestCase("product_desc", "产品描述", new Object[]{"智能手机", "拍照和续航"});
        // 适合 interview_prep 模板的测试用例（需要 2 个参数）
        abTest.addTestCase("interview_java", "面试准备", new Object[]{"Java开发工程师", "5"});
        // 适合 creative_writing 模板的测试用例（需要 2 个参数）
        abTest.addTestCase("creative_story", "创意写作", new Object[]{"未来世界", "科幻小说"});
        // 适合 email_writer 模板的测试用例（需要 2 个参数）
        abTest.addTestCase("email_business", "邮件撰写", new Object[]{"商务合作", "邀请对方进行合作洽谈"});
        
        System.out.println("开始执行 A/B 测试...");
        abTest.runTest("code_generator", "general_qa", "summarizer", "translator", "problem_analyzer", 
                      "learning_tutor", "product_description", "interview_prep", "creative_writing", "email_writer");
        System.out.println("测试执行完成，生成报告...");

        // 生成报告
        com.deepseek.test.PromptTemplateABTest.ABTestReport report = abTest.generateReport();
        report.printReport();
        report.exportToCSV("prompt_ab_test_report.csv");


        
        System.out.println("A/B 测试功能测试完成！");
    }
} 