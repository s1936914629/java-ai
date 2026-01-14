# AI SDK 使用指南

本文档将指导您如何在自己的项目中使用AI SDK进行图像分类推理。

## 1. 项目依赖配置

### Maven项目
在您的`pom.xml`文件中添加以下依赖：

```xml
<dependencies>
    <!-- ONNX Runtime 依赖 -->
    <dependency>
        <groupId>com.microsoft.onnxruntime</groupId>
        <artifactId>onnxruntime</artifactId>
        <version>1.16.0</version>
    </dependency>
    
    <!-- Java AWT 用于图像处理（JDK自带，无需额外添加） -->
</dependencies>
```

### Gradle项目
在您的`build.gradle`文件中添加以下依赖：

```groovy
dependencies {
    // ONNX Runtime 依赖
    implementation 'com.microsoft.onnxruntime:onnxruntime:1.16.0'
}
```

## 2. SDK使用步骤

### 2.1 导入SDK类

```java
import org.sqx.javaaidemo.sdk.AISDK;
import org.sqx.javaaidemo.sdk.model.PredictionResult;
import org.sqx.javaaidemo.sdk.model.SDKConfig;
import org.sqx.javaaidemo.sdk.utils.ImagePreprocessor;
```

### 2.2 初始化SDK

```java
import java.io.InputStream;

// 获取模型文件流
InputStream modelStream = YourClassName.class.getClassLoader().getResourceAsStream("models/resnet50-v2-7.onnx");
if (modelStream == null) {
    throw new RuntimeException("模型文件未找到");
}

// 创建配置
SDKConfig config = new SDKConfig.Builder(modelStream)
        .inputSize(224, 224) // 设置输入尺寸
        .useGPU(false) // 是否使用GPU加速
        .build();

// 初始化SDK
AISDK sdk = AISDK.initialize(config);
System.out.println("SDK初始化完成");
```

### 2.3 加载图片

```java
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

// 从文件加载图片
File imageFile = new File("path/to/your/image.jpg");
BufferedImage image = ImageIO.read(imageFile);
if (image == null) {
    throw new RuntimeException("无法读取图片文件");
}

// 或者从InputStream加载图片
// InputStream imageStream = YourClassName.class.getClassLoader().getResourceAsStream("images/test.jpg");
// BufferedImage image = ImageIO.read(imageStream);
```

### 2.4 执行推理

```java
// 进行推理
PredictionResult result = sdk.predict(image);
```

### 2.5 处理结果

```java
// 输出推理结果
System.out.println("推理结果：");
System.out.println("类别ID: " + result.getClassId());
System.out.println("置信度: " + result.getScore());
System.out.println("推理延迟: " + result.getLatencyMs() + "ms");

// 根据类别ID获取类别名称（需要您自己提供类别映射表）
String[] classNames = loadClassNames(); // 加载ImageNet类别名称
if (result.getClassId() >= 0 && result.getClassId() < classNames.length) {
    String className = classNames[result.getClassId()];
    System.out.println("类别名称: " + className);
}
```

### 2.6 释放资源

```java
// 使用完毕后释放资源
try {
    sdk.close();
    System.out.println("SDK资源已释放");
} catch (Exception e) {
    e.printStackTrace();
}
```

## 3. 完整示例代码

```java
import org.sqx.javaaidemo.sdk.AISDK;
import org.sqx.javaaidemo.sdk.model.PredictionResult;
import org.sqx.javaaidemo.sdk.model.SDKConfig;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

public class SDKUsageExample {
    public static void main(String[] args) {
        AISDK sdk = null;
        try {
            // 1. 获取模型文件流
            InputStream modelStream = SDKUsageExample.class.getClassLoader().getResourceAsStream("models/resnet50-v2-7.onnx");
            if (modelStream == null) {
                throw new RuntimeException("模型文件未找到");
            }

            // 2. 创建配置
            SDKConfig config = new SDKConfig.Builder(modelStream)
                    .inputSize(224, 224)
                    .useGPU(false)
                    .build();

            // 3. 初始化SDK
            sdk = AISDK.initialize(config);
            System.out.println("SDK初始化完成");

            // 4. 读取图片
            // 注意：这里使用的是项目resources/images目录下的cat.jpg图片
            InputStream imageStream = SDKUsageExample.class.getClassLoader().getResourceAsStream("images/cat.jpg");
            if (imageStream == null) {
                throw new RuntimeException("测试图片未找到");
            }
            BufferedImage image = ImageIO.read(imageStream);
            if (image == null) {
                throw new RuntimeException("无法读取图片文件");
            }

            // 5. 进行推理
            PredictionResult result = sdk.predict(image);
            
            // 6. 处理结果
            System.out.println("推理结果：");
            System.out.println("类别ID: " + result.getClassId());
            System.out.println("置信度: " + result.getScore());
            System.out.println("推理延迟: " + result.getLatencyMs() + "ms");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 7. 释放资源
            if (sdk != null) {
                try {
                    sdk.close();
                    System.out.println("SDK资源已释放");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

## 4. 注意事项

1. **模型文件路径**：确保模型文件在项目的资源目录下，或者提供正确的文件路径
2. **图片格式**：支持JPEG、PNG等常见图片格式
3. **线程安全**：SDK已实现线程安全，可以在多线程环境下使用
4. **资源管理**：使用完毕后请调用`close()`方法释放资源
5. **异常处理**：建议添加适当的异常处理机制

## 5. 常见问题

### 5.1 模型文件未找到

```
RuntimeException: 模型文件未找到
```

**解决方法**：确保模型文件路径正确，且已包含在项目的资源目录中。

### 5.2 图片无法读取

```
RuntimeException: 无法读取图片文件
```

**解决方法**：确保图片文件存在，且格式正确。

### 5.3 ONNX Runtime错误

```
OnnxRuntimeException: [ONNXRuntimeError] ...
```

**解决方法**：检查ONNX Runtime版本是否兼容，模型文件是否损坏。

## 6. 扩展功能

### 6.1 自定义图片预处理

```java
import org.sqx.javaaidemo.sdk.utils.ImagePreprocessor;

ImagePreprocessor preprocessor = new ImagePreprocessor();
// 使用自定义尺寸进行预处理
float[] inputData = preprocessor.preprocess(image, 256, 256);
```

### 6.2 GPU加速

```java
// 创建配置时启用GPU加速
SDKConfig config = new SDKConfig.Builder(modelStream)
        .inputSize(224, 224)
        .useGPU(true) // 启用GPU加速
        .build();
```

> 注意：使用GPU加速需要安装对应版本的CUDA和cuDNN，并确保ONNX Runtime支持GPU。

## 7. 性能优化建议

1. **模型缓存**：初始化一次SDK，多次使用，避免重复加载模型
2. **批量处理**：如果需要处理多张图片，建议使用批量推理（当前版本暂不支持，后续可扩展）
3. **线程池**：在多线程环境下使用线程池管理并发推理请求
4. **图片预处理优化**：根据实际需求调整预处理参数

## 8. 版本信息

- SDK版本：0.0.1-SNAPSHOT
- ONNX Runtime版本：1.16.0
- 支持模型：ResNet50-v2-7（ONNX格式）
- 输入尺寸：224x224（RGB）
