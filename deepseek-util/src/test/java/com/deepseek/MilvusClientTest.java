package com.deepseek;

import com.deepseek.util.MilvusClientUtil;
import io.milvus.client.MilvusClient;
import io.milvus.param.R;
import io.milvus.param.MetricType;
import io.milvus.grpc.SearchResults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class MilvusClientTest {

    @Autowired
    private MilvusClientUtil milvusClientUtil;

    @Test
    public void testMilvusConnection() {
        MilvusClient client = milvusClientUtil.getClient();
        assert client != null;
        System.out.println("Milvus client initialized successfully");
    }

    @Test
    public void testCreateCollection() {
        String collectionName = "test_collection";
        int dimension = 128;

        // 先删除已存在的集合
        R<?> dropResult = milvusClientUtil.dropCollection(collectionName);
        System.out.println("Drop collection result: " + dropResult);

        // 创建新集合
        R<?> createResult = milvusClientUtil.createCollection(collectionName, dimension);
        System.out.println("Create collection result: " + createResult);
        assert createResult.getStatus() == 0;
    }

    @Test
    public void testInsertAndSearch() {
        String collectionName = "test_collection";
        int dimension = 128;

        // 确保集合存在
        R<?> hasResult = milvusClientUtil.hasCollection(collectionName);
        if (hasResult.getStatus() == 0 && hasResult.getData() == null) {
            milvusClientUtil.createCollection(collectionName, dimension);
        }

        // 准备插入数据
        List<Long> ids = new ArrayList<>();
        List<List<Float>> vectors = new ArrayList<>();
        Map<String, List<?>> fields = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            ids.add((long) i);
            List<Float> vector = new ArrayList<>();
            for (int j = 0; j < dimension; j++) {
                vector.add((float) (Math.random() * 2 - 1));
            }
            vectors.add(vector);
        }

        // 插入数据
        R<?> insertResult = milvusClientUtil.insert(collectionName, ids, vectors, fields);
        System.out.println("Insert result: " + insertResult);
        assert insertResult.getStatus() == 0;

        // 准备搜索向量
        List<List<Float>> searchVectors = new ArrayList<>();
        List<Float> searchVector = new ArrayList<>();
        for (int j = 0; j < dimension; j++) {
            searchVector.add((float) (Math.random() * 2 - 1));
        }
        searchVectors.add(searchVector);

        // 搜索
        R<?> searchResult = milvusClientUtil.search(collectionName, searchVectors, 5, MetricType.L2);
        System.out.println("Search result: " + searchResult);
        assert searchResult.getStatus() == 0;
    }
}
