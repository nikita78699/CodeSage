package com.nikita.Codesage.service;

import com.nikita.Codesage.config.OpenAIConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    private final RestTemplate restTemplate;
    private final OpenAIConfig config;

    @Autowired
    public OpenAIService(RestTemplate restTemplate, OpenAIConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public String analyzeCodeChanges(String codeChanges) {
        String url = config.getGroqApiUrl();
        String apiKey = config.getGroqApiKey();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Construct message payload
        Map<String, Object> messageSystem = Map.of(
            "role", "system",
            "content", "You are a senior code reviewer."
        );

        // Dummy change to test CodeSage PR review

        Map<String, Object> messageUser = Map.of(
            "role", "user",
            "content", "Review the following code changes and give suggestions:\n\n" + codeChanges
        );

        Map<String, Object> requestBody = Map.of(
            "model", "llama3-70b-8192",
            "messages", List.of(messageSystem, messageUser)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> body = response.getBody();

            System.out.println("Groq full response: " + body);

            if (body != null && body.containsKey("choices")) {
                List<?> choices = (List<?>) body.get("choices");

                if (!choices.isEmpty()) {
                    Object firstChoice = choices.get(0);

                    if (firstChoice instanceof Map<?, ?> choiceMap) {
                        Object messageObj = choiceMap.get("message");

                        if (messageObj instanceof Map<?, ?> messageMap) {
                            Object content = messageMap.get("content");

                            // Debug actual type of content
                            System.out.println("messageMap.get(\"content\") type: " + content.getClass());
                            System.out.println("messageMap: " + messageMap);

                            // ✅ Check if it's a direct string
                            if (content instanceof String contentStr) {
                                return contentStr;

                            // ✅ If it's a map like { "text": "..." }
                            } else if (content instanceof Map<?, ?> contentMap) {
                                Object text = contentMap.get("text");

                                if (text instanceof String textStr) {
                                    return textStr;
                                } else {
                                    return "Groq response content.text is not a string.";
                                }

                            } else {
                                return "Unexpected Groq response format: content is not string or map.";
                            }
                        }
                    }
                }
            }

            return "Invalid or empty response from Groq.";
        } catch (Exception e) {
            e.printStackTrace(); // ✅ See stacktrace in logs
            return "An error occurred while communicating with Groq: " + e.getMessage();
        }
    }
}
