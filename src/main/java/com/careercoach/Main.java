package com.careercoach;

import com.careercoach.agent.AgentContext;
import com.careercoach.agent.OrchestratorAgent;
import com.careercoach.claude.ClaudeClient;
import com.careercoach.model.EngineerProfile;
import com.careercoach.model.PromotionReport;
import com.careercoach.rag.RagPipeline;
import com.careercoach.tools.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * AI Career Promotion Coach — Java Multi-Agent RAG System
 *
 * Architecture:
 *   - Multi-Agent: OrchestratorAgent → CareerAnalyzerAgent → PromotionCoachAgent
 *   - RAG Pipeline: TF-IDF vector store over engineering knowledge base
 *   - MCP-inspired Tools: ToolRegistry with career knowledge, skill gap, timeline tools
 *   - LLM: Claude via Anthropic API (claude-opus-4-6)
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        printBanner();

        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("ERROR: ANTHROPIC_API_KEY environment variable not set.");
            System.err.println("Set it with: export ANTHROPIC_API_KEY=your_key_here");
            System.exit(1);
        }

        // 1. Initialize RAG Pipeline
        System.out.println("[1/4] Initializing RAG knowledge base...");
        RagPipeline ragPipeline = new RagPipeline();
        ragPipeline.initialize();

        // 2. Register MCP-inspired Tools
        System.out.println("[2/4] Registering tools (MCP pattern)...");
        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(new CareerKnowledgeTool(ragPipeline));
        toolRegistry.register(new SkillGapTool(ragPipeline));
        toolRegistry.register(new PromotionTimelineTool(ragPipeline));
        System.out.println("  Tools registered: " + toolRegistry.getAll().stream()
            .map(t -> t.getName()).reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b));

        // 3. Initialize Claude Client and Agent Context
        System.out.println("[3/4] Connecting to Claude API...");
        ClaudeClient claude = new ClaudeClient(apiKey);
        AgentContext agentContext = new AgentContext(claude, ragPipeline, toolRegistry);

        // 4. Get Engineer Profile (from args or interactive)
        System.out.println("[4/4] Loading engineer profile...\n");
        EngineerProfile profile;
        if (args.length > 0 && args[0].equals("--demo")) {
            profile = createDemoProfile();
            System.out.println("Using demo profile: " + profile.getName());
        } else {
            profile = collectProfileInteractively();
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("Starting multi-agent analysis pipeline...");
        System.out.println("=".repeat(60));
        System.out.println("Profile: " + profile.getName() + " | " + profile.getCurrentLevel() + " → " + profile.getTargetLevel());
        System.out.println();

        // 5. Run Multi-Agent Pipeline
        long startTime = System.currentTimeMillis();
        OrchestratorAgent orchestrator = new OrchestratorAgent(agentContext);
        PromotionReport report = orchestrator.run(profile);
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("\n" + "=".repeat(60));
        System.out.println("PIPELINE COMPLETE in " + (duration / 1000) + "s");
        System.out.println("=".repeat(60));

        // 6. Output Report
        System.out.println("\n" + report.toText());

        // Save HTML report
        String htmlFile = profile.getName().replaceAll("\\s+", "_").toLowerCase() + "_promotion_report.html";
        saveHtmlReport(report, htmlFile);
        System.out.println("\nHTML report saved: " + htmlFile);
        System.out.println("\nDone! Run with --demo to use the sample profile.");
    }

    private static EngineerProfile collectProfileInteractively() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Engineer Profile Setup ===");
        System.out.print("Your name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Current level (e.g., L4, L5, Senior): ");
        String currentLevel = scanner.nextLine().trim();

        System.out.print("Target level (e.g., L5, L6, Staff): ");
        String targetLevel = scanner.nextLine().trim();

        System.out.print("Years at current level: ");
        String years = scanner.nextLine().trim();

        System.out.print("Current role title: ");
        String role = scanner.nextLine().trim();

        System.out.print("Company: ");
        String company = scanner.nextLine().trim();

        System.out.print("Discipline (backend/frontend/platform/ML/fullstack): ");
        String discipline = scanner.nextLine().trim();

        System.out.print("Technical skills (comma-separated): ");
        List<String> skills = Arrays.asList(scanner.nextLine().split(","));

        System.out.print("Recent projects (comma-separated): ");
        List<String> projects = Arrays.asList(scanner.nextLine().split(","));

        System.out.print("Biggest challenge toward promotion: ");
        String challenge = scanner.nextLine().trim();

        return new EngineerProfile(name, currentLevel, targetLevel, years, role,
            company, skills, projects, challenge, discipline);
    }

    private static EngineerProfile createDemoProfile() {
        return new EngineerProfile(
            "Shruti Akkala",
            "L4",
            "L5",
            "2.5 years",
            "Software Engineer II",
            "Acme Corp",
            Arrays.asList("Java", "Kubernetes", "System Design", "Distributed Systems", "REST APIs", "PostgreSQL", "Microservices"),
            Arrays.asList(
                "Designed and built a high-throughput event processing pipeline handling 50k events/sec",
                "Led system design for a new notification service used by 3 teams",
                "Architected microservices migration for the core billing module — reduced latency by 40%",
                "Mentored 2 junior engineers on system design best practices"
            ),
            "System design fundamentals are strong but I need to practice more complex distributed system scenarios and deepen breadth across caching, consensus algorithms, and large-scale data pipelines",
            "backend"
        );
    }

    private static void saveHtmlReport(PromotionReport report, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(report.toHtml());
            log.info("HTML report written to {}", filename);
        } catch (Exception e) {
            log.warn("Could not save HTML report: {}", e.getMessage());
        }
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║          AI CAREER PROMOTION COACH — JAVA                ║");
        System.out.println("║   Multi-Agent • RAG Pipeline • MCP-Inspired Tools        ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}
