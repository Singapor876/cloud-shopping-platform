package com.cloud.cms.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CmsDashboardSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<MetricCard> metrics;
    private List<NameValue> productCategoryDistribution;
    private List<NameValue> productStatusDistribution;
    private List<NameValue> orderStatusDistribution;
    private List<NameValue> userBehaviorDistribution;
    private List<NameValue> paymentMethodDistribution;

    @Data
    public static class MetricCard implements Serializable {
        private static final long serialVersionUID = 1L;

        private String title;
        private String value;
        private String unit;
        private String note;
    }

    @Data
    public static class NameValue implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private BigDecimal value;
    }
}