package org.sqx.mnistclassification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mnist.training")
public class TrainingConfig {

    private int defaultEpochs = 20;
    private int batchSize = 64;
    private double learningRate = 0.001;
    private boolean enableEarlyStopping = true;
    private int earlyStoppingPatience = 5;
    
    // 调参相关参数
    private int[] hiddenLayerSizes = {1024, 512, 256, 128};
    private double[] dropoutRates = {0.3, 0.3, 0.2};
    private double l2Regularization = 0.0001;
    private int rngSeed = 12345;

    // getters and setters
    public int getDefaultEpochs() { return defaultEpochs; }
    public void setDefaultEpochs(int defaultEpochs) { this.defaultEpochs = defaultEpochs; }

    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

    public double getLearningRate() { return learningRate; }
    public void setLearningRate(double learningRate) { this.learningRate = learningRate; }

    public boolean isEnableEarlyStopping() { return enableEarlyStopping; }
    public void setEnableEarlyStopping(boolean enableEarlyStopping) { this.enableEarlyStopping = enableEarlyStopping; }

    public int getEarlyStoppingPatience() { return earlyStoppingPatience; }
    public void setEarlyStoppingPatience(int earlyStoppingPatience) { this.earlyStoppingPatience = earlyStoppingPatience; }
    
    // 调参相关getters and setters
    public int[] getHiddenLayerSizes() { return hiddenLayerSizes; }
    public void setHiddenLayerSizes(int[] hiddenLayerSizes) { this.hiddenLayerSizes = hiddenLayerSizes; }
    
    public double[] getDropoutRates() { return dropoutRates; }
    public void setDropoutRates(double[] dropoutRates) { this.dropoutRates = dropoutRates; }
    
    public double getL2Regularization() { return l2Regularization; }
    public void setL2Regularization(double l2Regularization) { this.l2Regularization = l2Regularization; }
    
    public int getRngSeed() { return rngSeed; }
    public void setRngSeed(int rngSeed) { this.rngSeed = rngSeed; }
}