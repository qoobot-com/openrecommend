package com.qoobot.openrecommend.algorithm;

import com.qoobot.openrecommend.entity.Image;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 视觉特征提取器
 * 用于提取图片的颜色特征和主色调
 */
@Slf4j
@Component
public class VisualFeatureExtractor {

    /**
     * 提取颜色直方图
     *
     * @param image 图片对象
     * @param bins 直方图箱数（默认256）
     * @return 颜色直方图 [R直方图, G直方图, B直方图]
     */
    public Map<String, int[]> extractColorHistogram(Image image, int bins) {
        if (image == null) {
            return Collections.emptyMap();
        }

        int width = image.getWidth() != null ? image.getWidth() : 0;
        int height = image.getHeight() != null ? image.getHeight() : 0;

        if (width == 0 || height == 0) {
            log.warn("图片尺寸无效: width={}, height={}", width, height);
            return Collections.emptyMap();
        }

        // 这里简化处理，实际应用中需要从图片URL读取像素数据
        // 模拟提取颜色直方图
        int[] rHistogram = new int[bins];
        int[] gHistogram = new int[bins];
        int[] bHistogram = new int[bins];

        // 模拟随机填充（实际应用中应该读取真实图片数据）
        Random random = new Random(image.getId() != null ? image.getId().hashCode() : 0);
        for (int i = 0; i < bins; i++) {
            rHistogram[i] = random.nextInt(100);
            gHistogram[i] = random.nextInt(100);
            bHistogram[i] = random.nextInt(100);
        }

        Map<String, int[]> histogram = new HashMap<>();
        histogram.put("R", rHistogram);
        histogram.put("G", gHistogram);
        histogram.put("B", bHistogram);

        return histogram;
    }

    /**
     * 提取颜色直方图（默认256箱）
     */
    public Map<String, int[]> extractColorHistogram(Image image) {
        return extractColorHistogram(image, 256);
    }

    /**
     * 提取主色调
     *
     * @param image 图片对象
     * @param topN 返回前N个主色调
     * @return 主色调列表（十六进制颜色代码）
     */
    public List<String> extractDominantColors(Image image, int topN) {
        if (image == null) {
            return Collections.emptyList();
        }

        // 这里简化处理，实际应用中需要从图片URL读取像素数据并分析
        // 使用简单的量化方法提取主色调

        // 模拟提取主色调（实际应用中应该读取真实图片数据）
        Random random = new Random(image.getId() != null ? image.getId().hashCode() : 0);

        // 定义一些常见颜色作为候选
        String[] commonColors = {
                "#FF5733", "#C70039", "#900C3F", "#581845",
                "#FFC300", "#FF5733", "#C70039", "#DAF7A6",
                "#58C9B2", "#519D9E", "#2E86C1", "#1B4F72",
                "#1C2833", "#F4D03F", "#B7950B", "#7D6608"
        };

        // 随机选择topN个颜色作为主色调
        List<String> colors = new ArrayList<>();
        Set<Integer> indices = new HashSet<>();

        while (colors.size() < topN && indices.size() < commonColors.length) {
            int idx = random.nextInt(commonColors.length);
            if (indices.add(idx)) {
                colors.add(commonColors[idx]);
            }
        }

        return colors;
    }

    /**
     * 提取主色调（默认返回5个）
     */
    public List<String> extractDominantColors(Image image) {
        return extractDominantColors(image, 5);
    }

    /**
     * 从颜色直方图提取主色调
     *
     * @param histogram 颜色直方图
     * @param topN 返回前N个主色调
     * @return 主色调列表（十六进制颜色代码）
     */
    public List<String> extractDominantColorsFromHistogram(Map<String, int[]> histogram, int topN) {
        if (histogram == null || histogram.isEmpty()) {
            return Collections.emptyList();
        }

        int[] rHistogram = histogram.get("R");
        int[] gHistogram = histogram.get("G");
        int[] bHistogram = histogram.get("B");

        if (rHistogram == null || gHistogram == null || bHistogram == null) {
            return Collections.emptyList();
        }

        // 找出每个通道中频率最高的bin
        List<Map.Entry<Integer, Integer>> topR = getTopBins(rHistogram, topN);
        List<Map.Entry<Integer, Integer>> topG = getTopBins(gHistogram, topN);
        List<Map.Entry<Integer, Integer>> topB = getTopBins(bHistogram, topN);

        List<String> dominantColors = new ArrayList<>();

        // 组合RGB通道的峰值颜色
        for (int i = 0; i < Math.min(topN, topR.size()); i++) {
            int r = topR.get(i).getKey();
            int g = topG.get(i).getKey();
            int b = topB.get(i).getKey();

            String hexColor = String.format("#%02X%02X%02X", r, g, b);
            dominantColors.add(hexColor);
        }

        return dominantColors;
    }

    /**
     * 计算两张图片的颜色相似度
     *
     * @param hist1 图片1的颜色直方图
     * @param hist2 图片2的颜色直方图
     * @return 相似度（0-1之间，1表示完全相同）
     */
    public double calculateColorSimilarity(Map<String, int[]> hist1, Map<String, int[]> hist2) {
        if (hist1 == null || hist2 == null || hist1.isEmpty() || hist2.isEmpty()) {
            return 0.0;
        }

        int[] r1 = hist1.get("R");
        int[] g1 = hist1.get("G");
        int[] b1 = hist1.get("B");
        int[] r2 = hist2.get("R");
        int[] g2 = hist2.get("G");
        int[] b2 = hist2.get("B");

        if (r1 == null || g1 == null || b1 == null ||
            r2 == null || g2 == null || b2 == null) {
            return 0.0;
        }

        // 计算R、G、B三个通道的相似度
        double rSimilarity = calculateHistogramSimilarity(r1, r2);
        double gSimilarity = calculateHistogramSimilarity(g1, g2);
        double bSimilarity = calculateHistogramSimilarity(b1, b2);

        // 平均相似度
        return (rSimilarity + gSimilarity + bSimilarity) / 3.0;
    }

    /**
     * 计算主色调相似度
     *
     * @param colors1 图片1的主色调列表
     * @param colors2 图片2的主色调列表
     * @return 相似度（0-1之间）
     */
    public double calculateDominantColorSimilarity(List<String> colors1, List<String> colors2) {
        if (colors1 == null || colors2 == null || colors1.isEmpty() || colors2.isEmpty()) {
            return 0.0;
        }

        // 计算两个主色调列表的Jaccard相似度
        Set<String> set1 = new HashSet<>(colors1);
        Set<String> set2 = new HashSet<>(colors2);

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    /**
     * 计算图片亮度（基于RGB值）
     *
     * @param r 红色值（0-255）
     * @param g 绿色值（0-255）
     * @param b 蓝色值（0-255）
     * @return 亮度值（0-255）
     */
    public double calculateBrightness(int r, int g, int b) {
        // 使用亮度公式：0.299R + 0.587G + 0.114B
        return 0.299 * r + 0.587 * g + 0.114 * b;
    }

    /**
     * 判断图片是亮色还是暗色
     *
     * @param histogram 颜色直方图
     * @return "bright" 或 "dark"
     */
    public String judgeBrightness(Map<String, int[]> histogram) {
        if (histogram == null || histogram.isEmpty()) {
            return "unknown";
        }

        int[] rHistogram = histogram.get("R");
        int[] gHistogram = histogram.get("G");
        int[] bHistogram = histogram.get("B");

        if (rHistogram == null || gHistogram == null || bHistogram == null) {
            return "unknown";
        }

        // 计算平均亮度
        double totalBrightness = 0.0;
        int totalPixels = 0;

        for (int i = 0; i < rHistogram.length; i++) {
            double brightness = calculateBrightness(i, i, i);
            totalBrightness += brightness * (rHistogram[i] + gHistogram[i] + bHistogram[i]);
            totalPixels += (rHistogram[i] + gHistogram[i] + bHistogram[i]);
        }

        if (totalPixels == 0) {
            return "unknown";
        }

        double avgBrightness = totalBrightness / totalPixels;

        return avgBrightness > 128 ? "bright" : "dark";
    }

    /**
     * 获取直方图中频率最高的bin
     */
    private List<Map.Entry<Integer, Integer>> getTopBins(int[] histogram, int topN) {
        return Arrays.stream(histogram)
                .boxed()
                .collect(Collectors.toMap(
                        i -> i,
                        i -> histogram[i],
                        (e1, e2) -> e1
                ))
                .entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * 计算两个直方图的相似度（使用余弦相似度）
     */
    private double calculateHistogramSimilarity(int[] hist1, int[] hist2) {
        if (hist1.length != hist2.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < hist1.length; i++) {
            dotProduct += hist1[i] * hist2[i];
            norm1 += hist1[i] * hist1[i];
            norm2 += hist2[i] * hist2[i];
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 将十六进制颜色转换为RGB数组
     *
     * @param hexColor 十六进制颜色代码（如"#FF5733"）
     * @return RGB数组 [r, g, b]
     */
    public int[] hexToRgb(String hexColor) {
        if (hexColor == null || hexColor.isEmpty() || hexColor.charAt(0) != '#') {
            return new int[]{0, 0, 0};
        }

        try {
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);
            return new int[]{r, g, b};
        } catch (Exception e) {
            log.error("解析颜色失败: {}", hexColor, e);
            return new int[]{0, 0, 0};
        }
    }

    /**
     * 将RGB数组转换为十六进制颜色代码
     *
     * @param r 红色值（0-255）
     * @param g 绿色值（0-255）
     * @param b 蓝色值（0-255）
     * @return 十六进制颜色代码
     */
    public String rgbToHex(int r, int g, int b) {
        return String.format("#%02X%02X%02X",
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, b)));
    }
}
