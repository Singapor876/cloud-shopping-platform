package com.cloud.oms.enums;

public enum LogisticsStatus {
    UNSHIPPED(0L, "Unshipped"),
    SHIPPED(1L, "Shipped"),
    RECEIVE(2L, "Received"),
    COMPLETE(3L, "Completed");

    private final Long value;
    private final String label;

    LogisticsStatus(Long value, String label) {
        this.value = value;
        this.label = label;
    }

    public Long getValue() { return value; }
    public String getLabel() { return label; }

    // Find enum by dictionary value
    public static LogisticsStatus fromValue(String value) {
        for (LogisticsStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
