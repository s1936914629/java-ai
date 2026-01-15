package org.sqx.mnistclassification.service;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.sqx.mnistclassification.model.MnistModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    @Autowired
    private MnistModel mnistModel;

    public Map<String, Object> trainModel(int epochs) {
        Map<String, Object> result = new HashMap<>();

        try {
            long startTime = System.currentTimeMillis();

            // 训练模型
            mnistModel.train(epochs);

            // 评估模型
            DataSetIterator mnistTest = new MnistDataSetIterator(64, false, 123);
            Evaluation eval = mnistModel.evaluate(mnistTest);

            long endTime = System.currentTimeMillis();
            long trainingTime = (endTime - startTime) / 1000;

            // 正确格式化数据
            result.put("success", true);
            result.put("accuracy", eval.accuracy());
            result.put("precision", eval.precision());
            result.put("recall", eval.recall());
            result.put("f1", eval.f1());
            result.put("trainingTime", trainingTime);
            result.put("epochs", epochs);
            result.put("confusionMatrix", eval.confusionToString());

            // 添加每个数字的准确率
            Map<Integer, Double> perClassAccuracy = new HashMap<>();
            for (int i = 0; i < 10; i++) {
                perClassAccuracy.put(i, eval.precision(i));
            }
            result.put("perClassAccuracy", perClassAccuracy);

            log.info("训练完成，准确率: {:.2f}%", eval.accuracy() * 100);

        } catch (Exception e) {
            log.error("训练失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    public Map<String, Object> getModelStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("trained", mnistModel.isTrained());
        return status;
    }
}