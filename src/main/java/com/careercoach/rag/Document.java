package com.careercoach.rag;

public class Document {
    private final String id;
    private final String content;
    private final String source;
    private double similarityScore;

    public Document(String id, String content, String source) {
        this.id = id;
        this.content = content;
        this.source = source;
    }

    public String getId() { return id; }
    public String getContent() { return content; }
    public String getSource() { return source; }
    public double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }

    @Override
    public String toString() {
        return String.format("[%s] (%.3f) %s", source, similarityScore,
            content.length() > 100 ? content.substring(0, 100) + "..." : content);
    }
}
