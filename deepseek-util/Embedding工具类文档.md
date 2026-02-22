# Embedding 工具类文档

## 1. 简介

本文档详细介绍了 Embedding 服务封装及工具类的实现，特别是文本切片逻辑的设计与使用。该工具类主要用于处理长文本的嵌入操作，通过智能切片和向量合并，解决了大文本无法直接嵌入的问题。

## 2. 核心组件

### 2.1 EmbeddingClient 接口

`EmbeddingClient` 是一个抽象接口，定义了通用的文本嵌入操作方法，支持不同模型提供商的实现。

```java
public interface EmbeddingClient {
    List<Double> embed(String text);
    List<Double> embed(String model, String text);
    List<List<Double>> embedBatch(List<String> texts);
    List<List<Double>> embedBatch(String model, List<String> texts);
    String getDefaultModel();
    int getEmbeddingDimension();
}
```

### 2.2 EmbeddingUtil 工具类

`EmbeddingUtil` 是核心工具类，提供了文本切片、长文本嵌入等实用功能。

## 3. 文本切片逻辑

### 3.1 切片原理

文本切片是处理长文本的关键技术，其核心原理是：

1. 将长文本分割成多个较小的文本块（chunk）
2. 为每个文本块生成嵌入向量
3. 将多个嵌入向量合并为一个综合向量

### 3.2 实现细节

```java
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
        String chunk = text.substring(start, end);
        chunks.add(chunk);
        
        // 如果不是最后一个切片，向前移动 start 位置，留出重叠部分
        if (end < textLength) {
            start = end - overlapSize;
        } else {
            break;
        }
    }

    logger.info("文本切片完成，原始长度: {}, 切片数量: {}", textLength, chunks.size());
    return chunks;
}
```

### 3.3 切片参数

| 参数 | 默认值 | 描述 |
|------|--------|------|
| `chunkSize` | 1000 | 每个切片的大小（字符数） |
| `overlapSize` | 100 | 切片之间的重叠大小（字符数） |

### 3.4 切片策略

1. **长度检查**：如果文本长度小于等于切片大小，直接返回原文本
2. **重叠设计**：相邻切片之间保留重叠部分，确保语义连贯性
3. **边界处理**：最后一个切片不设置重叠，避免重复计算

## 4. 长文本嵌入

### 4.1 单文本嵌入

```java
public List<Double> embedLongText(String text, int chunkSize, int overlapSize) {
    List<String> chunks = chunkText(text, chunkSize, overlapSize);
    List<List<Double>> embeddings = embeddingClient.embedBatch(chunks);
    return averageEmbeddings(embeddings);
}
```

### 4.2 批量文本嵌入

```java
public List<List<Double>> embedLongTexts(List<String> texts, int chunkSize, int overlapSize) {
    return texts.stream()
            .map(text -> embedLongText(text, chunkSize, overlapSize))
            .collect(Collectors.toList());
}
```

## 5. 向量操作

### 5.1 向量平均

```java
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
```

### 5.2 余弦相似度计算

```java
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
```

### 5.3 相似文本查找

```java
public String findMostSimilar(String query, List<String> candidates) {
    List<Double> queryEmbedding = embeddingClient.embed(query);
    double maxSimilarity = -1.0;
    String mostSimilar = null;

    for (String candidate : candidates) {
        List<Double> candidateEmbedding = embeddingClient.embed(candidate);
        double similarity = cosineSimilarity(queryEmbedding, candidateEmbedding);
        
        if (similarity > maxSimilarity) {
            maxSimilarity = similarity;
            mostSimilar = candidate;
        }
    }

    logger.info("找到最相似文本，相似度: {}", maxSimilarity);
    return mostSimilar;
}
```

## 6. 使用示例

### 6.1 基本用法

```java
// 1. 创建 EmbeddingClient 实例
EmbeddingClient embeddingClient = new OpenAIEmbeddingClient(apiKey);

// 2. 创建 EmbeddingUtil 实例
EmbeddingUtil embeddingUtil = new EmbeddingUtil(embeddingClient);

// 3. 处理长文本
String longText = "这是一个非常长的文本...";
List<Double> embedding = embeddingUtil.embedLongText(longText);

// 4. 计算相似度
String query = "查询文本";
double similarity = embeddingUtil.cosineSimilarity(
    embeddingClient.embed(query),
    embedding
);

// 5. 查找最相似文本
List<String> candidates = List.of("文本1", "文本2", "文本3");
String mostSimilar = embeddingUtil.findMostSimilar(query, candidates);
```

### 6.2 自定义切片参数

```java
// 自定义切片大小和重叠大小
List<Double> embedding = embeddingUtil.embedLongText(
    longText, 
    2000,  // 更大的切片大小
    200    // 更大的重叠大小
);
```

## 7. 技术细节

### 7.1 默认参数配置

| 参数 | 值 | 说明 |
|------|-----|------|
| `DEFAULT_CHUNK_SIZE` | 1000 | 默认切片大小（字符数） |
| `DEFAULT_OVERLAP_SIZE` | 100 | 默认重叠大小（字符数） |

### 7.2 性能考虑

1. **批处理优化**：使用 `embedBatch` 方法减少 API 调用次数
2. **内存管理**：对于非常长的文本，可能需要进一步优化内存使用
3. **并行处理**：对于批量操作，可以考虑使用并行流提高性能

### 7.3 适用场景

- **文档检索**：为长文档生成嵌入向量，用于相似度搜索
- **文本分类**：处理长文本分类任务
- **问答系统**：为长文档生成向量，用于相关段落检索
- **语义搜索**：构建基于向量的搜索引擎

## 8. 代码结构

```
com.deepseek.llm
├── EmbeddingClient.java       // Embedding 客户端接口
└── impl
    └── OpenAIEmbeddingClient.java  // OpenAI 实现（示例）

com.deepseek.util
└── EmbeddingUtil.java         // Embedding 工具类（含切片逻辑）
```

## 9. 总结

Embedding 工具类通过以下特性提供了强大的文本嵌入能力：

1. **智能切片**：自动处理长文本，确保语义完整性
2. **灵活配置**：支持自定义切片大小和重叠大小
3. **批量处理**：优化 API 调用，提高处理效率
4. **向量操作**：提供丰富的向量处理方法
5. **相似性计算**：支持基于余弦相似度的文本匹配

该工具类可以无缝集成到各种需要文本嵌入功能的应用中，为长文本处理提供了可靠的解决方案。