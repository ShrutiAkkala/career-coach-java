package com.careercoach.tools;

import com.careercoach.rag.RagPipeline;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * Tool: Analyze skill gaps between current and target level.
 */
public class SkillGapTool implements Tool {

    private final RagPipeline ragPipeline;

    public SkillGapTool(RagPipeline ragPipeline) {
        this.ragPipeline = ragPipeline;
    }

    @Override
    public String getName() {
        return "analyze_skill_gaps";
    }

    @Override
    public String getDescription() {
        return "Analyzes the skill gaps between a current engineering level and target level, " +
               "based on the standard skills framework. Returns specific skills that need development.";
    }

    @Override
    public String getInputSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "current_level": {
                  "type": "string",
                  "description": "Current engineering level, e.g., L4, L5, Senior"
                },
                "target_level": {
                  "type": "string",
                  "description": "Target engineering level, e.g., L5, L6, Staff"
                },
                "current_skills": {
                  "type": "array",
                  "items": { "type": "string" },
                  "description": "List of skills the engineer currently has"
                }
              },
              "required": ["current_level", "target_level"]
            }
            """;
    }

    @Override
    public String execute(JsonNode input) {
        String currentLevel = input.get("current_level").asText();
        String targetLevel = input.get("target_level").asText();

        String query = String.format("skills required for %s %s promotion gap", currentLevel, targetLevel);
        String knowledgeContext = ragPipeline.retrieve(query, 3);

        StringBuilder result = new StringBuilder();
        result.append(String.format("SKILL GAP ANALYSIS: %s → %s\n\n", currentLevel, targetLevel));
        result.append("Relevant knowledge base context:\n");
        result.append(knowledgeContext);

        if (input.has("current_skills") && input.get("current_skills").isArray()) {
            result.append("\n\nEngineer's current skills: ");
            List<String> skills = new java.util.ArrayList<>();
            input.get("current_skills").forEach(s -> skills.add(s.asText()));
            result.append(String.join(", ", skills));
        }

        return result.toString();
    }
}
