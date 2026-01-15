package org.sqx.mnistclassification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mnist.training")
public class TrainingConfig {

    private int defaultEpochs = 15;
    private int batchSize = 128;
    private double learningRate = 0.001;
    private boolean enableEarlyStopping = true;
    private int earlyStoppingPatience = 5;

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
}