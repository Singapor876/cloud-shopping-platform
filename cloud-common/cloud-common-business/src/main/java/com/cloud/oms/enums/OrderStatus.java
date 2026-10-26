package com.cloud.oms.enums;

public enum OrderStatus {
    UNPAYMENT(0L, "Unpaid"),
    PAID(1L, "Paid"),
    CANCEL(2L, "Cancelled"),
    COMPLETE(3L, "Completed"),
    BACK(4L, "Refunded");

    private final Long value;
    private final String label;

    OrderStatus(Long value, String label) {
        this.value = value;
        this.label = label;
    }

    public Long getValue() { return value; }
    public String getLabel() { return label; }
}
