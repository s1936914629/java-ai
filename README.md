# Java-AI 项目集合

一个基于 Java 开发的人工智能项目集合，包含多个独立的 AI 应用示例，涵盖计算机视觉、深度学习等领域。

## 📁 项目结构

本项目包含以下子项目：

| 项目名称 | 描述 |
|---------|------|
| [mnist-classification](#mnist-classification) | 基于 Deeplearning4j 和 Spring Boot 构建的 MNIST 手写数字识别系统 |
| [java-ai-demo](#java-ai-demo) | Java AI 应用示例演示项目 |

## 🧠 子项目介绍

### mnist-classification

一个功能完整的手写数字识别系统，支持模型训练、实时手写识别和图像上传识别。

**主要特性：**
- 深度学习模型：使用多层感知机(MLP)神经网络，准确率可达95%以上
- 实时手写识别：提供画板交互，支持手写数字实时识别
- 图像上传识别：支持上传PNG/JPG格式图片进行识别
- 模型训练管理：可视化训练过程，实时查看准确率和损失曲线
- 响应式界面：适配桌面端和移动端，提供良好的用户体验
- 结果可视化：显示置信度分布和图像处理效果

**技术栈：**
- 后端：Spring Boot, Deeplearning4j, ND4J
- 前端：Vue 3, Vite, Bootstrap 5

**详细文档：** [mnist-classification/README.md](mnist-classification/README.md)

### java-ai-demo

Java AI 应用示例演示项目，包含模型加载和使用示例。

**主要特性：**
- 预训练模型加载和使用
- 前端集成示例
- SDK 使用指南

**技术栈：**
- Java
- ONNX 模型支持

**详细文档：** [java-ai-demo/README.md](java-ai-demo/README.md)

## 🚀 快速开始

### 运行 mnist-classification

```bash
# 克隆项目
git clone https://github.com/s1936914629/java-ai.git
cd java-ai/mnist-classification

# 安装前端依赖
cd web
npm install
cd ..

# 启动后端服务
mvn spring-boot:run

# 启动前端应用（新终端）
cd web
npm run dev

# 访问应用
# 前端：http://localhost:5173/
# 后端：http://localhost:8080/
```

### 运行 java-ai-demo

```bash
# 克隆项目
git clone https://github.com/s1936914629/java-ai.git
cd java-ai/java-ai-demo

# 编译项目
mvn clean compile

# 运行项目
mvn exec:java
```

## 🛠️ 开发环境

### 系统要求
- Java: 17 (推荐)
- Node.js: 18.0+ (用于前端开发)
- Maven: 3.6+ (推荐)

### 开发工具
- IDE: IntelliJ IDEA 或 Eclipse
- 浏览器: Chrome/Firefox/Edge

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

- Deeplearning4j - Java 深度学习框架
- Spring Boot - Java 应用框架
- Vue.js - 前端框架
- MNIST Dataset - 手写数字数据集

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 GitHub Issue
- 邮箱：1936914629@qq.com
- 项目主页：https://github.com/s1936914629/java-ai

## 📝 更新日志

- 2024-01-XX: 初始化项目结构，添加 mnist-classification 和 java-ai-demo 子项目
- 2024-01-XX: 完善 mnist-classification 项目的功能和文档

---

感谢您对 Java-AI 项目的关注与支持！ 🎉
