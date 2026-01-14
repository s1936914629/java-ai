# Java-AI Demo 项目

基于 Spring Boot 和 ONNX Runtime 构建的 AI 图像分类演示项目，支持 ResNet50 模型的图像分类推理服务。

## 🌟 特性
- 基于 ResNet50 模型的图像分类功能
- RESTful API 接口设计
- 支持图片上传和分类预测
- 提供详细的前端集成指南
- 提供可复用的 SDK
- Swagger API 文档支持
- 健康检查接口

## 📋 系统要求
- Java: 17 (推荐)
- Maven: 3.6+ (推荐使用Maven进行构建)
- 内存: 至少1GB可用内存

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/s1936914629/java-ai.git
cd java-ai/java-ai-demo
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
java -jar target/java-ai-demo-0.0.1-SNAPSHOT.jar
```

### 4. 访问应用
- 应用地址: http://localhost:8080/
- API文档: http://localhost:8080/swagger-ui/index.html
- 健康检查: http://localhost:8080/api/health

## 📁 项目结构

```text
java-ai-demo/
├── src/main/java/org/sqx/javaaidemo/
│   ├── JavaAiDemoApplication.java     # Spring Boot启动类
│   ├── controller/
│   │   └── InferController.java        # AI推理控制器
│   ├── dto/
│   │   └── PredictionResponse.java     # 预测响应数据结构
│   ├── sdk/                           # SDK相关代码
│   └── service/
│       ├── InferService.java          # 推理服务接口
│       └── impl/
│           └── InferServiceImpl.java  # 推理服务实现
├── src/main/resources/
│   ├── application.yml                # 应用配置
│   ├── images/                        # 示例图片
│   └── models/                        # 预训练模型
│       └── resnet50-v2-7.onnx         # ResNet50模型
├── FRONTEND_INTEGRATION_GUIDE.md      # 前端集成指南
├── SDK_USAGE_GUIDE.md                 # SDK使用指南
├── frontend_sdk.html                  # 前端SDK示例
├── README.md                          # 项目文档
└── pom.xml                            # Maven配置文件
```

## 🧠 技术架构

### 后端技术栈
- Spring Boot 3.5.6: Web应用框架
- ONNX Runtime 1.23.2: 深度学习模型推理引擎
- SpringDoc OpenAPI: API文档生成
- Apache Commons: 工具库
- Lombok: 简化Java代码

### 神经网络模型
- ResNet50-v2-7: 经典的图像分类模型
- 输入尺寸: 224x224 RGB图像
- 输出: 1000个ImageNet类别概率

### API接口

| 接口路径 | 方法 | 描述 |
|---------|------|------|
| `/api/predict` | POST | 上传图片进行图像分类 |
| `/api/health` | GET | 健康检查 |

## 📊 使用指南

### 使用API进行图像分类

#### 请求示例
```bash
curl -X POST "http://localhost:8080/api/predict" \
     -F "image=@path/to/your/image.jpg"
```

#### 响应示例
```json
{
  "classId": 285,
  "score": 0.4010,
  "latencyMs": 270
}
```

### 使用Swagger UI

1. 启动应用后，访问 http://localhost:8080/swagger-ui/index.html
2. 找到"AI 推理服务"标签
3. 点击`/api/predict`接口
4. 点击"Try it out"
5. 选择图片文件，点击"Execute"
6. 查看响应结果

### 前端集成

详细的前端集成指南请参考 [FRONTEND_INTEGRATION_GUIDE.md](FRONTEND_INTEGRATION_GUIDE.md)，包含：
- 原生JavaScript实现
- Axios实现
- React实现
- 错误处理
- 性能优化

### SDK使用

详细的SDK使用指南请参考 [SDK_USAGE_GUIDE.md](SDK_USAGE_GUIDE.md)，包含：
- SDK初始化
- 图片加载
- 推理执行
- 结果处理

## ⚙️ 配置说明

### 应用配置
```yaml
# application.yml
spring:
  application:
    name: java-ai-demo
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

### 模型配置

在 `InferController.java` 中可以配置模型路径：

```java
// 默认使用 ResNet50 模型
try (InputStream modelStream = new ClassPathResource("models/resnet50-v2-7.onnx").getInputStream()) {
    byte[] modelBytes = modelStream.readAllBytes();
    session = env.createSession(modelBytes, new OrtSession.SessionOptions());
}
```

## 🔧 故障排除

### 常见问题

#### 1. 模型加载失败
解决方案：
- 确保模型文件存在于 `src/main/resources/models/` 目录下
- 检查模型文件是否损坏

#### 2. 图片上传失败
解决方案：
- 确保图片大小不超过10MB
- 确保图片格式支持（JPG、PNG等）

#### 3. 启动失败：端口被占用
解决方案：
```bash
# 修改应用端口
java -jar target/java-ai-demo-0.0.1-SNAPSHOT.jar --server.port=8081
```

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
java $JAVA_OPTS -jar java-ai-demo-0.0.1-SNAPSHOT.jar
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
COPY --from=build /app/target/java-ai-demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

构建和运行：
```bash
docker build -t java-ai-demo .
docker run -p 8080:8080 -d java-ai-demo
```

## 📚 API 文档

### 图像分类 API
- **URL**: `/api/predict`
- **方法**: POST
- **参数**: `image` (图片文件)
- **响应**: JSON格式的预测结果

### 健康检查 API
- **URL**: `/api/health`
- **方法**: GET
- **响应**: 服务状态信息

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

- ONNX Runtime - 跨平台机器学习推理引擎
- ResNet50 - 深度学习图像分类模型
- Spring Boot - Java应用框架

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 GitHub Issue
- 邮箱：1936914629@qq.com
- 项目主页：https://github.com/s1936914629/java-ai

提示：首次启动应用会加载模型，可能需要几秒钟时间。

祝您使用愉快！ 🎉
