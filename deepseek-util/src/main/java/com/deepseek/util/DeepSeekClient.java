package com.deepseek.util;

import com.deepseek.config.DeepSeekConfig;
import com.deepseek.model.DeepSeekMessage;
import com.deepseek.model.DeepSeekRequest;
import com.deepseek.model.DeepSeekResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
public class DeepSeekClient {

    private final DeepSeekConfig deepSeekConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DeepSeekClient(DeepSeekConfig deepSeekConfig, ObjectMapper objectMapper) {
        this.deepSeekConfig = deepSeekConfig;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate(this.createRequestFactory());
    }

    private ClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(deepSeekConfig.getTimeout() * 1000);
        factory.setReadTimeout(deepSeekConfig.getTimeout() * 1000);
        return factory;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepSeekConfig.getApiKey());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    public DeepSeekResponse chat(List<DeepSeekMessage> messages) {
        return chat(deepSeekConfig.getModel(), messages);
    }

    public DeepSeekResponse chat(String model, List<DeepSeekMessage> messages) {
        DeepSeekRequest request = DeepSeekRequest.builder()
                .model(model)
                .messages(messages)
                .build();
        return sendRequest(request);
    }

    public DeepSeekResponse chat(String model, List<DeepSeekMessage> messages, Double temperature, Integer maxTokens) {
        DeepSeekRequest request = DeepSeekRequest.builder()
                .model(model)
                .messages(messages)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
        return sendRequest(request);
    }

    public String simpleChat(String prompt) {
        List<DeepSeekMessage> messages = Collections.singletonList(DeepSeekMessage.user(prompt));
        DeepSeekResponse response = chat(messages);
        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
            return response.getChoices().get(0).getMessage().getContent();
        }
        return null;
    }

    public String simpleChat(String systemPrompt, String userPrompt) {
        List<DeepSeekMessage> messages = List.of(
                DeepSeekMessage.system(systemPrompt),
                DeepSeekMessage.user(userPrompt)
        );
        DeepSeekResponse response = chat(messages);
        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
            return response.getChoices().get(0).getMessage().getContent();
        }
        return null;
    }

    private DeepSeekResponse sendRequest(DeepSeekRequest request) {
        String url = deepSeekConfig.getBaseUrl() + "/chat/completions";
        HttpEntity<DeepSeekRequest> httpEntity = new HttpEntity<>(request, createHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, httpEntity, DeepSeekResponse.class).getBody();
    }
}