package com.careercoach.tools;

import com.careercoach.rag.RagPipeline;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Tool: Estimate promotion timeline based on profile data.
 */
public class PromotionTimelineTool implements Tool {

    private final RagPipeline ragPipeline;

    public PromotionTimelineTool(RagPipeline ragPipeline) {
        this.ragPipeline = ragPipeline;
    }

    @Override
    public String getName() {
        return "estimate_promotion_timeline";
    }

    @Override
    public String getDescription() {
        return "Estimates a realistic promotion timeline given the engineer's current level, " +
               "years of experience, and known gaps. Returns context from the knowledge base " +
               "about typical timelines at this transition.";
    }

    @Override
    public String getInputSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "current_level": {
                  "type": "string",
                  "description": "Current level, e.g., L4"
                },
                "years_at_level": {
                  "type": "string",
                  "description": "How many years at the current level"
                },
                "performance_signal": {
                  "type": "string",
                  "description": "Performance signal: strong/moderate/developing"
                }
              },
              "required": ["current_level", "years_at_level"]
            }
            """;
    }

    @Override
    public String execute(JsonNode input) {
        String currentLevel = input.get("current_level").asText();
        String yearsAtLevel = input.get("years_at_level").asText();
        String performance = input.has("performance_signal") ? input.get("performance_signal").asText() : "moderate";

        String query = String.format("promotion timeline %s years experience", currentLevel);
        String context = ragPipeline.retrieve(query, 2);

        return String.format(
            "TIMELINE CONTEXT for %s with %s years at level (performance: %s):\n\n%s",
            currentLevel, yearsAtLevel, performance, context
        );
    }
}
