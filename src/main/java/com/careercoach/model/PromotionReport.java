package com.careercoach.model;

import java.time.LocalDate;
import java.util.List;

public class PromotionReport {
    private String engineerName;
    private String currentLevel;
    private String targetLevel;
    private String generatedDate;
    private String careerAnalysis;
    private String strengthsSummary;
    private List<String> skillGaps;
    private List<String> actionItems;
    private String promotionReadinessScore; // e.g., "6/10 — Developing"
    private String estimatedTimeline;
    private String coachingPlan;
    private String ragContext; // what knowledge base info was used

    public PromotionReport() {
        this.generatedDate = LocalDate.now().toString();
    }

    public String toHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>")
            .append("<meta charset='UTF-8'>")
            .append("<title>Promotion Report — ").append(engineerName).append("</title>")
            .append("<style>")
            .append("body{font-family:Arial,sans-serif;max-width:900px;margin:40px auto;padding:20px;color:#333;}")
            .append("h1{color:#1a1a2e;border-bottom:3px solid #4f46e5;padding-bottom:10px;}")
            .append("h2{color:#4f46e5;margin-top:30px;}")
            .append(".badge{display:inline-block;background:#4f46e5;color:white;padding:4px 12px;border-radius:20px;font-size:14px;}")
            .append(".card{background:#f8f9ff;border-left:4px solid #4f46e5;padding:16px;margin:12px 0;border-radius:0 8px 8px 0;}")
            .append(".gap{background:#fff3cd;border-left:4px solid #ffc107;padding:12px;margin:8px 0;border-radius:0 8px 8px 0;}")
            .append(".action{background:#d4edda;border-left:4px solid #28a745;padding:12px;margin:8px 0;border-radius:0 8px 8px 0;}")
            .append("</style></head><body>");

        html.append("<h1>Promotion Readiness Report</h1>");
        html.append("<p><strong>Engineer:</strong> ").append(engineerName)
            .append(" &nbsp; <span class='badge'>").append(currentLevel).append(" → ").append(targetLevel).append("</span>")
            .append(" &nbsp; <strong>Generated:</strong> ").append(generatedDate).append("</p>");

        html.append("<h2>Readiness Assessment</h2>");
        html.append("<div class='card'><strong>Score: ").append(promotionReadinessScore).append("</strong><br>")
            .append("<strong>Estimated Timeline: </strong>").append(estimatedTimeline).append("</div>");

        html.append("<h2>Career Analysis</h2>");
        html.append("<div class='card'>").append(careerAnalysis.replace("\n", "<br>")).append("</div>");

        html.append("<h2>Strengths</h2>");
        html.append("<div class='card'>").append(strengthsSummary.replace("\n", "<br>")).append("</div>");

        html.append("<h2>Skill Gaps to Close</h2>");
        if (skillGaps != null) {
            for (String gap : skillGaps) {
                html.append("<div class='gap'>").append(gap).append("</div>");
            }
        }

        html.append("<h2>Action Items</h2>");
        if (actionItems != null) {
            for (int i = 0; i < actionItems.size(); i++) {
                html.append("<div class='action'><strong>").append(i + 1).append(".</strong> ").append(actionItems.get(i)).append("</div>");
            }
        }

        html.append("<h2>Coaching Plan</h2>");
        html.append("<div class='card'>").append(coachingPlan.replace("\n", "<br>")).append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    public String toText() {
        return String.format(
            "=== PROMOTION REPORT: %s ===\n%s → %s | Generated: %s\n\n" +
            "READINESS: %s\nESTIMATED TIMELINE: %s\n\n" +
            "ANALYSIS:\n%s\n\nSTRENGTHS:\n%s\n\n" +
            "SKILL GAPS:\n%s\n\nACTION ITEMS:\n%s\n\nCOACHING PLAN:\n%s",
            engineerName, currentLevel, targetLevel, generatedDate,
            promotionReadinessScore, estimatedTimeline,
            careerAnalysis, strengthsSummary,
            skillGaps != null ? String.join("\n- ", skillGaps) : "None identified",
            actionItems != null ? String.join("\n- ", actionItems) : "None",
            coachingPlan
        );
    }

    // Getters and setters
    public String getEngineerName() { return engineerName; }
    public void setEngineerName(String engineerName) { this.engineerName = engineerName; }
    public String getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(String currentLevel) { this.currentLevel = currentLevel; }
    public String getTargetLevel() { return targetLevel; }
    public void setTargetLevel(String targetLevel) { this.targetLevel = targetLevel; }
    public String getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(String generatedDate) { this.generatedDate = generatedDate; }
    public String getCareerAnalysis() { return careerAnalysis; }
    public void setCareerAnalysis(String careerAnalysis) { this.careerAnalysis = careerAnalysis; }
    public String getStrengthsSummary() { return strengthsSummary; }
    public void setStrengthsSummary(String strengthsSummary) { this.strengthsSummary = strengthsSummary; }
    public List<String> getSkillGaps() { return skillGaps; }
    public void setSkillGaps(List<String> skillGaps) { this.skillGaps = skillGaps; }
    public List<String> getActionItems() { return actionItems; }
    public void setActionItems(List<String> actionItems) { this.actionItems = actionItems; }
    public String getPromotionReadinessScore() { return promotionReadinessScore; }
    public void setPromotionReadinessScore(String promotionReadinessScore) { this.promotionReadinessScore = promotionReadinessScore; }
    public String getEstimatedTimeline() { return estimatedTimeline; }
    public void setEstimatedTimeline(String estimatedTimeline) { this.estimatedTimeline = estimatedTimeline; }
    public String getCoachingPlan() { return coachingPlan; }
    public void setCoachingPlan(String coachingPlan) { this.coachingPlan = coachingPlan; }
    public String getRagContext() { return ragContext; }
    public void setRagContext(String ragContext) { this.ragContext = ragContext; }
}
