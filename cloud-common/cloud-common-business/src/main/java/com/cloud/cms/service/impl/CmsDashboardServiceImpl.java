package com.cloud.cms.service.impl;

import com.cloud.cms.domain.CmsProduct;
import com.cloud.cms.domain.CmsProductCategory;
import com.cloud.cms.enums.ProductStatus;
import com.cloud.cms.domain.vo.CmsDashboardSummary;
import com.cloud.cms.service.ICmsDashboardService;
import com.cloud.cms.service.ICmsProductCategoryService;
import com.cloud.cms.service.ICmsProductService;
import com.cloud.common.core.utils.StringUtils;
import com.cloud.oms.domain.OmsOrder;
import com.cloud.oms.service.IOmsOrderService;
import com.cloud.ums.domain.UmsBrowseHistory;
import com.cloud.ums.domain.UmsFavorite;
import com.cloud.ums.domain.UmsProductReview;
import com.cloud.ums.domain.UmsUser;
import com.cloud.ums.service.IUmsBrowseHistoryService;
import com.cloud.ums.service.IUmsFavoriteService;
import com.cloud.ums.service.IUmsProductReviewService;
import com.cloud.ums.service.IUmsUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class CmsDashboardServiceImpl implements ICmsDashboardService {
    @Resource
    private ICmsProductService cmsProductService;
    @Resource
    private ICmsProductCategoryService cmsProductCategoryService;
    @Resource
    private IOmsOrderService omsOrderService;
    @Resource
    private IUmsUserService umsUserService;
    @Resource
    private IUmsBrowseHistoryService browseHistoryService;
    @Resource
    private IUmsFavoriteService favoriteService;
    @Resource
    private IUmsProductReviewService reviewService;

    @Override
    public CmsDashboardSummary getSummary() {
        List<CmsProduct> products = cmsProductService.list();
        List<CmsProductCategory> categories = cmsProductCategoryService.list();
        List<OmsOrder> orders = omsOrderService.list();
        List<UmsUser> users = umsUserService.list();
        List<UmsBrowseHistory> browseHistories = browseHistoryService.list();
        List<UmsFavorite> favorites = favoriteService.list();
        List<UmsProductReview> reviews = reviewService.list();

        CmsDashboardSummary summary = new CmsDashboardSummary();
        summary.setMetrics(buildMetricCards(products, orders, users, browseHistories, favorites, reviews));
        summary.setProductCategoryDistribution(buildProductCategoryDistribution(products, categories));
        summary.setProductStatusDistribution(buildProductStatusDistribution(products));
        summary.setOrderStatusDistribution(buildOrderStatusDistribution(orders));
        summary.setUserBehaviorDistribution(buildUserBehaviorDistribution(browseHistories, favorites, reviews, orders));
        summary.setPaymentMethodDistribution(buildPaymentMethodDistribution(orders));
        return summary;
    }

    private List<CmsDashboardSummary.MetricCard> buildMetricCards(List<CmsProduct> products,
                                                                   List<OmsOrder> orders,
                                                                   List<UmsUser> users,
                                                                   List<UmsBrowseHistory> browseHistories,
                                                                   List<UmsFavorite> favorites,
                                                                   List<UmsProductReview> reviews) {
        long onSaleCount = products.stream().filter(product -> ProductStatus.UNSOLD.getValue().equals(String.valueOf(product.getProductStatus()))).count();
        long soldOutCount = products.stream().filter(product -> ProductStatus.SOLD.getValue().equals(String.valueOf(product.getProductStatus()))).count();
        long lowStockCount = products.stream()
            .filter(product -> product.getStock() != null && product.getStock() > 0 && product.getStock() <= 10)
            .count();
        long stockTotal = products.stream().mapToLong(product -> product.getStock() == null ? 0L : product.getStock()).sum();
        long activeUsers = collectActiveUsers(browseHistories, favorites, reviews, orders).size();
        long orderUsers = collectUserIds(orders.stream().map(OmsOrder::getUserId).collect(Collectors.toList())).size();
        long repeatUsers = collectRepeatUsers(orders).size();
        long paidOrders = orders.stream().filter(order -> isPaid(order.getOrderStatus())).count();
        long completedOrders = orders.stream().filter(order -> isCompleted(order.getOrderStatus())).count();

        List<CmsDashboardSummary.MetricCard> metrics = new ArrayList<>();
        metrics.add(metric("On Sale Products", String.valueOf(onSaleCount), "items", "Currently listed and available products"));
        metrics.add(metric("Sold Out Products", String.valueOf(soldOutCount), "items", "Products that are out of stock and need restocking"));
        metrics.add(metric("Low Stock Products", String.valueOf(lowStockCount), "items", "Products with less than 10 in stock"));
        metrics.add(metric("Total Product Stock", String.valueOf(stockTotal), "items", "Total inventory of all products"));
        metrics.add(metric("Platform Users", String.valueOf(users.size()), "people", "Total registered users"));
        metrics.add(metric("Active Users", String.valueOf(activeUsers), "people", "Users with browsing, favorite, review, or order behavior"));
        metrics.add(metric("Ordering Users", String.valueOf(orderUsers), "people", "Users who have placed at least one order"));
        metrics.add(metric("Repeat Purchase Users", String.valueOf(repeatUsers), "people", "Users who have placed two or more orders"));
        metrics.add(metric("Paid Orders", String.valueOf(paidOrders), "orders", "Number of orders that have been paid"));
        metrics.add(metric("Completed Orders", String.valueOf(completedOrders), "orders", "Number of orders that have been fulfilled"));
        return metrics;
    }

    private CmsDashboardSummary.MetricCard metric(String title, String value, String unit, String note) {
        CmsDashboardSummary.MetricCard metricCard = new CmsDashboardSummary.MetricCard();
        metricCard.setTitle(title);
        metricCard.setValue(value);
        metricCard.setUnit(unit);
        metricCard.setNote(note);
        return metricCard;
    }

    private List<CmsDashboardSummary.NameValue> buildProductCategoryDistribution(List<CmsProduct> products, List<CmsProductCategory> categories) {
        Map<String, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(CmsProductCategory::getId, CmsProductCategory::getName, (left, right) -> left));
        Map<String, Long> categoryCountMap = products.stream()
                .filter(product -> StringUtils.isNotBlank(product.getCategoryId()))
                .collect(Collectors.groupingBy(CmsProduct::getCategoryId, Collectors.counting()));

        return categoryCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .map(entry -> {
                    CmsDashboardSummary.NameValue nameValue = new CmsDashboardSummary.NameValue();
                    nameValue.setName(categoryNameMap.getOrDefault(entry.getKey(), entry.getKey()));
                    nameValue.setValue(BigDecimal.valueOf(entry.getValue()));
                    return nameValue;
                })
                .collect(Collectors.toList());
    }

    private List<CmsDashboardSummary.NameValue> buildProductStatusDistribution(List<CmsProduct> products) {
        List<CmsDashboardSummary.NameValue> result = new ArrayList<>();
        result.add(nameValue("On Sale", products.stream().filter(product -> ProductStatus.UNSOLD.getValue().equals(String.valueOf(product.getProductStatus()))).count()));
        result.add(nameValue("Sold Out", products.stream().filter(product -> ProductStatus.SOLD.getValue().equals(String.valueOf(product.getProductStatus()))).count()));
        return result;
    }

    private List<CmsDashboardSummary.NameValue> buildOrderStatusDistribution(List<OmsOrder> orders) {
        Map<Long, String> statusNameMap = new HashMap<>();
        statusNameMap.put(0L, "Pending Payment");
        statusNameMap.put(1L, "Paid");
        statusNameMap.put(2L, "Cancelled");
        statusNameMap.put(3L, "Completed");

        Map<Long, Long> statusCountMap = orders.stream()
                .collect(Collectors.groupingBy(order -> order.getOrderStatus() == null ? 0L : order.getOrderStatus(), Collectors.counting()));

        List<Long> statusOrder = new ArrayList<>();
        statusOrder.add(1L);
        statusOrder.add(0L);
        statusOrder.add(2L);
        statusOrder.add(3L);

        List<CmsDashboardSummary.NameValue> result = new ArrayList<>();
        statusOrder.forEach(status -> {
            Long count = statusCountMap.get(status);
            if (count != null && count > 0) {
                CmsDashboardSummary.NameValue nameValue = new CmsDashboardSummary.NameValue();
                nameValue.setName(statusNameMap.getOrDefault(status, String.valueOf(status)));
                nameValue.setValue(BigDecimal.valueOf(count));
                result.add(nameValue);
            }
        });
        return result;
    }

    private List<CmsDashboardSummary.NameValue> buildUserBehaviorDistribution(List<UmsBrowseHistory> browseHistories,
                                                                              List<UmsFavorite> favorites,
                                                                              List<UmsProductReview> reviews,
                                                                              List<OmsOrder> orders) {
        List<CmsDashboardSummary.NameValue> result = new ArrayList<>();

        result.add(nameValue("Browsing Users", collectUserIds(browseHistories.stream().map(UmsBrowseHistory::getUserId).collect(Collectors.toList())).size()));
        result.add(nameValue("Favorite Users", collectUserIds(favorites.stream().map(UmsFavorite::getUserId).collect(Collectors.toList())).size()));
        result.add(nameValue("Reviewing Users", collectUserIds(reviews.stream().map(UmsProductReview::getUserId).collect(Collectors.toList())).size()));
        result.add(nameValue("Ordering Users", collectUserIds(orders.stream().map(OmsOrder::getUserId).collect(Collectors.toList())).size()));
        return result;
    }

    private List<CmsDashboardSummary.NameValue> buildPaymentMethodDistribution(List<OmsOrder> orders) {
        Map<String, Long> methodCountMap = orders.stream()
                .collect(Collectors.groupingBy(order -> StringUtils.isNotBlank(order.getPaymentMethod()) ? order.getPaymentMethod() : "Not Set", Collectors.counting()));

        return methodCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> nameValue(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private CmsDashboardSummary.NameValue nameValue(String name, long value) {
        CmsDashboardSummary.NameValue nameValue = new CmsDashboardSummary.NameValue();
        nameValue.setName(name);
        nameValue.setValue(BigDecimal.valueOf(value));
        return nameValue;
    }

    private Set<String> collectUserIds(List<String> userIds) {
        return userIds.stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());
    }

    private Set<String> collectActiveUsers(List<UmsBrowseHistory> browseHistories,
                                          List<UmsFavorite> favorites,
                                          List<UmsProductReview> reviews,
                                           List<OmsOrder> orders) {
        Set<String> activeUsers = new HashSet<>();
        browseHistories.forEach(item -> addUserId(activeUsers, item.getUserId()));
        favorites.forEach(item -> addUserId(activeUsers, item.getUserId()));
        reviews.forEach(item -> addUserId(activeUsers, item.getUserId()));
        orders.forEach(item -> addUserId(activeUsers, item.getUserId()));
        return activeUsers;
    }

    private Set<String> collectRepeatUsers(List<OmsOrder> orders) {
        Map<String, Long> orderCountByUser = orders.stream()
                .filter(order -> StringUtils.isNotBlank(order.getUserId()))
                .collect(Collectors.groupingBy(OmsOrder::getUserId, Collectors.counting()));
        return orderCountByUser.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue() >= 2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private void addUserId(Set<String> target, String userId) {
        if (StringUtils.isNotBlank(userId)) {
            target.add(userId);
        }
    }

    private boolean isPaid(Long orderStatus) {
        return orderStatus != null && orderStatus == 1L;
    }

    private boolean isCompleted(Long orderStatus) {
        return orderStatus != null && orderStatus == 3L;
    }
}