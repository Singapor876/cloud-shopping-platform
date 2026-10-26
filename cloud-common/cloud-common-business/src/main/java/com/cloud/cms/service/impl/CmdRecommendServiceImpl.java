package com.cloud.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.cms.domain.CmsProduct;
import com.cloud.cms.domain.CmdRecommendWeight;
import com.cloud.cms.enums.ProductStatus;
import com.cloud.cms.service.ICmdRecommendService;
import com.cloud.cms.service.ICmdRecommendWeightService;
import com.cloud.cms.service.ICmsProductService;
import com.cloud.common.core.utils.StringUtils;
import com.cloud.ums.domain.UmsBrowseHistory;
import com.cloud.ums.domain.UmsFavorite;
import com.cloud.ums.domain.UmsProductReview;
import com.cloud.ums.service.IUmsBrowseHistoryService;
import com.cloud.ums.service.IUmsFavoriteService;
import com.cloud.ums.service.IUmsProductReviewService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Recommend Product Service Implementation
 */
@Service
public class CmdRecommendServiceImpl implements ICmdRecommendService {

    @Resource
    private ICmdRecommendWeightService recommendWeightService;
    @Resource
    private IUmsBrowseHistoryService browseHistoryService;
    @Resource
    private IUmsFavoriteService favoriteService;
    @Resource
    private IUmsProductReviewService reviewService;
    @Resource
    private ICmsProductService cmsProductService;

    @Override
    public List<CmsProduct> recommendProducts(String userId) {
        CmdRecommendWeight weight = recommendWeightService.getOrInitWeight();
        Map<String, Double> popularityScoreMap = buildPopularityScore(weight);

        // 先取所有上架商品，后续推荐和兜底都只从这里面选
        List<CmsProduct> availableProducts = cmsProductService.list(new LambdaQueryWrapper<CmsProduct>()
                .eq(CmsProduct::getProductStatus, ProductStatus.UNSOLD.getValue())
                .orderByDesc(CmsProduct::getUpdateTime));
        if (availableProducts == null || availableProducts.isEmpty()) {
            return new ArrayList<>();
        }

        // 没有登录用户时，直接走热门兜底
        if (StringUtils.isEmpty(userId) || "0".equals(userId)) {
            return completeRecommendations(new ArrayList<>(), availableProducts, new HashSet<>(), popularityScoreMap);
        }

        // 汇总当前用户和其他用户的商品行为分数
        Map<String, Map<String, Double>> userProductScores = buildUserProductScores(weight);
        Map<String, Double> currentUserScores = userProductScores.getOrDefault(userId, new HashMap<>());
        Set<String> currentInteractedProductIds = new HashSet<>(currentUserScores.keySet());

        // 当前用户没有行为数据时，直接按热门商品兜底
        if (currentUserScores.isEmpty()) {
            return completeRecommendations(new ArrayList<>(), availableProducts, currentInteractedProductIds, popularityScoreMap);
        }

        // 计算和其他用户的相似度，找出相近用户
        Map<String, Double> similarityMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : userProductScores.entrySet()) {
            String otherUserId = entry.getKey();
            if (userId.equals(otherUserId)) {
                continue;
            }
            double similarity = cosineSimilarity(currentUserScores, entry.getValue());
            if (similarity > 0) {
                similarityMap.put(otherUserId, similarity);
            }
        }

        if (similarityMap.isEmpty()) {
            return completeRecommendations(new ArrayList<>(), availableProducts, currentInteractedProductIds, popularityScoreMap);
        }

        // 根据相似用户的行为，为候选商品累积分数
        Map<String, Double> candidateScoreMap = new HashMap<>();
        List<Map.Entry<String, Double>> topSimilarUsers = similarityMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        for (Map.Entry<String, Double> similarUserEntry : topSimilarUsers) {
            Map<String, Double> similarUserScores = userProductScores.get(similarUserEntry.getKey());
            if (similarUserScores == null || similarUserScores.isEmpty()) {
                continue;
            }
            for (Map.Entry<String, Double> productScoreEntry : similarUserScores.entrySet()) {
                String productId = productScoreEntry.getKey();
                if (currentInteractedProductIds.contains(productId)) {
                    continue;
                }
                double score = similarUserEntry.getValue() * productScoreEntry.getValue();
                candidateScoreMap.merge(productId, score, Double::sum);
            }
        }

        Map<String, CmsProduct> productMap = availableProducts.stream().collect(Collectors.toMap(CmsProduct::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

        List<CmsProduct> result = new ArrayList<>();
        candidateScoreMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    CmsProduct product = productMap.get(entry.getKey());
                    if (product != null) {
                        product.setRecommendScore(BigDecimal.valueOf(entry.getValue()));
                        result.add(product);
                    }
                });

        return completeRecommendations(result, availableProducts, currentInteractedProductIds, popularityScoreMap);
    }

    private Map<String, Map<String, Double>> buildUserProductScores(CmdRecommendWeight weight) {
        Map<String, Map<String, Double>> userProductScores = new HashMap<>();

        // 浏览记录按浏览权重计分
        List<UmsBrowseHistory> browseHistoryList = browseHistoryService.list();
        if (browseHistoryList != null) {
            for (UmsBrowseHistory browseHistory : browseHistoryList) {
                addScore(userProductScores, browseHistory.getUserId(), browseHistory.getProductId(), weight.getBrowseWeight());
            }
        }

        // 收藏记录按收藏权重计分
        List<UmsFavorite> favoriteList = favoriteService.list();
        if (favoriteList != null) {
            for (UmsFavorite favorite : favoriteList) {
                addScore(userProductScores, favorite.getUserId(), favorite.getProductId(), weight.getFavoriteWeight());
            }
        }

        // 评价记录按星级权重计分
        List<UmsProductReview> reviewList = reviewService.list();
        if (reviewList != null) {
            for (UmsProductReview review : reviewList) {
                addScore(userProductScores, review.getUserId(), review.getProductId(), getReviewWeight(weight, review.getRating()));
            }
        }

        return userProductScores;
    }

    private Map<String, Double> buildPopularityScore(CmdRecommendWeight weight) {
        Map<String, Double> popularityScore = new HashMap<>();

        // 热度分数同样来自浏览、收藏、评价
        List<UmsBrowseHistory> browseHistoryList = browseHistoryService.list();
        if (browseHistoryList != null) {
            for (UmsBrowseHistory browseHistory : browseHistoryList) {
                addPopularity(popularityScore, browseHistory.getProductId(), weight.getBrowseWeight());
            }
        }

        List<UmsFavorite> favoriteList = favoriteService.list();
        if (favoriteList != null) {
            for (UmsFavorite favorite : favoriteList) {
                addPopularity(popularityScore, favorite.getProductId(), weight.getFavoriteWeight());
            }
        }

        List<UmsProductReview> reviewList = reviewService.list();
        if (reviewList != null) {
            for (UmsProductReview review : reviewList) {
                addPopularity(popularityScore, review.getProductId(), getReviewWeight(weight, review.getRating()));
            }
        }

        return popularityScore;
    }

    private List<CmsProduct> fallbackProducts(List<CmsProduct> availableProducts, Set<String> excludedProductIds, int limit, Map<String, Double> popularityScoreMap) {
        // 先按热度排序，再按更新时间排序，确保兜底结果稳定
        return availableProducts.stream()
                .filter(product -> !excludedProductIds.contains(product.getId()))
                .sorted(Comparator
                        .comparing((CmsProduct product) -> popularityScoreMap.getOrDefault(product.getId(), 0D)).reversed()
                        .thenComparing(CmsProduct::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .peek(product -> product.setRecommendScore(BigDecimal.ZERO))
                .collect(Collectors.toList());
    }

    private List<CmsProduct> completeRecommendations(List<CmsProduct> result,
                                                     List<CmsProduct> availableProducts,
                                                     Set<String> excludedProductIds,
                                                     Map<String, Double> popularityScoreMap) {
        // 第一层兜底，补热门，但仍优先避开用户已经交互过的商品
        if (result.size() < 5) {
            List<CmsProduct> fallback = fallbackProducts(availableProducts, excludedProductIds, 5 - result.size(), popularityScoreMap);
            for (CmsProduct product : fallback) {
                if (result.stream().noneMatch(item -> item.getId().equals(product.getId()))) {
                    result.add(product);
                }
            }
        }

        // 第二层兜底，允许从全部上架商品里补满，避免少用户场景空返回
        if (result.size() < 5) {
            List<CmsProduct> backupFallback = fallbackProducts(availableProducts, Collections.emptySet(), 5 - result.size(), popularityScoreMap);
            for (CmsProduct product : backupFallback) {
                if (result.stream().noneMatch(item -> item.getId().equals(product.getId()))) {
                    result.add(product);
                }
            }
        }

        return result.stream().limit(5).collect(Collectors.toList());
    }

    private void addScore(Map<String, Map<String, Double>> userProductScores, String userId, String productId, BigDecimal score) {
        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(productId) || score == null) {
            return;
        }
        userProductScores.computeIfAbsent(userId, key -> new HashMap<>())
                .merge(productId, score.doubleValue(), Double::sum);
    }

    private void addPopularity(Map<String, Double> popularityScore, String productId, BigDecimal score) {
        if (StringUtils.isEmpty(productId) || score == null) {
            return;
        }
        popularityScore.merge(productId, score.doubleValue(), Double::sum);
    }

    private BigDecimal getReviewWeight(CmdRecommendWeight weight, Long rating) {
        if (rating == null) {
            return weight.getReviewStar1Weight();
        }
        switch (rating.intValue()) {
            case 1:
                return weight.getReviewStar1Weight();
            case 2:
                return weight.getReviewStar2Weight();
            case 3:
                return weight.getReviewStar3Weight();
            case 4:
                return weight.getReviewStar4Weight();
            case 5:
                return weight.getReviewStar5Weight();
            default:
                return weight.getReviewStar1Weight();
        }
    }

    private double cosineSimilarity(Map<String, Double> left, Map<String, Double> right) {
        if (left == null || right == null || left.isEmpty() || right.isEmpty()) {
            return 0D;
        }

        Set<String> commonKeys = new HashSet<>(left.keySet());
        commonKeys.retainAll(right.keySet());
        if (commonKeys.isEmpty()) {
            return 0D;
        }

        double dotProduct = 0D;
        double leftNorm = 0D;
        double rightNorm = 0D;
        for (double value : left.values()) {
            leftNorm += value * value;
        }
        for (double value : right.values()) {
            rightNorm += value * value;
        }
        for (String key : commonKeys) {
            dotProduct += left.get(key) * right.get(key);
        }

        double denominator = Math.sqrt(leftNorm) * Math.sqrt(rightNorm);
        if (denominator == 0D) {
            return 0D;
        }
        return dotProduct / denominator;
    }
}