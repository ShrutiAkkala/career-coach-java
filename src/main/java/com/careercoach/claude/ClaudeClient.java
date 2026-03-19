package com.careercoach.claude;

import com.careercoach.tools.ToolRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Claude API client using Java's built-in HttpClient.
 * Handles the tool-use loop natively.
 */
public class ClaudeClient {
    private static final Logger log = LoggerFactory.getLogger(ClaudeClient.class);
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-opus-4-6";
    private static final int MAX_TOKENS = 4096;
    private static final int MAX_TOOL_ROUNDS = 5;

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public ClaudeClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.mapper = new ObjectMapper();
    }

    /**
     * Send a message with tools available. Executes the full tool-use loop.
     */
    public String chat(String systemPrompt, String userMessage, ToolRegistry toolRegistry) throws Exception {
        List<ObjectNode> messages = new ArrayList<>();

        ObjectNode userMsg = mapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
            ObjectNode requestBody = buildRequest(systemPrompt, messages, toolRegistry);
            log.debug("Claude API call round {}", round + 1);

            String responseBody = callApi(requestBody.toString());
            JsonNode response = mapper.readTree(responseBody);

            if (response.has("error")) {
                String errorMsg = response.path("error").path("message").asText("Unknown error");
                throw new RuntimeException("Claude API error: " + errorMsg);
            }

            String stopReason = response.path("stop_reason").asText();
            JsonNode contentArray = response.path("content");

            // Add assistant response to message history
            ObjectNode assistantMsg = mapper.createObjectNode();
            assistantMsg.put("role", "assistant");
            assistantMsg.set("content", contentArray);
            messages.add(assistantMsg);

            if ("end_turn".equals(stopReason) || !hasToolUse(contentArray)) {
                // Extract final text response
                return extractText(contentArray);
            }

            if ("tool_use".equals(stopReason)) {
                // Execute tools and collect results
                ArrayNode toolResults = mapper.createArrayNode();
                for (JsonNode contentBlock : contentArray) {
                    if ("tool_use".equals(contentBlock.path("type").asText())) {
                        String toolName = contentBlock.path("name").asText();
                        String toolUseId = contentBlock.path("id").asText();
                        JsonNode toolInput = contentBlock.path("input");

                        log.info("Executing tool: {}", toolName);
                        String toolResult = toolRegistry.execute(toolName, toolInput);

                        ObjectNode resultBlock = mapper.createObjectNode();
                        resultBlock.put("type", "tool_result");
                        resultBlock.put("tool_use_id", toolUseId);
                        resultBlock.put("content", toolResult);
                        toolResults.add(resultBlock);
                    }
                }

                ObjectNode toolResultMsg = mapper.createObjectNode();
                toolResultMsg.put("role", "user");
                toolResultMsg.set("content", toolResults);
                messages.add(toolResultMsg);
            }
        }

        return "Max tool rounds reached. Unable to generate complete response.";
    }

    /**
     * Simple chat without tools.
     */
    public String chat(String systemPrompt, String userMessage) throws Exception {
        List<ObjectNode> messages = new ArrayList<>();
        ObjectNode userMsg = mapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", MODEL);
        requestBody.put("max_tokens", MAX_TOKENS);
        requestBody.put("system", systemPrompt);
        ArrayNode msgArray = mapper.createArrayNode();
        messages.forEach(msgArray::add);
        requestBody.set("messages", msgArray);

        String responseBody = callApi(requestBody.toString());
        JsonNode response = mapper.readTree(responseBody);

        if (response.has("error")) {
            throw new RuntimeException("Claude API error: " + response.path("error").path("message").asText());
        }

        return extractText(response.path("content"));
    }

    private ObjectNode buildRequest(String systemPrompt, List<ObjectNode> messages, ToolRegistry toolRegistry) throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", MODEL);
        body.put("max_tokens", MAX_TOKENS);
        body.put("system", systemPrompt);

        ArrayNode msgArray = mapper.createArrayNode();
        messages.forEach(msgArray::add);
        body.set("messages", msgArray);

        if (toolRegistry != null && !toolRegistry.getAll().isEmpty()) {
            List<Map<String, Object>> toolDefs = toolRegistry.toClaudeToolDefinitions();
            body.set("tools", mapper.valueToTree(toolDefs));
        }

        return body;
    }

    private String callApi(String requestBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .header("Content-Type", "application/json")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofSeconds(120))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("API returned status {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("API error " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    private String extractText(JsonNode contentArray) {
        StringBuilder text = new StringBuilder();
        for (JsonNode block : contentArray) {
            if ("text".equals(block.path("type").asText())) {
                text.append(block.path("text").asText());
            }
        }
        return text.toString().trim();
    }

    private boolean hasToolUse(JsonNode contentArray) {
        for (JsonNode block : contentArray) {
            if ("tool_use".equals(block.path("type").asText())) return true;
        }
        return false;
    }
}
