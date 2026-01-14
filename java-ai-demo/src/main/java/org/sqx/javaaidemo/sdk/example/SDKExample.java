package org.sqx.javaaidemo.sdk.example;

import org.sqx.javaaidemo.sdk.AISDK;
import org.sqx.javaaidemo.sdk.model.PredictionResult;
import org.sqx.javaaidemo.sdk.model.SDKConfig;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

public class SDKExample {
    public static void main(String[] args) {
        AISDK sdk = null;
        try {
            // 1. 获取模型文件流
            InputStream modelStream = SDKExample.class.getClassLoader().getResourceAsStream("models/resnet50-v2-7.onnx");
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
            File imageFile = new File("path/to/your/image.jpg");
            BufferedImage image = ImageIO.read(imageFile);
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
            // 7. 关闭SDK资源
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