package com.careercoach.model;

import java.util.List;

public class EngineerProfile {
    private String name;
    private String currentLevel;
    private String targetLevel;
    private String yearsAtCurrentLevel;
    private String currentRole;
    private String company;
    private List<String> technicalSkills;
    private List<String> recentProjects;
    private String biggestChallenge;
    private String discipline; // e.g., backend, frontend, platform, ML

    // Constructors
    public EngineerProfile() {}

    public EngineerProfile(String name, String currentLevel, String targetLevel,
                           String yearsAtCurrentLevel, String currentRole, String company,
                           List<String> technicalSkills, List<String> recentProjects,
                           String biggestChallenge, String discipline) {
        this.name = name;
        this.currentLevel = currentLevel;
        this.targetLevel = targetLevel;
        this.yearsAtCurrentLevel = yearsAtCurrentLevel;
        this.currentRole = currentRole;
        this.company = company;
        this.technicalSkills = technicalSkills;
        this.recentProjects = recentProjects;
        this.biggestChallenge = biggestChallenge;
        this.discipline = discipline;
    }

    public String toDescription() {
        return String.format(
            "Engineer: %s\nCurrent Level: %s | Target Level: %s\nYears at current level: %s\n" +
            "Role: %s at %s\nDiscipline: %s\nTechnical Skills: %s\nRecent Projects: %s\nBiggest Challenge: %s",
            name, currentLevel, targetLevel, yearsAtCurrentLevel,
            currentRole, company, discipline,
            String.join(", ", technicalSkills),
            String.join(", ", recentProjects),
            biggestChallenge
        );
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(String currentLevel) { this.currentLevel = currentLevel; }
    public String getTargetLevel() { return targetLevel; }
    public void setTargetLevel(String targetLevel) { this.targetLevel = targetLevel; }
    public String getYearsAtCurrentLevel() { return yearsAtCurrentLevel; }
    public void setYearsAtCurrentLevel(String yearsAtCurrentLevel) { this.yearsAtCurrentLevel = yearsAtCurrentLevel; }
    public String getCurrentRole() { return currentRole; }
    public void setCurrentRole(String currentRole) { this.currentRole = currentRole; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public List<String> getTechnicalSkills() { return technicalSkills; }
    public void setTechnicalSkills(List<String> technicalSkills) { this.technicalSkills = technicalSkills; }
    public List<String> getRecentProjects() { return recentProjects; }
    public void setRecentProjects(List<String> recentProjects) { this.recentProjects = recentProjects; }
    public String getBiggestChallenge() { return biggestChallenge; }
    public void setBiggestChallenge(String biggestChallenge) { this.biggestChallenge = biggestChallenge; }
    public String getDiscipline() { return discipline; }
    public void setDiscipline(String discipline) { this.discipline = discipline; }
}
