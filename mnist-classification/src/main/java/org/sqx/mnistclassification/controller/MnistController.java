package org.sqx.mnistclassification.controller;

import org.apache.commons.io.FileUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.sqx.mnistclassification.service.TrainingService;
import org.sqx.mnistclassification.model.MnistModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Controller
public class MnistController {

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private MnistModel mnistModel;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("trained", mnistModel.isTrained());
        return "index";
    }

    @GetMapping("/train")
    public String trainPage(Model model) {
        model.addAllAttributes(trainingService.getModelStatus());
        return "train";
    }

    @PostMapping("/api/train")
    @ResponseBody
    public Map<String, Object> train(@RequestParam(defaultValue = "5") int epochs) {
        return trainingService.trainModel(epochs);
    }

    @GetMapping("/predict")
    public String predictPage(Model model) {
        model.addAttribute("trained", mnistModel.isTrained());
        return "predict";
    }

    @PostMapping("/api/predict")
    @ResponseBody
    public Map<String, Object> predict(@RequestParam("image") String imageData) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 解码Base64图像
            String base64Data = imageData.split(",")[1];
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // 转换为灰度图像并调整大小
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));

            // 使用增强的图像预处理
            BufferedImage processed = enhancedPreprocessImage(img);

            // 转换为INDArray - 使用改进的转换方法
            INDArray imageArray = enhancedImageToINDArray(processed);

            // 进行预测 - 使用新的详细预测方法
            Map<String, Object> predictionResult = (Map<String, Object>) mnistModel.predictWithDetail(imageArray);

            result.put("success", true);
            result.putAll(predictionResult);

            // 添加额外的建议
            int prediction = (int) predictionResult.get("prediction");
            double confidence = (double) predictionResult.get("confidence");

            if (confidence < 0.7) {
                result.put("warning", "置信度较低，请尝试重新书写");
            }

            // 针对0、6、9的特殊处理建议
            if (prediction == 0 || prediction == 6 || prediction == 9) {
                result.put("hint", getNumberHint(prediction));
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    private BufferedImage enhancedPreprocessImage(BufferedImage img) {
        // 1. 转换为灰度图
        BufferedImage grayImage = new BufferedImage(
                img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        // 2. 应用高斯模糊去噪
        grayImage = applyGaussianBlur(grayImage, 1.0f);

        // 3. 调整大小为28x28（使用高质量插值）
        BufferedImage resized = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = resized.createGraphics();

        // 设置高质量渲染参数
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(grayImage, 0, 0, 28, 28, null);
        g2d.dispose();

        // 4. 应用阈值处理（二值化）
        resized = applyThreshold(resized);

        // 5. 形态学操作 - 闭运算（先膨胀后腐蚀）填充小孔洞
        resized = applyMorphology(resized, 1);

        // 6. 中心化和缩放
        resized = centerAndNormalize(resized);

        return resized;
    }

    private BufferedImage applyGaussianBlur(BufferedImage src, float radius) {
        if (radius <= 0) return src;

        int size = (int) (radius * 2 + 1);
        float[] data = new float[size * size];
        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;

        // 生成高斯核
        for (int y = -size/2, i = 0; y <= size/2; y++) {
            for (int x = -size/2; x <= size/2; x++, i++) {
                float distance = x*x + y*y;
                data[i] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
                total += data[i];
            }
        }

        // 归一化
        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }

        Kernel kernel = new Kernel(size, size, data);
        BufferedImageOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(src, null);
    }

    private BufferedImage applyThreshold(BufferedImage src) {
        // 计算自适应阈值（使用Otsu算法）
        int threshold = calculateOtsuThreshold(src);
        BufferedImage result = new BufferedImage(
                src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int rgb = src.getRGB(x, y);
                int gray = rgb & 0xFF;
                int newGray = (gray > threshold) ? 255 : 0;
                result.setRGB(x, y, (newGray << 16) | (newGray << 8) | newGray);
            }
        }

        return result;
    }

    private int calculateOtsuThreshold(BufferedImage src) {
        int[] histogram = new int[256];
        int totalPixels = src.getWidth() * src.getHeight();

        // 计算直方图
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int rgb = src.getRGB(x, y);
                int gray = rgb & 0xFF;
                histogram[gray]++;
            }
        }

        // 计算Otsu阈值
        double maxVariance = 0;
        int optimalThreshold = 128;

        for (int t = 0; t < 256; t++) {
            // 计算前景和背景的像素数
            int foregroundPixels = 0;
            int backgroundPixels = 0;
            for (int i = 0; i < 256; i++) {
                if (i > t) foregroundPixels += histogram[i];
                else backgroundPixels += histogram[i];
            }

            if (foregroundPixels == 0 || backgroundPixels == 0) continue;

            // 计算前景和背景的均值
            double foregroundMean = 0;
            double backgroundMean = 0;
            for (int i = 0; i < 256; i++) {
                if (i > t) foregroundMean += i * histogram[i];
                else backgroundMean += i * histogram[i];
            }
            foregroundMean /= foregroundPixels;
            backgroundMean /= backgroundPixels;

            // 计算类间方差
            double variance = ((double)foregroundPixels / totalPixels) * 
                             ((double)backgroundPixels / totalPixels) * 
                             Math.pow(foregroundMean - backgroundMean, 2);

            if (variance > maxVariance) {
                maxVariance = variance;
                optimalThreshold = t;
            }
        }

        return optimalThreshold;
    }

    private BufferedImage applyMorphology(BufferedImage src, int iterations) {
        BufferedImage result = src;

        for (int i = 0; i < iterations; i++) {
            // 简单的膨胀操作（扩大白色区域）
            result = dilate(result);
            // 简单的腐蚀操作（缩小白色区域）
            result = erode(result);
        }

        return result;
    }

    private BufferedImage dilate(BufferedImage src) {
        BufferedImage result = new BufferedImage(
                src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        int[][] kernel = {{0,1,0},{1,1,1},{0,1,0}};

        for (int y = 1; y < src.getHeight()-1; y++) {
            for (int x = 1; x < src.getWidth()-1; x++) {
                int maxGray = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        if (kernel[ky+1][kx+1] == 1) {
                            int rgb = src.getRGB(x+kx, y+ky);
                            int gray = rgb & 0xFF;
                            if (gray > maxGray) maxGray = gray;
                        }
                    }
                }
                result.setRGB(x, y, (maxGray << 16) | (maxGray << 8) | maxGray);
            }
        }

        return result;
    }

    private BufferedImage erode(BufferedImage src) {
        BufferedImage result = new BufferedImage(
                src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        int[][] kernel = {{0,1,0},{1,1,1},{0,1,0}};

        for (int y = 1; y < src.getHeight()-1; y++) {
            for (int x = 1; x < src.getWidth()-1; x++) {
                int minGray = 255;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        if (kernel[ky+1][kx+1] == 1) {
                            int rgb = src.getRGB(x+kx, y+ky);
                            int gray = rgb & 0xFF;
                            if (gray < minGray) minGray = gray;
                        }
                    }
                }
                result.setRGB(x, y, (minGray << 16) | (minGray << 8) | minGray);
            }
        }

        return result;
    }

    private BufferedImage centerAndNormalize(BufferedImage src) {
        // 计算质心
        double sumX = 0, sumY = 0, sum = 0;

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int rgb = src.getRGB(x, y);
                int gray = rgb & 0xFF;
                double weight = 255 - gray; // 深色像素权重高
                sumX += x * weight;
                sumY += y * weight;
                sum += weight;
            }
        }

        if (sum == 0) return src;

        double centerX = sumX / sum;
        double centerY = sumY / sum;

        // 计算平移量（将质心移动到图像中心）
        double translateX = src.getWidth() / 2.0 - centerX;
        double translateY = src.getHeight() / 2.0 - centerY;

        // 应用平移变换
        AffineTransform tx = AffineTransform.getTranslateInstance(translateX, translateY);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC);
        BufferedImage centered = op.filter(src, null);

        return centered;
    }

    private INDArray enhancedImageToINDArray(BufferedImage img) {
        float[] pixels = new float[28 * 28];

        // 计算图像的平均像素值和标准差
        double sum = 0;
        double sumSq = 0;

        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int rgb = img.getRGB(x, y);
                int gray = rgb & 0xFF;
                // 反色处理：黑底白字 -> 白底黑字（与MNIST训练数据一致）
                float value = (255 - gray) / 255.0f;
                pixels[y * 28 + x] = value;
                sum += value;
                sumSq += value * value;
            }
        }

        // 标准化（零均值，单位方差）
        double mean = sum / (28 * 28);
        double std = Math.sqrt(sumSq / (28 * 28) - mean * mean);

        if (std > 0) {
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = (float) ((pixels[i] - mean) / std);
            }
        }

        return Nd4j.create(pixels);
    }

    private String getNumberHint(int number) {
        switch (number) {
            case 0:
                return "提示：书写0时尽量保持圆形闭合，避免过小或过大";
            case 6:
                return "提示：书写6时注意上半部分的小圈和向下的弧线";
            case 9:
                return "提示：书写9时注意上半部分的圈和向下的直线";
            default:
                return "";
        }
    }

    @GetMapping("/api/model/status")
    @ResponseBody
    public Map<String, Object> getModelStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("trained", mnistModel.isTrained());
        return status;
    }

    @PostMapping("/api/upload")
    @ResponseBody
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 保存上传的文件
            String uploadDir = "uploads/";
            new File(uploadDir).mkdirs();
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File destination = new File(uploadDir + filename);
            FileUtils.writeByteArrayToFile(destination, file.getBytes());

            // 处理图像并进行预测
            BufferedImage img = ImageIO.read(destination);
            BufferedImage processed = enhancedPreprocessImage(img);
            INDArray imageArray = enhancedImageToINDArray(processed);

            // 使用改进的预测方法获取详细结果
            Map<String, Object> predictionResult = (Map<String, Object>) mnistModel.predictWithDetail(imageArray);
            
            result.put("success", true);
            result.put("prediction", predictionResult.get("prediction"));
            result.put("probabilities", predictionResult.get("probabilities"));
            result.put("confidence", predictionResult.get("confidence"));
            result.put("filename", filename);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}