package com.deepseek.test;

import com.deepseek.util.DeepSeekClient;

/**
 * A/B 测试示例类
 * <p>
 * 展示如何使用 PromptTemplateABTest 执行模板对比测试
 * <p>
 * 注意：此类不再在应用启动时自动运行，需要显式调用 runExample 方法
 */
public class ABTestExample {

    private final DeepSeekClient deepSeekClient;

    /**
     * 构造方法
     * 
     * @param deepSeekClient DeepSeek 客户端
     */
    public ABTestExample(DeepSeekClient deepSeekClient) {
        this.deepSeekClient = deepSeekClient;
    }

    /**
     * 运行 A/B 测试示例
     */
    public void runExample() throws Exception {
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

        // 执行测试（为不同类型的模板使用适合的测试用例）
        System.out.println("开始执行 A/B 测试...");
        
        // 为代码生成模板创建专门的测试实例
        PromptTemplateABTest codeAbTest = new PromptTemplateABTest(deepSeekClient);
        codeAbTest.addTestCase("code_hello", "Java Hello World", new Object[]{"Java", "Hello World程序"});
        codeAbTest.addTestCase("code_sort", "Python 列表排序", new Object[]{"Python", "列表排序功能"});
        codeAbTest.runTest("code_generator");
        
        // 为问答和总结模板创建专门的测试实例
        PromptTemplateABTest qaAbTest = new PromptTemplateABTest(deepSeekClient);
        qaAbTest.addTestCase("qa_ai", "什么是人工智能？", new Object[]{"什么是人工智能？"});
        qaAbTest.addTestCase("qa_programming", "如何学习编程？", new Object[]{"如何学习编程？"});
        qaAbTest.addTestCase("summary_text", "内容总结", new Object[]{"人工智能（Artificial Intelligence，简称AI）是指通过计算机程序模拟人类智能的技术。它涵盖了机器学习、深度学习、自然语言处理等多个领域。人工智能的发展已经在图像识别、语音助手、自动驾驶等领域取得了显著成果。未来，人工智能有望在医疗、教育、金融等更多行业发挥重要作用，为人类生活带来更多便利。"});
        qaAbTest.runTest("general_qa", "summarizer");
        
        // 合并结果并生成报告
        System.out.println("测试执行完成，生成报告...");

        // 生成报告
        System.out.println("=== 代码生成模板测试报告 ===");
        PromptTemplateABTest.ABTestReport codeReport = codeAbTest.generateReport();
        codeReport.printReport();
        codeReport.exportToCSV("code_prompt_ab_test_report.csv");
        
        System.out.println("=== 问答和总结模板测试报告 ===");
        PromptTemplateABTest.ABTestReport qaReport = qaAbTest.generateReport();
        qaReport.printReport();
        qaReport.exportToCSV("qa_prompt_ab_test_report.csv");

        System.out.println("A/B 测试示例完成！");
    }

    /**
     * 主方法
     * <p>
     * 用于直接运行 A/B 测试示例
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        System.out.println("注意：ABTestExample 不再自动运行。");
        System.out.println("如需运行 A/B 测试示例，请通过依赖注入获取 ABTestExample 实例并调用 runExample() 方法。");
        System.out.println("或者，您可以创建一个专门的测试类来运行 A/B 测试。");
    }
}
