package org.sqx.mnistclassification.service;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
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

            // 评估模型 - 这里需要从MnistModel获取实际的网络
            DataSetIterator mnistTest = new MnistDataSetIterator(64, false, 123);

            // 修改：这里应该调用mnistModel内部的评估方法
            Evaluation eval = evaluateModel();

            long endTime = System.currentTimeMillis();
            long trainingTime = (endTime - startTime) / 1000;

            result.put("success", true);
            result.put("accuracy", eval.accuracy());
            result.put("precision", eval.precision());
            result.put("recall", eval.recall());
            result.put("f1", eval.f1());
            result.put("trainingTime", trainingTime);
            result.put("epochs", epochs);
            result.put("confusionMatrix", eval.confusionToString());

            log.info("训练完成，准确率: {:.2f}%", eval.accuracy() * 100);

        } catch (Exception e) {
            log.error("训练失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    // 新增：评估模型的方法
    private Evaluation evaluateModel() throws Exception {
        DataSetIterator mnistTest = new MnistDataSetIterator(64, false, 123);
        return mnistModel.evaluate(mnistTest);
    }

    public Map<String, Object> getModelStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("trained", mnistModel.isTrained());
        status.put("modelInfo", mnistModel.getModelInfo());
        return status;
    }
}