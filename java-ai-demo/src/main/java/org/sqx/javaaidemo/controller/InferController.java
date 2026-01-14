package org.sqx.javaaidemo.controller;

import ai.onnxruntime.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;
import org.sqx.javaaidemo.dto.PredictionResponse;
import org.sqx.javaaidemo.service.InferService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/api")
@Tag(name = "AI 推理服务", description = "基于 ResNet50 的图像分类推理接口")
public class InferController {
	
	@Resource
	private InferService inferService;
	
	private OrtEnvironment env;
	private OrtSession session;
	
	@PostConstruct
	public void init () throws Exception {
		env = OrtEnvironment.getEnvironment();
		// try (InputStream modelStream = new ClassPathResource("models/resnet50-v2-7.onnx").getInputStream()) {
		try (InputStream modelStream = new ClassPathResource("models/iris_logreg.onnx").getInputStream()) {
			byte[] modelBytes = modelStream.readAllBytes();
			session = env.createSession(modelBytes, new OrtSession.SessionOptions());
		}
		System.out.println("模型加载完成！");
	}
	
	@PostMapping("/predict")
	@Operation(summary = "上传图片进行图像分类", description = "接收一张图片，使用 ResNet50 模型进行推理，返回预测类别 ID、置信度和延迟时间")
	@ApiResponse(responseCode = "200", description = "推理成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PredictionResponse.class)))
	public PredictionResponse predict (@RequestParam("image") MultipartFile file) throws Exception {
		long start = System.currentTimeMillis();
		BufferedImage img = ImageIO.read(file.getInputStream());
		if (img == null) {
			throw new RuntimeException("无法读取图片");
		}
		
		float[] inputData = inferService.preprocessImage(img);
		float[][][][] inputArray = new float[1][3][224][224];
		for (int c = 0; c < 3; c++) {
			for (int h = 0; h < 224; h++) {
				for (int w = 0; w < 224; w++) {
					inputArray[0][c][h][w] = inputData[c * 224 * 224 + h * 224 + w];
				}
			}
		}
		
		try (OnnxTensor tensor = OnnxTensor.createTensor(env, inputArray)) {
			Map<String, OnnxTensor> inputs = Collections.singletonMap("data", tensor);
			try (OrtSession.Result results = session.run(inputs)) {
				float[][] output = (float[][]) results.get(0).getValue();
				float[] logits = output[0];
				
				// ========== 关键修改：添加 softmax ==========
				float[] probabilities = inferService.softmax(logits);
				
				int maxIndex = 0;
				float maxProb = probabilities[0];
				for (int i = 1; i < probabilities.length; i++) {
					if (probabilities[i] > maxProb) {
						maxProb = probabilities[i];
						maxIndex = i;
					}
				}
				// ============================================
				long end = System.currentTimeMillis();
				PredictionResponse result = new PredictionResponse();
				result.setClassId(maxIndex);
				result.setScore(maxProb);
				result.setLatencyMs(end - start);
				return result;
			}
		}
	}
	
	@GetMapping("/health")
	@Operation(summary = "健康检查", description = "检查服务是否正常运行")
	public String health () {
		return "Java AI 推理服务运行正常！";
	}
	
}