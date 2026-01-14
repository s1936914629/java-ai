package org.sqx.javaaidemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "图像分类推理结果响应")
public class PredictionResponse {
	
	@Schema(description = "预测的类别 ID（ImageNet 类别索引）", example = "285", requiredMode = Schema.RequiredMode.REQUIRED)
	public int classId;
	
	@Schema(description = "预测置信度分数（Softmax 输出）", example = "0.9342", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0.0", maximum = "1.0")
	public float score;
	
	@Schema(description = "端到端推理延迟（毫秒）", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
	public long latencyMs;
}