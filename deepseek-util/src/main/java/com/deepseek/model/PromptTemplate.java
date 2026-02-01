package com.deepseek.model;

/**
 * Prompt 模板类
 * <p>
 * 用于存储和管理提示模板信息
 */
public class PromptTemplate {

    /**
     * 模板名称
     */
    private String name;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 系统提示
     */
    private String systemPrompt;
    
    /**
     * 用户提示模板
     * <p>
     * 可以包含占位符，如 {0}, {1} 等，用于运行时替换
     */
    private String userPromptTemplate;
    
    /**
     * 模板参数数量
     */
    private int paramCount;

    /**
     * 默认构造方法
     */
    public PromptTemplate() {
    }

    /**
     * 构造方法
     * 
     * @param name 模板名称
     * @param description 模板描述
     * @param systemPrompt 系统提示
     * @param userPromptTemplate 用户提示模板
     * @param paramCount 模板参数数量
     */
    public PromptTemplate(String name, String description, String systemPrompt, String userPromptTemplate, int paramCount) {
        this.name = name;
        this.description = description;
        this.systemPrompt = systemPrompt;
        this.userPromptTemplate = userPromptTemplate;
        this.paramCount = paramCount;
    }

    /**
     * 获取模板名称
     * 
     * @return 模板名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置模板名称
     * 
     * @param name 模板名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取模板描述
     * 
     * @return 模板描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置模板描述
     * 
     * @param description 模板描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取系统提示
     * 
     * @return 系统提示
     */
    public String getSystemPrompt() {
        return systemPrompt;
    }

    /**
     * 设置系统提示
     * 
     * @param systemPrompt 系统提示
     */
    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    /**
     * 获取用户提示模板
     * 
     * @return 用户提示模板
     */
    public String getUserPromptTemplate() {
        return userPromptTemplate;
    }

    /**
     * 设置用户提示模板
     * 
     * @param userPromptTemplate 用户提示模板
     */
    public void setUserPromptTemplate(String userPromptTemplate) {
        this.userPromptTemplate = userPromptTemplate;
    }

    /**
     * 获取模板参数数量
     * 
     * @return 模板参数数量
     */
    public int getParamCount() {
        return paramCount;
    }

    /**
     * 设置模板参数数量
     * 
     * @param paramCount 模板参数数量
     */
    public void setParamCount(int paramCount) {
        this.paramCount = paramCount;
    }

    /**
     * 构建用户提示
     * <p>
     * 根据提供的参数替换用户提示模板中的占位符
     * 
     * @param params 参数数组
     * @return 构建后的用户提示
     */
    public String buildUserPrompt(Object... params) {
        if (params.length != paramCount) {
            throw new IllegalArgumentException("参数数量不匹配，需要 " + paramCount + " 个参数，但提供了 " + params.length + " 个参数");
        }
        
        String result = userPromptTemplate;
        for (int i = 0; i < params.length; i++) {
            result = result.replace("{" + i + "}", params[i].toString());
        }
        return result;
    }

    /**
     * 静态工厂方法：创建模板
     * 
     * @param name 模板名称
     * @param description 模板描述
     * @param systemPrompt 系统提示
     * @param userPromptTemplate 用户提示模板
     * @param paramCount 模板参数数量
     * @return PromptTemplate 实例
     */
    public static PromptTemplate of(String name, String description, String systemPrompt, String userPromptTemplate, int paramCount) {
        return new PromptTemplate(name, description, systemPrompt, userPromptTemplate, paramCount);
    }

}
