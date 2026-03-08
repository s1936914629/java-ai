package com.deepseek.service;

import com.deepseek.util.MilvusClientUtil;
import io.milvus.param.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 向量服务类
 * 用于管理向量的增删改查操作
 */
@Service
public class VectorService {

    @Autowired
    private MilvusClientUtil milvusClientUtil;

    /**
     * 创建向量集合
     * 
     * @param collectionName 集合名称
     * @param dimension 向量维度
     * @return 操作结果
     */
    public R<?> createCollection(String collectionName, int dimension) {
        return milvusClientUtil.createCollection(collectionName, dimension);
    }

    /**
     * 检查集合是否存在
     * 
     * @param collectionName 集合名称
     * @return 操作结果
     */
    public R<?> hasCollection(String collectionName) {
        return milvusClientUtil.hasCollection(collectionName);
    }

    /**
     * 删除集合
     * 
     * @param collectionName 集合名称
     * @return 操作结果
     */
    public R<?> dropCollection(String collectionName) {
        return milvusClientUtil.dropCollection(collectionName);
    }

    /**
     * 插入向量数据
     * 
     * @param collectionName 集合名称
     * @param ids 向量ID列表
     * @param vectors 向量列表
     * @param fields 额外字段
     * @return 操作结果
     */
    public R<?> insertVectors(String collectionName, List<Long> ids, List<List<Float>> vectors, Map<String, List<?>> fields) {
        return milvusClientUtil.insert(collectionName, ids, vectors, fields);
    }

    /**
     * 搜索向量
     * 
     * @param collectionName 集合名称
     * @param vectors 待搜索的向量列表
     * @param topK 返回的结果数量
     * @param metricType 距离度量类型
     * @return 搜索结果
     */
    public R<?> searchVectors(String collectionName, List<List<Float>> vectors, int topK, String metricType) {
        return milvusClientUtil.search(collectionName, vectors, topK, io.milvus.param.MetricType.valueOf(metricType));
    }
}
