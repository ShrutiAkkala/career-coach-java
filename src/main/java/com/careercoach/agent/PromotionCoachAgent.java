package com.careercoach.agent;

import com.careercoach.model.EngineerProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent 2: Generates a concrete promotion coaching plan.
 * Builds on CareerAnalyzerAgent's output.
 */
public class PromotionCoachAgent implements Agent {
    private static final Logger log = LoggerFactory.getLogger(PromotionCoachAgent.class);

    @Override
    public String getName() { return "PromotionCoachAgent"; }

    @Override
    public String getDescription() {
        return "Creates a concrete, actionable promotion coaching plan with timeline";
    }

    @Override
    public String run(AgentContext context) throws Exception {
        log.info("PromotionCoachAgent generating coaching plan...");
        EngineerProfile profile = context.getProfile();
        String careerAnalysis = context.getAgentOutput("CareerAnalyzerAgent");

        String systemPrompt = """
            You are an expert engineering promotion coach who has helped hundreds of engineers advance.
            Your coaching is direct, specific, and actionable — not generic platitudes.

            You have access to:
            - retrieve_career_knowledge: to look up promotion criteria and frameworks
            - analyze_skill_gaps: to get structured gap analysis
            - estimate_promotion_timeline: to get realistic timeline estimates

            Generate a concrete coaching plan that includes:
            1. Promotion Readiness Score (X/10 with justification)
            2. Top 3 Skill Gaps to close (specific, not vague)
            3. 5 Concrete Action Items (specific projects, behaviors, artifacts — not "be more visible")
            4. Realistic Timeline with milestones
            5. Coaching Plan narrative (3-6 month focus areas)
            6. The single most important thing to do in the next 30 days

            Ground everything in the career analysis provided and use tools for additional context.
            Cite specific level criteria when making recommendations.
            """;

        String userMessage = String.format(
            "Create a promotion coaching plan for:\n\n%s\n\n" +
            "=== CAREER ANALYSIS FROM PREVIOUS AGENT ===\n%s\n\n" +
            "Use the available tools to retrieve relevant promotion criteria and estimate timeline.",
            profile.toDescription(),
            careerAnalysis
        );

        String result = context.getClaudeClient().chat(systemPrompt, userMessage, context.getToolRegistry());
        log.info("PromotionCoachAgent completed plan ({} chars)", result.length());
        return result;
    }
}
