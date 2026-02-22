package com.deepseek.util;

import com.deepseek.llm.EmbeddingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Embedding 工具类
 * 提供文本切片、批量嵌入等实用功能，支持处理长文本
 */
public class EmbeddingUtil {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingUtil.class);
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_OVERLAP_SIZE = 100;
    public final EmbeddingClient embeddingClient;

    public EmbeddingUtil(EmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
    }

    /**
     * 将长文本切片（使用默认参数）
     */
    public List<String> chunkText(String text) {
        return chunkText(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP_SIZE);
    }

    /**
     * 将长文本切片（自定义参数）
     */
    public List<String> chunkText(String text, int chunkSize, int overlapSize) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        if (text.length() <= chunkSize) {
            return List.of(text);
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;
        int textLength = text.length();

        while (start < textLength) {
            int end = Math.min(start + chunkSize, textLength);
            chunks.add(text.substring(start, end));
            
            if (end < textLength) {
                start = end - overlapSize;
            } else {
                break;
            }
        }

        logger.info("文本切片完成，原始长度: {}, 切片数量: {}", textLength, chunks.size());
        return chunks;
    }

    /**
     * 生成长文本的嵌入向量（使用默认参数）
     */
    public List<Double> embedLongText(String text) {
        return embedLongText(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP_SIZE);
    }

    /**
     * 生成长文本的嵌入向量（自定义参数）
     */
    public List<Double> embedLongText(String text, int chunkSize, int overlapSize) {
        List<String> chunks = chunkText(text, chunkSize, overlapSize);
        List<List<Double>> embeddings = embeddingClient.embedBatch(chunks);
        return averageEmbeddings(embeddings);
    }

    /**
     * 批量生成长文本的嵌入向量（使用默认参数）
     */
    public List<List<Double>> embedLongTexts(List<String> texts) {
        return embedLongTexts(texts, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP_SIZE);
    }

    /**
     * 批量生成长文本的嵌入向量（自定义参数）
     */
    public List<List<Double>> embedLongTexts(List<String> texts, int chunkSize, int overlapSize) {
        return texts.stream()
                .map(text -> embedLongText(text, chunkSize, overlapSize))
                .collect(Collectors.toList());
    }

    /**
     * 平均多个嵌入向量
     */
    public List<Double> averageEmbeddings(List<List<Double>> embeddings) {
        if (embeddings == null || embeddings.isEmpty()) {
            return new ArrayList<>();
        }

        int dimension = embeddings.get(0).size();
        List<Double> averaged = new ArrayList<>(dimension);

        for (int i = 0; i < dimension; i++) {
            double sum = 0.0;
            for (List<Double> embedding : embeddings) {
                sum += embedding.get(i);
            }
            averaged.add(sum / embeddings.size());
        }

        return averaged;
    }

    /**
     * 计算两个嵌入向量的余弦相似度
     */
    public double cosineSimilarity(List<Double> embedding1, List<Double> embedding2) {
        if (embedding1 == null || embedding2 == null || embedding1.size() != embedding2.size()) {
            throw new IllegalArgumentException("嵌入向量维度不匹配");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < embedding1.size(); i++) {
            dotProduct += embedding1.get(i) * embedding2.get(i);
            norm1 += embedding1.get(i) * embedding1.get(i);
            norm2 += embedding2.get(i) * embedding2.get(i);
        }

        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (norm1 * norm2);
    }

    /**
     * 查找最相似的文本
     */
    public String findMostSimilar(String query, List<String> candidates) {
        List<Double> queryEmbedding = embeddingClient.embed(query);
        double maxSimilarity = -1.0;
        String mostSimilar = null;

        for (String candidate : candidates) {
            double similarity = cosineSimilarity(queryEmbedding, embeddingClient.embed(candidate));
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                mostSimilar = candidate;
            }
        }

        logger.info("找到最相似文本，相似度: {}", maxSimilarity);
        return mostSimilar;
    }
}
