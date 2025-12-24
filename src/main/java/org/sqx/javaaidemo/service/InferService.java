package org.sqx.javaaidemo.service;

import java.awt.image.BufferedImage;

public interface InferService {
	/**
	 * softmax 函数
	 * @param logits
	 * @return
	 */
	float[] softmax(float[] logits);
	
	/**
	 * 图片预处理
	 * @param img
	 * @return
	 */
	float[] preprocessImage (BufferedImage img);
}
