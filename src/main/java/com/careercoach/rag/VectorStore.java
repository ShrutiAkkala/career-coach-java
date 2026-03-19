package com.careercoach.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory vector store using TF-IDF with cosine similarity.
 * This implements the RAG retrieval layer inspired by rag-workshop-ghc25.
 */
public class VectorStore {
    private static final Logger log = LoggerFactory.getLogger(VectorStore.class);

    private final List<Document> documents = new ArrayList<>();
    private final Map<String, Map<String, Double>> tfidfVectors = new HashMap<>();
    private Map<String, Double> idf = new HashMap<>();
    private boolean indexed = false;

    public void addDocuments(List<Document> docs) {
        documents.addAll(docs);
        indexed = false;
    }

    public void buildIndex() {
        log.debug("Building TF-IDF index for {} documents", documents.size());
        Map<String, Integer> docFrequency = new HashMap<>();

        // Compute TF for each document
        Map<String, Map<String, Double>> tfMap = new HashMap<>();
        for (Document doc : documents) {
            Map<String, Double> tf = computeTF(doc.getContent());
            tfMap.put(doc.getId(), tf);
            for (String term : tf.keySet()) {
                docFrequency.merge(term, 1, Integer::sum);
            }
        }

        // Compute IDF
        int N = documents.size();
        idf = new HashMap<>();
        for (Map.Entry<String, Integer> entry : docFrequency.entrySet()) {
            idf.put(entry.getKey(), Math.log((double) N / entry.getValue()) + 1.0);
        }

        // Compute TF-IDF vectors
        tfidfVectors.clear();
        for (Document doc : documents) {
            Map<String, Double> tf = tfMap.get(doc.getId());
            Map<String, Double> tfidf = new HashMap<>();
            for (Map.Entry<String, Double> entry : tf.entrySet()) {
                double idfVal = idf.getOrDefault(entry.getKey(), 1.0);
                tfidf.put(entry.getKey(), entry.getValue() * idfVal);
            }
            tfidfVectors.put(doc.getId(), tfidf);
        }

        indexed = true;
        log.debug("Index built. Vocabulary size: {}", idf.size());
    }

    /**
     * Retrieve top-k most relevant documents for a query.
     */
    public List<Document> search(String query, int topK) {
        if (!indexed) buildIndex();

        Map<String, Double> queryVector = computeQueryVector(query);

        List<Document> results = new ArrayList<>(documents);
        for (Document doc : results) {
            Map<String, Double> docVector = tfidfVectors.get(doc.getId());
            double score = cosineSimilarity(queryVector, docVector);
            doc.setSimilarityScore(score);
        }

        return results.stream()
            .sorted(Comparator.comparingDouble(Document::getSimilarityScore).reversed())
            .limit(topK)
            .filter(d -> d.getSimilarityScore() > 0.0)
            .collect(Collectors.toList());
    }

    private Map<String, Double> computeTF(String text) {
        List<String> tokens = tokenize(text);
        Map<String, Integer> freq = new HashMap<>();
        for (String token : tokens) {
            freq.merge(token, 1, Integer::sum);
        }
        Map<String, Double> tf = new HashMap<>();
        int total = tokens.size();
        for (Map.Entry<String, Integer> entry : freq.entrySet()) {
            tf.put(entry.getKey(), (double) entry.getValue() / total);
        }
        return tf;
    }

    private Map<String, Double> computeQueryVector(String query) {
        Map<String, Double> tf = computeTF(query);
        Map<String, Double> queryVec = new HashMap<>();
        for (Map.Entry<String, Double> entry : tf.entrySet()) {
            double idfVal = idf.getOrDefault(entry.getKey(), 1.0);
            queryVec.put(entry.getKey(), entry.getValue() * idfVal);
        }
        return queryVec;
    }

    private double cosineSimilarity(Map<String, Double> a, Map<String, Double> b) {
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (Map.Entry<String, Double> entry : a.entrySet()) {
            double bVal = b.getOrDefault(entry.getKey(), 0.0);
            dot += entry.getValue() * bVal;
            normA += entry.getValue() * entry.getValue();
        }
        for (double v : b.values()) normB += v * v;
        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private List<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", " ")
            .split("\\s+"))
            .filter(t -> t.length() > 2)
            .filter(t -> !STOP_WORDS.contains(t))
            .collect(Collectors.toList());
    }

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "the", "and", "for", "are", "but", "not", "you", "all", "can", "her", "was",
        "one", "our", "out", "day", "get", "has", "him", "his", "how", "its", "may",
        "new", "now", "own", "way", "who", "did", "with", "that", "this", "they",
        "from", "have", "been", "will", "more", "also", "into", "then", "than",
        "when", "what", "their", "there", "which", "about", "would", "could", "each"
    ));

    public int size() { return documents.size(); }
}
