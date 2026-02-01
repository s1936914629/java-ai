package com.deepseek.test;

import com.deepseek.config.DeepSeekConfig;
import com.deepseek.util.DeepSeekClient;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A/B 测试示例类
 * <p>
 * 展示如何使用 PromptTemplateABTest 执行模板对比测试
 */
public class ABTestExample {

    public static void main(String[] args) {
        // 注意：在实际使用中，应该通过 Spring 依赖注入获取 DeepSeekClient
        // 这里为了示例，我们创建一个简单的配置
        DeepSeekConfig config = new DeepSeekConfig();
        config.setApiKey("test-api-key");
        config.setBaseUrl("https://api.deepseek.com/v1");
        config.setModel("deepseek-chat");
        config.setTimeout(30);

        ObjectMapper objectMapper = new ObjectMapper();
        DeepSeekClient deepSeekClient = new DeepSeekClient(config, objectMapper);

        // 创建 A/B 测试实例
        PromptTemplateABTest abTest = new PromptTemplateABTest(deepSeekClient);

        // 添加测试用例
        // 1. 代码生成测试
        abTest.addTestCase("code_hello", "Java Hello World", new Object[]{"Java", "Hello World程序"});
        abTest.addTestCase("code_sort", "Python 列表排序", new Object[]{"Python", "列表排序功能"});

        // 2. 问答测试
        abTest.addTestCase("qa_ai", "什么是人工智能？", new Object[]{"什么是人工智能？"});
        abTest.addTestCase("qa_programming", "如何学习编程？", new Object[]{"如何学习编程？"});

        // 3. 内容总结测试
        abTest.addTestCase("summary_text", "内容总结", new Object[]{"人工智能（Artificial Intelligence，简称AI）是指通过计算机程序模拟人类智能的技术。它涵盖了机器学习、深度学习、自然语言处理等多个领域。人工智能的发展已经在图像识别、语音助手、自动驾驶等领域取得了显著成果。未来，人工智能有望在医疗、教育、金融等更多行业发挥重要作用，为人类生活带来更多便利。"});

        // 执行测试（对比 3 个模板）
        System.out.println("开始执行 A/B 测试...");
        abTest.runTest("code_generator", "general_qa", "summarizer");
        System.out.println("测试执行完成，生成报告...");

        // 生成报告
        PromptTemplateABTest.ABTestReport report = abTest.generateReport();
        report.printReport();
        report.exportToCSV("prompt_ab_test_report.csv");

        System.out.println("A/B 测试示例完成！");
    }
}
