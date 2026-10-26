package com.cloud.oms.enums;

public enum PromotionType {
    ZK(0L, "Discount"),
    YH(1L, "Coupon");

    private final Long value;
    private final String label;

    PromotionType(Long value, String label) {
        this.value = value;
        this.label = label;
    }

    public Long getValue() { return value; }
    public String getLabel() { return label; }
}
