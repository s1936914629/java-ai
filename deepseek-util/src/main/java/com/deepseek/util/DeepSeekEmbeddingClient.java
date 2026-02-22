package com.deepseek.util;

import com.deepseek.config.DeepSeekConfig;
import com.deepseek.llm.EmbeddingClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DeepSeek Embedding 客户端实现
 * 用于与 DeepSeek Embedding API 进行交互，生成文本嵌入向量
 */
@Component
public class DeepSeekEmbeddingClient extends AbstractDeepSeekClient implements EmbeddingClient {

    /**
     * 构造方法
     * 
     * @param deepSeekConfig DeepSeek 配置
     * @param objectMapper 对象映射器
     */
    public DeepSeekEmbeddingClient(DeepSeekConfig deepSeekConfig, ObjectMapper objectMapper) {
        super(deepSeekConfig, objectMapper);
        logger.info("DeepSeekEmbeddingClient 初始化完成");
    }

    /**
     * 生成文本嵌入
     * 使用默认模型生成单个文本的嵌入向量
     */
    @Override
    public List<Double> embed(String text) {
        return embed(deepSeekConfig.getEmbeddingModel(), text);
    }

    /**
     * 生成文本嵌入
     * 使用指定模型生成单个文本的嵌入向量
     */
    @Override
    public List<Double> embed(String model, String text) {
        List<String> texts = Collections.singletonList(text);
        List<List<Double>> embeddings = embedBatch(model, texts);
        return embeddings.isEmpty() ? new ArrayList<>() : embeddings.get(0);
    }

    /**
     * 批量生成文本嵌入
     * 使用默认模型批量生成多个文本的嵌入向量
     */
    @Override
    public List<List<Double>> embedBatch(List<String> texts) {
        return embedBatch(deepSeekConfig.getEmbeddingModel(), texts);
    }

    /**
     * 批量生成文本嵌入
     * 使用指定模型批量生成多个文本的嵌入向量
     */
    @Override
    public List<List<Double>> embedBatch(String model, List<String> texts) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        
        ArrayNode inputArray = objectMapper.createArrayNode();
        for (String text : texts) {
            inputArray.add(text);
        }
        requestBody.set("input", inputArray);
        
        HttpEntity<ObjectNode> httpEntity = new HttpEntity<>(requestBody, createHeaders());
        return sendEmbeddingRequest(httpEntity);
    }

    /**
     * 获取默认模型名称
     */
    @Override
    public String getDefaultModel() {
        return deepSeekConfig.getEmbeddingModel();
    }

    /**
     * 获取嵌入向量维度
     */
    @Override
    public int getEmbeddingDimension() {
        // DeepSeek 嵌入模型的默认维度
        return 1536;
    }

    /**
     * 发送嵌入请求
     * 发送请求到 DeepSeek Embedding API 并处理响应
     */
    private List<List<Double>> sendEmbeddingRequest(HttpEntity<ObjectNode> httpEntity) {
        String url = deepSeekConfig.getBaseUrl() + "/embeddings";
        return sendRequestWithRetry(url, httpEntity, ObjectNode.class, "发送嵌入请求到 DeepSeek API", 
            this::parseEmbeddingResponse);
    }

    /**
     * 解析嵌入响应
     * 从 API 响应中提取嵌入向量
     */
    private List<List<Double>> parseEmbeddingResponse(ObjectNode response) {
        List<List<Double>> embeddings = new ArrayList<>();
        
        if (response != null && response.has("data")) {
            ArrayNode dataArray = (ArrayNode) response.get("data");
            for (int i = 0; i < dataArray.size(); i++) {
                ObjectNode item = (ObjectNode) dataArray.get(i);
                if (item.has("embedding")) {
                    ArrayNode embeddingArray = (ArrayNode) item.get("embedding");
                    List<Double> embedding = new ArrayList<>();
                    for (int j = 0; j < embeddingArray.size(); j++) {
                        embedding.add(embeddingArray.get(j).asDouble());
                    }
                    embeddings.add(embedding);
                }
            }
        }
        
        logger.debug("解析嵌入响应完成，获取到 {} 个嵌入向量", embeddings.size());
        return embeddings;
    }
}