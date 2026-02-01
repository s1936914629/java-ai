package com.deepseek.test;

import com.deepseek.model.PromptTemplate;
import com.deepseek.util.DeepSeekClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Prompt 模板 A/B 测试类
 * <p>
 * 用于对比不同 Prompt 模板的效果
 */
public class PromptTemplateABTest {

    private static final Logger logger = LoggerFactory.getLogger(PromptTemplateABTest.class);
    private final DeepSeekClient deepSeekClient;
    private final List<TestCase> testCases;
    private final Map<String, List<TestResult>> resultsMap;

    /**
     * 构造方法
     * 
     * @param deepSeekClient DeepSeek 客户端
     */
    public PromptTemplateABTest(DeepSeekClient deepSeekClient) {
        this.deepSeekClient = deepSeekClient;
        this.testCases = new ArrayList<>();
        this.resultsMap = new ConcurrentHashMap<>();
    }

    /**
     * 添加测试用例
     * 
     * @param name 测试用例名称
     * @param description 测试用例描述
     * @param params 测试参数
     */
    public void addTestCase(String name, String description, Object[] params) {
        testCases.add(new TestCase(name, description, params));
    }

    /**
     * 执行 A/B 测试
     * 
     * @param templateNames 要测试的模板名称列表
     */
    public void runTest(String... templateNames) {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(testCases.size() * templateNames.length);

        for (TestCase testCase : testCases) {
            for (String templateName : templateNames) {
                executor.submit(() -> {
                    try {
                        long startTime = System.currentTimeMillis();
                        String response = deepSeekClient.chatWithTemplate(templateName, testCase.getParams());
                        long endTime = System.currentTimeMillis();

                        // 评估响应质量（这里简化为示例评分，实际可根据需求实现更复杂的评估）
                        int qualityScore = evaluateQuality(response, testCase);
                        boolean relevant = evaluateRelevance(response, testCase);

                        TestResult result = new TestResult(
                                templateName,
                                testCase.getName(),
                                response,
                                endTime - startTime,
                                qualityScore,
                                relevant
                        );

                        resultsMap.computeIfAbsent(templateName, k -> new ArrayList<>()).add(result);
                        logger.info("测试完成: 模板={}, 测试用例={}, 响应时间={}ms, 质量评分={}, 相关性={}",
                                templateName, testCase.getName(), endTime - startTime, qualityScore, relevant);
                    } catch (Exception e) {
                        logger.error("测试执行失败: {} - {}", templateName, testCase.getName(), e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("测试被中断", e);
        }
        executor.shutdown();
        logger.info("A/B 测试执行完成");
    }

    /**
     * 评估响应质量
     * 
     * @param response 响应内容
     * @param testCase 测试用例
     * @return 质量评分 (1-5)
     */
    private int evaluateQuality(String response, TestCase testCase) {
        // 示例：根据测试用例类型进行简单评估
        if (testCase.getName().startsWith("code_")) {
            // 代码生成场景评估
            if (response != null && !response.isEmpty()) {
                if (response.contains("public static void main") || response.contains("def main")) {
                    return 5;
                } else if (response.contains("function") || response.contains("class")) {
                    return 4;
                } else {
                    return 3;
                }
            }
        } else if (testCase.getName().startsWith("qa_")) {
            // 问答场景评估
            if (response != null && !response.isEmpty()) {
                if (response.length() > 100) {
                    return 5;
                } else if (response.length() > 50) {
                    return 4;
                } else {
                    return 3;
                }
            }
        } else if (testCase.getName().startsWith("summary_")) {
            // 总结场景评估
            if (response != null && !response.isEmpty()) {
                if (response.length() > 150) {
                    return 5;
                } else if (response.length() > 80) {
                    return 4;
                } else {
                    return 3;
                }
            }
        }
        return 2; // 默认较低分
    }

    /**
     * 评估响应相关性
     * 
     * @param response 响应内容
     * @param testCase 测试用例
     * @return 是否相关
     */
    private boolean evaluateRelevance(String response, TestCase testCase) {
        if (response == null || response.isEmpty()) {
            return false;
        }
        // 简单检查响应是否包含参数中的关键词
        for (Object param : testCase.getParams()) {
            if (param != null && response.contains(param.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成测试报告
     * 
     * @return A/B 测试报告
     */
    public ABTestReport generateReport() {
        return new ABTestReport(resultsMap, testCases);
    }

    /**
     * 测试用例类
     */
    private static class TestCase {
        private final String name;
        private final String description;
        private final Object[] params;

        /**
         * 构造方法
         * 
         * @param name 测试用例名称
         * @param description 测试用例描述
         * @param params 测试参数
         */
        public TestCase(String name, String description, Object[] params) {
            this.name = name;
            this.description = description;
            this.params = params;
        }

        /**
         * 获取测试用例名称
         * 
         * @return 测试用例名称
         */
        public String getName() {
            return name;
        }

        /**
         * 获取测试用例描述
         * 
         * @return 测试用例描述
         */
        public String getDescription() {
            return description;
        }

        /**
         * 获取测试参数
         * 
         * @return 测试参数
         */
        public Object[] getParams() {
            return params;
        }
    }

    /**
     * 测试结果类
     */
    private static class TestResult {
        private final String templateName;
        private final String testCaseName;
        private final String response;
        private final long responseTime;
        private final int qualityScore;
        private final boolean relevant;

        /**
         * 构造方法
         * 
         * @param templateName 模板名称
         * @param testCaseName 测试用例名称
         * @param response 响应内容
         * @param responseTime 响应时间
         * @param qualityScore 质量评分
         * @param relevant 是否相关
         */
        public TestResult(String templateName, String testCaseName, String response, long responseTime, int qualityScore, boolean relevant) {
            this.templateName = templateName;
            this.testCaseName = testCaseName;
            this.response = response;
            this.responseTime = responseTime;
            this.qualityScore = qualityScore;
            this.relevant = relevant;
        }

        /**
         * 获取模板名称
         * 
         * @return 模板名称
         */
        public String getTemplateName() {
            return templateName;
        }

        /**
         * 获取测试用例名称
         * 
         * @return 测试用例名称
         */
        public String getTestCaseName() {
            return testCaseName;
        }

        /**
         * 获取响应内容
         * 
         * @return 响应内容
         */
        public String getResponse() {
            return response;
        }

        /**
         * 获取响应时间
         * 
         * @return 响应时间
         */
        public long getResponseTime() {
            return responseTime;
        }

        /**
         * 获取质量评分
         * 
         * @return 质量评分
         */
        public int getQualityScore() {
            return qualityScore;
        }

        /**
         * 获取是否相关
         * 
         * @return 是否相关
         */
        public boolean isRelevant() {
            return relevant;
        }
    }

    /**
     * A/B 测试报告类
     */
    public static class ABTestReport {
        private final Map<String, List<TestResult>> resultsMap;
        private final List<TestCase> testCases;

        /**
         * 构造方法
         * 
         * @param resultsMap 测试结果映射
         * @param testCases 测试用例列表
         */
        public ABTestReport(Map<String, List<TestResult>> resultsMap, List<TestCase> testCases) {
            this.resultsMap = resultsMap;
            this.testCases = testCases;
        }

        /**
         * 打印报告到控制台
         */
        public void printReport() {
            System.out.println("=== Prompt 模板 A/B 测试报告 ===");
            System.out.println("测试时间: " + new Date());
            System.out.println("测试用例数: " + testCases.size());
            System.out.println("参与模板: " + String.join(", ", resultsMap.keySet()));
            System.out.println();

            // 打印详细结果
            for (String templateName : resultsMap.keySet()) {
                System.out.println("--- 模板: " + templateName + " ---");
                List<TestResult> results = resultsMap.get(templateName);
                
                double avgQuality = results.stream().mapToInt(TestResult::getQualityScore).average().orElse(0);
                double avgTime = results.stream().mapToLong(TestResult::getResponseTime).average().orElse(0);
                double relevanceRate = results.stream().filter(TestResult::isRelevant).count() * 100.0 / results.size();

                System.out.println("平均质量得分: " + String.format("%.2f", avgQuality));
                System.out.println("平均响应时间: " + String.format("%.2f", avgTime) + "ms");
                System.out.println("相关性通过率: " + String.format("%.2f", relevanceRate) + "%");
                System.out.println();

                // 打印每个测试用例的详细结果
                for (TestResult result : results) {
                    System.out.println("  测试用例: " + result.getTestCaseName());
                    System.out.println("  质量评分: " + result.getQualityScore());
                    System.out.println("  响应时间: " + result.getResponseTime() + "ms");
                    System.out.println("  相关性: " + (result.isRelevant() ? "是" : "否"));
                    System.out.println("  响应摘要: " + (result.getResponse() != null ? 
                            result.getResponse().substring(0, Math.min(100, result.getResponse().length())) + "..." : "无"));
                    System.out.println();
                }
            }

            // 打印对比分析
            System.out.println("=== 对比分析 ===");
            System.out.println("模板名称 | 平均质量 | 平均响应时间(ms) | 相关性通过率(%)");
            System.out.println("---------|---------|----------------|----------------");
            for (String templateName : resultsMap.keySet()) {
                List<TestResult> results = resultsMap.get(templateName);
                double avgQuality = results.stream().mapToInt(TestResult::getQualityScore).average().orElse(0);
                double avgTime = results.stream().mapToLong(TestResult::getResponseTime).average().orElse(0);
                double relevanceRate = results.stream().filter(TestResult::isRelevant).count() * 100.0 / results.size();

                System.out.printf("%-9s | %.2f    | %.2f          | %.2f        %n", 
                        templateName, avgQuality, avgTime, relevanceRate);
            }
            System.out.println();

            // 打印结论与建议
            System.out.println("=== 结论与建议 ===");
            String bestTemplate = null;
            double bestScore = 0;

            for (String templateName : resultsMap.keySet()) {
                List<TestResult> results = resultsMap.get(templateName);
                double avgQuality = results.stream().mapToInt(TestResult::getQualityScore).average().orElse(0);
                if (avgQuality > bestScore) {
                    bestScore = avgQuality;
                    bestTemplate = templateName;
                }
            }

            if (bestTemplate != null) {
                System.out.println("表现最佳的模板: " + bestTemplate);
                System.out.println("建议: 在相关场景中优先使用此模板");
            }
            System.out.println();
        }

        /**
         * 导出报告为 CSV 文件
         * 
         * @param filename 文件名称
         */
        public void exportToCSV(String filename) {
            try (PrintWriter writer = new PrintWriter(new File(filename))) {
                writer.println("Template,TestCase,QualityScore,ResponseTime,Relevant,Response");
                for (String templateName : resultsMap.keySet()) {
                    List<TestResult> results = resultsMap.get(templateName);
                    for (TestResult result : results) {
                        writer.printf("%s,%s,%d,%d,%b,%s%n",
                                result.getTemplateName(),
                                result.getTestCaseName(),
                                result.getQualityScore(),
                                result.getResponseTime(),
                                result.isRelevant(),
                                result.getResponse() != null ? result.getResponse().replace(",", ";").replace("\n", " ") : ""
                        );
                    }
                }
                System.out.println("报告已导出到: " + filename);
            } catch (FileNotFoundException e) {
                logger.error("导出 CSV 失败", e);
                System.out.println("导出 CSV 失败: " + e.getMessage());
            }
        }
    }
}
