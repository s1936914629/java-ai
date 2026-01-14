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
import java.awt.image.BufferedImage;
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
            BufferedImage processed = preprocessImage(img);

            // 转换为INDArray
            INDArray imageArray = imageToINDArray(processed);

            // 进行预测
            int prediction = mnistModel.predict(imageArray);
            double[] probabilities = mnistModel.getPredictionProbabilities(imageArray);

            result.put("success", true);
            result.put("prediction", prediction);
            result.put("probabilities", probabilities);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
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
            BufferedImage processed = preprocessImage(img);
            INDArray imageArray = imageToINDArray(processed);

            int prediction = mnistModel.predict(imageArray);
            double[] probabilities = mnistModel.getPredictionProbabilities(imageArray);

            result.put("success", true);
            result.put("prediction", prediction);
            result.put("probabilities", probabilities);
            result.put("filename", filename);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    private BufferedImage preprocessImage(BufferedImage img) {
        // 转换为灰度图
        BufferedImage grayImage = new BufferedImage(
                img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        // 调整大小为28x28
        BufferedImage resized = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(grayImage, 0, 0, 28, 28, null);
        g2d.dispose();

        return resized;
    }

    private INDArray imageToINDArray(BufferedImage img) {
        float[] pixels = new float[28 * 28];

        // MNIST数据集是黑底白字，但我们的画板是白底黑字
        // 需要反色并归一化
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int rgb = img.getRGB(x, y);
                int gray = rgb & 0xFF; // 获取灰度值
                // 反色：黑底白字 -> 白底黑字
                float normalized = (255 - gray) / 255.0f;
                pixels[y * 28 + x] = normalized;
            }
        }

        return Nd4j.create(pixels);
    }
}