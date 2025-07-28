package com.nikita.Codesage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "groq.api")
public class OpenAIConfig {

    private String url;
    private String key;

    public String getGroqApiUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGroqApiKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
