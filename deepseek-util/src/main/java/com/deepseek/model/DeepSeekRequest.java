package com.deepseek.model;

import java.util.List;

public class DeepSeekRequest {

    private String model;
    private List<DeepSeekMessage> messages;
    private Double temperature = 0.7;
    private Double topP = 1.0;
    private Integer maxTokens = 1024;
    private Boolean stream = false;
    private String stop;

    public DeepSeekRequest() {
    }

    public DeepSeekRequest(String model, List<DeepSeekMessage> messages) {
        this.model = model;
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<DeepSeekMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<DeepSeekMessage> messages) {
        this.messages = messages;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public static class Builder {
        private String model;
        private List<DeepSeekMessage> messages;
        private Double temperature = 0.7;
        private Double topP = 1.0;
        private Integer maxTokens = 1024;
        private Boolean stream = false;
        private String stop;

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder messages(List<DeepSeekMessage> messages) {
            this.messages = messages;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder stream(Boolean stream) {
            this.stream = stream;
            return this;
        }

        public Builder stop(String stop) {
            this.stop = stop;
            return this;
        }

        public DeepSeekRequest build() {
            DeepSeekRequest request = new DeepSeekRequest();
            request.setModel(model);
            request.setMessages(messages);
            request.setTemperature(temperature);
            request.setTopP(topP);
            request.setMaxTokens(maxTokens);
            request.setStream(stream);
            request.setStop(stop);
            return request;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}