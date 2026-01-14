package org.sqx.javaaidemo.sdk.utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePreprocessor {
    private static final int DEFAULT_WIDTH = 224;
    private static final int DEFAULT_HEIGHT = 224;
    private static final float[] MEANS = {0.485f, 0.456f, 0.406f};
    private static final float[] STDS = {0.229f, 0.224f, 0.225f};

    public float[] preprocess(BufferedImage img) {
        return preprocess(img, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public float[] preprocess(BufferedImage img, int width, int height) {
        // 调整图片大小
        BufferedImage resized = resizeImage(img, width, height);
        
        // 预处理并返回float数组
        return normalize(resized, width, height);
    }

    private BufferedImage resizeImage(BufferedImage img, int width, int height) {
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    private float[] normalize(BufferedImage img, int width, int height) {
        float[] input = new float[3 * width * height];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // 归一化并应用均值和标准差
                input[y * width + x] = ((r / 255.0f - MEANS[0]) / STDS[0]);
                input[width * height + y * width + x] = ((g / 255.0f - MEANS[1]) / STDS[1]);
                input[2 * width * height + y * width + x] = ((b / 255.0f - MEANS[2]) / STDS[2]);
            }
        }
        
        return input;
    }
}