package org.sqx.mnistclassification.model;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class MnistModel {

    private static final Logger log = LoggerFactory.getLogger(MnistModel.class);

    private MultiLayerNetwork model;
    private boolean isTrained = false;
    private String modelPath = "models/mnist-model.zip";

    public MnistModel() {
        // 创建模型目录
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
                log.info("模型加载成功");
            } catch (Exception e) {
                log.error("加载模型失败，创建新模型", e);
                createNewModel();
            }
        } else {
            createNewModel();
        }
    }

    private void createNewModel() {
        log.info("创建新的神经网络模型...");

        int rngSeed = 123;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(rngSeed)
                .updater(new Adam(0.001))
                .weightInit(WeightInit.XAVIER)
                .list()
                // 简化版网络，便于快速训练
                .layer(new DenseLayer.Builder()
                        .nIn(28 * 28)
                        .nOut(128)
                        .activation(Activation.RELU)
                        .build())
                .layer(new DenseLayer.Builder()
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(
                        LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(10)
                        .activation(Activation.SOFTMAX)
                        .build())
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));
    }

    public void train(int epochs) throws IOException {
        log.info("开始训练模型，轮数: {}", epochs);

        int batchSize = 64;
        DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, true, 123);

        for (int i = 1; i <= epochs; i++) {
            log.info("训练轮次 {}/{}", i, epochs);
            model.fit(mnistTrain);
            mnistTrain.reset();
        }

        isTrained = true;
        saveModel();
        log.info("模型训练完成");
    }

    // 新增：评估方法
    public Evaluation evaluate(DataSetIterator testData) {
        log.info("评估模型性能...");
        Evaluation eval = new Evaluation(10); // 10个类别
        while (testData.hasNext()) {
            var dataSet = testData.next();
            INDArray output = model.output(dataSet.getFeatures());
            eval.eval(dataSet.getLabels(), output);
        }
        testData.reset();
        return eval;
    }

    public int predict(INDArray image) {
        if (!isTrained) {
            log.warn("模型未训练，使用随机权重进行预测");
        }

        // 确保输入形状正确
        if (image.shape().length == 1) {
            // 如果是1D数组，转换为合适的形状
            image = image.reshape(1, 28 * 28);
        }

        INDArray output = model.output(image);
        return output.argMax(1).getInt(0);
    }

    public double[] getPredictionProbabilities(INDArray image) {
        if (image.shape().length == 1) {
            image = image.reshape(1, 28 * 28);
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

    // 新增：获取模型实例（如果需要）
    public MultiLayerNetwork getModel() {
        return model;
    }
}