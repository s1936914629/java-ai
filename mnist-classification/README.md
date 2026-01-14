# MNIST 手写数字识别系统

基于 Deeplearning4j 和 Spring Boot 构建的智能手写数字识别系统，支持模型训练、实时手写识别和图像上传识别。

## 🌟 特性
- 深度学习模型：使用多层感知机(MLP)神经网络，准确率可达95%以上
- 实时手写识别：提供画板交互，支持手写数字实时识别
- 图像上传识别：支持上传PNG/JPG格式图片进行识别
- 模型训练管理：可视化训练过程，实时查看准确率和损失曲线
- 响应式界面：适配桌面端和移动端，提供良好的用户体验
- 结果可视化：显示置信度分布和图像处理效果



## 📋 系统要求
- Java: 21
- Maven: 3.6+ (推荐使用Maven进行构建)
- 内存: 至少2GB可用内存
- 磁盘空间: 至少100MB可用空间（用于存储MNIST数据集）

## 🚀 快速开始

1. 克隆项目
   ```bash
   git clone https://github.com/yourusername/mnist-classification.git
   cd mnist-classification
   ```

2. 构建项目
   ```bash
   mvn clean package
   ```

3. 运行应用
   ```bash
   # 方式1：使用Maven运行
   mvn spring-boot:run
   
   # 方式2：运行打包后的jar文件
   java -jar target/mnist-classification-0.0.1-SNAPSHOT.jar
   ```

4. 访问应用
   应用启动后，在浏览器中访问：
   - 主页: http://localhost:8080
   - 训练页面: http://localhost:8080/train
   - 识别页面: http://localhost:8080/predict

## 📁 项目结构

```text
mnist-classification/
├── src/main/java/org/sqx/mnistclassification/
│   ├── MnistClassificationApplication.java     # Spring Boot启动类
│   ├── controller/
│   │   └── MnistController.java                # Web控制器
│   ├── model/
│   │   └── MnistModel.java                     # 神经网络模型
│   └── service/
│       └── TrainingService.java                # 训练服务
├── src/main/resources/
│   ├── templates/                              # Thymeleaf模板
│   │   ├── index.html                          # 首页
│   │   ├── train.html                          # 训练页面
│   │   └── predict.html                        # 识别页面
│   └── application.properties                  # 应用配置
├── src/test/                                   # 测试代码目录
├── models/                                     # 模型保存目录（运行时生成）
├── uploads/                                    # 上传文件目录（运行时生成）
├── README.md                                   # 项目文档
├── pom.xml                                     # Maven配置文件
└── mvnw/mvnw.cmd                               # Maven包装器
```
## 🧠 技术架构

### 后端技术栈
- Spring Boot 4.0.1: Web应用框架
- Deeplearning4j 1.0.0-M2.1: 深度学习框架
- ND4J: 数值计算库
- Thymeleaf: 模板引擎
- Apache Commons: 工具库

### 前端技术栈
- Bootstrap 5: 响应式UI框架
- HTML5 Canvas: 画板功能
- JavaScript: 前端交互逻辑
- Fetch API: 异步请求

### 神经网络结构
```java
输入层: 784个神经元 (28×28像素)
隐藏层1: 128个神经元 (ReLU激活函数)
隐藏层2: 64个神经元 (ReLU激活函数)
输出层: 10个神经元 (Softmax激活函数)
损失函数: 负对数似然
优化器: Adam (学习率0.001)
```
## 📊 使用指南

### 1. 首次使用
- 启动应用后，系统会自动创建新模型
- 首次训练需要下载MNIST数据集（约11MB）
- 数据集会自动保存到 ~/.deeplearning4j/data/mnist/ 目录

### 2. 训练模型
1. 访问训练页面: http://localhost:8080/train
2. 设置训练轮数（建议5-10轮）
3. 点击"开始训练"按钮
4. 观察训练日志和进度条
5. 训练完成后查看准确率和其他评估指标

### 3. 手写识别
1. 访问识别页面: http://localhost:8080/predict
2. 在画板上手写数字（0-9）
3. 可以调整画笔粗细
4. 点击"识别数字"按钮
5. 查看识别结果和置信度分布

### 4. 上传识别
1. 在识别页面点击"选择文件"
2. 上传包含数字的图片（PNG/JPG格式）
3. 系统会自动处理并识别
4. 查看识别结果

## ⚙️ 配置说明

### 应用配置
```properties
# 应用端口
server.port=8080

# 文件上传大小限制
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# 模板缓存（开发时设置为false）
spring.thymeleaf.cache=false
```

### 神经网络配置
可以通过修改 MnistModel.java 调整网络参数：
```java
// 训练轮数
int epochs = 10;

// 批处理大小
int batchSize = 64;

// 学习率
double learningRate = 0.001;

// 网络层结构
// 可以修改nOut值调整每层神经元数量
```
## 📈 性能优化

### 提升训练速度
- 调整批处理大小（增大可加快训练）
- 减少训练轮数（快速验证模型）
- 简化网络结构（减少层数或神经元数量）

### 提升识别准确率
- 增加训练轮数（10-20轮）
- 增加网络深度（添加更多隐藏层）
- 使用更复杂的激活函数
- 添加正则化防止过拟合

### 内存优化
如果遇到内存不足的问题，可以：
```bash
# 增加JVM堆内存
java -Xmx4g -jar target/mnist-classification-0.0.1-SNAPSHOT.jar

# 或减小批处理大小
# 在MnistModel.java中修改batchSize值
```
## 🔧 故障排除

### 常见问题

#### 1. 训练时内存不足
解决方案：
```bash
# 增加JVM内存
export JAVA_OPTS="-Xmx4g -Xms2g"
mvn spring-boot:run
```

#### 2. MNIST数据集下载失败
解决方案：
- 手动下载数据集：
  - 访问：http://yann.lecun.com/exdb/mnist/
  - 下载4个.gz文件
  - 放置到 ~/.deeplearning4j/data/mnist/ 目录
  - 文件列表：
    ```
    train-images-idx3-ubyte.gz
    train-labels-idx1-ubyte.gz
    t10k-images-idx3-ubyte.gz
    t10k-labels-idx1-ubyte.gz
    ```

#### 3. 模型识别不准确
解决方案：
- 重新训练模型，增加训练轮数
- 在画板上写更清晰的数字
- 确保上传的图片背景干净

#### 4. 启动失败：端口被占用
解决方案：
```bash
# 修改应用端口
java -jar target/mnist-classification-0.0.1-SNAPSHOT.jar --server.port=8081

# 或结束占用端口的进程
# Linux/Mac:
lsof -ti:8080 | xargs kill -9
# Windows:
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### 日志查看
应用日志输出到控制台，主要包含：
- 模型加载/创建信息
- 训练进度和评估结果
- 识别请求处理信息
- 错误和异常信息

## 🚢 部署指南

### 生产环境部署

1. 打包应用：
   ```bash
   mvn clean package -DskipTests
   ```

2. 创建启动脚本：
   ```bash
   #!/bin/bash
   # run.sh
   export JAVA_HOME=/path/to/java21
   export JAVA_OPTS="-Xmx2g -Xms1g -Dspring.profiles.active=prod"
   java $JAVA_OPTS -jar mnist-classification-0.0.1-SNAPSHOT.jar
   ```

3. 配置生产环境：
   ```properties
   # application-prod.properties
   spring.thymeleaf.cache=true
   logging.level.org.springframework=WARN
   logging.level.org.sqx=INFO
   ```

### Docker 部署

```dockerfile
# Dockerfile
FROM openjdk:21-jre-slim
WORKDIR /app
COPY target/mnist-classification-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

构建和运行：
```bash
docker build -t mnist-classification .
docker run -p 8080:8080 -d mnist-classification
```
## 📚 API 文档

### 训练API
- **URL**: `/api/train`
- **方法**: POST
- **参数**: `epochs` (训练轮数，默认5)
- **响应**: JSON格式的训练结果，包含准确率、训练时间等信息

### 识别API
- **URL**: `/api/predict`
- **方法**: POST
- **参数**: `image` (Base64编码的图像数据)
- **响应**: JSON格式的识别结果，包含预测数字和置信度分布

### 上传API
- **URL**: `/api/upload`
- **方法**: POST
- **参数**: `file` (图片文件)
- **响应**: JSON格式的识别结果，包含预测数字和置信度分布

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

- Deeplearning4j - Java深度学习框架
- Spring Boot - Java应用框架
- MNIST Dataset - 手写数字数据集
- Bootstrap - 前端框架

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 GitHub Issue
- 邮箱：1936914629@qq.com
- 项目主页：https://github.com/s1936914629/java-ai

提示：首次训练需要下载MNIST数据集，请确保网络连接正常。训练时间取决于硬件配置，通常需要几分钟到十几分钟。

祝您使用愉快！ 🎉