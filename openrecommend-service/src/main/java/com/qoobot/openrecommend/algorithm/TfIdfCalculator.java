package com.qoobot.openrecommend.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TF-IDF计算器
 * 用于文本特征提取和关键词提取
 */
@Slf4j
@Component
public class TfIdfCalculator {

    /**
     * 计算TF-IDF值
     *
     * @param term 词语
     * @param document 文档内容（分词后的词语列表）
     * @param corpus 语料库（所有文档的词语列表）
     * @return TF-IDF值
     */
    public double calculate(String term, List<String> document, List<List<String>> corpus) {
        double tf = calculateTermFrequency(term, document);
        double idf = calculateInverseDocumentFrequency(term, corpus);
        return tf * idf;
    }

    /**
     * 计算所有词语的TF-IDF值
     *
     * @param document 文档内容（分词后的词语列表）
     * @param corpus 语料库
     * @return 词语到TF-IDF值的映射
     */
    public Map<String, Double> calculateAll(List<String> document, List<List<String>> corpus) {
        Map<String, Double> tfidfMap = new HashMap<>();

        Set<String> uniqueTerms = new HashSet<>(document);

        for (String term : uniqueTerms) {
            double tfidf = calculate(term, document, corpus);
            tfidfMap.put(term, tfidf);
        }

        return tfidfMap;
    }

    /**
     * 提取关键词
     *
     * @param document 文档内容（分词后的词语列表）
     * @param corpus 语料库
     * @param topN 返回前N个关键词
     * @return 关键词列表（按TF-IDF值降序）
     */
    public List<String> extractKeywords(List<String> document, List<List<String>> corpus, int topN) {
        Map<String, Double> tfidfMap = calculateAll(document, corpus);

        return tfidfMap.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 提取关键词及其权重
     *
     * @param document 文档内容（分词后的词语列表）
     * @param corpus 语料库
     * @param topN 返回前N个关键词
     * @return 关键词及其TF-IDF值的映射（按权重降序）
     */
    public Map<String, Double> extractKeywordsWithWeights(List<String> document,
                                                          List<List<String>> corpus,
                                                          int topN) {
        Map<String, Double> tfidfMap = calculateAll(document, corpus);

        return tfidfMap.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(topN)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * 计算词频（TF）
     *
     * @param term 词语
     * @param document 文档内容（分词后的词语列表）
     * @return TF值
     */
    public double calculateTermFrequency(String term, List<String> document) {
        if (document == null || document.isEmpty()) {
            return 0.0;
        }

        long count = document.stream()
                .filter(t -> t.equals(term))
                .count();

        return (double) count / document.size();
    }

    /**
     * 计算逆文档频率（IDF）
     *
     * @param term 词语
     * @param corpus 语料库（所有文档的词语列表）
     * @return IDF值
     */
    public double calculateInverseDocumentFrequency(String term, List<List<String>> corpus) {
        if (corpus == null || corpus.isEmpty()) {
            return 0.0;
        }

        int totalDocuments = corpus.size();
        int documentsWithTerm = 0;

        for (List<String> document : corpus) {
            if (document.contains(term)) {
                documentsWithTerm++;
            }
        }

        if (documentsWithTerm == 0) {
            return 0.0;
        }

        // 平滑处理，避免分母为0
        return Math.log((double) totalDocuments / documentsWithTerm) + 1.0;
    }

    /**
     * 简单的中文分词（基于空格和标点符号）
     * 注意：生产环境建议使用专业的分词库（如HanLP、jieba等）
     *
     * @param text 文本
     * @return 分词结果
     */
    public List<String> simpleTokenize(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        // 简单的分词逻辑：按空格、标点符号分割
        String[] tokens = text.split("[\\s,，。！？；：\"'（）\\[\\]{}、]+");

        return Arrays.stream(tokens)
                .filter(t -> t.length() >= 2) // 过滤掉单字
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 计算两个文档的相似度（基于TF-IDF向量）
     *
     * @param doc1 文档1
     * @param doc2 文档2
     * @param corpus 语料库
     * @return 相似度（0-1之间）
     */
    public double calculateDocumentSimilarity(List<String> doc1, List<String> doc2, List<List<String>> corpus) {
        Map<String, Double> tfidf1 = calculateAll(doc1, corpus);
        Map<String, Double> tfidf2 = calculateAll(doc2, corpus);

        Set<String> allTerms = new HashSet<>();
        allTerms.addAll(tfidf1.keySet());
        allTerms.addAll(tfidf2.keySet());

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String term : allTerms) {
            double v1 = tfidf1.getOrDefault(term, 0.0);
            double v2 = tfidf2.getOrDefault(term, 0.0);

            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 批量计算文档的TF-IDF向量
     *
     * @param documents 文档列表
     * @return 每个文档的TF-IDF向量
     */
    public List<Map<String, Double>> calculateBatch(List<List<String>> documents) {
        List<Map<String, Double>> results = new ArrayList<>();

        for (List<String> document : documents) {
            Map<String, Double> tfidf = calculateAll(document, documents);
            results.add(tfidf);
        }

        return results;
    }

    /**
     * 更新语料库
     * 将新文档添加到语料库中
     *
     * @param corpus 原语料库
     * @param newDocuments 新文档列表
     * @return 更新后的语料库
     */
    public List<List<String>> updateCorpus(List<List<String>> corpus, List<List<String>> newDocuments) {
        List<List<String>> updatedCorpus = new ArrayList<>(corpus);
        updatedCorpus.addAll(newDocuments);
        return updatedCorpus;
    }
}
