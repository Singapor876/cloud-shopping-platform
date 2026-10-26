package com.cloud.cms.enums;

public enum ProductStatus {
    UNSOLD("1", "On Sale"),
    SOLD("2", "Sold Out");

    private final String value;
    private final String label;

    ProductStatus(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }

    // Find enum by dictionary value
    public static ProductStatus fromValue(String value) {
        for (ProductStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
