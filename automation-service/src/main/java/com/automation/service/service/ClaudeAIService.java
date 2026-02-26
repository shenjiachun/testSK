package com.automation.service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Service for interacting with the Claude AI API (Anthropic).
 * Requires ANTHROPIC_API_KEY to be set in environment or application configuration.
 */
@Service
@Slf4j
public class ClaudeAIService {

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    @Value("${anthropic.api-key:}")
    private String apiKey;

    @Value("${anthropic.model:claude-3-5-haiku-20241022}")
    private String model;

    @Value("${anthropic.max-tokens:1024}")
    private int maxTokens;

    private final RestClient restClient;

    public ClaudeAIService() {
        this.restClient = RestClient.builder()
                .baseUrl(CLAUDE_API_URL)
                .build();
    }

    /**
     * Send a prompt to Claude AI and return the text response.
     *
     * @param prompt the user prompt
     * @return the text content from Claude's response
     * @throws IllegalStateException if the API key is not configured
     */
    public String sendPrompt(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Anthropic API key not configured; task will record an error response");
            return "ERROR: Claude AI not configured. Set the ANTHROPIC_API_KEY environment variable to enable AI responses.";
        }

        log.debug("Sending prompt to Claude AI model: {}", model);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("x-api-key", apiKey)
                .header("anthropic-version", ANTHROPIC_VERSION)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        return extractTextContent(response);
    }

    @SuppressWarnings("unchecked")
    private String extractTextContent(Map<String, Object> response) {
        if (response == null) {
            return "";
        }
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
        if (content == null || content.isEmpty()) {
            return "";
        }
        Object text = content.get(0).get("text");
        return text != null ? text.toString() : "";
    }
}
