package com.careercoach.tools;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MCP-inspired Tool Registry.
 * Registers tools and dispatches calls by name.
 */
public class ToolRegistry {
    private static final Logger log = LoggerFactory.getLogger(ToolRegistry.class);

    private final Map<String, Tool> tools = new LinkedHashMap<>();

    public void register(Tool tool) {
        tools.put(tool.getName(), tool);
        log.debug("Registered tool: {}", tool.getName());
    }

    public String execute(String toolName, JsonNode input) {
        Tool tool = tools.get(toolName);
        if (tool == null) {
            return "Error: Tool not found: " + toolName;
        }
        log.debug("Executing tool: {} with input: {}", toolName, input);
        try {
            String result = tool.execute(input);
            log.debug("Tool {} result length: {}", toolName, result.length());
            return result;
        } catch (Exception e) {
            log.error("Tool execution failed: {}", toolName, e);
            return "Error executing tool " + toolName + ": " + e.getMessage();
        }
    }

    public List<Tool> getAll() {
        return new ArrayList<>(tools.values());
    }

    /** Returns tool definitions formatted for Claude's API */
    public List<Map<String, Object>> toClaudeToolDefinitions() {
        return tools.values().stream().map(tool -> {
            Map<String, Object> def = new LinkedHashMap<>();
            def.put("name", tool.getName());
            def.put("description", tool.getDescription());
            // We'll parse the schema string as raw JSON via Jackson
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                def.put("input_schema", mapper.readTree(tool.getInputSchema()));
            } catch (Exception e) {
                def.put("input_schema", Map.of("type", "object", "properties", Map.of()));
            }
            return def;
        }).collect(Collectors.toList());
    }
}
