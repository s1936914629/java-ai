package com.deepseek.llm;

import java.util.List;

/**
 * Embedding 客户端抽象接口
 * <p>
 * 定义通用的文本嵌入操作方法，支持不同模型提供商的实现
 */
public interface EmbeddingClient {

    /**
     * 生成文本嵌入
     * <p>
     * 使用默认模型生成单个文本的嵌入向量
     * 
     * @param text 文本内容
     * @return 嵌入向量
     */
    List<Double> embed(String text);

    /**
     * 生成文本嵌入
     * <p>
     * 使用指定模型生成单个文本的嵌入向量
     * 
     * @param model 模型名称
     * @param text 文本内容
     * @return 嵌入向量
     */
    List<Double> embed(String model, String text);

    /**
     * 批量生成文本嵌入
     * <p>
     * 使用默认模型批量生成多个文本的嵌入向量
     * 
     * @param texts 文本列表
     * @return 嵌入向量列表
     */
    List<List<Double>> embedBatch(List<String> texts);

    /**
     * 批量生成文本嵌入
     * <p>
     * 使用指定模型批量生成多个文本的嵌入向量
     * 
     * @param model 模型名称
     * @param texts 文本列表
     * @return 嵌入向量列表
     */
    List<List<Double>> embedBatch(String model, List<String> texts);

    /**
     * 获取默认模型名称
     * 
     * @return 默认模型名称
     */
    String getDefaultModel();

    /**
     * 获取嵌入向量维度
     * 
     * @return 嵌入向量维度
     */
    int getEmbeddingDimension();
}
