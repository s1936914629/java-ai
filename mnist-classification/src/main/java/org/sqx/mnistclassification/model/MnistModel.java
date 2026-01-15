package org.sqx.mnistclassification.model;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MnistModel {

    private static final Logger log = LoggerFactory.getLogger(MnistModel.class);

    private MultiLayerNetwork model;
    private boolean isTrained = false;
    private String modelPath = "models/mnist-model-cnn.zip";

    // 训练历史记录
    private double[] trainingAccuracyHistory;
    private double[] trainingLossHistory;
    private int currentEpoch = 0;

    public MnistModel() {
        new File("models").mkdirs();
        loadOrCreateModel();
    }

    private void loadOrCreateModel() {
        File modelFile = new File(modelPath);
        if (modelFile.exists()) {
            try {
                log.info("加载已保存的模型...");
                model = MultiLayerNetwork.load(modelFile, true);
                isTrained = true;
                log.info("模型加载成功，模型信息：\n{}", model.summary());
            } catch (Exception e) {
                log.error("加载模型失败，创建新模型", e);
                createImprovedModel();
            }
        } else {
            createImprovedModel();
        }
    }

    private void createImprovedModel() {
        log.info("创建改进版神经网络模型...");

        int rngSeed = 12345;
        double learningRate = 0.001;

        // 使用优化的全连接神经网络结构，确保与现有输入格式兼容
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(rngSeed)
                .updater(new Adam(learningRate))
                .weightInit(WeightInit.XAVIER)
                .l2(0.0001) // L2正则化
                .list()
                // 输入层
                .layer(new DenseLayer.Builder()
                        .nIn(784) // 输入特征数（28x28）
                        .nOut(1024) // 增加神经元数量
                        .activation(Activation.RELU)
                        .dropOut(0.3) // Dropout防止过拟合
                        .build())
                // 批归一化层
                .layer(new BatchNormalization.Builder()
                        .build())
                // 隐藏层1
                .layer(new DenseLayer.Builder()
                        .nOut(512) // 神经元数量
                        .activation(Activation.RELU)
                        .dropOut(0.3) // Dropout防止过拟合
                        .build())
                // 批归一化层
                .layer(new BatchNormalization.Builder()
                        .build())
                // 隐藏层2
                .layer(new DenseLayer.Builder()
                        .nOut(256) // 神经元数量
                        .activation(Activation.RELU)
                        .dropOut(0.2) // Dropout防止过拟合
                        .build())
                // 批归一化层
                .layer(new BatchNormalization.Builder()
                        .build())
                // 隐藏层3
                .layer(new DenseLayer.Builder()
                        .nOut(128) // 神经元数量
                        .activation(Activation.RELU)
                        .build())
                // 输出层
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(10) // 10个类别（0-9）
                        .activation(Activation.SOFTMAX)
                        .build())
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();

        // 添加监听器
        model.setListeners(new ScoreIterationListener(100));

        log.info("改进版CNN模型创建完成，网络结构：");
        log.info(model.summary());
    }

    public void train(int epochs) throws IOException {
        log.info("开始训练模型，轮数: {}", epochs);

        int batchSize = 64; // 减小批大小以获得更好的梯度更新

        // 初始化训练历史记录
        trainingAccuracyHistory = new double[epochs];
        trainingLossHistory = new double[epochs];
        currentEpoch = 0;

        DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, true, 12345);

        // 评估初始状态
        mnistTrain.reset();
        double initialLoss = model.score();
        log.info("初始损失: {:.4f}", initialLoss);

        for (int i = 1; i <= epochs; i++) {
            currentEpoch = i;
            log.info("=== 训练轮次 {}/{} ===", i, epochs);

            long epochStartTime = System.currentTimeMillis();
            model.fit(mnistTrain);
            long epochEndTime = System.currentTimeMillis();

            // 计算本轮训练后的损失
            double epochLoss = model.score();
            trainingLossHistory[i-1] = epochLoss;

            // 在训练集上评估准确率
            mnistTrain.reset();
            Evaluation trainEval = new Evaluation(10);

            int batchCount = 0;
            while (mnistTrain.hasNext()) {
                var dataSet = mnistTrain.next();
                INDArray output = model.output(dataSet.getFeatures());
                trainEval.eval(dataSet.getLabels(), output);
                batchCount++;

                // 每100个批次打印一次进度
                if (batchCount % 100 == 0) {
                    log.debug("评估训练集: 已处理 {} 个批次", batchCount);
                }
            }
            mnistTrain.reset();

            double trainAccuracy = trainEval.accuracy();
            trainingAccuracyHistory[i-1] = trainAccuracy;

            // 正确格式化日志输出
            log.info("轮次 {} 完成 - 时间: {}ms, 损失: {:.4f}, 训练准确率: {:.2f}%",
                    i,
                    (epochEndTime - epochStartTime),
                    epochLoss,
                    trainAccuracy * 100);

            // 每3轮保存一次模型
            if (i % 3 == 0) {
                saveModel();
                log.info("已保存中间模型");
            }
        }

        isTrained = true;
        saveModel();

        log.info("模型训练完成，最终训练准确率: {:.2f}%",
                trainingAccuracyHistory[epochs-1] * 100);

        // 在测试集上评估
        evaluateOnTestSet();
    }

    private void evaluateOnTestSet() throws IOException {
        DataSetIterator mnistTest = new MnistDataSetIterator(64, false, 12345);
        Evaluation eval = evaluate(mnistTest);

        log.info("=== 测试集评估结果 ===");
        log.info("准确率: {:.2f}%", eval.accuracy() * 100);
        log.info("精确率: {:.2f}%", eval.precision() * 100);
        log.info("召回率: {:.2f}%", eval.recall() * 100);
        log.info("F1分数: {:.2f}%", eval.f1() * 100);

        // 打印每个数字的识别情况
        log.info("\n每个类别的精确率:");
        for (int i = 0; i < 10; i++) {
            log.info("数字 {}: {:.2f}%", i, eval.precision(i) * 100);
        }

        log.info("\n每个类别的召回率:");
        for (int i = 0; i < 10; i++) {
            log.info("数字 {}: {:.2f}%", i, eval.recall(i) * 100);
        }

        log.info("\n混淆矩阵:");
        log.info(eval.confusionToString());
    }

    public Evaluation evaluate(DataSetIterator testData) {
        log.info("评估模型性能...");
        Evaluation eval = new Evaluation(10);
        int batchCount = 0;

        while (testData.hasNext()) {
            var dataSet = testData.next();
            INDArray output = model.output(dataSet.getFeatures());
            eval.eval(dataSet.getLabels(), output);
            batchCount++;

            if (batchCount % 20 == 0) {
                log.debug("已评估 {} 个批次", batchCount);
            }
        }
        testData.reset();

        return eval;
    }

    public int predict(INDArray image) {
        if (!isTrained) {
            log.warn("模型未训练，使用随机权重进行预测");
        }

        // 确保输入形状正确（模型期望2维输入：批次大小, 特征数）
        if (image.shape().length == 1) {
            // 从一维向量(784)转换为2维输入格式(1, 784)
            image = image.reshape(1, 784);
        }

        INDArray output = model.output(image);
        int prediction = output.argMax(1).getInt(0);

        // 获取置信度
        double confidence = output.getDouble(prediction);

        if (confidence < 0.7) { // 置信度阈值
            log.debug("低置信度预测: {} (置信度: {:.2f}%)",
                    prediction, confidence * 100);
        }

        return prediction;
    }

    public Map<String, Object> predictWithDetail(INDArray image) {
        Map<String, Object> result = new HashMap<>();

        // 确保输入形状正确（模型期望2维输入：批次大小, 特征数）
        if (image.shape().length == 1) {
            // 从一维向量(784)转换为2维输入格式(1, 784)
            image = image.reshape(1, 784);
        }

        INDArray output = model.output(image);
        double[] probabilities = output.toDoubleVector();
        int prediction = output.argMax(1).getInt(0);
        double confidence = probabilities[prediction];

        // 找出Top-3预测
        List<Integer> top3Indices = new ArrayList<>();
        List<Double> top3Probabilities = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            int maxIndex = 0;
            double maxProb = -1;
            for (int j = 0; j < probabilities.length; j++) {
                if (!top3Indices.contains(j) && probabilities[j] > maxProb) {
                    maxProb = probabilities[j];
                    maxIndex = j;
                }
            }
            top3Indices.add(maxIndex);
            top3Probabilities.add(maxProb);
        }

        result.put("prediction", prediction);
        result.put("confidence", confidence);
        result.put("probabilities", probabilities);
        result.put("top3Indices", top3Indices);
        result.put("top3Probabilities", top3Probabilities);

        return result;
    }

    public double[] getPredictionProbabilities(INDArray image) {
        // 确保输入形状正确（模型期望2维输入：批次大小, 特征数）
        if (image.shape().length == 1) {
            // 从一维向量(784)转换为2维输入格式(1, 784)
            image = image.reshape(1, 784);
        }
        INDArray output = model.output(image);
        return output.toDoubleVector();
    }

    public void saveModel() {
        try {
            model.save(new File(modelPath));
            log.info("模型已保存到: {}", modelPath);
        } catch (IOException e) {
            log.error("保存模型失败", e);
        }
    }

    public boolean isTrained() {
        return isTrained;
    }

    public String getModelInfo() {
        return model.summary();
    }

    public MultiLayerNetwork getModel() {
        return model;
    }

    public Map<String, Object> getTrainingHistory() {
        Map<String, Object> history = new HashMap<>();
        if (trainingAccuracyHistory != null) {
            history.put("accuracy", trainingAccuracyHistory);
            history.put("loss", trainingLossHistory);
            history.put("epochs", currentEpoch);
        }
        return history;
    }
}