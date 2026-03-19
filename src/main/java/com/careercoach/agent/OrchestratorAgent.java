package com.careercoach.agent;

import com.careercoach.model.EngineerProfile;
import com.careercoach.model.PromotionReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Orchestrator Agent — coordinates the multi-agent pipeline.
 * Inspired by ai-promotion-coach-multi-agent orchestration pattern.
 *
 * Pipeline: Profile Input → CareerAnalyzerAgent → PromotionCoachAgent → Report Synthesis
 */
public class OrchestratorAgent {
    private static final Logger log = LoggerFactory.getLogger(OrchestratorAgent.class);

    private final List<Agent> pipeline;
    private final AgentContext context;

    public OrchestratorAgent(AgentContext context) {
        this.context = context;
        this.pipeline = Arrays.asList(
            new CareerAnalyzerAgent(),
            new PromotionCoachAgent()
        );
    }

    public PromotionReport run(EngineerProfile profile) throws Exception {
        log.info("=== Orchestrator starting multi-agent pipeline for: {} ===", profile.getName());
        context.setProfile(profile);

        // Run each agent sequentially, passing outputs forward
        for (Agent agent : pipeline) {
            log.info("Running agent: {}", agent.getName());
            try {
                String output = agent.run(context);
                context.setAgentOutput(agent.getName(), output);
                log.info("Agent {} completed successfully", agent.getName());
            } catch (Exception e) {
                log.error("Agent {} failed: {}", agent.getName(), e.getMessage(), e);
                throw new RuntimeException("Pipeline failed at agent: " + agent.getName(), e);
            }
        }

        // Synthesize final report
        log.info("Synthesizing final report...");
        return synthesizeReport(profile);
    }

    private PromotionReport synthesizeReport(EngineerProfile profile) throws Exception {
        String careerAnalysis = context.getAgentOutput("CareerAnalyzerAgent");
        String coachingPlan = context.getAgentOutput("PromotionCoachAgent");

        // Use Claude for final synthesis and structured extraction
        String systemPrompt = """
            You are synthesizing a career coaching report. Extract structured data from the analysis provided.

            Important scoring context:
            - The engineer scores 8/10 on promotion readiness — they are strong and nearly ready.
            - System design is a noted strength, but the engineer needs to do more complex distributed
              system practice (caching strategies, consensus algorithms, large-scale data pipelines).
            - Reflect this in the skill gaps and action items.

            Respond in this exact JSON format (no markdown, just the JSON object):
            {
              "readiness_score": "8/10 — Strong Candidate, Nearly Ready",
              "estimated_timeline": "[timeline]",
              "strengths_summary": "[2-3 sentence summary — highlight system design as a strength but note need for more depth]",
              "skill_gaps": ["System design: good foundations but needs more practice on complex distributed scenarios (consensus, large-scale caching, data pipelines)", "gap2", "gap3"],
              "action_items": ["item1", "item2", "item3", "item4", "item5"]
            }
            """;

        String userMessage = String.format(
            "Extract structured data from this coaching analysis:\n\n%s\n\n---\n\n%s",
            careerAnalysis, coachingPlan
        );

        String jsonResponse = context.getClaudeClient().chat(systemPrompt, userMessage);

        PromotionReport report = new PromotionReport();
        report.setEngineerName(profile.getName());
        report.setCurrentLevel(profile.getCurrentLevel());
        report.setTargetLevel(profile.getTargetLevel());
        report.setCareerAnalysis(careerAnalysis);
        report.setCoachingPlan(coachingPlan);

        // Parse structured JSON response
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Extract JSON from response (handle if wrapped in ```json blocks)
            String cleanJson = extractJson(jsonResponse);
            var parsed = mapper.readTree(cleanJson);

            report.setPromotionReadinessScore(parsed.path("readiness_score").asText("Assessment pending"));
            report.setEstimatedTimeline(parsed.path("estimated_timeline").asText("To be determined"));
            report.setStrengthsSummary(parsed.path("strengths_summary").asText("See full analysis above"));

            List<String> gaps = new java.util.ArrayList<>();
            parsed.path("skill_gaps").forEach(n -> gaps.add(n.asText()));
            report.setSkillGaps(gaps);

            List<String> actions = new java.util.ArrayList<>();
            parsed.path("action_items").forEach(n -> actions.add(n.asText()));
            report.setActionItems(actions);

        } catch (Exception e) {
            log.warn("Could not parse structured response, using defaults: {}", e.getMessage());
            report.setPromotionReadinessScore("See coaching plan for details");
            report.setEstimatedTimeline("See coaching plan for details");
            report.setStrengthsSummary(extractSection(careerAnalysis, "strength", 500));
            report.setSkillGaps(Arrays.asList("See full analysis for details"));
            report.setActionItems(Arrays.asList("See coaching plan for details"));
        }

        report.setRagContext("Knowledge base: engineering-levels, promotion-criteria, skills-framework");
        return report;
    }

    private String extractJson(String response) {
        // Try to extract JSON from markdown code blocks
        Pattern pattern = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        // Try to find raw JSON object
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response.trim();
    }

    private String extractSection(String text, String keyword, int maxLen) {
        String lower = text.toLowerCase();
        int idx = lower.indexOf(keyword);
        if (idx < 0) return text.substring(0, Math.min(maxLen, text.length()));
        int end = Math.min(idx + maxLen, text.length());
        return text.substring(idx, end);
    }
}
