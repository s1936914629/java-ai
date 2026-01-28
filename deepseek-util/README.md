# DeepSeek Util 项目

基于 Spring Boot 构建的 DeepSeek AI 接口工具库，提供与 DeepSeek 大语言模型的交互能力。

## 🌟 特性
- DeepSeek AI 模型接口封装
- 配置化的 API 调用参数
- 简洁易用的客户端工具类
- RESTful API 接口设计
- 完整的请求/响应模型
- 测试用例覆盖

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
│   │   └── DeepSeekResponse.java        # 响应模型
│   └── util/
│       └── DeepSeekClient.java          # DeepSeek客户端工具类
├── src/main/resources/
│   └── application.yml                  # 应用配置
├── src/test/
│   └── java/com/deepseek/
│       └── DeepSeekClientTest.java      # 客户端测试类
├── README.md                            # 项目文档
└── pom.xml                              # Maven配置文件
```

## 🧠 技术架构

### 后端技术栈
- Spring Boot: Web应用框架
- Spring Web: RESTful API支持
- Jackson: JSON序列化/反序列化
- HttpClient: HTTP请求客户端
- Lombok: 简化Java代码

### 核心组件

#### DeepSeekClient
封装了与DeepSeek API的交互逻辑，提供简洁的方法调用接口。

#### DeepSeekConfig
管理DeepSeek API的配置信息，包括API密钥、模型参数等。

#### 数据模型
- DeepSeekMessage: 表示对话消息
- DeepSeekRequest: 表示API请求
- DeepSeekResponse: 表示API响应

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
DeepSeekResponse response = client.complete(request);

// 处理响应
System.out.println(response.getChoices().get(0).getMessage().getContent());
```

### 使用配置文件

在 `application.yml` 中配置DeepSeek API参数：

```yaml
# application.yml
deepseek:
  api-key: your-api-key-here
  base-url: https://api.deepseek.com/v1/chat/completions
  model: deepseek-chat
  temperature: 0.7
  max-tokens: 1000
```

### API接口

| 接口路径 | 方法 | 描述 |
|---------|------|------|
| `/api/deepseek/example` | GET | 示例接口，测试DeepSeek连接 |

## ⚙️ 配置说明

### 应用配置
```yaml
# application.yml
spring:
  application:
    name: deepseek-util

server:
  port: 8080

deepseek:
  api-key: your-api-key-here
  base-url: https://api.deepseek.com/v1/chat/completions
  model: deepseek-chat
  temperature: 0.7
  max-tokens: 1000
```

### 环境变量配置

可以通过环境变量覆盖配置：

```bash
# 设置API密钥
DEEPSEEK_API_KEY=your-api-key-here

# 启动应用
java -jar target/deepseek-util-0.0.1-SNAPSHOT.jar
```

## 🔧 故障排除

### 常见问题

#### 1. API密钥配置错误
解决方案：
- 确保在 `application.yml` 中正确配置了API密钥
- 或通过环境变量 `DEEPSEEK_API_KEY` 设置

#### 2. 网络连接失败
解决方案：
- 检查网络连接是否正常
- 确保可以访问DeepSeek API地址

#### 3. 启动失败：端口被占用
解决方案：
```bash
# 修改应用端口
java -jar target/deepseek-util-0.0.1-SNAPSHOT.jar --server.port=8081
```

### 日志查看
应用日志输出到控制台，主要包含：
- 应用启动信息
- API调用日志
- 错误和异常信息

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
java $JAVA_OPTS -jar deepseek-util-0.0.1-SNAPSHOT.jar
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
COPY --from=build /app/target/deepseek-util-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

构建和运行：
```bash
docker build -t deepseek-util .
docker run -p 8080:8080 -e DEEPSEEK_API_KEY=your-api-key-here -d deepseek-util
```

## 📚 API 文档

### 示例 API
- **URL**: `/api/deepseek/example`
- **方法**: GET
- **响应**: JSON格式的示例响应

### DeepSeekClient API

#### 方法: complete
- **参数**: `DeepSeekRequest` 请求对象
- **返回值**: `DeepSeekResponse` 响应对象
- **描述**: 发送聊天完成请求到DeepSeek API

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