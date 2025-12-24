package org.sqx.javaaidemo.service.impl;

import org.springframework.stereotype.Service;
import org.sqx.javaaidemo.service.InferService;

import java.awt.*;
import java.awt.image.BufferedImage;

@Service
public class InferServiceImpl implements InferService {
	
	// 添加 softmax 函数
	public float[] softmax(float[] logits) {
		// 1. 找到最大值（防止计算 exp 时溢出）
		float max = logits[0];
		for (float v : logits) {
			if (v > max) max = v;
		}
		
		// 2. 计算 exp(x - max)
		float sum = 0.0f;
		float[] exps = new float[logits.length];
		for (int i = 0; i < logits.length; i++) {
			exps[i] = (float) Math.exp(logits[i] - max);
			sum += exps[i];
		}
		
		// 3. 归一化
		for (int i = 0; i < exps.length; i++) {
			exps[i] /= sum;
		}
		return exps;
	}
	
	// 图片预处理
	public float[] preprocessImage (BufferedImage img) {
		int width = 224;
		int height = 224;
		Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		resized.getGraphics().drawImage(scaled, 0, 0, null);
		
		float[] input = new float[3 * width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb = resized.getRGB(x, y);
				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = rgb & 0xFF;
				input[y * width + x] = (r / 255.0f - 0.485f) / 0.229f;
				input[width * height + y * width + x] = (g / 255.0f - 0.456f) / 0.224f;
				input[2 * width * height + y * width + x] = (b / 255.0f - 0.406f) / 0.225f;
			}
		}
		return input;
	}
}
