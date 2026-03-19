package com.careercoach.agent;

import com.careercoach.claude.ClaudeClient;
import com.careercoach.model.EngineerProfile;
import com.careercoach.rag.RagPipeline;
import com.careercoach.tools.ToolRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared context passed between agents in the multi-agent pipeline.
 */
public class AgentContext {
    private final ClaudeClient claudeClient;
    private final RagPipeline ragPipeline;
    private final ToolRegistry toolRegistry;
    private EngineerProfile profile;
    private final Map<String, String> agentOutputs = new HashMap<>();

    public AgentContext(ClaudeClient claudeClient, RagPipeline ragPipeline, ToolRegistry toolRegistry) {
        this.claudeClient = claudeClient;
        this.ragPipeline = ragPipeline;
        this.toolRegistry = toolRegistry;
    }

    public void setAgentOutput(String agentName, String output) {
        agentOutputs.put(agentName, output);
    }

    public String getAgentOutput(String agentName) {
        return agentOutputs.getOrDefault(agentName, "");
    }

    public ClaudeClient getClaudeClient() { return claudeClient; }
    public RagPipeline getRagPipeline() { return ragPipeline; }
    public ToolRegistry getToolRegistry() { return toolRegistry; }
    public EngineerProfile getProfile() { return profile; }
    public void setProfile(EngineerProfile profile) { this.profile = profile; }
}
