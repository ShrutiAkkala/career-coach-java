package com.careercoach.tools;

import com.careercoach.rag.RagPipeline;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Tool: Retrieve relevant career knowledge from the RAG knowledge base.
 */
public class CareerKnowledgeTool implements Tool {

    private final RagPipeline ragPipeline;

    public CareerKnowledgeTool(RagPipeline ragPipeline) {
        this.ragPipeline = ragPipeline;
    }

    @Override
    public String getName() {
        return "retrieve_career_knowledge";
    }

    @Override
    public String getDescription() {
        return "Retrieves relevant career knowledge about engineering levels, promotion criteria, " +
               "and skills frameworks. Use this to get factual information about what's expected " +
               "at each level and what it takes to get promoted.";
    }

    @Override
    public String getInputSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "query": {
                  "type": "string",
                  "description": "The search query — e.g., 'L4 to L5 promotion criteria' or 'senior engineer skills'"
                },
                "top_k": {
                  "type": "integer",
                  "description": "Number of knowledge chunks to retrieve (default: 3)",
                  "default": 3
                }
              },
              "required": ["query"]
            }
            """;
    }

    @Override
    public String execute(JsonNode input) {
        String query = input.get("query").asText();
        int topK = input.has("top_k") ? input.get("top_k").asInt(3) : 3;
        return ragPipeline.retrieve(query, topK);
    }
}
