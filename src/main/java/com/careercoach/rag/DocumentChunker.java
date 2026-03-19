package com.careercoach.rag;

import java.util.ArrayList;
import java.util.List;

public class DocumentChunker {

    private final int chunkSize;
    private final int overlapSize;

    public DocumentChunker(int chunkSize, int overlapSize) {
        this.chunkSize = chunkSize;
        this.overlapSize = overlapSize;
    }

    public DocumentChunker() {
        this(500, 50); // 500 chars with 50-char overlap
    }

    /**
     * Splits text into overlapping chunks. Tries to break at paragraph or sentence boundaries.
     */
    public List<Document> chunk(String text, String sourceFile) {
        List<Document> documents = new ArrayList<>();
        String[] paragraphs = text.split("\n\n+");
        int docIndex = 0;

        StringBuilder currentChunk = new StringBuilder();
        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) continue;

            if (currentChunk.length() + paragraph.length() > chunkSize && currentChunk.length() > 0) {
                documents.add(new Document(
                    sourceFile + "_chunk_" + docIndex++,
                    currentChunk.toString().trim(),
                    sourceFile
                ));
                // Overlap: keep last overlapSize chars
                String full = currentChunk.toString();
                currentChunk = new StringBuilder(
                    full.length() > overlapSize ? full.substring(full.length() - overlapSize) : full
                );
            }
            currentChunk.append(paragraph).append("\n\n");
        }

        if (currentChunk.length() > 0) {
            documents.add(new Document(
                sourceFile + "_chunk_" + docIndex,
                currentChunk.toString().trim(),
                sourceFile
            ));
        }

        return documents;
    }
}
