package com.careercoach.tools;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * MCP-inspired Tool interface.
 * Each tool has a name, description, JSON schema, and execute method.
 */
public interface Tool {
    String getName();
    String getDescription();
    /** Returns JSON Schema as a string for Claude's tool definition */
    String getInputSchema();
    /** Executes the tool with the given JSON input node */
    String execute(JsonNode input);
}
