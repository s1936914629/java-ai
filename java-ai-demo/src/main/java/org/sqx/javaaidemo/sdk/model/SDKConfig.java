package org.sqx.javaaidemo.sdk.model;

import java.io.InputStream;

public class SDKConfig {
    private final InputStream modelStream;
    private int inputWidth = 224;
    private int inputHeight = 224;
    private boolean useGPU = false;

    private SDKConfig(Builder builder) {
        this.modelStream = builder.modelStream;
        this.inputWidth = builder.inputWidth;
        this.inputHeight = builder.inputHeight;
        this.useGPU = builder.useGPU;
    }

    public static class Builder {
        private final InputStream modelStream;
        private int inputWidth = 224;
        private int inputHeight = 224;
        private boolean useGPU = false;

        public Builder(InputStream modelStream) {
            this.modelStream = modelStream;
        }

        public Builder inputSize(int width, int height) {
            this.inputWidth = width;
            this.inputHeight = height;
            return this;
        }

        public Builder useGPU(boolean useGPU) {
            this.useGPU = useGPU;
            return this;
        }

        public SDKConfig build() {
            return new SDKConfig(this);
        }
    }

    public InputStream getModelStream() {
        return modelStream;
    }

    public int getInputWidth() {
        return inputWidth;
    }

    public int getInputHeight() {
        return inputHeight;
    }

    public boolean isUseGPU() {
        return useGPU;
    }
}