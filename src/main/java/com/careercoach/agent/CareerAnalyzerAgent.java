package com.careercoach.agent;

import com.careercoach.model.EngineerProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent 1: Analyzes the engineer's profile against level expectations.
 * Uses RAG tools to retrieve relevant knowledge base content.
 */
public class CareerAnalyzerAgent implements Agent {
    private static final Logger log = LoggerFactory.getLogger(CareerAnalyzerAgent.class);

    @Override
    public String getName() { return "CareerAnalyzerAgent"; }

    @Override
    public String getDescription() {
        return "Analyzes engineer profile against level frameworks and promotion criteria";
    }

    @Override
    public String run(AgentContext context) throws Exception {
        log.info("CareerAnalyzerAgent starting analysis...");
        EngineerProfile profile = context.getProfile();

        String systemPrompt = """
            You are a senior engineering career analyst. Your job is to rigorously analyze an engineer's
            profile against the expectations for their current and target level.

            Use the retrieve_career_knowledge tool to look up:
            1. The expectations for their current level
            2. The expectations for their target level
            3. The skill gap between the two

            Then use analyze_skill_gaps to get a structured gap analysis.

            Be specific and honest. Don't sugarcoat gaps. Reference specific level criteria.
            Focus on: technical skills, leadership behaviors, scope of impact, and communication.

            Output a structured analysis with:
            - Current Level Assessment (what they demonstrate vs. what's expected)
            - Target Level Requirements (what L->L+1 actually requires)
            - Gap Analysis (specific named gaps, not generic advice)
            - Key Strengths (evidence-based, not flattery)
            """;

        String userMessage = String.format(
            "Analyze this engineer's profile for promotion readiness:\n\n%s\n\n" +
            "Use the knowledge retrieval tools to ground your analysis in the level frameworks.",
            profile.toDescription()
        );

        String result = context.getClaudeClient().chat(systemPrompt, userMessage, context.getToolRegistry());
        log.info("CareerAnalyzerAgent completed analysis ({} chars)", result.length());
        return result;
    }
}
