package com.careercoach.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG Pipeline — inspired by rag-workshop-ghc25.
 * Loads knowledge base, chunks documents, indexes them, and retrieves relevant context.
 */
public class RagPipeline {
    private static final Logger log = LoggerFactory.getLogger(RagPipeline.class);

    private final VectorStore vectorStore;
    private final DocumentChunker chunker;

    public RagPipeline() {
        this.vectorStore = new VectorStore();
        this.chunker = new DocumentChunker(600, 60);
    }

    /**
     * Loads all knowledge base files from resources/knowledge/
     */
    public void initialize() {
        log.info("Initializing RAG pipeline...");
        String[] knowledgeFiles = {
            "knowledge/engineering-levels.txt",
            "knowledge/promotion-criteria.txt",
            "knowledge/skills-framework.txt"
        };

        for (String file : knowledgeFiles) {
            try {
                String content = loadResource(file);
                List<Document> chunks = chunker.chunk(content, file);
                vectorStore.addDocuments(chunks);
                log.info("Loaded {} chunks from {}", chunks.size(), file);
            } catch (Exception e) {
                log.warn("Failed to load knowledge file: {}", file, e);
            }
        }

        vectorStore.buildIndex();
        log.info("RAG pipeline ready. Total chunks indexed: {}", vectorStore.size());
    }

    /**
     * Retrieves relevant context for a query.
     */
    public String retrieve(String query, int topK) {
        List<Document> results = vectorStore.search(query, topK);
        if (results.isEmpty()) {
            return "No relevant context found.";
        }

        return results.stream()
            .map(doc -> String.format("--- [%s] ---\n%s", doc.getSource(), doc.getContent()))
            .collect(Collectors.joining("\n\n"));
    }

    private String loadResource(String resourcePath) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) throw new RuntimeException("Resource not found: " + resourcePath);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }
}
