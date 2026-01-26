package com.deepseek.model;

public class DeepSeekMessage {

    private String role;
    private String content;

    public DeepSeekMessage() {
    }

    public DeepSeekMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static DeepSeekMessage of(String role, String content) {
        return new DeepSeekMessage(role, content);
    }

    public static DeepSeekMessage user(String content) {
        return new DeepSeekMessage("user", content);
    }

    public static DeepSeekMessage assistant(String content) {
        return new DeepSeekMessage("assistant", content);
    }

    public static DeepSeekMessage system(String content) {
        return new DeepSeekMessage("system", content);
    }
}