# DeepSeek Util 项目

基于 Spring Boot 构建的 DeepSeek AI 接口工具库，提供与 DeepSeek 大语言模型的交互能力。

## 🌟 特性
- DeepSeek AI 模型接口封装
- 配置化的 API 调用参数
- 简洁易用的客户端工具类
- RESTful API 接口设计
- 完整的请求/响应模型
- 测试用例覆盖
- **日志功能**：详细的中文日志记录
- **重试机制**：对服务器错误进行指数退避重试
- **Prompt 模板**：内置 10 个常用的提示模板
- **A/B 测试**：支持对比不同模板的表现
- **参数匹配检查**：确保测试用例与模板参数数量匹配
- **中文文档注释**：详细的中文 Javadoc 注释

## 📋 系统要求
- Java: 17 (推荐)
- Maven: 3.6+ (推荐使用Maven进行构建)
- 内存: 至少512MB可用内存

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/s1936914629/java-ai.git
cd java-ai/deepseek-util
```

### 2. 安装依赖
```bash
# 安装Java后端依赖
mvn dependency:resolve
```

### 3. 运行应用
```bash
# 使用Maven运行
mvn spring-boot:run

# 或运行打包后的jar文件
mvn clean package
java -jar target/deepseek-util-0.0.1-SNAPSHOT.jar
```

### 4. 访问应用
- 应用地址: http://localhost:8080/
- API示例: http://localhost:8080/api/deepseek/example

## 📁 项目结构

```text
deepseek-util/
├── src/main/java/com/deepseek/
│   ├── DeepSeekUtilApplication.java     # Spring Boot启动类
│   ├── config/
│   │   └── DeepSeekConfig.java          # DeepSeek配置类
│   ├── controller/
│   │   └── DeepSeekExampleController.java # 示例控制器
│   ├── model/
│   │   ├── DeepSeekMessage.java         # 消息模型
│   │   ├── DeepSeekRequest.java         # 请求模型
│   │   ├── DeepSeekResponse.java        # 响应模型
│   │   └── PromptTemplate.java          # Prompt模板模型
│   ├── test/
│   │   ├── ABTestExample.java           # A/B测试示例
│   │   └── PromptTemplateABTest.java    # Prompt模板A/B测试工具
│   └── util/
│       └── DeepSeekClient.java          # DeepSeek客户端工具类
├── src/main/resources/
│   ├── application.yml                  # 应用配置
│   ├── application-example.yml          # 示例配置文件
│   └── application-local.yml            # 本地配置文件（包含API密钥）
├── src/test/
│   └── java/com/deepseek/
│       └── DeepSeekClientTest.java      # 客户端测试类
├── .gitignore                           # Git忽略文件
├── README.md                            # 项目文档
└── pom.xml                              # Maven配置文件
```

## 🧠 技术架构

### 后端技术栈
- Spring Boot: Web应用框架
- Spring Web: RESTful API支持
- Jackson: JSON序列化/反序列化
- HttpClient: HTTP请求客户端
- SLF4J: 日志框架
- Lombok: 简化Java代码

### 核心组件

#### DeepSeekClient
封装了与DeepSeek API的交互逻辑，提供简洁的方法调用接口，支持重试机制和模板功能。

#### DeepSeekConfig
管理DeepSeek API的配置信息，包括API密钥、模型参数等。

#### PromptTemplate
存储和管理提示模板，支持参数占位符替换。

#### PromptTemplateABTest
A/B测试工具，用于对比不同Prompt模板的表现。

#### 数据模型
- DeepSeekMessage: 表示对话消息
- DeepSeekRequest: 表示API请求
- DeepSeekResponse: 表示API响应
- PromptTemplate: 表示提示模板

## 📊 使用指南

### 使用DeepSeekClient

```java
// 初始化客户端
DeepSeekClient client = new DeepSeekClient();

// 创建请求
DeepSeekRequest request = DeepSeekRequest.builder()
    .model("deepseek-chat")
    .messages(Arrays.asList(
        new DeepSeekMessage("user", "你好，DeepSeek！")
    ))
    .build();

// 发送请求
DeepSeekResponse response = client.chat(request);

// 处理响应
System.out.println(response.getChoices().get(0).getMessage().getContent());
```

### 使用Prompt模板

```java
// 获取模板
PromptTemplate template = client.getTemplate("general_qa");

// 使用模板发送请求
String response = client.chatWithTemplate("general_qa", "什么是人工智能？");
System.out.println(response);

// 生成消息列表
List<DeepSeekMessage> messages = client.generateMessagesFromTemplate("code_generator", "Java", "Hello World程序");

// 添加自定义模板
PromptTemplate customTemplate = PromptTemplate.of(
    "custom_template",
    "自定义模板",
    "你是一位专业的测试助手",
    "请测试 {0} 功能",
    1
);
client.addTemplate(customTemplate);
```

### 使用A/B测试

```java
// 创建A/B测试实例
PromptTemplateABTest abTest = new PromptTemplateABTest(client);

// 添加测试用例
abTest.addTestCase("qa_ai", "什么是人工智能？", new Object[]{"什么是人工智能？"});
abTest.addTestCase("code_hello", "Java Hello World", new Object[]{"Java", "Hello World程序"});

// 执行测试
abTest.runTest("general_qa", "code_generator");

// 生成报告
PromptTemplateABTest.ABTestReport report = abTest.generateReport();
report.printReport();
report.exportToCSV("ab_test_report.csv");
```

### 使用配置文件

在 `application.yml` 中配置DeepSeek API参数：

```yaml
# application.yml
spring:
  application:
    name: deepseek-util
  profiles:
    active: local  # 默认激活 local 环境
  server:
    port: 8081  # 应用端口

# DeepSeek 公共配置
deepseek:
  base-url: https://api.deepseek.com/v1
  model: deepseek-chat
  timeout: 30
  # 注意：api-key 不在此文件配置，由具体环境的配置文件提供
```

在 `application-local.yml` 中配置API密钥：

```yaml
# application-local.yml
deepseek:
  api-key: your-api-key-here  # 在此配置实际的API密钥
```

### API接口

| 接口路径 | 方法 | 描述 |
|---------|------|------|
| `/api/deepseek/example` | GET | 示例接口，测试DeepSeek连接 |
| `/api/deepseek/simple-chat` | GET | 简单聊天接口，支持单轮对话 |
| `/api/deepseek/system-chat` | GET | 系统聊天接口，支持系统提示和用户输入 |
| `/api/deepseek/template-chat` | GET | 模板聊天接口，使用预设的Prompt模板 |

## ⚙️ 配置说明

### 应用配置
```yaml
# application.yml
spring:
  application:
    name: deepseek-util
  profiles:
    active: local  # 默认激活 local 环境
  server:
    port: 8081  # 应用端口

# DeepSeek 公共配置
deepseek:
  base-url: https://api.deepseek.com/v1
  model: deepseek-chat
  timeout: 30
  # 注意：api-key 不在此文件配置，由具体环境的配置文件提供
```

### 本地配置
```yaml
# application-local.yml
deepseek:
  api-key: your-api-key-here  # 在此配置实际的API密钥
```

### 环境变量配置

可以通过环境变量覆盖配置：

```bash
# 设置API密钥
DEEPSEEK_API_KEY=your-api-key-here

# 设置应用端口
SERVER_PORT=8081

# 设置激活的环境
SPRING_PROFILES_ACTIVE=local

# 启动应用
java -jar target/deepseek-util-1.0.0.jar
```

### 内置Prompt模板

项目内置了10个常用的Prompt模板：

| 模板名称 | 描述 | 参数数量 | 参数说明 |
|---------|------|---------|---------|
| general_qa | 通用问答模板 | 1 | 问题内容 |
| code_generator | 代码生成模板 | 2 | 编程语言, 功能描述 |
| summarizer | 内容总结模板 | 1 | 要总结的内容 |
| email_writer | 邮件撰写模板 | 2 | 场景, 内容 |
| creative_writing | 创意写作模板 | 2 | 主题, 类型 |
| translator | 翻译模板 | 2 | 目标语言, 原文 |
| problem_analyzer | 问题分析模板 | 1 | 问题描述 |
| learning_tutor | 学习辅导模板 | 2 | 概念, 例子数量 |
| product_description | 产品描述模板 | 2 | 产品名称, 特点 |
| interview_prep | 面试准备模板 | 2 | 职位, 问题数量 |

## 🔧 故障排除

### 常见问题

#### 1. API密钥配置错误
解决方案：
- 确保在 `application-local.yml` 中正确配置了API密钥
- 或通过环境变量 `DEEPSEEK_API_KEY` 设置

#### 2. 网络连接失败
解决方案：
- 检查网络连接是否正常
- 确保可以访问DeepSeek API地址
- 检查防火墙设置，确保允许应用访问外部网络

#### 3. 启动失败：端口被占用
解决方案：
```bash
# 修改应用端口
java -jar target/deepseek-util-1.0.0.jar --server.port=8082
```

#### 4. 参数不匹配错误
解决方案：
- 确保测试用例的参数数量与模板需要的参数数量匹配
- 查看日志中的警告信息，了解具体的参数不匹配情况

#### 5. 超时错误
解决方案：
- 增加 `deepseek.timeout` 配置值
- 减少请求的复杂度和长度

### 日志查看
应用日志输出到控制台，主要包含：
- 应用启动信息
- API调用日志
- 错误和异常信息
- 重试机制的日志
- A/B测试的日志
- 模板管理相关日志

## 🚢 部署指南

### 生产环境部署

#### 1. 构建应用
```bash
mvn clean package -DskipTests
```

#### 2. 创建启动脚本
```bash
#!/bin/bash
# run.sh
export JAVA_HOME=/path/to/java17
export JAVA_OPTS="-Xmx1g -Xms512m -Dspring.profiles.active=prod"
export DEEPSEEK_API_KEY=your-api-key-here
java $JAVA_OPTS -jar deepseek-util-1.0.0.jar
```

### Docker 部署

```dockerfile
# Dockerfile
FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn clean package -DskipTests

FROM openjdk:17-jre-slim
WORKDIR /app
COPY --from=build /app/target/deepseek-util-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

构建和运行：
```bash
docker build -t deepseek-util .
docker run -p 8081:8081 -e DEEPSEEK_API_KEY=your-api-key-here -d deepseek-util
```

## 📚 API 文档

### 示例 API
- **URL**: `/api/deepseek/example`
- **方法**: GET
- **响应**: JSON格式的示例响应

### DeepSeekClient API

#### 方法: chat
- **参数**: `List<DeepSeekMessage>` 消息列表
- **返回值**: `DeepSeekResponse` 响应对象
- **描述**: 发送聊天请求到DeepSeek API

#### 方法: chat
- **参数**: `String` 模型名称, `List<DeepSeekMessage>` 消息列表
- **返回值**: `DeepSeekResponse` 响应对象
- **描述**: 使用指定模型发送聊天请求到DeepSeek API

#### 方法: chatWithTemplate
- **参数**: `String` 模板名称, `Object...` 模板参数
- **返回值**: `String` 响应内容
- **描述**: 使用指定模板发送聊天请求到DeepSeek API

#### 方法: generateMessagesFromTemplate
- **参数**: `String` 模板名称, `Object...` 模板参数
- **返回值**: `List<DeepSeekMessage>` 消息列表
- **描述**: 根据指定模板生成消息列表

#### 方法: getTemplate
- **参数**: `String` 模板名称
- **返回值**: `PromptTemplate` 模板对象
- **描述**: 获取指定名称的模板

#### 方法: getAllTemplates
- **返回值**: `List<PromptTemplate>` 模板列表
- **描述**: 获取所有模板

#### 方法: addTemplate
- **参数**: `PromptTemplate` 模板对象
- **返回值**: `boolean` 是否添加成功
- **描述**: 添加自定义模板

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建功能分支
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. 提交更改
   ```bash
   git commit -m 'Add some amazing feature'
   ```
4. 推送到分支
   ```bash
   git push origin feature/amazing-feature
   ```
5. 开启 Pull Request

## 📄 许可证

本项目基于 MIT 许可证开源。详情请查看 LICENSE 文件。

## 🙏 致谢

- DeepSeek AI - 大语言模型服务
- Spring Boot - Java应用框架
- Jackson - JSON处理库

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 GitHub Issue
- 邮箱：1936914629@qq.com
- 项目主页：https://github.com/s1936914629/java-ai

祝您使用愉快！ 🎉