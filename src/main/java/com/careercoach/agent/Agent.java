package com.careercoach.agent;

/**
 * Base Agent interface for the multi-agent system.
 * Inspired by ai-promotion-coach-multi-agent architecture.
 */
public interface Agent {
    String getName();
    String getDescription();
    String run(AgentContext context) throws Exception;
}
