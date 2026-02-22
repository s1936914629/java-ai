package com.deepseek.controller;

import com.deepseek.llm.EmbeddingClient;
import com.deepseek.util.EmbeddingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Embedding 示例控制器
 * 展示如何使用 Embedding 工具类处理文本嵌入任务
 */
@RestController
@RequestMapping("/api/embedding")
public class EmbeddingExampleController {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingExampleController.class);
    private final EmbeddingUtil embeddingUtil;
    private final EmbeddingClient embeddingClient;

    public EmbeddingExampleController(EmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
        this.embeddingUtil = new EmbeddingUtil(embeddingClient);
    }

    /**
     * 生成单个文本的嵌入向量
     */
    @PostMapping("/single")
    public List<Double> embedSingle(@RequestBody String text) {
        logger.info("生成单个文本的嵌入向量，文本长度: {}", text.length());
        return embeddingClient.embed(text);
    }

    /**
     * 批量生成文本的嵌入向量
     */
    @PostMapping("/batch")
    public List<List<Double>> embedBatch(@RequestBody List<String> texts) {
        logger.info("批量生成文本的嵌入向量，文本数量: {}", texts.size());
        return embeddingClient.embedBatch(texts);
    }

    /**
     * 生成长文本的嵌入向量
     */
    @PostMapping("/long-text")
    public List<Double> embedLongText(@RequestBody String text) {
        logger.info("生成长文本的嵌入向量，文本长度: {}", text.length());
        return embeddingUtil.embedLongText(text);
    }

    /**
     * 自定义参数生成长文本的嵌入向量
     */
    @PostMapping("/long-text/custom")
    public List<Double> embedLongTextCustom(@RequestBody LongTextRequest request) {
        logger.info("自定义参数生成长文本的嵌入向量，文本长度: {}, 切片大小: {}, 重叠大小: {}", 
            request.getText().length(), request.getChunkSize(), request.getOverlapSize());
        return embeddingUtil.embedLongText(
            request.getText(), request.getChunkSize(), request.getOverlapSize());
    }

    /**
     * 计算两个文本的相似度
     */
    @PostMapping("/similarity")
    public double calculateSimilarity(@RequestBody SimilarityRequest request) {
        logger.info("计算两个文本的相似度");
        var embedding1 = embeddingClient.embed(request.getText1());
        var embedding2 = embeddingClient.embed(request.getText2());
        return embeddingUtil.cosineSimilarity(embedding1, embedding2);
    }

    /**
     * 查找最相似的文本
     */
    @PostMapping("/most-similar")
    public String findMostSimilar(@RequestBody MostSimilarRequest request) {
        logger.info("查找最相似的文本，候选文本数量: {}", request.getCandidates().size());
        return embeddingUtil.findMostSimilar(request.getQuery(), request.getCandidates());
    }

    /**
     * 长文本请求参数
     */
    public static class LongTextRequest {
        private String text;
        private int chunkSize = 1000;
        private int overlapSize = 100;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public int getChunkSize() { return chunkSize; }
        public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }
        public int getOverlapSize() { return overlapSize; }
        public void setOverlapSize(int overlapSize) { this.overlapSize = overlapSize; }
    }

    /**
     * 相似度请求参数
     */
    public static class SimilarityRequest {
        private String text1;
        private String text2;

        public String getText1() { return text1; }
        public void setText1(String text1) { this.text1 = text1; }
        public String getText2() { return text2; }
        public void setText2(String text2) { this.text2 = text2; }
    }

    /**
     * 最相似文本请求参数
     */
    public static class MostSimilarRequest {
        private String query;
        private List<String> candidates;

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public List<String> getCandidates() { return candidates; }
        public void setCandidates(List<String> candidates) { this.candidates = candidates; }
    }
}