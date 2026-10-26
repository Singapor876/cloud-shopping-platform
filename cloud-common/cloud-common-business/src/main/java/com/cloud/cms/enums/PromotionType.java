package com.cloud.cms.enums;

public enum PromotionType {
    DISCOUNT("0", "Discount"),
    COUPON("1", "Coupon");

    private final String value;
    private final String label;

    PromotionType(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }

    // Find enum by dictionary value
    public static PromotionType fromValue(String value) {
        for (PromotionType status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
